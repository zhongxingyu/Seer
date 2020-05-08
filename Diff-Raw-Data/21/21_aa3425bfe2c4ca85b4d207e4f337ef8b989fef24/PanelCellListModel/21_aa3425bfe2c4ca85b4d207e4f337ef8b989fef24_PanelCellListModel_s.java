 /**
  * Created On: 16-Dec-06 1:18:16 AM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel;
 
 import java.text.MessageFormat;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel;
 import com.thinkparity.ophelia.browser.util.localization.MainCellL18n;
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class PanelCellListModel {
     
     /** A session key pattern for the list's selected index. */
     private static final String SK_LIST_SELECTED_INDEX_PATTERN;
     
     static {
         SK_LIST_SELECTED_INDEX_PATTERN = "PanelCellListModel#List.getSelectedIndex({0}:{1})";
     }
     
     /** The tab panel */
     private final DefaultTabPanel tabPanel;
     
     /** The list key */
     private final String listKey;
     
     /** The list model <code>DefaultListModel</code>. */
     private final DefaultListModel listModel;
     
     /** The list manager. */
     private final PanelCellListManager listManager;
     
     /** The selection. */
     private int selectedIndex;
     
     /**
      * Create a PanelCellListModel.
      * 
      * Handles the model of visible panel cells, including
      * selection index and next/previous buttons, for a list
      * of Cells.
      * 
      * @param tabPanel
      *            A <code>DefaultTabPanel</code> invoker.
      * @param listKey
      *            A list key <code>String</code>.         
      * @param localization
      *            A <code>MainCellL18n</code>.          
      * @param visibleRows
      *            The number of visible rows.
      * @param firstJLabel
      *            The first <code>JLabel</code>.
      * @param previousJLabel
      *            The previous <code>JLabel</code>.           
      * @param countJLabel
      *            The count <code>JLabel</code>.                             
      * @param nextJLabel
      *            The next <code>JLabel</code>.                              
      * @param lastJLabel
      *            The last <code>JLabel</code>.                                                  
      */
     public PanelCellListModel(
         final DefaultTabPanel tabPanel,
         final String listKey,
         final MainCellL18n localization,
         final int visibleRows,
         final javax.swing.JLabel firstJLabel,
         final javax.swing.JLabel previousJLabel,
         final javax.swing.JLabel countJLabel,
         final javax.swing.JLabel nextJLabel,
         final javax.swing.JLabel lastJLabel) {
         super();
         this.tabPanel = tabPanel;
         this.listKey = listKey;
         listModel = new DefaultListModel();
         listManager = new PanelCellListManager(this, localization,
                 visibleRows, firstJLabel, previousJLabel, countJLabel,
                 nextJLabel, lastJLabel, Boolean.TRUE);    
         selectedIndex = -1;
         installDataListener();
     }
     
     public void initialize(final List<? extends Cell> cells) {
        final int initialSelectedIndex;
         if (isSavedSelectedIndex()) {
             initialSelectedIndex = getSavedSelectedIndex();
         } else {
             initialSelectedIndex = 0;
         }
         listManager.initialize(cells);
         setSelectedIndex(initialSelectedIndex);
     }
     
     public Boolean isSelectionEmpty() {
         return (selectedIndex < 0);
     }
     
     public DefaultListModel getListModel() {
         return listModel;
     }
     
     public int getIndexOf(final Cell cell) {
         return listModel.indexOf(cell);
     }
     
     public Cell getSelectedCell() {
         if (isSelectionEmpty()) {
             return null;
         } else {
             return (Cell)listModel.get(selectedIndex);
         }
     }
 
     public int getSelectedIndex() {
         return selectedIndex;
     }
 
     public void setSelectedCell(final Cell cell) {
         setSelectedIndex(listModel.indexOf(cell));
     }
 
     private void setSelectedIndex(final int selectedIndex) {
         final int oldSelectedIndex = this.selectedIndex;
         this.selectedIndex = selectedIndex;
         if (oldSelectedIndex != selectedIndex && !isSelectionEmpty()) {
             tabPanel.panelCellSelectionChanged(getSelectedCell());
             saveSelectedIndex(selectedIndex);
         }
     }
 
     private void installDataListener() {
         listModel.addListDataListener(new ListDataListener() {
             /**
              * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
              */
             public void contentsChanged(final ListDataEvent e) {
                 setSelectedIndex(0);
             }
             /**
              * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
              */
             public void intervalAdded(final ListDataEvent e) {
                 setSelectedIndex(0);
             }
             /**
              * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
              */
             public void intervalRemoved(final ListDataEvent e) {
                 setSelectedIndex(-1);
             } 
         });
     }
     
     private Boolean isSavedSelectedIndex() {
         return (null != getSavedSelectedIndex());
     }
     
     private Integer getSavedSelectedIndex() {
         final Integer selectedIndex =
             (Integer) tabPanel.getAttribute(MessageFormat.format(
                     SK_LIST_SELECTED_INDEX_PATTERN, tabPanel.getId(), listKey));
         return selectedIndex;
     }
     
     private void saveSelectedIndex(final int selectedIndex) {
         tabPanel.setAttribute(MessageFormat.format(
                 SK_LIST_SELECTED_INDEX_PATTERN, tabPanel.getId(), listKey),
                 Integer.valueOf(selectedIndex));
     }
 }
