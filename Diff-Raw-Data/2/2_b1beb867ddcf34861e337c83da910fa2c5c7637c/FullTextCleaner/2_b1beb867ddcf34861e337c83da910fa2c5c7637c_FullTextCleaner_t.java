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
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
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
     
     public FullTextCleaner (String archiveName, ErrorReport errorReport) {
         this.errorReport = errorReport;
         this.archiveName = archiveName;
         this.log = Logger.getLogger(FullTextCleaner.class.getName());
         
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
         
         // clean it up
         String cleaned = TextUtils.stripEscapeSequences(content, this.errorReport, txtFile);
         cleaned = TextUtils.normalizeWhitespace(cleaned);
 
         
         // special case for CALI
         if ( this.archiveName.equals("cali")) {
            cleaned = stripJunk(cleaned, "Search Text:", "fetching image...");
         }
         
         // Look for unknown character and warn
         int pos = cleaned.indexOf("\ufffd");
         if (pos > -1) {
 
             String snip = cleaned.substring(Math.max(0, pos - 25), Math.min(cleaned.length(), pos + 25));
             errorReport.addError(new IndexerError(txtFile.toString(), "", "Invalid UTF-8 character at position " + pos
                 + "\n  Snippet: [" + snip + "]"));
         }
         
         // write out the cleaned content over the existing content
         FileWriter fw = null;
         try {
             fw = new FileWriter(txtFile);
             fw.write(cleaned);
         } catch (IOException e) {
             this.errorReport.addError( 
                 new IndexerError( txtFile.toString(), "", "Unable to write cleaned text file: " + e.toString()));
         } finally {
             IOUtils.closeQuietly(fw);
         }
     }
     
     private String stripJunk(String content, String startWord, String stopWord) {
         String[] lines = content.split("\n");
         StringBuffer finalContent = new StringBuffer();
         boolean skip = true;
         for ( int i=0; i<lines.length; i++) {
 
             if ( lines[i].equals(startWord) || lines[i].equals(stopWord) ) {
                 skip = !skip;
             } else {
                 if ( skip == false ) {
                     finalContent.append(lines[i]).append("\n");
                 }
             }
         }
         
         return finalContent.toString().trim();
     }
 }
