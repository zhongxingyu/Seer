 package de.hswt.hrm.inspection.ui.part;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.Separator;
 
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 
 public class InspectionPart {
 
 	@Inject
 	private IEclipseContext context;
 	
 	private FormToolkit formToolkit = new FormToolkit(Display.getDefault());
 
 	private Form form;
 
 	private ReportsOverviewComposite reportsOverviewComposite;
 	
 	private TabFolder tabFolder;
 
 	private TabItem overviewTab;
 	private TabItem generalTab;
 	private TabItem biolocicalRatingTab;
 	private TabItem physicalRatingTab;
 	private TabItem performanceTab;
 
 	private ActionContributionItem saveContribution;
 	private ActionContributionItem addContribution;
 	private ActionContributionItem copyContribution;
 	private ActionContributionItem editContribution;
 	private ActionContributionItem evaluateContribution;
 
 	private ReportGeneralComposite reportGeneralComposite;
 
 	private ComponentSelectionComposite performanceComposite;
 	private ComponentSelectionComposite physicalComposite;
 	private ComponentSelectionComposite biologicalComposite;
 
 	public InspectionPart() {
 		// toolkit can be created in PostConstruct, but then then
 		// WindowBuilder is unable to parse the code
 		formToolkit.dispose();
 		formToolkit = FormUtil.createToolkit();
 	}
 
 	/**
 	 * Create contents of the view part.
 	 */
 	@PostConstruct
 	public void createControls(Composite parent) {
 		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		composite.setLayout(new FillLayout());
 		
 		form = formToolkit.createForm(composite);
 		form.getHead().setOrientation(SWT.RIGHT_TO_LEFT);
 		form.getBody().setBackgroundMode(SWT.INHERIT_FORCE);
 		form.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		formToolkit.paintBordersFor(form);
 		form.setText("Inspection");
 		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
 		fillLayout.marginHeight = 5;
 		fillLayout.marginWidth = 5;
 		form.getBody().setLayout(fillLayout);
 		formToolkit.decorateFormHeading(form);
 		
 		tabFolder = new TabFolder(form.getBody(), SWT.NONE);
 		tabFolder.setBackgroundMode(SWT.INHERIT_FORCE);
 		formToolkit.adapt(tabFolder);
 		formToolkit.paintBordersFor(tabFolder);

 		
 		overviewTab = new TabItem(tabFolder, SWT.NONE);
 		overviewTab.setText("Overview");
 		
 		// reports overview composite
 		reportsOverviewComposite = new ReportsOverviewComposite(tabFolder);
 		ContextInjectionFactory.inject(reportsOverviewComposite, context);
 		overviewTab.setControl(reportsOverviewComposite);
 		
 		generalTab = new TabItem(tabFolder, SWT.NONE);
 		generalTab.setText("General");
 		
 		reportGeneralComposite = new ReportGeneralComposite(tabFolder);
 		ContextInjectionFactory.inject(reportGeneralComposite, context);
 		generalTab.setControl(reportGeneralComposite);
 		
 		biolocicalRatingTab = new TabItem(tabFolder, SWT.NONE);
 		biolocicalRatingTab.setText("Biological rating");
 
 		biologicalComposite = new ComponentSelectionComposite(tabFolder, ReportBiologicalComposite.class);
 		ContextInjectionFactory.inject(biologicalComposite, context);
 		biolocicalRatingTab.setControl(biologicalComposite);
 
 		physicalRatingTab = new TabItem(tabFolder, SWT.NONE);
 		physicalRatingTab.setText("Physical rating");
 
 		physicalComposite = new ComponentSelectionComposite(tabFolder, ReportPhysicalComposite.class);
 		ContextInjectionFactory.inject(physicalComposite, context);
 		physicalRatingTab.setControl(physicalComposite);
 
 		performanceTab = new TabItem(tabFolder, SWT.NONE);
 		performanceTab.setText("Performance");
 
 		performanceComposite = new ComponentSelectionComposite(tabFolder, ReportPerformanceComposite.class);
 		ContextInjectionFactory.inject(performanceComposite, context);
 		performanceTab.setControl(performanceComposite);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getItem(tabFolder.getSelectionIndex()).equals(overviewTab)) {
					showOverviewActions(true);
				} else {
					showOverviewActions(false);
				}
			}
		});
 		
 		createActions();
 	}
 
 	protected void showOverviewActions(boolean visible) {
 		addContribution.setVisible(visible);
 		copyContribution.setVisible(visible);
 		editContribution.setVisible(visible);
 		evaluateContribution.setVisible(visible);
 		form.getToolBarManager().update(true);
 	}
 
 	private void createActions() {
 		// TODO translate
 		Action evaluateAction = new Action("Report") {
 			@Override
 			public void run() {
 				super.run();
 				// TODO create latex report
 			}
 		};
 		evaluateAction.setDescription("Edit an exisitng report.");
 		evaluateContribution = new ActionContributionItem(evaluateAction);
 		form.getToolBarManager().add(evaluateContribution);
 
 		form.getToolBarManager().add(new Separator());
 		
 		Action saveAction = new Action("Save") {
 			@Override
 			public void run() {
 				super.run();
 				reportsOverviewComposite.addInspection();
 			}
 		};
 		saveAction.setDescription("Save the current edited report.");
 		saveAction.setEnabled(false);
 		saveContribution = new ActionContributionItem(saveAction);
 		form.getToolBarManager().add(saveContribution);
 
 		form.getToolBarManager().add(new Separator());
 		
 		Action editAction = new Action("Edit") {
 			@Override
 			public void run() {
 				super.run();
 				tabFolder.setSelection(generalTab);
 			}
 		};
 		editAction.setDescription("Edit an exisitng report.");
 		editContribution = new ActionContributionItem(editAction);
 		form.getToolBarManager().add(editContribution);
 
 		Action copyAction = new Action("Copy") {
 			@Override
 			public void run() {
 				super.run();
 				// TODO copy a report
 			}
 		};
 		copyAction.setDescription("Add's a new report.");
 		copyAction.setEnabled(false);
 		copyContribution = new ActionContributionItem(copyAction);
 		form.getToolBarManager().add(copyContribution);
 
 		Action addAction = new Action("Add") {
 				@Override
 				public void run() {
 					super.run();
 					reportsOverviewComposite.addInspection();
 				}
 			};
 		addAction.setDescription("Add's a new report.");
 		addContribution = new ActionContributionItem(addAction);
 		form.getToolBarManager().add(addContribution);
 
 		form.getToolBarManager().update(true);
 	}
 	
 	@PreDestroy
 	public void dispose() {
 		formToolkit.dispose();
 	}
 
 	@Focus
 	public void setFocus() {
 		// TODO	Set the focus to control
 	}
 	
 	private void updateTableFilter(String filterString) {
 		// TODO adapt to this class
 //		ContactFilter filter = (ContactFilter) tableViewer.getFilters()[0];
 //		filter.setSearchString(filterString);
 //		tableViewer.refresh();
 	}
 }
