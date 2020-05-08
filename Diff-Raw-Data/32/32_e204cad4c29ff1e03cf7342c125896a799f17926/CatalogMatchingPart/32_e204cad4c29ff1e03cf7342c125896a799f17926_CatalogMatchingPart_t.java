 package de.hswt.hrm.catalog.ui.part;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.List;
 
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.ICatalogItem;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.catalog.service.CatalogService;
 import de.hswt.hrm.common.ui.swt.table.ColumnDescription;
 import de.hswt.hrm.common.ui.swt.table.TableViewerController;
 import de.hswt.hrm.component.model.Category;
 
 public class CatalogMatchingPart {
 
     @Inject
     CatalogService catalogService;
 
     TableViewer targets;
     TableViewer currents;
     TableViewer activities;
    ListViewer categories;
 
     @PostConstruct
     public void postConstruct(Composite parent, IEclipseContext context) {
 
         URL url = CatalogPart.class.getClassLoader().getResource(
                 "de/hswt/hrm/catalog/ui/xwt/CatalogMatchingView" + IConstants.XWT_EXTENSION_SUFFIX);
 
         try {
             final Composite composite = (Composite) XWTForms.load(parent, url);
 
             targets = (TableViewer) XWT.findElementByName(composite, "availableTarget");
             currents = (TableViewer) XWT.findElementByName(composite, "availableCurrent");
             activities = (TableViewer) XWT.findElementByName(composite, "availableActivity");
            categories = (ListViewer) XWT.findElementByName(composite, "categories");
 
             Category c = new Category("Luftfilter", 2, 2, 2, true);
             Category c2 = new Category("Nacherhitzer", 2, 2, 2, true);
 
             categories.add(c.getName());
             categories.add(c2.getName());
 
            Collection<ICatalogItem> items = catalogService.findAllCatalogItem();

            initializeTables(parent, targets, currents, activities, items);

         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void initializeTables(Composite parent, TableViewer targets, TableViewer currents,
             TableViewer activities, Collection<ICatalogItem> items) {
 
         java.util.List<ColumnDescription<ICatalogItem>> columns = CatalogMatchingPartUtil
                 .getColumns();
 
         // Create columns in tableviewer
         TableViewerController<ICatalogItem> targetFiller = new TableViewerController<>(targets);
 
         targetFiller.createColumns(columns);
 
         // Enable column selection
         targetFiller.createColumnSelectionMenu();
 
         // Create columns in tableviewer
         TableViewerController<ICatalogItem> activityFiller = new TableViewerController<>(activities);
 
         activityFiller.createColumns(columns);
 
         // Enable column selection
         activityFiller.createColumnSelectionMenu();
 
         // Create columns in tableviewer
         TableViewerController<ICatalogItem> currentsFiller = new TableViewerController<>(currents);
 
         currentsFiller.createColumns(columns);
 
         // Enable column selection
         currentsFiller.createColumnSelectionMenu();
 
         Collection<Activity> a = new ArrayList<>();
         Collection<Current> c = new ArrayList<>();
         Collection<Target> t = new ArrayList<>();
 
         targets.setContentProvider(ArrayContentProvider.getInstance());
         currents.setContentProvider(ArrayContentProvider.getInstance());
         activities.setContentProvider(ArrayContentProvider.getInstance());
 
         for (ICatalogItem i : items) {
             if (i instanceof Activity) {
                 a.add((Activity) i);
             }
             else if (i instanceof Current) {
                 c.add((Current) i);
             }
             else {
                 t.add((Target) i);
             }
         }
 
         targets.setInput(t);
         currents.setInput(c);
         activities.setInput(a);
 
         targets.getTable().getColumn(1).setWidth(0);
         targets.getTable().getColumn(1).setResizable(false);
 
         currents.getTable().getColumn(1).setWidth(0);
         currents.getTable().getColumn(1).setResizable(false);
 
         activities.getTable().getColumn(1).setWidth(0);
         activities.getTable().getColumn(1).setResizable(false);

        activities.getTable().setEnabled(false);
        currents.getTable().setEnabled(false);
     }
 
 }
