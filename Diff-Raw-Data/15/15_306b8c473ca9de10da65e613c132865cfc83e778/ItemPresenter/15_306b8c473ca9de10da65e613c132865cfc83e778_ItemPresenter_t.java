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
 package org.jtalks.poulpe.web.controller.component.items;
 
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.validation.EntityValidator;
 import org.jtalks.poulpe.validation.ValidationResult;
 import org.jtalks.poulpe.web.controller.component.AbstractComponentPresenter;
 
 /**
  * The class for mediating between model and view representation of components.
  * 
  * @author Dmitriy Sukharev
  * @author Alexey Grigorev
  */
 public class ItemPresenter extends AbstractComponentPresenter {
 
     private ItemView view;
     private ItemStrategy editingStrategy = DoNothingItemStrategy.instance;
     
     private EntityValidator entityValidator;
 
     /**
      * Initializes this presenter for creating a new component
      * @param view current {@link ItemView} instance
      */
     public void create() {
         editingStrategy = new NewItemStrategy(view);
         reinit();
     }
 
     /**
      * Initializes this presenter for editing the new component
      * @param view current {@link ItemView} instance
      * @param component to be edited
      */
     public void edit(Component component) {
         editingStrategy = new EditItemStrategy(view, component);
         reinit();
     }
 
     private void reinit() {
         editingStrategy.init();
         view.setComponentTypes(getComponentService().getAvailableTypes());
     }
 
     /**
      * Saves the created or edited component in component list.
      */
     public void saveComponent() {
         Component component = editingStrategy.itemForSaving();
 
        if (validate(component)) {
             getComponentService().saveComponent(component);
             view.hide();
         }
     }
 
     /**
      * Checks if name of created or edited component is a duplicate. Invokes
      * notification if it is.
      */
     public void checkComponent() {
         Component component = ViewToEntityConverter.view2Model(view);
        validate(component);
    }
 
    private boolean validate(Component component) {
         ValidationResult result = entityValidator.validate(component);
         
         if (result.hasErrors()) {
             view.validaionFailure(result);
            return false;
         }
         
        return true;
     }
 
     /**
      * @return the view
      */
     public ItemView getView() {
         return view;
     }
 
     /**
      * @param view the view to set
      */
     public void setView(ItemView view) {
         this.view = view;
     }
 
     /**
      * @return the entityValidator
      */
     public EntityValidator getEntityValidator() {
         return entityValidator;
     }
 
     /**
      * @param entityValidator the entityValidator to set
      */
     public void setEntityValidator(EntityValidator entityValidator) {
         this.entityValidator = entityValidator;
     }
 
 }
