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
 import java.util.HashMap;
 import java.util.Date;
 import java.util.Set;
 
 import twisted.client.impl.ComponentQuery;
 import twisted.client.utils.CommonEvents;
 import twisted.client.utils.GenericCallback;
 import twisted.client.Component;
 import twisted.client.ComponentContainer;
 import twisted.client.ComponentFactory;
 import twisted.client.ComponentLog;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.Timer;
 
 /** 
  * Register for holding components and providing cross component access.
  * <p>
  * This class also provides the base functionality for loading components
  * from the DOM, via the parse() call.
  * <p>
  * To use the ComponentRegister:
  * <ul>
  * 		<li> Implement a number of Component sub-classes.
  * 		<li> Implement a ComponentFactory sub-class.
  * 		<li> Create a ComponentRegister
  * 		<li> Call ComponentRegister.parse()
  * </ul>
  */
 public class ComponentRegister {
 	
 	/** Held components, by element. */
 	private HashMap<Element, Component> elementCache = new HashMap<Element, Component>();
 	
 	/** 
 	 * Held components, by id.
 	 * <p>
 	 * Only components with an id are kept here.
 	 */
 	private HashMap<String, Component> idCache = new HashMap<String, Component>();
 	
 	/** 
 	 * Set of requested components, by id. 
 	 * <p>
 	 * Components are loaded async, so we need to keep track of the
 	 * loads that we are still waiting on completion for.
 	 */
 	private ArrayList<String> componentRequests = new ArrayList<String>();
 	
 	/** One time event listeners for component parsing being completed. */
 	private ArrayList<GenericCallback<Void>> readyListeners = new ArrayList<GenericCallback<Void>>();
 	
 	/** Set of components being run, if any. */
 	private ArrayList<Component> runList = null;
 	
 	/** The component currently being run, if any. */
 	private Component running = null;
 	
 	/** The factory associated with this register. */
 	private ComponentFactory factory = null;
 	
 	/** The timeout for component loading to complete. */
 	private Timer timer = null;
 	
 	/** The timeout for the timer for loading. */
 	private int timeout = 0; 
 	
 	/** If any component creations have failed. */
 	private boolean failed;
 	
 	public ComponentRegister(ComponentFactory factory) {
 		this.factory = factory;
 	}
 	
 	/** Parses the entire DOM body for components. */
 	public void parse() {
 		Element root = Document.get().getBody();
 		parse(root);
 	}
 	
 	/** Parses sub-objects of the root element given for components. */
 	public void parse(Element root) {
 		failed = false;
 		if (timer != null) {
 			timer.schedule(timeout);
 		}
		ComponentQuery elements = ComponentQuery.query("Component", root);
 		int count = elements.getLength();
 		for (int i = 0; i < count; ++i) {
 			root = elements.getItem(i);
 			if (getComponent(root) == null) {
 				ComponentContainer cc = ComponentContainer.get(root, this);
 				String requestId = createRequestId(cc);
 				createComponent(cc, requestId);
 			}
 		}
 	}
 	
 	/** Dispatches a request to the factory. */
 	private void createComponent(final ComponentContainer cc, final String requestId) {
 		final ComponentFactory factory = this.factory;
 		Timer deferredCallback = new Timer() {
 			@Override
 			public void run() {
 				factory.createComponent(cc, requestId);
 			}
 		};
 		deferredCallback.schedule(1); // Async invokation.
 	}
 	
 	/** Creates a unique request id. */
 	private String createRequestId(ComponentContainer cc) {
 		Date now = new Date();
 		String rtn = "r" + Math.random() + "__t" + now.getTime() + "__c" + cc.getType();
 		componentRequests.add(rtn);
 		return(rtn);
 	}
 	
 	/** Returns a component by the ID of the root element. */
 	public Component getComponent(String id) {
 		Component rtn = idCache.get(id);
 		return(rtn);
 	}
 	
 	/** Returns a the first component of the given type. */
 	public Component getComponentByType(String type) {
 		Component rtn = null;
 		for (Element root : elementCache.keySet()) {
 			Component c = getComponent(root);
 			if (c.getContainer().getType().equals(type)) {
 				rtn = c;
 				break;
 			}
 		}
 		return(rtn);
 	}
 	
 	/** Returns a component by the root element. */
 	public Component getComponent(Element root) {
 		Component rtn = elementCache.get(root);
 		return(rtn);
 	}
 	
 	/** Invoked async when a componet has been created. */
 	public void componentCreated(Component c, String requestId) {
 		Element root = c.getContainer().getRootElement();
 		elementCache.put(root, c);
 		if ((root.getId() != null) && (!root.getId().equals("")))
 			idCache.put(root.getId(), c);
 		if(componentRequests.contains(requestId))
 			componentRequests.remove(requestId);
 		if(componentRequests.size() == 0)
 			componentCreationComplete();
 	}
 	
 	/** Invoked async when a component could not be created. */
 	public void componentCreationFailed(String requestId) {
 		if(componentRequests.contains(requestId))
 			componentRequests.remove(requestId);
 		if(componentRequests.size() == 0)
 			componentCreationComplete();
 		ComponentLog.trace("Failed to create component: " + requestId);
 		failed = true;
 	}
 	
 	/** Invoked when all components have been created. */
 	private void componentCreationComplete() {
 		if (componentRequests.size() == 0) {
 			runList = runComponentInit();
 			componentReady(null); // First component.
 		}
 	}
 	
 	/** Inits all components. */
 	private ArrayList<Component> runComponentInit() {
 		ArrayList<Component> rtn = new ArrayList<Component>();
 		Set<Element> elements = elementCache.keySet();
 		for (Element key : elements) {
 			Component c = elementCache.get(key);
 			if (!c.active()) {
 				c.init();
 				rtn.add(c);
 			}
 		}
 		return(rtn);
 	}
 	
 	/** 
 	 * Runs the next component in the run list.
 	 * <p>
 	 * However, components depending on this component are not 
 	 * activated; we simply skip over it and wait for other ones
 	 * which are ready to finish running.
 	 */
 	public void componentFailed(Component target) {
 		if (runList.contains(target))
 			runList.remove(target);
 		ComponentLog.trace("Failed trying to run component: " + target.toString());
 		componentReady(null);
 	}
 	
 	/** 
 	 * Runs the next component in the run list.
 	 * <p>
 	 * If null is passed it is assumed the call is from internal
 	 * not a component; otherwise the notifyDependants() call is
 	 * made on the target.
 	 * <p>
 	 * After the notification, the next component with no dependencies
 	 * is found and run via async call; if none, the register stops.
 	 */
 	public void componentReady(Component target) {
 		if (runList != null) {
 		
 			// Nothing is running if we got this callback.
 			running = null; 
 			
 			// This target has run, so it is considered a resolved
 			// dependency; notify any one depending on it, so our
 			// next search finds (hopefully) another dependency
 			// free component to run.
 			if (target != null) {
 				target.notifyWaiting();
 				if (runList.contains(target))
 					runList.remove(target);
 			}
 			
 			// Find next
 			Component next = null;
 			for (Component c : runList) {
 				if (c.getDependencyCount() == 0) {
 					next = c;
 					break;
 				}
 			}
 			
 			// More?
 			if (next != null)
 				runComponentAsync(next);
 			
 			// Done?
 			else {
 				// Stop wait for components to load.
 				if ((timer != null) && (runList.size() == 0)){
 					timer.cancel();
 					timer = null;
 				}
 				invokeReadyCallbacks();
 			}
 		}
 	}
 	
 	/** Dispatch a run component call async. */
 	private void runComponentAsync(final Component target) {
 		Timer t = new Timer() {
 			@Override
 			public void run() {
 				running = target;
 				target.run();
 			}
 		};
 		t.schedule(1); // Async run this.
 	}
 	
 	/** 
 	 * Logs unresolved components. 
 	 * <p>
 	 * This is optional, but if set any component with unresolved dependencies
 	 * will be logged when the timer expires, along with the details of what
 	 * dependency failed.
 	 * <p>
 	 * Use the @see setTimeout(Timer) call to set a custom timeout for this.
 	 */
 	public void setTimeout(int milliseconds) {
 		if (timer != null)
 			timer.cancel();
 		timer = new Timer() {
 			@Override
 			public void run() {
 				failed = true; // Timeout.
 				invokeReadyCallbacks();
 				componentAudit();
 				timer = null; // Reset
 				timeout = 0;
 			}
 		};
 		timeout = milliseconds;
 	}
 	
 	/** 
 	 * Logs unresolved components. 
 	 * <p>
 	 * As per @see setTimeout(int), but allows a custom timer to be set so that 
 	 * a module can handle it's own errors if components don't load.
 	 */
 	public void setTimeout(Timer t, int milliseconds) {
 		if (timer != null)
 			timer.cancel();
 		timer = t;
 		timeout = milliseconds;
 	}
 	
 	/** Checks for components with unresolved dependencies and logs them. */
 	private int componentAudit() {
 		int waiting = 0;
 		if (runList != null) {
 			if (running != null) {
 				ComponentLog.trace("Component:" + running + " is running. Did you forget to call complete()?");
 				++waiting;
 			}
 			for (Component c: runList) {
 				String component_msg = "Component:" + c + " is waiting: ";
                                 int count = 0;
 				ArrayList<String> missing = c.getDependencies();
 				for (String msg : missing) {
                                         ++count;
 					component_msg += msg;
 					if (missing.indexOf(msg) != (missing.size() - 1))
 						component_msg += ", ";
 				}
                                 if (count == 0) 
                                     component_msg += " No outstanding dependencies.";
 				ComponentLog.trace(component_msg);
 				++waiting;
 			}
 		}
 		if (componentRequests.size() != 0) {
                     for(String c : componentRequests) {
                         ComponentLog.trace("Waiting on component request: " + c);
                     }
                     ComponentLog.trace("Some components may not be implemented by the factory or are taking a long time to load.");
                 }
 		return(waiting);
 	}
 	
 	/** 
 	 * Removes a specific component from the register; effectively turning it off. 
 	 * <p>
 	 * It's quite tricky to go through a cleanly rebind events and remove events, 
 	 * worry about components that depend on this one, etc., so none of that happens
 	 * here; handle it manually in the component's shutdown() call.
 	 */
 	public void removeComponent(Component c) throws Exception {
 		c.shutdown();
 		try {
 			elementCache.remove(c.getContainer().getRootElement());
 			idCache.remove(c.getContainer().getRootElement().getId());
 		}
 		catch(Exception e) {
 		}
 	}
 	
 	/** 
 	 * This is for completely removing all of a specific type of component.
 	 * <p>
 	 * @see #removeComponent(Component) for additional details; that function
 	 * is run on all components of the given type. 
 	 */
 	public void purgeComponents(String type) throws Exception {
 		Component c;
 		while ((c = getComponentByType(type)) != null) {
 			removeComponent(c);
 		}
 	}
 	
 	/** 
 	 * Attaches a one time ready callback which is invoked when all the components have been created. 
 	 * <p>
 	 * If any components failed to be created, etc. the failed callback is invoked.
 	 * Otherwise eventually the success back is invoked. Probably. Use the setTimeout() 
 	 * function to ensure it does eventually get invoked one way or another.
 	 */
 	public void attachReadyListener(GenericCallback<Void> callback) {
 		readyListeners.add(callback);
 	}
 	
 	/** Runs all the waiting ready listeners. */
 	private void invokeReadyCallbacks() {
 		Exception error = null;
 		if (failed) 
 			error = new Exception("Component creation failed.");
 		else if (timer != null)
 			error = new Exception("Timeout occured before component creation was completed.");
 		
 		// We'll do this async so we don't set up crazy locks.
 		for (GenericCallback<Void> c : readyListeners) {
 			if (error != null) 
 				CommonEvents.run(c, error);
 			else
 				CommonEvents.run(c);
 		}
 	}
 }
