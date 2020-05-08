 package org.eclipse.emf.emfstore.client.test.common.extensionregistry;
 
import static org.junit.Assert.assertEquals;

import org.eclipse.emf.emfstore.internal.client.model.Configuration;
import org.eclipse.emf.emfstore.internal.client.model.util.ChecksumErrorHandler;
 import org.junit.Test;
 
 public class ExtensionRegistryTest {
 
 	@Test
 	public void testGetExistingExtensionPoin() {
 		// ESAbstractSessionProvider sessionProvider = ExtensionRegistry.INSTANCE.getType(
 		// "org.eclipse.emf.emfstore.client.usersessionProvider",
 		// "class",
 		// ESAbstractSessionProvider.class);
 		// assertNotNull(sessionProvider);
 	}
 
	@Test
	public void testGetDefaultErrorChecksumHandler() {
		assertEquals(ChecksumErrorHandler.LOG, Configuration.getClientBehavior().getChecksumErrorHandler());
	}
 }
