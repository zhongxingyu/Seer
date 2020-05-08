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
  * Copyright 2009 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.business.fedora.resources;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.z3950.zing.cql.CQLAndNode;
 import org.z3950.zing.cql.CQLBooleanNode;
 import org.z3950.zing.cql.CQLNode;
 import org.z3950.zing.cql.CQLOrNode;
 import org.z3950.zing.cql.CQLParser;
 import org.z3950.zing.cql.CQLRelation;
 import org.z3950.zing.cql.CQLSortNode;
 import org.z3950.zing.cql.CQLTermNode;
 import org.z3950.zing.cql.Modifier;
 import org.z3950.zing.cql.ModifierSet;
 
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 /**
  * This class parses a CQL filter and translates it into a SQL WHERE clause. The
  * filter criteria may also be given as key/value pairs in the map this class is
  * derived from. The keys are the names of the corresponding DB table columns.
  * The values are lists to handle multiple values.
  * 
  * @author SCHE
  */
 public class CqlFilter extends AbstractFilter {
     private static final long serialVersionUID = -5954448073436759899L;
 
     /**
      * Logging goes there.
      */
     private static AppLogger logger = new AppLogger(CqlFilter.class.getName());
 
     private static final Map<String, String> RELATIONS =
         new HashMap<String, String>();
 
     static {
         // relation mappings from CQL to SQL
         RELATIONS.put("<", "<");
         RELATIONS.put("<=", "<=");
         RELATIONS.put("=", "=");
         RELATIONS.put(">", ">");
         RELATIONS.put(">=", ">=");
         RELATIONS.put("<>", "<>");
     }
 
     private final CQLNode root;
 
     /**
      * Empty constructor.
      * 
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     public CqlFilter() throws InvalidSearchQueryException {
         root = null;
     }
 
     /**
      * Parse the given CQL filter.
      * 
      * @param filter
      *            CQL filter
      * 
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     public CqlFilter(final String filter) throws InvalidSearchQueryException {
         try {
             logger.info("filter CQL: " + filter);
             root = new CQLParser().parse(filter);
         }
         catch (Exception e) {
             throw new InvalidSearchQueryException(e);
         }
     }
 
     /**
      * This method returns the given sub query as result if the sub query is not
      * null. A sub query with the value "null" means it is a special query for
      * which no SQL replacement exists. In such a case a replacement will be
      * returned which depends on the given operation.
      * 
      * @param subQuery
      *            original sub query (may be null)
      * @param node
      *            boolean node
      * 
      * @return the original sub query, "TRUE" or "FALSE"
      */
     private String getSubQuery(final String subQuery, final CQLBooleanNode node) {
         String result = subQuery;
 
         if (result == null) {
             if (node instanceof CQLAndNode) {
                 result = "TRUE";
             }
             else if (node instanceof CQLOrNode) {
                 result = "FALSE";
             }
         }
         return result;
     }
 
     /**
      * Convert the XML filter into a SQL WHERE clause.
      * 
      * @param root
      *            root node of the CQL statement
      * 
      * @return SQL WHERE clause representing this filter
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     private String parse(final CQLNode root) throws InvalidSearchQueryException {
         StringBuffer result = new StringBuffer();
         StringBuffer whereClause = new StringBuffer();
 
         if (root != null) {
             whereClause.append('(');
             whereClause.append(toSqlString(root));
             whereClause.append(')');
         }
 
         int andIndex = 0;
 
         for (String name : keySet()) {
             andIndex++;
             if (whereClause.length() > 0) {
                 whereClause.append(" AND ");
             }
             whereClause
                 .append("r.id IN (SELECT resource_id FROM list.property WHERE ");
 
             List<Object> filterList = (List<Object>) get(name);
 
             if (filterList.size() > 1) {
                 whereClause.append('(');
             }
             for (int orIndex = 0; orIndex < filterList.size(); orIndex++) {
                 if (orIndex > 0) {
                     whereClause.append(" OR ");
                 }
                 whereClause.append("(local_path='");
                 whereClause.append(name);
                 whereClause.append("' AND ");
                 whereClause.append("value=");
                 whereClause.append('\'');
                 whereClause.append(filterList
                     .get(orIndex).toString().toLowerCase());
                 whereClause.append("\')");
             }
             if (filterList.size() > 1) {
                 whereClause.append(')');
             }
             whereClause.append(')');
         }
 
         if (whereClause.length() > 0) {
             result.append(whereClause);
         }
         else {
             result.append("TRUE");
         }
 
         // add filter for top level objects
         if (getTopLevelOnly()) {
             if (result.length() > 0) {
                 result.append(" AND ");
             }
             if (objectType != null) {
                 if (objectType == ResourceType.CONTAINER) {
                    result.append("r.id NOT IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result
                         .append(" WHERE local_path='/struct-map/container/id')");
                 }
                 else if (objectType == ResourceType.ITEM) {
                     result.append("r.id NOT IN (SELECT value");
                     result.append(" FROM list.property");
                     result.append(" WHERE local_path='/struct-map/item/id')");
                 }
                 else if (objectType == ResourceType.OU) {
                     result.append("r.id NOT IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result.append(" WHERE local_path='/parents/parent/id')");
                 }
                 else {
                     result.append("TRUE");
                 }
             }
             else {
                 result
                    .append("r.id NOT IN (SELECT resource_id FROM list.property");
                 result
                     .append(" WHERE local_path='/struct-map/container/id' UNION");
                 result.append(" SELECT value FROM list.property");
                 result.append(" WHERE local_path='/struct-map/item/id' UNION");
                 result.append(" SELECT resource_id FROM list.property WHERE");
                 result.append(" local_path='/parents/parent/id')");
             }
         }
 
         // get all parents for a given child
         if (getMember() != null) {
             if (result.length() > 0) {
                 result.append(" AND ");
             }
             if (objectType != null) {
                 if (objectType == ResourceType.CONTAINER) {
                     result.append("r.id IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result
                         .append(" WHERE local_path='/struct-map/container/id'");
                     result.append(" AND value='");
                     result.append(getMember());
                     result.append("')");
                 }
                 else if (objectType == ResourceType.ITEM) {
                     result.append("r.id IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result.append(" WHERE local_path='/struct-map/item/id'");
                     result.append(" AND value='");
                     result.append(getMember());
                     result.append("')");
                 }
                 else if (objectType == ResourceType.OU) {
                     result.append("r.id IN (SELECT value");
                     result.append(" FROM list.property");
                     result.append(" WHERE resource_id='");
                     result.append(getMember());
                     result.append("' AND local_path='/parents/parent/id')");
                 }
                 else {
                     result.append("TRUE");
                 }
             }
             else {
                 result.append("r.id IN (SELECT resource_id FROM list.property");
                 result.append(" WHERE (local_path='/struct-map/item/id' OR "
                     + "local_path='/struct-map/container/id') AND value='");
                 result.append(getMember());
                 result.append("' UNION SELECT value FROM list.property WHERE");
                 result.append(" resource_id='");
                 result.append(getMember());
                 result.append("' AND local_path='/parents/parent/id')");
             }
         }
 
         // get all children for a given parent
         if (getParent() != null) {
             if (result.length() > 0) {
                 result.append(" AND ");
             }
             if (objectType != null) {
                 if (objectType == ResourceType.CONTAINER) {
                     result.append("r.id IN (SELECT value");
                     result.append(" FROM list.property");
                     result.append(" WHERE resource_id='");
                     result.append(getParent());
                     result
                         .append("' AND local_path='/struct-map/container/id')");
                 }
                 else if (objectType == ResourceType.ITEM) {
                     result.append("r.id IN (SELECT value");
                     result.append(" FROM list.property");
                     result.append(" WHERE resource_id='");
                     result.append(getParent());
                     result.append("' AND local_path='/struct-map/item/id')");
                 }
                 else if (objectType == ResourceType.OU) {
                     result.append("r.id IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result.append(" WHERE local_path='/parents/parent/id'");
                     result.append(" AND value='");
                     result.append(getParent());
                     result.append("')");
                 }
                 else {
                     result.append("TRUE");
                 }
             }
             else {
                 result
                     .append("r.id IN (SELECT resource_id FROM list.property WHERE");
                 result.append(" local_path='/parents/parent/id' AND value='");
                 result.append(getParent());
                 result.append("' UNION SELECT value FROM list.property");
                 result.append(" WHERE resource_id='");
                 result.append(getParent());
                 result.append("' AND (local_path='/struct-map/item/id' OR "
                     + "local_path='/struct-map/container/id'))");
             }
         }
         return result.toString();
     }
 
     /**
      * Set all sorting attributes.
      * 
      * @param orderBy
      *            order by attributes
      * 
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     private void setOrderBy(final Vector<ModifierSet> orderBy)
         throws InvalidSearchQueryException {
         Collection<OrderBy> attributes = new Vector<OrderBy>();
 
         for (ModifierSet modifier : orderBy) {
             if (modifier.getModifiers().size() > 0) {
                 for (Modifier mod : modifier.getModifiers()) {
                     if (mod.getType().equals("sort.ascending")) {
                         attributes.add(new OrderBy(modifier.getBase(),
                             DIRECTION_ASCENDING));
                     }
                     else if (mod.getType().equals("sort.descending")) {
                         attributes.add(new OrderBy(modifier.getBase(),
                             DIRECTION_DESCENDING));
                     }
                     else {
                         throw new InvalidSearchQueryException(mod.getType()
                             + ": index modifier type not implemented");
                     }
                 }
             }
             else {
                 attributes.add(new OrderBy(modifier.getBase(),
                     DIRECTION_ASCENDING));
             }
         }
         super.setOrderBy(attributes);
     }
 
     /**
      * Convert the XML filter into a SQL WHERE clause.
      * 
      * @return SQL WHERE clause representing this filter
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     public String toSqlString() throws InvalidSearchQueryException {
         return parse(root);
     }
 
     /**
      * Convert the CQL node into a SQL WHERE clause.
      * 
      * @param node
      *            CQL node
      * 
      * @return SQL WHERE clause representing this filter
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     private String toSqlString(final CQLBooleanNode node)
         throws InvalidSearchQueryException {
         String operation = null;
 
         if (node instanceof CQLAndNode) {
             operation = "AND";
         }
         else if (node instanceof CQLOrNode) {
             operation = "OR";
         }
         else {
             throw new InvalidSearchQueryException(node
                 + ": node type not implemented");
         }
 
         String left = getSubQuery(toSqlString(node.left), node);
         String right = getSubQuery(toSqlString(node.right), node);
 
         return "(" + left + " " + operation + " " + right + ")";
     }
 
     /**
      * Convert the CQL node into a SQL WHERE clause.
      * 
      * @param node
      *            CQL node
      * 
      * @return SQL WHERE clause representing this filter
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     private String toSqlString(final CQLNode node)
         throws InvalidSearchQueryException {
         String result = null;
 
         if (node instanceof CQLBooleanNode) {
             result = toSqlString((CQLBooleanNode) node);
         }
         else if (node instanceof CQLSortNode) {
             result = toSqlString((CQLSortNode) node);
         }
         else if (node instanceof CQLTermNode) {
             result = toSqlString((CQLTermNode) node);
             if (result == null) {
                 // query only contains of a special filter
                 result = "TRUE";
             }
         }
         else {
             throw new InvalidSearchQueryException(node
                 + ": node type not implemented");
         }
         return result;
     }
 
     /**
      * Convert the CQL relation into a SQL WHERE clause.
      * 
      * @param relation
      *            CQL relation
      * 
      * @return SQL WHERE clause representing this filter
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     private String toSqlString(final CQLRelation relation)
         throws InvalidSearchQueryException {
         String result = RELATIONS.get(relation.getBase());
 
         if (result == null) {
             throw new InvalidSearchQueryException(relation.getBase()
                 + ": relation not implemented");
         }
         return result;
     }
 
     /**
      * Convert the CQL node into a SQL WHERE clause.
      * 
      * @param node
      *            CQL node
      * 
      * @return SQL WHERE clause representing this filter
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     private String toSqlString(final CQLSortNode node)
         throws InvalidSearchQueryException {
         setOrderBy(node.getSortIndexes());
         return toSqlString(node.subtree);
     }
 
     /**
      * Convert the CQL node into a SQL WHERE clause.
      * 
      * @param node
      *            CQL node
      * 
      * @return SQL WHERE clause representing this filter
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      */
     private String toSqlString(final CQLTermNode node)
         throws InvalidSearchQueryException {
         String result = null;
         String columnName = PROPERTY_MAP.get(node.getIndex());
         String value = node.getTerm();
 
         if ((columnName == null) || (columnName.length() == 0)) {
             // assume the filter name is a local path style filter
             columnName = node.getIndex();
         }
         if (columnName.equals("role")) {
             setRoleId(value);
         }
         else if (columnName.equals("user")) {
             setUserId(value);
         }
         else if (columnName.equals(TripleStoreUtility.PROP_OBJECT_TYPE)) {
             setObjectType(ResourceType.getResourceTypeFromUri(value));
         }
         else if (columnName.equals(TripleStoreUtility.PROP_PARENT)) {
             setParent(XmlUtility.getIdFromURI(value));
         }
         else if (columnName.equals("top-level-containers")) {
             setObjectType(ResourceType.CONTAINER);
             setTopLevelOnly(Boolean.valueOf(value));
         }
         else if (columnName.equals("top-level-items")) {
             setObjectType(ResourceType.ITEM);
             setTopLevelOnly(Boolean.valueOf(value));
         }
         else if (columnName.equals("top-level-organizational-units")) {
             setObjectType(ResourceType.OU);
             setTopLevelOnly(Boolean.valueOf(value));
         }
         else {
             result =
                 "r.id IN (SELECT resource_id FROM list.property WHERE "
                     + "local_path='" + columnName + "' AND value"
                     + toSqlString(node.getRelation()) + "'"
                     + value.toLowerCase() + "')";
         }
         return result;
     }
 }
