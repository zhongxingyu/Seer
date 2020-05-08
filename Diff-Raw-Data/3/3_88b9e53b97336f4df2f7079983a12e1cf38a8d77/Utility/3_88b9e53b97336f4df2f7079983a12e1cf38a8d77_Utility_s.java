 
 package edu.wustl.cab2b.common.util;
 
 import static edu.wustl.cab2b.common.util.Constants.ATTRIBUTE;
 import static edu.wustl.cab2b.common.util.Constants.ATTRIBUTE_WITH_DESCRIPTION;
 import static edu.wustl.cab2b.common.util.Constants.CLASS;
 import static edu.wustl.cab2b.common.util.Constants.CLASS_WITH_DESCRIPTION;
 import static edu.wustl.cab2b.common.util.Constants.CONNECTOR;
 import static edu.wustl.cab2b.common.util.Constants.PV;
 import static edu.wustl.cab2b.common.util.Constants.TYPE_CATEGORY;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.axis.utils.XMLUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.globus.wsrf.encoding.ObjectDeserializer;
 import org.hibernate.HibernateException;
 import org.w3c.dom.Document;
 
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FloatAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.IntegerAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ObjectAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ShortAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerExceptionConstantsInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.cab2b.common.queryengine.result.IQueryResult;
 import edu.wustl.cab2b.common.queryengine.result.IRecord;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.querysuite.queryobject.DataType;
 import edu.wustl.dao.HibernateDAO;
 import edu.wustl.dao.exception.DAOException;
 import gov.nih.nci.cagrid.syncgts.bean.SyncDescription;
 import gov.nih.nci.cagrid.syncgts.core.SyncGTS;
 
 /**
  * Utility Class contain general methods used through out the application.
  * 
  * @author Chandrakant Talele
  * @author Gautam Shetty
  */
 public class Utility implements EntityManagerExceptionConstantsInterface
 {
 
 	private static final Logger logger = edu.wustl.common.util.logger.Logger
 			.getLogger(Utility.class);
 
 	/**
 	 * Checks whether passed attribute/association is inherited.
 	 * 
 	 * @param abstractAttribute
 	 *            Attribute/Association to check.
 	 * @return TRUE if it is inherited else returns FALSE
 	 */
 	public static boolean isInherited(AbstractAttributeInterface abstractAttribute)
 	{
 		for (TaggedValueInterface tag : abstractAttribute.getTaggedValueCollection())
 		{
 			if (tag.getKey().equals(Constants.TYPE_DERIVED))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Generates unique string identifier for given association. It is generated
 	 * by concatenating
 	 * 
 	 * sourceEntityName +{@link Constants#CONNECTOR} + sourceRoleName +{@link Constants#CONNECTOR} +
 	 * targetRoleName +{@link Constants#CONNECTOR} + TargetEntityName
 	 * 
 	 * @param association
 	 *            Association
 	 * @return Unique string to represent given association
 	 */
 	public static String generateUniqueId(AssociationInterface association)
 	{
 		return concatStrings(association.getEntity().getName(), association.getSourceRole()
 				.getName(), association.getTargetRole().getName(), association.getTargetEntity()
 				.getName());
 	}
 
 	/**
 	 * @param s1 String
 	 * @param s2 String
 	 * @param s3 String
 	 * @param s4 String
 	 * @return Concatenated string made after connecting s1, s2, s3, s4 by {@link Constants#CONNECTOR}
 	 */
 	public static String concatStrings(String s1, String s2, String s3, String s4)
 	{
 		StringBuffer buff = new StringBuffer();
 		buff.append(s1);
 		buff.append(CONNECTOR);
 		buff.append(s2);
 		buff.append(CONNECTOR);
 		buff.append(s3);
 		buff.append(CONNECTOR);
 		buff.append(s4);
 		return buff.toString();
 	}
 
 	/**
 	 * Compares whether given searchPattern is present in passed searchString.
 	 * If it is present returns the position where match found. Otherwise it
 	 * returns -1.
 	 * 
 	 * @param searchPattern
 	 * @param searchString
 	 * @return The position where match found, otherwise returns -1.
 	 */
 	public static int indexOfRegEx(String searchPattern, String searchString)
 	{
 		Pattern pat = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE);
 		Matcher mat = pat.matcher(searchString);
 		int position = -1;
 
 		if (mat.find())
 		{
 			position = mat.start();
 		}
 		return position;
 	}
 
 	/**
 	 * Returns the entity group of given entity
 	 * 
 	 * @param entity
 	 *            Entity to check
 	 * @return Returns parent Entity Group
 	 */
 	public static EntityGroupInterface getEntityGroup(EntityInterface entity)
 	{
 		EntityGroupInterface entityGroup = entity.getEntityGroup();
 		Collection<TaggedValueInterface> taggedValues = entityGroup.getTaggedValueCollection();
 		if (getTaggedValue(taggedValues, Constants.CAB2B_ENTITY_GROUP) != null)
 		{
 			return entityGroup;
 		}
 
 		throw new RuntimeException("This entity does not have DE entity group",
 				new java.lang.RuntimeException(), ErrorCodeConstants.DE_0003);
 	}
 
 	/**
 	 * @param taggedValues
 	 *            collection of TaggedValueInterface
 	 * @param key
 	 *            string
 	 * @return The tagged value for given key in given tagged value collection.
 	 */
 	public static TaggedValueInterface getTaggedValue(
 			Collection<TaggedValueInterface> taggedValues, String key)
 	{
 		for (TaggedValueInterface taggedValue : taggedValues)
 		{
 			if (taggedValue.getKey().equals(key))
 			{
 				return taggedValue;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param taggable
 	 *            taggable object
 	 * @param key
 	 *            string
 	 * @return The tagged value for given key.
 	 */
 	public static TaggedValueInterface getTaggedValue(AbstractMetadataInterface taggable, String key)
 	{
 		return getTaggedValue(taggable.getTaggedValueCollection(), key);
 	}
 
 	/**
 	 * Checks whether passed Entity is a category or not.
 	 * 
 	 * @param entity
 	 *            Entity to check
 	 * @return Returns TRUE if given entity is Category, else returns false.
 	 */
 	public static boolean isCategory(EntityInterface entity)
 	{
 		TaggedValueInterface tag = getTaggedValue(entity.getTaggedValueCollection(), TYPE_CATEGORY);
 		return tag != null;
 	}
 
 	/**
 	 * Converts DE data type to queryObject dataType.
 	 * 
 	 * @param type
 	 *            the DE attribute type.
 	 * @return the DataType.
 	 */
 	public static DataType getDataType(AttributeTypeInformationInterface type)
 	{
 		if (type instanceof StringAttributeTypeInformation)
 		{
 			return DataType.String;
 		}
 		else if (type instanceof IntegerAttributeTypeInformation)
 		{
 			return DataType.Integer;
 		}
 		else if (type instanceof DateAttributeTypeInformation)
 		{
 			return DataType.Date;
 		}
 		else if (type instanceof FloatAttributeTypeInformation)
 		{
 			return DataType.Float;
 		}
 		else if (type instanceof BooleanAttributeTypeInformation)
 		{
 			return DataType.Boolean;
 		}
 		else if (type instanceof LongAttributeTypeInformation)
 		{
 			return DataType.Long;
 		}
 		else if (type instanceof DoubleAttributeTypeInformation)
 		{
 			return DataType.Double;
 		}
 		else if (type instanceof ShortAttributeTypeInformation)
 		{
 			return DataType.Integer;//TODO define short in data type
 		}
 		else if (type instanceof ObjectAttributeTypeInformation)
 		{
 			return DataType.String;
 		}
 		else
 		{
 			throw new RuntimeException("Unknown Attribute type");
 		}
 
 	}
 
 	/**
 	 * @param attribute
 	 *            Check will be done for this Attribute.
 	 * @return TRUE if there are any permissible values associated with this
 	 *         attribute, otherwise returns false.
 	 */
 	public static boolean isEnumerated(AttributeInterface attribute)
 	{
 		if (attribute.getAttributeTypeInformation().getDataElement() instanceof UserDefinedDEInterface)
 		{
 			UserDefinedDEInterface de = (UserDefinedDEInterface) attribute
 					.getAttributeTypeInformation().getDataElement();
 			return de.getPermissibleValueCollection().size() != 0;
 		}
 		return false;
 	}
 
 	/**
 	 * @param attribute
 	 *            Attribute to process.
 	 * @return Returns all the permissible values associated with this
 	 *         attribute.
 	 */
 	public static Collection<PermissibleValueInterface> getPermissibleValues(
 			AttributeInterface attribute)
 	{
 		if (isEnumerated(attribute))
 		{
 			UserDefinedDEInterface de = (UserDefinedDEInterface) attribute
 					.getAttributeTypeInformation().getDataElement();
 			return de.getPermissibleValueCollection();
 		}
 		return new ArrayList<PermissibleValueInterface>(0);
 	}
 
 	/**
 	 * Returns the display name if present as tagged value. Else returns the
 	 * actual name of the entity
 	 * 
 	 * @param entity
 	 *            The entity to process
 	 * @return The display name.
 	 */
 	public static String getDisplayName(EntityInterface entity)
 	{
 		String name = entity.getName();
 		if (isCategory(entity))
 		{
 			return name;
 		}
 
 		EntityGroupInterface eg = getEntityGroup(entity);
 
 		// As per Bug# 4577 <class name> (app_name v<version name>) e.g. Participant (caTissue Core v1.1)
 		String projectName = eg.getLongName();
 		if (projectName.equals("caFE Server 1.1"))
 		{
 			projectName = "caFE Server";
 		}
 
 		StringBuffer buff = new StringBuffer();
 		buff.append(name.substring(name.lastIndexOf('.') + 1, name.length()));
 		buff.append(" (");
 		buff.append(projectName);
 
 		String version = eg.getVersion();
 		if (version != null)
 		{
 			buff.append(" v");
 			buff.append(version);
 		}
 		buff.append(')');
 
 		return buff.toString();
 	}
 
 	/**
 	 * This method trims out package name form the entity name
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	public static String getOnlyEntityName(EntityInterface entity)
 	{
 		String name = entity.getName();
		String displayName = name.substring(name.lastIndexOf('.') + 1, name.length());
		return displayName;
 	}
 
 	/**
 	 * @param path
 	 *            A IPath object
 	 * @return Display string for given path
 	 */
 	public static String getPathDisplayString(IPath path)
 	{
 		String text = getHtmlRepresentation(path);
 		StringBuffer sb = new StringBuffer();
 		int textLength = text.length();
 		int currentStart = 0;
 		String currentString = null;
 		final int offset = 100;
 		int strLen = 0;
 		int len = 0;
 		while (currentStart < textLength && textLength > offset)
 		{
 			int lastIndex = currentStart + offset;
 			currentString = text.substring(currentStart, lastIndex);
 			strLen = strLen + currentString.length() + len;
 			sb.append(currentString);
 			int index = text.indexOf("<B>----></B>", lastIndex);
 			if (index == -1)
 			{
 				int location = text.indexOf(')', lastIndex);
 				if (location != -1)
 				{
 					index = location + 1;
 				}
 			}
 			if (index == -1)
 			{
 				index = text.indexOf(',', lastIndex);
 			}
 			if (index == -1)
 			{
 				index = text.indexOf(' ', lastIndex);
 			}
 			if (index != -1)
 			{
 				len = index - strLen;
 				currentString = text.substring(lastIndex, (lastIndex + len));
 				sb.append(currentString);
 				sb.append("<P>");
 			}
 			else
 			{
 				if (currentStart == 0)
 				{
 					currentStart = offset;
 				}
 				sb.append(text.substring(currentStart));
 				return sb.toString();
 			}
 
 			currentStart = currentStart + offset + len;
 			if ((currentStart + offset + len) > textLength)
 			{
 				break;
 			}
 		}
 		sb.append(text.substring(currentStart));
 		return sb.toString();
 	}
 
 	static String getHtmlRepresentation(IPath path)
 	{
 		StringBuffer text = new StringBuffer(40);
 		text.append("<HTML><B>Path</B>:");
 		List<IAssociation> pathList = path.getIntermediateAssociations();
 		text.append(Utility.getDisplayName(path.getSourceEntity()));
 		for (int i = 0; i < pathList.size(); i++)
 		{
 			text.append("<B>----></B>");
 			text.append(Utility.getDisplayName(pathList.get(i).getTargetEntity()));
 		}
 		text.append("</HTML>");
 		return text.toString();
 	}
 
 	/**
 	 * @param entity
 	 *            Entity to check
 	 * @return Attribute whose name is "identifier" OR "id"
 	 */
 	public static AttributeInterface getIdAttribute(EntityInterface entity)
 	{
 		for (AttributeInterface attribute : entity.getAttributeCollection())
 		{
 			if (isIdentifierAttribute(attribute))
 			{
 				return attribute;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param entity
 	 *            Entity to check
 	 * @return Name of the application to which given entity belongs
 	 */
 	public static String getApplicationName(EntityInterface entity)
 	{
 		return getEntityGroup(entity).getName();
 	}
 
 	/**
 	 * @param attribute
 	 *            Attribute to check
 	 * @return TRUE if attribute name is "identifier" OR "id"
 	 */
 	public static boolean isIdentifierAttribute(AttributeInterface attribute)
 	{
 		String attribName = attribute.getName();
 		return attribName.equalsIgnoreCase("id") || attribName.equalsIgnoreCase("identifier");
 	}
 
 	/**
 	 * Converts attribute set into a alphabetically sorted list.
 	 * 
 	 * @param inputAttributeSet
 	 *            Attribute set to sort
 	 * @return Sorted list of attributes
 	 */
 	public static List<AttributeInterface> getAttributeList(
 			Set<AttributeInterface> inputAttributeSet)
 	{
 		List<AttributeInterface> attributes = new ArrayList<AttributeInterface>(inputAttributeSet);
 		Collections.sort(attributes, new AttributeInterfaceComparator());
 		return attributes;
 	}
 
 	/**
 	 * @param queryResult
 	 *            Query result to process.
 	 * @return List of attributes from query result
 	 */
 	public static List<AttributeInterface> getAttributeList(IQueryResult<IRecord> queryResult)
 	{
 		Map<String, List<IRecord>> allRecords = queryResult.getRecords();
 		List<AttributeInterface> attributeList = new ArrayList<AttributeInterface>();
 		for (List<IRecord> recordList : allRecords.values())
 		{
 			if (!recordList.isEmpty())
 			{
 				IRecord record = recordList.get(0);
 				attributeList = getAttributeList(record.getAttributes());
 				break;
 			}
 		}
 		return attributeList;
 	}
 
 	/**
 	 * This method converts stack trace to the string representation
 	 * 
 	 * @param aThrowable
 	 *            throwable object
 	 * @return String representation of the stack trace
 	 */
 	public static String getStackTrace(Throwable throwable)
 	{
 		final Writer result = new StringWriter();
 		final PrintWriter printWriter = new PrintWriter(result);
 		throwable.printStackTrace(printWriter);
 		return result.toString();
 	}
 
 	/**
 	 * Get the specified resource first look into the cab2b.home otherwise look
 	 * into the classpath
 	 * 
 	 * @param resource
 	 *            the name of the resource
 	 * @return the URL for the resource
 	 * @throws MalformedURLException
 	 */
 	public static URL getResource(String resource)
 	{
 		String home = System.getProperty("cab2b.home");
 		File file = new File(home + "/conf", resource);
 		if (file.exists())
 		{
 			try
 			{
 				return file.toURI().toURL();
 			}
 			catch (MalformedURLException e)
 			{
 				logger.error("File not found in cab2b_home, will use default file ", e);
 			}
 		}
 		// is there a better way of getting a non-null class loader ?
 		ClassLoader loader = Utility.class.getClassLoader();
 		return loader.getResource(resource);
 	}
 
 	/**
 	 * @param queryName
 	 * @param values
 	 * @return
 	 * @throws HibernateException
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public static Collection<?> executeHQL(String queryName, List<Object> values)
 			throws HibernateException, DynamicExtensionsSystemException
 	{
 		Collection objects = new HashSet();
 		HibernateDAO hibernateDAO=null;
 		try
 		{
 			hibernateDAO =DynamicExtensionsUtility.getHibernateDAO();
 			objects=hibernateDAO.executeNamedQuery(queryName, null);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while rolling back the session", e);
 		}
 
 		finally
 		{
 			try
 			{
 				DynamicExtensionsUtility.closeHibernateDAO(hibernateDAO);
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while closing the session", e, DYEXTN_S_001);
 			}
 
 		}
 
 		return objects;
 	}
 
 	/**
 	 * @param queryName
 	 * @return
 	 * @throws HibernateException
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public static Collection<?> executeHQL(String queryName) throws HibernateException, DynamicExtensionsSystemException
 	{
 		return executeHQL(queryName, null);
 	}
 
 	/**
 	 * This method replaces the occurrence of find string with replacement in
 	 * original string.
 	 * 
 	 * @param originalString
 	 * @param find
 	 * @param replacement
 	 * @return
 	 */
 	public static String replaceAllWords(String originalString, String find, String replacement)
 	{
 		String original=originalString;
 		if (original == null || find == null || replacement == null)
 		{
 			return null;
 		}
 
 		for (int i = original.indexOf(find); i > -1;)
 		{
 			String partBefore = original.substring(0, i);
 			String partAfter = original.substring(i + find.length());
 			original = partBefore + replacement + partAfter;
 			i = original.indexOf(find, i + 1);
 		}
 		return original;
 	}
 
 	/**
 	 * Loads properties from a file present in classpath to java objects.
 	 * If any exception occurs, it is callers responsibility to handle it. 
 	 * @param propertyfile Name of property file. It MUST be present in classpath
 	 * @return Properties loaded from given file.
 	 */
 	public static Properties getPropertiesFromFile(String propertyfile)
 	{
 		Properties properties = null;
 		try
 		{
 			URL url = getResource(propertyfile);
 			InputStream is = url.openStream();
 			if (is == null)
 			{
 				logger.error("Unable fo find property file : " + propertyfile
 						+ "\n please put this file in classpath");
 			}
 
 			properties = new Properties();
 			properties.load(is);
 		}
 		catch (IOException e)
 		{
 			logger.error("Unable to load properties from : " + propertyfile);
 		}
 
 		return properties;
 	}
 
 	/**
 	 * Returns a fomatted string.
 	 * 
 	 * Example : -------------------------------------------
 	 * -----Input-------|------Output------------- xaQaUtWsdkjsSbAd > Xa Qa Ut
 	 * Wsdkjs Sb Ad tomDickAndHarry > Tom Dick And Harry id > Identifier
 	 * pubmedCount > Pubmed Count organism > Organism chromosomeMap > Chromosome
 	 * Map pubmed5Count > Pubmed5 Count 1234 > 1234
 	 * ---------------------------------------------
 	 * 
 	 * Note: first character should be in lower case.
 	 * 
 	 * @param str
 	 *            String to format.
 	 * @return formatted string.
 	 */
 	public static String getFormattedString(String str)
 	{
 		String returner = "";
 
 		if (str.equalsIgnoreCase("id"))
 		{
 			return "Identifier";
 		}
 		String[] splitStrings = null;
 
 		int upperCaseCount = countUpperCaseLetters(str);
 		if (upperCaseCount > 0)
 		{
 			splitStrings = splitCamelCaseString(str, upperCaseCount);
 			returner = getFormattedString(splitStrings);
 		}
 		else
 		{
 			returner = capitalizeFirstCharacter(str);
 		}
 		return returner;
 	}
 
 	/**
 	 * Utility method to get a formated string
 	 * @param splitStrings
 	 * @return
 	 */
 	public static String getFormattedString(String[] splitStrings)
 	{
 		StringBuffer returner = new StringBuffer();
 		for (int i = 0; i < splitStrings.length; i++)
 		{
 			String str = splitStrings[i];
 			if (i == splitStrings.length - 1)
 			{
 				returner.append(str);
 			}
 			else
 			{
 				returner.append(str).append(' ');
 			}
 		}
 		return returner.toString();
 	}
 
 	/**
 	 * Utility method to count upper case characters in the String
 	 */
 
 	static int countUpperCaseLetters(String str)
 	{
 		/*
 		 * This is the count of Capital letters in a string excluding first
 		 * character, and continuous upper case letter in the string.
 		 */
 		int countOfCapitalLetters = 0;
 		char[] chars = str.toCharArray();
 
 		for (int i = 1; i < chars.length; i++)
 		{
 			char character = chars[i];
 			char nextCharacter = 'x';
 			char prevCharacter = chars[i - 1];
 
 			if ((i + 1) < chars.length)
 			{
 				nextCharacter = chars[i + 1];
 			}
 
 			if ((Character.isUpperCase(character) && Character.isUpperCase(prevCharacter)
 					&& Character.isLowerCase(nextCharacter) && i != chars.length - 1)
 					|| (Character.isUpperCase(character) && Character.isLowerCase(prevCharacter) && Character
 							.isUpperCase(nextCharacter))
 					|| (Character.isUpperCase(character) && Character.isLowerCase(prevCharacter) && Character
 							.isLowerCase(nextCharacter)))
 			{
 
 				countOfCapitalLetters++;
 			}
 		}
 		return countOfCapitalLetters;
 	}
 
 	/**
 	 * Utility method to capitalize first character in the String
 	 */
 	static String capitalizeFirstCharacter(String str)
 	{
 		char[] chars = str.toCharArray();
 		char firstChar = chars[0];
 		chars[0] = Character.toUpperCase(firstChar);
 		return new String(chars);
 	}
 
 	/**
 	 * 
 	 * @param str
 	 * @param countOfUpperCaseLetter
 	 * @return
 	 */
 	static String[] splitCamelCaseString(String str, int countOfUpperCaseLetter)
 	{
 		String[] splitStrings = new String[countOfUpperCaseLetter + 1];
 		char[] chars = str.toCharArray();
 		int firstIndex = 0;
 		int lastIndex = 0;
 		int splitStrCount = 0;
 
 		// change indexing from "chars" 1 to length
 		for (int i = 1; i < chars.length; i++)
 		{
 			char character = chars[i];
 			char nextCharacter;
 			char previousCharacter;
 			if (splitStrCount != countOfUpperCaseLetter)
 			{
 				if (Character.isUpperCase(character))
 				{
 					if (i == (chars.length - 1))
 					{
 						splitStrings[splitStrCount++] = str.substring(0, i);
 
 						char[] lastCharIsUpperCase = new char[1];
 						lastCharIsUpperCase[0] = character;
 						splitStrings[splitStrCount++] = new String(lastCharIsUpperCase);
 					}
 					else
 					{
 						lastIndex = i;
 
 						previousCharacter = chars[i - 1];
 						nextCharacter = chars[i + 1];
 						if (Character.isUpperCase(previousCharacter)
 								&& Character.isLowerCase(nextCharacter)
 								|| Character.isLowerCase(previousCharacter)
 								&& Character.isUpperCase(nextCharacter)
 								|| (Character.isLowerCase(previousCharacter) && Character
 										.isLowerCase(nextCharacter)))
 						{
 							String split = str.substring(firstIndex, lastIndex);
 							if (splitStrCount == 0)
 							{
 								split = capitalizeFirstCharacter(split);
 							}
 							splitStrings[splitStrCount] = split;
 							splitStrCount++;
 							firstIndex = lastIndex;
 						}
 						else
 						{
 							continue;
 						}
 					}
 				}
 			}
 			else
 			{
 				firstIndex = lastIndex;
 				lastIndex = str.length();
 				String split = str.substring(firstIndex, lastIndex);
 				splitStrings[splitStrCount] = split;
 				break;
 			}
 		}
 
 		return splitStrings;
 	}
 
 	/**
 	 * This method returns the current system date and time in default format
 	 * @return
 	 */
 	public static String getFormattedSystemDate()
 	{
 		return getFormattedDate(new Date(), null);
 	}
 
 	/**
 	 * This method returns the date with default format.
 	 * @param date
 	 * @return
 	 */
 	public static String getFormattedDate(Date date)
 	{
 		return getFormattedDate(date, null);
 	}
 
 	/**
 	 * This method get the formatted date given the raw date and the string format.
 	 * If not format is not specified then the default format will be dd/MM/yyyy HH:mm. For example, 01/01/1000 01:01
 	 * 
 	 * @param date
 	 * @param dateFormatAsString
 	 * @return
 	 */
 	public static String getFormattedDate(Date date, String dateFormatAsString)
 	{
 		String dateFormat=dateFormatAsString;
 		if (null == dateFormat || dateFormat.length() == 0)
 		{
 			dateFormat = "dd/MM/yyyy HH:mm";
 		}
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
 		return simpleDateFormat.format(date);
 	}
 
 	/**
 	 * This method returns the model name constructed from it long name and the version.
 	 * @param longName
 	 * @param version
 	 * @return
 	 */
 	public static String createModelName(String longName, String version)
 	{
 		StringBuffer projectName = new StringBuffer(longName);
 
 		if (version != null && version.length() != 0)
 		{
 			projectName.append("_v");
 			projectName.append(version);
 		}
 
 		return projectName.toString();
 	}
 
 	/**
 	 * @param query caB2B query to check
 	 * @return TRUE : if given query is being fired on at-least one service containing https 
 	 */
 	public static boolean hasAnySecureService(ICab2bQuery query)
 	{
 		boolean anySecureService = false;
 		for (String url : query.getOutputUrls())
 		{
 			if (url.trim().toLowerCase().startsWith("https://"))
 			{
 				anySecureService = true;
 				break;
 			}
 		}
 		return anySecureService;
 	}
 
 	/**
 	 * This method generates the globus certificate in user.home folder
 	 * @param gridType
 	 */
 	public static void generateGlobusCertificate(String gridType)
 	{
 		URL signingPolicy = null;
 		URL certificate = null;
 		signingPolicy = Utility.class.getClassLoader().getResource(
 				CommonPropertyLoader.getSigningPolicy(gridType));
 		certificate = Utility.class.getClassLoader().getResource(
 				CommonPropertyLoader.getCertificate(gridType));
 
 		if (signingPolicy != null || certificate != null)
 		{
 			copyCACertificates(signingPolicy);
 			copyCACertificates(certificate);
 		}
 		else
 		{
 			logger.error("Could not find CA certificates");
 			throw new RuntimeException("Could not find CA certificates", ErrorCodeConstants.CDS_016);
 		}
 
 		logger.debug("Getting sync-descriptor.xml file for " + gridType);
 		try
 		{
 			logger.debug("Synchronizing with GTS service");
 			URL syncDescFile = Utility.class.getClassLoader().getResource(
 					CommonPropertyLoader.getSyncDesFile(gridType));
 
 			Document doc = XMLUtils.newDocument(syncDescFile.openStream());
 			Object obj = ObjectDeserializer.toObject(doc.getDocumentElement(),
 					SyncDescription.class);
 			SyncDescription description = (SyncDescription) obj;
 			SyncGTS.getInstance().syncOnce(description);
 			logger
 					.debug("Successfully synchronized with GTS service. Globus certificates generated.");
 		}
 		catch (Exception e)
 		{
 			logger.error(e.getMessage(), e);
 			throw new RuntimeException("Error occurred while generating globus certificates",e,
 					ErrorCodeConstants.CDS_004);
 		}
 	}
 
 	private static void copyCACertificates(URL inFileURL)
 	{
 		int index = inFileURL.getPath().lastIndexOf('/');
 		if (index > -1)
 		{
 			String fileName = inFileURL.getPath().substring(index + 1).trim();
 			File destination = new File(gov.nih.nci.cagrid.common.Utils
 					.getTrustedCerificatesDirectory()
 					+ File.separator + fileName);
 
 			try
 			{
 				FileUtils.copyURLToFile(inFileURL, destination);
 			}
 			catch (IOException e)
 			{
 				logger.error(e.getMessage(), e);
 				throw new RuntimeException("Unable to copy CA certificates to [user.home]/.globus",e,
 						ErrorCodeConstants.CDS_003);
 			}
 		}
 	}
 
 	/**
 	 * Prepares the array of search targets from the check box values selected by user.
 	 * @param searchClass TRUE if class search is selected 
 	 * @param searchAttribute TRUE if attribute search is selected 
 	 * @param searchPv TRUE if permissible value search is selected 
 	 * @param includeDesc TRUE if description is to be searched 
 	 * @return Integer array of selections made by user.
 	 */
 	public static int[] prepareSearchTarget(boolean searchClass, boolean searchAttribute,
 			boolean searchPv, boolean includeDesc)
 	{
 		List<Integer> target = new ArrayList<Integer>();
 		if (searchClass && includeDesc)
 		{
 			target.add(CLASS_WITH_DESCRIPTION);
 		}
 		else if (searchClass)
 		{
 			target.add(CLASS);
 		}
 
 		if (searchAttribute && includeDesc)
 		{
 			target.add(ATTRIBUTE_WITH_DESCRIPTION);
 		}
 		else if (searchAttribute)
 		{
 			target.add(ATTRIBUTE);
 		}
 
 		if (searchPv)
 		{
 			target.add(PV);
 		}
 		return toIntArray(target);
 	}
 
 	/**
 	 * @param list List of integers
 	 * @return array of integers
 	 */
 	public static int[] toIntArray(List<Integer> list)
 	{
 		int[] array = new int[list.size()];
 		for (int i = 0; i < list.size(); i++)
 		{
 			array[i] = list.get(i);
 		}
 		return array;
 	}
 }
