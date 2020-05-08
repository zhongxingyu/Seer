 package com.craftfire.commons.util;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.sql.Blob;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 
 /**
  * An immutable implementation of ValueHolder with automatic type detection in constructor, and complex type conversion system.
  * <p>
  * NOTE: Conversions don't care about unsigned setting yet.
  */
 public class ValueHolderBase extends AbstractValueHolder {
    protected final String name;
    protected final Object value;
    protected final ValueType type;
    protected final boolean unsigned;
 
     // TODO: Make the conversions consider the unsigned setting
 
     /**
      * Detects the ValueType of specified object.
      * 
      * @param value  the object
      * @return       detected ValueType of the object
      */
     protected static ValueType typeDetect(Object value) {
         if (value == null) {
             return ValueType.NULL;
         } else if (value instanceof String) {
             return ValueType.STRING;
         } else if (value instanceof Number) {
             if (((Number) value).doubleValue() == ((Number) value).longValue()) {
                 return ValueType.INTEGER;
             } else {
                 return ValueType.REAL;
             }
         } else if (value instanceof Date) {
             return ValueType.DATE;
         } else if (value instanceof Blob) {
             return ValueType.BLOB;
         } else if (value instanceof byte[]) {
             return ValueType.BINARY;
         } else if (value instanceof Boolean) {
             return ValueType.BOOLEAN;
         }
         // TODO: DataManager.getLogManager().warning("Unknown data type: " + data.toString());
         return ValueType.UNKNOWN;
     }
 
     /**
      * Checks if the value of the holder matches the ValueType.
      * 
     * @throws IllegalArgumentException if the value doesn't match the type 
      */
     protected void typeCheck() {
         IllegalArgumentException e = new IllegalArgumentException("Data: "
                 + this.value + " doesn't match the type: "
                 + this.type.name());
         if (this.type.equals(ValueType.STRING)) {
             if (!(this.value instanceof String)) {
                 throw e;
             }
         } else if (this.type.equals(ValueType.INTEGER) || this.type.equals(ValueType.REAL)) {
             if (!(this.value instanceof Number)) {
                 throw e;
             }
         } else if (this.type.equals(ValueType.DATE)) {
             if (!(this.value instanceof Date)) {
                 throw e;
             }
         } else if (this.type.equals(ValueType.BLOB)) {
             if (!(this.value instanceof Blob)) {
                 throw e;
             }
         } else if (this.type.equals(ValueType.BINARY)) {
             if (!(this.value instanceof byte[])) {
                 throw e;
             }
         } else if (this.type.equals(ValueType.BOOLEAN)) {
             if (!(this.value instanceof Boolean)) {
                 throw e;
             }
         }
     }
 
     /**
      * Creates a new ValueHolderBase with specified value and automatically detected type (treated as signed).
      * 
      * @param value  the value
      */
     public ValueHolderBase(Object value) {
         this(false, value);
     }
 
     /**
      * Creates a new ValueHolderBase with specified value and specified type (treated as signed).
      * 
      * @param  type                     type of the value
      * @param  value                    the value
      * @throws IllegalArgumentException if the value doesn't match the type
      */
     public ValueHolderBase(ValueType type, Object value) {
         this(type, false, value);
     }
 
     /**
      * Creates a new ValueHolderBase with specified value and automatically detected type.
      * 
      * @param unsigned  weather or not the value is unsigned
      * @param value     the value
      */
     public ValueHolderBase(boolean unsigned, Object value) {
         this("", unsigned, value);
     }
 
     /**
      * Creates a new ValueHolderBase with specified value and specified type.
      * 
      * @param  type                     type of the value
      * @param  unsigned                 weather or not the value is unsigned
      * @param  value                    the value
      * @throws IllegalArgumentException if the value doesn't match the type
      */
     public ValueHolderBase(ValueType type, boolean unsigned, Object value) {
         this(type, "", unsigned, value);
     }
 
     /**
      * Creates a new ValueHolderBase with specified value and holder name, and automatically detected type.
      * 
      * @param name      name of the holder
      * @param unsigned  weather or not the value is unsigned
      * @param value     the value
      */
     public ValueHolderBase(String name, boolean unsigned, Object data) {
         this.type = typeDetect(data);
         this.name = name;
         this.unsigned = unsigned;
         this.value = data;
     }
 
     /**
      * Creates a new ValueHolderBase with specified value, type, and holder name.
      * 
      * @param  type                     type of the value
      * @param  name                     name of the holder
      * @param  unsigned                 weather or not the value is unsigned
      * @param  value                    the value
      * @throws IllegalArgumentException if the value doesn't match the type
      */
     public ValueHolderBase(ValueType type, String name, boolean unsigned, Object value) {
         if (value == null) {
             this.type = ValueType.NULL;
         } else {
             this.type = type;
         }
         this.name = name;
         this.unsigned = unsigned;
         this.value = value;
         typeCheck();
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getFieldName()
      */
     @Override
     public String getName() {
         return this.name;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getFieldType()
      */
     @Override
     public ValueType getType() {
         return this.type;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getData()
      */
     @Override
     public Object getValue() {
         return this.value;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getString()
      */
     @Override
    public String getString() {
        return getString(this.value.toString());
    }

    /* (non-Javadoc)
     * @see com.craftfire.commons.database.IValueHolder#getString()
     */
    @Override
     public String getString(String defaultValue) {
         if (getType().equals(ValueType.STRING)) {
             return (String) this.value;
         } else if (getType().equals(ValueType.BINARY)
                 || getType().equals(ValueType.BLOB)) {
             byte[] bytes = getBytes();
             if (bytes != null) {
                 return new String(bytes);
             }
         }
         if (isNull()) {
             return defaultValue;
         }
         return this.value.toString();
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getInt()
      */
     @Override
     public int getInt(int defaultValue) {
         return (int) getLong(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getLong()
      */
     @Override
     public long getLong(long defaultValue) {
         if (getType().equals(ValueType.INTEGER)
                 || getType().equals(ValueType.REAL)) {
             return ((Number) this.value).longValue();
         } else if (getType().equals(ValueType.BOOLEAN)) {
             return ((Boolean) this.value).booleanValue() ? 1 : 0;
         } else if (getType().equals(ValueType.DATE)) {
             return ((Date) this.value).getTime();
         } else if (getType().equals(ValueType.BINARY)
                 || getType().equals(ValueType.BLOB)) {
             byte[] bytes = { 0, 0, 0, 0, 0, 0, 0, 0 };
             byte[] bytes1 = getBytes();
             if (bytes1.length >= 8) {
                 System.arraycopy(bytes1, 0, bytes, 0, 8);
             } else {
                 System.arraycopy(bytes1, 0, bytes, 8 - bytes1.length,
                         bytes1.length);
             }
             return ByteBuffer.wrap(bytes).getLong();
         } else if (getType().equals(ValueType.STRING)) {
             try {
                 return Long.parseLong((String) this.value);
             } catch (NumberFormatException e) {
             }
             return new Double(getDouble()).longValue();
         }
         return defaultValue;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getBigInt()
      */
     @Override
     public BigInteger getBigInt(BigInteger defaultValue) {
         try {
             if (this.value instanceof BigInteger) {
                 return (BigInteger) this.value;
             } else if (getType().equals(ValueType.BOOLEAN)) {
                 return ((Boolean) this.value).booleanValue() ? BigInteger.ONE
                         : BigInteger.ZERO;
             } else if (getType().equals(ValueType.BINARY)
                     || getType().equals(ValueType.BLOB)) {
                 byte[] bytes = getBytes();
                 return new BigInteger(bytes);
             } else if (getType().equals(ValueType.STRING)) {
                 return new BigInteger(this.value.toString());
             } else {
                 long l = 0;
                 if (getType().equals(ValueType.INTEGER)
                         || getType().equals(ValueType.REAL)) {
                     l = ((Number) this.value).longValue();
                 } else if (getType().equals(ValueType.DATE)) {
                     l = ((Date) this.value).getTime();
                 } else {
                     return defaultValue;
                 }
                return new BigInteger(String.valueOf(l));
             }
         } catch (NumberFormatException e) {
         }
         return defaultValue;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getDouble()
      */
     @Override
     public double getDouble(double defaultValue) {
         if (getType().equals(ValueType.INTEGER)
                 || getType().equals(ValueType.REAL)) {
             return ((Number) this.value).doubleValue();
         } else if (getType().equals(ValueType.BOOLEAN)) {
             return ((Boolean) this.value).booleanValue() ? 1 : 0;
         } else if (getType().equals(ValueType.DATE)) {
             return new Long(((Date) this.value).getTime()).doubleValue();
         } else if (getType().equals(ValueType.BINARY)
                 || getType().equals(ValueType.BLOB)) {
             byte[] bytes = { 0, 0, 0, 0, 0, 0, 0, 0 };
             byte[] bytes1 = getBytes();
             if (bytes1.length >= 8) {
                 System.arraycopy(bytes1, 0, bytes, 0, 8);
             } else {
                 System.arraycopy(bytes1, 0, bytes, 8 - bytes1.length,
                         bytes1.length);
             }
             return ByteBuffer.wrap(bytes).getLong();
         } else if (getType().equals(ValueType.STRING)) {
             try {
                 return Double.parseDouble((String) this.value);
             } catch (NumberFormatException e) {
             }
             try {
                 return Double.parseDouble(((String) this.value)
                         .replace(',', '.'));
             } catch (NumberFormatException e) {
                 e.getCause();
             }
         }
         return defaultValue;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getFloat()
      */
     @Override
     public float getFloat(float defaultValue) {
         return (float) getDouble(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getDecimal()
      */
     @Override
     public BigDecimal getDecimal(BigDecimal defaultValue) {
         try {
             if (this.value instanceof BigDecimal) {
                 return (BigDecimal) this.value;
             } else if (getType().equals(ValueType.BOOLEAN)) {
                 return ((Boolean) this.value).booleanValue() ? BigDecimal.ONE
                         : BigDecimal.ZERO;
             } else if (getType().equals(ValueType.STRING)) {
                 return new BigDecimal(this.value.toString());
             } else if (getType().equals(ValueType.INTEGER)
                     || getType().equals(ValueType.REAL)) {
                 return new BigDecimal(((Number) this.value).doubleValue());
             } else if (getType().equals(ValueType.DATE)) {
                 return new BigDecimal(((Date) this.value).getTime());
             }
         } catch (NumberFormatException e) {
         }
         return defaultValue;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getBytes()
      */
     @Override
     public byte[] getBytes(byte[] defaultValue) {
         if (getType().equals(ValueType.BINARY)) {
             return (byte[]) this.value;
         } else if (getType().equals(ValueType.BLOB)) {
             try {
                 return ((Blob) this.value).getBytes(1,
                         (int) ((Blob) this.value).length());
             } catch (SQLException ignore) {
             }
         } else if (getType().equals(ValueType.BOOLEAN)) {
             return ByteBuffer.allocate(1)
                     .put((byte) (((Boolean) this.value).booleanValue() ? 1 : 0))
                     .array();
         } else if (getType().equals(ValueType.INTEGER)) {
             return ByteBuffer.allocate(8).putLong(getLong(0)).array();
         } else if (getType().equals(ValueType.REAL)) {
             return ByteBuffer.allocate(8).putDouble(getDouble()).array();
         } else if (getType().equals(ValueType.STRING)) {
             return this.value.toString().getBytes();
         }
         return defaultValue;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getDate()
      */
     @Override
     public Date getDate(Date defaultValue) {
         if (getType().equals(ValueType.DATE)) {
             return (Date) this.value;
         } else if (getType().equals(ValueType.INTEGER)) {
             return new Date(getLong(0));
         } else if (getType().equals(ValueType.STRING)) {
             try {
                 return DateFormat.getDateInstance().parse((String) this.value);
             } catch (ParseException e) {
             }
             try {
                 return DateFormat.getDateTimeInstance().parse(
                         (String) this.value);
             } catch (ParseException e) {
             }
             try {
                 return DateFormat.getTimeInstance().parse((String) this.value);
             } catch (ParseException e) {
             }
             try {
                 return DateFormat.getInstance().parse((String) this.value);
             } catch (ParseException e) {
             }
         }
         return defaultValue;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getBlob()
      */
     @Override
     public Blob getBlob(Blob defaultValue) {
         if (getType().equals(ValueType.BLOB)) {
             return (Blob) this.value;
         }
         return defaultValue;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#getBool()
      */
     @Override
     public boolean getBool(boolean defaultValue) {
         if (getType().equals(ValueType.BOOLEAN)) {
             return (Boolean) this.value;
         } else if (getType().equals(ValueType.INTEGER)
                 || getType().equals(ValueType.REAL)
                 || getType().equals(ValueType.DATE)) {
             return getLong(0) != 0;
         } else if (getType().equals(ValueType.BINARY)
                 || getType().equals(ValueType.BLOB)
                 || getType().equals(ValueType.STRING)) {
             String s = getString(null);
             return (s != null) && !s.isEmpty();
         }
         return defaultValue;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#isNull()
      */
     @Override
     public boolean isNull() {
         return getType().equals(ValueType.NULL);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.database.IValueHolder#isUnsigned()
      */
     @Override
     public boolean isUnsigned() {
         return this.unsigned;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return "ValueHolder " + getType().name() + " " + this.name + " = " + this.value;
     }
 
 }
