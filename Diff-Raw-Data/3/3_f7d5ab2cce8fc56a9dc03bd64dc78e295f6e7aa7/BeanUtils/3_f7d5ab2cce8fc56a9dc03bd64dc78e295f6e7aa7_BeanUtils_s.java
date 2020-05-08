 /*
  * $Id$
  *
  * Copyright 2003-2008 Online Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.AbstractList;
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.xins.common.collections.ChainedMap;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderConverter;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.text.TextUtils;
 import org.xins.common.types.EnumItem;
 import org.xins.common.types.ItemList;
 import org.xins.common.types.standard.Date;
 import org.xins.common.types.standard.Timestamp;
 import org.xins.common.xml.Element;
 
 /**
  * This class contains some utility methods that fills an object with values
  * from another object.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  *
  * @since XINS 1.5.0.
  */
 public class BeanUtils {
 
    /**
     * Constant used to identified some methods.
     */
    private static final Class[] STRING_CLASS = {String.class};
 
    /**
     * Get the values returned by the get methods of the source object and
     * call the set method of the destination object for the same property.
     *
     * e.g. String getFirstName() value of the source object will be used to
     * invoke setFirstName(String) of the destination object.
     *
     * If the no matching set method exists or the set method parameter is not the
     * same type as the object returned by the get method, the property is ignored.
     *
     * @param source
     *    the source object to get the values from. Cannot be <code>null</code>.
     * @param destination
     *    the destination object to put the values in. Cannot be <code>null</code>.
     *
     * @return
     *    the populated object, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>source == null || destination == null</code>.
     */
    public static Object populate(Object source, Object destination) throws IllegalArgumentException {
       return populate(source, destination, null);
    }
 
    /**
     * Get the values returned by the get methods of the source object and
     * call the set method of the destination object for the same property.
     *
     * e.g. String getFirstName() value of the source object will be used to
     * invoke setFirstName(String) of the destination object.
     *
     * If the no matching set method exists or the set method parameter is not the
     * same type as the object returned by the get method, the property is ignored.
     *
     * @param source
     *    the source object to get the values from. Cannot be <code>null</code>.
     * @param destination
     *    the destination object to put the values in. Cannot be <code>null</code>.
     * @param propertiesMapping
     *    the mapping between properties which does not have the same name.
     *
     * @return
     *    the populated object, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>source == null || destination == null</code>.
     */
    public static Object populate(Object source, Object destination, Properties propertiesMapping)
    throws IllegalArgumentException {
 
       MandatoryArgumentChecker.check("source", source, "destination", destination);
       // Go through all get methods of the source object
       Method[] sourceMethods = source.getClass().getMethods();
       for (int i = 0; i < sourceMethods.length; i++) {
          String getMethodName = sourceMethods[i].getName();
          Class getMethodReturnType = sourceMethods[i].getReturnType();
          if ((getMethodName.startsWith("get") && getMethodName.length() > 3 && !getMethodName.equals("getClass")) ||
                (getMethodName.startsWith("is") && getMethodName.length() > 2 &&
                   (getMethodReturnType == Boolean.class || getMethodReturnType == Boolean.TYPE)) ||
                (getMethodName.startsWith("has") && getMethodName.length() > 3 &&
                   (getMethodReturnType == Boolean.class || getMethodReturnType == Boolean.TYPE))) {
 
             // Determine the name of the set method
             String destProperty = null;
             if (getMethodName.startsWith("is")) {
                destProperty = getMethodName.substring(2);
             } else {
                destProperty = getMethodName.substring(3);
             }
             if (propertiesMapping != null && propertiesMapping.getProperty(destProperty) != null) {
                destProperty = propertiesMapping.getProperty(destProperty);
             }
             String setMethodName = "set" + destProperty;
 
             // Invoke the set method with the value returned by the get method
             try {
                Object value = sourceMethods[i].invoke(source, null);
                if (value != null) {
                   Object setValue = convertObject(value, destination, destProperty);
                   if (setValue != null) {
                      invokeMethod(destination, setMethodName, setValue);
                   }
                }
             } catch (Exception nsmex) {
 
                // Ignore this property
                Utils.logIgnoredException(nsmex);
             }
          }
       }
 
       // If the source object has a data section, fill the destination with it
       try {
          Method dataElementMethod = source.getClass().getMethod("dataElement", null);
          Object dataElement = dataElementMethod.invoke(source, null);
          if ("org.xins.client.DataElement".equals(dataElement.getClass().getName())) {
             Method toXMLElementMethod = dataElement.getClass().getMethod("toXMLElement", null);
             Element element = (Element) toXMLElementMethod.invoke(dataElement, null);
             xmlToObject(element, destination);
          }
       } catch (Exception e) {
          // Probably no method found
       }
       return destination;
    }
 
    /**
     * Converts the source object to an object of another class.
     *
     * @param origValue
     *    the original value of the object to be converted, if needed. Cannot be <code>null</code>.
     * @param destClass
     *    the destination class in which the object should be converted, cannot be <code>null</code>.
     *
     * @return
     *    the converted object or <code>null</code> if the object cannot be converted.
     *
     * @since XINS 2.0.
     */
    public static Object convert(Object origValue, Class destClass) {
 
       if (origValue.getClass() == destClass) {
          return origValue;
       }
       try {
          // Convert a String or an EnumItem to another EnumItem.
          if (EnumItem.class.isAssignableFrom(destClass)) {
             String enumTypeClassName = destClass.getName().substring(0, destClass.getName().length() - 5);
             Object enumType = Class.forName(enumTypeClassName).getDeclaredField("SINGLETON").get(null);
             Method convertionMethod = enumType.getClass().getMethod("getItemByValue", STRING_CLASS);
             Object[] convertParams = {origValue.toString()};
             Object convertedObj = convertionMethod.invoke(null, convertParams);
             return convertedObj;
 
          // Convert whatever to a String
          } else if (destClass == String.class) {
             return origValue.toString();
 
          // Convert an Object to a boolean
          } else if (destClass == Boolean.class || destClass == Boolean.TYPE) {
             if ("true".equals(origValue) || Boolean.TRUE.equals(origValue)) {
                return Boolean.TRUE;
             } else if ("false".equals(origValue) || Boolean.FALSE.equals(origValue)) {
                return Boolean.FALSE;
             }
 
          // Convert a String to a date
          } else if (origValue instanceof String && destClass == Date.Value.class) {
             return Date.SINGLETON.fromString((String) origValue);
 
          // Convert a String to a timestamp
          } else if (origValue instanceof String && destClass == Timestamp.Value.class) {
             return Timestamp.SINGLETON.fromString((String) origValue);
 
          // Convert a Collection (List,Set) to a ListItem
          } else if (origValue instanceof Collection && ItemList.class.isAssignableFrom(destClass)) {
             ItemList destValue = (ItemList) destClass.newInstance();
             destValue.add((Collection) origValue);
             return destValue;
 
          // Convert a ListItem to a collection
          } else if (origValue instanceof ItemList && Collection.class.isAssignableFrom(destClass)) {
             Class collectionClass = destClass;
             if (destClass.isAssignableFrom(AbstractList.class) || destClass == List.class || destClass == Collection.class) {
                collectionClass = ArrayList.class;
             } else if (destClass.isAssignableFrom(AbstractSet.class) || destClass == Set.class) {
                collectionClass = HashSet.class;
             }
             Collection destValue = (Collection) collectionClass.newInstance();
             Collection values = ((ItemList) origValue).get();
             destValue.addAll(values);
             return destValue;
 
          // Convert a Date or Calendar to a Date.Value or a Timestamp.Value
          } else if ((origValue instanceof java.util.Date | origValue instanceof Calendar) &&
                (destClass == Date.Value.class || destClass == Timestamp.Value.class)) {
             Class[] idemClass = {origValue.getClass()};
             Object[] valueArgs = {origValue};
             Constructor dateConstructor = destClass.getConstructor(idemClass);
             return dateConstructor.newInstance(valueArgs);
 
          // Convert a Date.Value to a Date
          } else if (origValue instanceof Date.Value && destClass == java.util.Date.class) {
             return ((Date.Value) origValue).toDate();
 
          // Convert a Timestamp.Value to a Date
          } else if (origValue instanceof Timestamp.Value && destClass == java.util.Date.class) {
             return ((Timestamp.Value) origValue).toDate();
 
          // Convert a String to whatever is asked
          } else if (origValue instanceof String) {
             Method convertionMethod = null;
             if (destClass.isPrimitive()) {
                if (destClass == Byte.TYPE) {
                   destClass = Byte.class;
                } else if (destClass == Short.TYPE) {
                   destClass = Short.class;
                } else if (destClass == Integer.TYPE) {
                   destClass = Integer.class;
                } else if (destClass == Long.TYPE) {
                   destClass = Long.class;
                } else if (destClass == Float.TYPE) {
                   destClass = Float.class;
                } else if (destClass == Double.TYPE) {
                   destClass = Double.class;
                }
             }
             try {
                convertionMethod = destClass.getMethod("valueOf", STRING_CLASS);
             } catch (NoSuchMethodException nsmex) {
                //Ignore
             }
             if (convertionMethod == null) {
                try {
                   convertionMethod = destClass.getMethod("fromStringForOptional", STRING_CLASS);
                } catch (NoSuchMethodException nsmex) {
                   //Ignore
                }
             }
             if (convertionMethod != null) {
                String[] convertParams = {origValue.toString()};
                Object convertedObj = convertionMethod.invoke(null, convertParams);
                return convertedObj;
             }
 
          // Convert a Number to the primitive type
          } else if (origValue instanceof Number && destClass.isPrimitive()) {
             return origValue;
          }
       } catch (Exception ex) {
          Log.log_1600(String.valueOf(origValue), origValue.getClass().getName(),
                destClass.getName(), ex.getClass().getName(), ex.getMessage());
       }
       return null;
    }
 
    /**
     * Converts the value of an object to another object in case that the
     * set method doesn't accept the same obejct as the get method.
     *
     * @param origValue
     *    the original value of the object to be converted, if needed. Cannot be <code>null</code>.
     * @param destination
     *    the destination class containing the set method, cannot be <code>null</code>.
     * @param property
     *    the name of the destination property, cannot be <code>null</code>.
     *
     * @return
     *    the converted object.
     *
     * @throws Exception
     *    if error occurs when using the reflection API.
     */
    private static Object convertObject(Object origValue, Object destination, String property) throws Exception {
       String setMethodName = "set" + TextUtils.firstCharUpper(property);
 
       // First test if the method with the same class as source exists
       try {
          Class[] idemClass = {origValue.getClass()};
          destination.getClass().getMethod(setMethodName, idemClass);
          return origValue;
       } catch (NoSuchMethodException nsmex) {
          // Ignore, try to find the other methods
       }
 
       Method[] destMethods = destination.getClass().getMethods();
       for (int i = 0; i < destMethods.length; i++) {
          if (destMethods[i].getName().equals(setMethodName)) {
             Class destClass = destMethods[i].getParameterTypes()[0];
             Object converted = convert(origValue, destClass);
             if (converted != null) {
                return converted;
             }
          }
       }
 
       // No method found
       return null;
    }
 
    /**
     * Fills the result object with of the content of the XML element object.
     *
     * @param element
     *    the XML element object, can be <code>null</code>.
     * @param result
     *    the object to put the values in, cannot be <code>null</code>.
     * @param elementMapping
     *    a Map&lt;String, String&gt; that maps the name of the source element
     *    to the name of the destination object, can be <code>null</code>.
     * @param attributeMapping
     *    a Map&lt;String, String&gt; that maps the attributes of the elements,
     *    can be <code>null</code>.
     *
     * @return
     *    the result object filled with the values of the element object, never <code>null</code>.
     *
     * @since XINS 2.0.
     */
    public static Object xmlToObject(Element element, Object result,
          Map elementMapping, Map attributeMapping) {
       return xmlToObject(element, result, elementMapping, attributeMapping, true);
    }
 
    /**
     * Fills the result object with of the content of the XML element object.
     *
     * @param element
     *    the XML element object, can be <code>null</code>.
     * @param result
     *    the object to put the values in, cannot be <code>null</code>.
     *
     * @return
     *    the result object filled with the values of the element object, never <code>null</code>.
     */
    public static Object xmlToObject(Element element, Object result) {
       return xmlToObject(element, result, null, null, true);
    }
 
    /**
     * Fills the result object with of the content of the XML element object.
     *
     * @param element
     *    the XML element object, can be <code>null</code>.
     * @param result
     *    the object to put the values in, cannot be <code>null</code>.
     * @param elementMapping
     *    a Map&lt;String, String&gt; that maps the name of the source element
     *    to the name of the destination object, can be <code>null</code>.
     * @param attributeMapping
     *    a Map&lt;String, String&gt; that maps the attributes of the elements,
     *    can be <code>null</code>.
     * @param topLevel
     *    <code>true</code> if the element passed is the top element,
     *    <code>false</code> if it is a sub-element.
     *
     * @return
     *    the result object filled with the values of the element object, never <code>null</code>.
     */
    private static Object xmlToObject(Element element, Object result,
          Map elementMapping, Map attributeMapping, boolean topLevel) {
 
       // Short-circuit if arg is null
       if (element == null) {
          return result;
       }
       String elementName = element.getLocalName();
       if (topLevel && elementName.equals("data")) {
          Iterator itChildren = element.getChildElements().iterator();
          while (itChildren.hasNext()) {
             Element nextChild = (Element) itChildren.next();
             xmlToObject(nextChild, result, elementMapping, attributeMapping, true);
          }
       } else {
          try {
             String hungarianName = TextUtils.firstCharUpper(elementName);
             if (elementMapping != null && elementMapping.containsKey(elementName)) {
                hungarianName = TextUtils.firstCharUpper((String) elementMapping.get(elementName));
             }
             Class[] argsClasses = {getElementClass(hungarianName, result)};
             Method addMethod = result.getClass().getMethod("add" + hungarianName, argsClasses);
             Object childElement = elementToObject(element, result, elementMapping, attributeMapping);
             if (childElement != null) {
                Object[] addArgs = { childElement };
                addMethod.invoke(result, addArgs);
             }
          } catch (Exception ex) {
             Utils.logIgnoredException(ex);
          }
       }
 
       return result;
    }
 
    /**
     * Gets the class matching the XML element.
     *
     * @param hungarianName
     *    the name of the XML element starting with an uppercase, cannot be <code>null</code>.
     * @param result
     *    the base object to get the class from, cannot be <code>null</code>.
     *
     * @return
     *    the class to used to fill the XML values with, never <code>null</code>
     *
     * @throws ClassNotFoundException
     *    if the class cannot be found.
     */
    private static Class getElementClass(String hungarianName, Object result) throws ClassNotFoundException {
       String elementClassName = result.getClass().getName();
       if (elementClassName.indexOf("$") != -1) {
          elementClassName = elementClassName.substring(0, elementClassName.indexOf("$"));
       }
       elementClassName += "$" + hungarianName;
       Class elementClass = Class.forName(elementClassName);
       return elementClass;
    }
 
    /**
     * Fills the result object with of the content of the XML element object.
     *
     * @param element
     *    the XML element object, cannot be <code>null</code>.
     * @param result
     *    the object to put the values in, cannot be <code>null</code>.
     * @param elementMapping
     *    a Map&lt;String, String&gt; that maps the name of the source element
     *    to the name of the destination object, can be <code>null</code>.
     * @param attributeMapping
     *    a Map&lt;String, String&gt; that maps the attributes of the elements,
     *    can be <code>null</code>.
     *
     * @return
     *    the result object filled with the values of the element object, never <code>null</code>.
     */
    private static Object elementToObject(Element element, Object result, Map elementMapping, Map attributeMapping) {
       String elementName = element.getLocalName();
       String hungarianName = TextUtils.firstCharUpper(elementName);
       if (elementMapping != null && elementMapping.containsKey(elementName)) {
          hungarianName = TextUtils.firstCharUpper((String) elementMapping.get(elementName));
       }
       //String newElementClassName = result.getClass().getName() + "." + elementName;
       Object newElement;
       try {
          newElement = getElementClass(hungarianName, result).newInstance();
       } catch (Exception ex) {
          Utils.logIgnoredException(ex);
          return null;
       }
 
       // Copy the attributes
       Iterator itAttr = element.getAttributeMap().entrySet().iterator();
       while (itAttr.hasNext()) {
          Map.Entry attr = (Map.Entry) itAttr.next();
          String name = ((Element.QualifiedName) attr.getKey()).getLocalName();
          if (attributeMapping != null && attributeMapping.containsKey(name)) {
             name = (String) attributeMapping.get(name);
          }
          String value = (String) attr.getValue();
          try {
             Object setArg = convertObject(value, newElement, name);
             invokeMethod(newElement, "set" + TextUtils.firstCharUpper(name), setArg);
          } catch (Exception ex) {
             Utils.logIgnoredException(ex);
          }
       }
 
       // Copy the character data content
       String text = element.getText();
       if (text != null && text.trim().length() > 0) {
          try {
             Method pcdataMethod = newElement.getClass().getMethod("pcdata", STRING_CLASS);
             Object[] pcdataArgs = { text };
             pcdataMethod.invoke(newElement, pcdataArgs);
          } catch (Exception ex) {
             Utils.logIgnoredException(ex);
          }
       }
 
       // Copy the children
       Iterator itChildren = element.getChildElements().iterator();
       while (itChildren.hasNext()) {
          Element child = (Element) itChildren.next();
          xmlToObject(child, newElement, elementMapping, attributeMapping, false);
       }
 
       return newElement;
    }
 
    /**
     * Gets the values returned by the get methods of the given POJO and put
     * the values in a <code>Map</code>.
     * The property names returned start with a lowercase.
     *
     * @param source
     *    the object from which the values are extracted and put in the Map, should not be <code>null</code>
     *
     * @return
     *     the property values of the source object. The key of the Map is
     *     the name of the property and the value is the value as returned by the get method.
     *     Never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>source == null</code>.
     *
     * @since XINS 2.0.
     */
    public static Map getParameters(Object source) throws IllegalArgumentException {
 
       MandatoryArgumentChecker.check("source", source);
 
       // Go through all get methods of the source object
       ChainedMap valuesMap = new ChainedMap();
       Method[] sourceMethods = source.getClass().getMethods();
       for (int i = 0; i < sourceMethods.length; i++) {
          String getMethodName = sourceMethods[i].getName();
          Class getMethodReturnType = sourceMethods[i].getReturnType();
          if ((getMethodName.startsWith("get") && getMethodName.length() > 3 && !getMethodName.equals("getClass")) ||
                (getMethodName.startsWith("is") && getMethodName.length() > 2 &&
                   (getMethodReturnType == Boolean.class || getMethodReturnType == Boolean.TYPE)) ||
                (getMethodName.startsWith("has") && getMethodName.length() > 3 &&
                   (getMethodReturnType == Boolean.class || getMethodReturnType == Boolean.TYPE))) {
 
             // Determine the name of the property
             String propertyName = null;
             if (getMethodName.startsWith("is")) {
                propertyName = getMethodName.substring(2);
             } else {
                propertyName = getMethodName.substring(3);
             }
             propertyName = TextUtils.firstCharLower(propertyName);
             try {
                Object propertyValue = sourceMethods[i].invoke(source, null);
                if (propertyValue != null) {
                   valuesMap.put(propertyName, propertyValue);
                }
             } catch (Exception ex) {
                Utils.logIgnoredException(ex);
             }
          }
       }
       return valuesMap;
   }
 
    /**
     * Gets the values returned by the get methods of the given POJO,
     * transform it to a String object and put the values in a <code>Map</code>.
     * The property names returned start with a lowercase.
     *
     * @param source
     *    the object from which the values are extracted and put in the Map, should not be <code>null</code>
     *
     * @return
     *     the property values of the source object. The key of the Map is
     *     the name of the property and the value is the String representation of
     *     the value as returned by the get method. Never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>source == null</code>.
     *
     * @since XINS 2.0.
     */
    public static Map getParametersAsString(Object source) throws IllegalArgumentException {
       ChainedMap stringMap = new ChainedMap();
       Map originalMap = getParameters(source);
       Iterator itParams = originalMap.entrySet().iterator();
       while (itParams.hasNext()) {
          Map.Entry nextParam = (Map.Entry) itParams.next();
          String paramName = (String) nextParam.getKey();
          Object paramValue = nextParam.getValue();
          String stringValue = String.valueOf(paramValue);
          stringMap.put(paramName, stringValue);
       }
       return stringMap;
    }
 
    /**
     * This method is similar to {@link #getParameters(Object)} except that objects
     * using classes of org.xins.common.type.* packages will be translated into
     * standard Java object java.* packages.
     *
     * @param source
     *    the object from which the values are extracted and put in the Map, should not be <code>null</code>
     *
     * @return
     *     the property values of the source object. The key of the Map is
     *     the name of the property and the value is a standard Java object representation of
     *     the value as returned by the get method. Never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>source == null</code>.
     *
     * @since XINS 2.0.
     */
    public static Map getParametersAsObject(Object source) throws IllegalArgumentException {
       ChainedMap objectMap = new ChainedMap();
       Map originalMap = getParameters(source);
       Iterator itParams = originalMap.entrySet().iterator();
       while (itParams.hasNext()) {
          Map.Entry nextParam = (Map.Entry) itParams.next();
          String paramName = (String) nextParam.getKey();
          Object paramValue = nextParam.getValue();
 
          // Convertion of the XINS types.
          if (paramValue instanceof Date.Value) {
             paramValue = ((Date.Value) paramValue).toDate();
          } else if (paramValue instanceof Timestamp.Value) {
             paramValue = ((Timestamp.Value) paramValue).toDate();
          } else if (paramValue instanceof PropertyReader) {
             paramValue = PropertyReaderConverter.toProperties((PropertyReader) paramValue);
          } else if (paramValue instanceof ItemList) {
             paramValue = ((ItemList) paramValue).get();
          } else if (paramValue instanceof EnumItem) {
             paramValue = ((EnumItem) paramValue).getValue();
          } else if (paramValue instanceof TargetDescriptor) {
             paramValue = ((TargetDescriptor) paramValue).getURL();
          } else if (paramValue instanceof Descriptor) {
             // TODO: Not supported yet
          }
          objectMap.put(paramName, paramValue);
       }
       return objectMap;
    }
 
    /**
     * Puts the values of the <code>Map</code> in the destination object (POJO).
     * If needed the property value is converted to the type needed for the set method.
     * The property names can start with an uppercase or a lowercase.
     *
     * @param properties
     *    the map containing the values to fill the destination object, cannot be <code>null</code>
     *    The key of the <code>Map</code> should be the property name and will be
     *    used to find the set method of the destination object.
     *
     * @param destination
     *    the object which should be filled, cannot be <code>null</code>.
     *
     * @return
     *     the destination object filled, never <code>null</code>. This object will
     *     be the same as the input parameter.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null || destination == null</code>.
     *
     * @since XINS 2.0.
     */
    public static Object setParameters(Map properties, Object destination) throws IllegalArgumentException {
 
       MandatoryArgumentChecker.check("properties", properties, "destination", destination);
 
       // Go through all properties and find the set method
       Iterator itProperties = properties.entrySet().iterator();
       while (itProperties.hasNext()) {
          Map.Entry nextProp = (Map.Entry) itProperties.next();
          try {
             String propertyName = (String) nextProp.getKey();
             Object propertyValue = nextProp.getValue();
             String methodName = "set" + TextUtils.firstCharUpper(propertyName);
             Object methodArg = convertObject(propertyValue, destination, propertyName);
             invokeMethod(destination, methodName, methodArg);
          } catch (Exception ex) {
             Utils.logIgnoredException(ex);
          }
       }
       return destination;
   }
 
    /**
     * Invokes the given method with the given argument.
     *
     * @param destination
     *    the object upon which the method should be invoked, cannnot be <code>null</code>.
     *
     * @param methodName
     *    the name of the method to invoke, cannot be <code>null</code>.
     *
     * @param argument
     *    the argument for the method, can be <code>null</code>.
     *
     * @throws Exception
     *    if the call to the method failed for any reason.
     */
    private static void invokeMethod(Object destination, String methodName, Object argument) throws Exception {
       Class argumentClass = argument.getClass();
       Class[] argsClasses = { argumentClass };
       if (argument instanceof Boolean) {
          try {
             destination.getClass().getMethod(methodName, argsClasses);
          } catch (NoSuchMethodException nsmex) {
             argumentClass = Boolean.TYPE;
          }
       } else if (argument instanceof Number) {
          try {
             destination.getClass().getMethod(methodName, argsClasses);
          } catch (NoSuchMethodException nsmex) {
             argumentClass = (Class) argumentClass.getDeclaredField("TYPE").get(argument);
          }
       }
       Class[] argsClasses2 = { argumentClass };
       Object[] args = { argument };
       Method setMethod = destination.getClass().getMethod(methodName, argsClasses2);
       setMethod.invoke(destination, args);
    }
 }
