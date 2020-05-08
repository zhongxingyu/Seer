 package org.dawnsci.plotting.tools.region;
 
 import java.util.Arrays;
 
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.RegionUtils;
 import org.dawnsci.plotting.api.tool.IToolPage;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.tools.region.MeasurementLabelProvider.LabelType;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 
 public class MeasurementTool extends AbstractRegionTableTool {
 
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		if (getToolId()!=null) {
 			if (getToolId().endsWith("1d")) return ToolPageRole.ROLE_1D;
 			if (getToolId().endsWith("2d")) return ToolPageRole.ROLE_2D;
 		}
 		return ToolPageRole.ROLE_1D;
 	}
 	
 	@Override
 	public boolean isStaticTool() {
 		return getToolPageRole()==ToolPageRole.ROLE_2D;
 	}
 
 
 	protected void createColumns(final TableViewer viewer) {
 		
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 
 		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
 		var.getColumn().setText("Name");
 		var.getColumn().setWidth(120);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROINAME));
 
 		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
 		var.getColumn().setText("Region Type");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROITYPE));
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
 		var.getColumn().setText("dx");
 		var.getColumn().setWidth(80);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.DX));
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 3);
 		var.getColumn().setText("dy");
 		var.getColumn().setWidth(80);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.DY));
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 4);
 		var.getColumn().setText("length");
 		var.getColumn().setWidth(80);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.LENGTH));
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 5);
 		var.getColumn().setText("Coordinates");
 		var.getColumn().setWidth(500);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROISTRING));
 	}
 		
 	protected void createActions() {
 
 		if (getToolPageRole()==ToolPageRole.ROLE_2D) {
			final Action calibrate = new Action("Calibrate axes using a measurement and apply these axes to other plots.\nThese axes can then be applied to other plots by keeping the\nmeasurement tool open using 'open in a dedicated view'.", IAction.AS_PUSH_BUTTON) {
 				public void run() {
 					MeasurementCalibrationDialog dialog = new MeasurementCalibrationDialog(MeasurementTool.this);
 					dialog.open();
 				}
 			};
 			calibrate.setImageDescriptor(Activator.getImageDescriptor("icons/measurement_calibrate.png"));
 			
 			final Action applyCalibrated = new Action("Apply calibrated axes to any images opened while this tool is active.", IAction.AS_CHECK_BOX) {
 				public void run() {
 					updateCalibrateTraceListener(isChecked());
 				}
 			};
 			applyCalibrated.setChecked(true);
 			applyCalibrated.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
 			updateCalibrateTraceListener(true);
 			
 			
 			getSite().getActionBars().getToolBarManager().add(calibrate);
 			getSite().getActionBars().getToolBarManager().add(applyCalibrated);
 			getSite().getActionBars().getToolBarManager().add(new Separator());
 		}
 		super.createActions();
 	}
 	
 	private ITraceListener axesTraceListener;
 	private double xCalibratedAxisFactor=Double.NaN;
 	private double yCalibratedAxisFactor=Double.NaN;
 	private String unitName;
 	/**
 	 * Add or remove a trace listener which applies the calibrated images
 	 * @param b
 	 */
 	private void updateCalibrateTraceListener(boolean addListener) {
 		if (addListener) {
 			axesTraceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesUpdated(TraceEvent evt) {
 					updateAxes(evt);
 				}
 				public void traceAdded(TraceEvent evt) {
 					updateAxes(evt);
 				}
 				protected void updateAxes(TraceEvent evt) {
 					applyCalibration();
 				}			
 			};
 			getPlottingSystem().addTraceListener(axesTraceListener);
 			applyCalibration();
 
 		} else {
 			getPlottingSystem().removeTraceListener(axesTraceListener);
 			axesTraceListener = null;
 			final IImageTrace image = getImageTrace();
 			if (image!=null) image.setAxes(null, true);
 		}
 		getPlottingSystem().repaint();
 	}
 	
 	protected void applyCalibration() {
 		final IImageTrace trace = getImageTrace();
 		if (trace!=null && !Double.isNaN(xCalibratedAxisFactor) && !Double.isNaN(yCalibratedAxisFactor)
 				        && xCalibratedAxisFactor>0              && yCalibratedAxisFactor>0) {
 			final IDataset data = trace.getData();
 			trace.setAxes(Arrays.asList(getCalibratedAxis(xCalibratedAxisFactor, data.getShape()[1]), 
 					                    getCalibratedAxis(yCalibratedAxisFactor, data.getShape()[0])), 
 					                    true);
 		}		
 	}
 
 	protected IDataset getCalibratedAxis(double factor, int size) {
 		AbstractDataset axis = AbstractDataset.arange(size, AbstractDataset.FLOAT64);
 		axis.imultiply(factor);
 		axis.setName(unitName);
 		return axis;
 	}
 
 	public IToolPage cloneTool() throws Exception {
 
 		IToolPage tp = super.cloneTool();
 		((MeasurementTool)tp).xCalibratedAxisFactor = xCalibratedAxisFactor;
 		((MeasurementTool)tp).yCalibratedAxisFactor = yCalibratedAxisFactor;
 		
 		return tp;
 	}
 	
 	@Override
 	public void activate() {
 		super.activate();
 		if (getPlottingSystem()!=null && axesTraceListener!=null) try {
 			getPlottingSystem().addTraceListener(axesTraceListener);
 		} catch (Exception e) {
 			logger.error("Cannot add trace listener!", e);
 		}		
 	}
 	
 	@Override
 	public void deactivate() {
 		super.deactivate();
 		if (getPlottingSystem()!=null) try {
 			getPlottingSystem().removeTraceListener(axesTraceListener);
 		} catch (Exception e) {
 			logger.error("Cannot remove trace listener!", e);
 		}		
 	}
 
 	protected void createNewRegion() {
 		try {
 			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Measurement", getPlottingSystem()), IRegion.RegionType.LINE);
 		} catch (Exception e) {
 			logger.error("Cannot create line region for selecting in measurement tool!", e);
 		}
 	}
 	/**
 	 * must be two axes in array.
 	 * @param axes
 	 */
 	public void setCalibratedAxes(String unitName, double... axes) {
 		this.unitName = unitName;
 		if (axes==null || axes.length!=2) {
 			xCalibratedAxisFactor=Double.NaN;
 			yCalibratedAxisFactor=Double.NaN;
 			return;
 		}
 		xCalibratedAxisFactor = axes[0];
 		yCalibratedAxisFactor = axes[1];
 	}
 
 	public double getxCalibratedAxisFactor() {
 		return xCalibratedAxisFactor;
 	}
 
 	public void setxCalibratedAxisFactor(double xCalibratedAxisFactor) {
 		this.xCalibratedAxisFactor = xCalibratedAxisFactor;
 	}
 
 	public double getyCalibratedAxisFactor() {
 		return yCalibratedAxisFactor;
 	}
 
 	public void setyCalibratedAxisFactor(double yCalibratedAxisFactor) {
 		this.yCalibratedAxisFactor = yCalibratedAxisFactor;
 	}
 
 	public String getUnitName() {
 		return unitName;
 	}
 
 	public void setUnitName(String unitName) {
 		this.unitName = unitName;
 	}
 }
