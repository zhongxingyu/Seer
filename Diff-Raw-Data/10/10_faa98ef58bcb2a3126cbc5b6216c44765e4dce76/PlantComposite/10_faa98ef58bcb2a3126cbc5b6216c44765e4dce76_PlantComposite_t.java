 package de.hswt.hrm.plant.ui.shared;
 
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
import de.hswt.hrm.i18n.I18n;
import de.hswt.hrm.i18n.I18nFactory;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.plant.service.PlantService;
 import de.hswt.hrm.plant.ui.shared.filter.PlantFilter;
 import de.hswt.hrm.plant.ui.shared.util.PlantPartUtil;
 
 public class PlantComposite extends Composite {
 
     public static final int TABLE_SELCTION_EVENT = SWT.Selection + 12341234;
 
     private final static Logger LOG = LoggerFactory.getLogger(PlantComposite.class);
    private final static I18n I18N = I18nFactory.getI18n(PlantComposite.class);
 
     private Text searchText;
 
     private TableViewer tableViewer;
 
     private Table table;
 
     private boolean allowEditing = true;
 
     @Inject
     private PlantService plantService;
 
     @Inject
     private IEclipseContext context;
 
     @Inject
     private IShellProvider shellProvider;
 
     public PlantComposite(Composite parent) {
         super(parent, SWT.NONE);
 
     }
 
     /**
      * Create contents of the view part.
      */
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
                 editPlace();
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
             tableViewer.setInput(plantService.findAll());
         }
         catch (DatabaseException e) {
             LOG.error("Unable to retrieve list of places.", e);
             showDBConnectionError();
         }
     }
 
     private void showDBConnectionError() {
        MessageDialog.openError(shellProvider.getShell(), I18N.tr("Connection Error"),
                I18N.tr("Could not load plants from Database."));
     }
 
     private void updateTableFilter(String filterString) {
         PlantFilter filter = (PlantFilter) tableViewer.getFilters()[0];
         filter.setSearchString(filterString);
         tableViewer.refresh();
     }
 
     private void initializeTable() {
         List<ColumnDescription<Plant>> columns = PlantPartUtil.getColumns();
 
         // Create columns in tableviewer
         TableViewerController<Plant> filler = new TableViewerController<>(tableViewer);
         filler.createColumns(columns);
 
         // Enable column selection
         filler.createColumnSelectionMenu();
 
         // Enable sorting
         ColumnComparator<Plant> comparator = new ColumnComparator<>(columns);
         filler.enableSorting(comparator);
 
         // Add dataprovider that handles our collection
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());
 
         // Enable filtering
         tableViewer.addFilter(new PlantFilter());
     }
 
     @SuppressWarnings("unchecked")
     public void addPlace() {
         Optional<Plant> newPlace = PlantPartUtil.showWizard(context, shellProvider.getShell(),
                 Optional.<Plant> absent());
 
         if (newPlace.isPresent()) {
             Collection<Plant> places = (Collection<Plant>) tableViewer.getInput();
             places.add(newPlace.get());
             tableViewer.refresh();
         }
     }
 
     public void editPlace() {
         if (!allowEditing) {
             return;
         }
         // obtain the place in the column where the doubleClick happend
         Plant selectedPlace = (Plant) tableViewer.getElementAt(tableViewer.getTable()
                 .getSelectionIndex());
         if (selectedPlace == null) {
             return;
         }
 
         // Refresh the selected place with values from the database
         try {
             plantService.refresh(selectedPlace);
             Optional<Plant> updatedPlace = PlantPartUtil.showWizard(context,
                     shellProvider.getShell(), Optional.of(selectedPlace));
 
             if (updatedPlace.isPresent()) {
                 tableViewer.refresh();
             }
         }
         catch (DatabaseException e) {
             LOG.error("Could not retrieve the place from database.", e);
             showDBConnectionError();
         }
     }
 
     public void setAllowEditing(boolean allowEditing) {
         this.allowEditing = allowEditing;
     }
 
     public void addSelectionChangedListener(ISelectionChangedListener listener) {
         tableViewer.addSelectionChangedListener(listener);
     }
 
     @Override
     protected void checkSubclass() {
     }
 
     public Plant getSelectedPlant() {
         return (Plant) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
     }
 
     public void addPlant() {
         Optional<Plant> newPlant = PlantPartUtil.showWizard(context, shellProvider.getShell(),
                 Optional.<Plant> absent());
 
         @SuppressWarnings("unchecked")
         Collection<Plant> contacs = (Collection<Plant>) tableViewer.getInput();
         if (newPlant.isPresent()) {
             contacs.add(newPlant.get());
             tableViewer.refresh();
         }
     }
 
     public void editPlant() {
 
         Optional<Plant> plant = getPlant();
         if (!plant.isPresent()) {
             return;
         }
         // Refresh the selected place with values from the database
         try {
             plantService.refresh(plant.get());
             Optional<Plant> updatedPlant = PlantPartUtil.showWizard(context,
                     shellProvider.getShell(), Optional.of(plant.get()));
 
             if (updatedPlant.isPresent()) {
                 tableViewer.refresh();
             }
         }
         catch (DatabaseException e) {
             LOG.error("Could not retrieve the plant from database.", e);
             showDBConnectionError();
         }
     }
 
     public Optional<Plant> getPlant() {
         if (table.getSelectionIndex() < 0) {
             return Optional.absent();
         }
         return Optional.fromNullable((Plant) tableViewer.getElementAt(tableViewer.getTable()
                 .getSelectionIndex()));
     }
 }
