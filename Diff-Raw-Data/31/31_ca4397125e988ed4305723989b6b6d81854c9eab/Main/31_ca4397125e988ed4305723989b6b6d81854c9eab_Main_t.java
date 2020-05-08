 
 public class Main {
 	private final static String defaultInput = "docs";
 	private final static String defaultOutput = "./eval/peers/1";
 	
 	private final static boolean REMOVE_CLOSED_CLASS = true;
	private final static boolean FIRST_SENTENCE_ONLY = false;
 	
 	public static void main(String[] args) {
 		SummaryGenerator sg = new SummaryGenerator(defaultInput, defaultOutput, REMOVE_CLOSED_CLASS);
 		sg.processAllFiles();
 		sg.scoreAndResults(FIRST_SENTENCE_ONLY);
 	}
 }
