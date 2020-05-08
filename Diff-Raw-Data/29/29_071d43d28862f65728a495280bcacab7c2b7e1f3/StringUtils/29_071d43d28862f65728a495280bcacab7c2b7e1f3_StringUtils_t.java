 package com.redshape.utils;
 
 import sun.net.util.IPAddressUtil;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.math.BigInteger;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 
 
 @SuppressWarnings("restriction")
 public class StringUtils extends SimpleStringUtils {
 
     public static List<String> camelCaseDelimiters = Arrays.asList( "_", "-" );
     private static final String RANDOM_STRING_SOURCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
     private static final String ESCAPE_SYMBOL = "\\";
 
     public final static String preparePathByClass( Class<?> clazz ) {
         return preparePathByClass( clazz.getCanonicalName() );
     }
 
    protected final static String preparePathByPackage( String packageName ) {
        return packageName.replaceAll(Pattern.quote("."), "\\" + File.separator );
    }

     public final static String preparePathByClass( String className ) {
        return preparePathByPackage(className) + ".class";
     }
 
     public final static byte[] stringToIP( String addrString ) {
         if ( addrString.isEmpty() ) {
             throw new IllegalArgumentException("Empty address given");
         }
 
         return IPAddressUtil.textToNumericFormatV4(addrString);
     }
 
     public final static Integer hex( String data ) {
         return Integer.valueOf(
                 String.format("%x", new BigInteger(1, data.getBytes( Charset.forName("UTF-8") ) ) )
         );
     }
 
     public final static String formatDate( String format, Date date ) {
         return new SimpleDateFormat(format).format(date);
     }
 
     public final static String stackTraceAsString( Throwable e ) {
         final Writer writer = new StringWriter();
         final PrintWriter printer = new PrintWriter(writer);
 
         e.printStackTrace(printer);
 
         return writer.toString();
     }
 
 }
