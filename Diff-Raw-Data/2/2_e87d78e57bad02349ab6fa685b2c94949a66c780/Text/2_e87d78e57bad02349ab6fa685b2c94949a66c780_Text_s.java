 package org.gwaspi.global;
 
 import org.gwaspi.constants.cGlobal;
 import org.gwaspi.constants.cNetCDF;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class Text {
 
 	public static class App {
 
 		public static final String appName = "GWASpi";
 		public static final String memoryAvailable1 = "MB have been allocated to run " + Text.App.appName + ".";
 		public static final String memoryAvailable2 = "This will allow you to operate on +/- ";
 		public static final String memoryAvailable3 = " markers.";
 		public static final String memoryAvailable4 = "Caution! This amount of memory is likely not to be sufficient for\n" + Text.App.appName + " to perform optimally!";
 		public static final String initText = "You are running " + Text.App.appName + " for the fist time!\nYou will now be asked to choose a directory to store your data in.\nPlease provide a valid location on your local hard-drive with sufficient free disk space.";
 		public static final String appDescription = appName + " provides the tools and integrated know-how to quickly and safley process your GWAS data, all in a single, easy to use application.\n\nOnce your genotype data is loaded into " + appName + "'s hierarchical database (netCDF), you will be able to manage, manipulate and transform it, as well as perform basic quality controls and association studies.\n" + appName + " will also generate charts, plots and reports for you.";
 		public static final String contact = "Contact information: fernando.muniz@upf.edu";
 		public static final String newVersionAvailable = "A new version of " + appName + " is available at http://www.gwaspi.org/.";
 		public static final String newVersionIsCompatible = "This version is backward compatible with your local one. ";
 		public static final String newVersionIsUnCompatible = "This version is NOT backward compatible with your local one and may cause your older\ndatabases not to function.";
 		public static final String license = ""; // "License agreement: GPL";
 		public static final String cite = "Please cite Genome-wide Association Studies Pipeline (GWASpi): a desktop application for genome-wide SNP analysis and management, as in http://bioinformatics.oxfordjournals.org/content/early/2011/05/16/bioinformatics.btr301.abstract";
 		public static final String warnUnableToInitForFirstTime = cGlobal.APP_NAME + " was unable to create the database on the \nspecified path. Please check if the write rights are enabled on \nthe given path or if there is enough space available";
 		public static final String Tab0 = Text.App.appName + " Management";
 		public static final String Tab1 = "Process Overview";
 		public static final String Tab2 = "Study Log";
 		public static final String processOutput = "Process Output";
 		public static final String warnProcessInterrupted = "Process has been interrupted!";
 		public static final String author = "Author: Fernando Muñiz Fernandez, Angel Carreño Torres, Carlos Morcillo-Suarez and Arcadi Navarro\nINB-Node8, UPF BioEvo Dept.";
 		public static final String whitePaper = "White paper: Genome-wide Association Studies Pipeline (GWASpi): a desktop application for genome-wide SNP analysis and management (unpublished)";
 		public static final String propertiesPaths = "Properties & Paths";
 		public static final String propertyName = "Property Name";
 		public static final String propertyValue = "Value";
 		public static final String warnPropertyRGB = "You must insert an RGB (Red, Green Blue) value like '0,0,0' (black), '255,255,255' (white) or '150,0,0' (dark red).\nMinimum value for each element is 0, maximum is 255.";
 		public static final String warnMustBeNumeric = "Field must contain valid numeric value (like 0.006 or 5E-7)!";
 		public static final String exit = "Exit";
 		public static final String preferences = "Preferences";
 		public static final String changeDataDir = "Change data directory";
 		public static final String confirmCopyDataDir = "This will copy all databases and genotypes to a new directory! Proceed?";
 		public static final String warnErrorCopyData = "Couldn't perform data copy!\nDo you have write permission?";
 		public static final String infoDataDirCopyOK = "The Data directory was copied successfully!\nYou may now remove the old directory.";
 		public static final String start = "Start";
 		public static final String warnOnlyOneInstance = Text.App.appName + " could not be started!\nIs there another instance of the program running?";
 		public static final String jobsStillPending = "There are still pending jobs in the processing queue!\nShut down anyway?";
		public static final String outOfMemoryError = "ERROR: Out Of Memory!\nAre you trying to process to many SNPs for the currently allocated RAM?\nRefer to http://www.gwaspi.org/?page_id=632 for instructions on how to allocate more RAM.";
 		public static final String treeParent = "Parent";
 		public static final String treeStudyManagement = "Study Management";
 		public static final String treeStudy = "Study";
 		public static final String treeSampleInfo = "Sample Info";
 		public static final String treeMatrix = "Matrix";
 		public static final String treeOperation = "Operation";
 		public static final String treeReport = "Report";
 		public static final String treeReferenceDBs = "Ref. Databases";
 		public static final String close = "Close";
 		public static final String thanx = "Special thanks to Angel Carreño";
 
 		private App() {
 		}
 	}
 
 	public static class All {
 
 		public static final String optional = "[Optional]";
 		public static final String description = "Description";
 		public static final String nameAndDescription = "Name and Description";
 		public static final String insertDescription = "Insert Description...";
 		public static final String saveDescription = "Save Description";
 		public static final String mainProject = "Main project:";
 		public static final String Back = "Back";
 		public static final String browse = "Browse";
 		public static final String get = "Get";
 		public static final String go = "Go!";
 		public static final String yes = "Yes";
 		public static final String no = "No";
 		public static final String ok = "OK!";
 		public static final String save = "Save";
 		public static final String reset = "Reset";
 		public static final String cancel = "Cancel";
 		public static final String abort = "Abort";
 		public static final String next = "Next";
 		public static final String processing = "Processing...";
 		public static final String file1 = "File 1";
 		public static final String file2 = "File 2";
 		public static final String file3 = "File 3";
 		public static final String folder = "Folder";
 		public static final String createDate = "Create Date";
 		public static final String warnLoadError = "An error has ocurred while loading the data!";
 		public static final String warnWrongFormat = "Did you provide a file with the correct stated format?";
 
 		private All() {
 		}
 	}
 
 	public static class Cli {
 
 		public static final String wrongScriptFilePath = "Unable to read start-up file!\nMake sure the specified file exists and try again.";
 		public static final String studyNotExist = "Provided Study Id does not exist!";
 		public static final String availableStudies = "Available Studies:\n";
 		public static final String doneExiting = "\n" + Text.App.appName + " has finished processing your script.\nExiting....\nGood-bye!\n########################";
 
 		private Cli() {
 		}
 	}
 
 	public static class Study {
 
 		public static final String study = "Study";
 		public static final String studies = "Studies";
 		public static final String addStudy = "Add Study";
 		public static final String createNewStudy = "Create New Study";
 		public static final String warnNoStudyName = "You must insert a Study name!";
 		public static final String studyName = "Study Name";
 		public static final String currentStudy = "Current Study:";
 		public static final String insertNewStudyName = "Insert Name of new Study...";
 		public static final String availableStudies = "Available Studies";
 		public static final String studyID = "StudyID";
 		public static final String deleteStudy = "Delete Study";
 		public static final String confirmDelete1 = "Do you really want to delete Studies as well as it's corresponding Sample Info?";
 		public static final String confirmDelete2 = "\nThis action cannot be undone!";
 		public static final String sampleInfo = "Sample Info";
 		public static final String loadSampleInfo = "Load Sample Info";
 		public static final String infoSampleInfo = "Next you must provide a valid Sample Info file.";
 		public static final String updateSampleInfo = "Update Sample Info";
 		public static final String warnMissingSampleInfo = "Warning! Your genotype files contains Samples that are not in your Sample Info file!\nGWASpi will fill dummy info for this Sample...";
 
 		private Study() {
 		}
 	}
 
 	public static class Matrix {
 
 		public static final String lblMatrix = "Matrix:";
 		public static final String matrix = "Matrix";
 		public static final String matrices = "Matrices";
 		public static final String exportMatrix = "Export Matrix";
 		public static final String deleteMatrix = "Delete Matrix";
 		public static final String trafoMatrix = "Transform Matrix";
 		public static final String confirmDelete1 = "Do you really want to delete Matrices? ";
 		public static final String confirmDelete2 = "\nThis action cannot be undone!";
 		public static final String loadGenotypes = "Load Genotype Data";
 		public static final String scanAffectionStandby = "Scanning Affection Status, standby...";
 		public static final String importGenotypes = "Import Genotypes";
 		public static final String newMatrixName = "New Matrix Name: ";
 		public static final String parentMatrix = "Parent Matrix: ";
 		public static final String matrixID = "MatrixID";
 		public static final String currentMatrix = "Current Matrix: ";
 		public static final String input = "Input";
 		public static final String format = "Format";
 		public static final String annotationFile = "Annotation File";
 		public static final String lgenFile = "LGEN File";
 		public static final String sampleInfo = "Sample Info";
 		public static final String genotypes = "Genotypes";
 		public static final String mapFile = "MAP File";
 		public static final String pedFile = "PED File";
 		public static final String bedFile = "BED File";
 		public static final String sampleInfoOrFam = "FAM or Sample Info";
         public static final String bimFile = "BIM File";
 		public static final String findComplementaryPlink = "Should " + App.appName + " search for complementary map/ped files in the same directory?";
 		public static final String findComplementaryPlinkBinary = "Should " + App.appName + " search for complementary bed/bim/fam files in the same directory?";
 		public static final String markerFile = "Marker File";
 		public static final String warnCantLoadData1 = "Data could not be loaded!";
 		public static final String warnCantLoadData2 = "Data could not be loaded!\nDo all Samples listed in your Sample Info File have a corresponding\ngenotype file under the specified path?";
 		public static final String warnInputNewMatrixName = "You must insert a name for the Imported Matrix!";
 		public static final String warnInputFileInField = "You must insert a valid path in field ";
 		public static final String pleaseInsertMatrixName = "Please insert the name of the resulting Matrix!";
 		public static final String infoScanSampleAffection = App.appName + " will now scan your Samples for Case/Control Affection state.\nStandby...";
 		public static final String caseCtrlDetected = "You have provided Case/Control Affection info.\nShould " + App.appName + " perform a complete GWAS study now?";
 		public static final String ifCaseCtrlDetected = "In case your data contains Case/Control Affection info,\nshould " + App.appName + " perform a complete GWAS study after loading data?";
 		public static final String noJustLoad = "No, just load data";
 		public static final String gwasInOne = "GWAS in one?";
 		public static final String descriptionHeader1 = "Matrix created at: "; // add time
 		public static final String descriptionHeader2 = "Loaded using format: "; // add format
 		public static final String descriptionHeader3 = "From files: "; // add file path
 		public static final String descriptionHeader4 = "From Matrices: "; // add file path
 		//public static final String  = ;
 
 		private Matrix() {
 		}
 	}
 
 	public static class Reports {
 
 		public static final String doneReport = "===>Done writing report files!<===";
 		public static final String makeChartsAndReport = "Make Charts and Report";
 		public static final String viewReportSummary = "View Report Summary";
 		public static final String report = "Report";
 		public static final String summary = "Summary";
 		public static final String confirmDelete = "Should corresponding Report and Chart files in 'reports' folder be deleted as well?";
 		public static final String selectSaveMode = "Choose report to be saved";
 		public static final String completeReport = "Complete Report";
 		public static final String currentReportView = "Current Report View";
 		public static final String warnCantOpenFile = "Cannot open file!";
 		public static final String radio1Prefix = "Show";
 		public static final String radio1Suffix_pVal = "most significant p-Values.";
 		public static final String radio1Suffix_gen = "Most significant";
 		public static final String radio2Prefix_pVal = "Show p-Values below";
 		public static final String cannotOpenEnsembl = "Couldn't connect to Ensemble website";
 		public static final String chr = "Chr";
 		public static final String markerId = "MarkerID";
 		public static final String chiSqr = "X²";
 		public static final String pVal = "p-Value";
 		public static final String pos = "Pos";
 		public static final String rsId = "RsID";
 		public static final String externalResourceDB = "External Database Resources";
 		public static final String externalResource = "Ext. Resource";
 		public static final String warnExternalResource = "You need to click on a specific SNP for the selected\nexternal database resource to return data!";
 		public static final String ensemblLink = "Ensembl";
 		public static final String NCBILink = "NCBI";
 		public static final String zoom = "Zoom";
 		public static final String queryDB = "Query DB";
 		public static final String alleles = "Alleles";
 		public static final String majAallele = "Maj. Allele";
 		public static final String minAallele = "Min. Allele";
 		public static final String oddsRatio = "Odds Ratio";
 		public static final String ORAAaa = "OR AA/aa";
 		public static final String ORAaaa = "OR Aa/aa";
 		public static final String missRatio = "Missing-Ratio";
 		public static final String genQA = "QA";
 		public static final String hwObsHetzy = "OBS_HETZY_";
 		public static final String hwExpHetzy = "EXP_HETZY_";
 		public static final String hwPval = "HW_p-Value_";
 		public static final String trendTest = "Trend X²";
 		public static final String CASE = "CASE";
 		public static final String CTRL = "CTRL";
 		public static final String ALL = "ALL";
 		public static final String sampleId = "SampleID";
 		public static final String familyId = "FamilyID";
 		public static final String sex = "Sex";
 		public static final String affection = "Affection";
 		public static final String age = "Age";
 		public static final String category = "Category";
 		public static final String disease = "Disease";
 		public static final String population = "Population";
 		public static final String fatherId = "FatherID";
 		public static final String motherId = "MotherID";
 		public static final String smplHetzyRat = "Hetzyg. ratio";
 		public static final String smplHetzyVsMissingRat = "Sample QA, Heterozygosity vs Missing Ratio";
 		public static final String backToTable = "Back to Table";
 		public static final String backToManhattanPlot = "Back to Manhattan Plot";
 		public static final String threshold = "Threshold";
 		public static final String thresholds = "Thresholds";
 		public static final String redraw = "Redraw";
 		public static final String heterozygosity = "Heterozygosity";
 
 		private Reports() {
 		}
 	}
 
 	public static class Operation {
 
 		public static final String operation = "Operation";
 		public static final String newOperation = "New Operation";
 		public static final String analyseData = "Analyse Data";
 		public static final String generateReports = "Generate Reports";
 		public static final String htmlPerformQA = "<html><div align='center'>Missingnes QA<br>Heterozygosity<div></html>";
 		public static final String infoPerformQA = "The Case/Control Census will be performed according to the initial Sample Affection you provided at load-time.";
 		public static final String htmlPhenoFileCensus = "<html><div align='center'>Genotypes freq. based on external Phenotype file<div></html>";
 		public static final String infoPhenotypeCensus = "Next you must provide a file specifying the Phenotype status of every Sample.";
 		public static final String htmlAffectionCensus = "<html><div align='center'>Genotypes freq. based on Affection status<div></html>";
 		public static final String htmlPhenoMatrixCensus = "<html><div align='center'>Genotypes freq. based on Phenotype DB<div></html>";
 		public static final String htmlGTFreqAndHW = "<html><div align='center'>Genotype freq. & Hardy-Weingerg QA<div></html>";
 		public static final String GTFreqAndHW = "Genotype freq. & Hardy-Weingerg QA";
 		public static final String GTFreqAndHWFriendlyName = "Type in a name for this Genotype frequency count";
 		public static final String htmlPerformHW = "<html><div align='center'>Perfom Hardy-Weinberg equilibrium test<div></html>";
 		//public static final String htmlAllelicAssocTest = "<html><div align='center'>Perform Allelic, Genotypic &<br>Armitage Trend Tests<div></html>";
 		public static final String htmlAllelicAssocTest = "<html><div align='center'>Allelic Association Test<div></html>";
 		public static final String htmlGenotypicTest = "<html><div align='center'>Genotypic Association Test<div></html>";
 		public static final String htmlTrendTest = "<html><div align='center'>Cochran-Armitage Trend Test<div></html>";
 		public static final String htmlBlank = "<html><div align='center'><div></html>";
 		public static final String warnQABeforeAnything = "You must perform a Samples & Markers Quality Assurance before making a GWAS!";
 		public static final String warnOperationsMissing = "Some neccesary previous Operations are missing!";
 		public static final String willPerformOperation = App.appName + " will now complete these missing Operation(s)...";
 		public static final String warnAffectionMissing = "You must provide a valid Case/Control input!\nUse the '" + Study.updateSampleInfo + "' feature in the Study management section.";
 		public static final String warnCensusBeforeHW = "You must perform the Census and QA tests before the Hardy-Weinberg test!\nShould " + App.appName + " perform these now instead?";
 		public static final String warnAllreadyExists = "The Operation you are trying to perform already exists!\nDelete existing operation and Try again.";
 		public static final String warnAllreadyExistsProceed = "The Operation you are trying to perform already exists!\nProceed anyway?";
 		public static final String warnOperationError = "There has been an error while performing operation!";
 		public static final String warnNoDataLeftAfterPicking = "There was no data left to operate on after applying threshold filters!\nNo further Operations performed.";
 		public static final String operationId = "OperationID";
 		public static final String operationName = "Operation Name";
 		public static final String deleteOperation = "Delete Operation";
 		public static final String hardyWeiberg = "Hardy Weinberg";
 		public static final String qaData = "QA Data";
 		public static final String confirmDelete1 = "Do you really want to delete the selected Operation(s)?";
 		public static final String confirmDelete2 = "?\nThis action cannot be undone!";
 		public static final String allelicAssocTest = "Allelic Association Test";
 		public static final String genoAssocTest = "Genotypic Association Test";
 		public static final String trendTest = "Cochran-Armitage Trend Test";
 		public static final String chosePhenotype = "Affection or Phenotype File";
 		public static final String genotypeFreqAndHW = "Do you want to use current Case/Conrol Affection info from the Samples DB\nor update the Samples DB Affection from an external Sample Info file?";
 		public static final String htmlCurrentAffectionFromDB = "<html><div align='center'> Current Case/Control <br> Affection from DB </div></html>";
 		public static final String htmlAffectionFromFile = "<html><div align='center'> Update Case/Control <br> Affection from File </div></html>";
 		public static final String addPhenotypes = "Add Phenotypes";
 		public static final String gwasInOneGo = "GWAS In One Go";
 		public static final String performAllelicTests = "Allelic tests";
 		public static final String performGenotypicTests = "Genotypic tests";
 		public static final String performTrendTests = "Trend tests";
 		public static final String discardMismatch = "Discard mismatching Markers?";
 		public static final String discardMarkerMissing = "Discard Markers with missingness    >";
 		public static final String discardMarkerHetzy = "Discard Markers with heterozygosity    >";
 		public static final String discardMarkerHWCalc1 = "Discard Markers with Hardy-Weinberg p-Value  <";
 		public static final String discardMarkerHWCalc2 = "(0.05 / Markers Nb)";
 		public static final String discardMarkerHWFree = "Discard Markers with Hardy-Weinberg p-Value  <";
 		public static final String discardSampleMissing = "Discard Samples with missingness   >";
 		public static final String discardSampleHetzy = "Discard Samples with heterozygosity    >";
 
 		private Operation() {
 		}
 	}
 
 	public static class Trafo {
 
 		public static final String doneExtracting = "===>Done extracting to new Matrix!<===";
 		public static final String extratedMatrixDetails = "New Matrix Details";
 		public static final String trafoMatrixDetails = "New Matrix Details";
 		public static final String criteriaReturnsNoResults = "The provided criteria has returned 0 matches!";
 		public static final String markerSelectZone = "Marker Selection";
 		public static final String filterMarkersBy = "Filter Markers By:";
 		public static final String variable = "Property / Variable: ";
 		public static final String criteria = "Criteria:";
 		public static final String criteriaFile = "Criteria File:";
 		public static final String sampleSelectZone = "Sample Selection";
 		public static final String filterSamplesBy = "Filter Samples By:";
 		public static final String inputMatrix = "Input Matrix:";
 		public static final String htmlTranslate1 = "<html><div align='center'>Translate AB or 12 to ACGT<div></html>";
 		public static final String htmlTranslate2 = "<html><div align='center'>Translate 1234 to ACGT<div></html>";
 		public static final String htmlTranslate3 = "<html><div align='center'>Translate 12 to ACGT<div></html>";
 		public static final String htmlForceAlleleStrand = "Force Alleles Strand";
 		public static final String mergeMatrices = "Merge Matrices";
 		public static final String mergeWithMatrix = "Matrix to be added";
 		public static final String selectMatrix = "Select Matrix: ";
 		public static final String mergeMarkersOnly = "Merge new Markers only";
 		public static final String mergeSamplesOnly = "Merge new Samples only";
 		public static final String mergeAll = "Merge Markers & Samples";
 		public static final String merge = "Merge";
 		public static final String mergedFrom = "Matrix merged from parent Matrices:";
 		public static final String mergeMethodMarkerJoin = "· The SampleSet from the 1st Matrix will be used in the result Matrix.\n· No new Samples from the 2nd Matrix will be added.\n· Markers from the 2nd Matrix will be merged in chromosome and position order to the\n  MarkersSet from the 1st Matrix.\n· Duplicate genotypes from the 2nd Matrix will overwrite genotypes from the 1st Matrix.";
 		public static final String mergeMethodSampleJoin = "· The MarkerSet from the 1st Matrix will be used in the result Matrix.\n· No new Markers from the 2nd Matrix will be added.\n· Samples from the 2nd Matrix will be appended to the end of the SampleSet from the 1st Matrix.\n· Duplicate genotypes from the 2nd Matrix will overwrite genotypes in the 1st Matrix.";
 		public static final String mergeMethodMergeAll = "· MarkerSets from the 1st and 2nd Matrix will be merged in the result Matrix.\n· Samples from the 2nd Matrix will be appended to the end of the SampleSet from the 1st Matrix.\n· Duplicate Genotypes from the 1st and 2nd matrix will be resolved by overwriting the 1st Matrix\n values by the 2nd Matrix values.";
 		public static final String htmlAddMatrixSamples = "<html><div align='center'>Add Samples To Matrix.<div></html>";
 		public static final String htmlAddMatrixMarkers = "<html><div align='center'>Add Markers To Matrix.<div></html>";
 		public static final String successMatricesJoined = "Matrix joined correctly!";
 		public static final String warnMatrixEncMismatch = "Matrix genotypes are encoded differently.\nTranslate first!";
 		public static final String warnMatrixEncUnknown = "Matrix genotypes encoding is unknown.\nCannot proceed!";
 		public static final String warnStrandUndet = "Part of the genotypes have an undetermined strand-placement\nContinue anyway?";
 		public static final String warnStrandMismatch = "The genotypes are placed on different strands.\nFlip strands first!";
 		public static final String warnExcessMismatch = "Matrix joined. \nThe acceptable number of mismatching genotypes has been crossed!";
 		public static final String warnNoDictionary = "The parent matrix you are trying to translate has no dictionnary!";
 		public static final String warnNotACGT = "The parent matrix you are trying to modify is not encoded as " + cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString();
 		public static final String warnNot1234 = "The parent matrix you are trying to modify is not encoded as " + cNetCDF.Defaults.GenotypeEncoding.O1234;
 		public static final String warnNotAB12 = "The parent matrix you are trying to modify is not encoded as " + cNetCDF.Defaults.GenotypeEncoding.AB0 + " or " + cNetCDF.Defaults.GenotypeEncoding.O12;
 		public static final String warnNotACGTor1234 = "The parent matrix you are trying to modify is not encoded as " + cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString() + " or " + cNetCDF.Defaults.GenotypeEncoding.O1234;
 		public static final String extract = "Extract";
 		public static final String extractToNewMatrix = "Extract data to new Matrix";
 		public static final String exportMatrix = "Export Matrix";
 		public static final String translateMatrix = "Translate Matrices";
 		public static final String transformMatrix = "Translation & Stranding";
 		public static final String extractData = "Extract Data";
 		public static final String flipStrand = "Flip Strand";
 
 		private Trafo() {
 		}
 	}
 
 	public static class Processes {
 
 		public static final String processes = "Processes";
 		public static final String processOverview = "Process Overview";
 		public static final String processLog = "Process Log";
 		public static final String launchTime = "Launch Time";
 		public static final String startTime = "Start Time";
 		public static final String id = "ID";
 		public static final String processeName = "Process Name";
 		public static final String endTime = "End Time";
 		public static final String queueState = "Activity";
 		public static final String cantDeleteRequiredItem = App.appName + " can't delete this Item!\nThis Item is required for a process in the pending processes queue.";
 		public static final String abortingProcess = "********* Process Aborted! **********";
 
 		private Processes() {
 		}
 	}
 
 	public static class Help {
 
 		public static final String riddle = "?";
 		public static final String launchError = "Error attempting to launch web browser\n";
 		public static final String help = "Help";
 		public static final String aboutHelp = "Pick a link to open a Help subject";
 
 		private Help() {
 		}
 	}
 
 	public static class Dialog {
 
 		public static final String chromosome = "Chromosome";
 		public static final String strand = "Strand";
 		public static final String genotypeEncoding = "Genotype encoding";
 
 		private Dialog() {
 		}
 	}
 }
