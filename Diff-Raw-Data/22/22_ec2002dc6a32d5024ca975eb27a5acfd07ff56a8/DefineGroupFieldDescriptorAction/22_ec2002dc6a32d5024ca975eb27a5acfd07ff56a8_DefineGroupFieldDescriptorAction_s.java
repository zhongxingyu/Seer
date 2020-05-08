 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.web.action.study.management;
 
 import gov.nih.nci.caintegrator.application.study.AnnotationGroup;
 import gov.nih.nci.caintegrator.web.ajax.DataElementSearchAjaxUpdater;
 
import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 /**
  * Action used to edit the type and annotation of a file column by a Study Manager.
  */
 @Component
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
 public class DefineGroupFieldDescriptorAction extends AbstractFieldDescriptorAction {
 
     private static final long serialVersionUID = 1L;
     private AnnotationGroup group = new AnnotationGroup();
 
     /**
      * Refreshes the current source configuration.
      */
     @Override
     public void prepare() {
         super.prepare();
         group = new AnnotationGroup();
         if (getGroupId() != null) {
             group.setId(Long.valueOf(getGroupId()));
             group = getStudyManagementService().getRefreshedEntity(group);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getCancelAction() {
         return "cancelGroupFieldDescriptor";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getEntityTypeForSearch() {
         return DataElementSearchAjaxUpdater.ReturnTypeEnum.GROUP_SOURCE.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getNewDefinitionAction() {
         return "createNewGroupDefinition";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSaveAnnotationDefinitionAction() {
         return "updateGroupAnnotationDefinition";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSaveFieldDescriptorTypeAction() {
         return "saveGroupFieldDescriptorType";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSelectDataElementAction() {
         return "selectGroupDataElement";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getSelectDefinitionAction() {
         return "selectGroupDefinition";
     }
 
     /**
      * @param group the group to set
      */
     public void setGroup(AnnotationGroup group) {
         this.group = group;
     }
 }
