 package com.punchline.javalib.entities.components;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.utils.Array;
 
 /**
  * Holds a map of {@link Component Components} with their type identifiers, and
  * helpful methods for accessing and manipulating them.
  * 
  * @author Natman64
  * @author MadcowD
  * 
  */
 public abstract class ComponentManager {
 
 	private Array<Component> components = new Array<Component>();
 	private Map<Class<? extends Component>, Component> componentMap = new HashMap<Class<? extends Component>, Component>();
 
 	/**
 	 * Adds the given component to this container.
 	 * 
 	 * @param component
 	 *            The component to be added.
 	 */
 	public Component addComponent(Component component) {
 		components.add(component);
 		component.onAdd(this);
 		return component;
 	}
 
 	/**
 	 * Removes the given component from this container.
 	 * 
 	 * @param value
 	 *            The component to be removed.
 	 */
 	public void removeComponent(Component component) {
 		component.onRemove(this);
 		components.removeValue(component, true);
		componentMap.remove(component.getClass());
 	}
 
 	/**
 	 * @param type
 	 *            The desired type.
 	 * @return This container's component of that type, or null if there is
 	 *         none.
 	 */
 	@SuppressWarnings("unchecked")
 	public <T extends Component> T getComponent(Class<? extends Component> type) {
 		if (componentMap.containsKey(type)) {
 			return (T) componentMap.get(type);
 		}
 
 		for (Component c : components) {
 			if (type.isInstance(c)) {
 				componentMap.put(type, c);
 				return (T) c;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param type
 	 *            The desired type.
 	 * @return Whether this container has a component of that type.
 	 */
 	public boolean hasComponent(Class<? extends Component> type) {
 		return getComponent(type) != null;
 	}
 
 	/**
 	 * Clears all of this manager's components.
 	 */
 	protected void clearComponents() {
 		for (Component c : components)
 			c.onRemove(this);
 
 		componentMap.clear();
 		components.clear();
 	}
 
 }
