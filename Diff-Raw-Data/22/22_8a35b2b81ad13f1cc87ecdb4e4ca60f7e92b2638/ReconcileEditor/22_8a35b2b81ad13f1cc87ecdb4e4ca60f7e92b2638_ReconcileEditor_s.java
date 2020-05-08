 package net.sf.jmoney.reconciliation.reconcilePage;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.Vector;
 import java.util.regex.Matcher;
 
 import net.sf.jmoney.entrytable.CellBlock;
 import net.sf.jmoney.entrytable.DeleteTransactionHandler;
 import net.sf.jmoney.entrytable.DuplicateTransactionHandler;
 import net.sf.jmoney.entrytable.EntryData;
 import net.sf.jmoney.entrytable.EntryRowControl;
 import net.sf.jmoney.entrytable.NewTransactionHandler;
 import net.sf.jmoney.entrytable.OpenTransactionDialogHandler;
 import net.sf.jmoney.entrytable.OtherEntriesPropertyBlock;
 import net.sf.jmoney.entrytable.PropertyBlock;
 import net.sf.jmoney.entrytable.RowControl;
 import net.sf.jmoney.entrytable.RowSelectionTracker;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.EntryInfo;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.model2.TransactionInfo;
 import net.sf.jmoney.reconciliation.BankStatement;
 import net.sf.jmoney.reconciliation.IBankStatementSource;
 import net.sf.jmoney.reconciliation.MemoPattern;
 import net.sf.jmoney.reconciliation.ReconciliationAccount;
 import net.sf.jmoney.reconciliation.ReconciliationAccountInfo;
 import net.sf.jmoney.reconciliation.ReconciliationEntryInfo;
 import net.sf.jmoney.reconciliation.ReconciliationPlugin;
