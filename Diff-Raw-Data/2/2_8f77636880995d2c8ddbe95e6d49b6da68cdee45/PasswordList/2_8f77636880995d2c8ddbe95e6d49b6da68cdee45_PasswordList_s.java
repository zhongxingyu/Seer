 /*
  * Copyright 2010 Google Inc.
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
  */
 
 package com.intradev.cerberus.web.client.screens;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.Widget;
 import com.intradev.cerberus.allshared.CerberusProtocol;
 import com.intradev.cerberus.allshared.JsonRpcClient;
 import com.intradev.cerberus.allshared.JsonRpcException;
 import com.intradev.cerberus.web.client.CerberusWeb;
 import com.intradev.cerberus.web.client.Screen;
 import com.intradev.cerberus.web.client.code.EncodedPassword;
 import com.intradev.cerberus.web.client.controls.PasswordItem;
 
 /**
  * The passwords list screen.
  */
 public class PasswordList extends Screen {
 
     private static PasswordsListUiBinder uiBinder = GWT.create(PasswordsListUiBinder.class);
 
     interface PasswordsListUiBinder extends UiBinder<Widget, PasswordList> {
     }
     
 
     @UiField
     FlowPanel passwordList;
 
     @UiField
     PushButton createButton;
     
     /*private CerberusWeb masterInstance;*/
 
     public PasswordList(CerberusWeb instance) {
         initWidget(uiBinder.createAndBindUi(this));
         /*masterInstance = instance;*/
         refreshPasswords();
     }
 
     @UiHandler("createButton")
     void onClick(ClickEvent e) {
         History.newItem("password");
     }
     
     public void refreshPasswords() {
     	passwordList.clear();
         if (CerberusWeb.sPasswords.keySet().isEmpty()) {
             Label emptyLabel = new Label();
             emptyLabel.setStyleName("empty");
             emptyLabel.setText("You haven't written any passwords, create one by clicking " +
                     "'Create Password' below!");
             passwordList.add(emptyLabel);
         } else {
             List<EncodedPassword> passwords = new ArrayList<EncodedPassword>();
             for (String id : CerberusWeb.sPasswords.keySet()) {
             	EncodedPassword password = CerberusWeb.sPasswords.get(id);
                 passwords.add(password);
             }
 
             Collections.sort(passwords, new Comparator<EncodedPassword>() {
                 public int compare(EncodedPassword o1, EncodedPassword o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                 }
             });
 
             for (EncodedPassword password : passwords) {
                 PasswordItem itemWidget = new PasswordItem(password, mPasswordItemActionCallback);
                 passwordList.add(itemWidget);
             }
         }
     }
 
     @Override
     public Screen fillOrReplace(List<String> args) {
         refreshPasswords();
         return this;
     }
 
     private PasswordItem.ActionCallback mPasswordItemActionCallback = new PasswordItem.ActionCallback() {
         public void onEdit(String passwordId) {
             History.newItem("password/" + passwordId);
         }
 
         public void onDelete(final String passwordId) {
             JSONObject paramsJson = new JSONObject();
             paramsJson.put(CerberusProtocol.PasswordsDelete.ARG_ID, new JSONString(passwordId));
             CerberusWeb.sJsonRpcClient.call(CerberusProtocol.PasswordsDelete.METHOD, paramsJson, new JsonRpcClient.Callback() {
                 public void onSuccess(Object data) {
                     EncodedPassword password = CerberusWeb.sPasswords.get(passwordId);
                     CerberusWeb.sPasswords.remove(passwordId);
                     CerberusWeb.showMessage("Deleted password '" + password.getTitle() + "'", true);
                     refreshPasswords();
                 }
 
                 public void onError(JsonRpcException caught) {
                     CerberusWeb.showMessage("Delete failed: " + caught.getMessage(), false);
                 }
             });
         }
     };
 
    
 }
