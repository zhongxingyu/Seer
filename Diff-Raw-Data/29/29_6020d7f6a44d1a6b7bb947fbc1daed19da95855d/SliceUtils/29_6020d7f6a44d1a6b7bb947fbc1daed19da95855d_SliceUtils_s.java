 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.common.ui.slicing;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import ncsa.hdf.object.Group;
 
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.io.SliceObject;
 import uk.ac.gda.doe.DOEUtils;
 
 public class SliceUtils {
 
     private static Logger logger = LoggerFactory.getLogger(SliceUtils.class);
 
 	private static NumberFormat format = DecimalFormat.getNumberInstance();
     /**
      * Generates a list of slice information for a given set of dimensional data.
      * 
      * The data may have fields which use DOE annotations and hence can be expanded.
      * This allows slices to be ranges in one or more dimensions which is a simple
      * form of summing sub-sets of data.
      * 
      * @param dimsDataHolder
      * @param dataShape
      * @param sliceObject
      * @return a list of slices
      */
     public static SliceObject createSliceObject(final DimsDataList dimsDataHolder,
     		                                    final int[]        dataShape,
     		                                    final SliceObject  sliceObject)  {
 
     	
     	if (dimsDataHolder.size()!=dataShape.length) throw new RuntimeException("The dims data and the data shape are not equal!");
     	
     	final SliceObject currentSlice = sliceObject!=null ? sliceObject.clone() : new SliceObject();
 
     	// This ugly code results from the ugly API to the slicing.
     	final int[] start  = new int[dimsDataHolder.size()];
     	final int[] stop   = new int[dimsDataHolder.size()];
     	final int[] step   = new int[dimsDataHolder.size()];
     	AbstractDataset x  = null;
     	AbstractDataset y  = null;
     	final StringBuilder buf = new StringBuilder();
 
      	//buf.append("\n"); // New graphing can deal with long titles.
     	for (int i = 0; i < dimsDataHolder.size(); i++) {
 
     		final DimsData dimsData = dimsDataHolder.getDimsData(i);
     		
     		start[i] = getStart(dimsData);
     		stop[i]  = getStop(dimsData,dataShape[i]);
     		step[i]  = getStep(dimsData);
 
     		if (dimsData.getAxis()<0) {
     			String nexusAxisName = getNexusAxisName(currentSlice, dimsData, " (Dim "+(dimsData.getDimension()+1)); 
     			String formatValue   = String.valueOf(dimsData.getSlice());
     			try {
     			    formatValue = format.format(getNexusAxisValue(sliceObject, dimsData, dimsData.getSlice(), null));
     			} catch (Throwable ne) {
     				formatValue   = String.valueOf(dimsData.getSlice());
     			}
     			buf.append("("+nexusAxisName+" = "+(dimsData.getSliceRange()!=null?dimsData.getSliceRange():formatValue)+")");
     		}
 
     		if (dimsData.getAxis()==0) {
     			x = createAxisDataset(dataShape[i]);
     			x.setName("Dimension "+(dimsData.getDimension()+1));
     			currentSlice.setX(dimsData.getDimension());
     			
     		}
     		if (dimsData.getAxis()==1) {
     			y = createAxisDataset(dataShape[i]);
     			y.setName("Dimension "+(dimsData.getDimension()+1));
     			currentSlice.setY(dimsData.getDimension());
     		}
     		
         	if (dimsData.getSliceRange()!=null&&dimsData.getAxis()<0) {
         		currentSlice.setRange(true);
         	}
 
     	}
 
     	if (x==null || x.getSize()<2) { // Nothing to plot
     		logger.debug("Cannot slice into an image because one of the dimensions is size of 1");
     		return null;
     	}
     	
     	if (y!=null) {
     	    currentSlice.setSlicedShape(new int[]{x.getSize(),y.getSize()});
         	currentSlice.setAxes(Arrays.asList(new AbstractDataset[]{x,y}));
     	} else {
     		currentSlice.setSlicedShape(new int[]{x.getSize()});
         	currentSlice.setAxes(Arrays.asList(new AbstractDataset[]{x}));
    	    }
     	
     	currentSlice.setSliceStart(start);
     	currentSlice.setSliceStop(stop);
     	currentSlice.setSliceStep(step);
     	currentSlice.setShapeMessage(buf.toString());
 
     	return currentSlice;
 	}
   
     /**
      * 
      * @param sliceObject
      * @param data
      * @return
      * @throws Exception
      */
     public static String getNexusAxisName(SliceObject sliceObject, DimsData data) {
     	
     	return getNexusAxisName(sliceObject, data, "indices");
     }
     /**
      * 
      * @param sliceObject
      * @param data
      * @return
      * @throws Exception
      */
     public static String getNexusAxisName(SliceObject sliceObject, DimsData data, String alternateName) {
     	
     	try {
 			Map<Integer,String> dims = sliceObject.getNexusAxes();
 			String axisName = dims.get(data.getDimension()+1); // The data used for this axis
 			if (axisName==null || "".equals(axisName)) return alternateName;
 			return axisName;
     	} catch (Throwable ne) {
     		return alternateName;
     	}
     }
     /**
      * 
      * @param sliceObject
      * @param data
      * @param value
      * @param monitor
      * @return the nexus axis value or the index if no nexus axis can be found.
      */
 	public static Number getNexusAxisValue(SliceObject sliceObject, DimsData data, int value, IProgressMonitor monitor) {
         AbstractDataset axis = null;
         try {
         	final String axisName = getNexusAxisName(sliceObject, data);
             axis = SliceUtils.getNexusAxis(sliceObject, axisName, false, monitor);
         } catch (Exception ne) {
         	axis = null;
         }
         
         try {
             return axis!=null ? axis.getDouble(value)  :  value;
         } catch (Throwable ne) {
         	return value;
         }
 
 	}
 
 
 
 	private static int getStart(DimsData dimsData) {
 		if (dimsData.getAxis()>-1) {
 			return 0;
 		} else  if (dimsData.getSliceRange()!=null) {
 			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
 			return (int)exp[0];
 		}
 		return dimsData.getSlice();
 	}
 	
 	private static int getStop(DimsData dimsData, final int size) {
 		if (dimsData.getAxis()>-1) {
 			return size;
 		} else  if (dimsData.getSliceRange()!=null) {
 			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
 			return (int)exp[1];
 			
 		}
 		return dimsData.getSlice()+1;
 	}
 
 	private static int getStep(DimsData dimsData) {
 		if (dimsData.getAxis()>-1) {
 			return 1;
 		} else  if (dimsData.getSliceRange()!=null) {
 			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
 			return (int)exp[2];
 			
 		}
 		return 1;
 	}
 
 
 	public static AbstractDataset createAxisDataset(int size) {
 		final int[] data = new int[size];
 		for (int i = 0; i < data.length; i++) data[i] = i;
 		IntegerDataset ret = new IntegerDataset(data, size);
 		return ret;
 	}
 
 	/**
 	 * Thread safe and time consuming part of the slice.
 	 * @param currentSlice
 	 * @param dataShape
 	 * @param type
 	 * @param plottingSystem - may be null, but if so no plotting will happen.
 	 * @param monitor
 	 * @throws Exception
 	 */
 	public static void plotSlice(final SliceObject       currentSlice,
 			                     final int[]             dataShape,
 			                     final PlotType          type,
 			                     final AbstractPlottingSystem   plottingSystem,
 			                     final IProgressMonitor  monitor) throws Exception {
 
 		if (plottingSystem==null) return;
 		if (monitor!=null) monitor.worked(1);
 		if (monitor!=null&&monitor.isCanceled()) return;
 		
         currentSlice.setFullShape(dataShape);
 		final AbstractDataset slice = getSlice(currentSlice,monitor);
 		if (slice==null) return;
 		
 		// We sum the data in the dimensions that are not axes
 		if (monitor!=null) monitor.worked(1);		
 		if (monitor!=null&&monitor.isCanceled()) return;
 		
 		if (monitor!=null) monitor.worked(1);
 		
 		boolean requireScale = plottingSystem.isRescale()
 				               || type!=plottingSystem.getPlotType();
 		
 		if (type==PlotType.XY) {
 			plottingSystem.clear();
 			final AbstractDataset x = getNexusAxis(currentSlice, slice.getShape()[0], currentSlice.getX()+1, true, monitor);
 			plottingSystem.setXfirst(true);
 			plottingSystem.createPlot1D(x, Arrays.asList(slice), slice.getName(), monitor);
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					plottingSystem.getSelectedXAxis().setTitle(x.getName());
 					plottingSystem.getSelectedYAxis().setTitle("");
 				}
 			});
 			
 		} else if (type==PlotType.XY_STACKED || type==PlotType.XY_STACKED_3D) {
 			
 			final AbstractDataset xAxis = getNexusAxis(currentSlice, slice.getShape()[0], currentSlice.getX()+1, true, monitor);
 			plottingSystem.clear();
 			// We separate the 2D image into several 1d plots
 			final int[]         shape = slice.getShape();
 			final List<double[]> sets = new ArrayList<double[]>(shape[1]);
 			for (int x = 0; x < shape[0]; x++) {
 				for (int y = 0; y < shape[1]; y++) {
 					
 					if (y > (sets.size()-1)) sets.add(new double[shape[0]]);
 					double[] data = sets.get(y);
 					data[x] = slice.getDouble(x,y);
 				}
 			}
 			final List<AbstractDataset> ys = new ArrayList<AbstractDataset>(shape[1]);
 			int index = 0;
 			for (double[] da : sets) {
 				final DoubleDataset dds = new DoubleDataset(da, da.length);
 				dds.setName(String.valueOf(index));
 				ys.add(dds);
 				++index;
 			}
 			plottingSystem.setXfirst(true);
 			plottingSystem.setPlotType(type);
 			plottingSystem.createPlot1D(xAxis, ys, monitor);
 			
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					plottingSystem.getSelectedXAxis().setTitle(xAxis.getName());
 					plottingSystem.getSelectedYAxis().setTitle("");
 				}
 			});
 			
 		} else if (type==PlotType.IMAGE || type==PlotType.SURFACE){
 			plottingSystem.setPlotType(type);
 			AbstractDataset y = getNexusAxis(currentSlice, slice.getShape()[0], currentSlice.getX()+1, true, monitor);
 			AbstractDataset x = getNexusAxis(currentSlice, slice.getShape()[1], currentSlice.getY()+1, true, monitor);		
 			if (monitor!=null&&monitor.isCanceled()) return;
 			
 			// Nullify user objects because the ImageHistoryTool uses
 			// user objects to know if the image came from it. Since we
 			// use update here, we update (as its faster) but we also 
 			// nullify the user object.
 			final IImageTrace trace = getImageTrace(plottingSystem);
 			if (trace!=null) trace.setUserObject(null);
 			plottingSystem.updatePlot2D(slice, Arrays.asList(x,y), monitor); 			
 		}
 
 		plottingSystem.repaint(requireScale);
 
 	}
 	
 	/**
 	 * this method gives access to the image trace plotted in the
 	 * main plotter or null if one is not plotted.
 	 * @return
 	 */
 	private static IImageTrace getImageTrace(IPlottingSystem plotting) {
 		if (plotting == null) return null;
 
 		final Collection<ITrace> traces = plotting.getTraces(IImageTrace.class);
 		if (traces==null || traces.size()==0) return null;
 		final ITrace trace = traces.iterator().next();
 		return trace instanceof IImageTrace ? (IImageTrace)trace : null;
 	}
 
 
 	/**
 	 * 
 	 * @param currentSlice
 	 * @param length of axis
 	 * @param inexusAxis nexus dimension (starting with 1)
 	 * @param requireIndicesOnError
 	 * @param monitor
 	 * @return
 	 * @throws Exception
 	 */
 	public static AbstractDataset getNexusAxis(SliceObject currentSlice, int length, int inexusAxis, boolean requireIndicesOnError, final IProgressMonitor  monitor) throws Exception {
 		
 		String axisName = currentSlice.getNexusAxis(inexusAxis);
 		if ("indices".equals(axisName) || axisName==null) {
 			AbstractDataset indices = IntegerDataset.arange(length, IntegerDataset.INT); // Save time
 			indices.setName("");
 			return indices;
 		}
 		
 		try {
 			return getNexusAxis(currentSlice, axisName, true, monitor);
 			
 		} catch (Throwable ne) {
 			logger.error("Cannot get nexus axis during slice!", ne);
 			if (requireIndicesOnError) {
 				AbstractDataset indices = IntegerDataset.arange(length, IntegerDataset.INT); // Save time
 				indices.setName("");
 				return indices;
 
 			} else {
 				return null;
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param currentSlice
 	 * @param axisName
 	 * @param requireUnit - if true will get unit but will be slower.
 	 * @param requireIndicesOnError
 	 * @param monitor
 	 * @return
 	 */
 	public static AbstractDataset getNexusAxis(final SliceObject currentSlice, 
 			                                          String axisName, 
 			                                   final boolean requireUnit,
 			                                   final IProgressMonitor  monitor) throws Exception {
 		
 		if (requireUnit) { // Slower
 			IHierarchicalDataFile file = null;
 			try {
 				file = HierarchicalDataFactory.getReader(currentSlice.getPath());
 				final Group  group    = file.getParent(currentSlice.getName());
 				final String fullName = group.getFullName()+"/"+axisName;
 				AbstractDataset axis = LoaderFactory.getDataSet(currentSlice.getPath(), fullName, new ProgressMonitorWrapper(monitor));
 				axis = axis.squeeze();
 				final String unit = file.getAttributeValue(fullName+"@unit");
 				if (unit!=null) axisName = axisName+" "+unit;
 				axis.setName(axisName);
 			    return axis;
 			} finally {
 				if (file!=null) file.close();
 			}
 		} else { // Faster
 			
 			final String dataPath = currentSlice.getName();
 			final File file = new File(dataPath);
 			final String fullName = file.getParent().replace('\\','/')+"/"+axisName;
 			AbstractDataset axis = LoaderFactory.getDataSet(currentSlice.getPath(), fullName, new ProgressMonitorWrapper(monitor));
 			axis = axis.squeeze();
 			axis.setName(axisName);
 			return axis;
 		
 		}
 
 	}
 
 
 	public static AbstractDataset getSlice(final SliceObject       currentSlice,
 			                               final IProgressMonitor  monitor) throws Exception {
 		
 		final int[] dataShape = currentSlice.getFullShape();
 		AbstractDataset slice = LoaderFactory.getSlice(currentSlice, new ProgressMonitorWrapper(monitor));
 		slice.setName("Slice of "+currentSlice.getName()+" "+currentSlice.getShapeMessage());
 		
 		if (currentSlice.isRange()) {
 			// We sum the data in the dimensions that are not axes
 			AbstractDataset sum    = slice;
 			final int       len    = dataShape.length;
 			for (int i = len-1; i >= 0; i--) {
 				if (!currentSlice.isAxis(i) && dataShape[i]>1)
 					sum = sum.sum(i);
 				if (monitor!=null) monitor.worked(1);
 				if (monitor!=null&&monitor.isCanceled()) return null;
 			}
 
 			if (currentSlice.getX() > currentSlice.getY()) sum = sum.transpose();
 			if (monitor!=null) monitor.worked(1);
 			if (monitor!=null&&monitor.isCanceled()) return null;
 			sum.setName(slice.getName());
 			
 			if (monitor!=null&&monitor.isCanceled()) return null;
 			
 			sum = sum.squeeze();
 			slice = sum;
 		} else {
 
 			slice = slice.squeeze();		
 			if (currentSlice.getX() > currentSlice.getY() && slice.getShape().length==2) {
 				// transpose clobbers name
 				final String name = slice.getName();
 				slice = slice.transpose();
 				if (name!=null) slice.setName(name);
 			}
 		}
 		return slice;
 	}
 
     /**
      * Transforms a SliceComponent defined slice into an expanded set
      * of slice objects so that the data can be sliced out of the h5 file.
      * 
      * @param fullShape
      * @param dimsDataList
      * @return
      */
 	public static List<SliceObject> getExpandedSlices(final int[]  fullShape,
 			                                          final Object dimsDataList) {	
 
 		final DimsDataList      ddl = (DimsDataList)dimsDataList;
 		final List<SliceObject> obs = new ArrayList<SliceObject>(89);
 		createExpandedSlices(fullShape, ddl, 0, new ArrayList<DimsData>(ddl.size()), obs);
 		return obs;
 	}
 
 
 	private static void createExpandedSlices(final int[]             fullShape,
 			                                 final DimsDataList      ddl,
 			                                 final int               index,
 			                                 final List<DimsData>    chunk,
 			                                 final List<SliceObject> obs) {
 		
 		final DimsData       dat = ddl.getDimsData(index);
 		final List<DimsData> exp = dat.expand(fullShape[index]);
 		
 		for (DimsData d : exp) {
 			
 			chunk.add(d);
 			if (index==ddl.size()-1) { // Reached end
 				SliceObject ob = new SliceObject();
 				ob.setFullShape(fullShape);
 				ob = SliceUtils.createSliceObject(new DimsDataList(chunk), fullShape, ob);
 				obs.add(ob);
 				chunk.clear();
 			} else {
 				createExpandedSlices(fullShape, ddl, index+1, chunk, obs);
 			}
 			
 		}
 	}
 
 }
