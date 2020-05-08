 /*===========================================================================
   Copyright (C) 2009-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.yaml;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import net.htmlparser.jericho.StartTagType;
 import net.htmlparser.jericho.Tag;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.filters.abstractmarkup.AbstractMarkupFilter;
 
 /**
  * Defines extraction rules useful for markup languages such as HTML and XML.
  * <p>
  * Extraction rules can handle the following cases:
  * <p>
  * NON EXTRACTABLE - Default rule - don't extract it.
  * <p>
  * INLINE - Elements that are included with text.
  * <p>
  * EXCLUDED -Element and children that should be excluded from extraction.
  * <p>
  * INCLUDED - Elements and children within EXLCUDED ranges that should be extracted.
  * <p>
  * GROUP - Elements that are grouped together structurally such as lists, tables etc..
  * <p>
  * ATTRIBUTES - Attributes on specific elements which should be extracted. May be translatable or localizable.
  * <p>
  * ATTRIBUTES ANY ELEMENT - Convenience rule for attributes which can occur on any element. May be translatable or
  * localizable.
  * <p>
  * TEXT UNIT - Elements whose start and end tags become part of a {@link TextUnit} rather than {@link DocumentPart}.
  * <p>
  * TEXT RUN - Elements which group together a common run of inline elements. For example, a style marker in OpenXML.
  * <p>
  * TEXT MARKER - Elements which immediately surround text.
  * <p>
  * Any of the above rules may have conditional rules based on attribute names and/or values. Conditional rules may be
  * attached to both elements and attributes. More than one conditional rules are evaluated as OR expressions. For
  * example, "type=button" OR "type=default".
  */
 @SuppressWarnings({ "unchecked", "rawtypes" })
 public class TaggedFilterConfiguration {
 	public static final String RULETYPES = "ruleTypes";
 	public static final String GLOBAL_PRESERVE_WHITESPACE = "preserve_whitespace";
 	public static final String GLOBAL_EXCLUDE_BY_DEFAULT = "exclude_by_default";
 	public static final String INLINE = "INLINE";
 	public static final String GROUP = "GROUP";
 	public static final String EXCLUDE = "EXCLUDE";
 	public static final String INCLUDE = "INCLUDE";
 	public static final String TEXTUNIT = "TEXTUNIT";
 	public static final String TEXTRUN = "TEXTRUN";
 	public static final String TEXTMARKER = "TEXTMARKER";
 	public static final String PRESERVE_WHITESPACE = "PRESERVE_WHITESPACE";
 	public static final String SCRIPT = "SCRIPT";
 	public static final String SERVER = "SERVER";
 	public static final String ATTRIBUTE_TRANS = "ATTRIBUTE_TRANS";
 	public static final String ATTRIBUTE_WRITABLE = "ATTRIBUTE_WRITABLE";
 	public static final String ATTRIBUTE_READONLY = "ATTRIBUTE_READONLY";
 	public static final String ATTRIBUTES_ONLY = "ATTRIBUTES_ONLY";
 	public static final String ATTRIBUTE_ID = "ATTRIBUTE_ID";
 	public static final String ATTRIBUTE_PRESERVE_WHITESPACE = "ATTRIBUTE_PRESERVE_WHITESPACE";
 
 	public static final String ALL_ELEMENTS_EXCEPT = "allElementsExcept";
 	public static final String ONLY_THESE_ELEMENTS = "onlyTheseElements";
 
 	public static final String EQUALS = "EQUALS";
 	public static final String NOT_EQUALS = "NOT_EQUALS";
 	public static final String MATCHES = "MATCHES";
 
 	public static final String ELEMENT_TYPE = "elementType";
 	public static final String WELLFORMED = "assumeWellformed";
 	public static final String USECODEFINDER = "useCodeFinder";
 	public static final String CODEFINDERRULES = "codeFinderRules";
 	public static final String GLOBAL_ESCAPE_NBSP = "escapeNbsp";
 	public static final String GLOBAL_PCDATA_SUBFILTER = "global_pcdata_subfilter";
 	public static final String GLOBAL_CDATA_SUBFILTER = "global_cdata_subfilter";
 	public static final String CONDITIONS = "conditions";
 	public static final String SUBFILTER = "subfilter";
 	public static final String ELEMENT_TRANSLATABLE_ATTRIBUTES = "translatableAttributes";
 	public static final String ELEMENT_WRITABLE_ATTRIBUTES = "writableLocalizableAttributes";
 	public static final String ELEMENT_READ_ONLY_ATTRIBUTES = "readOnlyLocalizableAttributes";
 	public static final String ELEMENT_ID_ATTRIBUTES = "idAttributes";
 	public static final String PRESERVE_CONDITION = "preserve";
 	public static final String DEFAULT_CONDITION = "default";
 
 	/**
 	 * {@link AbstractMarkupFilter} rule types. These rules are listed in YAML configuration files and interpreted by
 	 * the {@link TaggedFilterConfiguration} class.
 	 * 
 	 * @author HargraveJE
 	 * 
 	 */
 	public static enum RULE_TYPE {
 		/**
 		 * Tag that exists inside a text run, i.e., bold, underline etc..
 		 */
 		INLINE_ELEMENT,
 		/**
 		 * Tag that exists inside a text run, i.e., bold, underline etc.. but has been excluded based on another
 		 * conditional rule. Treat as a standalone inline code
 		 */
 		INLINE_EXCLUDED_ELEMENT,
 		/**
 		 * Tag that exists inside a text run, i.e., bold, underline etc.. but has been included based on another
 		 * conditional rule. Treat as a standalone inline code
 		 */
 		INLINE_INCLUDED_ELEMENT,
 		/**
 		 * Marks the beginning of an excluded block - all content in this block will be filtered as {@link DocumentPart}
 		 * s
 		 */
 		EXCLUDED_ELEMENT,
 		/**
 		 * Used inside EXCLUDED_ELEMENTs to mark exceptions to the excluded rule. Anything marked as INCLUDED_ELEMENT
 		 * will be filtered normally (i.e, not excluded)
 		 */
 		INCLUDED_ELEMENT,
 		/**
 		 * Marks a tag that is converted to an Okapi Group resource.
 		 */
 		GROUP_ELEMENT,
 		/**
 		 * Marks a tag that is converted to an Okapi TextUnit resource.
 		 */
 		TEXT_UNIT_ELEMENT,
 		/**
 		 * TODO: Used by the OpenXML filter (???)
 		 */
 		TEXT_RUN_ELEMENT,
 		/**
 		 * TODO: Used by the OpenXML filter (???)
 		 */
 		TEXT_MARKER_ELEMENT,
 		/**
 		 * Marks a tag that triggers a preserve whitespace rule.
 		 */
 		PRESERVE_WHITESPACE,
 		/**
 		 * Marks a tag begins or ends a web script (PHP, Perl, VBA etc..)
 		 */
 		SCRIPT_ELEMENT,
 		/**
 		 * Marks a tag that begins or ends a server side content (SSI)
 		 */
 		SERVER_ELEMENT,
 		/**
 		 * Attribute rule that defines the attribute as translatable.
 		 */
 		ATTRIBUTE_TRANS,
 		/**
 		 * Attribute rule that defines the attribute as writable (or localizable).
 		 */
 		ATTRIBUTE_WRITABLE,
 		/**
 		 * Attribute rule that defines the attribute as read-only.
 		 */
 		ATTRIBUTE_READONLY,
 		/**
 		 * Attribute rule that defines the attribute marking preserve whitespace state.
 		 */
 		ATTRIBUTE_PRESERVE_WHITESPACE,
 		/**
 		 * Element rule specifies a tag where only the attributes require processing.
 		 */
 		ATTRIBUTES_ONLY,
 		/**
 		 * Attribute rule that specifies the attribyte has an ID.
 		 */
 		ATTRIBUTE_ID,
 		/**
 		 * Rule was found but some condition of the rule failed
 		 */
 		RULE_FAILED,
 		/**
 		 * Rule was not found - default rule.
 		 */
 		RULE_NOT_FOUND
 	};
 
 	private final YamlConfigurationReader configReader;
 
 	public TaggedFilterConfiguration() {
 		configReader = new YamlConfigurationReader();
 	}
 
 	public TaggedFilterConfiguration(URL configurationPathAsResource) {
 		configReader = new YamlConfigurationReader(configurationPathAsResource);
 	}
 
 	public TaggedFilterConfiguration(File configurationFile) {
 		configReader = new YamlConfigurationReader(configurationFile);
 	}
 
 	public TaggedFilterConfiguration(String configurationScript) {
 		configReader = new YamlConfigurationReader(configurationScript);
 	}
 
 	public YamlConfigurationReader getConfigReader() {
 		return configReader;
 	}
 
 	@Override
 	public String toString() {
 		return configReader.toString();
 	}
 
 	public boolean isGlobalPreserveWhitespace() {
 		Boolean pw = (Boolean) configReader.getProperty(GLOBAL_PRESERVE_WHITESPACE);
 		if (pw == null) {
 			// default is preserve whitespace
 			return true;
 		}
 		return pw.booleanValue();
 	}
 	
 	public boolean isGlobalExcludeByDefault() {
 		Boolean pw = (Boolean) configReader.getProperty(GLOBAL_EXCLUDE_BY_DEFAULT);
 		if (pw == null) {
 			// The default is to include if no other rule is specified
 			return false;
 		}
 		return pw.booleanValue();
 	}
 
 	public boolean isWellformed() {
 		Boolean wf = (Boolean) configReader.getProperty(WELLFORMED);
 		if (wf == null) {
 			return false;
 		}
 		return wf.booleanValue();
 	}
 
 	public boolean isUseCodeFinder() {
 		Boolean useCF = (Boolean) configReader.getProperty(USECODEFINDER);
 		if ( useCF == null ) {
 			return false;
 		}
 		else {
 			return useCF.booleanValue();
 		}
 	}
 	
 	public boolean getBooleanParameter (String parameterName) {
 		Boolean res = (Boolean)configReader.getProperty(parameterName);
 		if ( res == null ) {
 			return false;
 		}
 		else {
 			return res.booleanValue();
 		}
 	}
 	
 	public String getStringParameter (String parameterName) {
 		return (String)configReader.getProperty(parameterName);
 	}
 
 	public String getGlobalPCDATASubfilter() {
 		return (String) configReader.getProperty(GLOBAL_PCDATA_SUBFILTER);
 	}
 
 	public String getGlobalCDATASubfilter() {
 		return (String) configReader.getProperty(GLOBAL_CDATA_SUBFILTER);
 	}
 
 	public String getCodeFinderRules() {
 		return (String) configReader.getProperty(CODEFINDERRULES);
 	}
 
 	private boolean isRuleType(String ruleName, RULE_TYPE ruleType, List<String> ruleTypes) {
 		for (String r : ruleTypes) {
 			if (convertRuleAsStringToRuleType(r).equals(ruleType)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isRuleType(String ruleName, RULE_TYPE ruleType) {
 		List<Map> rules = configReader.getRules(ruleName.toLowerCase());
 		for (Map rule : rules) {
 			List<String> ruleTypes = (List<String>) rule.get("ruleTypes");
 			for (String r : ruleTypes) {
 				if (convertRuleAsStringToRuleType(r).equals(ruleType)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasDefinedInlineRule(String ruleName) {
 		Map rule = configReader.getNonRegexElementRule(ruleName);
 		if (rule == null) {
 			return false;
 		}
 		
 		List<String> ruleTypes = (List<String>) rule.get("ruleTypes");
 		if (isRuleType(ruleName, RULE_TYPE.INLINE_ELEMENT, ruleTypes)) {
 			return true;
 		}
 		return false;
 	}
 
 	public String getElementType(Tag element) {
 		if (element.getTagType() == StartTagType.COMMENT) {
 			return Code.TYPE_COMMENT;
 		}
 
 		if (element.getTagType() == StartTagType.XML_PROCESSING_INSTRUCTION) {
 			return Code.TYPE_XML_PROCESSING_INSTRUCTION;
 		}
 
 		Map<String, Object> rule = configReader.getElementRule(element.getName().toLowerCase());
 		if (rule != null && rule.containsKey(ELEMENT_TYPE)) {
 			return (String) rule.get(ELEMENT_TYPE);
 		}
 
 		return element.getName();
 	}
 
 	/**
 	 * Rules are checked in priority order, first elements, then attributes, then regex
 	 * 
 	 * @param tag
 	 * @param attributes
 	 * @param attribute
 	 * @return the matching {@link RULE_TYPE}
 	 */
 	public RULE_TYPE findMatchingAttributeRule(String tag, Map<String, String> attributes,
 			String attribute) {
 		RULE_TYPE ruleType = RULE_TYPE.RULE_NOT_FOUND;
 
 		// check element rules (including regex). A match here has priority over attribute rules.
 		// We return any result as long as the element rule lists the attribute. Element
 		// rules always have priority
 		ruleType = findMatchingAttributeRuleOnElementRule(tag, attributes, attribute);
 		if (ruleType != RULE_TYPE.RULE_NOT_FOUND) {
 			return ruleType;
 		}
 
 		// check attribute rules (including regex)
 		return findMatchingElementOnAttributeRule(tag, attribute, attributes,
 				getAttributeRuleType(attribute.toLowerCase()));
 	}
 
 	private RULE_TYPE findMatchingAttributeRuleOnElementRule(String tag,
 			Map<String, String> attributes, String attribute) {
 
 		boolean attributeIsFound = false;
 
 		Map elementRule = configReader.getElementRule(tag.toLowerCase());
 		if (elementRule == null) {
 			return RULE_TYPE.RULE_NOT_FOUND;
 		}
 
 		// test for a conditional rule on this element
 // Do we want to include attributes on elements whose rules don't apply?
 //		if (!doesElementRuleConditionApply(elementRule, attributes)) {
 //			return RULE_TYPE.RULE_FAILED;
 //		}
 
 		// these attribute rules are mutually exclusive
 		String[] elementRuleNames = { ELEMENT_TRANSLATABLE_ATTRIBUTES, ELEMENT_WRITABLE_ATTRIBUTES,
 				ELEMENT_READ_ONLY_ATTRIBUTES, ELEMENT_ID_ATTRIBUTES };
 
 		for (String attributeRule : elementRuleNames) {
 			Object ta = elementRule.get(attributeRule);
 			if (ta == null) {
 				continue;
 			}
 
 			if (ta != null && ta instanceof List) {
 				List actionableAttributes = (List) elementRule.get(attributeRule);
 				for (Iterator<String> i = actionableAttributes.iterator(); i.hasNext();) {
 					String a = i.next();
 					if (a.equalsIgnoreCase(attribute)) {
 						return convertRuleAsStringToRuleType(attributeRule);
 					}
 				}
 
 			} else if (ta != null && ta instanceof Map) {
 				Map actionableAttributes = (Map) elementRule.get(attributeRule);
 				if (actionableAttributes.containsKey(attribute.toLowerCase())) {
 					attributeIsFound = true;
 					List condition = (List) actionableAttributes.get(attribute.toLowerCase());
 					// case where there is no condition applied to attribute
 					if (condition == null) {
 						return convertRuleAsStringToRuleType(attributeRule);
 					} else {
 						// apply conditions
 						if (condition.get(0) instanceof List) {
 							// We have multiple conditions - individual results are
 							// OR'ed together
 							// so only one condition need be true for the rule to
 							// apply
 							for (int i = 0; i <= condition.size() - 1; i++) {
 								List c = (List) condition.get(i);
 								if (applyConditions(c, attributes)) {
 									return convertRuleAsStringToRuleType(attributeRule);
 								}
 							}
 						} else {
 							// single condition
 							if (applyConditions(condition, attributes)) {
 								return convertRuleAsStringToRuleType(attributeRule);
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// if we found the attribute and we get to this point the condition must have failed
 		if (attributeIsFound) {
 			return RULE_TYPE.RULE_FAILED;
 		}
 
 		return RULE_TYPE.RULE_NOT_FOUND;
 	}
 
 	public RULE_TYPE getConditionalAttributeRuleType(String attribute,
 			Map<String, String> attributes) {
 		RULE_TYPE type = getAttributeRuleType(attribute);
 		if (type != RULE_TYPE.RULE_NOT_FOUND) {
 			if (doesAttributeRuleConditionApply(configReader.getAttributeRule(attribute),
 					attributes)) {
 				return type;
 			} else {
 				return RULE_TYPE.RULE_FAILED;
 			}
 		}
 		return type;
 	}
 
 	public RULE_TYPE getAttributeRuleType(String attribute) {
 		Map rule = configReader.getAttributeRule(attribute.toLowerCase());
 		if (rule != null) {
 			List<String> ruleTypes = (List<String>) rule.get("ruleTypes");
 			if (isRuleType(attribute, RULE_TYPE.ATTRIBUTE_TRANS, ruleTypes)) {
 				return RULE_TYPE.ATTRIBUTE_TRANS;
 			} else if (isRuleType(attribute, RULE_TYPE.ATTRIBUTE_WRITABLE, ruleTypes)) {
 				return RULE_TYPE.ATTRIBUTE_WRITABLE;
 			} else if (isRuleType(attribute, RULE_TYPE.ATTRIBUTE_READONLY, ruleTypes)) {
 				return RULE_TYPE.ATTRIBUTE_READONLY;
 			} else if (isRuleType(attribute, RULE_TYPE.ATTRIBUTE_ID, ruleTypes)) {
 				return RULE_TYPE.ATTRIBUTE_ID;
 			} else if (isRuleType(attribute, RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE, ruleTypes)) {
 				return RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE;
 			}
 		}
 
 		return RULE_TYPE.RULE_NOT_FOUND;
 	}
 
 	/**
 	 * @param elementName
 	 * @return
 	 */
 	private RULE_TYPE findMatchingElementOnAttributeRule(String tag, String attribute,
 			Map<String, String> attributes, RULE_TYPE ruleType) {
 		List excludedElements;
 		List onlyTheseElements;
 		Map attrRule = configReader.getAttributeRule(attribute.toLowerCase());
 
 		if (attrRule == null) {
 			return RULE_TYPE.RULE_NOT_FOUND;
 		}
 
 		// test for a conditional rule on this attribute
 		if (!doesAttributeRuleConditionApply(attrRule, attributes)) {
 			return RULE_TYPE.RULE_FAILED;
 		}
 
 		excludedElements = (List) attrRule.get(ALL_ELEMENTS_EXCEPT);
 		onlyTheseElements = (List) attrRule.get(ONLY_THESE_ELEMENTS);
 
 		if (excludedElements == null && onlyTheseElements == null) {
 			// means no exceptions - all tags can have this attribute/rule
 			return ruleType;
 		}
 
 		// ALL_ELEMENTS_EXCEPT and ONLY_THESE_ELEMENTS are mutually exclusive
 		// categories, either one or the other must be true , not both
 		if (excludedElements != null) {
 			for (int i = 0; i <= excludedElements.size() - 1; i++) {
 				String elem = (String) excludedElements.get(i);
 				if (elem.equalsIgnoreCase(tag)) {
 					return RULE_TYPE.RULE_FAILED;
 				}
 			}
 			// default
 			return ruleType;
 		} else if (onlyTheseElements != null) {
 			for (int i = 0; i <= onlyTheseElements.size() - 1; i++) {
 				String elem = (String) onlyTheseElements.get(i);
 				if (elem.equalsIgnoreCase(tag)) {
 					return ruleType;
 				}
 			}
 			// default
 			return RULE_TYPE.RULE_FAILED;
 		}
 		return ruleType;
 	}
 
 	public RULE_TYPE getConditionalElementRuleType(String tag, Map<String, String> attributes) {
 		RULE_TYPE type = getElementRuleTypeCandidate(tag.toLowerCase());		
 		
 		if (type != RULE_TYPE.RULE_NOT_FOUND) {				
 			// make sure this is really an INLINE_EXCLUDED_ELEMENT rule - condition must apply
 			if (type == RULE_TYPE.INLINE_EXCLUDED_ELEMENT) {
				Map rule = configReader.getRegexElementRule(tag.toLowerCase());
				if (rule != null && doesElementRuleConditionApply(rule, attributes)) {
					return type;
				} else if (hasDefinedInlineRule(tag.toLowerCase())) {
 					if (doesElementRuleConditionApply(configReader.getElementRule(tag.toLowerCase()), attributes)) {
 						return RULE_TYPE.INLINE_ELEMENT;
 					} else {
 						return RULE_TYPE.RULE_NOT_FOUND;
 					}						
 				} else {
 					return RULE_TYPE.RULE_NOT_FOUND;
 				}
 			}
 			
 			// short cut test - if the rule depends on a condition and there are no attributes skip
 			if (attributes.isEmpty() && 
 					configReader.getElementRule(tag.toLowerCase()).get(CONDITIONS) != null)  {
 				return RULE_TYPE.RULE_NOT_FOUND;
 			}
 						
 			// short cut test - if there are no conditions and no attributes the default rule applies
 			if (attributes.isEmpty() && 
 					configReader.getElementRule(tag.toLowerCase()).get(CONDITIONS) == null)  {
 				return type;
 			}
 			
 			// if we get this far just apply the condition and return the result
 			if (doesElementRuleConditionApply(configReader.getElementRule(tag.toLowerCase()), attributes)) {
 				return type;
 			} else {
 				return RULE_TYPE.RULE_FAILED;
 			}
 		}
 		return type;
 	}
 
 	public RULE_TYPE getElementRuleTypeCandidate(String tag) {
 		Map rule = configReader.getElementRule(tag.toLowerCase());
 		if (rule != null) {
 			List<String> ruleTypes = (List<String>) rule.get("ruleTypes");
 			// ORDER is important!!! These are matched in priority order
 			if (isRuleType(tag, RULE_TYPE.EXCLUDED_ELEMENT, ruleTypes) && 
 					!isRuleType(tag, RULE_TYPE.INLINE_ELEMENT, ruleTypes)) {
 				return RULE_TYPE.EXCLUDED_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.GROUP_ELEMENT, ruleTypes)) {
 				return RULE_TYPE.GROUP_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.INCLUDED_ELEMENT, ruleTypes)) {
 				return RULE_TYPE.INCLUDED_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.INLINE_ELEMENT, ruleTypes)) {
 				// handle case where inline is excluded by a more general rule
 				Map r1 = configReader.getRegexElementRule(tag.toLowerCase());
 				Map r2 = configReader.getNonRegexElementRule(tag.toLowerCase());
 				// test regex rule first
 				if (r1 != null) {
 					List<String> rt = (List<String>) r1.get("ruleTypes");
 					if (isRuleType(tag, RULE_TYPE.EXCLUDED_ELEMENT, rt)) {
 						return RULE_TYPE.INLINE_EXCLUDED_ELEMENT;
 					}
 				} else if (r2 != null) {
 					List<String> rt = (List<String>) r2.get("ruleTypes");
 					if (isRuleType(tag, RULE_TYPE.EXCLUDED_ELEMENT, rt)) {
 						return RULE_TYPE.INLINE_EXCLUDED_ELEMENT;
 					}
 				}
 				return RULE_TYPE.INLINE_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.SCRIPT_ELEMENT, ruleTypes)) {
 				return RULE_TYPE.SCRIPT_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.SERVER_ELEMENT, ruleTypes)) {
 				return RULE_TYPE.SERVER_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.ATTRIBUTES_ONLY, ruleTypes)) {
 				return RULE_TYPE.ATTRIBUTES_ONLY;
 			} else if (isRuleType(tag, RULE_TYPE.TEXT_UNIT_ELEMENT, ruleTypes)) {
 				return RULE_TYPE.TEXT_UNIT_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.TEXT_MARKER_ELEMENT, ruleTypes)) {
 				return RULE_TYPE.TEXT_MARKER_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.TEXT_RUN_ELEMENT, ruleTypes)) {
 				return RULE_TYPE.TEXT_RUN_ELEMENT;
 			} else if (isRuleType(tag, RULE_TYPE.PRESERVE_WHITESPACE, ruleTypes)) {
 				return RULE_TYPE.PRESERVE_WHITESPACE;
 			}
 		}
 
 		return RULE_TYPE.RULE_NOT_FOUND;
 	}
 
 	public boolean isTranslatableAttribute(String tag, String attribute,
 			Map<String, String> attributes) {
 		return findMatchingAttributeRule(tag, attributes, attribute) == RULE_TYPE.ATTRIBUTE_TRANS;
 	}
 
 	public boolean isReadOnlyLocalizableAttribute(String tag, String attribute,
 			Map<String, String> attributes) {
 		return findMatchingAttributeRule(tag, attributes, attribute) == RULE_TYPE.ATTRIBUTE_READONLY;
 	}
 
 	public boolean isWritableLocalizableAttribute(String tag, String attribute,
 			Map<String, String> attributes) {
 		return findMatchingAttributeRule(tag, attributes, attribute) == RULE_TYPE.ATTRIBUTE_WRITABLE;
 	}
 
 	public boolean isIdAttribute(String tag, String attribute, Map<String, String> attributes) {
 		return findMatchingAttributeRule(tag, attributes, attribute) == RULE_TYPE.ATTRIBUTE_ID;
 	}
 
 	public RULE_TYPE convertRuleAsStringToRuleType(String ruleType) {
 		if (ruleType.equalsIgnoreCase(INLINE)) {
 			return RULE_TYPE.INLINE_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(GROUP)) {
 			return RULE_TYPE.GROUP_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(EXCLUDE)) {
 			return RULE_TYPE.EXCLUDED_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(INCLUDE)) {
 			return RULE_TYPE.INCLUDED_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(TEXTUNIT)) {
 			return RULE_TYPE.TEXT_UNIT_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(TEXTRUN)) {
 			return RULE_TYPE.TEXT_RUN_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(TEXTMARKER)) {
 			return RULE_TYPE.TEXT_MARKER_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(PRESERVE_WHITESPACE)) {
 			return RULE_TYPE.PRESERVE_WHITESPACE;
 		} else if (ruleType.equalsIgnoreCase(SCRIPT)) {
 			return RULE_TYPE.SCRIPT_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(SERVER)) {
 			return RULE_TYPE.SERVER_ELEMENT;
 		} else if (ruleType.equalsIgnoreCase(ATTRIBUTE_TRANS)) {
 			return RULE_TYPE.ATTRIBUTE_TRANS;
 		} else if (ruleType.equalsIgnoreCase(ATTRIBUTE_WRITABLE)) {
 			return RULE_TYPE.ATTRIBUTE_WRITABLE;
 		} else if (ruleType.equalsIgnoreCase(ATTRIBUTE_READONLY)) {
 			return RULE_TYPE.ATTRIBUTE_READONLY;
 		} else if (ruleType.equalsIgnoreCase(ATTRIBUTES_ONLY)) {
 			return RULE_TYPE.ATTRIBUTES_ONLY;
 		} else if (ruleType.equalsIgnoreCase(ATTRIBUTE_ID)) {
 			return RULE_TYPE.ATTRIBUTE_ID;
 		} else if (ruleType.equalsIgnoreCase(ATTRIBUTE_PRESERVE_WHITESPACE)) {
 			return RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE;
 		} else if (ruleType.equalsIgnoreCase(ELEMENT_TRANSLATABLE_ATTRIBUTES)) {
 			return RULE_TYPE.ATTRIBUTE_TRANS;
 		} else if (ruleType.equalsIgnoreCase(ELEMENT_WRITABLE_ATTRIBUTES)) {
 			return RULE_TYPE.ATTRIBUTE_WRITABLE;
 		} else if (ruleType.equalsIgnoreCase(ELEMENT_READ_ONLY_ATTRIBUTES)) {
 			return RULE_TYPE.ATTRIBUTE_READONLY;
 		} else if (ruleType.equalsIgnoreCase(ELEMENT_ID_ATTRIBUTES)) {
 			return RULE_TYPE.ATTRIBUTE_ID;
 		} else {
 			return RULE_TYPE.RULE_NOT_FOUND;
 		}
 	}
 
 	private boolean applyConditions(List condition, Map<String, String> attributes) {
 		String conditionalAttribute = null;
 		conditionalAttribute = (String) condition.get(0);
 
 //		// we didn't find the conditional test attribute - we assume no
 //		// extraction
 //		if (attributes.get(conditionalAttribute.toLowerCase()) == null) {
 //			return false;
 //		}
 
 		// '=', '!=' or regex
 		String compareType = (String) condition.get(1);
 
 		if (condition.get(2) instanceof List) {
 			List conditionValues = (List) condition.get(2);
 
 			// multiple condition values of type NOT_EQUAL are AND'ed together
 			if (compareType.equalsIgnoreCase(NOT_EQUALS)) {
 				for (Iterator<String> i = conditionValues.iterator(); i.hasNext();) {
 					String value = i.next();
 					if (!applyCondition(attributes.get(conditionalAttribute.toLowerCase()),
 							compareType, value)) {
 						return false;
 					}
 				}
 				return true;
 			} else { // multiple condition values of type EQUAL or MATCH are
 				// OR'ed together
 				for (Iterator<String> i = conditionValues.iterator(); i.hasNext();) {
 					String value = i.next();
 					if (applyCondition(attributes.get(conditionalAttribute.toLowerCase()),
 							compareType, value)) {
 						return true;
 					}
 				}
 			}
 		}
 		// single condition
 		else if (condition.get(2) instanceof String) {
 			String conditionValue = (String) condition.get(2);
 			return applyCondition(attributes.get(conditionalAttribute.toLowerCase()), compareType,
 					conditionValue);
 		} else {
 			throw new RuntimeException("Error reading conditions. "
 					+ "Have you quoted values such as 'true', 'false', 'yes', and 'no'?");
 		}
 
 		return false;
 	}
 
 	private boolean applyCondition(String attributeValue, String compareType, String conditionValue) {
 		if (compareType.equalsIgnoreCase(EQUALS)) {
 			return conditionValue.equalsIgnoreCase(attributeValue);
 		} else if (compareType.equalsIgnoreCase(NOT_EQUALS)) {
 			return !conditionValue.equalsIgnoreCase(attributeValue);
 		} else if (compareType.equalsIgnoreCase(MATCHES)) {
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
 
 	private boolean doesElementRuleConditionApply(Map elementRule, Map<String, String> attributes) {
 		List conditions = (List) elementRule.get(CONDITIONS);
 		if (conditions != null) {
 			return applyConditions(conditions, attributes);
 		}
 
 		return true;
 	}
 
 	private boolean doesAttributeRuleConditionApply(Map attributeRule,
 			Map<String, String> attributes) {
 		if (attributeRule == null) {
 			return false;
 		}
 		List conditions = (List) attributeRule.get(CONDITIONS);
 		if (conditions != null) {
 			return applyConditions(conditions, attributes);
 		}
 
 		return true;
 	}
 
 	public boolean isPreserveWhitespaceCondition(String attribute, Map<String, String> attributes) {
 		Map attributeRule = configReader.getAttributeRule(attribute);
 		if (doesAttributeRuleConditionApply(attributeRule, attributes)) {
 			List preserveWhiteSpace = (List) attributeRule.get(PRESERVE_CONDITION);
 			if (preserveWhiteSpace != null) {
 				return applyConditions(preserveWhiteSpace, attributes);
 			}
 		}
 		return false;
 	}
 
 	public boolean isDefaultWhitespaceCondition(String attribute, Map<String, String> attributes) {
 		Map attributeRule = configReader.getAttributeRule(attribute);
 		if (doesAttributeRuleConditionApply(attributeRule, attributes)) {
 			List defaultWhiteSpace = (List) attributeRule.get(DEFAULT_CONDITION);
 			if (defaultWhiteSpace != null) {
 				return applyConditions(defaultWhiteSpace, attributes);
 			}
 		}
 		return false;
 	}
 
 	public Map<String, Object> getAttributeRules() {
 		return configReader.getAttributeRules();
 	}
 
 	public Map<String, Object> getElementRules() {
 		return configReader.getElementRules();
 	}
 }
