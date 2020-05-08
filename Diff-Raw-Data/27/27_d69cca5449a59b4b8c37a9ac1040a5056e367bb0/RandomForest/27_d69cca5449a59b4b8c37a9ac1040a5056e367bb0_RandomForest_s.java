 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;

 import edu.rit.mp.ObjectBuf;
 import edu.rit.pj.Comm;
 
 /**
  * A RandomForest is simply a collection of DecisionTrees.
  * These trees are grown in a certain random way, and they vote on decisions.
  */
 public class RandomForest<D> {
     
     /**
      * Grows a random forest from a list of samples.
      *
      * @param attrs     The attributes and their possible values.
      * @param samples   The sample data to train on.
      * @param size      The number of trees in the forest.
      * @param n         The number of sample records to choose with replacement
      *                  for each tree.
      * @param m         The best attribute at each node in a tree will be chosen
      *                  from m attributes selected at random without replacement.
      * @return          A new RandomForest.
      */
 	static Comm world;
 	static int rank, size;
 	static List<String> decisionbyvote=null;
	static List<String> mode;
     public static <D> RandomForest<D> growRandomForest(
     		
             Map<String,List<String>> attrs,
             List<Sample<D>> samples,
             int size,
             int n,
             int m)
     {
         List<DecisionTree<D>> trees = new ArrayList<DecisionTree<D>>(size);
         for (int i = 0; i < size; i++) {
             List<Sample<D>> sampleChoices = ListUtils.choices(samples, n);
             trees.add(DecisionTree.growDecisionTree(attrs, sampleChoices, m));
         }
         return new RandomForest<D>(trees);
     }
 
     /** The trees in this forest. */
     private List<DecisionTree<D>> trees;
 
     /**
      * Private constructor, for some reason.
      *
      * @param trees     The trees in this forest.
      */
     private RandomForest(List<DecisionTree<D>> trees) {
         this.trees = trees;
     }
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static void main(String args[]) throws IOException
     {
     	Comm.init(args);
     	world=Comm.world();
     	rank=world.rank();
     	size=world.size();
    	ObjectBuf<String> gather;
     	Scanner scan;
     	Map<String,String> choices=new HashMap<String,String>();
     	Map<String,List<String>> attrs=new HashMap<String,List<String>>();
     	String decision = null;
     	List<Sample<String>> samples=new ArrayList<Sample<String>>();
     	File sample=new File("INPUT.txt");
     	List<String> type=new ArrayList<String>();
     	decisionbyvote=new ArrayList<String>();
    	mode=new ArrayList<String>();
     	gather=ObjectBuf.buffer();
     	Sample test;
     	try {
 			scan=new Scanner(sample);
 			int number_of_attributes=scan.nextInt();
 			String[] attributes=new String[number_of_attributes];
 			
 			for(int i=0;i<number_of_attributes;i++){
 				attributes[i]=scan.next();
 				}
 			for(int i=0;i<number_of_attributes;i++){
 				int num=scan.nextInt();
 				for(int j=0;j<num;j++){
 					type.add(scan.next());
 				}
 				
 				attrs.put(attributes[i], type);
 				//System.out.println(attrs);
 				type=new ArrayList<String>();
 			}
 			while(scan.hasNext()){
 				for(int i=0;i<=number_of_attributes;i++){
 				if(i!=number_of_attributes){
 					choices.put(attributes[i], scan.next());
 				}
 				else{
 					decision=scan.next();
 				}
 				}
 				//System.out.println(choices);
 				//System.out.println(decision);
 				samples.add(new Sample(choices,decision));
 				choices=new HashMap<String,String>();
 			}
 			
 		} catch (FileNotFoundException e) {
 			System.err.print("File Not found");
 		}
     	test=samples.get(8);
     	System.out.println(test.choices);
     	RandomForest forest=growRandomForest(attrs,samples,3,5,9);
     	for(int i=0;i<forest.trees.size();i++){
     		Tree treeresult=(Tree) forest.trees.get(i);
    		mode.add((String) treeresult.decide(test.choices));
     		
     	}
    	String final_decision=ListUtils.mode(mode);
    	System.out.println("Decision by "+rank+" which is "+mode+" and the final decision taken by this forest is "+final_decision);
     	if(rank!=0){
    		gather.fill(final_decision);
     		world.send(0, gather);
     	}
     	else{
    		decisionbyvote.add(final_decision);
     		for(int i=1;i<size;i++){
     			world.receive(i, gather);
    			decisionbyvote.add(gather.get(0));
     		}
     		System.out.println(decisionbyvote);
     		System.out.println(ListUtils.mode(decisionbyvote));
     	}
     	
     	
     	
     }
     
 }
