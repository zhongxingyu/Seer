 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.web.action.analysis;
 
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisService;
 import gov.nih.nci.caintegrator2.application.analysis.grid.GridDiscoveryServiceJob;
 import gov.nih.nci.caintegrator2.application.analysis.grid.gistic.GisticParameters;
 import gov.nih.nci.caintegrator2.application.analysis.grid.gistic.GisticRefgeneFileEnum;
 import gov.nih.nci.caintegrator2.application.query.InvalidCriterionException;
 import gov.nih.nci.caintegrator2.application.query.QueryManagementService;
 import gov.nih.nci.caintegrator2.application.study.StudyManagementService;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.common.GenePatternUtil;
 import gov.nih.nci.caintegrator2.common.HibernateUtil;
 import gov.nih.nci.caintegrator2.domain.application.AnalysisJobStatusEnum;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 import gov.nih.nci.caintegrator2.web.action.AbstractDeployedStudyAction;
 import gov.nih.nci.caintegrator2.web.ajax.IPersistedAnalysisJobAjaxUpdater;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * 
  */
 public class GisticAnalysisAction  extends AbstractDeployedStudyAction {
     
     private static final long serialVersionUID = 1L;
 
     /**
      * Indicates action should open the analysis page.
      */
     public static final String OPEN_ACTION = "open";
     
     /**
      * Indicates action should execute the analysis.
      */
     public static final String EXECUTE_ACTION = "execute";
     
     /**
      * Indicates action should go to the status page.
      */
     public static final String STATUS_ACTION = "status";
 
     private AnalysisService analysisService;
     private QueryManagementService queryManagementService;
     private StudyManagementService studyManagementService;
     private IPersistedAnalysisJobAjaxUpdater ajaxUpdater;
     private String selectedAction = OPEN_ACTION;
 
     /**
      * {@inheritDoc}
      */
     public String execute() {
         if (OPEN_ACTION.equals(getSelectedAction())) {
             return open();
         } else if (EXECUTE_ACTION.equals(getSelectedAction())) {
             return executeAnalysis();
         } else  {
             addActionError("Invalid action: " + getSelectedAction());
             return INPUT;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         super.validate();
         
         if (EXECUTE_ACTION.equals(getSelectedAction())) {
             validateExecuteAnalysis();
         }
     }
 
     private void validateExecuteAnalysis() {
         if (StringUtils.isBlank(getCurrentGisticAnalysisJob().getName())) {
             addFieldError("currentGisticAnalysisJob.name", "Job name required.");
         }
         checkNegativeValue("gisticParameters.amplificationsThreshold",
                 getGisticParameters().getAmplificationsThreshold());
         checkNegativeValue("gisticParameters.deletionsThreshold",
             getGisticParameters().getDeletionsThreshold());
         checkNegativeValue("gisticParameters.joinSegmentSize",
                 getGisticParameters().getJoinSegmentSize());
         checkNegativeValue("gisticParameters.qvThresh",
             getGisticParameters().getQvThresh());
     }
 
     private void checkNegativeValue(String field, Integer value) {
         if (value < 0) {
             addFieldError(field, "Value must be positive.");
         }
     }
     
     private void checkNegativeValue(String field, Float value) {
         if (value < 0) {
             addFieldError(field, "Value must be positive.");
         }
     }
     
     /**
      * @return
      */
     private String open() {
         resetCurrentGisticAnalysisJob();
         loadDefaultValues();
         return SUCCESS;
     }
     
     private void loadDefaultValues() {
         getGisticAnalysisForm().setClinicalQueries(new HashMap<String, String>());
         addClinnicalQueries();
         getGisticAnalysisForm().setGisticParameters(new GisticParameters());
     }
 
     private void addClinnicalQueries() {
         for (Query query 
                 : getStudySubscription().getQueryCollection()) {
             if (ResultTypeEnum.CLINICAL.equals(query.getResultType()) 
                 && !Cai2Util.isCompoundCriterionGenomic(query.getCompoundCriterion())) {
                 getGisticAnalysisForm().getClinicalQueries().put(query.getId().toString(), query.getName());
             }
         }
     }
     
     private String executeAnalysis() {
         try {
             if (!loadParameters()) {
                 return INPUT;
             }
             storeCnvSegmentsToIgnoreFile();
         } catch (InvalidCriterionException e) {
             addActionError(e.getMessage());
             return INPUT;
         } catch (IOException e) {
             addActionError("Unable to save uploaded CNV ignore file.");
             return INPUT;
         }
         getCurrentGisticAnalysisJob().setCreationDate(new Date());
         getCurrentGisticAnalysisJob().setStatus(AnalysisJobStatusEnum.SUBMITTED);
         getStudySubscription().getAnalysisJobCollection()
             .add(getCurrentGisticAnalysisJob());
         getCurrentGisticAnalysisJob().setSubscription(getStudySubscription());
         getWorkspaceService().saveUserWorkspace(getWorkspace());
         ajaxUpdater.runJob(getCurrentGisticAnalysisJob());
         resetCurrentGisticAnalysisJob();
         return STATUS_ACTION;
     }
     
     private void storeCnvSegmentsToIgnoreFile() throws IOException {
         GisticParameters gisticParams = getCurrentGisticAnalysisJob().getGisticAnalysisForm().getGisticParameters();
         File currentCnvFile = gisticParams.getCnvSegmentsToIgnoreFile();
         if (currentCnvFile != null) {
             gisticParams
                     .setCnvSegmentsToIgnoreFile(getStudyManagementService().saveFileToStudyDirectory(
                             getCurrentStudy().getStudyConfiguration(),
                             currentCnvFile));
             
         }
     }
     
     private boolean loadParameters() throws InvalidCriterionException {
         loadServers();
         loadQueries();
         return loadRefgeneFileParameter();
     }
 
     private boolean loadRefgeneFileParameter() throws InvalidCriterionException {
         Set<Sample> samples = GenePatternUtil.getSamplesForGistic(getStudySubscription(), 
                 getQueryManagementService(), getGisticParameters().getClinicalQuery());
         Set<String> genomeVersions = getGenomeVersions(samples);
        if (samples.isEmpty()) {
            addActionError("There are no samples selected.");
            return false;
        } else if (genomeVersions.isEmpty()) {
            addActionError("The samples selected are not related to any copy number data.");
             return false;
         } else if (genomeVersions.size() > 1) {
             addActionError("The samples selected have copy number data loaded for multiple genome build versions.");
             return false;
         } else {
             return loadRefgeneFileParameter(genomeVersions.iterator().next());
         }
     }
 
     private boolean loadRefgeneFileParameter(String genomeVersion) {
         for (GisticRefgeneFileEnum refgeneFile : GisticRefgeneFileEnum.values()) {
             if (refgeneFile.getValue().toLowerCase(Locale.getDefault()).endsWith(
                     genomeVersion.toLowerCase(Locale.getDefault()))) {
                 getGisticParameters().setRefgeneFile(refgeneFile);
                 return true;
             }
         }
         addActionError("Copy number data is related to an unsupported genome build for GISTIC analysis. "
                 + "Valid values are Hg16, Hg17 or Hg18. Data was loaded for array annoted with genome build " 
                 + genomeVersion);
         return false;
     }
 
     private Set<String> getGenomeVersions(Set<Sample> samples) {
         Set<String> genomeVersions = new HashSet<String>();
         for (Sample sample : samples) {
             for (ArrayData arrayData : sample.getArrayDatas(ReporterTypeEnum.DNA_ANALYSIS_REPORTER)) {
                 for (ReporterList reporterList : arrayData.getReporterLists()) {
                     genomeVersions.add(reporterList.getGenomeVersion());
                 }
             }
         }
         return genomeVersions;
     }
 
     private void loadServers() {
         ServerConnectionProfile server = new ServerConnectionProfile();
         server.setUrl(getCurrentGisticAnalysisJob().getGisticUrl());
         getGisticParameters().setServer(server);
     }
     
     private void loadQueries() {
         if (!StringUtils.isBlank(getGisticAnalysisForm().getSelectedQuery())) {
             getGisticParameters().setClinicalQuery(getQuery(getGisticAnalysisForm().getSelectedQuery()));
         }
     }
     
     private Query getQuery(String id) {
         for (Query query 
                 : getStudySubscription().getQueryCollection()) {
             if (id.equals(query.getId().toString())) {
                 HibernateUtil.loadCollection(query);
                 return query;
             }
         }
         return null;
     }
 
     /**
      * @return the analysisService
      */
     public AnalysisService getAnalysisService() {
         return analysisService;
     }
 
     /**
      * @param analysisService the analysisService to set
      */
     public void setAnalysisService(AnalysisService analysisService) {
         this.analysisService = analysisService;
     }
 
     /**
      * @return the queryManagementService
      */
     public QueryManagementService getQueryManagementService() {
         return queryManagementService;
     }
 
     /**
      * @param queryManagementService the queryManagementService to set
      */
     public void setQueryManagementService(QueryManagementService queryManagementService) {
         this.queryManagementService = queryManagementService;
     }
 
     /**
      * @return the ajaxUpdater
      */
     public IPersistedAnalysisJobAjaxUpdater getAjaxUpdater() {
         return ajaxUpdater;
     }
 
     /**
      * @param ajaxUpdater the ajaxUpdater to set
      */
     public void setAjaxUpdater(IPersistedAnalysisJobAjaxUpdater ajaxUpdater) {
         this.ajaxUpdater = ajaxUpdater;
     }
 
     /**
      * @return the selectedAction
      */
     public String getSelectedAction() {
         return selectedAction;
     }
 
     /**
      * @param selectedAction the selectedAction to set
      */
     public void setSelectedAction(String selectedAction) {
         this.selectedAction = selectedAction;
     }
 
     /**
      * @return GisticParameters.
      */
     public GisticParameters getGisticParameters() {
         return getGisticAnalysisForm().getGisticParameters();
     }
 
     /**
      * @return available Gistic services.
      */
     public Map<String, String> getGisticServices() {
         return GridDiscoveryServiceJob.getGridGisticServices();
     }
 
     /**
      * @return the studyManagementService
      */
     public StudyManagementService getStudyManagementService() {
         return studyManagementService;
     }
 
     /**
      * @param studyManagementService the studyManagementService to set
      */
     public void setStudyManagementService(StudyManagementService studyManagementService) {
         this.studyManagementService = studyManagementService;
     }
 }
