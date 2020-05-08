 package org.eclipse.dltk.internal.core;
 
 import org.eclipse.dltk.core.IModelElement;
 
 /**
  * Holds cached structure and properties for a model element.
  * Subclassed to carry properties for specific kinds of elements.
  */
 /* package */ class ModelElementInfo {
 
 	/**
 	 * Collection of handles of immediate children of this
 	 * object. This is an empty array if this element has
 	 * no children.
 	 */
 	protected IModelElement[] children;
 
 	/**
 	 * Shared empty collection used for efficiency.
 	 */
 	static Object[] NO_NON_SCRIPT_RESOURCES = new Object[] {};	
 	
 	protected ModelElementInfo() {
 		this.children = ModelElement.NO_ELEMENTS;
 	}
 	public void addChild(IModelElement child) {
 		if (this.children == ModelElement.NO_ELEMENTS) {
 			setChildren(new IModelElement[] {child});
 		} else {
 			if (!includesChild(child)) {
 				setChildren(growAndAddToArray(this.children, child));
 			}
 		}
 	}
 	public Object clone() {
 		try {
 			return super.clone();
 		}
 		catch (CloneNotSupportedException e) {
 			throw new Error();
 		}
 	}
 	public IModelElement[] getChildren() {
 		return this.children;
 	}
 	/**
 	 * Adds the new element to a new array that contains all of the elements of the old array.
 	 * Returns the new array.
 	 */
 	protected IModelElement[] growAndAddToArray(IModelElement[] array, IModelElement addition) {
 		IModelElement[] old = array;
 		array = new IModelElement[old.length + 1];
 		System.arraycopy(old, 0, array, 0, old.length);
 		array[old.length] = addition;
 		return array;
 	}
 	/**
 	 * Returns <code>true</code> if this child is in my children collection
 	 */
 	protected boolean includesChild(IModelElement child) {
 		
 		for (int i= 0; i < this.children.length; i++) {
			if (this.children[i].equals(child)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	/**
 	 * Returns an array with all the same elements as the specified array except for
 	 * the element to remove. Assumes that the deletion is contained in the array.
 	 */
 	protected IModelElement[] removeAndShrinkArray(IModelElement[] array, IModelElement deletion) {
 		IModelElement[] old = array;
 		array = new IModelElement[old.length - 1];
 		int j = 0;
 		for (int i = 0; i < old.length; i++) {
 			if (!old[i].equals(deletion)) {
 				array[j] = old[i];
 			} else {
 				System.arraycopy(old, i + 1, array, j, old.length - (i + 1));
 				return array;
 			}
 			j++;
 		}
 		return array;
 	}
 	public void removeChild(IModelElement child) {
 		if (includesChild(child)) {
 			setChildren(removeAndShrinkArray(this.children, child));
 		}
 	}
 	public void setChildren(IModelElement[] children) {
 		this.children = children;
 	}
 	
 }
