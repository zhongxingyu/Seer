 /*
                         QueryJ
 
     Copyright (C) 2002  Jose San Leandro Armendariz
                         jsanleandro@yahoo.es
                         chousz@yahoo.com
 
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
     Contact info: jsanleandro@yahoo.es
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
  * Description: Is able to create engine-specific DAO interfaces for each
  *              table in the persistence model.
  *
  * Last modified by: $Author$ at $Date$
  *
  * File version: $Revision$
  *
  * Project version: $Name$
  *
  * $Id$
  *
  */
 package org.acmsl.queryj.tools.templates.dao;
 
 /*
  * Importing some project-specific classes.
  */
 import org.acmsl.queryj.tools.customsql.CustomSqlProvider;
 import org.acmsl.queryj.tools.customsql.ParameterElement;
 import org.acmsl.queryj.tools.customsql.ParameterRefElement;
 import org.acmsl.queryj.tools.customsql.PropertyElement;
 import org.acmsl.queryj.tools.customsql.PropertyRefElement;
 import org.acmsl.queryj.tools.customsql.ResultElement;
 import org.acmsl.queryj.tools.customsql.ResultRefElement;
 import org.acmsl.queryj.tools.customsql.SqlElement;
 import org.acmsl.queryj.tools.DatabaseMetaDataManager;
 import org.acmsl.queryj.tools.MetaDataUtils;
 import org.acmsl.queryj.tools.PackageUtils;
 import org.acmsl.queryj.tools.templates.TableTemplate;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.commons.regexpplugin.Helper;
 import org.acmsl.commons.regexpplugin.RegexpManager;
 import org.acmsl.commons.utils.EnglishGrammarUtils;
 import org.acmsl.commons.utils.StringUtils;
 import org.acmsl.commons.utils.StringValidator;
 
 /*
  * Importing some JDK classes.
  */
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /*
  * Importing Apache Commons Logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Is able to create engine-specific DAO interfaces for each
  * table in the persistence model.
  * @author <a href="mailto:jsanleandro@yahoo.es"
            >Jose San Leandro</a>
  * @version $Revision$
  */
 public abstract class DAOTemplate
 {
     /**
      * The default header.
      */
     public static final String DEFAULT_HEADER =
           "/*\n"
         + "                        QueryJ\n"
         + "\n"
         + "    Copyright (C) 2002  Jose San Leandro Armendariz\n"
         + "                        jsanleandro@yahoo.es\n"
         + "                        chousz@yahoo.com\n"
         + "\n"
         + "    This library is free software; you can redistribute it and/or\n"
         + "    modify it under the terms of the GNU General Public\n"
         + "    License as published by the Free Software Foundation; either\n"
         + "    version 2 of the License, or any later "
         + "version.\n"
         + "\n"
         + "    This library is distributed in the hope that it will be "
         + "useful,\n"
         + "    but WITHOUT ANY WARRANTY; without even the implied warranty "
         + "of\n"
         + "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
         + "GNU\n"
         + "    General Public License for more details.\n"
         + "\n"
         + "    You should have received a copy of the GNU General Public\n"
         + "    License along with this library; if not, write to the Free "
         + "Software\n"
         + "    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  "
         + "02111-1307  USA\n"
         + "\n"
         + "    Thanks to ACM S.L. for distributing this library under the GPL "
         + "license.\n"
         + "    Contact info: jsanleandro@yahoo.es\n"
         + "    Postal Address: c/Playa de Lagoa, 1\n"
         + "                    Urb. Valdecabanas\n"
         + "                    Boadilla del monte\n"
         + "                    28660 Madrid\n"
         + "                    Spain\n"
         + "\n"
         + " *****************************************************************"
         + "*************\n"
         + " *\n"
         + " * Filename: $" + "RCSfile: $\n"
         + " *\n"
         + " * Author: QueryJ\n"
         + " *\n"
         + " * Description: DAO layer responsible of retrieving\n"
         + "                {2} structures from "
          // Table name
         + "{0} {1} persistence layers.\n"
          // engine name - driver version
         + " *\n"
         + " * Last modified by: $" + "Author: $ at $" + "Date: $\n"
         + " *\n"
         + " * File version: $" + "Revision: $\n"
         + " *\n"
         + " * Project version: $" + "Name: $\n"
         + " *\n"
         + " * $" + "Id: $\n"
         + " *\n"
         + " */\n";
 
     /**
      * The package declaration.
      */
     public static final String PACKAGE_DECLARATION = "package {0};\n\n"; // package
 
     /**
      * The project imports.
      */
     public static final String DEFAULT_PROJECT_IMPORTS =
           "/*\n"
         + " * Importing project-specific classes.\n"
         + " */\n"
         + "import {0}.JdbcDAO;\n"
          // JDBC DAO package
         + "import {1}.{2}ValueObject;\n"
          // ValueObject DAO package - table name
         + "import {3}.{2}ValueObjectFactory;\n"
          // ValueObject factory DAO package - table name
         + "import {4}.{2}DAO;\n"
          // DAO interface package - table name
         + "import {5}.{6};\n"
          // Table repository package - Table repository name
         + "import {7}.DataAccessManager;\n"
          // data access manager package
         + "import {8}.{6}KeywordRepository;\n"
          // Keyword repository package - Table repository name
         + "{9}";
          // foreign DAO imports
 
     /**
      * Foreign DAO imports.
      */
     public static final String DEFAULT_FOREIGN_DAO_IMPORTS =
         "import {0}.{1}DAO;\n";
 
     /**
      * The ACM-SL imports.
      */
     public static final String ACMSL_IMPORTS =
           "\n/*\n"
         + " * Importing some ACM-SL classes.\n"
         + " */\n"
         + "import org.acmsl.commons.patterns.dao.DataAccessException;\n"
         + "import org.acmsl.queryj.dao.TransactionToken;\n"
         + "import org.acmsl.queryj.BigDecimalField;\n"
         + "import org.acmsl.queryj.CalendarField;\n"
         + "import org.acmsl.queryj.DeleteQuery;\n"
         + "import org.acmsl.queryj.DoubleField;\n"
         + "import org.acmsl.queryj.Field;\n"
         + "import org.acmsl.queryj.InsertQuery;\n"
         + "import org.acmsl.queryj.IntField;\n"
         + "import org.acmsl.queryj.LongField;\n"
         + "import org.acmsl.queryj.Query;\n"
         + "import org.acmsl.queryj.QueryFactory;\n"
         + "import org.acmsl.queryj.QueryResultSet;\n"
         + "import org.acmsl.queryj.QueryUtils;\n"
         + "import org.acmsl.queryj.SelectQuery;\n"
         + "import org.acmsl.queryj.StringField;\n"
         + "import org.acmsl.queryj.Table;\n"
         + "import org.acmsl.queryj.UpdateQuery;\n\n";
 
     /**
      * The JDK imports.
      */
     public static final String JDK_IMPORTS =
           "/*\n"
         + " * Importing some JDK classes.\n"
         + " */\n"
         + "import java.math.BigDecimal;\n"
         + "import java.sql.Connection;\n"
         + "import java.sql.PreparedStatement;\n"
         + "import java.sql.ResultSet;\n"
         + "import java.sql.SQLException;\n"
         + "import java.sql.Statement;\n"
         + "import java.util.Calendar;\n"
         + "import java.util.Date;\n\n";
 
     /**
      * The JDK extension imports.
      */
     public static final String JDK_EXTENSION_IMPORTS =
           "/*\n"
         + " * Importing some JDK extension classes\n"
         + " */\n"
         + "import javax.sql.DataSource;\n\n";
 
     /**
      * The logging imports.
      */
     public static final String LOGGING_IMPORTS =
           "/*\n"
         + " * Importing Jakarta Commons Loggig classes\n"
         + " */\n"
         + "import org.apache.commons.logging.LogFactory;\n\n";
     
     /**
      * The default class Javadoc.
      */
     public static final String DEFAULT_JAVADOC =
           "/**\n"
         + " * DAO class responsible of retrieving\n"
         + " * {2} structures from "
          // Table name
         + "{0} {1} persistence layers.\n"
          // engine name - driver version
         + " * @author <a href=\"http://maven.acm-sl.org/queryj\">QueryJ</a>\n"
         + " * @version $" + "Revision: $\n"
         + " */\n";
 
     /**
      * The class definition.
      */
     public static final String CLASS_DEFINITION =
           "public class {0}{1}DAO\n"
         + "    extends     JdbcDAO\n"
         + "    implements  {1}DAO\n";
         // engine name - table name
 
     /**
      * The class start.
      */
     public static final String DEFAULT_CLASS_START =
         "{\n";
 
     /**
      * The class constructor.
      */
     public static final String CLASS_CONSTRUCTOR =
           "    /**\n"
         + "     * Builds a {0}{1}DAO\n"
         // engine name - table name
         + "     * with given data source.\n"
         + "     * @param dataSource the required data source.\n"
         + "     * @precondition dateSource != null\n"
         + "     */\n"
         + "    public {0}{1}DAO(final DataSource dataSource)\n"
         + "    '{'\n"
         + "        super(dataSource);\n"
         + "    '}'\n\n";
         // engine name - table name
 
     /**
      * The find by primary key method.
      */
     public static final String DEFAULT_FIND_BY_PRIMARY_KEY_METHOD =
           "    /**\n"
         + "     * Loads {0} information from the persistence layer filtering\n"
          // table name
         + "     * by its primary keys."
         + "{1}\n"
          // FIND_BY_PRIMARY_KEY_PK_JAVADOC
         + "     * @param transactionToken needed to use an open connection and\n"
         + "     * see previously uncommited inserts/updates/deletes.\n"
         + "     * @return the information extracted from the persistence layer.\n"
         + "     * @throws DataAccessException if the access to the information fails.\n"
         + "     */\n"
         + "    public {2}ValueObject findByPrimaryKey("
          // java table name
         + "{3}\n"
          // FIND_BY_PRIMARY_KEY_PK_DECLARATION
         + "        final TransactionToken transactionToken)\n"
         + "      throws DataAccessException\n"
         + "    '{'\n"
         + "        {2}ValueObject result = null;\n\n"
          // java table name
         + "        Connection        t_Connection        = null;\n"
         + "        PreparedStatement t_PreparedStatement = null;\n\n"
         + "        try\n"
         + "        '{'\n"
         + "            t_Connection = getConnection(transactionToken);\n\n"
         + "            QueryFactory t_QueryFactory = QueryFactory.getInstance();\n\n"
         + "            if  (   (t_Connection   != null)\n"
         + "                 && (t_QueryFactory != null))\n"
         + "            '{'\n"
         + "                SelectQuery t_Query = t_QueryFactory.createSelectQuery();\n\n"
         + "{4}"
          // FIND_BY_PRIMARY_KEY_SELECT_FIELDS
         + "                t_Query.from({5}.{6});\n\n"
          // table repository name - table name
         + "{7}"
          // FIND_BY_PRIMARY_KEY_PK_FILTER_DECLARATION
         + "{8}"
          // FIND_BY_PRIMARY_KEY_PK_FILTER_VALUES
         + "                t_PreparedStatement = t_Query.prepareStatement(t_Connection);\n\n"
         + "                QueryResultSet t_Results = t_Query.retrieveMatchingResults();\n\n"
         + "                if  (   (t_Results != null)\n"
         + "                     && (t_Results.next()))\n"
         + "                '{'\n"
         + "                    result = build{2}(t_Results);\n\n"
         + "                    t_Results.close();\n"
         + "                '}'\n"
         + "            '}'\n"
         + "        '}'\n"
         + "        catch  (final SQLException sqlException)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).fatal(sqlException);\n"
         + "        '}'\n"
         + "        catch  (final Exception exception)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).error(exception);\n"
         + "        '}'\n"
         + "        finally\n"
         + "        '{'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_PreparedStatement != null)\n"
         + "                '{'\n"
         + "                    t_PreparedStatement.close();\n"
         + "                '}'\n"
         + "                if  (t_Connection != null)\n"
         + "                '{'\n"
         + "                    closeConnection(t_Connection, transactionToken);\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "        '}'\n\n"
         + "        return result;\n"
         + "    '}'\n\n";
 
     /**
      * The find-by-primary-key method's primary keys javadoc.
      */
     public static final String DEFAULT_FIND_BY_PRIMARY_KEY_PK_JAVADOC =
         "\n     * @param {0} the {1} value to filter.";
          // java pk - pk
 
     /**
      * The find-by-primary-key method's primary keys declaration.
      */
     public static final String DEFAULT_FIND_BY_PRIMARY_KEY_PK_DECLARATION =
         "\n        final {0} {1},";
          // pk type - java pk
 
     /**
      * The find-by-primary-key method's select fields.
      */
     public static final String DEFAULT_FIND_BY_PRIMARY_KEY_SELECT_FIELDS =
         "                t_Query.select({0}.{1}.{2});\n";
          // table repository name - table name - field name
 
     /**
      * The find-by-primary-key method's filter declaration.
      */
     public static final String DEFAULT_FIND_BY_PRIMARY_KEY_FILTER_DECLARATION = "";
     /*
         "                t_Query.where({0}.{1}.{2}.equals());\n\n";
          // table repository name - table name - pk field name
      */
 
     /**
      * The find-by-primary-key method's filter values.
      *
     public static final String DEFAULT_FIND_BY_PRIMARY_KEY_FILTER_VALUES =
           "                t_Query.set{0}(\n"
          // pk field type
         + "                    {1}.{2}.{3}.equals(),\n"
          // table repository name - table name - pk field name
         + "                    {4});\n\n";
          // java pk
      */
 
     /**
      * The find-by-primary-key method's filter values.
      */
     public static final String DEFAULT_FIND_BY_PRIMARY_KEY_FILTER_VALUES =
           "                t_Query.where(\n"
         + "                    {1}.{2}.{3}.equals({4}));\n\n";
          // table repository name - table name - field name - value
 
     /**
      * The build-value-object method.
      */
     public static final String BUILD_VALUE_OBJECT_METHOD =
           "    /**\n"
         + "     * Fills up a {0} with the information provided by given\n"
          // java table name
         + "     * result set.\n"
         + "     * @param queryResults the result set with the row values.\n"
         + "     * @return the {0} filled up with the persisted contents, or\n"
         + "     * <code>null</code> if something wrong occurs.\n"
         + "     * @exception SQLException if any invalid operation is performed\n"
         + "     * on the result set.\n"
         + "     */\n"
         + "    protected {0}ValueObject build{0}(final QueryResultSet queryResultSet)\n"
         + "        throws  SQLException\n"
         + "    '{'\n"
         + "        {0}ValueObject result = null;\n\n"
         + "        if  (queryResultSet != null) \n"
         + "        '{'\n"
         + "            {0}ValueObjectFactory t_{0}Factory =\n"
         + "                {0}ValueObjectFactory.getInstance();\n\n"
         + "            if  (t_{0}Factory != null) \n"
         + "            '{'\n"
         + "                result =\n"
         + "                    t_{0}Factory.create{0}("
         + "{1});\n"
          // BUILD_VALUE_OBJECT_VALUE_RETRIEVAL
         + "            '}'\n"
         + "        '}'\n\n"
         + "        return result;\n"
         + "    '}'\n\n";
 
     /**
      * The build-value-object method's value retrieval
      */
     public static final String BUILD_VALUE_OBJECT_VALUE_RETRIEVAL =
          "\n                        queryResultSet.{0}";
          //"\n                        queryResultSet.{0}(\n"
          // field type
          //+ "                            {1}.{2}.{3})";
          // table repository name - table name - field name
 
     /**
      * The store method.
      */
     public static final String DEFAULT_INSERT_METHOD =
           "    /**\n"
         + "     * Persists {0} information."
          // table name
         + "{1}"
          // (optional) pk javadoc
         + "{2}\n"
          // insert parameters javadoc
         + "     * @param transactionToken the transaction boundary.\n"
         + "     * @throws DataAccessException if the access to the information fails.\n"
         + "     */\n"
         + "    public void insert("
         + "{3}\n"
          // insert parameters declaration
         + "        final TransactionToken transactionToken)\n"
         + "      throws DataAccessException\n"
         + "    '{'\n"
         + "        Connection        t_Connection        = null;\n"
         + "        PreparedStatement t_PreparedStatement = null;\n\n"
         + "        try\n"
         + "        '{'\n"
         + "            t_Connection = getConnection(transactionToken);\n\n"
         + "            QueryFactory t_QueryFactory = QueryFactory.getInstance();\n\n"
         + "            {4}KeywordRepository t_KeywordRepository =\n"
         + "                {4}KeywordRepository.getInstance();\n\n"
         + "            if  (   (t_Connection        != null)\n"
         + "                 && (t_QueryFactory      != null)\n"
         + "                 && (t_KeywordRepository != null))\n"
         + "            '{'\n"
         + "                InsertQuery t_Query = t_QueryFactory.createInsertQuery();\n\n"
         + "                t_Query.insertInto({4}.{5});\n\n"
          // table repository name - table name
         + "{6}\n"
          // insert parameters specification
         + "                t_PreparedStatement = t_Query.prepareStatement(t_Connection);\n"
         + "                t_PreparedStatement.executeUpdate();\n"
         + "            '}'\n"
         + "        '}'\n"
         + "        catch  (final SQLException sqlException)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).fatal(sqlException);\n"
         + "        '}'\n"
         + "        catch  (final Exception exception)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).error(exception);\n"
         + "        '}'\n"
         + "        finally\n"
         + "        '{'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_PreparedStatement != null)\n"
         + "                '{'\n"
         + "                    t_PreparedStatement.close();\n"
         + "                '}'\n"
         + "                if  (t_Connection != null)\n"
         + "                '{'\n"
         + "                    closeConnection(t_Connection, transactionToken);\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "        '}'\n"
         + "    '}'\n";
 
     /**
      * The insert parameters javadoc.
      */
     public static final String DEFAULT_INSERT_PARAMETERS_JAVADOC =
         "\n     * @param {0} the {1} information.";
     // field name - field Name
 
     /**
      * The insert parameters declaration.
      */
     public static final String DEFAULT_INSERT_PARAMETERS_DECLARATION =
         "\n        final {0} {1}";
     // field type - field name
 
     /**
      * The insert parameters specification.
      */
     public static final String DEFAULT_INSERT_PARAMETERS_SPECIFICATION =
         "                t_Query.value({0}.{1}.{2}, {3});\n";
     // table repository name - table name - field name - field value
 
     /**
      * The insert keyword-based parameters specification.
      */
     public static final String DEFAULT_INSERT_KEYWORD_PARAMETERS_SPECIFICATION =
           "                t_Query.value(\n"
         + "                    {0}.{1}.{2},\n"
         + "                    t_KeywordRepository.{3}());\n";
     // table repository name - table name - field name - field value
 
     /**
      * The update method.
      */
     public static final String DEFAULT_UPDATE_METHOD =
           "    /**\n"
         + "     * Updates {0} information\n"
         + "     * in the persistence layer."
          // table name
         + "{1}"
          // (optional) pk javadoc
         + "{2}\n"
          // update parameters javadoc
         + "     * @param transactionToken the transaction boundary.\n"
         + "     * @throws DataAccessException if the access to the information fails.\n"
         + "     */\n"
         + "    public void update("
         + "{3}"
          // (optional) pk declaration
         + "{4}"
          // update parameters declaration
         + "\n        final TransactionToken transactionToken)\n"
         + "      throws DataAccessException\n"
         + "    '{'\n"
         + "        Connection        t_Connection        = null;\n"
         + "        PreparedStatement t_PreparedStatement = null;\n\n"
         + "        try\n"
         + "        '{'\n"
         + "            t_Connection = getConnection(transactionToken);\n\n"
         + "            QueryFactory t_QueryFactory = QueryFactory.getInstance();\n\n"
         + "            if  (   (t_Connection   != null)\n"
         + "                 && (t_QueryFactory != null))\n"
         + "            '{'\n"
         + "                UpdateQuery t_Query = t_QueryFactory.createUpdateQuery();\n\n"
         + "                t_Query.update({5}.{6});\n\n"
          // table repository name - table name
         + "{7}"
          // update parameters specification
         + "{8}"
          // pk values specificacion
         + "                t_PreparedStatement = t_Query.prepareStatement(t_Connection);\n"
         + "                t_PreparedStatement.executeUpdate();\n"
         + "            '}'\n"
         + "        '}'\n"
         + "        catch  (final SQLException sqlException)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).fatal(sqlException);\n"
         + "        '}'\n"
         + "        catch  (final Exception exception)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).error(exception);\n"
         + "        '}'\n"
         + "        finally\n"
         + "        '{'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_PreparedStatement != null)\n"
         + "                '{'\n"
         + "                    t_PreparedStatement.close();\n"
         + "                '}'\n"
         + "                if  (t_Connection != null)\n"
         + "                '{'\n"
         + "                    closeConnection(t_Connection, transactionToken);\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "        '}'\n"
         + "    '}'\n";
 
     /**
      * The update parameters javadoc.
      */
     public static final String DEFAULT_UPDATE_PARAMETERS_JAVADOC =
         "\n     * @param {0} the {1} information.";
     // field name - field Name
 
     /**
      * The update parameters declaration.
      */
     public static final String DEFAULT_UPDATE_PARAMETERS_DECLARATION =
         "\n        final {0} {1}";
     // field type - field name
 
     /**
      * The update parameters specification.
      */
     public static final String DEFAULT_UPDATE_PARAMETERS_SPECIFICATION =
         "                t_Query.set({0}.{1}.{2}, {3});\n";
     // table repository name - table name - field name - field value
 
     /**
      * The update filter.
      */
     public static final String DEFAULT_UPDATE_FILTER =
         "                t_Query.where({0}.{1}.{2}.equals({3}));\n";
     // table repository name - table name - field name - field value
 
     /**
      * The delete method.
      */
     public static final String DEFAULT_DELETE_METHOD =
           "    /**\n"
         + "     * Deletes {0} information from the persistence layer filtering\n"
          // table name
         + "     * by its primary keys."
         + "{1}\n"
          // DELETE_PK_JAVADOC
         + "     * @param transactionToken needed to use an open connection and\n"
         + "     * see previously uncommited inserts/updates/deletes.\n"
         + "     * @return <code>true</code> if the information has been deleted\n"
         + "     * successfully.\n"
         + "     * @throws DataAccessException if the access to the information fails.\n"
         + "     */\n"
         + "    {7} boolean delete{8}("
          // java table name
         + "{2}"
          // DELETE_PK_DECLARATION
         + "\n        final TransactionToken transactionToken)\n"
         + "      throws DataAccessException\n"
         + "    '{'\n"
         + "        boolean result = false;\n\n"
         + "        Connection        t_Connection        = null;\n"
         + "        PreparedStatement t_PreparedStatement = null;\n\n"
         + "        try\n"
         + "        '{'\n"
         + "            t_Connection = getConnection(transactionToken);\n\n"
         + "            QueryFactory t_QueryFactory = QueryFactory.getInstance();\n\n"
         + "            if  (   (t_Connection   != null)\n"
         + "                 && (t_QueryFactory != null))\n"
         + "            '{'\n"
         + "                DeleteQuery t_Query = t_QueryFactory.createDeleteQuery();\n\n"
         + "                t_Query.deleteFrom({3}.{4});\n\n"
          // table repository name - table name
         + "{5}"
 
          // DELETE_PK_FILTER_DECLARATION
         + "                t_PreparedStatement = t_Query.prepareStatement(t_Connection);\n\n"
         + "{6}"
          // DELETE_PK_FILTER_VALUES
         + "                t_PreparedStatement = t_Query.prepareStatement(t_Connection);\n"
         + "                t_PreparedStatement.executeUpdate();\n\n"
         + "                result = true;\n"
         + "            '}'\n"
         + "        '}'\n"
         + "        catch   (SQLException sqlException)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).fatal(sqlException);\n"
         + "        '}'\n"
         + "        catch   (Exception exception)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).error(exception);\n"
         + "        '}'\n"
         + "        finally\n"
         + "        '{'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_PreparedStatement != null)\n"
         + "                '{'\n"
         + "                    t_PreparedStatement.close();\n"
         + "                '}'\n"
         + "                if  (t_Connection != null)\n"
         + "                '{'\n"
         + "                    closeConnection(t_Connection, transactionToken);\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "        '}'\n\n"
         + "        return result;\n"
         + "    '}'\n\n";
 
     /**
      * The delete method's primary keys javadoc.
      */
     public static final String DEFAULT_DELETE_PK_JAVADOC =
         "\n     * @param {0} the {1} value to filter.";
          // java pk - pk
 
     /**
      * The delete method's primary keys declaration.
      */
     public static final String DEFAULT_DELETE_PK_DECLARATION =
         "        final {0}              {1},\n";
          // pk type - java pk
 
     /**
      * The delete method's filter declaration.
      */
     public static final String DEFAULT_DELETE_FILTER_DECLARATION =
         "                t_Query.where({0}.{1}.{2}.equals());\n\n";
          // table repository name - table name - pk field name
 
     /**
      * The delete method's filter values.
      */
     public static final String DEFAULT_DELETE_FILTER_VALUES =
           "                t_Query.set{0}(\n"
          // pk field type
         + "                    {1}.{2}.{3}.equals(),\n"
          // table repository name - table name - pk field name
         + "                    {4});\n\n";
          // java pk
 
     /**
      * The delete with fk method.
      */
     public static final String DEFAULT_DELETE_WITH_FK_METHOD =
           "    /**\n"
         + "     * Deletes {0} information from the persistence layer filtering\n"
          // table name
         + "     * by its primary keys."
         + "{1}\n"
          // DELETE_PK_JAVADOC
         + "     * @param transactionToken needed to use an open connection and\n"
         + "     * see previously uncommited inserts/updates/deletes.\n"
         + "     * @return <code>true</code> if the information has been deleted\n"
         + "     * successfully.\n"
         + "     * @throws DataAccessException if the access to the information fails.\n"
         + "     */\n"
         + "    public boolean delete("
         + "{2}"
          // DELETE_PK_DECLARATION
         + "\n        final TransactionToken transactionToken)\n"
         + "      throws DataAccessException\n"
         + "    '{'\n"
         + "        return\n"
         + "            delete(\n"
         + "                findByPrimaryKey("
         + "{3}"
         + "\n                    transactionToken),\n"
         + "                transactionToken);\n"
         + "    '}'\n\n"
         + "    /**\n"
         + "     * Deletes given {0} information from the persistence layer.\n"
          // table name
         + "     * @param {4} the information to delete.\n"
         + "     * @param transactionToken needed to use an open connection and\n"
         + "     * see previously uncommited inserts/updates/deletes.\n"
         + "     * @return <code>true</code> if the information has been deleted\n"
         + "     * successfully.\n"
         + "     * @precondition {4} != null\n"
         + "     */\n"
         + "    public boolean delete(\n"
         + "        final {0}ValueObject {4},\n"
         + "        final TransactionToken  transactionToken)\n"
         + "    '{'\n"
         + "        boolean result = false;\n\n"
         + "        TransactionToken  t_DeleteTransactionToken = transactionToken;\n"
         + "        boolean           t_bInnerTransaction      = false;\n\n"
         + "        if  (t_DeleteTransactionToken == null)\n"
         + "        '{'\n"
         + "            t_DeleteTransactionToken = createTransactionToken();\n"
         + "        '}'\n\n"
         + "        if  (   (t_DeleteTransactionToken != null)\n"
         + "             && (!t_DeleteTransactionToken.isTransactionAlive()))\n"
         + "        '{'\n"
         + "            t_DeleteTransactionToken.beginTransaction();\n"
         + "            t_bInnerTransaction = true;\n"
         + "        '}'\n\n"
         + "        try\n"
         + "        '{'\n"
         + "            DataAccessManager t_DataAccessManager = DataAccessManager.getInstance();\n"
         + "            if  (t_DataAccessManager != null)\n"
         + "            '{'\n"
         + "                result = true;\n\n"
         + "{5}\n"
         + "                if  (result)\n"
         + "                '{'\n"
         + "                    result =\n"
         + "                        delete{0}(\n"
         + "                            {4}.get{6}(),\n"
         + "                            t_DeleteTransactionToken);\n"
         + "                '}'\n"
         + "            '}'\n"
         + "        '}'\n"
         + "        catch  (final Exception exception)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).error(exception);\n"
         + "            result = false;\n"
         + "            if  (t_DeleteTransactionToken != null)\n"
         + "            '{'\n"
         + "                t_DeleteTransactionToken.setRollbackPending(true);\n"
         + "            '}'\n"
         + "        '}'\n"
         + "        finally\n"
         + "        '{'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (   (t_bInnerTransaction)\n"
         + "                     && (t_DeleteTransactionToken != null))\n"
         + "                '{'\n"
         + "                    if  (   (t_DeleteTransactionToken.isRollbackPending())\n"
         + "                         || (!result))\n"
         + "                    '{'\n"
         + "                        rollback(t_DeleteTransactionToken);\n"
         + "                    '}'\n"
         + "                    else\n"
         + "                    '{'\n"
         + "                        commit(t_DeleteTransactionToken);\n"
         + "                    '}'\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "        '}'\n\n"
         + "        return result;\n"
         + "    '}'\n\n";
 
     /**
      * The delete with fk method's primary keys javadoc.
      */
     public static final String DEFAULT_DELETE_WITH_FK_PK_JAVADOC =
         "\n     * @param {0} the {1} value to filter.";
          // java pk - pk
 
     /**
      * The delete with fk method's primary keys declaration.
      */
     public static final String DEFAULT_DELETE_WITH_FK_PK_DECLARATION =
         "        final {0}              {1},\n";
          // pk type - java pk
 
     /**
      * The delete with fk methods' FK DAO delete request.
      */
     public static final String DEFAULT_DELETE_WITH_FK_DAO_DELETE_REQUEST =
           "                if  (result)\n"
         + "                '{'\n"
         + "                    {0}DAO t_{0}DAO = t_DataAccessManager.get{0}DAO();\n\n"
         + "                    if  (t_{0}DAO != null)\n"
         + "                    '{'\n"
         + "                        result =\n"
         + "                            t_{0}DAO.delete(\n"
         + "                                {1}.get{2}(),\n"
         + "                                t_DeleteTransactionToken);\n"
         + "                    '}'\n"
         + "                    else\n"
         + "                    '{'\n"
         + "                        result = false;\n"
         + "                    '}'\n"
         + "                '}'\n"
         + "                else\n"
         + "                '{'\n"
         + "                    result = false;\n"
         + "                '}'\n";
 
     /**
      * The delete with fk methods' FK values.
      */
     public static final String DEFAULT_DELETE_WITH_FK_PK_VALUES =
         "\n                    {0},";
 
     /**
      * The custom select template.
      */
     public static final String DEFAULT_CUSTOM_SELECT =
           "    /**\n"
         + "     * Retrieves {0} entities\n"
          // result class
         + "     * from the persistence layer matching given criteria.\n"
         + "{1}\n"
          // CUSTOM_SELECT_PARAMETER_JAVADOC
         + "     * @param transactionToken needed to use an open connection and\n"
         + "     * see previously uncommited inserts/updates/deletes.\n"
         + "     * @return the information extracted from the persistence layer.\n"
         + "     * @throws DataAccessException if the access to the information fails.\n"
         + "     */\n"
         + "    public {0} {2}("
          // result class - sql name
         + "{3}\n"
          // CUSTOM_SELECT_PARAMETER_DECLARATION
         + "        final TransactionToken transactionToken)\n"
         + "      throws DataAccessException\n"
         + "    '{'\n"
         + "        {0} result = null;\n\n"
          // java table name
         + "        Connection        t_Connection        = null;\n"
         + "        PreparedStatement t_PreparedStatement = null;\n"
         + "        ResultSet         t_rsResults         = null;\n\n"
         + "        try\n"
         + "        '{'\n"
         + "            t_Connection = getConnection(transactionToken);\n\n"
         + "            String t_strQuery =\n"
         + "                \"{4}\";\n\n"
         + "            t_PreparedStatement = t_Connection.prepareStatement(t_strQuery);\n"
         + "{5}\n\n"
          // CUSTOM_SELECT_PARAMETER_VALUES
         + "            t_rsResults = t_PreparedStatement.executeQuery();\n\n"
         + "            if  (   (t_rsResults != null)\n"
         + "                 && (t_rsResults.next()))\n"
         + "            '{'\n"
         + "                {0}Factory t_Factory =\n"
         + "                    {0}Factory.getInstance();\n\n"
         + "                result =\n"
         + "                    t_Factory.create{6}("
         + "{7});\n"
          // CUSTOM_SELECT_RESULT_PROPERTIES
         + "            '}'\n"
         + "        '}'\n"
         + "        catch  (final SQLException sqlException)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).fatal(sqlException);\n"
         + "        '}'\n"
         + "        catch  (final Exception exception)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).error(exception);\n"
         + "        '}'\n"
         + "        finally\n"
         + "        '{'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_rsResults != null)\n"
         + "                '{'\n"
         + "                    t_rsResults.close();\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_PreparedStatement != null)\n"
         + "                '{'\n"
         + "                    t_PreparedStatement.close();\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_Connection != null)\n"
         + "                '{'\n"
         + "                    closeConnection(t_Connection, transactionToken);\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "        '}'\n\n"
         + "        return result;\n"
         + "    '}'\n\n";
 
     /**
      * The custom select parameter javadoc.
      */
     public static final String DEFAULT_CUSTOM_SELECT_PARAMETER_JAVADOC =
         "\n     * @param {0} the value to filter.";
          // parameter name
 
     /**
      * The custom select parameter declaration.
      */
     public static final String DEFAULT_CUSTOM_SELECT_PARAMETER_DECLARATION =
         "\n        final {0} {1},";
          // parameter type - parameter name
 
     /**
      * The custom select parameter values.
      */
     public static final String DEFAULT_CUSTOM_SELECT_PARAMETER_VALUES =
         "\n            t_PreparedStatement.set{0}({1}, {2});";
 
     /**
      * The custom select result properties.
      */
     public static final String DEFAULT_CUSTOM_SELECT_RESULT_PROPERTIES =
         "\n                        t_rsResults.get{0}({1})";
 
     /**
      * The custom update template.
      */
     public static final String DEFAULT_CUSTOM_UPDATE =
           "    /**\n"
         + "     * Performs the <i>{0}</i> operation\n"
          // query name
         + "     * in the persistence layer with the provided data."
         + "{1}\n"
          // CUSTOM_SELECT_PARAMETER_JAVADOC
         + "     * @param transactionToken needed to use an open connection and\n"
         + "     * see previously uncommited inserts/updates/deletes.\n"
         + "     * @return the information extracted from the persistence layer.\n"
         + "     * @throws DataAccessException if the access to the information fails.\n"
         + "     */\n"
         + "    public void {2}("
          // sql name
         + "{3}\n"
          // CUSTOM_UPDATE_PARAMETER_DECLARATION
         + "        final TransactionToken transactionToken)\n"
         + "      throws DataAccessException\n"
         + "    '{'\n"
          // java table name
         + "        Connection        t_Connection        = null;\n"
         + "        PreparedStatement t_PreparedStatement = null;\n\n"
         + "        try\n"
         + "        '{'\n"
         + "            t_Connection = getConnection(transactionToken);\n\n"
         + "            StringBuffer t_sbQuery = new StringBuffer();\n\n"
         + "{4}\n"
         + "            t_PreparedStatement =\n"
         + "                t_Connection.prepareStatement(t_sbQuery.toString());\n"
         + "{5}\n\n"
          // CUSTOM_UPDATE_PARAMETER_VALUES
         + "            t_PreparedStatement.executeUpdate();\n\n"
         + "        '}'\n"
         + "        catch  (final SQLException sqlException)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).fatal(sqlException);\n"
         + "        '}'\n"
         + "        catch  (final Exception exception)\n"
         + "        '{'\n"
         + "            LogFactory.getLog(getClass()).error(exception);\n"
         + "        '}'\n"
         + "        finally\n"
         + "        '{'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_PreparedStatement != null)\n"
         + "                '{'\n"
         + "                    t_PreparedStatement.close();\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "            try\n"
         + "            '{'\n"
         + "                if  (t_Connection != null)\n"
         + "                '{'\n"
         + "                    closeConnection(t_Connection, transactionToken);\n"
         + "                '}'\n"
         + "            '}'\n"
         + "            catch  (final Exception exception)\n"
         + "            '{'\n"
         + "                LogFactory.getLog(getClass()).error(exception);\n"
         + "            '}'\n"
         + "        '}'\n\n"
         + "    '}'\n\n";
 
     /**
      * The custom update parameter javadoc.
      */
     public static final String DEFAULT_CUSTOM_UPDATE_PARAMETER_JAVADOC =
         "\n     * @param {0} such information.";
          // parameter name
 
     /**
      * The custom update parameter declaration.
      */
     public static final String DEFAULT_CUSTOM_UPDATE_PARAMETER_DECLARATION =
         "\n        final {0} {1},";
          // parameter type - parameter name
 
     /**
      * The custom update parameter values.
      */
     public static final String DEFAULT_CUSTOM_UPDATE_PARAMETER_VALUES =
         "\n            t_PreparedStatement.set{0}({1}, {2});";
 
     /**
      * The custom update query line.
      */
     public static final String DEFAULT_CUSTOM_UPDATE_QUERY_LINE =
         "            t_sbQuery.append({0}\"{1} \");";
 
     /**
      * The default class end.
      */
     public static final String DEFAULT_CLASS_END = "}\n";
 
     /**
      * The table template.
      */
     private TableTemplate m__TableTemplate;
 
     /**
      * The database metadata manager.
      */
     private DatabaseMetaDataManager m__MetaDataManager;
 
     /**
      * The custom-sql provider.
      */
     private CustomSqlProvider m__CustomSqlProvider;
 
     /**
      * The header.
      */
     private String m__strHeader;
 
     /**
      * The package declaration.
      */
     private String m__strPackageDeclaration;
 
     /**
      * The package name.
      */
     private String m__strPackageName;
 
     /**
      * The engine name.
      */
     private String m__strEngineName;
 
     /**
      * The engine's version.
      */
     private String m__strEngineVersion;
 
     /**
      * The quote.
      */
     private String m__strQuote;
 
     /**
      * The base package name.
      */
     private String m__strBasePackageName;
 
     /**
      * The repository name.
      */
     private String m__strRepositoryName;
 
     /**
      * The project imports.
      */
     private String m__strProjectImports;
 
     /**
      * The foreign DAO imports.
      */
     private String m__strForeignDAOImports;
 
     /**
      * The ACM-SL import statements.
      */
     private String m__strAcmslImports;
 
     /**
      * The JDK import statements.
      */
     private String m__strJdkImports;
 
     /**
      * The JDK extension import statements.
      */
     private String m__strJdkExtensionImports;
 
     /**
      * The Logging import statements.
      */
     private String m__strLoggingImports;
 
     /**
      * The class Javadoc.
      */
     private String m__strJavadoc;
 
     /**
      * The class definition.
      */
     private String m__strClassDefinition;
 
     /**
      * The class start.
      */
     private String m__strClassStart;
 
     /**
      * The class constructor.
      */
     private String m__strClassConstructor;
 
     /**
      * The find-by-primary-key method.
      */
     private String m__strFindByPrimaryKeyMethod;
 
     /**
      * The find-by-primary-key pk javadoc.
      */
     private String m__strFindByPrimaryKeyPkJavadoc;
 
     /**
      * The find-by-primary-key pk declaration.
      */
     private String m__strFindByPrimaryKeyPkDeclaration;
 
     /**
      * The find-by-primary-key select fields.
      */
     private String m__strFindByPrimaryKeySelectFields;
 
     /**
      * The find-by-primary-key pk filter declaration.
      */
     private String m__strFindByPrimaryKeyFilterDeclaration;
 
     /**
      * The find-by-primary-key filter values.
      */
     private String m__strFindByPrimaryKeyFilterValues;
 
     /**
      * The build-value-object method.
      */
     private String m__strBuildValueObjectMethod;
 
     /**
      * The build-value-object value retrieval.
      */
     private String m__strBuildValueObjectValueRetrieval;
 
     /**
      * The insert method.
      */
     private String m__strInsertMethod;
 
     /**
      * The insert parameters Javadoc.
      */
     private String m__strInsertParametersJavadoc;
 
     /**
      * The insert parameters declaration.
      */
     private String m__strInsertParametersDeclaration;
 
     /**
      * The insert parameters specification.
      */
     private String m__strInsertParametersSpecification;
 
     /**
      * The insert keyword parameters specification.
      */
     private String m__strInsertKeywordParametersSpecification;
 
     /**
      * The update method.
      */
     private String m__strUpdateMethod;
 
     /**
      * The update parameters Javadoc.
      */
     private String m__strUpdateParametersJavadoc;
 
     /**
      * The update parameters declaration.
      */
     private String m__strUpdateParametersDeclaration;
 
     /**
      * The update parameters specification.
      */
     private String m__strUpdateParametersSpecification;
 
     /**
      * The update filter.
      */
     private String m__strUpdateFilter;
 
     /**
      * The delete method.
      */
     private String m__strDeleteMethod;
 
     /**
      * The delete PK javadoc.
      */
     private String m__strDeletePkJavadoc;
 
     /**
      * The delete PK declaration.
      */
     private String m__strDeletePkDeclaration;
 
     /**
      * The delete PK filter declaration.
      */
     private String m__strDeleteFilterDeclaration;
 
     /**
      * The delete filter values.
      */
     private String m__strDeleteFilterValues;
 
     /**
      * The delete with FK method.
      */
     private String m__strDeleteWithFkMethod;
 
     /**
      * The delete with FK PK javadoc.
      */
     private String m__strDeleteWithFkPkJavadoc;
 
     /**
      * The delete with FK PK declaration.
      */
     private String m__strDeleteWithFkPkDeclaration;
 
     /**
      * The delete with FK PK DAO delete request..
      */
     private String m__strDeleteWithFkDAODeleteRequest;
 
     /**
      * The delete with FK DAO FK values.
      */
     private String m__strDeleteWithFkPkValues;
 
     /**
      * The custom select.
      */
     private String m__strCustomSelect;
 
     /**
      * The custom select parameter javadoc.
      */
     private String m__strCustomSelectParameterJavadoc;
 
     /**
      * The custom select parameter declaration.
      */
     private String m__strCustomSelectParameterDeclaration;
 
     /**
      * The custom select parameter values.
      */
     private String m__strCustomSelectParameterValues;
 
     /**
      * The custom select result properties.
      */
     private String m__strCustomSelectResultPropertyValues;
 
     /**
      * The custom update.
      */
     private String m__strCustomUpdate;
 
     /**
      * The custom update parameter javadoc.
      */
     private String m__strCustomUpdateParameterJavadoc;
 
     /**
      * The custom update parameter declaration.
      */
     private String m__strCustomUpdateParameterDeclaration;
 
     /**
      * The custom update parameter values.
      */
     private String m__strCustomUpdateParameterValues;
 
     /**
      * The custom update query line.
      */
     private String m__strCustomUpdateQueryLine;
 
     /**
      * The class end.
      */
     private String m__strClassEnd;
 
     /**
      * Builds a DAOTemplate using given information.
      * @param tableTemplate the table template.
      * @param metaDataManager the database metadata manager.
      * @param customSqlProvider the CustomSqlProvider instance.
      * @param header the header.
      * @param packageDeclaration the package declaration.
      * @param packageName the package name.
      * @param engineName the engine name.
      * @param engineVersion the engine version.
      * @param quote the identifier quote string.
      * @param basePackageName the base package name.
      * @param repositoryName the repository name.
      * @param projectImports the project imports.
      * @param foreignDAOImports the foreign DAO imports.
      * @param acmslImports the ACM-SL imports.
      * @param jdkImports the JDK imports.
      * @param jdkExtensionImports the JDK extension imports.
      * @param loggingImports the commons-logging imports.
      * @param javadoc the class Javadoc.
      * @param classDefinition the class definition.
      * @param classStart the class start.
      * @param classConstructor the class constructor.
      * @param findByPrimaryKeyMethod the find by primary key method.
      * @param findByPrimaryKeyPkJavadoc the find by primary key pk javadoc.
      * @param findByPrimaryKeyPkDeclaration the find by primary key pk
      * declaration.
      * @param findByPrimaryKeySelectFields the find by primary key select fields.
      * @param findByPrimaryKeyFilterDeclaration the find by primary key filter
      * declaration.
      * @param findByPrimaryKeyFilterValues the find by primary key filter values.
      * @param buildValueObjectMethod the build value object method.
      * @param buildValueObjectValueRetrieval the build value object value retrieval.
      * @param insertMethod the insert method.
      * @param insertParametersJavadoc the javadoc of the insert method's parameters.
      * @param insertParametersDeclaration the declaration of the insert method's parameters.
      * @param insertParametersSpecification the specification of the insert
      * method's parameters.
      * @param insertKeywordParametersSpecification the specification of the insert
      * method's keyword-based parameters.
      * @param updateMethod the update method.
      * @param updateParametersJavadoc the javadoc of the update method's parameters.
      * @param updateParametersDeclaration the declaration of the update method's parameters.
      * @param updateParametersSpecification the specification of the update
      * method's parameters.
      * @param updateFilter the update method's filter.
      * @param deleteMethod the delete method.
      * @param deletePkJavadoc the delete PK javadoc.
      * @param deletePkDeclaration the delete PK declaration.
      * @param deleteFilterDeclaration the delete filter declaration.
      * @param deleteFilterValues the delete filter values.
      * @param deleteWithFkMethod the delete method.
      * @param deleteWithFkPkJavadoc the delete with FK PK javadoc.
      * @param deleteWithFkPkDeclaration the delete with FK PK declaration.
      * @param deleteWithFkDAODeleteRequest the delete with FK DAO delete request.
      * @param deleteWithFkPkValues the delete with FK PK values.
      * @param customSelect the custom select template.
      * @param customSelectParameterJavadoc the Javadoc for the parameters of the
      * custom selects.
      * @param customSelectParameterDeclaration the parameter declaration of the
      * custom selects.
      * @param customSelectParameterValues the parameter values of the custom
      * selects.
      * @param customSelectResultPropertyValues the properties of the result set for
      * custom selects.
      * @param customUpdate the custom update template.
      * @param customUpdateParameterJavadoc the Javadoc for the parameters of the
      * custom updates.
      * @param customUpdateParameterDeclaration the parameter declaration of the
      * custom updates.
      * @param customUpdateParameterValues the parameter values of the custom
      * updates.
      * @param customUpdateQueryLine the custom update query line.
      * @param classEnd the class end.
      */
     public DAOTemplate(
         final TableTemplate           tableTemplate,
         final DatabaseMetaDataManager metaDataManager,
         final CustomSqlProvider       customSqlProvider,
         final String                  header,
         final String                  packageDeclaration,
         final String                  packageName,
         final String                  engineName,
         final String                  engineVersion,
         final String                  quote,
         final String                  basePackageName,
         final String                  repositoryName,
         final String                  projectImports,
         final String                  foreignDAOImports,
         final String                  acmslImports,
         final String                  jdkImports,
         final String                  jdkExtensionImports,
         final String                  loggingImports,
         final String                  javadoc,
         final String                  classDefinition,
         final String                  classStart,
         final String                  classConstructor,
         final String                  findByPrimaryKeyMethod,
         final String                  findByPrimaryKeyPkJavadoc,
         final String                  findByPrimaryKeyPkDeclaration,
         final String                  findByPrimaryKeySelectFields,
         final String                  findByPrimaryKeyFilterDeclaration,
         final String                  findByPrimaryKeyFilterValues,
         final String                  buildValueObjectMethod,
         final String                  buildValueObjectValueRetrieval,
         final String                  insertMethod,
         final String                  insertParametersJavadoc,
         final String                  insertParametersDeclaration,
         final String                  insertParametersSpecification,
         final String                  insertKeywordParametersSpecification,
         final String                  updateMethod,
         final String                  updateParametersJavadoc,
         final String                  updateParametersDeclaration,
         final String                  updateParametersSpecification,
         final String                  updateFilter,
         final String                  deleteMethod,
         final String                  deletePkJavadoc,
         final String                  deletePkDeclaration,
         final String                  deleteFilterDeclaration,
         final String                  deleteFilterValues,
         final String                  deleteWithFkMethod,
         final String                  deleteWithFkPkJavadoc,
         final String                  deleteWithFkPkDeclaration,
         final String                  deleteWithFkDAODeleteRequest,
         final String                  deleteWithFkPkValues,
         final String                  customSelect,
         final String                  customSelectParameterJavadoc,
         final String                  customSelectParameterDeclaration,
         final String                  customSelectParameterValues,
         final String                  customSelectResultPropertyValues,
         final String                  customUpdate,
         final String                  customUpdateParameterJavadoc,
         final String                  customUpdateParameterDeclaration,
         final String                  customUpdateParameterValues,
         final String                  customUpdateQueryLine,
         final String                  classEnd)
     {
         immutableSetTableTemplate(
             tableTemplate);
 
         immutableSetMetaDataManager(
             metaDataManager);
 
         immutableSetCustomSqlProvider(
             customSqlProvider);
 
         immutableSetHeader(
             header);
 
         immutableSetPackageDeclaration(
             packageDeclaration);
 
         immutableSetPackageName(
             packageName);
 
         immutableSetEngineName(
             engineName);
 
         immutableSetEngineVersion(
             engineVersion);
 
         immutableSetQuote(
             quote);
 
         immutableSetBasePackageName(
             basePackageName);
 
         immutableSetRepositoryName(
             repositoryName);
 
         immutableSetProjectImports(
             projectImports);
 
         immutableSetForeignDAOImports(
             foreignDAOImports);
 
         immutableSetAcmslImports(
             acmslImports);
 
         immutableSetJdkImports(
             jdkImports);
 
         immutableSetJdkExtensionImports(
             jdkExtensionImports);
 
         immutableSetLoggingImports(
             loggingImports);
 
         immutableSetJavadoc(
             javadoc);
 
         immutableSetClassDefinition(
             classDefinition);
 
         immutableSetClassStart(
             classStart);
 
         immutableSetClassConstructor(
             classConstructor);
 
         immutableSetFindByPrimaryKeyMethod(
             findByPrimaryKeyMethod);
 
         immutableSetFindByPrimaryKeyPkJavadoc(
             findByPrimaryKeyPkJavadoc);
 
         immutableSetFindByPrimaryKeyPkDeclaration(
             findByPrimaryKeyPkDeclaration);
 
         immutableSetFindByPrimaryKeySelectFields(
             findByPrimaryKeySelectFields);
 
         immutableSetFindByPrimaryKeyFilterDeclaration(
             findByPrimaryKeyFilterDeclaration);
 
         immutableSetFindByPrimaryKeyFilterValues(
             findByPrimaryKeyFilterValues);
 
         immutableSetBuildValueObjectMethod(
             buildValueObjectMethod);
 
         immutableSetBuildValueObjectValueRetrieval(
             buildValueObjectValueRetrieval);
 
         immutableSetInsertMethod(
             insertMethod);
 
         immutableSetInsertParametersJavadoc(
             insertParametersJavadoc);
 
         immutableSetInsertParametersDeclaration(
             insertParametersDeclaration);
 
         immutableSetInsertParametersSpecification(
             insertParametersSpecification);
 
         immutableSetInsertKeywordParametersSpecification(
             insertKeywordParametersSpecification);
 
         immutableSetUpdateMethod(
             updateMethod);
 
         immutableSetUpdateParametersJavadoc(
             updateParametersJavadoc);
 
         immutableSetUpdateParametersDeclaration(
             updateParametersDeclaration);
 
         immutableSetUpdateParametersSpecification(
             updateParametersSpecification);
 
         immutableSetUpdateFilter(
             updateFilter);
 
         immutableSetDeleteMethod(
             deleteMethod);
 
         immutableSetDeletePkJavadoc(
             deletePkJavadoc);
 
         immutableSetDeletePkDeclaration(
             deletePkDeclaration);
 
         immutableSetDeleteFilterDeclaration(
             deleteFilterDeclaration);
 
         immutableSetDeleteFilterValues(
             deleteFilterValues);
 
         immutableSetDeleteWithFkMethod(
             deleteWithFkMethod);
 
         immutableSetDeleteWithFkPkJavadoc(
             deleteWithFkPkJavadoc);
 
         immutableSetDeleteWithFkPkDeclaration(
             deleteWithFkPkDeclaration);
 
         immutableSetDeleteWithFkDAODeleteRequest(
             deleteWithFkDAODeleteRequest);
 
         immutableSetDeleteWithFkPkValues(
             deleteWithFkPkValues);
 
         immutableSetCustomSelect(
             customSelect);
 
         immutableSetCustomSelectParameterJavadoc(
             customSelectParameterJavadoc);
 
         immutableSetCustomSelectParameterDeclaration(
             customSelectParameterDeclaration);
 
         immutableSetCustomSelectParameterValues(
             customSelectParameterValues);
 
         immutableSetCustomSelectResultPropertyValues(
             customSelectResultPropertyValues);
 
         immutableSetCustomUpdate(
             customUpdate);
 
         immutableSetCustomUpdateParameterJavadoc(
             customUpdateParameterJavadoc);
 
         immutableSetCustomUpdateParameterDeclaration(
             customUpdateParameterDeclaration);
 
         immutableSetCustomUpdateParameterValues(
             customUpdateParameterValues);
 
         immutableSetCustomUpdateQueryLine(
             customUpdateQueryLine);
 
         immutableSetClassEnd(
             classEnd);
     }
 
     /**
      * Builds a DAOTemplate using given information.
      * @param tableTemplate the table template.
      * @param metaDataManager the database metadata manager.
      * @param customSqlProvider the CustomSqlProvider instance.
      * @param packageName the package name.
      * @param engineName the engine name.
      * @param engineVersion the engine version.
      * @param quote the identifier quote string.
      * @param basePackageName the base package name.
      * @param repositoryName the repository name.
      */
     public DAOTemplate(
         final TableTemplate           tableTemplate,
         final DatabaseMetaDataManager metaDataManager,
         final CustomSqlProvider       customSqlProvider,
         final String                  packageName,
         final String                  engineName,
         final String                  engineVersion,
         final String                  quote,
         final String                  basePackageName,
         final String                  repositoryName)
     {
         this(
             tableTemplate,
             metaDataManager,
             customSqlProvider,
             DEFAULT_HEADER,
             PACKAGE_DECLARATION,
             packageName,
             engineName,
             engineVersion,
             quote,
             basePackageName,
             repositoryName,
             DEFAULT_PROJECT_IMPORTS,
             DEFAULT_FOREIGN_DAO_IMPORTS,
             ACMSL_IMPORTS,
             JDK_IMPORTS,
             JDK_EXTENSION_IMPORTS,
             LOGGING_IMPORTS,
             DEFAULT_JAVADOC,
             CLASS_DEFINITION,
             DEFAULT_CLASS_START,
             CLASS_CONSTRUCTOR,
             DEFAULT_FIND_BY_PRIMARY_KEY_METHOD,
             DEFAULT_FIND_BY_PRIMARY_KEY_PK_JAVADOC,
             DEFAULT_FIND_BY_PRIMARY_KEY_PK_DECLARATION,
             DEFAULT_FIND_BY_PRIMARY_KEY_SELECT_FIELDS,
             DEFAULT_FIND_BY_PRIMARY_KEY_FILTER_DECLARATION,
             DEFAULT_FIND_BY_PRIMARY_KEY_FILTER_VALUES,
             BUILD_VALUE_OBJECT_METHOD,
             BUILD_VALUE_OBJECT_VALUE_RETRIEVAL,
             DEFAULT_INSERT_METHOD,
             DEFAULT_INSERT_PARAMETERS_JAVADOC,
             DEFAULT_INSERT_PARAMETERS_DECLARATION,
             DEFAULT_INSERT_PARAMETERS_SPECIFICATION,
             DEFAULT_INSERT_KEYWORD_PARAMETERS_SPECIFICATION,
             DEFAULT_UPDATE_METHOD,
             DEFAULT_UPDATE_PARAMETERS_JAVADOC,
             DEFAULT_UPDATE_PARAMETERS_DECLARATION,
             DEFAULT_UPDATE_PARAMETERS_SPECIFICATION,
             DEFAULT_UPDATE_FILTER,
             DEFAULT_DELETE_METHOD,
             DEFAULT_DELETE_PK_JAVADOC,
             DEFAULT_DELETE_PK_DECLARATION,
             DEFAULT_DELETE_FILTER_DECLARATION,
             DEFAULT_DELETE_FILTER_VALUES,
             DEFAULT_DELETE_WITH_FK_METHOD,
             DEFAULT_DELETE_WITH_FK_PK_JAVADOC,
             DEFAULT_DELETE_WITH_FK_PK_DECLARATION,
             DEFAULT_DELETE_WITH_FK_DAO_DELETE_REQUEST,
             DEFAULT_DELETE_WITH_FK_PK_VALUES,
             DEFAULT_CUSTOM_SELECT,
             DEFAULT_CUSTOM_SELECT_PARAMETER_JAVADOC,
             DEFAULT_CUSTOM_SELECT_PARAMETER_DECLARATION,
             DEFAULT_CUSTOM_SELECT_PARAMETER_VALUES,
             DEFAULT_CUSTOM_SELECT_RESULT_PROPERTIES,
             DEFAULT_CUSTOM_UPDATE,
             DEFAULT_CUSTOM_UPDATE_PARAMETER_JAVADOC,
             DEFAULT_CUSTOM_UPDATE_PARAMETER_DECLARATION,
             DEFAULT_CUSTOM_UPDATE_PARAMETER_VALUES,
             DEFAULT_CUSTOM_UPDATE_QUERY_LINE,
             DEFAULT_CLASS_END);
     }
 
     /**
      * Specifies the table template.
      * @param tableTemplate the table template.
      */
     private void immutableSetTableTemplate(final TableTemplate tableTemplate)
     {
         m__TableTemplate = tableTemplate;
     }
 
     /**
      * Specifies the table template.
      * @param tableTemplate the table template.
      */
     protected void setTableTemplate(final TableTemplate tableTemplate)
     {
         immutableSetTableTemplate(tableTemplate);
     }
 
     /**
      * Retrieves the table template.
      * @return such template.
      */
     public TableTemplate getTableTemplate()
     {
         return m__TableTemplate;
     }
 
     /**
      * Specifies the metadata manager.
      * @param metaDataManager the metadata manager.
      */
     private void immutableSetMetaDataManager(
         final DatabaseMetaDataManager metaDataManager)
     {
         m__MetaDataManager = metaDataManager;
     }
 
     /**
      * Specifies the metadata manager.
      * @param metaDataManager the metadata manager.
      */
     protected void setMetaDataManager(
         final DatabaseMetaDataManager metaDataManager)
     {
         immutableSetMetaDataManager(metaDataManager);
     }
 
     /**
      * Retrieves the metadata manager.
      * @return such manager.
      */
     public DatabaseMetaDataManager getMetaDataManager()
     {
         return m__MetaDataManager;
     }
 
     /**
      * Specifies the custom-sql provider.
      * @param customSqlProvider the customsql provider.
      */
     private void immutableSetCustomSqlProvider(
         final CustomSqlProvider customSqlProvider)
     {
         m__CustomSqlProvider = customSqlProvider;
     }
 
     /**
      * Specifies the custom-sql provider.
      * @param customSqlProvider the customsql provider.
      */
     protected void setCustomSqlProvider(
         final CustomSqlProvider customSqlProvider)
     {
         immutableSetCustomSqlProvider(customSqlProvider);
     }
 
     /**
      * Retrieves the custom-sql provider.
      * @return such provider.
      */
     public CustomSqlProvider getCustomSqlProvider()
     {
         return m__CustomSqlProvider;
     }
 
     /**
      * Specifies the header.
      * @param header the new header.
      */
     private void immutableSetHeader(final String header)
     {
         m__strHeader = header;
     }
 
     /**
      * Specifies the header.
      * @param header the new header.
      */
     protected void setHeader(final String header)
     {
         immutableSetHeader(header);
     }
 
     /**
      * Retrieves the header.
      * @return such information.
      */
     public String getHeader() 
     {
         return m__strHeader;
     }
 
     /**
      * Specifies the package declaration.
      * @param packageDeclaration the new package declaration.
      */
     private void immutableSetPackageDeclaration(final String packageDeclaration)
     {
         m__strPackageDeclaration = packageDeclaration;
     }
 
     /**
      * Specifies the package declaration.
      * @param packageDeclaration the new package declaration.
      */
     protected void setPackageDeclaration(final String packageDeclaration)
     {
         immutableSetPackageDeclaration(packageDeclaration);
     }
 
     /**
      * Retrieves the package declaration.
      * @return such information.
      */
     public String getPackageDeclaration() 
     {
         return m__strPackageDeclaration;
     }
 
     /**
      * Specifies the package name.
      * @param packageName the new package name.
      */
     private void immutableSetPackageName(final String packageName)
     {
         m__strPackageName = packageName;
     }
 
     /**
      * Specifies the package name.
      * @param packageName the new package name.
      */
     protected void setPackageName(final String packageName)
     {
         immutableSetPackageName(packageName);
     }
 
     /**
      * Retrieves the package name.
      * @return such information.
      */
     public String getPackageName() 
     {
         return m__strPackageName;
     }
 
     /**
      * Specifies the engine name.
      * @param engineName the new engine name.
      */
     private void immutableSetEngineName(final String engineName)
     {
         m__strEngineName = engineName;
     }
 
     /**
      * Specifies the engine name.
      * @param engineName the new engine name.
      */
     protected void setEngineName(final String engineName)
     {
         immutableSetEngineName(engineName);
     }
 
     /**
      * Retrieves the engine name.
      * @return such information.
      */
     public String getEngineName() 
     {
         return m__strEngineName;
     }
 
     /**
      * Specifies the engine version.
      * @param engineVersion the new engine version.
      */
     private void immutableSetEngineVersion(final String engineVersion)
     {
         m__strEngineVersion = engineVersion;
     }
 
     /**
      * Specifies the engine version.
      * @param engineVersion the new engine version.
      */
     protected void setEngineVersion(final String engineVersion)
     {
         immutableSetEngineVersion(engineVersion);
     }
 
     /**
      * Retrieves the engine version.
      * @return such information.
      */
     public String getEngineVersion()
     {
         return m__strEngineVersion;
     }
 
     /**
      * Specifies the identifier quote string.
      * @param quote such identifier.
      */
     private void immutableSetQuote(final String quote)
     {
         m__strQuote = quote;
     }
 
     /**
      * Specifies the identifier quote string.
      * @param quote such identifier.
      */
     protected void setQuote(final String quote)
     {
         immutableSetQuote(quote);
     }
 
     /**
      * Retrieves the identifier quote string.
      * @return such identifier.
      */
     public String getQuote()
     {
         return m__strQuote;
     }
 
     /**
      * Specifies the base package name.
      * @param basePackageName the new base package name.
      */
     private void immutableSetBasePackageName(final String basePackageName)
     {
         m__strBasePackageName = basePackageName;
     }
 
     /**
      * Specifies the base package name.
      * @param basePackageName the new base package name.
      */
     protected void setBasePackageName(final String basePackageName)
     {
         immutableSetBasePackageName(basePackageName);
     }
 
     /**
      * Retrieves the base package name.
      * @return such information.
      */
     public String getBasePackageName() 
     {
         return m__strBasePackageName;
     }
 
     /**
      * Specifies the repository name.
      * @param repositoryName the new repository name.
      */
     private void immutableSetRepositoryName(final String repositoryName)
     {
         m__strRepositoryName = repositoryName;
     }
 
     /**
      * Specifies the repository name.
      * @param repositoryName the new repository name.
      */
     protected void setRepositoryName(final String repositoryName)
     {
         immutableSetRepositoryName(repositoryName);
     }
 
     /**
      * Retrieves the repository name.
      * @return such information.
      */
     public String getRepositoryName()
     {
         return m__strRepositoryName;
     }
 
     /**
      * Specifies the project imports.
      * @param projectImports the new project imports.
      */
     private void immutableSetProjectImports(final String projectImports)
     {
         m__strProjectImports = projectImports;
     }
 
     /**
      * Specifies the project imports.
      * @param projectImports the new project imports.
      */
     protected void setProjectImports(final String projectImports)
     {
         immutableSetProjectImports(projectImports);
     }
 
     /**
      * Retrieves the project imports.
      * @return such information.
      */
     public String getProjectImports() 
     {
         return m__strProjectImports;
     }
 
     /**
      * Specifies the foreign DAO imports.
      * @param foreignDAOImports the new foreign DAO imports.
      */
     private void immutableSetForeignDAOImports(final String foreignDAOImports)
     {
         m__strForeignDAOImports = foreignDAOImports;
     }
 
     /**
      * Specifies the foreign DAO imports.
      * @param foreignDAOImports the new foreign DAO imports.
      */
     protected void setForeignDAOImports(final String foreignDAOImports)
     {
         immutableSetForeignDAOImports(foreignDAOImports);
     }
 
     /**
      * Retrieves the foreign DAO imports.
      * @return such information.
      */
     public String getForeignDAOImports() 
     {
         return m__strForeignDAOImports;
     }
 
     /**
      * Specifies the ACM-SL imports.
      * @param acmslImports the new ACM-SL imports.
      */
     private void immutableSetAcmslImports(final String acmslImports)
     {
         m__strAcmslImports = acmslImports;
     }
 
     /**
      * Specifies the ACM-SL imports.
      * @param acmslImports the new ACM-SL imports.
      */
     protected void setAcmslImports(final String acmslImports)
     {
         immutableSetAcmslImports(acmslImports);
     }
 
     /**
      * Retrieves the ACM-SL imports.
      * @return such information.
      */
     public String getAcmslImports() 
     {
         return m__strAcmslImports;
     }
 
     /**
      * Specifies the JDK imports.
      * @param jdkImports the new JDK imports.
      */
     private void immutableSetJdkImports(final String jdkImports)
     {
         m__strJdkImports = jdkImports;
     }
 
     /**
      * Specifies the JDK imports.
      * @param jdkImports the new JDK imports.
      */
     protected void setJdkImports(final String jdkImports)
     {
         immutableSetJdkImports(jdkImports);
     }
 
     /**
      * Retrieves the JDK imports.
      * @return such information.
      */
     public String getJdkImports() 
     {
         return m__strJdkImports;
     }
 
     /**
      * Specifies the JDK extension imports.
      * @param jdkExtensionImports the new JDK extension imports.
      */
     private void immutableSetJdkExtensionImports(final String jdkExtensionImports)
     {
         m__strJdkExtensionImports = jdkExtensionImports;
     }
 
     /**
      * Specifies the JDK extension imports.
      * @param jdkExtensionImports the new JDK extension imports.
      */
     protected void setJdkExtensionImports(final String jdkExtensionImports)
     {
         immutableSetJdkExtensionImports(jdkExtensionImports);
     }
 
     /**
      * Retrieves the JDK extension imports.
      * @return such information.
      */
     public String getJdkExtensionImports() 
     {
         return m__strJdkExtensionImports;
     }
 
 
     /**
      * Specifies the logging imports.
      * @param loggingImports the new logging imports.
      */
     private void immutableSetLoggingImports(final String loggingImports)
     {
         m__strLoggingImports = loggingImports;
     }
 
     /**
      * Specifies the logging imports.
      * @param loggingImports the new logging imports.
      */
     protected void setLoggingImports(final String loggingImports)
     {
         immutableSetLoggingImports(loggingImports);
     }
 
     /**
      * Retrieves the logging imports.
      * @return such information.
      */
     public String getLoggingImports() 
     {
         return m__strLoggingImports;
     }
 
     /**
      * Specifies the javadoc.
      * @param javadoc the new javadoc.
      */
     private void immutableSetJavadoc(final String javadoc)
     {
         m__strJavadoc = javadoc;
     }
 
     /**
      * Specifies the javadoc.
      * @param javadoc the new javadoc.
      */
     protected void setJavadoc(final String javadoc)
     {
         immutableSetJavadoc(javadoc);
     }
 
     /**
      * Retrieves the javadoc.
      * @return such information.
      */
     public String getJavadoc() 
     {
         return m__strJavadoc;
     }
 
     /**
      * Specifies the class definition.
      * @param classDefinition the new class definition.
      */
     private void immutableSetClassDefinition(final String classDefinition)
     {
         m__strClassDefinition = classDefinition;
     }
 
     /**
      * Specifies the class definition.
      * @param classDefinition the new class definition.
      */
     protected void setClassDefinition(final String classDefinition)
     {
         immutableSetClassDefinition(classDefinition);
     }
 
     /**
      * Retrieves the class definition.
      * @return such information.
      */
     public String getClassDefinition() 
     {
         return m__strClassDefinition;
     }
 
     /**
      * Specifies the class start.
      * @param classStart the new class start.
      */
     private void immutableSetClassStart(final String classStart)
     {
         m__strClassStart = classStart;
     }
 
     /**
      * Specifies the class start.
      * @param classStart the new class start.
      */
     protected void setClassStart(final String classStart)
     {
         immutableSetClassStart(classStart);
     }
 
     /**
      * Retrieves the class start.
      * @return such information.
      */
     public String getClassStart() 
     {
         return m__strClassStart;
     }
 
     /**
      * Specifies the class constructor
      * @param constructor such source code.
      */
     private void immutableSetClassConstructor(final String constructor)
     {
         m__strClassConstructor = constructor;
     }
 
     /**
      * Specifies the class constructor
      * @param constructor such source code.
      */
     protected void setClassConstructor(final String constructor)
     {
         immutableSetClassConstructor(constructor);
     }
 
     /**
      * Retrieves the class constructor.
      * @return such source code.
      */
     public String getClassConstructor()
     {
         return m__strClassConstructor;
     }
 
     /**
      * Specifies the find-by-primary-key method.
      * @param findByPrimaryKeyMethod such method.
      */
     private void immutableSetFindByPrimaryKeyMethod(
         String findByPrimaryKeyMethod)
     {
         m__strFindByPrimaryKeyMethod = findByPrimaryKeyMethod;
     }
 
     /**
      * Specifies the find-by-primary-key method.
      * @param findByPrimaryKeyMethod such method.
      */
     protected void setFindByPrimaryKeyMethod(
         String findByPrimaryKeyMethod)
     {
         immutableSetFindByPrimaryKeyMethod(
             findByPrimaryKeyMethod);
     }
 
     /**
      * Retrieves the find-by-primary-key method.
      * @return such method.
      */
     public String getFindByPrimaryKeyMethod()
     {
         return m__strFindByPrimaryKeyMethod;
     }
 
     /**
      * Specifies the find-by-primary-key pk Javadoc.
      * @param findByPrimaryKeyPkJavadoc such Javadoc.
      */
     private void immutableSetFindByPrimaryKeyPkJavadoc(
         String findByPrimaryKeyPkJavadoc)
     {
         m__strFindByPrimaryKeyPkJavadoc = findByPrimaryKeyPkJavadoc;
     }
 
     /**
      * Specifies the find-by-primary-key pk Javadoc.
      * @param findByPrimaryKeyPkJavadoc such Javadoc.
      */
     protected void setFindByPrimaryKeyPkJavadoc(
         String findByPrimaryKeyPkJavadoc)
     {
         immutableSetFindByPrimaryKeyPkJavadoc(
             findByPrimaryKeyPkJavadoc);
     }
 
     /**
      * Retrieves the find-by-primary-key pk Javadoc.
      * @return such Javadoc.
      */
     public String getFindByPrimaryKeyPkJavadoc()
     {
         return m__strFindByPrimaryKeyPkJavadoc;
     }
     
     /**
      * Specifies the find-by-primary-key pk declaration.
      * @param findByPrimaryKeyPkDeclaration such declaration.
      */
     private void immutableSetFindByPrimaryKeyPkDeclaration(
         String findByPrimaryKeyPkDeclaration)
     {
         m__strFindByPrimaryKeyPkDeclaration =
             findByPrimaryKeyPkDeclaration;
     }
 
     /**
      * Specifies the find-by-primary-key pk declaration.
      * @param findByPrimaryKeyPkDeclaration such declaration.
      */
     protected void setFindByPrimaryKeyPkdeclaration(
         String findByPrimaryKeyPkDeclaration)
     {
         immutableSetFindByPrimaryKeyPkDeclaration(
             findByPrimaryKeyPkDeclaration);
     }
 
     /**
      * Retrieves the find-by-primary-key pk declaration.
      * @return such declaration.
      */
     public String getFindByPrimaryKeyPkDeclaration()
     {
         return m__strFindByPrimaryKeyPkDeclaration;
     }
 
     /**
      * Specifies the find-by-primary-key select fields.
      * @param findByPrimaryKeySelectFields such fields.
      */
     private void immutableSetFindByPrimaryKeySelectFields(
         String findByPrimaryKeySelectFields)
     {
         m__strFindByPrimaryKeySelectFields =
             findByPrimaryKeySelectFields;
     }
 
     /**
      * Specifies the find-by-primary-key select fields.
      * @param findByPrimaryKeySelectFields such fields.
      */
     protected void setFindByPrimaryKeySelectFields(
         String findByPrimaryKeySelectFields)
     {
         immutableSetFindByPrimaryKeySelectFields(
             findByPrimaryKeySelectFields);
     }
 
     /**
      * Retrieves the find-by-primary-key select fields.
      * @return such fields.
      */
     public String getFindByPrimaryKeySelectFields()
     {
         return m__strFindByPrimaryKeySelectFields;
     }
 
     /**
      * Specifies the find-by-primary-key filter declaration.
      * @param findByPrimaryKeyPkFilterDeclaration such declaration.
      */
     private void immutableSetFindByPrimaryKeyFilterDeclaration(
         String findByPrimaryKeyFilterDeclaration)
     {
         m__strFindByPrimaryKeyFilterDeclaration =
             findByPrimaryKeyFilterDeclaration;
     }
 
     /**
      * Specifies the find-by-primary-key filter declaration.
      * @param findByPrimaryKeyFilterDeclaration such declaration.
      */
     protected void setFindByPrimaryKeyFilterDeclaration(
         String findByPrimaryKeyFilterDeclaration)
     {
         immutableSetFindByPrimaryKeyFilterDeclaration(
             findByPrimaryKeyFilterDeclaration);
     }
 
     /**
      * Retrieves the find-by-primary-key filter declaration.
      * @return such declaration.
      */
     public String getFindByPrimaryKeyFilterDeclaration()
     {
         return m__strFindByPrimaryKeyFilterDeclaration;
     }
 
     /**
      * Specifies the find-by-primary-key filter values.
      * @param findByPrimaryKeyFilterValues such values.
      */
     private void immutableSetFindByPrimaryKeyFilterValues(
         String findByPrimaryKeyFilterValues)
     {
         m__strFindByPrimaryKeyFilterValues =
             findByPrimaryKeyFilterValues;
     }
 
     /**
      * Specifies the find-by-primary-key filter values.
      * @param findByPrimaryKeyFilterValues such values.
      */
     protected void setFindByPrimaryKeyFilterValues(
         String findByPrimaryKeyFilterValues)
     {
         immutableSetFindByPrimaryKeyFilterValues(
             findByPrimaryKeyFilterValues);
     }
 
     /**
      * Retrieves the find-by-primary-key filter values.
      * @return such values.
      */
     public String getFindByPrimaryKeyFilterValues()
     {
         return m__strFindByPrimaryKeyFilterValues;
     }
     
     /**
      * Specifies the build-value-object method.
      * @param buildValueObjectMethod such method.
      */
     private void immutableSetBuildValueObjectMethod(
         String buildValueObjectMethod)
     {
         m__strBuildValueObjectMethod = buildValueObjectMethod;
     }
 
     /**
      * Specifies the build-value-object method.
      * @param buildValueObjectMethod such method.
      */
     protected void setBuildValueObjectMethod(
         String buildValueObjectMethod)
     {
         immutableSetBuildValueObjectMethod(
             buildValueObjectMethod);
     }
 
     /**
      * Retrieves the build-value-object method.
      * @return such method.
      */
     public String getBuildValueObjectMethod()
     {
         return m__strBuildValueObjectMethod;
     }
 
     /**
      * Specifies the build-value-object value retrieval.
      * @param buildValueObjectValueRetrieval such method.
      */
     private void immutableSetBuildValueObjectValueRetrieval(
         String buildValueObjectValueRetrieval)
     {
         m__strBuildValueObjectValueRetrieval = buildValueObjectValueRetrieval;
     }
 
     /**
      * Specifies the build-value-object value retrieval.
      * @param buildValueObjectValueRetrieval such method.
      */
     protected void setBuildValueObjectValueRetrieval(
         String buildValueObjectValueRetrieval)
     {
         immutableSetBuildValueObjectValueRetrieval(
             buildValueObjectValueRetrieval);
     }
 
     /**
      * Retrieves the build-value-object value retrieval.
      * @return such method.
      */
     public String getBuildValueObjectValueRetrieval()
     {
         return m__strBuildValueObjectValueRetrieval;
     }
 
     /**
      * Specifies the insert method.
      * @param insertMethod such method.
      */
     private void immutableSetInsertMethod(final String insertMethod)
     {
         m__strInsertMethod = insertMethod;
     }
 
     /**
      * Specifies the insert method.
      * @param insertMethod such method.
      */
     protected void setInsertMethod(final String insertMethod)
     {
         immutableSetInsertMethod(insertMethod);
     }
 
     /**
      * Retrieves the insert method.
      * @return such method.
      */
     public String getInsertMethod()
     {
         return m__strInsertMethod;
     }
 
     /**
      * Specifies the insert parameters Javadoc.
      * @param javadoc such javadoc.
      */
     private void immutableSetInsertParametersJavadoc(final String javadoc)
     {
         m__strInsertParametersJavadoc = javadoc;
     }
 
     /**
      * Specifies the insert parameters Javadoc.
      * @param javadoc such javadoc.
      */
     protected void setInsertParametersJavadoc(final String javadoc)
     {
         immutableSetInsertParametersJavadoc(javadoc);
     }
 
     /**
      * Retrieves the insert parameters javadoc.
      * @return such information.
      */
     public String getInsertParametersJavadoc()
     {
         return m__strInsertParametersJavadoc;
     }
 
     /**
      * Specifies the insert parameters Declaration.
      * @param declaration such declaration.
      */
     private void immutableSetInsertParametersDeclaration(final String declaration)
     {
         m__strInsertParametersDeclaration = declaration;
     }
 
     /**
      * Specifies the insert parameters Declaration.
      * @param declaration such declaration.
      */
     protected void setInsertParametersDeclaration(final String declaration)
     {
         immutableSetInsertParametersDeclaration(declaration);
     }
 
     /**
      * Retrieves the insert parameters declaration.
      * @return such information.
      */
     public String getInsertParametersDeclaration()
     {
         return m__strInsertParametersDeclaration;
     }
 
     /**
      * Specifies the insert parameters specification.
      * @param specification such specification.
      */
     private void immutableSetInsertParametersSpecification(final String specification)
     {
         m__strInsertParametersSpecification = specification;
     }
 
     /**
      * Specifies the insert parameters specification.
      * @param specification such specification.
      */
     protected void setInsertParametersSpecification(final String specification)
     {
         immutableSetInsertParametersSpecification(specification);
     }
 
     /**
      * Retrieves the insert parameters specification.
      * @return such information.
      */
     public String getInsertParametersSpecification()
     {
         return m__strInsertParametersSpecification;
     }
 
     /**
      * Specifies the insert keyword-based parameters specification.
      * @param specification such specification.
      */
     private void immutableSetInsertKeywordParametersSpecification(
         String specification)
     {
         m__strInsertKeywordParametersSpecification = specification;
     }
 
     /**
      * Specifies the insert keyword-based parameters specification.
      * @param specification such specification.
      */
     protected void setInsertKeywordParametersSpecification(final String specification)
     {
         immutableSetInsertKeywordParametersSpecification(specification);
     }
 
     /**
      * Retrieves the insert keyword-based parameters specification.
      * @return such information.
      */
     public String getInsertKeywordParametersSpecification()
     {
         return m__strInsertKeywordParametersSpecification;
     }
 
     /**
      * Specifies the update method.
      * @param updateMethod such method.
      */
     private void immutableSetUpdateMethod(final String updateMethod)
     {
         m__strUpdateMethod = updateMethod;
     }
 
     /**
      * Specifies the update method.
      * @param updateMethod such method.
      */
     protected void setUpdateMethod(final String updateMethod)
     {
         immutableSetUpdateMethod(updateMethod);
     }
 
     /**
      * Retrieves the update method.
      * @return such method.
      */
     public String getUpdateMethod()
     {
         return m__strUpdateMethod;
     }
 
     /**
      * Specifies the update parameters Javadoc.
      * @param javadoc such javadoc.
      */
     private void immutableSetUpdateParametersJavadoc(final String javadoc)
     {
         m__strUpdateParametersJavadoc = javadoc;
     }
 
     /**
      * Specifies the update parameters Javadoc.
      * @param javadoc such javadoc.
      */
     protected void setUpdateParametersJavadoc(final String javadoc)
     {
         immutableSetUpdateParametersJavadoc(javadoc);
     }
 
     /**
      * Retrieves the update parameters javadoc.
      * @return such information.
      */
     public String getUpdateParametersJavadoc()
     {
         return m__strUpdateParametersJavadoc;
     }
 
     /**
      * Specifies the update parameters Declaration.
      * @param declaration such declaration.
      */
     private void immutableSetUpdateParametersDeclaration(final String declaration)
     {
         m__strUpdateParametersDeclaration = declaration;
     }
 
     /**
      * Specifies the update parameters Declaration.
      * @param declaration such declaration.
      */
     protected void setUpdateParametersDeclaration(final String declaration)
     {
         immutableSetUpdateParametersDeclaration(declaration);
     }
 
     /**
      * Retrieves the update parameters declaration.
      * @return such information.
      */
     public String getUpdateParametersDeclaration()
     {
         return m__strUpdateParametersDeclaration;
     }
 
     /**
      * Specifies the update parameters Specification.
      * @param specification such specification.
      */
     private void immutableSetUpdateParametersSpecification(final String specification)
     {
         m__strUpdateParametersSpecification = specification;
     }
 
     /**
      * Specifies the update parameters Specification.
      * @param specification such specification.
      */
     protected void setUpdateParametersSpecification(final String specification)
     {
         immutableSetUpdateParametersSpecification(specification);
     }
 
     /**
      * Retrieves the update parameters specification.
      * @return such information.
      */
     public String getUpdateParametersSpecification()
     {
         return m__strUpdateParametersSpecification;
     }
 
     /**
      * Specifies the update filter.
      * @param updateFilter such filter.
      */
     private void immutableSetUpdateFilter(final String updateFilter)
     {
         m__strUpdateFilter = updateFilter;
     }
 
     /**
      * Specifies the update filter.
      * @param updateFilter such filter.
      */
     protected void setUpdateFilter(final String updateFilter)
     {
         immutableSetUpdateFilter(updateFilter);
     }
 
     /**
      * Retrieves the update filter.
      * @return such filter.
      */
     public String getUpdateFilter()
     {
         return m__strUpdateFilter;
     }
 
     /**
      * Specifies the delete method.
      * @param deleteMethod such method.
      */
     private void immutableSetDeleteMethod(
         String deleteMethod)
     {
         m__strDeleteMethod = deleteMethod;
     }
 
     /**
      * Specifies the delete method.
      * @param deleteMethod such method.
      */
     protected void setDeleteMethod(
         String deleteMethod)
     {
         immutableSetDeleteMethod(
             deleteMethod);
     }
 
     /**
      * Retrieves the delete method.
      * @return such method.
      */
     public String getDeleteMethod()
     {
         return m__strDeleteMethod;
     }
 
     /**
      * Specifies the delete pk Javadoc.
      * @param deletePkJavadoc such Javadoc.
      */
     private void immutableSetDeletePkJavadoc(
         String deletePkJavadoc)
     {
         m__strDeletePkJavadoc = deletePkJavadoc;
     }
 
     /**
      * Specifies the delete pk Javadoc.
      * @param deletePkJavadoc such Javadoc.
      */
     protected void setDeletePkJavadoc(
         String deletePkJavadoc)
     {
         immutableSetDeletePkJavadoc(
             deletePkJavadoc);
     }
 
     /**
      * Retrieves the delete pk Javadoc.
      * @return such Javadoc.
      */
     public String getDeletePkJavadoc()
     {
         return m__strDeletePkJavadoc;
     }
     
     /**
      * Specifies the delete pk declaration.
      * @param deletePkDeclaration such declaration.
      */
     private void immutableSetDeletePkDeclaration(
         String deletePkDeclaration)
     {
         m__strDeletePkDeclaration =
             deletePkDeclaration;
     }
 
     /**
      * Specifies the delete pk declaration.
      * @param deletePkdeclaration such declaration.
      */
     protected void setDeletePkdeclaration(
         String deletePkDeclaration)
     {
         immutableSetDeletePkDeclaration(
             deletePkDeclaration);
     }
 
     /**
      * Retrieves the delete pk declaration.
      * @return such declaration.
      */
     public String getDeletePkDeclaration()
     {
         return m__strDeletePkDeclaration;
     }
 
     /**
      * Specifies the delete filter declaration.
      * @param deletePkFilterDeclaration such declaration.
      */
     private void immutableSetDeleteFilterDeclaration(
         String deleteFilterDeclaration)
     {
         m__strDeleteFilterDeclaration =
             deleteFilterDeclaration;
     }
 
     /**
      * Specifies the delete filter declaration.
      * @param deleteFilterDeclaration such declaration.
      */
     protected void setDeleteFilterDeclaration(
         String deleteFilterDeclaration)
     {
         immutableSetDeleteFilterDeclaration(
             deleteFilterDeclaration);
     }
 
     /**
      * Retrieves the delete filter declaration.
      * @return such declaration.
      */
     public String getDeleteFilterDeclaration()
     {
         return m__strDeleteFilterDeclaration;
     }
 
     /**
      * Specifies the delete filter values.
      * @param deleteFilterValues such values.
      */
     private void immutableSetDeleteFilterValues(
         String deleteFilterValues)
     {
         m__strDeleteFilterValues =
             deleteFilterValues;
     }
 
     /**
      * Specifies the delete filter values.
      * @param deleteFilterValues such values.
      */
     protected void setDeleteFilterValues(
         String deleteFilterValues)
     {
         immutableSetDeleteFilterValues(
             deleteFilterValues);
     }
 
     /**
      * Retrieves the delete filter values.
      * @return such values.
      */
     public String getDeleteFilterValues()
     {
         return m__strDeleteFilterValues;
     }
     
     /**
      * Specifies the delete with FK method.
      * @param deleteWithFkMethod such method.
      */
     private void immutableSetDeleteWithFkMethod(
         String deleteWithFkMethod)
     {
         m__strDeleteWithFkMethod = deleteWithFkMethod;
     }
 
     /**
      * Specifies the delete with FK method.
      * @param deleteWithFkMethod such method.
      */
     protected void setDeleteWithFkMethod(
         String deleteWithFkMethod)
     {
         immutableSetDeleteWithFkMethod(
             deleteWithFkMethod);
     }
 
     /**
      * Retrieves the delete with FK method.
      * @return such method.
      */
     public String getDeleteWithFkMethod()
     {
         return m__strDeleteWithFkMethod;
     }
 
     /**
      * Specifies the delete with FK PK Javadoc.
      * @param deleteWithFkPkJavadoc such Javadoc.
      */
     private void immutableSetDeleteWithFkPkJavadoc(
         String deleteWithFkPkJavadoc)
     {
         m__strDeleteWithFkPkJavadoc = deleteWithFkPkJavadoc;
     }
 
     /**
      * Specifies the delete with FK PK Javadoc.
      * @param deleteWithFkPkJavadoc such Javadoc.
      */
     protected void setDeleteWithFkPkJavadoc(
         String deleteWithFkPkJavadoc)
     {
         immutableSetDeleteWithFkPkJavadoc(
             deleteWithFkPkJavadoc);
     }
 
     /**
      * Retrieves the delete with FK PK Javadoc.
      * @return such Javadoc.
      */
     public String getDeleteWithFkPkJavadoc()
     {
         return m__strDeleteWithFkPkJavadoc;
     }
     
     /**
      * Specifies the delete with FK PK declaration.
      * @param deleteWithFkPkDeclaration such declaration.
      */
     private void immutableSetDeleteWithFkPkDeclaration(
         String deleteWithFkPkDeclaration)
     {
         m__strDeleteWithFkPkDeclaration =
             deleteWithFkPkDeclaration;
     }
 
     /**
      * Specifies the delete with FK PK declaration.
      * @param deleteWithFkPkdeclaration such declaration.
      */
     protected void setDeleteWithFkPkdeclaration(
         String deleteWithFkPkDeclaration)
     {
         immutableSetDeleteWithFkPkDeclaration(
             deleteWithFkPkDeclaration);
     }
 
     /**
      * Retrieves the delete with FK PK declaration.
      * @return such declaration.
      */
     public String getDeleteWithFkPkDeclaration()
     {
         return m__strDeleteWithFkPkDeclaration;
     }
 
     /**
      * Specifies the delete with FK DAO delete request.
      * @param deleteWithFkDAODeleteRequest such request.
      */
     private void immutableSetDeleteWithFkDAODeleteRequest(
         String deleteWithFkDAODeleteRequest)
     {
         m__strDeleteWithFkDAODeleteRequest =
             deleteWithFkDAODeleteRequest;
     }
 
     /**
      * Specifies the delete with FK DAO delete request.
      * @param deleteWithFkDAODeleteRequest such request.
      */
     protected void setDeleteWithFkDAODeleteRequest(
         String deleteWithFkDAODeleteRequest)
     {
         immutableSetDeleteWithFkDAODeleteRequest(
             deleteWithFkDAODeleteRequest);
     }
 
     /**
      * Retrieves the delete with FK DAO delete request.
      * @return such request.
      */
     public String getDeleteWithFkDAODeleteRequest()
     {
         return m__strDeleteWithFkDAODeleteRequest;
     }
 
     /**
      * Specifies the delete with FK PK values.
      * @param deleteWithFkPkValues such values.
      */
     private void immutableSetDeleteWithFkPkValues(
         String deleteWithFkPkValues)
     {
         m__strDeleteWithFkPkValues =
             deleteWithFkPkValues;
     }
 
     /**
      * Specifies the delete with FK PK values.
      * @param deleteWithFkPkValues such values.
      */
     protected void setDeleteWithFkPkValues(
         String deleteWithFkPkValues)
     {
         immutableSetDeleteWithFkPkValues(
             deleteWithFkPkValues);
     }
 
     /**
      * Retrieves the delete with FK PK values.
      * @return such values.
      */
     public String getDeleteWithFkPkValues()
     {
         return m__strDeleteWithFkPkValues;
     }
 
     /**
      * Specifies the custom select template.
      * @param select such template.
      */
     private void immutableSetCustomSelect(
         final String select)
     {
         m__strCustomSelect = select;
     }
 
     /**
      * Specifies the custom select template.
      * @param select such template.
      */
     protected void setCustomSelect(
         final String select)
     {
         immutableSetCustomSelect(select);
     }
 
     /**
      * Retrieves the custom select template.
      * @return such template.
      */
     public String getCustomSelect()
     {
         return m__strCustomSelect;
     }
 
     /**
      * Specifies the custom select parameter Javadoc template.
      * @param select such template.
      */
     private void immutableSetCustomSelectParameterJavadoc(
         final String template)
     {
         m__strCustomSelectParameterJavadoc = template;
     }
 
     /**
      * Specifies the custom select parameter Javadoc template.
      * @param select such template.
      */
     protected void setCustomSelectParameterJavadoc(
         final String select)
     {
         immutableSetCustomSelectParameterJavadoc(select);
     }
 
     /**
      * Retrieves the custom select parameter Javadoc template.
      * @return such template.
      */
     public String getCustomSelectParameterJavadoc()
     {
         return m__strCustomSelectParameterJavadoc;
     }
 
     /**
      * Specifies the custom select parameter declaration template.
      * @param select such template.
      */
     private void immutableSetCustomSelectParameterDeclaration(
         final String template)
     {
         m__strCustomSelectParameterDeclaration = template;
     }
 
     /**
      * Specifies the custom select parameter declaration template.
      * @param select such template.
      */
     protected void setCustomSelectParameterDeclaration(
         final String select)
     {
         immutableSetCustomSelectParameterDeclaration(select);
     }
 
     /**
      * Retrieves the custom select parameter declaration template.
      * @return such template.
      */
     public String getCustomSelectParameterDeclaration()
     {
         return m__strCustomSelectParameterDeclaration;
     }
 
     /**
      * Specifies the custom select parameter values template.
      * @param select such template.
      */
     private void immutableSetCustomSelectParameterValues(
         final String template)
     {
         m__strCustomSelectParameterValues = template;
     }
 
     /**
      * Specifies the custom select parameter values template.
      * @param select such template.
      */
     protected void setCustomSelectParameterValues(
         final String select)
     {
         immutableSetCustomSelectParameterValues(select);
     }
 
     /**
      * Retrieves the custom select parameter values template.
      * @return such template.
      */
     public String getCustomSelectParameterValues()
     {
         return m__strCustomSelectParameterValues;
     }
 
     /**
      * Specifies the custom select result properties' values template.
      * @param select such template.
      */
     private void immutableSetCustomSelectResultPropertyValues(
         final String template)
     {
         m__strCustomSelectResultPropertyValues = template;
     }
 
     /**
      * Specifies the custom select result properties' values template.
      * @param select such template.
      */
     protected void setCustomSelectResultPropertyValues(
         final String select)
     {
         immutableSetCustomSelectResultPropertyValues(select);
     }
 
     /**
      * Retrieves the custom select result properties' values template.
      * @return such template.
      */
     public String getCustomSelectResultPropertyValues()
     {
         return m__strCustomSelectResultPropertyValues;
     }
 
     /**
      * Specifies the custom update template.
      * @param update such template.
      */
     private void immutableSetCustomUpdate(
         final String update)
     {
         m__strCustomUpdate = update;
     }
 
     /**
      * Specifies the custom update template.
      * @param update such template.
      */
     protected void setCustomUpdate(
         final String update)
     {
         immutableSetCustomUpdate(update);
     }
 
     /**
      * Retrieves the custom update template.
      * @return such template.
      */
     public String getCustomUpdate()
     {
         return m__strCustomUpdate;
     }
 
     /**
      * Specifies the custom update parameter Javadoc template.
      * @param update such template.
      */
     private void immutableSetCustomUpdateParameterJavadoc(
         final String template)
     {
         m__strCustomUpdateParameterJavadoc = template;
     }
 
     /**
      * Specifies the custom update parameter Javadoc template.
      * @param update such template.
      */
     protected void setCustomUpdateParameterJavadoc(
         final String update)
     {
         immutableSetCustomUpdateParameterJavadoc(update);
     }
 
     /**
      * Retrieves the custom update parameter Javadoc template.
      * @return such template.
      */
     public String getCustomUpdateParameterJavadoc()
     {
         return m__strCustomUpdateParameterJavadoc;
     }
 
     /**
      * Specifies the custom update parameter declaration template.
      * @param update such template.
      */
     private void immutableSetCustomUpdateParameterDeclaration(
         final String template)
     {
         m__strCustomUpdateParameterDeclaration = template;
     }
 
     /**
      * Specifies the custom update parameter declaration template.
      * @param update such template.
      */
     protected void setCustomUpdateParameterDeclaration(
         final String update)
     {
         immutableSetCustomUpdateParameterDeclaration(update);
     }
 
     /**
      * Retrieves the custom update parameter declaration template.
      * @return such template.
      */
     public String getCustomUpdateParameterDeclaration()
     {
         return m__strCustomUpdateParameterDeclaration;
     }
 
     /**
      * Specifies the custom update parameter values template.
      * @param update such template.
      */
     private void immutableSetCustomUpdateParameterValues(
         final String template)
     {
         m__strCustomUpdateParameterValues = template;
     }
 
     /**
      * Specifies the custom update parameter values template.
      * @param update such template.
      */
     protected void setCustomUpdateParameterValues(
         final String update)
     {
         immutableSetCustomUpdateParameterValues(update);
     }
 
     /**
      * Retrieves the custom update parameter values template.
      * @return such template.
      */
     public String getCustomUpdateParameterValues()
     {
         return m__strCustomUpdateParameterValues;
     }
 
     /**
      * Specifies the custom update query line template.
      * @param update such template.
      */
     private void immutableSetCustomUpdateQueryLine(
         final String template)
     {
         m__strCustomUpdateQueryLine = template;
     }
 
     /**
      * Specifies the custom update query line template.
      * @param update such template.
      */
     protected void setCustomUpdateQueryLine(
         final String update)
     {
         immutableSetCustomUpdateQueryLine(update);
     }
 
     /**
      * Retrieves the custom update query line template.
      * @return such template.
      */
     public String getCustomUpdateQueryLine()
     {
         return m__strCustomUpdateQueryLine;
     }
 
     /**
      * Specifies the class end.
      * @param classEnd the new class end.
      */
     private void immutableSetClassEnd(final String classEnd)
     {
         m__strClassEnd = classEnd;
     }
 
     /**
      * Specifies the class end.
      * @param classEnd the new class end.
      */
     protected void setClassEnd(final String classEnd)
     {
         immutableSetClassEnd(classEnd);
     }
 
     /**
      * Retrieves the class end.
      * @return such information.
      */
     public String getClassEnd() 
     {
         return m__strClassEnd;
     }
 
     /**
      * Retrieves the source code generated by this template.
      * @return such code.
      */
     public String toString()
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         EnglishGrammarUtils t_EnglishGrammarUtils =
             EnglishGrammarUtils.getInstance();
         StringUtils t_StringUtils = StringUtils.getInstance();
 
         StringValidator t_StringValidator = StringValidator.getInstance();
 
         MetaDataUtils t_MetaDataUtils = MetaDataUtils.getInstance();
 
         TableTemplate t_TableTemplate = getTableTemplate();
 
         DatabaseMetaDataManager t_MetaDataManager = getMetaDataManager();
 
         PackageUtils t_PackageUtils = PackageUtils.getInstance();
 
         if  (   (t_TableTemplate   != null)
              && (t_MetaDataManager != null)
              && (t_MetaDataUtils   != null)
              && (t_StringUtils     != null)
              && (t_PackageUtils    != null))
         {
             String t_strRepositoryName =
                 t_StringUtils.capitalize(
                     getRepositoryName(),
                     '_');
 
             MessageFormat t_HeaderFormatter = new MessageFormat(getHeader());
 
             MessageFormat t_PackageDeclarationFormatter =
                 new MessageFormat(getPackageDeclaration());
 
             MessageFormat t_ProjectImportFormatter =
                 new MessageFormat(getProjectImports());
 
             MessageFormat t_ForeignDAOImportsFormatter =
                 new MessageFormat(getForeignDAOImports());
 
             MessageFormat t_JavadocFormatter = new MessageFormat(getJavadoc());
 
             MessageFormat t_ClassDefinitionFormatter =
                 new MessageFormat(getClassDefinition());
 
             MessageFormat t_ClassConstructorFormatter =
                 new MessageFormat(getClassConstructor());
 
             MessageFormat t_FindByPrimaryKeyFormatter =
                 new MessageFormat(getFindByPrimaryKeyMethod());
 
             MessageFormat t_FindByPrimaryKeyPkJavadocFormatter =
                 new MessageFormat(getFindByPrimaryKeyPkJavadoc());
 
             MessageFormat t_FindByPrimaryKeyPkDeclarationFormatter =
                 new MessageFormat(getFindByPrimaryKeyPkDeclaration());
 
             MessageFormat t_FindByPrimaryKeySelectFieldsFormatter =
                 new MessageFormat(getFindByPrimaryKeySelectFields());
 
             MessageFormat t_FindByPrimaryKeyFilterDeclarationFormatter =
                 new MessageFormat(getFindByPrimaryKeyFilterDeclaration());
 
             MessageFormat t_FindByPrimaryKeyFilterValuesFormatter =
                 new MessageFormat(getFindByPrimaryKeyFilterValues());
 
             MessageFormat t_BuildValueObjectMethodFormatter =
                 new MessageFormat(getBuildValueObjectMethod());
 
             MessageFormat t_BuildValueObjectValueRetrievalFormatter =
                 new MessageFormat(getBuildValueObjectValueRetrieval());
 
             MessageFormat t_InsertMethodFormatter =
                 new MessageFormat(getInsertMethod());
 
             MessageFormat t_InsertParametersJavadocFormatter =
                 new MessageFormat(getInsertParametersJavadoc());
 
             MessageFormat t_InsertParametersDeclarationFormatter =
                 new MessageFormat(getInsertParametersDeclaration());
 
             MessageFormat t_InsertParametersSpecificationFormatter =
                 new MessageFormat(getInsertParametersSpecification());
 
             MessageFormat t_InsertKeywordParametersSpecificationFormatter =
                 new MessageFormat(getInsertKeywordParametersSpecification());
 
             MessageFormat t_UpdateMethodFormatter =
                 new MessageFormat(getUpdateMethod());
 
             MessageFormat t_UpdateParametersJavadocFormatter =
                 new MessageFormat(getUpdateParametersJavadoc());
 
             MessageFormat t_UpdateParametersDeclarationFormatter =
                 new MessageFormat(getUpdateParametersDeclaration());
 
             MessageFormat t_UpdateParametersSpecificationFormatter =
                 new MessageFormat(getUpdateParametersSpecification());
 
             MessageFormat t_UpdateFilterFormatter =
                 new MessageFormat(getUpdateFilter());
 
             MessageFormat t_DeleteMethodFormatter =
                 new MessageFormat(getDeleteMethod());
 
             MessageFormat t_DeleteWithFkMethodFormatter =
                 new MessageFormat(getDeleteWithFkMethod());
 
             MessageFormat t_DeleteWithFkPkJavadocFormatter =
                 new MessageFormat(getDeleteWithFkPkJavadoc());
 
             MessageFormat t_DeleteWithFkPkDeclarationFormatter =
                 new MessageFormat(getDeleteWithFkPkDeclaration());
 
             MessageFormat t_DeleteWithFkDAODeleteRequestFormatter =
                 new MessageFormat(getDeleteWithFkDAODeleteRequest());
 
             MessageFormat t_DeleteWithFkPkValuesFormatter =
                 new MessageFormat(getDeleteWithFkPkValues());
 
             StringBuffer t_sbForeignDAOImports = new StringBuffer();
             StringBuffer t_sbPkJavadoc = new StringBuffer();
             StringBuffer t_sbPkDeclaration = new StringBuffer();
             StringBuffer t_sbUpdateFilter = new StringBuffer();
             StringBuffer t_sbDeleteMethod = new StringBuffer();
             StringBuffer t_sbSelectFields = new StringBuffer();
             StringBuffer t_sbFilterDeclaration = new StringBuffer();
             StringBuffer t_sbFilterValues = new StringBuffer();
             StringBuffer t_sbDeleteWithFkPkValues = new StringBuffer();
             StringBuffer t_sbDeleteWithFkPkValuesDeleteRequest = new StringBuffer();
 
             StringBuffer t_sbDeleteWithFkMethod = new StringBuffer();
             StringBuffer t_sbDeleteWithFkPkJavadoc = new StringBuffer();
             StringBuffer t_sbDeleteWithFkPkDeclaration = new StringBuffer();
             StringBuffer t_sbDeleteWithFkDAODeleteRequest = new StringBuffer();
             StringBuffer t_sbDeleteWithFkDAOFkValues = new StringBuffer();
 
             StringBuffer t_sbBuildValueObjectRetrieval     = new StringBuffer();
             StringBuffer t_sbInsertParametersJavadoc       = new StringBuffer();
             StringBuffer t_sbInsertParametersDeclaration   = new StringBuffer();
             StringBuffer t_sbInsertParametersSpecification = new StringBuffer();
             StringBuffer t_sbUpdateParametersJavadoc       = new StringBuffer();
             StringBuffer t_sbUpdateParametersDeclaration   = new StringBuffer();
             StringBuffer t_sbUpdateParametersSpecification = new StringBuffer();
 
             String t_strDeleteMethodModifier = "public";
             String t_strDeleteMethodSuffix = "";
 
             boolean t_bForeignKeys = false;
 
             String[] t_astrReferredTables =
                 t_MetaDataManager.getReferredTables(
                     t_TableTemplate.getTableName());
 
             if  (t_astrReferredTables != null)
             {
                 for  (int t_iRefTableIndex = 0;
                           t_iRefTableIndex < t_astrReferredTables.length;
                           t_iRefTableIndex++)
                 {
                     t_bForeignKeys = true;
 
                     String t_strReferredTableName =
                         t_StringUtils.capitalize(
                             t_EnglishGrammarUtils.getSingular(
                                 t_astrReferredTables[t_iRefTableIndex]),
                             '_');
 
                     String t_strFkName =
                         t_MetaDataManager.getReferredKey(
                             t_TableTemplate.getTableName(),
                             t_astrReferredTables[t_iRefTableIndex]);
 
                     t_sbDeleteWithFkDAODeleteRequest.append(
                         t_DeleteWithFkDAODeleteRequestFormatter.format(
                             new Object[]
                             {
                                 t_strReferredTableName,
                                 t_TableTemplate.getTableName().toLowerCase(),
                                 t_StringUtils.capitalize(
                                     t_MetaDataManager.getForeignKey(
                                         t_TableTemplate.getTableName(),
                                         t_astrReferredTables[t_iRefTableIndex])
                                     .toLowerCase(),
                                     '_')
                             }));
 
                     t_sbForeignDAOImports.append(
                         t_ForeignDAOImportsFormatter.format(
                             new Object[]
                             {
                                 t_PackageUtils.retrieveBaseDAOPackage(
                                     getBasePackageName()),
                                 t_strReferredTableName
                                 
                             }));
                 }
             }
 
             t_sbResult.append(
                 t_HeaderFormatter.format(
                     new Object[]
                     {
                         getEngineName(),
                         getEngineVersion(),
                         t_EnglishGrammarUtils.getSingular(
                             t_TableTemplate.getTableName())
                     }));
 
             t_sbResult.append(
                 t_PackageDeclarationFormatter.format(
                     new Object[]{getPackageName()}));
 
             t_sbResult.append(
                 t_ProjectImportFormatter.format(
                     new Object[]
                     {
                         t_PackageUtils.retrieveJdbcDAOPackage(
                             getBasePackageName()),
                         t_PackageUtils.retrieveValueObjectPackage(
                             getBasePackageName()),
                         t_StringUtils.capitalize(
                             t_EnglishGrammarUtils.getSingular(
                                 t_TableTemplate.getTableName().toLowerCase()),
                             '_'),
                         t_PackageUtils.retrieveBaseDAOPackage(
                             getBasePackageName()),
                         t_PackageUtils.retrieveBaseDAOFactoryPackage(
                             getBasePackageName()),
                         t_PackageUtils.retrieveTableRepositoryPackage(
                             getBasePackageName()),
                         t_strRepositoryName,
                         t_PackageUtils.retrieveDataAccessManagerPackage(
                             getBasePackageName()),
                         t_PackageUtils.retrieveKeywordRepositoryPackage(
                             getBasePackageName()),
                         t_sbForeignDAOImports
                     }));
 
             t_sbResult.append(getAcmslImports());
             t_sbResult.append(getJdkImports());
             t_sbResult.append(getJdkExtensionImports());
             t_sbResult.append(getLoggingImports());
 
             t_sbResult.append(
                 t_JavadocFormatter.format(
                     new Object[]
                     {
                         getEngineName(),
                         getEngineVersion(),
                         t_EnglishGrammarUtils.getSingular(
                             t_TableTemplate.getTableName())
                     }));
 
             t_sbResult.append(
                 t_ClassDefinitionFormatter.format(
                     new Object[]
                     {
                         getEngineName(),
                         t_StringUtils.capitalize(
                             t_EnglishGrammarUtils.getSingular(
                                 t_TableTemplate.getTableName().toLowerCase()),
                             '_')
                     }));
 
             t_sbResult.append(getClassStart());
 
             t_sbResult.append(
                 t_ClassConstructorFormatter.format(
                     new Object[]
                     {
                         getEngineName(),
                         t_StringUtils.capitalize(
                             t_EnglishGrammarUtils.getSingular(
                                 t_TableTemplate.getTableName().toLowerCase()),
                             '_')
                     }));
 
             String[] t_astrPrimaryKeys =
                 t_MetaDataManager.getPrimaryKeys(t_TableTemplate.getTableName());
 
             if  (t_astrPrimaryKeys != null)
             {
                 for  (int t_iPkIndex = 0;
                           t_iPkIndex < t_astrPrimaryKeys.length;
                           t_iPkIndex++)
                 {
                     t_sbPkJavadoc.append(
                         t_FindByPrimaryKeyPkJavadocFormatter.format(
                             new Object[]
                             {
                                 t_astrPrimaryKeys[t_iPkIndex].toLowerCase(),
                                 t_astrPrimaryKeys[t_iPkIndex]
                             }));
 
                     t_sbPkDeclaration.append(
                         t_FindByPrimaryKeyPkDeclarationFormatter.format(
                             new Object[]
                             {
                                 t_MetaDataUtils.getNativeType(
                                     t_MetaDataManager.getColumnType(
                                         t_TableTemplate.getTableName(),
                                         t_astrPrimaryKeys[t_iPkIndex])),
                                 t_astrPrimaryKeys[t_iPkIndex].toLowerCase()
                             }));
 
                     t_sbFilterValues.append(
                         t_FindByPrimaryKeyFilterValuesFormatter.format(
                             new Object[]
                             {
                                 t_StringUtils.capitalize(
                                     t_MetaDataUtils.getNativeType(
                                         t_MetaDataManager.getColumnType(
                                             t_TableTemplate.getTableName(),
                                             t_astrPrimaryKeys[t_iPkIndex])),
                                     '_'),
                                 t_strRepositoryName,
                                 t_TableTemplate.getTableName().toUpperCase(),
                                 t_astrPrimaryKeys[t_iPkIndex].toUpperCase(),
                                 t_astrPrimaryKeys[t_iPkIndex].toLowerCase()
                             }));
 
                     t_sbFilterDeclaration.append(
                         t_FindByPrimaryKeyFilterDeclarationFormatter.format(
                             new Object[]
                             {
                                 t_strRepositoryName,
                                 t_TableTemplate.getTableName().toUpperCase(),
                                 t_astrPrimaryKeys[t_iPkIndex].toUpperCase()
                             }));
 
                     t_sbUpdateFilter.append(
                         t_UpdateFilterFormatter.format(
                             new Object[]
                             {
                                 t_strRepositoryName,
                                 t_TableTemplate.getTableName().toUpperCase(),
                                 t_astrPrimaryKeys[t_iPkIndex].toUpperCase(),
                                 t_astrPrimaryKeys[t_iPkIndex].toLowerCase()
                             }));
 
                     t_sbDeleteWithFkPkValues.append(
                         t_DeleteWithFkPkValuesFormatter.format(
                             new Object[]
                             {
                                 t_astrPrimaryKeys[t_iPkIndex].toLowerCase()
                             }));
 
                     t_sbDeleteWithFkPkValuesDeleteRequest.append(
                         t_StringUtils.capitalize(
                             t_astrPrimaryKeys[t_iPkIndex].toLowerCase(),
                             '_'));
                 }
             }
 
             String[] t_astrColumnNames =
                 t_MetaDataManager.getColumnNames(t_TableTemplate.getTableName());
 
             if  (t_astrColumnNames != null)
             {
                 for  (int t_iColumnIndex = 0;
                           t_iColumnIndex < t_astrColumnNames.length;
                           t_iColumnIndex++)
                 {
                     t_sbSelectFields.append(
                         t_FindByPrimaryKeySelectFieldsFormatter.format(
                             new Object[]
                             {
                                 t_strRepositoryName,
                                 t_TableTemplate.getTableName().toUpperCase(),
                                 t_astrColumnNames[t_iColumnIndex].toUpperCase()
                             }));
 
                     t_sbBuildValueObjectRetrieval.append(
                         t_BuildValueObjectValueRetrievalFormatter.format(
                             new Object[]
                             {
                                 /*
                                 t_StringUtils.capitalize(
                                     t_MetaDataUtils.getNativeType(
                                         t_MetaDataManager.getColumnType(
                                             t_TableTemplate.getTableName(),
                                             t_astrColumnNames[t_iColumnIndex])),
                                     '_'),
                                 "Phoenix",
                                 t_TableTemplate.getTableName().toUpperCase(),
                                 t_astrColumnNames[t_iColumnIndex].toUpperCase()
                                 */
                                 t_MetaDataUtils.getGetterMethod(
                                     t_MetaDataManager.getColumnType(
                                         t_TableTemplate.getTableName(),
                                         t_astrColumnNames[t_iColumnIndex]),
                                     t_strRepositoryName + "."
                                     + t_TableTemplate.getTableName().toUpperCase() + "."
                                     + t_astrColumnNames[t_iColumnIndex].toUpperCase())
                             }));
 
                     if  (!t_MetaDataManager.isManagedExternally(
                              t_TableTemplate.getTableName(),
                              t_astrColumnNames[t_iColumnIndex]))
                     {
                         t_sbInsertParametersDeclaration.append(
                             t_InsertParametersDeclarationFormatter.format(
                                 new Object[]
                                 {
                                     t_MetaDataUtils.getNativeType(
                                         t_MetaDataManager.getColumnType(
                                             t_TableTemplate.getTableName(),
                                             t_astrColumnNames[t_iColumnIndex])),
                                     t_astrColumnNames[t_iColumnIndex].toLowerCase()
                                 }));
 
                         t_sbInsertParametersDeclaration.append(",");
 
                         t_sbInsertParametersSpecification.append(
                             t_InsertParametersSpecificationFormatter.format(
                                 new Object[]
                                 {
                                     t_strRepositoryName,
                                     t_TableTemplate.getTableName().toUpperCase(),
                                     t_astrColumnNames[t_iColumnIndex].toUpperCase(),
                                     t_astrColumnNames[t_iColumnIndex].toLowerCase()
                                 }));
                     }
                     else 
                     {
                         String t_strValue =
                             t_MetaDataManager.getKeyword(
                                 t_TableTemplate.getTableName(),
                                 t_astrColumnNames[t_iColumnIndex]);
 
                         if  (t_StringValidator.isEmpty(t_strValue))
                         {
                             t_strValue =
                                 t_astrColumnNames[t_iColumnIndex].toLowerCase();
                         }
                         else 
                         {
                             t_strValue =
                                 t_StringUtils.normalize(t_strValue, '_');
 
                         }
 
                         t_sbInsertParametersSpecification.append(
                             t_InsertKeywordParametersSpecificationFormatter.format(
                                 new Object[]
                                 {
                                     t_strRepositoryName,
                                     t_TableTemplate.getTableName().toUpperCase(),
                                     t_astrColumnNames[t_iColumnIndex].toUpperCase(),
                                     t_strValue
                                 }));
                     }
 
                     if  (!t_MetaDataManager.isPrimaryKey(
                              t_TableTemplate.getTableName(),
                              t_astrColumnNames[t_iColumnIndex]))
                     {
                         t_sbInsertParametersJavadoc.append(
                             t_InsertParametersJavadocFormatter.format(
                                 new Object[]
                                 {
                                     t_astrColumnNames[t_iColumnIndex].toLowerCase(),
                                     t_astrColumnNames[t_iColumnIndex]
                                 }));
 
                         t_sbUpdateParametersJavadoc.append(
                             t_UpdateParametersJavadocFormatter.format(
                                 new Object[]
                                 {
                                     t_astrColumnNames[t_iColumnIndex].toLowerCase(),
                                     t_astrColumnNames[t_iColumnIndex]
                                 }));
 
                         t_sbUpdateParametersDeclaration.append(
                             t_UpdateParametersDeclarationFormatter.format(
                                 new Object[]
                                 {
                                     t_MetaDataUtils.getNativeType(
                                         t_MetaDataManager.getColumnType(
                                             t_TableTemplate.getTableName(),
                                             t_astrColumnNames[t_iColumnIndex])),
                                     t_astrColumnNames[t_iColumnIndex].toLowerCase()
                                 }));
 
                         t_sbUpdateParametersDeclaration.append(",");
 
                         t_sbUpdateParametersSpecification.append(
                             t_UpdateParametersSpecificationFormatter.format(
                                 new Object[]
                                 {
                                     t_strRepositoryName,
                                     t_TableTemplate.getTableName().toUpperCase(),
                                     t_astrColumnNames[t_iColumnIndex].toUpperCase(),
                                     t_astrColumnNames[t_iColumnIndex].toLowerCase()
                                 }));
                     }
 
                     if  (t_iColumnIndex < t_astrColumnNames.length - 1)
                     {
                         t_sbBuildValueObjectRetrieval.append(",");
                     }
                 }
 
                 t_sbResult.append(
                     t_FindByPrimaryKeyFormatter.format(
                         new Object[]
                         {
                             t_TableTemplate.getTableName(),
                             t_sbPkJavadoc,
                             t_StringUtils.capitalize(
                                 t_EnglishGrammarUtils.getSingular(
                                     t_TableTemplate.getTableName().toLowerCase()),
                                 '_'),
                             t_sbPkDeclaration,
                             t_sbSelectFields,
                             t_strRepositoryName,
                             t_TableTemplate.getTableName().toUpperCase(),
                             t_sbFilterDeclaration,
                             t_sbFilterValues
                         }));
 
                 t_sbResult.append(
                     t_BuildValueObjectMethodFormatter.format(
                         new Object[]
                         {
                             t_StringUtils.capitalize(
                                 t_EnglishGrammarUtils.getSingular(
                                 t_TableTemplate.getTableName().toLowerCase()),
                                 '_'),
                             t_sbBuildValueObjectRetrieval
                         }));
 
                 t_sbResult.append(
                     t_InsertMethodFormatter.format(
                         new Object[]
                         {
                             t_StringUtils.capitalize(
                                 t_EnglishGrammarUtils.getSingular(
                                     t_TableTemplate.getTableName().toLowerCase()),
                                 '_'),
                             t_sbPkJavadoc.toString(),
                             t_sbInsertParametersJavadoc,
                             t_sbInsertParametersDeclaration,
                             t_strRepositoryName,
                             t_TableTemplate.getTableName().toUpperCase(),
                             t_sbInsertParametersSpecification,
                         }));
 
                 t_sbResult.append(
                     t_UpdateMethodFormatter.format(
                         new Object[]
                         {
                             t_StringUtils.capitalize(
                                 t_EnglishGrammarUtils.getSingular(
                                     t_TableTemplate.getTableName().toLowerCase()),
                                 '_'),
                             t_sbPkJavadoc.toString(),
                             t_sbUpdateParametersJavadoc,
                             t_sbPkDeclaration,
                             t_sbUpdateParametersDeclaration,
                             t_strRepositoryName,
                             t_TableTemplate.getTableName().toUpperCase(),
                             t_sbUpdateParametersSpecification,
                             t_sbUpdateFilter
                         }));
 
 
                 if  (t_bForeignKeys)
                 {
                     t_strDeleteMethodModifier = "protected";
                     t_strDeleteMethodSuffix = 
                         t_StringUtils.capitalize(
                             t_TableTemplate.getTableName(),
                             '_');
 
                     t_sbDeleteWithFkMethod.append(
                         t_DeleteWithFkMethodFormatter.format(
                             new Object[]
                             {
                                 t_StringUtils.capitalize(
                                     t_EnglishGrammarUtils.getSingular(
                                         t_TableTemplate.getTableName()),
                                     '_'),
                                 t_sbPkJavadoc,
                                 t_sbPkDeclaration,
                                 t_sbDeleteWithFkPkValues,
                                 t_TableTemplate.getTableName().toLowerCase(),
                                 t_sbDeleteWithFkDAODeleteRequest,
                                 t_sbDeleteWithFkPkValuesDeleteRequest
                             }));
 
                     t_sbResult.append(t_sbDeleteWithFkMethod);
                 }
                 
                 t_sbDeleteMethod.append(
                     t_DeleteMethodFormatter.format(
                         new Object[]
                         {
                             t_TableTemplate.getTableName(),
                             t_sbPkJavadoc,
                             t_sbPkDeclaration,
                             t_strRepositoryName,
                             t_TableTemplate.getTableName().toUpperCase(),
                             t_sbFilterDeclaration,
                             t_sbFilterValues,
                             t_strDeleteMethodModifier,
                             t_strDeleteMethodSuffix
                         }));
 
                 t_sbResult.append(t_sbDeleteMethod);
             }
         }
 
         t_sbResult.append(buildCustomSql());
 
         t_sbResult.append(getClassEnd());
 
         return t_sbResult.toString();
     }
 
     /**
      * Builds the custom templates.
      * @return such generated code.
      */
     protected String buildCustomSql()
     {
         StringBuffer result = new StringBuffer();
 
         CustomSqlProvider provider = getCustomSqlProvider();
 
         if  (provider != null)
         {
             result.append(buildCustomSelects(provider));
             result.append(buildCustomUpdates(provider));
         }
 
         return result.toString();
     }
 
     /**
      * Builds the custom selects.
      * @param provider the CustomSqlProvider instance.
      * @return such generated code.
      * @precondition provider != null
      */
     protected String buildCustomSelects(final CustomSqlProvider provider)
     {
         return
             buildCustomSelects(
                 provider,
                 getCustomSelect(),
                 getCustomSelectParameterJavadoc(),
                 getCustomSelectParameterDeclaration(),
                 getCustomSelectParameterValues(),
                 getCustomSelectResultPropertyValues(),
                 StringUtils.getInstance(),
                 StringValidator.getInstance());
     }
 
     /**
      * Builds the custom selects.
      * @param customSqlProvider the CustomSqlProvider instance.
      * @param customSelect the custom select.
      * @param parameterJavadoc the Javadoc template
      * of the parameters.
      * @param parameterDeclaration the parameter declaration.
      * @param parameterValues the parameter values.
      * @param resultPropertyValues the result property values.
      * @param stringUtils the StringUtils isntance.
      * @param stringValidator the StringValidator instance.
      * @return such generated code.
      * @precondition customSqlProvider != null
      * @precondition customSelect != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterValues != null
      * @precondition resultPropertyValues != null
      * @precondition stringUtils != null
      * @precondition stringValidator != null
      */
     protected String buildCustomSelects(
         final CustomSqlProvider customSqlProvider,
         final String customSelect,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterValues,
         final String resultPropertyValues,
         final StringUtils stringUtils,
         final StringValidator stringValidator)
     {
         StringBuffer result = new StringBuffer();
 
         Collection t_cContents = customSqlProvider.getCollection();
 
         if  (t_cContents != null)
         {
             Iterator t_itContentIterator = t_cContents.iterator();
 
             while  (t_itContentIterator.hasNext())
             {
                 Object t_Content = t_itContentIterator.next();
 
                 if  (t_Content instanceof SqlElement)
                 {
                     SqlElement t_SqlElement =
                         (SqlElement) t_Content;
 
                     if  (t_SqlElement.SELECT.equals(t_SqlElement.getType()))
                     {
                         String[] t_astrParameterTemplates =
                             buildParameterTemplates(
                                 customSqlProvider,
                                 t_SqlElement.getParameterRefs(),
                                 parameterJavadoc,
                                 parameterDeclaration,
                                 parameterValues,
                                 stringUtils,
                                 stringValidator);
 
                         String t_strResultPropertyValues =
                             buildResultPropertyValues(
                                 customSqlProvider,
                                 t_SqlElement.getResultRef(),
                                 resultPropertyValues);
 
                         result.append(
                             buildCustomSelect(
                                 customSqlProvider,
                                 t_SqlElement,
                                 customSelect,
                                 t_astrParameterTemplates[0],
                                 t_astrParameterTemplates[1],
                                 t_astrParameterTemplates[2],
                                 t_strResultPropertyValues,
                                 stringUtils));
                     }
                 }
             }
         }
 
         return result.toString();
     }
 
     /**
      * Builds the parameter templates with given parameters.
      * @param provider the CustomSqlProvider instance.
      * @param parameterRefs the parameter references.
      * @param parameterJavadoc the template.
      * @param parameterDeclaration the parameter declaration.
      * @param parameterValues the parameter values.
      * @param stringUtils the StringUtils instance.
      * @param stringValidator the StringValidator instance.
      * @return the generated code.
      * @precondition provider != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterValues != null
      * @precondition stringUtils != null
      * @precondition stringValidator != null
      */
     protected String[] buildParameterTemplates(
         final CustomSqlProvider provider,
         final Collection parameterRefs,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterValues,
         final StringUtils stringUtils,
         final StringValidator stringValidator)
     {
         String[] result = new String[3];
 
         StringBuffer t_sbParameterJavadoc = new StringBuffer();
         StringBuffer t_sbParameterDeclaration = new StringBuffer();
         StringBuffer t_sbParameterValues = new StringBuffer();
 
         if  (parameterRefs != null)
         {
             MessageFormat t_ParameterJavadocFormatter =
                 new MessageFormat(parameterJavadoc);
 
             MessageFormat t_ParameterDeclarationFormatter =
                 new MessageFormat(parameterDeclaration);
 
             MessageFormat t_ParameterValuesFormatter =
                 new MessageFormat(parameterValues);
 
             Iterator t_itParameterRefs =
                 parameterRefs.iterator();
 
             while  (t_itParameterRefs.hasNext())
             {
                 ParameterRefElement t_ParameterRef =
                     (ParameterRefElement) t_itParameterRefs.next();
 
                 if  (t_ParameterRef != null)
                 {
                     ParameterElement t_Parameter =
                         provider.resolveReference(t_ParameterRef);
 
                     if  (t_Parameter == null)
                     {
                         LogFactory.getLog("custom-sql").warn(
                               "Referenced parameter not found:"
                             + t_ParameterRef.getId());
                     }
                     else
                     {
                         String t_strName = t_Parameter.getName();
 
                         if  (stringValidator.isEmpty(t_strName))
                         {
                             t_strName =
                                 stringUtils.toJavaMethod(
                                     t_Parameter.getId(), '-');
                         }
 
                         t_sbParameterJavadoc.append(
                             t_ParameterJavadocFormatter.format(
                                 new Object[]
                                 {
                                     t_strName
                                 }));
                         t_sbParameterDeclaration.append(
                             t_ParameterDeclarationFormatter.format(
                                 new Object[]
                                 {
                                     t_Parameter.getType(),
                                     t_strName
                                 }));
                         t_sbParameterValues.append(
                             t_ParameterValuesFormatter.format(
                                 new Object[]
                                 {
                                     stringUtils.capitalize(
                                         t_Parameter.getType(), '_'),
                                     (   (t_Parameter.getIndex() > 0)
                                      ?  ("" + t_Parameter.getIndex())
                                      :  "\"" + t_Parameter.getColumnName() + "\""),
                                     t_strName
                                 }));
                     }
                 }
             }
         }
 
         result[0] = t_sbParameterJavadoc.toString();
         result[1] = t_sbParameterDeclaration.toString();
         result[2] = t_sbParameterValues.toString();
 
         return result;
     }
 
     /**
      * Builds the result property values with given input.
      * @param provider the CustomSqlProvider instance.
      * @param resultRef the result reference.
      * @param resultPropertyValues the template.
      * @return such generated code.
      * @precondition provider != null
      * @precondition resultPropertyValues != null
      */
     protected String buildResultPropertyValues(
         final CustomSqlProvider provider,
         final ResultRefElement resultRef,
         final String resultPropertyValues)
     {
         String result = "";
 
         if  (resultRef != null)
         {
             ResultElement t_Result = provider.resolveReference(resultRef);
 
             if  (t_Result == null)
             {
                 LogFactory.getLog("custom-sql").warn(
                     "Referenced result not found:"
                     + resultRef.getId());
             }
             else
             {
                 result =
                     buildResultPropertyValues(
                         provider,
                         t_Result.getPropertyRefs(),
                         resultPropertyValues,
                         StringUtils.getInstance());
             }
         }
 
         return result;
     }
 
     /**
      * Builds the result property values with given input.
      * @param provider the CustomSqlProvider instance.
      * @param propertyRefs the property references.
      * @param resultPropertyValues the template.
      * @param stringUtils the StringUtils instance.
      * @return such generated code.
      * @precondition provider != null
      * @precondition resultPropertyValues != null
      * @precondition stringUtils != null
      */
     protected String buildResultPropertyValues(
         final CustomSqlProvider provider,
         final Collection propertyRefs,
         final String resultPropertyValues,
         final StringUtils stringUtils)
     {
         StringBuffer result = new StringBuffer();
 
         if  (propertyRefs != null)
         {
             MessageFormat t_ResultPropertyValuesFormatter =
                 new MessageFormat(resultPropertyValues);
 
             Iterator t_itPropertyRefs = propertyRefs.iterator();
 
             boolean t_bPreviousWasTheFirst = true;
 
             while  (t_itPropertyRefs.hasNext())
             {
                 PropertyRefElement t_PropertyRef =
                     (PropertyRefElement) t_itPropertyRefs.next();
 
                 if  (t_PropertyRef != null)
                 {
                     PropertyElement t_Property =
                         provider.resolveReference(t_PropertyRef);
 
                     if  (t_Property == null)
                     {
                         LogFactory.getLog("custom-sql").warn(
                               "Referenced property not found:"
                             + t_PropertyRef.getId());
                     }
                     else
                     {
                         result.append(
                             t_ResultPropertyValuesFormatter.format(
                                 new Object[]
                                 {
                                     stringUtils.capitalize(
                                         t_Property.getType(), '_'),
                                     (   (t_Property.getIndex() > 0)
                                      ?  ("" + t_Property.getIndex())
                                      :  "\"" + t_Property.getColumnName() + "\""),
                                     t_Property.getName()
                                 }));
 
                         if  (t_itPropertyRefs.hasNext())
                         {
                             result.append(",");
                         }
                     }
                 }
             }
         }
 
         return result.toString();
     }
 
     /**
      * Builds the complete custom select.
      * @param provider the CustomSqlProvider instance.
      * @param sqlElement the SqlElement instance.
      * @param customSelect the custom select template.
      * @param parameterJavadoc the generated parameter Javadoc.
      * @param parameterDeclaration the generated parameter declaration.
      * @param parameterValues the generared parameter values.
      * @param resultPropertyValues the generated result property values.
      * @param stringUtils the StringUtils isntance.
      * @return the generated code.
      * @precondition provider != null
      * @precondition sqlElement != null
      * @precondition customSelect != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterValues != null
      * @precondition resultPropertyValues != null
      * @precondition stringUtils != null
      */
     protected String buildCustomSelect(
         final CustomSqlProvider provider,
         final SqlElement sqlElement,
         final String customSelect,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterValues,
         final String resultPropertyValues,
         final StringUtils stringUtils)
     {
         String result = "";
 
         MessageFormat t_CustomSelectFormatter =
             new MessageFormat(customSelect);
 
         ResultElement t_Result = null;
 
         ResultRefElement t_ResultRef = sqlElement.getResultRef();
 
         if  (t_ResultRef != null)
         {
             t_Result = provider.resolveReference(t_ResultRef);
         }
 
         result =
             t_CustomSelectFormatter.format(
                 new Object[]
                 {
                     (   (t_Result != null)
                      ?  t_Result.getClassValue()
                      :  "-no-result-type-defined-"),
                     parameterJavadoc,
                     stringUtils.unCapitalizeStart(
                         stringUtils.capitalize(
                             sqlElement.getName(), '-')),
                     parameterDeclaration,
                     sqlElement.getValue(),
                     parameterValues,
                     (   (t_Result != null)
                      ?  stringUtils.extractPackageName(
                             t_Result.getClassValue())
                      :  "-no-result-type-defined-"),
                     resultPropertyValues
                 });
 
         return result;
     }
 
     /**
      * Builds the custom updates.
      * @param provider the CustomSqlProvider instance.
      * @return such generated code.
      * @precondition provider != null
      */
     protected String buildCustomUpdates(final CustomSqlProvider provider)
     {
         return
             buildCustomUpdates(
                 provider,
                 getCustomUpdate(),
                 getCustomUpdateParameterJavadoc(),
                 getCustomUpdateParameterDeclaration(),
                 getCustomUpdateParameterValues(),
                 getCustomUpdateQueryLine(),
                 StringUtils.getInstance(),
                 StringValidator.getInstance(),
                 RegexpManager.createHelper());
     }
 
     /**
      * Builds the custom updates.
      * @param customSqlProvider the CustomSqlProvider instance.
      * @param customUpdate the custom update.
      * @param parameterJavadoc the Javadoc template
      * of the parameters.
      * @param parameterDeclaration the parameter declaration.
      * @param parameterValues the parameter values.
      * @param queryLine the query line.
      * @param stringUtils the StringUtils instance.
      * @param stringValidator the StringValidator instance.
      * @param helper the Helper instance.
      * @return such generated code.
      * @precondition customSqlProvider != null
      * @precondition customUpdate != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterValues != null
      * @preoncition queryLine != null
      * @precondition stringUtils != null
      * @precondition stringValidator != null
      * @precondition helper != null
      */
     protected String buildCustomUpdates(
         final CustomSqlProvider customSqlProvider,
         final String customUpdate,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterValues,
         final String queryLine,
         final StringUtils stringUtils,
         final StringValidator stringValidator,
         final Helper helper)
     {
         StringBuffer result = new StringBuffer();
 
         Collection t_cContents = customSqlProvider.getCollection();
 
         if  (t_cContents != null)
         {
             Iterator t_itContentIterator = t_cContents.iterator();
 
             while  (t_itContentIterator.hasNext())
             {
                 Object t_Content = t_itContentIterator.next();
 
                 if  (t_Content instanceof SqlElement)
                 {
                     SqlElement t_SqlElement =
                         (SqlElement) t_Content;
 
                     if  (t_SqlElement.UPDATE.equals(t_SqlElement.getType()))
                     {
                         String[] t_astrParameterTemplates =
                             buildParameterTemplates(
                                 customSqlProvider,
                                 t_SqlElement.getParameterRefs(),
                                 parameterJavadoc,
                                 parameterDeclaration,
                                 parameterValues,
                                 stringUtils,
                                 stringValidator);
 
                         result.append(
                             buildCustomUpdate(
                                 customSqlProvider,
                                 t_SqlElement,
                                 customUpdate,
                                 t_astrParameterTemplates[0],
                                 t_astrParameterTemplates[1],
                                 t_astrParameterTemplates[2],
                                 queryLine,
                                 stringUtils,
                                 helper));
                     }
                 }
             }
         }
 
         return result.toString();
     }
 
     /**
      * Builds the complete custom update.
      * @param provider the CustomSqlProvider instance.
      * @param sqlElement the SqlElement instance.
      * @param customUpdate the custom update template.
      * @param parameterJavadoc the generated parameter Javadoc.
      * @param parameterDeclaration the generated parameter declaration.
      * @param parameterValues the generared parameter values.
      * @param queryLine the query line.
      * @param stringUtils the StringUtils isntance.
      * @param helper the Helper instance.
      * @return the generated code.
      * @precondition provider != null
      * @precondition sqlElement != null
      * @precondition customUpdate != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterValues != null
      * @precondition queryLine != null
      * @precondition stringUtils != null
      * @precondition helper != null
      */
     protected String buildCustomUpdate(
         final CustomSqlProvider provider,
         final SqlElement sqlElement,
         final String customUpdate,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterValues,
         final String queryLine,
         final StringUtils stringUtils,
         final Helper helper)
     {
         String result = "";
 
         MessageFormat t_CustomUpdateFormatter =
             new MessageFormat(customUpdate);
 
         ResultElement t_Result = null;
 
         ResultRefElement t_ResultRef = sqlElement.getResultRef();
 
         if  (t_ResultRef != null)
         {
             t_Result = provider.resolveReference(t_ResultRef);
         }
 
         result =
             t_CustomUpdateFormatter.format(
                 new Object[]
                 {
                     sqlElement.getName(),
                     parameterJavadoc,
                     stringUtils.unCapitalizeStart(
                         stringUtils.capitalize(
                             sqlElement.getName(), '-')),
                     parameterDeclaration,
                     stringUtils.applyToEachLine(
                         stringUtils.removeFirstAndLastBlankLines(
                             helper.replaceAll(
                                 sqlElement.getValue(),
                                 "\"",
                                 "\\\"")),
                         queryLine),
                     parameterValues,
                     (   (t_Result != null)
                      ?  stringUtils.extractPackageName(
                             t_Result.getClassValue())
                      :  "-no-result-type-defined-")
                 });
 
         return result;
     }
 }
