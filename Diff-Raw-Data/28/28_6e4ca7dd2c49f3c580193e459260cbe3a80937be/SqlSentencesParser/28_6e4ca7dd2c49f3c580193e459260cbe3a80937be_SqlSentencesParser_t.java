 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.tools;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.apache.commons.lang.Validate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** Parses a text files consisting of sql sentences separated by ';'.
  *
  * This parses is very naive, it considers a line ending in ; as a sentence
  * separator. If the ; is followed by white space, it is not considered the end
  * of the line.
  */
 public class SqlSentencesParser {
 
   /** The class logger.
    */
   private static Logger log =
     LoggerFactory.getLogger(SqlSentencesParser.class);
 
   /** The reader with the sentences to parse.
    *
    * This is never null.
    */
   private BufferedReader sentences;
 
   /** The file name to read the sentences from.
    *
    * This is only used for error messages. It is never null.
    */
   private String fileName;
 
   /** Construct a SqlSentencesParser.
    *
   * @param theFileName The name of the file to parse, it cannot be null.
    */
   public SqlSentencesParser(final String theFileName) {
     Validate.notNull(theFileName, "The sql file name to parse cannot be null.");
     log.trace("Entering SqlSentencesParser('" + theFileName + "')");
     fileName = theFileName;
     try {
       sentences = new BufferedReader(new FileReader(theFileName));
     } catch (final IOException e) {
       throw new RuntimeException("Error opening " + theFileName, e);
     }
     log.trace("Leaving SqlSentencesParser");
   }
 
   /** Obtains the next sentence in the file.
    *
    * @return the next sentence in the file, or null if it reached the end.
    */
   public String readSentence() {
     log.trace("Entering readSentence");
 
     StringBuffer sentence = null;
     try {
       String line = null;
       while (null != (line = sentences.readLine())) {
         // I read a line, initialize the StringBuffer.
         if (sentence == null) {
           // Skip empty lines between sentences.
           if (line.length() == 0) {
             continue;
           }
           sentence = new StringBuffer();
         }
         if (line.endsWith(";")) {
           sentence.append(line.substring(0, line.length() - 1));
           return sentence.toString();
         } else {
           sentence.append(line);
           sentence.append("\n");
         }
       }
     } catch (IOException e) {
       throw new RuntimeException("Error reading from " + fileName, e);
     }
     if (sentence == null) {
       log.trace("Leaving runSqlSentences with null");
       return null;
     } else {
       log.trace("Leaving runSqlSentences");
       return sentence.toString();
     }
   }
 }
 
