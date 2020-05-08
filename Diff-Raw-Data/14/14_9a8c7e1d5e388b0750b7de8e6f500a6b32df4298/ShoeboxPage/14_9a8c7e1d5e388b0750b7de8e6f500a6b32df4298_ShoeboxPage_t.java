 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2002 Johann Gyger <johann.gyger@switzerland.org>
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
 
 package net.sf.jmoney.shoebox;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jmoney.IBookkeepingPage;
 import net.sf.jmoney.IBookkeepingPageFactory;
 import net.sf.jmoney.ITransactionTemplate;
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.entrytable.BaseEntryRowControl;
 import net.sf.jmoney.entrytable.Block;
 import net.sf.jmoney.entrytable.CellBlock;
 import net.sf.jmoney.entrytable.DebitAndCreditColumns;
 import net.sf.jmoney.entrytable.EntriesTable;
 import net.sf.jmoney.entrytable.EntryData;
 import net.sf.jmoney.entrytable.EntryRowControl;
 import net.sf.jmoney.entrytable.HorizontalBlock;
 import net.sf.jmoney.entrytable.IEntriesContent;
 import net.sf.jmoney.entrytable.IRowProvider;
 import net.sf.jmoney.entrytable.IndividualBlock;
 import net.sf.jmoney.entrytable.OtherEntriesBlock;
 import net.sf.jmoney.entrytable.PropertyBlock;
 import net.sf.jmoney.entrytable.ReusableRowProvider;
 import net.sf.jmoney.entrytable.RowSelectionTracker;
 import net.sf.jmoney.entrytable.SingleOtherEntryPropertyBlock;
 import net.sf.jmoney.entrytable.SplitEntryRowControl;
 import net.sf.jmoney.entrytable.VerticalBlock;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.isolation.UncommittedObjectKey;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.DatastoreManager;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.EntryInfo;
 import net.sf.jmoney.model2.IObjectKey;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.model2.TransactionInfo;
 import net.sf.jmoney.views.NodeEditor;
 import net.sf.jmoney.views.SectionlessPage;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * @author Nigel Westbury
  */
 public class ShoeboxPage implements IBookkeepingPageFactory {
 	
 	private static final String PAGE_ID = "net.sf.jmoney.shoebox.editor";
 	
 //	protected Vector<IEntriesTableProperty> allEntryDataObjects = new Vector<IEntriesTableProperty>();
 
 	ShoeboxFormPage formPage;
 	
 	/**
 	 * The transaction manager used for all changes made by
 	 * this page.  It is created by the page is created and
 	 * remains usable for the rest of the time that this page
 	 * exists.
 	 */
 	TransactionManager transactionManager = null;
 
     private Block<EntryData, EntryRowControl> rootBlock;
     
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.IBookkeepingPageListener#createPages(java.lang.Object, org.eclipse.swt.widgets.Composite)
 	 */
 	public IBookkeepingPage createFormPage(NodeEditor editor, IMemento memento) {
         // Create our own transaction manager.
         // This ensures that uncommitted changes
     	// made by this page are isolated from datastore usage outside
     	// of this page.
 		DatastoreManager sessionManager = JMoneyPlugin.getDefault().getSessionManager();
 
         transactionManager = new TransactionManager(sessionManager);
 /*    	
     	// Build an array of all possible properties that may be
     	// displayed in the table.
        
         // TODO: I think this following line needs to be in the other pages too.
         // (account entries and reconciliation).  We otherwise seem to get the
         // columns added multiple times.
     	allEntryDataObjects = new Vector<IEntriesTableProperty>();
         
         // Add properties from the transaction.
    		for (ScalarPropertyAccessor propertyAccessor: TransactionInfo.getPropertySet().getScalarProperties3()) {
         	allEntryDataObjects.add(new EntriesSectionProperty(propertyAccessor, "transaction") {
         		public ExtendableObject getObjectContainingProperty(IDisplayableItem data) {
         			return data.getTransactionForTransactionFields();
         		}
         	});
         }
 
         /*
 		 * Add properties from this entry. For time being, this is all the
 		 * properties except the description which come from the other entry,
 		 * and the amount which is shown in the debit and credit columns.
 		 * /
    		for (ScalarPropertyAccessor propertyAccessor: EntryInfo.getPropertySet().getScalarProperties3()) {
             if (propertyAccessor != EntryInfo.getDescriptionAccessor()
             		&& propertyAccessor != EntryInfo.getAmountAccessor()) {
             	allEntryDataObjects.add(new EntriesSectionProperty(propertyAccessor, "this") {
             		public ExtendableObject getObjectContainingProperty(IDisplayableItem data) {
             			return data.getEntryForAccountFields();
             		}
             	});
             }
         }
 
         /*
 		 * Add properties from the other entry where the property also is
 		 * applicable for capital accounts. For time being, this is just the
 		 * account.
 		 * /
    		for (ScalarPropertyAccessor propertyAccessor: EntryInfo.getPropertySet().getScalarProperties3()) {
             if (propertyAccessor == EntryInfo.getAccountAccessor()) {
             	allEntryDataObjects.add(new EntriesSectionProperty(propertyAccessor, "common2") {
 					public ExtendableObject getObjectContainingProperty(IDisplayableItem data) {
 						return data.getEntryForCommon2Fields();
 					}
             	});
             } else if (propertyAccessor == EntryInfo.getDescriptionAccessor()) {
             	allEntryDataObjects.add(new EntriesSectionProperty(propertyAccessor, "other") {
 					public ExtendableObject getObjectContainingProperty(IDisplayableItem data) {
 						return data.getEntryForOtherFields();
 					}
             	});
             }
         }
         
 		debitColumnManager = new DebitAndCreditColumns("Debit", "debit", true);     //$NON-NLS-2$
 		creditColumnManager = new DebitAndCreditColumns("Credit", "credit", false); //$NON-NLS-2$
 */
     	
 		formPage = new ShoeboxFormPage(
 				editor,
 				PAGE_ID);
 	
 		try {
 			editor.addPage(formPage);
 		} catch (PartInitException e) {
 			JMoneyPlugin.log(e);
 			// TODO: cleanly leave out this page.
 		}
 		
 		return formPage;
 	}
 
 	private class ShoeboxFormPage extends SectionlessPage {
 		
 		private Session session;
 		
 	    private EntriesTable recentlyAddedEntriesControl;
 	    private IEntriesContent recentEntriesTableContents = null;
 	    
 		Collection<IObjectKey> ourEntryList = new Vector<IObjectKey>();
 		
 		public Map<String, ITransactionTemplate> transactionTypes = new HashMap<String, ITransactionTemplate>();
 
 		ShoeboxFormPage(
 				NodeEditor editor,
 				String pageId) {
 			super(editor,
 					pageId, 
 					ShoeboxPlugin.getResourceString("navigationTreeLabel"), 
 			"Shoebox Receipts - data entry");
 			
 			this.session = JMoneyPlugin.getDefault().getSession();
 		}
 		
 		public Composite createControl(Object nodeObject, Composite parent, FormToolkit toolkit, IMemento memento) {
 			
 	        recentEntriesTableContents = new IEntriesContent() {
 
 				public Collection<Entry> getEntries() {
 					Collection<Entry> committedEntries = new Vector<Entry>();
 					for (IObjectKey objectKey: ourEntryList) {
 						Entry committedEntry = (Entry)((UncommittedObjectKey)objectKey).getCommittedObjectKey().getObject();
 						committedEntries.add(committedEntry);
 					}
 					return committedEntries;
 				}
 
 				public boolean isEntryInTable(Entry entry) {
 					/*
 					 * This entry is to be shown if the entry was entered using
 					 * this editor. We keep a list of entries that were entered
 					 * through this editor.
 					 */
 					for (IObjectKey objectKey: ourEntryList) {
 						IObjectKey committedKey = ((UncommittedObjectKey)objectKey).getCommittedObjectKey();
 						if (committedKey.equals(entry.getObjectKey())) {
 							return true;
 						}
 					}
 					return false;
 				}
 
 				public boolean filterEntry(EntryData data) {
 					// No filter here, so entries always match
 					return true;
 				}
 
 				public long getStartBalance() {
 					// No balance in this table
 					return 0;
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
 					/*
 					 * There are no properties we must set when an entry is
 					 * added to this table.
 					 */
 				}
 	        };
 
 			/**
 			 * topLevelControl is a control with grid layout, 
 			 * with one column of vertical controls.
 			 */
 			Composite topLevelControl = new Composite(parent, SWT.NULL);
 			topLevelControl.setLayout(new GridLayout(1, false));
 			parent.setBackground(this.getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
 			topLevelControl.setBackground(getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_CYAN));
 			
 			/*
 			 * Setup the layout structure of the header and rows.
 			 */
 			IndividualBlock<EntryData, Composite> transactionDateColumn = PropertyBlock.createTransactionColumn(TransactionInfo.getDateAccessor());
 			CellBlock<EntryData, BaseEntryRowControl> debitColumnManager = DebitAndCreditColumns.createDebitColumn(session.getDefaultCurrency());
 			CellBlock<EntryData, BaseEntryRowControl> creditColumnManager = DebitAndCreditColumns.createCreditColumn(session.getDefaultCurrency());
 			
 			rootBlock = new HorizontalBlock<EntryData, EntryRowControl>(
 					transactionDateColumn,
 					new VerticalBlock<EntryData, EntryRowControl>(
 							new HorizontalBlock<EntryData, EntryRowControl>(
 									PropertyBlock.createEntryColumn(EntryInfo.getAccountAccessor()),
 									PropertyBlock.createEntryColumn(EntryInfo.getCheckAccessor())
 							),
 							PropertyBlock.createEntryColumn(EntryInfo.getMemoAccessor())
 					),
 					new OtherEntriesBlock(
 							new HorizontalBlock<Entry, SplitEntryRowControl>(
 									new SingleOtherEntryPropertyBlock(EntryInfo.getAccountAccessor()),
 									new SingleOtherEntryPropertyBlock(EntryInfo.getMemoAccessor(), JMoneyPlugin.getResourceString("Entry.description")),
 									new SingleOtherEntryPropertyBlock(EntryInfo.getAmountAccessor())
 							)
 					),
 					debitColumnManager,
 					creditColumnManager
 			);
 			
 	        // Create the table control.
		    IRowProvider<EntryData> rowProvider = new ReusableRowProvider(rootBlock);
	        recentlyAddedEntriesControl = new EntriesTable<EntryData>(topLevelControl, toolkit, rootBlock, recentEntriesTableContents, rowProvider, this.session, transactionDateColumn, new RowSelectionTracker()) {
				@Override
				protected EntryData createEntryRowInput(Entry entry) {
					return new EntryData(entry, session.getDataManager());
				}

				@Override
				protected EntryData createNewEntryRowInput() {
					return new EntryData(null, session.getDataManager());
				}
	        }; 
 			
 			recentlyAddedEntriesControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
 
 			// The tab control
 			GridData gdTabControl = new GridData(SWT.FILL, SWT.FILL, false, false);
 			createTabbedArea(topLevelControl).setLayoutData(gdTabControl);
 
 			// init from the memento
 			init(memento);
 			
 			// Set up the context menus.
 			makeActions();
 			hookContextMenu(fEditor.getSite());
 			
 			return topLevelControl;
 		}
 		
 		private Control createTabbedArea(Composite parent) {
 			TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
 
 			
 			// Load the extensions
 			IExtensionRegistry registry = Platform.getExtensionRegistry();
 			for (IConfigurationElement element: registry.getConfigurationElementsFor("net.sf.jmoney.shoebox.templates")) {
 				if (element.getName().equals("template")) {
 
 					String label = element.getAttribute("label");
 					String id = element.getAttribute("id");
 					String position = element.getAttribute("position");
 					String fullId = element.getNamespaceIdentifier() + "." + id;
 
 					try {
 						ITransactionTemplate transactionType = (ITransactionTemplate)element.createExecutableExtension("class");
 
 						TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
 						tabItem.setText(transactionType.getDescription());
 						tabItem.setControl(transactionType.createControl(tabFolder, true, null, ourEntryList));
 
 						int positionNumber = 800;
 						if (position != null) {
 							positionNumber = Integer.parseInt(position);
 						}
 
 						transactionTypes.put(fullId, transactionType);
 
 					} catch (CoreException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 	        return tabFolder;
 		}
 
 		private void init(IMemento memento) {
 			if (memento != null) {
 				IMemento [] templateMementos = memento.getChildren("template");
 				for (int i = 0; i < templateMementos.length; i++) {
 					ITransactionTemplate transactionType = transactionTypes.get(templateMementos[i].getID());
 					if (transactionType != null) {
 						transactionType.init(templateMementos[i]);
 					}
 				}
 			}
 		}
 		
 		public void saveState(IMemento memento) {
 			for (String id: transactionTypes.keySet()) {
 				ITransactionTemplate transactionType = transactionTypes.get(id);
 				transactionType.saveState(memento.createChild("template", id));
 			}
 		}
 		
 		private void hookContextMenu(IWorkbenchPartSite site) {
 		}
 		
         private void makeActions() {
 		}
 		
 	};
 }
