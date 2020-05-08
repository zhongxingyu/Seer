 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
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
  */
 
 package org.romaframework.core.schema;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.core.Roma;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.flow.SchemaActionListener;
 import org.romaframework.core.schema.xmlannotations.XmlActionAnnotation;
 
 /**
  * Generic abstract class that represents an Action.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
 public abstract class SchemaAction extends SchemaClassElement {
 	private static final long							serialVersionUID	= -4789886810661429988L;
 
 	private static Log										log								= LogFactory.getLog(SchemaAction.class);
 
 	private Map<String, SchemaParameter>	parameters				= new HashMap<String, SchemaParameter>();
 
 	private List<SchemaParameter>					orderedParameters	= new ArrayList<SchemaParameter>();
 
 	private SchemaClass										returnType;
 
 	protected XmlActionAnnotation					descriptorInfo;
 
 	public SchemaAction(SchemaClassDefinition iEntity, String iName) {
 		super(iEntity, iName, FeatureType.ACTION);
 	}
 
 	public SchemaAction(SchemaClassDefinition iEntity, String iName, FeatureType type) {
 		super(iEntity, iName, type);
 	}
 
 	public SchemaAction(SchemaClassDefinition iEntity, String iName, List<SchemaParameter> iOrderedParameters) {
 		this(iEntity, iName, iOrderedParameters, FeatureType.ACTION);
 	}
 
 	public SchemaAction(SchemaClassDefinition iEntity, String iName, List<SchemaParameter> iOrderedParameters, FeatureType type) {
 		super(iEntity, iName, type);
 		orderedParameters = iOrderedParameters;
 		if (orderedParameters != null)
 			for (SchemaParameter param : orderedParameters)
 				parameters.put(param.getName(), param);
 	}
 
 	public abstract Object invokeFinal(Object iContent, Object[] params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
 
 	public Object invoke(Object iContent, Object... params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 
 		try {
 			// CREATE THE CONTEXT BEFORE TO CALL THE ACTION
 			Roma.context().create();
 
 			List<SchemaActionListener> listeners = Controller.getInstance().getListeners(SchemaActionListener.class);
 			boolean result = true;
 			for (SchemaActionListener listener : listeners) {
 				result = listener.onBeforeAction(iContent, this);
 				if (!result) {
 					log.debug("[SchemaAction.invoke] Listener " + listener + " has interrupted the chain of execution before the execution of the action");
 					return null;
 				}
 			}
 
 			Object value = null;
 			try {
 
 				if (params != null) {
 					Iterator<SchemaParameter> param = getParameterIterator();
 					Object newValues[] = new Object[params.length];
 					int i = 0;
 					while (param.hasNext()) {
 						SchemaParameter par = param.next();
 						newValues[i] = convertValue(params[i], par.getType());
 						i++;
 					}
 					value = invokeFinal(iContent, newValues);
 				} else {
 					value = invokeFinal(iContent, params);
 				}
 				for (SchemaActionListener listener : listeners) {
 					try {
 						listener.onAfterAction(iContent, this, value);
 					} catch (Throwable t) {
 						log.error("[SchemaAction.invoke] Listener " + listener + " has interrupted the chain of execution after the execution of the action", t);
 					}
 				}
 				return value;
 			} catch (IllegalArgumentException e) {
 				fireActionException(listeners, iContent, e);
 				throw e;
 			} catch (IllegalAccessException e) {
 				fireActionException(listeners, iContent, e);
 				throw e;
 			} catch (InvocationTargetException e) {
 				fireActionException(listeners, iContent, e);
 				throw e;
 			}
 		} finally {
 			// ASSURE TO DESTROY THE CONTEXT
 			Roma.context().destroy();
 		}
 	}
 
 	private void fireActionException(List<SchemaActionListener> listeners, Object iContent, Exception ex) {
 		for (SchemaActionListener listener : listeners) {
 			try {
 				listener.onExceptionAction(iContent, this, ex);
 			} catch (Throwable t) {
 				log.error("[SchemaAction.invoke] Listener " + listener + " has interrupted the chain of exception action  after the action throw and execution ", t);
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
		return super.toString() + getFullName();
 	}
 
 	public Iterator<SchemaParameter> getParameterIterator() {
 		return orderedParameters.iterator();
 	}
 
 	public SchemaParameter getParameter(String iName) {
 		return parameters.get(iName);
 	}
 
 	public int getParameterNumber() {
 		return parameters.size();
 	}
 
 	public String getSignature() {
 		return name;
 	}
 
 	public SchemaClass getReturnType() {
 		return returnType;
 	}
 
 	public void setReturnType(SchemaClass returnType) {
 		this.returnType = returnType;
 	}
 
 	public XmlActionAnnotation getDescriptorInfo() {
 		return descriptorInfo;
 	}
 
 	public static String getSignature(String name, Class<?>[] paramClass) {
 		StringBuilder methodSignature = new StringBuilder();
 		methodSignature.append(name);
 		String[] paramNames = new String[paramClass.length];
 		for (int i = 0; i < paramClass.length; ++i) {
 			paramNames[i] = paramClass[i].getSimpleName();
 		}
 		if (paramNames != null && paramNames.length != 0) {
 			methodSignature.append("(");
 			for (int i = 0; i < paramNames.length; i++) {
 				methodSignature.append(paramNames[i]);
 				if (i < paramNames.length - 1)
 					methodSignature.append(",");
 			}
 			methodSignature.append(")");
 		}
 		return methodSignature.toString();
 	}
 
 	public Map<String, SchemaParameter> getParameters() {
 		return parameters;
 	}
 }
