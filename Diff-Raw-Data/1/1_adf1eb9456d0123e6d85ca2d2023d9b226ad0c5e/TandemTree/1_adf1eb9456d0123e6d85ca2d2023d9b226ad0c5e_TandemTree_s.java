 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import java.io.*;
 import org.antlr.runtime.*;
 
 
 public class TandemTree{
 	public static void main(String args[]){
 		try{
 
 		TanGLexer lex = new TanGLexer(new ANTLRInputStream(new FileInputStream(args[0])));
 		TokenStream ts = new CommonTokenStream(lex);
 		TanGParser parse = new TanGParser(ts);
 		parse.tanG();
     	} catch(Exception e) {
     		System.err.println("exception: "+e);
     	}
     }	
 }
