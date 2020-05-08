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
 
 import com.google.gwt.dom.client.Element;
 
 import twisted.client.impl.ComponentFrame;
 import twisted.client.impl.ComponentQuery;
 import twisted.client.ComponentRegister;
 
 /** 
  * Converts arbitrary Elements into valid GWT containers.
  * <p>
  * This wrapper assumes that a component is in the form:
  * <pre>
  * 		&lt;div class="Component ComponentId-[Component Name]"&gt;
  * 			...
  * 			&lt;div class="ComponentValue ComponentId-[Value Name]"/&gt;
  * 			&lt;div class="ComponentAsset ComponentId-[Asset Name]"/&gt;
  * 		&lt;/div&gt;
  * </pre>
  * Note the type of element is actually irrelevant; divs are used
  * purely as an example.
  * <p>
  * The ComponentId-[VALUE] requires the VALUE field to be a single character
  * only sequence, eg. ComponentId-LoginSubmitButton; these ids are used to
  * allow the component direct access to the important elements of the 
  * component so that it can render itself and find it's dependencies.
  */
 public class ComponentContainer extends ComponentFrame {
 	
 	/** Register this component is associated with. */
 	private ComponentRegister register = null;
 	
 	/** Element type, because we do actually cache this. */
 	private String type = null;
 	
 	/** Cached element to classname mappings. */
 	private HashMap<String, ArrayList<Element>> elements = new HashMap<String, ArrayList<Element>>();
 	
 	/** Set of created element/panel instances. */
 	private static HashMap<Element, ComponentContainer> instances = new HashMap<Element, ComponentContainer>();
 	
 	/** 
 	 * Creates a component containers from an element. 
 	 * <p>
 	 * To avoid memory use consider using get() instead.
 	 */
 	protected ComponentContainer(Element root, ComponentRegister parent) {
 		super(root);
 		this.register = parent;
 	}
 	
 	/** Returns the component type */
 	public String getType() {
 		if (type == null) {
 			if (root != null) 
 				type = getComponentId(root.getClassName());
 			if (type == null) {
 				ComponentLog.trace("Found component with no defined type id.");
 				type = "";
 			}
 		}
 		return(type);
 	}
 	
 	private String getComponentId(String classname) {
 		String rtn = null;
 		if (classname != null) {
 	 		String[] classes = classname.split(" ");
 	 		for (int i = 0; i < classes.length; ++i) {
 	 			if (classes[i].startsWith("ComponentId-")) { 
 	 				rtn = classes[i].replaceFirst("ComponentId-", "");
 	 				break;
 	 			}
 	 		}
 		}
 		return(rtn);
 	}
 	
 	/** Returns an instance for the given element. */
 	public static ComponentContainer get(Element root, ComponentRegister register) {
 		ComponentContainer rtn = instances.get(root);
 		if (rtn == null) {
 			rtn = new ComponentContainer(root, register);
 			instances.put(root, rtn);
 		}
 		return(rtn);
 	}
 	
 	/** Returns the register. */
 	public ComponentRegister getRegister() {
 		return(register);
 	}
 	
 	/** Returns an asset by name. */
 	public Element getAsset(String id) {
 		return(getAsset(id, root));
 	}
 	
 	/** Returns assets by name. */
 	public ArrayList<Element> getAssets(String id, int limit) {
 		return(getAssets(id, limit, root));
 	}
 	
 	/** Returns an asset by name, specifying base node. */
 	public Element getAsset(String id, Element root) {
 		ArrayList<Element> set = getAssets(id, 1, root);
 		Element rtn = null;
 		if (set.size() > 0)
 			rtn = set.get(0);
 		return(rtn);
 	}
 	
 	/** Returns assets by name, specifying base node. */
 	public ArrayList<Element> getAssets(String id, int limit, Element root) {
 		ArrayList<Element> rtn = getElements("ComponentId-"+id, limit, root);
 		return(rtn);
 	}
 	
 	/** Returns a value by name. */
 	public String getValue(String id) {
 		return(getValue(id, root));
 	}
 	
 	/** Returns values by name. */
 	public ArrayList<String> getValues(String id, int limit) {
 		return(getValues(id, limit, root));
 	}
 	
 	/** Returns a value by name, specifying base node. */
 	public String getValue(String id, Element root) {
 		ArrayList<String> set = getValues(id, 1, root);
 		String rtn = null;
 		if (set.size() > 0) 
 			rtn = set.get(0);
 		return(rtn);
 	}
 	
 	/** Returns values by name, specifying base node. */
 	public ArrayList<String> getValues(String id, int limit, Element root) {
 		ArrayList<String> rtn = new ArrayList<String>();
 		if (id != null) {
 			ArrayList<Element> set = getElements("ComponentId-"+id, limit, root);
 			for (Element e : set) {
 				rtn.add(e.getInnerHTML());
 			}
 		}
 		return(rtn);
 	}
 	
 	/** 
 	 * Returns elements which are not the child of any other component matching classname.
 	 * <p>
 	 * The elements returned are ones under the node 'root' provided. To search the entire
 	 * component this.root should be passed as 'root'.
 	 */
 	private ArrayList<Element> getElements(String classname, int limit, Element root) {
 		ArrayList<Element> rtn = elements.get(classname);
 		if ((rtn == null) || (rtn.size() > limit)) {
 			rtn = new ArrayList<Element>();
			ComponentQuery q = ComponentQuery.query("."+classname, root);
 			int count = q.getLength();
 			int found = 0;
 			for (int i = 0; (found < limit) && (i < count); ++i) {
 				Element e = q.getItem(i);
 				if (getParentComponent(e) == root) 
 					rtn.add(e);
 			}
 			elements.put(classname, rtn);
 		}
 		return(rtn);
 	}
 	
 	/** Finds the parent component of an element. */
 	private Element getParentComponent(Element e) {
 		Element rtn = null;
 		Element parent = e.getParentElement();
 		loop: while ((parent != null) && (rtn == null)) {
 			String classname = parent.getClassName();
 			if (classname != null) {
 				if(classname.contains("Component")) {
 					String set[] = classname.split(" ");
 					for (String s : set) {
 						if (s.equals("Component")) {
 							rtn = parent;
 							break loop;
 						}
 					}
 				}
 			}
 			parent = parent.getParentElement();
 		}
 		return(rtn);
 	}
 }
