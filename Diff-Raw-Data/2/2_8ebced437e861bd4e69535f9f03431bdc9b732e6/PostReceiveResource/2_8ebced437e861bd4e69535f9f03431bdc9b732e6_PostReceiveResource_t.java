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
             CommitInfoBean bean = getCommitInformation(json);
             addContinuousIntegrationTask(bean);
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
     private void addContinuousIntegrationTask(CommitInfoBean bean) {
         log.warn("DEBUG: Add continuous integration task ...");
 
         String[] files= bean.getModifiedFiles();
         for (int i = 0; i < files.length; i++) {
             log.warn("DEBUG: Modified file: " + files[i]);
         }
 
         log.warn("DEBUG: Branch: " + bean.getBranch());
 
         log.warn("DEBUG: Repository: " + bean.getRepositoryName() + ", " + bean.getRepositoryURL());
     }
 
     /**
      *
      */
     private CommitInfoBean getCommitInformation(String json) {
         CommitInfoBean bean = new CommitInfoBean();
 
         // TODO: https://jsonp.java.net/
         org.json.JSONObject jsonObj = new org.json.JSONObject(json);
 
         String branch = jsonObj.getString("ref");
         log.warn("DEBUG: Branch: " + branch);
         bean.setBranch(branch);
 
         String repoName = jsonObj.getJSONObject("repository").getString("name");
         log.warn("DEBUG: Repository name: " + repoName);
        bean.setRepositoryName(repoName);
 
         String repoURL = jsonObj.getJSONObject("repository").getString("url");
         log.warn("DEBUG: Repository URL: " + repoURL);
         bean.setRepositoryURL(repoURL);
 
         java.util.ArrayList<String> files = new java.util.ArrayList<String>();
         org.json.JSONArray commits = jsonObj.getJSONArray("commits");
         for (int i = 0; i < commits.length(); i++) {
             org.json.JSONArray modifiedFiles = commits.getJSONObject(i).getJSONArray("modified");
             for (int k = 0; k < modifiedFiles.length(); k++) {
                 String file = modifiedFiles.get(k).toString();
                 files.add(file);
                 log.warn("DEBUG: Modified file: " + file);
             }
         }
         bean.setModifiedFiles(files.toArray(new String[0]));
 
         return bean;
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
 
 /**
  *
  */
 class CommitInfoBean {
 
     private String[] modifiedFiles;
     private String branch;
     private String repoName;
     private String repoURL;
 
     /**
      *
      */
     public void setBranch(String branch) {
         this.branch = branch;
     }
 
     /**
      *
      */
     public String getBranch() {
         return branch;
     }
 
     /**
      *
      */
     public void setRepositoryName(String name) {
         this.repoName = name;
     }
 
     /**
      *
      */
     public String getRepositoryName() {
         return repoName;
     }
 
     /**
      *
      */
     public void setRepositoryURL(String url) {
         this.repoURL = url;
     }
 
     /**
      *
      */
     public String getRepositoryURL() {
         return repoURL;
     }
 
     /**
      *
      */
     public void setModifiedFiles(String[] files) {
         this.modifiedFiles = files;
     }
 
     /**
      *
      */
     public String[] getModifiedFiles() {
         return modifiedFiles;
     }
 }
