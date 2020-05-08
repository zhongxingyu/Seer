 package com.smikhalev.sqlserverutils.schema;
 
 import com.smikhalev.sqlserverutils.core.executor.DataRow;
 import com.smikhalev.sqlserverutils.core.executor.DataTable;
 import com.smikhalev.sqlserverutils.core.executor.StatementExecutor;
 import com.smikhalev.sqlserverutils.schema.dbobjects.*;
 
 import java.util.List;
 
 /*
  * This class is load database schema using system sql server tables.
  * It works mostly in row-by-row mode more like a search in depth.
  * It is not effective and we real used it should be most probably refactored into
  * a bulk mode.
  */
 public class DatabaseLoader {
 
     private final StatementExecutor executor;
 
     public DatabaseLoader(StatementExecutor executor) {
         this.executor = executor;
     }
 
     public Database load() {
         Database database = new Database();
 
         loadTables(database.getTables());
         loadForeignKeys(database);
         loadTrigger(database);
 
         return database;
     }
 
     private void loadTables(List<Table> tables) {
         String query =
             "select" +
             "    object_id," +
             "    t.name as table_name," +
             "    s.name as schema_name " +
             "from sys.tables t " +
             "inner join sys.schemas s" +
             "    on t.schema_id = s.schema_id";
 
         DataTable dataTable = executor.executeAsDataTable(query);
 
         for (DataRow row : dataTable.getRows()) {
             int objectId = (int) row.get("object_id");
             String tableName = (String) row.get("table_name");
             String schemaName = (String) row.get("schema_name");
 
             Table table = new Table(tableName, schemaName);
 
             loadColumns(table);
             loadIndexes(objectId, table);
 
             tables.add(table);
         }
     }
 
     private void loadColumns(Table table) {
         String query =
             "select " +
             "    column_name, " +
             "    data_type, " +
             "    is_nullable, " +
             "    coalesce(character_maximum_length, 0) as char_max_length\n" +
            "from information_schema.columns \n" +
             "where table_name=? and table_schema=?";
 
         DataTable dataTable = executor.executeAsDataTable(query, table.getName(), table.getSchema());
 
         for (DataRow row : dataTable.getRows()) {
             String columnName = (String) row.get("column_name");
             String dataType = (String) row.get("data_type");
             String isNullable = (String) row.get("is_nullable");
             int maxCharLength = (int) row.get("char_max_length");
 
             DbType dbType = DbType.valueOf(dataType.toUpperCase());
             boolean isNull = isNullable.equals("YES");
 
             Column column = maxCharLength == 0
                     ? new Column(columnName, dbType, isNull)
                     : new CharColumn(columnName, dbType, isNull, maxCharLength);
 
             table.getColumns().add(column);
         }
     }
 
     private void loadIndexes(int objectId, Table table) {
         String query =
             "select name, index_id, type, is_unique\n" +
             "from sys.indexes\n" +
             "where object_id = ?" +
             " and type <> 0";
 
         DataTable dataTable = executor.executeAsDataTable(query, objectId);
 
         for (DataRow row : dataTable.getRows()) {
             String indexName = (String) row.get("name");
             int indexId = (int) row.get("index_id");
             int indexTypeId = (int) row.get("type");
             boolean isUnique = (boolean) row.get("is_unique");
 
             IndexType indexType = IndexType.values()[indexTypeId - 1];
 
             Index index = indexType == IndexType.CLUSTERED
                     ? new ClusteredIndex(indexName, table, isUnique)
                     : new NonClusteredIndex(indexName, table, isUnique);
 
             loadIndex(objectId, indexId, index);
 
             if (indexType == IndexType.CLUSTERED)
                 table.setClusteredIndex((ClusteredIndex) index);
             else
                 table.getNonClusteredIndexes().add((NonClusteredIndex) index);
         }
     }
 
     private void loadIndex(int objectId, int indexId, Index index) {
         String query =
             "select " +
             "    c.name as column_name,\n" +
             "    ic.is_descending_key,\n" +
             "    ic.is_included_column\n" +
             "from sys.index_columns ic\n" +
             "inner join sys.columns c\n" +
             "    on c.object_id = ic.object_id\n" +
             "and c.column_id = ic.column_id\n" +
             "where ic.object_id = ?\n" +
             "  and index_id = ?\n" +
             "order by key_ordinal";
 
         DataTable dataTable = executor.executeAsDataTable(query, objectId, indexId);
 
         for (DataRow row : dataTable.getRows()) {
             String columnName = (String) row.get("column_name");
             boolean isDescending = (boolean) row.get("is_descending_key");
             boolean isIncludedColumn = (boolean) row.get("is_included_column");
 
             if (!isIncludedColumn)
             {
                 SortType sort = isDescending ? SortType.DESC : SortType.ASC;
                 IndexColumn indexColumn = new IndexColumn(columnName, sort);
                 index.getKeyColumns().add(indexColumn);
             }
             else
             {
                 ((NonClusteredIndex)index).getIncludedColumns().add(columnName);
             }
         }
     }
 
     private void loadForeignKeys(Database database) {
         String query = "select " +
                 "    fk.name as fk_name, " +
                 "    st.name as source_table_name, " +
                 "    sts.name as source_table_schema, " +
                 "    stc.name as source_column_name, " +
                 "    tt.name as target_table_name, " +
                 "    tts.name as target_table_schema, " +
                 "    ttc.name as target_column_name " +
                 "from sys.foreign_keys fk " +
                 "inner join sys.foreign_key_columns fkc " +
                 "    on fkc.constraint_object_id = fk.object_id " +
                 "inner join sys.tables st " +
                 "    on st.object_id = fkc.parent_object_id " +
                 "inner join sys.schemas sts " +
                 "     on sts.schema_id = st.schema_id " +
                 "inner join sys.columns stc " +
                 "    on stc.object_id = st.object_id " +
                 "    and stc.column_id = fkc.parent_column_id " +
                 "inner join sys.tables tt " +
                 "    on tt.object_id = fkc.referenced_object_id " +
                 "inner join sys.schemas tts " +
                 "     on tts.schema_id = tt.schema_id " +
                 "inner join sys.columns ttc " +
                 "    on ttc.object_id = tt.object_id " +
                 "    and ttc.column_id = fkc.referenced_column_id ";
 
         DataTable dataTable = executor.executeAsDataTable(query);
 
         for (DataRow row : dataTable.getRows()) {
             String foreignKeyName = (String) row.get("fk_name");
             String sourceTableName = (String) row.get("source_table_name");
             String sourceSchemaName = (String) row.get("source_table_schema");
             String sourceColumnName = (String) row.get("source_column_name");
             String targetTableName = (String) row.get("target_table_name");
             String targetSchemaName = (String) row.get("target_table_schema");
             String targetColumnName = (String) row.get("target_column_name");
 
             String sourceTableFullName = DbObject.buildFullName(sourceSchemaName, sourceTableName);
             String targetTableFullName = DbObject.buildFullName(targetSchemaName, targetTableName);
 
             Table sourceTable = database.getTableByFullName(sourceTableFullName);
             Table targetTable = database.getTableByFullName(targetTableFullName);
 
             Column sourceColumn = sourceTable.getColumns().getByName(sourceColumnName);
             Column targetColumn = targetTable.getColumns().getByName(targetColumnName);
 
             ForeignKey fk = new ForeignKey(foreignKeyName, sourceTable, sourceColumn, targetTable, targetColumn);
             sourceTable.getForeignKeys().add(fk);
         }
     }
 
     private void loadTrigger(Database database) {
         String query =
             "select " +
             "    tb.name as table_name, " +
             "    s.name as schema_name, " +
             "    tr.name as trigger_name " +
             "from sys.triggers tr " +
             "inner join sys.tables tb " +
             "    on tr.parent_id = tb.object_id " +
             "inner join sys.schemas s\n" +
             "    on s.schema_id = tb.schema_id";
 
         DataTable dataTable = executor.executeAsDataTable(query);
 
         for (DataRow row : dataTable.getRows()) {
             String tableName = (String) row.get("table_name");
             String schemaName = (String) row.get("schema_name");
             String triggerName = (String) row.get("trigger_name");
 
             String tableFullName = DbObject.buildFullName(schemaName, tableName);
             Table table = database.getTableByFullName(tableFullName);
             Trigger trigger = new Trigger(triggerName, table.getSchema(), table, "");
             table.getTriggers().add(trigger);
         }
     }
 }
