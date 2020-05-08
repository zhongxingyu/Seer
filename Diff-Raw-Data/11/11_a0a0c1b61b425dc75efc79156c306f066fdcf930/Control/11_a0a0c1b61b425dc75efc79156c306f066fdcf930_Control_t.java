 /*
  * Copyright 2005 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * de.jwic.base.Control
  * $Id: Control.java,v 1.12 2010/02/07 14:24:24 lordsam Exp $
  */
 package de.jwic.base;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONWriter;
 
 /**
  * <p>Superclass for jWic controls placed into an jWic container. A Control represents
  * a part of an Application. All controls live in the SessionContext object, wich 
  * represents the application environment. A Control is connected to a 
  * {@link Container} object. 
  * 
  * <p>Control objects live persistent until the application is closed or the control
  * is removed from it's container.
  * 
  * <p>A control is created like a normal java object, using its constructor:
  * <code>ButtonControl button = new ButtonControl(container);</code> 
  
  * @author Florian Lippisch
  * @version $Revision: 1.12 $
  */
 public abstract class Control implements Serializable, IControl {
 
 	private static final long serialVersionUID = 1L;
 	
 	protected final static String DEFAULT_RENDERER = "jwic.renderer.Default";
 	protected final static String DEFAULT_OUTER_RENDERER = "jwic.renderer.OuterContainer";
 	
 	private final static String ACTION_PREFIX = "action";
 	
 	/** Logger available to subclasses */
 	protected transient Log log = LogFactory.getLog(getClass());
 	
 	protected boolean bolRequireRedraw = false;
 	protected boolean bolVisible = true;
 
 	private String name = null;
 	private IControlContainer objContainer = null;
 	private SessionContext objSessionContext = null;
 	private String strControlID = null;
 	private String rendererId = DEFAULT_RENDERER;
 	
 	protected Map<String, Field> fields = null;
 	protected String templateName = null;
 	
 
 	/**
 	 * Constructs a new control instance and adds it to the specified
 	 * container with the specified name. If the name is <code>null</code>,
 	 * a unique name will be choosen by the container.
 	 */
 	public Control(IControlContainer container, String name) {
 		container.registerControl(this, name);
 	}
 	
 	/**
 	 * Get new logger after deserialization.
 	 * @param s
 	 * @throws IOException
 	 */
 	private void readObject(ObjectInputStream s) throws IOException  {
 		try {
 			s.defaultReadObject();
 		} catch (ClassNotFoundException e) {
 			throw new IOException("ClassNotFound in readObject");
 		}
 		log = LogFactory.getLog(getClass());
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#actionPerformed(java.lang.String, java.lang.String)
 	 */
 	public void actionPerformed(String actionId, String parameter) {
 		
 		String methodName;
 		if (actionId.length() == 0) {
 			return;
 		} else if (actionId.length() == 1){
 			methodName = ACTION_PREFIX + actionId.toUpperCase();
 		} else {
 			methodName = ACTION_PREFIX + actionId.substring(0, 1).toUpperCase() + actionId.substring(1);
 		}
 		
 		// try to find a method of the implementing control class that
 		// equals the actionId
 		
 		
 		Method actionMethod = null;
 		Object[] arguments = new Object[] { parameter };
 		try {
 			actionMethod = getClass().getMethod(methodName, new Class[] { String.class });
 		} catch (NoSuchMethodException e) {
 			// if the parameter is empty, try to find a method without arguments
 			if (parameter.length() == 0) {
 				arguments = null;
 				try {
 					actionMethod = getClass().getMethod(methodName, (Class[])null);
 				} catch (NoSuchMethodException e1) {
 					// no method exists -> will be handled later
 				}
 			}
 		}
 		if (actionMethod == null) {
 			// FLI: I do now throw an exception if the desired method was not found. It should
 			// not break existing applications because this method was abstract in earlier versions 
 			// so it can not be called accidently.
 			log.error("A method to handle the action '" + actionId + "' does not exist in this control. (" + methodName + ")");
 			throw new IllegalArgumentException("A method to handle the action '" + actionId + "' does not exist in this control. (" + methodName + ")");
 		} else {
 			try {
 				actionMethod.invoke(this, arguments);
 			} catch (IllegalArgumentException e) {
 				log.error("Error invoking method " + actionMethod.getName(), e);
 			} catch (IllegalAccessException e) {
 				log.error("Error invoking method " + actionMethod.getName(), e);
 			} catch (InvocationTargetException e) {
 				Throwable t = e.getCause();
 				if (t instanceof RuntimeException) {
 					// forward the exception
 					throw (RuntimeException)t;
 				} else if (t != null) {
 					String msg = "Error during invocation of action method " + actionMethod.getName();
 					log.error(msg, t);
 					throw new RuntimeException(msg, t);
 				}
 				String msg = "Unexpected InvocationTargetException";
 				log.error(msg, e);
 				throw new RuntimeException(msg);
 			}
 		}
 		
 	}
 	
 	/**
 	 * Adds a field to the fieldlist.
 	 * @param name
 	 * @param field
 	 */
 	void addField(Field field) {
 		
 		if (fields == null) {
 			fields = new HashMap<String, Field>();
 		}
 		
 		if (field.getName() == null) {
 			// generate a name.
 			int idx = 0;
 			String fName = Integer.toString(idx);
 			while (fields.containsKey(fName)) {
 				fName = Integer.toString(++idx);
 			}
 			field.setName(fName);
 		}
 		if (fields.containsKey(field.getName())) {
 			throw new IllegalArgumentException("A field with the name '" + field.getName() + "' already exists in control '" + getControlID() + "'.");
 		}
 		fields.put(field.getName(), field);
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#destroy()
 	 */
 	public void destroy() {
 		if (objContainer != null && objContainer.getControl(name) != null) {
 			// remove this control from container
 			objContainer.removeControl(name);
 		} else {
 			// remove from top control
 			if (objSessionContext != null) {
 				objSessionContext.removeTopControl(this);
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#removeField(java.lang.String)
 	 */
 	public void removeField(String fieldname) {
 		if (fields != null) {
 			fields.remove(fieldname);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#removeField(de.jwic.base.Field)
 	 */
 	public void removeField(Field field) {
 		if (fields != null) {
 			fields.remove(field.getName());
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#getField(java.lang.String)
 	 */
 	public Field getField(String fieldname) {
 		if (fields != null) {
 			return fields.get(fieldname);
 		}
 		return null;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#createActionURL(java.lang.String, java.lang.String)
 	 */
 	public String createActionURL(String action, String acpara) {
 		StringBuffer sb = new StringBuffer();
 		sb.append("javascript:JWic.fireAction('");
 		sb.append(getControlID());
 		sb.append("', '");
 		sb.append(action);
 		sb.append("', '");
 		sb.append(acpara);
 		sb.append("');");
 			
 		return sb.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#getContainer()
 	 */
 	public IControlContainer getContainer() {
 		return objContainer;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#getControlID()
 	 */
 	public java.lang.String getControlID() {
 		return strControlID;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#getSessionContext()
 	 */
 	public SessionContext getSessionContext() {
 		return objSessionContext;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#isRequireRedraw()
 	 */
 	public boolean isRequireRedraw() {
 		return bolRequireRedraw;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#isVisible()
 	 */
 	public boolean isVisible() {
 		return bolVisible;
 	}
 	/**
 	 * This method is used to give the control a connection to the Container object
 	 * when it's added to a container.
 	 */
 	void setContainer(IControlContainer newContainer) {
 		objContainer = newContainer;
 	}
 	/**
 	 * Set the ID of the control. Invoked when the control is added to a container.
 	 * @param newControlID String
 	 */
 	void setControlID(String newControlID) {
 		strControlID = newControlID;
 	}
 	
 	/**
 	 * Flags the control that it must be rendered again to reflect its
 	 * current state. Control implementors should use this method to
 	 * let the framework render the control again when a page update 
 	 * occures.
 	 */
 	public void requireRedraw() {
 		setRequireRedraw(true);
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#setRequireRedraw(boolean)
 	 */
 	public void setRequireRedraw(boolean requireRedraw) {
 		bolRequireRedraw = requireRedraw;
 	}
 	/**
 	 * This method is used to give the control a connection to the SessionContext object
 	 * when it's added to a container.
 	 */
 	void setSessionContext(SessionContext newSessionContext) {
 		objSessionContext = newSessionContext;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#setVisible(boolean)
 	 */
 	public void setVisible(boolean newVisible) {
 		if (bolVisible != newVisible) {
 			// JBO 2005-09-06: for nicer ajax support container is not updated anymore
 			setRequireRedraw(true);
 		}
 		bolVisible = newVisible;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#getName()
 	 */
 	public final String getName() {
 		return name;
 	}
 	/**
 	 * Sets the name of the Contorl.
 	 * @param name The name to set.
 	 */
 	void setName(String name) {
 		if (this.name != null && !this.name.equals(name)) {
 			throw new IllegalStateException("The name of a control can not be changed.");
 		}
 		this.name = name;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#getRendererId()
 	 */
 	public String getRendererId() {
 		return rendererId;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#setRendererId(java.lang.String)
 	 */
 	public void setRendererId(String rendererId) {
 		this.rendererId = rendererId;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#getTemplateName()
 	 */
 	public String getTemplateName() {
 		if (templateName == null) {
 			Class<?> clazz = getClass();
 			for (String classname = clazz.getName(); classname.indexOf("$") != -1;) {
 				clazz = clazz.getSuperclass();
 				classname = clazz.getName();
 			}
 			return clazz.getName();
 		}
 		return templateName;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#setTemplateName(java.lang.String)
 	 */
 	public void setTemplateName(String templateName) {
 		this.templateName = templateName;
 	}
 	
 	/**
 	 * Builds Json object string of all methods marked with 
 	 * IncludeJsOption Annotation.
 	 * If Enums are used getCode needs to be implemented, which returns the value
 	 * @return
 	 */
 	public String buildJsonOptions(){
 		try{
 			StringWriter sw = new StringWriter();
 			JSONWriter writer = new JSONWriter(sw);
 			writer.object();
 			BeanInfo beanInfo;
 			beanInfo = Introspector.getBeanInfo(getClass());
 		
 			PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
 			
 			for (PropertyDescriptor pd : pds) {
				Method getter = pd.getReadMethod();
				if(getter != null && pd.getReadMethod().getAnnotation(IncludeJsOption.class) != null){
					Object o = getter.invoke(this);
 					if(o != null){
 						if(o instanceof Enum){
 							Method m = o.getClass().getMethod("getCode", null);
 							if(m != null){
 								writer.key(pd.getDisplayName()).value(m.invoke(o));
 								continue;
 							}
 						}else if(o instanceof Date){
 							writer.key(pd.getDisplayName()).value(new JsDateString((Date)o, getSessionContext().getTimeZone()));
 							continue;
 						}
 						writer.key(pd.getDisplayName()).value(o);						
 					}
 				}
 			}
 			writer.endObject();
 			return sw.toString();
 		}catch (Exception e) {
			throw new RuntimeException("Error while configuring Json Option for " + this.getClass().getName());
 		}
 	}
 }
