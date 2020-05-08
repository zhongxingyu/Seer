 package ru.maratyv.indexer.tokenizers.test;
 
 import org.junit.*;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import ru.maratyv.indexer.index.Index;
 import ru.maratyv.indexer.tokenizers.FileTokenizer;
 import ru.maratyv.indexer.tokenizers.Tokenizer;
 
 import java.io.*;
 import java.util.Scanner;
 
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mir
  * Date: 9/30/13
  * Time: 6:11 PM
  * Porsche is the only car
  */
 @RunWith(JUnit4.class)
 public class FileTokenizerTest {
     public static final File LOREM1_FILE = new File("test Docs/test1.txt");
     public static final File LOREM2_FILE = new File("test Docs/test2.txt");
     public static final File LOREM4_FILE = new File("test Docs/test4.txt");
     public static final String TEMP_OUTPUT_FILE_NAME = "temp.txt";
     public static final String LOREM = "lorem";
 
     @AfterClass
     public static void afterTests(){
         (new File(TEMP_OUTPUT_FILE_NAME)).delete();
     }
 
     private int wordCountIn(File file, String word) throws IOException {
         BufferedReader bf = new BufferedReader(new FileReader(file));
         int expectedCount = 0;
         try {
             String curLine = bf.readLine();
             while (curLine != null) {
                 Scanner sc = new Scanner(curLine);
                 while (sc.hasNext()) {
                     if (sc.next().toLowerCase().contains(word)) {
                         expectedCount++;
                     }
                 }
                 curLine = bf.readLine();
             }
         } finally {
             bf.close();
         }
         return expectedCount;
     }
 
     @Test
     public void lorem1Test() throws IOException {
         Tokenizer tokenizer = new FileTokenizer(LOREM1_FILE);
         Index index = new WordCounterIndex(LOREM);
         tokenizer.addTokensTo(index);
         int expectedCount = wordCountIn(LOREM1_FILE,LOREM);
         assertEquals(expectedCount + "", index.toString());
     }
 
     @Test
     public void lorem2Test() throws IOException {
         Tokenizer tokenizer = new FileTokenizer(LOREM2_FILE);
         Index index = new WordCounterIndex(LOREM);
         tokenizer.addTokensTo(index);
         int expectedCount = wordCountIn(LOREM2_FILE,LOREM);
         assertEquals(expectedCount + "", index.toString());
     }
 
     @Test
     public void lorem4Test() throws IOException {
         Tokenizer tokenizer = new FileTokenizer(LOREM4_FILE);
         Index index = new WordCounterIndex(LOREM);
         tokenizer.addTokensTo(index);
         int expectedCount = wordCountIn(LOREM4_FILE,LOREM);
         assertEquals(expectedCount + "", index.toString());
     }
 
     @Test
     public void lowCaseTest() throws IOException {
         // create test file
         String testString = "0 2 4";
         File temp = new File(TEMP_OUTPUT_FILE_NAME);
         Writer writer = (new FileWriter(temp)).append(testString);
         writer.close();
         //tokenize
         Tokenizer tokenizer = new FileTokenizer(temp);
         StringIndex stringIndex = new StringIndex();
         tokenizer.addTokensTo(stringIndex);
         //test
        assertEquals("0:<"+ TEMP_OUTPUT_FILE_NAME + "" +
                ",0> 2:<" +TEMP_OUTPUT_FILE_NAME + "" +
                ",2> 4:<" + TEMP_OUTPUT_FILE_NAME + ",4>",
                 stringIndex.toString());
     }
 }
