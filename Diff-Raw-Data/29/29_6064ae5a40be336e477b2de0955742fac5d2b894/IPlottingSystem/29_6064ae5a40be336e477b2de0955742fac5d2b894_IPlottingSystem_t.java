 /*-
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.common.ui.plot;
 
 import java.util.List;
 
 import org.dawb.common.ui.plot.annotation.IAnnotationSystem;
 import org.dawb.common.ui.plot.axis.IAxisSystem;
 import org.dawb.common.ui.plot.region.IRegionSystem;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceSystem;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchPart;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 
 /**
  * Represents a bridge to the plotting system.
  * 
  * Currently this is the diamond hardware accelerated system
  * or the fable directly drawn one.
  * 
  * To get your plotting system use the class PlottingFactory. This
  * will return the user preferred plotting.
  * 
  * @author gerring
  *
  */
 public interface IPlottingSystem extends ITraceSystem, IRegionSystem, IAxisSystem, IAnnotationSystem, IPrintablePlotting{
 
 	/**
 	 * Call to create the UI component dealing with the plotting.
 	 * @param parent
 	 * @param plotName
 	 * @param bars
 	 * @param hint
 	 * @param part - may be null
 	 */
 	public void createPlotPart(Composite      parent,
 			                   String         plotName,
 			                   IActionBars    bars,
 			                   PlotType       hint,
 			                   IWorkbenchPart part);
 
 	
 	/**
 	 * See also ITraceSystem for flexible trace manipulation.
 	 * 
 	 * For 1D - x is the x axis, ys is the y traces or null if only 1 data set should be plotted.
 	 * 
 	 * NOTE Using this option plots everything on the current x and y axes. These are the default axes,
 	 * to change axes, use createAxis(...) then setActiveAxis(...). Then subsequent plotting will plot
 	 * to these active axes.
 	 * 
 	 * Does not have to be called in the UI thread - any thread will do. Should be called to switch the entire
 	 * plot contents.
 	 * 
 	 * There is append for 1D plotting used for single points. @see IPlottingSystem.append(...)
 	 * 
 	 * Each call to createPlot1D(...) adds to the plot and the current selected axes, use reset() to clear the plot.
 	 * 
 	 * @param x
 	 * @param ys
 	 * @param mode
 	 * @param monitor
 	 * @return List of ITrace objects plotted, may be null. Normally you can cast these to ILineTrace as all 1D 
 	 *         plotting systems will wholly or partially support ILineTrace
 	 */
 	public List<ITrace> createPlot1D(AbstractDataset       x, 
 							         List<AbstractDataset> ys,
 							         IProgressMonitor      monitor);
	/**
	 * @see createPlot1D(AbstractDataset, List<AbstractDataset>, IProgressMonitor)
	 * @param x
	 * @param ys
	 * @param title - specifies the title instead of creating one.
	 * @param monitor
	 * @return
	 */
	public List<ITrace> createPlot1D(AbstractDataset       x, 
							         List<AbstractDataset> ys,
							         String title,
							         IProgressMonitor      monitor);

 	/**
 	 * Attempts to update any ILineTraces with the same name as the ys pass in, otherwise
 	 * will call createPlot1D(...)
 	 * 
 	 * @param x  - may be null, if null indices of y are used
 	 * @param ys -  must not be null
 	 * @param monitor
 	 * @return
 	 */
 	public List<ITrace> updatePlot1D(AbstractDataset       x, 
 							         List<AbstractDataset> ys,
 							         IProgressMonitor      monitor);
 
 	/**
 	 * See also ITraceSystem for flexible trace manipulation.
      *
 	 * For 2D - x is the image dataset, ys is the axes.
 	 * 
 	 * Does not have to be called in the UI thread. Should be called to switch the entire
 	 * plot contents. 
 	 * 
 	 * @param image
 	 * @param axes - may be null for pixels only. They must be data sets of the same size
 	 *               as the image. The first dataset is the x-axis in standard orientation,
 	 *               the second is the y-axis.
 	 * @param mode
 	 * @param monitor
 	 * @return Image trace plotted. You can normally cast this trace to an IImageTrace and
 	 *         use any image methods offered by this interface.
 	 */
 	public ITrace createPlot2D(AbstractDataset       image, 
 							   List<AbstractDataset> axes,
 							   IProgressMonitor      monitor);
 	
 	
 	/**
 	 * This method is similar to createPlot2D(...) however calling this method swaps the image data for
 	 * a plot - keeping zoom level intact. It can be used for a live update of an image plot for instance.
 	 * If there is no image to update, createPlot2D(...) will be called instead automatically.
 	 * 
 	 * @param image
 	 * @param axes
 	 * @param monitor
 	 * @return
 	 */
 	public ITrace updatePlot2D(AbstractDataset       image, 
 							   List<AbstractDataset> axes,
 							   IProgressMonitor      monitor);
 
 	/**
 	 * This method can be used to add a single plot data point to 
 	 * an individual 1D plot already created in createPlot(...). The dataSetName
 	 * argument is the same as the name of the original data set plotted,
 	 * the data is added to this plot efficiently.
 	 * 
 	 * Example of starting a plot with nothing and then adding points:
 	 * 
 	 * final AbstractDataset y = new DoubleDataset(new double[]{}, 0}
 	 * y.setName("y")
 	 * 
 	 * plottingSystem.createPlot(y, null, PlotType.PT1D, mon);
 	 * 
 	 * ...Update, x value indices in this case
 	 * plottingSystem.append("y", y.getSize()+1, 10, mon);
 	 * 
 	 * Call this update method in any thread, it will do the update in the UI thread
 	 * if not already called in the UI thread.
 	 * 
 	 * @param dataSetName
 	 * @param xValue - may be null if using indices
 	 * @param yValue
 	 * @param monitor - often null, this will be a fast operation.
 	 * @throws Exception
 	 */
 	public void append(final String           dataSetName, 
 			           final Number           xValue,
 					   final Number           yValue,
 					   final IProgressMonitor monitor) throws Exception ;
 
 	/**
 	 * Call to tell the plot to plot nothing and reset axes. Thread safe.
 	 */
 	public void reset();
 
 	/**
 	 * Call to tell the plot to remove data and leave axes unchanged. Thread safe.
 	 */
 	public void clear();
 
 	/**
 	 * Call to mark widgets and plotting as no longer required.
 	 * 
 	 * This will be called when the part is disposed.
 	 */
 	public void dispose();
 
 
 	/**
 	 * Redraws all the data
 	 */
 	public void repaint();
 	
     /**
      * The plot composite which plots are being drawn on.
      * 
      * Use this method with caution
      * 
      * @return
      */
 	public Composite getPlotComposite();
 		
 	/**
 	 * This ISelectionProvider will provide StructuredSelections which have been
 	 * made in the graph. It may be registered as a selection provider for the part
 	 * using this IPlottingSystem. The StructuredSelection will contain objects such
 	 * as 'LinerROI' for the selection inside the graph.
 	 * 
 	 * @return
 	 */
 	public ISelectionProvider getSelectionProvider();
 	
 	/**
 	 * Call this method to return a plotted data set by name. NOTE the plotting system
 	 * will likely not be using AbstractDataset as internal data. Instead it will get the
 	 * current data of the plot required and construct an AbstractDataset for it. This means
 	 * that you can plot int data but get back double data if the graph keeps data internally
 	 * as doubles for instance. If the append(...) method has been used, the data returned by
 	 * name from here will include the appended points.
 	 */
 	public AbstractDataset getData(final String dataSetName);
 
 
 	/**
 	 * 
 	 * @return true if some or all of the plotted data is 2D or images.
 	 */
 	public boolean is2D();
 	
 	/**
 	 * @return the action bars containing the graph actions.
 	 */
 	public IActionBars getActionBars();
 
     /**
      * Gives access to the action manager for removing and filling different actions.
      * @return
      */
 	public IPlotActionSystem getPlotActionSystem();
 }
