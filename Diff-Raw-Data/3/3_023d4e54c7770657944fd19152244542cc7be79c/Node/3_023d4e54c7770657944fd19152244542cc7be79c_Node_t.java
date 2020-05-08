 package nz.ac.vuw.ecs.fgpj.core;
 
 /*
  FGPJ Genetic Programming library
  Copyright (C) 2011  Roman Klapaukh
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 import java.util.List;
 
 /**
  * The Node class represents a single Node in a program Tree.
  * 
  * @author Roman Klapaukh
  * 
  */
 public abstract class Node {
 
 	/**
 	 * Represents what sort of node this is for memory management Default value
 	 * is -1 to try force errors
 	 */
 	private int kind = -1;
 
 	/**
 	 * Return type of this node
 	 */
 	private int returnType;
 
 	/**
 	 * Current depth of program tree
 	 */
 	private int depth;
 
 	/**
 	 * This nodes parent in the program tree
 	 */
 	private Node parent;
 
 	/**
 	 * The name of this node
 	 */
 	private String name;
 
 	/**
 	 * The number of children this node has
 	 */
 	protected int numArgs;
 
 	/**
 	 * position of this node in the tree
 	 */
 	private int position;
 
 	/**
 	 * Make a new node with the specified number of arguments, return type and
 	 * name
 	 * 
 	 * @param retType
 	 *            The return type of this node
 	 * @param numArgs
 	 *            The number of argument this node takes
 	 * @param name
 	 *            The name of this node
 	 */
 	public Node(int retType, int numArgs, String name) {
 		this.returnType = retType;
 		this.parent = null;
 		this.name = name;
 		this.numArgs = numArgs;
 	}
 
 	/**
 	 * Gets a new copy of the node. Guaranteed to be new and not come from the
 	 * cache. This code always just calls the constructor of the implementing
 	 * type and returns that result.
 	 * 
 	 * @param config
 	 *            config to generate the node with
 	 * @return the new node
 	 */
 	public abstract Node getNew(GPConfig config);
 
 	/**
 	 * Get the return type of this node
 	 * 
 	 * @return the return type of this node
 	 */
 	public int getReturnType() {
 		return returnType;
 	}
 
 	/**
 	 * Set the current depth
 	 * 
 	 * @param d
 	 *            current depth
 	 */
 	protected void setDepth(int d) {
 		depth = d;
 	}
 
 	/**
 	 * Get the current depth of the tree
 	 * 
 	 * @return current depth
 	 */
 	public int getDepth() {
 		return depth;
 	}
 
 	/**
 	 * The name of the node
 	 * 
 	 * @return the name of the node
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * The number of children this node needs
 	 * 
 	 * @return number of child slots
 	 */
 	public int getNumArgs() {
 		return numArgs;
 	}
 
 	/**
 	 * The Node above this one in the program tree
 	 * 
 	 * @return Nodes parent in a program
 	 */
 	public Node getParent() {
 		return parent;
 	}
 
 	/**
 	 * Set the parent of this node
 	 * 
 	 * @param n
 	 *            The node that is this ones parent
 	 */
 	public void setParent(Node n) {
 		parent = n;
 	}
 
 	/**
 	 * Evaluate this node and its subtree where out is the running value being
 	 * computed. Doing it this way allows for computation orderings that do work
 	 * on the way down the tree. This method does not need to return a value, as
 	 * changes to the argument will be visible to the calling nodes as it is a
 	 * reference
 	 * 
 	 * @param out
 	 *            The running value being computed
 	 */
 	public abstract void evaluate(ReturnData out);
 
 	/**
 	 * Compute the size of the subtree
 	 * 
 	 * @return size of the subtree
 	 */
 	public abstract int computeSize();
 
 	/**
 	 * Compute the depth of the longest subtree, and set the value for depth of
 	 * current node
 	 * 
 	 * @param curDepth
 	 *            depth at so far
 	 * @return depth of longest subtree
 	 */
 	public abstract int computeDepth(int curDepth);
 
 	/**
 	 * Compute the depth of the longest subtree, without modifying node state
 	 * 
 	 * @param curDepth
 	 *            depth at so far
 	 * @return depth of longest subtree
 	 */
 	public abstract int traceDepth(int curDepth);
 
 	/**
 	 * Add this node and all its children to the List (double dispatch trick)
 	 * 
 	 * @param list
 	 *            the lsit to add to
 	 */
 	public abstract void addTreeToVector(List<Node> list);
 
 	/**
 	 * Add all the nodes in the subtree that are of the type typeNum (double
 	 * dispatch trick)
 	 * 
 	 * @param list
 	 *            the list to add the nodes to
 	 * @param typeNum
 	 *            the type that the nodes need to be to be added
 	 */
 	public abstract void addTreeToVector(List<Node> list, int typeNum);
 
 	/**
 	 * Print this node to s. This allows programs to be written to a file. Uses
 	 * Lisp like brackets to show what owns what.
 	 * 
 	 * @param s
 	 *            The StringBuffer to print to
 	 */
 	public abstract void print(StringBuilder s);
 
 	/**
 	 * Return a copy of this node
 	 * 
 	 * @param conf
 	 *            the config to copy it using
 	 * @return a copy of this node
 	 */
 	public abstract Node copy(GPConfig conf);
 
 	/**
 	 * Sets the kind of a node (this is used only for memory management)
 	 * 
 	 * @param kind
 	 *            The kind of the node - a unique int per type
 	 */
 	public Node setKind(int kind) {
 		this.kind = kind;
 		return this;
 	}
 
 	/**
 	 * Initialise the node based on the values in n. n is guaranteed to be of
 	 * the same type as you.
 	 * 
 	 * @param n
 	 *            object of the same type as yourself
 	 */
 	public void init(Node n) {
 	}
 
 	/**
 	 * Re-initialise the state of the node to having a random value. This is
 	 * necessary as when a node is gotten from the NodeFactory as a 'new' node
 	 * it still has it old state which may need to be re-initialised. In general
 	 * nodes do not have state, so the default implementation is simply blank.
 	 * 
 	 * @param conf
 	 */
 	public void reinit(GPConfig conf) {
 	}
 
 	/**
 	 * Returns the kind of the node. Must be the same as the last setKind
 	 * 
 	 * @return the kind of the node
 	 */
 	public int getKind() {
 		return this.kind;
 	}
 
 	/**
 	 * Generate this node based on a string. This string is guaranteed to start
 	 * with your name. The default implementation assumes that it is just the
 	 * name, and has no other information
 	 * 
 	 * @param s
 	 *            generate this node based on this string representation of it
 	 * @param conf
 	 *            the config to generate it using
 	 * @return the generated Node
 	 */
 	public abstract <T extends Node> T generate(String s, GPConfig conf);
 
 	/**
 	 * Return a new copy of this node
 	 * 
 	 * @param conf
 	 *            the config to generate it using
 	 * @return the generated Node
 	 */
 	public abstract <T extends Node> T generate(GPConfig conf);
 
 	public String toString() {
 		StringBuilder s = new StringBuilder();
 		this.print(s);
 		return s.toString();
 	}
 
 	/**
 	 * Gets a node at a specified position
 	 * 
 	 * @param i
 	 *            position to get node at
 	 * @return the node at this position
 	 */
 	public abstract Node getNode(int i);
 
 	/**
 	 * Get the closest node to a specified position that has a specified type
 	 * 
 	 * @param i
 	 *            position to look at
 	 * @param type
 	 *            the type of the desired node
	 * @return closest node to i that has type type. Can be null if no matching
	 *         nodes of the right type
 	 */
 	public Node getNode(int i, int type) {
 		return this.getNode(i, type, null);
 	}
 
 	public abstract Node getNode(int i, int type, Node best);
 
 	/**
 	 * work out the numbering of each node
 	 * 
 	 * @param parent
 	 *            the number of your parent
 	 * @return the number of your largest child
 	 */
 	public abstract int computePositions(int parent);
 
 	/**
 	 * Set the position of this node
 	 * 
 	 * @param pos
 	 *            position of this node
 	 */
 	public void setPosition(int pos) {
 		this.position = pos;
 	}
 
 	/**
 	 * get the position of this node
 	 * 
 	 * @return position of this node
 	 */
 	public int getPosition() {
 		return this.position;
 	}
 }
