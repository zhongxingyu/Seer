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
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.authentication.UserObjectPermissionListener;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.flow.Controller;
 
 /**
  * In the Object Orientation paradigm it's the instance of the class whereas the class is the SchemaClassReflection class.
  * Instance's values are customizable, but at construction time are valued with SchemaClassReflection values.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
 public class SchemaObject extends SchemaClassDefinition {
	private static final long	serialVersionUID	= -228110121477948747L;
	private SchemaClass				schemaClass;
	private Object						instance;
	private static Log				log								= LogFactory.getLog(SchemaObject.class);
 
 	public SchemaObject(SchemaClass iEntityInfo, Object iInstance) {
		instance = iInstance;
 		schemaClass = iEntityInfo;
 		copyDefinition(schemaClass);
 		invokeListeners();
 	}
 
 	public SchemaObject(SchemaClass iEntityInfo) {
 		this(iEntityInfo, null);
 	}
 
 	/**
 	 * Load the SchemaObject upon first field access (lazy) to avoid memory consumption.
 	 * 
 	 */
 	@Override
 	public SchemaField getField(String iFieldName) {
 
 		String fieldName;
 		int sepPos = iFieldName.indexOf(Utility.PACKAGE_SEPARATOR);
 		if (sepPos == -1)
 			fieldName = iFieldName;
 		else
 			fieldName = iFieldName.substring(0, sepPos);
 
 		SchemaField field = fields.get(fieldName);
 		/*
 		 * if (field != null && field.getType() != null && !(field.getType() instanceof SchemaObject)) { synchronized (field) { if
 		 * (field.getType() != null) // COMPLEX CLASS: REPLACE IT WITH A SCHEMA OBJECT INSTANCE field.setType(new
 		 * SchemaObject(field.getType().getSchemaClass())); } }
 		 */
 		if (sepPos > -1 && field != null) {
 			if (field.getType() == null)
 				return null;
 			field = field.getType().getField(iFieldName.substring(sepPos + 1));
 		}
 
 		return field;
 	}
 
 	@Override
 	public SchemaClass getSchemaClass() {
 		return schemaClass;
 	}
 
 	public Object getInstance() {
		return instance;
 	}
 
 	public void setInstance(Object instance) {
		this.instance = instance;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder buffer = new StringBuilder();
 		if (instance != null) {
 			buffer.append("Instance: ");
 			buffer.append(instance);
 			buffer.append(" ");
 		}
 		if (schemaClass != null) {
 			buffer.append("Schema: ");
 			buffer.append(schemaClass);
 		}
 		return buffer.toString();
 	}
 
 	@Override
 	public String getName() {
 
 		return schemaClass.getName();
 	}
 
 	/**
 	 * Re-define the base implementation enabling the invocation of listeners.
 	 * 
 	 * @param iSource
 	 */
 	public void copyDefinition(SchemaClassDefinition iSource) {
 		if (iSource == null)
 			return;
 
 		this.features = null;
 		this.parent = iSource;
 		try {
 			Roma.context().create();
 
 			List<UserObjectPermissionListener> listeners = Controller.getInstance().getListeners(UserObjectPermissionListener.class);
 
 			cloneFields(iSource, listeners);
 			cloneActions(iSource, listeners);
 			cloneEvents(iSource, listeners);
 		} catch (CloneNotSupportedException e) {
 			log.error("[SchemaObject.copyDefinition] Can't clone element", e);
 		} finally {
 			Roma.context().destroy();
 		}
 	}
 
 	private void invokeListeners() {
 		// INVOKE ALL LISTENERS JUST AFTER CREATION TO BE ENRICHED FROM THE EXTERNAL
 		List<SchemaObjectListener> listeners = Controller.getInstance().getListeners(SchemaObjectListener.class);
 		if (listeners != null)
 			for (SchemaObjectListener l : listeners)
 				l.onCreate(this);
 	}
 
 	@Override
 	public <T> boolean isRuntimeSet(Feature<T> feature) {
 		return super.hasFeature(feature);
 	}
 }
