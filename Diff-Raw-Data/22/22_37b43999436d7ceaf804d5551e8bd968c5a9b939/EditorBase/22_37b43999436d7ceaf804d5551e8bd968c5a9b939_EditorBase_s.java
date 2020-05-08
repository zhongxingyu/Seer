 /*
  * This library is part of the Acacia Editor -
  * an open source inline and form based content editor for GWT.
  *
  * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * For further information about Alkacon Software, please see the
  * company website: http://www.alkacon.com
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package com.alkacon.acacia.client;
 
 import com.alkacon.acacia.client.widgets.FormWidgetWrapper;
 import com.alkacon.acacia.client.widgets.HalloWidget;
 import com.alkacon.acacia.client.widgets.I_EditWidget;
 import com.alkacon.acacia.client.widgets.I_FormEditWidget;
 import com.alkacon.acacia.client.widgets.StringWidget;
 import com.alkacon.acacia.shared.ContentDefinition;
 import com.alkacon.acacia.shared.TabInfo;
 import com.alkacon.acacia.shared.ValidationResult;
 import com.alkacon.acacia.shared.rpc.I_ContentServiceAsync;
 import com.alkacon.geranium.client.ui.TabbedPanel;
 import com.alkacon.geranium.client.ui.css.I_ImageBundle;
 import com.alkacon.geranium.client.ui.css.I_LayoutBundle;
 import com.alkacon.vie.client.Entity;
 import com.alkacon.vie.client.I_Vie;
 import com.alkacon.vie.client.Vie;
 import com.alkacon.vie.shared.I_Entity;
 import com.alkacon.vie.shared.I_Type;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.i18n.client.Dictionary;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Panel;
 
 /**
  * The content editor base.<p>
  */
 public class EditorBase {
 
     /** Message constant for key in the resource bundle. */
     public static String GUI_CHOICE_ADD_CHOICE_0 = "GUI_CHOICE_ADD_CHOICE_0"; //Add choice
 
     /** Message constant for key in the resource bundle. */
     public static String GUI_VIEW_ADD_0 = "GUI_VIEW_ADD_0"; //Add
 
     /** Message constant for key in the resource bundle. */
     public static String GUI_VIEW_CLOSE_0 = "GUI_VIEW_CLOSE_0"; //Close
 
     /** Message constant for key in the resource bundle. */
     public static String GUI_VIEW_DELETE_0 = "GUI_VIEW_DELETE_0"; //Delete
 
     /** Message constant for key in the resource bundle. */
     public static String GUI_VIEW_MOVE_0 = "GUI_VIEW_MOVE_0"; //Move
 
     /** The localized dictionary. */
     private static Dictionary m_dictionary;
 
     /** The VIE instance. */
     protected I_Vie m_vie;
 
     /** The content service instance. */
     private I_ContentServiceAsync m_service;
 
     /** The validation handler. */
     private ValidationHandler m_validationHandler;
 
     /** The widget service. */
     private WidgetService m_widgetService;
 
     /**
      * Constructor.<p>
      * 
      * @param service the content service 
      */
     public EditorBase(I_ContentServiceAsync service) {
 
         I_ImageBundle.INSTANCE.style().ensureInjected();
         I_LayoutBundle.INSTANCE.generalCss().ensureInjected();
         I_LayoutBundle.INSTANCE.buttonCss().ensureInjected();
         I_LayoutBundle.INSTANCE.highlightCss().ensureInjected();
         I_LayoutBundle.INSTANCE.tabbedPanelCss().ensureInjected();
         I_LayoutBundle.INSTANCE.dialogCss().ensureInjected();
         com.alkacon.acacia.client.css.I_LayoutBundle.INSTANCE.form().ensureInjected();
         com.alkacon.acacia.client.css.I_LayoutBundle.INSTANCE.attributeChoice().ensureInjected();
         m_service = service;
         m_vie = Vie.getInstance();
         m_widgetService = new WidgetService();
         I_EntityRenderer renderer = new Renderer(m_vie, m_widgetService);
         m_widgetService.setDefaultRenderer(renderer);
         m_widgetService.addWidgetFactory("string", new I_WidgetFactory() {
 
             public I_FormEditWidget createFormWidget(String configuration) {
 
                 return new FormWidgetWrapper(new StringWidget());
             }
 
             public I_EditWidget createInlineWidget(String configuration, Element element) {
 
                 return new StringWidget(element);
             }
         });
         m_widgetService.addWidgetFactory("html", new I_WidgetFactory() {
 
             public I_FormEditWidget createFormWidget(String configuration) {
 
                 return new FormWidgetWrapper(new HalloWidget());
             }
 
             public I_EditWidget createInlineWidget(String configuration, Element element) {
 
                 return new HalloWidget(element, null);
             }
         });
         m_validationHandler = new ValidationHandler();
         m_validationHandler.setContentService(m_service);
     }
 
     /**
      * Returns the m_dictionary.<p>
      *
      * @return the m_dictionary
      */
     public static Dictionary getDictionary() {
 
         return m_dictionary;
     }
 
     /**
      * Sets the m_dictionary.<p>
      *
      * @param dictionary the m_dictionary to set
      */
     public static void setDictionary(Dictionary dictionary) {
 
         m_dictionary = dictionary;
     }
 
     /**
      * Adds the value change handler to the entity with the given id.<p>
      * 
      * @param entityId the entity id
      * @param handler the change handler
      */
     public void addEntityChangeHandler(String entityId, ValueChangeHandler<I_Entity> handler) {
 
         I_Entity entity = m_vie.getEntity(entityId);
         if (entity != null) {
             ((Entity)entity).addValueChangeHandler(handler);
         }
     }
 
     /**
      * Adds a validation change handler.<p>
      * 
      * @param handler the validation change handler
      * 
      * @return the handler registration
      */
     public HandlerRegistration addValidationChangeHandler(ValueChangeHandler<ValidationContext> handler) {
 
         return m_validationHandler.addValueChangeHandler(handler);
     }
 
     /**
      * Destroys the form and related resources. Also clears all entities from VIE<p>
      * 
      * @param clearEntities <code>true</code> to also clear all entities
      */
     public void destroyFrom(boolean clearEntities) {
 
         HighlightingHandler.getInstance().destroy();
         if (clearEntities) {
             m_vie.clearEntities();
         }
     }
 
     /**
      * Returns the content service instance.<p>
      * 
      * @return the content service
      */
     public I_ContentServiceAsync getService() {
 
         return m_service;
     }
 
     /**
      * Loads the content definition for the given entity and executes the callback on success.<p>
      * 
      * @param entityId the entity id
      * @param callback the callback
      */
     public void loadContentDefinition(final String entityId, final Command callback) {
 
         AsyncCallback<ContentDefinition> asyncCallback = new AsyncCallback<ContentDefinition>() {
 
             public void onFailure(Throwable caught) {
 
                 onRpcError(caught);
             }
 
             public void onSuccess(ContentDefinition result) {
 
                 registerContentDefinition(result);
                 callback.execute();
             }
         };
         getService().loadContentDefinition(entityId, asyncCallback);
     }
 
     /**
      * Registers the types and entities of the given content definition.<p>
      * 
      * @param definition the content definition
      */
     public void registerContentDefinition(ContentDefinition definition) {
 
         m_widgetService.addConfigurations(definition.getConfigurations());
         I_Type baseType = definition.getTypes().get(definition.getEntityTypeName());
         m_vie.registerTypes(baseType, definition.getTypes());
         m_vie.registerTypes(baseType, definition.getTypes());
         m_vie.registerEntity(definition.getEntity());
     }
 
     /**
      * Renders the entity form within the given context.<p>
      * 
      * @param entityId the entity id
      * @param tabInfos the tab informations
      * @param context the context element
      * @param scrollParent the scroll element to be used for automatic scrolling during drag and drop
 
      */
     public void renderEntityForm(String entityId, List<TabInfo> tabInfos, Panel context, Element scrollParent) {
 
         I_Entity entity = m_vie.getEntity(entityId);
         if (entity != null) {
             I_Type type = m_vie.getType(entity.getTypeName());
            FlowPanel formPanel = new FlowPanel();
             context.add(formPanel);
             AttributeHandler.setScrollElement(scrollParent);
             RootHandler rootHandler = new RootHandler();
             TabbedPanel<?> formTabs = m_widgetService.getRendererForType(type).renderForm(
                 entity,
                 tabInfos,
                 formPanel,
                 rootHandler,
                 0);
             m_validationHandler.registerEntity(entity);
             m_validationHandler.setRootHandler(rootHandler);
             m_validationHandler.setFormTabPanel(formTabs);
         }
     }
 
     /**
      * Renders the entity form within the given context.<p>
      * 
      * @param entityId the entity id
      * @param context the context element
      * @param scrollParent the scroll element to be used for automatic scrolling during drag and drop
      */
     public void renderEntityForm(String entityId, Panel context, Element scrollParent) {
 
         I_Entity entity = m_vie.getEntity(entityId);
         if (entity != null) {
             I_Type type = m_vie.getType(entity.getTypeName());
            FlowPanel formPanel = new FlowPanel();
             context.add(formPanel);
             AttributeHandler.setScrollElement(scrollParent);
             RootHandler rootHandler = new RootHandler();
             m_widgetService.getRendererForType(type).renderForm(entity, formPanel, rootHandler, 0);
             m_validationHandler.setContentService(m_service);
             m_validationHandler.registerEntity(entity);
             m_validationHandler.setRootHandler(rootHandler);
         }
     }
 
     /**
      * Renders the entity form within the given context.<p>
      * 
      * @param entityId the entity id
      * @param context the context element
      */
     public void renderInlineEntity(String entityId, Element context) {
 
         I_Entity entity = m_vie.getEntity(entityId);
         if (entity != null) {
             I_Type type = m_vie.getType(entity.getTypeName());
             m_widgetService.getRendererForType(type).renderInline(entity, context);
         }
     }
 
     /**
      * Renders the entity form within the given context.<p>
      * 
      * @param entityId the entity id
      * @param formParent the form parent widget
      */
     public void renderInlineEntity(String entityId, I_InlineFormParent formParent) {
 
         I_Entity entity = m_vie.getEntity(entityId);
         if (entity != null) {
             I_Type type = m_vie.getType(entity.getTypeName());
             m_widgetService.getRendererForType(type).renderInline(entity, formParent);
         }
     }
 
     /**
      * Saves the given entities.<p>
      * 
      * @param entities the entities to save
      * @param clearOnSuccess <code>true</code> to clear the VIE instance on success
      * @param callback the call back command
      */
     public void saveEntities(
         List<com.alkacon.acacia.shared.Entity> entities,
         final boolean clearOnSuccess,
         final Command callback) {
 
         AsyncCallback<ValidationResult> asyncCallback = new AsyncCallback<ValidationResult>() {
 
             public void onFailure(Throwable caught) {
 
                 onRpcError(caught);
             }
 
             public void onSuccess(ValidationResult result) {
 
                 callback.execute();
                 if ((result != null) && result.hasErrors()) {
                     //   ValidationHandler.getInstance().displayErrors(null, result)
                 }
                 if (clearOnSuccess) {
                     destroyFrom(true);
                 }
             }
         };
         getService().saveEntities(entities, asyncCallback);
     }
 
     /**
      * Saves the given entity.<p>
      * 
      * @param entityIds the entity ids
      * @param clearOnSuccess <code>true</code> to clear all entities from VIE on success
      * @param callback the callback executed on success
      */
     public void saveEntities(Set<String> entityIds, boolean clearOnSuccess, Command callback) {
 
         List<com.alkacon.acacia.shared.Entity> entities = new ArrayList<com.alkacon.acacia.shared.Entity>();
         for (String entityId : entityIds) {
             I_Entity entity = m_vie.getEntity(entityId);
             if (entity != null) {
                 entities.add(com.alkacon.acacia.shared.Entity.serializeEntity(entity));
             }
         }
         saveEntities(entities, clearOnSuccess, callback);
     }
 
     /**
      * Saves the given entity.<p>
      * 
      * @param entity the entity
      * @param clearOnSuccess <code>true</code> to clear all entities from VIE on success
      * @param callback the callback executed on success
      */
     public void saveEntity(I_Entity entity, final boolean clearOnSuccess, final Command callback) {
 
         AsyncCallback<ValidationResult> asyncCallback = new AsyncCallback<ValidationResult>() {
 
             public void onFailure(Throwable caught) {
 
                 onRpcError(caught);
             }
 
             public void onSuccess(ValidationResult result) {
 
                 callback.execute();
                 if (clearOnSuccess) {
                     destroyFrom(true);
                 }
             }
         };
         getService().saveEntity(com.alkacon.acacia.shared.Entity.serializeEntity(entity), asyncCallback);
     }
 
     /**
      * Saves the given entity.<p>
      * 
      * @param entityId the entity id
      * @param clearOnSuccess <code>true</code> to clear all entities from VIE on success
      * @param callback the callback executed on success
      */
     public void saveEntity(String entityId, boolean clearOnSuccess, Command callback) {
 
         I_Entity entity = m_vie.getEntity(entityId);
         saveEntity(entity, clearOnSuccess, callback);
     }
 
     /**
      * Saves the given entity.<p>
      * 
      * @param entityId the entity id
      * @param callback the callback executed on success
      */
     public void saveEntity(String entityId, Command callback) {
 
         I_Entity entity = m_vie.getEntity(entityId);
         saveEntity(entity, false, callback);
     }
 
     /**
      * Returns the validation handler.<p>
      * 
      * @return the validation handler
      */
     protected ValidationHandler getValidationHandler() {
 
         return m_validationHandler;
     }
 
     /**
      * Returns the widget service.<p>
      * 
      * @return the widget service
      */
     protected I_WidgetService getWidgetService() {
 
         return m_widgetService;
     }
 
     /**
      * Handles RPC errors.<p>
      * 
      * Override this for better error handling
      * 
      * @param caught the error caught from the RPC
      */
     protected void onRpcError(Throwable caught) {
 
         // doing nothing
     }
 }
