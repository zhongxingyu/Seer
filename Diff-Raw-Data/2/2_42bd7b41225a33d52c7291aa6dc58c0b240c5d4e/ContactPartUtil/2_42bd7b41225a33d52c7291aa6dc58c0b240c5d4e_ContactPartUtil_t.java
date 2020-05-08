 package de.hswt.hrm.contact.ui.part;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TableColumn;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.contact.model.Contact;
 import de.hswt.hrm.contact.ui.wizard.ContactWizard;
 
 public final class ContactPartUtil {
 
     private final static int WIDTH = 120;
 
     private ContactPartUtil() {
 
     }
 
     public static Optional<Contact> showWizard(Shell shell, Optional<Contact> contact) {
 
         ContactWizard cw = new ContactWizard(contact);
         WizardDialog wd = new WizardDialog(shell, cw);
         wd.open();
         return cw.getContact();
     }
    
    
 
     public static Map<String, String> getDefaultColumnHeaders() {
 
         // TODO Multilanguage support
 
         Map<String, String> columnHeaders = new HashMap<String, String>();
 
         columnHeaders.put("lastName", "Nachname");
         columnHeaders.put("firstName", "Vorname");
         columnHeaders.put("street", "Strasse");
         columnHeaders.put("streetNo", "Hausnummer");
         columnHeaders.put("postCode", "Postleitzahl");
         columnHeaders.put("city", "Stadt");
         columnHeaders.put("shortcut", "Kürzel");
         columnHeaders.put("phone", "Telefonnummer");
         columnHeaders.put("fax", "Fax");
         columnHeaders.put("mobile", "Mobil");
         columnHeaders.put("email", "E-mail");
 
         return columnHeaders;
     }
 
     /*
      * vogella, check license
      */
     public static void createColumns(Composite parent, TableViewer viewer,
             Map<String, String> columnHeaders, final ContactComperator comparator) {
 
         Menu headerMenu = new Menu(viewer.getTable());
         viewer.getTable().setMenu(headerMenu);
 
         // LastName
         TableViewerColumn col = createTableViewerColumn(columnHeaders.get("lastName"), WIDTH,
                 viewer, 0, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Contact c = (Contact) element;
                 return c.getLastName();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // firstName
         col = createTableViewerColumn(columnHeaders.get("firstName"), WIDTH, viewer, 1, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Contact c = (Contact) element;
                 return c.getFirstName();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // street
         col = createTableViewerColumn(columnHeaders.get("street"), WIDTH, viewer, 2, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Contact c = (Contact) element;
                 return c.getStreet();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // streetNo
         col = createTableViewerColumn(columnHeaders.get("streetNo"), WIDTH, viewer, 3, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Contact c = (Contact) element;
                 return c.getStreetNo();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // postCode
         col = createTableViewerColumn(columnHeaders.get("postCode"), WIDTH, viewer, 4, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Contact c = (Contact) element;
                 return c.getPostCode();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
         // city
         col = createTableViewerColumn(columnHeaders.get("city"), WIDTH, viewer, 5, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Contact c = (Contact) element;
                 return c.getCity();
             }
         });
         createMenuItem(headerMenu, col.getColumn());
         // mobile
         col = createTableViewerColumn(columnHeaders.get("mobile"), WIDTH, viewer, 6, comparator);
         col.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Contact c = (Contact) element;
                 return c.getMobile().get();
 
             }
         });
         createMenuItem(headerMenu, col.getColumn());
 
     }
 
     /*
      * vogella, check license
      */
     private static void createMenuItem(Menu parent, final TableColumn column) {
         final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
         itemName.setText(column.getText());
         itemName.setSelection(column.getResizable());
         itemName.addListener(SWT.Selection, new Listener() {
             public void handleEvent(Event event) {
                 if (itemName.getSelection()) {
                     column.setWidth(WIDTH);
                     column.setResizable(true);
                 }
                 else {
                     column.setWidth(0);
                     column.setResizable(false);
                 }
             }
         });
 
     }
 
     /*
      * vogella, check license
      */
     private static TableViewerColumn createTableViewerColumn(String title, int bound,
             TableViewer viewer, int colNumber, final ContactComperator comparator) {
         TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
         TableColumn column = viewerColumn.getColumn();
         column.setText(title);
         column.setWidth(bound);
         column.setResizable(true);
         column.setMoveable(true);
         column.addSelectionListener(getSelectionAdapter(viewer, column, colNumber, comparator));
         return viewerColumn;
     }
 
     private static SelectionListener getSelectionAdapter(final TableViewer viewer,
             final TableColumn column, final int index, final ContactComperator comparator) {
         SelectionAdapter selectionAdapter = new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
 
                 comparator.setColumn(index);
                 int dir = comparator.getDirection();
                 viewer.getTable().setSortDirection(dir);
                 viewer.getTable().setSortColumn(column);
                 viewer.refresh();
 
             }
         };
         return selectionAdapter;
     }
 }
