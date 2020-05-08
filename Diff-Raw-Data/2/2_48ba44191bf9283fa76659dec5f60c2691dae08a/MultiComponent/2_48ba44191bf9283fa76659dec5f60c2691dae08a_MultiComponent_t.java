 package com.punchline.javalib.entities.components;
 
 import java.util.HashSet;
 
 import com.badlogic.gdx.utils.Array;
 
 /**
  * Component wrapper for multiple Components of the same type. For instance, an entity requiring multiple sprites could contain a MultiComponent<Sprite>.
  * MultiComponents contain a LibGDX array of contained components, but also a single base Component. Whenever information is needed from the MultiComponent, and each
  * Component's information is different, the accessor from base will be used.
  * @author Natman64
  * @created Aug 21, 2013
  * @param <T> The type of Component that will be stored in the MultiComponent.
  */
 public class MultiComponent<T extends Component> implements Component {
 
 	//region Fields/Initialization
 	
 	/**
 	 * This MultiComponent's base component.
 	 */
 	protected T base;
 	
 	/**
 	 * This MultiComponent's child components, including the base.
 	 */
 	protected Array<T> children = new Array<T>();
 	
 	/**
 	 * Constructs a MultiComponent.
 	 * @param base The base Component.
 	 * @param children The child components.
 	 */
 	public MultiComponent(T base, T... children) {
 		this.base = base;
 		
 		this.children.add(base);
 		
 		for (T child : children) {
 			this.children.add(child);
 		}
 	}
 	
 	//endregion
 	
 	//region Events
 	
 	@Override
 	public void onAdd(ComponentManager container) {		
 		for (T child : children) {
 			child.onAdd(container);
 		}
 	}
 
 	@Override
 	public void onRemove(ComponentManager container) {
 		for (T child : children) {
 			child.onRemove(container);
 		}
 	}
 
 	//endregion
 	
 	//region Reordering
 	
 	/**
 	 * Reorders the MultiComponent's children.
 	 * @param order An Array of integers, specifying the new order. Example: {0, 4, 1, 3, 2}
 	 */
 	public void reorder(Array<Integer> order) {
 		//Check for the proper size
 		if (order.size != children.size) throw new IllegalArgumentException("The order array's size does not match the size of the children array.");
 		
 		//Check for duplicates
 		HashSet<Integer> duplicateSet = new HashSet<Integer>();
 		
 		for (Integer i : order) {
 			duplicateSet.add(i);
 		}
 		
 		if (duplicateSet.size() != order.size) throw new IllegalArgumentException("The order array contained duplicate indices.");
 		
 		//Now it should be safe to reorder.
 		Array<T> children = new Array<T>();
 		
 		for (Integer i : order) {
			T child = this.children.get(i);
 			
 			children.add(child);
 		}
 		
 		this.children = children;
 	}
 	
 	
 	//endregion
 	
 	//region Accessors
 	
 	/**
 	 * @return This MultiComponent's base Component.
 	 */
 	public T getBase() {
 		return base;
 	}
 	
 	/**
 	 * @param index The index of one of this MultiComponent's children.
 	 * @return The desired child Component.
 	 */
 	public T getChild(int index) {
 		return children.get(index);
 	}
 	
 	/**
 	 * @return A copy of this MultiComponent's children list.
 	 */
 	public Array<T> getComponents() {
 		return new Array<T>(children);
 	}
 	
 	//endregion
 	
 }
