 package org.gwaspi.netCDF.loader;
 
 import org.gwaspi.constants.cDBGWASpi;
 import org.gwaspi.constants.cDBMatrix;
 import org.gwaspi.constants.cImport.ImportFormat;
 import org.gwaspi.constants.cImport.StrandFlags;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
 import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
 import org.gwaspi.database.DbManager;
 import org.gwaspi.global.ServiceLocator;
 import org.gwaspi.global.Text;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.gwaspi.netCDF.matrices.MatrixFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.ArrayChar;
 import ucar.ma2.ArrayInt;
 import ucar.ma2.Index;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.NetcdfFileWriteable;
 
 public abstract class AbstractLoadGTFromFiles implements GenotypesLoader {
 
 	private final Logger log
 			= LoggerFactory.getLogger(AbstractLoadGTFromFiles.class);
 
 	private ImportFormat format;
 	private StrandType matrixStrand;
 	private boolean hasDictionary;
 	private int markersD2ItemNb;
 	private String markersD2Variables;
 
 	public AbstractLoadGTFromFiles(
 			ImportFormat format,
 			StrandType matrixStrand,
 			boolean hasDictionary,
 			int markersD2ItemNb,
 			String markersD2Variables
 			)
 	{
		this.format = format;
 		this.matrixStrand = matrixStrand;
 		this.hasDictionary = hasDictionary;
 		this.markersD2ItemNb = markersD2ItemNb;
 		this.markersD2Variables = markersD2Variables;
 	}
 
 	@Override
 	public ImportFormat getFormat() {
 		return format;
 	}
 
 	@Override
 	public StrandType getMatrixStrand() {
 		return matrixStrand;
 	}
 
 	@Override
 	public boolean isHasDictionary() {
 		return hasDictionary;
 	}
 
 	@Override
 	public int getMarkersD2ItemNb() {
 		return markersD2ItemNb;
 	}
 
 	@Override
 	public String getMarkersD2Variables() {
 		return markersD2Variables;
 	}
 
 	protected void addAdditionalBigDescriptionProperties(StringBuilder descSB, GenotypesLoadDescription loadDescription) {
 	}
 
 	protected abstract MetadataLoader createMetaDataLoader(String filePath, GenotypesLoadDescription loadDescription);
 
 	//<editor-fold defaultstate="collapsed" desc="PROCESS GENOTYPES">
 	@Override
 	public int processData(GenotypesLoadDescription loadDescription, Map<String, Object> sampleInfo) throws IOException, InvalidRangeException, InterruptedException {
 		int result = Integer.MIN_VALUE;
 
 		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
 
 		File gtFile = new File(loadDescription.getGtDirPath());
 		File[] gtFilesToImport;
 		if (gtFile.isDirectory()) {
 			gtFilesToImport = org.gwaspi.global.Utils.listFiles(loadDescription.getGtDirPath(), false);
 		} else {
 			gtFilesToImport = new File[]{new File(loadDescription.getGtDirPath())};
 		}
 
 		Map<String, Object> markerSetMap = new LinkedHashMap<String, Object>();
 
 		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
 		for (int i = 0; i < gtFilesToImport.length; i++) {
 			MetadataLoader markerSetLoader = createMetaDataLoader(gtFilesToImport[i].getPath(), loadDescription);
 			Map<String, Object> tmpMarkerMap = markerSetLoader.getSortedMarkerSetWithMetaData();
 			markerSetMap.putAll(tmpMarkerMap);
 		}
 
 		log.info("Done initializing sorted MarkerSetMap at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 		///////////// CREATE netCDF-3 FILE ////////////
 		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
 		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
 		if (!loadDescription.getDescription().isEmpty()) {
 			descSB.append("\nDescription: ");
 			descSB.append(loadDescription.getDescription());
 			descSB.append("\n");
 		}
 //		descSB.append("\nStrand: ");
 //		descSB.append(strand);
 //		descSB.append("\nGenotype encoding: ");
 //		descSB.append(gtCode);
 		descSB.append("\n");
 		descSB.append("Markers: ").append(markerSetMap.size()).append(", Samples: ").append(sampleInfo.size());
 		descSB.append("\n");
 		descSB.append(Text.Matrix.descriptionHeader2);
 		descSB.append(loadDescription.getFormat());
 		descSB.append("\n");
 		descSB.append(Text.Matrix.descriptionHeader3);
 		descSB.append("\n");
 		descSB.append(loadDescription.getGtDirPath());
 		descSB.append(" (Genotype file)\n");
 		addAdditionalBigDescriptionProperties(descSB, loadDescription);
 		if (new File(loadDescription.getSampleFilePath()).exists()) {
 			descSB.append(loadDescription.getSampleFilePath());
 			descSB.append(" (Sample Info file)\n");
 		}
 
 		//RETRIEVE CHROMOSOMES INFO
 		Map<String, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 3);
 
 		MatrixFactory matrixFactory = new MatrixFactory(
 				loadDescription.getStudyId(),
 				loadDescription.getFormat(),
 				loadDescription.getFriendlyName(),
 				descSB.toString(), // description
 				loadDescription.getGtCode(),
 				(getMatrixStrand() != null) ? getMatrixStrand() : loadDescription.getStrand(),
 				isHasDictionary(),
 				sampleInfo.size(),
 				markerSetMap.size(),
 				chrSetMap.size(),
 				loadDescription.getGtDirPath());
 
 		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();
 
 		// create the file
 		try {
 			ncfile.create();
 		} catch (IOException ex) {
 			log.error("Failed creating file " + ncfile.getLocation(), ex);
 		}
 		//log.info("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
 		//</editor-fold>
 
 		//<editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
 		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
 		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(sampleInfo, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
 
 		int[] sampleOrig = new int[]{0, 0};
 		try {
 			ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
 		} catch (IOException ex) {
 			log.error("Failed writing file", ex);
 		} catch (InvalidRangeException ex) {
 			log.error(null, ex);
 		}
 		samplesD2 = null;
 		log.info("Done writing SampleSet to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 		// WRITE RSID & MARKERID METADATA FROM METADATAMap
 		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 1, cNetCDF.Strides.STRIDE_MARKER_NAME);
 
 		int[] markersOrig = new int[]{0, 0};
 		try {
 			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
 		} catch (IOException ex) {
 			log.error("Failed writing file", ex);
 		} catch (InvalidRangeException ex) {
 			log.error(null, ex);
 		}
 		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
 		try {
 			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
 		} catch (IOException ex) {
 			log.error("Failed writing file", ex);
 		} catch (InvalidRangeException ex) {
 			log.error(null, ex);
 		}
 		log.info("Done writing MarkerId and RsId to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
 		//Chromosome location for each marker
 		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 2, cNetCDF.Strides.STRIDE_CHR);
 
 		try {
 			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
 		} catch (IOException ex) {
 			log.error("Failed writing file", ex);
 		} catch (InvalidRangeException ex) {
 			log.error(null, ex);
 		}
 		log.info("Done writing chromosomes to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 		// Set of chromosomes found in matrix along with number of markersinfo
 		org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(ncfile, chrSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
 
 		// Number of marker per chromosome & max pos for each chromosome
 		int[] columns = new int[]{0, 1, 2, 3};
 		org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(ncfile, chrSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);
 
 
 		// WRITE POSITION METADATA FROM ANNOTATION FILE
 		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 3, cNetCDF.Strides.STRIDE_POS);
 		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(markerSetMap, 3);
 		int[] posOrig = new int[1];
 		try {
 			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
 		} catch (IOException ex) {
 			log.error("Failed writing file", ex);
 		} catch (InvalidRangeException ex) {
 			log.error(null, ex);
 		}
 		log.info("Done writing positions to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 		//WRITE CUSTOM ALLELES METADATA FROM ANNOTATION FILE
 		if (markersD2ItemNb != -1) {
 		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, markersD2ItemNb, cNetCDF.Strides.STRIDE_GT);
 		try {
 			ncfile.write(markersD2Variables, markersOrig, markersD2);
 		} catch (IOException ex) {
 			log.error("Failed writing file", ex);
 		} catch (InvalidRangeException ex) {
 			log.error(null, ex);
 		}
 		log.info("Done writing forward alleles to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 		}
 
 		// WRITE GT STRAND FROM ANNOTATION FILE
 		int[] gtOrig = new int[]{0, 0};
 		String strandFlag;
 		switch (loadDescription.getStrand()) {
 			case PLUS:
 				strandFlag = StrandFlags.strandPLS;
 				break;
 			case MINUS:
 				strandFlag = StrandFlags.strandMIN;
 				break;
 			case FWD:
 				strandFlag = StrandFlags.strandFWD;
 				break;
 			case REV:
 				strandFlag = StrandFlags.strandREV;
 				break;
 			default:
 				strandFlag = StrandFlags.strandUNK;
 				break;
 		}
 		for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
 			entry.setValue(strandFlag);
 		}
 		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(markerSetMap, cNetCDF.Strides.STRIDE_STRAND);
 
 		try {
 			ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
 		} catch (IOException ex) {
 			log.error("Failed writing file", ex);
 		} catch (InvalidRangeException ex) {
 			log.error(null, ex);
 		}
 		markersD2 = null;
 		log.info("Done writing strand info to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 
 		// </editor-fold>
 
 		// <editor-fold defaultstate="collapsed" desc="MATRIX GENOTYPES LOAD ">
 
 		int sampleIndex = 0;
 		for (String sampleId : sampleInfo.keySet()) {
 			//PURGE MarkerIdMap
 			for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
 				entry.setValue(cNetCDF.Defaults.DEFAULT_GT);
 			}
 
 			for (int i = 0; i < gtFilesToImport.length; i++) {
 			try {
 				loadIndividualFiles(
 						new File(loadDescription.getGtDirPath()),
 						sampleId,
 						markerSetMap);
 
 				// WRITING GENOTYPE DATA INTO netCDF FILE
 				org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, markerSetMap, sampleIndex);
 
 				if (Thread.interrupted()) {
 					throw new InterruptedException();
 				}
 			} catch (IOException ex) {
 				log.warn(null, ex);
 			} catch (InvalidRangeException ex) {
 				log.warn(null, ex);
 			} catch (InterruptedException ex) {
 				log.warn(null, ex);
 				// TODO Write some cleanup code for when thread has been interrupted
 			}
 			}
 
 			sampleIndex++;
 			if (sampleIndex == 1) {
 				log.info(Text.All.processing);
 			} else if (sampleIndex % 100 == 0) {
 				log.info("Done processing sample Nº{} at {}", sampleIndex, org.gwaspi.global.Utils.getMediumDateTimeAsString());
 			}
 		}
 
 		log.info("Done writing genotypes to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());
 		// </editor-fold>
 
 		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
 		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
 		try {
 			//GUESS GENOTYPE ENCODING
 			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
 			Index index = guessedGTCodeAC.getIndex();
 			guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
 			int[] origin = new int[]{0, 0};
 			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);
 
 			descSB.append("Genotype encoding: ");
 			descSB.append(guessedGTCode);
 			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
 			db.updateTable(cDBGWASpi.SCH_MATRICES,
 					cDBMatrix.T_MATRICES,
 					new String[]{cDBMatrix.f_DESCRIPTION},
 					new Object[]{descSB.toString()},
 					new String[]{cDBMatrix.f_ID},
 					new Object[]{matrixFactory.getMatrixMetaData().getMatrixId()});
 
 			//CLOSE FILE
 			ncfile.close();
 			result = matrixFactory.getMatrixMetaData().getMatrixId();
 		} catch (IOException ex) {
 			log.error("Failed creating file " + ncfile.getLocation(), ex);
 		}
 
 		logAsWhole(startTime, loadDescription.getStudyId(), loadDescription.getGtDirPath(), loadDescription.getFormat(), loadDescription.getFriendlyName(), loadDescription.getDescription());
 
 		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
 		return result;
 	}
 
 	public abstract void loadIndividualFiles(File file,
 			String currSampleId,
 			Map<String, Object> markerSetMap)
 			throws IOException, InvalidRangeException;
 
 	//</editor-fold>
 
 	static void logAsWhole(String startTime, int studyId, String dirPath, ImportFormat format, String matrixName, String description) throws IOException {
 		// LOG OPERATION IN STUDY HISTORY
 		StringBuilder operation = new StringBuilder("\nLoaded raw " + format + " genotype data in path " + dirPath + ".\n");
 		operation.append("Start Time: ").append(startTime).append("\n");
 		operation.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append(".\n");
 		operation.append("Data stored in matrix ").append(matrixName).append(".\n");
 		operation.append("Description: ").append(description).append(".\n");
 		org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
 	}
 }
