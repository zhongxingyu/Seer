 package de.hswt.hrm.scheme.ui.part;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.ui.workbench.modeling.EPartService;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Slider;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.dialogs.PatternFilter;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Collections2;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.ui.swt.constants.SearchFieldConstants;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 import de.hswt.hrm.common.ui.swt.utils.SWTResourceManager;
 import de.hswt.hrm.component.model.Attribute;
 import de.hswt.hrm.component.service.ComponentService;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.scheme.model.RenderedComponent;
 import de.hswt.hrm.scheme.model.Scheme;
 import de.hswt.hrm.scheme.model.SchemeComponent;
 import de.hswt.hrm.scheme.service.ComponentConverter;
 import de.hswt.hrm.scheme.service.SchemeService;
 import de.hswt.hrm.scheme.ui.ComponentLoadThread;
 import de.hswt.hrm.scheme.ui.ItemClickListener;
 import de.hswt.hrm.scheme.ui.SchemeGrid;
 import de.hswt.hrm.scheme.ui.SchemeGridItem;
 import de.hswt.hrm.scheme.ui.SchemeTreePatternFilter;
 import de.hswt.hrm.scheme.ui.dialog.EditAtrributesDialog;
 import de.hswt.hrm.scheme.ui.dialog.SchemeCopySelectionDialog;
 import de.hswt.hrm.scheme.ui.dialog.SchemeImportSelectionDialog;
 import de.hswt.hrm.scheme.ui.dnd.DragData;
 import de.hswt.hrm.scheme.ui.dnd.DragDataTransfer;
 import de.hswt.hrm.scheme.ui.dnd.GridDragListener;
 import de.hswt.hrm.scheme.ui.dnd.GridDropTargetListener;
 import de.hswt.hrm.scheme.ui.dnd.TreeDragListener;
 import de.hswt.hrm.scheme.ui.tree.SchemeTreeLabelProvider;
 import de.hswt.hrm.scheme.ui.tree.TreeContentProvider;
 
 public class SchemeComposite extends Composite {
 
     /**
      * How much is the grid to be move on button press?
      */
     private static final int MOVE_AMOUNT = 3;
 
     /**
      * Which Drag operations are allowed
      */
     private static final int DRAG_OPS = DND.DROP_COPY;
 
     /**
      * Which Drop operations are allowed
      */
     private static final int DROP_OPS = DND.DROP_COPY;
 
     /**
      * The pixel per grid range. Defines how far you can zoom in and out
      */
     private static final int MIN_PPG = 20, MAX_PPG = 70;
 
     /**
      * The DND transfer type
      */
     private static final Transfer[] TRANSFER = new Transfer[] { DragDataTransfer.getInstance() };
 
     private final static Logger LOG = LoggerFactory.getLogger(SchemeComposite.class);
     private static final String DELETE = "Löschen";
     private static final String EDIT = "Edit Attributes";
 
     @Inject
     private SchemeService schemeService;
 
     @Inject
     private EPartService service;
 
     @Inject
     private ComponentService componentsService;
 
     @Inject
     private IShellProvider shellProvider;
 
     @Inject
     private IEclipseContext context;
 
     private ActionContributionItem saveContribution;
     private IContributionItem moveXContribution;
     private IContributionItem moveYContribution;
     private IContributionItem copyContribution;
     private IContributionItem importContribution;
 
     private List<IContributionItem> contributionItems = new ArrayList<IContributionItem>();
 
     /**
      * The background color of the scheme editor.
      */
     private static final RGB EDITOR_BACKGROUND = new RGB(255, 255, 255);
 
     private SchemeGrid grid;
 
     private TreeViewer tree;
 
     /**
      * The PatternFilter defines which TreeItems are visible for a given search pattern
      */
     private PatternFilter filter;
 
     private Plant plant;
 
     private Scheme currentScheme;
 
     /*
      * DND items for the grid
      */
     private DragSource gridDragSource;
     private DropTarget gridDropTarget;
 
     private GridDropTargetListener gridListener;
     private GridDragListener gridDragListener;
     private TreeDragListener treeDragListener;
 
     private ScrolledComposite scrolledComposite;
 
     private Composite treeComposite;
     private Slider zoomSlider;
     private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
     private Section schemeSection;
 
     /**
      * Create the composite.
      * 
      * @param parent
      * @param style
      */
     private SchemeComposite(Composite parent, int style) {
         super(parent, style);
         createControls();
     }
 
     /**
      * Create the composite.
      * 
      * @param parent
      */
     public SchemeComposite(Composite parent) {
         super(parent, SWT.NONE);
     }
 
     @PostConstruct
     private void createControls() {
         setLayout(new FillLayout());
 
         if (schemeService == null) {
             LOG.error("SchemeService not properly injected to SchemePart.");
         }
 
         schemeSection = formToolkit.createSection(this, Section.TITLE_BAR);
         schemeSection.setBackgroundMode(SWT.INHERIT_NONE);
         formToolkit.paintBordersFor(schemeSection);
         schemeSection.setText("Scheme");
         schemeSection.setExpanded(true);
         FormUtil.initSectionColors(schemeSection);
 
         Composite composite = new Composite(schemeSection, SWT.NONE);
         composite.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
         composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         schemeSection.setClient(composite);
         composite.setLayout(new GridLayout(4, false));
 
         treeComposite = new Composite(composite, SWT.NONE);
         treeComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2));
 
         GridLayout gl = new GridLayout();
         gl.marginHeight = 0;
         gl.marginWidth = 0;
         treeComposite.setLayout(gl);
 
         scrolledComposite = new ScrolledComposite(composite, SWT.BORDER | SWT.H_SCROLL
                 | SWT.V_SCROLL);
         scrolledComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         scrolledComposite.setExpandHorizontal(false);
         scrolledComposite.setExpandVertical(false);
         scrolledComposite.setLayoutData(LayoutUtil.createFillData(3));
 
         Label zoomMinusLabel = new Label(composite, SWT.NONE);
         zoomMinusLabel.setFont(SWTResourceManager.getFont(
                 zoomMinusLabel.getFont().getFontData()[0].getName(), 15, SWT.BOLD));
         zoomMinusLabel.setText("-");
         zoomMinusLabel.setLayoutData(LayoutUtil.createLeftGridData());
         zoomSlider = new Slider(composite, SWT.HORIZONTAL);
         zoomSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
         Label zoomPlusLabel = new Label(composite, SWT.NONE);
         zoomPlusLabel.setFont(SWTResourceManager.getFont(
                 zoomPlusLabel.getFont().getFontData()[0].getName(), 15, SWT.BOLD));
         zoomPlusLabel.setText("+");
         zoomPlusLabel.setLayoutData(LayoutUtil.createRightGridData());
 
         initTree();
         initSchemeGrid();
         initGridDropTarget();
         initGridDragSource();
         initGridDropTargetListener();
         initTreeDND();
         initGridDND();
         initSlider();
         initItemClickListener();
 
         grid.addModifyListener(new de.hswt.hrm.scheme.listener.ModifyListener() {
             @Override
             public void modified(Event e) {
                 saveContribution.getAction().setEnabled(grid.isDirty());
             }
         });
 
         initSaveContribution();
         initCopyContribution();
         initImportContribution();
         initMoveXContribution();
         initMoveYContribution();
         contributionItems.add(moveYContribution);
         contributionItems.add(moveXContribution);
         contributionItems.add(importContribution);
         contributionItems.add(copyContribution);
         contributionItems.add(saveContribution);
 
         new ComponentLoadThread(this, componentsService).start();
     }
 
     /**
      * The given scheme is loaded into the editor. The user can save a new scheme for the given
      * plant.
      * 
      * @param scheme
      * @throws IOException
      *             If a pdf file could not be read
      */
     public void modifyScheme(Scheme scheme) throws IOException {
         if (scheme == null || !scheme.getPlant().isPresent()) {
             throw new IllegalArgumentException(
                     "The scheme must not be null and plant must be present here");
         }
 
         if (!checkToSave(scheme)) {
             return;
         }
 
         currentScheme = scheme;
         plant = scheme.getPlant().get();
         grid.setItems(toSchemeGridItems(scheme.getSchemeComponents()));
         clearDirty();
     }
 
     /**
      * @return Was the grid changed since last modifyScheme() or newScheme() call
      */
     public boolean isDirty() {
         return grid.isDirty();
     }
 
     private List<SchemeGridItem> toSchemeGridItems(Collection<SchemeComponent> sc)
             throws IOException {
         List<SchemeGridItem> l = new ArrayList<>();
         for (SchemeComponent c : sc) {
             l.add(new SchemeGridItem(
                     ComponentConverter.convert(this.getDisplay(), c.getComponent()), c
                             .getDirection(), c.getX(), c.getY()));
         }
         return l;
     }
 
     /*
      * init gui elements
      */
 
     private void initMoveXContribution() {
         // TODO translate
         Action moveXAction = new Action("Move X") {
             @Override
             public void run() {
                 super.run();
                 Collection<SchemeGridItem> items = grid.getItems();
                 items = Collections2.transform(items,
                         new Function<SchemeGridItem, SchemeGridItem>() {
                             public SchemeGridItem apply(SchemeGridItem item) {
                             	SchemeGridItem nItem = new SchemeGridItem(item);
                             	nItem.setX(item.getX() + MOVE_AMOUNT);
                                 return nItem;
                             }
                         });
                 grid.setItems(items);
             }
         };
         moveXAction.setDescription("Move's the grid elements in x direction.");
         moveXContribution = new ActionContributionItem(moveXAction);
     }
 
     private void initMoveYContribution() {
         // TODO translate
         Action moveYAction = new Action("Move Y") {
             @Override
             public void run() {
                 super.run();
                 Collection<SchemeGridItem> items = grid.getItems();
                 items = Collections2.transform(items,
                         new Function<SchemeGridItem, SchemeGridItem>() {
                             public SchemeGridItem apply(SchemeGridItem item) {
                             	SchemeGridItem nItem = new SchemeGridItem(item);
                             	nItem.setY(item.getY() + MOVE_AMOUNT);
                                 return nItem;
                             }
                         });
                 grid.setItems(items);
             }
         };
         moveYAction.setDescription("Move's the grid elements in y direction.");
         moveYContribution = new ActionContributionItem(moveYAction);
     }
 
     private void initCopyContribution() {
         Action copyAction = new Action("Copy") {
             @Override
             public void run() {
                 copyScheme();
             }
         };
         copyAction.setDescription("Copy an existing scheme and set it as current.");
         copyContribution = new ActionContributionItem(copyAction);
     }
 
     private void initImportContribution() {
         Action importAction = new Action("Import") {
             @Override
             public void run() {
                 importScheme();
             }
         };
         importAction.setDescription("Import an existing scheme from "
                 + "another plant and set it as current.");
         importContribution = new ActionContributionItem(importAction);
     }
 
     private void initSaveContribution() {
         // TODO translate
         Action saveAction = new Action("Save") {
             @Override
             public void run() {
                 saveScheme();
             }
         };
         saveAction.setDescription("Save the current scheme.");
         saveAction.setEnabled(false);
         saveContribution = new ActionContributionItem(saveAction);
     }
 
     private void initItemClickListener() {
         grid.setItemClickListener(new ItemClickListener() {
 
             @Override
             public void itemClicked(final MouseEvent e, final SchemeGridItem item) {
                 if (e.button == 3) { // right mouse click
                     final Menu menu = new Menu(shellProvider.getShell(), SWT.POP_UP);
                     final MenuItem delete = new MenuItem(menu, SWT.PUSH);
                     final MenuItem editAttribute = new MenuItem(menu, SWT.PUSH);
                     delete.addSelectionListener(new SelectionListener() {
 
                         @Override
                         public void widgetSelected(SelectionEvent e) {
                             grid.removeItem(item);
                         }
 
                         @Override
                         public void widgetDefaultSelected(SelectionEvent e) {
                         }
                     });
                     editAttribute.addSelectionListener(new SelectionListener() {
 
                         @Override
                         public void widgetSelected(SelectionEvent e) {
                             editAttribute(item);
 
                         }
 
                         @Override
                         public void widgetDefaultSelected(SelectionEvent e) {
 
                         }
                     });
                     delete.setText(DELETE);
                     editAttribute.setText(EDIT);
                     menu.setVisible(true);
                 }
             }
         });
 
     }
 
     private void editAttribute(SchemeGridItem item) {
 
         try {
             Collection<Attribute> attributes = componentsService.findAttributesByComponent(item
                     .getRenderedComponent().getComponent());
             if (attributes.isEmpty()) {
                 MessageDialog.openError(shellProvider.getShell(), "No Atrributes",
                         "The selected Component "
                                 + item.getRenderedComponent().getComponent().getName()
                                 + " has no assigend Attributes");
                 return;
             }
             //
             // Map<Attribute, String> assignedValues = schemeService
             // .findAttributesOfSchemeComponent(item.asSchemeComponent());
             EditAtrributesDialog eda = new EditAtrributesDialog(shellProvider.getShell(), item
                     .getRenderedComponent().getComponent(), attributes);
             eda.open();
 
         }
         catch (DatabaseException e) {
             LOG.error("an error occured", e);
         }
 
     }
 
     private void initGridDropTargetListener() {
         gridListener = new GridDropTargetListener(grid, this);
         gridDropTarget.addDropListener(gridListener);
     }
 
     private void initGridDragSource() {
         gridDragSource = new DragSource(grid, DRAG_OPS);
         gridDragSource.setTransfer(TRANSFER);
     }
 
     private void initGridDropTarget() {
         gridDropTarget = new DropTarget(grid, DROP_OPS);
         gridDropTarget.setTransfer(TRANSFER);
     }
 
     private void initTreeDND() {
         treeDragListener = new TreeDragListener(tree, grid);
         tree.addDragSupport(DRAG_OPS, TRANSFER, treeDragListener);
     }
 
     private void initGridDND() {
         gridDragListener = new GridDragListener(grid);
         gridDragSource.addDragListener(gridDragListener);
     }
 
     private void initSchemeGrid() {
         grid = new SchemeGrid(scrolledComposite, SWT.NONE, 40, 20, 40);
         grid.setBackground(new Color(this.getDisplay(), EDITOR_BACKGROUND));
         scrolledComposite.setContent(grid);
         grid.addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseScrolled(MouseEvent e) {
                 changeZoom(e.count);
             }
         });
     }
 
     private void initTree() {
         /*
          * A FilteredTree can't be created in XWT, so its done here
          */
         filter = new SchemeTreePatternFilter();
         final FilteredTree filteredTree = new FilteredTree(treeComposite, SWT.MULTI | SWT.H_SCROLL
                 | SWT.V_SCROLL, filter, true);
         filteredTree.getFilterControl().addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 updatePatternFilter(filteredTree.getFilterControl().getText());
             }
         });
         // TODO translate
         filteredTree.setInitialText(SearchFieldConstants.DEFAULT_SEARCH_STRING);
         GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
         gd.widthHint = 200;
         filteredTree.setLayoutData(gd);
         tree = filteredTree.getViewer();
         tree.setContentProvider(new TreeContentProvider());
         tree.setLabelProvider(new SchemeTreeLabelProvider());
     }
 
     private void initSlider() {
         zoomSlider.setMaximum(MAX_PPG);
         zoomSlider.setMinimum(MIN_PPG);
         zoomSlider.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateZoom();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
         updateZoom();
     }
 
     /*
      * setter
      */
 
     private void changeZoom(int amount) {
         zoomSlider.setSelection(zoomSlider.getSelection() + amount);
         updateZoom();
     }
 
     /*
      * getter
      */
     private void clearDirty() {
         grid.clearDirty();
         saveContribution.getAction().setEnabled(grid.isDirty());
     }
 
     /*
      * tools
      */
     private boolean checkToSave(Scheme scheme) {
         if (scheme.equals(currentScheme)) {
             return false;
         }
         if (currentScheme == null || !grid.isDirty()) {
             return true;
         }
 
         MessageBox md = new MessageBox(shellProvider.getShell(), SWT.ICON_QUESTION | SWT.YES
                 | SWT.NO | SWT.CANCEL);
         md.setText("Save changes?");
         md.setMessage("The current scheme has unsaved changes. Would you save them now?"
                 + "\n\nOtherwise your unsaved changes are lost.");
         int res = md.open();
 
         if (res == SWT.YES) {
             saveScheme();
             return true;
         }
         else if (res == SWT.NO) {
             return true;
         }
 
         return false;
     }
 
     /*
      * updates
      */
 
     private void updateZoom() {
         grid.setPixelPerGrid(zoomSlider.getSelection());
     }
 
     private void updatePatternFilter(String text) {
         filter.setPattern(text);
         tree.refresh();
         if (text.trim().length() == 0) {
             tree.collapseAll();
         }
         else {
             tree.expandAll();
         }
     }
 
     private void copyScheme() {
         if (grid.isDirty()) {
 
         }
 
         SchemeCopySelectionDialog dialog = new SchemeCopySelectionDialog(shellProvider.getShell(),
                 context, plant);
         ContextInjectionFactory.inject(dialog, context);
 
         if (dialog.open() != Window.OK) {
             return;
         }
 
         try {
             Scheme scheme = schemeService.copy(dialog.getScheme(), plant);
             modifyScheme(scheme);
         }
         catch (DatabaseException e) {
             LOG.error("Error during scheme copy.", e);
             MessageDialog.openError(shellProvider.getShell(), "Copy Error",
                     "Could not copy the requested scheme.");
         }
         catch (IOException e) {
             LOG.error("Error during scheme copy.", e);
             MessageDialog.openError(shellProvider.getShell(), "Copy Error",
                     "Could not copy the requested scheme.");
         }
     }
 
     private void importScheme() {
         SchemeImportSelectionDialog dialog = new SchemeImportSelectionDialog(
                 shellProvider.getShell(), context);
         ContextInjectionFactory.inject(dialog, context);
 
         if (dialog.open() != Window.OK) {
             return;
         }
 
         try {
             Scheme scheme = schemeService.copy(dialog.getScheme(), plant);
             modifyScheme(scheme);
         }
         catch (DatabaseException e) {
             LOG.error("Error during scheme copy.", e);
             MessageDialog.openError(shellProvider.getShell(), "Import Error",
                     "Could not import the requested scheme.");
         }
         catch (IOException e) {
             LOG.error("Error during scheme import.", e);
             MessageDialog.openError(shellProvider.getShell(), "Copy Error",
                     "Could not copy the requested scheme.");
         }
     }
 
     // private int getRenderedComponentId(RenderedComponent component) {
     // return this.components.indexOf(component);
     // }
     //
     // private RenderedComponent getRenderedComponent(int id) {
     // return components.get(id);
     // }
 
     private void saveScheme() {
         Collection<SchemeComponent> schemeComps = Collections2.transform(grid.getItems(),
                 new Function<SchemeGridItem, SchemeComponent>() {
                     public SchemeComponent apply(SchemeGridItem item) {
                         return item.asSchemeComponent();
                     }
                 });
         Preconditions.checkNotNull(plant, "The Plant must be set before saving");
         try {
            if (currentScheme.getPlant().isPresent()) {
                 schemeService.update(currentScheme, schemeComps);
                 System.out.println("after update");
             }
             else {
                 schemeService.insert(plant, schemeComps);
                 System.out.println("after insert");
             }
             clearDirty();
         }
         catch (DatabaseException e) {
             LOG.error("Error during scheme saving.", e);
             MessageDialog.openError(shellProvider.getShell(), "Fehler beim Speichern",
                     "Das Schema konnte nicht gespeichert werden");
         }
     }
 
     public DragData getDraggingItem() {
         DragData i1 = gridDragListener.getDraggingItem();
         DragData i2 = treeDragListener.getDraggingItem();
         if (i1 == null && i2 == null) {
             throw new RuntimeException("No drag and drop is running");
         }
         if (i1 != null) {
             return i1;
         }
         else {
             return i2;
         }
     }
 
     public void setRenderedComponents(List<RenderedComponent> components) {
         gridDragListener.setComponents(components);
         gridListener.setComponents(components);
         treeDragListener.setComponents(components);
         tree.setInput(components);
     }
 
     public List<IContributionItem> getContributionItems() {
         return Collections.unmodifiableList(contributionItems);
 
     }
 
     public Optional<Scheme> getCurrentScheme() {
         return Optional.fromNullable(currentScheme);
     }
 
     public Section getSchemeSection() {
         return schemeSection;
     }
 
     @Override
     protected void checkSubclass() {
         // Disable the check that prevents subclassing of SWT components
     }
 }
