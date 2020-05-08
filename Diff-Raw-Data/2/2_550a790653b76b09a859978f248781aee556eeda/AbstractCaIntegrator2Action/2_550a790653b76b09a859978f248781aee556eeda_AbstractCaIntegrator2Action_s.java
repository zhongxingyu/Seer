 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.web.action;
 
 import gov.nih.nci.caintegrator.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator.domain.application.ComparativeMarkerSelectionAnalysisJob;
 import gov.nih.nci.caintegrator.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.GenePatternAnalysisJob;
 import gov.nih.nci.caintegrator.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator.domain.application.GisticAnalysisJob;
 import gov.nih.nci.caintegrator.domain.application.PrincipalComponentAnalysisJob;
 import gov.nih.nci.caintegrator.domain.application.Query;
 import gov.nih.nci.caintegrator.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator.domain.translational.Study;
 import gov.nih.nci.caintegrator.security.SecurityHelper;
 import gov.nih.nci.caintegrator.web.DisplayableUserWorkspace;
 import gov.nih.nci.caintegrator.web.SessionHelper;
 import gov.nih.nci.caintegrator.web.action.analysis.ComparativeMarkerSelectionAnalysisForm;
 import gov.nih.nci.caintegrator.web.action.analysis.GenePatternAnalysisForm;
 import gov.nih.nci.caintegrator.web.action.analysis.GisticAnalysisForm;
 import gov.nih.nci.caintegrator.web.action.analysis.KMPlotForm;
 import gov.nih.nci.caintegrator.web.action.analysis.PrincipalComponentAnalysisForm;
 import gov.nih.nci.caintegrator.web.action.analysis.geneexpression.GEPlotForm;
 import gov.nih.nci.caintegrator.web.action.platform.form.PlatformForm;
 import gov.nih.nci.caintegrator.web.action.query.DisplayableCopyNumberQueryResult;
 import gov.nih.nci.caintegrator.web.action.query.DisplayableQueryResult;
 import gov.nih.nci.caintegrator.web.action.query.form.QueryForm;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.Preparable;
 
 /**
  * Base class for all Struts 2 <code>Actions</code> in the application, provides context set up
  * for the current request.
  */
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
     @Override
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
     @Autowired
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
              String holdString = inputString.replaceAll("(<[^>]+>)", StringUtils.EMPTY);
              holdString = holdString.replaceAll("(<.+)", StringUtils.EMPTY);
             holdString = holdString.replaceAll("[^a-zA-Z0-9 ]", StringUtils.EMPTY);
              holdString = holdString.replaceAll("[,']", StringUtils.EMPTY);
              holdString = holdString.replaceAll("[,\"]", StringUtils.EMPTY);
              holdString = holdString.replaceAll("[,>]", StringUtils.EMPTY);
              holdString = holdString.replaceAll("[,<]", StringUtils.EMPTY);
              return holdString.replace("<", StringUtils.EMPTY);
         }
         return StringUtils.EMPTY;
     }
 
     /**
      * @return creates a new struts token and returns it.
      */
     public String getNewToken() {
         org.apache.struts2.util.TokenHelper.setToken();
         return SessionHelper.getInstance().getToken();
     }
 
     /**
      * @return the struts token name
      */
     public String getTokenName() {
         return org.apache.struts2.util.TokenHelper.DEFAULT_TOKEN_NAME;
 
     }
 }
