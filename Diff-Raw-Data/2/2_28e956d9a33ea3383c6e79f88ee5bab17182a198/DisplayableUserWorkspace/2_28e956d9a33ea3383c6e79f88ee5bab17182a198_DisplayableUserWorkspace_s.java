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
 package gov.nih.nci.caintegrator2.web;
 
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapParameters;
 import gov.nih.nci.caintegrator2.application.analysis.igv.IGVParameters;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyLogo;
 import gov.nih.nci.caintegrator2.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator2.common.ConfigurationParameter;
 import gov.nih.nci.caintegrator2.domain.analysis.AbstractCopyNumberAnalysis;
 import gov.nih.nci.caintegrator2.domain.analysis.GisticAnalysis;
 import gov.nih.nci.caintegrator2.domain.application.ComparativeMarkerSelectionAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.GeneList;
 import gov.nih.nci.caintegrator2.domain.application.GenePatternAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.GisticAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.PrincipalComponentAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.application.SubjectList;
 import gov.nih.nci.caintegrator2.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.external.cabio.CaBioDisplayablePathway;
 import gov.nih.nci.caintegrator2.external.ncia.NCIADicomJob;
 import gov.nih.nci.caintegrator2.web.action.analysis.KMPlotForm;
 import gov.nih.nci.caintegrator2.web.action.analysis.geneexpression.GEPlotForm;
 import gov.nih.nci.caintegrator2.web.action.platform.form.PlatformForm;
 import gov.nih.nci.caintegrator2.web.action.query.DisplayableCopyNumberQueryResult;
 import gov.nih.nci.caintegrator2.web.action.query.DisplayableQueryResult;
 import gov.nih.nci.caintegrator2.web.action.query.form.QueryForm;
 import gov.nih.nci.caintegrator2.web.action.study.management.DataElementSearchObject;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.util.ValueStack;
 
 /**
  * Session singleton object used for displaying user workspace items such as Study's, Queries, and Lists.
  */
 @SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveClassLength" }) // Keep track of all current jobs
 public class DisplayableUserWorkspace {
 
     /**
      * Value to use in UI for no study in drop down list.
      */
     public static final Long NO_STUDY_SELECTED_ID = Long.valueOf(-1L);
     
     private static final String PUBLIC_STUDIES_HEADER = "-- Public Studies --";
     private static final String USER_WORKSPACE_VALUE_STACK_KEY = "workspace";
     private static final String CURRENT_STUDY_SUBSCRIPTION_VALUE_STACK_KEY = "studySubscription";
     private static final String CURRENT_STUDY_VALUE_STACK_KEY = "study";
     private static final String CURRENT_QUERY_RESULT_VALUE_STACK_KEY = "queryResult";
     private static final String CURRENT_GENOMIC_RESULT_VALUE_STACK_KEY = "genomicDataQueryResult";
     private static final String CURRENT_COPY_NUMBER_RESULT_VALUE_STACK_KEY = "copyNumberQueryResult";
     private static final String CURRENT_STUDY_LOGO_KEY = "studyLogo";
     
     private Long currentStudySubscriptionId;
     private GenePatternAnalysisJob currentGenePatternAnalysisJob = new GenePatternAnalysisJob();
     private ComparativeMarkerSelectionAnalysisJob currentComparativeMarkerSelectionAnalysisJob
         = new ComparativeMarkerSelectionAnalysisJob();
     private PrincipalComponentAnalysisJob currentPrincipalComponentAnalysisJob
         = new PrincipalComponentAnalysisJob();
     private GisticAnalysisJob currentGisticAnalysisJob = new GisticAnalysisJob();
     private final PlatformForm platformForm = new PlatformForm();
     private final QueryForm queryForm = new QueryForm();
     private final KMPlotForm kmPlotForm = new KMPlotForm();
     private final GEPlotForm gePlotForm = new GEPlotForm();
     private IGVParameters igvParameters = new IGVParameters();
     private HeatmapParameters heatmapParameters = new HeatmapParameters();
     private DisplayableQueryResult queryResult;
     private GenomicDataQueryResult genomicDataQueryResult;
     private DisplayableCopyNumberQueryResult copyNumberQueryResult;
     private NCIADicomJob dicomJob;
     private final DataElementSearchObject dataElementSearchObject = new DataElementSearchObject();
     private boolean createPlotSelected = false;
     private boolean createPlotRunning = false;
     private DownloadableFile temporaryDownloadFile;
     private final Set<StudyConfiguration> managedStudies = new HashSet<StudyConfiguration>();
     private StudyConfiguration currentStudyConfiguration;
     private List<CaBioDisplayablePathway> caBioPathways = new ArrayList<CaBioDisplayablePathway>();
     
     /**
      * Refreshes the workspace for this session, ensuring it is attached to the current Hibernate request.
      *
      * @param workspaceService service used to
      * @param isStudyNeedRefresh determines if we need to refresh study on the stack or not. 
      */
     public void refresh(WorkspaceService workspaceService, boolean isStudyNeedRefresh) {
             setUserWorkspace(retrieveUserWorkspace(workspaceService));
             subscribeAllStudies(workspaceService);
             if (isStudyNeedRefresh) {
                 refreshStudyObjects();    
             }
     }
     
     private void subscribeAllStudies(WorkspaceService workspaceService) {
         if (SessionHelper.isAnonymousUser()) {
             workspaceService.subscribeAllReadOnly(getUserWorkspace());
         } else {
             workspaceService.subscribeAll(getUserWorkspace());
         }
     }
 
     private UserWorkspace retrieveUserWorkspace(WorkspaceService workspaceService) {
         if (SessionHelper.isAnonymousUser()) {
             if (SessionHelper.getAnonymousUserWorkspace() == null) {
                 SessionHelper.setAnonymousUserWorkspace(workspaceService.getWorkspaceReadOnly());
             }
             workspaceService.refreshWorkspaceStudies(SessionHelper.getAnonymousUserWorkspace());
             return SessionHelper.getAnonymousUserWorkspace();
         }
         return workspaceService.getWorkspace();
     }
     
     private void refreshStudyObjects() {
         if (getCurrentStudySubscriptionId() == null  && getUserWorkspace().getDefaultSubscription() != null) {
             currentStudySubscriptionId = getUserWorkspace().getDefaultSubscription().getId();
         }
         putCurrentStudyOnValueStack();
         refreshQueryForm();
         putResultObjectsOnValueStack();
     }
 
     private void refreshQueryForm() {
         if (queryForm.getQuery() != null) {
             queryForm.getQuery().setSubscription(getCurrentStudySubscription());
         }
     }
 
     /**
      * @return the userWorkspace
      */
     public UserWorkspace getUserWorkspace() {
         return (UserWorkspace) getValueStack().findValue(USER_WORKSPACE_VALUE_STACK_KEY);
     }
 
     private ValueStack getValueStack() {
         return ActionContext.getContext().getValueStack();
     }
 
     private void setUserWorkspace(UserWorkspace userWorkspace) {
         getValueStack().set(USER_WORKSPACE_VALUE_STACK_KEY, userWorkspace);
     }
     
     /**
      * @return the studyLogo
      */
     public StudyLogo getStudyLogo() {
         return (StudyLogo) getValueStack().findValue(CURRENT_STUDY_LOGO_KEY);
     }
     
     /**
      * Sets current study logo.
      * @param studyLogo to set.
      */
     public void setStudyLogo(StudyLogo studyLogo) {
         getValueStack().set(CURRENT_STUDY_LOGO_KEY, studyLogo);
     }
 
     /**
      * @return the currentStudySubscription
      */
     public StudySubscription getCurrentStudySubscription() {
         return (StudySubscription) getValueStack().findValue(CURRENT_STUDY_SUBSCRIPTION_VALUE_STACK_KEY);
     }
 
     /**
      * @param currentStudySubscription the currentStudySubscription to set
      */
     public void setCurrentStudySubscription(StudySubscription currentStudySubscription) {
         if (currentStudySubscription == null) {
             setCurrentStudySubscriptionId(null);
         } else {
             setCurrentStudySubscriptionId(currentStudySubscription.getId());
         }
     }
 
     /**
      * @return the currentStudySubscriptionId
      */
     public Long getCurrentStudySubscriptionId() {
         return currentStudySubscriptionId;
     }
 
     /**
      * @param currentStudySubscriptionId the currentStudySubscriptionId to set
      */
     public void setCurrentStudySubscriptionId(Long currentStudySubscriptionId) {
         if (currentStudySubscriptionId == null || currentStudySubscriptionId.equals(NO_STUDY_SELECTED_ID)) {
             setQueryResult(null);
             setGenomicDataQueryResult(null);
             setCopyNumberQueryResult(null);
             getQueryForm().setQuery(null, null, null, null);
         }
         this.currentStudySubscriptionId = currentStudySubscriptionId;
         putCurrentStudyOnValueStack();
     }
 
     private void putCurrentStudyOnValueStack() {
         if (getUserWorkspace() == null) {
             return;
         }
         StudySubscription currentStudySubscription = null;
         Study currentStudy = null;
         for (StudySubscription subscription : getUserWorkspace().getSubscriptionCollection()) {
             if (subscription.getId().equals(getCurrentStudySubscriptionId())) {
                 currentStudySubscription = subscription;
                 currentStudy = subscription.getStudy();
             }
         }
         getValueStack().set(CURRENT_STUDY_SUBSCRIPTION_VALUE_STACK_KEY, currentStudySubscription);
         getValueStack().set(CURRENT_STUDY_VALUE_STACK_KEY, currentStudy);
     }
     
     /**
      * @return the subscriptionCollection
      */
     public List<StudySubscription> getOrderedSubscriptionList() {
         List<StudySubscription> orderedSubscriptionCollection = new ArrayList<StudySubscription>();
         List<StudySubscription> publicSubscriptionCollection = new ArrayList<StudySubscription>();
         if (getUserWorkspace().getSubscriptionCollection() != null) {
             for (StudySubscription studySubscription : getUserWorkspace().getSubscriptionCollection()) {
                 if (Status.DEPLOYED.equals(studySubscription.getStudy().getStudyConfiguration().getStatus())) {
                     if (studySubscription.isPublicSubscription()) {
                         publicSubscriptionCollection.add(studySubscription);
                     } else {
                         orderedSubscriptionCollection.add(studySubscription);
                     }
                 }
             }
         }
         sortStudySubscriptions(orderedSubscriptionCollection);
         addPublicSubscriptions(orderedSubscriptionCollection, publicSubscriptionCollection);
         return orderedSubscriptionCollection;
     }
 
     private void addPublicSubscriptions(List<StudySubscription> orderedSubscriptionCollection,
             List<StudySubscription> publicSubscriptionCollection) {
         if (!publicSubscriptionCollection.isEmpty()) {
             if (!orderedSubscriptionCollection.isEmpty()) {
                 StudySubscription publicStudyHeader = new StudySubscription();
                 publicStudyHeader.setId(NO_STUDY_SELECTED_ID);
                 publicStudyHeader.setStudy(new Study());
                 publicStudyHeader.getStudy().setShortTitleText(PUBLIC_STUDIES_HEADER);
                 sortStudySubscriptions(publicSubscriptionCollection);
                 orderedSubscriptionCollection.add(publicStudyHeader);
             }
             orderedSubscriptionCollection.addAll(publicSubscriptionCollection);
         }
     }
 
     private void sortStudySubscriptions(List<StudySubscription> orderedSubscriptionCollection) {
         Comparator<StudySubscription> nameComparator = new Comparator<StudySubscription>() {
             public int compare(StudySubscription subscription1, StudySubscription subscription2) {
                 return subscription1.getStudy().getShortTitleText().
                        compareToIgnoreCase(subscription2.getStudy().getShortTitleText());
             }
         };
         Collections.sort(orderedSubscriptionCollection, nameComparator);
     }
     
     private void putResultObjectsOnValueStack() {
         getValueStack().set(CURRENT_QUERY_RESULT_VALUE_STACK_KEY, getQueryResult());
         getValueStack().set(CURRENT_GENOMIC_RESULT_VALUE_STACK_KEY, getGenomicDataQueryResult());
         getValueStack().set(CURRENT_COPY_NUMBER_RESULT_VALUE_STACK_KEY, getCopyNumberQueryResult());
     }
 
     /**
      * @return the queryResult
      */
     public DisplayableQueryResult getQueryResult() {
         return queryResult;
     }
 
     /**
      * @param queryResult the queryResult to set
      */
     public void setQueryResult(DisplayableQueryResult queryResult) {
         this.queryResult = queryResult;
         getValueStack().set(CURRENT_QUERY_RESULT_VALUE_STACK_KEY, queryResult);
     }
 
     /**
      * @return the genomicDataQueryResult
      */
     public GenomicDataQueryResult getGenomicDataQueryResult() {
         return genomicDataQueryResult;
     }
 
     /**
      * @param genomicDataQueryResult the genomicDataQueryResult to set
      */
     public void setGenomicDataQueryResult(GenomicDataQueryResult genomicDataQueryResult) {
         this.genomicDataQueryResult = genomicDataQueryResult;
         getValueStack().set(CURRENT_GENOMIC_RESULT_VALUE_STACK_KEY, genomicDataQueryResult);
     }
     
     /**
      * @return the copyNumberQueryResult
      */
     public DisplayableCopyNumberQueryResult getCopyNumberQueryResult() {
         return copyNumberQueryResult;
     }
 
     /**
      * @param copyNumberQueryResult the copyNumberQueryResult to set
      */
     public void setCopyNumberQueryResult(DisplayableCopyNumberQueryResult copyNumberQueryResult) {
         this.copyNumberQueryResult = copyNumberQueryResult;
         getValueStack().set(CURRENT_COPY_NUMBER_RESULT_VALUE_STACK_KEY, getCopyNumberQueryResult());
     }
 
     /**
      * Retrieve the URL for CGAP.
      * @return - URL for CGAP.
      */
     public String getCgapUrl() {
         return ConfigurationParameter.CGAP_URL.getDefaultValue();
     }
 
     /**
      * @return the queryForm
      */
     public QueryForm getQueryForm() {
         return queryForm;
     }
     
     /**
      * @return the user's queries, ordered by name.
      */
     public List<Query> getUserQueries() {
         List<Query> queries = new ArrayList<Query>();
         if (getCurrentStudySubscription() != null) {
             queries.addAll(getCurrentStudySubscription().getQueryCollection());
         }
         Comparator<Query> nameComparator = new Comparator<Query>() {
             public int compare(Query query1, Query query2) {
                 return query1.getName().compareToIgnoreCase(query2.getName());
             }
         };
         Collections.sort(queries, nameComparator);
         return queries;
     }
     
     /**
     * @return the user's queries, ordered by name.
      */
     public List<GisticAnalysis> getUserGisticAnalysisList() {
         List<GisticAnalysis> gisticAnalysisList = new ArrayList<GisticAnalysis>();
         if (getCurrentStudySubscription() != null) {
             for (AbstractCopyNumberAnalysis copyNumberAnalysis 
                     : getCurrentStudySubscription().getCopyNumberAnalysisCollection()) {
                 if (copyNumberAnalysis instanceof GisticAnalysis) {
                     gisticAnalysisList.add((GisticAnalysis) copyNumberAnalysis);
                 }
             }
         }
         Comparator<GisticAnalysis> nameComparator = new Comparator<GisticAnalysis>() {
             public int compare(GisticAnalysis gisticAnalysis1, GisticAnalysis gisticAnalysis2) {
                 return gisticAnalysis1.getName().compareToIgnoreCase(gisticAnalysis2.getName());
             }
         };
         Collections.sort(gisticAnalysisList, nameComparator);
         return gisticAnalysisList;
     }
     
     /**
      * @return the saved gene lists.
      */
     public List<GeneList> getGeneLists() {
         return (getCurrentStudySubscription() == null)
             ? null : getCurrentStudySubscription().getGeneLists();
     }
     
     /**
      * @return the saved subject lists.
      */
     public List<SubjectList> getSubjectLists() {
         return (getCurrentStudySubscription() == null)
             ? null : getCurrentStudySubscription().getSubjectLists();
     }
     
     /**
      * @return the saved gene lists.
      */
     public List<GeneList> getGlobalGeneLists() {
         return (getCurrentStudySubscription() == null)
             ? null : getCurrentStudySubscription().getStudy().getStudyConfiguration().getGeneLists();
     }
     
     /**
      * @return the saved subject lists.
      */
     public List<SubjectList> getGlobalSubjectLists() {
         return (getCurrentStudySubscription() == null)
             ? null : getCurrentStudySubscription().getStudy().getStudyConfiguration().getSubjectLists();
     }
 
     /**
      * @return the dicomJob
      */
     public NCIADicomJob getDicomJob() {
         return dicomJob;
     }
 
     /**
      * @param dicomJob the dicomJob to set
      */
     public void setDicomJob(NCIADicomJob dicomJob) {
         this.dicomJob = dicomJob;
     }
 
     /**
      * @return the kmPlotForm
      */
     public KMPlotForm getKmPlotForm() {
         return kmPlotForm;
     }
     
     /**
      * @return the gePlotForm
      */
     public GEPlotForm getGePlotForm() {
         return gePlotForm;
     }
     
     /**
      * @return the dataElementSearchObject
      */
     public DataElementSearchObject getDataElementSearchObject() {
         return dataElementSearchObject;
     }
 
     /**
      * @return the createPlotSelected
      */
     public boolean isCreatePlotSelected() {
         return createPlotSelected;
     }
 
     /**
      * @param createPlotSelected the createPlotSelected to set
      */
     public void setCreatePlotSelected(boolean createPlotSelected) {
         this.createPlotSelected = createPlotSelected;
     }
 
     /**
      * @return the createPlotRunning
      */
     public boolean isCreatePlotRunning() {
         return createPlotRunning;
     }
 
     /**
      * @param createPlotRunning the createPlotRunning to set
      */
     public void setCreatePlotRunning(boolean createPlotRunning) {
         this.createPlotRunning = createPlotRunning;
     }
 
     /**
      * @return the currentAnalysisJob
      */
     public GenePatternAnalysisJob getCurrentGenePatternAnalysisJob() {
         return currentGenePatternAnalysisJob;
     }
 
     /**
      * @param currentGenePatternAnalysisJob the currentGenePatternAnalysisJob to set
      */
     public void setCurrentGenePatternAnalysisJob(GenePatternAnalysisJob currentGenePatternAnalysisJob) {
         this.currentGenePatternAnalysisJob = currentGenePatternAnalysisJob;
     }
 
     /**
      * @return the currentComparativeMarkerSelectionAnalysisJob
      */
     public ComparativeMarkerSelectionAnalysisJob getCurrentComparativeMarkerSelectionAnalysisJob() {
         return currentComparativeMarkerSelectionAnalysisJob;
     }
 
     /**
      * @param currentComparativeMarkerSelectionAnalysisJob the currentComparativeMarkerSelectionAnalysisJob to set
      */
     public void setCurrentComparativeMarkerSelectionAnalysisJob(
             ComparativeMarkerSelectionAnalysisJob currentComparativeMarkerSelectionAnalysisJob) {
         this.currentComparativeMarkerSelectionAnalysisJob = currentComparativeMarkerSelectionAnalysisJob;
     }
 
     /**
      * @return the temporaryDownloadFile
      */
     public DownloadableFile getTemporaryDownloadFile() {
         return temporaryDownloadFile;
     }
 
     /**
      * @param temporaryDownloadFile the temporaryDownloadFile to set
      */
     public void setTemporaryDownloadFile(DownloadableFile temporaryDownloadFile) {
         this.temporaryDownloadFile = temporaryDownloadFile;
     }
 
     /**
      * @return the currentPrincipalComponentAnalysisJob
      */
     public PrincipalComponentAnalysisJob getCurrentPrincipalComponentAnalysisJob() {
         return currentPrincipalComponentAnalysisJob;
     }
 
     /**
      * @param currentPrincipalComponentAnalysisJob the currentPrincipalComponentAnalysisJob to set
      */
     public void setCurrentPrincipalComponentAnalysisJob(
             PrincipalComponentAnalysisJob currentPrincipalComponentAnalysisJob) {
         this.currentPrincipalComponentAnalysisJob = currentPrincipalComponentAnalysisJob;
     }
 
     /**
      * @return the currentGisticAnalysisJob
      */
     public GisticAnalysisJob getCurrentGisticAnalysisJob() {
         return currentGisticAnalysisJob;
     }
 
     /**
      * @param currentGisticAnalysisJob the currentGisticAnalysisJob to set
      */
     public void setCurrentGisticAnalysisJob(
             GisticAnalysisJob currentGisticAnalysisJob) {
         this.currentGisticAnalysisJob = currentGisticAnalysisJob;
     }
 
     /**
      * @return the platformForm
      */
     public PlatformForm getPlatformForm() {
         return platformForm;
     }
 
     /**
      * @return the managedStudies
      */
     public Set<StudyConfiguration> getManagedStudies() {
         return managedStudies;
     }
 
     /**
      * @return the currentStudyConfiguration
      */
     public StudyConfiguration getCurrentStudyConfiguration() {
         return currentStudyConfiguration;
     }
 
     /**
      * @param currentStudyConfiguration the currentStudyConfiguration to set
      */
     public void setCurrentStudyConfiguration(StudyConfiguration currentStudyConfiguration) {
         this.currentStudyConfiguration = currentStudyConfiguration;
     }
 
     /**
      * @return the caBioPathways
      */
     public List<CaBioDisplayablePathway> getCaBioPathways() {
         return caBioPathways;
     }
 
     /**
      * @param caBioPathways the caBioPathways to set
      */
     public void setCaBioPathways(List<CaBioDisplayablePathway> caBioPathways) {
         this.caBioPathways = caBioPathways;
     }
     
     /**
      * Clears the analysis form cache.
      */
     public void clearAnalysisCache() {
         gePlotForm.clear();
         kmPlotForm.clear();
     }
 
     /**
      * @return the igvParameters
      */
     public IGVParameters getIgvParameters() {
         return igvParameters;
     }
 
     /**
      * @param igvParameters the igvParameters to set
      */
     public void setIgvParameters(IGVParameters igvParameters) {
         this.igvParameters = igvParameters;
     }
 
     /**
      * @return the igvParameters
      */
     public HeatmapParameters getHeatmapParameters() {
         return heatmapParameters;
     }
 
     /**
      * @param heatmapParameters the heatmapParameters to set
      */
     public void setHeatmapParameters(HeatmapParameters heatmapParameters) {
         this.heatmapParameters = heatmapParameters;
     }
 
 
 }
