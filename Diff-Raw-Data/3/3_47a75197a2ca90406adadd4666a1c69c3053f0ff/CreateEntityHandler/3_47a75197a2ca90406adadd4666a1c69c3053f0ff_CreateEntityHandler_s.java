 /*******************************************************************************
  * Copyright 2011 Adrian Cristian Ionescu
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package ro.zg.netcell.vaadin.action.application;
 
 import java.util.List;
 import java.util.Map;
 
 import ro.zg.netcell.control.CommandResponse;
 import ro.zg.netcell.vaadin.DataTranslationUtils;
 import ro.zg.netcell.vaadin.DefaultForm;
 import ro.zg.netcell.vaadin.DefaultForm.FormCommitEvent;
 import ro.zg.netcell.vaadin.DefaultForm.FormListener;
 import ro.zg.netcell.vaadin.action.ActionContext;
 import ro.zg.netcell.vaadin.action.ActionsManager;
 import ro.zg.netcell.vaadin.action.OpenGroupsActionHandler;
 import ro.zg.open_groups.OpenGroupsApplication;
 import ro.zg.opengroups.constants.ComplexEntityParam;
 import ro.zg.opengroups.vo.Entity;
 import ro.zg.opengroups.vo.User;
 import ro.zg.opengroups.vo.UserAction;
 
 import com.vaadin.terminal.UserError;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 
 public class CreateEntityHandler extends OpenGroupsActionHandler {
 
     /**
      * 
      */
     private static final long serialVersionUID = -6631433190447717139L;
 
     @Override
     public void handle(ActionContext actionContext) throws Exception {
 	ComponentContainer targetContainer = actionContext.getTargetContainer();
 	Entity entity = actionContext.getEntity();
 	targetContainer.removeAllComponents();
 	UserAction ua = actionContext.getUserAction();
 	List<String> currentUserTypes = getCurrentUserTypes(entity, actionContext.getApp());
 
 	if (!currentUserTypes.contains(ua.getUserType())) {
 	    /* current user is not allowed to execute this action */
 	    displayLoginRequired("create." + ua.getTargetEntityComplexType().toLowerCase() + ".login.required",
 		    targetContainer);
 	    return;
 	}
 
 	DefaultForm form = getForm(entity, actionContext.getUserAction(), actionContext.getApp(), targetContainer,actionContext);
 	targetContainer.addComponent(form);
 
     }
 
     private DefaultForm getForm(final Entity entity, final UserAction ua, final OpenGroupsApplication application,
 	    final ComponentContainer targetComponent, final ActionContext ac) {
 	final DefaultForm form = ua.generateForm();
 	final Entity parentEntity = ac.getMainEntity();
 	// EntityDefinitionSummary actionDef = getActionsManager().getFlowDefinitionSummary(ua.getAction());
 	// List<InputParameter> actionInputParams = actionDef.getInputParameters();
 	// List<InputParameter> userInputParams = ua.getUserInputParamsList(actionInputParams);
 	//
 	// form.setFormFieldFactory(new DefaultFormFieldFactory(userInputParams));
 	// form.populateFromInputParameterList(userInputParams);
 	form.addListener(new FormListener() {
 
 	    @Override
 	    public void onCommit(FormCommitEvent event) {
 
 		Map<String, Object> paramsMap = DataTranslationUtils.getFormFieldsAsMap(event.getForm());
 
 		String tags = ((String) paramsMap.get("tags"));
 		if (tags != null) {
 		    tags = tags.toLowerCase();
 		    tags.replaceAll("\\s", "");
 		    tags = "[" + tags + "]";
 		}
 		paramsMap.put("tags", tags);
 
 		User user = application.getCurrentUser();
 		paramsMap.put("userId", user.getUserId());
 		paramsMap.put("parentId", parentEntity.getId());
 		paramsMap.put("entityType", ua.getTargetEntityType());
 
 		String complexType = ua.getTargetEntityComplexType();
 
 		paramsMap.put("complexType", complexType);
 		paramsMap.put("allowDuplicateTitle", getAppConfigManager().getComplexEntityBooleanParam(complexType,
 			ComplexEntityParam.ALLOW_DUPLICATE_TITLE));
 		CommandResponse response = executeAction(new ActionContext(ua, application, entity), paramsMap);
 		if (response.isSuccessful()) {
 		    if ("titleExists".equals(response.getValue("exit"))) {
 			String message = application.getMessage(ua.getTargetEntityType().toLowerCase()
 				+ ".already.exists.with.title");
 			form.setComponentError(new UserError(message));
 		    } else {
 			long entityId = (Long) response.getValue("currentEntityId");
 			displaySuccessfulMessage(entity, ua, application, targetComponent, entityId,ac);
 		    }
 		}
 		/* refresh parent */
 		application.refreshEntity(parentEntity,ac);
 	    }
 	});
 
 	return form;
     }
 
     private void displaySuccessfulMessage(final Entity entity, final UserAction ua, final OpenGroupsApplication app,
 	    final ComponentContainer targetComponent, final long entityId, final ActionContext ac) {
 	/* store current target component */
 	// final ComponentContainer targetComponent = app.getTargetComponent();
 	String entityTypeLowerCase = ua.getTargetEntityType().toLowerCase();
 	String createdSuccessfullyMessage = app.getMessage(entityTypeLowerCase + ".created.successfully");
 	String createNewMessage = app.getMessage("create.new." + entityTypeLowerCase);
 	String openCreatedMessage = app.getMessage("open.created." + entityTypeLowerCase);
 
 	VerticalLayout container = new VerticalLayout();
 	container.setSizeFull();
 
 	Label success = new Label(createdSuccessfullyMessage);
 	container.addComponent(success);
 
 	HorizontalLayout linksContainer = new HorizontalLayout();
 	linksContainer.setSpacing(true);
 	Button openCreated = new Button(openCreatedMessage);
 	linksContainer.addComponent(openCreated);
 	openCreated.addListener(new ClickListener() {
 
 	    @Override
 	    public void buttonClick(ClickEvent event) {
 		List<String> subtypesList = getAppConfigManager().getSubtypesForComplexType(
 			ua.getTargetEntityComplexType());
 		if (subtypesList != null) {
 		    Entity entity = new Entity(entityId);
 //		    getActionsManager().executeAction(ActionsManager.REFRESH_SELECTED_ENTITY, entity, app, null, false,ac);
 //		    getActionsManager().executeAction(ActionsManager.OPEN_ENTITY_IN_TAB, entity, app, null, false,ac);
 		    app.openInActiveWindow(entity);
 		}
 		/* if no subtypes open the parent entity */
 		else {
 		    Entity parentEntity = ac.getMainEntity();
 		    parentEntity.getState().setEntityTypeVisible(true);
 		    parentEntity.getState().setDesiredActionsPath(ua.getTargetEntityComplexType() + "/LIST");
 //		    app.getTemporaryTab(parentEntity).setRefreshOn(true);
		    getActionsManager().executeAction(ActionsManager.OPEN_ENTITY_IN_TAB, parentEntity, app, null, false,ac);
 		}
 	    }
 	});
 
 	Button createNew = new Button(createNewMessage);
 	linksContainer.addComponent(createNew);
 	createNew.addListener(new ClickListener() {
 
 	    @Override
 	    public void buttonClick(ClickEvent event) {
 		/* recall the handle method on this handler */
 		// app.setTargetComponent(targetComponent);
 		ua.executeHandler(entity, app, targetComponent,ac);
 	    }
 	});
 
 	container.addComponent(linksContainer);
 
 	targetComponent.removeAllComponents();
 	targetComponent.addComponent(container);
 
     }
 
     private void displayLoginRequired(String messageKey, ComponentContainer targetContainer) {
 	String msg = getMessage(messageKey);
 	Label l = new Label(msg);
 	targetContainer.addComponent(l);
     }
 }
