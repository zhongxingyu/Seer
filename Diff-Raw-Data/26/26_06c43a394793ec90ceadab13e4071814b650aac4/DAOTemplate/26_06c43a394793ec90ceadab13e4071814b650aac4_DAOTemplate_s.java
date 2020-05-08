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
 
  *****************************************************************************
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
 import org.acmsl.queryj.tools.templates.dao.AbstractDAOTemplate;
 import org.acmsl.queryj.tools.templates.dao.DAOTemplateDefaults;
 import org.acmsl.queryj.tools.templates.dao.DAOTemplateUtils;
 import org.acmsl.queryj.tools.templates.TableTemplate;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.commons.regexpplugin.Helper;
 import org.acmsl.commons.regexpplugin.RegexpEngine;
 import org.acmsl.commons.regexpplugin.RegexpEngineNotFoundException;
 import org.acmsl.commons.regexpplugin.RegexpManager;
 import org.acmsl.commons.regexpplugin.RegexpPluginMisconfiguredException;
 import org.acmsl.commons.utils.EnglishGrammarUtils;
 import org.acmsl.commons.utils.StringUtils;
 import org.acmsl.commons.utils.StringValidator;
 
 /*
  * Importing Ant classes.
  */
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 
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
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
 * @version $Revision$
  */
 public class DAOTemplate
     extends  AbstractDAOTemplate
     implements  DAOTemplateDefaults
 {
     /**
      * Builds a <code>DAOTemplate</code> using given information.
      * @param tableTemplate the table template.
      * @param metaDataManager the database metadata manager.
      * @param customSqlProvider the CustomSqlProvider instance.
      * @param packageName the package name.
      * @param engineName the engine name.
      * @param engineVersion the engine version.
      * @param quote the identifier quote string.
      * @param basePackageName the base package name.
      * @param repositoryName the repository name.
      * @param project the project, for logging purposes.
      * @param task the task, for logging purposes.
      */
     public DAOTemplate(
         final TableTemplate tableTemplate,
         final DatabaseMetaDataManager metaDataManager,
         final CustomSqlProvider customSqlProvider,
         final String packageName,
         final String engineName,
         final String engineVersion,
         final String quote,
         final String basePackageName,
         final String repositoryName,
         final Project project,
         final Task task)
     {
         super(
             tableTemplate,
             metaDataManager,
             customSqlProvider,
             DEFAULT_HEADER,
             DEFAULT_PACKAGE_DECLARATION,
             packageName,
             engineName,
             engineVersion,
             quote,
             basePackageName,
             repositoryName,
             DEFAULT_PROJECT_IMPORTS,
             DEFAULT_CUSTOM_RESULT_SET_EXTRACTOR_IMPORT,
             DEFAULT_FOREIGN_KEY_STATEMENT_SETTER_IMPORT,
             DEFAULT_FOREIGN_DAO_IMPORTS,
             DEFAULT_ACMSL_IMPORTS,
             DEFAULT_ADDITIONAL_IMPORTS,
             DEFAULT_JDK_IMPORTS,
             DEFAULT_JDK_EXTENSION_IMPORTS,
             DEFAULT_LOGGING_IMPORTS,
             DEFAULT_JAVADOC,
             DEFAULT_CLASS_DEFINITION,
             DEFAULT_CLASS_START,
             DEFAULT_CLASS_CONSTANTS,
             DEFAULT_CUSTOM_RESULT_SET_EXTRACTOR_CONSTANT,
             DEFAULT_CLASS_CONSTRUCTOR,
             DEFAULT_FIND_BY_STATIC_FIELD_METHOD,
             DEFAULT_FIND_BY_STATIC_FIELD_JAVADOC,
             DEFAULT_FIND_BY_STATIC_FIELD_DECLARATION,
             DEFAULT_PK_JAVADOC,
             DEFAULT_ATTRIBUTE_JAVADOC,
             DEFAULT_ATTRIBUTE_DECLARATION,
             DEFAULT_ATTRIBUTE_FILTER,
             DEFAULT_STATEMENT_SETTER_CALL,
             DEFAULT_FIND_BY_PRIMARY_KEY_METHOD,
             DEFAULT_INSERT_METHOD,
             DEFAULT_INSERT_PARAMETERS_SPECIFICATION,
             DEFAULT_EXTERNALLY_MANAGED_INSERT_PARAMETERS_SPECIFICATION,
             DEFAULT_CREATE_METHOD,
             DEFAULT_ATTRIBUTES_PASSING_SUBTEMPLATE,
             DEFAULT_ATTRIBUTES_SPECIFICATION_SUBTEMPLATE,
             DEFAULT_CREATE_EXTERNALLY_MANAGED_FIELD_VALUE_RETRIEVAL,
             DEFAULT_UPDATE_METHOD,
             DEFAULT_UPDATE_PARAMETERS_SPECIFICATION,
             DEFAULT_DELETE_METHOD_NO_FK,
             DEFAULT_DELETE_METHOD_FK,
             DEFAULT_FOREIGN_DAO_DELETE_CALL,
             DEFAULT_FOREIGN_DAO_UPDATE_CALL,
             DEFAULT_DELETE_BY_FK_METHOD,
             DEFAULT_DELETE_FK_JAVADOC,
             DEFAULT_DELETE_FK_DECLARATION,
             DEFAULT_CUSTOM_SELECT,
             DEFAULT_CUSTOM_SELECT_PARAMETER_JAVADOC,
             DEFAULT_CUSTOM_SELECT_PARAMETER_DECLARATION,
             DEFAULT_CUSTOM_SELECT_PARAMETER_TYPE_SPECIFICATION,
             DEFAULT_CUSTOM_SELECT_PARAMETER_VALUES,
             DEFAULT_CUSTOM_SELECT_RESULT_PROPERTIES,
             DEFAULT_CUSTOM_UPDATE_OR_INSERT,
             DEFAULT_CUSTOM_UPDATE_OR_INSERT_PARAMETER_JAVADOC,
             DEFAULT_CUSTOM_UPDATE_OR_INSERT_PARAMETER_DECLARATION,
             DEFAULT_CUSTOM_UPDATE_OR_INSERT_PARAMETER_TYPE_SPECIFICATION,
             DEFAULT_CUSTOM_UPDATE_OR_INSERT_PARAMETER_VALUES,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_WITH_NO_RETURN,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_WITH_RETURN,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_WITH_SINGLE_RETURN,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_WITH_MULTIPLE_RETURN,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_PARAMETER_JAVADOC,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_RETURN_JAVADOC,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_PARAMETER_DECLARATION,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_PARAMETER_SPECIFICATION,
             DEFAULT_CUSTOM_SELECT_FOR_UPDATE_CONDITIONAL_RETURN,
             DEFAULT_PK_RESULTSET_EXTRACTOR,
             DEFAULT_PK_EXTRACTOR_SIMPLE_PARAMETER_RETRIEVAL,
             DEFAULT_PK_EXTRACTOR_PARAMETER_RETRIEVAL,
             DEFAULT_CLASS_END,
             project,
             task);
     }
 
     /**
      * Retrieves the source code generated by this template.
      * @return such code.
      */
     protected String generateOutput()
     {
         return
             generateOutput(
                 getTableTemplate(),
                 getMetaDataManager(),
                 getCustomSqlProvider(),
                 getHeader(),
                 getPackageDeclaration(),
                 getPackageName(),
                 getEngineName(),
                 getEngineVersion(),
                 getQuote(),
                 getBasePackageName(),
                 getRepositoryName(),
                 getProjectImports(),
                 getCustomResultSetExtractorImport(),
                 getForeignKeyStatementSetterImport(),
                 getForeignDAOImports(),
                 getAdditionalImports(),
                 getAcmslImports(),
                 getJdkImports(),
                 getJdkExtensionImports(),
                 getLoggingImports(),
                 getJavadoc(),
                 getClassDefinition(),
                 getClassStart(),
                 getClassConstants(),
                 getCustomResultSetExtractorConstant(),
                 getClassConstructor(),
                 getFindByStaticFieldMethod(),
                 getFindByStaticFieldJavadoc(),
                 getFindByStaticFieldDeclaration(),
                 getPkJavadoc(),
                 getAttributeJavadoc(),
                 getAttributeDeclaration(),
                 getAttributeFilter(),
                 getStatementSetterCall(),
                 getFindByPrimaryKeyMethod(),
                 getInsertMethod(),
                 getInsertParametersSpecification(),
                 getExternallyManagedInsertParametersSpecification(),
                 getCreateMethod(),
                 getAttributesPassing(),
                 getAttributesSpecification(),
                 getCreateExternallyManagedFieldValueRetrieval(),
                 getUpdateMethod(),
                 getUpdateParametersSpecification(),
                 getDeleteMethodNoFk(),
                 getDeleteMethodFk(),
                 getForeignDAODeleteCall(),
                 getForeignDAOUpdateCall(),
                 getDeleteByFkMethod(),
                 getDeleteFkJavadoc(),
                 getDeleteFkDeclaration(),
                 getCustomSelect(),
                 getCustomSelectParameterJavadoc(),
                 getCustomSelectParameterDeclaration(),
                 getCustomSelectParameterTypeSpecification(),
                 getCustomSelectParameterValues(),
                 getCustomSelectResultPropertyValues(),
                 getCustomUpdateOrInsert(),
                 getCustomUpdateOrInsertParameterJavadoc(),
                 getCustomUpdateOrInsertParameterDeclaration(),
                 getCustomUpdateOrInsertParameterTypeSpecification(),
                 getCustomUpdateOrInsertParameterValues(),
                 getCustomSelectForUpdate(),
                 getCustomSelectForUpdateWithNoReturn(),
                 getCustomSelectForUpdateWithReturn(),
                 getCustomSelectForUpdateWithSingleReturn(),
                 getCustomSelectForUpdateWithMultipleReturn(),
                 getCustomSelectForUpdateParameterJavadoc(),
                 getCustomSelectForUpdateReturnJavadoc(),
                 getCustomSelectForUpdateParameterDeclaration(),
                 getCustomSelectForUpdateParameterSpecification(),
                 getCustomSelectForUpdateConditionalReturn(),
                 getPkResultSetExtractor(),
                 getPkExtractorSimpleParameterRetrieval(),
                 getPkExtractorParameterRetrieval(),
                 getClassEnd(),
                 MetaDataUtils.getInstance(),
                 StringUtils.getInstance(),
                 PackageUtils.getInstance(),
                 StringValidator.getInstance(),
                 EnglishGrammarUtils.getInstance(),
                 DAOTemplateUtils.getInstance());
     }
 
     /**
      * Retrieves the source code generated by this template.
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
      * @param customResultSetExtractorImport the custom result set extractor
      * import subtemplate.
      * @param foreignKeyStatementSetterImport the foreign key
      * statement setter import subtemplate.
      * @param foreignDAOImports the foreign DAO imports.
      * @param acmslImports the ACM-SL imports.
      * @param additionalImports the additional imports.
      * @param jdkImports the JDK imports.
      * @param jdkExtensionImports the JDK extension imports.
      * @param loggingImports the commons-logging imports.
      * @param javadoc the class Javadoc.
      * @param classDefinition the class definition.
      * @param classStart the class start.
      * @param classConstants the class' constants.
      * @param customResultSetExtractorConstant the custom result set extractor
      * constant subtemplate.
      * @param classConstructor the class constructor.
      * @param findByStaticFieldMethod the find-by-static-field method.
      * @param findByStaticFieldJavadoc the field javadoc for
      * find-by-static-field method.
      * @param findByStaticFieldDeclaration the field declaration for
      * find-by-static-field method.
      * @param pkJavadoc the pk javadoc subtemplate.
      * @param attributeJavadoc the attribute javadoc subtemplate.
      * @param attributeDeclaration the attribute declaration subtemplate.
      * @param attributeFilter the attribute filter subtemplate.
      * @param statementSetterCall the statement setter call subtemplate.
      * @param findByPrimaryKeyMethod the find by primary key method.
      * @param insertMethod the insert method.
      * @param insertParametersSpecification the specification of the insert
      * method's parameters.
      * @param externallyManagedInsertParametersSpecification the specification
      * of the insert method's parameters managed externally.
      * @param createMethod the create method.
      * @param attributesPassing the attributes passing subtemplate.
      * @param attributesSpecification the attributes specification subtemplate.
      * @param createExternallyManagedFieldValueRetrieval the externally-managed
      * field value retrieval in create method.
      * @param updateMethod the update method.
      * @param updateParametersSpecification the specification of the update
      * method's parameters.
      * @param deleteMethodNoFk the delete method with no fk.
      * @param deleteMethodFk the delete method with fk.
      * @param foreignDAODeleteCall the delete call to foreign DAOs.
      * @param foreignDAOUpdateCall the update call to foreign DAOs.
      * @param deleteByFkMethod the delete by fk method.
      * @param deleteFkJavadoc the delete FK javadoc.
      * @param deleteFkDeclaration the delete FK declaration.
      * @param customSelect the custom select template.
      * @param customSelectParameterJavadoc the Javadoc for the parameters of
      * the custom selects.
      * @param customSelectParameterDeclaration the parameter declaration of the
      * custom selects.
      * @param customSelectParameterTypeSpecification the parameter type
      * specification subtemplate of the custom selects.
      * @param customSelectParameterValues the parameter values of the custom
      * selects.
      * @param customSelectResultPropertyValues the properties of the result
      * set for custom selects.
      * @param customUpdateOrInsert the custom update template.
      * @param customUpdateOrInsertParameterJavadoc the Javadoc for the
      * parameters of the custom updates or inserts.
      * @param customUpdateOrInsertParameterDeclaration the parameter
      * declaration of the custom updates or inserts.
      * @param customUpdateOrInsertParameterTypeSpecification the parameter type
      * specification subtemplate of the custom updates or inserts.
      * @param customUpdateOrInsertParameterValues the parameter values of
      * the custom updates or inserts.
      * @param customSelectForUpdate the custom-select-for-update template.
      * @param customSelectForUpdateWithNoReturn the
      * custom-select-for-update-with-no-return subtemplate.
      * @param customSelectForUpdateWithReturn the
      * custom-select-for-update-with-return subtemplate.
      * @param customSelectForUpdateWithSingleReturn the
      * custom-select-for-update-with-single-return subtemplate.
      * @param customSelectForUpdateWithMultipleReturn the
      * custom-select-for-update-with-multiple-return subtemplate.
      * @param customSelectForUpdateParameterJavadoc the Javadoc for the
      * parameters of the custom-select-for-update operations.
      * @param customSelectForUpdateReturnJavadoc the Javadoc for the
      * return of the custom-select-for-update operations.
      * @param customSelectForUpdateParameterDeclaration the parameter
      * declaration of the custom-select-for-update operations.
      * @param customSelectForUpdateParameterSpecification the parameter
      * specification of the custom-select-for-update operations.
      * @param customSelectForUpdateConditionalReturn the subtemplate
      * to conditionally provide return statement in select-for-update operations.
      * @param pkResultSetExtractor the PK ResultSet extractor.
      * @param pkExtractorSimpleParameterRetrieval the PK extractor parameter
      * retrieval subtemplate.
      * @param pkExtractorParameterRetrieval the PK extractor parameter
      * retrieval subtemplate.
      * @param classEnd the class end.
      * @param metaDataUtils the MetaDataUtils instance.
      * @param stringUtils the StringUtils instance.
      * @param packageUtils the PackageUtils instance.
      * @param stringValidator the StringValidator instance.
      * @param englishGrammarUtils the EnglishGrammarUtils instance.
      * @param daoTemplateUtils the DAOTemplateUtils instance.
      * @return such code.
      * @precondition tableTemplate != null
      * @precondition metaDataManager != null
      * @precondition metaDataUtils != null
      * @precondition stringUtils != null
      * @precondition packageUtils != null
      * @precondition stringValidator != null
      * @precondition englishGrammarUtils != null
      * @precondition daoTemplateUtils != null
      */
     protected String generateOutput(
         final TableTemplate tableTemplate,
         final DatabaseMetaDataManager metaDataManager,
         final CustomSqlProvider customSqlProvider,
         final String header,
         final String packageDeclaration,
         final String packageName,
         final String engineName,
         final String engineVersion,
         final String quote,
         final String basePackageName,
         final String repositoryName,
         final String projectImports,
         final String customResultSetExtractorImport,
         final String foreignKeyStatementSetterImport,
         final String foreignDAOImports,
         final String additionalImports,
         final String acmslImports,
         final String jdkImports,
         final String jdkExtensionImports,
         final String loggingImports,
         final String javadoc,
         final String classDefinition,
         final String classStart,
         final String classConstants,
         final String customResultSetExtractorConstant,
         final String classConstructor,
         final String findByStaticFieldMethod,
         final String findByStaticFieldJavadoc,
         final String findByStaticFieldDeclaration,
         final String pkJavadoc,
         final String attributeJavadoc,
         final String attributeDeclaration,
         final String attributeFilter,
         final String statementSetterCall,
         final String findByPrimaryKeyMethod,
         final String insertMethod,
         final String insertParametersSpecification,
         final String externallyManagedInsertParametersSpecification,
         final String createMethod,
         final String attributesPassing,
         final String attributesSpecification,
         final String createExternallyManagedFieldValueRetrieval,
         final String updateMethod,
         final String updateParametersSpecification,
         final String deleteMethodNoFk,
         final String deleteMethodFk,
         final String foreignDAODeleteCall,
         final String foreignDAOUpdateCall,
         final String deleteByFkMethod,
         final String deleteFkJavadoc,
         final String deleteFkDeclaration,
         final String customSelect,
         final String customSelectParameterJavadoc,
         final String customSelectParameterDeclaration,
         final String customSelectParameterTypeSpecification,
         final String customSelectParameterValues,
         final String customSelectResultPropertyValues,
         final String customUpdateOrInsert,
         final String customUpdateOrInsertParameterJavadoc,
         final String customUpdateOrInsertParameterDeclaration,
         final String customUpdateOrInsertParameterTypeSpecification,
         final String customUpdateOrInsertParameterValues,
         final String customSelectForUpdate,
         final String customSelectForUpdateWithNoReturn,
         final String customSelectForUpdateWithReturn,
         final String customSelectForUpdateWithSingleReturn,
         final String customSelectForUpdateWithMultipleReturn,
         final String customSelectForUpdateParameterJavadoc,
         final String customSelectForUpdateReturnJavadoc,
         final String customSelectForUpdateParameterDeclaration,
         final String customSelectForUpdateParameterSpecification,
         final String customSelectForUpdateConditionalReturn,
         final String pkResultSetExtractor,
         final String pkExtractorSimpleParameterRetrieval,
         final String pkExtractorParameterRetrieval,
         final String classEnd,
         final MetaDataUtils metaDataUtils,
         final StringUtils stringUtils,
         final PackageUtils packageUtils,
         final StringValidator stringValidator,
         final EnglishGrammarUtils englishGrammarUtils,
         final DAOTemplateUtils daoTemplateUtils)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         String t_strRepositoryName =
             stringUtils.capitalize(
                 repositoryName,
                 '_');
 
         String t_strValueObjectName =
             englishGrammarUtils.getSingular(
                 tableTemplate.getTableName().toLowerCase());
 
         String t_strCapitalizedValueObjectName =
             stringUtils.capitalize(t_strValueObjectName, '_');
 
         MessageFormat t_HeaderFormatter = new MessageFormat(header);
 
         MessageFormat t_PackageDeclarationFormatter =
             new MessageFormat(packageDeclaration);
 
         MessageFormat t_ProjectImportFormatter =
             new MessageFormat(projectImports);
 
         MessageFormat t_ForeignKeyStatementSetterImportFormatter =
             new MessageFormat(foreignKeyStatementSetterImport);
 
         MessageFormat t_ForeignDAOImportsFormatter =
             new MessageFormat(foreignDAOImports);
 
         MessageFormat t_JavadocFormatter = new MessageFormat(javadoc);
 
         MessageFormat t_ClassDefinitionFormatter =
             new MessageFormat(classDefinition);
 
         MessageFormat t_ClassConstantsFormatter =
             new MessageFormat(classConstants);
 
         MessageFormat t_ClassConstructorFormatter =
             new MessageFormat(classConstructor);
 
         MessageFormat t_PkJavadocFormatter =
             new MessageFormat(pkJavadoc);
 
         MessageFormat t_AttributeJavadocFormatter =
             new MessageFormat(attributeJavadoc);
 
         MessageFormat t_AttributeDeclarationFormatter =
             new MessageFormat(attributeDeclaration);
 
         MessageFormat t_AttributeFilterFormatter =
             new MessageFormat(attributeFilter);
 
         MessageFormat t_StatementSetterCallFormatter =
             new MessageFormat(statementSetterCall);
 
         MessageFormat t_FindByPrimaryKeyFormatter =
             new MessageFormat(findByPrimaryKeyMethod);
 
         MessageFormat t_InsertMethodFormatter =
             new MessageFormat(insertMethod);
 
         MessageFormat t_InsertParametersSpecificationFormatter =
             new MessageFormat(insertParametersSpecification);
 
         MessageFormat t_ExternallyManagedInsertParametersSpecificationFormatter =
             new MessageFormat(externallyManagedInsertParametersSpecification);
 
         MessageFormat t_CreateMethodFormatter =
             new MessageFormat(createMethod);
 
         MessageFormat t_AttributesPassingFormatter =
             new MessageFormat(attributesPassing);
 
         MessageFormat t_AttributesSpecificationFormatter =
             new MessageFormat(attributesSpecification);
 
         MessageFormat t_CreateExternallyManagedFieldValueRetrievalFormatter =
             new MessageFormat(createExternallyManagedFieldValueRetrieval);
 
         MessageFormat t_UpdateMethodFormatter =
             new MessageFormat(updateMethod);
 
         MessageFormat t_UpdateParametersSpecificationFormatter =
             new MessageFormat(updateParametersSpecification);
 
         MessageFormat t_DeleteMethodFormatter =
             new MessageFormat(deleteMethodNoFk);
 
         MessageFormat t_ForeignDAODeleteCallFormatter = null;
         MessageFormat t_ForeignDAOUpdateCallFormatter = null;
 
         MessageFormat t_DeleteByFkMethodFormatter =
             new MessageFormat(deleteByFkMethod);
 
         MessageFormat t_FkJavadocFormatter =
             new MessageFormat(deleteFkJavadoc);
         MessageFormat t_FkDeclarationFormatter =
             new MessageFormat(deleteFkDeclaration);
 
         MessageFormat t_PkResultSetExtractorFormatter =
             new MessageFormat(pkResultSetExtractor);
 
         MessageFormat t_PkExtractorSimpleParameterRetrievalFormatter =
             new MessageFormat(pkExtractorSimpleParameterRetrieval);
 
         MessageFormat t_PkExtractorParameterRetrievalFormatter =
             new MessageFormat(pkExtractorParameterRetrieval);
 
         StringBuffer t_sbForeignKeyStatementSetterImports =
             new StringBuffer();
         StringBuffer t_sbForeignDAOImports = new StringBuffer();
         StringBuffer t_sbAttributesJavadoc = new StringBuffer();
         StringBuffer t_sbUpdateAttributesJavadoc = new StringBuffer();
         StringBuffer t_sbPkJavadoc = new StringBuffer();
         StringBuffer t_sbNonPkJavadoc = new StringBuffer();
         StringBuffer t_sbAttributesDeclaration = new StringBuffer();
         StringBuffer t_sbUpdateAttributesDeclaration = new StringBuffer();
         StringBuffer t_sbPkDeclaration = new StringBuffer();
         StringBuffer t_sbNonPkDeclaration = new StringBuffer();
         StringBuffer t_sbPkFilter = new StringBuffer();
         StringBuffer t_sbNonPkFilter = new StringBuffer();
         StringBuffer t_sbFkFilter = new StringBuffer();
         StringBuffer t_sbPkStatementSetterCall = new StringBuffer();
         String t_strFkStatementSetterCall = "";
         StringBuffer t_sbInsertAttributesStatementSetterCall = new StringBuffer();
         StringBuffer t_sbUpdateAttributesStatementSetterCall = new StringBuffer();
         StringBuffer t_sbCreateMethod = new StringBuffer();
         StringBuffer t_sbCreateAttributesStatementSetterCall = new StringBuffer();
         StringBuffer t_sbRetrievalQueries = new StringBuffer();
         StringBuffer t_sbAttributesPassing = new StringBuffer();
         StringBuffer t_sbAttributesSpecification = new StringBuffer();
         StringBuffer t_sbCreateExternallyManagedFieldValueRetrieval =
             new StringBuffer();
         StringBuffer t_sbDeleteMethod = new StringBuffer();
         StringBuffer t_sbForeignDAODeleteCall = new StringBuffer();
         StringBuffer t_sbForeignDAOUpdateCall = new StringBuffer();
         StringBuffer t_sbDeleteByFkMethod = new StringBuffer();
         StringBuffer t_sbPkResultSetExtractor = new StringBuffer();
         StringBuffer t_sbPkExtractorSimpleParameterRetrieval = new StringBuffer();
         StringBuffer t_sbPkExtractorParameterRetrieval = new StringBuffer();
 
         StringBuffer t_sbInsertParametersSpecification = new StringBuffer();
         StringBuffer t_sbExternallyManagedInsertParametersSpecification =
             new StringBuffer();
         StringBuffer t_sbUpdateParametersSpecification = new StringBuffer();
 
         boolean t_bForeignKeys = false;
 
         String[] t_astrPrimaryKeys =
             metaDataManager.getPrimaryKeys(tableTemplate.getTableName());
 
         if  (t_astrPrimaryKeys != null)
         {
             for  (int t_iPkIndex = 0;
                       t_iPkIndex < t_astrPrimaryKeys.length;
                       t_iPkIndex++)
             {
                 t_sbPkFilter.append(
                     t_AttributeFilterFormatter.format(
                         new Object[]
                         {
                             t_strRepositoryName,
                             tableTemplate.getTableName().toUpperCase(),
                             t_astrPrimaryKeys[t_iPkIndex].toUpperCase(),
                             t_astrPrimaryKeys[t_iPkIndex].toLowerCase()
                         }));
 
                 t_sbPkJavadoc.append(
                     t_PkJavadocFormatter.format(
                         new Object[]
                         {
                             t_astrPrimaryKeys[t_iPkIndex].toLowerCase(),
                             t_astrPrimaryKeys[t_iPkIndex]
                         }));
 
                 if  (t_iPkIndex > 0)
                 {
                     t_sbPkDeclaration.append(",");
                 }
 
                 int t_iType =
                     metaDataManager.getColumnType(
                         tableTemplate.getTableName(),
                         t_astrPrimaryKeys[t_iPkIndex]);
 
                 String t_strNativeType =
                     metaDataUtils.getNativeType(t_iType);
 
                 t_sbPkDeclaration.append(
                     t_AttributeDeclarationFormatter.format(
                         new Object[]
                         {
                             metaDataUtils.getNativeType(
                                 metaDataManager.getColumnType(
                                     tableTemplate.getTableName(),
                                     t_astrPrimaryKeys[t_iPkIndex])),
                             t_astrPrimaryKeys[t_iPkIndex].toLowerCase()
                         }));
 
                 if  (t_iPkIndex > 0)
                 {
                     t_sbPkStatementSetterCall.append(",");
                 }
 
                 t_sbPkStatementSetterCall.append(
                     t_StatementSetterCallFormatter.format(
                         new Object[]
                         {
                             t_astrPrimaryKeys[t_iPkIndex].toLowerCase()
                         }));
 
                 if  (metaDataManager.isManagedExternally(
                          tableTemplate.getTableName(),
                          t_astrPrimaryKeys[t_iPkIndex]))
                 {
                     MessageFormat t_PkExtractorFormatter =
                         t_PkExtractorParameterRetrievalFormatter;
 
                     String t_strFieldType =
                         metaDataUtils.getFieldType(t_iType, false, null, null);
 
                     if  (t_strFieldType.equals(t_strNativeType))
                     {
                         t_PkExtractorFormatter =
                             t_PkExtractorSimpleParameterRetrievalFormatter;
                     }
 
                     t_strFieldType =
                         metaDataUtils.getFieldType(t_iType, true, null, null);
 
                     t_sbPkExtractorParameterRetrieval.append(
                         t_PkExtractorFormatter.format(
                             new Object[]
                             {
                                 t_astrPrimaryKeys[t_iPkIndex].toLowerCase(),
                                 metaDataUtils.getGetterMethod(t_iType),
                                 t_strFieldType
                             }));
 
                     t_sbCreateExternallyManagedFieldValueRetrieval.append(
                         t_CreateExternallyManagedFieldValueRetrievalFormatter.format(
                             new Object[]
                             {
                                 t_strNativeType,
                                 t_astrPrimaryKeys[t_iPkIndex].toLowerCase(),
                                 t_strFieldType
                             }));
                             
                 }
             }
         }
 
         String[] t_astrReferingTables =
             metaDataManager.getReferingTables(
                 tableTemplate.getTableName());
 
         if  (   (t_astrReferingTables != null)
              && (t_astrReferingTables.length > 0))
         {
             t_DeleteMethodFormatter = new MessageFormat(deleteMethodFk);
 
             t_ForeignDAODeleteCallFormatter =
                 new MessageFormat(foreignDAODeleteCall);
 
             t_ForeignDAOUpdateCallFormatter =
                 new MessageFormat(foreignDAOUpdateCall);
 
             String t_strReferingColumn = null;
 
             StringBuffer t_sbCall = null;
 
             MessageFormat t_CallFormatter = null;
 
             for  (int t_iRefTableIndex = 0;
                       t_iRefTableIndex < t_astrReferingTables.length;
                       t_iRefTableIndex++)
             {
                 t_bForeignKeys = true;
 
                 t_strReferingColumn =
                     metaDataManager.getForeignKey(
                         t_astrReferingTables[t_iRefTableIndex],
                         tableTemplate.getTableName());
 
                 if  (metaDataManager.allowsNull(
                          t_astrReferingTables[t_iRefTableIndex],
                          t_strReferingColumn))
                 {
                     t_sbCall = t_sbForeignDAOUpdateCall;
                     t_CallFormatter = t_ForeignDAOUpdateCallFormatter;
                 }
                 else
                 {
                     t_sbCall = t_sbForeignDAODeleteCall;
                     t_CallFormatter = t_ForeignDAODeleteCallFormatter;
                 }
 
                 String t_strReferingTableName =
                     stringUtils.capitalize(
                         englishGrammarUtils.getSingular(
                             t_astrReferingTables[t_iRefTableIndex]
                                 .toLowerCase()),
                         '_');
 
                 t_sbForeignDAOImports.append(
                     t_ForeignDAOImportsFormatter.format(
                         new Object[]
                         {
                             packageUtils.retrieveBaseDAOPackage(
                                 basePackageName),
                             t_strReferingTableName
                                 
                         }));
 
                 t_sbCall.append(
                     t_CallFormatter.format(
                         new Object[]
                         {
                             t_strReferingTableName,
                             t_strCapitalizedValueObjectName,
                             t_sbPkStatementSetterCall
                         }));
 
             }
         }
 
         String t_strReferredColumn = null;
 
         String[] t_astrReferredTables =
             metaDataManager.getReferredTables(
                 tableTemplate.getTableName());
 
         if  (   (t_astrReferredTables != null)
              && (t_astrReferredTables.length > 0))
         {
             for  (int t_iRefTableIndex = 0;
                       t_iRefTableIndex < t_astrReferredTables.length;
                       t_iRefTableIndex++)
             {
                 t_strReferredColumn =
                     metaDataManager.getForeignKey(
                         tableTemplate.getTableName(),
                         t_astrReferredTables[t_iRefTableIndex]);
 
                 String t_strReferredTableName =
                     stringUtils.capitalize(
                         englishGrammarUtils.getSingular(
                             t_astrReferredTables[t_iRefTableIndex]
                                 .toLowerCase()),
                         '_');
 
                 String t_strFkJavadoc =
                     t_FkJavadocFormatter.format(
                         new Object[]
                         {
                             t_strReferredColumn.toLowerCase(),
                             t_strReferredColumn
                         });
 
                 String t_strFkDeclaration =
                     t_FkDeclarationFormatter.format(
                         new Object[]
                         {
                             metaDataUtils.getNativeType(
                                 metaDataManager.getColumnType(
                                     tableTemplate.getTableName(),
                                     t_strReferredColumn)),
                             t_strReferredColumn.toLowerCase()
                         });
 
                 t_sbFkFilter.append(
                     t_AttributeFilterFormatter.format(
                         new Object[]
                         {
                             t_strRepositoryName,
                             tableTemplate.getTableName().toUpperCase(),
                             t_strReferredColumn.toUpperCase()
                         }));
 
                 t_strFkStatementSetterCall =
                     t_StatementSetterCallFormatter.format(
                         new Object[]
                         {
                             t_strReferredColumn.toLowerCase()
                         });
 
                 t_sbForeignKeyStatementSetterImports.append(
                     t_ForeignKeyStatementSetterImportFormatter.format(
                         new Object[]
                         {
                             packageUtils.retrieveFkStatementSetterPackage(
                                 basePackageName,
                                 engineName,
                                 tableTemplate.getTableName()),
                             stringUtils.capitalize(
                                 englishGrammarUtils.getSingular(
                                     tableTemplate.getTableName().toLowerCase()),
                                 '_'),
                             t_strReferredTableName
                         }));
 
                 t_sbDeleteByFkMethod.append(
                     t_DeleteByFkMethodFormatter.format(
                         new Object[]
                         {
                             stringUtils.capitalize(
                                 englishGrammarUtils.getSingular(
                                     tableTemplate.getTableName().toLowerCase()),
                                 '_'),
                             t_strFkJavadoc,
                             t_strReferredTableName,
                             t_strFkDeclaration,
                             t_sbFkFilter,
                             t_strFkStatementSetterCall,
                             t_strRepositoryName,
                             tableTemplate.getTableName().toUpperCase(),
                         }));
 
             }
         }
 
         t_sbResult.append(
             t_HeaderFormatter.format(
                 new Object[]
                 {
                     engineName,
                     engineVersion,
                     t_strValueObjectName
                 }));
 
         t_sbResult.append(
             t_PackageDeclarationFormatter.format(
                 new Object[]{packageName}));
 
         String t_strJdbcOperationsPackage =
             packageUtils.retrieveJdbcOperationsPackage(
                 basePackageName,
                 engineName,
                 tableTemplate.getTableName().toLowerCase());
 
         t_sbResult.append(
             t_ProjectImportFormatter.format(
                 new Object[]
                 {
                     t_strJdbcOperationsPackage,
                     t_strCapitalizedValueObjectName,
                     buildCustomResultSetExtractorImports(
                         tableTemplate.getTableName(),
                         t_strJdbcOperationsPackage,
                         customResultSetExtractorImport,
                         customSqlProvider,
                         daoTemplateUtils,
                         stringUtils),
                     packageUtils.retrieveValueObjectPackage(
                         basePackageName),
                     packageUtils.retrieveBaseDAOPackage(
                         basePackageName),
                     packageUtils.retrieveRdbPackage(
                         basePackageName),
                     packageUtils.retrieveTableRepositoryPackage(
                         basePackageName),
                     t_strRepositoryName,
                     packageUtils.retrieveDataAccessManagerPackage(
                         basePackageName),
                     t_sbForeignKeyStatementSetterImports,
                     t_sbForeignDAOImports
                 }));
 
         t_sbResult.append(acmslImports);
         t_sbResult.append(additionalImports);
         t_sbResult.append(jdkImports);
         t_sbResult.append(jdkExtensionImports);
         t_sbResult.append(loggingImports);
 
         t_sbResult.append(
             t_JavadocFormatter.format(
                 new Object[]
                 {
                     engineName,
                     engineVersion,
                     t_strValueObjectName
                 }));
 
         t_sbResult.append(
             t_ClassDefinitionFormatter.format(
                 new Object[]
                 {
                     engineName,
                     t_strCapitalizedValueObjectName
                 }));
 
         t_sbResult.append(classStart);
 
         t_sbResult.append(
             t_ClassConstantsFormatter.format(
                 new Object[]
                 {
                     t_strValueObjectName,
                     t_strValueObjectName.toUpperCase(),
                     t_strCapitalizedValueObjectName,
                     buildCustomResultSetExtractorConstants(
                         tableTemplate.getTableName(),
                         customResultSetExtractorConstant,
                         customSqlProvider,
                         daoTemplateUtils,
                         stringUtils),
                 }));
 
         t_sbResult.append(
             t_ClassConstructorFormatter.format(
                 new Object[]
                 {
                     engineName,
                     t_strCapitalizedValueObjectName
                 }));
 
         String[] t_astrColumnNames =
             metaDataManager.getColumnNames(tableTemplate.getTableName());
 
         String t_strTableComment =
             metaDataManager.getTableComment(
                 tableTemplate.getTableName());
 
         if  (t_strTableComment != null)
         {
             int t_iKeyIndex = t_strTableComment.lastIndexOf("@static");
 
             if  (t_iKeyIndex >= 0)
             {
                 MessageFormat t_FindByStaticFieldMethodFormatter =
                     new MessageFormat(findByStaticFieldMethod);
 
                 MessageFormat t_FindByStaticFieldJavadocFormatter =
                     new MessageFormat(findByStaticFieldJavadoc);
 
                 MessageFormat t_FindByStaticFieldDeclarationFormatter =
                     new MessageFormat(findByStaticFieldDeclaration);
 
                 String t_strDescriptionColumn =
                     t_strTableComment.substring(
                         t_iKeyIndex + "@static".length()).trim();
 
                 String t_strFindByStaticFieldJavadoc =
                     t_FindByStaticFieldJavadocFormatter.format(
                         new Object[]
                         {
                             t_strDescriptionColumn.toLowerCase(),
                             t_strDescriptionColumn
                         });
 
                 String t_strFindByStaticFieldDeclaration =
                     t_FindByStaticFieldDeclarationFormatter.format(
                         new Object[]
                         {
                             metaDataUtils.getNativeType(
                                 metaDataManager.getColumnType(
                                     tableTemplate.getTableName(),
                                     t_strDescriptionColumn)),
                             t_strDescriptionColumn.toLowerCase()
                         });
 
                 t_sbResult.append(
                     t_FindByStaticFieldMethodFormatter.format(
                         new Object[]
                         {
                             tableTemplate.getTableName(),
                             t_strDescriptionColumn,
                             t_strFindByStaticFieldJavadoc,
                             t_strCapitalizedValueObjectName,
                             stringUtils.capitalize(
                                 t_strDescriptionColumn, '_'),
                             t_strFindByStaticFieldDeclaration
                         }));
             }
         }
         if  (t_astrColumnNames != null)
         {
             for  (int t_iColumnIndex = 0;
                       t_iColumnIndex < t_astrColumnNames.length;
                       t_iColumnIndex++)
             {
                 int t_iColumnType =
                     metaDataManager.getColumnType(
                         tableTemplate.getTableName(),
                         t_astrColumnNames[t_iColumnIndex]);
 
                 String t_strType =
                     metaDataUtils.getNativeType(t_iColumnType);
 
                 boolean t_bAllowsNull =
                     metaDataManager.allowsNull(
                         tableTemplate.getTableName(),
                         t_astrColumnNames[t_iColumnIndex]);
 
                 if  (t_bAllowsNull)
                 {
                     t_strType = metaDataUtils.getObjectType(t_iColumnType);
                 }
 
                 t_sbUpdateAttributesJavadoc.append(
                     t_AttributeJavadocFormatter.format(
                         new Object[]
                         {
                             t_astrColumnNames[t_iColumnIndex].toLowerCase(),
                             t_astrColumnNames[t_iColumnIndex]
                         }));
 
                 t_sbUpdateAttributesDeclaration.append(
                     t_AttributeDeclarationFormatter.format(
                         new Object[]
                         {
                             t_strType,
                             t_astrColumnNames[t_iColumnIndex].toLowerCase()
                         }));
 
                 t_sbCreateAttributesStatementSetterCall.append(
                     t_StatementSetterCallFormatter.format(
                         new Object[]
                         {
                             t_astrColumnNames[t_iColumnIndex].toLowerCase()
                         }));
 
                 t_sbAttributesSpecification.append(
                     t_AttributesSpecificationFormatter.format(
                         new Object[]
                         {
                             t_astrColumnNames[t_iColumnIndex].toLowerCase()
                         }));
 
                 if  (t_iColumnIndex < t_astrColumnNames.length - 1)
                 {
                     t_sbUpdateAttributesDeclaration.append(",");
                     t_sbCreateAttributesStatementSetterCall.append(",");
                     t_sbAttributesSpecification.append(",");
                 }
 
                 boolean t_bManagedExternally =
                     metaDataManager.isManagedExternally(
                         tableTemplate.getTableName(),
                         t_astrColumnNames[t_iColumnIndex]);
 
                 if  (t_bManagedExternally)
                 {
                     t_sbExternallyManagedInsertParametersSpecification.append(
                         t_ExternallyManagedInsertParametersSpecificationFormatter.format(
                             new Object[]
                             {
                                 t_strRepositoryName,
                                 tableTemplate.getTableName().toUpperCase(),
                                 t_astrColumnNames[t_iColumnIndex].toUpperCase(),
                                 metaDataManager.getKeyword(
                                     tableTemplate.getTableName(),
                                     t_astrColumnNames[t_iColumnIndex])
                             }));
 
                     t_sbRetrievalQueries.append(
                         metaDataManager.getExternallyManagedFieldRetrievalQuery(
                             tableTemplate.getTableName(),
                             t_astrColumnNames[t_iColumnIndex]));
 
                     t_sbUpdateAttributesStatementSetterCall.append(
                         t_StatementSetterCallFormatter.format(
                             new Object[]
                             {
                                 t_astrColumnNames[t_iColumnIndex].toLowerCase()
                             }));
 
                     if  (t_iColumnIndex < t_astrColumnNames.length - 1)
                     {
                         t_sbUpdateAttributesStatementSetterCall.append(",");
                     }
                 }
                 else
                 {
                     t_sbAttributesJavadoc.append(
                         t_AttributeJavadocFormatter.format(
                             new Object[]
                             {
                                 t_astrColumnNames[t_iColumnIndex].toLowerCase(),
                                 t_astrColumnNames[t_iColumnIndex]
                             }));
 
                     t_sbAttributesDeclaration.append(
                         t_AttributeDeclarationFormatter.format(
                             new Object[]
                             {
                                 t_strType,
                                 t_astrColumnNames[t_iColumnIndex].toLowerCase()
                             }));
 
                     if  (t_iColumnIndex < t_astrColumnNames.length - 1)
                     {
                         t_sbAttributesDeclaration.append(",");
                     }
 
                     t_sbInsertParametersSpecification.append(
                         t_InsertParametersSpecificationFormatter.format(
                             new Object[]
                             {
                                 t_strRepositoryName,
                                 tableTemplate.getTableName().toUpperCase(),
                                 t_astrColumnNames[t_iColumnIndex].toUpperCase(),
                                 t_astrColumnNames[t_iColumnIndex].toLowerCase()
                             }));
 
                     t_sbAttributesPassing.append(
                         t_AttributesPassingFormatter.format(
                             new Object[]
                             {
                                 t_astrColumnNames[t_iColumnIndex].toLowerCase()
                             }));
 
                     t_sbUpdateAttributesStatementSetterCall.append(
                         t_StatementSetterCallFormatter.format(
                             new Object[]
                             {
                                 t_astrColumnNames[t_iColumnIndex].toLowerCase()
                             }));
 
                     t_sbInsertAttributesStatementSetterCall.append(
                         t_StatementSetterCallFormatter.format(
                             new Object[]
                             {
                                 t_astrColumnNames[t_iColumnIndex].toLowerCase()
                             }));
 
                     if  (t_iColumnIndex < t_astrColumnNames.length - 1)
                     {
                         t_sbUpdateAttributesStatementSetterCall.append(",");
                         t_sbInsertAttributesStatementSetterCall.append(",");
                     }
 
                     if  (!metaDataManager.isPrimaryKey(
                              tableTemplate.getTableName(),
                              t_astrColumnNames[t_iColumnIndex]))
                     {
                         t_sbUpdateParametersSpecification.append(
                             t_UpdateParametersSpecificationFormatter.format(
                                 new Object[]
                                 {
                                     t_strRepositoryName,
                                     tableTemplate.getTableName().toUpperCase(),
                                     t_astrColumnNames[t_iColumnIndex].toUpperCase()
                                 }));
 
                         t_sbNonPkJavadoc.append(
                             t_AttributeJavadocFormatter.format(
                                 new Object[]
                                 {
                                     t_astrColumnNames[t_iColumnIndex].toLowerCase(),
                                     t_astrColumnNames[t_iColumnIndex]
                                 }));
 
                         t_sbNonPkFilter.append(
                             t_AttributeFilterFormatter.format(
                                 new Object[]
                                 {
                                     t_strRepositoryName,
                                     tableTemplate.getTableName().toUpperCase(),
                                     t_astrColumnNames[t_iColumnIndex].toUpperCase()
                                 }));
                     }
                 }
             }
 
             t_sbResult.append(
                 t_FindByPrimaryKeyFormatter.format(
                     new Object[]
                     {
                         t_strValueObjectName.toLowerCase(),
                         t_strRepositoryName,
                         tableTemplate.getTableName().toUpperCase(),
                         t_sbPkFilter,
                         t_strValueObjectName,
                         t_sbPkJavadoc,
                         t_strCapitalizedValueObjectName,
                         t_sbPkDeclaration,
                         t_sbPkStatementSetterCall,
                         t_strValueObjectName.toUpperCase()
                     }));
 
             t_sbResult.append(
                 t_InsertMethodFormatter.format(
                     new Object[]
                     {
                         t_strCapitalizedValueObjectName,
                         t_strRepositoryName,
                         tableTemplate.getTableName().toUpperCase(),
                         t_sbInsertParametersSpecification,
                         t_sbAttributesJavadoc,
                         t_sbAttributesDeclaration,
                         t_sbInsertAttributesStatementSetterCall,
                         t_sbExternallyManagedInsertParametersSpecification
                     }));
 
             t_sbResult.append(
                 t_CreateMethodFormatter.format(
                     new Object[]
                     {
                         t_strCapitalizedValueObjectName,
                         t_strRepositoryName,
                         tableTemplate.getTableName().toUpperCase(),
                        t_sbInsertParametersSpecification,
                         t_sbAttributesJavadoc,
                         t_sbAttributesDeclaration,
                         t_sbCreateAttributesStatementSetterCall,
                         t_sbAttributesPassing,
                         t_sbRetrievalQueries,
                         t_sbAttributesSpecification,
                         t_sbCreateExternallyManagedFieldValueRetrieval,
                         t_strValueObjectName.toUpperCase()
                     }));
 
             t_sbResult.append(
                 t_UpdateMethodFormatter.format(
                     new Object[]
                     {
                         t_strCapitalizedValueObjectName,
                         t_strRepositoryName,
                         tableTemplate.getTableName().toUpperCase(),
                         t_sbUpdateParametersSpecification,
                         t_sbPkFilter,
                         t_sbUpdateAttributesJavadoc,
                         t_sbUpdateAttributesDeclaration,
                         t_sbUpdateAttributesStatementSetterCall
                     }));
                 
             t_sbDeleteMethod.append(
                 t_DeleteMethodFormatter.format(
                     new Object[]
                     {
                         t_strCapitalizedValueObjectName,
                         t_strRepositoryName,
                         tableTemplate.getTableName().toUpperCase(),
                         t_sbPkFilter,
                         t_sbPkJavadoc,
                         t_sbPkDeclaration,
                         t_sbPkStatementSetterCall,
                         t_sbForeignDAODeleteCall
                     }));
 
             t_sbResult.append(t_sbDeleteMethod);
 
             t_sbResult.append(t_sbDeleteByFkMethod);
        }
 
         t_sbResult.append(buildCustomSql());
 
         t_sbResult.append(
             t_PkResultSetExtractorFormatter.format(
                 new Object[]
                 {
                     t_strCapitalizedValueObjectName,
                     t_sbPkExtractorParameterRetrieval
                 }));
 
         t_sbResult.append(classEnd);
 
         return t_sbResult.toString();
     }
 
     /**
      * Builds the custom result set extractor imports.
      * @param tableName the table name.
      * @param jdbcOperationsPackage such package.
      * @param customResultSetExtractorImport the import template.
      * @param customSqlProvider the <code>CustomSqlProvider</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @param stringUtils the <code>StringUtils</code> instance.
      * @return such generated code.
      * @precondition tableName != null
      * @precondition jdbcOperationsPackage != null
      * @precondition customResultSetExtractorImport != null
      * @precondition customSqlProvider != null
      * @precondition daoTemplateUtils != null
      * @precondition stringUtils != null
      */
     protected String buildCustomResultSetExtractorImports(
         final String tableName,
         final String jdbcOperationsPackage,
         final String customResultSetExtractorImport,
         final CustomSqlProvider customSqlProvider,
         final DAOTemplateUtils daoTemplateUtils,
         final StringUtils stringUtils)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         Collection t_cContents = customSqlProvider.getCollection();
 
         if  (t_cContents != null)
         {
             Iterator t_itContentIterator = t_cContents.iterator();
 
             MessageFormat t_CustomResultSetExtractorImportFormatter =
                 new MessageFormat(customResultSetExtractorImport);
 
             while  (t_itContentIterator.hasNext())
             {
                 Object t_Content = t_itContentIterator.next();
 
                 if  (t_Content instanceof ResultElement)
                 {
                     ResultElement t_ResultElement =
                         (ResultElement) t_Content;
 
                     if  (daoTemplateUtils.matches(
                              t_ResultElement, tableName, customSqlProvider))
                     {
                         t_sbResult.append(
                             t_CustomResultSetExtractorImportFormatter.format(
                                 new Object[]
                                 {
                                     jdbcOperationsPackage,
                                     stringUtils.capitalize(
                                         stringUtils.capitalize(
                                             stringUtils.capitalize(
                                                 t_ResultElement.getId(), '_'),
                                             '-'),
                                         '.')
                                 }));
                     }
                 }
             }
         }
 
         return t_sbResult.toString();
     }
 
     /**
      * Builds the custom result set extractor constants.
      * @param tableName the table name.
      * @param customResultSetExtractorConstant the constant template.
      * @param customSqlProvider the <code>CustomSqlProvider</code> instance.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @param stringUtils the <code>StringUtils</code> instance.
      * @return such generated code.
      * @precondition customResultSetExtractorConstant != null
      * @precondition customSqlProvider != null
      * @precondition daoTemplateUtils != null
      * @precondition stringUtils != null
      */
     protected String buildCustomResultSetExtractorConstants(
         final String tableName,
         final String customResultSetExtractorConstant,
         final CustomSqlProvider customSqlProvider,
         final DAOTemplateUtils daoTemplateUtils,
         final StringUtils stringUtils)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         Collection t_cContents = customSqlProvider.getCollection();
 
         if  (t_cContents != null)
         {
             Iterator t_itContentIterator = t_cContents.iterator();
 
             MessageFormat t_CustomResultSetExtractorConstantFormatter =
                 new MessageFormat(customResultSetExtractorConstant);
 
             while  (t_itContentIterator.hasNext())
             {
                 Object t_Content = t_itContentIterator.next();
 
                 if  (t_Content instanceof ResultElement)
                 {
                     ResultElement t_ResultElement =
                         (ResultElement) t_Content;
 
                     if  (daoTemplateUtils.matches(
                              t_ResultElement, tableName, customSqlProvider))
                     {
                         t_sbResult.append(
                             t_CustomResultSetExtractorConstantFormatter.format(
                                 new Object[]
                                 {
                                     t_ResultElement.getId(),
                                     stringUtils.removeDuplicate(
                                         stringUtils.replace(
                                             stringUtils.replace(
                                                 t_ResultElement.getId(), "-", "_"),
                                             ".",
                                             "_"),
                                         '_').toUpperCase(),
                                     stringUtils.capitalize(
                                         stringUtils.capitalize(
                                             stringUtils.capitalize(
                                                 t_ResultElement.getId(), '_'),
                                             '-'),
                                         '.')
                                 }));
                     }
                 }
             }
         }
 
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
             result.append(buildCustomSelects(provider, getTableTemplate()));
             result.append(buildCustomUpdates(provider, getTableTemplate()));
             result.append(buildCustomInserts(provider, getTableTemplate()));
             result.append(buildCustomDeletes(provider, getTableTemplate()));
             result.append(
                 buildCustomSelectForUpdates(
                     provider, getTableTemplate()));
         }
 
         return result.toString();
     }
 
     /**
      * Builds the custom selects.
      * @param provider the <code>CustomSqlProvider</code> instance.
      * @param tableTemplate the table template.
      * @return such generated code.
      * @precondition provider != null
      * @precondition tableTemplate != null
      */
     protected String buildCustomSelects(
         final CustomSqlProvider provider,
         final TableTemplate tableTemplate)
     {
         return
             buildCustomSelects(
                 provider,
                 tableTemplate.getTableName(),
                 getCustomSelect(),
                 getCustomSelectParameterJavadoc(),
                 getCustomSelectParameterDeclaration(),
                 getCustomSelectParameterTypeSpecification(),
                 getCustomSelectParameterValues(),
                 getCustomSelectResultPropertyValues(),
                 DAOTemplateUtils.getInstance(),
                 MetaDataUtils.getInstance(),
                 StringUtils.getInstance(),
                 StringValidator.getInstance());
     }
 
     /**
      * Builds the custom selects.
      * @param customSqlProvider the CustomSqlProvider instance.
      * @param tableName the table name.
      * @param customSelect the custom select.
      * @param parameterJavadoc the Javadoc template
      * of the parameters.
      * @param parameterDeclaration the parameter declaration.
      * @param parameterTypeSpecification the parameter type specification.
      * @param parameterValues the parameter values.
      * @param resultPropertyValues the result property values.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @param metaDataUtils the <code>MetaDataUtils</code> instance.
      * @param stringUtils the <code>StringUtils</code> instance.
      * @param stringValidator the <code>StringValidator</code> instance.
      * @return such generated code.
      * @precondition customSqlProvider != null
      * @precondition tableName != null
      * @precondition customSelect != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterTypeSpecification != null
      * @precondition parameterValues != null
      * @precondition resultPropertyValues != null
      * @precondition daoTemplateUtils != null
      * @precondition stringUtils != null
      * @precondition stringValidator != null
      */
     protected String buildCustomSelects(
         final CustomSqlProvider customSqlProvider,
         final String tableName,
         final String customSelect,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterTypeSpecification,
         final String parameterValues,
         final String resultPropertyValues,
         final DAOTemplateUtils daoTemplateUtils,
         final MetaDataUtils metaDataUtils,
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
 
                     if  (   (t_SqlElement.SELECT.equals(t_SqlElement.getType()))
                          && (daoTemplateUtils.matches(
                                  tableName, t_SqlElement.getDao())))
                     {
                         String[] t_astrParameterTemplates =
                             buildParameterTemplates(
                                 customSqlProvider,
                                 t_SqlElement.getParameterRefs(),
                                 parameterJavadoc,
                                 parameterDeclaration,
                                 parameterTypeSpecification,
                                 parameterValues,
                                 "",
                                 metaDataUtils,
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
                                 t_astrParameterTemplates[3],
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
      * @param parameterTypeSpecification the parameter type specification.
      * @param parameterValues the parameter values.
      * @param parameterSpecification the parameter specification.
      * @param metaDataUtils the <code>MetaDataUtils</code> instance.
      * @param stringUtils the <code>StringUtils</code> instance.
      * @param stringValidator the <code>StringValidator</code> instance.
      * @return the generated code.
      * @precondition provider != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterTypeSpecification != null
      * @precondition parameterValues != null
      * @precondition parameterSpecification != null
      * @precondition stringUtils != null
      * @precondition stringValidator != null
      */
     protected String[] buildParameterTemplates(
         final CustomSqlProvider provider,
         final Collection parameterRefs,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterTypeSpecification,
         final String parameterValues,
         final String parameterSpecification,
         final MetaDataUtils metaDataUtils,
         final StringUtils stringUtils,
         final StringValidator stringValidator)
     {
         String[] result = new String[5];
 
         StringBuffer t_sbParameterJavadoc = new StringBuffer();
         StringBuffer t_sbParameterDeclaration = new StringBuffer();
         StringBuffer t_sbParameterTypeSpecification = new StringBuffer();
         StringBuffer t_sbParameterValues = new StringBuffer();
         StringBuffer t_sbParameterSpecification = new StringBuffer();
 
         if  (parameterRefs != null)
         {
             MessageFormat t_ParameterJavadocFormatter =
                 new MessageFormat(parameterJavadoc);
 
             MessageFormat t_ParameterDeclarationFormatter =
                 new MessageFormat(parameterDeclaration);
 
             MessageFormat t_ParameterTypeSpecificationFormatter =
                 new MessageFormat(parameterTypeSpecification);
 
             MessageFormat t_ParameterValuesFormatter =
                 new MessageFormat(parameterValues);
 
             MessageFormat t_ParameterSpecificationFormatter =
                 new MessageFormat(parameterSpecification);
 
             Iterator t_itParameterRefs =
                 parameterRefs.iterator();
 
             boolean t_bFirstParameter = true;
 
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
 
                         if  (!t_bFirstParameter)
                         {
                             t_sbParameterDeclaration.append(",");
                             t_sbParameterValues.append(",");
                             t_sbParameterSpecification.append(",");
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
                         t_sbParameterTypeSpecification.append(
                             t_ParameterTypeSpecificationFormatter.format(
                                 new Object[]
                                 {
                                     metaDataUtils.getConstantName(
                                         metaDataUtils.getJavaType(
                                             t_Parameter.getType()))
                                 }));
 
                         if  (metaDataUtils.isObject(
                                  metaDataUtils.getJavaType(
                                      t_Parameter.getType())))
                         {
                             t_sbParameterValues.append(t_strName);
                         }
                         else
                         {
                             t_sbParameterValues.append(
                                 t_ParameterValuesFormatter.format(
                                     new Object[]
                                     {
                                         metaDataUtils.getObjectType(
                                             metaDataUtils.getJavaType(
                                                 t_Parameter.getType())),
                                         t_strName
                                     }));
                         }
                         t_sbParameterSpecification.append(
                             t_ParameterSpecificationFormatter.format(
                                 new Object[]
                                 {
                                     t_strName
                                 }));
                     }
                 }
 
                 t_bFirstParameter = false;
             }
         }
 
         result[0] = t_sbParameterJavadoc.toString();
         result[1] = t_sbParameterDeclaration.toString();
         result[2] = t_sbParameterTypeSpecification.toString();
         result[3] = t_sbParameterValues.toString();
         result[4] = t_sbParameterSpecification.toString();
 
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
      * @param parameterTypeSpecification the generated parameter type
      * specification.
      * @param parameterValues the generared parameter values.
      * @param resultPropertyValues the generated result property values.
      * @param stringUtils the StringUtils isntance.
      * @return the generated code.
      * @precondition provider != null
      * @precondition sqlElement != null
      * @precondition customSelect != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterTypeSpecification != null
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
         final String parameterTypeSpecification,
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
 
         String t_strResultClass = t_Result.getClassValue();
 
         if  (ResultElement.MULTIPLE.equalsIgnoreCase(
                 t_Result.getMatches()))
         {
             t_strResultClass = "List";
         }
 
         result =
             t_CustomSelectFormatter.format(
                 new Object[]
                 {
                     sqlElement.getId(),
                     sqlElement.getDescription(),
                     parameterJavadoc,
                     t_strResultClass,
                     stringUtils.unCapitalizeStart(
                         stringUtils.capitalize(
                             sqlElement.getName(), '-')),
                     parameterDeclaration,
                     processSql(sqlElement.getValue()),
                     parameterTypeSpecification,
                     parameterValues,
                     stringUtils.removeDuplicate(
                         stringUtils.replace(
                             stringUtils.replace(
                                 t_Result.getId(), "-", "_"),
                             ".",
                             "_"),
                         '_').toUpperCase()
                 });
 
         return result;
     }
 
     /**
      * Builds the custom updates.
      * @param provider the CustomSqlProvider instance.
      * @param tableTemplate the table template.
      * @return such generated code.
      * @throws RegexpEngineNotFoundException if the defined
      * regexp engine cannot be found.
      * @throws RegexpPluginMisconfiguredException if
      * RegexpPlugin cannot be configured properly.
      * @precondition provider != null
      * @precondition tableTemplate != null
      */
     protected String buildCustomUpdates(
         final CustomSqlProvider provider,
         final TableTemplate tableTemplate)
       throws  RegexpEngineNotFoundException,
               RegexpPluginMisconfiguredException
     {
         return
             buildCustomUpdatesOrInserts(
                 provider,
                 tableTemplate.getTableName(),
                 SqlElement.UPDATE,
                 getCustomUpdateOrInsert(),
                 getCustomUpdateOrInsertParameterJavadoc(),
                 getCustomUpdateOrInsertParameterDeclaration(),
                 getCustomUpdateOrInsertParameterTypeSpecification(),
                 getCustomUpdateOrInsertParameterValues(),
                 DAOTemplateUtils.getInstance(),
                 MetaDataUtils.getInstance(),
                 StringUtils.getInstance(),
                 StringValidator.getInstance(),
                 createHelper(RegexpManager.getInstance()));
     }
 
     /**
      * Builds the custom update or insert.
      * @param customSqlProvider the CustomSqlProvider instance.
      * @param tableName the table name.
      * @param type the type of operation.
      * @param customTemplate the custom template.
      * @param parameterJavadoc the Javadoc template
      * of the parameters.
      * @param parameterDeclaration the parameter declaration.
      * @param parameterTypeSpecification the parameter type specification.
      * @param parameterValues the parameter values.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @param metaDataUtils the <code>MetaDataUtils</code> instance.
      * @param stringUtils the <code>StringUtils</code> instance.
      * @param stringValidator the <code>StringValidator</code> instance.
      * @param helper the Helper instance.
      * @return such generated code.
      * @precondition customSqlProvider != null
      * @precondition tableName != null
      * @precondition type != null
      * @precondition customTemplate != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterTypeSpecification != null
      * @precondition parameterValues != null
      * @precondition daoTemplateUtils != null
      * @precondition metaDataUtils != null
      * @precondition stringUtils != null
      * @precondition stringValidator != null
      * @precondition helper != null
      */
     protected String buildCustomUpdatesOrInserts(
         final CustomSqlProvider customSqlProvider,
         final String tableName,
         final String type,
         final String customTemplate,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterTypeSpecification,
         final String parameterValues,
         final DAOTemplateUtils daoTemplateUtils,
         final MetaDataUtils metaDataUtils,
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
 
                     if  (   (type.equals(t_SqlElement.getType()))
                          && (daoTemplateUtils.matches(
                                  tableName, t_SqlElement.getDao())))
                     {
                         String[] t_astrParameterTemplates =
                             buildParameterTemplates(
                                 customSqlProvider,
                                 t_SqlElement.getParameterRefs(),
                                 parameterJavadoc,
                                 parameterDeclaration,
                                 parameterTypeSpecification,
                                 parameterValues,
                                 "",
                                 metaDataUtils,
                                 stringUtils,
                                 stringValidator);
 
                         result.append(
                             buildCustomUpdateOrInsert(
                                 customSqlProvider,
                                 t_SqlElement,
                                 customTemplate,
                                 t_astrParameterTemplates[0],
                                 t_astrParameterTemplates[1],
                                 t_astrParameterTemplates[2],
                                 t_astrParameterTemplates[3],
                                 stringUtils,
                                 helper));
                     }
                 }
             }
         }
 
         return result.toString();
     }
 
     /**
      * Builds the complete custom update or insert.
      * @param provider the CustomSqlProvider instance.
      * @param sqlElement the SqlElement instance.
      * @param customTemplate the custom template.
      * @param parameterJavadoc the generated parameter Javadoc.
      * @param parameterDeclaration the generated parameter declaration.
      * @param parameterTypeSpecification the parameter type specification.
      * @param parameterValues the generared parameter values.
      * @param stringUtils the StringUtils isntance.
      * @param helper the Helper instance.
      * @return the generated code.
      * @precondition provider != null
      * @param tableName != null
      * @precondition sqlElement != null
      * @precondition customTemplate != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterTypeSpecification != null
      * @precondition parameterValues != null
      * @precondition stringUtils != null
      * @precondition helper != null
      */
     protected String buildCustomUpdateOrInsert(
         final CustomSqlProvider provider,
         final SqlElement sqlElement,
         final String customTemplate,
         final String parameterJavadoc,
         final String parameterDeclaration,
         final String parameterTypeSpecification,
         final String parameterValues,
         final StringUtils stringUtils,
         final Helper helper)
     {
         String result = "";
 
         MessageFormat t_CustomUpdateOrInsertFormatter =
             new MessageFormat(customTemplate);
 
         ResultElement t_Result = null;
 
         ResultRefElement t_ResultRef = sqlElement.getResultRef();
 
         if  (t_ResultRef != null)
         {
             t_Result = provider.resolveReference(t_ResultRef);
         }
 
         result =
             t_CustomUpdateOrInsertFormatter.format(
                 new Object[]
                 {
                     sqlElement.getId(),
                     sqlElement.getDescription(),
                     parameterJavadoc,
                     stringUtils.unCapitalizeStart(
                         stringUtils.capitalize(
                             sqlElement.getName(), '-')),
                     parameterDeclaration,
                     processSql(sqlElement.getValue()),
                     parameterTypeSpecification,
                     parameterValues
                 });
 
         return result;
     }
 
     /**
      * Builds the custom inserts.
      * @param provider the CustomSqlProvider instance.
      * @param tableTemplate the table template.
      * @return such generated code.
      * @precondition provider != null
      * @precondition tableTemplate != null
      */
     protected String buildCustomInserts(
         final CustomSqlProvider provider,
         final TableTemplate tableTemplate)
     {
         return
             buildCustomUpdatesOrInserts(
                 provider,
                 tableTemplate.getTableName(),
                 SqlElement.INSERT,
                 getCustomUpdateOrInsert(),
                 getCustomUpdateOrInsertParameterJavadoc(),
                 getCustomUpdateOrInsertParameterDeclaration(),
                 getCustomUpdateOrInsertParameterTypeSpecification(),
                 getCustomUpdateOrInsertParameterValues(),
                 DAOTemplateUtils.getInstance(),
                 MetaDataUtils.getInstance(),
                 StringUtils.getInstance(),
                 StringValidator.getInstance(),
                 createHelper(RegexpManager.getInstance()));
     }
 
     /**
      * Builds the custom deletes.
      * @param provider the CustomSqlProvider instance.
      * @param tableTemplate the table template.
      * @return such generated code.
      * @precondition provider != null
      * @precondition tableTemplate != null
      */
     protected String buildCustomDeletes(
         final CustomSqlProvider provider,
         final TableTemplate tableTemplate)
     {
         return
             buildCustomUpdatesOrInserts(
                 provider,
                 tableTemplate.getTableName(),
                 SqlElement.DELETE,
                 getCustomUpdateOrInsert(),
                 getCustomUpdateOrInsertParameterJavadoc(),
                 getCustomUpdateOrInsertParameterDeclaration(),
                 getCustomUpdateOrInsertParameterTypeSpecification(),
                 getCustomUpdateOrInsertParameterValues(),
                 DAOTemplateUtils.getInstance(),
                 MetaDataUtils.getInstance(),
                 StringUtils.getInstance(),
                 StringValidator.getInstance(),
                 createHelper(RegexpManager.getInstance()));
     }
 
     /**
      * Builds the custom select-for-update operationss.
      * @param provider the CustomSqlProvider instance.
      * @param tableTemplate the table template.
      * @return such generated code.
      * @precondition provider != null
      * @precondition tableTemplate != null
      */
     protected String buildCustomSelectForUpdates(
         final CustomSqlProvider provider,
         final TableTemplate tableTemplate)
     {
         return
             buildCustomSelectForUpdates(
                 provider,
                 tableTemplate.getTableName(),
                 getCustomSelectForUpdate(),
                 getCustomSelectForUpdateWithNoReturn(),
                 getCustomSelectForUpdateWithReturn(),
                 getCustomSelectForUpdateWithSingleReturn(),
                 getCustomSelectForUpdateWithMultipleReturn(),
                 getCustomSelectForUpdateParameterJavadoc(),
                 getCustomSelectForUpdateReturnJavadoc(),
                 getCustomSelectForUpdateParameterDeclaration(),
                 getCustomSelectForUpdateParameterSpecification(),
                 getCustomSelectForUpdateConditionalReturn(),
                 DAOTemplateUtils.getInstance(),
                 MetaDataUtils.getInstance(),
                 StringUtils.getInstance(),
                 StringValidator.getInstance());
     }
 
     /**
      * Builds the custom select-for-update operations.
      * @param customSqlProvider the <code>CustomSqlProvider</code> instance.
      * @param tableName the table name.
      * @param customSelectForUpdate the custom-select-for-update template.
      * @param customSelectForUpdateWithNoReturn the
      * custom-select-for-update-with-no-return subtemplate.
      * @param customSelectForUpdateWithReturn the
      * custom-select-for-update-with-return subtemplate.
      * @param customSelectForUpdateWithSingleReturn the
      * custom-select-for-update-with-single-return subtemplate.
      * @param customSelectForUpdateWithMultipleReturn the
      * custom-select-for-update-with-multiple-return subtemplate.
      * @param parameterJavadoc the Javadoc template
      * of the parameters.
      * @param customSelectForUpdateReturnJavadoc the Javadoc template
      * of the return.
      * @param parameterDeclaration the parameter declaration.
      * @param parameterSpecification the parameter specification.
      * @param customSelectForUpdateConditionalReturn the subtemplate
      * to conditionally provide return statement in select-for-update operations.
      * @param daoTemplateUtils the <code>DAOTemplateUtils</code> instance.
      * @param metaDataUtils the <code>MetaDataUtils</code> instance.
      * @param stringUtils the <code>StringUtils</code> instance.
      * @param stringValidator the <code>StringValidator</code> instance.
      * @return such generated code.
      * @precondition customSqlProvider != null
      * @precondition tableName != null
      * @precondition customSelectForUpdate != null
      * @precondition customSelectForUpdateWithNoReturn != null
      * @precondition customSelectForUpdateWithReturn != null
      * @precondition customSelectForUpdateWithSingleReturn != null
      * @precondition customSelectForUpdateWithMultipleReturn != null
      * @precondition parameterJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition daoTemplateUtils != null
      * @precondition metaDataUtils != null
      * @precondition stringUtils != null
      * @precondition stringValidator != null
      */
     protected String buildCustomSelectForUpdates(
         final CustomSqlProvider customSqlProvider,
         final String tableName,
         final String customSelectForUpdate,
         final String customSelectForUpdateWithNoReturn,
         final String customSelectForUpdateWithReturn,
         final String customSelectForUpdateWithSingleReturn,
         final String customSelectForUpdateWithMultipleReturn,
         final String parameterJavadoc,
         final String customSelectForUpdateReturnJavadoc,
         final String parameterDeclaration,
         final String parameterSpecification,
         final String customSelectForUpdateConditionalReturn,
         final DAOTemplateUtils daoTemplateUtils,
         final MetaDataUtils metaDataUtils,
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
 
                     if  (   (t_SqlElement.SELECT_FOR_UPDATE.equals(
                                  t_SqlElement.getType()))
                          && (daoTemplateUtils.matches(
                                  tableName, t_SqlElement.getDao())))
                     {
                         String[] t_astrParameterTemplates =
                             buildParameterTemplates(
                                 customSqlProvider,
                                 t_SqlElement.getParameterRefs(),
                                 parameterJavadoc,
                                 parameterDeclaration,
                                 "",
                                 "",
                                 parameterSpecification,
                                 metaDataUtils,
                                 stringUtils,
                                 stringValidator);
 
                         result.append(
                             buildCustomSelectForUpdate(
                                 customSqlProvider,
                                 t_SqlElement,
                                 customSelectForUpdate,
                                 customSelectForUpdateWithNoReturn,
                                 customSelectForUpdateWithReturn,
                                 customSelectForUpdateWithSingleReturn,
                                 customSelectForUpdateWithMultipleReturn,
                                 t_astrParameterTemplates[0],
                                 customSelectForUpdateReturnJavadoc,
                                 t_astrParameterTemplates[1],
                                 t_astrParameterTemplates[4],
                                 customSelectForUpdateConditionalReturn,
                                 stringUtils));
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
      * @param customSelectForUpdate the custom select-for-update template.
      * @param customSelectForUpdateWithNoReturn the
      * custom-select-for-update-with-no-return subtemplate.
      * @param customSelectForUpdateWithReturn the
      * custom-select-for-update-with-return subtemplate.
      * @param customSelectForUpdateWithSingleReturn the
      * custom-select-for-update-with-single-return subtemplate.
      * @param customSelectForUpdateWithMultipleReturn the
      * custom-select-for-update-with-multiple-return subtemplate.
      * @param parameterJavadoc the generated parameter Javadoc.
      * @param returnJavadoc the generated return Javadoc.
      * @param parameterDeclaration the generated parameter declaration.
      * @param parameterSpecification the generated parameter specification.
      * @param conditionalReturn the conditional return.
      * @param stringUtils the StringUtils isntance.
      * @return the generated code.
      * @precondition provider != null
      * @precondition sqlElement != null
      * @precondition customSelectForUpdate != null
      * @precondition customSelectForUpdateWithNoReturn != null
      * @precondition customSelectForUpdateWithReturn != null
      * @precondition customSelectForUpdateWithSingleReturn != null
      * @precondition customSelectForUpdateWithMultipleReturn != null
      * @precondition parameterJavadoc != null
      * @precondition returnJavadoc != null
      * @precondition parameterDeclaration != null
      * @precondition parameterSpecification != null
      * @precondition conditionalReturn != null
      * @precondition stringUtils != null
      */
     protected String buildCustomSelectForUpdate(
         final CustomSqlProvider provider,
         final SqlElement sqlElement,
         final String customSelectForUpdate,
         final String customSelectForUpdateWithNoReturn,
         final String customSelectForUpdateWithReturn,
         final String customSelectForUpdateWithSingleReturn,
         final String customSelectForUpdateWithMultipleReturn,
         final String parameterJavadoc,
         final String returnJavadoc,
         final String parameterDeclaration,
         final String parameterSpecification,
         final String conditionalReturn,
         final StringUtils stringUtils)
     {
         String result = "";
 
         MessageFormat t_CustomSelectForUpdateFormatter =
             new MessageFormat(customSelectForUpdate);
 
         ResultElement t_Result = null;
 
         ResultRefElement t_ResultRef = sqlElement.getResultRef();
 
         if  (t_ResultRef != null)
         {
             t_Result = provider.resolveReference(t_ResultRef);
         }
 
         String t_strCustomSelectForUpdateTemplate =
             customSelectForUpdateWithReturn;
 
         String t_strCustomSelectForUpdateSubtemplate = "";
 
         String t_strReturn = "void";
 
         String t_strConditionalReturn = "";
 
         String t_strReturnJavadoc = "";
 
         if (   (t_Result != null)
             && (ResultElement.SINGLE.equalsIgnoreCase(
                     t_Result.getMatches())))
         {
             t_strCustomSelectForUpdateSubtemplate =
                 customSelectForUpdateWithSingleReturn;
 
             t_strReturn = t_Result.getClassValue();
 
             t_strReturnJavadoc = returnJavadoc;
 
             t_strConditionalReturn = conditionalReturn;
         }
         else if  (   (t_Result != null)
                   && (ResultElement.MULTIPLE.equalsIgnoreCase(
                           t_Result.getMatches())))
         {
             t_strCustomSelectForUpdateSubtemplate =
                 customSelectForUpdateWithMultipleReturn;
 
             t_strReturn = "List";
 
             t_strReturnJavadoc = returnJavadoc;
 
             t_strConditionalReturn = conditionalReturn;
         }
         else
         {
             t_strCustomSelectForUpdateTemplate =
                 customSelectForUpdateWithNoReturn;
         }
 
         result =
             t_CustomSelectForUpdateFormatter.format(
                 new Object[]
                 {
                     sqlElement.getId(),
                     sqlElement.getDescription(),
                     parameterJavadoc,
                     t_strReturnJavadoc,
                     t_strReturn,
                     stringUtils.unCapitalizeStart(
                         stringUtils.capitalize(
                             sqlElement.getName(), '-')),
                     parameterDeclaration,
                     parameterSpecification,
                     buildCustomSelectForUpdate(
                         t_strCustomSelectForUpdateTemplate,
                         t_strCustomSelectForUpdateSubtemplate,
                         stringUtils.capitalize(
                             stringUtils.capitalize(
                                 stringUtils.capitalize(
                                     sqlElement.getName(), '_'),
                                 '-'),
                             '.'),
                         processSql(sqlElement.getValue()),
                         t_strReturn),
                     t_strConditionalReturn
                 });
 
         return result;
     }
 
     /**
      * Builds the custom select-for-update template.
      * @param template the template.
      * @param subtemplate the subtemplate.
      * @param queryObjectName the query object name.
      * @param sqlQuery the sql query.
      * @param resultType the result type.
      * @return the generated code.
      * @precondition template != null
      * @precondition subtemplate != null
      * @precondition customSelectForUpdateWithNoReturn != null
      * @precondition queryObjectName != null
      * @precondition sqlQuery != null
      * @precondition resultType != null
      */
     protected String buildCustomSelectForUpdate(
         final String template,
         final String subtemplate,
         final String queryObjectName,
         final String sqlQuery,
         final String resultType)
     {
         String result = "";
 
         MessageFormat t_Formatter =
             new MessageFormat(template);
 
         result =
             t_Formatter.format(
                 new Object[]
                 {
                     queryObjectName,
                     sqlQuery,
                     resultType,
                     subtemplate
                 });
 
         return result;
     }
 
     /**
      * Builds the subtemplate for <i>process connection flags</i>.
      * @param connectionFlags the connection flags.
      * @param processConnectionFlags the subtemplate.
      * @return the generated code.
      * @precondition processConnectionFlags != null
      */
     protected String buildProcessConnectionFlagsSubtemplate(
         final String[] connectionFlags,
         final String processConnectionFlags)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         if  (   (connectionFlags != null)
              && (connectionFlags.length > 0))
         {
             MessageFormat t_ProcessConnectionFlagsFormatter =
                 new MessageFormat(processConnectionFlags);
 
             for  (int t_iIndex = 0;
                       t_iIndex < connectionFlags.length;
                       t_iIndex++)
             {
                 t_sbResult.append(
                     t_ProcessConnectionFlagsFormatter.format(
                         new Object[]{connectionFlags[t_iIndex]}));
             }
         }
 
         return t_sbResult.toString();
     }
 
 
     /**
      * Builds the subtemplate for <i>process statement flags</i>.
      * @param statementFlags the statement flags.
      * @param processStatementFlags the subtemplate.
      * @param statementFlagsSetters the setter methods for statement flags.
      * @param stringValidator the StringValidator instance.
      * @return the generated code.
      * @precondition processStatementFlags != null
      * @precondition statementFlagsSetters != null
      * @precondition stringValidator != null
      */
     protected String buildProcessStatementFlagsSubtemplate(
         final String[] statementFlags,
         final String processStatementFlags,
         final String[] statementFlagsSetters,
         final StringValidator stringValidator)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         if  (   (statementFlags != null)
              && (statementFlags.length > 0))
         {
             MessageFormat t_ProcessStatementFlagsFormatter =
                 new MessageFormat(processStatementFlags);
 
             for  (int t_iIndex = 0;
                       t_iIndex < statementFlags.length;
                       t_iIndex++)
             {
                 if  (   (!stringValidator.isEmpty(statementFlags[t_iIndex]))
                      && (!stringValidator.isEmpty(
                              statementFlagsSetters[t_iIndex])))
                 {
                     MessageFormat t_CurrentSetterFormatter =
                         new MessageFormat(statementFlagsSetters[t_iIndex]);
 
                     t_sbResult.append(
                         t_ProcessStatementFlagsFormatter.format(
                             new Object[]
                             {
                                 t_CurrentSetterFormatter.format(
                                     new Object[]
                                     {
                                         statementFlags[t_iIndex]
                                     })
                             }));
                 }
             }
         }
 
         return t_sbResult.toString();
     }
 
     /**
      * Builds the subtemplate for <i>process resultset flags</i>.
      * @param resultSetFlags the resultset flags.
      * @param processResultSetFlags the template.
      * @param processResultSetFlagSubtemplate the subtemplate.
      * @param stringValidator the StringValidator instance.
      * @return the generated code.
      * @precondition processResultSetFlags != null
      * @precondition processResultSetFlagSubtemplate != null
      * @precondition stringValidator != null
      */
     protected String buildProcessResultSetFlagsSubtemplate(
         final String[] resultSetFlags,
         final String processResultSetFlags,
         final String processResultSetFlagSubtemplate,
         final StringValidator stringValidator)
     {
         String result = null;
 
         if  (   (resultSetFlags != null)
              && (resultSetFlags.length > 0))
         {
             MessageFormat t_ProcessResultSetFlagsFormatter =
                 new MessageFormat(processResultSetFlags);
 
             MessageFormat t_ProcessResultSetFlagSubtemplateFormatter =
                 new MessageFormat(processResultSetFlagSubtemplate);
 
             String[] t_astrResultSetFlags =
                 new String[resultSetFlags.length];
 
             for  (int t_iIndex = 0;
                       t_iIndex < t_astrResultSetFlags.length;
                       t_iIndex++)
             {
                 String t_strCurrentFlag = resultSetFlags[t_iIndex];
 
                 if  (!stringValidator.isEmpty(t_strCurrentFlag))
                 {
                     t_strCurrentFlag =
                         t_ProcessResultSetFlagSubtemplateFormatter.format(
                             new Object[]
                             {
                                 t_strCurrentFlag
                             });
                 }
 
                 t_astrResultSetFlags[t_iIndex] = t_strCurrentFlag;
             }
 
             result =
                 t_ProcessResultSetFlagsFormatter.format(
                     t_astrResultSetFlags);
         }
 
         return result;
     }
 
     /**
      * Requests a regexp helper to given RegexpManager instance.
      * @param regexpManager the RegexpManager instance.
      * @return the regexp helper.
      * @throws RegexpEngineNotFoundException if the defined
      * regexp engine cannot be found.
      * @throws RegexpPluginMisconfiguredException if
      * RegexpPlugin cannot be configured properly.
      * @precondition regexpManager != null
      */
     protected synchronized Helper createHelper(
         final RegexpManager regexpManager)
       throws  RegexpEngineNotFoundException,
               RegexpPluginMisconfiguredException
     {
         return createHelper(regexpManager.getEngine());
     }
 
     /**
      * Requests a regexp helper to given RegexpEngine instance.
      * @param regexpEngine the RegexpEngine instance.
      * @return the regexp helper.
      * @precondition regexpEngine != null
      */
     protected Helper createHelper(
         final RegexpEngine regexpEngine)
       throws  RegexpEngineNotFoundException,
               RegexpPluginMisconfiguredException
     {
         return regexpEngine.createHelper();
     }
 
     /**
      * Processes given SQL.
      * @param sql the sql to process.
      * @return the processed sql.
      * @preocndition sql != null
      */
     protected String processSql(final String sql)
     {
         return
             processSql(
                 sql,
                 StringUtils.getInstance());
     }
 
     /**
      * Processes given SQL.
      * @param sql the sql to process.
      * @param stringUtils the <code>StringUtils</code> instance.
      * @return the processed sql.
      * @preocndition sql != null
      * @precondition stringUtils != null
      */
     protected String processSql(
         final String sql, final StringUtils stringUtils)
     {
         String result = sql;
 
         result = stringUtils.replace(result,  "\"", "\\\"");
         result = stringUtils.replace(result, "\n", " ");
 
         return result;
     }
 }
