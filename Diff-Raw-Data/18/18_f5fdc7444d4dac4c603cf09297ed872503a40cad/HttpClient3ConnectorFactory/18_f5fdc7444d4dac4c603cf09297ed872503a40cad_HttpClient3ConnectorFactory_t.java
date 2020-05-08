 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2010 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.ide.wsclient;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
 import org.sonar.wsclient.Host;
 import org.sonar.wsclient.connectors.HttpClient3Connector;
 
import java.net.*;
import java.util.List;

 /**
  * @author Evgeny Mandrikov
  */
 public final class HttpClient3ConnectorFactory {
   protected static final int TIMEOUT_MS = 30000;
   private static final int MAX_TOTAL_CONNECTIONS = 40;
   private static final int MAX_HOST_CONNECTIONS = 4;
 
   /**
    * Hide utility-class constructor.
    */
   private HttpClient3ConnectorFactory() {
   }
 
   /**
    * Workaround for <a href="http://jira.codehaus.org/browse/SONAR-1715">SONAR-1715</a> and
    * for <a href="http://jira.codehaus.org/browse/SONAR-1586">SONAR-1586</a>.
    * <p>
    * TODO Godin: I suppose that call of method {@link HttpClient3Connector#configureCredentials()} should be added to constructor
    * {@link HttpClient3Connector#HttpClient3Connector(Host, HttpClient)}
    * </p>
    * 
    * @param server Sonar server
    * @return configured {@link HttpClient3Connector}
    * @see org.sonar.wsclient.connectors.HttpClient3Connector#createClient()
    * @see org.sonar.wsclient.connectors.HttpClient3Connector#configureCredentials()
    */
   public static HttpClient3Connector createConnector(Host server) {
     // createClient
     final HttpConnectionManagerParams params = new HttpConnectionManagerParams();
     params.setConnectionTimeout(TIMEOUT_MS);
     params.setSoTimeout(TIMEOUT_MS);
     params.setDefaultMaxConnectionsPerHost(MAX_HOST_CONNECTIONS);
     params.setMaxTotalConnections(MAX_TOTAL_CONNECTIONS);
     final MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
     connectionManager.setParams(params);
     HttpClient httpClient = new HttpClient(connectionManager);
 
     // configureCredentials
    if (StringUtils.isNotEmpty(server.getUsername())) {
       httpClient.getParams().setAuthenticationPreemptive(true);
       Credentials defaultcreds = new UsernamePasswordCredentials(server.getUsername(), server.getPassword());
       httpClient.getState().setCredentials(AuthScope.ANY, defaultcreds);
     }
 
     // TODO Godin: ugly hack to use ProxySelector with commons-httpclient 3.1
     try {
       List<Proxy> list = ProxySelector.getDefault().select(new URI(server.getHost()));
       for (Proxy proxy : list) {
         if (proxy.type() == Proxy.Type.HTTP) {
           // Proxy
           InetSocketAddress addr = (InetSocketAddress) proxy.address();
           httpClient.getHostConfiguration().setProxy(addr.getHostName(), addr.getPort());
           // Proxy auth
           InetAddress proxyInetAddress = InetAddress.getByName(addr.getHostName());
           PasswordAuthentication auth = Authenticator.requestPasswordAuthentication(proxyInetAddress, addr.getPort(), null, null, null);
           if (auth != null) {
             Credentials defaultcreds = new UsernamePasswordCredentials(auth.getUserName(), new String(auth.getPassword()));
             httpClient.getState().setProxyCredentials(AuthScope.ANY, defaultcreds);
           }
         }
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     return new HttpClient3Connector(server, httpClient);
   }
 }
