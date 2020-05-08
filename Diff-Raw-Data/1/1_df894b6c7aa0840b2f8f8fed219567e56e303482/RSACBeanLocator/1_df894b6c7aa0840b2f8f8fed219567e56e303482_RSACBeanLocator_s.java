 /*
  * Created on Sep 18, 2005
  */
 package uk.org.ponder.rsac;
 
 import java.beans.PropertyChangeEvent;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Level;
 import org.springframework.beans.BeanInstantiationException;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.TypeMismatchException;
 import org.springframework.beans.factory.BeanCurrentlyInCreationException;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
 import org.springframework.beans.factory.FactoryBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.BeanPostProcessor;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ConfigurableApplicationContext;
 
 import uk.org.ponder.beanutil.BeanUtil;
 import uk.org.ponder.beanutil.FallbackBeanLocator;
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.reflect.ReflectUtils;
 import uk.org.ponder.reflect.ReflectiveCache;
 import uk.org.ponder.saxalizer.AccessMethod;
 import uk.org.ponder.saxalizer.MethodAnalyser;
 import uk.org.ponder.saxalizer.SAXalizerMappingContext;
 import uk.org.ponder.springutil.BeanDefinitionSource;
 import uk.org.ponder.springutil.TLABPostProcessor;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Denumeration;
 import uk.org.ponder.util.EnumerationConverter;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.ObjectFactory;
 import uk.org.ponder.util.RunnableInvoker;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * The central class managing the Request Scope Application Context.
  * <p>
  * The principal method useful to users is <code>getBeanLocator()</code> which
  * returns a BeanLocator holding the request context for the current thread. For
  * each thread entering an RSAC request, it must call
  * <code>startRequest()</code> before acquiring any request beans, and
  * <code>endRequest()</code> at the end of its cycle (the latter is most
  * important).
  * <p>
  * In a "pure RSAC" application, the request logic will be defined by the
  * getting of a single "root bean" from the BeanLocator, although initial setup
  * and any proxies may require additional calls to <code>getBeanLocator()</code>.
  * <p>
  * This class will be due for some refactoring as soon as the next piece of
  * functionality gets added, getting on for 400 lines. Please note that this
  * class currently illegally casts BeanDefinitions received from Spring to
  * AbstractBeanDefinition, which is a potential dependency weakness. This
  * approach is known to work with Spring 1.1.2 through 2.0.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class RSACBeanLocator implements ApplicationContextAware,
     BeanDefinitionSource {
   /** The standard bean name for the RSACBeanLocator * */
   public static String RSAC_BEAN_LOCATOR_NAME = "RSACBeanLocator";
 
   private static CreationMarker BEAN_IN_CREATION_OBJECT = new CreationMarker(0);
   private static String REQUEST_STARTED_KEY = ".request  started";
   private ConfigurableApplicationContext blankcontext;
   private ApplicationContext parentcontext;
   private SAXalizerMappingContext smc;
   private ReflectiveCache reflectivecache;
   private TLABPostProcessor tlabpp;
 
   public void setBlankContext(ConfigurableApplicationContext blankcontext) {
     this.blankcontext = blankcontext;
   }
 
   // NB - currently used only for leafparsers, which are currently JVM-static.
   public void setMappingContext(SAXalizerMappingContext smc) {
     this.smc = smc;
   }
 
   public void setApplicationContext(ApplicationContext applicationContext) {
     parentcontext = applicationContext;
   }
 
   public void setReflectiveCache(ReflectiveCache reflectivecache) {
     this.reflectivecache = reflectivecache;
   }
 
   // private ThreadLocal threadlocal = new ThreadLocal() {
   // public Object initialValue() {
   // return new PerRequestInfo(RSACBeanLocator.this, lazysources);
   // }
   // };
   // We do not use initialValue here because of bug
   // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5025230 which corrupts
   // the table when initialising a further ThreadLocal from within
   // initialValue().
   // in this case the ThreadLocal is created within CGLib while instantiating
   // the RSACLazyTargetSource proxies.
   private ThreadLocal threadlocal = new ThreadLocal();
 
   private PerRequestInfo getPerRequest() {
     PerRequestInfo pri = (PerRequestInfo) threadlocal.get();
     if (pri == null) {
       pri = new PerRequestInfo(RSACBeanLocator.this, lazysources);
       threadlocal.set(pri);
     }
     return pri;
   }
 
   /**
    * Starts the request-scope container for the current thread.
    */
 
   public void startRequest() {
     if (isStarted()) {
       throw UniversalRuntimeException.accumulate(new IllegalStateException(),
           "RSAC container has already been started: ");
     }
     GlobalBeanAccessor.startRequest(parentcontext);
     PerRequestInfo pri = getPerRequest();
     pri.beans.set(REQUEST_STARTED_KEY, BEAN_IN_CREATION_OBJECT);
   }
 
   /**
    * Determines whether the container has already been started for the current
    * thread.
    */
 
   public boolean isStarted() {
     PerRequestInfo pri = getPerRequest();
     return pri.beans.locateBean(REQUEST_STARTED_KEY) != null;
   }
 
   private void assertIsStarted() {
     if (!isStarted()) {
       throw UniversalRuntimeException.accumulate(new IllegalStateException(),
           "RSAC container has not been started properly: ");
     }
   }
 
   /**
    * Called at the end of a request. I advise doing this in a finally block.
    */
   public void endRequest() {
     assertIsStarted();
     PerRequestInfo pri = getPerRequest();
     Runnable lazarusList = (Runnable) pri.beans.locateBean("RSACLazarusList");
     GlobalBeanAccessor.endRequest();
 
     for (int i = 0; i < pri.todestroy.size(); ++i) {
       String todestroyname = pri.todestroy.stringAt(i);
       RSACBeanInfo destroybean = (RSACBeanInfo) rbimap.get(todestroyname);
       Object todestroy = null;
       try {
         todestroy = getBean(pri, todestroyname, false);
         reflectivecache.invokeMethod(todestroy, destroybean.destroymethod);
       }
       // must try to destroy as many beans as possible, cannot propagate
       // exception in a finally block in any case.
       catch (Exception e) {
         Logger.log.error("Error destroying bean " + todestroy + " with name "
             + todestroyname, e);
       }
     }
     // System.out.println(pri.cbeans + " beans were created");
     // Give the garbage collector a head start
     pri.clear();
     if (lazarusList != null) {
       lazarusList.run();
     }
   }
 
   // this is a map of bean names to RSACBeanInfo
   private Map rbimap;
   // this is a list of the beans of type RSACLazyTargetSources
   private StringList lazysources;
   // this is a list of "fallback" beans that have already been queried
   private StringList fallbacks;
   // this is a map of alias names to "canonical" bean names
   private Map aliasMap;
 
   public void init() {
     // at this point we actually expect that the "Dead" factory is FULLY
     // CREATED. This checks that all dependencies are resolvable (if this
     // has not already been checked by the IDE).
     String[] beanNames = blankcontext.getBeanDefinitionNames();
     ConfigurableListableBeanFactory factory = blankcontext.getBeanFactory();
     // prepare our list of dependencies.
     rbimap = new HashMap();
     lazysources = new StringList();
     fallbacks = new StringList();
     aliasMap = new HashMap();
 
     RBIBeanDefConverter converter = new RBIBeanDefConverter(factory, smc);
 
     for (int i = 0; i < beanNames.length; i++) {
       String beanname = beanNames[i];
       BeanDefinition beandef = factory.getBeanDefinition(beanname);
       try {
         converter.convertBeanDef(beandef, beanname, false);
       }
       catch (Exception e) {
         Logger.log.error("Error loading definition for bean " + beanname, e);
       }
     }
     for (int i = 0; i < converter.rbilist.size(); ++i) {
       RSACBeanInfo rbi = (RSACBeanInfo) converter.rbilist.get(i);
       String beanname = rbi.beanname;
       rbimap.put(beanname, rbi);
       for (int j = 0; j < rbi.aliases.length; ++j) {
         aliasMap.put(rbi.aliases[j], beanname);
       }
     }
 
     // Make a last-ditch attempt to infer bean types.
     for (int i = 0; i < converter.rbilist.size(); ++i) {
       RSACBeanInfo rbi = (RSACBeanInfo) converter.rbilist.get(i);
       String beanname = rbi.beanname;
       if (rbi.beanclass == null) {
         rbi.beanclass = getBeanClass(beanname);
       }
       if (rbi.beanclass != null) {
         if (rbi.islazyinit) {
           lazysources.add(beanname);
         }
         if (FallbackBeanLocator.class.isAssignableFrom(rbi.beanclass)
             && !rbi.isabstract) {
           fallbacks.add(beanname);
         }
         rbi.isfactorybean = FactoryBean.class.isAssignableFrom(rbi.beanclass);
       }
     }
     BracketerPopulator.populateBracketers(parentcontext, rbimap);
     
     // we must add this manually because it expects a DIFFERENT applicationContext
     // to the real one. Also the lifecycle is somewhat different.
     tlabpp = new TLABPostProcessor();
     tlabpp.setMappingContext(smc);
     tlabpp.setApplicationContext(blankcontext);
   }
 
   /**
    * Returns a list of bean names which are known to correspond to beans
    * implementing or derived from the supplied class. RSAC has tried slightly
    * harder to resolve bean classes than Spring generally does, through walking
    * chains of factory-methods.
    * 
    * @param clazz A class or interface class to be searched for.
    * @return A list of derived bean names.
    */
   public String[] beanNamesForClass(Class clazz) {
     StringList togo = new StringList();
     String[] beanNames = blankcontext.getBeanDefinitionNames();
     for (int i = 0; i < beanNames.length; i++) {
       String beanname = beanNames[i];
       RSACBeanInfo rbi = (RSACBeanInfo) rbimap.get(beanname);
       if (rbi.beanclass != null && clazz.isAssignableFrom(rbi.beanclass)) {
         togo.add(beanname);
       }
     }
     return togo.toStringArray();
   }
 
   /**
    * Returns the class of this bean, if it can be statically determined,
    * <code>null</code> if it cannot (i.e. this bean is the product of a
    * factory-method of a class which is not yet known)
    * 
    * @param beanname
    * @return
    */
   public Class getBeanClass(String beanname) {
     RSACBeanInfo rbi = (RSACBeanInfo) rbimap.get(beanname);
     if (rbi == null) {
       return parentcontext.getType(beanname);
     }
     else if (rbi.beanclass != null) {
       return rbi.beanclass;
     }
     else if (rbi.factorymethod != null && rbi.factorybean != null) {
       try {
         Class factoryclass = getBeanClass(rbi.factorybean);
         Method m = ReflectiveCache.getMethod(factoryclass, rbi.factorymethod);
         if (m != null) {
           rbi.beanclass = m.getReturnType();
         }
       }
       catch (Exception e) {
         Logger.log.warn("Error reflecting for factory method "
             + rbi.factorymethod + " in bean " + rbi.factorybean, e);
       }
     }
     // Noone could possibly say we didn't do our best to work out the type of
     // this bean.
     return rbi.beanclass;
   }
 
   public void addPostProcessor(BeanPostProcessor beanpp) {
     getPerRequest().postprocessors.add(beanpp);
   }
 
   private String getTransformedBeanName(String beanname) {
     String alias = (String) aliasMap.get(beanname);
     return alias == null ? beanname
         : alias;
   }
 
   private Object getLocalBean(PerRequestInfo pri, String beanname,
       boolean nolazy) {
     beanname = getTransformedBeanName(beanname);
     Object bean = pri.beans.locateBean(beanname);
     if (bean instanceof CreationMarker) {
       throw new BeanCurrentlyInCreationException(beanname);
     }
     else if (bean == null) {
       FactoryBean pfb = (FactoryBean) pri.lazysources.get(beanname);
       if (pfb != null && !nolazy) {
         try {
           return pfb.getObject();
         }
         catch (Exception e) {
           throw UniversalRuntimeException.accumulate(e,
               "Error getting proxied bean");
         }
       }
       else {
         bean = createBean(pri, beanname, BEAN_IN_CREATION_OBJECT);
       }
     }
     return bean;
   }
 
   // package access ensures visibility from RSACLazarusList
   Map getSeedMap() {
     return getPerRequest().seedbeans;
   }
 
   // package access ensures visibility from RSACLazyTargetSource
   Object getBean(PerRequestInfo pri, String beanname, boolean nolazy) {
     Object bean = null;
     // NOTES on parentage: We actually WOULD like to make the "blank" context
     // a child context of the parent, so that we could resolve parents across
     // the gap - the problem is the line below, where we distinguish local beans
     // from parent ones. Revisit this when we have a sensible idea about parent
     // contexts.
     // NB - we check the container since some fiend might have thrown it in
     // manually on inchuck - but actually this is faster than Spring anyway.
     if (pri.beans.locateBean(beanname) != null || rbimap.containsKey(beanname)) {
       bean = getLocalBean(pri, beanname, nolazy);
     }
     else {
       if (bean == null &&  this.parentcontext.containsBean(beanname)) {
         bean = this.parentcontext.getBean(beanname);
       }
     }
     return bean;
   }
   
   /** Return a list of names of beans of type FallbackBeanLocator **/
   public StringList getFallbackBeans() {
     return fallbacks;
   }
 
   private Object assembleVectorProperty(PerRequestInfo pri,
       StringList beannames, Class declaredType) {
     Object deliver = ReflectUtils.instantiateContainer(declaredType, beannames
         .size(), reflectivecache);
     Denumeration den = EnumerationConverter.getDenumeration(deliver,
         reflectivecache);
     for (int i = 0; i < beannames.size(); ++i) {
       String thisbeanname = beannames.stringAt(i);
       Object bean = getBean(pri, thisbeanname, false);
       den.add(bean);
     }
     return deliver;
   }
 
   private Object createBean(final PerRequestInfo pri, final String beanname,
       CreationMarker marker) {
     boolean success = false;
 
     try {
       RSACBeanInfo rbi = (RSACBeanInfo) rbimap.get(beanname);
       if (rbi == null) {
         throw new NoSuchBeanDefinitionException(beanname,
             "Bean definition not found");
       }
       if (rbi.isabstract) {
         throw new BeanInstantiationException(rbi.beanclass, "Abstract bean "
             + rbi.beanname + " cannot be instantiated");
       }
 
       if (marker == null) {
         marker = BEAN_IN_CREATION_OBJECT;
         if (rbi.issingleton) {
           pri.beans.set(beanname, marker);
         }
       }
 
       // implement fetch wrappers in such a way that doesn't slow normal
       // creation.
       if (rbi.fetchwrappers != null
           && marker.wrapperindex < rbi.fetchwrappers.length) {
         Object wrappero = rbi.fetchwrappers[marker.wrapperindex];
         if (marker.wrapperindex == 0) {
           marker = new CreationMarker(1);
           if (rbi.issingleton) {
             pri.beans.set(beanname, marker);
           }
         }
         else {
           ++marker.wrapperindex;
         }
         RunnableInvoker wrapper = (RunnableInvoker) (wrappero instanceof RunnableInvoker ? wrappero
             : getBean(pri, (String) wrappero, true));
         final Object[] togo = new Object[1];
         final CreationMarker nextmarker = marker;
         wrapper.invokeRunnable(new Runnable() {
           public void run() {
             togo[0] = createBean(pri, beanname, nextmarker);
           }
 
         });
         return togo[0];
       }
       if (rbi.issingleton) {
         ++pri.cbeans;
       }
 
       Object newbean;
       // NB - isn't this odd, and in fact generally undocumented - properties
       // defined for factory-method beans are set on the PRODUCT, whereas those
       // set on FactoryBeans are set on the FACTORY!!
       if (rbi.factorybean != null) {
 
         Object factorybean = null;
         try {
           factorybean = getBean(pri, rbi.factorybean, false);
         }
         catch (Exception e) {
           throw UniversalRuntimeException.accumulate(e,
               "Error fetching factory bean " + rbi.factorybean
                   + " to initialise bean " + beanname);
         }
         try {
           newbean = reflectivecache
               .invokeMethod(factorybean, rbi.factorymethod);
         }
         catch (Exception e) {
           throw UniversalRuntimeException.accumulate(e, "Error fetching bean "
               + beanname + " from factory method " + rbi.factorymethod
               + " of factory bean " + rbi.factorybean);
         }
         if (newbean == null) {
           throw new IllegalArgumentException(
               "Error: null returned from factory method " + rbi.factorymethod
                   + " of bean " + rbi.factorybean);
         }
         // rbi.beanclass = newbean.getClass();
       }
       else {
         // Locate the "dead" bean from the genuine Spring context, and clone it
         // as quick as we can - bytecodes might do faster but in the meantime
         // observe that a clone typically costs 1.6 reflective calls so in
         // general this method will win over a reflective solution.
         // NB - all Copiables simply copy dependencies manually for now, no
         // cost.
         // Copiable deadbean = (Copiable) livecontext.getBean(rbi.isfactorybean?
         // "&" +beanname : beanname);
         // All the same, the following line will cost us close to 1us - unless
         // it invokes manual code!
         newbean = reflectivecache.construct(rbi.beanclass);
       }
       if (rbi.hasDependencies()) {
         // guard this block since if it is a factory-method bean it may be
         // something
         // extremely undesirable (like an inner class) that we should not even
         // dream of reflecting over. If on the other hand the user has specified
         // some dependencies they doubtless know what they are doing.
         MethodAnalyser ma = smc.getAnalyser(newbean.getClass());
         // Object clonebean = deadbean.copy();
         // iterate over each LOCAL dependency of the bean with given name.
         for (Iterator depit = rbi.dependencies(); depit.hasNext();) {
           String propertyname = (String) depit.next();
           try {
             AccessMethod setter = ma.getAccessMethod(propertyname);
             if (setter == null) {
               throw new IllegalArgumentException(newbean.getClass()
                   + " has no writeable property named " + propertyname);
             }
             Object depbean = null;
             Object beanref = rbi.beanref(propertyname);
             if (beanref instanceof String) {
               String depbeanname = (String) beanref;
               depbean = fetchDependent(pri, depbeanname, setter);
               BeanUtil.censorNullBean(depbeanname, depbean);
             }
             else if (beanref instanceof ValueHolder) {
               Class accezzz = setter.getAccessedType();
               String value = ((ValueHolder) beanref).value;
               if (smc.saxleafparser.isLeafType(accezzz)) {
                 depbean = smc.saxleafparser.parse(accezzz, value);
               }
               else {
                 // exception def copied from the beast BeanWrapperImpl!
                 throw new TypeMismatchException(new PropertyChangeEvent(
                     newbean, propertyname, null, value), accezzz, null);
               }
             }
             else {
               // Really need generalised conversion of vector values here.
               // The code to do this is actually WITHIN the grotty
               // BeanWrapperImpl
               // itself in a protected method with 5 arguments!!
               // This is a sort of 50% solution. It will deal with all 1-d array
               // types and collections, and values of parseable types.
               depbean = assembleVectorProperty(pri, (StringList) beanref,
                   setter.getDeclaredType());
             }
         
             // Lose another 500ns here, until we bring on FastClass.
             setter.setChildObject(newbean, depbean);
           }
           catch (Exception e) {
             throw UniversalRuntimeException.accumulate(e,
                 "Error setting dependency " + propertyname + " of bean "
                     + beanname);
           }
         }
       }
       if (rbi.dependson != null) {
         for (int i = 0; i < rbi.dependson.length; ++i) {
           getBean(pri, rbi.dependson[i], false);
         }
       }
       // process it FIRST since it will be the factory that is expecting the
       // dependencies set!
       newbean = processNewBean(pri, beanname, newbean);
       // now the bean is initialised, attempt to call any init-method or
       // InitBean.
       if (rbi.initmethod != null) {
         try {
           reflectivecache.invokeMethod(newbean, rbi.initmethod);
         }
         catch (Exception e) {
           throw UniversalRuntimeException.accumulate(e,
               "Error invoking init method " + rbi.initmethod + " on bean "
                   + beanname);
         }
       }
       if (newbean instanceof InitializingBean) {
         try {
           ((InitializingBean) newbean).afterPropertiesSet();
         }
         catch (Exception e) { // Evil Rod! Bad Juergen!
           throw UniversalRuntimeException.accumulate(e);
         }
       }
       if (rbi.destroymethod != null) {
         pri.todestroy.add(beanname);
       }
       if (newbean instanceof FactoryBean) {
         FactoryBean factorybean = (FactoryBean) newbean;
         try {
           newbean = factorybean.getObject();
         }
         catch (Exception e) {
           throw UniversalRuntimeException.accumulate(e);
         }
       }
       // enter the bean into the req-specific map.
       if (rbi.issingleton) {
         pri.beans.set(beanname, newbean);
       }
       success = true;
       return newbean;
     }
     finally {
       if (marker.wrapperindex > 0) {
         --marker.wrapperindex;
         if (marker.wrapperindex == 0 && !success) {
           pri.beans.remove(beanname);
         }
       }
     }
 
   }
 
   private Object fetchDependent(final PerRequestInfo pri,
       final String beanname, final AccessMethod setter) {
     Class targetclazz = setter.getAccessedType();
     if (ObjectFactory.class.isAssignableFrom(targetclazz)) {
       return new ObjectFactory() {
         public Object getObject() {
           return getBean(pri, beanname, false);
         }
       };
     }
     else if (org.springframework.beans.factory.ObjectFactory.class
         .isAssignableFrom(targetclazz)) {
       return new org.springframework.beans.factory.ObjectFactory() {
         public Object getObject() throws BeansException {
           return getBean(pri, beanname, false);
         }
       };
     }
     else
       return getBean(pri, beanname, false);
   }
 
   private Object processNewBean(PerRequestInfo pri, String beanname,
       Object newbean) {
     tlabpp.postProcessBeforeInitialization(newbean, beanname);
     for (int i = 0; i < pri.postprocessors.size(); ++i) {
       BeanPostProcessor beanpp = (BeanPostProcessor) pri.postprocessors.get(i);
       try {
         newbean = beanpp.postProcessBeforeInitialization(newbean, beanname);
         // TODO: Timing of this next line is incorrect - should occur after
         // initialisation in caller.
         // beanpp.postProcessAfterInitialization(newbean, beanname);
       }
       catch (Exception e) {
         Logger.log.log(Level.ERROR, "Exception processing bean "
             + newbean.getClass().getName(), e);
       }
     }
     if (newbean instanceof BeanFactoryAware) {
       ((BeanFactoryAware) newbean).setBeanFactory(pri.blfactory);
     }
     if (newbean instanceof BeanNameAware) {
       ((BeanNameAware) newbean).setBeanName(beanname);
     }
     if (newbean instanceof ApplicationContextAware) {
       ((ApplicationContextAware) newbean).setApplicationContext(parentcontext);
     }
     return newbean;
   }
 
   /**
    * This method gets a BeanLocator which is good just for the current request
    * scope. The ThreadLocal barrier has already been breached in the returned
    * object, and evaluation will proceed quickly.
    */
   public WriteableBeanLocator getBeanLocator() {
     assertIsStarted();
     PerRequestInfo pri = getPerRequest();
     return pri.requestwbl;
   }
 
   /**
    * Scope of this BeanLocator is the same as previous, but it will NOT
    * auto-create beans that are not present.
    */
   public WriteableBeanLocator getDeadBeanLocator() {
     assertIsStarted();
     PerRequestInfo pri = getPerRequest();
     return pri.beans;
   }
 
 }
