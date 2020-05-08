 /*-
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
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting;
 
 import gda.observable.IObserver;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ILineTrace.TraceType;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
 
 /**
  * Class to create a 1D plotting
  * 
  */
 public class Plotting1DUI extends AbstractPlotUI {
 
 	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.Plotting1DUI";
 	private static final Logger logger = LoggerFactory.getLogger(Plotting1DUI.class);
 
 	private AbstractPlottingSystem plottingSystem;
 	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
 	private String currentDataName;
 	private String dataName;
 	private String currentXAxisName;
 	private String xAxisName;
 
 	/**
 	 * Constructor of a plotting 1D 
 	 * @param plottingSystem plotting system
 	 */
 	public Plotting1DUI(AbstractPlottingSystem plottingSystem) {
 		this.plottingSystem = plottingSystem;
 	}
 
 	@Override
 	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
 		Collection<DataSetWithAxisInformation> plotData = dbPlot.getData();
 		
 		if (plotData != null) {
 			Iterator<DataSetWithAxisInformation> iter = plotData.iterator();
 			final List<AbstractDataset> yDatasets = Collections.synchronizedList(new LinkedList<AbstractDataset>());
 
 			final AbstractDataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
 			
 			while (iter.hasNext()) {
 				DataSetWithAxisInformation dataSetAxis = iter.next();
 				AbstractDataset data = dataSetAxis.getData();
 				yDatasets.add(data);
 				currentDataName = data.getName();
 			}
 			currentXAxisName = xAxisValues.getName();
 
 			Collection<ITrace> currentTraces = plottingSystem.getTraces();
 			for (ITrace iTrace : currentTraces) {
 				dataName = iTrace.getName();
				xAxisName = ((ILineTrace)iTrace).getXData().getName();
 			}
 
 			for (final AbstractDataset yData : yDatasets) {
 				// if same data being pushed to plot, we do an update instead of recreating the plot
 				if(currentDataName.equals(dataName)&&currentXAxisName.equals(xAxisName)){
 					Display.getDefault().asyncExec(new Runnable() {
 						@Override
 						public void run() {
							ILineTrace trace = (ILineTrace) plottingSystem.getTrace(currentDataName);
							trace.setData(xAxisValues, yData);
							trace.repaint();
 //							TODO : perform an auto scale only if no zoom has been previously done
 //								plottingSystem.autoscaleAxes();
 						}
 					});
 					logger.debug("Plot 1D updated");
 				} else {
 					plottingSystem.clear();
 					Collection<ITrace> traces = plottingSystem.createPlot1D(xAxisValues, yDatasets, null);
 					for (ITrace iTrace : traces) {
 						final ILineTrace lineTrace = (ILineTrace)iTrace;
 						Display.getDefault().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								lineTrace.setTraceType(TraceType.SOLID_LINE);
 							}
 						});
 					}
 					logger.debug("Plot 1D created");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void addIObserver(IObserver anIObserver) {
 		observers.add(anIObserver);
 	}
 
 	@Override
 	public void deleteIObserver(IObserver anIObserver) {
 		observers.remove(anIObserver);
 	}
 
 	@Override
 	public void deleteIObservers() {
 		observers.removeAll(observers);
 	}
 
 }
