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
 package org.jtalks.poulpe.web.controller.component;
 
 import com.google.common.annotations.VisibleForTesting;
 import org.apache.commons.lang3.Validate;
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.model.entity.ComponentType;
 import org.jtalks.poulpe.service.ComponentService;
 import org.jtalks.poulpe.service.JcommuneHttpNotifier;
 import org.jtalks.poulpe.service.exceptions.JcommuneRespondedWithErrorException;
 import org.jtalks.poulpe.service.exceptions.JcommuneUrlNotConfiguratedException;
 import org.jtalks.poulpe.service.exceptions.NoConnectionToJcommuneException;
 import org.jtalks.poulpe.web.controller.DialogManager;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
 import org.jtalks.poulpe.web.controller.zkutils.BindUtilsWrapper;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zul.Messagebox;
 
 /**
  * Adding, removing, editing and configuring of components.
  *
  * @author Alexey Grigorev
  * @author Leonid Kazantcev
  */
 public class ComponentsVm {
 
     public static final String SELECTED = "selected";
     public static final String CAN_CREATE_NEW_COMPONENT = "ableToCreateNewComponent";
     public static final String COMPONENTS = "components";
     public static final String SHOW_NOT_CONNECTED_NOTIFICATION = "showNotConnectedNotification";
     public static final String SHOW_NOT_CONFIGURED_NOTIFICATION = "showNotConfiguredNotification";
     public static final String JCOMMUNE = "jcommune";
     public static final String POULPE = "poulpe";
     public static final String JCOMMUNE_VISIBLE = "jcommuneVisible";
     public static final String POULPE_VISIBLE = "poulpeVisible";
 
     private static final String DEFAULT_NAME = "name";
     private static final String DEFAULT_DESCRIPTION = "descr";
     public static final String COMPONENTS_PAGE_LOCATION = "/WEB-INF/pages/component/components.zul";
     private boolean showNotConnectedNotification;
     private boolean showNotConfiguredNotification;
     private final ComponentService componentService;
     private final DialogManager dialogManager;
     private final WindowManager windowManager;
     private final SelectedEntity<Component> selectedEntity;
     private final JcommuneHttpNotifier jcommuneHttpNotifier;
 
     private static final String JCOMMUNE_CONNECTION_FAILED = "component.error.jcommune_no_connection";
     private static final String JCOMMUNE_RESPONSE_FAILED = "component.error.jcommune_no_response";
     private static final String JCOMMUNE_URL_FAILED = "component.error.jcommune_no_url";
     private static final String COMPONENT_DELETING_FAILED_DIALOG_TITLE = "component.deleting_problem_dialog.title";
 
     private BindUtilsWrapper bindWrapper = new BindUtilsWrapper();
 
     private Component selected;
 
     /**
      * @param componentService     service for loading and saving component
      * @param dialogManager        shows confirmation dialog for deletion
      * @param windowManager        object for opening and closing application windows
      * @param selectedEntity       desktop-scoped bean to which selected entities passed, used for editing components
      * @param jcommuneHttpNotifier instance of {@link JcommuneHttpNotifier}
      */
     public ComponentsVm(ComponentService componentService, DialogManager dialogManager, WindowManager windowManager,
                         SelectedEntity<Component> selectedEntity, JcommuneHttpNotifier jcommuneHttpNotifier) {
         this.componentService = componentService;
         this.dialogManager = dialogManager;
         this.windowManager = windowManager;
         this.selectedEntity = selectedEntity;
         this.jcommuneHttpNotifier = jcommuneHttpNotifier;
     }
 
     /**
      * @return {@link Component} with type ADMIN_PANEL if it exists, null otherwise
      */
     public Component getPoulpe() {
         return componentService.getByType(ComponentType.ADMIN_PANEL);
     }
 
     /**
      * @return true if ADMIN_PANEL component is exists, false otherwise
      */
     public boolean isPoulpeVisible() {
         return !(getPoulpe() == null);
     }
 
     /**
      * @return {@link Component} with type FORUM if it exists, null otherwise
      */
     public Component getJcommune() {
         return componentService.getByType(ComponentType.FORUM);
     }
 
     /**
      * @return true if FORUM component is exists, false otherwise
      */
     public boolean isJcommuneVisible() {
         return !(getJcommune() == null);
     }
 
     /**
      * Deletes selected component. Selected component is set using {@link #setSelected(Component)}.
      *
      * @throws IllegalStateException if no component selected
      */
     @Command
     public void deleteComponent() {
         Validate.validState(selected != null, "entity to delete must be selected");
 
         DialogManager.Performable dc = new DialogManager.Performable() {
             @Override
             public void execute() {
                 try {
                     componentService.deleteComponent(selected);
                     selected = null;
                     bindWrapper.postNotifyChange(ComponentsVm.this, JCOMMUNE, POULPE, JCOMMUNE_VISIBLE, POULPE_VISIBLE,
                             SELECTED, COMPONENTS, CAN_CREATE_NEW_COMPONENT);
                 } catch (NoConnectionToJcommuneException elementDoesNotExist) {
                     Messagebox.show(Labels.getLabel(JCOMMUNE_CONNECTION_FAILED),
                            Labels.getLabel(COMPONENT_DELETING_FAILED_DIALOG_TITLE),
                            Messagebox.OK, Messagebox.ERROR);
                 } catch (JcommuneRespondedWithErrorException elementDoesNotExist) {
                     Messagebox.show(Labels.getLabel(JCOMMUNE_RESPONSE_FAILED),
                            Labels.getLabel(COMPONENT_DELETING_FAILED_DIALOG_TITLE),
                            Messagebox.OK, Messagebox.ERROR);
                 } catch (JcommuneUrlNotConfiguratedException elementDoesNotExist) {
                     Messagebox.show(Labels.getLabel(JCOMMUNE_URL_FAILED),
                            Labels.getLabel(COMPONENT_DELETING_FAILED_DIALOG_TITLE),
                            Messagebox.OK, Messagebox.ERROR);
                 }
             }
         };
 
         dialogManager.confirmDeletion(selected.getName(), dc);
     }
 
     /**
      * Sending empty httpRequest to jcommune, this request starts re-index process at jcommune.
      */
     @Command
     public void reindexComponent() {
         try {
             jcommuneHttpNotifier.notifyAboutReindexComponent("url");   //TODO set JCommune url
         } catch (NoConnectionToJcommuneException e) {
             showNotConnectedNotification();
         } catch (JcommuneUrlNotConfiguratedException e) {
             showNotConfiguratedNotification();
         } catch (JcommuneRespondedWithErrorException e) {
             showNotConfiguratedNotification();        //TODO what is that? how should i handle this?
         }
     }
 
     /**
      * Opens window, witch notify about not configurated URL.
      */
     @NotifyChange(SHOW_NOT_CONFIGURED_NOTIFICATION)
     private void showNotConfiguratedNotification() {
         showNotConfiguredNotification = true;
     }
 
     /**
      * Opens window, witch notify about no connection to JCommune component.
      */
     @NotifyChange(SHOW_NOT_CONNECTED_NOTIFICATION)
     private void showNotConnectedNotification() {
         showNotConnectedNotification = true;
     }
 
     /**
      * Shows a window for adding {@link org.jtalks.poulpe.model.entity.Poulpe} component.
      */
     @Command
     @NotifyChange(CAN_CREATE_NEW_COMPONENT)
     public void addNewPoulpe() {
         selectedEntity.setEntity(componentService.
                 baseComponentFor(ComponentType.ADMIN_PANEL).newComponent(DEFAULT_NAME, DEFAULT_DESCRIPTION));
         AddComponentVm.openWindowForAdding(windowManager);
     }
 
     /**
      * Shows a window for adding {@link org.jtalks.poulpe.model.entity.Jcommune} component.
      */
     @Command
     @NotifyChange(CAN_CREATE_NEW_COMPONENT)
     public void addNewJcommune() {
         selectedEntity.setEntity(componentService.
                 baseComponentFor(ComponentType.FORUM).newComponent(DEFAULT_NAME, DEFAULT_DESCRIPTION));
         AddComponentVm.openWindowForAdding(windowManager);
     }
 
     /**
      * Shows a component edit window for currently selected element. Selected component is set using {@link
      * #setSelected(Component)}.
      */
     @Command
     public void configureComponent() {
         selectedEntity.setEntity(selected);
         EditComponentVm.openWindowForEdit(windowManager);
     }
 
     /**
      * @return {@code true} only if new component can be created, {@code false} otherwise.
      */
     public boolean isAbleToCreateNewComponent() {
         return !componentService.getAvailableTypes().isEmpty();
     }
 
     /**
      * @param selected currently selected component
      */
     public void setSelected(Component selected) {
         this.selected = selected;
     }
 
     /**
      * @param bindWrapper {@link BindUtilsWrapper} instance to set
      */
     @VisibleForTesting
     void setBindWrapper(BindUtilsWrapper bindWrapper) {
         this.bindWrapper = bindWrapper;
     }
 
     /**
      * Opens Components window.
      *
      * @param windowManager implementation of {@link WindowManager} for opening and closing application windows
      */
     public static void show(WindowManager windowManager) {
         windowManager.open(COMPONENTS_PAGE_LOCATION);
     }
 
     /**
      * @return true if component {@link org.jtalks.poulpe.model.entity.Jcommune} with {@link ComponentType}
      *         FORUM type are not created yet, false otherwise
      */
     public boolean isJcommuneAvailable() {
         return componentService.getAvailableTypes().contains(ComponentType.FORUM);
     }
 
     /**
      * @return true if component {@link org.jtalks.poulpe.model.entity.Poulpe} with {@link ComponentType}
      *         ADMIN_PANEL type are not created yet, false otherwise
      */
     public boolean isPoulpeAvailable() {
         return componentService.getAvailableTypes().contains(ComponentType.ADMIN_PANEL);
     }
 
     /**
      * Gets visibility status of notification window, boolean show added because after single opening of popup
      * window before next check we should have false at showNotConnectedNotification.
      *
      * @return true if notification is visible
      */
     public boolean isShowNotConnectedNotification() {
         boolean show = showNotConnectedNotification;
         showNotConnectedNotification = false;
         return show;
     }
 
     /**
      * Gets visibility status of notification window, boolean show added because after single opening of popup
      * window before next check we should have false at showNotConfiguredNotification.
      *
      * @return true if notification is visible
      */
     public boolean isShowNotConfiguredNotification() {
         boolean show = showNotConfiguredNotification;
         showNotConfiguredNotification = false;
         return show;
     }
 }
