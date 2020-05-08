 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.gda.extensions.plot;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.util.GridUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DUIAdapter;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
 import uk.ac.diamond.scisoft.analysis.rcp.util.PlotMode;
 import uk.ac.diamond.scisoft.analysis.rcp.util.PlotUtils;
 import uk.ac.diamond.scisoft.analysis.rcp.views.PlotServerConnection;
 
 
 /**
  * Diamond plotter implementation of IPlottingSystem.
  * 
  * Deprecated but still maintained so that other implementations of IPlottingSystem
  * exist and can be 
  * 
  * @author gerring
  *
  */
 @Deprecated
 public class DiamondPlottingSystem extends AbstractPlottingSystem {
 
 	private Logger logger = LoggerFactory.getLogger(DiamondPlottingSystem.class);
 	
 	private PlotServerConnection plotServerConnection;
 	private PlotWindow           plotWindow;
 	
 	public void createPlotPart(final Composite      parent,
 							   final String         plotName,
 							   final IActionBars    bars,
 							   final PlotType       hint,
 							   final IWorkbenchPart part) {
 		
 		// Connect this PlotWindow to the server TODO Lazy initiation with this?
 		// no point connecting it to the plot server unless absolutely necessary.
 		this.plotServerConnection = new PlotServerConnection(plotName);
 		
 		final GuiBean     bean     = plotServerConnection.getGUIInfo();
 		final GuiPlotMode plotMode = (GuiPlotMode) bean.get(GuiParameters.PLOTMODE);
 		this.plotWindow = new PlotWindow(parent,
 				                        plotMode,
 										plotServerConnection,
 										plotServerConnection,
 										bars,
 										part.getSite().getPage(),
 										plotName);	
 		plotServerConnection.setPlotWindow(plotWindow);
 		
 		// This block relies on the plotter always starting as 1D which it does currently
 		// We intentionally fail if this is not the case so alert the test desks as to the
 		// change.
 		final Plot1DUIAdapter ui = (Plot1DUIAdapter)plotWindow.getPlotUI();
 		ui.addPositionSwitchListener(new IPropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				GridUtils.setVisible(pointControls, (Boolean)event.getNewValue());
 				pointControls.getParent().layout(new Control[]{pointControls});
 			}
 		}); 
 	}
 
 	@Override
 	public Composite getPlotComposite() {
        return plotWindow.getMainPlotter().getComposite();
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	public List<ITrace> createPlot1D(final AbstractDataset      x, 
 						               final List<AbstractDataset> ys,
 						               final IProgressMonitor      monitor) {
 		
 		PlotUtils.create1DPlot(x, ys, PlotMode.PM1D, plotWindow, monitor);
 		return null;
 	}
	
	@Override
	public List<ITrace> createPlot1D(final AbstractDataset      x, 
						               final List<AbstractDataset> ys,
						               final String title_Ignored,
						               final IProgressMonitor      monitor) {
		
		PlotUtils.create1DPlot(x, ys, PlotMode.PM1D, plotWindow, monitor);
		return null;
	}
 
 	/**
 	 * Just calls createPlot2D(...) directly.
 	 */
 	@Override
 	public ITrace updatePlot2D(final AbstractDataset       image, 
 							   List<AbstractDataset> axes,
 							   final IProgressMonitor      monitor) {
 		return createPlot2D(image, axes, monitor);
 	}
 
     @Override
 	public ITrace createPlot2D(final AbstractDataset       image, 
 							   List<AbstractDataset> axes,
 							   final IProgressMonitor      monitor) {
 		
 		if (axes == null) {
 			IntegerDataset x = IntegerDataset.arange(image.getShape()[0]);
 			IntegerDataset y = IntegerDataset.arange(image.getShape()[0]);
 			axes = Arrays.asList(new AbstractDataset[]{x,y});
 		}
 
         PlotUtils.createPlot(image, axes, GuiPlotMode.TWOD, plotWindow, monitor);
         return null;
 	}
 
 	private GuiPlotMode getGuiPlotMode(PlotType type) {
 		if (type == PlotType.IMAGE)   return GuiPlotMode.TWOD;
 		if (type == PlotType.SURFACE) return GuiPlotMode.SURF2D;
 		return null;
 	}
 
 	@Override
 	public void reset() {
 		try {
 			// BODGE - Cannot work out how to plot nothing.
 			final AbstractDataset id = new IntegerDataset(new int[]{0,0},2);
 			id.setName("-");
 			plotWindow.getMainPlotter().replaceAllPlots(Arrays.asList(new AbstractDataset[]{id}));
 			plotWindow.getMainPlotter().getColourTable().clearLegend();
 			plotWindow.getMainPlotter().refresh(true);
 		} catch (Throwable e) {
 			logger.error("Cannot remove plots!", e);
 		}
 		
 	}
 
 	@Override
 	public void dispose() {
     	if (plotServerConnection!=null) plotServerConnection.dispose();
     	if (plotWindow!=null)           plotWindow.dispose();
 	}
 
 	@Override
 	public void clear() {
 		try {
 			// BODGE - Cannot work out how to plot nothing.
 			final AbstractDataset id = new IntegerDataset(new int[]{0,0},2);
 			id.setName("-");
 			plotWindow.getMainPlotter().replaceAllPlots(Arrays.asList(new AbstractDataset[]{id}));
 			plotWindow.getMainPlotter().refresh(true);
 		} catch (Throwable e) {
 			logger.error("Cannot remove plots!", e);
 		}
 	}
 	
 	@Override
 	public AbstractDataset getData(String name) {
         final List<IDataset> sets = plotWindow.getMainPlotter().getCurrentDataSets();
         for (IDataset set : sets) {
 			if (name.equals(set.getName())) {
 				return (AbstractDataset)set;
 			}
 		}
         return null;
 	}
 
 	@Override
 	public void printPlotting() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void copyPlotting() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void savePlotting(String filename) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public List<ITrace> updatePlot1D(AbstractDataset x, List<AbstractDataset> ys, IProgressMonitor monitor) {
 		// TODO A bit wrong...
 		return createPlot1D(x, ys, monitor);
 	}
 
 	@Override
 	public void savePlotting(String filename, String filetype) {
 		// TODO Auto-generated method stub
 		
 	}
 }
