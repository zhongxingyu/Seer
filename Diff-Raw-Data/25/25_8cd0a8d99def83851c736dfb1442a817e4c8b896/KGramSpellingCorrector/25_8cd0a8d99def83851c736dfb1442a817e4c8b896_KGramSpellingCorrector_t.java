 package cs276.pe1.spell;
 
 import java.io.File;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.HashSet;
 
 import cs276.util.IOUtils;
 import cs276.util.StringUtils;
 import cs276.util.Counter;
 
 public class KGramSpellingCorrector implements SpellingCorrector {
    protected static int K = 2; //start with bigrams, then extend out
     protected static int SE = K-1;//3;//defines extra kgrams - default to k-1 for best performance?
     protected static int WL = 10;//returned Word List size
 	/** Initializes spelling corrector by indexing kgrams in words from a file */
     protected Map<String,Set<String>> kgram;
     protected Counter<String> freq;
 	public KGramSpellingCorrector() {
        File path = new File("/afs/ir/class/cs276/pe1-2011/big.txt.gz");
        //File path = new File("datasources/big.txt.gz");
         
         String extraKGram = "";
         for(int i = 0; i < SE; i++)
             extraKGram += "$";
         
         kgram = new HashMap<String,Set<String>>();
         freq = new Counter<String>();
         
         for (String line : IOUtils.readLines(IOUtils.openFile(path))) {
             for (String word : StringUtils.tokenize(line)) {
                 freq.incrementCount(word);
             	String key = extraKGram+word+extraKGram;//$ to signal beginning/end of word
                 if(key.length() <= K)
                     addWord(key,word);
                 for(int i = 0;i<(key.length()-K+1);i++)
                     addWord(key.substring(i,i+K),word);
             }
         }
 	}
     
     /*
      * Checks key in kgram.  If it exists, add value to the set.
      * Otherwise it creates a new set and add its value to it.
      * Set prevents duplicate values
      */
     private void addWord(String key, String value)
     {
         Set<String> set;
         if(!kgram.containsKey(key))
         {
             set = new HashSet<String>();
         }
         else
         {
             set = kgram.get(key);
         }
         set.add(value);
         kgram.put(key,set);
     }
     
     protected Counter<String> jaccardScore(String word)
     {
         Counter<String> wordCounts = new Counter<String>();
         
         String extraKGram = "";
         for(int i = 0; i < SE; i++)
             extraKGram += "$";
             
         String key = extraKGram+word+extraKGram;
         Set<String> set;
         
         //if the word is smaller than the key size
         if(key.length() <= K)
         {
             if(kgram.containsKey(key))
             {
                 set = kgram.get(key);
                 for(String w : set)
                     wordCounts.incrementCount(w);
             }
             return wordCounts;
         }
         //otherwise iterate through the kgram and increment the count
         double numKeys = key.length()-K+1;
         for(int i = 0;i<numKeys;i++)
         {
             if(kgram.containsKey(key.substring(i,i+K)))
             {
                 set = kgram.get(key.substring(i,i+K));
                 for(String w : set)
                     wordCounts.incrementCount(w,1.0/numKeys);
             }
         }
         return wordCounts;
     }
 
 	public List<String> corrections(String word) {
 		return jaccardScore(word).topK(WL);
 	}
 }
