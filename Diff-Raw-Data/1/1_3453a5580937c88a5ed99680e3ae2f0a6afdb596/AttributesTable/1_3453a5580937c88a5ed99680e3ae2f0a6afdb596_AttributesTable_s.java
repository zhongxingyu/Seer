 package org.gladstoneinstitutes.customgraphicsui.internal.chart;
 
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 import javax.swing.JPanel;
 import javax.swing.JCheckBox;
 import javax.swing.TransferHandler;
 import javax.swing.AbstractCellEditor;
 import javax.swing.ListSelectionModel;
 import javax.swing.DropMode;
 import javax.swing.JComponent;
 
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 import java.awt.Component;
 import java.awt.GridBagLayout;
 
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.DataFlavor;
   
 import java.util.EventObject;
 
 import org.gladstoneinstitutes.customgraphicsui.internal.util.EasyGBC;
 
 class AttributesTable extends JTable {
 
   protected static TableModel newTableModel() {
     final DefaultTableModel model = new InternalTableModel();
     model.addColumn("");
     model.addColumn("Attribute");
     model.addColumn("Type");
 
     model.addRow(new Object[] { Boolean.FALSE, "a", "Float" });
     model.addRow(new Object[] { Boolean.FALSE, "b", "List of Strings" });
     model.addRow(new Object[] { Boolean.FALSE, "c", "Double" });
     return model;
   }
 
   public AttributesTable() {
     super(newTableModel());
     
     final TableColumn activeCol = super.getColumnModel().getColumn(0);
     activeCol.setCellRenderer(new BooleanCellHandler());
     activeCol.setCellEditor(new BooleanCellHandler());
     activeCol.setMaxWidth(35);
     activeCol.setMinWidth(35);
 
     super.setDragEnabled(true);
     super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     super.setDropMode(DropMode.INSERT_ROWS);
     super.setTransferHandler(new InternalTransferHandler());
   }
 
   static class InternalTableModel extends DefaultTableModel {
     public boolean isCellEditable(int row, int column) {
       switch (column) {
         case 0:
           return true;
         default:
           return false;
       }
     }
   }
 
   class BooleanCellHandler extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
     final JCheckBox checkBox = new JCheckBox();
 
     public BooleanCellHandler() {
       checkBox.setBackground(AttributesTable.super.getBackground());
     }
    
     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
       checkBox.setSelected((Boolean) value);
       return checkBox;
     }
 
     public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
       checkBox.setSelected((Boolean) value);
       return checkBox;
     }
 
     public Object getCellEditorValue() {
       return Boolean.valueOf(checkBox.isSelected());
     }
 
     public boolean shouldSelectCell(EventObject anEvent) {
       return false;
     }
   }
 
   class InternalTransferHandler extends TransferHandler {
     public int getSourceActions(JComponent c) {
       return TransferHandler.MOVE;
     }
 
     public Transferable createTransferable(JComponent c) {
       return new StringSelection(Integer.toString(AttributesTable.this.getSelectedRow()));
     }
 
     private Integer getRowIndexFromStringData(final TransferHandler.TransferSupport support) {
       final Transferable transferable = support.getTransferable();
       for (final DataFlavor flavor : support.getDataFlavors()) {
         if (!flavor.isFlavorTextType())
           continue;
         try {
           final Integer rowIndex = Integer.parseInt((String) transferable.getTransferData(flavor));
           if (0 <= rowIndex && rowIndex < AttributesTable.this.getRowCount())
             return rowIndex;
         } catch (Exception e) {}
       }
       return null;
     }
 
     public boolean canImport(TransferHandler.TransferSupport support) {
       if ((support.getDropAction() & TransferHandler.MOVE) == 0)
         return false;
       return getRowIndexFromStringData(support) != null;
     }
 
     public boolean importData(TransferHandler.TransferSupport support) {
       final Integer sourceRow = getRowIndexFromStringData(support);
       if (sourceRow == null)
         return false;
       int targetRow = ((JTable.DropLocation) support.getDropLocation()).getRow();
       if (targetRow < 0)
         targetRow = 0;
       if (sourceRow < targetRow)
         targetRow -= 1;
 
       final DefaultTableModel model = (DefaultTableModel) getModel();
       model.moveRow(sourceRow, sourceRow, targetRow);
       return true;
     }
   }
 }
