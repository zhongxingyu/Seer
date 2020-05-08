 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.web.controller.topictype;
 
 import org.jtalks.poulpe.model.entity.TopicType;
 import org.jtalks.poulpe.service.TopicTypeService;
 import org.jtalks.common.service.exceptions.NotFoundException;
 import org.jtalks.poulpe.service.exceptions.NotUniqueException;
 import org.jtalks.poulpe.web.controller.DialogManager;
 import org.jtalks.poulpe.web.controller.EditListener;
 import org.jtalks.poulpe.web.controller.WindowManager;
 
 /**
  * Presenter of TopicType list page.
  * @author Pavel Vervenko
  * @author Vahluev Vyacheslav
  */
 public class TopicTypePresenter {
 	    
     public interface TopicTypeView {
 
         void showTypeTitle(String title);
 
         void showTypeDescription(String description);
 
         String getTypeTitle();
         
         String getTypeDescription();
         
         void hideEditAction();
         
         void hideCreateAction();
         
         void openErrorPopupInTopicTypeDialog(String label);
     }
 
     /**
      * Save and init view.
      * @param view view
      */
     public void initView(TopicTypeView view) {
         this.view = view;
     }
     
     public static final String ERROR_TOPICTYPE_TITLE_CANT_BE_VOID = "topictypes.error.topictype_name_cant_be_void";
     public static final String ERROR_TOPICTYPE_TITLE_ALREADY_EXISTS = "topictypes.error.topictype_name_already_exists";
     public static final String ERROR_TOPICTYPE_TITLE_DOESNT_EXISTS = "topictypes.error.topictype_name_doesnt_exists";
     
     protected DialogManager dialogManager;
     protected WindowManager windowManager;
     protected TopicTypeService topicTypeService;
     protected TopicTypeView view;
     protected TopicType topicType;
     protected EditListener<TopicType> listener;
 
     /**
      * Set the TopicTypeService implementation.
      * @param topicTypeService impl of TopicTypeService
      */
     public void setTopicTypeService(TopicTypeService topicTypeService) {
         this.topicTypeService = topicTypeService;
     }
     
     public void setDialogManager(DialogManager dialogManager) {
         this.dialogManager = dialogManager;
     }
     
     public void setWindowManager(WindowManager windowManager) {
         this.windowManager = windowManager;
     }
     
     public void setListener(EditListener<TopicType> listener) {
         this.listener = listener;
     }
     
     public void initializeForCreate(TopicTypeView view, EditListener<TopicType> listener) {
         this.view = view;
         this.listener = listener;
         view.hideEditAction();
         this.topicType = new TopicType();
     }
 
     public void initializeForEdit(TopicTypeView view, TopicType topicType, EditListener<TopicType> listener) {
         this.view = view;
         this.listener = listener;
         view.hideCreateAction();
         try {        	
             this.topicType = topicTypeService.get(topicType.getId());            
         } catch (NotFoundException e) {       	        	
         	view.openErrorPopupInTopicTypeDialog(ERROR_TOPICTYPE_TITLE_DOESNT_EXISTS);
             return;
         }
         view.showTypeTitle(this.topicType.getTitle());
         view.showTypeDescription(this.topicType.getDescription());
     }
     
     public void onTitleLoseFocus() {
         String title = view.getTypeTitle();
         if (topicTypeService.isTopicTypeNameExists(title, topicType.getId())) {
         	view.openErrorPopupInTopicTypeDialog(ERROR_TOPICTYPE_TITLE_ALREADY_EXISTS);
         }    	
     }
     
     public void onCreateAction() {
         if (save()) {
             closeView();
             listener.onCreate(topicType);
         }
     }
     
     public void onUpdateAction() {
         if (update()) {
             closeView();
             listener.onUpdate(topicType);
         }
     }
     
     public void onCancelEditAction() {
         closeView();
         listener.onCloseEditorWithoutChanges();
     }
     
     public void closeView() {
         windowManager.closeWindow(view);
     }
 
     public boolean save() {    	
         topicType.setTitle(view.getTypeTitle());
         topicType.setDescription(view.getTypeDescription());        
         try {
         	String errorLabel = validateTopicType(topicType);
         	if (errorLabel != null) {
         		view.openErrorPopupInTopicTypeDialog(errorLabel);
         		return false;
         	}
             topicTypeService.saveTopicType(topicType);            
             return true;
         } catch (NotUniqueException e) {
         	view.openErrorPopupInTopicTypeDialog(ERROR_TOPICTYPE_TITLE_ALREADY_EXISTS);
             return false;
         }
     }
     
     public boolean update() {
     	topicType.setTitle(view.getTypeTitle());
         topicType.setDescription(view.getTypeDescription());
         try {
         	String errorLabel = validateTopicType(topicType);        	
         	if (errorLabel != null) {
         		view.openErrorPopupInTopicTypeDialog(errorLabel);
         		return false;
         	}
             topicTypeService.updateTopicType(topicType);
             return true;
         } catch (NotUniqueException e) {
         	view.openErrorPopupInTopicTypeDialog(ERROR_TOPICTYPE_TITLE_ALREADY_EXISTS);
             return false;
         }
     }    
 
     /**
      * 
      * @param topicType topicType we want to validate
      * @return null if TopicType has a valid title or error message otherwise 
      */
     public String validateTopicType(TopicType topicType) {
         if (topicType.getTitle() == null || topicType.getTitle().equals("")) {
             return ERROR_TOPICTYPE_TITLE_CANT_BE_VOID;
         }
         return null;
     }
 }
