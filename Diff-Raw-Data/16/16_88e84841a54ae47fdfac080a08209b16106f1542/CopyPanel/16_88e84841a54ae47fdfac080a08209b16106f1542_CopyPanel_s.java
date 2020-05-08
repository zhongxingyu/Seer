 package viewPanels;
 
 import java.awt.LayoutManager;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionListener;
 import domain.Book;
 import domain.Copy;
 import domain.Loan;
 import domain.Copy.Condition;
 import domain.Library;
 
 import settings.Icons;
 import viewModels.BookCopiesTableModel;
 import views.CopyAddLoanView;
 import views.ReturnLoanView;
 
 import java.awt.Insets;
 import com.jgoodies.forms.builder.ButtonBarBuilder;
 import components.ComboBoxCellRenderer;
 import components.IconCellRenderer;
 import components.IconCustomerCellRenderer;
 import components.MyJTable;
 import controller.LoanTableSelectListener;
 
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import localization.Messages;
 
 public class CopyPanel extends JPanel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6720768505050317833L;
 	private Book book;
 	private Library library;
 	private MyJTable table;
 	private JPanel table_panel;
 	private JPanel south_panel;
 
 	private JPanel button_panel;
 	private JDialog parent;
 
 	/**
 	 * @wbp.parser.constructor
 	 */
 	public CopyPanel(Library lib, Book book, JDialog p) {
 		this.library = lib;
 		this.book = book;
 		this.parent = p;
 		table = new MyJTable(new BookCopiesTableModel(library, book));
 		init();
 	}
 
 
 
 	private void init() {
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 0, 0 };
 		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		JPanel north_panel = new JPanel();
 		north_panel.setBorder(new TitledBorder(null, Messages.getString("CopyPanel.Title"),
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagConstraints gbc_north_panel = new GridBagConstraints();
 		gbc_north_panel.insets = new Insets(0, 0, 5, 0);
 		gbc_north_panel.fill = GridBagConstraints.BOTH;
 		gbc_north_panel.gridx = 0;
 		gbc_north_panel.gridy = 0;
 		add(north_panel, gbc_north_panel);
 		GridBagLayout gbl_north_panel = new GridBagLayout();
 		gbl_north_panel.columnWidths = new int[] { 0, 0 };
 		gbl_north_panel.rowHeights = new int[] { 0, 0 };
 		gbl_north_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
 		gbl_north_panel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
 		north_panel.setLayout(gbl_north_panel);
 
 		table_panel = new JPanel();
 		GridBagConstraints gbc_table_panel = new GridBagConstraints();
 		gbc_table_panel.fill = GridBagConstraints.BOTH;
 		gbc_table_panel.gridx = 0;
 		gbc_table_panel.gridy = 0;
 		north_panel.add(table_panel, gbc_table_panel);
 
 		createtableScrollPane(table_panel).setViewportView(table);
 		setModel();
 		button_panel = new JPanel();
 		table_panel.add(button_panel, BorderLayout.NORTH);
 		ButtonBarBuilder addRemoveCopy = new ButtonBarBuilder();
 
 		JButton addBtn = new JButton();
 		addBtn.addActionListener(new AddAction());
 		addBtn.setIcon(Icons.IconEnum.ADD.getIcon(16));
 		addBtn.setText(Messages.getString("CopyPanel.btnAddcopy.text"));
 		JButton removeBtn = new JButton();
 		removeBtn.addActionListener(new RemoveAction());
 		removeBtn.setIcon(Icons.IconEnum.DELETE.getIcon(16));
 		removeBtn
 				.setText(Messages.getString("CopyPanel.btnDelbook.text"));
 		removeBtn.setEnabled(false);
 
 		JButton addLoanBtn = new JButton();
 		addLoanBtn.setAction(new AddLoanAction());
 		addLoanBtn.setIcon(Icons.IconEnum.ADDLOAN.getIcon(16));
 		addLoanBtn.setText(Messages
 				.getString("CopyPanel.btnAddloan.text"));
 		addLoanBtn.setEnabled(false);
 		JButton returnLoanBtn = new JButton();
 		returnLoanBtn.setAction(new ReturnAction());
 		returnLoanBtn.setIcon(Icons.IconEnum.CLOSELOAN.getIcon(16));
 		returnLoanBtn.setText(Messages
 				.getString("CopyPanel.btnReturnLoan.text"));
 		returnLoanBtn.setEnabled(false);
 		table.getSelectionModel()
 				.addListSelectionListener(
 						(ListSelectionListener) new LoanTableSelectListener(
 								table, addLoanBtn, Messages
 										.getString("Domain.Book.Available")));
 		table.getSelectionModel().addListSelectionListener(
 				(ListSelectionListener) new LoanTableSelectListener(table,
 						returnLoanBtn, Messages
 								.getString("Domain.Book.Unavailable")
 								+ " / "
 								+ Messages.getString("Domain.Book.Lent")));
 		table.getSelectionModel()
 				.addListSelectionListener(
 						(ListSelectionListener) new LoanTableSelectListener(
 								table, removeBtn, Messages
 										.getString("Domain.Book.Unavailable")
 										+ " / "
 										+ Messages
 												.getString("Domain.Book.Lent"),
 								true));
 
 		addRemoveCopy.addButton(returnLoanBtn, addLoanBtn, removeBtn, addBtn);
 		button_panel.setLayout(new BorderLayout(0, 0));
 		button_panel.add(addRemoveCopy.build(), BorderLayout.EAST);
 
 		south_panel = new JPanel();
 		GridBagConstraints gbc_south_panel = new GridBagConstraints();
 		gbc_south_panel.fill = GridBagConstraints.BOTH;
 		gbc_south_panel.gridx = 0;
 		gbc_south_panel.gridy = 1;
 		add(south_panel, gbc_south_panel);
 		south_panel.setLayout(new BorderLayout(0, 0));
 
 		ButtonBarBuilder buttonBar = new ButtonBarBuilder();
 		JButton closeBtn = new JButton();
 		closeBtn.addActionListener(new CloseAction());
 		closeBtn.setIcon(Icons.IconEnum.CANCEL.getIcon(24));
 		closeBtn.setText(Messages.getString("Global.btnClose.title"));
 		buttonBar.addButton(closeBtn);
 		south_panel.add(buttonBar.build(), BorderLayout.EAST);
 	}
 
 	/**
 	 * 
 	 */
 	private void setModel() {
 		table.setRowHeight(30);
 		JComboBox<Condition> comboBoxCondition = new JComboBox<Condition>(
 				Condition.values());
 		// comboBoxCondition.setRenderer(new IconListCellRenderer());
 		table.getColumnModel().getColumn(1)
 				.setCellRenderer(new IconCellRenderer());
 		table.getColumnModel().getColumn(2)
 				.setCellRenderer(new IconCustomerCellRenderer());
 		table.getColumnModel().getColumn(3)
 				.setCellEditor(new DefaultCellEditor(comboBoxCondition));
 		table.getColumnModel()
 				.getColumn(3)
 				.setCellRenderer(
 						new ComboBoxCellRenderer(Copy.Condition.values()));
		// table.setAutoResizeMode(JTableComponent.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
 		table.getColumnModel().getColumn(0).setPreferredWidth(40);
 		table.getColumnModel().getColumn(0).setMaxWidth(40);
 		table.getColumnModel().getColumn(1).setPreferredWidth(50);
 		table.getColumnModel().getColumn(2).setPreferredWidth(200);
 		table.getColumnModel().getColumn(3).setPreferredWidth(50);
 	}
 
 	private JScrollPane createtableScrollPane(JPanel panel_1) {
 		table_panel.setLayout(new BorderLayout(0, 0));
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 0;
 		panel_1.add(scrollPane);
 		return scrollPane;
 	}
 
 	private final class AddAction extends AbstractAction {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private AddAction() {
 			super(Messages.getString("CopyPanel.btnAddcopy.text"));
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			library.createAndAddCopy(book);
 		}
 	}
 
 	private final class ReturnAction extends AbstractAction {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private ReturnAction() {
 			super("");
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (table.getSelectedRowCount() == 1) {
				int rightNr = table.getSelectedRow();
				new ReturnLoanView(library, library.getOpenLoans().get(rightNr))
						.setVisible(true);
 			}
 			if (table.getSelectedRowCount() > 1) {
 				List<Loan> loanList = new ArrayList<Loan>();
 				for (int i : table.getSelectedRows()) {
 					loanList.add(library.getOpenLoans().get(i));
 				}
 				// new LoanCloseMultipleView(library,
 				// loanList).setVisible(true);
 			}
 		}
 	}
 
 	private final class AddLoanAction extends AbstractAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -5190401524732242389L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
			System.out.println("");
 			new CopyAddLoanView(library, getSelectedCopies().get(0));
 		}
 
 	}
 
 	private final class RemoveAction extends AbstractAction {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private RemoveAction() {
 			super("Remove Selected Copy");
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String confirmationMessage;
 			String confirmationTitle;
 			if (table.getSelectedRowCount() == 1) {
 				confirmationMessage = Messages.getString("CopyPanel.ConfirmationDeleteCopyMessage");
 				confirmationTitle = Messages.getString("CopyPanel.ConfirmationDeleteCopyTitle");
 			} else {
 				confirmationMessage = Messages.getString("CopyPanel.ConfirmationDeleteCopiesMessage");
 				confirmationTitle = Messages.getString("CopyPanel.ConfirmationDeleteCopiesTitle");
 
 			}
 			try {
 				if (JOptionPane.showConfirmDialog(getCopyPanel(),
 						confirmationMessage, confirmationTitle,
 						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
 						Icons.IconEnum.QUESTION.getIcon(48)) == 0) {
 					List<Copy> copiesToDelete = new ArrayList<Copy>();
 					for (int row : table.getSelectedRows()) {
 						if (library.getCopiesOfBook(book).get(row)
 								.isInLendable()) {
 							copiesToDelete.add(library.getCopiesOfBook(book)
 									.get(row));
 						} else {
 						}
 					}
 					library.removeCopies(copiesToDelete);
 				}
 
 			} catch (ArrayIndexOutOfBoundsException e1) {
 				// System.err.println();
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	private final class CloseAction extends AbstractAction {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private CloseAction() {
 			super("Close");
 
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			dispose();
 
 		}
 	}
 
 	protected CopyPanel getCopyPanel() {
 		return this;
 	}
 
 	protected Stack<Copy> getSelectedCopies() {
 		Stack<Copy> copies = new Stack<Copy>();
 		if (table.getSelectedRowCount() > 1) {
 			int[] rows = table.getSelectedRows();
 			Copy c = null;
 			for (int i : rows) {
 				c = library.getCopiesOfBook(book).get(
 						table.convertRowIndexToModel(i));
 				copies.add(c);
 			}
 		}
 		if (table.getSelectedRowCount() == 1)
 			copies.add(library.getCopiesOfBook(book).get(
 					table.convertRowIndexToModel(table.getSelectedRow())));
 		return copies;
 	}
 
 	public CopyPanel(LayoutManager layout) {
 		super(layout);
 	}
 
 	public CopyPanel(boolean isDoubleBuffered) {
 		super(isDoubleBuffered);
 	}
 
 	public CopyPanel(LayoutManager layout, boolean isDoubleBuffered) {
 		super(layout, isDoubleBuffered);
 	}
 
 	private void dispose() {
 		parent.dispose();
 	}
 }
