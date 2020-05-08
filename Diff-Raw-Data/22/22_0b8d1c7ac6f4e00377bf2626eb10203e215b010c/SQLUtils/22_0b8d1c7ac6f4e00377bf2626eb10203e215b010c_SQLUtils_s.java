 package org.cccs.easql.execution;
 
 import org.cccs.easql.domain.DBTable;
 import org.cccs.easql.domain.LinkTable;
 import org.cccs.easql.domain.Sequence;
 import org.cccs.easql.domain.TableColumn;
 
 import javax.persistence.JoinTable;
 import java.util.Map;
 
 import static java.lang.String.format;
 import static org.cccs.easql.cache.ClassCache.getTable;
 import static org.cccs.easql.util.ObjectUtils.arrayAsString;
 import static org.cccs.easql.util.ObjectUtils.value;
 
 /**
  * User: boycook
  * Date: 15/06/2011
  * Time: 15:39
  */
 //TODO: consider making this a BaseClass
 public final class SQLUtils {
 
     //Template SQL
    private final static String INSERT_TEMPLATE = "INSERT INTO %s (%s) VALUES (%s);";
    private final static String SELECT_TEMPLATE = "SELECT %s FROM %s";
    private final static String SELECT_TEMPLATE_WHERE = "SELECT %s FROM %s %s";
    private final static String UPDATE_TEMPLATE = "UPDATE %s set %s WHERE %s;";
    private final static String DELETE_TEMPLATE = "DELETE FROM %s;";
    private final static String DELETE_OBJECT_TEMPLATE = "DELETE FROM %s WHERE %s;";
    private final static String CREATE_TEMPLATE = "CREATE TABLE %s (%s);";
    private final static String SELECT_SEQUENCE_TEMPLATE = "SELECT NEXT VALUE FOR %s FROM %s";
    private final static String CREATE_SEQUENCE_TEMPLATE = "CREATE SEQUENCE %s AS BIGINT START WITH %d INCREMENT BY %d;";
    private final static String OUTER_JOIN = "LEFT OUTER JOIN %s %s ON %s.%s = %s.%s";
    private final static String INNER_JOIN = "INNER JOIN %s %s ON %s.%s = %s.%s AND %s.%s = %d";
 
     public static String generateInsertSQL(Object o) {
         StringBuilder insertColumns = new StringBuilder();
         StringBuilder values = new StringBuilder();
         final DBTable table = getTable(o.getClass());
 
         //Set the ID value
         if (table.id != null) {
             if (table.id.getGeneratedValue() != null) {
                 appendInsertValue(insertColumns, values, table.id.getName(), Schema.getSequence(table.id.getGeneratedValue().generator()).getValue());
             } else {
                 appendInsertValue(insertColumns, values, table.id.getName(), value(table.id.getValue(o)));
             }
         }
 
         //Set column values
         for (TableColumn column : table.columns) {
             Object columnValue = column.getValue(o);
             if (columnValue != null) {
                 appendInsertValue(insertColumns, values, column.getName(), value(columnValue));
             }
         }
 
         //Set foreign key values
         for (TableColumn relation : table.many2one) {
             appendInsertValue(insertColumns, values, relation.getColumn().name(), "%s");
         }
 
         return format(INSERT_TEMPLATE, table.getName(), insertColumns.toString(), values.toString());
     }
 
     public static String generateInsertSQL(TableColumn column, Object left, Object right) {
         final JoinTable joinTable = column.getJoinTable();
         final DBTable leftTable = getTable(left.getClass());
         final String columns = joinTable.joinColumns()[0].name() + ", " + joinTable.inverseJoinColumns()[0].name();
         final String values = leftTable.id.getValue(left) + "," + getUniqueSelectSQL(right);
         return format(INSERT_TEMPLATE, joinTable.name(), columns, values);
     }
 
     private static String getUniqueSelectSQL(Object o) {
         final DBTable table = getTable(o.getClass());
         final String where = format("WHERE upper(%s) = upper('%s')", table.key.getName(), table.key.getValue(o));
         return format(SELECT_TEMPLATE_WHERE, table.id.getName(), table.getName(), where);
     }
 
     public static String generateSelectSQL(LinkTable linkTable) {
         return format(SELECT_TEMPLATE, "*", linkTable.name);
     }
 
     public static String generateSelectSQL(final Class c) {
         final DBTable table = getTable(c);
         StringBuilder select = new StringBuilder();
         if (table.id != null) {
             appendColumn(select, table.id.getName());
         }
         for (String column : table.columnNames) {
             appendColumn(select, column);
         }
         for (TableColumn relation : table.many2one) {
             appendColumn(select, relation.getColumn().name());
         }
         return format(SELECT_TEMPLATE, select.toString(), table.getName());
     }
 
     public static String generateSelectSQLForOneToMany(final Class c) {
         final DBTable table = getTable(c);
         StringBuilder select = new StringBuilder();
         StringBuilder joins = new StringBuilder();
 
         if (table.id != null) {
             appendColumn(select, table.id.getName());
         }
 
         for (String column : table.columnNames) {
             appendColumn(select, column);
         }
 
         for (TableColumn relation : table.many2one) {
             final DBTable relatedTable = getTable(relation.getType());
             final String joinName = getJoinTableName(relatedTable, table);
             if (relatedTable.id != null) {
                 appendColumn(select, getJoinColumnName(joinName, relatedTable.id.getName()));
             }
             for (String joinColumn : relatedTable.columnNames) {
                 appendColumn(select, getJoinColumnName(joinName, joinColumn));
             }
             joins.append(format(OUTER_JOIN, relatedTable.getName(), joinName, table.getName(), relation.getColumn().name(), joinName, relatedTable.id.getName()));
         }
 
         return format(SELECT_TEMPLATE_WHERE, select.toString(), table.getName(), joins.toString());
     }
 
     public static String generateSelectSQLForManyToMany(Class c, TableColumn column, long id) {
         final DBTable table = getTable(c);
         JoinTable joinTable = column.getJoinTable();
         final StringBuilder sql = new StringBuilder();
         final String columns = table.id.getName() + ", " + arrayAsString(table.columnNames);
         sql.append(format(SELECT_TEMPLATE, columns, table.getName() + " a "));
         sql.append(format(INNER_JOIN, joinTable.name(), "b", "a", table.id.getName(), "b", joinTable.inverseJoinColumns()[0].name(), "b", joinTable.joinColumns()[0].name(), id));
         sql.append(";");
         return sql.toString();
     }
 
     //TODO: deal with different data types specifically if column is foreign key
     public static String generateClearForeignKeySQL(Object o, TableColumn column) {
         final DBTable table = getTable(o.getClass());
         String values = column.getName() + " = 0";
         String where = table.id.getName() + " = " + table.id.getValue(o);
         return format(UPDATE_TEMPLATE, table.getName(), values, where);
     }
 
     public static String generateUpdateSQL(Object o) {
         final DBTable table = getTable(o.getClass());
         StringBuilder values = new StringBuilder();
 
         for (TableColumn column : table.columns) {
             Object columnValue = column.getValue(o);
             if (columnValue != null) {
                 appendUpdateValue(values, column.getName(), value(columnValue));
             }
         }
 
         String where = table.id.getName() + " = " + table.id.getValue(o);
         return format(UPDATE_TEMPLATE, table.getName(), values.toString(), where);
     }
 
     public static String generateUpdateSQLForRelation(Object o, TableColumn column) {
         final DBTable table = getTable(o.getClass());
         StringBuilder values = new StringBuilder();
         values.append(column.getName());
         values.append(" = ");
         values.append("%s");
         String where = table.id.getName() + " = " + table.id.getValue(o);
         return format(UPDATE_TEMPLATE, table.getName(), values.toString(), where);
     }
 
     public static String generateDeleteSQL(Object o) {
         final DBTable table = getTable(o.getClass());
         String where = table.id.getName() + " = " + table.id.getValue(o);
         return format(DELETE_OBJECT_TEMPLATE, table.getName(), where);
     }
 
     public static String generateDeleteSQL(Class c) {
         final DBTable table = getTable(c);
         return format(DELETE_TEMPLATE, table.getName());
     }
 
     public static String generateDeleteSQL(TableColumn column, Object left, Object right) {
         JoinTable joinTable = column.getJoinTable();
         final DBTable leftTable = getTable(left.getClass());
         final DBTable rightTable = getTable(right.getClass());
 
         final String where = joinTable.joinColumns()[0].name() + " = " + leftTable.id.getValue(left) + " and " +
                 joinTable.inverseJoinColumns()[0].name() + " = " + rightTable.id.getValue(right);
 
         return format(DELETE_OBJECT_TEMPLATE, joinTable.name(), where);
     }
 
     public static String generateSequenceSQL(Sequence sequence) {
         return format(CREATE_SEQUENCE_TEMPLATE, sequence.getName(), sequence.getStartsWith(), sequence.getIncrementBy());
     }
 
     public static String generateCreateSQL(LinkTable table) {
         StringBuilder columns = new StringBuilder();
         columns.append(table.leftKey);
         columns.append(" INTEGER, ");
         columns.append(table.rightKey);
         columns.append(" INTEGER");
         return format(CREATE_TEMPLATE, table.name, columns.toString());
     }
 
     public static String generateCreateSQL(Class c) {
         final DBTable table = getTable(c);
         StringBuilder columns = new StringBuilder();
 
         if (table.id != null) {
             appendColumn(columns, format("%s identity NOT NULL PRIMARY KEY", table.id.getName()));
         }
 
         for (TableColumn column : table.columns) {
             appendColumn(columns, format("%s %s", column.getName(), column.getDBType()));
         }
 
         for (TableColumn relation : table.many2one) {
             appendColumn(columns, format("%s %s", relation.getColumn().name(), "INTEGER"));
         }
 
         return format(CREATE_TEMPLATE, table.getName(), columns.toString());
     }
 
     public static String generateDropSQL(Object o) {
         throw new UnsupportedOperationException("Drop is not yet supported");
     }
 
     public static String generateWhere(Map<String, String> whereClauses) {
         final StringBuilder where = new StringBuilder();
 
         for (String key : whereClauses.keySet()) {
             String value = whereClauses.get(key);
 
             if (where.length() == 0) {
                 where.append(" WHERE ");
             } else {
                 where.append(" AND ");
             }
 
             where.append(key);
             where.append(" = '");
             where.append(value);
             where.append("' ");
         }
 
         return where.toString();
     }
 
     public static String getJoinTableName(DBTable left, DBTable right) {
         return left.getName().toLowerCase() + "2" + right.getName().toLowerCase();
     }
 
     public static String getJoinColumnName(String table, String column) {
         return table + "." + column + " as " + table + "_" + column;
     }
 
     private static void appendColumn(StringBuilder select, String column) {
         if (select.length() > 0) {
             select.append(", ");
         }
         select.append(column);
     }
 
     private static void appendInsertValue(StringBuilder columns, StringBuilder values, String column, String value) {
         if (columns.length() > 0) {
             columns.append(", ");
             values.append(", ");
         }
         columns.append(column);
         values.append(value);
     }
 
     private static void appendUpdateValue(StringBuilder values, String column, String value) {
         if (values.length() > 0) {
             values.append(", ");
         }
         values.append(column);
         values.append(" = ");
         values.append(value);
     }
 }
