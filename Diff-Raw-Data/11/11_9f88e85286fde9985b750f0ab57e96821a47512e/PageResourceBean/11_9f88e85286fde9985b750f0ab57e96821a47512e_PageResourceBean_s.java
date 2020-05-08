 package com.mobitle.kolonsports;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class PageResourceBean implements java.io.Serializable {
     private String indexPageUrl;
     private String allPageUrl;
     private String templateUrl;
     private String cssJspUrl;
     private String scriptJspUrl;
     private String sidebarJspUrl;
     private String footerJspUrl;
 
     private String contextPath;
     private String mediaUrl;
     private String imageUrl;
     private String movieUrl;
     private String scriptUrl;
 
     public void init(HttpServletRequest request) {
         setIndexPageUrl("/index.jsp");
         setAllPageUrl("/allpage.jsp");
         setTemplateUrl("/templates");
         setCssJspUrl(getTemplateUrl("css.jsp"));
         setScriptJspUrl(getTemplateUrl("script.jsp"));
         setSidebarJspUrl(getTemplateUrl("sidebar.jsp"));
         setFooterJspUrl(getTemplateUrl("footer.jsp"));
 
         setContextPath(request.getContextPath());
         setMediaUrl(getContextPath("media"));
         setImageUrl(getMediaUrl("images"));
         setMovieUrl(getMediaUrl("movies"));
         setScriptUrl(getMediaUrl("js"));
     }
 
     public void setIndexPageUrl(String url) {
         this.indexPageUrl = url;
     }
 
     public void setAllPageUrl(String url) {
         this.allPageUrl = url;
     }
 
     public void setTemplateUrl(String url) {
         this.templateUrl = url;
     }
 
     public void setCssJspUrl(String url) {
         this.cssJspUrl = url;
     }
 
     public void setScriptJspUrl(String url) {
         this.scriptJspUrl = url;
     }
 
     public void setSidebarJspUrl(String url) {
         this.sidebarJspUrl = url;
     }
 
     public void setFooterJspUrl(String url) {
         this.footerJspUrl = url;
     }
 
     public void setContextPath(String path) {
         this.contextPath = path;
     }
 
     public void setMediaUrl(String url) {
         this.mediaUrl = url;
     }
 
     public void setImageUrl(String url) {
         this.imageUrl = url;
     }
 
     public void setMovieUrl(String url) {
         this.movieUrl = url;
     }
 
     public void setScriptUrl(String url) {
         this.scriptUrl = url;
     }
 
     public String getContextPath() {
         return contextPath;
     }
 
     public String getContextPath(String path) {
         return contextPath + "/" + path;
     }
 
     public String getMediaUrl() {
         return mediaUrl;
     }
 
     public String getMediaUrl(String path) {
         return mediaUrl + "/" + path;
     }
     
     public String getTemplateUrl() {
         return templateUrl;
     }
 
     public String getTemplateUrl(String filename) {
         return templateUrl + "/" + filename;
     }
 
     public String getImageUrl() {
         return imageUrl;
     }
 
     public String getImageUrl(String filename) {
         return imageUrl + "/" + filename;
     }
 
     public String getMovieUrl() {
         return movieUrl;
     }
 
     public String getMovieUrl(String filename) {
         return movieUrl + "/" + filename;
     }
 
     public String getScriptUrl() {
         return scriptUrl;
     }
 
     public String getScriptUrl(String filename) {
         return scriptUrl + "/" + filename;
     }
 
     public String getIndexPageUrl() {
         return indexPageUrl;
     }
 
     public String getAllPageUrl() {
         return allPageUrl;
     }
 
     public String getCssJspUrl() {
         return cssJspUrl;
     }
 
     public String getScriptJspUrl() {
         return scriptJspUrl;
     }
 
     public String getSidebarJspUrl() {
         return sidebarJspUrl;
     }
 
     public String getFooterJspUrl() {
         return footerJspUrl;
     }
 }
