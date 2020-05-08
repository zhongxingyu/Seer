 package com.meterware.httpunit;
 /********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2000-2001, Russell Gold
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
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import java.util.Vector;
 import java.util.Enumeration;
 
 
 /**
  * Tests for the WebLink class.
  *
  * @author <a href="mailto:russgold@acm.org>Russell Gold</a>
  * @author <a href="mailto:bx@bigfoot.com>Benoit Xhenseval</a>
  **/
 public class WebLinkTest extends HttpUnitTest {
 
     public static void main(String args[]) {
         junit.textui.TestRunner.run( suite() );
     }
 
 
     public static Test suite() {
         return new TestSuite( WebLinkTest.class );
     }
 
 
     public WebLinkTest( String name ) {
         super( name );
     }
 
 
     public void setUp() throws Exception {
         super.setUp();
         defineResource( "SimplePage.html",
                         "<html><head><title>A Sample Page</title></head>\n" +
                         "<body>This has no forms but it does\n" +
                         "have <a href=\"/other.html\" id=\"activeID\">an <b>active</b> link</A>\n" +
                         " and <a name=here>an anchor</a>\n" +
                         "<a href=\"basic.html\" name=\"nextLink\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a>\n" +
                         "</body></html>\n" );
 
         WebConversation wc = new WebConversation();
         _simplePage = wc.getResponse( getHostPath() + "/SimplePage.html" );
     }
 
 
     public void testFindNoLinks() throws Exception {
         defineResource( "NoLinks.html", "<html><head><title>NoLinks</title></head><body>No links at all</body></html>" );
         WebConversation wc = new WebConversation();
 
         WebLink[] links = wc.getResponse( getHostPath() + "/NoLinks.html" ).getLinks();
         assertNotNull( links );
         assertEquals( 0, links.length );
     }
 
 
     public void testLinks() throws Exception {
         WebLink[] links = _simplePage.getLinks();
         assertNotNull( links );
         assertEquals( 2, links.length );
     }
 
 
     public void testLinkRequest() throws Exception {
         WebLink link = _simplePage.getLinks()[0];
         WebRequest request = link.getRequest();
         assertTrue( "Should be a get request", request instanceof GetMethodWebRequest );
         assertEquals( getHostPath() + "/other.html", request.getURL().toExternalForm() );
     }
 
 
     public void testLinkReference() throws Exception {
         WebLink link = _simplePage.getLinks()[0];
         assertEquals( "URLString", "/other.html", link.getURLString() );
     }
 
 
     public void testGetLinkByText() throws Exception {
         WebLink link = _simplePage.getLinkWith( "no link" );
         assertNull( "Non-existent link should not have been found", link );
         link = _simplePage.getLinkWith( "an active link" );
         assertNotNull( "an active link was not found", link );
         assertEquals( "active link URL", getHostPath() + "/other.html", link.getRequest().getURL().toExternalForm() );
 
         link = _simplePage.getLinkWithImageText( "Next -->" );
         assertNotNull( "the image link was not found", link );
         assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );
 
         HttpUnitOptions.setImagesTreatedAsAltText( true );
         link = _simplePage.getLinkWith( "Next -->" );
         assertNotNull( "the image link was not found", link );
         assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );
 
         HttpUnitOptions.setImagesTreatedAsAltText( false );
         link = _simplePage.getLinkWith( "Next -->" );
         assertNull( "the image link was found based on its hidden alt attribute", link );
     }
 
 
     public void testGetLinkByIDAndName() throws Exception {
         WebLink link = _simplePage.getLinkWithID( "noSuchID" );
         assertNull( "Non-existent link should not have been found", link );
 
         link = _simplePage.getLinkWithID( "activeID" );
         assertNotNull( "an active link was not found", link );
         assertEquals( "active link URL", getHostPath() + "/other.html", link.getRequest().getURL().toExternalForm() );
 
         link = _simplePage.getLinkWithName( "nextLink" );
         assertNotNull( "the image link was not found", link );
         assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );
     }
 
 
     public void testLinkText() throws Exception {
         WebLink link = _simplePage.getLinks()[0];
         assertEquals( "Link text", "an active link", link.asText() );
     }
 
 
     public void testLinkImageAsText() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "HasImage", "<a href='somwhere.html' >\r\n<img src='blah.gif' alt='Blah Blah' >\r\n</a>" );
 
         WebResponse initialPage = wc.getResponse( getHostPath() + "/HasImage.html" );
         WebLink link = initialPage.getLinks()[0];
         assertEquals( "Link text", "", link.asText() );
         initialPage.getLinkWithImageText("Blah Blah");
     }
 
 
     public void testLinkFollowing() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "Initial", "Go to <a href=\"Next.html\">the next page.</a> <a name=\"bottom\">Bottom</a>" );
         defineWebPage( "Next", "And go back to <a href=\"Initial.html#Bottom\">the first page.</a>" );
 
         WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html" );
         assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
         WebLink link = initialPage.getLinks()[0];
 
         WebResponse nextPage = wc.getResponse( link.getRequest() );
         assertEquals( "Title of next page", "Next", nextPage.getTitle() );
         assertEquals( "Num links in next page", 1, nextPage.getLinks().length );
         link = nextPage.getLinks()[0];
 
         WebResponse thirdPage = wc.getResponse( link.getRequest() );
         assertEquals( "Title of next page", "Initial", thirdPage.getTitle() );
     }
 
 
     public void testLinksWithFragmentsAndParameters() throws Exception {
         WebConversation wc = new WebConversation();
         defineResource( "Initial.html?age=3", "<html><head><title>Initial</title></head><body>" +
                                               "Go to <a href=\"Next.html\">the next page.</a> <a name=\"bottom\">Bottom</a>" +
                                               "</body></html>" );
         defineWebPage( "Next", "And go back to <a href=\"Initial.html?age=3#Bottom\">the first page.</a>" );
 
         WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html?age=3" );
         assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
         WebLink link = initialPage.getLinks()[0];
 
         WebResponse nextPage = wc.getResponse( link.getRequest() );
         assertEquals( "Title of next page", "Next", nextPage.getTitle() );
         assertEquals( "Num links in next page", 1, nextPage.getLinks().length );
         link = nextPage.getLinks()[0];
 
         WebResponse thirdPage = wc.getResponse( link.getRequest() );
         assertEquals( "Title of next page", "Initial", thirdPage.getTitle() );
     }
 
 
     public void testDocumentBase() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "alternate/Target", "Found me!" );
         defineResource( "Initial.html", "<html><head><title>Test for Base</title>" +
                                         "            <base href=\"" + getHostPath() + "/alternate/\"></head>" +
                                         "      <body><a href=\"Target.html\">Go</a></body></html>" );
 
         WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html" );
         assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
         WebLink link = initialPage.getLinks()[0];
 
         WebRequest request = link.getRequest();
         assertEquals( "Destination for link", getHostPath() + "/alternate/Target.html", request.getURL().toExternalForm() );
         WebResponse nextPage = wc.getResponse( request );
         assertTrue( "Did not find the target", nextPage.getText().indexOf( "Found" ) >= 0 );
     }
 
 
     public void testTargetBase() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "alternate/Target", "Found me!" );
         defineResource( "Initial.html", "<html><head><title>Test for Base</title>" +
                                         "            <base target=blue></head>" +
                                         "      <body><a href=\"Target.html\">Go</a></body></html>" );
 
         WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html" );
         assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
         WebLink link = initialPage.getLinks()[0];
 
         WebRequest request = link.getRequest();
         assertEquals( "Target for link", "blue", request.getTarget() );
     }
 
 
     public void testParametersOnLinks() throws Exception {
         defineResource( "ParameterLinks.html",
                         "<html><head><title>Param on Link Page</title></head>\n" +
                         "<body>" +
                         "<a href=\"/other.html\">no parameter link</A>\n" +
                         "<a href=\"/other.html?param1=value1\">one parameter link</A>\n" +
                         "<a href=\"/other.html?param1=value1&param2=value2\">two parameters link</A>\n" +
                         "<a href=\"/other.html?param1=value1&param1=value3\">two values link</A>\n" +
                         "<a href=\"/other.html?param1=value1&param2=value2&param1=value3\">two values link</A>\n" +
                         "</body></html>\n" );
         WebConversation wc = new WebConversation();
 
         WebLink[] links = wc.getResponse( getHostPath() + "/ParameterLinks.html" ).getLinks();
         assertNotNull( links );
         assertEquals( "number of links", 5, links.length );
         WebRequest request;
 
         // first link should not have any param
         request = links[0].getRequest();
         assertNotNull( request);
         Enumeration e = request.getParameterNames();
         assertNotNull( e);
         assertTrue( "Should not have any params", !e.hasMoreElements() );
         assertEquals("Non Existent parameter should be empty","",request.getParameter("nonexistent"));
 
         // second link should have one parameter
         request = links[1].getRequest();
         assertNotNull( request );
         e = request.getParameterNames();
         assertTrue( "No parameter found", e.hasMoreElements() );
         String paramName = (String)e.nextElement();
         assertNotNull(paramName);
         assertTrue( "More than one parameter found", !e.hasMoreElements());
         assertEquals("param1",paramName);
         assertEquals("value1",request.getParameter(paramName));
 
         // third link should have 2 parameters.  !! Order of parameters cannot be guaranted.
         request = links[2].getRequest();
         assertNotNull( request );
         e = request.getParameterNames();
         assertTrue( "No parameters found", e.hasMoreElements() );
         paramName = (String)e.nextElement();
         assertNotNull(paramName);
         assertTrue( "Only one parameter found", e.hasMoreElements());
         String paramName2 = (String)e.nextElement();
         assertNotNull(paramName2);
         assertTrue("different names",!paramName.equals(paramName2));
         assertTrue("test names for param1",paramName.equals("param1") || paramName2.equals("param1"));
         assertTrue("test names for param2",paramName.equals("param2") || paramName2.equals("param2"));
         assertEquals("value1",request.getParameter("param1"));
         assertEquals("value2",request.getParameter("param2"));
 
         // fourth link should have 1 parameter with 2 values.
         request = links[3].getRequest();
         assertNotNull( request );
         e = request.getParameterNames();
         assertTrue( "No parameters found", e.hasMoreElements() );
         paramName = (String)e.nextElement();
         assertNotNull(paramName);
         String[] values = request.getParameterValues("param1");
         assertEquals("Length of ",2,values.length);
         assertMatchingSet("Values",new String[] {"value1", "value3"}, values);
 
         // fifth link should have 2 parameters with one with 2 values.
         request = links[4].getRequest();
         assertNotNull( request );
         e = request.getParameterNames();
         assertTrue( "No parameters found", e.hasMoreElements() );
         paramName = (String)e.nextElement();
         assertNotNull(paramName);
         assertTrue( "Only one parameter found", e.hasMoreElements() );
         paramName2 = (String)e.nextElement();
         assertNotNull(paramName2);
         assertTrue("different names",!paramName.equals(paramName2));
         values = request.getParameterValues("param1");
         assertEquals("Length of ",2,values.length);
         assertMatchingSet("Values for param1",new String[] {"value1", "value3"}, values);
         assertMatchingSet("Values form param2",new String[] {"value2"}, request.getParameterValues("param2"));
         assertEquals("value2",request.getParameter("param2"));
     }
 
 
     public void testEncodedLinkParameters() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "encodedLinks", "<html><head><title>Encode Test</title></head>" +
                                        "<body>" +
                                        "<a href=\"/request?%24dollar=%25percent&%23hash=%26ampersand\">request</a>" +
                                        "</body></html>" );
         WebResponse mapPage = wc.getResponse( getHostPath() + "/encodedLinks.html" );
         WebLink link = mapPage.getLinks()[0];
         WebRequest wr = link.getRequest();
         assertMatchingSet( "Request parameter names", new String[] { "$dollar", "#hash" }, toStringArray( wr.getParameterNames() ) );
         assertEquals( "Value of $dollar", "%percent", wr.getParameter( "$dollar" ) );
         assertEquals( "Value of #hash", "&ampersand", wr.getParameter( "#hash" ) );
     }
 
 
     public void testValuelessLinkParameters() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "encodedLinks", "<html><head><title>Encode Test</title></head>" +
                                        "<body>" +
                                        "<a href=\"/request?arg1&valueless=\">request</a>" +
                                        "</body></html>" );
         WebResponse mapPage = wc.getResponse( getHostPath() + "/encodedLinks.html" );
         WebLink link = mapPage.getLinks()[0];
         WebRequest wr = link.getRequest();
         assertMatchingSet( "Request parameter names", new String[] { "arg1", "valueless" }, toStringArray( wr.getParameterNames() ) );
         assertEquals( "Value of arg1", null, wr.getParameter( "arg1" ) );
     }
 
 
     public void testLinkParameterOrder() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "encodedLinks", "<html><head><title>Encode Test</title></head>" +
                                        "<body>" +
                                        "<a href='/request?arg0=0\n&arg1&arg0=2&valueless='>request</a>" +
                                        "</body></html>" );
         WebResponse mapPage = wc.getResponse( getHostPath() + "/encodedLinks.html" );
         WebLink link = mapPage.getLinks()[0];
         WebRequest wr = link.getRequest();
         assertMatchingSet( "Request parameter names", new String[] { "arg0", "arg1", "valueless" }, toStringArray( wr.getParameterNames() ) );
         assertMatchingSet( "Value of arg0", new String[] { "0", "2" }, wr.getParameterValues( "arg0" ) );
         assertEquals( "Actual query", "arg0=0&arg1&arg0=2&valueless=", wr.getQueryString() );
     }
 
 
     public void testLinkParameterValidation() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "encodedLinks", "<html><head><title>Encode Test</title></head>" +
                                        "<body>" +
                                        "<a href='/request?arg0=0&arg1&arg0=2&valueless='>request</a>" +
                                        "</body></html>" );
         WebResponse mapPage = wc.getResponse( getHostPath() + "/encodedLinks.html" );
         WebLink link = mapPage.getLinks()[0];
         WebRequest wr = link.getRequest();
         wr.setParameter( "arg0", new String[] { "0", "2" } );
         try {
             wr.setParameter( "arg0", "3" );
             fail( "Did not prevent change to link parameters" );
         } catch (IllegalRequestParameterException e) {}
     }
 
 
     private String[] toStringArray( Enumeration e ) {
         Vector v = new Vector();
         while (e.hasMoreElements()) v.addElement( e.nextElement() );
         String[] result = new String[ v.size() ];
         v.copyInto( result );
         return result;
     }
 
 
     public void testImageMapLinks() throws Exception {
         WebConversation wc = new WebConversation();
         defineWebPage( "pageWithMap", "Here is a page with <a href=\"somewhere\">a link</a>" +
                                       " and a map: <IMG src=\"navbar1.gif\" usemap=\"#map1\" alt=\"navigation bar\">" +
                                       "<map name=\"map1\">" +
                                       "  <area href=\"guide.html\" alt=\"Guide\" shape=\"rect\" coords=\"0,0,118,28\">" +
                                       "  <area href=\"search.html\" alt=\"Search\" shape=\"circle\" coords=\"184,200,60\">" +
                                       "</map>" );
         WebResponse mapPage = wc.getResponse( getHostPath() + "/pageWithMap.html" );
         WebLink[] links = mapPage.getLinks();
         assertEquals( "number of links found", 3, links.length );
 
         WebLink guide = mapPage.getLinkWith( "Guide" );
         assertNotNull( "Did not find the guide area", guide );
         assertEquals( "Relative URL", "guide.html", guide.getURLString() );
     }
 
     private WebResponse _simplePage;
 }
