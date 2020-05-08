 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.brekka.stillingar.xmlbeans;
 
 import static java.lang.String.format;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.xmlbeans.XmlCursor;
 import org.apache.xmlbeans.XmlCursor.TokenType;
 import org.apache.xmlbeans.XmlObject;
 import org.brekka.stillingar.api.ConfigurationSource;
 import org.brekka.stillingar.api.ValueConfigurationException;
 import org.brekka.stillingar.core.conversion.ConversionManager;
 import org.brekka.stillingar.core.dom.DefaultNamespaceContext;
 
 /**
  * Configuration snapshot based on Apache XmlBeans.
  * 
  * @author Andrew Taylor
  */
 class XmlBeansConfigurationSource implements ConfigurationSource {
 
     private final XmlObject bean;
 
     private final ConversionManager conversionManager;
 
     private final DefaultNamespaceContext xpathNamespaces;
 
     public XmlBeansConfigurationSource(XmlObject bean, DefaultNamespaceContext xpathNamespaces,
             ConversionManager conversionManager) {
         this.bean = bean;
         this.xpathNamespaces = xpathNamespaces;
         this.conversionManager = conversionManager;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.Class)
      */
     public boolean isAvailable(Class<?> type) {
         XmlCursor cursor = bean.newCursor();
         try {
             TokenType token = cursor.toNextToken();
             while (token != TokenType.ENDDOC) {
                 if (token == TokenType.START) {
                     XmlObject object = cursor.getObject();
                     if (type.isAssignableFrom(object.getClass())) {
                         return true;
                     }
                 }
                 token = cursor.toNextToken();
             }
         } finally {
             cursor.dispose();
         }
         return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
      */
     public boolean isAvailable(String expression) {
         return evaluate(expression).length > 0;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.brekka.configuration.xmlbeans.Instance#retrieve(java.lang.Class)
      */
     public <T> T retrieve(Class<T> valueType) {
         T result = null;
         XmlObject[] found = find(valueType, true);
         if (found.length == 1) {
             result = convert(valueType, found[0], null);
         }
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.brekka.configuration.xmlbeans.Instance#retrieve(java.lang.Class, java.lang.String)
      */
     public <T> T retrieve(String expression, Class<T> valueType) {
         T value;
         XmlObject[] found = evaluate(expression);
         if (found.length == 1) {
             XmlObject xml = found[0];
             value = convert(valueType, xml, expression);
         } else if (found.length == 0) {
             // No value found, return null
             value = null;
         } else {
             throw new ValueConfigurationException(
                     "multiple values found, only one expected", valueType.getClass(),
                     expression);
         }
         return value;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.brekka.configuration.xmlbeans.Instance#retrieveList(java.lang.Class)
      */
     public <T> List<T> retrieveList(Class<T> valueType) {
         List<T> results = new ArrayList<T>();
        XmlObject[] found = find(valueType, true);
         for (XmlObject xmlObject : found) {
             T value = convert(valueType, xmlObject, null);
             results.add(value);
         }
         return results;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.brekka.configuration.xmlbeans.Instance#retrieveList(java.lang.Class, java.lang.String)
      */
     public <T> List<T> retrieveList(String expression, Class<T> valueType) {
         List<T> results = new ArrayList<T>();
         XmlObject[] found = evaluate(expression);
         for (XmlObject xmlObject : found) {
             T value = convert(valueType, xmlObject, expression);
             results.add(value);
         }
         return results;
     }
 
     private XmlObject[] find(Class<?> type, boolean singleExpected) {
         List<XmlObject> results = new ArrayList<XmlObject>();
         XmlCursor cursor = bean.newCursor();
         TokenType token = cursor.toNextToken();
         while (token != TokenType.ENDDOC) {
             if (token == TokenType.START) {
                 XmlObject object = cursor.getObject();
                 if (type.isAssignableFrom(object.getClass())) {
                     results.add(object);
                     if (results.size() > 1 && singleExpected) {
                         throw new ValueConfigurationException(
                                 "multiple values found, only one expected",
                                 type.getClass(), null);
                     }
                 }
             }
             token = cursor.toNextToken();
         }
         cursor.dispose();
         return results.toArray(new XmlObject[results.size()]);
     }
 
     private XmlObject[] evaluate(String expression) {
         StringBuilder sb = new StringBuilder();
         for (String prefix : xpathNamespaces.getPrefixes()) {
             sb.append("declare namespace ");
             sb.append(prefix);
             sb.append("='");
             sb.append(xpathNamespaces.getNamespaceURI(prefix));
             sb.append("';");
         }
         sb.append('.');
         sb.append(expression);
         return bean.selectPath(sb.toString());
     }
 
     @SuppressWarnings("unchecked")
     protected <T> T convert(Class<T> expectedType, XmlObject object, String expression) {
         T value;
         if (object == null) {
             // Leave as null
             value = null;
         } else if (expectedType.isAssignableFrom(object.getClass())) {
             value = (T) object;
         } else if (conversionManager.hasConverter(expectedType)) {
             try {
                 value = conversionManager.convert(object, expectedType);
             } catch (IllegalArgumentException e) {
                 throw new ValueConfigurationException(format(
                         "Conversion failure"), expectedType, expression, e);
             }
         } else {
             throw new ValueConfigurationException(format(
                     "No conversion available from type '%s'", object.getClass()
                     .getName()), expectedType, expression);
         }
         return value;
     }
 }
