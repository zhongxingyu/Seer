 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 /**
  * 
  */
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 /**
  * @author Dylan
  *
  */
 public class AcceptanceTestTest {
 
 	@Test
 	public void createTestAndRetrieveAttributes() {
 		AcceptanceTest at = new AcceptanceTest(0, "name", "description");
 		assertEquals(at.getName(), "name");
 		assertEquals(at.getDescription(), "description");
 		assertEquals(at.getStatus(), "");
 		
 		AcceptanceTest at2 = new AcceptanceTest(1, "a", "b");
 		at2.setName("name");
 		at2.setDescription("desc");
 		at2.setStatus(TestStatus.STATUS_PASSED);
 		assertEquals(at2.getName(), "name");
 		assertEquals(at2.getDescription(), "desc");
 		assertEquals(at2.getStatus(), "Passed");
 	}
 
 	@Test
 	public void testNameLongerThan100Chars() {
 		AcceptanceTest at = new AcceptanceTest(2, "name", "desc");
 		// 101 characters
 		at.setName("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901");
 		assertEquals(at.getName().length(), 100);
 	}
 	
 	@Test(expected=NullPointerException.class)
 	public void testNullName() {
 		// Assert an error is thrown when a name is not given
 		AcceptanceTest at = new AcceptanceTest(3, "", "desc");
 	}
 	
 }
