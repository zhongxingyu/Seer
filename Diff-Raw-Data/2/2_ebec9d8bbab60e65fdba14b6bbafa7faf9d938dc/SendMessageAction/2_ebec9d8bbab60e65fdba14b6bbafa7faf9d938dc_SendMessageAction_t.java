 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.registry.messaging;
 
 import Sirius.server.registry.rmplugin.exception.UnableToSendMessageException;
 import Sirius.server.registry.rmplugin.util.RMUser;
 
 import org.apache.log4j.Logger;
 
 import org.openide.nodes.Node;
 import org.openide.util.HelpCtx;
 import org.openide.util.NbBundle;
 import org.openide.util.actions.CookieAction;
 import org.openide.windows.WindowManager;
 
 import java.rmi.RemoteException;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.abf.registry.RegistryProject;
 import de.cismet.cids.abf.registry.cookie.RMUserCookie;
 import de.cismet.cids.abf.registry.cookie.RegistryProjectCookie;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public final class SendMessageAction extends CookieAction {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(SendMessageAction.class);
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public String getName() {
         return NbBundle.getMessage(SendMessageAction.class, "SendMessageAction.getName().returnvalue"); // NOI18N
     }
 
     @Override
     protected String iconResource() {
         return RegistryProject.IMAGE_FOLDER + "aim.png"; // NOI18N
     }
 
     @Override
     public HelpCtx getHelpCtx() {
         return HelpCtx.DEFAULT_HELP;
     }
 
     @Override
     protected boolean asynchronous() {
         return false;
     }
 
     @Override
     protected void performAction(final Node[] nodes) {
         final Map<RMUser, RegistryProject> map = new HashMap<RMUser, RegistryProject>();
         for (final Node node : nodes) {
             final Set<RMUser> users = node.getCookie(RMUserCookie.class).getRMUsers();
             final RegistryProject project = node.getCookie(RegistryProjectCookie.class).getProject();
             for (final RMUser user : users) {
                 map.put(user, project);
             }
         }
         final String message = JOptionPane.showInputDialog(
                 WindowManager.getDefault().getMainWindow(),
                 NbBundle.getMessage(
                     SendMessageAction.class,
                     "SendMessageAction.performAction(Node[]).message",
                     map.size()));
        if ((message != null) && (!message.trim().isEmpty())) {
             for (final Entry<RMUser, RegistryProject> entry : map.entrySet()) {
                 final RegistryProject project = entry.getValue();
                 final RMUser user = entry.getKey();
                 try {
                     project.getMessageForwarder()
                             .sendMessage(
                                 user.getQualifiedName(),
                                 user.getIpAddress(),
                                 message,
                                 org.openide.util.NbBundle.getMessage(
                                     SendMessageAction.class,
                                     "SendMessageAction.performAction(Node[]).messageFromAdmin")); // NOI18N
                 } catch (final RemoteException ex) {
                     LOG.error("could not send message", ex);                                      // NOI18N
                 } catch (final UnableToSendMessageException ex) {
                     LOG.error("could not send message", ex);                                      // NOI18N
                 }
             }
         }
     }
 
     @Override
     protected int mode() {
         return CookieAction.MODE_ALL;
     }
 
     @Override
     protected Class<?>[] cookieClasses() {
         return new Class[] {
                 RMUserCookie.class,
                 RegistryProjectCookie.class
             };
     }
 }
