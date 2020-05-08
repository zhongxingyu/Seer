 package net.madz.db.core.meta.immutable.mysql.impl;
 
 import net.madz.db.core.meta.immutable.impl.SchemaMetaDataImpl;
 import net.madz.db.core.meta.immutable.mysql.MySQLColumnMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLIndexMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLSchemaMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLTableMetaData;
 
 public final class MySQLSchemaMetaDataImpl extends
         SchemaMetaDataImpl<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData> implements
         MySQLSchemaMetaData {
 
     // TODO [Jan 22, 2013][barry][Done] Use modifier final with immutable fields
     private final String charSet;
     private final String collation;
 
     public MySQLSchemaMetaDataImpl(MySQLSchemaMetaData metaData) {
         super(metaData);
         this.charSet = metaData.getCharSet();
         this.collation = metaData.getCollation();
     }
 
     @Override
     public String getCharSet() {
         return charSet;
     }
 
     @Override
     public String getCollation() {
         return collation;
     }
 
 }
