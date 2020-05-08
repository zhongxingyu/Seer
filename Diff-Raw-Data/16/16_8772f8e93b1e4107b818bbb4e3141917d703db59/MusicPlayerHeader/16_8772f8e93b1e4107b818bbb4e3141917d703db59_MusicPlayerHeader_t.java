 package grammar;
 
 import org.antlr.v4.runtime.ANTLRInputStream;
 import org.antlr.v4.runtime.CharStream;
 import org.antlr.v4.runtime.CommonTokenStream;
 import org.antlr.v4.runtime.RuleContext;
 import org.antlr.v4.runtime.TokenStream;
 import org.antlr.v4.runtime.tree.ParseTree;
 import org.antlr.v4.runtime.tree.ParseTreeListener;
 import org.antlr.v4.runtime.tree.ParseTreeWalker;
 
 import sound.Song;
 import grammar.ABCMusicHeaderLexer;
 import grammar.ABCMusicHeaderParser;
 
 public class MusicPlayerHeader {
 	
 	public Song parse(String song) {
     	try{
     		HeaderListener listener = runListener(song);
     		return listener.getSong();
     	}
     	catch(Exception e){
    		throw new RuntimeException("Invalid Header Input!");
     	}
     }
    
     public HeaderListener runListener(String input) {
         // Create a stream of tokens using the lexer.
         CharStream stream = new ANTLRInputStream(input);
         ABCMusicHeaderLexer lexer = new ABCMusicHeaderLexer(stream);
         lexer.reportErrorsAsExceptions();
         TokenStream tokens = new CommonTokenStream(lexer);
         
         // Feed the tokens into the parser.
         ABCMusicHeaderParser parser = new ABCMusicHeaderParser(tokens);
         parser.reportErrorsAsExceptions();
         
         // Generate the parse tree using the starter rule.
         ParseTree tree;
         tree = parser.abc_tune_header(); // "abc_tune_header" is the starter rule.
 
         // debugging option #1: print the tree to the console
 //      System.err.println(tree.toStringTree(parser));
 
       // debugging option #2: show the tree in a window
 //      ((RuleContext)tree).inspect(parser);
 
         // Walk the tree with the listener.
         ParseTreeWalker walker = new ParseTreeWalker();
         ParseTreeListener listener = new HeaderListener();
         walker.walk(listener, tree);
         
         HeaderListener headListener = (HeaderListener) listener;
         return headListener;
     }
     
     public static void main(String[] args) {
 		MusicPlayerHeader header = new MusicPlayerHeader();
 		String s = "X:2167\n"+
 				"T:Waxie's: Dargle\n"+
 				//"C:Josh\n"+
 				"M:2/4\n"+
 //				"L:1/8\n"+
 //				"%\n"+
 //				"Q:1/4=180\n"+
 //				"V:1\n"+
 //				"V:Austin\n"+
 				"K:D\n";
 		Song song = header.runListener(s).getSong();
 		System.out.println(song.toString());
     }
     
 }
