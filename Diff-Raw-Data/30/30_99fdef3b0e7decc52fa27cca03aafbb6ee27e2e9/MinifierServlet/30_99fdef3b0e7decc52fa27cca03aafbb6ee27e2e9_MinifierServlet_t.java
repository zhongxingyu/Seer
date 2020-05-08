 /*
  * Copyright (c) 2007 Mysema Ltd.
  * All rights reserved.
  * 
  */
 package com.mysema.webmin;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mysema.webmin.impl.MinifierHandler;
 import com.mysema.webmin.util.ResourceUtil;
 
 /**
  * MinifierServlet provides minification and gzip compression of JavaScript and CSS content
  * 
  * @author Timo Westkamper
  * @version $Id$
  */
 public class MinifierServlet extends HttpServlet {
 
     private static final Logger logger = LoggerFactory.getLogger(MinifierServlet.class);
     
     private static final long serialVersionUID = 4375394993659397518L;
     
     private URL confResource = null;
     
    private transient Handler handler = null;
     
     /**
      * get the servlet config parameter with the given key or return defaultValue
      * @param key
      * @param defaultValue
      * @return
      */
     private String getParameter(String key, String defaultValue){
         String value = getServletConfig().getInitParameter(key);
         return value != null ? value : defaultValue;
     }
     
     /* (non-Javadoc)
      * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
      */
     public void init() throws ServletException {        
         try {
             String configResourceLocation = getParameter("configResourceLocation", "/WEB-INF/minifier.xml");
             confResource = getServletContext().getResource(configResourceLocation);
             
             // TODO : maybe wrap this with secure reloading ?
             logger.debug("loading");                 
             Configuration configuration = ConfigurationFactory.readFrom(confResource.openStream());
             configuration.setLastModified(ResourceUtil.lastModified(confResource));            
             configuration.setUseGzip(Boolean.valueOf(getParameter("useGzip","true")));
             configuration.setJavascriptCompressor(getParameter("javascriptCompressor", "jsmin"));
             getServletContext().setAttribute(Configuration.class.getName(), configuration);
             
             handler = new MinifierHandler(configuration, getServletContext());
                         
         } catch (Exception e) {
             String error = "Caught " + e.getClass().getName();
             logger.error(error, e);
             throw new RuntimeException(error, e);
         }
     }
     
     /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     @Override
     public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
         handler.handle(request, response);        
     }
        
      
 }
