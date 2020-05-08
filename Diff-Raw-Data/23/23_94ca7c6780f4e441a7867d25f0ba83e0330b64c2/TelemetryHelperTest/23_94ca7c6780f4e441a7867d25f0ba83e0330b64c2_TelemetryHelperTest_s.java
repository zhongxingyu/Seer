 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2011 Pentaho Corporation.  All rights reserved.
  *
  * Created Set 17th, 2012
  * @author Pedro Vale (pedro.vale@webdetails.pt)
  */
 
 package org.pentaho.telemetry;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.commons.httpclient.ConnectionPoolTimeoutException;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnection;
 import org.apache.commons.httpclient.HttpConnectionManager;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpState;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.StatusLine;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.auth.AuthState;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.junit.Assert;
 import org.junit.Test;
 
  
 
 
 
 
 public class TelemetryHelperTest {
 
   public class TelemetryHelperForTests extends TelemetryHelper {
       private String calledUrl;
       
       
       public TelemetryHelperForTests(final int resultCode, final boolean exception) {
         requestQueue.clear();
         
         defaultHttpMethod  = new HttpMethod() {
 
         @Override
         public String getName() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public HostConfiguration getHostConfiguration() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setPath(String string) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public String getPath() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public URI getURI() throws URIException {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setURI(URI uri) throws URIException {
           
         }
 
         @Override
         public void setStrictMode(boolean bln) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean isStrictMode() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setRequestHeader(String string, String string1) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setRequestHeader(Header header) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void addRequestHeader(String string, String string1) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void addRequestHeader(Header header) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Header getRequestHeader(String string) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void removeRequestHeader(String string) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void removeRequestHeader(Header header) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean getFollowRedirects() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setFollowRedirects(boolean bln) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setQueryString(String string) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setQueryString(NameValuePair[] nvps) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public String getQueryString() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Header[] getRequestHeaders() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Header[] getRequestHeaders(String string) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean validate() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public int getStatusCode() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public String getStatusText() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Header[] getResponseHeaders() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Header getResponseHeader(String string) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Header[] getResponseHeaders(String string) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Header[] getResponseFooters() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Header getResponseFooter(String string) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public byte[] getResponseBody() throws IOException {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public String getResponseBodyAsString() throws IOException {
           return "<result>OK</result>";
         }
 
         @Override
         public InputStream getResponseBodyAsStream() throws IOException {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean hasBeenUsed() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public int execute(HttpState hs, HttpConnection hc) throws HttpException, IOException {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void abort() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void recycle() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void releaseConnection() {
           
         }
 
         @Override
         public void addResponseFooter(Header header) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public StatusLine getStatusLine() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean getDoAuthentication() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setDoAuthentication(boolean bln) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public HttpMethodParams getParams() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public void setParams(HttpMethodParams hmp) {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public AuthState getHostAuthState() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public AuthState getProxyAuthState() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public boolean isRequestSent() {
           throw new UnsupportedOperationException("Not supported yet.");
         }
       };
         
         
         defaultHttpClient = new HttpClient() {
           
            public int executeMethod(HttpMethod method) throws IOException, HttpException {
              if (exception)
                throw new IOException();
              return resultCode;
           }
           
           public HttpConnectionManager getConnectionManager() {
             return new HttpConnectionManager() {
               @Override
               public HttpConnectionManagerParams getParams() {
                 return new HttpConnectionManagerParams();
               }
 
             @Override
             public HttpConnection getConnection(HostConfiguration hc) {
               throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public HttpConnection getConnection(HostConfiguration hc, long l) throws HttpException {
               throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public HttpConnection getConnectionWithTimeout(HostConfiguration hc, long l) throws ConnectionPoolTimeoutException {
               throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public void releaseConnection(HttpConnection hc) {
               throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public void closeIdleConnections(long l) {
               throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public void setParams(HttpConnectionManagerParams hcmp) {
               throw new UnsupportedOperationException("Not supported yet.");
             }
             };
           }
         };
       }
       
       @Override
       protected boolean sendRequest(String url) {
         calledUrl = url;
         return super.sendRequest(url);
       }
       
       
   
       
       public String calledUrl() {return calledUrl;};
        
       public int requestQueueSize() {return requestQueue.size();}
   
   }  
   
   
   @Test
   public void TestPublishWithEnabledTelemetry()  throws InterruptedException {
 
     
     TelemetryHelperForTests th = new TelemetryHelperForTests(200, false) ;
     
     ITelemetryDataProvider dprovider = getDefaultTelemetryDataProvider(true);
     th.setDataProvider(dprovider);
     Assert.assertTrue(th.publishTelemetryEvent());
    Assert.assertEquals("pentahoTelemetry?ep1=ev1&platVersion=4.5&pluginVersion=12.09.05&type=other&plugin=CDF", 
            th.calledUrl());
    
     Thread.sleep(1000);//Not pretty but works
     Assert.assertEquals(0, th.requestQueueSize());    
   }
   
 
   @Test
   public void TestPublishFailedResponseCode() throws InterruptedException {
 
     
     TelemetryHelperForTests th = new TelemetryHelperForTests(500, false) ;
     
     ITelemetryDataProvider dprovider = getDefaultTelemetryDataProvider(true);
     th.setDataProvider(dprovider);
     Assert.assertTrue(th.publishTelemetryEvent());
    Assert.assertEquals("pentahoTelemetry?ep1=ev1&platVersion=4.5&pluginVersion=12.09.05&type=other&plugin=CDF", 
            th.calledUrl());
    
     Thread.sleep(1000); //Not pretty but works
     Assert.assertEquals(1, th.requestQueueSize());    
   }
   
 
   
   @Test
   public void TestPublishException() throws InterruptedException {
 
     
     TelemetryHelperForTests th = new TelemetryHelperForTests(500, true) ;
     
     ITelemetryDataProvider dprovider = getDefaultTelemetryDataProvider(true);
     th.setDataProvider(dprovider);
     Assert.assertTrue(th.publishTelemetryEvent());
    Assert.assertEquals("pentahoTelemetry?ep1=ev1&platVersion=4.5&pluginVersion=12.09.05&type=other&plugin=CDF", 
            th.calledUrl());
     
     Thread.sleep(1000); //Not pretty but works
     Assert.assertEquals(1, th.requestQueueSize());    
   }
   
   
   
   @Test
   public void TestPublishWithDisabledTelemetry() {
     TelemetryHelper th = new TelemetryHelper();
     
     Assert.assertFalse(th.publishTelemetryEvent()); //Must be false before setting dataProvider
     ITelemetryDataProvider dprovider = getDefaultTelemetryDataProvider(false);
     th.setDataProvider(dprovider);
     Assert.assertFalse(th.publishTelemetryEvent());
     
   }
   
   
   private ITelemetryDataProvider getDefaultTelemetryDataProvider(final boolean enabled) {
       return new ITelemetryDataProvider() {
 
       @Override
       public String getPlatformVersion() {
         return "4.5";
       }
 
       @Override
       public String getPluginName() {
         return "CDF";
       }
 
       @Override
       public String getPluginVersion() {
         return "12.09.05";
       }
 
       @Override
       public Map<String, String> getExtraInformation() {
         HashMap<String, String> extraInfo = new HashMap<String, String>();
         extraInfo.put("ep1", "ev1");
         return extraInfo;
       }
 
       @Override
       public TelemetryHelper.TelemetryEventType getEventType() {
         return TelemetryHelper.TelemetryEventType.OTHER;
       }
 
       @Override
       public boolean isTelemetryEnabled() {
         return enabled;
       }
  
       @Override 
       public String getBaseUrl() {
         return "pentahoTelemetry";
       }      
       
       
     };    
   }
   
 }
