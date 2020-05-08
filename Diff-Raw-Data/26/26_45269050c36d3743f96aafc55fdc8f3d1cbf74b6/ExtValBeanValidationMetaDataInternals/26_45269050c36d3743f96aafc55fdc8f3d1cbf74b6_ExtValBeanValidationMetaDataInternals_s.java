 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.extensions.validator.beanval.util;
 
 import org.apache.myfaces.extensions.validator.beanval.ExtValBeanValidationContext;
 import org.apache.myfaces.extensions.validator.beanval.payload.ViolationSeverity;
 import org.apache.myfaces.extensions.validator.beanval.annotation.BeanValidation;
 import org.apache.myfaces.extensions.validator.beanval.annotation.ModelValidation;
 import org.apache.myfaces.extensions.validator.beanval.annotation.extractor.DefaultGroupControllerScanningExtractor;
 import org.apache.myfaces.extensions.validator.beanval.storage.ModelValidationEntry;
 import org.apache.myfaces.extensions.validator.core.el.ELHelper;
 import org.apache.myfaces.extensions.validator.core.el.ValueBindingExpression;
 import org.apache.myfaces.extensions.validator.core.metadata.MetaDataEntry;
 import org.apache.myfaces.extensions.validator.core.property.PropertyDetails;
 import org.apache.myfaces.extensions.validator.core.property.PropertyInformation;
 import org.apache.myfaces.extensions.validator.core.validation.message.FacesMessageHolder;
 import org.apache.myfaces.extensions.validator.internal.UsageCategory;
 import org.apache.myfaces.extensions.validator.internal.UsageInformation;
 import org.apache.myfaces.extensions.validator.internal.ToDo;
 import org.apache.myfaces.extensions.validator.internal.Priority;
 import org.apache.myfaces.extensions.validator.util.ExtValUtils;
 import org.apache.myfaces.extensions.validator.util.ReflectionUtils;
 import org.apache.myfaces.extensions.validator.util.ProxyUtils;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import javax.validation.ConstraintViolation;
 import javax.validation.Payload;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * @author Gerhard Petracek
  * @since x.x.3
  */
 @UsageInformation(UsageCategory.INTERNAL)
 class ExtValBeanValidationMetaDataInternals
 {
     private Logger logger;
     private LabeledMessageInternals labeledMessageInternals = new LabeledMessageInternals();
     private ELHelper elHelper;
 
     ExtValBeanValidationMetaDataInternals(Logger logger, ELHelper elHelper)
     {
         this.logger = logger;
         this.elHelper = elHelper;
     }
 
     void extractExtValBeanValidationMetaData(PropertyDetails propertyDetails,
                                              boolean processModelValidation,
                                              String[] key,
                                              Object firstBean,
                                              List<Class> foundGroupsForPropertyValidation,
                                              List<Class> restrictedGroupsForPropertyValidation,
                                              List<ModelValidationEntry> modelValidationEntryList,
                                              List<Class> restrictedGroupsForModelValidation,
                                              String activeViewId)
     {
         inspectFirstBean(processModelValidation,
                 firstBean,
                 foundGroupsForPropertyValidation,
                 restrictedGroupsForPropertyValidation,
                 modelValidationEntryList,
                 restrictedGroupsForModelValidation,
                 activeViewId);
 
         inspectFirstProperty(processModelValidation,
                 key,
                 firstBean,
                 foundGroupsForPropertyValidation,
                 restrictedGroupsForPropertyValidation,
                 modelValidationEntryList,
                 restrictedGroupsForModelValidation,
                 activeViewId,
                 key.length == 2);
 
         inspectBaseOfProperty(propertyDetails,
                 processModelValidation,
                 foundGroupsForPropertyValidation,
                 restrictedGroupsForPropertyValidation,
                 modelValidationEntryList,
                 restrictedGroupsForModelValidation,
                 activeViewId);
 
         inspectLastProperty(propertyDetails,
                 processModelValidation,
                 foundGroupsForPropertyValidation,
                 restrictedGroupsForPropertyValidation,
                 modelValidationEntryList,
                 restrictedGroupsForModelValidation,
                 activeViewId);
     }
 
     void processExtValBeanValidationMetaData(UIComponent component,
                                              PropertyDetails propertyDetails,
                                              List<Class> foundGroupsForPropertyValidation,
                                              List<Class> restrictedGroupsForPropertyValidation,
                                              List<ModelValidationEntry> modelValidationEntryList,
                                              List<Class> restrictedGroupsForModelValidation,
                                              String activeViewId)
     {
         ExtValBeanValidationContext extValBeanValidationContext = ExtValBeanValidationContext.getCurrentInstance();
 
         String clientId = component.getClientId(FacesContext.getCurrentInstance());
 
         processFoundGroups(extValBeanValidationContext, activeViewId, clientId,
                 foundGroupsForPropertyValidation);
 
         processRestrictedGroups(extValBeanValidationContext, activeViewId, clientId,
                 restrictedGroupsForPropertyValidation);
 
         initModelValidation(extValBeanValidationContext, component, propertyDetails,
                 modelValidationEntryList, restrictedGroupsForModelValidation);
     }
 
     private void inspectFirstBean(boolean processModelValidation,
                                   Object firstBean,
                                   List<Class> foundGroupsForPropertyValidation,
                                   List<Class> restrictedGroupsForPropertyValidation,
                                   List<ModelValidationEntry> modelValidationEntryList,
                                   List<Class> restrictedGroupsForModelValidation,
                                   String activeViewId)
     {
         processClass(firstBean,
                 foundGroupsForPropertyValidation,
                 restrictedGroupsForPropertyValidation,
                 modelValidationEntryList,
                 restrictedGroupsForModelValidation,
                 activeViewId,
                 processModelValidation);
     }
 
     private void inspectFirstProperty(boolean processModelValidation,
                                       String[] key,
                                       Object firstBean,
                                       List<Class> foundGroupsForPropertyValidation,
                                       List<Class> restrictedGroupsForPropertyValidation,
                                       List<ModelValidationEntry> modelValidationEntryList,
                                       List<Class> restrictedGroupsForModelValidation,
                                       String activeViewId,
                                       boolean isLastProperty)
     {
         processFieldsAndProperties(key[0] + "." + key[1],
                 firstBean,
                 key[1],
                 foundGroupsForPropertyValidation,
                 restrictedGroupsForPropertyValidation,
                 modelValidationEntryList,
                 restrictedGroupsForModelValidation,
                 activeViewId,
                 processModelValidation,
                 isLastProperty);
     }
 
     private void inspectBaseOfProperty(PropertyDetails propertyDetails,
                                        boolean processModelValidation,
                                        List<Class> foundGroupsForPropertyValidation,
                                        List<Class> restrictedGroupsForPropertyValidation,
                                        List<ModelValidationEntry> modelValidationEntryList,
                                        List<Class> restrictedGroupsForModelValidation,
                                        String activeViewId)
     {
         processClass(propertyDetails.getBaseObject(),
                 foundGroupsForPropertyValidation,
                 restrictedGroupsForPropertyValidation,
                 modelValidationEntryList,
                 restrictedGroupsForModelValidation,
                 activeViewId,
                 processModelValidation);
     }
 
     private void inspectLastProperty(PropertyDetails propertyDetails,
                                      boolean processModelValidation,
                                      List<Class> foundGroupsForPropertyValidation,
                                      List<Class> restrictedGroupsForPropertyValidation,
                                      List<ModelValidationEntry> modelValidationEntryList,
                                      List<Class> restrictedGroupsForModelValidation,
                                      String activeViewId)
     {
         processFieldsAndProperties(
                 propertyDetails.getKey(),
                 propertyDetails.getBaseObject(),
                 propertyDetails.getProperty(),
                 foundGroupsForPropertyValidation,
                 restrictedGroupsForPropertyValidation,
                 modelValidationEntryList,
                 restrictedGroupsForModelValidation,
                 activeViewId,
                 processModelValidation,
                 true);
     }
 
     private void processClass(Object objectToInspect,
                               List<Class> foundGroupsForPropertyValidation,
                               List<Class> restrictedGroupsForPropertyValidation,
                               List<ModelValidationEntry> modelValidationEntryList,
                               List<Class> restrictedGroupsForModelValidation,
                               String activeViewId,
                               boolean processModelValidation)
     {
         Class classToInspect = ProxyUtils.getUnproxiedClass(objectToInspect.getClass());
 
         while (!Object.class.getName().equals(classToInspect.getName()))
         {
            transferGroupValidationInformationToFoundGroups(objectToInspect,
                     foundGroupsForPropertyValidation,
                     restrictedGroupsForPropertyValidation,
                     modelValidationEntryList,
                     restrictedGroupsForModelValidation,
                     activeViewId,
                     processModelValidation);
 
            processInterfaces(objectToInspect.getClass(), objectToInspect,
                     foundGroupsForPropertyValidation,
                     restrictedGroupsForPropertyValidation,
                     modelValidationEntryList,
                     restrictedGroupsForModelValidation,
                     activeViewId,
                     processModelValidation);
 
             classToInspect = classToInspect.getSuperclass();
         }
     }
 
     private void processFieldsAndProperties(String key,
                                             Object base,
                                             String property,
                                             List<Class> foundGroupsForPropertyValidation,
                                             List<Class> restrictedGroupsForPropertyValidation,
                                             List<ModelValidationEntry> modelValidationEntryList,
                                             List<Class> restrictedGroupsForModelValidation,
                                             String activeViewId,
                                             boolean processModelValidation,
                                             boolean isLastProperty)
     {
         PropertyInformation propertyInformation = new DefaultGroupControllerScanningExtractor()
                 .extract(FacesContext.getCurrentInstance(), new PropertyDetails(key, base, property));
 
         for (MetaDataEntry metaDataEntry : propertyInformation.getMetaDataEntries())
         {
             if (metaDataEntry.getValue() instanceof BeanValidation)
             {
                 tryToProcessMetaData((BeanValidation) metaDataEntry.getValue(),
                         tryToCreateNewTarget(base, property, isLastProperty),
                         foundGroupsForPropertyValidation,
                         restrictedGroupsForPropertyValidation,
                         modelValidationEntryList,
                         restrictedGroupsForModelValidation,
                         activeViewId,
                         processModelValidation);
             }
             else if (metaDataEntry.getValue() instanceof BeanValidation.List)
             {
                 for (BeanValidation currentBeanValidation : ((BeanValidation.List) metaDataEntry.getValue()).value())
                 {
                     tryToProcessMetaData(currentBeanValidation,
                             tryToCreateNewTarget(base, property, isLastProperty),
                             foundGroupsForPropertyValidation,
                             restrictedGroupsForPropertyValidation,
                             modelValidationEntryList,
                             restrictedGroupsForModelValidation,
                             activeViewId,
                             processModelValidation);
                 }
             }
         }
     }
 
     private Object tryToCreateNewTarget(Object base, String property, boolean isLastProperty)
     {
         if(isLastProperty)
         {
             return base;
         }
 
         Object result = getValueOfProperty(base, property);
 
         if (result == null)
         {
             return base;
         }
 
         return result;
     }
 
     private Object getValueOfProperty(Object base, String property)
     {
         property = property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
 
         Class targetClass = ProxyUtils.getUnproxiedClass(base.getClass());
 
         Method targetMethod = ReflectionUtils.tryToGetMethod(targetClass, "get" + property);
 
         if (targetMethod == null)
         {
             targetMethod = ReflectionUtils.tryToGetMethod(targetClass, "is" + property);
         }
 
         if (targetMethod == null)
         {
             throw new IllegalStateException(
                     "class " + targetClass + " has no public get/is " + property.toLowerCase());
         }
         return ReflectionUtils.tryToInvokeMethod(base, targetMethod);
     }
 
     private void processFoundGroups(ExtValBeanValidationContext extValBeanValidationContext,
                                     String currentViewId,
                                     String clientId,
                                     List<Class> foundGroupsForPropertyValidation)
     {
         /*
          * add found groups to context
          */
         for (Class currentGroupClass : foundGroupsForPropertyValidation)
         {
             extValBeanValidationContext.addGroup(currentGroupClass, currentViewId, clientId);
         }
     }
 
     private void processRestrictedGroups(ExtValBeanValidationContext extValBeanValidationContext,
                                          String currentViewId,
                                          String clientId,
                                          List<Class> restrictedGroupsForPropertyValidation)
     {
         /*
          * add restricted groups
          */
         for (Class currentGroupClass : restrictedGroupsForPropertyValidation)
         {
             extValBeanValidationContext.restrictGroup(currentGroupClass, currentViewId, clientId);
         }
     }
 
     private void initModelValidation(ExtValBeanValidationContext extValBeanValidationContext,
                                      UIComponent component,
                                      PropertyDetails propertyDetails,
                                      List<ModelValidationEntry> modelValidationEntryList,
                                      List<Class> restrictedGroupsForModelValidation)
     {
         /*
          * add model validation entry list
          */
         for (ModelValidationEntry modelValidationEntry : modelValidationEntryList)
         {
             for (Class restrictedGroup : restrictedGroupsForModelValidation)
             {
                 modelValidationEntry.removeGroup(restrictedGroup);
             }
 
             if (modelValidationEntry.getGroups().length > 0)
             {
                 if (modelValidationEntry.getValidationTargets().isEmpty())
                 {
                     modelValidationEntry.addValidationTarget(propertyDetails.getBaseObject());
                 }
                 modelValidationEntry.setComponent(component);
                 extValBeanValidationContext.addModelValidationEntry(modelValidationEntry);
             }
         }
     }
 
     @ToDo(value = Priority.MEDIUM, description = "test proxy support")
     private void transferGroupValidationInformationToFoundGroups(
             Object objectToInspect,
             List<Class> foundGroupsForPropertyValidation,
             List<Class> restrictedGroupsForPropertyValidation,
             List<ModelValidationEntry> modelValidationEntryList,
             List<Class> restrictedGroupsForModelValidation,
             String activeViewId,
             boolean processModelValidation)
     {
        if (objectToInspect.getClass().isAnnotationPresent(BeanValidation.class))
         {
            tryToProcessMetaData(objectToInspect.getClass().getAnnotation(BeanValidation.class),
                     objectToInspect,
                     foundGroupsForPropertyValidation,
                     restrictedGroupsForPropertyValidation,
                     modelValidationEntryList,
                     restrictedGroupsForModelValidation,
                     activeViewId,
                     processModelValidation);
         }
        else if (objectToInspect.getClass().isAnnotationPresent(BeanValidation.List.class))
         {
             for (BeanValidation currentBeanValidation :
                    (objectToInspect.getClass().getAnnotation(BeanValidation.List.class)).value())
             {
                 tryToProcessMetaData(currentBeanValidation,
                         objectToInspect,
                         foundGroupsForPropertyValidation,
                         restrictedGroupsForPropertyValidation,
                         modelValidationEntryList,
                         restrictedGroupsForModelValidation,
                         activeViewId,
                         processModelValidation);
             }
         }
     }
 
     private void processInterfaces(Class currentClass,
                                    Object metaDataSourceObject,
                                    List<Class> foundGroupsForPropertyValidation,
                                    List<Class> restrictedGroupsForPropertyValidation,
                                    List<ModelValidationEntry> modelValidationEntryList,
                                    List<Class> restrictedGroupsForModelValidation,
                                    String activeViewId,
                                    boolean processModelValidation)
     {
         for (Class currentInterface : currentClass.getInterfaces())
         {
            transferGroupValidationInformationToFoundGroups(metaDataSourceObject,
                     foundGroupsForPropertyValidation,
                     restrictedGroupsForPropertyValidation,
                     modelValidationEntryList,
                     restrictedGroupsForModelValidation,
                     activeViewId,
                     processModelValidation);
 
             processInterfaces(currentInterface, metaDataSourceObject,
                     foundGroupsForPropertyValidation,
                     restrictedGroupsForPropertyValidation,
                     modelValidationEntryList,
                     restrictedGroupsForModelValidation,
                     activeViewId,
                     processModelValidation);
         }
     }
 
     private void tryToProcessMetaData(BeanValidation beanValidation,
                                       Object metaDataSourceObject,
                                       List<Class> foundGroupsForPropertyValidation,
                                       List<Class> restrictedGroupsForPropertyValidation,
                                       List<ModelValidationEntry> modelValidationEntryList,
                                       List<Class> restrictedGroupsForModelValidation,
                                       String activeViewId,
                                       boolean processModelValidation)
     {
         for (String currentViewId : beanValidation.viewIds())
         {
             if (useMetaDataForViewId(beanValidation, currentViewId, activeViewId))
             {
                 processMetaData(beanValidation,
                         metaDataSourceObject,
                         foundGroupsForPropertyValidation,
                         restrictedGroupsForPropertyValidation,
                         modelValidationEntryList,
                         restrictedGroupsForModelValidation,
                         processModelValidation);
                 break;
             }
         }
     }
 
     private boolean useMetaDataForViewId(BeanValidation beanValidation, String viewId, String activeViewId)
     {
         return (viewId.equals(activeViewId) ||
                 viewId.equals("*")) && isValidationPermitted(beanValidation);
     }
 
     private void processMetaData(BeanValidation beanValidation,
                                  Object metaDataSourceObject,
                                  List<Class> foundGroupsForPropertyValidation,
                                  List<Class> restrictedGroupsForPropertyValidation,
                                  List<ModelValidationEntry> modelValidationEntryList,
                                  List<Class> restrictedGroupsForModelValidation,
                                  boolean processModelValidation)
     {
         if (processModelValidation && isModelValidation(beanValidation))
         {
             addModelValidationEntry(
                     beanValidation, metaDataSourceObject,
                     modelValidationEntryList, restrictedGroupsForModelValidation);
         }
         else if (!isModelValidation(beanValidation))
         {
             processGroups(
                     beanValidation, foundGroupsForPropertyValidation, restrictedGroupsForPropertyValidation);
         }
     }
 
     private boolean isValidationPermitted(BeanValidation beanValidation)
     {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         for (String condition : beanValidation.conditions())
         {
             if (elHelper.isELTermWellFormed(condition) &&
                     elHelper.isELTermValid(facesContext, condition))
             {
                 if (Boolean.TRUE.equals(elHelper.getValueOfExpression(
                         facesContext, new ValueBindingExpression(condition))))
                 {
                     return true;
                 }
             }
             else
             {
                 this.logger.severe("an invalid condition is used: " + condition);
             }
         }
         return false;
     }
 
     private boolean isModelValidation(BeanValidation beanValidation)
     {
         return beanValidation.modelValidation().isActive();
     }
 
     private void addModelValidationEntry(BeanValidation beanValidation,
                                          Object metaDataSourceObject,
                                          List<ModelValidationEntry> modelValidationEntryList,
                                          List<Class> restrictedGroupsForModelValidation)
     {
         ModelValidationEntry modelValidationEntry = new ModelValidationEntry();
 
         modelValidationEntry.setGroups(Arrays.asList(beanValidation.useGroups()));
         modelValidationEntry.setDisplayMessageInline(beanValidation.modelValidation().displayInline());
         modelValidationEntry.setCustomMessage(beanValidation.modelValidation().message());
         modelValidationEntry.setMetaDataSourceObject(metaDataSourceObject);
 
         Object validationTarget;
         for (String validationTargetExpression : beanValidation.modelValidation().validationTargets())
         {
             if (ModelValidation.DEFAULT.equals(validationTargetExpression))
             {
                 continue;
             }
 
             validationTarget = tryToResolveValidationTargetExpression(validationTargetExpression);
             if (validationTarget != null)
             {
                 modelValidationEntry.addValidationTarget(validationTarget);
             }
         }
 
         if (beanValidation.restrictGroups().length > 0)
         {
             restrictedGroupsForModelValidation.addAll(Arrays.asList(beanValidation.restrictGroups()));
         }
 
         if (modelValidationEntry.getValidationTargets().isEmpty())
         {
             modelValidationEntry.addValidationTarget(metaDataSourceObject);
         }
 
         modelValidationEntryList.add(modelValidationEntry);
     }
 
     private Object tryToResolveValidationTargetExpression(String validationTargetExpression)
     {
         ValueBindingExpression valueBindingExpression = new ValueBindingExpression(validationTargetExpression);
         return this.elHelper.getValueOfExpression(FacesContext.getCurrentInstance(), valueBindingExpression);
     }
 
     private void processGroups(BeanValidation beanValidation,
                                List<Class> foundGroupsForPropertyValidation,
                                List<Class> restrictedGroupsForPropertyValidation)
     {
         foundGroupsForPropertyValidation.addAll(Arrays.asList(beanValidation.useGroups()));
 
         if (beanValidation.restrictGroups().length > 0)
         {
             restrictedGroupsForPropertyValidation.addAll(Arrays.asList(beanValidation.restrictGroups()));
         }
     }
 
     String createLabeledMessage(String violationMessage, boolean isDetailMessage)
     {
         return this.labeledMessageInternals.createLabeledMessage(violationMessage, isDetailMessage);
     }
 
     FacesMessage.Severity calcSeverity(ConstraintViolation<Object> violation)
     {
         for (Class<? extends Payload> payload : violation.getConstraintDescriptor().getPayload())
         {
             if (ExtValUtils.getValidationParameterClassFor(ViolationSeverity.Warn.class).isAssignableFrom(payload))
             {
                 return FacesMessage.SEVERITY_WARN;
             }
             else if (ExtValUtils
                     .getValidationParameterClassFor(ViolationSeverity.Info.class).isAssignableFrom(payload))
             {
                 return FacesMessage.SEVERITY_INFO;
             }
             else if (ExtValUtils
                     .getValidationParameterClassFor(ViolationSeverity.Fatal.class).isAssignableFrom(payload))
             {
                 return FacesMessage.SEVERITY_FATAL;
             }
         }
         return FacesMessage.SEVERITY_ERROR;
     }
 
     void processFacesMessage(FacesContext facesContext, UIComponent uiComponent,
                              List<FacesMessageHolder> facesMessageHolderList, FacesMessage facesMessage)
     {
         FacesMessageHolder facesMessageHolder = new FacesMessageHolder(facesMessage);
 
         facesMessageHolder.setClientId(uiComponent.getClientId(facesContext));
 
         facesMessageHolderList.add(facesMessageHolder);
     }
 
     boolean executeAfterThrowingInterceptors(UIComponent uiComponent,
                                              Object convertedObject,
                                              ValidatorException validatorException)
     {
         return ExtValUtils.executeAfterThrowingInterceptors(
                 uiComponent,
                 null,
                 convertedObject,
                 validatorException,
                 null);
     }
 
     boolean isMessageTextUnchanged(
             ValidatorException validatorException, String violationSummaryMessage, String violationDetailMessage)
     {
         return violationSummaryMessage.equals(validatorException.getFacesMessage().getSummary()) &&
                 violationDetailMessage.equals(validatorException.getFacesMessage().getDetail());
     }
 
     ValidatorException createValidatorException(
             String violationSummaryMessage, String violationDetailMessage, FacesMessage.Severity severity)
     {
         return new ValidatorException(
                 ExtValUtils.createFacesMessage(severity, violationSummaryMessage, violationDetailMessage));
     }
 
     List<FacesMessageHolder> getFacesMessageListWithLowSeverity(
             List<FacesMessageHolder> violationMessages)
     {
         List<FacesMessageHolder> result = new ArrayList<FacesMessageHolder>();
 
         for (FacesMessageHolder facesMessageHolder : violationMessages)
         {
             if (FacesMessage.SEVERITY_WARN.equals(facesMessageHolder.getFacesMessage().getSeverity()) ||
                     FacesMessage.SEVERITY_INFO.equals(facesMessageHolder.getFacesMessage().getSeverity()))
             {
                 result.add(facesMessageHolder);
             }
         }
         return result;
     }
 
     List<FacesMessageHolder> getFacesMessageListWithHighSeverity(
             List<FacesMessageHolder> violationMessageHolderList)
     {
         List<FacesMessageHolder> result = new ArrayList<FacesMessageHolder>();
 
         for (FacesMessageHolder facesMessageHolder : violationMessageHolderList)
         {
             if (FacesMessage.SEVERITY_ERROR.equals(facesMessageHolder.getFacesMessage().getSeverity()) ||
                     FacesMessage.SEVERITY_FATAL.equals(facesMessageHolder.getFacesMessage().getSeverity()))
             {
                 result.add(facesMessageHolder);
             }
         }
         return result;
     }
 
     void addMessages(List<FacesMessageHolder> facesMessageHolderListWithLowSeverity)
     {
         for (FacesMessageHolder facesMessageHolder : facesMessageHolderListWithLowSeverity)
         {
             ExtValUtils.tryToAddViolationMessageForComponentId(
                     facesMessageHolder.getClientId(), facesMessageHolder.getFacesMessage());
         }
     }
 }
