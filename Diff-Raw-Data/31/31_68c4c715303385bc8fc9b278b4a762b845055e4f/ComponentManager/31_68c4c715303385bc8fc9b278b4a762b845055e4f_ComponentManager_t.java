 package com.punchline.javalib.entities;
 
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.Map;
import java.util.Map.Entry;
 
 /**
  * Holds a map of {@link Component Components} with their type identifiers, 
  * and helpful methods for accessing and manipulating them.
  * @author Nathaniel + William
  *
  */
 public abstract class ComponentManager {
 
 	private Map<Class<?>, Component> components = new HashMap<Class<?>, Component>();
 	
 	/**
 	 * Adds the given component to this container.
 	 * @param value The component to be added.
 	 */
 	public <T extends Component> T addComponent(Class<? extends Component> type, T value) {
 		components.put(type, value);
 		value.onAdd(this);
 		return (T)value;
 	}
 	
 	/**
 	 * Adds the given component to this container.
 	 * @param value The component to be added.
 	 */
 	public <T extends Component> T addComponent(T value) {
 		components.put(value.getClass(), value);
 		value.onAdd(this);
 		return (T)value;
 	}
 	
 	/**
 	 * Removes the given component from this container.
 	 * @param value The component to be removed.
 	 */
 	public void removeComponent(Component value) {
 		value.onRemove(this);
 		components.remove(value.getClass());
 	}
 	
 	/**
 	 * @param type The desired type.
 	 * @return This container's component of that type.
 	 */
 	@SuppressWarnings("unchecked")
 	public <T extends Component> T getComponent(T... type) {
 		return (T)components.get((Class<T>) type.getClass().getComponentType());
 	}
 	
 	/**
 	 * @param type The desired type.
 	 * @return Whether this container has a component of that type.
 	 */
 	public boolean hasComponent(Class<? extends Component> type){
 		return components.containsKey(type);
 	}
 	
 	/**
 	 * Clears all of the components in a component container
 	 */
 	protected void clearComponents(){
		for(Entry<Class<?>, Component> e : components.entrySet())
			e.getValue().onRemove(this);
 		components.clear();
 	}
 	
 }
