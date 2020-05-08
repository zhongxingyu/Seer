 /***************************************************************************
     begin........: February 2012
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
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 import accounting.*;
 import accounting.application.*;
 import accounting.data.*;
 
 public class AccountDialog extends ADialog implements ActionListener, ListSelectionListener, ICurrencyListener
 {
 	public static final int RESULT_DELETE = -1;
 	public static final int RESULT_APPLY = 1;
 	public static final int RESULT_CLOSE = 2;
 
 	private static final long serialVersionUID = -3435461943944120358L;
 	private Factory factory;
 	private JButton buttonApply;
 	private JButton buttonClose;
 	private JTable tableAccounts;
 	private JButton buttonAdd;
 	private JButton buttonDelete;
 	private JTextField textName;
 	private JComboBox comboCurrency;
 	private JButton buttonCurrency;
 	private JTextField textNoPrefix;
 	private JSpinner spinnerCurrentNo;
 	private JTextArea areaRemarks;
 	private Container contentPane;
 	private int result;
 	private Vector<IAccountListener> accountListener = new Vector<IAccountListener>();
 	private Vector<ICurrencyListener> currencyListener = new Vector<ICurrencyListener>();
 	private Account lastSelectedAccount;
 	private Translation translation;
 
 	/*
 	 * table model:
 	 */
 	private class AccountsTableModel extends AbstractTableModel implements IEntityListener
 	{
 		private static final long serialVersionUID = -8060379832392914459L;
 		private Vector<Account> accounts = new Vector<Account>();
 
 		@Override
 		public int getColumnCount()
 		{
 			return 1;
 		}
 
 		@Override
 		public int getRowCount()
 		{
 			return accounts.size();
 		}
 
 		@Override
 		public Object getValueAt(int row, int column)
 		{
 			if(column == 0)
 			{
 				return accounts.get(row);
 			}
 
 			return null;
 		}
 
 		public void add(List<Account> accounts)
 		{
 			for(Account account : accounts)
 			{
 				this.accounts.add(account);
 				account.addEntityListener(this);
 			}
 
 			updateTable();
 		}
 
 		public void add(Account account)
 		{
 			this.accounts.add(account);
 			account.addEntityListener(this);
 			updateTable();
 		}
 
 		public void remove(Account account)
 		{
 			int index;
 
 			index = accounts.indexOf(account) - 1;
 
 			if(index < 0)
 			{
 				index = 0;
 			}
 		
 			accounts.remove(account);
 			updateTable();
 			selectAccount(getElementAt(index));
 		}
 
 		public Account getElementAt(int index)
 		{
 			return accounts.get(index);
 		}
 
 		private void sort()
 		{
 			Collections.sort(accounts);
 		}
 
 		private void updateTable()
 		{
 			fireTableDataChanged();
 		}
 
 		@Override
 		public void entityUpdated(EntityEvent event) { }
 
 		@Override
 		public void entityDeleted(EntityEvent event)
 		{
 			remove((Account)event.getSource());
 		}
 	}
 
 	/*
 	 * table selection model:
 	 */
 	private class AccountsSelectionModel extends DefaultListSelectionModel
 	{
 		private static final long serialVersionUID = -6267681042135020421L;
 
 		@Override
 		public void setSelectionInterval(int from, int to)
 		{
 			AccountsTableModel model = (AccountsTableModel)tableAccounts.getModel();
 			Account account;
 			boolean update = true;
 
 			if(checkEdited(lastSelectedAccount))
 			{
 				if(!applyChanges(lastSelectedAccount, true))
 				{
 					JOptionPane.showMessageDialog(null, "Couldn't update account, please try again.", "Warning", JOptionPane.ERROR_MESSAGE);
 					update = false;
 				}
 			}
 
 			if(update)
 			{
 				account = model.getElementAt(from);
 				model.sort();
 				
 				for(int i = 0; i < model.getRowCount(); ++i)
 				{
 					if(model.getElementAt(i).getId() == account.getId())
 					{
 						from = to = i;
 						break;
 					}
 				}
 
 				super.setSelectionInterval(from, to);
 			}
 			else
 			{
 				((AccountsTableModel)tableAccounts.getModel()).sort();
 			}
 		}
 	}
 
 	/*
 	 * dialog:
 	 */
 	public AccountDialog(JFrame parent, Account preSelected)
 	{
 		super(parent, "Accounts");
 
 		try
 		{
 			factory = Injection.getContainer().getComponent(Factory.class);
 		}
 		catch (ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 
 		populateCurrencies();
 		populateAccounts();
 
 		if(preSelected == null)
 		{
 			selectAccount(((AccountsTableModel)tableAccounts.getModel()).getElementAt(0));
 		}
 		else
 		{
 			selectAccount(preSelected);
 		}
 	}
 
 	@Override
 	protected void initialize()
 	{
 		BorderLayout borderLayout;
 		JPanel panelList;
 		JPanel panelContent;
 		JPanel panel;
 		JLabel label;
 		JLabel labelName;
 		JLabel labelCurrency;
 		JLabel labelFormat;
 		TableColumnModel columns;
 
 		translation = new Translation();
 		setTitle(translation.translate(getTitle()));
 
 		contentPane = getContentPane();
 
 		addWindowListener(this);
 
 		setResizable(false);
 
 		borderLayout = new BorderLayout();
 		borderLayout.setVgap(5);
 		contentPane.setLayout(borderLayout);
 		
 		// list panel:
 		panelList = new JPanel();
 		borderLayout = new BorderLayout();
 		borderLayout.setVgap(5);
 		panelList.setLayout(borderLayout);
 		panelList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		contentPane.add(panelList, BorderLayout.WEST);		
 		
 		tableAccounts = new JTable(new AccountsTableModel());
 		tableAccounts.setName("tableAccounts");
 		tableAccounts.setShowGrid(false);
 		tableAccounts.setSelectionModel(new AccountsSelectionModel());
 		tableAccounts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		columns = tableAccounts.getColumnModel();
 		columns.getColumn(0).setHeaderValue("Account");
 		tableAccounts.getSelectionModel().addListSelectionListener(this);
 		tableAccounts.registerKeyboardAction(new ActionListener()
 		{	
 			@Override
 			public void actionPerformed(ActionEvent event)
 			{
 				deleteSelectedAccount();
 			}
 		}, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 		panelList.add(tableAccounts);
 
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		panelList.add(panel, BorderLayout.SOUTH);
 		
 		buttonAdd = new JButton("Add");
 		buttonAdd.setName("buttonAdd");
 		buttonAdd.addActionListener(this);
 		panel.add(buttonAdd);
 		
 		buttonDelete = new JButton("Delete");
 		buttonDelete.setName("buttonDelete");
 		buttonDelete.addActionListener(this);
 		panel.add(buttonDelete);
 		
 		// content panel:
 		panelContent = new JPanel();
 		panelContent.setLayout(new BoxLayout(panelContent, BoxLayout.Y_AXIS));
 		panelContent.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		contentPane.add(panelContent, BorderLayout.CENTER);
 
 		// name:
 		panelContent.add(Box.createHorizontalBox());
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		panelContent.add(panel);
 
 		labelName = new JLabel("Name:");
 		panel.add(labelName);
 		textName = new JTextField();
 		textName.setName("textName");
 		panel.add(textName);
 
 		// currency:
 		panelContent.add(Box.createHorizontalBox());
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		panelContent.add(panel);
 
 		labelCurrency = new JLabel("Currency:");
 		panel.add(labelCurrency);
 		comboCurrency = new JComboBox();
 		comboCurrency.setName("comboCurrency");
 		comboCurrency.setModel(new GenericComboBoxModel<Currency>());
 		panel.add(comboCurrency);
 
 		buttonCurrency = new JButton("...");
 		buttonCurrency.setName("buttonCurrency");
 		buttonCurrency.addActionListener(this);
 		panel.add(buttonCurrency);
 
 		// no format:
 		panelContent.add(Box.createHorizontalBox());
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		panelContent.add(panel);
 
 		labelFormat = new JLabel("No. prefix:");
 		panel.add(labelFormat);
 		textNoPrefix = new JTextField();
 		textNoPrefix.setName("textName");
 		GuiUtil.setPreferredWidth(textNoPrefix, 80);
 		panel.add(textNoPrefix);
 		labelFormat = new JLabel("Current No.:");
 		panel.add(labelFormat);
 		spinnerCurrentNo = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 10));
 		spinnerCurrentNo.setEditor(new JSpinner.NumberEditor(spinnerCurrentNo, "0"));
 		spinnerCurrentNo.setName("spinnerCurrentNo");
 		panel.add(spinnerCurrentNo);
 		
 		// remarks:
 		panelContent.add(Box.createHorizontalBox());
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		
 		panelContent.add(panel); 
 		panel.add(new JLabel("Remarks:"));
 		
 		panelContent.add(Box.createHorizontalBox());
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		
 		panelContent.add(panel); 
 		areaRemarks = new JTextArea(10, 30);
 		panel.add(new JScrollPane(areaRemarks));
 		
 		// update preferred sizes:
 		label = GuiUtil.getPreferredWidth(labelName) > GuiUtil.getPreferredWidth(labelCurrency) ? labelName : labelCurrency;
 		GuiUtil.setPreferredWidth(labelName, GuiUtil.getPreferredWidth(label));
 		GuiUtil.setPreferredWidth(labelCurrency, GuiUtil.getPreferredWidth(label));
 		GuiUtil.setPreferredWidth(textName, GuiUtil.getPreferredWidth(areaRemarks) - GuiUtil.getPreferredWidth(label) - 1);
 		GuiUtil.setPreferredWidth(comboCurrency, GuiUtil.getPreferredWidth(areaRemarks) - GuiUtil.getPreferredWidth(label) - 1);
 		
 		// button panel:
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		contentPane.add(panel, BorderLayout.SOUTH);
 		
 		buttonApply = new JButton("Apply");
 		buttonApply.setName("buttonApply");
 		buttonApply.addActionListener(this);
 		panel.add(buttonApply);
 		
 		buttonClose = new JButton("Close");
 		buttonClose.setName("buttonClose");
 		buttonClose.addActionListener(this);
 		panel.add(buttonClose);
 	}
 
 	public int getResult()
 	{
 		return result;
 	}
 
 	public void addAccountListener(IAccountListener listener)
 	{
 		accountListener.add(listener);
 	}
 
 	public void removeAccountListener(IAccountListener listener)
 	{
 		accountListener.remove(listener);
 	}
 
 	public void addCurrencyListener(ICurrencyListener listener)
 	{
 		currencyListener.add(listener);
 	}
 
 	public void removeCurrencyListener(ICurrencyListener listener)
 	{
 		currencyListener.remove(listener);
 	}
 
 	public Account getSelectedAccount()
 	{
 		AccountsTableModel model = (AccountsTableModel)tableAccounts.getModel();
 
 		if(tableAccounts.getSelectedRow() != -1)
 		{
 			return model.getElementAt(tableAccounts.getSelectedRow());
 		}
 
 		return null;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event)
 	{
 		if(event.getSource().equals(buttonClose))
 		{
 			result = RESULT_CLOSE;
 			close();
 		}
 		else if(event.getSource().equals(buttonApply))
 		{
 			applyChanges(getSelectedAccount(), false);
 			result = RESULT_APPLY;
 			close();
 		}
 		else if(event.getSource().equals(buttonAdd))
 		{
 			addAccount();
 		}
 		else if(event.getSource().equals(buttonDelete))
 		{
 			deleteSelectedAccount();
 		}
 		else if(event.getSource().equals(buttonCurrency))
 		{
 			editCurrencies();
 		}
 	}
 
 	@Override
 	public void windowClosing(WindowEvent event)
 	{
 		applyChanges(getSelectedAccount(), true);
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent event)
 	{
 		lastSelectedAccount = getSelectedAccount();
 		populateSelectedAccount();
 	}
 
 	@Override
 	public void currencyChanged(EntityEvent event)
 	{
 		@SuppressWarnings("unchecked")
 		GenericListModel<Currency> model = (GenericListModel<Currency>)comboCurrency.getModel();
 		Currency currency;
 		Currency source = (Currency)event.getSource();
 
 		comboCurrency.removeActionListener(this);
 
 		for(int i = 0; i < model.getSize(); ++i)
 		{
 			currency = (Currency)model.getElementAt(i);
 
 			if(currency.getId() == source.getId())
 			{
 				try
 				{
 					currency.setName(source.getName());
 				}
 				catch(AttributeException e)
 				{
 					e.printStackTrace();
 				}
 
 				model.sort();
 				break;
 			}
 		}
 
 		comboCurrency.addActionListener(this);
 	}
 
 	@Override
 	public void currencyDeleted(EntityEvent event)
 	{
 		@SuppressWarnings("unchecked")
 		GenericListModel<Currency> model = (GenericListModel<Currency>)comboCurrency.getModel();
 		
 		comboCurrency.removeActionListener(this);
 		model.remove((Currency)event.getSource());
 		comboCurrency.addActionListener(this);
 	}
 
 	@Override
 	public void currencyAdded(EntityEvent event)
 	{
 		@SuppressWarnings("unchecked")
 		GenericListModel<Currency> model = (GenericListModel<Currency>)comboCurrency.getModel();
 
 		comboCurrency.removeActionListener(this);
 		model.add((Currency)event.getSource());
 		model.sort();
 		comboCurrency.addActionListener(this);
 	}
 
 	private void selectAccount(Account account)
 	{
 		AccountsTableModel model = (AccountsTableModel)tableAccounts.getModel();
 
 		for(int i = 0; i < tableAccounts.getModel().getRowCount(); ++i)
 		{
 			if(model.getElementAt(i).getId() == account.getId())
 			{
 				if(i != tableAccounts.getSelectedRow())
 				{
 					tableAccounts.getSelectionModel().setSelectionInterval(i, i);
 				}
 				break;
 			}
 		}
 	}
 
 	private boolean checkEdited(Account account)
 	{
 		if(account == null)
 		{
 			return false;
 		}
 
 		if(!account.getName().equals(textName.getText()))
 		{
 			return true;
 		}
 
 		if(!account.getRemarks().equals(areaRemarks.getText()))
 		{
 			return true;
 		}
 
 		if(account.getCurrency().getId() != ((Currency)comboCurrency.getSelectedItem()).getId())
 		{
 			return true;
 		}
 
 		if(!(textNoPrefix.getText().isEmpty() && account.getNoPrefix() == null) && !textNoPrefix.getText().equals(account.getNoPrefix()))
 		{
 			return true;
 		}
 		
 		if(account.getCurrentNo() != (Integer)spinnerCurrentNo.getModel().getValue())
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 	private boolean applyChanges(Account account, boolean showMessage)
 	{
 		String message = null;
 
 		if(!checkEdited(account))
 		{
 			return true;
 		}
 
 		if(showMessage && JOptionPane.showConfirmDialog(this, translation.translate("Do you want to save your changes?"), translation.translate("Save changes"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
 		{
 			return true;
 		}
 
 		try
 		{
 			account.setName(textName.getText());
 		}
 		catch(AttributeException e)
 		{
 			message = "The entered name is invalid, please check your data.";
 		}
 
 		try
 		{
 			account.setNoPrefix(textNoPrefix.getText());
 		}
 		catch(AttributeException e)
 		{
 			message = "The entered prefix is invalid, please check your data.";
 		}
 		
 		try
 		{
 			account.setCurrentNo((Integer)spinnerCurrentNo.getModel().getValue());
 		}
 		catch(AttributeException e)
 		{
 			message = "The entered prefix length is invalid, please check your data.";
 		}
 		
 		if(message == null)
 		{
 			try
 			{
 				account.setRemarks(areaRemarks.getText());
 			}
 			catch(AttributeException e)
 			{
 				message = "The entered remarks are invalid, please check your data.";
 			}
 		}
 
 		if(message == null)
 		{
 			try
 			{
 				account.setCurrency((Currency)comboCurrency.getSelectedItem());
 			}
 			catch(AttributeException e)
 			{
 				message = "The selected currency is invalid, please check your data.";
 			}
 		}
 
 		if(message == null)
 		{
 			try
 			{
 				account.save();
 
 				for(IAccountListener listener : accountListener)
 				{
 					listener.accountChanged(new EntityEvent(account));
 				}
 			}
 			catch(Exception e)
 			{
 				message = "Couldn't save account, an internal error occured. Please try again later.";
 				e.printStackTrace();
 			}
 		}
 
 		if(message != null)
 		{
 			JOptionPane.showMessageDialog(this, translation.translate(message), translation.translate("Warning"), JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private void populateSelectedAccount()
 	{
 		Account account;
 
 		if((account = getSelectedAccount()) != null)
 		{
 			textName.setText(account.getName());
 			areaRemarks.setText(account.getRemarks());
 			textNoPrefix.setText(account.getNoPrefix());
 			spinnerCurrentNo.setValue(account.getCurrentNo());
 			comboCurrency.removeActionListener(this);
 			comboCurrency.getModel().setSelectedItem(account.getCurrency());
 			comboCurrency.addActionListener(this);
 		}
 	}
 	
 	private void populateCurrencies()
 	{
 		@SuppressWarnings("unchecked")
 		GenericListModel<Currency> model = (GenericListModel<Currency>)comboCurrency.getModel();
 
 		try
 		{
 			model.add(factory.getCurrencies());
 		}
 		catch(ProviderException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void populateAccounts()
 	{
 		AccountsTableModel model = (AccountsTableModel)tableAccounts.getModel();
 
 		try
 		{
 			model.add(factory.getAccounts());
 			model.sort();
 		}
 		catch(ProviderException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void addAccount()
 	{
 		String name;
 		Account account;
 		AccountsTableModel model = (AccountsTableModel)tableAccounts.getModel();
 
 		name = JOptionPane.showInputDialog(this, translation.translate("Please enter a name for your new account:"), translation.translate("new account"));
 
 		if(name != null && !name.isEmpty())
 		{
 			try
 			{
				account = factory.createAccount(name, "", (Currency)comboCurrency.getModel().getElementAt(0), textNoPrefix.getText(), (Integer)spinnerCurrentNo.getModel().getValue());
 				model.add(account);
 				model.sort();
 				selectAccount(account);
 
 				for(IAccountListener listener : accountListener)
 				{
 					listener.accountAdded(new EntityEvent(account));
 				}
 			}
 			catch (ProviderException e)
 			{
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(this, translation.translate("Couldn't create account, please try again."), translation.translate("Warning"), JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 
 	private void deleteSelectedAccount()
 	{
 		Account account;
 
 		if((account = getSelectedAccount()) == null)
 		{
 			return;
 		}
 
 		if(tableAccounts.getRowCount() == 1)
 		{
 			JOptionPane.showMessageDialog(this, translation.translate("You are not allowed to delete the last existing account."), translation.translate("Warning"), JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		try
 		{
 			if(JOptionPane.showConfirmDialog(this, translation.translate("Do you really want to delete the selected account and all its related data?"), translation.translate("Delete account"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
 			{
 				account.delete();
 
 				for(IAccountListener listener : accountListener)
 				{
 					listener.accountDeleted(new EntityEvent(account));
 				}
 			}
 		}
 		catch (ProviderException e)
 		{
 			JOptionPane.showMessageDialog(this, translation.translate("Couldn't delete account, an internal error occured."), translation.translate("Warning"), JOptionPane.ERROR_MESSAGE);	
 		}
 		catch (ReferenceException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void editCurrencies()
 	{
 		CurrencyDialog dialog;
 
 		dialog = new CurrencyDialog(null, (Currency)comboCurrency.getSelectedItem());
 		dialog.addCurrencyListener(this);
 
 		for(ICurrencyListener listener : currencyListener)
 		{
 			dialog.addCurrencyListener(listener);
 		}
 		
 		dialog.open();
 
 		if(dialog.getResult() == CurrencyDialog.RESULT_OK)
 		{
 			comboCurrency.getModel().setSelectedItem(dialog.getSelectedCurrency());
 		}
 	}
 }
