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
import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.flow.SchemaEventListener;
 
 /**
  * Represent an event of a class.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
 public abstract class SchemaEvent extends SchemaAction {
 
 	private static Log							log													= LogFactory.getLog(SchemaEvent.class);
 
 	private static final long				serialVersionUID						= 1652569176934380370L;
 
 	protected SchemaField						field;
 
 	protected String								eventSignature;
 
 	protected SchemaClassDefinition	eventOwner;
 
 	public static final String			ON_METHOD										= "on";
 
 	public static final String			COLLECTION_VIEW_EVENT				= "view";
 
 	public static final String			COLLECTION_ADD_EVENT				= "add";
 
 	public static final String			COLLECTION_ADD_INLINE_EVENT	= "addInline";
 
 	public static final String			COLLECTION_EDIT_EVENT				= "edit";
 
 	public static final String			COLLECTION_REMOVE_EVENT			= "remove";
 
 	public static final String			DEFAULT_EVENT_NAME					= ".DEFAULT_EVENT";
 
 	public SchemaEvent(SchemaField field, String iName, List<SchemaParameter> iOrderedParameters) {
 		super(field.getEntity(), iName, iOrderedParameters, FeatureType.EVENT);
 		this.field = field;
 		eventOwner = field.getEntity();
 		// TODO:Manage Params in signature
 		eventSignature = ON_METHOD + Utility.getCapitalizedString(field.getName()) + Utility.getCapitalizedString(iName);
 	}
 
 	public SchemaEvent(SchemaClassDefinition iEntity, String iName, List<SchemaParameter> iOrderedParameters) {
 		super(iEntity, iName, iOrderedParameters, FeatureType.EVENT);
 		eventOwner = iEntity;
 		// TODO:Manage Params in signature
 		eventSignature = ON_METHOD + Utility.getCapitalizedString(iName);
 	}
 
 	public String getEventSignature() {
 		return eventSignature;
 	}
 
 	public SchemaClassDefinition getEventOwner() {
 		if (eventOwner == null) {
 			return getEntity();
 		}
 		return eventOwner;
 	}
 
 	@Override
 	public String getFullName() {
 		if (fullName == null) {
 			if (field != null)
 				fullName = field.getName() + "." + getName();
 			fullName = getName();
 		}
 		return fullName;
 	}
 
 	@Override
 	public Object invoke(Object iContent, Object... params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 		try {
 			// CREATE THE CONTEXT BEFORE TO CALL THE ACTION
 			Roma.context().create();
 
 			List<SchemaEventListener> listeners = Controller.getInstance().getListeners(SchemaEventListener.class);
 			boolean result = true;
 			for (SchemaEventListener listener : listeners) {
 				result = listener.onBeforeEvent(iContent, this);
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
 				for (SchemaEventListener listener : listeners) {
 					try {
 						listener.onAfterEvent(iContent, this, value);
 					} catch (Throwable t) {
 						log.error("[SchemaAction.invoke] Listener " + listener + " has interrupted the chain of execution after the execution of the action", t);
 					}
 				}
 				return value;
 			} catch (IllegalArgumentException e) {
 				fireEventException(listeners, iContent, e);
 				throw e;
 			} catch (IllegalAccessException e) {
 				fireEventException(listeners, iContent, e);
 				throw e;
 			} catch (InvocationTargetException e) {
 				fireEventException(listeners, iContent, e);
 				throw e;
 			}
 		} finally {
 			// ASSURE TO DESTROY THE CONTEXT
 			Roma.context().destroy();
 		}
 	}
 
 	public void setEventOwner(SchemaClassDefinition eventOwner) {
 		this.eventOwner = eventOwner;
 	}
 
 	public void setFieldOwner(SchemaField field) {
 		this.field = field;
 	}
 
 	private void fireEventException(List<SchemaEventListener> listeners, Object iContent, Exception ex) {
 		for (SchemaEventListener listener : listeners) {
 			try {
 				listener.onExceptionEvent(iContent, this, ex);
 			} catch (Throwable t) {
 				log.error("[SchemaAction.invoke] Listener " + listener + " has interrupted the chain of exception action  after the action throw and execution ", t);
 			}
 		}
 	}
 }
