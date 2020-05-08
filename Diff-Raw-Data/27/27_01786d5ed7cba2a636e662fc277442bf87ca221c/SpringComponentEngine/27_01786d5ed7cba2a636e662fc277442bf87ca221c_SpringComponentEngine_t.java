 package org.romaframework.core.component;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.config.AbstractServiceable;
 import org.romaframework.core.config.ContextException;
 import org.romaframework.core.exception.ConfigurationNotFoundException;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
 import org.springframework.beans.BeansException;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 @SuppressWarnings("unchecked")
 public class SpringComponentEngine extends AbstractServiceable implements ComponentAspect {
 	private Log																log																= LogFactory.getLog(SpringComponentEngine.class);
 	private static final String								COMPONENT_SRV_FILE_PATTERN				= "META-INF/components/applicationContext*.xml";
 	private static final String								COMPONENT_SRV_FILE_PATTERN_SUBDIR	= "META-INF/components/*/applicationContext*.xml";
 
 	protected ClassPathXmlApplicationContext	springContext;
 	protected Map<String, Object>							components												= new HashMap<String, Object>();
 	protected String													basePath;
 	protected String[]												additionalPaths;
 
 	public boolean existComponent(Class<? extends Object> iComponentClass) {
 		return existComponent(Utility.getClassName(iComponentClass));
 	}
 
 	public boolean existComponent(String iComponentName) {
 		boolean exist = springContext.containsBean(iComponentName);
 		if (!exist) {
 			synchronized (components) {
 				exist = components.containsKey(iComponentName);
 			}
 		}
 		return exist;
 	}
 
 	public <T> T getComponent(Class<T> iComponentClass) throws ContextException {
 		try {
 			String iComponentName = Utility.getClassName(iComponentClass);
 			T val = (T) springContext.getBean(iComponentName, iComponentClass);
 			if (val == null) {
 				Object comp;
 				synchronized (components) {
 					comp = components.get(iComponentName);
 				}
 				if (comp != null && iComponentClass.isAssignableFrom(comp.getClass()))
 					val = (T) comp;
 			}
 			return val;
 		} catch (BeansException e) {
 			throw new ContextException("Error on retrieving component '" + Utility.getClassName(iComponentClass) + "'", e);
 		}
 	}
 
 	public <T> T getComponent(String iComponentName) throws ContextException {
 		try {
 			T val = (T) springContext.getBean(iComponentName);
 			if (val == null) {
 				synchronized (components) {
 					val = (T) components.get(iComponentName);
 				}
 			}
 			return val;
 		} catch (BeansException e) {
 			throw new ContextException("Error on retrieving component '" + iComponentName + "'", e);
 		}
 	}
 
 	public <T> Map<String, T> getComponentsOfClass(Class<T> iComponentClass) throws ContextException {
 		try {
 			Map<String, T> classes = springContext.getBeansOfType(iComponentClass);
 			synchronized (components) {
 				for (Map.Entry<String, Object> entry : components.entrySet()) {
 					if (entry.getValue() != null && iComponentClass.isAssignableFrom(entry.getValue().getClass())) {
 						classes.put(entry.getKey(), (T) entry.getValue());
 					}
 				}
 			}
 			return classes;
 		} catch (BeansException e) {
 			throw new ContextException("Error on retrieving components of class '" + iComponentClass + "'", e);
 		}
 	}
 
 	public <T> T autoComponent(Class<?> iClass) {
 		try {
 			String iComponentName = Utility.getClassName(iClass);
 			T val = null;
 			if (springContext.containsBean(iComponentName))
 				val = (T) springContext.getBean(iComponentName);
 			if (val == null) {
 				Object comp;
 				synchronized (components) {
 					comp = components.get(iComponentName);
 					if (comp == null && !iClass.isInterface()) {
 						try {
 							comp = iClass.newInstance();
 							components.put(iComponentName, comp);
 						} catch (Exception e) {
 							log.warn("Error create new instance of component:'" + iComponentName + "'", e);
 						}
 					}
 				}
 				if (comp != null && iClass.isAssignableFrom(comp.getClass()))
 					val = (T) comp;
 			}
 			return val;
 		} catch (BeansException e) {
 			throw new ContextException("Error on retrieving component '" + Utility.getClassName(iClass) + "'", e);
 		}
 	}
 
 	public <T> T autoComponent(String iName) {
 		try {
 			T val = null;
 			if (springContext.containsBean(iName))
 				val = (T) springContext.getBean(iName);
 			if (val == null) {
 				synchronized (components) {
 					val = (T) components.get(iName);
 					if (val == null) {
 						try {
 							SchemaClass clazz = null;
 							try {
 								clazz = Roma.schema().getSchemaClass(iName);
 							} catch (ConfigurationNotFoundException e) {
 								log.info("Not Found a class with name:'" + iName + "'");
 								if (log.isDebugEnabled())
 									log.debug("Not Found a class with name:'" + iName + "'", e);
 							}
 							if (clazz != null && !clazz.isInterface()) {
 								val = (T) clazz.newInstance();
 								components.put(iName, val);
 							}
 						} catch (Exception e) {
 							log.warn("Error create new instance of component:'" + iName + "'", e);
 						}
 					}
 				}
 			}
 			return val;
 		} catch (BeansException e) {
 			throw new ContextException("Error on retrieving component '" + iName + "'", e);
 		}
 	}
 
 	/**
 	 * Load all Spring configuration files and start the context.
 	 */
 	public void startup() throws RuntimeException {
 		status = STATUS_STARTING;
 		int size = 2;
 		if (additionalPaths != null)
 			size += additionalPaths.length;
 		String paths[] = new String[size];
 		if (basePath != null) {
 			paths[0] = basePath + COMPONENT_SRV_FILE_PATTERN;
 			paths[1] = basePath + COMPONENT_SRV_FILE_PATTERN_SUBDIR;
 		} else {
 			paths[0] = COMPONENT_SRV_FILE_PATTERN;
 			paths[1] = COMPONENT_SRV_FILE_PATTERN_SUBDIR;
 		}
 		if (additionalPaths != null)
 			System.arraycopy(additionalPaths, 0, paths, 2, additionalPaths.length);
 
 		springContext = new ClassPathXmlApplicationContext(paths, false);
 
 		springContext.refresh();
 		status = STATUS_UP;
 	}
 
 	public void shutdown() throws RuntimeException {
		status = STATUS_SHUTTING_DOWN;
 
 		if (springContext != null) {
 			springContext.destroy();
 		}
 
 		status = STATUS_DOWN;
 	}
 
 	public String aspectName() {
 		return ASPECT_NAME;
 	}
 
 	public void beginConfigClass(SchemaClassDefinition iClass) {
 	}
 
 	public void endConfigClass(SchemaClassDefinition iClass) {
 	}
 
 	public void configAction(SchemaAction action) {
 	}
 
 	public void configClass(SchemaClassDefinition class1) {
 	}
 
 	public void configEvent(SchemaEvent event) {
 	}
 
 	public void configField(SchemaField field) {
 	}
 
 	public Object getUnderlyingComponent() {
 		return springContext;
 	}
 
 	public String getBasePath() {
 		return basePath;
 	}
 
 	public void setBasePath(String basePath) {
 		this.basePath = basePath;
 	}
 
 	public void setAdditionalPaths(String[] additionalApplicationContext) {
 		this.additionalPaths = additionalApplicationContext;
 	}
 }
