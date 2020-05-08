 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: ValueSourceFactory.java,v 1.7 2002-12-26 19:32:50 shahid.shah Exp $
  */
 
 package com.netspective.sparx.util.value;
 
import java.util.*;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.netspective.sparx.util.factory.Factory;
 
 /**
  * Factory that creates instances of ValueSource classes.
  */
 public class ValueSourceFactory implements Factory
 {
     public final static char VALUE_SOURCE_ID_DELIM = ':';
     public final static char VALUE_SOURCE_ID_DELIM_ESCAPE = '\\';
 
     private static Map srcClasses = new HashMap();
     private static Map srcInstances = new HashMap();
 
     static
     {
         srcClasses.put("data-source-entries", DataSourceEntriesListValue.class);
         srcClasses.put("form", DialogFieldValue.class);
         srcClasses.put("formOrRequest", DialogFieldOrRequestParameterValue.class); // legacy
         srcClasses.put("formOrRequestAttr", DialogFieldOrRequestAttributeValue.class); // legacy
         srcClasses.put("form-or-request", DialogFieldOrRequestParameterValue.class);
         srcClasses.put("form-or-request-attr", DialogFieldOrRequestAttributeValue.class);
         srcClasses.put("query", QueryResultsListValue.class);
         srcClasses.put("query-cols", QueryColumnsListValue.class);
         srcClasses.put("dialog-field-types", DialogFieldFactoryListValue.class);
         srcClasses.put("dialogs", DialogsListValue.class);
         srcClasses.put("query-defn-fields", QueryDefnFieldsListValue.class);
         srcClasses.put("query-defn-selects", QueryDefnSelectsListValue.class);
         srcClasses.put("sql-comparisons", SqlComparisonsListValue.class);
         srcClasses.put("strings", StringsListValue.class);
         srcClasses.put("string", StaticValue.class);
         srcClasses.put("static", StaticValue.class);
         srcClasses.put("request", RequestParameterValue.class);
         srcClasses.put("request-param", RequestParameterValue.class);
         srcClasses.put("request-attr", RequestAttributeValue.class);
         srcClasses.put("session", SessionAttributeValue.class);
         srcClasses.put("schema-tables", SchemaDocTablesListValue.class);
         srcClasses.put("schema-enum", SchemaDocEnumDataListValue.class);
         srcClasses.put("generate-id", GenerateIdValue.class);
         srcClasses.put("system-property", SystemPropertyValue.class);
         srcClasses.put("servlet-context-init-param", ServletContextInitParamValue.class);
         srcClasses.put("servlet-context-path", ServletContextPathValue.class);
         srcClasses.put("create-app-url", ServletContextUriValue.class);
         srcClasses.put("config", ConfigurationValue.class);
         srcClasses.put("config-expr", ConfigurationExprValue.class);
         srcClasses.put("simple-expr", ConfigurationExprValue.class);
         srcClasses.put("filesystem-entries", FilesystemEntriesListValue.class);
         srcClasses.put("create-data-cmd-heading", DialogDataCmdExprValue.class);
         srcClasses.put("create-record-add-text", RecordAddExprValue.class);
         srcClasses.put("report-record-url", RecordEditorUrlValue.class);
         srcClasses.put("java", JavaExpressionValue.class);
         srcClasses.put("custom-sql", CustomSqlValue.class); /* special-purpose ValueSource used only in DmlTask.java */
        srcClasses.put("image-src", ImageSourceValue.class); /* special-purpose ValueSource used only in DmlTask.java */
     }
 
     public static Map getValueSourceClasses()
     {
         return srcClasses;
     }
 
     public static void addValueSourceClass(String vsName, Class cls)
     {
         srcClasses.put(vsName, cls);
     }
 
     public static void createCatalog(Element parent)
     {
         Document doc = parent.getOwnerDocument();
         Element factoryElem = doc.createElement("factory");
         parent.appendChild(factoryElem);
         factoryElem.setAttribute("name", "Value Sources");
         factoryElem.setAttribute("class", ValueSourceFactory.class.getName());
         for(Iterator i = srcClasses.entrySet().iterator(); i.hasNext();)
         {
             Map.Entry entry = (Map.Entry) i.next();
             String vsId = (String) entry.getKey();
             Class vsClass = (Class) entry.getValue();
 
             Element childElem = doc.createElement("value-source");
             childElem.setAttribute("name", vsId);
             childElem.setAttribute("class", ((Class) entry.getValue()).getName());
             factoryElem.appendChild(childElem);
 
             try
             {
                 Object inst = vsClass.newInstance();
 
                 boolean isSVS = inst instanceof SingleValueSource;
                 boolean isLVS = inst instanceof ListValueSource;
                 boolean allowWrite = isSVS ? ((SingleValueSource) inst).supportsSetValue() : false;
                 SingleValueSource.Documentation vsDoc = isSVS ? ((SingleValueSource) inst).getDocumentation() : ((ListValueSource) inst).getDocumentation();
 
                 childElem.setAttribute("allow-write", allowWrite ? "yes" : "no");
                 childElem.setAttribute("svs", isSVS ? "yes" : "no");
                 childElem.setAttribute("lvs", isLVS ? "yes" : "no");
 
                 if(vsDoc != null)
                 {
                     childElem.setAttribute("params", vsDoc.getParamsHtml(vsId));
                     String descr = vsDoc.getDescription();
                     if(descr != null)
                     {
                         Element vsDescrElem = doc.createElement("descr");
                        vsDescrElem.appendChild(doc.createTextNode(vsDoc.getDescription()));
                         childElem.appendChild(vsDescrElem);
                     }
                 }
             }
             catch(Exception e)
             {
                 Element vsDescrElem = doc.createElement("descr");
                vsDescrElem.appendChild(doc.createTextNode(e.toString()));
                 childElem.appendChild(vsDescrElem);
             }
         }
     }
 
     public static class ValueSourceTokens
     {
         private String source;
         private int delimPos;
         private String idOrClassName;
         private String params;
         private boolean valid;
         private boolean escaped;
 
         public ValueSourceTokens(String source)
         {
             this.source = source;
             delimPos = source.indexOf(VALUE_SOURCE_ID_DELIM);
             valid = delimPos >= 0;
 
             if(valid)
             {
                 if(delimPos > 0 && source.charAt(delimPos-1) == VALUE_SOURCE_ID_DELIM_ESCAPE)
                 {
                     escaped = true;
                     valid = false;
                 }
                 else
                 {
                     idOrClassName = source.substring(0, delimPos);
                     params = source.substring(delimPos + 1);
                 }
             }
         }
 
         public StaticValue getStaticValueSource()
         {
             StaticValue result = null;
             if(escaped)
             {
                 StringBuffer sb = new StringBuffer(source);
                 sb.deleteCharAt(delimPos - 1);
                 result = new StaticValue(sb.toString());
             }
             else
                 result = new StaticValue(source);
             return result;
         }
 
         public String getSource()
         {
             return source;
         }
 
         public int getDelimPos()
         {
             return delimPos;
         }
 
         public String getIdOrClassName()
         {
             return idOrClassName;
         }
 
         public String getParams()
         {
             return params;
         }
 
         public boolean isValid()
         {
             return valid;
         }
 
         public boolean isEscaped()
         {
             return escaped;
         }
     }
 
     /**
      * Given a string of the format abc:def, parse the string into a value source Id ('abc') and value source
      * parameters ('def'). If the string contains a '\' (backslash) immediately prior to the ':' then the string is
      * not considered a value source string. Once parsed, check to see if 'abc' is an ID found in the srcClasses
      * private Map that contains value source Id keys that are mapped to ValueSource classes. If it's found, create
      * a new instance, pass along the parameters, and cache it. If the ID is not found in the map, check to if it is
      * a valid Java class that is found in the classpath -- if abc is a valid class, instantiate it and pass in the
      * value source parameters.
      */
     private static SingleValueSource getSingleValueSource(ValueSourceTokens vst)
     {
         SingleValueSource vs;
         Class vsClass = (Class) srcClasses.get(vst.idOrClassName);
         try
         {
             if(vsClass == null)
                 vsClass = Class.forName(vst.idOrClassName);
         }
         catch(ClassNotFoundException cnfe)
         {
             vsClass = null;
         }
 
         if(vsClass == null)
         {
             return new StaticValue("Value source '" + vst.idOrClassName + "' class not found in '"+ vst.source  +"'.");
         }
         else
         {
             try
             {
                 vs = (SingleValueSource) vsClass.newInstance();
                 vs.initializeSource(vst.params);
                 srcInstances.put(vst.source, vs);
             }
             catch(Exception e)
             {
                 return new StaticValue(e.toString());
             }
 
             return vs;
         }
     }
 
     public static SingleValueSource getSingleValueSource(String source)
     {
         SingleValueSource vs = (SingleValueSource) srcInstances.get(source);
         if(vs != null)
             return vs;
         else
         {
             ValueSourceTokens vst = new ValueSourceTokens(source);
             if(vst.valid)
                 return getSingleValueSource(vst);
             else
                 return null;
         }
     }
 
     public static SingleValueSource getStoreValueSource(String source)
     {
         return getSingleValueSource(source);
     }
 
     public static SingleValueSource getSingleOrStaticValueSource(String source)
     {
         ValueSourceTokens vst = new ValueSourceTokens(source);
         if(vst.valid)
             return getSingleValueSource(vst);
         else
             return vst.getStaticValueSource();
     }
 
     /**
      * Given a string of the format abc:def, parse the string into a value source Id ('abc') and value source
      * parameters ('def'). If the string contains a '\' (backslash) immediately prior to the ':' then the string is
      * not considered a value source string. Once parsed, check to see if 'abc' is an ID found in the srcClasses
      * private Map that contains value source Id keys that are mapped to ValueSource classes. If it's found, create
      * a new instance, pass along the parameters, and cache it. If the ID is not found in the map, check to if it is
      * a valid Java class that is found in the classpath -- if abc is a valid class, instantiate it and pass in the
      * value source parameters.
      */
     public static ListValueSource getListValueSource(String source)
     {
         ListValueSource vs = (ListValueSource) srcInstances.get(source);
         if(vs != null)
             return vs;
 
         ValueSourceTokens vst = new ValueSourceTokens(source);
         if(! vst.valid)
             return null;
 
         Class vsClass = (Class) srcClasses.get(vst.idOrClassName);
         try
         {
             if(vsClass == null)
                 vsClass = Class.forName(vst.idOrClassName);
         }
         catch(ClassNotFoundException cnfe)
         {
             vsClass = null;
         }
 
         if(vsClass == null)
         {
             return new ErrorListSource("List source '" + vst.idOrClassName + "' class not found.");
         }
         else
         {
             try
             {
                 vs = (ListValueSource) vsClass.newInstance();
                 vs.initializeSource(vst.params);
                 srcInstances.put(vst.idOrClassName, vs);
             }
             catch(Exception e)
             {
                 return new ErrorListSource(e);
             }
 
             return vs;
         }
     }
 }
