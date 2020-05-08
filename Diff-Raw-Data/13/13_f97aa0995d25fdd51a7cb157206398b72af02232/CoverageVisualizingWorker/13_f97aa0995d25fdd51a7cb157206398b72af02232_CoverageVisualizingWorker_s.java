 package ui.utils;
 
 import base.SourceLocation;
 import io.ConsoleWriter;
 import ui.ApplicationForm;
 import ui.ExtendedException;
 import ui.SimpleLock;
 import ui.base.HLDD2VHDLMapping;
 import ui.base.NodeItem;
 import ui.base.SplitCoverage;
 import ui.base.VariableItem;
 import ui.fileViewer.LinesStorage;
 import ui.graphics.CoveragePanel;
 import ui.io.CoverageReader;
 import ui.io.DiagnosisReader;
 import ui.io.HLDD2VHDLMappingReader;
 import ui.utils.uiWithWorker.TaskSwingWorker;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.LinkedList;
 
 /**
  * @author Anton Chepurov
  */
 public class CoverageVisualizingWorker extends TaskSwingWorker {
 
 	private final File vhdlFile;
 	private final File covFile;
 	private final File dgnFile;
 	private final File mappingFile;
 	private final ApplicationForm applicationForm;
 	private final ConsoleWriter consoleWriter;
 	private final SimpleLock simpleLock;
 
 	public CoverageVisualizingWorker(File vhdlFile, File covFile, File dgnFile, File mappingFile, ApplicationForm applicationForm, ConsoleWriter consoleWriter, SimpleLock simpleLock) {
 		this.vhdlFile = vhdlFile;
 		this.covFile = covFile;
 		this.dgnFile = dgnFile;
 		this.mappingFile = mappingFile;
 		this.applicationForm = applicationForm;
 		this.consoleWriter = consoleWriter;
 		this.simpleLock = simpleLock;
 		executableRunnable = createRunnable();
 	}
 
 	private Runnable createRunnable() {
 		return new Runnable() {
 			public void run() {
 				try {
 					/* Read HLDD-2-VHDL mapping */
 					consoleWriter.write("Mapping HLDD to VHDL...");
 					HLDD2VHDLMapping hldd2VHDLMapping = new HLDD2VHDLMappingReader(mappingFile).getMapping();
 					consoleWriter.done();
 
 					/* Read COV file */
 					CoverageReader coverageReader = null;
 					Collection<NodeItem> uncoveredNodeItems = null;
 					if (covFile != null) {
 						consoleWriter.write("Reading coverage file...");
 						coverageReader = new CoverageReader(covFile);
 						uncoveredNodeItems = coverageReader.getUncoveredNodeItems();
 						consoleWriter.done();
 					}
 
 					/* Read DGN file */
 					SourceLocation sourceCandidates1 = null;
 					SourceLocation sourceCandidates2 = null;
 					if (dgnFile != null) {
 						consoleWriter.write("Reading diagnosis file...");
 						DiagnosisReader diagnosisReader = new DiagnosisReader(dgnFile);
 						Collection<VariableItem> candidates1 = diagnosisReader.getCandidates1();
 						Collection<VariableItem> candidates2 = diagnosisReader.getCandidates2();
 						sourceCandidates1 = hldd2VHDLMapping.getSourceFor(candidates1);
 						sourceCandidates2 = hldd2VHDLMapping.getSourceFor(candidates2);
 						consoleWriter.done();
 					}
 
 					/* Extract lines for uncovered nodes */
 					SourceLocation allSources = hldd2VHDLMapping.getAllSources();
 					SourceLocation uncoveredSources = uncoveredNodeItems != null
 							? hldd2VHDLMapping.getSourceFor(uncoveredNodeItems) : null;
 
 					LinkedList<File> allSourceFiles = new LinkedList<File>(allSources.getFiles());
 					if (!allSourceFiles.contains(vhdlFile)) {
 						allSourceFiles.add(vhdlFile); // just in case it is not in allSources list (no transitions in it, only component instantiations)
 					}
 					java.util.Collections.sort(allSourceFiles);
 					LinkedList<SplitCoverage> vhdlNodeCoverages = new LinkedList<SplitCoverage>();
 					for (File sourceFile : allSourceFiles) {
 						/* Add tab to the FileViewer */
						if (coverageReader.hasNodeCoverage() || dgnFile != null) {
 							LinesStorage linesStorage = buildLinesStorage(sourceFile, uncoveredSources, sourceCandidates1, sourceCandidates2);
 							applicationForm.addFileViewerTabFromFile(sourceFile, linesStorage, null);
 						}
 						/* Add VHDL coverage bar */
						if (coverageReader.hasNodeCoverage()) {
 							int total = 0;
 							if (allSources.hasFile(sourceFile)) {
 								Collection<Integer> lines = allSources.getLinesForFile(sourceFile);
 								total = lines != null ? lines.size() : 0;
 							}
 							int uncovered = uncoveredSources != null && uncoveredSources.hasFile(sourceFile) ?
 									uncoveredSources.getLinesForFile(sourceFile).size() : 0;
 							vhdlNodeCoverages.add(new SplitCoverage(total - uncovered, total, sourceFile.getName(), null));
 						}
 					}
 
 					/* Add coverage */
 					if (covFile != null) {
						if (coverageReader.hasNodeCoverage()) {
 							int total = allSources.getTotalLinesNum();
 							int uncovered = uncoveredSources == null ? 0 : uncoveredSources.getTotalLinesNum();
 							vhdlNodeCoverages.addFirst(new SplitCoverage(total - uncovered, total, SplitCoverage.STATEMENT_COVERAGE,
 									"Coverage for top level: " + vhdlFile.getPath()));
 						}
 						CoveragePanel coveragePanel = new CoveragePanel(
 								coverageReader.getNodeCoverage(),
 								coverageReader.getEdgeCoverage(),
 								coverageReader.getToggleCoverage(),
 								coverageReader.getConditionCoverage(),
 								vhdlNodeCoverages);
 						applicationForm.addCoverage(generateTabTitle(covFile), covFile.getAbsolutePath(), coveragePanel);
 					}
 
 					isProcessFinished = true;
 				} catch (Exception e) {
 					occurredException = ExtendedException.create(e);
 					isProcessFinished = false;
 				}
 
 			}
 		};
 	}
 
 	private LinesStorage buildLinesStorage(File sourceFile,
 										   SourceLocation uncoveredSources,
 										   SourceLocation sourceCandidates1,
 										   SourceLocation sourceCandidates2) {
 		LinesStorage.Builder builder = new LinesStorage.Builder();
 		if (uncoveredSources != null) {
 			builder.nodes(uncoveredSources.getLinesForFile(sourceFile));
 		}
 		if (sourceCandidates1 != null && sourceCandidates1.hasFile(sourceFile)) {
 			builder.candidates1(sourceCandidates1.getLinesForFile(sourceFile));
 		}
 		if (sourceCandidates2 != null && sourceCandidates2.hasFile(sourceFile)) {
 			builder.candidates2(sourceCandidates2.getLinesForFile(sourceFile));
 		}
 		return builder.build();
 	}
 
 	private static String generateTabTitle(File aFile) {
 		StringBuilder sb = new StringBuilder(aFile.getName());
 		sb.delete(sb.lastIndexOf("."), sb.length());
 		sb.append(":Coverage");
 		return sb.toString();
 	}
 
 	protected Boolean doInBackground() {
 		/* Disable UI */
 		enableUI(false);
 
 		return super.doInBackground();
 	}
 
 	protected void done() {
 		/* Enable UI */
 		enableUI(true);
 
 		super.done();
 
 		simpleLock.unlock();
 	}
 
 	private void enableUI(boolean enable) {
 		applicationForm.enableCoverageHighlighter(enable);
 	}
 }
