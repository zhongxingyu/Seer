 package gov.nih.nci.caintegrator.analysis.server;
 
 import gov.nih.nci.caintegrator.analysis.messaging.AnalysisResult;
 import gov.nih.nci.caintegrator.analysis.messaging.GLMSampleGroup;
 import gov.nih.nci.caintegrator.analysis.messaging.GeneralizedLinearModelRequest;
 import gov.nih.nci.caintegrator.analysis.messaging.GeneralizedLinearModelResult;
 import gov.nih.nci.caintegrator.analysis.messaging.GeneralizedLinearModelResultEntry;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.enumeration.CoVariateType;
 import gov.nih.nci.caintegrator.enumeration.StatisticalMethodType;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.rosuda.JRclient.REXP;
 
 /**
  * This is the GLM task that interfaces with the RServe application.
  * 
  *
  * @author caIntegrator Team
  */
 public class GeneralizedLinearModelTaskR extends AnalysisTaskR {
 
     private GeneralizedLinearModelResult glmResult = null;
 
 
     public static final int MIN_GROUP_SIZE = 3;
 
     private static Logger logger = Logger
             .getLogger(GeneralizedLinearModelTaskR.class);
 
     public GeneralizedLinearModelTaskR(GeneralizedLinearModelRequest request) {
         this(request, true);
         logger.debug("constructting glm with true");
     }
 
     public GeneralizedLinearModelTaskR(GeneralizedLinearModelRequest request,
             boolean debugRcommands) {
         super(request, debugRcommands);
         logger.debug("constructting glm with " + debugRcommands);
     }
 
     public void run() {
         logger.debug("starting glm with " + this.getDebugRcommands());
         GeneralizedLinearModelRequest glmRequest = (GeneralizedLinearModelRequest) getRequest();
         glmResult = new GeneralizedLinearModelResult(glmRequest.getSessionId(),
                 glmRequest.getTaskId());
 
         logger
                 .info(getExecutingThreadName()
                         + ": processing generalized linear model request="
                         + glmRequest);
 
         // Validate that all the groups are correct and not overlapping
 
         List<GLMSampleGroup> groups = glmRequest.getComparisonGroups();
         //groups.add((GLMSampleGroup) glmRequest.getBaselineGroup());
 
         boolean errorCondition = false;
         SampleGroup idsSeen = new SampleGroup();
         String errorMsg = null;
         for (SampleGroup group : groups) {
             if (group.size() < 2) {
                 errorMsg = "Group: " + group.getGroupName()
                         + " has less than two members. Sending exception.";
                 logger.error(errorMsg);
                 errorCondition = true;
                 break;
             }
 
             if (idsSeen.containsAny(group)) {
                 errorMsg = "Group: " + group.getGroupName()
                         + " contains overlapping ids. Sending exception.";
                 logger.error(errorMsg);
                 errorCondition = true;
                 break;
             }
 
             idsSeen.addAll(group);
         }
 
         if (errorCondition) {
             AnalysisServerException ex = new AnalysisServerException(
                     "One or more groups have overlapping members or contain less than 3 entries.");
             ex.setFailedRequest(glmRequest);
             logger
                     .error("Groups have overlapping members or less than 3 entries.");
             setException(ex);
             return;
         }
 
         // set the data file
         // check to see if the data file on the compute connection is the
         // same as that for the analysis task
 
         try {
             setDataFile(glmRequest.getDataFileName());
         } catch (AnalysisServerException e) {
             e.setFailedRequest(glmRequest);
             logger
                     .error("Internal Error. Error setting data file to fileName for generalized linear model ="
                             + glmRequest.getDataFileName());
             setException(e);
             return;
         }
 
         // Execute the tasks to perform the GLM analysis
         try {
             SampleGroup baselineGroup = glmRequest.getBaselineGroup();
             List<GLMSampleGroup> sampleGroups = glmRequest
                     .getComparisonGroups();
 
             String glmPatients = "GLMPATIENTS";
             String glmGroups = "GLMGROUPS";
             logger.debug("building");
             List<String> allPatients = null;
             allPatients = createPatientList(baselineGroup, sampleGroups);
             String groupPatientCmd = getGlmPatientGroupCommand(glmPatients,
                     allPatients);
             String groupNameCommand = getGlmGroupNameCommand(glmGroups,
                     allPatients, baselineGroup, sampleGroups);
             logger.debug("about to invoke r");
             doRvoidEval(groupPatientCmd);
             doRvoidEval(groupNameCommand);
             logger.debug("invoking r");
             
             // Filter by gene variance to invrease performance
             Double geneVariance = glmRequest.getGeneVariance();
             String varianceCommand = "subMatrix<-GeneFilterVariance(dataMatrix," + geneVariance.toString() + ")";
             doRvoidEval(varianceCommand);
             String glmCommand = null;
             String commandName = null;
             
             StatisticalMethodType method = glmRequest.getStatisticalMethod();
             if(StatisticalMethodType.ANOVA.equals(method)) {
                 commandName = "eagle.anova.array";
             } else if(StatisticalMethodType.GLM.equals(method)) {
                 commandName = "eagle.glm.array";
             } else {
                 throw new AnalysisServerException("Invalid Statistical Method");
             }
             
             List<CoVariateType> coVariateTypes = glmRequest.getCoVariateTypes();
             if (coVariateTypes == null || coVariateTypes.size() == 0) {
                glmCommand = "glmResult<-" + commandName + "eagle.glm.array(subMatrix, "
                         + glmPatients + ", " + glmGroups + ", FALSE, " + "null"
                         + ")";
             } else {
                 String matrixName = constructDataMatrix(allPatients,
                         (GLMSampleGroup) baselineGroup, sampleGroups);
                 glmCommand = "glmResult<-" + commandName + "(subMatrix, "
                         + glmPatients + ", " + glmGroups + ", TRUE, "
                         + matrixName + ")";
             }
 
             doRvoidEval(glmCommand);
 
             // get the labels
             Vector reporterIds = doREval(
                     "glmReporters <- dimnames(glmResult)[[1]]").asVector();
             List<SampleGroup> resultSampleGroups = new ArrayList<SampleGroup>();
             
             if(glmRequest.getComparisonGroups().size() < 2 && (glmRequest.getCoVariateTypes() == null || glmRequest.getCoVariateTypes().size() < 1)) {
                 String groupId = doREval("glmGroups <- dimnames(glmResult)[[2]]").asString();
                 resultSampleGroups.add(new SampleGroup(groupId));
             } else {
                 Vector groupIds = new Vector();
                 groupIds = doREval("glmGroups <- dimnames(glmResult)[[2]]")
                     .asVector();
                 for (Object groupId : groupIds) {
                     resultSampleGroups.add(new SampleGroup(((REXP) groupId)
                             .asString()));
                 }
             }
 
 
             glmResult.setSampleGroups(resultSampleGroups);
 
             List<GeneralizedLinearModelResultEntry> entries = new ArrayList<GeneralizedLinearModelResultEntry>();
             for (int i = 0; i < reporterIds.size(); i++) {
 
                 GeneralizedLinearModelResultEntry entry = new GeneralizedLinearModelResultEntry();
                 String reporter = ((REXP) reporterIds.get(i)).asString();
                 entry.setReporterId(reporter);
                 double[] pvals = doREval("pval <- glmResult[" + (i+ 1) + ",]")
                         .asDoubleArray();
                 entry.setGroupPvalues(pvals);
                 entries.add(entry);
             }
             glmResult.setGlmResultEntries(entries);
             logger.debug("reporterIds.size=" + reporterIds.size());
             logger.debug("groupIds.size=" + resultSampleGroups.size());
 
             // glmResult.setSampleGroups(sampleGroups);
 
         } catch (AnalysisServerException asex) {
             AnalysisServerException aex = new AnalysisServerException(
                     "Problem computing GLM. Caught AnalysisServerException in FTestTaskR."
                             + asex.getMessage());
             aex.setFailedRequest(glmRequest);
             setException(aex);
             logger.error("Caught AnalysisServerException in GLM");
             logStackTrace(logger, asex);
             return;
         } catch (Exception ex) {
             AnalysisServerException asex = new AnalysisServerException(
                     "Internal Error. Caught Exception in GLM exClass="
                             + ex.getClass() + " msg=" + ex.getMessage());
             asex.setFailedRequest(glmRequest);
             setException(asex);
             logger.error("Caught Exception in GLM");
             logStackTrace(logger, ex);
             return;
         }
 
     }
 
     /**
      * This method constructs a datamatrix for the confounding
      * factors in the GLM analysis.
      * 
      * @param allPatients
      * @param baselineGroup
      * @param sampleGroups
      * @return
      * @throws AnalysisServerException
      */
     private String constructDataMatrix(List<String> allPatients,
             GLMSampleGroup baselineGroup, List<GLMSampleGroup> sampleGroups)
             throws AnalysisServerException {
 
         // Construct the data matrix for the confounding factors
         logger.debug("about to construct data matrix");
         int count = 0;
         Set<String> colNames = null;
         List<String> rowVarNames = new ArrayList<String>();
         String varName = "PATIENT";
         String command = "<-c(";
         String rowValues = null;
 
         for (String currPatient : allPatients) {
             HashMap valueMap = null;
             if (baselineGroup.contains(currPatient)) {
                 valueMap = baselineGroup.getAnnotationMap().get(currPatient);
             } else {
                 for (GLMSampleGroup sg : sampleGroups) {
                     if (sg.contains(currPatient)) {
                         valueMap = sg.getAnnotationMap().get(currPatient);
                         break;
                     }
                 }
             }
             if (colNames == null) {
                 colNames = valueMap.keySet();
             }
             List values = new ArrayList();
             for (String s : colNames) {
                 Object o = valueMap.get(s);
                 Double num = null;
                 try {
                     num = Double.parseDouble(o.toString());
                     values.add(num);
                 } catch(NumberFormatException e) {
                     values.add("\"" + o.toString() + "\"");
                 }
 
             }
             rowValues = StringUtils.join(values.toArray(), ",") ;
             rowVarNames.add(varName + count);
             String rCommand = varName + count + command + rowValues + ")";
             doRvoidEval(rCommand);
             count++;
 
         }
 
 
         logger.debug("about to bind data matrix");
         String bindCmd = "boundCol <- rbind(";
         String matrixName = "GLMMATRIX";
         String matrixCommand = matrixName + "<-as.matrix(boundCol)";
         String dimColumns = "dimnames(" + matrixName + ")[[2]]<-";
         String dimRows = "dimnames(" + matrixName + ")[[1]]<-";
 
         String columnNames = StringUtils.join(rowVarNames.toArray(), ",");
         String cbindCommand = bindCmd + columnNames + ")";
         String columnDimNames = "\""
                 + StringUtils.join(colNames.toArray(), "\",\"") + "\"";
         String rowDimNames = "\""
                 + StringUtils.join(allPatients.toArray(), "\",\"") + "\"";
 
         String columns = "DIMCOLUMNS";
         String rows = "DIMROWS";
 
         doRvoidEval(columns + command + columnDimNames + ")");
         doRvoidEval(rows + command + rowDimNames + ")");
 
         doRvoidEval(cbindCommand);
         doRvoidEval(matrixCommand);
 
         doRvoidEval(dimColumns + columns);
         doRvoidEval(dimRows + rows);
 
         return matrixName;
     }
 
     /**
      * This method creates a list of all the patients in order
      * to create a vector in R.
      * 
      * @param baseline
      * @param comparisons
      * @return
      */
     private List<String> createPatientList(SampleGroup baseline,
             List<GLMSampleGroup> comparisons) {
         String id;
         List<String> patients = new ArrayList<String>();
         if (baseline != null) {
             for (Iterator i = baseline.iterator(); i.hasNext();) {
                 id = (String) i.next();
                 patients.add(id);
             }
         }
         for (SampleGroup group : comparisons) {
             for (Iterator i = group.iterator(); i.hasNext();) {
                 id = (String) i.next();
                 patients.add(id);
             }
         }
         return patients;
     }
 
     /**
      * This creates the R command to create a vactor of paitents.
      * 
      * @param groupName
      * @param patients
      * @return
      */
     public String getGlmPatientGroupCommand(String groupName,
             List<String> patients) {
         String command = groupName + " <- c(" + "\""
                 + StringUtils.join(patients.toArray(), "\",\"") + "\")";
         return command;
     }
 
     /**
      * This creates the R command to create the vector of
      * group names, prepending a 0 to the baseline group, 
      * which the R task requires.
      * 
      * @param groupName
      * @param patients
      * @param baseline
      * @param comparisons
      * @return
      */
     public String getGlmGroupNameCommand(String groupName,
             List<String> patients, SampleGroup baseline,
             List<GLMSampleGroup> comparisons) {
         List<String> groupNames = new ArrayList<String>();
         for (String patientId : patients) {
             if (baseline.contains(patientId)) {
                 groupNames.add("0" + baseline.getGroupName());
             } else {
                 for (SampleGroup g : comparisons) {
                     if (g.contains(patientId)) {
                         groupNames.add(g.getGroupName());
                         break;
                     }
                 }
             }
 
         }
         String command = groupName + " <- c(" + "\""
                 + StringUtils.join(groupNames.toArray(), "\",\"") + "\")";
         return command;
     }
 
     public AnalysisResult getResult() {
         return glmResult;
     }
 
     public GeneralizedLinearModelResult getGeneralizedLinearModelResult() {
         return glmResult;
     }
 
     /**
      * Clean up some of the resources
      */
     public void cleanUp() {
         try {
             setRComputeConnection(null);
         } catch (AnalysisServerException e) {
             logger.error("Error in cleanUp method.");
             logger.error(e);
             setException(e);
         }
     }
 
 }
