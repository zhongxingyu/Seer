 package com.net.BitFlood;
 
 import com.net.BitFlood.sdk.Base64;
 import java.security.MessageDigest;
 
 /**
  * Created on Nov 28, 2004
  *
  */
 
 /**
  * @author burke
  *  
  */
 public class Encoder
 {
   public static String SHA1Base64Encode( final String input )
   {
     return SHA1Base64Encode( input.getBytes(), input.length() );	
   }
  
   public static String SHA1Base64Encode( final byte[] bytes, final int length )
   {
     MessageDigest sha1Encoder = null;
     
     try
     {
       sha1Encoder = MessageDigest.getInstance( "SHA-1" );
     }
     catch ( Exception e )
     {
      Logger.LogError( "Failed to encode (" + new String( bytes ) + ") : " + e );
     }
  
     sha1Encoder.reset();
     sha1Encoder.update( bytes, 0, length );
 
     // NOTE: base64 encoded sha1s are always 27 chars
     return Base64.encodeToString( sha1Encoder.digest(), false ).substring( 0, 27 );
   }
 }
