 package com.github.ryhmrt.mssqldiff.util;
 
 import com.github.ryhmrt.mssqldiff.data.Column;
 import com.github.ryhmrt.mssqldiff.data.Table;
 
 
 public class SqlUtil {
 
     public static String revoke(String table, String user, String option) {
         return "REVOKE " + option + " ON [" + table + "] FROM " + user + ";\nGO\n";
     }
 
     public static String grant(String table, String user, String option) {
         return "GRANT " + option + " ON [" + table + "] TO " + user + ";\nGO\n";
     }
 
     public static String addColumn(Column column) {
        return addColumn(column.getTableName(), column.getName(), column.getTableName(), column.getLength(), column.isPk(), column.isIdentity(), column.isNullable(), column.getDefaultValue());
     }
 
     public static String addColumn(String table, String column, String type, int length, boolean pk, boolean identity, boolean nullable, String defaultValue) {
         return "ALTER TABLE [" + table + "] ADD [" + column + "] " +
             (columnType(type, length) +
             (pk ? " PRIMARY KEY" : "") +
             (identity ? " IDENTITY" : "") +
             (nullable ? "" : " NOT NULL")) +
             (defaultValue != null && !defaultValue.isEmpty() ? " DEFAULT " + defaultValue : "") +
             ";\nGO\n";
     }
 
     private static String columnType(String type, int length) {
         boolean sizable = false;
         if (type.equalsIgnoreCase("char")) sizable = true;
         if (type.equalsIgnoreCase("nchar")) sizable = true;
         if (type.equalsIgnoreCase("varchar")) sizable = true;
         if (type.equalsIgnoreCase("nvarchar")) sizable = true;
         if (type.equalsIgnoreCase("binary")) sizable = true;
         if (type.equalsIgnoreCase("varbinary")) sizable = true;
         return type + (sizable ? "("+length+") " : "");
     }
 
     public static String dropColumn(String table, String column) {
         return "ALTER TABLE [" + table + "] DROP COLUMN [" + column + "]" +
             ";\nGO\n";
     }
 
     public static String createTable(Table table) {
         StringBuilder sb = new StringBuilder();
         sb.append("CREATE TABLE [");
         sb.append(table.getName());
         sb.append("] (");
         sb.append("\n");
         for (Column column : table.getColumns()) {
             sb.append("  [");
             sb.append(column.getName());
             sb.append("] ");
             sb.append(columnType(column.getType(), column.getLength()));
             if (column.isPk()) sb.append(" PRIMARY KEY");
             if (column.isIdentity()) sb.append(" IDENTITY");
             if (!column.isNullable()) sb.append(" NOT NULL");
             if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) sb.append(" DEFAULT " + column.getDefaultValue());
             sb.append(",\n");
         }
         sb.append(");\nGO\n");
         return sb.toString();
     }
 }
