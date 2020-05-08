 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet;
 
 import org.gridlab.gridsphere.portlet.impl.SportletGroup;
 
import java.util.List;
import java.util.ArrayList;

 
 /**
  * The <code>PortletGroup</code> interface describes portlet group used by the
  * portal.
  *
  * @see PortletRole
  */
 public class PortletGroupFactory {
 
     private PortletGroupFactory() {}
 
     public static PortletGroup GRIDSPHERE_GROUP = SportletGroup.CORE;
 
    public List groups = new ArrayList();

    public static synchronized PortletGroup createPortletGroup(String groupName) {
         return new SportletGroup(groupName);
     }
 
 }
