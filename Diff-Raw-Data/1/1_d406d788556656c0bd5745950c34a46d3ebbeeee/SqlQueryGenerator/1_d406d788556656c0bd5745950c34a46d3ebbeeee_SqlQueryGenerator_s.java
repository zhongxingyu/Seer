 /*
  * Copyright 2012 and beyond, Juan Luis Paz
  *
  * This file is part of Uaithne.
  *
  * Uaithne is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Uaithne is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Uaithne. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.uaithne.generator.processors.database.sql;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.tools.Diagnostic;
 import org.uaithne.annotations.Comparator;
 import org.uaithne.annotations.EntityQueries;
 import org.uaithne.annotations.IdQueries;
 import org.uaithne.annotations.MappedName;
 import org.uaithne.annotations.PageQueries;
 import org.uaithne.annotations.Query;
 import org.uaithne.annotations.sql.CustomSqlQuery;
 import org.uaithne.generator.commons.*;
 
 public abstract class SqlQueryGenerator extends SqlGenerator {
     
     //<editor-fold defaultstate="collapsed" desc="Complete query">
     public String[] completeQuery(String[] query, OperationInfo operation, boolean count, boolean selectPage) {
         CustomSqlQuery customQuery = operation.getAnnotation(CustomSqlQuery.class);
         String completed;
         if (query == null) {
             completed = completeQueryWithoutEnvolve(null, operation, count, selectPage, customQuery);
         } else {
             completed = joinln(query);
             completed = completeQueryWithoutEnvolve(completed, operation, count, selectPage, customQuery);
         }
         completed = finalizeQuery(completed, operation, customQuery);
 
         String[] result;
         if (completed == null) {
             result = null;
         } else {
             result = completed.split("\n");
         }
         if (operation.isLimitToOneResult()) {
             return envolveInSelectOneRow(result);
         } else {
             return result;
         }
 
     }
 
     public String completeQueryWithoutEnvolve(String query, OperationInfo operation, boolean count, boolean selectPage, CustomSqlQuery customQuery) {
         boolean addSelect = false;
         boolean addFrom = false;
         boolean addWhere = false;
         boolean addGroupBy = false;
         boolean addOrderBy = false;
         boolean ignoreQuery = false;
         boolean prepend = false;
         StringBuilder appender = new StringBuilder();
         EntityInfo entity = operation.getEntity();
         if (entity != null) {
             entity = entity.getCombined();
         }
 
         if (query == null && customQuery == null) {
             if (entity == null) {
                 processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                         "Unable to automatically generate the query for this operation, enter the query manually using Query annotation",
                         operation.getElement());
                 return null;
             }
             addSelect = addFrom = addWhere = addGroupBy = addOrderBy = ignoreQuery = true;
         } else if (query != null && !query.isEmpty()) {
             query = query.trim();
             String queryLowerCase = query.toLowerCase();
             if (query.isEmpty()) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically generate the query for this operation, enter the query manually using Query annotation",
                             operation.getElement());
                     return null;
                 }
                 addSelect = addFrom = addWhere = addGroupBy = addOrderBy = ignoreQuery = true;
             } else if (queryLowerCase.startsWith("from")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically complete the query for this operation, enter the full query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addSelect = true;
                 }
             } else if (queryLowerCase.startsWith("where")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically complete the query for this operation, enter the full query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addSelect = addFrom = true;
                 }
             } else if (queryLowerCase.startsWith("group")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically generate the query for this operation, enter the query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addSelect = addFrom = addWhere = true;
                 }
             } else if (queryLowerCase.startsWith("order")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically generate the query for this operation, enter the query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addSelect = addFrom = addWhere = addGroupBy = true;
                 }
             } else if (queryLowerCase.endsWith("select...")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically complete the query for this operation, enter the full query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addSelect = addFrom = addWhere = addGroupBy = addOrderBy = prepend = true;
                     query = query.substring(0, query.length() - "select...".length());
                 }
             } else if (queryLowerCase.endsWith("from...")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically complete the query for this operation, enter the full query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addFrom = addWhere = addGroupBy = addOrderBy = prepend = true;
                     query = query.substring(0, query.length() - "from...".length());
                 }
             } else if (queryLowerCase.endsWith("where...")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically complete the query for this operation, enter the full query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addWhere = addGroupBy = addOrderBy = prepend = true;
                     query = query.substring(0, query.length() - "where...".length());
                 }
             } else if (queryLowerCase.endsWith("group...")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically complete the query for this operation, enter the full query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addGroupBy = addOrderBy = prepend = true;
                     query = query.substring(0, query.length() - "group...".length());
                 }
             } else if (queryLowerCase.startsWith("order...")) {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Unable to automatically generate the query for this operation, enter the query manually using Query annotation",
                             operation.getElement());
                 } else {
                     addOrderBy = prepend = true;
                     query = query.substring(0, query.length() - "order...".length());
                 }
             } else {
                 return query;
             }
         }
 
         if (customQuery != null) {
             if (hasQueryValue(customQuery.query())) {
                 appendToQueryln(appender, customQuery.beforeQuery(), "");
                 appendToQueryln(appender, customQuery.query(), "");
                 appendToQueryln(appender, customQuery.afterQuery(), "");
                 return appender.toString();
             } else if (query == null) {
                 addSelect = addFrom = addWhere = addGroupBy = addOrderBy = ignoreQuery = true;
             }
         }
         
         ArrayList<FieldInfo> orderBys = new ArrayList<FieldInfo>(0);
         for (FieldInfo field : operation.getFields()) {
             if (field.isManually()) {
                 continue;
             }
             if (field.isSetValueMark()) {
                 continue;
             }
             if (field.isOrderBy()) {
                 orderBys.add(field);
             }
         }
 
         if (prepend && !ignoreQuery) {
             appender.append(query);
         }
 
         if (addSelect) {
             appendSelect(appender, operation, entity, count, customQuery);
         }
         if (selectPage && !count && (addSelect || addFrom)) {
             appendOrderByAfterSelectForSelectPage(appender, orderBys, customQuery);
         }
         if (addFrom) {
             appendFrom(appender, entity, customQuery);
         }
         if (addWhere) {
             appendWhere(appender, operation, customQuery, count);
         }
         if (addGroupBy) {
             appendGroupBy(appender, operation, entity, customQuery);
         }
         if (!count) {
             if (addOrderBy) {
                 if (selectPage) {
                     appendOrderByForSelectPage(appender, orderBys, customQuery);
                 } else {
                     appendOrderBy(appender, orderBys, customQuery);
                 }
             }
             if (operation.isLimitToOneResult()) {
                 String limitOne = selectOneRowAfterOrderBy();
                 if (limitOne != null && !limitOne.isEmpty()) {
                     appender.append("\n");
                     appender.append(limitOne);
                 }
             }
             if (operation.getOperationKind() == OperationKind.SELECT_PAGE) {
                 String page = selectPageAfterOrderBy();
                 if (page != null && !page.isEmpty()) {
                     appender.append("\n");
                     appender.append(page);
                 }
             }
         }
 
         String result;
         if (ignoreQuery || prepend) {
             result = appender.toString();
         } else {
             result = appender.toString();
             if (query != null && !query.isEmpty()) {
                 result = result + "\n" + query;
             }
         }
         if (customQuery != null) {
             StringBuilder sb = new StringBuilder();
             appendToQueryln(sb, customQuery.beforeQuery(), "");
             if (sb.length() > 0) {
                 sb.append("\n");
             }
             sb.append(result);
             appendToQueryln(sb, customQuery.afterQuery(), "");
             result = sb.toString();
         }
         return result;
     }
 
     public void appendSelect(StringBuilder result, OperationInfo operation, EntityInfo entity, boolean count, CustomSqlQuery customQuery) {
         result.append("select");
         if (operation.isDistinct()) {
             result.append(" distinct");
         }
         if (count) {
             result.append("\n    count(*)");
         } else {
             if (operation.isLimitToOneResult()) {
                 String limitOne = selectOneRowBeforeSelect();
                 if (limitOne != null && !limitOne.isEmpty()) {
                     result.append(" ");
                     result.append(limitOne);
                     result.append(" ");
                 }
             }
 
             if (operation.getOperationKind() == OperationKind.SELECT_PAGE) {
                 String page = selectPageBeforeSelect();
                 if (page != null && !page.isEmpty()) {
                     result.append(page);
                     result.append(" ");
                 }
             }
             if (customQuery!= null) {
                 appendToQueryln(result, customQuery.beforeSelectExpression(), "    ");
 
                 if (hasQueryValue(customQuery.select())) {
                     appendToQueryln(result, customQuery.select(), "    ");
                 } else {
                     appendSelectFields(result, operation, entity, count, customQuery);
                 }
 
                 appendToQueryln(result, customQuery.afterSelectExpression(), "    ");
             } else {
                 appendSelectFields(result, operation, entity, count, null);
             }
         }
     }
 
     public void appendSelectFields(StringBuilder result, OperationInfo operation, EntityInfo entity, boolean count, CustomSqlQuery customQuery) {
         boolean requireComma = false;
         for (FieldInfo field : entity.getFields()) {
             if (field.isManually()) {
                 continue;
             }
             if (requireComma) {
                 result.append(",\n    ");
             } else {
                 result.append("\n    ");
             }
             result.append(getColumnNameForSelect(field, customQuery));
             requireComma = true;
         }
     }
 
     public void appendFrom(StringBuilder result, EntityInfo entity, CustomSqlQuery customQuery) {
         result.append("\nfrom");
         if (customQuery != null) {
             appendToQueryln(result, customQuery.beforeFromExpression(), "    ");
             String[] from = customQuery.from();
             if (hasQueryValue(from)) {
                 appendToQueryln(result, from, "    ");
             } else {
                 appendToQueryln(result, getTableNameForSelect(entity, customQuery), "    ");
             }
             appendToQueryln(result, customQuery.afterFromExpression(), "    ");
         } else {
             appendToQueryln(result, getTableNameForSelect(entity, null), "    ");
         }
     }
 
     public void appendWhere(StringBuilder query, OperationInfo operation, CustomSqlQuery customQuery, boolean count) {
         ArrayList<FieldInfo> fields = operation.getFields();
 
         if (customQuery != null) {
             if (hasQueryValue(customQuery.where())) {
                 query.append("\n");
                 appendStartWhere(query);
                 appendToQueryln(query, customQuery.beforeWhereExpression(), "    ");
                 appendToQueryln(query, customQuery.where(), "    ");
                 appendToQueryln(query, customQuery.afterWhereExpression(), "    ");
                 appendSelectPageAfterWhere(query, true);
                 appendEndWhere(query, "\n");
             }
         }
 
         boolean hasConditions = false;
         boolean hasOptionals = false;
         StringBuilder result = new StringBuilder();
         boolean requireAnd = false;
         for (FieldInfo field : fields) {
             if (field.isManually()) {
                 continue;
             }
             if (field.isSetValueMark()) {
                 continue;
             }
             if (field.isOrderBy()) {
                 continue;
             }
             boolean optional = field.isOptional();
             if (optional) {
                 if (requireAnd) {
                     result.append("\n    ");
                 } else {
                     result.append("    ");
                 }
                 String separator;
                 if (requireAnd) {
                     separator = "and ";
                 } else {
                     separator = "";
                 }
                 appendConditionStartIfNotNull(result, field, separator);
                 hasOptionals = true;
             } else {
                 if (requireAnd) {
                     result.append("\n    and ");
                 } else {
                     result.append("    ");
                 }
             }
             appendCondition(result, field, customQuery);
             hasConditions = true;
             if (optional) {
                 appendConditionEndIf(result);
             }
             requireAnd = true;
         }
         if (operation.isLimitToOneResult()) {
             String limitOne = selectOneRowAfterWhere();
             if (limitOne != null && !limitOne.isEmpty()) {
                 if (requireAnd) {
                     result.append("\n    and ");
                 } else {
                     result.append("    ");
                 }
                 result.append(limitOne);
                 requireAnd = true;
                 hasConditions = true;
             }
         }
         if (operation.isUseLogicalDeletion()) {
             List<FieldInfo> entityFields = operation.getEntity().getCombined().getFields();
             for (FieldInfo field : entityFields) {
                 if (field.isManually()) {
                     continue;
                 }
                 if (field.isDeletionMark()) {
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     appendNotDeleted(result, field);
                     requireAnd = true;
                     hasConditions = true;
                 }
             }
         }
         if (!count && operation.getOperationKind() == OperationKind.SELECT_PAGE) {
             hasConditions = hasConditions || appendSelectPageAfterWhere(result, requireAnd);
         }
 
         if (hasConditions) {
             if (customQuery != null && (hasQueryValue(customQuery.beforeWhereExpression()) || hasQueryValue(customQuery.afterWhereExpression())) ) {
                 query.append("\n");
                 appendStartWhere(query);
                 appendToQueryln(query, customQuery.beforeWhereExpression(), "    ");
                 query.append("\n");
                 query.append(result);
                 appendToQueryln(query, customQuery.afterWhereExpression(), "    ");
                 appendEndWhere(query, "\n");
             } else {
                 if (hasOptionals) {
                     query.append("\n");
                     appendStartWhere(query);
                     query.append("\n");
                     query.append(result);
                     appendEndWhere(query, "\n");
                 } else {
                     query.append("\nwhere\n");
                     query.append(result);
                 }
             }
         } else if (customQuery != null && (hasQueryValue(customQuery.beforeWhereExpression()) || hasQueryValue(customQuery.where()) || hasQueryValue(customQuery.afterWhereExpression())) ) {
             query.append("\n");
             appendStartWhere(query);
             appendToQueryln(query, customQuery.beforeWhereExpression(), "    ");
             appendToQueryln(query, customQuery.where(), "    ");
             appendToQueryln(query, customQuery.afterWhereExpression(), "    ");
             appendEndWhere(query, "\n");
         }
     }
 
     public void appendCondition(StringBuilder result, FieldInfo field, CustomSqlQuery customQuery) {
         Comparator comparator = getComparator(field);
 
         String comparatorTemplate = translateComparator(comparator);
         String template = getConditionTemplate(field, comparatorTemplate);
 
         String condition = getConditionComparator(comparatorTemplate, template, field, customQuery);
         result.append(condition);
     }
 
 
     public void appendGroupBy(StringBuilder result, OperationInfo operation, EntityInfo entity, CustomSqlQuery customQuery) {
         if (customQuery != null) {
             if (hasQueryValue(customQuery.beforeGroupByExpression()) ||
                     hasQueryValue(customQuery.groupBy()) ||
                     hasQueryValue(customQuery.afterGroupByExpression())) {
                 result.append("\ngroup by");
                 appendToQueryln(result, customQuery.beforeGroupByExpression(), "    ");
                 appendToQueryln(result, customQuery.groupBy(), "    ");
                 appendToQueryln(result, customQuery.afterGroupByExpression(), "    ");
             }
         }
     }
 
     public abstract void appendOrderBy(StringBuilder result, ArrayList<FieldInfo> orderBys, CustomSqlQuery customQuery);
     public abstract void appendOrderByForSelectPage(StringBuilder result, ArrayList<FieldInfo> orderBys, CustomSqlQuery customQuery);
     public abstract void appendOrderByAfterSelectForSelectPage(StringBuilder result, ArrayList<FieldInfo> orderBys, CustomSqlQuery customQuery);
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Utils for generate the select query">
     public String getColumnNameForSelect(FieldInfo field, CustomSqlQuery customQuery) {
         String mappedName = field.getMappedName();
         String result;
         if (mappedName == null || mappedName.isEmpty()) {
             result = field.getName();
         } else {
             result = mappedName + " as \"" + field.getName() + "\"";
         }
         if (customQuery != null) {
             String tableAlias = customQuery.tableAlias();
             if (tableAlias != null && !tableAlias.isEmpty()) {
                 return tableAlias + "." + result;
             }
         }
         return result;
     }
 
     public String[] getTableNameForSelect(EntityInfo entity, CustomSqlQuery customQuery) {
         MappedName mappedName = entity.getAnnotation(MappedName.class);
         String[] result;
         if (mappedName == null) {
             result = entity.getDataType().getSimpleNameWithoutGenerics().split("\n");
         } else {
             result = joinln(mappedName.value()).split("\n");
         }
         if (customQuery != null) {
             String tableAlias = customQuery.tableAlias();
             if (tableAlias != null && !tableAlias.isEmpty()) {
                 for (int i = 0; i < result.length; i++) {
                     result[i] = result[i] + " " + tableAlias;
                 }
             }
         }
         return result;
     }
 
     public String getColumnName(FieldInfo field) {
         return field.getMappedNameOrName();
     }
 
     public String getColumnNameForWhere(FieldInfo field, CustomSqlQuery customQuery) {
         if (customQuery != null) {
             String tableAlias = customQuery.tableAlias();
             if (tableAlias != null && !tableAlias.isEmpty()) {
                 if (field.hasOwnMappedName()) {
                     return field.getMappedNameOrName();
                 } else {
                     return tableAlias + "." + field.getMappedNameOrName();
                 }
             }
         }
         return field.getMappedNameOrName();
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="For generate queries">
     public abstract void appendStartWhere(StringBuilder result);
 
     public abstract void appendEndWhere(StringBuilder result, String separator);
 
     public abstract void appendStartSet(StringBuilder result);
 
     public abstract void appendEndSet(StringBuilder result, String separator);
     
     public abstract void appendStartSetValueIfNotNull(StringBuilder result, FieldInfo field);
     
     public abstract void appendEndSetValueIfNotNull(StringBuilder result, boolean requireComma);
 
     public abstract void appendConditionStartIfNull(StringBuilder result, FieldInfo field, String separator);
 
     public abstract void appendConditionStartIfNotNull(StringBuilder result, FieldInfo field, String separator);
 
     public abstract void appendConditionEndIf(StringBuilder result);
 
     public abstract String translateComparator(Comparator comparator);
 
     public abstract String getConditionElementValue(String rule, FieldInfo field, CustomSqlQuery customQuery);
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Sql Values">
     public abstract String currentSqlDate();
 
     public abstract String falseValue();
 
     public abstract String trueValue();
 
     public abstract String[] envolveInSelectPage(String[] query);
 
     public abstract String selectPageBeforeSelect();
 
     public abstract boolean appendSelectPageAfterWhere(StringBuilder result, boolean requireAnd);
     
     public abstract String selectPageAfterOrderBy();
 
     public abstract String[] envolveInSelectOneRow(String[] query);
 
     public abstract String selectOneRowBeforeSelect();
     
     public abstract String selectOneRowAfterWhere();
     
     public abstract String selectOneRowAfterOrderBy();
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Deletion mark managment">
     public void appendNotDeleted(StringBuilder result, FieldInfo field) {
         if (field.isOptional()) {
             result.append(getColumnName(field));
             result.append(" is not null");
         } else {
             result.append(getColumnName(field));
             result.append(" = ");
             result.append(falseValue());
         }
     }
 
     public void appendNotDeletedValue(StringBuilder result, FieldInfo field) {
         if (field.isOptional()) {
             result.append("null");
         } else {
             result.append(falseValue());
         }
     }
 
     public void appendDeletedValue(StringBuilder result, FieldInfo field) {
         if (field.getDataType().isDate()) {
             result.append(currentSqlDate());
         } else {
             result.append(trueValue());
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Version managment">
     public abstract boolean handleVersionFieldOnInsert();
     public abstract boolean handleVersionFieldOnUpdate();
     public abstract boolean handleVersionFieldOnDelete();
     
     public abstract void appendInitialVersionValue(StringBuilder result, EntityInfo entity, FieldInfo field);
     public abstract void appendNextVersionValue(StringBuilder result, EntityInfo entity, FieldInfo field);
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Custom operations queries">
     @Override
     public String[] getCustomInsertQuery(OperationInfo operation) {
         Query query = operation.getAnnotation(Query.class);
         CustomSqlQuery customQuery = operation.getAnnotation(CustomSqlQuery.class);
         String finalQuery;
         if (customQuery != null && hasQueryValue(customQuery.query())) {
             finalQuery = joinln(customQuery.query());
         } else if (query == null) {
             EntityInfo entity = operation.getEntity().getCombined();
             StringBuilder result = new StringBuilder();
             result.append("insert into");
             if (customQuery != null && hasQueryValue(customQuery.from())) {
                 appendToQueryln(result, customQuery.from(), "    ");
             } else {
                 appendToQueryln(result, getTableName(entity), "    ");
             }
             result.append("\n(");
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.beforeInsertIntoExpression(), "    ");
             }
 
             if (customQuery != null && hasQueryValue(customQuery.insertInto())) {
                 appendToQueryln(result, customQuery.insertInto(), "    ");
             } else {
                 result.append("\n");
                 boolean requireComma = false;
                 for (FieldInfo field : entity.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (!field.isIdentifier()) {
                         continue;
                     } else if (field.isInsertDateMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         requireComma = true;
                     } else if (!includeIdOnInsert(entity, field)) {
                         continue;
                     } else {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         requireComma = true;
                     }
                 }
                 for (FieldInfo field : operation.getFields()) {
                     if (field.isManually()) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 }
                 for (FieldInfo field : entity.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (field.isIdentifier()) {
                         continue;
                     } else if (field.isInsertDateMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         requireComma = true;
                     } else if (field.isDeletionMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         requireComma = true;
                     } else if (field.isVersionMark()) {
                         if (!handleVersionFieldOnInsert()) {
                             continue;
                         }
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         requireComma = true;
                     }
                 }
             }
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.afterInsertIntoExpression(), "    ");
             }
 
             result.append("\n) values (");
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.beforeInsertValuesExpression(), "    ");
             }
 
             if (customQuery != null && hasQueryValue(customQuery.insertValues())) {
                 appendToQueryln(result, customQuery.insertInto(), "    ");
             } else {
                 result.append("\n");
                 boolean requireComma = false;
                 for (FieldInfo field : entity.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (!field.isIdentifier()) {
                         continue;
                     } else if (field.isInsertDateMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(currentSqlDate());
                         requireComma = true;
                     } else if (!includeIdOnInsert(entity, field)) {
                         continue;
                     } else {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         appendIdNextValue(result, entity, field);
                         requireComma = true;
                     }
                 }
                 for (FieldInfo field : operation.getFields()) {
                     if (field.isManually()) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getParameterValue(field));
                     requireComma = true;
                 }
                 for (FieldInfo field : entity.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (field.isIdentifier()) {
                         continue;
                     } else if (field.isInsertDateMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(currentSqlDate());
                         requireComma = true;
                     } else if (field.isDeletionMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         appendNotDeletedValue(result, field);
                         requireComma = true;
                     } else if (field.isVersionMark()) {
                         if (!handleVersionFieldOnInsert()) {
                             continue;
                         }
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         appendInitialVersionValue(result, entity, field);
                         requireComma = true;
                     }
                 }
             }
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.afterInsertValuesExpression(), "    ");
             }
 
             result.append("\n)");
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.value());
         }
         finalQuery = finalizeQuery(finalQuery, operation, customQuery);
         return finalQuery.split("\n");
     }
 
     @Override
     public String[] getCustomUpdateQuery(OperationInfo operation) {
         Query query = operation.getAnnotation(Query.class);
         CustomSqlQuery customQuery = operation.getAnnotation(CustomSqlQuery.class);
         String finalQuery;
         if (customQuery != null && hasQueryValue(customQuery.query())) {
             finalQuery = joinln(customQuery.query());
         } else if (query == null) {
             EntityInfo entity = operation.getEntity().getCombined();
             StringBuilder result = new StringBuilder();
             result.append("update");
             if (customQuery != null && hasQueryValue(customQuery.from())) {
                 appendToQueryln(result, customQuery.from(), "    ");
             } else {
                 appendToQueryln(result, getTableName(entity), "    ");
             }
             result.append("\nset");
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.beforeUpdateSetExpression(), "    ");
             }
 
             if (customQuery != null && hasQueryValue(customQuery.updateSet())) {
                 appendToQueryln(result, customQuery.updateSet(), "    ");
             } else {
                 result.append("\n");
                 boolean requireComma = false;
                 boolean hasSetValueFields = false;
                 for (FieldInfo field : operation.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (!field.isSetValueMark()) {
                         continue;
                     }
                     hasSetValueFields = true;
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireComma = true;
                 }
                 for (FieldInfo field : entity.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (field.isUpdateDateMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         result.append(currentSqlDate());
                         requireComma = true;
                     } else if (field.isVersionMark()) {
                         if (!handleVersionFieldOnUpdate()) {
                             continue;
                         }
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         appendNextVersionValue(result, entity, field);
                         requireComma = true;
                     }
                 }
                 
                 if (!hasSetValueFields) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "An update operation must has SetValue fields for generate the query", operation.getElement());
                     return new String[]{""};
                 }
 
             }
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.afterUpdateSetExpression(), "    ");
             }
             appendWhere(result, operation, customQuery, false);
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.value());
         }
         finalQuery = finalizeQuery(finalQuery, operation, customQuery);
         return finalQuery.split("\n");
     }
 
     @Override
     public String[] getCustomDeleteQuery(OperationInfo operation) {
         Query query = operation.getAnnotation(Query.class);
         CustomSqlQuery customQuery = operation.getAnnotation(CustomSqlQuery.class);
         String finalQuery;
         if (customQuery != null && hasQueryValue(customQuery.query())) {
             finalQuery = joinln(customQuery.query());
         } else if (query == null) {
             EntityInfo entity = operation.getEntity().getCombined();
             StringBuilder result = new StringBuilder();
             if (operation.isUseLogicalDeletion()) {
                 result.append("update");
                 if (customQuery != null && hasQueryValue(customQuery.from())) {
                     appendToQueryln(result, customQuery.from(), "    ");
                 } else {
                     appendToQueryln(result, getTableName(entity), "    ");
                 }
                 result.append("\nset\n");
                 boolean requireComma = false;
                 for (FieldInfo field : operation.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (!field.isSetValueMark()) {
                         continue;
                     } else if (field.isDeleteUserMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         result.append(getParameterValue(field));
                         requireComma = true;
                     }
                 }
                 for (FieldInfo field : entity.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (field.isDeleteDateMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         result.append(currentSqlDate());
                         requireComma = true;
                     } else if (field.isDeletionMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         appendDeletedValue(result, field);
                         requireComma = true;
                     } else if (field.isVersionMark()) {
                         if (!handleVersionFieldOnDelete()) {
                             continue;
                         }
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         appendNextVersionValue(result, entity, field);
                         requireComma = true;
                     }
                 }
             } else {
                 result.append("delete from");
                 if (customQuery != null && hasQueryValue(customQuery.from())) {
                     appendToQueryln(result, customQuery.from(), "    ");
                 } else {
                     appendToQueryln(result, getTableName(entity), "    ");
                 }
             }
             appendWhere(result, operation, customQuery, false);
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.value());
         }
         finalQuery = finalizeQuery(finalQuery, operation, customQuery);
         return finalQuery.split("\n");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Queries for diferent select operation">
     @Override
     public String[] getSelectOneQuery(OperationInfo operation) {
         Query query = operation.getAnnotation(Query.class);
         if (query == null) {
             return completeQuery(null, operation, false, false);
         }
         return completeQuery(query.value(), operation, false, false);
 
     }
 
     @Override
     public String[] getSelectManyQuery(OperationInfo operation) {
         Query query = operation.getAnnotation(Query.class);
         if (query == null) {
             return completeQuery(null, operation, false, false);
         }
         return completeQuery(query.value(), operation, false, false);
     }
 
     @Override
     public String[] getSelectPageQuery(OperationInfo operation) {
         PageQueries pageQueries = operation.getAnnotation(PageQueries.class);
         if (pageQueries == null) {
             Query query = operation.getAnnotation(Query.class);
             String[] result;
             if (query == null) {
                 result = completeQuery(null, operation, false, true);
             } else {
                 result = completeQuery(query.value(), operation, false, true);
             }
 
             if (result != null && result.length > 0) {
                 return envolveInSelectPage(result);
             } else {
                 return result;
             }
         }
         return completeQuery(pageQueries.selectPage(), operation, false, true);
     }
 
     @Override
     public String[] getSelectPageCountQuery(OperationInfo operation) {
         PageQueries pageQueries = operation.getAnnotation(PageQueries.class);
         if (pageQueries == null) {
             Query query = operation.getAnnotation(Query.class);
             String[] result;
             if (query == null) {
                 result = completeQuery(null, operation, true, true);
             } else {
                 result = completeQuery(query.value(), operation, true, true);
             }
             if (result != null) {
                 String s = joinsp(result);
                 s = s.replaceAll("\n", " ");
                 s = s.toLowerCase();
                 if (!s.matches("\\s*select\\s+count\\s*\\(.*")) {
                     String[] r = new String[result.length + 2];
                     r[0] = "select count(*) from (";
                     System.arraycopy(result, 0, r, 1, result.length);
                     r[r.length - 1] = ")";
                     return r;
                 }
             }
             return result;
         }
         return completeQuery(pageQueries.selectCount(), operation, true, true);
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Entity queries">
     @Override
     public String[] getEntitySelectByIdQuery(EntityInfo entity, OperationInfo operation) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             result.append("select");
             boolean requireComma = false;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 }
                 if (requireComma) {
                     result.append(",\n    ");
                 } else {
                     result.append("\n    ");
                 }
                 result.append(getColumnNameForSelect(field, null));
                 requireComma = true;
             }
             result.append("\nfrom");
             appendToQueryln(result, getTableNameForSelect(entity, null), "    ");
             boolean requireWhere = true;
             boolean requireAnd = false;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 } else if (field.isIdentifier()) {
                     if (requireWhere) {
                         result.append("\nwhere\n");
                         requireWhere = false;
                     }
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireAnd = true;
                 } else if (field.isDeletionMark()) {
                     if (!entity.isUseLogicalDeletion()) {
                         continue;
                     }
                     if (requireWhere) {
                         result.append("\nwhere\n");
                         requireWhere = false;
                     }
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     appendNotDeleted(result, field);
                     requireAnd = true;
                 }
             }
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.insert());
         }
         finalQuery = finalizeEntityQuery(finalQuery, entity, operation);
         return finalQuery.split("\n");
     }
 
     @Override
     public String[] getEntityInsertQuery(EntityInfo entity, OperationInfo operation) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             result.append("insert into");
             appendToQueryln(result, getTableName(entity), "    ");
             result.append("\n(\n");
             boolean requireComma = false;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 } else if (field.isInsertDateMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 } else if (field.isIdentifier()) {
                     
                     if (!includeIdOnInsert(entity, field)) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 } else if (field.isInsertUserMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 } else if (field.isUpdateDateMark()) {
                     continue;
                 } else if (field.isUpdateUserMark()) {
                     continue;
                 } else if (field.isDeleteDateMark()) {
                     continue;
                 } else if (field.isDeleteUserMark()) {
                     continue;
                 } else if (field.isDeletionMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 } else if (field.isVersionMark()) {
                     if (!handleVersionFieldOnInsert()) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 } else {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 }
             }
 
             result.append("\n) values (\n");
             requireComma = false;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 } else if (field.isInsertDateMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(currentSqlDate());
                     requireComma = true;
                 } else if (field.isIdentifier()) {
                     if (!includeIdOnInsert(entity, field)) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     appendIdNextValue(result, entity, field);
                     requireComma = true;
                 } else if (field.isInsertUserMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getParameterValue(field));
                     requireComma = true;
                 } else if (field.isUpdateDateMark()) {
                     continue;
                 } else if (field.isUpdateUserMark()) {
                     continue;
                 } else if (field.isDeleteDateMark()) {
                     continue;
                 } else if (field.isDeleteUserMark()) {
                     continue;
                 } else if (field.isDeletionMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     appendNotDeletedValue(result, field);
                     requireComma = true;
                 } else if (field.isVersionMark()) {
                     if (!handleVersionFieldOnInsert()) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     appendInitialVersionValue(result, entity, field);
                     requireComma = true;
                 } else {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getParameterValue(field));
                     requireComma = true;
                 }
             }
 
             result.append("\n)");
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.insert());
         }
         finalQuery = finalizeEntityQuery(finalQuery, entity, operation);
         return finalQuery.split("\n");
     }
 
     @Override
     public String[] getEntityLastInsertedIdQuery(EntityInfo entity, OperationInfo operation) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             finalQuery = joinln(getIdCurrentValue(entity, entity.getFirstIdField()));
         } else {
             finalQuery = joinln(query.lastInsertedId());
         }
         finalQuery = finalizeEntityQuery(finalQuery, entity, operation);
         if (finalQuery != null) {
             return finalQuery.split("\n");
         } else {
             return null;
         }
     }
 
     @Override
     public String[] getEntityUpdateQuery(EntityInfo entity, OperationInfo operation) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             result.append("update");
             appendToQueryln(result, getTableName(entity), "    ");
             result.append("\nset\n");
             boolean requireComma = false;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 } else if (field.isIdentifier()) {
                     continue;
                 } else if (field.isUpdateDateMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(currentSqlDate());
                     requireComma = true;
                 } else if (field.isUpdateUserMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireComma = true;
                 } else if (field.isInsertDateMark()) {
                     continue;
                 } else if (field.isInsertUserMark()) {
                     continue;
                 } else if (field.isDeleteDateMark()) {
                     continue;
                 } else if (field.isDeleteUserMark()) {
                     continue;
                 } else if (field.isDeletionMark()) {
                     continue;
                 } else if (field.isVersionMark()) {
                     if (!handleVersionFieldOnUpdate()) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     appendNextVersionValue(result, entity, field);
                     requireComma = true;
                 } else {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireComma = true;
                 }
             }
             boolean requireAnd = false;
             boolean requireWhere = true;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 } else if (field.isIdentifier()) {
                     if (requireWhere) {
                         result.append("\nwhere\n");
                         requireWhere = false;
                     }
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireAnd = true;
                 } else if (field.isDeletionMark()) {
                     if (!entity.isUseLogicalDeletion()) {
                         continue;
                     }
                     if (requireWhere) {
                         result.append("\nwhere\n");
                     }
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     appendNotDeleted(result, field);
                     requireAnd = true;
                 }
             }
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.update());
         }
         finalQuery = finalizeEntityQuery(finalQuery, entity, operation);
         return finalQuery.split("\n");
     }
 
     @Override
     public String[] getEntityMergeQuery(EntityInfo entity, OperationInfo operation) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             result.append("update");
             appendToQueryln(result, getTableName(entity), "    ");
             result.append("\n");
             appendStartSet(result);
             result.append("\n");
             boolean requireComma = false;
             boolean requireEndSetValueIfNotNull = false;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 } else if (field.isIdentifier()) {
                     continue;
                 } else if (field.isUpdateDateMark()) {
                     if (requireEndSetValueIfNotNull) {
                         appendEndSetValueIfNotNull(result, requireComma);
                         result.append("\n");
                         requireEndSetValueIfNotNull = false;
                     } else if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(currentSqlDate());
                     requireComma = true;
                 } else if (field.isUpdateUserMark()) {
                     if (requireEndSetValueIfNotNull) {
                         appendEndSetValueIfNotNull(result, requireComma);
                         result.append("\n");
                     } else if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     appendStartSetValueIfNotNull(result, field);
                     requireComma = true;
                     requireEndSetValueIfNotNull = true;
                 } else if (field.isInsertDateMark()) {
                     continue;
                 } else if (field.isInsertUserMark()) {
                     continue;
                 } else if (field.isDeleteDateMark()) {
                     continue;
                 } else if (field.isDeleteUserMark()) {
                     continue;
                 } else if (field.isDeletionMark()) {
                     continue;
                 } else if (field.isVersionMark()) {
                     if (!handleVersionFieldOnUpdate()) {
                         continue;
                     }
                     if (requireEndSetValueIfNotNull) {
                         appendEndSetValueIfNotNull(result, requireComma);
                         result.append("\n");
                         requireEndSetValueIfNotNull = false;
                     } else if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     appendNextVersionValue(result, entity, field);
                     requireComma = true;
                 } else {
                     if (requireEndSetValueIfNotNull) {
                         appendEndSetValueIfNotNull(result, requireComma);
                         result.append("\n");
                     } else if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     appendStartSetValueIfNotNull(result, field);
                     requireComma = true;
                     requireEndSetValueIfNotNull = true;
                 }
             }
             if (requireEndSetValueIfNotNull) {
                 appendEndSetValueIfNotNull(result, false);
             }
             appendEndSet(result, "\n");
             boolean requireAnd = false;
             boolean requireWhere = true;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 } else if (field.isIdentifier()) {
                     if (requireWhere) {
                         result.append("\nwhere\n");
                         requireWhere = false;
                     }
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireAnd = true;
                 } else if (field.isDeletionMark()) {
                     if (!entity.isUseLogicalDeletion()) {
                         continue;
                     }
                     if (requireWhere) {
                         result.append("\nwhere\n");
                     }
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     appendNotDeleted(result, field);
                     requireAnd = true;
                 }
             }
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.merge());
         }
         finalQuery = finalizeEntityQuery(finalQuery, entity, operation);
         return finalQuery.split("\n");
     }
 
     @Override
     public String[] getEntityDeleteByIdQuery(EntityInfo entity, OperationInfo operation) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             if (entity.isUseLogicalDeletion()) {
                 result.append("update");
                 appendToQueryln(result, getTableName(entity), "    ");
                 result.append("\nset\n");
                 boolean requireComma = false;
                 for (FieldInfo field : entity.getFields()) {
                     if (field.isManually()) {
                         continue;
                     } else if (field.isDeleteDateMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");                
                         result.append(currentSqlDate());
                         requireComma = true;
                     } else if (field.isDeletionMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         appendDeletedValue(result, field);
                         requireComma = true;
                     } else if (field.isDeleteUserMark()) {
                         // TODO: deleteUser
                         continue;
                     } else if (field.isVersionMark()) {
                         if (!handleVersionFieldOnDelete()) {
                             continue;
                         }
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         appendNextVersionValue(result, entity, field);
                         requireComma = true;
                     }
                 }
             } else {
                 result.append("delete from");
                 appendToQueryln(result, getTableName(entity), "    ");
             }
             boolean requireAnd = false;
             boolean requireWhere = true;
             for (FieldInfo field : entity.getFields()) {
                 if (field.isManually()) {
                     continue;
                 } else if (field.isIdentifier()) {
                     if (requireWhere) {
                         result.append("\nwhere\n");
                         requireWhere = false;
                     }
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireAnd = true;
                 } else if (field.isDeletionMark()) {
                     if (requireWhere) {
                         result.append("\nwhere\n");
                         requireWhere = false;
                     }
                     if (requireAnd) {
                         result.append("\n    and ");
                     } else {
                         result.append("    ");
                     }
                     appendNotDeleted(result, field);
                     requireAnd = true;
                 }
             }
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.deleteById());
         }
         finalQuery = finalizeEntityQuery(finalQuery, entity, operation);
         return finalQuery.split("\n");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="For generate entity queries">
     public String[] getTableName(EntityInfo entity) {
         return getMappedName(entity);
     }
     
     public abstract String getParameterValue(FieldInfo field);
     
     public boolean includeIdOnInsert(EntityInfo entity, FieldInfo field) {
         if (!field.isIdentifierAutogenerated()) {
             return true;
         }
         IdQueries idQueries = field.getAnnotation(IdQueries.class);
         if (idQueries != null) {
             return true;
         } else {
             idQueries = entity.getAnnotation(IdQueries.class);
             if (idQueries != null) {
                 return true;
             } else {
                 return !useAutoIncrementId();
             }
         }
     }
 
     public String[] getIdNextValue(EntityInfo entity, FieldInfo field) {
         if (!field.isIdentifierAutogenerated()) {
             String value = getParameterValue(field);
             return new String[]{value};
         }
         IdQueries idQueries = field.getAnnotation(IdQueries.class);
         if (idQueries != null) {
             return idQueries.selectNextValue();
         } else {
             idQueries = entity.getAnnotation(IdQueries.class);
             if (idQueries != null) {
                 return idQueries.selectNextValue();
             } else {
                 return getIdSequenceNextValue(entity, field);
             }
         }
     }
     
     public void appendIdNextValue(StringBuilder result, EntityInfo entity, FieldInfo field) {
         String[] nextValueQuery = getIdNextValue(entity, field);
         if (nextValueQuery != null) {
             for (int i = 0; i < nextValueQuery.length - 1; i++) {
                 result.append(nextValueQuery[i]);
                 result.append(" ");
             }
             result.append(nextValueQuery[nextValueQuery.length - 1]);
         } else {
             result.append("null");
         }
     }
 
     public String[] getIdCurrentValue(EntityInfo entity, FieldInfo field) {
         IdQueries idQueries = field.getAnnotation(IdQueries.class);
         if (idQueries != null) {
             return idQueries.selectCurrentValue();
         } else {
             idQueries = entity.getAnnotation(IdQueries.class);
             if (idQueries != null) {
                 return idQueries.selectCurrentValue();
             } else {
                 return getIdSequenceCurrentValue(entity, field);
             }
         }
 
     }
 
     public abstract String[] getIdSequenceNextValue(EntityInfo entity, FieldInfo field);
 
     public abstract String[] getIdSequenceCurrentValue(EntityInfo entity, FieldInfo field);
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Inline query elements managment">
     private static final Pattern comparatorPattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
     public String getConditionComparator(String comparatorRule, String template, FieldInfo field, CustomSqlQuery customQuery) {
         if (template == null) {
             return null;
         }
         String condition = null;
 
         Matcher matcher = comparatorPattern.matcher(template);
         StringBuffer sb = new StringBuffer(template.length());
         while (matcher.find()) {
             String rule = matcher.group(1);
             String value = getConditionElementValue(rule, field, customQuery);
             if (value != null) {
                 matcher.appendReplacement(sb, value);
             } else if ("condition".equals(rule)) {
                 if (condition == null) {
                     Matcher matcherc = comparatorPattern.matcher(comparatorRule);
                     StringBuffer sbc = new StringBuffer(comparatorRule.length());
                     while (matcherc.find()) {
                         String rulec = matcherc.group(1);
                         String valuec = getConditionElementValue(rulec, field, customQuery);
                         if (valuec == null) {
                             processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid comparator rule element: " + matcherc.group(), field.getElement());
                         } else {
                             matcherc.appendReplacement(sbc, valuec);
                         }
                     }
                     matcherc.appendTail(sbc);
                     condition = sbc.toString();
                 }
                 matcher.appendReplacement(sb, condition);
             } else {
                 processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid comparator element: " + matcher.group(), field.getElement());
             }
         }
         matcher.appendTail(sb);
         template = sb.toString();
 
         return template;
     }
 
     private static final Pattern finalizationPattern = Pattern.compile("\\{\\{(.*?)(?:\\:(.*?)(?:\\:(.*?))?)?\\}\\}");;
     public String finalizeQuery(String query, OperationInfo operation, CustomSqlQuery customQuery) {
         if (customQuery != null) {
             StringBuilder sb = new StringBuilder();
             appendToQueryln(sb, customQuery.beforeQuery(), "");
             if (sb.length() > 0) {
                 sb.append("\n");
             }
             sb.append(query);
             appendToQueryln(sb, customQuery.afterQuery(), "");
             query = sb.toString();
         }
         if (query == null) {
             return null;
         }
         Matcher matcher = finalizationPattern.matcher(query);
         StringBuffer result = new StringBuffer(query.length());
         while(matcher.find()) {
             String name = matcher.group(1);
             FieldInfo field = operation.getFieldByName(name);
             if (field == null) {
                 processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find the field used in the query element: " + matcher.group(), operation.getElement());
                 continue;
             }
 
             Comparator comparator;
             String comparatorTemplate;
             String template;
             String rule = matcher.group(2);
             String value = getConditionElementValue(rule, field, customQuery);
             if (value != null) {
                 matcher.appendReplacement(result, value);
                 continue;
             } else if (rule == null || "custom".equals(rule) || "CUSTOM".equals(rule)) {
                 comparator = getComparator(field);
                 comparatorTemplate = translateComparator(comparator);
                 template = getConditionTemplate(field, comparatorTemplate);
             } else if ("ifNull".equals(rule) || "IF_NULL".equals(rule)) {
                 StringBuilder sb = new StringBuilder();
                 String separator = matcher.group(3);
                 if (separator == null) {
                     separator = "";
                 }
                 appendConditionStartIfNull(sb, field, separator);
                 matcher.appendReplacement(result, sb.toString());
                 continue;
             } else if ("ifNotNull".equals(rule) || "IF_NOT_NULL".equals(rule)) {
                 StringBuilder sb = new StringBuilder();
                 String separator = matcher.group(3);
                 if (separator == null) {
                     separator = "";
                 }
                 appendConditionStartIfNotNull(sb, field, separator);
                 matcher.appendReplacement(result, sb.toString());
                 continue;
             } else if ("endIf".equals(rule) || "END_IF".equals(rule)) {
                 StringBuilder sb = new StringBuilder();
                 appendConditionEndIf(sb);
                 matcher.appendReplacement(result, sb.toString());
                 continue;
             } else {
                 if ("condition".equals(rule) || "CONDITION".equals(rule)) {
                     comparator = getComparator(field);
                 } else if ("=".equals(rule) || "==".equals(rule) || "equal".equals(rule) || "EQUAL".equals(rule)) {
                     comparator = Comparator.EQUAL;
                 } else if ("!=".equals(rule) || "<>".equals(rule) || "notEqual".equals(rule) || "NOT_EQUAL".equals(rule)) {
                     comparator = Comparator.NOT_EQUAL;
                 } else if ("=?".equals(rule) || "==?".equals(rule) || "equalNullable".equals(rule) || "EQUAL_NULLABLE".equals(rule)) {
                     comparator = Comparator.EQUAL_NULLABLE;
                 } else if ("<".equals(rule) || "smaller".equals(rule) || "SMALLER".equals(rule)) {
                     comparator = Comparator.SMALLER;
                 } else if (">".equals(rule) || "larger".equals(rule) || "LARGER".equals(rule)) {
                     comparator = Comparator.LARGER;
                 } else if ("<=".equals(rule) || "smallAs".equals(rule) || "SMALL_AS".equals(rule)) {
                     comparator = Comparator.SMALL_AS;
                 } else if (">=".equals(rule) || "largerAs".equals(rule) || "LARGER_AS".equals(rule)) {
                     comparator = Comparator.LARGER_AS;
                 } else if ("in".equals(rule) || "IN".equals(rule)) {
                     comparator = Comparator.IN;
                 } else if ("notIn".equals(rule) || "NOT_IN".equals(rule)) {
                     comparator = Comparator.NOT_IN;
                 } else if ("like".equals(rule) || "LIKE".equals(rule)) {
                     comparator = Comparator.LIKE;
                 } else if ("ilike".equals(rule) || "ILIKE".equals(rule) || "likeInsensitive".equals(rule) || "LIKE_INSENSITIVE".equals(rule)) {
                     comparator = Comparator.LIKE_INSENSITIVE;
                 } else if ("notIlike".equals(rule) || "NOT_ILIKE".equals(rule) || "notLikeInsensitive".equals(rule) || "NOT_LIKE_INSENSITIVE".equals(rule)) {
                     comparator = Comparator.NOT_LIKE_INSENSITIVE;
                 } else if ("startWith".equals(rule) || "START_WITH".equals(rule)) {
                     comparator = Comparator.START_WITH;
                 } else if ("notStartWith".equals(rule) || "NOT_START_WITH".equals(rule)) {
                     comparator = Comparator.NOT_START_WITH;
                 } else if ("endWith".equals(rule) || "END_WITH".equals(rule)) {
                     comparator = Comparator.END_WITH;
                 } else if ("notEndWith".equals(rule) || "NOT_END_WITH".equals(rule)) {
                     comparator = Comparator.NOT_END_WITH;
                 } else if ("istartWith".equals(rule) || "ISTART_WITH".equals(rule) || "startWithInsensitive".equals(rule) || "START_WITH_INSENSITIVE".equals(rule)) {
                     comparator = Comparator.START_WITH_INSENSITIVE;
                 } else if ("notIstartWith".equals(rule) || "NOT_ISTART_WITH".equals(rule) || "notStartWithInsensitive".equals(rule) || "NOT_START_WITH_INSENSITIVE".equals(rule)) {
                     comparator = Comparator.NOT_START_WITH_INSENSITIVE;
                 } else if ("iendWith".equals(rule) || "IEND_WITH".equals(rule) || "endWithInsensitive".equals(rule) || "END_WITH_INSENSITIVE".equals(rule)) {
                     comparator = Comparator.END_WITH_INSENSITIVE;
                 } else if ("notIendWith".equals(rule) || "NOT_IEND_WITH".equals(rule) || "notEndWithInsensitive".equals(rule) || "NOT_END_WITH_INSENSITIVE".equals(rule)) {
                     comparator = Comparator.NOT_END_WITH_INSENSITIVE;
                 } else if ("contains".equals(rule) || "CONTAINS".equals(rule)) {
                     comparator = Comparator.CONTAINS;
                 } else if ("notContains".equals(rule) || "NOT_CONTAINS".equals(rule)) {
                     comparator = Comparator.NOT_CONTAINS;
                 } else if ("icontains".equals(rule) || "ICONTAINST".equals(rule) || "containsInsensitive".equals(rule) || "CONTAINS_INSENSITIVE".equals(rule)) {
                     comparator = Comparator.CONTAINS_INSENSITIVE;
                 } else if ("notIcontains".equals(rule) || "NOT_ICONTAINS".equals(rule) || "notContainsInsensitive".equals(rule) || "NOT_CONTAINS_INSENSITIVE".equals(rule)) {
                     comparator = Comparator.NOT_CONTAINS_INSENSITIVE;
                 } else {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Invalid comparator rule used in: " + matcher.group(), operation.getElement());
                     continue;
                 }
                 comparatorTemplate = translateComparator(comparator);
                 template = comparatorTemplate;
             }
             String condition = getConditionComparator(comparatorTemplate, template, field, customQuery);
             matcher.appendReplacement(result, condition);
         }
         matcher.appendTail(result);
         return result.toString();
     }
 
     public String finalizeEntityQuery(String query, EntityInfo entity, OperationInfo operation) {
         return query;
     }
     //</editor-fold>
 }
