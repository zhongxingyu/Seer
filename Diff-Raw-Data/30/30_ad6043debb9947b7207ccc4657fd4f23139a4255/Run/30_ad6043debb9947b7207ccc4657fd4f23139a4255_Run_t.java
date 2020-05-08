 package concordance;
 
 import java.io.IOException;
 
 /**
  * Initializes and runs the Concordance application.
  * Concordarizer is a glorious application that creates a concordance for a given source text.
  * 
  * @author Sam Nicholls (msn)
  */
 public class Run {
 
 	/**
 	 * Initializes the application and builds the concordance.
 	 * 
 	 * @param args			Any command line arguments specified by the user
 	 * @throws IOException
 	 */
 	public static void main(String args[]) throws IOException{
 		//TODO Missing input file.
 		ConcordanceBuilder cb = new ConcordanceBuilder("indexes.txt", "source.txt");
 		Concordance c = cb.buildConcordance();
 		System.out.println(c.getContext("explosion"));
 		System.out.println(c.getLineNumbers("explosion"));
 	}
 }
