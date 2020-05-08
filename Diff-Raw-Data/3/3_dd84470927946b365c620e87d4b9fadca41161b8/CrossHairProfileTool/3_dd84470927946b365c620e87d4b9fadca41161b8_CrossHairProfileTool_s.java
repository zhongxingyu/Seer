 package org.dawb.workbench.plotting.tools;
 
 import java.util.Collection;
 import java.util.List;
 
import org.dawb.common.ui.IAxis;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.util.ColorUtility;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.jface.action.Action;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.part.IPageSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public class CrossHairProfileTool extends AbstractToolPage implements IROIListener, MouseListener  {
 
 	private final static Logger logger = LoggerFactory.getLogger(CrossHairProfileTool.class);
 	
 	protected IPlottingSystem        profilePlotter;
 	private   ITraceListener         traceListener;
 	private   IRegion                xHair, yHair;
 	private   IAxis                  x1,x2;
 	private   RunningJob             xUpdateJob, yUpdateJob;
 	private   ROIBase           xBounds, yBounds;
 	
 	public CrossHairProfileTool() {
 		try {
 			
 			profilePlotter = PlottingFactory.createPlottingSystem();
 			this.traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesPlotted(TraceEvent evt) {
 					
 					if (!(evt.getSource() instanceof List<?>)) {
 						return;
 					}
 					
 					if (xUpdateJob!=null) xUpdateJob.scheduleIfNotSuspended();
 					if (yUpdateJob!=null) yUpdateJob.scheduleIfNotSuspended();
 				}
 			};
 						
 		} catch (Exception e) {
 			logger.error("Cannot get plotting system!", e);
 		}
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 
 
 		final IPageSite site = getSite();
 		
 		profilePlotter.createPlotPart(parent, 
 								getTitle(), 
 								site.getActionBars(), 
 								PlotType.PT1D,
 								this.getViewPart());		
 		
 		profilePlotter.getSelectedYAxis().setTitle("Intensity");
 		this.x1 = profilePlotter.getSelectedXAxis();
 		x1.setTitle("X Slice");
 		
 		this.x2 = profilePlotter.createAxis("Y Slice", false, SWT.TOP);
 			
 		final Action reset = new Action("Clear cross hair profiles", Activator.getImageDescriptor("icons/axis.png")) {
 			public void run() {
 				profilePlotter.reset();
 				getPlottingSystem().clearRegions();
 			}
 		};
 		getSite().getActionBars().getToolBarManager().add(reset);
 		
 		activate();
 	}
 	
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return profilePlotter;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 	private void createRegions() {
 		
 		if (getPlottingSystem()==null) return;
 		try {
 			if (xHair==null || getPlottingSystem().getRegion(xHair.getName())==null) {
 				this.xHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Y Profile", getPlottingSystem()), IRegion.RegionType.XAXIS_LINE);
 				this.xUpdateJob = addRegion("Updating x cross hair", xHair);
 
 			}
 			
 			if (yHair==null || getPlottingSystem().getRegion(yHair.getName())==null) {
 				this.yHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("X Profile", getPlottingSystem()), IRegion.RegionType.YAXIS_LINE);
 				this.yUpdateJob = addRegion("Updating x cross hair", yHair);
 			}
 			
 		} catch (Exception ne) {
 			logger.error("Cannot create cross-hairs!", ne);
 		}
 	}
 	
 	private RunningJob addRegion(String jobName, IRegion region) {
 		region.setVisible(false);
 		region.setTrackMouse(true);
 		region.setRegionColor(ColorConstants.red);
 		region.setUserRegion(false); // They cannot see preferences or change it!
 		getPlottingSystem().addRegion(region);
 		return new RunningJob(jobName, region);
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void setFocus() {
 		
 	}
 	
 	public void activate() {
 		
 		createRegions();
 		if (xHair!=null) {
 			if (!isActive()) xHair.addMouseListener(this);
 			xHair.setVisible(true);
 			xHair.addROIListener(this);
 		}
 		if (yHair!=null) {
 			yHair.setVisible(true);
 			yHair.addROIListener(this);
 		}
 
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addTraceListener(traceListener);
 		}
 		
 		// We stop the adding of other regions because this tool does
 		// not like it when other regions are added.
 		setOtherRegionsEnabled(false);
 		
 		super.activate();	
 	}
 	
 	private static final String regionId = "org.dawb.workbench.ui.editors.plotting.swtxy.addRegions";
 	
 	private void setOtherRegionsEnabled(boolean isVisible) {
 
         final IActionBars bars = getPlottingSystem().getActionBars();
         if (bars.getToolBarManager().find(regionId)!=null) {
         	bars.getToolBarManager().find(regionId).setVisible(isVisible);
         	bars.getToolBarManager().update(true);
         }
         if (bars.getMenuManager().find(regionId)!=null) {
         	bars.getMenuManager().find(regionId).setVisible(isVisible);
         	bars.getMenuManager().update(true);
         }
 	}
 
 	public void deactivate() {
 		super.deactivate();
 		setOtherRegionsEnabled(true);
 
 		if (xHair!=null) {
 			xHair.removeMouseListener(this);
 			xHair.setVisible(false);
 			xHair.removeROIListener(this);
 		}
 		if (yHair!=null) {
 			yHair.setVisible(false);
 			yHair.removeROIListener(this);
 		}
 		profilePlotter.clear();
 
 		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
 	}
 	
 	public void dispose() {
 		
 	    deactivate();
 		if (profilePlotter!=null) profilePlotter.dispose();
 		profilePlotter = null;
 		super.dispose();
 	}
 	
 	@Override
 	public Control getControl() {
 		if (profilePlotter==null) return null;
 		return profilePlotter.getPlotComposite();
 	}
 
 
 	/**
 	 * The user can optionally nominate an x. In this case, we would like to 
 	 * use it for the derivative instead of the indices of the data. Therefore
 	 * there is some checking here to see if there are x values to plot.
 	 * 
 	 * Normally everything will be ILineTraces even if the x is indices.
 	 */
 	private class RunningJob extends Job {
 
 		private boolean isJobRunning = false;
 		private IRegion region;
 		private boolean suspend = false;
 		
 		RunningJob(String name, IRegion region) {
 			super(name);
 			this.region = region;
 		}
 
 		@Override
 		protected IStatus run(final IProgressMonitor monitor) {
 
 			try {
 				isJobRunning = true;
 				if (!isActive()) return  Status.CANCEL_STATUS;
 	
 				if (x1==null | x2==null) return Status.OK_STATUS;
 	
 				ROIBase bounds = region==xHair ? xBounds : yBounds;
 				
 				final boolean ok = profile(region, bounds, false, null, monitor);
 
 			    return ok ? Status.OK_STATUS : Status.CANCEL_STATUS;
 			    
 			} finally {
 				isJobRunning = false;
 			}
 		}	
 		
 
 		/**
 		 * Blocks until job has been stopped, does nothing if not running.
 		 */
 		public void stop() {
 			if (isJobRunning) cancel();
 		}
 
 		public void suspend(boolean suspend) {
 			this.suspend  = suspend;
 			cancel();	
 		}
 		
 		public void scheduleIfNotSuspended() {
 			if (suspend) return;
 			super.schedule();
 		}
 	}
 
 
 	@Override
 	public void roiDragged(ROIEvent evt) {
 		update((IRegion)evt.getSource(), evt.getROI());
 	}
 
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		final IRegion region = (IRegion)evt.getSource();
 		update(region, region.getROI());
 	}
 	
 	private void update(IRegion r, ROIBase rb) {
 		if (r == xHair) {
 			xUpdateJob.stop();
 			this.xBounds = rb;
 			xUpdateJob.scheduleIfNotSuspended();
 		}
 		if (r == yHair) {
 			yUpdateJob.stop();
 			this.yBounds = rb;
 			yUpdateJob.scheduleIfNotSuspended();
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 		
 		if (!isActive()) return;
 		try {
 			xUpdateJob.suspend(true);
 			yUpdateJob.suspend(true);
 	
 	        final Color   snapShotColor = RegionUtils.getUnqueColor(xHair.getRegionType(), getPlottingSystem(), ColorUtility.DEFAULT_SWT_COLORS);
 	        final IRegion x = createStaticRegion("Y Profile Static", xBounds, snapShotColor, xHair.getRegionType());
 	        profile(x, xBounds, true, snapShotColor, new NullProgressMonitor());
 	        
 	        final IRegion y = createStaticRegion("X Profile Static", yBounds, snapShotColor, yHair.getRegionType());
 	        profile(y, yBounds, true, snapShotColor, new NullProgressMonitor());
 
 			//getPlottingSystem().repaint();
 		} catch (Exception ne) {
 			logger.error(ne.getMessage(), ne);
 			
 		} finally {
 			xUpdateJob.suspend(false);
 			yUpdateJob.suspend(false);
 		}
 	}
 
 	private IRegion createStaticRegion(String nameStub, final ROIBase bounds, final Color snapShotColor, final RegionType regionType) throws Exception {
 		
 
 		final IRegion region = getPlottingSystem().createRegion(RegionUtils.getUniqueName(nameStub, getPlottingSystem()), regionType);
 		region.setRegionColor(snapShotColor);
 		getPlottingSystem().addRegion(region);
 		region.setROI(bounds);
         getPlottingSystem().addRegionListener(new IRegionListener.Stub() {
     		@Override
     		public void regionRemoved(RegionEvent evt) {
     			if (profilePlotter.getTrace(region.getName())!=null) {
     				profilePlotter.removeTrace(profilePlotter.getTrace(region.getName()));
     			}
     		}
         });
         
         region.addROIListener(new IROIListener.Stub() {
         	@Override
     		public void roiDragged(ROIEvent evt) {
         		if (!isActive()) return;
         		profile(region, evt.getROI(), false, snapShotColor, new NullProgressMonitor());
     		}
         });
 		
 		return region;
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent me) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseDoubleClicked(MouseEvent me) {
 		// When clicked adds new plot
 	}
 
 	
 	private boolean profile(final IRegion      region, 
 			                final ROIBase bounds, 
 			                final boolean      snapshot,
 			                final Color        snapShotColor,
 			                final IProgressMonitor monitor) {
 		
 		if (bounds!=null) {
 			
 			if (monitor.isCanceled()) return  false;
 			final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
 			IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
 
 			if (image==null) {
 				if (monitor.isCanceled()) return  false;
 				profilePlotter.clear();
 				return true;
 			}
 
 			if (monitor.isCanceled()) return  false;
 			
             		                  
 			ILineTrace trace = (ILineTrace)profilePlotter.getTrace(region.getName());
 			if (trace == null || snapshot) {
 				synchronized (profilePlotter) {  // Only one job at a time can choose axis and create plot.
 					if (region.getName().startsWith("Y Profile")) {
 						profilePlotter.setSelectedXAxis(x1);
 
 					} else {
 						profilePlotter.setSelectedXAxis(x2);
 					}
 					if (monitor.isCanceled()) return  false;
 					trace = profilePlotter.createLineTrace(region.getName());
 
 				    if (snapShotColor!=null) {
 				    	trace.setTraceColor(snapShotColor);
 				    } else {
 						if (region.getName().startsWith("Y Profile")) {
 							trace.setTraceColor(ColorConstants.blue);
 						} else {
 							trace.setTraceColor(ColorConstants.red);
 						}	
 				    }
 				}
 			}
 
 			final AbstractDataset data = image.getData();
 			AbstractDataset slice=null, sliceIndex=null;
 			if (monitor.isCanceled())return  false;
 			if (region.getName().startsWith("Y Profile")) {
 				int index = (int)Math.round(bounds.getPointX());
 				slice = data.getSlice(new int[]{0,index}, new int[]{data.getShape()[0], index+1}, new int[]{1,1});
 				if (monitor.isCanceled()) return  false;
 				slice = slice.flatten();
 				if (monitor.isCanceled()) return  false;
 				sliceIndex = AbstractDataset.arange(slice.getSize(), AbstractDataset.INT);
 
 			} else {
 				int index = (int)Math.round(bounds.getPointY());
 				slice = data.getSlice(new int[]{index,0}, new int[]{index+1, data.getShape()[1]}, new int[]{1,1});
 				if (monitor.isCanceled()) return  false;
 				slice = slice.flatten();
 				if (monitor.isCanceled()) return  false;
 				sliceIndex = AbstractDataset.arange(slice.getSize(), AbstractDataset.INT);
 			}
 			slice.setName(trace.getName());
 			trace.setData(sliceIndex, slice);
 
 			final ILineTrace finalTrace = trace;
 
 
 			if (monitor.isCanceled()) return  false;
 			getControl().getDisplay().syncExec(new Runnable() {
 				public void run() {
 
 					if (monitor.isCanceled()) return;
 					if (profilePlotter.getTrace(finalTrace.getName())==null) {							
 						profilePlotter.addTrace(finalTrace);
 					}
 
 					if (monitor.isCanceled()) return;
 					profilePlotter.autoscaleAxes();
 					profilePlotter.repaint();
 					if (region.getName().startsWith("Y Profile")) {
 						x1.setRange(0, data.getShape()[0]);
 					} else {
 						x2.setRange(0, data.getShape()[1]);
 					}
 				}
 			});
 		}
 		return true;
 	}
 
 }
