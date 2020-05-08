 package serialization;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.*;
 
 import serialization.ParseException;
 
 /**
  * A control that displays a set of hierarchical data as an outline.
  * A Tree node can either be a String or an Entry which is a Tree itself.
  * 
  * @author wafaahma
  *
  */
 public class Tree{
 	/**
 	 * The Entry class is a Tree with a name
 	 */
 	public static class Entry {
 		private final String name;
 		private final Tree tree;
 
 		public Entry(String n, Tree t){
 			name = n; tree = t;
 		}
 
 		public String name(){
 			return name;
 		}
 
 		public Tree tree(){
 			return tree;
 		}
 	}
 
 	// Exactly one of children and value will be null
 	private final List<Entry> children;
 	private final String value;
 
 	/**
 	 * Non-leaf constructor
 	 */
 	public Tree(){
 		children = new ArrayList<Entry>();
 		value = null;
 	}
 
 	/**
 	 * Leaf constructor
 	 * @param String
 	 */
 	public Tree(String v){
 		children = null;
 		value = v;
 	}
 
 	/**
 	 * checks if the node is a leaf
 	 * @return value if not null
 	 */
 	public boolean isLeaf(){
 		return value != null;
 	}
 
 	/**
 	 * Returns the value of Tree node
 	 * @return String value
 	 */
 	public String value(){
 		if(value == null)
 			throw new NullPointerException("Tree is not a leaf node");
 		return value;
 	}
 
 	/**
 	 * Can iterate through the Tree children.
 	 * @return collection of children
 	 */
 	public Iterable<Entry> children(){
 		if(children == null)
 			throw new NullPointerException("Tree is a leaf node");
 		return Collections.unmodifiableList(children);
 	}
 
 	/**
 	 * Adds an Entry to the children
 	 * @param Entry e
 	 */
 	public void add(Entry e){
 		children.add(e);
 	}
 
 	/**
 	 * A getter method for the current Tree Entry
 	 * @param int i
 	 * @return gets the ith position child in the tree 
 	 */
 	public Entry get(int i){
 		return children.get(i);
 	}
 
 	/**
 	 * Will find the String s in the tree
 	 * @param String s
 	 * @return Entry with the name s
 	 */
 	public Tree find(String s) throws ParseException {
 		for(Entry e : children)
 			if(e.name().equals(s))
 				return e.tree();
		throw new ParseException("Can't find child called `s'", this);
 	}
 	
 	/**
 	 * Will find the String s in the tree, used for things that may not exist yet, e.g. door code.
 	 * @param String s
 	 * @return Entry with the name s, or null if it doesn't exist
 	 */
 	public Tree findNull(String s){
 		for(Entry e : children)
 			if(e.name().equals(s))
 				return e.tree();
 		return null;
 	}
 
 	/**
 	 *  Leaf representation:
 	 * List of characters, preceded by |
 	 * Character representation:
 	 * \| - end of string
 	 * \\ - a literal \
 	 * any other character - it
 	 * Non-leaf representation:
 	 * List of trees, preceded by [, succeeded by ]
 	 * (this should be unambiguous)
 	 * 
 	 * @return String Tree
 	 */
 	public String toString(){
 		StringWriter sw = new StringWriter();
 		try{
 			toString(sw);
 		}catch(IOException e){} // impossible!
 		return sw.toString();
 	}
 
 	/**
 	 * Can write a Tree to Appendable objects (e.g bufferWriter)
 	 * @param out
 	 * @throws IOException
 	 */
 	public void toString(Appendable out) throws IOException {
 		if(isLeaf()){
 			out.append('|');
 			out.append(value.replace("\\", "\\\\"));
 			out.append("\\|");
 		}else{
 			out.append('[');
 			for(Entry c : children()){
 				out.append(c.name().replace("\\", "\\\\"));
 				out.append("\\|");
 				c.tree().toString(out);
 			}
 			out.append(']');
 		}
 	}
 
 	private static String readStr(String in, int[] cur) throws ParseException {
 		StringBuilder out = new StringBuilder();
 
 		for(;;){
 			int i = in.indexOf('\\', cur[0]);
 			if(i < 0)
 				throw new ParseException("Unterminated string", cur[0]);
 			out.append(in.substring(cur[0], i));
 			cur[0] = i + 2;
 			if(in.charAt(cur[0] - 1) == '|')
 				return out.toString();
 			out.append(in.charAt(cur[0] - 1));
 		}
 	}
 
 	// cur.length == 1; cur[0] read and updated as cursor position in the string
 	private static Tree fromString(String in, int[] cur) throws ParseException {
 		if(in.charAt(cur[0]) == '|'){
 			cur[0]++;
 			return new Tree(readStr(in, cur));
 		}
 		if(in.charAt(cur[0]) == '['){
 			cur[0]++;
 			Tree out = new Tree();
 			while(in.charAt(cur[0]) != ']')
 				out.add(new Entry(readStr(in, cur), fromString(in, cur)));
 			cur[0]++;
 			return out;
 		}
 		throw new ParseException("Invalid input", cur[0]);
 	}
 
 	/**
 	 * Interprets a toString-output String as a Tree
 	 * @param in
 	 * @return
 	 * @throws ParseException
 	 */
 	public static Tree fromString(String in) throws ParseException {
 		return fromString(in, new int[1]);
 	}
 
 	/**
 	 * Prints the indentation of the Tree in toString
 	 */
 	public void print(){
 		print("");
 	}
 
 	private void print(String indent){
 		if(isLeaf())
 			System.out.println(indent + value());
 		else{
 			String next = indent + " ";
 			for(Tree.Entry c : children()){
 				System.out.println(indent + c.name());
 				c.tree().print(next);
 			}
 		}
 	}
 	
 	/**
 	 * returns the size of the tree
 	 * @return int size
 	 */
 	public int size() {
 		if(children != null) {
 			return children.size();
 		}
 		else {
 			return 0;
 		}
 	}
 }
