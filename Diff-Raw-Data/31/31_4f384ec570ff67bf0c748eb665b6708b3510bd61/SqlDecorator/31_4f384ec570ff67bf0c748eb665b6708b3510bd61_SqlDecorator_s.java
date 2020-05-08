 /*
                         QueryJ
 
     Copyright (C) 2002-2005  Jose San Leandro Armendariz
                         chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: chous@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Decorates <sql> elements in custom-sql models.
  *
  */
 package org.acmsl.queryj.tools.metadata;
 
 /*
  * Importing project-specific classes.
  */
 import org.acmsl.queryj.tools.customsql.CustomSqlProvider;
 import org.acmsl.queryj.tools.customsql.ParameterElement;
 import org.acmsl.queryj.tools.customsql.ParameterRefElement;
 import org.acmsl.queryj.tools.customsql.ResultElement;
 import org.acmsl.queryj.tools.customsql.ResultRefElement;
 import org.acmsl.queryj.tools.customsql.SqlElement;
 import org.acmsl.queryj.tools.metadata.MetadataTypeManager;
 import org.acmsl.queryj.tools.metadata.ParameterDecorator;
 
 /*
  * Importing JDK classes.
  */
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 /*
  * Importing Apache Commons-Logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Decorates &lt;sql&gt; elements in <i>custom-sql</i> models.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public class SqlDecorator
     extends  SqlElement
 {
     /**
      * The class used to multiple results.
      */
     public static final String MULTIPLE_RESULT_CLASS = "List";
     
     /**
      * The custom sql provider.
      * @todo remove this.
      */
     private CustomSqlProvider m__CustomSqlProvider;
 
     /**
      * The metadata type manager.
      */
     private MetadataTypeManager m__MetadataTypeManager;
     
     /**
      * Creates a <code>SqlDecorator</code> with given information.
      * @param sqlElement the <code>SqlElement</code> to decorate.
      * @param customSqlProvider the <code>CustomSqlProvider</code>, required
      * to decorate referred parameters.
      * @param metadataTypeManager the metadata type manager.
      * @precondition sqlElement != null
      * @precondition customSqlProvider != null
      * @precondition metadataTypeManager != null
      */
     public SqlDecorator(
         final SqlElement sqlElement,
         final CustomSqlProvider customSqlProvider,
         final MetadataTypeManager metadataTypeManager)
     {
         super(
             sqlElement.getId(),
             sqlElement.getDao(),
             sqlElement.getName(),
             sqlElement.getType(),
             sqlElement.getImplementation(),
             sqlElement.getValidate());
         immutableSetDescription(sqlElement.getDescription());
         immutableSetValue(sqlElement.getValue());
         immutableSetParameterRefs(sqlElement.getParameterRefs());
         immutableSetResultRef(sqlElement.getResultRef());
         immutableSetConnectionFlagsRef(sqlElement.getConnectionFlagsRef());
         immutableSetStatementFlagsRef(sqlElement.getStatementFlagsRef());
         immutableSetResultSetFlagsRef(sqlElement.getResultSetFlagsRef());
         immutableSetCustomSqlProvider(customSqlProvider);
         immutableSetMetadataTypeManager(metadataTypeManager);
     }
 
     /**
      * Specifies the custom SQL provider.
      * @param customSqlProvider such provider.
      */
     protected final void immutableSetCustomSqlProvider(
         final CustomSqlProvider customSqlProvider)
     {
         m__CustomSqlProvider = customSqlProvider;
     }
     
     /**
      * Specifies the custom SQL provider.
      * @param customSqlProvider such provider.
      */
     protected void setCustomSqlProvider(
         final CustomSqlProvider customSqlProvider)
     {
         immutableSetCustomSqlProvider(customSqlProvider);
     }
     
     /**
      * Retrieves the custom SQL provider.
      * @return such provider.
      */
     protected CustomSqlProvider getCustomSqlProvider()
     {
         return m__CustomSqlProvider;
     }
 
     /**
      * Specifies the metadata type manager.
      * @param metadataTypeManager such manager.
      */
     protected final void immutableSetMetadataTypeManager(
         final MetadataTypeManager metadataTypeManager)
     {
         m__MetadataTypeManager = metadataTypeManager;
     }
     
     /**
      * Specifies the metadata type manager.
      * @param metadataTypeManager such manager.
      */
     protected void setMetadataTypeManager(
         final MetadataTypeManager metadataTypeManager)
     {
         immutableSetMetadataTypeManager(metadataTypeManager);
     }
     
     /**
      * Retrieves the metadata type manager.
      * @return such manager.
      */
     protected MetadataTypeManager getMetadataTypeManager()
     {
         return m__MetadataTypeManager;
     }
 
     /**
      * Retrieves the value, in multiple lines.
      * @return such output.
      */
     public String[] getSplittedQuotedValue()
     {
         return splitAndQuote(getValue(), DecorationUtils.getInstance());
     }
     
     /**
      * Splits given value into several lines, quoting each one.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the splitted value.
      * @precondition value != null
      * @precondition decorationUtils != null
      */
     protected String[] splitAndQuote(
         final String value, final DecorationUtils decorationUtils)
     {
         return
             decorationUtils.surround(
                 decorationUtils.trim(
                     decorationUtils.split(
                         decorationUtils.escape(value, '\"'))),
                 "\"",
                 " \"");
     }
     
     /**
      * Retrieves the id formatted as a constant.
      * @return such information.
      */
     public String getIdAsConstant()
     {
         return uppercase(normalize(getId(), DecorationUtils.getInstance()));
     }
     
     /**
      * Normalizes given value.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the value, after being processed.
      * @precondition value != null
      * @precondition decorationUtils != null
      */
     protected String normalize(
         final String value, final DecorationUtils decorationUtils)
     {
         return decorationUtils.normalize(value);
     }
     
     /**
      * Retrieves the id capitalized.
      * @return such information.
      */
     public String getIdCapitalized()
     {
         return capitalize(getId(), DecorationUtils.getInstance());
     }
     
     /**
      * Transforms given value to upper case.
      * @param value the value.
      * @return <code>value.toUpperCase()</code>.
      * @precondition value != null
      */
     protected String uppercase(final String value)
     {
         return value.toUpperCase();
     }
 
     /**
      * Retrieves the name, (un)capitalized.
      * @return such information.
      */
     public String getNameUncapitalized()
     {
         return uncapitalize(getName(), DecorationUtils.getInstance());
     }
     
     /**
      * (Un)capitalizes given value.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the value, after being processed.
      * @precondition value != null
      * @precondition decorationUtils != null
      */
     protected String uncapitalize(
         final String value, final DecorationUtils decorationUtils)
     {
         return decorationUtils.uncapitalize(value);
     }
     
     /**
      * Capitalizes given value.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return the value, after being processed.
      * @precondition value != null
      * @precondition decorationUtils != null
      */
     protected String capitalize(
         final String value, final DecorationUtils decorationUtils)
     {
         return decorationUtils.capitalize(value);
     }
     
     /**
      * Retrieves the parameters.
      * @return such information.
      */
     public Collection getParameters()
     {
         return
             getParameters(
                 getParameterRefs(),
                 getCustomSqlProvider(),
                 getMetadataTypeManager());
     }
     
     /**
      * Retrieves the parameters.
      * @todo fix reference to customSqlProvider.
      * @param parameterRefs the parameter references.
      * @param customSqlProvider the <code>CustomSqlProvider</code>.
      * @param metadataTypeManager the metadata type manager.
      * @return such information.
      * @precondition customSqlProvider != null
      * @precondition metadataTypeManager != null
      */
     protected Collection getParameters(
         final Collection parameterRefs,
         final CustomSqlProvider customSqlProvider,
         final MetadataTypeManager metadataTypeManager)
     {
         Collection result = new ArrayList();
 
         if  (parameterRefs != null)
         {
             Iterator t_ParameterRefIterator = parameterRefs.iterator();
             
             if  (t_ParameterRefIterator != null)
             {
                 ParameterRefElement t_ParameterRef = null;
                 ParameterElement t_Parameter = null;
                 
                 while  (t_ParameterRefIterator.hasNext())
                 {
                     t_ParameterRef =
                         (ParameterRefElement) t_ParameterRefIterator.next();
 
                     if  (t_ParameterRef != null)
                     {
                         t_Parameter =
                            customSqlProvider.resolveReference(t_ParameterRef);
 
                         result.add(
                             new ParameterDecorator(
                                 t_Parameter, metadataTypeManager));
                     }
                     else
                     {
                         try
                         {
                             // todo throw something.
                             LogFactory.getLog("custom-sql").warn(
                                   "Referenced parameter not found:"
                                 + t_ParameterRef.getId());
                         }
                         catch  (final Throwable throwable)
                         {
                             // class-loading problem.
                         }
                     }
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Retrieves the result class.
      * @return such information.
      */
     public String getResultClass()
     {
         return getResultClass(getResultRef(), getCustomSqlProvider());
     }
     
     /**
      * Retrieves the result class.
      * @param resultRef the result ref.
      * @param customSqlProvider the custom sql provider.
      * @return such information.
      * @precondition customSqlProvider != null
      */
     protected String getResultClass(
         final ResultRefElement resultRef,
         final CustomSqlProvider customSqlProvider)
     {
         String result = null;
 
         if  (resultRef != null)
         {
             ResultElement t_Result =
                 customSqlProvider.resolveReference(resultRef);
 
             if  (t_Result != null)
             {
                 if  (ResultElement.MULTIPLE.equalsIgnoreCase(
                          t_Result.getMatches()))
                 {
                     result = MULTIPLE_RESULT_CLASS;
                 }
                 else
                 {
                     result = t_Result.getClassValue();
                 }
             }
             else
             {
                 try
                 {
                     // todo throw something.
                     LogFactory.getLog("custom-sql").warn(
                           "Referenced result not found:"
                         + resultRef.getId());
                 }
                 catch  (final Throwable throwable)
                 {
                     // class-loading problem.
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Retrieves the result id as constant.
      * @return such information.
      */
     public String getResultIdAsConstant()
     {
         return
             getResultIdAsConstant(
                 getResultRef(),
                 getCustomSqlProvider(),
                 DecorationUtils.getInstance());
     }
     
     /**
      * Retrieves the result id as constant.
      * @param resultRef the result ref.
      * @param customSqlProvider the custom sql provider.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return such information.
      * @precondition customSqlProvider != null
      * @precondition decorationUtils != null
      */
     protected String getResultIdAsConstant(
         final ResultRefElement resultRef,
         final CustomSqlProvider customSqlProvider,
         final DecorationUtils decorationUtils)
     {
         String result = null;
 
         if  (resultRef != null)
         {
             ResultElement t_Result =
                 customSqlProvider.resolveReference(resultRef);
 
             if  (t_Result != null)
             {
                 result =
                     uppercase(normalize(t_Result.getId(), decorationUtils));
             }
             else
             {
                 try
                 {
                     // todo throw something.
                     LogFactory.getLog("custom-sql").warn(
                           "Referenced result not found:"
                         + resultRef.getId());
                 }
                 catch  (final Throwable throwable)
                 {
                     // class-loading problem.
                 }
             }
         }
 
         return result;
     }
 }
