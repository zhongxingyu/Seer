 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.file;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.FrameBean;
 import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
 import org.gridlab.gridsphere.provider.portletui.beans.ListBoxItemBean;
 import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
 import org.gridlab.gridsphere.provider.portletui.beans.TextBean;
 import org.gridlab.gridsphere.provider.portletui.beans.TableRowBean;
 import org.gridlab.gridsphere.provider.portletui.beans.TableCellBean;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 import org.gridlab.gridsphere.services.core.file.FileManagerService;
 
 import javax.servlet.UnavailableException;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 public class BannerPortlet extends ActionPortlet {
 
     private FileManagerService userStorage = null;
 
     private static final String CONFIGURE_JSP = "banner/configure.jsp";
     private static final String HELP_JSP = "banner/help.jsp";
     private static final String EDIT_JSP = "banner/edit.jsp";
 
     private static final String TITLE = "title";
     private static final String FILE = "file";
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         try {
             userStorage = (FileManagerService)config.getContext().getService(FileManagerService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize FileManagerService", e);
         }
         DEFAULT_VIEW_PAGE = "doViewFile";
         DEFAULT_EDIT_PAGE = "doEditViewFile";
         DEFAULT_CONFIGURE_PAGE = "doConfigureViewFile";
 
         DEFAULT_HELP_PAGE = HELP_JSP;
     }
 
     public String getTitle() {
         String defaultTitle = this.getPortletSettings().getAttribute(TITLE);
         if (defaultTitle == null)  defaultTitle = "";
         return defaultTitle;
     }
 
     public String getFileURL() {
         String defaultFileURL = this.getPortletSettings().getAttribute(FILE);
         if (defaultFileURL == null)  defaultFileURL = "";
         return defaultFileURL;
     }
 
      /**
       * Configure mode allows the displayed file to be set in PortletSettings
       *
       * @param event
       * @throws PortletException
       */
      public void doConfigureViewFile(FormEvent event) throws PortletException {
          PortletRequest req = event.getPortletRequest();
          checkAdminRole(event);
          TextFieldBean displayTitle = event.getTextFieldBean("displayTitle");
          displayTitle.setValue(getTitle());
 
          TextFieldBean displayFile = event.getTextFieldBean("displayFile");
          displayFile.setValue(getFileURL());
          setNextState(req, CONFIGURE_JSP);
      }
 
     public void setConfigureDisplayFile(FormEvent event) throws PortletException {
         log.debug("in BannerPortlet: setConfigureDisplayFile");
         checkAdminRole(event);
         PortletRequest req = event.getPortletRequest();
         TextFieldBean displayFile = event.getTextFieldBean("displayFile");
         String defaultFileURL = displayFile.getValue();
         getPortletSettings().setAttribute(FILE, defaultFileURL);
 
         TextFieldBean displayTitle = event.getTextFieldBean("displayTitle");
         String defaultTitle = displayTitle.getValue();
 
         getPortletSettings().setAttribute(TITLE, defaultTitle);
         FrameBean alert = event.getFrameBean("alert");
         try {
             getPortletSettings().store();
             alert.setValue(this.getLocalizedText(req, "BANNER_CONFIGURE"));
         } catch (IOException e) {
             log.error("Unable to save portlet settings", e);
             alert.setValue(this.getLocalizedText(req, "BANNER_FAILURE"));
             alert.setStyle("error");
         }
         setNextState(req, CONFIGURE_JSP);
     }
 
     public void setEditDisplayFile(FormEvent event) throws PortletException {
         log.debug("in BannerPortlet: setEditDisplayFile");
         checkUserRole(event);
         FrameBean alert = event.getFrameBean("alert");
         PortletRequest req = event.getPortletRequest();
         ListBoxBean lb = event.getListBoxBean("filelist");
         String file = lb.getSelectedValue();
         if (file == null) {
             log.error("did not select a file");
             alert.setValue(this.getLocalizedText(req, "BANNER_NOFILE_SELECTED"));
             alert.setStyle("error");
         }
         User user = event.getPortletRequest().getUser();
         String fileURL = userStorage.getLocationPath(user, file);
         int tmpLoc = fileURL.indexOf("/tempdir");
         fileURL = fileURL.substring(tmpLoc);
         PortletData data = req.getData();
         data.setAttribute(FILE, fileURL);
 
         TextFieldBean displayTitle = event.getTextFieldBean("displayTitle");
         String title = displayTitle.getValue();
 
         data.setAttribute(TITLE, title);
 
         try {
             data.store();
             alert.setValue(this.getLocalizedText(req, "BANNER_CONFIGURE"));
         } catch (IOException e) {
             log.error("Unable to save portlet data");
             alert.setValue(this.getLocalizedText(req, "BANNER_FAILURE"));
             alert.setStyle("error");
         }
 
         setNextState(req, EDIT_JSP);
     }
 
     /**
       * Edit mode allows the displayed file to be set in PortletData
       *
       * @param event
       * @throws PortletException
       */
     public void doEditViewFile(FormEvent event) throws PortletException {
         checkUserRole(event);
         ListBoxBean lb = event.getListBoxBean("filelist");
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         User user = req.getUser();
         String[] list = userStorage.getUserFileList(user);
         if (list == null) {
 
             String alertMsg = this.getLocalizedText(req, "BANNER_NOFILES_START");
             PortletURI mgrURI = res.createURI("filemanager", false);
             alertMsg += " " + "<a href=\"" + mgrURI.toString() + "\"/>" + " " + this.getLocalizedText(req, "BANNER_NOFILES_END");
 
             FrameBean alert = event.getFrameBean("alert");
             DefaultTableModel tm = new DefaultTableModel();
             TableRowBean tr = new TableRowBean();
             TableCellBean tc = new TableCellBean();
             TextBean tb = new TextBean();
             tb.setValue(alertMsg);
             tb.setStyle("alert");
             tc.addBean(tb);
             tr.addBean(tc);
             tm.addTableRowBean(tr);
             alert.setTableModel(tm);
         } else {
             lb.clear();
             lb.setSize(list.length + 3);
             for (int i = 0; i < list.length; i++) {
                 ListBoxItemBean item = new ListBoxItemBean();
                 item.setValue(list[i]);
                 lb.addBean(item);
             }
         }
 
         setNextState(req, EDIT_JSP);
     }
 
     public void doViewFile(FormEvent event) throws PortletException {
         log.debug("in BannerPortlet: doViewFile");
         PortletRequest request = event.getPortletRequest();
         PortletResponse response = event.getPortletResponse();
         User user = request.getUser();
         String title = getTitle();
        String fileURL = getFileURL();
         if (!(user instanceof GuestUser)) {
             PortletData data = request.getData();
             fileURL = data.getAttribute(FILE);
 
             // if user hasn't configured banner, show them help
             if (fileURL == null) {
                 setNextState(request, HELP_JSP);
                 return;
             }
         }
 
         //setNextState(request, fileURL);
         PrintWriter out = null;
         try {
             if (fileURL.equals("")) {
                 out = response.getWriter();
                 out.println(this.getLocalizedText(request, "BANNER_FILE_NOTFOUND"));
             } else {
                 getPortletConfig().getContext().include(fileURL, request, response);
             }
         } catch (IOException e) {
             out.println(this.getLocalizedText(request, "BANNER_FILE_NOTFOUND")+ " " + fileURL + "!");
             log.error("Unable to find file: " + fileURL);
         }
     }
 
     public void cancelEditFile(FormEvent event) throws PortletException {
         log.debug("in BannerPortlet: cancelEdit");
         PortletRequest req = event.getPortletRequest();
         req.setMode(Portlet.Mode.VIEW);
         setNextState(event.getPortletRequest(), DEFAULT_VIEW_PAGE);
     }
 
     public void doTitle(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         PrintWriter out = response.getWriter();
         if (request.getMode() == Portlet.Mode.VIEW) {
             User user = request.getUser();
             String title = null;
             if (!(user instanceof GuestUser)) {
                 PortletData data = request.getData();
                 title = data.getAttribute(TITLE);
                 if (title == null) title = this.getLocalizedText(request, "BANNER_HELP");
             } else {
                 title = getTitle();
             }
             out.println(title);
         } else if (request.getMode() == Portlet.Mode.HELP) {
             out.println(this.getLocalizedText(request, "BANNER_HELP"));
         } else {
             out.println(this.getLocalizedText(request, "BANNER_EDIT"));
         }
     }
 
 
 }
