 /*
                         QueryJ
 
     Copyright (C) 2002-2005  Jose San Leandro Armend&aacute;riz
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
                     Urb. Valdecaba&ntilde;as
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile: $
  *
  * Author: Jose San Leandro Armend&aacute;riz
  *
  * Description: Provides some useful methods when filling up templates.
  *
  */
 package org.acmsl.queryj.tools.templates;
 
 /*
  * Importing some project classes.
  */
 import org.acmsl.queryj.tools.customsql.CustomSqlProvider;
 import org.acmsl.queryj.tools.customsql.Sql;
 import org.acmsl.queryj.tools.customsql.ResultElement;
 import org.acmsl.queryj.tools.customsql.ResultRefElement;
 import org.acmsl.queryj.tools.metadata.CachingResultDecorator;
 import org.acmsl.queryj.tools.metadata.DecoratorFactory;
 import org.acmsl.queryj.tools.metadata.MetadataManager;
 import org.acmsl.queryj.tools.metadata.MetadataTypeManager;
 import org.acmsl.queryj.tools.templates.dao.DAOTemplateUtils;
 
 /*
  * Importing ACM-SL Commons classes.
  */
 import org.acmsl.commons.patterns.Utils;
 
 /*
  * Importing some Apache Commons-Logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /*
  * Importing some JDK classes.
  */
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * Provides some useful methods when filling up templates.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro Armend&aacute;riz</a>
  */
 public class TemplateUtils
     implements  Utils
 {
     /**
      * Singleton implemented as a weak reference.
      */
     private static WeakReference singleton;
 
     /**
      * Protected constructor to avoid accidental instantiation.
      */
     protected TemplateUtils() {};
 
     /**
      * Specifies a new weak reference.
      * @param utils the utils instance to use.
      */
     protected static void setReference(final TemplateUtils utils)
     {
         singleton = new WeakReference(utils);
     }
 
     /**
      * Retrieves the weak reference.
      * @return such reference.
      */
     protected static WeakReference getReference()
     {
         return singleton;
     }
 
     /**
      * Retrieves a <code>TemplateUtils</code> instance.
      * @return such instance.
      */
     public static TemplateUtils getInstance()
     {
         TemplateUtils result = null;
 
         WeakReference reference = getReference();
 
         if  (reference != null) 
         {
             result = (TemplateUtils) reference.get();
         }
 
         if  (result == null) 
         {
             result = new TemplateUtils();
 
             setReference(result);
         }
 
         return result;
     }
 
     /**
      * Retrieves the custom selects.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom selects.
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomSelects(
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         return
             retrieveCustomSelects(
                 null,
                 customSqlProvider,
                 metadataManager,
                 metadataTypeManager,
                 decoratorFactory,
                 daoTemplateUtils);
     }
 
     /**
      * Retrieves the custom selects.
      * @param tableName the table name, or <code>null</code> for
      * repository-wide results.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom selects.
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomSelects(
         final String tableName,
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         return
             retrieveCustomSql(
                 new String[]
                 {
                     Sql.SELECT,
                 },
                 tableName,
                 customSqlProvider,
                 metadataManager,
                 metadataTypeManager,
                 decoratorFactory,
                 daoTemplateUtils);
     }
 
     /**
      * Retrieves the custom updates or inserts.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom sql.
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomUpdatesOrInserts(
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         return
             retrieveCustomUpdatesOrInserts(
                 null,
                 customSqlProvider,
                 metadataManager,
                 metadataTypeManager,
                 decoratorFactory,
                 daoTemplateUtils);
     }
 
     /**
      * Retrieves the custom updates or inserts.
      * @param tableName the table name.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom sql.
      * @precondition tableName != null
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomUpdatesOrInserts(
         final String tableName,
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         return
             retrieveCustomSql(
                 new String[]
                 {
                     Sql.INSERT,
                     Sql.UPDATE,
                     Sql.DELETE
                 },
                 tableName,
                 customSqlProvider,
                 metadataManager,
                 metadataTypeManager,
                 decoratorFactory,
                 daoTemplateUtils);
     }
 
     /**
      * Retrieves the custom selects.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom selects.
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomSelectsForUpdate(
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         return
             retrieveCustomSelectsForUpdate(
                 null,
                 customSqlProvider,
                 metadataManager,
                 metadataTypeManager,
                 decoratorFactory,
                 daoTemplateUtils);
     }
 
     /**
      * Retrieves the custom selects.
      * @param tableName the table name.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom selects.
      * @precondition tableName != null
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomSelectsForUpdate(
         final String tableName,
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         return
             retrieveCustomSql(
                 new String[]
                 {
                     Sql.SELECT_FOR_UPDATE,
                 },
                 tableName,
                 customSqlProvider,
                 metadataManager,
                 metadataTypeManager,
                 decoratorFactory,
                 daoTemplateUtils);
     }
 
     /**
      * Retrieves the custom sql.
      * @param types the sql types.
      * @param tableName the table name, or <code>null</code> for
      * repository-wide results.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param metadataTypeManager the metadata type manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom sql.
      * @precondition sqlTypes != null
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition metadataTypeManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomSql(
         final String[] types,
         final String tableName,
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final MetadataTypeManager metadataTypeManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         Collection result = new ArrayList();
 
         Collection t_cContents = customSqlProvider.getCollection();
 
         if  (t_cContents != null)
         {
             Iterator t_itContentIterator = t_cContents.iterator();
 
             if  (t_itContentIterator != null)
             {
                 int t_iTypes = (types != null) ? types.length : 0;
 
                 while  (t_itContentIterator.hasNext())
                 {
                     Object t_Content = t_itContentIterator.next();
 
                     boolean t_bMatches;
 
                     if  (t_Content instanceof Sql)
                     {
                         Sql t_Sql = (Sql) t_Content;
 
                         String t_strDao = t_Sql.getDao();
 
                        if  (   (t_strDao != null)
                             && (tableName != null))
                         {
                             t_bMatches =
                                 daoTemplateUtils.matches(
                                     tableName, t_strDao);
                         }
                         else
                         {
                             t_bMatches = (t_Sql.getRepositoryScope() != null);
                         }
 
                         if  (t_bMatches)
                         {
                             boolean t_bAdd = false;
 
                             String t_strCurrentType = null;
 
                             for  (int t_iIndex = 0;
                                       t_iIndex < t_iTypes;
                                       t_iIndex++)
                             {
                                 t_strCurrentType = types[t_iIndex];
 
                                 if  (   (t_strCurrentType != null)
                                      && (t_strCurrentType.equals(
                                              t_Sql.getType())))
                                 {
                                     t_bAdd = true;
                                 }
                             }
 
                             if  (t_bAdd)
                             {
                                 result.add(
                                     decoratorFactory.createDecorator(
                                         t_Sql,
                                         customSqlProvider,
                                         metadataManager));
                             }
                         }
                     }
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Retrieves the custom results.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom results.
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomResults(
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         return
             retrieveCustomResults(
                 null,
                 customSqlProvider,
                 metadataManager,
                 decoratorFactory,
                 daoTemplateUtils);
     }
 
     /**
      * Retrieves the custom results.
      * @param tableName the table name, or <code>null</code> for
      * repository-wide results.
      * @param customSqlProvider the provider.
      * @param metadataManager the database metadata manager.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @return the custom results.
      * @precondition customSqlProvider != null
      * @precondition metadataManager != null
      * @precondition decoratorFactory != null
      * @precondition daoTemplateUtils != null
      */
     public Collection retrieveCustomResults(
         final String tableName,
         final CustomSqlProvider customSqlProvider,
         final MetadataManager metadataManager,
         final DecoratorFactory decoratorFactory,
         final DAOTemplateUtils daoTemplateUtils)
     {
         Collection result = new ArrayList();
 
         Collection t_cContents = customSqlProvider.getCollection();
 
         if  (t_cContents != null)
         {
             Object t_Content = null;
             Sql t_Sql = null;
             ResultRefElement t_ResultRefElement = null;
             ResultElement t_ResultElement = null;
             String t_strDao;
             boolean t_bMatches;
 
             Iterator t_itContentIterator = t_cContents.iterator();
 
             if  (t_itContentIterator != null)
             {
                 while  (t_itContentIterator.hasNext())
                 {
                     t_Content = t_itContentIterator.next();
 
                     if  (t_Content instanceof Sql)
                     {
                         t_Sql = (Sql) t_Content;
 
                         t_strDao = t_Sql.getDao();
 
                        if  (   (t_strDao != null)
                             && (tableName != null))
                         {
                             t_bMatches =
                                 daoTemplateUtils.matches(
                                     tableName, t_strDao);
                         }
                         else
                         {
                             t_bMatches = (t_Sql.getRepositoryScope() != null);
                         }
 
                         if  (t_bMatches)
                         {
                             t_ResultRefElement = t_Sql.getResultRef();
 
                             if  (t_ResultRefElement != null)
                             {
                                 t_ResultElement =
                                     customSqlProvider.resolveReference(
                                         t_ResultRefElement);
 
                                 if  (t_ResultElement != null)
                                 {
                                     if  (!result.contains(t_ResultElement))
                                     {
                                         result.add(
                                             new CachingResultDecorator(
                                                 t_ResultElement,
                                                 customSqlProvider,
                                                 metadataManager,
                                                 decoratorFactory));
                                     }
                                 }
                                 else
                                 {
                                     try
                                     {
                                         // todo throw something.
                                         LogFactory.getLog(TemplateUtils.class)
                                             .warn(
                                                 "Referenced result not found:"
                                                 + t_ResultRefElement.getId());
                                     }
                                     catch  (final Throwable throwable)
                                     {
                                         // class-loading problem.
                                     }
                                 }
                             }
                             else
                             {
                                 try
                                 {
                                     // todo throw something.
                                     LogFactory.getLog(TemplateUtils.class).warn(
                                           "Referenced result not found:"
                                         + t_ResultRefElement.getId());
                                 }
                                 catch  (final Throwable throwable)
                                 {
                                     // class-loading problem.
                                 }
                             }
                         }
                     }
                 }
             }
         }
 
         return result;
     }
 }
