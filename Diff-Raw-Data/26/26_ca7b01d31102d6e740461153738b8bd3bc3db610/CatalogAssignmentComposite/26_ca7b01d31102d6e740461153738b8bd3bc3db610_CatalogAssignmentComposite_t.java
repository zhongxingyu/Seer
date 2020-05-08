 package de.hswt.hrm.catalog.ui.part;
 
 import java.awt.event.KeyAdapter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Catalog;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.ICatalogItem;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.catalog.service.CatalogService;
 import de.hswt.hrm.catalog.ui.filter.CatalogTextFilter;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 
 public class CatalogAssignmentComposite extends Composite {
 
     private static final Logger LOG = LoggerFactory.getLogger(CatalogAssignmentComposite.class);
 
     @Inject
     private CatalogService catalogService;
 
     @Inject
     private IEclipseContext context;
 
     @Inject
     private IShellProvider shellProvider;
 
     private FormToolkit formToolkit = new FormToolkit(Display.getDefault());
     private Text assignedTargetSearch;
     private Text assignedCurrentSearch;
     private Text assignedActivitySearch;
     private Text availableTargetSearch;
     private Text availableCurrentSearch;
     private Text availableActivitySearch;
     private Text nameText;
     private Text descText;
 
     private ListViewer catalog;
     private ListViewer availableTarget;
     private ListViewer availableCurrent;
     private ListViewer availableActivity;
     private ListViewer assignedTarget;
     private ListViewer assignedCurrent;
     private ListViewer assignedActivity;
 
     private Button targetAdd;
     private Button targetRemove;
     private Button currentAdd;
     private Button currentRemove;
     private Button activityAdd;
     private Button activityRemove;
 
     private Collection<Catalog> catalogsFromDB;
 
     private List<Activity> activityFromDB;
 
     private List<Current> currentFromDB;
 
     private List<Target> targetFromDB;
 
     private List<Target> tempTarget;
 
     private List<Current> tempCurrent;
 
     private List<Activity> tempActivity;
 
     private Catalog selectedCatalog;
 
     private CatalogTextFilter searchFilter = new CatalogTextFilter();
 
     private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
         @Override
         public void selectionChanged(SelectionChangedEvent event) {
             ICatalogItem item = (ICatalogItem) ((IStructuredSelection) event.getSelectionProvider()
                     .getSelection()).getFirstElement();
             if (item == null) {
                 return;
             }
             nameText.setText(item.getName());
             descText.setText(item.getText());
         }
     };
 
     private IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
         @Override
         public void doubleClick(DoubleClickEvent event) {
             handleDoubleClickEvent(event.getViewer());
         }
     };
 
     /**
      * Do not use this constructor when instantiate this composite! It is only included to make the
      * WindowsBuilder working.
      * 
      * @param parent
      * @param style
      */
     private CatalogAssignmentComposite(Composite parent, int style) {
         super(parent, SWT.NONE);
         setBackgroundMode(SWT.INHERIT_FORCE);
         createControls();
     }
 
     /**
      * Create the composite.
      * 
      * @param parent
      */
     public CatalogAssignmentComposite(Composite parent) {
         super(parent, SWT.NONE);
         formToolkit.dispose();
         formToolkit = FormUtil.createToolkit();
         this.tempTarget = new ArrayList<>();
         this.tempCurrent = new ArrayList<>();
         this.tempActivity = new ArrayList<>();
 
     }
 
     @PostConstruct
     public void createControls() {
 
         if (catalogService == null) {
             LOG.error("SchemeService not properly injected to SchemePart.");
         }
 
         GridLayout gl = new GridLayout(4, true);
         setLayout(gl);
 
         Section catalogSection = formToolkit.createSection(this, Section.TITLE_BAR);
         catalogSection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(catalogSection);
         catalogSection.setText("Catalogs");
 
         Composite catalogComposite = formToolkit.createComposite(catalogSection);
         gl = new GridLayout();
         gl.marginWidth = 0;
         gl.marginHeight = 0;
         gl.marginTop = 5;
         catalogComposite.setLayout(gl);
         catalogSection.setClient(catalogComposite);
 
         catalog = new ListViewer(catalogComposite, SWT.BORDER | SWT.V_SCROLL);
         catalog.getList().setLayoutData(LayoutUtil.createFillData());
 
         Section assignedTargetSection = formToolkit.createSection(this, Section.TITLE_BAR);
         assignedTargetSection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(assignedTargetSection);
         assignedTargetSection.setText("Assigned Target");
 
         Composite assignedTargetComposite = new Composite(assignedTargetSection, SWT.NONE);
         formToolkit.adapt(assignedTargetComposite);
         formToolkit.paintBordersFor(assignedTargetComposite);
         assignedTargetSection.setClient(assignedTargetComposite);
         gl = new GridLayout();
         gl.marginWidth = 0;
         gl.marginHeight = 0;
         gl.marginTop = 5;
         assignedTargetComposite.setLayout(gl);
 
         assignedTargetSearch = new Text(assignedTargetComposite, SWT.H_SCROLL | SWT.SEARCH
                 | SWT.ICON_SEARCH | SWT.CANCEL);
         assignedTargetSearch.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(assignedTargetSearch, true, true);
 
         assignedTargetSearch.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 updateSearchTableFilter(assignedTarget, assignedTargetSearch.getText());
             }
         });
 
         assignedTarget = new ListViewer(assignedTargetComposite, SWT.BORDER | SWT.V_SCROLL);
         assignedTarget.getList().setLayoutData(LayoutUtil.createFillData());
 
         assignedTarget.addSelectionChangedListener(selectionChangedListener);
         assignedTarget.addDoubleClickListener(doubleClickListener);
 
         Section assignedCurrentSection = formToolkit.createSection(this, Section.TITLE_BAR);
         assignedCurrentSection.setLayoutData(LayoutUtil.createFillData());
         assignedCurrentSection.setBounds(0, 0, 105, 21);
         formToolkit.paintBordersFor(assignedCurrentSection);
         assignedCurrentSection.setText("Assigned Current");
 
         Composite assignedCurrentComposite = new Composite(assignedCurrentSection, SWT.NONE);
         formToolkit.adapt(assignedCurrentComposite);
         formToolkit.paintBordersFor(assignedCurrentComposite);
         assignedCurrentSection.setClient(assignedCurrentComposite);
         gl = new GridLayout();
         gl.marginWidth = 0;
         gl.marginHeight = 0;
         gl.marginTop = 5;
         assignedCurrentComposite.setLayout(gl);
 
         assignedCurrentSearch = new Text(assignedCurrentComposite, SWT.H_SCROLL | SWT.SEARCH
                 | SWT.ICON_SEARCH | SWT.CANCEL);
         assignedCurrentSearch.setLayoutData(LayoutUtil.createHorzFillData());
         assignedCurrentSearch.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 updateSearchTableFilter(assignedCurrent, assignedCurrentSearch.getText());
             }
         });
         formToolkit.adapt(assignedCurrentSearch, true, true);
 
         assignedCurrent = new ListViewer(assignedCurrentComposite, SWT.BORDER | SWT.V_SCROLL);
         assignedCurrent.getList().setLayoutData(LayoutUtil.createFillData());
 
         assignedCurrent.addSelectionChangedListener(selectionChangedListener);
         assignedCurrent.addDoubleClickListener(doubleClickListener);
 
         Section assignedActivitySection = formToolkit.createSection(this, Section.TITLE_BAR);
         assignedActivitySection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(assignedActivitySection);
         assignedActivitySection.setText("Assigned Activity");
 
         Composite matchedActivityComposite = new Composite(assignedActivitySection, SWT.NONE);
         formToolkit.adapt(matchedActivityComposite);
         formToolkit.paintBordersFor(matchedActivityComposite);
         assignedActivitySection.setClient(matchedActivityComposite);
         gl = new GridLayout();
         gl.marginWidth = 0;
         gl.marginHeight = 0;
         gl.marginTop = 5;
         matchedActivityComposite.setLayout(gl);
 
         assignedActivitySearch = new Text(matchedActivityComposite, SWT.H_SCROLL | SWT.SEARCH
                 | SWT.ICON_SEARCH | SWT.CANCEL);
         assignedActivitySearch.setLayoutData(LayoutUtil.createHorzFillData());
         assignedActivitySearch.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                updateSearchTableFilter(assignedActivity, assignedActivitySearch.getText());
             }
         });
         formToolkit.adapt(assignedActivitySearch, true, true);
 
         assignedActivity = new ListViewer(matchedActivityComposite, SWT.BORDER | SWT.V_SCROLL);
         assignedActivity.getList().setLayoutData(LayoutUtil.createFillData());
 
         assignedActivity.addSelectionChangedListener(selectionChangedListener);
         assignedActivity.addDoubleClickListener(doubleClickListener);
 
         new Label(this, SWT.NONE);
 
         Composite targetBtnComposite = formToolkit.createComposite(this, SWT.NONE);
         targetBtnComposite.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.paintBordersFor(targetBtnComposite);
         FillLayout fl = new FillLayout();
         fl.marginHeight = 5;
         targetBtnComposite.setLayout(fl);
 
         targetAdd = formToolkit.createButton(targetBtnComposite, "", SWT.ARROW | SWT.CENTER);
         targetAdd.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 selectedCatalog = getSelectedCatalog();
                 if (selectedCatalog == null) {
                     return;
                 }
                 addTarget(selectedCatalog, getSelectedListItem(availableTarget));
             }
         });
 
         targetRemove = formToolkit.createButton(targetBtnComposite, "", SWT.DOWN | SWT.ARROW
                 | SWT.CENTER);
         targetRemove.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 selectedCatalog = getSelectedCatalog();
                 if (selectedCatalog == null) {
                     return;
                 }
                 removeTarget(selectedCatalog, getSelectedListItem(assignedTarget));
             }
         });
 
         Composite currentBtnComposite = formToolkit.createComposite(this, SWT.NONE);
         currentBtnComposite.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.paintBordersFor(currentBtnComposite);
         fl = new FillLayout();
         fl.marginHeight = 5;
         currentBtnComposite.setLayout(fl);
 
         currentAdd = formToolkit.createButton(currentBtnComposite, "", SWT.ARROW | SWT.CENTER);
         currentAdd.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 addCurrent((Target) getSelectedListItem(assignedTarget),
                         getSelectedListItem(availableCurrent));
             }
         });
 
         currentRemove = formToolkit.createButton(currentBtnComposite, "", SWT.DOWN | SWT.ARROW
                 | SWT.CENTER);
         currentRemove.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 removeCurrent((Target) getSelectedListItem(assignedTarget),
                         getSelectedListItem(assignedCurrent));
             }
         });
 
         Composite activityBtnComposite = formToolkit.createComposite(this, SWT.NONE);
         activityBtnComposite.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.paintBordersFor(activityBtnComposite);
         fl = new FillLayout();
         fl.marginHeight = 5;
         activityBtnComposite.setLayout(fl);
 
         activityAdd = formToolkit.createButton(activityBtnComposite, "", SWT.ARROW | SWT.CENTER);
         activityAdd.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 addCurrent((Target) getSelectedListItem(assignedTarget),
                         getSelectedListItem(availableCurrent));
             }
         });
 
         activityRemove = formToolkit.createButton(activityBtnComposite, "", SWT.DOWN | SWT.ARROW
                 | SWT.CENTER);
         activityRemove.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 Current c = (Current) getSelectedListItem(assignedCurrent);
                 removeActivity(c, getSelectedListItem(assignedActivity));
             }
         });
 
         Section informationSection = formToolkit.createSection(this, Section.TITLE_BAR);
         informationSection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(informationSection);
         informationSection.setText("Information");
 
         Composite informationComposite = new Composite(informationSection, SWT.NONE);
         formToolkit.adapt(informationComposite);
         formToolkit.paintBordersFor(informationComposite);
         informationSection.setClient(informationComposite);
         gl = new GridLayout(2, false);
         gl.marginWidth = 0;
         informationComposite.setLayout(gl);
 
         Label nameLabel = new Label(informationComposite, SWT.NONE);
         formToolkit.adapt(nameLabel, true, true);
         nameLabel.setText("Name");
 
         nameText = new Text(informationComposite, SWT.BORDER | SWT.READ_ONLY);
         nameText.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(nameText, true, true);
 
         Label descLabel = new Label(informationComposite, SWT.NONE);
         descLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
         formToolkit.adapt(descLabel, true, true);
         descLabel.setText("Description");
 
         descText = new Text(informationComposite, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
         descText.setLayoutData(LayoutUtil.createFillData());
         formToolkit.adapt(descText, true, true);
 
         Section targetSection = formToolkit.createSection(this, Section.TITLE_BAR);
         targetSection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(targetSection);
         targetSection.setText("Available Target");
 
         Composite targetComposite = new Composite(targetSection, SWT.NONE);
         formToolkit.adapt(targetComposite);
         formToolkit.paintBordersFor(targetComposite);
         targetSection.setClient(targetComposite);
         gl = new GridLayout();
         gl.marginWidth = 0;
         targetComposite.setLayout(gl);
 
         availableTargetSearch = new Text(targetComposite, SWT.H_SCROLL | SWT.SEARCH
                 | SWT.ICON_SEARCH | SWT.CANCEL);
         availableTargetSearch.setLayoutData(LayoutUtil.createHorzFillData());
        availableTargetSearch.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateSearchTableFilter(availableTarget, availableTargetSearch.getText());
            }
        });
         formToolkit.adapt(availableTargetSearch, true, true);
 
         availableTarget = new ListViewer(targetComposite, SWT.BORDER | SWT.V_SCROLL);
         availableTarget.getList().setLayoutData(LayoutUtil.createFillData());
 
         availableTarget.addSelectionChangedListener(selectionChangedListener);
         availableTarget.addDoubleClickListener(doubleClickListener);
 
         Section currentSection = formToolkit.createSection(this, Section.TITLE_BAR);
         currentSection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(currentSection);
         currentSection.setText("Available Current");
 
         Composite currentComposite = new Composite(currentSection, SWT.NONE);
         formToolkit.adapt(currentComposite);
         formToolkit.paintBordersFor(currentComposite);
         currentSection.setClient(currentComposite);
         gl = new GridLayout();
         gl.marginWidth = 0;
         currentComposite.setLayout(gl);
 
         availableCurrentSearch = new Text(currentComposite, SWT.H_SCROLL | SWT.SEARCH
                 | SWT.ICON_SEARCH | SWT.CANCEL);
         availableCurrentSearch.setLayoutData(LayoutUtil.createHorzFillData());
        availableCurrentSearch.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateSearchTableFilter(availableCurrent, availableCurrentSearch.getText());
            }
        });
         formToolkit.adapt(availableCurrentSearch, true, true);
 
         availableCurrent = new ListViewer(currentComposite, SWT.BORDER | SWT.V_SCROLL);
         availableCurrent.getList().setLayoutData(LayoutUtil.createFillData());
 
         availableCurrent.addSelectionChangedListener(selectionChangedListener);
         availableCurrent.addDoubleClickListener(doubleClickListener);
 
         Section activitySection = formToolkit.createSection(this, Section.TITLE_BAR);
         activitySection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(activitySection);
         activitySection.setText("Available Activity");
 
         Composite activityComposite = new Composite(activitySection, SWT.NONE);
         formToolkit.adapt(activityComposite);
         formToolkit.paintBordersFor(activityComposite);
         activitySection.setClient(activityComposite);
         gl = new GridLayout();
         gl.marginWidth = 0;
         activityComposite.setLayout(gl);
 
         availableActivitySearch = new Text(activityComposite, SWT.H_SCROLL | SWT.SEARCH
                 | SWT.ICON_SEARCH | SWT.CANCEL);
         availableActivitySearch.setLayoutData(LayoutUtil.createHorzFillData());
        availableActivitySearch.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateSearchTableFilter(availableActivity, availableActivitySearch.getText());
            }
        });
         formToolkit.adapt(availableActivitySearch, true, true);
 
         availableActivity = new ListViewer(activityComposite, SWT.BORDER | SWT.V_SCROLL);
         availableActivity.getList().setLayoutData(LayoutUtil.createFillData());
 
         availableActivity.addSelectionChangedListener(selectionChangedListener);
         availableActivity.addDoubleClickListener(doubleClickListener);
 
         catalog.addSelectionChangedListener(new ISelectionChangedListener() {
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
 
                 selectedCatalog = getSelectedCatalog();
                 if (selectedCatalog == null) {
                     return;
                 }
                 obtainTargets(selectedCatalog);
 
             }
 
         });
 
         assignedTarget.getList().addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 Target t = (Target) getSelectedListItem(assignedTarget);
                 if (t == null) {
                     return;
                 }
                 obtainCurrents(t);
             }
         });
 
         assignedCurrent.getList().addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 Current c = (Current) getSelectedListItem(assignedCurrent);
                 if (c == null) {
                     return;
                 }
                 obtainActivities(c);
             }
         });
 
         obtainData();
         initListViewers();
 
     }
 
     private void initListViewers() {
         initializeCatalogViewer(catalog);
         initalize(availableActivity);
         initalize(availableCurrent);
         initalize(availableTarget);
         initalize(assignedActivity);
         initalize(assignedCurrent);
         initalize(assignedTarget);
         addFilter();
 
     }
 
     /**
      * Obtain all assigned targets for a given catalog from the database
      * 
      * @param c
      */
     private void obtainTargets(Catalog c) {
         List<Target> targets = null;
         try {
             // Obtain a list containing all targets for a given catalog
             targets = (List<Target>) catalogService.findTargetByCatalog(c);
 
         }
         catch (DatabaseException e) {
 
             LOG.error("An error occured", e);
         }
         if (targets.isEmpty()) {
 
             availableTarget.setInput(targetFromDB);
             assignedTarget.getList().removeAll();
 
         }
         else {
 
             // We have already matched targets...
             tempTarget.clear();
             tempTarget = new ArrayList<>(targetFromDB);
             assignedTarget.setInput(targets);
             tempTarget.removeAll(targets);
             availableTarget.setInput(tempTarget);
         }
     }
 
     private void obtainCurrents(Target t) {
         List<Current> currents = null;
         try {
             // Obtain a list containing all targets for a given catalog
             currents = (List<Current>) catalogService.findCurrentByTarget(t);
 
         }
         catch (DatabaseException e) {
 
             LOG.error("An error occured", e);
         }
 
         if (currents.isEmpty()) {
 
             availableCurrent.setInput(currentFromDB);
             assignedCurrent.getList().removeAll();
 
         }
         else {
 
             // We have already matched targets...
             tempCurrent.clear();
             tempCurrent = new ArrayList<>(currentFromDB);
             assignedCurrent.setInput(currents);
             tempCurrent.removeAll(currents);
             availableCurrent.setInput(tempCurrent);
         }
     }
 
     private void obtainActivities(Current c) {
         List<Activity> activities = null;
         try {
             // Obtain a list containing all targets for a given catalog
             activities = (List<Activity>) catalogService.findActivityByCurrent(c);
 
         }
         catch (DatabaseException e) {
 
             LOG.error("An error occured", e);
         }
 
         if (activities.isEmpty()) {
 
             availableActivity.setInput(activityFromDB);
             assignedActivity.getList().removeAll();
 
         }
         else {
 
             // We have already matched activities...
             tempActivity.clear();
             tempActivity = new ArrayList<>(activityFromDB);
             assignedActivity.setInput(activities);
             tempActivity.removeAll(activities);
             availableActivity.setInput(tempActivity);
         }
     }
 
     protected void handleDoubleClickEvent(Viewer viewer) {
         ICatalogItem item = (ICatalogItem) ((IStructuredSelection) viewer.getSelection())
                 .getFirstElement();
         selectedCatalog = getSelectedCatalog();
         if (selectedCatalog == null) {
             return;
         }
 
         if (viewer.equals(availableTarget)) {
             addTarget(selectedCatalog, item);
         }
         else if (viewer.equals(availableCurrent)) {
             Target t = (Target) getSelectedListItem(assignedTarget);
             addCurrent(t, item);
         }
         else if (viewer.equals(availableActivity)) {
             Current c = (Current) getSelectedListItem(assignedCurrent);
             addActivity(c, item);
         }
         else if (viewer.equals(assignedTarget)) {
             removeTarget(selectedCatalog, item);
         }
         else if (viewer.equals(assignedCurrent)) {
             Target t = (Target) getSelectedListItem(assignedTarget);
             removeCurrent(t, item);
         }
         else if (viewer.equals(assignedActivity)) {
             Current c = (Current) getSelectedListItem(assignedCurrent);
             removeActivity(c, item);
         }
     }
 
     private void addTarget(Catalog c, ICatalogItem item) {
         if (item == null) {
             return;
         }
         try {
 
             catalogService.addToCatalog(c, (Target) item);
 
         }
         catch (SaveException e) {
             LOG.error("An error occured", e);
         }
         moveItem(item, availableTarget, assignedTarget);
 
     }
 
     private void removeTarget(Catalog c, ICatalogItem item) {
         if (item == null) {
             return;
         }
         try {
             catalogService.removeFromCatalog(c, (Target) item);
         }
         catch (DatabaseException e) {
             LOG.error("An error occured", e);
         }
         moveItem(item, assignedTarget, availableTarget);
     }
 
     private void addCurrent(Target t, ICatalogItem item) {
 
         if (t == null || t == null) {
             return;
         }
         try {
             catalogService.addToTarget(t, (Current) item);
         }
         catch (DatabaseException e) {
             LOG.error("An error occured", e);
         }
         moveItem(item, availableCurrent, assignedCurrent);
 
     }
 
     private void removeCurrent(Target t, ICatalogItem item) {
         if (item == null || item == null) {
             return;
         }
         try {
             catalogService.removeFromTarget(t, (Current) item);
         }
         catch (DatabaseException e) {
             LOG.error("An error occured", e);
         }
         moveItem(item, assignedCurrent, availableCurrent);
     }
 
     private void addActivity(Current c, ICatalogItem item) {
         if (item == null) {
             return;
         }
         try {
             catalogService.addToCurrent(c, (Activity) item);
         }
         catch (DatabaseException e) {
             LOG.error("An error occured", e);
         }
         moveItem(item, availableActivity, assignedActivity);
     }
 
     private void removeActivity(Current c, ICatalogItem item) {
         if (item == null || c == null) {
             return;
         }
         try {
             catalogService.removeFromCurrent(c, (Activity) item);
         }
         catch (DatabaseException e) {
             LOG.error("An error occured", e);
         }
         moveItem(item, assignedCurrent, availableCurrent);
     }
 
     private void moveItem(ICatalogItem item, ListViewer source, ListViewer destination) {
 
         if (item == null) {
             return;
         }
 
         destination.add(item);
         source.remove(item);
 
     }
 
     private Catalog getSelectedCatalog() {
         IStructuredSelection selection = (IStructuredSelection) catalog.getSelection();
         return (Catalog) selection.getFirstElement();
 
     }
 
     private ICatalogItem getSelectedListItem(ListViewer viewer) {
         if (viewer.getSelection() == null) {
             return null;
         }
 
         return (ICatalogItem) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
     }
 
     // Obtain items from the Database
     private void obtainData() {
         try {
             catalogsFromDB = catalogService.findAllCatalog();
             activityFromDB = (List<Activity>) catalogService.findAllActivity();
             currentFromDB = (List<Current>) catalogService.findAllCurrent();
             targetFromDB = (List<Target>) catalogService.findAllTarget();
         }
         catch (DatabaseException e) {
             LOG.error("An error occured", e);
         }
 
     }
 
     // Init Catalog TableViewer
     private void initializeCatalogViewer(ListViewer catalog) {
         catalog.setContentProvider(ArrayContentProvider.getInstance());
         catalog.setLabelProvider(new LabelProvider() {
             @Override
             public String getText(Object element) {
                 Catalog c = (Catalog) element;
                 return c.getName();
             }
         });
         catalog.setComparator(new ViewerComparator() {
             @Override
             public int compare(Viewer viewer, Object e1, Object e2) {
 
                 Catalog c1 = (Catalog) e1;
                 Catalog c2 = (Catalog) e2;
                 return c1.getName().compareToIgnoreCase(c2.getName());
 
             }
 
         });
         catalog.setInput(catalogsFromDB);
     }
 
     private void initalize(ListViewer lv) {
 
         lv.setContentProvider(ArrayContentProvider.getInstance());
 
         lv.setLabelProvider(new LabelProvider() {
             @Override
             public String getText(Object element) {
                 ICatalogItem c = (ICatalogItem) element;
                 return c.getName();
             }
         });
 
         lv.setComparator(new ViewerComparator() {
 
             @Override
             public int compare(Viewer viewer, Object e1, Object e2) {
 
                 ICatalogItem ic1 = (ICatalogItem) e1;
                 ICatalogItem ic2 = (ICatalogItem) e2;
                 return ic1.getName().compareToIgnoreCase(ic2.getName());
 
             }
 
         });
     }
 
     private void addFilter() {
 
         availableActivity.addFilter(searchFilter);
         availableCurrent.addFilter(searchFilter);
         availableTarget.addFilter(searchFilter);
 
         assignedTarget.addFilter(searchFilter);
         assignedActivity.addFilter(searchFilter);
         assignedCurrent.addFilter(searchFilter);
 
     }
 
     private void updateSearchTableFilter(ListViewer viewer, String filterString) {
         searchFilter.setSearchString(filterString);
         viewer.refresh();
     }
 
 }
