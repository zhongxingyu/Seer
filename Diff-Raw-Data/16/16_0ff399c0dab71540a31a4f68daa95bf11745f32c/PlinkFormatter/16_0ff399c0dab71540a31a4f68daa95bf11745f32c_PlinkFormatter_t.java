 package org.gwaspi.netCDF.exporter;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import org.gwaspi.constants.cExport;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.model.MatrixMetadata;
 import org.gwaspi.model.SampleInfo;
 import org.gwaspi.netCDF.markers.MarkerSet_opt;
 import org.gwaspi.samples.SampleSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.nc2.NetcdfFile;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class PlinkFormatter implements Formatter {
 
 	private final Logger log = LoggerFactory.getLogger(PlinkFormatter.class);
 
 	public boolean export(
 			String exportPath,
 			MatrixMetadata rdMatrixMetadata,
 			MarkerSet_opt rdMarkerSet,
 			SampleSet rdSampleSet,
 			Map<String, Object> rdSampleSetMap,
 			String phenotype)
 			throws IOException
 	{
 		File exportDir = new File(exportPath);
 		if (!exportDir.exists() || !exportDir.isDirectory()) {
 			return false;
 		}
 
 		boolean result = false;
 		String sep = cExport.separator_PLINK;
		String sepBig = cExport.separator_PLINK_big;
 		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
 		rdMarkerSet.initFullMarkerIdSetMap();
 
 		try {
 			//<editor-fold defaultstate="collapsed" desc="PED FILE">
 			FileWriter pedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".ped");
 			BufferedWriter pedBW = new BufferedWriter(pedFW);
 
 			// Iterate through all samples
 			int sampleNb = 0;
 			for (String sampleId : rdSampleSetMap.keySet()) {
 				SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());
 
 				String familyId = sampleInfo.getFamilyId();
 				String fatherId = sampleInfo.getFatherId();
 				String motherId = sampleInfo.getMotherId();
 				String sex = sampleInfo.getSexStr();
 				String affection = sampleInfo.getAffectionStr();
 
 				// Iterate through all markers
 				rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleNb);
 				StringBuilder genotypes = new StringBuilder();
 				for (Object value : rdMarkerSet.getMarkerIdSetMap().values()) {
 					byte[] tempGT = (byte[]) value;
					genotypes.append(sepBig);
 					genotypes.append(new String(new byte[]{tempGT[0]}));
 					genotypes.append(sep);
 					genotypes.append(new String(new byte[]{tempGT[1]}));
 				}
 
 				// Family ID
 				// Individual ID
 				// Paternal ID
 				// Maternal ID
 				// Sex (1=male; 2=female; other=unknown)
 				// Affection
 				// Genotypes
 
 				StringBuilder line = new StringBuilder();
 				line.append(familyId);
				line.append(sepBig);

 				line.append(sampleId);
 				line.append(sep);
 				line.append(fatherId);
 				line.append(sep);
 				line.append(motherId);
 				line.append(sep);
 				line.append(sex);
 				line.append(sep);
 				line.append(affection);

 				line.append(genotypes);
 
 				pedBW.append(line);
 				pedBW.append("\n");
 				pedBW.flush();
 
 				sampleNb++;
 				if (sampleNb % 100 == 0) {
 					log.info("Samples exported to PED file: {}", sampleNb);
 				}
 			}
 			log.info("Samples exported to PED file: {}", sampleNb);
 			pedBW.close();
 			pedFW.close();
 			//</editor-fold>
 
 			//<editor-fold defaultstate="collapsed" desc="MAP FILE">
 			FileWriter mapFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".map");
 			BufferedWriter mapBW = new BufferedWriter(mapFW);
 
 			// MAP files
 			//     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
 			//     rs# or snp identifier
 			//     Genetic distance (morgans)
 			//     Base-pair position (bp units)
 
 			// PURGE MARKERSET
 			rdMarkerSet.fillWith("");
 
 			// MARKERSET CHROMOSOME
 			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
 
 			// MARKERSET RSID
 			rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERS_RSID, sep);
 
 			// DEFAULT GENETIC DISTANCE = 0
 			for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetMap().entrySet()) {
 				StringBuilder value = new StringBuilder(entry.getValue().toString());
 				value.append(sep);
 				value.append("0");
 				entry.setValue(value.toString());
 			}
 
 			// MARKERSET POSITION
 			rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERS_POS, sep);
 			int markerNb = 0;
 			for (Object pos : rdMarkerSet.getMarkerIdSetMap().values()) {
 				mapBW.append(pos.toString());
 				mapBW.append("\n");
 				markerNb++;
 			}
 
 			log.info("Markers exported to MAP file: {}", markerNb);
 
 			mapBW.close();
 			mapFW.close();
 			//</editor-fold>
 
 			result = true;
 		} catch (IOException ex) {
 			log.error(null, ex);
 		} finally {
 			if (null != rdNcFile) {
 				try {
 					rdNcFile.close();
 				} catch (IOException ex) {
 					log.warn("Cannot close file: " + rdNcFile, ex);
 				}
 			}
 		}
 
 		return result;
 	}
 }
