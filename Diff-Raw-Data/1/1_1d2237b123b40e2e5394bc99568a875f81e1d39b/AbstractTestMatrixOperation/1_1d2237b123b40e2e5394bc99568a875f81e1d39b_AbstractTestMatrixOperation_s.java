 package org.gwaspi.netCDF.operations;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.global.Text;
 import org.gwaspi.model.MarkerKey;
 import org.gwaspi.model.MatricesList;
 import org.gwaspi.model.MatrixMetadata;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.OperationMetadata;
 import org.gwaspi.model.OperationsList;
 import org.gwaspi.model.SampleKey;
 import org.gwaspi.netCDF.markers.MarkerSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.ArrayChar;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.NetcdfFileWriteable;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public abstract class AbstractTestMatrixOperation implements MatrixOperation {
 
 	private final Logger log
 			= LoggerFactory.getLogger(AbstractTestMatrixOperation.class);
 
 	private int rdMatrixId;
 	private Operation markerCensusOP;
 	private Operation hwOP;
 	private double hwThreshold;
 	private String testName;
 	private OPType testType;
 
 	public AbstractTestMatrixOperation(
 			int rdMatrixId,
 			Operation markerCensusOP,
 			Operation hwOP,
 			double hwThreshold,
 			String testName,
 			OPType testType)
 	{
 		this.rdMatrixId = rdMatrixId;
 		this.markerCensusOP = markerCensusOP;
 		this.hwOP = hwOP;
 		this.hwThreshold = hwThreshold;
 		this.testName = testName;
 		this.testType = testType;
 	}
 
 	@Override
 	public int processMatrix() throws IOException, InvalidRangeException {
 		int resultAssocId = Integer.MIN_VALUE;
 
 		Map<MarkerKey, Object> excludeMarkerSetMap = new LinkedHashMap<MarkerKey, Object>();
 		boolean dataLeft = excludeMarkersByHW(hwOP, hwThreshold, excludeMarkerSetMap);
 
 		if (dataLeft) { // CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING
 			OperationMetadata rdCensusOPMetadata = OperationsList.getOperationMetadata(markerCensusOP.getId());
 			NetcdfFile rdOPNcFile = NetcdfFile.open(rdCensusOPMetadata.getPathToMatrix());
 
 			MarkerOperationSet rdCaseMarkerSet = new MarkerOperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getId());
 			MarkerOperationSet rdCtrlMarkerSet = new MarkerOperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getId());
 			Map<SampleKey, Object> rdSampleSetMap = rdCaseMarkerSet.getImplicitSetMap();
 			Map<MarkerKey, Object> rdCtrlMarkerIdSetMap = rdCtrlMarkerSet.getOpSetMap();
 
 			Map<MarkerKey, Object> wrMarkerSetMap = new LinkedHashMap<MarkerKey, Object>();
 			for (MarkerKey key : rdCtrlMarkerIdSetMap.keySet()) {
 				if (!excludeMarkerSetMap.containsKey(key)) {
 					wrMarkerSetMap.put(key, "");
 				}
 			}
 
 			// GATHER INFO FROM ORIGINAL MATRIX
 			MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(markerCensusOP.getParentMatrixId());
 			MarkerSet rdMarkerSet = new MarkerSet(parentMatrixMetadata.getStudyId(), markerCensusOP.getParentMatrixId());
 			rdMarkerSet.initFullMarkerIdSetMap();
 
 			// retrieve chromosome info
 			rdMarkerSet.fillMarkerSetMapWithChrAndPos();
 			MarkerSet.replaceWithValuesFrom(wrMarkerSetMap, rdMarkerSet.getMarkerIdSetMap());
 			Map<MarkerKey, Object> rdChrInfoSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerSetMap, 0, 1);
 
 			NetcdfFileWriteable wrOPNcFile = null;
 			try {
 				// CREATE netCDF-3 FILE
 				DecimalFormat dfSci = new DecimalFormat("0.##E0#");
 				OperationFactory wrOPHandler = new OperationFactory(
 						rdCensusOPMetadata.getStudyId(),
 						testName, // friendly name
 						testName + " on " + markerCensusOP.getFriendlyName() + "\n" + rdCensusOPMetadata.getDescription() + "\nHardy-Weinberg threshold: " + dfSci.format(hwThreshold), // description
 						wrMarkerSetMap.size(),
 						rdCensusOPMetadata.getImplicitSetSize(),
 						rdChrInfoSetMap.size(),
 						testType,
 						rdCensusOPMetadata.getParentMatrixId(), // Parent matrixId
 						markerCensusOP.getId()); // Parent operationId
 				wrOPNcFile = wrOPHandler.getNetCDFHandler();
 
 				try {
 					wrOPNcFile.create();
 				} catch (IOException ex) {
 					log.error("Failed creating file: " + wrOPNcFile.getLocation(), ex);
 				}
 				//log.info("Done creating netCDF handle: {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 				//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
 				// MARKERSET MARKERID
 				ArrayChar.D2 markersD2 = Utils.writeMapKeysToD2ArrayChar(wrMarkerSetMap, cNetCDF.Strides.STRIDE_MARKER_NAME);
 				int[] markersOrig = new int[]{0, 0};
 				try {
 					wrOPNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
 				} catch (IOException ex) {
 					log.error("Failed writing file", ex);
 				} catch (InvalidRangeException ex) {
 					log.error("Failed writing file", ex);
 				}
 
 				// MARKERSET RSID
 				Map<MarkerKey, Object> rdCaseMarkerIdSetMap = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
 				for (Map.Entry<MarkerKey, Object> entry : wrMarkerSetMap.entrySet()) {
 					Object value = rdCaseMarkerIdSetMap.get(entry.getKey());
 					entry.setValue(value);
 				}
 				Utils.saveCharMapValueToWrMatrix(wrOPNcFile, wrMarkerSetMap, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);
 
 				// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
 				ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
 
 				int[] sampleOrig = new int[]{0, 0};
 				try {
 					wrOPNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
 				} catch (IOException ex) {
 					log.error("Failed writing file", ex);
 				} catch (InvalidRangeException ex) {
 					log.error("Failed writing file", ex);
 				}
 				log.info("Done writing SampleSet to matrix");
 
 				// WRITE CHROMOSOME INFO
 				// Set of chromosomes found in matrix along with number of markersinfo
 				org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(wrOPNcFile, rdChrInfoSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
 				// Number of marker per chromosome & max pos for each chromosome
 				int[] columns = new int[]{0, 1, 2, 3};
 				org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(wrOPNcFile, rdChrInfoSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);
 				//</editor-fold>
 
 				//<editor-fold defaultstate="expanded" desc="GET CENSUS & PERFORM TESTS">
 				// CLEAN Maps FROM MARKERS THAT FAILED THE HARDY WEINBERG THRESHOLD
 				Map<MarkerKey, Object> wrCaseMarkerIdSetMap = new LinkedHashMap<MarkerKey, Object>();
 				rdCaseMarkerIdSetMap = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
 				if (rdCaseMarkerIdSetMap != null) {
 					for (Map.Entry<MarkerKey, Object> entry : rdCaseMarkerIdSetMap.entrySet()) {
 						MarkerKey key = entry.getKey();
 
 						if (!excludeMarkerSetMap.containsKey(key)) {
 							wrCaseMarkerIdSetMap.put(key, entry.getValue());
 						}
 					}
 					rdCaseMarkerIdSetMap.clear();
 				}
 
 				Map<MarkerKey, Object> wrCtrlMarkerSet = new LinkedHashMap<MarkerKey, Object>();
 				rdCtrlMarkerIdSetMap = rdCtrlMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
 				if (rdCtrlMarkerIdSetMap != null) {
 					for (Map.Entry<MarkerKey, Object> entry : rdCtrlMarkerIdSetMap.entrySet()) {
 						MarkerKey key = entry.getKey();
 
 						if (!excludeMarkerSetMap.containsKey(key)) {
 							wrCtrlMarkerSet.put(key, entry.getValue());
 						}
 					}
 					rdCtrlMarkerIdSetMap.clear();
 				}
 
 				log.info(Text.All.processing);
 				performTest(wrOPNcFile, wrCaseMarkerIdSetMap, wrCtrlMarkerSet);
 
 				org.gwaspi.global.Utils.sysoutCompleted(testName);
 				//</editor-fold>
 
 				resultAssocId = wrOPHandler.getResultOPId();
 			} catch (InvalidRangeException ex) {
 				log.error(null, ex);
 			} catch (IOException ex) {
 				log.error(null, ex);
 			} finally {
 				if (null != rdOPNcFile) {
 					try {
 						rdOPNcFile.close();
 						wrOPNcFile.close();
 					} catch (IOException ex) {
 						log.warn("Cannot close file", ex);
 					}
 				}
 			}
 		} else { // NO DATA LEFT AFTER THRESHOLD FILTER PICKING
 			log.warn(Text.Operation.warnNoDataLeftAfterPicking);
 		}
 
 		return resultAssocId;
 	}
 
 	static boolean excludeMarkersByHW(Operation hwOP, double hwThreshold, Map<MarkerKey, Object> excludeMarkerSetMap) throws IOException {
 
 		excludeMarkerSetMap.clear();
 		int totalMarkerNb = 0;
 
 		if (hwOP != null) {
 			OperationMetadata hwMetadata = OperationsList.getOperationMetadata(hwOP.getId());
 			NetcdfFile rdHWNcFile = NetcdfFile.open(hwMetadata.getPathToMatrix());
 			MarkerOperationSet rdHWOperationSet = new MarkerOperationSet(hwMetadata.getStudyId(), hwMetadata.getOPId());
 			Map<MarkerKey, Object> rdHWMarkerSetMap = rdHWOperationSet.getOpSetMap();
 			totalMarkerNb = rdHWMarkerSetMap.size();
 
 			// EXCLUDE MARKER BY HARDY WEINBERG THRESHOLD
 			rdHWMarkerSetMap = rdHWOperationSet.fillOpSetMapWithVariable(rdHWNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
 			for (Map.Entry<MarkerKey, Object> entry : rdHWMarkerSetMap.entrySet()) {
 				double value = (Double) entry.getValue();
 				if (value < hwThreshold) {
 					excludeMarkerSetMap.put(entry.getKey(), value);
 				}
 			}
 			rdHWNcFile.close();
 		}
 
 		return (excludeMarkerSetMap.size() < totalMarkerNb);
 	}
 
 	/**
 	 * Performs actual Test.
 	 * @param wrNcFile
 	 * @param wrCaseMarkerIdSetMap
 	 * @param wrCtrlMarkerSet
 	 */
 	protected abstract void performTest(NetcdfFileWriteable wrNcFile, Map<MarkerKey, Object> wrCaseMarkerIdSetMap, Map<MarkerKey, Object> wrCtrlMarkerSet);
 }
