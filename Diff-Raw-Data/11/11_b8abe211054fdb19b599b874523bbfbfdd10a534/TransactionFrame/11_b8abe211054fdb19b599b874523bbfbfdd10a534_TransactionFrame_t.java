 /***************************************************************************
     begin........: January 2012
     copyright....: Sebastian Fedrau
     email........: lord-kefir@arcor.de
  ***************************************************************************/
 
 /***************************************************************************
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     General Public License for more details.
  ***************************************************************************/
 package accounting.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.text.*;
 import java.util.*;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.table.*;
 import org.picocontainer.PicoContainer;
 import accounting.*;
 import accounting.application.*;
 import accounting.application.Currency;
 import accounting.data.*;
 
 public class TransactionFrame extends AFrame implements ActionListener, MouseListener, ICategoryListener, ICurrencyListener, IAccountListener
 {
 	private static final long serialVersionUID = -8339739594637884467L;
 	private JComboBox comboTimeframe;
 	private JComboBox comboAccount;
 	private JButton buttonAccounts;
 	private JTable tableTransactions;
 	private JButton buttonAdd;
 	private JButton buttonDelete;
 	private JButton buttonTransfer;
 	private JPanel panelOpeningBalance;
 	private JLabel labelOpeningBalance;
 	private JPanel panelClosingBalance;
 	private JLabel labelClosingBalance;
 	private JMenuBar menuBar;
 	private Factory factory;
 	private Translation translation;
 
 	/*
 	 * table model:
 	 */
 	private class TransactionTableModel extends AbstractTableModel implements IEntityListener
 	{
 		private static final long serialVersionUID = 6010780954766295929L;
 		private List<Transaction> transactions;
 		private List<Double> balances;
 		private double openingBalance = 0;
 
 		public TransactionTableModel()
 		{
 			transactions = new LinkedList<Transaction>();
 			balances = new LinkedList<Double>();
 		}
 
 		@Override
 		public int getColumnCount()
 		{
 			return 7;
 		}
 
 		@Override
 		public int getRowCount()
 		{
 			return transactions.size();
 		}
 
 		@Override
 		public Object getValueAt(int row, int column)
 		{
 			DecimalFormat decimalFormat = new DecimalFormat("0.00");
 			Transaction transaction = transactions.get(row);
 			Account account;
 
 			account = (Account)comboAccount.getSelectedItem();
 
 			switch (column)
 			{
 				case 0:
                     SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
                     return format.format(transaction.getDate());
 
 				case 1:
 					return transaction.getNo();
 
 				case 2:
 					if(transaction.getRemarks() != null)
 					{
 						return transaction.getRemarks().split("\n")[0];
 					}
 
 					return null;
 
 				case 3:
 					return transaction.getCategory().getName();
 
 				case 4:
 					return transaction.getIncome() == 0 ? null : decimalFormat.format(transaction.getIncome()) + " " + account.getCurrency().getName();
 
 				case 5:
 					return transaction.getRebate() == 0 ? null : decimalFormat.format(transaction.getRebate()) + " " + account.getCurrency().getName();
 
 				case 6:
 					return balances.get(row) == 0 ? null : decimalFormat.format(balances.get(row)) + " " + account.getCurrency().getName();
 			}
 
 			return null;
 		}
 
 		public void add(List<Transaction> transactions)
 		{
 			for(Transaction transaction : transactions)
 			{
 				this.transactions.add(transaction);
 				transaction.addEntityListener(this);
 			}
 
 			sort();
 			updateTable();
 		}
 
 		public void add(Transaction transaction)
 		{
 			this.transactions.add(transaction);
 			transaction.addEntityListener(this);
 			sort();
 			updateTable();
 		}
 
 		public Transaction getElementAt(int row)
 		{
 			return transactions.get(row);
 		}
 
 		public void remove(Transaction transaction)
 		{
 			transactions.remove(transaction);
 			updateTable();
 		}
 
 		public void clear()
 		{
 			transactions.clear();
 			updateTable();
 		}
 
 		public void setOpeningBalance(double openingBalance)
 		{
 			this.openingBalance = openingBalance;
 			updateTable();
 		}
 
 		/*
 		 * entity listener:
 		 */
 		@Override
 		public void entityUpdated(EntityEvent event)
 		{
 			sort();
 			updateTable();
 		}
 
 		@Override
 		public void entityDeleted(EntityEvent event)
 		{
 			remove((Transaction)event.getSource());
 		}
 
 		/*
 		 * helpers:
 		 */
 		private void sort()
 		{
 			Collections.sort(transactions, new Comparator<Transaction>()
 			{
 				@Override
 				public int compare(Transaction a, Transaction b)
 				{
 					return a.getDate().compareTo(b.getDate());
 				}
 			});
 		}
 
 		private void updateTable()
 		{
 			double balance = openingBalance;
 			int i;
 
 			// recalculate balances:
 			for(i = balances.size(); i < transactions.size(); ++i)
 			{
 				balances.add(0.0);
 			}
 
 			for(i = 0; i < transactions.size(); ++i)
 			{
 				balance += transactions.get(i).getIncome();
 				balance -= transactions.get(i).getRebate();
 				balances.set(i, balance);
 			}
 
 			// fire event:
 			fireTableDataChanged();
 		}
 	}
 
 	/*
 	 * frame:
 	 */
 	public TransactionFrame()
 	{
 		super(String.format("%s version %d.%d.%d", AppInfo.TITLE, AppInfo.VERSION_MAJOR, AppInfo.VERSION_MINOR, AppInfo.VERSION_PATCHLEVEL));
 	}
 
 	/*
 	 * create window:
 	 */
 	@Override
 	protected void initialize()
 	{
 		BorderLayout borderLayout;
 
 		translation = new Translation();
 
 		setResizable(true);
 		borderLayout = new BorderLayout();
 		borderLayout.setVgap(5);
 		contentPane.setLayout(borderLayout);
 	
 		initializeFactory();
 		initializeMenu();
 		initializeFilterPanel();
 		initializeContentPanel();
 	
 		// maximize window:
 		pack();
 		setExtendedState(JFrame.MAXIMIZED_BOTH);
 
 		// populate data:
 		populateAccounts();
 		populateTimeframeBox();
 	}
 
 	private void initializeFactory()
 	{
 		try
 		{
 			PicoContainer pico = Injection.getContainer();
 			factory = pico.getComponent(Factory.class); 
 		}
 		catch(ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void initializeMenu()
 	{
 		JMenu menu;
 		JMenuItem menuItem;
 
 		menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		menu = new JMenu("File");
 		menuBar.add(menu);
 		menuItem = new JMenuItem("Close");
 		menuItem.setName("File_Close");
 		menuItem.addActionListener(this);
 		menu.add(menuItem);
 	
 		menu = new JMenu("Accounting");
 		menuBar.add(menu);
 		menuItem = new JMenuItem("New transaction");
 		menuItem.setName("Accounting_NewTransaction");
 		menuItem.addActionListener(this);
 		menu.add(menuItem);
 		menuItem = new JMenuItem("Transfer");
 		menuItem.setName("Accounting_Transfer");
 		menuItem.addActionListener(this);
 		menu.add(menuItem);
 	}
 
 	private void initializeFilterPanel()
 	{
 		JPanel panel;
 
 		panel = new JPanel();
 
 		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		contentPane.add(panel, BorderLayout.NORTH);
 	
 		// timeframe:
 		panel.add(new JLabel("Month:"));
 		comboTimeframe = new JComboBox();
 		comboTimeframe.setName("comboTimeframe");
 		panel.add(comboTimeframe);
 		GuiUtil.setPreferredWidth(comboTimeframe, 80);
 		comboTimeframe.addActionListener(this);
 	
 		// account(s):
 		panel.add(new JLabel("Account:"));
 		comboAccount = new JComboBox();
 		comboAccount.setModel(new GenericComboBoxModel<Account>());
 		comboAccount.setName("comboAccount");
 		panel.add(comboAccount);
 		GuiUtil.setPreferredWidth(comboAccount, 200);
 		comboAccount.addActionListener(this);
 
 		buttonAccounts = new JButton("...");
 		buttonAccounts.setName("buttonAccounts");
 		panel.add(buttonAccounts);
 		buttonAccounts.addActionListener(this);
 	}
 
 	private void initializeContentPanel()
 	{
 		JPanel panelContent;
 		JPanel panel;
         TableColumnModel columns;
 
 		// content panel:
 		panelContent = new JPanel();
 		panelContent.setLayout(new BorderLayout());
 		panelContent.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		contentPane.add(panelContent, BorderLayout.CENTER);
 
 		// initial balance:
 		panelOpeningBalance = new JPanel();
 		panelOpeningBalance.setLayout(new FlowLayout(FlowLayout.LEFT));
 		panelOpeningBalance.setBorder(BorderFactory.createCompoundBorder());
 		panelContent.add(panelOpeningBalance, BorderLayout.NORTH);
 		labelOpeningBalance = new JLabel();
 		panelOpeningBalance.add(labelOpeningBalance);
 	
 		// table:
 		JPanel panelTable;
 		panelTable = new JPanel();
 		panelTable.setLayout(new BorderLayout());
 		panelTable.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		panelContent.add(panelTable, BorderLayout.CENTER);
 	
 		tableTransactions = new JTable(new TransactionTableModel());
 		columns = tableTransactions.getColumnModel();
 		columns.getColumn(0).setHeaderValue(translation.translate("Date"));
 		columns.getColumn(1).setHeaderValue(translation.translate("No"));
 		columns.getColumn(2).setHeaderValue(translation.translate("Remarks"));
 		columns.getColumn(3).setHeaderValue(translation.translate("Category"));
 		columns.getColumn(4).setHeaderValue(translation.translate("Income"));
 		columns.getColumn(5).setHeaderValue(translation.translate("Rebate"));
 		columns.getColumn(6).setHeaderValue(translation.translate("Balance"));
 
 		columns.getColumn(0).setMinWidth(150);
 		columns.getColumn(0).setMaxWidth(200);
 		columns.getColumn(1).setMinWidth(50);
 		columns.getColumn(1).setMaxWidth(150);
 
 		for(int i = 4; i < 7; ++i)
 		{
 			columns.getColumn(i).setMinWidth(100);
 			columns.getColumn(i).setMaxWidth(150);
 		}
 
 		tableTransactions.setColumnSelectionAllowed(false);
 		tableTransactions.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		tableTransactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		panelTable.add(new JScrollPane(tableTransactions), BorderLayout.CENTER);
 
 		tableTransactions.registerKeyboardAction(new ActionListener()
 		{	
 			@Override
 			public void actionPerformed(ActionEvent event)
 			{
 				deleteSelectedTransaction();
 			}
 		}, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 		tableTransactions.registerKeyboardAction(new ActionListener()
 		{	
 			@Override
 			public void actionPerformed(ActionEvent event)
 			{
 				editSelectedTransaction();
 			}
 		}, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 		tableTransactions.addMouseListener(this);
 
 		// closing balance:
 		panelClosingBalance = new JPanel();
 		panelClosingBalance.setLayout(new FlowLayout(FlowLayout.LEFT));
 		panelClosingBalance.setBorder(BorderFactory.createCompoundBorder());
 		panelTable.add(panelClosingBalance, BorderLayout.SOUTH);
 		labelClosingBalance = new JLabel();
 		panelClosingBalance.add(labelClosingBalance);
 
 		// action buttons:
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		panelContent.add(panel, BorderLayout.SOUTH);
 	
 		buttonAdd = new JButton("New transaction");
 		buttonAdd.setName("buttonAdd");
 		buttonAdd.addActionListener(this);
 		panel.add(buttonAdd);
 
 		buttonTransfer = new JButton("Transfer");
 		buttonTransfer.setName("buttonTransfer");
 		buttonTransfer.addActionListener(this);
 		panel.add(buttonTransfer);
 		
 		buttonDelete = new JButton("Delete");
 		buttonDelete.setName("buttonDelete");
 		buttonDelete.addActionListener(this);
 		panel.add(buttonDelete);
 	}
 
 	/*
 	 * data population:
 	 */
 	private void populateAccounts()
 	{
 		@SuppressWarnings("unchecked")
 		GenericComboBoxModel<Account> model = (GenericComboBoxModel<Account>)comboAccount.getModel();
 
 		try
 		{
 			model.clear();
 			model.add(factory.getAccounts());
 			model.sort();
 			model.selectFirst();
 		}
 		catch(ProviderException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void populateTimeframeBox()
 	{
         DefaultComboBoxModel model;
 		Date date;
 		String dateString;
 		SimpleDateFormat format;
 		LinkedList<String> dates;
 
 		if(comboAccount.getSelectedItem() == null)
 		{
 			return;
 		}
 
 		model = (DefaultComboBoxModel)comboTimeframe.getModel();
 		model.removeAllElements();
 
 		format = new SimpleDateFormat("yyyy/MM");
 		dates = new LinkedList<String>();
 		date = new Date(0);
 
 		dates.add(format.format((Calendar.getInstance().getTime())));
 
 		try
 		{
 			for(long timestamp : ((Account)comboAccount.getSelectedItem()).getTimestamps())
 			{
 				date.setTime(timestamp);
 				dateString = format.format(date);
 
 				if(!dates.contains(dateString))
 				{
 					dates.add(dateString);
 				}
 			}
 		}
 		catch(ProviderException e)
 		{
 			e.printStackTrace();
 		}
 
 		for(String foundDate : dates)
 		{
 			model.addElement(foundDate);
 		}
 	}
 
 	private void populateTransactions()
 	{
 		String parts[];
 		Calendar calendar;
 		Date begin;
 		Date end;
 		TransactionTableModel model;
 		Account account;
 		double openingBalance;
 
 		if(comboTimeframe.getSelectedItem() == null)
 		{
 			return;
 		}
 
 		parts = comboTimeframe.getSelectedItem().toString().split("/", 2);
 
 		calendar = Calendar.getInstance();
 		calendar.set(Calendar.YEAR, Integer.parseInt(parts[0], 10));
 		calendar.set(Calendar.MONTH, Integer.parseInt(parts[1], 10) - 1);
 		calendar.set(Calendar.DAY_OF_MONTH, 1);
 		calendar.set(Calendar.HOUR, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 
 		begin = new Date(calendar.getTime().getTime());
 		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
 		end = new Date(calendar.getTime().getTime());
 
 		try
 		{
 			model = (TransactionTableModel)tableTransactions.getModel();
 			account = (Account)comboAccount.getSelectedItem();
 			openingBalance = account.getOpeningBalance(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
 			showOpeningBalance(openingBalance);
 			showClosingBalance(account.getClosingBalance(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)));
 			model.clear();
 			model.setOpeningBalance(openingBalance);
 			model.add(account.getTransactions(begin, end));
 		}
 		catch(ProviderException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * delete transaction:
 	 */
 	private void deleteSelectedTransaction()
 	{
 		TransactionTableModel model;
 		Transaction transaction;
 		Calendar calendar;
 
 		if(tableTransactions.getSelectedRow() == -1)
 		{
 			return;
 		}
 
 		if(JOptionPane.showConfirmDialog(this, translation.translate("Do you really want to delete the selected transaction?")) == JOptionPane.YES_OPTION)
 		{
 			model = (TransactionTableModel)tableTransactions.getModel();
 			transaction = model.getElementAt(tableTransactions.getSelectedRow());
 	
 			try
 			{
 				transaction.delete();
 				model.remove(transaction);
 				calendar = Calendar.getInstance();
 				calendar.setTime(transaction.getDate());
 				showClosingBalance(((Account)comboAccount.getSelectedItem()).getClosingBalance(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)));
 			}
 			catch(Exception e)
 			{
 				JOptionPane.showMessageDialog(this, "Couldn't delete transaction.", "Warning", JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 			}
 	
 			if(tableTransactions.getRowCount() == 0)
 			{
 				populateTimeframeBox();
 	
 				if(comboTimeframe.getModel().getSize() > 0)
 				{
 					comboTimeframe.setSelectedIndex(0);
 				}
 			}
 		}
 	}
 
 	/*
 	 * add transaction:
 	 */
 	private void addTransaction()
 	{
 		Account account;
 		Transaction transaction;
 		TransactionDialog dialog;
 		TransactionTableModel model;
 		
 		account = (Account)comboAccount.getSelectedItem();
 		model = (TransactionTableModel)tableTransactions.getModel();
 
 		dialog = new TransactionDialog(this, translation.translate("New transaction"), account);
 		dialog.addCategoryListener(this);
 		dialog.open();
 
 		if(dialog.getResult() == TransactionDialog.RESULT_APPLY)
 		{
 			transaction = dialog.getTransaction();
 			model.add(transaction);
 			updateTransaction(transaction);
 		}
 	}
 
 	/*
 	 * transfer money:
 	 */
 	private void transfer()
 	{
 		TransferDialog dialog;
 		Transaction transaction;
 
 		dialog = new TransferDialog(this, (Account)comboAccount.getSelectedItem());
 		dialog.open();
 
 		if(dialog.getResult() == TransactionDialog.RESULT_APPLY)
 		{
 			transaction = dialog.getFromTransaction();
 			((TransactionTableModel)tableTransactions.getModel()).add(transaction);
 			updateTransaction(transaction);
 		}
 	}
 
 	/*
 	 * balances:
 	 */
 	private void showOpeningBalance(double balance)
 	{
 		setBalance(labelOpeningBalance, balance);
 	}
 
 	private void showClosingBalance(double balance)
 	{
 		setBalance(labelClosingBalance, balance);
 	}
 
 	private void setBalance(JLabel label, double value)
 	{
 		DecimalFormat decimalFormat = new DecimalFormat("0.00");
 
 		label.setText(decimalFormat.format(value) + " " + ((Account)comboAccount.getSelectedItem()).getCurrency().toString());
 
 		if(value == 0)
 		{
 			label.getParent().setBackground(Color.YELLOW);
 		}
 		else if(value < 0)
 		{
 			label.getParent().setBackground(Color.RED);
 		}
 		else
 		{
 			label.getParent().setBackground(Color.GREEN);
 		}
 	}
 
 	private void changeBalanceCurrency(Currency currency)
 	{
 		labelOpeningBalance.setText(labelOpeningBalance.getText().split(" ")[0] + " " + currency);
 		labelClosingBalance.setText(labelClosingBalance.getText().split(" ")[0] + " " + currency);
 	}
 	
 	/*
 	 * edit transaction:
 	 */
 	private void editSelectedTransaction()
 	{
 		TransactionTableModel model = (TransactionTableModel)tableTransactions.getModel();
 		Transaction transaction;
 		TransactionDialog dialog;
 
 		transaction = model.getElementAt(tableTransactions.getSelectedRow());
 		dialog = new TransactionDialog(this, translation.translate("Edit transaction"), transaction);
 		dialog.addCategoryListener(this);
 		dialog.open();
 
 		if(dialog.getResult() == TransactionDialog.RESULT_APPLY)
 		{
 			updateTransaction(transaction);
 		}
 	}
 
 	/*
 	 * edit accounts:
 	 */
 	private void editAccounts()
 	{
 		AccountDialog dialog = new AccountDialog(this, (Account)comboAccount.getSelectedItem());
 
 		dialog.addAccountListener(this);
 		dialog.addCurrencyListener(this);
 		dialog.open();
 
 		if(dialog.getResult() == AccountDialog.RESULT_APPLY)
 		{
 			comboAccount.setSelectedItem(dialog.getSelectedAccount());
 		}
 	}	
 
 	/*
 	 * helpers:
 	 */
 	private void selectTransaction(Transaction transaction)
 	{
 		TransactionTableModel model = (TransactionTableModel)tableTransactions.getModel();
 
 		for(int i = 0; i < model.getRowCount(); ++i)
 		{
 			if(model.getElementAt(i).getId() == transaction.getId())
 			{
 				tableTransactions.getSelectionModel().setSelectionInterval(i, i);
 				break;
 			}
 		}
 	}
 
 	private void updateTransaction(Transaction transaction)
 	{
 		SimpleDateFormat dateFormat;
 		String dateString;
 		DefaultComboBoxModel comboModel;
 		Calendar calendar;
 
 		// check if month has been changed:
 		dateFormat = new SimpleDateFormat("yyyy/MM");
 		dateString = dateFormat.format(transaction.getDate());
 
 		if(!dateString.equals(comboTimeframe.getSelectedItem().toString()))
 		{
 			// month has been changed => rebuild timeframe box
 			populateTimeframeBox();
 
 			// select specified month:
 			comboModel = (DefaultComboBoxModel)comboTimeframe.getModel();
 
 			for(int i = 0; i < comboModel.getSize(); ++i)
 			{
 				if(comboModel.getElementAt(i).toString().equals(dateString))
 				{
 					comboModel.setSelectedItem(comboModel.getElementAt(i));
 					break;
 				}
 			}
 		}
 
 		// select transaction:
 		selectTransaction(transaction);
 
 		// update closing balance:
 		calendar = Calendar.getInstance();
 		calendar.setTime(transaction.getDate());
 		
 		try
 		{
 			showClosingBalance(((Account)comboAccount.getSelectedItem()).getClosingBalance(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)));
 		}
 		catch(ProviderException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * action listener:
 	 */
 	@Override
 	public void actionPerformed(ActionEvent event)
 	{
         if(event.getSource() instanceof JMenuItem || event.getSource() instanceof JButton)
         {
         	if(((JComponent)event.getSource()).getName().equals("File_Close"))
             {
                 close();
             }
             else if(((JComponent)event.getSource()).getName().equals("Accounting_NewTransaction") || event.getSource().equals(buttonAdd))
             {
                 addTransaction();
             }
             else if(((JComponent)event.getSource()).getName().equals("Accounting_Transfer") || event.getSource().equals(buttonTransfer))
             {
             	transfer();
             }
             else if(event.getSource().equals(buttonDelete))
             {
             	deleteSelectedTransaction();
             }
         	else if(event.getSource().equals(buttonAccounts))
         	{
         		editAccounts();
         	}
         }
         else if(event.getSource().equals(comboAccount))
     	{
     		populateTimeframeBox();
     	}
     	else if(event.getSource().equals(comboTimeframe))
     	{
     		populateTransactions();
         }
 	}
 	
 	/*
 	 * mouselistener:
 	 */
 	@Override
 	public void mouseClicked(MouseEvent event) 
 	{
 		if(event.getClickCount() == 2 && event.getSource().equals(tableTransactions))
 		{
 			editSelectedTransaction();
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent event) { }
 
 	@Override
 	public void mouseExited(MouseEvent event) { }
 
 	@Override
 	public void mousePressed(MouseEvent event) { }
 
 	@Override
 	public void mouseReleased(MouseEvent event) { }
 
 	/*
 	 * category listener:
 	 */
 	@Override
 	public void categoryChanged(EntityEvent event)
 	{
 		TransactionTableModel model = (TransactionTableModel)tableTransactions.getModel();
 		Transaction transaction;
 		Category source = (Category)event.getSource();
 
 		for(int i = 0; i < model.getRowCount(); ++i)
 		{
 			transaction = model.getElementAt(i);
 
 			if(transaction.getCategory().getId() == source.getId())
 			{
 				try
 				{
 					transaction.setCategory(source);
 				}
 				catch(AttributeException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 
 		model.fireTableDataChanged();
 	}
 
 	/*
 	 * account listener:
 	 */
 	@Override
 	public void accountAdded(EntityEvent event)
 	{
 		@SuppressWarnings("unchecked")
 		GenericComboBoxModel<Account> model = (GenericComboBoxModel<Account>)comboAccount.getModel();
 
 		model.add((Account)event.getSource());
 		model.sort();
 	}
 
 	@Override
 	public void accountDeleted(EntityEvent event)
 	{
 		@SuppressWarnings("unchecked")
 		GenericComboBoxModel<Account> model = ((GenericComboBoxModel<Account>)comboAccount.getModel());
 		Account source = (Account)event.getSource();
 
 		model.remove(source);
 
 		if(((Account)(model.getSelectedItem())).getId() == source.getId())
 		{
 			model.selectFirst();
 		}
 	}
 
 	@Override
 	public void accountChanged(EntityEvent event)
 	{
 		@SuppressWarnings("unchecked")
 		GenericListModel<Account> model = (GenericListModel<Account>)comboAccount.getModel();
 		Account account;
 		Account source = (Account)event.getSource();
 
 		for(int i = 0; i < model.getSize(); ++i)
 		{
 			account = (Account)model.getElementAt(i);
 
 			if(account.getId() == source.getId())
 			{
 				try
 				{
 					account.setName(source.getName());
 					account.setRemarks(source.getRemarks());
 					account.setCurrency(source.getCurrency());
 					model.sort();
 					((TransactionTableModel)tableTransactions.getModel()).fireTableDataChanged();
 					changeBalanceCurrency(account.getCurrency());
 				}
 				catch(AttributeException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/*
 	 * currency listener:
 	 */
 	@Override
 	public void currencyChanged(EntityEvent event)
 	{
 		TransactionTableModel model = (TransactionTableModel)tableTransactions.getModel();
 		Account account = (Account)comboAccount.getSelectedItem();
 		Currency source = (Currency)event.getSource();
 
 		if(account.getCurrency().getId() == source.getId())
 		{
 			try
 			{
 				account.setCurrency(source);
 				model.fireTableDataChanged();
 				changeBalanceCurrency(source);
 			}
 			catch(AttributeException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void currencyDeleted(EntityEvent event) { }
 
 	@Override
 	public void currencyAdded(EntityEvent event) { }
 }
