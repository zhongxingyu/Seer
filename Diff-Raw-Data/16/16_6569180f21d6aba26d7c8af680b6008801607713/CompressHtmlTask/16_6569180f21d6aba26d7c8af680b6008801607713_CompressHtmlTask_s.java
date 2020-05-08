 package com.googlecode.htmlcompressor.ant.task;
 
 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
  
 import org.apache.tools.ant.Task;
 import com.googlecode.htmlcompressor.compressor.*;
 import java.io.*;
 import java.util.Vector;
 import java.util.Enumeration;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.BuildException;
 
 /**
  * Ant Task that wraps the htmlcompressor library written by Sergiy Kovalchuck
  * 
  * @author <a href="mailto:ron@bieberlabs.com">Ron Bieber</a>
  */
 public class CompressHtmlTask extends Task {
     private Vector filesets = new Vector();
     private String destdir = null;
     private boolean removeComments = true;
     private boolean removeJspComments = true;
     private boolean compressJS = false;
     private boolean compressCSS = true;
     private boolean skipStrutsFormTagComments = false;
     private boolean debugMode = false;
     
     /**
      * Main execution function of the Ant Task.
      */
      
     public void execute() {
         String sourceFileName = null, destFileName = null;
        
         // Validate that required properties are set 
         if (destdir == null || destdir.length() == 0) {
             throw new BuildException("CompressHTML:  You must specify a destination directory to deposit the new files ...");
         }
         
         if (filesets.size() == 0) {
             throw new BuildException("CompressHTML:  No files specified to process for compression.");
         }
         
         Enumeration e = filesets.elements();
         
         while (e.hasMoreElements()) {
             FileSet fs = (FileSet) e.nextElement();
             DirectoryScanner ds = fs.getDirectoryScanner(getProject());
             
             String[] includedFiles = ds.getIncludedFiles();
 
             // Show a message so the user knows we're actually doing something.
             log("Compressing " + includedFiles.length + " files to " + this.destdir + " ...");
                 
             
             for (int i = 0; i < includedFiles.length; i++) {
                 File f = new File(ds.getBasedir(), includedFiles[i]);
                 File d = new File(destdir, includedFiles[i]);
                 
                 File p = new File(d.getParent());
                 
                 if (!p.exists()) {
                     p.mkdirs();
                 }
                 
                 try {
                     destFileName = d.getCanonicalPath();
                     sourceFileName = f.getCanonicalPath();
                     
                     if (sourceFileName.compareToIgnoreCase(destFileName) == 0) {
                         throw new BuildException("CompressHTML:  Destination directory is included in source <fileset>, which may overwrite files in your source.");
                     }
                     
                     writeFile(d.toString(), compressHTML(readFile(f.toString(), null)), null);
                     
                 } catch (Exception myException) {
                     throw new BuildException(myException.getMessage() + " while processing file " + sourceFileName);
                 }
             }
         }
     }
        
     /**
      * Function called by the main execute function that does the actual compression of HTML
      * using the HTMLCompressor class.
      *
      * @param buffer This is the full HTML buffer to compress.
      * @return Compressed html buffer     
      */
     private String compressHTML(String buffer) {
         HtmlCompressor compressor = new HtmlCompressor();
         String newHTML = null;
     
         compressor.setEnabled(true); //if false all compression is off (default is true)
        compressor.setRemoveJspComments(this.removeJspComments);
        compressor.setRemoveComments(this.removeComments); //if false keeps HTML comments (default is true)
         compressor.setRemoveMultiSpaces(true); //if false keeps multiple whitespace characters (default is true)
         compressor.setRemoveIntertagSpaces(true);//removes iter-tag whitespace characters
         compressor.setRemoveQuotes(false); //removes unnecessary tag attribute quotes
        compressor.setCompressCss(this.compressCSS); //compress css using Yahoo YUI Compressor
        compressor.setCompressJavaScript(this.compressJS); //compress js using Yahoo YUI Compressor
         compressor.setYuiCssLineBreak(80); //--line-break param for Yahoo YUI Compressor
         compressor.setYuiJsDisableOptimizations(false); //--disable-optimizations param for Yahoo YUI Compressor
         compressor.setYuiJsLineBreak(-1); //--line-break param for Yahoo YUI Compressor
         compressor.setYuiJsNoMunge(false); //--nomunge param for Yahoo YUI Compressor
         compressor.setYuiJsPreserveAllSemiColons(true);//--preserve-semi param for Yahoo YUI Compressor
         compressor.setDebugMode(debugMode);
         
         // custom attribute to skip comments with Struts html:form TagElement
         compressor.setSkipStrutsFormComments(skipStrutsFormTagComments);
         
         try {
             newHTML = compressor.compress(buffer);
         } catch (Exception q) {
             throw new BuildException(q.toString());
         }
     
         return(newHTML);
     }
      
     private String readFile(String path, String encoding) {
         String finalString = null;
         try {
             // summary: reads a file and returns a string
             if (encoding == null) {
                 encoding = "utf-8";
             }
             
             File file = new File(path);
             
             BufferedReader input = new java.io.BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
             
             try {
                 StringBuffer stringBuffer = new StringBuffer();
                 String line = input.readLine();
                 String lineSeparator = System.getProperty("line.separator");
                 
                 // Byte Order Mark (BOM) - The Unicode Standard, version 3.0, page
                 // 324
                 // http://www.unicode.org/faq/utf_bom.html
                 
                 // Note that when we use utf-8, the BOM should appear as "EF BB BF",
                 // but it doesn't due to this bug in the JDK:
                 // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4508058
                 if (line != null && line.length() > 0 && line.charAt(0) == 0xfeff) {
                     // Eat the BOM, since we've already found the encoding on this
                     // file,
                     // and we plan to concatenating this buffer with others; the BOM
                     // should
                     // only appear at the top of a file.
                     line = line.substring(1);
                 }
                 
                 while (line != null) {
                     stringBuffer.append(line);
                     stringBuffer.append(lineSeparator);
                     line = input.readLine();
                 }
                 
                 // Make sure we return a JavaScript string and not a Java string.
                 return stringBuffer.toString(); // String
             } finally {
                 input.close();
             }
         } catch (Exception e) {
             // bubble the exception message back up and stop the build.
             throw new BuildException(e.toString());
         }
     }
     
     
     private void writeFile(String path, String contents, String encoding) {
         String finalString = null;
         try {
             // summary: reads a file and returns a string
             if (encoding == null) {
                 encoding = "utf-8";
             }
             
             File file = new File(path);
             String lineSeparator = System.getProperty("line.separator");
             BufferedWriter output = new java.io.BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
             try {
                 output.write(contents, 0, contents.length());
             } finally {
                 output.flush();
                 output.close();
             }
         } catch (Exception e) {
             // bubble the exception message back up and stop the build.
             throw new BuildException(e.toString());
             //e.printStackTrace();
         }
     }
     
     /**
      * Sets the fileset property that signifies the collection of files to be compressed by this 
      * invocation of the task.
      * 
      * @param set Ant FileSet object.
      * @return nothing
      */     
      
     public void addFileset(FileSet set) {
         filesets.addElement(set);
     }
  
     /**
      * Sets the property that causes the compressor to remove JSP comments from the files processed.
      * 
      * @param compress true if JSP comments should be removed, false otherwise.
      * @return nothing
      */ 
         
     public void setRemoveJspComments(boolean removeComments) {
         this.removeJspComments = removeComments;
     }
     
     
     /**
      * Sets the property that causes the compressor to remove HTML comments from the files processed.
      * 
      * @param compress true if comments should be removed, false otherwise.
      * @return nothing
      */ 
     public void setRemoveComments(boolean removeComments) {
         this.removeComments = removeComments;
     }
     
     /**
      * Sets the property that causes the compressor to compress inline Javascript with the YUI compressor.  
      * This is false by default.
      * 
      * @param compress true if inline Javascript compression should be compressed, false otherwise.
      * @return nothing
      */        
     public void setCompressJs(boolean compress) {
         this.compressJS = compress;
     }
     
     /**
      * Sets the property that causes the compressor compress CSS with the YUI compressor.  
      * This is true by default.
      * 
      * @param compress true if CSS compression is to be used, false otherwise.
      * @return nothing
      */       
     public void setCompressCSS(boolean compress) {
         this.compressCSS = compress;
     }
     
     /**
      * Sets the destination directory in which processed files are deposited.
      * 
      * @param destpath path of directory in which to deposit processed files
      * @return nothing
      */    
     public void setDestDir(String destpath) {
         destdir = destpath;
     }
     
     /**
      * Sets the property that causes the compressor to leave HTML comments that 
      * reference the Struts <html:form> tags.
      * 
      * @param skipFormComments true if <html:form> comments are to be skipped, false if they should be removed.
      * @return nothing
      */    
     public void setSkipStrutsFormComments(boolean skipFormComments) {
        skipStrutsFormTagComments = skipFormComments;
     }
     
     public void setDebug(boolean debug) {
        this.debugMode = debug;
     }
 }
