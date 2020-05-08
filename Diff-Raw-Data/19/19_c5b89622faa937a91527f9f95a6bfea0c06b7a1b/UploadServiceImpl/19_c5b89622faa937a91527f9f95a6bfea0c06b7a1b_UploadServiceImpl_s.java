 // -------------------------------------------------------------------------
 // Copyright (c) 2000-2009 Ufinity. All Rights Reserved.
 //
 // This software is the confidential and proprietary information of
 // Ufinity
 // -------------------------------------------------------------------------
 // UFINITY MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 // THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 // TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 // PARTICULAR PURPOSE, OR NON-INFRINGEMENT. UFINITY SHALL NOT BE
 // LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 // MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 //
 // THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
 // CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
 // PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
 // NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
 // SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
 // SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
 // PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES"). UFINITY
 // SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
 // HIGH RISK ACTIVITIES.
 // -------------------------------------------------------------------------
 package com.ufinity.marchant.ubank.service.impl;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.util.Streams;
 import org.apache.log4j.Logger;
 
 import com.ufinity.marchant.ubank.bean.FileBean;
 import com.ufinity.marchant.ubank.bean.Folder;
 import com.ufinity.marchant.ubank.common.EntityManagerUtil;
 import com.ufinity.marchant.ubank.common.Validity;
 import com.ufinity.marchant.ubank.common.preferences.SystemGlobals;
 import com.ufinity.marchant.ubank.dao.DaoFactory;
 import com.ufinity.marchant.ubank.dao.FileDao;
 import com.ufinity.marchant.ubank.dao.FolderDao;
 import com.ufinity.marchant.ubank.dao.UserDao;
 import com.ufinity.marchant.ubank.exception.DbException;
 import com.ufinity.marchant.ubank.exception.UBankServiceException;
 import com.ufinity.marchant.ubank.service.UploadService;
 import com.ufinity.marchant.ubank.upload.ProgressInfo;
 import com.ufinity.marchant.ubank.upload.UploadConstant;
 
 /**
  * UploadService implements
  * 
  * @version 1.0 - 2010-8-23
  * @author liujun
  */
 public class UploadServiceImpl implements UploadService {
 
     private final Logger logger = Logger.getLogger(UploadServiceImpl.class);
 
     private FileDao fileDao;
 
     private FolderDao folderDao;
     
     private UserDao userDao;
 
     /**
      * upload file and save to db
      * 
      * @param currentFolder
      *            current folder
      * @param pi
      *            info of upload
      * @param item
      *            the FileItemStream
      * @throws UBankServiceException
      *             if have UBankServiceException
      */
     public void uploadAndSaveDb(Folder currentFolder, ProgressInfo pi, FileItemStream item)
             throws UBankServiceException {
         if(currentFolder == null){
             throw new UBankServiceException("current folder is null");
         }
         
         Long folderId = currentFolder.getFolderId();
         String folderDir = currentFolder.getDirectory();
         
         if (Validity.isNullOrZero(folderId)) {
             throw new UBankServiceException("current folder id is zero or is null");
         }
         if (item == null) {
             throw new UBankServiceException("FileItem is null. will return");
         }
 
         String fldName = "";
         BufferedInputStream stream = null;
         OutputStream out = null;
         ByteArrayOutputStream bStream = null;
         List<File> createFiles = new ArrayList<File>();
         String realDir = getRealFolderDir(folderDir, folderId);
 
         try {
             fldName = item.getFieldName();
             String fileFullName = item.getName();
             if(Validity.isNullAndEmpty(fileFullName)){
                 throw new UBankServiceException("File name is null");
             }
             
             fileFullName = checkFileName(fileFullName);
             pi.setCurFileName(fileFullName);
             pi.setUploadedFiles(pi.getUploadedFiles() + "<b>" + fileFullName
                     + "</b><br/>");
 
             stream = new BufferedInputStream(item.openStream());
             bStream = new ByteArrayOutputStream();
             long bStreamLen = Streams.copy(stream, bStream, true);
 
             String name = getFileName(fileFullName);
             String type = getFileType(fileFullName);
             
             File file = getUploadFile(realDir, name, type, 0);
             
             out = new FileOutputStream(file);
             bStream.writeTo(out);
             createFiles.add(file);
 
             logger.debug("Upload fldName :" + fldName
                     + ",just was uploaded len:" + bStreamLen);
 
             Folder folder = new Folder();
             folder.setFolderId(folderId);
 
             FileBean fb = new FileBean();
             fb.setFolder(folder);
             fb.setFileName(file.getName());
             fb.setFileType(type);
             Date now = new Date();
             fb.setCreateTime(now);
             fb.setModifyTime(now);
             fb.setRepeatCount(0);
             fb.setShare(currentFolder.getShare());
             
             fb.setDirectory(folderDir + File.separator + folderId);
             // kb
             fb.setSize(bStreamLen / 1024);
            fb.setShare(false);
             this.saveFile(fb);
         } catch (DbException e) {
             // remove files
             for (File file : createFiles) {
                 if (file.exists()) {
                     file.delete();
                 }
             }
             throw new UBankServiceException(e);
         } catch (Exception e) {
             throw new UBankServiceException(e);
         }finally {
             try {
                 if (bStream != null) {
                     bStream.close();
                 }
                 if (out != null) {
                     out.close();
                 }
             } catch (Exception e) {
             }
         }
     }
     
     /**
      * check file name with ie and firefox
      * 
      * @param fileName file name
      * @return file name
      */
     private String checkFileName(String fileName){
         String separator = File.separator;
         if(fileName.contains(separator)){
             fileName = fileName.substring(fileName.lastIndexOf(separator)+1);
         }
         return fileName;
     }
     
     /**
      * get real folder dir
      * 
      * @param folderDir
      * @param folderId
      * @return
      */
     private String getRealFolderDir(String folderDir, Long folderId){
         String objectPath = SystemGlobals.getString(UploadConstant.UBANK_PATH, new String[]{System.getProperty("catalina.home")});
         String dir = objectPath + folderDir + File.separator  + folderId + File.separator;
         return dir;
     }
 
     /**
      * 
      * get file type
      * 
      * @param fileName
      *            file name
      * @return file type
      */
     private String getFileType(String fileName) {
         int i = fileName.lastIndexOf(".");
         if (i == -1) {
             return "";
         }
         return fileName.substring(i + 1);
     }
     
     /**
      * get file name
      * 
      * @param fileFullName file full name
      * @return file name
      */
     private String getFileName(String fileFullName) {
         int i = fileFullName.lastIndexOf(".");
         if (i == -1) {
             return fileFullName;
         }
         return fileFullName.substring(0, i);
     }
 
     /**
      * get folder dir
      * 
      * @param folderId
      *            folder id
      * @throws UBankServiceException
      *             if has UBankServiceException
      * @return folder
      */
     public Folder getFolder(Long folderId) throws UBankServiceException {
         try {
             EntityManagerUtil.begin();
             folderDao = DaoFactory.createDao(FolderDao.class);
             Folder folder = folderDao.find(folderId);
             EntityManagerUtil.commit();
             
             return folder;
         } catch (RuntimeException e) {
             throw new UBankServiceException(e);
         } finally {
             EntityManagerUtil.closeEntityManager();
         }
     }
     
     /**
      * get all file size by user id
      * 
      * @param userId
      *            user id
      * @throws UBankServiceException
      *             if has UBankServiceException
      * @return total size
      */
     public long getTotalFileSize(Long userId) throws UBankServiceException {
         try {
             EntityManagerUtil.begin();
             fileDao = DaoFactory.createDao(FileDao.class);
             Long size = fileDao.findTotalSizeWithFileByUser(userId);
             logger.debug("Total size of user:" + userId + " is " + size);
             if(size == null){
                 return 0;
             }
             EntityManagerUtil.commit();
             return size;
         } catch (RuntimeException e) {
             throw new UBankServiceException(e);
         } finally {
             EntityManagerUtil.closeEntityManager();
         }
     }
     
     /**
      * user add point
      * 
      * @param userId
      *            user id
      */
     public void addPoint(Long userId) {
         try {
             EntityManagerUtil.begin();
             userDao = DaoFactory.createDao(UserDao.class);
             int point = SystemGlobals.getInt(UploadConstant.UPLOAD_DEFAULT_POINT);
             userDao.modifyPointByUserId(userId, point);
             EntityManagerUtil.commit();
         } catch (RuntimeException e) {
             //Ignore
             logger.warn("user add point has exception", e);
         } finally {
             EntityManagerUtil.closeEntityManager();
         }
     }
 
     /**
      * save file to db
      * 
      * @param fb
      *            file bean
      * @throws RuntimeException
      *             if has RuntimeException
      */
     private void saveFile(FileBean fb) throws DbException {
         try {
             EntityManagerUtil.begin();
             fileDao = DaoFactory.createDao(FileDao.class);
             fileDao.add(fb);
             EntityManagerUtil.commit();
         } catch (RuntimeException e) {
             EntityManagerUtil.rollback();
             throw new DbException(e);
         } finally {
             EntityManagerUtil.closeEntityManager();
         }
     }
     
     /**
      * get upload file
      * 
      * @param realDir dir
      * @param name file name
      * @param type file type
      * @param index repeat index
      * @return file
      */
     private File getUploadFile(String realDir, String name, String type, int index){
         String newName = name;
         
         if(index != 0){
             newName = name + "(" + index + ")";
         }
         if(!type.equals("")){
             newName = newName + "." +type;
         }
        
         File file = new File(realDir + newName);
         if (file.exists()) {
             index++;
             return getUploadFile(realDir, name, type, index);
         }else{
             logger.debug("Upload path is :" + realDir + name + type);
             return file;
         }
     }
 }
