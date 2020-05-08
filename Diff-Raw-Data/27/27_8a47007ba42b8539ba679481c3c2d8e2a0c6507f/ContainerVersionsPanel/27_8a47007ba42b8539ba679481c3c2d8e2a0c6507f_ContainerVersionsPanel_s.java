 /*
  * Created On: October 7, 2006, 10:25 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.MouseEvent;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JList;
 
 import com.thinkparity.codebase.FileUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.browser.Constants.Colors;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.MainCellImageCacheTest;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.MainCellImageCacheTest.TabCellIconTest;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.container.ContainerModel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel;
 import com.thinkparity.ophelia.browser.util.ArtifactUtil;
 import com.thinkparity.ophelia.model.container.ContainerDraft;
 
 /**
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class ContainerVersionsPanel extends DefaultTabPanel {
     
     /** Dimension of the cell. */
     private static final Dimension DIMENSION;
     
     static {        
         DIMENSION = new Dimension(50,100);
     }
 
     /** The <code>Container</code>. */
     private Container container;
 
     /** The version's <code>DocumentVersion</code>s. */
     private final Map<ContainerVersion, List<DocumentVersion>> documentVersions;
 
     /** The <code>ContainerModel</code>. */
     private final ContainerModel model;
 
     /** The version's published by <code>User</code>. */
     private final Map<ContainerVersion, User> publishedBy;
 
     /** The version's <code>User</code> and <code>ArtifactReceipt</code>. */
     private final Map<ContainerVersion, Map<User, ArtifactReceipt>> users;
 
     /** The container's <code>ContainerVersion</code>s. */
     private final List<ContainerVersion> versions;
 
     /** The version's content list model. */
     private final DefaultListModel versionsContentModel;
 
     /** The version's list model. */
     private final DefaultListModel versionsModel;
     
     /** An image cache. */
     protected final MainCellImageCacheTest imageCacheTest;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     javax.swing.JPanel leftJPanel;
     javax.swing.JPanel rightJPanel;
     private javax.swing.JList versionsContentJList;
     private javax.swing.JList versionsJList;
     // End of variables declaration//GEN-END:variables
 
     /**
      * Create ContainerVersionsPanel
      *
      * @param model
      *      The <code>ContainerModel</code>.
      */
     public ContainerVersionsPanel(final ContainerModel model) {
         super();
         this.documentVersions = new HashMap<ContainerVersion, List<DocumentVersion>>();
         this.model = model;
         this.publishedBy = new HashMap<ContainerVersion, User>();
         this.users = new HashMap<ContainerVersion, Map<User, ArtifactReceipt>>();
         this.versions = new ArrayList<ContainerVersion>();
         this.versionsModel = new DefaultListModel();
         this.versionsContentModel = new DefaultListModel();
         this.imageCacheTest = new MainCellImageCacheTest();
         initComponents();
     }
 
     /**
      * Add a container version. The container version includes a list of
      * documents; a list of users and their respective receipts; and the user
      * who published the container.
      * 
      * @param version
      *            A <code>ContainerVersion</code>.
      * @param documentVersions
      *            A <code>List&lt;Document&gt;</code>.
      * @param users
      *            A <code>Map&lt;User, ArtifactReceipt&gt;</code>.
      * @param publishedBy
      *            A <code>User</code>.
      */
     public void add(final ContainerVersion version,
             final List<DocumentVersion> documentVersions,
             final Map<User, ArtifactReceipt> users, final User publishedBy) {
         this.versions.add(version);
         this.documentVersions.put(version, documentVersions);
         this.users.put(version, users);
         this.publishedBy.put(version, publishedBy);
 
         versionsModel.addElement(new VersionCell(version, documentVersions,
                 users, publishedBy));
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel#getId()
      */
     @Override
     public Object getId() {
         return new StringBuffer(getClass().getName()).append("//")
                 .append(container.getId()).toString();
     }
 
     /**
      * Select the first version cell.
      *
      */
     public void selectFirstVersion() {
         if (0 < versionsModel.size()) {
             versionsJList.setSelectedIndex(0);
         }
     }
 
     /**
      * Set the container draft.
      * 
      * @param container
      *            The <code>Container</code>.
      * @param draft
      *            The <code>ContainerDraft</code>.
      */
     public void setDraft(final Container container, final ContainerDraft draft) {
         this.container = container;
 
         if (null != draft) {
             versionsModel.addElement(new DraftCell(draft));
         }
     }
     
     /**
      * Get the background color.
      * 
      * @param index
      *          Index into the display list.
      * @return Background color.
      */
     public Color getBackground(final int index) {
         final Color color;
        Integer containerIndex = model.indexOfContainerPanel(container);
         if (isSelectedContainer()) {
             color = Colors.Browser.List.LIST_SELECTION_BG;
         } else {           
             color = Colors.Browser.List.LIST_EXPANDED_NOT_SELECTED_BG;    
         }
        
 
         leftJPanel.setBackground(color);
         rightJPanel.setBackground(color);
         
         return color;
     }  
     
     /**
      * Determine if the container is selected.
      * 
      * @return True if the container is selected; false otherwise.
      */
     private Boolean isSelectedContainer() {
         return model.isSelectedContainer(container);
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel#triggerPopup(java.awt.Component,
      *      java.awt.event.MouseEvent)
      */
     @Override
     protected void triggerPopup(final Component invoker, final MouseEvent e) {
         new ContainerPopup(model, container).show(invoker, e);
     }
 
     protected void formMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_formMouseClicked
     }//GEN-LAST:event_formMouseClicked
 
     protected void formMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_formMouseReleased
     }//GEN-LAST:event_formMouseReleased
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
         javax.swing.JScrollPane versionsContentJScrollPane;
         javax.swing.JScrollPane versionsJScrollPane;
         javax.swing.JSplitPane versionsJSplitPane;
 
         versionsJSplitPane = new javax.swing.JSplitPane();
         leftJPanel = new javax.swing.JPanel();
         versionsJScrollPane = new javax.swing.JScrollPane();
         versionsJList = new javax.swing.JList();
         rightJPanel = new javax.swing.JPanel();
         versionsContentJScrollPane = new javax.swing.JScrollPane();
         versionsContentJList = new javax.swing.JList();
 
         setLayout(new java.awt.GridBagLayout());
 
         setOpaque(false);
         addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 formMouseClicked(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 formMouseReleased(evt);
             }
         });
 
         versionsJSplitPane.setBorder(null);
         versionsJSplitPane.setDividerSize(0);
         versionsJSplitPane.setResizeWeight(0.5);
         versionsJSplitPane.setMinimumSize(new java.awt.Dimension(52, 75));
         leftJPanel.setLayout(new java.awt.GridBagLayout());
 
         leftJPanel.setBackground(new java.awt.Color(255, 255, 255));
         versionsJScrollPane.setBorder(null);
         versionsJList.setModel(versionsModel);
         versionsJList.setCellRenderer(new VersionCellRenderer());
         versionsJList.setVisibleRowCount(5);
         versionsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 versionsJListValueChanged(evt);
             }
         });
         versionsJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 versionsJListMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 versionsJListMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 versionsJListMouseReleased(evt);
             }
         });
 
         versionsJScrollPane.setViewportView(versionsJList);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 1);
         leftJPanel.add(versionsJScrollPane, gridBagConstraints);
 
         versionsJSplitPane.setLeftComponent(leftJPanel);
 
         rightJPanel.setLayout(new java.awt.GridBagLayout());
 
         rightJPanel.setBackground(new java.awt.Color(255, 255, 255));
         versionsContentJScrollPane.setBorder(null);
         versionsContentJList.setModel(versionsContentModel);
         versionsContentJList.setCellRenderer(new VersionContentCellRenderer());
         versionsContentJList.setVisibleRowCount(5);
         versionsContentJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 versionsContentJListMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 versionsContentJListMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 versionsContentJListMouseReleased(evt);
             }
         });
 
         versionsContentJScrollPane.setViewportView(versionsContentJList);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(3, 1, 3, 3);
         rightJPanel.add(versionsContentJScrollPane, gridBagConstraints);
 
         versionsJSplitPane.setRightComponent(rightJPanel);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(versionsJSplitPane, gridBagConstraints);
 
     }// </editor-fold>//GEN-END:initComponents
 
     private void triggerJListPopup(final AbstractContentCell selectedContent,
             final MouseEvent e) {
         selectedContent.showPopupMenu(versionsContentJList, e);
     }
 
     private void triggerJListPopup(final AbstractVersionCell selectedVersion,
             final MouseEvent e) {
         selectedVersion.showPopupMenu(versionsJList, e);
     }
     
     private void versionsContentJListMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsContentJListMouseClicked
     }//GEN-LAST:event_versionsContentJListMouseClicked
 
     private void versionsContentJListMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsContentJListMousePressed
         logger.logApiId();
         logger.logVariable("e", e);
         final JList jList = (JList) e.getSource();
         if (e.isPopupTrigger() && 0 < jList.getSelectedIndices().length) {
             triggerJListPopup((AbstractContentCell) jList.getSelectedValue(), e);
             e.consume();
         }
     }//GEN-LAST:event_versionsContentJListMousePressed
 
     private void versionsContentJListMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsContentJListMouseReleased
         logger.logApiId();
         logger.logVariable("e", e);
         final JList jList = (JList) e.getSource();
         if (e.isPopupTrigger() && 0 < jList.getSelectedIndices().length) {
             triggerJListPopup((AbstractContentCell) jList.getSelectedValue(), e);
             e.consume();
         }
     }//GEN-LAST:event_versionsContentJListMouseReleased
 
     private void versionsJListMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsJListMouseClicked
     }//GEN-LAST:event_versionsJListMouseClicked
     private void versionsJListMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsJListMousePressed
         logger.logApiId();
         logger.logVariable("e", e);
         final JList jList = (JList) e.getSource();
         if (e.isPopupTrigger() && 0 < jList.getSelectedIndices().length) {
             triggerJListPopup((AbstractVersionCell) jList.getSelectedValue(), e);
             e.consume();
         }
     }//GEN-LAST:event_versionsJListMousePressed
 
     private void versionsJListMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsJListMouseReleased
         logger.logApiId();
         logger.logVariable("e", e);
         final JList jList = (JList) e.getSource();
         if (e.isPopupTrigger() && 0 < jList.getSelectedIndices().length) {
             triggerJListPopup((AbstractVersionCell) jList.getSelectedValue(), e);
             e.consume();
         }
     }//GEN-LAST:event_versionsJListMouseReleased
     private void versionsJListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_versionsJListValueChanged
         if (!evt.getValueIsAdjusting()) {
             logger.logVariable("evt", evt);
             versionsContentModel.clear();
             for (final Object selectedValue : versionsJList.getSelectedValues()) {
                 for (final AbstractContentCell contentCell : ((AbstractVersionCell) selectedValue).contentCells) {
                     versionsContentModel.addElement(contentCell);
                 }
             }
         }
     }//GEN-LAST:event_versionsJListValueChanged
 
     /** The version's content list cell. */
     abstract class AbstractContentCell {
         private String text;
         private Icon icon = null;
         protected AbstractContentCell() {
             super();
         }
         protected String getText() {
             return text;
         }
         protected void setText(final String text) {
             this.text = text;
         }
         protected Icon getIcon() {
             return icon;
         }
         protected void setIcon(final Icon icon) {
             this.icon = icon;
         }
         protected Boolean isSelectedContainer() {
             return ContainerVersionsPanel.this.isSelectedContainer();
         }
         protected abstract void showPopupMenu(final Component invoker,
                 final MouseEvent e);
     }
 
     /** The version's list cell. */
     abstract class AbstractVersionCell {
         private final List<AbstractContentCell> contentCells;
         private String text;
         private Icon icon = null;
         protected AbstractVersionCell() {
             super();
             this.contentCells = new ArrayList<AbstractContentCell>();
         }
         protected void addContentCell(final AbstractContentCell contentCell) {
             this.contentCells.add(contentCell);
         }
         protected List<AbstractContentCell> getContentCells() {
             return Collections.unmodifiableList(contentCells);
         }
         protected String getText() {
             return text;
         }
         protected void setText(final String text) {
             this.text = text;
         }
         protected Icon getIcon() {
             return icon;
         }
         protected void setIcon(final Icon icon) {
             this.icon = icon;
         }
         protected Boolean isSelectedContainer() {
             return ContainerVersionsPanel.this.isSelectedContainer();
         }
         protected abstract void showPopupMenu(final Component invoker,
                 final MouseEvent e);
     }
 
     /** A draft cell. */
     final class DraftCell extends AbstractVersionCell {
         private final ContainerDraft draft;
         private DraftCell(final ContainerDraft draft) {
             super();
             this.draft = draft;
             final Integer documentCount = draft.getDocumentCount();
             setText(1 == documentCount ?
                     "Draft - 1 Document" :
                     MessageFormat.format("Draft - {0} Documents",
                             documentCount));
             setIcon(imageCacheTest.read(TabCellIconTest.DRAFT));
             for (final Document document : draft.getDocuments()) {
                 addContentCell(new DraftDocumentCell(draft, document));
             }
         }
         @Override
         protected void showPopupMenu(final Component invoker, final MouseEvent e) {
             new ContainerVersionsPopup(model, this).show(invoker, e);
         }
         Long getContainerId() {
             return draft.getContainerId();
         }
     }
 
     /** A draft document cell. */
     final class DraftDocumentCell extends AbstractContentCell {
         private final ContainerDraft draft;
         private final Document document;
         private DraftDocumentCell(final ContainerDraft draft,
                 final Document document) {
             super();
             this.document = document;
             this.draft = draft;
             final String formatPattern;
             switch (draft.getState(document)) {
             case ADDED:
             case MODIFIED:
             case REMOVED:
                 formatPattern = "{0} - {1}";
                 break;
             case NONE:
                 formatPattern = "{0}";
                 break;
             default:
                 throw Assert.createUnreachable("UNKNOWN DOCUMENT STATE");
             }
             setText(MessageFormat.format(formatPattern,
                     document.getName(), draft.getState(document)));
             setIcon(getDocumentIcon(document));
         }
         @Override
         protected void showPopupMenu(final Component invoker, final MouseEvent e) {
             new ContainerVersionsPopup(model, this).show(invoker, e);
         }
         Long getContainerId() {
             return draft.getContainerId();
         }
         Long getId() {
             return document.getId();
         }
         ContainerDraft.ArtifactState getState() {
             return draft.getState(document);
         }
     }
 
     /** A user cell. */
     final class UserCell extends AbstractContentCell {
         private final User user;
         private UserCell(final User user, final ArtifactReceipt receipt) {
             this.user = user;
             if (receipt.isSetReceivedOn()) {
                 setText(MessageFormat.format("{0} - {3,date,MMM dd, yyyy h:mm a}",
                         user.getName(), user.getOrganization(), user.getTitle(),
                         receipt.getReceivedOn().getTime()));
             } else {
                 setText(MessageFormat.format("{0}",
                         user.getName(), user.getOrganization(), user.getTitle()));
             }
             setIcon(imageCacheTest.read(TabCellIconTest.CONTACT));
         }
         JabberId getId() {
             return user.getId();
         }
         @Override
         protected void showPopupMenu(final Component invoker, final MouseEvent e) {
             new ContainerVersionsPopup(model, this).show(invoker, e);
         }
     }
 
     /** A version cell. */
     final class VersionCell extends AbstractVersionCell {
         private final ContainerVersion version;
         private VersionCell(final ContainerVersion version,
                 final List<DocumentVersion> documentVersions,
                 final Map<User, ArtifactReceipt> users, final User publishedBy) {
             super();
             this.version = version;
             setText(MessageFormat.format(
                     "Version - {0,date,MMM d, yyyy h:mm a} - {1}",
                     version.getCreatedOn().getTime(),
                     publishedBy.getName(), publishedBy.getTitle(),
                     publishedBy.getOrganization()));
             setIcon(imageCacheTest.read(TabCellIconTest.VERSION));
             for (final DocumentVersion documentVersion : documentVersions) {
                 addContentCell(new DocumentVersionCell(documentVersion));
             }
             for (final Entry<User, ArtifactReceipt> entry : users.entrySet()) {
                 addContentCell(new UserCell(entry.getKey(), entry.getValue()));
             }
         }
         @Override
         protected void showPopupMenu(final Component invoker, final MouseEvent e) {
             new ContainerVersionsPopup(model, this).show(invoker, e);
         }
         Long getArtifactId() {
             return version.getArtifactId();
         }
         Long getVersionId() {
             return version.getVersionId();
         }
     }
 
     /** A version document cell. */
     final class DocumentVersionCell extends AbstractContentCell {
         private final DocumentVersion version;
         private DocumentVersionCell(final DocumentVersion version) {
             super();
             this.version = version;
             setText(MessageFormat.format("{0}", version.getName()));
             setIcon(getDocumentIcon(version));
         }
         @Override
         protected void showPopupMenu(final Component invoker, final MouseEvent e) {
             new ContainerVersionsPopup(model, this).show(invoker, e);
         }
         Long getDocumentId() {
             return version.getArtifactId();
         }
         Long getVersionId() {
             return version.getVersionId();
         }
     }
     
     /**
      * Get the preferred size.
      * 
      * @param last
      *          True if this is the last entity.
      * @return The preferred size <code>Dimension</code>.
      */   
     public Dimension getPreferredSize(final Boolean last) {
         return DIMENSION;
     }
     
     /**
      * Get the document icon.
      * 
      * @return The document icon.
      */
     public ImageIcon getDocumentIcon(final Document document) {
         final String extension = getNameExtension(document);
         if (extension.equalsIgnoreCase(".DOC")) {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_WORD); 
         } else if (extension.equalsIgnoreCase(".XLS")) {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_EXCEL);  
         } else {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_NOTEPAD); 
         }
     }
     
     /**
      * Get the file name extension.
      * 
      * @return The file name extension.
      */
     private String getNameExtension(final Document document) {
         return ArtifactUtil.getNameExtension(document);
     }
     
     // todo fix this
     public ImageIcon getDocumentIcon(final DocumentVersion documentVersion) {
         final String extension = getNameExtension(documentVersion);
         if (extension.equalsIgnoreCase(".DOC")) {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_WORD); 
         } else if (extension.equalsIgnoreCase(".XLS")) {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_EXCEL);  
         } else {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_NOTEPAD); 
         }
     }
     private String getNameExtension(final DocumentVersion documentVersion) {
         final String name = documentVersion.getName();
         return FileUtil.getExtension(name);
     }
 }
