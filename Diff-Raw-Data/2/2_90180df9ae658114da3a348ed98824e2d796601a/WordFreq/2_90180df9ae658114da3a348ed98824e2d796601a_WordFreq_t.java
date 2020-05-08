 package week2.textutil;
 
 import java.util.*;
 import java.io.*;
 
 /**
  * P2 prac wk2.
  * WordFreq. Bepaalt de frequentie van woorden.
  * @author  Theo Ruys
  * @version 2005.02.08
  */
 public class WordFreq implements Analyzer {
     public static final String DELIM = "[\\s\"\\-`',?.!();:]+";
     
     /**
      * Bepaalt de frequentie van de verschillende woorden r en
      * schrijft de gesorteerde lijst naar de System.out.
      * Woorden worden gescheiden door DELIM.
      * @throws IOException als er iets mis gaat bij het lezen
      */
     public void process(String fname, BufferedReader reader) throws IOException {
     	Map<String,Integer> woordenMap = new HashMap<String,Integer>();
     	
     	System.out.println("Bestandsnaam: " + fname);
 
     	boolean doorgaan = true;
     	while(doorgaan) {
            	String line = reader.readLine();
            	if(line != null && !line.equals(":EXIT")) {
            		Scanner s = new Scanner(line).useDelimiter(DELIM);
            		
            		// reads words
     	        while(s.hasNext()) {
     	        	int count;
 
     	        	String woord = s.next().toLowerCase();
     	        	if(woordenMap.containsKey(woord)) {
     	        		count = woordenMap.get(woord) + 1;
     	        	} else {
     	        		count = 1;
     	        	}
     	        	woordenMap.put(woord, count);
     	        }
            	} else {
            		doorgaan = false;
            	}
         }
 
     	// converts HashMap into SortedSet
     	Iterator<String> itrWoordenMap = woordenMap.keySet().iterator();
     	SortedSet<Word> woordenSorted = new TreeSet<Word>();
     	while(itrWoordenMap.hasNext()) {
     		String woord = itrWoordenMap.next();
     		int count = woordenMap.get(woord);
 
     		woordenSorted.add(new Word(woord, count));
     	}
 
     	System.out.println();
     	System.out.println("Frequentie waarmee woorden voorkomen");
     	Iterator<Word> itrWoordenSorted = woordenSorted.iterator();
 
     	int i = 0;
     	while(itrWoordenSorted.hasNext() && i < 10) {
     		i++;
     		System.out.println(" " + itrWoordenSorted.next());
     	}
     	
 
     }
 
 	private class Word implements Comparable<Word> {
 		String word;
 		int count;
 		
 		public Word(String word, int count) {
 			this.word = word;
 			this.count = count;
 		}
 
 		// sorting from high to low and first to last appearance
 		@Override
 		public int compareTo(Word w) {
			return (w.count - count == 0) ? -1 : w.count - count; // minus 1 so words with same count are seen as different words
 		}
 
 		@Override
 		public String toString() {
 			return String.format("%-15s", word) + String.format("%8s", "[" + count + " x]");
 		}
 
 	}
 
     public static void main(String[] args) {
         FilesProcessor fp = new FilesProcessor(new WordFreq());
         fp.openAndProcess(args);
     }
 }
