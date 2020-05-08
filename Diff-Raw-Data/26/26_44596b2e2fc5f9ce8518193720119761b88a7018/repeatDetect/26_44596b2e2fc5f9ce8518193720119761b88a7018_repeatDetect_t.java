 package bioGUI;
 
 import java.util.ArrayList;
 
 import bioGUI.trie.Edge;
 import bioGUI.trie.Node;
 import bioGUI.trie.Tree;
 import java.util.HashMap;
 import java.util.Map;
 
 public class repeatDetect {
 
 	public repeatDetect() {
 		// TODO Auto-generated constructor stub
 	}
 	
	public static ArrayList<Repeat> getSingleRepeat(String sequence, String text){
 		ArrayList<Repeat> r = new ArrayList<Repeat>();
 		
 		r = getRepeats(text);
		ArrayList<Repeat> sequenceRepeats = new ArrayList<Repeat>();
 		for(int i = 0; i < r.size(); i++){
			if(r.get(i).getText().contains(sequence)){
				sequenceRepeats.add(r.get(i));
 			}
 		}
 		
		return sequenceRepeats;
 	}
 	
 	public static ArrayList<Repeat> getRepeats(String s){
 		ArrayList<Repeat> repeats = new ArrayList<Repeat>();
 		
 		trie x = new trie();
 		bioGUI.trie.Tree bob = x.new Tree();
 		bob.StringToTree(s);
 		System.out.println("bob leafcount: "+bob.leafCount);
 /*		for(int i = 0; i < bob.leafCount; i++){
 			System.out.println("string to leafnode "+i+":"+bob.GetSubString(i));
 		}*/
 /*		for(Edge re : bob.root.getChild()){
 			printBranch(re, "");
 		}*/
 		
 		Node pruned = pruneTree(s, s.length(), bob.root);
 		for(int i = 0; i < pruned.getChild().size(); i++){
 /*			System.out.println("repeat: "+i+":"+pruned.getChild().get(i).data+" hasChildren? "
 					+pruned.getChild().get(i).getNode().getChild().size()+" label: "+pruned.getChild().get(i).getNode().getLabel());
 	*/		
 			Edge e = pruned.getChild().get(i);
 			ArrayList<Repeat> temp = buildRepeats(e, "");
 			for(Repeat r : temp){
 				System.out.println("checking repeat: "+r.getText());
 /*				if(repeats.contains(r)){
 					//merge repeats
 					repeats.get(repeats.indexOf(r)).mergeStartStop(e.start, e.end, e.count);
 				}
 				else{*/
 					boolean nFlag = true;
 					for(char c : r.text.toCharArray()){
 						if(c != 'n' && c != 'N')
 							nFlag = false;
 					}
 					if(!nFlag)
 						repeats.add(r);
 //				}
 			}
 			
 //			printBranch(e,"");
 //			repeats.add(new Repeat(e.data,e.start, e.end, e.start.length));
 		}
 		
 		for(Repeat r: repeats){
 			System.out.println("r: "+r.getText());
 		}
 		
 		return repeats;
 	}
 	
 	
 	
 	private static void printBranch(Edge e, String longestRepeat){
 	 
 			//if have children, keep going
 		System.out.println("numEdges: "+e.getNode().getChild().size());
 		 for(Edge ce : e.getNode().getChild()){
 			 if(ce.getNode().getLabel().equals("ld"))			 
 				 printBranch(ce, longestRepeat+e.data);
 			 else
 				 System.out.println(longestRepeat+e.data+" leafNum: "+e.getNode().getLeafNum()+" getLabel: "+e.getNode().getLabel());
 		 }
 		
 	}
 	
 	private static ArrayList<Repeat> buildRepeats(Edge e, String prev){
 		ArrayList<Repeat> repeats = new ArrayList<Repeat>();
 		
 		if(e.getNode().getChild().isEmpty()){
 			if(e.getNode().getLabel().equals("ld")){
 				Repeat r = new Repeat(prev + e.data,e.start, e.end, e.count);
 /*				if(repeats.contains(r)){
 					//merge repeats
 
 					System.out.println("merging repeat: "+prev + e.data);
 					int index = repeats.indexOf(r);
 					repeats.get(index).mergeStartStop(e.start, e.end, e.count);
 				}
 				else{*/
 					repeats.add(r);
 //				}
 				
 			}
 			else{
 				Repeat r = new Repeat(prev,e.start, e.end, e.count);
 /*				if(repeats.contains(r)){
 					//merge repeats
 					System.out.println("merging repeat: "+prev);
 					int index = repeats.indexOf(r);
 					repeats.get(index).mergeStartStop(e.start, e.end, e.count);
 				}
 				else{*/
 					repeats.add(r);	
 //				}
 			}
 		}
 		else{
 			for(Edge ce: e.getNode().getChild())
 				repeats.addAll(buildRepeats(ce, e.data));
 		}
 		
 		return repeats;
 	}
 	
 	public static Node pruneTree(String s, int size, Node root){
 		// perform depth-first search traversal of suffix tree T(S$)
 		for(Edge e : root.getChild()){
 			pruneTree(s, size, e.getNode());
 		}
 		//Base case - for each leaf node, record its left character ALREADY DO THIS IN TREE CONSTRUCTION
 		if(root.getChild().isEmpty()){
 			//root.setLabel(s.substring(root.getLeafNum()-1, root.getLeafNum()));
 //			System.out.println("root: "+root.getLeafNum()+" label: "+root.getLabel());
 		}
 		
 		//inductive step - for each internal node v examine the labels of its children
 			//if at least one child is labeled as left diverse, then label v as left diverse
 			//if no child is left diverse, then
 				// if all child labels coincide (are the same), set the label of v to be the same
 				// if at least two children have different labels, then set the label of v to be left diverse
 		
 		if(!root.getChild().isEmpty()){ // internal node
 			for(Edge e: root.getChild()){
 				if(e.getNode().getLabel().equals("ld")){
 					root.setLabel("ld");
 //					("ldroot1: "+root.getLeafNum()+" label:"+root.getLabel());
 					break;
 				}
 			}
 			if(!root.getLabel().equals("ld")){
 				String childLabel = "";
 				for(int i = 0; i < root.getChild().size(); i++){
 					if(i == 0){
 						childLabel = root.getChild().get(i).getNode().getLabel();
 					}
 					else if(!root.getChild().get(i).getNode().getLabel().equals(childLabel)){
 						root.setLabel("ld");
 //						System.out.println("ldroot2: "+root.getLeafNum()+" label:"+root.getLabel());
 						break;
 					}
 				}
 				if(!root.getLabel().equals("ld"))
 					root.setLabel(childLabel);
 			}
 		}
 /*		System.out.println("before pruning!!!!!!!!!!!!!!!!!!!!!!!");
 		System.out.print("root: "+root.getLeafNum()+" label:"+root.getLabel());
 		if(root.getLeafNum() == -1){
 			System.out.print(" children: ");
 			for(Edge ie : root.getChild()){
 				System.out.print(ie.getNode().getLeafNum()+" "+ie.data+",");
 			}
 		}*/
 //		System.out.println();
 		// pruning - delete all nodes that are NOT marked as left diverse from T(S$), return remaining tree
 /*		for(int i = 0; i< root.getChild().size(); i++){
 			Edge e = root.getChild().get(i);
 			if(!e.getNode().getLabel().equals("ld")){
 				root.getChild().remove(e);
 			}
 		}*/
 		ArrayList<Edge> toRemove = new ArrayList<Edge>();
 		for(Edge e : root.getChild()){
 			if(!e.getNode().getLabel().equals("ld")){
 				toRemove.add(e);/*root.getChild().remove(e);*/
 			}
 		}
 		for(Edge e : toRemove){
 			root.getChild().remove(e);
 		}
 		
 /*		System.out.println("after pruning..........................");
 		System.out.print("root: "+root.getLeafNum()+" label:"+root.getLabel());
 		if(root.getLeafNum() == -1){
 			System.out.print(" children: ");
 			for(Edge ie : root.getChild()){
 				System.out.print(ie.getNode().getLeafNum()+" "+ie.data+",");
 			}
 		}*/
 		System.out.println();
 		return root;
 	}
 
 
         public static ArrayList<Repeat> magicSoup(ArrayList<Repeat> repeats, String fasta, int limit){
             ArrayList<Repeat> ret = new ArrayList<Repeat>();
             HashMap<Character,Double> pct = atcgCount(fasta);
             double random = 1.0;
 
             for(Repeat r : repeats){
                 Double occurance  = (double)r.getNumRepeat();
 
                 for(Character c : r.getText().toCharArray()){
                 	if(pct.containsKey(c))
                 		random *= pct.get(c);
                 }
 
                 random *= fasta.length();
 
                 Double fold = occurance / random;
 
                 if(fold > limit){
                 	r.setGreater(fold);
                 	ret.add(r);
                 }
             }
             
             return ret;
         }
 
         private static HashMap<Character,Double> atcgCount(String fasta){
             HashMap<Character,Double> hm = new HashMap<Character,Double>();
             int a=0,t=0,c=0,g=0;
 
 
             for(int i = 0; i < fasta.length(); i++){
                 char temp = fasta.charAt(i);
 
                 switch (temp){
                     case 'C':
                         c++;
                         break;
                     case 'G':
                         g++;
                         break;
                     case 'T':
                         t++;
                         break;
                     case 'A':
                         a++;
                         break;
                 }
             }
 
 
             hm.put('C', (double)c/(double)fasta.length());
             hm.put('G', (double)g/(double)fasta.length());
             hm.put('A', (double)a/(double)fasta.length());
             hm.put('T', (double)t/(double)fasta.length());
             return hm;
         }
 }
