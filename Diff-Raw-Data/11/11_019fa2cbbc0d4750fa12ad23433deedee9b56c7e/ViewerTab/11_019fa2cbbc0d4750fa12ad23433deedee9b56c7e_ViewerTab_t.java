 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 import javax.swing.RowFilter;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableRowSorter;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.ViewerActivityExportAction;
 import de.aidger.controller.actions.ViewerActivityReportAction;
 import de.aidger.controller.actions.ViewerAddAction;
 import de.aidger.controller.actions.ViewerAssistantAction;
 import de.aidger.controller.actions.ViewerContractAction;
 import de.aidger.controller.actions.ViewerCourseAction;
 import de.aidger.controller.actions.ViewerDeleteAction;
 import de.aidger.controller.actions.ViewerDetailViewAction;
 import de.aidger.controller.actions.ViewerEditAction;
 import de.aidger.model.Runtime;
 import de.aidger.view.UI;
 import de.aidger.view.models.ActivityTableModel;
 import de.aidger.view.models.AssistantTableModel;
 import de.aidger.view.models.ContractTableModel;
 import de.aidger.view.models.CostUnitTableModel;
 import de.aidger.view.models.CourseTableModel;
 import de.aidger.view.models.EmploymentTableModel;
 import de.aidger.view.models.FinancialCategoryTableModel;
 import de.aidger.view.models.HourlyWageTableModel;
 import de.aidger.view.models.TableModel;
 import de.aidger.view.utils.BooleanTableRenderer;
 import de.aidger.view.utils.DateTableRenderer;
 import de.aidger.view.utils.MultiLineTableRenderer;
 import javax.swing.RowSorter.SortKey;
 import javax.swing.SortOrder;
 import javax.swing.Timer;
 
 /**
  * A tab which will be used to display the data.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class ViewerTab extends Tab {
 
     private class SearchRowFilter extends RowFilter<TableModel, Integer> {
 
             private final Pattern pat;
 
             public SearchRowFilter(String search) {
                 super();
 
                 // Sanitize the search string before using it
                 search = search.replaceAll("([\\\\\\*\\+\\?\\(\\)\\{\\}\\[\\]\\|\\^\\$])", "\\\\$1");
                 pat = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
             }
 
             @Override
             public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                 for (int i = entry.getValueCount() - 1; i >= 0; i--) {
                     Matcher m = pat
                         .matcher(entry.getStringValue(i));
                     if (m.find()) {
                         return true;
                     }
                 }
                 return false;
             }
         };
     /**
      * The type of the data that will be viewed.
      */
     public enum DataType {
         Course(_("Course")), Assistant(_("Assistant")), FinancialCategory(
                 _("Financial Category")), HourlyWage(_("Hourly Wage")), CostUnit(
                 _("Cost unit")), Employment(_("Employment")), Contract(
                 _("Contract")), Activity(_("Activity"));
 
         /**
          * The display name of an item.
          */
         private final String displayName;
 
         /**
          * Constructs a qualification item.
          * 
          * @param displayName
          *            the display name of the item
          */
         DataType(final String displayName) {
             this.displayName = displayName;
         }
 
         /**
          * Returns the display name.
          * 
          * @return the display name
          */
         public String getDisplayName() {
             return displayName;
         }
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
      * The type of the data.
      */
     private final DataType type;
 
     /**
      * The table model of this viewer tab.
      */
     private TableModel tableModel;
 
     /**
      * The row sorter of the table.
      */
     private final TableRowSorter<TableModel> sorter;
 
     /**
      * Timer to wait a second before searching.
      */
     private Timer timer;
 
     /**
      * Constructs the data viewer tab.
      * 
      * @param type
      *            the type of the data
      */
     @SuppressWarnings("unchecked")
     public ViewerTab(DataType type) {
         this.type = type;
         initComponents();
 
         itemCourse.setVisible(false);
         itemAssistant.setVisible(false);
         itemContract.setVisible(false);
         itemActivityReport.setVisible(false);
         itemActivityExport.setVisible(false);
 
         List<SortKey> sortKeys = new ArrayList<SortKey>();
 
         // use different table model for each data type
         switch (type) {
         case Course:
             tableModel = new CourseTableModel();
 
             sortKeys.add(new SortKey(1, SortOrder.ASCENDING));
             sortKeys.add(new SortKey(3, SortOrder.ASCENDING));
             sortKeys.add(new SortKey(4, SortOrder.ASCENDING));
             break;
         case Assistant:
             tableModel = new AssistantTableModel();
 
             toolBar.add(btnActivityReport);
             toolBar.add(separator9);
             itemActivityReport.setVisible(true);
 
             sortKeys.add(new SortKey(2, SortOrder.ASCENDING));
             sortKeys.add(new SortKey(1, SortOrder.ASCENDING));
             break;
         case FinancialCategory:
             tableModel = new FinancialCategoryTableModel();
             break;
         case HourlyWage:
             tableModel = new HourlyWageTableModel();
             break;
         case CostUnit:
             tableModel = new CostUnitTableModel();
             break;
         case Employment:
             tableModel = new EmploymentTableModel();
 
             toolBar.add(btnCourse);
             toolBar.add(separator6);
             toolBar.add(btnAssistant);
             toolBar.add(separator7);
             toolBar.add(btnContract);
             toolBar.add(separator8);
 
             itemCourse.setVisible(true);
             itemAssistant.setVisible(true);
             itemContract.setVisible(true);
 
             sortKeys.add(new SortKey(1, SortOrder.ASCENDING));
             sortKeys.add(new SortKey(2, SortOrder.ASCENDING));
             sortKeys.add(new SortKey(4, SortOrder.ASCENDING));
 
             break;
         case Contract:
             tableModel = new ContractTableModel();
 
             toolBar.add(btnAssistant);
             toolBar.add(separator7);
 
             itemAssistant.setVisible(true);
 
             break;
         case Activity:
             tableModel = new ActivityTableModel();
 
             toolBar.add(btnCourse);
             toolBar.add(separator6);
             toolBar.add(btnAssistant);
             toolBar.add(separator7);
             toolBar.add(btnActivityExport);
             toolBar.add(separator10);
 
             itemCourse.setVisible(true);
             itemAssistant.setVisible(true);
             itemActivityExport.setVisible(true);
 
            sortKeys.add(new SortKey(0, SortOrder.DESCENDING));
             sortKeys.add(new SortKey(1, SortOrder.ASCENDING));
             sortKeys.add(new SortKey(2, SortOrder.ASCENDING));
             sortKeys.add(new SortKey(4, SortOrder.ASCENDING));
 
             break;
         }
 
         table.setModel(tableModel);
 
         sorter = new TableRowSorter<TableModel>(tableModel);
        sorter.setSortsOnUpdates(true);
         table.setRowSorter(sorter);
        sorter.setSortKeys(sortKeys);
        sorter.sort();

 
         table.setDoubleBuffered(true);
         table.setFocusCycleRoot(true);
 
         table.addMouseListener(new PopupListener(popupMenu) {
             @Override
             public void mousePressed(MouseEvent me) {
                 if (table.getSelectedRowCount() == 1) {
                     super.mousePressed(me);
                 }
             }
 
             @Override
             public void mouseReleased(MouseEvent me) {
                 if (table.getSelectedRowCount() == 1) {
                     super.mouseReleased(me);
                 }
             }
         });
 
         // sort on first column by default
        //sorter.toggleSortOrder(0);
 
         // type specific cell rendering
 
         if (type == DataType.HourlyWage) {
             table.getColumnModel().getColumn(1).setCellRenderer(
                 new DateTableRenderer("MM.yyyy"));
         }
 
         if (type == DataType.FinancialCategory) {
             table.getColumnModel().getColumn(3).setCellRenderer(
                 new MultiLineTableRenderer());
             table.getColumnModel().getColumn(4).setCellRenderer(
                 new MultiLineTableRenderer());
         }
 
         if (type == DataType.Employment) {
             table.getColumnModel().getColumn(4).setCellRenderer(
                 new DateTableRenderer("MM.yyyy"));
         }
 
         if (type == DataType.Contract) {
             for (int i = 2; i < 6; ++i) {
                 table.getColumnModel().getColumn(i).setCellRenderer(
                     new DateTableRenderer("dd.MM.yyyy"));
             }
 
             table.getColumnModel().getColumn(7).setCellRenderer(
                 new BooleanTableRenderer());
         }
 
         if (type == DataType.Activity) {
             table.getColumnModel().getColumn(3).setCellRenderer(
                 new DateTableRenderer("dd.MM.yyyy"));
         }
 
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
             btnCourse.setAction(ActionRegistry.getInstance().get(
                 ViewerCourseAction.class.getName()));
             btnAssistant.setAction(ActionRegistry.getInstance().get(
                 ViewerAssistantAction.class.getName()));
             btnContract.setAction(ActionRegistry.getInstance().get(
                 ViewerContractAction.class.getName()));
             btnActivityReport.setAction(ActionRegistry.getInstance().get(
                 ViewerActivityReportAction.class.getName()));
             btnActivityExport.setAction(ActionRegistry.getInstance().get(
                 ViewerActivityExportAction.class.getName()));
 
             itemView.setAction(ActionRegistry.getInstance().get(
                 ViewerDetailViewAction.class.getName()));
             itemEdit.setAction(ActionRegistry.getInstance().get(
                 ViewerEditAction.class.getName()));
             itemDelete.setAction(ActionRegistry.getInstance().get(
                 ViewerDeleteAction.class.getName()));
             itemCourse.setAction(ActionRegistry.getInstance().get(
                 ViewerCourseAction.class.getName()));
             itemAssistant.setAction(ActionRegistry.getInstance().get(
                 ViewerAssistantAction.class.getName()));
             itemContract.setAction(ActionRegistry.getInstance().get(
                 ViewerContractAction.class.getName()));
             itemActivityReport.setAction(ActionRegistry.getInstance().get(
                 ViewerActivityReportAction.class.getName()));
             itemActivityExport.setAction(ActionRegistry.getInstance().get(
                 ViewerActivityExportAction.class.getName()));
 
             // shortcuts for table
             table.getInputMap().put(
                 KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "viewEntry");
             table.getInputMap().put(
                 KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removeEntry");
             table.getInputMap().put(
                 KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK),
                 "selectAllEntries");
 
             table.getActionMap().put(
                 "viewEntry",
                 ActionRegistry.getInstance().get(
                     ViewerDetailViewAction.class.getName()));
             table.getActionMap().put(
                 "removeEntry",
                 ActionRegistry.getInstance().get(
                     ViewerDeleteAction.class.getName()));
             table.getActionMap().put("selectAllEntries", new AbstractAction() {
                 public void actionPerformed(ActionEvent e) {
                     table.selectAll();
                 }
             });
 
             table.addMouseListener((MouseListener) ActionRegistry.getInstance()
                 .get(ViewerDetailViewAction.class.getName()));
 
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
 
         // set up the search field
         searchField.addFocusListener(new java.awt.event.FocusAdapter() {
             /*
              * (non-Javadoc)
              * 
              * @see
              * java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent
              * )
              */
             @Override
             public void focusGained(java.awt.event.FocusEvent evt) {
                 if (searchField.getText().equals(_("Search"))) {
                     searchField.setText("");
                 }
             }
 
             /*
              * (non-Javadoc)
              * 
              * @see
              * java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
              */
             @Override
             public void focusLost(java.awt.event.FocusEvent evt) {
                 if (searchField.getText().equals("")) {
                     searchField.setText(_("Search"));
                 }
             }
         });
 
         searchField.addKeyListener(new java.awt.event.KeyAdapter() {
             /*
              * (non-Javadoc)
              * 
              * @see
              * java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
              */
             @Override
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 String text = searchField.getText();
 
                 if (text.length() == 0) {
                     sorter.setRowFilter(null);
                 } else {
                     if (timer == null || !timer.isRunning()) {
                         AbstractAction action = new AbstractAction() {
                             public void actionPerformed(ActionEvent ae) {
                                 RowFilter<TableModel, Integer> filter = new SearchRowFilter(searchField.getText());
                                 sorter.setRowFilter(filter);
                             }
                         };
                         timer = new Timer(500, action);
                         timer.setRepeats(false);
                         timer.start();
                     } else {
                         timer.restart();
                     }
                 }
             }
         });
 
         clearSearch.setIcon(new ImageIcon(getClass().getResource(
             "/de/aidger/res/icons/broom.png")));
 
         clearSearch.addMouseListener(new java.awt.event.MouseAdapter() {
             @Override
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 searchField.setText(_("Search"));
                 sorter.setRowFilter(null);
                 table.requestFocus();
             }
         });
 
         // selection listener for table
         table.getSelectionModel().addListSelectionListener(
             new ListSelectionListener() {
                 @Override
                 public void valueChanged(ListSelectionEvent e) {
                     int rowCount = table.getSelectedRowCount();
 
                     String message = _("1 entity selected.");
 
                     if (rowCount > 1) {
                         message = MessageFormat.format(
                             _("{0} entities selected."),
                             new Object[] { rowCount });
                     }
 
                     UI.getInstance().setStatusMessage(message);
 
                     btnCourse.setEnabled(rowCount == 1);
                     btnAssistant.setEnabled(rowCount == 1);
                     btnContract.setEnabled(rowCount == 1);
                 }
             });
 
         btnCourse.setEnabled(false);
         btnAssistant.setEnabled(false);
         btnContract.setEnabled(false);
 
         btnCourse
             .setToolTipText(_("Show the course of the selected employment in detail."));
         btnAssistant
             .setToolTipText(_("Show the assistant of the selected employment in detail."));
         btnContract
             .setToolTipText(_("Show the contract of the selected employment in detail."));
     }
 
     /**
      * Get the name of the tab and constructor options if necessary.
      * 
      * @return A string representation of the class
      */
     @Override
     public String toString() {
         return getClass().getName() + "<" + DataType.class.getName() + "@"
                 + type;
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
             return _("Courses");
         case Assistant:
             return _("Assistants");
         case FinancialCategory:
             return _("Financial Categories");
         case HourlyWage:
             return _("Hourly Wages");
         case CostUnit:
             return _("Cost Units");
         case Employment:
             return _("Employment overview");
         case Contract:
             return _("Contracts");
         case Activity:
             return _("Activitiy overview");
         default:
             return _("Data");
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
      * Returns the type of the data that is shown.
      * 
      * @return the type of the shown data
      */
     public DataType getType() {
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
         itemCourse = new javax.swing.JMenuItem();
         itemAssistant = new javax.swing.JMenuItem();
         itemContract = new javax.swing.JMenuItem();
         itemActivityReport = new javax.swing.JMenuItem();
         itemActivityExport = new javax.swing.JMenuItem();
         btnCourse = new javax.swing.JButton();
         separator6 = new javax.swing.JToolBar.Separator();
         btnAssistant = new javax.swing.JButton();
         separator7 = new javax.swing.JToolBar.Separator();
         btnContract = new javax.swing.JButton();
         separator8 = new javax.swing.JToolBar.Separator();
         btnActivityReport = new javax.swing.JButton();
         separator9 = new javax.swing.JToolBar.Separator();
         btnActivityExport = new javax.swing.JButton();
         separator10 = new javax.swing.JToolBar.Separator();
         toolBar = new javax.swing.JToolBar();
         separator1 = new javax.swing.JToolBar.Separator();
         btnView = new javax.swing.JButton();
         separator2 = new javax.swing.JToolBar.Separator();
         btnEdit = new javax.swing.JButton();
         separator3 = new javax.swing.JToolBar.Separator();
         btnAdd = new javax.swing.JButton();
         separator4 = new javax.swing.JToolBar.Separator();
         btnDelete = new javax.swing.JButton();
         separator5 = new javax.swing.JToolBar.Separator();
         scrollPane = new javax.swing.JScrollPane();
         table = new javax.swing.JTable();
         searchField = new javax.swing.JTextField();
         clearSearch = new javax.swing.JLabel();
 
         itemView.setText(_("View"));
         popupMenu.add(itemView);
 
         itemEdit.setText(_("Edit"));
         popupMenu.add(itemEdit);
 
         itemDelete.setText(_("Delete"));
         popupMenu.add(itemDelete);
 
         itemCourse.setText(_("Course"));
         popupMenu.add(itemCourse);
 
         itemAssistant.setText(_("Assistant"));
         popupMenu.add(itemAssistant);
 
         itemContract.setText(_("Contract"));
         popupMenu.add(itemContract);
 
         itemActivityReport.setText(_("Activity Report"));
         popupMenu.add(itemActivityReport);
 
         itemActivityExport.setText(_("Export"));
         popupMenu.add(itemActivityExport);
 
         btnCourse.setText(_("Course"));
         btnCourse.setFocusable(false);
         btnCourse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnCourse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 
         btnAssistant.setText(_("Assistant"));
         btnAssistant.setFocusable(false);
         btnAssistant.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnAssistant.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 
         btnContract.setText(_("Contract"));
         btnContract.setFocusable(false);
         btnContract.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnContract.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 
         btnActivityReport.setText(_("Activity Report"));
         btnActivityReport.setFocusable(false);
         btnActivityReport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnActivityReport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 
         btnActivityExport.setText(_("Export"));
         btnActivityExport.setFocusable(false);
         btnActivityExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnActivityExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 
         setLayout(new java.awt.GridBagLayout());
 
         toolBar.setFloatable(false);
         toolBar.setRollover(true);
         toolBar.add(separator1);
 
         btnView.setText(_("View"));
         btnView.setFocusable(false);
         btnView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnView.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnView);
         toolBar.add(separator2);
 
         btnEdit.setText(_("Edit"));
         btnEdit.setFocusable(false);
         btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnEdit);
         toolBar.add(separator3);
 
         btnAdd.setText(_("Add"));
         btnAdd.setFocusable(false);
         btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnAdd);
         toolBar.add(separator4);
 
         btnDelete.setText(_("Delete"));
         btnDelete.setFocusable(false);
         btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(btnDelete);
         toolBar.add(separator5);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         add(toolBar, gridBagConstraints);
 
         scrollPane.setViewportView(table);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
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
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         add(clearSearch, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnActivityExport;
     private javax.swing.JButton btnActivityReport;
     private javax.swing.JButton btnAdd;
     private javax.swing.JButton btnAssistant;
     private javax.swing.JButton btnContract;
     private javax.swing.JButton btnCourse;
     private javax.swing.JButton btnDelete;
     private javax.swing.JButton btnEdit;
     private javax.swing.JButton btnView;
     private javax.swing.JLabel clearSearch;
     private javax.swing.JMenuItem itemActivityExport;
     private javax.swing.JMenuItem itemActivityReport;
     private javax.swing.JMenuItem itemAssistant;
     private javax.swing.JMenuItem itemContract;
     private javax.swing.JMenuItem itemCourse;
     private javax.swing.JMenuItem itemDelete;
     private javax.swing.JMenuItem itemEdit;
     private javax.swing.JMenuItem itemView;
     private javax.swing.JPopupMenu popupMenu;
     private javax.swing.JScrollPane scrollPane;
     private javax.swing.JTextField searchField;
     private javax.swing.JToolBar.Separator separator1;
     private javax.swing.JToolBar.Separator separator10;
     private javax.swing.JToolBar.Separator separator2;
     private javax.swing.JToolBar.Separator separator3;
     private javax.swing.JToolBar.Separator separator4;
     private javax.swing.JToolBar.Separator separator5;
     private javax.swing.JToolBar.Separator separator6;
     private javax.swing.JToolBar.Separator separator7;
     private javax.swing.JToolBar.Separator separator8;
     private javax.swing.JToolBar.Separator separator9;
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
