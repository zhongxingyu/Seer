 package project.gui;
 
 import project.Controller;
 import project.gui.util.DollarAmountFormatter;
 import project.gui.util.FieldInputVerifier;
 import project.model.Bank;
 import project.model.InvalidInputException;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.math.BigDecimal;
 
 /*
  * TODO consider only accessing Bank through Controller
  */
 public final class AccountantFrame extends AbstractUserWindow {
     private final JTable statisticTable;
 
     public AccountantFrame(Controller controller) {
         super(controller);
         statisticTable = new JTable();
         initComponents();
         updateStatisticTable();
     }
 
     private void initComponents() {
         setLayout(new BorderLayout());
         add(new JScrollPane(statisticTable), BorderLayout.CENTER);
 
         JPanel loanCapPanel = new JPanel(new GridLayout(1, 2));
         loanCapPanel.add(new JLabel("Loan cap (blank for no cap)"));
         JFormattedTextField loanCapField = new JFormattedTextField(new DollarAmountFormatter.Factory(true), Bank.getInstance().getLoanCap());
         loanCapField.setInputVerifier(new FieldInputVerifier(this) {
             @Override
             protected void setField(BigDecimal value) throws InvalidInputException {
                 Bank.getInstance().setLoanCap(value);
             }
         });
         loanCapPanel.add(loanCapField);
         add(loanCapPanel, BorderLayout.SOUTH);
     }
 
     public void updateStatisticTable() {
         statisticTable.setModel(new DefaultTableModel(
                 controller.updateAccountantTableView(),
                 new String[] { "Statistic", "Value" }) {
             @Override
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return false;
             }
         });
        statisticTable.revalidate();
         pack();
     }
 }
