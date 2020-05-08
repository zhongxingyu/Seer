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
 package org.jboss.tools.internal.deltacloud.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.List;
 
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClient;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClientException;
 import org.jboss.tools.deltacloud.core.client.Image;
 import org.jboss.tools.deltacloud.core.client.Instance;
 import org.jboss.tools.deltacloud.core.client.Instance.State;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Integration tests for instance related methods in {@link DeltaCloudClient}.
  * 
  * @see DeltaCloudClient#listInstances()
  * @see DeltaCloudClient#createInstance(String)
  * @see DeltaCloudClient#destroyInstance(String)
  * @see DeltaCloudClient#startInstance(String)
  */
 public class InstanceMockIntegrationTest {
 
 	private MockIntegrationTestSetup testSetup;
 
 	@Before
 	public void setUp() throws IOException, DeltaCloudClientException {
 		this.testSetup = new MockIntegrationTestSetup();
 		testSetup.setUp();
 	}
 
 	@After
 	public void tearDown() {
 		testSetup.tearDown();
 	}
 
 	/**
 	 * #listInstance contains the test instance created in {@link #setUp()}
 	 * 
 	 * @throws DeltaCloudClientException
 	 *             the delta cloud client exception
 	 */
 	@Test
 	public void listContainsTestInstance() throws DeltaCloudClientException {
 		DeltaCloudClient client = testSetup.getClient();
 		List<Instance> instances = client.listInstances();
 		assertTrue(instances.size() > 0);
 		Instance testInstance = testSetup.getTestInstance();
 		assertNotNull(testSetup.getInstanceById(testInstance.getId(), client));
 	}
 
 	@Test
 	public void listTestInstance() throws DeltaCloudClientException {
 		Instance instance = testSetup.getClient().listInstances(testSetup.getTestInstance().getId());
 		assertNotNull(instance);
 		Instance testInstance = testSetup.getTestInstance();
 		assertEquals(testInstance.getId(), instance.getId());
 		assertInstance(
 				testInstance.getName()
 				, testInstance.getOwnerId()
 				, testInstance.getImageId()
 				, testInstance.getRealmId()
 				, testInstance.getProfileId()
 				, testInstance.getMemory()
 				, testInstance.getPrivateAddresses()
 				, testInstance.getPublicAddresses()
 				, instance);
 	}
 
 	@Test(expected = DeltaCloudClientException.class)
 	public void listDestroyedInstanceThrowsException() throws DeltaCloudClientException {
 		Instance testInstance = testSetup.getTestInstance();
 		testSetup.quietlyDestroyInstance(testInstance);
 		testSetup.getClient().listInstances(testInstance.getId());
 	}
 
 	private void assertInstance(String name, String owner, String ImageId, String realmId, String profile,
 			String memory, List<String> privateAddresses, List<String> publicAddresses, Instance instance) {
 		assertNotNull(instance);
 		assertEquals(name, instance.getName());
 		assertEquals(owner, instance.getOwnerId());
 		assertEquals(realmId, instance.getRealmId());
 		assertEquals(profile, instance.getProfileId());
 		assertEquals(memory, instance.getMemory());
 		assertTrue(privateAddresses.equals(instance.getPrivateAddresses()));
 		assertTrue(publicAddresses.equals(instance.getPublicAddresses()));
 	}
 
 	@Test(expected = DeltaCloudClientException.class)
 	public void cannotDestroyIfNotAuthenticated() throws MalformedURLException, DeltaCloudClientException {
 		DeltaCloudClient unauthenticatedClient = new DeltaCloudClient(MockIntegrationTestSetup.DELTACLOUD_URL,
 				"badUser", "badPassword");
 		Image image = testSetup.getFirstImage(unauthenticatedClient);
 		unauthenticatedClient.createInstance(image.getId());
 	}
 
 	@Test
 	public void canCreateInstance() throws DeltaCloudClientException {
 		Instance instance = null;
 		try {
 			Image image = testSetup.getFirstImage(testSetup.getClient());
 			instance = testSetup.getClient().createInstance(image.getId());
 			assertTrue(instance != null);
 			assertEquals(image.getId(), instance.getImageId());
 			assertEquals(State.RUNNING, instance.getState());
 		} finally {
 			testSetup.quietlyDestroyInstance(instance);
 		}
 	}
 
 	@Test(expected = DeltaCloudClientException.class)
 	public void cannotDestroyUnknownImageId() throws DeltaCloudClientException {
 		testSetup.getClient().createInstance("dummy");
 	}
 
 	@Test
 	public void canDestroy() throws DeltaCloudClientException {
 		Image image = testSetup.getFirstImage(testSetup.getClient());
 		Instance instance = testSetup.getClient().createInstance(image.getId());
 		testSetup.getClient().destroyInstance(instance.getId());
 		assertNull(testSetup.getInstanceById(instance.getId(), testSetup.getClient()));
 	}
 
 	@Test(expected = DeltaCloudClientException.class)
 	public void destroyThrowExceptionOnUnknowInstanceId() throws DeltaCloudClientException {
 		testSetup.getClient().destroyInstance("dummy");
 	}
 
 	@Test
 	public void canShutdownInstance() throws DeltaCloudClientException {
 		Instance testInstance = testSetup.getTestInstance();
 		DeltaCloudClient client = testSetup.getClient();
 		client.shutdownInstance(testInstance.getId());
 		testInstance = client.listInstances(testInstance.getId()); // reload!
 		assertEquals(State.STOPPED, testInstance.getState());
 	}
 
 	@Test
 	public void canStartInstance() throws DeltaCloudClientException {
 		Instance testInstance = testSetup.getTestInstance();
 		DeltaCloudClient client = testSetup.getClient();
 		if (testInstance.getState() == State.RUNNING) {
 			client.shutdownInstance(testInstance.getId());
 		}
 		client.startInstance(testInstance.getId());
		testInstance = client.listInstances(testInstance.getId());
 		assertEquals(State.RUNNING, testInstance.getState());
 	}
 }
