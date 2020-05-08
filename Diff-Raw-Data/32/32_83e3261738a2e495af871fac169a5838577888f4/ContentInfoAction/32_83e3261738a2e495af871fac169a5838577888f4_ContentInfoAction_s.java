 /**
  * ContentInfoAction.java
  * kevin 2012-6-16
  * @version 0.1
  */
 package com.kevin.group.action.content;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.struts2.ServletActionContext;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import com.kevin.common.action.AbstractBaseAction;
 import com.kevin.common.exception.CommonServiceException;
 import com.kevin.common.pojo.PageBean;
 import com.kevin.group.constance.GroupConstance;
 import com.kevin.group.pojo.content.ContentInfo;
 import com.kevin.group.service.content.IContentInfoService;
 import com.opensymphony.xwork2.Action;
 
 /**
  * @author kevin
  * @since jdk1.6
  */
 @Component("contentInfoAction")
 @Scope("prototype")
 public class ContentInfoAction extends AbstractBaseAction {
 
     private IContentInfoService contInfoService;
     private ContentInfo continfo;
     private List<ContentInfo> contList;
     private List<Serializable> ides;
     private PageBean<ContentInfo> page;
     private String savePath;
     private File file[];
     private String fileFileName[];
     private String fileContentType[];
 
     /**
      * @return the continfo
      */
     public final ContentInfo getContinfo() {
         return continfo;
     }
 
     /**
      * @param continfo
      *            the continfo to set
      */
     public final void setContinfo(ContentInfo continfo) {
         this.continfo = continfo;
     }
 
     /**
      * @return the contList
      */
     public final List<ContentInfo> getContList() {
         return contList;
     }
 
     /**
      * @param contList
      *            the contList to set
      */
     public final void setContList(List<ContentInfo> contList) {
         this.contList = contList;
     }
  
     /**
      * @return the file
      */
     public final File[] getFile() {
         return file;
     }
 
     /**
      * @param file the file to set
      */
     public final void setFile(File[] file) {
         this.file = file;
     }
 
     /**
      * @return the fileFileName
      */
     public final String[] getFileFileName() {
         return fileFileName;
     }
 
     /**
      * @param fileFileName the fileFileName to set
      */
     public final void setFileFileName(String[] fileFileName) {
         this.fileFileName = fileFileName;
     }
 
     /**
      * @return the fileContentType
      */
     public final String[] getFileContentType() {
         return fileContentType;
     }
 
     /**
      * @param fileContentType the fileContentType to set
      */
     public final void setFileContentType(String[] fileContentType) {
         this.fileContentType = fileContentType;
     }
 
     /**
      * @return the savePath
      */
     public final String getSavePath() {
         return savePath;
     }
 
     /**
      * @param savePath
      *            the savePath to set
      */
     public final void setSavePath(String savePath) {
         this.savePath = savePath;
     }
 
     /**
      * @param contInfoService
      *            the contInfoService to set
      */
     @Autowired
     public final void setContInfoService(IContentInfoService contInfoService) {
         this.contInfoService = contInfoService;
     }
 
     /**
      * @return the ides
      */
     public final List<Serializable> getIdes() {
         return ides;
     }
 
     /**
      * @param ides
      *            the ides to set
      */
     public final void setIdes(List<Serializable> ides) {
         this.ides = ides;
     }
   
 
     /**
      * @return the page
      */
     public final PageBean<ContentInfo> getPage() {
         return page;
     }
 
     /**
      * @param page the page to set
      */
     public final void setPage(PageBean<ContentInfo> page) {
         this.page = page;
     }
 
     public String addContent() {// add new contInfo
         try {
             if (null != file) {
                 // make the parent folder when each month
                 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM", Locale.ENGLISH);
                 String monthlize = dateFormat.format(new Date());
                 String realPath = ServletActionContext.getServletContext().getRealPath(this.getSavePath());
                 File storeFolder = new File(realPath + File.separator + GroupConstance.UPLOAD_NEWS + File.separator + monthlize);
                 //File storeFolder = new File("/upload/content/" + monthlize);
                 if(!storeFolder.exists()) storeFolder.mkdirs();
                 for(int i = 0;i<file.length;i++){
                    File storeFile = new File(storeFolder, System.currentTimeMillis() + "_" + fileFileName[i]);
                     if(0 == i){
                         String storePath = storeFile.getAbsolutePath();
                         continfo.setContAttachment(storePath.substring(storePath.indexOf(GroupConstance.UPLOAD_ROOT)));
                     }
                     if(1 == i){
                         String storePath = storeFile.getAbsolutePath();
                         continfo.setDisplayImage(storePath.substring(storePath.indexOf(GroupConstance.UPLOAD_ROOT)));
                     }
                     //storeFile.createNewFile();
                     //FileUtils.copyFile(file[i], storeFile);
                     FileUtils.moveFile(file[i], storeFile);
                 } 
             }
             Serializable id = contInfoService.save(continfo);
             setResultCode((Integer) id);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         } catch (IOException e) {
             System.out.println("move file exception: " + e.getMessage());
             setMessage(e.getMessage()); 
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String searchContent() {// search contInfo
         try {
             this.continfo = contInfoService.search(continfo);
             setResultCode(1);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String listContent() {// list all the contInfo
         try {
             if (page != null) {
                 page.setCondition(continfo);
             }
             page = contInfoService.list(page);
             //contList = contInfoService.listAll();
             setResultCode(1);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String updateContent() {// update the contInfo info
         try {
             if (null != file) {
                 // make the parent folder when each month
                 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM", Locale.ENGLISH);
                 String monthlize = dateFormat.format(new Date());
                 String realPath = ServletActionContext.getServletContext().getRealPath(this.getSavePath());
                 File storeFolder = new File(realPath + File.separator + GroupConstance.UPLOAD_NEWS + File.separator +  monthlize);
                 //File storeFolder = new File("/upload/content/" + monthlize);
                 if(!storeFolder.exists()) storeFolder.mkdirs();
                 for(int i = 0;i<file.length;i++){
                    File storeFile = new File(storeFolder, System.currentTimeMillis() + "_" + fileFileName[i]);
                     if(0 == i){
                         String storePath = storeFile.getAbsolutePath();
                         continfo.setContAttachment(storePath.substring(storePath.indexOf(GroupConstance.UPLOAD_ROOT)));
                     }
                     if(1 == i){
                         String storePath = storeFile.getAbsolutePath();
                         continfo.setDisplayImage(storePath.substring(storePath.indexOf(GroupConstance.UPLOAD_ROOT)));
                     }
                     //storeFile.createNewFile();
                     //FileUtils.copyFile(file[i], storeFile);
                     FileUtils.moveFile(file[i], storeFile);
                 } 
             }
             int result = contInfoService.update(continfo);
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         } catch (IOException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String deleteContent() {// delete the contInfo
         try {
             int result = contInfoService.delete(continfo);
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
     
     public String updateClickContent() {// click the contInfo counter
         try {
             int result = contInfoService.updateClickContent(continfo);
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String batchDeleteContent() {// bath delete the contInfo
         try {
             int result = contInfoService.batchDelete(ides);
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String auditorContent() {// auditor the contInfo
         try {
             int result = contInfoService.auditorContent(continfo);
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String generateHomeJson() {// auditor the contInfo
         try {
             String realPath = ServletActionContext.getServletContext().getRealPath(this.getSavePath());
             //File storeFile = new File(realPath);
             //File storeFolder = storeFile.getParentFile();
             File storeFolder = new File(realPath);
             if (!storeFolder.exists())
                 storeFolder.mkdirs();
             int result = contInfoService.generateHomeJson(storeFolder.getAbsolutePath());
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 }
