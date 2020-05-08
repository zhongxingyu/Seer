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
 package gov.nih.nci.caintegrator2.web.action.query;
 
 import gov.nih.nci.caintegrator2.application.query.QueryManagementService;
 import gov.nih.nci.caintegrator2.application.query.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator2.application.study.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.QueryResult;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.web.SessionHelper;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import com.opensymphony.xwork2.ActionContext;
 
 /**
  * Helper class for ManageQueryAction.
  */
 final class ManageQueryHelper {
     /**
      * String to use to store and retrieve this object from session.
      */
     static final String MANAGE_QUERY_HELPER_STRING = "manageQueryHelper";
     
     private boolean advancedView = false;
     private List<QueryAnnotationCriteria> queryCriteriaRowList;
     private Collection<AnnotationDefinition> clinicalAnnotationDefinitions;
     private Collection<AnnotationDefinition> sampleAnnotationDefinitions;
     private Collection<AnnotationDefinition> imageAnnotationDefinitions;
     private AnnotationSelection clinicalAnnotationSelections;
     private AnnotationSelection imageSeriesAnnotationSelections;
     private boolean prepopulated;
     
     /**
      * Default constructor.
      */
     private ManageQueryHelper() {
         prepopulated = false;
     }
     
     /**
      * Singleton method to get the instance off of the sessionMap object or create a new one. 
      * @return instance of ManageQueryHelper.
      */
     static ManageQueryHelper getInstance() {
         return retrieveProperInstance(false);
     }
     
     /**
      * Resets the session's ManageQueryHelper object to new.
      * @return new instance of ManageQueryHelper.
      */
     static ManageQueryHelper resetSessionInstance() {
         return retrieveProperInstance(true);
     }
     
     @SuppressWarnings("unchecked") // sessionMap is not parameterized in struts2.
     private static ManageQueryHelper retrieveProperInstance(boolean reset) {
         if (ActionContext.getContext() != null 
                 && ActionContext.getContext().getSession() != null) {
             ManageQueryHelper instance =
                 (ManageQueryHelper) ActionContext.getContext().getSession().get(MANAGE_QUERY_HELPER_STRING);
             if (instance == null || reset) {
                 instance = new ManageQueryHelper();
                 ActionContext.getContext().getSession().put(MANAGE_QUERY_HELPER_STRING, instance);
             }
             return instance;
         } else {
             throw new IllegalStateException("Not inside of an ActionContext!");
         }
     }
     
     /**
      * @return the advancedView
      */
     public boolean isAdvancedView() {
         return advancedView;
     }
 
     /**
      * @param advancedView the advancedView to set
      */
     public void setAdvancedView(boolean advancedView) {
         this.advancedView = advancedView;
     }
     
     /**
      * @return the queryCriteriaRowList
      */
     public List<QueryAnnotationCriteria> getQueryCriteriaRowList() {
         if (this.queryCriteriaRowList == null) {
             this.queryCriteriaRowList = new ArrayList<QueryAnnotationCriteria>(); 
         }
         
         return this.queryCriteriaRowList;
     }
 
     /**
      * @param queryCriteriaRowList the queryCriteriaRowList to set
      */
     public void setQueryCriteriaRowList(List<QueryAnnotationCriteria> queryCriteriaRowList) {
         this.queryCriteriaRowList = queryCriteriaRowList;
     }
     
     /**
      * @param queryAnnotationCriteria the QueryAnnotationCriteria to add
      */
     void addQueryAnnotationCriteriaToList(QueryAnnotationCriteria queryAnnotationCriteria) {
         this.getQueryCriteriaRowList().add(queryAnnotationCriteria);
     }
       
     /**
      * @return the clinicalAnnotationDefinitions
      */
     public Collection<AnnotationDefinition> getClinicalAnnotationDefinitions() {
         return clinicalAnnotationDefinitions;
     }
 
     /**
      * @param clinicalAnnotationDefinitions the clinicalAnnotationDefinitions to set
      */
     public void setClinicalAnnotationDefinitions(Collection<AnnotationDefinition> clinicalAnnotationDefinitions) {
         this.clinicalAnnotationDefinitions = clinicalAnnotationDefinitions;
     }
 
     /**
      * @return the sampleAnnotationDefinitions
      */
     public Collection<AnnotationDefinition> getSampleAnnotationDefinitions() {
         return sampleAnnotationDefinitions;
     }
 
     /**
      * @param sampleAnnotationDefinitions the sampleAnnotationDefinitions to set
      */
     public void setSampleAnnotationDefinitions(Collection<AnnotationDefinition> sampleAnnotationDefinitions) {
         this.sampleAnnotationDefinitions = sampleAnnotationDefinitions;
     }
 
     /**
      * @return the imageAnnotationDefinitions
      */
     public Collection<AnnotationDefinition> getImageAnnotationDefinitions() {
         return imageAnnotationDefinitions;
     }
 
     /**
      * @param imageAnnotationDefinitions the imageAnnotationDefinitions to set
      */
     public void setImageAnnotationDefinitions(Collection<AnnotationDefinition> imageAnnotationDefinitions) {
         this.imageAnnotationDefinitions = imageAnnotationDefinitions;
     }
 
     /**
      * @return the clinicalAnnotationSelections
      */
     public AnnotationSelection getClinicalAnnotationSelections() {
         return clinicalAnnotationSelections;
     }
 
     /**
      * @param clinicalAnnotationSelections the clinicalAnnotationSelections to set
      */
     public void setClinicalAnnotationSelections(AnnotationSelection clinicalAnnotationSelections) {
         this.clinicalAnnotationSelections = clinicalAnnotationSelections;
     }
     
     /**
      * Fetch and store the annotation select lists.
      */
     void prepopulateAnnotationSelectLists() {
         StudySubscription studySubscription = 
             SessionHelper.getInstance().getDisplayableUserWorkspace().getCurrentStudySubscription();
         if (studySubscription == null) {
            throw new IllegalStateException("No current StudySubscription is selected on the Session."); 
         }
         Study study = studySubscription.getStudy();
         populateClinicalAnnotationDefinitions(study);
         populateImageSeriesAnnotationDefinitions(study);
         populateSampleAnnotationDefinitions(study);
         populateAnnotationSelections();
         setPrepopulated(true);
     }
     
     private void populateAnnotationSelections() {
         populateClinicalAnnotationSelections();
         populateImageSeriesAnnotationSelections();
 
     }
     
     private void populateClinicalAnnotationDefinitions(Study study) {
         Collection<AnnotationDefinition> annoDefs = study.getSubjectAnnotationCollection();
 
         if (annoDefs != null && !annoDefs.isEmpty()) {
             clinicalAnnotationDefinitions = annoDefs;
         }
     }
     
     private void populateImageSeriesAnnotationDefinitions(Study study) {
         Collection<AnnotationDefinition> annoDefs = study.getImageSeriesAnnotationCollection();
 
         if (annoDefs != null && !annoDefs.isEmpty()) {
             imageAnnotationDefinitions = annoDefs;
         }
     }
     
     private void populateSampleAnnotationDefinitions(Study study) {
         Collection<AnnotationDefinition> annoDefs = study.getSampleAnnotationCollection();
 
         if (annoDefs != null && !annoDefs.isEmpty()) {
             sampleAnnotationDefinitions = annoDefs;
         }
     }
     
     private void populateClinicalAnnotationSelections() {
         if (this.clinicalAnnotationSelections == null) {
             this.clinicalAnnotationSelections = new AnnotationSelection();
         }
         if (clinicalAnnotationDefinitions != null && !clinicalAnnotationDefinitions.isEmpty()) {
             List<String> annotationSelections = getDefinitionDisplayNames(clinicalAnnotationDefinitions);
             clinicalAnnotationSelections.setAnnotationSelections(annotationSelections);
             clinicalAnnotationSelections.setAnnotationDefinitions(clinicalAnnotationDefinitions);
         }
     }
     
     private void populateImageSeriesAnnotationSelections() {
         if (this.imageSeriesAnnotationSelections == null) {
             this.imageSeriesAnnotationSelections = new AnnotationSelection();
         }
         if (imageAnnotationDefinitions != null && !imageAnnotationDefinitions.isEmpty()) {
             List<String> annotationSelections = getDefinitionDisplayNames(imageAnnotationDefinitions);
             imageSeriesAnnotationSelections.setAnnotationSelections(annotationSelections);
             imageSeriesAnnotationSelections.setAnnotationDefinitions(imageAnnotationDefinitions);
         }
     }
     
     private List<String> getDefinitionDisplayNames(Collection<AnnotationDefinition> annotationDefinitions) {
         List<String> annotationSelections = new ArrayList<String>();
         for (AnnotationDefinition annotationDefinition : annotationDefinitions) {
             annotationSelections.add(annotationDefinition.getDisplayName());
         }
         return annotationSelections;
     }
     /**
      * Configures clinical row.
      * @return true/false depending on if there's anything to add (any clinical data for study).
      */
     boolean configureClinicalQueryCriterionRow() {
         if (getClinicalAnnotationSelections() == null 
              || getClinicalAnnotationSelections().getAnnotationDefinitions().isEmpty()) {
             return false;
         }
         QueryAnnotationCriteria queryAnnotationCriteria = new QueryAnnotationCriteria();
         queryAnnotationCriteria.setAnnotationSelections(this.getClinicalAnnotationSelections());
         queryAnnotationCriteria.setAnnotationValue("");
         queryAnnotationCriteria.setRowType(EntityTypeEnum.SUBJECT); //TODO make clinical a constant
         queryAnnotationCriteria.setRowLabel("Clinical");
         this.addQueryAnnotationCriteriaToList(queryAnnotationCriteria);
         return true;
     }
     
     /**
      * Configures image series row.
      * @return true/false depending on if there's anything to add (any image series data for study).
      */
     boolean configureImageSeriesQueryCriterionRow() {
         if (getImageSeriesAnnotationSelections() == null 
                 || getImageSeriesAnnotationSelections().getAnnotationDefinitions().isEmpty()) {
                return false;
            }
         QueryAnnotationCriteria queryAnnotationCriteria = new QueryAnnotationCriteria();
         //queryAnnotationCriteria.setAnnotationSelections(this.getCurrentAnnotationSelections());
         queryAnnotationCriteria.setAnnotationSelections(this.getImageSeriesAnnotationSelections());
         queryAnnotationCriteria.setAnnotationValue("");
         queryAnnotationCriteria.setRowType(EntityTypeEnum.IMAGESERIES); //TODO make clinical a constant
         queryAnnotationCriteria.setRowLabel("Image");
         this.addQueryAnnotationCriteriaToList(queryAnnotationCriteria);
         return true;
     }
     
     /**
      * @param selectedValues The array of values to be added.
      */
     void updateSelectedValues(String[] selectedValues) {
 
         QueryAnnotationCriteria tempAnnotationCriteria = null;
         Iterator<QueryAnnotationCriteria> rowIter = this.getQueryCriteriaRowList().iterator();
         int i = 0;
         while (rowIter.hasNext()) {
             tempAnnotationCriteria = rowIter.next();
             if (selectedValues[i] != null) {
                 tempAnnotationCriteria.setAnnotationSelection(selectedValues[i]);
                 this.queryCriteriaRowList.set(i, tempAnnotationCriteria);
             }
             i++;
         }
     }
     
     /**
      * @param selectedValues The array of values to be added.
      */
     void updateSelectedOperatorValues(String[] selectedValues) {
 
         QueryAnnotationCriteria tempAnnotationCriteria = null;
         Iterator<QueryAnnotationCriteria> rowIter = this.getQueryCriteriaRowList().iterator();
         int i = 0;
         while (rowIter.hasNext()) {
             tempAnnotationCriteria = rowIter.next();
             if (selectedValues[i] != null) {
                 tempAnnotationCriteria.setAnnotationOperatorSelection(selectedValues[i]);
                 this.queryCriteriaRowList.set(i, tempAnnotationCriteria);
             }
             i++;
         }
     }    
 
     /**
      * @param selectedValues The array of values to be added.
      */
     void updateSelectedUserValues(String[] selectedValues) {
 
         QueryAnnotationCriteria tempAnnotationCriteria = null;
         Iterator<QueryAnnotationCriteria> rowIter = this.getQueryCriteriaRowList().iterator();
         int i = 0;
         while (rowIter.hasNext()) {
             tempAnnotationCriteria = rowIter.next();
             if (selectedValues[i] != null) {
                 tempAnnotationCriteria.setAnnotationValue(selectedValues[i]);
                 this.queryCriteriaRowList.set(i, tempAnnotationCriteria);
             }
             i++;
         }
     }    
 
     /**
      * @param queryManagementService A QueryManagementService instance
      * @param basicQueryOperator String AND or OR
      * @return QueryResult Valid results from the query execution, or null 
      */
     QueryResult executeQuery(QueryManagementService queryManagementService, String basicQueryOperator) {
         QueryResult queryResult = null;
 
         queryResult = createQueryHelper(basicQueryOperator).executeQuery(queryManagementService,
                 this.getQueryCriteriaRowList());
         return queryResult;
     }
     
     /**
      * Saves the query based on the queryCriteriaRowList.
      * @param queryManagementService - service object to pass to the query helper.
      * @param basicQueryOperator - String AND or OR.
      * @param queryName The name of the query.
      * @param queryDescription The description of the query.
      * @return - true/false depending on if it gets saved or not.
      */
     boolean saveQuery(QueryManagementService queryManagementService, 
                              String basicQueryOperator,
                              String queryName, 
                              String queryDescription) {
         
         Query query = createQueryHelper(basicQueryOperator).buildQuery(queryManagementService,
                 getQueryCriteriaRowList(), queryName, queryDescription);
         if (query != null) {
             query.setResultType(ResultTypeEnum.CLINICAL.getValue());
             queryManagementService.save(query);
             return true;
         }
         SessionHelper.getInstance().getDisplayableUserWorkspace().setQuery(query);
         return false;
     }
     
     private QueryHelper createQueryHelper(String basicQueryOperator) {
         QueryHelper queryHelper = new QueryHelper();
         queryHelper.setAdvancedView(advancedView);
         if (basicQueryOperator == null || basicQueryOperator.equals("")) {
             queryHelper.setBasicQueryOperator(BooleanOperatorEnum.OR.getValue());
         } else {
             queryHelper.setBasicQueryOperator(basicQueryOperator);
         }
         return queryHelper;
     }
 
     /**
      * @return the prepopulated
      */
     boolean isPrepopulated() {
         return prepopulated;
     }
 
     /**
      * @param prepopulated the prepopulated to set
      */
     void setPrepopulated(boolean prepopulated) {
         this.prepopulated = prepopulated;
     }
 
     /**
      * @return the imageSeriesAnnotationSelections
      */
     public AnnotationSelection getImageSeriesAnnotationSelections() {
         return imageSeriesAnnotationSelections;
     }
 
     /**
      * @param imageSeriesAnnotationSelections the imageSeriesAnnotationSelections to set
      */
     public void setImageSeriesAnnotationSelections(AnnotationSelection imageSeriesAnnotationSelections) {
         this.imageSeriesAnnotationSelections = imageSeriesAnnotationSelections;
     }
 
 
 }
