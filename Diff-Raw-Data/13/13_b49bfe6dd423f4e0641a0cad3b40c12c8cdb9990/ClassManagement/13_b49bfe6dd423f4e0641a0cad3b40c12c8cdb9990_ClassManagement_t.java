 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.nodes;
 
 import org.apache.log4j.Logger;
 
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.util.WeakListeners;
 import org.openide.util.actions.CallableSystemAction;
 
 import java.awt.Image;
 
 import java.io.IOException;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.Action;
 
 import de.cismet.cids.abf.domainserver.RefreshAction;
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.ProjectChildren;
 import de.cismet.cids.abf.domainserver.project.ProjectNode;
 import de.cismet.cids.abf.domainserver.project.cidsclass.CheckRightsAction;
 import de.cismet.cids.abf.domainserver.project.cidsclass.CidsClassNode;
 import de.cismet.cids.abf.domainserver.project.cidsclass.ClassManagementContextCookie;
 import de.cismet.cids.abf.domainserver.project.cidsclass.ImportClassesAction;
 import de.cismet.cids.abf.domainserver.project.cidsclass.NewCidsClassWizardAction;
 import de.cismet.cids.abf.utilities.Comparators;
 import de.cismet.cids.abf.utilities.ConnectionEvent;
 import de.cismet.cids.abf.utilities.ConnectionListener;
 import de.cismet.cids.abf.utilities.Refreshable;
 import de.cismet.cids.abf.utilities.windows.ErrorUtils;
 
 import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @version  $Revision$, $Date$
  */
 public class ClassManagement extends ProjectNode implements Refreshable,
     ConnectionListener,
     ClassManagementContextCookie {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Image nodeImage;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of ClassManagement.
      *
      * @param  project  DOCUMENT ME!
      */
     public ClassManagement(final DomainserverProject project) {
         super(Children.LEAF, project);
         getCookieSet().add(this);
         project.addConnectionListener(WeakListeners.create(ConnectionListener.class, this, project));
         nodeImage = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "class_management.png"); // NOI18N
         setDisplayName(NbBundle.getMessage(
                 ClassManagement.class,
                 "ClassManagement.ClassManagement(DomainserverProject).displayName"));                    // NOI18N
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Image getIcon(final int i) {
         return nodeImage;
     }
 
     @Override
     public Image getOpenedIcon(final int i) {
         return nodeImage;
     }
 
     @Override
     public void connectionStatusChanged(final ConnectionEvent event) {
         if (event.isConnected() && !event.isIndeterminate()) {
             setChildrenEDT(new ClassManagementChildren(project));
         } else {
             setChildrenEDT(Children.LEAF);
         }
     }
 
     @Override
     public Action[] getActions(final boolean context) {
         return new Action[] {
                 CallableSystemAction.get(NewCidsClassWizardAction.class),
                 null,
                 CallableSystemAction.get(CheckRightsAction.class),
                 null,
                 CallableSystemAction.get(RefreshAction.class),
//                null,
//                CallableSystemAction.get(ImportClassesAction.class)
             };
     }
 
     @Override
     public void refresh() {
         final Children ch = getChildren();
         if (ch instanceof ProjectChildren) {
             ((ProjectChildren)ch).refreshByNotify();
         }
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 final class ClassManagementChildren extends ProjectChildren {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(ClassManagementChildren.class);
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ClassManagementChildren object.
      *
      * @param  project  DOCUMENT ME!
      */
     public ClassManagementChildren(final DomainserverProject project) {
         super(project);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     protected Node[] createUserNodes(final Object o) {
         if (o instanceof CidsClass) {
             return new Node[] { new CidsClassNode((CidsClass)o, project) };
         } else {
             return new Node[] {};
         }
     }
 
     @Override
     protected void threadedNotify() throws IOException {
         try {
             final List<CidsClass> allClasses = project.getCidsDataObjectBackend().getAllEntities(CidsClass.class);
             Collections.sort(allClasses, new Comparators.CidsClasses());
             setKeysEDT(allClasses);
         } catch (final Exception ex) {
             LOG.error("could not fetch all classes from backend", ex); // NOI18N
             ErrorUtils.showErrorMessage(
                 NbBundle.getMessage(
                     ClassManagementChildren.class,
                     "ClassManagement.addNotify().ErrorUtils.message"), // NOI18N
                 ex);
         }
     }
 }
