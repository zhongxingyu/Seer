 /*
  * Copyright (c) 2012 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawnsci.persistence.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.HObject;
 
 import org.dawb.common.services.IPersistentFile;
 import org.dawnsci.io.h5.H5LazyDataset;
 import org.dawnsci.persistence.json.IJSonMarshaller;
 import org.dawnsci.persistence.json.JacksonMarshaller;
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
 import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
 import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
 import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
 import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
 import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
 import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
 import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
 import org.eclipse.dawnsci.analysis.api.processing.IOperation;
 import org.eclipse.dawnsci.analysis.api.processing.OperationData;
 import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
 import org.eclipse.dawnsci.analysis.api.roi.IROI;
 import org.eclipse.dawnsci.analysis.dataset.impl.BooleanDataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.Comparisons;
 import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
 import org.eclipse.dawnsci.hdf5.H5Utils;
 import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
 import org.eclipse.dawnsci.hdf5.HierarchicalDataFileUtils;
 import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
 import org.eclipse.dawnsci.hdf5.Nexus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionMetaReader;
 
 /**
  * Implementation of IPersistentFile<br>
  * 
  * This class is internal and not supposed to be used out of this bundle.
  * 
  * @author wqk87977
  *
  */
 class PersistentFileImpl implements IPersistentFile {
 	
 	private static final Logger logger = LoggerFactory.getLogger(PersistentFileImpl.class);
 	private IHierarchicalDataFile file;
 	private String filePath;
 
 	/**
 	 * For save
 	 * @param file
 	 */
 	public PersistentFileImpl(IHierarchicalDataFile file) throws Exception{
 		this.file = file;
 		this.filePath = file.getPath();
 
 		String currentSite = System.getenv("DAWN_SITE");
 		if (currentSite==null || "".equals(currentSite)) {
 			logger.debug("DAWN_SITE is not set, persistence layer defaulting to DLS in file meta information.");
 			currentSite = "Diamond Light Source";
 		}
 		setSite(currentSite);
 		setVersion(PersistenceConstants.CURRENT_VERSION);
 	}
 
 	/**
 	 * For read
 	 * @param filePath
 	 */
 	public PersistentFileImpl(String filePath) {
 		this.filePath = filePath;
 		try {
 			this.file = HierarchicalDataFactory.getReader(filePath);
 		} catch (Exception e) {
 			logger.error("Error getting H5 Reader:" + e);
 		}
 	}
 
 	@Override
 	public void setMasks(Map<String, ? extends IDataset> masks) throws Exception {
 		if(file == null)
 			file = HierarchicalDataFactory.getWriter(filePath);
 
 		createParentEntry(PersistenceConstants.MASK_ENTRY);
 
 		if (masks != null) {
 			Set<String> names = masks.keySet();
 
 			Iterator<String> it = names.iterator();
 			while(it.hasNext()){
 				String name = it.next();
 				BooleanDataset bd = (BooleanDataset)masks.get(name);
 				// Inverse the dataset
 				bd = Comparisons.logicalNot(bd);
 
 				Dataset id = DatasetUtils.cast(bd, Dataset.INT8);
 
 				final String dataset = file.replaceDataset(name, id, PersistenceConstants.MASK_ENTRY);
 				file.setNexusAttribute(dataset, Nexus.SDS);
 			}
 		}
 	}
 
 	@Override
 	public void addMask(String name, IDataset mask, IMonitor mon) throws Exception{
 		// Inverse the dataset
 		mask = Comparisons.logicalNot((BooleanDataset)mask);
 		Dataset id = DatasetUtils.cast((BooleanDataset)mask, Dataset.INT8);
 		//check if parent group exists
 		Object parentObj = file.getData(PersistenceConstants.MASK_ENTRY);
 		if(parentObj == null) {
 			createParentEntry(PersistenceConstants.MASK_ENTRY);
 		} 
 
 		final String dataset = file.replaceDataset(name, id, PersistenceConstants.MASK_ENTRY);
 		file.setNexusAttribute(dataset, Nexus.SDS);
 	}
 
 	@Override
 	public void setData(IDataset data) throws Exception {
 		writeH5Data(data, null, null);
 	}
 	
 	@Override
 	public void setHistory(IDataset... sets) throws Exception {
 		if(file == null)
 			file = HierarchicalDataFactory.getWriter(filePath);
 		
 		createParentEntry(PersistenceConstants.HISTORY_ENTRY);
 		int index = 0;
 		for (IDataset data : sets) {
 			index++;
 			if(data != null) {
 				String dataName = !data.getName().equals("") ? data.getName() : "history"+index;
 				final String dataset = file.replaceDataset(dataName,  data, PersistenceConstants.HISTORY_ENTRY);
 				file.setNexusAttribute(dataset, Nexus.SDS);
 			}
 		}
 	}
 
 	@Override
 	public void setAxes(List<? extends IDataset> axes) throws Exception{
 		writeH5Data(null, axes.get(0), axes.get(1));
 	}
 
 	@Override
 	public void setROIs(Map<String, IROI> rois) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 
 		createParentEntry(PersistenceConstants.ROI_ENTRY);
 		if (rois != null) {
 			Iterator<String> it = rois.keySet().iterator();
 			while(it.hasNext()){
 				String name = it.next();
 				IROI roi = rois.get(name);
 				writeRoi(file, PersistenceConstants.ROI_ENTRY, name, roi);
 			}
 		}
 	}
 
 	@Override
 	public void addROI(String name, IROI roiBase) throws Exception {
 		if (file == null)
 			file = HierarchicalDataFactory.getWriter(filePath);
 		createParentEntry(PersistenceConstants.ROI_ENTRY);
 		writeRoi(file, PersistenceConstants.ROI_ENTRY, name, roiBase);
 	}
 
 	@Override
 	public void setRegionAttribute(String regionName, String attributeName, String attributeValue) throws Exception {
 		if ("JSON".equals(attributeName)) throw new Exception("Cannot override the JSON attribute!");
 		final HObject node = (HObject)file.getData(PersistenceConstants.ROI_ENTRY+"/"+regionName);
 		file.setAttribute(node.getFullName(), attributeName, attributeValue);
 	}
 
 	@Override
 	public String getRegionAttribute(String regionName, String attributeName) throws Exception{
 		return file.getAttributeValue(PersistenceConstants.ROI_ENTRY+"/"+regionName+"@"+attributeName);
 	}
 
 	/**
 	 * Used to set the version of the API
 	 * @param version
 	 * @throws Exception
 	 */
 	private void setVersion(String version) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		//check if parent group exists
 		Object parent = file.getData(PersistenceConstants.ENTRY);
 		if (parent == null) createParentEntry(PersistenceConstants.ENTRY);
 		file.setAttribute(PersistenceConstants.ENTRY, "Version", version);
 	}
 
 	@Override
 	public void setSite(String site) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		//check if parent group exists
 		Object parent = file.getData(PersistenceConstants.ENTRY);
 		if(parent == null) createParentEntry(PersistenceConstants.ENTRY);
 		file.setAttribute(PersistenceConstants.ENTRY, "Site", site);
 	}
 
 	@Override
 	public ILazyDataset getData(String dataName, IMonitor mon) throws Exception{
 		if (file == null)
 			file = HierarchicalDataFactory.getReader(filePath);
 		dataName = !dataName.equals("") ? dataName : "data";
 		ncsa.hdf.object.Dataset set = (ncsa.hdf.object.Dataset)file.getData(PersistenceConstants.DATA_ENTRY+"/"+dataName);
 		set.getMetadata();
 		return new H5LazyDataset(set);
 	}
 
 	/**
 	 * Method to set datasets which persist history
 	 * 
 	 * @param data
 	 * @throws Exception 
 	 */
 	@Override
 	public Map<String, ILazyDataset> getHistory(IMonitor mon) throws Exception {
 		IDataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		Map<String, ILazyDataset> sets = new HashMap<String, ILazyDataset>(dh.size());
 		for (String name : dh.getNames()) {
 			if (name.startsWith(PersistenceConstants.HISTORY_ENTRY)) {
 				sets.put(name, dh.getLazyDataset(name));
 			}
 		}
 		return sets;
 	}
 
 	@Override
 	public List<ILazyDataset> getAxes(String xAxisName, String yAxisName, IMonitor mon) throws Exception {
 		List<ILazyDataset> axes = new ArrayList<ILazyDataset>();
 		ILazyDataset xaxis = null, yaxis = null;
 
 		IDataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		xAxisName = !xAxisName.equals("") ? xAxisName : "X Axis";
 		xaxis = readH5Data(dh, xAxisName, PersistenceConstants.DATA_ENTRY);
 		if(xaxis != null)
 			axes.add(xaxis);
 		yAxisName = !yAxisName.equals("") ? yAxisName : "Y Axis";
 		yaxis = readH5Data(dh, yAxisName, PersistenceConstants.DATA_ENTRY);
 		if(yaxis != null)
 			axes.add(yaxis);
 		return axes;
 	}
 
 	@Override
 	public BooleanDataset getMask(String maskName, IMonitor mon) throws Exception {
 		if(file == null)
 			file = HierarchicalDataFactory.getReader(filePath);
 		ncsa.hdf.object.Dataset data = (ncsa.hdf.object.Dataset)file.getData(PersistenceConstants.MASK_ENTRY+"/"+maskName);
		if (data == null)
			throw new Exception("The mask with the name " + maskName + " is null");
 		Object val = data.read();
 		Dataset ret =  H5Utils.getSet(val,data);
 		BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(ret, Dataset.BOOL);
 		if (getVersionNumber() > 1) {
 			// Inverse the dataset
 			bd = Comparisons.logicalNot(bd);
 		}
 		return bd;
 	}
 
 	@Override
 	public Map<String, IDataset> getMasks(IMonitor mon) throws Exception {
 		if(file == null)
 			file = HierarchicalDataFactory.getReader(filePath);
 		Map<String, IDataset> masks = new HashMap<String, IDataset>();
 		List<String> names = getMaskNames(mon);
 		Iterator<String> it = names.iterator();
 		while (it.hasNext()) {
 			String name = (String) it.next();
 			ncsa.hdf.object.Dataset data = (ncsa.hdf.object.Dataset)file.getData(PersistenceConstants.MASK_ENTRY+"/"+name);
 			Object val = data.read();
 			Dataset ret =  H5Utils.getSet(val,data);
 			BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(ret, Dataset.BOOL);
 			if (getVersionNumber() > 1) {
 				// Inverse the dataset
 				bd = Comparisons.logicalNot(bd);
 			}
 			masks.put(name, bd);
 		}
 		return masks;
 	}
 
 	private Double getVersionNumber() throws Exception {
 		String version = getVersion();
 		if (version == null) throw new Exception("No version number could be found in the file");
 		String str = version.replace("[", "").replace("]", "");
 		return Double.parseDouble(str);
 	}
 
 	@Override
 	public IROI getROI(String roiName) throws Exception {
 		IJSonMarshaller converter = new JacksonMarshaller();
 		String json = file.getAttributeValue(PersistenceConstants.ROI_ENTRY+"/"+roiName+"@JSON");
 		if(json == null) throw new Exception("Reading Exception: " +PersistenceConstants.ROI_ENTRY+ " entry does not exist in the file " + filePath);
 		// JSON deserialization
 		json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
 		IROI roi = (IROI)converter.unmarshal(json);
 		return roi;
 	}
 
 	@Override
 	public Map<String, IROI> getROIs(IMonitor mon) throws Exception {
 		Map<String, IROI> rois = new HashMap<String, IROI>();
 		if(file == null)
 			file = HierarchicalDataFactory.getReader(filePath);
 		IJSonMarshaller converter = new JacksonMarshaller();
 		List<String> names = getROINames(mon);
 		if (names== null) return null;
 		Iterator<String> it = names.iterator();
 		while (it.hasNext()) {
 			String name = (String) it.next();
 			String json = file.getAttributeValue(PersistenceConstants.ROI_ENTRY+"/"+name+"@JSON");
 			json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
 			IROI roi = (IROI) converter.unmarshal(json);
 			rois.put(name, roi);
 		}
 		return rois;
 	}
 
 	@Override
 	public List<String> getDataNames(IMonitor mon) throws Exception{
 		return getNames(PersistenceConstants.DATA_ENTRY, mon);
 	}
 
 	@Override
 	public List<String> getMaskNames(IMonitor mon) throws Exception{
 		return getNames(PersistenceConstants.MASK_ENTRY, mon);
 	}
 
 	@Override
 	public List<String> getROINames(IMonitor mon) throws Exception{
 		return getNames(PersistenceConstants.ROI_ENTRY, mon);
 	}
 
 	private List<String> getNames(String path, IMonitor mon) throws Exception {
 		List<String> names = null;
 		IHierarchicalDataFile f = null;
 		try {
 			f = HierarchicalDataFactory.getReader(getFilePath());
 			Group grp = (Group)f.getData(path);
 			if (grp==null) throw new Exception("Reading Exception: " + path + " entry does not exist in the file " + filePath);
 			List<HObject> children =  grp.getMemberList();
 			if (names==null) names = new ArrayList<String>(children.size());
 			for (HObject hObject : children) {
 				names.add(hObject.getName());
 			}
 		} finally {
 			if (f!=null) f.close();
 		}
 		return names;
 	}
 
 	@Override
 	public void setDiffractionMetadata(IDiffractionMetadata metadata) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 
 		String parent = HierarchicalDataFileUtils.createParentEntry(file, PersistenceConstants.DIFFRACTIONMETADATA_ENTRY,Nexus.INST);
 		parent = file.group("detector", parent);
 		file.setNexusAttribute(parent, Nexus.DETECT);
 
 		DetectorProperties detprop = metadata.getDetector2DProperties();
 		PersistDiffractionMetadataUtils.writeDetectorProperties(file, parent, detprop);
 		DiffractionCrystalEnvironment crysenv = metadata.getDiffractionCrystalEnvironment();
 		PersistDiffractionMetadataUtils.writeWavelengthMono(file, file.getParent(parent), crysenv.getWavelength());
 
 	}
 
 	@Override
 	public IDiffractionMetadata getDiffractionMetadata(IMonitor mon) throws Exception {
 		//Reverse of the setMetadata.  Would be nice in the future to be able to use the
 		//LoaderFactory but work needs to be done on loading specific metadata from nexus
 		//files first
 		NexusDiffractionMetaReader nexusDiffReader = new NexusDiffractionMetaReader(filePath);
 		return nexusDiffReader.getDiffractionMetadataFromNexus(null);	
 	}
 
 	@Override
 	public String getVersion() throws Exception {
 		return file.getAttributeValue(PersistenceConstants.ENTRY+"@Version");
 	}
 
 	@Override
 	public String getSite() throws Exception {
 		return file.getAttributeValue(PersistenceConstants.ENTRY+"@Site");
 	}
 	
 	@Override
 	public boolean containsData() {
 		return isEntry(PersistenceConstants.DATA_ENTRY,null);
 	}
 
 	@Override
 	public boolean containsMask() {
 		return isEntry(PersistenceConstants.MASK_ENTRY,null);
 	}
 
 	@Override
 	public boolean containsRegion() {
 		return isEntry(PersistenceConstants.ROI_ENTRY,null);
 	}
 
 	@Override
 	public boolean containsDiffractionMetadata() {
 		return isEntry(PersistenceConstants.DIFFRACTIONMETADATA_ENTRY,null);
 	}
 
 	@Override
 	public boolean containsFunction() {
 		return isEntry(PersistenceConstants.FUNCTION_ENTRY,null);
 	}
 
 	private boolean isEntry(String entryPath, IMonitor mon)  {
 		HObject hOb = null;
 		try {
 			hOb = (HObject)file.getData(entryPath);
 			return hOb != null;
 		} catch (Exception e) {
 			logger.debug("Error while reading file: "+ e);
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Method to write image data (and axis) to an HDF5 file given a specific path entry to save the data.
 	 * 
 	 * @param data
 	 * @param xAxisData
 	 * @param yAxisData
 	 * @throws Exception
 	 */
 	private void writeH5Data(final IDataset data, 
 							final IDataset xAxisData, 
 							final IDataset yAxisData) throws Exception {
 
 		if(file == null)
 			file = HierarchicalDataFactory.getWriter(filePath);
 
 		createParentEntry(PersistenceConstants.DATA_ENTRY);
 
 		if(data != null){
 
 			String dataName = !data.getName().equals("") ? data.getName() : "data";
 
 			final String dataset = file.replaceDataset(dataName,  data, PersistenceConstants.DATA_ENTRY);
 			file.setNexusAttribute(dataset, Nexus.SDS);
 			file.setIntAttribute(dataset, "signal", 1);
 		}
 		if(xAxisData != null){
 			String xAxisName = !xAxisData.getName().equals("") ? xAxisData.getName() : "X Axis";
 	
 			final String xDataset = file.replaceDataset(xAxisName,  xAxisData, PersistenceConstants.DATA_ENTRY);
 			file.setNexusAttribute(xDataset, Nexus.SDS);
 			file.setIntAttribute(xDataset, "axis", 1);
 		}
 
 		if(yAxisData != null){
 			String yAxisName = !yAxisData.getName().equals("") ? yAxisData.getName() : "Y Axis";
 
 			final String yDataset = file.replaceDataset(yAxisName,  yAxisData, PersistenceConstants.DATA_ENTRY);
 			file.setNexusAttribute(yDataset, Nexus.SDS);
 			file.setIntAttribute(yDataset, "axis", 2);
 		}
 	}
 
 	/**
 	 * Method to write rois data to an HDF5 file given a specific path entry to save the data.<br>
 	 * The rois are serialised using GSON and are saved as JSON format in the HDF5 file.
 	 * 
 	 * @param rois
 	 * @throws Exception
 	 */
 	private String writeRoi(IHierarchicalDataFile file, 
 								String  parent,
 								String  name,
 								IROI    roi) throws Exception {
 		long[] dims = {1};
 		IJSonMarshaller converter = new JacksonMarshaller();
 		String json = converter.marshal(roi);
 		// we create the dataset
 		String dat = file.replaceDataset(name, Dataset.INT32, dims, new int[]{0}, parent);
 		// we set the JSON attribute
 		file.setAttribute(dat, "JSON", json);
 		return dat;
 	}
 
 	private void createParentEntry(String fullEntry) throws Exception {
 		HierarchicalDataFileUtils.createParentEntry(file, fullEntry, Nexus.DATA);
 	}
 
 	/**
 	 * Method to read image data (axes, masks, image) from an HDF5 file and returns an ILazyDataset
 
 	 * @return ILazyDataset
 	 * @throws Exception 
 	 */
 	private ILazyDataset readH5Data(IDataHolder dh, String dataName, String dataEntry) throws Exception{
 		ILazyDataset ld = dh.getLazyDataset(dataEntry+"/"+dataName);
 		if(ld == null) throw new Exception("Reading Exception: " +dataEntry+ " entry does not exist in the file " + filePath);
 		return ld;
 	}
 
 	/**
 	 * Method to read mask data from an HDF5 file
 	 * @return BooleanDataset
 	 * @throws Exception 
 	 */
 	@SuppressWarnings("unused")
 	private BooleanDataset readH5Mask(IDataHolder dh, String maskName) throws Exception{
 		ILazyDataset ld = dh.getLazyDataset(PersistenceConstants.MASK_ENTRY+"/"+maskName);
 		if (ld instanceof H5LazyDataset) {
 			return (BooleanDataset)DatasetUtils.cast(((H5LazyDataset)ld).getCompleteData(null), Dataset.BOOL);
 		} else {
 			return (BooleanDataset)DatasetUtils.cast(dh.getDataset(PersistenceConstants.MASK_ENTRY+"/"+maskName), Dataset.BOOL);
 		}
 	}
 
 	@Override
 	public void close() {
 		try {
 			if (file != null) {
 				file.close();
 			}
 		} catch (Exception e) {
 			logger.debug("Cannot close " + filePath, e);
 		}
 	}
 
 	public IHierarchicalDataFile getFile(){
 		return file;
 	}
 
 	public String getFilePath(){
 		return filePath;
 	}
 
 	@Override
 	public boolean isRegionSupported(IROI roi) {
 		return JacksonMarshaller.isROISupported(roi);
 	}
 
 	@Override
 	public void setFunctions(Map<String, IFunction> functions) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		createParentEntry(PersistenceConstants.FUNCTION_ENTRY);
 		if (functions != null) {
 			IJSonMarshaller converter = new JacksonMarshaller();
 			Iterator<String> it = functions.keySet().iterator();
 			while(it.hasNext()){
 				String name = it.next();
 				IFunction function = functions.get(name);
 				writeFunction(file, PersistenceConstants.FUNCTION_ENTRY, name, function, converter);
 			}
 		}
 	}
 
 	@Override
 	public void addFunction(String name, IFunction function) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		createParentEntry(PersistenceConstants.FUNCTION_ENTRY);
 		IJSonMarshaller converter = new JacksonMarshaller();
 		writeFunction(file, PersistenceConstants.FUNCTION_ENTRY, name, function, converter);
 	}
 
 	@Override
 	public IFunction getFunction(String functionName) throws Exception {
 		String json = file.getAttributeValue(PersistenceConstants.FUNCTION_ENTRY+"/"+functionName);
 		if(json == null) throw new Exception("Reading Exception: " +PersistenceConstants.FUNCTION_ENTRY+ " entry does not exist in the file " + filePath);
 		IJSonMarshaller converter = new JacksonMarshaller();
 		//Deserialize the json back to a function
 		IFunction function = (IFunction) converter.unmarshal(json);
 		return function;
 	}
 
 	@Override
 	public Map<String, IFunction> getFunctions(IMonitor mon) throws Exception {
 		Map<String, IFunction> functions = new HashMap<String, IFunction>();
 		if(file == null)
 			file = HierarchicalDataFactory.getReader(filePath);
 		IJSonMarshaller converter = new JacksonMarshaller();
 		List<String> names = getFunctionNames(mon);
 		Iterator<String> it = names.iterator();
 		while (it.hasNext()) {
 			String name = (String) it.next();
 			String json = file.getAttributeValue(PersistenceConstants.FUNCTION_ENTRY+"/"+name+"@JSON");
 			json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
 			//Deserialize the json back to a function
 			IFunction function = (IFunction) converter.unmarshal(json);
 			functions.put(name, function);
 		}
 		return functions;
 	}
 
 	@Override
 	public List<String> getFunctionNames(IMonitor mon) throws Exception {
 		List<String> names = null;
 		IHierarchicalDataFile file = null;
 		try {
 			file      = HierarchicalDataFactory.getReader(getFilePath());
 			Group grp = (Group)file.getData(PersistenceConstants.FUNCTION_ENTRY);
 			if (grp==null) throw new Exception("Reading Exception: " +PersistenceConstants.FUNCTION_ENTRY+ " entry does not exist in the file " + filePath);
 			List<HObject> children =  grp.getMemberList();
 			if (names==null) names = new ArrayList<String>(children.size());
 			for (HObject hObject : children) {
 				names.add(hObject.getName());
 			}
 		} finally {
 			if (file!=null) file.close();
 		}
 		return names;
 	}
 
 	private String writeFunction(IHierarchicalDataFile file, String parent,
 			                     String name, IFunction function, IJSonMarshaller converter) throws Exception {
 		long[] dims = {1};
 		String json = converter.marshal(function);
 		// we create the dataset
 		String dat = file.replaceDataset(name, Dataset.INT32, dims, new int[]{0}, parent);
 		// we set the JSON attribute
 		file.setAttribute(dat, "JSON", json);
 		return dat;
 	}
 	
 	public void setPowderCalibrationInformation(IDataset calibrationImage,
 			IDiffractionMetadata metadata, IPowderCalibrationInfo info) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		
 		PersistSinglePowderCalibration.writeCalibrationToFile(file, calibrationImage, metadata, info);
 		
 	}
 	
 	public void setOperations(IOperation<? extends IOperationModel, ? extends OperationData>[] operations) throws Exception  {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		PersistJsonOperationHelper helper = new PersistJsonOperationHelper();
 		helper.writeOperations(file, operations);
 	}
 	
 	public IOperation<? extends IOperationModel, ? extends OperationData>[] getOperations() throws Exception  {
 		if (file == null) file = HierarchicalDataFactory.getReader(filePath);
 		PersistJsonOperationHelper helper = new PersistJsonOperationHelper();
 		return helper.readOperations(file);
 	}
 }
