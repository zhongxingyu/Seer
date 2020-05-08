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
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Field;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.WeakHashMap;
 import java.util.List;
 
 import org.jboss.aop.advice.AdviceBinding;
 import org.jboss.aop.advice.AdviceStack;
 import org.jboss.aop.advice.AspectDefinition;
 import org.jboss.aop.advice.AspectFactory;
 import org.jboss.aop.advice.AspectFactoryWithClassLoader;
 import org.jboss.aop.advice.DynamicCFlowDefinition;
 import org.jboss.aop.advice.InterceptorFactory;
 import org.jboss.aop.advice.PrecedenceDef;
 import org.jboss.aop.advice.PrecedenceDefEntry;
 import org.jboss.aop.advice.PrecedenceSorter;
 import org.jboss.aop.advice.Scope;
 import org.jboss.aop.classpool.AOPClassPoolRepository;
 import org.jboss.aop.classpool.AOPScopedClassLoaderHelper;
 import org.jboss.aop.instrument.GeneratedAdvisorInstrumentor;
 import org.jboss.aop.instrument.Instrumentor;
 import org.jboss.aop.instrument.InstrumentorFactory;
 import org.jboss.aop.instrument.TransformerCommon;
 import org.jboss.aop.introduction.AnnotationIntroduction;
 import org.jboss.aop.introduction.InterfaceIntroduction;
 import org.jboss.aop.metadata.ClassMetaDataBinding;
 import org.jboss.aop.metadata.ClassMetaDataLoader;
 import org.jboss.aop.metadata.SimpleClassMetaDataLoader;
 import org.jboss.aop.pointcut.CFlowStack;
 import org.jboss.aop.pointcut.DeclareDef;
 import org.jboss.aop.pointcut.DynamicCFlow;
 import org.jboss.aop.pointcut.Pointcut;
 import org.jboss.aop.pointcut.PointcutExpression;
 import org.jboss.aop.pointcut.PointcutInfo;
 import org.jboss.aop.pointcut.PointcutStats;
 import org.jboss.aop.pointcut.Typedef;
 import org.jboss.aop.pointcut.ast.ClassExpression;
 import org.jboss.util.collection.WeakValueHashMap;
 import org.jboss.util.loading.Translatable;
 import org.jboss.util.loading.Translator;
 import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.scopedpool.ScopedClassPool;
 import javassist.scopedpool.ScopedClassPoolFactory;
 
 /**
  * This singleton class manages all pointcuts and metadata.
  * Coders can access it via the AspectManager.instance() method.
  * <p/>
  * It is also the middle man between the ClassLoader and
  * the actual class instrumentation as well.
  * <p/>
  * App Developers that want to create and apply, interceptors,
  * pointcuts, or metadata at runtime can also use this object
  * to do that.
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @version $Revision$
  */
 public class AspectManager
         implements Translator
 {
 
    // Attributes ---------------------------------------------------
    protected final WeakHashMap advisors = new WeakHashMap();
    /** A map of domains by classloader, maintaned by the top level AspectManager */
    protected final WeakHashMap scopedClassLoaderDomains = new WeakHashMap();
 
    /** A map of domains by class, maintaned by the top level AspectManager */
    protected final WeakHashMap subDomainsPerClass = new WeakHashMap();
    
    /** A map of domains by name */
    protected final WeakValueHashMap subDomainsByName = new WeakValueHashMap();
 
    /** Each domain may have sub domains interested in changes happening in this manager/domain */
    protected final WeakHashMap subscribedSubDomains = new WeakHashMap();
 
    /** A queue for adding new subscribed subdomains to */
    protected final WeakHashMap subscribedSubDomainsQueue = new WeakHashMap();
    protected int subscribedDomainQueueRef;
 
    protected final LinkedHashMap interfaceIntroductions = new LinkedHashMap();
    protected final LinkedHashMap annotationIntroductions = new LinkedHashMap();
    protected final LinkedHashMap annotationOverrides = new LinkedHashMap();
    protected final LinkedHashMap bindings = new LinkedHashMap();
    protected final LinkedHashMap typedefs = new LinkedHashMap();
    protected final HashMap interceptorFactories = new HashMap();
    protected final Hashtable classMetaDataLoaders = new Hashtable();
    protected final HashMap interceptorStacks = new HashMap();
    protected final HashMap declares = new HashMap();
    protected final ConcurrentReaderHashMap cflowStacks = new ConcurrentReaderHashMap();
    protected final ConcurrentReaderHashMap dynamicCFlows = new ConcurrentReaderHashMap();
    protected final ConcurrentReaderHashMap aspectDefinitions = new ConcurrentReaderHashMap();
    protected final ConcurrentReaderHashMap perVMAspects = new ConcurrentReaderHashMap();
 
    /** class name prefixes to explicitly exclude unless contained in include. Maintained by top-level AspectManager */
    protected final ArrayList exclude = new ArrayList();
 
    /** class name prefixes to explicitly include, this overrides whatever was set in exclude. Maintained by top-level AspectManager */
    protected final ArrayList include = new ArrayList();
 
    /** A set of wildcard enabled classnames that will be ignored no matter if they have been included. Maintained by top-level AspectManager */
    protected final ArrayList ignore = new ArrayList();
 
    /** ClassExpressions built from ignore. Maintained by top-level AspectManager */
    protected ClassExpression[] ignoreExpressions = new ClassExpression[0];
 
    protected final LinkedHashMap pointcuts = new LinkedHashMap();
    // contains pointcuts-binding association info
    protected final LinkedHashMap pointcutInfos = new LinkedHashMap();
    // these fields represent whether there are certain pointcut types.
    // for performance reasons the transformers and binders can make a lot of us of this.
    protected boolean execution = false;
    protected boolean construction = false;
    protected boolean call = false;
    protected boolean within = false;
    protected boolean get = false;
    protected boolean set = false;
    protected boolean withincode = false;
    public static boolean classicOrder = false;
 
    protected final LinkedHashMap classMetaData = new LinkedHashMap();
    protected final HashMap containers = new HashMap();
    protected final LinkedHashMap precedenceDefs = new LinkedHashMap();
    protected PrecedenceDefEntry[] sortedPrecedenceDefEntries;
    protected WeavingStrategy weavingStrategy;
 
    protected DynamicAOPStrategy dynamicStrategy = new LoadInterceptedClassesStrategy();
    // indicates that the transformation process has begun
    protected boolean transformationStarted = false;
 
    //This will be set by the AspectManagerService if running in JBoss
    public static AOPScopedClassLoaderHelper scopedCLHelper;
    
    //Keeps track of if we need to convert references etc for a given class. Domains for scoped classloaders will have their own version of this
    protected static InterceptionMarkers interceptionMarkers = new InterceptionMarkers();
 
    // Static -------------------------------------------------------
 
    protected static AspectManager manager;
    public static boolean optimize = true;
    public static boolean debugClasses;//If true, the generated advisor instrumentor will output the generated classes
    public static ClassLoaderValidation classLoaderValidator;
 
    /**
     * logging switch.  We don't use log4j to avoid another heavy library
     */
    public static boolean verbose = false;
 
    public static synchronized AspectManager getTopLevelAspectManager()
    {
       if (scopedCLHelper == null)
       {
          //We are not running in jboss
          return instance();
       }
       ClassLoader topUcl = scopedCLHelper.getTopLevelJBossClassLoader();
       return instance(topUcl);
 
    }
 
    public static synchronized AspectManager instance()
    {
       return instance(Thread.currentThread().getContextClassLoader());
    }
 
    public static synchronized AspectManager instance(ClassLoader loadingClassLoader)
    {
       if (manager == null)
       {
          AccessController.doPrivileged(new PrivilegedAction()
          {
             public Object run()
             {
                String optimized = System.getProperty("jboss.aop.optimized", null);
                if (optimized != null)
                {
                   optimize = (new Boolean(optimized)).booleanValue();
                }
                String pruneit = System.getProperty("jboss.aop.prune", null);
                if (pruneit != null)
                {
                   AOPClassPoolRepository.getInstance().setPrune((new Boolean(pruneit)).booleanValue());
                }
                manager = new AspectManager();
                AOPClassPoolRepository.getInstance().setAspectManager(manager);
 
                if (!verbose)
                {
                   verbose = (new Boolean(System.getProperty("jboss.aop.verbose", "false"))).booleanValue();
                }
                String exclude = System.getProperty("jboss.aop.exclude", null);
                if (exclude != null)
                {
                   ArrayList list = new ArrayList();
                   StringTokenizer tokenizer = new StringTokenizer(exclude, ",");
                   while (tokenizer.hasMoreTokens())
                   {
                      list.add(tokenizer.nextToken().trim());
                   }
                   manager.setExclude(list);
                }
                String include = System.getProperty("jboss.aop.include", null);
                if (include != null)
                {
                   ArrayList list = new ArrayList();
                   StringTokenizer tokenizer = new StringTokenizer(include, ",");
                   while (tokenizer.hasMoreTokens())
                   {
                      list.add(tokenizer.nextToken().trim());
                   }
                   manager.setInclude(list);
                }
                String ignore = System.getProperty("jboss.aop.ignore", null);
                if (ignore != null)
                {
                   ArrayList list = new ArrayList();
                   StringTokenizer tokenizer = new StringTokenizer(ignore, ",");
                   while (tokenizer.hasMoreTokens())
                   {
                      list.add(tokenizer.nextToken().trim());
                   }
                   manager.setIgnore(list);
                }
 
                String instrument = System.getProperty("jboss.aop.instrumentor", null);
                InstrumentorFactory.initialise(instrument);
 
                String advisorName = System.getProperty("jboss.aop.advisor", null);
                AdvisorFactory.initialise(advisorName);
 
                String debugClass = System.getProperty("jboss.aop.debug.classes", null);
                if (debugClass != null)
                {
                   debugClasses = (new Boolean(debugClass)).booleanValue();
                }
 
                String classic = System.getProperty("jboss.aop.classicorder", null);
                if (classic != null)
                {
                   classicOrder = (new Boolean(classic)).booleanValue();
                }
 
 
                Deployment.deploy();
                return null;
             }
          });
       }
 
       if (scopedCLHelper != null)
       {
          ClassLoader scopedClassLoader = scopedCLHelper.ifScopedDeploymentGetScopedParentUclForCL(loadingClassLoader);
          if (scopedClassLoader != null)
          {
             Domain scopedManager = null;
             synchronized (AOPClassPoolRepository.getInstance().getRegisteredCLs())
             {
                scopedManager = (Domain)manager.scopedClassLoaderDomains.get(scopedClassLoader);
                if (scopedManager == null)
                {
                   scopedManager = scopedCLHelper.getScopedClassLoaderDomain(scopedClassLoader, manager);
                   if (verbose)
                   {
                      System.out.println("Created domain " + scopedManager + " for scoped deployment on: " +
                            loadingClassLoader + "; identifying scoped ucl: " + scopedClassLoader);
                   }
                   scopedManager.setInheritsBindings(true);
                   scopedManager.setInheritsDeclarations(true);
                   manager.scopedClassLoaderDomains.put(scopedClassLoader, scopedManager);
                }
             }
             return scopedManager;
          }
       }
       return manager;
    }
 
    public InterceptionMarkers getInterceptionMarkers()
    {
       return interceptionMarkers;
    }
 
    public LinkedHashMap getPointcuts()
    {
       return pointcuts;
    }
 
    public LinkedHashMap getPointcutInfos()
    {
       return pointcutInfos;
    }
 
    public CFlowStack getCFlowStack(String name)
    {
       return (CFlowStack) cflowStacks.get(name);
    }
 
    public void addCFlowStack(CFlowStack stack)
    {
       cflowStacks.put(stack.getName(), stack);
    }
 
    public void removeCFlowStack(String name)
    {
       cflowStacks.remove(name);
    }
 
    public DynamicCFlow getDynamicCFlow(String name)
    {
       DynamicCFlowDefinition def = (DynamicCFlowDefinition) dynamicCFlows.get(name);
 
       if (def != null)
       {
          return def.create();
       }
       return null;
    }
 
 
    /* (non-Javadoc)
     * @see org.jboss.aop.Manager#addDynamicCFlow(java.lang.String, org.jboss.aop.advice.DynamicCFlowDefinition)
     */
    public void addDynamicCFlow(String name, DynamicCFlowDefinition cflow)
    {
       dynamicCFlows.put(name, cflow);
    }
 
    public void removeDynamicCFlow(String name)
    {
       dynamicCFlows.remove(name);
    }
 
    /**
     * This is a callback object that receives events about
     * new pointcuts, interceptors and metadata.
     * The Aspect Management console hooks into this listener
     */
    public static AspectNotificationHandler notificationHandler = null;
 
    public static boolean suppressTransformationErrors = false;
 
 
    /**
     * Suppress when a class cannot be found that a class references
     * This may happen if code in a class references something and the
     * class is not in the classpath.
     */
    public static boolean suppressReferenceErrors = true;
 
    // Constructors -------------------------------------------------
 
    /**
     * Called by subclasses
     */
    public AspectManager()
    {
 
    }
    /**
     * Every &lt;class-metadata&gt; tag corresponds to
     * a ClassMetaDataLoader.  The ClassMetaDataLoader knows how to take
     * arbitrary XML and apply it to SimpleMetaData.
     * <p/>
     * Given a group, return the loader for that group
     */
    public ClassMetaDataLoader findClassMetaDataLoader(String group)
    {
       ClassMetaDataLoader loader = (ClassMetaDataLoader) classMetaDataLoaders.get(group);
       if (loader == null) loader = SimpleClassMetaDataLoader.singleton;
       return loader;
    }
 
    /**
     * Every &lt;class-metadata&gt; tag corresponds to
     * a ClassMetaDataLoader.  The ClassMetaDataLoader knows how to take
     * arbitrary XML and apply it to SimpleMetaData.
     * <p/>
     * Add a loader for a given group
     */
    public void addClassMetaDataLoader(String group, ClassMetaDataLoader loader)
    {
       classMetaDataLoaders.put(group, loader);
    }
 
    /**
     * Remove a loader for a given group
     */
    public void removeClassMetaDataLoader(String group)
    {
       classMetaDataLoaders.remove(group);
    }
 
    public Map getAdvisors()
    {
       return advisors;
    }
 
    public Advisor getAdvisor(String name)
    {
       /*
       synchronized (advisors)
       {
          return (Advisor) advisors.get(name);
       }
       */
       throw new RuntimeException("OPERATION NOT SUPPORTED ANYMORE");
    }
 
    public LinkedHashMap getBindings()
    {
       return bindings;
    }
 
    protected Map getSubDomainsPerClass()
    {
       return subDomainsPerClass;
    }
 
    public Advisor findAdvisor(Class clazz)
    {
       if (getSubDomainsPerClass().size() > 0)
       {
          //For generated advisors each advisor has its own domain
          //This is primarily needed for the reflection aspect
          Domain subDomain = null;
          synchronized (getSubDomainsPerClass())
          {
             WeakReference ref = (WeakReference)getSubDomainsPerClass().get(clazz);
             if (ref != null)
             {
                subDomain = (Domain)ref.get();
             }
          }
 
          if (subDomain != null && subDomain != this)
          {
             Advisor advisor = subDomain.findAdvisor(clazz);
             if (advisor != null)
             {
                return advisor;
             }
          }
       }
 
       synchronized (advisors)
       {
          WeakReference ref = (WeakReference) advisors.get(clazz);
          if (ref == null) return null;
          return (Advisor) ref.get();
       }
    }
    
    /**
     * Takes a string of the form /sub1/sub2/sub3 of subdomains by name, where the leading "/" is the main AspectManager.
     * The main user of the naming of domains is (un)serialization of advisors/containers
     * 
     * @param The FQN of the domain
     * @return the domain referenced by the FQN or null if it does not exist
     */
    public AspectManager findManagerByName(String fqn)
    {
       String[] nameparts = fqn.split("/");
       return findManagerByName(nameparts);
    }
    
    private AspectManager findManagerByName(String[] nameparts)
    {
       AspectManager found = this;
       for (int i = 0 ; i < nameparts.length ; i++)
       {
          if (nameparts[i].trim().length() == 0)
          {
             continue;
          }
          found = found.findManagerByNameInternal(nameparts[i]);
          if (found == null)
          {
             break;
          }
       }
       return found;
    }
    
    private AspectManager findManagerByNameInternal(String name)
    {
       return (Domain)subDomainsByName.get(name);
    }
    
    protected void addSubDomainByName(Domain domain)
    {
       subDomainsByName.put(domain.getDomainName(), domain);
    }
    
    public String getManagerFQN()
    {
       return "/";
    }
 
    public ClassAdvisor getAdvisorIfAdvised(Class clazz)
    {
       return (ClassAdvisor)getAnyAdvisorIfAdvised(clazz);
    }
 
    /**
     * Take into account that an advisor may be a container
     */
    public Advisor getAnyAdvisorIfAdvised(Class clazz)
    {
       try
       {
          Advisor advisor;
          advisor = findAdvisor(clazz);
          if (advisor == null)
          {
             return null;
          }
          if (advisor.getClazz() == null && advisor instanceof ClassAdvisor)
          {
             ((ClassAdvisor)advisor).attachClass(clazz);
             if (notificationHandler != null)
             {
                notificationHandler.attachClass(clazz.getName());
             }
          }
          return advisor;
       }
       catch (RuntimeException ex)
       {
          ex.printStackTrace();
          throw ex;
       }
    }
 
    /**
     * This method is called by the aspectized class when it is loaded
     * This causes all initialization of interceptors for ClassAdvisor
     *
     * @param clazz
     * @return
     */
    public ClassAdvisor getAdvisor(Class clazz)
    {
       ClassAdvisor advisor = null;
       // See if one already exists
       advisor = (ClassAdvisor)findAdvisor(clazz);
       // if one does not
       if (advisor == null)
       {
          advisor = AdvisorFactory.getClassAdvisor(clazz, this);
          initialiseClassAdvisor(clazz, advisor);
       }
       return advisor;
    }
 
    public void initialiseClassAdvisor(Class clazz, ClassAdvisor advisor)
    {
       synchronized (advisors)
       {
          advisors.put(clazz, new WeakReference(advisor));
       }
 
       registerClass(clazz);
       advisor.attachClass(clazz);
       InterceptorChainObserver observer = dynamicStrategy.getInterceptorChainObserver(clazz);
       advisor.setInterceptorChainObserver(observer);
       if (notificationHandler != null)
       {
          notificationHandler.attachClass(clazz.getName());
       }
    }
 
    // Public -------------------------------------------------------
 
    public static Map getRegisteredCLs()
    {
       return AOPClassPoolRepository.getInstance().getRegisteredCLs();
    }
 
    /**
     * This method will check to see if a register classloader has been undeployed (as in JBoss)
     */
    public static void clearUnregisteredClassLoaders()
    {
       AOPClassPoolRepository.getInstance().clearUnregisteredClassLoaders();
    }
 
    /**
     * Checks to see if an Advisor represents a class that should have been undeployed.
     *
     * @param advisor
     * @return
     */
    public boolean isAdvisorRegistered(Advisor advisor)
    {
       synchronized (getRegisteredCLs())
       {
          if (!advisors.containsKey(advisor.getClazz())) return false;
          ScopedClassPool pool = (ScopedClassPool) getRegisteredClassPool(advisor.getClazz().getClassLoader());
          if (pool == null) return false;
          if (pool.isUnloadedClassLoader())
          {
             unregisterClassLoader(advisor.getClazz().getClassLoader());
             return false;
          }
          else
          {
             return true;
          }
       }
    }
 
    public ClassPool findClassPool(ClassLoader cl)
    {
       if (!(cl instanceof Translatable))
       {
          // findClassPool has problems with boot and system classes.
          return registerClassLoader(Thread.currentThread().getContextClassLoader());
       }
       return registerClassLoader(cl);
    }
 
    protected ClassPool getRegisteredClassPool(ClassLoader cl)
    {
       return (ClassPool)getRegisteredCLs().get(cl);
    }
 
    public ClassPool registerClassLoader(ClassLoader ucl)
    {
       return AOPClassPoolRepository.getInstance().registerClassLoader(ucl);
    }
 
    protected void registerClass(Class clazz)
    {
       AOPClassPoolRepository.getInstance().registerClass(clazz);
    }
 
    protected Map getScopedClassLoaderDomains()
    {
       return scopedClassLoaderDomains;
    }
 
    public void unregisterClassLoader(ClassLoader cl)
    {
       AOPClassPoolRepository.getInstance().unregisterClassLoader(cl);
       //top-level only
       getScopedClassLoaderDomains().remove(cl);
    }
 
    public ArrayList getExclude()
    {
       return exclude;
    }
 
    public void setExclude(ArrayList exclude)
    {
       this.exclude.clear();
       this.exclude.addAll(exclude);
    }
 
    public ArrayList getInclude()
    {
       return include;
    }
 
    public void setInclude(ArrayList include)
    {
       this.include.clear();
       this.include.addAll(include);
    }
 
    public ArrayList getIgnore()
    {
       return ignore;
    }
 
    public ClassExpression[] getIgnoreExpressions()
    {
       return ignoreExpressions;
    }
 
    public void setIgnore(ArrayList ignore)
    {
       this.ignore.clear();
       this.ignore.addAll(ignore);
       ignoreExpressions = new ClassExpression[ignore.size()];
       for (int i = 0 ; i < ignore.size() ; i++)
       {
         String ex = (String)ignore.get(i);
         ignoreExpressions[i] = new ClassExpression(ex);
       }
    }
 
    public boolean ignoreClass(String classname)
    {
       ArrayList ignore = getIgnore();
       if (ignore == null) return false;
       for (int i = 0; i < ignoreExpressions.length; i++)
       {
          if(ignoreExpressions[i].matches(classname)) return true;
       }
       return false;
    }
 
    public boolean includeClass(String classname)
    {
       ArrayList include = getInclude();
       if (include == null) return false;
       for (int i = 0; i < include.size(); i++)
       {
          String str = (String) include.get(i);
          if (classname.startsWith(str)) return true;
       }
       return false;
    }
 
    public boolean excludeClass(String classname)
    {
       ArrayList exclude = getExclude();
       if (exclude == null) return false;
       for (int i = 0; i < exclude.size(); i++)
       {
          String str = (String) exclude.get(i);
          if (classname.startsWith(str)) return true;
       }
       return false;
    }
 
    public static boolean getPrune()
    {
       return AOPClassPoolRepository.getInstance().isPrune();
    }
 
    public static void setPrune(boolean prune)
    {
       AOPClassPoolRepository.getInstance().setPrune(prune);
    }
 
    public static void setClassPoolFactory(ScopedClassPoolFactory factory)
    {
       AOPClassPoolRepository.getInstance().setClassPoolFactory(factory);
    }
 
    public static ScopedClassPoolFactory getClassPoolFactory()
    {
       return AOPClassPoolRepository.getInstance().getClassPoolFactory();
    }
 
    public boolean isNonAdvisableClassName(String classname)
    {
       if (ignoreClass(classname)) return true;
       if (includeClass(classname)) return false;
       if (excludeClass(classname)) return true;
       return (classname.startsWith("org.jboss.aop") ||
               classname.endsWith("$aop") ||
               classname.startsWith("javassist") ||
               classname.startsWith("org.jboss.util.") ||
               classname.startsWith("gnu.trove.") ||
               classname.startsWith("EDU.oswego.cs.dl.util.concurrent.") ||
       // System classes
               classname.startsWith("org.apache.tools.ant") ||
               classname.startsWith("org.apache.crimson") ||
               classname.startsWith("org.apache.xalan") ||
               classname.startsWith("org.apache.xml") ||
               classname.startsWith("org.apache.xpath") ||
               classname.startsWith("org.ietf.") ||
               classname.startsWith("org.omg.") ||
               classname.startsWith("org.w3c.") ||
               classname.startsWith("org.xml.sax.") ||
               classname.startsWith("sunw.") ||
               classname.startsWith("sun.") ||
               classname.startsWith("java.") ||
               classname.startsWith("javax.") ||
               classname.startsWith("com.sun.") ||
               classname.startsWith("junit") ||
               classname.startsWith("jrockit.") ||
               classname.startsWith("com.bea.vm.")
              );
    }
 
    /**
     * This is the hook for ClassLoaders that want to instrument their classes with AOP
     * <p/>
     * This would be called during a findClass or loadClass call.  The return value
     * is used by defineClass to create the class from bytecode
     */
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
            throws Exception
    {
       byte[] b = translate(className, loader, classfileBuffer);
       return b;
    }
 
 
    /**
     * This is to be backward compatible with JBoss 3.2.3 Translator interface
     *
     * @param className
     * @param loader
     * @return
     * @throws Exception
     */
    public byte[] translate(String className, ClassLoader loader) throws Exception
    {
       return translate(className, loader, null);
    }
 
    /**
     * This is to be backward compatible with JBoss 3.2.3 Translator interface
    *
     * @param className
     * @param loader
     * @return
     * @throws Exception
     */
   public byte[] translate(String className, ClassLoader loader, byte[] classfileBuffer) throws Exception
    {
       try
       {
          if (isNonAdvisableClassName(className))
          {
             return null;
          }
          if (weavingStrategy == null)
          {
           synchronized (this)
            {
                if (TransformerCommon.isCompileTime() || classicOrder)
                {
                   weavingStrategy = new ClassicWeavingStrategy();
                }
                else if(InstrumentorFactory.getInstrumentor(this,dynamicStrategy.getJoinpointClassifier())
                      instanceof GeneratedAdvisorInstrumentor)
                {
                   weavingStrategy = new SuperClassesFirstWeavingStrategy();
                }
                else
                {
                   weavingStrategy = new ClassicWeavingStrategy();
                }
            }
          }
 
          return weavingStrategy.translate(this, className, loader, classfileBuffer);
       }
       catch (Exception e)
       {
          // AutoGenerated
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Add an interceptor factory that can be referenced by name.
     */
    public void addInterceptorFactory(String name, InterceptorFactory factory)
    {
       synchronized (interceptorFactories)
       {
          interceptorFactories.put(name, factory);
       }
    }
 
    /**
     * Remove an interceptor factory that can be referenced by name.
     */
    public void removeInterceptorFactory(String name)
    {
       synchronized (interceptorFactories)
       {
          interceptorFactories.remove(name);
       }
    }
 
    public Map getInterceptorFactories()
    {
       return interceptorFactories;
    }
 
    /**
     * Find the interceptor factory that can be referenced by name.
     */
    public InterceptorFactory getInterceptorFactory(String name)
    {
       synchronized (interceptorFactories)
       {
          return (InterceptorFactory) interceptorFactories.get(name);
       }
    }
 
 
    public void addPrecedence(PrecedenceDef precedenceDef)
    {
       synchronized (precedenceDefs)
       {
          precedenceDefs.put(precedenceDef.getName(), precedenceDef);
       }
       forceResortPrecedenceDefs();
    }
 
 
    public void removePrecedence(String name)
    {
       synchronized (precedenceDefs)
       {
          precedenceDefs.remove(name);
       }
 
       forceResortPrecedenceDefs();
    }
 
    protected void forceResortPrecedenceDefs()
    {
       synchronized (precedenceDefs)
       {
          sortedPrecedenceDefEntries = null;
       }
       synchronized (subscribedSubDomains)
       {
          copySubDomainsFromQueue(true);
          boolean newSubscribers = true;
          while (newSubscribers)
          {
             for (Iterator it = subscribedSubDomains.keySet().iterator() ; it.hasNext() ; )
             {
                Domain domain = (Domain)it.next();
                domain.forceResortPrecedenceDefs();
             }
             newSubscribers = copySubDomainsFromQueue(false);
          }
       }
    }
 
    public LinkedHashMap getPrecedenceDefs()
    {
       return precedenceDefs;
    }
 
    public PrecedenceDefEntry[] getSortedPrecedenceDefEntries()
    {
       if (sortedPrecedenceDefEntries == null)
       {
          synchronized (precedenceDefs)
          {
             if (sortedPrecedenceDefEntries == null)
             {
                sortedPrecedenceDefEntries = PrecedenceSorter.createOverallPrecedence(this);
             }
          }
       }
       return sortedPrecedenceDefEntries;
    }
 
    /**
     * Add a referencable InterceptorStack( &lt;stack&gt; )
     */
    public void addAdviceStack(AdviceStack stack)
    {
       synchronized (interceptorStacks)
       {
          interceptorStacks.put(stack.getName(), stack);
       }
    }
 
    /**
     * Remove a referencable InterceptorStack( &lt;stack&gt; )
     */
    public void removeInterceptorStack(String name)
    {
       synchronized (interceptorStacks)
       {
          interceptorStacks.remove(name);
       }
    }
 
    /**
     * Find an interceptor stack referenced by name ( &lt;stack&gt; )
     */
    public AdviceStack getAdviceStack(String name)
    {
       synchronized (interceptorStacks)
       {
          return (AdviceStack) interceptorStacks.get(name);
       }
    }
 
 
    protected boolean attachMetaData(ClassAdvisor advisor, CtClass clazz, boolean addAdvisor) throws Exception
    {
       boolean attached = false;
       synchronized (classMetaData)
       {
          Iterator it = classMetaData.values().iterator();
          while (it.hasNext())
          {
             ClassMetaDataBinding data = (ClassMetaDataBinding) it.next();
             if (data.matches(advisor, clazz))
             {
                attached = true;
                if (addAdvisor) data.addAdvisor(advisor);
                ClassMetaDataLoader loader = data.getLoader();
                loader.bind(advisor, data, clazz.getDeclaredMethods(), clazz.getDeclaredFields(), clazz.getDeclaredConstructors());
             }
          }
       }
       return attached;
    }
 
    protected void attachMetaData(ClassAdvisor advisor, Class clazz)
    {
       synchronized (classMetaData)
       {
          Iterator it = classMetaData.values().iterator();
          while (it.hasNext())
          {
             ClassMetaDataBinding data = (ClassMetaDataBinding) it.next();
             addAdvisorToClassMetaDataBinding(data, clazz, advisor, clazz);
          }
       }
    }
 
    public ClassAdvisor getTempClassAdvisor(CtClass clazz) throws Exception
    {
       String classname = clazz.getName();
       ClassAdvisor advisor = AdvisorFactory.getClassAdvisor(clazz, this);
       attachMetaData(advisor, clazz, false);
       applyInterfaceIntroductions(advisor, clazz);
       return advisor;
    }
 
    public Advisor getTempClassAdvisorIfNotExist(Class clazz)
    {
       Advisor advisor = findAdvisor(clazz);
       if (advisor != null) return advisor;
       if (Advised.class.isAssignableFrom(clazz))
       {
 
          Class superClass = clazz;
          try
          {
             while (superClass != null)
             {
                try
                {
                   Field field = superClass.getDeclaredField(Instrumentor.HELPER_FIELD_NAME);
                   SecurityActions.setAccessible(field);
                   return (ClassAdvisor) field.get(null);
                }
                catch (NoSuchFieldException e)
                {
                   superClass = clazz.getSuperclass();
                }
             }
          }
          catch (IllegalAccessException e)
          {
             throw new RuntimeException(e);
          }
       }
 
       advisor = AdvisorFactory.getClassAdvisor(clazz, this);
       advisor.setClazz(clazz);
       return advisor;
    }
 
 
    public DomainDefinition getContainer(String name)
    {
       return (DomainDefinition) containers.get(name);
    }
 
    public void addContainer(DomainDefinition def)
    {
       containers.put(def.getName(), def);
    }
 
    public void removeContainer(String name)
    {
       containers.remove(name);
    }
 
    /**
     * Find a pointcut of with a given name
     */
    public Pointcut getPointcut(String name)
    {
       synchronized (pointcuts)
       {
          return (Pointcut) pointcuts.get(name);
       }
    }
 
    /**
     * Remove an interceptor pointcut with a given name
     */
    public void removePointcut(String name)
    {
       synchronized (pointcuts)
       {
          pointcuts.remove(name);
          pointcutInfos.remove(name);
       }
    }
 
    /**
     * Add an interceptor pointcut with a given name
     */
    public synchronized void addPointcut(Pointcut pointcut)
    {
       removePointcut(pointcut.getName());
       synchronized (pointcuts)
       {
          pointcuts.put(pointcut.getName(), pointcut);
          pointcutInfos.put(pointcut.getName(), new PointcutInfo(pointcut, this.transformationStarted));
       }
       updatePointcutStats(pointcut);
    }
 
    /**
     * THis method is used for performance reasons.
     *
     * @param pointcut
     */
    protected void updatePointcutStats(Pointcut pointcut)
    {
       // the following is for performance reasons.
       if (pointcut instanceof PointcutExpression)
       {
          PointcutExpression expr = (PointcutExpression) pointcut;
          expr.setManager(this);
          PointcutStats stats = expr.getStats();
          updateStats(stats);
       }
       else
       {
          // can't be sure so set all
          execution = true;
          construction = true;
          call = true;
          within = true;
          get = true;
          set = true;
          withincode = true;
       }
    }
 
    protected void updateStats(PointcutStats stats)
    {
       if (stats != null)
       {
          construction |= stats.isConstruction();
          execution |= stats.isExecution();
          call |= stats.isCall();
          within |= stats.isWithin();
          get |= stats.isGet();
          set |= stats.isSet();
          withincode |= stats.isWithincode();
       }
       else
       {
          if (verbose) System.out.println("[debug] Setting all pointcut stats to true");
          // can't be sure so set all
          execution = true;
          construction = true;
          call = true;
          within = true;
          get = true;
          set = true;
          withincode = true;
       }
    }
 
    public boolean isExecution()
    {
       return execution;
    }
 
    public boolean isConstruction()
    {
       return construction;
    }
 
    public boolean isCall()
    {
       return call;
    }
 
    public boolean isWithin()
    {
       return within;
    }
 
    public boolean isWithincode()
    {
       return withincode;
    }
 
    public boolean isGet()
    {
       return get;
    }
 
    public boolean isSet()
    {
       return set;
    }
 
    /**
     * Remove an interceptor pointcut with a given name
     */
    public void removeBinding(String name)
    {
       AdviceBinding binding = internalRemoveBinding(name);
       if (binding != null)
       {
          binding.clearAdvisors();
          dynamicStrategy.interceptorChainsUpdated();
       }
    }
 
    public void removeBindings(ArrayList binds)
    {
       clearUnregisteredClassLoaders();
 
       HashSet bindingAdvisors = new HashSet();
       ArrayList removedBindings = new ArrayList();
       synchronized (bindings)
       {
          int bindSize = binds.size();
 
          for (int i = 0; i < bindSize; i++)
          {
 
             AdviceBinding binding = (AdviceBinding) bindings.get(binds.get(i));
             if (binding == null)
             {
                System.out.println("[warn] AspectManager.removeBindings() no binding found with name " + binds.get(i));
                continue;
             }
             ArrayList ads = binding.getAdvisors();
             bindingAdvisors.addAll(ads);
             bindings.remove(binding.getName());
             Pointcut pointcut = binding.getPointcut();
             this.removePointcut(pointcut.getName());
             removedBindings.add(binding);
          }
       }
       Iterator it = bindingAdvisors.iterator();
       while (it.hasNext())
       {
          Advisor advisor = (Advisor) it.next();
          if (!isAdvisorRegistered(advisor))
          {
             //Check sub domains in case of generated advisors
 
             WeakReference ref = (WeakReference)getSubDomainsPerClass().get(advisor.getClazz());
             Domain domain = null;
             if (ref != null) domain = (Domain)ref.get();
             if (domain != null)
             {
                if (subscribedSubDomains.containsKey(domain))
                {
                   if (!domain.isAdvisorRegistered(advisor))continue;
                }
             }
          }
          advisor.removeAdviceBindings(removedBindings);
       }
       dynamicStrategy.interceptorChainsUpdated();
    }
 
    /**
     * Add an interceptor pointcut with a given name
     */
    public synchronized void addBinding(AdviceBinding binding)
    {
       AdviceBinding removedBinding = internalRemoveBinding(binding.getName());
       Set affectedAdvisors = removedBinding == null? new HashSet(): new HashSet(removedBinding.getAdvisors());
       synchronized (bindings)
       {
          bindings.put(binding.getName(), binding);
       }
       synchronized (pointcuts)
       {
          Pointcut pointcut = binding.getPointcut();
          pointcuts.put(pointcut.getName(), pointcut);
          pointcutInfos.put(pointcut.getName(), new PointcutInfo(pointcut, binding, this.transformationStarted));
          updatePointcutStats(pointcut);
       }
 
       synchronized (advisors)
       {
          updateAdvisorsForAddedBinding(binding);
 
          for (Iterator i = affectedAdvisors.iterator(); i.hasNext(); )
          {
             Advisor advisor = (Advisor) i.next();
             if (isAdvisorRegistered(advisor))
                advisor.removeAdviceBinding(removedBinding);
          }
       }
       this.dynamicStrategy.interceptorChainsUpdated();
    }
 
 
    public void updateAdvisorsForAddedBinding(AdviceBinding binding)
    {
       synchronized (advisors)
       {
          //System.out.println("******* addBinding to possibly this many advisors: " + advisors.size());
          Collection keys = advisors.keySet();
          if (keys.size() > 0)
          {
             Iterator it = keys.iterator();
             while (it.hasNext())
             {
                Advisor advisor = getAdvisorFromAdvisorsKeySetIterator(it);
                if (advisor == null) continue;
 
                if (binding.getPointcut().softMatch(advisor))
                {
                   if (AspectManager.verbose)
                      System.out.println("[debug] softmatch succeeded for : " + advisor.getName() + " " + binding + " " + binding.getPointcut().getExpr());
                   advisor.newBindingAdded();
                   //affectedAdvisors.remove(advisor);
                }
                else
                {
                   if (AspectManager.verbose)
                      System.out.println("[debug] softmatch failed for : " + advisor.getName() + " " + binding + " " + binding.getPointcut().getExpr());
                }
             }
          }
       }
       synchronized (subscribedSubDomains)
       {
          copySubDomainsFromQueue(true);
          boolean newSubscribers = true;
          while (newSubscribers)
          {
             Collection keys =  subscribedSubDomains.keySet();
             if (keys.size() > 0)
             {
                //When interceptors are installed as beans in the microcontainer, creating the interceptor instances
                for (Iterator it = keys.iterator() ; it.hasNext() ; )
                {
                   Domain domain = (Domain)it.next();
                   domain.updateAdvisorsForAddedBinding(binding);
                }
             }
             newSubscribers = copySubDomainsFromQueue(false);
          }
       }
    }
 
    public void removeClassMetaData(String name)
    {
       synchronized (classMetaData)
       {
          ClassMetaDataBinding meta = (ClassMetaDataBinding) classMetaData.remove(name);
          if (meta == null) return;
          meta.clearAdvisors();
       }
    }
 
    public void addClassMetaData(ClassMetaDataBinding meta)
    {
       removeClassMetaData(meta.getName());
 
       updateAdvisorsForAddedClassMetaData(meta);
 
       synchronized (classMetaData)
       {
          classMetaData.put(meta.getName(), meta);
       }
    }
 
    protected void updateAdvisorsForAddedClassMetaData(ClassMetaDataBinding meta)
    {
       synchronized (advisors)
       {
          Iterator it = advisors.keySet().iterator();
 
          while (it.hasNext())
          {
             Advisor advisor = getAdvisorFromAdvisorsKeySetIterator(it);
             if (advisor == null) continue;
 
             Class clazz = advisor.getClazz();
             addAdvisorToClassMetaDataBinding(meta, clazz, advisor, clazz);
          }
       }
 
       synchronized (subscribedSubDomains)
       {
          copySubDomainsFromQueue(true);
          boolean newSubscribers = true;
          while (newSubscribers)
          {
             for (Iterator it = subscribedSubDomains.keySet().iterator() ; it.hasNext() ; )
             {
                Domain domain = (Domain)it.next();
                domain.updateAdvisorsForAddedClassMetaData(meta);
             }
             newSubscribers = copySubDomainsFromQueue(false);
          }
       }
    }
 
    protected void addAdvisorToClassMetaDataBinding(ClassMetaDataBinding meta, Class clazz, Advisor advisor, Class advisedClass)
    {
       if (meta.matches(advisor, clazz))
       {
             meta.addAdvisor(advisor);
       }
       else if (advisor.chainOverridingForInheritedMethods())
       {
          //If advisor class does not match class metadata directly, try the superclasses so that methods can inherit
          //old skool weaving doesn't support metadata overriding for inherited methods, so only do this extra work for generated advisors
          Class superClass = clazz.getSuperclass();
          if (superClass != null && superClass != Object.class)
          {
             addAdvisorToClassMetaDataBinding(meta, superClass, advisor, advisedClass);
          }
       }
    }
 
    //--- Introductions
 
    /**
     * Retrieve an introduction pointcut of a certain name
     */
    public InterfaceIntroduction getInterfaceIntroduction(String name)
    {
       synchronized (interfaceIntroductions)
       {
          return (InterfaceIntroduction) interfaceIntroductions.get(name);
       }
    }
 
    /**
     * Register an introduction pointcut
     */
    public synchronized void addInterfaceIntroduction(InterfaceIntroduction pointcut)
    {
       removeInterfaceIntroduction(pointcut.getName());
       synchronized (interfaceIntroductions)
       {
          interfaceIntroductions.put(pointcut.getName(), pointcut);
       }
    }
 
    /**
     * remove an introduction pointcut of a certain name
     */
    public void removeInterfaceIntroduction(String name)
    {
       synchronized (interfaceIntroductions)
       {
          InterfaceIntroduction pointcut = (InterfaceIntroduction) interfaceIntroductions.remove(name);
          if (pointcut == null) return;
          pointcut.clearAdvisors();
       }
    }
 
    /**
     * Register an annotation introduction
     */
    public synchronized void addAnnotationIntroduction(AnnotationIntroduction pointcut)
    {
       String name = pointcut.getOriginalAnnotationExpr() + pointcut.getOriginalExpression();
       removeAnnotationIntroduction(pointcut);
       synchronized (annotationIntroductions)
       {
          annotationIntroductions.put(name, pointcut);
       }
    }
 
    /**
     * remove an annotation pointcut
     */
    public void removeAnnotationIntroduction(AnnotationIntroduction pointcut)
    {
       String name = pointcut.getOriginalAnnotationExpr() + pointcut.getOriginalExpression();
       synchronized (annotationIntroductions)
       {
          annotationIntroductions.remove(name);
       }
    }
 
    public List getAnnotationIntroductions()
    {
       synchronized (annotationIntroductions)
       {
          return new ArrayList(annotationIntroductions.values());
       }
    }
 
    public synchronized void addDeclare(DeclareDef declare)
    {
       removeDeclare(declare.getName());
       synchronized (declares)
       {
          declares.put(declare.getName(), declare);
       }
       if (declare.isPointcut())
       {
          PointcutStats stats;
          stats = new PointcutStats(declare.getAst(), manager);
          stats.matches();
          updateStats(stats);
       }
    }
 
    public void removeDeclare(String name)
    {
       synchronized (declares)
       {
          declares.remove(name);
       }
    }
 
    public Iterator getDeclares()
    {
       return declares.values().iterator();
    }
 
    protected void applyInterfaceIntroductions(Advisor advisor, Class clazz)
    {
       Map interfaceIntroductions = getInterfaceIntroductions();
       if (interfaceIntroductions != null && interfaceIntroductions.size() > 0)
       {
          Iterator it = interfaceIntroductions.values().iterator();
          while (it.hasNext())
          {
             InterfaceIntroduction pointcut = (InterfaceIntroduction) it.next();
             if (pointcut.matches(advisor, clazz))
             {
                pointcut.addAdvisor(advisor);
             }
          }
       }
    }
 
    protected void applyInterfaceIntroductions(ClassAdvisor advisor, CtClass clazz) throws Exception
    {
       Map interfaceIntroductions = getInterfaceIntroductions();
       if (interfaceIntroductions != null && interfaceIntroductions.size() > 0)
       {
          Iterator it = interfaceIntroductions.values().iterator();
          while (it.hasNext())
          {
             InterfaceIntroduction pointcut = (InterfaceIntroduction) it.next();
             if (pointcut.matches(advisor, clazz))
             {
                pointcut.addAdvisor(advisor);
             }
          }
       }
    }
 
 
    /**
     * Register an annotation introduction
     */
    public synchronized void addAnnotationOverride(AnnotationIntroduction pointcut)
    {
       String name = pointcut.getOriginalAnnotationExpr() + pointcut.getOriginalExpression();
       synchronized (annotationOverrides)
       {
          annotationOverrides.put(name, pointcut);
       }
       updateAdvisorsForAddedAnnotationOverride(pointcut);
    }
 
    public void updateAdvisorsForAddedAnnotationOverride(AnnotationIntroduction introduction)
    {
       synchronized (advisors)
       {
          //System.out.println("******* addBinding to possibly this many advisors: " + advisors.size());
          Iterator it = advisors.keySet().iterator();
          while (it.hasNext())
          {
             Advisor advisor = getAdvisorFromAdvisorsKeySetIterator(it);
             if (advisor == null) continue;
 
             advisor.deployAnnotationOverride(introduction);
          }
       }
       synchronized (subscribedSubDomains)
       {
          copySubDomainsFromQueue(true);
          boolean newSubscribers = true;
          while (newSubscribers)
          {
             for (Iterator it = subscribedSubDomains.keySet().iterator() ; it.hasNext() ; )
             {
                Domain domain = (Domain)it.next();
                domain.updateAdvisorsForAddedAnnotationOverride(introduction);
             }
             newSubscribers = copySubDomainsFromQueue(false);
          }
       }
    }
 
 
    /**
     * remove an annotation pointcut
     */
    public void removeAnnotationOverride(AnnotationIntroduction pointcut)
    {
       String name = pointcut.getOriginalAnnotationExpr() + pointcut.getOriginalExpression();
       synchronized (annotationOverrides)
       {
          annotationOverrides.remove(name);
       }
    }
 
    public List getAnnotationOverrides()
    {
       synchronized (annotationOverrides)
       {
          return new ArrayList(annotationOverrides.values());
       }
    }
 
    public Object getPerVMAspect(AspectDefinition def)
    {
       return getPerVMAspect(def.getName());
    }
 
    public Object getPerVMAspect(String def)
    {
       Object aspect = perVMAspects.get(def);
       if (aspect instanceof AspectDefinition)
       {
          synchronized (aspect)
          {
             return createPerVmAspect(def, (AspectDefinition)aspect, null);
          }
       }
       return aspect;
    }
    
    protected Object createPerVmAspect(String def, AspectDefinition adef, ClassLoader scopedClassLoader)
    {
       Object instance = null;
       synchronized (adef)
       {
          try
          {
             if (scopedClassLoader != null && adef.getFactory() instanceof AspectFactoryWithClassLoader)
             {
                //If a scoped classloader with no parent delegation redefines the class, we need to make sure that that class is pushed on the stack
                ((AspectFactoryWithClassLoader)adef.getFactory()).pushScopedClassLoader(scopedClassLoader);
             }
             instance = adef.getFactory().createPerVM();
             perVMAspects.put(def, instance);
          }
          finally
          {
             if (scopedClassLoader != null && adef.getFactory() instanceof AspectFactoryWithClassLoader)
             {
                ((AspectFactoryWithClassLoader)adef.getFactory()).popScopedClassLoader();
             }
          }
       }
       return instance;
    }
 
    public void addAspectDefinition(AspectDefinition def)
    {
       removeAspectDefinition(def.getName());
       if (def.getScope() == Scope.PER_VM)
       {
          perVMAspects.put(def.getName(), def);
       }
       aspectDefinitions.put(def.getName(), def);
    }
 
    public void removeAspectDefinition(String name)
    {
       AspectDefinition def = (AspectDefinition) aspectDefinitions.remove(name);
       if (def != null)
       {
          def.undeploy();
          if (def.getScope() == Scope.PER_VM) perVMAspects.remove(def.getName());
       }
    }
 
    public Map getAspectDefinitions()
    {
       return aspectDefinitions;
    }
 
    public AspectDefinition getAspectDefinition(String name)
    {
       return (AspectDefinition) aspectDefinitions.get(name);
    }
 
    public synchronized void addTypedef(Typedef def) throws Exception
    {
       removeTypedef(def.getName());
       synchronized (typedefs)
       {
          typedefs.put(def.getName(), def);
       }
    }
 
    public void removeTypedef(String name)
    {
       synchronized (typedefs)
       {
          typedefs.remove(name);
       }
    }
 
    public Typedef getTypedef(String name)
    {
       synchronized (typedefs)
       {
          return (Typedef) typedefs.get(name);
       }
    }
 
    public Map getInterfaceIntroductions()
    {
       return interfaceIntroductions;
    }
 
    public Map getTypedefs()
    {
       return typedefs;
    }
 
    public Map getInterceptorStacks()
    {
       return interceptorStacks;
    }
 
    public Map getClassMetaDataLoaders()
    {
       return classMetaDataLoaders;
    }
 
    public Map getCflowStacks()
    {
       return cflowStacks;
    }
 
    public Map getDynamicCFlows()
    {
       return dynamicCFlows;
    }
 
    public Map getPerVMAspects()
    {
       return perVMAspects;
    }
 
    public Map getClassMetaData()
    {
       return classMetaData;
    }
 
    /**
     * Returns the dynamic aop strategy to be used.
     */
    public DynamicAOPStrategy getDynamicAOPStrategy()
    {
       return this.dynamicStrategy;
    }
 
    /**
     * Sets the dynamic aop strategy to be used.
     * Should be called only before any class is transformed.
     * @param strategy the new dynamic aop strategy.
     */
    public void setDynamicAOPStrategy(DynamicAOPStrategy strategy)
    {
       // avoid users calling this method in run time
       if (this.transformationStarted)
       {
          throw new RuntimeException("Dynamic AOP Strategy Update not allowed in run time");
       }
       this.dynamicStrategy = strategy;
    }
 
    /**
     * Removes an AdviceBinding without notifying dynamic aop strategy.
     * @param name the binding to be removed.
     */
    private AdviceBinding internalRemoveBinding(String name)
    {
       synchronized (bindings)
       {
          AdviceBinding binding = (AdviceBinding) bindings.remove(name);
          if (binding == null)
          {
             return null;
          }
          Pointcut pointcut = binding.getPointcut();
          this.removePointcut(pointcut.getName());
          return binding;
       }
    }
 
    public void setBindings(LinkedHashMap bindings)
    {
       this.bindings.clear();
       this.bindings.putAll(bindings);
    }
 
    public void addSubDomainPerClass(Class clazz, Domain domain)
    {
       synchronized (getSubDomainsPerClass())
       {
          getSubDomainsPerClass().put(clazz, new WeakReference(domain));
       }
    }
 
    /**
     * Add subscriber to queue
     * @see AspectManager#copySubDomainsFromQueue(boolean)
     */
    public void subscribeSubDomain(Domain domain)
    {
       synchronized (subscribedSubDomains)
       {
          subscribedSubDomainsQueue.put(domain, "Contents do not matter");
       }
    }
 
    public void unsubscribeSubDomain(Domain domain)
    {
       synchronized (subscribedSubDomains)
       {
          subscribedSubDomains.remove(domain);
       }
    }
 
    private Advisor getAdvisorFromAdvisorsKeySetIterator(Iterator it)
    {
       Class clazz = (Class) it.next();
       if (classLoaderValidator != null && !classLoaderValidator.isValidClassLoader(clazz.getClassLoader()))
       {
          it.remove();
          return null;
       }
       WeakReference ref = (WeakReference) advisors.get(clazz);
       if (ref == null) return null;
       Advisor advisor = (Advisor) ref.get();
       if (advisor == null)
       {
          it.remove();
          return null;
       }
       return advisor;
    }
 
    /**
     * When running in the microcontainer with aspects installed as beans, a ClassProxyContainer will be created per bean
     * to check if this bean needs interceptors, each container creates a sunscribed domain for matching. This subscribed
     * domain is added to a queue, which is checked when we need to iterate over the subscribed domains.
     */
    private boolean copySubDomainsFromQueue(boolean increment)
    {
       boolean copied = false;
       synchronized (subscribedSubDomains)
       {
          if (!increment && subscribedDomainQueueRef > 0) subscribedDomainQueueRef--;
 
          if (subscribedDomainQueueRef == 0 && subscribedSubDomainsQueue.size() > 0){
             subscribedSubDomains.putAll(subscribedSubDomainsQueue);
             subscribedSubDomainsQueue.clear();
             copied = true;
          }
 
          if (increment) subscribedDomainQueueRef++;
       }
       return copied;
    }
 
 /*
    public void dumpSubDomainsAndAdvisors(int indent)
    {
       indent(indent);
       System.out.println("Manager: " + this);
       indent++;
       indent(indent);
       System.out.println("<Advisors>");
       //indent(indent);
 
       for (Iterator it = advisors.keySet().iterator() ; it.hasNext() ; )
       {
          Class clazz = (Class) it.next();
          Advisor advisor = null;
          WeakReference ref = (WeakReference) advisors.get(clazz);
          if (ref != null) advisor = (Advisor) ref.get();
          indent(indent);
          System.out.println(System.identityHashCode(advisor) + " " + advisor);
          indent(indent);
       }
       indent--;
       indent(indent);
       System.out.println("</Advisors>");
 
       indent(indent);
       System.out.println("<Sub domains>");
       indent++;
       for (Iterator it = subscribedSubDomains.keySet().iterator(); it.hasNext() ; )
       {
          AspectManager manager = (AspectManager)it.next();
          manager.dumpSubDomainsAndAdvisors(indent);
       }
       indent--;
       indent(indent);
       System.out.println("</Sub domains>");
       indent--;
 
    }
 
    private void indent(int indent)
    {
       for (int i = 0 ; i < indent ; i++) System.out.print(" ");
    }
 */
 }
