 package fr.imag.adele.apam.impl;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.osgi.framework.Filter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.ApamManagers;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.apform.ApformComponent;
 import fr.imag.adele.apam.core.ComponentDeclaration;
 import fr.imag.adele.apam.core.PropertyDefinition;
 import fr.imag.adele.apam.util.ApamFilter;
 import fr.imag.adele.apam.util.Util;
 //import fr.imag.adele.apam.util.ApamFilter;
 
 public abstract class ComponentImpl extends ConcurrentHashMap<String, Object> implements Component, Comparable<Component> {
 
 	private final Object  componentId				= new Object();                 // only for hashCode
 	private static final long serialVersionUID 		= 1L;
 
 	protected static Logger logger = LoggerFactory.getLogger(ComponentImpl.class);
 
 	private final ApformComponent      apform ;
 	private final ComponentDeclaration declaration;
 
 	//Contains the composite type that was the first to physically deploy that component.
 	private CompositeType firstDeployed ;
 
 	/**
 	 * An exception that can be thrown in the case of problems while creating a component
 	 */
 	public static class InvalidConfiguration extends Exception {
 
 		private static final long serialVersionUID = 1L;
 
 		public InvalidConfiguration(String message) {
 			super(message);
 		}
 
 		public InvalidConfiguration(String message, Throwable cause) {
 			super(message,cause);
 		}
 
 		public InvalidConfiguration( Throwable cause) {
 			super(cause);
 		}
 
 	}
 
 	public ComponentImpl(ApformComponent apform) throws InvalidConfiguration {
 
 		if (apform == null)
 			throw new InvalidConfiguration("Null apform instance while creating component");
 
 		//assert apform != null;
 
 		this.apform 		= apform;
 		this.declaration	= apform.getDeclaration();
 
 	}
 
 	/**
 	 * to be called once the Apam entity is fully initialized.
 	 * Computes all its attributes, including inheritance. 
 	 */
 	public void initializeProperties (Map<String, String> initialProperties) {
 		/*
 		 * get the initial attributes from declaration and overriden initial
 		 * properties
 		 */
 		Map<String, String> props = new HashMap<String, String> (getDeclaration().getProperties()) ;
 		if (initialProperties != null)
 			props.putAll(initialProperties);
 
 		ComponentImpl group = (ComponentImpl)getGroup () ;
 
 		//First eliminate the attributes which are not valid.
 		for (String attr : props.keySet()) {
 			if (Util.validAttr(this.getName(), attr)) {
 				//At initialization, all valid attributes are ok for specs
 				if (group == null || validDef (attr, props.get(attr), true) != null)
 					put (attr, props.get(attr)) ;
 			}
 		}
 
 		//then add those coming from its group, avoiding overloads.
 		if (group != null) {
 			for (String attr : group.getAllProperties().keySet()) {
 				if (get(attr) == null) 
 					put (attr, ((ComponentImpl)group).get(attr)) ;
 			}
 		} 
 
 		/*
 		 * Add the default values specified in the group for properties not
 		 * explicitly specified
 		 */
 		if (group != null) {
 			for (PropertyDefinition definition : group.getDeclaration().getPropertyDefinitions()) {
 				if ( definition.getDefaultValue() != null && get(definition.getName()) == null) 
 					put (definition.getName(),definition.getDefaultValue()) ;
 			}
 		} 
 
 		/*
 		 * Set the attribute for the final attributes 
 		 */
 		put (CST.SHARED, Boolean.toString(isShared())) ;
 		put (CST.SINGLETON, Boolean.toString(isSingleton())) ;
 		put (CST.INSTANTIABLE, Boolean.toString(isInstantiable())) ;
 
 		/*
 		 * Finally add the specific attributes. Should be the only place where instanceof is used.
 		 */
 		put (CST.NAME, apform.getDeclaration().getName()) ;
 		if (this instanceof Specification) {
 			put (CST.SPECNAME, apform.getDeclaration().getName()) ;
 		} else {
 			if (this instanceof Implementation) {
 				put (CST.IMPLNAME, apform.getDeclaration().getName()) ;	
 				if (this instanceof CompositeType) {
 					put(CST.APAM_COMPOSITETYPE, CST.V_TRUE);
 				}
 				if (this instanceof Composite) {
 					put(CST.APAM_COMPOSITE, CST.V_TRUE);
 					put(CST.APAM_MAIN_INSTANCE, ((Composite)this).getMainInst().getName());
 				}
 			} else  {
 				if (this instanceof Instance) {
 					put (CST.INSTNAME, apform.getDeclaration().getName()) ;
 					//put (CST.A_COMPOSITE, ((Instance)this).getComposite().getName());
 				}
 			}
 		}
 
 		//and propagate, to the platform and to members, in case the spec has been created after the implem
 		for (String attr : getAllProperties().keySet()) {
 			propagateInit (attr, get(attr)) ;
 		}
 	}
 
 	/**
 	 * This methods adds a newly created component to the Apam state model, so that it is visible to the
 	 * external API
 	 */
 	public abstract void register(Map<String, String> initialProperties) throws InvalidConfiguration;
 
 
 	/**
 	 * This method removes a component from the Apam state model, it must ensure that the component is
 	 * no longer referenced by any other component or visible by the external API.
 	 * Should be called ONLY from the Broker.
 	 */
 	abstract void unregister();
 
 	/**
 	 * Components are uniquely represented in the Apam state model, so we use reference equality
 	 * in all comparisons.
 	 */
 	@Override
 	public final boolean equals(Object o) {
 		return (this == o);
 	}
 
 	/**
 	 * TODO Assumes that all components are in the same name space, including instance !!
 	 */
 	@Override
 	public int compareTo(Component that) {  
 		return this.getName().compareTo(that.getName());
 	}
 
 	/**
 	 * Override to make hash code conform to the equality definition
 	 */
 	@Override
 	public final int hashCode() {
 		return componentId.hashCode();
 	}
 
 	@Override
 	public final String getName () {
 		return declaration.getName() ;
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 	@Override
 	public final ApformComponent getApformComponent () {
 		return apform ;
 	}
 
 	@Override
 	public final ComponentDeclaration getDeclaration () {
 		return declaration ;
 	}
 
 	/**
 	 * Get the value of the property.
 	 * 
 	 * Attributes are supposed to be correct and inherited staticaly
 	 * 
 	 */
 	@Override
 	public String getProperty(String attr) {
 		Object val = get(attr) ;
 		return (val == null) ? null : val.toString();
 	}
 
 	/**
 	 * Get the value of all the properties in the component.
 	 *  
 	 */
 
 	@Override
 	public Map<String, Object> getAllProperties() {
 		return Collections.unmodifiableMap(this);
 	}
 
 	/**
 	 * Set the value of the property in the Apam state model. Changing an attribute notifies
 	 * the property manager, the underlying execution platform (apform), and propagates to members
 	 */
 	@Override
 	public boolean setProperty(String attr, String value) {
 		return setPropertyInt(attr, value, false);
 	}
 
 	/**
 	 * Warning: to be used only by Apform for setting internal attributes.
 	 * Only Inhibits the message "Attribute " + attr +  " is an internal field attribute and cannot be set.");
 	 * @param attr
 	 * @param value
 	 * @param forced
 	 * @return
 	 */
 	public boolean setPropertyInt(String attr, String value, boolean forced) {
 
 		/*
 		 * Validate that the property is defined and the value is valid 
 		 */
 		if (!Util.validAttr(this.getName(), attr))
 			return false;
 
 		Object val = validDef (attr, value, forced) ;
 		if (val == null)
 			return false ;
 
 		//does the change, notifies, changes the platform and propagate to members
 		this.propagate (attr, val) ;
 		return true ;
 	}
 
 	/**
 	 * During initialisation, set the new (attrbute, value) in the object, 
 	 * in the platform, and propagates to the members recursively.
 	 * Does not notify managers.
 	 * @param com the component to which is added the attribute.
 	 * @param attr
 	 * @param value
 	 */
 	private void propagateInit (String attr, Object value) {
 		//Notify the execution platform
 		getApformComponent().setProperty (attr, value.toString());
 
 		//Propagate to members recursively
 		for (Component member : getMembers()) {
 			((ComponentImpl)member).propagate (attr, value) ;
 		}
 	}
 
 
 	/**
 	 * set the value, update apform and the platform, notify managers and propagates to the members, recursively
 	 * @param com the component on which ot set the attribute
 	 * @param attr attribute name
 	 * @param value attribute value
 	 */
 
 	private void propagate (String attr, Object value) {
 		//Change value and notify managers
 		setInternalProperty(attr,value);
 
 		//Notify the execution platform
 		getApformComponent().setProperty (attr,value.toString());
 
 		//Propagate to members recursively
 		for (Component member : getMembers()) {
 			((ComponentImpl)member).propagate (attr, value) ;
 		}
 	}
 
 	/**
 	 * Sets the value of a property changed in the state and notifies property managers,
 	 * but doesn't call back the execution platform.
 	 * 
 	 * TODO,IMPORTANT This method should be private, but it is actually directly invoked by 
 	 * apform to avoid notification loops. We need to refactor the different APIs of Apam.  
 	 */
 	public void setInternalProperty(String attr, Object value) {
 		Object oldValue = get(attr);
 		put(attr, value);
 		/*
 		 * notify property managers
 		 */
 		if (oldValue == null)
 			ApamManagers.notifyAttributeAdded(this, attr, value.toString()) ;
 		else
 			ApamManagers.notifyAttributeChanged(this, attr, value.toString(), oldValue.toString());
 	}
 
 	/**
 	 * Sets all the values of the specified properties
 	 * 
 	 * We validate all attributes before actually modifying
 	 * the value to avoid partial modifications ?
 	 */
 	@Override
 	public boolean setAllProperties(Map<String, String> properties) {
 		for (String attr : properties.keySet()) {
 			String value = properties.get(attr);
 			if (! setProperty(attr, value))
 				return false;
 		}
 
 		return true ;
 	}
 
 
 	/**
 	 * Removes the specifed property
 	 * 
 	 */
 	@Override
 	public boolean removeProperty(String attr) {
 
 		String oldValue = getProperty(attr) ;
 
 		if (oldValue == null) {
 			logger.error("ERROR: \"" + attr + "\" not instanciated");
 			return false;			
 		}
 
 		if (Util.isFinalAttribute(attr)) {
 			logger.error("ERROR: \"" + attr + "\" is a final attribute");
 			return false;
 		}
 
 		if (Util.isReservedAttributePrefix(attr)) {
 			logger.error("ERROR: \"" + attr + "\" is a reserved attribute");
 			return false;
 		}
 
 		PropertyDefinition propDef = getAttrDefinition(attr) ;
 		if (propDef != null && propDef.getField() != null) {
 			logger.error("In " + this + " attribute " + attr +  " is a program field and cannot be removed.");
 			return false;
 		}
 
 		if (getGroup() != null && getGroup().getProperty(attr) != null) {
 			logger.error("In " + this + " attribute " + attr +  " inherited and cannot be removed.");
 			return false;			
 		}
 
 		//it is ok, remove it and propagate to members, recursively
 		propagateRemove(attr) ;
 
 		//TODO. Should we notify at all levels ?
 		ApamManagers.notifyAttributeRemoved(this, attr, oldValue);
 
 		return true ;	
 	}
 
 	/**
 	 * TODO. Should we notify at all levels ?
 	 * @param ent
 	 * @param attr
 	 */
 	private void propagateRemove (String attr) {
 
 		remove(attr) ;
 		for (Component member : getMembers ()) {
 			((ComponentImpl)member).propagateRemove(attr) ;
 		}
 	}
 
 	/**
 	 * Tries to find the definition of attribute "attr" associated with component "component".
 	 * Returns null if the attribute is not explicitly defined 
 	 * @param component
 	 * @param attr
 	 * @return
 	 */
 	private PropertyDefinition getAttrDefinition (String attr) {
 		Component group = this.getGroup() ;
 		if (group == null) 
 			return getDeclaration().getPropertyDefinition(attr);
 
 		while (group != null) {
 			PropertyDefinition definition =  group.getDeclaration().getPropertyDefinition(attr);
 			if (definition != null)
 				return definition;
 
 			group = group.getGroup () ;
 		}
 
 		return null ;
 	}
 
 
 	/**
 	 * An attribute is valid if declared in an ancestor, and not set in an ancestor.
 	 * Check if the value is conforming to its type (string, int, boolean).
 	 * Internal attribute (associated with a field) cannot be set.
 	 * Must be called on the level above.
 	 * 
 	 * Checks if attr is correctly defined for component ent
 	 * 	it must be explicitly defined in its upper groups,
 	 *  for the top group, it must be already existing. 
 	 *  
 	 *  boolean Forced = true can be set only by Apform. 
 	 *  Needed when an internal attribute is set in the program, Apform propagates the value to the attribute.
 	 *  
 	 * @param attr
 	 * @param value
 	 * @return
 	 */
 	private Object validDef (String attr, String value, boolean forced) {
 		if (Util.isFinalAttribute(attr)) {
 			logger.error("Cannot redefine final attribute \"" + attr + "\"");
 			return null;			
 		}		
 
 		Component group = this.getGroup() ;
 
 		//if the same attribute exists above, it is a redefinition.
 		if (group != null && group.getProperty(attr) != null) {
 			logger.error("Cannot redefine attribute \"" + attr + "\"");
 			return null;
 		}
 
 		PropertyDefinition definition = this.getAttrDefinition (attr) ;
 
 		if (definition == null) {
 			logger.error("Attribute \"" + attr + "=" + value + "\" is undefined.");
 			return null;
 		}
 
 		// there is a definition for attr
 		if (definition.isInternal() && !forced) {
 			logger.error("Attribute " + attr +  " is an internal field attribute and cannot be set.");
 			return null;
 		}
 
 		return Util.checkAttrType(attr, value, definition.getType());
 	}
 
 	@Override
 	public <T extends Component> Set<T> getSelectedComponents(Set<T> candidates, Set<Filter> constraints) {
 		if (constraints == null) return candidates;
 		if (candidates == null) return null ;
 
 		Set<T> ret = new HashSet <T> () ;
 		for (T inst : candidates) {
 			if (inst.match(constraints)) {
 				ret.add (inst) ;
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public <T extends Component> T getSelectedComponent(Set<T> candidates, Set<Filter> constraints) {
 		Set<T> ret = getSelectedComponents(candidates, constraints) ;
 		if (ret.isEmpty()) return null ;
 		return ret.iterator().next() ;
 	}
 
 
 	/**
 	 * Returns the candidate that best matches the preferences.
 	 * Take the preferences in orden: m candidates
 	 * find  the n candidates that match the constraint.
 	 * 		if n= 0 ignore the constraint
 	 *      if n=1 return it.
 	 * iterate with the n candidates.
 	 * At the end, if n > 1 return hte default one.
 	 * 
 	 */
 	@Override
 	public <T extends Component> T getPreferedComponent(Set<T> candidates, List<Filter> preferences, Set<Filter> memberConstraints) {
 		if (candidates == null || candidates.isEmpty()) return null ;
 
 		//Select only those implem with instances matching the constraints		
 		if (memberConstraints != null && !memberConstraints.isEmpty()) {
 			Set<T> withInsts = getPreferedComponent (candidates, memberConstraints) ;
 			String constraints = "" ;
 			for (Filter constraint : memberConstraints) {
 				constraints += (constraint + ", ");
 			}
 			String mess = "\nLooking for implems with instances matching " + constraints ;
 			constraints = "\n     Candidates: " ;
 			for (T t : candidates) {
 				constraints += t.getName() + ", " ;
 			}
 
 			if (!withInsts.isEmpty()) {
 				String trace = "";
 				for (T t : withInsts) 
 					trace += t.getName() + " " ;
 				logger.debug (mess + constraints + "\n     Implems matching instance constraints : " + trace) ;
 				candidates = withInsts ;
 			} else {
 				logger.debug (mess + constraints + "\n     No Implem is matching the instance constraints.") ;
 			}
 		}
 
 		if ((preferences == null) || preferences.isEmpty()) {
 			return getDefaultComponent(candidates);
 		}
 
 		/*
 		 * select the prefered one
 		 */
 		Set<T> valids = new HashSet<T> ();
 		for (Filter f : preferences) {
 			for (T compo : candidates) {
 				if (compo.match(f)) 
 					valids.add (compo) ;
 			}
 			if (valids.size()==1) return valids.iterator().next();
 			if (!valids.isEmpty()) {
 				candidates = valids ;
 				valids=new HashSet<T> () ;
 			}
 		}
 		return getDefaultComponent(candidates) ;
 	}
 
 	/*
 	 * Look for the candidates that has at least a member matching the memberConstraints.
 	 * WARNING: do not check visibility constraints
 	 */
 	private <T extends Component> Set<T> getPreferedComponent(Set<T> candidates, Set<Filter> memberConstraints) {
 		Set<T> valids = new HashSet<T> ();
 
 		for (T compo : candidates) {
			Set<Component> members = (Set<Component>) compo.getMembers() ;
 			//System.err.println("members de " + compo + " : " + members);
 			if (members == null || members.isEmpty()) break ; 
 			for (Component member : members) {
 				if (member.match(memberConstraints)) {
 					valids.add (compo) ;
 					break ;
 				}
 			}
 		}
 		return valids ;
 	}
 
 
 	@Override
 	public <T extends Component> T getDefaultComponent (Set<T> candidates) {
 		for (T impl : candidates) {
 			if (! (impl instanceof Implementation)) return impl ;
 			for (Component inst : impl.getMembers()) {
 				if (((Instance)inst).isSharable())
 					return impl;
 			}
 		}
 		for (T impl : candidates) {
 			if (impl.isInstantiable())
 				return impl;
 		}
 		return candidates.iterator().next();
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
 
 
 
 	/**
 	 * Whether the component is instantiable
 	 */
 	@Override
 	public boolean isInstantiable() {
 		if (declaration.isDefinedInstantiable() || getGroup() == null) 
 			return declaration.isInstantiable() ;
 		return getGroup().isInstantiable() ;
 	}
 
 	/**
 	 * Whether the component is singleton
 	 */
 	@Override
 	public boolean isSingleton(){
 		if (declaration.isDefinedSingleton() || getGroup() == null) 
 			return declaration.isSingleton() ;
 		return getGroup().isSingleton() ;
 	}
 
 	/**
 	 * Whether the component is shared
 	 */
 	@Override
 	public boolean isShared() {
 		if (declaration.isDefinedShared() || getGroup() == null) 
 			return declaration.isShared() ;
 		return getGroup().isShared() ;
 	}
 
 	@Override
 	public CompositeType getFirstDeployed () {
 		return firstDeployed == null ? CompositeTypeImpl.getRootCompositeType() : firstDeployed ;
 	}
 
 	public void setFirstDeployed (CompositeType father) {
 		firstDeployed = father ;
 	}
 
 	@Override
 	public Map<String, String> getValidAttributes () {
 		Map<String, String> ret = new HashMap <String, String> () ;
 		for (PropertyDefinition def: declaration.getPropertyDefinitions()) {
 			ret.put(def.getName(), def.getType());
 		}
 		if (getGroup() != null) {
 			ret.putAll (getGroup().getValidAttributes()) ;
 		}
 		return ret ;
 	}
 
 
 }
 
