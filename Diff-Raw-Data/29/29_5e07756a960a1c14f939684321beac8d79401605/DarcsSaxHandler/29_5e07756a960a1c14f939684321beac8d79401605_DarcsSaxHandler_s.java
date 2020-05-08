 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 package org.jenkinsci.plugins.darcs;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  *
  * @author sxs
  */
 public class DarcsSaxHandler extends DefaultHandler {
 
     private static final Logger LOGGER = Logger.getLogger(DarcsSaxHandler.class.getName());
 
     public enum DarcsChangelogTag {
         CHANGELOG,
         PATCH,
         NAME,
         COMMENT,
         SUMMARY,
         MODIFY_FILE,
         ADD_FILE,
         REMOVE_FILE,
         MOVE_FILE,
         ADDED_LINES,
         REMOVED_LINES,
         ADD_DIRECTORY,
         REMOVE_DIRECTORY;
     }
 
     private DarcsChangelogTag currentTag;
     private DarcsChangeSet currentChangeset;
     private boolean ready;
     private List<DarcsChangeSet> changeSets;
     private StringBuilder literal;
 
     public DarcsSaxHandler() {
         super();
         ready = false;
         changeSets = new ArrayList<DarcsChangeSet>();
     }
 
     public boolean isReady() {
         return ready;
     }
 
     public List<DarcsChangeSet> getChangeSets() {
         return changeSets;
     }
 
     @Override
     public void endDocument() {
         ready = true;
     }
 
     private void recognizeTag(String tagName) {
         if ("changelog".equals(tagName)) {
             currentTag = DarcsChangelogTag.CHANGELOG;
         } else if ("patch".equals(tagName)) {
             currentTag = DarcsChangelogTag.PATCH;
         } else if ("name".equals(tagName)) {
             currentTag = DarcsChangelogTag.NAME;
         } else if ("comment".equals(tagName)) {
             currentTag = DarcsChangelogTag.COMMENT;
         } else if ("summary".equals(tagName)) {
             currentTag = DarcsChangelogTag.SUMMARY;
         } else if ("modify_file".equals(tagName)) {
             currentTag = DarcsChangelogTag.MODIFY_FILE;
         } else if ("add_file".equals(tagName)) {
             currentTag = DarcsChangelogTag.ADD_FILE;
         } else if ("remove_file".equals(tagName)) {
             currentTag = DarcsChangelogTag.REMOVE_FILE;
         } else if ("move".equals(tagName)) {
             currentTag = DarcsChangelogTag.MOVE_FILE;
         } else if ("added_lines".equals(tagName)) {
             currentTag = DarcsChangelogTag.ADDED_LINES;
         } else if ("removed_lines".equals(tagName)) {
             currentTag = DarcsChangelogTag.REMOVED_LINES;
         } else if ("add_directory".equals(tagName)) {
             currentTag = DarcsChangelogTag.ADD_DIRECTORY;
         } else if ("remove_directory".equals(tagName)) {
             currentTag = DarcsChangelogTag.REMOVE_DIRECTORY;
         } else {
             LOGGER.log(Level.WARNING, "Unrecognized tag <" + tagName + ">!");
         }
     }
 
     @Override
     public void startElement(String uri, String name, String qName, Attributes atts) {
         if (DarcsChangelogTag.MODIFY_FILE == currentTag) {
             currentChangeset.getModifiedPaths().add(literal.toString());
         }
         
         recognizeTag(qName);
 
         if (DarcsChangelogTag.PATCH == currentTag) {
             currentChangeset = new DarcsChangeSet();
             currentChangeset.setAuthor(atts.getValue("author"));
             currentChangeset.setDate(atts.getValue("date"));
             currentChangeset.setLocalDate(atts.getValue("local_date"));
             currentChangeset.setHash(atts.getValue("hash"));
 
             if (atts.getValue("inverted").equals("True")) {
                 currentChangeset.setInverted(true);
             } else if (atts.getValue("inverted").equals("False")) {
                 currentChangeset.setInverted(false);
             }
         } else if (DarcsChangelogTag.MOVE_FILE == currentTag) {
             currentChangeset.getDeletedPaths().add(atts.getValue("from"));
             currentChangeset.getAddedPaths().add(atts.getValue("to"));
         }
 
         literal = new StringBuilder();
     }
 
     @Override
     public void endElement(String uri, String name, String qName) {
         recognizeTag(qName);
         
         switch (currentTag) {
             case PATCH:
                 changeSets.add(currentChangeset);
                 break;
             case NAME:
                 currentChangeset.setName(literal.toString());
                 break;
             case COMMENT:
                 String comment = stripIgnoreThisFromComment(literal.toString());
                 currentChangeset.setComment(comment);
                 break;
             case ADD_FILE:
             case ADD_DIRECTORY:
                 currentChangeset.getAddedPaths().add(literal.toString());
                 break;
             case REMOVE_FILE:
             case REMOVE_DIRECTORY:
                 currentChangeset.getDeletedPaths().add(literal.toString());
                 break;
         }
         
         currentTag = null;
     }
 
     /**
      * Strips out strings like Ignore-this: 606c40ef0d257da9b7a916e7f1c594aa.
      * 
     * It is asumed that the after the colon a single white space character occures and the
     * whole string with the hash occureas at the beginning og an commit message and ends with 
     * two new lines.
     *
     * @param comment
     * @return 
      */
     public static String stripIgnoreThisFromComment(String comment) {
         if (comment.startsWith("Ignore-this:")) {
            int end = comment.indexOf("\n\n");
             
             if (-1 == end) {
                 return "";
             }
             
            return comment.substring(end + 2);
         }
         
         return comment;
     }
     
     private boolean isWhiteSpace(char c) {
         switch (c) {
             case '\n':
             case '\r':
             case '\t':
             case ' ':
                 return true;
             default:
                 return false;
         }
     }
 
     private boolean skipWhiteSpace() {
         return DarcsChangelogTag.NAME != currentTag && DarcsChangelogTag.COMMENT != currentTag;
     }
 
     @Override
     public void characters(char ch[], int start, int length) {
         for (int i = start; i < start + length; i++) {
             if (isWhiteSpace(ch[i]) && skipWhiteSpace()) {
                 continue;
             }
 
             literal.append(ch[i]);
         }
     }
 
     @Override
     public void error(SAXParseException saxpe) {
         LOGGER.log(Level.WARNING, saxpe.toString());
     }
 
     @Override
     public void fatalError(SAXParseException saxpe) {
         LOGGER.log(Level.WARNING, saxpe.toString());
     }
 
     @Override
     public void warning(SAXParseException saxpe) {
         LOGGER.log(Level.WARNING, saxpe.toString());
     }
 }
