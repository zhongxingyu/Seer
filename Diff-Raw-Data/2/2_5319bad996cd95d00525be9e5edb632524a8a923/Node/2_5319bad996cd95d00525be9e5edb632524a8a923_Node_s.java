 import java.util.ArrayList;
 import java.util.HashSet;
 
 
 public class Node {
 
     public Criterion m_splitting_criterion;
     public ArrayList<Entry> m_entries;
     public Node m_lchild;
     public Node m_rchild;
     public int m_label;
 
     public int m_stoppingParam;
 
     public int m_splitting_feature;
     public int m_splitting_value;
     
     public boolean p_leafNode;
     
 	private class SplitInfo {
 		public ArrayList<Entry> leftLst, rightLst; 
 		
 		//split entrylst into two arraylists<Entry> according to the clause 
 		//	left: Entry.attribute.value <= compareValue
 		//	right: Entry.attribute.value > compareValue 
 		public SplitInfo (ArrayList<Entry> entrylst, int attribute, double compareValue) {
 			this.leftLst = new ArrayList<Entry>(); 
 			this.rightLst = new ArrayList<Entry>(); 
 			for (Entry e : entrylst) {
 				if (e.features[attribute] > compareValue)
 					rightLst.add(e); 
 				else 
 					leftLst.add(e); 
 			} 
 		}
 	}
 
 	public Node(Criterion criterion, int stoppingParam) throws Exception {
         m_splitting_criterion = criterion;
         m_stoppingParam = stoppingParam;
         if (stoppingParam < 1)
         	throw new Exception ("stoppingParam must be positive");
 		this.p_leafNode = false; 
     }
 	
 	//gets all possible values for attribute
 	private HashSet<Integer> getAllValues(int attribute){
 		HashSet<Integer> values = new HashSet<Integer>(); 
 		for (Entry e : m_entries) {
 			values.add(e.features[attribute]); 
 		}
 		return values; 	
 	}
 
 	
     // Split the current tree into left/right children
     // Set the splitting feature and values and create l/r child
     public void split() throws Exception {
 		
     	//Stop at the stopping parameter
 		if (m_entries.size() < m_stoppingParam){
 			//stop splitting
 			m_label = calculateLabel(m_entries);
 			p_leafNode = true; 
 			return;
 		}
 		
 		//find the local best split
 		int bestAttr = -1; 
		int bestValue = -1 * Integer.MAX_VALUE; 
 		double bestPerf = -1 * Double.MAX_VALUE; 
 		ArrayList<Entry> bestLchild = null, bestRchild = null;
 		
 		
 		for (int attr = 0; attr < m_entries.get(0).features.length; attr ++){
 			HashSet<Integer> attr_values = getAllValues(attr); 
 			for (Integer v : attr_values){
 				SplitInfo si = new SplitInfo(m_entries, attr, v); 
 				double perf = m_splitting_criterion.calculateSplitPerf(si.leftLst, si.rightLst, m_entries);
 				if (perf > bestPerf) {
 					bestLchild = si.leftLst; 
 					bestRchild = si.rightLst; 
 					bestAttr = attr; 
 					bestValue = v; 
 					bestPerf = perf; 
 				}
 			}
 		}
 		
 		//set everything for this node
 		m_splitting_feature = bestAttr; 
 		m_splitting_value = bestValue; 
 		
 		m_lchild = NodeFactory.returnNode(); 
 		m_lchild.m_entries = bestLchild; 
 		
 		m_rchild = NodeFactory.returnNode(); 
 		m_rchild.m_entries = bestRchild; 
 		
     	m_label = calculateLabel(m_entries); 
     	p_leafNode = false; 
     	
     	
     	//recursively set everything after; 
     	m_lchild.split(); 
     	m_rchild.split(); 
     }
 	
     /// Find the label of this node(i.e., find all children/decedents 
     // of this node, and set label to the majority.
     public static int calculateLabel(ArrayList<Entry> entrylst) {
     	double sum = 0.; 
     	for (Entry e : entrylst) {
     		sum += e.label; 
     	}
     	return sum/entrylst.size() < 0.5 ? 0 : 1; 
     }
    
     public int nodeCount(){
     	return 1 + this.m_lchild.nodeCount() + this.m_rchild.nodeCount(); 
     }
 
 }
