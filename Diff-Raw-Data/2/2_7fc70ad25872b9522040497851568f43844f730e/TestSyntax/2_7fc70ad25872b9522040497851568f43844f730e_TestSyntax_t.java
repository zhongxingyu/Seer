 package apeg.compiler.syntax;
 
 import java.io.IOException;
 
 import org.antlr.runtime.RecognitionException;
 
 import apeg.common.path.AbsolutePath;
 import apeg.common.path.Path;
 
 public class TestSyntax {
 
 	public static void main(String[] args) throws IOException, RecognitionException {
 		String grammarFileName="./../test/syntax/teste02.apeg";
 		
 		Path path = new AbsolutePath(grammarFileName);
 		Parser.parse(path);
 		
 		System.out.println("------------- END -------------------");
 	}
 
 }
