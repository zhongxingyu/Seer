 /** 
  * Copyright 2010 Douglas Linder.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package twisted.client;
 
 import java.util.ArrayList;
 
import com.google.gwt.dom.client.Element;

 /** 
  * Common base for all components. 
  * <p>
  * Notice that Components are not widgets and connot be added to
  * things, etc. 
  * <p>
  * Components are specifically for translating marked HTML into a
  * series of Widgets / JS / CSS / Events.
  * <p>
  * If a component depends on another component is should use it's
  * init() call to call createDepedency() with the instances of any 
  * components it depends on.
  * <p>
  * Note that it is entirely possible to create circular dependencies 
  * with components; if that happens no run() call will ever be
  * made to the components involved.
  * <p>
  * Components should only be created by a ComponentFactory.
  * <p>
  * To use a Component extends ComponentFactory to create component
  * instances, and Component to create any number of drop in dynamic
  * components.
  */
 public abstract class Component {
 	
 	/** The root component container. */
 	protected ComponentContainer root = null;
 	
 	/** List of components that depend on this one. */
 	private ArrayList<Component> dependedOn = null;
 	
 	/** List of components that this one depends on. */
 	private ArrayList<Component> dependsOn = null;
 	
 	/** List of values that this component requires. */
 	private ArrayList<String> requiredValues = null;
 	
 	/** List of assets that this component requires. */
 	private ArrayList<String> requiredAssets = null;
 	
 	/** 
 	 * If this component has run. 
 	 * <p>
 	 * More specifically, if the child component has called complete(),
 	 * which is should do in it's run() call. This marks a component as
 	 * ready, and means we do not have to call run(), init(), etc. again
 	 * the next time a parse() call is made.
 	 */
 	private boolean hasRun = false;
 	
 	/** For other Component to register their intent to wait on this one. */
 	public void registerWaitIntent(Component waiting) {
 		if(dependedOn == null)
 			dependedOn = new ArrayList<Component>();
 		if(!dependedOn.contains(waiting))
 			dependedOn.add(waiting);
 	}
 	
 	/** Call to be made to this component when a dependency is resolved. */
 	private void componentReady(Component resolved) {
 		if(dependsOn != null) {
 			if(dependsOn.contains(resolved))
 				dependsOn.remove(resolved);
 		}
 	}
 	
 	/** Call to wait on a specific component instance by id. */
 	protected void requireComponentInstance(String id) {
 		Component c = root.getRegister().getComponent(id);
 		if (c == null) 
 			ComponentLog.trace(this.toString()+": Unable to depend on Component #"+id+". No such id.");
 		if (!c.active()) 
 			requireComponentInstance(c);
 	}
 	
 	/** 
 	 * Call to wait on a specific component instance, by instance. 
 	 * <p>
 	 * Call this in preference to @see dependOn() on the target,
 	 * because this records the target locally for queries to this
 	 * object about what it is waiting for.
 	 */
 	protected void requireComponentInstance(Component target) {
 		if ((target != null) && (!target.active())) {
 			if(dependsOn == null)
 				dependsOn = new ArrayList<Component>();
 			dependsOn.add(target);
 			target.registerWaitIntent(this);
 		}
 	}
 	
 	/** Call to wait on another component, non-specifically, by type. */
 	protected void requireComponentType(String type) {
 		Component instance = root.getRegister().getComponentByType(type);
 		if (instance == null) 
 			ComponentLog.trace(this.toString()+": Unable to depend on Component type "+type+". No such type.");
 		else
 			requireComponentInstance(instance);
 	}
 	
 	/** Adds an asset that this component requires. */
 	protected void requireAsset(String name) {
 		if (requiredAssets == null)
 			requiredAssets = new ArrayList<String>();
 		requiredAssets.add(name);
 	}
 	
 	/** Adds a value that this component requires. */
 	protected void requireValue(String name) {
 		if (requiredValues == null)
 			requiredValues = new ArrayList<String>();
 		requiredValues.add(name);
 	}
 	
 	/** 
 	 * Call to notify all waiting components that this component is ready. 
 	 * <p>
 	 * This function is called by the overseeing ComponentRegister
 	 * after a successful callback to ComponentRegister.componentReady().
 	 */
 	public void notifyWaiting() {
 		if (dependedOn != null) {
 			for(Component dep : dependedOn) {
 				dep.componentReady(this);
 			}
 			// NB. We don't clear dependedOn because it may be used
 			// later for shutdown().
 		}
 	}
 	
 	/** Returns the count of unresolved dependencies. */
 	public int getDependencyCount() {
 		int rtn = 0;
 		if (dependsOn != null)
 			rtn = dependsOn.size();
 		
 		// Look for any missing assets and values. 
 		// ...but only if we don't have any external dependencies.
 		if (rtn == 0) {
 			if (requiredAssets != null) {
 				for (String name : requiredAssets) {
 					if (root.getAsset(name) == null)
 						++rtn;
 				}
 			}
 			if (requiredValues != null) {
 				for (String name : requiredValues) {
 					if (root.getValue(name) == null)
 						++rtn;
 				}
 			}
 		}
 		
 		return(rtn);
 	}
 	
 	/** 
 	 * Returns a list of human readable dependencies that are unresolved.
 	 * <p>
 	 * If possible use @see getDepencenyCount() in preference to this,
 	 * because it's quicker.
 	 */
 	public ArrayList<String> getDependencies() {
 		ArrayList<String> rtn = new ArrayList<String>();
 		if (dependsOn != null) {
 			for (Component c : dependsOn) {
 				String id = c.getContainer().getElement().getId();
 				if ((id == null) || (id.equals("")))
 					id = "*";
 				String item = "Component:" + c.getContainer().getElement().getId() + "@" + c.getContainer().getType();
 				rtn.add(item);
 			}
 		}
 		if (requiredAssets != null) {
 			for (String name : requiredAssets) {
 				if (root.getAsset(name) == null) {
 					String item = "Asset:" + name;
 					rtn.add(item);
 				}
 			}
 		}
 		if (requiredValues != null) {
 			for (String name : requiredValues) {
 				if (root.getValue(name) == null) {
 					String item = "Value:" + name;
 					rtn.add(item);
 				}
 			}
 		}
 		return(rtn);
 	}
 	
 	/** Returns the container. */
 	public ComponentContainer getContainer() {
 		return(root);
 	}
 	
	/** Shortcut to get an asset. */
	public Element getAsset(String id) {
		return(getContainer().getAsset(id));
	}
	
	/** Shortcut to get a value. */
	public String getValue(String id) {
		return(getContainer().getValue(id));
	}
	
 	/** 
 	 * Returns a string name for this component. 
 	 * <p>
 	 * In the form [id]@[type] or just [type] if the
 	 * component has no id on it's root element.
 	 */
 	public String toString() {
 		String id = getContainer().getElement().getId();
 		if ((id != null) && (!id.equals(""))) 
 			id += "@";
 		id += getContainer().getType();
 		return(id);
 	}
 	
 	/** Marks run() as having been completed. */
 	protected void complete() {
 		if (!hasRun) {
 			root.getRegister().componentReady(this);
 			hasRun = true;
 		}
 	}
 	
 	/** Marks run() as having been completed, but unsuccessfully. */
 	protected void failed() {
 	   if (!hasRun) {
 		   root.getRegister().componentFailed(this);
 		   hasRun = true;
 	   }
 	}
 	
 	/** Returns true if the component has run. */
 	public boolean active() {
 		return(hasRun);
 	}
 	
 	/** 
 	 * This function can be overwritten to provide specific shutdown actions. 
 	 * <p>
 	 * In general the shutdown function is not called unless a call to 
 	 * @see ComponentRegister.removeComponent(Component) is made; however,
 	 * if that call is made this shutdown call should allow the component
 	 * to be gracefully removed.
 	 * <p>
 	 * Ie. Unbind event handlers, remove dependencies, remove components
 	 * that depend on this one or throw an exception.
 	 * <p>
 	 * By default this function throws an Exception if other components
 	 * are depending on this one.
 	 * */
 	public void shutdown() throws Exception {
 		if (dependedOn.size() > 0) 
 			throw new Exception("Unresolved component dependency");
 	}
 	
 	/** 
 	 * Staging function for setting up component dependencies.
 	 * <p>
 	 * This call is made after all components have been created,
 	 * and is where any calls to createDependency() should be invoked.
 	 * <p>
 	 * Note that you shouldn't do that from the constructor as the
 	 * ComponentRegister won't have all the components available 
 	 * in some cases; when init() called, all components will have
 	 * been created.
 	 */
 	public abstract void init();
 	
 	/** 
 	 * Invoked when the component is ready.
 	 * <p>
 	 * This being invoked means all components that this component depends
 	 * on are ready; but components that depend on this one will not yet be.
 	 * <p>
 	 * Once run has completed (including async services) it should call
 	 * @see complete().
 	 */
 	public abstract void run();
 	
 	/** 
 	 * Returns the public component API. 
 	 * <p>
 	 * After run has been called this function should return the API;
 	 * if the component does not have an API return null in the subclass.
 	 */
 	public abstract ComponentApi api();
 	
 	/** 
 	 * Creates a component from a tagged HTML block. 
 	 * <p>
 	 * Don't call createDependancy here; it should be invoked in the 
 	 * init() call to ensure all other components have been created.
 	 */
 	public Component(ComponentContainer root) {
 		this.root = root;
 	}
 }
