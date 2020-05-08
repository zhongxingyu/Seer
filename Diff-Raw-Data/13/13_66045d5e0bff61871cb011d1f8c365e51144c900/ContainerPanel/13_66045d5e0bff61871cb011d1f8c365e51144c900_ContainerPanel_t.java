 /*
  * Created On: October 6, 2006, 5:03 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.swing.SwingUtil;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.container.ContainerVersionArtifactVersionDelta.Delta;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.container.ContainerDraft;
 
 import com.thinkparity.ophelia.browser.Constants.Colors;
 import com.thinkparity.ophelia.browser.application.browser.BrowserSession;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Fonts;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.FileIconReader;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.MainPanelImageCache.TabPanelIcon;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.Cell;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.DefaultCell;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.EastCell;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.EastCellRenderer;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.WestCell;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.WestCellRenderer;
 import com.thinkparity.ophelia.browser.util.localization.MainCellL18n;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public class ContainerPanel extends DefaultTabPanel {
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JLabel additionalTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel collapsedJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel eastCountJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel eastFirstJLabel = new javax.swing.JLabel();
     private final javax.swing.JList eastJList = new javax.swing.JList();
     private final javax.swing.JPanel eastJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel eastLastJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel eastNextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel eastPreviousJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel expandedJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel expansionJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel iconJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel textJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel westCountJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel westFirstJLabel = new javax.swing.JLabel();
     private final javax.swing.JList westJList = new javax.swing.JList();
     private final javax.swing.JPanel westJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel westLastJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel westNextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel westPreviousJLabel = new javax.swing.JLabel();
     // End of variables declaration//GEN-END:variables
 
     /** A <code>Container</code>. */
     protected Container container;
 
     /** The container's created by <code>User</code>. */
     protected User containerCreatedBy;
 
     /** A <code>ContainerDraft</code>. */
     protected ContainerDraft draft;
 
     /** The expanded <code>Boolean</code> state. */
     protected boolean expanded;
 
     /** The container tab's <code>DefaultActionDelegate</code>. */
     private ActionDelegate actionDelegate;
 
     /** The east list <code>DefaultListModel</code>. */
     private final DefaultListModel eastListModel;
 
     /** A  <code>FileIconReader</code>. */
     private final FileIconReader fileIconReader;
 
     /** The most recent <code>ContainerVersion</code>. */
     private ContainerVersion latestVersion;
 
     /** The panel localization. */
     private final MainCellL18n localization;
 
     /** The container tab's <code>PopupDelegate</code>. */
     private PopupDelegate popupDelegate;
 
     /** The west list <code>DefaultListModel</code>. */
     private final DefaultListModel westListModel;
 
     /**
      * Create ContainerPanel.
      * 
      */
     public ContainerPanel(final BrowserSession session) {
         super(session);
         this.eastListModel = new DefaultListModel();
         this.expanded = Boolean.FALSE;
         this.fileIconReader = new FileIconReader();
         this.localization = new MainCellL18n("ContainerPanel");
         this.westListModel = new DefaultListModel();
         // NOCOMMIT-BEGIN
         this.eastCountJLabel.setVisible(false);
         this.eastFirstJLabel.setVisible(false);
         this.eastLastJLabel.setVisible(false);
         this.eastNextJLabel.setVisible(false);
         this.eastPreviousJLabel.setVisible(false);
         this.westCountJLabel.setVisible(false);
         this.westFirstJLabel.setVisible(false);
         this.westLastJLabel.setVisible(false);
         this.westNextJLabel.setVisible(false);
         this.westPreviousJLabel.setVisible(false);
         // NOCOMMIT-END        
         initComponents();
     }
 
     /**
      * Collapse the panel.
      * 
      */
     public void collapse() {
         doCollapse(true);
     }
 
     /**
      * Obtain actionDelegate.
      *
      * @return A ContainerTabActionDelegate.
      */
     public ActionDelegate getActionDelegate() {
         return actionDelegate;
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
      * Obtain the container draft.
      * 
      * @return A <code>ContainerDraft</code>.
      */
     public ContainerDraft getDraft() {
         return draft;
     }
 
     /**
      * Obtain the id for the tab panel.  In this case it's a container id.
      *
      * @return An id <code>Object</code>.
      */
     @Override
     public Object getId() {
         return new StringBuffer(getClass().getName()).append("//")
             .append(container.getId()).toString();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel#getPopupDelegate()
      * 
      */
     public PopupDelegate getPopupDelegate() {
         return popupDelegate;
     }
 
     /**
      * Determine the expanded state.
      * 
      * @return A <code>Boolean</code> expanded state.
      */
     public Boolean isExpanded() {
         return Boolean.valueOf(expanded);
     }
 
     /**
      * Set actionDelegate.
      *
      * @param actionDelegate
      *		A ContainerTabActionDelegate.
      */
     public void setActionDelegate(final ActionDelegate actionDelegate) {
         this.actionDelegate = actionDelegate;
     }
 
     /**
      * Set the expanded state.
      * 
      * @param expanded
      *            A <code>Boolean</code> expanded state.
      */
     public void setExpanded(final Boolean expanded) {
         if (expanded.booleanValue())
             doExpand(false);
         else
             doCollapse(false);
     }
 
     /**
      * Expand the panel.
      *
      */
     public void expand() {
         doExpand(true);
     }
 
     /**
      * Set the panel data.
      * 
      * @param container
      *            A <code>Container</code>.
      * @param draft
      *            A <code>ContainerDraft</code>.
      * @param latestVersion
      *            The latest <code>ContainerVersion</code>.
      */
     public void setPanelData(
             final Container container,
             final User containerCreatedBy,
             final ContainerDraft draft,
             final ContainerVersion latestVersion,
             final List<ContainerVersion> versions,
             final Map<ContainerVersion, Map<DocumentVersion, Delta>> documentVersions,
             final Map<ContainerVersion, Map<User, ArtifactReceipt>> publishedTo,
             final Map<ContainerVersion, User> publishedBy) {
         this.container = container;
         this.containerCreatedBy = containerCreatedBy;
         this.draft = draft;
         this.latestVersion = latestVersion;
         westListModel.addElement(new ContainerCell());
         // if a draft exists and the user is the creator of the draft or the
         // user has the latest version of the container display a draft element
         if (container.isLocalDraft()) {
             westListModel.addElement(new DraftCell());
         }
         // display all version elements
         for (final ContainerVersion version : versions) {
             // NOCOMMIT-Begin
             if (NUMBER_VISIBLE_ROWS > westListModel.size())
                 westListModel.addElement(new VersionCell(version, documentVersions
                         .get(version), publishedTo.get(version), publishedBy
                         .get(version)));
             // NOCOMMIT-End
         }
         iconJLabel.setIcon(container.isBookmarked()
                 ? IMAGE_CACHE.read(TabPanelIcon.CONTAINER_BOOKMARK)
                 : IMAGE_CACHE.read(TabPanelIcon.CONTAINER));
         reloadText();
         restoreSelection("eastJList", eastListModel, eastJList);
         restoreSelection("westJList", westListModel, westJList);
     }
 
     /**
      * Set popupDelegate.
      *
      * @param popupDelegate
      *		A ContainerTabPopupDelegate.
      */
     public void setPopupDelegate(final PopupDelegate popupDelegate) {
         this.popupDelegate = popupDelegate;
     }
 
     /**
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      * 
      */
     @Override
     protected void paintComponent(final Graphics g) {
         super.paintComponent(g);
         if (expanded) {
             renderer.paintExpandedBackground(g, this);
             if (!westJList.isSelectionEmpty()) {
                 final int selectionIndex = westJList.getSelectedIndex();
                 renderer.paintExpandedBackgroundWest(g, westJPanel.getWidth(), getHeight(), selectionIndex, this);
                 renderer.paintExpandedBackgroundCenter(g, westJPanel.getWidth(), getHeight(), selectionIndex, this);
                 renderer.paintExpandedBackgroundEast(g, westJPanel.getWidth(), getHeight(), this);
             }
         } else {
             renderer.paintBackground(g, getWidth(), getHeight());
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel#repaintLists()
      *
      */
     @Override
     protected void repaintLists() {
         eastJList.repaint();
         westJList.repaint();
     }
 
     private void collapsedJPanelMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_collapsedJPanelMousePressed
         logger.logApiId();
         logger.logVariable("e", e);
         if (e.isPopupTrigger()) {
             tabDelegate.toggleExpansion(this);
             popupDelegate.initialize(expandedJPanel, e.getX(), e.getY());
             popupDelegate.showForContainer(container);
         }
     }//GEN-LAST:event_collapsedJPanelMousePressed
 
     private void collapsedJPanelMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_collapsedJPanelMouseReleased
         logger.logApiId();
         logger.logVariable("e", e);
         if (e.isPopupTrigger()) {
             tabDelegate.toggleExpansion(this);
             popupDelegate.initialize(expandedJPanel, e.getX(), e.getY());
             popupDelegate.showForContainer(container);
         } else {
             tabDelegate.toggleExpansion(this);
         }
     }//GEN-LAST:event_collapsedJPanelMouseReleased
 
     /**
      * Expand the panel.
      * 
      * @param animate
      *            Whether or not to animate.
      */
     private void doExpand(final boolean animate) {
         this.expanded = true;
         remove(collapsedJPanel);
         add(expandedJPanel, constraints.clone());
 
         if (animate) {
             animator.expand(20, 165);
         } else {
             final Dimension preferredSize = expandedJPanel.getPreferredSize();
             preferredSize.height = 165;
             expandedJPanel.setPreferredSize(preferredSize);
         }
 
         revalidate();
         reload();
         repaint();
     }
 
     /**
      * Collapse the panel.
      * 
      * @param animate
      *            Whether or not to animate.
      */
     private void doCollapse(final boolean animate) {
         this.expanded = false;
         remove(expandedJPanel);
         add(collapsedJPanel, constraints.clone());
 
         if (animate) {
             animator.collapse(20, 25);
         } else {
             final Dimension preferredSize = expandedJPanel.getPreferredSize();
             preferredSize.height = 25;
             expandedJPanel.setPreferredSize(preferredSize);
         }
 
         revalidate();
         reload();
         repaint();
     }
 
     private void eastJListFocusGained(java.awt.event.FocusEvent e) {//GEN-FIRST:event_eastJListFocusGained
         jListFocusGained((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_eastJListFocusGained
 
     private void eastJListFocusLost(java.awt.event.FocusEvent e) {//GEN-FIRST:event_eastJListFocusLost
         jListFocusLost((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_eastJListFocusLost
 
     private void eastJListMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_eastJListMouseClicked
         jListMouseClicked((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_eastJListMouseClicked
 
     private void eastJListMouseEntered(java.awt.event.MouseEvent e) {//GEN-FIRST:event_eastJListMouseEntered
         jListMouseEntered(eastListModel, (javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_eastJListMouseEntered
 
     private void eastJListMouseExited(java.awt.event.MouseEvent e) {//GEN-FIRST:event_eastJListMouseExited
         jListMouseExited((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_eastJListMouseExited
 
     private void eastJListMouseMoved(java.awt.event.MouseEvent e) {//GEN-FIRST:event_eastJListMouseMoved
         jListMouseMoved(
                 (DefaultListModel) ((javax.swing.JList) e.getSource()).getModel(),
                 (javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_eastJListMouseMoved
 
     private void eastJListMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_eastJListMousePressed
         jListMousePressed((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_eastJListMousePressed
 
     private void eastJListMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_eastJListMouseReleased
         jListMouseReleased((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_eastJListMouseReleased
 
     private void eastJListValueChanged(javax.swing.event.ListSelectionEvent e) {//GEN-FIRST:event_eastJListValueChanged
         if (e.getValueIsAdjusting() || ((javax.swing.JList) e.getSource()).isSelectionEmpty()) {
             repaint();
             return;
         }
         saveSelection("eastJList", (javax.swing.JList) e.getSource());
     }//GEN-LAST:event_eastJListValueChanged
 
     private void expansionJLabelMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_expansionJLabelMouseClicked
         tabDelegate.toggleExpansion(this);
     }//GEN-LAST:event_expansionJLabelMouseClicked
 
     private void expansionJLabelMouseEntered(java.awt.event.MouseEvent e) {//GEN-FIRST:event_expansionJLabelMouseEntered
         SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), java.awt.Cursor.HAND_CURSOR);
     }//GEN-LAST:event_expansionJLabelMouseEntered
 
     private void expansionJLabelMouseExited(java.awt.event.MouseEvent e) {//GEN-FIRST:event_expansionJLabelMouseExited
         SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), java.awt.Cursor.DEFAULT_CURSOR);
     }//GEN-LAST:event_expansionJLabelMouseExited
 
     private void iconJLabelMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_iconJLabelMouseClicked
         actionDelegate.invokeForContainer(container);
     }//GEN-LAST:event_iconJLabelMouseClicked
 
     private void iconJLabelMouseEntered(java.awt.event.MouseEvent e) {//GEN-FIRST:event_iconJLabelMouseEntered
         SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), java.awt.Cursor.HAND_CURSOR);
     }//GEN-LAST:event_iconJLabelMouseEntered
 
     private void iconJLabelMouseExited(java.awt.event.MouseEvent e) {//GEN-FIRST:event_iconJLabelMouseExited
         SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), java.awt.Cursor.DEFAULT_CURSOR);
     }//GEN-LAST:event_iconJLabelMouseExited
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         setLayout(new java.awt.GridBagLayout());
 
         setBorder(BORDER);
         collapsedJPanel.setLayout(new java.awt.GridBagLayout());
 
         collapsedJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent e) {
                 collapsedJPanelMousePressed(e);
             }
             public void mouseReleased(java.awt.event.MouseEvent e) {
                 collapsedJPanelMouseReleased(e);
             }
         });
 
         iconJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/IconContainer.png")));
         iconJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 iconJLabelMouseClicked(e);
             }
             public void mouseEntered(java.awt.event.MouseEvent e) {
                 iconJLabelMouseEntered(e);
             }
             public void mouseExited(java.awt.event.MouseEvent e) {
                 iconJLabelMouseExited(e);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.insets = new java.awt.Insets(4, 32, 0, 4);
         collapsedJPanel.add(iconJLabel, gridBagConstraints);
 
         textJLabel.setText("!Package Text!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 3, 4, 0);
         collapsedJPanel.add(textJLabel, gridBagConstraints);
 
         additionalTextJLabel.setForeground(Colors.Browser.List.INNER_LIST_SELECTION_BORDER);
         additionalTextJLabel.setText("!Package Additional Text!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 4, 0);
         collapsedJPanel.add(additionalTextJLabel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(collapsedJPanel, gridBagConstraints);
 
         expandedJPanel.setLayout(new java.awt.GridBagLayout());
 
         expandedJPanel.setOpaque(false);
         westJPanel.setLayout(new java.awt.GridBagLayout());
 
         westJPanel.setOpaque(false);
         expansionJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/IconCollapse.png")));
         expansionJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 expansionJLabelMouseClicked(e);
             }
             public void mouseEntered(java.awt.event.MouseEvent e) {
                 expansionJLabelMouseEntered(e);
             }
             public void mouseExited(java.awt.event.MouseEvent e) {
                 expansionJLabelMouseExited(e);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 8);
         westJPanel.add(expansionJLabel, gridBagConstraints);
 
         westJList.setModel(westListModel);
         westJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         westJList.setCellRenderer(new WestCellRenderer());
         westJList.setOpaque(false);
         westJList.setVisibleRowCount(NUMBER_VISIBLE_ROWS);
         westJList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent e) {
                 westJListMouseMoved(e);
             }
         });
         westJList.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent e) {
                 westJListFocusGained(e);
             }
             public void focusLost(java.awt.event.FocusEvent e) {
                 westJListFocusLost(e);
             }
         });
         westJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                 westJListValueChanged(e);
             }
         });
         westJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 westJListMouseClicked(e);
             }
             public void mouseEntered(java.awt.event.MouseEvent e) {
                 westJListMouseEntered(e);
             }
             public void mouseExited(java.awt.event.MouseEvent e) {
                 westJListMouseExited(e);
             }
             public void mousePressed(java.awt.event.MouseEvent e) {
                 westJListMousePressed(e);
             }
             public void mouseReleased(java.awt.event.MouseEvent e) {
                 westJListMouseReleased(e);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         westJPanel.add(westJList, gridBagConstraints);
 
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("localization/ListItem_Messages"); // NOI18N
         westLastJLabel.setText(bundle.getString("ContainerPanel.firstJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         westJPanel.add(westLastJLabel, gridBagConstraints);
 
         westNextJLabel.setText(bundle.getString("ContainerPanel.nextJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         westJPanel.add(westNextJLabel, gridBagConstraints);
 
         westCountJLabel.setText(bundle.getString("ContainerPanel.countJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         westJPanel.add(westCountJLabel, gridBagConstraints);
 
         westPreviousJLabel.setText(bundle.getString("ContainerPanel.previousJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         westJPanel.add(westPreviousJLabel, gridBagConstraints);
 
         westFirstJLabel.setText(bundle.getString("ContainerPanel.firstJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         westJPanel.add(westFirstJLabel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 1.0;
         expandedJPanel.add(westJPanel, gridBagConstraints);
 
         eastJPanel.setLayout(new java.awt.GridBagLayout());
 
         eastJPanel.setOpaque(false);
         eastJList.setModel(eastListModel);
         eastJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         eastJList.setCellRenderer(new EastCellRenderer());
         eastJList.setOpaque(false);
         eastJList.setVisibleRowCount(NUMBER_VISIBLE_ROWS);
         eastJList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseMoved(java.awt.event.MouseEvent e) {
                 eastJListMouseMoved(e);
             }
         });
         eastJList.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent e) {
                 eastJListFocusGained(e);
             }
             public void focusLost(java.awt.event.FocusEvent e) {
                 eastJListFocusLost(e);
             }
         });
         eastJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                 eastJListValueChanged(e);
             }
         });
         eastJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 eastJListMouseClicked(e);
             }
             public void mouseEntered(java.awt.event.MouseEvent e) {
                 eastJListMouseEntered(e);
             }
             public void mouseExited(java.awt.event.MouseEvent e) {
                 eastJListMouseExited(e);
             }
             public void mousePressed(java.awt.event.MouseEvent e) {
                 eastJListMousePressed(e);
             }
             public void mouseReleased(java.awt.event.MouseEvent e) {
                 eastJListMouseReleased(e);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         eastJPanel.add(eastJList, gridBagConstraints);
 
         eastLastJLabel.setText(bundle.getString("ContainerPanel.lastJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         eastJPanel.add(eastLastJLabel, gridBagConstraints);
 
         eastFirstJLabel.setText(bundle.getString("ContainerPanel.firstJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         eastJPanel.add(eastFirstJLabel, gridBagConstraints);
 
         eastNextJLabel.setText(bundle.getString("ContainerPanel.nextJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         eastJPanel.add(eastNextJLabel, gridBagConstraints);
 
         eastCountJLabel.setText(bundle.getString("ContainerPanel.countJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         eastJPanel.add(eastCountJLabel, gridBagConstraints);
 
         eastPreviousJLabel.setText(bundle.getString("ContainerPanel.previousJLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         eastJPanel.add(eastPreviousJLabel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 1.0;
         expandedJPanel.add(eastJPanel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(expandedJPanel, gridBagConstraints);
 
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Determine if there is a latest version or not.
      * 
      * @return True if the version is the latest.
      */
     private boolean isLatest() {
         return container.isLocalDraft() || container.isLatest();
     }
 
     /**
      * Reload the container panel based upon internal criteria; more
      * specifically whether or not the panel is expanded controls the text to
      * display; the icons and fonts to use.
      * 
      */
     private void reload() {
         reloadText();
     }
 
     /**
      * Reload the text on the panel.
      *
      */
     private void reloadText() {
         // if expanded display the container name
         // if not expanded display the container name; if there exists a draft
         // and the the latest version also display the draft owner; otherwise
         // if there exists the latest version display the published on date
         textJLabel.setText(container.getName());
         if (!expanded) {
             if (container.isDraft() && container.isLatest()) {
                 additionalTextJLabel.setText(localization.getString(
                         "ContainerMessageDraftOwner",
                         draft.getOwner().getName()));
             } else if (null != latestVersion) {
                 additionalTextJLabel.setText(localization.getString(
                         "ContainerMessagePublishDate",
                         formatFuzzy(latestVersion.getUpdatedOn())));
             } else {
                 additionalTextJLabel.setText("");
             }
         }
         if (!expanded && !container.isSeen().booleanValue()) {
             textJLabel.setFont(Fonts.DefaultFontBold);
             additionalTextJLabel.setFont(Fonts.DefaultFontBold);
         }
         if (!isLatest()) {
             textJLabel.setForeground(
                     Colors.Browser.List.LIST_LACK_MOST_RECENT_VERSION_FG);
         }
     }
 
     private void westJListFocusGained(java.awt.event.FocusEvent e) {//GEN-FIRST:event_westJListFocusGained
         jListFocusGained((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_westJListFocusGained
 
     private void westJListFocusLost(java.awt.event.FocusEvent e) {//GEN-FIRST:event_westJListFocusLost
         jListFocusLost((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_westJListFocusLost
 
     private void westJListMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_westJListMouseClicked
         westJListMouseClicked("westJList", (javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_westJListMouseClicked
 
     private void westJListMouseEntered(java.awt.event.MouseEvent e) {//GEN-FIRST:event_westJListMouseEntered
         westJListSetCursor((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_westJListMouseEntered
 
     private void westJListMouseExited(java.awt.event.MouseEvent e) {//GEN-FIRST:event_westJListMouseExited
         jListMouseExited((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_westJListMouseExited
 
     private void westJListMouseMoved(java.awt.event.MouseEvent e) {//GEN-FIRST:event_westJListMouseMoved
         westJListSetCursor((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_westJListMouseMoved
 
     private void westJListMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_westJListMousePressed
         jListMousePressed((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_westJListMousePressed
 
     private void westJListMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_westJListMouseReleased
         jListMouseReleased((javax.swing.JList) e.getSource(), e);
     }//GEN-LAST:event_westJListMouseReleased
 
     private void westJListValueChanged(javax.swing.event.ListSelectionEvent e) {//GEN-FIRST:event_westJListValueChanged
         if (e.getValueIsAdjusting() || ((javax.swing.JList) e.getSource()).isSelectionEmpty()) {
             repaint();
             return;
         }
         saveSelection("westJList", (javax.swing.JList) e.getSource());
         eastListModel.clear();
         for (final Object selectedValue : westJList.getSelectedValues()) {
             for (final Cell cell : ((WestCell) selectedValue).getEastCells()) {
                 // NOCOMMIT-Begin
                 if (NUMBER_VISIBLE_ROWS > eastListModel.size())
                     eastListModel.addElement(cell);
                 // NOCOMMIT-End
             }
         }
     }//GEN-LAST:event_westJListValueChanged
 
     /** An east list cell. */
     private abstract class AbstractEastCell extends DefaultCell implements
             EastCell {
         /**
          * Create AbstractEastCell.
          *
          */
         private AbstractEastCell() {
             super();
             setEnabled(isLatest());
         }
     }
 
     /** A west list cell. */
     private abstract class AbstractWestCell extends WestCell {
         /**
          * Create AbstractEastCell.
          *
          */
         private AbstractWestCell() {
             super();
             setEnabled(isLatest());
         }
     }
 
     /** A container cell. */
     private final class ContainerCell extends AbstractWestCell {
         /**
          * Create ContainerCell.
          * 
          */
         private ContainerCell() {
             super();
             add(new ContainerFieldCell(this, "ContainerCreatedBy",
                     containerCreatedBy.getName()));
             add(new ContainerFieldCell(this, "ContainerCreatedOn",
                     formatFuzzy(container.getCreatedOn())));
             add(new ContainerFieldCell(this, "ContainerLatestVersion",
                     null == latestVersion
                     ? null : formatFuzzy(latestVersion.getUpdatedOn())));
             add(new ContainerFieldCell(this, "ContainerDraftOwner",
                     container.isDraft() ? draft.getOwner().getName() : null));
         }
         @Override
         public Icon getIcon() {
             if (container.isBookmarked())
                 return IMAGE_CACHE.read(TabPanelIcon.CONTAINER_BOOKMARK);
             else
                 return IMAGE_CACHE.read(TabPanelIcon.CONTAINER);
         }
         @Override
         public String getText() {
             return container.getName();
         }
         @Override
         public void invokeAction() {
             actionDelegate.invokeForContainer(container);
         }
         @Override
         public void showPopup() {
             popupDelegate.showForContainer(container);
         }
         @Override
         public Boolean isActionAvailable() {
             return Boolean.TRUE;
         }
     }
 
     /** A container value cell. */
     private final class ContainerFieldCell extends AbstractEastCell {
         /** The field localization key <code>String</code>. */
         private final String key;
         /** The parent <code>WestCell</code>. */
         private final WestCell parent;
         /** The field value <code>String</code>. */
         private final String value;
         /**
          * Create ContactFieldCell.
          * 
          * @param name
          *            The field name <code>String</code>.
          * @param value
          *            The field value <code>String</code>.
          */
         private ContainerFieldCell(final WestCell parent, final String key,
                 final String value) {
             super();
             this.key = key;
             this.parent = parent;
             this.value = value;
         }
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.Cell#getText()
          *
          */
         public String getText() {
             if (null == value) {
                 return "";
             } else {
                 return localization.getString(key, value);
             }
         }
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.Cell#invokeAction()
          *
          */
         public void invokeAction() {
             parent.invokeAction();
         }
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.Cell#showPopup()
          *
          */
         public void showPopup() {
             parent.showPopup();
         }
     }
 
     /** A draft cell. */
     private final class DraftCell extends AbstractWestCell {
         /**
          * Create DraftCell.
          *
          */
         private DraftCell() {
             super();
             for (final Document document : draft.getDocuments()) {
                 add(new DraftDocumentCell(document));
             }
         }
         @Override
         public Icon getIcon() {
             return IMAGE_CACHE.read(TabPanelIcon.DRAFT);
         }
         @Override
         public String getText() {
             if (container.isLocalDraft()) {
                 return localization.getString("Draft");
             } else {
                 return localization.getString("DraftNotLocal", draft.getOwner().getName());
             }
         }
         
         @Override
         public void showPopup() {
             popupDelegate.showForDraft(container, draft);
         }
     }
 
     /** A draft document cell. */
     private final class DraftDocumentCell extends AbstractEastCell {
         private final Document document;
         private DraftDocumentCell(final Document document) {
             super();
             this.document = document;
             setIcon(fileIconReader.getIcon(document));
             switch (draft.getState(document)) {
             case ADDED:
             case MODIFIED:
             case REMOVED:
                 setAdditionalText(localization.getString(draft.getState(document)));
                 break;
             case NONE:
                 break;
             default:
                 throw Assert.createUnreachable("UNKNOWN DOCUMENT STATE");
             }
             setText(document.getName());
         }
         @Override
         public void invokeAction() {
             actionDelegate.invokeForDocument(draft, document);
         }
         @Override
         public void showPopup() {
             popupDelegate.showForDraft(container, draft);
         }
     }
 
     /** A version cell. */
     private final class VersionCell extends AbstractWestCell {
         /** A published to <code>User</code>. */
         private final User publishedBy;
         /** A <code>ContainerVersion</code>. */
         private final ContainerVersion version;
         /**
          * Create VersionCell.
          * 
          * @param version
          *            A <code>ContainerVersion</code>.
          * @param documentVersions
          *            A <code>List</code> of <code>DocumentVersion</code>s.
          * @param users
          *            A <code>List</code> of published to
          *            <code>ArtifactReceipt</code>s and <code>User</code>.
          * @param publishedBy
          *            A published by <code>User</code>.
          */
         private VersionCell(final ContainerVersion version,
                 final Map<DocumentVersion, Delta> documentVersions,
                 final Map<User, ArtifactReceipt> users, final User publishedBy) {
             this.version = version;
             this.publishedBy = publishedBy;
             for (final Entry<DocumentVersion, Delta> entry : documentVersions.entrySet()) {
                 add(new VersionDocumentCell(entry.getKey(), entry.getValue()));
             }
             add(new VersionUserCell(publishedBy));
             for (final Entry<User, ArtifactReceipt> entry : users.entrySet()) {
                 add(new VersionUserCell(entry.getKey(), entry.getValue()));
             }
         }
         @Override
         public Icon getIcon() {
             if (version.isSetComment()) {
                 return IMAGE_CACHE.read(TabPanelIcon.VERSION_WITH_COMMENT);
             } else {
                 return IMAGE_CACHE.read(TabPanelIcon.VERSION); 
             }
         }
         @Override
         public String getText() {
             return localization.getString("Version", formatFuzzy(version
                     .getCreatedOn()), publishedBy.getName());
         }
         @Override
         public void invokeAction() {
             actionDelegate.invokeForVersion(version);
         }
         @Override
         public void showPopup() {
             popupDelegate.showForVersion(version);
         }
         @Override
         public Boolean isActionAvailable() {
             return version.isSetComment();
         }
     }
 
     /** A version document cell. */
     private final class VersionDocumentCell extends AbstractEastCell {
         /** A <code>Delta</code>. */
         private final Delta delta;
         /** A <code>DocumentVersion</code>. */
         private final DocumentVersion version;
         /**
          * Create VersionDocumentCell.
          * 
          * @param version
          *            A <code>DocumentVersion</code>.
          * @param delta
          *            A <code>Delta</code>.
          */
         private VersionDocumentCell(final DocumentVersion version,
                 final Delta delta) {
             super();
             this.delta = delta;
             this.version = version;
             setIcon(fileIconReader.getIcon(version));
             switch (delta) {
             case ADDED:
             case MODIFIED:
             case REMOVED:
                 setAdditionalText(localization.getString(delta));
                 break;
             case NONE:
                 break;
             default:
                 throw Assert.createUnreachable("UNKNOWN DOCUMENT STATE");
             }
             setText(version.getName());
         }
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.DefaultCell#invokeAction()
          */
         @Override
         public void invokeAction() {
             actionDelegate.invokeForDocument(version, delta);
         }
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.DefaultCell#showPopup()
          */
         @Override
         public void showPopup() {
             popupDelegate.showForDocument(version, delta);
         }
     }
 
     /** A user cell. */
     private final class VersionUserCell extends AbstractEastCell {
         /** A <code>User</code>. */
         private final User user;
         /**
          * Create VersionUserCell.
          * 
          * @param user
          *            A <code>User</code>.
          */
         private VersionUserCell(final User user) {
             super();
             this.user = user;
             setIcon(IMAGE_CACHE.read(TabPanelIcon.USER));
             setText(user.getName());
             setAdditionalText(localization.getString("UserPublished"));
         }
         /**
          * Create VersionUserCell.
          * 
          * @param user
          *            A <code>User</code>.
          * @param receipt
          *            An <code>ArtifactReceipt</code>.
          */
         private VersionUserCell(final User user, final ArtifactReceipt receipt) {
             super();
             this.user = user;
            setIcon(receipt.isSetReceivedOn()
                    ? IMAGE_CACHE.read(TabPanelIcon.USER)
                    : IMAGE_CACHE.read(TabPanelIcon.USER_NOT_RECEIVED));
             setText(user.getName());
             setAdditionalText(receipt.isSetReceivedOn()
                     ? localization.getString("UserReceived",
                             formatFuzzy(receipt.getReceivedOn()))
                     : localization.getString("UserDidNotReceive"));
                     
         }
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.DefaultCell#invokeAction()
          */
         @Override
         public void invokeAction() {
             actionDelegate.invokeForUser(user);
         }
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel.DefaultCell#showPopup()
          */
         @Override
         public void showPopup() {
             popupDelegate.showForUser(user);
         }
     }
 }
