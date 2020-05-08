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
 
 package org.gwaspi.threadbox;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import org.gwaspi.constants.cImport.ImportFormat;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.global.Text;
 import org.gwaspi.model.GWASpiExplorerNodes;
 import org.gwaspi.model.MatrixKey;
 import org.gwaspi.model.OperationMetadata;
 import org.gwaspi.model.OperationsList;
 import org.gwaspi.model.SampleInfo;
 import org.gwaspi.model.SampleInfoList;
 import org.gwaspi.netCDF.operations.GWASinOneGOParams;
 import org.gwaspi.netCDF.operations.OperationManager;
 import org.gwaspi.reports.OutputAssociation;
 import org.gwaspi.reports.OutputTrendTest;
 import org.gwaspi.samples.SamplesParserManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Threaded_GWAS extends CommonRunnable {
 
 	private MatrixKey matrixKey;
 	private File phenotypeFile;
 	private GWASinOneGOParams gwasParams;
 
 	public Threaded_GWAS(
 			MatrixKey matrixKey,
 			File phenotypeFile,
 			GWASinOneGOParams gwasParams)
 	{
 		super("GWAS", "GWAS", "GWAS on Matrix ID: " + matrixKey.getMatrixId(), "GWAS");
 
 		this.matrixKey = matrixKey;
 		this.phenotypeFile = phenotypeFile;
 		this.gwasParams = gwasParams;
 	}
 
 	protected Logger createLog() {
 		return LoggerFactory.getLogger(Threaded_GWAS.class);
 	}
 
 	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {
 
 		List<OperationMetadata> operations = OperationsList.getOperationsList(matrixKey.getMatrixId());
 		int sampleQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.SAMPLE_QA);
 		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);
 
 		checkRequired(gwasParams);
 
 		//<editor-fold defaultstate="expanded" desc="PRE-GWAS PROCESS">
 		// GENOTYPE FREQ.
 		int censusOpId = Integer.MIN_VALUE;
 		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
 			if (phenotypeFile != null && phenotypeFile.exists() && phenotypeFile.isFile()) { // BY EXTERNAL PHENOTYPE FILE
 				// use Sample Info file affection state
 				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.scanSampleInfoAffectionStates(phenotypeFile.getPath());
 
 				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
 						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
 				{
 					getLog().info("Updating Sample Info in DB");
 					Collection<SampleInfo> sampleInfos = SamplesParserManager.scanSampleInfo(
 							matrixKey.getStudyId(),
 							ImportFormat.GWASpi,
 							phenotypeFile.getPath());
					SampleInfoList.insertSampleInfos(matrixKey.getMatrixId(), sampleInfos); // FIXME this should be studyId, not MatrixId!!
 
 					String censusName = gwasParams.getFriendlyName() + " using " + phenotypeFile.getName();
 					censusOpId = OperationManager.censusCleanMatrixMarkersByPhenotypeFile(
 							matrixKey.getMatrixId(),
 							sampleQAOpId,
 							markersQAOpId,
 							gwasParams.getDiscardMarkerMisRatVal(),
 							gwasParams.isDiscardGTMismatches(),
 							gwasParams.getDiscardSampleMisRatVal(),
 							gwasParams.getDiscardSampleHetzyRatVal(),
 							censusName,
 							phenotypeFile);
 
 					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
 				} else {
 					getLog().warn(Text.Operation.warnAffectionMissing);
 				}
 			} else { // BY DB AFFECTION
 				// use Sample Info file affection state
 				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.getDBAffectionStates(matrixKey.getMatrixId());
 				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
 						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
 				{
 					String censusName = gwasParams.getFriendlyName() + " using " + cNetCDF.Defaults.DEFAULT_AFFECTION;
 					censusOpId = OperationManager.censusCleanMatrixMarkers(
 							matrixKey.getMatrixId(),
 							sampleQAOpId,
 							markersQAOpId,
 							gwasParams.getDiscardMarkerMisRatVal(),
 							gwasParams.isDiscardGTMismatches(),
 							gwasParams.getDiscardSampleMisRatVal(),
 							gwasParams.getDiscardSampleHetzyRatVal(),
 							censusName);
 
 					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
 				} else {
 					getLog().warn(Text.Operation.warnAffectionMissing);
 				}
 			}
 
 			GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixKey.getMatrixId(), censusOpId);
 		}
 
 		int hwOpId = checkPerformHW(thisSwi, censusOpId);
 		//</editor-fold>
 
 		performGWAS(gwasParams, matrixKey.getMatrixId(), thisSwi, markersQAOpId, censusOpId, hwOpId);
 	}
 
 	static int checkPerformHW(SwingWorkerItem thisSwi, int censusOpId) throws Exception {
 
 		// HW ON GENOTYPE FREQ.
 		int hwOpId = Integer.MIN_VALUE;
 		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)
 				&& censusOpId != Integer.MIN_VALUE)
 		{
 			hwOpId = OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
 			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
 		}
 
 		return hwOpId;
 	}
 
 	static void checkRequired(GWASinOneGOParams gwasParams) {
 
 		// CHECK IF GWAS IS REQUIRED AND IF AFFECTIONS IS AVAILABLE
 		if (!gwasParams.isDiscardMarkerByMisRat()) {
 			gwasParams.setDiscardMarkerMisRatVal(1);
 		}
 		if (!gwasParams.isDiscardMarkerByHetzyRat()) {
 			gwasParams.setDiscardMarkerHetzyRatVal(1);
 		}
 		if (!gwasParams.isDiscardSampleByMisRat()) {
 			gwasParams.setDiscardSampleMisRatVal(1);
 		}
 		if (!gwasParams.isDiscardSampleByHetzyRat()) {
 			gwasParams.setDiscardSampleHetzyRatVal(1);
 		}
 	}
 
 	static void performGWAS(GWASinOneGOParams gwasParams, int matrixId, SwingWorkerItem thisSwi, int markersQAOpId, int censusOpId, int hwOpId) throws Exception {
 		// ASSOCIATION TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
 		if (gwasParams.isPerformAssociationTests()
 				&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
 				&& censusOpId != Integer.MIN_VALUE
 				&& hwOpId != Integer.MIN_VALUE)
 		{
 			boolean allelic = gwasParams.isPerformAllelicTests();
 
 			OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);
 
 			if (gwasParams.isDiscardMarkerHWCalc()) {
 				gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getOpSetSize());
 			}
 
 			int assocOpId = OperationManager.performCleanAssociationTests(
 					matrixId,
 					censusOpId,
 					hwOpId,
 					gwasParams.getDiscardMarkerHWTreshold(),
 					allelic);
 			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);
 
 			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
 			if (assocOpId != Integer.MIN_VALUE) {
 				new OutputAssociation(allelic).writeReportsForAssociationData(assocOpId);
 				GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
 			}
 		}
 
 		// TREND TESTS (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
 		if (gwasParams.isPerformTrendTests()
 				&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
 				&& censusOpId != Integer.MIN_VALUE
 				&& hwOpId != Integer.MIN_VALUE) {
 
 			OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpId);
 
 			if (gwasParams.isDiscardMarkerHWCalc()) {
 				gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getOpSetSize());
 			}
 
 			int trendOpId = OperationManager.performCleanTrendTests(
 					matrixId,
 					censusOpId,
 					hwOpId, gwasParams.getDiscardMarkerHWTreshold());
 			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, trendOpId);
 
 			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
 			if (trendOpId != Integer.MIN_VALUE) {
 				OutputTrendTest.writeReportsForTrendTestData(trendOpId);
 				GWASpiExplorerNodes.insertReportsUnderOperationNode(trendOpId);
 			}
 		}
 	}
 }
