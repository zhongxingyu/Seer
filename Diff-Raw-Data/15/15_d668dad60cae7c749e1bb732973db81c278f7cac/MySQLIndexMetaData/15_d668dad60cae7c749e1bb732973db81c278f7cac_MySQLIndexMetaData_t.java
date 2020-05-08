 package net.madz.db.core.meta.immutable.mysql;
 
 import net.madz.db.core.meta.immutable.IndexMetaData;
 import net.madz.db.core.meta.immutable.mysql.enums.MySQLIndexMethod;
 
 public interface MySQLIndexMetaData extends
         IndexMetaData<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData> {
 
     boolean isNull();
 
     MySQLIndexMethod getIndexMethod();
 }
