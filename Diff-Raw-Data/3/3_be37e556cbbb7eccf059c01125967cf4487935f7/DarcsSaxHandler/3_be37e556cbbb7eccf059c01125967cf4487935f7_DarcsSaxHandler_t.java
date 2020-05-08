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
         REMOVED_LINES;
     }

     private DarcsChangelogTag currentTag;
     private DarcsChangeSet currentChangeset;
     private boolean ready;
     private List<DarcsChangeSet> changeSets;
 
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
         }
     }
 
     @Override
     public void startElement(String uri, String name, String qName, Attributes atts) {
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
     }
 
     @Override
     public void endElement(String uri, String name, String qName) {
         recognizeTag(qName);
 
         if (DarcsChangelogTag.PATCH == currentTag) {
             changeSets.add(currentChangeset);
             currentTag = null;
         }
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
 
     @Override
     public void characters(char ch[], int start, int length) {
         String literal = "";
 
         for (int i = start; i < start + length; i++) {
             if (isWhiteSpace(ch[i])) {
                 continue;
             }
 
             literal += ch[i];
         }
 
         if (literal.equals("")) {
             return;
         }
 
         switch (currentTag) {
             case NAME:
                 currentChangeset.setName(literal);
                 break;
             case COMMENT:
                 currentChangeset.setComment(literal);
                 break;
             case ADD_FILE:
                 currentChangeset.getAddedPaths().add(literal);
                 break;
             case REMOVE_FILE:
                 currentChangeset.getDeletedPaths().add(literal);
                 break;
             case MODIFY_FILE:
                 currentChangeset.getModifiedPaths().add(literal);
                 break;
         }
     }
 
     @Override
     public void error(SAXParseException saxpe) {
         LOGGER.log(Level.WARNING, saxpe.toString());
     }
 }
