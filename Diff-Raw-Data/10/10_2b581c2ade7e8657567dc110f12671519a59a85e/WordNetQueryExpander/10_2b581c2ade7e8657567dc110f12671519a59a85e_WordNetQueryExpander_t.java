 package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import edu.mit.jwi.*;
 import edu.mit.jwi.item.IIndexWord;
 import edu.mit.jwi.item.ISynset;
 import edu.mit.jwi.item.IWord;
 import edu.mit.jwi.item.IWordID;
 import edu.mit.jwi.item.POS;
 import edu.mit.jwi.morph.WordnetStemmer;
 
 /**
  * 
  * The WordNetQueryExpander class uses the See <a href="http://wordnet.princeton.edu">WordNet</a> to
  * stem the query words and find its synonyms.
  * 
  * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
  */
 public class WordNetQueryExpander extends AbstractQueryExpander {
 
   // Use singleton pattern
   private IDictionary dict;
 
   private WordnetStemmer stemmer;
 
   private WordNetQueryExpander() {
     dict = null;
   }
 
   @Override
   public boolean init(Properties prop) {
     URL url;
     try {
       url = new URL("file", null, prop.getProperty("dictionary"));
       dict = new Dictionary(url);
       dict.open();
       stemmer = new WordnetStemmer(dict);
     } catch (IOException e) {
       return false;
     }
 
     return true;
   }
 
   private static WordNetQueryExpander instance = null;
 
   public static WordNetQueryExpander getInstance() {
     if (instance == null) {
       instance = new WordNetQueryExpander();
     }
     return instance;
   }
 
   /* returns null if no similar words found */
   @Override
   public List<String> expandQuery(String query, int size) {
 
     List<String> stems = stemmer.findStems(query, POS.VERB);
     if (stems == null || stems.size() == 0)
       return null;
 
     IIndexWord idxWord = dict.getIndexWord(stems.get(0), POS.VERB);
     if (idxWord == null || idxWord.getWordIDs().isEmpty())
       return null;
 
     IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
     IWord word = dict.getWord(wordID);
     ISynset synset = word.getSynset();
     List<IWord> words = synset.getWords();
     List<String> expandedQueries = new ArrayList<String>(words.size());
    for (IWord w : synset.getWords()) {
      String lemma = w.getLemma();
      lemma = lemma.replaceAll("_", " ");
      expandedQueries.add(lemma);
    }
     
     if (expandedQueries.size() > size)
       return expandedQueries.subList(0, size);
 
     return expandedQueries;
   }
 
   public static void main(String[] args) throws IOException {
     AbstractQueryExpander expander = WordNetQueryExpander.getInstance();
     Properties prop = new Properties();
     prop.setProperty("dictionary", "src/main/resources/data/wordnet-dict");
     expander.init(prop);
 
     String query = "affected";
    List<String> expandedQueries = expander.expandQuery(query, 100);
 
     for (String expandedQuery : expandedQueries) {
       System.out.println(expandedQuery);
     }
   }
 }
