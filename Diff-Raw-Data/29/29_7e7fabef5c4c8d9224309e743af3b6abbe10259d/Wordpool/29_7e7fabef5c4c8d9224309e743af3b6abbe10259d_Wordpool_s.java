 package essentials.objects;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 
 
 /**
  * Class for word list pool
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "", propOrder = {
     "word"
 })
 @XmlRootElement(name = "wordpool")
 public class Wordpool {
 
     @XmlElement(required = true)
     @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
     @XmlSchemaType(name = "NCName")
     protected List<String> word = new ArrayList<String>();
     
     /**
      * Adds a word to wordpool
      * @param aWord
      */
     public void addWord(String aWord){
     	word.add(aWord);
     }
     
     /**
      * Adds a list of words to wordpool
      * @param aWordList List of words
      */
     public void addWords(List<String> aWordList){
     	for( String w : aWordList ){
     		addWord(w);
     	}
     }
 
     /**
      * Gets list of words 
      */
     public List<String> getWords() {
         if (word == null) {
             word = new ArrayList<String>();
         }
         return word;
     }
     
     /**
      * Checks if pool contains a specific word
      * @param aWord Word to search for
      * @return true if word is in pool, otherwise false
      */
     public boolean contains(String aWord) {
     	aWord = aWord.replace('*', '.');
     	for( String w : word ){
     		if( Pattern.compile(aWord).matcher(w).matches() ) {
     			return true;
     		}
     	}
     	return false;    	
     }
     
     /**
      * Checks if a word (represented by a bricklist) is inside pool
      * @param aBrickList List of bricks
      * @return true if word is in pool, otherweise false
      */
     public boolean contains(BrickList aBrickList){    	
     	return contains(aBrickList.toWord());
     }
     
     /**
      * Checks if a set of words (represented by a list of bricklists)
      * is inside pool
      * @param aBrickLists
      * @return true if at least one word is in pool, otherwise false
      */
     public boolean contains(List<BrickList> aBrickLists){
     	for( BrickList bL : aBrickLists ){
     		if( contains(bL) ){
     			return true;
     		}
     	}
     	return false;
     }
     
     /**
      * Checks if a set of words (represented by a list of bricklists)
      * is inside pool
      * @param aBrickLists
      * @return true if all words are in pool, otherwise false
      */
     public boolean containsAll(List<BrickList> aBrickLists){
     	for( BrickList bL : aBrickLists ){
     		if( !contains(bL) ){
     			return false;
     		}
     	}
     	return true;
     }
     
     
     /**
      * Checks if a word from a list of words is in pool
      * @param aWords List of words
      * @return true if at least one word is in pool, otherwise false
      */
     public boolean containsWords(List<String> aWords) {  	
     	for( String word : aWords ){
     		if( contains(word) ) {
     			return true;
     		}
     	}    	
     	return false;
     }
     
     /**
      * Gets a sublist of bricklists from a 
      * list of bricklists that are in pool
      * @param aBrickLists List of bricklists to search for
      * @return List of bricklists that are in pool
      */
     public List<BrickList> getValid(List<BrickList> aBrickLists){
     	List<BrickList> rBricklist = new ArrayList<BrickList>();
     	for(BrickList bL : aBrickLists){
     		if( contains(bL) ){
     			rBricklist.add(bL);
     		}
     	}
     	return rBricklist;
    }
    
    /**
     * Checks if a word from a list of words is in pool
     * @param aWords
     * @return true if all words are in pool, otherwise false
     */
    public boolean containsAllWords(List<String> aWords){
    	for( String word : aWords ){
    		if( !contains(word) ){
    			return false;
    		}
    	}
    	return true;
    }
    
     
     
     /**
      * Returns object as string
      */
     public String toString(){
     	String rString = "";
     	
     	for( String w : word ){
     		rString = rString.concat( "[" + w + "]" );
     	}
     	
     	return rString;
     }
 
 }
