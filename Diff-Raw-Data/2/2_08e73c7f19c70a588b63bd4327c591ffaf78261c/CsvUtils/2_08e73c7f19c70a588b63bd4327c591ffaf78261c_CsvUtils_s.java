 package com.telenor.fun.reader.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.*;
 
 /**
  * Simple tools to read a delimiter-separated file.
  * Default delimiter is comma.
  *
  * @author <a href="vegard.aasen@telenor.com">Vegard Aasen</a>
  */
 public final class CsvUtils {
 
     public static final char DEFAULT_DELIMITER = ',';
     public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
 
     private static final String EMPTY = "";
     private static final char HYPHEN = '"';
    private static final char PLACEHOLDER = '§';
     private static final String PLACEHOLDER_STR = String.valueOf(PLACEHOLDER);
 
     private static String delimiter = String.valueOf(DEFAULT_DELIMITER);
     private static char delimiter_char = DEFAULT_DELIMITER;
     private static Charset charset = DEFAULT_CHARSET;
     private static boolean omitFirstLine = false;
 
     private CsvUtils() {
     }
 
     public static Map<Integer, Map<String, String>> getElementsFromCsv(final String location) throws FileNotFoundException {
         return getElementsFromCsv(location, false);
     }
 
     /**
      * @param location  The location
      * @param mapTitles true == map attributes to the titles (if any) found on the top level of the csv.
      * @return a map containing the line, and its mapping wihting that line. Simple.
      * @throws FileNotFoundException _
      */
     public static Map<Integer, Map<String, String>> getElementsFromCsv(final String location, final boolean mapTitles) throws FileNotFoundException {
         if (!"".equals(location)) {
             try {
                 return getElementsFromCsv(FileUtils.getFile(location), mapTitles);
             } catch (IOException e) {
                 throw new FileNotFoundException("File not found.");
             }
         }
         throw new IllegalArgumentException("Missing argument.");
     }
 
     public static Map<Integer, Map<String, String>> getElementsFromCsv(final File file) {
         return getElementsFromCsv(file, false);
     }
 
     /**
      * @param file      The file
      * @param mapTitles true == map attributes to the titles (if any) found on the top level of the csv.
      * @return a map containing the line, and its mapping wihting that line. Simple.
      */
     public static Map<Integer, Map<String, String>> getElementsFromCsv(final File file, final boolean mapTitles) {
         if (file != null) {
             try {
                 return readCsv(FileUtils.getBufferedReader(file), mapTitles);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         throw new IllegalArgumentException("Missing argument.");
     }
 
     private static Map<Integer, Map<String, String>> readCsv(BufferedReader input, final boolean mapTitles) throws IOException {
         if (input != null && input.ready()) {
             Map<Integer, Map<String, String>> entries = new TreeMap<>();
             List<String> titles = new ArrayList<>();
             String currentLine;
             boolean completedFirstLine = false, omitted = false;
             int counter = 0, position = 0;
             while ((currentLine = input.readLine()) != null) {
                 if (!"".equals(currentLine)) {
                     currentLine = massageLine(currentLine);
                     if (mapTitles) {
                         if (!completedFirstLine) {
                             if (currentLine.contains(delimiter)) {
                                 Collections.addAll(titles, currentLine.split(delimiter));
                             } else {
                                 throw new RuntimeException(
                                         String.format("Unable to find {%s} as delimiter. Wrong delimiter?", delimiter)
                                 );
                             }
                             completedFirstLine = true;
                         }
                     }
                     final Map<String, String> entryLine = new TreeMap<>();
                     if (omitFirstLine) {
                         omitFirstLine = false;
                         continue;
                     }
                     for (String entry : currentLine.split(delimiter)) {
                         if (entry.contains(PLACEHOLDER_STR)) {
                             entry = entry.replaceAll(PLACEHOLDER_STR, delimiter);
                         }
                         entryLine.put(
                                 ((mapTitles) ? titles.get(position) : String.valueOf(position)),
                                 ((charset != DEFAULT_CHARSET) ? new String(entry.getBytes(), charset) : entry)
                         );
                         position++;
                     }
                     entries.put(counter, entryLine);
                     position = 0;
                     counter++;
 
                 }
             }
             return entries;
         }
         return Collections.emptyMap();
     }
 
     private static String massageLine(final String line) {
         if (!"".equals(line)) {
             if (line.contains("\"")) {
                 ArrayList<Character> modifiedLine = new ArrayList<>();
                 boolean withinHyphenSequence = false;
                 for (int i = 0; i < line.length(); i++) {
                     if (line.charAt(i) == HYPHEN) {
                         withinHyphenSequence = ((!withinHyphenSequence));
                     }
                     if (withinHyphenSequence) {
                         if (line.charAt(i) == delimiter_char) {
                             modifiedLine.add(PLACEHOLDER);
                         } else {
                             modifiedLine.add(line.charAt(i));
                         }
                     } else {
                         modifiedLine.add(line.charAt(i));
                     }
                 }
                 return getStringFromList(modifiedLine);
             }
             return line;
         }
         return EMPTY;
     }
 
     private static String getStringFromList(ArrayList<Character> list) {
         StringBuilder sb = new StringBuilder();
         for (char s : list) {
             sb.append(s);
         }
         return sb.toString();
     }
 
     public static void setDelimiter(final String del) {
         if (!"".equals(del)) {
             delimiter = del;
             delimiter_char = del.charAt(0);
         }
     }
 
     public static void setCharset(final Charset chSet) {
         charset = chSet;
     }
 
     public static void setOmitFirstLine(final boolean choice) {
         omitFirstLine = choice;
     }
 
 }
