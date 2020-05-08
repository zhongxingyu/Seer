 package de.hswt.hrm.contact.ui.part;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.ui.swt.constants.SearchFieldConstants;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 import de.hswt.hrm.common.ui.swt.table.ColumnComparator;
 import de.hswt.hrm.common.ui.swt.table.ColumnDescription;
 import de.hswt.hrm.common.ui.swt.table.TableViewerController;
 import de.hswt.hrm.contact.model.Contact;
 import de.hswt.hrm.contact.service.ContactService;
 import de.hswt.hrm.contact.ui.filter.ContactFilter;
import de.hswt.hrm.i18n.I18n;
import de.hswt.hrm.i18n.I18nFactory;
 
 public class ContactComposite extends Composite {
 
     public static final int TABLE_SELCTION_EVENT = SWT.Selection + 12341234;
 
     private final static Logger LOG = LoggerFactory.getLogger(ContactComposite.class);
    
    private final static I18n I18N = I18nFactory.getI18n(ContactComposite.class);
 
     private Text searchText;
 
     private TableViewer tableViewer;
 
     private Table table;
 
     private boolean allowEditing = true;
 
     @Inject
     private ContactService contactService;
 
     @Inject
     private IEclipseContext context;
 
     @Inject
     private IShellProvider shellProvider;
 
     /**
      * Do not use this constructor when instantiate this composite! It is only included to make the
      * WindowsBuilder working.
      * 
      * @param parent
      * @param style
      */
     private ContactComposite(Composite parent, int style) {
         super(parent, style);
         createControls();
     }
 
     /**
      * Create the composite.
      * 
      * @param parent
      */
     public ContactComposite(Composite parent) {
         super(parent, SWT.NONE);
     }
 
     @PostConstruct
     public void createControls() {
         this.setLayout(new FillLayout());
         this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 
         Composite composite = new Composite(this, SWT.NONE);
         composite.setLayout(new GridLayout(1, false));
 
         searchText = new Text(composite, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.CANCEL
                 | SWT.ICON_CANCEL);
         searchText.setMessage(SearchFieldConstants.DEFAULT_SEARCH_STRING);
         searchText.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 updateTableFilter(searchText.getText());
             }
         });
         searchText.setLayoutData(LayoutUtil.createHorzFillData());
 
         tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
         tableViewer.addDoubleClickListener(new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 editContact();
             }
         });
 
         table = tableViewer.getTable();
         table.setLinesVisible(true);
         table.setHeaderVisible(true);
         GridData gd = LayoutUtil.createFillData();
         gd.widthHint = 800;
         gd.heightHint = 300;
         table.setLayoutData(gd);
 
         initializeTable();
         refreshTable();
     }
 
     private void refreshTable() {
         try {
             tableViewer.setInput(contactService.findAll());
         }
         catch (DatabaseException e) {
             LOG.error("Unable to retrieve list of contacs.", e);
             showDBConnectionError();
         }
     }
 
     private void showDBConnectionError() {
        // TODO check translation
        MessageDialog.openError(shellProvider.getShell(), I18N.tr("Connection Error"),
                I18N.tr("Could not load contacts from Database."));
     }
 
     private void updateTableFilter(String filterString) {
         ContactFilter filter = (ContactFilter) tableViewer.getFilters()[0];
         filter.setSearchString(filterString);
         tableViewer.refresh();
     }
 
     private void initializeTable() {
         List<ColumnDescription<Contact>> columns = ContactPartUtil.getColumns();
 
         // Create columns in tableviewer
         TableViewerController<Contact> filler = new TableViewerController<>(tableViewer);
         filler.createColumns(columns);
 
         // Enable column selection
         filler.createColumnSelectionMenu();
 
         // Enable sorting
         ColumnComparator<Contact> comparator = new ColumnComparator<>(columns);
         filler.enableSorting(comparator);
 
         // Add dataprovider that handles our collection
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());
 
         // Enable filtering
         tableViewer.addFilter(new ContactFilter());
     }
 
     @SuppressWarnings("unchecked")
     public void addContact() {
         Optional<Contact> newPlace = ContactPartUtil.showWizard(context, shellProvider.getShell(),
                 Optional.<Contact> absent());
 
         if (newPlace.isPresent()) {
             Collection<Contact> contacts = (Collection<Contact>) tableViewer.getInput();
             contacts.add(newPlace.get());
             tableViewer.refresh();
         }
     }
 
     public void editContact() {
         if (!allowEditing) {
             return;
         }
         // obtain the place in the column where the doubleClick happend
         Contact selectedPlace = (Contact) tableViewer.getElementAt(tableViewer.getTable()
                 .getSelectionIndex());
         if (selectedPlace == null) {
             return;
         }
 
         // Refresh the selected place with values from the database
         try {
             contactService.refresh(selectedPlace);
             Optional<Contact> updatedContact = ContactPartUtil.showWizard(context,
                     shellProvider.getShell(), Optional.of(selectedPlace));
 
             if (updatedContact.isPresent()) {
                 tableViewer.refresh();
             }
         }
         catch (DatabaseException e) {
             LOG.error("Could not retrieve the contacts from database.", e);
             showDBConnectionError();
         }
     }
 
     @Override
     protected void checkSubclass() {
     }
 
     public TableViewer getTableViewer() {
         return this.tableViewer;
     }
 
     public void setSelection(Contact contact) {
         tableViewer.setSelection(new StructuredSelection(contact), true);
     }
 
     public Contact getSelectedContact() {
         return (Contact) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
     }
 
     public void setAllowEditing(boolean allowEditing) {
         this.allowEditing = allowEditing;
     }
 
     public boolean isAllowEditing() {
         return allowEditing;
     }
 
     public void addSelectionChangedListener(ISelectionChangedListener listener) {
         tableViewer.addSelectionChangedListener(listener);
     }
 
     public void removeSelectionChangedListener(ISelectionChangedListener listener) {
         tableViewer.removeSelectionChangedListener(listener);
     }
 }
