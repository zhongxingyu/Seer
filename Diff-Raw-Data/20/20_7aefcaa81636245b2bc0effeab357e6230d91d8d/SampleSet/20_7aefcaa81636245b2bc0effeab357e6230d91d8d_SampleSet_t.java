 package org.gwaspi.samples;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.model.MatricesList;
 import org.gwaspi.model.MatrixMetadata;
 import org.gwaspi.model.SampleInfo;
 import org.gwaspi.model.SampleInfoList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.ArrayByte;
 import ucar.ma2.ArrayChar;
 import ucar.ma2.ArrayDouble;
 import ucar.ma2.DataType;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 
 /**
  * TODO move to package org.gwaspi.model and create a SampleService
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class SampleSet {
 
 	private final Logger log
 			= LoggerFactory.getLogger(SampleSet.class);
 
 	// SAMPLESET_MEATADATA
 	private int sampleset_id = Integer.MIN_VALUE; // id
 	private int sampleSetSize = 0;
 	private MatrixMetadata matrixMetadata;
 	private Map<String, Object> sampleIdSetMap = new LinkedHashMap<String, Object>();
 
 	public SampleSet(int studyId, int matrixId) throws IOException {
 		matrixMetadata = MatricesList.getMatrixMetadataById(matrixId);
 		sampleSetSize = matrixMetadata.getMarkerSetSize();
 	}
 
 	public SampleSet(int studyId, String netCDFName) throws IOException {
 		matrixMetadata = MatricesList.getMatrixMetadataByNetCDFname(netCDFName);
 		sampleSetSize = matrixMetadata.getMarkerSetSize();
 	}
 
 	public SampleSet(int studyId, String netCDFPath, String netCDFName) throws IOException {
 		matrixMetadata = MatricesList.getMatrixMetadata(netCDFPath, studyId, netCDFName);
 		sampleSetSize = matrixMetadata.getMarkerSetSize();
 	}
 
 	// ACCESSORS
 	public int getSampleSetId() {
 		return sampleset_id;
 	}
 
 	public int getSampleSetSize() {
 		return sampleSetSize;
 	}
 
 	public MatrixMetadata getMatrixMetadata(List<String> _sampleSetAL) {
 		return matrixMetadata;
 	}
 
 	//<editor-fold defaultstate="collapsed" desc="SAMPLESET FETCHERS">
 	public Map<String, Object> getSampleIdSetMap() throws InvalidRangeException {
 		NetcdfFile ncfile = null;
 
 		try {
 			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
 			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_SAMPLESET);
 
 			if (null == var) {
 				return null;
 			}
 
 			int[] varShape = var.getShape();
 			Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
 
 			try {
 				sampleSetSize = markerSetDim.getLength();
 				ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (sampleSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
 
 				sampleIdSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(sampleSetAC);
 
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 
 		} catch (IOException ex) {
 			log.error("Cannot open file", ex);
 		} finally {
 			if (null != ncfile) {
 				try {
 					ncfile.close();
 				} catch (IOException ex) {
 					log.warn("Cannot close file", ex);
 				}
 			}
 		}
 
 		return sampleIdSetMap;
 	}
 
 	public Map<String, Object> getSampleIdSetMap(String matrixImportPath) throws InvalidRangeException {
 		NetcdfFile ncfile = null;
 
 		try {
 			ncfile = NetcdfFile.open(matrixImportPath);
 			Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_SAMPLESET);
 
 			if (null == var) {
 				return null;
 			}
 
 			int[] varShape = var.getShape();
 			Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
 
 			try {
 				sampleSetSize = markerSetDim.getLength();
 				ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (sampleSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
 
 				sampleIdSetMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapKeys(sampleSetAC);
 
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 
 		} catch (IOException ex) {
 			log.error("Cannot open file", ex);
 		} finally {
 			if (null != ncfile) {
 				try {
 					ncfile.close();
 				} catch (IOException ex) {
 					log.warn("Cannot close file", ex);
 				}
 			}
 		}
 
 		return sampleIdSetMap;
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="collapsed" desc="SAMPLESET FILLERS">
 	public Map<String, Object> readAllSamplesGTsFromCurrentMarkerToMap(NetcdfFile rdNcFile, Map<String, Object> rdMap, int markerNb) throws IOException {
 
 		try {
 			Variable genotypes = rdNcFile.findVariable(cNetCDF.Variables.VAR_GENOTYPES);
 
 			if (null == genotypes) {
 				return rdMap;
 			}
 			try {
 				int[] varShape = genotypes.getShape();
 //				Dimension markerSetDim = ncfile.findDimension(cNetCDF.DIM_MARKERSET);
 //				ArrayChar.D3 gt_ACD3 = (ArrayChar.D3) genotypes.read(
 //						"(" + markerNb + ":" + markerNb + ":1, 0:"
 //						+ (markerSetDim.getLength() - 1) + ":1, 0:"
 //						+ (varShape[2] - 1) + ":1)");
 
 				Dimension sampleSetDim = rdNcFile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
 //				ArrayChar.D3 gt_ACD3 = (ArrayChar.D3) genotypes.read(
 //						"(0:" + (sampleSetDim.getLength() - 1) + ":1, "
 //						 + markerNb + ":" + markerNb + ":1, "
 //						 + "0:" + (varShape[2] - 1) + ":1)");
 
 				ArrayByte.D3 gt_ACD3 = (ArrayByte.D3) genotypes.read(
 						"(0:" + (sampleSetDim.getLength() - 1) + ":1, "
 						+ markerNb + ":" + markerNb + ":1, "
 						+ "0:" + (varShape[2] - 1) + ":1)");
 
 				int[] shp = gt_ACD3.getShape();
 				int reducer = 0;
 				if (shp[0] == 1) {
 					reducer++;
 				}
 				if (shp[1] == 1) {
 					reducer++;
 				}
 				if (shp[2] == 1) {
 					reducer++;
 				}
 
 				if (reducer == 1) {
 					ArrayByte.D2 gt_ACD2 = (ArrayByte.D2) gt_ACD3.reduce();
 					org.gwaspi.netCDF.operations.Utils.writeD2ArrayByteToMapValues(gt_ACD2, rdMap);
 				} else if (reducer == 2) {
 					ArrayByte.D1 gt_ACD1 = (ArrayByte.D1) gt_ACD3.reduce();
 					org.gwaspi.netCDF.operations.Utils.writeD1ArrayByteToMapValues(gt_ACD1, rdMap);
 				}
 
 //				ArrayChar.D2 gt_ACD2 = (ArrayChar.D2) gt_ACD3.reduce();
 //				rdMap = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(gt_ACD2, rdMap);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		} catch (IOException ex) {
 			log.error("Cannot open file", ex);
 		}
 
 		return rdMap;
 	}
 
 	public Map<String, Object> fillSampleIdSetMapWithVariable(NetcdfFile ncfile, String variable) {
 		try {
 			Variable var = ncfile.findVariable(variable);
 
 			if (null == var) {
 				return null;
 			}
 
 			DataType dataType = var.getDataType();
 			int[] varShape = var.getShape();
 
 			sampleSetSize = varShape[0];
 			if (dataType == DataType.CHAR) {
 				ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (sampleSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
 				org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(sampleSetAC, sampleIdSetMap);
 			}
 			if (dataType == DataType.DOUBLE) {
 				ArrayDouble.D1 sampleSetAF = (ArrayDouble.D1) var.read("(0:" + (sampleSetSize - 1) + ":1");
 				org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToMapValues(sampleSetAF, sampleIdSetMap);
 			}
 		} catch (IOException ex) {
 			log.error("Cannot open file", ex);
 		} catch (InvalidRangeException ex) {
 			log.error("Cannot read data", ex);
 		} finally {
 			if (null != ncfile) {
 				try {
 					ncfile.close();
 				} catch (IOException ex) {
 					log.warn("Cannot close file", ex);
 				}
 			}
 		}
 
 		return sampleIdSetMap;
 	}
 
 	private void fillSampleIdSetMapWithVariable(Map<String, Object> map, String variable) {
 		NetcdfFile ncfile = null;
 
 		try {
 			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
 			Variable var = ncfile.findVariable(variable);
 
 			if (null == var) {
 				return;
 			}
 
 			DataType dataType = var.getDataType();
 			int[] varShape = var.getShape();
 			Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
 
 			try {
 				sampleSetSize = sampleSetDim.getLength();
 				if (dataType == DataType.CHAR) {
 					ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (sampleSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
 					org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(sampleSetAC, map);
 					sampleIdSetMap = map;
 				}
 				if (dataType == DataType.DOUBLE) {
 					ArrayDouble.D1 sampleSetAF = (ArrayDouble.D1) var.read("(0:" + (sampleSetSize - 1) + ":1");
 					org.gwaspi.netCDF.operations.Utils.writeD1ArrayDoubleToMapValues(sampleSetAF, map);
 					sampleIdSetMap = map;
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		} catch (IOException ex) {
 			log.error("Cannot open file", ex);
 		} finally {
 			if (null != ncfile) {
 				try {
 					ncfile.close();
 				} catch (IOException ex) {
 					log.warn("Cannot close file", ex);
 				}
 			}
 		}
 	}
 
 	private void fillSampleIdSetMapWithFilterVariable(Map<String, Object> map, String variable, int filterPos) {
 		NetcdfFile ncfile = null;
 
 		try {
 			ncfile = NetcdfFile.open(matrixMetadata.getPathToMatrix());
 			Variable var = ncfile.findVariable(variable);
 
 			if (null == var) {
 				return;
 			}
 
 			DataType dataType = var.getDataType();
 //			int[] varShape = var.getShape();
 			Dimension sampleSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);
 
 			try {
 				sampleSetSize = sampleSetDim.getLength();
 				if (dataType == DataType.CHAR) {
 					ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (sampleSetSize - 1) + ":1, " + filterPos + ":" + filterPos + ":1)");
 					org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToMapValues(sampleSetAC, map);
 					sampleIdSetMap = map;
 				}
 			} catch (IOException ex) {
 				log.error("Cannot read data", ex);
 			} catch (InvalidRangeException ex) {
 				log.error("Cannot read data", ex);
 			}
 		} catch (IOException ex) {
 			log.error("Cannot open file", ex);
 		} finally {
 			if (null != ncfile) {
 				try {
 					ncfile.close();
 				} catch (IOException ex) {
 					log.warn("Cannot close file", ex);
 				}
 			}
 		}
 	}
 
 	public Map<String, Object> fillMapWithDefaultValue(Map<String, Object> map, Object defaultVal) {
 		for (Map.Entry<String, Object> entry : map.entrySet()) {
 			entry.setValue(defaultVal);
 		}
 		return map;
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="collapsed" desc="SAMPLESET PICKERS">
 	public Map<String, Object> pickValidSampleSetItemsByDBField(Object poolId, Map<String, Object> map, String dbField, Set<Object> criteria, boolean include) throws IOException {
 		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
 		List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(poolId);
 
 		int pickCounter = 0;
 		if (include) {
 			for (String key : map.keySet()) {
 				// loop through rows of result set
 				for (SampleInfo sampleInfo : sampleInfos) {
 					if (sampleInfo.getSampleId().equals(key.toString())) {
 						if (criteria.contains(sampleInfo.getField(dbField).toString())) {
 							returnMap.put(key, pickCounter);
 						}
 					}
 				}
 				pickCounter++;
 			}
 		} else {
 			for (String key : map.keySet()) {
 				// loop through rows of result set
 				for (SampleInfo sampleInfo : sampleInfos) {
 					if (sampleInfo.getSampleId().equals(key.toString())) {
 						if (!criteria.contains(sampleInfo.getField(dbField).toString())) {
 							returnMap.put(key, pickCounter);
 						}
 					}
 				}
 				pickCounter++;
 			}
 		}
 
 		return returnMap;
 	}
 
 	public Map<String, Object> pickValidSampleSetItemsByNetCDFValue(Map<String, Object> map, String variable, Set<Object> criteria, boolean include) {
 		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
 		fillSampleIdSetMapWithVariable(map, variable);
 
 		int pickCounter = 0;
 		if (include) {
 			for (Map.Entry<String, Object> entry : sampleIdSetMap.entrySet()) {
 				if (criteria.contains(entry.getValue())) {
 					returnMap.put(entry.getKey(), pickCounter);
 				}
 				pickCounter++;
 			}
 		} else {
 			for (Map.Entry<String, Object> entry : sampleIdSetMap.entrySet()) {
 				if (!criteria.contains(entry.getValue())) {
 					returnMap.put(entry.getKey(), pickCounter);
 				}
 				pickCounter++;
 			}
 		}
 
 		return returnMap;
 	}
 
 	public Map<String, Object> pickValidSampleSetItemsByNetCDFFilter(Map<String, Object> map, String variable, int fiterPos, Set<Object> criteria, boolean include) {
 		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
 		fillSampleIdSetMapWithFilterVariable(map, variable, fiterPos);
 
 		int pickCounter = 0;
 		if (include) {
 			for (Map.Entry<String, Object> entry : sampleIdSetMap.entrySet()) {
 				if (criteria.contains(entry.getValue())) {
 					returnMap.put(entry.getKey(), pickCounter);
 				}
 				pickCounter++;
 			}
 		} else {
 			for (Map.Entry<String, Object> entry : sampleIdSetMap.entrySet()) {
 				if (!criteria.contains(entry.getValue())) {
 					returnMap.put(entry.getKey(), pickCounter);
 				}
 				pickCounter++;
 			}
 		}
 
 		return returnMap;
 	}
 
 	public Map<String, Object> pickValidSampleSetItemsByNetCDFKey(Map<String, Object> map, Set<Object> criteria, boolean include) throws IOException {
 		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();
 
 		int pickCounter = 0;
 		if (include) {
 			for (String key : map.keySet()) {
 				if (criteria.contains(key)) {
 					returnMap.put(key, pickCounter);
 				}
 				pickCounter++;
 			}
 		} else {
 			for (String key : map.keySet()) {
 				if (!criteria.contains(key)) {
 					returnMap.put(key, pickCounter);
 				}
 				pickCounter++;
 			}
 		}
 
 		return returnMap;
 	}
 	//</editor-fold>
 }
