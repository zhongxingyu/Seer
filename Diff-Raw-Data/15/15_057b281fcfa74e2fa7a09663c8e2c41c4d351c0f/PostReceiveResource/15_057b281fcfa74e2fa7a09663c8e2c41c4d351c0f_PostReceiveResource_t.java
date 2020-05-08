 /*
  * Copyright 2014 Wyona
  */
 
 package com.wyona.yanel.impl.resources.github;
 
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.ViewableV2;
 import org.wyona.yanel.core.attributes.viewable.View;
 import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
 
 import org.apache.logging.log4j.Logger;
 import org.apache.logging.log4j.LogManager;
 
 /**
  * See https://developer.github.com/webhooks/
  */
 public class PostReceiveResource extends Resource implements ViewableV2  {
     
     private static Logger log = LogManager.getLogger(PostReceiveResource.class);
 
     /**
      * @see org.wyona.yanel.core.api.attributes.ViewableV2#exists()
      */
     public boolean exists() throws Exception {
         return true;
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.ViewableV2#getSize()
      */
     public long getSize() throws Exception {
         return -1;
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.ViewableV2#getView(String)
      */
     public View getView(String viewId) throws Exception {
         String xGithubDelivery = getEnvironment().getRequest().getHeader("X-Github-Delivery");
        log.debug("X-Github-Delivery: " + xGithubDelivery);
 
         String xHubSignature = getEnvironment().getRequest().getHeader("X-Hub-Signature"); // INFO: https://developer.github.com/webhooks/
        log.debug("X-Hub-Signature: " + xHubSignature);
 
         String json = getJSon();
         if (json != null) {
             // INFO: Parse json, e.g. http://json.parser.online.fr/
             String[] modifiedFiles = getModifiedFiles(json);
         } else {
             log.error("No json received!");
         }
 
         View view = new View();
         view.setMimeType("text/plain");
         view.setInputStream(new java.io.ByteArrayInputStream("post-receive".getBytes()));
 
         return view;
     }
 
     /**
      *
      */
     private String[] getModifiedFiles(String json) {
         // TODO: https://jsonp.java.net/
         org.json.JSONObject jsonObj = new org.json.JSONObject(json);
 
         String branch = jsonObj.getString("ref");
         log.warn("DEBUG: Branch: " + branch);
 
         String repoName = jsonObj.getJSONObject("repository").getString("name");
         log.warn("DEBUG: Repository name: " + repoName);
 
         String repoURL = jsonObj.getJSONObject("repository").getString("url");
         log.warn("DEBUG: Repository URL: " + repoURL);
 
         org.json.JSONArray commits = jsonObj.getJSONArray("commits");
         for (int i = 0; i < commits.length(); i++) {
            org.json.JSONArray modifiedFiles = commits.getJSONObject(i).getJSONArray("modified");
            for (int k = 0; k < modifiedFiles.length(); k++) {
                log.warn("DEBUG: Modified file: " + modifiedFiles.getJSONObject(k));
            }
         }
 
         return null;
     }
 
     /**
      * Get json from HTTP Post
      * @return json, e.g. {"ref":"refs/heads/continuous-deployment","bef .... pe":"User","site_admin":false}}
      */
     private String getJSon () {
         String contentType = getEnvironment().getRequest().getContentType();
         if ("application/x-www-form-urlencoded".equals(contentType)) {
             log.warn("DEBUG: Decode application/x-www-form-urlencoded ...");
 
             java.util.Enumeration<String> paramNames = getEnvironment().getRequest().getParameterNames();
             if (paramNames.hasMoreElements()) {
                 String name = paramNames.nextElement().toString();
                 if ("payload".equals(name)) {
                     String json = java.net.URLDecoder.decode(getEnvironment().getRequest().getParameter("payload"));
                    log.debug("Key-value pairs as json: " + json);
                     return json;
                 } else {
                     log.error("POST does not contain a parameter called 'payload', but only a parameter called '" + name + "'!");
                 }
             } else {
                 log.error("POST does not contain any parameters!");
             }
         } else if ("application/json".equals(contentType)) {
             log.error("Content type '" + contentType + "' not supported yet!");
         } else {
             log.error("Content type '" + contentType + "' not supported yet!");
         }
         return null;
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.ViewableV2#getViewDescriptors()
      */
     public ViewDescriptor[] getViewDescriptors() {
         return null;
     }
 }
