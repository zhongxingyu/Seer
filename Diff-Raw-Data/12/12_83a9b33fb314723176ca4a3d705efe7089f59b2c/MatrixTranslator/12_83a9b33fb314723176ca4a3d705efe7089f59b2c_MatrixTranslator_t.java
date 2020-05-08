 /*
  * Copyright (C) 2013 Universitat Pompeu Fabra
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.gwaspi.netCDF.operations;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
 import org.gwaspi.global.Text;
 import org.gwaspi.model.MarkerKey;
 import org.gwaspi.model.MatricesList;
 import org.gwaspi.model.MatrixKey;
 import org.gwaspi.model.MatrixMetadata;
 import org.gwaspi.model.SampleKey;
 import org.gwaspi.model.StudyKey;
 import org.gwaspi.netCDF.markers.MarkerSet;
 import org.gwaspi.netCDF.matrices.MatrixFactory;
 import org.gwaspi.samples.SampleSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.ArrayChar;
 import ucar.ma2.Index;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.NetcdfFileWriteable;
 
 public class MatrixTranslator {
 
 	private final Logger log = LoggerFactory.getLogger(MatrixTranslator.class);
 
 	private final MatrixKey rdMatrixKey;
 	private final int wrMatrixId;
 	private final String wrMatrixFriendlyName;
 	private final String wrMatrixDescription;
 	private final MatrixMetadata rdMatrixMetadata;
 	private final MarkerSet rdMarkerSet;
 	private final SampleSet rdSampleSet;
 	private Map<MarkerKey, byte[]> wrMarkerIdSetMap;
 	private final Map<MarkerKey, int[]> rdChrInfoSetMap;
 	private final Map<SampleKey, ?> rdSampleSetMap;
 
 	public MatrixTranslator(
 			MatrixKey rdMatrixKey,
 			String wrMatrixFriendlyName,
 			String wrMatrixDescription)
 			throws IOException, InvalidRangeException
 	{
 		// INIT EXTRACTOR OBJECTS
 		this.rdMatrixKey = rdMatrixKey;
 		this.rdMatrixMetadata = MatricesList.getMatrixMetadataById(this.rdMatrixKey);
 		this.wrMatrixId = Integer.MIN_VALUE;
 		this.wrMatrixFriendlyName = wrMatrixFriendlyName;
 		this.wrMatrixDescription = wrMatrixDescription;
 
 		this.rdMarkerSet = new MarkerSet(this.rdMatrixKey);
 		this.rdMarkerSet.initFullMarkerIdSetMap();
 
 		this.wrMarkerIdSetMap = new LinkedHashMap<MarkerKey, byte[]>();
 		this.rdChrInfoSetMap = this.rdMarkerSet.getChrInfoSetMap();
 
 		this.rdSampleSet = new SampleSet(this.rdMatrixKey);
 		this.rdSampleSetMap = this.rdSampleSet.getSampleIdSetMapByteArray();
 	}
 
 	public int translateAB12AllelesToACGT() throws InvalidRangeException, IOException {
 		int result = Integer.MIN_VALUE;
 
 		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
 		GenotypeEncoding rdMatrixGtCode = rdMatrixMetadata.getGenotypeEncoding();
 
 		if (!rdMatrixGtCode.equals(GenotypeEncoding.ACGT0)) { //Has not allready been translated
 
 			try {
 				// CREATE netCDF-3 FILE
 				StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
 				descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
				descSB.append("\nThrough Matrix translation from parent Matrix MX: ").append(rdMatrixKey.getMatrixId()).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());
 				descSB.append("\nTranslation method: AB0 or 012 to ACGT0 using the parent's dictionnary");
 				if (!wrMatrixDescription.isEmpty()) {
 					descSB.append("\n\nDescription: ");
 					descSB.append(wrMatrixDescription);
 					descSB.append("\n");
 				}
 				descSB.append("\n");
 				descSB.append("Markers: ").append(rdMatrixMetadata.getMarkerSetSize()).append(", Samples: ").append(rdMatrixMetadata.getSampleSetSize());
 				descSB.append("\nGenotype encoding: ");
 				descSB.append(GenotypeEncoding.ACGT0.toString());
 
 				MatrixFactory wrMatrixHandler = new MatrixFactory(
 						rdMatrixMetadata.getTechnology(), // technology
 						wrMatrixFriendlyName,
 						descSB.toString(), // description
 						GenotypeEncoding.ACGT0, // New matrix genotype encoding
 						rdMatrixMetadata.getStrand(),
 						rdMatrixMetadata.getHasDictionray(), // has dictionary?
 						rdSampleSet.getSampleSetSize(),
 						rdMarkerSet.getMarkerSetSize(),
 						rdChrInfoSetMap.size(),
 						rdMatrixKey, // Orig matrixId 1
 						null); // Orig matrixId 2
 
 				NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
 				try {
 					wrNcFile.create();
 				} catch (IOException ex) {
 					log.error("Failed creating file " + wrNcFile.getLocation(), ex);
 				}
 				//log.trace("Done creating netCDF handle in MatrixataTransform: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 				//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
 				// WRITING METADATA TO MATRIX
 
 				// SAMPLESET
 				ArrayChar.D2 samplesD2 = Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
 
 				int[] sampleOrig = new int[] {0, 0};
 				try {
 					wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
 				} catch (IOException ex) {
 					log.error("Failed writing file", ex);
 				} catch (InvalidRangeException ex) {
 					log.error(null, ex);
 				}
 				log.info("Done writing SampleSet to matrix");
 
 				// MARKERSET MARKERID
 				ArrayChar.D2 markersD2 = Utils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerKeys(), cNetCDF.Strides.STRIDE_MARKER_NAME);
 				int[] markersOrig = new int[] {0, 0};
 				try {
 					wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
 				} catch (IOException ex) {
 					log.error("Failed writing file", ex);
 				} catch (InvalidRangeException ex) {
 					log.error(null, ex);
 				}
 
 				// MARKERSET RSID
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
 				Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);
 
 				// MARKERSET CHROMOSOME
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
 				Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);
 
 				// Set of chromosomes found in matrix along with number of markersinfo
 				org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(wrNcFile, rdChrInfoSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
 				// Number of marker per chromosome & max pos for each chromosome
 				int[] columns = new int[]{0, 1, 2, 3};
 				org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(wrNcFile, rdChrInfoSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);
 
 				// MARKERSET POSITION
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
 				Utils.saveIntMapD1ToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapInteger(), cNetCDF.Variables.VAR_MARKERS_POS);
 
 				// MARKERSET DICTIONARY ALLELES
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
 				Map<MarkerKey, char[]> sortedBasesDicts = org.gwaspi.global.Utils.createOrderedMap(wrMarkerIdSetMap, rdMarkerSet.getMarkerIdSetMapCharArray());
 				Utils.saveCharMapValueToWrMatrix(wrNcFile, sortedBasesDicts, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);
 
 				// GENOTYPE STRAND
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
 				Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Variables.VAR_GT_STRAND, cNetCDF.Strides.STRIDE_STRAND);
 				//</editor-fold>
 
 				//<editor-fold defaultstate="expanded" desc="GENOTYPES WRITER">
 				// Get correct bases dictionary for translation
 				Map<MarkerKey, char[]> dictionnaryMap = rdMarkerSet.getDictionaryBases();
 
 				// Iterate through Samples, use Sample item position to read all Markers GTs from rdMarkerIdSetMap.
 				int sampleIndex = 0;
 				for (int i = 0; i < rdSampleSetMap.size(); i++) {
 					// Get alleles from read matrix
 					rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleIndex);
 					// Send to be translated
 					wrMarkerIdSetMap = rdMarkerSet.getMarkerIdSetMapByteArray();
 					translateCurrentSampleAB12AllelesMap(wrMarkerIdSetMap, rdMatrixGtCode, dictionnaryMap);
 
 					// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
 					Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetMap, sampleIndex);
 					if (sampleIndex % 100 == 0) {
 						log.info("Samples translated: {}", sampleIndex);
 					}
 					sampleIndex++;
 				}
 				log.info("Total Samples translated: {}", sampleIndex);
 				//</editor-fold>
 
 				// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
 				try {
 					// GUESS GENOTYPE ENCODING
 					ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
 					Index index = guessedGTCodeAC.getIndex();
 					guessedGTCodeAC.setString(index.set(0, 0), cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());
 					int[] origin = new int[] {0, 0};
 					wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);
 
 					wrNcFile.close();
 					result = wrMatrixHandler.getResultMatrixId();
 				} catch (IOException ex) {
 					log.error("Failed creating file " + wrNcFile.getLocation(), ex);
 				}
 
 				org.gwaspi.global.Utils.sysoutCompleted("Translation");
 			} catch (InvalidRangeException ex) {
 				log.error(null, ex);
 			} catch (IOException ex) {
 				log.error(null, ex);
 			} finally {
 				if (null != rdNcFile) {
 					try {
 						rdNcFile.close();
 					} catch (IOException ex) {
 						log.warn("Failed to close file " + rdNcFile.getLocation(), ex);
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	// TODO Test translate1234AllelesToACGT
 	public int translate1234AllelesToACGT() throws IOException, InvalidRangeException {
 		int result = Integer.MIN_VALUE;
 
 		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
 		GenotypeEncoding rdMatrixGTCode = rdMatrixMetadata.getGenotypeEncoding();
 
 		if (!rdMatrixGTCode.equals(GenotypeEncoding.ACGT0)) { // Has not yet been translated
 			try {
 				// CREATE netCDF-3 FILE
 				StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
 				descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
 				descSB.append("\nThrough Matrix translation from parent Matrix MX: ").append(rdMatrixMetadata.getMatrixId()).append(" - ").append(rdMatrixMetadata.getMatrixFriendlyName());
 				descSB.append("\nTranslation method: O1234 to ACGT0 using 0=0, 1=A, 2=C, 3=G, 4=T");
 				if (!wrMatrixDescription.isEmpty()) {
 					descSB.append("\n\nDescription: ");
 					descSB.append(wrMatrixDescription);
 					descSB.append("\n");
 				}
 				descSB.append("\nGenotype encoding: ");
 				descSB.append(GenotypeEncoding.ACGT0.toString());
 				descSB.append("\n");
 				descSB.append("Markers: ").append(rdMatrixMetadata.getMarkerSetSize()).append(", Samples: ").append(rdMatrixMetadata.getSampleSetSize());
 
 				MatrixFactory wrMatrixHandler = new MatrixFactory(
 						rdMatrixMetadata.getTechnology(), // technology
 						wrMatrixFriendlyName,
 						descSB.toString(), // description
 						GenotypeEncoding.ACGT0, // New matrix genotype encoding
 						rdMatrixMetadata.getStrand(),
 						rdMatrixMetadata.getHasDictionray(), // has dictionary?
 						rdSampleSet.getSampleSetSize(),
 						rdMarkerSet.getMarkerSetSize(),
 						rdChrInfoSetMap.size(),
 						rdMatrixKey, // Orig matrixId 1
 						null); // Orig matrixId 2
 
 				NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
 				try {
 					wrNcFile.create();
 				} catch (IOException ex) {
 					log.error("Failed creating file " + wrNcFile.getLocation(), ex);
 				}
 				//log.trace("Done creating netCDF handle in MatrixataTransform: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
 
 				//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
 				// WRITING METADATA TO MATRIX
 
 				// SAMPLESET
 				ArrayChar.D2 samplesD2 = Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
 
 				int[] sampleOrig = new int[]{0, 0};
 				try {
 					wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
 				} catch (IOException ex) {
 					log.error("Failed writing file", ex);
 				} catch (InvalidRangeException ex) {
 					log.error(null, ex);
 				}
 				log.info("Done writing SampleSet to matrix");
 
 				// MARKERSET MARKERID
 				ArrayChar.D2 markersD2 = Utils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerKeys(), cNetCDF.Strides.STRIDE_MARKER_NAME);
 				int[] markersOrig = new int[]{0, 0};
 				try {
 					wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
 				} catch (IOException ex) {
 					log.error("Failed writing file", ex);
 				} catch (InvalidRangeException ex) {
 					log.error(null, ex);
 				}
 
 				// MARKERSET RSID
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
 				Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);
 
 				// MARKERSET CHROMOSOME
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
 				Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);
 
 				// MARKERSET POSITION
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
 				//Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerIdSetMap, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
 				Utils.saveIntMapD1ToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapInteger(), cNetCDF.Variables.VAR_MARKERS_POS);
 
 				// MARKERSET DICTIONARY ALLELES
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
 				Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);
 
 				// GENOTYPE STRAND
 				rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
 				Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Variables.VAR_GT_STRAND, cNetCDF.Strides.STRIDE_STRAND);
 				//</editor-fold>
 
 				//<editor-fold defaultstate="expanded" desc="GENOTYPES WRITER">
 				// Get correct strand of each marker for newStrand translation
 				Map<MarkerKey, Object> markerStrandsMap = new LinkedHashMap<MarkerKey, Object>();
 				markerStrandsMap.putAll(rdChrInfoSetMap); // XXX was rdSampleSetMap instead of rdChrInfoSetMap before; but that one had the wrong type -> bug; but is this the right set (with MarkerKey's as keys)?
 
 				// Iterate through pmAllelesAndStrandsMap, use Sample item position to read all Markers GTs from rdMarkerIdSetMap.
 				int sampleNb = 0;
 				for (int i = 0; i < rdSampleSetMap.size(); i++) {
 					// Get alleles from read matrix
 					rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleNb);
 					// Send to be translated
 					wrMarkerIdSetMap = rdMarkerSet.getMarkerIdSetMapByteArray();
 					translateCurrentSample1234AllelesMap(wrMarkerIdSetMap, markerStrandsMap.keySet());
 
 					// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
 					Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrMarkerIdSetMap, sampleNb);
 
 					if (sampleNb % 100 == 0) {
 						log.info("Samples translated: {}", sampleNb);
 					}
 					sampleNb++;
 				}
 				log.info("Total Samples translated: {}", sampleNb);
 				//</editor-fold>
 
 				// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
 				try {
 					// GUESS GENOTYPE ENCODING
 					ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
 					Index index = guessedGTCodeAC.getIndex();
 					guessedGTCodeAC.setString(index.set(0, 0), cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString());
 					int[] origin = new int[] {0, 0};
 					wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);
 
 					wrNcFile.close();
 					result = wrMatrixHandler.getResultMatrixId();
 				} catch (IOException ex) {
 					log.error("Failed creating file " + wrNcFile.getLocation(), ex);
 				}
 
 				org.gwaspi.global.Utils.sysoutCompleted("Translation");
 			} catch (InvalidRangeException ex) {
 				log.error(null, ex);
 			} catch (IOException ex) {
 				log.error(null, ex);
 			} finally {
 				if (null != rdNcFile) {
 					try {
 						rdNcFile.close();
 					} catch (IOException ex) {
 						log.warn("Failed close file " + rdNcFile.getLocation(), ex);
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	private void translateCurrentSampleAB12AllelesMap(Map<MarkerKey, byte[]> codedMap, GenotypeEncoding rdMatrixType, Map<MarkerKey, char[]> dictionaryMap) {
 		byte alleleA;
 		byte alleleB;
 
 		switch (rdMatrixType) {
 			case AB0:
 				alleleA = cNetCDF.Defaults.AlleleBytes.A;
 				alleleB = cNetCDF.Defaults.AlleleBytes.B;
 				// Iterate through all markers
 				for (Map.Entry<MarkerKey, byte[]> entry : codedMap.entrySet()) {
 					MarkerKey markerKey = entry.getKey();
 					char[] basesDict = dictionaryMap.get(markerKey);
 					byte[] codedAlleles = entry.getValue();
 					byte[] transAlleles = new byte[2];
 
 					if (codedAlleles[0] == alleleA) {
 						transAlleles[0] = (byte) basesDict[0];
 					} else if (codedAlleles[0] == alleleB) {
 						transAlleles[0] = (byte) basesDict[1];
 					} else {
 						transAlleles[0] = cNetCDF.Defaults.AlleleBytes._0;
 					}
 
 					if (codedAlleles[1] == alleleA) {
 						transAlleles[1] = (byte) basesDict[0];
 					} else if (codedAlleles[1] == alleleB) {
 						transAlleles[1] = (byte) basesDict[1];
 					} else {
 						transAlleles[1] = cNetCDF.Defaults.AlleleBytes._0;
 					}
 
 					entry.setValue(transAlleles);
 				}
 				break;
 			case O12:
 				alleleA = cNetCDF.Defaults.AlleleBytes._1;
 				alleleB = cNetCDF.Defaults.AlleleBytes._2;
 
 				// Iterate through all markers
 				for (Map.Entry<MarkerKey, byte[]> entry : codedMap.entrySet()) {
 					MarkerKey markerKey = entry.getKey();
 					char[] basesDict = dictionaryMap.get(markerKey);
 					byte[] codedAlleles = entry.getValue();
 					byte[] transAlleles = new byte[2];
 
 					if (codedAlleles[0] == alleleA) {
 						transAlleles[0] = (byte) basesDict[0];
 					} else if (codedAlleles[0] == alleleB) {
 						transAlleles[0] = (byte) basesDict[1];
 					} else {
 						transAlleles[0] = cNetCDF.Defaults.AlleleBytes._0;
 					}
 
 					if (codedAlleles[1] == alleleA) {
 						transAlleles[1] = (byte) basesDict[0];
 					} else if (codedAlleles[1] == alleleB) {
 						transAlleles[1] = (byte) basesDict[1];
 					} else {
 						transAlleles[0] = cNetCDF.Defaults.AlleleBytes._0;
 					}
 
 					entry.setValue(transAlleles);
 				}
 				break;
 			default:
 				break;
 		}
 	}
 
 	private void translateCurrentSample1234AllelesMap(Map<MarkerKey, byte[]> codedMap, Collection<MarkerKey> markerStrands) {
 
 		Map<Byte, Byte> dictionary = new HashMap<Byte, Byte>();
 		dictionary.put(cNetCDF.Defaults.AlleleBytes._0, cNetCDF.Defaults.AlleleBytes._0);
 		dictionary.put(cNetCDF.Defaults.AlleleBytes._1, cNetCDF.Defaults.AlleleBytes.A);
 		dictionary.put(cNetCDF.Defaults.AlleleBytes._2, cNetCDF.Defaults.AlleleBytes.C);
 		dictionary.put(cNetCDF.Defaults.AlleleBytes._3, cNetCDF.Defaults.AlleleBytes.G);
 		dictionary.put(cNetCDF.Defaults.AlleleBytes._4, cNetCDF.Defaults.AlleleBytes.T);
 
 		// Iterate through all markers
 		for (MarkerKey markerKey : markerStrands) {
 			byte[] codedAlleles = codedMap.get(markerKey);
 
 			byte[] transAlleles = new byte[2];
 			transAlleles[0] = dictionary.get(codedAlleles[0]);
 			transAlleles[1] = dictionary.get(codedAlleles[1]);
 
 			codedMap.put(markerKey, transAlleles);
 		}
 	}
 
 	//<editor-fold defaultstate="expanded" desc="ACCESSORS">
 	public int getRdMatrixId() {
 		return rdMatrixKey.getMatrixId();
 	}
 
 	public StudyKey getStudyKey() {
 		return rdMatrixKey.getStudyKey();
 	}
 
 	public int getWrMatrixId() {
 		return wrMatrixId;
 	}
 	//</editor-fold>
 }
