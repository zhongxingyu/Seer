 /*
  * Copyright 2004-2010 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.eclipse.model.impl;
 
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jdt.core.IJavaProject;
 import org.seasar.dolteng.core.entity.ColumnMetaData;
 import org.seasar.dolteng.core.entity.FieldMetaData;
 import org.seasar.dolteng.core.entity.impl.BasicColumnMetaData;
 import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
 import org.seasar.dolteng.core.types.TypeMapping;
 import org.seasar.dolteng.core.types.TypeMappingRegistry;
 import org.seasar.dolteng.eclipse.DoltengCore;
 import org.seasar.dolteng.eclipse.convention.NamingUtil;
 import org.seasar.dolteng.eclipse.model.EntityMappingRow;
 import org.seasar.dolteng.eclipse.model.RootModel;
 import org.seasar.dolteng.eclipse.model.TreeContent;
 import org.seasar.dolteng.eclipse.part.DatabaseView;
 import org.seasar.dolteng.eclipse.scaffold.ScaffoldImportsSet;
 import org.seasar.dolteng.eclipse.util.NameConverter;
 import org.seasar.dolteng.eclipse.util.ProjectUtil;
 import org.seasar.framework.convention.NamingConvention;
 import org.seasar.framework.util.ClassUtil;
 import org.seasar.framework.util.StringUtil;
 
 /**
  * @author seiichi
  * 
  */
 public class HeadMeisaiScaffoldModel implements RootModel {
 
     private static final Map<String, String> formPkeyArgsFormat = new HashMap<String, String>();
     static {
         formPkeyArgsFormat.put("boolean", "Boolean.parseBoolean(%1$s.%2$s), ");
         formPkeyArgsFormat.put("java.lang.Boolean",
                 "Boolean.valueOf(%1$s.%2$s), ");
         formPkeyArgsFormat.put("double", "Double.parseDouble(%1$s.%2$s), ");
         formPkeyArgsFormat.put("java.lang.Double",
                 "Double.valueOf(%1$s.%2$s), ");
         formPkeyArgsFormat.put("float", "Float.parseDouble(%1$s.%2$s), ");
         formPkeyArgsFormat.put("java.lang.Float", "Float.valueOf(%1$s.%2$s), ");
         formPkeyArgsFormat.put("short", "Short.parseShort(%1$s.%2$s), ");
         formPkeyArgsFormat.put("java.lang.Short", "Short.valueOf(%1$s.%2$s), ");
         formPkeyArgsFormat.put("int", "Integer.parseInt(%1$s.%2$s), ");
         formPkeyArgsFormat.put("java.lang.Integer",
                 "Integer.valueOf(%1$s.%2$s), ");
         formPkeyArgsFormat.put("long", "Long.parseLong(%1$s.%2$s), ");
         formPkeyArgsFormat.put("java.lang.Long", "Long.valueOf(%1$s.%2$s), ");
         formPkeyArgsFormat.put("java.math.BigDecimal",
                 "new java.math.BigDecimal(%1$s.%2$s), ");
     }
 
     private String typeName;
 
     private NamingConvention namingConvention;
 
     private EntityMappingRow[] mappings;
     
     // 選択されたテーブルの列情報
     private EntityMappingRow[] selectedColumnsMappings = new EntityMappingRow[0];
     
     // 明細テーブルの名前
     private EntityMappingRow meisaiTableName;
     
     // 明細テーブルの列情報
     private EntityMappingRow[] meisaiColumnsMappings;
     
     
     
     
     // 検索条件に付与するためのORDER BY句
     private String orderbyString;
     
     // 検索条件に付与するためのORDER BY句における列名
     private String orderbyStringColumn;
     
     // 検索条件のArguments句
     private String conditionArguments;
     
     // 検索条件のArguments句（定数アノテーション用）
     private String conditionArgumentsTeisuAnnotation;
     
     // 検索条件に与えるためのパラメータ（定義文用）
     private String conditionParam;
     
     // 検索条件に与えるためのパラメータ（呼び出し用）
     private String conditionCallParam;
     
     
     
     
     
     
     
     
     
     
     
 
     private Map<String, String> configs;
 
     private IJavaProject project;
 
     public HeadMeisaiScaffoldModel(Map<String, String> configs, TableNode node,
             Map<Integer, String[]> selectedColumns,
             String meisaiTableName, Map<Integer, String[]> meisaiColumns) {
         super();
         
         this.configs = configs;
         this.configs.put("pagingpackagename", "paging");
         this.configs.put("dtopackagename", "dto");
                 
         initialize(node, selectedColumns, meisaiTableName, meisaiColumns);
     }
 
     @SuppressWarnings("unchecked")
     protected void initialize(TableNode node, Map<Integer, String[]> selectedColumns,
             String meisaiTableName, Map<Integer, String[]> meisaiColumns) {
         ProjectNode n = (ProjectNode) node.getRoot();
         this.project = n.getJavaProject();
 
         // データベースビューの起動元のテーブル情報を設定します。
         {
             List<TreeContent> columns = Arrays.asList(node.getChildren());
             Collections.sort(columns);
             List rows = new ArrayList(columns.size());
             for (TreeContent content : columns) {
                 if (content instanceof ColumnNode) {
                     ColumnNode cn = (ColumnNode) content;
                     ColumnMetaData meta = cn.getColumnMetaData();
                     rows.add(createEntityMappingRow(meta));
                 }
             }
             setMappings((EntityMappingRow[]) rows.toArray(new EntityMappingRow[rows
                     .size()]));
         }
         
         if (selectedColumns != null) {
             // 選択されたテーブルの列情報を設定します。
             List<TreeContent> columns = Arrays.asList(node.getChildren());
             Collections.sort(columns);
             List rows = new ArrayList(selectedColumns.size());
             for (int i = 0; i < selectedColumns.size(); i++) {
                 for (TreeContent content : columns) {
                     if (content instanceof ColumnNode) {
                         ColumnNode cn = (ColumnNode) content;
                         ColumnMetaData meta = cn.getColumnMetaData();
                         if (meta.getName().equalsIgnoreCase(selectedColumns.get(i)[0])) {
                             rows.add(createEntityMappingRow(meta));
                             break;
                         }
                     }
                 }
             }
             setSelectedColumnsMappings(
                     (EntityMappingRow[]) rows.toArray(
                             new EntityMappingRow[rows.size()]));
         }
 
         {
             // 検索条件に付与するためのORDER BY句を作成します。
             orderbyString = "";
             orderbyStringColumn = "";
             conditionArguments = "";
             conditionArgumentsTeisuAnnotation = "\"";
             for (int i = 0; i < selectedColumnsMappings.length; i++) {
                 if (i > 0) {
                     orderbyString += "And";
                     orderbyStringColumn += ",";
                     conditionArguments += ",";
                     conditionArgumentsTeisuAnnotation += ",";
                 }
                 orderbyString += pascalize(selectedColumnsMappings[i].getSqlColumnName());
                 orderbyStringColumn += selectedColumnsMappings[i].getSqlColumnName();
                 conditionArguments += "\"" + "arg" + pascalize(selectedColumnsMappings[i].getSqlColumnName()) + "\"";
                 conditionArgumentsTeisuAnnotation += "arg" + pascalize(selectedColumnsMappings[i].getSqlColumnName());
             }
             conditionArgumentsTeisuAnnotation += "\"";
             
             // 検索条件に与えるためのパラメータ
             conditionParam = "";
             conditionCallParam = "";
             for (int i = 0; i < selectedColumnsMappings.length; i++) {
                 if (i > 0) {
                     conditionParam += ", ";
                     conditionCallParam += ", ";
                 }
                 String javaClassName = selectedColumnsMappings[i].getJavaClassName();
                 if (javaClassName.startsWith("java.lang")) {
                     javaClassName = ClassUtil.getShortClassName(javaClassName);
                 }
                 conditionParam += javaClassName + " arg" + pascalize(selectedColumnsMappings[i].getSqlColumnName());
                 conditionCallParam += "text" + pascalize(selectedColumnsMappings[i].getSqlColumnName());
             }
         }
 
         // 明細
         // 明細のテーブル名を設定します。
         {
             ColumnMetaData meta = new BasicColumnMetaData();
             meta.setName(meisaiTableName);
             setMeisaiTableName(createEntityMappingRow(meta));
         }
         
         // 明細のカラム情報を設定します。
         {
             List<TreeContent> columns = Arrays.asList(DatabaseView.getTableNode(meisaiTableName).getChildren());
             Collections.sort(columns);
             List rows = new ArrayList(columns.size());
             for (TreeContent content : columns) {
                 if (content instanceof ColumnNode) {
                     ColumnNode cn = (ColumnNode) content;
                     ColumnMetaData meta = cn.getColumnMetaData();
                     rows.add(createEntityMappingRow(meta));
                 }
             }
             setMeisaiColumnsMappings(
                     (EntityMappingRow[])
                     rows.toArray(new EntityMappingRow[rows.size()]));
         }
     }
     
     /**
      * text に指定された文字列をパスカル形式に変換します。
      * @param text 変換対象の文字列
      * @return パスカル形式に変換された文字列
      */
     private String pascalize(String text) {
         int length = text.length();
         StringBuffer sb = new StringBuffer();
         boolean isFirstChar = true;
         for (int i = 0; i < length; i++) {
             if (isFirstChar) {
                 sb.append(Character.toUpperCase(text.charAt(i)));
                 isFirstChar = false;
             } else {
                 if(text.charAt(i) == '-' || text.charAt(i) == '_') {
                     isFirstChar = true;
                 } else {
                     sb.append(Character.toLowerCase(text.charAt(i)));
                 }
             }
         }
         return sb.toString();
     }
 
     private EntityMappingRow createEntityMappingRow(ColumnMetaData column) {
         FieldMetaData field = new BasicFieldMetaData();
         TypeMappingRegistry registry = DoltengCore
                 .getTypeMappingRegistry(this.project);
         TypeMapping mapping = registry.toJavaClass(column);
         field.setModifiers(Modifier.PUBLIC);
         field.setDeclaringClassName(mapping.getJavaClassName());
         field.setName(StringUtil.decapitalize(NameConverter.toCamelCase(column
                 .getName())));
 
         return new BasicEntityMappingRow(column, field, registry);
     }
 
     /**
      * @param typeName
      *            The typeName to set.
      */
     public void setTypeName(String typeName) {
         this.typeName = typeName;
     }
 
     /**
      * @return Returns the typeName.
      */
     public String getTypeName() {
         return typeName;
     }
 
     /**
      * @return Returns the namingConvention.
      */
     public NamingConvention getNamingConvention() {
         return namingConvention;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.seasar.dolteng.eclipse.model.impl.RootModel#setNamingConvention(org
      * .seasar.framework.convention.NamingConvention)
      */
     public void setNamingConvention(NamingConvention namingConvention) {
         this.namingConvention = namingConvention;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.model.impl.RootModel#getConfigs()
      */
     public Map<String, String> getConfigs() {
         return configs;
     }
 
     /**
      * @return Returns the mappings.
      */
     public EntityMappingRow[] getMappings() {
         return mappings;
     }
 
     /**
      * @param mappings
      *            The mappings to set.
      */
     public void setMappings(EntityMappingRow[] mappings) {
         this.mappings = mappings;
     }
     
     public EntityMappingRow[] getSelectedColumnsMappings() {
         return selectedColumnsMappings;
     }
     
     public void setSelectedColumnsMappings(EntityMappingRow[] selectedColumnsMappings) {
         this.selectedColumnsMappings = selectedColumnsMappings;
     }
     
     
     
     
     public EntityMappingRow getMeisaiTableName() {
         return meisaiTableName;
     }
     
     public void setMeisaiTableName(EntityMappingRow meisaiTableName) {
         this.meisaiTableName = meisaiTableName;
     }
     
     
     
     
     
     
     public EntityMappingRow[] getMeisaiColumnsMappings() {
         return meisaiColumnsMappings;
     }
     
     public void setMeisaiColumnsMappings(EntityMappingRow[] meisaiColumnsMappings) {
         this.meisaiColumnsMappings = meisaiColumnsMappings;
     }
     
     
     
     
     
     
     
     /**
      * 検索条件に付与するためのORDER BY句を取得します。
      * @return
      */
     public String getOrderbyString() {
         return orderbyString;
     }
     
     /**
      * 検索条件に付与するためのORDER BY句を設定します。
      * @param orderbyString
      */
     public void setOrderbyString(String orderbyString) {
         this.orderbyString = orderbyString;
     }
     
     /**
      * 検索条件に付与するためのORDER BY句における列名を取得します。
      * @return
      */
     public String getOrderbyStringColumn() {
         return orderbyStringColumn;
     }
     
     /**
      * 検索条件に付与するためのORDER BY句における列名を設定します。
      * @param orderbyStringColumn
      */
     public void setOrderbyStringColumn(String orderbyStringColumn) {
         this.orderbyStringColumn = orderbyStringColumn;
     }
     
     public String getConditionArguments() {
         return conditionArguments;
     }
     
     public void setConditionArguments(String conditionArguments) {
         this.conditionArguments = conditionArguments;
     }
     
     public String getConditionArgumentsTeisuAnnotation() {
         return conditionArgumentsTeisuAnnotation;
     }
 
     public void setConditionArgumentsTeisuAnnotation(
             String conditionArgumentsTeisuAnnotation) {
         this.conditionArgumentsTeisuAnnotation = conditionArgumentsTeisuAnnotation;
     }
 
     public String getConditionParam() {
         return conditionParam;
     }
     
     public void setConditionParam(String conditionParam) {
         this.conditionParam = conditionParam;
     }
     
     public String getConditionCallParam() {
         return conditionCallParam;
     }
     
     public void setConditionCallParam(String conditionCallParam) {
         this.conditionCallParam = conditionCallParam;
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     /**
      * 条件列が存在する場合は、trueを返却します。
      * @return 条件列が存在する場合は、true
      */
     public boolean isSelectedExisted() {
         if (selectedColumnsMappings != null && selectedColumnsMappings.length > 0) {
             return true;
         }
         return false;
     }
     
     
     
     
     
     
 
 
     public String getImports() {
         ScaffoldImportsSet imports = new ScaffoldImportsSet();
         imports.addAll(mappings);
         return imports.getImportsString();
     }
 
     public String getDateImport() {
         ScaffoldImportsSet imports = new ScaffoldImportsSet();
         imports.addDate(mappings);
         return imports.getImportsString();
     }
 
     public String getDateAndSelectedImports() {
         ScaffoldImportsSet imports = new ScaffoldImportsSet();
         imports.addDate(mappings);
         imports.addAll(selectedColumnsMappings);
         return imports.getImportsString();
     }
 
     public String getNullableAndPrimaryKeyImports() {
         ScaffoldImportsSet imports = new ScaffoldImportsSet();
         imports.addRequired(mappings);
         return imports.getImportsString();
     }
 
     public String getDateAndNullableAndPrimaryKeyImports() {
         ScaffoldImportsSet imports = new ScaffoldImportsSet();
         imports.addDate(mappings);
         imports.addRequired(mappings);
         return imports.getImportsString();
     }
 
     public String getImportsInMeisai() {
         ScaffoldImportsSet imports = new ScaffoldImportsSet();
         imports.addAll(meisaiColumnsMappings);
         return imports.getImportsString();
     }
 
     
     
     
     
     
     
     
     
     
     
     public String getJavaClassName(EntityMappingRow row) {
         return ClassUtil.getShortClassName(row.getJavaClassName());
     }
 
     public String createPkeyMethodArgs(boolean includeVersion) {
         StringBuffer stb = new StringBuffer();
         boolean is = false;
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()
                     || (includeVersion && NamingUtil.isVersionNo(row
                             .getSqlColumnName()))) {
                 String s = row.getJavaClassName();
                 if (s.startsWith("java.lang")) {
                     s = ClassUtil.getShortClassName(s);
                 }
                 stb.append(s);
                 stb.append(' ');
                 stb.append(row.getJavaFieldName());
                 stb.append(',');
                 is |= true;
             }
         }
         if (is) {
             stb.setLength(stb.length() - 1);
         }
         return stb.toString();
 
     }
 
     public String createPkeyMethodArgs() {
         return createPkeyMethodArgs(false);
     }
 
     public String createPkeyMethodArgNames() {
         if (isTigerResource()) {
             return createAnnotationArgNames();
         }
         return createConstArgNames();
     }
 
     private String createAnnotationArgNames() {
         List<EntityMappingRow> prows = new ArrayList<EntityMappingRow>();
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()) {
                 prows.add(row);
             }
         }
 
         StringBuffer stb = new StringBuffer();
         if (0 < prows.size()) {
             boolean is = 1 < prows.size();
             if (is) {
                 stb.append('{');
             }
             for (EntityMappingRow row : prows) {
                 stb.append('"');
                 stb.append(row.getSqlColumnName());
                 stb.append('"');
                 stb.append(',');
             }
             stb.setLength(stb.length() - 1);
             if (is) {
                 stb.append('}');
             }
         }
         return stb.toString();
     }
 
     private String createConstArgNames() {
         StringBuffer stb = new StringBuffer();
         boolean is = false;
         stb.append('"');
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()) {
                 stb.append(row.getSqlColumnName());
                 stb.append(',');
                 is = true;
             }
         }
         if (is) {
             stb.setLength(stb.length() - 1);
         }
         stb.append('"');
         return stb.toString();
     }
 
     /**
      * ヘッダテーブルにおけるプライマリキーの数を取得します。
      * @return ヘッダテーブルにおけるプライマリキーの数
      */
     public int countPkeys() {
         int result = 0;
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()) {
                 result++;
             }
         }
         return result;
     }
     
     /**
      * 明細テーブルにおけるプライマリキーの数を取得します。
      * @return 明細テーブルにおけるプライマリキーの数
      */
     public int countPkeyInMeisai() {
         int meisaiPrimaryCount = 0;
         for (EntityMappingRow row : meisaiColumnsMappings) {
             if (row.isPrimaryKey()) {
                 meisaiPrimaryCount++;
             }
         }
         return meisaiPrimaryCount;
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
 
     public String createPkeyMethodCallArgs(boolean includeVersion, String prefix) {
         StringBuffer stb = new StringBuffer();
         boolean is = false;
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()
                     || (includeVersion && NamingUtil.isVersionNo(row
                             .getSqlColumnName()))) {
                 stb.append(prefix);
                 stb.append("get");
                 stb.append(StringUtil.capitalize(row.getJavaFieldName()));
                 stb.append("()");
                 stb.append(',');
                 stb.append(' ');
                 is = true;
             }
         }
         if (is) {
             stb.setLength(stb.length() - 2);
         }
         return stb.toString();
     }
 
     public String createPkeyMethodCallArgs(boolean includeVersion) {
         return createPkeyMethodCallArgs(includeVersion, "");
     }
 
     public String createPkeyMethodCallArgs() {
         return createPkeyMethodCallArgs(false);
     }
 
     public String createFormPkeyMethodCallArgsCopy(String formName) {
         return createFormPkeyMethodCallArgsCopy(formName, false);
     }
 
     public String createFormPkeyMethodCallArgsCopy(String formName,
             boolean includeVersion) {
         StringBuffer stb = new StringBuffer();
         boolean is = false;
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()
                     || (includeVersion && NamingUtil.isVersionNo(row
                             .getSqlColumnName()))) {
                 String type = row.getJavaClassName();
                 String format = formPkeyArgsFormat.get(type);
                 if (format == null) {
                     format = "%1$s.%2$s, ";
                 }
                 Formatter formatter = new Formatter(stb);
                 formatter.format(format, formName, row.getJavaFieldName());
                 formatter.close();
                 is = true;
             }
         }
         if (is) {
             stb.setLength(stb.length() - 2);
         }
         return stb.toString();
     }
 
     public String createPkeyMethodCallArgsCopy(boolean includeVersion) {
         StringBuffer stb = new StringBuffer();
         boolean is = false;
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()
                     || (includeVersion && NamingUtil.isVersionNo(row
                             .getSqlColumnName()))) {
                 stb.append(row.getJavaFieldName());
                 stb.append(',');
                 stb.append(' ');
                 is = true;
             }
         }
         if (is) {
             stb.setLength(stb.length() - 2);
         }
         return stb.toString();
     }
 
     public String createPkeyMethodCallArgsCopy() {
         return createPkeyMethodCallArgsCopy(false);
     }
 
     public String createPkeyLink(boolean includeVersion) {
         StringBuffer stb = new StringBuffer();
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()
                     || (includeVersion && NamingUtil.isVersionNo(row
                             .getSqlColumnName()))) {
                 stb.append("&amp;");
                 stb.append(row.getJavaFieldName());
                 stb.append('=');
                 stb.append(row.getJavaFieldName());
             }
         }
         return stb.toString();
     }
 
     public String createPkeyLink() {
         return createPkeyLink(false);
     }
 
     public boolean isTigerResource() {
         return ProjectUtil.enableAnnotation(this.project);
     }
 
     public boolean isMappingsContainsDate() {
         for (EntityMappingRow mapping : mappings) {
             if (mapping.isDate()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isMappingsContainsRequired() {
         for (EntityMappingRow mapping : mappings) {
             if (mapping.isNullable() == false || mapping.isPrimaryKey()) {
                 return true;
             }
         }
         return false;
     }
 
     public String toAsType(EntityMappingRow row) {
         String result = "Object";
         try {
             String s = row.getJavaClassName();
             s = DoltengCore.getAsTypeResolver(this.project).resolve(s);
             if (StringUtil.isEmpty(s) == false) {
                 result = s;
             }
         } catch (Exception e) {
             DoltengCore.log(e);
         }
         return result;
     }
 
     public boolean isVersionColumn(EntityMappingRow row) {
         return NamingUtil.isVersionNo(row.getSqlColumnName());
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     public String createMeisaiPkeyMethodArgNames() {
         if (isTigerResource()) {
             return createMeisaiAnnotationArgNames();
         }
         return createMeisaiConstArgNames();
     }
     
     private String createMeisaiAnnotationArgNames() {
         List<EntityMappingRow> prows = new ArrayList<EntityMappingRow>();
         for (EntityMappingRow row : meisaiColumnsMappings) {
             if (row.isPrimaryKey()) {
                 prows.add(row);
             }
         }
 
         StringBuffer stb = new StringBuffer();
         if (0 < prows.size()) {
             boolean is = 1 < prows.size();
             if (is) {
                 stb.append('{');
             }
             for (EntityMappingRow row : prows) {
                 stb.append('"');
                 stb.append(row.getSqlColumnName());
                 stb.append('"');
                 stb.append(',');
             }
             stb.setLength(stb.length() - 1);
             if (is) {
                 stb.append('}');
             }
         }
         return stb.toString();
     }
 
     private String createMeisaiConstArgNames() {
         StringBuffer stb = new StringBuffer();
         boolean is = false;
         stb.append('"');
         for (EntityMappingRow row : meisaiColumnsMappings) {
             if (row.isPrimaryKey()) {
                 stb.append(row.getSqlColumnName());
                 stb.append(',');
                 is = true;
             }
         }
         if (is) {
             stb.setLength(stb.length() - 1);
         }
         stb.append('"');
         return stb.toString();
     }
     
     public int countMeisaiPkeys() {
         int result = 0;
         for (EntityMappingRow row : meisaiColumnsMappings) {
             if (row.isPrimaryKey()) {
                 result++;
             }
         }
         return result;
     }
 
     public String createMeisaiPkeyMethodArgs() {
         return createMeisaiPkeyMethodArgs(false);
     }
 
     /**
      * 明細テーブルにおける単一のレコードを取得するための、メソッドのパラメタを取得します。
      * @param includeVersion バージョン列を含める場合、true を指定します。
      * @return 明細テーブルにおける単一のレコードを取得するための、メソッドのパラメタ
      */
     public String createMeisaiPkeyMethodArgs(boolean includeVersion) {
         StringBuffer stb = new StringBuffer();
         boolean is = false;
         for (EntityMappingRow row : meisaiColumnsMappings) {
             if (row.isPrimaryKey()
                     || (includeVersion && NamingUtil.isVersionNo(row
                             .getSqlColumnName()))) {
                 String s = row.getJavaClassName();
                 if (s.startsWith("java.lang")) {
                     s = ClassUtil.getShortClassName(s);
                 }
                 stb.append(s);
                 stb.append(' ');
                 stb.append(row.getJavaFieldName());
                 stb.append(',');
                 //is |= true;
                 is = true;
             }
         }
         if (is) {
             stb.setLength(stb.length() - 1);
         }
         return stb.toString();
     }
 
     /**
      * S2Dao の SQL 文で使用する条件を取得します。
      * @param fieldName 列名
      * @param typeName タイプ名
      * @return S2Dao の SQL 文で使用する条件
      */
     public String getS2DaoCondition(String fieldName, String typeName) {
         if (typeName.equals("String")) {
             return "LIKE concat(/*" + fieldName + "*/' ','%')";
         } else if (typeName.equals("Integer") || typeName.equals("BigDecimal")) {
             return ">= /*" + fieldName + "*/'0'";
         } else if (typeName.equals("Date") || typeName.equals("Timestamp")) {
             return ">= /*" + fieldName + "*/'1900/1/1'";
         }
         return "= fieldName";
     }
 
 
 
 
 
 
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     public String createHeadPkeyName() {
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()) {
                 return row.getJavaFieldName();
             }
         }
         return "pkey";
     }
 
     /**
      * ヘッダの１レコードに対応する明細テーブル上の単一のレコードを取得する
      * ためのプライマリキー名を取得します。
      * @return ヘッダの１レコードに対応する明細テーブル上の単一のレコード
      *         を取得するためのプライマリキー名
      */
     public String createMeisaiPkeyName() {
         if (countPkeyInMeisai() == 1) {
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     return row.getJavaFieldName();
                 }
             }
         } else {
             int i = 0;
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     if (i == 1) {
                         return row.getJavaFieldName();
                     }
                     i++;
                 }
             }
         }
         return "by";
     }
     
     /**
      * ヘッダの１レコードに対応する明細テーブル上の単一のレコードを取得する
      * ためのプライマリキーのSQLカラム名を取得します。
      * @return ヘッダの１レコードに対応する明細テーブル上の単一のレコード
      *         を取得するためのプライマリキーのSQLカラム名
      */
     public String createMeisaiPkeySqlColumnName() {
         if (countPkeyInMeisai() == 1) {
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     return row.getSqlColumnName();
                 }
             }
         } else {
             int i = 0;
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     if (i == 1) {
                         return row.getSqlColumnName();
                     }
                     i++;
                 }
             }
         }
         return "by";
     }
     
     
     
     
     /**
      * 明細テーブルのカラム名に対するクラスを取得します。
      * @param meisaiMapping 明細テーブルのカラム名
      * @return 明細テーブルのカラム名に対するクラス
      */
     public String getMeisaiJavaClassName(EntityMappingRow meisaiMapping) {
         // 明細テーブルのカラム名がヘッダテーブルのプライマリキーと対応している場合は、
         // ヘッダテーブルのクラス名を取得します。
         if (meisaiMapping.getSqlColumnName().equalsIgnoreCase(this.configs.get("table_rdb") + "_ID")) {
             for (EntityMappingRow mapping : mappings) {
                 if (mapping.getSqlColumnName().equalsIgnoreCase("ID")) {
                     String s = mapping.getJavaClassName();
                     if (s.startsWith("java.lang")) {
                         s = ClassUtil.getShortClassName(s);
                     }
                     return s;
                 }
             }
         }
         String s = meisaiMapping.getJavaClassName();
         if (s.startsWith("java.lang")) {
             s = ClassUtil.getShortClassName(s);
         }
         return s;
     }
 
     /**
      * 与えられた明細のカラム名がヘッダのプライマリキーに対応している場合、true を返却します。
      * @param meisaiMapping 与えられた明細のカラム
      * @return 与えられた明細のカラム名がヘッダのプライマリキーに対応している場合、true
      */
     public boolean isHeadPkey(EntityMappingRow meisaiMapping) {
         if (meisaiMapping.getSqlColumnName().equalsIgnoreCase(this.configs.get("table_rdb") + "_ID")) {
             return true;
         }
         return false;
     }
     
     
     
     
     /**
      * 「i+1」という構文を明細のプライマリキーの型へ変換するための構文を取得します。
      * @return 「i+1」という構文を明細のプライマリキーの型へ変換するための構文
      */
     public String castFromMeisaiToHead() {
         // 明細テーブルのプライマリキーの数が１の場合、
         // 「ID」の列を明細テーブル上から探索します。
         if (countPkeyInMeisai() == 1) {
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.getSqlColumnName().equalsIgnoreCase("ID")) {
                     //return row.getJavaFieldName();
                     
 //                    String s = row.getJavaClassName();
 //                    if (s.startsWith("java.lang")) {
 //                        s = ClassUtil.getShortClassName(s);
 //                    }
                     
                     if (ClassUtil.getShortClassName(row.getJavaClassName()).equals("Long")) {
                         return "new Long(i+1)";
                     } else {
                         return "i+1";
                     }
                 }
             }
         } else {
             // １番目のプライマリキーを取得し、使用します。
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     if (ClassUtil.getShortClassName(row.getJavaClassName()).equals("Long")) {
                         return "new Long(i+1)";
                     } else {
                         return "i+1";
                     }
                 }
             }
         }
         
         return "i+1";
     }
     
 
     /**
      * ヘッダの１レコードに対応する明細テーブル上のレコードを取得するための
      * プライマリキー名を取得します。
      * @return ヘッダの１レコードに対応する明細テーブル上のレコードを
      *         取得するためのプライマリキー名
      */
     public String createHeadMeisaiPkeyByName() {
         // 明細テーブルのプライマリキーの数が１の場合、
         // 「ヘッダテーブル名_id」の列を明細テーブル上から探索します。
         if (countPkeyInMeisai() == 1) {
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.getSqlColumnName().equalsIgnoreCase(this.configs.get("table_rdb") + "_ID")) {
                     return row.getJavaFieldName();
                 }
             }
         } else {
             // １番目のプライマリキーを取得し、使用します。
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     return row.getJavaFieldName();
                 }
             }
         }
         
         return "by";
     }
 
     
     
     
     
     
     
     /**
      * ヘッダの１レコードに対応する明細テーブル上のレコードのプライマリキーのタイプ名を取得します。
      * 
      * @return ヘッダの１レコードに対応する明細テーブル上のレコードのプライマリキーのタイプ名
      */
     public String createHeadMeisaiPkeyBySqlTypeName() {
         // 明細テーブルのプライマリキーの数が１の場合、
         // 「ヘッダテーブル名_id」の列を明細テーブル上から探索します。
         if (countPkeyInMeisai() == 1) {
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.getSqlColumnName().equalsIgnoreCase(this.configs.get("table_rdb") + "_ID")) {
                     return row.getSqlTypeName();
                 }
             }
         } else {
             // １番目のプライマリキーを取得し、使用します。
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     return row.getSqlTypeName();
                 }
             }
         }
         
         return "by";
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     /**
      * ヘッダの１レコードに対応する明細テーブル上のレコードを取得するためのメソッドに
      * 与える引数を取得します。
      * @param includeVersion バージョン列を含む場合、true
      * @return ヘッダの１レコードに対応する明細テーブル上のレコードを取得する
      *         ためのメソッドに与える引数
      */
     public String createHeadMeisaiPkeyMethodArgs(boolean includeVersion) {
         StringBuffer stb = new StringBuffer();
         boolean is = false;
         // 明細テーブルのプライマリキーの数が１の場合、
         // 「ヘッダテーブル名_id」の列を明細テーブル上から探索します。
         if (countPkeyInMeisai() == 1) {
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.getSqlColumnName().
                     equalsIgnoreCase(this.configs.get("table_rdb") + "_ID")) {
                     String s = row.getJavaClassName();
                     if (s.startsWith("java.lang")) {
                         s = ClassUtil.getShortClassName(s);
                         // ヘッダテーブルのIDのクラス名を取得します。
                         for (EntityMappingRow headRow : mappings) {
                             if (headRow.getSqlColumnName().equalsIgnoreCase("ID")) {
                                 s = headRow.getJavaClassName();
                                 if (s.startsWith("java.lang")) {
                                     s = ClassUtil.getShortClassName(s);
                                     break;
                                 }
                             }
                         }
                     }
                     stb.append(s);
                     stb.append(' ');
                     stb.append(row.getJavaFieldName());
                     stb.append(',');
                     is = true;
                     break;
                 }
             }
         } else {
             // １番目のプライマリキーを取得し、使用します。
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()
                     || (includeVersion && NamingUtil.isVersionNo(row
                                           .getSqlColumnName()))) {
                     String s = row.getJavaClassName();
                     if (s.startsWith("java.lang")) {
                         s = ClassUtil.getShortClassName(s);
                     }
                     stb.append(s);
                     stb.append(' ');
                     stb.append(row.getJavaFieldName());
                     stb.append(',');
                     is = true;
                     break;
                 }
             }
         }
         if (is) {
             stb.setLength(stb.length() - 1);
         }
         return stb.toString();
     }
 
     /**
      * ヘッダの１レコードに対応する明細テーブル上のレコードを取得するための
      * メソッドに対して与えるアノテーションを取得します。
      * @return ヘッダの１レコードに対応する明細テーブル上のレコードを取得するための
      *         メソッドに対して与えるアノテーション
      */
     public String createHeadMeisaiPkeyMethodArgNames() {
         if (isTigerResource()) {
             return createHeadMeisaiAnnotationArgNames();
         }
         return createHeadMeisaiConstArgNames();
     }
     
     /**
      * ヘッダの１レコードに対応する明細テーブル上のレコードを取得するための
      * メソッドに対して与えるアノテーションを取得します。
      * @return ヘッダの１レコードに対応する明細テーブル上のレコードを取得するための
      *         メソッドに対して与えるアノテーション
      */
     private String createHeadMeisaiAnnotationArgNames() {
         List<EntityMappingRow> prows = new ArrayList<EntityMappingRow>();
         // 明細テーブルのプライマリキーの数が１の場合、
         // 「ヘッダテーブル名_id」の列を明細テーブル上から探索します。
         if (countPkeyInMeisai() == 1) {
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.getSqlColumnName().equalsIgnoreCase(this.configs.get("table_rdb") + "_ID")) {
                     prows.add(row);
                     break;
                 }
             }
         } else {
             // １番目のプライマリキーを取得し、使用します。
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     prows.add(row);
                     break;
                 }
             }
         }
 
         StringBuffer stb = new StringBuffer();
         if (0 < prows.size()) {
             boolean is = 1 < prows.size();
             if (is) {
                 stb.append('{');
             }
             for (EntityMappingRow row : prows) {
                 stb.append('"');
                 stb.append(row.getSqlColumnName());
                 stb.append('"');
                 stb.append(',');
             }
             stb.setLength(stb.length() - 1);
             if (is) {
                 stb.append('}');
             }
         }
         return stb.toString();
     }
 
     /**
      * ヘッダの１レコードに対応する明細テーブル上のレコードを取得するための
      * メソッドに対して与えるアノテーションを取得します。
      * @return ヘッダの１レコードに対応する明細テーブル上のレコードを取得するための
      *         メソッドに対して与えるアノテーション
      */
     private String createHeadMeisaiConstArgNames() {
         // 明細テーブルのプライマリキーの数が１の場合、
         // 「ヘッダテーブル名_id」の列を明細テーブル上から探索します。
         if (countPkeyInMeisai() == 1) {
             StringBuffer stb = new StringBuffer();
             boolean is = false;
             stb.append('"');
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.getSqlColumnName().equalsIgnoreCase(this.configs.get("table_rdb") + "_ID")) {
                     stb.append(row.getSqlColumnName());
                     stb.append(',');
                     is = true;
                     break;
                 }
             }
             if (is) {
                 stb.setLength(stb.length() - 1);
             }
             stb.append('"');
             return stb.toString();
         } else {
             // １番目のプライマリキーを取得し、使用します。
             StringBuffer stb = new StringBuffer();
             boolean is = false;
             stb.append('"');
             for (EntityMappingRow row : meisaiColumnsMappings) {
                 if (row.isPrimaryKey()) {
                     stb.append(row.getSqlColumnName());
                     stb.append(',');
                     is = true;
                     break;
                 }
             }
             if (is) {
                 stb.setLength(stb.length() - 1);
             }
             stb.append('"');
             return stb.toString();
         }
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
 
 
 
 
 
 }
