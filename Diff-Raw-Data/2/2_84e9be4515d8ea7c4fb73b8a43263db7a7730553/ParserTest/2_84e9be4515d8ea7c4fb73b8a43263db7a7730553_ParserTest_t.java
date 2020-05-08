 import java.io.IOException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class ParserTest {
 
 	@Test
 	public void testParseFile() throws IOException {
 		Parser parser = new Parser("TestCase.txt");
 		
 		parser.parseFile();
 		
 		Assert.assertEquals(parser.getPublications().get("Sexual Selection, Resource Distribution, and Population Size in Synthetic Sympatric Speciation").getTitlePaper(), "Sexual Selection, Resource Distribution, and Population Size in Synthetic Sympatric Speciation");
 	}
 	
 	@Test
 	public void testParseFileString() throws IOException {
 		Parser parser = new Parser("TestCase.txt");
 		
 		parser.parseFile();
 		
 		Assert.assertEquals(parser.getPublications().get("Extending Adaptive Fuzzy Behavior Hierarchies to Multiple Levels of Composite Behaviors").toString(), "\nJournal Article\n\tAuthors = Eskridge, Brent E.; Hougen, Dean F.; \n\tPaper Title = Extending Adaptive Fuzzy Behavior Hierarchies to Multiple Levels of Composite Behaviors\n\tSerial Title = Robotics and Autonomous Systems\n\tStarting Page = 1076\n\tEnding Page = 1084\n\tTime of Publication = September2010\n\tHyperlink = http://dx.doi.org/10.7551/978-0-262-31050-5-ch020\n\tVolume = 58\n\tIssue = 9");
 	}
 	
 	@Test
 	public void testParseFileAuthor() throws IOException {
 		Parser parser = new Parser("TestCase.txt");
 		
 		parser.parseFile();
 		
		Assert.assertEquals(parser.getAuthors().get("Dean Hougen") != null, true);
 	}
 
 }
