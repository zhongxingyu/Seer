 package preprocessor;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import edu.stanford.nlp.ling.IndexedWord;
 
 public class DependencyNode {
 	 public int sentenceId;
	 public Hashtable<String, DependencyNode> neighbours;
 	 public ArrayList<DependencyNode> parents;
 	 public ArrayList<DependencyNode> link;
 	 public IndexedWord value;
 	 public String lemValue;
 	 public String posTag;
 	 // get all synonyms using: WordNetInterface.getAllSynonymsForPOS(str, pos_tag, Constants.MAX_SYNONIMS);
 	 public ArrayList<String> synonims;
 	 
 	 public DependencyNode(IndexedWord value,int id)
 	 {
 		 this.sentenceId = id;
		 this.neighbours = new Hashtable<String, DependencyNode>();
 		 this.parents = new ArrayList<>();
 		 this.link = new ArrayList<>();
 		 this.value = value;
 		 this.synonims = new ArrayList<>();
 	 }
 }
