 package com.h2.org.springframework.beans.factory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 
 import com.h2.org.springframework.beans.Bean;
 
 public class SimpleBeanFactory implements BeanFactory {
    
    private Map<Class<?>, Map<String,Object>> _classesByType =
       new HashMap<Class<?>, Map<String,Object>>();
    
    private Map<String, Object> _beansByName =
          new HashMap<String, Object>();
    
    ////
    ////
    
    public void addBean(Bean bean) {
 	   Object instance = bean.getInstantiatedObject();
 	   Class superClass = instance.getClass();
 	   
 	   while (!"java.lang.Object".equals(superClass.getName())) {
 		   Class[] interfaces = superClass.getInterfaces();
 		   
 		   if (interfaces != null) {
 			   for (Class interfaze : interfaces) {
 				   Map<String,Object> beans = (Map<String,Object>)getClassesByType().get(interfaze);
 				   if (beans == null) {
 					   beans = new HashMap<String,Object>();
 					   getClassesByType().put(interfaze, beans);
 				   }
				   beans.put(bean.getNameResolved(), instance);
 			   }
 		   }
 		   superClass = superClass.getSuperclass();
 	   }
 	   
 	   Map<String,Object>beans = new HashMap<String,Object>();
	   beans.put(bean.getNameResolved(), instance);
 	   
 	   getClassesByType().put(bean.getClazz(), beans);
       if(bean.getNameResolved() != null) {
          getBeansByName().put(bean.getNameResolved(), instance);
       }
    }
 
    @Override
    public Object getBean(String name) throws BeansException {
       return getBeansByName().get(name);
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(String name, Class<T> requiredType)
          throws BeansException {
       return (T)getBeansByName().get(name);
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
       return (T) getClassesByType().get(requiredType);
    }
 
    public <T> Map<String,T> getBeansOfType(Class<T> requiredType) throws BeansException {
 	   Map<String,T> beans = new HashMap<String,T>();
 	   Map<String, Object> m = getClassesByType().get(requiredType);
 	   if (m != null) {
 		   beans.putAll((Map<String,T>)m);
 	   }
 	   return beans;
    }
    
    @Override
    public Object getBean(String name, Object... args) throws BeansException {
       // TODO Auto-generated method stub
       return null;
    }
 
    @Override
    public boolean containsBean(String name) {
       // TODO Auto-generated method stub
       return false;
    }
 
    @Override
    public boolean isSingleton(String name) throws BeansException {
       // TODO Auto-generated method stub
       return false;
    }
 
    @Override
    public boolean isPrototype(String name) throws BeansException {
       // TODO Auto-generated method stub
       return false;
    }
 
    @SuppressWarnings("rawtypes")
    @Override
    public boolean isTypeMatch(String name, Class targetType)
          throws BeansException {
       // TODO Auto-generated method stub
       return false;
    }
 
    @Override
    public Class<?> getType(String name) throws BeansException {
       // TODO Auto-generated method stub
       return null;
    }
 
    @Override
    public String[] getAliases(String name) {
       // TODO Auto-generated method stub
       return null;
    }
 
    /**
     * @return the classesByType
     */
    public Map<Class<?>, Map<String,Object>> getClassesByType() {
       return _classesByType;
    }
 
    /**
     * @param classesByType the classesByType to set
     */
    public void setClassesByType(Map<Class<?>, Map<String,Object>> classesByType) {
       _classesByType = classesByType;
    }
 
    /**
     * @return the beanByName
     */
    public Map<String, Object> getBeansByName() {
       return _beansByName;
    }
 
    /**
     * @param beanByName the beanByName to set
     */
    public void setBeansByName(Map<String, Object> beanByName) {
       _beansByName = beanByName;
    }
 
 }
