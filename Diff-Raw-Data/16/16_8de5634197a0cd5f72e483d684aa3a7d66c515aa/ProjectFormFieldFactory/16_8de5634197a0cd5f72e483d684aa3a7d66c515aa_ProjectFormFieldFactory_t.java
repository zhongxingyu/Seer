 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.ibpworkbench.model.formfieldfactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.generationcp.commons.exceptions.InternationalizableException;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.actions.CropTypeComboAction;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.api.ManagerFactoryProvider;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.Location;
 import org.generationcp.middleware.pojos.Method;
 import org.generationcp.middleware.pojos.Person;
 import org.generationcp.middleware.pojos.User;
 import org.generationcp.middleware.pojos.workbench.CropType;
 import org.generationcp.middleware.pojos.workbench.WorkflowTemplate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.util.BeanContainer;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.data.validator.StringLengthValidator;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.DefaultFieldFactory;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.TwinColSelect;
 
 @Configurable
 public class ProjectFormFieldFactory extends DefaultFieldFactory{
 
     private static final Logger LOG = LoggerFactory.getLogger(ProjectFormFieldFactory.class);
     private static final long serialVersionUID = 1L;
 
     @Autowired
     private WorkbenchDataManager workbenchDataManager;
     
     @Autowired
     private ManagerFactoryProvider managerFactoryProvider;
     
     // For new item handling and listener of crop type combo box
     private CropTypeComboAction cropTypeComboAction;
     
     public ProjectFormFieldFactory() {
         cropTypeComboAction = new CropTypeComboAction();
     }
 
     @Override
     public Field createField(Item item, Object propertyId, Component uiContext) {
         Field field = super.createField(item, propertyId, uiContext);
 
         if ("projectName".equals(propertyId)) {
             TextField tf = (TextField) field;
             tf.setRequired(true);
             tf.setRequiredError("Please enter a Project Name.");
             tf.addValidator(new StringLengthValidator("Project Name must be 3-255 characters", 3, 255, false));
         } else if ("startDate".equals(propertyId)) {
             field.setRequired(true);
             field.setRequiredError("Please enter a Start Date.");
         } else if ("template".equals(propertyId)) {
             BeanContainer<Long, WorkflowTemplate> templateContainer = new BeanContainer<Long, WorkflowTemplate>(WorkflowTemplate.class);
             templateContainer.setBeanIdProperty("templateId");
 
             // TODO: Verify the try-catch flow
             try {
                 List<WorkflowTemplate> templateList = workbenchDataManager.getWorkflowTemplates();
 
                 for (WorkflowTemplate template : templateList) {
                     templateContainer.addBean(template);
                 }
 
                 ComboBox comboBox = new ComboBox("Workflow Template");
                 comboBox.setContainerDataSource(templateContainer);
                 comboBox.setItemCaptionPropertyId("name");
                 comboBox.setRequired(true);
                 comboBox.setRequiredError("Please enter a Workflow Template.");
 
                 return comboBox;
             } catch (MiddlewareQueryException e) {
                 LOG.error("Error encountered while getting workflow templates", e);
                 throw new InternationalizableException(e, Message.DATABASE_ERROR, 
                         Message.CONTACT_ADMIN_ERROR_DESC);
             }
         } else if ("cropType".equals(propertyId)) {
             List<CropType> cropTypes = null;
             try {
                 cropTypes = workbenchDataManager.getInstalledCentralCrops();
             }
             catch (MiddlewareQueryException e) {
                 LOG.error("Error encountered while getting installed central crops", e);
                 throw new InternationalizableException(e, Message.DATABASE_ERROR, 
                         Message.CONTACT_ADMIN_ERROR_DESC);
             }
             
             BeanItemContainer<CropType> beanItemContainer = new BeanItemContainer<CropType>(CropType.class);
             for (CropType cropType : cropTypes) {
                 beanItemContainer.addBean(cropType);
             }
 
             ComboBox comboBox = new ComboBox("Crop");
             comboBox.setContainerDataSource(beanItemContainer);
             comboBox.setNewItemsAllowed(true);
             cropTypeComboAction.setCropTypeComboBox(comboBox);
             comboBox.setNewItemHandler(cropTypeComboAction);
             comboBox.setItemCaptionPropertyId("cropName");
             comboBox.setRequired(true);
             comboBox.setRequiredError("Please select a Crop.");
             comboBox.setImmediate(true);
             
             return comboBox;
 
         } else if ("locations".equals(propertyId)) {
             TwinColSelect select = new TwinColSelect("Locations");
             select.setLeftColumnCaption("Available Locations");
             select.setRightColumnCaption("Selected Locations");
             select.setRows(10);
             select.setWidth("400px");
             select.setMultiSelect(true);
             select.setNullSelectionAllowed(true);
             
             CropType cropType = (CropType) item.getItemProperty("cropType").getValue();
             if (cropType != null) {
                 try {
                     Container container = createLocationsContainer(cropType);
                     select.setContainerDataSource(container);
 
                     for (Object itemId : container.getItemIds()) {
                         Location location = (Location) itemId;
                         select.setItemCaption(itemId, location.getLname());
                     }
                 }
                 catch (MiddlewareQueryException e) {
                     LOG.error("Error encountered while getting central locations", e);
                     throw new InternationalizableException(e, Message.DATABASE_ERROR, 
                                                            Message.CONTACT_ADMIN_ERROR_DESC);
                 }
             }
             
             return select;
 
         } else if ("methods".equals(propertyId)) {
             TwinColSelect select = new TwinColSelect("Methods");
             select.setLeftColumnCaption("Available Methods");
             select.setRightColumnCaption("Selected Methods");
             select.setRows(10);
             select.setWidth("400px");
             select.setMultiSelect(true);
             select.setNullSelectionAllowed(true);
             
             CropType cropType = (CropType) item.getItemProperty("cropType").getValue();
             if (cropType != null) {
                 try {
                     Container container = createMethodsContainer(cropType);
                     select.setContainerDataSource(container);
 
                     for (Object itemId : container.getItemIds()) {
                         Method method = (Method) itemId;
                         select.setItemCaption(itemId, method.getMname());
                     }
                 }
                 catch (MiddlewareQueryException e) {
                     LOG.error("Error encountered while getting central methods", e);
                     throw new InternationalizableException(e, Message.DATABASE_ERROR, 
                                                            Message.CONTACT_ADMIN_ERROR_DESC);
                 }
             }
 
             return select;
         }
         else if ("members".equals(propertyId)) {
             TwinColSelect select = new TwinColSelect("Project Members");
             select.setLeftColumnCaption("Available Users");
             select.setRightColumnCaption("Selected Project Members");
             select.setRows(10);
             select.setWidth("400px");
             select.setMultiSelect(true);
             select.setNullSelectionAllowed(true);
             
             try {
                 Container container = createUsersContainer();
                 select.setContainerDataSource(container);
 
                 for (Object itemId : container.getItemIds()) {
                     User user = (User) itemId;
                     select.setItemCaption(itemId, user.getPerson().getDisplayName());
                 }
             }
             catch (MiddlewareQueryException e) {
                 LOG.error("Error encountered while getting workbench users", e);
                 throw new InternationalizableException(e, Message.DATABASE_ERROR, 
                                                        Message.CONTACT_ADMIN_ERROR_DESC);
             }
             
             return select;
         }
         return field;
     }
 
     private Container createMethodsContainer(CropType cropType) throws MiddlewareQueryException {
         ManagerFactory managerFactory = managerFactoryProvider.getManagerFactoryForCropType(cropType);
         
         BeanItemContainer<Method> beanItemContainer = new BeanItemContainer<Method>(Method.class);
         if (managerFactory == null) {
             return beanItemContainer;
         }
         
         List<Method> methodList = managerFactory.getGermplasmDataManager().getAllMethods();
         for (Method method : methodList) {
             beanItemContainer.addBean(method);
         }
         
         return beanItemContainer;
     }
     
     private Container createLocationsContainer(CropType cropType) throws MiddlewareQueryException {
         ManagerFactory managerFactory = managerFactoryProvider.getManagerFactoryForCropType(cropType);
         
         BeanItemContainer<Location> beanItemContainer = new BeanItemContainer<Location>(Location.class);
         if (managerFactory == null) {
             return beanItemContainer;
         }
         
        long locCount = managerFactory.getGermplasmDataManager().countAllLocations();
        List<Location> locationList = managerFactory.getGermplasmDataManager().getAllLocations(0, (int) locCount);
         
         for (Location location : locationList) {
             beanItemContainer.addBean(location);
         }
         
         return beanItemContainer;
     }
 
     private Container createUsersContainer() throws MiddlewareQueryException {
         List<User> validUserList = new ArrayList<User>();
         
         // TODO: This can be improved once we implement proper User-Person mapping
         List<User> userList = workbenchDataManager.getAllUsers();
         for (User user : userList) {
             Person person = workbenchDataManager.getPersonById(user.getPersonid());
             user.setPerson(person);
             
             if (person != null) {
                 validUserList.add(user);
             }
         }
         
        BeanItemContainer<User> beanItemContainer = new BeanItemContainer<User>(User.class);
         for (User user : validUserList) {
             beanItemContainer.addBean(user);
         }
         
         return beanItemContainer;
     }
 
     
     public CropTypeComboAction getCropTypeComboAction() {
         return cropTypeComboAction;
     }
         
 }
