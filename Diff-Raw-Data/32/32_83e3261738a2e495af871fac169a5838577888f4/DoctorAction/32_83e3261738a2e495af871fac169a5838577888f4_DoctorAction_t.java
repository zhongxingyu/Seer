 /**
  * DoctorAction.java
  * kevin 2012-6-17
  * @version 0.1
  */
 package com.kevin.group.action.member;
 
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
 import com.kevin.group.pojo.member.DoctorInfo;
 import com.kevin.group.service.member.IDoctorService;
 import com.opensymphony.xwork2.Action;
 
 /**
  * @author kevin
  * @since jdk1.6
  */
 @Component("doctorAction")
 @Scope("prototype")
 public class DoctorAction extends AbstractBaseAction {
 
     private IDoctorService doctorService;
     private DoctorInfo doct;
     private List<DoctorInfo> doctList;
     private PageBean<DoctorInfo> page;
     private List<Serializable> ides;
     private File file;
     private String fileFileName;
     private String fileContentType;
     private String savePath;
  
     /**
      * @return the page
      */
     public final PageBean<DoctorInfo> getPage() {
         return page;
     }
 
     /**
      * @param page the page to set
      */
     public final void setPage(PageBean<DoctorInfo> page) {
         this.page = page;
     }
 
     /**
      * @return the doct
      */
     public final DoctorInfo getDoct() {
         return doct;
     }
 
     /**
      * @param doct
      *            the doct to set
      */
     public final void setDoct(DoctorInfo doct) {
         this.doct = doct;
     }
 
     /**
      * @return the doctList
      */
     public final List<DoctorInfo> getDoctList() {
         return doctList;
     }
 
     /**
      * @param doctList
      *            the doctList to set
      */
     public final void setDoctList(List<DoctorInfo> doctList) {
         this.doctList = doctList;
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
      * @param doctorService
      *            the doctorService to set
      */
     @Autowired
     public final void setDoctorService(IDoctorService doctorService) {
         this.doctorService = doctorService;
     }
 
     /**
      * @return the file
      */
     public final File getFile() {
         return file;
     }
 
     /**
      * @param file the file to set
      */
     public final void setFile(File file) {
         this.file = file;
     }
 
     /**
      * @return the fileFileName
      */
     public final String getFileFileName() {
         return fileFileName;
     }
 
     /**
      * @param fileFileName the fileFileName to set
      */
     public final void setFileFileName(String fileFileName) {
         this.fileFileName = fileFileName;
     }
 
     /**
      * @return the fileContentType
      */
     public final String getFileContentType() {
         return fileContentType;
     }
 
     /**
      * @return the savePath
      */
     public final String getSavePath() {
         return savePath;
     }
 
     /**
      * @param savePath the savePath to set
      */
     public final void setSavePath(String savePath) {
         this.savePath = savePath;
     }
 
     /**
      * @param fileContentType the fileContentType to set
      */
     public final void setFileContentType(String fileContentType) {
         this.fileContentType = fileContentType;
     }
 
     public String addDoctor() {// add new doct
         try {
             if (null != file) {
                 // make the parent folder when each month
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                String dateTime = dateFormat.format(new Date());
                 //File storeFolder = new File("/upload/member/" + monthlize);
                 //String realPath = ServletActionContext.getServletContext().getContextPath();
                 String realPath = ServletActionContext.getServletContext().getRealPath(this.getSavePath());
                 File storeFolder = new File(realPath + File.separator + GroupConstance.UPLOAD_DOCTOR);
                 if (!storeFolder.exists())
                     storeFolder.mkdirs();
                File storeFile = new File(storeFolder, dateTime + "_" + fileFileName);
                 String storePath = storeFile.getAbsolutePath();
                 doct.setDiPortrait(storePath.substring(storePath .indexOf(GroupConstance.UPLOAD_ROOT)));
                 // FileUtils.copyFile(file, storeFile);
                 // storeFile.createNewFile();
                 FileUtils.moveFile(file, storeFile);
             }
             Serializable id = doctorService.save(doct);
             setResultCode((Integer) id);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         } catch (IOException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String searchDoctor() {// search doct
         try {
             this.doct = doctorService.search(doct);
             setResultCode(1);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String listDoctor() {// list all the doct
         try {
             if(page!=null){
                 page.setCondition(doct);
             }
             page = doctorService.list(page);
             setResultCode(1);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String updateDoctor() {// update the doct info
         try {
             if (null != file) {
                 // make the parent folder when each month
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                String dateTime = dateFormat.format(new Date());
                 //File storeFolder = new File("/upload/member/" + monthlize);
                 String realPath = ServletActionContext.getServletContext().getRealPath(this.getSavePath());
                 File storeFolder = new File(realPath + File.separator + GroupConstance.UPLOAD_DOCTOR);
                 if (!storeFolder.exists())
                     storeFolder.mkdirs();
                File storeFile = new File(storeFolder, dateTime + "_" + fileFileName);
                 String storePath = storeFile.getAbsolutePath();
                 doct.setDiPortrait(storePath.substring(storePath .indexOf(GroupConstance.UPLOAD_ROOT)));
                 // FileUtils.copyFile(file, storeFile);
                 // storeFile.createNewFile();
                 FileUtils.moveFile(file, storeFile);
             }
             int result = doctorService.update(doct);
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
 
     public String deleteDoctor() {// delete the doct
         try {
             int result = doctorService.delete(doct);
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 
     public String batchDeleteDoctor() {// bath delete the doct
         try {
             int result = doctorService.batchDelete(ides);
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
     
     public String generateDoctJson() {// bath delete the doct
         try {
             String realPath = ServletActionContext.getServletContext().getRealPath(this.getSavePath());
             //File storeFile = new File(realPath);
             //File storeFolder = storeFile.getParentFile();
             File storeFolder = new File(realPath);
             if (!storeFolder.exists())
                 storeFolder.mkdirs();
             int result = doctorService.generateDoctJson(storeFolder.getAbsolutePath());
             setResultCode(result);
         } catch (CommonServiceException e) {
             setMessage(e.getMessage());
             setResultCode(-1);
         }
         return Action.SUCCESS;
     }
 }
