 /*
  * Created On: October 7, 2006, 10:25 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JList;
 
 import com.thinkparity.codebase.FuzzyDateFormat;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.container.ContainerVersionArtifactVersionDelta.Delta;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.container.ContainerDraft;
 
 import com.thinkparity.ophelia.browser.Constants.Images;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.FileIconReader;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.MainPanelImageCache;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.MainPanelImageCache.TabPanelIcon;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel;
 import com.thinkparity.ophelia.browser.platform.action.AbstractAction;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.action.contact.Read;
 import com.thinkparity.ophelia.browser.platform.action.container.ReadVersion;
 import com.thinkparity.ophelia.browser.platform.action.document.Open;
 import com.thinkparity.ophelia.browser.platform.action.document.OpenVersion;
 import com.thinkparity.ophelia.browser.util.localization.MainCellL18n;
 
 /**
  * <b>Title:</b>thinkParity Container Versions Panel<br>
  * <b>Description:</b><br>
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public class ContainerVersionsPanel extends DefaultTabPanel {
 
     /** A read contact <code>AbstractAction</code>. */
     private static final AbstractAction CONTACT_READ;
 
     /** A read container version <code>AbstractAction</code>. */
     private static final AbstractAction CONTAINER_READ_VERSION;
 
     /**
      * A client property key <code>String</code> for the is popup trigger
      * property.
      */
     private static final String CPK_IS_POPUP_TRIGGER;
 
     /** An open document <code>AbstractAction</code>. */
     private static final AbstractAction DOCUMENT_OPEN;
 
     /** An open document version <code>AbstractAction</code>. */
     private static final AbstractAction DOCUMENT_OPEN_VERSION;
 
     /** A <code>FuzzyDateFormat</code>. */
     private static final FuzzyDateFormat FUZZY_DATE_FORMAT;
 
     /** Number of rows visible in the expanded package lists. */
     private static final Integer NUMBER_VISIBLE_ROWS;
 
     static {
         CPK_IS_POPUP_TRIGGER = ContainerVersionsPanel.class.getName() + "#isPopupTrigger";
         FUZZY_DATE_FORMAT = new FuzzyDateFormat();
         NUMBER_VISIBLE_ROWS = 5;
         CONTACT_READ = getInstance(ActionId.CONTACT_READ);
         CONTAINER_READ_VERSION = getInstance(ActionId.CONTAINER_READ_VERSION);
         DOCUMENT_OPEN_VERSION = getInstance(ActionId.DOCUMENT_OPEN_VERSION);
         DOCUMENT_OPEN = getInstance(ActionId.DOCUMENT_OPEN);
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JList versionsContentJList = new javax.swing.JList();
     private final javax.swing.JList versionsJList = new javax.swing.JList();
     // End of variables declaration//GEN-END:variables
 
     /** The clipped down background images on the left. */
     private BufferedImage[] clippedContainerBackgroundLeft;
 
     /** The clipped down background image on the right. */
     private BufferedImage clippedContainerBackgroundRight;
 
     /** The <code>Container</code>. */
     private Container container;
 
     /** A file icon reader. */
     private final FileIconReader fileIconReader;
 
     /** An image cache. */
     private final MainPanelImageCache imageCache;
 
     /** The panel localization. */
     private final MainCellL18n localization;
 
     /** An apache logger. */
     private final Log4JWrapper logger;
     
     /** A popup factory for the versions panel. */
     private VersionsPopupFactory popupFactory;
 
     /** The previous selection index of the Versions list. */
     private int previousVersionSelectionIndex = -1;
 
     /** The scaled up background images on the left. */
     private BufferedImage[] scaledContainerBackgroundLeft;
 
     /** The scaled up background image on the right. */
     private BufferedImage scaledContainerBackgroundRight;
 
     /** The version's content list model. */
     private final DefaultListModel versionsContentModel;
 
     /** The version's list model. */
     private final DefaultListModel versionsModel;
 
     /**
      * Create ContainerVersionsPanel
      * 
      */
     public ContainerVersionsPanel() {
         super();
         this.versionsModel = new DefaultListModel();
         this.versionsContentModel = new DefaultListModel();
         this.imageCache = new MainPanelImageCache();
         this.fileIconReader = new FileIconReader();
         this.logger = new Log4JWrapper();
         this.localization = new MainCellL18n("ContainerVersionsPanel");
         initBackgroundImages();
         initComponents();
     }
 
     /**
      * Obtain the container.
      * 
      * @return A <code>Container</code>.
      */
     public Container getContainer() {
         return container;
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
      * Set the container and its draft.
      * 
      * @param container
      *            The <code>Container</code>.
      * @param draft
      *            The <code>ContainerDraft</code>.
      */
     public void setPanelData(
             final Container container,
             final ContainerDraft draft,
             final List<ContainerVersion> versions,
             final Map<ContainerVersion, Map<DocumentVersion, Delta>> documentVersions,
             final Map<ContainerVersion, Map<User, ArtifactReceipt>> publishedTo,
             final Map<ContainerVersion, User> publishedBy) {
         this.container = container;
         // if a draft exists and the user is the creator of the draft or the
         // user has the latest version of the container display a draft element
         if (null != draft && container.isLocalDraft()
                 || (container.isDraft() && container.isLatest())) {
             versionsModel.addElement(new DraftCell(draft));
         }
         // display all version elements
         for (final ContainerVersion version : versions) {
             versionsModel.addElement(new VersionCell(version, documentVersions
                     .get(version), publishedTo.get(version), publishedBy
                     .get(version)));
         }
         // If necessary, add filler rows to the versions list
         // so the backgrounds will paint in alternating colours
         if (versionsJList.getModel().getSize() <
                 versionsJList.getVisibleRowCount()) {
             for (int i = versionsJList.getModel().getSize();
                     i < versionsJList.getVisibleRowCount(); i++) {
                 versionsModel.addElement(new FillCell());
             }
         }
         selectFirstVersion();
     }
 
     /**
      * Set the popup factory.
      * 
      * @param popupFactor
      *            A <code>VersionsPopupFactory</code>.
      */
     public void setPopupFactory(final VersionsPopupFactory popupFactory) {
         this.popupFactory = popupFactory;
     }
 
     /**
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      *
      */
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         final int rightWidth = getWidth() / 2;
         final int leftWidth = getWidth() - rightWidth - 5;
         final int selectedIndex = versionsJList.getSelectedIndex() - versionsJList.getFirstVisibleIndex();        
         final int imageIndex;
         if (versionsJList.getSelectedIndex() < 0 ) {
             imageIndex = 0;
         } else if ((selectedIndex < 0 ) || (selectedIndex >= NUMBER_VISIBLE_ROWS)) {
             imageIndex = 0;
         } else {
             imageIndex = selectedIndex + 1;
         }
         
         // Prepare the right side.        
         //  - Resize the scaled background image if it is not large enough. This operation is a bit slow
         // so we only use it to make the image larger. This covers the unlikely scenario that the width
         // becomes larger than the screen size.
         //  - Then clip the background image if it is too large. This operation is quicker than scaling.
         if ((null == scaledContainerBackgroundRight) || scaledContainerBackgroundRight.getWidth() < rightWidth) {
             initBackgroundImages(rightWidth);
         }
         if ((null == clippedContainerBackgroundRight) || (clippedContainerBackgroundRight.getWidth() != rightWidth)) {
             clippedContainerBackgroundRight = scaledContainerBackgroundRight.getSubimage(
                     0, 0, rightWidth, getHeight());
         }
         
         // Prepare the left side.
         if ((null == scaledContainerBackgroundLeft[imageIndex]) || scaledContainerBackgroundLeft[imageIndex].getWidth() < leftWidth) {
             initBackgroundImages(getWidth());
         }
         if ((null == clippedContainerBackgroundLeft[imageIndex]) || (clippedContainerBackgroundLeft[imageIndex].getWidth() != leftWidth)) {
             clippedContainerBackgroundLeft[imageIndex] = scaledContainerBackgroundLeft[imageIndex].getSubimage(
                     0, 0, leftWidth, getHeight());
         }
                 
         // Paint
         final Graphics g2 = g.create();
         try {
             g2.drawImage(clippedContainerBackgroundLeft[imageIndex], 0, 0, leftWidth, getHeight(), null);
             g2.drawImage(Images.BrowserTitle.CONTAINER_BACKGROUND_MID[imageIndex], leftWidth, 0, 5, getHeight(), null);
             g2.drawImage(clippedContainerBackgroundRight, getWidth()-rightWidth, 0, rightWidth, getHeight(), null);
         }
         finally { g2.dispose(); }
     }
 
     /**
      * Get the document icon, ie. associated with the file extension.
      * 
      * @return The document icon.
      */
     private ImageIcon getDocumentIcon(final Document document) {
         return fileIconReader.getIcon(document);
     }
 
     /**
      * Get the document icon, ie. associated with the file extension.
      * 
      * @return The document icon.
      */
     private ImageIcon getDocumentIcon(final DocumentVersion documentVersion) {
         return fileIconReader.getIcon(documentVersion);
     }
 
     /**
      * Initialize the background scaling and clipping images.
      *
      */
     private void initBackgroundImages() {
         if (null == scaledContainerBackgroundLeft) {
            scaledContainerBackgroundLeft = new BufferedImage[NUMBER_VISIBLE_ROWS];
         }
         if (null == clippedContainerBackgroundLeft) {
            clippedContainerBackgroundLeft = new BufferedImage[NUMBER_VISIBLE_ROWS];
         }
         final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         initBackgroundImages((int)screenSize.getWidth());
     }
 
     /**
      * Initialize background images to the specified size (or larger).
      * 
      * @param newDimension
      *      New dimension.
      */
     private void initBackgroundImages(final int newWidth) {
        // Set up and if necessary scale the left background images
        for (int index = 0; index < NUMBER_VISIBLE_ROWS; index++) {
             if (newWidth <= Images.BrowserTitle.CONTAINER_BACKGROUND_LEFT[index].getWidth()) {
                 scaledContainerBackgroundLeft[index] = Images.BrowserTitle.CONTAINER_BACKGROUND_LEFT[index];
             } else {
                 final Image image = Images.BrowserTitle.CONTAINER_BACKGROUND_LEFT[index].getScaledInstance(
                         newWidth, Images.BrowserTitle.CONTAINER_BACKGROUND_LEFT[index].getHeight(), Image.SCALE_FAST);
                 scaledContainerBackgroundLeft[index] = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
                 final Graphics g2 = scaledContainerBackgroundLeft[index].createGraphics();
                 try {
                     g2.drawImage(image, 0, 0, null);
                 }
                 finally { g2.dispose(); }
             }
         }
 
         // Set up and if necessary scale the right background image
         if (newWidth <= Images.BrowserTitle.CONTAINER_BACKGROUND_RIGHT.getWidth()) {
             scaledContainerBackgroundRight = Images.BrowserTitle.CONTAINER_BACKGROUND_RIGHT;
         } else {
             final Image image = Images.BrowserTitle.CONTAINER_BACKGROUND_RIGHT.getScaledInstance(
                     newWidth, Images.BrowserTitle.CONTAINER_BACKGROUND_RIGHT.getHeight(), Image.SCALE_FAST);
             scaledContainerBackgroundRight = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
             final Graphics g2 = scaledContainerBackgroundRight.createGraphics();
             try {
                 g2.drawImage(image, 0, 0, null);
             }
             finally { g2.dispose(); }
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         final javax.swing.JSplitPane versionsJSplitPane = new javax.swing.JSplitPane();
         final javax.swing.JPanel leftJPanel = new javax.swing.JPanel();
         final javax.swing.JScrollPane versionsJScrollPane = new javax.swing.JScrollPane();
         versionsJScrollPane.getViewport().setOpaque(false);
         final javax.swing.JPanel rightJPanel = new javax.swing.JPanel();
         final javax.swing.JScrollPane versionsContentJScrollPane = new javax.swing.JScrollPane();
         versionsContentJScrollPane.getViewport().setOpaque(false);
 
         setLayout(new java.awt.GridBagLayout());
 
         versionsJSplitPane.setBorder(null);
         versionsJSplitPane.setDividerSize(0);
         versionsJSplitPane.setResizeWeight(0.5);
         versionsJSplitPane.setMinimumSize(new java.awt.Dimension(55, 75));
         versionsJSplitPane.setOpaque(false);
         leftJPanel.setLayout(new java.awt.GridBagLayout());
 
         leftJPanel.setOpaque(false);
         leftJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 leftJPanelMouseClicked(e);
             }
             public void mousePressed(java.awt.event.MouseEvent e) {
                 leftJPanelMousePressed(e);
             }
         });
 
         versionsJScrollPane.setBorder(null);
         versionsJScrollPane.setOpaque(false);
         versionsJList.setModel(versionsModel);
         versionsJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         versionsJList.setCellRenderer(new VersionCellRenderer());
         versionsJList.setOpaque(false);
         versionsJList.setVisibleRowCount(NUMBER_VISIBLE_ROWS);
         versionsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                 versionsJListValueChanged(e);
             }
         });
         versionsJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 versionsJListMouseClicked(e);
             }
             public void mousePressed(java.awt.event.MouseEvent e) {
                 versionsJListMousePressed(e);
             }
             public void mouseReleased(java.awt.event.MouseEvent e) {
                 versionsJListMouseReleased(e);
             }
         });
 
         versionsJScrollPane.setViewportView(versionsJList);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         leftJPanel.add(versionsJScrollPane, gridBagConstraints);
 
         versionsJSplitPane.setLeftComponent(leftJPanel);
 
         rightJPanel.setLayout(new java.awt.GridBagLayout());
 
         rightJPanel.setOpaque(false);
         rightJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 rightJPanelMouseClicked(e);
             }
             public void mousePressed(java.awt.event.MouseEvent e) {
                 rightJPanelMousePressed(e);
             }
         });
 
         versionsContentJScrollPane.setBorder(null);
         versionsContentJScrollPane.setOpaque(false);
         versionsContentJList.setModel(versionsContentModel);
         versionsContentJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         versionsContentJList.setCellRenderer(new VersionContentCellRenderer());
         versionsContentJList.setOpaque(false);
         versionsContentJList.setVisibleRowCount(NUMBER_VISIBLE_ROWS);
         versionsContentJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                 versionsContentJListValueChanged(e);
             }
         });
         versionsContentJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 versionsContentJListMouseClicked(e);
             }
             public void mousePressed(java.awt.event.MouseEvent e) {
                 versionsContentJListMousePressed(e);
             }
             public void mouseReleased(java.awt.event.MouseEvent e) {
                 versionsContentJListMouseReleased(e);
             }
         });
 
         versionsContentJScrollPane.setViewportView(versionsContentJList);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         rightJPanel.add(versionsContentJScrollPane, gridBagConstraints);
 
         versionsJSplitPane.setRightComponent(rightJPanel);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 34, 0, 0);
         add(versionsJSplitPane, gridBagConstraints);
 
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Handle the click event for the given list. All we do is check for a
      * double-click event to run a specific action.
      * 
      * @param jList
      *            A <code>JList</code>.
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void jListMouseClicked(final JList jList,
             final java.awt.event.MouseEvent e) {
         logger.logApiId();
         logger.logVariable("e", e);
         // a click count of 2, 4, 6, etc. triggers double click event
         if (e.getClickCount() % 2 == 0) {
             ((AbstractCell) jList.getSelectedValue()).doubleClick(jList, e);
         }
     }
 
     /**
      * Handle the mouse pressed event for the given list. The isPopupTrigger
      * property of the mouse event is only set within the pressed event so here
      * we save that value within the list; and check it within
      * {@link #jListMouseRelease(JList, MouseEvent)}.
      * 
      * @param jList
      *            A <code>JList</code>.
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void jListMousePressed(final JList jList,
             final java.awt.event.MouseEvent e) {
         logger.logApiId();
         logger.logVariable("e", e);
         if (e.isPopupTrigger())
             jList.putClientProperty(CPK_IS_POPUP_TRIGGER, Boolean.TRUE);
         else
             jList.putClientProperty(CPK_IS_POPUP_TRIGGER, Boolean.FALSE);
     }
 
     /**
      * Handle the mouse released event for the given list. Check the list's
      * client property for the is popup trigger boolean property set within
      * {@link #jListMousePressed(JList, MouseEvent)} and if true; first select
      * the underlying cell; then display its popup.
      * 
      * @param jList
      *            A <code>JList</code>.
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void jListMouseReleased(final JList jList,
             final java.awt.event.MouseEvent e) {
         logger.logApiId();
         logger.logVariable("e", e);
         final Boolean isPopupTrigger = (Boolean) jList.getClientProperty(CPK_IS_POPUP_TRIGGER);
         if (null != isPopupTrigger && isPopupTrigger.booleanValue()) {
             jList.setSelectedIndex(jList.locationToIndex(e.getPoint()));
             ((AbstractCell) jList.getSelectedValue()).showPopup(jList, e.getX(), e.getY());
         }
     }
 
     private void leftJPanelMouseClicked(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_leftJPanelMouseClicked
     }// GEN-LAST:event_leftJPanelMouseClicked
 
     private void leftJPanelMousePressed(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_leftJPanelMousePressed
     }// GEN-LAST:event_leftJPanelMousePressed
     
     private void rightJPanelMouseClicked(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_rightJPanelMouseClicked
     }// GEN-LAST:event_rightJPanelMouseClicked
     
     private void rightJPanelMousePressed(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_rightJPanelMousePressed
     }// GEN-LAST:event_rightJPanelMousePressed
     
     /**
      * Select the first version cell.
      *
      */
     private void selectFirstVersion() {
         if (0 < versionsModel.size()) {
             versionsJList.setSelectedIndex(0);
         }
     }
     
     /**
      * Select an entry in the JList.
      * 
      * @param jList
      *              The JList.
      * @param index
      *              The JList index to select.
      */
     private void setSelectedIndex(final JList jList, final Integer index) {
         if (index != jList.getSelectedIndex()) {
             jList.setSelectedIndex(index);
         }
         if (!jList.isFocusOwner()) {
             jList.requestFocusInWindow();
         } 
     }
 
     private void versionsContentJListMouseClicked(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_versionsContentJListMouseClicked
         jListMouseClicked((JList) e.getSource(), e);
     }// GEN-LAST:event_versionsContentJListMouseClicked
 
     private void versionsContentJListMousePressed(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_versionsContentJListMousePressed
         jListMousePressed((JList) e.getSource(), e);
     }// GEN-LAST:event_versionsContentJListMousePressed
 
     private void versionsContentJListMouseReleased(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_versionsContentJListMouseReleased
         jListMouseReleased((JList) e.getSource(), e);
     }
 
     private void versionsContentJListValueChanged(javax.swing.event.ListSelectionEvent e) {//GEN-FIRST:event_versionsContentJListValueChanged
     }//GEN-LAST:event_versionsContentJListValueChanged
     
     private void versionsJListMouseClicked(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsJListMouseClicked
         jListMouseClicked((JList) e.getSource(), e);
     }//GEN-LAST:event_versionsJListMouseClicked
 
     private void versionsJListMousePressed(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsJListMousePressed
         jListMousePressed((JList) e.getSource(), e);
     }//GEN-LAST:event_versionsJListMousePressed
     
     private void versionsJListMouseReleased(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_versionsJListMouseReleased
         jListMouseReleased((JList) e.getSource(), e);        
     }//GEN-LAST:event_versionsJListMouseReleased
     
     private void versionsJListValueChanged(final javax.swing.event.ListSelectionEvent e) {//GEN-FIRST:event_versionsJListValueChanged
         logger.logApiId();
         logger.logVariable("e", e);
         if (null != versionsJList.getSelectedValue() &&
             ((AbstractCell) versionsJList.getSelectedValue()).isFill() &&
             (previousVersionSelectionIndex != -1)) {
             setSelectedIndex(versionsJList, previousVersionSelectionIndex);
         } else if (!e.getValueIsAdjusting()) {
             previousVersionSelectionIndex = versionsJList.getSelectedIndex();
             versionsContentModel.clear();
             for (final Object selectedValue : versionsJList.getSelectedValues()) {
                 for (final AbstractContentCell contentCell : ((AbstractVersionCell) selectedValue).contentCells) {
                     versionsContentModel.addElement(contentCell);
                 }
             }
         }
         repaint();
     }//GEN-LAST:event_versionsJListValueChanged
     
     /** The abstract list cell. */
     abstract class AbstractCell extends DefaultVersionsCell {
         protected AbstractCell() {
             super();
         }
         protected void doubleClick(final Component invoker, final MouseEvent e) {
         }
         protected boolean isFill() {
             return false;
         }
     }
     
     /** A content default cell. */
     abstract class AbstractContentCell extends AbstractCell {}
     
     /** The version's list cell. */
     abstract class AbstractVersionCell extends AbstractCell {
         /** The list of content cells. */
         private final List<AbstractContentCell> contentCells;
         
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
     }
 
     final class ContentFillCell extends AbstractContentCell {
         @Override
         public void showPopup(final Component invoker, final int x, final int y) {
         }
         @Override
         protected boolean isFill() {
             return true;
         }
 
     }
     /** A version document cell. */
     final class DocumentVersionCell extends AbstractContentCell {
         private final Delta delta;
         private final DocumentVersion version;
         private DocumentVersionCell(final DocumentVersion version, final Delta delta) {
             super();
             this.delta = delta;
             this.version = version;
             initText(version, delta);   
             setIcon(getDocumentIcon(version));
         }
         @Override
         public void showPopup(final Component invoker, final int x, final int y) {
             popupFactory.createVersionDocumentPopup(version, delta).show(invoker, x, y);
         }
         @Override
         protected void doubleClick(final Component invoker, final MouseEvent e) {
             final Data data = new Data(2);
             data.set(OpenVersion.DataKey.DOCUMENT_ID, version.getArtifactId());
             data.set(OpenVersion.DataKey.VERSION_ID, version.getVersionId());
             DOCUMENT_OPEN_VERSION.invoke(data);
         }
         Long getDocumentId() {
             return version.getArtifactId();
         }
         Long getVersionId() {
             return version.getVersionId();
         }
         private void initText(final DocumentVersion documentVersion, final Delta delta) {
             final String formatPattern;
             Delta finalDelta = delta;
             if (null == finalDelta) {
                 finalDelta = Delta.ADDED;
             }            
             switch (delta) {
             case ADDED:
             case MODIFIED:
             case REMOVED:
                 // TODO remove text colour from within html
                 formatPattern = "<html>{0} <font color=\"#646464\">({1})</font></html>";
                 break;
             case NONE:
                 formatPattern = "{0}";
                 break;
             default:
                 throw Assert.createUnreachable("UNKNOWN DOCUMENT STATE");
             }
             setText(MessageFormat.format(formatPattern,
                     documentVersion.getName(), delta.toString().toLowerCase()));
         }
     }
 
     /** A draft cell. */
     final class DraftCell extends AbstractVersionCell {
         private final ContainerDraft draft;
         private DraftCell(final ContainerDraft draft) {
             super();
             this.draft = draft;
             initText(draft);
             setIcon(imageCache.read(TabPanelIcon.DRAFT));
             int countCells = 0;
             if (container.isLocalDraft()) {
                 for (final Document document : draft.getDocuments()) {
                     addContentCell(new DraftDocumentCell(draft, document));
                     countCells++;
                 }
             }
             for (int i = countCells; i < versionsContentJList.getVisibleRowCount(); i++) {
                 addContentCell(new ContentFillCell());
             }
         }
         @Override
         public void showPopup(final Component invoker, final int x, final int y) {
             if (container.isLocalDraft())
                 popupFactory.createDraftPopup(container, draft).show(invoker, x, y);
         }
         Long getContainerId() {
             return draft.getContainerId();
         }
         private void initText(final ContainerDraft draft) {
             if (container.isLocalDraft()) {
                 setText(localization.getString("Draft"));
             } else {
                 setText(localization.getString("DraftNotLocal", draft.getOwner().getName()));
             }
         }
     }
 
     /** A draft document cell. */
     final class DraftDocumentCell extends AbstractContentCell {
         private final Document document;
         private final ContainerDraft draft;
         private DraftDocumentCell(final ContainerDraft draft,
                 final Document document) {
             super();
             this.document = document;
             this.draft = draft;
             initText(draft, document);            
             setIcon(getDocumentIcon(document));
         }
         @Override
         public void showPopup(final Component invoker, final int x, final int y) {
             popupFactory.createDraftDocumentPopup(draft, document).show(invoker, x, y);
         }
         @Override
         protected void doubleClick(final Component invoker, final MouseEvent e) {
             final Data data = new Data(1);
             data.set(Open.DataKey.DOCUMENT_ID, document.getId());
             DOCUMENT_OPEN.invoke(data);
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
         private void initText(final ContainerDraft draft, final Document document) {
             final String formatPattern;
             switch (draft.getState(document)) {
             case ADDED:
             case MODIFIED:
             case REMOVED:
                 // TODO remove text colour from within html
                 formatPattern = "<html>{0} <font color=\"#646464\">({1})</font></html>";
                 break;
             case NONE:
                 formatPattern = "{0}";
                 break;
             default:
                 throw Assert.createUnreachable("UNKNOWN DOCUMENT STATE");
             }
             setText(MessageFormat.format(formatPattern,
                     document.getName(), draft.getState(document).toString().toLowerCase()));
         }
     }
 
     /** A fill cell for the content list, so the background can be drawn */
     final class FillCell extends AbstractCell {
         @Override
         public void showPopup(final Component invoker, final int x, final int y) {
         }
         @Override
         protected boolean isFill() {
             return true;
         }
     }
 
     /** A user cell. */
     final class UserCell extends AbstractContentCell {
         private final boolean publisher;
         private final ArtifactReceipt receipt;
         private final User user;
         private UserCell(final User user, final Boolean publisher,
                 final ArtifactReceipt receipt) {
             this.publisher = publisher;
             this.receipt = receipt;
             this.user = user;
             initText(user, publisher, receipt);
             initIcon(publisher, receipt);
         }
         @Override
         public void showPopup(final Component invoker, final int x, final int y) {
             if (publisher)
                 popupFactory.createPublishedByPopup(user).show(invoker, x, y);
             else
                 popupFactory.createPublishedToPopup(user, receipt).show(invoker, x, y);
         }
         @Override
         protected void doubleClick(final Component invoker, final MouseEvent e) {
             final Data data = new Data(1);
             data.set(Read.DataKey.CONTACT_ID, user.getId());
             CONTACT_READ.invoke(data);
         }
         JabberId getId() {
             return user.getId();
         }
         
         private void initIcon(final Boolean isPublisher, final ArtifactReceipt receipt) {
             if ((isPublisher) || ((null != receipt) && (receipt.isSetReceivedOn()))) {
                 setIcon(imageCache.read(TabPanelIcon.USER));
             } else {
                 setIcon(imageCache.read(TabPanelIcon.USER_NOT_RECEIVED));
             }
         }
         private void initText(final User user, final Boolean isPublisher, final ArtifactReceipt receipt) {
             final StringBuffer text = new StringBuffer("<html>").append(user.getName()).append(" ");
             // TODO remove text colour from within html
             text.append("<font color=\"#646464\">");
             if (isPublisher) {
                 text.append(localization.getString("UserPublished"));
             } else if (null == receipt || !receipt.isSetReceivedOn()) {
                 text.append(localization.getString("UserDidNotReceive"));
             } else {
                 text.append(localization.getString("UserReceived",
                         FUZZY_DATE_FORMAT.format(receipt.getReceivedOn())));
             }
             text.append("</font></html>");
             setText(text.toString());
         }
     }
 
     /** A version cell. */
     final class VersionCell extends AbstractVersionCell {
         private final ContainerVersion version;
         private VersionCell(final ContainerVersion version,
                 final Map<DocumentVersion, Delta> documentVersions,
                 final Map<User, ArtifactReceipt> users, final User publishedBy) {
             super();
             this.version = version;
             initText(version, publishedBy);
             if (isComment()) {
                 setIcon(imageCache.read(TabPanelIcon.VERSION_WITH_COMMENT));
             } else {
                 setIcon(imageCache.read(TabPanelIcon.VERSION)); 
             }
             int countCells = 0;
             for (final Entry<DocumentVersion, Delta> entry : documentVersions.entrySet()) {
                 addContentCell(new DocumentVersionCell(entry.getKey(), entry.getValue()));
                 countCells++;
             }
             addContentCell(new UserCell(publishedBy, Boolean.TRUE, null));
             countCells++;
             for (final Entry<User, ArtifactReceipt> entry : users.entrySet()) {
                 addContentCell(new UserCell(entry.getKey(), Boolean.FALSE, entry.getValue()));
                 countCells++;
             }
             for (int i = countCells; i < versionsContentJList.getVisibleRowCount(); i++) {
                 addContentCell(new ContentFillCell());
             }
         }
         @Override
         public void showPopup(final Component invoker, final int x, final int y) {
             popupFactory.createVersionPopup(version).show(invoker, x, y);
         }
         @Override
         protected void doubleClick(final Component invoker, final MouseEvent e) {
             if (isComment()) {
                 final Data data = new Data(2);
                 data.set(ReadVersion.DataKey.CONTAINER_ID, version.getArtifactId());
                 data.set(ReadVersion.DataKey.VERSION_ID, version.getVersionId());
                 CONTAINER_READ_VERSION.invoke(data);
             }
         }
         Long getArtifactId() {
             return version.getArtifactId();
         }
         Long getVersionId() {
             return version.getVersionId();
         }
         Boolean isComment() {
             return ((version.isSetComment()) && (version.getComment().length()>0));
         }
         private void initText(final ContainerVersion version, final User publishedBy) {
             setText(localization.getString("Version", FUZZY_DATE_FORMAT.format(version.getCreatedOn()), publishedBy.getName()));
         }
     }
 }
