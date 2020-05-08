 /*******************************************************************************
  * Copyright 2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.api;
 
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.junit.Test;
 
 /**
  * Tests the API used in the example plugins.
  * 
  * @author mkoegel
  * 
  */
 public class APIUsedInExampleTest extends BaseLoggedInUserTest {
 
 	@Test
 	public void testHelloWorldExample() throws ESException {
		org.eclipse.emf.emfstore.example.helloworld.Application.runClient(server);
 	}
 
 	@Test
 	public void testMergeExample() throws ESException {
		org.eclipse.emf.emfstore.example.helloworld.Application.runClient(server);
		org.eclipse.emf.emfstore.example.merging.Application.runClient(server);
 	}
 
 }
