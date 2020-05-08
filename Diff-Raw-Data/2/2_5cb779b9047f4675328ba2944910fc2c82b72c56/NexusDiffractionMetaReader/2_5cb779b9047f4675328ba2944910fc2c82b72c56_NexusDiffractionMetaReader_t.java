 package uk.ac.diamond.scisoft.analysis.io;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Vector3d;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5ScalarDS;
 
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.HierarchicalDataUtils;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.nexus.IFindInNexus;
 import org.dawb.hdf5.nexus.NexusFindDatasetByName;
 import org.dawb.hdf5.nexus.NexusFindGroupByAttributeText;
 import org.dawb.hdf5.nexus.NexusUtils;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 
 public class NexusDiffractionMetaReader {
 	
 	public static final String NX_MONOCHROMATOR = "NXmonochromator";
 	public static final String NX_INSTRUMENT = "NXinstrument";
 	public static final String NX_DETECTOR = "NXDetector";
 	public static final String DATA_NAME = "data";
 	public static final String DISTANCE_NAME = "distance";
 	public static final String CAMERA_LENGTH = "camera length";
 	public static final String ENERGY_NAME = "energy";
 	public static final String WAVELENGTH = "wavelength";
 	public static final String UNITS = "units";
 	public static final String BEAM_CENTER_X = "beam_center_x";
 	public static final String BEAM_CENTER_Y = "beam_center_y";
 	public static final String BEAM_CENTER = "beam centre";
 	public static final String COUNT_TIME = "count_time";
 	public static final String EXPOSURE_NAME = "exposure";
 	public static final String MM = "mm";
 	public static final String M = "m";
 	
 	
 	public enum DiffractionMetaValue {
 		PIXEL_SIZE,PIXEL_NUMBER,BEAM_CENTRE,DISTANCE,DETECTOR_ORIENTATION,ENERGY,PHI,EXPOSURE,BEAM_VECTOR
 	}
 	
 	private String filePath;
 	
 	private Map<DiffractionMetaValue,Boolean> successMap = new HashMap<DiffractionMetaValue,Boolean>();
 	
 	public NexusDiffractionMetaReader(String filePath) {
 		this.filePath = filePath;
 		for (DiffractionMetaValue val : DiffractionMetaValue.values()) {
 			successMap.put(val, false);
 		}
 	}
 
 	/**
 	 * Read the diffraction metadata from a Nexus file.
 	 * Other methods on the class can be used to determine how complete the read is
 	 * May return null
 	 * 
 	 * @param imageSize Size of the image the diffraction metadata is associated with in pixels (can be null)
 	 */
 	public IDiffractionMetadata getDiffractionMetadataFromNexus(int[] imageSize) {
 		return getDiffractionMetadataFromNexus(imageSize, null, null, null);
 	}
 	
 	/**
 	 * Read the diffraction metadata from a Nexus file.
 	 * Other methods on the class can be used to determine how complete the read is
 	 * May return null
 	 * 
 	 * @param imageSize Size of the image the diffraction metadata is associated with in pixels (can be null)
 	 * @param detprop Detector properties object to be populated from the nexus file
 	 * @param diffcrys Detector properties object to be populated from the nexus file
 	 */
 	public IDiffractionMetadata getDiffractionMetadataFromNexus(int[] imageSize,DetectorProperties detprop, DiffractionCrystalEnvironment diffcrys) {
 		return getDiffractionMetadataFromNexus(imageSize, detprop, diffcrys, null);
 	}
 	
 	/**
 	 * Read the diffraction metadata from a Nexus file.
 	 * Other methods on the class can be used to determine how complete the read is
 	 * May return null
 	 * 
 	 * @param imageSize Size of the image the diffraction metadata is associated with in pixels (can be null)
 	 * @param detprop Detector properties object to be populated from the nexus file
 	 * @param diffcrys Detector properties object to be populated from the nexus file
 	 * @param xyPixelSize Guess at pixel size. Will be used if pixel size not read.
 	 */
 	public IDiffractionMetadata getDiffractionMetadataFromNexus(int[] imageSize,DetectorProperties detprop, DiffractionCrystalEnvironment diffcrys, double[] xyPixelSize) {
 		if (!HierarchicalDataFactory.isHDF5(filePath)) return null;
 		
 		if (detprop == null) detprop = getInitialDetectorProperties();
 		if (diffcrys == null) diffcrys = getInitialCrystalEnvironment();
 		
 		IHierarchicalDataFile hiFile = null;
 		
 		try {
 			hiFile = HierarchicalDataFactory.getReader(filePath);
 
 			Group rootGroup = hiFile.getRoot();
 
 			//Check only one entry in root - might not act on it at the moment but may be useful to know
 //			if (rootGroup.getMemberList().size() > 1)
 //				logger.warn("More than one root node in file, metadata may be incorrect");
 
 			List<Group> nxInstruments = getNXGroups(rootGroup, NX_INSTRUMENT);
 
 			Group nxDetector = null;
 			Group nxInstrument = null;
 			if (nxInstruments != null) {
 				for (Group inst : nxInstruments) {
 					nxInstrument =  inst;
 					nxDetector = findBestNXDetector(inst, imageSize);
 					if (nxDetector != null) {
 						break;
 					}
 				}
 			}
 			
 			if (nxInstrument != null) {
 				Group nxMono = getNXGroup(nxInstrument, NX_MONOCHROMATOR);
 				if (nxMono != null) successMap.put(DiffractionMetaValue.ENERGY,updateEnergy(nxMono,diffcrys));
 			}
 			
 			//For NCD results files
 			if (nxDetector == null) {
 				nxDetector= findNXDetectorByName(rootGroup, "SectorIntegration");
 			}
 			
 			
 			//if no detectors with pixel in search the entire nxInstrument group
 			if (nxDetector == null) {
 				nxDetector = getNXGroup(rootGroup, NX_DETECTOR);
 			}
 			
 			if (nxDetector != null) {
 				//populate the crystal environment
 				populateFromNexus(nxDetector, diffcrys);
 				//populate detector properties
 				populateFromNexus(nxDetector, detprop);
 			}
 
 			} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (hiFile!= null)
 				try {
 					hiFile.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 		}
 		
 		if (!successMap.get(DiffractionMetaValue.PIXEL_SIZE) && xyPixelSize != null) {
 			detprop.setHPxSize(xyPixelSize[0]);
 			detprop.setHPxSize(xyPixelSize[1]);
 		}
 		
 		
 		return new DiffractionMetadata(filePath,detprop,diffcrys);
 	}
 	
 	/**
 	 * Check if the specified value was read from the Nexus file
 	 */
 	public boolean isMetadataEntryRead(DiffractionMetaValue entry) {
 		if (successMap.containsKey(entry)) {
 			return successMap.get(entry);
 		}
 		return false;
 	}
 	
 	/**
 	 * Have complete DetectorProperties and DiffractionCrystalEnvironment values been read
 	 */
 	public boolean isCompleteRead() {
 		return !successMap.containsValue(false);
 	}
 	
 	/**
 	 * Have enough values to perform downstream calculations been read (ie exposure time not read)
 	 */
 	public boolean isPartialRead() {
 		return successMap.get(DiffractionMetaValue.BEAM_CENTRE) &&
 			   successMap.get(DiffractionMetaValue.DETECTOR_ORIENTATION) &&
 			   successMap.get(DiffractionMetaValue.DISTANCE) &&
 			   successMap.get(DiffractionMetaValue.ENERGY) &&
 			   successMap.get(DiffractionMetaValue.PIXEL_NUMBER) &&
 			   successMap.get(DiffractionMetaValue.PIXEL_SIZE) &&
 			   successMap.get(DiffractionMetaValue.BEAM_VECTOR);
 	}
 	
 	public boolean isDetectorRead() {
 		return successMap.get(DiffractionMetaValue.PIXEL_SIZE);
 
 	}
 	
 	/**
 	 * Were any values read from the Nexus file
 	 */
 	public boolean anyValuesRead() {
 		return successMap.containsValue(true);
 	}
 	
 	private Group findBestNXDetector(Group nxInstrument, int[] imageSize) {
 		//Find nxDetectors in instrument
 		// TODO should probably change to find data then locate correct
 		// detector from image size
 		List<Group> nxDetectors = findNXDetectors(nxInstrument, DATA_NAME);
 
 		if (nxDetectors.size() == 1 || imageSize == null) {
 			//only one nxdetector or we dont know the image size
 			//so just use the first one
 			return nxDetectors.get(0);
 		}
 		for (Group detector : nxDetectors) {
 			H5ScalarDS dataset = getDataset(detector, DATA_NAME);
 			long[] dataShape;
 			try {
 				dataShape = HierarchicalDataUtils.getDims(dataset);
 			} catch (Exception e) {
 				continue;
 			}
 			if (dataShape == null) continue;
 			boolean matchesX = false;
 			boolean matchesY = false;
 			for (long val : dataShape) {
 				if (val == imageSize[0])
 					matchesX = true;
 				else if (val == imageSize[1])
 					matchesY = true;
 			}
 			if (matchesX & matchesY) {
 				return detector;
 			}
 		}
 		
 		return null;
 	}
 	
 	private Group getNXGroup(Group rootGroup, String nxAttribute) {
 		//Find NXinstrument (hopefully there is only one!)
 		NexusFindGroupByAttributeText finder = new NexusFindGroupByAttributeText(nxAttribute,NexusUtils.NXCLASS);
 		List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(finder, rootGroup, true);
 		if (hOb.isEmpty() || !(hOb.get(0) instanceof Group)) return null;
 		return (Group)hOb.get(0);
 	}
 	
 	private List<Group> getNXGroups(Group rootGroup, String nxAttribute) {
 		//Find NXinstrument (hopefully there is only one!)
 		NexusFindGroupByAttributeText finder = new NexusFindGroupByAttributeText(nxAttribute,NexusUtils.NXCLASS);
 		List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(finder, rootGroup, false);
 		if (hOb.isEmpty()) return null;
 		
 		List<Group> groupList = new ArrayList<Group>();
 		
 		for (HObject ob : hOb) {
 			if (ob instanceof Group) groupList.add((Group)ob);
 		}
 		
 		if (groupList.isEmpty()) return null;
 		
 		return groupList;
 	}
 	
 	
 	private void populateFromNexus(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
 		successMap.put(DiffractionMetaValue.EXPOSURE, updateExposureTime(nexusGroup,diffcrys));
 		
 		//Energy might not have been in NXmonochromator, if not look here
 		if (!successMap.get(DiffractionMetaValue.ENERGY)) {
 			successMap.put(DiffractionMetaValue.ENERGY,updateEnergy(nexusGroup, diffcrys));
 		}
 		
 		boolean phi = updatePhiRange(nexusGroup, diffcrys) && updatePhiStart(nexusGroup, diffcrys);
 		successMap.put(DiffractionMetaValue.PHI, phi);
 	}
 	
 	private boolean updateEnergy(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
 		H5ScalarDS dataset = getDataset(nexusGroup, ENERGY_NAME);
 		if (dataset == null) getDataset(nexusGroup, WAVELENGTH);
 		if (dataset == null) return false;
 		
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units.equals("keV")) {
 				diffcrys.setWavelengthFromEnergykeV(ds.getDouble(0));
 				return true;
 				}
 			if (units.contains("Angstrom")) {
 				diffcrys.setWavelength(ds.getDouble(0));
 				return true;
 				}
 			return false;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 		
 	private boolean updatePhiStart(Group nexusGroup, DiffractionCrystalEnvironment diffcry) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "phi_start");
 		if (dataset == null) return false;
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null) return false;
 			if (units.equals("degrees")) {
 				diffcry.setPhiStart(ds.getDouble(0));
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 	
 	private boolean updatePhiRange(Group nexusGroup, DiffractionCrystalEnvironment diffcry) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "phi_range");
 		if (dataset == null) return false;
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null) return false;
 			if (units.equals("degrees")) {
 				diffcry.setPhiRange(ds.getDouble(0));
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 	
 	private boolean updateExposureTime(Group nexusGroup, DiffractionCrystalEnvironment diffcrys) {
 		H5ScalarDS dataset = getDataset(nexusGroup, COUNT_TIME);
 		if (dataset == null) dataset = getDataset(nexusGroup, EXPOSURE_NAME);
 		if (dataset == null) return false;
 		
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null) return false;
 			if (units.equals("s")) {
 				diffcrys.setExposureTime(ds.getDouble(0));
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		
 		return false;
 	}
 	
 	private boolean updateBeamCentre(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, BEAM_CENTER);
 		if (dataset == null) dataset = getDataset(nexusGroup, "beam_centre");
 		if (dataset == null) return updateBeamCentreFromXY(nexusGroup, detprop);
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null || units.equals("pixels")) {
 				detprop.setBeamCentreCoords(new double[] {ds.getDouble(0),ds.getDouble(1)});
 				return true;
 			} else if (units.equals(MM)) {
 				detprop.setBeamCentreCoords(new double[] {ds.getDouble(0)*detprop.getVPxSize(),
 						ds.getDouble(1)*detprop.getHPxSize()});
 				return true;
 			} else if (units.equals(M)) {
 				detprop.setBeamCentreCoords(new double[] {ds.getDouble(0)*detprop.getVPxSize()*1000,
 						ds.getDouble(1)*detprop.getHPxSize()*1000});
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 	
 	private boolean updateBeamCentreFromXY(Group nexusGroup, DetectorProperties detprop){
 		H5ScalarDS dataset = getDataset(nexusGroup, BEAM_CENTER_X);
 		if (dataset == null) return false;
 		
 		double xCoord = Double.NaN;
 		
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset,UNITS);
 			if (units == null || units.equals("pixels")) {
 				xCoord = ds.getDouble(0);
 			}
 			
 			dataset = getDataset(nexusGroup, BEAM_CENTER_Y);
 			if (dataset == null) return false;
 			
 			ds = getSet(dataset);
 			units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null || units.equals("pixels")) {
				if (!Double.isNaN(xCoord)) {
 					detprop.setBeamCentreCoords(new double[] {xCoord,ds.getDouble(0)});
 					return true;
 				}
 				
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 	
 	private boolean updateDetectorDistance(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, CAMERA_LENGTH);
 		if (dataset == null) dataset = getDataset(nexusGroup, DISTANCE_NAME);
 		if (dataset == null) return false;
 		
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units.equals(MM)) {
 				detprop.setBeamCentreDistance(ds.getDouble(0));
 				return true;
 			} else if (units.equals(M)) {
 				detprop.setBeamCentreDistance(1000*ds.getDouble(0));
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 	
 	
 	private void populateFromNexus(Group nxDetector, DetectorProperties detprop) {
 		
 		successMap.put(DiffractionMetaValue.PIXEL_SIZE,updatePixelSize(nxDetector,detprop));
 		successMap.put(DiffractionMetaValue.BEAM_CENTRE, updateBeamCentre(nxDetector,detprop));
 		successMap.put(DiffractionMetaValue.DISTANCE, updateDetectorDistance(nxDetector,detprop));
 		successMap.put(DiffractionMetaValue.DETECTOR_ORIENTATION, updateDetectorOrientation(nxDetector, detprop));
 		successMap.put(DiffractionMetaValue.BEAM_VECTOR, updateBeamVector(nxDetector, detprop));
 		successMap.put(DiffractionMetaValue.PIXEL_NUMBER, updatePixelNumber(nxDetector, detprop));
 	}
 	
 	private boolean updateDetectorOrientation(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "detector_orientation");
 		if (dataset == null) return false;
 		
 		try {
 			AbstractDataset ds = getSet(dataset);
 			if (ds.getSize() != 9) return false;
 			if (ds instanceof DoubleDataset) {
 				detprop.setOrientation(new Matrix3d(((DoubleDataset)ds).getData()));
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 	
 	private boolean updateBeamVector(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "beam_vector");
 		if (dataset == null) return false;
 
 		try {
 			AbstractDataset ds = getSet(dataset);
 			if (ds.getSize() != 3) return false;
 			if (ds instanceof DoubleDataset) {
 				detprop.setBeamVector(new Vector3d(((DoubleDataset)ds).getData()));
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 
 		return false;
 	}
 	
 	private boolean updatePixelNumber(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "x_pixel_number");
 		if (dataset == null) return false;
 		
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null) return false;
 			if (units.equals("pixels")) {
 				detprop.setPx(ds.getInt(0));
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		
 		
 		dataset = getDataset(nexusGroup, "y_pixel_number");
 		if (dataset == null) return false;
 		
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null) return false;
 			if (units.equals("pixels")) {
 				detprop.setPy(ds.getInt(0));
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		
 		return false;
 	}
 	
 	
 	private boolean updatePixelSize(Group nexusGroup, DetectorProperties detprop) {
 		H5ScalarDS dataset = getDataset(nexusGroup, "x_pixel_size");
 		if (dataset == null) return false;
 
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null) return false;
 			if (units.equals(MM)) {detprop.setVPxSize(ds.getDouble(0));}
 			else if (units.equals(M)) {detprop.setVPxSize(ds.getDouble(0)*1000);}
 		} catch (Exception e) {
 			return false;
 		}
 
 		dataset = getDataset(nexusGroup, "y_pixel_size");
 
 		try {
 			AbstractDataset ds = getSet(dataset);
 			String units = NexusUtils.getNexusGroupAttributeValue(dataset, UNITS);
 			if (units == null) return false;
 			if (units.equals(MM)) {
 				detprop.setHPxSize(ds.getDouble(0));
 				return true;
 			}
 			else if (units.equals(M)) {
 				detprop.setHPxSize(ds.getDouble(0)*1000);
 				return true;
 			}
 
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 		
 	}
 	
 	private H5ScalarDS getDataset(Group group, String name) {
 		NexusFindDatasetByName dataFinder = new NexusFindDatasetByName(name);
 		List<HObject>  hOb = NexusUtils.nexusBreadthFirstSearch(dataFinder, group, true);
 		hOb = NexusUtils.nexusBreadthFirstSearch(dataFinder,group, false);
 		if (hOb.isEmpty() || !(hOb.get(0) instanceof H5ScalarDS)) { return null;}
 		H5ScalarDS h5data = (H5ScalarDS)hOb.get(0);
 		
 		return h5data;
 	}
 	
 	
 	private List<Group> findNXDetectors(Group nxInstrument, String childNameContains) {
 		final String groupText = NX_DETECTOR;
 		final String childText = childNameContains;
 		
 		IFindInNexus findWithChild = new IFindInNexus() {
 			
 			@Override
 			public boolean inNexus(HObject nexusObject) {
 				if(nexusObject instanceof Group) {
 					if (NexusUtils.getNexusGroupAttributeValue(nexusObject,NexusUtils.NXCLASS).toLowerCase().equals(groupText.toLowerCase())) {
 						for (Object ob: ((Group)nexusObject).getMemberList()) {
 							if(ob instanceof H5ScalarDS) {
 								if (((H5ScalarDS)ob).getName().toLowerCase().contains((childText.toLowerCase()))) {
 									return true;
 								}
 							}
 						}
 					}
 				}
 				return false;
 			}
 		};
 		
 		List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(findWithChild, nxInstrument, false);
 		
 		List<Group> detectorGroups = new ArrayList<Group>(hOb.size());
 		
 		for (HObject ob : hOb) {
 			if (ob instanceof Group) {
 				detectorGroups.add((Group)ob);
 			}
 		}
 		return detectorGroups;
 	}
 	
 	private Group findNXDetectorByName(Group nxInstrument, final String name) {
 		final String groupText = NX_DETECTOR;
 		IFindInNexus findWithName = new IFindInNexus() {
 
 			@Override
 			public boolean inNexus(HObject nexusObject) {
 				if(nexusObject instanceof Group) {
 					if (NexusUtils.getNexusGroupAttributeValue(nexusObject,NexusUtils.NXCLASS).toLowerCase().equals(groupText.toLowerCase())) {
 						if  (nexusObject.getName().toLowerCase().contains((name.toLowerCase()))) return true;
 					}
 				}
 				return false;
 			}
 		};
 		
 		List<HObject> hOb = NexusUtils.nexusBreadthFirstSearch(findWithName, nxInstrument, true);
 
 		if (hOb == null || hOb.isEmpty()) return null;
 
 
 		return (Group)hOb.get(0);
 	}
 	
 	private DetectorProperties getInitialDetectorProperties() {
 		//Try to return harmless but not physical values
 		Matrix3d identityMatrix = new Matrix3d();
 		identityMatrix.setIdentity();
 		return new DetectorProperties(new Vector3d(1,1,1), 1,
 				1, 1, 1, identityMatrix);
 	}
 	
 	private DiffractionCrystalEnvironment getInitialCrystalEnvironment() {
 		//Try to return harmless but not physical values
 		return new DiffractionCrystalEnvironment(1, 0, 0, 1);
 	}
 	
 	private AbstractDataset getSet(final Dataset set) throws Exception {
 
 		final Object  val = set.read();
 		
 		long[] dataShape = HierarchicalDataUtils.getDims(set);
 		
 		final int[] intShape  = getInt(dataShape);
          
 		AbstractDataset ret = null;
         if (val instanceof byte[]) {
         	ret = new ByteDataset((byte[])val, intShape);
         } else if (val instanceof short[]) {
         	ret = new ShortDataset((short[])val, intShape);
         } else if (val instanceof int[]) {
         	ret = new IntegerDataset((int[])val, intShape);
         } else if (val instanceof long[]) {
         	ret = new LongDataset((long[])val, intShape);
         } else if (val instanceof float[]) {
         	ret = new FloatDataset((float[])val, intShape);
         } else if (val instanceof double[]) {
         	ret = new DoubleDataset((double[])val, intShape);
         } else {
         	throw new Exception("Cannot deal with data type "+set.getDatatype().getDatatypeDescription());
         }
         
 		if (set.getDatatype().isUnsigned()) {
 			switch (ret.getDtype()) {
 			case AbstractDataset.INT32:
 				ret = new LongDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 32);
 				break;
 			case AbstractDataset.INT16:
 				ret = new IntegerDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 16);
 				break;
 			case AbstractDataset.INT8:
 				ret = new ShortDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 8);
 				break;
 			}
 		}
         return ret;
 	}
 	
 	private int[] getInt(long[] longShape) {
 		final int[] intShape  = new int[longShape.length];
 		for (int i = 0; i < intShape.length; i++) intShape[i] = (int)longShape[i];
 		return intShape;
 	}
 }
