 package lv.semti.morphology.pipetool;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import lv.semti.morphology.analyzer.*;
 import lv.semti.morphology.corpus.Statistics;
 
 public class WordPipe {
 	public static void main(String[] args) throws Exception {
 		Analyzer analyzer = new Analyzer("dist/Lexicon.xml"); 	
 		analyzer.enableVocative = true;
 		analyzer.enableDiminutive = true;
 		analyzer.enablePrefixes = true;
 		analyzer.enableGuessing = true;
 		analyzer.enableAllGuesses = true;
 		analyzer.meklētsalikteņus = true; 
 		analyzer.setCacheSize(10000);
 		
 		Statistics statistics = new Statistics("dist/Statistics.xml");
 		
 		boolean full_output = false;
 		boolean tab_output = false;
 		for (int i=0; i<args.length; i++) {
 			if (args[i].equalsIgnoreCase("-full")) full_output = true;
 			if (args[i].equalsIgnoreCase("-tab")) tab_output = true;
 		}
 				
 		PrintStream out = new PrintStream(System.out, true, "UTF8");
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
 	    String s;
 	    while ((s = in.readLine()) != null && s.length() != 0) {
 	    	if (!tab_output) 
 	    		out.println( analyze( analyzer, statistics, s, full_output));
 	    	else out.println( analyze_tab( analyzer, statistics, s));
 	    	out.flush();
 	    }
 	}	
 	
 	private static String analyze(Analyzer analyzer, Statistics statistics, String query, boolean all_options) {
		List<Word> tokens = Splitting.tokenize(analyzer, query, true);
 		LinkedList<String> tokenJSON = new LinkedList<String>();
 		
 		for (Word word : tokens) {
 			if (all_options) tokenJSON.add(word.toJSON());
 			else tokenJSON.add(word.toJSONsingle(statistics));
 		}
 		
 		String s = formatJSON(tokenJSON).toString();
 		tokens = null;
 		tokenJSON = null;
 		
 		return s;
 	}
 	
 	private static String analyze_tab(Analyzer analyzer, Statistics statistics, String query){
		List<Word> tokens = Splitting.tokenize(analyzer, query, true);
 		StringBuilder s = new StringBuilder(); 
 		
 		for (Word word : tokens) {
 			if (s.length()>0) s.append("\t");
 			s.append(word.toTabSepsingle(statistics));
 		}
 		
 		tokens = null;
 		return s.toString();
 	}
 	
 	private static StringBuilder formatJSON(Collection<String> tags) {
 		Iterator<String> i = tags.iterator();
 		StringBuilder out = new StringBuilder("[");
 		while (i.hasNext()) {
 			out.append(i.next());
 			if (i.hasNext()) out.append(", ");
 		}
 		out.append("]");
 		return out;
 	}
 }	
