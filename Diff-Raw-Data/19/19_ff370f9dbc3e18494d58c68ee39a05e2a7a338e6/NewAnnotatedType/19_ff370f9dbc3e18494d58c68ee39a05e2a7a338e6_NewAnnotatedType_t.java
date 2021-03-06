 package org.jboss.weld.extensions.util.annotated;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.enterprise.inject.spi.AnnotatedConstructor;
 import javax.enterprise.inject.spi.AnnotatedField;
 import javax.enterprise.inject.spi.AnnotatedMethod;
 import javax.enterprise.inject.spi.AnnotatedType;
 
 /**
  * AnnotatedType implementation for adding beans in the BeforeBeanDiscovery
  * event
  * 
  * @author Stuart Douglas
  * 
  */
 class NewAnnotatedType<X> extends AbstractNewAnnotatedElement implements AnnotatedType<X>
 {
 
    private final Set<AnnotatedConstructor<X>> constructors;
    private final Set<AnnotatedField<? super X>> fields;
    private final Set<AnnotatedMethod<? super X>> methods;
 
    private final Class<X> javaClass;
 
    /**
     * We make sure that there is a NewAnnotatedMember for every public
     * method/field/constructor
     * 
     * If annotation have been added to other methods as well we add them to
     * 
     */
    NewAnnotatedType(Class<X> clazz, AnnotationStore typeAnnotations, Map<Field, AnnotationStore> fieldAnnotations, Map<Method, AnnotationStore> methodAnnotations, Map<Method, Map<Integer, AnnotationStore>> methodParameterAnnotations, Map<Constructor<X>, AnnotationStore> constructorAnnotations, Map<Constructor<X>, Map<Integer, AnnotationStore>> constructorParameterAnnotations)
    {
       super(clazz, typeAnnotations);
       this.javaClass = clazz;
       this.constructors = new HashSet<AnnotatedConstructor<X>>();
       Set<Constructor<?>> cset = new HashSet<Constructor<?>>();
       Set<Method> mset = new HashSet<Method>();
       Set<Field> fset = new HashSet<Field>();
       for (Constructor<?> c : clazz.getConstructors())
       {
          NewAnnotatedConstructor<X> nc = new NewAnnotatedConstructor<X>(this, c, constructorAnnotations.get(c), constructorParameterAnnotations.get(c));
          constructors.add(nc);
          cset.add(c);
       }
       for (Entry<Constructor<X>, AnnotationStore> c : constructorAnnotations.entrySet())
       {
          if (!cset.contains(c.getKey()))
          {
             NewAnnotatedConstructor<X> nc = new NewAnnotatedConstructor<X>(this, c.getKey(), c.getValue(), constructorParameterAnnotations.get(c.getKey()));
             constructors.add(nc);
          }
       }
       this.methods = new HashSet<AnnotatedMethod<? super X>>();
       for (Method m : clazz.getMethods())
       {
          NewAnnotatedMethod<X> met = new NewAnnotatedMethod<X>(this, m, methodAnnotations.get(m), methodParameterAnnotations.get(m));
          methods.add(met);
          mset.add(m);
       }
       for (Entry<Method, AnnotationStore> c : methodAnnotations.entrySet())
       {
          if (!mset.contains(c.getKey()))
          {
             NewAnnotatedMethod<X> nc = new NewAnnotatedMethod<X>(this, c.getKey(), c.getValue(), methodParameterAnnotations.get(c.getKey()));
             methods.add(nc);
          }
       }
       this.fields = new HashSet<AnnotatedField<? super X>>();
       for (Field f : clazz.getFields())
       {
          NewAnnotatedField<X> b = new NewAnnotatedField<X>(this, f, fieldAnnotations.get(f));
          fields.add(b);
          fset.add(f);
       }
       for (Entry<Field, AnnotationStore> e : fieldAnnotations.entrySet())
       {
         if (!fset.contains(e.getKey()))
          {
             fields.add(new NewAnnotatedField<X>(this, e.getKey(), e.getValue()));
          }
       }
    }
 
    public Set<AnnotatedConstructor<X>> getConstructors()
    {
       return Collections.unmodifiableSet(constructors);
    }
 
    public Set<AnnotatedField<? super X>> getFields()
    {
       return Collections.unmodifiableSet(fields);
    }
 
    public Class<X> getJavaClass()
    {
       return javaClass;
    }
 
    public Set<AnnotatedMethod<? super X>> getMethods()
    {
       return Collections.unmodifiableSet(methods);
    }
 
 }
