 package net.sf.okapi.filters.yaml;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 public class TaggedFilterConfiguration {
 	private static final String COLLAPSE_WHITSPACE = "collapse_whitespace";
 	private static final String INLINE = "INLINE";
 	private static final String GROUP = "GROUP";
 	private static final String EXCLUDE = "EXCLUDE";
 	private static final String INCLUDE = "INCLUDE";
 	private static final String TEXTUNIT = "TEXTUNIT";
 	private static final String PRESERVE_WHITESPACE = "PRESERVE_WHITESPACE";
 	private static final String SCRIPT = "SCRIPT";
 	private static final String SERVER = "SERVER";
 	private static final String ATTRIBUTE_TRANS = "ATTRIBUTE_TRANS";
 	private static final String ATTRIBUTE_WRITABLE = "ATTRIBUTE_WRITABLE";
 	private static final String ATTRIBUTE_READONLY = "ATTRIBUTE_READONLY";
 	private static final String ATTRIBUTES_ONLY = "ATTRIBUTES_ONLY";
 
 	private static final String EQUALS = "EQUALS";
 	private static final String NOT_EQUALS = "NOT_EQUALS";
 	private static final String MATCH = "MATCH";
 
 	private static final String ELEMENT_TYPE = "elementType";
 
 	public static enum RULE_TYPE {
 		INLINE_ELEMENT, EXCLUDED_ELEMENT, INCLUDED_ELEMENT, GROUP_ELEMENT, TEXT_UNIT_ELEMENT, PRESERVE_WHITESPACE, SCRIPT_ELEMENT, SERVER_ELEMENT, ATTRIBUTE_TRANS, ATTRIBUTE_WRITABLE, ATTRIBUTE_READONLY, ATTRIBUTES_ONLY, UNKOWN
 	};
 
 	private YamlConfigurationReader configReader;
 
 	public TaggedFilterConfiguration(URL configurationPathAsResource) {
 		configReader = new YamlConfigurationReader(configurationPathAsResource);
 	}
 
 	public TaggedFilterConfiguration(File configurationFile) {
 		configReader = new YamlConfigurationReader(configurationFile);
 	}
 
 	public TaggedFilterConfiguration(String configurationScript) {
 		configReader = new YamlConfigurationReader(configurationScript);
 	}
 
 	@Override
 	public String toString() {
 		return configReader.toString();
 	}
 
 	public boolean collapseWhitespace() {
 		return ((Boolean) configReader.getProperty(COLLAPSE_WHITSPACE)).booleanValue();
 	}
 
 	private RULE_TYPE convertRuleAsStringToRuleType(String ruleType) {
 		if (ruleType.equals(INLINE)) {
 			return RULE_TYPE.INLINE_ELEMENT;
 		} else if (ruleType.equals(GROUP)) {
 			return RULE_TYPE.GROUP_ELEMENT;
 		} else if (ruleType.equals(EXCLUDE)) {
 			return RULE_TYPE.EXCLUDED_ELEMENT;
 		} else if (ruleType.equals(INCLUDE)) {
 			return RULE_TYPE.INCLUDED_ELEMENT;
 		} else if (ruleType.equals(TEXTUNIT)) {
 			return RULE_TYPE.TEXT_UNIT_ELEMENT;
 		} else if (ruleType.equals(PRESERVE_WHITESPACE)) {
 			return RULE_TYPE.PRESERVE_WHITESPACE;
 		} else if (ruleType.equals(SCRIPT)) {
 			return RULE_TYPE.SCRIPT_ELEMENT;
 		} else if (ruleType.equals(SERVER)) {
 			return RULE_TYPE.SERVER_ELEMENT;
 		} else if (ruleType.equals(ATTRIBUTE_TRANS)) {
 			return RULE_TYPE.ATTRIBUTE_TRANS;
 		} else if (ruleType.equals(ATTRIBUTE_WRITABLE)) {
 			return RULE_TYPE.ATTRIBUTE_WRITABLE;
 		} else if (ruleType.equals(ATTRIBUTE_READONLY)) {
 			return RULE_TYPE.ATTRIBUTE_READONLY;
 		} else if (ruleType.equals(ATTRIBUTES_ONLY)) {
 			return RULE_TYPE.ATTRIBUTES_ONLY;
 		} else {
 			return RULE_TYPE.UNKOWN;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public RULE_TYPE getMainRuleType(String ruleName) {
 		Map<String, Object> rule = configReader.getRule(ruleName);
 		if (rule == null) {
 			return RULE_TYPE.UNKOWN;
 		}
 
 		List ruleTypes = (List) rule.get("ruleTypes");
 		String ruleType = (String) ruleTypes.get(0);
 		return convertRuleAsStringToRuleType(ruleType);
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean isRuleType(String ruleName, RULE_TYPE ruleType) {
 		Map<String, Object> rule = configReader.getRule(ruleName);
 		if (rule == null) {
 			return false;
 		}
 
 		List ruleTypes = (List) rule.get("ruleTypes");
 		for (Object r : ruleTypes) {
 			String rt = (String) r;
 			if (convertRuleAsStringToRuleType(rt) == ruleType) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public String getElementType(String elementName) {
 		Map<String, Object> rule = configReader.getRule(elementName);
 		if (rule != null && rule.containsKey(ELEMENT_TYPE)) {
 			return (String) rule.get(ELEMENT_TYPE);
 		}
 
 		return elementName;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean isAttributeRule(String ruleName) {
 		Map rule = configReader.getRule(ruleName);
 		if (rule != null && getMainRuleType(ruleName) == RULE_TYPE.ATTRIBUTE_TRANS
 				|| getMainRuleType(ruleName) == RULE_TYPE.ATTRIBUTE_WRITABLE
 				|| getMainRuleType(ruleName) == RULE_TYPE.ATTRIBUTE_READONLY) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean hasActionableAttributes(String ruleName) {
 		return hasTranslatableAttributes(ruleName) || hasLocalizableAttributes(ruleName);
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean hasTranslatableAttributes(String ruleName) {
 		Map rule = configReader.getRule(ruleName);
 		if (rule != null && rule.containsKey("translatableAttributes")) {
 			return true;
 		}
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean hasLocalizableAttributes(String ruleName) {
 		Map<String, Object> rule = configReader.getRule(ruleName);
 		if (rule != null
 				&& (rule.containsKey("writableLocalizableAttributes") || rule
 						.containsKey("readOnlyLocalizableAttributes"))) {
 			return true;
 		}
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean hasReadOnlyLocalizableAttributes(String ruleName) {
 		Map<String, Object> rule = configReader.getRule(ruleName);
 		if (rule != null && rule.containsKey("readOnlyLocalizableAttributes")) {
 			return true;
 		}
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean hasWritableLocalizableAttributes(String ruleName) {
 		Map<String, Object> rule = configReader.getRule(ruleName);
 		if (rule != null && rule.containsKey("writableLocalizableAttributes")) {
 			return true;
 		}
 		return false;
	}
 
 	public boolean isTranslatableAttribute(String elementName, String attribute, Map<String, String> attributes) {
 		return isActionableAttribute("translatableAttributes", elementName, attribute, attributes)
 				|| isActionableAttribute("translatableAttributes", elementName, attribute, attributes)
 				|| isActionableAttribute("translatableAttributes", elementName, attribute, attributes);
 	}
 
 	public boolean isReadOnlyLocalizableAttribute(String elementName, String attribute, Map<String, String> attributes) {
 		return isActionableAttribute("readOnlyLocalizableAttributes", elementName, attribute, attributes);
 	}
 
 	public boolean isWritableLocalizableAttribute(String elementName, String attribute, Map<String, String> attributes) {
 		return isActionableAttribute("writableLocalizableAttributes", elementName, attribute, attributes);
 	}
 
 	@SuppressWarnings("unchecked")
 	private boolean isActionableAttribute(String type, String elementName, String attribute,
 			Map<String, String> attributes) {
 
 		Map elementRule = configReader.getRule(elementName);
 		if (elementRule == null) {
 			// catch attributes that may appear on any element
 			if (isActionableAttributeRule(elementName, attribute, type)) {
 				return true;
 			}
 			return false;
 		}
 
 		Object ta = elementRule.get(type);
 
 		if (ta instanceof List) {
 			List actionableAttributes = (List) elementRule.get(type);
 			for (Iterator<String> i = actionableAttributes.iterator(); i.hasNext();) {
 				String a = i.next();
 				if (a.equals(attribute)) {
 					return true;
 				}
 			}
 
 		} else if (ta instanceof Map) {
 			Map actionableAttributes = (Map) elementRule.get(type);
 			if (actionableAttributes.containsKey(attribute)) {
 				List condition = (List) actionableAttributes.get(attribute);
 				// case where there is no condition applied to attribute
 				if (condition == null) {
 					return true;
 				} else {
 					// apply conditions
 					if (condition.get(0) instanceof List) {
 						// We have multiple conditions - individual results are
 						// OR'ed together
 						// so only one condition need be true for the rule to
 						// apply
 						for (int i = 0; i <= condition.size() - 1; i++) {
 							List c = (List) condition.get(i);
 							if (applyConditions(c, attribute, attributes)) {
 								return true;
 							}
 						}
 						return false;
 					}
 					return applyConditions(condition, attribute, attributes);
 				}
 
 			}
 
 		}
 		
 		// catch attributes that may appear on any element
 		if (isActionableAttributeRule(elementName, attribute, type)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * @param elementName
 	 * @param type
 	 * @return
 	 */
 	private boolean isActionableAttributeRule(String elementName, String attrName, String type) {
 		if (type.equals("translatableAttributes") && getMainRuleType(attrName) == RULE_TYPE.ATTRIBUTE_TRANS) {
 			if (isListedElement(elementName, attrName, type)) {
 				return true;
 			}
 		} else if (type.equals("readOnlyLocalizableAttributes")
 				&& getMainRuleType(attrName) == RULE_TYPE.ATTRIBUTE_READONLY) {
 			if (isListedElement(elementName, attrName, type)) {
 				return true;
 			}
 		} else if (type.equals("writableLocalizableAttributes")
 				&& getMainRuleType(attrName) == RULE_TYPE.ATTRIBUTE_WRITABLE) {
 			if (isListedElement(elementName, attrName, type)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * @param elementName
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private boolean isListedElement(String elementName, String attrName, String type) {
 		Map elementRule = configReader.getRule(attrName);
 		if (elementRule == null) {
 			return false;
 		}
 
 		List excludedElements = (List) elementRule.get("allElementsExcept");
 
 		if (excludedElements == null) {
 			// means no exceptions - all tags can have this attribute/rule
 			return true;
 		}
 
 		for (int i = 0; i <= excludedElements.size() - 1; i++) {
 			String elem = (String) excludedElements.get(i);
 			if (elem.equals(elementName)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	private boolean applyConditions(List<?> condition, String attribute, Map<String, String> attributes) {
 		String conditionalAttribute = null;
 		conditionalAttribute = (String) condition.get(0);
 
 		// we didn't find the conditional test attribute - we assume no
 		// extraction
 		if (attributes.get(conditionalAttribute) == null) {
 			return false;
 		}
 
 		// '=', '!=' or regex
 		String compareType = (String) condition.get(1);
 
 		if (condition.get(2) instanceof List) {
 			List conditionValues = (List) condition.get(2);
 
 			// multiple condition values of type NOT_EQUAL are AND'ed together
 			if (compareType.equals(NOT_EQUALS)) {
 				for (Iterator<String> i = conditionValues.iterator(); i.hasNext();) {
 					String value = i.next();
 					if (applyCondition(attributes.get(conditionalAttribute), compareType, value)) {
 						return false;
 					}
 				}
 				return true;
 			} else { // multiple condition values of type EQUAL or MATCH are
 						// OR'ed together
 				for (Iterator<String> i = conditionValues.iterator(); i.hasNext();) {
 					String value = i.next();
 					if (applyCondition(attributes.get(conditionalAttribute), compareType, value)) {
 						return true;
 					}
 				}
 			}
 		}
 		// single condition
 		else if (condition.get(2) instanceof String) {
 			String conditionValue = (String) condition.get(2);
 			return applyCondition(attributes.get(conditionalAttribute), compareType, conditionValue);
 		} else {
 			throw new RuntimeException("Error reading attributes from config file");
 		}
 
 		return false;
 	}
 
 	private boolean applyCondition(String attributeValue, String compareType, String conditionValue) {
 		if (compareType.equals(EQUALS)) {
 			return attributeValue.equalsIgnoreCase(conditionValue);
 		} else if (compareType.equals(NOT_EQUALS)) {
 			return attributeValue.equalsIgnoreCase(conditionValue);
 		} else if (compareType.equals(MATCH)) {
 			boolean result = false;
 			Pattern matchPattern = Pattern.compile(conditionValue);
 			try {
 				Matcher m = matchPattern.matcher(attributeValue);
 				result = m.matches();
 			} catch (PatternSyntaxException e) {
 				throw new IllegalConditionalAttributeException(e);
 			}
 			return result;
 		} else {
 			throw new IllegalConditionalAttributeException("Unkown match type");
 		}
 	}
 }
