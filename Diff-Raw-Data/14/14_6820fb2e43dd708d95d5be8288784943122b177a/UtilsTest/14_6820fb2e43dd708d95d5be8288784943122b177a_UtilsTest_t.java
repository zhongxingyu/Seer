 package com.bradmcevoy.http;
 
 import junit.framework.TestCase;
 
 /**
  *
  * @author brad
  */
 public class UtilsTest extends TestCase {
 
     public UtilsTest( String testName ) {
         super( testName );
     }
 
     public void testPercentEncode() {
 //        for( int i=0; i<180; i++ ) {
 //            String s = String.valueOf((char)i);
 //            System.out.println(i + " = " + s);
 //        }
         assertEquals( "", Utils.percentEncode( "" ) );
         assertEquals( "abc", Utils.percentEncode( "abc" ) );
         assertEquals( "%20", Utils.percentEncode( " " ) );
 
         assertEquals( "a[b]", Utils.decodePath( "a%5Bb%5D" ) );
         assertEquals( "a%5Bb%5D", Utils.percentEncode( "a[b]" ) );
 
         assertEquals( "ampersand%26", Utils.percentEncode( "ampersand&" ) );
         assertEquals( "0", Utils.percentEncode( "0" ) );
         assertEquals( "2009-01_02", Utils.percentEncode( "2009-01_02" ) );
 
         // check decode simple cases
         assertEquals( "abc", Utils.decodePath( "abc" ) );
         assertEquals( "/abc", Utils.decodePath( "/abc" ) );
 
         // this string seems to encode differently on different platforms. this
         // isnt ideal and will hopefully be corrected, but in the mean time
         // its good enough if it 'round trips' Ie encode + decode = original
         String originalUnencoded = "ne";
         System.out.println( "encoding: " + originalUnencoded );
         String encoded = Utils.percentEncode( originalUnencoded );
         System.out.println( "encoded to: " + encoded );
         String decoded = Utils.decodePath( encoded );
         System.out.println( "decoded to: " + decoded );
         assertEquals( originalUnencoded, decoded );
     }
 
     public void testDecodeHref() {
         String href = "/";
         String result = Utils.decodePath( href );
         assertEquals( result, href );
 
         href = "/with%20space";
         result = Utils.decodePath( href );
         assertEquals( "/with space", result );
 
     }

    public void testDecodeHref_WithSquareBrackets() {
        String href = "/a[b]";
        String result = Utils.decodePath( href );
        assertEquals( "/a[b]", result );

    }

 }
