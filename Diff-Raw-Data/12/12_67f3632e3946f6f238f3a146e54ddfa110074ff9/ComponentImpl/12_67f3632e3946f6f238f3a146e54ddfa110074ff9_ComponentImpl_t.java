 package fr.imag.adele.apam.impl;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.osgi.framework.Filter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.ApamManagers;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.apform.ApformComponent;
 import fr.imag.adele.apam.core.ComponentDeclaration;
 import fr.imag.adele.apam.core.PropertyDefinition;
 import fr.imag.adele.apam.util.ApamFilter;
 import fr.imag.adele.apam.util.Util;
 
 public abstract class ComponentImpl extends ConcurrentHashMap<String, Object> implements Component, Comparable<Component> {
 
 	private final Object  componentId				= new Object();                 // only for hashCode
 	private static final long serialVersionUID 		= 1L;
 	
 	protected static Logger logger = LoggerFactory.getLogger(ComponentImpl.class);
 
 	private ApformComponent      apform ;
 	private ComponentDeclaration declaration;
 
 	public ComponentImpl(ApformComponent apform, Map<String, Object> configuration) {
 		assert apform != null;
 		
 		setApform(apform);
 		if (configuration != null) putAll (configuration);
 	}
 
 	/**
 	 * This methods adds a newly created component to the Apam state model, so that it is visible to the
 	 * external API
 	 */
 	public abstract void register();
 	
 	
 	/**
 	 * This method removes a component from the Apam state model, it must ensure that the component is
 	 * no longer referenced by any other component or visible by the external API
 	 */
 	public abstract void unregister();
 	
 	/**
 	 * Components are uniquely represented in the Apam state model, so we use reference equality
 	 * in all comparisons.
 	 */
 	@Override
 	public final boolean equals(Object o) {
 		return (this == o);
 	}
 
 	@Override
 	public int compareTo(Component that) {  
 		return this.getName().toLowerCase().compareTo(that.getName().toLowerCase());
 	}
 	
 	/**
 	 * Override to make hash code conform to the equality definition
 	 */
 	@Override
 	public final int hashCode() {
 		return componentId.hashCode();
 	}
 
 	public final String getName () {
 		return declaration.getName() ;
 	}
 
     @Override
     public String toString() {
         return getName();
     }
 
 	public final ApformComponent getApformComponent () {
 		return apform ;
 	}
 
 	public final void setApform(ApformComponent apform) {
		this.apform = apform;
		if (apform == null)
			return;
		
 		this.declaration 	= apform.getDeclaration() ;
 		putAll(apform.getDeclaration().getProperties());
 	}
     
 	public final ComponentDeclaration getDeclaration () {
 		return declaration ;
 	}
 
 	/**
 	 * Get the value of the property.
 	 * 
 	 * IMPORTANT this method must be overridden in different subclasses to implement group
 	 * attribute propagation.
 	 * 
 	 */
 	@Override
 	public Object getProperty(String attr) {
 		return get(attr);
 	}
 
 	/**
 	 * Get the value of all the properties in the component.
 	 *  
 	 * IMPORTANT this method must be overridden in different subclasses to implement group
 	 * attribute propagation.
 	 */
 	
 	@Override
 	public Map<String, Object> getAllProperties() {
 		return new HashMap<String, Object>(this);
 	}
 
 	/**
 	 * Set the value of the property in the Apam state model. Changing an attribute notifies
 	 * the property manager and the underlying execution platform (apform).
 	 */
 	@Override
 	public boolean setProperty(String attr, Object value) {
 		/*
 		 * Validate property can be set
 		 */
 		PropertyDefinition propDef = Util.getAttrDefinition(this, attr) ;
 		if (propDef != null && propDef.isInternal() == true) {
 			logger.error("In " + this + " attribute " + attr +  " is internal and cannot be set.");
 			return false;
 		}
 		
 		/*
 		 * Validate that the property is defined and the value valid 
 		 */
 		if (! Util.validAttr(this, attr, value))
 			return false;
 		
 		/*
 		 * Change internal value and notify managers
 		 */
 		setInternalProperty(attr,value);
 		
 		/*
 		 * Notify the execution platform
 		 */
 		getApformComponent().setProperty (attr,value);
 		return true ;
 	}
 
 	/**
 	 * Sets the value of a property changed in the state and notifies property managers,
 	 * but doesn't call back the execution platform.
 	 * 
 	 * TODO,IMPORTANT This method should be private, but it is actually directly invoked by 
 	 * apform to avoid notification loops. We need to refactor the different APIs of Apam.  
 	 */
 	public void setInternalProperty(String attr, Object value) {
 
 		/*
 		 * set value
 		 */
 		Object oldValue = get(attr);
 		put(attr, value);
 		
 		/*
 		 * notify property managers
 		 */
 		if (oldValue == null)
 			ApamManagers.notifyAttributeAdded(this, attr, value) ;
 		else
 			ApamManagers.notifyAttributeChanged(this, attr, value, oldValue);
 	}
 
 	/**
 	 * Sets all the values of the specified properties
 	 * 
 	 * TODO Should we validate all attributes before actually modifying
 	 * the value to avoid partial modifications ?
 	 */
 	@Override
 	public boolean setAllProperties(Map<String, Object> properties) {
 		for (String attr : properties.keySet()) {
 			Object value = properties.get(attr);
 			if (! setProperty(attr, value))
 				return false;
 		}
 		
 		return true ;
 	}
 	
 
 	/**
 	 * Removes the specifed property
 	 * 
 	 * TODO Should we add this to the API? how to notify apform?
 	 */
 	public boolean removeProperty(String attr) {
 		PropertyDefinition propDef = Util.getAttrDefinition(this, attr) ;
 		if (propDef != null && propDef.getField() != null) {
 			logger.error("In " + this + " attribute " + attr +  " is a program field and cannot be removed.");
 			return false;
 		}
 		
 		Object oldValue = get(attr) ;
 		if (oldValue != null) {
 			remove(attr);
 			ApamManagers.notifyAttributeRemoved(this, attr, oldValue);
 			return true ;
 		}
 		return false ;	
 	}
 
 	/*
 	 * Filter evaluation on the properties of this component
 	 */
 	
 	@Override
 	public boolean match(String goal) {
 		return goal == null || match(ApamFilter.newInstance(goal));
 	}
 
 	@Override
 	public boolean match(Filter goal) {
 		return goal == null || match(Collections.singleton(goal));
 	}
 
 	@Override
 	public boolean match(Set<Filter> goals) {
 		if ((goals == null) || goals.isEmpty())
 			return true;
 		
 		Map<String,Object> props = getAllProperties() ;
 		try {
 			for (Filter f : goals) {
 				if (!((ApamFilter) f).matchCase(props)) {
 					return false ;
 				}
 			}
 			return true;
 		} catch (Exception e) {
 			return false ;
 		}
 	}
 	
 }
 
