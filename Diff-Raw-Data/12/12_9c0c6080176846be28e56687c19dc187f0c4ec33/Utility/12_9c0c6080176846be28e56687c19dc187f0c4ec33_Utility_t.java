 package edu.wustl.cab2b.common.util;
 
 import static edu.wustl.cab2b.common.util.Constants.CONNECTOR;
 import static edu.wustl.cab2b.common.util.Constants.TYPE_CATEGORY;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
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
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.axis.utils.XMLUtils;
 import org.apache.log4j.Logger;
 import org.globus.wsrf.encoding.ObjectDeserializer;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
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
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.cab2b.common.queryengine.result.IQueryResult;
 import edu.wustl.cab2b.common.queryengine.result.IRecord;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.querysuite.queryobject.DataType;
 import edu.wustl.common.util.dbManager.DBUtil;
 import gov.nih.nci.cagrid.syncgts.bean.SyncDescription;
 import gov.nih.nci.cagrid.syncgts.core.SyncGTS;
 
 /**
  * Utility Class contain general methods used through out the application.
  * 
  * @author Chandrakant Talele
  * @author Gautam Shetty
  */
 public class Utility {
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(Utility.class);
 
     /**
      * Checks whether passed attribute/association is inherited.
      * 
      * @param abstractAttribute
      *            Attribute/Association to check.
      * @return TRUE if it is inherited else returns FALSE
      */
     public static boolean isInherited(AbstractAttributeInterface abstractAttribute) {
         for (TaggedValueInterface tag : abstractAttribute.getTaggedValueCollection()) {
             if (tag.getKey().equals(Constants.TYPE_DERIVED)) {
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
     public static String generateUniqueId(AssociationInterface association) {
         return concatStrings(association.getEntity().getName(), association.getSourceRole().getName(),
                              association.getTargetRole().getName(), association.getTargetEntity().getName());
     }
 
     /**
      * @param s1 String
      * @param s2 String
      * @param s3 String
      * @param s4 String
      * @return Concatenated string made after connecting s1, s2, s3, s4 by {@link Constants#CONNECTOR}
      */
     public static String concatStrings(String s1, String s2, String s3, String s4) {
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
     public static int indexOfRegEx(String searchPattern, String searchString) {
         Pattern pat = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE);
         Matcher mat = pat.matcher(searchString);
         int position = -1;
 
         if (mat.find()) {
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
     public static EntityGroupInterface getEntityGroup(EntityInterface entity) {
         for (EntityGroupInterface entityGroup : entity.getEntityGroupCollection()) {
             Collection<TaggedValueInterface> taggedValues = entityGroup.getTaggedValueCollection();
             if (getTaggedValue(taggedValues, Constants.CAB2B_ENTITY_GROUP) != null) {
                 return entityGroup;
             }
         }
         throw new RuntimeException("This entity does not have DE entity group", new java.lang.RuntimeException(),
                 ErrorCodeConstants.DE_0003);
     }
 
     /**
      * @param taggedValues
      *            collection of TaggedValueInterface
      * @param key
      *            string
      * @return The tagged value for given key in given tagged value collection.
      */
     public static TaggedValueInterface getTaggedValue(Collection<TaggedValueInterface> taggedValues, String key) {
         for (TaggedValueInterface taggedValue : taggedValues) {
             if (taggedValue.getKey().equals(key)) {
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
     public static TaggedValueInterface getTaggedValue(AbstractMetadataInterface taggable, String key) {
         return getTaggedValue(taggable.getTaggedValueCollection(), key);
     }
 
     /**
      * Checks whether passed Entity is a category or not.
      * 
      * @param entity
      *            Entity to check
      * @return Returns TRUE if given entity is Category, else returns false.
      */
     public static boolean isCategory(EntityInterface entity) {
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
     public static DataType getDataType(AttributeTypeInformationInterface type) {
         if (type instanceof StringAttributeTypeInformation) {
             return DataType.String;
         } else if (type instanceof IntegerAttributeTypeInformation) {
             return DataType.Integer;
         } else if (type instanceof DateAttributeTypeInformation) {
             return DataType.Date;
         } else if (type instanceof FloatAttributeTypeInformation) {
             return DataType.Float;
         } else if (type instanceof BooleanAttributeTypeInformation) {
             return DataType.Boolean;
         } else if (type instanceof LongAttributeTypeInformation) {
             return DataType.Long;
        } else if (type instanceof DoubleAttributeTypeInformation) {
            return DataType.Double;
        } else if (type instanceof ShortAttributeTypeInformation) {
            return DataType.Integer;//TODO define short in data type
        } else if (type instanceof ObjectAttributeTypeInformation) {
            return DataType.String;
         } else {
             throw new RuntimeException("Unknown Attribute type");
         }
 
     }
 
     /**
      * @param attribute
      *            Check will be done for this Attribute.
      * @return TRUE if there are any permissible values associated with this
      *         attribute, otherwise returns false.
      */
     public static boolean isEnumerated(AttributeInterface attribute) {
         if (attribute.getAttributeTypeInformation().getDataElement() instanceof UserDefinedDEInterface) {
             UserDefinedDEInterface de = (UserDefinedDEInterface) attribute.getAttributeTypeInformation().getDataElement();
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
     public static Collection<PermissibleValueInterface> getPermissibleValues(AttributeInterface attribute) {
         if (isEnumerated(attribute)) {
             UserDefinedDEInterface de = (UserDefinedDEInterface) attribute.getAttributeTypeInformation().getDataElement();
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
     public static String getDisplayName(EntityInterface entity) {
         String name = entity.getName();
         if (isCategory(entity)) {
             return name;
         }
 
         EntityGroupInterface eg = getEntityGroup(entity);
 
         // As per Bug# 4577 <class name> (app_name v<version name>) e.g. Participant (caTissue Core v1.1)
         String projectName = eg.getLongName();
         if (projectName.equals("caFE Server 1.1")) {
             projectName = "caFE Server";
         }
 
         StringBuffer buff = new StringBuffer();
         buff.append(name.substring(name.lastIndexOf(".") + 1, name.length()));
         buff.append(" (");
         buff.append(projectName);
 
         String version = eg.getVersion();
         if (version != null) {
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
     public static String getOnlyEntityName(EntityInterface entity) {
         String name = entity.getName();
         String displayName = name.substring(name.lastIndexOf(".") + 1, name.length());
         return displayName;
     }
 
     /**
      * @param path
      *            A IPath object
      * @return Display string for given path
      */
     public static String getPathDisplayString(IPath path) {
         String text = getHtmlRepresentation(path);
         StringBuffer sb = new StringBuffer();
         int textLength = text.length();
         int currentStart = 0;
         String currentString = null;
         final int offset = 100;
         int strLen = 0;
         int len = 0;
         while (currentStart < textLength && textLength > offset) {
             int lastIndex = currentStart + offset;
             currentString = text.substring(currentStart, lastIndex);
             strLen = strLen + currentString.length() + len;
             sb.append(currentString);
             int index = text.indexOf("<B>----></B>", lastIndex);
             if (index == -1) {
                 int location = text.indexOf(")", lastIndex);
                 if (location != -1) {
                     index = location + 1;
                 }
             }
             if (index == -1) {
                 index = text.indexOf(",", lastIndex);
             }
             if (index == -1) {
                 index = text.indexOf(" ", lastIndex);
             }
             if (index != -1) {
                 len = index - strLen;
                 currentString = text.substring(lastIndex, (lastIndex + len));
                 sb.append(currentString);
                 sb.append("<P>");
             } else {
                 if (currentStart == 0) {
                     currentStart = offset;
                 }
                 sb.append(text.substring(currentStart));
                 return sb.toString();
             }
 
             currentStart = currentStart + offset + len;
             if ((currentStart + offset + len) > textLength)
                 break;
         }
         sb.append(text.substring(currentStart));
         return sb.toString();
     }
 
     static String getHtmlRepresentation(IPath path) {
         StringBuffer text = new StringBuffer("<HTML><B>Path</B>:");
         List<IAssociation> pathList = path.getIntermediateAssociations();
         text.append(Utility.getDisplayName(path.getSourceEntity()));
         for (int i = 0; i < pathList.size(); i++) {
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
     public static AttributeInterface getIdAttribute(EntityInterface entity) {
         for (AttributeInterface attribute : entity.getAttributeCollection()) {
             if (isIdentifierAttribute(attribute)) {
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
     public static String getApplicationName(EntityInterface entity) {
         return getEntityGroup(entity).getName();
     }
 
     /**
      * @param attribute
      *            Attribute to check
      * @return TRUE if attribute name is "identifier" OR "id"
      */
     public static boolean isIdentifierAttribute(AttributeInterface attribute) {
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
     public static List<AttributeInterface> getAttributeList(Set<AttributeInterface> inputAttributeSet) {
         List<AttributeInterface> attributes = new ArrayList<AttributeInterface>(inputAttributeSet);
         Collections.sort(attributes, new AttributeInterfaceComparator());
         return attributes;
     }
 
     /**
      * @param queryResult
      *            Query result to process.
      * @return List of attributes from query result
      */
     public static List<AttributeInterface> getAttributeList(IQueryResult<IRecord> queryResult) {
         Map<String, List<IRecord>> allRecords = queryResult.getRecords();
         List<AttributeInterface> attributeList = new ArrayList<AttributeInterface>();
         for (List<IRecord> recordList : allRecords.values()) {
             if (!recordList.isEmpty()) {
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
     public static String getStackTrace(Throwable throwable) {
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
     public static URL getResource(String resource) {
         String home = System.getProperty("cab2b.home");
         File file = new File(home + "/conf", resource);
         if (file.exists()) {
             try {
                 return file.toURI().toURL();
             } catch (MalformedURLException e) {
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
      */
     public static Collection<?> executeHQL(String queryName, List<Object> values) throws HibernateException {
         Session session = DBUtil.currentSession();
         Query q = session.getNamedQuery(queryName);
 
         if (values != null) {
             for (int counter = 0; counter < values.size(); counter++) {
 
                 Object value = values.get(counter);
                 String objectType = value.getClass().getName();
                 String onlyClassName = objectType.substring(objectType.lastIndexOf(".") + 1, objectType.length());
 
                 if (onlyClassName.equals("String")) {
                     q.setString(counter, (String) value);
                 } else if (onlyClassName.equals("Integer")) {
                     q.setInteger(counter, Integer.parseInt(value.toString()));
                 } else if (onlyClassName.equals("Long")) {
                     q.setLong(counter, Long.parseLong(value.toString()));
                 }
             }
         }
         return q.list();
     }
 
     /**
      * @param queryName
      * @return
      * @throws HibernateException
      */
     public static Collection<?> executeHQL(String queryName) throws HibernateException {
         return executeHQL(queryName, null);
     }
 
     /**
      * This method replaces the occurrence of find string with replacement in
      * original string.
      * 
      * @param original
      * @param find
      * @param replacement
      * @return
      */
     public static String replaceAllWords(String original, String find, String replacement) {
         if (original == null || find == null || replacement == null) {
             return null;
         }
 
         for (int i = original.indexOf(find); i > -1;) {
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
     public static Properties getPropertiesFromFile(String propertyfile) {
         Properties properties = null;
         try {
             URL url = getResource(propertyfile);
             InputStream is = url.openStream();
             if (is == null) {
                 logger.error("Unable fo find property file : " + propertyfile
                         + "\n please put this file in classpath");
             }
 
             properties = new Properties();
             properties.load(is);
 
         } catch (IOException e) {
             logger.error("Unable to load properties from : " + propertyfile);
             e.printStackTrace();
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
     public static String getFormattedString(String str) {
         String returner = "";
 
         if (str.equalsIgnoreCase("id")) {
             return "Identifier";
         }
         String[] splitStrings = null;
 
         int upperCaseCount = countUpperCaseLetters(str);
         if (upperCaseCount > 0) {
             splitStrings = splitCamelCaseString(str, upperCaseCount);
             returner = getFormattedString(splitStrings);
         } else {
             returner = capitalizeFirstCharacter(str);
         }
         return returner;
     }
 
     /**
      * Utility method to get a formated string
      * @param splitStrings
      * @return
      */
     public static String getFormattedString(String[] splitStrings) {
         String returner = "";
         for (int i = 0; i < splitStrings.length; i++) {
             String str = splitStrings[i];
             if (i == splitStrings.length - 1) {
                 returner += str;
             } else {
                 returner += str + " ";
             }
         }
         return returner;
     }
 
     /**
      * Utility method to count upper case characters in the String
      */
 
     static int countUpperCaseLetters(String str) {
         /*
          * This is the count of Capital letters in a string excluding first
          * character, and continuous upper case letter in the string.
          */
         int countOfCapitalLetters = 0;
         char[] chars = str.toCharArray();
 
         for (int i = 1; i < chars.length; i++) {
             char character = chars[i];
             char nextCharacter = 'x';
             char prevCharacter = chars[i - 1];
 
             if ((i + 1) < chars.length)
                 nextCharacter = chars[i + 1];
 
             if ((Character.isUpperCase(character) && Character.isUpperCase(prevCharacter)
                     && Character.isLowerCase(nextCharacter) && i != chars.length - 1)
                     || (Character.isUpperCase(character) && Character.isLowerCase(prevCharacter) && Character.isUpperCase(nextCharacter))
                     || (Character.isUpperCase(character) && Character.isLowerCase(prevCharacter) && Character.isLowerCase(nextCharacter)))
 
                 countOfCapitalLetters++;
         }
         return countOfCapitalLetters;
     }
 
     /**
      * Utility method to capitalize first character in the String
      */
     static String capitalizeFirstCharacter(String str) {
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
     static String[] splitCamelCaseString(String str, int countOfUpperCaseLetter) {
         String[] splitStrings = new String[countOfUpperCaseLetter + 1];
         char[] chars = str.toCharArray();
         int firstIndex = 0;
         int lastIndex = 0;
         int splitStrCount = 0;
 
         // change indexing from "chars" 1 to length
         for (int i = 1; i < chars.length; i++) {
             char character = chars[i];
             char nextCharacter;
             char previousCharacter;
             if (splitStrCount != countOfUpperCaseLetter) {
                 if (Character.isUpperCase(character)) {
                     if (i == (chars.length - 1)) {
                         splitStrings[splitStrCount++] = str.substring(0, i);
 
                         char[] lasrCharIsUpperCase = new char[1];
                         lasrCharIsUpperCase[0] = character;
                         splitStrings[splitStrCount++] = new String(lasrCharIsUpperCase);
                     } else {
                         lastIndex = i;
 
                         previousCharacter = chars[i - 1];
                         nextCharacter = chars[i + 1];
                         if (Character.isUpperCase(previousCharacter)
                                 && Character.isLowerCase(nextCharacter)
                                 || Character.isLowerCase(previousCharacter)
                                 && Character.isUpperCase(nextCharacter)
                                 || (Character.isLowerCase(previousCharacter) && Character.isLowerCase(nextCharacter))) {
                             String split = str.substring(firstIndex, lastIndex);
                             if (splitStrCount == 0) {
                                 split = capitalizeFirstCharacter(split);
                             }
                             splitStrings[splitStrCount] = split;
                             splitStrCount++;
                             firstIndex = lastIndex;
                         } else {
                             continue;
                         }
                     }
                 }
             } else {
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
     public static String getFormattedSystemDate() {
         return getFormattedDate(new Date(), null);
     }
 
     /**
      * This method returns the date with default format.
      * @param date
      * @return
      */
     public static String getFormattedDate(Date date) {
         return getFormattedDate(date, null);
     }
 
     /**
      * This method get the formatted date given the raw date and the string format.
      * If not format is not specified then the default format will be dd/MM/yyyy HH:mm. For example, 01/01/1000 01:01
      * 
      * @param date
      * @param dateFormat
      * @return
      */
     public static String getFormattedDate(Date date, String dateFormat) {
         if (null == dateFormat || dateFormat.length() == 0) {
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
     public static String createModelName(String longName, String version) {
         StringBuffer projectName = new StringBuffer(longName);
 
         if (version != null && version.length() != 0) {
             projectName.append("_v");
             projectName.append(version);
         }
 
         return projectName.toString();
     }
 
     /**
      * @param query caB2B query to check
      * @return TRUE : if given query is being fired on at-least one service containing https 
      */
     public static boolean hasAnySecureService(ICab2bQuery query) {
         boolean anySecureSevice = false;
         for (String url : query.getOutputUrls()) {
             if (url.trim().toLowerCase().startsWith("https://")) {
                 anySecureSevice = true;
                 break;
             }
         }
         return anySecureSevice;
     }
 
     /**
      * 
      * @param inputStream
      * @param objectType
      * @return
      * @throws Exception
      */
     public static Object deserializeDocument(InputStream inputStream, Class objectType) throws Exception {
         Document doc = XMLUtils.newDocument(inputStream);
         Object obj = ObjectDeserializer.toObject(doc.getDocumentElement(), objectType);
         return obj;
     }
 
     /**
      * This method generates the globus certificate in user.home folder
      * @param idP
      */
     public static void generateGlobusCertificate(String idP) {
         URL signingPolicy = null;
         URL certificate = null;
         if ("Production".compareTo(idP) == 0) {
             signingPolicy = Utility.class.getClassLoader().getResource(idP + "/62f4fd66.signing_policy");
             certificate = Utility.class.getClassLoader().getResource(idP + "/62f4fd66.0");
         } else if ("Training".compareTo(idP) == 0) {
             signingPolicy = Utility.class.getClassLoader().getResource(idP + "/68907d53.signing_policy");
             certificate = Utility.class.getClassLoader().getResource(idP + "/68907d53.0");
         }
 
         if (signingPolicy != null || certificate != null) {
             copyCACertificates(signingPolicy);
             copyCACertificates(certificate);
         } else {
             logger.error("Could not find CA certificates");
             throw new RuntimeException("Could not find CA certificates", ErrorCodeConstants.CDS_016);
         }
 
         logger.debug("Getting sync-descriptor.xml file for " + idP);
         try {
             logger.debug("Synchronizing with GTS service");
             URL syncDescFile = Utility.class.getClassLoader().getResource(idP + "/sync-description.xml");
             SyncDescription description = (SyncDescription) Utility.deserializeDocument(syncDescFile.openStream(),
                                                                                         SyncDescription.class);
             SyncGTS.getInstance().syncOnce(description);
             logger.debug("Successfully syncronized with GTS service. Globus certificates generated.");
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
             throw new RuntimeException("Error occurred while generating globus certificates",
                     ErrorCodeConstants.CDS_004);
         }
     }
 
     private static void copyCACertificates(URL inFileURL) {
         InputStream in = null;
         try {
             in = inFileURL.openStream();
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
             throw new RuntimeException(inFileURL.getPath() + " file not found", ErrorCodeConstants.CDS_016);
         }
 
         File dest = null;
         int index = inFileURL.getPath().lastIndexOf('/');
         if (index > -1) {
             String fileName = inFileURL.getPath().substring(index + 1).trim();
             dest = new File(gov.nih.nci.cagrid.common.Utils.getTrustedCerificatesDirectory() + File.separator
                     + fileName);
         }
 
         byte[] buf = new byte[1024];
         int len = 0;
         try {
             OutputStream out = new FileOutputStream(dest);
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
 
             in.close();
             out.close();
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
             throw new RuntimeException("Unable to copy CA certificates to [user.home]/.globus",
                     ErrorCodeConstants.CDS_003);
         }
     }
 
     /**
      * This method sync the globus credential of server
      */
     public static void syncGlobusCredential() {
         synchronized (Utility.class) {
             File commonDir = new File(System.getProperty("user.home") + "\\commonDirCert");
             try {
                 generateGlobusCertificate("Production");
             } catch (RuntimeException re) {
                 logger.error("Cannot Create Production Grid Certificate", re);
             }
             copyToCommonPlace(commonDir);
 
             try {
                 generateGlobusCertificate("Training");
             } catch (RuntimeException re) {
                 logger.error("Cannot Create Training Grid Certificate", re);
             }
 
             File dir = gov.nih.nci.cagrid.common.Utils.getTrustedCerificatesDirectory();
             if (commonDir != null && commonDir.isDirectory()) {
                 for (File files : commonDir.listFiles())
                     copyFiles(files, dir);
             }
         }
     }
 
     private static void copyToCommonPlace(File commonDir) {
         File dir = gov.nih.nci.cagrid.common.Utils.getTrustedCerificatesDirectory();
         commonDir.mkdir();
         if (dir != null && dir.isDirectory()) {
             for (File file : dir.listFiles())
                 copyFiles(file, commonDir);
         }
     }
 
     private static void copyFiles(File file, File commonDir) {
         try {
             InputStream in = new FileInputStream(file);
 
             int index = file.getPath().lastIndexOf('\\');
             File dest = null;
             if (index > -1) {
                 String fileName = file.getPath().substring(index + 1).trim();
                 dest = new File(commonDir + File.separator + fileName);
             }
 
             byte[] buf = new byte[1024];
             int len = 0;
 
             OutputStream out = new FileOutputStream(dest);
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             in.close();
             out.close();
             //  file.delete();
         } catch (FileNotFoundException e) {
             logger.error(e.getMessage(), e);
             throw new RuntimeException("File Not Found ", e.getMessage());
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
             throw new RuntimeException("IO Exception Occured ", e.getMessage());
         }
     }
 
 }
