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
 package gov.nih.nci.caintegrator2.web.action.study.management;
 
 import gov.nih.nci.caintegrator2.application.analysis.InvalidSurvivalValueDefinitionException;
 import gov.nih.nci.caintegrator2.application.study.AnnotationFieldDescriptor;
 import gov.nih.nci.caintegrator2.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.LogEntry;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.PermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.SurvivalLengthUnitsEnum;
 import gov.nih.nci.caintegrator2.domain.annotation.SurvivalValueDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.SurvivalValueTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Action used to modify/create/delete SurvivalValueDefinitions for a Study.
  */
 public class DefineSurvivalDefinitionAction extends AbstractStudyAction {
 
     private static final long serialVersionUID = 1L;
     private SurvivalValueDefinition survivalValueDefinition = new SurvivalValueDefinition();
 
     private Map<String, AnnotationDefinition> dateAnnotationDefinitions =
                                                 new HashMap<String, AnnotationDefinition>();
     private Map<String, AnnotationDefinition> numericAnnotationDefinitions =
                                                 new HashMap<String, AnnotationDefinition>();
     private Map<String, AnnotationDefinition> survivalStatusAnnotationDefinitions =
                                                 new HashMap<String, AnnotationDefinition>();
     private Map<String, SurvivalValueDefinition> survivalValueDefinitions =
                                                 new HashMap<String, SurvivalValueDefinition>();
     private List<String> survivalStatusValues = new ArrayList<String>();
     private DefineSurvivalDefinitionActionForm survivalDefinitionFormValues = new DefineSurvivalDefinitionActionForm();
     private String actionType = "";
     private boolean newDefinition = false;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         clearErrorsAndMessages();
         if ("modify".equals(actionType) && survivalValueDefinition.getId() == null) {
             addActionError(getText("struts.messages.error.select.survival.definition.first"));
         }
         actionType = "";
         validateStudyHasValidAnnotations();
     }
 
     private void validateStudyHasValidAnnotations() {
         if (getStudy().getAllVisibleAnnotationFieldDescriptors(
                 EntityTypeEnum.SUBJECT, AnnotationTypeEnum.DATE).size() < 3
                 && getStudy().getAllVisibleAnnotationFieldDescriptors(EntityTypeEnum.SUBJECT,
                         AnnotationTypeEnum.NUMERIC).isEmpty()) {
             addActionError(getText("struts.messages.error.survival.definition.invalid"));
         }
     }
 
     /**
      * Refreshes the current clinical source configuration.
      */
     @Override
     public void prepare() {
         super.prepare();
         populateSurvivalValueDefinitions();
         populateDateAnnotationDefinitions();
         populateNumericAnnotationDefinitions();
         populateSurvivalStatusAnnotationDefinitions();
         refreshExistingSurvivalDefinition();
         retrieveFormValues();
         refreshObjectInstances();
         updateSurvivalStatus();
         newDefinition = false;
     }
 
     private void refreshExistingSurvivalDefinition() {
         if (!StringUtils.isBlank(survivalDefinitionFormValues.getSurvivalValueDefinitionId())) {
             survivalValueDefinition.setId(Long.valueOf(survivalDefinitionFormValues.getSurvivalValueDefinitionId()));
         }
         if (survivalValueDefinition.getId() != null) {
             survivalValueDefinition = getStudyManagementService().getRefreshedEntity(survivalValueDefinition);
         }
     }
 
     private void populateSurvivalValueDefinitions() {
         if (getStudy() != null
             && getStudy().getSurvivalValueDefinitionCollection() != null
             && survivalValueDefinitions.size()
                 != getStudy().getSurvivalValueDefinitionCollection().size()) {
             survivalValueDefinitions = new HashMap<String, SurvivalValueDefinition>();
             for (SurvivalValueDefinition def
                     : getStudy().getSurvivalValueDefinitionCollection()) {
                 survivalValueDefinitions.put(def.getId().toString(), def);
             }
         }
     }
 
     private void populateDateAnnotationDefinitions() {
         if (dateAnnotationDefinitions.isEmpty()) {
             dateAnnotationDefinitions = new HashMap<String, AnnotationDefinition>();
             for (AnnotationFieldDescriptor descriptor : getStudy().getAllVisibleAnnotationFieldDescriptors(
                     EntityTypeEnum.SUBJECT, AnnotationTypeEnum.DATE)) {
                 dateAnnotationDefinitions
                         .put(descriptor.getDefinition().getId().toString(), descriptor.getDefinition());
             }
         }
     }
 
     private void populateNumericAnnotationDefinitions() {
         if (numericAnnotationDefinitions.isEmpty()) {
             numericAnnotationDefinitions = new HashMap<String, AnnotationDefinition>();
             for (AnnotationFieldDescriptor descriptor : getStudy().getAllVisibleAnnotationFieldDescriptors(
                     EntityTypeEnum.SUBJECT, AnnotationTypeEnum.NUMERIC)) {
                 numericAnnotationDefinitions
                         .put(descriptor.getDefinition().getId().toString(), descriptor.getDefinition());
             }
         }
     }
 
     private void populateSurvivalStatusAnnotationDefinitions() {
         if (survivalStatusAnnotationDefinitions.isEmpty()) {
             survivalStatusAnnotationDefinitions = new HashMap<String, AnnotationDefinition>();
             for (AnnotationFieldDescriptor descriptor : getStudy().getAllVisibleAnnotationFieldDescriptors(
                     EntityTypeEnum.SUBJECT, null)) {
                 if (!descriptor.getDefinition().getPermissibleValueCollection().isEmpty()) {
                     survivalStatusAnnotationDefinitions
                         .put(descriptor.getDefinition().getId().toString(), descriptor.getDefinition());
                 }
             }
         }
     }
 
     private void refreshObjectInstances() {
         if (survivalValueDefinition.getSurvivalStartDate() != null
             && survivalValueDefinition.getSurvivalStartDate().getId() != null) {
             survivalValueDefinition.setSurvivalStartDate(getStudyManagementService().
                     getRefreshedEntity(survivalValueDefinition.getSurvivalStartDate()));
         }
 
         if (survivalValueDefinition.getDeathDate() != null
             && survivalValueDefinition.getDeathDate().getId() != null) {
             survivalValueDefinition.setDeathDate(getStudyManagementService().
                     getRefreshedEntity(survivalValueDefinition.getDeathDate()));
         }
 
         if (survivalValueDefinition.getLastFollowupDate() != null
             && survivalValueDefinition.getLastFollowupDate().getId() != null) {
             survivalValueDefinition.setLastFollowupDate(getStudyManagementService().
                     getRefreshedEntity(survivalValueDefinition.getLastFollowupDate()));
         }
         if (survivalValueDefinition.getSurvivalLength() != null
             && survivalValueDefinition.getSurvivalLength().getId() != null) {
             survivalValueDefinition.setSurvivalLength(getStudyManagementService().
                     getRefreshedEntity(survivalValueDefinition.getSurvivalLength()));
         }
         if (survivalValueDefinition.getSurvivalStatus() != null
             && survivalValueDefinition.getSurvivalStatus().getId() != null) {
             survivalValueDefinition.setSurvivalStatus(getStudyManagementService().
                     getRefreshedEntity(survivalValueDefinition.getSurvivalStatus()));
         }
     }
 
     /**
      * Updates values to show user for survival status.
      * @return struts result.
      */
     public String updateSurvivalStatusValues() {
         checkNullFormValues();
         updateSurvivalStatus();
         if (survivalValueDefinition.getId() == null) {
             newDefinition = true;
         }
         return SUCCESS;
     }
 
     private void checkNullFormValues() {
         if (StringUtils.isBlank(survivalDefinitionFormValues.getSurvivalStatusId())) {
             survivalValueDefinition.setSurvivalStatus(null);
         }
         if (StringUtils.isBlank(survivalDefinitionFormValues.getSurvivalLengthId())) {
             survivalValueDefinition.setSurvivalLength(null);
         }
     }
 
     private void updateSurvivalStatus() {
         survivalStatusValues.clear();
         if (survivalValueDefinition.getSurvivalStatus() != null) {
             for (PermissibleValue pv : survivalValueDefinition.getSurvivalStatus().getPermissibleValueCollection()) {
                 survivalStatusValues.add(pv.getValue());
             }
             Collections.sort(survivalStatusValues);
         }
     }
 
     private void retrieveFormValues() {
         if (!StringUtils.isBlank(survivalDefinitionFormValues.getSurvivalStartDateId())) {
             survivalValueDefinition.setSurvivalStartDate(new AnnotationDefinition());
             survivalValueDefinition.getSurvivalStartDate().setId(
                     Long.valueOf(survivalDefinitionFormValues.getSurvivalStartDateId()));
         }
         if (!StringUtils.isBlank(survivalDefinitionFormValues.getSurvivalDeathDateId())) {
             survivalValueDefinition.setDeathDate(new AnnotationDefinition());
             survivalValueDefinition.getDeathDate().setId(
                     Long.valueOf(survivalDefinitionFormValues.getSurvivalDeathDateId()));
         }
         if (!StringUtils.isBlank(survivalDefinitionFormValues.getLastFollowupDateId())) {
             survivalValueDefinition.setLastFollowupDate(new AnnotationDefinition());
             survivalValueDefinition.getLastFollowupDate().setId(
                     Long.valueOf(survivalDefinitionFormValues.getLastFollowupDateId()));
         }
         if (!StringUtils.isBlank(survivalDefinitionFormValues.getSurvivalLengthId())) {
             survivalValueDefinition.setSurvivalLength(new AnnotationDefinition());
             survivalValueDefinition.getSurvivalLength().setId(
                     Long.valueOf(survivalDefinitionFormValues.getSurvivalLengthId()));
         }
         if (!StringUtils.isBlank(survivalDefinitionFormValues.getSurvivalStatusId())) {
             survivalValueDefinition.setSurvivalStatus(new AnnotationDefinition());
             survivalValueDefinition.getSurvivalStatus().setId(
                     Long.valueOf(survivalDefinitionFormValues.getSurvivalStatusId()));
         }
         if (!StringUtils.isBlank(survivalDefinitionFormValues.getSurvivalLengthUnits())) {
             survivalValueDefinition.setSurvivalLengthUnits(SurvivalLengthUnitsEnum
                     .getByValue(survivalDefinitionFormValues.getSurvivalLengthUnits()));
         }
     }
 
 
     private void clear() {
         survivalDefinitionFormValues.clear();
         this.survivalValueDefinition = new SurvivalValueDefinition();
     }
 
     /**
      * Cancel action.
      * @return struts return string.
      */
     public String cancel() {
         return SUCCESS;
     }
 
     /**
      * Edits the survival value definitions.
      * @return Struts result.
      */
     public String editSurvivalValueDefinitions() {
         clear();
         return SUCCESS;
     }
 
     /**
      * Edits the survival value definition that is chosen.
      * @return the Struts result.
      */
     public String editSurvivalValueDefinition() {
         survivalDefinitionFormValues.load(survivalValueDefinition);
         updateSurvivalStatus();
         return SUCCESS;
     }
 
     /**
      * Creates a new survival value definition.
      *
      * @return the Struts result.
      */
     public String newSurvivalValueDefinition() {
         newDefinition = true;
         survivalValueDefinition = new SurvivalValueDefinition();
         survivalDefinitionFormValues.clear();
         populateSurvivalValueDefinitions();
         return SUCCESS;
     }
 
     /**
      * Deletes a survival value definition.
      *
      * @return the Struts result.
      */
     public String deleteSurvivalValueDefinition() {
         setStudyLastModifiedByCurrentUser(null,
                 LogEntry.getSystemLogDelete(survivalValueDefinition));
         getStudyManagementService().
             removeSurvivalValueDefinition(getStudy(), getSurvivalValueDefinition());
         this.clear();
         populateSurvivalValueDefinitions();
         return SUCCESS;
     }
 
     /**
      * Saves a survival value definition.
      *
      * @return the Struts result.
      */
     public String saveSurvivalValueDefinition() {
         setSurvivalValueDefinitionValues();
 
         try {
             validateSurvivalValueDefinition();
         } catch (InvalidSurvivalValueDefinitionException e) {
             if (survivalValueDefinition.getId() == null) {
                 newDefinition = true;
             }
             addActionError("Unable to save Survival Value Definition: " + e.getMessage());
             return INPUT;
         }
         if (!getStudy().getSurvivalValueDefinitionCollection().contains(survivalValueDefinition)) {
             getStudy().getSurvivalValueDefinitionCollection().add(survivalValueDefinition);
         }
         getStudyManagementService().save(getStudyConfiguration());
         setStudyLastModifiedByCurrentUser(null,
                 LogEntry.getSystemLogSave(survivalValueDefinition));
         survivalDefinitionFormValues.clear();
         return SUCCESS;
     }
 
     /**
      *
      */
     private void setSurvivalValueDefinitionValues() {
         survivalValueDefinition.setName(survivalDefinitionFormValues.getSurvivalValueDefinitionName());
         survivalValueDefinition.setSurvivalValueType(SurvivalValueTypeEnum.getByValue(survivalDefinitionFormValues
                 .getSurvivalValueType()));
         survivalValueDefinition.setSurvivalLengthUnits(SurvivalLengthUnitsEnum.getByValue(
                 survivalDefinitionFormValues.getSurvivalLengthUnits()));
         if (SurvivalValueTypeEnum.DATE.equals(survivalValueDefinition.getSurvivalValueType())) {
             survivalValueDefinition.setSurvivalLength(null);
             survivalValueDefinition.setSurvivalStatus(null);
             survivalValueDefinition.setValueForCensored(null);
         }
         if (SurvivalValueTypeEnum.LENGTH_OF_TIME.equals(survivalValueDefinition.getSurvivalValueType())) {
             survivalValueDefinition.setSurvivalStartDate(null);
             survivalValueDefinition.setDeathDate(null);
             survivalValueDefinition.setLastFollowupDate(null);
             survivalValueDefinition.setValueForCensored(survivalDefinitionFormValues.getValueForCensored());
             checkNullFormValues();
         }
 
     }
 
     /**
      * @throws InvalidSurvivalValueDefinitionException
      */
     private void validateSurvivalValueDefinition() throws InvalidSurvivalValueDefinitionException {
         if (StringUtils.isBlank(survivalValueDefinition.getName())) {
             throw new InvalidSurvivalValueDefinitionException("Must enter a name for Survival Value Definition.");
         }
         for (SurvivalValueDefinition definition : getStudy().getSurvivalValueDefinitionCollection()) {
             if (definition != survivalValueDefinition
                 && definition.getName().equalsIgnoreCase(survivalValueDefinition.getName())) {
                 throw new InvalidSurvivalValueDefinitionException("That name already exists, must enter a unique "
                         + "name for Survival Value Definition.");
             }
         }
         Cai2Util.validateSurvivalValueDefinition(survivalValueDefinition);
     }
 
     /**
      * @return the survivalValueDefinition
      */
     public SurvivalValueDefinition getSurvivalValueDefinition() {
         return survivalValueDefinition;
     }
 
     /**
      * @param survivalValueDefinition the survivalValueDefinition to set
      */
     public void setSurvivalValueDefinition(SurvivalValueDefinition survivalValueDefinition) {
         this.survivalValueDefinition = survivalValueDefinition;
     }
 
     /**
      * @return the survivalValueDefinitions
      */
     public Map<String, SurvivalValueDefinition> getSurvivalValueDefinitions() {
         return survivalValueDefinitions;
     }
 
     /**
      * @param survivalValueDefinitions the survivalValueDefinitions to set
      */
     public void setSurvivalValueDefinitions(Map<String, SurvivalValueDefinition> survivalValueDefinitions) {
         this.survivalValueDefinitions = survivalValueDefinitions;
     }
 
 
     /**
      * @return the survivalDefinitionFormValues
      */
     public DefineSurvivalDefinitionActionForm getSurvivalDefinitionFormValues() {
         return survivalDefinitionFormValues;
     }
 
 
     /**
      * @param survivalDefinitionFormValues the survivalDefinitionFormValues to set
      */
     public void setSurvivalDefinitionFormValues(DefineSurvivalDefinitionActionForm survivalDefinitionFormValues) {
         this.survivalDefinitionFormValues = survivalDefinitionFormValues;
     }
 
 
     /**
      * @return the dateAnnotationDefinitions
      */
     public Map<String, AnnotationDefinition> getDateAnnotationDefinitions() {
         return dateAnnotationDefinitions;
     }
 
 
     /**
      * @param dateAnnotationDefinitions the dateAnnotationDefinitions to set
      */
     public void setDateAnnotationDefinitions(Map<String, AnnotationDefinition> dateAnnotationDefinitions) {
         this.dateAnnotationDefinitions = dateAnnotationDefinitions;
     }
 
     /**
      * @return the actionType
      */
     public String getActionType() {
         return actionType;
     }
 
     /**
      * @param actionType the actionType to set
      */
     public void setActionType(String actionType) {
         this.actionType = actionType;
     }
 
     /**
      * @return the numericAnnotationDefinitions
      */
     public Map<String, AnnotationDefinition> getNumericAnnotationDefinitions() {
         return numericAnnotationDefinitions;
     }
 
     /**
      * @param numericAnnotationDefinitions the numericAnnotationDefinitions to set
      */
     public void setNumericAnnotationDefinitions(Map<String, AnnotationDefinition> numericAnnotationDefinitions) {
         this.numericAnnotationDefinitions = numericAnnotationDefinitions;
     }
 
     /**
      * @return the survivalStatusAnnotationDefinitions
      */
     public Map<String, AnnotationDefinition> getSurvivalStatusAnnotationDefinitions() {
         return survivalStatusAnnotationDefinitions;
     }
 
     /**
      * @param survivalStatusAnnotationDefinitions the survivalStatusAnnotationDefinitions to set
      */
     public void setSurvivalStatusAnnotationDefinitions(
             Map<String, AnnotationDefinition> survivalStatusAnnotationDefinitions) {
         this.survivalStatusAnnotationDefinitions = survivalStatusAnnotationDefinitions;
     }
 
     /**
      * @return the survivalStatusValues
      */
     public List<String> getSurvivalStatusValues() {
         return survivalStatusValues;
     }
 
     /**
      * @param survivalStatusValues the survivalStatusValues to set
      */
     public void setSurvivalStatusValues(List<String> survivalStatusValues) {
         this.survivalStatusValues = survivalStatusValues;
     }
 
     /**
      * @return the newSurvivalValueDefinition
      */
     public boolean isNewDefinition() {
         return newDefinition;
     }
 
     /**
      *
      * @return css style value.
      */
     public String getDateInputCssStyle() {
         return SurvivalValueTypeEnum.DATE.getValue().equals(survivalDefinitionFormValues.getSurvivalValueType())
             ? "display: block;" : "display: none;";
     }
 
     /**
      *
      * @return css style value.
      */
     public String getLengthOfTimeInputCssStyle() {
         return SurvivalValueTypeEnum.LENGTH_OF_TIME.getValue().equals(
                 survivalDefinitionFormValues.getSurvivalValueType()) ? "display: block;" : "display: none;";
     }
 
 }
