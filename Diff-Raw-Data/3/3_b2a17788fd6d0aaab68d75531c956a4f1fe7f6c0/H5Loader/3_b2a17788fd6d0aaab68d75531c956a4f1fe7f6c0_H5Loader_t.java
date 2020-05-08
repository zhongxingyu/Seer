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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ncsa.hdf.object.Dataset;
 
 import org.dawb.common.util.io.FileUtils;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.HierarchicalDataUtils;
 import org.dawb.hdf5.HierarchicalInfo;
 import org.dawb.hdf5.IHierarchicalDataFile;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IDataSetLoader;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.IMetaLoader;
 import uk.ac.diamond.scisoft.analysis.io.MetaDataAdapter;
 import uk.ac.diamond.scisoft.analysis.io.SliceObject;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public class H5Loader extends AbstractFileLoader implements IMetaLoader, IDataSetLoader {
 
 	public final static List<String> EXT;
 	static {
 		List<String> tmp = new ArrayList<String>(7);
 		tmp.add("h5");
 		tmp.add("nxs");
 		tmp.add("hd5");
 		tmp.add("hdf5");
 		tmp.add("hdf");
 		tmp.add("nexus");
 		EXT = Collections.unmodifiableList(tmp);
 	}	
 
 	public static boolean isH5(final String filePath) {
 		final String ext = FileUtils.getFileExtension(filePath);
		if (ext==null) return false;
		return EXT.contains(ext.toLowerCase());
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
 
 			if (loadMetadata) {
 				metaInfo = file.getDatasetInformation(IHierarchicalDataFile.NUMBER_ARRAY);
 				holder.setMetadata(getMetaData());
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
 
 	protected synchronized AbstractDataset slice(SliceObject bean, IMonitor mon) throws Exception {
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
 			final Object    val  = dataset.read(); // Appears in stack traces of VM exists
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
 			resetDims(set);
 			final Object  val = set.read(); // Dangerous if data large!
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
 
 
 
 	private HierarchicalInfo metaInfo;
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		
 		IHierarchicalDataFile file = null;
 		try {
 			file = HierarchicalDataFactory.getReader(filePath);
 			if (mon!=null) mon.worked(1);
 			
 			metaInfo = file.getDatasetInformation(IHierarchicalDataFile.NUMBER_ARRAY);
 		} finally {
 			if (file!=null) file.close();
 		}
 		
 	}
 
 	@Override
 	public IMetaData getMetaData() {
 		
  		return new MetaDataAdapter() {
 			private static final long serialVersionUID = MetaDataAdapter.serialVersionUID;
 			private Map<String, Object> attributeValues;
 			
 			@Override
 			public Collection<String> getMetaNames() throws Exception{
 				/**
 				 * We lazy load the meta data attributes as it's not always needed.
 				 */
 				if (attributeValues==null) readAttributes();
 				return Collections.unmodifiableCollection(attributeValues.keySet());
 			}
 
 			@Override
 			public String getMetaValue(String fullAttributeKey) throws Exception{
 				/**
 				 * We lazy load the meta data attributes as it's not always needed.
 				 */
 				if (attributeValues==null) readAttributes();
 				return HierarchicalDataUtils.extractValue(attributeValues.get(fullAttributeKey));
 			}
 			
 			@Override
 			public Collection<String> getDataNames() {
 				return Collections.unmodifiableCollection(metaInfo.getDataSetNames());
 			}
 
 			@Override
 			public Map<String, Integer> getDataSizes() {
 				return Collections.unmodifiableMap(metaInfo.getDataSetSizes());
 			}
 
 			@Override
 			public Map<String, int[]> getDataShapes() {
 				return Collections.unmodifiableMap(metaInfo.getDataSetShapes());
 			}
 			
 			private void readAttributes() throws Exception {
 				if (attributeValues==null) {
 					IHierarchicalDataFile file = null;
 					try {
 						file = HierarchicalDataFactory.getReader(filePath);
 						
 						attributeValues = file.getAttributeValues();
 					} finally {
 						if (file!=null) file.close();
 					}
 				
 				}
 			}
 		};
 	}
 
 }
