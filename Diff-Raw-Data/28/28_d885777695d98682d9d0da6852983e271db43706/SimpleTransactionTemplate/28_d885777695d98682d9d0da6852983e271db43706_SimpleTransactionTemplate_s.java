 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2006 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.shoebox.templates;
 
 import java.util.Collection;
 import java.util.Date;
 
 import net.sf.jmoney.ITransactionTemplate;
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.AccountComposite;
 import net.sf.jmoney.fields.AccountControlUsingTextbox;
 import net.sf.jmoney.fields.AccountControlWithMruList;
 import net.sf.jmoney.fields.DateComposite;
 import net.sf.jmoney.fields.DateControl;
 import net.sf.jmoney.fields.DateControlAlwaysExpanded;
 import net.sf.jmoney.fields.TextComposite;
 import net.sf.jmoney.fields.TextControlWithMruList;
 import net.sf.jmoney.fields.TextControlWithSimpleTextbox;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.BankAccount;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.IObjectKey;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.Transaction;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.ui.IMemento;
 
 /**
  * The base class for both the income transaction template
  * and the expense transaction template.  These two templates
  * allow entry of any simple transaction (a transaction that
  * is neither a transfer nor a split transaction), and that does
  * not prompt for any fields added by plug-ins.
  *   
  * @author Nigel Westbury
  *
  */
 public abstract class SimpleTransactionTemplate implements ITransactionTemplate {
 	boolean isIncome;
 	
 	/**
 	 * The account into which this transaction is to be placed,
 	 * or null if the user is to be prompted for the account
 	 */
 	Account account;
 	
 	DateComposite dateControl;
 	AccountComposite<BankAccount> accountControl;
 	AccountComposite<IncomeExpenseAccount> categoryControl;
 	TextComposite memoControl;
 	TextComposite numberControl;
 	TextComposite descriptionControl;
 	TextComposite amountControl;
     
     Session session;
     
     public SimpleTransactionTemplate(boolean isIncome) {
 		this.isIncome = isIncome;
		
		session = JMoneyPlugin.getDefault().getSession();
 	}
 
 	public String getDescription() {
 		return isIncome ? "Income" : "Expense";
 	}
 
 	public boolean isApplicable(Account account) {
 		return true;
 	}
 	
	public Control createControl(Composite parent, boolean expandedControls, Account account, Collection<IObjectKey> ourEntryList) {
 		this.account = account;
 		
 		Composite areaComposite = new Composite(parent, SWT.NULL);
 		areaComposite.setLayout(new GridLayout(1, false));
 		
 		// Create the edit controls
 		GridData gdEditArea = new GridData();
 		gdEditArea.horizontalAlignment = SWT.FILL;
 		if (expandedControls) {
 			createExpandedEditArea(areaComposite).setLayoutData(gdEditArea);
 		} else {
 			createCompressedEditArea(areaComposite).setLayoutData(gdEditArea);
 		}
 		
 		// The button area
 		createButtonArea(areaComposite, ourEntryList);
 		
 		return areaComposite;
 	}
 
 	private Control createCompressedEditArea(Composite parent) {
 		Composite areaComposite = new Composite(parent, SWT.NULL);
 		areaComposite.setLayout(new GridLayout(4, false));
 
 		if (account == null) {
 			new Label(areaComposite, 0).setText("Bank Account:");
 			accountControl = new AccountControlUsingTextbox<BankAccount>(areaComposite, session, BankAccount.class);
 			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
 			gd.horizontalSpan = 3;
 			accountControl.setLayoutData(gd);
 		}
 		
 		new Label(areaComposite, 0).setText("Payee:");
 		memoControl = new TextControlWithSimpleTextbox(areaComposite, SWT.NONE);
         GridData gdPayee = new GridData(SWT.FILL, SWT.FILL, true, false);
         gdPayee.widthHint = 200;
         memoControl.setLayoutData(gdPayee);
 
 		new Label(areaComposite, 0).setText("Number:");
 		numberControl = new TextControlWithSimpleTextbox(areaComposite, SWT.NONE);
 		numberControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 
 		new Label(areaComposite, 0).setText("Category:");
         categoryControl = new AccountControlUsingTextbox<IncomeExpenseAccount>(areaComposite, session, IncomeExpenseAccount.class);
         GridData gdCategory = new GridData(SWT.FILL, SWT.FILL, true, false);
         gdCategory.widthHint = 200;
         categoryControl.setLayoutData(gdCategory);
 
 		new Label(areaComposite, 0).setText("Date:");
 		dateControl = new DateControl(areaComposite);
 	
 		new Label(areaComposite, 0).setText("Description:");
 		descriptionControl = new TextControlWithSimpleTextbox(areaComposite, SWT.NONE);
         GridData gdDesc = new GridData(SWT.FILL, SWT.FILL, true, false);
         gdDesc.widthHint = 200;
         descriptionControl.setLayoutData(gdDesc);
 
 		new Label(areaComposite, 0).setText("Amount:");
 		amountControl = new TextControlWithSimpleTextbox(areaComposite, SWT.NONE);
 		amountControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 
         return areaComposite;
 	}
 
 	private Control createExpandedEditArea(Composite parent) {
 		Composite areaComposite = new Composite(parent, SWT.NULL);
 		areaComposite.setLayout(new GridLayout(2, false));
 		
 		GridData dateData = new GridData(SWT.FILL, SWT.FILL, false, false);
 		dateData.verticalSpan = 2;
 		
 		createDateArea(areaComposite).setLayoutData(dateData);
 		createTopRow(areaComposite).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		createBottomRow(areaComposite).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         
         return areaComposite;
 	}
 
 	private Control createDateArea(Composite parent) {
 		Composite areaComposite = new Composite(parent, SWT.NULL);
 		areaComposite.setLayout(new GridLayout(1, false));
 		
 		new Label(areaComposite, 0).setText("Date");
 		dateControl = new DateControlAlwaysExpanded(areaComposite);
         
         return areaComposite;
 	}
 
 	private Control createTopRow(Composite parent) {
 		Composite areaComposite = new Composite(parent, SWT.NULL);
 		areaComposite.setLayout(new GridLayout(3, false));
 		
 		new Label(areaComposite, 0).setText("Account");
 		new Label(areaComposite, 0).setText("Category");
 		new Label(areaComposite, 0).setText("Amount");
 
 		accountControl = new AccountControlWithMruList<BankAccount>(areaComposite, session, BankAccount.class);
 		GridData gdAccount = new GridData(SWT.FILL, SWT.FILL, false, true);
 		gdAccount.widthHint = 200;
 		accountControl.setLayoutData(gdAccount);
 		
 		categoryControl = new AccountControlWithMruList<IncomeExpenseAccount>(areaComposite, session, IncomeExpenseAccount.class);
 		GridData gdCategory = new GridData(SWT.FILL, SWT.FILL, false, true);
 		gdCategory.widthHint = 200;
 		categoryControl.setLayoutData(gdCategory);
 		
 		amountControl = new TextControlWithMruList(areaComposite);
 		amountControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
         
         return areaComposite;
 	}
 
 	private Control createBottomRow(Composite parent) {
 		Composite areaComposite = new Composite(parent, SWT.NULL);
 		areaComposite.setLayout(new GridLayout(2, true));
 		
 		new Label(areaComposite, 0).setText("Memo");
 		new Label(areaComposite, 0).setText("Description");
 
 		memoControl = new TextControlWithMruList(areaComposite);
         memoControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         
         descriptionControl = new TextControlWithMruList(areaComposite);
         descriptionControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         
         return areaComposite;
 	}
 
 	private Control createButtonArea(Composite parent, final Collection<IObjectKey> ourEntryList) {
 		Composite buttonArea = new Composite(parent, SWT.NULL);
 		
 		RowLayout layout = new RowLayout();
 		layout.pack = false; // make all buttons the same size
 		layout.type = SWT.HORIZONTAL;
 		buttonArea.setLayout(layout);
 		
 		Button addButton = new Button(buttonArea, SWT.PUSH);
 		addButton.setText("Enter");
 		addButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				addTransaction(ourEntryList);
 			}
 		});
         
 		Button clearButton = new Button(buttonArea, SWT.PUSH);
 		clearButton.setText("Clear");
 		clearButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 			}
 		});
         
         return buttonArea;
 	}
 
 	public void init(IMemento memento) {
 		if (memento != null) {
 	    	accountControl.init(memento.getChild("account"));
 	    	categoryControl.init(memento.getChild("category"));
 	        memoControl.init(memento.getChild("memo"));
 	        descriptionControl.init(memento.getChild("description"));
 		}
 	}
 	
     public void saveState(IMemento memento) {
     	accountControl.saveState(memento.createChild("account"));
     	categoryControl.saveState(memento.createChild("category"));
         memoControl.saveState(memento.createChild("memo"));
         descriptionControl.saveState(memento.createChild("description"));
     }
     
 	public void addTransaction(Collection<IObjectKey> ourEntryList) {
 		// TODO: This is not quite right - we should obtain
 		// the currency given the account information.
 		Currency currency = session.getDefaultCurrency();
 		
 		/**
 		 * Add a new transaction using the data entered by the user.
 		 * 
 		 * This request is passed on to the template object as only
 		 * the template object knows how to interpret the fields presented
 		 * to the user and build a transaction from the values.
 		 */
 		Date date = dateControl.getDate();
 		
 		if (date == null) {
 			MessageBox diag = new MessageBox(JMoneyPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
 			diag.setText("Warning");
 			diag.setMessage("No date selected.");
 			diag.open();
 			return;
 		}
 		
 		Account bankAccount = accountControl.getAccount();
 		
 	    if (bankAccount == null) {
 	    		MessageBox diag = new MessageBox(JMoneyPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
 	            diag.setText("Warning");
 	            diag.setMessage("No account entered.");
 	            diag.open();
 	        	return;
 	        }
 
 		Account categoryAccount = categoryControl.getAccount();
 		
 	    if (categoryAccount == null) {
 	    		MessageBox diag = new MessageBox(JMoneyPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
 	            diag.setText("Warning");
 	            diag.setMessage("No category entered.");
 	            diag.open();
 	        	return;
 	        }
 
 	    String amountString = amountControl.getText();
         long amount = currency.parse(amountString);
 
         accountControl.rememberChoice();
 	    categoryControl.rememberChoice();
 	    memoControl.rememberChoice();
 	    descriptionControl.rememberChoice();
 	    amountControl.rememberChoice();
 	    
         // Create our own transaction manager.
         TransactionManager transactionManager = new TransactionManager(session.getDataManager());
     	
     	// Set the account that this page is viewing and editing.
     	// We set an account object that is managed by our own
     	// transaction manager.
         Session ourSession = transactionManager.getCopyInTransaction(session);
         
 	    // Add the transaction
    		Transaction transaction = ourSession.createTransaction();
    		Entry entry1 = transaction.createEntry();
    		Entry entry2 = transaction.createEntry();
    		
    		Account bankAccountInTrans = transactionManager.getCopyInTransaction(bankAccount);
    		Account categoryAccountInTrans = transactionManager.getCopyInTransaction(categoryAccount);
    		
    		transaction.setDate(date);
    		entry1.setAccount(bankAccountInTrans);
    		entry2.setAccount(categoryAccountInTrans);
    		
    		entry1.setMemo(memoControl.getText());
    		entry2.setMemo(descriptionControl.getText());
    		
    		if (isIncome) {
    			entry1.setAmount(amount);
    			entry2.setAmount(-amount);
    		} else {
    			entry1.setAmount(-amount);
    			entry2.setAmount(amount);
    		}
    		
    		/*
 		 * Add the entry to the list of entries to be displayed in the above
 		 * table. Note that we put the key in the list, not the object itself.
 		 * The object itself may contain references to other uncommitted
 		 * objects, and so will prevent garbage collection of the transaction.
 		 * Note also that the object key at this time will not contain any
 		 * information that identifies the committed entry (how can it, because
 		 * the committed entry has not yet been created). However, when the
 		 * transaction is committed, the object key will be updated.
 		 */
    		ourEntryList.add(entry1.getObjectKey());
    		
 	    transactionManager.commit(isIncome ? "Income Transaction" : "Expense Transaction");
 
 	    // Clear the controls.
 	    dateControl.setDate(null);
         accountControl.setAccount(null);
 	    categoryControl.setAccount(null);
 	    memoControl.setText("");
 	    descriptionControl.setText("");
 	    amountControl.setText("");
 	}
 
 	public boolean loadEntry(Account account, Entry entry) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
 
