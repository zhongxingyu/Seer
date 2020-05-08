 package uk.ac.ed.inf.proj.xmlnormaliser.validator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import uk.ac.ed.inf.proj.xmlnormaliser.parser.dtd.DTD;
 import uk.ac.ed.inf.proj.xmlnormaliser.parser.fd.FDPath;
 
 
 /**
  * Moving attributes and creating new element types
  * 
  * @author Tomas Tauber
  * 
  */
 public class XNFTransformation {
 
 	private XNFTransformation() {
 	}
 	
 	/**
 	 * Returns elements of the longest path in the set
 	 * @param path set of paths
 	 * @return elements of the longest path
 	 */
 	private static String[] getLongestLastElements(FDPath path) {
 		String[] last = {};
 		int max = 0;
 		for (String p : path) {
 			String[] elements = p.split("\\.");
 			int lastE = elements.length;
 			if (elements[elements.length-1].startsWith("@")) {
 				lastE -= 1;
 			}
 			if (lastE > max) {
 				last = elements;
 				max = lastE;
 			}
 		}
 		return last;
 	}
 	
 	/**
 	 * Given an anomalous XFD of the form q -> p.@l, the set of actions to transform DTD and XFDs is returned  
 	 * @param leftHandSide
 	 * @param rightHandSide
 	 * @param originalXfds
 	 * @param doc
 	 * @return
 	 */
 	public static List<TransformAction> moveAttribute(FDPath leftHandSide, String rightHandSide, Map<FDPath, FDPath> originalXfds, DTD doc) {
 		List<TransformAction> actions = new ArrayList<TransformAction>();
 		String[] q = getLongestLastElements(leftHandSide);
 		int qIndex = q.length - 1;
 		if (q[qIndex].startsWith("@")) {
 			qIndex -= 1;
 		}
 		String lastQ = q[qIndex];
 		StringBuilder qPath = new StringBuilder();
 		for (int i = 0; i <= qIndex; i++) {
 			qPath.append(q[i]).append(".");
 		}
 		String[] p = rightHandSide.split("\\.");
 		String attr = p[p.length - 1];
 		qPath.append(attr);
 		String lastP = p[p.length - 2];
 		actions.add(new TransformAction(TransformAction.ActionType.MOVE_ATTRIBUTE, new Object[] {lastP, lastQ, attr}));
 		doc.moveAttribute(attr, lastP, lastQ);
 		
 		for (Entry<FDPath, FDPath> xfd : originalXfds.entrySet()) {
 			FDPath lhs = xfd.getKey();
 			FDPath rhs = xfd.getValue();
 			boolean action = false;
 			if (lhs.contains(rightHandSide)) {
 				lhs = new FDPath();
 				lhs.addAll(xfd.getKey());
 				lhs.remove(rightHandSide);
 				lhs.add(qPath.toString());
 				action = true;
 			}
 			if (rhs.contains(rightHandSide)) {
 				lhs = new FDPath();
 				lhs.addAll(xfd.getKey());
 				lhs.remove(rightHandSide);
 				lhs.add(qPath.toString());
 				action = true;
 			}
 			if (action) {
 				actions.add(new TransformAction(TransformAction.ActionType.CHANGE_XFD, new Object[] {xfd.getKey(), lhs, rhs}));
 				originalXfds.remove(xfd.getKey());
 				originalXfds.put(lhs, rhs);
 			}
 		}
 		
 		return actions;
 	}
 
 	/**
 	 * Return the last element of the non-attribute path
 	 * @param path set of paths
 	 * @return last element of the non-attribute path (or null if no such path found)
 	 */
 	private static String[] getQElements(FDPath path) {
 		for (String p : path) {
 			if (!p.contains("@")) {
 				return p.split("\\.");
 			}
 		}
 		return null;
 	}	
 
 	/**
 	 * Return the last element of the attribute paths
 	 * @param path set of paths
 	 * @return last element of the non-attribute paths (or null if no such path found)
 	 */
 	private static String[][] getPElements(FDPath path) {
 		String[][] result = new String[path.size() - 1][];
 		int index = 0;
 		for (String p : path) {
 			if (p.contains("@")) {
 				result[index] = p.split("\\.");
 				index++;
 			}
 		}
 		return result;
 	}	
 	
 	
 	/**
 	 * Given an anomalous XFD of the form q, p1.@l1, p1.@l2, p1.@l3..., pn.@ln -> p.@l, the set of actions to transform DTD and XFDs is returned
 	 * @param leftHandSide
 	 * @param rightHandSide
 	 * @param originalXfds
 	 * @param doc
 	 * @return
 	 */
 	public static List<TransformAction> createNewET(int exCount, String namePrefix, FDPath leftHandSide, String rightHandSide, Map<FDPath, FDPath> originalXfds, DTD doc) {
 		List<TransformAction> actions = new ArrayList<TransformAction>();
 		String[] q = getQElements(leftHandSide);
 		String lastQ = q[q.length - 1];
 		
 		StringBuilder qPath = new StringBuilder(q[0]);
 		for (int i = 1; i < q.length; i++) {
 			qPath.append(".").append(q[i]);
 		}
 		
 		actions.add(new TransformAction(TransformAction.ActionType.ADD_NODE, new Object[] {lastQ, namePrefix + exCount}));
 		doc.addElement(namePrefix + exCount);
 		doc.addElementTypeDefinition(lastQ, "(" + doc.getElementTypeDefinition(lastQ) + ", " + namePrefix + exCount + ")");
 		String[][] keys = getPElements(leftHandSide);
 		StringBuilder docTypeDef = new StringBuilder("(");
 		for (int innerCount = 0; innerCount < keys.length; innerCount++) {
 			actions.add(new TransformAction(TransformAction.ActionType.ADD_NODE, new Object[] {namePrefix + exCount, (namePrefix + exCount) + innerCount}));
 			doc.addElement((namePrefix + exCount) + innerCount);
 			docTypeDef.append(namePrefix).append(exCount).append(innerCount).append(",");
 		}
 		docTypeDef.deleteCharAt(docTypeDef.length()-1).append(")");
 		doc.addElementTypeDefinition(namePrefix + exCount, docTypeDef.toString());
 		String[] p = rightHandSide.split("\\.");
 		
 		actions.add(new TransformAction(TransformAction.ActionType.DELETE_NODE, new Object[] {p[p.length - 3], p[p.length - 2]}));
 		actions.add(new TransformAction(TransformAction.ActionType.ADD_NODE, new Object[] {namePrefix + exCount, p[p.length - 2]}));
 		doc.addElementTypeDefinition(p[p.length - 3], doc.getElementTypeDefinition(p[p.length - 3]).replaceAll(p[p.length - 2], "").replaceAll("[(][\\s]*[,|\\|]", "(").replaceAll("[,|\\|][\\s]*[)]", ")"));
		doc.addElementTypeDefinition(namePrefix + exCount, "(" + p[p.length - 2] + ")");
 		int innerCount = 0;
 		for (String[] pn : keys) {
 			actions.add(new TransformAction(TransformAction.ActionType.ADD_ATTRIBUTE, new Object[] {(namePrefix + exCount) + innerCount, pn[pn.length - 1]}));
 			doc.addElementAttribute((namePrefix + exCount) + innerCount, pn[pn.length - 1]);
 			innerCount++;
 		} 
 		actions.add(new TransformAction(TransformAction.ActionType.DELETE_XFD, new Object[] {leftHandSide, new FDPath(rightHandSide)}));
 		originalXfds.get(leftHandSide).remove(rightHandSide);
 		if (originalXfds.get(leftHandSide).isEmpty()) {
 			originalXfds.remove(leftHandSide);
 		}
 		/* conversion of original XFDs - not yet implemented */
 		
 		String qp = qPath.toString();
 		String qpN = qp + "." + namePrefix + exCount;
 		for (innerCount = 0; innerCount < keys.length; innerCount++) {
 			FDPath lhs1 = new FDPath(qpN);
 			FDPath lhs2 = new FDPath(qp);
 			String current = qpN + "." + (namePrefix + exCount) + innerCount;
 			for (String[] pn : keys) {
 				lhs1.add(current + "." + pn[pn.length - 1]);
 				lhs2.add(current + "." +pn[pn.length - 1]);
 			}
 			originalXfds.put(lhs1, new FDPath(current));
 			originalXfds.put(lhs2, new FDPath(qpN));
 			actions.add(new TransformAction(TransformAction.ActionType.ADD_XFD, new Object[] {lhs1, new FDPath(current)}));
 			actions.add(new TransformAction(TransformAction.ActionType.ADD_XFD, new Object[] {lhs2, new FDPath(qpN)}));
 			
 		}
 		
 		return actions;
 	}	
 }
