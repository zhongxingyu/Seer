 package chameleon.core.language;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.rejuse.association.OrderedReferenceSet;
 import org.rejuse.association.Reference;
 import org.rejuse.association.ReferenceSet;
 import org.rejuse.association.Relation;
 import org.rejuse.property.Property;
 import org.rejuse.property.PropertyMutex;
 import org.rejuse.property.PropertySet;
 import org.rejuse.property.PropertyUniverse;
 
 import chameleon.core.element.ChameleonProgrammerException;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupStrategyFactory;
 import chameleon.core.namespace.RootNamespace;
 import chameleon.core.property.PropertyRule;
 import chameleon.tool.Connector;
 import chameleon.tool.Processor;
 
 /**
  * A class representing a Chameleon language.
  *
  * The language object contains the default namespace, which is the entry point into the model.
  * 
  * The language object contains the properties that elements in that language can have. In addition, it contains
  * the property rule which define the default properties of elements. These rules are used if the explicitly declared
  * properties of an element say nothing about a certain property.
  * 
  * The language object contains the connectors and processors for a tool that allow that tool to work with that particular language. 
  * 
  * @author Marko van Dooren
  */
 
 public abstract class Language implements PropertyUniverse<Element> {
 	
 	/**
 	 * Initialize a new language with the given name.
 	 * 
 	 * @param name
 	 *        The name of the language.
 	 */
  /*@
    @ public behavior
    @
    @ pre name != null;
    @
    @ post name().equals(name);
    @ post (* The lookup strategy factory is initialized to the default LookupStrategyFactory *);
    @*/
 	public Language(String name) {
 		this(name, new LookupStrategyFactory());
 	}
 	
 	/**
 	 * Initialize a new language with the given name and lookup strategy factory.
 	 * 
 	 * @param name
 	 *        The name of the language.
 	 */
  /*@
    @ public behavior
    @
    @ pre name != null;
    @ pre factory != null;
    @
    @ post name().equals(name);
    @ post lookupFactory() == factory;
    @*/
 	public Language(String name, LookupStrategyFactory factory) {
 		setName(name);
 		setLookupStrategyFactory(factory);
 		initializePropertyRules();
 		SCOPE_MUTEX = new PropertyMutex<Element>();
 	}
 	
 	/**
 	 * Return the name of this language.
 	 */
  /*@
    @ public behavior
    @
    @ post \result != null;
    @*/
 	public String name() {
 		return _name;
 	}
 	
 	/**
 	 * Return the default properties of the given element.
 	 * @return
 	 */
  /*@
    @ public behavior
    @
    @ pre element != null;
    @
    @ (* The properties of all rules are added to the result.*)
    @ post (\forall PropertyRule rule; propertyRules().contains(rule);
    @         \result.containsAll(rule.properties(element)));
    @ (* Only the properties given by the property rules are in the result *);
    @ post (\forall Property<Element> p; \result.contains(p);
    @        \exists(PropertyRule rule; propertyRules().contains(rule);
    @           rule.properties(element).contains(p)));
    @*/
 	public PropertySet<Element> defaultProperties(Element element) {
 		PropertySet<Element> result = new PropertySet<Element>();
 		for(PropertyRule rule:propertyRules()) {
 			result.addAll(rule.properties(element));
 		}
 		return result;
 	}
 	
 	/**
 	 * Return the list of rule that determine the default properties of an element.
 	 * @return
 	 */
  /*@
    @ public behavior
    @
    @ post \result != null;
    @*/
 	public List<PropertyRule> propertyRules() {
 		return _propertyRules.getOtherEnds();
 	}
 	
   /**
    * Add all property rules in this method.
    */
 	protected abstract void initializePropertyRules();
 	
 	/**
 	 * Add a property rule to this language object.
 	 * @param rule
 	 */
  /*@
    @ public behavior
    @
    @ pre rule != null;
    @
    @ post propertyRules().contains(rule);
    @*/
 	public void addPropertyRule(PropertyRule rule) {
 		if(rule == null) {
 			throw new ChameleonProgrammerException("adding a null property rule to a language");
 		}
 		_propertyRules.add(rule.languageLink());
 	}
 	
 	/**
 	 * Remove a property rule from this language object.
 	 * @param rule
 	 */
  /*@
    @ public behavior
    @
    @ pre rule != null;
    @
    @ post ! propertyRules().contains(rule);
    @*/
 	public void removePropertyRule(PropertyRule rule) {
 		if(rule == null) {
 			throw new ChameleonProgrammerException("removing a null property rule to a language");
 		}
 		_propertyRules.remove(rule.languageLink());
 	} 
 	
 	private OrderedReferenceSet<Language,PropertyRule> _propertyRules = new OrderedReferenceSet<Language,PropertyRule>(this);
 	
 	/**
 	 * Set the name of this language.
 	 * @param name
 	 *        The new name of this language
 	 */
  /*@
    @ public behavior
    @
    @ pre name != null;
    @
    @ post getName() == name;
    @*/
 	public void setName(String name) {
 		_name = name;
 	}
 	
 	/*
 	 * The name of this language.
 	 */
 	private String _name;
 
 	public RootNamespace defaultNamespace() {
         return (RootNamespace)_default.getOtherEnd();
     }
 
 	/**
 	 * A property mutex for the scope property.
 	 */
 	public final PropertyMutex<Element> SCOPE_MUTEX;
 
 	 /**************
     * CONNECTORS *
     **************/
 	
     private static class MapWrapper<T> {
         private Map<Class<? extends T>,T> _map = new HashMap<Class<? extends T>,T>();
 
         public <S extends T> S get(Class<S> key) {
             return (S)_map.get(key);
         }
 
         public <S extends T> void put(Class<? extends S> key, S value) {
             _map.put(key,value);
         }
 
         public <S extends T> void remove(Class<S> key) {
             _map.remove(key);
         }
 
         public Collection<T> values() {
             return _map.values();
         }
 
         public <S extends T> boolean containsKey(Class<S> key) {
             return _map.containsKey(key);
         }
 
         public boolean isEmpty() {
             return _map.isEmpty();
         }
     }
 
     private static class ListMapWrapper<T> {
       private Map<Class<? extends T>,List<? extends T>> _map = new HashMap<Class<? extends T>,List<? extends T>>();
 
       public int size() {
       	return _map.size();
       }
       
       public <S extends T> List<S> get(Class<S> key) {
       	List<S> processors = (List<S>)_map.get(key);
       	if(processors == null) {
       		return new ArrayList<S>();
       	} else {
           return new ArrayList<S>(processors);
       	}
       }
 
       public <S extends T> void add(Class<S> key, S value) {
       	  List<S> list = (List<S>)_map.get(key);
       	  if(list == null) {
       	  	list = new ArrayList<S>();
       	  	_map.put(key, list);
       	  }
       	  list.add(value);
       }
 
       public <S extends T> void remove(Class<S> key, S value) {
       	_map.get(key).remove(key);
       }
 
       public Collection<List<? extends T>> values() {
           return _map.values();
       }
 
       public <S extends T> boolean containsKey(Class<S> key) {
           return _map.containsKey(key);
       }
 
       public boolean isEmpty() {
           return _map.isEmpty();
       }
   }
 
     private MapWrapper<Connector> _toolConnectors = new MapWrapper<Connector>();
 
     //private Map<Class<? extends ToolExtension>,? extends ToolExtension> toolExtensions = new HashMap<Class<? extends ToolExtension>,ToolExtension>();
     /*private <T extends ToolExtension> Map<Class<T>,T> getMap() {
         return (Map<Class<T>,T>)toolExtensions;
     }*/
 
     /**
      * Return the connector corresponding to the given connector interface.
      */
     public <T extends Connector> T connector(Class<T> connectorInterface) {
         return _toolConnectors.get(connectorInterface);//((Map<Class<T>,T>)getMap()).get(toolExtensionClass);
     }
 
     /**
      * Remove the connector corresponding to the given connector interface. The
      * bidirection relation is kept in a consistent state.
      * 
      * @param <T>
      * @param connectorInterface
      */
     public <T extends Connector> void removeConnector(Class<T> connectorInterface) {
         T old = _toolConnectors.get(connectorInterface);
         _toolConnectors.remove(connectorInterface);
         if (old!=null && old.language() == this) {
             old.setLanguage(null, connectorInterface);
         }
     }
 
     /**
      * Set the connector correponding to the given connector interface. The bidirectional relation is 
      * kept in a consistent state.
      * 
      * @param <T>
      * @param connectorInterface
      * @param connector
      */
     public <T extends Connector> void setConnector(Class<T> connectorInterface, T connector) {
         T old = _toolConnectors.get(connectorInterface);
         if (old!=connector) {
             if ((connector!=null) && (connector.language()!=this)) {
                 connector.setLanguage(this, connectorInterface);
             }
             // Clean up old backpointer
             if (old!=null) {
                 old.setLanguage(null, connectorInterface);
             }
             // Either
             if(connector != null) {
             	// Add connector to map
               _toolConnectors.put(connectorInterface, connector);
             } else {
             	// Remove entry in map
             	_toolConnectors.remove(connectorInterface);
             }
         }
     }
 
     /**
      * Return all connectors attached to this language object.
      * @return
      */
    /*@
      @ public behavior
      @
      @ post \result != null; 
      @*/
     public Collection<Connector> connectors() {
         return _toolConnectors.values();
     }
     
 
     /**
      * Check if this language has a connector for the given connector type. Typically
      * the type is an interface or abstract class for a specific tool.
      * 
      * @param <T>
      * @param connectorInterface
      * @return
      */
     public <T extends Connector> boolean hasConnector(Class<T> connectorInterface) {
         return _toolConnectors.containsKey(connectorInterface);
     }
 
     /**
      * Check if this language object has any connectors.
      * 
      * @return
      */
     public boolean hasConnectors() {
         return ! _toolConnectors.isEmpty();
     }
 
     /**************
      * PROCESSORS *
      **************/
     
     private ListMapWrapper<Processor> _processors = new ListMapWrapper<Processor>();
 
     /**
      * Return the processors corresponding to the given processor interface.
      */
     public <T extends Processor> List<T> processors(Class<T> connectorInterface) {
       return _processors.get(connectorInterface);
     }
 
     /**
      * Remove the given processor. The
      * bidirection relation is kept in a consistent state.
      * 
      * @param <T>
      * @param connectorInterface
      */
     public <T extends Processor> void removeProcessor(Class<T> connectorInterface, T processor) {
         List<T> list = _processors.get(connectorInterface);
         if (list!=null && list.contains(processor)) {
             processor.setLanguage(null, connectorInterface);
             list.remove(processor);
         }
     }
 
 
     /**
      * Add the ginve processor to the list of processors correponding to the given connector interface. 
      * The bidirectional relation is kept in a consistent state.
      * 
      * @param <T>
      * @param connectorInterface
      * @param connector
      */
     public <T extends Processor> void addProcessor(Class<T> connectorInterface, T processor) {
       _processors.add(connectorInterface, processor);
       if(processor.language() != this) {
       	processor.setLanguage(this, connectorInterface);
       }
     }
 
     /**************************************************************************
      *                                 PROPERTIES                             *
      **************************************************************************/
     
     /**
      * Return the properties that can be used for elements in this model.
      * 
      * For every class of properties, one object is in the set.
      * @return
      */
     public Set<Property<Element>> properties() {
       return new HashSet<Property<Element>>(_properties.getOtherEnds());
     }
 
     /**
      * Return the object representing the association between this language and the
      * properties to which it is attached.
      * 
      * DO NOT MODIFY THE RESULTING OBJECT. IT IS ACCESSIBLE ONLY BECAUSE OF THE 
      * VERY DUMB ACCESS CONTROL IN JAVA.
      */
     public ReferenceSet<PropertyUniverse<Element>,Property<Element>> propertyLink() {
       return _properties;
     }
     
     private ReferenceSet<PropertyUniverse<Element>,Property<Element>> _properties = new ReferenceSet<PropertyUniverse<Element>, Property<Element>>(this);
     
     /**
      * 
      * @param name
      * @return
      * @throws ChameleonProgrammerException
      *         There is no property with the given name.
      */
     public Property<Element> property(String name) throws ChameleonProgrammerException {
     	for(Property<Element> p: properties()) {
     		if(p.name().equals(name)) {
     			return p;
     		}
     	}
     	throw new ChameleonProgrammerException("Property with name: "+name+" not found.");
     }
     
     /**************************************************************************
      *                           DEFAULT NAMESPACE                            *
      **************************************************************************/
     
     public void setDefaultNamespace(RootNamespace defaultNamespace) {
         _default.connectTo(defaultNamespace.languageLink());
     }
 
     private Reference<Language,RootNamespace> _default = new Reference<Language,RootNamespace>(this); //todo wegens setDefaultNamespace kan dit niet generisch worden gemaakt?
 
     /**
      * @return
      */
     public Relation defaultNamespaceLink() {
         return _default;
     }
 
     public LookupStrategyFactory lookupFactory() {
     	return _contextFactory;
     }
     
     protected void setLookupStrategyFactory(LookupStrategyFactory factory) {
     	_contextFactory = factory;
     }
     
     private LookupStrategyFactory _contextFactory;
 
    /**
		 * Returns true if the given character is a valid character
		 * for an identifier.
		 */
		public abstract boolean isValidIdentifierCharacter(char character);
 }
 
