 package net.premereur.mvp.example.swing.productmgt;
 
 import java.util.ArrayList;
 
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
 import net.premereur.mvp.example.domain.model.Product;
 
 /**
  * Shows a selectable list of Products. Not used yet.
  * 
  * @author gpremer
  * 
  */
 // NOCS MAGIC 400
 public class ProductSelectionPanel extends JPanel {
 
     private static final long serialVersionUID = 1L;
 
     private List<ProductSelection> productSelections = new ArrayList<ProductSelection>();
     private ProductModel productModel;
     private JTable productList;
     private JScrollPane jScrollPane1;
 
     public ProductSelectionPanel() {
         initComponents();
         initModel();
     }
 
     private void initComponents() {
         jScrollPane1 = new JScrollPane();
         productList = new JTable();
         jScrollPane1.setViewportView(productList);
 
         GroupLayout layout = new GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 446,
                 Short.MAX_VALUE));
         layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 320,
                 Short.MAX_VALUE));
     }
 
     private void initModel() {
         productModel = new ProductModel();
         productList.setModel(productModel);
         productList.getColumnModel().getColumn(0).setPreferredWidth(10);
         productList.getColumnModel().getColumn(1).setPreferredWidth(60);
     }
 
     public final void setProducts(final List<Product> products) {
         productSelections.clear();
         for (Product product : products) {
             productSelections.add(new ProductSelection(false, product));
         }
         productModel.fireTableDataChanged();
     }
 
     private static class ProductSelection {
         public ProductSelection(final boolean selected, final Product product) {
             this.selected = selected;
             this.product = product;
         }
 
         private Boolean selected = false;
         private Product product;
 
     }
 
     private static final String[] COLUMN_NAMES = {" ", "Name"};
 
     private class ProductModel extends AbstractTableModel {
 
         private static final long serialVersionUID = 1L;
 
         @Override
         public int getColumnCount() {
             return 2;
         }
 
         @Override
         public int getRowCount() {
             return productSelections.size();
         }
 
         @Override
         public String getColumnName(final int column) {
             return COLUMN_NAMES[column];
         }
 
         @Override
         public Object getValueAt(final int rowIndex, final int columnIndex) {
             switch (columnIndex) {
             case 0:
                 return productSelections.get(rowIndex).selected;
             case 1:
             default:
                 return productSelections.get(rowIndex).product.getName();
             }
         }
 
         @Override
         public boolean isCellEditable(final int rowIndex, final int columnIndex) {
             return true;
         }
 
         @Override
         public Class<?> getColumnClass(final int columnIndex) {
             switch (columnIndex) {
             case 0:
                 return Boolean.class;
             case 1:
             default:
                 return String.class;
             }
         }
 
         @Override
         public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
             if (columnIndex == 0) {
                 productSelections.get(rowIndex).selected = (Boolean) value;
                 fireTableCellUpdated(rowIndex, columnIndex);
             }
         }
     }
 
 }
