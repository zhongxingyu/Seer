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
 
 package org.romaframework.core.schema.reflection;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.romaframework.core.Roma;
 import org.romaframework.core.aspect.Aspect;
 import org.romaframework.core.schema.FeatureLoader;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaParameter;
 import org.romaframework.core.schema.config.SchemaConfiguration;
 import org.romaframework.core.schema.virtual.VirtualObject;
 import org.romaframework.core.schema.xmlannotations.XmlActionAnnotation;
 import org.romaframework.core.schema.xmlannotations.XmlParameterAnnotation;
 
 /**
  * Represent an event of a class.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
 public class SchemaEventReflection extends SchemaEvent {
 
 	private static final long			serialVersionUID				= 7861837910403510791L;
 
 	protected Method							method;
 
 	final static public Object[]	NO_PARAMETERS						= new Object[] {};
 
 	public static final String		COLLECTION_VIEW_EVENT		= "View";
 	public static final String		COLLECTION_ADD_EVENT		= "Add";
 	public static final String		COLLECTION_EDIT_EVENT		= "Edit";
 	public static final String		COLLECTION_REMOVE_EVENT	= "Remove";
 
 	public SchemaEventReflection(SchemaField field, String iName, List<SchemaParameter> iOrderedParameters) {
		super(field, iName, null);
 	}
 
 	public SchemaEventReflection(SchemaClassDefinition iEntity, String iName, List<SchemaParameter> iOrderedParameters) {
		super(iEntity, iName, null);
 	}
 
 	@Override
 	public Object invokeFinal(Object iContent, Object[] params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 		while (iContent instanceof VirtualObject)
 			iContent = ((VirtualObject) iContent).getSuperClassObject();
 
 		return method.invoke(iContent, params);
 	}
 
 	@Override
 	public String toString() {
 		return name + " (event:" + method + ")";
 	}
 
 	public void setMethod(Method method) {
 		this.method = method;
 	}
 
 	public Method getMethod() {
 		return method;
 	}
 
 	public void configure() {
 		SchemaConfiguration classDescriptor = entity.getSchemaClass().getDescriptor();
 
 		XmlActionAnnotation parentDescriptor = null;
 		if (classDescriptor != null && classDescriptor.getType() != null) {
 			parentDescriptor = classDescriptor.getType().getEvent(name);
 		}
 		FeatureLoader.loadEventFeatures(this, parentDescriptor);
 
 		// BROWSE ALL ASPECTS
 		for (Aspect aspect : Roma.aspects()) {
 			// CONFIGURE THE SCHEMA OBJECT WITH CURRENT ASPECT
 			aspect.configEvent(this);
 		}
 		int i = 0;
 		List<XmlParameterAnnotation> paramAnn = null;
 		if (parentDescriptor != null)
 			paramAnn = parentDescriptor.getParameters();
 		for (SchemaParameter parameter : getParameters().values()) {
 			XmlParameterAnnotation xmlParam = null;
 			if (paramAnn != null)
 				xmlParam = paramAnn.get(i++);
 			parameter.configure(method, xmlParam);
 		}
 
 	}
 
 }
