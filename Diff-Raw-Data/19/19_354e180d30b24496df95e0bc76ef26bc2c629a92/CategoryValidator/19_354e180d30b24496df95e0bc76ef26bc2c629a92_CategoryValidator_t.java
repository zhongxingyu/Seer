 
 package edu.common.dynamicextensions.validation.category;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FloatAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.IntegerAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ShortAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.CategoryGenerationUtil;
 import edu.common.dynamicextensions.util.parser.CategoryCSVConstants;
 import edu.common.dynamicextensions.util.parser.CategoryCSVFileParser;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * @author kunal_kamble
  *
  */
 public class CategoryValidator
 {
 	EntityGroupInterface entityGroup;
 
 	CategoryCSVFileParser categoryFileParser;
 
 	StringBuffer errorMessage = new StringBuffer();
 
 	/**
 	 * @param entityGroup
 	 */
 	public CategoryValidator(CategoryCSVFileParser categoryFileParser)
 	{
 		this.categoryFileParser = categoryFileParser;
 		ApplicationProperties.initBundle(CategoryCSVConstants.DYNAMIC_EXTENSIONS_ERROR_MESSAGES_FILE);
 	}
 
 	/**
 	 * @return
 	 */
 	public EntityGroupInterface getEntityGroup()
 	{
 		return entityGroup;
 	}
 
 	/**
 	 * @param entityGroup
 	 */
 	public void setEntityGroup(EntityGroupInterface entityGroup)
 	{
 		this.entityGroup = entityGroup;
 	}
 
 	/**
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void validateMultiplicity() throws DynamicExtensionsSystemException
 	{
 		if (categoryFileParser.readLine()[0].split(":").length < 2)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure") + getErrorMessageStart()
 					+ ApplicationProperties.getValue("multiplicityUndefined"));
 		}
 	}
 
 	/**
 	 * @param entityName
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void validateEntityName(String entityName) throws DynamicExtensionsSystemException
 	{
 		String errorMessage = getErrorMessageStart() + ApplicationProperties.getValue("entityDoesNotExist") + entityName;
 		checkForNullRefernce(entityGroup.getEntityByName(entityName), errorMessage);
 	}
 
 	/**
 	 * 
 	 */
 	public void validateSubcategoryTag()
 	{
 		categoryFileParser.getTargetContainerCaption();
 	}
 
 	/**
 	 * @param object
 	 * @param message
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void checkForNullRefernce(Object object, String message) throws DynamicExtensionsSystemException
 	{
 		if (object == null)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure") + message);
 		}
 	}
 
 	/**
 	 * @param attribute
 	 * @param rulesMap
 	 * @throws DynamicExtensionsSystemException
 	 * @throws ParseException
 	 */
 	public static void checkRangeAgainstAttributeValueRange(AttributeInterface attribute, Map<String, Object> rulesMap)
 			throws DynamicExtensionsSystemException, ParseException
 	{
 		if (attribute == null)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 					+ ApplicationProperties.getValue("nullAttribute"));
 		}
 
 		if (!rulesMap.isEmpty())
 		{
 			Map<String, Object> categoryMinMaxValuesMap = (Map<String, Object>) rulesMap.get(CategoryCSVConstants.RANGE.toLowerCase());
 
 			if (categoryMinMaxValuesMap != null && !categoryMinMaxValuesMap.isEmpty())
 			{
 				String minValue = (String) categoryMinMaxValuesMap.get(CategoryCSVConstants.MIN.toLowerCase());
 				String maxValue = (String) categoryMinMaxValuesMap.get(CategoryCSVConstants.MAX.toLowerCase());
 
 				Set<RuleInterface> rules = new HashSet<RuleInterface>(attribute.getRuleCollection());
 				String min = "";
 				String max = "";
 
 				for (RuleInterface rule : rules)
 				{
 					if (rule.getName().equalsIgnoreCase(CategoryCSVConstants.RANGE) || rule.getName().equalsIgnoreCase(ProcessorConstants.DATE_RANGE))
 					{
 						Set<RuleParameterInterface> ruleParameters = new HashSet<RuleParameterInterface>(rule.getRuleParameterCollection());
 
 						for (RuleParameterInterface ruleParameter : ruleParameters)
 						{
 							if (ruleParameter.getName().equalsIgnoreCase(CategoryCSVConstants.MIN))
 							{
 								min = ruleParameter.getValue();
 							}
 							if (ruleParameter.getName().equalsIgnoreCase(CategoryCSVConstants.MAX))
 							{
 								max = ruleParameter.getValue();
 							}
 						}
 					}
 				}
 
 				if (min.length() != 0 && max.length() != 0)
 				{
 					validateRange(attribute.getAttributeTypeInformation(), minValue, maxValue, min, max);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param attribute
 	 * @param rulesMap
 	 * @throws DynamicExtensionsSystemException
 	 * @throws ParseException
 	 */
 	public static void checkRequiredRule(AttributeInterface attribute, Map<String, Object> rulesMap) throws DynamicExtensionsSystemException,
 			ParseException
 	{
 		if (attribute == null)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 					+ ApplicationProperties.getValue("nullAttribute"));
 		}
 
 		for (RuleInterface rule : attribute.getRuleCollection())
 		{
 			if (rule.getName().equalsIgnoreCase(CategoryCSVConstants.REQUIRED) && (rulesMap == null || rulesMap.isEmpty()))
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 						+ ApplicationProperties.getValue("cannotOverrideRequiredRule") + attribute.getName());
 			}
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private String getErrorMessageStart()
 	{
 		return ApplicationProperties.getValue("lineNumber") + categoryFileParser.getLineNumber() + " ";
 	}
 
 	/**
 	 * @param attributeTypeInformation
 	 * @param minValue
 	 * @param maxValue
 	 * @param min
 	 * @param max
 	 * @throws DynamicExtensionsSystemException
 	 * @throws ParseException
 	 */
 	private static void validateRange(AttributeTypeInformationInterface attributeTypeInformation, String minValue, String maxValue, String min,
 			String max) throws DynamicExtensionsSystemException, ParseException
 	{
 		if (attributeTypeInformation instanceof IntegerAttributeTypeInformation || attributeTypeInformation instanceof StringAttributeTypeInformation)
 		{
 			if (Integer.parseInt(minValue) < Integer.parseInt(min) || Integer.parseInt(maxValue) > Integer.parseInt(max))
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 						+ ApplicationProperties.getValue("numberRangeError"));
 			}
 		}
 		else if (attributeTypeInformation instanceof FloatAttributeTypeInformation)
 		{
 			if (Float.parseFloat(minValue) < Float.parseFloat(min) || Float.parseFloat(maxValue) > Float.parseFloat(max))
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 						+ ApplicationProperties.getValue("numberRangeError"));
 			}
 		}
 		else if (attributeTypeInformation instanceof ShortAttributeTypeInformation)
 		{
 			if (Short.parseShort(minValue) < Short.parseShort(min) || Short.parseShort(maxValue) > Short.parseShort(max))
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 						+ ApplicationProperties.getValue("numberRangeError"));
 			}
 		}
 		else if (attributeTypeInformation instanceof DoubleAttributeTypeInformation)
 		{
 			if (Double.parseDouble(minValue) < Double.parseDouble(min) || Double.parseDouble(maxValue) > Double.parseDouble(max))
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 						+ ApplicationProperties.getValue("numberRangeError"));
 			}
 		}
 		else if (attributeTypeInformation instanceof LongAttributeTypeInformation)
 		{
 			if (Long.parseLong(minValue) < Long.parseLong(min) || Long.parseLong(maxValue) > Long.parseLong(max))
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 						+ ApplicationProperties.getValue("numberRangeError"));
 			}
 		}
 		else if (attributeTypeInformation instanceof DateAttributeTypeInformation)
 		{
 			SimpleDateFormat sf = new SimpleDateFormat(ProcessorConstants.SQL_DATE_ONLY_FORMAT);
 			sf.setLenient(false);
 			if (sf.parse(minValue).before(sf.parse(min)) || sf.parse(maxValue).after(sf.parse(max)))
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
 						+ ApplicationProperties.getValue("dateRangeError"));
 			}
 		}
 	}
 
 	/**
 	 * User should not be allowed to use root entity twice in the category creation
 	 * @param entityName
 	 * @param mainFormList
 	 * @param categoryEntityName
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void isRootEntityUsedTwice(String entityName, List<String> mainFormList, Collection<String> categoryEntityName)
 			throws DynamicExtensionsSystemException
 	{
 		if (!mainFormList.contains(entityName))
 		{
 			return;
 		}
 		String seperatedEntityName = null;
 		for (String categoriesCreated : categoryEntityName)
 		{
 			seperatedEntityName = CategoryGenerationUtil.getEntityName(categoriesCreated);
 			if (seperatedEntityName.equals(entityName))
 			{
 				String errorMessage = getErrorMessageStart() + ApplicationProperties.getValue("rootEntityUsedTwice") + entityName;
 				throw new DynamicExtensionsSystemException(errorMessage);
 			}
 		}
 	}
 
 	/**
 	 * This method checks for Numeric type field cannot have textarea control
 	 * @param controlType
 	 * @param attribute
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void isTextAreaForNumeric(String controlType, AttributeInterface attribute) throws DynamicExtensionsSystemException
 	{
		if (controlType.equals("textArea"))
 		{
			AttributeTypeInformationInterface atrinfo = attribute.getAttributeTypeInformation();
			if (atrinfo instanceof IntegerAttributeTypeInformation || atrinfo instanceof LongAttributeTypeInformation
					|| atrinfo instanceof DoubleAttributeTypeInformation || atrinfo instanceof ShortAttributeTypeInformation
					|| atrinfo instanceof FloatAttributeTypeInformation)
			{
 
				throw new DynamicExtensionsSystemException(ApplicationProperties.getValue("categoryCreationFailure")
						+ ApplicationProperties.getValue("lineNumber") + categoryFileParser.getLineNumber()
						+ ApplicationProperties.getValue("incorrectControlTypeTextArea") + attribute.getName());
			}
 		}
 	}
 
 }
