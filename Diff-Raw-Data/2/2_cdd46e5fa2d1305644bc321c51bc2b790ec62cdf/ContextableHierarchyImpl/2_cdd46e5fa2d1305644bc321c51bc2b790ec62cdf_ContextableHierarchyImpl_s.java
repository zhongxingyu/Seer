 package org.sakaiproject.hierarchy.impl;
 
 import java.util.AbstractMap;
 import java.util.AbstractSet;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.sakaiproject.hierarchy.api.model.Hierarchy;
 import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
 
 /**
  * Wrapper for a Hierarchy node which gives the node a context.
  * @author buckett
  *
  */
 public class ContextableHierarchyImpl implements Hierarchy {
 
 	private Hierarchy delegate;
 	private String context;
 	
 	public ContextableHierarchyImpl(Hierarchy delegate, String context) {
 		if (delegate == null)
 			throw new IllegalArgumentException("Delegate node can't be null.");
 		if (context != null && !delegate.getPath().startsWith(context))
 			throw new IllegalArgumentException("Context must match the start of the path.");
 		this.delegate = delegate;
 		this.context = context;
 	}
 	
 	public void addTochildren(Hierarchy hierarchy) {
 		if (hierarchy instanceof ContextableHierarchyImpl) {
 			ContextableHierarchyImpl childImpl = (ContextableHierarchyImpl) hierarchy;
 			delegate.addTochildren(childImpl.getDelegate());
 		} else {
 			throw new IllegalArgumentException("Parameter must be obtained from same service.");
 		}
 	}
 
 	public void addToproperties(HierarchyProperty hierarchyProperty) {
 		delegate.addToproperties(hierarchyProperty);
 	}
 
 	public HierarchyProperty addToproperties(String string, String value) {
 		return delegate.addToproperties(string, value);
 	}
 
 	public Hierarchy getChild(String childPath) {
 		return new ContextableHierarchyImpl(delegate.getChild(context+childPath), context);
 	}
 
 	public Map getChildren() {
 		// Not very efficient as every time this is called we return a new Map but it is easier than
 		// keeping a wrapping map.
 		return new AbstractMap() {
 
 			public Set entrySet() {
 				return new AbstractSet() {
 
 					public Iterator iterator() {
 						return new Iterator() {
 							
 							Iterator entries = delegate.getChildren().entrySet().iterator();
 							Entry next;
 							
 							private void loadNext() {
 								if (next == null && entries.hasNext()) {
 									final Entry entry = (Entry)entries.next();
 									if (entry != null) {
 										next = new Entry () {
 
 											public Object getKey() {
 												return entry.getKey();
 											}
 
 											public Object getValue() {
 												return new ContextableHierarchyImpl((Hierarchy)entry.getValue(), context);
 											}
 
 											public Object setValue(Object value) {
 												return entry.setValue(value);
 											}
 										};
 									}
 								}
 							}
 							
 							public boolean hasNext() {
 								loadNext();
 								return next != null;
 							}
 
 							public Object next() {
 								loadNext();
 								Object toReturn = next;
 								next = null;
 								return toReturn;
 							}
 
 							public void remove() {
 								throw new UnsupportedOperationException();
 							}
 							
 						};
 					}
 
 					public int size() {
 						return delegate.getChildren().size();
 					}
 					
 				};
 			}
 			
 		};
 	}
 
 	public String getId() {
 		return delegate.getId();
 	}
 
 	public Hierarchy getParent() {
 		Hierarchy parent = delegate.getParent();
 		// Don't allow caller to escape the context.
		if (parent == null || parent != null && context.equals(parent.getPath())) {
 			return null;
 		}
 		return new ContextableHierarchyImpl(parent, context);
 	}
 
 	public String getPath() {
 		if (delegate.getPath().equals(context)) {
 			return "/";
 		}
 		return delegate.getPath().substring(context.length());
 	}
 
 	public String getPathHash() {
 		return delegate.getPathHash();
 	}
 
 	public Map getProperties() {
 		return delegate.getProperties();
 	}
 
 	public HierarchyProperty getProperty(String string) {
 		return delegate.getProperty(string);
 	}
 
 	public String getRealm() {
 		return delegate.getRealm();
 	}
 
 	public Date getVersion() {
 		return delegate.getVersion();
 	}
 	
 	public Hierarchy getDelegate() {
 		return delegate;
 	}
 
 	public boolean isModified() {
 		return delegate.isModified();
 	}
 
 	public void setChildren(Map children) {
 		// Unlike the default implementation this one copies everything out of the map.
 		Iterator childrenIt = children.entrySet().iterator();
 		Map delegateMap = new HashMap();
 		while (childrenIt.hasNext()) {
 			Entry entry = (Entry)childrenIt.next();
 			if (entry.getValue() instanceof ContextableHierarchyImpl) {
 				delegateMap.put(entry.getKey(), ((ContextableHierarchyImpl) entry.getValue()).getDelegate());
 			} else {
 				throw new IllegalArgumentException("Map can only contain children obtained from this service.");
 			}
 			
 		}
 		delegate.setChildren(children);
 	}
 
 	public void setId(String string) {
 		delegate.setId(string);
 	}
 
 	public void setModified(boolean b) {
 		delegate.setModified(b);
 	}
 
 	public void setParent(Hierarchy parent) {
 		if (parent instanceof ContextableHierarchyImpl) {
 			ContextableHierarchyImpl parentImpl = (ContextableHierarchyImpl) parent;
 			delegate.setParent(parentImpl.getDelegate());
 		} else {
 			throw new IllegalArgumentException("Parameter must be obtained from same service.");
 		}
 
 	}
 
 	public void setProperties(Map properties) {
 		delegate.setProperties(properties);
 	}
 
 	public void setRealm(String realm) {
 		delegate.setRealm(realm);
 	}
 
 	public void setVersion(Date date) {
 		delegate.setVersion(date);
 	}
 
 
 }
