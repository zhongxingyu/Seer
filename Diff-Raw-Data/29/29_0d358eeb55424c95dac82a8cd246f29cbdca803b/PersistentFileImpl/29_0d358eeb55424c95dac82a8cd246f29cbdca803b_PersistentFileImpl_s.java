 package org.dawnsci.persistence.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.vecmath.Matrix3d;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5Datatype;
 
 import org.dawb.common.services.IPersistentFile;
 import org.dawb.gda.extensions.loaders.H5Utils;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.Nexus;
 import org.dawb.hdf5.nexus.NexusUtils;
 import org.dawnsci.persistence.roi.ROIBean;
 import org.dawnsci.persistence.roi.ROIBeanConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /**
  * Implementation of IPersistentFile<br>
  * 
  * This class is internal and not supposed to be used out of this bundle.
  * 
  * @author wqk87977
  *
  */
 class PersistentFileImpl implements IPersistentFile{
 	private static final Logger logger = LoggerFactory.getLogger(PersistentFileImpl.class);
 	private IHierarchicalDataFile file;
 	private String filePath;
 	private final String ENTRY = "/entry";
 	private final String DATA_ENTRY = "/entry/data";
 	private final String MASK_ENTRY = "/entry/mask";
 	private final String ROI_ENTRY = "/entry/region";
	private final String DIFFRACTIONMETADATA_ENTRY = "/entry/diffractionmetadata";
 
 	/**
 	 * Version of the API
 	 */
 	private final String VERSION = "1.0";
 	/**
 	 * Site where the API is used
 	 */
 	private final String SITE = "Diamond Light Source";
 
 	/**
 	 * For save
 	 * @param file
 	 */
 	PersistentFileImpl(IHierarchicalDataFile file) throws Exception{
 		this.file = file;
 		this.filePath = file.getPath();
 		setSite(SITE);
 		setVersion(VERSION);
 	}
 
 	/**
 	 * For read
 	 * @param filePath
 	 */
 	PersistentFileImpl(String filePath) {
 		this.filePath = filePath;
 		try {
 			this.file = HierarchicalDataFactory.getReader(filePath);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setMasks(Map<String, BooleanDataset> masks) throws Exception {
 		writeH5Mask(masks);
 	}
 
 	@Override
 	public void addMask(String name, BooleanDataset mask, IMonitor mon) throws Exception{
 		
 		AbstractDataset id = DatasetUtils.cast(mask, AbstractDataset.INT8);
 		//check if parent group exists
 		Group parent = (Group)file.getData(MASK_ENTRY);
 		if(parent == null) parent = createParentEntry(MASK_ENTRY);
 		final Datatype datatype = H5Utils.getDatatype(id);
 		final long[] shape = new long[id.getShape().length];
 		
 		for (int i = 0; i < shape.length; i++) shape[i] = id.getShape()[i];
 		final Dataset dataset = file.replaceDataset(name, datatype, shape, id.getBuffer(), parent);
 		file.setNexusAttribute(dataset, Nexus.SDS);
 	}
 
 	@Override
 	public void setData(AbstractDataset data) throws Exception {
 		writeH5Data(data, null, null);
 	}
 
 	@Override
 	public void setAxes(List<AbstractDataset> axes) throws Exception{
 		writeH5Data(null, axes.get(0), axes.get(1));
 	}
 
 	@Override
 	public void setROIs(Map<String, ROIBase> rois) throws Exception {
 		writeH5Rois(rois);
 	}
 
 	@Override
 	public void addROI(String name, ROIBase roiBase, String roiType) throws Exception {
 		
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		
 		Group parent = createParentEntry(ROI_ENTRY);
 		
 		// JSON serialisation
 		GsonBuilder builder = new GsonBuilder();
 		//TODO: serialiser to be worked on...
 		//builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
 		Gson gson = builder.create();
 	
 		HObject dataset = writeRoi(file, parent, name, roiBase, gson);
 		if (dataset!=null) {
 			file.setAttribute(dataset, "Region Type", roiType);
 		}
 	}
 
 	/**
 	 * Used to set the version of the API
 	 * @param version
 	 * @throws Exception
 	 */
 	private void setVersion(String version) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		//check if parent group exists
 		Group parent = (Group)file.getData(ENTRY);
 		if(parent == null) parent = createParentEntry(ENTRY);
 		file.setAttribute(parent, "Version", version);
 	}
 
 	@Override
 	public void setSite(String site) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		//check if parent group exists
 		Group parent = (Group)file.getData(ENTRY);
 		if(parent == null) parent = createParentEntry(ENTRY);
 		file.setAttribute(parent, "Site", site);
 	}
 
 	@Override
 	public ILazyDataset getData(String dataName, IMonitor mon) throws Exception{
 		ILazyDataset data = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		dataName = !dataName.equals("") ? dataName : "data";
 		data = readH5Data(dh, dataName, DATA_ENTRY);
 		
 		return data;
 	}
 
 	@Override
 	public List<ILazyDataset> getAxes(String xAxisName, String yAxisName, IMonitor mon) throws Exception {
 		List<ILazyDataset> axes = new ArrayList<ILazyDataset>();
 		ILazyDataset xaxis = null, yaxis = null;
 
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		xAxisName = !xAxisName.equals("") ? xAxisName : "X Axis";
 		xaxis = readH5Data(dh, xAxisName, DATA_ENTRY);
 		if(xaxis != null)
 			axes.add(xaxis);
 		yAxisName = !yAxisName.equals("") ? yAxisName : "Y Axis";
 		yaxis = readH5Data(dh, yAxisName, DATA_ENTRY);
 		if(yaxis != null)
 			axes.add(yaxis);
 		
 		return axes;
 	}
 
 	@Override
 	public BooleanDataset getMask(String maskName, IMonitor mon) throws Exception {
 		BooleanDataset mask = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		mask = readH5Mask(dh, maskName);
 		return mask;
 	}
 
 	@Override
 	public Map<String, BooleanDataset> getMasks(IMonitor mon) throws Exception {
 		Map<String, BooleanDataset> masks = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		masks = readH5Masks(dh, mon);
 		return masks;
 	}
 
 	@Override
 	public ROIBase getROI(String roiName) throws Exception {
 		ROIBase roi = null;
 		roi = readH5ROI(roiName);
 	
 		return roi;
 	}
 
 	@Override
 	public Map<String, ROIBase> getROIs(IMonitor mon) throws Exception {
 		Map<String, ROIBase> rois = null;
 		rois = readH5ROIs(mon);
 		
 		return rois;
 	}
 
 	@Override
 	public List<String> getDataNames(IMonitor mon) throws Exception{
 		List<String> names = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		names = getNames(dh, DATA_ENTRY);
 		
 		return names;
 	}
 
 	@Override
 	public List<String> getMaskNames(IMonitor mon) throws Exception{
 		List<String> names = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		names = getNames(dh, MASK_ENTRY);
 		return names;
 	}
 
 	@Override
 	public List<String> getROINames(IMonitor mon) throws Exception{
 		List<String> names = null;
 		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
 		names = getNames(dh, ROI_ENTRY);
 		
 		return names;
 	}
 	
 	@Override
 	public void setDiffractionMetadata(IDiffractionMetadata metadata) throws Exception {
 		writeH5DiffractionMetadata(metadata);		
 	}
 
 	@Override
 	public String getVersion() throws Exception {
 		return file.getAttributeValue(ENTRY+"@Version");
 	}
 
 	@Override
 	public String getSite() throws Exception {
 		return file.getAttributeValue(ENTRY+"@Site");
 	}
 
 	/**
 	 * Method to write image data (and axis) to an HDF5 file given a specific path entry to save the data.
 	 * 
 	 * @param data
 	 * @param xAxisData
 	 * @param yAxisData
 	 * @throws Exception
 	 */
 	private void writeH5Data(final AbstractDataset data, 
 							 final AbstractDataset xAxisData, 
 							 final AbstractDataset yAxisData) throws Exception {
 
 		if(file == null)
 			file = HierarchicalDataFactory.getWriter(filePath);
 		
 		Group parent = createParentEntry(DATA_ENTRY);
 		
 		if(data != null){
 
 			String dataName = !data.getName().equals("") ? data.getName() : "data";
 			final Datatype      datatype = H5Utils.getDatatype(data);
 			final long[]         shape = new long[data.getShape().length];
 			for (int i = 0; i < shape.length; i++) shape[i] = data.getShape()[i];
 			
 			final Dataset dataset = file.replaceDataset(dataName,  datatype, shape, data.getBuffer(), parent);
 			file.setNexusAttribute(dataset, Nexus.SDS);
 		}
 		if(xAxisData != null){
 			String xAxisName = !xAxisData.getName().equals("") ? xAxisData.getName() : "X Axis";
 			final Datatype      xDatatype = H5Utils.getDatatype(xAxisData);
 			final long[]         xShape = new long[xAxisData.getShape().length];
 			for (int i = 0; i < xShape.length; i++) xShape[i] = xAxisData.getShape()[i];
 
 			final Dataset xDataset = file.replaceDataset(xAxisName,  xDatatype, xShape, xAxisData.getBuffer(), parent);
 			file.setNexusAttribute(xDataset, Nexus.SDS);
 		}
 
 		if(yAxisData != null){
 			String yAxisName = !yAxisData.getName().equals("") ? yAxisData.getName() : "Y Axis";
 			final Datatype      yDatatype = H5Utils.getDatatype(yAxisData);
 			final long[]         yShape = new long[yAxisData.getShape().length];
 			for (int i = 0; i < yShape.length; i++) yShape[i] = yAxisData.getShape()[i];
 
 			final Dataset yDataset = file.replaceDataset(yAxisName,  yDatatype, yShape, yAxisData.getBuffer(), parent);
 			file.setNexusAttribute(yDataset, Nexus.SDS);
 		}
 	}
 
 	// delete empty strings
 	private static String[] cleanArray(String[] array){
 
 		List<String> list = new ArrayList<String>();
 		for (int i = 0; i < array.length; i++) {
 			if(!array[i].isEmpty()){
 				list.add(array[i]);
 			}
 		}
 		String[] result = new String[list.size()];
 		return list.toArray(result);
 	}
 
 	/**
 	 * Method to write mask data to an HDF5 file given a specific path entry to save the data.
 	 * 
 	 * @param masks
 	 * @throws Exception
 	 */
 	private void writeH5Mask(final Map<String, BooleanDataset> masks) throws Exception {
 
 		if(file == null)
 			file = HierarchicalDataFactory.getWriter(filePath);
 
 		Group parent = createParentEntry(MASK_ENTRY);
 		
 		if (masks != null) {
 			Set<String> names = masks.keySet();
 			
 			Iterator<String> it = names.iterator();
 			while(it.hasNext()){
 				String name = it.next();
 				BooleanDataset bd = masks.get(name);
 				AbstractDataset id = DatasetUtils.cast(bd, AbstractDataset.INT8);
 				final Datatype datatype = H5Utils.getDatatype(id);
 				final long[] shape = new long[id.getShape().length];
 				for (int i = 0; i < shape.length; i++)
 					shape[i] = id.getShape()[i];
 				
 				final Dataset dataset = file.replaceDataset(name, datatype, shape, id.getBuffer(), parent);
 				file.setNexusAttribute(dataset, Nexus.SDS);
 			}
 		}
 	}
 
 	/**
 	 * Method to write rois data to an HDF5 file given a specific path entry to save the data.<br>
 	 * The rois are serialised using GSON and are saved as JSON format in the HDF5 file.
 	 * 
 	 * @param rois
 	 * @throws Exception
 	 */
 	private void writeH5Rois(final Map<String, ROIBase> rois) throws Exception {
 
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		
 		Group parent = createParentEntry(ROI_ENTRY);
 		
 		if (rois != null) {
 			// JSON serialisation
 			GsonBuilder builder = new GsonBuilder();
 			//TODO: serialiser to be worked on...
 			//builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
 			Gson gson = builder.create();
 
 			Iterator<String> it = rois.keySet().iterator();
 			while(it.hasNext()){
 				String name = it.next();
 				ROIBase roi = rois.get(name);
 				writeRoi(file, parent, name, roi, gson);
 			}
 
 		}
 	}
 	
 	private HObject writeRoi(IHierarchicalDataFile file, 
 			                 Group   parent,
 				             String  name,
 				             ROIBase roi,
 				             Gson    gson) throws Exception {
 		
 		ROIBean roibean = ROIBeanConverter.ROIBaseToROIBean(name, roi);
 		
 		long[] dims = {1};
 		
 		String json = gson.toJson(roibean);
 		
 		// we create the dataset
 		Dataset dat = file.replaceDataset(name, new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE), dims, new int[]{0}, parent);
 		// we set the JSON attribute
 		file.setAttribute(dat, "JSON", json);
 
 		return dat;
 	}
 
 	private Group createParentEntry(String fullEntry) throws Exception{
 		String[] entries = fullEntry.split("/");
 		entries = cleanArray(entries);
 		Group parent = null;
 		for (int i = 0; i < entries.length; i++) {
 			Group entry = null;
 			if(i == 0){
 				entry = file.group(entries[i]);
 				file.setNexusAttribute(entry, Nexus.ENTRY);
 				parent = entry;
 			} else if(i == entries.length-1) {
 				entry = file.group(entries[i], parent);
 				file.setNexusAttribute(entry, Nexus.DATA);
 				parent = entry;
 			} else {
 				entry = file.group(entries[i], parent);
 				file.setNexusAttribute(entry, Nexus.ENTRY);
 				parent = entry;
 			}
 		}
 		return parent;
 	}
 
 	/**
 	 * Method to read image data (axes, masks, image) from an HDF5 file and returns an ILazyDataset
 
 	 * @return ILazyDataset
 	 * @throws Exception 
 	 */
 	private ILazyDataset readH5Data(DataHolder dh, String dataName, String dataEntry) throws Exception{
 		ILazyDataset ld = dh.getLazyDataset(dataEntry+"/"+dataName);
 		if(ld == null) throw new Exception("Reading Exception: " +dataEntry+ " entry does not exist in the file " + filePath);
 		return ld;
 	}
 
 	/**
 	 * Method to read mask data from an HDF5 file
 	 * @return BooleanDataset
 	 * @throws Exception 
 	 */
 	private BooleanDataset readH5Mask(DataHolder dh, String maskName) throws Exception{
 		BooleanDataset bd = (BooleanDataset)dh.getDataset(MASK_ENTRY+maskName);
 		if(bd == null) throw new Exception("Reading Exception: " +MASK_ENTRY+ " entry does not exist in the file " + filePath);
 		return bd;
 	}
 
 	/**
 	 * Method to read mask data from an HDF5 file
 	 * @return Map<String, BooleanDataset>
 	 * @throws Exception 
 	 */
 	private Map<String, BooleanDataset> readH5Masks(DataHolder dh, IMonitor mon) throws Exception{
 		Map<String, BooleanDataset> masks = new HashMap<String, BooleanDataset>();
 		List<String> names = getMaskNames(mon);
 
 		Iterator<String> it = names.iterator();
 		while (it.hasNext()) {
 			String name = (String) it.next();
 			
 			ShortDataset sdata = (ShortDataset)LoaderFactory.getDataSet(filePath, MASK_ENTRY+"/"+name, mon);
 			BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(sdata, AbstractDataset.BOOL);
 			masks.put(name, bd);
 		}
 		return masks;
 	}
 
 	/**
 	 * Method to read roi data from an HDF5 file
 	 * 
 	 * @return ROIBase
 	 * @throws Exception 
 	 */
 	private ROIBase readH5ROI(String roiName) throws Exception{
 	
 		String json = file.getAttributeValue(ROI_ENTRY+roiName);
 		if(json == null) throw new Exception("Reading Exception: " +ROI_ENTRY+ " entry does not exist in the file " + filePath);
 		// JSON deserialization
 		GsonBuilder builder = new GsonBuilder();
 		//TODO: deserialiser to be worked on...
 		//builder.registerTypeAdapter(ROIBean.class, new ROIDeserializer());
 		Gson gson = builder.create();
 		ROIBean roibean = gson.fromJson(json, ROIBean.class);
 		//convert the bean to roibase
 		ROIBase roi = ROIBeanConverter.ROIBeanToROIBase(roibean);
 		
 		return roi;
 	}
 
 	/**
 	 * Method to read roi data from an HDF5 file
 	 * @return Map<String, ROIBase>
 	 * @throws Exception 
 	 */
 	private Map<String, ROIBase> readH5ROIs(IMonitor mon) throws Exception{
 		Map<String, ROIBase> rois = new HashMap<String, ROIBase>();
 		if(file == null)
 			file = HierarchicalDataFactory.getReader(filePath);
 		// JSON deserialization
 		GsonBuilder builder = new GsonBuilder();
 		//TODO: deserialiser to be worked on...
 		//builder.registerTypeAdapter(ROIBean.class, new ROIDeserializer());
 		Gson gson = builder.create();
 		
 		List<String> names = getROINames(mon);
 
 		Iterator<String> it = names.iterator();
 		while (it.hasNext()) {
 			String name = (String) it.next();
 			String json = file.getAttributeValue(ROI_ENTRY+"/"+name+"@JSON");
 			json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
 			ROIBean roibean = ROIBeanConverter.getROIBeanfromJSON(gson, json);
 			
 			//convert the bean to roibase
 			ROIBase roi = ROIBeanConverter.ROIBeanToROIBase(roibean);
 			rois.put(name, roi);
 		}
 
 		return rois;
 	}
 
 	/**
 	 * Method to retrieve all names in dataEntry
 	 * @param dataEntry
 	 * @param mon
 	 * @return List<String>
 	 * @throws Exception
 	 */
 	private List<String> getNames(DataHolder dh, String dataEntry) throws Exception{
 		List<String> nameslist = new ArrayList<String>();
 		String[] names = dh.getNames();
 		for (int i = 0; i < names.length; i++) {
 			if(names[i].startsWith(dataEntry)){
 				nameslist.add(names[i].substring(dataEntry.length()+1));
 			}
 		}
 		if (nameslist.isEmpty()) throw new Exception("Reading Exception: " +dataEntry+ " entry does not exist in the file " + filePath);
 
 		return nameslist;
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
 	public boolean isRegionSupported(ROIBase roi) {
 		return ROIBeanConverter.isROISupported(roi);
 	}
 	
 	private void writeH5DiffractionMetadata(IDiffractionMetadata metadata) throws Exception {
 		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
 		
 		Group parent = createParentEntry(DIFFRACTIONMETADATA_ENTRY);
 		
 		//TODO do we want to be an NX_detector?
 		//TODO should existing diffraction metadata node be deleted?
 		
 		DetectorProperties detprop = metadata.getDetector2DProperties();
 		
 		H5Datatype intType = new H5Datatype(Datatype.CLASS_INTEGER, 32/8, Datatype.NATIVE, Datatype.NATIVE);
 		H5Datatype doubleType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);
 		
		final Dataset nXPix = file.createDataset("x_pixel_number", intType, new long[] {1}, detprop.getPx(), parent);
 		file.setAttribute(nXPix,NexusUtils.UNIT, "pixels");
		final Dataset nYPix = file.createDataset("y_pixel_number", intType, new long[] {1}, detprop.getPy(), parent);
 		file.setAttribute(nYPix,NexusUtils.UNIT , "pixels");
 		
		final Dataset sXPix = file.createDataset("x_pixel_size", doubleType, new long[] {1}, detprop.getHPxSize(), parent);
 		file.setAttribute(sXPix, NexusUtils.UNIT, "mm");
		final Dataset sYPix = file.createDataset("y_pixel_size", doubleType, new long[] {1}, detprop.getVPxSize(), parent);
 		file.setAttribute(sYPix, NexusUtils.UNIT, "mm");
 		
 		double[] beamVector = new double[3];
 		detprop.getBeamVector().get(beamVector);
 		
 		file.createDataset("beam_vector", doubleType, new long[] {3}, beamVector, parent);
 		
 		Matrix3d or = detprop.getOrientation();
 		double[] orientation = new double[] {or.m00 ,or.m01, or.m02,
 											 or.m10, or.m11, or.m12,
 											 or.m20, or.m21, or.m22};
 		
		file.createDataset("beam_vector", doubleType, new long[] {9}, orientation, parent);
 		
 		DiffractionCrystalEnvironment crysenv = metadata.getDiffractionCrystalEnvironment();
 		// lambda(A) = 10^7 * (h*c/e) / energy(keV)
 		double energy = 1./(0.0806554465*crysenv.getWavelength());// constant from NIST CODATA 2006
		final Dataset ene = file.createDataset("energy", doubleType, new long[] {1}, energy, parent);
 		file.setAttribute(ene, NexusUtils.UNIT, "keV");
 		
		final Dataset count = file.createDataset("count_time", doubleType, new long[] {1}, crysenv.getExposureTime(), parent);
 		file.setAttribute(count, NexusUtils.UNIT, "s");
 		
		final Dataset phi_start = file.createDataset("phi_start", doubleType, new long[] {1}, crysenv.getPhiStart(), parent);
 		file.setAttribute(phi_start, NexusUtils.UNIT, "degrees");
 		
		final Dataset phi_range = file.createDataset("phi_start", doubleType, new long[] {1}, crysenv.getPhiRange(), parent);
 		file.setAttribute(phi_range, NexusUtils.UNIT, "degrees");
 	}
 	
 }
