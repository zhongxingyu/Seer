 package org.nines.cleaner;
 
 import org.nines.ICustomCleaner;
 
 public class LocEphemeraCleaner implements ICustomCleaner {
 
     public String clean(String archiveName, String content) {
         
         if ( archiveName.equals("locEphemera")) {
             return stripJunk(content, "<hr>", "Information about SGML version of this document.");
         }
         return content;
     }
     
     private String stripJunk(String content, String startWord, String stopWord) {
         String[] lines = content.split("\n");
         StringBuffer finalContent = new StringBuffer();
         boolean skip = true;
        int startCnt = 0;
        boolean startDone = false;
        String line = "";
         for ( int i=0; i<lines.length; i++) {
            line = lines[i].trim().toLowerCase();
            if ( line.contains(startWord) && startDone == false ) {
                startCnt++;
                if (startCnt == 2) {
                    skip = !skip;
                    startDone = true;
                }
            } else if ( line.contains(stopWord.toLowerCase()) ) {
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
