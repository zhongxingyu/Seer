 package org.jmlspecs.jml4.boogie;
 
 import java.util.Hashtable;
 
 import org.eclipse.jdt.internal.compiler.ast.ASTNode;
 
 /**
  * Represents a buffer of Boogie source code that will be
  * passed to a Boogie runtime object. 
  * 
  * To return the actual source code use {@link #getResults()}.
  */
 public class BoogieSource {
 	private int indent = 0;
 	private BoogieSourcePoint sourcePoint = new BoogieSourcePoint(1, 1);
 	private boolean newLine = true;
 	private StringBuffer implBody = new StringBuffer();
 	private Hashtable/* <BoogieSourcePoint, ASTNode> */pointTable = new Hashtable();
 	private static int headersOffset;
 	private int prependsOffset;
 	private static StringBuffer headers;
 	private StringBuffer prepends;
 	
 	public BoogieSource(){
 		prepends = new StringBuffer();
 		implBody.append(getHeaders());
 		sourcePoint.row += headersOffset;
 	}
 
 	/**
 	 * Returns the Boogie source code that was parsed by AST traversal.
 	 * 
 	 * @return the resulting Boogie source code.
 	 */
 	public String getResults() {
 		if (prepends.length() != 0)
 			prependAll();
 		return implBody.toString();
 	}
 
 	/**
 	 * This method is to ensure that the boogie source points point to the corresponding
 	 * ASTNodes after any prepends have been added.
 	 */
 	private void adjustSourcePoints() {
 		Object[] keys = pointTable.keySet().toArray();
 		for (int i = 0; i < keys.length; i++) {
 			System.out.println(keys[i]);
 			ASTNode n = (ASTNode) pointTable.remove(keys[i]);
 			((BoogieSourcePoint) keys[i]).row += prependsOffset;
 			pointTable.put(keys[i], n);
 		}
 		sourcePoint.row += prependsOffset;
 		prependsOffset = 0;
 	}
 
 	private void prependAll() {
		implBody.insert(getHeaders().length(), prepends.toString());
 		prepends = new StringBuffer();
 		adjustSourcePoints();
 	}
 
 	/**
 	 * Returns the node associated with a point in a Boogie source file.
 	 * 
 	 * @param start a position in a Boogie source file
 	 * @return the node which was mapped to a specific point in a Boogie source file
 	 */
 	public ASTNode getTermAtPoint(BoogieSourcePoint start) {
 		return (ASTNode)pointTable.get(start);
 	}
 	
 	/**
 	 * Maps the current position in a Boogie source file to a specific Java ASTNode object.
 	 * 
 	 * @param term the AST node object to map
 	 */
 	public void addPoint(ASTNode term) {
 		pointTable.put(sourcePoint.clone(), term);
 	}
 
 	/**
 	 * Convenience method to append contents to the buffer without performing any 
 	 * Boogie source point mapping. Equivalent to <tt>append(o, null);</tt>.
 	 * 
 	 * @param o the object to append
 	 */
 	public void append(Object o) {
 		append(o, null);
 	}
 
 	public void preprend(Object o) {
 		//Ensures that everything that is prepended is on it's own line
 		//And counts the number of lines that have been prepended.
 		if (((String) o).charAt(((String) o).length()-1) != '\n')
 			o = ((String) o) + "\n";
 		String[] num = ((String) o).split("\\n");
 		prepends.insert(0, o);
 		prependsOffset += num.length;
 	}
 
 	public static String getHeaders() {
 		if (headers == null)
 			populateHeaders();
 		return headers.toString();
 	}
 	
 	/**
 	 * These are the current headers necessary for division and modulus operations to work.
 	 * Should eventually be moved into it's own file.
 	 */
 	private static void populateHeaders() {
 		headers = new StringBuffer();
 		headers.append("axiom (∀ x : int, y: int • {x % y} {x /y} x%y == x - x/y *y);\n");
 		headers.append("axiom (∀x:int,y:int•{x%y}(0<y⇒0<=x%y∧x%y<y)∧(y<0⇒y<x%y∧x%y<=0));\n");
 		headersOffset = 2;
 	}
 
 	/**
 	 * Adds an object to the source buffer by calling {@link #toString()} on the
 	 * object. An optional {@link ASTNode} can be passed in to map the current
 	 * position in the Boogie source (at the insertion point of the object) to a
 	 * specific Java AST node, in order to recall it during problem reporting.
 	 * For instance, when parsing an <tt>@assert</tt> JML annotation, it is
 	 * possible to attach the expression node to a specific point in the Boogie
 	 * file. When the Boogie results are parsed, the point in the Boogie file
 	 * could be used to map back to the assert expression which would allow
 	 * highlighting of the invalid expression through Eclipse's problem reporter
 	 * API.
 	 * 
 	 * @param o the object to output to the buffer ({@link #toString()} is called on the object)
 	 * @param linePointTerm used to map the current position in the Boogie source to a Java AST node
 	 * 		in order to map a position in a Boogie source file to a Java source file (during problem reporting).
 	 * 		If this object is null, no mapping will be done at the current position.
 	 */
 	public void append(Object o, ASTNode linePointTerm) {
 		if (newLine && indent > 0) {
 			for (int i = 0; i < indent; i++) {
 				implBody.append("\t"); //$NON-NLS-1$
 			}
 			sourcePoint.col += indent;
 		}
 		if (linePointTerm != null) { 
 			// map point for given AST node to current boogie position
 			addPoint(linePointTerm);
 		}
 		
 		implBody.append(o);
 		newLine = false;
 		sourcePoint.col += o.toString().length();
 	}
 
 	/**
 	 * Appends an object similar to {@link #append(Object)} but adds a newline after
 	 * the contents have been appended (increasing the line point and resetting indentation).
 	 * 
 	 * @param o the object to append (by calling {@link #toString()} on the object).
 	 */
 	public void appendLine(Object o) {
 		append(o, null);
 		implBody.append("\n"); //$NON-NLS-1$
 		newLine = true;
 		sourcePoint.row++;
 		sourcePoint.col = 1;
 	}
 	
 	/**
 	 * Increases the indent by one.
 	 */
 	public void increaseIndent() {
 		indent++;
 	}
 	
 	/**
 	 * Decreases the indent by one.
 	 */
 	public void decreaseIndent() {
 		indent--;
 		if (indent < 0) indent = 0;
 	}
 }
