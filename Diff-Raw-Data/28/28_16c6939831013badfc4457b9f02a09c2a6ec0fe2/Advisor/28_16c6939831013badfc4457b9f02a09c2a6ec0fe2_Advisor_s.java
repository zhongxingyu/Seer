 /*
   * JBoss, Home of Professional Open Source
   * Copyright 2005, JBoss Inc., and individual contributors as indicated
   * by the @authors tag. See the copyright.txt in the distribution for a
   * full listing of individual contributors.
   *
   * This is free software; you can redistribute it and/or modify it
   * under the terms of the GNU Lesser General Public License as
   * published by the Free Software Foundation; either version 2.1 of
   * the License, or (at your option) any later version.
   *
   * This software is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   * Lesser General Public License for more details.
   *
   * You should have received a copy of the GNU Lesser General Public
   * License along with this software; if not, write to the Free
   * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
   * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
   */
 package org.jboss.aop;
 
 import gnu.trove.TLongObjectHashMap;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.security.AccessController;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMethod;
 
 import org.jboss.aop.advice.AdviceBinding;
 import org.jboss.aop.advice.AspectDefinition;
 import org.jboss.aop.advice.CFlowInterceptor;
 import org.jboss.aop.advice.Interceptor;
 import org.jboss.aop.advice.InterceptorFactory;
 import org.jboss.aop.advice.PrecedenceSorter;
 import org.jboss.aop.advice.Scope;
 import org.jboss.aop.annotation.AnnotationElement;
 import org.jboss.aop.annotation.AnnotationRepository;
 import org.jboss.aop.instrument.ConstructionTransformer;
 import org.jboss.aop.instrument.ConstructorExecutionTransformer;
 import org.jboss.aop.introduction.AnnotationIntroduction;
 import org.jboss.aop.introduction.InterfaceIntroduction;
 import org.jboss.aop.joinpoint.ConstructorJoinpoint;
 import org.jboss.aop.joinpoint.Invocation;
 import org.jboss.aop.joinpoint.InvocationResponse;
 import org.jboss.aop.joinpoint.Joinpoint;
 import org.jboss.aop.joinpoint.MethodInvocation;
 import org.jboss.aop.metadata.ClassMetaDataBinding;
 import org.jboss.aop.metadata.ConstructorMetaData;
 import org.jboss.aop.metadata.FieldMetaData;
 import org.jboss.aop.metadata.MethodMetaData;
 import org.jboss.aop.metadata.SimpleMetaData;
 import org.jboss.aop.pointcut.PointcutMethodMatch;
 import org.jboss.aop.util.UnmodifiableEmptyCollections;
 import org.jboss.metadata.spi.MetaData;
 import org.jboss.metadata.spi.signature.ConstructorSignature;
 import org.jboss.metadata.spi.signature.FieldSignature;
 import org.jboss.metadata.spi.signature.MethodSignature;
 import org.jboss.util.NestedRuntimeException;
 import org.jboss.util.NotImplementedException;
 
 /**
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author adrian@jboss.org
  * @version $Revision$
  */
 public abstract class Advisor
 {
    public MethodInfo getMethodInfo(long hash)
    {
       return methodInfos.getMethodInfo(hash);
    }
 
    private class AdviceInterceptorKey
    {
       private String adviceName;
       private Joinpoint joinpoint;
       private int hash;
 
       public AdviceInterceptorKey(String adviceName, Joinpoint joinpoint)
       {
          this.adviceName = adviceName;
          this.joinpoint = joinpoint;
          hash = adviceName.hashCode();
          hash = 29 * hash + (joinpoint != null ? joinpoint.hashCode() : 0);
       }
 
       public boolean equals(Object o)
       {
          if (this == o) return true;
          if (!(o instanceof AdviceInterceptorKey)) return false;
 
          final AdviceInterceptorKey adviceInterceptorKey = (AdviceInterceptorKey) o;
 
          if (!adviceName.equals(adviceInterceptorKey.adviceName)) return false;
          if (joinpoint != null ? !joinpoint.equals(adviceInterceptorKey.joinpoint) : adviceInterceptorKey.joinpoint != null) return false;
 
          return true;
       }
 
       public int hashCode()
       {
          return hash;
       }
    }
 
    /** Read/Write lock to be used when lazy creating the collections */
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
 
    protected Set<AdviceBinding> adviceBindings = new HashSet<AdviceBinding>();
    protected volatile ArrayList<InterfaceIntroduction> interfaceIntroductions = UnmodifiableEmptyCollections.EMPTY_ARRAYLIST;
    protected volatile ArrayList<ClassMetaDataBinding> classMetaDataBindings = UnmodifiableEmptyCollections.EMPTY_ARRAYLIST;
    protected SimpleMetaData defaultMetaData = new SimpleMetaData();
    protected MethodMetaData methodMetaData = new MethodMetaData();
    protected FieldMetaData fieldMetaData = new FieldMetaData();
    protected SimpleMetaData classMetaData = new SimpleMetaData();
    protected ConstructorMetaData constructorMetaData = new ConstructorMetaData();
    //Does not seem to be used
    //protected HashMap classAnnotations = UnmodifiableEmptyCollections.EMPTY_HASHMAP;
    protected AnnotationRepository annotations = new AnnotationRepository();
    protected boolean doesHaveAspects = false;
 
    protected String name;
    protected ConcurrentHashMap aspects = new ConcurrentHashMap();
    protected HashMap adviceInterceptors = new HashMap();
    protected volatile CopyOnWriteArraySet perInstanceAspectDefinitions = UnmodifiableEmptyCollections.EMPTY_COPYONWRITE_ARRAYSET;
    protected volatile ConcurrentHashMap perInstanceJoinpointAspectDefinitions = UnmodifiableEmptyCollections.EMPTY_CONCURRENT_HASHMAP;
 
    static Class cl = java.lang.String.class;
    protected volatile TLongObjectHashMap advisedMethods = UnmodifiableEmptyCollections.EMPTY_TLONG_OBJECT_HASHMAP;
    // The method signatures are sorted at transformation and load time to
    // make sure the tables line up.
    //Common sense suggests that this should be lazily initialised for generated advisors, profiling shows that is a major performance hit...
    /** @deprecated use methodInfos instead */
    protected TLongObjectHashMap methodInterceptors = new TLongObjectHashMap();
    protected MethodInterceptors methodInfos = new MethodInterceptors(this);;
    protected AspectManager manager;
    protected Class clazz = null;
    protected Constructor[] constructors;
 
    /** @deprecated Use constructorInfos instead */
    protected Interceptor[][] constructorInterceptors;
    protected ConstructorInfo[] constructorInfos; //This should replace constructorInterceptors
 
    /** @deprecated Use constructorInfos instead */
    protected Interceptor[][] constructionInterceptors;
    protected ConstructionInfo[] constructionInfos;
 
 
    /** The meta data */
    private MetaData metadata;
 
    public Advisor(String name, AspectManager manager)
    {
       this.name = name;
       this.manager = manager;
    }
 
    public Constructor[] getConstructors()
    {
       return constructors;
    }
 
    /** @deprecated Use getConstructorInfos instead*/
    public Interceptor[][] getConstructorInterceptors()
    {
       return constructorInterceptors;
    }
 
    public ConstructorInfo[] getConstructorInfos()
    {
       return constructorInfos;
    }
 
    /** @deprecated Use getConstructionInfos instead*/
    public Interceptor[][] getConstructionInterceptors()
    {
       return constructionInterceptors;
    }
 
    public ConstructionInfo[] getConstructionInfos()
    {
       return constructionInfos;
    }
 
    /**
     * @deprecated Need a better mechanism to override the
     *             methods seen by pointcuts, e.g. those provided
     *             by a "proxy advisor"
     */
    public Method[] getAllMethods()
    {
       return null;
    }
 
    public AspectManager getManager()
    {
       return manager;
    }
 
    /**
     * For use by generated advisors. They will explicitly set the manager
     * @param name
     */
    protected void setManager(AspectManager manager)
    {
       this.manager = manager;
    }
 
 
    public List getClassMetadataBindings()
    {
       return classMetaDataBindings;
    }
 
    public SimpleMetaData getClassMetaData()
    {
       return classMetaData;
    }
 
    public SimpleMetaData getDefaultMetaData()
    {
       return defaultMetaData;
    }
 
    public MethodMetaData getMethodMetaData()
    {
       return methodMetaData;
    }
 
    public FieldMetaData getFieldMetaData()
    {
       return fieldMetaData;
    }
 
    public ConstructorMetaData getConstructorMetaData()
    {
       return constructorMetaData;
    }
 
    /* (non-Javadoc)
     * @see org.jboss.aop.AdvisorIF#deployAnnotationOverrides()
     */
    public void deployAnnotationOverrides()
    {
       List annotationOverrides = getManager().getAnnotationOverrides();
       if (annotationOverrides != null)
       {
          for (int i = 0; i < annotationOverrides.size(); ++i)
          {
             AnnotationIntroduction introduction = (AnnotationIntroduction) annotationOverrides.get(i);
             deployAnnotationOverride(introduction);
          }
       }
    }
 
    public void deployAnnotationOverride(AnnotationIntroduction introduction)
    {
       if (System.getSecurityManager() == null)
       {
          DeployAnnotationOverrideAction.NON_PRIVILEGED.deploy(this, introduction);
       }
       else
       {
          DeployAnnotationOverrideAction.PRIVILEGED.deploy(this, introduction);
       }
    }
 
    public void doDeployAnnotationOverride(AnnotationIntroduction introduction)
    {
       if (introduction.matches(this, clazz))
       {
          annotations.addClassAnnotation(introduction.getAnnotation().getIdentifier(), introduction.getOriginalAnnotationExpr());
       }
 
       Class theClass = clazz;
 
       deployMethodAnnotationOverrides(theClass, introduction);
       Field[] fields = theClass.getDeclaredFields();
       for (int i = 0; i < fields.length; i++)
       {
          if (introduction.matches(this, fields[i]))
          {
             annotations.addAnnotation(fields[i], introduction.getAnnotation().getIdentifier(), introduction.getOriginalAnnotationExpr());
          }
       }
       Constructor[] cons = theClass.getDeclaredConstructors();
       for (int i = 0; i < cons.length; i++)
       {
          if (introduction.matches(this, cons[i]))
          {
             annotations.addAnnotation(cons[i], introduction.getAnnotation().getIdentifier(), introduction.getOriginalAnnotationExpr());
          }
       }
    }
 
    protected void initializeInterfaceIntroductions(Class theClass)
    {
       manager.applyInterfaceIntroductions(this, theClass);
    }
 
    protected void deployMethodAnnotationOverrides(Class theClass, AnnotationIntroduction introduction)
    {
       if (theClass.getSuperclass() != null)
       {
          deployMethodAnnotationOverrides(theClass.getSuperclass(), introduction);
       }
       Method[] methods = theClass.getDeclaredMethods();
       for (int i = 0; i < methods.length; i++)
       {
          if (introduction.matches(this, methods[i]))
          {
             annotations.addAnnotation(methods[i], introduction.getAnnotation().getIdentifier(), introduction.getOriginalAnnotationExpr());
          }
       }
    }
 
 
    public AnnotationRepository getAnnotations()
    {
       return annotations;
    }
 
    public Object resolveAnnotation(Class annotation)
    {
       if (metadata != null)
       {
          Object value = metadata.getAnnotation(annotation);
          if (value != null) return value;
       }
 
       if (annotations.isDisabled(annotation))
          return null;
 
       Object value = annotations.resolveClassAnnotation(annotation);
       if (clazz == null) return null;
       if (value == null && metadata == null)
       {
          value = AnnotationElement.getVisibleAnnotation(clazz, annotation);
       }
       return value;
    }
 
    public boolean hasAnnotation(String annotation)
    {
       return hasAnnotation(clazz, annotation);
    }
 
    public boolean hasAnnotation(Class tgt, String annotation)
    {
       return hasAnnotation(tgt, annotation, null);
    }
 
    public boolean hasAnnotation(Class tgt, Class annotation)
    {
       return hasAnnotation(tgt, null, annotation);
    }
 
    private boolean hasAnnotation(Class tgt, String annotation, Class annotationClass)
    {
       if (annotation == null && annotationClass == null)
       {
          throw new RuntimeException("annotation or annotationClass must be passed in");
       }
 
       try
       {
          if (metadata != null)
          {
             if (annotationClass == null)
             {
                ClassLoader cl = SecurityActions.getClassLoader(tgt);
                if (cl == null)
                {
                   cl = SecurityActions.getContextClassLoader();
                }
                annotationClass = cl.loadClass(annotation);
             }
             if (annotationClass != null && metadata.isAnnotationPresent(annotationClass)) return true;
          }
       }
       catch (ClassNotFoundException e)
       {
          //The "annotation" is probably aop metadata for which there will be no corresponding class
       }
 
       if (annotation == null)
       {
          annotation = annotationClass.getName();
       }
 
       if (annotations.hasClassAnnotation(annotation)) return true;
       if (tgt == null) return false;
       try
       {
          if (metadata == null)
          {
             return AnnotationElement.isAnyAnnotationPresent(tgt, annotation);
          }
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
       }
       return false;
    }
 
    public Object resolveAnnotation(Method m, Class annotation)
    {
       return resolveAnnotation(0, m, annotation);
    }
 
    public Object resolveAnnotation(long hash, Method m, Class annotation)
    {
       if (metadata != null)
       {
          MethodSignature signature = new MethodSignature(m.getName(), m.getParameterTypes());
          MetaData methodMD = metadata.getComponentMetaData(signature);
          if (methodMD != null)
          {
             Object val = methodMD.getAnnotation(annotation);
             if (val != null) return val;
          }
       }
 
       if (annotations.isDisabled(m,annotation))
          return null;
 
       Object value = annotations.resolveAnnotation(m, annotation);
       if (value == null && metadata == null) 
       {
          value = AnnotationElement.getVisibleAnnotation(m, annotation);
       }
       return value;
    }
 
    public Object resolveAnnotation(Method m, Class[] annotationChoices)
    {
       for (Class ann : annotationChoices)
       {
          Object val = resolveAnnotation(m, annotationChoices);
          if (val != null) return val;
       }
       return null;
    }
 
    public Object resolveAnnotation(Field f, Class annotation)
    {
       Object value = null;
       if (metadata != null)
       {
          FieldSignature signature = new FieldSignature(f);
          MetaData fieldMD = metadata.getComponentMetaData(signature);
          if (fieldMD != null)
          {
             value = fieldMD.getAnnotation(annotation);
             if (value != null) return value;
          }
       }
       
       value = annotations.resolveAnnotation(f, annotation);
       if (value == null && metadata == null)
       {
          value = AnnotationElement.getVisibleAnnotation(f, annotation);
       }
       return value;
    }
 
    public Object resolveAnnotation(Constructor c, Class annotation)
    {
       Object value = null;
       if (metadata != null)
       {
          ConstructorSignature signature = new ConstructorSignature(c);
          MetaData conMD = metadata.getComponentMetaData(signature);
          if (conMD != null)
          {
             value = conMD.getAnnotation(annotation);
             if (value != null) return value;
          }
       }
       
       value = annotations.resolveAnnotation(c, annotation);
       if (value == null && metadata == null)
       {
          value = AnnotationElement.getVisibleAnnotation(c, annotation);
       }
       return value;
    }
 
    public boolean hasAnnotation(Method m, String annotation)
    {
       return hasAnnotation(0, m, annotation, null);
    }
 
    public boolean hasAnnotation(Method m, Class annotation)
    {
       return hasAnnotation(0, m, null, annotation);
    }
 
    private boolean hasAnnotation(long hash, Method m, String annotation, Class annotationClass)
    {
       if (annotation == null && annotationClass == null)
       {
          throw new RuntimeException("annotation or annotationClass must be passed in");
       }
 
       if (annotation == null)
       {
          annotation = annotationClass.getName();
       }
       if (metadata != null)
       {
         if (hasJoinPointAnnotationFromStringName(m.getDeclaringClass(), new MethodSignature(m), annotation))
          {
             return true;
          }
       }
 
       if (annotations.hasAnnotation(m, annotation)) return true;
       try
       {
          if (metadata == null)
          {
             return AnnotationElement.isAnyAnnotationPresent(m, annotation);
          }
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
       }
       return false;
    }
 
    public boolean hasAnnotation(Field m, String annotation)
    {
       if (metadata != null)
       {
          if (hasJoinPointAnnotationFromStringName(m.getDeclaringClass(), new FieldSignature(m), annotation))
          {
             return true;
          }
       }
       if (annotations.hasAnnotation(m, annotation)) return true;
       try
       {
          if (metadata == null)
          {
             return AnnotationElement.isAnyAnnotationPresent(m, annotation);
          }
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
       }
       return false;
    }
 
    public boolean hasAnnotation(Constructor<?> m, String annotation)
    {
       if (metadata != null)
       {
          if (hasJoinPointAnnotationFromStringName(m.getDeclaringClass(), new ConstructorSignature(m), annotation))
          {
             return true;
          }
       }
       if (annotations.hasAnnotation(m, annotation)) return true;
       try
       {
          if (metadata == null)
          {
             return AnnotationElement.isAnyAnnotationPresent(m, annotation);
          }
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
       }
       return false;
    }
 
    private boolean hasJoinPointAnnotationFromStringName(Class declaringClass, org.jboss.metadata.spi.signature.Signature sig, String annotationName)
    {
       try
       {
          if (metadata != null)
          {
             ClassLoader cl = SecurityActions.getClassLoader(declaringClass);
             if (cl == null)
             {
                cl = SecurityActions.getContextClassLoader();
             }
             if (cl != null)
             {
                Class annotationClass = cl.loadClass(annotationName);
                if (annotationClass != null)
                {
                  MetaData md = metadata.getComponentMetaData(sig);
                  if (md != null)
                  {
                     if (md.isAnnotationPresent(annotationClass))
                        return true;
                  }
                }
             }
          }
       }
       catch (ClassNotFoundException e)
       {
          //The "annotation" is probably aop metadata for which there will be no corresponding class
       }
       catch(Exception e)
       {
          throw new RuntimeException(e);
       }
       return false;
    }
    
    public boolean hasAnnotation(CtClass clazz, String annotation)
    {
       if (annotations.hasClassAnnotation(annotation)) return true;
       try
       {
          return AnnotationElement.isAnyAnnotationPresent(clazz, annotation);
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
       }
    }
 
    public boolean hasAnnotation(CtMethod member, String annotation)
    {
       // todo these are here so that we can chain configuration domains
       if (annotations.hasAnnotation(member, annotation)) return true;
       return AnnotationElement.isAnyAnnotationPresent(member, annotation);
    }
 
    public boolean hasAnnotation(CtField member, String annotation)
    {
       // todo these are here so that we can chain configuration domains
       if (annotations.hasAnnotation(member, annotation)) return true;
       return AnnotationElement.isAnyAnnotationPresent(member, annotation);
    }
 
    public boolean hasAnnotation(CtConstructor member, String annotation)
    {
       // todo these are here so that we can chain configuration domains
       if (annotations.hasAnnotation(member, annotation)) return true;
       return AnnotationElement.isAnyAnnotationPresent(member, annotation);
    }
 
    /**
     * Get the metadata
     * 
     * @return the metadata
     */
    public MetaData getMetadata()
    {
       return metadata;
    }
 
    /**
     * Set the metadata
     * 
     * FIXME why does this have java.lang.Object signature?
     * @param metadata the metadata
     */
    public void setMetadata(MetaData metadata)
    {
       this.metadata = metadata;
    }
 
    public String getName()
    {
       return name;
    }
 
    public final boolean hasAspects()
    {
       return doesHaveAspects;
    }
 
    public synchronized void removeAdviceBinding(AdviceBinding binding)
    {
       adviceBindings.remove(binding);
       rebuildInterceptors();
       doesHaveAspects = adviceBindings.size() > 0;
    }
 
    public synchronized void removeAdviceBindings(ArrayList bindings)
    {
       adviceBindings.removeAll(bindings);
       rebuildInterceptors();
       doesHaveAspects = adviceBindings.size() > 0;
    }
 
    /**
     * a new binding has been added to the AspectManager, recalculate interceptors
     */
    public synchronized void newBindingAdded()
    {
       rebuildInterceptors();
       doesHaveAspects = adviceBindings.size() > 0;
    }
 
    public ArrayList<InterfaceIntroduction> getInterfaceIntroductions()
    {
       return interfaceIntroductions;
    }
 
    public synchronized void addInterfaceIntroduction(InterfaceIntroduction pointcut)
    {
       initInterfaceIntroductionsList();      
       interfaceIntroductions.add(pointcut);
    }
 
    public synchronized void removeInterfaceIntroduction(InterfaceIntroduction pointcut)
    {
       interfaceIntroductions.remove(pointcut);
    }
 
    protected abstract void rebuildInterceptors();
 
    ////////////////////////////////
    // Metadata.  Metadata will be used for things like Transaction attributes (Required, RequiresNew, etc...)
    //
 
    public abstract void addClassMetaData(ClassMetaDataBinding data);
 
    public abstract void removeClassMetaData(ClassMetaDataBinding data);
 
    // This is aspect stuff.  Aspect again, is a class that encapsulates advices
    
    public Object getPerVMAspect(AspectDefinition def)
    {
       return getManager().getPerVMAspect(def);
    }
    
    public void addPerInstanceAspect(AspectDefinition def)
    {
       initPerInstanceAspectDefinitionsSet();
       perInstanceAspectDefinitions.add(def);
       def.registerAdvisor(this);
    }
 
    public void removePerInstanceAspect(AspectDefinition def)
    {
       perInstanceAspectDefinitions.remove(def);
    }
 
    public Set getPerInstanceAspectDefinitions()
    {
       return perInstanceAspectDefinitions;
    }
 
    // This is aspect stuff.  Aspect again, is a class that encapsulates advices
 
    public void addPerInstanceJoinpointAspect(Joinpoint joinpoint, AspectDefinition def)
    {
       Set joinpoints = (Set) perInstanceJoinpointAspectDefinitions.get(def);
       if (joinpoints == null)
       {
          joinpoints = new CopyOnWriteArraySet();
          initPerInstanceJoinpointAspectDefinitionsMap();
          perInstanceJoinpointAspectDefinitions.put(def, joinpoints);
          def.registerAdvisor(this);
       }
       joinpoints.add(joinpoint);
    }
 
    void addPerInstanceJoinpointAspect(Set joinpoints, AspectDefinition def)
    {
       initPerInstanceJoinpointAspectDefinitionsMap();
       Set setJoinpoints = (Set) perInstanceJoinpointAspectDefinitions.get(def);
       if (setJoinpoints == null)
       {
          setJoinpoints = new CopyOnWriteArraySet();
          perInstanceJoinpointAspectDefinitions.put(def, setJoinpoints);
          def.registerAdvisor(this);
       }
       setJoinpoints.addAll(joinpoints);
    }
    
    public void removePerInstanceJoinpointAspect(AspectDefinition def)
    {
       perInstanceJoinpointAspectDefinitions.remove(def);
    }
 
    public Map getPerInstanceJoinpointAspectDefinitions()
    {
       return perInstanceJoinpointAspectDefinitions;
    }
 
    public Object getPerClassAspect(AspectDefinition def)
    {
       return aspects.get(def.getName());
    }
 
    public Object getPerClassAspect(String def)
    {
       return aspects.get(def);
    }
 
    public void addPerClassAspect(AspectDefinition def)
    {
       if (aspects.containsKey(def.getName())) return;
       Object aspect = def.getFactory().createPerClass(this);
       aspects.put(def.getName(), aspect);
       def.registerAdvisor(this);
    }
 
    public void removePerClassAspect(AspectDefinition def)
    {
       aspects.remove(def.getName());
       adviceInterceptors.remove(def);
    }
 
    public Interceptor getAdviceInterceptor(AspectDefinition def, String adviceName, Joinpoint joinpoint)
    {
       AdviceInterceptorKey key = new AdviceInterceptorKey(adviceName, joinpoint);
       synchronized (adviceInterceptors)
       {
          Map map = null;
          map = (Map) adviceInterceptors.get(def);
          if (map != null)
          {
             return (Interceptor) map.get(key);
          }
       }
       return null;
    }
 
    public void addAdviceInterceptor(AspectDefinition def, String adviceName, Interceptor interceptor, Joinpoint joinpoint)
    {
       synchronized (adviceInterceptors)
       {
          Map map = (Map) adviceInterceptors.get(def);
          if (map == null)
          {
             map = new HashMap();
             adviceInterceptors.put(def, map);
          }
          map.put(adviceName, interceptor);
       }
    }
 
 
    protected void createInterceptorChain(InterceptorFactory[] factories, ArrayList<Interceptor> newinterceptors, Joinpoint joinpoint)
    {
       for (int i = 0; i < factories.length; i++)
       {
          if (factories[i].getType().isGeneratedOnly())
          {
             throw new RuntimeException("Before/After/Throwing is only supported for Generated Advisors");
          }
          if (factories[i].isDeployed()) newinterceptors.add(factories[i].create(this, joinpoint));
       }
    }
 
    protected void resolveMethodPointcut(AdviceBinding binding)
    {
       long[] keys = methodInfos.keys();
       for (int i = 0; i < keys.length; i++)
       {
          Method method = (Method) advisedMethods.get(keys[i]);
          PointcutMethodMatch match = binding.getPointcut().matchesExecution(this, method);
          
          if (match != null && match.isMatch())
          {
             adviceBindings.add(binding);
             if (AspectManager.verbose)
             {
                /*
                RepositoryClassLoader loader = (RepositoryClassLoader)clazz.getClassLoader();
                try
                {
                   System.err.println("method matched binding " + binding.getPointcut().getExpr() + " " + method.toString() + " " + loader.getObjectName());
                }
                catch (MalformedObjectNameException e)
                {
                   throw new RuntimeException(e);
                }
                */
                System.err.println("[debug] method matched binding: " + method.toString());
             }
             binding.addAdvisor(this);
             MethodMatchInfo info = methodInfos.getMatchInfo(keys[i]);
             info.addMatchedBinding(binding, match);
          }
       }
    }
 
    protected void lockWriteChain(MethodInterceptors methodInterceptors)
    {
       Object[] methodMatchInfos = methodInterceptors.infos.getValues();
       for (int i = 0; i < methodMatchInfos.length; i++)
       {
          MethodMatchInfo methodMatchInfo = (MethodMatchInfo) methodMatchInfos[i];
          methodMatchInfo.getInfo().getInterceptorChainReadWriteLock().writeLock().lock();
       }
    }
    
    protected void unlockWriteChain(MethodInterceptors methodInterceptors)
    {
       Object[] methodMatchInfos = methodInterceptors.infos.getValues();
       for (int i = 0; i < methodMatchInfos.length; i++)
       {
          MethodMatchInfo methodMatchInfo = (MethodMatchInfo) methodMatchInfos[i];
          methodMatchInfo.getInfo().getInterceptorChainReadWriteLock().writeLock().unlock();
       }
    }
    
    protected void resetChain(MethodInterceptors methodInterceptors)
    {
       Object[] methodMatchInfos = methodInterceptors.infos.getValues();
       for (int i = 0; i < methodMatchInfos.length; i++)
       {
          MethodMatchInfo methodMatchInfo = (MethodMatchInfo) methodMatchInfos[i];
          if (methodMatchInfo.bindings != null)
          {
             methodMatchInfo.bindings.clear();
          }
          if (methodMatchInfo.pointcutMethodMatches != null)
          {
             methodMatchInfo.pointcutMethodMatches.clear();
          }
          methodMatchInfo.getInfo().clear();
       }
    }
    
    protected void finalizeMethodChain()
    {
       long[] keys = methodInfos.keys();
       for (int i = 0; i < keys.length; i++)
       {
          MethodMatchInfo matchInfo = methodInfos.getMatchInfo(keys[i]);
          matchInfo.populateBindings();
          MethodInfo info = matchInfo.getInfo();
          ArrayList<Interceptor> list = info.getInterceptorChain();
          Interceptor[] interceptors = null;
          if (list.size() > 0)
          {
             interceptors = applyPrecedence(list.toArray(new Interceptor[list.size()]));
          }
          info.setInterceptors(interceptors);
       }
    }
 
    public InvocationResponse dynamicInvoke(Object target, Invocation invocation)
    throws Throwable
    {
       // Only need to set Method because fields will already have been set.
       if (invocation instanceof MethodInvocation)
       {
          Interceptor[] aspects = null;
          MethodInvocation methodInvocation = (MethodInvocation) invocation;
          long hash = methodInvocation.getMethodHash();
          MethodInfo info = (MethodInfo) methodInfos.getMethodInfo(hash);
          aspects = info.getInterceptors();
          if (aspects == null) aspects = new Interceptor[0];
          if (target != null && target instanceof Advised)
          {
             InstanceAdvised advised = (InstanceAdvised) target;
             aspects = advised._getInstanceAdvisor().getInterceptors(aspects);
          }
          MethodInvocation nextInvocation = new MethodInvocation(info, aspects);
          nextInvocation.setMetaData(invocation.getMetaData());
          nextInvocation.setTargetObject(target);
          nextInvocation.setArguments(methodInvocation.getArguments());
          nextInvocation.setAdvisor(this);
          InvocationResponse response = new InvocationResponse(nextInvocation.invokeNext());
          response.setContextInfo(nextInvocation.getResponseContextInfo());
          return response;
       }
       throw new RuntimeException("dynamic field invocations not supported yet!");
    }
 
    public Class getClazz()
    {
       return clazz;
    }
 
    void setClazz(Class clazz)
    {
       this.clazz = clazz;
    }
 
    public static String getSimpleName(Class clazz)
    {
       String name = clazz.getName();
       int lastIndex = name.lastIndexOf('.');
       if (lastIndex < 0)
       {
          return name;
       }
 
       return name.substring(lastIndex + 1);
    }
 
    protected void initializeConstructorChain()
    {
       if (clazz != null && constructors == null)
       {
           constructors = clazz.getDeclaredConstructors();
       }
 
       this.constructorInfos = new ConstructorInfo[constructors.length];
       for (int i = 0; i < constructors.length; i++)
       {
          final ConstructorInfo info = new ConstructorInfo();
          info.setConstructor(constructors[i]);
          info.setIndex(i);
          try
          {
             final String name = ConstructorExecutionTransformer.constructorFactory(getSimpleName(clazz));
             final Class[] types = constructors[i].getParameterTypes();
             Method method = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>()
             {
                public Method run() throws Exception
                {
                   return clazz.getDeclaredMethod(name, types);
                }
             });
             info.setWrapper(method);
          }
          catch (PrivilegedActionException e1)
          {
             Exception e = e1.getException();
             if (e instanceof NoSuchMethodException == false)
                throw new NestedRuntimeException(e);
          }
 
          info.setAdvisor(this);
          constructorInfos[i] = info;
 
          try
          {
             final String name = ConstructorExecutionTransformer.getConstructorInfoFieldName(getSimpleName(clazz), i);
             AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
             {
                public Object run() throws Exception
                {
                   Field infoField = clazz.getDeclaredField(name);
                   infoField.setAccessible(true);
                   infoField.set(null, new WeakReference<ConstructorInfo>(info));
                   return null;
                }
             });
          }
          catch (PrivilegedActionException e1)
          {
             Exception e = e1.getException();
             if (e instanceof NoSuchFieldException == false)
                throw new NestedRuntimeException(e);
          }
       }
    }
 
    protected void initializeConstructionChain()
    {
       this.constructionInfos = new ConstructionInfo[constructors.length];
       for (int i = 0; i < constructors.length; i++)
       {
          ConstructionInfo info = new ConstructionInfo();
          info.setConstructor(constructors[i]);
          info.setIndex(i);
          info.setAdvisor(this);
          constructionInfos[i] = info;
 
          try
          {
             Field infoField = clazz.getDeclaredField(ConstructionTransformer.getConstructionInfoFieldName(getSimpleName(clazz), i));
             infoField.setAccessible(true);
             infoField.set(null, new WeakReference<ConstructionInfo>(info));
          }
          catch (NoSuchFieldException e)
          {
             // ignore, method may not be advised.
          }
          catch (IllegalAccessException e)
          {
             throw new RuntimeException(e);
          }
       }
    }
 
    protected void finalizeChain(JoinPointInfo[] infos)
    {
       for (int i = 0; i < infos.length; i++)
       {
          JoinPointInfo info = infos[i];
          ArrayList<Interceptor> list = info.getInterceptorChain();
          Interceptor[] interceptors = null;
          if (list.size() > 0)
          {
           interceptors = applyPrecedence(list.toArray(new Interceptor[list.size()]));
          }
          info.setInterceptors(interceptors);
       }
    }
    
    protected void lockWriteChain(JoinPointInfo[] infos)
    {
       for (int i = 0; i < infos.length; i++)
       {
          infos[i].getInterceptorChainReadWriteLock().writeLock().lock();
       }
    }
    
    protected void unlockWriteChain(JoinPointInfo[] infos)
    {
       for (int i = 0; i < infos.length; i++)
       {
          infos[i].getInterceptorChainReadWriteLock().writeLock().unlock();
       }
    }
    
    protected void resetChain(JoinPointInfo[] infos)
    {
       for (int i = 0; i < infos.length; i++)
       {
          infos[i].clear();
       }
    }
    
    
 
 //   protected void finalizeConstructionChain(ArrayList newConstructionInfos)
 //   {
 //      for (int i = 0; i < newConstructionInfos.size(); i++)
 //      {
 //         ConstructionInfo info = (ConstructionInfo) newConstructionInfos.get(i);
 //         ArrayList list = info.getInterceptorChain();
 //         Interceptor[] interceptors = null;
 //         if (list.size() > 0)
 //         {
 //          interceptors = applyPrecedence((Interceptor[]) list.toArray(new Interceptor[list.size()]));
 //         }
 //         info.setInterceptors(interceptors);
 //      }
 //   }
 
    protected void resolveConstructorPointcut(AdviceBinding binding)
    {
       for (int i = 0; i < constructors.length; i++)
       {
          Constructor constructor = constructors[i];
          if (binding.getPointcut().matchesExecution(this, constructor))
          {
             if (AspectManager.verbose) System.err.println("[debug] constructor matched binding: " + constructor);
             adviceBindings.add(binding);
             binding.addAdvisor(this);
             pointcutResolved(constructorInfos[i], binding, new ConstructorJoinpoint(constructor));
          }
       }
    }
 
    protected void resolveConstructionPointcut(AdviceBinding binding)
    {
       if (constructionInfos.length > 0)
       {
          for (int i = 0; i < constructionInfos.length ;i++)
          {
             ConstructionInfo info = constructionInfos[i];
             Constructor constructor = info.getConstructor();
             if (binding.getPointcut().matchesConstruction(this, constructor))
             {
                if (AspectManager.verbose) System.err.println("[debug] construction matched binding: " + constructor);
                adviceBindings.add(binding);
                binding.addAdvisor(this);
                pointcutResolved(info, binding, new ConstructorJoinpoint(constructor));
             }
          }
       }
    }
 
    /** @deprecated We should just be using xxxxInfos */
    protected void populateInterceptorsFromInfos()
    {
       constructorInterceptors = new Interceptor[constructorInfos.length][];
       for (int i = 0 ; i < constructorInfos.length ; i++)
       {
          constructorInterceptors[i] = constructorInfos[i].getInterceptors();
       }
    }
 
    /**
     * Default implementation adds interceptorChain directly to the info.
     * GeneratedClassAdvisor overrides this
     */
    protected void pointcutResolved(JoinPointInfo info, AdviceBinding binding, Joinpoint joinpoint)
    {
       ArrayList<Interceptor> curr = info.getInterceptorChain();
       if (binding.getCFlow() != null)
       {
          ArrayList<Interceptor> cflowChain = new ArrayList<Interceptor>();
          createInterceptorChain(binding.getInterceptorFactories(), cflowChain, joinpoint);
          Interceptor[] cflowInterceptors = (Interceptor[]) cflowChain.toArray(new Interceptor[cflowChain.size()]);
          curr.add(new CFlowInterceptor(binding.getCFlowString(), binding.getCFlow(), cflowInterceptors));
       }
       else
       {
          createInterceptorChain(binding.getInterceptorFactories(), curr, joinpoint);
       }
    }
 
    Interceptor[] applyPrecedence(Interceptor[] interceptors)
    {
       return PrecedenceSorter.applyPrecedence(interceptors, manager);
    }
 
    /**
     * Whether the type of advisor supports matching on pointcut expression, where the method is defined in a superclass only,
     * while the pointcut expression class matches the subclass. This is currently only supported for generated advisors, due to
     * the new weaving model. So (with generated advisors) if we have<BR/>
     * <code><BR/>
     * public class Super {<BR/>
     * &nbsp;&nbsp;void method(){}<BR/>
     * }<BR/>
     * <BR/>
     * public class Sub etxends Super {<BR/>
     * }<BR/>
     * </code>
     * and<BR/>
     * <code>
     *    &lt;bind pointcut="execution(* Super->method())"&gt;<BR/>
     *    &nbsp;&nbsp;&lt;interceptor class="A"/&gt;<BR/>
     *    &lt;/bind&gt;<BR/>
     *    &lt;bind pointcut="execution(* sub->method())"&gt;<BR/>
     *    &nbsp;&nbsp;&lt;interceptor class="B"/&gt;<BR/>
     *    &lt;/bind&gt;<BR/>
     * </code><BR/>
     * Super.method() will be intercepted by A only<BR/>
     * Sub.method() will be intercepted by A and B
     *
     */
    public boolean chainOverridingForInheritedMethods()
    {
       return false;
    }
 
    /**
     * @param overriding the new value of chainOverridingForInheritedMethods
     * @see Advisor#chainOverridingForInheritedMethods()
     */
    protected void setChainOverridingForInheritedMethods(boolean overriding)
    {
       //Implemented by base-classes
       throw new NotImplementedException("Not a legal operation for Advisor");
    }
    
    interface DeployAnnotationOverrideAction
    {
       void deploy(Advisor advisor, AnnotationIntroduction introduction);
 
       DeployAnnotationOverrideAction PRIVILEGED = new DeployAnnotationOverrideAction()
       {
          public void deploy(final Advisor advisor, final AnnotationIntroduction introduction)
          {
             try
             {
                AccessController.doPrivileged(new PrivilegedExceptionAction()
                {
                   public Object run()
                   {
                      advisor.doDeployAnnotationOverride(introduction);
                      return null;
                   }
                });
             }
             catch (PrivilegedActionException e)
             {
                Exception ex = e.getException();
                if (ex instanceof RuntimeException)
                {
                   throw (RuntimeException)ex;
                }
                throw new RuntimeException(ex);
             }
          }
       };
 
       DeployAnnotationOverrideAction NON_PRIVILEGED = new DeployAnnotationOverrideAction()
       {
          public void deploy(Advisor advisor, AnnotationIntroduction introduction)
          {
             advisor.doDeployAnnotationOverride(introduction);
          }
       };
    }
    
    public void cleanup()
    {
       //AspectDefinitions have strong links back to us
       for(Iterator it = perInstanceAspectDefinitions.iterator() ; it.hasNext() ; )
       {
          AspectDefinition def = (AspectDefinition)it.next();
          removePerInstanceAspect(def);
          def.unregisterAdvisor(this);
       }
       
       for(Iterator it = perInstanceJoinpointAspectDefinitions.keySet().iterator() ; it.hasNext() ; )
       {
          AspectDefinition def = (AspectDefinition)it.next();
          removePerInstanceJoinpointAspect(def);
          def.unregisterAdvisor(this);
       }
 
       AspectDefinition[] defs = (AspectDefinition[])adviceInterceptors.keySet().toArray(new AspectDefinition[adviceInterceptors.size()]);
       for(int i = 0 ; i < defs.length ; i++)
       {
          if (defs[i].getScope() == Scope.PER_CLASS)
          {
             removePerClassAspect(defs[i]);
             defs[i].unregisterAdvisor(this);
          }
       }
             
       if (methodInfos != null)
       {
          methodInfos. clear();
       }
    }
    
    /**
     * Lock for write
     */
    protected void lockWrite()
    {
       lock.writeLock().lock();
    }
 
    /**
     * Unlock for write
     */
    protected void unlockWrite()
    {
       lock.writeLock().unlock();
    }
 
    protected void initInterfaceIntroductionsList()
    {
       if (interfaceIntroductions == UnmodifiableEmptyCollections.EMPTY_ARRAYLIST)
       {
          lockWrite();
          try
          {
             if (interfaceIntroductions == UnmodifiableEmptyCollections.EMPTY_ARRAYLIST)
             {
                interfaceIntroductions = new ArrayList<InterfaceIntroduction>();
             }
          }
          finally
          {
             unlockWrite();
          }
       }
    }
    
    protected void initClassMetaDataBindingsList()
    {
       if (classMetaDataBindings == UnmodifiableEmptyCollections.EMPTY_ARRAYLIST)
       {
          lockWrite();
          try
          {
             if (classMetaDataBindings == UnmodifiableEmptyCollections.EMPTY_ARRAYLIST)
             {
                classMetaDataBindings = new ArrayList();
             }
          }
          finally
          {
             unlockWrite();
          }
       }
    }
    
    protected void initPerInstanceAspectDefinitionsSet()
    {
       if (perInstanceAspectDefinitions == UnmodifiableEmptyCollections.EMPTY_COPYONWRITE_ARRAYSET)
       {
          lockWrite();
          try
          {
             if (perInstanceAspectDefinitions == UnmodifiableEmptyCollections.EMPTY_COPYONWRITE_ARRAYSET)
             {
                perInstanceAspectDefinitions = new CopyOnWriteArraySet();
             }
          }
          finally
          {
             unlockWrite();
          }
       }
    }
    
    protected void initPerInstanceJoinpointAspectDefinitionsMap()
    {
       if (perInstanceJoinpointAspectDefinitions == UnmodifiableEmptyCollections.EMPTY_CONCURRENT_HASHMAP)
       {
          lockWrite();
          try
          {
             if (perInstanceJoinpointAspectDefinitions == UnmodifiableEmptyCollections.EMPTY_CONCURRENT_HASHMAP)
             {
                perInstanceJoinpointAspectDefinitions = new ConcurrentHashMap();
             }
          }
          finally
          {
             unlockWrite();
          }
       }
    }
 
    protected void initAdvisedMethodsMap()
    {
       if (advisedMethods == UnmodifiableEmptyCollections.EMPTY_TLONG_OBJECT_HASHMAP)
       {
          lockWrite();
          try
          {
             if (advisedMethods == UnmodifiableEmptyCollections.EMPTY_TLONG_OBJECT_HASHMAP)
             {
                advisedMethods = new TLongObjectHashMap();
             }
          }
          finally
          {
             unlockWrite();
          }
       }
    }
 }
