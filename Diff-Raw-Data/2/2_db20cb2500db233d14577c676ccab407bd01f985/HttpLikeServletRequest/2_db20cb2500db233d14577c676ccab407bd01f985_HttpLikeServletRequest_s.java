 package com.jrodeo.remote;
 
 import com.jrodeo.restlike.RestlikeServlet;
 import com.jrodeo.util.memory.ReadableByteChannelSource;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.SocketChannel;
 import java.security.Principal;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: brad_hlista
  */
 
 public class HttpLikeServletRequest implements HttpServletRequest {
 
     HttpLikeSession session;
     RestlikeServlet servlet;
 
     String requestMethod;
     String urlString;
 
     public static ReadableByteChannel getReadableByteChannel(HttpServletRequest request) {
         return ((HttpLikeServletRequest) request).session.sc;
     }
 
     public static ByteBuffer getByteBuffer(HttpServletRequest request) {
         HttpLikeServletRequest req = (HttpLikeServletRequest) request;
         return req.session.bb;
     }
 
     public static void read(HttpServletRequest request, ByteBuffer bb) throws IOException {
         HttpLikeServletRequest req = (HttpLikeServletRequest) request;
         req.session.read(bb);
     }
 
     public static void turnOffReads(HttpServletRequest request) {
         ((HttpLikeServletRequest) request).session.turnOffReads();
     }
 
     public static void scheduleRequestReader(HttpServletRequest request, RequestReader requestReader) {
         HttpLikeServletRequest req = (HttpLikeServletRequest) request;
         req.session.scheduleRequestReader(requestReader);
     }
 
     public static void setReader(HttpServletRequest request, RequestReader reader) {
         HttpLikeServletRequest req = (HttpLikeServletRequest) request;
         req.session.setRequestReader(reader);
     }
 
     public static HttpLikeSession getHttpLikeSession(HttpServletRequest request) {
         HttpLikeServletRequest req = (HttpLikeServletRequest) request;
         return req.session;
     }
 
     public static void complete(HttpServletRequest request) {
         HttpLikeServletRequest req = (HttpLikeServletRequest) request;
         req.session.setReadComplete();
     }
 
     public static ReadableByteChannelSource getReadableByteChannelSource(final HttpServletRequest request) {
 
         final ReadableByteChannel rbc = getReadableByteChannel(request);
 
         final ReadableByteChannelSource rbcs = new ReadableByteChannelSource() {
             ByteBuffer bb = HttpLikeServletRequest.getByteBuffer(request);
 
             public long transferFrom(FileChannel fileChannel, long position, long count) throws IOException {
                 long n = 0;
                 if (bb.hasRemaining()) {
                     n = fileChannel.write(bb);
                     if (bb.hasRemaining()) {
                         return n;
                     }
                 }
 
                 n += fileChannel.transferFrom(rbc, position, count);
                 return n;
             }
         };
 
         return rbcs;
     }
 
 
     public HttpLikeServletRequest(HttpLikeSession session, RestlikeServlet servlet, String requestMethod, String urlString) {
         this.session = session;
         this.servlet = servlet;
         this.requestMethod = requestMethod;
         this.urlString = urlString;
     }
 
     public void setRequestMethod(String requestMethod) {
         this.requestMethod = requestMethod;
     }
 
     public String getAuthType() {
         return null;
     }
 
     public Cookie[] getCookies() {
         return new Cookie[0];
     }
 
     public long getDateHeader(String s) {
         String value = session.headers.get(s);
         if (value != null) {
             try {
                 return RFC1123Date.fromString(value).date.getTime();
             } catch (Exception e) {
                 throw new IllegalArgumentException(e);
             }
         }
         return -1;
     }
 
     public String getHeader(String s) {
         return session.headers.get(s);
     }
 
     public Enumeration<String> getHeaders(String s) {
         return Collections.enumeration(session.headers.values());
     }
 
     public Enumeration<String> getHeaderNames() {
         return Collections.enumeration(session.headers.keySet());
     }
 
     public int getIntHeader(String s) {
         String value = session.headers.get(s);
         if (value != null) {
             return Integer.parseInt(value);
         }
         return -1;
     }
 
     public String getMethod() {
         return requestMethod;
     }
 
     public String getPathInfo() {
         return servlet.resolvePathInfo(urlString);
     }
 
     public String getPathTranslated() {
         return null;
     }
 
     public String getContextPath() {
         return servlet.resolveContextPath(urlString);
     }
 
     public String getQueryString() {
         int i = urlString.lastIndexOf("?");
         if (i > 0) {
             return urlString.substring(i);
         }
         return null;
     }
 
     public String getRemoteUser() {
         return null;
     }
 
     public boolean isUserInRole(String s) {
         return false;
     }
 
     public Principal getUserPrincipal() {
         return null;
     }
 
     public String getRequestedSessionId() {
         return null;
     }
 
     public String getRequestURI() {
         return servlet.resolveURI(urlString);
     }
 
     public StringBuffer getRequestURL() {
         StringBuffer sb = new StringBuffer();
         sb.append("http://");
         sb.append(getServerName());
         sb.append(":");
         sb.append(getServerPort());
         sb.append(urlString);
         return sb;
     }
 
     public String getServletPath() {
         return servlet.resolveContextPath(urlString);
     }
 
     public HttpSession getSession(boolean b) {
         return null;
     }
 
     public HttpSession getSession() {
         return null;
     }
 
     public boolean isRequestedSessionIdValid() {
         return false;
     }
 
     public boolean isRequestedSessionIdFromCookie() {
         return false;
     }
 
     public boolean isRequestedSessionIdFromURL() {
         return false;
     }
 
     public boolean isRequestedSessionIdFromUrl() {
         return false;
     }
 
     public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
         return false;
     }
 
     public void login(String s, String s1) throws ServletException {
 
     }
 
     public void logout() throws ServletException {
 
     }
 
     public Collection<Part> getParts() throws IOException, ServletException {
         return null;
     }
 
     public Part getPart(String s) throws IOException, ServletException {
         return null;
     }
 
     public Object getAttribute(String s) {
         return null;
     }
 
     public Enumeration<String> getAttributeNames() {
         return null;
     }
 
     public String getCharacterEncoding() {
         return null;
     }
 
     public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
 
     }
 
     public int getContentLength() {
         return this.getIntHeader("Content-Length");
     }
 
     public String getContentType() {
         return this.getHeader("Content-Type");
     }
 
     HttpLikeServletInputStream inputStream = null;
 
     public ServletInputStream getInputStream() throws IOException {
         if (inputStream == null) {
             inputStream = new HttpLikeServletInputStream();
             inputStream.session = session;
         }
         return inputStream;
     }
 
     public String getParameter(String s) {
         return null;
     }
 
     public Enumeration<String> getParameterNames() {
         return null;
     }
 
     public String[] getParameterValues(String s) {
         return new String[0];
     }
 
     public Map<String, String[]> getParameterMap() {
         return null;
     }
 
     public String getProtocol() {
         return null;
     }
 
     public String getScheme() {
         return null;
     }
 
     public String getServerName() {
         SocketChannel socketChannel = (SocketChannel) session.clientSelectionKey.channel();
         return socketChannel.socket().getLocalAddress().getCanonicalHostName();
     }
 
     public int getServerPort() {
         SocketChannel socketChannel = (SocketChannel) session.clientSelectionKey.channel();
         return socketChannel.socket().getLocalPort();
     }
 
     public BufferedReader getReader() throws IOException {
         return null;
     }
 
     public String getRemoteAddr() {
         SocketChannel socketChannel = (SocketChannel) session.clientSelectionKey.channel();
         return socketChannel.socket().getInetAddress().getHostAddress();
     }
 
     public String getRemoteHost() {
         SocketChannel socketChannel = (SocketChannel) session.clientSelectionKey.channel();
         return socketChannel.socket().getInetAddress().getCanonicalHostName();
     }
 
     public void setAttribute(String s, Object o) {
 
     }
 
     public void removeAttribute(String s) {
 
     }
 
     public Locale getLocale() {
         return null;
     }
 
     public Enumeration<Locale> getLocales() {
         return null;
     }
 
     public boolean isSecure() {
         return false;
     }
 
     public RequestDispatcher getRequestDispatcher(String s) {
         return null;
     }
 
     public String getRealPath(String s) {
         return null;
     }
 
     public int getRemotePort() {
         SocketChannel socketChannel = (SocketChannel) session.clientSelectionKey.channel();
         return socketChannel.socket().getPort();
     }
 
     public String getLocalName() {
         SocketChannel socketChannel = (SocketChannel) session.clientSelectionKey.channel();
         return socketChannel.socket().getLocalAddress().getHostName();
     }
 
     public String getLocalAddr() {
         SocketChannel socketChannel = (SocketChannel) session.clientSelectionKey.channel();
         return socketChannel.socket().getLocalAddress().getHostAddress();
     }
 
     public int getLocalPort() {
         SocketChannel socketChannel = (SocketChannel) session.clientSelectionKey.channel();
         return socketChannel.socket().getLocalPort();
     }
 
     public ServletContext getServletContext() {
         return null;
     }
 
     public AsyncContext startAsync() throws IllegalStateException {
         return null;
     }
 
     public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
         return null;
     }
 
     public boolean isAsyncStarted() {
         return false;
     }
 
     public boolean isAsyncSupported() {
         return false;
     }
 
     public AsyncContext getAsyncContext() {
         return null;
     }
 
     public DispatcherType getDispatcherType() {
         return null;
     }
 }
