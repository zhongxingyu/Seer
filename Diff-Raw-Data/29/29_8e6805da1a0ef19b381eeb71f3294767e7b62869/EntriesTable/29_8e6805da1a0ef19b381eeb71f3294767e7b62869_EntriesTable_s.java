 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2002 Johann Gyger <johann.gyger@switzerland.org>
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
 
 package net.sf.jmoney.entrytable;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.AccountInfo;
 import net.sf.jmoney.model2.CommodityInfo;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.pages.entries.EntryRowSelectionListener;
 import net.sf.jmoney.pages.entries.TransactionDialog;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * Class that displays a list of entries in a table. The table contains one row
  * control for each entry in the list.
  * <P>
  * Note that it is possible that one transaction may contain two or more entries
  * in the same account. For example, a user may write a check for a deposit and
  * a check for the balance of a purchase. The user may enter this as a single
  * 'split' transaction containing the two checks and a single entry containing
  * the total purchase amount in an expense category. In this situation, the
  * single transaction will have two top-level rows (one for each check).
  * Expanding one of the rows with show two child entries: the other check and
  * the purchase entry.
  * <P>
  * A simple transaction is a transaction that contains two entries where one
  * entry is in a capital account and the other entry is in an income and expense
  * account. This is a common form of transaction and therefore we make a special
  * effort to display such transactions on a single row.
  * <P>
  * Some of the entry properties apply only to entries in capital accounts, some
  * apply only to entries in income and expense accounts, and some apply to
  * entries in both types of accounts. In a simple transaction, all the
  * properties from both entries have a column. Properties from the transaction
  * are displayed in the top level and also have a column. So, we have a column
  * for all the following:
  * <UL>
  * <LI>Every property in the transaction</LI>
  * <LI>Every property in an entry that may be applicable given the account
  * being listed, with the exception that if all the entries are in the same
  * account (true in most uses of this class) then no column exists for the
  * account property (as such a column would contain the same account in every
  * row and therefore not be of much use)</LI>
  * <LI>Every property in an entry that may be applicable when the entry is in
  * an income and expense account</LI>
  * </UL>
  * 
  * Some properties may be applicable for both entries in the capital account and
  * for entries in income and expense accounts. There will be two columns for
  * such properties. When an entry is being show on its own child row, we have
  * the choice of which of the two columns we use for the property. We chose to
  * show it in the column that would, for a simple transaction, be used to show
  * the property of the entry in the income and expense account. This makes the
  * child rows look more similar and also ensures that a transfer account is
  * shown.
  * <P>
  * Each column is managed by an IEntriesTableProperty object. Each row is
  * managed by an EntryData object. These two classes
  * must work together to determine the contents of a cell.
  * <P>
  * The credit, debit, and balance columns are special cases and special
  * implementations of IEntriesTableProperty handle those three columns. The
  * other columns fall into one of the above three categories. The rest of this
  * explanation applies only to the latter class of columns.
  * <P>
  * A request for cell contents (whether for displaying text or for creating a
  * cell editor) goes first to the IEntriesTableProperty object. That object then
  * gets data from the EntryData object to create and load the control for
  * the cell. As the IEntriesTableProperty object has the property accessor,
  * the property value associated with the cell can be got and set.
  */
 // TODO: make this not abstract but instead move the abstract methods into the
 // content providers?????
 public abstract class EntriesTable<T extends EntryData> extends Composite {
 
 	protected Session session;
 	
 	protected IEntriesContent entriesContent;
 	
 	public VirtualRowTable<T> table;
 	
 	/**
 	 * List of entries to show in the table. Only the top level entries are
 	 * included, the other entries in the transactions, which are shown as child
 	 * items, are not in this list. The elements are not sorted.
 	 */
 	Map<Entry, T> entries;
 
 	/**
 	 * The 'new entry' row, being an extra blank row at the bottom of the table that
 	 * the user can use to enter new entries.
 	 */
 	T newEntryRow;
 	
 	/**
 	 * The comparator that sorts entries according to the current sort order.
 	 */
 	Comparator<EntryData> rowComparator;
 	
 	/**
 	 * The entries in sorted order.  This list contains the same
 	 * items that are in the entries map.
 	 */
 	List<T> sortedEntries;
 	
 	/**
 	 * Set of listeners for selection changes
 	 */
 	private Vector<EntryRowSelectionListener> selectionListeners = new Vector<EntryRowSelectionListener>();
 
 	public EntriesTable(Composite parent, FormToolkit toolkit, Block rootBlock, 
 			final IEntriesContent entriesContent, IRowProvider<T> rowProvider, final Session session, IndividualBlock<EntryData, ?> defaultSortColumn, final RowSelectionTracker<EntryRowControl> rowTracker) {
 		super(parent, SWT.NONE);
 		
 		this.session = session;
 
 		toolkit.adapt(this, true, false);
 
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		setLayout(layout);
 		
 		/*
 		 * Ensure indexes are set.
 		 */
 		rootBlock.initIndexes(0);
 		
 		this.entriesContent = entriesContent;
 
 		// Fetch and sort the list of top level entries to display.
 		buildEntryList();
 
 		newEntryRow = createNewEntryRowInput();
 		
 	    /*
 		 * Build the initial sort order. This must be done before we can create
 		 * the composite table because the constructor for the composite table
 		 * requires a row content provider.
 		 */
 		rowComparator = new RowComparator(defaultSortColumn, true);
 	    sort();
 
 	    IContentProvider<T> contentProvider = new IContentProvider<T>() {
 
 			public int getRowCount() {
 				return sortedEntries.size();
 			}
 
 			public T getElement(int rowNumber) {
 				return sortedEntries.get(rowNumber); 
 			}
 
 			public int indexOf(EntryData entryData) {
 				return sortedEntries.indexOf(entryData);
 			}
 	    };
 	    
 		table = new VirtualRowTable<T>(this, rootBlock, this, contentProvider, rowProvider, rowTracker);
 		
 		/*
 		 * Use a single cell focus tracker for this table. The row focus tracker
 		 * is passed to this object because it may be shared with other tables
 		 * (thus forcing a single row selection for two or more tables).
 		 */
 	    FocusCellTracker cellTracker = new FocusCellTracker();
 
 	    rowProvider.init(table, rowTracker, cellTracker);
 		
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gridData.heightHint = 100;
 		gridData.widthHint = 100;
 		table.setLayoutData(gridData);
 
         // Create the button area
 		Composite buttonArea = toolkit.createComposite(this);
 		
 		// Note that the buttons touch each other and also touch
 		// the table above.  This makes it clearer that the buttons
 		// are tightly associated with the table.
 		RowLayout layoutOfButtons = new RowLayout();
 		layoutOfButtons.fill = false;
 		layoutOfButtons.justify = true;
 		layoutOfButtons.marginTop = 0;
 		layoutOfButtons.spacing = 5;
 		buttonArea.setLayout(layoutOfButtons);
 		
         // Create the 'add transaction' button.
 		Button addButton = toolkit.createButton(buttonArea, "New Transaction", SWT.PUSH);
 		addButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				/*
 				 * This action is not absolutely necessary because there is
 				 * always an empty row at the end of the table that can be used
 				 * to add a new transaction.
 				 * 
 				 * This action is helpful, though, because it does one of two
 				 * things:
 				 * 
 				 * 1. If the 'new entry' row is not the currently selected row
 				 * then it sets the 'new entry' row to be the selected row,
 				 * scrolling if necessary.
 				 * 
 				 * 2. If the 'new entry' row is the currently selected row then
 				 * the entry is committed and, if the commit succeeded, the
 				 * newly created 'new entry' row is selected.
 				 */
				EntryRowControl selectedRowControl = rowTracker.getSelectedRow();
 				if (selectedRowControl != null) {
					Entry selectedEntry = selectedRowControl.committedEntryData.getEntry();
					if (selectedEntry == null) {
						/*
						 * The 'new entry' row is currently selected.  We attempt to
						 * commit it first.
						 */
						selectedRowControl.commitChanges();
 					}
 				}
 				
 				/*
 				 * Regardless of whether the 'new entry' row was previously
 				 * selected or not, and regardless of whether any attempt to
 				 * commit the row failed, we select what is now the 'new entry'
 				 * row.
 				 */
 				// TODO: Is this method name confusing?
 				table.getRowControl(newEntryRow);
 			}
 		});
 
         // Create the 'duplicate transaction' button.
         Button duplicateButton = toolkit.createButton(buttonArea, "Duplicate Transaction", SWT.PUSH);
         duplicateButton.addSelectionListener(new SelectionAdapter() {
 			@Override
         	public void widgetSelected(SelectionEvent event) {
         		EntryRowControl selectedRowControl = rowTracker.getSelectedRow();
         		
         		if (selectedRowControl != null) {
         			Entry selectedEntry = selectedRowControl.committedEntryData.getEntry();
 
         			if (selectedEntry == null) {
         				// This is the empty row control.
         				// TODO: Should we attempt to commit this first, then duplicate it if it committed?
         				MessageDialog.openInformation(getShell(), "No Selection", "You must first select the entry you want to duplicate. (The current selection is a new entry that has not yet been registered).");
         			} else {
         				/*
 						 * The 'new entry' row was not the selected row, so we
 						 * know that there can't be any changes in the 'new
 						 * entry' row. We copy properties from the selected
 						 * entry into the new 'entry row' and then select the
 						 * 'new entry' row.
 						 */
 
         				BaseEntryRowControl newEntryRowControl = table.getRowControl(newEntryRow);
         				Entry newEntry = newEntryRowControl.uncommittedEntryData.getEntry();
         				TransactionManager transactionManager = (TransactionManager)newEntry.getDataManager();
         				
         				newEntry.setMemo(selectedEntry.getMemo());
         				newEntry.setAmount(selectedEntry.getAmount());
         				
         				Entry thisEntry = newEntryRowControl.uncommittedEntryData.getOtherEntry();
         				for (Entry origEntry: selectedRowControl.committedEntryData.getSplitEntries()) {
         					if (thisEntry == null) {
         						thisEntry = newEntryRowControl.uncommittedEntryData.getEntry().getTransaction().createEntry();
         					}
             				thisEntry.setAccount(transactionManager.getCopyInTransaction(origEntry.getAccount()));
             				thisEntry.setMemo(origEntry.getMemo());
             				thisEntry.setAmount(origEntry.getAmount());
             				thisEntry = null;
         				}
         				
         				// The 'new entry' row control should be listening for changes to
         				// its uncommitted data, so we have nothing more to do. 
         			}
         		} else {
         			MessageDialog.openInformation(getShell(), "No Selection", "You must first select the entry you want to duplicate.");
         		}
         	}
         });
 
         // Create the 'delete transaction' button.
         Button deleteButton = toolkit.createButton(buttonArea, "Delete Transaction", SWT.PUSH);
         deleteButton.addSelectionListener(new SelectionAdapter() {
 			@Override
         	public void widgetSelected(SelectionEvent event) {
         		EntryRowControl selectedRowControl = rowTracker.getSelectedRow();
         		
         		if (selectedRowControl != null) {
             		Entry selectedEntry = selectedRowControl.committedEntryData.getEntry();
         			
             		if (selectedEntry == null) {
             			// This is the empty row control.
             			// TODO: Should we just clear the control contents?
             			MessageDialog.openInformation(getShell(), "No Selection", "You must first select the entry you want to delete. (The current selection is a new entry that has not yet been registered).");
             		} else {
             			// Does this need be so complex??? It is only in a transaction
             			// so we can undo it.  A more efficient way would be to make the change
             			// in a callback.
 
             			TransactionManager transactionManager = new TransactionManager(selectedEntry.getDataManager());
             			Entry selectedEntry2 = transactionManager.getCopyInTransaction(selectedEntry); 
             			Transaction transaction = selectedEntry2.getTransaction();
             			transaction.getSession().deleteTransaction(transaction);
             			transactionManager.commit("Delete Transaction");
             		}
         		} else {
         			MessageDialog.openInformation(getShell(), "No Selection", "You must first select the entry you want to delete.");
         		}
         	}
         });
         
         // Create the 'details' button.
         Button detailsButton = toolkit.createButton(buttonArea, "Details", SWT.PUSH);
         detailsButton.addSelectionListener(new SelectionAdapter() {
 			@Override
         	public void widgetSelected(SelectionEvent event) {
         		EntryRowControl selectedRowControl = rowTracker.getSelectedRow();
         		
         		if (selectedRowControl != null) {
         			Entry selectedEntry = selectedRowControl.uncommittedEntryData.getEntry();
 
         			TransactionDialog dialog = new TransactionDialog(getShell(), selectedEntry);
         			dialog.open();
         		} else {
         			MessageDialog.openInformation(getShell(), "No Selection", "You must first select the entry.");
         		}
         	}
         });
         
 		
 		session.getDataManager().addChangeListener(new SessionChangeAdapter() {
 			@Override
 			public void objectInserted(ExtendableObject newObject) {
 				if (newObject instanceof Entry) {
 					Entry newEntry = (Entry) newObject;
 					// if the entry is in this table, add it
 					if (entriesContent.isEntryInTable(newEntry)) {
 						addEntryToTable(newEntry);
 					}
 
 					// Even if this entry is not in this table, if one of
 					// the other entries in the transaction is in this table
 					// then the table view will need updating because the split
 					// entry rows will need updating.
 					for (Entry entry: newEntry.getTransaction().getEntryCollection()) {
 						if (!entry.equals(newEntry)
 								&& entries.containsKey(entry)) {
 							updateEntryInTable(entry);
 						}
 					}
 				} else if (newObject instanceof Transaction) {
 					Transaction newTransaction = (Transaction) newObject;
 					// Add all entries in the transaction that are to be listed as
 					// an entry in this list.
 					for (Entry entry: newTransaction.getEntryCollection()) {
 						if (entriesContent.isEntryInTable(entry)) {
 							addEntryToTable(entry);
 						}
 					}
 				}
 			}
 
 			@Override
 			public void objectRemoved(ExtendableObject deletedObject) {
 				if (deletedObject instanceof Entry) {
 					Entry deletedEntry = (Entry) deletedObject;
 					// if the entry is in this table, remove it.
 					if (entries.containsKey(deletedEntry)) {
 						removeEntryFromTable(deletedEntry);
 					}
 
 					// Even if this entry is not in this table, if one of
 					// the other entries in the transaction is in this table
 					// then the table view will need updating because the split
 					// entry rows will need updating.
 					for (Entry entry: deletedEntry.getTransaction().getEntryCollection()) {
 						if (!entry.equals(deletedEntry)
 								&& entries.containsKey(entry)) {
 							updateEntryInTable(entry);
 						}
 					}
 				} else if (deletedObject instanceof Transaction) {
 					Transaction deletedTransaction = (Transaction) deletedObject;
 
 					// TODO: This is not complete.  What happens if the entry
 					// with focus is in the transaction being deleted?
 					
 					for (Entry deletedEntry: deletedTransaction.getEntryCollection()) {
 						if (entries.containsKey(deletedEntry)) {
 							removeEntryFromTable(deletedEntry);
 						}
 					}
 				}
 			}
 
 			@Override
 			public void objectChanged(ExtendableObject extendableObject,
 					ScalarPropertyAccessor propertyAccessor, Object oldValue,
 					Object newValue) {
 				if (extendableObject instanceof Entry) {
 					Entry entry = (Entry) extendableObject;
 
 					/*
 					 * A property change may result in a top-level entry no
 					 * longer meeting the requirements to be listed in the
 					 * table. If the entry is not either the selected entry or
 					 * the parent of the selected entry then the changed entry
 					 * is immediately removed from the list.
 					 * 
 					 * However, if the changed entry is the selected top-level
 					 * entry or the parent of the selected child entry then we
 					 * do not remove the entry from the list. It would confuse
 					 * the user if an entry disappeared while the user was
 					 * editing the entry. The entry is instead removed when the
 					 * entry is no longer the selected entry. This may still be
 					 * a little confusing, because the entry would disappear
 					 * when the user selects another entry, but the user is
 					 * forced to commit the changes and should not be so
 					 * surprised to see that the entry is no longer in the table
 					 * once the changes are committed.
 					 * 
 					 * Note that the entry being changed may be the other entry
 					 * in the transaction, whose properties are also diplayed on
 					 * the parent row. As long as properties from the 'other'
 					 * entry never affect whether an entry is listed then this
 					 * code is correct. If, however, properties from the other
 					 * entry or properties from the transaction affect whether
 					 * an entry is listed then this code will need re-visiting.
 					 */
 					boolean wasInTable = entries.containsKey(entry);
 					boolean isNowInTable = entriesContent.isEntryInTable(entry);
 					if (wasInTable && !isNowInTable) {
 						removeEntryFromTable(entry);
 					} else if (!wasInTable && isNowInTable) {
 						addEntryToTable(entry);
 					}
 
 					/*
 					 * Find all rows on which properties of this entry are
 					 * displayed and update those rows. This involves finding
 					 * all entries in the same transaction that are listed in
 					 * the table contents (including this entry itself if this
 					 * entry is a top level entry in the table). TODO: we do not
 					 * need to include this entry itself if it were just added
 					 * above, but there is no harm in doing so.
 					 */
 					Transaction transaction = entry.getTransaction();
 					for (Entry entry2: transaction.getEntryCollection()) {
 						if (entries.containsKey(entry2)) {
 							updateEntryInTable(entry2);
 						}
 					}
 				}
 
 				// When a transaction property changes, we notify the entries list
 				// control once for each entry in the transaction where the entry is
 				// listed as a top level entry in the table.
 				// (Only rows for top level entries display transaction properties).
 				if (extendableObject instanceof Transaction) {
 					Transaction transaction = (Transaction) extendableObject;
 					
 					for (Entry entry: transaction.getEntryCollection()) {
 						if (entries.containsKey(entry)) {
 							updateEntryInTable(entry);
 						}
 					}
 				}
 
 				/*
 				 * Account names and currency names affect the data displayed in
 				 * the entries list. These changes are both infrequent, may
 				 * involve a change to a lot of entries, and would involve
 				 * finding all transactions that contain both an entry with the
 				 * changed account or currency and an entry with in the account
 				 * for this page. It is therefore better just to refresh all the
 				 * entire entries list, but note that only the text need be
 				 * refreshed.
 				 * 
 				 * TODO: Other properties added by plug-ins could potentially
 				 * affect this view too. Should we refresh on ANY property
 				 * change on ANY object class, or should we implement something
 				 * more complex?
 				 */
 				if (propertyAccessor == AccountInfo.getNameAccessor()
 						|| propertyAccessor == CommodityInfo.getNameAccessor()) {
 					table.refreshContentOfAllRows();
 				}
 			}
 
 			private void updateEntryInTable(Entry entry) {
 				T data = entries.get(entry);
 
 				/*
 				 * This change may result in the entry moving in the sort order.
 				 * We can efficiently check for this by comparing the entry
 				 * against its neighbors and moving it as appropriate. 
 				 */
 				int originalIndex = data.getIndex();
 				int index = originalIndex;
 				Assert.isTrue(index == sortedEntries.indexOf(data));
 				
 				// Bubble to up the screen (down the sort order)
 				while (index > 0) {
 					T data2 = sortedEntries.get(index-1); 
 					if (rowComparator.compare(data2, data) < 0) {
 						break;
 					}
 					sortedEntries.set(index, data2);
 					index--;
 				}
 				
 				// The current value if the index is the point from which we
 				// need to refresh the balances.
 				int balanceRefreshStartPoint = index;
 				
 				// Bubble down the screen (up the sort order)
 				while (index < entries.size() - 1) {
 					T data2 = sortedEntries.get(index+1); 
 					
 					if (rowComparator.compare(data, data2) < 0) {
 						break;
 					}
 					
 					sortedEntries.set(index, data2);
 					index++;
 				}
 
 				sortedEntries.set(index, data);
 				
 				/*
 				 * It is possible that the amount of this entry changed.
 				 * Therefore we must refresh the balances of all later
 				 * entries in the sorted list.
 				 */
 				updateFollowingValues(balanceRefreshStartPoint, data.getBalance());
 
 				if (index != originalIndex) {
 					table.refreshContent();
 				} else {
 					// TODO: Test to see if the amount has changed, and update the following
 					// rows only if so (as the balances will have changed).
 					table.refreshBalancesOfAllRows();
 				}
 			}
 
 			private void removeEntryFromTable(Entry entry) {
 				EntryData data = entries.get(entry);
 				
 				entries.remove(entry);
 
 				int indexToRemove = sortedEntries.indexOf(data); 
 				sortedEntries.remove(indexToRemove);
 				
 				// Update all the later entries
 				updateFollowingValues(indexToRemove, data.getBalance() - data.getEntry().getAmount());
 
 				table.deleteRow(indexToRemove);
 			}
 
 			private void addEntryToTable(Entry entry) {
 				T newData = createEntryRowInput(entry);
 				
 				entries.put(entry, newData);
 				
 				/*
 				 * Find the insert point in the sorted entries.
 				 * 
 				 * Scan the table to find the correct index to insert this row.
 				 * Because rows are likely to be inserted near the bottom of the
 				 * table, we scan backwards.
 				 * 
 				 * We start the search at the penultimate entry. The last entry
 				 * is the uncommitted 'new entry' row. We cannot compare against
 				 * such entries as we don't have a committed entry available,
 				 * and also we want to be sure that the 'new entry' row remains
 				 * at the end.
 				 */
 				int i = sortedEntries.size()-1;
 				while (i > 0) {
 					EntryData previousData = sortedEntries.get(i - 1);
 					if (rowComparator.compare(newData, previousData) >= 0) {
 						break;
 					}
 					i--;
 				}
 				
 				int insertIndex = i;
 				long balance = sortedEntries.get(i).getBalance();
 				
 				// Insert the entry at the appropriate place in the sorted list.
 				sortedEntries.add(insertIndex, newData);
 				
 				updateFollowingValues(insertIndex, balance);
 				
 				table.insertRow(insertIndex);
 			}
 		}, this);
 	}
 
 	protected abstract T createNewEntryRowInput();
 
 	/**
 	 * Adjust the indexes and balances of all entries that follow the given
 	 * start position in the sorted list.
 	 * 
 	 * The EntryData objects hold the committed data, so changing an amount
 	 * in an entry will not update the balances until the entry is committed.
 	 * If an EntryData object represents a new entry that has never been
 	 * committed then the Entry value will be null.  In that case the balance
 	 * is not changed by the entry.
 	 *  
 	 * @param startIndex the index of the first entry that needs updating
 	 * @param startBalance the balance prior to the given start index
 	 */
 	private void updateFollowingValues(int startIndex, long startBalance) {
 		long balance = startBalance;
 		for (int index = startIndex; index < sortedEntries.size(); index++) {
 			EntryData data = sortedEntries.get(index);
 			
 			data.setIndex(index);
 			data.setBalance(balance);
 
 			if (data.getEntry() != null) {
 				balance += data.getEntry().getAmount();
 			}
 		}
 	}
 
 	/**
 	 * Change the sort according to the given parameters.
 	 */
 	public void sort(IndividualBlock<EntryData, EntryRowControl> sortProperty, boolean sortAscending) {
 		rowComparator = new RowComparator(sortProperty, sortAscending);
 		sort();
 	}
 	
 	/**
 	 * Sort the entries according to the rowComparator field.
 	 */
 	public void sort() {
 		
 		/*
 		 * It would be efficient if we could create a sorted TreeSet and copy
 		 * the entries into that. However the TreeSet object unfortunately uses
 		 * the comparator not just as a comparator but also assumes that if two
 		 * objects compare the same then they are the same object. This is a
 		 * design flaw, as of course that is wrong. For example, if sorting by
 		 * date you cannot assume that two entries are the same simply because
 		 * they have the same date. The equals method should be used to
 		 * determine equality. Java's own documentation says that the equals
 		 * method is used to determine equality but Sun's documentation is
 		 * wrong.
 		 * 
 		 * We therefore copy the entries into a list and then sort that.
 		 */
 		sortedEntries = new ArrayList<T>();
 		sortedEntries.addAll(entries.values());
 		Collections.sort(sortedEntries, rowComparator);
 		
 		// Add an empty row at the end so that users can enter new entries.
 		sortedEntries.add(newEntryRow);
 		
         /*
          * Having sorted the entries, the indexes and balances must be updated.
          */
 		updateFollowingValues(0, entriesContent.getStartBalance());
 	}	
 
 	/**
      * Build the list of entries to be shown in the entries list. This method
      * sets the list into <code>entries</code>. This list contains only the
      * top level entries.
      * <P>
      * The entries are unsorted.
      */
     private void buildEntryList() {
         // Note that the balances are not set at this time. This is done
         // when the data is sorted.
         entries = new HashMap<Entry, T>();
         for (Entry accountEntry: entriesContent.getEntries()) {
         	T data = createEntryRowInput(accountEntry);
             if (matchesFilter(data)) {
                 entries.put(accountEntry, data);
             }
         }
     }
 
 	/**
 	 * Filters work at the transaction level, not the entry level.
 	 * Either the entire transaction is displayed, or none of
 	 * the transaction is displayed.  If any entry in a split
 	 * transaction matches the filter then the entire transaction
 	 * is shown.
 	 * 
 	 * @param transData
 	 * @return
 	 */
 	private boolean matchesFilter(EntryData transData) {
 		if (entriesContent.filterEntry(transData)) {
 			return true;
 		}
 
 		// TODO: decide if we should be searching split entries.
 /*		
 		if (!transData.isSimpleEntry()) {
 			for (Entry entry2: transData.getSplitEntries()) {
 				DisplayableEntry entryData = new DisplayableEntry(entry2,
 						transData);
 				if (entriesContent.filterEntry(entryData)) {
 					return true;
 				}
 			}
 		}
 */
 		return false;
 	}
 
 	/**
 	 * Refresh the list of entries.
 	 */
 	public void refreshEntryList() {
 		buildEntryList();
 
 		/*
 		 * Re-sort using the rowComparator field as a comparator.
 		 */
 	    sort();
 	    
 	}
 
     @Override	
 	public void dispose() {
 		table.dispose();
 	}
 /*
 	/*
 	 * Note that the transaction may have been inserted from within
 	 * this page (the user pressed the 'new transaction' button in
 	 * this page, or the transaction may have been inserted outside
 	 * this page (for example, the user imported some transactions
 	 * from a QIF file).  This method is responsible for inserting
 	 * the new data into the entries list.  It must not set the
 	 * current selection.  If the transaction is being inserted because
 	 * the user pressed the 'new transaction' button then the action
 	 * code for that button will set the transaction as the selection
 	 * after this method has created the table rows for the transaction.
 	 * /
 	public void addEntryInAccount(Entry entry) {
 		DisplayableTransaction dTrans = new DisplayableTransaction(entry, 0);
 
 		// TODO: check that caller ensures this entry is in the entries content provider.
 		
 		// Do not add this entry to our view if a filter is on and the entry does
 		// not match the filter.
 		if (!matchesFilter(dTrans)) {
 			return;
 		}
 
 		entries.put(entry, dTrans);
 
 		// Scan the table to find the correct index to insert this row.
 		// Because rows are likely to be inserted near the bottom of
 		// the table, we scan backwards.
 		Comparator<DisplayableTransaction> rowComparator = new RowComparator(sortColumn, sortAscending);
 		int parentIndex = 0;
 		long balance = entriesContent.getStartBalance();
 		for (int i = fTable.getItemCount()-1; i >= 0; i--) {
 			TreeItem item = fTable.getItem(i);
 			Object data = item.getData();
 			if (data instanceof DisplayableTransaction) {
 				DisplayableTransaction dTrans2 = (DisplayableTransaction) data;
 				if (rowComparator.compare(dTrans, dTrans2) >= 0) {
 					parentIndex = i + 1;
 					balance = dTrans2.getBalance();
 					break;
 				}
 			}
 		}
 		
 		// Insert the transaction and its entries now.
 		TreeItem parentItem = new TreeItem(fTable, 0, parentIndex);
 		parentItem.setData(dTrans);
 		
 		// Set the column values for this new row (note that the balance
 		// column is not set.  The balance column is always set
 		// later by the same code that updates all the following balances).
 		for (int columnIndex = 0; columnIndex < fTable.getColumnCount(); columnIndex++) {
 			IEntriesTableProperty p = (IEntriesTableProperty)fTable.getColumn(columnIndex).getData();
 			if (!p.getId().equals("balance")) {
 				parentItem.setText(columnIndex, p.getValueFormattedForTable(dTrans));
 			}
 		}
 
 		if (!dTrans.isSimpleEntry()) {
 			/*
 			 * Case of an splitted or double entry. We display the transaction
 			 * on the first line and the entries of the transaction on the
 			 * following ones. However, the transaction line also holds the
 			 * properties for the entry in this account, so display just the
 			 * other entries underneath.
 			 * /
 			for (Entry entry2: dTrans.getSplitEntries()) {
 				DisplayableEntry entryData = new DisplayableEntry(entry2, dTrans);
 				TreeItem childItem = new TreeItem(parentItem, SWT.NULL);
 				childItem.setData(entryData);
 				updateItem(childItem);
 			}
 		}
 
 		// Recalculate balances from this point onwards.
 		updateBalances(parentIndex, balance);
 
 		// Set colors from this point onwards (colors have switched).
 		boolean isAlternated = ((parentIndex % 2) == 0);
 		updateColors(parentIndex, isAlternated);
 	}
 
 	/**
 	 * Remove a parent entry from the table.
 	 * <P>
 	 * The caller will alread (non-Javadoc)
 	 * @see net.sf.jmoney.pages.entries.IEntriesControl#removeTransaction(net.sf.jmoney.model2.Entry)
 	 * /
 	private void removeEntryInAccount(Entry entry) {
 		// TODO: processing is done correctly for entries being added and removed from
 		// the set provided by entriesContent.  However, the filter is not kept correctly.
 		// This is a problem, because the user may edit properties that make the entry
 		// disappear because it no longer passes the filter.  How do we deal with this?
 		
 		// get the index of the row for the transaction
 		int parentIndex = lookupEntryInAccount(entry);
 
 		TreeItem parentItem = fTable.getItem(parentIndex);
 		DisplayableTransaction dTrans = (DisplayableTransaction)parentItem.getData();
 
 		// Remove from our cached set of entries
 		entries.remove(dTrans.entry);
 		
 		// Determine the color of this row.  It is used to switch the color of
 		// all the following rows.
 		boolean isAlternated = (alternateTransactionColor.equals(parentItem
 				.getBackground()));
 
 		
 		// Dispose it
 		parentItem.dispose();
 
 		long balance = dTrans.getBalance() - entry.getAmount();
 
 		// Recalculate balances from this point onwards.
 		updateBalances(parentIndex, balance);
 
 		// Set colors from this point onwards (colors have switched).
 		updateColors(parentIndex, isAlternated);
 	}
 
 	public void updateEntry(Entry entryInAccount, Entry changedEntry,
 			PropertyAccessor changedProperty, Object oldValue, Object newValue) {
 		int parentIndex = lookupEntryInAccount(entryInAccount);
 		JMoneyPlugin.myAssert(parentIndex >= 0);
 		TreeItem parentItem = fTable.getItem(parentIndex);
 		
 		// If there are two entries in the transaction
 		// and the changed entry in not the listed entry
 		// and the changed property was the account property
 		// and the account changed from being a capital account to an income and expense account
 		// or vica versa
 		// then we must add or remove a child item.
 		DisplayableTransaction dTrans = (DisplayableTransaction)parentItem.getData(); 
 		if (dTrans.otherEntries.size() == 1
 				&& !changedEntry.equals(entryInAccount)
 				&& changedProperty == EntryInfo.getAccountAccessor()) {
 			
 			if ((oldValue instanceof CapitalAccount)
 					&& !(newValue instanceof CapitalAccount)) {
 				// Remove the single child item.
 				parentItem.getItem(0).dispose();
 			}
 			if (!(oldValue instanceof CapitalAccount)
 					&& (newValue instanceof CapitalAccount)) {
 				// Add a child item.Remove the single child item.
 				TreeItem childItem = new TreeItem(parentItem, SWT.NULL);
 				childItem.setData(new DisplayableEntry(changedEntry, dTrans));
 
 				Color colorOfNewEntry = parentItem.getBackground().equals(
 						transactionColor) ? entryColor : alternateEntryColor;
 				childItem.setBackground(colorOfNewEntry);
 			}
 		}
 		
 		// If this is a double entry transaction then changing the other entry
 		// may affect the top-level row (the account, for example, is displayed
 		// in the top-level row).
 		updateItem(parentItem);
 
 		// Update the row containing the changed entry.  This may be
 		// the parent row, in which case the parent row is updated
 		// twice, but that is okay.
 		TreeItem item = lookupSplitEntry(parentItem, changedEntry);
 		updateItem(item);
 
 		// If the changed property is the sort property, the entry may
 		// need to be moved in the sort order.  This is difficult to do
 		// properly, as we do not want to mess up the user while the user
 		// is editing the row.  To move a row requires the row item to be
 		// disposed and a new row item inserted at the new position.
 		// The easiest way may be to see if this is the selected row, 
 		// and if it is, move all the rows past this row.
 		// Until this is implemented, we leave the row out of order. 
 
 		// Recalculate balances from this point onwards.
 		if (changedProperty == EntryInfo.getAmountAccessor()
 				&& entryInAccount.equals(changedEntry)) {
 
 			// Determine the balance prior to this entry.  This is most easily done
 			// by deducting the old amount of this entry from the old balance.
 			long balance = dTrans.getBalance() - ((Long) oldValue).longValue();
 
 			updateBalances(parentIndex, balance);
 		}
 	}
 
 	/**
 	 * This method is called when a transaction property is changed.
 	 * This method is called once for each entry in the transaction that
 	 * is a top level listed entry.
 	 * /
 	public void updateTransaction(Entry entryInAccount,
 			PropertyAccessor changedProperty, Object oldValue, Object newValue) {
 		int parentIndex = lookupEntryInAccount(entryInAccount);
 		JMoneyPlugin.myAssert(parentIndex >= 0);
 		TreeItem parentItem = fTable.getItem(parentIndex);
 		updateItem(parentItem);
 
 		// If the changed property is the sort property, the entry may
 		// need to be moved in the sort order.  This is difficult to do
 		// properly, as we do not want to mess up the user while the user
 		// is editing the row.  To move a row requires the row item to be
 		// disposed and a new row item inserted at the new position.
 		// The easiest way may be to see if this is the selected row, 
 		// and if it is, move all the rows past this row.
 		// Until this is implemented, we leave the row out of order. 
 	}
 
 	/**
 	 * Update the view to show the insertion of a new child
 	 * entry to a given top-level entry.
 	 * This includes updating the cached list of entries
 	 * in the DisplayableTransaction object.
 	 * /
 	public void addEntry(Entry entryInAccount, Entry newEntry) {
 		int parentIndex = lookupEntryInAccount(entryInAccount);
 		JMoneyPlugin.myAssert(parentIndex >= 0);
 		TreeItem parentItem = fTable.getItem(parentIndex);
 
 		Transaction transaction = entryInAccount.getTransaction();
 		DisplayableTransaction dTrans = (DisplayableTransaction)parentItem.getData();
 		
 		// Update the list of child entries in the
 		// DisplayableTransaction object.
 		dTrans.otherEntries.add(newEntry);
 		
 		Color colorOfNewEntry = parentItem.getBackground().equals(
 				transactionColor) ? entryColor : alternateEntryColor;
 
 		// If there were no DisplayableEntry rows then this was a
 		// simple transaction.  It can no longer be a simple transaction
 		// so add a row for entry that had been combined.  We also need
 		// to update the combined row.
 
 		if (parentItem.getItemCount() == 0) {
 			// The transaction should have three entries,
 			// newEntry, entryInAccount, and one other.
 			// We need to find the other.
 			Entry otherEntry = null;
 			for (Entry entry: transaction.getEntryCollection()) {
 				if (!entry.equals(entryInAccount) && !entry.equals(newEntry)) {
 					if (otherEntry != null) {
 						throw new RuntimeException("internal inconsistency");
 					}
 					otherEntry = entry;
 				}
 			}
 
 			// Create row for entry that was combined.
 			DisplayableEntry dEntry = new DisplayableEntry(otherEntry, dTrans);
 			TreeItem newItem = new TreeItem(parentItem, SWT.NONE);
 			newItem.setData(dEntry);
 			updateItem(newItem);
 			newItem.setBackground(colorOfNewEntry);
 
 			// Update the transaction row.
 			updateItem(parentItem);
 		}
 
 		DisplayableEntry dEntry = new DisplayableEntry(newEntry, dTrans);
 		TreeItem newItem = new TreeItem(parentItem, SWT.NONE);
 		newItem.setData(dEntry);
 		updateItem(newItem);
 		newItem.setBackground(colorOfNewEntry);
 	}
 
 	/**
 	 * Update the view to show the deletion of a child
 	 * entry to a given top-level entry.
 	 * This includes updating the cached list of entries
 	 * in the DisplayableTransaction object.
 	 * /
 	public void removeEntry(Entry entryInAccount, Entry deletedEntry) {
 		// get the index of the row for the transaction
 		int transIndex = lookupEntryInAccount(entryInAccount);
 
 		TreeItem parentItem = fTable.getItem(transIndex);
 		DisplayableTransaction dTrans = (DisplayableTransaction) parentItem
 				.getData();
 
 		// Update the list of child entries in the
 		// DisplayableTransaction object.
 		dTrans.otherEntries.remove(deletedEntry);
 		
 		// get the child row item for the entry
 		TreeItem childItem = lookupSplitEntry(parentItem, deletedEntry);
 
 		// Dispose it
 		childItem.dispose();
 
 		// If the transaction is now a simple transaction,
 		// dispose the remaining item, and update the transaction
 		// row.
 		if (dTrans.isSimpleEntry()) {
 			JMoneyPlugin.myAssert(parentItem.getItemCount() == 1);
 			parentItem.getItem(0).dispose();
 			updateItem(parentItem);
 		}
 	}
 */
 	/**
 	 * Add a selection listener.  The listener will be notified whenever
 	 * the selected row in the tree changes.
 	 * <P>
 	 * The event will contain the EntryData object as data.
 	 */
 	public void addSelectionListener(EntryRowSelectionListener tableSelectionListener) {
 		// We do not add the listener directly to the tree control.
 		// Instead we keep our own list of listeners and we add our
 		// own listener to the tree control.  When our own listener is
 		// notified of a change in selection, the listener in turn
 		// notifies the listeners added thru this method.
 		
 		// This approach is necessary because not all selection changes
 		// the tree should be passed on to the listeners added thru this
 		// method.  The selection change may be ignored if, for example,
 		// there is an error in the data on the previous row.  Also,
 		// when a selection change is initiated by the code from within
 		// a listener, SWT does not seem to generate a SelectionListener
 		// event, but we want one generated and by keeping our own list
 		// of listeners, we can do that.
 		
 		selectionListeners.add(tableSelectionListener);
 	}
 
 	/**
 	 * A private utility method for notifying our listeners of
 	 * a selection change event.
 	 * 
 	 * @param event the event to be passed on to the listeners
 	 */
 	private void fireSelectionChanges(EntryData data) {
 		for (EntryRowSelectionListener listener: selectionListeners) {
 			listener.widgetSelected(data);
 		}
 	}
 
 	private void fireRowDefaultSelection(EntryData data) {
 		for (EntryRowSelectionListener listener: selectionListeners) {
 			listener.widgetDefaultSelected(data);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.pages.entries.IEntriesControl#getControl()
 	 */
 	public Control getControl() {
 		return table;
 	}
 
 	private void showMessage (String message) {
 		MessageDialog.openWarning(
 				table.getShell(),
 				"Disabled Action Selected",
 				message);
 	}
 
 	// TODO: This class is duplicated in Header.
 	// Need to get sorting working.
 	private class RowComparator implements Comparator<EntryData> {
 		private Comparator<EntryData> cellComparator;
 		private boolean ascending;
 		
 		RowComparator(IndividualBlock<EntryData, ?> sortProperty, boolean ascending) {
 			this.cellComparator = sortProperty.getComparator();
 			this.ascending = ascending;
 		}
 		
 		public int compare(EntryData entryData1, EntryData entryData2) {
 			int result = cellComparator.compare(entryData1, entryData2);
 			return ascending ? result : -result;
 		}
 	}
 
 	public Session getSession() {
 		return session;
 	}
 
 	protected abstract T createEntryRowInput(Entry entry);
 }
