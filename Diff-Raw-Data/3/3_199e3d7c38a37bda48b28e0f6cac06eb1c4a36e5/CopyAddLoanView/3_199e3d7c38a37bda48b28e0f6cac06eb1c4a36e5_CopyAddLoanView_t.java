 package views;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import components.IconCellRenderer;
 
 
 
 import settings.Icons;
 import viewModels.CustomerTableModel;
 
 import localization.Messages;
 
 import domain.Copy;
 import domain.Customer;
 import domain.Library;
 import domain.Setting;
 
 public class CopyAddLoanView extends AbstractViewer {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3557493872238661967L;
 	private final JPanel contentPanel = new JPanel();
 	private Copy copy;
 	private Library library;
 	private JTable tblCustomers;
 
 	public CopyAddLoanView(Library lib, Copy cop) {
 		library =lib;
 		copy = cop;
 		setTitle(Messages.getString("CopyAddLoanView.title"));
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		contentPanel.setLayout(new BorderLayout(0, 0));
 		JPanel pnlBookPart = new JPanel();
 		pnlBookPart.setBorder(new TitledBorder(null, Messages.getString("MasterView.pnlBooks.title"),
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		contentPanel.add(pnlBookPart, BorderLayout.NORTH);
 		JLabel lblSelectedBookTitleText = new JLabel(Messages.getString("CopyAddLoanView.lblSelectedBookTitleText"));
 		JLabel lblSelectedBookCopyText = new JLabel(Messages.getString("CopyAddLoanView.lblSelectedBookCopyText"));
 
 		String title = (copy.getTitle().getName().length() > 50) ? copy.getTitle().getName().substring(0, 50) : copy.getTitle().getName();
 		JLabel lblSelectedBookTitleValue = new JLabel(title);
 		JLabel lblSelectedBookCopy = new JLabel(copy.getInventoryNumber()+"");
 		GroupLayout gl_pnlBookPart = new GroupLayout(pnlBookPart);
 		gl_pnlBookPart.setHorizontalGroup(gl_pnlBookPart.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_pnlBookPart
 										.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(gl_pnlBookPart
 														.createParallelGroup(Alignment.TRAILING,false)
 														.addComponent(
 																lblSelectedBookCopyText,
 																Alignment.LEADING,
 																GroupLayout.DEFAULT_SIZE,
 																GroupLayout.DEFAULT_SIZE,
 																Short.MAX_VALUE)
 														.addComponent(
 																lblSelectedBookTitleText,
 																Alignment.LEADING,
 																GroupLayout.DEFAULT_SIZE,
 																155,
 																Short.MAX_VALUE))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(gl_pnlBookPart
 														.createParallelGroup(Alignment.LEADING)
 														.addComponent(lblSelectedBookTitleValue)
 														.addComponent(lblSelectedBookCopy))
 										.addContainerGap(195, Short.MAX_VALUE)));
 		gl_pnlBookPart
 				.setVerticalGroup(gl_pnlBookPart
 						.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_pnlBookPart
 										.createSequentialGroup()
 										.addGroup(gl_pnlBookPart
 														.createParallelGroup(Alignment.BASELINE)
 														.addComponent(lblSelectedBookTitleText)
 														.addComponent(lblSelectedBookTitleValue))
 										.addPreferredGap(
 												ComponentPlacement.RELATED,
 												GroupLayout.DEFAULT_SIZE,
 												Short.MAX_VALUE)
 										.addGroup(gl_pnlBookPart
 														.createParallelGroup(Alignment.BASELINE)
 														.addComponent(lblSelectedBookCopyText)
 														.addComponent(lblSelectedBookCopy))));
 		pnlBookPart.setLayout(gl_pnlBookPart);
 		
 		// Customers
 
 		JPanel pnlCustomersPart = new JPanel();
 		pnlCustomersPart.setBorder(new TitledBorder(null, Messages.getString("MasterView.pnlCustomers.title"),
 		TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		contentPanel.add(pnlCustomersPart, BorderLayout.CENTER);
 		pnlCustomersPart.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnlSearch = new JPanel();
 		pnlCustomersPart.add(pnlSearch, BorderLayout.NORTH);
 		GridBagLayout gbl_pnlSearch = new GridBagLayout();
 		gbl_pnlSearch.columnWidths = new int[] { 163, 86, 0 };
 		gbl_pnlSearch.rowHeights = new int[] { 20, 0 };
 		gbl_pnlSearch.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
 		gbl_pnlSearch.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
 		pnlSearch.setLayout(gbl_pnlSearch);
 
 		tblCustomers = 	new JTable(new CustomerTableModel(library));	
 		tblCustomers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 
 		
 		JScrollPane scrollPaneCustomer = new JScrollPane();
 		pnlCustomersPart.add(scrollPaneCustomer, BorderLayout.CENTER);
 
 		
 		
 		scrollPaneCustomer.setViewportView(tblCustomers);
 		
 		JPanel buttonPane = new JPanel();
 		getContentPane().add(buttonPane, BorderLayout.SOUTH);
 		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		final JButton btnAddtoloan = new JButton(Messages.getString("CopyAddLoanView.btnAddloan.text"));
 		btnAddtoloan.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(tblCustomers.getSelectedRowCount() < 1){
 				}else{
 					int selection = tblCustomers.getSelectedRow();
 					Customer customer = library.getCustomers().get(selection);
 					if (library.getCustomerOngoingLoans(customer).size()>=3){
 					} else {
 						library.createAndAddLoan(library.getCustomers().get(tblCustomers.getSelectedRow()), copy);
 						dispose();
 					}
 				}
 				
 			}
 		});
 		
 		
 		
 		tblCustomers.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				btnAddtoloan.setEnabled(false);
 				if (tblCustomers.getSelectedRowCount() == 1) {
 					String s = tblCustomers.getValueAt(tblCustomers.getSelectedRow(), 0).toString();
 					if (s.contains(Messages.getString("CustomersInventoryView.Overdue")) == false && Integer.parseInt(s.substring(0, 1)) < Setting.getMaxBorrowsPerCustomer()) {
 						btnAddtoloan.setEnabled(true);
 					} else {
 						btnAddtoloan.setEnabled(false);
 					}
 				}
 				
 			}
 		});
 //		
 		buttonPane.add(btnAddtoloan);
 		getRootPane().setDefaultButton(btnAddtoloan);
 		btnAddtoloan.setEnabled(false);
 
 		JButton btnCancel = new JButton(Messages.getString("Global.btnClose.title"));
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();
 			}
 		});
 		buttonPane.add(btnCancel);
 		
 		btnAddtoloan.setIcon(Icons.IconEnum.SAVE.getIcon(24));
 		btnCancel.setIcon(Icons.IconEnum.CANCEL.getIcon(24));
 		tblCustomers.getColumnModel().getColumn(0).setCellRenderer(new IconCellRenderer());
 
 		btnCancel.setMnemonic(KeyEvent.VK_C);
 		btnAddtoloan.setMnemonic(KeyEvent.VK_S);
 		createWindow();
 	}
 
 }
