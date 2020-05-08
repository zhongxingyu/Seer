 package semanticMarkup.ling.learn;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class DataHolder {
 	// Data Holders
 	// Table sentence
 	public List<Sentence> sentenceTable = new LinkedList<Sentence>();
 
	
	
 	// Table unknownwords
 	public Map<String, String> unknownWordTable = new HashMap<String, String>();
 
 	// Table wordpos
 	public Map<WordPOSKey, WordPOSValue> wordPOSTable = new HashMap<WordPOSKey, WordPOSValue>();
 
 	// Table heuristicnouns
 	public Map<String, String> heuristicNounTable = new HashMap<String, String>();
 
 	// Table singularPlural
 	public Set<SingularPluralPair> singularPluralTable = new HashSet<SingularPluralPair>();
 
 	// Table modifier
 	public Map<String, ModifierTableValue> modifierTable = new HashMap<String, ModifierTableValue>();
 
 	// Table discounted
 	public Map<DiscountedKey, String> discountedTable = new HashMap<DiscountedKey, String>();
 
 	public DataHolder() {
 		sentenceTable = new LinkedList<Sentence>();
 		unknownWordTable = new HashMap<String, String>();
 		wordPOSTable = new HashMap<WordPOSKey, WordPOSValue>();
 		heuristicNounTable = new HashMap<String, String>();
 		singularPluralTable = new HashSet<SingularPluralPair>();
 		modifierTable = new HashMap<String, ModifierTableValue>();
 		discountedTable = new HashMap<DiscountedKey, String>();
 	}
 	
 	public List<Sentence> getSentenceTable(){
 		return this.sentenceTable;
 	}
 	
 	public Map<String, String> getHeuristicNounTable(){
 		return this.heuristicNounTable;
 	}
 	
 	
 	public boolean equals(Object obj) {
 		if (obj==this){
 			return true;
 		}
 		
 		if (obj==null||obj.getClass()!=this.getClass()){
 			return false;
 		}
 		
 		DataHolder myDataHolder = (DataHolder) obj;
 		
 		return ((this.discountedTable.equals(myDataHolder.discountedTable))
 				&& (this.heuristicNounTable.equals(myDataHolder.heuristicNounTable))
 				&& (this.modifierTable.equals(myDataHolder.modifierTable))
 				&& (this.sentenceTable.equals(myDataHolder.sentenceTable))
 				&& (this.singularPluralTable.equals(myDataHolder.singularPluralTable))
 				&& (this.unknownWordTable.equals(myDataHolder.unknownWordTable))
 				&& (this.wordPOSTable.equals(myDataHolder.wordPOSTable))
 				);
 	}
 
 	/**
 	 * 
 	 * @param sgl
 	 * @param pl
 	 * @return if add a pair, return true; otherwise return false
 	 */
 	public boolean addSingularPluralPair(String sgl, String pl) {
 		SingularPluralPair pair = new SingularPluralPair(sgl, pl);
 		boolean result = this.singularPluralTable.add(pair);
 		return result;
 	}
 	
 }
