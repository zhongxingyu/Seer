 package com.example.classproject;
 
 import org.junit.Test;
 
 import java.io.*;
 
 import static org.junit.Assert.*;
 
 public class HelloWorldTest {
 
     @Test
     public void testThrowsFileNotFound() {
         try {
             File textFile = new File("a file that does not exist");
 
             HelloWorld.readFile(textFile);
 
             fail("It didn't throw!");
         } catch (FileNotFoundException e) {
 
         } catch (Exception e) {
             fail("Unexpected: " + e.getMessage());
         }
     }
 
     @Test
     public void testReadsATextFile() {
         File textFile = new File("story/dummy.txt");
         try {
             String output = HelloWorld.readFile(textFile);
             assertEquals("this is some example text", output);
         } catch (Exception e) {
             fail("exception!: " + e.getMessage());
         }
 
     }
 
     @Test
     public void testCanReadInputFromStream() {
         ByteArrayInputStream input = getInputStream("something");
 
         try {
             String result = HelloWorld.readInput(input);
             assertEquals("something", result);
         } catch (Exception e) {
             fail("exception" + e.getMessage());
         }
     }
 
     @Test
     public void testPrintsCorrectStory() {
         ByteArrayInputStream input = getInputStream("1");
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         System.setOut(new PrintStream(output));
 
         try {
            String text = readFile("story/1.txt");
             HelloWorld.runProgram("story/story.txt", input);
//            assertEquals("", output.toString());
             String outputString = output.toString();
            assertTrue(outputString.contains(text));
         } catch (IOException e) {
             fail("exception: " + e.getMessage());
         }
     }
 
     private String readFile(String filename) throws IOException {
         BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
         String line;
         StringBuilder builder = new StringBuilder();
         while ((line = reader.readLine()) != null) {
             builder.append(line + "\n");
         }
 
        return line;
     }
 
     @Test
     public void testValidateReturnsTrueIfOk() {
         String goodInput = "1";
         assertTrue(HelloWorld.validateInput(goodInput));
     }
 
     @Test
     public void testValidateDoesNotThrowForNonNumeric() {
         String badInput = "hello";
         assertFalse(HelloWorld.validateInput(badInput));
     }
 
     @Test
     public void testValidateReturnsFalseIfOutOfStoryRange() {
         String badInput = "5";
         assertFalse(HelloWorld.validateInput(badInput));
     }
 
     @Test
     public void runProgramPrintsAnErrorIfUserInputDoesNotValidate() {
         ByteArrayInputStream input = getInputStream("b");
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         System.setOut(new PrintStream(output));
 
         try {
             HelloWorld.runProgram("story/story.txt", input);
             assertTrue(output.toString().contains("Invalid selection: 'b'"));
         } catch (IOException e) {
             fail(e.getMessage());
         }
     }
 
     private ByteArrayInputStream getInputStream(String inputStream) {
         return new ByteArrayInputStream(inputStream.getBytes());
     }
 }
