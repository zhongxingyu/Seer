 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.arco;
 
 import java.util.*;
 import com.sun.grid.arco.model.ObjectFactory;
 import com.sun.grid.logging.SGELog;
 import javax.xml.bind.JAXBException;
 import java.lang.reflect.*;
 import java.text.SimpleDateFormat;
 
 public class ResultConverter {
    
    
    public static Class getColumnClass( String name ) {
       return getConverter( name ).getType();
    }
    
    public static String getColumnType( Class clazz ) {
       return getConverter(clazz).getName();
    } 
 
    public static String objToStr( Object obj ) {
       if( obj == null ) {
          return "";
       } else {
          return getConverter( obj.getClass() ).toStr( obj );
       }
    }
    
    public static Object strToObj( String str, String name ) {
       return getConverter( name ).toObj( str );
    }
    
    private static AbstractConverter getConverter( String name ) {
       AbstractConverter ret = (AbstractConverter)converterNameMap.get( name );
       if( ret == null ) {
          throw new IllegalArgumentException( "Converter for name " + name + " not registered");
       }
       return ret;
    }
 
    private static AbstractConverter getConverter( Class type ) {
       AbstractConverter ret = (AbstractConverter)converterTypeMap.get( type );
       if( ret == null ) {
          throw new IllegalArgumentException( "Converter for type " + type + " not registered");
       }
       return ret;
    }
    
    private static Map converterNameMap = new HashMap();
    private static Map converterTypeMap = new HashMap();
    
    private static void reg( AbstractConverter conv ) {
       converterNameMap.put( conv.getName(), conv );
       converterTypeMap.put( conv.getType(), conv );
    }
    
    static {
 
       reg( new StringConverter() );
       reg( new WrapperConverter( Integer.class, "int" ) );
       reg( new WrapperConverter( Long.class, "long" ) );
       reg( new WrapperConverter( Boolean.class, "boolean" ) );
       reg( new WrapperConverter( Double.class, "double"));
       reg( new WrapperConverter( Float.class, "float"));
       reg( new BigDecimalConverter() );
      reg( new BigIntegerConverter() );
       reg( new DateConverter( Date.class, "date", "yyyy-MM-dd HH:mm:ss") );
       reg( new DateConverter( java.sql.Timestamp.class, "timestamp", "yyyy-MM-dd HH:mm:ss") );
       reg( new DateConverter( java.sql.Time.class, "time", "HH:mm:ss") );
       reg( new DateConverter( java.sql.Date.class, "sqlDate", "yyyy-MM-dd HH:mm:ss" ) );      
    }
    
    static abstract class AbstractConverter {
       protected Class type;
       protected String name;
       
       public AbstractConverter( Class type, String name ) {
          this.type = type; this.name = name;
       }
       
       public abstract String toStr( Object obj );
       public abstract Object toObj( String str );
       
       public String getName() { return name; }
       public Class  getType() { return type; }
    }
    
    static class StringConverter extends AbstractConverter {
       public StringConverter() {
          super( String.class, "string" );
       }
       public Object toObj(String str) {
          return str;
       }
       
       public String toStr(Object obj) {
          return(String)obj;
       }      
    }
    
    static class DateConverter extends AbstractConverter {
       
       private SimpleDateFormat format;// = new SimpleDateFormat( "yyyy-MM-dd HH-mm-ss" );
       
       private Constructor constructor; 
       
       public DateConverter( Class type, String name, String format ) {
           super( type, name );
           this.format = new SimpleDateFormat( format );
           if( !type.equals( java.util.Date.class ) ) {
              try {
                constructor = type.getConstructor( new Class [] { Long.TYPE } );
              } catch( NoSuchMethodException nsme ) {
                 throw new IllegalArgumentException("constructor in class " + type + " not found" );
              }
           }
       }
       
       public Object toObj(String str) {
          try {
             if(str == null || str.length() == 0) {
                return null;
             } else {
                Date d = format.parse( str );
                if( constructor == null ) {
                   return d;
                } else {
                   return constructor.newInstance( new Object[] { new Long( d.getTime() ) } );
                }
             }
          } catch( java.text.ParseException pe ) {            
             throw new IllegalArgumentException( str + " is not a valid date" );
          } catch( InstantiationException ise ) { 
             IllegalStateException ilse = new IllegalStateException("Can't create instanceof of class " + type );
             ilse.initCause( ise );
             throw ilse;
          } catch( IllegalAccessException iae ) { 
             IllegalStateException ilse = new IllegalStateException("Have o access on constructor of class " + type );
             ilse.initCause( iae );
             throw ilse;
          } catch( InvocationTargetException ite ) {
             IllegalStateException ilse = new IllegalStateException("Error in constructor of class " + type );
             ilse.initCause( ite.getTargetException() );
             throw ilse;
          }
       }
       
       public String toStr(Object obj) {
          if(obj == null ) {
             return null;
          } else {
             return format.format( obj );
          }
       }
       
    }
    
    static class WrapperConverter extends AbstractConverter {
       private Method valueOfMethod;
       
       public WrapperConverter( Class type, String name ) {
          super( type, name );
 
          try {
             valueOfMethod = type.getMethod( "valueOf", new Class[] { String.class } );
          } catch( NoSuchMethodException nsme ) {
             throw new IllegalArgumentException("type " + type + " has no valueOf method" );
          }
 
       }
       public Object toObj(String str) {
          try {
             if(str == null || str.length() == 0) {
                return null;
             } else {
                return valueOfMethod.invoke( type, new Object[] { str } );
             }
          } catch( IllegalAccessException iae ) {
             throw new IllegalArgumentException("has no access on valueOf method of type " + type  );
          } catch( InvocationTargetException ivte ) {
             IllegalStateException ilse = new IllegalStateException("can't convert str '"+ str + " to object: " + ivte.getTargetException().getMessage());
             ilse.initCause( ivte.getTargetException() );
             throw ilse;
          }
       }
       
       public String toStr(Object obj) {
          if(obj == null) {
             return null;
          } else {
             return obj.toString();
          }
       }
    }
    
   static class BigIntegerConverter extends AbstractConverter {
      public BigIntegerConverter( ) {
         super( java.math.BigInteger.class, "biginteger");
      }
      
      public Object toObj(String str) {
         if(str == null || str.length() == 0) {
            return null;
         } else {
            return new java.math.BigInteger( str );
         }
      }
      
      public String toStr(Object obj) {
         if(obj == null) {
            return null;
         } else {
            return obj.toString();
         }
      }     
   }
   
    static class BigDecimalConverter extends AbstractConverter {
       
       public BigDecimalConverter( ) {
          super( java.math.BigDecimal.class, "bigdecimal");
       }
       
       public Object toObj(String str) {
          if(str == null || str.length() == 0) {
             return null;
          } else {
             return new java.math.BigDecimal( str );
          }
       }
       
       public String toStr(Object obj) {
          if(obj == null) {
             return null;
          } else {
             return obj.toString();
          }
       }
       
    }
    
 }
