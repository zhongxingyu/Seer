 package midcontainers.local;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.LinkedList;
 import java.lang.reflect.*;
 import java.lang.annotation.*;
 
 import midcontainers.Binding;
 import midcontainers.Container;
 import midcontainers.ContainerException;
 import midcontainers.Named;
 
 public class LocalContainer implements Container {
 
     private final Map<Binding.Key, Binding> bindings = new HashMap<Binding.Key, Binding>();
     private final Map<String, Object> definitions = new HashMap<String, Object>();
     private final Map<Binding.Key, Object> singletons = new HashMap<Binding.Key, Object>();
     private final List<Container> delegates = new LinkedList<Container>();
 
     /**
      * Declare a component binding.
      *
      * @param binding the binding definition
      * @return this container
      */
     public Container declare(Binding binding) {
 
         bindings.put(binding.getKey(), binding);
         return this;
 
     }
 
     private String qualifierNameFor(Annotation[] parameterAnnotations) {
     for (Annotation annotation : parameterAnnotations) {
         if (annotation instanceof Named) {
             return ((Named) annotation).value();
         }
     }
     return null;
 }
 
     /**
      * Define a value.
      *
      * @param name the value name
      * @param value the value
      * @return this container
      */
     public Container define(String name, Object value) {
 
         definitions.put(name, value);
         return this;
 
     }
 
     /**
      * Obtain a reference to a component based on an interface and a
      * <code>null</code> qualifier. This is the same as
      * <code>obtainReference(interfaceClass, null)</code>.
      *
      * @param interfaceClass the component interface class
      * @param <T> the type of the component interface
      * @return the component implementation
      */
     public <T> T obtainReference(Class<T> interfaceClass) throws ContainerException {
         
         return obtainReference(interfaceClass, null);
        
     }
         
     
 
     /**
      * Obtain a reference to a component based on a full binding.
      *
      * @param interfaceClass the component interface class
      * @param qualifier the binding qualifier, which may be <code>null</code>
      * @param <T> the type of the component interface
      * @return the component implementation
      */
     public <T> T obtainReference(Class<T> interfaceClass, String qualifier) {
 
         Binding bind = this.bindings.get(new Binding.Key(interfaceClass, qualifier));
         if (bind == null) {
             throw new ContainerException();
         } else {
             T res = null;
             Class<?> implClass = bind.getImplementationClass();
            Constructor<?>[] constructors = implClass.getConstructors();
             for (int i = 0; i < constructors.length; i++) {
                 
                 Class<?>[] paramType = constructors[i].getParameterTypes();
                 Annotation[][] parameterAnnotations = constructors[i].getParameterAnnotations();
                 Object[] parameterValue = new Object [paramType.length];
                 
                 for (int j = 0 ; j < paramType.length; j++) {
                     String annot = qualifierNameFor(parameterAnnotations[j]);    
                     if(hasReferenceDeclaredFor(paramType[j], annot)) {
                         parameterValue[j] = this.obtainReference(paramType[j], annot);
                     } else if (hasValueDefinedFor(annot)) {
                         parameterValue[j] = this.definitionValue(annot);
                     }else{
                         
                         for (int k = 0; k < this.delegates.size(); k++) {
                             Container delegat = this.delegates.get(k) ;
                             if ( (delegat.obtainReference(paramType[j], annot)) != null) {
                                 parameterValue[j] = delegat.obtainReference(paramType[j], annot);
                                 break;
                             }
                         }
                     }
                 }
                 
                 try {
                     if (bind.getPolicy() == Binding.Policy.SINGLETON) {
                        if ( this.singletons.containsKey(bind.getKey()) == false) {
                            this.singletons.put(bind.getKey(), (T) constructors[i].newInstance(parameterValue));
                         }
                        return (T) this.singletons.get(bind.getKey());   
                     } 
                 return res = (T) constructors[i].newInstance(parameterValue);
                 }    
                 catch (InstantiationException e) {
                     e.printStackTrace();
                 }
                 catch (IllegalAccessException e) {
                     e.printStackTrace();
                 }
                 catch (InvocationTargetException e) {
                     e.printStackTrace();
              
                 }
                
             }
             return res;
             }        
 
     }
 
     /**
      * Obtain a defined value.
      *
      * @param name the value name
      * @return the value
      */
     public Object definitionValue(String name) throws ContainerException {
 
         if (definitions.get(name) == null) {
             throw new ContainerException();
         } else {
             Object result = definitions.get(name);
             return result;
         }
     }
 
     // Appears with delegation support .............................................................................. //
     /**
      * Checks whether a component is available locally for an interface with a
      * <code>null</code> qualifier.
      *
      * @param interfaceClass the interface class
      * @return <code>true</code> if a component is bound, <code>false</code>
      * otherwise
      */
     public boolean hasReferenceDeclaredFor(Class<?> interfaceClass) {
         Binding.Key key = new Binding.Key (interfaceClass, null);
         if (bindings.containsKey(key)) {
             return true;
         } else return false;
     }
 
     /**
      * Checks whether a component is available locally for an interface with a
      * qualifier.
      *
      * @param interfaceClass the interface class
      * @param qualifier the qualifier
      * @return <code>true</code> if a component is bound, <code>false</code>
      * otherwise
      */
     public boolean hasReferenceDeclaredFor(Class<?> interfaceClass, String qualifier) {
          Binding.Key key = new Binding.Key (interfaceClass, qualifier);
          if (bindings.containsKey(key)) {
             return true;
         } else return false;
     }
 
     /**
      * Checks whether a value is available locally.
      *
      * @param name the value name
      * @return <code>true</code> if a value is defined, <code>false</code>
      * otherwise
      */
     public boolean hasValueDefinedFor(String name) {
         if (definitions.containsKey(name)) {
             return true;
         } else
         return false;
     }
 
     /**
      * Add a delegation link from this container to another one.
      *
      * @param container the delegate container
      * @return this container
      */
     public Container delegateTo(Container container) {
         this.delegates.add(container);
         return this;
     }
 }
