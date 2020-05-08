 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.domain.validator;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.service.VirtualResourceGroupService;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 
 public class VirtualResourceGroupValidatorTest {
 
   private VirtualResourceGroupService virtualResourceGroupServiceMock;
   private VirtualResourceGroupValidator virtualResourceGroupValidator;
 
   @Before
   public void initController() {
     virtualResourceGroupValidator = new VirtualResourceGroupValidator();
     virtualResourceGroupServiceMock = mock(VirtualResourceGroupService.class);
     virtualResourceGroupValidator.setVirtualResourceGroupService(virtualResourceGroupServiceMock);
   }
 
   @Test
   public void testSupportsValidClass() {
     assertTrue(virtualResourceGroupValidator.supports(VirtualResourceGroup.class));
   }
 
   @Test
   public void testSupportsInValidClass() {
     assertFalse(virtualResourceGroupValidator.supports(Object.class));
   }
 
   @Test
   public void noValidateOnSurfConextGroupName() {
     VirtualResourceGroup virtualResourceGroupOne = new VirtualResourceGroupFactory().setSurfConextGroupName("one")
         .create();
 
     when(virtualResourceGroupServiceMock.findBySurfConextGroupName("one")).thenReturn(null);
     Errors errors = new BeanPropertyBindingResult(virtualResourceGroupOne, "virtualResourceGroup");
 
     virtualResourceGroupValidator.validate(virtualResourceGroupOne, errors);
 
     assertFalse(errors.hasErrors());
   }
 
   @Test
   public void renamingOneToTwoWithExistingTwoShouldGiveAnError() {
     VirtualResourceGroup virtualResourceGroupOne = new VirtualResourceGroupFactory().setName("one").create();
     VirtualResourceGroup virtualResourceGroupTwo = new VirtualResourceGroupFactory().setName("two").create();
 
     when(virtualResourceGroupServiceMock.findByName("two")).thenReturn(virtualResourceGroupTwo);
 
     virtualResourceGroupOne.setName("two");
 
     Errors errors = new BeanPropertyBindingResult(virtualResourceGroupOne, "virtualResourceGroup");
     assertFalse(errors.hasErrors());
 
     virtualResourceGroupValidator.validate(virtualResourceGroupOne, errors);
 
     assertTrue(errors.hasFieldErrors("name"));
     assertFalse(errors.hasGlobalErrors());
   }
 
   @Test
   public void testValidateNonExistingName() {
     VirtualResourceGroup virtualResourceGroupOne = new VirtualResourceGroupFactory().setName("one").create();
 
     when(virtualResourceGroupServiceMock.findByName("one")).thenReturn(null);
 
     Errors errors = new BeanPropertyBindingResult(virtualResourceGroupOne, "virtualResourceGroup");
     assertFalse(errors.hasErrors());
 
     virtualResourceGroupValidator.validate(virtualResourceGroupOne, errors);
 
     assertFalse(errors.hasErrors());
   }
 
   @Test
   public void testValidateExistingNameAndSurfConextGroup() {
     VirtualResourceGroup virtualResourceGroupOne = new VirtualResourceGroupFactory().setName("one")
         .setSurfConextGroupName("surfOne").create();
 
     when(virtualResourceGroupServiceMock.findBySurfConextGroupName("one")).thenReturn(virtualResourceGroupOne);
 
     Errors errors = new BeanPropertyBindingResult(virtualResourceGroupOne, "virtualResourceGroup");
     assertFalse(errors.hasErrors());
 
     virtualResourceGroupValidator.validate(virtualResourceGroupOne, errors);
 
     assertFalse(errors.hasFieldErrors("name"));
     assertFalse(errors.hasFieldErrors("surfConextGroupName"));
     assertFalse(errors.hasGlobalErrors());
   }
   
   @Test
  public void testValidateNonExistingNameAndExistingSurfConextGroup() {
     final String surfConextGroupName = "surfGroupTest";
     VirtualResourceGroup virtualResourceGroupOne = new VirtualResourceGroupFactory().setName("one")
         .setSurfConextGroupName(surfConextGroupName).create();
 
     when(virtualResourceGroupServiceMock.findBySurfConextGroupName(surfConextGroupName)).thenReturn(virtualResourceGroupOne);
    when(virtualResourceGroupServiceMock.findByName("one")).thenReturn(null);
 
     Errors errors = new BeanPropertyBindingResult(virtualResourceGroupOne, "virtualResourceGroup");
     assertThat(errors.hasErrors(), is(false));
 
     virtualResourceGroupValidator.validate(virtualResourceGroupOne, errors);
     
    assertThat(errors.getAllErrors(), hasSize(1));
    assertThat(errors.hasFieldErrors("surfConextGroupName"), is(true));
   }
 
 }
