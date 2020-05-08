 package cellHTS.components;
 
 import org.apache.tapestry5.*;
 import org.apache.tapestry5.json.JSONObject;
 import org.apache.tapestry5.services.Request;
 import org.apache.tapestry5.annotations.*;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.internal.util.TapestryException;
 import org.apache.tapestry5.ioc.Messages;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import cellHTS.classes.RInterface;
 import cellHTS.pages.CellHTS2;
 import data.RCellHTS2Version;
 
 /**
  * Created by IntelliJ IDEA.
  * User: oliverpelz
  * Date: 07.12.2009
  * Time: 17:37:42
  * To change this template use File | Settings | File Templates.
  */
 @IncludeJavaScriptLibrary(value = {"${tapestry.scriptaculous}/prototype.js","browserDetect.js","flashdetect.js","dependenciesChecker.js"})
 public class DependenciesChecker {
     @Persist
     private boolean init;
      @Inject
     private ComponentResources componentResources;
     @Persist
     private String checkAllLink;
     @Persist
     private String successSentLink;
     @Persist
     private String flashInstallLink;
     @Persist
     private String reloadPagelink;
     @Persist
     private String B_PARAMNAME;
     @Persist
     private String F_PARAMNAME;
     @Persist
     private String SUCCESSEVENTNAME;
     @Inject
     private Request request;
     @InjectPage
     private CellHTS2 cellHTS2;
     @Environmental
     private RenderSupport pageRenderSupport;
     @Inject
     private Messages msg;
     @Persist
     private boolean allDependenciesAreMet;
     @SessionState
     private RCellHTS2Version rCellHTS2Version;
     private boolean rCellHTS2VersionExists;
     
     @Parameter(required=true, defaultPrefix="literal")
     private String enableDIVOnSuccess;
 
     public void setupRender() {
             if(!init) {
                 init=true;
                 Link checkAllL = componentResources.createEventLink("checkAllSent", new Object[]{});
                 Link successAllL = componentResources.createEventLink("successSent", new Object[]{});
                 checkAllLink = checkAllL.toAbsoluteURI();
                 successSentLink = successAllL.toAbsoluteURI();
                 B_PARAMNAME="B_PARAMNAME";
            
                 F_PARAMNAME="F_PARAMNAME";
 
                 SUCCESSEVENTNAME="allDependenciesMet";
                 allDependenciesAreMet=false;
             }
 
     }
 
     @OnEvent(value = "checkAllSent")
     public JSONObject  checkAllSentReceiver() {
         
         String ajaxMessage;
         String browserVersionMessage;
 
         //if we got here in this method we must be have enabled javascript
         boolean jsProceed=true;
 
         //check if this is a AJAX request
         if(!request.isXHR()) {
             //if this is not an ajax request we have to throw an exception
             //this can be if proxys such as ezproxy remove the XML Header from the request
             //so check before sending back a json obj which would result in an
             //Return type org.apache.tapestry5.json.JSONObject can not be handled.
             // Configured return types are java.lang.Class, java.lang.String, java.net.URL, org.apache.tapestry5.Link, org.apache.tapestry5.StreamResponse,
             //  org.apache.tapestry5.runtime.Component. 
             ajaxMessage="1. Testing if your ISP proxy supports AJAX failed, Can't proceed. Please check your Proxy settings. Can't proceed";
             //what happens?
             throw new TapestryException(ajaxMessage,null);
 
 
         }
         else {
             ajaxMessage="1. Ajax Request/Response can be made.";
 
         }
         String []browserNVersion = request.getParameter(B_PARAMNAME).split(",");
         String browser = browserNVersion[0];
         float version = Float.parseFloat(browserNVersion[1]);
 
        
         //these are all three browser plus tested versions cellHTS2 was successfully run on
            if(!(browser.equals("Firefox")||browser.equals("Mozilla")||browser.equals("Explorer")||browser.equals("Safari"))) {
                 browserVersionMessage="2. Your browser: "+browser+" is not supported by web CellHTS2. Can't proceed";
 
             }
 
             else if(browser.equals("Firefox") && version<3) {
                 browserVersionMessage="2. Your firefox version is too old (<3). Can't proceed.";
             }             
             else if(browser.equals("Explorer") && version<8) {
              browserVersionMessage="2. Your Internet Explorer version is too old (<8). Can't proceed.";
             }
             else if(browser.equals("Safari") && version <3) {
                 browserVersionMessage="2. Your Safari version is too old (<3). Can't proceed.";
             }
             else {
                 browserVersionMessage="2. browser and version are supported";
             }
 
         //now check out flash
          String []flashEnabledNVersion = request.getParameter(F_PARAMNAME).split(",");
        String isTrue = flashEnabledNVersion[0];
        String fVersion = flashEnabledNVersion[1];
         String flashMessage="";
        if(isTrue.equalsIgnoreCase("true")) {
            flashMessage="3. Optional (no requirement):your installed flash is valid to use with web cellHTS2";
        }
         else {
            flashMessage="3. Optional (no requirement):you have not installed flash or your version is smaller than:  v."+version;
        }
 
 
 
 
         JSONObject returnJSON = new JSONObject();
         returnJSON.put("AJAX",ajaxMessage);
         //can we make an ajax request
         //if we are here javascript must work (otherwise there would be no XHR request possible)
         returnJSON.put("JAVASCRIPT","0. javascript is enabled");
 
         //this is only to complete the ajax request with a response
         returnJSON.put("BROWSER", browserVersionMessage);
 
         returnJSON.put("FLASH", flashMessage);
 
         returnJSON.put("UPLOADPATH","4. "+checkAndCreateUploadDirectory());
 
         //get R and cellHTS Version
         String cellHTS2AndRVersion="";
         String rVersionFetched = getRVersion();
         System.out.println("cellHTS2Version:"+rVersionFetched);
         if(rVersionFetched.equals("not found")||rVersionFetched.equals("<not available>")) {
            cellHTS2AndRVersion="R or cellHTS2 version can't be fetched. Maybe can't connect to RServer. Can't proceed"; 
         }
         else {
             // get R and cellHTS2 version and check if we are above the minimum version needs
             Pattern p = Pattern.compile("([\\d\\.]+)\\s*\\(R:([\\d\\.]+)\\)");
             Matcher m = p.matcher(rVersionFetched);
             if(m.find()) {
                 String rVer = m.group(2);
                 String cellHTS2Ver = m.group(1);
 
                
                 String requiredCellHTS2Ver = msg.get("required-cellHTS2-version");
 
                 if(compareTwoRVersions(rVer, msg.get("required-R-version"))&&cellHTS2Ver.equals(requiredCellHTS2Ver)) {
                     cellHTS2AndRVersion="R, cellHTS2 and RServer could be fe fetched and are in the right version.";
                 }
                 else {
                     cellHTS2AndRVersion="Fetched R and cellHTS2 version "+rVersionFetched+" do not match the required <br/>   " +
                             "R version:"+msg.get("required-R-version")+" and cellHTS2 version:"+requiredCellHTS2Ver+". Maybe can't connect to RServer. Can't proceed";
 
                 }
                 
             }
             else {
                 cellHTS2AndRVersion="R or cellHTS2 version exists but can't be fetched. Maybe can't connect to RServer. Can't proceed";
             }
 
 
         }
         if(cellHTS2AndRVersion.equals("")) {
             cellHTS2AndRVersion="R or cellHTS2 version can't be fetched. Maybe can't connect to RServer. Can't proceed"; 
         }
         returnJSON.put("CELLHTS2VERSION","5. "+cellHTS2AndRVersion);
         return returnJSON;
 
     }
 
 
 
      public void afterRender(MarkupWriter writer){
         if(!allDependenciesAreMet) {
             pageRenderSupport.addScript("checkAll('%s','%s','%s','%s','%s','dependencyChecker')",checkAllLink, successSentLink, B_PARAMNAME,F_PARAMNAME,enableDIVOnSuccess);
         }
     }
 
     @OnEvent(value = "successSent")
     public JSONObject  successReceiver() {
         if(!request.isXHR()) {
             String ajaxMessage="1. Testing if your ISP proxy supports AJAX failed, Can't proceed. Please check your Proxy settings. Can't proceed";
             //what happens?
             throw new TapestryException(ajaxMessage,null);
         }
     //send out an tapestry event that we are done and successfully checked everything    
        //triggerSuccessEvent();
      
         allDependenciesAreMet=true;
         JSONObject returnJSON = new JSONObject();
         returnJSON.put("dummy","dummy");
        return returnJSON;
     }
 
      public void triggerSuccessEvent() {
         //trigger an event that everything has been converted successfully and
         //parameters are the converted files
         ComponentEventCallback callback = new ComponentEventCallback() {
             public boolean handleResult(Object result) {
                 return true;
             }
         };
         componentResources.triggerEvent(SUCCESSEVENTNAME, new Object[]{}, callback);
     }
 
 
     public boolean compareTwoRVersions(String version, String minimumVersion) {               
         String cellHTS2AndRVersion;
                 try {
                     String[] versionPoints = version.split("\\.");
                     String[] versionPointsDep = minimumVersion.split("\\.");
 
                     if(versionPoints.length!=versionPointsDep.length) {
                         return false;
                     }
 
                     int i=0;
                     for(String versionPoint : versionPoints) {
                         int ver = Integer.parseInt(versionPoint);
                         int verDep = Integer.parseInt(versionPointsDep[i++]);
                         System.out.println(ver+"|"+verDep);
                         if(ver<verDep) {
                             return false;
                         }
                     }
 
                 }
                 catch(NumberFormatException e) {
                     return false;
                 }
         return true;
     }
 
     public String checkAndCreateUploadDirectory() {
             String uploadPath;
             if(System.getProperty("upload-path")!=null) {
 
                 //get from command line
                 uploadPath=System.getProperty("upload-path");
             }
             else {
                 //else get from properties file
                 uploadPath=msg.get("upload-path");
             }        
 
             if(!uploadPath.endsWith(File.separator)) {
                 uploadPath=uploadPath+File.separator;
             }
 
             File uploadPathObj = new File(uploadPath);
             if(!uploadPathObj.exists()) {
                 if(!uploadPathObj.mkdirs()) {
                     return "Cannot create directory on the server to upload files: "+uploadPath+". <br/>   " +
                             "Check read/write permissions or change file upload property in apps.properties file. Can't proceed";
                 }
             }
             if(uploadPathObj.canRead()&&uploadPathObj.canWrite()) {
                 //check if we can create a temp file in the new dir
                 File tmpFile =new File(uploadPath+"tmp.txt");
                 try{
 
                     tmpFile.createNewFile();
                     if(tmpFile.canWrite()) {
                         //tmpFile.delete();
                         return "Reading/Writing in temp folder: "+uploadPath+" succeeded!";
                     }
 
                 }catch(IOException e) {
                     e.printStackTrace();
                     if(tmpFile.exists()){
                         tmpFile.delete();
                     }
                     return "Cannot write a test file in the upload path: "+uploadPath+"tmp.txt"+"<br/>   " +
                             "Check read/write permissions or change file upload property in apps.properties file. Can't proceed";
                 }
 
              }else {
                     return "Cannot read or write directory: "+uploadPath+".\nCheck read/write permissions. Can't proceed";
             }
              return "Cannot read or write directory: "+uploadPath+".\nCheck read/write permissions. Can't proceed";
         }
     public String getRVersion() {
         if(!rCellHTS2VersionExists)  {
                 RInterface rInterface = new RInterface();
                 return rInterface.getCellHTS2Version();
         }
         else {
             return rCellHTS2Version.getCellHTS2Version();
         }
 
     }
 
     public String getB_PARAMNAME() {
         return B_PARAMNAME;
     }
 
     public void setB_PARAMNAME(String b_PARAMNAME) {
         B_PARAMNAME = b_PARAMNAME;
     }
 
     public String getF_PARAMNAME() {
         return F_PARAMNAME;
     }
 
     public void setF_PARAMNAME(String f_PARAMNAME) {
         F_PARAMNAME = f_PARAMNAME;
     }
 
     public String getFlashInstallLink() {
         return flashInstallLink;
     }
 
     public void setFlashInstallLink(String flashInstallLink) {
         this.flashInstallLink = flashInstallLink;
     }
 
     public String getReloadPagelink() {
         return reloadPagelink;
     }
 
     public void setReloadPagelink(String reloadPagelink) {
         this.reloadPagelink = reloadPagelink;
     }
 
     public boolean isAllDependenciesAreMet() {
         return allDependenciesAreMet;
     }
 
     public void setAllDependenciesAreMet(boolean allDependenciesAreMet) {
         this.allDependenciesAreMet = allDependenciesAreMet;
     }
 }
