 /** 
  *  Copyright 2011 Applied Research in Patacriticism and the University of Virginia
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  **/
 package org.nines;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CodingErrorAction;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Cleaner for full text files. It will fix escape sequences, strip bad 
  * utf-8 characters and normalize whitespace. The result will overwrite 
  * the prior full text file. It should ony be run once 
  * 
  * @author loufoster
  *
  */
 public class FullTextCleaner {
     private CharsetDecoder decoder;
     private ErrorReport errorReport;
     private String archiveName;
     private Logger log;
     private String custom;
     private long totalOrigChars = 0;
     private long totalFilesChanged = 0;
     private long totalCleanedChars = 0;
     
     public FullTextCleaner (String archiveName, ErrorReport errorReport, String custom) {
         this.errorReport = errorReport;
         this.archiveName = archiveName;
         this.log = Logger.getLogger(FullTextCleaner.class.getName());
         this.custom = custom;
         
         Charset cs = Charset.availableCharsets().get("UTF-8");
         this.decoder = cs.newDecoder();
         this.decoder.onMalformedInput(CodingErrorAction.REPLACE);
         this.decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
     }
     
     public void clean(File txtFile) {
         
         this.log.info("Clean full text from file "+txtFile.toString());
         
         // Read the text from the file. Bail if this fails
         String content = null;
         InputStreamReader is = null;
         try {
             is = new InputStreamReader(new FileInputStream(txtFile), this.decoder);
             content =  IOUtils.toString(is);
         } catch ( Exception e ) {
             this.errorReport.addError( 
                 new IndexerError(txtFile.toString(), "", "Unable to read full text file: " + e.toString()));
             return;
             
         } finally {
             IOUtils.closeQuietly(is);
         }  
         
         // stats!
         long startChars = content.length();;
         this.totalOrigChars += startChars;
         
         // clean it up
        String cleaned = TextUtils.stripEscapeSequences(content, this.errorReport, txtFile);
        cleaned = TextUtils.normalizeWhitespace(cleaned);
         
         // Look for unknown character and warn
        int pos = cleaned.indexOf("\ufffd");
         if (pos > -1) {
 
            String snip = cleaned.substring(Math.max(0, pos - 25), Math.min(cleaned.length(), pos + 25));
             errorReport.addError(new IndexerError(txtFile.toString(), "", "Invalid UTF-8 character at position " + pos
                 + "\n  Snippet: [" + snip + "]"));
         }
         
         if ( custom != null && custom.length() > 0) {
             try {
                 @SuppressWarnings("rawtypes")
                 Class newClass  = Class.forName("org.nines.cleaner."+this.custom);
                 ICustomCleaner cleaner = (ICustomCleaner)newClass.newInstance();
                cleaned = cleaner.clean(this.archiveName, cleaned);
             } catch (Exception e) {
                 errorReport.addError(new IndexerError(txtFile.toString(), "", "Unable to run custom cleaner " 
                     + this.custom +": " + e.toString()));
             }
         }
         
         // final stats
         long endChars = content.length();
         this.totalCleanedChars += endChars;
         if ( endChars != startChars ) {
             this.totalFilesChanged++;
         }
         this.log.info("  => Original length: "+startChars+", Cleaned length: "+endChars+", Delta:"+(startChars - endChars) );
         
         // write out the cleaned content over the existing content
         Writer outWriter = null;
         try {
             outWriter = new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8");
            outWriter.write( cleaned );
         } catch (IOException e) {
             this.errorReport.addError( 
                 new IndexerError(txtFile.toString(), "", "Unable to write cleaned text file: " + e.toString()));
         } finally {
             IOUtils.closeQuietly(outWriter);
         }
     }
     
     public long getTotalFilesChanged() {
         return this.totalFilesChanged;
     }
     
     public long getOriginalLength() {
         return this.totalOrigChars;
     }
     
     public long getCleanedLength() {
         return this.totalCleanedChars;
     }
 }
