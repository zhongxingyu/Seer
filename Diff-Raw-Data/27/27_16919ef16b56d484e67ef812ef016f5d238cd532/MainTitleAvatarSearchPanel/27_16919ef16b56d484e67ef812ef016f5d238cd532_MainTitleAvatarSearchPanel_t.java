 /*
  * Created on July 30, 2006, 3:30 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar;
 
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.swing.SwingUtil;
 import com.thinkparity.codebase.swing.border.TopBottomBorder;
 
 import com.thinkparity.ophelia.browser.Constants.Colors;
 import com.thinkparity.ophelia.browser.Constants.Search;
 import com.thinkparity.ophelia.browser.application.browser.component.MenuFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.MainTitleAvatar.TabId;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarFilterDelegate;
 import com.thinkparity.ophelia.browser.util.ImageIOUtil;
 
 /**
  *
  * @author raymond@thinkparity.com
  */
 public class MainTitleAvatarSearchPanel extends MainTitleAvatarAbstractPanel {
 
     /** @see java.io.Serializable */
     private static final long serialVersionUID = 1;
 
     /** The <code>BufferedImage</code> for the halo. */
     public static final BufferedImage HALO;
 
     /** The <code>BufferedImage</code> for the search background. */
     public static final BufferedImage SEARCH_BACKGROUND;
 
     /** The <code>BufferedImage</code> for the search left. */
     private static final ImageIcon SEARCH_LEFT;
 
     /** The <code>BufferedImage</code> for the search left when active (menu available). */
     private static final ImageIcon SEARCH_LEFT_ACTIVE;
 
     /** The <code>BufferedImage</code> for the search left when a filter is active. */
     private static final ImageIcon SEARCH_LEFT_FILTERING;
 
     /** The <code>BufferedImage</code> for the search left during rollover. */
     private static final ImageIcon SEARCH_LEFT_ROLLOVER;
 
     static {
         HALO = ImageIOUtil.read("BrowserTitle_SearchHalo.png");
         SEARCH_BACKGROUND = ImageIOUtil.read("BrowserTitle_SearchBackground.png");
         SEARCH_LEFT = ImageIOUtil.readIcon("BrowserTitle_SearchLeft.png");
         SEARCH_LEFT_ACTIVE = ImageIOUtil.readIcon("BrowserTitle_SearchLeftActive.png");
         SEARCH_LEFT_FILTERING = ImageIOUtil.readIcon("BrowserTitle_SearchLeftFiltering.png");
         SEARCH_LEFT_ROLLOVER = ImageIOUtil.readIcon("BrowserTitle_SearchLeftRollover.png");
     }
 
     /** The search activation timer. */
     private Timer searchActivationTimer;
 
     /** The filter delegate. */
     private TabAvatarFilterDelegate filterDelegate;
 
     /** The filter popup delegate. */
     private final FilterPopupDelegate filterPopupDelegate;
 
     /** Creates new form BrowserTitleSearch */
     public MainTitleAvatarSearchPanel() {
         super();
         this.filterPopupDelegate = new FilterPopupDelegate();
         initComponents();
         addMoveListener(this);
         addPopupListener();
         new FocusHelper().addFocusListener(this);
         new Resizer(getBrowser(), this, Boolean.FALSE, Resizer.ResizeEdges.RIGHT);
     }
 
     /**
      * Clear the filter.
      */
     public void clearFilter() {
         filterDelegate.clearFilter();
         setFilterIcon(Boolean.FALSE);
     }
 
     /**
      * Clear the search.
      */
     public void clearSearch() {
        searchJTextField.setText(null);
        if (searchActivationTimer != null) {
            searchActivationTimer.stop();
         }
        applySearch();
     }
 
     /**
      * Determine if the search control has focus.
      * 
      * @return true if the search control has focus.
      */
     public boolean hasFocus() {
         return searchJTextField.hasFocus();
     }
 
     /**
      * Reload the tab filter.
      * 
      * @param tabId
      *            A tab.
      */
     public void reloadTabFilter(final TabId tabId) {
         filterDelegate = getBrowser().getFilterDelegate(tabId);
         clearFilter();
     }
 
     /**
      * Request focus.
      */
     public void requestFocus() {
         searchJTextField.requestFocusInWindow();
     }
 
     /**
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      */
     @Override
     protected void paintComponent(final Graphics g) {
         super.paintComponent(g);
         final Graphics2D g2 = (Graphics2D) g.create();
         try {
             if (searchJTextField.isFocusOwner()) {
                 final Point leftLocation = leftJLabel.getLocation();
                 g2.drawImage(HALO, leftLocation.x - 1, leftLocation.y - 1, null);
             } else {
                 final Point leftLocation = searchJTextField.getLocation();
                 g2.drawImage(SEARCH_BACKGROUND, leftLocation.x, leftLocation.y + 1, null); 
             }
         } finally {
             g2.dispose();
         }
     }
 
     /**
      * Add popup listener. This shows a popup when shift-f10 is pressed.
      */
     private void addPopupListener() {
         new KeyboardPopupHelper().addPopupListener(searchJTextField, new Runnable() {
             public void run() {
                 if (isFilterActive()) {
                     showFilterMenu();
                 }
             }
         });
     }
 
     /**
      * Apply a search.
      * 
      */
     private void applySearch() {
         final boolean searchIsFocusOwner = searchJTextField.isFocusOwner();
         mainTitleAvatar.getController().applySearch(
                 SwingUtil.extract(searchJTextField));
         if (searchIsFocusOwner) {
             requestFocus();
         }
     }
 
     /**
      * Get the filter menu width.
      * 
      * @return The filter menu width <code>int</code>.
      */
     private int getFilterMenuWidth() {
         // The menu width is slightly less than the width of the search control.
         return leftJLabel.getWidth() + rightJLabel.getWidth() + searchJTextField.getWidth() - 2;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         searchJTextField = new javax.swing.JTextField();
         searchJTextField.setBorder(new TopBottomBorder(Colors.Browser.MainTitle.SEARCH_OUTLINE));
         searchJTextField.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(final DocumentEvent e) {
                 searchJTextFieldChangedUpdate(e);
             }
             public void insertUpdate(final DocumentEvent e) {
                 searchJTextFieldInsertUpdate(e);
             }
             public void removeUpdate(final DocumentEvent e) {
                 searchJTextFieldRemoveUpdate(e);
             }
         });
 
         setLayout(new java.awt.GridBagLayout());
 
         setOpaque(false);
         rightJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/BrowserTitle_SearchRight.png")));
         rightJLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
         gridBagConstraints.insets = new java.awt.Insets(11, 0, 4, 4);
         add(rightJLabel, gridBagConstraints);
 
         searchJTextField.setMargin(new java.awt.Insets(3, 5, 0, 4));
         searchJTextField.setMinimumSize(new java.awt.Dimension(1, 19));
         searchJTextField.setOpaque(false);
         searchJTextField.setPreferredSize(new java.awt.Dimension(1, 19));
         searchJTextField.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 searchJTextFieldMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 searchJTextFieldMouseExited(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 searchJTextFieldMousePressed(evt);
             }
         });
         searchJTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 searchJTextFieldActionPerformed(evt);
             }
         });
         searchJTextField.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 searchJTextFieldFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 searchJTextFieldFocusLost(evt);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.ipadx = 109;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
         gridBagConstraints.insets = new java.awt.Insets(11, 0, 4, 0);
         add(searchJTextField, gridBagConstraints);
 
         leftJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/BrowserTitle_SearchLeft.png")));
         leftJLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
         leftJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 leftJLabelMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 leftJLabelMouseExited(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 leftJLabelMousePressed(evt);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(11, 4, 4, 0);
         add(leftJLabel, gridBagConstraints);
 
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Determine if the filter is active for this tab.
      * 
      * @return true if the filter is active for this tab.
      */
     private boolean isFilterActive() {
         return !filterDelegate.getFilterBy().isEmpty();
     }
 
     private void leftJLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leftJLabelMousePressed
         if (isFilterActive() && !MenuFactory.isPopupMenu()) {
             showFilterMenu();
         }
     }//GEN-LAST:event_leftJLabelMousePressed
 
     private void leftJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leftJLabelMouseExited
         setFilterIcon(Boolean.FALSE);
     }//GEN-LAST:event_leftJLabelMouseExited
 
     private void leftJLabelMouseEntered(java.awt.event.MouseEvent evt) {                                        
         setFilterIcon(Boolean.TRUE);
     }                                                                          
 
     private void searchJTextFieldMousePressed(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_searchJTextFieldMousePressed
         if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && isFilterActive() && !MenuFactory.isPopupMenu()) {
             showFilterMenu();
         }
     }//GEN-LAST:event_searchJTextFieldMousePressed
 
     private void searchJTextFieldMouseExited(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_searchJTextFieldMouseExited
         Window window = SwingUtilities.getWindowAncestor(this);
         if (null!=window) {
             window.setCursor(null);
         }
     }// GEN-LAST:event_searchJTextFieldMouseExited
 
     private void searchJTextFieldMouseEntered(final java.awt.event.MouseEvent e) {// GEN-FIRST:event_searchJTextFieldMouseEntered
         Window window = SwingUtilities.getWindowAncestor(this);
         if (null!=window) {
             window.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
         }
     }// GEN-LAST:event_searchJTextFieldMouseEntered
 
     private void searchJTextFieldFocusLost(final java.awt.event.FocusEvent e) {//GEN-FIRST:event_searchJTextFieldFocusLost
         repaint();
     }//GEN-LAST:event_searchJTextFieldFocusLost
 
     private void searchJTextFieldFocusGained(final java.awt.event.FocusEvent e) {//GEN-FIRST:event_searchJTextFieldFocusGained
         repaint();
     }//GEN-LAST:event_searchJTextFieldFocusGained
 
     /**
      * When the search text field's document is updated; initialize a timer to
      * run the search after a delay. Each update to the text field's document
      * re-sets the timer.
      * 
      * @param e
      *            The document event.
      */
     private void searchFieldUpdated(final DocumentEvent e) {
         if (searchActivationTimer == null) {
             searchActivationTimer= new Timer(Search.ACTIVATION_DELAY, new ActionListener() {
                 public void actionPerformed(final ActionEvent timerEvent) {
                     searchActivationTimer.stop();
                     applySearch();
                 }
             });
             searchActivationTimer.start();
         }
         else {
             searchActivationTimer.restart();
         }
     }
 
     /**
      * When the user performs an action on the search text field (i.e. hits
      * enter) the activation timer is re-set and the search run immetiately.
      * 
      * @param evt
      *            The action event.
      */
     private void searchJTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchJTextFieldActionPerformed
         if (searchActivationTimer != null) {
             searchActivationTimer.stop();
         }
         applySearch();
     }//GEN-LAST:event_searchJTextFieldActionPerformed
 
     /**
      * @see MainTitleAvatarSearchPanel#searchFieldUpdated(DocumentEvent)
      * 
      */
     private void searchJTextFieldChangedUpdate(final DocumentEvent e) {}
 
     /**
      * @see MainTitleAvatarSearchPanel#searchFieldUpdated(DocumentEvent)
      * 
      */
     private void searchJTextFieldInsertUpdate(final DocumentEvent e) {
         searchFieldUpdated(e);
     }
 
     /**
      * @see MainTitleAvatarSearchPanel#searchFieldUpdated(DocumentEvent)
      * 
      */
     private void searchJTextFieldRemoveUpdate(final DocumentEvent e) {
         searchFieldUpdated(e);
     }
 
     /**
      * Set the appropriate filter icon.
      * 
      * @param mouseOver
      *            A mouse rollover <code>Boolean</code>.
      */
     private void setFilterIcon(final Boolean mouseOver) {
         Assert.assertNotNull("Null filter delegate in search avatar.", filterDelegate);
         if (!isFilterActive()) {
             leftJLabel.setIcon(SEARCH_LEFT);
         } else if (mouseOver) {
             leftJLabel.setIcon(SEARCH_LEFT_ROLLOVER);
         } else if (filterDelegate.isFilterApplied()) {
             leftJLabel.setIcon(SEARCH_LEFT_FILTERING);
         } else {
             leftJLabel.setIcon(SEARCH_LEFT_ACTIVE);
         }
     }
 
     /**
      * Show the filter menu.
      */
     private void showFilterMenu() {
         filterPopupDelegate.setFilterDelegate(filterDelegate);
         filterPopupDelegate.setFilterMenuWidth(getFilterMenuWidth());
         filterPopupDelegate.setFilterActionComplete(new Runnable() {
             public void run() {
                 setFilterIcon(Boolean.FALSE);
             }
         });
         filterPopupDelegate.initialize(this, leftJLabel.getX(), leftJLabel.getY() + leftJLabel.getHeight());
         filterPopupDelegate.show();
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JLabel leftJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel rightJLabel = new javax.swing.JLabel();
     private javax.swing.JTextField searchJTextField;
     // End of variables declaration//GEN-END:variables
 }
