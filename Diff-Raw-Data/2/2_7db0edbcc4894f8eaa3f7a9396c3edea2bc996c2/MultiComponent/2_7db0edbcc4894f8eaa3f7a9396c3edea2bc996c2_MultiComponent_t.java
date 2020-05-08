 package com.punchline.javalib.entities.components;
 
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
 	 * This MultiComponent's child components.
 	 */
 	protected Array<T> children = new Array<T>();
 	
 	/**
 	 * Constructs a MultiComponent.
 	 * @param base The base Component.
 	 * @param children The child components.
 	 */
 	public MultiComponent(T base, T... children) {
 		this.base = base;
 		
 		for (T child : children) {
 			this.children.add(child);
 		}
 	}
 	
 	//endregion
 	
 	//region Events
 	
 	@Override
 	public void onAdd(ComponentManager container) {
 		base.onAdd(container);
 		
 		for (T child : children) {
 			child.onAdd(container);
 		}
 	}
 
 	@Override
 	public void onRemove(ComponentManager container) {
 		base.onRemove(container);
 		
 		for (T child : children) {
 			child.onRemove(container);
 		}
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
 	 * @return An Array containing all Components contained in this MultiComponent, with the base component at index 0 and children in the same order
 	 * starting from index 1.
 	 */
 	public Array<T> getComponents() {
 		Array<T> components = new Array<T>();
 		components.add(base);
 		components.addAll(children.toArray());
 		return components;
 	}
 	
 	//endregion
 	
 }
