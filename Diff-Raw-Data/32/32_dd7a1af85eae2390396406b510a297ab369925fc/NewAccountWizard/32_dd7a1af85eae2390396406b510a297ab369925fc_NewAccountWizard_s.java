 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2007 Nigel Westbury <westbury@users.sourceforge.net>
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
 
 package net.sf.jmoney.wizards;
 
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.isolation.UncommittedObjectKey;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.AccountInfo;
 import net.sf.jmoney.model2.BankAccountInfo;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.IncomeExpenseAccountInfo;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.resources.Messages;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWizard;
 
 public class NewAccountWizard extends Wizard implements IWorkbenchWizard {
 	
 	private ExtendablePropertySet<? extends Account> accountPropertySet;
 
 	private TransactionManager transactionManager;
 	
 	private Account newUncommittedAccount;
 	
 	/**
 	 * This is set when 'finish' is pressed and the new account is committed.
 	 */
 	private Account newCommittedAccount;
 
 	/**
 	 * Default version used from plugin.xml.
 	 * TODO: Need to have a separate class for each type of account.
 	 * 
 	 */
 	public NewAccountWizard() {
 		this(JMoneyPlugin.getDefault().getSession(), null, BankAccountInfo.getPropertySet());
 	}
 
 	/**
 	 * 
 	 * @param finalPropertySet the property set object of the class
 	 * 		of account to create 
 	 * @param parentAccount the parent account or null if this is to be
 	 * 		a top level account 
 	 */
 	public NewAccountWizard(Session session, IncomeExpenseAccount parentAccount) {
 		this.accountPropertySet = IncomeExpenseAccountInfo.getPropertySet();
 		
 		this.setWindowTitle(Messages.NewAccountWizard_WindowTitleCategory);
 		this.setHelpAvailable(true);
 		
 		transactionManager = new TransactionManager(session.getDataManager());
 		
 		IncomeExpenseAccount parentAccount2 = transactionManager.getCopyInTransaction(parentAccount);
 		if (parentAccount2 == null) {
 			Session session2 = transactionManager.getSession();
 			newUncommittedAccount = session2.createAccount(accountPropertySet);
 		} else {
 			newUncommittedAccount = parentAccount2.createSubAccount();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param finalPropertySet the property set object of the class
 	 * 		of account to create 
 	 * @param parentAccount the parent account or null if this is to be
 	 * 		a top level account 
 	 */
 	public NewAccountWizard(Session session, CapitalAccount parentAccount, ExtendablePropertySet<? extends CapitalAccount> accountPropertySet) {
 		this.accountPropertySet = accountPropertySet;
 		
 		this.setWindowTitle(Messages.NewAccountWizard_WindowTitleAccount);
 		this.setHelpAvailable(true);
 		
 		transactionManager = new TransactionManager(session.getDataManager());
 		
 		CapitalAccount parentAccount2 = transactionManager.getCopyInTransaction(parentAccount);
 		if (parentAccount2 == null) {
 			Session session2 = transactionManager.getSession();
 			newUncommittedAccount = session2.createAccount(accountPropertySet);
 		} else {
 			newUncommittedAccount = parentAccount2.createSubAccount(accountPropertySet);
 		}
 //		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getContainer(), "com.toutvirtual.help.locationDialogId");
 	}
 	
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	public void addPages()
 	{
 		// Show the page that prompts for all the property values.
		WizardPage propertyPage = new WizardPropertyPage(Messages.NewAccountWizard_PropertyPageName, Messages.NewAccountWizard_PropertyPageTitle, Messages.NewAccountWizard_PropertyPageMessage, newUncommittedAccount, accountPropertySet, AccountInfo.getNameAccessor());
 		addPage(propertyPage);
 
		WizardPage summaryPage = new SummaryPage(Messages.NewAccountWizard_SummaryPageName);
 		addPage(summaryPage);
 	}
 	
 	@Override
 	public boolean performFinish() {
 		// TODO: verify properties are valid.
 		
		transactionManager.commit("Add New Account"); //$NON-NLS-1$
 		
 		newCommittedAccount = (Account)((UncommittedObjectKey)newUncommittedAccount.getObjectKey()).getCommittedObjectKey().getObject();
 		
 		return true;
 	}
 	
 	class SummaryPage extends WizardPage {
 		
 		SummaryPage(String pageName) {
 			super(pageName);
 			setTitle(Messages.NewAccountWizard_SummaryPageTitle);
 			setMessage(Messages.NewAccountWizard_SummaryPageMessage);
 		}
 		
 		public void createControl(Composite parent) {
 			Composite container = new Composite(parent, SWT.NONE);
 			
 			GridLayout layout = new GridLayout();
 			layout.marginWidth = 10;
 			layout.marginHeight =10;
 			container.setLayout(layout);
 			
 			GridData gd1 = new GridData();
 			gd1.grabExcessHorizontalSpace = true;
 			gd1.horizontalAlignment = SWT.FILL;
 			gd1.widthHint = 300;
 			
 			Label introText = new Label(container, SWT.WRAP);
 			introText.setText(Messages.NewAccountWizard_SummaryPageContent);
 			introText.setLayoutData(gd1);
 			
 			setControl(container);			
 		}
 		
 		@Override
 		public void performHelp() {
 			MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
 			messageBox.setMessage(Messages.NewAccountWizard_SummaryPageHelpMessage);
 			messageBox.open();
 		}
 	}
 
 	public Account getNewAccount() {
 		return newCommittedAccount;
 	}
 }
 
