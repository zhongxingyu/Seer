 package edu.common.dynamicextensions.util;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import edu.common.dynamicextensions.domain.PathAssociationRelationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.parser.FormulaParser;
 import edu.common.dynamicextensions.validation.ValidatorUtil;
 
 
 
 /**
  * 
  * @author rajesh_patil
  *
  */
 public class FormulaCalculator 
 {
 	/**
 	 * 
 	 */
 	private FormulaParser formulaParser = null;
 
 	/**
 	 * 
 	 */
 	public FormulaCalculator()
 	{
 		this.formulaParser = new FormulaParser();
 	}
 	/**
 	 * 
 	 */
 	public FormulaCalculator(FormulaParser formulaParser)
 	{
 		this.formulaParser = formulaParser;
 	}
 	/**
 	 * 
 	 * @param attributeValueMap
 	 * @return
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws ParseException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public Object evaluateFormula(
 			Map<BaseAbstractAttributeInterface, Object> attributeValueMap,
			CategoryAttributeInterface categoryAttributeInterface,CategoryInterface category,Integer entryNumber) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Object value = null;
 		Map<BaseAbstractAttributeInterface, Object> attributeValueMapForValidation = new HashMap <BaseAbstractAttributeInterface, Object>();
 		boolean allCalculatedAttributesPresent = true;
 		List <Object> values = new ArrayList <Object>();
 		FormulaParser formulaParser = getFormulaParser();
 		formulaParser.parseExpression(categoryAttributeInterface.getFormula().getExpression());
 		for (CategoryAttributeInterface calculatedAttribute : categoryAttributeInterface.getCalculatedCategoryAttributeCollection())
 		{
 			values.clear();
			evaluateFormulaForCalulatedAttribute(attributeValueMap,calculatedAttribute,values,entryNumber,entryNumber);
 			if (!values.isEmpty())
 			{
 				attributeValueMapForValidation.put(calculatedAttribute, values.get(0));
 			}
 			else
 			{
 				allCalculatedAttributesPresent = false;
 			}
 		}
 		List<String> errorList = new ArrayList<String>();
 		Set<Map.Entry<BaseAbstractAttributeInterface, Object>> attributeSet = attributeValueMapForValidation
 				.entrySet();
 		for (Map.Entry<BaseAbstractAttributeInterface, Object> attributeValueNode : attributeSet) 
 		{
 			errorList.addAll(ValidatorUtil.validateAttributes(
 					attributeValueNode, attributeValueNode.getKey().getName()));
 		}
 		if (allCalculatedAttributesPresent && errorList.isEmpty())
 		{
 			Set<Entry<BaseAbstractAttributeInterface, Object>> entries = attributeValueMapForValidation.entrySet();
 			for (Map.Entry<BaseAbstractAttributeInterface, Object> entry : entries)
 			{
 				CategoryAttributeInterface attribute = (CategoryAttributeInterface) entry.getKey();
 				List<PathAssociationRelationInterface> pathAssociationCollection = attribute.getCategoryEntity()
 				.getPath().getSortedPathAssociationRelationCollection();
 				PathAssociationRelationInterface pathAssociationRelationInterface = pathAssociationCollection
 						.get(pathAssociationCollection.size() - 1);
 				AttributeMetadataInterface attributeInterface = (AttributeMetadataInterface) attribute.getAbstractAttribute();
 				PermissibleValueInterface permissibleValueInterface = null;
 				try 
 				{
 					permissibleValueInterface = attributeInterface.getAttributeTypeInformation().getPermissibleValueForString(entry.getValue().toString());
 				} 
 				catch (ParseException e)
 				{
 					throw new DynamicExtensionsSystemException("ParseException",e);
 				}
 				formulaParser.setVariableValue(attribute.getCategoryEntity()
 						.getEntity().getName()
 						+ "_"
 						+ pathAssociationRelationInterface
 								.getTargetInstanceId().toString()
 						+ "_"
 						+ attributeInterface.getName(),
 						permissibleValueInterface.getValueAsObject());
 			}
 
 			value = formulaParser.evaluateExpression();
 		}
 		PermissibleValueInterface permissibleValueInterface = null;
 		try 
 		{
 			permissibleValueInterface = ((AttributeMetadataInterface) categoryAttributeInterface)
 					.getAttributeTypeInformation().getPermissibleValueForString(
 							value.toString());
 		} 
 		catch (ParseException e) 
 		{
 			throw new DynamicExtensionsSystemException("ParseException",e);
 		}
 		return permissibleValueInterface.getValueAsObject();
 	}
 
 	/**
 	 * 
 	 * @param attributeValueMap
 	 * @return
 	 * @throws ParseException 
 	 */
 	private void evaluateFormulaForCalulatedAttribute(
 			Map<BaseAbstractAttributeInterface, Object> attributeValueMap,
			CategoryAttributeInterface calculatedAttribute,List <Object> values,Integer entryNumber,Integer mapentryNumber)
 	{
 		if (!values.isEmpty())
 		{
 			return;
 		}
 		Set<Entry<BaseAbstractAttributeInterface, Object>> entries = attributeValueMap.entrySet();
 		for (Map.Entry<BaseAbstractAttributeInterface, Object> entry : entries)
 		{
 			BaseAbstractAttributeInterface attribute = entry.getKey();
 			if (attribute instanceof CategoryAttributeInterface)
 			{
 				CategoryAttributeInterface categoryAttribute = (CategoryAttributeInterface) attribute;
				if (categoryAttribute.equals(calculatedAttribute) && entryNumber.equals(mapentryNumber))
 				{
 					String value = (String) entry.getValue();
 					if (value != null && value.length() > 0)
 					{
 						values.add(value);
 						return;
 					}
 				}
 			}
 			else if (attribute instanceof CategoryAssociationInterface)
 			{
				Integer rowNumber = 0;
 				List <Map<BaseAbstractAttributeInterface, Object>> attributeValueMapList = (List<Map<BaseAbstractAttributeInterface, Object>>) entry.getValue();
 				for (Map<BaseAbstractAttributeInterface, Object> map : attributeValueMapList)
 				{
					rowNumber++;
					evaluateFormulaForCalulatedAttribute(map,calculatedAttribute,values,entryNumber,rowNumber);
 				}
 			}
 		}
 	}
 	/**
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws ParseException 
 	 * @throws DynamicExtensionsSystemException 
 	 * 
 	 */
 	public String setDefaultValueForCalculatedAttributes(CategoryAttributeInterface categoryAttribute,CategoryInterface category) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		StringBuffer message = new StringBuffer("");
 		Object value = null;
 		Map<BaseAbstractAttributeInterface, Object> attributeValueMap = new HashMap <BaseAbstractAttributeInterface, Object>();
 		boolean allCalculatedAttributesPresent = true;
 		FormulaParser formulaParser = getFormulaParser();
 		formulaParser.parseExpression(categoryAttribute.getFormula().getExpression());
 		for (CategoryAttributeInterface calculatedAttribute : categoryAttribute.getCalculatedCategoryAttributeCollection())
 		{
 			if (calculatedAttribute.getDefaultValuePermissibleValue() == null)
 			{
 				allCalculatedAttributesPresent = false;
 			}
 			else
 			{
 				attributeValueMap.put(calculatedAttribute, calculatedAttribute.getDefaultValue());
 			}
 		}
 		List<String> errorList = new ArrayList<String>();
 		Set<Map.Entry<BaseAbstractAttributeInterface, Object>> attributeSet = attributeValueMap
 				.entrySet();
 		for (Map.Entry<BaseAbstractAttributeInterface, Object> attributeValueNode : attributeSet) 
 		{
 			errorList.addAll(ValidatorUtil.validateAttributes(
 					attributeValueNode, attributeValueNode.getKey().getName()));
 		}
 
 		if (allCalculatedAttributesPresent && errorList.isEmpty())
 		{
 			Set<Entry<BaseAbstractAttributeInterface, Object>> entries = attributeValueMap.entrySet();
 			for (Map.Entry<BaseAbstractAttributeInterface, Object> entry : entries)
 			{
 				CategoryAttributeInterface attribute = (CategoryAttributeInterface) entry.getKey();
 				AttributeMetadataInterface attributeInterface = (AttributeMetadataInterface) attribute.getAbstractAttribute();
 				PermissibleValueInterface permissibleValueInterface = null;
 				try 
 				{
 					permissibleValueInterface = attributeInterface.getAttributeTypeInformation().getPermissibleValueForString(entry.getValue().toString());
 				} 
 				catch (ParseException e) 
 				{
 					throw new DynamicExtensionsSystemException("ParseException",e);
 				}
 				List<PathAssociationRelationInterface> pathAssociationCollection = attribute.getCategoryEntity()
 				.getPath().getSortedPathAssociationRelationCollection();
 				PathAssociationRelationInterface pathAssociationRelationInterface = pathAssociationCollection
 						.get(pathAssociationCollection.size() - 1);
 
 				formulaParser.setVariableValue(attribute.getCategoryEntity()
 						.getEntity().getName()
 						+ "_"
 						+ pathAssociationRelationInterface
 								.getTargetInstanceId().toString()
 						+ "_"
 						+ attribute.getAbstractAttribute().getName(),
 						permissibleValueInterface.getValueAsObject());
 			}
 			value = formulaParser.evaluateExpression();
 			PermissibleValueInterface defaultValue = null;
 			try 
 			{
 				defaultValue = ((AttributeMetadataInterface) categoryAttribute)
 						.getAttributeTypeInformation()
 						.getPermissibleValueForString(value.toString());
 			}
 			catch (ParseException e) 
 			{
 				throw new DynamicExtensionsSystemException("ParseException",e);
 			}
 			categoryAttribute.setDefaultValue(defaultValue);
 		}
 		else if (!errorList.isEmpty())
 		{
 			for (String error : errorList)
 			{
 				message.append(error);
 			}
 		}
 		return message.toString();
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public FormulaParser getFormulaParser() 
 	{
 		return formulaParser;
 	}
 	/**
 	 * 
 	 * @param formulaParser
 	 */
 	public void setFormulaParser(FormulaParser formulaParser) 
 	{
 		this.formulaParser = formulaParser;
 	}
 }
