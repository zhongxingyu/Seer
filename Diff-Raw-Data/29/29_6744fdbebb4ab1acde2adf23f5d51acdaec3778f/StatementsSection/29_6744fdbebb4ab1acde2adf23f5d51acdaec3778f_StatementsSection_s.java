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
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import net.sf.jmoney.fields.EntryInfo;
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.reconciliation.BankStatement;
 import net.sf.jmoney.reconciliation.IReconciliationQueries;
 import net.sf.jmoney.reconciliation.ReconciliationEntryInfo;
 
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.forms.SectionPart;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Implementation of the 'Statements' section of the reconciliation
  * page.  This section lists all the statements in the account that
  * have reconciled entries in them.
  * 
  * @author Nigel Westbury
  */
 public class StatementsSection extends SectionPart {
 	
 	private Table statementTable;
 	
 	private StatementContentProvider contentProvider;
 	
 	private TableColumn column2;
 	
 	public StatementsSection(Composite parent, FormToolkit toolkit, CurrencyAccount account) {
 		super(parent, toolkit, 
 				Section.DESCRIPTION | Section.TITLE_BAR);		
 		getSection().setText("Statements");
 		getSection().setDescription("Double click a statement to show that statement.");
 
		statementTable = new Table(getSection(), SWT.FULL_SELECTION | SWT.SINGLE);
 		statementTable.setHeaderVisible(true);
 		statementTable.setLinesVisible(true);
 		
 		// 1st column contains the statement number/date
 		TableColumn column1 = new TableColumn(statementTable, SWT.LEFT, 0);
 		column1.setText("Statement");
 		column1.setWidth(50);
 		
 		// 2nd column contains the statement balance
 		column2 = new TableColumn(statementTable, SWT.RIGHT, 1);
 		column2.setText("Balance");
 		column2.setWidth(70);
 		
		GridData gd = new GridData(GridData.FILL_BOTH);
		statementTable.setLayoutData(gd);
		
 		// Create and setup the TableViewer
 		TableViewer tableViewer = new TableViewer(statementTable);   
 		
 		tableViewer.setUseHashlookup(true);
 		//	       tableViewer.setColumnProperties(columnNames);
 	
 		contentProvider = new StatementContentProvider(tableViewer);
 		
 		tableViewer.setContentProvider(contentProvider);
 		tableViewer.setLabelProvider(new StatementLabelProvider());
 		// The input for the table viewer is the instance of ExampleTaskList
 		tableViewer.setInput(account);
 		
		statementTable.pack(true);   // Does this do anything??????
 		
		getSection().setClient(statementTable);
		toolkit.paintBordersFor(statementTable);
 		refresh();
 	}
 	
 	class StatementLabelProvider
 	implements ITableLabelProvider {
 		public String getColumnText(Object element, int columnIndex) {
 			String result = "";
 			StatementDetails statementDetails = (StatementDetails)element;
 			switch (columnIndex) {
 			case 0:  // statement number
 				// TODO: We need another method to get the string in
 				// a user friendly format.  Something similar to the
 				// table and message formats in the editor factories.
 				result = statementDetails.statement.toString();  
 				break;
 			case 1 :
 				// TODO: format this correctly for the currency
 				result = Float.toString(((float)statementDetails.getClosingBalance())/100);
 				break;
 			}
 			return result;
 		}	
 		
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
 		 */
 		public void addListener(ILabelProviderListener listener) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
 		 */
 		public void dispose() {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
 		 */
 		public boolean isLabelProperty(Object element, String property) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
 		 */
 		public void removeListener(ILabelProviderListener listener) {
 			// TODO Auto-generated method stub
 			
 		}
 	} 
 	
 	class StatementContentProvider implements IStructuredContentProvider {
 		/**
 		 * The table viewer to be notified whenever the content changes.
 		 */
 		private TableViewer tableViewer;
 		
 		private CurrencyAccount account;
 		
 		/**
 		 * Maps BankStatement to StatementDetails
 		 */
 		private SortedMap statementDetailsMap;
 
 		StatementContentProvider(TableViewer tableViewer) {
 			this.tableViewer = tableViewer;
 		}
 		
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 			account = (CurrencyAccount)newInput;
 			
 			// Build a tree map of the statement totals
 			
 			// We use a tree map in preference to the more efficient
 			// hash map because we can then fetch the results in order.
 			statementDetailsMap = new TreeMap();
 			
 			// When this item is disposed, the input may be set to null.
 			// Return an empty list in this case.
 			if (newInput == null) {
 				return;
 			}
 			
 			IReconciliationQueries queries = (IReconciliationQueries)account.getSession().getAdapter(IReconciliationQueries.class);
 			if (queries != null) {
 				// TODO: change this method
 				//return queries.getStatements(fPage.getAccount());
 			} else {
 				// IReconciliationQueries has not been implemented in the datastore.
 				// We must therefore provide our own implementation.
 				
 				// We use a tree map in preference to the more efficient
 				// hash map because we can then fetch the results in order.
 				SortedMap statementTotals = new TreeMap();
 				
 				Iterator it = account.getEntries().iterator();
 				while (it.hasNext()) {
 					Entry entry = (Entry)it.next();
 					BankStatement statement = (BankStatement)entry .getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 					
 					if (statement != null) {
 						Long statementTotal = (Long)statementTotals.get(statement);
 						if (statementTotal == null) {
 							statementTotal = new Long(0);
 						}
 						statementTotals.put(statement, new Long(statementTotal.longValue() + entry.getAmount()));
 					}
 				}
 				
 				long balance = account.getStartBalance();
 				for (Iterator iter = statementTotals.entrySet().iterator(); iter.hasNext(); ) {
 					Map.Entry mapEntry = (Map.Entry)iter.next();
 					BankStatement statement = (BankStatement)mapEntry.getKey();
 					long totalEntriesOnStatement = ((Long)mapEntry.getValue()).longValue();
 					
 					statementDetailsMap.put(
 							statement,
 							new StatementDetails(
 									statement,
 									balance,
 									totalEntriesOnStatement)
 					);
 					balance += totalEntriesOnStatement;
 				}
 			}
 
 			// Listen for changes so we can keep the tree map upto date.
 			account.getSession().addSessionChangeListener(new SessionChangeAdapter() {
 				public void objectAdded(ExtendableObject newObject) {
 					if (newObject instanceof Entry) {
 						Entry newEntry = (Entry)newObject;
 						if (account.equals(newEntry.getAccount())) {
 							BankStatement statement = (BankStatement)newEntry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 							adjustStatement(statement, newEntry.getAmount());
 						}
 					}
 				}
 				
 				public void objectDeleted(ExtendableObject deletedObject) {
 					if (deletedObject instanceof Entry) {
 						Entry deletedEntry = (Entry)deletedObject;
 						if (account.equals(deletedEntry.getAccount())) {
 							BankStatement statement = (BankStatement)deletedEntry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 							adjustStatement(statement, -deletedEntry.getAmount());
 						}
 					} else if (deletedObject instanceof Transaction) {
 						Transaction deletedTransaction = (Transaction)deletedObject;
 						for (Iterator iter = deletedTransaction.getEntryCollection().iterator(); iter.hasNext(); ) {
 							Entry deletedEntry = (Entry)iter.next();
 							if (account.equals(deletedEntry.getAccount())) {
 								BankStatement statement = (BankStatement)deletedEntry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 								adjustStatement(statement, -deletedEntry.getAmount());
 							}
 						}
 					}
 				}
 				
 				public void objectChanged(ExtendableObject changedObject, PropertyAccessor changedProperty, Object oldValue, Object newValue) {
 					if (changedObject instanceof Entry) {
 						Entry entry = (Entry)changedObject;
 						
 						if (changedProperty == EntryInfo.getAccountAccessor()) {
 							if (account.equals(oldValue)) {
 								BankStatement statement = (BankStatement)entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 								adjustStatement(statement, -entry.getAmount());
 							}
 							if (account.equals(newValue)) {
 								BankStatement statement = (BankStatement)entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 								adjustStatement(statement, entry.getAmount());
 							}
 						} else {
 							if (account.equals(entry.getAccount())) {
 								if (changedProperty == EntryInfo.getAmountAccessor()) {
 									BankStatement statement = (BankStatement)entry.getPropertyValue(ReconciliationEntryInfo.getStatementAccessor());
 									long oldAmount = ((Long)oldValue).longValue();
 									long newAmount = ((Long)newValue).longValue();
 									adjustStatement(statement, newAmount - oldAmount);
 								} else if (changedProperty == ReconciliationEntryInfo.getStatementAccessor()) {
 									adjustStatement((BankStatement)oldValue, -entry.getAmount());
 									adjustStatement((BankStatement)newValue, entry.getAmount());
 								}
 							}
 						}
 					}
 				}
 			}, statementTable);
 		}
 		
 		/**
 		 * @param statement the statement to be adjusted.  This parameter
 		 * 				may be null in which case this method does nothing
 		 * @param amount the amount by which the total for the given statement
 		 * 				is to be adjusted
 		 */
 		private void adjustStatement(BankStatement statement, long amount) {
 			if (statement != null) {
 				StatementDetails thisStatementDetails = (StatementDetails)statementDetailsMap.get(statement);
 				if (thisStatementDetails == null) {
 					
 					long openingBalance;
 					SortedMap priorStatements = statementDetailsMap.headMap(statement);
 					if (priorStatements.isEmpty()) {
 						openingBalance = account.getStartBalance();
 					} else {
 						openingBalance = ((StatementDetails)priorStatements.get(priorStatements.lastKey())).getClosingBalance();
 					}
 					
 					thisStatementDetails = new StatementDetails(
 							statement,
 							openingBalance,
 							amount);
 					
 					statementDetailsMap.put(statement, thisStatementDetails);
 
 					// Notify the viewer that we have a new item
 					tableViewer.add(thisStatementDetails);
 				} else {
 					thisStatementDetails.adjustEntriesTotal(amount);
 					
 					// Notify the viewer that an item has changed
 					tableViewer.update(thisStatementDetails, null);
 				}
 				
 				// This total affects all later balances.
 				
 				// Iterate through all later statements updating the
 				// statement details and then notify the viewer of the
 				// update.  Note that tailMap returns a collection that
 				// includes the starting key, so we must be sure not to
 				// process that.
 				SortedMap laterStatements = statementDetailsMap.tailMap(statement);
 				for (Iterator iter = laterStatements.values().iterator(); iter.hasNext(); ) {
 					StatementDetails statementDetails = (StatementDetails)iter.next();
 					if (!statementDetails.statement.equals(statement)) {
 						statementDetails.adjustOpeningBalance(amount);
 						
 						// Notify the viewer that an item has changed
 						tableViewer.update(statementDetails, null);
 					}
 				}
 			}
 		}
 		
 		public void dispose() {
 			//       statementList.removeChangeListener(this);
 		}
 		
 		// Return the statements as an array of Objects
 		public Object[] getElements(Object parent) {
 			// Build the array of statements and balances
 /*
 			statementsAndBalances = new Vector();
 			
 			long balance = account.getStartBalance();
 			for (Iterator iter = statementTotals.entrySet().iterator(); iter.hasNext(); ) {
 				Map.Entry mapEntry = (Map.Entry)iter.next();
 				long totalEntriesOnThisStatement = ((Long)mapEntry.getValue()).longValue();
 				statementsAndBalances.add( 
 					new StatementDetails(
 							(BankStatement)mapEntry.getKey(),
 							balance,
 							totalEntriesOnThisStatement)
 				);
 				balance += totalEntriesOnThisStatement;
 			}
 			*/
 			return statementDetailsMap.values().toArray();
 		}
 
 		/**
 		 * @return
 		 */
 		public StatementDetails getLastStatement() {
 			if (this.statementDetailsMap.isEmpty()) {
 				return null;
 			} else {
 				return (StatementDetails)statementDetailsMap.get(statementDetailsMap.lastKey());
 			}
 		}
 	}
 	
 	/**
 	 * @param show
 	 */
 	public void showBalance(boolean show) {
 		if (show) {
 			column2.setWidth(SWT.DEFAULT);
 		} else {
 			column2.setWidth(0);
 		}
 	}
 	
 	/**
 	 * Returns the last statement in the list of statements for
 	 * the account.  This is used to determine default values when
 	 * the user creates a new statement and also the starting
 	 * balance of any newly created statement.
 	 * 
 	 * @return the last statement, or null if no statements have
 	 * 				yet been created in the account
 	 */
 	public StatementDetails getLastStatement() {
 		return contentProvider.getLastStatement();
 	}
 
 	/**
 	 * Listen for changes in the selected statement.
 	 */
 	public void addSelectionListener(SelectionAdapter listener) {
 		// Pass thru the request to the table control.
 		statementTable.addSelectionListener(listener);
 	}	
 }
