 package ch.compass.gonzoproxy.model;
 
 import java.io.Serializable;
 
 public class Field implements Serializable, Cloneable {
 
 	private static final long serialVersionUID = -6724126085297330455L;
 
 	private String name;
 	private String value;
 	private String description;
 
 	public Field() {
 
 	}
 
 	public Field(String name, String value, String description) {
 		this.name = name;
 		this.value = value;
 		this.description = description;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Override
 	public Field clone() {
 		Field clonedField = new Field();
 		clonedField.setDescription(description);
 		clonedField.setName(name);
 		clonedField.setValue(value);
 		return clonedField;
 	}
 
 	public String toAscii() {

 		StringBuffer sb = new StringBuffer("");
 		String hexPacketData = this.value.replaceAll("\\s", "");
 		String[] hexPacketValues = hexPacketData.split("(?<=\\G..)");
 
 		for (String hexValue : hexPacketValues) {
 			try {
 				int decimalValue = Integer.parseInt(hexValue, 16);
 				char asciiValue = (char) decimalValue;
 				sb.append(asciiValue);
 			} catch (NumberFormatException e) {
 				sb.append('?');
 			}
 		}
 		return sb.toString();
 
 	}
 
 	public void replaceValue(String originalValue, String replacedValue) {
		if (replacedValue.isEmpty() && !originalValue.isEmpty()) {
			value += " ";
			originalValue += " ";
			value = value.replace(originalValue, replacedValue);
			if(!value.isEmpty()){
				value = value.substring(0, value.length() - 1);
			}
		} else {
			value = value.replace(originalValue, replacedValue);
		}
 	}
 
 }
