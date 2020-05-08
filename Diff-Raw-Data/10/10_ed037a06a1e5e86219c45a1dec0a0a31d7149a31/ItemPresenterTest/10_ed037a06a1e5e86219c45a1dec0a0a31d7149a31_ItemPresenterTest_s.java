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
 
import static org.testng.Assert.*;
 import static org.jtalks.poulpe.web.controller.utils.ObjectCreator.createComponents;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.service.ComponentService;
 import org.jtalks.poulpe.validation.EntityValidator;
 import org.jtalks.poulpe.validation.ValidationError;
 import org.jtalks.poulpe.validation.ValidationResult;
 import org.jtalks.poulpe.web.controller.DialogManager;
 import org.jtalks.poulpe.web.controller.component.items.ItemPresenter;
 import org.jtalks.poulpe.web.controller.component.items.ItemView;
 import org.mockito.ArgumentCaptor;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * The test for {@link ItemPresenter} class
  * 
  * @author Dmitriy Sukharev
  * @author Alexey Grigorev
  */
 public class ItemPresenterTest {
 
     private ItemPresenter presenter;
 
     private ComponentService componentService;
     private ItemView view;
     private List<Component> components;
     private Component component;
     private DialogManager dialogManager;
 
     private EntityValidator entityValidator;
 
     
     private ValidationResult resultWithErrors = resultWithErrors();
     
     private ValidationResult resultWithErrors() {
         ValidationError error = new ValidationError("name", Component.NOT_UNIQUE_NAME);
         Set<ValidationError> errors = Collections.singleton(error);
         return new ValidationResult(errors);
     }
     
     @BeforeMethod
     public void setUp() {
         presenter = new ItemPresenter();
 
         componentService = mock(ComponentService.class);
         presenter.setComponentService(componentService);
 
         dialogManager = mock(DialogManager.class);
         presenter.setDialogManager(dialogManager);
         
         entityValidator = mock(EntityValidator.class);
         presenter.setEntityValidator(entityValidator);
         
         view = mock(ItemView.class);
         presenter.setView(view);
         
         prepareComponents();
         prepareInput();
     }
 
     private void prepareComponents() {
         components = createComponents();
         component = components.get(0);
         int someRandomId = 12;
         component.setId(someRandomId);
         
         when(componentService.getAll()).thenReturn(components);
     }
 
     private void prepareInput() {
         when(view.getName()).thenReturn(component.getName());
         when(view.getComponentId()).thenReturn(component.getId());
         when(view.getDescription()).thenReturn(component.getDescription());
         when(view.getComponentType()).thenReturn(component.getComponentType());
     }
 
     @Test
     public void editTest() throws Exception {
         presenter.edit(component);
         verifyAllDataSet();
     }
 
     private void verifyAllDataSet() {
         verify(view).setComponentId(component.getId());
         verify(view).setName(component.getName());
         verify(view).setDescription(component.getDescription());
         verify(view).setComponentType(component.getComponentType());
     }
 
     @Test
     public void createTest() {
         presenter.create();
         verifyBlankWindowShown();
     }
 
     private void verifyBlankWindowShown() {
         verify(view).setComponentId(0);
         verify(view).setName(null);
         verify(view).setDescription(null);
         verify(view).setComponentType(null);
     }
 
     @Test
     public void saveNewComponentTest() throws Exception {
         presenter.create();
 
         givenNoConstraintViolations();
         presenter.saveComponent();
 
         assertComponentSaved();
         verify(view).hide();
     }
 
     private void givenNoConstraintViolations() {
         when(entityValidator.validate(any(Component.class))).thenReturn(ValidationResult.EMPTY);
     }
 
     private void assertComponentSaved() {
         ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
 
         verify(presenter.getComponentService()).saveComponent(captor.capture());
         Component value = captor.getValue();
 
         assertEquals(value.getName(), component.getName());
         assertEquals(value.getComponentType(), component.getComponentType());
     }
 
     @Test
     public void saveComponentExistingTest() throws Exception {
         presenter.edit(component);
 
         givenNoConstraintViolations();
         presenter.saveComponent();
 
         verify(presenter.getComponentService()).saveComponent(component);
         verify(view).hide();
     }
 
     @Test
     public void saveComponentDuplicateTest() throws Exception {
         presenter.create();
 
         givenConstraintViolations();
         presenter.saveComponent();
 
         verify(view).validaionFailure(resultWithErrors);
     }
 
     private void givenConstraintViolations() {
         when(entityValidator.validate(any(Component.class))).thenReturn(resultWithErrors);
     }
 
     @Test
     public void saveComponentDuplicateTestEdit() throws Exception {
         presenter.edit(component);
 
         givenConstraintViolations();
         presenter.saveComponent();
 
         verify(view).validaionFailure(resultWithErrors);
     }
 
     @Test
     public void checkComponentTestNewComponent() {
         presenter.create();
 
         givenNoConstraintViolations();
         presenter.checkComponent();
 
         verify(view, never()).validaionFailure(resultWithErrors);
     }
 
     @Test
     public void checkComponentTestExistingComponent() {
         presenter.edit(component);
 
         givenNoConstraintViolations();
         presenter.checkComponent();
 
         verify(view, never()).validaionFailure(resultWithErrors);
     }
 
     @Test
     public void checkComponentDiplicate() {
         presenter.edit(component);
         
         givenConstraintViolations();
         presenter.checkComponent();
 
         verify(view).validaionFailure(resultWithErrors);
     }
 
 }
