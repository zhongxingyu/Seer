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
 package org.ned.server.nedadminconsole.client.dialogs;
 
 import org.ned.server.nedadminconsole.client.NedCatalogService;
 import org.ned.server.nedadminconsole.client.NedCatalogServiceAsync;
 import org.ned.server.nedadminconsole.client.NedRes;
 import org.ned.server.nedadminconsole.client.NedStringGenerator;
 import org.ned.server.nedadminconsole.client.callbacks.NedAddNewElementCallback;
 import org.ned.server.nedadminconsole.client.callbacks.NedCheckIdCallback;
 import org.ned.server.nedadminconsole.client.interfaces.NedLibraryListUpdater;
 import org.ned.server.nedadminconsole.shared.NedObject;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 
 public class NedNewElementDialog extends DialogBox {
 
     private String type;
     private String parentId;
     private TextBox textBoxId;
     private TextBox textBoxName;
     private TreeItem parentTreeItem;
     private Label labelIdResult;
    private final String idRegExp = "^[[a-z][A-Z][0-9]]*$";
 
     public NedNewElementDialog(String type, String parentId,
             TreeItem parentTreeItem) {
         this(type, parentId, parentTreeItem, (NedLibraryListUpdater) null);
     }
 
     /**
      * @wbp.parser.constructor
      */
     public NedNewElementDialog(String type, String parentId,
             TreeItem parentTreeItem, NedLibraryListUpdater libraryUpdater) {
         this.type = type;
         this.parentId = parentId;
         this.parentTreeItem = parentTreeItem;
         setText(NedRes.instance().newElemDlgAddNew() + " " + type);
 
         VerticalPanel verticalPanel = new VerticalPanel();
         verticalPanel.setSpacing(8);
         verticalPanel
                 .setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
         setWidget(verticalPanel);
         verticalPanel.setSize("335px", "100%");
 
         Grid grid = new Grid(3, 3);
         verticalPanel.add(grid);
         verticalPanel.setCellWidth(grid, "25%");
         grid.setSize("100%", "100%");
 
         Label lblId = new Label(NedRes.instance().id() + ":");
         grid.setWidget(0, 0, lblId);
         grid.getCellFormatter().setWidth(0, 0, "");
 
         textBoxId = new TextBox();
         textBoxId.setMaxLength(20);
         grid.setWidget(0, 1, textBoxId);
         grid.getCellFormatter().setWidth(0, 1, "");
         textBoxId.setWidth("96%");
 
         Button buttonRandomize = new Button("New button");
         buttonRandomize.addClickHandler(new ClickHandlerRandomize());
         grid.setWidget(0, 2, buttonRandomize);
         buttonRandomize.setWidth("100%");
         grid.getCellFormatter().setWidth(0, 2, "40%");
         buttonRandomize.setText(NedRes.instance().newElemDlgRandomize());
 
         Button buttonCheck = new Button("New button");
         buttonCheck.addClickHandler(new ClickHandlerCheck());
         grid.setWidget(1, 1, buttonCheck);
         grid.getCellFormatter().setWidth(1, 1, "");
         buttonCheck.setWidth("100%");
         buttonCheck.setText(NedRes.instance().newElemDlgCheck());
 
         labelIdResult = new Label("");
         labelIdResult.setWordWrap(false);
         labelIdResult.setStyleName("gwt-Label-red");
         grid.setWidget(1, 2, labelIdResult);
         grid.getCellFormatter().setWidth(1, 2, "25%");
         grid.getCellFormatter().setWidth(2, 1, "");
 
         Label lblName = new Label(NedRes.instance().name() + ":");
         grid.setWidget(2, 0, lblName);
         grid.getCellFormatter().setWidth(2, 0, "");
         lblName.setWidth("");
 
         textBoxName = new TextBox();
         textBoxName.setMaxLength(20);
         grid.setWidget(2, 1, textBoxName);
         textBoxName.setWidth("96%");
 
         grid.getCellFormatter().setVerticalAlignment(0, 1,
                 HasVerticalAlignment.ALIGN_BOTTOM);
         grid.getCellFormatter().setVerticalAlignment(0, 2,
                 HasVerticalAlignment.ALIGN_BOTTOM);
         grid.getCellFormatter().setHorizontalAlignment(0, 2,
                 HasHorizontalAlignment.ALIGN_LEFT);
         grid.getCellFormatter().setVerticalAlignment(1, 1,
                 HasVerticalAlignment.ALIGN_TOP);
         grid.getCellFormatter().setVerticalAlignment(1, 2,
                 HasVerticalAlignment.ALIGN_MIDDLE);
         grid.getCellFormatter().setHorizontalAlignment(0, 0,
                 HasHorizontalAlignment.ALIGN_LEFT);
         grid.getCellFormatter().setHorizontalAlignment(2, 0,
                 HasHorizontalAlignment.ALIGN_LEFT);
 
         HorizontalPanel horizontalPanel = new HorizontalPanel();
         verticalPanel.add(horizontalPanel);
         verticalPanel.setCellWidth(horizontalPanel, "100%");
         horizontalPanel.setWidth("100%");
 
         Button buttonOk = new Button();
         buttonOk.addClickHandler(new ClickHandlerOk(libraryUpdater));
         horizontalPanel.add(buttonOk);
         horizontalPanel.setCellWidth(buttonOk, "50%");
         buttonOk.setText(NedRes.instance().ok());
         buttonOk.setWidth("100%");
 
         Button buttonCancel = new Button();
         buttonCancel.addClickHandler(new ClickHandlerCancel());
         horizontalPanel.add(buttonCancel);
         horizontalPanel.setCellWidth(buttonCancel, "50%");
         buttonCancel.setText(NedRes.instance().cancel());
         buttonCancel.setWidth("100%");
 
         center();
         textBoxName.setFocus(true);
     }
 
     private class ClickHandlerCancel implements ClickHandler {
         @Override
         public void onClick(ClickEvent event) {
             NedNewElementDialog.this.hide();
         }
 
     }
 
     private class ClickHandlerOk implements ClickHandler {
         private NedLibraryListUpdater libraryUpdater;
         public ClickHandlerOk(NedLibraryListUpdater libraryUpdater) {
             this.libraryUpdater = libraryUpdater;
         }
 
         @Override
         public void onClick(ClickEvent event) {
             String itemName = textBoxName.getText();
             if (itemName != null && !itemName.trim().isEmpty()) {
                 NedCatalogServiceAsync service = (NedCatalogServiceAsync) GWT
                         .create(NedCatalogService.class);
                 ServiceDefTarget serviceDef = (ServiceDefTarget) service;
                 serviceDef.setServiceEntryPoint("NedCatalogServlet");
                 if (type.equals("Media Item")) {
                     type = "Undefined";
                 } else if (type.equals("Library")) {
                     parentId = null;
                 }
                 String id = textBoxId.getText();
                 if (id == null || id.trim().trim().isEmpty()) {
                     id = new NedStringGenerator().nextString();
                     textBoxId.setText(id);
                 }
                 NedObject newObject = new NedObject(id, parentId,
                         itemName.trim(), type, null, null, null, null);
                 NedAddNewElementCallback serviceCallback = new NedAddNewElementCallback(
                         NedNewElementDialog.this, newObject, parentTreeItem,
                         libraryUpdater);
                 service.addNewItem(newObject, serviceCallback);
             } else {
                 NedAlert.showAlert(NedRes.instance().msgErrorEmptyName());
             }
         }
     }
 
     private class ClickHandlerRandomize implements ClickHandler {
         @Override
         public void onClick(ClickEvent event) {
             textBoxId.setText(new NedStringGenerator().nextString());
         }
     }
 
     private class ClickHandlerCheck implements ClickHandler {
 
         @Override
         public void onClick(ClickEvent event) {
             if (!textBoxId.getText().isEmpty()) {
                 labelIdResult.setText("");
                if (!textBoxId.getText().matches(idRegExp)) {
                     labelIdResult.setText(NedRes.instance()
                             .newElemDlgIdIllegal());
                 } else {
                     NedCatalogServiceAsync service = (NedCatalogServiceAsync) GWT
                             .create(NedCatalogService.class);
                     ServiceDefTarget serviceDef = (ServiceDefTarget) service;
                     serviceDef.setServiceEntryPoint("NedCatalogServlet");
                     NedCheckIdCallback serviceCallback = new NedCheckIdCallback(
                             labelIdResult);
                     service.checkIdAvailable(textBoxId.getText(),
                             serviceCallback);
                 }
             } else {
                 labelIdResult.setText(NedRes.instance().newElemDlgIdIsEmpty());
             }
         }
 
     }
 
 }
