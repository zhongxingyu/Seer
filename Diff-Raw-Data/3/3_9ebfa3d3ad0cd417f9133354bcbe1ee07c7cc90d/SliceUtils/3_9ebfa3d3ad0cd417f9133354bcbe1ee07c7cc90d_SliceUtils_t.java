 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawnsci.slicing.api.util;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import ncsa.hdf.object.Group;
 
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawnsci.doe.DOEUtils;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.slicing.api.system.DimsData;
 import org.dawnsci.slicing.api.system.DimsDataList;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.IAnalysisService;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
 import uk.ac.diamond.scisoft.analysis.io.SliceObject;
 
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
      * @throws Exception 
      */
     public static SliceObject createSliceObject(final DimsDataList dimsDataHolder,
     		                                    final int[]        dataShape,
     		                                    final SliceObject  sliceObject) throws Exception  {
 
     	
     	if (dimsDataHolder.size()!=dataShape.length) throw new RuntimeException("The dims data and the data shape are not equal!");
     	
     	final SliceObject currentSlice = sliceObject!=null ? sliceObject.clone() : new SliceObject();
 
     	// This ugly code results from the ugly API to the slicing.
     	final int[] start  = new int[dimsDataHolder.size()];
     	final int[] stop   = new int[dimsDataHolder.size()];
     	final int[] step   = new int[dimsDataHolder.size()];
     	IDataset x  = null;
     	IDataset y  = null;
     	final StringBuilder buf = new StringBuilder();
 
      	//buf.append("\n"); // New graphing can deal with long titles.
     	for (int i = 0; i < dimsDataHolder.size(); i++) {
 
     		final DimsData dimsData = dimsDataHolder.getDimsData(i);
     		
     		start[i] = getStart(dimsData);
     		stop[i]  = getStop(dimsData,dataShape[i]);
     		step[i]  = getStep(dimsData);
 
     		if (dimsData.getPlotAxis()<0) {
     			String nexusAxisName = getNexusAxisName(currentSlice, dimsData, " (Dim "+(dimsData.getDimension()+1)); 
     			String formatValue   = String.valueOf(dimsData.getSlice());
     			try {
     			    formatValue = format.format(getNexusAxisValue(sliceObject, dimsData, dimsData.getSlice(), null));
     			} catch (Throwable ne) {
     				formatValue   = String.valueOf(dimsData.getSlice());
     			}
     			buf.append("("+nexusAxisName+" = "+(dimsData.getSliceRange()!=null?dimsData.getSliceRange():formatValue)+")");
     		}
 
     		final IAnalysisService service = (IAnalysisService)ServiceManager.getService(IAnalysisService.class);
     		if (dimsData.getPlotAxis()==0) {
     			x = service.arange(dataShape[i], IAnalysisService.INT);
     			x.setName("Dimension "+(dimsData.getDimension()+1));
     			currentSlice.setX(dimsData.getDimension());
     			
     		}
     		if (dimsData.getPlotAxis()==1) {
        			y = service.arange(dataShape[i], IAnalysisService.INT);
     			y.setName("Dimension "+(dimsData.getDimension()+1));
     			currentSlice.setY(dimsData.getDimension());
     		}
     		
         	if (dimsData.getSliceRange()!=null&&dimsData.getPlotAxis()<0) {
         		currentSlice.setRange(true);
         	}
 
     	}
 
     	if (x==null || x.getSize()<2) { // Nothing to plot
     		logger.debug("Cannot slice into an image because one of the dimensions is size of 1");
     		return null;
     	}
     	
     	if (y!=null) {
     	    currentSlice.setSlicedShape(new int[]{x.getSize(),y.getSize()});
         	currentSlice.setAxes(Arrays.asList(new IDataset[]{x,y}));
     	} else {
     		currentSlice.setSlicedShape(new int[]{x.getSize()});
         	currentSlice.setAxes(Arrays.asList(new IDataset[]{x}));
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
      * @throws Throwable 
      */
 	public static Number getNexusAxisValue(SliceObject sliceObject, DimsData data, int value, IProgressMonitor monitor) throws Throwable {
         IDataset axis = null;
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
 		if (dimsData.getPlotAxis()>-1) {
 			return 0;
 		} else  if (dimsData.getSliceRange()!=null) {
 			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
 			return (int)exp[0];
 		}
 		return dimsData.getSlice();
 	}
 	
 	private static int getStop(DimsData dimsData, final int size) {
 		if (dimsData.getPlotAxis()>-1) {
 			return size;
 		} else  if (dimsData.getSliceRange()!=null) {
 			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
 			return (int)exp[1];
 			
 		}
 		return dimsData.getSlice()+1;
 	}
 
 	private static int getStep(DimsData dimsData) {
 		if (dimsData.getPlotAxis()>-1) {
 			return 1;
 		} else  if (dimsData.getSliceRange()!=null) {
 			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
 			return (int)exp[2];
 			
 		}
 		return 1;
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
 	public static void plotSlice(final ILazyDataset      lazySet,
 			                     final SliceObject       currentSlice,
 			                     final int[]             dataShape,
 			                     final PlotType          type,
 			                     final IPlottingSystem   plottingSystem,
 			                     final IProgressMonitor  monitor) throws Exception {
 
 		if (plottingSystem==null) return;
 		if (monitor!=null) monitor.worked(1);
 		if (monitor!=null&&monitor.isCanceled()) return;
 		
         currentSlice.setFullShape(dataShape);
         
         final IDataset slice;
        final int[] slicedShape = currentSlice.getSlicedShape();
        if (lazySet instanceof IDataset && Arrays.equals(slicedShape, lazySet.getShape())) {
         	slice = (IDataset)lazySet;
         } else {
         	slice = getSlice(lazySet, currentSlice,monitor);
         }
 		if (slice==null) return;
 		
 		// DO NOT CANCEL the monitor now, we have done the hard part the slice.
 		// We may as well plot it or the plot will look very slow.
 		if (monitor!=null) monitor.worked(1);
 		
 		boolean requireScale = plottingSystem.isRescale()
 				               || type!=plottingSystem.getPlotType();
 		
 		if (type==PlotType.XY) {
 			plottingSystem.clear();
 			final IDataset x = getNexusAxis(currentSlice, slice.getShape()[0], currentSlice.getX()+1, true, monitor);
 			plottingSystem.setXFirst(true);
 			plottingSystem.setPlotType(type);
 			plottingSystem.createPlot1D(x, Arrays.asList((IDataset)slice), slice.getName(), monitor);
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					plottingSystem.getSelectedXAxis().setTitle(x.getName());
 					plottingSystem.getSelectedYAxis().setTitle("");
 				}
 			});
 			
 		} else if (type==PlotType.XY_STACKED || type==PlotType.XY_STACKED_3D) {
 			
 			final IDataset xAxis = getNexusAxis(currentSlice, slice.getShape()[0], currentSlice.getX()+1, true, monitor);
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
 			final List<IDataset> ys = new ArrayList<IDataset>(shape[1]);
 			int index = 0;
 			
     		final IAnalysisService service = (IAnalysisService)ServiceManager.getService(IAnalysisService.class);
 
 			for (double[] da : sets) {
 				final IDataset dds = service.createDoubleDataset(da, da.length);
 				dds.setName(String.valueOf(index));
 				ys.add(dds);
 				++index;
 			}
 			plottingSystem.setXFirst(true);
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
 			IDataset y = getNexusAxis(currentSlice, slice.getShape()[0], currentSlice.getX()+1, true, monitor);
 			IDataset x = getNexusAxis(currentSlice, slice.getShape()[1], currentSlice.getY()+1, true, monitor);		
 			
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
 	public static IDataset getNexusAxis(SliceObject currentSlice, int length, int inexusAxis, boolean requireIndicesOnError, final IProgressMonitor  monitor) throws Exception {
 		
 		
 		String axisName = currentSlice.getNexusAxis(inexusAxis);
 		final IAnalysisService service = (IAnalysisService)ServiceManager.getService(IAnalysisService.class);
 		if ("indices".equals(axisName) || axisName==null) {
 			IDataset indices = service.arange(length, IAnalysisService.INT); // Save time
 			indices.setName("");
 			return indices;
 		}
 		
 		if (axisName.endsWith("[Expression]")) {
 			final IDataset set = currentSlice.getExpressionAxis(axisName);
 			return service.convertToAbstractDataset(set);
 		}
 		
 		try {
 			return getNexusAxis(currentSlice, axisName, true, monitor);
 			
 		} catch (Throwable ne) {
 			logger.error("Cannot get nexus axis during slice!", ne);
 			if (requireIndicesOnError) {
 				IDataset indices = service.arange(length, IAnalysisService.INT); // Save time
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
 	 * @param axisName, full path and then optionally a : and the dimension which the axis is for.
 	 * @param requireUnit - if true will get unit but will be slower.
 	 * @param requireIndicesOnError
 	 * @param monitor
 	 * @return
 	 */
 	public static IDataset getNexusAxis(final SliceObject currentSlice, 
 										      String origName, 
 										final boolean requireUnit,
 										final IProgressMonitor  monitor) throws Throwable {
 		
 		int dimension = -1;
 		String axisName = origName;
 		if (axisName.contains(":")) {
 			final String[] sa = axisName.split(":");
 			axisName  = sa[0];
 			dimension = Integer.parseInt(sa[1])-1;
 		}
 		
 		IDataset axis = null;
 		
 		if (axisName.endsWith("[Expression]")) {
 			final IDataset set = currentSlice.getExpressionAxis(axisName);
 			final IAnalysisService service = (IAnalysisService)ServiceManager.getService(IAnalysisService.class);
 			return service.convertToAbstractDataset(set);
 		}
 		
 		if (requireUnit) { // Slower
 			IHierarchicalDataFile file = null;
 			try {
 				file = HierarchicalDataFactory.getReader(currentSlice.getPath());
 				final Group  group    = file.getParent(currentSlice.getName());
 				
 				final String fullName = group.getFullName()+"/"+axisName;
 				
 				final ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
 				axis = service.getDataset(currentSlice.getPath(), fullName, monitor);
 				if (axis == null) return null;
 				axis = axis.squeeze();
 				
 				final String unit = file.getAttributeValue(fullName+"@unit");
 				if (unit!=null) origName = origName+" "+unit;
 			    
 			} finally {
 				if (file!=null) file.close();
 			}
 		} else { // Faster
 			
 			final String dataPath = currentSlice.getName();
 			final File file = new File(dataPath);
 			final String fullName = file.getParent().replace('\\','/')+"/"+axisName;
 			final ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
 			axis = service.getDataset(currentSlice.getPath(), fullName, monitor);
 			if (axis == null) return null;
 			axis = axis.squeeze();
 		
 		}
 
 		// TODO Should really be averaging not using first index.
 		if (dimension>-1) {
 			final int[] shape = axis.getShape();
 			final int[] start = new int[shape.length];
 			final int[] stop  = new int[shape.length];
 			final int[] step  = new int[shape.length];
 			for (int i = 0; i < shape.length; i++) {
 				start[i] = 0;
 				step[i]  = 1;
 				if (i==dimension) {
 					stop[i] = shape[i];
 				} else {
 					stop[i] = 1;
 				}
 			}
 			axis = axis.getSlice(start, stop, step);
 			if (axis == null) return null;
 			axis = axis.squeeze();
 		}
 		
 		axis.setName(origName);
 	    return axis;
 
 	}
 
 
 	public static IDataset getSlice(final ILazyDataset      ld,
 									final SliceObject       currentSlice,
 									final IProgressMonitor  monitor) throws Exception {
 		
 		final int[] dataShape = currentSlice.getFullShape();
 		
 		if (monitor!=null&&monitor.isCanceled()) return null;
 
 		// This is the bit that takes the time. 
 		// *DO NOT CANCEL MONITOR* if we get this far
 		IDataset slice = (IDataset)ld.getSlice(currentSlice.getSliceStart(), currentSlice.getSliceStop(), currentSlice.getSliceStep());
 		slice.setName("Slice of "+currentSlice.getName()+" "+currentSlice.getShapeMessage());
 		
 		final IAnalysisService service = (IAnalysisService)ServiceManager.getService(IAnalysisService.class);
 		if (currentSlice.isRange()) {
 			// We sum the data in the dimensions that are not axes
 			IDataset sum    = slice;
 			final int       len    = dataShape.length;
 			for (int i = len-1; i >= 0; i--) {
 				if (!currentSlice.isAxis(i) && dataShape[i]>1) sum = service.sum(sum, i);
 			}
 
 			if (currentSlice.getX() > currentSlice.getY()) sum = service.transpose(sum);
 			sum.setName(slice.getName());
 			
 			sum = sum.squeeze();
 			slice = sum;
 		} else {
 
 			slice = slice.squeeze();		
 			if (currentSlice.getX() > currentSlice.getY() && slice.getShape().length==2) {
 				// transpose clobbers name
 				final String name = slice.getName();
 				slice = service.transpose(slice);
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
      * @throws Exception 
      */
 	public static List<SliceObject> getExpandedSlices(final int[]  fullShape,
 			                                          final Object dimsDataList) throws Exception {	
 
 		final DimsDataList      ddl = (DimsDataList)dimsDataList;
 		final List<SliceObject> obs = new ArrayList<SliceObject>(89);
 		createExpandedSlices(fullShape, ddl, 0, new ArrayList<DimsData>(ddl.size()), obs);
 		return obs;
 	}
 
 
 	private static void createExpandedSlices(final int[]             fullShape,
 			                                 final DimsDataList      ddl,
 			                                 final int               index,
 			                                 final List<DimsData>    chunk,
 			                                 final List<SliceObject> obs) throws Exception {
 		
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
 	
 	
 	/**
 	 * Deals with loaders which provide data names of size 1
 	 * 
 	 * 
 	 * @param meta
 	 * @return
 	 */
 	public static final Collection<String> getSlicableNames(IDataHolder holder) {
 				
 		Collection<String> names = Arrays.asList(holder.getNames());
 		if (names==null||names.isEmpty()) return null;
 		
 		Collection<String> ret   = new ArrayList<String>(names.size());
 		for (String name : names) {
 			ILazyDataset ls = holder.getLazyDataset(name);
 			int[] shape = ls!=null ? ls.getShape() : null;
 			if (shape==null) continue;
 			
 			boolean foundDims = false;
 			for (int i = 0; i < shape.length; i++) {
 				if (shape[i]>1) {
 					foundDims = true;
 					break;
 				}
 			}
 			if (!foundDims) continue;
 			ret.add(name);
 		}
 		return ret;
 	}
 
 
 }
