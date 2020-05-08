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
 package org.uaithne.generator.processors.sql;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.tools.Diagnostic;
 import org.uaithne.annotations.*;
 import org.uaithne.annotations.sql.CustomSqlQuery;
 import org.uaithne.generator.commons.*;
 
 public abstract class SqlMappersProcessor extends TemplateProcessor {
 
     //<editor-fold defaultstate="collapsed" desc="Complete query">
     public String[] completeQuery(String[] query, OperationInfo operation, boolean count) {
         CustomSqlQuery customQuery = operation.getAnnotation(CustomSqlQuery.class);
         String completed;
         if (query == null) {
             completed = completeQueryWithoutEnvolve(null, operation, count, customQuery);
         } else {
             completed = joinln(query);
             completed = completeQueryWithoutEnvolve(completed, operation, count, customQuery);
         }
         completed = finalizeQuery(completed, operation, customQuery);
 
         String[] result;
         if (completed == null) {
             result = null;
         } else {
             result = completed.split("\n");
         }
         if (limitToOneResult(operation)) {
             return envolveInSelectOneRow(result);
         } else {
             return result;
         }
 
     }
 
     public String completeQueryWithoutEnvolve(String query, OperationInfo operation, boolean count, CustomSqlQuery customQuery) {
         boolean addSelect = false;
         boolean addFrom = false;
         boolean addWhere = false;
         boolean addGroupBy = false;
         boolean addOrderBy = false;
         boolean ignoreQuery = false;
         boolean prepend = false;
         FieldInfo orderBy = null;
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
             if (query == null || query.isEmpty()) {
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
 
         if (prepend && !ignoreQuery) {
             appender.append(query);
         }
 
         if (addSelect) {
             appendSelect(appender, operation, entity, count, customQuery);
         }
         if (addFrom) {
             appendFrom(appender, entity, customQuery);
         }
         if (addWhere) {
             orderBy = appendWhere(appender, operation, addOrderBy, customQuery);
         }
         if (addGroupBy) {
             appendGroupBy(appender, operation, entity, customQuery);
         }
         if (addOrderBy && !count) {
             appendOrderBy(appender, orderBy, customQuery);
         }
 
         String result;
         if (ignoreQuery || prepend) {
             result = appender.toString();
         } else {
             result = appender.toString();
             if (query != null || !query.isEmpty()) {
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
             if (limitToOneResult(operation)) {
                 String limitOne = selectOneRowBeforeSelect();
                 if (limitOne != null && !limitOne.isEmpty()) {
                     result.append(limitOne);
                     result.append(" ");
                 }
             }
 
             if (selectPageOperation(operation)) {
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
                 appendSelectFields(result, operation, entity, count, customQuery);
             }
         }
     }
 
     public void appendSelectFields(StringBuilder result, OperationInfo operation, EntityInfo entity, boolean count, CustomSqlQuery customQuery) {
         boolean requireComma = false;
         List<FieldInfo> fields = entity.getFields();
         for (FieldInfo field : fields) {
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
         List<FieldInfo> extraFields = entity.getExtraFields();
         for (FieldInfo field : extraFields) {
             if (extraIncludeOnSelect(field)) {
                 if (requireComma) {
                     result.append(",\n    ");
                 } else {
                     result.append("\n    ");
                 }
                 result.append(getColumnNameForSelect(field, customQuery));
                 requireComma = true;
             }
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
             appendToQueryln(result, getTableNameForSelect(entity, customQuery), "    ");
         }
     }
 
     /**
      * Return the field with the OrderBy annotation
      */
     public FieldInfo appendWhere(StringBuilder query, OperationInfo operation, boolean onlyOneOrderBy, CustomSqlQuery customQuery) {
         FieldInfo orderBy = null;
         ArrayList<FieldInfo> fields = operation.getFields();
         boolean useDeletionMarks = useLogicalDeletion(operation);
 
         if (customQuery != null) {
             if (hasQueryValue(customQuery.where())) {
                 query.append("\n");
                 appendStartWhere(query);
                 appendToQueryln(query, customQuery.beforeWhereExpression(), "    ");
                 appendToQueryln(query, customQuery.where(), "    ");
                 appendToQueryln(query, customQuery.afterWhereExpression(), "    ");
                 query.append("\n");
                 appendEndWhere(query);
 
                 if (!fields.isEmpty() || useDeletionMarks) {
                     for (FieldInfo field : fields) {
                         if (field.isOrderBy()) {
                             if (orderBy != null && onlyOneOrderBy) {
                                 processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                         "Only one field marked with OrderBy annotation is allowed when the query is automatically generated",
                                         field.getElement());
                             } else {
                                 orderBy = field;
                             }
                             continue;
                         }
                     }
                 }
                 return orderBy;
             }
         }
 
         int whereCount = 0;
         boolean hasOptionals = false;
         StringBuilder result = new StringBuilder();
         boolean requireAnd = false;
         for (FieldInfo field : fields) {
             if (field.isManually()) {
                 continue;
             }
             if (field.getAnnotation(SetValue.class) != null) {
                 continue;
             }
             if (field.isOrderBy()) {
                 if (orderBy != null && onlyOneOrderBy) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                             "Only one field marked with OrderBy annotation is allowed when the query is automatically generated",
                             field.getElement());
                 } else {
                     orderBy = field;
                 }
                 continue;
             }
             boolean optional = field.isOptional();
             if (optional) {
                 if (!requireAnd) {
                     result.append("    ");
                 }
                 appendConditionStartIfNotNull(result, field);
                 if (requireAnd) {
                     result.append(" and\n");
                     result.append("    ");
                 } else {
                     result.append(" ");
                 }
                 hasOptionals = true;
             } else {
                 if (requireAnd) {
                     result.append(" and\n");
                 }
                 result.append("    ");
             }
             appendCondition(result, field, customQuery);
             whereCount++;
             if (optional) {
                 appendConditionEndIf(result);
             }
             requireAnd = true;
         }
         if (limitToOneResult(operation)) {
             String limitOne = selectOneRowBeforeWhere();
             if (limitOne != null && !limitOne.isEmpty()) {
                 if (requireAnd) {
                     result.append(" and\n");
                 }
                 result.append("    ");
                 result.append(limitOne);
                 requireAnd = true;
                 whereCount++;
             }
         }
         if (selectPageOperation(operation)) {
             String page = selectPageBeforeWhere();
             if (page != null && !page.isEmpty()) {
                 if (requireAnd) {
                     result.append(" and\n");
                 }
                 result.append("    ");
                 result.append(page);
                 requireAnd = true;
                 whereCount++;
             }
         }
 
         if (useDeletionMarks) {
             List<FieldInfo> fieldsWithExtras = operation.getEntity().getCombined().getFieldsWithExtras();
             for (FieldInfo field : fieldsWithExtras) {
                 if (field.isManually()) {
                     continue;
                 }
                 if (field.isDeletionMark()) {
                     if (requireAnd) {
                         result.append(" and\n");
                     }
                     result.append("    ");
                     appendNotDeleted(result, field);
                     requireAnd = true;
                     whereCount++;
                 }
             }
         }
 
         if (whereCount > 0) {
             if (customQuery != null && (hasQueryValue(customQuery.beforeWhereExpression()) || hasQueryValue(customQuery.afterWhereExpression())) ) {
                 query.append("\n");
                 appendStartWhere(query);
                 appendToQueryln(query, customQuery.beforeWhereExpression(), "    ");
                 query.append("\n");
                 query.append(result);
                 appendToQueryln(query, customQuery.afterWhereExpression(), "    ");
                 query.append("\n");
                 appendEndWhere(query);
             } else {
                 if (hasOptionals) {
                     query.append("\n");
                     appendStartWhere(query);
                     query.append("\n");
                     query.append(result);
                     query.append("\n");
                     appendEndWhere(query);
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
             query.append("\n");
             appendEndWhere(query);
         }
         return orderBy;
     }
 
     public Comparator getComparator(FieldInfo field) {
         UseComparator uc = field.getAnnotation(UseComparator.class);
         Comparator comparator = null;
 
         if (uc != null) {
             comparator = uc.value();
         }
 
         if (comparator == null) {
             if ("java.util.List".equals(field.getDataType().getQualifiedName()) || "java.util.ArrayList".equals(field.getDataType().getQualifiedName())) {
                 comparator = Comparator.IN;
             } else {
                 comparator = Comparator.EQUAL;
             }
         }
         return comparator;
     }
 
     public String getConditionTemplate(FieldInfo field, String comparatorTemplate) {
         UseCustomComparator ucc = field.getAnnotation(UseCustomComparator.class);
         String template;
         if (ucc != null) {
             template = ucc.value();
         } else {
             template = comparatorTemplate;
         }
         return template;
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
 
     public abstract void appendOrderBy(StringBuilder result, FieldInfo orderBy, CustomSqlQuery customQuery);
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Utils for generate the select query">
     public void appendToQuery(StringBuilder query, String[] array, String prefix) {
         if (!hasQueryValue(array)) {
             return;
         }
         for (int i = 0; i < array.length - 1; i++) {
             String s = array[i];
             query.append(prefix).append(s).append("\n");
         }
         query.append(prefix);
         String s = array[array.length - 1];
         query.append(s);
         query.append(" ");
     }
 
     public void appendToQueryln(StringBuilder query, String[] array, String prefix) {
         if (!hasQueryValue(array)) {
             return;
         }
         if (query.length() > 0) {
             if (query.charAt(query.length() - 1) != '\n') {
                 query.append("\n");
             }
         }
         for (int i = 0; i < array.length - 1; i++) {
             String s = array[i];
             query.append(prefix).append(s).append("\n");
         }
         query.append(prefix);
         String s = array[array.length - 1];
         query.append(s);
         query.append(" ");
     }
 
     public String joinln(String[] strings) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < strings.length - 1; i++) {
             sb.append(strings[i]);
             sb.append("\n");
         }
         sb.append(strings[strings.length - 1]);
         return sb.toString();
     }
 
     public String joinsp(String[] strings) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < strings.length - 1; i++) {
             sb.append(strings[i]);
             sb.append("\n");
         }
         sb.append(strings[strings.length - 1]);
         return sb.toString();
     }
 
     public boolean hasQueryValue(String[] array) {
         if (array == null || array.length <= 0) {
             return false;
         }
         if (array.length == 1) {
             String s = array[0];
             if (s == null || s.isEmpty()) {
                 return false;
             }
         }
         return true;
     }
 
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
             result = entity.getDataType().getSimpleName().split("\n");
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
 
     public boolean limitToOneResult(OperationInfo operation) {
         if (operation.getOperationKind() != OperationKind.SELECT_ONE) {
             return false;
         }
         SelectOne selectOne = operation.getAnnotation(SelectOne.class);
         if (selectOne != null) {
             return selectOne.limit();
         }
         return false;
     }
 
     public boolean selectPageOperation(OperationInfo operation) {
         return operation.getOperationKind() == OperationKind.SELECT_PAGE;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Entity rules">
     public boolean useLogicalDeletion(EntityInfo entity) {
         IgnoreLogicalDeletion ignoreLogicalDeletion = entity.getAnnotation(IgnoreLogicalDeletion.class);
         if (ignoreLogicalDeletion != null) {
             return false;
         }
         return entity.isLogicalDeletion();
     }
 
     public boolean useLogicalDeletion(OperationInfo operation) {
         IgnoreLogicalDeletion ignoreLogicalDeletion = operation.getAnnotation(IgnoreLogicalDeletion.class);
         if (ignoreLogicalDeletion != null) {
             return false;
         }
         EntityInfo entity = operation.getEntity();
         if (entity != null) {
             return useLogicalDeletion(entity.getCombined());
         }
         return false;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="For generate queries">
     public abstract void appendStartWhere(StringBuilder result);
 
     public abstract void appendEndWhere(StringBuilder result);
 
     public abstract void appendStartSet(StringBuilder result);
 
     public abstract void appendEndSet(StringBuilder result);
 
     public abstract void appendConditionStartIfNull(StringBuilder result, FieldInfo field);
 
     public abstract void appendConditionStartIfNotNull(StringBuilder result, FieldInfo field);
 
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
 
     public abstract String selectPageBeforeWhere();
 
     public abstract String[] envolveInSelectOneRow(String[] query);
 
     public abstract String selectOneRowBeforeSelect();
 
     public abstract String selectOneRowBeforeWhere();
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Deletion mark managment">
     public void appendNotDeleted(StringBuilder result, FieldInfo field) {
         if (field.isOptional()) {
             result.append(getColumnName(field));
             result.append("is not null");
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
 
     //<editor-fold defaultstate="collapsed" desc="Custom operations queries">
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
             appendToQueryln(result, getTableName(entity), "    ");
             result.append("\n(");
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.beforeInsertIntoExpression(), "    ");
             }
 
             if (customQuery != null && hasQueryValue(customQuery.insertInto())) {
                 appendToQueryln(result, customQuery.insertInto(), "    ");
             } else {
                 result.append("\n");
                 boolean requireComma = false;
                 if (insertQueryIncludeId()) {
                     List<FieldInfo> idFields = entity.getIdFields();
                     for (FieldInfo field : idFields) {
                         if (omitOnInsert(field)) {
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
                 List<FieldInfo> entityFieldsWithExtras = entity.getFieldsWithExtras();
                 for (FieldInfo field : entityFieldsWithExtras) {
                     if (omitOnInsert(field)) {
                         continue;
                     }
                     if (!field.isDeletionMark()) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 }
                 List<FieldInfo> fields = operation.getFields();
                 for (FieldInfo field : fields) {
                     if (omitOnInsert(field)) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     requireComma = true;
                 }
                 List<FieldInfo> extraFields = operation.getExtraFields();
                 for (FieldInfo field : extraFields) {
                     boolean process = false;
                     if (field.getAnnotation(InsertDate.class) != null) {
                         process = true;
                     } else if (field.getAnnotation(InsertUser.class) != null) {
                         process = true;
                     } else if (field.isDeletionMark()) {
                         process = true;
                     }
                     if (process) {
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
                 if (insertQueryIncludeId()) {
                     List<FieldInfo> idFields = entity.getIdFields();
                     for (FieldInfo field : idFields) {
                         if (omitOnInsert(field)) {
                             continue;
                         }
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         if (field.getAnnotation(InsertDate.class) != null) {
                             result.append(currentSqlDate());
                         } else {
                             String nextValueQuery[] = getIdNextValue(entity, field);
                             for (int i = 0; i < nextValueQuery.length - 1; i++) {
                                 result.append(nextValueQuery[i]);
                                 result.append(" ");
                             }
                             result.append(nextValueQuery[nextValueQuery.length - 1]);
                         }
                         requireComma = true;
                     }
                 }
                 List<FieldInfo> entityFieldsWithExtras = entity.getFieldsWithExtras();
                 for (FieldInfo field : entityFieldsWithExtras) {
                     if (omitOnInsert(field)) {
                         continue;
                     }
                     if (!field.isDeletionMark()) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     appendNotDeletedValue(result, field);
                     requireComma = true;
                 }
                 List<FieldInfo> fields = operation.getFields();
                 for (FieldInfo field : fields) {
                     if (omitOnInsert(field)) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     if (field.getAnnotation(InsertDate.class) != null) {
                         result.append(currentSqlDate());
                     } else if (field.isDeletionMark()) {
                         appendNotDeletedValue(result, field);
                     } else {
                         result.append(getParameterValue(field));
                     }
                     requireComma = true;
                 }
                 List<FieldInfo> extraFields = operation.getExtraFields();
                 for (FieldInfo field : extraFields) {
                     if (field.getAnnotation(InsertDate.class) != null) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(currentSqlDate());
                         requireComma = true;
                     } else if (field.getAnnotation(InsertUser.class) != null) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getParameterValue(field));
                         requireComma = true;
                     } else if (field.isDeletionMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         appendNotDeletedValue(result, field);
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
         if (customQuery != null) {
             StringBuilder sb = new StringBuilder();
             appendToQueryln(sb, customQuery.beforeQuery(), "");
             if (sb.length() > 0) {
                 sb.append("\n");
             }
             sb.append(finalQuery);
             appendToQueryln(sb, customQuery.afterQuery(), "");
             finalQuery = sb.toString();
         }
         return finalQuery.split("\n");
     }
 
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
             appendToQueryln(result, getTableName(entity), "    ");
             result.append("\nset");
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.beforeUpdateSetExpression(), "    ");
             }
 
             if (customQuery != null && hasQueryValue(customQuery.updateSet())) {
                 appendToQueryln(result, customQuery.updateSet(), "    ");
             } else {
                 result.append("\n");
                 boolean requireComma = false;
                 List<FieldInfo> fields = operation.getFields();
                 boolean hasSetValueFields = false;
                 for (FieldInfo field : fields) {
                     if (omitOnUpdate(field)) {
                         continue;
                     }
                     if (field.getAnnotation(SetValue.class) == null) {
                         continue;
                     }
                     hasSetValueFields = true;
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     if (field.getAnnotation(UpdateDate.class) != null) {
                         result.append(currentSqlDate());
                     } else {
                         result.append(getParameterValue(field));
                     }
                     requireComma = true;
                 }
 
                 if (!hasSetValueFields) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "An update operation must has SetValue fields for generate the query", operation.getElement());
                     return new String[]{""};
                 }
 
                 List<FieldInfo> extraFields = operation.getExtraFields();
                 for (FieldInfo field : extraFields) {
                     if (field.getAnnotation(SetValue.class) == null) {
                         continue;
                     }
                     if (field.getAnnotation(UpdateDate.class) != null) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         result.append(currentSqlDate());
                         requireComma = true;
                     } else if (field.getAnnotation(UpdateUser.class) != null) {
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
             }
 
             if (customQuery != null) {
                 appendToQueryln(result, customQuery.afterUpdateSetExpression(), "    ");
             }
            appendWhere(result, operation, false, customQuery);
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.value());
         }
         finalQuery = finalizeQuery(finalQuery, operation, customQuery);
         if (customQuery != null) {
             StringBuilder sb = new StringBuilder();
             appendToQueryln(sb, customQuery.beforeQuery(), "");
             if (sb.length() > 0) {
                 sb.append("\n");
             }
             sb.append(finalQuery);
             appendToQueryln(sb, customQuery.afterQuery(), "");
             finalQuery = sb.toString();
         }
         return finalQuery.split("\n");
     }
 
     public String[] getCustomDeleteQuery(OperationInfo operation) {
         Query query = operation.getAnnotation(Query.class);
         CustomSqlQuery customQuery = operation.getAnnotation(CustomSqlQuery.class);
         String finalQuery;
         if (customQuery != null && hasQueryValue(customQuery.query())) {
             finalQuery = joinln(customQuery.query());
         } else if (query == null) {
             EntityInfo entity = operation.getEntity().getCombined();
             StringBuilder result = new StringBuilder();
             if (useLogicalDeletion(operation)) {
                 result.append("update");
                 appendToQueryln(result, getTableName(entity), "    ");
                 result.append("\nset\n");
                 boolean requireComma = false;
                 for (FieldInfo field : entity.getFieldsWithExtras()) {
                     if (field.isManually()) {
                         continue;
                     }
                     if (field.getAnnotation(DeleteDate.class) != null) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         result.append(currentSqlDate());
                     } else if (field.isDeletionMark()) {
                         if (requireComma) {
                             result.append(",\n");
                         }
                         result.append("    ");
                         result.append(getColumnName(field));
                         result.append(" = ");
                         appendDeletedValue(result, field);
                     } else {
                         // Todo: a falta de añadir el usuario de la operación
                         continue;
                     }
                     requireComma = true;
                 }
                 for (FieldInfo field : operation.getFieldsWithExtras()) {
                     if (field.isManually()) {
                         continue;
                     }
                     if (field.getAnnotation(SetValue.class) == null) {
                         continue;
                     }
                     if (field.getAnnotation(DeleteUser.class) == null) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                 }
             } else {
                 result.append("delete from");
                 appendToQueryln(result, getTableName(entity), "    ");
             }
             appendWhere(result, operation, false, customQuery);
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.value());
         }
         finalQuery = finalizeQuery(finalQuery, operation, customQuery);
         if (customQuery != null) {
             StringBuilder sb = new StringBuilder();
             appendToQueryln(sb, customQuery.beforeQuery(), "");
             if (sb.length() > 0) {
                 sb.append("\n");
             }
             sb.append(finalQuery);
             appendToQueryln(sb, customQuery.afterQuery(), "");
             finalQuery = sb.toString();
         }
         return finalQuery.split("\n");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Queries for diferent select operation">
     public String[] getSelectOneQuery(OperationInfo operation) {
         Query query = operation.getAnnotation(Query.class);
         if (query == null) {
             return completeQuery(null, operation, false);
         }
         return completeQuery(query.value(), operation, false);
 
     }
 
     public String[] getSelectManyQuery(OperationInfo operation) {
         Query query = operation.getAnnotation(Query.class);
         if (query == null) {
             return completeQuery(null, operation, false);
         }
         return completeQuery(query.value(), operation, false);
     }
 
     public String[] getSelectPageQuery(OperationInfo operation) {
         PageQueries pageQueries = operation.getAnnotation(PageQueries.class);
         if (pageQueries == null) {
             Query query = operation.getAnnotation(Query.class);
             String[] result;
             if (query == null) {
                 result = completeQuery(null, operation, false);
             } else {
                 result = completeQuery(query.value(), operation, false);
             }
 
             if (result != null && result.length > 0) {
                 return envolveInSelectPage(result);
             } else {
                 return result;
             }
         }
         return completeQuery(pageQueries.selectPage(), operation, false);
     }
 
     public String[] getSelectPageCountQuery(OperationInfo operation) {
         PageQueries pageQueries = operation.getAnnotation(PageQueries.class);
         if (pageQueries == null) {
             Query query = operation.getAnnotation(Query.class);
             String[] result;
             if (query == null) {
                 result = completeQuery(null, operation, true);
             } else {
                 result = completeQuery(query.value(), operation, true);
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
         return completeQuery(pageQueries.selectCount(), operation, true);
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Entity queries">
     public String[] getEntitySelectByIdQuery(EntityInfo entity) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             result.append("select");
             boolean requireComma = false;
             List<FieldInfo> fields = entity.getFields();
             for (FieldInfo field : fields) {
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
             List<FieldInfo> extraFields = entity.getExtraFields();
             for (FieldInfo field : extraFields) {
                 if (extraIncludeOnSelect(field)) {
                     if (requireComma) {
                         result.append(",\n    ");
                     } else {
                         result.append("\n    ");
                     }
                     result.append(getColumnNameForSelect(field, null));
                     requireComma = true;
                 }
             }
             result.append("\nfrom");
             appendToQueryln(result, getTableNameForSelect(entity, null), "    ");
             List<FieldInfo> idFields = entity.getIdFields();
             boolean requireWhere = true;
             if (!idFields.isEmpty()) {
                 result.append("\nwhere\n");
                 requireWhere = false;
                 boolean requireAnd = false;
                 for (FieldInfo field : idFields) {
                     if (field.isManually()) {
                         continue;
                     }
                     if (requireAnd) {
                         result.append(" and\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireAnd = true;
                 }
             }
             if (useLogicalDeletion(entity)) {
                 List<FieldInfo> fieldsWithExtras = entity.getFieldsWithExtras();
                 if (!fieldsWithExtras.isEmpty()) {
                     if (requireWhere) {
                         result.append("\nwhere\n");
                     }
                     boolean requireAnd = !requireWhere;
                     for (FieldInfo field : fieldsWithExtras) {
                         if (field.isManually()) {
                             continue;
                         }
                         if (field.isDeletionMark()) {
                             if (requireAnd) {
                                 result.append(" and\n");
                             }
                             result.append("    ");
                             appendNotDeleted(result, field);
                             requireAnd = true;
                         }
                     }
                 }
             }
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.insert());
         }
         finalQuery = finalizeQuery(finalQuery, entity);
         return finalQuery.split("\n");
     }
 
     public String[] getEntityInsertQuery(EntityInfo entity) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             result.append("insert into");
             appendToQueryln(result, getTableName(entity), "    ");
             result.append("\n(\n");
             boolean requireComma = false;
             if (insertQueryIncludeId()) {
                 List<FieldInfo> idFields = entity.getIdFields();
                 for (FieldInfo field : idFields) {
                     if (omitOnInsert(field)) {
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
             List<FieldInfo> mandatoryAndOptionalFields = entity.getMandatoryAndOptionalFields();
             for (FieldInfo field : mandatoryAndOptionalFields) {
                 if (omitOnInsert(field)) {
                     continue;
                 }
                 if (requireComma) {
                     result.append(",\n");
                 }
                 result.append("    ");
                 result.append(getColumnName(field));
                 requireComma = true;
             }
             List<FieldInfo> extraFields = entity.getExtraFields();
             for (FieldInfo field : extraFields) {
                 boolean process = false;
                 if (field.getAnnotation(InsertDate.class) != null) {
                     process = true;
                 } else if (field.getAnnotation(InsertUser.class) != null) {
                     process = true;
                 } else if (field.isDeletionMark()) {
                     process = true;
                 }
                 if (process) {
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
             if (insertQueryIncludeId()) {
                 List<FieldInfo> idFields = entity.getIdFields();
                 for (FieldInfo field : idFields) {
                     if (omitOnInsert(field)) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     if (field.getAnnotation(InsertDate.class) != null) {
                         result.append(currentSqlDate());
                     } else {
                         String nextValueQuery[] = getIdNextValue(entity, field);
                         for (int i = 0; i < nextValueQuery.length - 1; i++) {
                             result.append(nextValueQuery[i]);
                             result.append(" ");
                         }
                         result.append(nextValueQuery[nextValueQuery.length - 1]);
                     }
                     requireComma = true;
                 }
             }
             for (FieldInfo field : mandatoryAndOptionalFields) {
                 if (omitOnInsert(field)) {
                     continue;
                 }
                 if (requireComma) {
                     result.append(",\n");
                 }
                 result.append("    ");
                 if (field.getAnnotation(InsertDate.class) != null) {
                     result.append(currentSqlDate());
                 } else if (field.isDeletionMark()) {
                     appendNotDeletedValue(result, field);
                 } else {
                     result.append(getParameterValue(field));
                 }
                 requireComma = true;
             }
             for (FieldInfo field : extraFields) {
                 if (field.getAnnotation(InsertDate.class) != null) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(currentSqlDate());
                     requireComma = true;
                 } else if (field.getAnnotation(InsertUser.class) != null) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getParameterValue(field));
                     requireComma = true;
                 } else if (field.isDeletionMark()) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     appendNotDeletedValue(result, field);
                     requireComma = true;
                 }
             }
 
             result.append("\n)");
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.insert());
         }
         finalQuery = finalizeQuery(finalQuery, entity);
         return finalQuery.split("\n");
     }
 
     public String[] getEntityLastInsertedIdQuery(EntityInfo entity) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             finalQuery = joinln(getIdCurrentValue(entity, entity.getFirstIdField()));
         } else {
             finalQuery = joinln(query.lastInsertedId());
         }
         finalQuery = finalizeQuery(finalQuery, entity);
         return finalQuery.split("\n");
     }
 
     public String[] getEntityUpdateQuery(EntityInfo entity) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             result.append("update");
             appendToQueryln(result, getTableName(entity), "    ");
             result.append("\nset\n");
             boolean requireComma = false;
             List<FieldInfo> mandatoryAndOptionalFields = entity.getMandatoryAndOptionalFields();
             for (FieldInfo field : mandatoryAndOptionalFields) {
                 if (omitOnUpdate(field)) {
                     continue;
                 }
                 if (requireComma) {
                     result.append(",\n");
                 }
                 result.append("    ");
                 result.append(getColumnName(field));
                 result.append(" = ");
                 if (field.getAnnotation(UpdateDate.class) != null) {
                     result.append(currentSqlDate());
                 } else {
                     result.append(getParameterValue(field));
                 }
                 requireComma = true;
             }
             List<FieldInfo> extraFields = entity.getExtraFields();
             for (FieldInfo field : extraFields) {
                 if (field.getAnnotation(UpdateDate.class) != null) {
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(currentSqlDate());
                     requireComma = true;
                 } else if (field.getAnnotation(UpdateUser.class) != null) {
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
             List<FieldInfo> idFields = entity.getIdFields();
             if (!idFields.isEmpty()) {
                 result.append("\nwhere\n");
                 boolean requireAnd = false;
                 for (FieldInfo field : idFields) {
                     if (omitOnUpdate(field)) {
                         continue;
                     }
                     if (requireAnd) {
                         result.append(" and\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireAnd = true;
                 }
             }
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.update());
         }
         finalQuery = finalizeQuery(finalQuery, entity);
         return finalQuery.split("\n");
     }
 
     public String[] getEntityMergeQuery(EntityInfo entity) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             result.append("update");
             appendToQueryln(result, getTableName(entity), "    ");
             result.append("\n");
             appendStartSet(result);
             result.append("\n");
             List<FieldInfo> mandatoryAndOptionalFields = entity.getMandatoryAndOptionalFields();
             for (FieldInfo field : mandatoryAndOptionalFields) {
                 if (omitOnUpdate(field)) {
                     continue;
                 }
                 result.append("    ");
                 appendConditionStartIfNotNull(result, field);
                 result.append(getColumnName(field));
                 result.append(" = ");
                 if (field.getAnnotation(UpdateDate.class) != null) {
                     result.append(currentSqlDate());
                 } else {
                     result.append(getParameterValue(field));
                 }
                 result.append(",");
                 appendConditionEndIf(result);
                 result.append("\n");
             }
             List<FieldInfo> extraFields = entity.getExtraFields();
             for (FieldInfo field : extraFields) {
                 if (field.getAnnotation(UpdateDate.class) != null) {
                     result.append("    ");
                     appendConditionStartIfNotNull(result, field);
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(currentSqlDate());
                     result.append(",");
                     appendConditionEndIf(result);
                     result.append("\n");
                 } else if (field.getAnnotation(UpdateUser.class) != null) {
                 result.append("    ");
                     appendConditionStartIfNotNull(result, field);
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     result.append(",");
                     appendConditionEndIf(result);
                     result.append("\n");
                 }
             }
             appendEndSet(result);
             List<FieldInfo> idFields = entity.getIdFields();
             if (!idFields.isEmpty()) {
                 result.append("\nwhere\n");
                 boolean requireAnd = false;
                 for (FieldInfo field : idFields) {
                     if (omitOnUpdate(field)) {
                         continue;
                     }
                     if (requireAnd) {
                         result.append(" and\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireAnd = true;
                 }
             }
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.merge());
         }
         finalQuery = finalizeQuery(finalQuery, entity);
         return finalQuery.split("\n");
     }
 
     public String[] getEntityDeleteByIdQuery(EntityInfo entity) {
         EntityQueries query = entity.getAnnotation(EntityQueries.class);
         String finalQuery;
         if (query == null) {
             StringBuilder result = new StringBuilder();
             if (useLogicalDeletion(entity)) {
                 result.append("update");
                 appendToQueryln(result, getTableName(entity), "    ");
                 result.append("\nset\n");
                 boolean requireComma = false;
                 for (FieldInfo field : entity.getFieldsWithExtras()) {
                     if (omitOnDelete(field)) {
                         continue;
                     }
                     if (requireComma) {
                         result.append(",\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     if (field.getAnnotation(DeleteDate.class) != null) {
                         result.append(currentSqlDate());
                     } else if (field.isDeletionMark()) {
                         appendDeletedValue(result, field);
                     } else {
                         result.append(getParameterValue(field));
                     }
                     requireComma = true;
                 }
             } else {
                 result.append("delete from");
                 appendToQueryln(result, getTableName(entity), "    ");
             }
             List<FieldInfo> idFields = entity.getIdFields();
             boolean requireAnd = false;
             if (!idFields.isEmpty()) {
                 result.append("\nwhere\n");
                 for (FieldInfo field : idFields) {
                     if (field.isManually()) {
                         continue;
                     }
                     if (requireAnd) {
                         result.append(" and\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(getParameterValue(field));
                     requireAnd = true;
                 }
             }
             boolean requireWhere = idFields.isEmpty();
             for (FieldInfo field : entity.getFieldsWithExtras()) {
                     if (field.isManually()) {
                         continue;
                     }
                     if (!field.isDeletionMark()) {
                         continue;
                     }
                     if (requireWhere) {
                         result.append("\nwhere\n");
                     }
                     if (requireAnd) {
                         result.append(" and\n");
                     }
                     result.append("    ");
                     result.append(getColumnName(field));
                     result.append(" = ");
                     result.append(falseValue());
                     requireAnd = true;
                 }
             finalQuery = result.toString();
         } else {
             finalQuery = joinln(query.deleteById());
         }
         finalQuery = finalizeQuery(finalQuery, entity);
         return finalQuery.split("\n");
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Utils for generate the entity queries">
     public String[] getTableName(EntityInfo entity) {
         MappedName mappedName = entity.getAnnotation(MappedName.class);
         if (mappedName == null) {
             return entity.getDataType().getSimpleName().split("\n");
         } else {
             return joinln(mappedName.value()).split("\n");
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="For generate entity queries">
     public abstract boolean insertQueryIncludeId();
 
     public abstract String getParameterValue(FieldInfo field);
 
     public String[] getIdNextValue(EntityInfo entity, FieldInfo field) {
         IdQueries idQueries = field.getAnnotation(IdQueries.class);
         if (idQueries != null) {
             return idQueries.selectNextValue();
         } else {
             idQueries = entity.getAnnotation(IdQueries.class);
             if (idQueries != null) {
                 return idQueries.selectNextValue();
             } else {
                 return getDefaultIdNextValue(entity, field);
             }
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
                 return getDefaultIdCurrentValue(entity, field);
             }
         }
 
     }
 
     public abstract String[] getDefaultIdNextValue(EntityInfo entity, FieldInfo field);
 
     public abstract String[] getDefaultIdCurrentValue(EntityInfo entity, FieldInfo field);
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Managment of the extra fields">
     public boolean extraIncludeOnSelect(FieldInfo field) {
         if (field.isManually()) {
             return false;
         }
         if (field.getAnnotation(InsertDate.class) != null) {
             return true;
         }
         if (field.getAnnotation(InsertUser.class) != null) {
             return true;
         }
         if (field.getAnnotation(UpdateDate.class) != null) {
             return true;
         }
         if (field.getAnnotation(UpdateUser.class) != null) {
             return true;
         }
         if (field.getAnnotation(DeleteDate.class) != null) {
             return true;
         }
         if (field.getAnnotation(DeleteUser.class) != null) {
             return true;
         }
         if (field.getAnnotation(Version.class) != null) {
             return true;
         }
         if (field.isDeletionMark()) {
             return true;
         }
         return false;
     }
 
     public boolean omitOnInsert(FieldInfo field) {
         if (field.isManually()) {
             return true;
         }
         if (field.getAnnotation(InsertDate.class) != null) {
             return false;
         }
         if (field.getAnnotation(InsertUser.class) != null) {
             return false;
         }
         if (field.getAnnotation(UpdateDate.class) != null) {
             return true;
         }
         if (field.getAnnotation(UpdateUser.class) != null) {
             return true;
         }
         if (field.getAnnotation(DeleteDate.class) != null) {
             return true;
         }
         if (field.getAnnotation(DeleteUser.class) != null) {
             return true;
         }
         if (field.getAnnotation(Version.class) != null) {
             return true;
         }
         return false;
     }
 
     public boolean omitOnUpdate(FieldInfo field) {
         if (field.isManually()) {
             return true;
         }
         if (field.getAnnotation(UpdateDate.class) != null) {
             return false;
         }
         if (field.getAnnotation(UpdateUser.class) != null) {
             return false;
         }
         if (field.getAnnotation(InsertDate.class) != null) {
             return true;
         }
         if (field.getAnnotation(InsertUser.class) != null) {
             return true;
         }
         if (field.getAnnotation(DeleteDate.class) != null) {
             return true;
         }
         if (field.getAnnotation(DeleteUser.class) != null) {
             return true;
         }
         if (field.getAnnotation(Version.class) != null) {
             return true;
         }
         if (field.isDeletionMark()) {
             return true;
         }
         return false;
     }
 
     public boolean omitOnDelete(FieldInfo field) {
         if (field.isManually()) {
             return true;
         }
         if (field.getAnnotation(DeleteDate.class) != null) {
             return false;
         }
         if (field.getAnnotation(DeleteUser.class) != null) {
             // Todo: a falta de añadir el usuario de la operación
             // return false;
             return true;
         }
         if (field.isDeletionMark()) {
             return false;
         }
         return true;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Inline query elements managment">
     private static Pattern comparatorPattern;
     public String getConditionComparator(String comparatorRule, String template, FieldInfo field, CustomSqlQuery customQuery) {
         if (comparatorPattern == null) {
             comparatorPattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
         }
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
 
     private static Pattern finalizationPattern;
     public String finalizeQuery(String query, OperationInfo operation, CustomSqlQuery customQuery) {
         if (finalizationPattern == null) {
             finalizationPattern = Pattern.compile("\\{\\{(.*?)(?:\\:(.*?))?\\}\\}");
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
                 appendConditionStartIfNull(sb, field);
                 matcher.appendReplacement(result, sb.toString());
                 continue;
             } else if ("ifNotNull".equals(rule) || "IF_NOT_NULL".equals(rule)) {
                 StringBuilder sb = new StringBuilder();
                 appendConditionStartIfNotNull(sb, field);
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
 
     public String finalizeQuery(String query, EntityInfo entity) {
         return query;
     }
     //</editor-fold>
 }
