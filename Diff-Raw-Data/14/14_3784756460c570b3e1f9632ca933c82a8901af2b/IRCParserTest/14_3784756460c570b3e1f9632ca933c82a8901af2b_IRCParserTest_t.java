 package autocomplete.io.parse.irc;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 
 import org.junit.Test;
 
 import autocomplete.core.completer.BasicCompleter;
 import autocomplete.core.completer.Completer;
 import autocomplete.core.completer.FilterCompleter;
 import autocomplete.core.completer.IRCCompleter;
 import autocomplete.core.completer.LearningCompleter;
 
 public class IRCParserTest {
 	
 	/**
 	//Original test settings.
 	String [] trainPaths = {"resources/##chemistry.20100116.log.irc",
 							"resources/#wikipedia.20090731.log.irc",
 							"resources/#wikipedia-en.20100103.log.irc"};
 	
 	String testPath = "resources/#wikipedia-en.20100117.log.irc";
 	int numberOfSuggestions = 3;
 	
 	String resSuffix = ".txt";
 	**/
 	
 	// second test settings with more train.
 	String [] trainPaths = {"resources/##chemistry.20100116.log.irc",
 			"resources/#wikipedia.20090731.log.irc",
 			"resources/#wikipedia-en.20100103.log.irc",
			"resources/#GA.log.irc",
			"resources/#ga.20100615.log.irc",
			"resources/#GA.20090602.log.irc"
 		};
 
	//String testPath = "resources/#GA.20080715.log.irc";
	String testPath = "resources/#wikipedia-en.20100117.log.irc";
 	int numberOfSuggestions = 3;
 	
	//String resSuffix = "-test-GA.2008715.txt";
	String resSuffix = " .txt";
 	
 	public void runIRCParser(String [] trainPaths, 
 						String testPath, 
 						String resPath,
 						Completer completer,
 						int numberOfSuggestions) throws IOException{
 		
 		IRCParser parser = new IRCParser(completer, numberOfSuggestions);
 		
 		for (String path : trainPaths) {
 			System.out.println("Training parser on: " + path);
 			Reader trainReader = new FileReader(path);
 			parser.train(trainReader);
 			System.out.println("Finish training parser on: " + path);
 		}
 		
 		Reader testReader = new FileReader(testPath);
 		OutputStreamWriter writer = new FileWriter(resPath + resSuffix);
 		parser.complete(testReader, writer);
 	}
 	
 	@Test
 	public void testIRCParser() throws IOException {
 		String resPath = "results/res-irc";
 		Completer completer = new IRCCompleter(1, 4); 
 		runIRCParser(trainPaths, testPath, resPath, completer, numberOfSuggestions);
 	}
 	
 	@Test
 	public void testIRCParserLearning() throws IOException {
 		String resPath = "results/res-irc-learning-1gram";
 		
 		Completer completer = new LearningCompleter(1); 
 		runIRCParser(trainPaths, testPath, resPath, completer, numberOfSuggestions);
 	}
 
 	@Test
 	public void testIRCParserBaseline() throws IOException {
 		String resPath = "results/res-irc-baseline-1gram";
 		
 		Completer completer = new BasicCompleter(1); 
 		runIRCParser(trainPaths, testPath, resPath, completer, numberOfSuggestions);
 	}
 	
 	//@Test
 	public void testIRCParserIRCCompleterWithFilter() throws IOException {
 		String resPath = "results/res-irc-with-filter-1gram.txt";
 		
 		Completer completer = new FilterCompleter(new IRCCompleter(1, 4)); 
 		runIRCParser(trainPaths, testPath, resPath, completer, numberOfSuggestions);
 	}
 
 	@Test
 	public void testIRCParserIRCCompleterWithFilterDifferentNgrams() throws IOException {
 		for (int i = 2; i < 6; i ++ ) {
 			String resPath = "results/res-irc-with-filter-" + i + "gram-sfactor4";
 
 			Completer completer = new FilterCompleter(new IRCCompleter(i, 4));
 			runIRCParser(trainPaths, testPath, resPath, completer, numberOfSuggestions);
 		}
 	}
 
 }
