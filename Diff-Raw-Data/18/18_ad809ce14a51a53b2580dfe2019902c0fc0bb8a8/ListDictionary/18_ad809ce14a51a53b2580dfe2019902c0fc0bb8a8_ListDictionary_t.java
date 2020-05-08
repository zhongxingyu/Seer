 /**
  * 
  */
 package spellcheck;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 
 /**
  * @author Calum Gilchrist
  *
  * Dictionary based on a LinkedList
  */
 public class ListDictionary implements Dictionary {
 
 	private LinkedList<String> dict;
 	
 	public ListDictionary() {
 		dict = new LinkedList<String>();
 	}
 	
 	/**
 	 * Add to the dictionary
 	 */
 	@Override
 	public void insert(String key) throws DictionaryException {
 		
 		//Only add if the key is not already present
		if (!this.find(key) ) {
 			dict.add(key);
		} else {
			throw new DictionaryException();
		}
 			
 	}
 
 	/**
 	 * Remove from the dictionary
 	 */
 	@Override
 	public void remove(String key) throws DictionaryException {
		if (!this.find(key)) {
			dict.remove(key);
		} else {
			throw new DictionaryException();
		}
 
 	}
 
 	/**
 	 * Returns true if the Dictionary contains the specified element
 	 */
 	@Override
	public boolean find(String key) {	
 		return dict.contains(key);
 	}
 
 	/**
 	 * Iterate through the dictionary
 	 */
 	@Override
 	public Iterator<String> elements() {
 		// TODO Auto-generated method stub
 		return dict.iterator();
 	}
 
 }
