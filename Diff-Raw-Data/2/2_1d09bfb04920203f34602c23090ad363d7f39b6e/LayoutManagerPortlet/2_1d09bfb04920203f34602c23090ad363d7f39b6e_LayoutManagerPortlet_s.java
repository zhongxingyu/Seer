 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.admin.layout;
 
 import org.gridlab.gridsphere.layout.PortletPage;
 import org.gridlab.gridsphere.layout.PortletTabRegistry;
 import org.gridlab.gridsphere.layout.PortletTabbedPane;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigSettings;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 
 import javax.servlet.UnavailableException;
 import java.io.*;
 import java.util.*;
 
 public class LayoutManagerPortlet extends ActionPortlet {
 
     // JSP pages used by this portlet
     public static final String VIEW_JSP = "admin/layout/view.jsp";
     public static final String EDIT_JSP = "admin/layout/edit.jsp";
 
     // Portlet services
     private LayoutManagerService layoutMgr = null;
     private PortalConfigService portalConfigService = null;
     private AccessControlManagerService aclManagerService = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         log.debug("Entering initServices()");
         try {
             this.layoutMgr = (LayoutManagerService) config.getContext().getService(LayoutManagerService.class);
             this.portalConfigService = (PortalConfigService) config.getContext().getService(PortalConfigService.class);
             aclManagerService = (AccessControlManagerService) this.getConfig().getContext().getService(AccessControlManagerService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
         log.debug("Exiting initServices()");
         //portletMgr = PortletManager.getInstance();
 
         DEFAULT_VIEW_PAGE = "doShowLayout";
         DEFAULT_HELP_PAGE = "admin/layout/help.jsp";
 
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void refreshLayout(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         layoutMgr.refreshPage(req);
     }
 
     public void doShowLayout(FormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getPortletRequest();
 
         TextAreaBean ta = event.getTextAreaBean("bannerTA");
 
         String filename = this.getPortletConfig().getContext().getRealPath("/html/pagehead.html");
         BufferedReader reader = new BufferedReader(new FileReader(filename));
         String line = null;
         StringBuffer sb = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             sb.append(line);
             sb.append("\n");
         }
         reader.close();
         ta.setValue(sb.toString());
 
         String themesPath = getPortletConfig().getContext().getRealPath("/themes");
 
         System.err.println("themes path=" + themesPath);
 
         String[] themes = null;
         File f = new File(themesPath);
         if (f.isDirectory()) {
             themes = f.list();
         }
 
         String defaultTheme = portalConfigService.getPortalConfigSettings().getDefaultTheme();
         ListBoxBean lb = event.getListBoxBean("themesLB");
         ListBoxItemBean item;
         for (int i = 0; i < themes.length; i++) {
             item = new ListBoxItemBean();
             item.setValue(themes[i]);
             item.setName(themes[i]);
             if (themes[i].equals(defaultTheme)) item.setSelected(true);
             lb.addBean(item);
         }
 
         Map tabs = PortletTabRegistry.getApplicationTabs();
 
         List tabNames = new ArrayList();
         Iterator it = tabs.keySet().iterator();
         while (it.hasNext()) {
             tabNames.add((String) it.next());
         }
 
         req.setAttribute("tabNames", tabNames);
 
         Map groups = PortletTabRegistry.getGroupTabs();
         it = groups.keySet().iterator();
         Map groupNames = new HashMap();
 
         User user = req.getUser();
 
         String name;
         PortletGroup group;
         while (it.hasNext()) {
             name = (String) it.next();
             group = aclManagerService.getGroupByName(name);
             if (group != null) {
                 groupNames.put(name, group.getDescription());
             }
         }
         req.setAttribute("groupNames", groupNames);
 
         setNextState(req, VIEW_JSP);
     }
 
     public void saveBanner(FormEvent event) throws PortletException, IOException {
         this.checkSuperRole(event);
         TextAreaBean ta = event.getTextAreaBean("bannerTA");
         String newText = ta.getValue();
         String filename = this.getPortletConfig().getContext().getRealPath("/html/pagehead.html");
 
         FileWriter f = new FileWriter(filename);
 
         f.write(newText);
         f.close();
 
     }
 
     public void saveDefaultTheme(FormEvent event) throws PortletException, IOException {
         this.checkSuperRole(event);
         PortletRequest req = event.getPortletRequest();
         ListBoxBean themesLB = event.getListBoxBean("themesLB");
         String theme = themesLB.getSelectedValue();
         themesLB.clear();
         if (!theme.equals("")) {
             PortalConfigSettings configSettings = portalConfigService.getPortalConfigSettings();
             configSettings.setDefaultTheme(theme);
             portalConfigService.savePortalConfigSettings(configSettings);
         }
         PortletPage page = layoutMgr.getPortletPage(req);
 
         page.setTheme(theme);
         User user = req.getUser();
         theme = (String) user.getAttribute(User.THEME);
         if (theme != null) page.setTheme(theme);
         layoutMgr.reloadPage(event.getPortletRequest());
     }
 
     public void importLayout(FormEvent event) throws PortletException, IOException {
         this.checkSuperRole(event);
 
         ListBoxBean appsLB = event.getListBoxBean("appsLB");
         String val = appsLB.getSelectedValue();
 
         HiddenFieldBean groupHF = event.getHiddenFieldBean("layoutHF");
         String thisgroup = groupHF.getValue();
         String thisFile = PortletTabRegistry.getTabDescriptorPath(thisgroup);
 
         //String groupFile = PortletTabRegistry.getTabDescriptorPath(val);
 
         PortletTabbedPane groupPane = PortletTabRegistry.getGroupTabs(val);
 
         groupPane.setLayoutDescriptor(thisFile);
 
         groupPane.save();
         try {
             PortletTabRegistry.reloadTab(val, thisFile);
             saveLayout(event);
 
             String groupLayoutPath = PortletTabRegistry.getTabDescriptorPath(thisgroup);
             editGroup(event, thisgroup, groupLayoutPath);
 
         } catch (Exception e) {
             log.error("Unable to reload tab", e);
 
         }
 
 
     }
 
     public void editGroupLayout(FormEvent event) throws PortletException, IOException {
 
         String group = event.getAction().getParameter("group");
 
         String groupLayoutPath = PortletTabRegistry.getTabDescriptorPath(group);
         editGroup(event, group, groupLayoutPath);
 
     }
 
     public void editGroup(FormEvent event, String group, String layoutPath) throws PortletException, IOException {
 
         PortletRequest req = event.getPortletRequest();
 
         Boolean allowImport = Boolean.TRUE;
 
         if (PortletTabRegistry.getApplicationTabs(group) != null) {
             allowImport = Boolean.FALSE;
         }
 
         TextAreaBean ta = event.getTextAreaBean("layoutFile");
         HiddenFieldBean hf = event.getHiddenFieldBean("layoutHF");
         hf.setName("group");
         hf.setValue(group);
         req.setAttribute("name", group);
         req.setAttribute("allowImport", allowImport);
 
         ListBoxBean appsLB = event.getListBoxBean("appsLB");
 
         Map tabs = PortletTabRegistry.getGroupTabs();
 
         Iterator it = tabs.keySet().iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = new ListBoxItemBean();
             String name = (String) it.next();
             item.setName(name);
             item.setValue(name);
             appsLB.addBean(item);
         }
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(layoutPath), "UTF8"));
 
         String line = null;
         StringBuffer sb = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             sb.append(line);
             sb.append("\n");
         }
         reader.close();
         ta.setValue(sb.toString());
 
         setNextState(req, EDIT_JSP);
     }
 
     public void cancelLayout(FormEvent event) throws PortletException, IOException {
         setNextState(event.getPortletRequest(), DEFAULT_VIEW_PAGE);
     }
 
     public void saveLayout(FormEvent event) throws PortletException, IOException {
         //this.checkSuperRole(event);
 
         User user = event.getPortletRequest().getUser();
         HiddenFieldBean groupHF = event.getHiddenFieldBean("layoutHF");
         TextAreaBean ta = event.getTextAreaBean("layoutFile");
         String newText = ta.getValue();
 
         if (groupHF.getValue().equals("guest")) {
             this.checkSuperRole(event);
             String guestFile = PortletTabRegistry.getGuestLayoutFile();
             String tmpFile = guestFile + "-tmp";
             Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8"));
             byte[] text = newText.getBytes("iso-8859-1");
             String newstring = new String(text, "UTF-8");
             out.write(newstring);
             out.close();
             try {
                 PortletTabRegistry.loadPage(tmpFile);
                 copyFile(new File(tmpFile), new File(guestFile));
                 PortletTabRegistry.reloadGuestLayout();
 
                 createSuccessMessage(event, this.getLocalizedText(event.getPortletRequest(), "LAYOUTMGR_VALID_LAYOUT"));
             } catch (Exception e) {
                 createErrorMessage(event, this.getLocalizedText(event.getPortletRequest(), "LAYOUTMGR_INVALID_LAYOUT"));
             } finally {
                 File f = new File(tmpFile);
                 f.delete();
             }
             return;
         }
 
         PortletGroup group = aclManagerService.getGroupByName(groupHF.getValue());
         if (!aclManagerService.hasSuperRole(user) && !aclManagerService.hasAdminRoleInGroup(user, group)) {
             return;
         }
 
         String groupFile = PortletTabRegistry.getTabDescriptorPath(group.getName());
         log.info("saving group layout: " + group.getName());
         String tmpFile = groupFile + "-tmp";
         Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8"));
         byte[] text = newText.getBytes("iso-8859-1");
         String newstring = new String(text, "UTF-8");
         out.write(newstring);
         out.close();
 
         // first validate tab
         try {
            PortletTabRegistry.loadPage(tmpFile);
 
             copyFile(new File(tmpFile), new File(groupFile));
             PortletTabRegistry.reloadTab(groupHF.getValue(), groupFile);
             createSuccessMessage(event, this.getLocalizedText(event.getPortletRequest(), "LAYOUTMGR_VALID_LAYOUT"));
         } catch (Exception e) {
             // ok use old tab
             log.error("Unable to reload new tab!", e);
             createErrorMessage(event, this.getLocalizedText(event.getPortletRequest(), "LAYOUTMGR_INVALID_LAYOUT"));
             editGroup(event, group.getName(), tmpFile);
         } finally {
             File f = new File(tmpFile);
             f.delete();
         }
 
 
     }
 
     public void editGuestLayout(FormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getPortletRequest();
         req.setAttribute("name", "GuestUserLayout.xml");
         req.setAttribute("allowImport", Boolean.FALSE);
 
         TextAreaBean ta = event.getTextAreaBean("layoutFile");
         HiddenFieldBean hf = event.getHiddenFieldBean("layoutHF");
         hf.setName("group");
         hf.setValue("guest");
         String guestLayoutPath = PortletTabRegistry.getGuestLayoutFile();
         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(guestLayoutPath), "UTF-8"));
 
         String line = null;
         StringBuffer sb = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             sb.append(line);
             sb.append("\n");
         }
         reader.close();
         ta.setValue(sb.toString());
 
         setNextState(req, EDIT_JSP);
     }
 
 
     public void deleteLayout(FormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getPortletRequest();
 
         String group = event.getAction().getParameter("group");
 
         PortletTabRegistry.removeGroupTab(group);
         createSuccessMessage(event, this.getLocalizedText(event.getPortletRequest(), "LAYOUTMGR_DELETE_LAYOUT") + "  " + group);
         setNextState(req, VIEW_JSP);
     }
 
     private void copyFile(File in, File out) throws Exception {
         FileInputStream fis = new FileInputStream(in);
         FileOutputStream fos = new FileOutputStream(out);
         byte[] buf = new byte[1024];
         int i = 0;
         while ((i = fis.read(buf)) != -1) {
             fos.write(buf, 0, i);
         }
         fis.close();
         fos.close();
     }
 
     private void createErrorMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(TextBean.MSG_ERROR);
         msgBox.setValue(msg);
     }
 
     private void createSuccessMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(TextBean.MSG_SUCCESS);
         msgBox.setValue(msg);
     }
 }
