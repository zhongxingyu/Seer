 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.sm.business;
 
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.business.filter.DbRequestParameters;
 import de.escidoc.core.common.business.filter.SRURequestParameters;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSqlException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ReportDefinitionNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ScopeNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.ScopeContextViolationException;
 import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.xml.factory.ExplainXmlProvider;
 import de.escidoc.core.sm.business.filter.ReportDefinitionFilter;
 import de.escidoc.core.sm.business.interfaces.ReportDefinitionHandlerInterface;
 import de.escidoc.core.sm.business.persistence.DirectDatabaseAccessorInterface;
 import de.escidoc.core.sm.business.persistence.SmAggregationDefinitionsDaoInterface;
 import de.escidoc.core.sm.business.persistence.SmReportDefinitionsDaoInterface;
 import de.escidoc.core.sm.business.persistence.SmScopesDaoInterface;
 import de.escidoc.core.sm.business.persistence.hibernate.AggregationDefinition;
 import de.escidoc.core.sm.business.persistence.hibernate.ReportDefinition;
 import de.escidoc.core.sm.business.persistence.hibernate.Scope;
 import de.escidoc.core.sm.business.renderer.interfaces.ReportDefinitionRendererInterface;
 import de.escidoc.core.sm.business.stax.handler.ReportDefinitionStaxHandler;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 /**
  * An statistic ReportDefinition resource handler.
  * 
  * @spring.bean id="business.ReportDefinitionHandler" scope="prototype"
  * @author MIH
  * @sm
  */
 public class ReportDefinitionHandler
     implements ReportDefinitionHandlerInterface {
 
     private static AppLogger log = new AppLogger(
         ReportDefinitionHandler.class.getName());
 
     private SmReportDefinitionsDaoInterface dao;
 
     private SmScopesDaoInterface scopesDao;
 
     private SmAggregationDefinitionsDaoInterface aggregationDefinitionsDao;
 
     private DirectDatabaseAccessorInterface dbAccessor;
 
     private SmXmlUtility xmlUtility;
 
     private SmFilterUtility filterUtility;
 
     private ReportDefinitionRendererInterface renderer;
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.interfaces
      *      .ReportDefinitionHandlerInterface #create(java.lang.String)
      * 
      * @param xmlData
      *            ReportDefinition as xml in ReportDefinition schema.
      * @return Returns the XML representation of the resource.
      * 
      * @throws MissingMethodParameterException
      *             ex
      * @throws ScopeNotFoundException
      *             ex
      * @throws ScopeContextViolationException
      *             ex
      * @throws InvalidSqlException
      *             ex
      * @throws SystemException
      *             ex
      * 
      * @sm
      */
     public String create(final String xmlData) throws InvalidSqlException,
         MissingMethodParameterException, ScopeNotFoundException,
         ScopeContextViolationException, SystemException {
         if (log.isDebugEnabled()) {
             log.debug("ReportDefinitionHandler does create");
         }
         if (xmlData == null || xmlData.equals("")) {
             log.error("xml may not be null");
             throw new MissingMethodParameterException("xml may not be null");
         }
         // parse
         StaxParser sp = new StaxParser();
         ReportDefinitionStaxHandler handler =
             new ReportDefinitionStaxHandler();
         sp.addHandler(handler);
         try {
             sp.parse(xmlData);
         }
         catch (Exception e) {
             log.error(e);
             throw new SystemException(e);
         }
 
         String scopeId = handler.getReportDefinition().getScope().getId();
         Scope scope = scopesDao.retrieve(scopeId);
 
         ReportDefinition reportDefinition = handler.getReportDefinition();
         Utility utility = new Utility();
         reportDefinition.setCreatorId(utility.getCurrentUserId());
         reportDefinition.setModifiedById(reportDefinition.getCreatorId());
         reportDefinition.setLastModificationDate(new Timestamp(System
             .currentTimeMillis()));
         reportDefinition.setCreationDate(reportDefinition
             .getLastModificationDate());
         reportDefinition.setScope(scope);
 
         // check if sql is executable and
         // only accesses tables that belong to the correct scope
         checkSql(reportDefinition.getSql(), scopeId);
 
         dao.save(reportDefinition);
 
         return renderer.render(reportDefinition);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.interfaces
      *      .ReportDefinitionHandlerInterface #delete(java.lang.String)
      * 
      * @param id
      *            resource id.
      * 
      * @throws ReportDefinitionNotFoundException
      *             e.
      * @throws MissingMethodParameterException
      *             e.
      * @throws SystemException
      *             e.
      * 
      * @sm
      */
     public void delete(final String id)
         throws ReportDefinitionNotFoundException,
         MissingMethodParameterException, SystemException {
         if (log.isDebugEnabled()) {
             log.debug("ReportDefinitionHandler does delete");
         }
         if (id == null) {
             log.error("id may not be null");
             throw new MissingMethodParameterException("id may not be null");
         }
 
         ReportDefinition reportDefinition = dao.retrieve(id);
         dao.delete(reportDefinition);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.interfaces
      *      .ReportDefinitionHandlerInterface #retrieve(java.lang.String)
      * 
      * @param id
      *            resource id.
      * @return Returns the XML representation of the resource.
      * 
      * @throws ReportDefinitionNotFoundException
      *             e.
      * @throws MissingMethodParameterException
      *             e.
      * @throws SystemException
      *             e.
      * 
      * @sm
      */
     public String retrieve(final String id)
         throws ReportDefinitionNotFoundException,
         MissingMethodParameterException, SystemException {
         if (log.isDebugEnabled()) {
             log.debug("ReportDefinitionHandler does retrieve");
         }
         if (id == null) {
             log.error("id may not be null");
             throw new MissingMethodParameterException("id may not be null");
         }
         return renderer.render(dao.retrieve(id));
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.interfaces
      *      .ReportDefinitionHandlerInterface #retrieveReportDefinitions()
      * 
      * @param parameters
      *            filter as CQL query
      * 
      * @return Returns the XML representation of the resource-list.
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws SystemException
      *             e.
      */
     public String retrieveReportDefinitions(
         final Map<String, String[]> parameters)
         throws InvalidSearchQueryException, SystemException {
         String result = null;
         SRURequestParameters params =
             new DbRequestParameters((Map<String, String[]>) parameters);
         String query = params.getQuery();
         int limit = params.getLimit();
         int offset = params.getOffset();
 
         if (params.isExplain()) {
             Map<String, Object> values = new HashMap<String, Object>();
 
             values.put("PROPERTY_NAMES",
                 new ReportDefinitionFilter(null).getPropertyNames());
             result =
                 ExplainXmlProvider.getInstance().getExplainReportDefinitionXml(
                     values);
         }
         else {
             // get all scope-ids from database
             Collection<String> scopeIds = scopesDao.retrieveScopeIds();
 
             Collection<String> filteredScopeIds = null;
             Collection<ReportDefinition> reportDefinitions = null;
 
             if (scopeIds != null && !scopeIds.isEmpty()) {
                 // get scope-ids filtered by user-privileges
                 filteredScopeIds =
                     filterUtility.filterRetrievePrivilege(
                         Constants.SCOPE_OBJECT_TYPE, scopeIds);
             }
 
             int numberOfRecords = 0;
 
             if (filteredScopeIds != null && !filteredScopeIds.isEmpty()) {
                 // get report-definitions as xml
                 reportDefinitions =
                     dao.retrieveReportDefinitions(filteredScopeIds, query,
                         offset, limit);
                 if (reportDefinitions != null) {
                     numberOfRecords = reportDefinitions.size();
                 }
             }
 
             result =
                 renderer.renderReportDefinitions(reportDefinitions);
         }
         return result;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.sm.business.interfaces
      *      .ReportDefinitionHandlerInterface
      *      #update(java.lang.String,java.lang.String)
      * 
      * @param xmlData
      *            ReportDefinition data as xml in ReportDefinition schema.
      * @param id
      *            resource id.
      * @return Returns the XML representation of the resource.
      * 
      * @throws ReportDefinitionNotFoundException
      *             e.
      * @throws MissingMethodParameterException
      *             e.
      * @throws ScopeNotFoundException
      *             ex
      * @throws ScopeContextViolationException
      *             ex
      * @throws InvalidSqlException
      *             ex
      * @throws SystemException
      *             e.
      * 
      * @sm
      */
     public String update(final String id, final String xmlData)
         throws ReportDefinitionNotFoundException,
         MissingMethodParameterException, ScopeNotFoundException,
         InvalidSqlException, ScopeContextViolationException, SystemException {
         if (log.isDebugEnabled()) {
             log.debug("ReportDefinitionHandler does update");
         }
         if (id == null || id.equals("")) {
             log.error("id may not be null");
             throw new MissingMethodParameterException("id may not be null");
         }
         if (xmlData == null) {
             log.error("xmlData may not be null");
             throw new MissingMethodParameterException("xmlData may not be null");
         }
 
         // parse
         StaxParser sp = new StaxParser();
         ReportDefinitionStaxHandler handler =
             new ReportDefinitionStaxHandler();
         handler.setReportDefinition(dao.retrieve(id));
         sp.addHandler(handler);
         try {
             sp.parse(xmlData);
         }
         catch (Exception e) {
             log.error(e);
             throw new SystemException(e);
         }
 
         ReportDefinition reportDefinition = handler.getReportDefinition();
 
         String scopeId = reportDefinition.getScope().getId();
         Scope scope = scopesDao.retrieve(scopeId);
 
         Utility utility = new Utility();
         reportDefinition.setModifiedById(utility.getCurrentUserId());
         reportDefinition.setLastModificationDate(new Timestamp(System
             .currentTimeMillis()));
         reportDefinition.setScope(scope);
 
         // check if sql is executable and
         // only accesses tables that belong to the correct scope
         checkSql(reportDefinition.getSql(), scopeId);
 
         dao.update(reportDefinition);
 
         return renderer.render(reportDefinition);
     }
 
     /**
      * Checks: 
      * -if sql only accesses aggregation-tables that belong to the scope
      *  with the given scopeId. 
      * -if sql is executable
      * -if sql only selects and doesnt do other things
      * 
      * @param sql
      *            sql-statement of report-definition.
      * @param scopeId
      *            scope-id of report-definition.
      * 
      * @throws ScopeContextViolationException
      *             ex
      * @throws InvalidSqlException
      *             ex
      * @throws ScopeNotFoundException
      *             ex
      * @throws SystemException
      *             ex
      * 
      * @sm
      */
     private void checkSql(final String sql, final String scopeId)
         throws ScopeContextViolationException, InvalidSqlException,
         ScopeNotFoundException, SystemException {
         // getScope
         Scope scope = scopesDao.retrieve(scopeId);
 
         if (scope == null) {
             throw new ScopeNotFoundException("Scope not found");
         }
         
         if (sql == null) {
             throw new InvalidSqlException("sql is null");
         }
         
         // check if sql is executable
         try {
             dbAccessor.executeReadOnlySql(generateFakeSql(sql));
         }
         catch (SqlDatabaseSystemException e) {
             throw new InvalidSqlException(e);
         }
 
         // check scope-type. If type=admin, sql may access all
         // aggregation-tables
         // if scopeType != admin: check requested tables
         if (!scope.getScopeType().equals(Constants.SCOPE_TYPE_ADMIN)) {
             // get Aggregation-definitions
             Collection<String> scopeIds = new ArrayList<String>();
             scopeIds.add(scopeId);
             Collection<AggregationDefinition> aggregationDefinitions =
                 aggregationDefinitionsDao
                     .retrieveAggregationDefinitions(scopeIds);
 
             // Collect aggregation-definition primary-keys
             HashSet<String> allowedPrimKeys = new HashSet<String>();
             for (AggregationDefinition aggregationDefinition : aggregationDefinitions) {
                 String primKey = aggregationDefinition.getId();
                 primKey = xmlUtility.convertPrimKeyToTableName(primKey);
                 allowedPrimKeys.add(primKey);
             }
 
             // extract tablenames from sql
             Collection<String> primKeys =
                 xmlUtility.extractAggregationPrimKeysFromSql(sql);
 
             // check primKeys against allowed primKeys
             for (String primKey : primKeys) {
                 if (!allowedPrimKeys.contains(primKey)) {
                     throw new ScopeContextViolationException(
                         "AggregationTable with prefix=_" + primKey
                             + "_ does not belong to the scope "
                             + "of the reportDefinition");
                 }
 
             }
 
         }
     }
 
     /**
      * replaces placeholders from sql and returns replaced sql.
      * 
      * @param sql
      *            sql-statement of report-definition.
      * @return String replacedSql
      * @sm
      */
     private String generateFakeSql(final String sql) {
        return sql.replaceAll("(?s)'?\"?\\{.*?\\}'?\"?", "'1'");
     }
 
     /**
      * Setter for the dao.
      * 
      * @spring.property ref="persistence.SmReportDefinitionsDao"
      * @param dao
      *            The data access object.
      * 
      * @sm
      */
     public void setDao(final SmReportDefinitionsDaoInterface dao) {
         this.dao = dao;
     }
 
     /**
      * Setting the xmlUtility.
      * 
      * @param xmlUtility
      *            The xmlUtility to set.
      * @spring.property ref="business.sm.XmlUtility"
      */
     public final void setXmlUtility(final SmXmlUtility xmlUtility) {
         this.xmlUtility = xmlUtility;
     }
 
     /**
      * Setter for the scopesDao.
      * 
      * @spring.property ref="persistence.SmScopesDao"
      * @param scopesDao
      *            The data access object.
      * 
      * @sm
      */
     public void setScopesDao(final SmScopesDaoInterface scopesDao) {
         this.scopesDao = scopesDao;
     }
 
     /**
      * Setter for the aggregationDefinitions.
      * 
      * @spring.property ref="persistence.SmAggregationDefinitionsDao"
      * @param aggregationDefinitionsDao
      *            The data access object.
      * 
      * @sm
      */
     public void setAggregationDefinitionsDao(
         final SmAggregationDefinitionsDaoInterface aggregationDefinitionsDao) {
         this.aggregationDefinitionsDao = aggregationDefinitionsDao;
     }
 
     /**
      * Setting the directDatabaseAccessor.
      * 
      * @param dbAccessorIn
      *            The directDatabaseAccessor to set.
      * @spring.property ref="sm.persistence.DirectDatabaseAccessor"
      */
     public final void setDirectDatabaseAccessor(
         final DirectDatabaseAccessorInterface dbAccessorIn) {
         this.dbAccessor = dbAccessorIn;
     }
 
     /**
      * Setting the filterUtility.
      * 
      * @param filterUtility
      *            The filterUtility to set.
      * @spring.property ref="business.sm.FilterUtility"
      */
     public final void setFilterUtility(final SmFilterUtility filterUtility) {
         this.filterUtility = filterUtility;
     }
 
     /**
      * Injects the renderer.
      * 
      * @param renderer
      *            The renderer to inject.
      * 
      * @spring.property ref=
      *                  "eSciDoc.core.aa.business.renderer.VelocityXmlReportDefinitionRenderer"
      * @aa
      */
     public void setRenderer(final ReportDefinitionRendererInterface renderer) {
         this.renderer = renderer;
     }
 
 }
