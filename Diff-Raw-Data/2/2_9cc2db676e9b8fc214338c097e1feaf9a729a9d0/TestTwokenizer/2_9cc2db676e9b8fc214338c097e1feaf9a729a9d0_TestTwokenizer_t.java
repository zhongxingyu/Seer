 package graphbuilder.twitter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.zip.GZIPInputStream;
 
 public class TestTwokenizer {
 	public static void main (String[] args) throws FileNotFoundException, IOException {
 		File inputpath = new File(args[0]);
 		int parsedcount = 0;
 		for (File file : inputpath.listFiles()) {
 			System.out.println("Parsing " + file.getName());
 			InputStream in = new GZIPInputStream(new FileInputStream(file));
 			Reader decoder = new InputStreamReader(in);
 			BufferedReader buffered = new BufferedReader(decoder);
 			String line = null;
 			while((line = buffered.readLine()) != null) {
 				try {
 					TweetsJSParser parser = new TweetsJSParser(line);
 					System.out.println (parser.getText());
 					parser.tokenize();
 					parsedcount++;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				if (parsedcount % 10000 == 0)
 					System.out.println("Parsed " + parsedcount + "Tweets");
 			}
 		}
 		/*
		String test = "...............................................";
 		test = test.toLowerCase().replaceAll("\\.{3,}", "\\.");
 		System.out.println(test);
 		Twokenize.tokenize(test);
 		System.out.println("done");
 		*/
 	}
 }
