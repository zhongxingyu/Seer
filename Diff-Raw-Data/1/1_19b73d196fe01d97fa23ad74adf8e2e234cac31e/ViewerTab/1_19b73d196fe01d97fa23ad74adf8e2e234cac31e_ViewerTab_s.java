 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.table.TableColumn;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.ViewerActivitiesAction;
 import de.aidger.controller.actions.ViewerAddAction;
 import de.aidger.controller.actions.ViewerDeleteAction;
 import de.aidger.controller.actions.ViewerDetailViewAction;
 import de.aidger.controller.actions.ViewerEditAction;
 import de.aidger.model.Runtime;
 import de.aidger.view.UI;
 import de.aidger.view.models.AssistantTableModel;
 import de.aidger.view.models.CourseTableModel;
 import de.aidger.view.models.FinancialCategoryTableModel;
 import de.aidger.view.models.HourlyWageTableModel;
 import de.aidger.view.models.TableModel;
 import de.aidger.view.utils.MultiLineCellRenderer;
 
 /**
  * A tab which will be used to display the master data.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class ViewerTab extends Tab {
     /**
      * The type of the master data that will be viewed.
      */
     public enum MasterDataType {
         Course, Assistant, FinancialCategory, HourlyWage
     }
 
     /**
      * The header size of the shown table.
      */
     private final int[][] tableHeaderSize;
 
     /**
      * The hidden columns for restoring configuration state.
      */
     private final List<String> hiddenColumns = new ArrayList<String>();
 
     /**
      * The type of the master data.
      */
     private final MasterDataType type;
 
     /**
      * The table model of this viewer tab.
      */
     private TableModel tableModel;
 
     /**
      * Constructs the master data viewer tab.
      * 
      * @param type
      *            the type of the master data
      */
     @SuppressWarnings("unchecked")
     public ViewerTab(MasterDataType type) {
         this.type = type;
         initComponents();
 
         // use different table model for each master data type
         switch (type) {
         case Course:
             tableModel = new CourseTableModel();
             break;
         case Assistant:
             tableModel = new AssistantTableModel();
             break;
         case FinancialCategory:
             tableModel = new FinancialCategoryTableModel();
             btnActivities.setVisible(false);
             separator5.setVisible(false);
             break;
         case HourlyWage:
             tableModel = new HourlyWageTableModel();
             btnActivities.setVisible(false);
             separator5.setVisible(false);
             break;
         }
 
         table.setModel(tableModel);
 
         // multi line columns for financial category table
         if (type == MasterDataType.FinancialCategory) {
             table.getColumnModel().getColumn(2).setCellRenderer(
                 new MultiLineCellRenderer());
             table.getColumnModel().getColumn(3).setCellRenderer(
                 new MultiLineCellRenderer());
         }
 
         table.setComponentPopupMenu(popupMenu);
         table.setAutoCreateRowSorter(true);
         table.setDoubleBuffered(true);
         table.setFocusCycleRoot(true);
 
         // initializes the button and menu items actions
         try {
             btnView.setAction(ActionRegistry.getInstance().get(
                 ViewerDetailViewAction.class.getName()));
             btnEdit.setAction(ActionRegistry.getInstance().get(
                 ViewerEditAction.class.getName()));
             btnAdd.setAction(ActionRegistry.getInstance().get(
                 ViewerAddAction.class.getName()));
             btnDelete.setAction(ActionRegistry.getInstance().get(
                 ViewerDeleteAction.class.getName()));
             btnActivities.setAction(ActionRegistry.getInstance().get(
                 ViewerActivitiesAction.class.getName()));
 
             itemView.setAction(ActionRegistry.getInstance().get(
                 ViewerDetailViewAction.class.getName()));
             itemEdit.setAction(ActionRegistry.getInstance().get(
                 ViewerEditAction.class.getName()));
             itemDelete.setAction(ActionRegistry.getInstance().get(
                 ViewerDeleteAction.class.getName()));
             itemActivities.setAction(ActionRegistry.getInstance().get(
                 ViewerActivitiesAction.class.getName()));
 
         } catch (ActionNotFoundException e) {
             UI.displayError(e.getMessage());
         }
 
         tableHeaderSize = new int[table.getColumnCount()][3];
 
         // activate column filtering
         String[] hiddenColumns = Runtime.getInstance().getOptionArray(
             "hiddenColumns" + type);
 
         if (hiddenColumns == null) {
             switch (type) {
             case Course:
                 hiddenColumns = new String[] { "6", "7", "8", "9", "10" };
                 break;
             default:
                 hiddenColumns = new String[] {};
                 break;
             }
 
             Runtime.getInstance().setOptionArray("hiddenColumns" + type,
                 hiddenColumns);
         }
 
         for (int i = 0; i < hiddenColumns.length; ++i) {
             if (!hiddenColumns[i].isEmpty()) {
                 toggleColumnVisibility(Integer.valueOf(hiddenColumns[i]));
             }
         }
 
         JPopupMenu headerMenu = new JPopupMenu();
 
         Enumeration en = table.getTableHeader().getColumnModel().getColumns();
 
         while (en.hasMoreElements()) {
             TableColumn column = (TableColumn) en.nextElement();
 
             JCheckBoxMenuItem mi = new JCheckBoxMenuItem(new AbstractAction(
                 column.getHeaderValue().toString()) {
                 public void actionPerformed(ActionEvent evt) {
                     String cmd = evt.getActionCommand();
 
                     int index = table.getTableHeader().getColumnModel()
                         .getColumnIndex(cmd);
 
                     toggleColumnVisibility(index);
                 }
             });
 
             if (column.getPreferredWidth() != 0) {
                 mi.setSelected(true);
             } else {
                 mi.setSelected(false);
             }
 
             headerMenu.add(mi);
         }
 
         table.getTableHeader().addMouseListener(new PopupListener(headerMenu));
     }
 
     /**
      * Get the name of the tab and constructor options if necessary.
      * 
      * @return A string representation of the class
      */
     @Override
     public String toString() {
         return getClass().getName() + "<" + MasterDataType.class.getName()
                 + "@" + type;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getTabName()
      */
     @Override
     public String getTabName() {
         switch (type) {
         case Course:
             return _("Master Data Courses");
         case Assistant:
             return _("Master Data Assistants");
         case FinancialCategory:
             return _("Master Data Financial Categories");
         case HourlyWage:
             return _("Master Data Hourly Wages");
         default:
             return _("Master Data");
         }
     }
 
     /**
      * Toggles the visibility of the given column.
      * 
      * @param index
      *            the column index whose visibility will be toggled
      */
     private void toggleColumnVisibility(int index) {
         TableColumn column = table.getTableHeader().getColumnModel().getColumn(
             index);
 
         if (column.getPreferredWidth() != 0) {
             tableHeaderSize[index][0] = column.getPreferredWidth();
             tableHeaderSize[index][1] = column.getMinWidth();
             tableHeaderSize[index][2] = column.getMaxWidth();
 
             column.setMinWidth(0);
             column.setMaxWidth(0);
             column.setPreferredWidth(0);
 
             hiddenColumns.add(String.valueOf(index));
         } else {
             column.setMinWidth(tableHeaderSize[index][1]);
             column.setMaxWidth(tableHeaderSize[index][2]);
             column.setPreferredWidth(tableHeaderSize[index][0]);
 
             column.sizeWidthToFit();
 
             hiddenColumns.remove(String.valueOf(index));
         }
 
         Runtime.getInstance().setOptionArray("hiddenColumns" + type,
             hiddenColumns.toArray(new String[0]));
     }
 
     /**
      * Returns the table model.
      * 
      * @return the table model
      */
     public TableModel getTableModel() {
         return tableModel;
     }
 
     /**
      * Returns the shown table.
      * 
      * @return the table that is shown
      */
     public JTable getTable() {
         return table;
     }
 
     /**
      * Returns the type of the master data that is shown.
      * 
      * @return the type of the shown master data
      */
     public MasterDataType getType() {
         return type;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         popupMenu = new javax.swing.JPopupMenu();
         itemView = new javax.swing.JMenuItem();
         itemEdit = new javax.swing.JMenuItem();
         itemDelete = new javax.swing.JMenuItem();
         itemActivities = new javax.swing.JMenuItem();
         toolBar = new javax.swing.JToolBar();
         separator = new javax.swing.JToolBar.Separator();
         btnView = new javax.swing.JButton();
         separator1 = new javax.swing.JToolBar.Separator();
         btnEdit = new javax.swing.JButton();
         separator2 = new javax.swing.JToolBar.Separator();
         btnAdd = new javax.swing.JButton();
         separator3 = new javax.swing.JToolBar.Separator();
         btnDelete = new javax.swing.JButton();
         separator4 = new javax.swing.JToolBar.Separator();
         btnActivities = new javax.swing.JButton();
         separator5 = new javax.swing.JToolBar.Separator();
         scrollPane = new javax.swing.JScrollPane();
         table = new javax.swing.JTable();
         searchField = new javax.swing.JTextField();
 
         itemView.setText(_("View"));
         popupMenu.add(itemView);
 
         itemEdit.setText(_("Edit"));
         popupMenu.add(itemEdit);
 
         itemDelete.setText(_("Delete"));
         popupMenu.add(itemDelete);
 
         itemActivities.setText(_("Activities"));
         popupMenu.add(itemActivities);
 
         setLayout(new java.awt.GridBagLayout());
 
         toolBar.setFloatable(false);
         toolBar.setRollover(true);
         toolBar.add(separator);
 
         btnView.setText(_("View"));
         btnView.setFocusable(false);
         btnView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnView.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnView);
         toolBar.add(separator1);
 
         btnEdit.setText(_("Edit"));
         btnEdit.setFocusable(false);
         btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnEdit);
         toolBar.add(separator2);
 
         btnAdd.setText(_("Add"));
         btnAdd.setFocusable(false);
         btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnAdd);
         toolBar.add(separator3);
 
         btnDelete.setText(_("Delete"));
         btnDelete.setFocusable(false);
         btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnDelete);
         toolBar.add(separator4);
 
         btnActivities.setText(_("Activites"));
         btnActivities.setFocusable(false);
         btnActivities
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnActivities
             .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnActivities);
         toolBar.add(separator5);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         add(toolBar, gridBagConstraints);
 
         scrollPane.setViewportView(table);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(scrollPane, gridBagConstraints);
 
         searchField.setText(_("Search"));
         searchField.setPreferredSize(new java.awt.Dimension(200, 25));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(8, 0, 5, 0);
         add(searchField, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnActivities;
     private javax.swing.JButton btnAdd;
     private javax.swing.JButton btnDelete;
     private javax.swing.JButton btnEdit;
     private javax.swing.JButton btnView;
     private javax.swing.JMenuItem itemActivities;
     private javax.swing.JMenuItem itemDelete;
     private javax.swing.JMenuItem itemEdit;
     private javax.swing.JMenuItem itemView;
     private javax.swing.JPopupMenu popupMenu;
     private javax.swing.JScrollPane scrollPane;
     private javax.swing.JTextField searchField;
     private javax.swing.JToolBar.Separator separator;
     private javax.swing.JToolBar.Separator separator1;
     private javax.swing.JToolBar.Separator separator2;
     private javax.swing.JToolBar.Separator separator3;
     private javax.swing.JToolBar.Separator separator4;
     private javax.swing.JToolBar.Separator separator5;
     private javax.swing.JTable table;
     private javax.swing.JToolBar toolBar;
 
     // End of variables declaration//GEN-END:variables
 
     /**
      * A mouse listener that shows a given popup menu.
      * 
      * @author aidGer Team
      */
     class PopupListener extends MouseAdapter {
         JPopupMenu popupMenu;
 
         /**
          * Constructs the popup listener.
          * 
          * @param popup
          *            the popup menu that will be shown
          */
         public PopupListener(JPopupMenu popup) {
             this.popupMenu = popup;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
          */
         @Override
         public void mousePressed(MouseEvent me) {
             showPopup(me);
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
          */
         @Override
         public void mouseReleased(MouseEvent me) {
             showPopup(me);
         }
 
         /**
          * Shows the popup menu.
          * 
          * @param me
          *            the mouse event
          */
         private void showPopup(MouseEvent me) {
             if (me.isPopupTrigger()) {
                 popupMenu.show(me.getComponent(), me.getX(), me.getY());
             }
         }
     }
 }
