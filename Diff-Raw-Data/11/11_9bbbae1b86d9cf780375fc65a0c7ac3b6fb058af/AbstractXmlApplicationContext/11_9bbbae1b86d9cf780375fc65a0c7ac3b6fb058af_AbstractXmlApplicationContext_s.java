 package org.springframework.context.support;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Stack;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer.PlaceholderResolver;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.util.ReflectionUtils;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import com.h2.org.springframework.beans.Bean;
 import com.h2.org.springframework.beans.Bean.BeanType;
 import com.h2.org.springframework.beans.ConstructorArg;
 import com.h2.org.springframework.beans.IBeanListener;
 import com.h2.org.springframework.beans.IValueBean;
 import com.h2.org.springframework.beans.PropertyBean;
 import com.h2.org.springframework.beans.UtilBean;
 import com.h2.org.springframework.beans.factory.DResourceManager;
 import com.h2.org.springframework.beans.factory.IResourceManager;
 import com.h2.org.springframework.beans.factory.ResourceResolver;
 import com.h2.org.springframework.beans.factory.SimpleBeanFactory;
 import com.h2.org.springframework.beans.factory.config.DefaultPlaceholderResolver;
 import com.h2.util.lang.reflect.ReflectUtils;
 
 public class AbstractXmlApplicationContext extends AbstractApplicationContext {
    
    private static final Logger LOG = LoggerFactory.getLogger(
          AbstractXmlApplicationContext.class);
    
    ////
    
    protected IResourceManager _resorceManager = new DResourceManager();
    
    private PlaceholderResolver _placeholderResolver;
    
    private ResourceResolver _resourceResolver = new ResourceResolver();
    
    private Stack<Bean> _beanStack = new Stack<Bean>();
    
 //   private Stack<PropertyBean> _propertyBeanStack = new Stack<PropertyBean>();
    
    private Stack<IValueBean> _valueBeanStack = new Stack<IValueBean>();
    
    private Map<String, Bean> _beanPreProcess = new HashMap<String, Bean>();
    
    private Properties _properties = new Properties();
    
    private Collection<IBeanListener> _beanListeners =
          new ArrayList<IBeanListener>();
    
    private Stack<String> _contextStack = new Stack<String>();
    
    ////
    ////
 
    /**
     * Create a new ClassPathXmlApplicationContext for bean-style configuration.
     * @see #setConfigLocation
     * @see #setConfigLocations
     * @see #afterPropertiesSet()
     */
    public AbstractXmlApplicationContext() {
       //nothing
    }
    
    /**
     * Create a new ClassPathXmlApplicationContext, loading the definitions
     * from the given XML file and automatically refreshing the context.
     * @param configLocation resource location
     * @throws BeansException if context creation failed
     */
    public AbstractXmlApplicationContext(String ... configLocations) {
       SimpleBeanFactory factory = new SimpleBeanFactory();
       setBeanFactory(factory);
       
       for (String configLocation : configLocations) {
     	  processContext(configLocation);  
       }
       
       preProcess(factory);
       
       postProcess(factory);
    }
    
    private void preProcess(final SimpleBeanFactory factory) {
       Collection<Bean> beans = getBeanPreProcess().values();
       Collection<Bean> beansToRemove = new ArrayList<Bean>();
       
       //search for preprocessing beans
       for (final Bean bean : beans) {
          if("org.springframework.beans.factory.config.PropertyPlaceholderConfigurer"
                .equals(bean.getClazzName())) {
             try {
                processBean(factory, bean);
                processInnerBean(factory, bean);
             } catch (Throwable exp) {
                LOG.error("Couldn't process " + bean);
                LOG.error(exp.getMessage());
                LOG.debug("Details: ", exp);
             }
             
             //loadLocations
             PropertyPlaceholderConfigurer config = 
                (PropertyPlaceholderConfigurer) bean.getInstantiatedObject();
             try {
                getProperties().load(getResorceManager().getResource(
                      getResourceResolver().resolvePath(
                      config.getLocation())));
             } catch (IOException exp) {
                LOG.error("Couldn't process " + bean);
                LOG.error(exp.getMessage());
                LOG.debug("Details: ", exp);
             }
             setPlaceholderResolver(new DefaultPlaceholderResolver() {
                @Override
                public String getPlaceholder(String strVal) {
                   return getProperties().getProperty(strVal);
                }
             });
          }
          else if("org.springframework.beans.factory.config.MethodInvokingFactoryBean"
                .equals(bean.getClazzName())) {
 //            processBean(bean);
             
             if(bean.getProperties().containsKey("staticMethod")) {
                String value = bean.getProperties().get("staticMethod");
                int index = value.lastIndexOf('.');
                String clazz = value.substring(0, index);
                String methodName = value.substring(index + 1);
                Method method = ReflectionUtils.findMethod(
                      BeanUtils.getClass(clazz), methodName);
                try {
                   bean.setInstantiatedObject(method.invoke(null));
                   
                   bean.setClazz(method.getReturnType());
                } catch (IllegalArgumentException exp) {
                   LOG.error("Couldn't process " + bean);
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                } catch (IllegalAccessException exp) {
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                } catch (InvocationTargetException exp) {
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                }
             }
             factory.addBean(bean);
             beansToRemove.add(bean);
          }
          else if (bean.getDependsOn() != null) {
             //TODO handle multiple beans
             
             getBeanListeners().add(new IBeanListener() {
                
                @Override
                public void afterInitialization(Bean initBean) {
                   if(bean.getDependsOn().equals(initBean.getNameResolved())) {
                      postProcessDependency(factory, bean);
                   }
                }
             });
             beansToRemove.add(bean);
          }
          else if (bean instanceof UtilBean) {
             String type = guessUtilBeanType((UtilBean)bean);
             if(type == null) {
                //add bean to lazy-init
                
             }
             else {
                ((UtilBean)bean).setValueType(type);
             }
          }
       }
       
       for (Bean bean : beansToRemove) {
          beans.remove(bean);
       }
    }
 
    protected void processContext(String location) {
       LOG.trace("Processing context: " + location);
       getContextStack().push(location);
       XmlPullParserFactory factory = null;
       try {
          factory = XmlPullParserFactory.newInstance(
                System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
       } catch (XmlPullParserException exp) {
          LOG.error(exp.getMessage());
          LOG.error("Error processing Context: " + location);
          LOG.debug("Details: ", exp);
       }
       factory.setNamespaceAware(true);
       try {
          XmlPullParser xpp = factory.newPullParser();
          Reader reader = getResorceManager().getReader(
                new File(getResourceResolver().resolvePath(location)));
          xpp.setInput(reader);
          
          processDocument(xpp);
          
          reader.close();
       } catch (XmlPullParserException exp) {
          LOG.error(exp.getMessage());
          LOG.error("Error processing Context: " + location);
          LOG.debug("Details: ", exp);
       } catch (IOException exp) {
          LOG.error(exp.getMessage());
          LOG.error("Error processing Context: " + location);
          LOG.debug("Details: ", exp);
       }
       getContextStack().push(location);
    }
    
    protected void postProcess(SimpleBeanFactory factory) {
       Collection<Bean> beans = getBeanPreProcess().values();
       
       //instantiate
       for (Bean bean : beans) {
          processBean(factory, bean);
       }
       
       //properties
       for (Bean bean : beans) {
          try {
             processInnerBean(factory, bean);
          } catch (Throwable exp) {
             LOG.error("Couldn't process " + bean);
             LOG.error(exp.getMessage());
             LOG.debug("Details: ", exp);
          }
       }
       
      for (Bean bean : beans) {
    	  if (bean.getInstantiatedObject() instanceof ApplicationContextAware) {
    		  ((ApplicationContextAware)bean.getInstantiatedObject()).setApplicationContext(this);
     	  }
       
    		  try {
 				processInitMethod(bean);
    		  } catch (Throwable e) {
 				// TODO Should we throw an exception or just continue?
 				e.printStackTrace();
    		  }  
       
     	  for (IBeanListener listener : getBeanListeners()) {
     		  listener.afterInitialization(bean);
     	  }
       }
    }
    
    protected void postProcessDependency(SimpleBeanFactory factory, Bean bean) {
       try {
          processBean(factory, bean);
          processInnerBean(factory, bean);
       } catch (Throwable exp) {
          LOG.error("Couldn't process " + bean);
          LOG.error(exp.getMessage());
          LOG.debug("Details: ", exp);
       }
    }
    
    protected void processBean(final SimpleBeanFactory factory,
          final Bean bean) {
       
       //already instantiated
       if(bean.getInstantiatedObject() != null) {
          return;
       }
       
       if(bean.getFactoryBean() != null) {
          Object factoryBean = getBeanFactory().getBean(
                bean.getFactoryBean());
          if(factoryBean == null) {
             getBeanListeners().add(new IBeanListener() {
                
                @Override
                public void afterInitialization(Bean initBean) {
                   if(bean.getFactoryBean().equals(initBean.getNameResolved())) {
                      postProcessDependency(factory, bean);
                   }
                }
             });
          }
          else {
             try {
                Method method = factoryBean.getClass().getMethod(
                      bean.getFactoryMethod());
                Object ref = method.invoke(factoryBean);
                if(ref == null) {
                   LOG.warn("Reference is null: "
                         + bean.getFactoryMethod());
                }
                else {
                   bean.setInstantiatedObject(ref);
                   bean.setClazz(ref.getClass());
                }
             } catch (SecurityException exp) {
                LOG.error(exp.getMessage());
                LOG.debug("Details: ", exp);
             } catch (NoSuchMethodException exp) {
                LOG.error("Couldn't process " + bean);
                LOG.error(exp.getMessage());
                LOG.debug("Details: ", exp);
             } catch (IllegalArgumentException exp) {
                LOG.error("Couldn't process " + bean);
                LOG.error(exp.getMessage());
                LOG.debug("Details: ", exp);
             } catch (IllegalAccessException exp) {
                LOG.error(exp.getMessage());
                LOG.debug("Details: ", exp);
             } catch (InvocationTargetException exp) {
                LOG.error(exp.getMessage());
                LOG.debug("Details: ", exp);
             }
          }
       }
       
       //check for parent
       //TODO
       
       if(bean.getClazzName() != null) {
          bean.setClazz(BeanUtils.getClass(bean.getClazzName()));
          if(bean.getConstructorArgs() == null
                || bean.getConstructorArgs().isEmpty()) {
             bean.setInstantiatedObject(BeanUtils.instantiate(bean.getClazz()));
          }
          else {
             Object[] objs = processConstructors(bean);
             try {
                bean.setInstantiatedObject(BeanUtils.instantiateClass(
                      bean.getClazz(),
                      objs));
             } catch (Exception exp) {
                LOG.error("Couldn't process " + bean);
                LOG.error(exp.getMessage());
                LOG.debug("Details: ", exp);
                return;
             }
          }
       }
       
       factory.addBean(bean);
    }
    
    protected Object[] processConstructors(Bean bean) {
       
       Class<?>[] constructorArgClasses = new Class<?>[
             bean.getConstructorArgs().size()];
       ConstructorArg arg = null;
       for (int i = 0; i < constructorArgClasses.length; i++) {
          try {
             arg = bean.getConstructorArgs().get(i);
             if (arg.getType() == null) {
                if (arg.getBean() != null) {
                   constructorArgClasses[i] = arg.getBean().getClazz();
                }
                if (arg.getRef() != null) {
                   Object ref = getBeanFactory().getBean(arg.getRef());
                   Bean refBean = new Bean();
                   refBean.setClazz(ref.getClass());
                   refBean.setInstantiatedObject(ref);
                   refBean.setContext(bean.getContext());
                   arg.setBean(refBean);
                   constructorArgClasses[i] = ref.getClass();
                }
                continue;
             }
             constructorArgClasses[i] = Class.forName(arg.getType());
          } catch (ClassNotFoundException exp) {
             LOG.error(exp.getMessage());
             LOG.debug("Details: ", exp);
          }
       }
       
       Constructor<?> method = ReflectUtils.getConstructor(bean.getClazz(),
             constructorArgClasses);
    
       if(method == null) {
          LOG.error(String.format(
                "Constructor does not exist in %s for %d",
                bean.getClazz(),
                bean.getConstructorArgs().size()));
          return null;
       }
       
       
       for (int i = 0; i < bean.getConstructorArgs().size(); i++) {
          arg = bean.getConstructorArgs().get(i);
          if(arg.getType() == null) {
             arg.setType(method.getParameterTypes()[i].getName());
          }
       }
       
       for (int i = 0; i < bean.getConstructorArgs().size(); i++) {
          arg = bean.getConstructorArgs().get(i);
          
          Object ref = null;
          if(arg.getBean() != null) {
             ref = arg.getBean().getInstantiatedObject();
          }
          else if(arg.getRef() != null) {
             ref = getBeanFactory().getBean(arg.getRef());
          }
          else if(arg.getValue() != null) {
             ref = BeanUtils.convertToBean(
                   (getPlaceholderResolver() == null) ? arg.getValue()
                         : getPlaceholderResolver().resolvePlaceholder(
                               arg.getValue()),
                               method.getParameterTypes()[i]);
          }
          arg.setInstantiatedObject(ref);
       }
       
       Object[] results = new Object[bean.getConstructorArgs().size()];
       for (int i = 0; i < bean.getConstructorArgs().size(); i++) {
          arg = bean.getConstructorArgs().get(i);
          results[i] = arg.getInstantiatedObject();
       }
       return results;
    }
    
    protected void processInnerBean(SimpleBeanFactory factory, Bean bean)
          throws Throwable {
 
       Collection<IValueBean> beanProperties = bean.getBeanProperties();
       if(beanProperties != null) {
          
          //utility properties
          if(bean instanceof UtilBean) {
             
             Method method = null;
             if(bean.getInstantiatedObject() instanceof List
                   || bean.getInstantiatedObject() instanceof Set) {
                method = ReflectionUtils.findMethod(
                      bean.getInstantiatedObject().getClass(), "add",
                      Object.class);
                
                for (IValueBean prop : beanProperties) {
                   Object ref = evaluateProperty(factory, bean, prop, method);
                   try {
                      method.invoke(bean.getInstantiatedObject(), ref);
                   } catch (IllegalArgumentException exp) {
                      LOG.error("Couldn't process " + bean);
                      LOG.error(exp.getMessage());
                      LOG.debug("Details: ", exp);
                      LOG.error("Method: " + method);
                   } catch (IllegalAccessException exp) {
                      LOG.error(exp.getMessage());
                      LOG.debug("Details: ", exp);
                      LOG.error("Method: " + method);
                   } catch (InvocationTargetException exp) {
                      LOG.error(exp.getMessage());
                      LOG.debug("Details: ", exp);
                      LOG.error("Method: " + method);
                      throw exp.getCause();
                   }
                }
             }
             else if(bean.getInstantiatedObject() instanceof Map) {
                method = ReflectionUtils.findMethod(List.class, "put",
                      Object.class, Object.class);
             }
             
          }
          else {
             for (IValueBean prop : beanProperties) {
                
                //find property method
                Method method = ReflectUtils.getSetterMethod(bean.getClazz(),
                      prop.getName());
                if(method == null) {
                   LOG.error(String.format(
                         "Method does not exist in %s for %s",
                         bean.getClazz(),
                         prop.getName()));
                   continue;
                }
                
                //find reference bean
                Object ref = evaluateProperty(factory, bean, prop, method);
                if(ref == null) {
                   LOG.error(String.format(
                         "Reference is null for %s %s.",
                         bean.getClazz(),
                         prop.getName()));
                   continue;
                }
                
                //call setter method
                try {
                   method.invoke(bean.getInstantiatedObject(), ref);
                } catch (IllegalArgumentException exp) {
                   LOG.error("Couldn't process " + bean);
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                   LOG.error("Method: " + method);
                } catch (IllegalAccessException exp) {
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                   LOG.error("Method: " + method);
                } catch (InvocationTargetException exp) {
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                   LOG.error("Method: " + method);
                   throw exp.getCause();
                }
             }
          }
          
          Map<String, String> properties = bean.getProperties();
          if(properties != null) {
             for (Entry<String, String> entry : properties.entrySet()) {
                
                Object ref = null;
                //resolve property
                Method method = ReflectUtils.getSetterMethod(bean.getClazz(),
                      entry.getKey());
                if(method == null) {
                   LOG.error("Couldn't process " + bean);
                   LOG.error(String.format(
                         "Method does not exist in %s for %s",
                         bean.getClazz(),
                         entry.getKey()));
                   continue;
                }
                ref = BeanUtils.convertToBean(
                      (getPlaceholderResolver() == null) ? entry.getValue()
                            : getPlaceholderResolver().resolvePlaceholder(
                                  entry.getValue()),
                      method.getParameterTypes()[0]);
                
                //call setter method
                method = ReflectUtils.getSetterMethod(bean.getClazz(),
                      entry.getKey(), method.getParameterTypes()[0]);
                if(method == null) {
                   LOG.error("Couldn't process " + bean);
                   LOG.error(String.format(
                         "Method does not exist in %s for %s",
                         bean.getClazz(),
                         entry.getKey()));
                   continue;
                }
                try {
                   method.invoke(bean.getInstantiatedObject(), ref);
                } catch (IllegalArgumentException exp) {
                   LOG.error("Couldn't process " + bean);
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                } catch (IllegalAccessException exp) {
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                } catch (InvocationTargetException exp) {
                   LOG.error(exp.getMessage());
                   LOG.debug("Details: ", exp);
                }
             }
          }
       }
    }
    
    protected Object evaluateProperty(SimpleBeanFactory factory, Bean parentBean,
          IValueBean prop, Method method) {
       Object ref = null;
       if(prop.getRef() != null) {
          ref = getBeanFactory().getBean(prop.getRef());
       }
       else if(prop.getBean() != null) {
          try {
             //need to create bean
             processBean(factory, prop.getBean());
             processInnerBean(factory, prop.getBean());
          } catch (Throwable exp) {
             LOG.error("Couldn't process " + prop.getBean());
             LOG.error(exp.getMessage());
             LOG.debug("Details: ", exp);
          }
          
          ref = prop.getBean().getInstantiatedObject();
       }
       else if(prop.getValue() != null && parentBean instanceof UtilBean) {
          ref = BeanUtils.convertToBean(
                (getPlaceholderResolver() == null) ? prop.getValue()
                      : getPlaceholderResolver().resolvePlaceholder(
                            prop.getValue()),
                ((UtilBean)parentBean).getValueType());
       }
       else if(prop.getValue() != null) {
          ref = BeanUtils.convertToBean(
                (getPlaceholderResolver() == null) ? prop.getValue()
                      : getPlaceholderResolver().resolvePlaceholder(
                            prop.getValue()),
                method.getParameterTypes()[0]);
       }
       return ref;
    }
    
    protected void processInitMethod(Bean bean) throws Throwable {
       if(bean.getInitMethod() != null) {
          Method method = null;
          try {
             method = bean.getClazz().getMethod(bean.getInitMethod());
          } catch (SecurityException exp) {
             //do nothing
          } catch (NoSuchMethodException exp) {
             //do nothing
          }
          if(method == null) {
             LOG.error("Couldn't process " + bean);
             LOG.error(String.format(
                   "Method does not exist in %s for %s",
                   bean.getClazz(),
                   bean.getInitMethod()));
             return;
          }
          try {
             method.invoke(bean.getInstantiatedObject());
          } catch (IllegalArgumentException exp) {
             LOG.error(exp.getMessage());
             LOG.debug("Details: ", exp);
          } catch (IllegalAccessException exp) {
             LOG.debug("Details: ", exp);
          } catch (InvocationTargetException exp) {
             LOG.error(exp.getMessage());
             LOG.debug("Details: ", exp);
             throw exp.getCause();
          }
       }
    }
    
    /**
     * Determines a UtilBean's generic type.
     *
     * @param bean
     */
    protected String guessUtilBeanType(UtilBean bean) {
       String clazzName = null;
       
       if(bean.getValueType() != null) {
          return bean.getValueType();
       }
       
       Collection<IValueBean> beanProperties = bean.getBeanProperties();
       for (IValueBean prop : beanProperties) {
          if(prop.getRef() != null) {
             Bean refBean = getBeanPreProcess().get(prop.getRef());
             if(refBean.getClazzName() != null) {
                clazzName = refBean.getClazzName();
             }
          }
          else if (prop.getBean() != null) {
             Bean refBean = prop.getBean();
             clazzName = refBean.getClazzName();
          }
       }
       
       return clazzName;
    }
    
    ////
    ////
 
    public void processDocument(XmlPullParser xpp)
          throws XmlPullParserException, IOException {
       int eventType = xpp.getEventType();
       do {
           if(eventType == XmlPullParser.START_DOCUMENT) {
              LOG.trace("Start Document");
           }
           else if(eventType == XmlPullParser.END_DOCUMENT) {
              LOG.trace("End document");
           }
           else if(eventType == XmlPullParser.START_TAG) {
               processStartElement(xpp);
           }
           else if(eventType == XmlPullParser.END_TAG) {
               processEndElement(xpp);
           }
           else if(eventType == XmlPullParser.TEXT) {
               processText(xpp);
           }
           eventType = xpp.next();
       } while (eventType != XmlPullParser.END_DOCUMENT);
    }
 
    public void processStartElement (XmlPullParser xpp) {
       String name = xpp.getName();
       
       if("bean".equals(name)) {
          Bean bean = initBean(new Bean());
          getBeanStack().push(bean);
          
          processBeanAttributes(xpp);
          LOG.debug("Created: " + bean);
       }
       else if("property".equals(name)) {
          PropertyBean prop = new PropertyBean();
          getValueBeanStack().push(prop);
          
          processPropertyBeanAttributes(xpp);
          LOG.debug("Created: " + prop);
       }
       else if("constructor-arg".equals(name)) {
          ConstructorArg arg = new ConstructorArg();
          getValueBeanStack().push(arg);
          
          processBeanConstructorArg(xpp);
       }
       else if("value".equals(name)) {
          
       }
       else if("ref".equals(name)) {
          PropertyBean prop = new PropertyBean();
          getValueBeanStack().push(prop);
          
          processRef(xpp);
       }
       else if("list".equals(name)) {
          Bean bean = initBean(new UtilBean());
          getBeanStack().push(bean);
          bean.setClazzName(ArrayList.class.getName());
          
          processUtilBeanAttributes(xpp);
       }
       else if("set".equals(name)) {
          Bean bean = initBean(new UtilBean());
          getBeanStack().push(bean);
          bean.setClazzName(HashSet.class.getName());
          
          processUtilBeanAttributes(xpp);
       }
       else if("map".equals(name)) {
          Bean bean = initBean(new UtilBean());
          getBeanStack().push(bean);
          bean.setClazzName(HashMap.class.getName());
          
          processUtilBeanAttributes(xpp);
       }
       else if("properties".equals(name)) {
          //TODO
       }
       else if("import".equals(name)) {
          processImportAttributes(xpp);
       }
    }
    
    public void processEndElement (XmlPullParser xpp) {
       String name = xpp.getName();
       
       if("bean".equals(name)
             || "list".equals(name)
             || "set".equals(name)
             || "map".equals(name)) {
          Bean lastBean = getBeanStack().pop();
          
          if(!getBeanStack().isEmpty()
                && getBeanStack().peek() instanceof UtilBean) {
             PropertyBean prop = new PropertyBean();
             prop.setBean(lastBean);
             getBeanStack().peek().getBeanProperties().add(prop);
          }
          else if(!getValueBeanStack().isEmpty()) {
             //push to parent
             getValueBeanStack().peek().setBean(lastBean);
          }
          
          //add top level beans
          if(getBeanStack().isEmpty() && lastBean.getNameResolved() != null) {
             getBeanPreProcess().put(lastBean.getNameResolved(), lastBean);
          }
       }
       else if("property".equals(name) || "ref".equals(name)) {
          getBeanStack().peek().getBeanProperties().add(
                getValueBeanStack().pop());
       }
       else if("constructor-arg".equals(name)) {
          getBeanStack().peek().getConstructorArgs().add(
                (ConstructorArg) getValueBeanStack().pop());
       }
       else if("properties".equals(name)) {
          //TODO
       }
    }
    
    private void processBeanConstructorArg(XmlPullParser xpp) {
       int count = xpp.getAttributeCount();
       
       ConstructorArg arg = (ConstructorArg) getValueBeanStack().peek();
       for (int i = 0; i < count; i++) {
          String name = xpp.getAttributeName(i);
          String value = xpp.getAttributeValue(i);
          
          if("value".equals(name)) {
             arg.setValue(value);
          }
          else if("type".equals(name)) {
             arg.setType(value);
          }
          else if("ref".equals(name)) {
             arg.setRef(value);
          }
          else if("name".equals(name)) {
             arg.setName(value);
          }
          else if("index".equals(name)) {
             arg.setIndex(Integer.parseInt(value));
          }
       }
    }
 
    private Bean initBean(Bean bean) {
       bean.setProperties(new HashMap<String, String>());
       bean.setBeanProperties(new ArrayList<IValueBean>());
       bean.setType(BeanType.OBJECT);
       bean.setContext(getContextStack().peek());
       return bean;
    }
    
    protected void processImportAttributes(XmlPullParser xpp) {
       int count = xpp.getAttributeCount();
       
       for (int i = 0; i < count; i++) {
          String name = xpp.getAttributeName(i);
 //         String prefix = xpp.getAttributePrefix(i);
          String value = xpp.getAttributeValue(i);
          
          if("resource".equals(name)) {
             processContext(value);
          }
       }
    }
    
    protected void processRef(XmlPullParser xpp) {
       int count = xpp.getAttributeCount();
       
       for (int i = 0; i < count; i++) {
          String name = xpp.getAttributeName(i);
          String value = xpp.getAttributeValue(i);
          
          if("local".equals(name) || "bean".equals(name)) {
 //            if(getBeanStack().peek() instanceof UtilBean) {
 //               PropertyBean prop = new PropertyBean();
 //               prop.setRef(value);
 //               getBeanStack().peek().getBeanProperties().add(prop);
 //            }
             getValueBeanStack().peek().setRef(value);
          }
       }
    }
 
    private void processPropertyBeanAttributes(XmlPullParser xpp) {
       IValueBean prop = getValueBeanStack().peek();
       int count = xpp.getAttributeCount();
       
       for (int i = 0; i < count; i++) {
          String name = xpp.getAttributeName(i);
 //         String prefix = xpp.getAttributePrefix(i);
          String value = xpp.getAttributeValue(i);
          
          if("name".equals(name)) {
             prop.setName(value);
          }
          else if("value".equals(name)) {
             prop.setValue(value);
          }
       }
    }
 
    public void processBeanAttributes(XmlPullParser xpp) {
       Bean bean = getBeanStack().peek();
       int count = xpp.getAttributeCount();
       
       for (int i = 0; i < count; i++) {
          String name = xpp.getAttributeName(i);
          String prefix = xpp.getAttributePrefix(i);
          String value = xpp.getAttributeValue(i);
          
          if("p".equals(prefix)) {
             if(name.endsWith("-ref")) {
                PropertyBean prop = new PropertyBean();
                prop.setName(name.substring(0,
                      name.length() - "-ref".length()));
                prop.setRef(value);
                bean.getBeanProperties().add(prop);
             }
             else {
                bean.getProperties().put(name, value);
             }
             
          }
          else if("id".equals(name)) {
             bean.setId(value);
          }
          else if("name".equals(name)) {
             bean.setName(value);
          }
          else if("class".equals(name)) {
             bean.setClazzName(value);
          }
          else if("init-method".equals(name)) {
             bean.setInitMethod(value);
          }
          else if("abstract".equals(name)) {
             bean.setAbstract(Boolean.parseBoolean(value));
          }
          else if("parent".equals(name)) {
             bean.setParent(value);
          }
          else if("scope".equals(name)) {
             bean.setScope(value);
          }
          else if("factory-bean".equals(name)) {
             bean.setFactoryBean(value);
          }
          else if("factory-method".equals(name)) {
             bean.setFactoryMethod(value);
          }
          else if("depends-on".equals(name)) {
             bean.setDependsOn(value);
          }
       }
    }
    
    public void processUtilBeanAttributes(XmlPullParser xpp) {
       UtilBean bean = (UtilBean)getBeanStack().peek();
       int count = xpp.getAttributeCount();
       
       for (int i = 0; i < count; i++) {
          String name = xpp.getAttributeName(i);
          String value = xpp.getAttributeValue(i);
          
          if("id".equals(name)) {
             bean.setId(value);
          }
          else if("name".equals(name)) {
             bean.setName(value);
          }
          else if("scope".equals(name)) {
             bean.setScope(value);
          }
          else if(name.endsWith("-class")) {
             bean.setClazzName(value);
          }
          else if("value-type".equals(name)) {
             bean.setValueType(value);
          }
       }
    }
 
    public void processText (XmlPullParser xpp) throws XmlPullParserException {
       
       String text = xpp.getText();
       if(text.matches("\\s*")) {
          return;
       }
       else if(!getBeanStack().isEmpty()
             && getBeanStack().peek() instanceof UtilBean) {
          PropertyBean prop = new PropertyBean();
          prop.setValue(text);
          getBeanStack().peek().getBeanProperties().add(prop);
       }
       else if(!getValueBeanStack().isEmpty()) {
          getValueBeanStack().peek().setValue(text);
       }
    }
 
    /**
     * @return the resorceManager
     */
    public IResourceManager getResorceManager() {
       return _resorceManager;
    }
 
    /**
     * @param resorceManager the resorceManager to set
     */
    public void setResorceManager(IResourceManager resorceManager) {
       _resorceManager = resorceManager;
    }
 
    /**
     * @return the beans
     */
    public Stack<Bean> getBeanStack() {
       return _beanStack;
    }
 
    /**
     * @param beans the beans to set
     */
    public void setBeanStack(Stack<Bean> beans) {
       _beanStack = beans;
    }
 
    /**
     * @return the beanPreProcess
     */
    public Map<String, Bean> getBeanPreProcess() {
       return _beanPreProcess;
    }
 
    /**
     * @param beanPreProcess the beanPreProcess to set
     */
    public void setBeanPreProcess(Map<String, Bean> beanPreProcess) {
       _beanPreProcess = beanPreProcess;
    }
 
 //   /**
 //    * @return the propertyBeanStack
 //    */
 //   public Stack<PropertyBean> getPropertyBeanStack() {
 //      return _propertyBeanStack;
 //   }
 //
 //   /**
 //    * @param propertyBeanStack the propertyBeanStack to set
 //    */
 //   public void setPropertyBeanStack(Stack<PropertyBean> propertyBeanStack) {
 //      _propertyBeanStack = propertyBeanStack;
 //   }
 
    /**
     * @return the resourceResolver
     */
    public ResourceResolver getResourceResolver() {
       return _resourceResolver;
    }
 
    /**
     * @param resourceResolver the resourceResolver to set
     */
    public void setResourceResolver(ResourceResolver resourceResolver) {
       _resourceResolver = resourceResolver;
    }
 
    /**
     * @return the placeholderResolver
     */
    public PlaceholderResolver getPlaceholderResolver() {
       return _placeholderResolver;
    }
 
    /**
     * @param placeholderResolver the placeholderResolver to set
     */
    public void setPlaceholderResolver(PlaceholderResolver placeholderResolver) {
       _placeholderResolver = placeholderResolver;
    }
 
    /**
     * @return the properties
     */
    public Properties getProperties() {
       return _properties;
    }
 
    /**
     * @param properties the properties to set
     */
    public void setProperties(Properties properties) {
       _properties = properties;
    }
 
    /**
     * @return the beanListeners
     */
    public Collection<IBeanListener> getBeanListeners() {
       return _beanListeners;
    }
 
    /**
     * @param beanListeners the beanListeners to set
     */
    public void setBeanListeners(Collection<IBeanListener> beanListeners) {
       _beanListeners = beanListeners;
    }
 
    /**
     * @return the valueBeanStack
     */
    public Stack<IValueBean> getValueBeanStack() {
       return _valueBeanStack;
    }
 
    /**
     * @param valueBeanStack the valueBeanStack to set
     */
    public void setValueBeanStack(Stack<IValueBean> valueBeanStack) {
       _valueBeanStack = valueBeanStack;
    }
 
    /**
     * @return the contextStack
     */
    public Stack<String> getContextStack() {
       return _contextStack;
    }
 
    /**
     * @param contextStack the contextStack to set
     */
    public void setContextStack(Stack<String> contextStack) {
       _contextStack = contextStack;
    }
    
 }
