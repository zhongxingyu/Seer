 package net.madz.db.core.meta.mutable.mysql.impl;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.madz.db.core.meta.immutable.ForeignKeyEntry;
 import net.madz.db.core.meta.immutable.mysql.MySQLColumnMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLIndexMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLSchemaMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLTableMetaData;
 import net.madz.db.core.meta.immutable.mysql.datatype.DataType;
 import net.madz.db.core.meta.immutable.mysql.enums.MySQLDataTypeEnum;
 import net.madz.db.core.meta.immutable.mysql.impl.MySQLColumnMetaDataImpl;
 import net.madz.db.core.meta.mutable.impl.BaseColumnMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLColumnMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLForeignKeyMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLIndexMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLSchemaMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLTableMetaDataBuilder;
 import net.madz.db.utils.ResourceManagementUtils;
 
 public final class MySQLColumnMetaDataBuilderImpl
         extends
         BaseColumnMetaDataBuilder<MySQLSchemaMetaDataBuilder, MySQLTableMetaDataBuilder, MySQLColumnMetaDataBuilder, MySQLForeignKeyMetaDataBuilder, MySQLIndexMetaDataBuilder, MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData>
         implements MySQLColumnMetaDataBuilder {
 
     private String characterSet;
     // private MySQLDataTypeEnum
     private String columnType;
     private long characterMaximumLength;
     private Integer numericPrecision;
     private Integer numericScale;
     private String collationName;
     private String columnKey;
     private String extra;
     private boolean isUnsigned;
     private boolean isZeroFill;
     private boolean isCollationWithBin;
     private LinkedList<String> typeValues = new LinkedList<String>();
 
     public MySQLColumnMetaDataBuilderImpl(MySQLTableMetaDataBuilder tableBuilder, String columnName) {
         super(tableBuilder, columnName);
     }
 
     @Override
     public MySQLColumnMetaDataBuilder build(Connection conn) throws SQLException {
         Statement stmt = conn.createStatement();
         ResultSet rs = null;
         stmt.executeQuery("USE information_schema;");
         try {
             rs = stmt.executeQuery("SELECT * FROM columns WHERE table_schema='" + this.tableBuilder.getTablePath().getParent().getName() + "' AND table_name='"
                     + this.tableBuilder.getTableName() + "' AND column_name='" + this.columnPath.getName() + "';");
             while ( rs.next() ) {
                 this.ordinalPosition = rs.getShort("ordinal_position");
                 this.defaultValue = rs.getString("column_default");
                 this.isNullable = rs.getBoolean("is_nullable");
                 this.sqlTypeName = MySQLDataTypeEnum.valueOf(rs.getString("data_type").toUpperCase()).name();
                 this.characterMaximumLength = rs.getLong("character_maximum_length");
                 this.characterOctetLength = rs.getLong("character_octet_length");
                 this.numericPrecision = rs.getInt("numeric_precision");
                 this.numericScale = rs.getInt("numeric_scale");
                 this.characterSet = rs.getString("character_set_name");
                 this.collationName = rs.getString("collation_name");
                 if ( null != this.collationName ) {
                     if ( this.collationName.toUpperCase().endsWith("_BIN") ) {
                         this.isCollationWithBin = true;
                     }
                 }
                 final String rawColumnType = rs.getString("column_type");
                 if ( null != rawColumnType ) {
                     if ( rawColumnType.toUpperCase().contains("UNSIGNED") ) {
                         setUnsigned(true);
                     }
                     if ( rawColumnType.toUpperCase().contains("ZEROFILL") ) {
                         setZeroFill(true);
                     }
                     if ( rawColumnType.toUpperCase().contains("ENUM") || rawColumnType.toUpperCase().contains("SET") ) {
                         final String[] result = rawColumnType.substring(rawColumnType.indexOf("(") + 1, rawColumnType.indexOf(")")).split(",");
                         for ( String value : result ) {
                             this.addTypeValue(value);
                         }
                     }
                 }
                this.columnType = rawColumnType;
                 if ( null != this.characterSet ) {
                    this.columnType += " CHARACTER SET " + this.characterSet;
                 }
                 if ( null != this.collationName ) {
                    this.columnType += " COLLATE " + this.collationName;
                 }
                 this.columnKey = rs.getString("column_key");
                 this.extra = rs.getString("extra");
                 if ( this.extra.equalsIgnoreCase("auto_increment") ) {
                     this.isAutoIncremented = true;
                 }
                 this.remarks = rs.getString("column_comment");
             }
         } finally {
             ResourceManagementUtils.closeResultSet(rs);
         }
         return this;
     }
 
     @Override
     public String getCharacterSet() {
         return characterSet;
     }
 
     @Override
     public String getColumnType() {
         return columnType;
     }
 
     @Override
     public long getCharacterMaximumLength() {
         return characterMaximumLength;
     }
 
     @Override
     public Integer getNumericPrecision() {
         return numericPrecision;
     }
 
     @Override
     public Integer getNumericScale() {
         return numericScale;
     }
 
     @Override
     public String getCollationName() {
         return collationName;
     }
 
     @Override
     public String getColumnKey() {
         return columnKey;
     }
 
     @Override
     public String getExtra() {
         return extra;
     }
 
     @Override
     public void setCharacterSet(String characterSet) {
         this.characterSet = characterSet;
     }
 
     @Override
     public void setColumnType(String columnType) {
         this.columnType = columnType;
     }
 
     @Override
     public void setCharacterMaximumLength(long characterMaximumLength) {
         this.characterMaximumLength = characterMaximumLength;
     }
 
     @Override
     public void setNumericPrecision(int numericPrecision) {
         this.numericPrecision = numericPrecision;
     }
 
     @Override
     public void setNumericScale(int numericScale) {
         this.numericScale = numericScale;
     }
 
     @Override
     public void setCollationName(String collationName) {
         this.collationName = collationName;
     }
 
     @Override
     public void setColumnKey(String columnKey) {
         this.columnKey = columnKey;
     }
 
     @Override
     public void setExtra(String extra) {
         this.extra = extra;
     }
 
     @Override
     public MySQLColumnMetaDataImpl createMetaData() {
         this.constructedMetaData = new MySQLColumnMetaDataImpl(this.tableBuilder.getMetaData(), this);
         return (MySQLColumnMetaDataImpl) this.constructedMetaData;
     }
 
     @Override
     public MySQLColumnMetaDataBuilder appendForeignKeyEntry(
             ForeignKeyEntry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData> entry) {
         this.fkList.add(entry);
         return this;
     }
 
     @Override
     public boolean isUnsigned() {
         return this.isUnsigned;
     }
 
     @Override
     public boolean isZeroFill() {
         return this.isZeroFill;
     }
 
     @Override
     public void setUnsigned(Boolean isUnsigned) {
         this.isUnsigned = isUnsigned;
     }
 
     @Override
     public void setZeroFill(Boolean isZeroFill) {
         this.isZeroFill = isZeroFill;
     }
 
     @Override
     public void setCollationWithBin(Boolean isCollationWithBin) {
         this.isCollationWithBin = isCollationWithBin;
     }
 
     @Override
     public boolean isCollationWithBin() {
         return this.isCollationWithBin;
     }
 
     @Override
     public List<String> getTypeValues() {
         return this.typeValues;
     }
 
     @Override
     public void addTypeValue(String typeValue) {
         this.typeValues.add(typeValue);
     }
 
     @Override
     public void setDataType(DataType dataType) {
         dataType.build(this);
     }
 }
