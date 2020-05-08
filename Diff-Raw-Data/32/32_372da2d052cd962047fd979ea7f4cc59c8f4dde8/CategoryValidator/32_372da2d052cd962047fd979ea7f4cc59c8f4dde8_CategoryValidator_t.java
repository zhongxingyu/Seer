 
 package edu.common.dynamicextensions.validation.category;
 
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.NumericAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ListBoxInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.CategoryGenerationUtil;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.CategoryConstants;
 import edu.common.dynamicextensions.util.parser.CategoryCSVConstants;
 import edu.common.dynamicextensions.util.parser.CategoryCSVFileParser;
 import edu.common.dynamicextensions.validation.DateRangeValidator;
 import edu.common.dynamicextensions.validation.RangeValidator;
 import edu.wustl.common.util.global.ApplicationProperties;
import edu.wustl.common.util.global.CommonServiceLocator;
 import edu.wustl.dao.exception.DAOException;
 
 /**
  * @author kunal_kamble
  *
  */
 public class CategoryValidator
 {
 
 	Long entityGroupId;
 
 	CategoryCSVFileParser categoryFileParser;
 
 	StringBuffer errorMessage = new StringBuffer();
 
 	/**
 	 * @param entityGroup
 	 */
 	public CategoryValidator(CategoryCSVFileParser categoryFileParser)
 	{
 		this.categoryFileParser = categoryFileParser;
 		ApplicationProperties
 				.initBundle(CategoryCSVConstants.DYEXTN_ERROR_MESSAGES_FILE);
 	}
 
 	/**
 	 * @return
 	 */
 	public Long getEntityGroupId()
 	{
 		return entityGroupId;
 	}
 
 	/**
 	 * @param entityGroup
 	 */
 	public void setEntityGroupId(Long entityGroupId)
 	{
 		this.entityGroupId = entityGroupId;
 	}
 
 	/**
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void validateMultiplicity() throws DynamicExtensionsSystemException
 	{
 		if (categoryFileParser.readLine()[0].split(":").length < 2)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ getErrorMessageStart()
 					+ ApplicationProperties.getValue(CategoryConstants.MULT_UNDEFINED));
 		}
 	}
 
 	/**
 	 * @param entityName
 	 * @throws DynamicExtensionsSystemException
 	 * @throws ClassNotFoundException 
 	 * @throws DAOException 
 	 */
 	public void validateEntityName(String entityName) throws DynamicExtensionsSystemException,
 			DAOException, ClassNotFoundException
 	{
 		String errorMessage = getErrorMessageStart()
 				+ ApplicationProperties.getValue(CategoryConstants.NO_ENTITY) + entityName;
 
 		String entityHQL = "select id from Entity entity where entity.entityGroup.id = "
 				+ entityGroupId + " and entity.name = '" + entityName + "'";
 		List entityIdList = DynamicExtensionsUtility.executeQuery(entityHQL);
 		if(entityIdList.isEmpty())
 		{
 			throw new DynamicExtensionsSystemException(errorMessage);
 		}
 		checkForNullRefernce(entityIdList.get(0), errorMessage);
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
 	public static void checkForNullRefernce(Object object, String message)
 			throws DynamicExtensionsSystemException
 	{
 		if (object == null)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ message);
 		}
 	}
 
 	/**
 	 * This method checks whether the range specified for category attribute is
 	 * a valid subset of range specified for its attribute.
 	 * @param attribute
 	 * @param rules
 	 * @throws DynamicExtensionsSystemException
 	 * @throws ParseException
 	 * @throws DynamicExtensionsValidationException 
 	 */
 	public static void checkRangeAgainstAttributeValueRange(AttributeInterface attribute,
 			Map<String, Object> rules) throws DynamicExtensionsSystemException, ParseException
 	{
		Locale locale= CommonServiceLocator.getInstance().getDefaultLocale();
 		if (attribute == null)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ ApplicationProperties.getValue(CategoryConstants.NULL_ATTR));
 		}
 
 		if (!rules.isEmpty())
 		{
 			Map<String, Object> catMinMaxValues = null;
 
 			if (rules.containsKey(CategoryCSVConstants.DATE_RANGE))
 			{
 				catMinMaxValues = (Map<String, Object>) rules.get(CategoryCSVConstants.DATE_RANGE);
 			}
			else if (rules.containsKey(CategoryCSVConstants.RANGE.toLowerCase(locale)))
 			{
 				catMinMaxValues = (Map<String, Object>) rules.get(CategoryCSVConstants.RANGE
						.toLowerCase(locale));
 			}
 
 			if (catMinMaxValues != null && !catMinMaxValues.isEmpty())
 			{
 				validateAttributeTypeInformationMetadata(attribute);
 				validateRangeValues(attribute, catMinMaxValues);
 			}
 		}
 	}
 
 	/**
 	 * This method checks whether the range specified for category attribute is
 	 * a valid subset of range specified for its attribute.
 	 * @param attribute
 	 * @param catMinMaxValues
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private static void validateRangeValues(AttributeInterface attribute,
 			Map<String, Object> catMinMaxValues) throws DynamicExtensionsSystemException
 	{
		Locale locale= CommonServiceLocator.getInstance().getDefaultLocale();
		String minValue = (String) catMinMaxValues.get(CategoryCSVConstants.MIN.toLowerCase(locale));
		String maxValue = (String) catMinMaxValues.get(CategoryCSVConstants.MAX.toLowerCase(locale));
 
 		Map<String, String> values = new HashMap<String, String>();
 
 		Set<RuleInterface> attributeRules = new HashSet<RuleInterface>(attribute
 				.getRuleCollection());
 
 		for (RuleInterface rule : attributeRules)
 		{
 			if (rule.getName().equalsIgnoreCase(CategoryCSVConstants.RANGE)
 					|| rule.getName().equalsIgnoreCase(ProcessorConstants.DATE_RANGE))
 			{
 				Set<RuleParameterInterface> ruleParameters = new HashSet<RuleParameterInterface>(
 						rule.getRuleParameterCollection());
 
 				for (RuleParameterInterface ruleParameter : ruleParameters)
 				{
 					if (ruleParameter.getName().equalsIgnoreCase(CategoryCSVConstants.MIN))
 					{
 						String min = ruleParameter.getValue();
						values.put(CategoryCSVConstants.MIN.toLowerCase(locale), min);
 						validateRange(minValue, attribute, values);
 						values.clear();
 					}
 					if (ruleParameter.getName().equalsIgnoreCase(CategoryCSVConstants.MAX))
 					{
 						String max = ruleParameter.getValue();
						values.put(CategoryCSVConstants.MAX.toLowerCase(locale), max);
 						validateRange(maxValue, attribute, values);
 						values.clear();
 					}
 				}
 			}
 		}
 
 		validateCategoryAttributeRangeValues(minValue, maxValue, attribute);
 	}
 
 	/**
 	 * This method performs some basic validations like checking if the attribute or
 	 * the attribute type information is null. It also checks whether range is specified 
 	 * for valid attribute type information.
 	 * @param attribute
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private static void validateAttributeTypeInformationMetadata(AttributeInterface attribute)
 			throws DynamicExtensionsSystemException
 	{
 		AttributeTypeInformationInterface attrTypeInfo = attribute.getAttributeTypeInformation();
 
 		if (attrTypeInfo == null)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ ApplicationProperties.getValue(CategoryConstants.NULL_ATTR_TYPE_INFO)
 					+ attribute.getName());
 		}
 
 		if (!(attrTypeInfo instanceof NumericAttributeTypeInformation || attrTypeInfo instanceof DateAttributeTypeInformation))
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ ApplicationProperties.getValue(CategoryConstants.NON_NUM_RANGE)
 					+ attribute.getName());
 		}
 	}
 
 	/**
 	 * This method checks whether the range specified for category attribute 
 	 * is a valid subset of range specified for its attribute.
 	 * @param catAttrValue
 	 * @param attribute
 	 * @param values
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private static void validateRange(String catAttrValue, AttributeInterface attribute,
 			Map<String, String> values) throws DynamicExtensionsSystemException
 	{
 		AttributeTypeInformationInterface attrTypeInfo = attribute.getAttributeTypeInformation();
 
 		if (attrTypeInfo instanceof NumericAttributeTypeInformation)
 		{
 			RangeValidator rangeValidator = new RangeValidator();
 
 			try
 			{
 				rangeValidator.validate((AttributeMetadataInterface) attribute, catAttrValue,
 						values, attribute.getName());
 			}
 			catch (DynamicExtensionsValidationException e)
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties
 						.getValue(CategoryConstants.CREATE_CAT_FAILS)
 						+ ApplicationProperties.getValue(e.getErrorCode()) + attribute.getName(),e);
 			}
 		}
 		else
 		{
 			DateRangeValidator dateRangeValidator = new DateRangeValidator();
 
 			try
 			{
 				dateRangeValidator.validate((AttributeMetadataInterface) attribute, catAttrValue,
 						values, attribute.getName());
 			}
 			catch (DynamicExtensionsValidationException e)
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties
 						.getValue(CategoryConstants.CREATE_CAT_FAILS)
 						+ ApplicationProperties.getValue(e.getErrorCode()) + attribute.getName(),e);
 			}
 		}
 	}
 
 	/**
 	 * @param attribute
 	 * @param rules
 	 * @throws DynamicExtensionsSystemException
 	 * @throws ParseException
 	 */
 	public static void checkRequiredRule(AttributeInterface attribute, Map<String, Object> rules)
 			throws DynamicExtensionsSystemException, ParseException
 	{
 		if (attribute == null)
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ ApplicationProperties.getValue(CategoryConstants.NULL_ATTR));
 		}
 
 		for (RuleInterface rule : attribute.getRuleCollection())
 		{
 			if (rule.getName().equalsIgnoreCase(CategoryCSVConstants.REQUIRED)
 					&& (rules == null || rules.isEmpty()))
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties
 						.getValue(CategoryConstants.CREATE_CAT_FAILS)
 						+ ApplicationProperties.getValue(CategoryConstants.NO_OVERRIDE_REQ_RULE)
 						+ attribute.getName());
 			}
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private String getErrorMessageStart()
 	{
 		return ApplicationProperties.getValue(CategoryConstants.LINE_NUMBER)
 				+ categoryFileParser.getLineNumber() + " ";
 	}
 
 	/**
 	 * User should not be allowed to use root entity twice in the category creation.
 	 * @param entityName
 	 * @param mainForms
 	 * @param catEntNames
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void isRootEntityUsedTwice(String entityName, List<String> mainForms,
 			Map<String, String> categoryEntityNameInstanceMap)
 			throws DynamicExtensionsSystemException
 	{
 		if (!mainForms.contains(entityName))
 		{
 			return;
 		}
 
 		String entName = null;
 		String[] categoryEntitiesInPath = null;
 		for (String categoryEntityName : categoryEntityNameInstanceMap.keySet())
 		{
 			categoryEntitiesInPath = categoryEntityNameInstanceMap.get(categoryEntityName).split(
 					"->");
 			String newCategoryEntityName = categoryEntitiesInPath[categoryEntitiesInPath.length - 1];
 			entName = CategoryGenerationUtil.getEntityName(newCategoryEntityName);
 			if (entName.equals(entityName))
 			{
 				String errorMessage = getErrorMessageStart()
 						+ ApplicationProperties.getValue(CategoryConstants.ROOT_ENT_TWICE)
 						+ entityName;
 				throw new DynamicExtensionsSystemException(errorMessage);
 			}
 		}
 	}
 
 	/**
 	 * This method checks whether 'textArea' is the control type specified for numeric type field.  
 	 * @param controlType
 	 * @param attribute
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void isTextAreaForNumeric(String controlType, AttributeInterface attribute)
 			throws DynamicExtensionsSystemException
 	{
 		if (CategoryCSVConstants.TEXT_AREA.equals(controlType))
 		{
 			AttributeTypeInformationInterface attrTypeInfo = attribute
 					.getAttributeTypeInformation();
 			if (attrTypeInfo instanceof NumericAttributeTypeInformation)
 			{
 				throw new DynamicExtensionsSystemException(ApplicationProperties
 						.getValue(CategoryConstants.CREATE_CAT_FAILS)
 						+ ApplicationProperties.getValue(CategoryConstants.LINE_NUMBER)
 						+ categoryFileParser.getLineNumber()
 						+ ApplicationProperties.getValue(CategoryConstants.NO_TEXTAREA)
 						+ attribute.getName());
 			}
 		}
 	}
 
 	/**
 	 * This method checks if multiselect specified for a category attribute is valid.
 	 * @param entity
 	 * @param attributeName
 	 * @param control
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void checkIsMultiSelectValid(EntityInterface entity, String attributeName,
 			ControlInterface control) throws DynamicExtensionsSystemException
 	{
 		AbstractAttributeInterface abstractAttribute = entity
 				.getAbstractAttributeByName(attributeName);
 
 		if (control instanceof ListBoxInterface)
 		{
 			Boolean isMultiSelect = ((ListBoxInterface) control).getIsMultiSelect();
 
			if (isMultiSelect != null && isMultiSelect && abstractAttribute != null)
 			{
 					if (!(abstractAttribute instanceof AssociationInterface))
 					{
 						throw new DynamicExtensionsSystemException(ApplicationProperties
 								.getValue(CategoryConstants.CREATE_CAT_FAILS)
 								+ ApplicationProperties
 										.getValue(CategoryConstants.INVALID_MULTI_SELECT)
 								+ attributeName);
 					}
 					else
 					{
 						Boolean isCollection = ((AssociationInterface) abstractAttribute)
 								.getIsCollection();
						if (isCollection != null && !isCollection)
 						{
 							throw new DynamicExtensionsSystemException(ApplicationProperties
 									.getValue(CategoryConstants.CREATE_CAT_FAILS)
 									+ ApplicationProperties
 											.getValue(CategoryConstants.INVALID_MULTI_SELECT)
 									+ attributeName);
 						}
 					}
 			}
 		}
 		else
 		{
 				if (abstractAttribute instanceof AssociationInterface)
 				{
 					Boolean isCollection = ((AssociationInterface) abstractAttribute)
 							.getIsCollection();
					if (isCollection != null && isCollection)
 					{
 						throw new DynamicExtensionsSystemException(
 								ApplicationProperties.getValue(CategoryConstants.CREATE_CAT_FAILS)
 										+ ApplicationProperties
 												.getValue(CategoryConstants.INVALID_CONTROL_FOR_MULTI_SELECT)
 										+ attributeName);
 					}
 				}
 		}
 	}
 
 	/**
 	 * This method checks if the heading information has been repeated and
 	 * throws an exception if it is repeated.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void checkHeadingInfoRepeatation(String lineAfterHeading, long lineNumber)
 			throws DynamicExtensionsSystemException
 	{
 		if (lineAfterHeading.startsWith(CategoryConstants.HEADING))
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ ApplicationProperties.getValue("headingInfoRepeted") + lineNumber);
 		}
 	}
 
 	/**
 	 * This method checks if the note is appropriate.
 	 * @param note
 	 * @param lineNumber
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void checkIfNoteIsAppropriate(String note, long lineNumber)
 			throws DynamicExtensionsSystemException
 	{
 		if (note.trim().split("~").length != 2 || note.trim().split("~")[1].length() > 255
 				|| note.contains(":"))
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ ApplicationProperties.getValue("noteNotAppropriate") + lineNumber);
 		}
 	}
 
 	/**
 	 * This method checks if the heading is appropriate.
 	 * @param note
 	 * @param lineNumber
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void checkIfHeadingIsAppropriate(String heading, long lineNumber)
 			throws DynamicExtensionsSystemException
 	{
 		if (heading.split("~").length != 2 || heading.split("~")[1].length() > 255
 				|| heading.contains(":") || heading.contains(","))
 		{
 			throw new DynamicExtensionsSystemException(ApplicationProperties
 					.getValue(CategoryConstants.CREATE_CAT_FAILS)
 					+ ApplicationProperties.getValue("headingNotAppropriate") + lineNumber);
 		}
 	}
 
 	/**
 	 * @param minValue
 	 * @param maxValue
 	 * @param attribute
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private static void validateCategoryAttributeRangeValues(String minValue, String maxValue,
 			AttributeInterface attribute) throws DynamicExtensionsSystemException
 	{
 		AttributeTypeInformationInterface attrTypeInfo = attribute.getAttributeTypeInformation();
 
 		if (minValue != null && maxValue != null)
 		{
 			if (attrTypeInfo instanceof NumericAttributeTypeInformation)
 			{
 				if (Double.parseDouble(minValue) > Double.parseDouble(maxValue))
 				{
 					throw new DynamicExtensionsSystemException(ApplicationProperties
 							.getValue(CategoryConstants.CREATE_CAT_FAILS)
 							+ ApplicationProperties.getValue(CategoryConstants.INCORRECT_MINMAX)
 							+ attribute.getName());
 				}
 			}
 			else
 			{
 				String dateFormat = ((DateAttributeTypeInformation) attrTypeInfo).getFormat();
 				int result = DynamicExtensionsUtility.compareDates(minValue, maxValue, dateFormat);
 				if (result == 1 || result == -2)
 				{
 					throw new DynamicExtensionsSystemException(ApplicationProperties
 							.getValue(CategoryConstants.CREATE_CAT_FAILS)
 							+ ApplicationProperties
 									.getValue(CategoryConstants.INCORRECT_MINMAX_DATE)
 							+ attribute.getName());
 				}
 			}
 		}
 	}
 
 }
