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
 
 import edu.mit.broad.genepattern.gp.services.GenePatternServiceException;
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisMethod;
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisMethodInvocation;
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisService;
 import gov.nih.nci.caintegrator2.application.query.QueryManagementService;
 import gov.nih.nci.caintegrator2.application.query.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.web.action.AbstractCaIntegrator2Action;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Action to configure and run GenePattern analysis jobs.
  */
 public class GenePatternAnalysisAction extends AbstractCaIntegrator2Action {
     
     private static final long serialVersionUID = 1L;
 
     /**
      * Indicates action should open the analysis page.
      */
     public static final String OPEN_ACTION = "open";
 
     /**
      * Indicates action should connect to GenePattern.
      */
     public static final String CONNECT_ACTION = "connect";
     
     /**
      * Indicates user has changed the analysis method.
      */
     public static final String CHANGE_METHOD_ACTION = "change";
 
     /**
      * Indicates action should execute the analysis.
      */
     public static final String EXECUTE_ACTION = "execute";
     
     private static final String RESULTS = "results";
     
     private AnalysisService analysisService;
     private QueryManagementService queryManagementService;
     private String selectedAction = OPEN_ACTION;
     private String resultUrl;
     private String analysisMethodName;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String execute() {
         if (OPEN_ACTION.equals(getSelectedAction())) {
             return open();
         } else if (CONNECT_ACTION.equals(getSelectedAction())) {
             return connect();
         } else if (CHANGE_METHOD_ACTION.equals(getSelectedAction())) {
             return changeMethod();
         } else if (EXECUTE_ACTION.equals(getSelectedAction())) {
             return executeAnalysis();
         } else  {
             throw new IllegalStateException("Invalid action: " + getSelectedAction());
         }
     }
     
     /**
      * @return
      */
     private String open() {
         getAnalysisForm().setGenomicQueries(getGenomicQueries());
         Collection<AnnotationDefinition> subjectClassifications = 
             getClassificationAnnotations(getStudy().getSubjectAnnotationCollection());
         Collection<AnnotationDefinition> imageSeriesClassifications = 
             getClassificationAnnotations(getStudy().getImageSeriesAnnotationCollection());
         Collection<AnnotationDefinition> sampleClassifications = 
             getClassificationAnnotations(getStudy().getSampleAnnotationCollection());
         getAnalysisForm().clearClassificationAnnotations();
         getAnalysisForm().addClassificationAnnotations(subjectClassifications, EntityTypeEnum.SUBJECT);
         getAnalysisForm().addClassificationAnnotations(imageSeriesClassifications, EntityTypeEnum.IMAGESERIES);
         getAnalysisForm().addClassificationAnnotations(sampleClassifications, EntityTypeEnum.SAMPLE);
         setAnalysisMethodName(getAnalysisForm().getAnalysisMethodName());
         return SUCCESS;
     }
 
     private Collection<AnnotationDefinition> getClassificationAnnotations(
             Collection<AnnotationDefinition> annotationCollection) {
         Set<AnnotationDefinition> classificationAnnotations = new HashSet<AnnotationDefinition>();
         for (AnnotationDefinition annotation : annotationCollection) {
             if (annotation.getAnnotationValueCollection() != null 
                     && !annotation.getAnnotationValueCollection().isEmpty()) {
                 classificationAnnotations.add(annotation);
             }
         }
         return classificationAnnotations;
     }
 
     /**
      * @return
      */
     private List<Query> getGenomicQueries() {
         List<Query> queries = new ArrayList<Query>();
         for (Query query : getStudySubscription().getQueryCollection()) {
             if (ResultTypeEnum.GENOMIC.equals(ResultTypeEnum.getByValue(query.getResultType()))) {
                 queries.add(query);
             }
         }
         return queries;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         super.validate();
        if (OPEN_ACTION.equals(getSelectedAction())) {
            validateHasStudy();
        } else if (CONNECT_ACTION.equals(getSelectedAction())) {
             validateConnect();
         } else if (EXECUTE_ACTION.equals(getSelectedAction())) {
             validateExecuteAnalysis();
         }
     }
    
    private void validateHasStudy() {
        if (getStudySubscription() == null) {
            addActionError("Please select a study first under \"My Studies\".");
            return;
        }
    }
    
     private void validateConnect() {
         if (StringUtils.isEmpty(getAnalysisForm().getUrl())) {
             addFieldError("analysisForm.url", "URL is required");
         } else {
             validateUrl();
         }
         if (StringUtils.isEmpty(getAnalysisForm().getUsername())) {
             addFieldError("analysisForm.username", "Username is required");
         }
     }
 
     private void validateUrl() {
         try {
             new URL(getAnalysisForm().getUrl());
         } catch (MalformedURLException e) {
             addFieldError("analysisForm.url", "Invalid URL format");
         }
     }
 
     private void validateExecuteAnalysis() {
         getAnalysisForm().validate(this);
     }
 
     /**
      * @return
      */
     private String changeMethod() {
         if (!getAnalysisMethodName().equals(getAnalysisForm().getAnalysisMethodName())) {
             getAnalysisForm().setAnalysisMethodName(getAnalysisMethodName());
         }
         return SUCCESS;
     }
 
     private String connect() {
         try {
             if (!StringUtils.isBlank(getAnalysisForm().getServer().getUrl())) {
                 getAnalysisForm().setAnalysisMethods(
                         getAnalysisService().getGenePatternMethods(getAnalysisForm().getServer()));
                 setAnalysisMethodName(getAnalysisForm().getAnalysisMethodName());
             }
             return SUCCESS;
         } catch (GenePatternServiceException e) { 
             getAnalysisForm().setAnalysisMethods(new ArrayList<AnalysisMethod>());
             addActionError("Couldn't retrieve GenePattern analysis method information: " + e.getMessage());
             return ERROR;
         }
     }
     
     private String executeAnalysis() {
         try {
             AnalysisMethodInvocation invocation = getAnalysisForm().getInvocation();
             configureInvocationParameters();
             setResultUrl(getAnalysisService().executeGenePatternJob(
                     getAnalysisForm().getServer(), invocation).toExternalForm());
             return RESULTS;
         } catch (GenePatternServiceException e) {
             addActionError("Couldn't execute GenePattern analysis job: " + e.getMessage());
             return ERROR;
         }
     }
 
     private void configureInvocationParameters() {
         for (AbstractAnalysisFormParameter formParameter : getAnalysisForm().getParameters()) {
             formParameter.configureForInvocation(getStudySubscription(), getQueryManagementService());
         }
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
      * @return the resultUrl
      */
     public String getResultUrl() {
         return resultUrl;
     }
 
     /**
      * @param resultUrl the resultUrl to set
      */
     private void setResultUrl(String resultUrl) {
         this.resultUrl = resultUrl;
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
      * @return the analysisMethodName
      */
     public String getAnalysisMethodName() {
         return analysisMethodName;
     }
 
     /**
      * @param analysisMethodName the analysisMethodName to set
      */
     public void setAnalysisMethodName(String analysisMethodName) {
         this.analysisMethodName = analysisMethodName;
     }
 
 }
