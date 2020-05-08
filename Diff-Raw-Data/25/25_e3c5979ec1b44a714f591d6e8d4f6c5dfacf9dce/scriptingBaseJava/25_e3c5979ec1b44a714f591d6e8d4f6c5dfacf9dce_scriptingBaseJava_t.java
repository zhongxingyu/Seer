 package com.chiorichan.framework;
 
 import groovy.lang.Script;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import com.chiorichan.util.Common;
 import com.chiorichan.util.StringUtil;
 
 abstract public class scriptingBaseJava extends Script
 {
 	String trim( String str )
 	{
 		return str.trim();
 	}
 	
 	String strtoupper( String str )
 	{
 		return str.toUpperCase();
 	}
 	
 	String strtolower( String str )
 	{
 		return str.toLowerCase();
 	}
 	
 	int count( Map<Object, Object> maps )
 	{
 		return maps.size();
 	}
 	
 	int count( List<Object> list )
 	{
 		return list.size();
 	}
 	
 	int count( String var )
 	{
 		return var.length();
 	}
 	
 	int strlen( String var )
 	{
 		return var.length();
 	}
 	
 	boolean empty( List<Object> list )
 	{
 		return ( list == null || list.size() < 1 );
 	}
 	
 	boolean empty( Map<Object, Object> maps )
 	{
 		return ( maps == null || maps.size() < 1 );
 	}
 	
 	boolean empty( String var )
 	{
 		if ( var == null )
 			return true;
 		
 		if ( var.isEmpty() )
 			return true;
 		
 		return false;
 	}
 	
 	int time()
 	{
 		return Common.getEpoch();
 	}
 	
 	String date( String format )
 	{
 		return date( format, null );
 	}
 	
 	String date( String format, String data )
 	{
 		Date date = new Date();
 		
 		if ( data != null && !data.isEmpty() )
 		{
 			data = data.trim();
 			
 			if ( data.length() > 10 )
 				data = data.substring( 0, 10 );
 			
 			date = new Date( Long.parseLong( data ) * 1000 );
 		}
 		
 		if ( format.equals( "U" ) )
 			return Common.getEpoch() + "";
 		
 		if ( format == null || format.isEmpty() )
			format = "MMM dx YYYY";
 		
 		if ( format.contains( "x" ) )
 		{
 			Calendar var1 = Calendar.getInstance();
 			var1.setTime( date );
 			int day = var1.get( Calendar.DAY_OF_MONTH );
 			String suffix = "";
 			
 			if ( day >= 11 && day <= 13 )
 			{
 				suffix = "'th'";
 			}
 			else
 				switch ( day % 10 )
 				{
 					case 1:
 						suffix = "'st'";
 						break;
 					case 2:
 						suffix = "'nd'";
 						break;
 					case 3:
 						suffix = "'rd'";
 						break;
 					default:
 						suffix = "'th'";
 						break;
 				}
 			
 			format = format.replaceAll( "x", suffix );
 		}
 		
 		return new SimpleDateFormat( format ).format( date );
 	}
 	
 	String md5( String str )
 	{
 		return StringUtil.md5( str );
 	}
 	
 	Integer strpos( String haystack, String needle )
 	{
 		return strpos( haystack, needle, 0 );
 	}
 	
 	Integer strpos( String haystack, String needle, int offset )
 	{
 		if ( offset > 0 )
 			haystack = haystack.substring( offset );
 		
 		if ( !haystack.contains( needle ) )
 			return null;
 		
 		return haystack.indexOf( needle );
 	}
 	
 	String str_replace( String needle, String replacement, String haystack )
 	{
 		return haystack.replaceAll( needle, replacement );
 	}
 	
 	String money_format( String amt )
 	{
 		if ( amt == "" )
 			return "$0.00";
 		
 		return money_format( Integer.parseInt( amt ) );
 	}
 	
 	String money_format( Integer amt )
 	{
 		if ( amt == 0 )
 			return "$0.00";
 		
 		DecimalFormat df = new DecimalFormat( "$###,###,###.00" );
 		return df.format( amt );
 	}
 	
 	String money_format( Double amt )
 	{
 		if ( amt == 0 )
 			return "$0.00";
 		
 		DecimalFormat df = new DecimalFormat( "$###,###,###.00" );
 		return df.format( amt );
 	}
 	
 	@Deprecated
 	public boolean is_numeric( String str )
 	{
 		return isNumeric( str );
 	}
 	
 	public boolean isNumeric(String str)
 	{
 	  NumberFormat formatter = NumberFormat.getInstance();
 	  ParsePosition pos = new ParsePosition(0);
 	  formatter.parse(str, pos);
 	  return str.length() == pos.getIndex();
 	}
 	
 	boolean is_null( Object obj )
 	{
 		return ( obj == null );
 	}
 	
 	boolean file_exists( File file )
 	{
 		return file.exists();
 	}
 	
 	boolean file_exists( String file )
 	{
 		return new File( file ).exists();
 	}
 	
 	String dirname( File path )
 	{
 		return path.getParent();
 	}
 	
 	String dirname( String path )
 	{
 		return new File( path ).getParent();
 	}
 	
 	String apache_get_version()
 	{
 		return "THIS IS NOT APACHE YOU DUMMY!!!";
 	}
 }
