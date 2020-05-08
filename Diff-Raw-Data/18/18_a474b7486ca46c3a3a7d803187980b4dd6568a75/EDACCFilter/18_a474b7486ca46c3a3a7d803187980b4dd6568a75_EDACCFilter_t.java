 /*
  * Filter.java
  *
  * Created on 13.10.2010, 11:29:39
  */
 package edacc;
 
 import edacc.filter.ArgumentPanel;
 import edacc.filter.BooleanFilter;
 import edacc.filter.FilterInterface;
 import edacc.filter.NumberFilter;
 import edacc.filter.Parser;
 import edacc.filter.StringFilter;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.lang.Integer;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JTable;
 import javax.swing.RowFilter;
 import javax.swing.RowFilter.Entry;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 import org.jdesktop.application.Action;
 
 /**
  * This class represents a filter. It can be used with any AbstractTableModel.
  * Currently supported are: BooleanFilter, NumberFilter & StringFilter.
  * @author simon
  */
 public class EDACCFilter extends javax.swing.JDialog {
 
     private JTable table;
     private TableRowSorter<? extends TableModel> rowSorter;
     private RowFilter<Object, Object> rowFilter;
     private HashMap<Integer, FilterType> colFilter;
     private GridBagLayout argumentLayout;
     private GridBagConstraints gridBagConstraints;
     private Parser parser;
     private String expression;
     private LinkedList<ArgumentPanel> filterArguments;
     private boolean updateFilterTypes;
 
     /**
      * Creates new form EDACCFilter. If the table has no instance of TableRowSorter a IllegalArgumentException is thrown.
      * @param parent The parent for this dialog
      * @param modal
      * @param table the table to be used for this filter
      * @param autoUpdateFilterTypes if this is set to true, the filter will updated the classes of the columns on each setVisible(true)
      */
     public EDACCFilter(java.awt.Frame parent, boolean modal, JTable table, boolean autoUpdateFilterTypes) {
         super(parent, modal);
         initComponents();
         if (!(table.getRowSorter() instanceof TableRowSorter)) {
             throw new IllegalArgumentException("Expected TableRowSorter.");
         }
         this.table = table;
        filterArguments = new LinkedList<ArgumentPanel>();
         rowSorter = (TableRowSorter<? extends TableModel>) table.getRowSorter();
         final RowFilter oldRowFilter = rowSorter.getRowFilter();
         rowFilter = new RowFilter<Object, Object>() {
 
             @Override
             public boolean include(Entry<? extends Object, ? extends Object> entry) {
                 return (oldRowFilter == null || oldRowFilter.include(entry)) && EDACCFilter.this.include(entry);
             }
         };
         rowSorter.setRowFilter(rowFilter);
         colFilter = new HashMap<Integer, FilterType>();
         argumentLayout = new GridBagLayout();
         pnlArguments.setLayout(argumentLayout);
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.weightx = 1000;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new Insets(6, 6, 6, 6);
         gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
         parser = new Parser();
         this.updateFilterTypes = autoUpdateFilterTypes;
         expression = "";
        
     }
 
     /**
      * Gets the value of the underlying table, i.e. it calls the getValue method of the table model.
      * @param row
      * @param col
      * @return the value object
      */
     public Object getValueAt(int row, int col) {
         return table.getModel().getValueAt(row, col);
     }
 
     /**
      * Returns true iff the given entry matches the filter parameters.
      * @param entry
      * @return boolean
      */
     public synchronized boolean include(Entry<? extends Object, ? extends Object> entry) {
         HashMap<Integer, Boolean> arguments = new HashMap<Integer, Boolean>();
         for (ArgumentPanel panel : filterArguments) {
             arguments.put(panel.getArgNum(), panel.getFilterInterface().include(getValueAt((Integer) entry.getIdentifier(), panel.getColumn())));
         }
         try {
             return parser.eval(expression, arguments);
         } catch (Exception ex) {
             return false;
         }
     }
 
     /**
      * Removes all filters and clears the expression.
      */
     public void clearFilters() {
         pnlArguments.removeAll();
         filterArguments.clear();
         txtExpression.setText("");
         expression = "";
     }
 
     /**
      * Returns true iff there is the chance that some rows might not be visible in the table.
      * @return boolean
      */
     public synchronized boolean hasFiltersApplied() {
         try {
             return parser != null && !parser.eval(expression, new HashMap<Integer, Boolean>());
         } catch (Exception ex) {
             return true;
         }
     }
 
     /**
      * Updates the classes of the columns to generate the filters. Can be overwritten but should then call updateFilterTypes(Class<?>[], String[]).
      */
     public void updateFilterTypes() {
         // create to arrays with the column classes and the column names
         Class<?>[] classes = new Class<?>[table.getModel().getColumnCount()];
         String[] columnNames = new String[table.getModel().getColumnCount()];
 
         for (int i = 0; i < table.getModel().getColumnCount(); i++) {
             classes[i] = table.getModel().getColumnClass(i);
             columnNames[i] = table.getModel().getColumnName(i);
         }
         // update the filter types with that data
         updateFilterTypes(classes, columnNames);
     }
 
     protected void updateFilterTypes(Class<?>[] classes, String[] columnNames) {
         for (int i = 0; i < classes.length; i++) {
             if (colFilter.containsKey(i)) {
                 if (colFilter.get(i).clazz != classes[i]) {
                     // the generated filter for this column has a wrong class, i.e. the class of the column has been changed.
                     // remove this filter (will be added later with the right class)
                     colFilter.remove(i);
                     // and remove all panels in the argument referring this column
                     for (int k = pnlArguments.getComponentCount() - 1; k >= 0; k--) {
                         if (pnlArguments.getComponent(k) instanceof ArgumentPanel) {
                             ArgumentPanel panel = (ArgumentPanel) pnlArguments.getComponent(k);
                             if (panel.getColumn() == i) {
                                 pnlArguments.remove(k);
                             }
                         }
                     }
                 } else {
                     continue;
                 }
             }
             // add the filter class
             colFilter.put(i, new FilterType(i, columnNames[i], classes[i]));
         }
         // update the filter combo box
         comboFilterTypes.removeAllItems();
         for (FilterType f : colFilter.values()) {
             comboFilterTypes.addItem(f);
         }
     }
 
     @Override
     public void setVisible(boolean visible) {
         if (updateFilterTypes) {
             updateFilterTypes();
         }
         super.setVisible(visible);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         pnlArguments = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         btnAdd = new javax.swing.JButton();
         comboFilterTypes = new javax.swing.JComboBox();
         jPanel2 = new javax.swing.JPanel();
         txtExpression = new javax.swing.JTextField();
         btnApply = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         btnDismiss = new javax.swing.JButton();
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(EDACCFilter.class);
         setTitle(resourceMap.getString("Form.title")); // NOI18N
         setMinimumSize(new java.awt.Dimension(500, 300));
         setName("Form"); // NOI18N
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentHidden(java.awt.event.ComponentEvent evt) {
                 formComponentHidden(evt);
             }
         });
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
         jPanel1.setName("jPanel1"); // NOI18N
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
         jScrollPane1.setPreferredSize(new java.awt.Dimension(2, 200));
 
         pnlArguments.setName("pnlArguments"); // NOI18N
         pnlArguments.setLayout(new java.awt.GridBagLayout());
         jScrollPane1.setViewportView(pnlArguments);
 
         jPanel4.setName("jPanel4"); // NOI18N
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getActionMap(EDACCFilter.class, this);
         btnAdd.setAction(actionMap.get("btnAdd")); // NOI18N
         btnAdd.setText(resourceMap.getString("btnAdd.text")); // NOI18N
         btnAdd.setName("btnAdd"); // NOI18N
 
         comboFilterTypes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         comboFilterTypes.setName("comboFilterTypes"); // NOI18N
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addComponent(comboFilterTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnAdd)
                 .addContainerGap(355, Short.MAX_VALUE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(comboFilterTypes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnAdd))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
         jPanel2.setName("jPanel2"); // NOI18N
 
         txtExpression.setText(resourceMap.getString("txtExpression.text")); // NOI18N
         txtExpression.setName("txtExpression"); // NOI18N
         txtExpression.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtExpressionKeyReleased(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(txtExpression, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(txtExpression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
 
         btnApply.setAction(actionMap.get("btnApply")); // NOI18N
         btnApply.setText(resourceMap.getString("btnApply.text")); // NOI18N
         btnApply.setName("btnApply"); // NOI18N
         btnApply.setPreferredSize(new java.awt.Dimension(67, 23));
 
         jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
         jLabel1.setName("jLabel1"); // NOI18N
 
         btnDismiss.setAction(actionMap.get("btnDismiss")); // NOI18N
         btnDismiss.setText(resourceMap.getString("btnDismiss.text")); // NOI18N
         btnDismiss.setName("btnDismiss"); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 117, Short.MAX_VALUE)
                 .addComponent(btnDismiss)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnApply, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
             .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(btnApply, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnDismiss))
                     .addComponent(jLabel1))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
         btnDismiss();
     }//GEN-LAST:event_formComponentHidden
 
     private void txtExpressionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtExpressionKeyReleased
         if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
             evt.consume();
             btnApply();
         }
     }//GEN-LAST:event_txtExpressionKeyReleased
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAdd;
     private javax.swing.JButton btnApply;
     private javax.swing.JButton btnDismiss;
     private javax.swing.JComboBox comboFilterTypes;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JPanel pnlArguments;
     private javax.swing.JTextField txtExpression;
     // End of variables declaration//GEN-END:variables
 
     class FilterType {
         int column;
         String name;
         Class<?> clazz;
 
         public FilterType(int column, String name, Class<?> clazz) {
             this.column = column;
             this.name = name;
             this.clazz = clazz;
         }
 
         @Override
         public String toString() {
             return name;
         }
     }
 
     @Action
     public void btnAdd() {
         // find out, what is the highest argument number
         int argNum = 0;
         for (int i = 0; i < pnlArguments.getComponentCount(); i++) {
             if (pnlArguments.getComponent(i) instanceof ArgumentPanel) {
                 int tmp = ((ArgumentPanel) pnlArguments.getComponent(i)).getArgNum();
                 if (argNum < tmp) {
                     argNum = tmp;
                 }
             }
         }
         // increment, i.e. this number is free to use
         argNum++;
         if (comboFilterTypes.getSelectedItem() instanceof FilterType) {
             // find out which filter to use, construct it and add it to the panel.
             FilterType filterType = (FilterType) comboFilterTypes.getSelectedItem();
             FilterInterface filter = null;
             if (filterType.clazz == Integer.class || filterType.clazz == Float.class || filterType.clazz == Double.class) {
                 filter = new NumberFilter(filterType.name);
             } else if (filterType.clazz == Boolean.class) {
                 filter = new BooleanFilter(filterType.name);
             } else if (filterType.clazz == String.class || Object.class.isAssignableFrom(filterType.clazz)) {
                 filter = new StringFilter(filterType.name);
             } 
             if (filter == null) {
                 return;
             }
             // add key listener for VK_ENTER event on textfields
             if (filter instanceof Component) {
                 ((Component) filter).addKeyListener(new KeyAdapter() {
 
                     @Override
                     public void keyReleased(KeyEvent e) {
                         if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                             e.consume();
                             btnApply();
                         }
                     }
                     
                 });
             }
             
             pnlArguments.add(new ArgumentPanel(this, filter, argNum, filterType.column));
             // replace the expression by `$argNum` iff the current expression will always validate to true
             // add `&& $argNum` to the expression iff there is currently a valid expression and this expression will not always validate to true
             try {
                 if (parser.eval(txtExpression.getText(), new HashMap<Integer, Boolean>())) {
                     // expression is valid and will always evaluate to true (no arguments needed)
                     txtExpression.setText("$" + argNum);
                 }
             } catch (Exception ex) {
                 try {
                     parser.eval(txtExpression.getText(), new HashMap<Integer, Boolean>(), true);
                     // expression is valid but arguments are needed
                     txtExpression.setText(txtExpression.getText() + " && $" + argNum);
                 } catch (Exception ex1) {
                 }
 
             }
             setGridBagConstraints();
         }
     }
 
     private void setGridBagConstraints() {
         // update the grid bag constraints
         // set weight to maximum
         gridBagConstraints.gridy = 0;
         gridBagConstraints.weighty = 1;
         for (int i = 0; i < pnlArguments.getComponentCount(); i++) {
             gridBagConstraints.gridy++;
             gridBagConstraints.weighty *= 1000;
             argumentLayout.setConstraints(pnlArguments.getComponent(i), gridBagConstraints);
         }
         pnlArguments.repaint();
         pnlArguments.revalidate();
     }
 
     @Action
     public synchronized void btnApply() {
         // save the expression and filter arguments to be used in the include part
         expression = txtExpression.getText();
         filterArguments.clear();
         for (int i = 0; i < pnlArguments.getComponentCount(); i++) {
             if (pnlArguments.getComponent(i) instanceof ArgumentPanel) {
                 filterArguments.add((ArgumentPanel) pnlArguments.getComponent(i));
                 ((ArgumentPanel) pnlArguments.getComponent(i)).getFilterInterface().apply();
             }
         }
         setVisible(false);
     }
 
     /**
      * Removes the panel from this view. It also replaces all occurrences of `$argNum` by `true`
      * @param pnl the panel to be removed
      */
     public void remove(ArgumentPanel pnl) {
         // first remove the panel
         pnlArguments.remove(pnl);
         // then replace all occurrences of `$argNum` by true
         String arg = String.valueOf(pnl.getArgNum());
         String expr = txtExpression.getText();
         Matcher matcher = Pattern.compile("\\$" + arg + "([^0-9]|\n)").matcher(expr + "\n");
         while (matcher.find()) {
             expr = expr.substring(0, matcher.start()) + "true" + expr.substring(matcher.start()+arg.length()+1, expr.length());
             matcher = Pattern.compile("\\$" + arg + "([^0-9]|\n)").matcher(expr + "\n");
         }
         txtExpression.setText(expr);
         // finally update grid bag constraints
         setGridBagConstraints();
     }
 
     @Action
     public synchronized void btnDismiss() {
         // revert every operation, i.e. load the saved values
         txtExpression.setText(expression);
         pnlArguments.removeAll();
         for (ArgumentPanel panel : filterArguments) {
             panel.getFilterInterface().undo();
             pnlArguments.add(panel);
         }
         setGridBagConstraints();
         setVisible(false);
     }
 }
