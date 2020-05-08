 package net.madz.db.core.meta.mutable.mysql.impl;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import net.madz.db.core.meta.DottedPath;
 import net.madz.db.core.meta.immutable.impl.MetaDataResultSet;
 import net.madz.db.core.meta.immutable.mysql.MySQLColumnMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLIndexMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLSchemaMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLTableMetaData;
 import net.madz.db.core.meta.immutable.mysql.impl.MySQLSchemaMetaDataImpl;
 import net.madz.db.core.meta.mutable.impl.BasedSchemaMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLColumnMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLForeignKeyMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLIndexMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLSchemaMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLTableMetaDataBuilder;
 
 public class MySQLSchemaMetaDataBuilderImpl
         extends
         BasedSchemaMetaDataBuilder<MySQLSchemaMetaDataBuilder, MySQLTableMetaDataBuilder, MySQLColumnMetaDataBuilder, MySQLForeignKeyMetaDataBuilder, MySQLIndexMetaDataBuilder, MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData>
         implements MySQLSchemaMetaDataBuilder {
 
     private String charSet;
     private String collation;
 
     public MySQLSchemaMetaDataBuilderImpl(DottedPath schemaPath) throws SQLException {
         super(schemaPath);
     }
 
     public MySQLSchemaMetaDataBuilder build(Connection conn) throws SQLException {
         System.out.println("Mysql schema builder");
         Statement stmt = conn.createStatement();
         stmt.executeQuery("use information_schema;");
         ResultSet rs = stmt.executeQuery("select * from SCHEMATA where schema_name = '" + schemaPath.getName() + "'");
         while ( rs.next() && rs.getRow() == 1 ) {
             charSet = rs.getString("DEFAULT_CHARACTER_SET_NAME");
             collation = rs.getString("DEFAULT_COLLATION_NAME");
         }
        rs = stmt.executeQuery("SELECT * FROM tables INNER JOIN character_sets ON default_collate_name = table_collation WHERE schema_name = '" + schemaPath.getName() + "'");
         MetaDataResultSet<MySQLTableDbMetaDataEnum> rsMd = new MetaDataResultSet<MySQLTableDbMetaDataEnum>(rs, MySQLTableDbMetaDataEnum.values());
         while ( rsMd.next() ) {
             final MySQLTableMetaDataBuilder table = new MySQLTableMetaDataBuilderImpl(this).build(rsMd,conn);
             appendTableMetaDataBuilder(table);
         }
         return this;
     }
 
     @Override
     public String getCharSet() {
         return this.charSet;
     }
 
     @Override
     public String getCollation() {
         return this.collation;
     }
 
     @Override
     public MySQLSchemaMetaData getMetaData() {
         return new MySQLSchemaMetaDataImpl(this);
     }
 
 }
