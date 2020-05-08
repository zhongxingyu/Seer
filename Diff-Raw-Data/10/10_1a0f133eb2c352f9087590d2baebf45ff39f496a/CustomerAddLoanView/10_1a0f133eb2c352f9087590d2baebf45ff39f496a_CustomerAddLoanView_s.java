 package views;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 
 import javax.swing.AbstractAction;
 import javax.swing.JTextField;
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 import javax.swing.RowFilter;
 
 import localization.Messages;
 
 import com.jgoodies.forms.builder.ButtonBarBuilder;
 
 import components.MySearchField;
 
 import settings.Icons;
 import viewModels.BookCopiesTableModel;
 import viewModels.CopiesTableModel;
 import viewModels.CustomerCopiesTableModel;
 import views.AbstractEditor.CancelAction;
 import views.AbstractEditor.OkAction;
 
 import domain.Customer;
 import domain.Library;
 
 public class CustomerAddLoanView extends AbstractViewer {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4364856647691500471L;
 	private final JPanel contentPanel = new JPanel();
 	private JTable copiesTable;
 	private JTable loanTable;
 	private Library library;
 	private Customer customer;
 	private java.util.List<RowFilter<Object, Object>> filters_customer = new ArrayList<RowFilter<Object, Object>>(
 			3);
 	
 	/**
 	 * Create the dialog.
 	 */
 	public CustomerAddLoanView(Customer customer, Library library) {
 		
 		this.customer = customer;
 		this.library = library;
 		createView();
 		setVisible(true);
 	}
 
 	private void createView() {
		setBounds(100, 100, 450, 300);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		GridBagLayout gbl_contentPanel = new GridBagLayout();
 		gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
 		gbl_contentPanel.columnWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
 		contentPanel.setLayout(gbl_contentPanel);
 		{
 			JLabel lblCustomer = new JLabel("Customer:");
 			GridBagConstraints gbc_lblCustomer = new GridBagConstraints();
 			gbc_lblCustomer.insets = new Insets(0, 0, 5, 5);
 			gbc_lblCustomer.gridx = 0;
 			gbc_lblCustomer.gridy = 0;
 			contentPanel.add(lblCustomer, gbc_lblCustomer);
 		}
 		{
 			JLabel lblcustomername = new JLabel("%customer_name");
 			GridBagConstraints gbc_lblcustomername = new GridBagConstraints();
 			gbc_lblcustomername.insets = new Insets(0, 0, 5, 5);
 			gbc_lblcustomername.gridx = 1;
 			gbc_lblcustomername.gridy = 0;
 			contentPanel.add(lblcustomername, gbc_lblcustomername);
 		}
 		
 		{
 			JButton btnAddLoan = new JButton("Add");
 			GridBagConstraints gbc_btnAdd = new GridBagConstraints();
 			gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
 			gbc_btnAdd.gridx = 2;
 			gbc_btnAdd.gridy = 1;
 			contentPanel.add(btnAddLoan, gbc_btnAdd);
 		}
 		{
 			JScrollPane copiesScrollPane = new JScrollPane();
 			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 			gbc_scrollPane.gridwidth = 3;
 			gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
 			gbc_scrollPane.fill = GridBagConstraints.BOTH;
 			gbc_scrollPane.gridx = 0;
 			gbc_scrollPane.gridy = 2;
 			contentPanel.add(copiesScrollPane, gbc_scrollPane);
 			{
 				copiesTable = new JTable(new CopiesTableModel(library));
 				copiesScrollPane.setViewportView(copiesTable);
 				
 			}
 		}
 		{
 			JScrollPane loanScrollPane = new JScrollPane();
 			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 			gbc_scrollPane.gridwidth = 3;
 			gbc_scrollPane.fill = GridBagConstraints.BOTH;
 			gbc_scrollPane.insets = new Insets(0, 0, 0, 0);
 			gbc_scrollPane.gridx = 0;
 			gbc_scrollPane.gridy = 3;
 			contentPanel.add(loanScrollPane, gbc_scrollPane);
 			{
 				loanTable = new JTable(new CustomerCopiesTableModel(library, customer));
 				loanScrollPane.setViewportView(loanTable);
 			}
 		}
 		{
 			JPanel buttonPane = buttonPanel();
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			
 		}
 		{
 			searchfield = new MySearchField(copiesTable, 1, filters_customer);
 			GridBagConstraints gbc_txtSearchfield = new GridBagConstraints();
 			gbc_txtSearchfield.gridwidth = 2;
 			gbc_txtSearchfield.insets = new Insets(0, 0, 5, 5);
 			gbc_txtSearchfield.fill = GridBagConstraints.HORIZONTAL;
 			gbc_txtSearchfield.gridx = 0;
 			gbc_txtSearchfield.gridy = 1;
 			contentPanel.add(searchfield, gbc_txtSearchfield);
 			searchfield.setColumns(10);
 		}
 	}
 	protected final JPanel buttonPanel() {
 		JButton closeBtn = new JButton();
 		closeBtn.setAction(new CancelAction());
 		closeBtn.setIcon(Icons.IconEnum.CANCEL.getIcon(16));
 		closeBtn.setText(Messages.getString("Global.btnCancel.title"));
 		JButton saveBtn = new JButton();
 		saveBtn.setAction(new OkAction());
 		saveBtn.setIcon(Icons.IconEnum.SAVE.getIcon(16));
 		saveBtn.setText(Messages.getString("Global.btnSave.title"));
 
 		ButtonBarBuilder buttonBar = new ButtonBarBuilder();
 		buttonBar.addButton(closeBtn);
 		buttonBar.addButton(saveBtn);
 		JPanel button_panel = new JPanel();
 		button_panel.setLayout(new BorderLayout(0, 0));
 		button_panel.add(buttonBar.build(), BorderLayout.EAST);
 		return button_panel;
 	}
 	protected final class OkAction extends AbstractAction {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private OkAction() {
 			super("Ok");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			// don't close the frame on OK unless it validates
 		}
 	}
 
 	protected final class CancelAction extends AbstractAction {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private CancelAction() {
 			super("Cancel");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			setInvalid();
 			
 
 
 		}
 
 		private void setInvalid() {
 			
 			
 		}
 
 	}
 }
