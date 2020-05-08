 package fr.imag.adele.apam.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * This class represents the common description of a component at all levels of abstraction
  * (specification, implementation, composite or instance)
  * 
  * @author vega
  * 
  */
 public abstract class ComponentDeclaration {
 
     /**
      * The identifier of the service provider
      */
     private final String name;
 
     
     /**
      * The reference to this component declaration
      */
     private final ComponentReference<?> reference;
 
     /**
      * The resources provided by this service
      */
     protected final Set<ResourceReference> providedResources;
 
     /**
      * The resources required by this service
      */
     private final Set<DependencyDeclaration> 	dependencies;
 
     /**
      * The properties describing this service provider
      */
     private final Map<String,String> 			properties;
     private final List<PropertyDefinition>		definitions;
 
     /**
      * Whether instances of this component must be accessed exclusively by a single client
      * at the time
      */
     private boolean isExclusive;
     private boolean isDefinedExclusive;
 
     /**
      * Whether we can create instances of this component
      */
     private boolean isInstantiable;
     private boolean isDefinedInstantiable;
     
     /**
      * Whether there can be a single instance of this component in the execution platform
      */
     private boolean isSingleton;
     private boolean isDefinedSingleton;
 
     /**
      * Whether the instance of this component can be shared by different clients
      */
     public boolean isShared;
     public boolean isDefinedShared;
 
     protected ComponentDeclaration(String name) {
 
         assert name != null;
 
         this.name			= name;
         
         this.isInstantiable	= true;
         this.isExclusive	= false;
         this.isSingleton	= false;
         this.isShared	    = true;
         this.isDefinedInstantiable	= false;
         this.isDefinedExclusive	    = false;
         this.isDefinedSingleton	    = false;
         this.isDefinedShared	    = false;
         
         reference			= generateReference();
         properties			= new HashMap<String,String>();
         providedResources	= new HashSet<ResourceReference>();
         dependencies		= new HashSet<DependencyDeclaration>();
         definitions 		= new ArrayList<PropertyDefinition>();
     }
 
     /**
      * Get the name of the provider
      */
     public String getName() {
         return name;
     }
 
     /**
      * Get the reference to this declaration
      */
     public ComponentReference<?> getReference() {
         return reference;
     }
 
     /**
      * Get the reference to this declaration
      */
     public abstract ComponentReference<?> getGroupReference() ;
 
     /**
      * Generates a unique resource identifier to reference this declaration
      */
     protected  abstract ComponentReference<?> generateReference();
 
     /**
      * Whether the component is exclusive
      */
     public boolean isExclusive() {
 		return isExclusive;
 	}
  
     public boolean isDefinedExclusive() {
 		return isDefinedExclusive;
 	}
 
     public void setExclusive(boolean isExclusive) {
 		this.isExclusive = isExclusive;
 		this.isDefinedExclusive = true ;
 	}
     //Warning : should be called ONLY by CoreMetadataParser
     public void setDefinedExclusive(boolean isExclusive) {
 		this.isDefinedExclusive = isExclusive ;
 	}
    
     /**
      * Whether the component is instantiable
      */
     public boolean isInstantiable() {
 		return isInstantiable;
 	}
     
     public void setInstantiable(boolean isInstantiable) {
 		this.isInstantiable = isInstantiable;
 		this.isDefinedInstantiable = true ;
 	}
     public boolean isDefinedInstantiable() {
 		return isDefinedInstantiable;
 	}
     
     //Warning : should be called ONLY by CoreMetadataParser
     public void setDefinedInstantiable(boolean isInstantiable) {
 		this.isDefinedInstantiable = isInstantiable;
 	}
     
     
     /**
      * Whether the component is singleton
      */
     public boolean isSingleton() {
 		return isSingleton;
 	}
     
     public void setSingleton(boolean isSingleton) {
 		this.isSingleton = isSingleton;
 		this.isDefinedSingleton = true ;
 	}
  
     public boolean isDefinedSingleton() {
 		return isDefinedSingleton;
 	}
     
     //Warning : should be called ONLY by CoreMetadataParser
     public void setDefinedSingleton(boolean isSingleton) {
 		this.isDefinedSingleton = isSingleton;
 	}
 
     /**
      * Whether the component is shared
      */
     public boolean isShared() {
 		return isShared;
 	}
     
     public void setShared(boolean isShared) {
 		this.isShared = isShared;
 		this.isDefinedShared = true ;
 	}
  
     public boolean isDefinedShared() {
 		return isDefinedShared;
 	}
     
     //Warning : should be called ONLY by CoreMetadataParser
     public void setDefinedShared(boolean isShared) {
 		this.isDefinedShared = isShared;
 	}
 
     /**
      * Get the properties describing this provider
      */
     public Map<String,String> getProperties() {
         return properties;
     }
 
     /**
      * Get the value of a property
      */
     public String getProperty(String property) {
         return properties.get(property);
     }
 
     /**
      * Get the property definitions defined by this component
      */
     public List<PropertyDefinition> getPropertyDefinitions() {
         return definitions;
     }
 
     /**
      * Get the named property definition, if declared
      */
     public PropertyDefinition getPropertyDefinition(String propertyName) {
         for (PropertyDefinition definition : definitions) {
             if (definition.getName().equalsIgnoreCase(propertyName))
                 return definition;
         }
         return null;
     }
 
    /**
     * Get the named property definition, if declared
     */
    public PropertyDefinition getPropertyDefinition(PropertyDefinition.Reference property) {
    	
    	if (! this.getReference().equals(property.getDeclaringComponent()))
    		return null;
    	
    	return getPropertyDefinition(property.getIdentifier());

    }
    
     public boolean isDefined(String propertyName) {
         return getPropertyDefinition(propertyName) != null;
     }
 
     /**
      * Get the provided resources
      */
     public Set<ResourceReference> getProvidedResources() {
         return providedResources;
     }
 
     /**
      * Get the provided resources of a given kind, for example Services or Messages. 
      * 
      * We use subclasses of ResourceReference as tags to identify kinds of resources. To add a new kind
      * of resource a new subclass must be added.
      * 
      * Notice that we return a set of resource references but typed to particular subtype of references,
      * the unchecked downcast is then safe at runtime.
      */
 	@SuppressWarnings("unchecked")
 	public <T extends ResourceReference> Set<T> getProvidedResources(Class<T> kind) {
     	Set<ResourceReference> resources = new HashSet<ResourceReference>();
     	for (ResourceReference resourceReference : providedResources) {
 			if (kind.isInstance(resourceReference))
 				resources.add(resourceReference);
 		}
         return (Set<T>) resources;
     }
    
 
     /**
      * Check if the specified resource is provided by this component
      * 
      */
     public boolean resolves(DependencyDeclaration dependency) {
         return providedResources.contains(dependency.getTarget());
     }
 
     /**
      * Get the declared dependencies of this component
      */
     public Set<DependencyDeclaration> getDependencies() {
         return dependencies;
     }
 
     /**
      * Get a dependency declaration by name
      */
     public DependencyDeclaration getDependency(String id) {
         for (DependencyDeclaration dependency : dependencies) {
             if (dependency.getIdentifier().equals(id))
                 return dependency;
         }
 
         return null;
     }
 
     /**
      * Get a dependency declaration by reference
      */
     public DependencyDeclaration getDependency(DependencyDeclaration.Reference dependency) {
     	if (! this.getReference().equals(dependency.getDeclaringComponent()))
     		return null;
     	
     	return getDependency(dependency.getIdentifier());
     }
 
     /**
      * Check if this component requires the specified resource
      */
     public boolean isRequired(ResourceReference resource) {
         for (DependencyDeclaration dependency : dependencies) {
             if ( dependency.getTarget().equals(resource))
                 return true;
         }
 
         return false;
     }
 
 
     @Override
     public String toString() {
         return printDeclaration("") ;
     }
 
     
 	/**
 	 * Displays the declaration on screen, indented.
 	 * Same as toString, plus the indentation.
 	 * @param indent: a number of white characters as indentation.
 	 */
 	public String printDeclaration (String indent) {
 		String nl = "\n" + indent ;
         String ret = indent + " Declaration of " + name;
         if (providedResources.size() != 0) {
             ret += nl + "   Provided resources: ";
             for (ResourceReference resRef : providedResources) {
                 ret += nl + "      " + resRef;
             }
         }
         if (dependencies.size() != 0) {
             ret += nl + "   Dependencies: \n";
             for (DependencyDeclaration resRef : dependencies) {
                 ret += resRef.printDependencyDeclaration(indent + "   ") + "\n";
             }
         }
         if (properties.size() != 0) {
             ret += nl + "   Properties: ";
             for (Object resRef : properties.keySet()) {
                 ret += nl + "      " + (String) resRef + " = " + properties.get(resRef);
             }
         }
         if (definitions.size() != 0) {
             ret += nl + "   Attribute definitions: ";
             for (PropertyDefinition resRef : definitions) {
                 ret += nl + "      " + resRef;
             }
         }
         
         return ret;
 	}
 
 }
