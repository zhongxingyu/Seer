 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id: TitleBar.java 4496 2006-02-08 20:27:04Z wehrens $
  */
 package org.gridlab.gridsphere.layout.view.standard;
 
 import org.gridlab.gridsphere.layout.PortletComponent;
 import org.gridlab.gridsphere.layout.PortletTitleBar;
 import org.gridlab.gridsphere.layout.view.BaseRender;
 import org.gridlab.gridsphere.layout.view.Render;
import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class TitleBar extends BaseRender implements Render {
 
     /**
      * Constructs an instance of PortletTitleBar
      */
     public TitleBar() {
     }
 
     public StringBuffer doStart(GridSphereEvent event, PortletComponent comp) {
         PortletTitleBar titleBar = (PortletTitleBar)comp;
         PortletRequest req = event.getPortletRequest();
         StringBuffer titleBuffer = new StringBuffer();
         if (titleBar.isActive()) {
             titleBuffer.append("<div class=\"window-title-active\">");
         } else {
             titleBuffer.append("<div class=\"window-title-inactive\">");
         }
         titleBar.setActive(false);
         List modeLinks = titleBar.getModeLinks();
         titleBuffer.append("<div class=\"window-icon-left\">");        
         if (modeLinks != null) {
             Iterator modesIt = modeLinks.iterator();
             PortletTitleBar.PortletModeLink mode;
             if (!modesIt.hasNext()) {
                 titleBuffer.append("&nbsp;");
             }
            String tmp = "";
             while (modesIt.hasNext()) {
                 mode = (PortletTitleBar.PortletModeLink) modesIt.next();
                tmp = "<a href=\"" + mode.getHref() + "\"><img src=\"" + req.getContextPath() +"/themes/" + titleBar.getRenderKit() + "/" + titleBar.getTheme() + "/" + mode.getImageSrc() + "\" title=\"" + mode.getAltTag() + "\" alt=\"" + mode.getAltTag() + "\"";

                 if (!mode.getCursor().equals("")) {
                    tmp += " style=\"cursor: " + mode.getCursor() + ";\"";
                 }
                tmp += "\" /></a>"; /// Removed File.separator(s)
             }
             titleBuffer.append(tmp);
         }
         titleBuffer.append("</div>");
         titleBuffer.append("<div class=\"window-title-name\">");
         return titleBuffer;
     }
 
     public StringBuffer doEnd(GridSphereEvent event, PortletComponent comp) {
         PortletTitleBar titleBar = (PortletTitleBar)comp;
         PortletRequest req = event.getPortletRequest();
         StringBuffer titleBuffer = new StringBuffer();
         titleBuffer.append("</div>");
         // Output window state icons
         List windowLinks = titleBar.getWindowLinks();
         titleBuffer.append("<div class=\"window-icon-right\">");        
         String tmp = "";
         if (windowLinks != null) {
             Iterator windowsIt = windowLinks.iterator();
             PortletTitleBar.PortletStateLink state;
             while (windowsIt.hasNext()) {
                 state = (PortletTitleBar.PortletStateLink) windowsIt.next();
                 tmp += "<a href=\"" + state.getHref() + "\"><img src=\"" + req.getContextPath() +"/themes/" + titleBar.getRenderKit() + "/" + titleBar.getTheme() + "/" + state.getImageSrc() + "\" title=\"" + state.getAltTag() + "\" alt=\"" + state.getAltTag() + "\" /></a>";
             }
         }
         titleBuffer.append(tmp);
         titleBuffer.append("</div>");        
         titleBuffer.append("</div>");
         tmp = null;
         return titleBuffer;
     }
 
 
 }
 
