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
 
 package net.sf.jmoney.pages.entries;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 import net.sf.jmoney.fields.TransactionInfo;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.Transaction;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * This class manages the transaction dialog that appears when
  * the 'details' button is pressed in the account entries list page.
  * <P>
  * The transaction dialog displays a single transaction.  The controls
  * for each entry are on alternating yellow and green areas, providing
  * a clear distinction between the properties for one entry and the
  * properties for another entry even when more than one line of controls
  * is needed for a single entry.  This dialog displays all the appropriate
  * properties for each entry.  Thus, if the entries list table does not
  * show a column for a property, the user can use this dialog to fill in
  * the complete set of properties.
  * 
  * @author Nigel Westbury
  */
 public class TransactionDialog {
 
 	private static final Color yellow        = new Color(Display.getCurrent(), 255, 255, 200);
 	private static final Color green         = new Color(Display.getCurrent(), 225, 255, 225);
 
     private Shell shell;
 
     private Display display;
 
     private Session session;
 
     private Currency defaultCurrency;
     
     private Composite transactionArea;
     
     private Composite entriesArea;
     
     private Vector<EntryControls> entryControlsList = new Vector<EntryControls>();
     
     private Vector<IPropertyControl> transactionControls = new Vector<IPropertyControl>();
     
    public TransactionDialog(Shell parent, Entry originalAccountEntry) {
     	
     	/*
     	 * Use a transaction manager to hold the changes.
     	 * This allows the changes to be easily cancelled
     	 * if the user presses the 'cancel' button.
     	 */
    	final TransactionManager transactionManager = new TransactionManager(originalAccountEntry.getObjectKey().getSessionManager());
     	
    	this.session = transactionManager.getSession();
     	Entry accountEntry = transactionManager.getCopyInTransaction(originalAccountEntry);
     	
     	this.defaultCurrency = accountEntry.getCommodity() instanceof Currency
     	? (Currency)accountEntry.getCommodity()
     			: session.getDefaultCurrency();
     	
         this.display = parent.getDisplay();
         
    		final Transaction transaction = accountEntry.getTransaction();
    		
         shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.CLOSE);
         shell.setText("Transaction Details");
     
         GridLayout sectionLayout = new GridLayout();
         sectionLayout.numColumns = 1;
         sectionLayout.marginHeight = 0;
         sectionLayout.marginWidth = 0;
         shell.setLayout(sectionLayout);
 
         // Create the transaction property area
 		transactionArea = new Composite(shell, 0);
 		transactionArea.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		GridLayout transactionAreaLayout = new GridLayout();
 		transactionAreaLayout.numColumns = 10;
 		transactionArea.setLayout(transactionAreaLayout);
 
         // Create the entries area
 		entriesArea = new Composite(shell, 0);
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		gd.minimumWidth = 800;
 		gd.widthHint = 800;
 		gd.grabExcessHorizontalSpace = true;
 		entriesArea.setLayoutData(gd);
 
 		GridLayout entriesAreaLayout = new GridLayout();
         entriesAreaLayout.numColumns = 5;
         entriesAreaLayout.horizontalSpacing = 0;  // Ensures no uncolored gaps between items in same row
         entriesAreaLayout.verticalSpacing = 0;  // Ensures no uncolored gaps between items in same column
         entriesAreaLayout.marginWidth = 0;
         entriesArea.setLayout(entriesAreaLayout);
 
         // Add properties from the transaction.
   		for (ScalarPropertyAccessor<?> propertyAccessor: TransactionInfo.getPropertySet().getScalarProperties3()) {
         	Label propertyLabel = new Label(transactionArea, 0);
         	propertyLabel.setText(propertyAccessor.getDisplayName() + ':');
         	IPropertyControl propertyControl = propertyAccessor.createPropertyControl(transactionArea);
         	propertyControl.load(null);
         	transactionControls.add(propertyControl);
         }
 
     	// Create the button area
 		Composite buttonArea = new Composite(shell, 0);
 		
 		RowLayout layoutOfButtons = new RowLayout();
 		layoutOfButtons.fill = false;
 		layoutOfButtons.justify = true;
 		buttonArea.setLayout(layoutOfButtons);
 		
         // Create the 'add entry' button.
         Button addButton = new Button(buttonArea, SWT.PUSH);
         addButton.setText("Split off New Entry");
         addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            		Entry newEntry = transaction.createEntry();
            		
            		// If all entries so far are in the same currency then set the
            		// amount of the new entry to be the amount that takes the balance
            		// to zero.  If we cannot determine the currency because the user
            		// has not yet entered the necessary data, assume that the currencies
            		// are all the same.
            		Commodity commodity = null;
            		boolean mismatchedCommodities = false;
            		long totalAmount = 0;
                 for (Entry entry: transaction.getEntryCollection()) {
                 	if (commodity == null) {
                 		// No commodity yet determined, so set to the commodity for
                 		// this entry, if any.
                 		commodity = entry.getCommodity(); 
                 	} else {
                 		if (!commodity.equals(entry.getCommodity())) {
                 			mismatchedCommodities = true;
                 			break;
                 		}
                 	}
                 	totalAmount += entry.getAmount();
                 }
                 
                 if (!mismatchedCommodities) {
                 	newEntry.setAmount(-totalAmount);
                 }
                 
         		Color entryColor = (entryControlsList.size() % 2) == 0
         		? yellow
         		: green;
 
         		EntryControls newEntryControls = new EntryControls(TransactionDialog.this.session, entriesArea, newEntry, entryColor, defaultCurrency);
         		entryControlsList.add(newEntryControls);
 
                 shell.pack(true);
            }
         });
 
         // Create the 'delete entry' button.
         Button deleteButton = new Button(buttonArea, SWT.PUSH);
         deleteButton.setText("Delete Entries with Zero or Blank Amounts");
         deleteButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent event) {
         		boolean alternate = false;
        		for (Iterator<EntryControls> iter = entryControlsList.iterator(); iter.hasNext(); ) {
        			EntryControls entryControls = iter.next();
         			if (entryControls.entry.getAmount() == 0) {
         				entryControls.dispose();
         				transaction.deleteEntry(entryControls.entry);
         				iter.remove();
         			} else {
         				entryControls.setColor(alternate ? green : yellow);
         				alternate = !alternate;
         			}
         		}
         		shell.pack();
         	}
         });
 
         // Create the 'OK' button
         Button okButton = new Button(buttonArea, SWT.PUSH);
         okButton.setText("OK");
         okButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent event) {
         		transactionManager.commit("Edit Transaction");
         		shell.close();
         	}
         });
 
         // Create the 'cancel' button
         Button cancelButton = new Button(buttonArea, SWT.PUSH);
         cancelButton.setText("Cancel");
         cancelButton.addSelectionListener(new SelectionAdapter() {
         	public void widgetSelected(SelectionEvent event) {
         		shell.close();
         	}
         });
 
     	// Update transaction property controls.
         for (IPropertyControl control: transactionControls) {
            	control.load(transaction);
         }
         
 		// Load the values from the given entry into the property controls.
 		entryControlsList.add(new EntryControls(session, entriesArea, accountEntry, yellow, defaultCurrency));
 		
         // Create and set the controls for the other entries in
         // the transaction.
         for (Entry entry: transaction.getEntryCollection()) {
         	if (!entry.equals(accountEntry)) {
 
         		Color entryColor = (entryControlsList.size() % 2) == 0
         		? yellow 
         		: green;
 
         		entryControlsList.add(new EntryControls(session, entriesArea, entry, entryColor, defaultCurrency));
         	}
         }
         
         shell.pack();
 	}
 	
     public void open() {
         shell.pack();
         shell.open();
         while (!shell.isDisposed()) {
             if (!display.readAndDispatch()) display.sleep();
         }
     }
 }
