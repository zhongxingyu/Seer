 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.business.fedora.mptstore;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.IOUtils;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.db.DatabaseType;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import org.nsdl.mptstore.core.BasicTableManager;
 import org.nsdl.mptstore.core.DDLGenerator;
 import org.nsdl.mptstore.core.TableManager;
 import org.nsdl.mptstore.rdf.URIReference;
 import org.nsdl.mptstore.util.NTriplesUtil;
 import org.springframework.jdbc.CannotGetJdbcConnectionException;
 
 import javax.sql.DataSource;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 /**
  * To use is as implementation of the abstract class TripleStoreUtility register this as spring.bean
  * id="business.TripleStoreUtility".
  *
  * @author Frank Schwichtenberg
  */
 public class MPTTripleStoreUtility extends TripleStoreUtility {
 
     private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s");
 
     private TableManager tableManager;
 
     private DatabaseType databaseType;
 
     /**
      * Injects the data source.
      * @param myDataSource
      */
     public void setMyDataSource(final DataSource myDataSource) {
         setDataSource(myDataSource);
     }
 
     /**
      *
      */
     @Override
     public Map<String, String> getProperties(final String pid, final Collection<String> fullqualifiedNamedProperties)
         throws TripleStoreSystemException {
         final Map<String, String> result = new HashMap<String, String>();
         for (final String property : fullqualifiedNamedProperties) {
             result.put(property, getRelation(pid, property));
         }
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility#executeQueryId (java.lang.String, boolean,
      * java.lang.String)
      */
     @Override
     public List<String> executeQueryId(final String id, final boolean targetIsSubject, final String predicate)
         throws TripleStoreSystemException {
         return executeQuery(false, id, targetIsSubject, predicate);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility#executeQueryLiteral (java.lang.String, boolean,
      * java.lang.String)
      */
     @Override
     protected List<String> executeQueryLiteral(
         final String literal, final boolean targetIsSubject, final String predicate) throws TripleStoreSystemException {
         return executeQuery(true, literal, targetIsSubject, predicate);
     }
 
     @Override
     protected String executeQueryEarliestCreationDate() throws TripleStoreSystemException {
         String result = null;
         final String tableName = getTableName(PROP_CREATION_DATE);
         if (tableName != null) {
             // TODO: Possible SQL injection? Fix this!
             final String select = "SELECT min(o) FROM " + tableName;
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             final List<String> results = executeSqlQuery(select);
 
             if (getLogger().isDebugEnabled()) {
                 if (results.isEmpty()) {
                     getLogger().debug("found no records");
                 }
                 else {
                     getLogger().debug("found " + results.size() + " records");
                     for (final String item : results) {
                         getLogger().debug("item: " + item);
                     }
                 }
             }
 
             if (results.size() == 1) {
                 result = results.get(0);
             }
             else {
                 final String message = "More than one result for earliest creation date.";
                 getLogger().error(message);
                 throw new TripleStoreSystemException(message);
             }
         }
         return result;
 
     }
 
     /**
      *
      * @param queryByLiteral
      * @param idOrLiteral
      * @param targetIsSubject
      * @param predicate
      * @return
      * @throws TripleStoreSystemException
      */
     private List<String> executeQuery(
         final boolean queryByLiteral, final String idOrLiteral, final boolean targetIsSubject, final String predicate)
         throws TripleStoreSystemException {
 
         List<String> result = new ArrayList<String>();
         final String tableName = getTableName(predicate);
         if (tableName != null) {
             final CharSequence table = new StringBuilder(tableName);
             StringBuffer select = new StringBuffer("SELECT ");
             final StringBuilder from = new StringBuilder("FROM ").append(table).append(' ');
             StringBuffer where = new StringBuffer("WHERE (");
 
             if (targetIsSubject) {
                 select = select.append(table).append(".s").append(' ');
                 where = where.append(table).append(".o = ");
             }
             else {
                 select = select.append(table).append(".o").append(' ');
                 where = where.append(table).append(".s = ");
             }
             if (queryByLiteral) {
                 where = where.append("'\"").append(MPTStringUtil.escapeLiteralValueForSql(idOrLiteral)).append("\"')");
             }
             else {
                 try {
                     where =
                         where.append('\'').append(
                             new URIReference(Constants.IDENTIFIER_PREFIX + idOrLiteral).toString()).append("')");
                 }
                 catch (final URISyntaxException e) {
                     throw new TripleStoreSystemException(e.getMessage(), e);
                 }
             }
             select = select.append(from).append(where);
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             result = executeSqlQuery(select.toString());
             if (getLogger().isDebugEnabled()) {
                 if (result.isEmpty()) {
                     getLogger().debug("found no records");
                 }
                 else {
                     getLogger().debug("found " + result.size() + " records");
                     for (final String item : result) {
                         getLogger().debug("item: " + item);
                     }
                 }
             }
         }
         return result;
     }
 
     /**
      *
      * @param ids
      * @param targetIsSubject
      * @param predicate
      * @return
      * @throws TripleStoreSystemException
      */
     @Override
     public List<String> executeQueryForList(
         final Collection<String> ids, final boolean targetIsSubject, final String predicate)
         throws TripleStoreSystemException {
 
         List<String> result = new ArrayList<String>();
         final String tableName = getTableName(predicate);
         if (tableName != null) {
             final CharSequence table = new StringBuilder(tableName);
             StringBuffer select = new StringBuffer("SELECT ");
             final StringBuilder from = new StringBuilder("FROM ").append(table).append(' ');
             StringBuffer where = new StringBuffer("WHERE (");
             select = targetIsSubject ? select.append(table).append(".s ") : select.append(table).append(".o ");
             final Iterator<String> iterator = ids.iterator();
             boolean firstStep = true;
             while (iterator.hasNext()) {
                 final String id = iterator.next();
                 if (firstStep) {
                     firstStep = false;
                     where = targetIsSubject ? where.append(table).append(".o = ") : where.append(table).append(".s = ");
                 }
                 else {
                     where = where.append(" OR ");
                     where = targetIsSubject ? where.append(table).append(".o = ") : where.append(table).append(".s = ");
                 }
 
                 try {
                     where =
                         where
                             .append('\'').append(new URIReference(Constants.IDENTIFIER_PREFIX + id).toString()).append(
                                 '\'');
                 }
                 catch (final URISyntaxException e) {
                     throw new TripleStoreSystemException(e.getMessage(), e);
                 }
 
             }
             where = where.append(')');
             select = select.append(from).append(where);
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             result = executeSqlQuery(select.toString());
             if (getLogger().isDebugEnabled()) {
                 if (result.isEmpty()) {
                     getLogger().debug("found no records");
                 }
                 else {
                     getLogger().debug("found " + result.size() + " records");
                     for (final String item : result) {
                         getLogger().debug("item: " + item);
                     }
                 }
             }
         }
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility#getRelation (java.lang.String, java.lang.String,
      * java.lang.String)
      */
     @Override
     @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
     public String getRelation(final String pid, final String fullQualifiedPropertyName)
         throws TripleStoreSystemException {
         String result = null;
         Connection connection = null;
         ResultSet resultSet = null;
         String query = null;
         try {
             final String table = getTableName(fullQualifiedPropertyName);
             if (table == null) {
                 return result;
             }
             final String querySelect = "SELECT " + table + ".o";
             final String queryFrom = " FROM " + table;
             final String queryWhere =
                 " WHERE " + '(' + table + ".s = '" + new URIReference(Constants.IDENTIFIER_PREFIX + pid).toString()
                     + "')";
 
             query = querySelect + queryFrom + queryWhere;
             connection = getConnection();
             resultSet = connection.prepareStatement(query).executeQuery();
             if (resultSet.next()) {
                 result = getValue(resultSet.getString(1));
             }
         }
         catch (final URISyntaxException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (final CannotGetJdbcConnectionException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (final SQLException e) {
             throw new TripleStoreSystemException("Failed to execute query " + query, e);
         }
         catch (final SystemException e) {
             throw new TripleStoreSystemException("Failed to escape forbidden xml characters ", e);
         }
         finally {
             if (connection != null) {
                 releaseConnection(connection);
             }
             IOUtils.closeResultSet(resultSet);
         }
         return result;
     }
 
     /**
      *
      * @param objectType
      * @param filterMap
      * @param whereClause
      * @throws de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException
      * @return
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     public List<String> evaluate(final String objectType, final Map<String, Object> filterMap, final String whereClause)
         throws MissingMethodParameterException, TripleStoreSystemException, IntegritySystemException {
 
         return evaluate(objectType, filterMap, null, whereClause);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility#evaluate (java.lang.String,
      * java.lang.String)
      */
     @Override
     public List<String> evaluate(
         final String objectType, final Map<String, Object> filterMap, final String additionalConditionTriple,
         final String whereClause) throws MissingMethodParameterException, TripleStoreSystemException,
         IntegritySystemException {
 
         final Map filter = (Map) filterMap.get("filter");
 
         // String objectsToFind = objectType + "s";
         // String query = "select $object from <#ri> "
         // + "where $object
         // <http://www.nsdl.org/ontologies/relationships/objectType> '"
         // + objectType + "' ";
         final String tableWithObjectType = getTableName(PROP_OBJECT_TYPE);
         if (tableWithObjectType == null) {
             return new ArrayList<String>();
         }
         final String objectTypeColumn = tableWithObjectType + ".o";
         final StringBuilder queryResultBuf = new StringBuilder();
         final StringBuilder querySelectPartBuf = new StringBuilder();
         final StringBuilder queryWherePart = new StringBuilder();
 
         final String tableWithIdentifier = getTableName("http://purl.org/dc/elements/1.1/identifier");
         if (tableWithIdentifier == null) {
             return new ArrayList<String>();
         }
         final String idColumn = tableWithIdentifier + ".o";
         querySelectPartBuf.append("SELECT ");
         querySelectPartBuf.append(tableWithObjectType);
         querySelectPartBuf.append(".s FROM ");
         queryResultBuf.append(tableWithObjectType);
         // queryResultBuf.append(" WHERE ");
 
         if ("member".equalsIgnoreCase(objectType)) {
             if (filter != null && filter.containsKey(PROP_OBJECT_TYPE)) {
                 final String queryPartObjectTypeMember =
                     objectTypeColumn + '=' + "\'<" + filter.remove(PROP_OBJECT_TYPE) + ">\'";
                 queryWherePart.append(queryPartObjectTypeMember);
             }
             else {
                 final String queryPartObjectTypeMember =
                     '(' + objectTypeColumn + '=' + "\'<http://escidoc.de/core/01/resources/Item>\' OR "
                         + objectTypeColumn + '=' + "\'<http://escidoc.de/core/01/resources/Container>\')";
                 queryWherePart.append(queryPartObjectTypeMember);
             }
         }
         else {
             final String queryPartObjectType = objectTypeColumn + '=' + "\'<" + objectType + ">\'";
             queryWherePart.append(queryPartObjectType);
         }
         boolean first = true;
         if (additionalConditionTriple != null) {
             first = false;
             final String[] tripleParts = SPLIT_PATTERN.split(additionalConditionTriple);
             if (tripleParts.length != 3) {
                 throw new IntegritySystemException("Wrong triple");
             }
             boolean inverse = false;
             if (tripleParts[0].startsWith("<")) {
                 inverse = true;
             }
             final int length = tripleParts[1].length();
             tripleParts[1] = tripleParts[1].substring(1, length - 1);
             final String additionalTableName = getTableName(tripleParts[1]);
             if (additionalTableName == null) {
                 return new ArrayList<String>();
             }
             queryResultBuf.append(" INNER JOIN ");
             queryResultBuf.append(additionalTableName);
             queryResultBuf.append(" ON ");
             queryResultBuf.append(additionalTableName);
             if (inverse) {
                 queryResultBuf.append(".o=");
             }
             else {
                 queryResultBuf.append(".s=");
             }
             queryResultBuf.append(tableWithObjectType);
             queryResultBuf.append(".s");
             queryWherePart.append(" AND ");
             queryWherePart.append(additionalTableName);
             if (inverse) {
                 queryWherePart.append(".s=");
                 queryWherePart.append('\'').append(tripleParts[0]).append('\'');
             }
             else {
                 queryWherePart.append(".o=");
                 queryWherePart.append('\'').append(tripleParts[2]).append('\'');
             }
         }
         final String queryResult;
         if (filter == null) {
             queryResultBuf.append(" WHERE ");
             queryResultBuf.append(queryWherePart);
             querySelectPartBuf.append(queryResultBuf);
             queryResult = querySelectPartBuf.toString();
 
         }
         else {
             // stored for later use
             final String roleCriteria = (String) filter.remove("role");
             final String userCriteria = (String) filter.remove("user");
             if (userCriteria == null) {
                 if (roleCriteria != null) {
                     throw new MissingMethodParameterException("If role criteria is used, user id must be specified");
                 }
             }
             else {
                 // try {
                 // whereClause =
                 // getPdp().getRoleUserWhereClauseMPT(objectType,
                 // userCriteria, roleCriteria);
                 if (whereClause == null) {
                     return new ArrayList<String>(0);
                 }
 
                 if (whereClause.length() > 0) {
                     if (first) {
                         queryResultBuf.append(" INNER JOIN (");
                     }
                     else {
                         queryResultBuf.insert(0, '(');
                         queryResultBuf.append(") INNER JOIN (");
                     }
                     queryResultBuf.append(whereClause);
                     queryResultBuf.append(") unionTable ON unionTable.temp=");
                     queryResultBuf.append(tableWithObjectType);
                     queryResultBuf.append(".s");
                 }
                 // }
                 // catch (final SystemException e) {
                 // // FIXME: throw SystemException?
                 // throw new TripleStoreSystemException(
                 // "Failed to retrieve clause for user and role criteria",
                 // e);
                 // }
             }
 
             final String filterCriteria =
                 getQueryPartId(idColumn, (Set<String>) filter.remove(Constants.DC_IDENTIFIER_URI));
             if (filterCriteria.length() != 0) {
                 if (first) {
 
                     queryResultBuf.append(" INNER JOIN ");
                 }
                 else {
                     queryResultBuf.insert(0, '(');
                     queryResultBuf.append(") INNER JOIN ");
                 }
                 queryResultBuf.append(tableWithIdentifier);
                 queryResultBuf.append(" ON ");
                 queryResultBuf.append(tableWithIdentifier);
                 queryResultBuf.append(".s=");
                 queryResultBuf.append(tableWithObjectType);
                 queryResultBuf.append(".s");
 
                 queryWherePart.append(" AND (");
                 queryWherePart.append(filterCriteria);
                 queryWherePart.append(')');
             }
 
             final String topLevelOus = (String) filter.remove("top-level-organizational-units");
 
             if (!filter.isEmpty()) {
                 final String[] joinPartProperties = getJoinPartProperties(filter, first);
                 if (joinPartProperties == null) {
 
                     return new ArrayList<String>();
                 }
                 else {
                     queryResultBuf.insert(0, joinPartProperties[0]);
                     queryResultBuf.append(joinPartProperties[1]);
                     queryWherePart.append(getWherePartProperties(filter));
                 }
             }
 
             if (topLevelOus != null) {
                 // shouldn't have a parent
                 final String tableWithParents = getTableName(Constants.STRUCTURAL_RELATIONS_NS_URI + "parent");
                 if (tableWithParents != null) {
                     queryWherePart.append(" AND (");
                     queryWherePart.append(tableWithObjectType);
                     queryWherePart.append(".s NOT IN (SELECT ");
                     queryWherePart.append(tableWithParents);
                     queryWherePart.append(".s FROM ");
                     queryWherePart.append(tableWithParents);
                     queryWherePart.append("))");
                 }
 
             }
             queryResultBuf.append(" WHERE ");
 
             queryResultBuf.append(queryWherePart);
 
             querySelectPartBuf.append(queryResultBuf);
             queryResult = querySelectPartBuf.toString();
         }
         if (!checkQuery(queryResult)) {
             return new ArrayList<String>();
         }
         return executeSqlQuery(queryResult);
 
     }
 
     /**
      *
      * @param filters
      * @param begin
      * @return
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     private String[] getJoinPartProperties(final Map<String, String> filters, final boolean begin)
         throws TripleStoreSystemException {
         final StringBuilder queryPartBuffer = new StringBuilder();
 
         final Iterator<String> it = filters.keySet().iterator();
         String tableWithOldPredicate = null;
         final String tableWithObjectType = getTableName(PROP_OBJECT_TYPE);
         boolean first = true;
         final StringBuilder openBracesBuffer = new StringBuilder();
         while (it.hasNext()) {
             final String predicate = it.next();
             final String tableWithPredicate = getTableName(predicate);
             if (tableWithPredicate == null) {
                 return null;
             }
             if (begin && first) {
                 queryPartBuffer.append(" INNER JOIN ");
             }
             else {
                 queryPartBuffer.append(") INNER JOIN ");
             }
             queryPartBuffer.append(tableWithPredicate);
             queryPartBuffer.append(" ON ");
             if (first) {
                 queryPartBuffer.append(tableWithObjectType);
                 queryPartBuffer.append(".s=");
             }
             else {
                 queryPartBuffer.append(tableWithOldPredicate);
                 queryPartBuffer.append(".s=");
             }
             queryPartBuffer.append(tableWithPredicate);
             queryPartBuffer.append(".s");
             // queryPartBuffer.append(")");
             if (!begin || !first) {
                 openBracesBuffer.append('(');
             }
 
             tableWithOldPredicate = tableWithPredicate;
             if (first) {
                 first = false;
             }
         }
         final String openBraces = openBracesBuffer.toString();
         final String queryPart = queryPartBuffer.toString();
         final String[] result = new String[2];
         result[0] = openBraces;
         result[1] = queryPart;
         return result;
     }
 
     private String getWherePartProperties(final Map<String, String> filters) throws TripleStoreSystemException {
         if (filters.isEmpty()) {
             // just provide NO query part if there are no predicates properties
             return "";
         }
 
         final StringBuilder queryPart = new StringBuilder();
         final Set<Entry<String, String>> filtersEntrySet = filters.entrySet();
         for (final Entry<String, String> entry : filtersEntrySet) {
             final String predicate = entry.getKey();
             final String tableWithPredicate = getTableName(predicate);
             if (tableWithPredicate == null) {
                 return null;
             }
             final String val = entry.getValue();
 
             // make URIs from given IDs or HREFs for all structural-relation
             // predicates
             final String object;
             if (predicate.startsWith(Constants.STRUCTURAL_RELATIONS_NS_URI)) {
                 String id = val;
                 if (val.startsWith("http://") || val.startsWith("/")) {
                     id = Utility.getId(val);
                 }
                 object = "\'<info:fedora/" + id + ">\'";
             }
             else {
                 object = "\'\"" + val + "\"\'";
             }
             queryPart.append(" AND ");
             queryPart.append(tableWithPredicate);
             queryPart.append(".o=");
             queryPart.append(object);
 
         }
         return queryPart.toString();
     }
 
     /**
      *
      * @param columnName
      * @param objects
      * @return
      */
     private static String getQueryPartId(final String columnName, final Set<String> objects) {
 
         final StringBuilder queryPart = new StringBuilder();
         String queryPartString = "";
 
         // TODO or rule for every id
         if (objects != null && !objects.isEmpty()) {
             final Iterator<String> it = objects.iterator();
 
             while (it.hasNext()) {
                 final String id = it.next();
 
                 queryPart.append(columnName).append('=' + "\'\"").append(id).append("\"\'");
                 if (it.hasNext()) {
                     queryPart.append(" OR ");
                 }
             }
             queryPartString = queryPart.toString();
         }
 
         return queryPartString;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility# getMemberList(java.lang.String)
      */
     @Override
     public List<String> getMemberList(final String id, final String whereClause) throws TripleStoreSystemException {
 
         // TODO the return type of List should be uniform
 
         final String tableWithMembers = getTableName(Constants.STRUCTURAL_RELATIONS_NS_URI + "member");
 
         if (tableWithMembers == null) {
             return new LinkedList<String>();
         }
 
         final StringBuilder queryResultBuf = new StringBuilder();
 
         queryResultBuf.append("SELECT ");
         queryResultBuf.append(tableWithMembers);
         queryResultBuf.append(".o FROM ");
         queryResultBuf.append(tableWithMembers);
         queryResultBuf.append(" WHERE ");
         queryResultBuf.append(tableWithMembers);
         queryResultBuf.append(".s='<info:fedora/").append(id).append(">'");
         final String queryResult = queryResultBuf.toString();
 
         if (!checkQuery(queryResult)) {
             return new ArrayList<String>();
         }
         return executeSqlQuery(queryResult);
     }
 
     /**
      * Get list of Container member. These member list is not filtered for user permission! (because we have to keep out
      * AA components from common package).
      */
     @Override
     public List<String> getContainerMemberList(
         final String containerId, final Map<String, Object> filterMap, final String whereClause)
         throws MissingMethodParameterException, TripleStoreSystemException {
 
         final String tableWithMembers = getTableName(Constants.STRUCTURAL_RELATIONS_NS_URI + "member");
         final StringBuffer queryResultBuf;
         final String queryResult;
 
         if (filterMap == null) {
             // prepare statement for empty filter
             queryResultBuf = new StringBuffer();
             queryResultBuf.append("SELECT ");
             queryResultBuf.append(tableWithMembers);
             queryResultBuf.append(".o FROM ");
             queryResultBuf.append(tableWithMembers);
             queryResultBuf.append(" WHERE ");
             queryResultBuf.append(tableWithMembers);
             queryResultBuf.append(".s='<info:fedora/").append(containerId).append(">'");
             queryResult = queryResultBuf.toString();
 
         }
         else {
 
             String idColumn = null;
             final String filterCriteria = getQueryPartId(idColumn, (Set<String>) filterMap.remove("members"));
             String tableWithIdentifier = getTableName("http://purl.org/dc/elements/1.1/identifier");
             idColumn = tableWithIdentifier + ".o";
             // object type filter TODO now, we only have items
             // String type = (String) filter.remove("type");
             final String objectType = (String) filterMap.remove("object-type");
             // String tableWithObjectType =
             // getTableName("http://www.nsdl.org/ontologies/relationships/objectType");
             // String objectTypeColumn = tableWithObjectType + ".o";
             // String queryPartObjectType =
             // objectTypeColumn + "=" + "\'\"" + objectType + "\"\'";
             if (objectType == null) {
                 // generic
                 final StringBuilder queryPartPropertiesBuffer = new StringBuilder();
                 final StringBuilder queryPartJoinPropertiesBuffer = new StringBuilder();
                 String tablenameFirstInChain = null;
                 String tableNameFirst = null;
                 final Collection<String> tableNames = new ArrayList<String>();
                 int i = 0;
                 // Vector<String> columnNames = new Vector<String>();
                 final Set<Entry<String, Object>> filterEntrySet = filterMap.entrySet();
                 for (final Entry<String, Object> entry : filterEntrySet) {
                     String key = entry.getKey();
                     String val = (String) entry.getValue();
                     val = MPTStringUtil.escapeLiteralValueForSql(val);
                     if ("context-type".equals(key)) {
                         key = "type";
                     }
                     // val may be href, need id
                     if (val.startsWith("http://") || val.startsWith("/")) {
                         val = val.substring(val.lastIndexOf('/'));
                     }
                     final String propertiesPredicateItem = Constants.ITEM_PROPERTIES_NAMESPACE_URI + '/' + key;
                     final String propertiesPredicateContainer = "http://www.escidoc.de/schemas/container/0.1/" + key;
                     final String tableNameItem = getTableName(propertiesPredicateItem);
                     final String tableNameContainer = getTableName(propertiesPredicateContainer);
                     final String tableNameNext = tableNameItem + tableNameContainer;
                     if (tableNameFirst == null) {
                         tablenameFirstInChain = tableNameNext;
                     }
                     tableNames.add(tableNameItem + tableNameContainer);
 
                     final String columnObjectItem = tableNameItem + ".o";
                     final String columnObjectContainer = tableNameContainer + ".o";
                     final String columnSubjectItem = tableNameItem + ".s";
                     final String columnSubjectContainer = tableNameContainer + ".s";
                     // columnNames.add(columnName);
                     // query += "and $object <http://www.escidoc.de/schemas/"
                     // + objectType + "/0.1/" + key + "> '" + val + "' ";
 
                     queryPartPropertiesBuffer.append("(SELECT ");
                     queryPartPropertiesBuffer.append(columnSubjectItem);
                     queryPartPropertiesBuffer.append(" FROM ");
                     queryPartPropertiesBuffer.append(tableNameItem);
                     queryPartPropertiesBuffer.append(" WHERE ");
                     if ("context".equals(key) || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
                         queryPartPropertiesBuffer
                             .append(columnObjectItem).append("='<info:fedora/").append(val).append(">'");
                     }
                     else {
                         queryPartPropertiesBuffer.append(columnObjectItem).append("=\'\"").append(val).append("\"\'");
                     }
                     queryPartPropertiesBuffer.append(" UNION ");
                     queryPartPropertiesBuffer.append(" SELECT ");
                     queryPartPropertiesBuffer.append(columnSubjectContainer);
                     queryPartPropertiesBuffer.append(" FROM ");
                     queryPartPropertiesBuffer.append(tableNameContainer);
                     queryPartPropertiesBuffer.append(" WHERE ");
                     if ("context".equals(key) || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
                         queryPartPropertiesBuffer
                             .append(columnObjectContainer).append("='<info:fedora/").append(val).append(">'");
                     }
                     else {
                         queryPartPropertiesBuffer.append(columnObjectContainer).append("=\'\"").append(val).append(
                             "\"\'");
                     }
                     queryPartPropertiesBuffer.append(") ");
                     queryPartPropertiesBuffer.append(tableNameNext);
                     if (tableNameFirst != null) {
                         queryPartJoinPropertiesBuffer
                             .append(tableNameFirst).append(".s=").append(tableNameNext).append(".s");
                     }
                     i++;
                     if (i != filterEntrySet.size()) {
                         queryPartPropertiesBuffer.append(", ");
                         queryPartJoinPropertiesBuffer.append(" AND ");
 
                     }
                     tableNameFirst = tableNameNext;
 
                 }
                 String queryPartProperties = "";
                 String queryPartJoinProperties = "";
                 String queryPartJoinMembersAndProperties = "";
 
                 if (!tableNames.isEmpty()) {
                     queryPartProperties = queryPartPropertiesBuffer.toString();
                     queryPartJoinProperties = queryPartJoinPropertiesBuffer.toString();
                     queryPartJoinMembersAndProperties = tablenameFirstInChain + ".s=" + tableWithMembers + ".o";
                 }
                 tableWithIdentifier = getTableName("http://purl.org/dc/elements/1.1/identifier");
                 idColumn = tableWithIdentifier + ".o";
 
                 queryResultBuf = new StringBuffer();
                 queryResultBuf.append("SELECT ");
                 queryResultBuf.append(tableWithMembers);
                 queryResultBuf.append(".o FROM ");
                 queryResultBuf.append(tableWithMembers);
                 // queryResultBuf.append("SELECT ");
                 // queryResultBuf.append(tableWithObjectType);
                 // queryResultBuf.append(".s FROM ");
 
                 String joinIdentifierAndMembers = "";
                 if (filterCriteria.length() != 0) {
                     queryResultBuf.append(tableWithIdentifier);
                     queryResultBuf.append(',');
                     joinIdentifierAndMembers = tableWithIdentifier + ".s=" + tableWithMembers + ".o AND ";
                 }
                 // queryResultBuf.append(tableWithObjectType);
                 if (!tableNames.isEmpty()) {
                     queryResultBuf.append(',');
                     queryResultBuf.append(queryPartProperties);
 
                 }
                 queryResultBuf.append(" WHERE ");
 
                 queryResultBuf.append(joinIdentifierAndMembers);
 
                 queryResultBuf.append(queryPartJoinMembersAndProperties);
                 queryResultBuf.append(queryPartJoinProperties);
                 if (queryPartJoinMembersAndProperties.length() != 0) {
                     queryResultBuf.append(" AND ");
                 }
                 queryResultBuf.append(tableWithMembers);
                 queryResultBuf.append(".s='<info:fedora/").append(containerId).append(">'");
                 if (filterCriteria.length() != 0) {
                     queryResultBuf.append(" AND (");
                     queryResultBuf.append(filterCriteria);
                     queryResultBuf.append(')');
                 }
                 queryResult = queryResultBuf.toString();
             }
             else {
                 final StringBuilder queryPartPropertiesBuffer = new StringBuilder();
                 final StringBuilder queryPartJoinPropertiesBuffer = new StringBuilder();
                 final Iterator<String> it = filterMap.keySet().iterator();
                 String tablenameFirstInChain = null;
                 String tableNameFirst = null;
                 final Collection<String> tableNames = new ArrayList<String>();
                 final Set<Entry<String, Object>> filterEntrySet = filterMap.entrySet();
                 for (final Entry<String, Object> entry : filterEntrySet) {
                     String key = entry.getKey();
                     String val = (String) entry.getValue();
                     val = MPTStringUtil.escapeLiteralValueForSql(val);
                     if ("context-type".equals(key)) {
                         key = "type";
                     }
                     // val may be href, need id
                     if (val.startsWith("http://") || val.startsWith("/")) {
                         val = val.substring(val.lastIndexOf('/'));
                     }
                     final String propertiesPredicate = "http://www.escidoc.de/schemas/" + objectType + "/0.1/" + key;
                     final String tableNameNext = getTableName(propertiesPredicate);
                     if (tableNameFirst == null) {
                         tablenameFirstInChain = tableNameNext;
                     }
                     tableNames.add(tableNameNext);
                     final String columnName = tableNameNext + ".o";
                     // columnNames.add(columnName);
                     // query += "and $object <http://www.escidoc.de/schemas/"
                     // + objectType + "/0.1/" + key + "> '" + val + "' ";
                     if ("context".equals(key) || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
                         queryPartPropertiesBuffer.append(columnName).append("='<info:fedora/").append(val).append(">'");
                     }
                     else {
                         queryPartPropertiesBuffer.append(columnName).append("=\'\"").append(val).append("\"\'");
                     }
                     if (tableNameFirst != null) {
                         queryPartJoinPropertiesBuffer
                             .append(tableNameFirst).append(".s=").append(tableNameNext).append(".s");
                     }
 
                     if (it.hasNext()) {
                         queryPartPropertiesBuffer.append(" AND ");
 
                         queryPartJoinPropertiesBuffer.append(" AND ");
 
                     }
                     tableNameFirst = tableNameNext;
                 }
                 String queryPartProperties = "";
                 String queryPartJoinProperties = "";
                 String queryPartJoinMembersAndProperties = "";
 
                 if (!tableNames.isEmpty()) {
                     queryPartProperties = queryPartPropertiesBuffer.toString();
                     queryPartJoinProperties = queryPartJoinPropertiesBuffer.toString();
                     queryPartJoinMembersAndProperties = tablenameFirstInChain + ".s=" + tableWithMembers + ".o";
                 }
                 tableWithIdentifier = getTableName("http://purl.org/dc/elements/1.1/identifier");
                 idColumn = tableWithIdentifier + ".o";
 
                 queryResultBuf = new StringBuffer();
                 queryResultBuf.append("SELECT ");
                 queryResultBuf.append(tableWithMembers);
                 queryResultBuf.append(".o FROM ");
 
                 String joinIdentifierAndMembers = "";
                 if (filterCriteria.length() != 0) {
                     queryResultBuf.append(tableWithIdentifier);
                     queryResultBuf.append(',');
                     joinIdentifierAndMembers = tableWithIdentifier + ".s=" + tableWithMembers + ".o AND ";
                 }
                 queryResultBuf.append(tableWithMembers);
                 if (!tableNames.isEmpty()) {
                     for (final String tableName : tableNames) {
                         queryResultBuf.append(',');
                         queryResultBuf.append(tableName);
                     }
 
                 }
                 queryResultBuf.append(" WHERE ");
 
                 queryResultBuf.append(joinIdentifierAndMembers);
 
                 queryResultBuf.append(queryPartJoinMembersAndProperties);
                 queryResultBuf.append(queryPartJoinProperties);
                 if (queryPartJoinMembersAndProperties.length() != 0) {
                     queryResultBuf.append(" AND ");
                 }
                 queryResultBuf.append(tableWithMembers);
                 queryResultBuf.append(".s='<info:fedora/").append(containerId).append(">'");
                 if (queryPartProperties.length() != 0) {
                     queryResultBuf.append(" AND ");
                     queryResultBuf.append(queryPartProperties);
                 }
 
                 if (filterCriteria.length() != 0) {
                     queryResultBuf.append(" AND (");
                     queryResultBuf.append(filterCriteria);
                     queryResultBuf.append(')');
                 }
                 queryResult = queryResultBuf.toString();
             }
         }
 
         if (!checkQuery(queryResult)) {
             return new ArrayList<String>();
         }
 
         return executeSqlQuery(queryResult);
     }
 
     /**
      *
      * @param query
      * @return
      */
     private boolean checkQuery(final String query) {
         boolean result = false;
         if (query != null && !query.contains("null.")) {
             result = true;
         }
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility# getContextMemberList(java.lang.String,
      * java.lang.String)
      */
 
     @Override
     public List<String> getContextMemberList(
         final String contextId, final Map<String, Object> filterMap, final String whereClause)
         throws MissingMethodParameterException, TripleStoreSystemException, IntegritySystemException {
         // TODO check functionality
         return evaluate("member", filterMap, "* <" + Constants.STRUCTURAL_RELATIONS_NS_URI + "context> <info:fedora/"
             + contextId + '>');
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility# getObjectRefs(java.lang.String,
      * java.lang.String)
      */
     @Override
     public String getObjectRefs(final String objectType, final Map<String, Object> filterMap, final String whereClause)
         throws MissingMethodParameterException, WebserverSystemException, TripleStoreSystemException,
         IntegritySystemException {
 
         final List<String> list = evaluate(objectType, filterMap, whereClause);
 
         String absoluteLocalPathFirstPart = "ir";
         if (objectType.equals(Elements.ELEMENT_CONTENT_MODEL)) {
             absoluteLocalPathFirstPart = "ctm";
         }
 
         final String namespacePrefix = objectType + "-ref-list";
         String schemaVersion = "0.2";
         if ("item".equals(objectType)) {
             schemaVersion = "0.3";
         }
         final String namespaceUri = "http://www.escidoc.de/schemas/" + objectType + "reflist/" + schemaVersion;
         final String rootElementName = objectType + "-ref-list";
         final String listElementName = objectType + "-ref";
 
         final String prefixedRootElement = namespacePrefix + ':' + rootElementName;
         final String prefixedListElement = namespacePrefix + ':' + listElementName;
 
         final String namespaceDecl = " xmlns:" + namespacePrefix + "=\"" + namespaceUri + "\" ";
 
         final StringBuilder sb = new StringBuilder();
 
         sb.append('<');
         sb.append(prefixedRootElement);
 
         sb.append(namespaceDecl);
         sb.append(" xlink:title=\"list of ");
         sb.append(objectType);
         sb.append(" references\" xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
         sb.append(" xml:base=\"");
         sb.append(XmlUtility.getEscidocBaseUrl()).append('\"');
         sb.append('>');
 
         for (final String id : list) {
             sb.append('<');
             sb.append(prefixedListElement);
             sb.append(" xlink:href=\"/");
             sb.append(absoluteLocalPathFirstPart);
             sb.append('/');
             sb.append(objectType);
             sb.append('/');
             sb.append(id);
             sb.append("\" xlink:type=\"simple\"");
             sb.append(" />");
         }
         sb.append("</");
         sb.append(prefixedRootElement);
         sb.append('>');
         return sb.toString();
     }
 
     /**
      * Get name of table for predicate.
      *
      * @param predicate Predicate (from SPO).
      * @return name of table where predicate name is used.
      * @throws TripleStoreSystemException Thrown if request of TripleStore failed.
      */
     public String getTableName(final String predicate) throws TripleStoreSystemException {
         String result = null;
         if (predicate != null) {
             try {
                 final URIReference predicateNode = new URIReference(predicate);
 
                 result = getTableManager().getTableFor(predicateNode);
                 if (result == null) {
                     reinitialize();
                     result = getTableManager().getTableFor(predicateNode);
                 }
             }
             catch (final URISyntaxException e) {
                 throw new TripleStoreSystemException(e);
             }
         }
         return result;
     }
 
     /**
      * @param pid                       The Id of the object.
      * @param fullqualifiedPropertyName The full qualified property name.
      * @return Value of property element.
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     @Override
     public String getPropertiesElements(final String pid, final String fullqualifiedPropertyName)
         throws TripleStoreSystemException {
 
         // TODO refactor to getPropertiesElement
 
         String value = null;
         final List<String> results = executeQueryId(pid, false, fullqualifiedPropertyName);
 
         // work around for more than one dc:identifier
         for (final String result : results) {
             value = result;
             if (!"http://purl.org/dc/elements/1.1/identifier".equals(fullqualifiedPropertyName) || pid.equals(value)) {
                 break;
             }
         }
         return value;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility# reinitialize()
      */
     @Override
     public void reinitialize() throws TripleStoreSystemException {
         setUpTableManager();
     }
 
     /**
      * Sets up the table manager.<br> The table manager is created in order to get the current table mappings.
      *
      * @return Returns the created table manager.
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     private TableManager setUpTableManager() throws TripleStoreSystemException {
 
         final TableManager result;
         try {
             result = new BasicTableManager(getDataSource(), getDdlGenerator(), "tMap", "t");
         }
         catch (final SQLException e1) {
             throw new TripleStoreSystemException(e1.getMessage(), e1);
         }
         setTableManager(result);
         return result;
     }
 
     /**
      * Determine the type of relational database the system is using.
      *
      * @return current DatabaseType
      * @throws TripleStoreSystemException If access to the database fails.
      */
     private DatabaseType getDatabaseType() throws TripleStoreSystemException {
         if (databaseType == null) {
             Connection con = null;
             try {
                 con = getConnection();
                 databaseType = DatabaseType.valueOf(con);
             }
             catch (final CannotGetJdbcConnectionException e) {
                 throw new TripleStoreSystemException("Failed to get JDBC connection.", e);
             }
             catch (final SQLException e) {
                 throw new TripleStoreSystemException("Failed to get database metadata ", e);
             }
             finally {
                 if (con != null) {
                     releaseConnection(con);
                 }
             }
         }
         return databaseType;
     }
 
     /**
      * Gets the database-dependent configured ddl-generator for the triplestore.
      *
      * @return DDLGenerator.
      * @throws TripleStoreSystemException If instanciation fails.
      */
     private DDLGenerator getDdlGenerator() throws TripleStoreSystemException {
 
         try {
             final String ddlGenerator =
                 EscidocConfiguration.getInstance().get(EscidocConfiguration.TRIPLESTORE_DDL_GENERATOR);
             return (DDLGenerator) Class.forName(ddlGenerator).newInstance();
         }
         catch (InstantiationException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (IllegalAccessException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (ClassNotFoundException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
     }
 
     /**
      * If entry is an URI Identifier, the method extracts the contained id, if the entry is a literal value, the method
      * removes the leading and trailing quote (").
      *
      * @param entry The entry (result of a triplestore query)
      * @return The result as described above.
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     private String getValue(final String entry) throws TripleStoreSystemException {
         String result = null;
         try {
             if (entry != null) {
                 result = NTriplesUtil.unescapeLiteralValue(entry);
                 if (result != null) {
                     if (result.startsWith("<info") || result.startsWith("info")) {
                         result = XmlUtility.getIdFromURI(result);
                     }
                     else if (result.startsWith("<") && result.endsWith(">")) {
                         result = result.substring(1, result.length() - 1);
                     }
                     else if (result.startsWith("\"")) {
                         result = result.substring(1, result.lastIndexOf('\"'));
                     }
                     result = XmlUtility.escapeForbiddenXmlCharacters(result);
                 }
             }
         }
         catch (final ParseException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         return result;
     }
 
     /**
      * Excecute query on TripleStore.
      *
      * @param query TripleStore query
      * @return result list for request.
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
     public List<String> executeSqlQuery(final String query) throws TripleStoreSystemException {
 
         final List<String> result = new LinkedList<String>();
         Connection con = null;
         ResultSet rs = null;
         try {
             con = getConnection();
             rs = con.prepareStatement(query).executeQuery();
             while (rs.next()) {
                 final String entry = getValue(rs.getString(1));
                 result.add(entry);
             }
         }
         catch (final CannotGetJdbcConnectionException e) {
             throw new TripleStoreSystemException("Failed to get JDBC connection.", e);
         }
         catch (final SQLException e) {
             throw new TripleStoreSystemException("Failed to execute query " + query, e);
         }
         finally {
             if (con != null) {
                 releaseConnection(con);
             }
             IOUtils.closeResultSet(rs);
         }
         return result;
 
     }
 
     /**
      * @return the tableManager
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     public TableManager getTableManager() throws TripleStoreSystemException {
         if (this.tableManager == null) {
             setUpTableManager();
         }
         return this.tableManager;
     }
 
     /**
      * @param tableManager the tableManager to set
      */
     public void setTableManager(final TableManager tableManager) {
         this.tableManager = tableManager;
     }
 
     @Override
     public String getObjectList(final String objectType, final Map filter, final String whereClause) {
 
         throw new UnsupportedOperationException("Not implemented for MPTTripleStore");
     }
 
     /**
      * Builds the starting clause of a query to the triple store to retrieve objects.
      *
      * @param targetIsSubject targetIsSubject
      * @return Returns the starting clause "SELECT PREDICATE_TABLE.S FROM " in a {@link StringBuffer}
      */
     @Override
     public StringBuffer getRetrieveSelectClause(final boolean targetIsSubject, final String predicateId)
         throws TripleStoreSystemException {
         final StringBuffer result = new StringBuffer();
 
         if (predicateId == null) {
             throw new TripleStoreSystemException("predicate must not be null");
         }
 
         // Initialize select clause
         final String predicateTable = getTableName(predicateId);
 
         if (predicateTable != null) {
             result.append("SELECT ");
             result.append(predicateTable);
             if (targetIsSubject) {
                 result.append(".s AS temp FROM ");
             }
             else {
                 result.append(".o AS temp FROM ");
             }
         }
         return result;
     }
 
     /**
      * Test id a resource with provided id exists.
      *
      * @param pid Fedora objid.
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     @Override
     @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
     public boolean exists(final String pid) throws TripleStoreSystemException {
         Connection connection = null;
         ResultSet resultSet = null;
         String query = null;
         try {
             final String table = getTableName(FEDORA_CREATION_DATE_PREDICATE);
             if (table == null) {
                 return false;
             }
             final StringBuilder queryBuffer = new StringBuilder();
             queryBuffer.append("SELECT ");
             queryBuffer.append(table);
             queryBuffer.append(".o FROM ");
             queryBuffer.append(table);
             queryBuffer.append(" WHERE (");
             queryBuffer.append(table);
             queryBuffer.append(" .s = '");
             queryBuffer.append(new URIReference(Constants.IDENTIFIER_PREFIX + pid).toString());
             queryBuffer.append("')");
             query = queryBuffer.toString();
             connection = getConnection();
 
             resultSet = connection.prepareStatement(query).executeQuery();
             return resultSet.next();
         }
         catch (final URISyntaxException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (final CannotGetJdbcConnectionException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (final SQLException e) {
             throw new TripleStoreSystemException("Failed to execute query " + query, e);
         }
         catch (final SystemException e) {
             throw new TripleStoreSystemException("Failed to escape forbidden xml characters ", e);
         }
         finally {
             if (connection != null) {
                 releaseConnection(connection);
             }
             IOUtils.closeResultSet(resultSet);
         }
 
     }
 
     /**
      * Builds the starting clause of a query to the triple store.
      *
      * @param targetIsSubject     Flag indicating that the target to search for is the subject ( {@code true}) or
      *                            the object ( {@code false}) of the specified predicate.
      * @param predicateId         The predicate id. If this equals to the id predicate (see de.escidoc.core.common.business.Constants.DC_IDENTIFIER_URI),
      *                            the provided value of the parameter {@code targetIsSubject} is ignored and it is
      *                            assumed it has been set to {@code true}.
      * @param expectedValue       The value that must be matched by the specified predicate. If
      *                            {@code targetIsSubject} is {@code true}, the object of the predicate must
      *                            match the value. Otherwise the subject must match the value.<br/> In case of the id
      *                            attribute, the expected value is ignored and $s is used for creating $s
      *                            &lt;predicate&gt; $s clause part.
      * @param targetResourceType  The object type of the target of the query. If this is {@code null}, no
      *                            restriction for expected resource type is added.
      * @param contentModelTitleId The id of the predicate pointing to the title of the content model. If this is
      *                            {@code null}, targets of any content model are searched.
      * @param contentModelTitle   The content model title that the subject must match. This must not be
      *                            {@code null}, if contentModelTitleId is not {@code null}.
      * @return Returns the where clause searching for the specified subjects.
      */
     @Override
     public StringBuffer getRetrieveWhereClause(
         final boolean targetIsSubject, final String predicateId, final String expectedValue,
         final String targetResourceType, final String contentModelTitleId, final String contentModelTitle)
         throws TripleStoreSystemException {
 
         final String creationDateTable = getTableName(FEDORA_CREATION_DATE_PREDICATE);
         // String dcIdentifierTable = getTableName(Constants.DC_IDENTIFIER_URI);
         final String contentModelTitleTableName = getTableName(PROP_TITLE);
         final String contentModelOfObjectTableName = getTableName(contentModelTitleId);
         final String tableWithObjectType = getTableName(PROP_OBJECT_TYPE);
         final String tableWithPredicate = getTableName(predicateId);
         final StringBuffer faultCase = new StringBuffer();
         if (tableWithPredicate != null) {
             faultCase.append(tableWithPredicate);
             faultCase.append(" WHERE ");
             faultCase.append(tableWithPredicate);
             faultCase.append(".s=\'\"bla\"\'");
         }
         else {
             faultCase.append(creationDateTable);
             faultCase.append(" WHERE ");
             faultCase.append(creationDateTable);
             faultCase.append(".s=\'\"bla\"\'");
             return faultCase;
         }
         final StringBuffer queryPart = new StringBuffer();
 
         final StringBuilder queryPartJoinObjectTypeWithPredicateBuffer = new StringBuilder();
         queryPartJoinObjectTypeWithPredicateBuffer.append(tableWithPredicate);
         queryPartJoinObjectTypeWithPredicateBuffer.append(" ON ");
         queryPartJoinObjectTypeWithPredicateBuffer.append(tableWithObjectType);
         queryPartJoinObjectTypeWithPredicateBuffer.append(".s=");
         queryPartJoinObjectTypeWithPredicateBuffer.append(tableWithPredicate);
         if (targetIsSubject) {
             queryPartJoinObjectTypeWithPredicateBuffer.append(".s");
         }
         else {
             queryPartJoinObjectTypeWithPredicateBuffer.append(".o");
         }
         final String queryPartJoinObjectTypeWithPredicate = queryPartJoinObjectTypeWithPredicateBuffer.toString();
 
         final StringBuilder queryPartJoinContentModelWithPredicateBuffer = new StringBuilder();
         queryPartJoinContentModelWithPredicateBuffer.append(tableWithPredicate);
         queryPartJoinContentModelWithPredicateBuffer.append(" ON ");
         queryPartJoinContentModelWithPredicateBuffer.append(contentModelOfObjectTableName);
         queryPartJoinContentModelWithPredicateBuffer.append(".s=");
         queryPartJoinContentModelWithPredicateBuffer.append(tableWithPredicate);
         if (targetIsSubject) {
             queryPartJoinContentModelWithPredicateBuffer.append(".s");
         }
         else {
             queryPartJoinContentModelWithPredicateBuffer.append(".o");
         }
         final String queryPartJoinContentModelWithPredicate = queryPartJoinContentModelWithPredicateBuffer.toString();
 
         boolean isFirst = true;
         if (contentModelTitleId != null && contentModelTitle != null) {
             if (contentModelTitleTableName == null || contentModelOfObjectTableName == null) {
                 return faultCase;
             }
             isFirst = false;
             queryPart.append('(');
             queryPart.append(contentModelOfObjectTableName);
             queryPart.append(" INNER JOIN ");
             queryPart.append(contentModelTitleTableName);
             queryPart.append(" ON ");
             queryPart.append(contentModelOfObjectTableName);
             queryPart.append(".o=");
             queryPart.append(contentModelTitleTableName);
             queryPart.append(".s)");
         }
         int braceToAddAtBeginn = 0;
         if (targetResourceType != null) {
             if (tableWithObjectType == null) {
                 return faultCase;
             }
             if (isFirst) {
                 isFirst = false;
                 queryPart.append(tableWithObjectType);
                 queryPart.append(" INNER JOIN ");
                 queryPart.append(queryPartJoinObjectTypeWithPredicate);
             }
             else {
                 braceToAddAtBeginn++;
                 queryPart.append(" INNER JOIN ");
                 queryPart.append(tableWithObjectType);
                 queryPart.append(" ON ");
                 queryPart.append(contentModelOfObjectTableName);
                 queryPart.append(".s=");
                 queryPart.append(tableWithObjectType);
                 queryPart.append(".s) INNER JOIN ");
                 queryPart.append(queryPartJoinObjectTypeWithPredicate);
 
             }
 
         }
         else {
             if (isFirst) {
                 queryPart.append(tableWithPredicate);
             }
             else {
                 queryPart.append(" INNER JOIN ");
                 queryPart.append(queryPartJoinContentModelWithPredicate);
             }
         }
         queryPart.append(" WHERE ");
         if (contentModelTitle != null) {
             final String contentModelTitleEscaped = MPTStringUtil.escapeLiteralValueForSql(contentModelTitle);
             queryPart.append(contentModelTitleTableName);
             queryPart.append(".o=");
             queryPart.append("\'\"").append(contentModelTitleEscaped).append("\"\' AND ");
         }
         if (targetResourceType != null) {
             if ("member".equals(targetResourceType)) {
                 queryPart.append('(');
                 queryPart.append(tableWithObjectType);
                 queryPart.append(".o=");
                 queryPart.append("'<" + Constants.ITEM_OBJECT_TYPE + ">' OR ");
                 queryPart.append(tableWithObjectType);
                 queryPart.append(".o=");
                 queryPart.append("'<" + Constants.CONTAINER_OBJECT_TYPE + ">') AND ");
             }
             else {
                 queryPart.append(tableWithObjectType);
                 queryPart.append(".o=");
                 queryPart.append("'<").append(targetResourceType).append(">' AND ");
             }
         }
         if (targetIsSubject) {
             queryPart.append(tableWithPredicate);
             // the query should return objid if a predicate is
             // TripleStoreUtility.Fedora_Creation_Date_Predicate,
             // therefore the dummy SQL-Query is using here:
             // select t.s from t where t.s='<info:fedora/escidoc:bla>';
             if (predicateId.equals(TripleStoreUtility.FEDORA_CREATION_DATE_PREDICATE)) {
                 queryPart.append(".s=");
                 queryPart.append("'<info:fedora/").append(expectedValue).append(">'");
             }
             else {
                 queryPart.append(".o=");
 
                 queryPart.append("'<info:fedora/").append(expectedValue).append(">'");
             }
 
         }
         else {
             queryPart.append(tableWithPredicate);
             queryPart.append(".s=");
             queryPart.append("'<info:fedora/").append(expectedValue).append(">'");
         }
 
         if (braceToAddAtBeginn == 1) {
             queryPart.insert(0, '(');
         }
 
         return queryPart;
     }
 
     /**
      * See Interface for functional description.
      *
      * @see TripleStoreUtility #retrieve(java.lang.String)
      */
     @Override
     public List<String> retrieve(final String query) throws TripleStoreSystemException {
         return executeSqlQuery(query);
     }
 
     /**
      * Get the context id of the context with the given name.
      *
      * @param name context name
      * @return context id or null, if no such context exists
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     @Override
     public String getContextForName(final String name) throws TripleStoreSystemException {
         String result = null;
         final String titleTableName = getTableName(PROP_DC_TITLE);
         final String typeTableName = getTableName(PROP_OBJECT_TYPE);
 
         if (titleTableName != null && typeTableName != null) {
             final StringBuffer select = new StringBuffer();
 
             select.append("SELECT ");
             select.append(titleTableName);
             select.append(".s FROM ");
             select.append(titleTableName);
             select.append(',');
             select.append(typeTableName);
             select.append(" WHERE ");
             select.append(titleTableName);
             select.append(".s = ");
             select.append(typeTableName);
             select.append(".s AND ");
             select.append(typeTableName);
             select.append(".o = '<");
             select.append(Constants.CONTEXT_OBJECT_TYPE);
             select.append(">' AND ");
             select.append(titleTableName);
             select.append(".o = '\"");
             select.append(MPTStringUtil.escapeLiteralValueForSql(name));
             select.append("\"'");
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             final List<String> res = executeSqlQuery(select.toString());
            if (!res.isEmpty()) {
                for (final String item : res) {
                    result = item;
                }
            }
             if (getLogger().isDebugEnabled()) {
                 if (res.isEmpty()) {
                     getLogger().debug("found no records");
                 }
                 else {
                     getLogger().debug("found " + res.size() + " records");
                     for (final String item : res) {
                         getLogger().debug("item: " + item);
                         result = item;
                     }
                 }
             }
         }
         return result;
     }
 
     /**
      * Get all child containers of the given container.
      *
      * @param id container id
      * @return id list of all child containers
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     @Override
     public List<String> getAllChildContainers(final String id) throws TripleStoreSystemException {
         List<String> result = null;
         final String memberTableName = getTableName(PROP_MEMBER);
         final String typeTableName = getTableName(PROP_OBJECT_TYPE);
 
         if (memberTableName != null && typeTableName != null) {
             final String select;
             final DatabaseType databaseType = getDatabaseType();
             if (databaseType == DatabaseType.POSTGRES) {
                 select =
                     MessageFormat.format("WITH RECURSIVE getChildContainers AS (SELECT {1}.s, {1}.o"
                         + " FROM {0}, {1} WHERE {0}.s={1}.o AND {0}.o=''<" + Constants.CONTAINER_OBJECT_TYPE
                         + ">'' AND {1}.s=''" + "<info:fedora/" + id + ">'' UNION SELECT {1}.s, {1}.o FROM {0}, {1}, "
                         + "getChildContainers WHERE {1}.s=" + "getChildContainers.o AND {0}.s={1}.o AND {0}.o=''<"
                         + Constants.CONTAINER_OBJECT_TYPE + ">'') SELECT o" + " FROM getChildContainers;",
                         typeTableName, memberTableName);
             }
             else if (databaseType == DatabaseType.ORACLE) {
                 select =
                     MessageFormat.format("WITH getChildContainers (s, o) AS (SELECT {1}.s, {1}.o"
                         + " FROM {0}, {1} WHERE {0}.s={1}.o AND {0}.o=''<" + Constants.CONTAINER_OBJECT_TYPE
                         + ">'' AND {1}.s=''" + "<info:fedora/" + id
                         + ">'' UNION ALL SELECT {1}.s, {1}.o FROM {0}, {1}, " + "getChildContainers WHERE {1}.s="
                         + "getChildContainers.o AND {0}.s={1}.o AND {0}.o=''<" + Constants.CONTAINER_OBJECT_TYPE
                         + ">'') SELECT o" + " FROM getChildContainers", typeTableName, memberTableName);
             }
             else {
                 throw new TripleStoreSystemException("This kind of relational database is not supported.");
             }
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             result = executeSqlQuery(select);
             if (getLogger().isDebugEnabled()) {
                 if (result.isEmpty()) {
                     getLogger().debug("found no records");
                 }
                 else {
                     getLogger().debug("found " + result.size() + " records");
                     getLogger().debug("records: " + result);
                 }
             }
         }
         return result;
     }
 
     /**
      * Get all child OUs of the given organizational unit.
      *
      * @param id OU id
      * @return id list of all child OUs
      * @throws TripleStoreSystemException If access to the triple store fails.
      */
     @Override
     public List<String> getAllChildOUs(final String id) throws TripleStoreSystemException {
         List<String> result = null;
         final String parentTableName = getTableName(PROP_PARENT);
 
         if (parentTableName != null) {
             final String select;
             final DatabaseType databaseType = getDatabaseType();
             if (databaseType == DatabaseType.POSTGRES) {
                 select =
                     MessageFormat.format("WITH RECURSIVE getChildOUs AS (SELECT {0}.s, {0}.o"
                         + " FROM {0} WHERE {0}.o=''<info:fedora/" + id + ">'' UNION SELECT {0}.s, {0}.o FROM {0},"
                         + " getChildOUs WHERE {0}.o=getChildOUs.s)" + " SELECT distinct(s) FROM getChildOUs;",
                         parentTableName);
             }
             else if (databaseType == DatabaseType.ORACLE) {
                 select =
                     MessageFormat.format("WITH getChildOUs (s, o) AS (SELECT {0}.s, {0}.o"
                         + " FROM {0} WHERE {0}.o=''<info:fedora/" + id + ">'' UNION ALL SELECT {0}.s, {0}.o FROM {0},"
                         + " getChildOUs WHERE {0}.o=getChildOUs.s)" + " SELECT distinct(s) FROM getChildOUs",
                         parentTableName);
             }
             else {
                 throw new TripleStoreSystemException("This kind of relational database is not supported.");
             }
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             result = executeSqlQuery(select);
             if (getLogger().isDebugEnabled()) {
                 if (result.isEmpty()) {
                     getLogger().debug("found no records");
                 }
                 else {
                     getLogger().debug("found " + result.size() + " records");
                     getLogger().debug("records: " + result);
                 }
             }
         }
         return result;
     }
 }
