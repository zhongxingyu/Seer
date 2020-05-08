 package org.makumba.parade.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 
 public class CommandAction extends DispatchAction {
 
     public ActionForward reset(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
        Runtime.getRuntime().exec("./home/projects/parade/eec/eec-reset-script/reset");
         return mapping.findForward("files");
     }
 
     public ActionForward newFile(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         String context = request.getParameter("context");
         String path = request.getParameter("path");
         String file = request.getParameter("file");
 
         Response result = CommandController.onNewFile(context, path, file);
 
         Boolean success = result.isSuccess();
         if (success) {
             result.appendMessage(" <a href='/Edit.do?op=editFile&context=" + context + "&path=" + path + "&file="
                     + file + "&editor=simple'>Edit</a></b>");
         }
 
         request.setAttribute("result", result.getMessage());
         request.setAttribute("success", success);
         request.setAttribute("context", context);
         request.setAttribute("path", path);
         request.setAttribute("display", "file");
         return mapping.findForward("files");
     }
 
     public ActionForward newDir(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         String context = request.getParameter("context");
         String path = request.getParameter("path");
         String file = request.getParameter("file");
 
         Response result = CommandController.onNewDir(context, path, file);
 
         request.setAttribute("result", result.getMessage());
         request.setAttribute("success", result.isSuccess());
         request.setAttribute("context", context);
         request.setAttribute("path", path);
         request.setAttribute("display", "file");
         return mapping.findForward("files");
     }
 
     public ActionForward upload(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         String context = request.getParameter("context");
         String path = request.getParameter("path");
 
         // Process the file upload form
         UploadForm upload = (UploadForm) form;
         String contentType = upload.getFileContentType();
         String fileName = upload.getFileName();
         int fileSize = upload.getFileSize();
         byte[] fileData = upload.getFileData();
 
         Response result = CommandController.onUpload(context, path, fileName, fileData);
 
         Boolean success = result.isSuccess();
         if (success) {
             request.setAttribute("contentType", contentType);
             request.setAttribute("contentLength", fileSize);
         }
         request.setAttribute("result", result.getMessage());
         request.setAttribute("success", success);
         request.setAttribute("context", context);
         request.setAttribute("path", path);
         request.setAttribute("file", fileName);
         return mapping.findForward("uploadResponse");
     }
 }
