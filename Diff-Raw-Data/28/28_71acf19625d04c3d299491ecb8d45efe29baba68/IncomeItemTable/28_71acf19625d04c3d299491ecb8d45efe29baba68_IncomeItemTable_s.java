 package blacksmyth.personalfinancier.control.gui;
 
 import java.awt.Component;
 import java.util.Observer;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import blacksmyth.general.SwingUtilities;
 
 import blacksmyth.personalfinancier.model.CashFlowFrequency;
 import blacksmyth.personalfinancier.model.PreferencesModel;
 
 public class IncomeItemTable extends JTable {
   private static final long serialVersionUID = 1L;
   
   public enum TABLE_COLUMNS {
     Category, Description, Amount, Frequency, 
     Daily, Weekly,Fortnightly, Monthly,
     Quarterly, Yearly, Account
   }
   
   private static final int CELL_BUFFER = 15;
   
   public IncomeItemTable(IncomeItemTableController model) {
     super(model);
     this.setRowSelectionAllowed(true);
     setupColumns();
 
     this.getIncomeItemController().fireTableDataChanged();
   }
   
   private void setupColumns() {
     this.tableHeader.setReorderingAllowed(false);
     this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
     
     setupCategoryCol();
     setupAmountCol(TABLE_COLUMNS.Amount);
     setupFrequencyCol();
     setupAmountCol(TABLE_COLUMNS.Daily);
     setupAmountCol(TABLE_COLUMNS.Weekly);
     setupAmountCol(TABLE_COLUMNS.Fortnightly);
     setupAmountCol(TABLE_COLUMNS.Monthly);
     setupAmountCol(TABLE_COLUMNS.Quarterly);
     setupAmountCol(TABLE_COLUMNS.Yearly);
     setupAccountCol();
   }
   
   private void setupAccountCol() {
     getColFromEnum(TABLE_COLUMNS.Account).setCellRenderer(
         WidgetFactory.createTableCellRenderer(JTextField.CENTER)    
     );
     
     DefaultCellEditor editor = WidgetFactory.createBudgetAccountCellEditor();    
 
     getColFromEnum(TABLE_COLUMNS.Account).setCellEditor(editor);
     
     this.getIncomeItemController().addModelObserver(
         (Observer) editor.getComponent()
     );
     
     SwingUtilities.lockColumnWidth(
         getColFromEnum(TABLE_COLUMNS.Account),
         (int) editor.getComponent().getPreferredSize().getWidth()
     );
   }
 
   private void setupAmountCol(TABLE_COLUMNS thisColumn) {
     SwingUtilities.lockColumnWidth(
         getColFromEnum(thisColumn),
         SwingUtilities.getTextWidth(
             WidgetFactory.DECIMAL_FORMAT_PATTERN
         ) + CELL_BUFFER
     );
 
     getColFromEnum(thisColumn).setCellRenderer(
         WidgetFactory.createAmountCellRenderer()    
     );
     getColFromEnum(thisColumn).setCellEditor(
         WidgetFactory.createAmountCellEditor()    
     );
   }
 
   private void setupCategoryCol() {
     getColFromEnum(TABLE_COLUMNS.Category).setCellRenderer(
         WidgetFactory.createTableCellRenderer(JTextField.CENTER)    
     );
 
    DefaultCellEditor editor = WidgetFactory.createBudgetCategoryCellEditor();    
     getColFromEnum(TABLE_COLUMNS.Category).setCellEditor(editor);
 
     SwingUtilities.lockColumnWidth(
         getColFromEnum(TABLE_COLUMNS.Category),
         (int) editor.getComponent().getPreferredSize().getWidth()
     );
   }
 
   private void setupFrequencyCol() {
     getColFromEnum(TABLE_COLUMNS.Frequency).setCellRenderer(
         WidgetFactory.createTableCellRenderer(JTextField.CENTER)    
     );
 
     DefaultCellEditor editor = WidgetFactory.createCashFlowFrequencyCellEditor();    
     getColFromEnum(TABLE_COLUMNS.Frequency).setCellEditor(editor);
 
     SwingUtilities.lockColumnWidth(
         getColFromEnum(TABLE_COLUMNS.Frequency),
         (int) editor.getComponent().getPreferredSize().getWidth()
     );
   }
   
   private TableColumn getColFromEnum(TABLE_COLUMNS thisEnum) {
     return this.getColumnModel().getColumn(thisEnum.ordinal());
   }
   
   public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
 
     Component cellRenderer = super.prepareRenderer(renderer, row, column);
     
     WidgetFactory.prepareTableCellRenderer(
       cellRenderer,
       row, 
       column, 
       this.getModel().isCellEditable(row, column)
     );
     
     // One final tweak specific to this table below.  
     // when the frequency selected matches the column name, render the 
     // foreground text of the cell slightly differently.
 
     if (this.getColumnName(column) == this.getFrequencyAt(row).toString()) {
       cellRenderer.setForeground(
           PreferencesModel.getInstance().getPreferredBudgetFrequencyCellColor()
       );
     }
    
      return cellRenderer;
   }
   
   private CashFlowFrequency getFrequencyAt(int row) {
     return (CashFlowFrequency) this.getModel().getValueAt(row, TABLE_COLUMNS.Frequency.ordinal());
   }
 
   public IncomeItemTableController getIncomeItemController() {
     return (IncomeItemTableController) getModel();
   }
 
   public void addBudgetItem() {
     this.getIncomeItemController().addIncomeItem();
   }
   
   public void removeBudgetItem() {
     int row = this.getSelectedRow();
     if (row >= 0) {
       this.getIncomeItemController().removeItem(row);
     }
   }
 }
 
 
