 /*
 Ian Buitrago: Slip days used for this project: 0  Slip days used (total): 0
 Ervin Kalemi: Slip days used for this project: 0  Slip days used (total): 0
 
 Pair programming log (> 80% paired)
 9/19 3-4a  Ian, 1 hr
 9/20 8:30-10p Ian Ervin, 4 hrs
 
 Total time X hrs, Z hrs of pair programing
 
 Challenges: Converting the first argument from string to character
 
 Learned:
 
 Notes:
 run with commands:
 javac LZcoding.java
 java -ea LZcoding c test
 java -ea LZcoding d test.cpz
 
 To compare the files: 
 cmp test test.cpz.dcz
 
 2 space indent (google standard)
 */
 
 //Ervin driving now
 //swap every ~30 min
 
 public class LZcoding {
   public static void main(String[] args) throws Exception{
   	//assertion
     assert(args.length == 2);
     assert(args[0].charAt(0) == 'c' || args[0].charAt(0) == 'd');
     assert(args[0].length() == 1);
   	
   	//testing args[]
   	char type = args[0].charAt(0);
   	String file = args[1];
   	
   	//Compress or decompress file based on the input
   	if(type == 'c')
 	    compress(file);
     else if(type == 'd')
       decompress(file);
   }
   
   public static void compress(String file) throws Exception{
     // IO compressor on the file
     IO.Compressor compressor = new IO.Compressor(file);
     // Convert file to an array of characters
     char[] charArray = compressor.getCharacters();
     // Create a dictionary
     Trie dictionary = new Trie();
     // Initial null string to lookup in the file
     String lookup = "";
     
     // For every character in the array of characters run encode
    for (int i= 0; < charArray.length; i++){
       // Appand the next character to the lookup string
       lookup += charArray[i];
       // If the new string formed is not in the dictionary 
       if(!dictionary.contains(lookup)){
         // If the new string is just a character
         if(lookup.length() == 1){
           // Create a transmission node with index 0 and that character
           TrieNode node = new TrieNode(0, lookup);
           // Add that node to the dictionary trie
           dictionary.add(node);
           // Run encode on that character
          compressor.encode(0, charArray[]);
           // Re-initialize the lookup string
           lookup = "";
         // Else the new string is not a character, so we have to find it's parent
         else{
         
         }
       }    
     }
   }
    
    
    public static void decompress(String file) throws Exception{
     
       
    
    }
 }
 class Trie<TrieNode>{
 }
 class TrieNode{
   private String word;
   private int index;
   
  public TrieNode(String word, int index){
     this.word = word;
     this.index = index;
   }
 }
   
