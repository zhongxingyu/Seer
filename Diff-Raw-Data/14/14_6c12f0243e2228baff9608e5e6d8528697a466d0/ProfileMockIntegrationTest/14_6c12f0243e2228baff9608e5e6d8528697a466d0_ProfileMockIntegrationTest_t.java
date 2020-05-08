 /*******************************************************************************
  * Copyright (c) 2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.internal.deltacloud.test.core.client;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClient;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClientException;
 import org.jboss.tools.deltacloud.core.client.HardwareProfile;
 import org.jboss.tools.internal.deltacloud.test.context.MockIntegrationTestContext;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Integration tests for key related operations in delta cloud client.
  * 
  * @author Andre Dietisheim
  * 
  * @see DeltaCloudClient#listProfiles()
  * @see DeltaCloudClient#listProfie(String)
  */
 public class ProfileMockIntegrationTest {
 
 	private MockIntegrationTestContext testSetup;
 
 	@Before
 	public void setUp() throws IOException, DeltaCloudClientException {
 		this.testSetup = new MockIntegrationTestContext();
 		testSetup.setUp();
 	}
 
 	@After
 	public void tearDown() {
 		testSetup.tearDown();
 	}
 
 	@Test
 	public void canListProfiles() throws DeltaCloudClientException {
 		List<HardwareProfile> hardwareProfiles = testSetup.getClient().listProfiles();
 		assertNotNull(hardwareProfiles);
 	}
 
 	@Test
 	public void canGetProfile() throws DeltaCloudClientException {
 		// get a profile seen in the web UI
 		HardwareProfile profile = testSetup.getClient().listProfile("m1-small");
 		assertNotNull(profile);
		assertHardWareProfile("i386", "1740.8 MB", "160 GB", "1", profile);
 	}
 
	public void assertHardWareProfile(String architecture, String memory, String storage, String cpu, HardwareProfile profile) {
 		assertEquals(architecture, profile.getArchitecture());
 		assertEquals(memory, profile.getMemory());
 		assertEquals(storage, profile.getStorage());
		assertEquals(cpu, profile.getCPU());
 	}
 }
