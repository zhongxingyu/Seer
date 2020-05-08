 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.gda.extensions.loaders;
 
 import gda.analysis.io.ScanFileHolderException;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ncsa.hdf.object.Dataset;
 
 import org.dawb.common.util.io.FileUtils;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.HierarchicalInfo;
 import org.dawb.hdf5.IHierarchicalDataFile;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IDataSetLoader;
 import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.IMetaLoader;
 import uk.ac.diamond.scisoft.analysis.io.ISliceLoader;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.io.MetaDataAdapter;
 import uk.ac.diamond.scisoft.analysis.io.SliceObject;
 import uk.ac.gda.monitor.IMonitor;
 
 public class H5Loader extends AbstractFileLoader implements IMetaLoader, IDataSetLoader, ISliceLoader {
 
 	private final static List<String> EXT;
 	static {
 		EXT = new ArrayList<String>(7);
 		EXT.add("h5");
 		EXT.add("nxs");
 		EXT.add("hd5");
 		EXT.add("hdf5");
 		EXT.add("hdf");
 		EXT.add("nexus");
 	}
 	/**
 	 * Called to ensure that the loader for h5 and nxs is this one.
 	 * @throws Exception
 	 */
 	public static void setLoaderInFactory() throws Exception {
 		LoaderFactory.getSupportedExtensions();
 		
		/**
		 * Warning DataImportSource assumes LoaderFactory will return
		 * H5LazyDatasets in order to then get full data for the pipeline.
		 */
 		for (String ext : EXT) {
 			LoaderFactory.clearLoader(ext);
 			LoaderFactory.registerLoader(ext, H5Loader.class);
 		}
 	}
 	
 
 	public static boolean isH5(final String filePath) {
 		final String ext = FileUtils.getFileExtension(filePath);
 		return EXT.contains(ext);
 	}
 
 	
 	private String filePath;
 
 	public H5Loader() {
 		
 	}
 	
 	public H5Loader(final String path) {
 		this.filePath = path;
 	}
 	
 	@Override
 	public DataHolder loadFile() throws ScanFileHolderException {
         return this.loadFile(null);
 	}
 	
 	@Override
 	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
 		
 		final DataHolder holder = new DataHolder();
 		IHierarchicalDataFile file = null;
 		try {
 			file = HierarchicalDataFactory.getReader(filePath);
 			if (mon!=null) mon.worked(1);
 			final List<String>     fullPaths = file.getDatasetNames(IHierarchicalDataFile.NUMBER_ARRAY);
 			if (mon!=null) mon.worked(1);
 			final Map<String, ILazyDataset> sets = getSets(file, fullPaths, mon);
 			for (String fullPath : fullPaths) {
 				holder.addDataset(fullPath, sets.get(fullPath));
 				if (mon!=null) mon.worked(1);
 			}
 			return holder;
 			
 		} catch (Exception ne) {
 			throw new ScanFileHolderException(ne.getMessage());
 		} finally {
 			try {
 			    if (file!=null) file.close();
 			} catch (Exception ne) {
 				throw new ScanFileHolderException(ne.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public synchronized AbstractDataset slice(SliceObject bean, IMonitor mon) throws Exception {
 		IHierarchicalDataFile file = null;
 		try {
 			file = HierarchicalDataFactory.getReader(bean.getPath());
 			final Dataset dataset = (Dataset)file.getData(bean.getName());
 			
 			if (dataset.getStartDims()==null) dataset.getMetadata();
   		    long[] start    = dataset.getStartDims(); // the off set of the selection
 			long[] stride   = dataset.getStride(); // the stride of the dataset
 			long[] selected = dataset.getSelectedDims(); // the selected size of the dataet
 			
 			if (mon!=null) mon.worked(1);
 			for (int i = 0; i < selected.length; i++) {
 				start[i] = bean.getSliceStart()[i];
 			}
 			for (int i = 0; i < stride.length; i++) {
 				stride[i] = bean.getSliceStep()[i];
 			}
 			for (int i = 0; i < selected.length; i++) {
 				selected[i] = bean.getSliceStop()[i]-bean.getSliceStart()[i];
 			}
 
 			if (mon!=null) mon.worked(1);
 			final Object    val  = dataset.read();
 			if (mon!=null) mon.worked(1);
 			AbstractDataset aset = H5Utils.getSet(val,selected,dataset);
 			
 			// Reset dims
 			resetDims(dataset);
 			if (mon!=null) mon.worked(1);
 			return aset;
            
 		} finally {
 			if (file!=null) file.close();
 		}
 	}
 
 	protected void resetDims(Dataset dataset) {
 		long[] selected = dataset.getSelectedDims(); // the selected size of the dataet
		long[] dims     = dataset.getDims();
		if (dims==null||selected==null) return;
 		for (int i = 0; i < selected.length; i++) {
 			selected[i] = dims[i];
 		}
 	}
 
 
 	@Override
 	public AbstractDataset loadSet(String path, String fullPath, IMonitor mon) throws Exception {
 		IHierarchicalDataFile file = null;
 		try {
 			file = HierarchicalDataFactory.getReader(path);
 			if (mon!=null) mon.worked(1);
 			final Dataset set = (Dataset)file.getData(fullPath);
 			if (mon!=null) mon.worked(1);
 			final Object  val = set.read();
 			if (mon!=null) mon.worked(1);
 			return H5Utils.getSet(val,set);
 		} finally {
 			if (file!=null) file.close();
 		}
 	}
 
 	@Override
 	public Map<String, ILazyDataset> loadSets(String path, List<String> fullPaths, IMonitor mon) throws Exception {
 		IHierarchicalDataFile file = null;
 		try {
 			if (mon!=null) mon.worked(1);
 			file = HierarchicalDataFactory.getReader(path);
 			return getSets(file, fullPaths, mon);
 		} finally {
 			if (file!=null) file.close();
 		}
 	}
 
 	private Map<String, ILazyDataset> getSets(final IHierarchicalDataFile file, List<String> fullPaths, IMonitor mon) throws Exception {
 		
 		final Map<String, ILazyDataset> ret = new HashMap<String,ILazyDataset>(fullPaths.size());
 		for (String fullPath : fullPaths) {
 			if (mon!=null) mon.worked(1);
 			
 			final Dataset      set = (Dataset)file.getData(fullPath);
 			set.getMetadata();
 			final LazyDataset  lazy   = new H5LazyDataset(set, file.getPath());
 			
 			ret.put(fullPath, lazy);
 		}
 		return ret;
 	}
 
 
 
 	private List<String>         allDataSetNames;
 	private Map<String, Integer> allDataSetSizes;
 	private Map<String, int[]>   allDataSetShapes;
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		
 		IHierarchicalDataFile file = null;
 		try {
 			file = HierarchicalDataFactory.getReader(filePath);
 			if (mon!=null) mon.worked(1);
 			
 			final HierarchicalInfo info = file.getDatasetInformation(IHierarchicalDataFile.NUMBER_ARRAY);
 			if (mon!=null) mon.worked(1);
 			allDataSetNames  = info.getDataSetNames();
 			if (mon!=null) mon.worked(1);
 			allDataSetSizes  = info.getDataSetSizes();
 			if (mon!=null) mon.worked(1);
 			allDataSetShapes = info.getDataSetShapes();
 		} finally {
 			if (file!=null) file.close();
 		}
 		
 	}
 
 	@Override
 	public IMetaData getMetaData() {
 		return new MetaDataAdapter() {
 			
 			@Override
 			public String getMetaValue(String key) {
 				return null; // not implemented as yet
 			}
 			
 			@Override
 			public Collection<String> getDataNames() {
 				return Collections.unmodifiableCollection(allDataSetNames);
 			}
 
 			@Override
 			public Map<String, Integer> getDataSizes() {
 				return allDataSetSizes;
 			}
 
 			@Override
 			public Map<String, int[]> getDataShapes() {
 				return allDataSetShapes;
 			}
 		};
 	}
 
 }
