 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.DescriptorException;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.impl.GuestUser;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.HashMap;
 
 public class PortletLayoutEngine {
 
     private static PortletLog log = SportletLog.getInstance(PortletLayoutEngine.class);
 
     private static PortletLayoutEngine instance = new PortletLayoutEngine();
 
     private String layoutMappingPath;
 
     private PortletLayoutDescriptor guestLayout = null;
     private String guestLayoutPath = null;
     private PortletContainer guestContainer;
     private String userLayoutDir = null;
 
     private String error = "";
 
     private Map userLayouts = new HashMap();
 
     private PortletLayoutEngine() {}
 
     /**
      *
      */
     public static PortletLayoutEngine getInstance() {
         return instance;
     }
 
     public void init() throws IOException, DescriptorException {
         userLayoutDir = GridSphereConfig.getProperty(GridSphereConfigProperties.USER_LAYOUT_DIR);
         layoutMappingPath = GridSphereConfig.getProperty(GridSphereConfigProperties.LAYOUT_MAPPING_XML);
         guestLayoutPath =  GridSphereConfig.getProperty(GridSphereConfigProperties.LAYOUT_XML);
         guestLayout = new PortletLayoutDescriptor(guestLayoutPath, layoutMappingPath);
         guestContainer = guestLayout.getPortletContainer();
         guestContainer.init(new ArrayList());
     }
 
     public PortletContainer getPortletContainer(User user) {
         if (!userLayouts.containsKey(user)) return null;
         PortletLayoutDescriptor pld = (PortletLayoutDescriptor)userLayouts.get(user);
         return pld.getPortletContainer();
     }
 
     public void removeUser(User user) {
         if (userLayouts.containsKey(user)) {
             userLayouts.remove(user);
         }
     }
 
     public PortletContainer getPortletContainer(GridSphereEvent event) throws PortletLayoutException {
         // if user is guest then use guest template
         PortletLayoutDescriptor pld = null;
         PortletContainer pc = null;
 
         User user = event.getSportletRequest().getUser();
         System.err.println(user.getFamilyName());
         if (user.getID().equals(GuestUser.getInstance().getID())) {
             return guestContainer;
 
             // Check if we have user's layout already
         } else if (userLayouts.containsKey(user)) {
 
             pld = (PortletLayoutDescriptor)userLayouts.get(user);
             pc = pld.getPortletContainer();
 
             // If not we try to load it in (creating new one if necessary)
         } else {
             try {
                 pld = loadUserLayout(user);
                 pc = pld.getPortletContainer();
                 pc.init(new ArrayList());
                 pc.login(event);
             } catch (Exception e) {
                 log.error("Unable to loadUserLayout for user: " + user, e);
                 throw new PortletLayoutException("Unable to deserialize user layout from layout descriptor: " + e.getMessage());
             }
             userLayouts.put(user, pld);
         }
         return pc;
     }
 
    public void service(GridSphereEvent event) throws PortletLayoutException {
         log.debug("in service()");
         boolean doLayoutAction = false;
         PortletContainer pc = null;
 
        pc = getPortletContainer(event);

         // XXX: How do we signal a user has logged out so we can userLayouts.remove(user)???
         try {
             pc.actionPerformed(event);
             pc = getPortletContainer(event);
             pc.doRender(event);
        } catch (IOException e) {
             error = e.getMessage();
            log.error("Caught IOException: ", e);
            throw new PortletLayoutException("Caught IOException", e);
         }
     }
 
     public void doRenderError(PortletRequest req, PortletResponse res, Throwable t) {
         PrintWriter out = null;
         try {
             out = res.getWriter();
         } catch (IOException e) {
             log.error("in doRenderError: ", e);
         }
         out.println("<h>Portlet Layout Engine unable to render!</h>");
         out.println("<b>" + error + "</b>");
     }
 
     protected PortletLayoutDescriptor loadUserLayout(User user) throws DescriptorException, IOException {
         // load in layout.xml file
 
         if (userLayoutDir == null) {
             throw new DescriptorException("Unable to get user layout directory info from web.xml. Please specify user-layouts-dir in web.xml.");
         }
         File layDir = new File(userLayoutDir);
         if (!layDir.exists()) {
             layDir.mkdir();
         }
 
         String layoutPath = getUserLayoutPath(user);
 
         File f = new File(layoutPath);
 
         // if no layout file exists for user, make new one from template
         if (!f.exists()) {
             f.createNewFile();
             copyFile(new File(guestLayoutPath), f);
         }
 
         PortletLayoutDescriptor userLayout = new PortletLayoutDescriptor(layoutPath, layoutMappingPath);
 
         return userLayout;
     }
 
     public void saveUserLayout(User user) throws DescriptorException, IOException {
 
         PortletLayoutDescriptor pld = (PortletLayoutDescriptor)userLayouts.get(user);
         if (pld == null) {
             throw new DescriptorException("Layout does not exist for user: " + user.getID());
         }
         String userLayoutPath = getUserLayoutPath(user);
         pld.save(userLayoutPath, layoutMappingPath);
     }
 
     protected String getUserLayoutPath(User user) {
         return userLayoutDir + "/" + user.getID();
     }
 
     protected void copyFile(File oldFile, File newFile) throws IOException {
         // Destination and streams
         log.info("in copyFile(): oldFile: " + oldFile.getAbsolutePath() + " newFile: " + newFile.getCanonicalPath());
         FileInputStream fis = new FileInputStream(oldFile);
         FileOutputStream fos = new FileOutputStream(newFile);
 
         // Amount of data to copy
         long fileLength = oldFile.length();
         byte[] bytes = new byte[1024]; // 1K at a time
         int length = 0;
         long totalLength = 0;
         while (length > -1) {
             length = fis.read(bytes);
             if (length > 0) {
                 fos.write(bytes, 0, length);
                 totalLength += length;
             }
         }
         // Test that we copied all the data
         if (fileLength != totalLength) {
             throw new IOException("File copy size missmatch");
         }
         fos.flush();
         fos.close();
         fis.close();
     }
 
 }
