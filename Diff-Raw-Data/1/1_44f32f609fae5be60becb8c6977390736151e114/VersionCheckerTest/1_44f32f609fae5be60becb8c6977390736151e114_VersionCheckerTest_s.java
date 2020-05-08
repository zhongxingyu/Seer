 /*
  * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
  * 
  * This software was developed by Pentaho Corporation and is provided under the terms
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use
  * this file except in compliance with the license. If you need a copy of the license,
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
  * the license for the specific language governing your rights and limitations.
  */
 
 package org.pentaho.versionchecker;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpState;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 @SuppressWarnings( { "unchecked", "rawtypes", "deprecation" } )
 public class VersionCheckerTest extends TestCase {
 
   /**
    * Tests the setDataProvider method and makes sure it works with null and without null
    */
   public void testSetDataProvider() {
     MockDataProvider dataProvider = new MockDataProvider();
     MockHttpClient httpClient = new MockHttpClient();
     VersionChecker vc = new VersionChecker( httpClient, null );
 
     vc.setDataProvider( dataProvider );
     vc.performCheck( false );
     assertEquals( 1, dataProvider.getApplicationIDCallCount );
 
     vc.setDataProvider( null );
     vc.performCheck( false );
     assertEquals( 1, dataProvider.getApplicationIDCallCount );
 
     vc.setDataProvider( dataProvider );
     vc.performCheck( false );
     assertEquals( 2, dataProvider.getApplicationIDCallCount );
   }
 
   public void testUnwritableUserHomeDirectory() {
     String backupUserHomeFolder = System.getProperty( "user.home" ); //$NON-NLS-1$
     try {
       // Clear user.home property
       System.setProperty( "user.home", "" ); //$NON-NLS-1$ //$NON-NLS-2$
       // Next, re-initialize the VersionChecker which will clear all info..
       VersionChecker.init();
       assertEquals( VersionChecker.getIsWritable(), true );
       String versionCheckerPropertiesDirectory = null;
       try {
         versionCheckerPropertiesDirectory = VersionChecker.getPropertiesDirectory();
       } catch ( IOException ex ) {
         versionCheckerPropertiesDirectory = null;
       }
 
       // This checks that the fallbacks work.
       assertNotNull( versionCheckerPropertiesDirectory );
 
     } finally {
       System.setProperty( "user.home", backupUserHomeFolder ); //$NON-NLS-1$
     }
   }
 
   /**
    * Tests the addResultHandler and removeRestulHandler methods
    */
   public void testResultHandler() {
     MockHttpClient httpClient = new MockHttpClient();
     MockGetMethod getMethod = new MockGetMethod();
     MockResultHandler resultsHandler1 = new MockResultHandler();
     MockResultHandler resultsHandler2 = new MockResultHandler();
     VersionChecker vc = new VersionChecker( httpClient, getMethod );
 
     vc.addResultHandler( resultsHandler1 );
     vc.performCheck( false );
     assertEquals( 1, resultsHandler1.processResultsCount );
     assertEquals( getMethod.responseBody, resultsHandler1.results );
 
     vc.addResultHandler( resultsHandler2 );
     vc.removeResultHandler( null );
     resultsHandler1.results = null;
     vc.performCheck( false );
     assertEquals( 2, resultsHandler1.processResultsCount );
     assertEquals( 1, resultsHandler2.processResultsCount );
     assertEquals( getMethod.responseBody, resultsHandler1.results );
     assertEquals( getMethod.responseBody, resultsHandler2.results );
 
     vc.removeResultHandler( resultsHandler1 );
     resultsHandler1.results = null;
     resultsHandler2.results = null;
     vc.performCheck( false );
     assertEquals( 2, resultsHandler1.processResultsCount );
     assertEquals( 2, resultsHandler2.processResultsCount );
     assertEquals( null, resultsHandler1.results );
     assertEquals( getMethod.responseBody, resultsHandler2.results );
 
     resultsHandler2.throwException = true;
     vc.performCheck( false );
     assertEquals( 3, resultsHandler2.processResultsCount );
   }
 
   /**
    * Tests the error handler capabilities
    */
   public void testErrorHandler() {
     MockDataProvider dataProvider = new MockDataProvider();
     dataProvider.baseURL = "htp://test.pentaho.org/testing_page_doesnot_exist"; //$NON-NLS-1$
     VersionChecker vc = new VersionChecker();
     vc.setDataProvider( dataProvider );
     MockErrorHandler errorHandler1 = new MockErrorHandler();
     MockErrorHandler errorHandler2 = new MockErrorHandler();
     vc.addErrorHandler( errorHandler1 );
     vc.performCheck( false );
     assertEquals( 1, errorHandler1.errorCount );
 
     vc.addErrorHandler( errorHandler2 );
     vc.addErrorHandler( null );
     vc.performCheck( false );
     assertEquals( 2, errorHandler1.errorCount );
     assertEquals( 1, errorHandler2.errorCount );
 
     vc.removeErrorHandler( errorHandler1 );
     vc.removeErrorHandler( null );
     vc.performCheck( false );
     assertEquals( 2, errorHandler1.errorCount );
     assertEquals( 2, errorHandler2.errorCount );
 
     errorHandler2.throwException = true;
     vc.performCheck( false );
     assertEquals( 3, errorHandler2.errorCount );
   }
 
   /**
    * Tests the method that sets the URL in the HttpMethod
    */
   public void testSetURL() {
     try {
       MockGetMethod httpMethod = new MockGetMethod();
       VersionChecker vc = new VersionChecker( null, httpMethod );
       vc.setURL( httpMethod, null );
       assertEquals( 1, httpMethod.setURICount );
       assertEquals( vc.getDefaultURL(), httpMethod._uri.toString() );
 
       MockDataProvider dataProvider = new MockDataProvider();
       vc.setDataProvider( dataProvider );
       vc.setURL( httpMethod, null );
       assertEquals( 2, httpMethod.setURICount );
       assertTrue( httpMethod._uri.toString().startsWith( "http://test.pentho.org:8080/sample?" ) ); //$NON-NLS-1$
     } catch ( URIException e ) {
       fail( e.getMessage() );
     }
   }
 
   public void testCreateURL() {
     Map params = new HashMap();
     String baseUrl = "http://www.pentaho.org/"; //$NON-NLS-1$
     String result = VersionChecker.createURL( baseUrl, params );
     assertEquals( baseUrl, result );
 
     String junk = "a1B2 !@#$%^&*()_-+={[}]|\\:;\"'<,>.?/~`"; //$NON-NLS-1$
     String encodedJunk = URLEncoder.encode( junk );
     params.put( "junk", junk ); //$NON-NLS-1$
     result = VersionChecker.createURL( baseUrl, params );
     assertEquals( baseUrl + "?junk=" + encodedJunk, result ); //$NON-NLS-1$
 
     params.put( "one", "one" ); //$NON-NLS-1$ //$NON-NLS-2$
     result = VersionChecker.createURL( baseUrl, params );
     assertTrue( result.indexOf( "?one=one&" ) > 0 || result.indexOf( "&one=one" ) > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
 
     params.clear();
     params.put( "two", "two" ); //$NON-NLS-1$ //$NON-NLS-2$
     baseUrl += "?one=one"; //$NON-NLS-1$
     result = VersionChecker.createURL( baseUrl, params );
     assertEquals( baseUrl + "&two=two", result ); //$NON-NLS-1$
 
     params.clear();
     params.put( "two", "two" ); //$NON-NLS-1$ //$NON-NLS-2$
     baseUrl += "&"; //$NON-NLS-1$
     result = VersionChecker.createURL( baseUrl, params );
     assertEquals( baseUrl + "two=two", result ); //$NON-NLS-1$
   }
 
   public void testCheckForUpdates() {
     String xmlTest = "<vercheck protocol=\"1.0\"/>"; //$NON-NLS-1$
     IVersionCheckDataProvider dataProvider = new MockDataProvider();
     Properties props = new Properties();
     String output = VersionChecker.checkForUpdates( dataProvider, xmlTest, props, false );
     assertEquals( xmlTest, output );
     assertEquals( props.size(), 0 );
 
     xmlTest = "<vercheck protocol=\"1.0\">\n" + //$NON-NLS-1$
         "<product id=\"\"><update title=\"\" version=\"\" type=\"\"/></product>\n" + //$NON-NLS-1$
         "</vercheck>"; //$NON-NLS-1$
 
     output = VersionChecker.checkForUpdates( dataProvider, xmlTest, props, false );
     assertEquals( xmlTest, output );
     assertEquals( props.getProperty( "versionchk.prd.1.6.0-RC1.123.update" ), "  " ); //$NON-NLS-1$ //$NON-NLS-2$
 
     output = VersionChecker.checkForUpdates( dataProvider, xmlTest, props, true );
     assertEquals( output, "<vercheck protocol=\"1.0\"/>" ); //$NON-NLS-1$
     assertEquals( props.getProperty( "versionchk.prd.1.6.0-RC1.123.update" ), "  " ); //$NON-NLS-1$ //$NON-NLS-2$
 
     xmlTest = "<vercheck protocol=\"1.0\">\n" + //$NON-NLS-1$
         "<product id=\"POBS\">\n" + //$NON-NLS-1$
         "<update title=\"Pentaho BI Suite\" version=\"1.1\" type=\"GA\"/>\n" + //$NON-NLS-1$
         "<update title=\"Pentaho BI Suite\" version=\"1.2\" type=\"GA\"/>\n" + //$NON-NLS-1$
         "</product>\n" + //$NON-NLS-1$
         "</vercheck>"; //$NON-NLS-1$
 
     props = new Properties();
 
     output = VersionChecker.checkForUpdates( dataProvider, xmlTest, props, true );
     assertEquals( xmlTest, output );
     assertEquals(
         props.getProperty( "versionchk.prd.1.6.0-RC1.123.update" ), "Pentaho BI Suite 1.1 GA,Pentaho BI Suite 1.2 GA" ); //$NON-NLS-1$ //$NON-NLS-2$
 
     output = VersionChecker.checkForUpdates( dataProvider, xmlTest, props, false );
     assertEquals( xmlTest, output );
     assertEquals(
         props.getProperty( "versionchk.prd.1.6.0-RC1.123.update" ), "Pentaho BI Suite 1.1 GA,Pentaho BI Suite 1.2 GA" ); //$NON-NLS-1$ //$NON-NLS-2$
 
     output = VersionChecker.checkForUpdates( dataProvider, xmlTest, props, true );
     assertEquals( output, "<vercheck protocol=\"1.0\"/>" ); //$NON-NLS-1$
     assertEquals(
         props.getProperty( "versionchk.prd.1.6.0-RC1.123.update" ), "Pentaho BI Suite 1.1 GA,Pentaho BI Suite 1.2 GA" ); //$NON-NLS-1$ //$NON-NLS-2$
 
   }
 
   /**
    * Mock GetMethod that allows for the tracking of the URL and the returning of sample results
    */
   private class MockGetMethod extends GetMethod {
 
     public String responseBody = "sample response"; //$NON-NLS-1$
 
     public String getResponseBodyAsString() {
       return responseBody;
     }
 
     private URI _uri = null;
 
     public int setURICount = 0;
 
     public void setURI( URI uri ) throws URIException {
       _uri = uri;
       ++setURICount;
       super.setURI( uri );
     }
 
   };
 
   /**
    * Mock HttpClient class that prevents the executeMethod method from executing
    */
   private class MockHttpClient extends HttpClient {
     public int responseCode = HttpURLConnection.HTTP_OK;
 
     public int executeMethod( HostConfiguration arg0, HttpMethod arg1, HttpState arg2 ) throws IOException,
       HttpException {
       return responseCode;
     }
 
     public int executeMethod( HostConfiguration arg0, HttpMethod arg1 ) throws IOException, HttpException {
       return responseCode;
     }
 
     public int executeMethod( HttpMethod arg0 ) throws IOException, HttpException {
       return responseCode;
     }
   };
 
   /**
    * Mock IVersionCheckDataProvider implementation that allows for canned responses and method call counting
    */
   private class MockDataProvider implements IVersionCheckDataProvider {
     public int getApplicationIDCallCount = 0;
 
     public String applicationID = "prd"; //$NON-NLS-1$
 
     public String getApplicationID() {
       ++getApplicationIDCallCount;
       return applicationID;
     }
 
     public String applicationVersion = "1.6.0-RC1.123"; //$NON-NLS-1$
 
     public String getApplicationVersion() {
       return applicationVersion;
     }
 
     public int getDepth() {
       return 154;
     }
 
     public String baseURL = "http://test.pentho.org:8080/sample"; //$NON-NLS-1$
 
     public String getBaseURL() {
       return baseURL;
     }
 
     public HashMap extraInformation = new HashMap();
 
     public Map getExtraInformation() {
       return extraInformation;
     }
 
     public void setVersionRequestFlags( int value ) {
       // TODO Auto-generated method stub
 
     }
   };
 
   private class MockResultHandler implements IVersionCheckResultHandler {
     public String results = null;
 
     public int processResultsCount = 0;
 
     public boolean throwException = false;
 
     public void processResults( String resultsStr ) {
       ++processResultsCount;
       this.results = resultsStr;
       if ( throwException ) {
         throw new NullPointerException( "Test" ); //$NON-NLS-1$
       }
     }
   };
 
   private class MockErrorHandler implements IVersionCheckErrorHandler {
 
     public int errorCount = 0;
 
     public boolean throwException = false;
 
     public void handleException( Exception e ) {
       if ( throwException ) {
         throw new NullPointerException( "Test" ); //$NON-NLS-1$
       }
     }
   };
 }
