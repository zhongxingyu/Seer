 package com.meterware.httpunit.cookies;
 /********************************************************************************************************************
  * $Id$
  *
  * Copyright (c) 2002, Russell Gold
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
  * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions
  * of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
  * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  *
  *******************************************************************************************************************/
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 
 /**
  *
  * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
  **/
 public class CookieTest extends TestCase {
 
     public static void main( String args[] ) {
         TestRunner.run( suite() );
     }
 
 
     public static TestSuite suite() {
         return new TestSuite( CookieTest.class );
     }
 
 
     public CookieTest( String name ) {
         super( name );
     }
 
 
     public void testSimpleCookies() throws Exception {
         CookieJar jar = new CookieJar(
                 new TestSource( new URL( "http://www.meterware.com" ),
                                 new String[] { "Reason=; path=/",
                                                "age=12, name= george",
                                                "type=short",
                                                "funky=ab$==",
                                                "p30waco_sso=3.0,en,us,AMERICA,Drew;path=/, PORTAL30_SSO_TEST=X",
                                                "SESSION_ID=17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==; path=/;" } ) );
         assertEquals( "cookie 'Reason' value", "", jar.getCookieValue( "Reason" ) );
         assertEquals( "cookie 'age' value", "12", jar.getCookieValue( "age" ) );
         assertEquals( "cookie 'name' value", "george", jar.getCookieValue( "name" ) );
         assertEquals( "cookie 'type' value", "short", jar.getCookieValue( "type" ) );
         assertEquals( "cookie 'funky' value", "ab$==", jar.getCookieValue( "funky" ) );
         assertEquals( "cookie 'p30waco_sso' value", "3.0,en,us,AMERICA,Drew", jar.getCookieValue( "p30waco_sso" ) );
         assertEquals( "cookie 'PORTAL30_SSO_TEST' value", "X", jar.getCookieValue( "PORTAL30_SSO_TEST" ) );
         assertEquals( "cookie 'SESSION_ID' value", "17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==", jar.getCookieValue( "SESSION_ID" ) );
     }
 
 
     public void testCookieMatching() throws Exception {
         assertTrue( "Universal cookie could not be sent", new Cookie( "name", "value" ).mayBeSentTo( new URL( "http://httpunit.org/anywhere" ) ));
 
         checkMatching( 1, true, new URL( "http://www.meterware.com/servlets/sample" ), "www.meterware.com", "/servlets/sample" );
 
         checkMatching( 2, false, new URL( "http://www.meterware.com/servlets/sample" ), "meterware.com", "/" );
         checkMatching( 3, true, new URL( "http://www.meterware.com/servlets/sample" ), ".meterware.com", "/" );
         checkMatching( 4, false, new URL( "http://www.meterware.com/servlets/sample" ), ".httpunit.org", "/" );
 
         checkMatching( 5, true, new URL( "http://www.meterware.com/servlets/sample" ), "www.meterware.com", "/servlets" );
         checkMatching( 6, false, new URL( "http://www.meterware.com/servlets/sample" ), "www.meterware.com", "/servlets/sample/data" );
     }
 
 
     private void checkMatching( int index, boolean success, URL url, String domain, String path ) {
         HashMap attributes = new HashMap();
         attributes.put( "path", path );
         attributes.put( "domain", domain );
         Cookie cookie = new Cookie( "name", "value", attributes );
         if (success) {
             assertTrue( "Cookie " + index + " did not allow " + url, cookie.mayBeSentTo( url ) );
         } else {
             assertFalse( "Cookie " + index + " allowed " + url, cookie.mayBeSentTo( url ) );
         }
     }
 
 
     public void testCookieAcceptance() throws Exception {
         checkAcceptance( 1, true, "www.meterware.com/servlets/special", null, null );
         checkAcceptance( 2, true, "www.meterware.com/servlets/special", ".meterware.com", "/servlets" );
         checkAcceptance( 3, false, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary" );
         checkAcceptance( 4, false, "www.meterware.com/servlets/special", "meterware.com", null );
         checkAcceptance( 5, false, "www.meterware.com/servlets/special", "meterware", null );
         checkAcceptance( 6, false, "www.meterware.com/servlets/special", ".com", null );
         checkAcceptance( 7, false, "www.meterware.com/servlets/special", ".httpunit.org", null );
        checkAcceptance( 8, false, "www.some.meterware.com/servlets/special", ".meterware.com", null );
     }
 
 
     private void checkAcceptance( int index, boolean shouldAccept, String urlString,
                                   String specifiedDomain, String specifiedPath ) throws MalformedURLException {
         CookieJar jar = newJar( urlString, specifiedDomain, specifiedPath );
 
         if (shouldAccept) {
             assertNotNull( "Rejected cookie " + index, jar.getCookie( "name" ) );
         } else {
             assertNull( "Cookie " + index + " should have been rejected", jar.getCookie( "name" ) );
         }
     }
 
 
     public void testCookieDefaults() throws Exception {
         checkDefaults( 1, "www.meterware.com/servlets/special", ".meterware.com", "/servlets", ".meterware.com", "/servlets" );
         checkDefaults( 2, "www.meterware.com/servlets/special/myServlet", null, null, "www.meterware.com", "/servlets/special" );
     }
 
 
     private void checkDefaults( int index, String urlString, String specifiedDomain, String specifiedPath,
                                 String expectedDomain, String expectedPath ) throws MalformedURLException {
         CookieJar jar = newJar( urlString, specifiedDomain, specifiedPath );
         assertNotNull( "case " + index + " domain is null", jar.getCookie( "name" ).getDomain() );
         assertEquals( "case " + index + " domain", expectedDomain, jar.getCookie( "name" ).getDomain() );
         assertNotNull( "case " + index + " path is null", jar.getCookie( "name" ).getPath() );
         assertEquals( "case " + index + " path", expectedPath, jar.getCookie( "name" ).getPath() );
     }
 
 
     private CookieJar newJar( String urlString, String specifiedDomain, String specifiedPath ) throws MalformedURLException {
         StringBuffer header = new StringBuffer( "name=value" );
         if (specifiedDomain != null) header.append( "; domain=" ).append( specifiedDomain );
         if (specifiedPath != null) header.append( "; path=" ).append( specifiedPath );
 
         return new CookieJar( new TestSource( new URL( "http://" + urlString ), header.toString() ) );
     }
 
 
     public void testHeaderGeneration() throws Exception {
         CookieJar jar = new CookieJar();
         jar.addCookie( "zero", "nil" );
         jar.updateCookies( newJar( "www.meterware.com/servlets/standard/AServlet", "first=ready" ) );
         jar.updateCookies( newJar( "www.meterware.com/servlets/AnotherServlet", "second=set" ) );
         jar.updateCookies( newJar( "www.httpunit.org", "zero=go; domain=.httpunit.org" ) );
         jar.updateCookies( newJar( "meterware.com", "fourth=money" ) );
 
         checkHeader( 1, jar, "first=ready;second=set;zero=nil", "www.meterware.com/servlets/standard/Count" );
         checkHeader( 2, jar, "second=set;zero=nil", "www.meterware.com/servlets/special/Divide" );
         checkHeader( 3, jar, "zero=go", "fancy.httpunit.org/servlets/AskMe" );
     }
 
 
     private void checkHeader( int index, CookieJar jar, String expectedHeader, String targetURLString ) throws MalformedURLException {
         assertEquals( "header " + index, expectedHeader, jar.getCookieHeaderField( new URL( "http://" + targetURLString ) ) );
     }
 
 
     public void testCookieReplacement() throws Exception {
         CookieJar jar = new CookieJar();
         jar.updateCookies( newJar( "www.meterware.com/servlets/standard", "first=ready" ) );
         jar.updateCookies( newJar( "meterware.com/servlets/standard", "second=more" ) );
         jar.updateCookies( newJar( "www.meterware.com/servlets", "third=day" ) );
         jar.updateCookies( newJar( "www.meterware.com/servlets", "third=tomorrow" ) );
 
         checkHeader( 1, jar, "first=ready;third=tomorrow", "www.meterware.com/servlets/standard" );
     }
 
 
     private CookieJar newJar( String urlString, String setCookieHeader ) throws MalformedURLException {
         return new CookieJar( new TestSource( new URL( "http://" + urlString ), setCookieHeader ) );
     }
 
 
     private class TestSource implements CookieSource {
 
         private URL _sourceURL;
         private String[] _headers;
 
 
         public TestSource( URL sourceURL, String header ) {
             this( sourceURL, new String[] { header } );
         }
 
 
         public TestSource( URL sourceURL, String[] headers ) {
             _sourceURL = sourceURL;
             _headers = headers;
         }
 
 
         public URL getURL() {
             return _sourceURL;
         }
 
 
         public String[] getHeaderFields( String fieldName ) {
             return fieldName.equalsIgnoreCase( "set-cookie" ) ? _headers : new String[0];
         }
     }
 
 
        // XXX test cookie deletion (need age attribute?)
 }
