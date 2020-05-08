 package uk.ac.ebi.fgpt.conan.process.atlas;
 
 import net.sourceforge.fluxion.spi.ServiceProvider;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
 import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
 import uk.ac.ebi.fgpt.conan.dao.DatabaseConanControlledVocabularyDAO;
 import uk.ac.ebi.fgpt.conan.model.ConanProcess;
 import uk.ac.ebi.fgpt.conan.model.ConanParameter;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.*;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.*;
 import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Process to check experiment eligibility for Atlas.
  * Consists of six steps:
  * 1 check for experiment type,
  * 2 check for two-channel experiment,
  * 3 check for factor values,
  * 4 check for factor types from controlled vocabulary only,
  * 5 check for array design existence in Atlas,
  * 6 check for raw data files for Affy and
  * derived data files for all other platforms.
  *
  * @author Natalja Kurbatova
  * @date 15/02/11
  */
 @ServiceProvider
 public class ExperimentEligibilityCheckingProcess implements ConanProcess {
 
   // Add to the desired logger
   private BufferedWriter log;
 
   private final Collection<ConanParameter> parameters;
   private final AccessionParameter accessionParameter;
 
   private List<String> ArrayDesignAccessions = new ArrayList<String>();
 
   private final DatabaseConanControlledVocabularyDAO controlledVocabularyDAO;
   private final CommonAtlasProcesses atlas = new CommonAtlasProcesses();
 
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
     //deal with parameters
     AccessionParameter accession = new AccessionParameter();
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
 
       ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
       "Can't create report file '" + fileName + "'");
 
       String[] errors = new String[1];
       errors[0] =  "Can't create report file '" + fileName + "'";
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
             "'Experiment Type' " + restrictedExptType + " is not accepted by Atlas\n");
         System.out.println(
             "'Experiment Type' " + restrictedExptType + " is not accepted by Atlas");
 
         ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
         "'Experiment Type' " + restrictedExptType + " is not accepted by Atlas");
 
         String[] errors = new String[1];
         errors[0] = "'Experiment Type' " + restrictedExptType + " is not accepted by Atlas";
         pex.setExceptionCausesAbort();
         pex.setProcessOutput(errors);
         throw pex;
 
       }
       else {
         //2 two-channel experiment
         if (investigation.SDRF.getNumberOfChannels() > 1) {
           exitValue = 1;
           log.write(
               "Two-channel experiment is not accepted by Atlas\n");
           System.out.println(
               "Two-channel experiment is not accepted by Atlas");
           ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
           "Two-channel experiment is not accepted by Atlas");
 
           String[] errors = new String[1];
           errors[0] = "Two-channel experiment is not accepted by Atlas";
           pex.setExceptionCausesAbort();
           pex.setProcessOutput(errors);
           throw pex;
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
         for (HybridizationNode hybNode : hybridizationNodes)
 
         {
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
 
         //3 presence of factor values
         if (factorValues == 0) {
           exitValue = 1;
           log.write(
               "Experiment does not have Factor Values\n");
           System.out.println(
               "Experiment does not have Factor Values");
 
           ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
           "Experiment does not have Factor Values");
 
           String[] errors = new String[1];
           errors[0] = "Experiment does not have Factor Values";
           pex.setExceptionCausesAbort();
           pex.setProcessOutput(errors);
           throw pex;
         }
 
         //4 factor types are from controlled vocabulary
         boolean factorTypesFromCV = true;
         List<String> missedFactorType = new ArrayList<String>();
         for (String factorType : investigation.IDF.experimentalFactorType) {
           if (!controlledVocabularyDAO
               .getAtlasFactorTypes().contains(factorType)) {
             factorTypesFromCV = false;
             missedFactorType.add(factorType);
           }
         }
         if (!factorTypesFromCV) {
           exitValue = 1;
           log.write(
               "Experiment have Factor Types that are not in controlled vocabulary:" +
                   missedFactorType + "\n");
           System.out.println(
               "Experiment have Factor Types that are not in controlled vocabulary:" +
                   missedFactorType);
           ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
           "Experiment have Factor Types that are not in controlled vocabulary:" +
                   missedFactorType);
 
           String[] errors = new String[1];
           errors[0] = "Experiment have Factor Types that are not in controlled vocabulary:" +
                   missedFactorType;
           pex.setExceptionCausesAbort();
           pex.setProcessOutput(errors);
           throw pex;
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
             System.out.println("Array design '" +
                                    arrayDesign +
                                    "' used in experiment is not in Atlas");
             ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
             "Array design '" +
                                    arrayDesign +
                                    "' used in experiment is not in Atlas");
 
             String[] errors = new String[1];
             errors[0] = "Array design '" +
                                    arrayDesign +
                                    "' used in experiment is not in Atlas";
             pex.setExceptionCausesAbort();
             pex.setProcessOutput(errors);
             throw pex;
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
                 hybridizationSubNodes.size() != rawDataSubNodes.size())
             //6a affy
             {
               exitValue = 1;
               log.write(
                   "Affymetrix experiment without raw data files\n");
               System.out.println(
                   "Affymetrix experiment without raw data files");
               ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
               "Affymetrix experiment without raw data files");
 
               String[] errors = new String[1];
               errors[0] = "Affymetrix experiment without raw data files";
               pex.setExceptionCausesAbort();
               pex.setProcessOutput(errors);
               throw pex;
             }
             else
               //6b not affy
               if (processedDataSubNodes.size() == 0 &&
                   processedDataMatrixSubNodes.size() == 0) {
                 exitValue = 1;
                 log.write(
                     "Non-Affymetrix experiment without processed data files\n");
                 System.out.println(
                     "Non-Affymetrix experiment without processed data files");
                 ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                 "Non-Affymetrix experiment without processed data files");
 
                 String[] errors = new String[1];
                 errors[0] = "Non-Affymetrix experiment without processed data files";
                 pex.setExceptionCausesAbort();
                 pex.setProcessOutput(errors);
                 throw pex;
 
               }
           }
         }
       }
     }
     catch (ParseException e) {
       exitValue = 1;
       e.printStackTrace();
       ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                 e.getMessage());
 
       String[] errors = new String[1];
       errors[0] = e.getMessage();
       pex.setProcessOutput(errors);
       throw pex;
     }
     catch (IOException e) {
       exitValue = 1;
       e.printStackTrace();
       ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                 e.getMessage());
 
       String[] errors = new String[1];
       errors[0] = e.getMessage();
       pex.setProcessOutput(errors);
       throw pex;
     }
     catch (RuntimeException e) {
       exitValue = 1;
       e.printStackTrace();
       ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
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
         }
         log.write("Atlas Eligibility Check: FINISHED");
         log.write(
               "Eligibility checks for Gene Expression Atlas version 2.0.9.3: \n" +
               "1. Experiment has raw data for Affymetrix platforms or normalized data for all other platforms;\n" +
               "2. Array design(s) used in experiment are loaded into Atlas;\n" +
               "3. Type of experiment is from the list: \n" +
                   "transcription profiling by array,\n" +
                   "methylation profiling by array,\n" +
                   "tiling path by array,\n" +
                   "comparative genomic hybridization by array,\n" +
                   "microRNA profiling by array,\n" +
                   "RNAi profiling by array,\n" +
                   "ChIP-chip by array;\n" +
               "4. Experiments is not two-channel;\n" +
               "5. Experiment has factor values;\n" +
               "6. Factor types are from controlled vocabulary.");
         log.close();
       }
       catch (IOException e) {
         e.printStackTrace();
         ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                 e.getMessage());
 
         String[] errors = new String[1];
         errors[0] = e.getMessage();
         pex.setProcessOutput(errors);
         throw pex;
       }
     }
 
     ProcessExecutionException pex =  new ProcessExecutionException(exitValue,"Something wrong in the code ");
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
       throws ProcessExecutionException, IllegalArgumentException,
       InterruptedException {
 
     boolean result = false;
 
     // now, parse from a file
     File idfFile = new File(file);
 
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
       result = false;
       e.printStackTrace();
       throw new ProcessExecutionException(1, "Can't create report file '" +
           fileName + "'", e);
     }
 
     // make a new parser
     MAGETABParser parser = new MAGETABParser();
 
 
     try {
       MAGETABInvestigation investigation = parser.parse(idfFile);
       // I check: experiment types
       boolean isAtlasType = false;
       if (investigation.IDF.getComments().containsKey("AEExperimentType")) {
         for (String exptType : investigation.IDF.getComments()
             .get("AEExperimentType")) {
           for (String AtlasType : controlledVocabularyDAO
               .getAtlasExperimentTypes()) {
             if (exptType.equals(AtlasType)) {
               isAtlasType = true;
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
         result = false;
         log.write(
             "Atlas Eligibility Check: 'Experiment Type' is not accepted by Atlas\n");
         System.out.println(
             "Atlas Eligibility Check: 'Experiment Type' is not accepted by Atlas");
         throw new ProcessExecutionException(1,
                                             "Atlas Eligibility Check: 'Experiment Type' is not accepted by Atlas");
       }
       //Ia two-channel experiment
       if (investigation.SDRF.getNumberOfChannels() > 1) {
         result = false;
         log.write(
             "Atlas Eligibility Check: two-channel experiment is not accepted by Atlas\n");
         System.out.println(
             "Atlas Eligibility Check: two-channel experiment is not accepted by Atlas");
         throw new ProcessExecutionException(1,
                                             "Atlas Eligibility Check: two-channel experiment is not accepted by Atlas");
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
       for (HybridizationNode hybNode : hybridizationNodes)
 
       {
         if (hybNode.factorValues.size() > 0) {
           factorValues++;
         }
         for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns) {
           if (!ArrayDesignAccessions
               .contains(arrayDesign.getAttributeValue())) {
             ArrayDesignAccessions.add(arrayDesign.getAttributeValue());
           }
         }
       }
 
       //Ib presence of factor values
       if (factorValues == 0) {
         result = false;
         log.write(
             "Atlas Eligibility Check: experiment does not have Factor Values\n");
         System.out.println(
             "Atlas Eligibility Check: experiment does not have Factor Values");
         throw new ProcessExecutionException(1,
                                             "Atlas Eligibility Check: experiment does not have Factor Values");
       }
 
       //Ic factor types are from controlled vocabulary
       boolean factorTypesFromCV = true;
       List<String> missedFactorType = new ArrayList<String>();
       for (String factorType : investigation.IDF.experimentalFactorType) {
         if (!controlledVocabularyDAO
             .getAtlasFactorTypes().contains(factorType)) {
           factorTypesFromCV = false;
           missedFactorType.add(factorType);
         }
       }
       if (!factorTypesFromCV) {
         result = false;
         log.write(
             "Atlas Eligibility Check: experiment have Factor Types that are not in controlled vocabulary:" +
                 missedFactorType + "\n");
         System.out.println(
             "Atlas Eligibility Check:experiment have Factor Types that are not in controlled vocabulary:" +
                 missedFactorType);
         throw new ProcessExecutionException(1,
                                             "Atlas Eligibility Check: experiment have Factor Types that are not in controlled vocabulary");
       }
 
       // II check: array design is in Atlas
       for (String arrayDesign : ArrayDesignAccessions) {
         ArrayDesignExistenceChecking arrayDesignExistenceChecking =
             new ArrayDesignExistenceChecking();
         String arrayCheckResult =
             arrayDesignExistenceChecking.execute(arrayDesign);
         if (arrayCheckResult.equals("empty") ||
             arrayCheckResult.equals("no")) {
           result = false;
           log.write("Atlas Eligibility Check: array design '" +
                         arrayDesign +
                         "' used in experiment is not in Atlas\n");
           System.out.println("Atlas Eligibility Check: array design '" +
                                  arrayDesign +
                                  "' used in experiment is not in Atlas");
           throw new ProcessExecutionException(1,
                                               "Atlas Eligibility Check: array design '" +
                                                   arrayDesign +
                                                   "' used in experiment is not in Atlas");
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
 
           //III check: if Affy then check for raw data files, else for derived
           if (arrayCheckResult.equals("affy") &&
               hybridizationSubNodes.size() != rawDataSubNodes.size())
           //affy
           {
             result = false;
             log.write(
                 "Atlas Eligibility Check: Affymetrix experiment without raw data files\n");
             System.out.println(
                 "Atlas Eligibility Check: Affymetrix experiment without raw data files");
             throw new ProcessExecutionException(1,
                                                 "Atlas Eligibility Check: Affymetrix experiment without raw data files");
           }
           else
            //not affy
             if (!arrayCheckResult.equals("affy") && processedDataSubNodes.size() == 0 &&
                 processedDataMatrixSubNodes.size() == 0) {
               result = false;
               log.write(
                   "Atlas Eligibility Check: non-Affymetrix experiment without processed data files\n");
               System.out.println(
                   "Atlas Eligibility Check: non-Affymetrix experiment without processed data files");
               throw new ProcessExecutionException(1,
                                                   "Atlas Eligibility Check: non-Affymetrix experiment without processed data files");
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
               "6. Factor types are from controlled vocabulary.");
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
