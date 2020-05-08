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
 import org.openide.ErrorManager;
 import org.openide.NotifyDescriptor;
 import org.openide.WizardDescriptor;
 import org.openide.actions.DeleteAction;
 import org.openide.actions.NewAction;
 import org.openide.actions.RenameAction;
 import org.openide.cookies.CloseCookie;
 import org.openide.cookies.EditCookie;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
 import org.openide.nodes.NodeAdapter;
 import org.openide.nodes.NodeListener;
 import org.openide.nodes.NodeMemberEvent;
 import org.openide.nodes.PropertySupport;
 import org.openide.nodes.Sheet;
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.util.actions.CallableSystemAction;
 import org.openide.util.datatransfer.NewType;
 
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Image;
 
 import java.io.IOException;
 
 import java.lang.reflect.InvocationTargetException;
 
 import java.text.MessageFormat;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.swing.Action;
 import javax.swing.JComponent;
 
 import de.cismet.cids.abf.domainserver.RefreshAction;
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.KeyContainer;
 import de.cismet.cids.abf.domainserver.project.ProjectChildren;
 import de.cismet.cids.abf.domainserver.project.ProjectNode;
 import de.cismet.cids.abf.domainserver.project.nodes.ConfigAttrManagement;
 import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
 import de.cismet.cids.abf.utilities.Refreshable;
 
 import de.cismet.cids.jpa.backend.service.Backend;
 import de.cismet.cids.jpa.entity.common.CommonEntity;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrKey;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;
 import de.cismet.cids.jpa.entity.user.User;
 import de.cismet.cids.jpa.entity.user.UserGroup;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public class ConfigAttrKeyNode extends ProjectNode {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(ConfigAttrKeyNode.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient ConfigAttrKey key;
     private final transient Types type;
     private final transient Image keyIcon;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ConfigAttrKeyNode object.
      *
      * @param  key      DOCUMENT ME!
      * @param  type     DOCUMENT ME!
      * @param  project  DOCUMENT ME!
      */
     public ConfigAttrKeyNode(final ConfigAttrKey key, final Types type, final DomainserverProject project) {
         super(new ConfigAttrKeyNodeChildren(key, type, project), project);
         super.setName(key.getKey());
         this.key = key;
         this.type = type;
 
         getCookieSet().add(new ConfigAttrKeyCookieImpl());
         getCookieSet().add(new RefreshableImpl());
 
         keyIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "key_16.png"); // NOI18N
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Image getIcon(final int type) {
         return keyIcon;
     }
 
     @Override
     public Image getOpenedIcon(final int type) {
         return getIcon(type);
     }
 
     @Override
     public Action[] getActions(final boolean context) {
         return new Action[] {
                 CallableSystemAction.get(NewAction.class),
                 CallableSystemAction.get(RenameAction.class),
                 null,
                 CallableSystemAction.get(RefreshAction.class),
                 null,
                 CallableSystemAction.get(DeleteAction.class)
             };
     }
 
     @Override
     public boolean canRename() {
         return true;
     }
 
     @Override
     public void setName(final String s) {
         final String oldName = key.getKey();
 
         try {
             key.setKey(s);
             project.getCidsDataObjectBackend().store(key);
 
             super.setName(s);
         } catch (final Exception e) {
             key.setKey(oldName);
             LOG.error("cannot rename key: " + key, e); // NOI18N
 
             final NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(
                         ConfigAttrKeyNode.class,
                         "ConfigAttrKeyNode.setName(String).renameError.message", // NOI18N
                         e.getLocalizedMessage()),
                     NotifyDescriptor.WARNING_MESSAGE);
 
             DialogDisplayer.getDefault().notify(nd);
         }
     }
 
     @Override
     public NewType[] getNewTypes() {
         return new NewType[] { new NewEntryType() };
     }
 
     @Override
     public boolean canDestroy() {
         return project.isConnected();
     }
 
     @Override
     public void destroy() throws IOException {
         try {
             for (final Node node : getChildren().getNodes(true)) {
                 final CloseCookie cookie = node.getCookie(CloseCookie.class);
                 if (cookie != null) {
                     if (!cookie.close()) {
                         return;
                     }
                 }
             }
             final Backend backend = project.getCidsDataObjectBackend();
             final List<CommonEntity> entities = (List)backend.getEntries(key);
             entities.add(key);
             backend.delete(entities);
 
             final Node parent = getParentNode();
             if (parent == null) {
                 LOG.warn("cannot access parent node, refresh cannot be performed");                            // NOI18N
             } else {
                 final Refreshable refreshable = parent.getCookie(Refreshable.class);
                 if (refreshable == null) {
                     LOG.warn("cannot get Refreshable from parent, refresh cannot be performed: " + getName()); // NOI18N
                 } else {
                     refreshable.refresh();
                 }
             }
 
             fireNodeDestroyed();
 
             project.getLookup().lookup(UserManagement.class).refreshProperties(false);
         } catch (final Exception e) {
             final String message = "cannot destroy key: " + key; // NOI18N
             LOG.error(message, e);
             throw new IOException(message, e);
         }
     }
 
     @Override
     protected Sheet createSheet() {
         final Sheet sheet = Sheet.createDefault();
 
         try {
             final Property idProp = new PropertySupport.Reflection(key, Integer.class, "getId", null);                   // NOI18N
             idProp.setName(NbBundle.getMessage(ConfigAttrKeyNode.class, "ConfigAttrKeyNode.createSheet().idProp.name")); // NOI18N
             final Property keyProp = new PropertySupport.Reflection(key, String.class, "getKey", null);                  // NOI18N
             keyProp.setName(NbBundle.getMessage(
                     ConfigAttrKeyNode.class,
                     "ConfigAttrKeyNode.createSheet().keyProp.name"));                                                    // NOI18N
 
             final Property groupProp = new PropertySupport<String>(
                     "group", // NOI18N
                     String.class,
                     NbBundle.getMessage(ConfigAttrKeyNode.class, "ConfigAttrKeyNode.createSheet().groupProp.name"),
                     NbBundle.getMessage(
                         ConfigAttrKeyNode.class,
                         "ConfigAttrKeyNode.createSheet().groupProp.description"),
                     true,
                     true) {
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         if (ConfigAttrKey.NO_GROUP.equals(key.getGroupName())) {
                             return ConfigAttrGroupNode.NO_GROUP_DISPLAYNAME;
                         } else {
                             return key.getGroupName();
                         }
                     }
 
                     @Override
                     public void setValue(final String t) throws IllegalAccessException,
                         IllegalArgumentException,
                         InvocationTargetException {
                         final String old = key.getGroupName();
                        key.setGroupName((String)t);
                         // use action dispatcher if too slow
                         ConfigAttrManagement.ACTION_DISPATCHER.execute(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     try {
                                         project.getCidsDataObjectBackend().store(key);
                                         ConfigAttrManagement.REFRESH_DISPATCHER.execute(new Runnable() {
 
                                                 @Override
                                                 public void run() {
                                                     project.getLookup()
                                                             .lookup(ConfigAttrManagement.class)
                                                             .refresh(type, false);
                                                     project.getLookup()
                                                             .lookup(ConfigAttrManagement.class)
                                                            .refreshGroups(type, old, (String)t);
                                                 }
                                             });
                                     } catch (final Exception ex) {
                                         LOG.error("could not store config attr key", ex); // NOI18N
                                         key.setGroupName(old);
                                         ErrorManager.getDefault().notify(ex);
                                     }
                                 }
                             });
                     }
                 };
 
             final Sheet.Set main = Sheet.createPropertiesSet();
             main.setName("keyProperties");                                   // NOI18N
             main.setDisplayName(NbBundle.getMessage(
                     ConfigAttrKeyNode.class,
                     "ConfigAttrKeyNode.createSheet().mainSet.displayName")); // NOI18N
             main.put(idProp);
             main.put(keyProp);
             main.put(groupProp);
             sheet.put(main);
         } catch (final Exception e) {
             LOG.error("cannot create property sheet", e);                    // NOI18N
         }
 
         return sheet;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class RefreshableImpl implements Refreshable {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void refresh() {
             if (Children.LEAF.equals(getChildren()) || !(getChildren() instanceof ProjectChildren)) {
                 setChildren(new ConfigAttrKeyNodeChildren(key, type, project));
             } else {
                 ((ProjectChildren)getChildren()).refreshByNotify();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ConfigAttrKeyCookieImpl implements ConfigAttrKeyCookie {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public ConfigAttrKey getKey() {
             return key;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class NewEntryType extends NewType {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient NodeListener nodeL;
         private transient WizardDescriptor.Panel[] panels;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new NewEntryType object.
          */
         public NewEntryType() {
             nodeL = new NodeListenerImpl();
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
             wizard.setTitleFormat(new MessageFormat("{0}"));                // NOI18N
             wizard.setTitle(NbBundle.getMessage(
                     ConfigAttrKeyNode.class,
                     "ConfigAttrKeyNode.NewEntryType.create.wizard.title")); // NOI18N
             wizard.putProperty(NewEntryWizardPanel1.PROP_ENTRY_KEY, key);
             wizard.putProperty(NewEntryWizardPanel1.PROP_ENTRY_TYPE, type);
             wizard.putProperty(NewEntryWizardPanel1.PROP_PROJECT, project);
             final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
             dialog.setVisible(true);
             dialog.toFront();
             final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
             if (!cancelled) {
                 final Backend backend = project.getCidsDataObjectBackend();
                 final List<ConfigAttrEntry> oldEntries = backend.getEntries(key);
                 final List<ConfigAttrEntry> newEntries = (List)wizard.getProperty(NewEntryWizardPanel1.PROP_ENTRIES);
 
                 for (final ConfigAttrEntry old : oldEntries) {
                     if (!newEntries.contains(old)) {
                         backend.delete(old);
                     }
                 }
 
                 for (final ConfigAttrEntry nuu : newEntries) {
                     if (nuu.getId() == null) {
                         backend.storeEntry(nuu);
                     }
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
                 panels = new WizardDescriptor.Panel[] { new NewEntryWizardPanel1() };
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
             return NbBundle.getMessage(ConfigAttrKeyNode.class, "ConfigAttrKeyNode.NewEntryType.getName.returnValue"); // NOI18N
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
                     final EditCookie editor = delta[0].getCookie(EditCookie.class);
                     if (editor == null) {
                         LOG.warn("cannot edit new node: " + delta[0]); // NOI18N
                     } else {
                         editor.edit();
                     }
                     removeNodeListener(nodeL);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     static final class ConfigAttrKeyNodeChildren extends ProjectChildren {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient ConfigAttrKey key;
         private final transient Types type;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ConfigAttrKeyNodeChildren object.
          *
          * @param  key      DOCUMENT ME!
          * @param  type     DOCUMENT ME!
          * @param  project  DOCUMENT ME!
          */
         ConfigAttrKeyNodeChildren(final ConfigAttrKey key, final Types type, final DomainserverProject project) {
             super(project);
             this.key = key;
             this.type = type;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void threadedNotify() throws IOException {
             final List<ConfigAttrEntry> entries = project.getCidsDataObjectBackend().getEntries(key, type);
             Collections.sort(entries, new Comparator<ConfigAttrEntry>() {
 
                     @Override
                     public int compare(final ConfigAttrEntry o1, final ConfigAttrEntry o2) {
                         final int domainCompare = o1.getDomain().getName().compareTo(o2.getDomain().getName());
                         if (domainCompare == 0) {
                             final UserGroup ug1 = o1.getUsergroup();
                             final UserGroup ug2 = o2.getUsergroup();
                             if ((ug1 == null) && (ug2 == null)) {
                                 return domainCompare;
                             } else if ((ug1 == null) && (ug2 != null)) {
                                 return -1;
                             } else if ((ug1 != null) && (ug2 == null)) {
                                 return 1;
                             } else {
                                 final int ugCompare = ug1.getName().compareTo(ug2.getName());
                                 if (ugCompare == 0) {
                                     final User user1 = o1.getUser();
                                     final User user2 = o2.getUser();
                                     if ((user1 == null) && (user2 == null)) {
                                         return ugCompare;
                                     } else if ((user1 == null) && (user2 != null)) {
                                         return -1;
                                     } else if ((user1 != null) && (user2 == null)) {
                                         return 1;
                                     } else {
                                         return user1.getLoginname().compareTo(user2.getLoginname());
                                     }
                                 } else {
                                     return ugCompare;
                                 }
                             }
                         } else {
                             return domainCompare;
                         }
                     }
                 });
 
             setKeysEDT(KeyContainer.convertCollection(ConfigAttrEntry.class, entries));
         }
 
         @Override
         protected Node[] createUserNodes(final Object o) {
             if (o instanceof KeyContainer) {
                 return new Node[] { new ConfigAttrEntryNode((ConfigAttrEntry)((KeyContainer)o).getObject(), project) };
             } else {
                 return new Node[] {};
             }
         }
     }
 }
