 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 
 public class SimpleHuffProcessor implements IHuffProcessor {
     
     private HuffViewer myViewer;
     private TreeNode myRoot; 
     private HashMap<Integer, String> myMap; 
     private Integer mySize; 
     private int[] myCounts = new int[256]; 
     
     public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
     	// write the magic number
     	BitOutputStream bout = new BitOutputStream(out); 
     	bout.writeBits(BITS_PER_INT, MAGIC_NUMBER); 
     	
     	// write info that allows tree to be recreated
     	for(int k=0; k < ALPH_SIZE; k++){
             bout.writeBits(BITS_PER_INT, myCounts[k]);
         }
     	
     	// write bits needed to encode each character of input file 
     	BitInputStream binput = new BitInputStream(in);
        int next = binput.read(); 
        while(next > 0){	
         	String encoding = myMap.get(next);
         	for(int i=0; i<encoding.length(); i++){
         		char c = encoding.charAt(i);
         		if(c=='0'){ bout.writeBits(1, 0);}
         		else if(c=='1'){ bout.writeBits(1, 1); } 
         	}
        	next = binput.read(); 
         }
         binput.close(); 
         
         bout.writeBits(BITS_PER_INT, PSEUDO_EOF);
     	bout.close(); 
     	
     	return 0; 
     }
 
     public int preprocessCompress(InputStream in) throws IOException {
     	// create forest of nodes
     	HashMap<Integer, TreeNode> forest = new HashMap<Integer, TreeNode>(); 
     	BitInputStream binput = new BitInputStream(in);
         int numBits=0; 
        int next = binput.read(); 
        while(next > 0){  	
         	numBits += 8; 
         	if(forest.containsKey(next)){
         		TreeNode node = forest.get(next);
         		node.myWeight++;
         		forest.put(next, node);
         	}
         	else{
         		TreeNode node = new TreeNode(next, 1); 
         		forest.put(next, node);
         	}
        	next = binput.read(); 
         }
         binput.close(); 
         
         // create list of weights
         for(TreeNode t: forest.values()){
         	int v = t.myValue;
         	int w = t.myWeight;
         	if(v>0){
         		myCounts[v] = w; 
         	}
         }
         
         // turn forest into a single tree
         PriorityQueue<TreeNode> pq = new PriorityQueue<TreeNode>(forest.values()); 
         TreeNode nodeEof = new TreeNode(PSEUDO_EOF, 1); 
         pq.add(nodeEof); 
         myRoot = qShrinker(pq); 
         
         //create map of ints to encodings
         myMap = new HashMap<Integer, String>(); 
         mySize=0; 
         encodePaths(myRoot, ""); 
         
         return numBits; 
     }
     
     public TreeNode qShrinker(PriorityQueue<TreeNode> q){
     	TreeNode tree; 
     	if(q.size()==1){
     		tree = q.poll(); 
     	}
     	else{
     		TreeNode smallest = q.remove();
     		TreeNode nextSmallest = q.remove();
    		TreeNode newNode = new TreeNode(smallest.myValue, 
    				nextSmallest.myWeight+smallest.myWeight, smallest, nextSmallest);
     		q.add(newNode); 
     		tree = qShrinker(q); 
     	}
     	return tree; 
     }
     
     public void encodePaths(TreeNode t, String path){
     	if(t.isLeaf()){
     		myMap.put(t.myValue, path); 
     		mySize += t.myWeight*path.length(); 
     		return; 
     	}
     	else{
     		encodePaths(t.myLeft, path + "0");
     		encodePaths(t.myRight, path + "1");
     	}
     }
 
     public void setViewer(HuffViewer viewer) {
         myViewer = viewer;
     }
 
     public int uncompress(InputStream in, OutputStream out) throws IOException {
 
         BitInputStream binput = new BitInputStream(in); 
         BitOutputStream bout = new BitOutputStream(out); 
     	
         // read magic number
         int magic = binput.readBits(BITS_PER_INT);
         if (magic != MAGIC_NUMBER){
         	binput.close(); 
         	bout.close();
             throw new IOException("magic number not right");
         }
         else{ System.out.println("magic number right"); }
 
         // read in encoding table
         HashMap<Integer, TreeNode> forest = new HashMap<Integer, TreeNode>();
         for(int k=0; k < ALPH_SIZE; k++){
             int bits = binput.readBits(BITS_PER_INT);
             if(bits>0){
             	TreeNode node = new TreeNode(k, bits); 
             	forest.put(k, node); 
             }
         }
         PriorityQueue<TreeNode> pq = new PriorityQueue<TreeNode>(forest.values()); 
         TreeNode nodeEof = new TreeNode(PSEUDO_EOF, 1); 
         pq.add(nodeEof); 
         myRoot = qShrinker(pq);
         System.out.println("weight: " + myRoot.myWeight);
         
         // read remaining bits, map them, and write them out 
         int inbits;
         TreeNode node = myRoot; 
         while (true){
         	inbits = binput.readBits(1); 
             if (inbits == -1){
                 System.err.println("should not happen! trouble reading bits");
                 break; 
             }
             else{ 
                 if ( (inbits & 1) == 0){ node = node.myLeft; } 
                 else{ node = node.myRight;}                  
 
                 if (node.isLeaf()){
                     if (node.myValue==PSEUDO_EOF){
                     	break; 
                     }
                     else{
                    	char data = (char) node.myValue; 
                    	System.out.println(data); 
                    	bout.writeBits(BITS_PER_INT, data);
                     	node = myRoot;
                     }    
                 }
             }
         }
         
         binput.close();
         bout.close(); 
         return 0; 
     }
     
     private void showString(String s){
         myViewer.update(s);
     }
 
 }
