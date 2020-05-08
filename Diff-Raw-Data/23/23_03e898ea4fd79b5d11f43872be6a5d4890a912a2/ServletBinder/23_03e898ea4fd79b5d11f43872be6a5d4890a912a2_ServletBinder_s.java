 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.servlet;
 
 import cx.ath.mancel01.webframework.http.Cookie;
 import cx.ath.mancel01.webframework.http.Header;
 import cx.ath.mancel01.webframework.http.Request;
 import cx.ath.mancel01.webframework.http.Response;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mathieuancelin
  */
 public class ServletBinder {
 
    static Request parseRequest(HttpServletRequest httpServletRequest) throws Exception {
         Request request = new Request();
         URI uri = new URI(httpServletRequest.getRequestURI());
         request.method = httpServletRequest.getMethod().intern();
         request.path = uri.getPath();
         request.querystring = httpServletRequest.getQueryString() == null ? "" : httpServletRequest.getQueryString();
         if (httpServletRequest.getHeader("Content-Type") != null) {
             request.contentType = httpServletRequest.getHeader("Content-Type").split(";")[0].trim().toLowerCase().intern();
         } else {
             request.contentType = "text/html".intern();
         }
 
         if (httpServletRequest.getHeader("X-HTTP-Method-Override") != null) {
             request.method = httpServletRequest.getHeader("X-HTTP-Method-Override").intern();
         }
 
         request.body = httpServletRequest.getInputStream();
         request.secure = httpServletRequest.isSecure();
 
         request.url = uri.toString() + (httpServletRequest.getQueryString() == null ? "" : "?" + httpServletRequest.getQueryString());
         request.host = httpServletRequest.getHeader("host");
         if (request.host.contains(":")) {
             request.port = Integer.parseInt(request.host.split(":")[1]);
             request.domain = request.host.split(":")[0];
         } else {
             request.port = 80;
             request.domain = request.host;
         }
         request.remoteAddress = httpServletRequest.getRemoteAddr();
         Enumeration headersNames = httpServletRequest.getHeaderNames();
         while (headersNames.hasMoreElements()) {
             Header hd = new Header();
             hd.name = (String) headersNames.nextElement();
             hd.values = new ArrayList<String>();
             Enumeration enumValues = httpServletRequest.getHeaders(hd.name);
             while (enumValues.hasMoreElements()) {
                 String value = (String) enumValues.nextElement();
                 hd.values.add(value);
             }
             request.headers.put(hd.name.toLowerCase(), hd);
         }
 
         javax.servlet.http.Cookie[] cookies = httpServletRequest.getCookies();
         if (cookies != null) {
             for (javax.servlet.http.Cookie cookie : cookies) {
                 Cookie playCookie = new Cookie();
                 playCookie.name = cookie.getName();
                 playCookie.path = cookie.getPath();
                 playCookie.domain = cookie.getDomain();
                 playCookie.secure = cookie.getSecure();
                 playCookie.value = cookie.getValue();
                 playCookie.maxAge = cookie.getMaxAge();
                 request.cookies.put(playCookie.name, playCookie);
             }
         }
         return request;
     }
 
     static void flushResponse(Request request, Response response, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
         if (response.contentType != null) {
             servletResponse.setHeader("Content-Type", response.contentType
                     + (response.contentType.startsWith("text/") ? "; charset=utf-8" : ""));
         } else {
             servletResponse.setHeader("Content-Type", "text/plain;charset=utf-8");
         }
 
         servletResponse.setStatus(response.status);
         if (!response.headers.containsKey("cache-control")) {
             servletResponse.setHeader("Cache-Control", "no-cache");
         }
         Map<String, Header> headers = response.headers;
         for (Map.Entry<String, Header> entry : headers.entrySet()) {
             Header hd = entry.getValue();
             String key = entry.getKey();
             for (String value : hd.values) {
                 servletResponse.setHeader(key, value);
             }
         }
 
         Map<String, Cookie> cookies = response.cookies;
         for (Cookie cookie : cookies.values()) {
             javax.servlet.http.Cookie c = new javax.servlet.http.Cookie(cookie.name, cookie.value);
             c.setSecure(cookie.secure);
             c.setPath(cookie.path);
             if (cookie.domain != null) {
                 c.setDomain(cookie.domain);
             }
             if (cookie.maxAge != null) {
                 c.setMaxAge(cookie.maxAge);
             }
             servletResponse.addCookie(c);
         }
         response.out.flush();
         if (response.direct != null && response.direct instanceof File) {
             File file = (File) response.direct;
             servletResponse.setHeader("Content-Length", String.valueOf(file.length()));
             if (!request.method.equals("HEAD")) {
                 copyStream(servletResponse, new FileInputStream(file));
             } else {
                 copyStream(servletResponse, new ByteArrayInputStream(new byte[0]));
             }
         } else if (response.direct != null && response.direct instanceof InputStream) {
             copyStream(servletResponse, (InputStream) response.direct);
         } else {
             byte[] content = response.out.toByteArray();
             servletResponse.setHeader("Content-Length", String.valueOf(content.length));
             if (!request.method.equals("HEAD")) {
                 servletResponse.getOutputStream().write(content);
             } else {
                 copyStream(servletResponse, new ByteArrayInputStream(new byte[0]));
             }
         }
     }
 
     private static void copyStream(HttpServletResponse servletResponse, InputStream is) throws IOException {
         OutputStream os = servletResponse.getOutputStream();
         byte[] buffer = new byte[8096];
         int read = 0;
         while ((read = is.read(buffer)) > 0) {
             os.write(buffer, 0, read);
         }
         os.flush();
         is.close();
     }
 }
