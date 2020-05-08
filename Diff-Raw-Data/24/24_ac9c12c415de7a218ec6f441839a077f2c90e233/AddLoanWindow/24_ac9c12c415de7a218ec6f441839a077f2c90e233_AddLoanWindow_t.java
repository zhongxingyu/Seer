 package view;
 
 import controller.WindowController;
 import domain.*;
 import tablemodel.AddLoanWindowCustomerTableModel;
 import tablemodel.AddLoanWindowLoanTableModel;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Date;
 import java.util.Observable;
 
 
 public class AddLoanWindow extends ListenerJFrame {
 //public class AddLoanWindow extends JFrame {
 
 	
     private JPanel contentPane;
     private JTextField textField_1;
    private JLabel dateLabel;
     private JTable loanTable;
     private JTextField txtKundeSuchen;
     private JTable customerTable;
     private TableRowSorter<? extends AbstractTableModel> sorter;
     private String kundeSuchenText = "Kunde suchen...";
     private JPanel panel_2;
     private Customer cust;
     private Library library;
     private JButton btnAnzeigen;
     private AddLoanWindowCustomerTableModel custTableModel;
     private JLabel errorLabel;
     private WindowController windowCtrl;
 
 
     public AddLoanWindow(final Library library, WindowController windowCtrl) {
         super(library, windowCtrl);
         this.library = library;
         this.windowCtrl = windowCtrl;
         initialize();
        this.setMinimumSize(new Dimension(800, 400));
 
     }
 
     public void setCustomer(Customer customer) {
 
         setTitle(customer.getSurname() + " " + customer.getName());
         txtKundeSuchen.setText(customer.getSurname() + " " + customer.getName());
         customerTable.setRowSelectionInterval(0, 0);
         update(new Observable(), new Object());
 
     }
 
     private void initialize() {
         setTitle("Ausleihe hinzufügen");
 
         setBounds(100, 100, 1000, 500);
         contentPane = new JPanel();
         contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
         setContentPane(contentPane);
 
         JPanel panel = new JPanel();
         GridBagConstraints gbc_panel = new GridBagConstraints();
         gbc_panel.fill = GridBagConstraints.HORIZONTAL;
         gbc_panel.anchor = GridBagConstraints.NORTH;
         gbc_panel.insets = new Insets(0, 0, 5, 5);
         gbc_panel.gridx = 0;
         gbc_panel.gridy = 0;
 
         panel.setBorder(new TitledBorder(null, "Kundenauswahl", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         panel.setLayout(new BorderLayout(0, 0));
 
         JPanel panel_4 = new JPanel();
         FlowLayout flowLayout = (FlowLayout) panel_4.getLayout();
         flowLayout.setAlignment(FlowLayout.LEFT);
         panel.add(panel_4, BorderLayout.NORTH);
 
         txtKundeSuchen = new JTextField();
         txtKundeSuchen.setText(kundeSuchenText);
         txtKundeSuchen.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent e) {
                 if (txtKundeSuchen.getText().equals(kundeSuchenText)) {
                     txtKundeSuchen.setText("");
                 }
             }
 
             public void focusLost(FocusEvent e) {
 
             }
         });
         
         panel_4.add(txtKundeSuchen);
         txtKundeSuchen.setColumns(20);
         txtKundeSuchen.getDocument().addDocumentListener(
                 new DocumentListener() {
                     public void changedUpdate(DocumentEvent e) {
                         sorter.setRowFilter(StartWindow.getTextFilter(txtKundeSuchen));
                     }
 
                     public void insertUpdate(DocumentEvent e) {
                         sorter.setRowFilter(StartWindow.getTextFilter(txtKundeSuchen));
                     }
 
                     public void removeUpdate(DocumentEvent e) {
                         sorter.setRowFilter(StartWindow.getTextFilter(txtKundeSuchen));
                     }
                 }
         );
 
         JPanel panel_5 = new JPanel();
         panel_5.setBorder(new TitledBorder(null, "Kunde ausw\u00E4hlen", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         panel.add(panel_5, BorderLayout.CENTER);
         panel_5.setLayout(new BorderLayout(0, 0));
 
         JScrollPane scrollPane_1 = new JScrollPane();
         panel_5.add(scrollPane_1);
 
         custTableModel = new AddLoanWindowCustomerTableModel(library);
         customerTable = new JTable();
         customerTable.setModel(custTableModel);
         customerTable.setFillsViewportHeight(true);
         scrollPane_1.add(customerTable);
         scrollPane_1.setViewportView(customerTable);
         sorter = new TableRowSorter<>(custTableModel);
         customerTable.setRowSorter(sorter);
         customerTable.getColumnModel().getColumn(0).setCellRenderer(new CustomerCellRenderer());
         customerTable.getColumnModel().getColumn(1).setCellRenderer(new CustomerCellRenderer());
         customerTable.getColumnModel().getColumn(2).setCellRenderer(new CustomerCellRenderer());
 
         customerTable.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 JTable target = (JTable) e.getSource();
                 if (e.getClickCount() == 1) {
                     int row = target.getSelectedRow();
                     if (row >= 0) {
                         updateRightSide(row);
 
 
                     }
                 }
             }
         });
 
         JPanel panel_6 = new JPanel();
         GridBagConstraints gbc_panel_6 = new GridBagConstraints();
         gbc_panel_6.insets = new Insets(0, 0, 5, 0);
         gbc_panel_6.fill = GridBagConstraints.HORIZONTAL;
         gbc_panel_6.anchor = GridBagConstraints.NORTH;
         gbc_panel_6.gridx = 1;
         gbc_panel_6.gridy = 0;
         contentPane.setLayout(new BorderLayout(0, 0));
 
         panel_6.setLayout(new BorderLayout(0, 0));
 
         JPanel panel_3 = new JPanel();
         panel_6.add(panel_3, BorderLayout.NORTH);
         panel_3.setLayout(new BorderLayout(0, 0));
 
         JPanel panel_1 = new JPanel();
         panel_3.add(panel_1, BorderLayout.SOUTH);
         panel_1.setBorder(new TitledBorder(null, "Neues Exemplar ausleihen", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         GridBagLayout gbl_panel_1 = new GridBagLayout();
         gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
         gbl_panel_1.rowHeights = new int[]{0, 25, 0, 0, 0, 0};
         gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
         gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
         panel_1.setLayout(gbl_panel_1);
 
         JLabel lblExemplarid = new JLabel("Exemplar-ID");
         GridBagConstraints gbc_lblExemplarid = new GridBagConstraints();
         gbc_lblExemplarid.insets = new Insets(0, 0, 5, 5);
         gbc_lblExemplarid.anchor = GridBagConstraints.WEST;
         gbc_lblExemplarid.gridx = 0;
         gbc_lblExemplarid.gridy = 0;
         panel_1.add(lblExemplarid, gbc_lblExemplarid);
 
         textField_1 = new JTextField();
         textField_1.getDocument().addDocumentListener(
                 new DocumentListener() {
                     public void changedUpdate(DocumentEvent e) {
                         errorLabel.setText("");
 
                     }
 
                     public void insertUpdate(DocumentEvent e) {
                         errorLabel.setText("");
                     }
 
                     public void removeUpdate(DocumentEvent e) {
                         errorLabel.setText("");
                     }
                 }
         );
         textField_1.setEnabled(false);
         GridBagConstraints gbc_textField_1 = new GridBagConstraints();
         gbc_textField_1.insets = new Insets(0, 0, 5, 5);
         gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
         gbc_textField_1.gridx = 1;
         gbc_textField_1.gridy = 0;
         panel_1.add(textField_1, gbc_textField_1);
         textField_1.setColumns(10);
 
         btnAnzeigen = new JButton("Hinzufügen");
         btnAnzeigen.setEnabled(false);
         btnAnzeigen.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 tryAddingLoanToCustomer();
 
 
             }
         });
 
         errorLabel = new JLabel("");
         errorLabel.setForeground(Color.RED);
         GridBagConstraints gbc_errorLabel = new GridBagConstraints();
         gbc_errorLabel.fill = GridBagConstraints.HORIZONTAL;
         gbc_errorLabel.insets = new Insets(0, 0, 5, 5);
         gbc_errorLabel.gridx = 1;
         gbc_errorLabel.gridy = 1;
         panel_1.add(errorLabel, gbc_errorLabel);
         GridBagConstraints gbc_btnAnzeigen = new GridBagConstraints();
         gbc_btnAnzeigen.fill = GridBagConstraints.BOTH;
         gbc_btnAnzeigen.insets = new Insets(0, 0, 0, 5);
         gbc_btnAnzeigen.gridx = 1;
         gbc_btnAnzeigen.gridy = 4;
         panel_1.add(btnAnzeigen, gbc_btnAnzeigen);
 
         JLabel lblZurckAm = new JLabel("Zurück am");
         GridBagConstraints gbc_lblZurckAm = new GridBagConstraints();
         gbc_lblZurckAm.anchor = GridBagConstraints.WEST;
         gbc_lblZurckAm.insets = new Insets(0, 0, 5, 5);
         gbc_lblZurckAm.gridx = 0;
         gbc_lblZurckAm.gridy = 2;
         panel_1.add(lblZurckAm, gbc_lblZurckAm);
 
        dateLabel = new JLabel();
        dateLabel.setEnabled(false);
         Date today = new Date();
 
        dateLabel.setText(library.getDateplusDays(today, 30));
         GridBagConstraints gbc_textField_2 = new GridBagConstraints();
         gbc_textField_2.insets = new Insets(0, 0, 5, 5);
         gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
         gbc_textField_2.gridx = 1;
         gbc_textField_2.gridy = 2;
        panel_1.add(dateLabel, gbc_textField_2);

 
         panel_2 = new JPanel();
         panel_6.add(panel_2);
         panel_2.setBorder(new TitledBorder(null, "Ausleihen von...", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         panel_2.setLayout(new BorderLayout(0, 0));
 
         JScrollPane scrollPane = new JScrollPane();
         panel_2.add(scrollPane, BorderLayout.CENTER);
 
         loanTable = new JTable();
         loanTable.setEnabled(false);
         loanTable.setFillsViewportHeight(true);
         scrollPane.add(loanTable);
         scrollPane.setViewportView(loanTable);
 
         loanTable.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 JTable target = (JTable) e.getSource();
                 if (e.getClickCount() == 2) {
                     int row = target.getSelectedRow();
                     if (row >= 0) {
                         Loan loan = library.getLoansOfCustomer(cust).get(row);
                         windowCtrl.openDetailLoanWindow(loan);
                     }
                 }
             }
         });
 
         JSplitPane splitPane = new JSplitPane();
         contentPane.add(splitPane);
 
         splitPane.setLeftComponent(panel);
         splitPane.setRightComponent(panel_6);
         splitPane.setResizeWeight(0.5);
         splitPane.setDividerLocation(500);
 
     }
 
     public void tryAddingLoanToCustomer() {
         try {
             Integer copy_id = Integer.parseInt(textField_1.getText());
             Copy copy = library.getCopyfromId(copy_id);
 
             if (copy != null) {
                 Loan feedback = library.createAndAddLoan(cust, copy);
 
                 if (feedback == null) {
                     errorLabel.setText("Dieses Exemplar ist bereits ausgeliehen");
                 }
             } else {
                 errorLabel.setText("Dieses Exemplar existiert nicht oder wurde verloren");
             }
         } catch (NumberFormatException e) {
             errorLabel.setText("Bitte geben Sie eine Zahl ein, die kleiner als 2147483647 ist");
         }
     }
 
     public void updateRightSide(int row) {
         try {
             cust = library.getCustomers().get(customerTable.convertRowIndexToModel(row));
         } catch (IndexOutOfBoundsException e) {
             //Do Nothing
         }
         if (cust != null) {
             loanTable.setModel(new AddLoanWindowLoanTableModel(library, cust));
             loanTable.setEnabled(true);
             panel_2.setBorder(new TitledBorder(null, "Ausleihen von " + cust.getSurname() + " " + cust.getName(), TitledBorder.LEADING, TitledBorder.TOP, null, null));
 
             if (library.getCustomerStatus(cust) == CustomerStatus.OK) {
                 textField_1.setEnabled(true);
                 textField_1.setText("");
                dateLabel.setEnabled(true);
                 btnAnzeigen.setEnabled(true);
             } else {
                 textField_1.setEnabled(false);
                 textField_1.setText("");
                dateLabel.setEnabled(false);
                 btnAnzeigen.setEnabled(false);
             }
         }
 
     }
 
 
     @Override
     public void update(Observable o, Object arg) {
         int row = customerTable.getSelectedRow();
         updateRightSide(row);
         custTableModel.fireTableDataChanged();
         ((AbstractTableModel) loanTable.getModel()).fireTableDataChanged();
         customerTable.setRowSelectionInterval(row, row);
 
 
     }
 }
