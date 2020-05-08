 /**
  * ====================
  *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
  * Copyright 2011-2013 Tirasa. All rights reserved.
  *
  * The contents of this file are subject to the terms of the Common Development
  * and Distribution License("CDDL") (the "License"). You may not use this file
  * except in compliance with the License.
  *
  * You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL
  * See the License for the specific language governing permissions and limitations
  * under the License.
  *
  * When distributing the Covered Code, include this CDDL Header Notice in each file
  * and include the License file at https://oss.oracle.com/licenses/CDDL.
  * If applicable, add the following below this CDDL Header, with the fields
  * enclosed by brackets [] replaced by your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * ====================
  */
 import java.sql.Types;
 import org.connid.bundles.csvdir.CSVDirConfiguration;
 import org.connid.bundles.csvdir.CSVDirConnector;
 import org.connid.bundles.db.common.FilterWhereBuilder;
 import org.connid.bundles.db.common.SQLParam;
 import org.identityconnectors.common.StringUtil;
 import org.identityconnectors.framework.common.objects.Attribute;
 import org.identityconnectors.framework.common.objects.AttributeUtil;
 import org.identityconnectors.framework.common.objects.Name;
 import org.identityconnectors.framework.common.objects.ObjectClass;
 import org.identityconnectors.framework.common.objects.OperationOptions;
 import org.identityconnectors.framework.common.objects.OperationalAttributes;
 import org.identityconnectors.framework.common.objects.Uid;
 import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
 import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
 import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
 import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
 import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;
 import org.identityconnectors.framework.common.objects.filter.GreaterThanOrEqualFilter;
 import org.identityconnectors.framework.common.objects.filter.LessThanFilter;
 import org.identityconnectors.framework.common.objects.filter.LessThanOrEqualFilter;
 import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
 
 public class CSVDirFilterTranslator
         extends AbstractFilterTranslator<FilterWhereBuilder> {
 
     private CSVDirConnector connector;
 
     private ObjectClass oclass;
 
     private OperationOptions options;
 
     public CSVDirFilterTranslator(
             final CSVDirConnector connector,
             final ObjectClass oclass,
             final OperationOptions options) {
         this.connector = connector;
         this.oclass = oclass;
         this.options = options;
     }
 
     protected FilterWhereBuilder createBuilder() {
         return new FilterWhereBuilder();
     }
 
     @Override
     protected FilterWhereBuilder createAndExpression(FilterWhereBuilder leftExpression,
             FilterWhereBuilder rightExpression) {
         FilterWhereBuilder build = createBuilder();
         build.join("AND", leftExpression, rightExpression);
         return build;
     }
 
     @Override
     protected FilterWhereBuilder createOrExpression(FilterWhereBuilder leftExpression,
             FilterWhereBuilder rightExpression) {
         FilterWhereBuilder build = createBuilder();
         build.join("OR", leftExpression, rightExpression);
         return build;
     }
 
     @Override
     protected FilterWhereBuilder createEqualsExpression(EqualsFilter filter, boolean not) {
         final Attribute attribute = filter.getAttribute();
         if (!validateSearchAttribute(attribute)) {
             return null;
         }
 
         SQLParam[] params = getSQLParam(attribute, oclass, options);
         if (params == null) {
             return null;
         }
 
         final FilterWhereBuilder ret = createBuilder();
 
         ret.getWhere().append("(");
 
         if (not) {
             ret.getWhere().append("NOT ");
         }
 
         SQLParam param;
         for (int i = 0; i < params.length; i++) {
             param = params[i];
             if (i > 0) {
                 ret.getWhere().append(" AND ");
             }
 
             if (param.getValue() == null) {
                 // Normalize NULLs
                 ret.addNull(param.getName());
             } else {
                 ret.addBind(param, "=");
             }
         }
 
         ret.getWhere().append(")");
 
         return ret;
     }
 
     @Override
     protected FilterWhereBuilder createContainsExpression(
             final ContainsFilter filter, final boolean not) {
 
         final Attribute attribute = filter.getAttribute();
         if (!validateSearchAttribute(attribute)) {
             return null;
         }
 
         final SQLParam[] params = getSQLParam(attribute, oclass, options);
         if (params == null) {
             //Null value filter is not supported
             return null;
         }
 
         final FilterWhereBuilder ret = createBuilder();
 
         ret.getWhere().append("(");
 
         if (not) {
             ret.getWhere().append("NOT ");
         }
 
         String value;
 
         SQLParam param;
         for (int i = 0; i < params.length; i++) {
             param = params[i];
             if (i > 0) {
                 ret.getWhere().append(" AND ");
             }
 
             value = (String) param.getValue();
 
             // ignore null values
             if (value != null && value instanceof String) {
 
                 //To be sure, this is not already quoted
                 if (!value.startsWith("%")) {
                     value = "%" + value;
                 }
                 if (!value.endsWith("%")) {
                     value = value + "%";
                 }
                 ret.addBind(
                         new SQLParam(param.getName(),
                         value,
                         param.getSqlType()), "LIKE");
             }
         }
         ret.getWhere().append(")");
 
         return ret;
     }
 
     @Override
     protected FilterWhereBuilder createEndsWithExpression(
             final EndsWithFilter filter, final boolean not) {
         final Attribute attribute = filter.getAttribute();
         if (!validateSearchAttribute(attribute)) {
             return null;
         }
 
         final SQLParam[] params = getSQLParam(attribute, oclass, options);
         if (params == null) {
             //Null value filter is not supported
             return null;
         }
 
         final FilterWhereBuilder ret = createBuilder();
 
         ret.getWhere().append("(");
 
         if (not) {
             ret.getWhere().append("NOT ");
         }
 
         String value;
 
         SQLParam param;
         for (int i = 0; i < params.length; i++) {
             param = params[i];
             if (i > 0) {
                 ret.getWhere().append(" AND ");
             }
 
             value = (String) param.getValue();
 
             // ignore null values
             if (value != null && value instanceof String) {
                 //To be sure, this is not already quoted
                 if (!value.startsWith("%")) {
                     value = "%" + value;
                 }
                 ret.addBind(
                         new SQLParam(param.getName(),
                         value,
                         param.getSqlType()), "LIKE");
             }
         }
         ret.getWhere().append(")");
 
         return ret;
     }
 
     @Override
     protected FilterWhereBuilder createStartsWithExpression(
             final StartsWithFilter filter, final boolean not) {
         final Attribute attribute = filter.getAttribute();
         if (!validateSearchAttribute(attribute)) {
             return null;
         }
 
         final SQLParam[] params = getSQLParam(attribute, oclass, options);
         if (params == null) {
             //Null value filter is not supported
             return null;
         }
 
         final FilterWhereBuilder ret = createBuilder();
 
         ret.getWhere().append("(");
 
         if (not) {
             ret.getWhere().append("NOT ");
         }
 
         String value;
 
         SQLParam param;
         for (int i = 0; i < params.length; i++) {
             param = params[i];
             if (i > 0) {
                 ret.getWhere().append(" AND ");
             }
 
             value = (String) param.getValue();
 
             // ignore null values
             if (value != null && value instanceof String) {
                 //To be sure, this is not already quoted
                 if (!value.endsWith("%")) {
                     value = value + "%";
                 }
                 ret.addBind(
                         new SQLParam(param.getName(),
                         value,
                         param.getSqlType()), "LIKE");
             }
         }
 
         ret.getWhere().append(")");
 
         return ret;
     }
 
     @Override
     protected FilterWhereBuilder createGreaterThanExpression(
             final GreaterThanFilter filter, final boolean not) {
         final Attribute attribute = filter.getAttribute();
         if (!validateSearchAttribute(attribute)) {
             return null;
         }
 
         final SQLParam[] params = getSQLParam(attribute, oclass, options);
         if (params == null) {
             //Null value filter is not supported
             return null;
         }
 
         final FilterWhereBuilder ret = createBuilder();
 
         ret.getWhere().append("(");
 
         String value;
         SQLParam param;
 
         for (int i = 0; i < params.length; i++) {
             param = params[i];
             if (i > 0) {
                 ret.getWhere().append(" AND ");
             }
 
             value = (String) param.getValue();
 
             // ignore null values
             if (value != null) {
                 final String op = not ? "<=" : ">";
                 ret.addBind(param, op);
             }
         }
         ret.getWhere().append(")");
 
         return ret;
     }
 
     @Override
     protected FilterWhereBuilder createGreaterThanOrEqualExpression(
             final GreaterThanOrEqualFilter filter, final boolean not) {
         final Attribute attribute = filter.getAttribute();
         if (!validateSearchAttribute(attribute)) {
             return null;
         }
 
         final SQLParam[] params = getSQLParam(attribute, oclass, options);
         if (params == null) {
             //Null value filter is not supported
             return null;
         }
 
         final FilterWhereBuilder ret = createBuilder();
 
         ret.getWhere().append("(");
 
         String value;
         SQLParam param;
 
         for (int i = 0; i < params.length; i++) {
             param = params[i];
             if (i > 0) {
                 ret.getWhere().append(" AND ");
             }
 
             value = (String) param.getValue();
 
             // ignore null values
             if (value != null) {
                 final String op = not ? "<" : ">=";
                 ret.addBind(param, op);
             }
         }
 
         ret.getWhere().append(")");
 
         return ret;
     }
 
     @Override
     protected FilterWhereBuilder createLessThanExpression(
             final LessThanFilter filter, final boolean not) {
         final Attribute attribute = filter.getAttribute();
         if (!validateSearchAttribute(attribute)) {
             return null;
         }
 
         final SQLParam[] params = getSQLParam(attribute, oclass, options);
         if (params == null) {
             //Null value filter is not supported
             return null;
         }
 
         final FilterWhereBuilder ret = createBuilder();
 
         ret.getWhere().append("(");
 
         String value;
         SQLParam param;
 
         for (int i = 0; i < params.length; i++) {
             param = params[i];
             if (i > 0) {
                 ret.getWhere().append(" AND ");
             }
 
             value = (String) param.getValue();
 
             // ignore null values
             if (value != null) {
                 final String op = not ? ">=" : "<";
                 ret.addBind(param, op);
             }
         }
 
         ret.getWhere().append(")");
 
         return ret;
     }
 
     @Override
     protected FilterWhereBuilder createLessThanOrEqualExpression(
             final LessThanOrEqualFilter filter, final boolean not) {
         final Attribute attribute = filter.getAttribute();
         if (!validateSearchAttribute(attribute)) {
             return null;
         }
 
         final SQLParam[] params = getSQLParam(attribute, oclass, options);
         if (params == null) {
             //Null value filter is not supported
             return null;
         }
 
         final FilterWhereBuilder ret = createBuilder();
 
         ret.getWhere().append("(");
 
         String value;
         SQLParam param;
 
         for (int i = 0; i < params.length; i++) {
             param = params[i];
             if (i > 0) {
                 ret.getWhere().append(" AND ");
             }
 
             value = (String) param.getValue();
 
             // ignore null values
             if (value != null) {
                 final String op = not ? ">" : "<=";
                 ret.addBind(param, op);
             }
         }
 
         ret.getWhere().append(")");
 
         return ret;
     }
 
     protected boolean validateSearchAttribute(final Attribute attribute) {
         //Ignore streamed ( byte[] objects ) from query
         if (byte[].class.equals(AttributeUtil.getSingleValue(attribute).
                 getClass())) {
             return false;
         }
         //Otherwise let the database process
         return true;
     }
 
     protected SQLParam[] getSQLParam(
             final Attribute attribute,
             final ObjectClass oclass,
             final OperationOptions options) {
 
         final Integer columnType = Types.VARCHAR;
         final String[] columnNames = getColumnName(attribute.getName());
 
         final String[] values =
                 AttributeUtil.getSingleValue(attribute) != null
                 ? AttributeUtil.getSingleValue(attribute).toString().split(
                 ((CSVDirConfiguration) connector.getConfiguration()).getKeyseparator())
                 : null;
 
         final SQLParam[] params;
 
         if (columnNames.length > 1) {
             params = new SQLParam[columnNames.length];
 
             for (int i = 0; i < columnNames.length; i++) {
                 params[i] = new SQLParam(
                         columnNames[i],
                         values != null && values.length == columnNames.length
                         ? values[i] : null,
                         columnType);
             }
         } else {
             params = new SQLParam[]{new SQLParam(
                 columnNames[0],
                 values != null && values.length == columnNames.length
                 ? values[0] : null,
                 columnType)};
         }
 
         return params;
     }
 
     protected String[] getColumnName(String attributeName) {
         if (Name.NAME.equalsIgnoreCase(attributeName)) {
             return ((CSVDirConfiguration) connector.getConfiguration()).getKeyColumnNames();
         }
         if (Uid.NAME.equalsIgnoreCase(attributeName)) {
             return ((CSVDirConfiguration) connector.getConfiguration()).getKeyColumnNames();
         }
         if (!StringUtil.isBlank(
                 ((CSVDirConfiguration) connector.getConfiguration()).getPasswordColumnName())
                 && OperationalAttributes.PASSWORD_NAME.equalsIgnoreCase(attributeName)) {
             return new String[]{
                         ((CSVDirConfiguration) connector.getConfiguration()).getPasswordColumnName()};
         }
         return new String[]{attributeName};
     }
 }
