 /*
  * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.layout;
 
 import org.gridlab.gridsphere.layout.PortletPage;
 import org.gridlab.gridsphere.layout.PortletTab;
 import org.gridlab.gridsphere.layout.PortletTabbedPane;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletUser;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 
 import javax.servlet.UnavailableException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class UserLayoutPortlet extends ActionPortlet {
 
     // JSP pages used by this portlet
     public static final String VIEW_JSP = "layout/view.jsp";
 
     // Portlet services
     private LayoutManagerService layoutMgr = null;
     private UserManagerService userManagerService = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         try {
             this.layoutMgr = (LayoutManagerService) config.getContext().getService(LayoutManagerService.class);
             this.userManagerService = (UserManagerService) config.getContext().getService(UserManagerService.class);
 
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
 
         DEFAULT_VIEW_PAGE = "doRender";
         DEFAULT_HELP_PAGE = "layout/help.jsp";
 
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void createNewTab(FormEvent event) throws PortletException, IOException {
 
         String tabName = event.getTextFieldBean("userTabTF").getValue();
        RadioButtonBean rb = event.getRadioButtonBean("colsRB");
        String rbtype = rb.getSelectedValue();
 
         int cols = Integer.valueOf(rbtype).intValue();

         PortletTabbedPane pane = layoutMgr.getUserTabbedPane(event.getPortletRequest());
         if (pane != null) {
         Iterator it = pane.getPortletTabs().iterator();
         while (it.hasNext()) {
             PortletTab tab = (PortletTab)it.next();
             if (tab.getLabel().equals(tabName + "Tab")) {
                 createErrorMessage(event, this.getLocalizedText(event.getPortletRequest(), "LAYOUT_SAMETAB_ERROR"));
                 return;
             }
         }
         }
 
         pane = layoutMgr.createUserTabbedPane(event.getPortletRequest(), cols, tabName);
 
 
         PortletTab tab = pane.getLastPortletTab();
 
         pane.save();
         PortletPage page = layoutMgr.getPortletPage(event.getPortletRequest());
         PortletTabbedPane mypane = page.getPortletTabbedPane();
         List tabs = mypane.getPortletTabs();
         tabs.add(0, tab);
         layoutMgr.reloadPage(event.getPortletRequest());
     }
 
     public void deleteTab(FormEvent event) throws IOException {
         String label = event.getAction().getParameter("tabid");
 
         PortletTabbedPane pane = layoutMgr.getUserTabbedPane(event.getPortletRequest());
         List tabs = pane.getPortletTabs();
         Iterator it = tabs.iterator();
         while (it.hasNext()) {
             PortletTab tab = (PortletTab) it.next();
             if (tab.getLabel().equals(label)) {
                 it.remove();
             }
         }
         pane.save();
         PortletPage page = layoutMgr.getPortletPage(event.getPortletRequest());
         PortletTabbedPane mypane = page.getPortletTabbedPane();
         it = mypane.getPortletTabs().iterator();
         while (it.hasNext()) {
             PortletTab tab = (PortletTab) it.next();
             if (tab.getLabel().equals(label)) {
                 it.remove();
             }
         }
         layoutMgr.reloadPage(event.getPortletRequest());
     }
 
     public void saveTab(FormEvent event) throws IOException {
 
         String lang = event.getPortletRequest().getLocale().getLanguage();
         String name = event.getPortletRequest().getParameter("myTF");
         String label = event.getAction().getParameter("tabid");
 
         PortletTabbedPane pane = layoutMgr.getUserTabbedPane(event.getPortletRequest());
         List tabs = pane.getPortletTabs();
         Iterator it = tabs.iterator();
         while (it.hasNext()) {
             PortletTab tab = (PortletTab) it.next();
             if (tab.getLabel().equals(label)) {
                 tab.setTitle(lang, name);
             }
         }
         pane.save();
         PortletPage page = layoutMgr.getPortletPage(event.getPortletRequest());
         PortletTabbedPane mypane = page.getPortletTabbedPane();
         it = mypane.getPortletTabs().iterator();
         while (it.hasNext()) {
             PortletTab tab = (PortletTab) it.next();
             if (tab.getLabel().equals(label)) {
                 tab.setTitle(lang, name);
             }
         }
         layoutMgr.reloadPage(event.getPortletRequest());
     }
 
     public void doRender(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
 
         req.setAttribute("lang", req.getLocale().getLanguage());
 
         ListBoxBean themeLB = event.getListBoxBean("themeLB");
         PortletPage page = layoutMgr.getPortletPage(req);
         String theme = page.getTheme();
         themeLB.clear();
         String themes = getPortletSettings().getAttribute("supported-themes");
         StringTokenizer st = new StringTokenizer(themes, ",");
         while (st.hasMoreTokens()) {
             ListBoxItemBean lb = new ListBoxItemBean();
             String val = (String) st.nextElement();
             lb.setValue(val.trim());
             if (val.trim().equalsIgnoreCase(theme)) lb.setSelected(true);
             themeLB.addBean(lb);
         }
 
         PortletTabbedPane pane = layoutMgr.getUserTabbedPane(req);
         List tabs;
         if (pane != null) {
             tabs = pane.getPortletTabs();
         } else {
             tabs = new ArrayList();
         }
         req.setAttribute("tabs", tabs);
         setNextState(req, VIEW_JSP);
     }
 
     public void saveTheme(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         ListBoxBean themeLB = event.getListBoxBean("themeLB");
         String theme = themeLB.getSelectedValue();
 
         User user = req.getUser();
         SportletUser acctReq = userManagerService.editUser(user);
         if (user != null) {
             acctReq.setAttribute(User.THEME, theme);
             userManagerService.saveUser(acctReq);
         }
 
         PortletPage page = layoutMgr.getPortletPage(req);
         page.setTheme(theme);
         layoutMgr.reloadPage(req);
     }
 
     private void createErrorMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(MessageStyle.MSG_ERROR);
         msgBox.setValue(msg);
     }
 }
