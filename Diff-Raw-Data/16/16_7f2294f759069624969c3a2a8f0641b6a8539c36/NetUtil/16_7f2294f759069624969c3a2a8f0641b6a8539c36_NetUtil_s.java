 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.util;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.NTCredentials;
 import org.apache.commons.httpclient.auth.AuthPolicy;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Net Utilities
  */
 public class NetUtil {
 
     private static Logger l = Logger.getLogger(NetUtil.class);
 
     public static void configureHttpProxy(HttpClient client) {
         final String proxyHost = System.getProperty("http.proxyHost");
         final int proxyPort = System.getProperty("http.proxyPort") == null
                 ? 8080 : Integer.parseInt(System.getProperty("http.proxyPort"));
 
         if (proxyHost != null) {
             l.debug("Configuring HTTP client with proxyHost=" + proxyHost + ", proxyPort=" + proxyPort);
             final String domain = System.getProperty("http.auth.ntlm.domain");
             if (domain != null && domain.length() > 0) {
                 l.debug("NTLM proxy requested for domain=" + domain);
                 final String user = System.getProperty("http.proxyUser");
                final String password = System.getProperty("http.proxyUser");
                 l.debug("Configuring HTTP client with proxyUser=" + user + " and password.");
                 List authPrefs = new ArrayList();
                 authPrefs.add(AuthPolicy.NTLM);
                 client.getState().setProxyCredentials(new AuthScope(null, proxyPort, null), new NTCredentials(user,
                         password, "", domain));
                 client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
             }
             client.getHostConfiguration().setProxy(proxyHost, proxyPort);
         }
     }
 
 
 }
