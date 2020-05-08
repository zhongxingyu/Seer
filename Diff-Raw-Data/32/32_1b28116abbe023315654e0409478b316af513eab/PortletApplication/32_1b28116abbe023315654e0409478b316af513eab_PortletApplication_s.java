 /*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @team sonicteam
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 public class PortletApplication {
     static org.apache.log4j.Category cat = org.apache.log4j.Category.getInstance(PortletApplication.class.getName());
 
    String Uid;                 // Uid of the Portletapplication
    String Name;                // Name of the
    Portlet Portlet;            // Portlet
 
     public String getUid() {
         return Uid;
     }
 
     public void setUid(String uid) {
         Uid = uid;
     }
 
     public String getName() {
         return Name;
     }
 
     public void setName(String name) {
         Name = name;
     }
 
     public Portlet getPortlet() {
         return Portlet;
     }
 
     public void setPortlet(Portlet portlet) {
         Portlet = portlet;
     }
 }
 
