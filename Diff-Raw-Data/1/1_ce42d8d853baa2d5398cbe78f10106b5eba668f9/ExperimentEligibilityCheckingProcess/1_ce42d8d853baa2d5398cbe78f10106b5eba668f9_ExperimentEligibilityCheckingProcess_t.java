 package uk.ac.ebi.fgpt.conan.process.atlas;
 
 import net.sourceforge.fluxion.spi.ServiceProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.*;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.ArrayDesignAttribute;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
 import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
 import uk.ac.ebi.fgpt.conan.dao.DatabaseConanControlledVocabularyDAO;
 import uk.ac.ebi.fgpt.conan.model.ConanParameter;
 import uk.ac.ebi.fgpt.conan.model.ConanProcess;
 import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Process to check experiment eligibility for Atlas. Consists of six steps: 1
  * check for experiment type, 2 check for two-channel experiment, 3 check for
  * factor values, 4 check for factor types from controlled vocabulary only, 5
  * check for array design existence in Atlas, 6 check for raw data files for
  * Affy and derived data files for all other platforms.
  *
  * @author Natalja Kurbatova
  * @date 15/02/11
  */
 @ServiceProvider
 public class ExperimentEligibilityCheckingProcess implements ConanProcess {
     private final Collection<ConanParameter> parameters;
     private final AccessionParameter accessionParameter;
     private final List<String> ArrayDesignAccessions = new ArrayList<String>();
     private final DatabaseConanControlledVocabularyDAO controlledVocabularyDAO;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     protected Logger getLog() {
         return log;
     }
 
     /**
      * Constructor for process. Initializes conan2 parameters for the process.
      */
     public ExperimentEligibilityCheckingProcess() {
         parameters = new ArrayList<ConanParameter>();
         accessionParameter = new AccessionParameter();
         parameters.add(accessionParameter);
         ClassPathXmlApplicationContext ctx =
                 new ClassPathXmlApplicationContext("controlled-vocabulary-context.xml");
         controlledVocabularyDAO =
                 ctx.getBean("databaseConanControlledVocabularyDAO",
                             DatabaseConanControlledVocabularyDAO.class);
     }
 
     public boolean execute(Map<ConanParameter, String> parameters)
             throws ProcessExecutionException, IllegalArgumentException,
             InterruptedException {
 
         int exitValue = 0;
 
         // Add to the desired logger
         BufferedWriter log;
 
         String error_val = "";
 
         //deal with parameters
         final AccessionParameter accession = new AccessionParameter();
         accession.setAccession(parameters.get(accessionParameter));
 
         String reportsDir =
                 accession.getFile().getParentFile().getAbsolutePath() + File.separator +
                         "reports";
         File reportsDirFile = new File(reportsDir);
         if (!reportsDirFile.exists()) {
             reportsDirFile.mkdirs();
         }
 
         String fileName = reportsDir + File.separator + accession.getAccession() +
                 "_AtlasEligibilityCheck" +
                 "_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()) +
                 ".report";
         try {
             log = new BufferedWriter(new FileWriter(fileName));
             log.write("Atlas Eligibility Check: START\n");
         }
         catch (IOException e) {
             exitValue = 1;
             e.printStackTrace();
 
             ProcessExecutionException pex = new ProcessExecutionException(exitValue,
                                                                           "Can't create report file '" +
                                                                                   fileName +
                                                                                   "'");
 
             String[] errors = new String[1];
             errors[0] = "Can't create report file '" + fileName + "'";
             pex.setProcessOutput(errors);
             throw pex;
         }
 
         // make a new parser
         MAGETABParser parser = new MAGETABParser();
 
         try {
             MAGETABInvestigation investigation =
                     parser.parse(accession.getFile().getAbsoluteFile());
 
             // 1 check: experiment types
             boolean isAtlasType = false;
             String restrictedExptType = "";
             if (investigation.IDF.getComments().containsKey("AEExperimentType")) {
                 for (String exptType : investigation.IDF.getComments()
                                                         .get("AEExperimentType")) {
                     for (String AtlasType : controlledVocabularyDAO
                             .getAtlasExperimentTypes()) {
                         if (exptType.equals(AtlasType)) {
                             isAtlasType = true;
                         }
                         else {
                             restrictedExptType = exptType;
                         }
                     }
                 }
             }
             else {
                 for (String exptType : investigation.IDF.experimentalDesign) {
                     for (String AtlasType : controlledVocabularyDAO
                             .getAtlasExperimentTypes()) {
                         if (exptType.equals(AtlasType)) {
                             isAtlasType = true;
                         }
                     }
                 }
             }
 
             if (!isAtlasType)
             //not in Atlas Experiment Types
             {
                 exitValue = 1;
                 log.write(
                         "'Experiment Type' " + restrictedExptType +
                                 " is not accepted by Atlas\n");
                 getLog().debug(
                         "'Experiment Type' " + restrictedExptType +
                                 " is not accepted by Atlas");
                 error_val = "'Experiment Type' " + restrictedExptType +
                         " is not accepted by Atlas.\n";
             }
 
             //2 two-channel experiment
             if (investigation.SDRF.getNumberOfChannels() > 1) {
                 exitValue = 1;
                 log.write(
                         "Two-channel experiment is not accepted by Atlas\n");
                 getLog().debug(
                         "Two-channel experiment is not accepted by Atlas");
                 error_val = error_val + "Two-channel experiment is not accepted by Atlas. \n";
             }
 
 
             Collection<HybridizationNode> hybridizationNodes =
                     investigation.SDRF.getNodes(HybridizationNode.class);
             Collection<ArrayDataNode> rawDataNodes =
                     investigation.SDRF.getNodes(ArrayDataNode.class);
             Collection<DerivedArrayDataNode> processedDataNodes =
                     investigation.SDRF.getNodes(DerivedArrayDataNode.class);
             Collection<DerivedArrayDataMatrixNode> processedDataMatrixNodes =
                     investigation.SDRF.getNodes(DerivedArrayDataMatrixNode.class);
             int factorValues = 0;
             for (HybridizationNode hybNode : hybridizationNodes) {
                 if (hybNode.factorValues.size() > 0) {
                     factorValues++;
                 }
 
                
                 ArrayDesignAccessions.clear();
                 for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns) {
                     if (!ArrayDesignAccessions
                             .contains(arrayDesign.getAttributeValue())) {
                         ArrayDesignAccessions.add(arrayDesign.getAttributeValue());
                     }
                 }
             }
 
 
             boolean replicates = true;
             // All experiments must have replicates for at least 1 factor
             Hashtable<String,Hashtable> factorTypesCounts = new Hashtable<String, Hashtable>();
             for (String factorType : investigation.IDF.experimentalFactorType) {
                 Hashtable<String,Integer> factorValuesCounts = new Hashtable<String, Integer>();
                 for (HybridizationNode hybNode : hybridizationNodes) {
                     String arrayDesignName = "";
                     for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns) {
                         arrayDesignName=arrayDesign.getAttributeValue() ;
                     }
                     for (FactorValueAttribute fva : hybNode.factorValues) {
                         if (fva.getAttributeType().toLowerCase().contains(factorType.toLowerCase())) {
                             String key = arrayDesignName+"_"+fva.getAttributeValue();
                             if (factorValuesCounts.get(key)==null){
                                 factorValuesCounts.put(key,1);
                             }
                             else {
                                 int value = factorValuesCounts.get(key);
                                 value++;
                                 factorValuesCounts.put(key,value);
                             }
                         }
                     }
                 }
 
                 factorTypesCounts.put(factorType,factorValuesCounts);
             }
 
 
 
             for (Hashtable<String,Integer> fvc : factorTypesCounts.values() ) {
                 for (int val : fvc.values()){
                     if (val == 1){
                         replicates = false;
                     }
                 }
             }
 
             // replicates
             if (replicates == false) {
                 exitValue = 1;
                 log.write(
                         "Experiment does not have replicates for at least 1 factor type\n");
                 getLog().debug(
                         "Experiment does not have replicates for at least 1 factor type");
                 error_val = error_val + "Experiment does not have replicates for at least 1 factor type. \n";
             }
 
             //3 factor values
             if (factorValues == 0) {
                 exitValue = 1;
                 log.write(
                         "Experiment does not have Factor Values\n");
                 getLog().debug(
                         "Experiment does not have Factor Values");
                 error_val = error_val + "Experiment does not have Factor Values. \n";
             }
 
             //6 and 7 factor types are from controlled vocabulary and not repeated
             boolean factorTypesFromCV = true;
             boolean factorTypesVariable = true;
             boolean characteristicsFromCV = true;
             boolean characteristicsVariable = true;
             List<String> missedFactorTypes = new ArrayList<String>();
             List<String> missedCharacteristics = new ArrayList<String>();
             List<String> repeatedFactorTypes = new ArrayList<String>();
             List<String> repeatedCharacteristics = new ArrayList<String>();
             for (String factorType : investigation.IDF.experimentalFactorType) {
                 if (!controlledVocabularyDAO
                         .getAtlasFactorTypes().contains(factorType.toLowerCase())) {
                     factorTypesFromCV = false;
                     missedFactorTypes.add(factorType);
                 }
                 if (repeatedFactorTypes.contains(factorType)) {
                     factorTypesVariable = false;
                 }
                 repeatedFactorTypes.add(factorType);
             }
             for (SampleNode sampleNode : investigation.SDRF.getNodes(SampleNode.class)) {
                 for (CharacteristicsAttribute ca : sampleNode.characteristics) {
                     if (repeatedCharacteristics.contains(ca.getAttributeType())) {
                         characteristicsVariable = false;
                     }
                     repeatedCharacteristics.add(ca.getAttributeType());
                     if (!controlledVocabularyDAO
                             .getAtlasFactorTypes().contains(ca.getAttributeType().toLowerCase())) {
                         characteristicsFromCV = false;
                         missedCharacteristics.add(ca.getAttributeType());
                     }
                 }
             }
             if (!factorTypesFromCV) {
                 exitValue = 1;
                 log.write(
                         "Experiment has Factor Types that are not in controlled vocabulary:" +
                                 missedFactorTypes + "\n");
                 getLog().debug(
                         "Experiment has Factor Types that are not in controlled vocabulary:" +
                                 missedFactorTypes);
                 error_val = error_val +
                         "Experiment has Factor Types that are not in controlled vocabulary:" +
                         missedFactorTypes + ".\n";
             }
             if (!characteristicsFromCV) {
                 exitValue = 1;
                 log.write(
                         "Experiment has Characteristics that are not in controlled vocabulary:" +
                                 missedCharacteristics + "\n");
                 getLog().debug(
                         "Experiment has Characteristics that are not in controlled vocabulary:" +
                                 missedCharacteristics);
                 error_val = error_val +
                         "Experiment has Characteristics that are not in controlled vocabulary:" +
                         missedCharacteristics + ".\n";
             }
 
             if (!factorTypesVariable) {
                 exitValue = 1;
                 log.write("Experiment has repeated Factor Types.\n");
                 getLog().debug("Experiment has repeated Factor Types.");
                 error_val = error_val + "Experiment has repeated Factor Types.\n";
             }
 
             if (!characteristicsVariable) {
                 exitValue = 1;
                 log.write("Experiment has repeated Characteristics.\n");
                 getLog().debug("Experiment has repeated Characteristics.");
                 error_val = error_val + "Experiment has repeated Characteristics.\n";
             }
 
             // 5 check: array design is in Atlas
             for (String arrayDesign : ArrayDesignAccessions) {
                 ArrayDesignExistenceChecking arrayDesignExistenceChecking =
                         new ArrayDesignExistenceChecking();
                 String arrayCheckResult =
                         arrayDesignExistenceChecking.execute(arrayDesign);
                 if (arrayCheckResult.equals("empty") ||
                         arrayCheckResult.equals("no")) {
                     exitValue = 1;
                     log.write("Array design '" +
                                       arrayDesign +
                                       "' used in experiment is not in Atlas\n");
                     getLog().debug("Array design '" +
                                            arrayDesign +
                                            "' used in experiment is not in Atlas");
                     error_val = error_val + "Array design '" +
                             arrayDesign +
                             "' used in experiment is not in Atlas. \n";
                 }
 
                 else {
                     //Array Design is in Atlas
                     Collection<HybridizationNode> hybridizationSubNodes =
                             new ArrayList<HybridizationNode>();
                     Collection<Node> rawDataSubNodes = new ArrayList<Node>();
                     Collection<Node> processedDataSubNodes = new ArrayList<Node>();
                     Collection<Node> processedDataMatrixSubNodes =
                             new ArrayList<Node>();
                     //Number of arrays in experiment
                     if (ArrayDesignAccessions.size() > 1) {
 
                         for (HybridizationNode hybNode : hybridizationNodes) {
                             ArrayDesignAttribute attribute = new ArrayDesignAttribute();
                             attribute.setAttributeValue(arrayDesign);
 
                             if (hybNode.arrayDesigns.contains(attribute)) {
                                 //get data nodes for particular array design
                                 hybridizationSubNodes.add(hybNode);
                                 getNodes(hybNode, ArrayDataNode.class, rawDataSubNodes);
                                 getNodes(hybNode, DerivedArrayDataNode.class,
                                          processedDataSubNodes);
                                 getNodes(hybNode, DerivedArrayDataMatrixNode.class,
                                          processedDataMatrixSubNodes);
 
                             }
 
                         }
                     }
                     else {
                         //one array design in experiment
                         hybridizationSubNodes = hybridizationNodes;
                         for (ArrayDataNode node : rawDataNodes) {
                             rawDataSubNodes.add(node);
                         }
                         for (DerivedArrayDataNode node : processedDataNodes) {
                             processedDataSubNodes.add(node);
                         }
                         for (DerivedArrayDataMatrixNode node : processedDataMatrixNodes) {
                             processedDataMatrixSubNodes.add(node);
                         }
 
                     }
 
                     //6 check: if Affy then check for raw data files, else for derived
                     if (arrayCheckResult.equals("affy") &&
                             hybridizationSubNodes.size() != rawDataSubNodes.size()) {   //6a affy
                         exitValue = 1;
                         log.write(
                                 "Affymetrix experiment without raw data files\n");
                         getLog().debug(
                                 "Affymetrix experiment without raw data files");
                         error_val = error_val + "Affymetrix experiment without raw data files.\n";
                     }
                     else {
                         //6b not affy without processed data
                         if (!arrayCheckResult.equals("affy") &&
                                 //processedDataSubNodes.size() == 0 &&
                                 processedDataMatrixSubNodes.size() == 0) {
                             exitValue = 1;
                             log.write(
                                     "Non-Affymetrix experiment without processed data files\n");
                             getLog().debug(
                                     "Non-Affymetrix experiment without processed data files");
                             error_val = error_val +
                                     "Non-Affymetrix experiment without processed data files. \n";
 
                         }
                     }
                 }
             }
 
             if (exitValue == 1) {
                 ProcessExecutionException pex = new ProcessExecutionException(exitValue,
                                                                               error_val);
                 String[] errors = new String[1];
                 errors[0] = error_val;
                 pex.setProcessOutput(errors);
                 throw pex;
             }
 
         }
         catch (ParseException e) {
             exitValue = 1;
             ProcessExecutionException pex = new ProcessExecutionException(exitValue,
                                                                           e.getMessage());
 
             String[] errors = new String[1];
             errors[0] = e.getMessage();
             errors[1] =
                     "Please check MAGE-TAB files and/or run validation process.\n";
             pex.setProcessOutput(errors);
             throw pex;
         }
         catch (IOException e) {
             exitValue = 1;
             ProcessExecutionException pex = new ProcessExecutionException(exitValue,
                                                                           e.getMessage());
 
             String[] errors = new String[1];
             errors[0] = e.getMessage();
             pex.setProcessOutput(errors);
             throw pex;
         }
         catch (RuntimeException e) {
             exitValue = 1;
             ProcessExecutionException pex = new ProcessExecutionException(exitValue,
                                                                           e.getMessage());
 
             String[] errors = new String[1];
             errors[0] = e.getMessage();
             pex.setProcessOutput(errors);
             throw pex;
         }
         finally {
             try {
                 if (exitValue == 0) {
                     log.write("Experiment \"" +
                                       accession.getAccession() +
                                       "\" is eligible for Atlas\n");
                 }
                 else {
                     log.write("Experiment \"" +
                                       accession.getAccession() +
                                       "\" is NOT eligible for Atlas\n");
                    log.write(error_val);
                 }
                 log.write("Atlas Eligibility Check: FINISHED\n");
                 log.write(
                         "Eligibility checks for Gene Expression Atlas version 2.0.9.3: \n" +
                                 "1. Experiment has raw data for Affymetrix platforms or normalized data for all other platforms;\n" +
                                 "2. Array design(s) used in experiment are loaded into Atlas;\n" +
                                 "3. Type of experiment is from the list: \n" +
                                 " - transcription profiling by array,\n" +
                                 " - methylation profiling by array,\n" +
                                 " - tiling path by array,\n" +
                                 " - comparative genomic hybridization by array,\n" +
                                 " - microRNA profiling by array,\n" +
                                 " - RNAi profiling by array,\n" +
                                 " - ChIP-chip by array;\n" +
                                 "4. Experiments is not two-channel;\n" +
                                 "5. Experiment has factor values;\n" +
                                 "6. Experiment has replicates for at least 1 factor type;"+
                                 "7. Factor types and Characteristics are from controlled vocabulary;\n" +
                                 "8. Factor types and Characteristics are variable (not repeated).");
                 log.close();
             }
             catch (IOException e) {
                 e.printStackTrace();
                 ProcessExecutionException pex = new ProcessExecutionException(exitValue,
                                                                               e.getMessage());
 
                 String[] errors = new String[1];
                 errors[0] = e.getMessage();
                 pex.setProcessOutput(errors);
                 throw pex;
             }
         }
 
         ProcessExecutionException pex = new ProcessExecutionException(exitValue,
                                                                       "Something wrong in the code ");
         if (exitValue == 0) {
             return true;
         }
         else {
             String[] errors = new String[1];
             errors[0] = "Something wrong in the code ";
             pex.setProcessOutput(errors);
             throw pex;
         }
     }
 
 
     public boolean executeMockup(String file)
             throws ProcessExecutionException, IllegalArgumentException {
 
         // Add to the desired logger
         BufferedWriter log;
 
         boolean result = false;
 
         // now, parse from a file
         File idfFile = new File(file);
         int exitValue = 0;
         String error_val = "";
 
         String reportsDir =
                 idfFile.getParentFile().getAbsolutePath() + File.separator +
                         "reports";
         File reportsDirFile = new File(reportsDir);
         if (!reportsDirFile.exists()) {
             reportsDirFile.mkdirs();
         }
 
         String fileName = reportsDir + File.separator + idfFile.getName() +
                 "_AtlasEligibilityCheck" +
                 "_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()) +
                 ".report";
         try {
             log = new BufferedWriter(new FileWriter(fileName));
             log.write("Atlas Eligibility Check: START\n");
         }
         catch (IOException e) {
             e.printStackTrace();
             throw new ProcessExecutionException(1, "Can't create report file '" +
                     fileName + "'", e);
         }
 
         // make a new parser
         MAGETABParser parser = new MAGETABParser();
 
 
         try {
             MAGETABInvestigation investigation = parser.parse(idfFile);
             // I check: experiment types
             boolean isAtlasType = true;
 
             String restrictedExptType = "";
            /* if (investigation.IDF.getComments().containsKey("AEExperimentType")) {
                 for (String exptType : investigation.IDF.getComments()
                         .get("AEExperimentType")) {
                     for (String AtlasType : controlledVocabularyDAO
                             .getAtlasExperimentTypes()) {
                         if (exptType.equals(AtlasType)) {
                             isAtlasType = true;
                         }
                         else {
                             restrictedExptType = exptType;
                         }
                     }
                 }
             }
             else {
                 for (String exptType : investigation.IDF.experimentalDesign) {
                     for (String AtlasType : controlledVocabularyDAO
                             .getAtlasExperimentTypes()) {
                         if (exptType.equals(AtlasType)) {
                             isAtlasType = true;
                         }
                     }
                 }
             }        */
 
             if (!isAtlasType)
             //not in Atlas Experiment Types
             {
                 exitValue = 1;
                 log.write(
                         "'Experiment Type' " + restrictedExptType +
                                 " is not accepted by Atlas\n");
                 getLog().debug(
                         "'Experiment Type' " + restrictedExptType +
                                 " is not accepted by Atlas");
                 error_val = "'Experiment Type' " + restrictedExptType +
                         " is not accepted by Atlas.\n";
             }
 
             //2 two-channel experiment
             if (investigation.SDRF.getNumberOfChannels() > 1) {
                 exitValue = 1;
                 log.write(
                         "Two-channel experiment is not accepted by Atlas\n");
                 getLog().debug(
                         "Two-channel experiment is not accepted by Atlas");
                 error_val = error_val + "Two-channel experiment is not accepted by Atlas. \n";
             }
 
 
             Collection<HybridizationNode> hybridizationNodes =
                     investigation.SDRF.getNodes(HybridizationNode.class);
             Collection<ArrayDataNode> rawDataNodes =
                     investigation.SDRF.getNodes(ArrayDataNode.class);
             Collection<DerivedArrayDataNode> processedDataNodes =
                     investigation.SDRF.getNodes(DerivedArrayDataNode.class);
             Collection<DerivedArrayDataMatrixNode> processedDataMatrixNodes =
                     investigation.SDRF.getNodes(DerivedArrayDataMatrixNode.class);
             int factorValues = 0;
             for (HybridizationNode hybNode : hybridizationNodes) {
                 if (hybNode.factorValues.size() > 0) {
                     factorValues++;
                 }
 
 
                 ArrayDesignAccessions.clear();
                 for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns) {
                     if (!ArrayDesignAccessions
                             .contains(arrayDesign.getAttributeValue())) {
                         ArrayDesignAccessions.add(arrayDesign.getAttributeValue());
                     }
                 }
             }
 
 
             boolean replicates = true;
             // All experiments must have replicates for at least 1 factor
             Hashtable<String,Hashtable> factorTypesCounts = new Hashtable<String, Hashtable>();
             for (String factorType : investigation.IDF.experimentalFactorType) {
                 Hashtable<String,Integer> factorValuesCounts = new Hashtable<String, Integer>();
                 for (HybridizationNode hybNode : hybridizationNodes) {
                     String arrayDesignName = "";
                     for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns) {
                         arrayDesignName=arrayDesign.getAttributeValue() ;
                     }
                     for (FactorValueAttribute fva : hybNode.factorValues) {
                         if (fva.getAttributeType().toLowerCase().contains(factorType.toLowerCase())) {
                             String key = arrayDesignName+"_"+fva.getAttributeValue();
                             if (factorValuesCounts.get(key)==null){
                                 factorValuesCounts.put(key,1);
                             }
                             else {
                                 int value = factorValuesCounts.get(key);
                                 value++;
                                 factorValuesCounts.put(key,value);
                             }
                         }
                     }
                 }
 
                 factorTypesCounts.put(factorType,factorValuesCounts);
             }
 
 
 
             for (Hashtable<String,Integer> fvc : factorTypesCounts.values() ) {
                 for (int val : fvc.values()){
                     if (val == 1){
                         replicates = false;
                     }
                 }
             }
 
             // replicates
             if (replicates == false) {
                 exitValue = 1;
                 log.write(
                         "Experiment does not have replicates for at least 1 factor type\n");
                 getLog().debug(
                         "Experiment does not have replicates for at least 1 factor type");
                 error_val = error_val + "Experiment does not have replicates for at least 1 factor type. \n";
             }
 
             //3 factor values
             if (factorValues == 0) {
                 exitValue = 1;
                 log.write(
                         "Experiment does not have Factor Values\n");
                 getLog().debug(
                         "Experiment does not have Factor Values");
                 error_val = error_val + "Experiment does not have Factor Values. \n";
             }
 
             //6 and 7 factor types are from controlled vocabulary and not repeated
             boolean factorTypesFromCV = true;
             boolean factorTypesVariable = true;
             boolean characteristicsFromCV = true;
             boolean characteristicsVariable = true;
             List<String> missedFactorTypes = new ArrayList<String>();
             List<String> missedCharacteristics = new ArrayList<String>();
             List<String> repeatedFactorTypes = new ArrayList<String>();
             List<String> repeatedCharacteristics = new ArrayList<String>();
             for (String factorType : investigation.IDF.experimentalFactorType) {
                 if (!controlledVocabularyDAO
                         .getAtlasFactorTypes().contains(factorType.toLowerCase())) {
                     factorTypesFromCV = false;
                     missedFactorTypes.add(factorType);
                 }
                 if (repeatedFactorTypes.contains(factorType)) {
                     factorTypesVariable = false;
                 }
                 repeatedFactorTypes.add(factorType);
             }
             for (SampleNode sampleNode : investigation.SDRF.getNodes(SampleNode.class)) {
                 for (CharacteristicsAttribute ca : sampleNode.characteristics) {
                     if (repeatedCharacteristics.contains(ca.getAttributeType())) {
                         characteristicsVariable = false;
                     }
                     repeatedCharacteristics.add(ca.getAttributeType());
                     if (!controlledVocabularyDAO
                             .getAtlasFactorTypes().contains(ca.getAttributeType().toLowerCase())) {
                         characteristicsFromCV = false;
                         missedCharacteristics.add(ca.getAttributeType());
                     }
                 }
             }
             if (!factorTypesFromCV) {
                 exitValue = 1;
                 log.write(
                         "Experiment has Factor Types that are not in controlled vocabulary:" +
                                 missedFactorTypes + "\n");
                 getLog().debug(
                         "Experiment has Factor Types that are not in controlled vocabulary:" +
                                 missedFactorTypes);
                 error_val = error_val +
                         "Experiment has Factor Types that are not in controlled vocabulary:" +
                         missedFactorTypes + ".\n";
             }
             if (!characteristicsFromCV) {
                 exitValue = 1;
                 log.write(
                         "Experiment has Characteristics that are not in controlled vocabulary:" +
                                 missedCharacteristics + "\n");
                 getLog().debug(
                         "Experiment has Characteristics that are not in controlled vocabulary:" +
                                 missedCharacteristics);
                 error_val = error_val +
                         "Experiment has Characteristics that are not in controlled vocabulary:" +
                         missedCharacteristics + ".\n";
             }
 
             if (!factorTypesVariable) {
                 exitValue = 1;
                 log.write("Experiment has repeated Factor Types.\n");
                 getLog().debug("Experiment has repeated Factor Types.");
                 error_val = error_val + "Experiment has repeated Factor Types.\n";
             }
 
             if (!characteristicsVariable) {
                 exitValue = 1;
                 log.write("Experiment has repeated Characteristics.\n");
                 getLog().debug("Experiment has repeated Characteristics.");
                 error_val = error_val + "Experiment has repeated Characteristics.\n";
             }
 
             // 5 check: array design is in Atlas
             for (String arrayDesign : ArrayDesignAccessions) {
                 ArrayDesignExistenceChecking arrayDesignExistenceChecking =
                         new ArrayDesignExistenceChecking();
                 String arrayCheckResult =
                         arrayDesignExistenceChecking.execute(arrayDesign);
                 if (arrayCheckResult.equals("empty") ||
                         arrayCheckResult.equals("no")) {
                     exitValue = 1;
                     log.write("Array design '" +
                             arrayDesign +
                             "' used in experiment is not in Atlas\n");
                     getLog().debug("Array design '" +
                             arrayDesign +
                             "' used in experiment is not in Atlas");
                     error_val = error_val + "Array design '" +
                             arrayDesign +
                             "' used in experiment is not in Atlas. \n";
                 }
 
                 else {
                     //Array Design is in Atlas
                     Collection<HybridizationNode> hybridizationSubNodes =
                             new ArrayList<HybridizationNode>();
                     Collection<Node> rawDataSubNodes = new ArrayList<Node>();
                     Collection<Node> processedDataSubNodes = new ArrayList<Node>();
                     Collection<Node> processedDataMatrixSubNodes =
                             new ArrayList<Node>();
                     //Number of arrays in experiment
                     if (ArrayDesignAccessions.size() > 1) {
 
                         for (HybridizationNode hybNode : hybridizationNodes) {
                             ArrayDesignAttribute attribute = new ArrayDesignAttribute();
                             attribute.setAttributeValue(arrayDesign);
 
                             if (hybNode.arrayDesigns.contains(attribute)) {
                                 //get data nodes for particular array design
                                 hybridizationSubNodes.add(hybNode);
                                 getNodes(hybNode, ArrayDataNode.class, rawDataSubNodes);
                                 getNodes(hybNode, DerivedArrayDataNode.class,
                                         processedDataSubNodes);
                                 getNodes(hybNode, DerivedArrayDataMatrixNode.class,
                                         processedDataMatrixSubNodes);
 
                             }
 
                         }
                     }
                     else {
                         //one array design in experiment
                         hybridizationSubNodes = hybridizationNodes;
                         for (ArrayDataNode node : rawDataNodes) {
                             rawDataSubNodes.add(node);
                         }
                         for (DerivedArrayDataNode node : processedDataNodes) {
                             processedDataSubNodes.add(node);
                         }
                         for (DerivedArrayDataMatrixNode node : processedDataMatrixNodes) {
                             processedDataMatrixSubNodes.add(node);
                         }
 
                     }
 
                     //6 check: if Affy then check for raw data files, else for derived
                     if (arrayCheckResult.equals("affy") &&
                             hybridizationSubNodes.size() != rawDataSubNodes.size()) {   //6a affy
                         exitValue = 1;
                         log.write(
                                 "Affymetrix experiment without raw data files\n");
                         getLog().debug(
                                 "Affymetrix experiment without raw data files");
                         error_val = error_val + "Affymetrix experiment without raw data files.\n";
                     }
                     else {
                         //6b not affy without processed data
                         if (!arrayCheckResult.equals("affy") &&
                                 //processedDataSubNodes.size() == 0 &&
                                 processedDataMatrixSubNodes.size() == 0) {
                             exitValue = 1;
                             log.write(
                                     "Non-Affymetrix experiment without processed data files\n");
                             getLog().debug(
                                     "Non-Affymetrix experiment without processed data files");
                             error_val = error_val +
                                     "Non-Affymetrix experiment without processed data files. \n";
 
                         }
                     }
                 }
             }
         }
         catch (Exception e) {
             result = false;
             e.printStackTrace();
             throw new ProcessExecutionException(1,
                                                 "Atlas Eligibility Check: something is wrong in the code",
                                                 e);
         }
         finally {
             try {
                 if (result) {
                     log.write(
                             "Atlas Eligibility Check: experiment \"" + idfFile.getName() +
                                     "\" is eligible for ArrayExpress.\n");
                 }
                 else {
                     log.write(
                             "Atlas Eligibility Check: experiment \"" + idfFile.getName() +
                                     "\" is NOT eligible for ArrayExpress.\n");
                 }
                 log.write(error_val);
                 System.out.println(error_val);
                 log.write("Atlas Eligibility Check: FINISHED\n");
                 log.write(
                         "Eligibility checks for Gene Expression Atlas version 2.0.9.3: \n" +
                                 "1. Experiment has raw data for Affymetrix platforms or normalized data for all other platforms;\n" +
                                 "2. Array design(s) used in experiment are loaded into Atlas;\n" +
                                 "3. Type of experiment: transcription profiling by array,\n" +
                                 "methylation profiling by array,\n" +
                                 "tiling path by array,\n" +
                                 "comparative genomic hybridization by array,\n" +
                                 "microRNA profiling by array,\n" +
                                 "RNAi profiling by array,\n" +
                                 "ChIP-chip by array;\n" +
                                 "4. Two-channel experiments - can't be loaded into Atlas;\n" +
                                 "5. Experiment has factor values;\n" +
                                 "6. Factor types are from controlled vocabulary;\n" +
                                 "7. Factor types are variable (not repeated).");
                 log.close();
             }
             catch (IOException e) {
                 e.printStackTrace();
                 throw new ProcessExecutionException(1,
                                                     "Atlas Eligibility Check: can't close report file",
                                                     e);
             }
         }
 
         return result;
     }
 
     private Collection<Node> getNodes(Node parentNode, Class typeOfNode,
                                       Collection<Node> nodes) {
         for (Node childNode : parentNode.getChildNodes()) {
             if (childNode.getClass().equals(typeOfNode) &&
                     !nodes.contains(childNode)) {
                 nodes.add(childNode);
             }
             else {
                 getNodes(childNode, typeOfNode, nodes);
             }
         }
         return nodes;
     }
 
     /**
      * Returns the name of this process.
      *
      * @return the name of this process
      */
     public String getName() {
         return "atlas eligibility";
     }
 
     /**
      * Returns a collection of strings representing the names of the parameters.
      *
      * @return the parameter names required to generate a task
      */
     public Collection<ConanParameter> getParameters() {
         return parameters;
     }
 
 
 }