import net.sf.jmoney.views.NodeEditorInput;
 
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Sash;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.part.EditorPart;
 
 public class ReconcileEditor extends EditorPart {
 
 	static public final String ID = "net.sf.jmoney.accountEntriesEditor";
 	
 	/**
 	 * The account being shown in this page.
 	 */
 	protected ReconciliationAccount account = null;
 	
 	protected Vector<CellBlock> allEntryDataObjects = new Vector<CellBlock>();
 
 	protected StatementsSection fStatementsSection;
     protected StatementSection fStatementSection;
     protected UnreconciledSection fUnreconciledSection;
 
 	/**
 	 * The statement currently being shown in this page.
 	 * Null indicates that no statement is currently showing.
 	 */
 	BankStatement statement;
 	
 	/**
 	 * the transaction currently being edited, or null
 	 * if no transaction is being edited
 	 */
 	protected Transaction currentTransaction = null;
 
 	/**
 	 * The import implementation (which implements an import such as QFX, OFX etc.)
 	 * for the last statement import, or null if the user has not yet done a statement import
 	 */
 	protected IBankStatementSource statementSource = null;
 	
     
 	@Override
 	public void init(IEditorSite site, IEditorInput input) {
 		setSite(site);
 		setInput(input);
 		
     	// Set the account that this page is viewing and editing.
		NodeEditorInput input2 = (NodeEditorInput)input;
		CurrencyAccount baseAccount = (CurrencyAccount) input2.getNode();
 		account = baseAccount.getExtension(ReconciliationAccountInfo.getPropertySet(), true);
         
         // Create our own transaction manager.
         // This ensures that uncommitted changes
     	// made by this page are isolated from datastore usage outside
     	// of this page.
 //		DatastoreManager sessionManager = JMoneyPlugin.getDefault().getSessionManager();
 //		session = sessionManager.getSession();
 //
 //        transactionManager = new TransactionManager(sessionManager);
 	}
 
 	@Override
 	public boolean isDirty() {
 		// Page is never dirty
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// Will never be called because editor is never dirty.
 	}
 
 	@Override
 	public void doSaveAs() {
 		// Will never be called because editor is never dirty and 'save as' is not allowed anyway.
 	}
 
     public CurrencyAccount getAccount() {
     	return account.getBaseObject();
     }
 
     public BankStatement getStatement() {
     	return statement;
     }
 
 	@Override
 	public void createPartControl(Composite parent) {
         /*
 		 * Set the statement to show initially. If there are any entries in
 		 * statements after the last reconciled statement, set the first such
 		 * unreconciled statement in this view. Otherwise set the statement to
 		 * null to indicate no statement is to be shown.
 		 */
         // TODO: implement this
         statement = null;
         
     	// Build an array of all possible properties that may be
     	// displayed in the table.
         
         // Add properties from the transaction.
    		for (final ScalarPropertyAccessor propertyAccessor: TransactionInfo.getPropertySet().getScalarProperties3()) {
         	allEntryDataObjects.add(new PropertyBlock<EntryData, RowControl>(propertyAccessor, "transaction") {
     			@Override
         		public ExtendableObject getObjectContainingProperty(EntryData data) {
         			return data.getEntry().getTransaction();
         		}
         	});
         }
 
         // Add properties from this entry.
         // For time being, this is all the properties except the account
         // which come from the other entry, and the amount which is shown in the debit and
         // credit columns.
    		for (ScalarPropertyAccessor<?> propertyAccessor: EntryInfo.getPropertySet().getScalarProperties3()) {
             if (propertyAccessor != EntryInfo.getAccountAccessor() 
            		&& propertyAccessor != EntryInfo.getAmountAccessor()) {
             	allEntryDataObjects.add(new PropertyBlock<EntryData, RowControl>(propertyAccessor, "this") {
         			@Override
             		public ExtendableObject getObjectContainingProperty(EntryData data) {
             			return data.getEntry();
             		}
             	});
             }
         }
 
         /* Add properties that show values from the other entries.
          * These are the account, description, and amount properties.
          * 
          * I don't know what to do if there are other capital accounts
          * (a transfer or a purchase with money coming from more than one account).
          */
    		allEntryDataObjects.add(new OtherEntriesPropertyBlock(EntryInfo.getAccountAccessor()));
    		allEntryDataObjects.add(new OtherEntriesPropertyBlock(EntryInfo.getMemoAccessor(), "description"));
         
         FormToolkit toolkit = new FormToolkit(parent.getDisplay());
     	final ScrolledForm form = toolkit.createScrolledForm(parent);
         form.getBody().setLayout(new GridLayout(2, false));
         
         fStatementsSection = new StatementsSection(form.getBody(), toolkit, account.getBaseObject());
         GridData data = new GridData(SWT.LEFT, SWT.FILL, false, true);
         data.verticalSpan = 2;
         fStatementsSection.getSection().setLayoutData(data);
 
 		// Listen for double clicks.
 		// Double clicking on a statement from the list will show
 		// that statement in the statement table.
         fStatementsSection.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				StatementDetails statementDetails = (StatementDetails)e.item.getData();
 		    	statement = statementDetails.statement;
 		    	
 		    	// Refresh the statement section
 		    	fStatementSection.setStatement(statementDetails.statement, statementDetails.openingBalance);
 			}
 		});
 		
 		Composite actionbarContainer = new Composite(form.getBody(), 0);
 		
 		GridLayout actionbarLayout = new GridLayout();
 		actionbarLayout.numColumns = 4;
 		actionbarContainer.setLayout(actionbarLayout);
 		
 		final Combo fStatementsViewCombo = new Combo(actionbarContainer, SWT.DROP_DOWN);
 		fStatementsViewCombo.setItems(new String [] {
 				ReconciliationPlugin.getResourceString("ToolbarSection.hideStatements"),
 				ReconciliationPlugin.getResourceString("ToolbarSection.showStatementsWithoutBalances"),
 				ReconciliationPlugin.getResourceString("ToolbarSection.showStatementsWithBalances"),
 		});
 		fStatementsViewCombo.select(2);
 		fStatementsViewCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				GridData gd = (GridData)fStatementsSection.getSection().getLayoutData();
 				switch (fStatementsViewCombo.getSelectionIndex()) {
 				case 0:
 					gd.heightHint = 0;
 					gd.widthHint = 0;
 					break;
 				case 1:
 					gd.heightHint = SWT.DEFAULT;
 					gd.widthHint = SWT.DEFAULT;
 					fStatementsSection.showBalance(false);
 					break;
 				case 2:
 					gd.heightHint = SWT.DEFAULT;
 					gd.widthHint = SWT.DEFAULT;
 					fStatementsSection.showBalance(true);
 					break;
 				}
 				
 				form.getBody().layout(true);
 			}
 		});
 		
 		Button newStatementButton = new Button(actionbarContainer, SWT.PUSH);
 		newStatementButton.setText("New Statement...");
 		newStatementButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				StatementDetails lastStatement = fStatementsSection.getLastStatement();
 				NewStatementDialog messageBox = 
 					new NewStatementDialog(getSite().getShell(), lastStatement==null ? null : lastStatement.statement);
 				if (messageBox.open() == Dialog.OK) {
 					statement = messageBox.getValue();
 					long openingBalanceOfNewStatement = fStatementsSection.getStatementOpeningBalance(statement);
 					fStatementSection.setStatement(statement, openingBalanceOfNewStatement);
 				}				
 			}
 		});
 
 		final ToolBar toolBar =
 			new ToolBar(actionbarContainer, SWT.FLAT);
 		final ToolItem importButton =
 			new ToolItem(toolBar, SWT.DROP_DOWN);
 		importButton.setText("Import");
 		final Menu menu = new Menu(getSite().getShell(), SWT.POP_UP);
 
 		
 		// The list of sources are taken from the net.sf.jmoney.reconciliation.bankstatements
 		// extension point.
 		
 		// Load the extensions
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sf.jmoney.reconciliation.bankstatements");
 		IExtension[] extensions = extensionPoint.getExtensions();
 		
 		for (int i = 0; i < extensions.length; i++) {
 			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
 			for (int j = 0; j < elements.length; j++) {
 				if (elements[j].getName().equals("statement-source")) {
 					String description = elements[j].getAttribute("description");
 					
 					MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
 					menuItem.setText(description);
 					
 					final IConfigurationElement thisElement = elements[j];
 					
 					menuItem.addSelectionListener(new SelectionAdapter() {
 						@Override
 						public void widgetSelected(SelectionEvent event) {
 							try {
 								// Load the extension point listener for the selected source
 								statementSource = (IBankStatementSource)thisElement.createExecutableExtension("class");
 								importStatement();
 							} catch (CoreException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 								throw new RuntimeException("class attribute not found");
 							}
 						}
 					});
 				}
 			}
 		}		  
 
 		importButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!account.isReconcilable()) {
 			        MessageBox diag = new MessageBox(getSite().getShell());
 			        diag.setText("Feature not Available");
 			        diag.setMessage("Before you can import entries from your bank's servers, you must first set the rules for the initial categories for the imported entries.  Press the 'Options...' button to set this up.");
 			        diag.open();
 			        return;
 				}
 				
 				if (statement == null) {
 			        MessageBox diag = new MessageBox(getSite().getShell());
 			        diag.setText("Feature not Available");
 			        diag.setMessage("Before you can import entries from your bank's servers, you must first create or select a bank statement into which the entries will be imported.");
 			        diag.open();
 			        return;
 				}
 				
 				if (event.detail == SWT.NONE) {
 					/*
 					 * The user pressed the import button, but not on the down-arrow that
 					 * is positioned at the right side of the import button.  In this case,
 					 * we import using the format that was last used, or we ignore the click
 					 * if the user has not yet done an import using this button.
 					 */
 					if (statementSource != null) {
 						importStatement();
 					}
 				} else if (event.detail == SWT.ARROW) {
 					Rectangle rect = importButton.getBounds();
 					Point pt = new Point(rect.x, rect.y + rect.height);
 					menu.setLocation(toolBar.toDisplay(pt));
 					menu.setVisible(true);
 				}
 			}
 		});
 
 		Button optionsButton = new Button(actionbarContainer, SWT.PUSH);
 		optionsButton.setText("Options...");
 		optionsButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ImportOptionsDialog messageBox = 
 					new ImportOptionsDialog(getSite().getShell(), account);
 				messageBox.open();
 			}
 		});
 		
         Composite containerOfSash = new Composite(form.getBody(), 0);
         containerOfSash.setLayout(new FormLayout());
 
         // Create the sash first, so the other controls
         // can be attached to it.
         final Sash sash = new Sash(containerOfSash, SWT.BORDER | SWT.HORIZONTAL);
         FormData formData = new FormData();
         formData.left = new FormAttachment(0, 0); // Attach to left
         formData.right = new FormAttachment(100, 0); // Attach to right
         formData.top = new FormAttachment(50, 0); // Attach halfway down
         sash.setLayoutData(formData);
 
         sash.addSelectionListener(new SelectionAdapter() {
         	@Override
         	public void widgetSelected(SelectionEvent event) {
         		final int mimimumHeight = 61;  // In Windows, allows 3 lines minimum.  TODO: Calculate this for other OS's
         		int y = event.y;
         		if (y < mimimumHeight) {
         			y = mimimumHeight;
         		}
         		if (y + sash.getSize().y > sash.getParent().getSize().y - mimimumHeight) {
         			y = sash.getParent().getSize().y - mimimumHeight - sash.getSize().y;
         		}
 
         		// We re-attach to the top edge, and we use the y value of the event to
         		// determine the offset from the top
         		((FormData) sash.getLayoutData()).top = new FormAttachment(0, y);
 
         		// Until the parent window does a layout, the sash will not be redrawn in
         		// its new location.
         		sash.getParent().layout();
         	}
         });
 
         GridData gridData1 = new GridData(SWT.FILL, SWT.FILL, true, true);
         gridData1.heightHint = 200;   // TODO: tidy up???
         gridData1.widthHint = 200;   // TODO: tidy up???
         containerOfSash.setLayoutData(gridData1);
         
         /*
          * The common row tracker.  This is used by both tables, so that
          * there is only one selection in the part.
          */
 	    RowSelectionTracker<EntryRowControl> rowTracker = new RowSelectionTracker<EntryRowControl>();
         
         fStatementSection = new StatementSection(containerOfSash, toolkit, this, rowTracker);
 
         formData = new FormData();
         formData.top = new FormAttachment(0, 0);
         formData.bottom = new FormAttachment(sash, 0);
         formData.left = new FormAttachment(0, 0);
         formData.right = new FormAttachment(100, 0);
         fStatementSection.getSection().setLayoutData(formData);
         
         fUnreconciledSection = new UnreconciledSection(containerOfSash, toolkit, this, rowTracker);
 
         formData = new FormData();
         formData.top = new FormAttachment(sash, 0);
         formData.bottom = new FormAttachment(100, 0);
         formData.left = new FormAttachment(0, 0);
         formData.right = new FormAttachment(100, 0);
         fUnreconciledSection.getSection().setLayoutData(formData);
 
         form.setText("Reconcile Entries against Bank Statement/Bank's Records");
 
          /*
 		 * Activate the handlers. Note that the 'new' and 'duplicate' actions
 		 * put the new entry into the unreconciled section. It is important that
 		 * is where the new entry is put because no statement is set on the
 		 * entry for a new or duplicated entry.
 		 */
 		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
 
 		IHandler handler = new NewTransactionHandler(rowTracker, fUnreconciledSection.fUnreconciledEntriesControl);
 		handlerService.activateHandler("net.sf.jmoney.newTransaction", handler);		
 
 		handler = new DeleteTransactionHandler(rowTracker);
 		handlerService.activateHandler("net.sf.jmoney.deleteTransaction", handler);		
 
 		handler = new DuplicateTransactionHandler(rowTracker, fUnreconciledSection.fUnreconciledEntriesControl);
 		handlerService.activateHandler("net.sf.jmoney.duplicateTransaction", handler);		
 
 		handler = new OpenTransactionDialogHandler(rowTracker);
 		handlerService.activateHandler("net.sf.jmoney.transactionDetails", handler);		
 	}
 	
 	public void saveState(IMemento memento) {
 //		for (String id: transactionTypes.keySet()) {
 //			ITransactionTemplate transactionType = transactionTypes.get(id);
 //			transactionType.saveState(memento.createChild("template", id));
 //		}
 	}
 	
 	@Override
 	public void setFocus() {
 		// Don't bother to do anything.  User can select as required.
 	}
 	/**
 	 * Imports data from an external source (usually the Bank's server or a file downloaded
 	 * from the Bank's server) into this bank statement.
 	 * 
 	 * Before this method can be called, the following must be set:
 	 * 
 	 * 1. statementSource must be set to an implementation of the IBankStatementSource interface.
 	 * 2. A statement must be open in the editor 
 	 */
 	void importStatement() {
 //		outer: for (Entry entry : account.getBaseObject().getEntries()) {
 //		if (entry.getCheck() == null && entry.getMemo() != null) {
 //			if (entry.getMemo().length() < 3) continue;
 //			if (entry.getMemo().length() > 4) continue;
 //			String memo = entry.getMemo();
 //			if (memo.charAt(0) == '0') memo = memo.substring(1);
 //			if (memo.length() < 3) continue;
 //			if (memo.charAt(0) == '0') continue;
 //			for (int i=0; i < memo.length(); i++) {
 //				if (memo.charAt(i) < '0' || memo.charAt(i) > '9') continue outer; 
 //			}
 //			entry.setCheck(memo);
 //			entry.setMemo("check " + memo);
 //		}
 //			
 //		}
 	
 	
 		/*
 		 * Set the default start date to be the first day after the date of the previous
 		 * statement (if any and if statements are dated, not numbered), and the default
 		 * end date to be the date of this statement.
 		 */
 		Date defaultEndDate = 
 			statement.isNumber() ? null : statement.getStatementDate();
 		
 		BankStatement priorStatement = fStatementsSection.getPriorStatement(statement);
 		Date defaultStartDate = null;
 		if (priorStatement != null && !priorStatement.isNumber()) {
 			Calendar oneDayLater = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 			oneDayLater.setTime(priorStatement.getStatementDate());
 			oneDayLater.add(Calendar.DAY_OF_MONTH, 1);
 			defaultStartDate = oneDayLater.getTime();
 		}
 		
 		Collection<IBankStatementSource.EntryData> importedEntries = statementSource.importEntries(getSite().getShell(), getAccount(), defaultStartDate, defaultEndDate);
 		if (importedEntries != null) {
 			/*
 			 * Create a transaction to be used to import the entries.  This allows the entries to
 			 * be more efficiently written to the back-end datastore and it also groups
 			 * the entire import as a single change for undo/redo purposes.
 			 */
 			TransactionManager transactionManager = new TransactionManager(account.getDataManager());
 			CurrencyAccount accountInTransaction = transactionManager.getCopyInTransaction(account.getBaseObject());
 			IncomeExpenseAccount defaultCategoryInTransaction = accountInTransaction.getPropertyValue(ReconciliationAccountInfo.getDefaultCategoryAccessor());
 			Session sessionInTransaction = accountInTransaction.getSession();
 
 			/*
 			 * Get the patterns sorted into order.  It is important that we test patterns in the
 			 * correct order because an entry may match both a general pattern and a more specific
 			 * pattern.
 			 */
 			List<MemoPattern> sortedPatterns = new ArrayList<MemoPattern>(account.getPatternCollection());
 			Collections.sort(sortedPatterns, new Comparator<MemoPattern>(){
 				public int compare(MemoPattern arg1, MemoPattern arg2) {
 					return arg1.getOrderingIndex() - arg2.getOrderingIndex();
 				}
 			});
 			
 			entryLoop: for (IBankStatementSource.EntryData entryData: importedEntries) {
 				/*
 				 * First we try auto-matching.
 				 * 
 				 * If we have an auto-match then we don't have to create a new
 				 * transaction at all. We just update a few properties in the
 				 * existing entry.
 				 * 
 				 * An entry auto-matches if:
 				 *  - The amount exactly matches
 				 *  - The entry has no statement set
 				 *  - If a check number is specified in the existing entry then
 				 * it must match a check number in the import (but if no check
 				 * number is in the existing entry, that is ok)
 				 *  - The date must be either exactly equal,
 				 * 
 				 * or it can be up to 10 days in the future but it can only be
 				 * in the future if there is a check number match. This allows,
 				 * say, a check to match that is likely not going to appear till
 				 * a few days later.
 				 * 
 				 * or it can be up to 1 day in the future but only if there
 				 * are no other entries that match. This restriction prevents a
 				 * false match when there are lots of charges for the same
 				 * amount very close together (e.g. consider a cup of coffee
 				 * charged every day or two)
 				 */
 				Collection<Entry> possibleMatches = new ArrayList<Entry>();
 				for (Entry entry : account.getBaseObject().getEntries()) {
 					if (entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor()) == null
 							&& entry.getAmount() == entryData.amount) {
 						System.out.println("amount: " + entryData.amount);
 						Date importedDate = (entryData.valueDate != null)
 						? entryData.valueDate
 								: entryData.clearedDate;
 						if (entry.getCheck() == null) {
 							if (entry.getTransaction().getDate().equals(importedDate)) {
 								// Auto-reconcile
 								possibleMatches.add(entry);
 								
 								/*
 								 * Date exactly matched - so we can quit
 								 * searching for other matches. (If user entered
 								 * multiple entries with same check number then
 								 * the user will not be surprised to see an
 								 * arbitrary one being used for the match).
 								 */
 								break;
 							} else {
 								Calendar fiveDaysLater = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 								fiveDaysLater.setTime(entry.getTransaction().getDate());
 								fiveDaysLater.add(Calendar.DAY_OF_MONTH, 5);
 								
 								if ((entryData.check == null || entryData.check.length() == 0) 
 										&& (importedDate.equals(entry.getTransaction().getDate())
 										 || importedDate.after(entry.getTransaction().getDate()))
 										 && importedDate.before(fiveDaysLater.getTime())) {
 									// Auto-reconcile
 									possibleMatches.add(entry);
 								}
 							}
 						} else {
 							// A check number is present
 							Calendar twentyDaysLater = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 							twentyDaysLater.setTime(entry.getTransaction().getDate());
 							twentyDaysLater.add(Calendar.DAY_OF_MONTH, 20);
 							
 							if (entry.getCheck().equals(entryData.check)
 									&& (importedDate.equals(entry.getTransaction().getDate())
 									 || importedDate.after(entry.getTransaction().getDate()))
 									 && importedDate.before(twentyDaysLater.getTime())) {
 								// Auto-reconcile
 								possibleMatches.add(entry);
 								
 								/*
 								 * Check number matched - so we can quit
 								 * searching for other matches. (If user entered
 								 * multiple entries with same check number then
 								 * the user will not be surprised to see an
 								 * arbitrary one being used for the match).
 								 */
 								break;
 							}
 						}
 					}
 				}
 
 				if (possibleMatches.size() == 1) {
 					Entry match = possibleMatches.iterator().next();
 					
 					Entry entryInTrans = transactionManager.getCopyInTransaction(match);
 
 					if (entryData.valueDate == null) {
 						entryInTrans.setValuta(entryData.clearedDate);
 					} else {
 						entryInTrans.setValuta(entryData.valueDate);
 					}
 
 					entryInTrans.setPropertyValue(ReconciliationEntryInfo.getStatementAccessor(), statement);
 					entryInTrans.setCheck(entryData.check);
 					entryInTrans.setPropertyValue(ReconciliationEntryInfo.getUniqueIdAccessor(), entryData.uniqueId);
 
 					continue entryLoop;
 				}
 				
 		   		Transaction transaction = sessionInTransaction.createTransaction();
 		   		Entry entry1 = transaction.createEntry();
 		   		Entry entry2 = transaction.createEntry();
 		   		entry1.setAccount(accountInTransaction);
 		   		entry1.setPropertyValue(ReconciliationEntryInfo.getStatementAccessor(), statement);
 		   		
 		   		/*
 		   		 * Scan for a match in the patterns.  If a match is found,
 		   		 * use the values for memo, description etc. from the pattern.
 		   		 */
 				String text = entryData.getTextToMatch();
 		   		for (MemoPattern pattern: sortedPatterns) {
 		   			Matcher m = pattern.getCompiledPattern().matcher(text);
 		   			System.out.println(pattern.getPattern() + ", " + text);
 		   			if (m.matches()) {
 		   				/*
 		   				 * Group zero is the entire string and the groupCount method
 		   				 * does not include that group, so there is really one more group
 		   				 * than the number given by groupCount.
 		   				 */
 		   				Object [] args = new Object[m.groupCount()+1];
 		   				for (int i = 0; i <= m.groupCount(); i++) {
 		   					args[i] = m.group(i);
 		   				}
 		   				
 		   				// TODO: What effect does the locale have in the following?
 		   				if (pattern.getCheck() != null) {
 		   					entry1.setCheck(
 		   							new java.text.MessageFormat(
 		   									pattern.getCheck(), 
 		   									java.util.Locale.US)
 		   							.format(args));
 		   				}
 		   				
 		   				if (pattern.getMemo() != null) {
 		   					entry1.setMemo(
 		   							new java.text.MessageFormat(
 		   									pattern.getMemo(), 
 		   									java.util.Locale.US)
 		   							.format(args));
 		   				}
 		   				
 		   				if (pattern.getDescription() != null) {
 		       				entry2.setMemo(
 		       						new java.text.MessageFormat(
 		       								pattern.getDescription(), 
 		       								java.util.Locale.US)
 		       								.format(args));
 		   				}
 		   				
 		           		entry2.setAccount(transactionManager.getCopyInTransaction(pattern.getAccount()));
 		           		
 		           		break;
 		   			}
 		   		}
 		   		
 		   		// If nothing matched, set the default account but no 
 		   		// other property.
 		   		if (entry2.getAccount() == null) {
 		   			entry2.setAccount(defaultCategoryInTransaction);
 					entry1.setMemo(entryData.getDefaultMemo());
 					entry2.setMemo(entryData.getDefaultDescription());
 					
 		   		}
 		   		
 		   		entryData.assignPropertyValues(transaction, entry1, entry2);
 			}
 			
 			/*
 			 * All entries have been imported and all the properties
 			 * have been set and should be in a valid state, so we
 			 * can now commit the imported entries to the datastore.
 			 */
 			transactionManager.commit("Import Entries");									
 		}
 	}
 }
