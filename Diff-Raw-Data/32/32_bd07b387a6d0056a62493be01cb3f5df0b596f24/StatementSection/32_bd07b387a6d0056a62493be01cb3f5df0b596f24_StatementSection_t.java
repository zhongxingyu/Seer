 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
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
 
 package net.sf.jmoney.reconciliation.reconcilePage;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.entrytable.BalanceColumn;
 import net.sf.jmoney.entrytable.BaseEntryRowControl;
 import net.sf.jmoney.entrytable.Block;
 import net.sf.jmoney.entrytable.ButtonCellControl;
 import net.sf.jmoney.entrytable.CellBlock;
 import net.sf.jmoney.entrytable.DebitAndCreditColumns;
 import net.sf.jmoney.entrytable.EntriesTable;
 import net.sf.jmoney.entrytable.EntryData;
 import net.sf.jmoney.entrytable.EntryRowControl;
 import net.sf.jmoney.entrytable.HorizontalBlock;
 import net.sf.jmoney.entrytable.ICellControl;
 import net.sf.jmoney.entrytable.IEntriesContent;
 import net.sf.jmoney.entrytable.IRowProvider;
 import net.sf.jmoney.entrytable.IndividualBlock;
 import net.sf.jmoney.entrytable.OtherEntriesButton;
 import net.sf.jmoney.entrytable.PropertyBlock;
 import net.sf.jmoney.entrytable.ReusableRowProvider;
 import net.sf.jmoney.entrytable.RowSelectionTracker;
 import net.sf.jmoney.entrytable.SingleOtherEntryPropertyBlock;
 import net.sf.jmoney.entrytable.SplitEntryRowControl;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.EntryInfo;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.model2.TransactionInfo;
 import net.sf.jmoney.reconciliation.BankStatement;
 import net.sf.jmoney.reconciliation.ReconciliationEntryInfo;
 import net.sf.jmoney.reconciliation.ReconciliationPlugin;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.ui.forms.SectionPart;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Class implementing the section containing the reconciled
  * entries for a statement on the account reconciliation page.
  * 
  * @author Nigel Westbury
  */
 public class StatementSection extends SectionPart {
 
     ReconcilePage fPage;
 
     EntriesTable fReconciledEntriesControl;
     
 	/**
 	 * Contains two controls:
 	 * - noStatementMessage
 	 * - containerOfTableAndButtons
 	 * Layout: AlternativeLayout
 	 */
 	Composite container;
     
     /**
      * Control for the text that is displayed when no session
      * is open.
      */
     Label noStatementMessage;
     
     FormToolkit toolkit;
     
     IEntriesContent reconciledTableContents = null;
 
     ArrayList<CellBlock<EntryData, EntryRowControl>> cellList;
     
     long openingBalance = 0;
     
    public StatementSection(ReconcilePage page, Composite parent, RowSelectionTracker<EntryRowControl> rowTracker) {
         super(parent, page.getManagedForm().getToolkit(), Section.TITLE_BAR);
         getSection().setText("Entries Shown on Statement");
         fPage = page;
     	this.toolkit = page.getManagedForm().getToolkit();
     	
         container = toolkit.createComposite(getSection());
 
         reconciledTableContents = new IEntriesContent() {
 			public Collection<Entry> getEntries() {
 		        Vector<Entry> requiredEntries = new Vector<Entry>();
 
 		        // If no statement is set, return an empty collection.
 		        // The table will not be visible in this situation, but
 		        // this method will be called and so we must allow for
 		        // this situation.
 		        if (fPage.getStatement() == null) {
 		        	return requiredEntries;
 		        }
 		        
 				/* The caller always sorts, so there is no point in us returning
 				 * sorted results.  It may be at some point we decide it is more
 				 * efficient to get the database to sort for us, but that would
 				 * only help the first time the results are fetched, it would not
 				 * help on a re-sort.  It also only helps if the database indexes
 				 * on the date.		
 				CurrencyAccount account = fPage.getAccount();
 		        Collection<Entry> accountEntries = 
 		        	account
 						.getSortedEntries(TransactionInfo.getDateAccessor(), false);
 				*/
 				Collection<Entry> accountEntries = fPage.getAccount().getEntries();
 
 		        for (Entry entry: accountEntries) {
 		        	BankStatement statement = entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 		        	if (fPage.getStatement().equals(statement)) {
 		        		requiredEntries.add(entry);
 		        	}
 		        }
 		        
 		        return requiredEntries;
 			}
 
 			public boolean isEntryInTable(Entry entry) {
 		        // If no statement is set, nothing is in the table.
 		        // The table will not be visible in this situation, but
 		        // this method will be called and so we must allow for
 		        // this situation.
 		        if (fPage.getStatement() == null) {
 		        	return false;
 		        }
 		        
 				// This entry is to be shown if both the account and
 				// the statement match.
 	        	BankStatement statement = entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 				return fPage.getAccount().equals(entry.getAccount())
 	        	 && fPage.getStatement().equals(statement);
 			}
 
 			public boolean filterEntry(EntryData data) {
 				// No filter here, so entries always match
 				return true;
 			}
 
 			public long getStartBalance() {
 				return openingBalance;
 			}
 
 			public Entry createNewEntry(Transaction newTransaction) {
 				Entry entryInTransaction = newTransaction.createEntry();
 				Entry otherEntry = newTransaction.createEntry();
 
 				setNewEntryProperties(entryInTransaction);
 
 				// TODO: See if this code has any effect, and
 				// should this be here at all?
 				/*
 				 * We set the currency by default to be the currency of the
 				 * top-level entry.
 				 * 
 				 * The currency of an entry is not applicable if the entry is an
 				 * entry in a currency account or an income and expense account
 				 * that is restricted to a single currency.
 				 * However, we set it anyway so the value is there if the entry
 				 * is set to an account which allows entries in multiple currencies.
 				 * 
 				 * It may be that the currency of the top-level entry is not
 				 * known. This is not possible if entries in a currency account
 				 * are being listed, but may be possible if this entries list
 				 * control is used for more general purposes. In this case, the
 				 * currency is not set and so the user must enter it.
 				 */
 				if (entryInTransaction.getCommodity() instanceof Currency) {
 					otherEntry.setIncomeExpenseCurrency((Currency)entryInTransaction.getCommodity());
 				}
 				
 				return entryInTransaction;
 			}
 			
 			private void setNewEntryProperties(Entry newEntry) {
 				// It is assumed that the entry is in a data manager that is a direct
 				// child of the data manager that contains the account.
 				TransactionManager tm = (TransactionManager)newEntry.getDataManager();
 				newEntry.setAccount(tm.getCopyInTransaction(fPage.getAccount()));
 
 				newEntry.setPropertyValue(ReconciliationEntryInfo.getStatementAccessor(), fPage.getStatement());
 			}
         };
         
 		// We manage the layout of 'container' ourselves because we want either
 		// the 'containerOfTableAndButtons' to be visible or the 'no statement'
 		// message to be visible.  There is no suitable layout for
 		// this.  Therefore clear out the layout manager.
         container.setLayout(null);
 
 		// Create the control that will be visible if no session is open
 		noStatementMessage = new Label(container, SWT.WRAP);
 		noStatementMessage.setText(ReconciliationPlugin.getResourceString("EntriesSection.noStatementMessage"));
 		noStatementMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
 
 		// Load the 'unreconcile' indicator
 		URL installURL = ReconciliationPlugin.getDefault().getBundle().getEntry("/icons/unreconcile.gif");
 		final Image unreconcileImage = ImageDescriptor.createFromURL(installURL).createImage();
 		parent.addDisposeListener(new DisposeListener(){
 			public void widgetDisposed(DisposeEvent e) {
 				unreconcileImage.dispose();
 			}
 		});
 		
 		CellBlock<EntryData, EntryRowControl> unreconcileButton = new CellBlock<EntryData, EntryRowControl>(20, 0) {
 
 			@Override
 			public ICellControl<EntryData> createCellControl(Composite parent, final EntryRowControl rowControl) {
 				ButtonCellControl cellControl = new ButtonCellControl(rowControl, unreconcileImage, "Remove Entry from this Statement") {
 					@Override
 					protected void run(EntryRowControl rowControl) {
 						unreconcileEntry(rowControl);
 					}
 				};
 
 				// Allow entries to be dropped in the statement table in the account to be moved from the unreconciled list
 				final DropTarget dropTarget = new DropTarget(cellControl.getControl(), DND.DROP_MOVE);
 
 				// Data is provided using a local reference only (can only drag and drop
 				// within the Java VM)
 				Transfer[] types = new Transfer[] { LocalSelectionTransfer.getTransfer() };
 				dropTarget.setTransfer(types);
 
 				dropTarget.addDropListener(new DropTargetAdapter() {
 
 					@Override
 					public void dragEnter(DropTargetEvent event) {
 						/*
 						 * We want to check what is being dragged, in case it is not an
 						 * EntryData object.  Unfortunately this is not available on all platforms,
 						 * only on Windows.  The following call to the nativeToJava method will
 						 * return the ISelection object on Windows but null on other platforms.
 						 * If we get null back, we assume the drop is valid.
 						 */
 						ISelection selection = (ISelection)LocalSelectionTransfer.getTransfer().nativeToJava(event.currentDataType);
 						if (selection == null) {
 							// The selection cannot be determined on this platform - accept the drop
 							return;
 						}
 						
 						if (selection instanceof StructuredSelection) {
 							StructuredSelection structured = (StructuredSelection)selection;
 							if (structured.size() == 1
 									&& structured.getFirstElement() instanceof Entry) {
 								// We do want to accept the drop
 								return;
 							}
 						}
 
 						// we don't want to accept drop
 						event.detail = DND.DROP_NONE;
 					}
 
 					@Override
 					public void dragLeave(DropTargetEvent event) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void dragOperationChanged(DropTargetEvent event) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void dragOver(DropTargetEvent event) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void drop(DropTargetEvent event) {
 						if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
 							ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
 							Entry sourceEntry = (Entry)((StructuredSelection)selection).getFirstElement();
 							EntryRowControl dropRow = rowControl;
 
 							/*
 							 * Merge data from dragged transaction into the target transaction
 							 * and delete the dragged transaction.
 							 */
 							boolean success = mergeTransaction(sourceEntry, dropRow);
 							if (!success) {
 								event.detail = DND.DROP_NONE;
 							} else {
 								dropRow.commitChanges("Merge Entries");
 							}
 						}
 					}
 
 					@Override
 					public void dropAccept(DropTargetEvent event) {
 						// TODO Auto-generated method stub
 					}
 				});
 
 				fReconciledEntriesControl.getControl().addDisposeListener(new DisposeListener() {
 					public void widgetDisposed(DisposeEvent e) {
 						dropTarget.dispose();
 					}
 				});
 
 
 				return cellControl;
 			}
 
 			@Override
 			public void createHeaderControls(Composite parent, EntryData entryData) {
 				/*
 				 * All CellBlock implementations must create a control because
 				 * the header and rows must match. Maybe these objects could
 				 * just point to the header controls, in which case this would
 				 * not be necessary.
 				 * 
 				 * Note also we use Label, not an empty Composite, because we
 				 * don't want a preferred height that is higher than the labels.
 				 */
 				new Label(parent, SWT.NONE);
 			}
 		};
 		 
 		IndividualBlock<EntryData, Composite> transactionDateColumn = PropertyBlock.createTransactionColumn(TransactionInfo.getDateAccessor());
 		CellBlock<EntryData, BaseEntryRowControl> debitColumnManager = DebitAndCreditColumns.createDebitColumn(fPage.getAccount().getCurrency());
 		CellBlock<EntryData, BaseEntryRowControl> creditColumnManager = DebitAndCreditColumns.createCreditColumn(fPage.getAccount().getCurrency());
 		CellBlock<EntryData, BaseEntryRowControl> balanceColumnManager = new BalanceColumn(fPage.getAccount().getCurrency());
 
 		/*
 		 * Setup the layout structure of the header and rows.
 		 */
 		Block<EntryData, EntryRowControl> rootBlock = new HorizontalBlock<EntryData, EntryRowControl>(
 				unreconcileButton,
 				transactionDateColumn,
 				PropertyBlock.createEntryColumn(EntryInfo.getValutaAccessor()),
 				PropertyBlock.createEntryColumn(EntryInfo.getCheckAccessor()),
 				PropertyBlock.createEntryColumn(EntryInfo.getMemoAccessor()),
 				new OtherEntriesButton(
 						new HorizontalBlock<Entry, SplitEntryRowControl>(
 								new SingleOtherEntryPropertyBlock(EntryInfo.getAccountAccessor()),
 								new SingleOtherEntryPropertyBlock(EntryInfo.getMemoAccessor(), JMoneyPlugin.getResourceString("Entry.description")),
 								new SingleOtherEntryPropertyBlock(EntryInfo.getAmountAccessor())
 						)
 				),
 				debitColumnManager,
 				creditColumnManager,
 				balanceColumnManager
 		);
 		
 		// Create the table control.
 	    IRowProvider rowProvider = new ReusableRowProvider(rootBlock);
        fReconciledEntriesControl = new EntriesTable<EntryData>(container, toolkit, rootBlock, reconciledTableContents, rowProvider, fPage.getAccount().getSession(), transactionDateColumn, rowTracker) {
			@Override
			protected EntryData createEntryRowInput(Entry entry) {
				return new EntryData(entry, session.getDataManager());
			}

			@Override
			protected EntryData createNewEntryRowInput() {
				return new EntryData(null, session.getDataManager());
			}
        }; 
         
 		// TODO: do not duplicate this.
 		if (fPage.getStatement() == null) {
 			noStatementMessage.setSize(container.getSize());
 			//fReconciledEntriesControl.getControl().setSize(0, 0);
 			fReconciledEntriesControl.setSize(0, 0);
 		} else {
 			noStatementMessage.setSize(0, 0);
 			//fReconciledEntriesControl.getControl().setSize(containerOfEntriesControl1.getSize());
 			fReconciledEntriesControl.setSize(container.getSize());
 			fReconciledEntriesControl.layout(true);  // ??????
 		}
         
 		// There is no layout set on the navigation view.
 		// Therefore we must listen for changes to the size of
 		// the navigation view and adjust the size of the visible
 		// control to match.
 		container.addControlListener(new ControlAdapter() {
 			@Override
 			public void controlResized(ControlEvent e) {
 				if (fPage.getStatement() == null) {
 					noStatementMessage.setSize(container.getSize());
 					fReconciledEntriesControl.setSize(0, 0);
 				} else {
 					noStatementMessage.setSize(0, 0);
 					fReconciledEntriesControl.setSize(container.getSize());
 					fReconciledEntriesControl.layout(true);  // ??????
 				}
 			}
 		});
 
         getSection().setClient(container);
         toolkit.paintBordersFor(container);
         refresh();
     }
 
 	public void unreconcileEntry(EntryRowControl rowControl) {
 		Entry entry = rowControl.getUncommittedTopEntry();
 		
 		// If the blank new entry row, entry will be null.
 		// We must guard against that.
 		
 // TODO: How do we handle the blank row?
 		
 		// The EntriesTree control will always validate and commit
 		// any outstanding changes before firing a default selection
 		// event.  We set the property to take the entry out of the
 		// statement and immediately commit the change.
 		if (entry != null) {
 			entry.setPropertyValue(ReconciliationEntryInfo.getStatementAccessor(), null);
 			
 			/*
 			 * We tell the row control to commit its changes. These changes
 			 * include the above change. They may also include prior changes
 			 * made by the user.
 			 * 
 			 * The tables and controls in this editor should all be capable of
 			 * updating themselves correctly when the change is committed. There
 			 * is a listener that is listening for changes to the committed data
 			 * and this listener should ensure all is updated appropriately,
 			 * just as though the change came from outside this view. However,
 			 * we must go through the row control to commit the changes. This
 			 * ensures that the row control knows that its changes are being
 			 * committed and it does not get confused when the listener
 			 * processes the changes.
 			 */
 			rowControl.commitChanges("Unreconcile Entry");
 		}
 	}
 	
 	/**
 	 * Merge data from other transaction.
 	 * 
 	 * The normal case is that the reconciled transaction was imported from the
 	 * bank and the statement being merged into this transaction has been
 	 * manually edited. We therefore take the following properties from the
 	 * reconciled transaction:
 	 * 
 	 * <UL>
 	 * <LI>the valuta date</LI>
 	 * <LI>the amount</LI>
 	 * <LI>the check number</LI>
 	 * </UL>
 	 * 
 	 * And we take all other properties and all the other entries from the
 	 * source transaction.
 	 * 
 	 * However, if any property is null in one transaction and non-null in the
 	 * other then we use the non-null property.
 	 * 
 	 * If any of the following conditions apply in the target transaction then
 	 * we give a warning to the user. These conditions indicate that there is
 	 * data in the target transaction that will be lost and that information was
 	 * probably manually entered.
 	 * 
 	 * <UL>
 	 * <LI>the transaction has split entries</LI>
 	 * <LI>there are properties set in the other entry other that the required
 	 * account, or the account in the other entry is not the default account</LI>
 	 * </UL>
 	 * 
 	 * The source and targets will be in different transaction managers. We want
 	 * to take the properties from the source transaction and move them into the
 	 * target transaction without committing the target transaction. The target
 	 * row control should update itself automatically because the controls
 	 * always listen for property changes in the model.
 	 * 
 	 * The source is deleted, with any uncommitted changes being dropped.
 	 * 
 	 * The merge constitutes a single undoable action.
 	 * 
 	 * @return true if the merge succeeded, false if it failed (in which case
 	 *         the user is notified by this method of the failure)
 	 */
 	boolean mergeTransaction(Entry committedSourceEntry, EntryRowControl recRowControl) {
 
 		// The target entry, which is in the transaction manager for the target row.
 		Entry targetEntry = recRowControl.getUncommittedTopEntry();
 		
 		TransactionManager transactionManager = (TransactionManager)targetEntry.getDataManager();
 		Entry sourceEntry = transactionManager.getCopyInTransaction(committedSourceEntry); 
 		Transaction sourceTransaction = sourceEntry.getTransaction();
 		Transaction targetTransaction = targetEntry.getTransaction();
 		
 		Entry targetOther = targetTransaction.getOther(targetEntry);
 		if (targetOther == null) {
 	        MessageBox diag = new MessageBox(this.getSection().getShell(), SWT.YES | SWT.NO);
 	        diag.setText("Warning");
 	        diag.setMessage("The target entry has split entries.  These entries will be replaced by the data from the transaction for the dragged entry.  The split entries will be lost.  Are you sure you want to do this?");
 	        if (diag.open() != SWT.YES) {
 	        	return false;
 	        }
 		}
 
 		// Delete all entries in the target except the entry in the account.
 		for (Entry entry: targetTransaction.getEntryCollection()) {
 			if (entry != targetEntry) {
 				targetTransaction.getEntryCollection().deleteEntry(entry);
 			}
 		}
 		
 		/*
 		 * Set the transaction date to be the earlier of the two dates. If the
 		 * unreconciled entry was manually entered then that date is likely to
 		 * be the correct date of the transaction. However, it is possible that
 		 * this transaction involves more than one account that can be imported
 		 * (such as a transfer). In such a situation, the transaction date
 		 * should be set to the earliest date.
 		 */
 		Date sourceDate = sourceTransaction.getDate();
 		Date targetDate = targetTransaction.getDate();
 		System.out.println("source date: " + sourceDate.toLocaleString() + ", target date: " + targetDate.toLocaleString());
 		if (sourceDate.compareTo(targetDate) < 0) {
 			targetTransaction.setDate(sourceDate);
 		}
 		
 		if (targetEntry.getAmount() != sourceEntry.getAmount()) {
 	        MessageBox diag = new MessageBox(this.getSection().getShell(), SWT.YES | SWT.NO);
 	        diag.setText("Warning");
 	        diag.setMessage(
 	        		"The target entry has an amount of " 
 	        		+ EntryInfo.getAmountAccessor().formatValueForMessage(targetEntry) 
 	        		+ " and the dragged entry has an amount of " 
 	        		+ EntryInfo.getAmountAccessor().formatValueForMessage(sourceEntry) 
 	        		+ "."
 	        		+ "These amounts should normally be equal.  It may be that the incorrect amount was originally entered for the dragged entry and you want the amount corrected to the amount given by the import.  If so, continue and the amount will be corrected.  Do you want to continue?");
 	        if (diag.open() != SWT.YES) {
 	        	return false;
 	        }
 		}
 
 		/*
 		 * All other properties are taken from the target transaction only if
 		 * the property is null in the source transaction.
 		 */
 		for (ScalarPropertyAccessor<?> propertyAccessor: EntryInfo.getPropertySet().getScalarProperties3()) {
 			copyPropertyConditionally(propertyAccessor, sourceEntry, targetEntry);
 		}
 		
 		/*
 		 * Re-parent all the other entries from the source to the target.
 		 */
 		for (Entry entry: sourceTransaction.getEntryCollection()) {
 			if (entry != sourceEntry) {
 				// Cannot currently move within a transaction, so copy for time being.
 //				targetTransaction.getEntryCollection().moveElement(entry);
 				
 				Entry targetOtherEntry = targetTransaction.createEntry();
 				for (ScalarPropertyAccessor<?> propertyAccessor: EntryInfo.getPropertySet().getScalarProperties3()) {
 					copyPropertyForcibly(propertyAccessor, entry, targetOtherEntry);
 				}
 			}
 		}
 		
 		/*
 		 * Having copied the relevant properties from the source entry, we now
 		 * delete the source entry. with normal drag-and-drop design, it is the
 		 * responsibility of the drag source listener to delete the source
 		 * object. However, we want to do this as part of this transaction. This
 		 * ensures that:
 		 * 
 		 * 1. The entire merge process is performed as a single undoable
 		 * operation.
 		 * 
 		 * 2. The target entry will become the selected entry and may not
 		 * necessarily be valid. The user may attempt to leave the entry while
 		 * it is invalid, and then press 'cancel' to back out the changes. It
 		 * such a case, the source entry should re-appear. If the source entry
 		 * was deleted as a part of this transaction then it will automatically
 		 * re-appear when the transaction is rolled back.
 		 * 
 		 * It may be that the source entry may have uncommitted changes, or even
 		 * was a new entry (one that had just been entered and has never been
 		 * committed to the datastore). Such uncommitted changes are considered
 		 * part of the same operation as far as undo or cancel support is
 		 * concerned.
 		 * 
 		 * The drag source does, however, have the responsibility of some UI cleanup.
 		 * If the source was a new uncommitted entry then it should be cleared to
 		 * become an empty 'new entry' row.  If the source entry has uncommitted changes,
 		 * these should be reversed in the UI so that the user does not get warnings
 		 * when the committed entry is deleted.
 		 */
 		sourceTransaction.getSession().deleteTransaction(sourceTransaction);
 		
 		return true;
 	}
 
 	/**
 	 * Helper method to copy a property from the target entry to the source entry if the
 	 * property is null in the source entry but not null in the target entry.
 	 */
 	private <V> void copyPropertyConditionally(ScalarPropertyAccessor<V> propertyAccessor, Entry sourceAccount, Entry targetAccount) {
 		V targetValue = targetAccount.getPropertyValue(propertyAccessor);
 		V sourceValue = sourceAccount.getPropertyValue(propertyAccessor);
 		if (sourceValue != null) {
 			if (targetValue == null
 					|| !doesImportedValueHavePriority(propertyAccessor)) {
 				targetAccount.setPropertyValue(propertyAccessor, sourceValue);
 			}
 		}
 	}
 
 	private <V> void copyPropertyForcibly(ScalarPropertyAccessor<V> propertyAccessor, Entry sourceEntry, Entry targetEntry) {
 		V sourceValue = sourceEntry.getPropertyValue(propertyAccessor);
 		targetEntry.setPropertyValue(propertyAccessor, sourceValue);
 	}
 
 	/**
 	 * Properties for which the values from the bank are used in preference to
 	 * values manually entered.  For all other properties, manually entered values
 	 * take precedence.  This is used when merging transactions.
 	 */
 	static private boolean doesImportedValueHavePriority(ScalarPropertyAccessor propertyAccessor) {
 		return propertyAccessor == EntryInfo.getValutaAccessor()
 		|| propertyAccessor == EntryInfo.getAmountAccessor()
 		|| propertyAccessor == EntryInfo.getCheckAccessor();
 	}
 	
 	
 	/**
 	 * @param statement
 	 * @param openingBalance
 	 */
 	public void setStatement(BankStatement statement, long openingBalance) {
 		// Statement will already have been set in fPage,
 		// but we must save the opening balance for ourselves.
 		this.openingBalance = openingBalance;
 		
     	fReconciledEntriesControl.refreshEntryList();
 
     	// TODO: this should be automatic from the above, i.e.
     	// content provider should tell the table?
     	fReconciledEntriesControl.table.refreshContent();
     	
 		if (statement == null) {
 	        getSection().setText("Entries Shown on Statement");
 	        refresh();  // Must refresh to see new section title
 
 	        noStatementMessage.setSize(container.getSize());
 	        fReconciledEntriesControl.setSize(0, 0);
 		} else {
 			getSection().setText("Entries Shown on Statement " + statement.toLocalizedString());
 	        getSection().layout(true);  // Required to get the new section title to show
 
 	        noStatementMessage.setSize(0, 0);
 	        fReconciledEntriesControl.setSize(container.getSize());
 	        fReconciledEntriesControl.layout(true);  // ??????
 		}
 	}
 }
