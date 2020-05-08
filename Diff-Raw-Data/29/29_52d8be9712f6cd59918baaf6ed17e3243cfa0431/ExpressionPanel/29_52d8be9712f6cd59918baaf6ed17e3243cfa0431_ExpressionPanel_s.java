 /**
  * Copyright (c) 2011. Physion Consulting LLC
  * All rights reserved.
  */
 package com.physion.ovation.gui.ebuilder;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JViewport;
 import javax.swing.Scrollable;
 import javax.swing.SwingConstants;
 
 import com.physion.ovation.gui.ebuilder.datamodel.RowData;
 import com.physion.ovation.gui.ebuilder.datamodel.RowDataEvent;
 import com.physion.ovation.gui.ebuilder.datamodel.RowDataListener;
 
 
 /**
  * This is the panel that contains a list of RowPanels.
  * Each RowPanel handles one row in the expression tree.
  * Each RowPanel displays/edits one RowData object.
  */
 public class ExpressionPanel
     extends JPanel
     implements RowDataListener, Scrollable {
 
 	/**
 	 * We never serialize this class, so this declaration is
 	 * just to stop the compiler warning.
 	 * You can suppress the warning instead if you want using
 	 * @SuppressWarnings("serial")
 	 */
 	private static final long serialVersionUID = 1L;
 
     /**
      * This is the "root" RowData for the tree.  I.e. this is
      * the very first row at the top of this ExpressionPanel
      * that the user uses to select the Class Under Qualification.
      */
 	private RowData rootRow;
 
 
     /**
      * Create an ExpressionPanel that will display and edit the passed
      * in expression tree.
      */
     ExpressionPanel(RowData rootRow) {
 
         super(new GridLayout(1,1));
         setRootRow(rootRow);
     }
 
 
     /**
      * Set the expression tree this ExpressionPanel will display.
      */
     public void setRootRow(RowData rootRow) {
 
         if (this.rootRow != null)
             this.rootRow.removeRowDataListener(this);
 
         this.rootRow = rootRow;
         rootRow.addRowDataListener(this);
         createRowPanels();
     }
 
 
     /**
      * Get the "root" RowData for the tree.  I.e. this is
      * the very first row at the top of this ExpressionPanel
      * that the user uses to select the Class Under Qualification.
      */
     public RowData getRootRow() {
         return(rootRow);
     }
 
 
     /**
      * Create all the RowPanels this ExpressionPanel contains.
      * Please note, we try to reuse any already existing RowPanels
      * that we can.
      */
     public void createRowPanels() {
 
         /**
          * Go through the list of RowData objects that we want
          * to display and see how many RowPanels this
          * ExpressionPanel already contains that are displaying
          * these RowData objects.
          * We will reuse as many of these RowPanels that we can
          * by creating an array "rowPanels" that contains as
          * many of the already existing RowPanels that we can
          * reuse.
          */
         ArrayList<RowPanel> rowPanels = new ArrayList<RowPanel>();
         for (RowData rowData : rootRow.getRows()) {
 
             /**
              * Get the RowPanel that is currently displaying the
              * rowData we want to display.
              */
             RowPanel rowPanel = getRowPanel(rowData);
             if (rowPanel == null) {
                 /**
                  * This ExpressionPanel does NOT already have
                  * a RowPanel displaying this rowData, so create
                  * a new RowPanel for this rowData.
                  */
                 rowPanel = new RowPanel(rowData);
             }
             else {
                 /**
                  * This ExpressionPanel already has a RowPanel
                  * that is displaying this rowData so we can
                  * just reuse it.
                  */
             }
 
             rowPanels.add(rowPanel);
         }
 
         /**
          * At this point, rowPanels contains the list of
          * all the RowPanels that we want to display.
          *
          * Remove all the RowPanels we are currently displaying
          * and insert the new list of RowPanels.  Please note,
          * in most cases, most of the RowPanels in the
          * rowPanels array will be the same RowPanels we were
          * already displaying.
          */
 
         removeAll();
 
         GridLayout layout = (GridLayout)getLayout();
         layout.setRows(rootRow.getDescendentCount()+1);
 
         //int zebraCount = 0;
         for (RowPanel rowPanel : rowPanels) {
 
             add(rowPanel);
 
             /**
              * Zebra stripe the rows.
              *
              * TODO: Delete the zebra stripe code if Physion
              * is sure they don't want to do something like this.
              */
             /*
             if ((zebraCount % 2) == 0)
                 rowPanel.setBackground(rowPanel.getBackground().darker());
             count++;
             */
         }
 
         /**
          * Make the scrollpane layout things NOW so that when
          * we later need to figure out where a particular row
          * or any of its child components are within the
          * scrollpane they will be laid out already.
          * This is so code that scrolls the scrollpane to make
          * a particular row or component visible can figure
          * out where a component is.
          */
         if (Util.getScrollPane(this) != null)
             Util.getScrollPane(this).validate();
     }
     
 
     /**
      * Get the RowPanel at the passed in index.
      * The root RowPanel, which is the one that is used
      * to select the Class Under Qualification, is at index 0.
      *
      * This method is not currently used, but just uncomment it
      * if you need it in the future.
      */
     /*
     private RowPanel getRowPanel(int index) {
         return((RowPanel)getComponent(index));
     }
     */
 
 
     /**
      * This method returns the RowPanel that is displaying/editing
      * the passed in rowData.  Returns null if no RowPanel in this
      * ExpressionPanel is handling the passed in rowData.
      */
     private RowPanel getRowPanel(RowData rowData) {
 
         for (Component component : getComponents()) {
             if (((RowPanel)component).getRowData() == rowData) {
                 return((RowPanel)component);
             }
         }
 
         return(null);
     }
 
 
     /**
      * Our rowDataChanged method keeps the index of the row that
      * was deleted, (assuming a row was deleted), using this value.
      */
     int deletedRowIndex = 0;
 
 
     /**
      * This is called when the expression tree we are displaying
      * is modified.
      *
      * If the change is one that changes the number of rows
      * in the GUI, recreate the RowPanels.
      */
     public void rowDataChanged(RowDataEvent event) {
 
         if ((event.getTiming() == RowDataEvent.TIMING_BEFORE) &&
             (event.getChangeType() == RowDataEvent.TYPE_CHILD_DELETE)) {
 
             /**
              * Save the index of the row that is being deleted.
              * We do this so that we can later set the focus to
              * the delete button in the RowPanel that is
              * currently below the row that is being deleted.
              *
              * (Seems to be the best place to set the focus
              * to after deleting the row that had the focus.)
              */
             deletedRowIndex = rootRow.getIndex(event.getChildRowData());
         }
 
        /**
         * Relayout the GUI because the number of rows have changed.
         */
         if ((event.getTiming() == RowDataEvent.TIMING_AFTER) &&
             ((event.getChangeType() == RowDataEvent.TYPE_CHILD_ADD) ||
              (event.getChangeType() == RowDataEvent.TYPE_CHILD_DELETE) ||
             (event.getChangeType() == RowDataEvent.TYPE_CUQ))) {
 
             createRowPanels();
         }
 
         /**
          * Set the focus appropriately.  For example, if we created
          * a new row, set the focus to the first component in that
          * row.
          */
         if ((event.getTiming() == RowDataEvent.TIMING_AFTER) &&
             ((event.getChangeType() == RowDataEvent.TYPE_CHILD_ADD) ||
              (event.getChangeType() == RowDataEvent.TYPE_CHILD_DELETE) ||
              (event.getChangeType() == RowDataEvent.TYPE_ATTRIBUTE) ||
              (event.getChangeType() == RowDataEvent.TYPE_CUQ))) {
 
             if (event.getChangeType() == RowDataEvent.TYPE_CHILD_ADD) {
                 /**
                  * Set the focus to the new row.  Rows are always
                  * added to the end of the parent row's list of children.
                  * So, simply set the focus to the last child row of the
                  * row that caused the event.  I.e. the row that
                  * had the child added to it, which is the "originalRowData"
                  * member data of the RowDataEvent.
                  */
                 RowData rowData = event.getOriginalRowData();
                 rowData = rowData.getChildRows().get(
                     rowData.getChildRows().size()-1);
 
                 /**
                  * Get the RowPanel that handles that rowData
                  * and set the focus to it.
                  */
                 RowPanel rowPanel = getRowPanel(rowData);
                 rowPanel.setFocusToFirstFocusableComponent();
             }
             else if (event.getChangeType() == RowDataEvent.TYPE_CHILD_DELETE) {
 
                 /**
                  * Set the focus to the row below the row that was just
                  * deleted.  (I.e. Set the focus to the row that got
                  * "moved up" to fill the deleted row's location.)
                  */
                 int indexOfRowToGetFocus = deletedRowIndex;
 
                 /**
                  * If the user deleted the bottom row, then we need
                  * to set the focus to the new bottom row.
                  */
                 if (indexOfRowToGetFocus > rootRow.getDescendentCount())
                     indexOfRowToGetFocus = rootRow.getDescendentCount();
 
                 /**
                  * At this point we know which row we want to set the
                  * focus to.  If we are setting the focus to the top
                  * row, which has no delete button, "-", set the focus
                  * to the create attribute row button, "+".
                  * Otherwise, set the focus to the delete button.
                  */
                 if (indexOfRowToGetFocus == 0) {
                     RowPanel rowPanel = getRowPanel(rootRow);
                     rowPanel.setFocusToCreateAttributeRowButton();
                 }
                 else {
                     RowPanel rowPanel = getRowPanel(rootRow.getChild(
                         indexOfRowToGetFocus));
                     rowPanel.setFocusToDeleteButton();
                 }
             }
             else if (event.getChangeType() == RowDataEvent.TYPE_CUQ) {
             
                 /**
                  * User changed the Class Under Qualification, so
                  * set the focus to the first component in the root
                  * row.
                  */
                 RowPanel rowPanel = getRowPanel(rootRow);
                 rowPanel.setFocusToFirstFocusableComponent();
             }
         }
     }
 
 
     /**
      * Tell any JScrollPane that contains us that it should NOT
      * stretch us to fill the scrollPane.
      */
     @Override
     public boolean getScrollableTracksViewportHeight() {
         return(false);
     }
 
 
     /**
      * This method is required by the Scrollable interface
      * we implement, and is called by the JScrollPane that
      * contains us to decide whether it should stretch/shrink
      * us to fit the viewport (i.e. the visible portion), or
      * whether it should display a horizontal scrollbar.
      *
      * Make the RowPanels stretch to fill the width of the
      * scrollPane that contains us if the scrollPane is
      * larger than this ExpressionPanel would like to be.
      *
      * But, if the scrollPane's viewport is smaller than 
      * this ExpressionPanel would like to be, tell the
      * scrollPane to scroll us horizontally.
      */
     @Override
     public boolean getScrollableTracksViewportWidth() {
 
         JScrollPane scrollPane = Util.getScrollPane(this);
         if (scrollPane == null) {
             /**
              * This should never happen, because no one will
              * call this method if we are not in a JScrollPane.
              */
             return(false);
         }
 
         JViewport viewport = scrollPane.getViewport();
 
         Rectangle viewRect = viewport.getViewRect();
         Dimension preferredSize = getPreferredSize();
         if (viewRect.width < preferredSize.width) {
             /**
              * The viewport is no wide enough for us, so
              * tell the scrollPane that it should NOT shrink
              * us to fit the viewport.  Instead, the scrollpane
              * will end up displaying a horizontal scrollbar so
              * the user can scroll us horizontally.
              */
             return(false);
         }
 
         /**
          * The viewport is larger than we need to be,
          * (or exactly the right size), so tell the
          * scrollPane to stretch us to fill the viewport.
          * (I.e. this ExpressionPanel, which is the "scrollable",
          * should "track" the viewport width.)
          */
         return(true);
     }
 
 
     /**
      * Tell the caller, (e.g. the JScrollPane that holds us),
      * how large we would like the JViewport to be.  Ideally,
      * the viewport is the same size this ExpressionPanel
      * would like to be so that no scrolling is needed.
      */
     @Override
     public Dimension getPreferredScrollableViewportSize() {
         return(getPreferredSize());
     }
 
 
     /**
      * Return the number of pixels the JScrollPane that contains us
      * should scroll when the user does a "block" scroll.  E.g. the
      * user clicks above or below the scroll "thumb" or uses the
      * page up/down keys.
      *
      * This should scroll an entire "page" of rows.  Note, I think
      * it is unlikely that a user will have that many rows worth
      * of data in a real use case, but we should implement the
      * behavior anyway.
      */
     @Override
     public int getScrollableBlockIncrement(Rectangle visibleRect,
                                            int orientation, int direction) {
 
         if (orientation == SwingConstants.HORIZONTAL) {
             /**
              * When scrolling horizontally, scroll sideways
              * by the width of the viewport.
              */
             return(visibleRect.width);
         }
         else {
             return(visibleRect.height);
         }
     }
 
 
     /**
      * Return the number of pixels the JScrollPane that contains us
      * should scroll when the user clicks on a scrolling arrow.
      * When scrolling vertically, this is the height of one row, or
      * a lesser amount necessary to get a row aligned with the top or
      * bottom of the scrollpane.
      *
      * When scrolling horizontally, there isn't really a good
      * answer for how much scrolling one "unit" is.
      * Note, we don't currently scroll horizontally.
      */
     @Override
     public int getScrollableUnitIncrement(Rectangle visibleRect,
                                           int orientation, int direction) {
 
         if (orientation == SwingConstants.HORIZONTAL) {
             /**
              * When scrolling horizontally, there isn't really a good
              * answer for how much scrolling one "unit" is.
              * So, just scroll by a percentage, (e.g. 25% =  0.25),
              * of the viewport width.
              */
             return((int)(visibleRect.width * 0.15));
         }
 
         /**
          * If we get here, the user is scrolling the window vertically.
          */
 
         /**
          * Get the first RowPanel to use to calculate
          * the height of a "unit", (i.e. one row), increment.
          */
         RowPanel rowPanel = getRowPanel(getRootRow());
 
         /**
          * If we don't have any rows yet, just return a number.
          * This number will never get used for scrolling because
          * the GUI will always have at least one row in it when it
          * is displayed.
          */
         if (rowPanel == null)
             return(10);
 
         Dimension size = rowPanel.getPreferredSize();
 
         if (direction > 0) {
             /**
              * When scrolling down, i.e. clicking the down arrow which
              * moves the scrollpane contents up, we want to make the
              * bottommost visible row align with the bottom of the
              * scrollpane.  If the bottommost visible row is already
              * aligned with the bottom of the scrollpane then that
              * means the number we return will be the height of one row.
              *
              * If the bottommost row is NOT aligned with the bottom
              * of the scrollpane, return the number of pixels that
              * WILL make it aligned with the bottom of the scrollpane.
              */
 
             int bottomPixel = visibleRect.height + visibleRect.y;
             int leftOver = bottomPixel % size.height;
 
             if (leftOver == 0) {
                 /**
                  * The bottommost row is aligned with the bottom of the
                  * scrollpane, so return the height of one row as the
                  * amount to scroll one unit.
                  */
                 return(size.height);
             }
             else {
                 return(size.height - leftOver);
             }
         }
         else {  // direction < 0
             /**
              * When scrolling up, i.e. clicking the up arrow which
              * moves the scrollpane contents down, we want to make the
              * topmost visible row align with the top of the
              * scrollpane.  If the topmost visible row is already
              * aligned with the top of the scrollpane then that
              * means the number we return will be the height of one row.
              *
              * If the topmost row is NOT aligned with the top
              * of the scrollpane, return the number of pixels that
              * WILL make it aligned with the top of the scrollpane.
              */
 
             int topPixel = visibleRect.y;
             int leftOver = topPixel % size.height;
 
             if (leftOver == 0) {
                 /**
                  * The topmost row is aligned with the top of the
                  * scrollpane, so return the height of one row as the
                  * amount to scroll one unit.
                  */
                 return(size.height);
             }
             else {
                 return(leftOver);
             }
         }
     }
 
 
     /**
      * This is just for testing/demo purposes.
      */
     public void print() {
         System.out.println("\nCurrent Value:\n"+rootRow.toString(false, ""));
         System.out.println("\nDebug Version:\n"+rootRow.toString());
     }
 }
