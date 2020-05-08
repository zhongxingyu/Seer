 package logic.search;
 
 import data.Graph;
 import data.tree.Trie;
 import data.tree.TrieNode;
 import data.tree.TrieNodeList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import logic.TrieNodeCostSorter;
 
 /**
  *
  * @author Christian
  */
 public class RShrink implements INeighborhood {
 
     private int r; /* the r-shrink r value */
     private final int max_level;
 
     /**
      *
      * @param r r parameter of rshrink
      * @param max_level max_level of rshrink
      */
     public RShrink(int r, int max_level) {
         this.r = r;
         this.max_level = max_level;
     }
 
     public RShrink(){
         this.max_level = 0;
     }
 
     private void rshrink(Trie best, TrieNode node){
         sort_kids(node);
         int size = node.getChildren().size();
         int count = 0;
         TrieNode tdc = null; /* temporarily disconnected child*/
         TrieNodeList fosterParents = null;
         TrieNodeList children = new TrieNodeList();
         children.addAll(node.getChildren());
         
         while((size > 0) && (count < size) && (count < r) ){
             tdc =  children.get(count);
             fosterParents = getFosterParents(best, tdc);
             TrieNode bestFP = null;
             for(TrieNode fp : fosterParents){
 
                 if(incrementalCostAtK(fp,tdc) < decrementalCostAtJ(node,tdc)){
                     if((bestFP == null) || tdc.distance_to(bestFP) > tdc.distance_to(fp)){
                         bestFP = fp;
                     }
                 }
             }
             if( bestFP != null){
                 best.swap_node(tdc, bestFP);
             }
             count++;
         }
     }
 
     private void sort_kids(TrieNode node) {
         Collections.sort(node.getChildren(), new TrieNodeCostSorter(node.getDataNode()));
     }
 
     private double incrementalCostAtK(TrieNode nodei, TrieNode nodek) {
 
         if (nodei.hasChildren()) {
             return nodei.distance_to(nodek) - nodei.costliest_edge(); /* result = Pik - Pij */
         } else {
             return nodei.distance_to(nodek); /* result = Pik */
         }
   
     }
 
     private double decrementalCostAtJ(TrieNode nodei, TrieNode nodek) {
    
         if(nodei.getChildren().size() > 1){
             return nodei.costliest_edge() - nodei.distance_to(nodek); /* Pij - Pik */
         } else{
             return nodei.costliest_edge(); /* Pij */
         }
 
     }
 
     /* returns the nondescendants of node, excluding its parent */
     private TrieNodeList getFosterParents(Trie best, TrieNode node) {
         Iterator<TrieNode> it = best.getTreeNodes().iterator();
         TrieNodeList fpa = getNonDescendants(best, node);
         fpa.remove(node.getParent());
        
         return fpa;
     }
 
     /* returns the nondescendants of node*/
     private TrieNodeList getNonDescendants(Trie best, TrieNode node) {
         TrieNodeList nd = new TrieNodeList();
         Iterator<TrieNode> it = null;
         TrieNode tmp = null;
         TrieNodeList desc = node.getDescendants();
 
         it = best.getTreeNodes().iterator();
 
         while (it.hasNext()) {
             tmp = it.next();
             if (!desc.contains(tmp)) {
                 nd.add(tmp);
             }
         }
 
         nd.remove(node);
 
         return nd;
     }
 
     public TrieNodeList getNodesAtLevel(TrieNode parent, int level){
         TrieNodeList nodes = new TrieNodeList();
 
         if(level == 1){
             nodes = parent.getChildren();
         }else{
             for(TrieNode node : parent.getChildren()){
                 nodes.addAll(getNodesAtLevel(node,level -1));
             }
         }
 
         return nodes;
     }
     /**
      *
      * @param best
      * @return
      */
     public Trie run(Trie best) {
         int level = max_level;
 
         while(level != 0){
             /*check parents at level*/
            TrieNodeList tnl =  getNodesAtLevel(best.getRoot(),level);
            for(int i = 0; i < tnl.size();i++){
                TrieNode node = tnl.get(0);
                 rshrink(best,node);
             }
             level--;
         }
         return best;
     }
 }
