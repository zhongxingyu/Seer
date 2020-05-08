 /*******************************************************************************
 * Copyright (c) 2011 Nokia Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Comarch team - initial API and implementation
 *******************************************************************************/
 package org.ned.server.nedadminconsole.client.widgets;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.ned.server.nedadminconsole.client.NedCatalogService;
 import org.ned.server.nedadminconsole.client.NedCatalogServiceAsync;
 import org.ned.server.nedadminconsole.client.NedRes;
 import org.ned.server.nedadminconsole.client.NedStringGenerator;
 import org.ned.server.nedadminconsole.client.callbacks.NedUpdateUsersCallback;
 import org.ned.server.nedadminconsole.client.callbacks.NedUserListCallback;
 import org.ned.server.nedadminconsole.client.dialogs.NedAddNewUserDialog;
 import org.ned.server.nedadminconsole.client.dialogs.NedAlert;
 import org.ned.server.nedadminconsole.client.dialogs.NedChangePasswordDialog;
 import org.ned.server.nedadminconsole.client.interfaces.ConfirmCallback;
 import org.ned.server.nedadminconsole.client.interfaces.NedUserListUpdater;
 import org.ned.server.nedadminconsole.client.interfaces.NedUsernameReceiver;
 import org.ned.server.nedadminconsole.shared.NedUser;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 
 public class NedUserManagementWidget extends Composite implements NedUsernameReceiver, NedUserListUpdater {
 
     private List<NedUser> listUsers = null;
     private Grid gridUsers;
     private Label labelUsername; 
     private Label labelPassword;
     private boolean blockNavigation = false;
 
     public NedUserManagementWidget() {
 
         HorizontalPanel horizontalPanelOuter = new HorizontalPanel();
         horizontalPanelOuter.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
         
         VerticalPanel verticalPanel = new VerticalPanel();
         verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
         verticalPanel.setSpacing(10);
         initWidget(horizontalPanelOuter);
         horizontalPanelOuter.setWidth("100%");
         
         horizontalPanelOuter.add(verticalPanel);
         horizontalPanelOuter.setCellHorizontalAlignment(verticalPanel, HasHorizontalAlignment.ALIGN_CENTER);
         horizontalPanelOuter.setCellWidth(verticalPanel, "95%");
         verticalPanel.setSize("95%", "95px");
 
         gridUsers = new Grid(1, 4);
         gridUsers.setCellSpacing(13);
         gridUsers.setStyleName("NedUserManagementGrid");
         gridUsers.setBorderWidth(1);
         verticalPanel.add(gridUsers);
         verticalPanel.setCellHorizontalAlignment(gridUsers, HasHorizontalAlignment.ALIGN_CENTER);
         gridUsers.setWidth("100%");
         verticalPanel.setCellWidth(gridUsers, "100%");
 
         labelUsername = new Label(NedRes.instance().username());
         labelUsername.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
         labelUsername.setStyleName("NedUserManagementLabels");
         gridUsers.setWidget(0, 0, labelUsername);
         labelUsername.setWidth("85%");
         gridUsers.getCellFormatter().setWidth(0, 0, "36%");
 
         labelPassword =     new Label(NedRes.instance().userDlgPassword());
         labelPassword.setStyleName("NedUserManagementLabels");
         gridUsers.setWidget(0, 1, labelPassword);
         labelPassword.setWidth("85%");
         gridUsers.getCellFormatter().setHeight(0, 1, "");
         gridUsers.getCellFormatter().setWidth(0, 1, "36%");
         gridUsers.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
         gridUsers.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 
         HorizontalPanel horizontalPanel = new HorizontalPanel();
         horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
         verticalPanel.add(horizontalPanel);
         verticalPanel.setCellWidth(horizontalPanel, "100%");
         horizontalPanel.setWidth("100%");
 
         Button buttonAddUser = new Button("");
         buttonAddUser.addClickHandler(new ClickHandlerAddUser());
         buttonAddUser.setText(NedRes.instance().userDlgAddNewUser());
         horizontalPanel.add(buttonAddUser);
         horizontalPanel.setCellWidth(buttonAddUser, "33%");
         buttonAddUser.setWidth("100%");
 
         Button buttonSaveModifications = new Button("");
         buttonSaveModifications
                 .addClickHandler(new ClickHandlerSaveModifications());
         buttonSaveModifications.setText(NedRes.instance().userDlgSaveModifications());
         horizontalPanel.add(buttonSaveModifications);
         horizontalPanel.setCellWidth(buttonSaveModifications, "33%");
         buttonSaveModifications.setWidth("100%");
         
     }
     
     protected void onLoad()
     {
         if(listUsers == null)
         {
         NedCatalogServiceAsync service = (NedCatalogServiceAsync) GWT
         .create(NedCatalogService.class);
         ServiceDefTarget serviceDef = (ServiceDefTarget) service;
         serviceDef.setServiceEntryPoint("NedCatalogServlet");
         NedUserListCallback serviceCallback = new NedUserListCallback(this);
         service.getUserList(serviceCallback);
         }
     }
 
     protected void onUnload()
     {
         NedAlert.showInfo("unload");
     }
 
     public void loadUsers(List<NedUser> listUsers) {
         this.listUsers = listUsers;
         if(listUsers != null && gridUsers != null)
         {
         int gridRowCount = listUsers.size() + 1;
         gridUsers.resize(gridRowCount, 4);
         addUsersToList();
         }
     }
 
     private void addUsersToList() {
         
         for (int i = 0; i < listUsers.size(); i++) {
             fillUserRow(i+1, listUsers.get(i).username, "********");
         }
     }
 
     private void fillUserRow(int index, String username, String password)
     {
         TextBox textBoxUsername = new TextBox();
         textBoxUsername.setText(username);
         textBoxUsername.setReadOnly(true);
         gridUsers.setWidget(index, 0, textBoxUsername);
         gridUsers.getCellFormatter().setWidth(index, 0, "36%");
         textBoxUsername.setWidth("90%");
         TextBox textBoxPassword = new TextBox();
         textBoxPassword.setText(password);
         textBoxPassword.setReadOnly(true);
         gridUsers.setWidget(index, 1, textBoxPassword);
         gridUsers.getCellFormatter().setWidth(index, 1, "36%");
         textBoxPassword.setWidth("85%");
 
         
         gridUsers.getCellFormatter().setHorizontalAlignment(index, 1, HasHorizontalAlignment.ALIGN_CENTER);
         gridUsers.getCellFormatter().setHorizontalAlignment(index, 0, HasHorizontalAlignment.ALIGN_LEFT);
         addResetButton(index);
         addDeleteButton(index);
     }
 
     private class ClickHandlerAddUser implements ClickHandler {
 
         @Override
         public void onClick(ClickEvent event) {
                 new NedAddNewUserDialog(listUsers, NedUserManagementWidget.this).show();
 
         }
     }
 
     private void addResetButton(int rowIndex) {
         Button buttonResetPassword = new Button(NedRes.instance().userDlgNewPassword());
         buttonResetPassword.addClickHandler(new ClickHandlerResetPassword(
                 rowIndex));
         gridUsers.setWidget(rowIndex, 2, buttonResetPassword);
         gridUsers.getCellFormatter().setHorizontalAlignment(rowIndex, 2, HasHorizontalAlignment.ALIGN_CENTER);
         gridUsers.getCellFormatter().setWidth(rowIndex, 2, "20%");
         buttonResetPassword.setWidth("100%");
     }
 
     private void addDeleteButton(int rowIndex) {
         Image buttonImage = new Image("images/x_button_red2.png");
         PushButton buttonDelete = new PushButton(buttonImage, new Image("images/x_button_red2_pressed.png"));
         buttonDelete.setStylePrimaryName("NedDeleteButton");
         buttonDelete.addClickHandler(new ClickHandlerDelete(rowIndex));
         gridUsers.setWidget(rowIndex, 3, buttonDelete);
         gridUsers.getCellFormatter().setHorizontalAlignment(rowIndex, 3, HasHorizontalAlignment.ALIGN_RIGHT);
     }
 
     private class ClickHandlerResetPassword implements ClickHandler {
 
         private String username;
 
         public ClickHandlerResetPassword(int index) {
             username = listUsers.get(actualIndex(index - 1)).username;
         }
 
         @Override
         public void onClick(ClickEvent event) {
             new NedChangePasswordDialog(username, NedUserManagementWidget.this).show();
         }
     }
     
     private class ClickHandlerDelete implements ClickHandler {
 
         private String username;
 
         public ClickHandlerDelete(int index) {
             username =  listUsers.get(actualIndex(index - 1)).username;
         }
         @Override
         public void onClick(ClickEvent event) {
             int index = findIndex(username, false);
             if(index < 0)
             {
                 return; // error occurred
             }
             NedUser deleted = listUsers.get(findIndex(username, true));
             if((deleted.flags & NedUser.DATABASE_PRESENT) != 0)
             {
                 deleted.flags |= NedUser.CHANGED;
                 deleted.flags |= NedUser.DELETE;
             } else{
                 listUsers.remove(deleted);
             }
             gridUsers.removeRow(index + 1);
             blockNavigation = true;
         }
     }
     
     private int findIndex(String username, boolean includeDeleted) {
         int retval = -1;
         boolean found = false;
         for (int i = 0; i < listUsers.size() && !found; i++) {
             if (includeDeleted || (listUsers.get(i).flags & NedUser.DELETE) == 0) {
                 retval++;
                 if (listUsers.get(i).username.equals(username)) {
                     found = true;
                 }
             }
         }
         return found ? retval : -1;
     }
     
     private int actualIndex(int index)
     {
         int retval = index;
         for (int i = 0; i < listUsers.size(); i++) {
             if ((listUsers.get(i).flags & NedUser.DELETE) != 0) {
                 retval++;
             }
         }
         return retval;
     }
 
     private class ClickHandlerSaveModifications implements ClickHandler {
 
         @Override
         public void onClick(ClickEvent event) {
             saveUsers();
         }
     }
     
     private void saveUsers() {
         List<NedUser> changedUsers = getChangedUsersList();
 
         if(changedUsers.size() > 0)
         {
             updateUsers(changedUsers);
         } else {
             NedAlert.showAlert(NedRes.instance().msgNoChanges());
         }
     }
 
     private void updateUsers(List<NedUser> changedUsers){
         if( changedUsers!= null && changedUsers.size() > 0){
             NedCatalogServiceAsync service = (NedCatalogServiceAsync) GWT
             .create(NedCatalogService.class);
             ServiceDefTarget serviceDef = (ServiceDefTarget) service;
             serviceDef.setServiceEntryPoint("NedCatalogServlet");
             NedUpdateUsersCallback serviceCallback = new NedUpdateUsersCallback((NedUserListUpdater)NedUserManagementWidget.this);
             service.updateUsers(changedUsers, serviceCallback);
         }
     }
     
     private List<NedUser> getChangedUsersList(){
         List<NedUser> changedUsers = new LinkedList<NedUser>();
         for(int i = 0 ; i < listUsers.size(); i++)
         {
             if((listUsers.get(i).flags & NedUser.CHANGED) != 0)
             {
                 changedUsers.add(listUsers.get(i));
             }
         }
         
         return changedUsers;
     }
 
     @Override
     public void receiveUsername(String received) {
         int lastIndex = gridUsers.getRowCount();
         gridUsers.insertRow(lastIndex);
         String password = new NedStringGenerator().nextString(8);
         addUserToList(received, password);
         fillUserRow(lastIndex, received, password);
         blockNavigation = true;
     }
 
     private void addUserToList(String username, String password) {
         listUsers.add(new NedUser(username, password, NedUser.CHANGED));
     }
 
     @Override
     public void refreshUserList() {
         
         for(int i = 0; i < listUsers.size(); i++)
         {
             if((listUsers.get(i).flags & NedUser.DELETE) != 0){
                 listUsers.remove(i);
                 i--;
             } else{
                 listUsers.get(i).flags = NedUser.DATABASE_PRESENT;
             }
         }
         blockNavigation = false;
         
     }
 
     @Override
     public void receivePassword(String user, String passowrd) {
         int index = findIndex(user, false);
         if (index >= 0) {
             ((TextBox) gridUsers.getWidget(index+1, 1)).setText("********");
             listUsers.get(actualIndex(index)).flags |= NedUser.CHANGED;
             listUsers.get(actualIndex(index)).password = passowrd;
         }
     }
 
     public boolean isPersisted() {
             List<NedUser> changedUsers = getChangedUsersList();
             return changedUsers == null || changedUsers.size() == 0;
     }
 
     public void tryPersist(int nextIndex, TabPanel tabPanelMain) {
             NedAlert.showConfirmYesNoCancel(NedRes.instance().userDlgChangesNotSaved(), new TryPersistCallback(nextIndex, tabPanelMain));
     }
 
     private class TryPersistCallback implements ConfirmCallback 
     {
         private TabPanel tabPanelMain;
         private int nextIndex;
 
         TryPersistCallback(int nextIndex, TabPanel tabPanelMain)
         {
         this.nextIndex = nextIndex;
         this.tabPanelMain = tabPanelMain;
         }
 
         @Override
         public void onYes() {
             saveUsers();
         }
 
         @Override
         public void onNo() {
             blockNavigation  = false;
             Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                 @Override
                 public void execute() {
                     tabPanelMain.selectTab(nextIndex);
                 }
             });
         }
 
         @Override
         public void onCancel() {
         }
         
     }
 
     public boolean isBlocked() {
         return blockNavigation;
     }
 
     public void refreshBlockNavigation() {
         blockNavigation = !isPersisted();
         
     }
 
 }
