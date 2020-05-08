 // Copyright (C) 2005 - 2009 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package HTTPClient;
 
 import java.net.ProtocolException;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import net.grinder.testutility.RandomStubFactory;
 import junit.framework.TestCase;
 
 
 /**
  * Unit tests for our modifications to {@link Cookie}.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestCookie extends TestCase {
 
   private final RoRequestStubFactory m_roRequestStubFactory =
     new RoRequestStubFactory();
   private final RoRequest m_roRequest = m_roRequestStubFactory.getStub();
 
   public void testParse() throws Exception {
     // No cookies, nothing to do.
     Cookie.parse("", null);
 
     try {
       Cookie.parse("foo", m_roRequest);
       fail("Expected ProtocolException for bad set cookie header");
     }
     catch (ProtocolException e) {
     }
 
     final Cookie[] cookies = Cookie.parse("foo=bah", m_roRequest);
     assertEquals(1, cookies.length);
     assertEquals("host.local", cookies[0].getDomain());
     assertEquals("foo", cookies[0].getName());
     assertEquals("bah", cookies[0].getValue());
     assertEquals("/path", cookies[0].getPath());
     assertNull(cookies[0].expires());
 
     final Cookie[] cookies2 =
       Cookie.parse("foo=bah;path=lah;expires=Sat, Mar 25 16:53:28 GMT 2006", m_roRequest);
     assertEquals(1, cookies2.length);
     assertEquals("host.local", cookies2[0].getDomain());
     assertEquals("foo", cookies2[0].getName());
     assertEquals("bah", cookies2[0].getValue());
     assertEquals("lah", cookies2[0].getPath());
 
     final DateFormat df =
       DateFormat.getDateTimeInstance(
         DateFormat.SHORT, DateFormat.MEDIUM, Locale.UK);
     final Date result = df.parse("25/03/06 16:53:28");
     assertEquals(result, cookies2[0].expires());
   }
 
   public void testWithMartinsBrokenDates() throws Exception {
     final Cookie[] cookies =
       Cookie.parse("foo=bah;expires=Friday, 01-01-2038, 00:00:00 GMT",
                    m_roRequest);
     assertEquals(1, cookies.length);
     assertEquals("host.local", cookies[0].getDomain());
     assertEquals("foo", cookies[0].getName());
     assertEquals("bah", cookies[0].getValue());
     assertEquals("/path", cookies[0].getPath());
 
     // Martin's test case date isn't valid. Our modification makes the
     // parser ignore it (logging a warning to the HTTPClient logger), rather
     // than fail.
     assertNull(cookies[0].expires());
   }
 
   public void testFixForBug982834() throws Exception {
     Cookie.parse("foo=bah;expires=", m_roRequest);
   }
 
   public void testDotNetHttpOnlyNonsense() throws Exception {
     Cookie.parse(".ASPXANONYMOUS=AcbBC8KU9yE3MmQyMDA1Ni0wZDlmLTQ0MjktYWI2NS0zMTUwOGQwZmZhNTk1; expires=Wed, 16-Aug-2006 04:12:47 GMT; path=/;HttpOnly, language=en-US; path=/;HttpOnly",
       m_roRequest);
   }
 
   public void testFixForBug1576609() throws Exception {
     m_roRequestStubFactory.setHost("khan.idc.shaw.ca");
 
     final Cookie[] cookies =
       Cookie.parse(
         "ssogrp1-uwc=131088949714C3AFD48A39038260AD79;Domain=.shaw.ca;Path=/",
         m_roRequest);
 
     assertEquals(1, cookies.length);
 
     final Cookie[] cookies2 =
       Cookie.parse(
         "ssogrp1-uwc=131088949714C3AFD48A39038260AD79;Domain=.ca;Path=/",
         m_roRequest);
 
     assertEquals(0, cookies2.length);
   }
 
   public static final class RoRequestStubFactory
     extends RandomStubFactory<RoRequest> {
 
     private String m_host = "host";
     private String m_requestURI = "/path/sub;blah=blah";
 
     public RoRequestStubFactory() {
       super(RoRequest.class);
     }
 
     public HTTPConnection override_getConnection(Object proxy) {
       return new HTTPConnection(m_host);
     }
 
     public String override_getRequestURI(Object proxy) {
       return m_requestURI;
     }
 
     public void setHost(String host) {
       m_host = host;
     }
 
     public void setRequestURI(String requestURI) {
       m_requestURI = requestURI;
     }
   }
 }
