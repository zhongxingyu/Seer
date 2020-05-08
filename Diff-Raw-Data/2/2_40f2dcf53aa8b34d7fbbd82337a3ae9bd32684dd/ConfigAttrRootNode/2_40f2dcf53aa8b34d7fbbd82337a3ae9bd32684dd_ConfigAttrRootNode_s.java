 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.configattr;
 
 import org.apache.log4j.Logger;
 
 import org.openide.DialogDisplayer;
 import org.openide.WizardDescriptor;
 import org.openide.actions.NewAction;
 import org.openide.cookies.EditCookie;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
 import org.openide.nodes.NodeAdapter;
 import org.openide.nodes.NodeListener;
 import org.openide.nodes.NodeMemberEvent;
 import org.openide.util.NbBundle;
 import org.openide.util.WeakListeners;
 import org.openide.util.actions.CallableSystemAction;
 import org.openide.util.datatransfer.NewType;
 
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.EventQueue;
 
 import java.io.IOException;
 
 import java.text.MessageFormat;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.Action;
 import javax.swing.JComponent;
 
 import de.cismet.cids.abf.domainserver.RefreshAction;
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.KeyContainer;
 import de.cismet.cids.abf.domainserver.project.ProjectChildren;
 import de.cismet.cids.abf.domainserver.project.ProjectNode;
 import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
 import de.cismet.cids.abf.utilities.ConnectionEvent;
 import de.cismet.cids.abf.utilities.ConnectionListener;
 import de.cismet.cids.abf.utilities.Refreshable;
 import de.cismet.cids.abf.utilities.nodes.LoadingNode;
 
 import de.cismet.cids.jpa.backend.service.Backend;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public abstract class ConfigAttrRootNode extends ProjectNode {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(GenericConfigAttrRootNodeChildren.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient ConnectionListener connL;
     private final transient Types type;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ConfigAttrRootNode object.
      *
      * @param  type     DOCUMENT ME!
      * @param  project  DOCUMENT ME!
      */
     public ConfigAttrRootNode(final Types type, final DomainserverProject project) {
         super(Children.LEAF, project);
         this.type = type;
         connL = new ConnL();
         project.addConnectionListener(WeakListeners.create(ConnectionListener.class, connL, project));
         getCookieSet().add(new RefreshableImpl());
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Action[] getActions(final boolean context) {
         return new Action[] {
                 CallableSystemAction.get(NewAction.class),
                 null,
                 CallableSystemAction.get(RefreshAction.class)
             };
     }
 
     @Override
     public NewType[] getNewTypes() {
         if (project.isConnected()) {
             return new NewType[] { new NewKeyType() };
         } else {
             return new NewType[] {};
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class NewKeyType extends NewType {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient NodeListener nodeL;
         private final transient SubNodeListenerImpl subNodeL;
         private transient WizardDescriptor.Panel[] panels;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new NewKeyType object.
          */
         public NewKeyType() {
             nodeL = new NodeListenerImpl();
             subNodeL = new SubNodeListenerImpl();
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @throws  IOException  DOCUMENT ME!
          */
         @Override
         public void create() throws IOException {
             final WizardDescriptor wizard = new WizardDescriptor(getPanels());
             // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
             wizard.setTitleFormat(new MessageFormat("{0}"));                 // NOI18N
             wizard.setTitle(NbBundle.getMessage(
                     ConfigAttrRootNode.class,
                     "ConfigAttrRootNode.NewKeyType.create().wizard.title")); // NOI18N
             wizard.putProperty(NewEntryWizardPanel1.PROP_ENTRY_TYPE, type);
             wizard.putProperty(NewEntryWizardPanel1.PROP_PROJECT, project);
             final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
             dialog.setVisible(true);
             dialog.toFront();
             final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
             if (!cancelled) {
                 final List<ConfigAttrEntry> newEntries = (List)wizard.getProperty(NewEntryWizardPanel1.PROP_ENTRIES);
                 final Backend backend = project.getCidsDataObjectBackend();
 
                 for (final ConfigAttrEntry entry : newEntries) {
                     backend.storeEntry(entry);
                 }
 
                 addNodeListener(nodeL);
                 getCookie(Refreshable.class).refresh();
                 project.getLookup().lookup(UserManagement.class).refreshProperties(false);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
             if (panels == null) {
                 panels = new WizardDescriptor.Panel[] { new NewKeyWizardPanel1(), new NewEntryWizardPanel1() };
                 final String[] steps = new String[panels.length];
                 for (int i = 0; i < panels.length; i++) {
                     final Component c = panels[i].getComponent();
                     // Default step name to component name of panel. Mainly useful
                     // for getting the name of the target chooser to appear in the
                     // list of steps.
                     steps[i] = c.getName();
                     if (c instanceof JComponent) { // assume Swing components
                         final JComponent jc = (JComponent)c;
                         // Sets step number of a component
                         // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                         jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                         // Sets steps names for a panel
                         jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                         // Turn on subtitle creation on each step
                         jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                         // Show steps on the left side with the image on the background
                         jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                         // Turn on numbering of all steps
                         jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                     }
                 }
             }
 
             return panels;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         @Override
         public String getName() {
             return NbBundle.getMessage(ConfigAttrRootNode.class, "ConfigAttrRootNode.NewKeyType.getName().returnValue"); // NOI18N
         }
 
         //~ Inner Classes ------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @version  $Revision$, $Date$
          */
         private final class NodeListenerImpl extends NodeAdapter {
 
             //~ Methods --------------------------------------------------------
 
             @Override
             public void childrenAdded(final NodeMemberEvent ev) {
                 final Node[] delta = ev.getDelta();
                 if ((delta != null) && (delta.length == 1) && (delta[0] != null)) {
                     final Node keyNode = delta[0];
                     final Node newEntry = keyNode.getChildren().getNodeAt(0);
                     if (newEntry == null) {
                         if (LOG.isInfoEnabled()) {
                             LOG.info("new entry cannot be found, register nodelistener on keynode: " + keyNode); // NOI18N
                         }
                         final Children children = keyNode.getChildren();
                         if (children instanceof ProjectChildren) {
                             subNodeL.setSubNode(keyNode);
                             keyNode.addNodeListener(subNodeL);
                             ((ProjectChildren)children).refreshByNotify();
                         } else {
                             LOG.warn("children not instanceof ProjectChildren, cannot create editor");           // NOI18N
                         }
                     } else {
                         final EditCookie editor = newEntry.getCookie(EditCookie.class);
                         if (editor == null) {
                             LOG.warn("cannot edit new node: " + keyNode);                                        // NOI18N
                         } else {
                             editor.edit();
                         }
                     }
                     removeNodeListener(nodeL);
                 }
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @version  $Revision$, $Date$
          */
         private final class SubNodeListenerImpl extends NodeAdapter {
 
             //~ Instance fields ------------------------------------------------
 
             private transient Node subNode;
 
             //~ Methods --------------------------------------------------------
 
             /**
              * DOCUMENT ME!
              *
              * @param  subNode  DOCUMENT ME!
              */
             public void setSubNode(final Node subNode) {
                 this.subNode = subNode;
             }
 
             @Override
             public void childrenAdded(final NodeMemberEvent ev) {
                 final Node[] delta = ev.getDelta();
                 if ((delta != null) && (delta.length == 1) && (delta[0] != null)
                             && !(delta[0] instanceof LoadingNode)) {
                     final EditCookie editor = delta[0].getCookie(EditCookie.class);
                     if (editor == null) {
                         LOG.warn("cannot edit new node: " + delta[0]); // NOI18N
                     } else {
                         editor.edit();
                     }
                     subNode.removeNodeListener(subNodeL);
                     subNode = null;
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class RefreshableImpl implements Refreshable {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void refresh() {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("requesting refresh", new Throwable("trace")); // NOI18N
             }
 
             if (Children.LEAF.equals(getChildren()) || !(getChildren() instanceof ProjectChildren)) {
                 setChildrenEDT(new GenericConfigAttrRootNodeChildren(type, project));
             } else {
                 final Future<?> refreshing = ((ProjectChildren)getChildren()).refreshByNotify();
 
                 try {
                     refreshing.get(10, TimeUnit.SECONDS);
                     final Runnable r = new Runnable() {
 
                             @Override
                             public void run() {
                                 final Node[] childNodes = getChildren().getNodes(false);
                                 for (final Node childNode : childNodes) {
                                     final Refreshable refreshableChild = childNode.getCookie(Refreshable.class);
                                     if (refreshableChild != null) {
                                         refreshableChild.refresh();
                                     }
                                 }
                             }
                         };
 
                     if (EventQueue.isDispatchThread()) {
                         r.run();
                     } else {
                         EventQueue.invokeLater(r);
                     }
                 } catch (final Exception e) {
                     LOG.warn("cannot wait for finish of refresh of config attr root node children", e); // NOI18N
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ConnL implements ConnectionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void connectionStatusChanged(final ConnectionEvent event) {
             if (!event.isIndeterminate()) {
                 if (event.isConnected()) {
                     setChildrenEDT(new GenericConfigAttrRootNodeChildren(type, project));
                 } else {
                     setChildrenEDT(Children.LEAF);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class GenericConfigAttrRootNodeChildren extends ProjectChildren {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient Types type;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new GenericConfigAttrRootNodeChildren object.
          *
          * @param  type     DOCUMENT ME!
          * @param  project  backend DOCUMENT ME!
          */
         public GenericConfigAttrRootNodeChildren(final Types type, final DomainserverProject project) {
             super(project);
             this.type = type;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void threadedNotify() throws IOException {
             final List<ConfigAttrEntry> entries = project.getCidsDataObjectBackend().getEntries(type);
             Collections.sort(entries, new Comparator<ConfigAttrEntry>() {
 
                     @Override
                     public int compare(final ConfigAttrEntry o1, final ConfigAttrEntry o2) {
                         return o1.getKey().getKey().compareTo(o2.getKey().getKey());
                     }
                 });
 
             final Set<ConfigAttrKey> keys = new LinkedHashSet<ConfigAttrKey>(entries.size());
             for (final ConfigAttrEntry entry : entries) {
                 keys.add(entry.getKey());
             }
 
             setKeysEDT(KeyContainer.convertCollection(ConfigAttrKey.class, keys));
         }
 
         @Override
         protected Node[] createUserNodes(final Object o) {
            if (o instanceof ConfigAttrKey) {
                 return new Node[] {
                         new ConfigAttrKeyNode((ConfigAttrKey)((KeyContainer)o).getObject(), type, project)
                     };
             } else {
                 return new Node[] {};
             }
         }
     }
 }
