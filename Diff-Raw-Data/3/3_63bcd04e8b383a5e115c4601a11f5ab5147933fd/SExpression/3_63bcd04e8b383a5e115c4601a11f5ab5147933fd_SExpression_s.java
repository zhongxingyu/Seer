 package com.cse755;
 
 /**
  * Class to represent an S-expression or an atom. If atom is non-null, contents
  * of leftChild and rightChild should be ignored because this object represents
  * an atom.
  * 
  * @author Dan Ziemba
  */
 public class SExpression {
 
 	private Atom atom;
 	private SExpression leftChild;
 	private SExpression rightChild;
 	private SExpression parent;
 	private boolean hasDot;
 	private Boolean isList = null;
 
 	/**
 	 * Create empty s-expression.
 	 */
 	public SExpression() {
 	}
 
 	/**
 	 * Create an s-expression that is just an atom.
 	 */
 	public SExpression(Atom atom) {
 		this.atom = atom;
 	}
 
 	/**
 	 * @return the atom
 	 */
 	public Atom getAtom() {
 		return atom;
 	}
 
 	/**
 	 * @param atom
 	 *            the atom to set. Don't do this if left or right child are set.
 	 */
 	public void setAtom(Atom atom) {
 		this.atom = atom;
 		if (this.leftChild != null || this.rightChild != null) {
 			// TODO: remove warnings
 			System.out.println("WARNING: Atom set on non-empty s-expression");
 		}
 	}
 
 	/**
 	 * @return the leftChild
 	 */
 	public SExpression getLeftChild() {
 		return leftChild;
 	}
 
 	/**
 	 * Set left child. Also sets left child's parent to this.
 	 * 
 	 * @param leftChild
 	 *            the s-expression to set as left child
 	 */
 	public void setLeftChild(SExpression leftChild) {
 		this.leftChild = leftChild;
 		this.leftChild.setParent(this);
 		if (this.atom != null) {
 			// TODO: remove warnings
 			System.out.println("WARNING: Left-child set on atom");
 		}
 	}
 
 	/**
 	 * @return the rightChild
 	 */
 	public SExpression getRightChild() {
 		return rightChild;
 	}
 
 	/**
 	 * Set right child. Also sets right child's parent to this.
 	 * 
 	 * @param rightChild
 	 *            the s-expression to set as right child
 	 */
 	public void setRightChild(SExpression rightChild) {
 		this.rightChild = rightChild;
 		this.rightChild.setParent(this);
 		if (this.atom != null) {
 			// TODO: remove warnings
 			System.out.println("WARNING: Right-child set on atom");
 		}
 	}
 
 	/**
 	 * @return the parent s-expression. A value of null indicates this is the
 	 *         root s-expression.
 	 */
 	public SExpression getParent() {
 		return parent;
 	}
 
 	/**
 	 * @param parent
 	 *            the parent s-expression. Leave null to indicate this is root.
 	 */
 	public void setParent(SExpression parent) {
 		this.parent = parent;
 	}
 
 	/**
 	 * @return True if this s-expression had a dot in the middle
 	 */
 	public boolean hasDot() {
 		return hasDot;
 	}
 
 	/**
 	 * @param hasDot
 	 *            True if this s-expression had a dot in the middle
 	 */
 	public void setHasDot(boolean hasDot) {
 		this.hasDot = hasDot;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder tabs = new StringBuilder();
 		SExpression temp = this;
 		while ((temp = temp.getParent()) != null) {
 			tabs.append('\t');
 		}
 		return (isList() ? "*" : "")
 				+ "SExpression ["
 				+ (atom != null ? "atom=" + atom + ", " : "")
 				+ (leftChild != null ? "\n" + tabs + "\tleftChild=" + leftChild
 						+ ", " : "")
 				+ (rightChild != null ? "\n" + tabs + "\trightChild="
 						+ rightChild : "") + "]";
 	}
 
 	public void print() {
 		if (this.atom != null) {
 			atom.print();
 		} else if (isList()) {
 			boolean isListRoot = false;
			isListRoot = (parent == null || (parent != null && !parent.isList()));
 			if (isListRoot)
 				System.out.print('(');
 			leftChild.print();
 			if (!(rightChild.isAtom() && rightChild.getAtom().isNil())) {
 				System.out.print(' ');
 				rightChild.print();
 			}
 			if (isListRoot)
 				System.out.print(')');
 		} else {
 			System.out.print('(');
 			leftChild.print();
 			System.out.print(" . ");
 			rightChild.print();
 			System.out.print(')');
 		}
 		// Print newline if this is root
 		if (parent == null)
 			System.out.println();
 	}
 
 	public boolean isAtom() {
 		return (atom != null);
 	}
 
 	public boolean isList() {
 		if (atom != null && atom.isNil()) {
 			isList = Boolean.TRUE;
 		} else if (rightChild != null && rightChild.isList()) {
 			isList = Boolean.TRUE;
 		} else {
 			isList = Boolean.FALSE;
 		}
 		return isList.booleanValue();
 	}
 
 }
