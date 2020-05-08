 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import dom.Text;
 import dom.Word;
 import dom.WordPart;
 import fst.Configuration;
 import fst.FstPrinter;
 import fst.ResultCollector;
 import fst.State;
 import fst.StringLink;
 import fst.Tape;
 
 public class Main {
 
 	public static void main(String[] args) {
 		Text trainingText = new Text("Data/Train.txt");
 		Text testText = new Text("Data/Test.txt");
 
 		try {
 			trainingText.readText();
 			testText.readText();
 		} catch (FileNotFoundException e) {
 			System.err.println("File not found!");
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println("IO Exception!");
 			System.exit(1);
 		}
 
 		trainingText.generateWords();
 		trainingText.searchForPrefixes();
 		trainingText.searchForSuffixes();
 		trainingText.generateStatistics();
 
 		// create the states
 		State<ResultCollector> startState = new State<ResultCollector>(), preStemState = new State<ResultCollector>(), postStemState = new State<ResultCollector>(), finalState = new State<ResultCollector>(), pastWordEndState = new State<ResultCollector>();
 
 		// for testing the fst, we'll add # to the end of every word.
 		// this makes sure the whole word is parsed before reaching the
 		// accepting state
 		pastWordEndState.setAccepting(true);
 
 		// add empty prefixes and suffixes
 		StringLink link = new StringLink("", "", preStemState);
 		link.setWeight(0);
 		startState.addLink(link);
 		link = new StringLink("", "", finalState);
 		link.setWeight(0);
 		postStemState.addLink(link);
 
 		// add word end link
 		finalState.addLink(new StringLink("#", "", pastWordEndState));
 
 		// add links for the prefixes
 		for (WordPart part : trainingText.prefixes.values()) {
 			if (part.name == "")
 				continue;
 
 			link = new StringLink(part.name, part.name + "^", preStemState);
 			link.setWeight(part.frequency);
 			startState.addLink(link);
 		}
 
 		// add links for the stems
 		for (WordPart part : trainingText.stems.values()) {
 			link = new StringLink(part.name, part.name, postStemState);
 			link.setWeight(part.frequency);
 			preStemState.addLink(link);
 		}
 
 		// add links for the suffixes
 		for (WordPart part : trainingText.suffixes.values()) {
 			if (part.name == "")
 				continue;
 
			StringLink link = new StringLink(part.name, "^" + part.name,
					finalState);
 			link.setWeight(part.frequency);
 			postStemState.addLink(link);
 		}
 
 		try {
 			FstPrinter.print(startState, new PrintStream("graph.dot"));
 			// Runtime.getRuntime().exec(new
 			// String[]{"dot","-o","graph.gif","-T","gif","graph.dot"});
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		testText.generateWords();
 		for (Word word : testText.words) {
 			// create and fill the input tape
 			Tape inputTape = new Tape();
 			for (char ch : word.getName().toCharArray()) {
 				inputTape.write(ch);
 			}
 			// add the final # to mark the end of the word
 			inputTape.write('#');
 			inputTape.setPosition(0);
 
 			// create the output tape
 			Tape outputTape = new Tape();
 
 			// create the result collector
 			ResultCollector collector = new ResultCollector();
 
 			// run the configuration
 			Configuration<ResultCollector> config = new Configuration<ResultCollector>(
 					startState, inputTape, outputTape, collector);
 			config.run();
 
 			collector.sortAcceptionConfigurations();
 			System.out.printf("%s\n", word.getName());
 			for (Configuration<ResultCollector> conf : collector
 					.getAcceptingConfigurations()) {
 				System.out.printf("  %s (%.2f)\n", conf.getOutputTape(), conf
 						.getProbability());
 			}
 		}
 	}
 }
