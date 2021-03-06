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
 
 import groovy.lang.GroovyObject;
 import groovy.lang.MetaClass;
 import groovy.lang.GroovyClassLoader;
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 import org.codehaus.groovy.grails.orm.support.TransactionManagerPostProcessor;
 import org.codehaus.groovy.runtime.InvokerHelper;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.StaticApplicationContext;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.ResourcePatternResolver;
 import org.springframework.ui.context.Theme;
 import org.springframework.ui.context.ThemeSource;
 import org.springframework.ui.context.support.UiApplicationContextUtils;
 import org.springframework.web.context.ConfigurableWebApplicationContext;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.context.request.RequestScope;
 import org.springframework.web.context.request.SessionScope;
 import org.springframework.web.context.support.ServletContextAwareProcessor;
 import org.springframework.web.context.support.ServletContextResource;
 import org.springframework.web.context.support.ServletContextResourcePatternResolver;
 import org.springframework.webflow.config.scope.ScopeRegistrar;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 
 /**
  * A WebApplicationContext that extends StaticApplicationContext to allow for programmatic
  * configuration at runtime. The code is adapted from StaticWebApplicationContext.
  * 
  * @author Graeme
  * @since 0.3
  *
  */
 public class GrailsWebApplicationContext extends StaticApplicationContext
 		implements ConfigurableWebApplicationContext, GroovyObject, ThemeSource {
 
 	private ServletContext servletContext;
 	private String namespace;
 	private ThemeSource themeSource;
 	private ServletConfig servletConfig;
 	private MetaClass metaClass;
 	private BeanWrapper ctxBean = new BeanWrapperImpl(this);
    private GroovyClassLoader grailsClassLoader;
 
     public GrailsWebApplicationContext() throws BeansException {
 		super();
 		initialise();
 	}
 
 	public GrailsWebApplicationContext(ApplicationContext parent) throws BeansException {
 		super(parent);
         initialise();
 	}
 
 	private void initialise() {
 		this.metaClass = InvokerHelper.getMetaClass(this);
 		addBeanFactoryPostProcessor(new TransactionManagerPostProcessor());
 	}
 
     public ClassLoader getClassLoader() {
        return grailsClassLoader != null ? grailsClassLoader : super.getClassLoader();
     }
 
     /**
 	 * Set the ServletContext that this WebApplicationContext runs in.
 	 */
 	public void setServletContext(ServletContext servletContext) {
 		this.servletContext = servletContext;
 	}
 
 	public ServletContext getServletContext() {
 		return servletContext;
 	}
 
 	public void setNamespace(String namespace) {
 		this.namespace = namespace;
 		if (namespace != null) {
 			setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
 		}
 	}
 
 	public String getNamespace() {
 		return namespace;
 	}
 
 	public void setConfigLocations(String[] configLocations) {
 		throw new UnsupportedOperationException("StaticWebApplicationContext does not support configLocations");
 	}
 
     public String[] getConfigLocations() {
         throw new UnsupportedOperationException("StaticWebApplicationContext does not support configLocations");
     }
 
 
     /**
 	 * Register ServletContextAwareProcessor.
 	 * @see ServletContextAwareProcessor
 	 */
 	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
         beanFactory.registerScope(SCOPE_REQUEST, new RequestScope());
         beanFactory.registerScope(SCOPE_SESSION, new SessionScope(false));
         beanFactory.registerScope(SCOPE_GLOBAL_SESSION, new SessionScope(true));
         new ScopeRegistrar().postProcessBeanFactory(beanFactory);
 
         beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
 		beanFactory.ignoreDependencyInterface(ServletContextAware.class);
 	}
 
 	/**
 	 * This implementation supports file paths beneath the root of the ServletContext.
 	 * @see ServletContextResource
 	 */
 	protected Resource getResourceByPath(String path) {
 		return new ServletContextResource(this.servletContext, path);
 	}
 
 	/**
 	 * This implementation supports pattern matching in unexpanded WARs too.
 	 * @see ServletContextResourcePatternResolver
 	 */
 	protected ResourcePatternResolver getResourcePatternResolver() {
 		return new ServletContextResourcePatternResolver(this);
 	}
 
 	/**
 	 * Initialize the theme capability.
 	 */
 	protected void onRefresh() {
 		this.themeSource = UiApplicationContextUtils.initThemeSource(this);
 	}
 
     public void refresh() throws BeansException, IllegalStateException {
        ApplicationContext parent = getParent();
        if(parent != null && parent.containsBean(GrailsApplication.APPLICATION_ID)) {
            final GrailsApplication application = (GrailsApplication) parent.getBean(GrailsApplication.APPLICATION_ID);
            this.grailsClassLoader = application.getClassLoader();
        }


        
         super.refresh();
     }
 
     public Theme getTheme(String themeName) {
 		return this.themeSource.getTheme(themeName);
 	}
 
 	public void setServletConfig(ServletConfig servletConfig) {
 		this.servletConfig = servletConfig;
 	}
 
     public ServletConfig getServletConfig() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public MetaClass getMetaClass() {
 		return this.metaClass;
 	}
 
 	public Object getProperty(String property) {
 		if(containsBean(property)) {
 			return getBean(property);
 		}
 		else if(ctxBean.isReadableProperty(property)) {
 			return ctxBean.getPropertyValue(property);
 		}
 		return null;
 	}
 
 	public Object invokeMethod(String name, Object args) {
 		return metaClass.invokeMethod(this, name, args);
 	}
 
 	public void setMetaClass(MetaClass metaClass) {
 		this.metaClass = metaClass;
 	}
 
 	public void setProperty(String property, Object newValue) {
 		if(newValue instanceof BeanDefinition) {
             if(containsBean(property)) {
                 removeBeanDefinition(property);
             }
             
             registerBeanDefinition(property, (BeanDefinition)newValue);
 		}
 		else {
 			metaClass.setProperty(this, property, newValue);
 		}
 	}
 }
