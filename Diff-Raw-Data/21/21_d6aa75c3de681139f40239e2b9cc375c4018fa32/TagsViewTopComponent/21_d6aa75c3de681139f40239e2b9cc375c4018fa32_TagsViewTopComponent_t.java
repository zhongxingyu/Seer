 /**
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package us.physion.ovation.ui.detailviews;
 
 import com.google.common.collect.Lists;
 
 import java.util.*;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import org.netbeans.api.settings.ConvertAsProperties;
 import org.openide.awt.ActionID;
 import org.openide.awt.ActionReference;
 import org.openide.explorer.ExplorerManager;
 import org.openide.util.Lookup;
 import org.openide.util.LookupEvent;
 import org.openide.util.LookupListener;
 import org.openide.windows.TopComponent;
 import org.openide.util.NbBundle.Messages;
 import org.openide.util.Utilities;
 import us.physion.ovation.DataContext;
 import us.physion.ovation.DataStoreCoordinator;
 import us.physion.ovation.domain.OvationEntity;
 import us.physion.ovation.domain.User;
 import us.physion.ovation.domain.mixin.Owned;
 import us.physion.ovation.domain.mixin.Taggable;
 import us.physion.ovation.ui.*;
 import us.physion.ovation.ui.interfaces.ConnectionProvider;
 import us.physion.ovation.ui.interfaces.EventQueueUtilities;
 import us.physion.ovation.ui.interfaces.IEntityWrapper;
 
 
 @ConvertAsProperties(dtd = "-//us.physion.ovation.detailviews//TagsView//EN",
 autostore = false)
 @TopComponent.Description(preferredID = "TagsViewTopComponent",
 //iconBase="SET/PATH/TO/ICON/HERE",
 persistenceType = TopComponent.PERSISTENCE_ALWAYS)
 @TopComponent.Registration(mode = "properties", openAtStartup = true)
 @ActionID(category = "Window", id = "us.physion.ovation.detailviews.TagsViewTopComponent")
 @ActionReference(path = "Menu/Window" /*
  * , position = 333
  */)
 @TopComponent.OpenActionRegistration(displayName = "#CTL_TagsViewAction",
 preferredID = "TagsViewTopComponent")
 @Messages({
     "CTL_TagsViewAction=Keyword Tags",
     "CTL_TagsViewTopComponent=Keyword Tags",
     "HINT_TagsViewTopComponent=This is a Keyword Tags window"
 })
 public final class TagsViewTopComponent extends TopComponent {
 
     private DefaultComboBoxModel tagComboModel = new DefaultComboBoxModel(new String[] {});
     Lookup.Result global;
     private Collection<? extends IEntityWrapper> entities;
     private LookupListener listener = new LookupListener() {
 
         @Override
         public void resultChanged(LookupEvent le) {
 
             //TODO: we should have some other Interface for things that can update the tags view
             //then we could get rid of the Library dependancy on the Explorer API
             if (TopComponent.getRegistry().getActivated() instanceof ExplorerManager.Provider)
             {
                 update();
             }
         }
     };
 
     protected void addTags(final Collection<? extends IEntityWrapper> entities, String tags) {
         final String[] tagList = tags.split(",");
         EventQueueUtilities.runOffEDT(new Runnable() {
             @Override
             public void run() {
                 updateTagList(tagList, Lookup.getDefault().lookup(ConnectionProvider.class).getConnection());
             }
         });
     }
 
     protected void update()
     {
         entities = global.allInstances();
         ConnectionProvider cp = Lookup.getDefault().lookup(ConnectionProvider.class);
         cp.getConnection().getContext(); //getContext
         EventQueueUtilities.runOffEDT(new Runnable()
         {
             @Override
             public void run()
             {
                 update(entities, Lookup.getDefault().lookup(ConnectionProvider.class).getConnection());
             }
         });
     }
 
     protected List<TableTreeKey> update(Collection<? extends IEntityWrapper> entities, DataStoreCoordinator dsc)
     {
         DataContext c = dsc.getContext();
 
         ArrayList<TableTreeKey> tags = new ArrayList<TableTreeKey>();
         Set<String> uris = new HashSet<String>();
         Set<OvationEntity> entitybases = new HashSet();
         Set<String> owners = new HashSet();
         for (IEntityWrapper w : entities) {
             OvationEntity e = w.getEntity();
             entitybases.add(e);
             uris.add(e.getURI().toString());
             if (e instanceof Owned)
                 owners.add(((Owned)e).getOwner().getUuid().toString());
         }
 
         boolean containsCurrentUser = false;//current user's property table should always exist, even if there are no properties
         for (User u : c.getUsers()) {
             List<String> taglist = new ArrayList<String>();
             for (OvationEntity e : entitybases) {
                 if (e instanceof Taggable)
                 {
                     taglist.addAll(Lists.newArrayList(((Taggable)e).getUserTags(u)));
                 }
             }
             if (!taglist.isEmpty()) {
                 String uuid = u.getUuid().toString();
                 containsCurrentUser = isUserAuthenticated(u, c);
                 tags.add(new TagsSet(u, owners.contains(uuid), containsCurrentUser, taglist, uris));
             }
         }
         if (!containsCurrentUser) {
             User current = c.getAuthenticatedUser();
            tags.add(new TagsSet(current, true, true, new ArrayList<String>(), uris));
         }
         
         Collections.sort(tags);
         
         ((ScrollableTableTree) tagTree).setKeys(tags);
         
         this.entities = entities;
         return tags;
     }
 
     private boolean isUserAuthenticated(User u, DataContext c) {
         return c.getAuthenticatedUser().getUuid().equals(u.getUuid());
     }
 
     protected void updateTagList(String[] newTags, DataStoreCoordinator dsc)
     {
         JTree tree = ((ScrollableTableTree) tagTree).getTree();
         DefaultMutableTreeNode n = (DefaultMutableTreeNode)((DefaultTreeModel)tree.getModel()).getRoot();
 
         DefaultMutableTreeNode currentUserNode = (DefaultMutableTreeNode)n.getChildAt(0);
         final DefaultMutableTreeNode tagTableNode = (DefaultMutableTreeNode)currentUserNode.getChildAt(0);
         if (tagTableNode instanceof TableNode)
         {
             final TableNode node = (TableNode)tagTableNode;
             EditableTableModel m = ((EditableTableModel)node.getPanel().getTable().getModel());
             int row = m.getRowCount() -1;
             for (String tag : newTags)
             {
                 m.setValueAt(tag, row++, 0);
             }
            ((ScrollableTableTree)tagTree).resizeNode(node);
         }
     }
     
     public TagsViewTopComponent() {
         initComponents();
        //this.add(tagTree);
         setName(Bundle.CTL_TagsViewTopComponent());
         setToolTipText(Bundle.HINT_TagsViewTopComponent());
         global = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
         global.addLookupListener(listener);
         
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jSpinner1 = new javax.swing.JSpinner();
         addTagComboBox = new javax.swing.JComboBox();
         jLabel1 = new javax.swing.JLabel();
        tagTree = new us.physion.ovation.ui.ScrollableTableTree();
 
         addTagComboBox.setEditable(true);
         addTagComboBox.setModel(tagComboModel);
         addTagComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addTagComboBoxActionPerformed(evt);
             }
         });
 
         org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TagsViewTopComponent.class, "TagsViewTopComponent.jLabel1.text")); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(addTagComboBox, 0, 485, Short.MAX_VALUE))
                     .addComponent(tagTree))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(addTagComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(tagTree, javax.swing.GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void addTagComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTagComboBoxActionPerformed
 
         if (evt.getActionCommand().equals("comboBoxEdited"))
         {
             //add tag
             ConnectionProvider cp = Lookup.getDefault().lookup(ConnectionProvider.class);
             cp.getConnection().getContext(); //getContext
             String tags = addTagComboBox.getSelectedItem().toString();
             addTags(entities, tags);
             tagComboModel.removeAllElements();
             addTagComboBox.setSelectedItem("");
             addTagComboBox.setSelectedItem(null);
         }
     }//GEN-LAST:event_addTagComboBoxActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox addTagComboBox;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JSpinner jSpinner1;
     private javax.swing.JScrollPane tagTree;
     // End of variables declaration//GEN-END:variables
     @Override
     public void componentOpened() {
         // TODO add custom code on component opening
     }
 
     @Override
     public void componentClosed() {
         // TODO add custom code on component closing
     }
 
     void writeProperties(java.util.Properties p) {
         // better to version settings since initial version as advocated at
         // http://wiki.apidesign.org/wiki/PropertyFiles
         p.setProperty("version", "1.0");
         // TODO store your settings
     }
 
     void readProperties(java.util.Properties p) {
         String version = p.getProperty("version");
         // TODO read your settings according to their version
     }
 }
