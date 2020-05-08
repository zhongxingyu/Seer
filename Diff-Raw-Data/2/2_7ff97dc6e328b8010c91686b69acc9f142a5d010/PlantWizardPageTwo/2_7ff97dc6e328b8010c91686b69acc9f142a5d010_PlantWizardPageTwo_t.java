 package de.hswt.hrm.plant.ui.wizard;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.ui.swt.table.ColumnComparator;
 import de.hswt.hrm.common.ui.swt.table.ColumnDescription;
 import de.hswt.hrm.common.ui.swt.table.TableViewerController;
 import de.hswt.hrm.common.ui.xwt.XwtHelper;
 import de.hswt.hrm.place.model.Place;
 import de.hswt.hrm.place.service.PlaceService;
 import de.hswt.hrm.place.ui.filter.PlaceFilter;
 import de.hswt.hrm.place.ui.part.PlacePartUtil;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.plant.ui.event.PlaceEventHandlerForPlant;
 
 public class PlantWizardPageTwo extends WizardPage {
     private Composite container;
     private Optional<Plant> plant;
     private Collection<Place> places = null;
     private TableViewer viewer;
     private Button editPlace;
     private Button back;
     private static final Logger LOG = LoggerFactory.getLogger(PlantWizardPageTwo.class);
     @Inject
     private PlaceService placeService;
     @Inject
     private IEclipseContext context;
 
     public PlantWizardPageTwo(String title, Optional<Plant> plant) {
         super(title);
         this.plant = plant;
         if (plant.isPresent()) {
            setDescription("Standort Auswählen für Anlage mit der Bezeichnung: " + plant.get().getDescription());
         }
     }
 
     @Override
     public void createControl(Composite parent) {
 
         PlaceEventHandlerForPlant eventHandler = ContextInjectionFactory.make(
                 PlaceEventHandlerForPlant.class, context);
         URL url = PlantWizardPageTwo.class.getClassLoader().getResource(
                 "de/hswt/hrm/place/ui/xwt/PlaceView" + IConstants.XWT_EXTENSION_SUFFIX);
         try {
             container = (Composite) XwtHelper.loadWithEventHandler(parent, url, eventHandler);
             viewer = (TableViewer) XWT.findElementByName(container, "placeTable");
             editPlace = (Button) XWT.findElementByName(container, "editPlace");
             back = (Button) XWT.findElementByName(container, "back2Main");
 
         }
         catch (Exception e) {
             LOG.error("An error occured: ", e);
         }
 
         setControl(container);
         initializeTable(parent, viewer);
         refreshTable(parent);
         setButtonInvisible();
 
     }
 
     private void updateFields(Composite container) {
         Plant p = plant.get();
         int i = 1;
         while (viewer.getElementAt(i) != null) {
             if (viewer.getElementAt(i).equals(p.getPlace())) {
                 break;
             }
             else {
                 i++;
             }
         }
         Plant a = (Plant) viewer.getData(p.getPlace().get().getPlaceName());
         viewer.setSelection(new StructuredSelection(viewer.getElementAt(i)), true);
     }
 
     private void setButtonInvisible() {
         editPlace.setVisible(false);
         back.setVisible(false);
 
     }
 
     private void refreshTable(Composite parent) {
         try {
             places = placeService.findAll();
             viewer.setInput(places);
         }
         catch (DatabaseException e) {
             LOG.error("Unable to retrieve list of places.", e);
 
             // TODO: übersetzen
             MessageDialog.openError(parent.getShell(), "Connection Error",
                     "Could not load places from Database.");
         }
     }
 
     private void initializeTable(Composite parent, TableViewer viewer) {
         List<ColumnDescription<Place>> columns = PlacePartUtil.getColumns();
 
         // Create columns in tableviewer
         TableViewerController<Place> filler = new TableViewerController<>(viewer);
         filler.createColumns(columns);
 
         // Enable column selection
         filler.createColumnSelectionMenu();
 
         // Enable sorting
         ColumnComparator<Place> comparator = new ColumnComparator<>(columns);
         filler.enableSorting(comparator);
 
         // Add dataprovider that handles our collection
         viewer.setContentProvider(ArrayContentProvider.getInstance());
 
         // Enable filtering
         viewer.addFilter(new PlaceFilter());
 
         viewer.addSelectionChangedListener(new ISelectionChangedListener() {
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 getWizard().getContainer().updateButtons();
             }
         });
     }
 
     public TableViewer getTableViewer() {
         return viewer;
     }
 
     @Override
     public boolean isPageComplete() {
         IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
         if (sel.getFirstElement() == null) {
             return false;
         }
         else {
             return true;
         }
     }
 }
