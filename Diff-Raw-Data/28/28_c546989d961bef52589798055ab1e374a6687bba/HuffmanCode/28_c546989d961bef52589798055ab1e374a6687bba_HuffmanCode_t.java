 import java.lang.Character;
 import java.util.PriorityQueue;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 
 /**
  * Driver to create Huffman codes and encoded strings. 
  * 
  * @author  Josh Gillham
  * @version 12-3-12
  */
 public class HuffmanCode {
 
     /**
      * Establish the codeTree using the PriorityQueue provided. 
      * 
     * @param t the priority queue with data for the Huffman Code.
      * 
      * @return the root of the tree.
      */
    static public HNode buildTree(PriorityQueue<HNode> t) {
         if ( t.isEmpty() )
            throw new IllegalArgumentException( "t cannot be empty." );
         while ( t.size() > 1 ) {
             HNode smaller = t.poll();
             HNode larger = t.poll();
             double combinedFrequencies = 
                 larger.getFrequency() + smaller.getFrequency();
            t.add( new HNode( null, combinedFrequencies, null, larger, smaller ) );
         }
         return t.poll();
     }
     
     /**
      * Maps symbol -> code.
      * 
      * @param tree is the root of the tree.
      * @param outMap is the map to add these mappings.
      */
     static public void addMapCodes( 
             HNode tree, Map<Character, String> outMap ) {
         HNode left = tree.getLeftChild();
         HNode right = tree.getRightChild();
         // Tree or leaf?
         if ( right == null && left == null ) {
             outMap.put( tree.getSymbol(), tree.getCode() );
         }
         // Leaf:
         else {
             if ( left != null )
                 addMapCodes( left, outMap );
             if ( right != null )
                 addMapCodes( right, outMap );
         }
     }
     
     /**
      * Create a map of character -> frequency based on a string. 
      * 
      * @param source the string of characters.
      * 
      * @return the frequency map.
      */
     static public Map<Character, Integer> analyse(String source) {
         // Goes through each character and the resulting data will have 
         //  letters and corresponding frequencies.
         Map<Character, Integer> map = new HashMap<Character, Integer>();
         for ( int i = 0; i < source.length(); ++i ) {
             Character key = new java.lang.Character( source.charAt( i ) );
             Integer value = map.get( key );
             if ( value == null  ) {
                 value = new Integer( 1 );
                 map.put( key, value );
             }
             else {
                 map.put( key, ++value );
             }
         }
         return map;
     }
     
     /**
      * Create a Priority Queue using data from a frequency map.
      * 
      * @param fm frequency map.
      * 
      * @return a priority queue with values from the frequency map.
      */
     static public PriorityQueue<HNode> convert(Map<Character, Integer> fm) {
         PriorityQueue<HNode> ret = new PriorityQueue<HNode>();
         Set<Map.Entry<Character, Integer>> pairs = fm.entrySet();
         for ( Map.Entry<Character, Integer> entry : pairs ) {
             ret.add( new HNode( entry.getKey(), entry.getValue() ) );
         }
         return ret;
     }
     
     /**
      * Fill in the Huffman Codes in a tree.
      * 
      * @param root the root of the Huffman Code subtree.
      * @param code the code to set at the root.
      */
     static public void setCodes(HNode root, String code) {
         HNode leftTree = root.getLeftChild();
         HNode rightTree = root.getRightChild();
        if ( leftTree == null && rightTree == null ) {
            root.setCode( code );
            return;
        }
        
         if ( leftTree != null ) {
             setCodes( leftTree, code + "0" );
         }
         if ( rightTree != null ) {
             setCodes( rightTree, code + "1" );
         }
     }
     
     /**
      * Traverses the tree to find the code.
      * 
      * @param root the tree base to start the search.
      * @param code of the leaf to search for.
      * 
      * @return the match.
      */
     static public HNode find( HNode root, String code ) {
         if ( root.getLeftChild() == null && 
                 root.getRightChild() == null )
             return root;
                 
         if ( code.isEmpty() ) {
             throw new IllegalArgumentException( "Empty code." );
         }
         else {
             char choice = code.charAt( 0 );
             HNode child;
             if ( choice == '0' ) {
                 child = root.getLeftChild();
             }
             else if ( choice == '1' ) {
                 child = root.getRightChild();
             }
             else {
                 throw new IllegalArgumentException( 
                     "Code contains non 1's and 0's."
                 );
             }
             if ( child == null ) {
                 throw new IllegalArgumentException( "Bad Code." );
             }
             return find( child, code.substring( 1 ) );
         }
     }
     
     /** The Huffman code map created from the seed. */
     private Map<Character, String> codeMap = new HashMap<Character, String>();
     /** Root of the Huffman code tree created from the seed. */
     private HNode codeTree;
     /** The seed string used to create the Huffman code. */
     private String seed;
     
     /**
      * Establish this Huffman code based on an initial string.
      * 
      * @param initialString the initial string used to create the code
      */
     HuffmanCode( String initialString ) {
         this.seed = initialString;
         this.createCodeTree( this.createPriorityQueue( 
             this.createFrequencyMap( initialString ) 
         ) );
         this.setCodes( "" );
        if ( this.codeTree.getLeftChild() == null && 
                this.codeTree.getRightChild() == null )
            this.codeTree.setCode( "0" );
            
         this.growCodeMap( codeTree );
     }
     
     /**
      * Establish the codeTree using the PriorityQueue provided. 
      * 
      * @param hh the priority queue with data for the Huffman Code.
      */
     private void createCodeTree(PriorityQueue<HNode> hh) {
         this.codeTree = this.buildTree( hh );
     }
     
     /**
      * Create a map of character -> frequency based on a string. 
      * 
      * @param source the string of characters.
      */
     private Map<Character, Integer> createFrequencyMap(String source) {
         return this.analyse( source );
     }
     
     /**
      * Create a Priority Queue using data from a frequency map.
      * 
      * @param fm frequency map.
      * 
      * @return a priority queue with values from the frequency map.
      */
     private PriorityQueue<HNode> createPriorityQueue(
             Map<Character, Integer> fm) {
         return this.convert( fm );
     }
     
     /**
      * Decode a string using this Huffman code.
      * 
      * @param encoded the string to decode.
      * 
      * @return the decoded string.
      */
     public String decode(String encoded) {
         StringBuilder decodedText = new StringBuilder();
         int i = 0;
         
         while ( i < encoded.length() ) {
             HNode match = this.find( this.codeTree, encoded.substring( i ) );
             decodedText.append( match.getSymbol() );
             i += match.getCode().length();
         }
         return decodedText.toString();
     }
     
     /**
      * Encode a string using this Huffman code.
      * 
      * @param cleartext the string to encode.
      * 
      * @return the encoded string.
      */
     public String encode(String cleartext) {
         StringBuilder encodedText = new StringBuilder();
         for ( int i = 0; i < cleartext.length(); ++i ) {
             char c = cleartext.charAt( i );
            String code = this.codeMap.get( c );
 
             if ( code == null ) 
                 throw new IllegalArgumentException( "Symbol not defined." );
             encodedText.append( code );
             
         }
         return encodedText.toString();
     }
     
     /**
      * Retrieve the internal Huffman Code Map.
      * 
      * @return the code map for this Huffman Code.
      */
     public Map<Character, String> getCodeMap() {
         return this.codeMap;
     }
     
     /**
      * Retrieve the internal Huffman Code Tree.
      * 
      * @return the root of the code tree for this Huffman Code.
      */
     public HNode getCodeTree() {
         return this.codeTree;
     }
     
     /**
      * Create codeMap from a code tree.
      * 
      * @param root the root of a Huffman Code subtree.
      */
     private void growCodeMap(HNode root) {
         this.addMapCodes( root, this.codeMap );
     }
     
     /**
      * Fill in the Huffman Codes in a tree.
      * 
      * @param code the code to set at the root.
      */
     private void setCodes(String code) {
         this.setCodes( this.codeTree, code );
     }
 
 }
