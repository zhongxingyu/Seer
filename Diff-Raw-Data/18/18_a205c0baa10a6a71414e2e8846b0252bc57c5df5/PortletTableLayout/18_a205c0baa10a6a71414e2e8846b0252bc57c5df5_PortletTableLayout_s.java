 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.layout.view.TableLayoutView;
 import org.gridlab.gridsphere.portlet.PortletGroup;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletRole;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portlet.impl.SportletRoleInfo;
 import org.gridlab.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridlab.gridsphere.portletcontainer.ConcretePortlet;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridlab.gridsphere.portletcontainer.PortletRegistry;
 import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * The <code>PortletTableLayout</code> is a concrete implementation of the <code>PortletFrameLayout</code>
  * that organizes portlets into a grid with a provided number of columns.
  * Portlets are arranged in column-wise order starting from the left most column.
  */
 public class PortletTableLayout extends PortletFrameLayout implements Serializable, Cloneable {
 
     public static final String PORTLET_COL = "gs_col";
     public static final String PORTLET_ADD_ACTION = "gs_addPortlet";
     public static final String PORTLET_NO_ACTION = "gs_none";
 
     protected transient TableLayoutView tableView = null;
     private transient GroupManagerService groupService = null;
 
     /**
      * Constructs an instance of PortletTableLayout
      */
     public PortletTableLayout() {
     }
 
     public List init(PortletRequest req, List list) {
         tableView = (TableLayoutView)getRenderClass("TableLayout");
         PortletServiceFactory factory = SportletServiceFactory.getInstance();
         try {
             groupService = (GroupManagerService)factory.createPortletService(GroupManagerService.class, null, true);
         } catch (PortletServiceException e) {
             System.err.println("Unable to init Cache service! " + e.getMessage());
         }
         return super.init(req, list);
     }
 
     private PortletComponent getMaximizedComponent(List components) {
         PortletComponent p;
         List scomponents = Collections.synchronizedList(components);
         synchronized (scomponents) {
             for (int i = 0; i < scomponents.size(); i++) {
                 p = (PortletComponent) scomponents.get(i);
                 if (p instanceof PortletLayout) {
                     PortletComponent layout = this.getMaximizedComponent(((PortletLayout) p).getPortletComponents());
                     if (layout != null) {
                         p = layout;
                     }
                 }
                 if (p.getWidth().equals("100%")) {
                     return p;
                 }
             }
         }
         return null;
     }
 
     protected void addPortlet(GridSphereEvent event) {
 
         PortletRequest req = event.getPortletRequest();
 
         String portletId = req.getParameter(PORTLET_ADD_ACTION);
 
         if (portletId.equals(PORTLET_NO_ACTION)) return;
 
         String colStr = req.getParameter(PORTLET_COL);
         int col = Integer.valueOf(colStr).intValue();
 
         // first loop thru rows to see if one is empty for the requested column
         if (!components.isEmpty()) {
             Object o = components.get(0);
             if (o instanceof PortletFrameLayout) {
                 PortletFrameLayout r = (PortletFrameLayout) o;
                 List cols = r.getPortletComponents();
 
                 Object c = cols.get(col);
 
                 if (c instanceof PortletFrameLayout) {
                     PortletFrameLayout existingColumn = (PortletFrameLayout) c;
 
                     if (!portletId.equals("")) {
                         PortletFrame frame = new PortletFrame();
                         frame.setPortletClass(portletId);
                         existingColumn.addPortletComponent(frame);
                     }
                 }
             }
         }
 
         req.setAttribute(SportletProperties.INIT_PAGE, "true");
     }
 
     public void customActionPerformed(GridSphereEvent event) throws
             PortletLayoutException, IOException {
 
         if (event.hasAction()) {
             if (event.getAction().getName().equals(PORTLET_ADD_ACTION)) {
                 addPortlet(event);
             }
         }
     }
 
     public Map getAvailablePortletsToAdd(GridSphereEvent event) {
         PortletRegistry registry = PortletRegistry.getInstance();
         PortletRequest req = event.getPortletRequest();
         Locale locale = req.getLocale();

        //PrintWriter out = res.getWriter();

         List groups = (List) req.getGroups();

         Map availPortlets = new HashMap();
         Iterator it = groups.iterator();
         while (it.hasNext()) {
             String group = (String) it.next();
             PortletGroup g = groupService.getGroup(group);
             //System.err.println("group= " + g.getName());
             if (g.equals(req.getAttribute(SportletProperties.PORTLET_GROUP))) continue;
             Iterator sit = g.getPortletRoleList().iterator();
             //PortletRole role = (PortletRole) groups.get(g);
             while (sit.hasNext()) {
                 SportletRoleInfo info = (SportletRoleInfo) sit.next();
                 String appID = PortletRegistry.getApplicationPortletID(info.getPortletClass());

                 //System.err.println("info class= " + info.getPortletClass());

                 PortletRole reqRole = info.getSportletRole();

                 //System.err.println("role= " + info.getSportletRole());
                if (req.getRoles().contains(reqRole))
                     if (!availPortlets.containsKey(appID)) {
                         ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
                         if (appPortlet != null) {
                             //System.err.println("appID= " + appID);
                             ConcretePortlet concPortlet = appPortlet.getConcretePortlet(appID);
                             if (concPortlet != null) {
                                 String dispName = concPortlet.getDisplayName(locale);

                                 //System.err.println("show portlet = " + dispName);
                                 availPortlets.put(appID, dispName);
                             }
                         } else {
                             //System.err.println("app portlet was null for " + appID);
                         }
                     }
 
             }
         }
         return availPortlets;
     }
 
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         super.doRender(event);
 
         StringBuffer table = new StringBuffer();
         PortletComponent p;
 
         // check if one window is maximized
 
         for (int i = 0; i < components.size(); i++) {
             p = (PortletComponent) components.get(i);
             if (p instanceof PortletLayout) {
                 PortletComponent maxi = getMaximizedComponent(components);
                 if (maxi != null) {
                     table.append(tableView.doStartMaximizedComponent(event, this));
                     maxi.doRender(event);
                     table.append(maxi.getBufferedOutput(event.getPortletRequest()));
                     table.append(tableView.doEndMaximizedComponent(event, this));
                     if ((canModify) && (!hasFrameMaximized)) {
                         table.append(tableView.doRenderUserSelects(event, this));
                     }
                     event.getPortletRequest().setAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr, table);
                     return;
                 }
             }
         }
 
         table.append(tableView.doStart(event, this));
 
         for (int i = 0; i < components.size(); i++) {
             p = (PortletComponent)components.get(i);
             table.append(tableView.doStartBorder(event, p));
             if (p.getVisible()) {
                 p.doRender(event);
                 table.append(p.getBufferedOutput(event.getPortletRequest()));
             }
             table.append(tableView.doEndBorder(event, this));
         }
 
         /** setup bottom add portlet listbox */
         if ((canModify) && (!hasFrameMaximized)) {
             table.append(tableView.doRenderUserSelects(event, this));
         }
 
         table.append(tableView.doEnd(event, this));
 
         event.getPortletRequest().setAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr, table);
 
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletTableLayout g = (PortletTableLayout) super.clone();
         g.style = this.style;
         return g;
     }
 
 }
