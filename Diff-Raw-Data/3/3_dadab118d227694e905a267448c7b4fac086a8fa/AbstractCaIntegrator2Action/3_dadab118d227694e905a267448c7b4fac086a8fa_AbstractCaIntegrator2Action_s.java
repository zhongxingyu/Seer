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
 package gov.nih.nci.caintegrator2.web.action;
 
 import gov.nih.nci.caintegrator2.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator2.common.HibernateUtil;
 import gov.nih.nci.caintegrator2.domain.application.ComparativeMarkerSelectionAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.GenePatternAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.GisticAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.PrincipalComponentAnalysisJob;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.security.SecurityHelper;
 import gov.nih.nci.caintegrator2.web.DisplayableUserWorkspace;
 import gov.nih.nci.caintegrator2.web.SessionHelper;
 import gov.nih.nci.caintegrator2.web.action.analysis.ComparativeMarkerSelectionAnalysisForm;
 import gov.nih.nci.caintegrator2.web.action.analysis.GenePatternAnalysisForm;
 import gov.nih.nci.caintegrator2.web.action.analysis.GisticAnalysisForm;
 import gov.nih.nci.caintegrator2.web.action.analysis.KMPlotForm;
 import gov.nih.nci.caintegrator2.web.action.analysis.PrincipalComponentAnalysisForm;
 import gov.nih.nci.caintegrator2.web.action.analysis.geneexpression.GEPlotForm;
 import gov.nih.nci.caintegrator2.web.action.platform.form.PlatformForm;
 import gov.nih.nci.caintegrator2.web.action.query.DisplayableQueryResult;
 import gov.nih.nci.caintegrator2.web.action.query.DisplayableCopyNumberQueryResult;
 import gov.nih.nci.caintegrator2.web.action.query.form.QueryForm;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.Preparable;
 
 /**
  * Base class for all Struts 2 <code>Actions</code> in the application, provides context set up
  * for the current request.
  */
 @SuppressWarnings("PMD.ExcessiveClassLength")  // A lot of methods
 public abstract class AbstractCaIntegrator2Action extends ActionSupport implements Preparable {
     
     /**
      * Default serialize.
      */
     private static final long serialVersionUID = 1L;
     
     private WorkspaceService workspaceService;
     private String openGeneListName = "";
     private String openSubjectListName = "";
     private String openGlobalGeneListName = "";
     private String openGlobalSubjectListName = "";
 
     /**
      * {@inheritDoc}
      */
     public void prepare() {
         setAuthorizedPage(true);
         setInvalidDataBeingAccessed(false);
         openGeneListName = "";
         openSubjectListName = "";
         openGlobalGeneListName = "";
         openGlobalSubjectListName = "";
         if (!isFileUpload()) {
             prepareValueStack();
         }
     }
     
     /**
      * Transforms String... to String[]
      * @param args to transform to array.
      * @return string array of args.
      */
     protected String[] getArgs(String... args) {
         return args;
     }
     
     /**
      * Call this method in the prepare statement if an object is being accessed that is unauthorized.  By
      * default in this Action's prepare method it will be set to true otherwise.
      * @param isAuthorized T/F value if user is allowed to proceed with requested action.
      */
     protected void setAuthorizedPage(Boolean isAuthorized) {
         SessionHelper.getInstance().setAuthorizedPage(isAuthorized);
     }
     
     /**
      * Call this method in the prepare statement if a study is being accessed that is invalid on a 
      * study type action.
      * @param isInvalidAccess T/F value if user is accessing an invalid data.
      */
     protected void setInvalidDataBeingAccessed(Boolean isInvalidAccess) {
         SessionHelper.getInstance().setInvalidDataBeingAccessed(isInvalidAccess);
     }
 
     /**
      * Override this method to return true for actions that use file upload functionality and ensure that the 
      * <code>execute()</code> method calls <code>prepareValueStack()</code> outside of the prepare method.
      * 
      * @return false if no file upload is used (default), true otherwise (in overrides).
      */
     @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")     // PMD mistakenly flagging as empty method
     protected boolean isFileUpload() {
         return false;
     }
     
     /**
      * Override this method to return true for actions that are "management" type actions.
      * @return false if it's not a management action (default), true otherwise (in overrides).
      */
     @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")     // PMD mistakenly flagging as empty method
     protected boolean isManagementAction() {
         return false;
     }
 
     /**
      * refresh data on the value stack.
      */
     protected final void prepareValueStack() {
         boolean isStudyNeedRefresh = true;
         if (isManagementAction()) {
             isStudyNeedRefresh = false;
             setStudySubscription(null);
         }
         SessionHelper.getInstance().refresh(getWorkspaceService(), isStudyNeedRefresh);
     }
    
     
     /**
      * @return the workspaceService
      */
     protected final WorkspaceService getWorkspaceService() {
         return workspaceService;
     }
 
     /**
      * @param workspaceService the workspaceService to set
      */
     public final void setWorkspaceService(WorkspaceService workspaceService) {
         this.workspaceService = workspaceService;
     }
 
     /**
      * @return the workspace
      */
     protected final UserWorkspace getWorkspace() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getUserWorkspace();
         } else {
             return null;
         }
     }
 
     /**
      * @return the displayableWorkspace
      */
     protected final DisplayableUserWorkspace getDisplayableWorkspace() {
         return SessionHelper.getInstance().getDisplayableUserWorkspace();
     }
 
     /**
      * @return the studySubscription
      */
     protected final StudySubscription getStudySubscription() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentStudySubscription();
         } else {
             return null;
         }
     }
 
     /**
      * @param studySubscription the studySubscription to set
      */
     protected final void setStudySubscription(StudySubscription studySubscription) {
         getDisplayableWorkspace().setCurrentStudySubscription(studySubscription);
     }
 
     /**
      * @return the study
      */
     protected Study getStudy() {
         if (getStudySubscription() != null) {
             return getStudySubscription().getStudy();
         } else {
             return null;
         }
     }
     
     /**
      * Refreshes genomic sources for any page that needs it.
      */
     protected final void refreshGenomicSources() {
         if (getCurrentStudy() != null) {
             HibernateUtil.loadGenomicSources(getWorkspaceService().getRefreshedEntity(
                     getCurrentStudy().getStudyConfiguration()).getGenomicDataSources());
         }
     }
     
     /**
      * Public wrapper for "getStudy()" so that JSP's can access the current study on the session through OGNL.
      * @return the study
      */
     public Study getCurrentStudy() {
         return getStudy();
     }
 
     /**
      * @return the query form
      */
     public Query getQuery() {
         if (getQueryForm() != null) {
             return getQueryForm().getQuery();
         } else {
             return null;
         }
     }
 
     /**
      * @return the query form
      */
     public QueryForm getQueryForm() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getQueryForm();
         } else {
             return null;
         }
     }
 
     /**
      * @return the platform form
      */
     public PlatformForm getPlatformForm() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getPlatformForm();
         } else {
             return null;
         }
     }
 
     /**
      * @return the queryResult
      */
     public final DisplayableQueryResult getQueryResult() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getQueryResult();
         } else {
             return null;
         }
     }
 
     /**
      * @return the genomicDataQueryResult
      */
     public final GenomicDataQueryResult getGenomicDataQueryResult() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getGenomicDataQueryResult();
         } else {
             return null;
         }
     }
 
     /**
      * @return the DisplayableCopyNumberQueryResult
      */
     protected final DisplayableCopyNumberQueryResult getCopyNumberQueryResult() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCopyNumberQueryResult();
         } else {
             return null;
         }
     }
     
     /**
      * Resets the query results.
      */
     protected final void resetQueryResult() {
         setQueryResult(null);
         setGenomicDataQueryResult(null);
     }
 
     /**
      * @param queryResult the queryResult to set
      */
     protected final void setQueryResult(DisplayableQueryResult queryResult) {
         getDisplayableWorkspace().setQueryResult(queryResult);
     }
 
     /**
      * @param genomicDataQueryResult the genomicDataQueryResult to set
      */
     protected final void setGenomicDataQueryResult(GenomicDataQueryResult genomicDataQueryResult) {
         getDisplayableWorkspace().setGenomicDataQueryResult(genomicDataQueryResult);
     }
 
     /**
      * @param result the DisplayableCopyNumberQueryResult to set
      */
     protected final void setCopyNumberQueryResult(DisplayableCopyNumberQueryResult result) {
         getDisplayableWorkspace().setCopyNumberQueryResult(result);
     }
 
     /**
      * @return the analysisForm
      */
     public GenePatternAnalysisForm getGenePatternAnalysisForm() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentGenePatternAnalysisJob().getGenePatternAnalysisForm();
         } else {
             return null;
         }
     }
 
     /**
      * @return the analysisForm
      */
     public ComparativeMarkerSelectionAnalysisForm getComparativeMarkerSelectionAnalysisForm() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentComparativeMarkerSelectionAnalysisJob().
                 getForm();
         } else {
             return null;
         }
     }
     
     /**
      * @return the analysisForm
      */
     public PrincipalComponentAnalysisForm getPrincipalComponentAnalysisForm() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentPrincipalComponentAnalysisJob().
                 getForm();
         } else {
             return null;
         }
     }
     
     /**
      * @return the analysisForm
      */
     public GisticAnalysisForm getGisticAnalysisForm() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentGisticAnalysisJob().
                 getGisticAnalysisForm();
         } else {
             return null;
         }
     }
     
     /**
      * @return the currentGenePatternAnalysisJob.
      */
     public GenePatternAnalysisJob getCurrentGenePatternAnalysisJob() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentGenePatternAnalysisJob();
         } else {
             return null;
         }
     }
     
     /**
      * Resets the current GenePattern job.
      */
     protected void resetCurrentGenePatternAnalysisJob() {
         if (getDisplayableWorkspace() != null) {
             getDisplayableWorkspace().setCurrentGenePatternAnalysisJob(new GenePatternAnalysisJob());
         }
     }
     
     /**
      * @return the currentComparativeMarkerSelectionAnalysisJob.
      */
     public ComparativeMarkerSelectionAnalysisJob getCurrentComparativeMarkerSelectionAnalysisJob() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentComparativeMarkerSelectionAnalysisJob();
         } else {
             return null;
         }
     }
     
     /**
      * Resets the current ComparativeMarkerSelection job.
      */
     protected void resetCurrentComparativeMarkerSelectionAnalysisJob() {
         if (getDisplayableWorkspace() != null) {
             getDisplayableWorkspace().setCurrentComparativeMarkerSelectionAnalysisJob(
                                             new ComparativeMarkerSelectionAnalysisJob());
         }
     }
     
     /**
      * @return the currentComparativePCAAnalysisJob.
      */
     public PrincipalComponentAnalysisJob getCurrentPrincipalComponentAnalysisJob() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentPrincipalComponentAnalysisJob();
         } else {
             return null;
         }
     }
     
     /**
      * Resets the current PCA job.
      */
     protected void resetCurrentPrincipalComponentAnalysisJob() {
         if (getDisplayableWorkspace() != null) {
             getDisplayableWorkspace().setCurrentPrincipalComponentAnalysisJob(new PrincipalComponentAnalysisJob());
         }
     }
     
     /**
      * @return the currentGisticAnalysisJob.
      */
     public GisticAnalysisJob getCurrentGisticAnalysisJob() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getCurrentGisticAnalysisJob();
         } else {
             return null;
         }
     }
     
     /**
      * Resets the current GISTIC job.
      */
     protected void resetCurrentGisticAnalysisJob() {
         if (getDisplayableWorkspace() != null) {
             getDisplayableWorkspace().setCurrentGisticAnalysisJob(new GisticAnalysisJob());
         }
     }
 
     /**
      * @return the KM Plot form
      */
     public KMPlotForm getKmPlotForm() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getKmPlotForm();
         } else {
             return null;
         }
     }
     
     /**
      * 
      * @return the gePlotForm object.
      */
     public GEPlotForm getGePlotForm() {
         if (getDisplayableWorkspace() != null) {
             return getDisplayableWorkspace().getGePlotForm();
         } else {
             return null;
         }
     }
     
     /**
      * Clears the analysis form cache.
      */
     protected void clearAnalysisCache() {
         if (getDisplayableWorkspace() != null) {
             getDisplayableWorkspace().clearAnalysisCache();
         }
     }
 
     /**
      * @return a Map of annotation types
      */
     public Map<String, String> getAnnotationTypes() {
         Map<String, String> annotationTypes = new HashMap<String, String>();
         annotationTypes.put(EntityTypeEnum.SUBJECT.getValue(), "Subject");
         if (getStudy().hasVisibleImageSeriesData()) {
             annotationTypes.put(EntityTypeEnum.IMAGESERIES.getValue(), "Image Series");
         }
         return annotationTypes;
     }
 
     /**
      * @return the openGeneListName
      */
     public String getOpenGeneListName() {
         return openGeneListName;
     }
 
     /**
      * @param openGeneListName the openGeneListName to set
      */
     public void setOpenGeneListName(String openGeneListName) {
         this.openGeneListName = openGeneListName;
     }
 
     /**
      * @return the openSubjectListName
      */
     public String getOpenSubjectListName() {
         return openSubjectListName;
     }
 
     /**
      * @param openSubjectListName the openSubjectListName to set
      */
     public void setOpenSubjectListName(String openSubjectListName) {
         this.openSubjectListName = openSubjectListName;
     }
 
     /**
      * @return the openGlobalGeneListName
      */
     public String getOpenGlobalGeneListName() {
         return openGlobalGeneListName;
     }
 
     /**
      * @param openGlobalGeneListName the openGlobalGeneListName to set
      */
     public void setOpenGlobalGeneListName(String openGlobalGeneListName) {
         this.openGlobalGeneListName = openGlobalGeneListName;
     }
 
     /**
      * @return the openGlobalSubjectListName
      */
     public String getOpenGlobalSubjectListName() {
         return openGlobalSubjectListName;
     }
 
     /**
      * @param openGlobalSubjectListName the openGlobalSubjectListName to set
      */
     public void setOpenGlobalSubjectListName(String openGlobalSubjectListName) {
         this.openGlobalSubjectListName = openGlobalSubjectListName;
     }
     
     /**
      * 
      * @return if user is anonymous or not.
      */
     public boolean isAnonymousUser() {
         return (getWorkspace() != null && UserWorkspace.ANONYMOUS_USER_NAME.equals(getWorkspace().getUsername())) 
             ? true : false;
     }
     
     /**
      * @return is the current user a study manager
      */
     public boolean isStudyManager() {
         return (isAnonymousUser()) ? false : SecurityHelper.isStudyManager(SecurityHelper.getCurrentUsername());
     }
    
     /**
      * @param inputString the string being parsed
      * @return the same string with html characters removed or empty string.
      */
     protected static final String removeHtmlChars(String inputString) {
         if (inputString != null) {
             return inputString.replaceAll("\\<[^>]*>", StringUtils.EMPTY);
         }
         return StringUtils.EMPTY;
     }    
     
 }
