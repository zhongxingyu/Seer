 package dbs.project.service;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 import dbs.project.entity.KnockoutMatch;
 import dbs.project.stage.KnockoutStage;
 
 public class KnockoutStageService {
 
 	private static final String[] matchLevel = { "Finale", "Halbfinale", "Viertelfinal", "Achtelfinale"};
 	
 	public static TreeModel getAsTreeModel(KnockoutStage knockoutStage) {
 		class KnockoutTree implements TreeModel {
 			KnockoutMatch root;
 			
 			KnockoutTree(KnockoutMatch finale) {
 				this.root = finale;
 			}
 			public Object getChild(Object parent, int index) {
 				return ((KnockoutMatch) parent).getChilds().get(index);
 			}
 
 			public int getChildCount(Object parent) {
 				return ((KnockoutMatch) parent).getChilds().size();
 			}
 
 			public int getIndexOfChild(Object parent, Object child) {
 				int i = 0;
 				for(KnockoutMatch tmpChild : ((KnockoutMatch) parent).getChilds())
 					if(tmpChild == (KnockoutMatch) child)
 						return i;
 					else
 						i++;
 				
 				return -1;
 			}
 
 			public Object getRoot() {
 				return this.root;
 			}
 
 			public boolean isLeaf(Object node) {
 				return (getChildCount(node) == 0) ? true : false;
 			}
 
 			public void addTreeModelListener(TreeModelListener l) {}
 			public void removeTreeModelListener(TreeModelListener l) {}
 			public void valueForPathChanged(TreePath path, Object newValue) {}
 			
 		}
 		
 		return new KnockoutTree(knockoutStage.getFinalMatch());
 		
 	}
 	
 
 	private static void addRecursivlyKnockoutMatch(KnockoutMatch parent, int height) {
 		if(height > 3)
 			return;
 		
 		String matchName = matchLevel[height];
 		KnockoutMatch match1 = new KnockoutMatch(matchName);
 		KnockoutMatch match2 = new KnockoutMatch(matchName);
 		
 		List<KnockoutMatch> childs = new LinkedList<KnockoutMatch>();
 		childs.add(match1);
 		childs.add(match2);
 		parent.setChilds(childs);
 		
 		int newHeight = ++height;
 		addRecursivlyKnockoutMatch(match1, newHeight);
 		addRecursivlyKnockoutMatch(match2, newHeight);
 	}
 	
 	public static KnockoutStage getDefault() {
 		KnockoutMatch root = new KnockoutMatch("Finale");
 		addRecursivlyKnockoutMatch(root,1);
 		
 		KnockoutStage knockoutStage = new KnockoutStage();
 		knockoutStage.setFinalMatch(root);
 		return knockoutStage;
 	}
 	
 	public static List<KnockoutMatch> getAllMatches(KnockoutStage knockoutStage) {
 		List<KnockoutMatch> matches = new LinkedList<KnockoutMatch>();
 		
 		//BFS iteration
 		Stack<KnockoutMatch> stack = new Stack<KnockoutMatch>();
		stack.add(knockoutStage.getFinalMatch());
 		KnockoutMatch tmpNode;
 		while(stack.size() > 0) {
 			tmpNode = stack.pop();
 			if(tmpNode.getChilds().size() == 0)
 				matches.add(tmpNode);
 			else
 				stack.addAll(tmpNode.getChilds());
 		}
 		
 		return matches;
 	}
 
 }
