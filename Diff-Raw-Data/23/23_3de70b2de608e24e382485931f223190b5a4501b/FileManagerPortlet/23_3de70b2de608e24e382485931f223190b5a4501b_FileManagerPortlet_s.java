 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.file;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.provider.ActionPortlet;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 
 import org.gridlab.gridsphere.services.core.file.FileManagerService;
 
 import javax.servlet.UnavailableException;
 import javax.activation.FileDataSource;
 import javax.activation.DataHandler;
 import java.io.*;
 import java.util.List;
 import java.util.Iterator;
 
 public class FileManagerPortlet extends ActionPortlet {
 
     private FileManagerService userStorage = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         try {
             userStorage = (FileManagerService)config.getContext().getService(FileManagerService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize FileManagerService", e);
         }
         DEFAULT_VIEW_PAGE = "doViewUserFiles";
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void doViewUserFiles(FormEvent event) throws PortletException {
         log.debug("in FileManagerPortlet: doViewUser");
         PortletRequest request = event.getPortletRequest();
         User user = request.getUser();
 
         ListBoxBean lb = event.getListBoxBean("filelist");
         lb.clear();
         String[] list = userStorage.getUserFileList(user);
         // set the list box size to number of files plus padding
         if (list.length == 0) {
             list = new String[] {"empty directory"};
         }
         lb.setSize(list.length + 3);
         for (int i = 0; i < list.length; i++) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setValue(list[i]);
             lb.addBean(item);
         }
         setNextState(request, "filemanager/view.jsp");
     }
 
     public void uploadFile(FormEvent event) throws PortletException {
         log.debug("in FileManagerPortlet: doUploadFile");
         try {
             FileInputBean fi = event.getFileInputBean("userfile");
             User user = event.getPortletRequest().getUser();
             File f = fi.getFile();
             String fileName = fi.getFileName();
             userStorage.storeFile(user, f, fileName);
             //String location = userStorage.getLocationPath(user, "myfile");
             //log.debug("fileinputbean value=" + fi.getValue() + " location to store=" + location);
             //fi.saveFile(location);
         } catch (IOException e) {
             setNextError(event.getPortletRequest(), "Unable to store uploaded file " + e.getMessage());
         }
         setNextState(event.getPortletRequest(), DEFAULT_VIEW_PAGE);
     }
 
     public void deleteFile(FormEvent event) throws PortletException {
         log.debug("in FileManagerPortlet: deleteFile");
 
         // Files can be deleted from edit or view pages
         // In view page the file is in a listbox
         ListBoxBean lb = event.getListBoxBean("filelist");
         List files = lb.getSelectedValues();
         User user = event.getPortletRequest().getUser();
         Iterator it = files.iterator();
       String fname = null;
         while (it.hasNext()) {
             fname = (String)it.next();
             userStorage.deleteFile(user, fname);
         }
 
         // In edit page the file is in a hidden field
         HiddenFieldBean hf = event.getHiddenFieldBean("fileName");
         fname = hf.getValue();
         if (fname != null) userStorage.deleteFile(user, fname);
     }
 
     public void cancelEdit(FormEvent event) throws PortletException {
         log.debug("in FileManagerPortlet: cancelEdit");
         setNextState(event.getPortletRequest(), DEFAULT_VIEW_PAGE);
     }
 
     public void saveFile(FormEvent event) throws PortletException {
         log.debug("in FileManagerPortlet: saveFile");
         User user = event.getPortletRequest().getUser();
         PortletRequest req = event.getPortletRequest();
         //String fname = event.getAction().getParameter("fileName");
 
         HiddenFieldBean hf = event.getHiddenFieldBean("fileName");
         String fileName = hf.getValue();
 
         TextAreaBean ta = event.getTextAreaBean("fileTextArea");
         String newText = ta.getValue();
         try {
             System.err.println(newText);
             File tmpFile = userStorage.getFile(user, fileName);
             FileWriter f = new FileWriter(tmpFile);
 
             f.write(newText);
             f.close();
             //userStorage.storeFile(user, tmpFile, fname);
             //tmpFile.delete();
         } catch (IOException e) {
             log.error("Error saving file:" + fileName, e);
            ErrorFrameBean error = event.getErrorFrameBean("editError");
             error.setValue("Unable to save file: " + fileName);
         }
     }
 
     public void downloadFile(FormEvent event) throws PortletException {
         log.debug("in FileManagerPortlet: downloadFile");
         ListBoxBean lb = event.getListBoxBean("filelist");
         List files = lb.getSelectedValues();
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         User user = req.getUser();
         Iterator it = files.iterator();
         String fileName = null;
         while (it.hasNext()) {
             fileName = (String)it.next();
         }
 
 
         try {
             File file = userStorage.getFile(user, fileName);
             // ... find the file you want ...
             if (!file.exists()) {
                 throw new FileNotFoundException("No file found: " + fileName + " for user: " + user.getUserName());
             }
             log.debug("filename: " + fileName);
             FileDataSource fds = new FileDataSource(file);
 
             log.debug("content type = " + fds.getContentType());
             //res.setContentType(fds.getContentType());
             //res.setContentType("application/download");
             res.reset();
             res.resetBuffer();
             res.setContentType("application/octet-stream");
             //res.setContentType( "application/unknow" );
             res.setHeader("Cache-Control","no-cache"); //HTTP 1.1
             res.setHeader("Pragma","no-cache"); //HTTP 1.0
             res.setDateHeader ("Expires", 0); //prevents caching at the proxy server
             res.setHeader("Cache-Control","no-store"); //HTTP 1.1
 
             log.debug("file length= " + (int)file.length());
             res.setContentLength((int)file.length());
             // force a save as dialog in recent browsers.
             res.setHeader("Content-Disposition","attachment; filename=\"" + fileName + "\";");
 
             // you should use a datahandler to write out data from a datasource.
             DataHandler handler = new DataHandler(fds);
             handler.writeTo(res.getOutputStream());
             System.err.println("OK acttually wrote file to output!! :-)");
         } catch(FileNotFoundException e) {
             log.error("Unable to find file!", e);
         } catch(SecurityException e) {
             // this gets thrown if a security policy applies to the file. see java.io.File for details.
             log.error("A security exception occured!", e);
         } catch(IOException e) {
             log.error("Caught IOException", e);
             //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER,e.getMessage());
         }
     }
 
     public void editFile(FormEvent event) throws PortletException {
 
         log.debug("in FileManagerPortlet: viewFile");
         ListBoxBean lb = event.getListBoxBean("filelist");
         PortletRequest req = event.getPortletRequest();
         User user = req.getUser();
 
         if (lb.hasSelectedValue()) {
             String file = lb.getSelectedValue();
             try {
                 req.setAttribute("fileName", file);
                 TextBean textBean = event.getTextBean("msg");
                 textBean.setValue("Displaying file: " + file);
                 TextAreaBean fileTextArea = event.getTextAreaBean("fileTextArea");
                 HiddenFieldBean hidden = event.getHiddenFieldBean("fileName");
                 hidden.setValue(file);
                 File editFile = userStorage.getFile(user, file);
                 BufferedReader reader = new BufferedReader(new FileReader(editFile));
                 String line = null;
                 StringBuffer sb = new StringBuffer();
                 while ((line = reader.readLine()) != null) {
                     sb.append(line);
                     sb.append("\n");
                 }
                 fileTextArea.setValue(sb.toString());
                 setNextState(req, "filemanager/edit.jsp");
             } catch (FileNotFoundException e) {
                 log.error("Error opening file:" + file, e);
                ErrorFrameBean error = event.getErrorFrameBean("errorFrame");
                 error.setValue("Unable to open file: " + file);
                 setNextState(req, DEFAULT_VIEW_PAGE);
             } catch (IOException e) {
                 log.error("Error opening file:" + file, e);
                ErrorFrameBean error = event.getErrorFrameBean("errorFrame");
                 error.setValue("Unable to open file: " + file);
                 setNextState(req, DEFAULT_VIEW_PAGE);
             }
         }
     }
 
 
 }
