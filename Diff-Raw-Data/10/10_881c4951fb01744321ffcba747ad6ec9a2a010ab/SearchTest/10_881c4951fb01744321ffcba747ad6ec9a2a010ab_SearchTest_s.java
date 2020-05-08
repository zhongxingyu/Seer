 /* Tester for TextBuddy.search()
  * 
  * Tests the functionality of search
  */
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class SearchTest {
 
     @Test
     public void testSearch() {
 	String fileName = "testFile.txt";
 	File file;
 	file = new File(fileName);
 
 	try {
 	    if (!file.exists())
 		file.createNewFile();
 	} catch (IOException e) {
 	    System.out.println("Error opening/creating file");
 	    e.printStackTrace();
 	    System.exit(1);
 	}
 
 	testOneCommand("clears file before test",
 		("All contents deleted from " + fileName), 
 		"clear", file);
 	testOneCommand("simple add",
 		("Added to " + fileName + ": \"The little brown fox\""),
 		"add The little brown fox", file);
 	testOneCommand("more adds",
 		("Added to " + fileName + ": \"jumped over the moon\""),
 		"add jumped over the moon", file);
 	testOneCommand("more adds",
 		("Added to " + fileName + ": \"and fell\""),
 		"add and fell", file);
 	testOneCommand("search brown",
 		("1. The little brown fox\n"),
 		"search brown", file);
 	testOneCommand("search the",
 		("1. The little brown fox\n2. jumped over the moon\n"),
 		"search the", file);
     }
 
     private void testOneCommand(String description, String expected,
 	    String command, File file) {
 	assertEquals(description, expected,
 		TextBuddy.processCommand(command, file));
     }
 }
