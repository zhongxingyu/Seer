 package com.iusenko.subrip;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
class SubRipParser {
 
     private static final Pattern phraseNumberPattern = Pattern.compile("^\\d+");
     private static final Pattern timePattern = Pattern.compile("\\d+(:\\d+)+,\\d+\\s+-->\\s+\\d+(:\\d+)+,\\d+");
     private BufferedReader in;
 
     protected SubRipParser() {
         // for testing purpose
     }
 
     public SubRipParser(InputStream in) {
         if (in == null) {
             throw new IllegalArgumentException();
         }
         this.in = new BufferedReader(new InputStreamReader(in));
     }
 
     public List<Phrase> getPhrases() throws Exception {
         List<Phrase> phrases = new ArrayList<Phrase>();
         String line;
         StringBuilder buffer = new StringBuilder();
         Phrase phrase = new Phrase();
 
         while ((line = in.readLine()) != null) {
             line = line.trim();
             
             if (isPhraseNumber(line)) {
                 phrase = new Phrase();
                 phrase.setNumber(Integer.parseInt(line));
                 buffer = new StringBuilder();
                 continue;
             }
 
             if (isBlank(line)) {
                 phrase.setText(buffer.toString().trim());
                 boolean fromBlank = isBlank(phrase.getFromTime());
                 boolean toBlank = isBlank(phrase.getToTime());
                 boolean textBlank = isBlank(phrase.getText());
                 if (!fromBlank && !toBlank && !textBlank) {
                     phrases.add(phrase);
                 }
                 continue;
             }
 
             if (isTime(line)) {
                 phrase.setFromTime(getStartTime(line));
                 phrase.setToTime(getEndTime(line));
                 continue;
             }
 
             if (isSentence(line)) {
                 buffer.append(" ").append(line).append("\n");
             }
         }
         return phrases;
     }
 
     protected final boolean isSentence(String line) {
         if (isBlank(line) || isTime(line) || isPhraseNumber(line)) {
             return false;
         }
         return true;
     }
 
     protected final boolean isTime(String line) {
         return timePattern.matcher(line).matches();
     }
 
     protected final boolean isPhraseNumber(String line) {
         return phraseNumberPattern.matcher(line).matches();
     }
 
     protected final String getStartTime(String rawString) {
         return getTime(rawString, true);
     }
 
     protected final String getEndTime(String rawString) {
         return getTime(rawString, false);
     }
 
     protected final String getTime(String rawString, boolean getStartTime) {
         int index = rawString.indexOf("-->");
         if (index == -1) {
             return "";
         }
 
         rawString = rawString.replaceAll(",", ".");
         if (getStartTime) {
             return rawString.substring(0, index).trim();
         } else {
             return rawString.substring(index + 3).trim();
         }
     }
 
     private boolean isBlank(String string) {
         return string == null || "".equals(string.trim());
     }
 }
