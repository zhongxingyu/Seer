 package features;
 
 import java.util.List;
 
 import annotationclasses.AuxTokenInfoAnnotation;
 import dataclasses.AuxTokenInfo;
 
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.trees.Tree;
 import edu.stanford.nlp.ling.HasIndex;
 
 public class SyntacticRelationFeature implements TempEvalFeature {
 	
 	private static final String SYN_REL_STRING = "__SYN_REL__";
 	
 	private Tree getLeaf(Tree tree, int idx) {
 		if (tree == null) return null;
 		if (tree.isLeaf()) {
 			if (((HasIndex)tree.label()).index() == idx) return tree;
 			return null;
 		}
 		for (Tree child : tree.children()) {
 			Tree leaf = getLeaf(child, idx);
 			if (leaf != null) return leaf;
 		}
 		return null;
 	}
 	
 	@Override
 	public void add(List<String> features, CoreLabel token1, CoreLabel token2) {
 		AuxTokenInfo aux1 = token1.get(AuxTokenInfoAnnotation.class); // aux token info for timex
 		AuxTokenInfo aux2 = token2.get(AuxTokenInfoAnnotation.class); // aux token info for event
 		assert (aux1.tree.equals(aux2.tree));
 
 		Tree t1 = getLeaf(aux1.tree, aux1.tree_idx);
 		Tree t2 = getLeaf(aux2.tree, aux2.tree_idx);
 		Tree lowest_subsumer = aux1.tree.joinNode(t1, t2);
 		if (lowest_subsumer == null) System.out.println("No Subsumer!");
 		else {
 			String type = lowest_subsumer.toString().split(" ")[0];
			if (type.equals("(NP") || type.equals("(VP") || type.equals("(PP")) {
 				features.add(SYN_REL_STRING + "=" + "SAME_PHRASE");
			} else if (type.equals("(SBAR"))
 				features.add(SYN_REL_STRING + "=" + "SAME_SUBORDINATE CLAUSE");
 			else if (type.equals("(SINV"))
 				features.add(SYN_REL_STRING + "=" + "SAME_INVERTED_CLAUSE");
 			else
 				features.add(SYN_REL_STRING + "=" + "SAME_SENTENCE"); 
 		}
 	}
 }
