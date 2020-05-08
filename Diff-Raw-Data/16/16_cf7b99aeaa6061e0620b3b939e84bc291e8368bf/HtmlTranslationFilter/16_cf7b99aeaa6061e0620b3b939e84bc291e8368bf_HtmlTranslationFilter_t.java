 /**
  *  Copyright 2011 Kurt Westerfeld
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
    
 package com.kawsoft.rewritehtml;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 import java.util.zip.DeflaterInputStream;
 import java.util.zip.DeflaterOutputStream;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 import org.mvel2.MVEL;
 
 import com.kawsoft.rewritehtml.config.BaseUriConstrainedFilter;
 import com.kawsoft.rewritehtml.config.ContentFilter;
 import com.kawsoft.rewritehtml.config.Replacement;
 import com.kawsoft.rewritehtml.config.RequestHeaderFilter;
 import com.kawsoft.rewritehtml.config.ResponseHeaderFilter;
 import com.kawsoft.rewritehtml.config.URIFilter;
 import com.kawsoft.rewritehtml.config.URIFilterType;
 
 
 public class HtmlTranslationFilter implements javax.servlet.Filter {
     private static final Logger log = Logger.getLogger(HtmlTranslationFilter.class.getName());
     private ConfigManager configManager;
     private Map<Replacement,Serializable> mvelExpressionCache = Collections.synchronizedMap(new WeakHashMap<Replacement, Serializable>());
 
     public HtmlTranslationFilter() {
         super();
     }
 
     public void init(FilterConfig filterConfig) throws ServletException {
         this.configManager = new ConfigManager(filterConfig);
     }
 
     public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
 
        // Get the uri for possible rewrite.
         HttpServletRequest httpReq = (HttpServletRequest) req;
        String uri = httpReq.getRequestURI();
        String uriOriginal = uri;
         if (log.isLoggable(Level.FINE)) {
            log.info("Processing request for URI: " + uri);
         }
         
         // Make URI replacements; if it was altered, forward it along.
         URIFilterType type = null;
         for (URIFilter uriFilter: this.configManager.getConfig().getUriFilters()) {
             if (uri.matches(uriFilter.getUriMatch())) {
                 uri = replace(
                         "uri",
                         uri, 
                         uriFilter.getReplacements(),
                         "request",
                         httpReq,
                         "session",
                         httpReq.getSession(false),
                         "response",
                         res
                         );
                 type = uriFilter.getType();
                 break;
             }
         }
        if (uri != uriOriginal && uri != null && !uriOriginal.equals(uri)) {
             
             // Translate 
             String translatedURI = null;
             String translatedQueryString = null;
             StringBuffer translatedURL = new StringBuffer();
             try {
                 if (log.isLoggable(Level.FINE)) {
                     log.fine("Replacement URI is returned as: " + uri);
                 }
                 URI translated = new URI(uri);
                 translatedURI = translated.getPath();
                 translatedQueryString = translated.getQuery();
                 
                 // Did they return a scheme and host for redirect?
                 if (translated.getScheme() != null && translated.getHost() != null) {
                     translatedURL.append(translated.toString());
                 }
                 else {
                     translatedURL.append(httpReq.getRequestURL().toString().replace(uriOriginal, translatedURI));
                     if (translatedQueryString != null && translatedQueryString.length() > 0) {
                         if (!translatedQueryString.startsWith("?")) {
                             translatedURL.append("?");
                         }
                         translatedURL.append(translatedQueryString);
                     }
                 }
                 if (log.isLoggable(Level.FINE)) {
                     log.fine(String.format("URI translation from: %s %s %s", httpReq.getRequestURI(), httpReq.getQueryString(), httpReq.getRequestURL().toString()));
                     log.fine(String.format("URI translation to: %s %s %s", translatedURI, translatedQueryString, translatedURL.toString()));
                 }
             } catch (URISyntaxException e) {
                 throw new ServletException("Unexpected exception translating URI: " + uri, e);
             }
             
             if (log.isLoggable(Level.FINE)) {
                log.fine("Performing uri rewrite dispatch by " + type);
             }
             if (type == URIFilterType.Forward) {
                 req.getRequestDispatcher(translatedURI).forward(req, res);
             } else  if (type == URIFilterType.Redirect) {
                 ((HttpServletResponse) res).sendRedirect(translatedURL.toString());
             } else {
                 // Do the filtering/translation
                 ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletRequest) req, (HttpServletResponse) res);
                 filterChain.doFilter(new RequestWrapper(httpReq, translatedURL, translatedURI, translatedQueryString), responseWrapper);
                 responseWrapper.commit();
             }
         } else {
             // Do the filtering/translation
             if (log.isLoggable(Level.FINE)) {
                 log.fine("Doing filtering");
             }
             ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletRequest) req, (HttpServletResponse) res);
             filterChain.doFilter(new RequestWrapper(httpReq, null, null, null), responseWrapper);
             responseWrapper.commit();
         }
     }
 
     public void destroy() {
     }
     
     private String replace(String what, String value, List<Replacement> replacements, Object...args) {
         String originalValue = value;
         for (Replacement replacement: replacements) {
             switch (replacement.getType()) {
             case ReplaceAll:
                 value = value.replace(replacement.getFrom(), replacement.getEffectiveTo());
                 break;
             case ReplaceFirst:
                 value = value.replaceFirst(Pattern.quote(replacement.getFrom()), replacement.getEffectiveTo());
                 break;
             case ReplaceAllRegex:
                 value = value.replaceAll(replacement.getFrom(), replacement.getEffectiveTo());
                 break;
             case ReplaceFirstRegex:
                 value = value.replaceFirst(replacement.getFrom(), replacement.getEffectiveTo());
                 break;
             case MVEL: 
                 try {
                     Map<String,Object> vars = new HashMap<String,Object>();
                     for (int i = 0; i < args.length; i+= 2) {
                         vars.put((String) args[i], args[i+1]);
                     }
                     vars.put("value", value);
                     Serializable expression = mvelExpressionCache.get(replacement);
                     if (expression == null) {
                         mvelExpressionCache.put(replacement, expression = MVEL.compileExpression(replacement.getEffectiveTo()));
                     }
                     if (log.isLoggable(Level.FINE)) {
                         log.fine("Evaluating " + value + " for replacement using expression " + replacement.getEffectiveTo() + " using vars: " + vars);
                     }
                     Object rvalue = MVEL.executeExpression(expression, vars);
                     if (rvalue == null) {
                         return null;
                     }
                     value = rvalue.toString();
                 } catch (Exception e) {
                     log.log(Level.WARNING, "Unexpected exception occurred while evaluating replacement expression: " + replacement.getEffectiveTo(), e);
                 }
  
                 break;
             }
         }
         
         if (!originalValue.equals(value)) {
             if (log.isLoggable(Level.FINEST)) {
                 log.finest(String.format("Performed replacement of %s from %s to %s", what, originalValue, value));
             }
         }
         
         return value;
     }
 
     private class RequestWrapper extends HttpServletRequestWrapper {
 
         private final StringBuffer requestURL;
         private final String requestURI;
         private final String queryString;
 
         public RequestWrapper(HttpServletRequest request, StringBuffer requestURL, String requestURI, String queryString) {
             super(request);
             this.requestURL = requestURL;
             this.requestURI = requestURI;
             this.queryString = queryString;
         }
         
         @Override
         public String getQueryString() {
             if (this.queryString != null) {
                 return this.queryString;
             } else {
                 return super.getQueryString();
             }
         }
 
         @Override
         public String getRequestURI() {
             if (this.requestURI != null) {
                 return this.requestURI;
             } else {
                 return super.getRequestURI();
             }
         }
 
         @Override
         public StringBuffer getRequestURL() {
             if (this.requestURL != null) {
                 return this.requestURL;
             } else {
                 return super.getRequestURL();
             }
         }
 
         @Override
         public String getHeader(String name) {
             return processHeader(name, super.getHeader(name));
         }
 
         @SuppressWarnings("rawtypes")
         @Override
         public Enumeration getHeaders(final String name) {
             final Enumeration delegate = super.getHeaders(name);
             return new Enumeration() {
 
                 @Override
                 public boolean hasMoreElements() {
                     return delegate.hasMoreElements();
                 }
 
                 @Override
                 public Object nextElement() {
                     return processHeader(name, (String) delegate.nextElement());
                 }
             };
         }
         
         private String processHeader(String name, String value) {
             if (value == null) {
                 return null;
             }
             for (RequestHeaderFilter requestHeaderFilter: configManager.getConfig().getRequestHeaderFilters()) {
                 if (name.equals(requestHeaderFilter.getHeader())) {
                     String originalValue = value;
                     value = replace(
                             "request header '" + name + "'",
                             value, 
                             requestHeaderFilter.getReplacements(),
                             "header",
                             name,
                             "request", 
                             getRequest(), 
                             "session", 
                             ((HttpServletRequest)getRequest()).getSession(false));
                     if (log.isLoggable(Level.FINE)) {
                         log.fine("Returning updated request header " + name + " translated from " + originalValue + " to " + value + " for " + getRequestURI());
                     }
                 }
             }
             return value;
         }
     }
 
     private class ResponseWrapper extends HttpServletResponseWrapper {
         private WrappedServletOutputStream stream;
         private boolean isFiltered;
         private boolean isWriter;
         private boolean isGzip;
         private boolean isDeflated;
         private BaseUriConstrainedFilter contentFilter;
         private String charset;
         private HttpServletRequest request;
 
         public ResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
             super(response);
             this.request = request;
         }
 
         public void commit() throws IOException {
             if (this.isFiltered) {
                 if (log.isLoggable(Level.FINE)) {
                     log.fine("Filtering content for: " + this.request.getRequestURI());
                 }
                 String content = replace(
                         "content",
                         getAsString(), 
                         this.contentFilter.getReplacements(),
                         "request",
                         request,
                         "session",
                         request.getSession(false),
                         "response",
                         getResponse()
                         );
 
                 if (this.isGzip || this.isDeflated) {
                     if (log.isLoggable(Level.FINE)) {
                         log.fine("Returning filtered, compressed content for: " + this.request.getRequestURI());
                     }
                     ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     OutputStream wrapped = this.isGzip ? new GZIPOutputStream(bos) : new DeflaterOutputStream(bos);
                     wrapped.write(this.charset != null ? content.getBytes(Charset.forName(this.charset)) : content.getBytes());
                     wrapped.close();
 
                     byte[] data = bos.toByteArray();
                     this.getResponse().setContentLength(data.length);
                     ServletOutputStream out = this.getResponse().getOutputStream();
                     out.write(data);
                     try {
                         out.close();
                     } catch (Exception e) {
                         log.log(Level.WARNING, "Unexpected exception occurred during close", e);
                     }
                 } else {
 
                     if (log.isLoggable(Level.FINE)) {
                         log.fine("Returning filtered content for: " + this.request.getRequestURI());
                     }
                     this.getResponse().setContentLength(content.length());
                     PrintWriter out = this.getResponse().getWriter();
                     out.write(content);
                     try {
                         out.close();
                     } catch (Exception e) {
                         log.log(Level.WARNING, "Unexpected exception occurred during close", e);
                     }
                 }
             } else if (this.stream != null) {
                 if (log.isLoggable(Level.FINE)) {
                     log.fine("Returning unmodified content for: " + this.request.getRequestURI());
                 }
                 byte[] data = this.stream.toByteArray();
                 this.getResponse().setContentLength(data.length);
                 ServletOutputStream out = this.getResponse().getOutputStream();
                 out.write(data);
                 try {
                     out.close();
                 } catch (Exception e) {
                     log.log(Level.WARNING, "Unexpected exception occurred during close", e);
                 }
             }
         }
 
         @Override
         public void setHeader(String name, String value) {
             String originalValue = value;
             value = processHeader(name, value);
             if (value != null && value.trim().length() == 0) {
                 if (log.isLoggable(Level.FINE)) {
                     log.fine("Dropping empty response header " + name + " translated from " + originalValue + " for " + this.request.getRequestURI());
                 }
                 return;
             }
             super.setHeader(name, value);
         }
 
         @Override
         public void addHeader(String name, String value) {
             String originalValue = value;
             value = processHeader(name, value);
             if (value != null && value.trim().length() == 0) {
                 if (log.isLoggable(Level.FINE)) {
                     log.fine("Dropping empty response header " + name + " translated from " + originalValue + " for " + this.request.getRequestURI());
                 }
                 return;
             }
             super.addHeader(name, value);
         }
 
         private String processHeader(String name, String value) {
             if (name.equals("Content-Type")) {
                 
                 String contentType = value.replaceAll("([^;]*).*", "$1");
                 if (value.contains("charset")) {
                     this.charset = value.replaceAll(".*charset\\s*=\\s*([^;]*).*", "$1");
                 }
 
                 for (ContentFilter contentFilter : configManager.getConfig().getContentFilters()) {
                     if (contentType.matches(contentFilter.getMimeTypeMatch()) &&
                         request.getRequestURI().matches(contentFilter.getUriMatch())) {
                         isFiltered = true;
                         this.contentFilter = contentFilter;
                         if (log.isLoggable(Level.FINE)) {
                             log.fine("Configured to filter (" + contentType + "/" + charset + ") content for: " + this.request.getRequestURI());
                         }
                         break;
                     }
                 }
             } else if (name.equals("Content-Encoding") && value.toLowerCase().contains("gzip")) {
                 isGzip = true;
             } else if (name.equals("Content-Encoding") && value.toLowerCase().contains("deflate")) {
                 isDeflated = true;
             } else {
                 for (ResponseHeaderFilter responseHeaderFilter: configManager.getConfig().getResponseHeaderFilters()) {
                     if (name.equals(responseHeaderFilter.getHeader())) {
                         String originalValue = value;
                         value = replace(
                                 "response header '" + name + "'",
                                 value, 
                                 responseHeaderFilter.getReplacements(),
                                 "header",
                                 name,
                                 "request",
                                 request,
                                 "session",
                                 request.getSession(false),
                                 "response",
                                 getResponse());
                         if (log.isLoggable(Level.FINE)) {
                             log.fine("Returning updated response header " + name + " translated from " + originalValue + " to " + value + " for " + this.request.getRequestURI());
                         }
                     }
                 }
             }
 
             return value;
         }
 
         @Override
         public ServletOutputStream getOutputStream() throws IOException {
             // TODO: we might be able optimize if we can assume header has been set for content-type.  This 
             //       would allow us to pass-through for non-filtered content.
             if (this.isWriter) {
                 throw new IllegalStateException("Cannot call getOutputStream() if getWriter() has been called"); // Per servlet spec!
             }
             if (this.stream == null) {
                 this.stream = new WrappedServletOutputStream();
             }
             return this.stream;
         }
 
         @Override
         public PrintWriter getWriter() throws IOException {
             if (this.stream != null) {
                 throw new IllegalStateException("Cannot call getWriter() if getOutputStream() has been called"); // Per servlet spec!
             }
             isWriter = true;
             return super.getWriter();
         }
 
         public String getAsString() {
             if (this.stream == null) {
                 throw new IllegalStateException("Cannot get string content without delegate");
             }
 
             // Handle gzip/deflation.
             byte[] data = this.stream.toByteArray();
             if (data.length > 0) {
                 try {
                     if (isGzip) {
                         
                         // Double check gzip magic # here.
                         int header_magic = ((int) data[0] & 0xff) | ((data[1] << 8) & 0xff00);
                         if (header_magic == GZIPInputStream.GZIP_MAGIC) {
                             data = capture(new GZIPInputStream(new ByteArrayInputStream(data)));
                         }
                     } else if (isDeflated) {
                         data = capture(new DeflaterInputStream(new ByteArrayInputStream(data)));
                     }
                 } catch (IOException e) {
                     throw new RuntimeException("Unexpected i/o exception occurred unwrapping compressed content", e);
                 }
             }
 
             return this.charset != null ? new String(data, Charset.forName(this.charset)) : new String(data);
         }
 
         private byte[] capture(InputStream ins) throws IOException {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             byte[] buffer = new byte[16384];
             while (true) {
                 int len = ins.read(buffer);
                 if (len <= 0) {
                     break;
                 }
                 bos.write(buffer, 0, len);
             }
             ins.close();
             bos.close();
             return bos.toByteArray();
         }
     }
 
     private static class WrappedServletOutputStream extends ServletOutputStream {
 
         private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();
 
         @Override
         public void write(int b) throws IOException {
             this.delegate.write(b);
         }
 
         public byte[] toByteArray() {
             return this.delegate.toByteArray();
         }
     }
 }
 
