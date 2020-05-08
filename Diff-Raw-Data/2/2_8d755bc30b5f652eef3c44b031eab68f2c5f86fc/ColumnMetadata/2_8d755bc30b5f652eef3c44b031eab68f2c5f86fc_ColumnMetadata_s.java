 package com.nearinfinity.hbaseclient;
 
 /**
  * Created with IntelliJ IDEA.
  * User: acrute
  * Date: 8/15/12
  * Time: 4:49 PM
  * To change this template use File | Settings | File Templates.
  */
 public enum ColumnMetadata {
     NONE("None".getBytes()),
     IS_NULLABLE("IsNullable".getBytes()),
     PRIMARY_KEY("PrimaryKey".getBytes()),
     STRING("String".getBytes()),
     LONG("Long".getBytes()),
     ULONG("ULong".getBytes()),
     DOUBLE("Double".getBytes()),
     TIME("Time".getBytes()),
     DATE("Date".getBytes()),
     DATETIME("DateTime".getBytes()),
    BINARY("Binary".getBytes());
     DECIMAL("Decimal".getBytes());
 
     private byte[] value;
 
     ColumnMetadata(byte[] value) {
         this.value = value;
     }
 
     public byte[] getValue() {
         return this.value;
     }
 }
