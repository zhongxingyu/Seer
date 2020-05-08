 package gui;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import db.Column;
 import db.Database;
 
 public class ResultsTable extends JTable {
 
     private static final long serialVersionUID = -6101041763023896681L;
 
     private static final boolean DEBUG = false;
     private static final String[] EMPTY_COL_VALUES = new String[] { "", "" };
 
     private MyTableModel model;
 
     private int selectedColumn, selectedRow;
 
     protected String selectedValue;
 
     private boolean isAdding = false;
 
     public ResultsTable(MyTableModel model) {
         super(model);
         model.addColumn("Romana");
         model.addColumn("English");
         this.model = model;
         getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if (e.getValueIsAdjusting()) {
                     markSelectedValue();
                 }
             }
         });
         getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if (e.getValueIsAdjusting()) {
                     markSelectedValue();
                 }
             }
         });
         addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (DEBUG)
                     System.out.println("propertyChange: " + evt.getPropertyName());
                 if ("tableCellEditor".equals(evt.getPropertyName())) {
                     if (isEditing())
                         processEditingStarted();
                     else
                         processEditingStopped();
                 }
             }
         });
     }
 
     private void markSelectedValue() {
         selectedRow = getSelectedRow();
         selectedColumn = getSelectedColumn();
         if (selectedRow == -1 || selectedColumn == -1)
             return;
         if (DEBUG)
             System.out.println(selectedColumn + "." + selectedRow);
         selectedValue = getValueAt(selectedRow, selectedColumn).toString();
         if (DEBUG)
             System.out.println("value: " + selectedValue);
     }
 
     protected void processEditingStopped() {
         if (DEBUG)
             System.out.println("save: " + editingRow + "." + editingColumn);
         String[] values = EMPTY_COL_VALUES;
         if (isAdding) {
             Database.insert(values);
             isAdding = false;
             if (DEBUG)
                 System.out.println("insert");
         } else {
             values[editingColumn] = selectedValue;
             int unchangedColumn = Column.EN_INDEX - editingColumn;
             values[unchangedColumn] = model.getValueAt(editingRow, unchangedColumn);
         }
        String newValue = model.getValueAt(editingRow, editingColumn);
         if (!values[editingColumn].equals(newValue)) {
             Column column = new Column(editingColumn, newValue);
             Database.update(values, column);
             if (DEBUG)
                 System.out.println("update: " + selectedValue + "->" + newValue);
             selectedValue = newValue;
         }
     }
 
     protected void processEditingStarted() {
         // could not use because editingRow and editingColumn are -1
     }
 
     public void search(String ro, String en) {
         model.getDataVector().removeAllElements();
         HashMap<String, String> translations = Database.getTranslations(ro, en);
         Iterator<Entry<String, String>> iterator = translations.entrySet().iterator();
         while (iterator.hasNext()) {
             Entry<String, String> entry = iterator.next();
             Vector<String> values = new Vector<String>();
             values.add(entry.getKey());
             values.add(entry.getValue());
             model.getDataVector().add(values);
         }
         model.fireTableDataChanged();
     }
 
     public void addTranlation() {
         if (!isAdding) {
             this.isAdding = true;
             model.addRow(new String[2]);
             editCellAt(getRowCount() - 1, 0, null);
         }
         transferFocus();
     }
 
     public void deleteSelected() {
         int row = getSelectedRow();
        String text = "Delete current translation [" + model.getRo(row) + "]:[" + model.getEn(row) + "] ?";
         String title = "Delete confirmation";
         String[] options = new String[] { "Yes", "No" };
        int selectedOption = JOptionPane.showOptionDialog(this, text, title, JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
         if (selectedOption == 0) {
             Database.delete(model.getValues(row));
             model.removeRow(row);
         }
     }
 }
