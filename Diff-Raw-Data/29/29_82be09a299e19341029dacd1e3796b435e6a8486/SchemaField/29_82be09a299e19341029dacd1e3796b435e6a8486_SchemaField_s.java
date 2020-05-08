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
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.core.feature.CoreFieldFeatures;
 import org.romaframework.core.Roma;
 import org.romaframework.core.binding.BindingException;
 import org.romaframework.core.domain.entity.ComposedEntity;
 import org.romaframework.core.exception.FieldErrorUserException;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.flow.SchemaFieldListener;
 import org.romaframework.core.schema.config.SchemaConfiguration;
 import org.romaframework.core.schema.virtual.VirtualObject;
 import org.romaframework.core.schema.xmlannotations.XmlFieldAnnotation;
 
 /**
  * Generic abstract class that represents a Field.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  */
 public abstract class SchemaField extends SchemaClassElement {
 	private static final long						serialVersionUID	= -4789886810661429988L;
 
 	protected Map<String, SchemaEvent>	events;
 	protected SchemaClassDefinition			type;
 	protected SchemaClass								embeddedType;
 	protected SchemaClass[]							embeddedTypeGenerics;
 	private static Log									log								= LogFactory.getLog(SchemaField.class);
 
 	public abstract Object getValue(Object iObject) throws BindingException;
 
 	public abstract boolean isArray();
 
 	public abstract Object getLanguageType();
 
 	protected abstract SchemaClass getSchemaClassFromLanguageType();
 
 	protected abstract void setValueFinal(Object iObject, Object iValue) throws IllegalAccessException, InvocationTargetException;
 
 	public SchemaField(SchemaClassDefinition iEntity, String iName) {
 		super(iEntity, iName, FeatureType.FIELD);
 		events = new HashMap<String, SchemaEvent>();
 	}
 
 	public void setValue(Object iObject, Object iFieldValue) {
 		Roma.context().create();
 		try {
 			Object value = convertValue(iFieldValue);
 			List<SchemaFieldListener> listeners = Controller.getInstance().getListeners(SchemaFieldListener.class);
 			try {
 
 				// CALL ALL LISTENERS BEFORE FIELD WRITE CALLBACKS
 				synchronized (listeners) {
 					for (SchemaFieldListener listener : listeners) {
 						Object curVal = listener.onBeforeFieldWrite(iObject, this, value);
 						if (curVal != SchemaFieldListener.IGNORED)
 							value = curVal;
 					}
 				}
 
 				setValueFinal(iObject, value);
 			} finally {
 				// ASSURE TO CALL THE AFTER FIELD WRITE CALLBACKS
 				synchronized (listeners) {
 					for (SchemaFieldListener listener : listeners) {
 						value = listener.onAfterFieldWrite(iObject, this, value);
 					}
 				}
 			}
 		} catch (Exception e) {
 			if (e.getCause() instanceof FieldErrorUserException) {
 				throw (FieldErrorUserException) e.getCause();
 			} else {
				log.error("[SchemaHelper.setFieldValue] Error on setting value '" + iFieldValue + "' for field '" + name + "' on object " + iObject, e);
 				throw new BindingException(iObject, name, e);
 			}
 		} finally {
 
 			Roma.context().destroy();
 		}
 	}
 
 	/**
 	 * Return field's class information
 	 * 
 	 * @return SchemaClassReflection if found, otherwise null
 	 */
 	public SchemaClass getClassInfo() {
 		SchemaClassDefinition def = getType();
 		if (def != null)
 			return def.getSchemaClass();
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return super.toString() + name;
 	}
 
 	public SchemaClassDefinition getType() {
 		if (type == null)
 			type = getSchemaClassFromLanguageType();
 		if (entity instanceof SchemaObject && !(type instanceof SchemaObject)) {
 			Object value = getValue(((SchemaObject) entity).getInstance());
 			if (value != null)
 				setType(Roma.session().getSchemaObject(value));
 			else
 				setType(Roma.session().getSchemaObject(type));
 		}
 		return type;
 	}
 
 	public void setType(SchemaClassDefinition iType) {
 		this.type = iType;
 	}
 
 	public XmlFieldAnnotation getDescriptorInfo() {
 		SchemaConfiguration classDescriptor = entity.getSchemaClass().getDescriptor();
 
 		if (classDescriptor == null || classDescriptor.getType() == null)
 			return null;
 		return classDescriptor.getType().getField(name);
 	}
 
 	public SchemaEvent getEvent(String iEventName) {
 		return events.get(iEventName);
 	}
 
 	public Iterator<SchemaEvent> getEventIterator() {
 		return events.values().iterator();
 	}
 
 	public void setEvent(String iEventName, SchemaEvent iEvent) {
 		events.put(iEventName, iEvent);
 	}
 
 	public void setEvent(SchemaEvent iEvent) {
 		setEvent(iEvent.getName(), iEvent);
 	}
 
 	public Type getEmbeddedLanguageType() {
 		SchemaClass cls = getFeature(CoreFieldFeatures.EMBEDDED_TYPE);
 		if (cls != null)
 			return (Type) cls.getLanguageType();
 
 		return null;
 	}
 
 	public void setEmbeddedLanguageType(Type iEmbeddedType) {
 		setEmbeddedType(Roma.schema().getSchemaClassIfExist((Class<?>) iEmbeddedType));
 	}
 
 	public SchemaClass getEmbeddedType() {
 		return getFeature(CoreFieldFeatures.EMBEDDED_TYPE);
 	}
 
 	public void setEmbeddedType(SchemaClass iEmbeddedSchemaClass) {
 		setFeature(CoreFieldFeatures.EMBEDDED_TYPE, iEmbeddedSchemaClass);
 	}
 
 	public SchemaClass[] getEmbeddedTypeGenerics() {
 		return embeddedTypeGenerics;
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException {
 		SchemaField copy = (SchemaField) super.clone();
 		copy.events = new HashMap<String, SchemaEvent>();
 		for (Map.Entry<String, SchemaEvent> entry : events.entrySet()) {
 			copy.events.put(entry.getKey(), (SchemaEvent) entry.getValue().clone());
 		}
 		return copy;
 	}
 
 	protected Object convertValue(Object iFieldValue) {
 		if (type == null || isArray())
 			return iFieldValue;
 
 		SchemaClass typeClass = getType().getSchemaClass();
 		if (typeClass.equals(Roma.schema().getSchemaClass(iFieldValue)))
 			return iFieldValue;
 
 		String textValue = null;
 		if (iFieldValue instanceof String) {
 			textValue = (String) iFieldValue;
 		} else if (iFieldValue != null) {
 			textValue = iFieldValue.toString();
 		}
 
 		Object value = null;
 
 		if (textValue != null) {
 			// TRY A SOFT CONVERSION
 			if (typeClass.isOfType(Integer.class) || typeClass.isOfType(Integer.TYPE)) {
 				try {
 					value = textValue.equals("") ? null : Integer.parseInt(textValue);
 				} catch (Exception e) {
 					value = textValue.equals("") ? null : Double.valueOf(textValue).intValue();
 				}
 			} else if (typeClass.isOfType(Long.class) || typeClass.isOfType(Long.TYPE)) {
 				value = textValue.equals("") ? null : Long.parseLong(textValue);
 			} else if (typeClass.isOfType(Short.class) || typeClass.isOfType(Short.TYPE)) {
 				value = textValue.equals("") ? null : Short.parseShort(textValue);
 			} else if (typeClass.isOfType(Byte.class) || typeClass.isOfType(Byte.TYPE)) {
 				value = textValue.equals("") ? null : Byte.parseByte(textValue);
 			} else if (typeClass.isOfType(Character.class) || typeClass.isOfType(Character.TYPE)) {
 				if (textValue.length() > 0) {
 					value = new Character(textValue.charAt(0));
 				}
 			} else if (typeClass.isOfType(Float.class) || typeClass.isOfType(Float.TYPE)) {
 				value = textValue.equals("") ? null : Float.parseFloat(textValue);
 			} else if (typeClass.isOfType(Double.class) || typeClass.isOfType(Double.TYPE)) {
 				value = textValue.equals("") ? null : Double.parseDouble(textValue);
 			} else if (typeClass.isOfType(BigDecimal.class)) {
 				value = textValue.equals("") ? null : new BigDecimal(textValue);
 			} else if (iFieldValue != null && !typeClass.isArray() && iFieldValue.getClass().isArray()) {
 				// DESTINATION VALUE IS NOT AN ARRAY: ASSIGN THE FIRST ONE ELEMENT
 				value = ((Object[]) iFieldValue)[0];
 			} else {
 				value = iFieldValue;
 			}
 		}
 
 		if (value != null) {
 			// TODO is this the right place to do this...?
 			Class<?> valueClass = value.getClass();
 			// SUCH A MONSTER!!! MOVE THIS LOGIC IN SchemaClass.isAssignableFrom...
			if (value instanceof VirtualObject && !(typeClass.getLanguageType() instanceof Class<?> && ((Class<?>) typeClass.getLanguageType()).isAssignableFrom(VirtualObject.class))
					&& ((VirtualObject) value).getSuperClassObject() != null) {
 				if (ComposedEntity.class.isAssignableFrom(((VirtualObject) value).getSuperClassObject().getClass())) {
 					value = ((VirtualObject) value).getSuperClassObject();
 					valueClass = value.getClass();
 				}
 			}
 
 			if (value instanceof ComposedEntity<?> && !typeClass.isAssignableFrom(valueClass)) {
				value = ((ComposedEntity<?>) value).getEntity();
 			}
 		}
 
 		if (value == null && typeClass.isPrimitive()) {
			log.warn("Cannot set the field value to null for primitive types! Field: " + getEntity() + "." + name + " of class " + getType().getName() + ". Setting value to 0.");
 			// SET THE VALUE TO 0
 			value = SchemaHelper.assignDefaultValueToLiteral(typeClass);
 		}
 		return value;
 	}
 
 	protected Object invokeCallbackBeforeFieldRead(List<SchemaFieldListener> listeners, Object iObject) {
 		Object value = SchemaFieldListener.IGNORED;
 
 		synchronized (listeners) {
 			Object callbackReturn;
 			for (SchemaFieldListener listener : listeners) {
 				callbackReturn = listener.onBeforeFieldRead(iObject, this, value);
 
 				if (callbackReturn != SchemaFieldListener.IGNORED)
 					value = callbackReturn;
 			}
 		}
 		return value;
 	}
 
 	protected Object invokeCallbackAfterFieldRead(List<SchemaFieldListener> listeners, Object iObject, Object value) {
 		synchronized (listeners) {
 			for (SchemaFieldListener listener : listeners) {
 				value = listener.onAfterFieldRead(iObject, this, value);
 			}
 		}
 		return value;
 	}
 
 	public void setEmbeddedTypeGenerics(SchemaClass[] embeddedTypeGenerics) {
 		this.embeddedTypeGenerics = embeddedTypeGenerics;
 	}
 
 	@Override
 	public SchemaField getParent() {
 		return (SchemaField) super.getParent();
 	}
 
 }
