 package org.dawb.workbench.plotting.tools;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.workbench.plotting.Activator;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public class EllipseFittingTool extends AbstractToolPage {
 	private final static Logger logger = LoggerFactory.getLogger(EllipseFittingTool.class);
 
 	private Composite composite;
 	private IRegionListener ellipseRegionListener;
 	private ITraceListener traceListener;
 	private IROIListener ellipseROIListener;
 
 	private TableViewer viewer;
 	private List<IRegion> ellipses;
 
 	public EllipseFittingTool() {
 		ellipses = new ArrayList<IRegion>(5);
 
 		ellipseRegionListener = new IRegionListener.Stub() {
 			@Override
 			public void regionsRemoved(RegionEvent evt) {
				updateEllipses();
 			}
 
 			@Override
 			public void regionRemoved(RegionEvent evt) {
 				removeEllipse(evt.getRegion());
 			}
 
 			@Override
 			public void regionAdded(RegionEvent evt) {
 				updateEllipse(evt.getRegion());
 			}
 		};
 
 		ellipseROIListener = new IROIListener.Stub() {
 			@Override
 			public void roiChanged(ROIEvent evt) {
				updateEllipses();
 			}
 		};
 
 		try {
 			setPlottingSystem(PlottingFactory.createPlottingSystem());
 			traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesPlotted(TraceEvent evt) {
 
 					if (!(evt.getSource() instanceof List<?>)) {
 						return;
 					}
 
 				}
 			};
 
 		} catch (Exception e) {
 			logger.error("Cannot get plotting system!", e);
 		}
 	}
 
 	protected void updateEllipse(IRegion region) {
 		if (region == null)
 			return;
 
 		if (ellipses.contains(region))
 			return;
 
 		if (region.getRegionType() != RegionType.ELLIPSEFIT)
 			return;
 
 		region.addROIListener(ellipseROIListener);
 		ellipses.add(region);
 		viewer.refresh();
 	}
 
 	protected void removeEllipse(IRegion region) {
 		if (region == null)
 			return;
 
 		if (region.getRegionType() != RegionType.ELLIPSEFIT)
 			return;
 
 		if (ellipses.contains(region))
 			ellipses.remove(region);
 
 		region.removeROIListener(ellipseROIListener);
 		viewer.refresh();
 	}
 
	protected void updateEllipses() {
 		IPlottingSystem plotter = getPlottingSystem();
 		if (plotter == null) return;
 
		for (IRegion r : ellipses) {
			r.removeROIListener(ellipseROIListener);
 		}
 		ellipses.clear();
 
 		Collection<IRegion> regions = plotter.getRegions(RegionType.ELLIPSEFIT);
 		if (regions != null && regions.size() > 0) {
 			IRegion r = null;
 			Iterator<IRegion> it = regions.iterator();
 			while (it.hasNext()) {
 				r = it.next();
 				ellipses.add(r);
				r.addROIListener(ellipseROIListener);
 			}
 		}
 		if (viewer != null) // can be null during activation
 			viewer.refresh();
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new FillLayout());
 
 		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 
 		createColumns(viewer);
 		Table t = viewer.getTable();
 		t.setLinesVisible(true);
 		t.setHeaderVisible(true);
 
 		createActions();
 
 		getSite().setSelectionProvider(viewer);
 
 		viewer.setContentProvider(new IStructuredContentProvider() {
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			}
 			
 			@Override
 			public void dispose() {
 			}
 			
 			@Override
 			public Object[] getElements(Object inputElement) {
 				return ellipses.toArray();
 			}
 		});
 
 		viewer.setInput(ellipses);
 		parent.layout();
 	}
 
 	private void createColumns(final TableViewer viewer) {
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 	
 		int i = 0;
 		TableViewerColumn col;
 		TableColumn c;
 	
 		col = new TableViewerColumn(viewer, SWT.CENTER, i);
 		col.setLabelProvider(new EllipseROILabelProvider(i++));
 		c = col.getColumn();
 		c.setText("ID");
 		c.setWidth(100);
 	
 		col = new TableViewerColumn(viewer, SWT.CENTER, i);
 		col.setLabelProvider(new EllipseROILabelProvider(i++));
 		c = col.getColumn();
 		c.setText("Major semi-axis");
 		c.setWidth(120);
 	
 		col = new TableViewerColumn(viewer, SWT.CENTER, i);
 		col.setLabelProvider(new EllipseROILabelProvider(i++));
 		c = col.getColumn();
 		c.setText("Minor semi-axis");
 		c.setWidth(120);
 	
 		col = new TableViewerColumn(viewer, SWT.CENTER, i);
 		col.setLabelProvider(new EllipseROILabelProvider(i++));
 		c = col.getColumn();
 		c.setText("Angle");
 		c.setWidth(75);
 	
 		col = new TableViewerColumn(viewer, SWT.CENTER, i);
 		col.setLabelProvider(new EllipseROILabelProvider(i++));
 		c = col.getColumn();
 		c.setText("Centre x");
 		c.setWidth(75);
 	
 		col = new TableViewerColumn(viewer, SWT.CENTER, i);
 		col.setLabelProvider(new EllipseROILabelProvider(i++));
 		c = col.getColumn();
 		c.setText("Centre y");
 		c.setWidth(75);
 	
 		col = new TableViewerColumn(viewer, SWT.CENTER, i);
 		col.setLabelProvider(new EllipseROILabelProvider(i++));
 		c = col.getColumn();
 		c.setText("Points");
 		c.setWidth(20);
 	}
 
 	private void createActions() {
 		final Action delete = new Action("Delete selected region", Activator.getImageDescriptor("icons/plot-tool-measure-delete.png")) {
 			@Override
 			public void run() {
 				if (!isActive()) return;
 				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
 
 				if (sel != null && sel.getFirstElement() != null) {
 					final IRegion region = (IRegion) sel.getFirstElement();
 					getPlottingSystem().removeRegion(region);
 					viewer.refresh();
 				}
 			}
 		};
 		delete.setToolTipText("Delete selected region, if there is one.");
 
 		getSite().getActionBars().getToolBarManager().add(delete);
 		getSite().getActionBars().getMenuManager().add(delete);
 	}
 
 	class EllipseROILabelProvider extends ColumnLabelProvider {
 		private final int column;
 
 		public EllipseROILabelProvider(int i) {
 			column = i;
 		}
 
 		@Override
 		public String getText(Object element) {
 
 			ROIBase rb = ((IRegion) element).getROI();
 
 			if (!(rb instanceof EllipticalROI)) {
 				return null;
 			}
 			EllipticalROI eroi = (EllipticalROI) rb;
 
 			switch (column) {
 			case 0:
 				return ((IRegion) element).getName();
 			case 1:
 				return String.format("%.2f", eroi.getSemiAxis(0));
 			case 2:
 				return String.format("%.2f", eroi.getSemiAxis(1));
 			case 3:
 				return String.format("%.2f", eroi.getAngleDegrees());
 			case 4:
 				return String.format("%.2f", eroi.getPointX());
 			case 5:
 				return String.format("%.2f", eroi.getPointY());
 			case 6:
 				if (eroi instanceof EllipticalFitROI) {
 					EllipticalFitROI froi = (EllipticalFitROI) eroi;
 					return Integer.toString(froi.getPoints().getSides());
 				}
 				return "--";
 			}
 			return null;
 		}
 	}
 
 	@Override
 	public void activate() {
 		super.activate();
 		IPlottingSystem plotter = getPlottingSystem();
 		if (plotter == null) return;
 
 		if (traceListener != null)
 			plotter.addTraceListener(traceListener);
 		if (ellipseRegionListener != null)
 			plotter.addRegionListener(ellipseRegionListener);
 
		updateEllipses();
 
 		// Start with a selection of the right type
 		try {
 			plotter.createRegion(RegionUtils.getUniqueName("Ellipse fit", plotter), RegionType.ELLIPSEFIT);
 		} catch (Exception e) {
 			logger.error("Cannot create region for ellipse fitting tool!");
 		}
 	}
 
 	@Override
 	public void deactivate() {
 		IPlottingSystem plotter = getPlottingSystem();
 		if (plotter != null) {
 			plotter.removeTraceListener(traceListener);
 			plotter.removeRegionListener(ellipseRegionListener);
 		}
 
 		super.deactivate();
 	}
 
 	@Override
 	public Control getControl() {
 		return composite;
 	}
 
 	@Override
 	public void setFocus() {
 	}
 }
