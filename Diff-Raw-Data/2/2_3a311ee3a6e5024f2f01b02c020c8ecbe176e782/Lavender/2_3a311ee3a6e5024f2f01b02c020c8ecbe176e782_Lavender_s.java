 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.lavender.filter;
 
 import net.oneandone.lavender.config.Settings;
 import net.oneandone.lavender.filter.processor.ProcessorFactory;
 import net.oneandone.lavender.filter.processor.RewriteEngine;
 import net.oneandone.lavender.index.Hex;
 import net.oneandone.lavender.index.Index;
 import net.oneandone.lavender.modules.DefaultModule;
 import net.oneandone.lavender.modules.Module;
 import net.oneandone.lavender.modules.Resource;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.World;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.URI;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 /**
  * A servlet filter that <em>lavendelizes</em> the response content.
  */
 public class Lavender implements Filter, LavenderMBean {
     private static final Logger LOG = LoggerFactory.getLogger(Lavender.class);
 
     public static final String LAVENDEL_IDX = "WEB-INF/lavender.idx";
     public static final String LAVENDEL_NODES = "WEB-INF/lavender.nodes";
 
 
     private World world;
 
     /** The filter configuration. */
     protected FilterConfig filterConfig;
 
     protected ProcessorFactory processorFactory;
 
     protected List<Module> develModules;
 
     @Override
     public void init(FilterConfig config) throws ServletException {
         long started;
         Node src;
         Index index;
         RewriteEngine rewriteEngine;
         Settings settings;
         Node webapp;
 
         try {
             LOG.info("init");
             filterConfig = config;
             world = new World();
             webapp = world.file(filterConfig.getServletContext().getRealPath(""));
             src = webapp.join(LAVENDEL_IDX);
             if (src.exists()) {
                 index = Index.load(src);
                 rewriteEngine = RewriteEngine.load(index, webapp.join(LAVENDEL_NODES));
                 processorFactory = new ProcessorFactory(rewriteEngine);
                 LOG.info("Lavender prod filter");
             } else {
                 started = System.currentTimeMillis();
                 settings = Settings.load(world);
                 processorFactory = null;
                 develModules = DefaultModule.fromWebapp(false, webapp, settings.svnUsername, settings.svnPassword);
                 LOG.info("Lavender devel filter for " + webapp + ", " + develModules.size()
                         + " resources. Init in " + (System.currentTimeMillis() - started + " ms"));
             }
         } catch (IOException ie) {
             LOG.error("Error in Lavendelizer.init()", ie);
             throw new ServletException("io error", ie);
         } catch (RuntimeException se) {
             LOG.error("Error in Lavendelizer.init()", se);
             throw se;
         }
 
         try {
             ManagementFactory.getPlatformMBeanServer().registerMBean(this,
                     new ObjectName("com.oneandone:type=Lavender,name=" + UUID.randomUUID().toString()));
         } catch (InstanceAlreadyExistsException | NotCompliantMBeanException | MalformedObjectNameException e) {
             throw new IllegalStateException(e);
         } catch (MBeanRegistrationException e) {
             LOG.error("MBean initialization failure", e);
         }
     }
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         if (processorFactory == null) {
             doDevelFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
         } else {
             doProdFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
         }
     }
 
     public void doDevelFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
             throws IOException, ServletException {
         if (!develIntercept(request, response)) {
             chain.doFilter(request, response);
             LOG.debug("[passed through: " + request.getMethod() + " " + request.getRequestURI() + ": " + response.getStatus() + "]");
         }
     }
 
     public boolean develIntercept(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         String path;
         Resource resource;
 
         path = request.getPathInfo();
         if (!path.startsWith("/")) {
             return false;
         }
         path = path.substring(1);
         resource = develLookup(path);
         if (resource == null) {
             return false;
         }
 
         switch (request.getMethod()) {
             case "GET":
                 develGet(resource, request, response, true);
                 LOG.info(response.getStatus() + " GET " + path + " -> " + resource.getOrigin());
                 return true;
             case "HEAD":
                 develGet(resource, request, response, false);
                 LOG.info(response.getStatus() + " HEAD " + path + " -> " + resource.getOrigin());
                 return true;
             default:
                 return false;
         }
     }
 
    private Resource develLookup(String resourcePath) throws IOException {
         Resource resource;
 
         // lookup cached stuff first
         for (Module module : develModules) {
             if (module.hasFiles()) {
                 resource = module.probe(resourcePath);
                 if (resource != null) {
                     if (!resource.isOutdated()) {
                         return resource;
                     } else {
                         LOG.info(resource.getOrigin() + ": outdated");
                     }
                 }
             }
         }
         for (Module module : develModules) {
             if (module.matches(resourcePath) != null) {
                 module.invalidate();
                 resource = module.probe(resourcePath);
                 if (resource != null) {
                     return resource;
                 }
             }
         }
         return null;
     }
 
     public boolean getProd() {
         return processorFactory != null;
     }
 
     public int getModules() {
         return develModules != null ? -1 : develModules.size();
     }
 
     public void doProdFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
             throws IOException, ServletException {
 
         StringBuffer url;
         LavendelizeHttpServletRequest lavenderRequest;
         LavendelizeHttpServletResponse lavenderResponse;
 
         try {
             url = request.getRequestURL();
             URI requestURI = URI.create(url.toString());
 
             // use custom request and response objects
             lavenderRequest = new LavendelizeHttpServletRequest(request);
             lavenderResponse = new LavendelizeHttpServletResponse(response, processorFactory,
                     requestURI, request.getHeader("User-Agent"), request.getContextPath() + "/", Gzip.canGzip(request));
             logRequest(url, request);
         } catch (RuntimeException re) {
             LOG.error("Error in Lavendelizer.doFilter()", re);
             throw re;
         }
 
         // continue the request
         // No exception handling at this point. Exceptions in processors are handled in LavendelizeOutputStream/Writer
         chain.doFilter(lavenderRequest, lavenderResponse);
 
         try {
             // close the response to make sure all buffers are flushed
             lavenderResponse.close();
 
             logResponse(url, lavenderResponse);
         } catch (IOException | RuntimeException e) {
             LOG.error("Error in Lavendelizer.doFilter()", e);
             throw e;
         }
     }
 
     private void logRequest(StringBuffer url, HttpServletRequest httpRequest) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Entering doFilter: url=" + url);
         }
         if (LOG.isTraceEnabled()) {
             LOG.trace("  Request headers: ");
             Enumeration<String> headerNames = httpRequest.getHeaderNames();
             while (headerNames.hasMoreElements()) {
                 String key = headerNames.nextElement();
                 String value = httpRequest.getHeader(key);
                 LOG.trace("    " + key + ": " + value);
             }
         }
     }
 
     private void logResponse(StringBuffer url, LavendelizeHttpServletResponse lavendelResponse) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Leaving doFilter:  url=" + url);
         }
         if (LOG.isTraceEnabled()) {
             LOG.trace("  Response headers: ");
             for (Entry<String, String> entry : lavendelResponse.getHeaders().entrySet()) {
                 LOG.trace("    " + entry.getKey() + ": " + entry.getValue());
             }
         }
     }
 
     @Override
     public void destroy() {
         if (develModules != null) {
             for (Module module : develModules) {
                 try {
                     module.saveCaches();
                 } catch (IOException e) {
                     LOG.error("cannot save caches for " + module.getName() + ": " + e.getMessage(), e);
                 }
             }
         }
     }
 
     //--
 
     public void develGet(Resource resource, HttpServletRequest request, HttpServletResponse response, boolean withBody) throws IOException {
         String etag;
         String contentType;
         long contentLength;
         ServletOutputStream out;
         byte[] data;
         String previousEtag;
 
         etag = Hex.encodeString(resource.getMd5());
         response.setDateHeader("Last-Modified", resource.getLastModified());
         response.setHeader("ETag", etag);
         contentType = filterConfig.getServletContext().getMimeType(resource.getPath());
         if (contentType != null) {
             response.setContentType(contentType);
         }
 
         previousEtag = request.getHeader("If-None-Match");
         if (etag.equals(previousEtag)) {
             LOG.debug("ETag match: returning 304 Not Modified: " + resource.getPath());
             response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
         } else  { 		// first time through - set last modified time to now
             data = resource.getData();
             contentLength = data.length;
             if (contentLength >= Integer.MAX_VALUE) {
                 throw new IOException(resource.getPath() + ": resource too big: " + contentLength);
             }
             if (withBody) {
                 response.setContentLength((int) contentLength);
                 out = response.getOutputStream();
                 try {
                     response.setBufferSize(4096);
                 } catch (IllegalStateException e) {
                     // Silent catch
                 }
                 out.write(data);
             }
         }
     }
 }
