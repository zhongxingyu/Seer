 package com.globalmouth;
 /**
  * Copyright (c) Global Mouth AB 2011
  */
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * 
  * A simple class that shows how an implementation towards the 
  * GLOBALMOUTH MCM HTTPApi could look.
  * 
  * @author Claes Johansson <claes.johansson@globalmouth.com>
  *
  */
 public class McmHttpApi {
    
    final char hexMap[] = ("0123456789abcdef".toCharArray());
    final String endpoint = "http://mcm.globalmouth.com:8080/api/mcm";
    
    public static void main( String[] args ) {
       McmHttpApi api = new McmHttpApi();
      api.sendMessage( "myusername", "mypassword", "mymessage", "mymsisdn", "mybody" );
    }
    
    public void sendMessage( String username, String password, String originator, String msisdn, String body ) {
       sendMessage( username, password, originator, msisdn, body, false, null );
    }
    
    public void sendMessage( String username, String password, String originator, String msisdn, String body, boolean deliveryReport, String reference ) {
       String parameters = "?";
       parameters += "username=" + urlEncode( username ) + "&";
       parameters += "body=" + urlEncode( body ) + "&";
       parameters += "msisdn=" + urlEncode( msisdn ) + "&";
       parameters += "dlr=" + String.valueOf( deliveryReport ) + "&";
       if( deliveryReport ) {
          parameters += "ref=" + urlEncode( reference ) + "&";
       }
       if( originator != null && !"".equals( originator ) ) {
          parameters += "originator=" + urlEncode( originator ) + "&";
       }
       parameters += "hash=" + computeHash( username, password, new String[] { body, originator, msisdn } );
       
       try {
          URL url = new URL( endpoint + parameters );
          HttpURLConnection conn = (HttpURLConnection)url.openConnection();
          conn.setRequestMethod( "GET" );
          conn.connect();
          conn.getInputStream();
          conn.disconnect();
       } catch( MalformedURLException mue ) {
          mue.printStackTrace();
       } catch( ProtocolException pe ) {
          pe.printStackTrace();
       } catch( IOException ioe ) {
          ioe.printStackTrace();
       }
    }
    
    private String computeHash( String username, String password, String[] values) {
       String hash = username;
       for( int i = 0; i < values.length; i++ ) {
          if( values[i] != null ) hash += values[i];
       }
       String pwHash = convertToMD5Hex( username + ":" + password );
       return convertToMD5Hex( hash + pwHash );
    }
    
    /**
     * Make sure that the parameter is URLencoded using Latin1
     * @param str
     * @return
     */
    private String urlEncode( String str ) {
       if( str != null ) {
          try {
             return URLEncoder.encode( str, "ISO-8859-1" );
          } catch( UnsupportedEncodingException e ) {
             e.printStackTrace();
          }
       }
       return str;
    }
    
    /**
     * Get MD5 checksum from String
     * 
     * @param string
     * @return
     */
    private String convertToMD5Hex( String string ) {
       MessageDigest digester = null;
       
       try {
          digester = MessageDigest.getInstance( "MD5" );
       } catch( NoSuchAlgorithmException nsae ) {
          nsae.printStackTrace();
       }
       
       byte hashInputBytes[] = null;
       byte hashedBytes[] = null;
       StringBuffer hashOutput = new StringBuffer();
       
       try {
          hashInputBytes = string.getBytes( "ISO-8859-1" );
       } catch( UnsupportedEncodingException e ) {
          e.printStackTrace();
       }
       
       hashedBytes = digester.digest( hashInputBytes );
       hashOutput.setLength( 0 );
       
       for( int i = 0 ; i < hashedBytes.length ; i++ ) {
          hashOutput.append( hexMap[(hashedBytes[i]&0xF0) >> 4] );
          hashOutput.append( hexMap[hashedBytes[i]&0x0F] );
       }
       return hashOutput.toString();
    }
 }
