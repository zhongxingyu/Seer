 package org.eclipse.imp.formatting.spec;
 
 import java.util.Iterator;
 
 import org.eclipse.imp.services.IASTAdapter;
 
 /**
  * I would like to make something that constructs Box AST's directly. For now,
  * I will build strings to represent the boxes, which will be parsed by the
  * Meta-Environment parser before formatting takes place.
  * @author jurgenv
  *
  */
 public class BoxStringBuilder {
 	private IASTAdapter adapter;
 
 	public BoxStringBuilder(IASTAdapter adapter) {
 		this.adapter = adapter;
 	}
 	
 	/**
 	 * Construct a box literal.
 	 * @param source
 	 * @return
 	 */
 	public String literal(String source, Object ast) {
 		int start = adapter.getOffset(ast);
 		int len = adapter.getLength(ast);
 		
 		if (len != 0) {
 		  int end = start + len;
 		  String term = source.substring(start, end);
		  return "\"" + term.replaceAll("\n","\\\\n").replaceAll("\t","\\\\t").replaceAll("\"", "\\\\\"") + "\"";
 		}
 		else {
 			return "";
 		}
 	}
 	
 	/**
 	 * Substitutes the corresponding box values of ast variables in a box pattern
 	 * @param pattern    the box string containing references to variables
 	 * @param variables  the environment that maps ast variables to ast values
 	 * @param boxes      the environment that maps ast values to box string
 	 * @return
 	 */
 	public String substitute(String pattern, VariableEnvironment variables, BoxEnvironment boxes) {
 		Iterator<Object> iter = variables.keySet().iterator();
 		String current = pattern;
 
 		while (iter.hasNext()) {
 			Object var = iter.next();
 			Object val = variables.get(var);
 			String box = boxes.get(val);
 			current = replaceAll(current, var.toString(), box);
 		}
 
 		return current;
 	}
 
 	private String replaceAll(String boxString, String var, String val) {
 		int i;
 		// there is some magic going on here, every variable in the box string
 		// is surrounded by quotes. We adapt the value of i (+1 and -1) to remove
 		// them on-the-fly. This makes the code brittle, since every variable now
 		// HAS to be surrounded by double quotes
 		while ((i = boxString.indexOf(var)) != -1) {
 			boxString = boxString.substring(0, i - 1) + val
 					+ boxString.substring(i + var.length() + 1);
 		}
 		return boxString;
 	}
 
 	/**
 	 * Composes a vertical box around the boxes for the children of this node.
 	 * This code is still buggy since literals and comments are not put in the
 	 * box here.
 	 * @param kids
 	 * @param boxes
 	 * @return
 	 */
 	public String defaultWrapper(String source, Object ast, Object[] kids, BoxEnvironment boxes) {
 		if (adapter.isList(ast)) {
 			return buildList(source,  kids, boxes);
 		}
 		else if (isExpressionStructured(kids)) {
 			return buildBox(source, "H", kids, boxes);
 		}
 		else if (isBlockStructured(kids)) {
 			return buildBlock(source, kids, boxes);
 		}
 		else {
 			return buildBox(source, "V", kids, boxes);
 		}
 	}
 
 	private boolean isExpressionStructured(Object[] kids) {
 		int len = kids.length;
 		
 		if (len == 2) {
 			return true;
 		}
 		else if (len == 3) {
 			if (adapter.getChildren(kids[1]).length == 0) {
 				return true;
 			}
 		}
 		
 		
 		return false;
 	}
 
 	private boolean isBlockStructured(Object[] kids) {
 		int len = kids.length;
 		
 		if (len >= 3) {
 			Object first = kids[0];
 			Object last = kids[len - 1];
 			int lenFirst = adapter.getChildren(first).length;
 			int lenLast = adapter.getChildren(last).length;
 			
 			if (lenFirst == 0 && lenLast == 0) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	private String buildBox(String source, String op, Object[] kids, BoxEnvironment boxes) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(op + " [");
 		for (int i = 0; i < kids.length; i++) {
 			if (i != 0) {
 				appendSeparatorsAndComments(buffer, source, kids[i-1], kids[i]);
 			}
 			String box = boxes.get(kids[i]);
 			assert box != null;
 			buffer.append(box);
 		}
 		buffer.append("]");
 		return buffer.toString();
 	}
 
 	public static String exampleToBox(String source) {
 		String nonWhitespace[] = source.split("[\\s]");
 		StringBuffer result = new StringBuffer();
 		
 		result.append("HV [");
 		
 		for (int i = 0; i < nonWhitespace.length; i++) {
 			String word = nonWhitespace[i];
 			if (word.length() > 0) {
 			  result.append("\"" + word.replaceAll("\"","\\\"") + "\" ");
 			}
 		}
 	
 		result.append("]");
 		return result.toString();
 	}
 	
 	// TODO This is a major workaround for retrieving tokens that can not be found as leaves to any AST.
 	// It works well for list separators, but for comments it does not work. This will just find any non-white space
 	// characters in between two nodes, but comments contain non-whitespace characters. These are removed by this code.
 	private void appendSeparatorsAndComments(StringBuffer buffer, String source, Object first, Object second) {
 		int startFirst = adapter.getOffset(first);
 		int lenFirst = adapter.getLength(first);
 		int startSecond = adapter.getOffset(second);
 		String middle = source.substring(startFirst + lenFirst, startSecond);
 		
 		String nonWhitespace[] = middle.split("[\\s]");
 		for (int i = 0; i < nonWhitespace.length; i++) {
 			if (nonWhitespace[i].length() != 0) {
 			  buffer.append("\"" + nonWhitespace[i] + "\"");
 			}
 		}
 	}
 
 	private String buildList(String source, Object[] kids, BoxEnvironment boxes) {
 		StringBuffer buffer = new StringBuffer();
 		
 		for (int i = 0; i < kids.length; i++) {
 			if (i != 0) {
 				appendSeparatorsAndComments(buffer, source, kids[i-1], kids[i]);
 			}
 			String box = boxes.get(kids[i]);
 			assert box != null;
 			buffer.append(box);
 		}
 		
 		return buffer.toString();
 	}
 
 	private String buildBlock(String source, Object[] kids, BoxEnvironment boxes) {
 		assert kids.length >= 3;
 		
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("V [ ");
 		buffer.append(boxes.get(kids[0]));
 		buffer.append("I is=3 [ V [");
 		
 		for (int i = 1; i < kids.length - 1; i++) {
 			if (i != 0) {
 				appendSeparatorsAndComments(buffer, source, kids[i-1], kids[i]);
 			}
 			
 			String box = boxes.get(kids[i]);
 			assert box != null;
 			buffer.append(box);
 		}
 		
 		buffer.append("] ]");
 		buffer.append(boxes.get(kids[kids.length - 1]));
 		buffer.append("]");
 		return buffer.toString();
 	}
 	
 }
