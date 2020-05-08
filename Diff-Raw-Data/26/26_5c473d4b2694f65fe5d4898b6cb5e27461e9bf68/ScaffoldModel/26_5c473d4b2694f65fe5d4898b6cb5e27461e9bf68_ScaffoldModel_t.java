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
 import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
 import org.seasar.dolteng.core.types.TypeMapping;
 import org.seasar.dolteng.core.types.TypeMappingRegistry;
 import org.seasar.dolteng.eclipse.DoltengCore;
 import org.seasar.dolteng.eclipse.convention.NamingUtil;
 import org.seasar.dolteng.eclipse.model.EntityMappingRow;
 import org.seasar.dolteng.eclipse.model.RootModel;
 import org.seasar.dolteng.eclipse.model.TreeContent;
 import org.seasar.dolteng.eclipse.scaffold.ScaffoldImportsSet;
 import org.seasar.dolteng.eclipse.util.NameConverter;
 import org.seasar.dolteng.eclipse.util.ProjectUtil;
 import org.seasar.framework.convention.NamingConvention;
 import org.seasar.framework.util.ClassUtil;
 import org.seasar.framework.util.StringUtil;
 
 /**
  * @author taichi
  * 
  */
 public class ScaffoldModel implements RootModel {
 
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
         
     // 検索条件に付与するためのORDER BY句
     private String orderbyString;
     
     // 検索条件に付与するためのORDER BY句における列名
     private String orderbyStringColumn;
     
    // 検索条件に付与するためのORDER BY句におけるプロパティ名
    private String orderbyStringProperty;
    
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
 
     public ScaffoldModel(Map<String, String> configs, TableNode node, Map<Integer, String[]> selectedColumns) {
         super();
         this.configs = configs;
         this.configs.put("pagingpackagename", "paging");
         this.configs.put("dtopackagename", "dto");
         initialize(node, selectedColumns);
     }
 
     @SuppressWarnings("unchecked")
     protected void initialize(TableNode node, Map<Integer, String[]> selectedColumns) {
         ProjectNode n = (ProjectNode) node.getRoot();
         this.project = n.getJavaProject();
 
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
         
         // 検索条件に付与するためのORDER BY句を作成します。
         orderbyString = "";
         orderbyStringColumn = "";
        orderbyStringProperty = "";
         conditionArguments = "";
         conditionArgumentsTeisuAnnotation = "\"";
         for (int i = 0; i < selectedColumnsMappings.length; i++) {
             if (i > 0) {
                 orderbyString += "And";
                 orderbyStringColumn += ",";
                orderbyStringProperty += ",";
                 conditionArguments += ",";
                 conditionArgumentsTeisuAnnotation += ",";
             }
             orderbyString += pascalize(selectedColumnsMappings[i].getSqlColumnName());
             orderbyStringColumn += selectedColumnsMappings[i].getSqlColumnName();
            orderbyStringProperty += selectedColumnsMappings[i].getJavaFieldName();
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
     
    /**
     * 検索条件に付与するためのORDER BY句におけるプロパティ名を取得します。
     * @return
     */
    public String getOrderbyStringProperty() {
        return orderbyStringProperty;
    }
    
    /**
     * 検索条件に付与するためのORDER BY句におけるプロパティ名を設定します。
     * @param orderbyString
     */
    public void setOrderbyStringProperty(String orderbyStringProperty) {
        this.orderbyStringProperty = orderbyStringProperty;
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
     
     public void setConditionArgumentsTeisuAnnotation(String conditionArgumentsTeisuAnnotation) {
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
     
     /**
      * The suffix in the dto property is returned.
      * @param typeName
      * @return
      */
     public String getDtoSuffix(String typeName) {
         if (typeName.equals("Integer") ||
             typeName.equals("BigDecimal") ||
             typeName.equals("Date") ||
             typeName.equals("Timestamp")) {
             return "GE";
         } else if (typeName.equals("String")) {
             return "LIKE";
         }
         return "";
     }
 
     /**
      * S2Dao の SQL 文で使用する条件を取得します。
      * @param fieldName 列名
      * @param typeName タイプ名
      * @return S2Dao の SQL 文で使用する条件
      */
     public String getS2DaoCondition(String fieldName, String typeName) {
         
         //System.out.println("タイプ名：" + typeName + ", " + fieldName);
         
         if (typeName.equals("String")) {
             return "LIKE concat(/*" + fieldName + "*/' ','%')";
         } else if (typeName.equals("Integer") ||
                    typeName.equals("BigDecimal") ||
                    typeName.equals("Long")) {
             return ">= /*" + fieldName + "*/'0'";
         } else if (typeName.equals("Date") || typeName.equals("Timestamp")) {
             return ">= /*" + fieldName + "*/'1900/1/1'";
         }
         //return "= fieldName";
         return "= /*" + fieldName + "*/";
     }
     
     
     
     /**
      * If the type of dto property is "String", true is returned.
      * @param typeName
      * @return
      */
     public boolean isDtoParameterLike(String typeName) {
         if (typeName.equals("String")) {
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
 
     public String getJpaEntityImports() {
         ScaffoldImportsSet imports = new ScaffoldImportsSet();
         imports.addJpaAnnotaion(mappings);
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
 
     public int countPkeys() {
         int result = 0;
         for (EntityMappingRow row : mappings) {
             if (row.isPrimaryKey()) {
                 result++;
             }
         }
         return result;
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
 }
