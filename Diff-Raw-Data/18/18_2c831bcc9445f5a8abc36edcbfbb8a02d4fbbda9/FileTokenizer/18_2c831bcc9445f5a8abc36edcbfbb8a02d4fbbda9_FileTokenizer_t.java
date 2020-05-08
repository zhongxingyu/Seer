 package ru.maratyv.indexer.tokenizers;
 
 import ru.maratyv.indexer.Token;
 import ru.maratyv.indexer.index.Index;
 
 import java.io.*;
 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mir
  * Date: 9/30/13
  * Time: 6:07 PM
  * Porsche is the only car
  */
 public class FileTokenizer implements Tokenizer{
 
     private BufferedReader bf;
     private final String docID;
 
     public FileTokenizer(File inputFile) throws IOException {
         docID = inputFile.getName();
         bf = new BufferedReader(new FileReader(inputFile));
     }
 
     public void addTokensTo(Index index) throws IOException {
         String currentLine = bf.readLine();
         int offset = 0;
         try {
             while (currentLine != null) {
                 StringBuilder word = new StringBuilder("");
                 int length = currentLine.length();
                 for (int i = 0; i < length; i++) {
                     char curCharacter = currentLine.charAt(i);
                     if (Character.isLetterOrDigit(curCharacter)) {
                         word.append(curCharacter);
                     } else {
                         if (word.length() != 0) {
                             index.add(new Token(docID,
                                                 word.toString().toLowerCase(),
                                                 offset + i - word.length()));
                             word = new StringBuilder("");
                         }
                     }
                 }
                 if (word.length() != 0) {
                     index.add(new Token(docID,
                             word.toString(),
                            offset + length - word.length()));
                 }
                 offset += length;
                 currentLine = bf.readLine();
             }
         } finally {
             bf.close();
         }
     }
 }
