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
  * Action used to edit the type and annotation of an imaging file column by a Study Manager.
  */
 @Component
@Scope("session")
 public class DefineImagingFieldDescriptorAction extends AbstractFieldDescriptorAction {
 
     private static final long serialVersionUID = 1L;
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void updateDataSourceStatus() {
         getStudyManagementService().updateImageDataSourceStatus(getStudyConfiguration()); // If it changes state
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getCancelAction() {
         return "cancelImagingFieldDescriptor";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getEntityTypeForSearch() {
         return DataElementSearchAjaxUpdater.ReturnTypeEnum.IMAGING_SOURCE.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getNewDefinitionAction() {
         return "createNewImagingDefinition";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSaveAnnotationDefinitionAction() {
         return "updateImagingAnnotationDefinition";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSaveFieldDescriptorTypeAction() {
         return "saveImagingFieldDescriptorType";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSelectDataElementAction() {
         return "selectImagingDataElement";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSelectDefinitionAction() {
         return "selectImagingDefinition";
     }
 
 }
