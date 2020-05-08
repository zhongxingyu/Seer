 package org.iucn.sis.shared.api.models.fields;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.BooleanPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.DatePrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.FloatPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.IntegerPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.StringPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;
 
 public class ProxyField {
 	
 	protected final Field proxy;
 	
 	public ProxyField(Field field) {
 		this.proxy = field == null ? new Field() : field;
 	}
 	
 	public Integer getForeignKeyPrimitiveField(String key) {
 		return getForeignKeyPrimitiveField(key, null);
 	}
 	
 	public Integer getForeignKeyPrimitiveField(String key, Integer defaultValue) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field == null)
 			return defaultValue;
 		else
 			return ((ForeignKeyPrimitiveField)field).getValue();
 	}
 	
 	public void setForeignKeyPrimitiveField(String key, Integer value) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null) {
 			if (value != null)
 				((ForeignKeyPrimitiveField)field).setValue(value);
 			else
 				proxy.getPrimitiveField().remove(field);
 		}
 		else if (value != null)
			proxy.addPrimitiveField(new ForeignKeyPrimitiveField(key, proxy, value, null));
 	}
 	
 	public List<Integer> getForeignKeyListPrimitiveField(String key) {
 		return getForeignKeyListPrimitiveField(key, new ArrayList<Integer>());
 	}
 	
 	public List<Integer> getForeignKeyListPrimitiveField(String key, List<Integer> defaultValue) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field == null)
 			return defaultValue;
 		else
 			return ((ForeignKeyListPrimitiveField)field).getValue();
 	}
 	
 	public void setForeignKeyListPrimitiveField(String key, List<Integer> value) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null) {
 			if (value != null)
 				((ForeignKeyListPrimitiveField)field).setValue(value);
 			else
 				proxy.getPrimitiveField().remove(field);
 		}
 		else if (value != null) {
 			ForeignKeyListPrimitiveField newField = 
 				new ForeignKeyListPrimitiveField(key, proxy);
 			newField.setValue(value);
 			
 			proxy.addPrimitiveField(newField);
 		}
 	}
 	
 	public Float getFloatPrimitiveField(String key) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field == null)
 			return null;
 		else
 			return ((FloatPrimitiveField)field).getValue();
 	}
 	
 	public void setFloatPrimitiveField(String key, Float value) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null) {
 			if (value != null && !"".equals(value))
 				((FloatPrimitiveField)field).setValue(value);
 			else
 				proxy.getPrimitiveField().remove(field);
 		}
 		else if (value != null && !"".equals(value))
 			proxy.addPrimitiveField(new FloatPrimitiveField(key, proxy, value));
 	}
 	
 	public Integer getIntegerPrimitiveField(String key) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field == null)
 			return null;
 		else
 			return ((IntegerPrimitiveField)field).getValue();
 	}
 	
 	public void setIntegerPrimitiveField(String key, Integer value) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null) {
 			if (value != null && !"".equals(value))
 				((IntegerPrimitiveField)field).setValue(value);
 			else
 				proxy.getPrimitiveField().remove(field);
 		}
 		else if (value != null && !"".equals(value))
 			proxy.addPrimitiveField(new IntegerPrimitiveField(key, proxy, value));
 	}
 	
 	public String getStringPrimitiveField(String key) {
 		String value;
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field == null)
 			value = null;
 		else
 			value = ((StringPrimitiveField)field).getValue();
 		if (value == null)
 			value = "";
 		return value;
 	}
 	
 	public void setStringPrimitiveField(String key, String value) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null) {
 			if (value != null && !"".equals(value))
 				field.setRawValue(value);
 			else
 				proxy.getPrimitiveField().remove(field);
 		}
 		else if (value != null && !"".equals(value))
 			proxy.addPrimitiveField(new StringPrimitiveField(key, proxy, value));
 	}
 	
 	public String getTextPrimitiveField(String key) {
 		String value;
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field == null)
 			value = null;
 		else
 			value = ((TextPrimitiveField)field).getValue();
 		if (value == null)
 			value = "";
 		return value;
 	}
 	
 	public void setTextPrimitiveField(String key, String value) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null) {
 			if (value != null && !"".equals(value))
 				field.setRawValue(value);
 			else
 				proxy.getPrimitiveField().remove(field);
 		}
 		else if (value != null && !"".equals(value))
 			proxy.addPrimitiveField(new TextPrimitiveField(key, proxy, value));
 	}
 	
 	public Boolean getBooleanPrimitiveField(String key, boolean defaultValue) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null)
 			return ((BooleanPrimitiveField)field).getValue();
 		else
 			return defaultValue;
 	}
 	
 	public void setBooleanPrimitiveField(String key, Boolean value, Boolean defaultValue) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null) {
 			if (value != null && !value.equals(defaultValue))
 				((BooleanPrimitiveField)field).setValue(value);
 			else
 				proxy.getPrimitiveField().remove(field);
 		}
 		else if (value != null && !value.equals(defaultValue))
 			proxy.addPrimitiveField(new BooleanPrimitiveField(key, proxy, value));
 		else
 			proxy.getPrimitiveField().remove(field);
 	}
 	
 	public Date getDatePrimitiveField(String key) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field == null)
 			return null;
 		else
 			return ((DatePrimitiveField)field).getValue();
 	}
 	
 	public void setDatePrimitiveField(String key, Date value) {
 		PrimitiveField<?> field = proxy.getPrimitiveField(key);
 		if (field != null) {
 			if (value != null)
 				((DatePrimitiveField)field).setValue(value);
 			else
 				proxy.getPrimitiveField().remove(field);
 		}
 		else if (value != null)
 			proxy.addPrimitiveField(new DatePrimitiveField(key, proxy, value));
 	}
 
 }
