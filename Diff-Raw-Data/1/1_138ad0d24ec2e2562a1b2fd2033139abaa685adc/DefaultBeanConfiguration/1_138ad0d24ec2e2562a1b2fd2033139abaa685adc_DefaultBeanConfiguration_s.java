 /*
  * Copyright 2004-2005 the original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */ 
 package org.codehaus.groovy.grails.commons.spring;
 
 import groovy.lang.GroovyObjectSupport;
 import groovy.lang.MissingPropertyException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.beans.factory.config.ConstructorArgumentValues;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.beans.factory.support.RootBeanDefinition;
 
 /**
  * Default implementation of the BeanConfiguration interface 
  * 
  * Credit must go to Solomon Duskis and the
  * article: http://jroller.com/page/Solomon?entry=programmatic_configuration_in_spring
  * 
  * @author Graeme
  * @since 0.3
  *
  */
 public class DefaultBeanConfiguration extends GroovyObjectSupport implements BeanConfiguration {
 
 	private static final String AUTOWIRE = "autowire";
 	private static final String CONSTRUCTOR_ARGS = "constructorArgs";
 	private static final String DESTROY_METHOD = "destroyMethod";
 	private static final String FACTORY_BEAN = "factoryBean";
 	private static final String FACTORY_METHOD = "factoryMethod";
 	private static final String INIT_METHOD = "initMethod";
 	private static final String BY_NAME = "byName";
 	private static final String BY_TYPE = "byType";
 	private static final String BY_CONSTRUCTOR = "constructor";
 	private static final List DYNAMIC_PROPS = new ArrayList(){ { 
 		add(AUTOWIRE);
 		add(CONSTRUCTOR_ARGS);
 		add(DESTROY_METHOD);
 		add(FACTORY_BEAN);
 		add(FACTORY_METHOD);
 		add(INIT_METHOD);
 		add(BY_NAME);
 		add(BY_TYPE);
 		add(BY_CONSTRUCTOR);
 	} };
 	
 	public Object getProperty(String property) {
 		AbstractBeanDefinition bd = getBeanDefinition();
 		if(wrapper.isReadableProperty(property)) {
 			return wrapper.getPropertyValue(property);
 		}
 		else if(DYNAMIC_PROPS.contains(property)) {
 			return null;
 		}
 		return super.getProperty(property);
 	}
 
 	public void setProperty(String property, Object newValue) {
 		AbstractBeanDefinition bd = getBeanDefinition();
 		if(wrapper.isWritableProperty(property)) {
 			wrapper.setPropertyValue(property, newValue);
 		}
 		// autowire		
 		else if(AUTOWIRE.equals(property)) {
 			if(BY_NAME.equals(newValue)) {
 				bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME);
 			}
 			else if(BY_TYPE.equals(newValue)) {
 				bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
 			}
 			else if(Boolean.TRUE.equals(newValue)) {
 				bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME);
 			}
 			else if(BY_CONSTRUCTOR.equals(newValue)) {
 				bd.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
 			}
 		}
 		// constructorArgs
 		else if(CONSTRUCTOR_ARGS.equals(property) && newValue instanceof List) {
 			ConstructorArgumentValues cav = new ConstructorArgumentValues();
 			List args = (List)newValue;
 			for (Iterator i = args.iterator(); i.hasNext();) {
 				Object e = i.next();
 				cav.addGenericArgumentValue(e);
 			}
 			bd.setConstructorArgumentValues(cav);
 		}
 		// destroyMethod
 		else if(DESTROY_METHOD.equals(property)) {
 			if(newValue != null)
 				bd.setDestroyMethodName(newValue.toString());
 		}
 		// factoryBean
 		else if(FACTORY_BEAN.equals(property)) {
 			if(newValue != null)
 				bd.setFactoryBeanName(newValue.toString());
 		}
 		// factoryMethod
 		else if(FACTORY_METHOD.equals(property)) {
 			if(newValue != null)
 				bd.setFactoryMethodName(newValue.toString());
 		}
 		// initMethod		
 		else if(INIT_METHOD.equals(property)) {
 			if(newValue != null)
 				bd.setInitMethodName(newValue.toString());
 		}
 		else {
 			throw new MissingPropertyException(property, getClass());
 		}
 	}
 
 	private Class clazz;
 	private String name;
 	private boolean singleton = true;
 	private AbstractBeanDefinition definition;
 	private Collection constructorArgs = Collections.EMPTY_LIST;
 	private BeanWrapper wrapper;
 
 	public DefaultBeanConfiguration(String name, Class clazz) {
 		this.name = name;
 		this.clazz = clazz;
 	}
 	
 	public DefaultBeanConfiguration(String name, Class clazz, boolean prototype) {
 		this.name = name;
 		this.clazz = clazz;
 		this.singleton = !prototype;
 	}	
 
 	public DefaultBeanConfiguration(String name) {
 		this.name= name;
 	}
 
 	public DefaultBeanConfiguration(Class clazz2) {
 		this.clazz = clazz2;
 	}
 
 	public DefaultBeanConfiguration(String name2, Class clazz2, Collection args) {
 		this.name = name2;
 		this.clazz = clazz2;
 		this.constructorArgs = args;
 	}
 
 	public DefaultBeanConfiguration(String name2, boolean prototype) {
 		this.name = name2;
 		this.singleton = !prototype;
 	}
 
 	public DefaultBeanConfiguration(Class clazz2, Collection constructorArguments) {
 		this.clazz = clazz2;
 		this.constructorArgs = constructorArguments;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public boolean isSingleton() {
 		return this.singleton ;
 	}
 
 	public AbstractBeanDefinition getBeanDefinition() {
 		if (definition == null)
 			definition = createBeanDefinition();
 		return definition;
 	}
 
 	protected AbstractBeanDefinition createBeanDefinition() {
 		AbstractBeanDefinition bd;
 		if(constructorArgs.size() > 0) {
 			ConstructorArgumentValues cav = new ConstructorArgumentValues();
 			for (Iterator i = constructorArgs.iterator(); i.hasNext();) {
 				cav.addGenericArgumentValue(i.next());
 			}
 			 bd = new RootBeanDefinition(clazz,cav,null);
 			bd.setSingleton(singleton);
			return bd;
 		}
 		else {
 			bd = new RootBeanDefinition(clazz,singleton);
 		}
 		wrapper = new BeanWrapperImpl(bd);
 		return bd;
 	}
 	
 	public BeanConfiguration addProperty(String propertyName, Object propertyValue) {
 		if(propertyValue instanceof BeanConfiguration) {
 			propertyValue = ((BeanConfiguration)propertyValue).getBeanDefinition();
 		}
 		getBeanDefinition()
 			.getPropertyValues()
 			.addPropertyValue(propertyName,propertyValue);
 				
 		return this;
 	}
 
 	public BeanConfiguration setDestroyMethod(String methodName) {
 		getBeanDefinition().setDestroyMethodName(methodName);
 		return this;
 	}
 
 	public BeanConfiguration setDependsOn(String[] dependsOn) {
 		getBeanDefinition().setDependsOn(dependsOn);
 		return this;		
 	}
 
 	public BeanConfiguration setFactoryBean(String beanName) {
 		getBeanDefinition().setFactoryBeanName(beanName);
 		
 		return this;
 	}
 
 	public BeanConfiguration setFactoryMethod(String methodName) {
 		getBeanDefinition().setFactoryMethodName(methodName);
 		return this;
 	}
 
 	public BeanConfiguration setAutowire(String type) {
 		if("byName".equals(type)) {
 			getBeanDefinition().setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
 		}
 		else if("byType".equals(type)){
 			getBeanDefinition().setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
 		}
 		return this;
 	}
 
     public void setName(String beanName) {
         this.name = beanName;
     }
 
 	public Object getPropertyValue(String name) {
 		return getBeanDefinition()
 					.getPropertyValues()
 					.getPropertyValue(name)
 					.getValue();
 	}
 
 	public boolean hasProperty(String name) {
 		return getBeanDefinition().getPropertyValues().contains(name);
 	}
 
 	public void setPropertyValue(String property, Object newValue) {
 		getBeanDefinition().getPropertyValues().addPropertyValue(property, newValue);
 	}
 
 }
