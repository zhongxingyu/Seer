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
 package org.jtalks.poulpe.service.transactional;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 
 import java.util.Collections;
 import java.util.Set;
 
import org.jtalks.common.model.entity.Entity;
 import org.jtalks.poulpe.model.dao.ComponentDao;
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.validation.EntityValidator;
 import org.jtalks.poulpe.validation.ValidationError;
 import org.jtalks.poulpe.validation.ValidationException;
import org.jtalks.poulpe.validation.ValidationResult;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author Pavel Vervenko
  * @author Alexey Grigorev
  */
 public class TransactionalComponentServiceTest {
 
     private ComponentDao componentDao;
     private TransactionalComponentService conponentService;
     private EntityValidator validator;
 
     Component component = new Component();
 
     @BeforeMethod
     public void setUp() throws Exception {
         componentDao = mock(ComponentDao.class);
         validator = mock(EntityValidator.class);
         conponentService = new TransactionalComponentService(componentDao, validator);
     }
 
     @Test
     public void testGetAll() {
         conponentService.getAll();
         verify(componentDao).getAll();
     }
 
     @Test(expectedExceptions = ValidationException.class)
     public void testSaveComponentException() {
         givenConstraintsViolations();
         conponentService.saveComponent(component);
     }
 
     private void givenConstraintsViolations() {
        Set<ValidationError> errors = Collections.singleton(new ValidationError("name", "errorMessageCode"));
        ValidationResult result = new ValidationResult(errors);
        when(validator.validate(any(Entity.class))).thenReturn(result);
     }
 
     @Test
     public void testDeleteComponent() {
         conponentService.deleteComponent(component);
         verify(componentDao).delete(component.getId());
     }
 
     @Test
     void testGetAvailableTypes() {
         conponentService.getAvailableTypes();
         verify(componentDao).getAvailableTypes();
     }
 }
