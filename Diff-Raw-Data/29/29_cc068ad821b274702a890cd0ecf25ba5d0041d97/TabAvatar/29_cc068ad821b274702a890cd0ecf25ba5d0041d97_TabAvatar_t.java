 /*
  * Created on September 1, 2006, 11:32 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab;
 
 import java.awt.Component;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 import javax.swing.TransferHandler;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.event.MouseInputListener;
 
 import com.thinkparity.codebase.swing.SwingUtil;
 
 import com.thinkparity.ophelia.browser.application.browser.component.MenuFactory;
 import com.thinkparity.ophelia.browser.application.browser.component.PopupItemFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.Resizer;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.ContentProvider;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCellRenderer;
 import com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.ophelia.browser.platform.util.State;
 
 /**
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public abstract class TabAvatar<T extends TabModel> extends Avatar {
 
     /** A menu item factory. */
     protected final PopupItemFactory menuItemFactory;
 
     /** The avatar's accompanying <code>TabModel</code>.*/
     protected final T model;
 
     /** The avatar's id. */
     private final AvatarId id;
 
     /** Variables used to modify behavior of selection. */
     private Integer selectedIndex = -1;
 
     private Boolean selectingLastIndex = Boolean.FALSE;
 
     /** Create TabAvatar */
     protected TabAvatar(final AvatarId id, final T model) {
         // HACK This null check of an avatar id was done to allow the tab
         // plugin extension framework to create an avatar.  It needs to be
         // refactored in order to work "correctly".
         super(null == id ? "" : id.toString());
         this.id = id;
         this.menuItemFactory = PopupItemFactory.getInstance();
         this.model = model;
         setResizeEdges(Resizer.FormLocation.MIDDLE);
         initComponents();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getId()
      */
     @Override
     public final AvatarId getId() {
         return id;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getState()
      */
     @Override
     public final State getState() {
         return null;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#reload()
      */
     @Override
     public void reload() {
         model.reload();
         if (tabJList.isSelectionEmpty()) {
             setSelectedIndex(0);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setContentProvider(com.thinkparity.ophelia.browser.application.browser.display.provider.ContentProvider)
      */
     @Override
     public void setContentProvider(final ContentProvider contentProvider) {
         model.setContentProvider(contentProvider);
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setInput(java.lang.Object)
      */
     @Override
     public void setInput(final Object input) {
         model.setInput(input);
         super.setInput(input);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setState(com.thinkparity.ophelia.browser.platform.util.State)
      */
     @Override
     public final void setState(final State state) {}
 
     /**
      * Obtain the cell at a given index.
      * 
      * @param index
      *            An index <code>Integer</code>.
      * @return A <code>TabCell</code>.
      */
     protected final TabCell getCell(final Integer index) {
         return (TabCell) model.getListModel().elementAt(index);
     }
 
     /**
      * Obtain the selected tab cell.
      * 
      * @return A <code>TabCell</code>.
      */
     protected TabCell getSelectedCell() {
         return (TabCell) tabJList.getSelectedValue();
     }
 
     /**
      * Obtain the selected cell index.
      * 
      * @return A list index.
      */
     protected int getSelectedIndex() {
         return tabJList.getSelectedIndex();
     }
 
     /**
      * Install a mouse over tracker which tracks the mouse and records the index
      * above which it is positioned. It also updates the underlying cell's
      * "mouseOver" property.
      * 
      */
     protected final void installMouseOverTracker() {
         final MouseInputListener mouseOverListener = new MouseInputAdapter() {
             private int mouseOverIndex = -1;
             @Override
             public void mouseEntered(final MouseEvent e) {
                 mouseMoved(e);
             }
             @Override
             public void mouseExited(final MouseEvent e) {
                 if (-1 != mouseOverIndex) {
                     updateCellMouseOver(mouseOverIndex, Boolean.FALSE);
                     mouseOverIndex = -1;
                 }
             }
             @Override
             public void mouseMoved(MouseEvent e) {
                 final int mouseOverIndex = getMouseOverIndex(e);
                 if (mouseOverIndex != this.mouseOverIndex) {
                     if (-1 != this.mouseOverIndex) {
                         updateCellMouseOver(this.mouseOverIndex, Boolean.FALSE);
                     }
                     if (-1 != mouseOverIndex) {
                         updateCellMouseOver(mouseOverIndex, Boolean.TRUE);
                     }
                     this.mouseOverIndex = mouseOverIndex;
                 }
             }
         };
         tabJList.addMouseListener(mouseOverListener);
         tabJList.addMouseMotionListener(mouseOverListener);
     }
         
     /**
      * Set the header label transfer handler.
      * 
      * @param newHandler
      *            A <code>TransferHandler</code>.
      */
     protected void setHeaderTransferHandler(final TransferHandler newHandler) {
         headerJLabel.setTransferHandler(newHandler);
     }                                     
 
     /**
      * Set the list transfer handler.
      * 
      * @param newHandler
      *            A <code>TransferHandler</code>.
      */
     protected void setListTransferHandler(final TransferHandler newHandler) {
         tabJList.setTransferHandler(newHandler);
     }
 
     /**
      * Set the selected tab cell.
      * 
      * @param tabCell
      *            A <code>TabCell</code>.
      * @return Success or failure.
      */
     protected Boolean setSelectedCell(final TabCell tabCell) {
         if (tabCell != getSelectedCell()) {
             // Set selectedIndex to -1 so the ListSelectionListener behaves correctly
             selectedIndex = -1;
             tabJList.setSelectedValue(tabCell, true);
             
             if (getSelectedCell() != tabCell) {
                 return Boolean.FALSE;
             }
         }
         return Boolean.TRUE;
     }
 
     /**
      * Select an entry in the JList.
      * 
      * @param index
      *              The JList index to select.
      */
     protected void setSelectedIndex(final Integer index) {
         if (index != getSelectedIndex()) {
             // Set selectedIndex to -1 so the ListSelectionListener behaves correctly        
             selectedIndex = -1;
             tabJList.setSelectedIndex(index);
         }
     }
 
     /**
      * Trigger a double click event on the tab.
      *
      */
     protected void triggerDoubleClick() {}
 
     /**
      * Trigger a popup for the tab avatar.
      *
      */
     protected void triggerPopup(final Component invoker, final MouseEvent e) {}
 
     /**
      * Get the index of the cell the event is over.
      * 
      * @param e
      *          Mouse event.
      * @return  index of the cell the mouse is over, or -1.
      */
     private Integer getMouseOverIndex(java.awt.event.MouseEvent e) {
         Integer tabCellIndex = tabJList.locationToIndex(e.getPoint());
         
         // Handle the case that the mouse is below the last entry
         if ((tabCellIndex != -1) && (tabCellIndex == tabJList.getModel().getSize() - 1)) {
             final Rectangle tabCellBounds = tabJList.getCellBounds(tabCellIndex, tabCellIndex);
             if (!SwingUtil.regionContains(tabCellBounds, e.getPoint())) {
                 tabCellIndex = -1;
             }
         }
         
         return tabCellIndex;
     }
 
     private void headerJLabelMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_headerJLabelMousePressed
     }//GEN-LAST:event_headerJLabelMousePressed
 
     private void headerJLabelMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_headerJLabelMouseReleased
         if (e.isPopupTrigger()) {
             triggerPopup(tabJList, e);
         }
     }//GEN-LAST:event_headerJLabelMouseReleased
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         setLayout(new java.awt.GridBagLayout());
 
         headerJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 headerJLabelMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 headerJLabelMouseReleased(evt);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipady = 15;
         gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
         add(headerJLabel, gridBagConstraints);
 
         tabJScrollPane.setBorder(null);
         tabJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         tabJList.setModel(model.getListModel());
         tabJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         tabJList.setCellRenderer(new TabCellRenderer());
         tabJList.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tabJListKeyPressed(evt);
             }
         });
         tabJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 tabJListValueChanged(evt);
             }
         });
         tabJList.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tabJListMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 tabJListMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 tabJListMouseReleased(evt);
             }
         });
 
         tabJScrollPane.setViewportView(tabJList);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
         add(tabJScrollPane, gridBagConstraints);
 
     }// </editor-fold>//GEN-END:initComponents
     
     /**
      * Determine if the mouse event occurred on or below the cell in an x-y plane.
      * 
      * @return True if the mouse event occurred in the cell.
      */
     private boolean isMouseEventWithinCell(final java.awt.event.MouseEvent e) {
         final Integer tabCellIndex = tabJList.locationToIndex(e.getPoint());
         if (tabCellIndex == -1) {  // Will be -1 if the model is empty
             return false;
         } else {
             final Rectangle tabCellBounds = tabJList.getCellBounds(tabCellIndex, tabCellIndex);
             return SwingUtil.regionContains(tabCellBounds, e.getPoint());
         }
     }
 
     private void tabJListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tabJListKeyPressed
         if ((evt.getKeyCode()==KeyEvent.VK_DOWN) ||
             (evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN) ||
             (evt.getKeyCode()==KeyEvent.VK_END)) {
             if (tabJList.getSelectedIndex() != tabJList.getModel().getSize()-1) {
                 // Set selectedIndex to -1 so the ListSelectionListener behaves correctly
                 // ie. allow the selection to change.
                 selectedIndex = -1;
             }
         }
     }//GEN-LAST:event_tabJListKeyPressed
 
     private void tabJListMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_tabJListMouseClicked
         // Interesting fact aobut getClickCount() is that the count continues to 3, 4 and beyond if
         // the user keeps clicking with less than (say) 1/2 second delay between clicks.
         if (!MenuFactory.isPopupMenu()) {
             if (e.getButton()==MouseEvent.BUTTON1) {
                 final Boolean isInCell = isMouseEventWithinCell(e);
                 // Every click (regardless of getClickCount() triggers
                 // expand / collapse if the node supports it.
                 if (isInCell) { 
                     triggerExpand(getSelectedCell());
                 }
                 // A click count of 2, 4, 6, etc. triggers double click event
                 if (e.getClickCount() % 2 == 0) {
                     if (isInCell) {  
                         triggerDoubleClick(getSelectedCell());
                     } else {
                         triggerDoubleClick();
                     }
                 }
             }
         }
     }//GEN-LAST:event_tabJListMouseClicked
 
     private void tabJListMousePressed(java.awt.event.MouseEvent e) {                                             
         // If selectingLastIndex is true then the selection was interrupted. Don't
         // perform the selection if the user clicked below the last entry, otherwise
         // proceed with the selection.
         if (selectingLastIndex) {
             if (isMouseEventWithinCell(e)) { 
                 selectedIndex = tabJList.locationToIndex(e.getPoint());                        
                 tabJList.setSelectedIndex(selectedIndex);
             }
             selectingLastIndex = Boolean.FALSE; 
         }
     }
 
     private void tabJListMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_tabJListMouseReleased
         if (e.isPopupTrigger()) {
             // Desired behavior: if click on an entry in the list then trigger a popup for that cell.
             // If click in the blank area below the last entry in the list then trigger a popup that
             // is related to the tab rather than a cell in the tab.
             if (isMouseEventWithinCell(e)) {
                 setSelectedIndex(tabJList.locationToIndex(e.getPoint()));
                 triggerPopup(getSelectedCell(), e);
             } else {
                 triggerPopup(tabJList, e);             
             }
         }
     }//GEN-LAST:event_tabJListMouseReleased    
     
     private void tabJListValueChanged(javax.swing.event.ListSelectionEvent e) {//GEN-FIRST:event_tabJListValueChanged
         final Integer newSelectedIndex = tabJList.getSelectedIndex();
         final Integer lastIndex = tabJList.getModel().getSize() - 1;
         
         // The first time here, or if the current selection is the last item
         // in the list, or if the new selection is not the last item in
         // the list, then proceed as usual.
         if ((selectedIndex == -1) || (selectedIndex == lastIndex)
                 || (newSelectedIndex != lastIndex)) {
             selectedIndex = newSelectedIndex;
             selectingLastIndex = Boolean.FALSE;                    
         }
         // If the last item is being selected then hold off until we can determine
         // that the user has clicked on the cell and not below the cell.
         else {
             tabJList.setSelectedIndex(selectedIndex);
             selectingLastIndex = Boolean.TRUE;   
         }
     }//GEN-LAST:event_tabJListValueChanged  
     
     /**
      * Trigger a double click event on the tab cell.
      * 
      * @param tabCell
      *            A <code>TabCell</code>.
      */
     private void triggerDoubleClick(final TabCell tabCell) {
         model.triggerDoubleClick(tabCell);
         setSelectedCell(tabCell);
     }
 
     /**
      * Trigger an expand for the tab cell.
      * 
      * @param tabCell
      *            A <code>TabCell</code>.
      */
     private void triggerExpand(final TabCell tabCell) {
         model.triggerExpand(tabCell);
         setSelectedCell(tabCell);
     }
 
     /**
      * Trigger a popup menu for a tab cell.
      *
      * @param tabCell A <code>TabCell</code>.
      */
     private void triggerPopup(final TabCell tabCell, final MouseEvent e) {
         model.triggerPopup(tabCell, tabJList, e);
         setSelectedCell(tabCell);
     }
 
     /**
      * Update the cell that the mouse is over with the mouseOver flag and
      * cause the cell to redraw.
      * 
      * @param index
      *              Index of the cell.
      * @param mouseOver
      *              Mouse over flag.
      */
     private void updateCellMouseOver(final Integer index, final Boolean mouseOver) {
         saveSelection();
        if (index < tabJList.getModel().getSize()) {
            final TabCell cell = (TabCell) tabJList.getModel().getElementAt(index);
            cell.setMouseOver(mouseOver);
            model.getListModel().removeElementAt(index);
            model.getListModel().add(index, cell);
            restoreSelection();
        }
     }
 
     /**
      * Save the current selection cell.
      *
      */
     private void saveSelection() {
         putClientProperty(ClientProperty.SAVE_SELECTION, getSelectedIndex());
     }
 
     /**
      * Restore the saved selection.
      *
      */
     private void restoreSelection() {
         final Integer selection =
             (Integer) getClientProperty(ClientProperty.SAVE_SELECTION);
         tabJList.setSelectedIndex(selection);
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     protected final javax.swing.JLabel headerJLabel = new javax.swing.JLabel();
     protected final javax.swing.JList tabJList = new javax.swing.JList();
     protected final javax.swing.JScrollPane tabJScrollPane = new javax.swing.JScrollPane();
     // End of variables declaration//GEN-END:variables
 
     private enum ClientProperty { SAVE_SELECTION }
 
     public enum DataKey { SEARCH_EXPRESSION }
 }
