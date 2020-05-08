 package net.premereur.mvp.example.swing.categorymgt;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 
 import net.premereur.mvp.core.View;
 import net.premereur.mvp.example.domain.model.Category;
 
 /**
  * Shows a list of categories.
  * 
  * @author gpremer
  * 
  */
 public class CategoryList extends JPanel implements View {
     /**
      * Class version.
      */
     private static final long serialVersionUID = 1L;
     private TableModel dataModel;
     private JTable table;
     private List<Category> data = new ArrayList<Category>();
 
     public CategoryList() {
         init();
     }
 
     private void init() {
         dataModel = new TableModel();
         table = new JTable(dataModel);
         table.setPreferredScrollableViewportSize(new Dimension(150, 270)); // NOCS MAGIC 0
         table.setFillsViewportHeight(true);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane scrollPane = new JScrollPane(table);
         add(scrollPane);
         setOpaque(true); // Otherwise it doesn't show
     }
 
     public void bind(final List<Category> list) {
         this.data = new ArrayList<Category>(list);
         dataModel.fireTableDataChanged();
     }
 
     private class TableModel extends AbstractTableModel {
         private static final long serialVersionUID = 1L;
 
         @Override
         public String getColumnName(final int column) {
             return "Name";
         }
 
         @Override
         public int getColumnCount() {
             return 1;
         }
 
         @Override
         public int getRowCount() {
             return data.size();
         }
 
         @Override
         public Object getValueAt(final int rowIndex, final int columnIndex) {
             return data.get(rowIndex).getName();
         }
 
         @Override
         public boolean isCellEditable(final int rowIndex, final int columnIndex) {
             return false;
         }
     }
 
     public void refreshList() {
         dataModel.fireTableDataChanged();
     }
 
     public void addSelectionListener(final CategoryListPresenter presenter) {
         table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(final ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                     Category selectedCategory = getSelectedCategory();
                     if (selectedCategory != null) {
                         presenter.categorySelected(selectedCategory);
                     }
                 }
             }
 
         });
 
     }
 
     private Category getSelectedCategory() {
         int selectedRow = table.getSelectedRow();
         if (selectedRow == -1) {
             return null;
         }
         return data.get(selectedRow);
     }
 }
