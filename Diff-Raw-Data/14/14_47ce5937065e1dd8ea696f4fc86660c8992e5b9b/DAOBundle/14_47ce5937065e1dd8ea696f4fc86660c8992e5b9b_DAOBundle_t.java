 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
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
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: DAOBundle.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Bundles the complete set of handlers related to DAO
  *              templates.
  *
  */
 package org.acmsl.queryj.tools.templates.dao;
 
 /*
  * Importing some project classes.
  */
 import org.acmsl.queryj.tools.templates.dao.handlers.AttributesStatementSetterTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.BaseAbstractDAOTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.BaseDAOFactoryTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.BaseDAOTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.BaseResultSetExtractorTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.ConfigurationPropertiesTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.CustomResultSetExtractorTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.DAOChooserTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.DAOFactoryTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.DAOListenerImplTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.DAOListenerTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.DAOTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.DAOTestTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.DataAccessManagerTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.DataAccessContextLocalTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.FkStatementSetterTemplateHandlerBundle;
import org.acmsl.queryj.tools.templates.dao.handlers.JdbcDAOTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.PkStatementSetterTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.handlers.ResultSetExtractorTemplateHandlerBundle;
 import org.acmsl.queryj.tools.templates.dao.mock.MockDAOBundle;
 import org.acmsl.queryj.tools.templates.dao.xml.XMLDAOBundle;
 import org.acmsl.queryj.tools.templates.handlers.TemplateHandlerBundle;
 
 /**
  * Bundles the complete set of handlers related to DAO templates.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  */
 public class DAOBundle
     extends  TemplateHandlerBundle
 {
     /**
      * Builds a bundle with DAO-related handlers.
      * @param includeMock whether to include Mock implementations.
      * @param includeXML whether to include XML implementations.
      */
     public DAOBundle(final boolean includeMock, final boolean includeXML)
     {
         super(
             new TemplateHandlerBundle[]
             {
                 new AttributesStatementSetterTemplateHandlerBundle(),
                 new BaseDAOFactoryTemplateHandlerBundle(),
                 new BaseDAOTemplateHandlerBundle(),
                 new BaseAbstractDAOTemplateHandlerBundle(),
                 new BaseResultSetExtractorTemplateHandlerBundle(),
                 new ConfigurationPropertiesTemplateHandlerBundle(),
                 new CustomResultSetExtractorTemplateHandlerBundle(),
                 new DAOChooserTemplateHandlerBundle(),
                 new DAOFactoryTemplateHandlerBundle(),
                 new DAOListenerTemplateHandlerBundle(),
                 new DAOListenerImplTemplateHandlerBundle(),
                 new DAOTemplateHandlerBundle(),
                 new DAOTestTemplateHandlerBundle(),
                 new DataAccessManagerTemplateHandlerBundle(),
                 new DataAccessContextLocalTemplateHandlerBundle(),
                 new FkStatementSetterTemplateHandlerBundle(),
 //                new JdbcDAOTemplateHandlerBundle(),
                 new PkStatementSetterTemplateHandlerBundle(),
 //                new QueryPreparedStatementCreatorTemplateHandlerBundle(),
                 new ResultSetExtractorTemplateHandlerBundle(),
                 (includeMock ? new MockDAOBundle() : null),
                 (includeXML ? new XMLDAOBundle() : null),
                 new DAOListenerTemplateHandlerBundle(),
                 new DAOListenerImplTemplateHandlerBundle(),
                new JdbcDAOTemplateHandlerBundle(),
 // TODO                new ThreadLocalBagTemplateHandlerBundle(),
 //                new ThreadAwareDataSourceWrapperTemplateHandlerBundle(),
 //                new TransactionManagerTemplateHandlerBundle(),
 //                new JndiUtilsTemplateHandlerBundle(),
 //                new DataSourceTransactionTokenTemplateHandlerBundle(),
 //                new StatisticsProviderTemplateHandlerBundle(),
 //                new BaseResultSetExtractorTemplateHandlerBundle()
             });
     }
 }
