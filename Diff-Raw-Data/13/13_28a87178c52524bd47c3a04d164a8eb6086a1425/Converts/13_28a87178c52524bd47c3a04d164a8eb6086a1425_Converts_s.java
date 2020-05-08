 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package bingo.lang;
 
 import java.io.InputStream;
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Blob;
 import java.sql.Clob;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 
 import bingo.lang.convert.ArrayConverter;
 import bingo.lang.convert.BeanConverter;
 import bingo.lang.convert.BlobConverter;
 import bingo.lang.convert.BooleanConverter;
 import bingo.lang.convert.CharacterConverter;
 import bingo.lang.convert.ClassConverter;
 import bingo.lang.convert.ClobConverter;
 import bingo.lang.convert.CollectionConverters;
 import bingo.lang.convert.Converter;
 import bingo.lang.convert.Convertible;
 import bingo.lang.convert.DateTimeConverters;
 import bingo.lang.convert.EnumConverter;
 import bingo.lang.convert.InputStreamConverter;
 import bingo.lang.convert.MethodConverter;
 import bingo.lang.convert.NumberConverters;
 import bingo.lang.convert.StringConverter;
 import bingo.lang.convert.CollectionConverters.ListConverter;
 import bingo.lang.convert.StringConvertible;
 import bingo.lang.exceptions.ConvertException;
 import bingo.lang.exceptions.ConvertUnsupportedException;
 
 @SuppressWarnings({"unchecked","rawtypes"})
 public class Converts {
 	
 	private static final Map<Class<?>, Converter<?>> converters = new ConcurrentHashMap<Class<?>, Converter<?>>();
 	private static final Map<Class<?>, Converter> assignableFromConverters = new ConcurrentHashMap<Class<?>, Converter>();
 	
 	private static Converter beanConverter  = new BeanConverter();
 	private static Converter arrayConverter = new ArrayConverter();
 	private static Converter enumConverter  = new EnumConverter();
 	private static ListConverter listConverter = new ListConverter();
 	
 	static {
 		//String , Charracter , Boolean
 		register(String.class,				new StringConverter());
 		register(Character.class,			new CharacterConverter());
 		register(Boolean.class,				new BooleanConverter());
 
 		//Number : Byte, Short, Integer, Long, Float, Double, BigInteger, BigDecimal
 		register(Byte.class,				new NumberConverters.ByteConverter());
 		register(Short.class,				new NumberConverters.ShortConverter());
 		register(Integer.class,				new NumberConverters.IntegerConverter());
 		register(Long.class,				new NumberConverters.LongConverter());
 		register(Float.class,				new NumberConverters.FloatConverter());
 		register(Double.class,				new NumberConverters.DoubleConverter());
 		register(BigInteger.class,			new NumberConverters.BigIntegerConverter());
 		register(BigDecimal.class,			new NumberConverters.BigDecimalConverter());
 		
 		//Date: java.util.Date, java.sql.Date, java.sql.Time, java.sql.Timestamp
 		register(java.util.Date.class, 		new DateTimeConverters.DateTimeConverter());
 		register(java.sql.Date.class,  		new DateTimeConverters.SqlDateConverter());
 		register(java.sql.Timestamp.class,	new DateTimeConverters.SqlTimestampConverter());
 		register(java.sql.Time.class,		new DateTimeConverters.SqlTimeConverter());
 		
 		//Collection :  
 		register(Iterable.class,new CollectionConverters.ListConverter());
 		register(Collection.class,new CollectionConverters.ListConverter());
 		register(List.class,new CollectionConverters.ListConverter());
 		register(Set.class,new CollectionConverters.SetConverter());
 		
 		//Class
 		register(Class.class,				new ClassConverter());
 		register(Method.class,				new MethodConverter());
 		
 		registerAssignableFrom(Clob.class, new ClobConverter());
 		registerAssignableFrom(Blob.class, new BlobConverter());
 		registerAssignableFrom(InputStream.class, new InputStreamConverter());
 	}
 	
 	protected Converts(){
 
 	}
 	
 	public static void register(Class<?> clazz,Converter<?> converter){
 		converters.put(clazz,converter);
 	}
 	
 	public static void registerAssignableFrom(Class<?> superType,Converter<?> converter){
 		assignableFromConverters.put(superType, converter);
 	}
 	
 	public static <T> T convert(Object value,Class<T> targetType) throws ConvertUnsupportedException {
 		return convert(value,targetType,null);
 	}
 	
 	public static <T> T convert(Object value,Class<T> targetType, Type genericType) throws ConvertUnsupportedException {
 		Assert.notNull(targetType);
 		
 		value = trimToNull(value);
         
         //primitive target type
         if(targetType.isPrimitive()){
         	return toPrimitive(value,targetType);
         }
         
         //null value TODO: default value of null 
         if(null == value){
         	return null;
         }
         
         Class<?> sourceType = value.getClass();
         
         //target and source type is same
         if(targetType.equals(sourceType)){
         	return (T)value;
         }
         
         //string target type
         if(targetType.equals(String.class)){
         	return (T)toString(value);
         }
         
         //enum value
         if(sourceType.isEnum()){
         	value      = Enums.getValue((Enum<?>)value);
         	sourceType = value.getClass();
         }
         
         if(targetType.isEnum()){
         	return (T)Enums.valueOf((Class<? extends Enum>)targetType,value);
         }
         
         try {
 	        //value is Convertible
 	        Out<Object> out = new OutObject<Object>();
 	        
 	        if(value instanceof Convertible) {
 	        	if(((Convertible)value).convertTo(targetType, genericType, out)){
 	        		return (T)out.getValue();
 	        	}
 	        }
 	        
 	        //get converter fro target type
 	        Converter converter = findConverter(targetType);
 	        
 	        //convert from
 	        if(null != converter && converter.convertFrom(value, targetType, genericType, out)){
 	        	return (T)out.getValue();
 	        }
 
 	        //get converter for source type
 	        converter = findConverter(sourceType);
 	        
 	        //convert to
 	        if(null != converter && converter.convertTo(value, targetType, genericType, out)){
 	        	return (T)out.getValue();
 	        }
 	        
 	        //assignablefrom convert
 	        if(targetType.isAssignableFrom(sourceType)){
 	        	return (T)value;
 	        }
 	        
 	        for(Entry<Class<?>, Converter> entry : assignableFromConverters.entrySet()){
 	        	Class<?> superType = entry.getKey();
 	        
 	        	if(superType.isAssignableFrom(targetType) && entry.getValue().convertFrom(value, targetType, genericType, out)){
 	        		return (T)out.getValue();
 	        	}
 	        	
 	        	if(superType.isAssignableFrom(sourceType) && entry.getValue().convertTo(value, targetType, genericType, out)){
 	        		return (T)out.getValue();
 	        	}
 	        }
 	        
 	        //object type convert
 	        if(targetType.equals(Object.class)){
 	            return (T)value;
 	        }
 	        
 	        if(beanConverter.convertFrom(value, targetType, genericType, out) ){
 	        	return (T)out.getValue();
 	        }
 	        
 	        if(beanConverter.convertTo(value, targetType, genericType, out)){
 	        	return (T)out.getValue();
 	        }
         } catch (ConvertException e){
         	throw e;
         } catch (Throwable e) {
         	throw new ConvertException("Error converting '{0}' to '{1}', value : {2}",sourceType.getName(),targetType.getName(),value,e);
         }
         
 		throw new ConvertUnsupportedException("Cannot convert '{0}' to '{1}', value : {2}",sourceType.getName(),targetType.getName(),value.toString());
 	}
 	
 	public static int toInt(Object value){
 		return null == value ? 0 : convert(value,Integer.class);
 	}
 	
 	public static long toLong(Object value){
 		return null == value ? 0L : convert(value,Long.class);
 	}
 	
 	public static boolean toBoolean(Object value){
 		return null == value || (value instanceof String && value.equals(Strings.EMPTY)) ? false : convert(value,Boolean.class);
 	}
 	
 	public static boolean toBoolean(String stringValue,boolean defaultValue){
 		return Strings.isEmpty(stringValue) ? defaultValue : convert(stringValue,Boolean.class);
 	}
 	
 	public static String toString(Object value) {
 		if(null == value){
 			return null;
 		}
 		
     	if(value instanceof StringConvertible){
     		return ((StringConvertible)value).convertToString();
     	}
     	
     	if(value instanceof byte[]){
     		return Strings.newStringUtf8((byte[])value);
     	}
 		
		value = trimToNull(value);
		
 		Class<?> sourceType = value.getClass();
 		
         try {
 	        //get converter for value type
 	        Converter converter = findConverter(sourceType);
 	        
 	        //convert to string
 	        if(null != converter){
 	        	return converter.convertToString(value);
 	        }
 	        
 	        if(value.getClass().isArray()){
 	            StringBuilder string = new StringBuilder();
 	            for(int i=0;i<Array.getLength(value);i++){
 	                if(i > 0){
 	                    string.append(',');
 	                }
 	                string.append(toString(Array.get(value, i)));
 	            }
 	            return string.toString();
 	        }
 
 	        if(value instanceof Iterable){
 	        	return Strings.join((Iterable)value,",");
 	        }
 	        
 	        //TODO : hard code Clob convert
 	        if(value instanceof Clob){
 	        	Clob clob = (Clob)value;
 	        	return clob.getSubString(1,(int)clob.length());
 	        }
 	        
 	        return value.toString();
         } catch (ConvertException e){
         	throw e;
         } catch (Throwable e) {
         	throw new ConvertException("error converting '{0}' to String",sourceType.getName(),e);
         }
 	}
 	
 	public static <E> List<E> toList(Class<E> elementType,Object value){
 		if(null == value){
 			return null;
 		}
 		
		value = trimToNull(value);
		
 		try {
 			if(value instanceof String){
 				return listConverter.toCollection(List.class, elementType, (String)value);
 			}else if(value instanceof Iterable){
 	        	return listConverter.toCollection(List.class, elementType, (Iterable)value);
 	        }else if(value.getClass().isArray()){
 	        	return listConverter.toCollectionFromArray(List.class, elementType, value);
 	        }
 		} catch (ConvertException e){
 			throw e;
         } catch (Throwable e) {
         	throw new ConvertException("Error converting '{0}' to List, value : {1}",value.getClass().getName(),value,e);
         }
 		
         throw new ConvertUnsupportedException("Cannot convert '{0}' to List, value : {1}",value.getClass().getName(),value.toString());
 	}
 	
 	static <T> T toPrimitive(Object value,Class<T> targetType) {
 		value = trimToNull(value);
 		if(null == value){
 			return (T)Primitives.defaultValue(targetType);
 		}else{
 			return convert(value,Primitives.wrap(targetType));
 		}
 	}
 	
 	static Converter findConverter(Class<?> type) {
 		Converter converter = converters.get(type);
 		
 		if(null == converter){
 			
 			if(type.isArray()){
 				return arrayConverter;
 			}
 			
 			if(type.isEnum()){
 				return enumConverter;
 			}
 			
 			for(Entry<Class<?>, Converter> entry : assignableFromConverters.entrySet()){
 				if(entry.getKey().isAssignableFrom(type)){
 					return entry.getValue();
 				}
 			}
 		}
 		
 		return converter;
 	}
 	
 	static Object trimToNull(Object value){
 		if(Null.is(value)){
 			return null;
 		}else{
 			return Strings.trimToNull(value);
 		}
 	}
 }
