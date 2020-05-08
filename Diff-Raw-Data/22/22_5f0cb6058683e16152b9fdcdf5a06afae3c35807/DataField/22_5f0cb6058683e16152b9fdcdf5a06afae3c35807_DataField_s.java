 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons.database;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.sql.Blob;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 
 public class DataField {
     private final String name, table /* , type */;
     private final int size;
     private final Object data;
     private final FieldType ftype;
     private final int sqltype;
     private final boolean unsigned;
 
     public DataField(int column, ResultSetMetaData metaData, Object data)
             throws SQLException {
         this.sqltype = metaData.getColumnType(column);
         this.size = metaData.getColumnDisplaySize(column);
         this.data = data;
         this.ftype = sqlTypeParse(this.sqltype, this.size, data);
         this.typeCheck();
         this.name = metaData.getColumnLabel(column);
         this.table = metaData.getTableName(column);
         this.unsigned = metaData.getColumnTypeName(column).contains("UNSIGNED");
     }
 
     public DataField(int size, Object data) {
         this(size, false, data);
     }
 
     public DataField(FieldType type, int size, Object data) {
         this(type, size, false, data);
     }
 
     public DataField(int size, boolean unsigned, Object data) {
         this("", size, unsigned, data);
     }
 
     public DataField(FieldType type, int size, boolean unsigned, Object data) {
         this(type, "", size, unsigned, data);
     }
 
     public DataField(String name, int size, boolean unsigned, Object data) {
         this(name, "", size, unsigned, data);
     }
 
     public DataField(FieldType type, String name, int size, boolean unsigned,
             Object data) {
         this(type, name, "", size, unsigned, data);
     }
 
     public DataField(String name, String table, int size, boolean unsigned,
             Object data) {
         this(typeDetect(data), name, table, size, unsigned, data);
     }
 
     public DataField(FieldType type, String name, String table, int size,
             boolean unsigned, Object data) {
         this.sqltype = Types.NULL;
        this.ftype = type;
         this.name = name;
         this.table = table;
         this.size = size;
         this.unsigned = unsigned;
         this.data = data;
         this.typeCheck();
     }
 
     public DataField(int column, ResultSet resultset) throws SQLException {
         ResultSetMetaData metaData = resultset.getMetaData();
         this.name = metaData.getColumnLabel(column);
         this.table = metaData.getTableName(column);
         this.size = metaData.getColumnDisplaySize(column);
         if (metaData.getColumnType(column) == Types.BLOB
                 || metaData.getColumnType(column) == Types.LONGVARBINARY) {
             this.data = resultset.getBlob(column);
         } else if (metaData.getColumnType(column) == Types.CLOB) {
             this.data = resultset.getString(column);
         } else {
             this.data = resultset.getObject(column);
         }
         this.sqltype = metaData.getColumnType(column);
         this.unsigned = metaData.getColumnTypeName(column).contains("UNSIGNED");
         this.ftype = sqlTypeParse(this.sqltype, this.size, this.data);
     }
 
     private void typeCheck() {
         IllegalArgumentException e = new IllegalArgumentException("Data: "
                 + this.data.toString() + " doesn't match the type: "
                 + this.ftype.name());
         if (this.ftype.equals(FieldType.STRING)) {
             if (!(this.data instanceof String)) {
                 throw e;
             }
         } else if (this.ftype.equals(FieldType.INTEGER)
                 || this.ftype.equals(FieldType.REAL)) {
             if (!(this.data instanceof Number)) {
                 throw e;
             }
         } else if (this.ftype.equals(FieldType.DATE)) {
             if (!(this.data instanceof Date)) {
                 throw e;
             }
         } else if (this.ftype.equals(FieldType.BLOB)) {
             if (!(this.data instanceof Blob)) {
                 throw e;
             }
         } else if (this.ftype.equals(FieldType.BINARY)) {
             if (!(this.data instanceof byte[])) {
                 throw e;
             }
         } else if (this.ftype.equals(FieldType.BOOLEAN)) {
             if (!(this.data instanceof Boolean)) {
                 throw e;
             }
         }
     }
 
     private static FieldType typeDetect(Object data) {
         if (data == null) {
             return FieldType.NULL;
         } else if (data instanceof String) {
             return FieldType.STRING;
         } else if (data instanceof Number) {
             if (((Number) data).doubleValue() == ((Number) data).longValue()) {
                 return FieldType.INTEGER;
             } else {
                 return FieldType.REAL;
             }
         } else if (data instanceof Date) {
             return FieldType.DATE;
         } else if (data instanceof Blob) {
             return FieldType.BLOB;
         } else if (data instanceof byte[]) {
             return FieldType.BINARY;
         } else if (data instanceof Boolean) {
             return FieldType.BOOLEAN;
         }
         //TODO: DataManager.getLogManager().warning("Unknown data type: " + data.toString());
         return FieldType.UNKNOWN;
     }
 
     private static FieldType sqlTypeParse(int sqltype, int size, Object data) {
         switch (sqltype) {
         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
         case Types.CLOB:
             return FieldType.STRING;
         case Types.BLOB:
         case Types.LONGVARBINARY:
             return FieldType.BLOB;
         case Types.BOOLEAN:
             return FieldType.BOOLEAN;
         case Types.BIT:
             if (size <= 1) {
                 return FieldType.BOOLEAN;
             }
             /* falls through */
         case Types.BINARY:
         case Types.VARBINARY:
             return FieldType.BINARY;
         case Types.TINYINT:
             if (size <= 1) {
                 return FieldType.BOOLEAN;
             }
             /* falls through */
         case Types.SMALLINT:
         case Types.INTEGER:
         case Types.BIGINT:
             return FieldType.INTEGER;
         case Types.FLOAT:
         case Types.DOUBLE:
         case Types.REAL:
         case Types.DECIMAL:
             return FieldType.REAL;
         case Types.DATE:
         case Types.TIMESTAMP:
         case Types.TIME:
             return FieldType.DATE;
         case Types.NULL:
             return FieldType.NULL;
         default:
             //TODO: DataManager.getLogManager().warning("Unknown SQL type: " + sqltype + " Field data:" + data.toString());
             return FieldType.UNKNOWN;
         }
     }
 
     public String getFieldName() {
         return this.name;
     }
 
     public FieldType getFieldType() {
         return this.ftype;
     }
 
     public int getSQLType() {
         return this.sqltype;
     }
 
     public int getFieldSize() {
         return this.size;
     }
 
     public String getTable() {
         return this.table;
     }
 
     public Object getData() {
         return this.data;
     }
 
     public String getString() {
         if (this.getFieldType().equals(FieldType.STRING)) {
             return (String) this.data;
         } else if (this.getFieldType().equals(FieldType.BINARY)
                 || this.getFieldType().equals(FieldType.BLOB)) {
             return new String(this.getBytes());
         }
         return this.data.toString();
     }
 
     public int getInt() {
         return (int) this.getLong();
     }
 
     public long getLong() {
         if (this.getFieldType().equals(FieldType.INTEGER)
                 || this.getFieldType().equals(FieldType.REAL)) {
             return ((Number) this.data).longValue();
         } else if (this.getFieldType().equals(FieldType.BOOLEAN)) {
             return ((Boolean) this.data).booleanValue() ? 1 : 0;
         } else if (this.getFieldType().equals(FieldType.DATE)) {
             return ((Date) this.data).getTime();
         } else if (this.getFieldType().equals(FieldType.BINARY)
                 || this.getFieldType().equals(FieldType.BLOB)) {
             byte[] bytes = { 0, 0, 0, 0, 0, 0, 0, 0 };
             byte[] bytes1 = this.getBytes();
             if (bytes1.length >= 8) {
                 System.arraycopy(bytes1, 0, bytes, 0, 8);
             } else {
                 System.arraycopy(bytes1, 0, bytes, 8 - bytes1.length,
                         bytes1.length);
             }
             return ByteBuffer.wrap(bytes).getLong();
         } else if (this.getFieldType().equals(FieldType.STRING)) {
             try {
                 return Long.parseLong((String) this.data);
             } catch (NumberFormatException e) {
             }
             return new Double(this.getDouble()).longValue();
         }
         return 0;
     }
 
     public BigInteger getBigInt() {
         try {
             if (this.data instanceof BigInteger) {
                 return (BigInteger) this.data;
             } else if (this.getFieldType().equals(FieldType.BOOLEAN)) {
                 return ((Boolean) this.data).booleanValue() ? BigInteger.ONE
                         : BigInteger.ZERO;
             } else if (this.getFieldType().equals(FieldType.BINARY)
                     || this.getFieldType().equals(FieldType.BLOB)) {
                 byte[] bytes = this.getBytes();
                 return new BigInteger(bytes);
             } else if (this.getFieldType().equals(FieldType.STRING)) {
                 return new BigInteger(this.data.toString());
             } else {
                 long l = 0;
                 if (this.getFieldType().equals(FieldType.INTEGER)
                         || this.getFieldType().equals(FieldType.REAL)) {
                     l = ((Number) this.data).longValue();
                 } else if (this.getFieldType().equals(FieldType.DATE)) {
                     l = ((Date) this.data).getTime();
                 } else {
                     return null;
                 }
                 return new BigInteger(String.valueOf(l));
             }
         } catch (NumberFormatException e) {
         }
         return null;
     }
 
     public double getDouble() {
         if (this.getFieldType().equals(FieldType.INTEGER)
                 || this.getFieldType().equals(FieldType.REAL)) {
             return ((Number) this.data).doubleValue();
         } else if (this.getFieldType().equals(FieldType.BOOLEAN)) {
             return ((Boolean) this.data).booleanValue() ? 1 : 0;
         } else if (this.getFieldType().equals(FieldType.DATE)) {
             return new Long(((Date) this.data).getTime()).doubleValue();
         } else if (this.getFieldType().equals(FieldType.BINARY)
                 || this.getFieldType().equals(FieldType.BLOB)) {
             byte[] bytes = { 0, 0, 0, 0, 0, 0, 0, 0 };
             byte[] bytes1 = this.getBytes();
             if (bytes1.length >= 8) {
                 System.arraycopy(bytes1, 0, bytes, 0, 8);
             } else {
                 System.arraycopy(bytes1, 0, bytes, 8 - bytes1.length,
                         bytes1.length);
             }
             return ByteBuffer.wrap(bytes).getLong();
         } else if (this.getFieldType().equals(FieldType.STRING)) {
             try {
                 return Double.parseDouble((String) this.data);
             } catch (NumberFormatException e) {
             }
             try {
                 return Double.parseDouble(((String) this.data)
                         .replace(',', '.'));
             } catch (NumberFormatException e) {
                 e.getCause();
             }
         }
         return 0;
     }
 
     public float getFloat() {
         return (float) this.getDouble();
     }
 
     public BigDecimal getDecimal() {
         try {
             if (this.data instanceof BigDecimal) {
                 return (BigDecimal) this.data;
             } else if (this.getFieldType().equals(FieldType.BOOLEAN)) {
                 return ((Boolean) this.data).booleanValue() ? BigDecimal.ONE
                         : BigDecimal.ZERO;
             } else if (this.getFieldType().equals(FieldType.STRING)) {
                 return new BigDecimal(this.data.toString());
             } else if (this.getFieldType().equals(FieldType.INTEGER)
                     || this.getFieldType().equals(FieldType.REAL)) {
                 return new BigDecimal(((Number) this.data).doubleValue());
             } else if (this.getFieldType().equals(FieldType.DATE)) {
                 return new BigDecimal(((Date) this.data).getTime());
             }
         } catch (NumberFormatException e) {
         }
         return null;
     }
 
     public byte[] getBytes() {
         if (this.getFieldType().equals(FieldType.BINARY)) {
             return (byte[]) this.data;
         } else if (this.getFieldType().equals(FieldType.BLOB)) {
             try {
                 return ((Blob) this.data).getBytes(1,
                         (int) ((Blob) this.data).length());
             } catch (SQLException e) {
                 e.getClass();
             }
         } else if (this.getFieldType().equals(FieldType.BOOLEAN)) {
             return ByteBuffer.allocate(1)
                     .put((byte) (((Boolean) this.data).booleanValue() ? 1 : 0))
                     .array();
         } else if (this.getFieldType().equals(FieldType.INTEGER)) {
             return ByteBuffer.allocate(8).putLong(this.getLong()).array();
         } else if (this.getFieldType().equals(FieldType.REAL)) {
             return ByteBuffer.allocate(8).putDouble(this.getDouble()).array();
         } else if (this.getFieldType().equals(FieldType.STRING)) {
             return this.data.toString().getBytes();
         }
         return null;
     }
 
     public Date getDate() {
         if (this.getFieldType().equals(FieldType.DATE)) {
             return (Date) this.data;
         } else if (this.getFieldType().equals(FieldType.INTEGER)) {
             return new Date(this.getLong());
         } else if (this.getFieldType().equals(FieldType.STRING)) {
             try {
                 return DateFormat.getDateInstance().parse((String) this.data);
             } catch (ParseException e) {
             }
             try {
                 return DateFormat.getDateTimeInstance().parse(
                         (String) this.data);
             } catch (ParseException e) {
             }
             try {
                 return DateFormat.getTimeInstance().parse((String) this.data);
             } catch (ParseException e) {
             }
             try {
                 return DateFormat.getInstance().parse((String) this.data);
             } catch (ParseException e) {
             }
         }
         return null;
     }
 
     public Blob getBlob() {
         if (this.getFieldType().equals(FieldType.BLOB)) {
             return (Blob) this.data;
         }
         return null;
     }
 
     public boolean getBool() {
         if (this.getFieldType().equals(FieldType.BOOLEAN)) {
             return (Boolean) this.data;
         } else if (this.getFieldType().equals(FieldType.INTEGER)
                 || this.getFieldType().equals(FieldType.REAL)
                 || this.getFieldType().equals(FieldType.DATE)) {
             return this.getLong() != 0;
         } else if (this.getFieldType().equals(FieldType.BINARY)
                 || this.getFieldType().equals(FieldType.BLOB)
                 || this.getFieldType().equals(FieldType.STRING)) {
             String s = this.getString();
             return (s != null) && !s.isEmpty();
         }
         return false;
     }
 
     public boolean isNull() {
         return this.getFieldType().equals(FieldType.NULL);
     }
 
     public boolean isUnsigned() {
         return this.unsigned;
     }
 
     @Override
     public String toString() {
         return "DataField " + this.getFieldType().name() + "(" + this.size
                 + ") " + this.name + " = " + this.data.toString();
     }
 }
