 /*******************************************************************************
  * Copyright (c) 16 dec. 2012 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.screens.f3.charts;
 
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.core.databinding.observable.list.ComputedList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.IJobChangeListener;
 import org.eclipse.emf.databinding.EMFDataBindingContext;
 import org.eclipse.emf.databinding.EMFObservables;
 import org.eclipse.emf.databinding.IEMFListProperty;
 import org.eclipse.emf.databinding.edit.EMFEditProperties;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.widgets.ColumnLayout;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.part.ShowInContext;
 import org.eclipse.ui.progress.UIJob;
 import org.swtchart.Chart;
 
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ChartModel;
 import com.netxforge.netxstudio.common.model.IChartModel;
 import com.netxforge.netxstudio.common.model.IMonitoringSummary;
 import com.netxforge.netxstudio.common.model.MonitoringStateModel;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.metrics.MetricValueRange;
 import com.netxforge.netxstudio.operators.OperatorsFactory;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.operators.ResourceMonitor;
 import com.netxforge.netxstudio.operators.ToleranceMarker;
 import com.netxforge.netxstudio.screens.AbstractScreen;
 import com.netxforge.netxstudio.screens.editing.IDataScreenInjection;
 import com.netxforge.netxstudio.screens.editing.actions.WizardUtil;
 import com.netxforge.netxstudio.screens.editing.tables.TableHelper;
 import com.netxforge.netxstudio.screens.editing.tables.TableHelper.TBVCFeatureSorter;
 import com.netxforge.netxstudio.screens.f1.support.ValueRangeSelectionWizard;
 import com.netxforge.netxstudio.screens.internal.ScreensActivator;
 import com.netxforge.netxstudio.screens.showins.ChartInput;
 
 /**
  * 
  * @author Christophe Bouhier
  */
 public class SmartChartScreen extends AbstractScreen implements
 		IDataScreenInjection, IJobChangeListener {
 
 	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
 
 	private Table table;
 
 	private Form frmChartScreen;
 
 	private TableViewer markersTableViewer;
 
 	/** Holds model data together for the chart */
 	private IChartModel chartModel;
 
 	/**
 	 * A specialized chart for {@link NetXResource} and {@link ResourceMonitor}
 	 * objects
 	 */
 	private SmartResourceChart smartResourceChart;
 
 	@Inject
 	private TableHelper tableHelper;
 
 	/**
 	 * Track the last selection.
 	 */
 	private EObject latestSelection;
 
 	@Inject
 	private MonitoringStateModel monitoringState;
 
 	private RefreshSummaryJob refreshSummaryJob = new RefreshSummaryJob();
 
 	/**
 	 * Refreshes the RFS Service Summary Section.
 	 * 
 	 * @author Christophe Bouhier
 	 */
 	class RefreshSummaryJob extends UIJob {
 
 		private IMonitoringSummary summary;
 
 		public void setSummary(IMonitoringSummary summary) {
 			this.summary = summary;
 		}
 
 		/**
 		 * Creates a new instance of the class.
 		 */
 		public RefreshSummaryJob() {
 			super("refresh");
 			// setSystem(true);
 		}
 
 		@Override
 		public IStatus runInUIThread(IProgressMonitor monitor) {
 
 			monitor.setTaskName("Refresh monitoring");
 
 			if (monitor.isCanceled()) {
 				return new Status(IStatus.OK, ScreensActivator.PLUGIN_ID,
 						IStatus.OK, "Cancelled ", null);
 			}
 
 			if (summary == null) {
 				return new Status(IStatus.WARNING, ScreensActivator.PLUGIN_ID,
 						IStatus.ERROR, "No summary for this object", null);
 			}
 
 			refreshSummaryUI(summary);
 
 			return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, "",
 					null);
 		}
 
 		private void refreshSummaryUI(IMonitoringSummary summary) {
 			if (getLatestSelection() instanceof NetXResource) {
 				NetXResource netxRes = (NetXResource) getLatestSelection();
 				if (netxRes.getMetricValueRanges().size() > 0) {
 					MetricValueRange mvr = netxRes.getMetricValueRanges()
 							.get(0);
 					IChartModel valueFor = ChartModel.valueFor(modelUtils,
 							summary, mvr.getIntervalHint(), mvr.getKindHint());
 					chartModel = valueFor;
 					if (chartModel.isChartModelOk()) {
 						initDataBindings_();
 					}else{
 						// It's not OK. Do not load it. 
 					}
 				}
 			}
 		}
 	}
 
 	public SmartChartScreen(Composite parent, int style) {
 		super(parent, style);
 		addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				toolkit.dispose();
 				disposeData();
 			}
 		});
 		toolkit.adapt(this);
 		toolkit.paintBordersFor(this);
 	}
 
 	public void buildUI() {
 		setLayout(new FillLayout(SWT.HORIZONTAL));
 
 		frmChartScreen = toolkit.createForm(this);
 		frmChartScreen.setSeparatorVisible(true);
 		toolkit.paintBordersFor(frmChartScreen);
 
 		frmChartScreen.getBody().setLayout(new ColumnLayout());
 
 		// Composite for the chart.
 		Composite cmChart = toolkit.createComposite(frmChartScreen.getBody(),
 				SWT.NONE);
 		toolkit.paintBordersFor(cmChart);
 		// sctnPeriod.setClient(composite_2);
 		cmChart.setLayout(new GridLayout(1, false));
 		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 4);
 		gridData.heightHint = 350;
 		// gridData.widthHint = 400;
 		smartResourceChart = new SmartResourceChart(cmChart, SWT.NONE, gridData);
 		smartResourceChart.setModelUtils(modelUtils);
 
 		// buildScrollStick(cmChart);
 		// buildZoom(cmChart);
 
 		buildMarkersUI();
 
 	}
 
 	private void buildMarkersUI() {
 		Section sctnMarkers = toolkit.createSection(frmChartScreen.getBody(),
 				Section.TWISTIE | Section.TITLE_BAR);
 
 		toolkit.paintBordersFor(sctnMarkers);
 		sctnMarkers.setText("Markers");
 		sctnMarkers.setExpanded(false);
 
 		Composite composite = toolkit.createComposite(sctnMarkers, SWT.NONE);
 		toolkit.paintBordersFor(composite);
 		sctnMarkers.setClient(composite);
 		composite.setLayout(new GridLayout(1, false));
 
 		markersTableViewer = new TableViewer(composite, SWT.BORDER
 				| SWT.FULL_SELECTION);
 		markersTableViewer
 				.addSelectionChangedListener(new ISelectionChangedListener() {
 
 					public void selectionChanged(SelectionChangedEvent event) {
 						ISelection selection = event.getSelection();
 						if (selection instanceof IStructuredSelection) {
 							Object firstElement = ((IStructuredSelection) selection)
 									.getFirstElement();
 							if (firstElement instanceof ToleranceMarker) {
 								smartResourceChart
 										.showHover((ToleranceMarker) firstElement);
 							}
 						}
 
 					}
 				});
 		table = markersTableViewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		GridData gdMarkers = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
 		gdMarkers.heightHint = 250;
 		table.setLayoutData(gdMarkers);
 		toolkit.paintBordersFor(table);
 
 		{ // Column 1.
 			tableHelper.new TBVC<String>().tbvcFor(markersTableViewer, "Type",
 					"Marker Type", 125, null,
 					new TableHelper.ComparableComparator<String>());
 		}
 
 		{ // Column 2.
 			tableHelper.new TBVC<String>().tbvcFor(markersTableViewer,
 					"Description", "Additional description for the Marker",
 					200, null, new TableHelper.ComparableComparator<String>());
 		}
 
 		{ // Column 3.
 			TableViewerColumn tbvcFor = tableHelper.new TBVC<Date>().tbvcFor(
 					markersTableViewer, "TimeStamp",
 					"The date and time the Marker occurred", 140,
 					(EditingSupport) null);
 
 			TBVCFeatureSorter<Value> tbvcFeatureSorter = tableHelper.new TBVCFeatureSorter<Value>(
 					tbvcFor, OperatorsPackage.Literals.MARKER__VALUE_REF,
 					modelUtils.valueTimeStampCompare());
 			tbvcFeatureSorter.setSorter(TBVCFeatureSorter.DESC);
 		}
 
 		{ // Column 4.
 			tableHelper.new TBVC<String>().tbvcFor(markersTableViewer, "Value",
 					"The marked Value.", 100, null,
 					new TableHelper.ComparableComparator<String>());
 		}
 
 		{ // Column 5.
 			tableHelper.new TBVC<String>().tbvcFor(markersTableViewer,
 					"Direction", "The direction in which the Value moves.",
 					100, null, new TableHelper.ComparableComparator<String>());
 		}
 
 		{ // Column 6.
 			tableHelper.new TBVC<String>().tbvcFor(markersTableViewer, "Level",
 					"The Marker level.", 100, null,
 					new TableHelper.ComparableComparator<String>());
 		}
 	}
 
 	/**
 	 * @param cmChart
 	 */
 	@SuppressWarnings("unused")
 	private void buildZoom(Composite cmChart) {
 
 		// ZOOM etc... buttons.
 
 		Button btnZoomIn = toolkit.createButton(cmChart, "Z+", SWT.NONE);
 		btnZoomIn.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				getChart().getAxisSet().zoomIn();
 				getChart().redraw();
 			}
 		});
 		GridData gd_btnZoomIn = new GridData(SWT.CENTER, SWT.CENTER, false,
 				false, 1, 1);
 		gd_btnZoomIn.widthHint = 24;
 		gd_btnZoomIn.heightHint = 18;
 		btnZoomIn.setLayoutData(gd_btnZoomIn);
 
 		Button btnZoomOut = toolkit.createButton(cmChart, "Z-", SWT.NONE);
 		btnZoomOut.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				getChart().getAxisSet().zoomOut();
 				getChart().redraw();
 			}
 		});
 
 		GridData gd_btnZoomOut = new GridData(SWT.CENTER, SWT.CENTER, false,
 				false, 1, 1);
 		gd_btnZoomOut.widthHint = 24;
 		gd_btnZoomOut.heightHint = 18;
 		btnZoomOut.setLayoutData(gd_btnZoomOut);
 	}
 
 	/**
 	 * A Scroll stick which scrolls the Y-Axis up or down, and the X-Axis left
 	 * and right.
 	 * 
 	 * @param parent
 	 * @param gd
 	 */
 	@SuppressWarnings("unused")
 	private void buildScrollStick(Composite parent) {
 
 		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1,
 				1);
 
 		Composite compositeScrollStick = toolkit.createComposite(parent,
 				SWT.NONE);
 		compositeScrollStick.setLayoutData(gridData);
 		toolkit.paintBordersFor(compositeScrollStick);
 		GridLayout gl_compositeScrollStick = new GridLayout(3, false);
 		gl_compositeScrollStick.marginTop = 3;
 		gl_compositeScrollStick.verticalSpacing = 0;
 		gl_compositeScrollStick.marginWidth = 0;
 		gl_compositeScrollStick.marginHeight = 0;
 		gl_compositeScrollStick.horizontalSpacing = 0;
 		compositeScrollStick.setLayout(gl_compositeScrollStick);
 
 		new Label(compositeScrollStick, SWT.NONE);
 		Button btnUpScroll = toolkit.createButton(compositeScrollStick, "",
 				SWT.ARROW | SWT.UP);
 		btnUpScroll.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				getChart().getAxisSet().getYAxis(0).scrollUp();
 				getChart().redraw();
 			}
 		});
 		new Label(compositeScrollStick, SWT.NONE);
 
 		Button btnLeftScroll = toolkit.createButton(compositeScrollStick, "",
 				SWT.ARROW | SWT.LEFT);
 		btnLeftScroll.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				getChart().getAxisSet().getXAxis(0).scrollDown();
 				getChart().redraw();
 			}
 		});
 		new Label(compositeScrollStick, SWT.NONE);
 		Button btnRightScroll = toolkit.createButton(compositeScrollStick, "",
 				SWT.ARROW | SWT.RIGHT);
 		btnRightScroll.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				getChart().getAxisSet().getXAxis(0).scrollUp();
 				getChart().redraw();
 			}
 		});
 
 		new Label(compositeScrollStick, SWT.NONE);
 		Button btnDownScroll = toolkit.createButton(compositeScrollStick, "",
 				SWT.ARROW | SWT.DOWN);
 		btnDownScroll.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				getChart().getAxisSet().getYAxis(0).scrollDown();
 				getChart().redraw();
 			}
 		});
 		new Label(compositeScrollStick, SWT.NONE);
 	}
 
 	public Chart getChart() {
 		return smartResourceChart;
 	}
 
 	public Viewer getViewer() {
 		return null;
 	}
 
 	@Override
 	public void injectData(Object... selection) {
 		processSelection(selection);
 	}
 
 	private void processSelection(Object... selection) {
 
 		if (validSelection(selection) && selection.length == 1) {
 			Object o = selection[0];
 
 			// Prep the summary itself, as the monitoring job might still be
 			// running, we delay until it's ready.
 			if (o instanceof EObject) {
 				setLatestSelection((EObject) o);
 			}
 
 			// We can't add a notifier until the
 			deActivate();
 			activate();
 
 		} else {
 			// TODO, for multiple selections, we need a
 			// IMonitoringSummaryComposite object.
 
 		}
 	}
 
 	private synchronized void setLatestSelection(EObject o) {
 		latestSelection = o;
 	}
 
 	private synchronized EObject getLatestSelection() {
 		return latestSelection;
 	}
 
 	/**
 	 * remove the listener to the monitoring state jobs.
 	 */
 	public void deActivate() {
 		monitoringState.removeJobNotifier(this);
 	}
 
 	/**
 	 * Listen to the monitoring state jobs.
 	 */
 	public void activate() {
 		monitoringState.addJobNotifier(this);
 	}
 
 	/**
 	 * The selection should be similar objects.
 	 * 
 	 * @param selection
 	 * @return
 	 */
 	private boolean validSelection(Object[] selection) {
 
 		for (Object o : selection) {
 			if (o instanceof EObject) {
 				continue;
 			} else {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void addData() {
 		// N/A this is a view screen.
 	}
 
 	public boolean isValid() {
 		return true;
 	}
 
 	public EMFDataBindingContext initDataBindings_() {
 		EMFDataBindingContext context = new EMFDataBindingContext();
 
 		if (chartModel.hasNetXResource()) {
 			frmChartScreen
 					.setText("Resource: "
 							+ chartModel.getNetXResource().getLongName()
 							+ (chartModel.getNetXResource().getComponentRef() != null ? " ( "
 									+ modelUtils.componentName(chartModel
 											.getNetXResource()
 											.getComponentRef()) + ")" : ""));
 		}
 		smartResourceChart.initChartBinding(chartModel);
 
 		if (chartModel.hasMonitor()) {
 			initMarkersBinding();
 		}
 
 		if (chartModel.hasMarkers()) {
 			initMarkersBindingCollection();
 		}
 
 		return context;
 	}
 
 	/**
 	 * @deprecated
 	 */
 	private void initMarkersBinding() {
 
 		ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
 		markersTableViewer.setContentProvider(listContentProvider);
 
 		IObservableMap[] observeMaps = EMFObservables.observeMaps(
 				listContentProvider.getKnownElements(),
 				new EStructuralFeature[] {
 						OperatorsPackage.Literals.MARKER__KIND,
 						OperatorsPackage.Literals.MARKER__DESCRIPTION,
 						OperatorsPackage.Literals.MARKER__VALUE_REF,
 						OperatorsPackage.Literals.TOLERANCE_MARKER__DIRECTION,
 						OperatorsPackage.Literals.TOLERANCE_MARKER__LEVEL });
 
 		markersTableViewer
 				.setLabelProvider(new MarkersObervableMapLabelProvider(
 						observeMaps));
 		IEMFListProperty resourceMonitorObservableList = EMFEditProperties
 				.list(editingService.getEditingDomain(),
 						OperatorsPackage.Literals.RESOURCE_MONITOR__MARKERS);
 
 		// EMFEditProperties.
 		markersTableViewer.setInput(resourceMonitorObservableList
 				.observe(chartModel.getResMonitor()));
 	}
 
 	private void initMarkersBindingCollection() {
 
 		ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
 		markersTableViewer.setContentProvider(listContentProvider);
 
 		IObservableMap[] observeMaps = EMFObservables.observeMaps(
 				listContentProvider.getKnownElements(),
 				new EStructuralFeature[] {
 						OperatorsPackage.Literals.MARKER__KIND,
 						OperatorsPackage.Literals.MARKER__DESCRIPTION,
 						OperatorsPackage.Literals.MARKER__VALUE_REF,
 						OperatorsPackage.Literals.TOLERANCE_MARKER__DIRECTION,
 						OperatorsPackage.Literals.TOLERANCE_MARKER__LEVEL });
 
 		markersTableViewer
 				.setLabelProvider(new MarkersObervableMapLabelProvider(
 						observeMaps));
 
 		ComputedList markersList = new ComputedList() {
 
 			@SuppressWarnings("rawtypes")
 			@Override
 			protected List calculate() {
 				return chartModel.getMarkers();
 			}
 
 		};
 
 		markersTableViewer.setInput(markersList);
 	}
 
 	public class MarkersObervableMapLabelProvider extends
 			ObservableMapLabelProvider {
 
 		public MarkersObervableMapLabelProvider(IObservableMap[] attributeMaps) {
 			super(attributeMaps);
 		}
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			return super.getColumnImage(element, columnIndex);
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof ToleranceMarker) {
 				ToleranceMarker rm = (ToleranceMarker) element;
 				switch (columnIndex) {
 				case 0:
 					return rm.getKind().getName();
 				case 1:
 					return rm.getDescription();
 				case 2:
 
 					return modelUtils.date(modelUtils.fromXMLDate(rm
 							.getValueRef().getTimeStamp()))
 							+ "@"
 							+ modelUtils.time(modelUtils.fromXMLDate(rm
 									.getValueRef().getTimeStamp()));
 				case 3:
 					return new Double(rm.getValueRef().getValue()).toString();
 				case 4:
 					return rm.getDirection().getLiteral();
 				case 5:
 					return rm.getLevel().getLiteral();
 				}
 			}
 			return super.getColumnText(element, columnIndex);
 		}
 	}
 
 	public Form getScreenForm() {
 		return this.frmChartScreen;
 	}
 
 	public void disposeData() {
 	}
 
 	public String getScreenName() {
 		return "Resource graph";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.screens.AbstractScreenImpl#handleShowIn(org.
 	 * eclipse.ui.part.ShowInContext)
 	 */
 	@Override
 	public boolean handleShowIn(ShowInContext context) {
 
 		if (context.getInput() instanceof ChartInput) {
 
 			ChartInput chartInput = (ChartInput) context.getInput();
 
 			// Do we care about the selection??
 
 			ISelection selection = context.getSelection();
 			NetXResource netXResource = null;
 			if (selection instanceof IStructuredSelection) {
 				if (((IStructuredSelection) selection).getFirstElement() instanceof NetXResource) {
 					netXResource = (NetXResource) ((IStructuredSelection) selection)
 							.getFirstElement();
 				}
 			}
 
 			// fire a wizard to select the range when the interval is unknwown,
 			// block until we select a range.
 			IWizard wiz = WizardUtil.openWizard(
 					"com.netxforge.netxstudio.screens.valueranges",
 					(IStructuredSelection) selection, true);
 
 			if (wiz instanceof ValueRangeSelectionWizard) {
 				MetricValueRange valueRange = ((ValueRangeSelectionWizard) wiz)
 						.getValueRange();
 				chartInput.setInterval(valueRange.getIntervalHint());
 				chartInput.setKind(valueRange.getKindHint());
 			}
 
 			if (chartInput.getResourceMonitor() != null) {
 				chartModel = ChartModel.valueFor(modelUtils,
 						chartInput.getPeriod(), chartInput.getInterval(),
 						chartInput.getKind(), netXResource,
 						chartInput.getResourceMonitor(), null);
 			} else {
 				chartModel = ChartModel.valueFor(modelUtils,
 						chartInput.getPeriod(), chartInput.getInterval(),
 						chartInput.getKind(), netXResource,
 						OperatorsFactory.eINSTANCE.createResourceMonitor(),
 						null);
 
 			}
 
 			this.initDataBindings_();
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * {@link IJobChangeListener}
 	 */
 	public void aboutToRun(IJobChangeEvent event) {
 	}
 
 	public void awake(IJobChangeEvent event) {
 	}
 
 	public void done(IJobChangeEvent event) {
 		updateLatestSelection();
 	}
 
 	public void running(IJobChangeEvent event) {
 	}
 
 	public void scheduled(IJobChangeEvent event) {
 	}
 
 	public void sleeping(IJobChangeEvent event) {
 	}
 
 	private void updateLatestSelection() {
 		if (MonitoringStateModel.isAdapted(this.getLatestSelection())) {
 			System.out.println("Chart: Good :-) selection already adapted: "
 					+ (this.getLatestSelection()).toString());
 
 			IMonitoringSummary adapted = MonitoringStateModel.getAdapted(this
 					.getLatestSelection());
 
 			refreshSummaryJob.setSummary(adapted);
 			refreshSummaryJob.schedule(100);
 		} else {
 		}
 	}
 
 }
