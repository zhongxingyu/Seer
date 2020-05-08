 package viewPanels;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 
 import settings.Icons;
 import viewModels.LendingTableModel;
 import views.ReturnLoanView;
 import views.ReturnMultipleLoansView;
 import views.WarningWindow;
 
 import components.IconCellRenderer;
 import components.MySearchField;
 
 import domain.Library;
 import domain.Loan;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import localization.Messages;
 
 public class LoanPanel extends JPanel implements Observer{
 	private static final long serialVersionUID = 6034035113335278353L;
 	private static final Color background_Color = new Color(226, 226, 226);
 	private Library library;
 	private JTextField txtSearchfield;
 	private JTable lending_table;
 	private JButton btnDisplayLoan;
 	private JLabel display_number_of_rents;
 	private WarningWindow warningWindow;
 	private JCheckBox chckbxOverdue;
 	private JLabel display_overdue;
     private java.util.List<RowFilter<Object,Object>> filters = new ArrayList<RowFilter<Object,Object>>(3);  
 	
 	public LoanPanel(Library library){
 		super();
 		this.library = library;
 		this.library.addObserver(this);
 		initialize();
 		
 	}
 	
 	private void initialize (){
 		GridBagLayout gbl_lendingTab = new GridBagLayout();
 		gbl_lendingTab.columnWidths = new int[]{0, 0};
 		gbl_lendingTab.rowHeights = new int[]{48, 0, 0};
 		gbl_lendingTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_lendingTab.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		setLayout(gbl_lendingTab);
 		
 		JPanel panelrentstatistics = new JPanel();
 		panelrentstatistics.setBackground(background_Color);
 		panelrentstatistics.setBorder(new TitledBorder(null, Messages.getString("LoanPanel.pnlLoanStats.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagConstraints gbc_panelrentstatistics = new GridBagConstraints();
 		gbc_panelrentstatistics.insets = new Insets(0, 0, 5, 0);
 		gbc_panelrentstatistics.fill = GridBagConstraints.BOTH;
 		gbc_panelrentstatistics.gridx = 0;
 		gbc_panelrentstatistics.gridy = 0;
 		add(panelrentstatistics, gbc_panelrentstatistics);
 		GridBagLayout gbl_panelrentstatistics = new GridBagLayout();
 		gbl_panelrentstatistics.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_panelrentstatistics.rowHeights = new int[]{0, 0};
 		gbl_panelrentstatistics.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panelrentstatistics.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panelrentstatistics.setLayout(gbl_panelrentstatistics);
 		
 		JLabel lblNumber_of_Rents = new JLabel(Messages.getString("LoanPanel.lblNrOfLoansText.text"));
 		GridBagConstraints gbc_lblNumber_of_Rents = new GridBagConstraints();
 		gbc_lblNumber_of_Rents.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNumber_of_Rents.anchor = GridBagConstraints.EAST;
 		gbc_lblNumber_of_Rents.gridx = 0;
 		gbc_lblNumber_of_Rents.gridy = 0;
 		panelrentstatistics.add(lblNumber_of_Rents, gbc_lblNumber_of_Rents);
 		
 		display_number_of_rents = new JLabel(library.getLentOutCopies().size()+"");
 		GridBagConstraints gbc_display_number_of_rents = new GridBagConstraints();
 		gbc_display_number_of_rents.insets = new Insets(0, 0, 0, 5);
 		gbc_display_number_of_rents.anchor = GridBagConstraints.WEST;
 		gbc_display_number_of_rents.gridx = 1;
 		gbc_display_number_of_rents.gridy = 0;
 		panelrentstatistics.add(display_number_of_rents, gbc_display_number_of_rents);
 		
 		
 		JLabel lblOverdue = new JLabel(Messages.getString("LoanPanel.pnlLoanStats.overdues"));
 		GridBagConstraints gbc_lblOverdue = new GridBagConstraints();
 		gbc_lblOverdue.insets = new Insets(0, 0, 0, 5);
 		gbc_lblOverdue.gridx = 6;
 		gbc_lblOverdue.gridy = 0;
 		panelrentstatistics.add(lblOverdue, gbc_lblOverdue);
 		
 		display_overdue = new JLabel(library.getOverdueLoans().size()+"");
 		GridBagConstraints gbc_display_overdue = new GridBagConstraints();
 		gbc_display_overdue.gridx = 7;
 		gbc_display_overdue.gridy = 0;
 		panelrentstatistics.add(display_overdue, gbc_display_overdue);
 		
 		JPanel panel = new JPanel();
 		panel.setBackground(background_Color);
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 1;
 		add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_panel.rowHeights = new int[]{0, 0, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		JScrollPane lending_scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
 		gbc_scrollPane_2.gridwidth = 6;
 		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_2.gridx = 0;
 		gbc_scrollPane_2.gridy = 0;
 		panel.add(lending_scrollPane, gbc_scrollPane_2);
 				
 		lending_table = new JTable();
 		lending_table.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(e.getClickCount() == 2){
 					openEditLoanWindow();
 				}
 				
 				if (lending_table.getSelectedRowCount()==1)
 					btnDisplayLoan.setEnabled(true);
 				else
 					btnDisplayLoan.setEnabled(false);
 			}
 		});
 		lending_table.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if(arg0.getKeyCode()==KeyEvent.VK_ENTER){
 					arg0.consume();
 					openEditLoanWindow();
 				}
 			}
 		});
 		lending_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		lending_scrollPane.setViewportView(lending_table);
 		setLendingModel(new LendingTableModel(library));
 		
 		
 		txtSearchfield = new MySearchField(lending_table,2,filters);
 
 		GridBagConstraints gbc_txtSearchfield = new GridBagConstraints();
 		gbc_txtSearchfield.insets = new Insets(0, 0, 0, 5);
 		gbc_txtSearchfield.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtSearchfield.gridx = 2;
 		gbc_txtSearchfield.gridy = 1;
 		panel.add(txtSearchfield, gbc_txtSearchfield);
 		txtSearchfield.setColumns(10);
 		final RowFilter<Object, Object> DueFilter = RowFilter.regexFilter("(?i)due");
 	
 		chckbxOverdue = new JCheckBox(Messages.getString("LoansInventoryView.chckbxOnlyOverdue.text"));
 		chckbxOverdue.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				if(chckbxOverdue.getSelectedObjects()!=null){ //is selected
 					try {
 						filters.add(DueFilter);
 						applyFilter(lending_table);
 					} catch (java.util.regex.PatternSyntaxException ee) {
 					}
 				} else {
 					filters.remove(DueFilter);
 					applyFilter(lending_table);
 					//					sorter.setRowFilter(RowFilter.regexFilter("(?i)"));
 					
 				}
 			}
 
 		});
 		GridBagConstraints gbc_chckbxOverdue = new GridBagConstraints();
 		gbc_chckbxOverdue.insets = new Insets(0, 0, 0, 5);
 		gbc_chckbxOverdue.gridx = 3;
 		gbc_chckbxOverdue.gridy = 1;
 		panel.add(chckbxOverdue, gbc_chckbxOverdue);
 		
 		btnDisplayLoan = new JButton(Messages.getString("LoansInventoryView.btnCloseLoan.text"),Icons.IconEnum.OK.getIcon(24));
 		btnDisplayLoan.setEnabled(false);
 		btnDisplayLoan.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 					openEditLoanWindow();
 			}
 		});
 		GridBagConstraints gbc_btnDisplay_selected = new GridBagConstraints();
 		gbc_btnDisplay_selected.insets = new Insets(0, 0, 0, 5);
 		gbc_btnDisplay_selected.gridx = 4;
 		gbc_btnDisplay_selected.gridy = 1;
 		panel.add(btnDisplayLoan, gbc_btnDisplay_selected);
 		RowFilter<Object,Object> excludeEnded = new RowFilter<Object,Object>() {
 			  public boolean include(Entry<? extends Object, ? extends Object> entry) {
 				  if (entry.getValue(0) == "Ended")
 					  return false;
 				  return true;
 			 }
 			};
 			filters.add(excludeEnded);
 	}
 
 
 
 	private void applyFilter(JTable table) {
 		@SuppressWarnings("unchecked")
 		TableRowSorter<LendingTableModel> sorter = (TableRowSorter<LendingTableModel>) table.getRowSorter();
 
 		RowFilter<Object,Object> serviceFilter = RowFilter.andFilter(filters);  
 
 		sorter.setRowFilter(serviceFilter);
 		table.setRowSorter(sorter);
 		AbstractTableModel t = (AbstractTableModel) table.getModel();
 		t.fireTableDataChanged();
 	}
 
 	private void setLendingModel(LendingTableModel model) {
 		lending_table.setModel(model);
 		lending_table.getColumnModel().getColumn(1).setMaxWidth(200);
 		lending_table.getColumnModel().getColumn(1).setPreferredWidth(120);
 		lending_table.getColumnModel().getColumn(1).setMinWidth(20);
 		lending_table.getColumnModel().getColumn(1).setCellRenderer(new IconCellRenderer());
 		
 		lending_table.getColumnModel().getColumn(0).setMaxWidth(30);
 		lending_table.getColumnModel().getColumn(2).setMinWidth(90);
 		lending_table.getColumnModel().getColumn(2).setMaxWidth(90);
 		lending_table.getColumnModel().getColumn(4).setMinWidth(100);
 		lending_table.getColumnModel().getColumn(4).setMaxWidth(160);
 	}
 
 	
 	public void updateFields(){
 		display_number_of_rents.setText(library.getLentOutCopies().size()+"");
 		display_overdue.setText(library.getOverdueLoans().size()+"");
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		updateFields();
 	}
 
 	private Loan getSelectedLoan() {
 		return library.getOngoingLoans().get(lending_table.convertRowIndexToModel(lending_table.getSelectedRow()));
 	}
 
 	private void openEditLoanWindow() {
 		try{
 //			new ReturnLoanView(library, getSelectedLoan()).setVisible(true);
 				if (lending_table.getSelectedRowCount()==1) {
 					new ReturnLoanView(library, getSelectedLoan()).setVisible(true);				}
 				if (lending_table.getSelectedRowCount()>1) {
 					List<Loan> loanList = new ArrayList<Loan>();
 					for (int i: lending_table.getSelectedRows()) {
 						loanList.add(library.getOpenLoans().get(lending_table.convertRowIndexToModel(i)));
 					}
 					new ReturnMultipleLoansView(library, loanList).setVisible(true);
 
 				}
 				
 			
 				
 			
 		}
 	 catch (IndexOutOfBoundsException e) {
 		warningWindow = new WarningWindow("Please select a Loan!");
 		warningWindow.setVisible();
 	}
 
 	}
 
 }
