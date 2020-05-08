 /*
 * $Id: CVSHistory.java,v 1.6 2004-06-11 07:12:37 marion Exp $
  * Copyright (C) 2004 Klaus Rennecke, all rights reserved.
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package net.sf.fraglets.cca;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 
 import net.sourceforge.cruisecontrol.CruiseControlException;
 import net.sourceforge.cruisecontrol.Modification;
 import net.sourceforge.cruisecontrol.SourceControl;
 
 import org.apache.log4j.Logger;
 
 /**
  * Perform modification check based on a plain history file.
  * 
  * @author  Klaus Rennecke
 * @version $Revision: 1.6 $
  */
 public class CVSHistory implements SourceControl {
     /** The properties set for ANT. */
     private Hashtable properties = new Hashtable();
 
     /** The property name for the property to set on modifications. */
     private String onModifiedName;
 
     /** The property name for the property to set on deletions. */
     private String onDeletedName;
 
     /** The file name of the history file to read. */
     private String historyFileName;
 
     /** The URL where to fetch the history. */
     private String historyUrl;
     
     /** The URL of the viewcvs gateway. */
     private String viewcvsUrl;
 
     /** The module name to check for. */
     private List modules = new ArrayList();
 
     /** Logger for this class. */
     private static Logger log = Logger.getLogger(CVSHistory.class);
 
     /** Flag indicating that a deletion was seen. */
     private boolean deletionSeen;
 
     /** Minimum line length for a valid history entry. */
     private static final int MINIMUM_HISTORY_LINE = 10;
 
     /** Time conversion factor from un*x time to java time. */
     private static final int TIME_MULTIPLIER = 1000;
 
     /** End index of the timestamp. */
     private static final int TIMESTAMP_END = 9;
 
     /** Radix of the history timestamp. */
     private static final int TIMESTAMP_RADIX = 16;
 
     /**
      * Get the modifications recorded in the history file.
      * @param lastBuild the last build
      * @param now the check time
      */
     public List getModifications(Date lastBuild, Date now) {
         List result = new ArrayList();
         try {
             Reader fin;
             if (historyFileName != null) {
                 fin = new FileReader(historyFileName);
             } else {
                 fin = openUrl(historyUrl);
             }
             try {
                 BufferedReader in = new BufferedReader(fin);
                 String line;
                 while ((line = in.readLine()) != null) {
                     parseModification(line, result, lastBuild, now);
                 }
 
                 if (onModifiedName != null && result.size() > 0) {
                     properties.put(onModifiedName, "true"); //$NON-NLS-1$
                 }
                 if (onDeletedName != null && deletionSeen) {
                     properties.put(onDeletedName, "true"); //$NON-NLS-1$
                 }
             } finally {
                 fin.close();
             }
         } catch (IOException e) {
             log.error(Messages.getString(
                 "CVSHistory.Failed_to_read_history_file"), e); //$NON-NLS-1$
         }
         return result;
     }
 
     /**
      * @param line the line to parse
      * @param result the list where to put results
      * @param lastBuild lower bound
      * @param now upper bound
      * @param cvslog the CVS log map, updated as necessary
      */
     private void parseModification(
         String line,
         List result,
         Date lastBuild,
         Date now)
         throws MalformedURLException, UnsupportedEncodingException, IOException {
         if (line.length() >= MINIMUM_HISTORY_LINE) {
             String type;
             switch (line.charAt(0)) {
                 case 'A' :
                     type = "added"; //$NON-NLS-1$
                     break;
                 case 'M' :
                     type = "modified"; //$NON-NLS-1$
                     break;
                 case 'R' :
                     type = "deleted"; //$NON-NLS-1$
                     break;
                 default :
                     // other entries ignored
                     return;
             }
 
             String timestamp = line.substring(1, TIMESTAMP_END);
             long time =
                 Long.parseLong(timestamp, TIMESTAMP_RADIX) * TIME_MULTIPLIER;
             if (time < lastBuild.getTime()) {
                 return; // too old
             } else if (time > now.getTime()) {
                 return; // too young
             }
 
             try {
                     StringTokenizer tok = new StringTokenizer(
                         line.substring(TIMESTAMP_END + 1), "|", //$NON-NLS-1$
                         true);
                 String userName = tok.nextToken();
                 tok.nextToken(); // delimiter
                 tok.nextToken(); // working directory
                 tok.nextToken(); // delimiter
                 String folderName = tok.nextToken();
 
                 Module module = matchModule(folderName);
                 if (module == null) {
                     return; // not in modules
                 }
                 
                 tok.nextToken(); // delimiter
                 String revision = tok.nextToken();
                 tok.nextToken(); // delimiter
                 String fileName = tok.nextToken();
 
                 String comment =
                     fetchLog(
                         folderName + '/' + fileName,
                         revision,
                         module.getBranch());
                 if (comment == null) {
                     return; // modification on a different branch
                 }
                 
                 Modification modification = new Modification();
                 modification.type = type;
                 modification.modifiedTime = new Date(time);
                 modification.userName = userName;
                 modification.folderName = folderName;
                 modification.fileName = fileName;
                 modification.revision = revision;
                 modification.comment = comment;
 
                 if (type.equals("deleted")) { //$NON-NLS-1$
                     deletionSeen = true;
                 }
                 
                 result.add(modification);
             } catch (NoSuchElementException e) {
                 log.warn(Messages.getString(
                     "CVSHistory.Invalid_history_format") //$NON-NLS-1$
                     + line + "\""); //$NON-NLS-1$
             }
         }
     }
 
     /**
      * Fetch the log entry for the given file revision, returning its
      * commit message. If the revision does not exist on the given
      * branch, null is returned.
      * 
      * @param fileName the file name to fetch.
      * @param revision a revision to fetch.
      * @param branch the branch to select, or null.
      * @param cvslog the CVS log map, updated as necessary.
      * @return the commit message, or null.
      */
     private String fetchLog(String fileName, String revision, String branch)
         throws MalformedURLException, UnsupportedEncodingException, IOException {
         if (viewcvsUrl == null) {
             // cannot fetch log
             return ""; //$NON-NLS-1$
         }
         
         Reader reader;
         if (branch != null) {
             reader =
                 openUrl(
                     viewcvsUrl
                        + urlquote(fileName)
                         + "?only_with_tag=" //$NON-NLS-1$
                         + urlquote(branch));
         } else {
             reader = openUrl(viewcvsUrl + urlquote(fileName));
         }
         
         reader = new BufferedReader(reader);
         try {
             for (;;) {
                 skipTag(reader, "hr"); //$NON-NLS-1$
                 if (!"Revision".equals(readToken(reader))) { //$NON-NLS-1$
                     continue;
                 }
                 if (!revision.equals(readToken(reader))) {
                     continue;
                 }
                 return readBlock(reader, "pre");
             }
         } catch (EOFException e) {
         } finally {
             reader.close();
         }
         
         return null;
     }
     
     /**
      * Open a URL and return a Reader that uses the appropriate encoding.
      * @param url a URL.
      * @return an open Reader.
      * @throws IOException propagated.
      * @throws MalformedURLException propagated.
      * @throws UnsupportedEncodingException propagated.
      */
     private static Reader openUrl(String url)
         throws IOException, MalformedURLException, UnsupportedEncodingException {
         Reader fin;
         URLConnection connection = new URL(url).openConnection();
         String encoding = connection.getContentEncoding();
         if (encoding == null) {
             fin = new InputStreamReader(connection.getInputStream());
         } else {
             fin = new InputStreamReader(connection.getInputStream(), encoding);
         }
         return fin;
     }
 
     private static void skipTo(Reader reader, String mark) throws IOException {
         int end = mark.length();
         int scan = 0;
         while (scan < end){
             int c = reader.read();
             if (c == mark.charAt(scan)) {
                 scan += 1;
             } else if (c == -1) {
                 throw new EOFException();
             } else {
                 scan = 0;
             }
         }
     }
     
     private static boolean matchText(Reader reader, String text) throws IOException {
         int end = text.length();
         int scan = 0;
         reader.mark(end);
         while (scan < end){
             int c = reader.read();
             if (c == text.charAt(scan)) {
                 scan += 1;
             } else if (c == -1) {
                 throw new EOFException();
             } else {
                 reader.reset();
                 return false;
             }
         }
         return true;
     }
     
     private static String readToken(Reader reader) throws IOException {
         StringBuffer buffer = new StringBuffer();
         for (;;) {
             reader.mark(1);
             int c = reader.read();
             if (c == -1) {
                 break;
             } else if (Character.isWhitespace((char)c)) {
                 if (buffer.length() > 0) {
                     break;
                 }
             } else if (c == '<') {
                 if (buffer.length() > 0) {
                     break;
                 }
                 skipTo(reader, ">"); //$NON-NLS-1$
             } else {
                 buffer.append((char)c);
             }
         }
         if (buffer.length() > 0) {
             reader.reset();
             return buffer.toString();
         } else {
             throw new EOFException();
         }
     }
     
     private static String readBlock(Reader reader, String tagName) throws IOException {
         skipTag(reader, tagName); //$NON-NLS-1$
         StringBuffer buffer = new StringBuffer();
         for (;;) {
             int c = reader.read();
             if (c == -1) {
                 break;
             } else if (c == '<') {
                 if (matchText(reader, "/"+tagName)) {
                     skipTo(reader, ">"); //$NON-NLS-1$
                     break;
                 }
             }
             buffer.append((char)c);
         }
         return buffer.toString();
     }
     
     private static void skipWhite(Reader reader) throws IOException {
         do {
             reader.mark(1);
         } while (Character.isWhitespace((char)reader.read()));
         reader.reset();
     }
     
     private static void skipTag(Reader reader, String tagName)
         throws IOException {
         String found;
         do {
             found = readTag(reader, "<");
             skipTo(reader, ">"); //$NON-NLS-1$
         } while (!tagName.equals(found));
     }
     
     private static String readTag(Reader reader, String start)
         throws IOException {
         skipTo(reader, start);
         skipWhite(reader);
         StringBuffer buffer = new StringBuffer();
         for (;;) {
             reader.mark(1);
             int c = reader.read();
             if (c == -1) {
                 break;
             } else if (Character.isLetter((char)c)) {
                 buffer.append((char)c);
             } else {
                 break;
             }
         }
         reader.reset();
         return buffer.toString();
     }
 
     /**
      * Quote the give string for use in a URL.
      * @param string a string.
      * @return the quoted string.
      */
     private String urlquote(String string) {
         byte[] data;
         try {
             data = string.getBytes("UTF-8"); //$NON-NLS-1$
         } catch (UnsupportedEncodingException e) {
             // UTF-8 must be supported
             throw new RuntimeException(e.toString());
         }
 
         StringBuffer buffer = new StringBuffer();
         for (int i = 0; i < data.length; i++) {
             char c = (char) (data[i] & 0xff);
             if (c == '/'
                 || c == '-'
                 || c == '_'
                 || c == '.'
                 || c == '*'
                 || Character.isLetterOrDigit(c)) {
                 if (buffer != null) {
                     buffer.append(c);
                 }
             } else if (c == ' ') {
                 buffer.append('+');
             } else {
                 if (buffer == null) {
                     buffer = new StringBuffer(string.substring(0, i));
                 }
                 buffer.append('%');
                 if (c < 16) {
                     buffer.append('0');
                 }
                 buffer.append(Integer.toHexString(c));
             }
         }
 
         return buffer.toString();
     }
 
     /**
      * Check of the given folder name is in the set of modules configured.
      * @param folderName the folder name to check.
      * @return the module for the given folder, or null.
      */
     public Module matchModule(String folderName) {
         int scan = modules.size();
         if (scan == 0) {
             return Module.DEFAULT_MODULE; // no modules set
         }
 
         while (--scan >= 0) {
             Module module = (Module)modules.get(scan);
             String moduleName = module.getName();
             if (!folderName.startsWith(moduleName)) {
                 continue; // does not match
             } else if (
                 folderName.length() > moduleName.length()
                     && folderName.charAt(moduleName.length()) != '/') {
                 continue; // different folder with equal prefix
             } else {
                 return module;
             }
         }
 
         return null; // not found
     }
 
     /**
      * @see net.sourceforge.cruisecontrol.SourceControl#validate()
      */
     public void validate() throws CruiseControlException {
         if (historyFileName == null) {
             if (historyUrl == null) {
                 throw new CruiseControlException(Messages.getString(
                     "CVSHistory.history_required")); //$NON-NLS-1$
             } else {
                 try {
                     new URL(historyUrl).toExternalForm();
                 } catch (MalformedURLException e) {
                     throw new CruiseControlException(Messages.getString(
                         "CVSHistory.Malformed_historyurl"), e); //$NON-NLS-1$
                 }
             }
         } else if (historyUrl != null) {
             throw new CruiseControlException(Messages.getString(
                 "CVSHistory.Either_historyfilename_or_historyurl")); //$NON-NLS-1$
         } else if (!new File(historyFileName).isFile()) {
             // changed to warning to allow bootstrapping the history
             log.warn(Messages.getString("CVSHistory.History_file") //$NON-NLS-1$
                 + historyFileName
                 + Messages.getString("CVSHistory.does_not_exist")); //$NON-NLS-1$
         }
         if (viewcvsUrl != null) {
             try {
                 new URL(viewcvsUrl).toExternalForm();
             } catch (MalformedURLException e) {
                 throw new CruiseControlException(Messages.getString(
                     "CVSHistory.Malformed_viewcvsurl"), e); //$NON-NLS-1$
             }
         }
     }
 
     /**
      * @see net.sourceforge.cruisecontrol.SourceControl#getProperties()
      */
     public Hashtable getProperties() {
         return properties;
     }
 
     /**
      * Set the property name for the property to set on modification.
      * @see net.sourceforge.cruisecontrol.SourceControl#setProperty(java.lang.String)
      */
     public void setProperty(String property) {
         onModifiedName = property;
     }
 
     /**
      * Set the property name for the property to set on deletions.
      * @see net.sourceforge.cruisecontrol.SourceControl#setPropertyOnDelete(java.lang.String)
      */
     public void setPropertyOnDelete(String property) {
         onDeletedName = property;
     }
 
     /**
      * @return the history file name
      */
     public String getHistoryFileName() {
         return historyFileName;
     }
 
     /**
      * @param string the new history file name
      */
     public void setHistoryFileName(String string) {
         historyFileName = string;
     }
 
     /**
      * @return the history url
      */
     public String getHistoryUrl() {
         return historyUrl;
     }
 
     /**
      * @param string the new history url
      */
     public void setHistoryUrl(String string) {
         historyUrl = string;
     }
 
     /**
      * @return the viewcvs URL.
      */
     public String getViewcvsUrl() {
         return viewcvsUrl;
     }
 
     /**
      * @param string the new viewcvs URL.
      */
     public void setViewcvsUrl(String string) {
         // make sure it ends with a slash
         if (string == null || string.endsWith("/")) { //$NON-NLS-1$
             viewcvsUrl = string;
         } else {
             viewcvsUrl = string + '/';
         }
     }
 
     /**
      * @param string the new module name
      */
     public void setModule(String string) {
         createModule().setName(string);
     }
 
     /**
      * Create a new sub-element for a module.
      * @return the new module sub-element.
      */
     public Module createModule() {
         Module result = new Module();
         modules.add(result);
         return result;
     }
 
     /**
      * Bean implementation for the module sub-element.
      * @since 01.03.2004
      * @author Klaus Rennecke
     * @version $Revision: 1.6 $
      */
     public static class Module {
         
         public static final Module DEFAULT_MODULE = new Module();
 
         /** The module name. */
         private String name;
 
         /** The branch name. */
         private String branch;
 
         /**
          * @return the name
          */
         public String getName() {
             return name;
         }
 
         /**
          * @param string the new name
          */
         public void setName(String string) {
             name = string;
         }
 
         /**
          * @return the branch.
          */
         public String getBranch() {
             return branch;
         }
 
         /**
          * @param string the new branch.
          */
         public void setBranch(String string) {
             branch = string;
         }
 
     }
     
 }
