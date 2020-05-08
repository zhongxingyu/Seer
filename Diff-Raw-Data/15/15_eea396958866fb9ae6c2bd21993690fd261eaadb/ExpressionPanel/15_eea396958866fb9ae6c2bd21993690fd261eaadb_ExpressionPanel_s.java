 package com.physion.ovation.gui.ebuilder;
 
 import java.awt.Component;
 import java.awt.GridLayout;
 import javax.swing.JPanel;
 import com.physion.ovation.gui.ebuilder.datamodel.RowData;
 import com.physion.ovation.gui.ebuilder.datamodel.RowDataEvent;
 import com.physion.ovation.gui.ebuilder.datamodel.RowDataListener;
 
 
 /**
  * This is the panel that contains a list of RowPanels.
  * Each RowPanel handles one row in the expression tree.
  * Each RowPanel displays/edits one RowData object.
  *
  * TODO: Remove the commented out "zebra striping" code if
  * Physion decides it does not want to do something like that.
  */
 public class ExpressionPanel
     extends JPanel
     implements RowDataListener {
 
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
 
 
     public RowData getRootRow() {
         return(rootRow);
     }
 
 
     /**
      * Create all the RowPanels this ExpressionPanel contains.
      *
      * TODO: Make this code more clever so when rows are added
      * or deleted we don't remove all the rows and recreate
      * all the rows, but instead we only change what we need to.
      */
     public void createRowPanels() {
 
         removeAll();
 
         GridLayout layout = (GridLayout)getLayout();
         layout.setRows(rootRow.getDescendentCount()+1);
 
         //int count = 0;
         for (RowData rowData : rootRow.getRows()) {
             RowPanel rowPanel = new RowPanel(rowData);
             add(rowPanel);
 
             /**
              * Zebra stripe the rows.
              */
             /*
             if ((count % 2) == 0)
                 rowPanel.setBackground(rowPanel.getBackground().darker());
             count++;
             */
         }
 
         /**
          * Make the scrollpane layout things again.
          */
         if ((getParent() != null) && (getParent().getParent() != null))
             getParent().getParent().validate();
     }
     
 
     /**
      * Get the RowPanel at the passed in index.
      * The root RowPanel, which is the one that is used
      * to select the Class Under Qualification, is at index 0.
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
      * This is called when the expression tree we are displaying
      * is modified.
      *
      * If the change is one that changes the number of rows
      * in the GUI, recreate the RowPanels.
      *
      * TODO: Handle focus traversal properly if we want to have
      * the GUI usable from the keyboard.
      */
     public void rowDataChanged(RowDataEvent event) {
 
         if ((event.getTiming() == RowDataEvent.TIMING_AFTER) &&
             ((event.getChangeType() == RowDataEvent.TYPE_CHILD_ADD) ||
              (event.getChangeType() == RowDataEvent.TYPE_CHILD_DELETE) ||
              (event.getChangeType() == RowDataEvent.TYPE_CUQ))) {
 
             createRowPanels();
 
             if (event.getChangeType() == RowDataEvent.TYPE_CHILD_ADD) {
                 /**
                  * Set the focus to the new row.  Rows are always
                  * added to the end of the parent row's list of children.
                  * So, simply set the focus to the last child row of the
                  * row that sent the event.
                  */
                 RowData rowData = event.getRowData();
                 rowData = rowData.getChildRows().get(
                     rowData.getChildRows().size()-1);
 
                 /**
                  * Get the RowPanel that handles that rowData
                  * and set the focus to it.
                  */
                 RowPanel rowPanel = getRowPanel(rowData);
                 rowPanel.setFocusToFirstFocusableComponent();
             }
             else if ((event.getChangeType() ==
                       RowDataEvent.TYPE_CHILD_DELETE) ||
                      (event.getChangeType() == RowDataEvent.TYPE_CUQ)) {
 
                 /**
                  * Set the focus to the first row.
                  */
                 RowPanel rowPanel = getRowPanel(rootRow);
                 rowPanel.setFocusToFirstFocusableComponent();
             }
         }
     }
 
 
     /**
      * This is just for debugging purposes.
      */
     public void print() {
         System.out.println("\nCurrent Value:\n"+rootRow.toString(false, ""));
         System.out.println("\nDebug Version:\n"+rootRow.toString());
     }
 }
