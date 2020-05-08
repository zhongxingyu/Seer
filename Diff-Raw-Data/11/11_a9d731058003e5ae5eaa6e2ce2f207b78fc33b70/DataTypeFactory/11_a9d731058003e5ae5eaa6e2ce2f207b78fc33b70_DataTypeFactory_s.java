 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 
 import edu.common.dynamicextensions.dao.impl.DynamicExtensionDAO;
 import edu.common.dynamicextensions.dao.impl.DynamicExtensionDBFactory;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
 import edu.wustl.dao.daofactory.DAOConfigFactory;
 
 /**
  * This is a Singleton class which parses the XML file that consists of mapping between PrimitiveAttribute 
  * and Database DataTypes and generates the Map of the same. 
  * @author chetan_patil
  */
 /**
  * @author mandar_shidhore
  *
  */
 public class DataTypeFactory
 {
 
 	/**
 	 * 
 	 */
 	private static DataTypeFactory dataTypeFactory = null;
 
 	/**
 	 * 
 	 */
 	private Map<String, Object> dataTypeMap;
 
 	/**
 	 * Empty constructor
 	 */
 	protected DataTypeFactory()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * This method returns the instance of DataTypeFactory
 	 * @return DataTypeFactory instance
 	 * @throws DataTypeFactoryInitializationException on Exception
 	 */
 	public static synchronized DataTypeFactory getInstance()
 			throws DataTypeFactoryInitializationException
 	{
 		if (dataTypeFactory == null)
 		{
 			String appName=DynamicExtensionDAO.getInstance().getAppName();
 			String dbType = DAOConfigFactory.getInstance().getDAOFactory(appName).getDataBaseType();				
 			String dataTypeMappingFileName=DynamicExtensionDBFactory.getInstance().getDataTypeMappingFile(dbType);			
 			dataTypeFactory = new DataTypeFactory();
 			dataTypeFactory.populateDataTypeMap(dataTypeMappingFileName);
 		}
 		return dataTypeFactory;
 	}
 
 	/**
 	 * This method updates module map by parsing xml file
 	 * @param xmlFileName file to be parsed
 	 * @return dataType Map
 	 * @throws DataTypeFactoryInitializationException on Exception
 	 */
 	public final Map<String, Object> populateDataTypeMap(String xmlFileName)
 			throws DataTypeFactoryInitializationException
 	{
 		dataTypeMap = new HashMap<String, Object>();
 
 		SAXReader saxReader = new SAXReader();
 		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(xmlFileName);
 
 		Document document = null;
 
 		try
 		{
 			document = saxReader.read(inputStream);
 			Element name = null;
 			Element databaseDataType = null;
 			Element digitsBeforeDecimal = null;
 			Element digitsAfterDecimal = null;
 
 			Element primitiveAttributesElement = document.getRootElement();
 			Iterator primitiveAttrElementIterator = primitiveAttributesElement
 					.elementIterator("Primitive-Attribute");
 
 			Element primitiveAttributeElement = null;
 
 			while (primitiveAttrElementIterator.hasNext())
 			{
 				primitiveAttributeElement = (Element) primitiveAttrElementIterator.next();
 
 				name = primitiveAttributeElement.element("name");
 				databaseDataType = primitiveAttributeElement.element("database-datatype");
 				digitsBeforeDecimal = primitiveAttributeElement.element("digits-before-decimal");
 				digitsAfterDecimal = primitiveAttributeElement.element("digits-after-decimal");
 
 				DataTypeInformation dataTypeInfo = new DataTypeInformation();
 				dataTypeInfo.setName(name.getStringValue());
 				dataTypeInfo.setDatabaseDataType(databaseDataType.getStringValue());
 
 				if (digitsBeforeDecimal != null && digitsAfterDecimal != null)
 				{
 					dataTypeInfo.setDigitsBeforeDecimal(digitsBeforeDecimal.getStringValue());
 					dataTypeInfo.setDigitsAfterDecimal(digitsAfterDecimal.getStringValue());
 				}
 
 				dataTypeMap.put(name.getStringValue(), dataTypeInfo);
 			}
 		}
 		catch (DocumentException documentException)
 		{
 			throw new DataTypeFactoryInitializationException(documentException);
 		}
 
 		return dataTypeMap;
 	}
 
 	/**
 	 * This method returns the name of the Database Data type given the name
 	 * of the corresponding Primitive attribute.
 	 * @param primitiveAttribute The name of the primitive attribute
 	 * @return String The name of Database data type
 	 * @throws DataTypeFactoryInitializationException If dataTypeMap is not populated
 	 */
 	public String getDatabaseDataType(String primitiveAttribute)
 			throws DataTypeFactoryInitializationException
 	{
 		String databaseDataType = null;
		if (dataTypeMap != null)
 		{
 			DataTypeInformation dataTypeInfo = (DataTypeInformation) dataTypeMap
 					.get(primitiveAttribute);
 			if(dataTypeInfo != null)
 			{
 				databaseDataType = dataTypeInfo.getDatabaseDataType();
 			}
 		}
		else
		{
			throw new DataTypeFactoryInitializationException("Cannot find populated dataType Map.");
		}
 
 		return databaseDataType;
 	}
 
 	/**
 	 * @param dataType
 	 * @return
 	 */
 	public DataTypeInformation getDataTypePrecisionScaleInformation(String dataType)
 	{
 		DataTypeInformation dataTypeInfo = null;
 		if(dataTypeMap != null)
 		{
 			dataTypeInfo=(DataTypeInformation) dataTypeMap.get(dataType);
 		}
 		return dataTypeInfo;
 	}
 
 }
