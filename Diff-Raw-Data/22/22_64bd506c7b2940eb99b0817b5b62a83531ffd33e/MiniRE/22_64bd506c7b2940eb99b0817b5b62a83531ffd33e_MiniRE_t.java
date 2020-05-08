 package greenrubber.parser;
 
 public class MiniRE {
 	private MiniRE() { }
 	
 	public static Grammar getGrammar() {
 		Grammar g = new Grammar();
 		
		Rule miniREProgram = new Rule(g, "<MiniRE-program>");
		miniREProgram.concatenate("begin");
		miniREProgram.concatenate("<statement-list>");
		miniREProgram.concatenate("end");
		g.addRule("<MiniRE-program>", miniREProgram, true);
		
		
 		
 		return g;
 	}
 }
