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
 
 public class MusicPlayerBody {
 	private Song song;
 	
 	/**
 	 * Constructor for BodyListener object - requires that a pre-initialized song created by
 	 * Header Listener be passed in as parameter
 	 * @param s: Song created by HeaderListener
 	 */
 	public MusicPlayerBody(Song s) {
 		super();
 		this.song = s;
 	}
 	
 	public Song parse(String s) {
     	try{
     		BodyListener listener = runListener(s);
     		return listener.getSong();
     	}
     	catch(Exception e){
    		throw new RuntimeException("Invalid input!");
     	}
     }
 	
     public BodyListener runListener(String input) {
         // Create a stream of tokens using the lexer.
         CharStream stream = new ANTLRInputStream(input);
         ABCMusicBodyLexer lexer = new ABCMusicBodyLexer(stream);
         lexer.reportErrorsAsExceptions();
         TokenStream tokens = new CommonTokenStream(lexer);
         
         // Feed the tokens into the parser.
         ABCMusicBodyParser parser = new ABCMusicBodyParser(tokens);
         parser.reportErrorsAsExceptions();
         
         // Generate the parse tree using the starter rule.
         ParseTree tree;
         tree = parser.abc_tune_body(); // "abc_tune_header" is the starter rule.
 
         // debugging option #1: print the tree to the console
 //      System.err.println(tree.toStringTree(parser));
 
       // debugging option #2: show the tree in a window
 //      ((RuleContext)tree).inspect(parser);
 
         // Walk the tree with the listener.
         ParseTreeWalker walker = new ParseTreeWalker();
         ParseTreeListener listener = new BodyListener(this.song);
         walker.walk(listener, tree);
       
         BodyListener bodylistener = (BodyListener) listener;
         return bodylistener;
     }
     
     public static void main(String[] args) {
 //		MusicPlayerBody body = new MusicPlayerBody();
 //		String s = "gf|e2dc B2A2|B2G2 E2D2|G2G2 GABc|d4 B2gf|\n"+
 //				"w: Sa-ys my aul' wan to your aul' wan\n"+
 //				"e2dc B2A2|B2G2 E2G2|F2A2 D2EF|G2z2 G4|\n"+
 //				"w: Sa-ys your aul'_ wan to my aul' wan,\n"+
 //				"B2d2 e2f2|g2d2 BAG2|Bcd2 e2f2|g4 f2gf|\n"+
 //				"w: I'll go down to Mon-to to-w-n\n"+
 //				"e2dc B2A2|B2G2 E2G2|F2A2 D2EF|G2z2 G4|\n"+
 //				"w: A-nd ask him for a half a crown\n";
 		
 //		String s = "V:1\n"+
 //				"z2 Gc eGce (3abc z2 Gc eGce| z2 Ad fAdf z2 Ad fAdf| z2 Gd fGdf z2 Gd fGdf|\n"+
 //				"V:2\n"+
 //				"z E7 z E7|z D7 z D7|z D7 z D7|\n"+
 //				"V:3\n"+
 //				"C8 C8|C8 C8|B,8 B,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 Gc eGce z2 Gc eGce| z2 Ae aAea z2 Ae aAea| z2 ^FA dFAd z2 FA dFAd|\n"+
 //				"V:2\n"+
 //				"z E7 z E7|z E7 z E7|z D7 z D7|\n"+
 //				"V:3\n"+
 //				"C8 C8|C8 C8|C8 C8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 Gd gGdg z2 Gd gGdg| z2 EG cEGc z2 EG cEGc| z2 EG cEGc z2 EG cEGc|\n"+
 //				"V:2\n"+
 //				"z D7 z D7|z C7 z C7|z C7 z C7|\n"+
 //				"V:3\n"+
 //				"B,8 B,8|B,8 B,8|A,8 A,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 D^F cDFc z2 DF cDFc| z2 DG BDGB z2 DG BDGB| z2 EG ^cEGc z2 EG cEGc|\n"+
 //				"V:2\n"+
 //				"z A,7 z A,7|z B,7 z B,7|z _B,7 z B,7|\n"+
 //				"V:3\n"+
 //				"D,8 D,8|G,8 G,8|G,8 G,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 DA dDAd z2 DA dDAd| z2 DF BDFB z2 DF BDFB| z2 CG cCGc z2 CG cCGc|\n"+
 //				"V:2\n"+
 //				"z A,7 z A,7|z _A,7 z A,7|z G,7 z G,7|\n"+
 //				"V:3\n"+
 //				"F,8 F,8|F,8 F,8|E,8 E,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 A,C FA,CF z2 A,C FA,CF| z2 A,C FA,CF z2 A,C FA,CF| z2 G,B, FG,B,F z2 G,B, FG,B,F|\n"+
 //				"V:2\n"+
 //				"z F,7 z F,7|z F,7 z F,7|z D,7 z D,7|\n"+
 //				"V:3\n"+
 //				"E,8 E,8|D,8 D,8|G,,8 G,,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 G,C EG,CE z2 G,C EG,CE| z2 _B,C EB,CE z2 B,C EB,CE| z2 A,C EA,CE z2 A,C EA,CE|\n"+
 //				"V:2\n"+
 //				"z E,7 z E,7|z G,7 z G,7|z F,7 z F,7|\n"+
 //				"V:3\n"+
 //				"C,8 C,8|C,8 C,8|F,,8 F,,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 A,C _EA,CE z2 A,C EA,CE| z2 B,C DB,CD z2 B,C DB,CD| z2 G,B, DG,B,D z2 G,B, DG,B,D|\n"+
 //				"V:2\n"+
 //				"z C,7 z C,7|z F,7 z F,7|z F,7 z F,7|\n"+
 //				"V:3\n"+
 //				"^F,,8 F,,8|_A,,8 A,,8|G,,8 G,,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 G,C EG,CE z2 G,C EG,CE| z2 G,C FG,CF z2 G,C FG,CF| z2 G,B, FG,B,F z2 G,B, FG,B,F|\n"+
 //				"V:2\n"+
 //				"z E,7 z E,7|z D,7 z D,7|z D,7 z D,7|\n"+
 //				"V:3\n"+
 //				"G,,8 G,,8|G,,8 G,,8|G,,8 G,,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 A,C ^FA,CF z2 A,C FA,CF| z2 G,C GG,CG z2 G,C GG,CG| z2 G,C FG,CF z2 G,C FG,CF|\n"+
 //				"V:2\n"+
 //				"z _E,7 z E,7|z E,7 z E,7|z D,7 z D,7|\n"+
 //				"V:3\n"+
 //				"G,,8 G,,8|G,,8 G,,8|G,,8 G,,8|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				" z2 G,B, FG,B,F z2 G,B, FG,B,F| z2 G,_B, EG,B,E z2 G,B, EG,B,E|z2 F,A, CFCA, CA,F,A, F,D,F,D,|\n"+
 //				"V:2\n"+
 //				"z D,7 z D,7|z C,7 z C,7|z C,15|\n"+
 //				"V:3\n"+
 //				"G,,8 G,,8|C,,8 C,,8|C,,16|\n"+
 //				"%\n"+
 //				"V:1\n"+
 //				"z2 GB dfdB dBGB DFED|[E16G16c16]|]\n"+
 //				"V:2\n"+
 //				"z B,,15|C,16|]\n"+
 //				"V:3\n"+
 //				"C,,16|C,,16|]\n";
 				
 //		body.runListener(s);
     }
     
 }
