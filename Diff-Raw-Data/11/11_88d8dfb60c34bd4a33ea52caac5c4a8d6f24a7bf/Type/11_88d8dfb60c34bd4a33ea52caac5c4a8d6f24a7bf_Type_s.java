 package org.chai.kevin.data;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.Lob;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.chai.kevin.util.Utils;
 import org.chai.kevin.value.Value;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 @Embeddable
 public class Type {
 	
 	public enum ValueType {NUMBER, BOOL, STRING, TEXT, DATE, ENUM, LIST, MAP}
 
 	private String jsonType = "";
 	
 	public Type() {}
 	
 	public Type(String jsonType) {
 		this.jsonType = jsonType;
 		refreshType();
 	}
 	
 	@Lob
 	@Column(nullable=false)
 	public String getJsonType() {
 		return jsonType;
 	}
 	
 	public void setJsonType(String jsonType) {
 		this.jsonType = jsonType;
 		refreshType();
 	}
 	
 	private JSONObject type = null;
 	
 	private Type listType = null;
 	private Map<String, Type> elementMap = null;
 	private String enumCode = null;
 	private ValueType valueType = null;
 	
 	private void refreshType() {
 		type = null;
 		
 		try {
 			type = new JSONObject(jsonType);
 		} catch (JSONException e) {}
 	}
 	
 	@Transient
 	public JSONObject getJsonObject() {
 		return type;
 	}
 	
 	@Transient
 	public ValueType getType() {
 		if (valueType == null) {
 			try {
 				if (getJsonObject() != null) {
 					valueType = ValueType.valueOf(getJsonObject().getString("type").toUpperCase());
 				}
 			} catch (JSONException e) {
 				valueType = null;
 			} catch (IllegalArgumentException e) {
 				valueType = null;
 			}
 		}
 		return valueType;
 	}
 	
 	@Transient
 	public String getEnumCode() {
 		if (enumCode == null) {
 			// TODO think that through
 			if (!getType().equals(ValueType.ENUM)) throw new IllegalStateException();
 			try {
 				enumCode = getJsonObject().getString("enum_code");
 			} catch (JSONException e) {
 				enumCode = null;
 			}
 		}
 		return enumCode;
 	}
 	
 	@Transient
 	public Type getListType() {
 		if (listType == null) {
 			if (!getType().equals(ValueType.LIST)) throw new IllegalStateException();
 			try {
 				listType = new Type(getJsonObject().getString("list_type"));
 			} catch (JSONException e) {
 				listType = null;
 			}
 		}
 		return listType;
 	}
 	
 	@Transient
 	public Map<String, Type> getElementMap() {
 		if (elementMap == null) {
 			if (!getType().equals(ValueType.MAP)) throw new IllegalStateException();
 			Map<String, Type> result = new LinkedHashMap<String, Type>();
 			try {
 				JSONArray array = getJsonObject().getJSONArray("elements");
 				for (int i = 0; i < array.length(); i++) {
 					JSONObject object = array.getJSONObject(i);
 					result.put(object.getString("name"), new Type(object.getString("element_type")));
 				}
 				elementMap = result;
 			} catch (JSONException e) {
 				elementMap = null;
 			}
 		}
 		return elementMap;
 	}
 	
 	@Transient
 	public boolean isValid() {
 		if (getType() == null) return false;
 		switch (getType()) {
 		case LIST:
 			return getListType().isValid();
 		case MAP:
 			for (Type type : getElementMap().values()) {
 				if (!type.isValid()) return false;
 			}
 			break;
 		case ENUM:
 			return getEnumCode() != null;
 		}
 		return true;
 	}
 	
 	@Transient
 	public Value getPlaceHolderValue() {
 		try {
 			JSONObject object = new JSONObject();
 			switch (getType()) {
 				case NUMBER:
 					object.put(Value.VALUE_STRING, 0);
 					break;
 				case BOOL:
 					object.put(Value.VALUE_STRING, true);
 					break;
 				case STRING:
 				case TEXT:
 					object.put(Value.VALUE_STRING, "0");
 					break;
 				case DATE:
 					object.put(Value.VALUE_STRING, "01-01-1970");
 					break;
 				case ENUM:
 					object.put(Value.VALUE_STRING, "0");
 					break;
 				case LIST:
 					JSONArray array1 = new JSONArray();
 					array1.put(getListType().getPlaceHolderValue().getJsonObject());
 					object.put(Value.VALUE_STRING, array1);
 					break;
 				case MAP:
 					Map<String, Type> elementMap = getElementMap();
 					JSONArray array = new JSONArray();
 					for (Entry<String, Type> entry : elementMap.entrySet()) {
 						JSONObject element = new JSONObject();
 						element.put(Value.MAP_KEY, entry.getKey());
 						element.put(Value.MAP_VALUE, elementMap.get(entry.getKey()).getPlaceHolderValue().getJsonObject());
 						array.put(element);
 					}
 					object.put(Value.VALUE_STRING, array);
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 			return new Value(object);
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Transient
 	public Value mergeValueFromMap(Value oldValue, Map<String, Object> map, String suffix, Set<String> attributes) {
 		try {
 			// first we construct the jsonobject containing the value only
 			JSONObject object = new JSONObject();
 			switch (getType()) {
 				case NUMBER:
 				case BOOL:
 				case STRING:
 				case TEXT:
 				case DATE:
 				case ENUM:
 					if (!map.containsKey(suffix)) {
 						if (oldValue.isNull()) object.put(Value.VALUE_STRING, JSONObject.NULL);
 						else object.put(Value.VALUE_STRING, oldValue.getJsonObject().get(Value.VALUE_STRING));
 					}
 					else {
 						object.put(Value.VALUE_STRING, sanitizeValue(map.get(suffix)));
 					}
 					break;
 				case LIST:
 					JSONArray array1 = new JSONArray();
 					if (!map.containsKey(suffix)) {
 						// we don't modify the list but merge the values inside it
 						if (!oldValue.isNull()) { 
 							for (int i = 0; i < oldValue.getListValue().size(); i++) {
 								array1.put(getListType().mergeValueFromMap(oldValue.getListValue().get(i), map, suffix+"["+i+"]", attributes).getJsonObject());
 							}
 						}
 					}
 					else {
 						// the list gets modified with the new indexes
 						List<String> stringIndexList = new ArrayList<String>();
 						if (map.get(suffix) instanceof String[]) stringIndexList.addAll(Arrays.asList((String[])map.get(suffix)));
 						else if (map.get(suffix) instanceof Collection) stringIndexList.addAll((Collection<String>)map.get(suffix));
 						else if (map.get(suffix) instanceof String) stringIndexList.add((String)map.get(suffix));
 
 						List<Integer> filteredIndexList = new ArrayList<Integer>();
 						for (String suffixInBracket : stringIndexList) {
 							String index = suffixInBracket.replace("[", "").replace("]", "");
 							if (NumberUtils.isDigits(index)) filteredIndexList.add(Integer.valueOf(index));
 						}
 						
 						for (Integer index : filteredIndexList) {
 							Value oldListValue = null;
 							if (oldValue.isNull()) oldListValue = Value.NULL;
 							else {
 								if (index < oldValue.getListValue().size()) oldListValue = oldValue.getListValue().get(index);
 								else oldListValue = Value.NULL;
 							}
 							array1.put(getListType().mergeValueFromMap(oldListValue, map, suffix+"["+index+"]", attributes).getJsonObject());
 						}
 					}
 					if (array1.length() == 0) object.put(Value.VALUE_STRING, JSONObject.NULL);
 					else object.put(Value.VALUE_STRING, array1);
 					break;
 				case MAP:
 					Map<String, Type> elementMap = getElementMap();
 					JSONArray array = new JSONArray();
 					for (Entry<String, Type> entry : elementMap.entrySet()) {
 						JSONObject element = new JSONObject();
 						element.put(Value.MAP_KEY, entry.getKey());
 						Value oldMapValue = null;
 						if (oldValue.isNull()) oldMapValue = Value.NULL;
 						else {
 							oldMapValue = oldValue.getMapValue().get(entry.getKey());
 							if (oldMapValue == null) oldMapValue = Value.NULL;
 						}
 						element.put(Value.MAP_VALUE, elementMap.get(entry.getKey()).mergeValueFromMap(oldMapValue, map, suffix+"."+entry.getKey(), attributes).getJsonObject());
 						array.put(element);
 					}
 					object.put(Value.VALUE_STRING, array);
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 			
 			// then we construct a new value object and set the attributes on it
 			Value value = new Value(object);
 			for (String attribute : attributes) {
 				if (!map.containsKey(suffix)) {
 					value.setAttribute(attribute, oldValue.getAttribute(attribute));
 				}
 				else {
 					Object attributeValue = map.get(suffix+"["+attribute+"]");
 					String attributeString = String.valueOf(attributeValue);
 					if (attributeValue != null && !attributeString.isEmpty()) value.setAttribute(attribute, attributeString);
 				}
 			}
 			return value;
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Transient
 	public Value getValue(Object value) {
 		if (value == null) return Value.NULL;
 		try {
 			JSONObject object = new JSONObject();
 			switch (getType()) {
 				case NUMBER:
 					object.put(Value.VALUE_STRING, (Number)value);
 					break;
 				case BOOL:
 					object.put(Value.VALUE_STRING, (Boolean)value);
 					break;
 				case STRING:
 				case TEXT:
 					object.put(Value.VALUE_STRING, (String)value);
 					break;
 				case DATE:
 					object.put(Value.VALUE_STRING, (Utils.formatDate((Date)value)));
 					break;
 				case ENUM:
 					object.put(Value.VALUE_STRING, (String)value);
 					break;
 				case LIST:
 					JSONArray array1 = new JSONArray();
 					for (Object item : (List<?>)value) {
 						array1.put(getListType().getValue(item).getJsonObject());
 					}
 					if (array1.length() == 0) object.put(Value.VALUE_STRING, JSONObject.NULL);
 					else object.put(Value.VALUE_STRING, array1);
 					break;
 				case MAP:
 					Map<String, Type> elementMap = getElementMap();
 					JSONArray array = new JSONArray();
 					for (Entry<String, Object> entry : ((Map<String, Object>)value).entrySet()) {
 						JSONObject element = new JSONObject();
 						element.put(Value.MAP_KEY, entry.getKey());
 						element.put(Value.MAP_VALUE, elementMap.get(entry.getKey()).getValue(entry.getValue()).getJsonObject());
 						array.put(element);
 					}
 					object.put(Value.VALUE_STRING, array);
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 			return new Value(object);
 		} catch (JSONException e) {
 			throw new IllegalArgumentException("object "+value+" does not correspond to type "+type, e);
 		}
 	}
 	
 	public Value getValueFromJaql(String jaqlString) {
 		if (jaqlString.equals("null")) return Value.NULL;
 		try {
 			JSONObject object = new JSONObject();
 			switch (getType()) {
 				case NUMBER:
 					if (!NumberUtils.isNumber(jaqlString))
 						throw new IllegalArgumentException("jaql string is not a number: "+jaqlString);
 					object.put(Value.VALUE_STRING, Double.parseDouble(jaqlString));
 					break;
 				case BOOL:
 					if (!jaqlString.equals("true") && !jaqlString.equals("false")) 
 						throw new IllegalArgumentException("jaql string is not a boolean: "+jaqlString);
 					object.put(Value.VALUE_STRING, Boolean.parseBoolean(jaqlString));
 					break;
 				case STRING:
 				case TEXT:
 					object.put(Value.VALUE_STRING, StringUtils.strip(jaqlString, "\""));
 					break;
 				case DATE:
 					try {
 						Date date = Utils.parseDate(StringUtils.strip(jaqlString, "\""));
 						object.put(Value.VALUE_STRING, Utils.formatDate(date));
 					} catch (ParseException e) {
 						throw new IllegalArgumentException("jaql string is not a date: "+jaqlString, e);
 					}
 					break;
 				case ENUM:
 					object.put(Value.VALUE_STRING, StringUtils.strip(jaqlString, "\""));
 					break;
 				case LIST:
 					JSONArray values = new JSONArray();
 					JSONArray array = new JSONArray(jaqlString);
 					for (int i = 0; i < array.length(); i++) {
 						String itemJaqlString = array.getString(i);
 						values.put(getListType().getValueFromJaql(itemJaqlString).getJsonObject());
 					}
 					if (values.length() == 0) object.put(Value.VALUE_STRING, JSONObject.NULL);
 					else object.put(Value.VALUE_STRING, values);
 					break;
 				case MAP:
 					JSONObject jaqlObject = new JSONObject(jaqlString);
 					
 					Map<String, Type> elementMap = getElementMap();
 					JSONArray array1 = new JSONArray();
 					for (Entry<String, Type> entry : elementMap.entrySet()) {
 						JSONObject element = new JSONObject();
 						element.put(Value.MAP_KEY, entry.getKey());
 						element.put(Value.MAP_VALUE, entry.getValue().getValueFromJaql(jaqlObject.getString(entry.getKey())).getJsonObject());
 						array1.put(element);
 					}
 					object.put("value", array1);
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 			return new Value(object);
 		} catch (JSONException e) {
 			throw new IllegalArgumentException("jaql value does not correspond to type", e);
 		}
 	}
 	
 	public String getJaqlValue(Value value) {
 		StringBuilder result = new StringBuilder();
 		if (value.isNull()) result.append("null");
 		else {
 			switch (getType()) {
 				case NUMBER:
 					result.append(value.getNumberValue().toString());
 					break;
 				case BOOL:
 					result.append(value.getBooleanValue().toString());
 					break;
 				case STRING:
 					result.append("\""+value.getStringValue()+"\"");
 					break;
 				case DATE:
 					result.append("\""+Utils.formatDate(value.getDateValue())+"\"");
 					break;
 				case ENUM:
 					result.append("\""+value.getEnumValue()+"\"");
 					break;
 				case LIST:
 					result.append("[");
 					for (Value item : value.getListValue()) {
 						result.append(getListType().getJaqlValue(item));
 						result.append(',');
 					}
 					result.append("]");
 					break;
 				case MAP:
 					result.append("{");
 					for (Entry<String, Value> entry : value.getMapValue().entrySet()) {
 						result.append("\""+entry.getKey()+"\"");
 						result.append(":");
 						result.append(getElementMap().get(entry.getKey()).getJaqlValue(entry.getValue()));
 						result.append(",");
 					}
 					result.append("}");
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 		}
 		return result.toString();
 	}
 	
 	public void setAttribute(final Value value, final String prefix, final String attribute, final String text) {
 		// TODO throw exception if prefix does not exist
 		transformValue(value, "", new ValuePredicate() {
 			@Override
 			public boolean transformValue(Value currentValue, Type currentType, String currentPrefix) {
 				if (currentPrefix.equals(prefix)) {
 					currentValue.setAttribute(attribute, text);
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 	
 	public void setValue(Value value, final String prefix, final Value toSet) {
 		// TODO throw exception if prefix does not exist
 		transformValue(value, "", new ValuePredicate() {
 			@Override
 			public boolean transformValue(Value currentValue, Type currentType, String currentPrefix) {
 				if (currentPrefix.equals(prefix)) {
 					currentValue.setJsonValue(toSet.getJsonValue());
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 	
 	public String getAttribute(Value value, String prefix, String attribute) {
 		Value prefixedValue = getValue(value, prefix);
 		if (prefixedValue == null) return null;
 		return prefixedValue.getAttribute(attribute);
 	}
 	
 	public Value getValue(Value value, String prefix) {
 		Value prefixedValue = getValue(prefix, value, "");
 //		if (prefixedValue == null) throw new IndexOutOfBoundsException("prefix "+prefix+" not found in value "+value);
 		return prefixedValue;
 	}
 	
 //	public	 boolean hasPrefix(Value value, String prefix) {
 //		return getValue(prefix, value, "") != null;
 //	}
 	
 	private Value getValue(String prefix, Value currentValue, String currentPrefix) {
 		if (prefix.equals(currentPrefix)) return currentValue;
 		else if (!currentValue.isNull()) {
 			switch (getType()) {
 				case LIST:
 					List<Value> values = currentValue.getListValue();
 					Type listType = getListType();
 					for (int i = 0; i < values.size(); i++) {
 						if (prefix.startsWith(currentPrefix+"["+i+"]")) return listType.getValue(prefix, values.get(i), currentPrefix+"["+i+"]");
 					}
 					break;
 				case MAP:
 					Map<String, Type> typeMap = getElementMap();
 					Map<String, Value> valueMap = currentValue.getMapValue();
 					for (Entry<String, Value> entry : valueMap.entrySet()) {
 						if (prefix.startsWith(currentPrefix+"."+entry.getKey())) return typeMap.get(entry.getKey()).getValue(prefix, entry.getValue(), currentPrefix+"."+entry.getKey());
 					}
 					break;
 				default:
 					break;
 			}
 		}
 		return null;
 	}
 	
 	public static interface ValuePredicate {
 		public boolean transformValue(Value currentValue, Type currentType, String currentPrefix);
 	}
 	
 	// depth-first transform
 	public void transformValue(Value currentValue, ValuePredicate predicate) {
 		transformValue(currentValue, "", predicate);
 	}
 	
 	private boolean transformValue(Value currentValue, String currentPrefix, ValuePredicate predicate) {
 		boolean changed = false;
 		if (!currentValue.isNull()) {
 			try {
 				switch (getType()) {
 					case NUMBER:
 					case STRING:
 					case DATE:
 					case ENUM:
 					case BOOL:
 //						value = currentValue;
 						break;
 					case LIST:
 						Type listType = getListType();
 						
 						List<Value> listValues = currentValue.getListValue();
 						for (int i = 0; i < listValues.size(); i++) {
 							changed = changed | listType.transformValue(listValues.get(i), currentPrefix+"["+i+"]", predicate);
 						}
 					
 						if (changed) {
 							JSONObject object1 = new JSONObject(currentValue.getJsonValue());
 							JSONArray array1 = new JSONArray();
 							for (int i = 0; i < listValues.size(); i++) {
 								array1.put(i, listValues.get(i).getJsonObject());
 							}
 							object1.put(Value.VALUE_STRING, array1);
 							currentValue.setJsonObject(object1);
 						}
 						break;
 					case MAP:
 						Map<String, Type> typeMap = getElementMap();
 						
 						Map<String, Value> mapValues = currentValue.getMapValue();
 						for (Entry<String, Value> entry : mapValues.entrySet()) {
 							changed = changed | typeMap.get(entry.getKey()).transformValue(entry.getValue(), currentPrefix+"."+entry.getKey(), predicate);
 						}
 						
 						if (changed) {
 							JSONObject object2 = new JSONObject(currentValue.getJsonValue());
 							JSONArray array2 = new JSONArray();
 							for (Entry<String, Value> entry : mapValues.entrySet()) {
 								JSONObject element = new JSONObject();
 								element.put(Value.MAP_KEY, entry.getKey());
 								element.put(Value.MAP_VALUE, mapValues.get(entry.getKey()).getJsonObject());
 								array2.put(element);
 							}
 							object2.put(Value.VALUE_STRING, array2);
 							currentValue.setJsonObject(object2);
 						}
 						break;
 					default:
 						throw new NotImplementedException();
 				}
 				
 			}
 			catch(JSONException e) {
 				throw new IllegalArgumentException();
 			}
 		}
 		return changed | predicate.transformValue(currentValue, this, currentPrefix);
 	}
 	
 	public void getCombinations(Value value, List<String> strings, Set<List<String>> combinations, String prefix) {
 		switch (getType()) {
 			case NUMBER:
 			case BOOL:
 			case STRING:
 			case DATE:
 			case ENUM:
 				combinations.add(strings);
 				break;
 			case LIST:
 				if (!value.isNull()) {
 					List<Value> values = value.getListValue();
 					Type listType = getListType();
 					for (int i = 0; i < values.size(); i++) {
 						combinations.add(replace(strings, prefix+"[_]", prefix+"["+i+"]"));
 						listType.getCombinations(values.get(i), strings, combinations, prefix+"["+i+"]");
 						listType.getCombinations(values.get(i), strings, combinations, prefix+"[]");
 					}
 				}
 				break;
 			case MAP:
 				if (!value.isNull()) {
 					Map<String, Type> typeMap = getElementMap();
 					for (Entry<String, Value> entry : value.getMapValue().entrySet()) {
 						typeMap.get(entry.getKey()).getCombinations(entry.getValue(), strings, combinations, prefix+"."+entry.getKey());
 					}
 					break;
 				}
 			default:
 				throw new NotImplementedException();
 		}
 	}
 
 	public Map<String, Value> getPrefixes(Value value, PrefixPredicate predicate) {
 		Map<String, Value> result = new LinkedHashMap<String, Value>();
 		getPrefixes(value, "", result, predicate);
 		return result;
 	}
 	
 	private void getPrefixes(Value value, String prefix, Map<String, Value> prefixes, PrefixPredicate predicate) {
 		predicate.types.push(this);
 		if (predicate.holds(this, value, prefix)) prefixes.put(prefix, value);
 		if (value != null && !value.isNull()) {
 			switch (getType()) {
 				case NUMBER:
 				case BOOL:
 				case STRING:
 				case DATE:
 				case ENUM:
 					break;
 				case LIST:
 					Type listType = getListType();
 					for (int i = 0; i < value.getListValue().size(); i++) {
 						listType.getPrefixes(value.getListValue().get(i), prefix+"["+i+"]", prefixes, predicate);
 					}
 					break;
 				case MAP:
 					for (Entry<String, Type> entry : getElementMap().entrySet()) {
 						entry.getValue().getPrefixes(value.getMapValue().get(entry.getKey()), prefix+"."+entry.getKey(), prefixes, predicate);
 					}
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 		}
 		predicate.types.pop();
 	}
 
 	public static abstract class PrefixPredicate {
 		
 		Stack<Type> types = new Stack<Type>();
 		
 		public Type getParent() {
 			if (types.size() >= 2) return types.get(types.size() - 2);
 			return null;
 		}
 		
 		public abstract boolean holds(Type type, Value value, String prefix);
 	}
 	
 	public String getDisplayValue(Value value) {
 		// TODO implement this
 		return getJaqlValue(value);
 	}
 	
 	private Object sanitizeValue(Object value) {
 		Object result = null;
 		String string = String.valueOf(value);
 		switch (getType()) {
 			case NUMBER:
 				if (string.trim().isEmpty()) result = JSONObject.NULL;
 				else {
 					try {
 						result = Double.parseDouble(string);
 					} catch (NumberFormatException e) {
 						result = JSONObject.NULL;
 					}
 				}
 				break;
 			case BOOL:
 				if (value != null && string.equals("0")) result = false;
 				else if (value != null && !string.equals("") && !string.equals("0")) result = true;
 				else result = JSONObject.NULL;
 				break;
 			case STRING:
 				if (value == null || string.equals("")) result = JSONObject.NULL;
 				else result = string;
 				break;
 			case DATE:
 				if (value == null || string.equals("")) result = JSONObject.NULL;
 				else result = string;
 				break;
 			case ENUM:
 				if (value == null || string.equals("null")) result = JSONObject.NULL;
 				else result = string; 
 				break;
 			default:
 				if (value == null || string.equals("null")) result = JSONObject.NULL;
 				else result = string;
 		}
 		return result;
 	}
 	
 	private List<String> replace(List<String> strings, String toReplace, String replaceWith) {
 		List<String> result = new ArrayList<String>();
 		for (String string : strings) {
 			result.add(string.replace(toReplace, replaceWith));
 		}
 		return result;
 	}
 	
 	public String getDisplayedValue(int indent, Integer numberOfLines) {
 		return getDisplayedValue(indent, 0, numberOfLines, 1);
 	}
 		
 	private String getDisplayedValue(final int indent, int currentIndent, final Integer numberOfLines, Integer currentNumberOfLines) {
 		StringBuilder builder = new StringBuilder();
 		
 		String typeName = null;
 		switch (getType()) {
 			case NUMBER:
 			case BOOL:
 			case STRING:
 			case DATE:
 			case LIST:
 			case MAP:
 				typeName = getType().name().toLowerCase();
 				break;
 			case ENUM:
 				typeName = getType().name().toLowerCase()+"("+getEnumCode()+")";
 				break;
 			default:
 				throw new NotImplementedException();
 		}
 		
 		builder.append(typeName);
 		
 		switch (getType()) {
 			case LIST:
 				builder.append(" : ");
 				builder.append(getListType().getDisplayedValue(indent, currentIndent+indent, numberOfLines, currentNumberOfLines));
 				break;
 			case MAP:
 				for (Entry<String, Type> entry : getElementMap().entrySet()) {
 					if (numberOfLines == null || numberOfLines > currentNumberOfLines) {
 						currentNumberOfLines = currentNumberOfLines + 1;
 						builder.append("\n");
 						builder.append(StringUtils.leftPad(entry.getKey()+" : ", entry.getKey().length()+3+currentIndent+indent));
 						builder.append(entry.getValue().getDisplayedValue(indent, currentIndent+indent, numberOfLines, currentNumberOfLines));
 					}
 					else {
 						builder.append(" ...");
 						break;
 					}
 				}
 				break;
 			default:
 				break;
 		}
 		
 		return builder.toString();
 	}
 	
 	@Override
 	public String toString() {
 		return jsonType;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((getJsonObject() == null) ? 0 : getJsonObject().toString().hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Type))
 			return false;
 		Type other = (Type) obj;
 		if (getJsonObject() == null) {
 			if (other.getJsonObject() != null)
 				return false;
 		} else if (!getJsonObject().toString().equals(other.getJsonObject().toString()))
 			return false;
 		return true;
 	}
 
 	public static Type TYPE_DATE() { return new Type("{\"type\":\"date\"}");}
 	public static Type TYPE_STRING() { return new Type("{\"type\":\"string\"}");}
 	public static Type TYPE_NUMBER() { return new Type("{\"type\":\"number\"}");}
 	public static Type TYPE_BOOL() { return new Type("{\"type\":\"bool\"}");}
 	
 	public static Type TYPE_MAP (Map<String, Type> map) {
 		StringBuilder builder = new StringBuilder();
 		for (Entry<String, Type> entry : map.entrySet()) {
 			builder.append("{\"name\":\""+entry.getKey()+"\", \"element_type\":"+entry.getValue().toString()+"}");
 			builder.append(',');
 		}
 		return new Type("{\"type\":\"map\", \"elements\":["+builder.toString()+"]}");
 	}
 
 	public static Type TYPE_LIST (Type listType) {
 		return new Type("{\"type\":\"list\", \"list_type\":"+listType.toString()+"}");
 	}
 
 	public static Type TYPE_ENUM (String enumCode) {
 		return new Type("{\"type\":\"enum\", \"enum_code\":"+enumCode+"}");
 	}
 
 }
