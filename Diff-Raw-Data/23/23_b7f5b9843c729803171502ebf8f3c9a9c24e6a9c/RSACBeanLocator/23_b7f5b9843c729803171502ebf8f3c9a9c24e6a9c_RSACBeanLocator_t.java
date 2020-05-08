 /*
  * Created on Sep 18, 2005
  */
 package uk.org.ponder.springutil;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Level;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.PropertyValue;
 import org.springframework.beans.factory.FactoryBean;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.BeanDefinitionHolder;
 import org.springframework.beans.factory.config.BeanPostProcessor;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.config.RuntimeBeanReference;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.beans.factory.support.ManagedList;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ConfigurableApplicationContext;
 
import uk.org.ponder.beanutil.ConcreteWBL;
import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.saxalizer.AccessMethod;
 import uk.org.ponder.saxalizer.MethodAnalyser;
 import uk.org.ponder.saxalizer.SAXalizerMappingContext;
 import uk.org.ponder.servletutil.RequestPostProcessor;
 import uk.org.ponder.servletutil.ServletRequestWrapper;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.ReflectiveCache;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * The central class managing the Request Scope Application Context. This class
  * will be due for some refactoring as soon as the next piece of functionality
  * gets added, getting on for 400 lines. Please note that this class currently
  * illegally casts BeanDefinitions received from Spring to
  * AbstractBeanDefinition, which is a potential dependency weakness. This
  * approach is known to work with Spring 1.1.2 through 1.1.5. It also calls the
  * deprecated method BeanDefinition.getBeanClass().
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class RSACBeanLocator implements ApplicationContextAware,
     ServletRequestWrapper {
   private ConfigurableApplicationContext blankcontext;
   private ApplicationContext livecontext;
   private SAXalizerMappingContext smc;
 
   public RSACBeanLocator(ConfigurableApplicationContext context) {
     this.blankcontext = context;
   }
 
   public void setMappingContext(SAXalizerMappingContext smc) {
     this.smc = smc;
   }
 
   public void setApplicationContext(ApplicationContext applicationContext) {
     livecontext = applicationContext;
   }
 
   /**
    * This method is to be used in the awkward situation where a request is
    * subject to a RequestDispatch partway through its lifecycle, and the request
    * visible to it is not the one for which the RSAC context is required to
    * execute, but client nonetheless wishes to populate or amend some beans into
    * the request scope. Clearly none of these beans should be RequestAware. <br>
    * This method has the effect of marking the bean container both onto the
    * current thread and onto a request attributes, which it is assumed the
    * request dispatcher has the sense not to trash.
    */
   public void protoStartRequest(HttpServletRequest request) {
     threadlocal.set(new PerRequestInfo());
     RSACUtils.setRequestApplicationContext(request, getBeanLocator());
   }
 
   /**
    * Initialises the RSAC container if it has not already been so by
    * protoStartRequest. Enters the supplied request and response as
    * postprocessors for any RequestAware beans in the container.
    */
   public void startRequest(HttpServletRequest request,
       HttpServletResponse response) {
     Object getter = threadlocal.get();
     if (getter == null) {
       protoStartRequest(request);
     }
     RequestPostProcessor rpp = new RequestPostProcessor(request, response);
     addPostProcessor(rpp);
   }
 
   /**
    * Called at the end of a request. I advise doing this in a finally block.
    */
   public void endRequest() {
     PerRequestInfo pri = getPerRequest();
     for (int i = 0; i < pri.todestroy.size(); ++i) {
       String todestroyname = pri.todestroy.stringAt(i);
       String destroymethod = (String) rbimap.get(todestroyname);
       Object todestroy = null;
       try {
         todestroy = getBean(pri, todestroyname);
         ReflectiveCache.invokeMethod(todestroy, destroymethod);
       }
       // must try to destroy as many beans as possible, cannot propagate
       // exception in a finally block in any case.
       catch (Exception e) {
         Logger.log.error("Error destroying bean " + todestroy + " with name "
             + todestroyname, e);
       }
     }
     threadlocal.set(null);
   }
 
   // magic evil code from AbstractBeanFactory l.443 - this is the main reason
   // I abandoned Spring Forms and the like, and it will return to plague us.
   // Just take a look at the constructor for BeanWrapperImpl - one of these
   // is created for EVERY BEAN IN A FACTORY!
 
   // protected BeanWrapper createBeanWrapper(Object beanInstance) {
   // return (beanInstance != null ? new BeanWrapperImpl(beanInstance) : new
   // BeanWrapperImpl());
   // }
 
   // this method is really
   // resolveValueIfNecessary **LITE**, we assume all other resolution
   // is done by the parent factory and are ONLY interested in propertyvalues
   // that refer to other beans IN THIS CONTAINER.
   // org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory
   // l.900:
   // protected Object resolveValueIfNecessary(
   // String beanName, RootBeanDefinition mergedBeanDefinition, String argName,
   // Object value)
   // throws BeansException {
 
   // returns either a String or StringList of bean names.
   private static Object propertyValueToBeanName(Object value) {
     Object beanname = null;
     if (value instanceof BeanDefinitionHolder) {
       // Resolve BeanDefinitionHolder: contains BeanDefinition with name and
       // aliases.
       BeanDefinitionHolder bdHolder = (BeanDefinitionHolder) value;
       beanname = bdHolder.getBeanName();
     }
     else if (value instanceof BeanDefinition) {
       throw new IllegalArgumentException(
           "No idea what to do with bean definition!");
     }
     else if (value instanceof RuntimeBeanReference) {
       RuntimeBeanReference ref = (RuntimeBeanReference) value;
       beanname = ref.getBeanName();
     }
     else if (value instanceof ManagedList) {
       List valuelist = (List) value;
       StringList togo = new StringList();
       for (int i = 0; i < valuelist.size(); ++i) {
         String thisbeanname = (String) propertyValueToBeanName(valuelist.get(i));
         togo.add(thisbeanname);
       }
       beanname = togo;
     }
     return beanname;
   }
 
   // the static information stored about each bean.
   private static class RSACBeanInfo {
     // The ACTUAL class of the bean to be FIRST constructed. The class of the
     // resultant bean may differ for a factory bean.
     Class beanclass;
     boolean isfactorybean = false;
     String initmethod;
     String destroymethod;
     String factorybean;
     String factorymethod;
     // key is dependent bean name, value is property name.
     // ultimately we will cache introspection info here.
     private HashMap localdepends = new HashMap();
 
     public void recordDependency(String propertyname, Object beannames) {
       localdepends.put(propertyname, beannames);
     }
 
     public Iterator dependencies() {
       return localdepends.keySet().iterator();
     }
 
     public Object beannames(String propertyname) {
       return localdepends.get(propertyname);
     }
   }
 
   // this is a map of bean names to RSACBeanInfo
   private HashMap rbimap;
 
   public void init() {
     // at this point we actually expect that the "Dead" factory is FULLY
     // CREATED. This checks that all dependencies are resolvable (if this
     // has not already been checked by the IDE).
     String[] beanNames = blankcontext.getBeanDefinitionNames();
     ConfigurableListableBeanFactory factory = blankcontext.getBeanFactory();
     // prepare our list of dependencies.
 
     rbimap = new HashMap();
 
     for (int i = 0; i < beanNames.length; i++) {
       String beanname = beanNames[i];
       RSACBeanInfo rbi = new RSACBeanInfo();
       BeanDefinition def = factory.getBeanDefinition(beanname);
       MutablePropertyValues pvs = def.getPropertyValues();
       PropertyValue[] values = pvs.getPropertyValues();
       for (int j = 0; j < values.length; ++j) {
         PropertyValue thispv = values[j];
         Object beannames = propertyValueToBeanName(thispv.getValue());
         boolean skip = false;
         // skip recording the dependency if it was unresolvable (some
         // unrecognised
         // type) or was a single-valued type referring to a static dependency.
         // NB - we now record ALL dependencies - bean-copying strategy
         // discontinued.
         if (beannames == null
         // || beannames instanceof String
         // && !blankcontext.containsBean((String) beannames)
         ) {
           skip = true;
         }
         if (!skip) {
           rbi.recordDependency(thispv.getName(), beannames);
         }
       }
       // yes, I am PERFECTLY aware this is a deprecated method, but it is the
       // only
       // visible way to determine ahead of time whether this will be a factory
       // bean.
       Class beanclass = def.getBeanClass();
       rbi.beanclass = beanclass;
 
       rbi.isfactorybean = FactoryBean.class.isAssignableFrom(beanclass);
       AbstractBeanDefinition abd = (AbstractBeanDefinition) def;
       rbi.initmethod = abd.getInitMethodName();
       rbi.destroymethod = abd.getDestroyMethodName();
       rbi.factorybean = abd.getFactoryBeanName();
       rbi.factorymethod = abd.getFactoryMethodName();
       rbimap.put(beanname, rbi);
     }
   }
 
   private ThreadLocal threadlocal = new ThreadLocal();
 
   private PerRequestInfo getPerRequest() {
     return (PerRequestInfo) threadlocal.get();
   }
 
   private static class PerRequestInfo {
    //HashMap beans = new HashMap();
    ConcreteWBL beans = new ConcreteWBL();
     ArrayList postprocessors = new ArrayList();
     StringList todestroy = new StringList();
   }
 
   public void addPostProcessor(BeanPostProcessor beanpp) {
     getPerRequest().postprocessors.add(beanpp);
   }
 
   private Object getLocalBean(PerRequestInfo pri, String beanname) {
    Object bean = pri.beans.locateBean(beanname);
     if (bean == null) {
       bean = createBean(pri, beanname);
     }
     return bean;
   }
 
   private Object getBean(PerRequestInfo pri, String beanname) {
     Object bean = null;
     if (blankcontext.containsBean(beanname)) {
       bean = getLocalBean(pri, beanname);
     }
     else {
       bean = this.livecontext.getBean(beanname);
     }
     return bean;
   }
 
   private Object createBean(PerRequestInfo pri, String beanname) {
     RSACBeanInfo rbi = (RSACBeanInfo) rbimap.get(beanname);
     
     Object newbean;
     if (rbi.factorybean != null) {
       Object factorybean = getBean(pri, rbi.factorybean);
       newbean = ReflectiveCache.invokeMethod(factorybean, rbi.factorymethod);
       rbi.beanclass = newbean.getClass();
     }
     else {
     // Locate the "dead" bean from the genuine Spring context, and clone it
     // as quick as we can - bytecodes might do faster but in the meantime
     // observe that a clone typically costs 1.6 reflective calls so in general
     // this method will win over a reflective solution.
     // NB - all Copiables simply copy dependencies manually for now, no cost.
     // Copiable deadbean = (Copiable) livecontext.getBean(rbi.isfactorybean? "&"
     // +beanname : beanname);
     // All the same, the following line will cost us close to 1us - unless it
     // invokes manual code!
       newbean = ReflectiveCache.construct(rbi.beanclass);
     }
     MethodAnalyser ma = MethodAnalyser.getMethodAnalyser(rbi.beanclass, smc);
     // Object clonebean = deadbean.copy();
     // iterate over each LOCAL dependency of the bean with given name.
     for (Iterator depit = rbi.dependencies(); depit.hasNext();) {
       String propertyname = (String) depit.next();
       Object depbean = null;
       Object beanref = rbi.beannames(propertyname);
       if (beanref instanceof String) {
         depbean = getBean(pri, (String) beanref);
       }
       else {
         ArrayList beanlist = new ArrayList();
         StringList beannames = (StringList) beanref;
         for (int i = 0; i < beannames.size(); ++i) {
           String thisbeanname = beannames.stringAt(i);
           Object bean = getBean(pri, thisbeanname);
           beanlist.add(bean);
         }
         depbean = beanlist;
       }
 
       AccessMethod setter = ma.getAccessMethod(propertyname);
       // Deliver the dependent bean as member. This will at this point
       // OVERWRITE the "dead" bean inherited from the cloned parent.
       // Lose another 500ns here, until we bring on FastClass.
       setter.setChildObject(newbean, depbean);
     }
     // process it FIRST since it will be the factory that is expecting the
     // dependencies
     // set!
     processNewBean(pri, beanname, newbean);
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
    pri.beans.set(beanname, newbean);
     if (rbi.initmethod != null) {
       ReflectiveCache.invokeMethod(newbean, rbi.initmethod);
     }
     if (rbi.destroymethod != null) {
       pri.todestroy.add(beanname);
     }
 
     return newbean;
   }
 
   private void processNewBean(PerRequestInfo pri, String beanname,
       Object clonebean) {
     for (int i = 0; i < pri.postprocessors.size(); ++i) {
       BeanPostProcessor beanpp = (BeanPostProcessor) pri.postprocessors.get(i);
       try {
         beanpp.postProcessBeforeInitialization(clonebean, beanname);
         // someday we might put something in between here.
         beanpp.postProcessAfterInitialization(clonebean, beanname);
       }
       catch (Exception e) {
         Logger.log.log(Level.ERROR, "Exception processing bean "
             + clonebean.getClass().getName(), e);
       }
     }
   }
 
   /**
    * This method gets a BeanLocator which is good just for the current request
    * scope. The ThreadLocal barrier has already been breached in the returned
    * object, and evaluation will proceed quickly.
    */
  public WriteableBeanLocator getBeanLocator() {
     final PerRequestInfo pri = getPerRequest();
    return pri.beans;
   }
 
   /**
    * This method is a hack to prevent PostHandler from needing to be RSACed just
    * yet. It performs a ThreadLocal get for the current bean container on each
    * call.
    */
   // public Object getBean(String beanname) {
   // PerRequestInfo pri = getPerRequest();
   // return getLocalBean(pri, beanname);
   // }
 }
