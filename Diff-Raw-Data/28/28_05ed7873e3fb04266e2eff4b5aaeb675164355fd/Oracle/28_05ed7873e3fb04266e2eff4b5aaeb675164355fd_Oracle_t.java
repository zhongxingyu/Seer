 package edu.northwestern.bioinformatics.bering.dialect;
 
 import static edu.northwestern.bioinformatics.bering.SqlUtils.sqlLiteral;
 import org.apache.ddlutils.model.Table;
 import org.apache.ddlutils.model.Column;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  * @author Rhett Sutphin
  */
 public class Oracle extends Generic {
     public List<String> createTable(Table table) {
         List<String> statments = new ArrayList<String>(2);
         if (hasAutomaticPrimaryKey(table)) statments.add("CREATE SEQUENCE " + createIdSequenceName(table));
         statments.addAll(super.createTable(massageTableForOracle(table)));
         return statments;
     }
 
     public List<String> dropTable(Table table) {
         List<String> statments = new ArrayList<String>(2);
         statments.addAll(super.dropTable(massageTableForOracle(table)));
         if (hasAutomaticPrimaryKey(table)) statments.add("DROP SEQUENCE " + createIdSequenceName(table));
         return statments;
     }
 
     public List<String> setDefaultValue(String table, String column, String newDefault) {
         return Arrays.asList(String.format(
             "ALTER TABLE %s MODIFY (%s DEFAULT %s)", table, column, sqlLiteral(newDefault)
         ));
     }
 
     public List<String> setNullable(String table, String column, boolean nullable) {
         return Arrays.asList(String.format(
             "ALTER TABLE %s MODIFY (%s %sNULL)", table, column, nullable ? "" : "NOT "
         ));
     }
 
     private String createIdSequenceName(Table table) {
         return createIdSequenceName(table.getName());
     }
 
     private String createIdSequenceName(String tableName) {
         int maxlen = getPlatform().getPlatformInfo().getMaxIdentifierLength();
         return "seq_" + truncate(tableName, maxlen - 7) + "_id";
     }
 
     private String truncate(String str, int maxlen) {
         if (str.length() <= maxlen) return str;
         return str.substring(0, maxlen);
     }
 
    private static Table massageTableForOracle(Table table) {
        Table massaged = cloneTable(table);

        if (hasAutomaticPrimaryKey(massaged)) {
            massaged.getPrimaryKeyColumns()[0].setAutoIncrement(false);
         }
         return table;
     }
 
    // Table#clone doesn't clone the individual columns, so:
    private static Table cloneTable(Table table) {
        Table massaged;
        try {
            massaged = (Table) table.clone();
            while (massaged.getColumnCount() > 0) {
                massaged.removeColumn(0);
            }
            for (Column column : table.getColumns()) {
                massaged.addColumn((Column) column.clone());
            }
        } catch (CloneNotSupportedException e) {
            throw new Error("This shouldn't be possible", e);
        }
        return massaged;
    }

    private static boolean hasAutomaticPrimaryKey(Table table) {
         if (table.getPrimaryKeyColumns().length == 1) {
             Column pk = table.getPrimaryKeyColumns()[0];
             return pk.isAutoIncrement();
         } else {
             return false;
         }
     }
 
     // Attempts to be SQLPLUS-compatible for scripts that mix PL/SQL and plain SQL
     public List<String> separateStatements(String script) {
         List<String> statments = new LinkedList<String>();
 
         // First, split by '/' on a line by itself
         String[] blocks = script.split("[\\r\\n]+/[\\r\\n]+");
 
         // For each resulting block (except the last),
         for (int i = 0 ; i < blocks.length - 1 ; i++) {
             String block = blocks[i];
             // Walk back to the previous CREATE -- it is the beginning of the last statement in the block.
             int lastCreate = block.toUpperCase().lastIndexOf("CREATE");
             if (lastCreate < 0) {
                 throw new IllegalArgumentException("Block ends with '/' but does not include a CREATE");
             }
             String last = block.substring(lastCreate);
             String balance = block.substring(0, lastCreate);
 
             // Split the remainder on semi-colons
             splitBySemiColons(balance, statments);
 
             statments.add(last.trim());
         }
 
         // The last block doesn't end with a '/', so include all its statements
         splitBySemiColons(blocks[blocks.length - 1], statments);
 
         return statments;
     }
 
     private void splitBySemiColons(String block, List<String> addTo) {
         String[] bStatements = block.split("\\s*;\\s*");
         for (String bStatement : bStatements) {
             if (bStatement.length() > 0) addTo.add(bStatement);
         }
     }
 
     public List<String> insert(String table, List<String> columns, List<Object> values, boolean automaticPrimaryKey) {
         if (columns.size() == 0) {
             return Arrays.asList(String.format(
                 "INSERT INTO %s (id) VALUES (%s.nextval)", table, createIdSequenceName(table)
             ));
         } else if (automaticPrimaryKey) {
             return Arrays.asList(String.format(
                 "INSERT INTO %s (id, %s) VALUES (%s.nextval, %s)",
                     table,
                     StringUtils.join(columns.iterator(), INSERT_DELIMITER),
                     createIdSequenceName(table),
                     createInsertValueString(values)
             ));
         } else {
             return super.insert(table, columns, values, automaticPrimaryKey);
         }
     }
 }
