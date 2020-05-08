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
 
 package net.sf.jmoney.ui.internal.pages.account.capital;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Vector;
 
 import net.sf.jmoney.fields.TransactionInfo;
import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.Transaction;
 
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ICellModifier;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * An implementation of IEntriesControl that displays the entries
  * in a flat table.
  */
 public class EntriesTable implements IEntriesControl {
 
     protected static final Color transactionColor          = new Color(Display.getCurrent(), 125, 215, 060);
     protected static final Color alternateTransactionColor = new Color(Display.getCurrent(), 100, 160, 200);
     protected static final Color entryColor                = new Color(Display.getCurrent(), 180, 225, 140);
     protected static final Color alternateEntryColor       = new Color(Display.getCurrent(), 135, 185, 205);
 
 	protected EntriesPage fPage;
 	protected Table fTable;
     protected TableViewer fViewer;
     protected EntryLabelProvider fLabelProvider;
 
 	/**
 	 * @param container
 	 * @param page
 	 */
 	public EntriesTable(Composite container, EntriesPage page) {
 		this.fPage = page;
 		FormToolkit toolkit = page.getEditor().getToolkit();
 		
         fTable = toolkit.createTable(container, SWT.FULL_SELECTION);
         GridData gd = new GridData(GridData.FILL_BOTH);
         gd.heightHint = 100;
         fTable.setLayoutData(gd);
 
         TableColumn col;
         TableLayout tlayout = new TableLayout();
         
 
         for (Iterator iter = fPage.allEntryDataObjects.iterator(); iter.hasNext(); ) {
         	EntriesSectionProperty entriesSectionProperty = (EntriesSectionProperty)iter.next();
         	
             col = new TableColumn(fTable, SWT.NULL);
             col.setText(entriesSectionProperty.getText());
             tlayout.addColumnData(new ColumnWeightData(50, entriesSectionProperty.getMinimumWidth()));
         }
 
         col = new TableColumn(fTable, SWT.RIGHT);
         col.setText("Debit");
         tlayout.addColumnData(new ColumnWeightData(50, 15));
 
         col = new TableColumn(fTable, SWT.RIGHT);
         col.setText("Credit");
         tlayout.addColumnData(new ColumnWeightData(50, 15));
 
         col = new TableColumn(fTable, SWT.RIGHT);
         col.setText("Balance");
         tlayout.addColumnData(new ColumnWeightData(50, 15));
 
         fTable.setLayout(tlayout);
         fTable.setHeaderVisible(true);
         fTable.setLinesVisible(true);
         
         fViewer = new TableViewer(fTable);
         fViewer.setContentProvider(new ContentProvider());
         fLabelProvider = new EntryLabelProvider();
         fViewer.setLabelProvider(fLabelProvider);
         fViewer.setSorter(new EntrySorter());
 
         String[] columnProperties = new String[fPage.allEntryDataObjects.size() + 3];
         CellEditor[] cellEditors = new CellEditor[fPage.allEntryDataObjects.size() + 3];
         {
         int i = 0;
         for ( ; i < fPage.allEntryDataObjects.size(); i++) {
         	columnProperties[i] = ((EntriesSectionProperty)fPage.allEntryDataObjects.get(i)).getId();
         	cellEditors[i] = ((EntriesSectionProperty)fPage.allEntryDataObjects.get(i)).getPropertyAccessor().createCellEditor(fTable);
 
         }
         columnProperties[i] = "debit";
         cellEditors[i] = new TextCellEditor(fTable);
         columnProperties[i+1] = "credit";
         cellEditors[i+1] = new TextCellEditor(fTable);
         columnProperties[i+2] = "balance";
         cellEditors[i+2] = null;
         }
         fViewer.setColumnProperties(columnProperties);
         fViewer.setCellEditors(cellEditors);
         fViewer.setCellModifier(new CellModifier());
 
         fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
             public void selectionChanged(SelectionChangedEvent event) {
                 // Display the details of the new selected entry in the fields
                 // TODO: update the fields for all AbstractDispayableEntry
                 if (event.getSelection() instanceof IStructuredSelection) {
                     IStructuredSelection s = (IStructuredSelection) event.getSelection();
                     Object selectedObject = s.getFirstElement();
                     // The selected object might be null.  This occurs when the table is refreshed.
                     // I don't understand this so I am simply bypassing the update
                     // in this case.  Nigel
                     if (selectedObject != null) {
                     	if (selectedObject instanceof DisplayableTransaction) {
                     		DisplayableTransaction de = (DisplayableTransaction) selectedObject;
                     		fPage.fEntrySection.update(de.entry);
                     	}
                     }
                 }
             }
         });
 
         fViewer.setInput(fPage.getAccount());
 
         /*
          * Alternate the color of the entries to improve the readibility 
          * Remark: I'm not sure it's the better implementation. Not sure too
          * that it's works when the entries are changed (inserted or deleted)...
          */
         boolean isAlternated = true;
         for (int i = 0; i < fViewer.getTable().getItemCount(); i++) {
             TableItem item = fViewer.getTable().getItem(i);
             Object data = item.getData();
             // alternate the color
             if (data instanceof DisplayableTransaction) isAlternated = ! isAlternated;
             
             if (isAlternated) {
                 if (data instanceof DisplayableTransaction) item.setBackground(alternateTransactionColor);
                 if (data instanceof DisplayableEntry)       item.setBackground(alternateEntryColor);
             } else {
                 if (data instanceof DisplayableTransaction) item.setBackground(transactionColor);
                 if (data instanceof DisplayableEntry)       item.setBackground(entryColor);                
             }
         }
 
         
 		fTable.setTopIndex(fTable.getItemCount()-5);
 
 //		fTable.pack();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.ui.internal.pages.account.capital.IEntriesControl#setSelection(org.eclipse.jface.viewers.StructuredSelection)
 	 */
 	public void setSelection(StructuredSelection selection) {
         fViewer.setSelection(selection, true);
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.ui.internal.pages.account.capital.IEntriesControl#getSelection()
 	 */
 	public Entry getSelectedEntry() {
         if (fViewer.getSelection() instanceof IStructuredSelection) {
             IStructuredSelection s = (IStructuredSelection) fViewer.getSelection();
             Object selectedObject = s.getFirstElement();
             // The selected object cannot be null because the 'delete tranaction'
             // button would be disabled if no entry were selected.
            	if (selectedObject instanceof DisplayableTransaction) {
            		DisplayableTransaction de = (DisplayableTransaction) selectedObject;
            		return de.getEntry();
             }
         }
         
         return null;
 	}
 
 	
     /**
      * Class to sort two displayable items.
      * For the time: two transactions are sorted by the date,
      * two Entries are sort, first by their transaction then (if they belong the same transaction)
      * by the amount.
      * 
      * In all case, a new Entry is sorted as least.
      * 
      * @author Faucheux
      */
     class EntrySorter extends ViewerSorter {
         public int compare(Viewer notused, Object o1, Object o2) {
             
             assert (o1 instanceof DisplayableItem);
             assert (o2 instanceof DisplayableItem);
             
             // The case of a new Entry
             if (o1 instanceof DisplayableNewEmptyEntry) return 1;
             if (o2 instanceof DisplayableNewEmptyEntry) return -1;
 
             // Compare the transactions
             DisplayableItem di1 = (DisplayableItem)o1;
             DisplayableItem di2 = (DisplayableItem)o2;
             
             // Case of two items of the same transaction
             if (di1.getTransaction() == (di2.getTransaction())) {
                 if (di1 instanceof DisplayableTransaction) return -1;
                 if (di2 instanceof DisplayableTransaction) return 1;
                 return (di1.getAmount() - di2.getAmount() > 0) ? -1 : 1;
             }
             
             // Case of two different transactions
             Date d1 = di1.getTransaction().getDate();
             Date d2 = di2.getTransaction().getDate();
             // TODO Taken from EntryComparator as a quick fix
             if (d1 == null && d2 == null) return 0;
             if (d1 == null) return 1;
             if (d2 == null) return -1;
             int difference = d1.compareTo(d2); 
             if (difference != 0) return difference;
             // TODO perhaps an other test for transactions with the same date?
             return di1.getTransaction().hashCode() - di2.getTransaction().hashCode(); 
         }
     }
 
     abstract class DisplayableItem {
         abstract String getCheck ();
         abstract Date   getDate ();
         abstract Date   getValuta ();
         abstract String getDescription ();
         abstract long   getAmount ();
         
         abstract void setCheck (String check);
         abstract void setDate (Date date);
         abstract void setValuta (Date valuata);
         abstract void setDescription (String description);
         abstract void setAmount (long amount);
 
         // abstract String getOtherAccountName ();
         // abstract long   getBalance ();
         abstract Transaction getTransaction ();
         abstract Entry       getEntry ();
 
         //TODO: abstract void set/getCommodity();
     }
     
     class DisplayableEntry extends DisplayableItem { 
         
         private Entry entry;
 
         public DisplayableEntry(Entry entry) {
             this.entry = entry;
         }
         
         /* Trivial functions */
         String getCheck ()        { return entry.getCheck(); }
         Date   getDate ()         { return entry.getTransaction().getDate(); }
         Date   getValuta ()       { return entry.getValuta(); }
         String getDescription ()  { return entry.getDescription(); }
         long   getAmount ()       { return entry.getAmount(); }
 
         void setCheck (String check)              { entry.setCheck(check); }
         void setDate (Date date)                  { entry.getTransaction().setDate(date); }
         void setValuta (Date valuta)              { entry.setValuta(valuta); }
         void setDescription (String description)  { entry.setDescription(description); }
         void setAmount (long amount)              { entry.setAmount(amount); }
 
         String      getAccountName () {return entry.getAccount().getName(); }
         Transaction getTransaction () {return entry.getTransaction(); }
         Entry getEntry() { return entry; }
     }
     
     
     /**
      * This class is to be used for new entries which are currently filled in by the
      * user, before they are comited and transformed in real entries
      * @author Faucheux
      */
     class DisplayableNewEmptyEntry extends DisplayableItem {
         
         String check = null;
         Date   date = null;
         Date   valuta = null;
         String description = null;
         long   amount = 0;
         
         public DisplayableNewEmptyEntry(){
             // 	constructor to define a new, empty entry which we be filled in by the user
         }
 
         String getCheck ()        { return check; }
         Date   getDate ()         { return date; }
         Date   getValuta ()       { return valuta; }
         String getDescription ()  { return description; }
         long   getAmount ()       { return amount;}
         
         void setCheck (String check)              { this.check = check; }
         void setDate (Date date)                  { this.date = date; }
         void setValuta (Date valuta)              { this.valuta = valuta; }
         void setDescription (String description)  { this.description = description; }
         void setAmount (long amount)              { this.amount = amount; }
 
         String getOtherAccountName () { return null; }
         long   getBalance () { return 0; }
         Transaction getTransaction () {return null; }
         Entry getEntry () { return null ; }
     }
 
     class DisplayableTransaction extends DisplayableItem { 
         
         private Entry entry;
         private long balance;
 
         public DisplayableTransaction(Entry entry, long saldo) {
             this.entry = entry;
             this.balance = saldo;
         }
         
         /* Trivial functions */
         String getCheck ()        { return entry.getCheck(); }
         Date   getDate ()         { return entry.getTransaction().getDate(); }
         Date   getValuta ()       { return entry.getValuta(); }
         String getDescription ()  { return entry.getDescription(); }
         long   getAmount ()       { return entry.getAmount(); }
 
         void setCheck (String check)              { entry.setCheck(check); }
         void setDate (Date date)                  { entry.getTransaction().setDate(date); }
         void setValuta (Date valuta)              { entry.setValuta(valuta); }
         void setDescription (String description)  { entry.setDescription(description); }
         void setAmount (long amount)              { entry.setAmount(amount); }
 
         String getOtherAccountName () {
             Transaction t = entry.getTransaction();
             if (t.hasMoreThanTwoEntries())
                 return "-- splitted --";
             else
                 return t.getOther(entry).getAccount().getName();
         }
 
         long  getBalance () { return balance ; }
         Entry getEntry() { return entry; }
         Transaction getTransaction () {return entry.getTransaction(); }
         
     }
 
     class ContentProvider implements IStructuredContentProvider {
         public void dispose() {
             // TODO Auto-generated method stub
         }
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
             // TODO Auto-generated method stub
         }
         /** 
          * Returns the elements the table has to display. 
          * We should give the entry back with other informations the display
          * needs, for example the saldo.
          */
         public Object[] getElements(Object parent) {
            CapitalAccount account = (CapitalAccount) parent;
             Iterator it = account
 				.getSortedEntries(TransactionInfo.getDateAccessor(), false)
 				.iterator();
             Vector d_entries = new Vector();
            long saldo = 0;
             while (it.hasNext()) {
                 Entry e = (Entry) it.next();
                 saldo = saldo + e.getAmount();
                 d_entries.add(new DisplayableTransaction(e, saldo));
                 if (e.getTransaction().hasMoreThanTwoEntries()) {
                     // Case of an splitted entry. We display the transaction on the first line
                     // and the entries of the transaction on the following ones.
                     Iterator itSubEntries = e.getTransaction().getEntryIterator();
                     while (itSubEntries.hasNext()) {
                         // TODO: create a new class for this usage.
                         d_entries.add(new DisplayableEntry((Entry) itSubEntries.next()));
                     }
                 }
             }
             
             // an empty line to have the possibility to enter a new entry
             d_entries.add(new DisplayableNewEmptyEntry());
 
             return d_entries.toArray();
         }
     }
 
     class EntryLabelProvider extends LabelProvider implements ITableLabelProvider {
         protected NumberFormat nf = DecimalFormat.getCurrencyInstance();
 
         public String getColumnText(Object obj, int index) {
             if (obj instanceof DisplayableItem && ! (obj instanceof DisplayableNewEmptyEntry)) {
         		DisplayableItem de = (DisplayableItem) obj;
         		
         		if (index < fPage.allEntryDataObjects.size()) {
         			EntriesSectionProperty entryData = (EntriesSectionProperty)fPage.allEntryDataObjects.get(index);
         			return entryData.getValueFormattedForTable(de.getEntry());
         		} else {
         			Commodity c = fPage.getAccount().getCurrency();
         			switch (index - fPage.allEntryDataObjects.size()) {
         			case 0: // debit
         				return de.getEntry().getAmount() < 0 ? c.format(-de.getEntry().getAmount()) : "";
         			case 1: // credit
         				return de.getEntry().getAmount() > 0 ? c.format(de.getEntry().getAmount()) : "";
         			case 2: // balance
         			    if (de instanceof DisplayableTransaction) {
         			        return c.format(((DisplayableTransaction)de).balance);
         			    } else { 
         			        return "";
         			    }
         			}
             	}
             }
 
             return ""; //$NON-NLS-1$
         }
 
         public Image getColumnImage(Object obj, int index) {
             return null;
         }
     }
 
     class CellModifier implements ICellModifier {
         public boolean canModify(Object element, String property) {
             return !property.equals("balance");
         }
         public Object getValue(Object element, String property) {
         	if (element instanceof DisplayableTransaction) {
         		DisplayableTransaction de = (DisplayableTransaction) element;
         		
         		// The value is not always the String being displayed,
         		// nor is it as stored in the datastore.  For example,
         		// the ComboBoxCellEditor requires that the value is
         		// the index of the selection.  We therefore pass the
         		// request on to the PropertyAccessor which knows the
         		// correct format.
         		EntriesSectionProperty entriesSectionProperty = null;
         		int i = 0;
         		for ( ; i < fPage.allEntryDataObjects.size(); i++ ) {
         			entriesSectionProperty = (EntriesSectionProperty)fPage.allEntryDataObjects.get(i);
         			if (entriesSectionProperty.getId().equals(property)) {
         				break;
         			}
         		}
         		if (i < fPage.allEntryDataObjects.size()) {
         			ExtendableObject object = entriesSectionProperty.getObjectContainingProperty(de.entry);
         			return entriesSectionProperty.getPropertyAccessor().getValueTypedForCellEditor(object);
         		} else if (property.equals("credit")) {
         			Commodity c = fPage.getAccount().getCurrency();
         			return de.entry.getAmount() >= 0 ? c.format(de.entry.getAmount()) : "";
         		} else if (property.equals("debit")) {
         			Commodity c = fPage.getAccount().getCurrency();
         			return de.entry.getAmount() < 0 ? c.format(-de.entry.getAmount()) : "";
         		} else {
         			// Must be a column that is not editable,
         			// such as the balance column.
         			return null;
         		}
         	} else {
         		// should not happen??
         		return null;
         	}
         }
         
         public void modify(Object element, String property, Object value) {
             
             if (element instanceof Item) {
                 element = ((Item) element).getData();
             }
             
             if (!(element instanceof DisplayableTransaction)) {
             	// TODO: complete this case.
             	return;
             }
             
             Entry entry = ((DisplayableTransaction) element).getEntry();
 
             // Get the currency that is used to format the amounts.
             Currency c = fPage.getAccount().getCurrency();
             
             // Find the property given the name.
     		EntriesSectionProperty entriesSectionProperty = null;
     		int i = 0;
     		for ( ; i < fPage.allEntryDataObjects.size(); i++ ) {
     			entriesSectionProperty = (EntriesSectionProperty)fPage.allEntryDataObjects.get(i);
     			if (entriesSectionProperty.getId().equals(property)) {
     				break;
     			}
     		}
     		if (i < fPage.allEntryDataObjects.size()) {
     			ExtendableObject object = entriesSectionProperty.getObjectContainingProperty(entry);
     			entriesSectionProperty.getPropertyAccessor().setValueTypedForCellEditor(object, value);
                 fViewer.update(element, new String[] { property});
     		} else if (property.equals("credit")) {
                 entry.setAmount(c.parse((String) value));
                 fViewer.refresh();
     		} else if (property.equals("debit")) {
                 entry.setAmount(-c.parse((String) value));
                 fViewer.refresh();
     		} else {
     			// should not happen??
     		}
         }
     }
 
 	/**
 	 * Refresh the viewer.
 	 */
 	public void refresh() {
 		fViewer.refresh();
 	}	
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.ui.internal.pages.account.capital.IEntriesControl#dispose()
 	 */
 	public void dispose() {
 		fTable.dispose();
 	}
 }
