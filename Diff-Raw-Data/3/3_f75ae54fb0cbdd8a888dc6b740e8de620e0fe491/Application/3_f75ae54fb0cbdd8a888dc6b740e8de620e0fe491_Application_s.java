 package haw.ci;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 
 import haw.ci.lib.ITokenStream;
 import haw.ci.lib.Parser;
 import haw.ci.lib.ParserAcceptError;
 import haw.ci.lib.Tokenizer;
 
 public class Application {
 	public static void main(String[] args) throws IOException {
 		Reader fileReader = new FileReader(new File("./example4.ob"));
 		ITokenStream tokenStream = new Tokenizer(fileReader);
 		Parser parser = new Parser(tokenStream);
 		try {
 			System.out.println(parser.build());
 		} catch (ParserAcceptError e) {
 			System.out.println("Mature Failure");
			System.out.println(e);
			System.out.println(e.getStackTrace());
 		}
 	}
 }
