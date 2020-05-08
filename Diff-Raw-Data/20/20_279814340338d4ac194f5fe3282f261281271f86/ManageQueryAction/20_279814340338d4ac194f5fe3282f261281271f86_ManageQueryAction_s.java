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
 import gov.nih.nci.caintegrator2.application.study.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.QueryResult;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.web.action.AbstractCaIntegrator2Action;
 
 import java.util.Collection;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Handles the form in which the user constructs, edits and runs a query.
  */
 @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength" }) // see execute method
 
 public class ManageQueryAction extends AbstractCaIntegrator2Action {
 
     private static final long serialVersionUID = 1L;
     private QueryManagementService queryManagementService;
     private ManageQueryHelper manageQueryHelper;
     private String selectedAction = "";
     private String rowNumber = "";
     private String selectedRowCriterion = "uninitializedselectedRowCriterion";
     //Struts should automatically populate these arrays from the form element.
     private String[] selectedAnnotations;  //selected annotations for all criterion as a list.
     private String[] selectedOperators; //selected operators for all criterion as a list.
     private String[] selectedValues; //selected values for all criterion as a list.
     private String searchName;
     private String searchDescription;
     private Long[] selectedClinicalAnnotations; // selected clinical annotations from columns tab.
     private Long[] selectedImageAnnotations;    // selected image annotations from columns tab.
     private String displayTab;
     
     /**
      * The 'prepare' interceptor will look for this method enabling 
      * preprocessing.
      */
     public void prepare() {
         super.prepare();
         if ("selectedTabSearchResults".equals(selectedAction)) {
             displayTab = "searchResults";
             return;
         }
         displayTab = "criteria";
         // Instantiate/prepopulate manageQueryHelper if necessary
         manageQueryHelper = ManageQueryHelper.getInstance();
         if ("createNewQuery".equals(selectedAction)) {
             manageQueryHelper = ManageQueryHelper.resetSessionInstance();
         }
 
         if (getStudySubscription() != null) {
             if (!manageQueryHelper.isPrepopulated()) {
                 manageQueryHelper.prepopulateAnnotationSelectLists(getStudy());
             }
             if (this.selectedAnnotations == null || this.selectedAnnotations.length == 0) {
                 checkClinicalAnnotationDefinitions();     
                 checkImageAnnotationDefinitions();
             }
         }
     }
     /**
      * Setting all the Clinical definitions checked in JSP as a default action.
      */
     public void checkClinicalAnnotationDefinitions() {
         Collection<AnnotationDefinition> annotationDefinitions = manageQueryHelper.getClinicalAnnotationDefinitions();
         if (annotationDefinitions != null) {
             this.selectedClinicalAnnotations = new Long[annotationDefinitions.size()];
             int i = 0;
             for (AnnotationDefinition annotationDefinition : annotationDefinitions) {
                 this.selectedClinicalAnnotations[i] = annotationDefinition.getId();
                 i++;
             }
         }
     }
     
     /**
      * Setting all the Image definitions checked in JSP as a default action.
      */
     public void checkImageAnnotationDefinitions() {
         Collection<AnnotationDefinition> annotationDefinitions = manageQueryHelper.getImageAnnotationDefinitions();
         if (annotationDefinitions != null) {
             this.selectedImageAnnotations = new Long[annotationDefinitions.size()];
             int i = 0;
             for (AnnotationDefinition annotationDefinition : annotationDefinitions) {
                 this.selectedImageAnnotations[i] = annotationDefinition.getId();
                 i++;
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
 
         if ("selectedTabSearchResults".equals(selectedAction)) {
             return;
         }
 
         if (getStudySubscription() == null) {
             addActionError("Please select a study under \"My Studies\".");
             return;
         }
         
         saveFormData();
         
         if ("executeQuery".equals(selectedAction)) {
             validateExecuteQuery(); 
         }
             
         if ("saveQuery".equals(selectedAction)) {
             validateSaveQuery();
         }
         
         if ("addCriterionRow".equals(selectedAction)) {
             validateAddCriterionRow();
         } 
     }
     
     private void validateAddCriterionRow() {
         if (selectedRowCriterion.equalsIgnoreCase("Select Criteria Type")) {
             addActionError(" Please select a Criteria type");
         }
     }
     
     private void validateExecuteQuery() {
         validateHasCriterion();
         validateHasUserSelectedValues();
     }
     
     private void validateSaveQuery() {
         validateHasCriterion();
         validateHasUserSelectedValues();
         // Need to see if there's non-space characters in the saved name.
         if (StringUtils.isEmpty(searchName)) {
             addActionError("Must have a name for your query");
         } else {
             for (Query query : getStudySubscription().getQueryCollection()) {
                 if (searchName.equalsIgnoreCase(query.getName())) {
                     addActionError("There is already a query named '" + searchName
                             + "', either delete that one, or use a different name.");
                     break;
                 }
             }
         }
     }
     
     private void validateHasCriterion() {
         if (manageQueryHelper.getQueryCriteriaRowList().isEmpty()) {
             addActionError("No Criterion Added, must have Criteria to Search or Save a Query.");
         }
     }
    
     private void validateHasUserSelectedValues() {
         for (String selectedAnnotation : selectedAnnotations) {
             if (selectedAnnotation.equalsIgnoreCase("1")) {
                 addActionError(" Please select an Annotation ");
             }
         }
         for (String selectedOperator : selectedOperators) {
             if (selectedOperator.equalsIgnoreCase("1")) {
                 addActionError(" Please select an Operator ");
             }
         }
         for (String selectedValue : selectedValues) {
             if (selectedValue.equalsIgnoreCase("")) {
                 addActionError(" Please select or type a value to run the query ");
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength" }) // Checking action type.
     public String execute()  {
 
         if ("selectedTabSearchResults".equals(selectedAction)) {
             return SUCCESS;
         }
         
         // declarations
         String returnValue = ERROR;
         
         // Check which user action is submitted
         if ("remove".equals(selectedAction)) {
             setQueryResult(null);
             returnValue = removeRow();
         } else if ("addCriterionRow".equals(selectedAction)) {
             setQueryResult(null);
             returnValue = addCriterionRow();
         } else if ("executeQuery".equals(selectedAction)) {
             returnValue = executeQuery();
         } else if ("saveQuery".equals(selectedAction)) {
             returnValue = saveQuery();
         } else if ("editQuery".equals(selectedAction)) {
             // call editQuery
             returnValue = SUCCESS;
         } else if ("createNewQuery".equals(selectedAction)) {
             // call new query
             setQueryResult(null);
             returnValue = SUCCESS;    
         } else if ("updateResultsPerPage".equals(selectedAction)) {
          // Does nothing right now, eventually might actually persist this value to db.
             displayTab = "searchResults";
             returnValue = SUCCESS;
         } else if ("updateAnnotationDefinition".equals(selectedAction)) {
             setQueryResult(null);
             returnValue = updateAnnotationDefinition();
         } else {
             addActionError("Unknown action '" + selectedAction + "'");
             returnValue = ERROR; 
         } 
         return returnValue;
     }
     
     /**
      * Add a a criterion row to the query, and validates that row has data for this study.
      * 
      * @return the Struts result.
      */
     public String addCriterionRow() {
         
         if (EntityTypeEnum.SUBJECT.getValue().equals(this.selectedRowCriterion) 
             && !manageQueryHelper.configureClinicalQueryCriterionRow()) {
                 addActionError("There are no clinical annotations defined for this study.");
         }
         if (EntityTypeEnum.IMAGESERIES.getValue().equals(this.selectedRowCriterion)
             && !manageQueryHelper.configureImageSeriesQueryCriterionRow()) {
                 addActionError("There are no image series annotations defined for this study.");
         }
         
         if (EntityTypeEnum.GENEEXPRESSION.getValue().equals(this.selectedRowCriterion)) {
             if (!manageQueryHelper.configureGeneExpressionCriterionRow()) {
                 addActionError("There are no gene expression annotations defined for this study.");
             } else {
                 // Turn it into a genomic query type, by default, if they add a genomic row.
                 manageQueryHelper.setResultType(ResultTypeEnum.GENOMIC.getValue());
             }
         }
         return SUCCESS;
     }
     
     private String removeRow() {
         try {
             if (!manageQueryHelper.removeQueryAnnotationCriteria(Integer.valueOf(rowNumber))) {
                 addActionError("Invalid row to remove");
             }
         } catch (NumberFormatException e) {
             addActionError("rowNumber is not a valid number.");
         }
         return SUCCESS;
     }
     
     private String updateAnnotationDefinition() {
         try {
              if (this.selectedAnnotations != null) {
                   manageQueryHelper.updateAnnotationDefinition(selectedAnnotations, Integer.valueOf(rowNumber));
                 }
         } catch (NumberFormatException e) {
             addActionError("rowNumber is not a valid number.");  
         }
         return SUCCESS;
     }
 
     private void saveFormData() {
         manageQueryHelper.updateSelectedValues(getSelectedAnnotations());
         manageQueryHelper.updateSelectedOperatorValues(getSelectedOperators());
         manageQueryHelper.updateSelectedUserValues(getSelectedValues());
         manageQueryHelper.setSaveClinicalAnnotations(getSelectedClinicalAnnotations());
         manageQueryHelper.setSaveImageAnnotations(getSelectedImageAnnotations());
         manageQueryHelper.setClinicalResultColumnCollection();
         manageQueryHelper.setImageResultColumnCollection();
         manageQueryHelper.getColumnIndexOptions().clear();
         manageQueryHelper.indexOption();
     }
     
     /**
      * Execute the current query.
      * 
      * @return the Struts result.
      */
     public String executeQuery() {
         
         if (ResultTypeEnum.GENOMIC.getValue().equals(manageQueryHelper.getResultType())) {
             GenomicDataQueryResult genomicResult =  
                 manageQueryHelper.executeGenomicQuery(queryManagementService);
             setGenomicDataQueryResult(genomicResult);
         } else {
             QueryResult result = manageQueryHelper.executeQuery(queryManagementService);
             for (ResultRow resultRow : result.getRowCollection()) { // Load all images
                 if (resultRow.getImageSeries() != null) {
                     resultRow.getImageSeries().getImageCollection().isEmpty();
                 }
             }
             setQueryResult(new DisplayableQueryResult(result));
         }
         displayTab = "searchResults";
         return SUCCESS;
     }
     
     /**
      * Save the current query.
      * 
      * @return the Struts result.
      */
     public String saveQuery() {
         manageQueryHelper.saveQuery(queryManagementService, searchName, searchDescription);
         getWorkspaceService().saveUserWorkspace(getWorkspace());
         return SUCCESS;
     }
 
     /**
      * @return the manageQueryHelper
      */
     public ManageQueryHelper getManageQueryHelper() {
         return manageQueryHelper;
     }
     
     /**
      * @param manageQueryHelper the manageQueryHelper to set
      */
     public void setManageQueryHelper(ManageQueryHelper manageQueryHelper) {
         this.manageQueryHelper = manageQueryHelper;
     }
 
     /**
      * @return the selectedRowCriterion
      */
     public String getSelectedRowCriterion() {
         return selectedRowCriterion;
     }
 
     /**
      * @param selectedRowCriterion the selectedRowCriterion to set
      */
     public void setSelectedRowCriterion(String selectedRowCriterion) {
         this.selectedRowCriterion = selectedRowCriterion;
     }
 
     /**
      * @return the selectedAnnotations
      */
     public String[] getSelectedAnnotations() {
         return (this.selectedAnnotations == null)
             ? null : this.selectedAnnotations.clone();
     }
 
     /**
      * @param selectedAnnotations the selectedAnnotations to set
      */
     public void setSelectedAnnotations(String[] selectedAnnotations) {
         this.selectedAnnotations = (selectedAnnotations == null)
             ? null : selectedAnnotations.clone();
     }
 
     /**
      * @return the selectedOperators
      */
     public String[] getSelectedOperators() {
         String[] cloneArray = (selectedOperators == null)
             ? null : selectedOperators.clone();
         return cloneArray;
     }
 
     /**
      * @param selectedOperators the selectedOperators to set
      */
     public void setSelectedOperators(String[] selectedOperators) {
         this.selectedOperators = (selectedOperators == null)
             ? null : selectedOperators.clone();
     }
 
     /**
      * @return the selectedValues
      */
     public String[] getSelectedValues() {
         String[] cloneArray = (selectedValues == null)
             ? null : selectedValues.clone();
         return cloneArray;
     }
 
     /**
      * @param selectedValues the selectedValues to set
      */
     public void setSelectedValues(String[] selectedValues) {
         this.selectedValues = (selectedValues == null)
             ? null : selectedValues.clone();
     }
     
     /**
      * @return the selectedClinicalAnnotations
      */
     public Long[] getSelectedClinicalAnnotations() {
         Long[] cloneArray = (selectedClinicalAnnotations == null)
             ? null : selectedClinicalAnnotations.clone();
         return cloneArray;
     }
 
     /**
      * @param selectedClinicalAnnotations the selectedClinicalAnnotations to set
      */
     public void setSelectedClinicalAnnotations(Long[] selectedClinicalAnnotations) {
         this.selectedClinicalAnnotations = (selectedClinicalAnnotations == null)
             ? null : selectedClinicalAnnotations.clone();
     }
 
     /**
      * @return the selectedImageAnnotations
      */
     public Long[] getSelectedImageAnnotations() {
         Long[] cloneArray = (selectedImageAnnotations == null)
             ? null : selectedImageAnnotations.clone();
         return cloneArray;
     }
 
     /**
      * @param selectedImageAnnotations the selectedImageAnnotations to set
      */
     public void setSelectedImageAnnotations(Long[] selectedImageAnnotations) {
         this.selectedImageAnnotations = (selectedImageAnnotations == null)
             ? null : selectedImageAnnotations.clone();
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
      * @param selectedAction the action as selected in the input form
      */
     public void setSelectedAction(String selectedAction) {
         this.selectedAction = selectedAction;
     }
 
     /**
      * @return the searchName
      */
     public String getSearchName() {
         return searchName;
     }
 
     /**
      * @param searchName the searchName to set
      */
     public void setSearchName(String searchName) {
         this.searchName = searchName;
     }
 
     /**
      * @return the searchDescription
      */
     public String getSearchDescription() {
         return searchDescription;
     }
 
     /**
      * @param searchDescription the searchDescription to set
      */
     public void setSearchDescription(String searchDescription) {
         this.searchDescription = searchDescription;
     }
 
     /**
      * @return the clinicalDefinitionsSize
      */
     public int getClinicalDefinitionsSize() {
         return manageQueryHelper.getClinicalAnnotationDefinitions().size();
     }
     
     /**
      * @return the imageDefinitionsSize
      */
     public int getImageDefinitionsSize() {
         return manageQueryHelper.getImageAnnotationDefinitions().size();
     }
     
     /**
      * @return the rowNumber
      */
     public String getRowNumber() {
         return rowNumber;
     }
     
     /**
      * @param rowNumber the rowNumber to set
      */
     public void setRowNumber(String rowNumber) {
         this.rowNumber = rowNumber;
     }
         
     /**
      * @return the displayTab
      */
     public String getDisplayTab() {
         return displayTab;
     }
    
    /**
     * @return page size
     */
    public String getPageSize() {
        if (getQueryResult() != null) {
            return Integer.toString(getQueryResult().getPageSize());
        }
        return null;
    }
    
    /**
     * Set the page size.
     * @param pageSize the page size
     */
    public void setPageSize(String pageSize) {
        if (getQueryResult() != null) {
            getQueryResult().setPageSize(Integer.parseInt(pageSize));
        }
    }
 }
