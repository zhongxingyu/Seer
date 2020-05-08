 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.registry;
 
 import Sirius.server.registry.rmplugin.interfaces.RMForwarder;
 
 import org.apache.log4j.Logger;
 
 import org.netbeans.api.progress.ProgressHandle;
 import org.netbeans.api.progress.ProgressHandleFactory;
 import org.netbeans.api.project.Project;
 import org.netbeans.api.project.ProjectInformation;
 import org.netbeans.spi.project.ActionProvider;
 import org.netbeans.spi.project.ProjectState;
 import org.netbeans.spi.project.ui.LogicalViewProvider;
 
 import org.openide.filesystems.FileObject;
 import org.openide.util.ImageUtilities;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.Lookups;
 
 import java.awt.EventQueue;
 
 import java.beans.PropertyChangeListener;
 
 import java.io.IOException;
 
 import java.net.MalformedURLException;
 
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 
 import java.util.Properties;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 import de.cismet.cids.abf.utilities.Connectable;
 import de.cismet.cids.abf.utilities.ConnectionEvent;
 import de.cismet.cids.abf.utilities.ConnectionListener;
 import de.cismet.cids.abf.utilities.ConnectionSupport;
 import de.cismet.cids.abf.utilities.project.NotifyProperties;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public class RegistryProject implements Project, Connectable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(RegistryProject.class);
 
     public static final String RUNTIME_PROPS = "runtime.properties";                 // NOI18N
     public static final String WEBINTERFACE_DIR = "webinterface";                    // NOI18N
     public static final String IMAGE_FOLDER = "de/cismet/cids/abf/registry/images/"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient FileObject projectDir;
     private final transient ProjectState state;
     private final transient LogicalViewProvider logicalView;
     private final transient Properties runtimeProps;
     private final transient ConnectionSupport connectionSupport;
 
     private transient Lookup lkp;
     private transient RMForwarder messageForwarder;
     private transient boolean connectionInProgress;
     private transient ProgressHandle handle;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new RegistryProject object.
      *
      * @param   projDir  DOCUMENT ME!
      * @param   state    DOCUMENT ME!
      *
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     public RegistryProject(final FileObject projDir, final ProjectState state) {
         this.projectDir = projDir;
         this.state = state;
         final FileObject fob = projDir.getFileObject(RUNTIME_PROPS);
         runtimeProps = new Properties();

         try {
             runtimeProps.load(fob.getInputStream());
         } catch (final Exception e) {
             final String message = "cannot load runtime.properties"; // NOI18N
             LOG.error(message, e);
             throw new IllegalStateException(message, e);
         }

         logicalView = new RegistryLogicalView(this);
         connectionSupport = new ConnectionSupport();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public FileObject getProjectDirectory() {
         return projectDir;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   create  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     FileObject getWebinterfaceFolder(final boolean create) {
         FileObject result = projectDir.getFileObject(WEBINTERFACE_DIR);
         if ((result == null) && create) {
             try {
                 result = projectDir.createFolder(WEBINTERFACE_DIR);
             } catch (final IOException ioe) {
                 final String message = "cannot create web " // NOI18N
                             + "interface dir '"             // NOI18N
                             + WEBINTERFACE_DIR
                             + "' in folder: "               // NOI18N
                             + projectDir;
                 LOG.error(message, ioe);
                 throw new IllegalStateException(message, ioe);
             }
         }
 
         return result;
     }
 
     @Override
     public boolean isConnected() {
         return messageForwarder != null;
     }
 
     @Override
     public void setConnected(final boolean connected) {
         connectionInProgress = true;
         fireConnectionStatusIndeterminate();
         if (connected) {
             final Thread t = new Thread() {
 
                     @Override
                     public void run() {
                         try {
                             messageForwarder = (RMForwarder)Naming.lookup(
                                     "rmi://"                                         // NOI18N
                                             + runtimeProps.getProperty("registryIP") // NOI18N
                                             + ":1099/RMRegistryServer");             // NOI18N
                         } catch (final NotBoundException ex) {
                             final String message = "rmi server not running";         // NOI18N
                             LOG.error(message, ex);
                             throw new IllegalStateException(message, ex);
                         } catch (final MalformedURLException ex) {
                             final String message = "server url not valid";           // NOI18N
                             LOG.error(message, ex);
                             throw new IllegalStateException(message, ex);
                         } catch (final RemoteException ex) {
                             final String message = "could not connect to server";    // NOI18N
                             LOG.error(message, ex);
                             throw new IllegalStateException(message, ex);
                         } finally {
                             connectionInProgress = false;
                             fireConnectionStatusChanged();
                         }
                     }
                 };
             t.start();
         } else {
             messageForwarder = null;
             connectionInProgress = false;
             fireConnectionStatusChanged();
         }
     }
 
     @Override
     public Lookup getLookup() {
         if (lkp == null) {
             lkp = Lookups.fixed(
                     new Object[] {
                         this,                     // project spec requires a project be in its own lookup
                         state,                    // allow outside code to mark the project eg. need saving
                         new ActionProviderImpl(), // Provides standard actions
                         loadProperties(),         // The project properties
                         new Info(),               // Project information implementation
                         logicalView,              // Logical view of project implementation
                     });
         }
         return lkp;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     private Properties loadProperties() {
         final FileObject fob = projectDir.getFileObject(
                 RegistryProjectFactory.PROJECT_DIR
                         + "/" // NOI18N
                         + RegistryProjectFactory.PROJECT_PROPFILE);
         final Properties properties = new NotifyProperties(state);
         if (fob != null) {
             try {
                 properties.load(fob.getInputStream());
             } catch (final IOException e) {
                 final String message = "could not load project props"; // NOI18N
                 LOG.error(message, e);
                 throw new IllegalStateException(message, e);
             }
         }
         return properties;
     }
 
     @Override
     public void addConnectionListener(final ConnectionListener cl) {
         connectionSupport.addConnectionListener(cl);
     }
 
     @Override
     public void removeConnectionListener(final ConnectionListener cl) {
         connectionSupport.removeConnectionListener(cl);
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void fireConnectionStatusChanged() {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     handle.finish();
                 }
             });
 
         final ConnectionEvent event = new ConnectionEvent(state, isConnected(), isConnectionInProgress());
         connectionSupport.fireConnectionStatusChanged(event);
     }
 
     /**
      * DOCUMENT ME!
      */
     protected void fireConnectionStatusIndeterminate() {
         if (isConnected()) {
             handle = ProgressHandleFactory.createHandle(
                     org.openide.util.NbBundle.getMessage(
                         RegistryProject.class,
                         "RegistryProject.handle.message.disconnectFromRegistry", // NOI18N
                         runtimeProps.getProperty("registryIP"))); // NOI18N
         } else {
             handle = ProgressHandleFactory.createHandle(
                     org.openide.util.NbBundle.getMessage(
                         RegistryProject.class,
                         "RegistryProject.handle.message.connectToRegistry", // NOI18N
                         runtimeProps.getProperty("registryIP"))); // NOI18N
         }
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     handle.start();
                     handle.switchToIndeterminate();
                 }
             });
 
         final ConnectionEvent event = new ConnectionEvent(state, isConnected(), isConnectionInProgress());
         connectionSupport.fireConnectionStatusChanged(event);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public RMForwarder getMessageForwarder() {
         return messageForwarder;
     }
 
     @Override
     public boolean isConnectionInProgress() {
         return connectionInProgress;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ActionProviderImpl implements ActionProvider {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String[] getSupportedActions() {
             return new String[0];
         }
 
         @Override
         public void invokeAction(final String string, final Lookup lookup) throws IllegalArgumentException {
             // do nothing
         }
 
         @Override
         public boolean isActionEnabled(final String string, final Lookup lookup) throws IllegalArgumentException {
             return false;
         }
     }
 
     /**
      * Implementation of project system's ProjectInformation class.
      *
      * @version  $Revision$, $Date$
      */
     private final class Info implements ProjectInformation {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient ImageIcon icon;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new Info object.
          */
         Info() {
             icon = new ImageIcon(ImageUtilities.loadImage(IMAGE_FOLDER + "registry.png")); // NOI18N
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Icon getIcon() {
             return icon;
         }
 
         @Override
         public String getName() {
             return projectDir.getName();
         }
 
         @Override
         public String getDisplayName() {
             return getName();
         }
 
         @Override
         public void addPropertyChangeListener(final PropertyChangeListener p) {
             // do nothing, won't change
         }
 
         @Override
         public void removePropertyChangeListener(final PropertyChangeListener p) {
             // do nothing, won't change
         }
 
         @Override
         public Project getProject() {
             return RegistryProject.this;
         }
     }
 }
