 /*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @team sonicteam
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 public class Portlet {
     static org.apache.log4j.Category cat = org.apache.log4j.Category.getInstance(Portlet.class.getName());
 
    String Href;
    String Id;
    String Name;
 
     public String getHref() {
         return Href;
     }
 
     public void setHref(String href) {
         Href = href;
     }
 
     public String getId() {
         return Id;
     }
 
     public void setId(String id) {
         Id = id;
     }
 
     public String getName() {
         return Name;
     }
 
     public void setName(String name) {
         Name = name;
     }
 }
 
