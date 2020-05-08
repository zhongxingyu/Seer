 /*
  * Copyright (C) 2013 Jajja Communications AB
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package com.jajja.arachne.net;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.jajja.arachne.exceptions.MalformedDomainException;
 import com.jajja.arachne.exceptions.MalformedUriException;
 
 /**
  * XXX: First draft for URL with proper URI parsing
  *
  * @author Andreas Allerdahl <andreas.allerdahl@jajja.com>
  * @author Martin Korinth <martin.korinth@jajja.com>
  */
 public class Url {
     public final static int REPAIR = 0;
     private final static Pattern schemeValidationPattern = Pattern.compile("^[a-z][a-z0-9*.-]*:", Pattern.CASE_INSENSITIVE);
     private final static Pattern repairPattern = Pattern.compile("^([a-z][a-z0-9*.-]*):/+(.*)", Pattern.CASE_INSENSITIVE);
     private final static Pattern deprefixHostPattern = Pattern.compile("^(www|ftp|smtp|mail|pop)[0-9]*\\.", Pattern.CASE_INSENSITIVE);
     String string;
     String scheme;
     String userInfo;
     Host host;
     Integer port;
     String path;
     LinkedList<Parameter> parameters;
     String querySeparator = "&";
     String fragment;
     String encoding;
 
     private void parse() throws MalformedUriException {
         int pos;
         String string = this.string;
         if (!schemeValidationPattern.matcher(string).find()) {
             throw new MalformedUriException(string, "invalid scheme");
         }
 
         if ((pos = string.indexOf('#')) >= 0) {
             fragment = string.substring(pos + 1);
             string = string.substring(0, pos);
         } else {
             fragment = null;
         }
 
         if ((pos = string.indexOf('?')) >= 0) {
             setQuery(string.substring(pos + 1));
             string = string.substring(0, pos);
         } else {
             setQuery(null);
         }
 
         pos = string.indexOf(':');
         scheme = string.substring(0, pos);
         string = string.substring(pos + 1);
 
         if (string.startsWith("//")) {
             string = string.substring(2);
         }
 
         if (string.equals(""))
             throw new MalformedUriException(string, "missing host or path");
 
         if (string.charAt(0) == '/') {
             /* file:/// */
             path = string;
         } else {
             if ((pos = string.indexOf('/')) >= 0) {
                 path = string.substring(pos);
                 string = string.substring(0, pos);
             } else {
                 path = "";
             }
 
             if ((pos = string.indexOf('@')) >= 0) {
                 userInfo = string.substring(0, pos);
                 string = string.substring(pos + 1);
             } else {
                 userInfo = null;
             }
 
             Endpoint endpoint = new Endpoint(string);
             host = endpoint.getHost();
             port = endpoint.getPort();
         }
     }
 
     public Url(Url url) {
         querySeparator = url.querySeparator;
         encoding = url.encoding;
         scheme = url.getScheme();
         userInfo = url.getUserInfo();
         host = url.getHost();
         port = url.getPort();
         path = url.getPath();
         setQuery(url.getQuery());
         fragment = url.getFragment();
     }
 
     public Url(String string, String encoding, String querySeparator) throws MalformedUriException {
         this.string = string;
         this.encoding = encoding;
         this.querySeparator = querySeparator;
         parse();
         if (host == null && path == null) {
             throw new MalformedUriException(string, "no host or path");
         }
     }
 
     public Url(String url, String encoding) throws MalformedUriException {
         this(url, encoding, "&");
     }
 
     public Url(String url) throws MalformedUriException {
         this(url, "detect", "&");
     }
 
     public static String repairURI(String str) {
         str = str.trim();
         if (!schemeValidationPattern.matcher(str).find()) {
             str = "http://" + str;
         } else {
             Matcher m = repairPattern.matcher(str);
             if (m.matches()) {
                 if (m.group(1).equals("file")) {
                     str = m.group(1) + ":///" + m.group(2);
                 } else {
                     str = m.group(1) + "://" + m.group(2);
                 }
             }
         }
 
         return str;
     }
 
     public String getScheme() {
         return scheme;
     }
 
     public void setScheme(String scheme) throws URISyntaxException {
         if (scheme == null) {
             throw new URISyntaxException(scheme, "empty/null scheme");
         }
         if (!schemeValidationPattern.matcher(scheme + ":").matches()) {
             throw new URISyntaxException(scheme, "invalid scheme");
         }
         this.scheme = scheme;
     }
 
     public String getUserInfo() {
         return userInfo;
     }
 
     public void setUserinfo(String userinfo) {
         this.userInfo = userinfo;
     }
 
     public Host getHost() {
         return host;
     }
 
     public Domain getDomain() {
         return host instanceof Domain ? (Domain) host : null;
     }
 
     public Record getRecord() {
         Domain domain = getDomain();
         return domain != null ? domain.getRecord() : null;
     }
 
     public Record getRegisteredRecord() {
         Domain domain = getDomain();
         return domain != null ? domain.getRegisteredRecord() : null;
     }
 
     public Record getSubleasedRecord() {
         Domain domain = getDomain();
         return domain != null ? domain.getSubleasedRecord() : null;
     }
 
     public String getDeprefixedHost() {
         Record record = getRegisteredRecord();
         if (record == null || record.getEntry().equalsIgnoreCase(host.getString())) {
             return host.getString();
         }
         Matcher m = deprefixHostPattern.matcher(host.getString());
         return m.replaceFirst("");
     }
 
     public void setHost(Host host) {
         this.host = host;
     }
 
     public void setHost(String host) throws MalformedDomainException {
        this.host = Host.get(string);
     }
 
     public Integer getPort() {
         return port;
     }
 
     public void setPort(Integer port) {
         this.port = port;
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         if (path.charAt(0) != '/') {
             this.path = "/" + path;
         } else {
             this.path = path;
         }
     }
 
     public LinkedList<Parameter> getParameters() {
         return parameters;
     }
 
     public void addParameter(Parameter p) {
         if (parameters == null) {
             parameters = new LinkedList<Parameter>();
         }
         parameters.add(p);
     }
 
     public void addParameter(String name, String value) {
         addParameter(new Parameter(name, value));
     }
 
     public void addParameter(String name) {
         addParameter(name, null);
     }
 
     public Parameter getParameter(String name) {
         if (parameters == null)
             return null;
 
         for (Parameter p : parameters) {
             if (p.getName().equals(name))
                 return new Parameter(p);
         }
 
         return null;
     }
 
     public LinkedList<Parameter> getParameters(String name) {
         LinkedList<Parameter> r = new LinkedList<Parameter>();
 
         if (parameters != null) {
             for (Parameter p : parameters) {
                 if (p.getName().equals(name))
                     r.add(new Parameter(p));
             }
         }
 
         return r;
     }
 
     public void removeParameter(String name) {
         if (parameters == null)
             return;
 
         Iterator<Parameter> i = parameters.iterator();
         while (i.hasNext()) {
             Parameter p = i.next();
             if (p.getName().equals(name))
                 i.remove();
         }
         if (parameters.isEmpty())
             parameters = null;
     }
 
     public void setParameter(String name, String value) {
         removeParameter(name);
         addParameter(new Parameter(name, value));
     }
 
     public String getQuerySeparator() {
         return querySeparator;
     }
 
     public void setQuerySeparator(String querySeparator) {
         if (querySeparator.equals(this.querySeparator))
             return;
         this.querySeparator = querySeparator;
         setQuery(getQuery());
     }
 
     public String getFragment() {
         return fragment;
     }
 
     public void setFragment(String fragment) {
         this.fragment = fragment;
     }
 
     private static boolean isHex(char ch) {
         return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
     }
 
     private static String fixUrlEncoding(String str) {
         //final String validChars = "^][!_$.,+*<>%\"\\'(){}|\\^~0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ;:-";
         int len = str.length();
         StringBuilder sb = new StringBuilder(len);
 
         for (int i = 0; i < len; i++) {
             char ch = str.charAt(i);
 
             if (ch == '%') {
                 if (i + 2 >= len || !isHex(str.charAt(i+1)) || !isHex(str.charAt(i+2))) {
                     sb.append("%25");
                     continue;
                 }
             }
             sb.append(ch);
         }
         return sb.toString();
     }
 
     public void setQuery(String query) {
         if (query == null) {
             this.parameters = null;
             return;
         }
 
         LinkedList<Parameter> parameters = new LinkedList<Parameter>();
 
         if (query.equals("")) {
             this.parameters = parameters;
             return;
         }
 
         query = fixUrlEncoding(query);
 
         String parts[] = query.split(Pattern.quote(querySeparator));
         for (String part : parts) {
             String v[] = part.split("=", 2);
 
             if (v.length == 1) {
                 parameters.add(new Parameter(v[0], null));
             } else {
                 try {
                     try {
                         parameters.add(new Parameter(v[0], URLDecoder.decode(v[1], encoding.equals("detect") ? "UTF-8" : encoding)));
                     } catch (IllegalArgumentException e) {
                         try {
                             parameters.add(new Parameter(v[0], URLDecoder.decode(v[1], "ISO-8859-1")));
                             encoding = "ISO-8859-1";
                         } catch (IllegalArgumentException no_perro) {
                             parameters.add(new Parameter(v[0], v[1]));
                         }
                     }
                 } catch (UnsupportedEncodingException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
 
         this.parameters = parameters;
     }
 
     public String getQuery() {
         if (parameters == null)
             return null;
 
         StringBuilder sb = new StringBuilder();
 
         for (Parameter p : parameters) {
             if (sb.length() != 0) {
                 sb.append(querySeparator);
             }
             sb.append(p.toString());
         }
 
         return sb.toString();
     }
 
     private static String resolveDotDot(String path) {
         int len = path.length();
         if (len <= 1) return path;
         return path.substring(0, path.lastIndexOf('/', len - 2) + 1);
     }
 
     public static String resolve(String path, String relPath) {
         int relPathPos = 0;
 
         if (relPath.equals("")) {
             return path;
         }
 
         if (relPath.startsWith("/")) {
             // absolute path, treat is as relative (to normalize "." and "..")
             path = "/";
             relPath = relPath.substring(1);
         }
 
         if (!path.endsWith("/")) {
             path = path.substring(0, path.lastIndexOf('/') + 1);
         }
 
         while (relPathPos < relPath.length()) {
             int next = relPath.indexOf('/', relPathPos);
             boolean slash = true;
 
             if (next == -1) {
                 next = relPath.length();
                 slash = false;
             }
 
             String part = relPath.substring(relPathPos, next);
 
             relPathPos = next + 1;
 
 
             if (part.equals(".")) {
                 continue;
             }
             if (part.equals("..")) {
                 path = resolveDotDot(path);
                 continue;
             }
 
             path = path + part + (slash ? "/" : "");
         }
 
         return path;
     }
 
     public Url resolve(String str) throws MalformedUriException {
     	if ("".equals(str)) {
     		return this;
     	}
 
     	if (schemeValidationPattern.matcher(str).find()) {
             return new Url(str, encoding, querySeparator);
         }
 
         if (str.startsWith("//")) {
             return new Url(scheme + ":" + str, encoding, querySeparator);
         }
 
         Url u = new Url(this);
 
         int pos;
 
         if ((pos = str.indexOf('#')) >= 0) {
             u.setFragment(str.substring(pos + 1));
             if (pos == 0) {
                 return u;
             }
             str = str.substring(0, pos);
         } else {
         	u.setFragment(null);
         }
 
         if ((pos = str.indexOf('?')) >= 0) {
             u.setQuery(str.substring(pos + 1));
             if (pos == 0) {
                 return u;
             }
             str = str.substring(0, pos);
         } else {
         	u.setQuery(null);
         }
 
         u.setPath(resolve(u.getPath(), str));
 
         return u;
     }
 
     public URI toURI() {
         try {
             return new URI(scheme, userInfo, host.getString(), (port == null ? -1 : port), path, getQuery(), fragment);
         } catch (URISyntaxException e) {
             return null;
         }
     }
 
     public String getLocalPath() {
         StringBuilder sb = new StringBuilder();
 
         if (path != null)
             sb.append(path);
         String query = getQuery();
         if (query != null) {
             sb.append('?');
             sb.append(query);
         }
         if (fragment != null) {
             sb.append('#');
             sb.append(fragment);
         }
 
         return sb.toString();
     }
 
     // XXX replace with getservbyname()... https://github.com/wmeissner/jnr-netdb ?
     private static HashMap<Integer, String> schemePorts = new HashMap<Integer, String>();
     static {
         schemePorts.put(21, "ftp");
         schemePorts.put(80, "http");
         schemePorts.put(443, "https");
     }
 
     public Url normalize() {
         Url url = new Url(this);
         try {
             url.setScheme(getScheme().toLowerCase());
             try {
                 url.setHost(getHost().getString().toLowerCase());
             } catch (MalformedDomainException e) {
                 // Highly unlikely
                 throw new RuntimeException(e);
             }
             String portScheme;
             if (url.getPort() != null && (portScheme = schemePorts.get(url.getPort())) != null && portScheme.equals(getScheme())) {
                 url.setPort(null);
             }
         } catch (URISyntaxException e) {
         }
 
         // XXX rencode path, qparam
 
         return url;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
 
         String host = this.host.getString();
 
         sb.append(scheme);
         sb.append("://");
 
         if (host != null) {
             if (userInfo != null) {
                 sb.append(userInfo);
                 sb.append('@');
             }
             boolean needBrackets = ((host.indexOf(':') >= 0)
                                 && !host.startsWith("[")
                                 && !host.endsWith("]"));
             if (needBrackets) {
                 sb.append('[');
             }
             sb.append(host);
             if (needBrackets) {
                 sb.append(']');
             }
             if (port != null) {
                 sb.append(':');
                 sb.append(port);
             }
         }
         if (path != null)
             sb.append(path);
         String query = getQuery();
         if (query != null) {
             sb.append('?');
             sb.append(query);
         }
         if (fragment != null) {
             sb.append('#');
             sb.append(fragment);
         }
 
         return sb.toString();
     }
 
 }
