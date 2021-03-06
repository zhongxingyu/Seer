 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.appointment.api;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Location;
 import org.openmrs.Provider;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.appointment.AppointmentBlock;
 import org.openmrs.module.appointment.AppointmentType;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.openmrs.test.Verifies;
 
 /**
  * Tests {@link $ AppointmentService} .
  */
 public class AppointmentBlockServiceTest extends BaseModuleContextSensitiveTest {
 	
 	private AppointmentService service;
 	
 	@Before
 	public void before() throws Exception {
 		service = Context.getService(AppointmentService.class);
 		executeDataSet("standardAppointmentTestDataset.xml");
 	}
 	
 	@Test
 	@Verifies(value = "should get all appointment blocks", method = "getAllAppointmentBlocks()")
 	public void getAllAppointmentBlocks_shouldGetAllAppointmentBlocks() throws Exception {
 		List<AppointmentBlock> appointmentBlocks = service.getAllAppointmentBlocks();
 		assertEquals(3, appointmentBlocks.size());
 	}
 	
 	@Test
 	@Verifies(value = "should get correct appointment block", method = "getAppointmentBlock(Integer)")
 	public void getAppointmentBlock_shouldGetCorrectAppointmentBlock() throws Exception {
 		AppointmentBlock appointmentBlock = service.getAppointmentBlock(1);
 		assertNotNull(appointmentBlock);
 		assertEquals("2005-01-01 00:00:00.0", appointmentBlock.getStartDate().toString());
 		
 		appointmentBlock = service.getAppointmentBlock(2);
 		assertNotNull(appointmentBlock);
 		assertEquals("2005-01-02 00:00:00.0", appointmentBlock.getStartDate().toString());
 		
 		appointmentBlock = service.getAppointmentBlock(3);
 		assertNotNull(appointmentBlock);
 		assertEquals("2005-01-03 00:00:00.0", appointmentBlock.getStartDate().toString());
 		
 		appointmentBlock = service.getAppointmentBlock(4);
 		Assert.assertNull(appointmentBlock);
 	}
 	
 	@Test
 	@Verifies(value = "should get correct appointment block", method = "getAppointmentBlockByUuid(String)")
 	public void getAppointmentBlockByUuid_shouldGetCorrectAppointmentBlock() throws Exception {
 		AppointmentBlock appointmentBlock = service.getAppointmentBlockByUuid("c0c579b0-8e59-401d-8a4a-976a0b183599");
 		assertNotNull(appointmentBlock);
 		assertEquals("2005-01-01 00:00:00.0", appointmentBlock.getStartDate().toString());
 		
 		appointmentBlock = service.getAppointmentBlockByUuid("759799ab-c9a5-435e-b671-77773ada99e9");
 		assertNotNull(appointmentBlock);
 		assertEquals("2005-01-02 00:00:00.0", appointmentBlock.getStartDate().toString());
 		
 		appointmentBlock = service.getAppointmentBlockByUuid("759799ab-c9a5-435e-b671-77773ada7499");
 		assertNotNull(appointmentBlock);
 		assertEquals("2005-01-03 00:00:00.0", appointmentBlock.getStartDate().toString());
 		
 		appointmentBlock = service.getAppointmentBlockByUuid("759799ab-c9a5-435e-b671-77773ada74e1");
 		Assert.assertNull(appointmentBlock);
 	}
 	
 	@Test
 	@Verifies(value = "should get correct appointment blocks", method = "getAppointmentBlocks(String)")
 	public void getAppointmentBlocks_shouldGetCorrectAppointmentBlocks() throws Exception {
 		//I think we should delete it.
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Test
 	@Verifies(value = "should save new appointment block", method = "saveAppointmentBlock(AppointmentBlock)")
 	public void saveAppointmentBlock_shouldSaveNewAppointmentBlock() throws Exception {
 		List<AppointmentBlock> appointmentBlocks = service.getAllAppointmentBlocks(true);
		assertEquals(2, appointmentBlocks.size());
 		
		appointmentBlocks = service.getAllAppointmentBlocks(false);
		assertEquals(1, appointmentBlocks.size());
 		
 		Set<AppointmentType> appointmentTypes = service.getAllAppointmentTypes();
		AppointmentBlock appointmentBlock = new AppointmentBlock(4, new Date("2005-01-01 00:00:00.0"), new Date(
		        "2005-01-01 00:00:00.0"), new Provider(), new Location(), appointmentTypes);
 		service.saveAppointmentBlock(appointmentBlock);
 		
 		appointmentBlock = service.getAppointmentBlock(4);
 		assertNotNull(appointmentBlock);
 		
 		//Should create a new appointment block row
 		assertEquals(4, service.getAllAppointmentBlocks());
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Test
 	@Verifies(value = "should save edited appointment block", method = "saveAppointmentBlock(AppointmentBlock)")
 	public void saveAppointmentBlock_shouldSaveEditedAppointmentBlock() throws Exception {
 		
 		AppointmentBlock appointmentBlock = service.getAppointmentBlock(1);
 		assertNotNull(appointmentBlock);
 		assertEquals("2005-01-01 00:00:00.0", appointmentBlock.getStartDate().toString());
		
		appointmentBlock.setStartDate(new Date("2006-01-01 00:00:00.0"));
 		service.saveAppointmentBlock(appointmentBlock);
 		
 		appointmentBlock = service.getAppointmentBlock(1);
 		assertNotNull(appointmentBlock);
		assertEquals("2006-01-01 00:00:00.0", appointmentBlock.getStartDate().toString());
 		
 		//Should not change the number of appointment types
 		assertEquals(3, service.getAllAppointmentBlocks().size());
 		
 	}
 	
 	@Test
 	@Verifies(value = "should void given appointment block", method = "voidAppointmentBlock(AppointmentBlock, String)")
 	public void voidAppointmentBlock_shouldVoidGivenAppointmentBlock() throws Exception {
 		AppointmentBlock appointmentBlock = service.getAppointmentBlock(1);
 		assertNotNull(appointmentBlock);
 		Assert.assertFalse(appointmentBlock.isVoided());
 		Assert.assertNull(appointmentBlock.getVoidReason());
 		
 		service.voidAppointmentBlock(appointmentBlock, "void reason");
 		
 		appointmentBlock = service.getAppointmentBlock(1);
 		assertNotNull(appointmentBlock);
 		assertTrue(appointmentBlock.isVoided());
 		assertEquals("void reason", appointmentBlock.getVoidReason());
 		
 		//Should not change the number of appointment blocks.
 		assertEquals(3, service.getAllAppointmentBlocks().size());
 	}
 	
 	@Test
 	@Verifies(value = "should unvoid given appointment block", method = "unvoidAppointmentBlock(AppointmentBlock)")
 	public void unvoidAppointmentBlock_shouldUnvoidGivenAppointmentBlock() throws Exception {
 		AppointmentBlock appointmentBlock = service.getAppointmentBlock(2);
 		assertNotNull(appointmentBlock);
 		Assert.assertTrue(appointmentBlock.isVoided());
 		assertEquals("Some void reason", appointmentBlock.getVoidReason());
 		
 		service.unvoidAppointmentBlock(appointmentBlock);
 		
 		appointmentBlock = service.getAppointmentBlock(2);
 		assertNotNull(appointmentBlock);
 		Assert.assertFalse(appointmentBlock.isVoided());
 		Assert.assertNull("void reason", appointmentBlock.getVoidReason());
 		
 		//Should not change the number of appointment blocks.
 		assertEquals(3, service.getAllAppointmentBlocks().size());
 	}
 	
 	@Test
 	@Verifies(value = "should delete given appointment block", method = "purgeAppointmentBlock(AppointmentBlock)")
 	public void purgeAppointmentBlock_shouldDeleteGivenAppointmentBlock() throws Exception {
 		AppointmentBlock appointmentBlock = service.getAppointmentBlock(3);
 		assertNotNull(appointmentBlock);
 		
 		service.purgeAppointmentBlock(appointmentBlock);
 		
 		appointmentBlock = service.getAppointmentBlock(3);
 		Assert.assertNull(appointmentBlock);
 		
 		//Should decrease the number of appointment blocks by one.
 		assertEquals(2, service.getAllAppointmentBlocks().size());
 	}
 	
 }
