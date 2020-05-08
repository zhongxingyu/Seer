 package fr.imag.adele.apam.instance;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.felix.ipojo.FieldInterceptor;
 import org.apache.felix.ipojo.InstanceManager;
 import org.apache.felix.ipojo.metadata.Attribute;
 import org.apache.felix.ipojo.metadata.Element;
 import org.apache.felix.ipojo.parser.FieldMetadata;
 import org.apache.felix.ipojo.parser.PojoMetadata;
 import org.apache.felix.ipojo.util.Logger;
 import org.osgi.framework.Filter;
 
 import fr.imag.adele.apam.apamAPI.ASMInst;
 
 /**
  * This class keeps track of an APAM dependency, it handles the calculation of the target services based on updates to
  * the application model.
  * 
  * The target of a dependency can specify
  * 
  * @author vega
  * 
  */
 public class Dependency implements FieldInterceptor {
 
     /**
      * The source instance
      */
     private final InstanceManager	instance;
 
     /**
      * The name of the dependency
      */
     private final String            name;
 
     /**
      * Whether this dependency is aggregate or scalar
      */
     private final boolean           isAggregate;
 
     /**
      * The name of the target entity.
      * 
      */
     private final String            target;
 
     /**
      * The kind of target
      */
     private final Kind              targetKind;
 
     /**
      * The optional constraints
      */
     private final Set<Filter> 		constraints;
     
      /**
      * The optional preferences
      */
     private final List<Filter>		preferences;
     
     /**
      * The description of the injected fields
      */
     private final PojoMetadata		instrumentedCodeDescription;
     
     
     /**
      * The kind of possible targets for the dependency
      */
 
     public static enum Kind {
         INTERFACE, SPECIFICATION, IMPLEMENTATION
     }
 
     /**
      * The list of target services.
      */
     private final Set<ASMInst> targetServices;
 
     /**
      * The last injected value.
      * 
      * This is a cached value that must be recalculated in case of update of the dependency.
      */
     private Object             injectedValue;
 
     /**
      * The last injected value type.
      * 
      * This is a cached value that must be recalculated in case of update of the dependency.
      */
     private String             injectedType;
 
     /**
      * Whether this dependency is satisfied by a target service.
      * 
      * This is a cached value that must be recalculated in case of update of the dependency.
      */
     private boolean            isResolved;
 
     /**
      * Dependency constructor.
      * 
      * @param pojoMetadata the name of the dependency.
      * 
      */
     public Dependency(InstanceManager instance, PojoMetadata instrumentedCodeDescription, String name, Boolean isAggregate,
             String target, Kind targetKind, Set<Filter> constraints, List<Filter> preferences) {
         
     	this.instance = instance;
         this.instrumentedCodeDescription = instrumentedCodeDescription;
 
         this.name = name;
         this.isAggregate = isAggregate;
         this.target = target;
         this.targetKind = targetKind;
         
         this.constraints = constraints;
         this.preferences = preferences;
 
         targetServices = new HashSet<ASMInst>();
         injectedValue = null;
         injectedType = null;
         isResolved = false;
        
    	
    	/*
    	 * If the attached instance is a native APAM instance, register ourselves to be notified of changes in dependency
    	 * resolution.
    	 * 
         * TODO we are only able to receive notifications if the source instance is an APAM native instance. We need
         * to also handle the case of iPojo component instances with APAM dependencies (e.g. hybrid configurations)
         */
    	if (instance instanceof Instance)
    		((Instance)instance).addDependency(this);

        
     }
 
     /**
      * The dependency name.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Whether this dependency is aggregate
      */
     public boolean isAggregate() {
         return isAggregate;
     }
 
     /**
      * Whether this dependency is scalar
      */
     public boolean isScalar() {
         return !isAggregate();
     }
 
     /**
      * The name of the target entity
      */
     public String getTarget() {
         return target;
     }
 
     /**
      * The kind of target
      */
     public Kind getKind() {
         return targetKind;
     }
 
     /**
      * The constraints
      */
     public Set<Filter> getConstraints() {
     	return constraints;
     }
     
     /**
      * The preferences
      */
     public List<Filter> getPreferences() {
     	return preferences;
     }
    
     /**
      * Get an XML representation of the state of this dependency
      */
     public Element getDescription() {
     	
 		Element dependencyDescription = new Element("dependency", "");
 		dependencyDescription.addAttribute(new Attribute("name", getName()));
 		dependencyDescription.addAttribute(new Attribute("isAggregate",	Boolean.toString(isAggregate())));
 		dependencyDescription.addAttribute(new Attribute("target", getTarget()));
 		dependencyDescription.addAttribute(new Attribute("kind", getKind().toString()));
 
 		boolean firstElement = false;
 
 		StringBuffer constraints = new StringBuffer();
 		constraints.append("{");
 		firstElement = true;
 		for (Filter filter : getConstraints()) {
 			if (!firstElement)
 				constraints.append(",");
 			constraints.append(filter.toString());
 			firstElement = false;
 		}
 		constraints.append("}");
 		dependencyDescription.addAttribute(new Attribute("constraints", constraints.toString()));
 
 		StringBuffer preferences = new StringBuffer();
 		preferences.append("{");
 		firstElement = true;
 		for (Filter filter : getPreferences()) {
 			if (!firstElement)
 				preferences.append(",");
 			preferences.append(filter.toString());
 			firstElement = false;
 		}
 		preferences.append("}");
 		dependencyDescription.addAttribute(new Attribute("preferences",	preferences.toString()));
 
 		dependencyDescription.addAttribute(new Attribute("resolved",Boolean.toString(isResolved())));
 
 		if (isResolved()) {
 			StringBuffer resolution = new StringBuffer();
 			if (targetServices.size() > 1)
 				resolution.append("{");
 
 			firstElement = true;
 
 			for (ASMInst target : targetServices) {
 				if (!firstElement)
 					resolution.append(",");
 				resolution.append(target.getName());
 				firstElement = false;
 			}
 
 			if (targetServices.size() > 1)
 				resolution.append("}");
 
 			dependencyDescription.addAttribute(new Attribute("resolution", resolution.toString()));
 		}
 		
 		return dependencyDescription;
 	
     }
     /**
      * Adds a new target to this dependency
      */
     public void addTarget(ASMInst target) {
 
         /*
          * Add this target and invalidate cache
          */
         synchronized (this) {
 
             /*
              * In the case of scalar dependencies we try to not unnecessarily invalidate the cache by keeping the
              * already used target.
              * 
              * TODO This is an anomalous case, the APAM should not add a new target to an scalar dependency without
              * removing the previous ones. This may happen only if there is an application model that is not coherent
              * with the dependency metadata.
              */
         	
         	/*
         	 * TODO  When substituting dependencies, the existing dependency is not removed before the new one is
         	 * added, so we have to replace anyway. Modify APAM code to use substituteDependency 
         	 * 
             if (isScalar() && (targetServices.size() != 0)) {
                 return;
             }
             
         	 */
             targetServices.add(target);
             injectedValue = null;
         }
 
     }
 
     /**
      * Removes a target from the dependency
      * 
      * @param target
      */
     public void removeTarget(ASMInst target) {
 
         /*
          * Remove this target and invalidate cache
          */
         synchronized (this) {
             targetServices.remove(target);
             injectedValue = null;
         }
     }
 
     /**
      * Substitutes an existing target by a new one
      * 
      * @param oldTarget
      * @param newTarget
      */
     public void substituteTarget(ASMInst oldTarget, ASMInst newTarget) {
 
         /*
          * substitute the target atomically and invalidate the cache
          */
         synchronized (this) {
 
             if (!targetServices.contains(oldTarget))
                 return;
 
             targetServices.remove(oldTarget);
             targetServices.add(newTarget);
             injectedValue = null;
 
         }
     }
 
     /**
      * Whether this dependency is satisfied by a target service.
      * 
      */
     public boolean isResolved() {
         synchronized (this) {
             /*
              * Return the cached value, if it has not been invalidated
              */
             if (injectedValue != null)
                 return isResolved;
 
             /*
              * update cached value
              */
             isResolved = !targetServices.isEmpty();
 
             return isResolved;
         }
 
     }
 
     public void onSet(Object pojo, String fieldName, Object value) {
         // Nothing to do, this should never happen as we exclusively handle the field's value
     }
 
     public Object onGet(Object pojo, String fieldName, Object value) {
 
         /*
          * Verify if there is a service fault (a required dependency is not present) and delegate to APAM handling of
          * this case.
          */
         if (!isResolved()) {
 
             /*
              * Ask APAM to resolve the dependency. Depending on the application policies this may throw an error, or
              * block the thread until the dependency is fulfilled, or keep the dependency unresolved in the case of
              * optional dependencies.
              * 
              * Resolution has as side-effect a modification of the target services.
              * 
              * TODO we are only able to resolve a dependency if the source instance is an APAM native instance. We need
              * to also handle the case of iPojo component instances with APAM dependencies (e.g. hybrid configurations)
              */
         	if (instance instanceof Instance)
         		((Instance)instance).resolve(this);
         }
 
          return getFieldValue(fieldName);
     }
 
     /**
      * Get the value to be injected in the field. The returned object depends on the cardinality of the dependency.
      * 
      * For scalar dependencies, returns directly the service object associated with the target instance. For unresolved
      * optional dependencies it return null.
      * 
      * For aggregate dependencies, returns a collection of service objects. The kind of collection is chosen to match
      * the declared class of the field. For unresolved optional dependencies returns an empty collection.
      * 
      * In principle the returned object should not be aliased (by keeping a reference to it or passing it in parameters
      * to other classes), as it is potentially dynamically recalculated every time the field is accessed.
      * 
      * In the case of aggregate dependencies, the returned collection should be immutable, as it is calculated by APAM
      * according to global application policies.
      * 
      * We suppose that components are well behaved and follow these restrictions, so we do not have a complex machinery
      * to enforce this. This method directly return the service object or a non thread-safe mutable collection of
      * service objects.
      * 
      * TODO if we want to support a less restrictive programming model (allowing aliasing), or to be more robust against
      * bad behaved components, we should use smart proxies and collections backed up by the information in this
      * dependency object.
      * 
      */
     private Object getFieldValue(String fieldName) {
 
         synchronized (this) {
         	
             
             /*
              * Handle first the most common case of scalar dependencies.
              */
         	if (isScalar()) {
             	/*
                  * Return the cached value, if it has not been invalidated.
                  */ 
         		if (injectedValue != null)
         			return injectedValue;
         		
         		/*
         		 * update cached value
         		 */
                 isResolved = !targetServices.isEmpty();
         		injectedValue = targetServices.isEmpty() ? null : targetServices.iterator().next().getServiceObject();
                 return injectedValue;        		
         	}
         	
             /* For the aggregate dependencies, the type of the returned Collection depends on the type of the declared field.
              *  
              * TODO Currently we only cache a single value for a dependency. For different collection types we need to
              * evaluate if it is worth caching all accessed fields.
              */
 
             FieldMetadata field = instrumentedCodeDescription.getField(fieldName);
             String fieldType	= FieldMetadata.getReflectionType(field.getFieldType());
 
         	/*
              * Return the cached value, if it has not been invalidated.
              */ 
            if (injectedValue != null && injectedType.equals(fieldType))
                 return injectedValue;
 
 	   		/*
 	   		 * update cached value to a private copy of the resolution target
 	   		 */
             isResolved = !targetServices.isEmpty();
             injectedValue = getServiceObjectCollection(fieldType);
             injectedType = fieldType;
 
             return injectedValue;
         }
 
     }
 
     /**
      * Returns a collection of service objects that is compatible with the class of the field that will be injected.
      * 
      */
     private Object getServiceObjectCollection(String fieldType) {
 
         try {
 
             /*
              * return the collection that better fits the field declaration
              */
             Class<?> fieldClass = instance.getFactory().loadClass(fieldType);
 
             /*
              * For arrays we need to reflectively build a type conforming array 
              */
             if (fieldClass.isArray()) {
             	
                 int index = 0;
             	Object array = Array.newInstance(fieldClass.getComponentType(),targetServices.size());
                 for (ASMInst targetService : targetServices) {
                 	Array.set(array, index++, targetService.getServiceObject());
                 }
                return array;
             }
 
             /*
              * For collections use an erased Object collection
              */
             List<Object> serviceObjects = new ArrayList<Object>(targetServices.size());
             for (ASMInst targetService : targetServices) {
             	serviceObjects.add(targetService.getServiceObject());
             }
 
  
             if (Vector.class.isAssignableFrom(fieldClass)) {
                 return new Vector<Object>(serviceObjects);
             }
 
             if (List.class.isAssignableFrom(fieldClass)) {
                 return serviceObjects;
             }
 
             if (Set.class.isAssignableFrom(fieldClass)) {
                 return new HashSet<Object>(serviceObjects);
             }
 
             if (Collection.class.isAssignableFrom(fieldClass)) {
                 return serviceObjects;
             }
 
         } catch (ClassNotFoundException unexpected) {
             instance.getLogger().log(Logger.ERROR,"error accesing field for APAM dependency " + getName(),unexpected);
         }
 
         return null;
     }
 
     private final static Class<?>[] supportedCollections      = new Class<?>[] { Collection.class, List.class,Vector.class, Set.class };
     private final static String     supportedCollectionsNames = "array or Collection or List or Vector or Set";
 
     /**
      * Return the list of supported collections
      */
     public static String supportedCollectionClasses() {
         return Dependency.supportedCollectionsNames;
     }
 
     /**
      * Validates if the field class specified is compatible with the service collections that we know
      */
     public static boolean isSupportedCollection(Field field) {
 
         Class<?> fieldClass = field.getType();
 
         if (fieldClass.isArray())
             return true;
 
         for (Class<?> supportedCollection : Dependency.supportedCollections) {
             if (fieldClass.isAssignableFrom(supportedCollection))
                 return true;
         }
 
         return false;
     }
 
     /**
      * Get the type of elements of the specified supported collection. It may not be possible to have this information
      * in case of erased collections.
      */
     public static Class<?> getCollectionElement(Field field) {
 
         Type fieldType = field.getGenericType();
 
         /*
          * The field is declared as a raw type, we can only infer element types for arrays
          */
         if (fieldType instanceof Class) {
             Class<?> fieldClass = (Class<?>) fieldType;
 
             if (fieldClass.isArray())
                 return fieldClass.getComponentType();
 
         }
 
         /*
          * verify if the field is declared as one of the supported parameterized collections, and an actual type
          * parameter was specified
          */
         if (fieldType instanceof ParameterizedType) {
 
             ParameterizedType fieldParametizedType = (ParameterizedType) field.getGenericType();
 
             for (Class<?> supportedCollection : Dependency.supportedCollections) {
                 if (supportedCollection.equals(fieldParametizedType.getRawType())) {
                     Type[] parameters = fieldParametizedType.getActualTypeArguments();
                     if ((parameters.length == 1) && (parameters[0] instanceof Class))
                         return (Class<?>) parameters[0];
                 }
             }
         }
 
         return null;
 
     }
 
 }
