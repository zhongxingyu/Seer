 
 package edu.common.dynamicextensions.util.parser;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.CategoryHelper;
 import edu.common.dynamicextensions.util.CategoryHelperInterface;
 import edu.common.dynamicextensions.validation.category.CategoryValidator;
 
 /**
  * @author kunal_kamble
  * This class the imports the permissible values from csv file into the database.
  *
  */
 public class ImportPermissibleValues
 {
 
 	private CategoryCSVFileParser categoryCSVFileParser;
 
 	private static final String ENTITY_GROUP = "Entity_Group";
 
 	/**
 	 *
 	 * @param filePath
 	 * @throws DynamicExtensionsSystemException
 	 * @throws FileNotFoundException
 	 */
 	public ImportPermissibleValues(String filePath) throws DynamicExtensionsSystemException,
 			FileNotFoundException
 	{
 		this.categoryCSVFileParser = new CategoryCSVFileParser(filePath);
 	}
 
 	/**
 	 *
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws ParseException
 	 */
 	public void importValues() throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException, ParseException
 	{
 		CategoryHelperInterface categoryHelper = new CategoryHelper();
 
 		try
 		{
 			while (categoryCSVFileParser.readNext())
 			{
 				EntityManagerInterface entityManager = EntityManager.getInstance();
 
 				// First line in the category file is Category_Definition.
 				if (ENTITY_GROUP.equals(categoryCSVFileParser.readLine()[0]))
 				{
 					continue;
 				}
 
 				// Fetch the entity group id.
 				Long entityGroupId = entityManager.getEntityGroupId(categoryCSVFileParser
 						.getEntityGroupName());
 				CategoryValidator.checkForNullRefernce(entityGroupId, " ERROR AT LINE:"
 						+ categoryCSVFileParser.getLineNumber() + " ENTITY GROUP WITH NAME "
 						+ categoryCSVFileParser.getEntityGroupName() + " DOES NOT");
 
 				categoryCSVFileParser.getCategoryValidator().setEntityGroupId(entityGroupId);
 
 				while (categoryCSVFileParser.readNext())
 				{
 					boolean isOverridePVs = categoryCSVFileParser.isOverridePermissibleValues();
 					if (ENTITY_GROUP.equals(categoryCSVFileParser.readLine()[0]))
 					{
 						break;
 					}
 
 					String entityName = categoryCSVFileParser.getEntityName();
 					Long entityId = entityManager.getEntityId(entityName, entityGroupId);
 					CategoryValidator.checkForNullRefernce(entityId, " ERROR AT LINE:"
 							+ categoryCSVFileParser.getLineNumber() + " ENTITY WITH NAME "
 							+ entityName + " DOES NOT EXIST");
 
 					String attributeName = categoryCSVFileParser.getAttributeName();
 
 					Map<String, Collection<SemanticPropertyInterface>> pvList = categoryCSVFileParser
 							.getPermissibleValues();
 					Map<String, Collection<SemanticPropertyInterface>> finalPVs = new LinkedHashMap<String, Collection<SemanticPropertyInterface>>();
 					Long attributeId = entityManager.getAttributeId(attributeName, entityId);
 					// Bug # 10432,10382
 					// If this attribute is of type association (as in case of multi select),  
 					// it is required to fetch association's target entity's attribute id.
 					Long associationAttributeId = entityManager
 							.getAssociationAttributeId(attributeId);
 					if (associationAttributeId != null)
 					{
 						attributeId = associationAttributeId;
 					}
 
 					CategoryValidator.checkForNullRefernce(attributeId, " ERROR AT LINE:"
 							+ categoryCSVFileParser.getLineNumber() + " ATTRIBUTE WITH NAME "
 							+ attributeName + " DOES NOT EXIST");
 
 					AttributeTypeInformationInterface attrTypeInfo = entityManager
 							.getAttributeTypeInformation(attributeId);
 
 					UserDefinedDEInterface userDefinedDE = (UserDefinedDEInterface) attrTypeInfo
 							.getDataElement();
 
 					if (userDefinedDE == null)
 					{
 						userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
 						attrTypeInfo.setDataElement(userDefinedDE);
 						finalPVs = pvList;
 					}
 					else
 					{
 						if (isOverridePVs)
 						{
 							userDefinedDE.clearPermissibleValues();
 						}
 
 						List<String> presentPVs = new ArrayList<String>();
 						for (PermissibleValueInterface permissibleValue : userDefinedDE
 								.getPermissibleValueCollection())
 						{
 							presentPVs.add(permissibleValue.getValueAsObject().toString());
 						}
 
 						Set<String> pvListKeySet = pvList.keySet();
 						for (String string : pvListKeySet)
 						{
 							if (!presentPVs.contains(string))
 							{
 								finalPVs.put(string, pvList.get(string));
 							}
 						}
 					}
 
 					List<PermissibleValueInterface> permValues = categoryHelper
 							.getPermissibleValueList(attrTypeInfo, finalPVs);
 
 					userDefinedDE.addAllPermissibleValues(permValues);
 
 					entityManager.updateAttributeTypeInfo(attrTypeInfo);
 				}
 			}
 		}
 		catch (IOException e)
 		{
 			throw new DynamicExtensionsSystemException("FATAL ERROR AT LINE:"
 					+ categoryCSVFileParser.getLineNumber(), e);
 		}
 		catch (Exception e)
 		{
 			if (!(e instanceof DynamicExtensionsSystemException))
 			{
 				throw new DynamicExtensionsSystemException("FATAL ERROR AT LINE: "
 						+ categoryCSVFileParser.getLineNumber() + "READING FILE "
 						+ categoryCSVFileParser.getFilePath(), e);
 			}
 
 			throw new DynamicExtensionsSystemException("", e);
 		}
 	}
 
 	public static void main(String args[]) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		try
 		{
 			if (args.length == 0)
 			{
 				throw new Exception("Please Specify the path for .csv file");
 			}
 
 			String filePath = args[0];
 			System.out.println("---- The .csv file path is " + filePath + " ----");
 
 			ImportPermissibleValues importPermissibleValues = new ImportPermissibleValues(filePath);
 			importPermissibleValues.importValues();
 
 			System.out.println("Added permissible values successfully!!!!");
 		}
 		catch (Exception ex)
 		{
 			System.out.println("\n----------");
 			System.out.println("Exception: ");
 			System.out.println("----------");
 			throw new RuntimeException(ex);
 		}
 	}
 
 }
