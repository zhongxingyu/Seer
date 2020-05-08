 package net.sf.jmoney.transactionDialog;
 
 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2006, 2008 Nigel Westbury <westbury@users.sourceforge.net>
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
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.jmoney.entrytable.BaseEntryRowControl;
 import net.sf.jmoney.entrytable.Block;
 import net.sf.jmoney.entrytable.CellBlock;
 import net.sf.jmoney.entrytable.FocusCellTracker;
 import net.sf.jmoney.entrytable.Header;
 import net.sf.jmoney.entrytable.HorizontalBlock;
 import net.sf.jmoney.entrytable.ICellControl2;
 import net.sf.jmoney.entrytable.InvalidUserEntryException;
 import net.sf.jmoney.entrytable.RowControl;
 import net.sf.jmoney.entrytable.RowSelectionTracker;
 import net.sf.jmoney.entrytable.SingleOtherEntryPropertyBlock;
 import net.sf.jmoney.entrytable.SplitEntryRowControl;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.EntryInfo;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.model2.TransactionInfo;
 import net.sf.jmoney.resources.Messages;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.DialogMessageArea;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * This class manages the transaction dialog that appears when
  * the 'details' button is pressed in the account entries list page.
  * <P>
  * The transaction dialog displays a single transaction.  This is a lower-level
  * view of the transaction, showing all the applicable underlying properties
  * of each entry.
  * 
  * @author Nigel Westbury
  */
 public class TransactionDialog extends Dialog {
 	
 	private static final int NEW_SPLIT_ID     = IDialogConstants.CLIENT_ID + 0;
 	private static final int DELETE_SPLIT_ID  = IDialogConstants.CLIENT_ID + 1;
 	private static final int ADJUST_AMOUNT_ID = IDialogConstants.CLIENT_ID + 2;
 
 	private TransactionManager transactionManager;
 	
 	private Entry topEntry;
 	
 	private DialogMessageArea messageArea;
 
 	private Composite tableComposite;
 
 	private RowSelectionTracker<SplitEntryRowControl> rowTracker;
 
 	private FocusCellTracker cellTracker;
 
 	private Block<Entry, SplitEntryRowControl> rootBlock;
 
 	private Transaction transaction;
 
 	private ScrolledComposite scrolled;
 	
 	/** This needs to be a field only because it is the contents of the scrolled composite */
 	private Control entriesTable;
 	
 	protected Map<Entry, RowControl> rowControlMap = new HashMap<Entry, RowControl>();
 
 	/**
 	 * Note that an Entry object is passed, not a Transaction object as one might
 	 * expect.  This allows this dialog to put the 'main' entry first with the
 	 * 'split' entries below.  Of course, which entry is the 'main' entry depends
 	 * on the context from which this dialog was opened.
 	 */
 	public TransactionDialog(Shell parentShell, Entry originalEntry) {
 		super(parentShell);
 
 		/*
 		 * All changes within this dialog are made within a transaction, so canceling
 		 * is trivial (the transaction is simply not committed).
 		 */
 		transactionManager = new TransactionManager(originalEntry.getDataManager());
     	
     	topEntry = transactionManager.getCopyInTransaction(originalEntry);
     	
    		transaction = topEntry.getTransaction();
 	}
 
 	@Override
 	protected void buttonPressed(int buttonId) {
 		switch (buttonId) {
 		case IDialogConstants.OK_ID:
 			// All edits are transferred to the model as they are made,
 			// so we just need to commit them.
 			
 			// But first make sure the current selection is saved.
 			if (cellTracker.getFocusCell() != null) {
 				cellTracker.getFocusCell().save();
 			}
 			
     		transactionManager.commit("Edit Transaction"); //$NON-NLS-1$
 			break;
 		case NEW_SPLIT_ID:
 			newSplit();
 			break;
 		case DELETE_SPLIT_ID:
 			deleteSplit();
 			break;
 		case ADJUST_AMOUNT_ID:
 			adjustAmount();
 			break;
 		} 
 		super.buttonPressed(buttonId);
 	}
 
 	private void newSplit() {
 		/*
 		 * If all entries so far are in the same currency then set the amount of
 		 * the new entry to be the amount that takes the balance to zero. If we
 		 * cannot determine the currency because the user has not yet entered
 		 * the necessary data, assume that the currencies are all the same.
 		 * 
 		 * Note that this must be done before we create the new entry.
 		 */
 		Commodity commodity = null;
    		boolean mismatchedCommodities = false;
    		long totalAmount = 0;
         for (Entry entry: topEntry.getTransaction().getEntryCollection()) {
         	if (commodity == null) {
         		// No commodity yet determined, so set to the commodity for
         		// this entry, if any.
        		commodity = entry.getCommodityInternal(); 
         	} else {
        		if (!commodity.equals(entry.getCommodityInternal())) {
         			mismatchedCommodities = true;
         			break;
         		}
         	}
         	totalAmount += entry.getAmount();
         }
         
 		Entry newEntry = topEntry.getTransaction().createEntry();
 
         if (!mismatchedCommodities) {
         	newEntry.setAmount(-totalAmount);
         }
 	}
 
 	private void deleteSplit() {
		/*
		 * One of the controls in the deleted row will have the focus.
		 * We set this to null as the control will not longer exist.
		 */
		cellTracker.setFocusCell(null);
		
 		SplitEntryRowControl rowControl = rowTracker.getSelectedRow();
 		transaction.deleteEntry(rowControl.getInput());
 	}
 	
 	private void adjustAmount() {
 		SplitEntryRowControl rowControl = rowTracker.getSelectedRow();
 		// TODO: Is a row ever not selected?
 		if (rowControl != null) {
 			Entry entry = rowControl.getInput();
 			
 			long totalAmount = 0;
 			for (Entry eachEntry: transaction.getEntryCollection()) {
 				totalAmount += eachEntry.getAmount();
 			}
 			
 			entry.setAmount(entry.getAmount() - totalAmount);
 		}
 	}
 	
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		shell.setText(Messages.TransactionDialog_ShellText);
 	}
 	
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, NEW_SPLIT_ID,
 				Messages.TransactionDialog_ButtonTextNewSplit, false);
 		createButton(parent, DELETE_SPLIT_ID,
 				Messages.TransactionDialog_ButtonTextDeleteSplit, false);
 		createButton(parent, ADJUST_AMOUNT_ID,
 				Messages.TransactionDialog_ButtonTextAdjustAmont, false);
 		// create OK and Cancel buttons by default
 		createButton(parent, IDialogConstants.OK_ID,
 				IDialogConstants.OK_LABEL, true);
 		createButton(parent, IDialogConstants.CANCEL_ID,
 				IDialogConstants.CANCEL_LABEL, false);
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite composite = (Composite) super.createDialogArea(parent);
 		composite.setLayout(new GridLayout(1, false));
 
 		// Message label
 		messageArea = new DialogMessageArea();
 		messageArea.createContents(composite);
 		
 		// Ensure the message area is shown and fills the space
 		messageArea.setTitleLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		messageArea.setMessageLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		/*
 		 * Any errors that would prevent the transaction from being committed
 		 * are shown. This dialog may be opened on an uncommitted transaction
 		 * (causing a third level transaction to be created). In that case the
 		 * transaction may not be valid even before this dialog is opened. We
 		 * must be sure that error messages are shown straight away.
 		 */
 		updateErrorMessage();
 		
 		transactionManager.addChangeListener(new SessionChangeAdapter() {
 			@Override
 			public void objectCreated(ExtendableObject newObject) {
 				if (newObject instanceof Entry) {
 					Entry newEntry = (Entry)newObject;
 					if (newEntry.getTransaction() == transaction) {
 						createEntryRow(newEntry);
 					}
 					
 					layoutTable();
 				}
 			}
 
 			// May be objectDestroyed should be called???  However that method is not
 			// called when an entry is deleted outside a transaction.
 			@Override
 			public void objectRemoved(ExtendableObject deletedObject) {
 				RowControl rowControl = rowControlMap.remove(deletedObject);
 				if (rowControl != null) {
 					rowControl.dispose();
 					
 					// There is a bug here.  The problem is that this method is called
 					// before the entry is removed from the datastore.  That is by design
 					// because once an object is removed from the datastore it is no longer
 					// valid, so the code to process the delete cannot do stuff like get
 					// the parent.  However, the code to update the error message and stuff
 					// (which is called in the following call) will not work correctly because
 					// it will still see the deleted entry.
 					// 
 					// There are two solutions:
 					// 1. This code here should pass down the deleted entry so that the deletion
 					//   can be taken into account.
 					// 2. The framework should be changed so that the entry is not returned in the
 					// 		parent list but one can still follow references AWAY from the deleted
 					//		object so that code can know where the object used to exist and what
 					//		was in the object.
 					//
 					// 2. is probably better, but may be involved.
 					
 					layoutTable();
 				}
 			}
 
 			@Override
 			public void objectChanged(ExtendableObject changedObject, ScalarPropertyAccessor changedProperty, Object oldValue, Object newValue) {
 				// This may be overkill, but for time being do a full layout when any property changes.
 				// In particular, changing the account type will change the height of the row.
 				layoutTable();
 			}
 			
 			private void layoutTable() {
 				refreshScrolling();
 
 				updateErrorMessage();
 				
 				// This must be called if the error message has changed, because otherwise
 				// the message may be truncated.  It still seems to get a little bit
 				// truncated.
 				getShell().layout(true);
 		        getShell().pack(true);
 			}
 		}, composite);
 		
 		Label label = new Label(composite, SWT.WRAP);
 		label.setText(Messages.TransactionDialog_DialogLabel);
 
 		GridData messageData = new GridData();
 		Rectangle rect = getShell().getMonitor().getClientArea();
 		messageData.widthHint = rect.width/2;
 		label.setLayoutData(messageData);
 
         // Create the transaction property area
 		createTransactionPropertiesArea(composite).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		// Create the table area
 		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		tableData.minimumWidth = 200;
 		tableData.minimumHeight = 200;
 		createScrollableEntriesTable(composite).setLayoutData(tableData);
 
 		applyDialogFont(composite);
 		return composite;
 	}
 
 	private Control createTransactionPropertiesArea(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout(10, false));
 
         // Add properties from the transaction.
    		for (ScalarPropertyAccessor<?> propertyAccessor: TransactionInfo.getPropertySet().getScalarProperties3()) {
         	Label propertyLabel = new Label(composite, 0);
         	propertyLabel.setText(propertyAccessor.getDisplayName() + ':');
         	final IPropertyControl<ExtendableObject> propertyControl = propertyAccessor.createPropertyControl(composite);
         	propertyControl.load(topEntry.getTransaction());
 
         	// Create an ICellControl2, which is really just an IPropertyControl but with 2 extra methods.
         	// We do this by wrapping.   Can this be cleaned up a little?
     		final ICellControl2<Entry> cellControl = new ICellControl2<Entry>() {
 
     			public Control getControl() {
     				return propertyControl.getControl();
     			}
 
     			public void load(Entry entry) {
     				propertyControl.load(entry);
     			}
 
     			public void save() {
     				propertyControl.save();
     			}
 
     			@Override
     			public void setSelected() {
     				propertyControl.getControl().setBackground(RowControl.selectedCellColor);
     			}
 
     			@Override
     			public void setUnselected() {
     				propertyControl.getControl().setBackground(null);
     			}
     		};
     		
 
         	
         	addFocusListenerRecursively(propertyControl.getControl(), new FocusAdapter() {
 				@Override
 				public void focusGained(FocusEvent e) {
 					final ICellControl2<?> previousFocus = cellTracker.getFocusCell();
 					if (cellControl == previousFocus) {
 						/*
 						 * The focus has changed to a different control as far as SWT is
 						 * concerned, but the focus is still within the same cell
 						 * control. This can happen if the cell control is a composite
 						 * that contains multiple child controls, such as the date
 						 * control. Focus may move from the text box of a date control
 						 * to the button in the date control, but focus has not left the
 						 * cell. We take no action in this situation.
 						 * 
 						 * This can also happen if focus was lost to a control outside
 						 * of the table. This does not change the focus cell within the
 						 * table so when focus is returned to the table we will not see
 						 * a cell change here.
 						 */
 						return;
 					}
 
 					/*
 					 * It is important to set the new focus cell straight away. The
 					 * reason is that if, for example, a dialog box is shown (such as
 					 * may happen in the selectionTracker.setSelection method below)
 					 * then focus will move away from the control to the dialog then
 					 * back again when the dialog is closed. If the new focus is already
 					 * set then nothing will happen the second time the control gets
 					 * focus (because of the test above).
 					 */
 					cellTracker.setFocusCell(cellControl);
 
 					/*
 					 * Make sure any changes in the control are written back to the model.
 					 */
 					if (previousFocus != null) {
 						previousFocus.save();
 					}
 
 					/*
 					 * Opening dialog boxes (as may be done by the
 					 * selectionTracker.setSelection method below) and calling setFocus
 					 * both cause problems if done from within the focusGained method.
 					 * We therefore queue up a new task on this same thread to check
 					 * whether the row selection can change and either update the
 					 * display (background colors and borders) to show the row selection
 					 * or revert the focus to the original cell.
 					 */
 					cellControl.getControl().getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							// No row is selected when a transaction property is selected
 							boolean success = rowTracker.setSelection(null, /*TODO: cellBlock*/null);
 							if (success) {
 								/*
 								 * The row selection will have been set by the setSelection method
 								 * but we must also update the cell selection.
 								 */ 
 								if (previousFocus != null) {
 									previousFocus.setUnselected();
 								}
 
 								cellControl.setSelected();
 							} else {
 								/*
 								 * The row selection change was rejected so restore the original cell selection.
 								 */
 	
 								// This needs to be sorted out, but is not high priority because row departures
 								// rarely (never) fail in this dialog.
 //								rowTracker.getSelectedRow().scrollToShowRow();
 								
 								// TODO: Should we be restoring selection to the cell that needs correcting?
 								cellTracker.setFocusCell(previousFocus);
 								if (previousFocus != null) {
 								    previousFocus.getControl().setFocus();
 								}
 							}
 						}
 					});
 				}
 			});
         }
 		return composite;
 	}
 
 	private void addFocusListenerRecursively(Control control, FocusListener listener) {
 		control.addFocusListener(listener);
 		if (control instanceof Composite) {
 			for (Control child: ((Composite)control).getChildren()) {
 				addFocusListenerRecursively(child, listener);
 			}
 		}
 	}
 
 	private Control createScrollableEntriesTable(Composite parent) {
 		scrolled = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
 
 		GridData resultData = new GridData(SWT.DEFAULT, 200);
 		resultData.heightHint = 200;
 		
 		GridData resultData2 = new GridData(SWT.DEFAULT, 200);
 		resultData2.heightHint = 200;
 		
 		entriesTable = createEntriesTable(scrolled);
 		entriesTable.setLayoutData(resultData);
 		
 		entriesTable.setLayoutData(resultData2);
 		
 		scrolled.setContent(entriesTable);
         
 		scrolled.setExpandHorizontal(true);
 		scrolled.setExpandVertical(true);
 		
 		Point preferredSize = entriesTable.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
 		scrolled.setMinSize(preferredSize);
 		
 		return scrolled;
 	}
 	
 	/**
 	 * this method should be called when the total height of all entries in the
 	 * table changes. this could happen when entries are added or deleted and it
 	 * could happen if the preferred height of an entry changes (perhaps because
 	 * the account was changed which resulted in more or fewer properties being
 	 * applicable to the entry).
 	 */
 	public void refreshScrolling() {
 		Point preferredSize = entriesTable.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 		scrolled.setMinSize(preferredSize);
 	
 		preferredSize = scrolled.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 		if (preferredSize.y > 400) {
 			preferredSize.y = 400;
 		}
 		scrolled.setLayoutData(new GridData(preferredSize.x, preferredSize.y));
 	}
 	
 	private Control createEntriesTable(Composite parent) {
 		
 		/*
 		 * Setup the layout structure of the header and rows.
 		 */
 
 		CellBlock<Entry, SplitEntryRowControl> debitColumnManager = SplitEntryDebitAndCreditColumns.createDebitColumn(transaction.getSession().getDefaultCurrency());
 		CellBlock<Entry, SplitEntryRowControl> creditColumnManager = SplitEntryDebitAndCreditColumns.createCreditColumn(transaction.getSession().getDefaultCurrency());
 		
 		rootBlock = new HorizontalBlock<Entry, SplitEntryRowControl>(
 								new SingleOtherEntryPropertyBlock(EntryInfo.getAccountAccessor()),
 								new SingleOtherEntryPropertyBlock(EntryInfo.getMemoAccessor()),
 								new PropertiesBlock(this), 
 								debitColumnManager,
 								creditColumnManager
 		);
 
 		/*
 		 * Ensure indexes are set.
 		 */
 		rootBlock.initIndexes(0);
 
 		rowTracker = new RowSelectionTracker<SplitEntryRowControl>();
 		cellTracker = new FocusCellTracker();
 		tableComposite = new Composite(parent, SWT.NONE);
 		tableComposite.setLayout(new GridLayout(1, false));
 
 		new Header<Entry>(tableComposite, SWT.NONE, rootBlock).setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		
 		for (Entry entry: topEntry.getTransaction().getEntryCollection()) {
 			createEntryRow(entry);
 		}
 			
 	    
 		return tableComposite;
 	}
 
 	private void createEntryRow(Entry entry) {
 		SplitEntryRowControl rowControl = new SplitEntryRowControl(tableComposite, SWT.NONE, rootBlock, false, rowTracker, cellTracker);
 		rowControl.setInput(entry);
 		rowControl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		
 		rowControlMap.put(entry, rowControl);
 	}
 
 	/**
 	 * Sets or clears the error message.
 	 * If not <code>null</code>, the OK button is disabled.
 	 * 
 	 * @param errorMessage
 	 *            the error message, or <code>null</code> to clear
 	 */
 	public void updateErrorMessage() {
 		String errorMessage = null;
 
 		try {
 			BaseEntryRowControl.baseValidation(topEntry.getTransaction());
 			
 			// No exception was thrown, so transaction is valid.
 			
 			// If there are two currencies/commodities involved in
 			// the transaction then the exchange rate or conversion cost
 			// or net price or whatever is displayed.
 			
 			Map<Commodity, Long> amounts = new HashMap<Commodity, Long>();
 
 			for (Entry entry: transaction.getEntryCollection()) {
				Commodity commodity = entry.getCommodityInternal();
 				Long previousAmount = amounts.get(commodity);
 				if (previousAmount == null) {
 					amounts.put(commodity, entry.getAmount());
 				} else {
 					amounts.put(commodity, entry.getAmount() + previousAmount);
 				}
 			}
 
 			if (amounts.size() == 2) {
 				List<Map.Entry<Commodity, Long>> a = new ArrayList<Map.Entry<Commodity, Long>>(amounts.entrySet());
 
 				Commodity commodity1 = a.get(0).getKey();
 				Commodity commodity2 = a.get(1).getKey();
 				long amount1 = a.get(0).getValue();
 				long amount2 = a.get(1).getValue();
 
 				if (amount1 >= 0 && amount2 >=0) {
 					messageArea.updateText("A net gain has occurred!", IMessageProvider.ERROR);
 				} else if (amount1 <= 0 && amount2 <=0) {
 					messageArea.updateText("A net loss has occurred!", IMessageProvider.ERROR);
 				} else {
 					amount1 = Math.abs(amount1);
 					amount2 = Math.abs(amount2);
 					
 					long worthOfCommodity1 = amount2 * commodity1.getScaleFactor() / amount1;
 					long worthOfCommodity2 = amount1 * commodity2.getScaleFactor() / amount2;
 					
 					String message = MessageFormat.format(Messages.TransactionDialog_Message,
 							commodity1.getName(),
 							commodity2.getName(),
 							commodity2.format(worthOfCommodity1),
 							commodity1.format(worthOfCommodity2)
 					);
 					messageArea.updateText(message, IMessageProvider.INFORMATION);
 				}
 			} else {
 //				messageArea.clearErrorMessage();    ?????
 				messageArea.restoreTitle();
 			}
 		} catch (InvalidUserEntryException e) {
 			errorMessage = e.getLocalizedMessage();
 			messageArea.updateText(errorMessage, IMessageProvider.ERROR);
 		}
 		
 		// If called during createDialogArea, the okButton
 		// will not have been created yet.
 		Button okButton = this.getButton(IDialogConstants.OK_ID);
 		if (okButton != null) {
 			okButton.setEnabled(errorMessage == null);
 		}
 	}
 
 }
