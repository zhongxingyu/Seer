 /* 
  * Copyright 2011 - Inform8
  * http://www.inform8.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License")
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package com.eighty8.inform8.db.mysql;
 
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.eighty8.inform8.GenerationContext;
 import com.eighty8.inform8.db.DbConnection;
 import com.eighty8.inform8.db.model.DatabaseModel;
 import com.eighty8.inform8.db.model.ModelBuilder;
 import com.eighty8.inform8.db.table.BaseColumn;
 import com.eighty8.inform8.db.table.BaseTable;
 import com.eighty8.inform8.db.table.Inform8Column;
 import com.eighty8.inform8.db.table.Inform8Table;
 import com.eighty8.inform8.db.table.MasterChildForeignKey;
 import com.eighty8.inform8.db.types.BitType;
 import com.eighty8.inform8.db.types.BlobType;
 import com.eighty8.inform8.db.types.BooleanType;
 import com.eighty8.inform8.db.types.DateTimeType;
 import com.eighty8.inform8.db.types.DateType;
 import com.eighty8.inform8.db.types.DecimalType;
 import com.eighty8.inform8.db.types.EnumType;
 import com.eighty8.inform8.db.types.FieldType;
 import com.eighty8.inform8.db.types.FloatType;
 import com.eighty8.inform8.db.types.IntType;
 import com.eighty8.inform8.db.types.TextType;
 import com.eighty8.inform8.db.types.TimeStampType;
 import com.eighty8.inform8.db.types.TimeType;
 import com.eighty8.inform8.db.types.UnknownType;
 import com.eighty8.inform8.db.types.VarcharType;
 import com.eighty8.inform8.db.types.YearType;
 import com.eighty8.inform8.displaysettings.Label;
 
 /**
  * Builds the basics of an Inform8 model representing the supplied database connection.
  * 
  * Creates the tables. Creates the members Sets the PK (Only supports a single pk, composite pks are not supported) Sets Member Attributes: Unique, Not Null,
  * Default, Auto Increment
  * 
  * 
  * 
  * @author ryanhenderson
  * 
  */
 public class MysqlModelBuilder implements ModelBuilder {
 
   static Logger LOG = Logger.getLogger(MysqlModelBuilder.class);
 
   private final DbConnection connection;
 
 
   public MysqlModelBuilder(DbConnection connection) {
     this.connection = connection;
   }
 
 
   @Override
   public DatabaseModel extractModel() throws SQLException {
     DatabaseModel model = new DatabaseModel();
 
     List<Inform8Table> tables = extractTables();
 
     model.setTables(tables);
 
     extractColumns(connection, model);
 
     applyIndexes(connection, model);
 
     applyPrimaryKeys(connection, model);
 
     applyForeignKeys(connection, model);
 
     return model;
   }
 
 
   /**
    * 
    * @param connection
    * @return
    * @throws SQLException
    */
   protected List<Inform8Table> extractTables() throws SQLException {
     LOG.trace("Extracting tables");
     List<Inform8Table> tables = new ArrayList<Inform8Table>();
 
     DatabaseMetaData metaData = connection.getConnection().getMetaData();
 
     ResultSet tableRs = metaData.getTables(GenerationContext.getInstance().getDatabaseName(), null, null, null);
     if (tableRs.first()) {
       do {
         tables.add(new BaseTable(tableRs.getString("TABLE_NAME")));
       } while (tableRs.next());
     }
 
     LOG.trace("Tables: " + tables);
     return tables;
   }
 
 
   /**
    * 
    * @param connection
    * @param model
    * @throws SQLException
    */
   protected void extractColumns(DbConnection connection, DatabaseModel model) throws SQLException {
     LOG.info("Extracting Column Information");
 
     List<Inform8Table> tables = model.getTables();
     DatabaseMetaData metaData = connection.getConnection().getMetaData();
 
     for (Inform8Table table : tables) {
       ResultSet cols = metaData.getColumns(GenerationContext.getInstance().getDatabaseName(), null, table.getName(), null);
       if (cols.first()) {
         int index = 0;
         do {
 
           BaseColumn column = new BaseColumn(cols.getString("COLUMN_NAME"), index++);
           table.addColumn(column);
           
           column.setType(getMemberType(cols, table));
 
           if (cols.getString("IS_NULLABLE").equalsIgnoreCase("no")) {
             column.getSettings().addLabel(Label.NOT_NULL.toString());
           }
 
           if (cols.getString("IS_AUTOINCREMENT").equalsIgnoreCase("yes")) {
             column.getSettings().addLabel(Label.AUTO_INCREMENT.toString());
           }
 
           if (cols.getString("COLUMN_DEF") != null && !cols.getString("COLUMN_DEF").equalsIgnoreCase("null")) {
             column.getSettings().addProperty(Label.DEFAULT_VALUE, cols.getString("COLUMN_DEF"));
           }
 
           LOG.debug("New table[" + table.getName() + "] column: " + column);
 
         } while (cols.next());
       }
     }
 
     LOG.trace("Extracting Columns Complete");
   }
 
 
   /**
    * Currently only supports one Primary Key.
    * 
    * Composite keys might be supported in the future...
    * 
    * @param connection
    * @param model
    * @throws SQLException
    */
   protected void applyPrimaryKeys(DbConnection connection, DatabaseModel model) throws SQLException {
     LOG.trace("Extracting Primary Key Information");
     List<Inform8Table> tables = model.getTables();
     DatabaseMetaData metaData = connection.getConnection().getMetaData();
 
     for (Inform8Table table : tables) {
       ResultSet pks = metaData.getPrimaryKeys(GenerationContext.getInstance().getDatabaseName(), null, table.getName());
       if (pks.first()) {
         int count = 0;
         do {
           if (count > 0) {
             throw new UnsupportedOperationException("Inform8 - Multiple/Composite primary keys are not supported.");
           }
 
           Inform8Column pk = table.getColumn(pks.getString("COLUMN_NAME"));
           pk.getSettings().addLabel(Label.PRIMARY_KEY.toString());
           table.setPrimaryKey(pk);
 
           count++;
         } while (pks.next());
       }
     }
     LOG.info("Extracting Primary Keys Complete");
   }
 
 
   protected void applyForeignKeys(DbConnection connection, DatabaseModel model) throws SQLException {
 
     List<Inform8Table> tables = model.getTables();
     DatabaseMetaData metaData = connection.getConnection().getMetaData();
 
     for (Inform8Table table : tables) {
       ResultSet exportedFks = metaData.getExportedKeys(GenerationContext.getInstance().getDatabaseName(), null, table.getName());
       LOG.debug("Extracting Foreign Keys for table " + table.getName());
       if (exportedFks.first()) {
         do {
           MasterChildForeignKey fk = new MasterChildForeignKey(model.getTable(exportedFks.getString("PKTABLE_NAME")), exportedFks.getString("PKCOLUMN_NAME"),
               model.getTable(exportedFks.getString("FKTABLE_NAME")), exportedFks.getString("FKCOLUMN_NAME"));
           
           LOG.debug("FK " +
         		  " PKTABLE_NAME:" + exportedFks.getString("PKTABLE_NAME") +
         		  " PKCOLUMN_NAME:" + exportedFks.getString("PKCOLUMN_NAME") +
         		  " FKTABLE_NAME:" + exportedFks.getString("FKTABLE_NAME") +
         		  " FKCOLUMN_NAME:" + exportedFks.getString("FKCOLUMN_NAME"));
           
           model.getTable(exportedFks.getString("PKTABLE_NAME")).getPrimaryKey().addMasterForeignKeys(fk);
           model.getTable(exportedFks.getString("FKTABLE_NAME")).getColumn(exportedFks.getString("FKCOLUMN_NAME")).setChildForeignKey(fk);
           
          
           
         } while (exportedFks.next());
       }
     }
     LOG.info("Extracting Foreign Keys Complete");
   }
 
 
   /**
    * 
    * @param connection
    * @param model
    * @throws SQLException
    */
   protected void applyIndexes(DbConnection connection, DatabaseModel model) throws SQLException {
     List<Inform8Table> tables = model.getTables();
     DatabaseMetaData metaData = connection.getConnection().getMetaData();
 
     for (Inform8Table table : tables) {
       ResultSet indexes = metaData.getIndexInfo(GenerationContext.getInstance().getDatabaseName(), null, table.getName(), true, false);
       if (indexes.first()) {
         do {
           if (indexes.getBoolean("NON_UNIQUE") == false) {
             table.getColumn(indexes.getString("COLUMN_NAME")).getSettings().addLabel(Label.NOT_NULL.toString());
           }
         } while (indexes.next());
       }
     }
     LOG.info("Extracting Indexes Complete");
   }
 
 
   /**
    * 
    * @param cols
    * @param table 
    * @return
    * @throws SQLException
    */
   protected FieldType getMemberType(ResultSet cols, Inform8Table table) throws SQLException {
 
     String mysqlDataTypeName = cols.getString("TYPE_NAME");
 
     // CHAR, VARCHAR
     if (mysqlDataTypeName.toLowerCase().indexOf("varchar") >= 0) {
       return new VarcharType(cols.getInt("CHAR_OCTET_LENGTH"));
     } else if (mysqlDataTypeName.toLowerCase().indexOf("char") >= 0) {
       return new VarcharType(cols.getInt("CHAR_OCTET_LENGTH"));
 
       // TINYTEXT, TEXT, MEDIUMTEXT, and LONGTEXT
     } else if (mysqlDataTypeName.toLowerCase().indexOf("tinytext") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("mediumtext") >= 0
         || mysqlDataTypeName.toLowerCase().indexOf("longtext") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("text") >= 0) {
       return new TextType(cols.getInt("CHAR_OCTET_LENGTH"));
 
       // BLOB TYPE
     } else if (mysqlDataTypeName.toLowerCase().indexOf("tinyblob") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("blob") >= 0
         || mysqlDataTypeName.toLowerCase().indexOf("mediumblob") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("longblob") >= 0) {
       return new BlobType(cols.getInt("COLUMN_SIZE"));
 
       // FLOAT and DOUBLE, REAL, DOUBLE PRECISION
     } else if (mysqlDataTypeName.toLowerCase().indexOf("float") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("double") >= 0
         || mysqlDataTypeName.toLowerCase().indexOf("real") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("double precision") >= 0) {
       return new FloatType(cols.getInt("COLUMN_SIZE"));
     } else if (mysqlDataTypeName.toLowerCase().indexOf("decimal") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("fixed") >= 0
         || mysqlDataTypeName.toLowerCase().indexOf("numeric") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("dec") >= 0) {
       return new DecimalType(cols.getInt("COLUMN_SIZE"), cols.getInt("DECIMAL_DIGITS"));
     } else if (mysqlDataTypeName.toLowerCase().indexOf("bit") >= 0 && cols.getInt("COLUMN_SIZE") == 0) {
       return new BooleanType();
     } else if (mysqlDataTypeName.toLowerCase().indexOf("bit") >= 0) {
       return new BitType(cols.getInt("COLUMN_SIZE"));
 
       // TINYINT, SMALLINT, MEDIUMINT, INTEGER, BIGINT
     } else if (mysqlDataTypeName.toLowerCase().indexOf("tinyint") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("smallint") >= 0
         || mysqlDataTypeName.toLowerCase().indexOf("mediumint") >= 0 || mysqlDataTypeName.toLowerCase().indexOf("int") >= 0
         || mysqlDataTypeName.toLowerCase().indexOf("bigint") >= 0) {
       return new IntType(cols.getInt("COLUMN_SIZE"));
 
       // DATETIME, DATE, TIMESTAMP, TIME, and YEAR
     } else if (mysqlDataTypeName.toLowerCase().indexOf("datetime") >= 0) {
       return new DateTimeType();
     } else if (mysqlDataTypeName.toLowerCase().indexOf("date") >= 0) {
       return new DateType();
     } else if (mysqlDataTypeName.toLowerCase().indexOf("timestamp") >= 0) {
       return new TimeStampType();
     } else if (mysqlDataTypeName.toLowerCase().indexOf("time") >= 0) {
       return new TimeType();
     } else if (mysqlDataTypeName.toLowerCase().indexOf("year") >= 0) {
       return new YearType();
 
       // ENUM
     } else if (mysqlDataTypeName.toLowerCase().indexOf("enum") >= 0) {
       Statement columns = connection.getConnection().createStatement();
       columns.execute("SHOW COLUMNS FROM " + table.getName() + " WHERE Field = \'" + cols.getString("COLUMN_NAME") + "\'");
       ResultSet resultSet = columns.getResultSet();
       if (resultSet.first()) {
         String enumDef = resultSet.getString("Type");
         return new EnumType(new EnumParser().parse(enumDef));        
       }
     }
     return new UnknownType();
   }
 
   
 }
