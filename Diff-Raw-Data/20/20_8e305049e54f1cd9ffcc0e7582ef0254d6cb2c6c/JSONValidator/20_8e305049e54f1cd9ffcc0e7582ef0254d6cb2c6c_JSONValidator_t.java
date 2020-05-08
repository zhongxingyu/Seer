 /* 
  *  Copyright (C) 2013 Royall & Company
  *
  *  JSON DTD is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  Free Software Foundation,version 3.
  *  
  *  JSON DTD is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with JSON DTD.  If not, see http://www.gnu.org/licenses/
  *  
  *  Additional permission under GNU GPL version 3 section 7
  *  
  *  If you modify this Program, or any covered work, by linking or combining 
  *  it with any of the JARS listed in the README.txt (or a modified version of 
  *  (that library), containing parts covered by the terms of that JAR, the 
  *  licensors of this Program grant you additional permission to convey the 
  *  resulting work. 
  *  
  */
 package com.royall.jsondtd;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.royall.jsondtd.exceptions.JSONValidatorException;
 import com.royall.jsondtd.exceptions.PrototypeException;
 import com.royall.jsondtd.util.DateUtil;
 
 public class JSONValidator {
 
 	private String failMessage = null;
 	private Object returnJson = null;
 
 	public final static String KEY_TYPE = "type";
 	public final static String KEY_CLASS = "class";
 	public final static String KEY_NORMALIZE = "normalize";
 	public final static String KEY_DENORMALIZE = "denormalize";
 	public final static String KEY_DEFAULT = "def";
 	public final static String KEY_DEFAULT_ITEM = "defitem";
 	public final static String KEY_REQ = "req";
 	public final static String KEY_ENUM = "enum";
 	public final static String KEY_STRING_ERR_ON_EMPTY = "err_on_empty";
 	public final static String KEY_STRING_REMOVE_EMPTY = "remove_empty";
 	public final static String KEY_REGEX = "regex";
 	public final static String KEY_FIELDS = "fields";
 	public final static String KEY_FIELDS_MIN = "min";
 	public final static String KEY_FIELDS_MAX = "max";
 	public final static String KEY_DATE_MUST_BE_AFTER = "after";
 	public final static String KEY_DATE_MUST_BE_BEFORE = "before";
 	public final static String KEY_NUM_MIN = "min";
 	public final static String KEY_NUM_MAX = "max";
 	public final static String KEY_ERR_ON = "err_on";
 	public final static String KEY_CHILDREN = "children";
 	public final static String KEY_ARRAY_MIN = "min";
 	public final static String KEY_ARRAY_MAX = "max";
 	public final static String KEY_WILDCARD_FIELD = "*";
 
 	private final boolean errorOnUnspecifiedKeys;
 	private final boolean removeUnspecifiedKeys;
 	private final boolean removeKeysWhenValueEmpty;
 	private final String datePattern;
 
 	private Map<String, Map<String, ?>> customTypes = new HashMap<String, Map<String, ?>>();
 
 	// Custom HashMap will return a current date if getting the key "now"
 	private Map<String, Object> defaultItems = new HashMap<String, Object>() {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Object get(Object _o) {
 			if (_o instanceof String && ((String) _o).equalsIgnoreCase("now") && !super.containsKey(_o))
 				return new java.util.Date();
 			else
 				return super.get(_o);
 		}
 
 		@Override
 		public boolean containsKey(Object _o) {
 			if (_o instanceof String && ((String) _o).equalsIgnoreCase("now"))
 				return true;
 			else
 				return super.containsKey(_o);
 		}
 	};
 
 	public JSONValidator() {
 		this(new HashMap<String, Object>());
 	}
 
 	public JSONValidator(Map<String, ?> _options) {
 		if (_options == null)
 			throw new IllegalStateException("_options cannot be null");
 
 		Object errorOnUnspecifiedKeys = _options.get(ValidationOptions.ErrorOnUnspecifiedKeys);
 		Object removeUnspecifiedKeys = _options.get(ValidationOptions.RemoveUnspecifiedKeys);
 		Object removeKeysWhenValueEmpty = _options.get(ValidationOptions.RemoveKeysWhenValueEmpty);
 		Object datePattern = _options.get(ValidationOptions.DatePattern);
 
 		this.errorOnUnspecifiedKeys = (errorOnUnspecifiedKeys instanceof Boolean) && ((Boolean) errorOnUnspecifiedKeys);
 		this.removeUnspecifiedKeys = (removeUnspecifiedKeys instanceof Boolean) && ((Boolean) removeUnspecifiedKeys);
 		this.removeKeysWhenValueEmpty = (removeKeysWhenValueEmpty instanceof Boolean) && ((Boolean) removeKeysWhenValueEmpty);
 		this.datePattern = (datePattern != null) ? datePattern.toString() : null;
 	}
 
 	public void addDefaultItem(String _key, Object _item) {
 		defaultItems.put(_key, _item);
 	}
 
 	public void addDefaultItems(Map<String, ?> _defaultItems) {
 		defaultItems.putAll(_defaultItems);
 	}
 
 	public Map<String, Object> getDefaultItemsMap() {
 		return new HashMap<String, Object>(this.defaultItems);
 	}
 
 	public void addCustomType(String _typeName, Map<String, ?> _customTypePrototype) {
 		customTypes.put(_typeName, _customTypePrototype);
 	}
 
 	public void addCustomTypes(Map<String, Map<String, ?>> _customTypes) {
 		customTypes.putAll(_customTypes);
 	}
 
 	public Map<String, ?> getCustomTypes() {
 		return new HashMap<String, Object>(this.customTypes);
 	}
 
 	/**
 	 * Returns the fail message for the last validation test. Null if validation passed.
 	 * 
 	 * @return Validation fail message, or null if passed.
 	 */
 	public String getFailMessage() {
 		return failMessage;
 	}
 
 	public Object getReturnJson() {
 		return returnJson;
 	}
 
 	public boolean validate(Object _json, Map<String, ?> _prototype) throws JSONValidatorException {
 
 		// Reset fail message
 		failMessage = null;
 		returnJson = null;
 
 		JSONBlock testBuild = new JSONBlock();
 		try {
 			boolean result = validateBlock(new JSONBlock(_json), _prototype, testBuild);
 			returnJson = testBuild.getBlock();
 			return result;
 		} catch (PrototypeException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new JSONValidatorException(e.getMessage(), e);
 		}
 
 	}
 
 	// Main Block Handler that will be called for Document and every SubDocument
 	private boolean validateBlock(JSONBlock _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		if (_prototype.get(KEY_CLASS) != null) {
 			return handleClass(_json, _prototype, _testBuild);
 		} else if (_prototype.get(KEY_TYPE) != null) {
 			return handleType(_json, _prototype, _testBuild);
 		} else
 			throw new PrototypeException("'" + KEY_TYPE + "' or '" + KEY_CLASS + "' is required for each level in prototype.");
 	}
 
 	private boolean handleClass(JSONBlock _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		String classExpectedValue = _prototype.get(KEY_CLASS).toString();
 
 		String classFound;
 		Object json;
 		if ((json = _json.getBlock()) == null) {
 			classFound = "null";
 		} else {
 			classFound = json.getClass().getName();
 		}
 
 		// Check for valid class
 		List<String> classExpectedList = Arrays.asList(classExpectedValue.split("\\s*,\\s*"));
 		boolean classCompare = false;
 		for (String classExpected : classExpectedList) {
 			if (checkClass(classExpected, classFound))
 				classCompare = true;
 		}
 		if (!classCompare) {
 			failMessage = " Expected Class (" + classExpectedValue + ") found Class (" + classFound + ")";
 			return false;
 		}
 
 		// Evaluate Type
 		if (_prototype.get(KEY_TYPE) != null) {
 			// Normalize
 			Object normalize = _prototype.get(KEY_NORMALIZE);
 			Class<?> c = null;
 			Object instance = null;
 			if (normalize != null) {
 
 				try {
 					c = Class.forName(normalize.toString());
 					Method m = c.getDeclaredMethod(KEY_NORMALIZE, Object.class);
 					instance = Class.forName(normalize.toString()).newInstance();
 					json = m.invoke(instance, json);
 				} catch (Exception e) {
 					throw new PrototypeException("Failed to invoke " + normalize.toString() + "." + KEY_NORMALIZE + "(Object _jsonValue) due to " + e.getClass().getName() + ": " + e.getMessage(), e);
 				}
 			}
 
 			// Evaluate the normalized Object by the type property
 			boolean typeResult = handleType(new JSONBlock(json, _json.getParent()), _prototype, _testBuild);
 			if (!typeResult)
 				return typeResult;
 
 			// Now denormalize and save
 			Object denormalize = _prototype.get(KEY_DENORMALIZE);
			if (normalize != null && denormalize instanceof Boolean && (Boolean)denormalize) {
 				try {
 					Method m = c.getDeclaredMethod(KEY_DENORMALIZE, Object.class);
 					json = m.invoke(instance, _testBuild.getBlock());
 				} catch (Exception e) {
					throw new PrototypeException("Failed to invoke " + normalize.toString() + "." + KEY_DENORMALIZE + "(Object _jsonValue) due to " + e.getClass().getName() + ": " + e.getMessage(), e);
 				}
 			} else {
 				json = _testBuild.getBlock();
 			}
 		}
 
 		_testBuild.putBlock(json);
 
 		return true;
 	}
 
 	private boolean handleType(JSONBlock _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		String type = _prototype.get(KEY_TYPE).toString();
 		if (type.equalsIgnoreCase("string"))
 			return handleStringType(_json.getBlock(), _prototype, _testBuild);
 		else if (type.equalsIgnoreCase("number") || type.equalsIgnoreCase("numeric"))
 			return handleNumberType(_json.getBlock(), _prototype, _testBuild);
 		else if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("bool"))
 			return handleBooleanType(_json.getBlock(), _prototype, _testBuild);
 		else if (type.equalsIgnoreCase("simple"))
 			return handleSimpleType(_json.getBlock(), _prototype, _testBuild);
 		else if (type.equalsIgnoreCase("struct"))
 			return handleStructType(_json, _prototype, _testBuild);
 		else if (type.equalsIgnoreCase("array") || type.equalsIgnoreCase("list"))
 			return handleArrayType(_json, _prototype, _testBuild);
 		else if (type.equalsIgnoreCase("date"))
 			return handleDateType(_json.getBlock(), _prototype, _testBuild);
 		else if (type.equalsIgnoreCase("null"))
 			return handleNullType(_json.getBlock(), _prototype, _testBuild);
 		else if (type.equalsIgnoreCase("any"))
 			return handleAnyType(_json.getBlock(), _prototype, _testBuild);
 		else if (customTypes.get(type) instanceof Map)
 			return handleType(_json, (Map<?, ?>) customTypes.get(type), _testBuild);
 		else
 			throw new PrototypeException("Type '" + type + "' was not recognized"); // type not recognized
 	}
 
 	private boolean handleArrayType(JSONBlock _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		if (!(_json.getBlock() instanceof List)) {
 			failMessage = " Expected " + List.class.getName() + " found " + _json.getBlock().getClass().getName();
 			return false;
 		}
 		// expected list...
 
 		List<?> jsonList = (List<?>) _json.getBlock();
 
 		// Min elements in array
 		if (_prototype.containsKey(KEY_ARRAY_MIN)) {
 			Object o = _prototype.get(KEY_ARRAY_MIN);
 			if (o != null) {
 				if (!(o instanceof Integer))
 					throw new PrototypeException("Array min must be an Integer");
 				int min = (Integer) o;
 				if (jsonList.size() < min) {
 					failMessage = " Min Array Length " + min + " Actual " + jsonList.size();
 					return false;
 				}
 			}
 		}
 
 		// Max elements in array
 		if (_prototype.containsKey(KEY_ARRAY_MAX)) {
 			Object o = _prototype.get(KEY_ARRAY_MAX);
 			if (o != null) {
 				if (!(o instanceof Integer))
 					throw new PrototypeException("Array max must be an Integer");
 				int max = (Integer) o;
 				if (jsonList.size() > max) {
 					failMessage = " Max Array Length " + max + " Actual " + jsonList.size();
 					return false;
 				}
 			}
 		}
 
 		// Get children map
 		Object childrenObject = _prototype.get(KEY_CHILDREN);
 		if (childrenObject == null)
 			throw new PrototypeException("'" + KEY_CHILDREN + "' key required for Type List");
 		if (!(childrenObject instanceof Map))
 			throw new PrototypeException("'" + KEY_CHILDREN + "' key of type: list must be a Map");
 		Map<?, ?> childrenMap = (Map<?, ?>) childrenObject;
 
 		List<Object> testBuildList = new ArrayList<Object>();
 
 		int i = 0;
 		for (Object jsonObject : jsonList) {
 			JSONBlock testBuild = new JSONBlock();
 			if (!validateBlock(new JSONBlock(jsonObject, _json), childrenMap, testBuild)) {
 				failMessage = "[" + i + "]" + failMessage;
 				return false;
 			} else {
 				testBuildList.add(testBuild.getBlock());
 			}
 			i++;
 		}
 
 		_testBuild.putBlock(testBuildList);
 
 		return true;
 	}
 
 	private boolean handleStructType(JSONBlock _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 
 		if (!(_json.getBlock() instanceof Map)) {
 			failMessage = " Expected Type " + Map.class.getName() + " Found Type " + _json.getBlock().getClass().getName();
 			return false;
 		}
 		// expected map etc...
 
 		// Get Fields array
 		Object fieldsObject = _prototype.get(KEY_FIELDS);
 
 		// If no fields Object return true
 		if (fieldsObject == null)
 			return true;
 
 		if (!(fieldsObject instanceof List) && !(fieldsObject instanceof Map))
 			throw new PrototypeException("Fields key of type: Struct must be a List or Map");
 
 		if (fieldsObject instanceof Map) {
 			Map<String, Object> testBuild = new HashMap<String, Object>();
 			if (handleFields(_json, (Map<?, ?>) fieldsObject, testBuild)) {
 				_testBuild.putBlock(testBuild);
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		List<?> fields = (List<?>) fieldsObject;
 		if (fields.isEmpty())
 			throw new PrototypeException("Cannot have empty fields array in prototype");
 
 		if (fields.size() == 1) {
 			Map<String, Object> testBuild = new HashMap<String, Object>();
 			if (handleFields(_json, (Map<?, ?>) fields.get(0), testBuild)) {
 				_testBuild.putBlock(testBuild);
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		// Check each possible fit
 		List<String> subFailures = new ArrayList<String>();
 		Iterator<?> fieldIT = fields.iterator();
 		while (fieldIT.hasNext()) {
 
 			Object fit = fieldIT.next();
 			if (fit instanceof Map) {
 				Map<String, Object> testBuild = new HashMap<String, Object>();
 				if (handleFields(_json, (Map<?, ?>) fit, testBuild)) {
 					_testBuild.putBlock(testBuild);
 					return true;
 				}
 			}
 
 			// Store fail message in the case that we have looped back to here.
 			subFailures.add(failMessage);
 			failMessage = null;
 		}
 
 		// None of the attempts worked
 		failMessage = " None of the possible field patterns validated Nexted Failures:";
 		String tab = "";
 		for (int i = 0; i < _json.getDepth(); i++)
 			tab += "-";
 		for (String subFailure : subFailures)
 			failMessage += "\n" + tab + subFailure;
 		return false;
 
 	}
 
 	private boolean handleFields(JSONBlock _json, Map<?, ?> _prototype, Map<String, Object> _testBuild) throws Exception {
 
 		Map<?, ?> jsonMap = (Map<?, ?>) _json.getBlock();
 
 		// Wildcard is indicated by a "*" field - the wildcard field will force any unspecified fields to conform to the validation given.
 		boolean containsWildCard = false;
 
 		// Cycle through all of the fields...
 		Set<?> prototypeKeys = _prototype.keySet();
 		Iterator<?> keyIT = prototypeKeys.iterator();
 		while (keyIT.hasNext()) {
 			String field = (String) keyIT.next();
 			if (field == null)
 				throw new PrototypeException("Prototype contained null key");
 
 			// If wildcard we will examine later.
 			if (field.equalsIgnoreCase(KEY_WILDCARD_FIELD)) {
 				containsWildCard = true;
 				continue;
 			}
 
 			Object o = _prototype.get(field);
 			if (!(o instanceof Map))
 				throw new PrototypeException("Fieldmap for " + field + " in prototype was not a map");
 			Map<?, ?> fieldMap = (Map<?, ?>) o;
 
 			// Get Req value
 			boolean req = false;
 			if (fieldMap.containsKey(KEY_REQ)) {
 				Object o1 = fieldMap.get(KEY_REQ);
 				if (o1 instanceof Boolean)
 					req = (Boolean) o1;
 				else if (o1 instanceof Map)
 					req = new ConditionEvaluator().evaluate((Map<?, ?>) o1, _json);
 				else
 					throw new PrototypeException(KEY_REQ + " can only be boolean or struct.");
 			} else
 				req = false;
 
 			// Get Err_On Value
 			boolean err_on;
 			if (fieldMap.containsKey(KEY_ERR_ON)) {
 				Object o2 = fieldMap.get(KEY_ERR_ON);
 				if (o2 instanceof Boolean)
 					err_on = (Boolean) o2;
 				else if (o2 instanceof Map)
 					err_on = new ConditionEvaluator().evaluate((Map<?, ?>) o2, _json);
 				else
 					throw new PrototypeException(KEY_ERR_ON + " can only be boolean or struct.");
 			} else
 				err_on = false;
 
 			// Evaluate JSON with Req & err_on...
 			boolean containsKey = jsonMap.containsKey(field);
 			if (!containsKey && req) {
 				failMessage = " Required field " + field + " was not found";
 				return false;
 			} else if (containsKey && err_on) {
 				failMessage = " Error on field " + field + " was found";
 				return false;
 			} else if (!containsKey && !req && (fieldMap.containsKey(KEY_DEFAULT) || fieldMap.containsKey(KEY_DEFAULT_ITEM))) {
 				// Enter default if field was not required and not present.
 				if (fieldMap.containsKey(KEY_DEFAULT)) {
 					_testBuild.put(field, fieldMap.get(KEY_DEFAULT));
 				} else {
 					// Default Item...
 					Object defaultItem = fieldMap.get(KEY_DEFAULT_ITEM);
 					if (defaultItem == null)
 						throw new PrototypeException(KEY_DEFAULT_ITEM + " cannot be null");
 					if (!defaultItems.containsKey(defaultItem))
 						throw new PrototypeException(KEY_DEFAULT_ITEM + " " + defaultItem.toString() + " was not found. Please add too fieldItems.");
 					_testBuild.put(field, defaultItems.get(defaultItem));
 				}
 			} else if (containsKey) {
 				// Get actual field
 				Object jsonValue = jsonMap.get(field);
 
 				JSONBlock testBuild = new JSONBlock();
 				if (!validateBlock(new JSONBlock(jsonValue, _json), fieldMap, testBuild)) {
 					failMessage = "." + field + failMessage;
 					return false;
 				} else {
 					if (isBlockAllowed(testBuild.getBlock(), fieldMap.get(KEY_STRING_REMOVE_EMPTY)))
 						_testBuild.put(field, testBuild.getBlock());
 				}
 			}
 		}
 
 		// If wildcard was present evaluate other fields, otherwise clear other fields if removeUnspecifiedkeys set
 		if (containsWildCard) {
 			Object o = _prototype.get(KEY_WILDCARD_FIELD);
 			if (!(o instanceof Map))
 				throw new PrototypeException("Fieldmap for " + KEY_WILDCARD_FIELD + " in prototype was not a map");
 			Map<?, ?> wildCardMap = (Map<?, ?>) o;
 
 			// Loop only through the fields we have not yet examined.
 			Set<?> jsonKeys = jsonMap.keySet();
 			Iterator<?> jsonIT = jsonKeys.iterator();
 			while (jsonIT.hasNext()) {
 				Object jsonField = jsonIT.next();
 				if (_prototype.containsKey(jsonField))
 					continue;
 
 				// Error on null unless we are removing unspecified keys
 				if (jsonField == null) {
					failMessage = " Null key in struct is not allowed.";
					return false;
 				}
 
 				// Get the field value.
 				Object jsonValue = jsonMap.get(jsonField);
 
 				JSONBlock testBuild = new JSONBlock();
 				if (!validateBlock(new JSONBlock(jsonValue, _json), wildCardMap, testBuild)) {
 					failMessage = "." + jsonField + failMessage;
 					return false;
 				} else {
 					if (isBlockAllowed(testBuild.getBlock(), wildCardMap.get(KEY_STRING_REMOVE_EMPTY)))
 						_testBuild.put((String) jsonField, testBuild.getBlock());
 				}
 			}
 		} else if (errorOnUnspecifiedKeys) {
 
 			// Confirm that there were no unspecified keys - this only happens if there was no wildcard
 			Set<?> jsonKeys = jsonMap.keySet();
 			Iterator<?> jsonIT = jsonKeys.iterator();
 			while (jsonIT.hasNext()) {
 				Object jsonField = jsonIT.next();
 				if (!prototypeKeys.contains(jsonField)) {
 					failMessage = " Unexpected field '" + jsonField + "' was found.";
 					return false;
 				}
 			}
 		} else if (!removeUnspecifiedKeys) {
 
 			// Add the unspecified keys
 			Set<?> jsonKeys = jsonMap.keySet();
 			Iterator<?> jsonIT = jsonKeys.iterator();
 			while (jsonIT.hasNext()) {
 				Object jsonField = jsonIT.next();
 				if (!_testBuild.containsKey(jsonField) && isBlockAllowed(jsonMap.get(jsonField), new Boolean(false)))
 					_testBuild.put((String) jsonField, jsonMap.get(jsonField));
 			}
 		}
 
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	private boolean handleStringType(Object _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 
 		List<String> enumValues = null;
 		if (_prototype.containsKey(KEY_ENUM)) {
 			Object enumObject = _prototype.get(KEY_ENUM);
 			if (!(enumObject instanceof List))
 				throw new PrototypeException("Enum list must be a list.");
 			enumValues = (List<String>) enumObject;
 		}
 
 		if (!checkNull(_json, false))
 			return false;
 
 		if (!(_json instanceof String)) {
 			failMessage = " Expected Type " + String.class.getName() + " Found Type " + _json.getClass().getName();
 			return false;
 		}
 
 		// Is empty String Allowed?
 		Object o;
 		if (_prototype.containsKey(KEY_STRING_ERR_ON_EMPTY) && (o = _prototype.get(KEY_STRING_ERR_ON_EMPTY)) instanceof Boolean) {
 			if ((Boolean) o) {
				if (((String) _json).trim().isEmpty()) {
 					failMessage = " Empty String was found but not allowed";
 					return false;
 				}
 			}
 		}
 
 		if (enumValues != null && !enumValues.contains(_json)) {
 			failMessage = " Item " + _json + " not found in enum list";
 			return false;
 		}
 
 		Object regexObject = _prototype.get(KEY_REGEX);
 		if (regexObject instanceof String) {
 			String regexString = (String) regexObject;
 			if (!((String) _json).matches(regexString)) {
 				failMessage = " Json String '" + _json + "' did not match regex pattern " + regexString;
 				return false;
 			}
 		}
 
 		_testBuild.putBlock(_json);
 
 		return true;
 	}
 
 	private boolean handleDateType(Object _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		if (!checkNull(_json, false))
 			return false;
 
 		java.util.Date date = null;
 		if (!(_json instanceof java.util.Date)) {
 			if (_json instanceof String) {
 				if (!(datePattern != null && (date = DateUtil.parseDate(_json.toString(), datePattern)) != null)) {
 					failMessage = " Failed to parse String " + _json.toString() + " as Date of pattern ( " + datePattern + " ) ";
 					return false;
 				}
 			} else {
 				failMessage = " Expected Type " + java.util.Date.class.getName() + " or " + String.class.getName() + " Found Type " + _json.getClass().getName();
 				return false;
 			}
 		} else {
 			date = (java.util.Date) _json;
 		}
 
 		// Validate before & after
 		if (_prototype.containsKey(KEY_DATE_MUST_BE_AFTER)) {
 			Object after = _prototype.get(KEY_DATE_MUST_BE_AFTER);
 			if (!(after instanceof java.util.Date) && !(after instanceof String))
 				throw new PrototypeException(KEY_DATE_MUST_BE_AFTER + " must be instance of java.util.Date or String.");
 			if (after instanceof String) {
 				after = DateUtil.parseDate(after.toString(), datePattern);
 				if (after == null)
 					throw new PrototypeException("Failed to parse prototype " + KEY_DATE_MUST_BE_AFTER + " String " + _prototype.get(KEY_DATE_MUST_BE_AFTER).toString() + " as Date of pattern ( " + datePattern + " ) ");
 			}
 			if (!((java.util.Date) after).before(date)) {
 				failMessage = " Expected Date to be after " + after.toString() + " but was " + date;
 				return false;
 			}
 		}
 		if (_prototype.containsKey(KEY_DATE_MUST_BE_BEFORE)) {
 			Object before = _prototype.get(KEY_DATE_MUST_BE_BEFORE);
 			if (!(before instanceof java.util.Date) && !(before instanceof String))
 				throw new PrototypeException(KEY_DATE_MUST_BE_BEFORE + " must be instance of java.util.Date or String.");
 			if (before instanceof String) {
 				before = DateUtil.parseDate(before.toString(), datePattern);
 				if (before == null)
 					throw new PrototypeException("Failed to parse prototype " + KEY_DATE_MUST_BE_BEFORE + " String " + _prototype.get(KEY_DATE_MUST_BE_BEFORE).toString() + " as Date of pattern ( " + datePattern + " ) ");
 			}
 			if (!((java.util.Date) before).after(date)) {
 				failMessage = " Expected Date to be before " + before.toString() + " but was " + date;
 				return false;
 			}
 		}
 
 		_testBuild.putBlock(_json);
 		return true;
 	}
 
 	private boolean handleNumberType(Object _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		if (!checkNull(_json, false))
 			return false;
 
 		if (!(_json instanceof Number)) {
 			failMessage = " Expected Type " + Number.class.getName() + " Found Type " + _json.getClass().getName();
 			return false;
 		}
 
 		if (_prototype.containsKey(KEY_NUM_MIN)) {
 			if (!(_prototype.get(KEY_NUM_MIN) instanceof Number)) {
 				throw new PrototypeException(KEY_NUM_MIN + " must be instance of Number.");
 			}
 			Number o = (Number) _prototype.get(KEY_NUM_MIN);
 			if (((Number) _json).doubleValue() < o.doubleValue()) {
 				failMessage = " Expected number to be greater than or equal to " + o.doubleValue() + " but was " + ((Number) _json).doubleValue();
 				return false;
 			}
 		}
 		if (_prototype.containsKey(KEY_NUM_MAX)) {
 			if (!(_prototype.get(KEY_NUM_MAX) instanceof Number)) {
 				throw new PrototypeException(KEY_NUM_MAX + " must be instance of Number.");
 			}
 			Number o = (Number) _prototype.get(KEY_NUM_MAX);
 			if (((Number) _json).doubleValue() > o.doubleValue()) {
 				failMessage = " Expected number to be less than or equal to " + o.doubleValue() + " but was " + ((Number) _json).doubleValue();
 				return false;
 			}
 		}
 
 		_testBuild.putBlock(_json);
 
 		return true;
 	}
 
 	private boolean handleBooleanType(Object _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		if (!checkNull(_json, false))
 			return false;
 
 		if (!(_json instanceof Boolean)) {
 			failMessage = " Expected Type " + Boolean.class.getName() + " Found Type " + _json.getClass().getName();
 			return false;
 		}
 
 		_testBuild.putBlock(_json);
 
 		return true;
 	}
 
 	private boolean handleSimpleType(Object _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		if (!checkNull(_json, false))
 			return false;
 
 		if (!(_json instanceof String) && !(_json instanceof Number) && !(_json instanceof Boolean)) {
 			failMessage = " Expected Type Simple(" + String.class.getName() + "|" + Number.class.getName() + "|" + Boolean.class.getName() + ") Found Type " + _json.getClass().getName();
 			return false;
 		}
 
 		_testBuild.putBlock(_json);
 
 		return true;
 	}
 
 	private boolean handleAnyType(Object block, Map<?, ?> _prototype, JSONBlock _testBuild) {
 		_testBuild.putBlock(block);
 		return true;
 	}
 
 	private boolean handleNullType(Object _json, Map<?, ?> _prototype, JSONBlock _testBuild) throws Exception {
 		if (_json != null) {
 			failMessage = " Expected null value Found Type " + _json.getClass().getName();
 			return false;
 		}
 
 		_testBuild.putBlock(null);
 
 		return true;
 	}
 
 	/**
 	 * Compare classes. If they are not equal and the expected class name is a package-less class then compare as java.util. and java.lang.
 	 * 
 	 * @param _classExpected
 	 * @param _classFound
 	 * @return
 	 */
 	private boolean checkClass(String _classExpected, String _classFound) {
 		if (_classFound.equals(_classExpected))
 			return true;
 		if (_classExpected.indexOf(".") == -1) {
 			if (_classFound.equals("java.util." + _classExpected) || _classFound.equals("java.lang." + _classExpected))
 				return true;
 		}
 		return false;
 	}
 
 	private boolean isBlockAllowed(Object _testBuild, Object _removeEmpty) {
 		boolean bool = true;
 		if (removeKeysWhenValueEmpty || (_removeEmpty instanceof Boolean && (Boolean) _removeEmpty)) {
			if (_testBuild instanceof String && ((String) _testBuild).trim().isEmpty() ) {
 				bool = false;
 			}
 		}
 		return bool;
 	}
 
 	private boolean checkNull(Object _json, boolean _nullAllowed) {
 		if (_json == null && !_nullAllowed) {
 			failMessage = " null value disallowed";
 			return false;
 		}
 		return true;
 	}
 
 }
