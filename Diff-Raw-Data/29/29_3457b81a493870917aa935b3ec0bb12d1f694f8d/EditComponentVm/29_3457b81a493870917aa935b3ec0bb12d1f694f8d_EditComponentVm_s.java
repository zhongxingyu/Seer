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
 package org.jtalks.poulpe.web.controller.component.dialogs;
 
 import static org.apache.commons.lang3.Validate.notNull;
 import static org.zkoss.util.resource.Labels.getLabel;
 
 import javax.annotation.Nonnull;
 
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.service.ComponentService;
 import org.jtalks.poulpe.service.exceptions.EntityIsRemovedException;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
 import org.jtalks.poulpe.web.controller.component.ComponentList;
 import org.jtalks.poulpe.web.controller.component.ComponentsVm;
 import org.jtalks.poulpe.web.controller.zkutils.BooleanStringConverter;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.zul.Messagebox;
 
 /**
  * ViewModel class for EditComponent View
  * 
  * @author Vahluev Vyacheslav
  * @author Kazancev Leonid
  * @author Alexey Grigorev
  */
 public class EditComponentVm {
     public static final String EDIT_COMPONENT_LOCATION = "/WEB-INF/pages/component/edit_comp.zul";
    public static final String COMPONENT_EDITING_FAILED = "component.error.is_removed";
    public static final String COMPONENT_EDITING_FAILED_TITLE = "component.error.editing_failed_title";
 
     private final ComponentService componentService;
     private final Component component;
     private final ComponentList components;
 
     /**
      * Opens window for editing component.
      *
      * @param windowManager The object which is responsible for creation and closing application windows
      */
     public static void openWindowForEdit(WindowManager windowManager) {
         windowManager.open(EDIT_COMPONENT_LOCATION);
     }
     private WindowManager windowManager;
 
     /**
      * Creates edit dialog for editing currently selected component
      *
      * @param componentService service for saving component
      * @param selectedComponent currently selected component
      * @param components
      */
     public EditComponentVm(@Nonnull ComponentService componentService,
                            @Nonnull SelectedEntity<Component> selectedComponent, ComponentList components) {
         this.componentService = componentService;
         this.components = components;
         this.component = notNull(selectedComponent.getEntity());
     }
 
     /**
      * Saves a component
      */
     @Command
     public void save() {
         try {
             componentService.updateComponent(component);
             components.componentsUpdated();
         } catch (EntityIsRemovedException ex) {
             components.renew(componentService.getAll());
             Messagebox.show(getLabel(COMPONENT_EDITING_FAILED),
                             getLabel(COMPONENT_EDITING_FAILED_TITLE), Messagebox.OK, Messagebox.ERROR);
         } finally {
             switchToComponentsWindow();
         }
     }
 
     /**
      * Cancels all the actions
      */
     @Command
     public void cancel() {
         switchToComponentsWindow();
     }
 
     /**
      * Opens component view window.
      */
     private void switchToComponentsWindow() {
         ComponentsVm.show(windowManager);
     }
 
     /**
      * @return component being edited
      */
     public Component getComponent() {
         return component;
     }
 
     /**
      * @param windowManager manager responsible for creation and closing application windows
      */
     public void setWindowManager(WindowManager windowManager) {
         this.windowManager = windowManager;
     }
 }
