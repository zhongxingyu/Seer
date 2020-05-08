 package cs4120.der34dlc287lg342.xi.cfg;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import cs4120.der34dlc287lg342.xi.ir.*;
 import cs4120.der34dlc287lg342.xi.ir.context.Label;
import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 
 public class Trace {
 	CFG node;
 	ArrayList<CFG> blocks;
 	public Trace(CFG node){
 		this.node = node;
 		blocks = new ArrayList<CFG>();
 		//add_labels(node, new HashSet<CFG>());
 		recompute(node, new HashSet<CFG>());
 	}
 	
 	public void flatten(Func f){
 		HashSet<CFG> seen = new HashSet<CFG>();
		CFG node = null;
 		while (!blocks.isEmpty()){
			node = blocks.get(0);
 			blocks.remove(0);
 			
 			while (!seen.contains(node)){
 				// mark node
 				seen.add(node);
 				f.add(node.ir); // append
 				
 				for (CFG next : node.succ()){
 					if (!seen.contains(next)){
 						node = next;
 						break;
 					}
 				}
 			}
 		}
		
		if (!(node.ir instanceof Return || node.ir instanceof Jump) && node.child1 != null){
			if (node.child1.ir instanceof LabelNode){
				f.add(new Jump(((LabelNode)node.child1.ir).label));
			} else {
				ArrayList<VisualizableTreeNode> children = f.children;
				int i = children.indexOf(node.child1.ir);
				Label l = new Label();
				if (i != -1){
					children.add(i, new LabelNode(l));
					f.add(new Jump(l));
				}
			}
		}
 	}
 	
 	public void recompute(CFG node, HashSet<CFG>seen){
 		if (node == null) return;
 		if (seen.contains(node))
 			return;
 		seen.add(node);
 		blocks.add(node);
 		
 		for (CFG next : node.succ()){
 			if (!next.parents.contains(node)){
 				next.parents.add(node);
 			}
 			
 			recompute(next, seen);
 		}
 	}
 	
 	
 	public boolean is_child2(ArrayList<CFG> parents, CFG node){
 		for (CFG prev : parents){
 			if (prev.child2 == node){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean is_jump(ArrayList<CFG> parents, CFG node){
 		for (CFG prev : parents){
 			if (prev.ir instanceof Jump && prev.child1 == node){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 //	public void add_labels(CFG node, HashSet<CFG> seen){
 //		if (seen.contains(node)){
 //			return;
 //		}
 //		seen.add(node);
 //		
 //		if (node.pred().size() > 1 || is_child2(node.pred(), node) || is_jump(node.pred(), node)){
 //			// insert label before
 //			CFG label = new CFG(new LabelNode(new Label()));
 //			seen.add(label);
 //			for (CFG parent : node.pred()){
 //				if (parent.child1 == node){
 //					parent.child1 = label;
 //					if (parent.ir instanceof Jump){
 //						((Jump)parent.ir).label = ((LabelNode)label.ir).label;
 //					}
 //				} else{
 //					parent.child2 = label;
 //					if (parent.ir instanceof Cjump){
 //						((Cjump)parent.ir).to = ((LabelNode)label.ir).label;
 //					}
 //				}
 //			}
 //			label.parents = node.parents;
 //			label.child1 = node;
 //			node.parents = new ArrayList<CFG>();
 //			node.parents.add(label);
 //		}
 //		
 //		for (CFG next : node.succ()){
 //			add_labels(next, seen);
 //		}
 //	}
 }
