 package org.gwaspi.reports;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.gwaspi.constants.cExport;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.global.Config;
 import org.gwaspi.global.Text;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.OperationMetadata;
 import org.gwaspi.model.OperationsList;
 import org.gwaspi.model.Report;
 import org.gwaspi.model.ReportsList;
 import org.gwaspi.netCDF.markers.MarkerSet_opt;
 import org.gwaspi.netCDF.operations.OperationSet;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.CombinedRangeXYPlot;
 import org.jfree.chart.plot.XYPlot;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.nc2.NetcdfFile;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class OutputAllelicAssociation {
 
	private static final Logger log = LoggerFactory.getLogger(OperationSet.class);
 
 	private OutputAllelicAssociation() {
 	}
 
 	public static boolean writeReportsForAssociationData(int opId) throws IOException {
 		boolean result = false;
 		Operation op = OperationsList.getById(opId);
 
 		org.gwaspi.global.Utils.createFolder(Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, ""), "STUDY_" + op.getStudyId());
 		//String manhattanName = "mnhtt_"+outName;
 		String prefix = ReportsList.getReportNamePrefix(op);
 		String manhattanName = prefix + "manhtt";
 
 		log.info(Text.All.processing);
 		if (writeManhattanPlotFromAssociationData(opId, manhattanName, 4000, 500)) {
 			result = true;
 			ReportsList.insertRPMetadata(new Report(
 					Integer.MIN_VALUE,
 					"Allelic assoc. Manhattan Plot",
 					manhattanName + ".png",
 					OPType.MANHATTANPLOT.toString(),
 					op.getParentMatrixId(),
 					opId,
 					"Allelic Association Manhattan Plot",
 					op.getStudyId()));
 			log.info("Saved Allelic Association Manhattan Plot in reports folder"); // FIXME log system already adds time
 		}
 		//String qqName = "qq_"+outName;
 		String qqName = prefix + "qq";
 		if (result && writeQQPlotFromAssociationData(opId, qqName, 500, 500)) {
 			result = true;
 			ReportsList.insertRPMetadata(new Report(
 					Integer.MIN_VALUE,
 					"Allelic assoc. QQ Plot",
 					qqName + ".png",
 					OPType.QQPLOT.toString(),
 					op.getParentMatrixId(),
 					opId,
 					"Allelic Association QQ Plot",
 					op.getStudyId()));
 
 			log.info("Saved Allelic Association QQ Plot in reports folder"); // FIXME log system already adds time
 		}
 		//String assocName = "assoc_"+outName;
 		String assocName = prefix;
 		if (result && createSortedAssociationReport(opId, assocName)) {
 			result = true;
 			ReportsList.insertRPMetadata(new Report(
 					Integer.MIN_VALUE,
 					"Allelic Association Tests Values",
 					assocName + ".txt",
 					OPType.ALLELICTEST.toString(),
 					op.getParentMatrixId(),
 					opId,
 					"Allelic Association Tests Values",
 					op.getStudyId()));
 
 			org.gwaspi.global.Utils.sysoutCompleted("Allelic Association Reports & Charts");
 		}
 
 		return result;
 	}
 
 	public static boolean writeManhattanPlotFromAssociationData(int opId, String outName, int width, int height) throws IOException {
 		boolean result = false;
 		//Generating XY scatter plot with loaded data
 		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(opId, cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR);
 
 		JFreeChart chart = new JFreeChart("P value", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
 
 		//CHART BACKGROUD COLOR
 		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); //Hue, saturation, brightness
 
 		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 		int pointNb = rdOPMetadata.getOpSetSize();
 		int picWidth = 4000;
 		if (pointNb < 1000) {
 			picWidth = 600;
 		} else if (pointNb < 1E4) {
 			picWidth = 1000;
 		} else if (pointNb < 1E5) {
 			picWidth = 1500;
 		} else if (pointNb < 5E5) {
 			picWidth = 2000;
 		}
 
 		String imagePath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/" + outName + ".png";
 		try {
 			ChartUtilities.saveChartAsPNG(new File(imagePath),
 					chart,
 					picWidth,
 					height);
 			result = true;
 		} catch (IOException ex) {
 			log.error("Problem occurred creating chart", ex);
 		}
 
 		return result;
 	}
 
 	public static boolean writeQQPlotFromAssociationData(int opId, String outName, int width, int height) throws IOException {
 		boolean result = false;
 		//Generating XY scatter plot with loaded data
 		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(opId, cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR, 1);
 
 		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);
 
 		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 		String imagePath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/" + outName + ".png";
 		try {
 			ChartUtilities.saveChartAsPNG(new File(imagePath),
 					chart,
 					width,
 					height);
 			result = true;
 		} catch (IOException ex) {
 			log.error("Problem occurred creating chart", ex);
 		}
 
 		return result;
 	}
 
 	public static boolean createSortedAssociationReport(int opId, String reportName) throws IOException {
 		boolean result;
 
 		try {
 			Map<String, Object> unsortedMarkerIdAssocValsMap = GenericReportGenerator.getAnalysisVarData(opId, cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR);
 			Map<String, Object> unsortedMarkerIdPvalMap = new LinkedHashMap<String, Object>();
 			for (Map.Entry<String, Object> entry : unsortedMarkerIdAssocValsMap.entrySet()) {
 				String key = entry.getKey();
 				double[] values = (double[]) entry.getValue();
 				unsortedMarkerIdPvalMap.put(key, values[1]);
 			}
 
 			Map<String, Object> sortingMarkerSetMap = ReportsList.getSortedMarkerSetByDoubleValue(unsortedMarkerIdPvalMap);
 			if (unsortedMarkerIdPvalMap != null) {
 				unsortedMarkerIdPvalMap.clear();
 			}
 
 			String sep = cExport.separator_REPORTS;
 			OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
 			rdInfoMarkerSet.initFullMarkerIdSetMap();
 
 			//WRITE HEADER OF FILE
 			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tX²\tPval\tOR\n";
 			String reportNameExt = reportName + ".txt";
 			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";
 
 			// WRITE MARKERSET RSID
 			//infoMatrixMarkerSetMap = rdInfoMarkerSet.appendVariableToMarkerSetMapValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_RSID, sep);
 			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
 			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(key);
 				entry.setValue(value);
 			}
 			ReportWriter.writeFirstColumnToReport(reportPath, reportNameExt, header, sortingMarkerSetMap, true);
 
 			// WRITE MARKERSET CHROMOSOME
 			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
 			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(key);
 				entry.setValue(value);
 			}
 			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetMap, false, false);
 
 			// WRITE MARKERSET POS
 			//infoMatrixMarkerSetMap = rdInfoMarkerSet.appendVariableToMarkerSetMapValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
 			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
 			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(key);
 				entry.setValue(value);
 			}
 			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetMap, false, false);
 
 			// WRITE KNOWN ALLELES FROM QA
 			// get MARKER_QA Operation
 			List<Object[]> operationsAL = OperationsList.getMatrixOperations(rdOPMetadata.getParentMatrixId());
 			int markersQAopId = Integer.MIN_VALUE;
 			for (int i = 0; i < operationsAL.size(); i++) {
 				Object[] element = operationsAL.get(i);
 				if (element[1].toString().equals(OPType.MARKER_QA.toString())) {
 					markersQAopId = (Integer) element[0];
 				}
 			}
 			if (markersQAopId != Integer.MIN_VALUE) {
 				OperationMetadata qaMetadata = OperationsList.getOperationMetadata(markersQAopId);
 				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());
 
 				OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(), markersQAopId);
 				Map<String, Object> opMarkerSetMap = rdOperationSet.getOpSetMap();
 
 				// MINOR ALLELE
 				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
 				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
 					String key = entry.getKey();
 					Object minorAllele = opMarkerSetMap.get(key);
 					entry.setValue(minorAllele);
 				}
 
 				// MAJOR ALLELE
 				rdOperationSet.fillMapWithDefaultValue(opMarkerSetMap, "");
 				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
 				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
 					String key = entry.getKey();
 					Object minorAllele = entry.getValue();
 					entry.setValue(minorAllele + sep + opMarkerSetMap.get(key));
 				}
 			}
 			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(key);
 				entry.setValue(value);
 			}
 			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetMap, false, false);
 
 			// WRITE DATA TO REPORT
 			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
 				String key = entry.getKey();
 				Object value = unsortedMarkerIdAssocValsMap.get(key);
 				entry.setValue(value);
 			}
 			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetMap, true, false);
 
 			result = true;
 		} catch (IOException ex) {
 			result = false;
 			log.warn(null, ex);
 		}
 
 		return result;
 	}
 }
