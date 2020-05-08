 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.dawnsci.plotting.tools.window;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.trace.IStackTrace;
 import org.dawb.common.ui.plot.trace.ISurfaceTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.IWindowTrace;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawnsci.plotting.Activator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.mihalis.opal.rangeSlider.RangeSlider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.gda.richbeans.components.scalebox.IntegerBox;
 import uk.ac.gda.richbeans.components.scalebox.NumberBox;
 import uk.ac.gda.richbeans.event.ValueAdapter;
 import uk.ac.gda.richbeans.event.ValueEvent;
 
 /**
  * A tool which has one box region for configuring the region
  * which defines the window of a 3D plot.
  * 
  * TODO Add aspect ratio controls like the old windowing tool used to have.
  * 
  * @author fcp94556
  *
  */
 public class WindowTool extends AbstractToolPage {
 
 	private static final Logger logger = LoggerFactory.getLogger(WindowTool.class);
 	
 	private AbstractPlottingSystem windowSystem;
 	private IRegionListener        regionListener;
 	private IROIListener           roiListener;
 	private ITraceListener         traceListener;
 	private WindowJob              windowJob;
	private Composite              sliceControl;
 	private Composite              content;
  
 	public WindowTool() {
 		try {
 			this.windowSystem  = PlottingFactory.createPlottingSystem();
 			this.windowJob     = new WindowJob();
 			
 			this.traceListener = new ITraceListener.Stub() {
 				protected void update(TraceEvent evt) {
 					ITrace trace = getTrace();
 					if (trace!=null) {
 						updateTrace(trace);
 					} else {
 						windowSystem.clear();
 					}
 				}
 			};
 			
 			this.roiListener = new IROIListener.Stub() {
 				public void update(ROIEvent evt) {
 					windowJob.schedule(evt.getROI());
 				}
 			};
 			
 			this.regionListener = new IRegionListener.Stub() {
 				@Override
 				public void regionAdded(RegionEvent evt) {
 					evt.getRegion().addROIListener(roiListener);
 				}
 
 				@Override
 				public void regionRemoved(RegionEvent evt) {
 					evt.getRegion().removeROIListener(roiListener);
 				}			
 			};
 			
 		} catch (Exception e) {
 			logger.error("Cannot create a plotting system, something bad happened!", e);
 		}
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		
 		this.content = new Composite(parent, SWT.NONE);
 		final StackLayout stackLayout = new StackLayout();
 		content.setLayout(stackLayout);
 		
 		windowSystem.createPlotPart(content, getTitle(), getSite().getActionBars(), PlotType.IMAGE, getPart());
 		
 		try {
 			final ISurfaceTrace surface = getSurfaceTrace();
 							
 			final IRegion region = windowSystem.createRegion("Window", RegionType.BOX);
 			region.setROI(surface!=null ? surface.getWindow() : new RectangularROI(0,0,300,300,0));
 			windowSystem.addRegion(region);
 			
 		} catch (Exception e) {
 			logger.error("Cannot create region for surface!", e);
 		}
 		
         this.sliceControl = createSliceControl();
    
         final ITrace trace = getTrace();
 		if (trace instanceof ISurfaceTrace) {
 			stackLayout.topControl = windowSystem.getPlotComposite();
 		} else if (trace instanceof IStackTrace) {
 		    stackLayout.topControl = sliceControl;
 		}
         
 	}
 	
 	private CLabel      errorLabel;
 	private RangeSlider sliceSlider;
 	private NumberBox   lowerControl, upperControl;
 	private int         lastLower = -1, lastUpper = -1;
 	
 	private Composite createSliceControl() {
 		Composite sliceControl = new Composite(content, SWT.NONE);
 		sliceControl.setLayout(new GridLayout(3, false));
 		
 		final Label info = new Label(sliceControl, SWT.WRAP);
 		info.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 2));
 		info.setText("Please edit the window of the data, not more than 100 symultaneous plots are allowed in 3D.");
 	
 		sliceSlider = new RangeSlider(sliceControl, SWT.HORIZONTAL);
 		sliceSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2));
 		sliceSlider.setMinimum(0);
 		sliceSlider.setMaximum(25);
 		sliceSlider.setLowerValue(0);
 		sliceSlider.setUpperValue(25);
 		sliceSlider.setIncrement(1);
 		
 		GridData gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false, 2, 1);
 		gridData.widthHint=100;
 		
         lowerControl = new IntegerBox(sliceControl, SWT.NONE);
         lowerControl.setLabel(" Lower    ");
         lowerControl.setLayoutData(gridData);
         lowerControl.setIntegerValue(sliceSlider.getLowerValue());
         lowerControl.setActive(true);
         lowerControl.on();
         lowerControl.addValueListener(new ValueAdapter() {			
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				sliceSlider.setLowerValue(lowerControl.getIntegerValue());
 				final int lower = sliceSlider.getLowerValue();
 				final int upper = sliceSlider.getUpperValue();
 				if (lower<0 || upper<0)                   return;
 				updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
 				lastLower = lower;
 				lastUpper = upper;
 			}
 		});
  
         upperControl = new IntegerBox(sliceControl, SWT.NONE);
         upperControl.setLabel(" Upper    ");
         upperControl.setLayoutData(gridData);
         upperControl.setIntegerValue(sliceSlider.getUpperValue());
         upperControl.setActive(true);
         upperControl.on();
         upperControl.addValueListener(new ValueAdapter() {			
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				sliceSlider.setUpperValue(upperControl.getIntegerValue());
 				final int lower = sliceSlider.getLowerValue();
 				final int upper = sliceSlider.getUpperValue();
 				if (lower<0 || upper<0)                   return;
 				updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
 				lastLower = lower;
 				lastUpper = upper;
 			}
 		});
         
         upperControl.setMinimum(lowerControl);
         upperControl.setMaximum(25);
         lowerControl.setMinimum(0);
         lowerControl.setMaximum(upperControl);
         
 		errorLabel = new CLabel(sliceControl, SWT.WRAP);
 		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
 		errorLabel.setText("The slice range is too large.");
 		errorLabel.setImage(Activator.getImage("icons/error.png"));
         
 		sliceSlider.addMouseMoveListener(new MouseMoveListener() {
 			
 			@Override
 			public void mouseMove(MouseEvent e) {
 				if ((e.button & SWT.BUTTON1)==0) {
 					final int lower = sliceSlider.getLowerValue();
 					final int upper = sliceSlider.getUpperValue();
 					if (lower<0 || upper<0)                   return;
 					if (lower==lastLower && upper==lastUpper) return;
 					
 					sliceSlider.setToolTipText("("+sliceSlider.getMinimum()+")  "+lower+" <-> "+upper+"  ("+sliceSlider.getMaximum()+")");
 					upperControl.setIntegerValue(upper);
 					lowerControl.setIntegerValue(lower);
 					updateSliceRange(lower, upper, sliceSlider.getMaximum(), false);
 					
 					lastLower = lower;
 					lastUpper = upper;
 				}
 			}
 		});
 		
 
 		return sliceControl;
 	}
 
 	
 	protected void updateSliceRange(int lower, int upper, int max, boolean setValue) {
 		if (upper-lower>100) {
 			GridUtils.setVisible(errorLabel, true);
 			errorLabel.getParent().layout();
 			return;
 		}
 		
 		GridUtils.setVisible(errorLabel, false);
 		errorLabel.getParent().layout();
 		
 		if (setValue) { // Send to UI
 			sliceSlider.setMaximum(max);
 			sliceSlider.setLowerValue(lower);
 			sliceSlider.setUpperValue(upper);
 			lowerControl.setIntegerValue(lower);
 			upperControl.setIntegerValue(upper);
 	        upperControl.setMaximum(max);
 		} else {        // Send to region
 			final LinearROI   roi   = new LinearROI(new double[]{lower,0}, new double[]{upper,0});
 			windowJob.schedule(roi);
 		}
 	
 	}
 
 	protected void updateTrace(ITrace trace) {
 		if (trace instanceof ISurfaceTrace) {
 		    setActionsEnabled(true);
 			updateWindowPlot((ISurfaceTrace)trace);
 		} else if (trace instanceof IStackTrace) {
 		    setActionsEnabled(false);
 		    updateSlicePlot((IStackTrace)trace);
 		} else {
 		    setActionsEnabled(false);
 			StackLayout stackLayout = (StackLayout)content.getLayout();
			stackLayout.topControl  = null;
 			content.layout();
 		}
 	}
 
 	private void setActionsEnabled(boolean enabled) {
 		IContributionManager[] mans = new IContributionManager[]{ 
 				                             getSite().getActionBars().getToolBarManager(),
 				                             getSite().getActionBars().getMenuManager()};
 		for (IContributionManager man : mans) {
 			IContributionItem[] items = man.getItems();
 			for (IContributionItem item : items) {
 				item.setVisible(enabled);
 			}
 			man.update(true);
 		}
 	}
 
 	protected void updateWindowPlot(ISurfaceTrace trace) {
 		StackLayout stackLayout = (StackLayout)content.getLayout();
 		stackLayout.topControl  = windowSystem.getPlotComposite();
 		
 		AbstractDataset data = trace.getData();
 		List<AbstractDataset> axes = trace.getAxes();
 		if (axes!=null) axes = Arrays.asList(axes.get(0), axes.get(1));
 		windowSystem.updatePlot2D(data, axes, null);	
 		
 		content.layout();
 	}
 	
 	protected void updateSlicePlot(IStackTrace trace) {
 		
 		StackLayout stackLayout = (StackLayout)content.getLayout();
 		stackLayout.topControl  = sliceControl;
 		
 		final LinearROI roi = (LinearROI)trace.getWindow();
 		if (roi!=null) {
 			final int lower = roi.getIntPoint()[0];
 			final int upper = (int)Math.round(((LinearROI)roi).getEndPoint()[0]);
 		    updateSliceRange(lower, upper, trace.getStack().length, true);
 		}
 		content.layout();
 	}
 
 
 	@Override
 	public void activate() {
 		super.activate();
 		
 		if (getPlottingSystem()!=null) {
 			
 			getPlottingSystem().addTraceListener(traceListener);
 			
 		}
 		if (windowSystem!=null && windowSystem.getPlotComposite()!=null) {
 			
 			final ITrace trace = getTrace();
 			if (trace!=null) updateTrace(trace);
 
 			windowSystem.addRegionListener(regionListener);
 			
 			final Collection<IRegion> boxes = windowSystem.getRegions(RegionType.BOX);
 			if (boxes!=null) for (IRegion iRegion : boxes) iRegion.addROIListener(roiListener);
 		}
 	}
 	
 	@Override
 	public void deactivate() {
 		super.deactivate();
 		
 		if (getPlottingSystem()!=null) {
 			
 			getPlottingSystem().removeTraceListener(traceListener);
 			
 		}
 		if (windowSystem!=null && windowSystem.getPlotComposite()!=null) {
 			windowSystem.removeRegionListener(regionListener);
 			
 			final Collection<IRegion> boxes = windowSystem.getRegions(RegionType.BOX);
 			if (boxes!=null) for (IRegion iRegion : boxes) iRegion.removeROIListener(roiListener);
 		}
 	}
 
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_3D;
 	}
 
 
 	@Override
 	public Control getControl() {
 		return content;
 	}
 	
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return windowSystem;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 
 	@Override
 	public void setFocus() {
 		if (windowSystem!=null) windowSystem.setFocus();
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (windowSystem!=null) windowSystem.dispose();
 		windowSystem = null;
 	}
 	
 	
 	private class WindowJob extends Job {
 
 		private ROIBase window;
 
 		public WindowJob() {
 			super("Window");
 			setPriority(Job.INTERACTIVE);
 			setUser(false);
 			setSystem(true);
 		}
 		
 		protected void schedule(ROIBase window) {
 			cancel();
 			this.window = window;
 			schedule();
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			
 			final IWindowTrace windowTrace = getWindowTrace();
 			if (windowTrace!=null) {
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						windowTrace.setWindow(window);
 					}
 				});
 			}
 	
 			return Status.OK_STATUS;
 		}
 		
 	}
 
 }
