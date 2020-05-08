 package jDistsim.ui.panel.tab;
 
 import jDistsim.ui.panel.listener.LogTabListener;
 import jDistsim.ui.renderer.ValueTableCellHeaderRenderer;
 import jDistsim.ui.renderer.ValueTableCellRenderer;
 import jDistsim.ui.skins.ScrollBarUI;
 import jDistsim.utils.ui.ListenerablePanel;
 import jDistsim.utils.ui.SwingUtil;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumn;
 import java.awt.*;
 import java.util.Vector;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 28.2.13
  * Time: 10:05
  */
 public class EntitiesTabPanel extends ListenerablePanel<LogTabListener> {
 
    private int minimalRow = 7;
     private JTable entitiesInfoTable;
     private JScrollPane scrollPane;
 
     public EntitiesTabPanel(JTable entitiesInfoTable) {
         this.entitiesInfoTable = entitiesInfoTable;
         initializeUI();
     }
 
     private void initializeUI() {
         setLayout(new BorderLayout());
         setBorder(new EmptyBorder(5, 5, 5, 5));
 
         add(new EntityTablePanel(), BorderLayout.CENTER);
         renderTable();
     }
 
     public void renderTable() {
         scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         JTableHeader tableHeader = entitiesInfoTable.getTableHeader();
         tableHeader.setReorderingAllowed(false);
         tableHeader.setResizingAllowed(true);
         tableHeader.setDefaultRenderer(new ValueTableCellHeaderRenderer());
 
         if (entitiesInfoTable.getRowCount() < minimalRow) {
             for (int i = 0; i < minimalRow; i++) {
                 Vector<String> row = new Vector<>();
                 for (int index = 0; index < entitiesInfoTable.getColumnCount(); index++) {
                     row.addElement(new String());
                 }
                 ((DefaultTableModel) entitiesInfoTable.getModel()).addRow(row);
             }
             scrollPane.getVerticalScrollBar().setVisible(false);
             scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
         }
 
         for (int index = 0; index < entitiesInfoTable.getColumnCount(); index++) {
             TableColumn tableColumn = entitiesInfoTable.getColumnModel().getColumn(index);
             tableColumn.setCellRenderer(new ValueTableCellRenderer());
         }
 
         if (entitiesInfoTable.getColumnCount() < 7) return;
         SwingUtil.setPrefColumnWidth(entitiesInfoTable, 0, 55); //Name
         SwingUtil.setPrefColumnWidth(entitiesInfoTable, 1, 70); //Originator
         SwingUtil.setPrefColumnWidth(entitiesInfoTable, 2, 70); //Owner
         SwingUtil.setPrefColumnWidth(entitiesInfoTable, 3, 65); //Distributed
         SwingUtil.setPrefColumnWidth(entitiesInfoTable, 4, 65); //Per interval
         SwingUtil.setPrefColumnWidth(entitiesInfoTable, 5, 68); //First creation
         SwingUtil.setPrefColumnWidth(entitiesInfoTable, 6, 65); //Max arrivals
         SwingUtil.setPrefColumnWidth(entitiesInfoTable, 7, 90); //Between arrivals
 
     }
 
     private class EntityTablePanel extends JComponent {
 
         public EntityTablePanel() {
             setLayout(new BorderLayout());
             entitiesInfoTable.setModel(new DefaultTableModel());
             entitiesInfoTable.setEnabled(false);
             entitiesInfoTable.setFocusable(false);
             entitiesInfoTable.setRowSelectionAllowed(false);
             entitiesInfoTable.setGridColor(new Color(156, 156, 156));
             entitiesInfoTable.setOpaque(false);
             entitiesInfoTable.setShowGrid(true);
 
             scrollPane = new JScrollPane(entitiesInfoTable);
             scrollPane.setBackground(Color.white);
             scrollPane.getViewport().setBackground(Color.white);
             scrollPane.getVerticalScrollBar().setUI(new ScrollBarUI());
 
             add(scrollPane, BorderLayout.CENTER);
         }
     }
 }
