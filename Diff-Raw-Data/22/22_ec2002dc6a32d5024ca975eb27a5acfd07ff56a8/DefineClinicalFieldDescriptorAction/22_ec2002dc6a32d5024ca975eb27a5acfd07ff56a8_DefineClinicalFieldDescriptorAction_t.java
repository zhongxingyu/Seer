 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.web.action.study.management;
 
 import gov.nih.nci.caintegrator.web.ajax.DataElementSearchAjaxUpdater;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 /**
  * Action used to edit the type and annotation of a file column by a Study Manager.
  */
 @Component
@Scope("session")
 public class DefineClinicalFieldDescriptorAction extends AbstractFieldDescriptorAction {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getCancelAction() {
         return "cancelClinicalFieldDescriptor";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getEntityTypeForSearch() {
         return DataElementSearchAjaxUpdater.ReturnTypeEnum.CLINICAL_SOURCE.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getNewDefinitionAction() {
         return "createNewClinicalDefinition";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSaveAnnotationDefinitionAction() {
         return "updateClinicalAnnotationDefinition";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSaveFieldDescriptorTypeAction() {
         return "saveClinicalFieldDescriptorType";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSelectDataElementAction() {
         return "selectClinicalDataElement";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSelectDefinitionAction() {
         return "selectClinicalDefinition";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String saveFieldDescriptorType() {
         String result = super.saveFieldDescriptorType();
         if (SUCCESS.equals(result) && IDENTIFIER_TYPE.equals(getFieldDescriptorType())) {
             getStudyManagementService().unloadAllClinicalAnnotation(getStudyConfiguration());
             result = "saveIdentifier";
         }
         return result;
     }
 }
