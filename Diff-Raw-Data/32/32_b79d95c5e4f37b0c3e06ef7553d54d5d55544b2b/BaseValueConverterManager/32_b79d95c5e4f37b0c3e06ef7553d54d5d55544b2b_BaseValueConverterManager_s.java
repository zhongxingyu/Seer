 package fi.vincit.jmobster.processor.defaults.base;
 
 /*
  * Copyright 2012 Juha Siponen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import fi.vincit.jmobster.annotation.IgnoreDefaultValue;
 import fi.vincit.jmobster.processor.FieldValueConverter;
 import fi.vincit.jmobster.processor.ValueConverter;
 import fi.vincit.jmobster.processor.languages.javascript.JavaToJSValueConverter;
 import fi.vincit.jmobster.processor.languages.javascript.valueconverters.ConverterMode;
 import fi.vincit.jmobster.processor.languages.javascript.valueconverters.ToStringConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * <p>
  *     The implementation first tries to match the convertable object by class' exact equality
  * (i.e. class hash code). If it can't find the match, then it tries to find match for
  * the super class and if no match is found, then for the interfaces. The search is
  * recursive so the whole hierarchy is used in the search. The search is conducted in
  * top-to-bottom manner where the first match wins.
  * </p>
  * <p>
  *     If no class is found in the search, the object is converted using the to string converter.
  * Currently there is no way to figure out how the classes that should be converted with default
  * toString method and the classes that shouldn't be converted at all other than manually specifying
  * the classes with {@link BaseValueConverterManager#setConverter}
  * method. The library default implementations will rely on the to string converter for all non-matching
  * classes in order to widely support the Java's toString() methods on various classes (e.g. BigDecimal and BigInteger).
  * </p>
  */
 public abstract class BaseValueConverterManager implements FieldValueConverter {
     private static final Logger LOG = LoggerFactory.getLogger( JavaToJSValueConverter.class );
 
     private final String nullValue;
     private final ConverterMode converterMode;
 
     private final Map<Class, ValueConverter> converters;
     private final ToStringConverter toStringConverter;
 
     protected BaseValueConverterManager(ConverterMode converterMode, String nullValue) {
         converters = new HashMap<Class, ValueConverter>();
         this.nullValue = nullValue;
         this.converterMode = converterMode;
         this.toStringConverter = new ToStringConverter();
     }
 
     /**
      * Sets converter for given classes.
      * @param valueConverter Value converter
      * @param classesToConvert Classes for which the converter is used
      */
     public void setConverter( ValueConverter valueConverter, Class... classesToConvert ) {
         for( Class clazz: classesToConvert ) {
             converters.put( clazz, valueConverter );
         }
     }
 
     @Override
     public String convert( Field field, Object defaultValueObject ) {
         try {
             Object defaultValue;
             if( !field.isAnnotationPresent( IgnoreDefaultValue.class ) ) {
                 defaultValue = field.get( defaultValueObject );
             } else {
                 defaultValue = null;
             }
 
             if( converterMode == ConverterMode.ALLOW_NULL && defaultValue == null ) {
                 return nullValue;
             } else {
                 return convertByClass( defaultValue, field.getType() );
             }
         } catch( IllegalAccessException e ) {
             return null;
         }
     }
 
     @Override
     public String convert(Class type, Object fieldValue) {
         if( converterMode == ConverterMode.ALLOW_NULL && fieldValue == null ) {
             return nullValue;
         } else {
             return convertByClass( fieldValue, type );
         }
     }
 
     /**
      * Converts the given value using a suitable converter class
      * @param value Value to convert
      * @param clazz Value's class
      * @return Converted value depending on settings
      */
     private String convertByClass( Object value, Class clazz ) {
         ValueConverter converter = getConverterByClass( clazz );
         if( converter == null ) {
             LOG.debug(value.getClass().getCanonicalName());
         }
         return converter.convertValue(value);
     }
 
     /**
      * Tries to find a suitable converter for the given class type.
      * Uses also super classes and interfaces for the search.
      * @param clazz Class type
      * @return ValueConverter if found, otherwise null.
      */
     private ValueConverter getConverterByClass( Class clazz ) {
         if( clazz == null ) {
             return toStringConverter;
         } else if( clazz.isArray() ) {
             return converters.get(Array.class);
         } else {
             ValueConverter converter = converters.get(clazz);
             if( converter == null ) {
                 converter = getConverterBySuperClass( clazz );
                 if( converter == null ) { converter = getConverterByInterface(clazz); }
                 if( converter == null ) { converter = getConverterByClass(clazz.getSuperclass()); }
             }
             if( converter == null ) {
                 // This shouldn't happen, but in case it happens, inform about it in the log
                 LOG.error("Converter not found for class {}", clazz.getName());
             }
             return converter;
         }
     }
 
     /**
      * Tries to find suitable converter by super class of
      * the given class.
      * @param clazz Class which super class is user for search
      * @return ValueConverter if found, otherwise null.
      */
     private ValueConverter getConverterBySuperClass( Class clazz ) {
         return converters.get(clazz.getSuperclass());
     }
 
     /**
      * Tries to find suitable converter class by the interfaces
      * of the given class
      * @param clazz Class which interface are used for search
      * @return ValueConverter if found, otherwise null.
      */
     private ValueConverter getConverterByInterface(Class clazz) {
         Class[] interfaces = clazz.getInterfaces();
         if( interfaces != null && interfaces.length > 0 ) {
             for( Class interfaceClass : interfaces ) {
                 ValueConverter interfaceConverter = getConverterByClass(interfaceClass);
                 if( interfaceConverter != null ) {
                     return interfaceConverter;
                 }
             }
         }
         return null;
     }
 }
