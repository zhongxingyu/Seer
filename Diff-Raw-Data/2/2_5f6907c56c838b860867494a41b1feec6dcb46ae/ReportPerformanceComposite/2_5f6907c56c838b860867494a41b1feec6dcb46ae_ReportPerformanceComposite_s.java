 package de.hswt.hrm.inspection.ui.part;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Catalog;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.ICatalogItem;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.catalog.model.tree.TreeTarget;
 import de.hswt.hrm.catalog.service.CatalogService;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 import de.hswt.hrm.common.ui.swt.utils.ContentProposalUtil;
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 import de.hswt.hrm.inspection.model.Inspection;
 import de.hswt.hrm.inspection.model.Performance;
 import de.hswt.hrm.inspection.model.util.PerformanceUtil;
 import de.hswt.hrm.inspection.service.InspectionService;
 import de.hswt.hrm.inspection.ui.performance.tree.PerformanceTreeContentProvider;
 import de.hswt.hrm.inspection.ui.performance.tree.PerformanceTreeLabelProvider;
 import de.hswt.hrm.misc.priority.model.Priority;
 import de.hswt.hrm.misc.priority.service.PriorityService;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.scheme.model.SchemeComponent;
 
 public class ReportPerformanceComposite extends AbstractComponentRatingComposite {
 
     private static final Logger LOG = LoggerFactory.getLogger(ReportPerformanceComposite.class);
     private static final I18n I18N = I18nFactory.getI18n(ReportPerformanceComposite.class);
 
     @Inject
     private InspectionService inspectionService;
 
     @Inject
     private IEclipseContext context;
 
     @Inject
     private CatalogService catalogService;
 
     @Inject
     private IShellProvider shellProvider;
     
     @Inject
     private PriorityService priorityService;
 
     private ListViewer targetListViewer;
     private ListViewer currentListViewer;
     private ListViewer activityListViewer;
     private TreeViewer treeViewer;
     
     private Button addButton;
 	private Button removeButton;
     
     private FormToolkit formToolkit = new FormToolkit(Display.getDefault());
 
     private Map<Performance, TreeTarget> performances;
     
     private Inspection inspection;
     
     private SchemeComponent currentSchemeComponent;
 	private ComboViewer priorityComboViewer;
     
     /**
      * Do not use this constructor when instantiate this composite! It is only included to make the
      * WindowsBuilder working.
      * 
      * @param parent
      * @param style
      */
     private ReportPerformanceComposite(Composite parent, int style) {
         super(parent, SWT.NONE);
         createControls();
     }
 
     /**
      * Create the composite.
      * 
      * @param parent
      */
     public ReportPerformanceComposite(Composite parent) {
         super(parent, SWT.NONE);
         formToolkit.dispose();
         formToolkit = FormUtil.createToolkit();
         performances = new LinkedHashMap<>();
     }
 
     @PostConstruct
     public void createControls() {
         setBackgroundMode(SWT.INHERIT_FORCE);
         GridLayout gl = new GridLayout(5, false);
         gl.marginWidth = 0;
         gl.marginHeight = 0;
         gl.marginLeft = 5;
         gl.marginBottom = 5;
         setLayout(gl);
 
         Section targetSection = formToolkit.createSection(this, Section.TITLE_BAR);
         targetSection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(targetSection);
         FormUtil.initSectionColors(targetSection);
         targetSection.setText(I18N.tr("Target"));
 
         targetListViewer = new ListViewer(targetSection, SWT.BORDER | SWT.V_SCROLL);
         List targetList = targetListViewer.getList();
         targetSection.setClient(targetList);
 
         Section currentSection = formToolkit.createSection(this, Section.TITLE_BAR);
         currentSection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(currentSection);
         FormUtil.initSectionColors(currentSection);
         currentSection.setText(I18N.tr("Current"));
 
         currentListViewer = new ListViewer(currentSection, SWT.BORDER | SWT.V_SCROLL);
         List currentList = currentListViewer.getList();
         currentSection.setClient(currentList);
 
         Section activitySection = formToolkit.createSection(this, Section.TITLE_BAR);
         activitySection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(activitySection);
         FormUtil.initSectionColors(activitySection);
         activitySection.setText(I18N.tr("Activity"));
 
         activityListViewer = new ListViewer(activitySection, SWT.BORDER | SWT.V_SCROLL);
         List activityList = activityListViewer.getList();
         activitySection.setClient(activityList);
 
         Composite buttonComposite = new Composite(this, SWT.NONE);
         formToolkit.adapt(buttonComposite);
         formToolkit.paintBordersFor(buttonComposite);
         buttonComposite.setLayout(new FillLayout(SWT.VERTICAL));
 
         addButton = new Button(buttonComposite, SWT.NONE);
         formToolkit.adapt(addButton, true, true);
         addButton.setText(">>");
 
         removeButton = new Button(buttonComposite, SWT.NONE);
         formToolkit.adapt(removeButton, true, true);
         removeButton.setText("<<");
 
         Section containedSection = formToolkit.createSection(this, Section.TITLE_BAR);
         containedSection.setLayoutData(LayoutUtil.createFillData());
         formToolkit.paintBordersFor(containedSection);
         FormUtil.initSectionColors(containedSection);
         containedSection.setText(I18N.tr("Assigned"));
 
         Composite composite = new Composite(containedSection, SWT.NONE);
         formToolkit.adapt(composite);
         formToolkit.paintBordersFor(composite);
         containedSection.setClient(composite);
         GridLayout gl_composite = new GridLayout(2, false);
         gl_composite.marginWidth = 0;
         gl_composite.marginHeight = 0;
         composite.setLayout(gl_composite);
 
         treeViewer = new TreeViewer(composite, SWT.BORDER);
         Tree tree = treeViewer.getTree();
         treeViewer.setLabelProvider(new PerformanceTreeLabelProvider());
         treeViewer.setContentProvider(new PerformanceTreeContentProvider());
         // TODO replace with data from DB is present
         treeViewer.setInput(new ArrayList<TreeTarget>());
         
         TreeColumn column = new TreeColumn(treeViewer.getTree(),SWT.NONE);
         column.setWidth(200);
 
         GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 8);
         gd_tree.heightHint = 241;
         tree.setLayoutData(gd_tree);
         formToolkit.paintBordersFor(tree);
 
         Label label = new Label(composite, SWT.NONE);
         label.setText(I18N.tr("Priority"));
 
         priorityComboViewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
         priorityComboViewer.setContentProvider(ArrayContentProvider.getInstance());
         priorityComboViewer.setLabelProvider(new LabelProvider() {
         	@Override
         	public String getText(Object element) {
         		if (element instanceof Priority) {
         			return ((Priority)element).getText();
         		}
         		return super.getText(element);
         	}
         });       
         priorityComboViewer.getCombo().setLayoutData(LayoutUtil.createHorzFillData());
         priorityComboViewer.getCombo().addSelectionListener(new SelectionAdapter() {
         	@Override
         	public void widgetSelected(SelectionEvent e) {
         		priorityChanged();
         	}
 		});
         priorityComboViewer.setInput(getPriorities());
         formToolkit.adapt(priorityComboViewer.getCombo());
         formToolkit.paintBordersFor(priorityComboViewer.getCombo());
 
         initalizeListViewer(targetListViewer);
         initalizeListViewer(activityListViewer);
         initalizeListViewer(currentListViewer);
         
         initalizeListViewerListener();
     }
 
     private void initalizeListViewer(ListViewer viewer) {
 
         viewer.setContentProvider(ArrayContentProvider.getInstance());
         viewer.setLabelProvider(new LabelProvider() {
             @Override
             public String getText(Object element) {
                 return ((ICatalogItem) element).getName();
             }
         });
 
     }
 
     @Override
     protected void checkSubclass() {
     }
 
     public void initalizeListViewerListener() {
         targetListViewer.getList().addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
             	targetSelected();
             }
         });
 
         currentListViewer.getList().addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
             	currentSelected();
             }
         });
         
         activityListViewer.addDoubleClickListener(new IDoubleClickListener() {
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				addActivity();
 			}
 		});
         
         addButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				addActivity();
 			}
 		});
 
         removeButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				removeActivity();
 			}
 		});
     }
     
     
 	private Priority[] getPriorities() {
 
 		Collection<Priority> prioritities;
 		try {
 			prioritities = priorityService.findAll();
 			Priority[] prios = new Priority[prioritities.size()];
 			prioritities.toArray(prios);
 			return prios;
 		} catch (DatabaseException e) {
 			LOG.debug("An error occured", e);
 		}
 		return new Priority[0];
 	}
 
     private void targetSelected() {
         IStructuredSelection selection = (IStructuredSelection) targetListViewer
                 .getSelection();
         if (selection == null) {
             return;
         }
         Target t = (Target) selection.getFirstElement();
 
         try {
             currentListViewer.setInput(catalogService.findCurrentByTarget(t));
         }
         catch (DatabaseException e) {
             LOG.debug("An error occured", e);
         }
     }
     
     private void currentSelected() {
         IStructuredSelection selection = (IStructuredSelection) currentListViewer
                 .getSelection();
         if (selection == null) {
             return;
         }
         Current c = (Current) selection.getFirstElement();
         try {
             activityListViewer.setInput(catalogService.findActivityByCurrent(c));
         }
         catch (DatabaseException e) {
             LOG.debug("An error occured", e);
         }
     }
     
     
     private void priorityChanged() {
     	IStructuredSelection selection = (IStructuredSelection)priorityComboViewer.getSelection();
     	if (selection.isEmpty()) {
     		return;
     	}
     	
     	TreeTarget tt = getSelectedTreeTarget();
     	if (tt == null) {
     		return;
     	}
     	
     	Performance performance = getPerformanceOfTreeTarget(tt);
     	if (performance != null) {
     		// FIXME add setter for priority
 //    		performance.setPriority((Priority)selection.getFirstElement());
     	}
     }
     
     private void addActivity() {
 		IStructuredSelection selection = (IStructuredSelection) targetListViewer
 				.getSelection();
 		if (selection == null) {
 			return;
 		}
 
 		Target target = (Target) selection.getFirstElement();
 		selection = (IStructuredSelection) currentListViewer
 				.getSelection();
 		if (selection == null) {
 			return;
 		}
 		Current current = (Current) selection.getFirstElement();
 
 		selection = (IStructuredSelection) activityListViewer
 				.getSelection();
 		if (selection == null) {
 			return;
 		}
 		Activity activity = (Activity) selection.getFirstElement();
 		
 		Performance performance = new Performance(currentSchemeComponent, 
				target, current, activity, null, inspection);
 		TreeTarget tt = PerformanceUtil.createTreeTriplet(performance);
         if (!performances.containsKey(performance)) {
             performances.put(performance, tt);
     		treeViewer.add(treeViewer.getInput(), tt);
             treeViewer.expandAll();
         }
     }
     
     private void removeActivity() {
     	TreeTarget tt = getSelectedTreeTarget();
     	if (tt == null) {
     		return;
     	}
     	performances.remove(getPerformanceOfTreeTarget(tt));
 		treeViewer.remove(tt);
 	}
 
     private TreeTarget getSelectedTreeTarget() {
     	ITreeSelection selection = ((ITreeSelection)treeViewer.getSelection());
     	if (selection.isEmpty()) {
     		return null;
     	}
     	return (TreeTarget)selection.getPaths()[0].getFirstSegment();
     }
     
     private Performance getPerformanceOfTreeTarget(TreeTarget treeTarget) {
     	for (Performance performance : performances.keySet()) {
     		if (performances.get(performance).equals(treeTarget)) {
     			return performance;
     		}
     	}
     	return null;
     }
     
     private void deselectPriorityCombo() {
 		int selection = priorityComboViewer.getCombo().getSelectionIndex();
 		if (selection >= 0) {
 			priorityComboViewer.getCombo().deselect(selection);
 		}
     }
     
     private void reset() {
 		performances.clear();
 		deselectPriorityCombo();
 		priorityComboViewer.setInput(getPriorities());
     }
     
 	@Override
 	public void inspectionChanged(Inspection inspection) {
 		if (inspection == null) {
 			return;
 		}
 		this.inspection = inspection;
 		reset();
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public void inspectionComponentSelectionChanged(SchemeComponent component) {
 		if (component == null){
        		return;
         }
 		
 		currentSchemeComponent = component;
 		
         Catalog c = component.getComponent().getCategory().get().getCatalog().get();
         try {
 
             targetListViewer.setInput(catalogService.findTargetByCatalog(c));
             currentListViewer.getList().removeAll();
             activityListViewer.getList().removeAll();
             ((java.util.List)treeViewer.getInput()).clear();
             treeViewer.refresh();
             for (Performance performance : performances.keySet()) {
             	if (performance.getSchemeComponent().equals(currentSchemeComponent)) {
             		treeViewer.add(treeViewer.getInput(), 
             				performances.get(performance));
             	}
             }
             treeViewer.expandAll();
             deselectPriorityCombo();
         }
         catch (DatabaseException e) {
         	e.printStackTrace();
             LOG.debug("An error occured", e);
         }
 	}
 
 	@Override
 	public void plantChanged(Plant plant) {
 		reset();
 	}
 
 	@Override
 	protected void saveValues() {
 		
 	}
 
 	@Override
 	public void dispose() {
 		formToolkit.dispose();
 		super.dispose();
 	}
 }
 
