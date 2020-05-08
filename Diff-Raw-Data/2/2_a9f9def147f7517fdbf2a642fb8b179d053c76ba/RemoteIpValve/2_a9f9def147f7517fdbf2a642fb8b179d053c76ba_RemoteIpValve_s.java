 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.catalina.connector;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.servlet.ServletException;
 
 import org.apache.catalina.util.StringManager;
 import org.apache.catalina.valves.Constants;
 import org.apache.catalina.valves.RequestFilterValve;
 import org.apache.catalina.valves.ValveBase;
 import org.apache.juli.logging.Log;
 import org.apache.juli.logging.LogFactory;
 
 /**
  * <p>
  * Tomcat port of <a href="http://httpd.apache.org/docs/trunk/mod/mod_remoteip.html">mod_remoteip</a>, this valve replaces the apparent
  * client remote IP address and hostname for the request with the IP address list presented by a proxy or a load balancer via a request
  * headers.
  * </p>
  * <p>
  * This valve proceeds as follows:
  * <ul>
  * <li>Check if the incoming <code>request.getRemoteAddr()</code> matches the valve's list of internal proxies.</li>
  * <li>If so, loop on the comma delimited list of IPs and hostnames passed by the preceding load balancer or proxy in the given request's
  * Http header named <code>remoteIPHeader</code> (default value <code>x-forwarded-for</code>). Values are processed in Right-to-Left order.</li>
  * <li>For each ip/host of the list:
  * <ul>
  * <li>if it matches the internal proxies list, the ip/host is swallowed</li>
  * <li>if it matches the trusted proxies list, the ip/host is added to the created proxies header</li>
  * <li>otherwise, the ip/host is declared to be the remote ip and looping is stopped.</li>
  * </ul>
  * </li>
  * </ul>
  * </p>
  * <p>
  * <strong>Configuration parameters:</strong>
  * <table border="1">
  * <tr>
  * <th>RemoteIpValve property</th>
  * <th>Equivalent mod_remoteip directive</th>
  * <th>Format</th>
  * <th>Default Value</th>
  * </tr>
  * <tr>
  * <td>remoteIPHeader</td>
  * <td>RemoteIPHeader</td>
  * <td>Compliant http header string</td>
  * <td>x-forwarded-for</td>
  * </tr>
  * <tr>
  * <td>internalProxies</td>
  * <td>RemoteIPInternalProxy</td>
  * <td>Comma delimited list of regular expressions (in the syntax supported by the {@link java.util.regex.Pattern} library)</td>
  * <td>10\.\d{1,3}\.\d{1,3}\.\d{1,3}, 192\.168\.\d{1,3}\.\d{1,3}, 169\.254\.\d{1,3}\.\d{1,3}, 127\.\d{1,3}\.\d{1,3}\.\d{1,3} <br/>
  * By default, 10/8, 192.168/16, 169.254/16 and 127/8 are allowed ; 172.16/12 has not been enabled by default because it is complex to
  * describe with regular expressions</td>
  * </tr>
  * </tr>
  * <tr>
  * <td>proxiesHeader</td>
  * <td>RemoteIPProxiesHeader</td>
  * <td>Compliant http header String</td>
  * <td>x-forwarded-by</td>
  * </tr>
  * <tr>
  * <td>trustedProxies</td>
  * <td>RemoteIPTrustedProxy</td>
  * <td>Comma delimited list of regular expressions (in the syntax supported by the {@link java.util.regex.Pattern} library)</td>
  * <td>&nbsp;</td>
  * </tr>
  * <tr>
  * <td>protocolHeader</td>
  * <td>N/A</td>
  * <td>Compliant http header string like <code>X-Forwarded-Pro</code></td>
  * <td><code>null</code></td>
  * </tr>
  * <tr>
  * <td>protocolHeaderSslValue</td>
  * <td>N/A</td>
  * <td>String like <code>https</code> or <code>ON</code></td>
  * <td><code>https</code></td>
  * </tr>
  * <tr>
  * </table>
  * </p>
  * <p>
  * <p>
  * This Valve may be attached to any Container, depending on the granularity of the filtering you wish to perform.
  * </p>
  * <p>
  * <strong>Regular expression vs. IP address blocks:</strong> <code>mod_remoteip</code> allows to use address blocks (e.g.
  * <code>192.168/16</code>) to configure <code>RemoteIPInternalProxy</code> and <code>RemoteIPTrustedProxy</code> ; as Tomcat doesn't have a
  * library similar to <a
  * href="http://apr.apache.org/docs/apr/1.3/group__apr__network__io.html#gb74d21b8898b7c40bf7fd07ad3eb993d">apr_ipsubnet_test</a>,
  * <code>RemoteIpValve</code> uses regular expression to configure <code>internalProxies</code> and <code>trustedProxies</code> in the same
  * fashion as {@link RequestFilterValve} does.
  * </p>
  * <p>
  * <strong>Package org.apache.catalina.connector vs. org.apache.catalina.valves</strong>: This valve is temporarily located in
  * <code>org.apache.catalina.connector</code> package instead of <code>org.apache.catalina.valves</code> because it uses
  * <code>protected</code> visibility of {@link Request#remoteAddr} and {@link Request#remoteHost}. This valve could move to
  * <code>org.apache.catalina.valves</code> if {@link Request#setRemoteAddr(String)} and {@link Request#setRemoteHost(String)} were modified
  * to no longer be no-op but actually set the underlying property.
  * </p>
  * <hr/>
  * <p>
  * <strong>Sample with trusted proxies</strong>
  * </p>
  * <p>
  * RemoteIpValve configuration:
  * </p>
  * <code><pre>
  * &lt;Valve 
  *   className="org.apache.catalina.connector.RemoteIpValve"
  *   allowedInternalProxies="192\.168\.0\.10, 192\.168\.0\.11"
  *   remoteIPHeader="x-forwarded-for"
  *   remoteIPProxiesHeader="x-forwarded-by"
  *   trustedProxies="proxy1, proxy2"
  *   /&gt;</pre></code>
  * <p>
  * Request values:
  * <table border="1">
  * <tr>
  * <th>property</th>
  * <th>Value Before RemoteIpValve</th>
  * <th>Value After RemoteIpValve</th>
  * </tr>
  * <tr>
  * <td>request.remoteAddr</td>
  * <td>192.168.0.10</td>
  * <td>140.211.11.130</td>
  * </tr>
  * <tr>
  * <td>request.header['x-forwarded-for']</td>
  * <td>140.211.11.130, proxy1, proxy2</td>
  * <td>null</td>
  * </tr>
  * <tr>
  * <td>request.header['x-forwarded-by']</td>
  * <td>null</td>
  * <td>proxy1, proxy2</td>
  * </tr>
  * </table>
  * Note : <code>proxy1</code> and <code>proxy2</code> are both trusted proxies that come in <code>x-forwarded-for</code> header, they both
  * are migrated in <code>x-forwarded-by</code> header. <code>x-forwarded-by</code> is null because all the proxies are trusted or internal.
  * </p>
  * <hr/>
  * <p>
  * <strong>Sample with internal and trusted proxies</strong>
  * </p>
  * <p>
  * RemoteIpValve configuration:
  * </p>
  * <code><pre>
  * &lt;Valve 
  *   className="org.apache.catalina.connector.RemoteIpValve"
  *   allowedInternalProxies="192\.168\.0\.10, 192\.168\.0\.11"
  *   remoteIPHeader="x-forwarded-for"
  *   remoteIPProxiesHeader="x-forwarded-by"
  *   trustedProxies="proxy1, proxy2"
  *   /&gt;</pre></code>
  * <p>
  * Request values:
  * <table border="1">
  * <tr>
  * <th>property</th>
  * <th>Value Before RemoteIpValve</th>
  * <th>Value After RemoteIpValve</th>
  * </tr>
  * <tr>
  * <td>request.remoteAddr</td>
  * <td>192.168.0.10</td>
  * <td>140.211.11.130</td>
  * </tr>
  * <tr>
  * <td>request.header['x-forwarded-for']</td>
  * <td>140.211.11.130, proxy1, proxy2, 192.168.0.10</td>
  * <td>null</td>
  * </tr>
  * <tr>
  * <td>request.header['x-forwarded-by']</td>
  * <td>null</td>
  * <td>proxy1, proxy2</td>
  * </tr>
  * </table>
  * Note : <code>proxy1</code> and <code>proxy2</code> are both trusted proxies that come in <code>x-forwarded-for</code> header, they both
  * are migrated in <code>x-forwarded-by</code> header. As <code>192.168.0.10</code> is an internal proxy, it does not appear in
  * <code>x-forwarded-by</code>. <code>x-forwarded-by</code> is null because all the proxies are trusted or internal.
  * </p>
  * <hr/>
  * <p>
  * <strong>Sample with internal proxies</strong>
  * </p>
  * <p>
  * RemoteIpValve configuration:
  * </p>
  * <code><pre>
  * &lt;Valve 
  *   className="org.apache.catalina.connector.RemoteIpValve"
  *   allowedInternalProxies="192\.168\.0\.10, 192\.168\.0\.11"
  *   remoteIPHeader="x-forwarded-for"
  *   remoteIPProxiesHeader="x-forwarded-by"
  *   /&gt;</pre></code>
  * <p>
  * Request values:
  * <table border="1">
  * <tr>
  * <th>property</th>
  * <th>Value Before RemoteIpValve</th>
  * <th>Value After RemoteIpValve</th>
  * </tr>
  * <tr>
  * <td>request.remoteAddr</td>
  * <td>192.168.0.10</td>
  * <td>140.211.11.130</td>
  * </tr>
  * <tr>
  * <td>request.header['x-forwarded-for']</td>
  * <td>140.211.11.130, 192.168.0.10</td>
  * <td>null</td>
  * </tr>
  * <tr>
  * <td>request.header['x-forwarded-by']</td>
  * <td>null</td>
  * <td>null</td>
  * </tr>
  * </table>
  * Note : <code>x-forwarded-by</code> header is null because only internal proxies as been traversed by the request.
  * <code>x-forwarded-by</code> is null because all the proxies are trusted or internal.
  * </p>
  * <hr/>
  * <p>
  * <strong>Sample with an untrusted proxy</strong>
  * </p>
  * <p>
  * RemoteIpValve configuration:
  * </p>
  * <code><pre>
  * &lt;Valve 
  *   className="org.apache.catalina.connector.RemoteIpValve"
  *   allowedInternalProxies="192\.168\.0\.10, 192\.168\.0\.11"
  *   remoteIPHeader="x-forwarded-for"
  *   remoteIPProxiesHeader="x-forwarded-by"
  *   trustedProxies="proxy1, proxy2"
  *   /&gt;</pre></code>
  * <p>
  * Request values:
  * <table border="1">
  * <tr>
  * <th>property</th>
  * <th>Value Before RemoteIpValve</th>
  * <th>Value After RemoteIpValve</th>
  * </tr>
  * <tr>
  * <td>request.remoteAddr</td>
  * <td>192.168.0.10</td>
  * <td>untrusted-proxy</td>
  * </tr>
  * <tr>
  * <td>request.header['x-forwarded-for']</td>
  * <td>140.211.11.130, untrusted-proxy, proxy1</td>
  * <td>140.211.11.130</td>
  * </tr>
  * <tr>
  * <td>request.header['x-forwarded-by']</td>
  * <td>null</td>
  * <td>proxy1</td>
  * </tr>
  * </table>
  * Note : <code>x-forwarded-by</code> holds the trusted proxy <code>proxy1</code>. <code>x-forwarded-by</code> holds
  * <code>140.211.11.130</code> because <code>untrusted-proxy</code> is not trusted and thus, we can not trust that
  * <code>untrusted-proxy</code> is the actual remote ip. <code>request.remoteAddr</code> is <code>untrusted-proxy</code> that is an IP
  * verified by <code>proxy1</code>.
  * </p>
  * <hr/>
  * <p>
  * TODO : add "remoteIpValve.syntax" NLSString, declare valve in mbeans-descriptors.xml
  * </p>
  */
 public class RemoteIpValve extends ValveBase {
     
     /**
      * {@link Pattern} for a comma delimited string that support whitespace characters
      */
     private static final Pattern commaSeparatedValuesPattern = Pattern.compile("\\s*,\\s*");
     
     /**
      * The descriptive information related to this implementation.
      */
     private static final String info = "org.apache.catalina.connector.RemoteIpValve/1.0";
     
     /**
      * Logger
      */
     private static Log log = LogFactory.getLog(RemoteIpValve.class);
     
     /**
      * The StringManager for this package.
      */
     protected static StringManager sm = StringManager.getManager(Constants.Package);
     
     /**
      * Convert a given comma delimited list of regular expressions into an array of compiled {@link Pattern}
      * 
     * @return array of pattenrs (not <code>null</code>)
      */
     protected static Pattern[] commaDelimitedListToPatternArray(String commaDelimitedPatterns) {
         String[] patterns = commaDelimitedListToStringArray(commaDelimitedPatterns);
         List<Pattern> patternsList = new ArrayList<Pattern>();
         for (String pattern : patterns) {
             try {
                 patternsList.add(Pattern.compile(pattern));
             } catch (PatternSyntaxException e) {
                 throw new IllegalArgumentException(sm.getString("remoteIpValve.syntax", pattern), e);
             }
         }
         return patternsList.toArray(new Pattern[0]);
     }
     
     /**
      * Convert a given comma delimited list of regular expressions into an array of String
      * 
      * @return array of patterns (non <code>null</code>)
      */
     protected static String[] commaDelimitedListToStringArray(String commaDelimitedStrings) {
         return (commaDelimitedStrings == null || commaDelimitedStrings.length() == 0) ? new String[0] : commaSeparatedValuesPattern
             .split(commaDelimitedStrings);
     }
     
     /**
      * Convert an array of strings in a comma delimited string
      */
     protected static String listToCommaDelimitedString(List<String> stringList) {
         if (stringList == null) {
             return "";
         }
         StringBuilder result = new StringBuilder();
         for (Iterator<String> it = stringList.iterator(); it.hasNext();) {
             Object element = it.next();
             if (element != null) {
                 result.append(element);
                 if (it.hasNext()) {
                     result.append(", ");
                 }
             }
         }
         return result.toString();
     }
     
     /**
      * Return <code>true</code> if the given <code>str</code> matches at least one of the given <code>patterns</code>.
      */
     protected static boolean matchesOne(String str, Pattern... patterns) {
         for (Pattern pattern : patterns) {
             if (pattern.matcher(str).matches()) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * @see #setInternalProxies(String)
      */
     private Pattern[] internalProxies = new Pattern[] {
         Pattern.compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}"),
         Pattern.compile("169\\.254\\.\\d{1,3}\\.\\d{1,3}"), Pattern.compile("127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
     };
     
     /**
      * @see #setProtocolHeader(String)
      */
     private String protocolHeader = null;
     
     private String protocolHeaderSslValue = "https";
     
     /**
      * @see #setProxiesHeader(String)
      */
     private String proxiesHeader = "X-Forwarded-By";
     
     /**
      * @see #setRemoteIPHeader(String)
      */
     private String remoteIPHeader = "X-Forwarded-For";
     
     /**
      * @see RemoteIpValve#setTrustedProxies(String)
      */
     private Pattern[] trustedProxies = new Pattern[0];
     
     /**
      * Return descriptive information about this Valve implementation.
      */
     public String getInfo() {
         return info;
     }
     
     /**
      * @see #setInternalProxies(String)
      * @return comma delimited list of internal proxies
      */
     public String getInternalProxies() {
         List<String> internalProxiesAsStringList = new ArrayList<String>();
         for (Pattern internalProxyPattern : internalProxies) {
             internalProxiesAsStringList.add(String.valueOf(internalProxyPattern));
         }
         return listToCommaDelimitedString(internalProxiesAsStringList);
     }
     
     /**
      * @see #setProtocolHeader(String)
      * @return the protocol header (e.g. "X-Forwarded-Proto")
      */
     public String getProtocolHeader() {
         return protocolHeader;
     }
     
     /**
      * @see RemoteIpValve#setProtocolHeaderSslValue(String)
      * @return the value of the protocol header for incoming https request (e.g. "https")
      */
     public String getProtocolHeaderSslValue() {
         return protocolHeaderSslValue;
     }
     
     /**
      * @see #setProxiesHeader(String)
      * @return the proxies header name (e.g. "X-Forwarded-By")
      */
     public String getProxiesHeader() {
         return proxiesHeader;
     }
     
     /**
      * @see #setRemoteIPHeader(String)
      * @return the remote IP header name (e.g. "X-Forwarded-For")
      */
     public String getRemoteIPHeader() {
         return remoteIPHeader;
     }
     
     /**
      * @see #setTrustedProxies(String)
      * @return comma delimited list of trusted proxies
      */
     public String getTrustedProxies() {
         List<String> trustedProxiesAsStringList = new ArrayList<String>();
         for (Pattern trustedProxy : trustedProxies) {
             trustedProxiesAsStringList.add(String.valueOf(trustedProxy));
         }
         return listToCommaDelimitedString(trustedProxiesAsStringList);
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void invoke(Request request, Response response) throws IOException, ServletException {
         final String originalRemoteAddr = request.getRemoteAddr();
         final String originalRemoteHost = request.getRemoteHost();
         final String originalScheme = request.getScheme();
         final boolean originalSecure = request.isSecure();
         
         if (matchesOne(originalRemoteAddr, internalProxies)) {
             String remoteIp = null;
             // In java 6, proxiesHeaderValue should be declared as a java.util.Deque
             LinkedList<String> proxiesHeaderValue = new LinkedList<String>();
             
             String[] remoteIPHeaderValue = commaDelimitedListToStringArray(request.getHeader(remoteIPHeader));
             int idx;
             // loop on remoteIPHeaderValue to find the first trusted remote ip and to build the proxies chain
             for (idx = remoteIPHeaderValue.length - 1; idx >= 0; idx--) {
                 String currentRemoteIp = remoteIPHeaderValue[idx];
                 remoteIp = currentRemoteIp;
                 if (matchesOne(currentRemoteIp, internalProxies)) {
                     // do nothing, internalProxies IPs are not appended to the
                 } else if (matchesOne(currentRemoteIp, trustedProxies)) {
                     proxiesHeaderValue.addFirst(currentRemoteIp);
                 } else {
                     idx--; // decrement idx because break statement doesn't do it
                     break;
                 }
             }
             // continue to loop on remoteIPHeaderValue to build the new value of the remoteIPHeader
             LinkedList<String> newRemoteIpHeaderValue = new LinkedList<String>();
             for (; idx >= 0; idx--) {
                 String currentRemoteIp = remoteIPHeaderValue[idx];
                 newRemoteIpHeaderValue.addFirst(currentRemoteIp);
             }
             if (remoteIp != null) {
                 
                 // use field access instead of setters because request.setRemoteAddr(str) and request.setRemoteHost() are no-op in Tomcat
                 // 6.0
                 request.remoteAddr = remoteIp;
                 request.remoteHost = remoteIp;
                 
                 // use request.coyoteRequest.mimeHeaders.setValue(str).setString(str) because request.addHeader(str, str) is no-op in Tomcat
                 // 6.0
                 if (proxiesHeaderValue.size() == 0) {
                     request.getCoyoteRequest().getMimeHeaders().removeHeader(proxiesHeader);
                 } else {
                     String commaDelimitedListOfProxies = listToCommaDelimitedString(proxiesHeaderValue);
                     request.getCoyoteRequest().getMimeHeaders().setValue(proxiesHeader).setString(commaDelimitedListOfProxies);
                 }
                 if (newRemoteIpHeaderValue.size() == 0) {
                     request.getCoyoteRequest().getMimeHeaders().removeHeader(remoteIPHeader);
                 } else {
                     String commaDelimitedRemoteIpHeaderValue = listToCommaDelimitedString(newRemoteIpHeaderValue);
                     request.getCoyoteRequest().getMimeHeaders().setValue(remoteIPHeader).setString(commaDelimitedRemoteIpHeaderValue);
                 }
             }
             
             if (protocolHeader != null) {
                 String protocolHeaderValue = request.getHeader(protocolHeader);
                 if (protocolHeaderValue != null && protocolHeaderSslValue.equalsIgnoreCase(protocolHeaderValue)) {
                     request.setSecure(true);
                     // use request.coyoteRequest.scheme instead of request.setScheme() because request.setScheme() is no-op in Tomcat 6.0
                     request.getCoyoteRequest().scheme().setString("https");
                 }
             }
             
             if (log.isDebugEnabled()) {
                 log.debug("Incoming request " + request.getRequestURI() + " with originalRemoteAddr '" + originalRemoteAddr
                           + "', originalRemoteHost='" + originalRemoteHost + "', originalSecure='" + originalSecure + "', originalScheme='"
                           + originalScheme + "' will be seen as newRemoteAddr='" + request.getRemoteAddr() + "', newRemoteHost='"
                           + request.getRemoteHost() + "', newScheme='" + request.getScheme() + "', newSecure='" + request.isSecure() + "'");
             }
         }
         try {
             getNext().invoke(request, response);
         } finally {
             // use field access instead of setters because setters are no-op in Tomcat 6.0
             request.remoteAddr = originalRemoteAddr;
             request.remoteHost = originalRemoteHost;
             
             request.setSecure(originalSecure);
             
             // use request.coyoteRequest.scheme instead of request.setScheme() because request.setScheme() is no-op in Tomcat 6.0
             request.getCoyoteRequest().scheme().setString(originalScheme);
         }
     }
     
     /**
      * <p>
      * Comma delimited list of internal proxies. Can be expressed with regular expressions.
      * </p>
      * <p>
      * Default value : 10\.\d{1,3}\.\d{1,3}\.\d{1,3}, 192\.168\.\d{1,3}\.\d{1,3}, 127\.\d{1,3}\.\d{1,3}\.\d{1,3}
      * </p>
      */
     public void setInternalProxies(String commaAllowedInternalProxies) {
         this.internalProxies = commaDelimitedListToPatternArray(commaAllowedInternalProxies);
     }
     
     /**
      * <p>
      * Header that holds the incoming protocol, usally named <code>X-Forwarded-Proto</code>. If <code>null</code>, request.scheme and
      * request.secure will not be modified.
      * </p>
      * <p>
      * Default value : <code>null</code>
      * </p>
      */
     public void setProtocolHeader(String protocolHeader) {
         this.protocolHeader = protocolHeader;
     }
     
     /**
      * <p>
      * Case insensitive value of the protocol header to indicate that the incoming http request uses SSL.
      * </p>
      * <p>
      * Default value : <code>HTTPS</code>
      * </p>
      */
     public void setProtocolHeaderSslValue(String protocolHeaderSslValue) {
         this.protocolHeaderSslValue = protocolHeaderSslValue;
     }
     
     /**
      * <p>
      * The proxiesHeader directive specifies a header into which mod_remoteip will collect a list of all of the intermediate client IP
      * addresses trusted to resolve the actual remote IP. Note that intermediate RemoteIPTrustedProxy addresses are recorded in this header,
      * while any intermediate RemoteIPInternalProxy addresses are discarded.
      * </p>
      * <p>
      * Name of the http header that holds the list of trusted proxies that has been traversed by the http request.
      * </p>
      * <p>
      * The value of this header can be comma delimited.
      * </p>
      * <p>
      * Default value : <code>X-Forwarded-By</code>
      * </p>
      */
     public void setProxiesHeader(String proxiesHeader) {
         this.proxiesHeader = proxiesHeader;
     }
     
     /**
      * <p>
      * Name of the http header from which the remote ip is extracted.
      * </p>
      * <p>
      * The value of this header can be comma delimited.
      * </p>
      * <p>
      * Default value : <code>X-Forwarded-For</code>
      * </p>
      * 
      * @param remoteIPHeader
      */
     public void setRemoteIPHeader(String remoteIPHeader) {
         this.remoteIPHeader = remoteIPHeader;
     }
     
     /**
      * <p>
      * Comma delimited list of proxies that are trusted when they appear in the {@link #remoteIPHeader} header. Can be expressed as a
      * regular expression.
      * </p>
      * <p>
      * Default value : empty list, no external proxy is trusted.
      * </p>
      */
     public void setTrustedProxies(String commaDelimitedTrustedProxies) {
         this.trustedProxies = commaDelimitedListToPatternArray(commaDelimitedTrustedProxies);
     }
 }
