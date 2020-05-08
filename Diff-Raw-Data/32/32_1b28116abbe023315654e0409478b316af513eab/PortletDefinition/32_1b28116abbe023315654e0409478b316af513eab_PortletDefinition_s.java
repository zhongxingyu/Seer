 /*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @team sonicteam
  * @version $Id$
  *
  * Masterobject for the portlet.xml
  */
 
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 import java.util.Vector;
 
 public class PortletDefinition {
     static org.apache.log4j.Category cat = org.apache.log4j.Category.getInstance(PortletDefinition.class.getName());
 
    Vector PortletAppList;
 
     /**
      * gets the List of PortletApps
      *
      * @return vector of portletapps
      */
    public Vector getPortletAppList() {
         return PortletAppList;
     }
 
     /**
      * set the Vector of portletapps
      * (never used, only testing)
      *
      */
     public void setPortletAppList(Vector portletAppList) {
        PortletAppList = portletAppList;
     }
 
 
     /**
      * adds a portletapp to the List
      * (never used, only testing)
      */
     public void addPortletApp(PortletApplication app) {
         PortletAppList.add(app);
     }
 }
 
 
