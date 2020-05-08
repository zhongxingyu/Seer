 /*
  * Dipankar Datta (dipdatta@user.sourceforge.net)
  * https://github.com/dipdatta/zfind
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package net.sf.util.zip;
 
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import net.sf.util.zip.analyzer.FileAnalyzerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  *  Immutable class for searching or listing (or finding within) a directory/archive file contents
  */
 
 
 public class ZFind {
 
     private File targetFile = null;
     private String fileName = null;
     private boolean ignoreCase = false;
     private boolean regularExpression = false;
     List<String> allEntries = null;
 
     /**
      * Constructor to create immutable ZFind object
      *
      * @param targetFile  the target directory or archive/compressed file where to look in
      *
      * @throws  IOException if target doesn't exists and/or it is
      * not a directory/archive/compressed file
      */
     public ZFind(File targetFile) throws IOException {
         if (!targetFile.exists()) {
             throw new IOException("Target file/folder must exist.");
         } else if (!FileNameUtil.isArchieveFile(targetFile.getName()) && !FileNameUtil.isCompressedFile(targetFile.getName()) && !targetFile.isDirectory()) {
             throw new IOException("Target must be either a directory or a zip file.");
         } else
             this.targetFile = targetFile;
     }
 
 
     /**
      * Iteratively searches within target directory/archive and accumulates all entries in a list. Subsequent calls
      * to this method will return same cached list. Call clearPreviousResult() to clean cache so that subsequent
      * getAllEntries() call will do a fresh listing again.
      *
      * @return A list of all file entries within target or an empty list if no entries found (or some error occured)
      */
     public List<String> getAllEntries() {
         if(allEntries==null){
         try {
             FileAnalyzerFactory factory=new FileAnalyzerFactory();
             allEntries=factory.getAnalyzer(targetFile).analyze(targetFile);
         } catch (Exception e) {
             e.printStackTrace();
             if(allEntries==null)
                 allEntries=new ArrayList<String>();
         }
         }
         return allEntries;
     }
 
 
     /**
      * cleans up internal entry cache list
      */
     public void clearPreviousResult(){
         allEntries.clear();
         allEntries=null;
     }
 
     /**
      * Iterates through the list of file entries to find out matching entries
      *
      * @return A list with file entries that matches provided file name
      * @throws IOException if some problem occurred while reading target
      */
     public List<String> getMatchedEntries() throws IOException {
         List<String> matchedEntries = null;
 
         if (fileName != null) {
             List<String> entries = getAllEntries();
             matchedEntries = new ArrayList<String>();
 
             if(!regularExpression && fileName.indexOf("*")!=-1){
                 setRegularExpression(true);
                fileName=fileName.replace(".","\\."); //any suffix . needs to be escaped in regex
                fileName=fileName.replace("*",".*");  //replace regular wildcard with equivalent regex
             }
 
             Pattern p = null;
             if (regularExpression) {
                 if (!fileName.endsWith("$")) //we want only node matching
                     fileName = fileName + "$";
               
                 p = (ignoreCase) ? Pattern.compile(fileName, Pattern.CASE_INSENSITIVE) : Pattern.compile(fileName);
             }
 
             for (String entry : entries) {
                //get the node for each entry
                int indx=(entry.indexOf("/")==-1)?0:entry.lastIndexOf("/")+1;
                String node=entry.substring(indx);

                 if (regularExpression) {
                    Matcher m = p.matcher(node);
                     if (m.find())
                         matchedEntries.add(entry);
                 } else {
                    String s = (ignoreCase) ? node.toLowerCase() : node;
                     String f = (ignoreCase) ? fileName.toLowerCase() : fileName;
                    if (s.equals(f))
                         matchedEntries.add(entry);
 
                 }
             }
         }
         return matchedEntries;
     }
 
 
     /**
      * the filename to search for. In regular filename, wildcard is supported (i.e. foo*.bar). If it's a
      * regular expression, you must also call setRegularExpression(..) to set regularExpression flag
      *
      * @param fileName filename to search for
      */
     public void setSearchFileName(String fileName) {
         this.fileName = fileName;
     }
 
 
     /**
      * Sets the ignoreCase flag. If true, a case insensitive search will be performed
      *
      * @param ignoreCase the ignoreCase flag
      */
     public void setIgnoreCase(boolean ignoreCase) {
         this.ignoreCase = ignoreCase;
     }
 
 
     /**
      * sets the regularExpression flag. If true, fileName will be considered as a regex pattern
      *
      * @param regularExpression  the regularExpression flag
      */
     public void setRegularExpression(boolean regularExpression) {
         this.regularExpression = regularExpression;
     }
 
 
     private static void printHelpOnError(String msg, OptionParser parser) throws IOException {
         System.err.println(msg + "\n\n");
         System.out.println("USAGE: java -jar zfind.jar -t <target file or folder where to look> [OPTIONS]\n");
         parser.printHelpOn(System.out);
         System.exit(0);
     }
 
     public static void main(String[] args) throws IOException {
 
         OptionParser parser = new OptionParser() {
             {
                 accepts("t").withRequiredArg().required().describedAs("target folder or zip file where to look");
                 accepts("h", "show help");
                 accepts("f").withRequiredArg().describedAs("file name to search)");
                 accepts("r", "treat <file name to search> as regular expression pattern");
                 accepts("i", "perform case insensitive search");
             }
         };
 
 
         OptionSet options = null;
 
         if (args.length == 0)
             printHelpOnError("Error parsing arguments. At least target zip file/folder option (-t) must be provided.", parser);
         else {
             //search is it's a help request
             for (String opt : args) {
                 if (opt.equals("-h")) //print help and exit
                     printHelpOnError("", parser);
             }
         }
 
         try {
             options = parser.parse(args);
         } catch (Throwable e) {
             printHelpOnError("Error parsing arguments. Please check for correct arguments.", parser);
         }
 
         File tagetF = new File(options.valueOf("t").toString());
         ZFind zFind = null;
         try {
             zFind = new ZFind(tagetF);
         } catch (IllegalArgumentException i) {
             printHelpOnError(i.getMessage(), parser);
         }
         if (options.has("r"))
             zFind.setRegularExpression(true);
 
         if (options.has("i"))
             zFind.setIgnoreCase(true);
 
         boolean isFind = false;
         if (options.has("f")) {
             zFind.setSearchFileName(options.valueOf("f").toString());
             isFind = true;
         }
 
         List<String> entries = (isFind) ? zFind.getMatchedEntries() : zFind.getAllEntries();
         System.out.println(entries.size() + " Total entries found.\n");
         for (int i = 0; i < entries.size(); i++) {
             System.out.println(entries.get(i));
         }
 
 
     }
 }
