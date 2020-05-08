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
 package ro.zg.netcell.vaadin.action;
 
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ro.zg.open_groups.OpenGroupsApplication;
 import ro.zg.open_groups.gui.constants.OpenGroupsStyles;
 import ro.zg.opengroups.vo.Entity;
 import ro.zg.opengroups.vo.UserAction;
 import ro.zg.opengroups.vo.UserActionList;
 
 import com.vaadin.ui.AbstractComponentContainer;
 import com.vaadin.ui.AbstractLayout;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
 import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
 import com.vaadin.ui.themes.Reindeer;
 
 /**
  * This is going to basically create a tabsheet with all the nested actions from a {@link UserActionList} object
  * 
  * @author adi
  * 
  */
 public class UserActionListHandler extends OpenGroupsActionHandler {
 
     /**
      * 
      */
     private static final long serialVersionUID = 8494158542993409953L;
 
     @Override
     public void handle(final ActionContext actionContext) throws Exception {
 	ComponentContainer displayArea = actionContext.getTargetContainer();
 	displayArea.removeAllComponents();
 	
 	UserActionList ual = (UserActionList) actionContext.getUserAction();
 	final OpenGroupsApplication app = actionContext.getApp();
 	final Entity entity = actionContext.getEntity();
 
 	final TabSheet actionsTabSheet = new TabSheet();
 	actionsTabSheet.addStyleName(Reindeer.TABSHEET_MINIMAL);
 //	actionsTabSheet.addStyleName(OpenGroupsStyles.USER_ACTION_CONTENT_PANE);
 	actionsTabSheet.addStyleName(OpenGroupsStyles.USER_ACTIONS_TABSHEET);
 //	final CssLayout contentArea = new CssLayout();
 //	contentArea.setWidth("100%");
 //	contentArea.setStyleName(OpenGroupsStyles.USER_ACTION_CONTENT_PANE);
 //	displayArea.addComponent(contentArea);
 	
 	/* add listener */
 	actionsTabSheet.addListener(new SelectedTabChangeListener() {
 
 	    @Override
 	    public void selectedTabChange(SelectedTabChangeEvent event) {
 		TabSheet tabSheet = event.getTabSheet();
 
 		AbstractComponentContainer selectedTabContent = (AbstractComponentContainer) tabSheet.getSelectedTab();
 		UserAction ua = (UserAction) selectedTabContent.getData();
 		if (entity != null) {
 		    Deque<String> desiredActionsQueue = entity.getState().getDesiredActionTabsQueue();
 		    /*
 		     * if a desired action exists, it will be set afterwards, otherwise allow the first action from the
 		     * list to be selected by default
 		     */
 		    if (desiredActionsQueue.size() != 0) {
 			String nextAction = desiredActionsQueue.peek();
 			if (nextAction.equals(ua.getActionName())) {
 			    /* remove action from the queue */
 			    desiredActionsQueue.remove();
 			} else {
 			    /*
 			     * if this action does not match with the next desired action, do nothing
 			     */
 			    return;
 			}
 		    }
 		}
 		
 		if (ua instanceof UserActionList) {
 //		    selectedTabContent.removeStyleName(OpenGroupsStyles.USER_ACTION_CONTENT_PANE);
 //		    contentArea.setWidth("100%");
 //		    contentArea.setMargin(false);
 //		    selectedTabContent.setMargin(false);
 		    
 		    ua.executeHandler(entity, app, selectedTabContent, false, actionContext);
 		    
 		} else {
 //		    selectedTabContent.addStyleName(OpenGroupsStyles.USER_ACTION_CONTENT_PANE);
 //		    contentArea.setWidth("99.5%");
 //		    contentArea.setMargin(true);
 //		    selectedTabContent.setMargin(true);
 //		    selectedTabContent.setWidth("100%");
 		    if (entity != null) {
 			entity.getState().setCurrentTabAction(ua);
 			entity.getState().setCurrentTabActionContainer(selectedTabContent);
 			entity.getState().setCurrentActionsPath(ua.getFullActionPath());
 //			entity.getState().getDesiredActionTabsQueue().clear();
 //			entity.getState().resetPageInfoForCurrentAction();
 			actionContext.getWindow().setFragmentToEntity(entity);
 		    }
 		    ua.executeHandler(entity, app, selectedTabContent, false, actionContext);
 		    
 		}
 
 	    }
 	});
 	/* add the tabsheet to the target component */
 
 //	List<String> currentUserTypes = getCurrentUserTypes(entity, app);
 	Map<String, ComponentContainer> actionPathContainers = new HashMap<String, ComponentContainer>();
 	List<UserAction> actionsList = new ArrayList<UserAction>(ual.getActions().values());
 	for (UserAction cua : actionsList) {
 
 	    /* display only the actions that the user is allowed to see */
 //	    if (!cua.allowRead(currentUserTypes)) {
 	    if(!cua.isVisible(actionContext)) {
 		continue;
 	    }
 
 	    CssLayout tabContent = new CssLayout();
 	    if (cua instanceof UserActionList) {
 //		tabContent.setMargin(false);
 //		contentArea.setMargin(false);
 //		tabContent.addStyleName(OpenGroupsStyles.USER_ACTION_CONTENT_PANE);
 //		tabContent.setWidth("100%");
 
 	    } else {
 //		tabContent.addStyleName(OpenGroupsStyles.USER_ACTION_CONTENT_PANE);
 		tabContent.setMargin(true);
 //		contentArea.addStyleName(OpenGroupsStyles.USER_ACTION_CONTENT_PANE);
 //		contentArea.setMargin(true);
 	    }
 	    tabContent.addStyleName(OpenGroupsStyles.USER_ACTION_CONTENT_PANE);
 	    actionPathContainers.put(cua.getActionName(), tabContent);
 	    tabContent.setData(cua);
 	    actionsTabSheet.addTab(tabContent, cua.getDisplayName(), null);
 	}
 	
 	displayArea.addComponent(actionsTabSheet);
 	if (entity != null) {
 	    Deque<String> desiredActionsQueue = entity.getState().getDesiredActionTabsQueue();
 
 	    if (desiredActionsQueue.size() != 0) {
 		// System.out.println("desired actions: " +
 		// entity.getState().getDesiredActionsPath());
 		// System.out.println("full url: "+app.getFullUrl());
 		/* select the tab specified by the next desired action */
 		actionsTabSheet.setSelectedTab(actionPathContainers.get(desiredActionsQueue.peek()));
 	    }
 	}
     }
 
 }
