 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.impl;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
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
 import fr.imag.adele.apam.declarations.ComponentDeclaration;
 import fr.imag.adele.apam.declarations.DependencyDeclaration;
 import fr.imag.adele.apam.declarations.PropertyDefinition;
 import fr.imag.adele.apam.declarations.ResourceReference;
 import fr.imag.adele.apam.util.ApamFilter;
 import fr.imag.adele.apam.util.Util;
 
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
 
 		this.apform 		= apform;
 		this.declaration	= apform.getDeclaration();
 
 	}
 
 
 /**
  * To be called when the object is fully loaded and chained.
  * Terminate the generic initialization : computing the dependencies, and the properties
  * @param initialProperties
  */
 	public void finishInitialize (Map<String, String> initialProperties) {
 		computeGroupDependency () ;
 		initializeProperties (initialProperties) ;
 	}
 	
 	/**
 	 * Provided a dependency declaration, compute the effective dependency, adding group constraint and flags.
 	 * Compute which is the good target, and check the targets are compatible. 
 	 * If needed changes the target to set the more general one.
 	 * It is supposed to be correct !! No failure expected
 	 * @param depComponent
 	 * @param dependency
 	 * @return
 	 */
 	private void computeGroupDependency () {
 		for (DependencyDeclaration dependency : this.getDeclaration().getDependencies() ) {
 			Component group = getGroup() ;
 			//look for that dependency declaration above
 			DependencyDeclaration groupDep = null ;
 			while (group != null && (groupDep == null)) {
 				groupDep = group.getDeclaration().getDependency(dependency.getIdentifier()) ;
				group = getGroup() ;
 			}
 
 			if (groupDep == null) {
 				//It is not defined above. Do not change it.
 				continue ;
 			}
 
 			//it is declared above. Merge and check.
 			//First merge flags, and then constraints.
 			Util.overrideDepFlags (dependency, groupDep, false);
 			dependency.getImplementationConstraints().addAll(groupDep.getImplementationConstraints()) ;
 			dependency.getInstanceConstraints().addAll(groupDep.getInstanceConstraints()) ;
 			dependency.getImplementationPreferences().addAll(groupDep.getImplementationPreferences()) ;
 			dependency.getInstancePreferences().addAll(groupDep.getInstancePreferences()) ;		
 
 			//set the target
 			dependency.setTarget(groupDep.getTarget()) ;
 		}
 	}
 
 	/**
 	 * to be called once the Apam entity is fully initialized.
 	 * Computes all its attributes, including inheritance.
 	 */
 	private void initializeProperties (Map<String, String> initialProperties) {
 		/*
 		 * get the initial attributes from declaration and overriden initial
 		 * properties
 		 */
 		Map<String, String> props = new HashMap<String, String> (getDeclaration().getProperties()) ;
 		if (initialProperties != null)
 			props.putAll(initialProperties);
 
 		ComponentImpl group = (ComponentImpl)getGroup () ;
 
 		//First eliminate the attributes which are not valid.
 		for ( Map.Entry<String,String> entry : props.entrySet()) {
 			PropertyDefinition def = validDef (entry.getKey(), true) ;
 			if (def != null) {
 				//At initialization, all valid attributes are ok for specs
 				Object val = Util.checkAttrType(entry.getKey(), entry.getValue(), def.getType());
 				if (val != null) {
 					put (entry.getKey(), val) ;
 				}
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
 				if ( definition.getDefaultValue() != null && get(definition.getName()) == null) {
 					Object val = Util.checkAttrType(definition.getName(), definition.getDefaultValue(), definition.getType());
 					if (val != null)
 						put (definition.getName(),val) ;
 				}
 
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
 	 * Attributes are supposed to be correct and inherited statically
 	 *
 	 */
 	@Override
 	public String getProperty(String attr) {
 		Object val = get(attr) ;
 		return (val == null) ? null : val.toString();
 	}
 
 
 	/**
 	 * Get the value of a property, the property can be valued in this component or in its
 	 * defining group
 	 * Return will be an object of type int, String, boolean for attributes declared int, String, boolean
 	 * 					String for an enumeration.
 	 * 
 	 * For sets, the return will be an array of the corresponding types. i.e; int[], String[] and so on.
 	 * Returns null if attribute is not defined or not set.
 	 */
 	@Override
 	public Object getPropertyObject (String attribute) {
 		PropertyDefinition def = getAttrDefinition(attribute) ; 
 		if (def == null) {
 			logger.error("No definition for attribute " + attribute) ; 
 			return null ;
 		}
 
 		String type = def.getType() ; 
 		if (type == null) return null ;
 		Object val = get(attribute) ;
 		if (val == null) return null ;
 
 		boolean isSet = false ;
 		if (type.charAt(0)=='{' ) {
 			isSet = true ;
 			type = type.substring(1, type.length()-1) ;	
 		}
 
 		if (!isSet) {
 			if (type.equals ("int")) {
 				return val ;
 			}
 			if (type.equals("boolean")) {
 				return (val.equals("true") || val.equals("TRUE")) ;
 			}
 			//string and enumeration are strings
 			return val ;
 		}
 
 		//it is a set. Internaly it is a string. Returns an array of int of an array of strings
 		String[] enumVals = Util.split(type);
 		int l = enumVals.length ;
 
 		// If a set of integer, build the array
 		if (l==1 && type.equals ("int")) {
 			int[] intReturn = new int[l] ;
 			for (int i = 0 ; i < l; i++) {
 				intReturn[i] = Integer.parseInt(enumVals[i]) ;
 			}
 			return intReturn ;
 		}
 		if (l==1 && type.equals("boolean")) {
 			logger.error("Set of integers are not allowed" ) ;
 			return null ;
 		}
 
 		//value is a set of String
 		return enumVals;
 	}
 
 	/**
 	 * Warning: to be used only by Apform for setting internal attributes.
 	 * Only Inhibits the message "Attribute " + attr +  " is an internal field attribute and cannot be set.");
 	 * @param attr
 	 * @param value
 	 * @param forced
 	 * @return
 	 */
 	public boolean setPropertyInt(String attribute, Object value, boolean forced) {
 		if ( ! setPropertyObjectInternal (attribute, value, forced)) {
 			return false ;
 		}
 		//does the change, notifies, changes the platform and propagate to members
 		this.propagate (attribute, value) ;
 		return true ;
 	}
 
 	@Override
 	public boolean setPropertyObject (String attribute, Object value) {
 		return setPropertyObjectInternal (attribute, value, false) ;
 	}
 
 	/**
 	 * Set the value of a property, the property can be valued in this component or in its
 	 * defining group
 	 * Value must be an int, String, boolean or string for attributes declared int, String, boolean
 	 * 					String for an enumeration.
 	 * 
 	 * For sets, the value must be an array of the corresponding types. i.e; int[], String[] and so on.
 	 * 
 	 * If the attribute does not exist, of it the value does not correspond to the attribute type, "false" is returned.
 	 */
 	private boolean setPropertyObjectInternal (String attribute, Object value, boolean forced) {
 		PropertyDefinition def = validDef (attribute, forced) ;
 		if (def == null) return false ;
 
 		String type = def.getType() ; 
 		if (type == null) return false ;
 
 		boolean isSet = false ;
 		if (type.charAt(0)=='{' ) {
 			isSet = true ;
 			type = type.substring(1, type.length()-1) ;	
 		}
 
 		if (!isSet) {
 			// int are stored as int
 			if (type.equals ("int")) {
 				int val ;
 				//convert from string to int
 				if (value instanceof String)  {
 					try {
 						val = Integer.parseInt((String)value) ;
 					}catch (Exception e) {
 						logger.error("Invalid integer value " + value + " for attribute " + attribute) ;
 						return false ;
 					}
 				}
 				if ( !(value instanceof Integer)) {
 					logger.error("Invalid integer value " + value + " for attribute " + attribute) ;
 					return false ;
 				}
 				val = ((Integer)value).intValue()  	;	
 				put (attribute, val) ;
 				return true ;
 			}
 
 			//Boolean are stored as strings
 			if (type.equals("boolean")) {
 				if (value instanceof String)  {
 					put (attribute, ((String) value).equalsIgnoreCase("true")) ;
 				}
 				if ( !(value instanceof Boolean)) {
 					logger.error("Invalid value: not a Boolean " + value + " for attribute " + attribute) ;
 					return false ;
 				}
 				put (attribute, ((Boolean)value).toString());
 				return true ;
 			}
 
 
 			if (! (value instanceof String)) {
 				logger.error("Invalid value: not a String " + value + " for attribute " + attribute) ;
 				return false ;
 			}
 			put (attribute, value) ;
 			return true ;
 		}
 
 
 		//it is a set. Value must be an array of the right type. Internaly it is a string. 
 		Set<String> enumVals = Util.splitSet(type);
 		int l = enumVals.size() ;
 		String aType = enumVals.iterator().next() ;
 
 		if (l==1 && aType.equals ("int")) {
 			//Value MUST be an array of int
 			if (value instanceof  int[]) {
 				StringBuffer sVal = new StringBuffer () ;
 				int lv = ((int[])value).length;
 				for (int i=0; i < lv ; i++) {
 					sVal.append (Integer.toString(((int[])value)[i]) ); 
 					if (i < lv -1) {
 						sVal.append(", ") ;
 					}
 				}
 				put (attribute, sVal.toString()) ;
 				return true ;
 			}
 			logger.error("Attribute value " + value + " not an int array for attribute " + attribute) ;
 			return false ;
 		}
 
 		if (l==1 && aType.equals("boolean")) {
 			logger.error("Set of integers are not alowed" ) ;
 			return false ;
 		}
 
 
 		//It should be a String array.
 		if (!(value instanceof  String[])) {
 			logger.error("Invalid value: not an array of String " + value + " for attribute " + attribute) ;
 			return false ;
 		}
 
 		//Tranform the string array into a string.
 		StringBuffer sVal = new StringBuffer () ;
 		int lv = ((String[])value).length;
 		for (int i=0; i < lv ; i++) {
 			sVal.append (((String[])value)[i]) ; 
 			if (i < lv -1) {
 				sVal.append(", ") ;
 			}
 		}
 		String setString = sVal.toString() ;
 
 		// A set of string, allway valid
 		if (l==1 && aType.equals("string")) {
 			put (attribute,setString) ;
 			return true ;
 		}
 
 		//It is a set of enumeration. Check if values are valid.
 		if (! (enumVals.containsAll(Util.splitSet(setString)))) {
 			String errorMes = "Invalid attribute value(s) \"" + value + "\" for attribute \"" + attribute
 					+ "\".  Expected subset of: " + type;
 			logger.error(errorMes);
 			return false;
 		}
 
 		put (attribute, setString) ;
 		return true ;		
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
 		/*
 		 * Validate that the property is defined and the value is valid
 		 */
 
 		PropertyDefinition def = validDef (attr, false) ;
 		if (def == null) return false ;
 		//At initialization, all valid attributes are ok for specs
 		Object val = Util.checkAttrType(attr, value, def.getType());
 		if (val == null)
 			return false ;
 
 		//does the change, notifies, changes the platform and propagate to members
 		this.propagate (attr, val) ;
 		return true ;
 		//
 		//		return setPropertyInt(attr, value, false);
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
 
 		//Notify the execution platform
 		if(get(attr)==value)
 			getApformComponent().setProperty (attr,value.toString());
 
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
 		for (Map.Entry<String,String> entry : properties.entrySet()) {
 			if (! setProperty(entry.getKey(), entry.getValue()))
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
 
 		//Check if it is a local definition: <property name="..." type="..." value=".." />
 		PropertyDefinition definition = getDeclaration().getPropertyDefinition(attr);
 		if (definition != null) {
 			if (definition.isLocal()) {
 				return definition ;
 			} else {
 				return null ;
 			}
 		}
 
 		Component group = this.getGroup() ;
 		while (group != null) {
 			definition =  group.getDeclaration().getPropertyDefinition(attr);
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
 	private PropertyDefinition validDef (String attr, boolean forced) {
 		if (Util.isFinalAttribute(attr)) {
 			logger.error("Cannot redefine final attribute \"" + attr + "\"");
 			return null;
 		}
 
 		if (Util.isReservedAttributePrefix(attr)) {
 			logger.error("ERROR: in " + this + ", attribute\"" + attr + "\" is reserved");
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
 			logger.error("Attribute \"" + attr +  "\" is undefined.");
 			return null;
 		}
 
 		// there is a definition for attr
 		if (definition.isInternal() && !forced) {
 			logger.error("Attribute " + attr +  " is an internal field attribute and cannot be set.");
 			return null;
 		}
 
 		return definition ;
 		//return Util.checkAttrType(attr, value, definition.getType());
 	}
 
 
 
 	/*
 	 * Filter evaluation on the properties of this component
 	 */
 
 	@Override
 	public boolean match(String goal) {
 		if (goal == null) return true ;
 		ApamFilter f = ApamFilter.newInstance(goal) ;
 		if (f == null) return false ;
 		return match(f);
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
 
 	@Override
 	public Set<ResourceReference> getAllProvidedResources () {
 		Set<ResourceReference> allResources  = new HashSet<ResourceReference> () ;
 		Component current = this ;
 		while (current != null) {
 			if (current.getDeclaration().getProvidedResources() != null)
 				allResources.addAll (current.getDeclaration().getProvidedResources()) ;
 			current = current.getGroup() ;
 		}
 		return allResources ;
 	}
 
 }
