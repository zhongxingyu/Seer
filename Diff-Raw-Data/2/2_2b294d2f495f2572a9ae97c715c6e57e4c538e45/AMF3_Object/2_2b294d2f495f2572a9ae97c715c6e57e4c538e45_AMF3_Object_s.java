 package de.uxnr.amf.v3;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import de.uxnr.amf.AMF_Context;
 import de.uxnr.amf.AMF_Type;
 import de.uxnr.amf.flex.type.ArrayCollection;
 import de.uxnr.amf.flex.type.ObjectProxy;
 import de.uxnr.amf.v3.base.UTF8;
 import de.uxnr.amf.v3.type.Array;
 import de.uxnr.amf.v3.type.ByteArray;
 import de.uxnr.amf.v3.type.Double;
 import de.uxnr.amf.v3.type.False;
 import de.uxnr.amf.v3.type.Integer;
 import de.uxnr.amf.v3.type.Null;
 import de.uxnr.amf.v3.type.Object;
 import de.uxnr.amf.v3.type.String;
 import de.uxnr.amf.v3.type.True;
 import de.uxnr.amf.v3.type.Undefined;
 
 @SuppressWarnings("rawtypes")
 public abstract class AMF3_Object extends Object {
 	private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 
 	@Override
 	public void write(AMF_Context context, DataOutputStream output) throws IOException {
 		this.writeFields(this.getClass(), this.getObjectData());
 		this.writeAttributes(context, output);
 	}
 
 	@Override
 	public AMF_Type read(AMF_Context context, DataInputStream input) throws IOException {
 		this.readAttributes(context, input);
 		this.readFields(this.getClass(), this.getObjectData());
 
 		return this;
 	}
 
 	protected final void writeFields(Class type, Map<UTF8, AMF3_Type> fields) throws IOException {
 		try {
 			for (Field field : type.getDeclaredFields()) {
 				int modifiers = field.getModifiers();
 				if ((modifiers & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
 					continue;
 
 				Field modifiersField = Field.class.getDeclaredField("modifiers");
 				modifiersField.setAccessible(true);
 				modifiersField.setInt(field, (modifiers & ~Modifier.PRIVATE & ~Modifier.PROTECTED) | Modifier.PUBLIC);
 
 				java.lang.String fieldName = field.getName();
 				java.lang.Object fieldValue = field.get(this);
 
 				try {
 					UTF8 name = new UTF8(fieldName);
 					AMF3_Type data = null;
 
 					if (fieldValue == null) {
						
 					} else if (fieldValue instanceof java.lang.String) {
 						data = new String((java.lang.String) fieldValue);
 					} else if (fieldValue instanceof java.lang.Integer) {
 						data = new Integer((java.lang.Integer) fieldValue);
 					} else if (fieldValue instanceof java.lang.Double) {
 						data = new Double((java.lang.Double) fieldValue);
 					} else if (fieldValue instanceof java.lang.Float) {
 						data = new Double((java.lang.Float) fieldValue);
 					} else if (fieldValue instanceof java.lang.Boolean) {
 						boolean value = (java.lang.Boolean) fieldValue;
 						if (value) {
 							data = new True();
 						} else {
 							data = new False();
 						}
 					} else if (fieldValue instanceof int[]) {
 						data = new ByteArray((int[]) fieldValue);
 					} else if (fieldValue instanceof ObjectProxy) {
 						data = (ObjectProxy) fieldValue;
 					} else if (fieldValue instanceof ArrayCollection) {
 						data = (ArrayCollection) fieldValue;
 					} else if (fieldValue instanceof AMF3_Type) {
 						data = (AMF3_Type) fieldValue;
 					}
 
 					if (data != null) {
 						fields.put(name, data);
 					}
 
 				} catch (ClassCastException e) {
 					continue;
 
 				} finally {
 					modifiersField.setInt(field, modifiers);
 					modifiersField.setAccessible(false);
 				}
 			}
 		} catch (Exception e) {
 			throw new IOException(e);
 		}
 	}
 
 	protected final void readFields(Class type, Map<UTF8, AMF3_Type> fields) throws IOException {
 		try {
 			for (Field field : type.getDeclaredFields()) {
 				int modifiers = field.getModifiers();
 				if ((modifiers & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
 					continue;
 
 				Field modifiersField = Field.class.getDeclaredField("modifiers");
 				modifiersField.setAccessible(true);
 				modifiersField.setInt(field, (modifiers & ~Modifier.PRIVATE & ~Modifier.PROTECTED) | Modifier.PUBLIC);
 
 				java.lang.String fieldName = field.getName();
 				java.lang.Object fieldValue = field.get(this);
 
 				try {
 					AMF3_Type data = fields.get(new UTF8(fieldName));
 					java.lang.Object newValue = null;
 
 					if (data instanceof Null || data instanceof Undefined) {
 						if (java.lang.Object.class.isAssignableFrom(field.getType())) {
 							field.set(this, null);
 						}
 						continue;
 					}
 
 					if (field.getType() == java.lang.String.class) {
 						newValue = this.readStringField(data);
 					} else if (field.getType() == java.lang.Integer.class || field.getType() == int.class) {
 						newValue = this.readIntegerField(data);
 					} else if (field.getType() == java.lang.Double.class || field.getType() == double.class) {
 						newValue = this.readDoubleField(data);
 					} else if (field.getType() == java.lang.Boolean.class || field.getType() == boolean.class) {
 						newValue = this.readBooleanField(data);
 					} else if (field.getType() == int[].class) {
 						newValue = this.readByteArrayField(data);
 					} else if (field.getType() == ObjectProxy.class) {
 						newValue = this.readObjectProxyField(data);
 					} else if (field.getType() == ArrayCollection.class) {
 						newValue = this.readArrayCollectionField(data);
 					} else if (AMF3_Type.class.isAssignableFrom(field.getType())) {
 						newValue = this.readTypeField(data);
 					} else if (Collection.class.isAssignableFrom(field.getType())) {
 						newValue = this.readCollectionField(data);
 					} else if (Map.class.isAssignableFrom(field.getType())) {
 						newValue = this.readMapField(data);
 					}
 
 					if (newValue != null) {
 						field.set(this, newValue);
 						this.firePropertyChange(fieldName, fieldValue, newValue);
 					}
 
 				} catch (ClassCastException e) {
 					continue;
 
 				} finally {
 					modifiersField.setInt(field, modifiers);
 					modifiersField.setAccessible(false);
 				}
 			}
 		} catch (Exception e) {
 			throw new IOException(e);
 		}
 	}
 
 	private final java.lang.Object readStringField(AMF3_Type data) {
 		java.lang.String value = null;
 		if (data instanceof String) {
 			value = ((String) data).get();
 		}
 		return value;
 	}
 
 	private final java.lang.Object readIntegerField(AMF3_Type data) {
 		java.lang.Integer value = null;
 		if (data instanceof Integer) {
 			value = ((Integer) data).get();
 		}
 		return value;
 	}
 
 	private final java.lang.Object readDoubleField(AMF3_Type data) {
 		java.lang.Double value = null;
 		if (data instanceof Double) {
 			value = ((Double) data).get();
 		}
 		return value;
 	}
 
 	private final java.lang.Object readBooleanField(AMF3_Type data) {
 		java.lang.Boolean value = null;
 		if (data instanceof True) {
 			value = true;
 		} else if (data instanceof False) {
 			value = false;
 		}
 		return value;
 	}
 
 	private final java.lang.Object readByteArrayField(AMF3_Type data) {
 		int[] value = null;
 		if (data instanceof ByteArray) {
 			value = ((ByteArray) data).get();
 		}
 		return value;
 	}
 
 	private final java.lang.Object readObjectProxyField(AMF3_Type data) {
 		ObjectProxy value = null;
 		if (data instanceof ObjectProxy) {
 			value = (ObjectProxy) data;
 		}
 		return value;
 	}
 
 	private final java.lang.Object readArrayCollectionField(AMF3_Type data) {
 		ArrayCollection value = null;
 		if (data instanceof ArrayCollection) {
 			value = (ArrayCollection) data;
 		}
 		return value;
 	}
 
 	private final java.lang.Object readTypeField(AMF3_Type data) {
 		AMF3_Type value = null;
 		if (data instanceof AMF3_Type) {
 			value = data;
 		}
 		return value;
 	}
 
 	private final java.lang.Object readCollectionField(AMF3_Type data) {
 		Collection<AMF3_Type> value = null;
 		if (data instanceof Array) {
 			value = ((Array) data).values();
 		} else if (data instanceof ArrayCollection) {
 			value = ((ArrayCollection) data).getArray().values();
 		} else if (data instanceof Object) {
 			value = ((Object) data).values();
 		} else if (data instanceof ObjectProxy) {
 			value = ((ObjectProxy) data).getObject().values();
 		}
 		return value;
 	}
 
 	private final java.lang.Object readMapField(AMF3_Type data) {
 		Map<java.lang.String, AMF3_Type> value = null;
 		if (data instanceof Array) {
 			value = this.convertMap(((Array) data).getArrayData());
 		} else if (data instanceof ArrayCollection) {
 			value = this.convertMap(((ArrayCollection) data).getArray().getArrayData());
 		} else if (data instanceof Object) {
 			value = this.convertMap(((Object) data).getObjectData());
 		} else if (data instanceof ObjectProxy) {
 			value = this.convertMap(((ObjectProxy) data).getObject().getObjectData());
 		}
 		return value;
 	}
 
 	private final Map<java.lang.String, AMF3_Type> convertMap(Map<UTF8, AMF3_Type> input) {
 		Map<java.lang.String, AMF3_Type> output = new LinkedHashMap<java.lang.String, AMF3_Type>();
 		for (Entry<UTF8, AMF3_Type> entry : input.entrySet()) {
 			output.put(entry.getKey().get(), entry.getValue());
 		}
 		return output;
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		this.propertyChangeSupport.addPropertyChangeListener(listener);
 	}
 
 	public void addPropertyChangeListener(java.lang.String propertyName, PropertyChangeListener listener) {
 		this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		this.propertyChangeSupport.removePropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(java.lang.String propertyName, PropertyChangeListener listener) {
 		this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
 	}
 
 	protected void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
 		this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
 	}
 }
