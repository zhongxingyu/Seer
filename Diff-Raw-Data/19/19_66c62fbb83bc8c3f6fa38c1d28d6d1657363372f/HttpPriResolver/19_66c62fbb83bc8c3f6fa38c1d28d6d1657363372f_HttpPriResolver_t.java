 /* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package org.snowfk.web.part;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.snowfk.util.FileUtil;
 import org.snowfk.util.Pair;
 import org.snowfk.web.RequestContext;
 
 
 
 
 public class HttpPriResolver {
     static private Logger logger = LoggerFactory.getLogger(HttpPriResolver.class);
     
     static final private String FRAME_DEFAULT_HTML  = "/frame-default";
     static final private String FRAME_DEFAULT_EMPTY = "/frame-empty";
 
 
    static final private String INDEX_PAGE_NAME_DEFAULT       = "index";
 
     
     
     public static final String                        LINK_MOD_PREFIX           = "/_m_";
     public static final String                        LINK_CONTENT_PREFIX       = "/_c_";
     
     public static final String PRI_PATH_ACTION_RESPONSE_JSON = "/_actionResponse.json";
     
     /**
      * @param req
      * @return The pri pair for this httpRequest. priPair[0] pagePri, priPair[1] is the eventual framePri
      */
     public static String[] getPriPairFromRequest(RequestContext rc,String defaultModuleName) {
         String[] priPair = new String[2];
         String framePri = null;
         String pagePri = null;
         
         String pathInfo = rc.getPathInfo();
         
         Pair<Part.Type,String> partTypeAndModPrefix = getPartType(pathInfo);
         
         Part.Type partType = partTypeAndModPrefix.getFirst();
         String modPrefix = partTypeAndModPrefix.getSecond();
         
         String modName = getModuleName(pathInfo,modPrefix,defaultModuleName);
         
         String pagePriPath = getPagePriPath(pathInfo, modPrefix);
         
         
         StringBuilder pagePriSb = new StringBuilder(partType.name()).append(':').append(modName).append(':');
         pagePriSb.append(pagePriPath);
         pagePri = pagePriSb.toString();
         
         //build the framePri if this is not a static content or a ".json" page
         //if it is a static content, then, just the  priPage
         if (isTemplateContent(pathInfo)){
             StringBuilder frameSb = new StringBuilder(partType.name()).append(':').append(modName).append(':');
             String framePriPath = getFramePriPath(pathInfo);
             frameSb.append(framePriPath);   
             framePri = frameSb.toString();
         }
         
         priPair[0] = pagePri;
         priPair[1] = framePri;
         return priPair;
     }
 
     
     private static Pair<Part.Type,String> getPartType(String pathInfo){
         Part.Type partType = null;
         String modPrefix = null;
         
         if (pathInfo.startsWith(LINK_MOD_PREFIX)) {
             modPrefix = LINK_MOD_PREFIX;
             partType = Part.Type.t;
         } else if (pathInfo.startsWith(LINK_CONTENT_PREFIX)) {
             modPrefix = LINK_CONTENT_PREFIX;
             partType = Part.Type.c;
         }
         
         // by default, it is a Part.Type.t
         else {
             partType = Part.Type.t;
         }
         return new Pair<Part.Type, String>(partType,modPrefix);
     }
     
     private static  String getModuleName(String pathInfo,String modPrefix, String defaultModuleName){
         //my default, take the rootModuleName
         if (modPrefix == null){
             return defaultModuleName;
         }else{
             String modName = pathInfo.substring(modPrefix.length());
             int idx = modName.indexOf('/');
             // Split the module name from the path
             
             // if we have a moduleName, then add the path
             if (idx != -1) {
                 modName = modName.substring(0,idx);
             }
             
             return modName;
         }
         
 
         
         
     }
     
     
     private static String getPagePriPath(String pathInfo,String modPrefix){
         String pagePriPath;
         int pathStartIdx = 0;
         //get the start of the pathInfo (remove the eventual MOD_PREFIX..MOD_NAME)
         //if we have any MOD_PREFIX, then, ignore the first path
         if (modPrefix != null){
             pathStartIdx = pathInfo.indexOf('/', modPrefix.length());
         }
         pagePriPath = pathInfo.substring(pathStartIdx);
         
         //remove the eventual '_'
         int lastSlashIdx = pagePriPath.lastIndexOf("/");
         if (lastSlashIdx != -1) {
             String pageName = pagePriPath.substring(lastSlashIdx + 1);
             if (pageName.startsWith("_")) {
                 pageName = pageName.substring(1);
             }        
             pagePriPath = new StringBuilder(pagePriPath.substring(0, lastSlashIdx +1)).append(pageName).toString();
         }
         
        //if it ends with .ftl, then remove it
        if (pagePriPath.endsWith(".ftl")){
            pagePriPath = pagePriPath.substring(0, ".ftl".length());
        }
        
        //if it ends with "/" then add the INDEX page name
        if (pagePriPath.endsWith("/")){
            pagePriPath += INDEX_PAGE_NAME_DEFAULT;
         }
 
         
         return pagePriPath;
         
     }
     
     private static String getFramePriPath(String pathInfo) {
         int lastSlashIdx = pathInfo.lastIndexOf("/");
         String pageName = (lastSlashIdx != -1)?pathInfo.substring(lastSlashIdx + 1):pathInfo;
         if (pageName.startsWith("_")) {
             return FRAME_DEFAULT_EMPTY;
         } else {
             return FRAME_DEFAULT_HTML;
         }
     }
     
     /*--------- OLD METHODS  ---------*/
 
 
 
     /*--------- Statics ---------*/
     /**
      * Return true if the content pointed by the pathInfo is static.<br>
      * Right now, just return true if there is no extension
      * 
      * @param pathInfo
      * @return
      */
     static final public boolean isTemplateContent(String pathInfo) {
         int idx = pathInfo.lastIndexOf('.');
         if (idx != -1){
             return false;
         }else{
             return true;
         }
         
     }
     
     static final public boolean isJsonContent(String pathInfo){
         return pathInfo.endsWith(".json");
     }
     /*--------- /Statics ---------*/
     
     public static String getHrefForPart(Part part) {
         StringBuilder hrefSB;
         String href;
 
         String moduleName = part.getWebModule().getName();
         String path = PriUtil.getPathFromPri(part.getPri());
 
         if (part.getType() == Part.Type.c) {
             hrefSB = new StringBuilder(LINK_CONTENT_PREFIX);
             hrefSB.append(moduleName);
 
             if (path.startsWith("/")) {
                 hrefSB.append(path);
                 
             }
             //do the splitIdFolder
             //TODO: need to optimize this. 
             else {
                 hrefSB.append("/");
                 //remove the extention
                 int extDotIdx = path.indexOf('.');
                 String pathWoExt = path;
                 String ext = null;
                 if (extDotIdx != -1) {
                     pathWoExt = path.substring(0, extDotIdx);
                     ext = path.substring(extDotIdx);
                 }
                 String splittedPath = FileUtil.splitIdFolder2(pathWoExt,'/');
                 hrefSB.append(splittedPath.replace(File.separatorChar, '/'));
                 if (ext != null) {
                     hrefSB.append(ext);
                 }
             }
             href = hrefSB.toString();
             
             // --------- Encode the file name --------- //
             //NOTE: this should be generalized.
             String[] filePathAndName = FileUtil.getFilePathAndName(href);
             try {
                 //NOTE: we need to also replace the "+" by "%20" otherwise the file name will include the "+"
                 filePathAndName[1] = URLEncoder.encode(filePathAndName[1], "UTF-8").replace("+","%20");
                 href = new StringBuilder(filePathAndName[0]).append(filePathAndName[1]).toString();
             } catch (UnsupportedEncodingException e) {
                 logger.error(e.getMessage());
             }
             // --------- /Encode the file name --------- //
 
         } else {
             hrefSB = new StringBuilder(LINK_MOD_PREFIX);
             hrefSB.append(moduleName);
             hrefSB.append(path);
             href = hrefSB.toString();
         }
 
         // TODO Auto-generated method stub
         return href;
     }
 
 }
