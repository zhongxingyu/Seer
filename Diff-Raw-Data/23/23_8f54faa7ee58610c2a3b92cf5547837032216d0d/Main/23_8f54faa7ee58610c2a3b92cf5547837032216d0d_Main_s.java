 package autocomplete.core;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.util.Arrays;
 import java.util.List;
 
 import autocomplete.core.completer.Completer;
 import autocomplete.core.completer.FilterCompleter;
 import autocomplete.core.completer.IRCCompleter;
 import autocomplete.io.parse.irc.IRCParser;
 
 public class Main {
 
 	/**
 	 * Run the irc completer.
 	 * 
 	 * Usage: Main <suggestions> <ngram> <testfile> <resfile> <train> [<train>, ...]
 	 * 		
 	 * 	suggestions - the number of suggestions to suggest (int)
 	 *	ngram - the ngram number to use during training (int)
 	 *  testfile - the path to the irc file to test.
 	 *  resfile - the path to the result file to write.
 	 *  train - list of irc files to train from.
 	 * 
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 		
 		if (args.length < 5) {
			System.err.println("Usage: Main <suggestions> <ngram> <testfile>" +
							   "<resfile> <train> [<train>, ...]");
 		}
 		
 		int suggestions = Integer.parseInt(args[0]);
 		int ngram = Integer.parseInt(args[1]);
 		String testfile = args[2];
 		String resfile = args[3];
 		List<String> train = Arrays.asList(args).subList(4, args.length);
 		
		System.out.println(suggestions);
		System.out.println(train);
		
 		Completer completer = new FilterCompleter(new IRCCompleter(ngram, 4));
 		
 		runIRCParser(train, testfile, resfile, completer, suggestions);
 	}
 	
 	/**
 	 * 
 	 * @param trainPaths the paths to irc files to train from.
 	 * @param testPath the path where the test file located
 	 * @param resPath the path to where the results file will be created.
 	 * @param completer the completer to test.
 	 * @param numberOfSuggestions the number of suggestions to get from the completer.
 	 * @throws IOException oh no.
 	 */
 	public static void runIRCParser(List<String> trainPaths, String testPath,
 			String resPath, Completer completer, int numberOfSuggestions)
 			throws IOException {
 
 		IRCParser parser = new IRCParser(completer, numberOfSuggestions);
 
 		for (String path : trainPaths) {
 			Reader trainReader = new FileReader(path);
 			parser.train(trainReader);
 		}

 		Reader testReader = new FileReader(testPath);
 		OutputStreamWriter writer = new FileWriter(resPath);
 		parser.complete(testReader, writer);
 	}
 
 }
