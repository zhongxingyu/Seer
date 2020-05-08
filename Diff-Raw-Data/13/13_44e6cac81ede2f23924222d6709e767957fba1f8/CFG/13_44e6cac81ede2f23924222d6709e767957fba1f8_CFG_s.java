 package ir.cfg;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Stack;
 
 public class CFG {
 
 	String label;
 	
 	public BasicBlock startBB;
 	public BasicBlock exitBB;
 	
 	public BasicBlock currentBB;
 	public BasicBlock currentJoinBB;
 
 	public CFG(String lbl) {
 		label = lbl;
 		startBB = new BasicBlock("start");
 		exitBB = new BasicBlock("exit");
 		currentBB = startBB;
 //		addBranch(startBB, exitBB);//FIXME not sure why this was in here. caused unconditional link from start of program to exit.
 	}
 
 	public void setCurrentBB(BasicBlock current) {
 		currentBB = current;
 	}
 
 	public void setCurrentJoinBB(BasicBlock currentJoin) {
 		currentJoinBB = currentJoin;
 	}
 
 	// branch is '=>' in comments
 	public static void addBranch(BasicBlock source, BasicBlock destination) {
 		source.succ.add(destination);
 		destination.pred.add(source);
 	}
 	
 	// linear link is '->' in comments
 	public static void addLinearLink(BasicBlock source, BasicBlock destination) {
 		source.next = destination;
 		destination.prev = source;
 	}
 	
 	public String liniarPassPrint() {
 		
 		String str = "";
 
 		Iterator<BasicBlock> bbIt = topDownIterator();
 		while(bbIt.hasNext()) {
 			str += bbIt.next() + "\n";
 		}
 		
 		return str;
 	}
 	
 	public String toString() {
 		return "#" + label + "#" + "\n" + liniarPassPrint();
 	}
 	
 	public void calculateDepths(){
 	    Integer depth = 0;
         HashSet<BasicBlock> visited = new HashSet<BasicBlock>();
         Stack<BasicBlock> currentLevel = new Stack<BasicBlock>();
         Stack<BasicBlock> nextLevel = new Stack<BasicBlock>();
         currentLevel.push(startBB);
         
         while (! (nextLevel.isEmpty() && currentLevel.isEmpty()) ){
             // assign depths and visit
             for (BasicBlock block: currentLevel){
                 block.depth=depth;
                 visited.add(block);
             }
             
             // add next level nodes
             while(!currentLevel.isEmpty()){
                 BasicBlock node = currentLevel.pop();
                 for (BasicBlock edgeNode: node.succ){
                     if (!visited.contains(edgeNode)){
                         nextLevel.add(edgeNode);
                     }else if (!edgeNode.label.equals("while-cond")){
                         nextLevel.add(edgeNode);
                     }
                 }
             }
             currentLevel = nextLevel;
             nextLevel = new Stack<BasicBlock>();
             depth++;
         }
 	}
 
 	public Iterator<BasicBlock> topDownIterator() {
 		return new TopDownLiniarIterator();
 	}	
 	
 	public Iterator<BasicBlock> bottomUpIterator() {
 		return new BottomUpLiniarIterator();
 	}
 	
 	public class TopDownLiniarIterator implements Iterator<BasicBlock> {
 
 		private BasicBlock nextBB;
 		
 		public TopDownLiniarIterator() {
 			nextBB = startBB;
 		}
 		
 		public boolean hasNext() {
 			return nextBB != null;
 		}
 
 		public BasicBlock next() {
 			BasicBlock ret = nextBB;
 			nextBB = nextBB.next;
 			return ret;
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	public class BottomUpLiniarIterator implements Iterator<BasicBlock> {
 
 		private BasicBlock prevBB;
 
 		public BottomUpLiniarIterator() {
 			prevBB = exitBB;
 		}
 
 		public boolean hasNext() {
 			return prevBB != null;
 		}
 
 		public BasicBlock next() {
 			BasicBlock ret = prevBB;
 			prevBB = prevBB.prev;
 			return ret;
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 }
