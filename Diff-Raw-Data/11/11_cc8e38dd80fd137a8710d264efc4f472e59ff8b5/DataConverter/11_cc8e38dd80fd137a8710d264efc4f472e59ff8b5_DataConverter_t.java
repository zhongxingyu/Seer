 package org.plasma.sdo.helper;
 
 
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.joda.time.format.ISOPeriodFormat;
 import org.joda.time.format.PeriodFormatter;
 import org.plasma.config.PlasmaConfig;
 import org.plasma.sdo.DataType;
 import org.plasma.sdo.PlasmaDataObjectConstants;
 import org.plasma.sdo.PlasmaDataObjectException;
 
 import commonj.sdo.Type;
 
 public class DataConverter {
     
     private static Log log = LogFactory.getFactory().getInstance(DataConverter.class);
     static public DataConverter INSTANCE = initializeInstance();
 
     private static Map<Class<?>, Map<String,DataType>> javaClassToAllowableTypesMap;
     
     private static Map<Class<?>, Method> javaClassToConverterFromMethodMap;
     
     private DateFormat timeFormat;
     private DateFormat dateFormat;
     private DateFormat dateTimeFormat;
     private PeriodFormatter durationFormat;
     private DateFormat dayFormat;
     private DateFormat monthFormat;
     private DateFormat monthDayFormat;
     private DateFormat yearFormat;
     private DateFormat yearMonthFormat;
     private DateFormat yearMonthDayFormat;
     
     private DataConverter() {
     	timeFormat = new SimpleDateFormat("HH:mm:ss'.'SSS'Z'");
     	timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
     	
     	dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
         dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 
     	// Note: the Java class for SDO dataTime datatype is String. This seems unfortunate
     	// because a java.util.Date is certainly capable of representing
     	// far more precision that than month/day/year. It seems also
         // that users would expect generated method signatures for
         // dataTime to be java.util.Date. Have to comply w/the spec though
         // but can compensate for this by allowing more precision
         // in the SDO date datatype. See above data format which allows
         // seconds precision.
         dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
         dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
  
     	durationFormat = ISOPeriodFormat.standard();
     	
     	dayFormat = new SimpleDateFormat("dd");
     	dayFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 
     	monthFormat = new SimpleDateFormat("MM");
     	monthFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
     	
     	monthDayFormat = new SimpleDateFormat("MM-dd");
     	monthDayFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 
     	yearFormat = new SimpleDateFormat("yyyy");
     	yearFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 
     	yearMonthFormat = new SimpleDateFormat("yyyy-MM");
     	yearMonthFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 
     	yearMonthDayFormat = new SimpleDateFormat("yyyy-MM-dd");
     	yearMonthDayFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
     	
         if (javaClassToAllowableTypesMap == null) {
             javaClassToAllowableTypesMap = new HashMap<Class<?>, Map<String,DataType>>();
             for (int i = 0; i < DataType.values().length; i++) {
                 DataType dataTypeEnum = DataType.values()[i];
                 
                 Class<?> javaClass = toPrimitiveJavaClass(dataTypeEnum);
                 
                 Map<String,DataType> allowableMap = new HashMap<String,DataType>();
                 List<DataType> list = getAllowableTargetTypes(dataTypeEnum);
                 for (DataType allowableType : list)
                     allowableMap.put(allowableType.toString(), allowableType);                
                 javaClassToAllowableTypesMap.put(javaClass, allowableMap);
             } 
         }
         if (javaClassToConverterFromMethodMap == null) {
         	javaClassToConverterFromMethodMap = new HashMap<Class<?>, Method>();
         	Map<Class<?>, Method> map = javaClassToConverterFromMethodMap;
         	try {
         	
         		Method method = this.getClass().getMethod("fromBoolean", Type.class, boolean.class);
         		map.put(Boolean.class, method);
         		map.put(boolean.class, method);
         		
         		method = this.getClass().getMethod("fromByte", Type.class, byte.class);
         		map.put(Byte.class, method);
         		map.put(byte.class, method);
         		
         		method = this.getClass().getMethod("fromBytes", Type.class, byte[].class);
         		map.put(Byte[].class, method);
         		map.put(byte[].class, method);
         		
         		method = this.getClass().getMethod("fromCharacter", Type.class, char.class);
         		map.put(Character.class, method);
         		map.put(char.class, method);
         		
         		method = this.getClass().getMethod("fromDate", Type.class, Date.class);
         		map.put(Date.class, method);
         		
         		method = this.getClass().getMethod("fromDecimal", Type.class, BigDecimal.class);
         		map.put(BigDecimal.class, method);
         		
         		method = this.getClass().getMethod("fromDouble", Type.class, double.class);
         		map.put(Double.class, method);
         		map.put(double.class, method);
         		
         		method = this.getClass().getMethod("fromFloat", Type.class, float.class);
         		map.put(Float.class, method);
         		map.put(float.class, method);
         		
         		method = this.getClass().getMethod("fromInt", Type.class, int.class);
         		map.put(int.class, method);
         		map.put(Integer.class, method);
         		
         		method = this.getClass().getMethod("fromInteger", Type.class, BigInteger.class); 
         		map.put(BigInteger.class, method);
         		
         		method = this.getClass().getMethod("fromLong", Type.class, long.class);
         		map.put(Long.class, method);
         		map.put(long.class, method);
 
         		method = this.getClass().getMethod("fromShort", Type.class, short.class);
         		map.put(Short.class, method);
         		map.put(short.class, method);
         		
         		method = this.getClass().getMethod("fromString", Type.class, String.class);
         		map.put(String.class, method);
         		
         		//Class<?> stringListClass = (java.lang.Class<java.util.List<java.lang.String>>)new ArrayList<java.lang.String>().getClass();        		
         		method = this.getClass().getMethod("fromStrings", Type.class, List.class);         		
         		map.put(List.class, method);
         	
         	}
         	catch (NoSuchMethodException e) {
         		log.error(e.getMessage(), e);
         	}
         }
     }
     
     private static synchronized DataConverter initializeInstance()
     {
         if (INSTANCE == null)
             INSTANCE = new DataConverter();
         return INSTANCE;
     }    
  
     public DateFormat getDateTimeFormat() {
         return dateTimeFormat;
     }
 
     public DateFormat getTimeFormat() {
 		return timeFormat;
 	}
 
 	public DateFormat getDateFormat() {
 		return dateFormat;
 	}
 
 	public PeriodFormatter getDurationFormat() {
 		return durationFormat;
 	}
 
 	public DateFormat getDayFormat() {
 		return dayFormat;
 	}
 
 	public DateFormat getMonthFormat() {
 		return monthFormat;
 	}
 
 	public DateFormat getMonthDayFormat() {
 		return monthDayFormat;
 	}
 
 	public DateFormat getYearFormat() {
 		return yearFormat;
 	}
 
 	public DateFormat getYearMonthFormat() {
 		return yearMonthFormat;
 	}
 
 	public DateFormat getYearMonthDayFormat() {
 		return yearMonthDayFormat;
 	}
 
     public Object convert(Type targetType, Object value) {
         
         if (!targetType.isDataType())
             throw new IllegalArgumentException("type " + targetType.getURI() + "#" 
                     + targetType.getName() + " is not a data-type");
 
         Method method = javaClassToConverterFromMethodMap.get(value.getClass());
         try {
 			return method.invoke(this, targetType, value);
 		} catch (IllegalArgumentException e) {
 		    throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 		    throw new RuntimeException(e);
 		} catch (InvocationTargetException e) {
 			if (e.getTargetException() instanceof InvalidDataConversionException)
 			    throw (InvalidDataConversionException)e.getTargetException();
 			else
 			    throw new RuntimeException(e.getTargetException());
 		}
     }
     
     public Object convert(Type targetType, Type sourceType, Object value) {
         if (!targetType.isDataType())
             throw new IllegalArgumentException("type " + targetType.getURI() + "#" 
                     + targetType.getName() + " is not a data-type");
         if (!sourceType.isDataType())
             throw new IllegalArgumentException("type " + sourceType.getURI() + "#" 
                     + sourceType.getName() + " is not a data-type");
         DataType targetDataType = DataType.valueOf(targetType.getName());
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
     
         switch (targetDataType) {
         case Boolean: return toBoolean(sourceType, value);
         case Byte:    return toByte(sourceType, value); 
         case Bytes:   return toBytes(sourceType, value); 
         case Character: return toCharacter(sourceType, value);
         case Decimal:   return toDecimal(sourceType, value);
         case Double:   return toDouble(sourceType, value);
         case Float:    return toFloat(sourceType, value);
         case Int:      return toInt(sourceType, value);
         case Integer:  return toInteger(sourceType, value);
         case Long:     return toLong(sourceType, value);
         case Short:    return toShort(sourceType, value);
         case String:   return toString(sourceType, value);
         case Strings:  return toStrings(sourceType, value);
         case Date:     return toDate(sourceType, value);
         case Duration: 
         case DateTime: 
         case Day:      
         case Month:    
         case MonthDay: 
         case Year:     
         case YearMonth:
         case YearMonthDay:
         case Time:  
             return toTemporalDataType(targetType, sourceType, value);
         case URI:      
         case Object:   
         default:
             throw new InvalidDataConversionException(targetDataType, sourceDataType, value);
         }        
     }       
 
     public boolean toBoolean(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Boolean:
             return ((Boolean)value).booleanValue();
         case String:
             return Boolean.parseBoolean(value.toString());
         default: 
             throw new InvalidDataConversionException(DataType.Boolean, 
                     sourceDataType, value);
         }
     }
     
     public Object fromBoolean(Type targetType, boolean value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Boolean:
             return Boolean.valueOf(value);
         case String:
             return String.valueOf(value);
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Boolean, value);
         }
     }    
 
     public byte toByte(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Byte:
             return ((Byte)value).byteValue();
         case Double:
             return ((Double) value).byteValue();
         case Float:
             return ((Float) value).byteValue();
         case Int:
             return ((Integer) value).byteValue();
         case Long:
             return ((Long) value).byteValue();
         case Short:
             return ((Short) value).byteValue();
         case String:
             // as per spec: 8 bits unsigned [0-9]+
             return Integer.valueOf((String)value).byteValue();
         default: 
             throw new InvalidDataConversionException(DataType.Byte, 
                     sourceDataType, value);
         }
     }
 
     public Object fromByte(Type targetType, byte value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         // as per spec: 8 bits unsigned [0-9]+
         switch (targetDataType) {
         case Byte:
             return Byte.valueOf(value);
         case Double:
             return Double.valueOf((int)value & 0xFF);
         case Float:
             return Float.valueOf((int)value & 0xFF);
         case Int:
             return Integer.valueOf((int)value & 0xFF);
         case Long:
             return Long.valueOf((int)value & 0xFF);
         case Short:
             return new Short(Integer.valueOf((int)value & 0xFF).shortValue());
         case String:
             return Integer.valueOf((int)value & 0xFF).toString();
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Byte, value);
         }
     }   
     
     public byte[] toBytes(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Bytes:
             return (byte[])value;
         case String:
             return ((String)value).getBytes();
         case Integer:
             return ((BigInteger)value).toByteArray();
         default: 
             throw new InvalidDataConversionException(DataType.Bytes, 
                     sourceDataType, value);
         }
     }
 
     public Object fromBytes(Type targetType, byte[] value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Bytes:
             return value;
         case String:
             return toHexString(value); // SDO Spec 2.1.1 Section 8.1 calls for byte array String representation to be HEX
         case Integer:
             return new BigInteger(value);
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Bytes, value);
         }
     }    
     
     public char toCharacter(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Character:
             return ((Character)value).charValue();
         case String:
             return ((String)value).charAt(0);
         default: 
             throw new InvalidDataConversionException(DataType.Character, 
                     sourceDataType, value);
         }
     }
 
     public Object fromCharacter(Type targetType, char value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Character:
             return Character.valueOf(value);
         case String:
             return String.valueOf(value);
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Character, value);
         }
     }    
     
     public BigDecimal toDecimal(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Decimal:
             return (BigDecimal)value;
         case Double:
         case Float:
         case Int:
         case Long:
         case Integer:
             return new BigDecimal(((Number)value).doubleValue());
         case String:
             return new BigDecimal((String)value);
         default: 
             throw new InvalidDataConversionException(DataType.Decimal, 
                     sourceDataType, value);
         }
     }
 
     public Object fromDecimal(Type targetType, BigDecimal value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Decimal:
             return value;
         case Double:
             return new Double(value.doubleValue());
         case Float:
             return new Float(value.floatValue());
         case Int:
             return new Integer(value.intValue());
         case Long:
             return new Long(value.longValue());
         case Integer:
             return value.toBigInteger();
         case String:
             // as per spec: ('+'|'-')? [0-9]* ('.'[0-9]*)? (('E'|'e') ('+'|'-')? [0-9]+)?
             /*
              *  [123,0]      "123"
              *   [-123,0]     "-123"
              *   [123,-1]     "1.23E+3"
              *   [123,-3]     "1.23E+5"
              *   [123,1]      "12.3"
              *   [123,5]      "0.00123"
              *   [123,10]     "1.23E-8"
              *   [-123,12]    "-1.23E-10"
              */
             return value.toString(); 
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Decimal, value);
         }
     }    
     
     public double toDouble(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Double:
             return ((Double)value).doubleValue();
         case Byte:
             return ((Byte)value).doubleValue();
         case Float:
             return ((Float)value).doubleValue();
         case Int:
             return ((Integer)value).doubleValue();
         case Long:
             return ((Long)value).doubleValue();
         case Short:
             return ((Short)value).doubleValue();
         case Integer:
             return ((BigInteger)value).doubleValue();
         case Decimal:
             return ((BigDecimal)value).doubleValue();
         case String:
             return Double.parseDouble((String)value);
             //return Double.valueOf((String)value).doubleValue();
         default: 
             throw new InvalidDataConversionException(DataType.Double, 
                     sourceDataType, value);
         }
     }
 
     public Object fromDouble(Type targetType, double value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Double:
             return Double.valueOf(value);
         case Byte:
             return new Byte(Double.valueOf(value).byteValue());
         case Float:
             return new Float(Double.valueOf(value).floatValue());
         case Int:
             return new Integer(Double.valueOf(value).intValue());
         case Long:
             return new Long(Double.valueOf(value).longValue());
         case Short:
             return new Short(Double.valueOf(value).shortValue());
         case Integer:
             return BigInteger.valueOf(Double.valueOf(value).longValue()); 
         case Decimal:
             return  BigDecimal.valueOf(value);
         case String:
             // as per spec:  Decimal | 'NaN' | '-NaN' | 'Infinity' | '-Infinity'
             return Double.toString(value);
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Double, value);
         }
     }    
     
     public float toFloat(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Float:
             return ((Float)value).floatValue();
         case Byte:
             return ((Byte) value).floatValue();
         case Double:
             return ((Double) value).floatValue();
         case Int:
             return ((Integer) value).floatValue();
         case Long:
             return ((Long) value).floatValue();
         case Short:
             return ((Short) value).floatValue();
         case Decimal:
             return ((BigDecimal) value).floatValue();
         case Integer:
             return ((BigInteger) value).floatValue();
         case String:
             return Float.valueOf((String) value).floatValue();
         default: 
             throw new InvalidDataConversionException(DataType.Float, 
                     sourceDataType, value);
         }        
     }
 
     public Object fromFloat(Type targetType, float value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Float:
             return Float.valueOf(value);
         case Byte:
             return new Byte(Float.valueOf(value).byteValue());
         case Double:
             return new Double(Float.valueOf(value).doubleValue());
         case Int:
             return new Integer(Float.valueOf(value).intValue());
         case Long:
             return new Long(Float.valueOf(value).longValue());
         case Short:
             return new Short(Float.valueOf(value).shortValue());
         case Decimal:
             return  BigDecimal.valueOf(value);
         case Integer:
             return BigInteger.valueOf(Double.valueOf(value).longValue()); 
         case String:
             // as per spec:  Decimal | 'NaN' | '-NaN' | 'Infinity' | '-Infinity'
             return Float.toString(value);
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Float, value);
         }
     }    
     
     public int toInt(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Int:
             return ((Integer)value).intValue();
         case Byte:
             return ((Byte) value).intValue();
         case Double:
             return ((Double) value).intValue();
         case Float:
             return ((Float) value).intValue();
         case Long:
             return ((Long) value).intValue();
         case Short:
             return ((Short) value).intValue();
         case Decimal:
             return ((BigDecimal) value).intValue();
         case Integer:
             return ((BigInteger) value).intValue();
         case String:
             return Integer.parseInt((String)value);
         default: 
             throw new InvalidDataConversionException(DataType.Int, 
                     sourceDataType, value);
         }
     }
 
     public Object fromInt(Type targetType, int value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Int:
             return Integer.valueOf(value);
         case Byte:
             return new Byte(Integer.valueOf(value).byteValue());
         case Double:
             return new Double(Integer.valueOf(value).doubleValue());
         case Float:
             return new Float(Integer.valueOf(value).floatValue());
         case Long:
             return new Long(Integer.valueOf(value).longValue());
         case Short:
             return new Short(Integer.valueOf(value).shortValue());
         case Decimal:
             return  BigDecimal.valueOf(value);
         case Integer:
             return BigInteger.valueOf(Integer.valueOf(value).longValue()); 
         case String:
             return Integer.toString(value);
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Int, value);
         }
     }    
     
     public BigInteger toInteger(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Integer:
             return (BigInteger)value;
         case Double:
             return BigInteger.valueOf(((Double) value).longValue());
         case Float:
             return BigInteger.valueOf(((Float) value).longValue());
         case Int:
             return BigInteger.valueOf(((Integer) value).longValue());
         case Long:
             return BigInteger.valueOf(((Long) value).longValue());
         case Decimal:
             return BigInteger.valueOf(((BigDecimal) value).longValue());
         case Bytes:
             return new BigInteger((byte[])value);
         case String:
             return new BigInteger((String)value);
         default: 
             throw new InvalidDataConversionException(DataType.Integer, 
                     sourceDataType, value);
         }
     }
     
     public Object fromInteger(Type targetType, BigInteger value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Integer:
             return value;
         case Double:
             return new Double(value.doubleValue());
         case Float:
             return new Float(value.floatValue());
         case Int:
             return new Integer(value.intValue());
         case Long:
             return new Long(value.longValue());
         case Decimal:
             return new BigDecimal(value.doubleValue());
         case Bytes:
             return value.toByteArray();
         case String:
             //as per spec: ('+'|'-')? [0-9]+
             return value.toString();
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Integer, value);
         }
     }    
  
     public long toLong(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Long:
             return ((Long)value).longValue();
         case Byte:
             return ((Byte) value).longValue();
         case Double:
             return ((Double) value).longValue();
         case Float:
             return ((Float) value).longValue();
         case Int:
             return ((Integer) value).longValue();
         case Short:
             return ((Short) value).longValue();
         case Decimal:
             return ((BigDecimal) value).longValue();
         case Integer:
             return ((BigInteger) value).longValue();
         case Date:
             return ((Date)value).getTime();
         case String:
             return Long.parseLong((String)value);
         default: 
             throw new InvalidDataConversionException(DataType.Long, 
                     sourceDataType, value);
         }
     }
     
     public Object fromLong(Type targetType, long value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Long:
             return Long.valueOf(value);
         case Byte:
             return new Byte(Long.valueOf(value).byteValue());
         case Double:
             return new Double(Long.valueOf(value).doubleValue());
         case Float:
             return new Float(Long.valueOf(value).floatValue());
         case Int:
             return new Integer(Long.valueOf(value).intValue());
         case Short:
             return new Short(Long.valueOf(value).shortValue());
         case Decimal:
             return new BigDecimal(Long.valueOf(value).longValue());
         case Integer:
             return BigInteger.valueOf(Long.valueOf(value).longValue());
         case Date:
             return new Date(value);
         case String:
             return Long.toString(value);
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Long, value);
         }
     }    
 
     public short toShort(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Short:
             return ((Short)value).shortValue();
         case Byte:
             return ((Byte) value).shortValue();
         case Double:
             return ((Double) value).shortValue();
         case Float:
             return ((Float) value).shortValue();
         case Int:
             return ((Integer) value).shortValue();
         case Long:
             return ((Long) value).shortValue();
         case String:
             return Short.parseShort((String)value);
         default: 
             throw new InvalidDataConversionException(DataType.Short, 
                     sourceDataType, value);
         }
     }
 
     public Object fromShort(Type targetType, short value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Short:
             return Short.valueOf(value);
         case Byte:
             return new Byte(Short.valueOf(value).byteValue());
         case Double:
             return new Double(Short.valueOf(value).doubleValue());
         case Float:
             return new Float(Short.valueOf(value).floatValue());
         case Int:
             return new Integer(Short.valueOf(value).intValue());
         case Long:
             return new Long(Short.valueOf(value).longValue());
         case String:
             return Short.valueOf(value).toString();
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Short, value);
         }
     }    
     
    /**
     * Converts the given value to a string.
     * @param sourceType the property type for the given property
     * @param value the value to convert
     * @return the string value
     * @throws InvalidDataConversionException if the given source 
     * type cannot be converted to string as per the SDO 2.1 
     * datatype conversion table.
     * @throws IllegalArgumentException if the given value is not
     * the expected Java type as per the SDO 2.1 specification
     */
     @SuppressWarnings("unchecked")
     public String toString(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case String:
         	if (!(value instanceof String))
         		throw new IllegalArgumentException("expected value as class "
         			+ String.class.getName() + ", not " 
         			+ value.getClass().getName() + ", for datatype '"
         			+ sourceDataType.name() + "'");
             return (String)value;
         case Decimal: 
         	if (!(value instanceof BigDecimal))
         		throw new IllegalArgumentException("expected value as class "
         			+ BigDecimal.class.getName() + ", not " 
                 	+ value.getClass().getName() + ", for datatype '"
         			+ sourceDataType.name() + "'");
             return ((BigDecimal)value).toString();
         case Bytes:  
         	if (!(value instanceof byte[]))
         		throw new IllegalArgumentException("expected value as class "
         			+ byte[].class.getName() + ", not " 
                 	+ value.getClass().getName() + ", for datatype '"
         			+ sourceDataType.name() + "'");
             // as per spec: [0-9A-F]+
             return toHexString(((byte[])value));
         case Byte:   
         	if (!(value instanceof Byte))
         		throw new IllegalArgumentException("expected value as class "
         			+ Byte.class.getName() + ", not " 
                     + value.getClass().getName() + ", for datatype '"
         			+ sourceDataType.name() + "'");
             // as per spec: 8 bits unsigned [0-9]+
             return Integer.valueOf(((Byte)value).byteValue() & 0xFF).toString();
         case Boolean:  
         case Character:
         case Double:   
         case Float:    
         case Int:      
         case Integer:  
         case Long:     
         case Short:    
             return String.valueOf(value);
         case Strings:  
         	if (!(value instanceof List))
         		throw new IllegalArgumentException("expected value as class "
         			+ List.class.getName() + ", not " 
                     + value.getClass().getName() + ", for datatype '"
         			+ sourceDataType.name() + "'");
             StringBuffer buf = new StringBuffer();
             List<String> list = (List<String>)value;
             Iterator<String> iter = list.iterator();
             for (int i = 0; iter.hasNext(); i++) {
                 if (i > 0)
                     buf.append(" ");
                 buf.append(iter.next());
             }
             return buf.toString();
         case URI:     
         	if (!(value instanceof String))
         		throw new IllegalArgumentException("expected value as class "
         			+ String.class.getName() + ", not " 
                     + value.getClass().getName() + ", for datatype '"
         			+ sourceDataType.name() + "'");
             return (String)value;
         case Date:     
         	if (!(value instanceof Date))
         		throw new IllegalArgumentException("expected value as class "
         			+ Date.class.getName() + ", not " 
                     + value.getClass().getName() + ", for datatype '"
         			+ sourceDataType.name() + "'");
         	return this.dateFormat.format(value);
         case DateTime: 
         case Month:    
         case MonthDay: 
         case Day:      
         case Time:     
         case Year:     
         case YearMonth:
         case YearMonthDay:
         	if (!(value instanceof String))
         		throw new IllegalArgumentException("expected value as class "
         			+ String.class.getName() + ", not " 
                     + value.getClass().getName() + ", for datatype '"
         			+ sourceDataType.name() + "'");
             return (String)value; // Temporal type except Date are String in Java
         case Duration: 
             return String.valueOf(value);
         case Object:   
             return String.valueOf(value);
         default: 
             throw new InvalidDataConversionException(DataType.String, 
                     sourceDataType, value);
         }
     }
 
     public Object fromString(Type targetType, String value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case String:
             return value;
         case Decimal: 
             return new BigDecimal(value);
         case Bytes:
             return value.getBytes();
         case Byte:   
             return new Byte(value.getBytes()[0]); // TODO: truncation warning?
         case Boolean:  
             return Boolean.valueOf(value);
         case Character:
             return Character.valueOf(value.charAt(0)); // TODO: truncation warning?
         case Double:  
             return Double.valueOf(value);
         case Float:    
             return Float.valueOf(value);
         case Int:      
             return Integer.valueOf(value);
         case Integer:  
             return new BigInteger(value);
         case Long:     
             return Long.valueOf(value);
         case Short:    
             return Short.valueOf(value);
         case Strings:  
             String[] values = value.split("\\s");
             List<String> list = new ArrayList<String>(values.length);
             for (int i = 0; i < values.length; i++)
                 list.add(values[i]); // what no Java 5 sugar for this ??
             return list;
         case Date:                 
             try {
                 return dateFormat.parse(value);
             } catch (ParseException e) {
                 throw new PlasmaDataObjectException(e);
             }
         case DateTime:
         case Month:    
         case MonthDay: 
         case URI:  
             return value;
         case Day:      
         case Duration: 
         case Time:     
         case Year:     
         case YearMonth:
         case YearMonthDay:
             return value; // TODO: See lexical string representation for XML Schema
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.String, value);
         }
     }    
     
     public List<String> toStrings(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Strings:
             return (List<String>)value;
         case String:
             String[] values = ((String)value).split("\\s");
             List<String> list = new ArrayList<String>(values.length);
             for (int i = 0; i < values.length; i++)
                 list.add(values[i]); // what no Java 5 sugar for this ??
             return list;
         default: 
             throw new InvalidDataConversionException(DataType.Strings, 
                     sourceDataType, value);
         }
     }
 
     public Object fromStrings(Type targetType, List<String> value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Strings:
             return value;
         case String:
             StringBuffer buf = new StringBuffer();
             Iterator<String> iter = value.iterator();
             for (int i = 0; iter.hasNext(); i++) {
                 if (i > 0)
                     buf.append(" ");
                 buf.append(iter.next());
             }
             return buf.toString();
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Strings, value);
         }
     }    
     
     public java.util.Date toDate(Type sourceType, Object value)
     {    
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Date:
             return (Date)value;
         case Long:
             return new Date(((Long)value).longValue());
         case DateTime:
         case Day:
         case Duration:
         case Month:
         case MonthDay:
         case Time:
         case Year:
         case YearMonth:
         case YearMonthDay:
         case String:
             try {
                 return dateTimeFormat.parse((String)value);
             } catch (ParseException e) {
                 throw new PlasmaDataObjectException(e);
             }
         default: 
             throw new InvalidDataConversionException(DataType.Date, 
                     sourceDataType, value);
         }
     }
 
     public Object fromDate(Type targetType, Date value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         switch (targetDataType) {
         case Date:
             return value;
         case DateTime:
             return this.dateTimeFormat.format(value);
         case Day:
             return this.dayFormat.format(value);
         case Month:
             return this.monthFormat.format(value);
         case MonthDay:
             return this.monthDayFormat.format(value);
         case Time:
             return this.timeFormat.format(value);
         case Year:
             return this.yearFormat.format(value);
         case YearMonth:
             return this.yearMonthFormat.format(value);
         case YearMonthDay:
             return this.yearMonthDayFormat.format(value);
         case Long:
             return new Long(value.getTime());
         case String: // use format with max precision
             return this.dateTimeFormat.format(value);
         case Duration:    
             return new Duration(value.getTime());
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     DataType.Date, value);
         }
     }    
     
     public String toTemporalDataType(Type targetType, Type sourceType, Object value)
     { 
         DataType targetDataType = DataType.valueOf(targetType.getName());
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (sourceDataType) {
         case Date:
         case String:
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     sourceDataType, value);
         }
     }
 
     public Object fromTemporalDataType(Type targetType, Type sourceType, String value)
     {    
         DataType targetDataType = DataType.valueOf(targetType.getName());
         DataType sourceDataType = DataType.valueOf(sourceType.getName());
         switch (targetDataType) {
         default: 
             throw new InvalidDataConversionException(targetDataType, 
                     sourceDataType, value);
         }
     }    
     
     /**
      * Returns a primitive Java class, wherever possible, for 
      * the given SDO data-type (as 
      * per the SDO Specification 2.10 Section 8.1), and where
      * no primitive exists for the give SDO datatype, the appropriate
      * class is returned.  
      * @param dataType the SDO datatype
      * @return the primitive Java class.
      */
     public Class<?> toPrimitiveJavaClass(DataType dataType) {
         
         switch (dataType) {
         case Boolean:  return boolean.class;   
         case Byte:     return byte.class;  
         case Bytes:    return byte[].class;  
         case Character:  return char.class;
         case Date:       return java.util.Date.class;
         case DateTime:   return String.class;
         case Day:        return String.class;
         case Decimal:    return java.math.BigDecimal.class;
         case Double:     return double.class;
         case Duration:   return String.class;
         case Float:      return float.class;
         case Int:        return int.class;
         case Integer:    return java.math.BigInteger.class;
         case Long:       return long.class;
         case Month:      return String.class;
         case MonthDay:   return String.class;
         case Object:     return java.lang.Object.class;
         case Short:      return short.class;
         case String:     return String.class;
         case Strings:    return java.util.List.class; // of String
         case Time:       return String.class;
         case URI:        return String.class;
         case Year:       return String.class;
         case YearMonth:  return String.class;
         case YearMonthDay:    return String.class;  
         default:
             throw new PlasmaDataObjectException("unknown SDO datatype, " 
                     + dataType.toString());
         }
     }
 
     /**
      * Returns a primitive wrapper Java class, wherever possible, for 
      * the given SDO data-type (as 
      * per the SDO Specification 2.10 Section 8.1), and where
      * no primitive exists for the give SDO datatype, the appropriate
      * class is returned.  
      * @param dataType the SDO datatype
      * @return the primitive Java class.
      */
     public Class<?> toWrapperJavaClass(DataType dataType) {
         
         switch (dataType) {
         case Boolean:  return Boolean.class;   
         case Byte:     return Byte.class;  
         case Bytes:    return byte[].class;  
         case Character:  return Character.class;
         case Date:       return java.util.Date.class;
         case DateTime:   return String.class;
         case Day:        return String.class;
         case Decimal:    return java.math.BigDecimal.class;
         case Double:     return Double.class;
         case Duration:   return String.class;
         case Float:      return Float.class;
         case Int:        return Integer.class;
         case Integer:    return java.math.BigInteger.class;
         case Long:       return Long.class;
         case Month:      return String.class;
         case MonthDay:   return String.class;
         case Object:     return java.lang.Object.class;
         case Short:      return Short.class;
         case String:     return String.class;
         case Strings:    return java.util.List.class; // of String
         case Time:       return String.class;
         case URI:        return String.class;
         case Year:       return String.class;
         case YearMonth:  return String.class;
         case YearMonthDay:    return String.class;  
         default:
             throw new PlasmaDataObjectException("unknown SDO datatype, " 
                     + dataType.toString());
         }
     }
     
     public List<DataType> getAllowableTargetTypes(DataType dataType) {
         List<DataType> result = new ArrayList<DataType>();
         
         switch (dataType) {
         case Boolean:  
             result.add(DataType.Boolean);
             result.add(DataType.String);
             break;
         case Byte:     
             result.add(DataType.Byte);
             result.add(DataType.Double);
             result.add(DataType.Float);
             result.add(DataType.Int);
             result.add(DataType.Long);
             result.add(DataType.Short);
             result.add(DataType.String);
             break;
         case Bytes:    
             result.add(DataType.Bytes);
             result.add(DataType.String);
             result.add(DataType.Integer);
             break;
         case Character:
             result.add(DataType.Character);
             result.add(DataType.String);
             break;
         case Decimal:  
             result.add(DataType.Decimal);
             result.add(DataType.Double);
             result.add(DataType.Float);
             result.add(DataType.Int);
             result.add(DataType.Long);
             result.add(DataType.Integer);
             result.add(DataType.String);
             break;
         case Double:   
             result.add(DataType.Double);
             result.add(DataType.Byte);
             result.add(DataType.Float);
             result.add(DataType.Int);
             result.add(DataType.Long);
             result.add(DataType.Short);
             result.add(DataType.Decimal);
             result.add(DataType.Integer);
             result.add(DataType.String);
             break;
         case Float:    
             result.add(DataType.Float);
             result.add(DataType.Byte);
             result.add(DataType.Double);
             result.add(DataType.Int);
             result.add(DataType.Long);
             result.add(DataType.Short);
             result.add(DataType.Decimal);
             result.add(DataType.Integer);
             result.add(DataType.String);
             break;
         case Int:      
             result.add(DataType.Int);
             result.add(DataType.Byte);
             result.add(DataType.Double);
             result.add(DataType.Float);
             result.add(DataType.Long);
             result.add(DataType.Short);
             result.add(DataType.Decimal);
             result.add(DataType.Integer);
             result.add(DataType.String);
             break;
         case Integer:  
             result.add(DataType.Integer);
             result.add(DataType.Double);
             result.add(DataType.Float);
             result.add(DataType.Int);
             result.add(DataType.Long);
             result.add(DataType.Bytes);
             result.add(DataType.Decimal);
             result.add(DataType.String);
             break;
         case Long:     
             result.add(DataType.Long);
             result.add(DataType.Byte);
             result.add(DataType.Double);
             result.add(DataType.Float);
             result.add(DataType.Int);
             result.add(DataType.Short);
             result.add(DataType.Decimal);
             result.add(DataType.Integer);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case Short:    
             result.add(DataType.Short);
             result.add(DataType.Byte);
             result.add(DataType.Double);
             result.add(DataType.Float);
             result.add(DataType.Int);
             result.add(DataType.Long);
             result.add(DataType.String);
             break;
         case String:   
             result.add(DataType.String);  
             result.add(DataType.Boolean);  
             result.add(DataType.Byte);     
             result.add(DataType.Bytes);    
             result.add(DataType.Character);
             result.add(DataType.Date);     
             result.add(DataType.DateTime); 
             result.add(DataType.Day);      
             result.add(DataType.Decimal);  
             result.add(DataType.Double);   
             result.add(DataType.Duration); 
             result.add(DataType.Float);    
             result.add(DataType.Int);      
             result.add(DataType.Integer);  
             result.add(DataType.Long);     
             result.add(DataType.Month);    
             result.add(DataType.MonthDay); 
             result.add(DataType.Short);    
             result.add(DataType.Strings);  
             result.add(DataType.Time);     
             result.add(DataType.URI);      
             result.add(DataType.Year);     
             result.add(DataType.YearMonth);
             result.add(DataType.YearMonthDay);
             break;
         case Strings:  
             result.add(DataType.Strings);
             result.add(DataType.String);
             break;
         case Date:     
             result.add(DataType.Date);
             result.add(DataType.Long);
             result.add(DataType.String);
             break;
         case Duration: 
             result.add(DataType.Duration);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case DateTime: 
             result.add(DataType.DateTime);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case Day:      
             result.add(DataType.Day);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case Month:    
             result.add(DataType.Month);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case MonthDay: 
             result.add(DataType.MonthDay);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case Year:     
             result.add(DataType.Year);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case YearMonth:
             result.add(DataType.YearMonth);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case YearMonthDay:
             result.add(DataType.YearMonthDay);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case Time:     
             result.add(DataType.Time);
             result.add(DataType.Date);
             result.add(DataType.String);
             break;
         case URI:      
             result.add(DataType.String);
             break;
         case Object:   
         default:
         }
         
         return result;
     }
     
     private String toHexString(byte[] block) {
         StringBuffer buf = new StringBuffer();
         char[] hexChars = { 
             '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
             'A', 'B', 'C', 'D', 'E', 'F' };
         int len = block.length;
         int high = 0;
         int low = 0;
         for (int i = 0; i < len; i++) {
             high = ((block[i] & 0xf0) >> 4);
             low = (block[i] & 0x0f);
             buf.append(hexChars[high]);
             buf.append(hexChars[low]);
         } 
         return buf.toString();
     }    
 }
