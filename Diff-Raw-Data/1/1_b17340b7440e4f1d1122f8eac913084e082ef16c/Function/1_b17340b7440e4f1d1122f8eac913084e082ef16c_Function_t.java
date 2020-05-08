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
  * The Function class represents a function in the GP program tree
  * 
  * @author Roman Klapaukh
  * 
  */
 public abstract class Function extends Node {
 
 	private Node args[];
 
 	private int argReturnTypes[];
 
 	/**
 	 * Make a new Function. It has a fixed number of children. A return type and
 	 * a name. The name cannot contain any whitespace.
 	 * 
 	 * Another important restriction is that you cannot have two Nodes such that
 	 * the name of one is the prefix of another, e.g., Int and Integer. This is
 	 * because of the way that nodes are identified when being parsed from
 	 * files. While this problem could be avoided it would require more code on
 	 * the user end which does not seem worth while.
 	 * 
 	 * @param type
 	 *            return type of the function
 	 * @param numArgs
 	 *            number of arguments the function takes
 	 * @param name
 	 *            name of the function (with no whitespace)
 	 */
 	public Function(int type, int numArgs, String name) {
 		super(type, numArgs, name);
 		args = null;
 		argReturnTypes = null;
 
 		// If the number of arguments this function accepts is greater than zero
 		// (which is must be as it is a function
 		// and not a Terminal
 		// then we need to allocate the space to store pointers to the arguments
 		// and to store their return types.
 		args = new Node[numArgs];
 		argReturnTypes = new int[numArgs];
 
 		for (int i = 0; i < numArgs; i++) {
 			args[i] = null;
 			argReturnTypes[i] = -1;
 		}
 	}
 
 	/**
 	 * Set the Nth argument
 	 * 
 	 * @param N
 	 *            the argument to set
 	 * @param node
 	 *            the node that is the argument
 	 */
 	public void setArgN(int N, Node node) {
 		if (node == null)
 			throw new IllegalArgumentException("Node is NULL");
 
 		if (node.getReturnType() != argReturnTypes[N])
 			throw new IllegalArgumentException(
 					"Incorrect return type for argument " + N);
 
 		args[N] = node;
 		node.setParent(this);
 	}
 
 	/**
 	 * Get the argument at position N
 	 * 
 	 * @param N
 	 *            The argument to get
 	 * @return root of subtree at position N
 	 */
 	public Node getArgN(int N) {
 		return args[N];
 	}
 
 	/**
 	 * Set the return type of the Nth argument
 	 * 
 	 * @param N
 	 *            position for which argument to set
 	 * @param type
 	 *            the type to set the argument to
 	 */
 	protected void setArgNReturnType(int N, int type) {
 		if (type <= 0)
 			throw new IllegalArgumentException("Invalid return type: " + type);
 
 		argReturnTypes[N] = type;
 	}
 
 	/**
 	 * Get the return type of the Nth argument
 	 * 
 	 * @param argument
 	 *            to get type for
 	 * @return Type of the argument
 	 */
 	public int getArgNReturnType(int N) {
 		return argReturnTypes[N];
 	}
 
 	public int computeSize() {
 		int size = 1;
 
 		for (int i = 0; i < numArgs; i++) {
 			size += args[i].computeSize();
 		}
 
 		return size;
 	}
 
 	public int traceDepth(int curDepth) {
 		int retDepth = 0;
 		int maxDepth = 0;
 
 		for (int i = 0; i < numArgs; i++) {
 			retDepth = args[i].traceDepth(getDepth());
 			if (retDepth > maxDepth) {
 				maxDepth = retDepth;
 			}
 		}
 		return maxDepth + 1;
 	}
 
 	public int computeDepth(int curDepth) {
 		setDepth(curDepth + 1);
 
 		int retDepth = 0;
 		int maxDepth = 0;
 
 		for (int i = 0; i < numArgs; i++) {
 			retDepth = args[i].computeDepth(getDepth());
 			if (retDepth > maxDepth) {
 				maxDepth = retDepth;
 			}
 		}
 
 		return maxDepth + 1;
 	}
 
 	public void addTreeToVector(List<Node> list) {
 		list.add(this);
 
 		for (int i = 0; i < numArgs; i++) {
 			args[i].addTreeToVector(list);
 		}
 	}
 
 	public void addTreeToVector(List<Node> list, int typeNum) {
 		if (getReturnType() == typeNum)
 			list.add(this);
 
 		for (int i = 0; i < numArgs; i++) {
 			args[i].addTreeToVector(list, typeNum);
 		}
 	}
 
 	/**
 	 * Append a String representing yourself to the StringBuilder passed in.
 	 * This allows a program to be saved to a String. Functions are printed in
 	 * prefix notation Brackets are used to show which function has which
 	 * children. A function foo with n children would be printed as (foo
 	 * child<SUB>0</SUB> ... child<SUB>n</SUB>)
 	 * 
 	 */
 	public void print(StringBuilder s) {
 		s.append(" ( ");
 		s.append(getName());
 
 		for (int i = 0; i < numArgs; i++) {
 			s.append(" ");
 			args[i].print(s);
 		}
 
 		s.append(" ) ");
 	}
 
 	/**
 	 * Set child at position i to be null
 	 * 
 	 * @param i
 	 *            position to set to null
 	 */
 	public void unhook(int i) {
 		args[i] = null;
 	}
 
 	/**
 	 * Set all children to null
 	 * 
 	 */
 	public void unhook() {
 		for (int i = 0; i < numArgs; i++) {
 			args[i] = null;
 		}
 	}
 
 	public int computePositions(int parent) {
 		int pos = parent + 1;
 		this.setPosition(pos);
 		for (int i = 0; i < args.length; i++) {
 			pos = args[i].computePositions(pos);
 		}
 		return pos;
 	}
 
 	public Node getNode(int node) {
 		if (this.getPosition() == node) {
 			return this;
 		}
 		for (int i = 0; i < args.length; i++) {
 			Node n = args[i].getNode(node);
 			if (n != null) {
 				return n;
 			}
 		}
 		return null;
 	}
 
 	public Node getNode(int node, int type, Node best) {
 		// if you are it return yourself
 		if (this.getReturnType() == type && this.getPosition() == node) {
 			return this;
 		}
 		// if you are the best so far, nominate yourself
 		if (this.getReturnType() == type
 				&& (best == null || Math.abs(best.getPosition() - node) > Math
 						.abs(this.getPosition() - node))) {
 			best = this;
 		}
 		// Check to make sure your children aren't it
 		for (int i = 0; i < args.length; i++) {
 			best = args[i].getNode(node, type, best);
			if(best == null) continue;
 			if (best.getPosition() == node
 					|| (i < args.length - 1 && args[i + 1].getPosition() > node && Math
 							.abs(best.getPosition() - node) < Math
 							.abs(args[i + 1].getPosition() - node))) {
 				// can't get any better by searching
 				return best;
 			}
 		}
 
 		return best;
 	}
 
 	@Override
 	public final Function copy(GPConfig conf) {
 		Function a = NodeFactory.newNode(this, conf);
 		a.init(this);
 		for (int i = 0; i < getNumArgs(); i++) {
 			a.setArgN(i, getArgN(i).copy(conf));
 		}
 		return a;
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public Function generate(String s, GPConfig conf) {
 		return NodeFactory.newNode(this, conf);
 	}
 
 	@SuppressWarnings("unchecked")
 	public Function generate(GPConfig conf) {
 		return NodeFactory.newNode(this, conf);
 	}
 }
