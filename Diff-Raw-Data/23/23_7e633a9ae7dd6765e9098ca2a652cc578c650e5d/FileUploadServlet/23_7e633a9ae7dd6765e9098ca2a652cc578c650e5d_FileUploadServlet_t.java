 // -------------------------------------------------------------------------
 // Copyright (c) 2000-2010 Ufinity. All Rights Reserved.
 //
 // This software is the confidential and proprietary information of
 // Ufinity
 //
 // Original author:
 //
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
 package com.ufinity.marchant.ubank.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.log4j.Logger;
 
 import com.ufinity.marchant.ubank.bean.Folder;
 import com.ufinity.marchant.ubank.common.JsonUtil;
 import com.ufinity.marchant.ubank.common.Validity;
 import com.ufinity.marchant.ubank.common.preferences.MessageKeys;
 import com.ufinity.marchant.ubank.common.preferences.MessageResource;
 import com.ufinity.marchant.ubank.service.ServiceFactory;
 import com.ufinity.marchant.ubank.service.UploadService;
 import com.ufinity.marchant.ubank.upload.ProgressInfo;
 import com.ufinity.marchant.ubank.upload.UploadConstant;
 import com.ufinity.marchant.ubank.upload.UploadListener;
 
 /**
  * 
  * file upload servlet
  * 
  * @author liujun
  * @version 1.0
  * @since 2010-8-20
  */
 public class FileUploadServlet extends AbstractServlet {
 
     private static final long serialVersionUID = 6092584996678971635L;
 
     private Logger logger = Logger.getLogger(FileUploadServlet.class);
     
     private UploadService uploadService = null;
     
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
      *      response)
      */
     protected void doGet(HttpServletRequest request,
             HttpServletResponse response) throws ServletException, IOException {
         doPost(request, response);
     }
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
      *      response)
      */
     protected void doPost(HttpServletRequest request,
             HttpServletResponse response) throws ServletException, IOException {
         String method = parseActionName(request);
         if (UploadConstant.UPLOAD_METHOD.equals(method)) {
             doUpload(request);
         } else if (UploadConstant.GET_INFO_METHOD.equals(method)) {
             getUploadInfo(request, response);
         } else if (UploadConstant.PAUSE_METHOD.equals(method)) {
             pause(request);
         } else if (UploadConstant.CONTINUE_UOLOAD_METHOD.equals(method)) {
             continueUpload(request);
         }
     }
 
     /**
      * 
      * response json date to client
      * 
      * @param request
      *            response
      * @param String
      *            json
      */
     private void responseClient(HttpServletResponse response, String json) {
         response.setContentType(UploadConstant.CONTENT_TYPE);
         PrintWriter out = null;
         try {
             out = response.getWriter();
             out.write(json);
             out.flush();
         } catch (Exception e) {
             logger.error("response client error:", e);
         } finally {
             if (out != null) {
                 out.close();
             }
         }
     }
 
     /**
      * 
      * get upload info
      * 
      * @param request
      *            request
      * @param response
      *            response
      */
     private void getUploadInfo(HttpServletRequest request,
             HttpServletResponse response) {
         // System.out.println("getUploadInfo.........");
         ProgressInfo pi = (ProgressInfo) request.getSession().getAttribute(
                 UploadConstant.PROGRESS_INFO);
         if (pi != null) {
             String json = JsonUtil.bean2json(pi);
             responseClient(response, json);
         }
     }
 
     /**
      * 
      * pause upload
      * 
      * @param request
      *            request
      */
     private void pause(HttpServletRequest request) {
         // System.out.println("pause~~~~~~~~~~~~~~~~~");
         ProgressInfo pi = (ProgressInfo) request.getSession().getAttribute(
                 UploadConstant.PROGRESS_INFO);
         if (pi != null) {
             pi.setPause(true);
             request.getSession().setAttribute(UploadConstant.PROGRESS_INFO, pi);
         }
     }
 
     /**
      * 
      * continue upload
      * 
      * @param request
      *            request
      */
     private void continueUpload(HttpServletRequest request) {
         // System.out.println("continue upload~~~~~~~~~~~~~~~~~");
         ProgressInfo pi = (ProgressInfo) request.getSession().getAttribute(
                 UploadConstant.PROGRESS_INFO);
         if (pi != null) {
             pi.setPause(false);
             request.getSession().setAttribute(UploadConstant.PROGRESS_INFO, pi);
         }
     }
 
     /**
      * 
      * do file upload
      * 
      * @param request
      *            request
      */
     private void doUpload(HttpServletRequest request) {
         Folder currentFolder = (Folder)request.getSession().getAttribute(UploadConstant.CURRENT_FOLDER);
         ProgressInfo pi = new ProgressInfo();
         request.getSession().setAttribute(UploadConstant.PROGRESS_INFO, pi);
 
         try {
             boolean isMultipart = ServletFileUpload.isMultipartContent(request);
             if (isMultipart) {
                 int filesSize = request.getContentLength()
                         - UploadConstant.HTTP_REDUNDANT_LENGTH;
                 if (filesSize >= UploadConstant.MAX_LENGTH) {
                     String[] params = {filesSize / (1024 * 1024)+"MB",UploadConstant.MAX_LENGTH/(1024*1024)+"MB"};
                     String errorMsg = MessageResource.getMessage(MessageKeys.UPLOAD_SIZE_MAX, params);
                     pi.setInProgress(false);
                     pi.setErrorMsg(errorMsg);
                    logger.debug("Upload files size is to big");
                    //System.out.println("Upload files size is to big");
                     throw new Exception(errorMsg);
                 }
                 ServletFileUpload upload = new ServletFileUpload();
                 upload.setHeaderEncoding(UploadConstant.HEADER_ENCODE);
                 upload.setFileSizeMax(UploadConstant.MAX_LENGTH);
                 upload.setSizeMax(UploadConstant.MAX_LENGTH);
                 UploadListener uploadListener = new UploadListener(pi);
                 upload.setProgressListener(uploadListener);
                 // Parse the request
                 FileItemIterator fIter = upload.getItemIterator(request);
                 
                 //TODO
                 currentFolder = new Folder();
                 currentFolder.setFolderId(1l);
                 currentFolder.setDirectory("E:/temp/folder/");
                 uploadService = ServiceFactory.createService(UploadService.class);
                 uploadService.uploadAndSaveDb(currentFolder, pi, fIter);
                 
                 pi.setCurrentTime(System.currentTimeMillis());
                 pi.setBytesRead(filesSize);
                 pi.setCompleted(true);
             }
         } catch (Exception e) {
             pi.setInProgress(false);
             if(Validity.isEmpty(pi.getErrorMsg())){
                 pi.setErrorMsg(MessageResource.getMessage(MessageKeys.UPLOAD_EXECEPTION));
             }
             logger.warn("Upload cancelled or interrupted!" ,e);
         } 
     }
 
 }
