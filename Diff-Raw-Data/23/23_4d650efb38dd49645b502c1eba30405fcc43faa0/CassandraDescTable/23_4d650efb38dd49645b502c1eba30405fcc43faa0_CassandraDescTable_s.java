 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mymed.controller.core.manager.reputation.db.facade;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.mymed.controller.core.manager.reputation.globals.Constants;
 import com.mymed.utils.MLogger;
 
 /**
  * 
  * @author piccolo
  */
 public class CassandraDescTable {
   private Document dom;
 
   private static CassandraDescTable instance = new CassandraDescTable();
 
   public static CassandraDescTable getNewInstance() {
     return instance;
   }
 
   public static final String SEPARATOR_CHAR = "|";
 
   private CassandraDescTable() {
     // get the factory
     final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
     try {
       // Using factory get an instance of document builder
       final DocumentBuilder db = dbf.newDocumentBuilder();
 
       // parse using builder to get DOM representation of the XML file
       dom = db.parse(Constants.DATA_MODEL_FILE_PATH);
     } catch (final ParserConfigurationException ex) {
       MLogger.getDebugLog().debug("Error parsing data model file", ex.getCause());
     } catch (final SAXException ex) {
       MLogger.getDebugLog().debug("Error parsing data model file", ex.getCause());
     } catch (final IOException ex) {
       MLogger.getDebugLog().debug("Error parsing data model file", ex.getCause());
     }
   }
 
   public NodeList getListOfAttribute(final String columnFamilyName) {
     final Element rootElement = dom.getDocumentElement();
     final NodeList nl = rootElement.getElementsByTagName("table");
     Element elToRetrieve = null;
 
     if (nl != null && nl.getLength() > 0) {
       for (int i = 0; i < nl.getLength(); i++) {
         final Element currentEl = (Element) nl.item(i);
 
         if (currentEl.getAttribute("name").equals(columnFamilyName)) {
           elToRetrieve = currentEl;
           break;
         }
       }
 
       if (elToRetrieve == null) {
         throw new RuntimeException(columnFamilyName + ": ColumnFamily not found");
       } else {
         final Element attributesElement = (Element) elToRetrieve.getElementsByTagName("attributes").item(0);
         final NodeList listOfAttribute = attributesElement.getElementsByTagName("attribute");
         return listOfAttribute;
       }
     } else {
       throw new RuntimeException("The structure list cannot be empty");
     }
   }
 
   public NodeList getListOfConstraints(final String columnFamilyName) {
     final Element rootElement = dom.getDocumentElement();
     final NodeList nl = rootElement.getElementsByTagName("table");
     Element elToRetrieve = null;
 
     if (nl != null && nl.getLength() > 0) {
       for (int i = 0; i < nl.getLength(); i++) {
         final Element currentEl = (Element) nl.item(i);
         if (currentEl.getAttribute("name").equals(columnFamilyName)) {
           elToRetrieve = currentEl;
           break;
         }
       }
 
       if (elToRetrieve == null) {
         throw new RuntimeException(columnFamilyName + ": ColumnFamily not found");
       } else {
         final Element retEl = (Element) elToRetrieve.getElementsByTagName("constraints").item(0);
         return retEl.getElementsByTagName("constraint");
       }
     } else {
       throw new RuntimeException("The structure list cannot be empty");
     }
   }
 
   public String getNameOfPrimaryKeyField(final String columnFamilyName) {
     final NodeList listOfAttribute = getListOfAttribute(columnFamilyName);
 
     if (listOfAttribute != null && listOfAttribute.getLength() > 0) {
       Element pkToRetrieve = null;
       for (int i = 0; i < listOfAttribute.getLength(); i++) {
         final Element currEl = (Element) listOfAttribute.item(i);
         if (currEl.hasAttribute("key") && currEl.getAttribute("key").equals("primary")) {
           pkToRetrieve = currEl;
           break;
         }
       }
 
       if (pkToRetrieve == null) {
         throw new RuntimeException("Primary key not found");
       } else {
         return pkToRetrieve.getFirstChild().getNodeValue();
       }
     } else {
       throw new RuntimeException("The attribute list cannot be empty");
     }
   }
 
   public List<String> getUniqueFields(final String columnFamilyName) {
     final NodeList constraintList = getListOfConstraints(columnFamilyName);
     final ArrayList<String> result = new ArrayList<String>();
     if (constraintList != null && constraintList.getLength() > 0) {
       Element elToRetrieve = null;
       for (int i = 0; i < constraintList.getLength(); i++) {
         final Element currEl = (Element) constraintList.item(i);
 
         if (currEl.hasAttribute("type") && currEl.getAttribute("type").equals("unique")) {
           elToRetrieve = currEl;
           break;
         }
       }
 
       if (elToRetrieve != null) {
         final NodeList listOfAttributes = elToRetrieve.getElementsByTagName("attribute");
         if (listOfAttributes != null && listOfAttributes.getLength() > 0) {
           final String[] resultArray = new String[listOfAttributes.getLength()];
           for (int i = 0; i < listOfAttributes.getLength(); i++) {
             final Element currEl = (Element) listOfAttributes.item(i);
             resultArray[Integer.parseInt(currEl.getAttribute("order"))] = currEl.getFirstChild().getNodeValue();
           }
           result.addAll(Arrays.asList(resultArray));
           return result;
         } else {
           throw new RuntimeException();
         }
       } else {
         throw new RuntimeException();
       }
     } else {
       throw new RuntimeException();
     }
   }
 
   public String generateKeyForColumnFamily(final Object dbTableObject) {
     try {
       final String[] objParts = dbTableObject.getClass().getName().split("\\.");
 
       final String columnFamilyName = objParts[objParts.length - 1];
 
       final Class<?> idClass = Class.forName(Constants.PRIMARY_KEY_PACKAGE + "." + columnFamilyName + "Id");
       final Constructor<?> mainConstructor = idClass.getConstructors()[0];
 
       final List<String> uniqueFields = getUniqueFields(columnFamilyName);
       final Object[] initArgs = new Object[uniqueFields.size()];
 
       for (int i = 0; i < uniqueFields.size(); i++) {
         final Field currentField = dbTableObject.getClass().getDeclaredField(uniqueFields.get(i));
         currentField.setAccessible(true);
         initArgs[i] = currentField.get(dbTableObject);
       }
 
       final Object referredId = mainConstructor.newInstance(initArgs);
       return referredId.toString();
     } catch (final InstantiationException ex) {
      Logger.getLogger(CassandraDescTable.class.getName()).log(Level.SEVERE, null, ex);
     } catch (final IllegalAccessException ex) {
      Logger.getLogger(CassandraDescTable.class.getName()).log(Level.SEVERE, null, ex);
     } catch (final IllegalArgumentException ex) {
      Logger.getLogger(CassandraDescTable.class.getName()).log(Level.SEVERE, null, ex);
     } catch (final InvocationTargetException ex) {
      Logger.getLogger(CassandraDescTable.class.getName()).log(Level.SEVERE, null, ex);
     } catch (final NoSuchFieldException ex) {
      Logger.getLogger(CassandraDescTable.class.getName()).log(Level.SEVERE, null, ex);
     } catch (final SecurityException ex) {
      Logger.getLogger(CassandraDescTable.class.getName()).log(Level.SEVERE, null, ex);
     } catch (final ClassNotFoundException ex) {
      Logger.getLogger(CassandraDescTable.class.getName()).log(Level.SEVERE, null, ex);
     }
     return "";
   }
 
   @Deprecated
   public String generateKeyForSuperColumnItem(final Object dbTableObject, final String superColumnFamilyName)
       throws Exception {
     return generateKeyForColumnFamily(dbTableObject);
   }
 
   public List<String> getListOfForeignKey(final String tableName) {
     final List<String> result = new ArrayList<String>();
 
     final NodeList nl = getListOfAttribute(tableName);
     if (nl != null && nl.getLength() > 0) {
       for (int i = 0; i < nl.getLength(); i++) {
         final Element currEl = (Element) nl.item(i);
         if (currEl.hasAttribute("key") && currEl.getAttribute("key").equals("foreign")) {
           result.add(currEl.getFirstChild().getNodeValue());
         }
       }
     }
     return result;
   }
 
   public String getReferredTable(final String tableName, final String fieldName) {
     final NodeList nl = getListOfAttribute(tableName);
     if (nl != null && nl.getLength() > 0) {
       for (int i = 0; i < nl.getLength(); i++) {
         final Element currEl = (Element) nl.item(i);
         if (currEl.getFirstChild().getNodeValue().equals(fieldName)) {
           if (currEl.hasAttribute("references")) {
             return currEl.getAttribute("references");
           }
         }
       }
     }
     throw new RuntimeException();
   }
 }
