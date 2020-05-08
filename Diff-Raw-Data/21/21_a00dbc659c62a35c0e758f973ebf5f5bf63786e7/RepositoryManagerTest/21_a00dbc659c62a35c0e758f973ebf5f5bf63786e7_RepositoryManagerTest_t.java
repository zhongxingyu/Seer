 package org.eclipse.swordfish.p2.internal.deploy.server;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.*;
 import static org.easymock.EasyMock.*;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.net.URL;
 
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.junit.Before;
 import org.junit.Test;
 import static org.eclipse.swordfish.p2.internal.deploy.server.ObjectFactory.*;
 
 public class RepositoryManagerTest {
 	private static final String VALID_REPO = "org/eclipse/swordfish/p2/internal/deploy/server/ValidRepository.zip";
 	private static final String INVALID_REPO = "org/eclipse/swordfish/p2/internal/deploy/server/InvalidRepository.zip";
 	private RepositoryManager repoMan;
 
 	@Before
 	public void setUp() throws Exception {
 		repoMan = new RepositoryManager();
 	}
 
 	
 	/**
 	 * Verify that without a provisioning agent we get an IllegalStateException when calling the load method.
 	 */
 	@Test
 	public void testRequireProvisioningAgent() 
 		throws ProvisionException, OperationCanceledException, FileNotFoundException, IOException {
 		assertThat(getExceptionThrownOnLoad(repoMan, "", null), is(IllegalStateException.class));
 		assertThat(getExceptionThrownOnRemove(repoMan, null), is(IllegalStateException.class));
 
 		repoMan.setProvisioningAgent(createProvisioningAgent(null, null));
 		assertThat(getExceptionThrownOnLoad(repoMan, "", null), is(IllegalStateException.class));
 		assertThat(getExceptionThrownOnRemove(repoMan, null), is(IllegalStateException.class));
 
 		repoMan.setProvisioningAgent(createProvisioningAgent(createMetadataManager(false, false), null));
 		assertThat(getExceptionThrownOnLoad(repoMan, "", null), is(IllegalStateException.class));
 		assertThat(getExceptionThrownOnRemove(repoMan, null), is(IllegalStateException.class));
 	}
 
 
 	/**
 	 * Verify that a null stream causes a NullpointerException
 	 */
	@Test(expected = IllegalArgumentException.class)
 	public void testLoadNullRepo() 
 		throws ProvisionException, OperationCanceledException, FileNotFoundException, IOException {
 		repoMan.setProvisioningAgent(createProvisioningAgent(createMetadataManager(), createArtifactManager()));
 		repoMan.load("testLoadNullRepo", null);
 	}
 
 
 	/**
 	 * Verify that an invalid Zip file causes an IOException.
 	 */
 	@Test(expected = IOException.class)
 	public void testLoadInvalidRepo() 
 		throws ProvisionException, OperationCanceledException, FileNotFoundException, IOException {
 		repoMan.setProvisioningAgent(createProvisioningAgent(createMetadataManager(), createArtifactManager()));
 		InputStream in = RepositoryManagerTest.class.getClassLoader().getResourceAsStream(INVALID_REPO);
 		repoMan.load("testLoadInvalidRepo", in);
 	}
 
 	
 	/**
 	 * When the repository can be loaded, there'll be no exception.
 	 */
 	@Test
 	public void testLoadValidRepo()
 		throws ProvisionException, OperationCanceledException, FileNotFoundException, IOException {
 		repoMan.setProvisioningAgent(createProvisioningAgent(createMetadataManager(true, false)
 									, createArtifactManager(true, false)));
 		InputStream in = RepositoryManagerTest.class.getClassLoader().getResourceAsStream(VALID_REPO);
 		repoMan.load("testLoadValidRepo", in);		
 	}
 	
 	
 	/**
 	 * Check that removing a repository means that we we call to remove metadata and artifact repository
 	 */
 	@Test
 	public void testRemoveRepo() throws ProvisionException, OperationCanceledException, URISyntaxException {
 		repoMan.setProvisioningAgent(createProvisioningAgent(createMetadataManager(false, true)
 				, createArtifactManager(false, true)));
 		assertTrue(repoMan.remove(new URI(VALID_REPO)));		
 	}
 
 	
 	private Throwable getExceptionThrownOnLoad(RepositoryManager rm, String name, InputStream in) {
 		Throwable throwable = null;
 		
 		try {
 			rm.load(name, in);
 		} catch(Throwable t) {
 			throwable = t;
 		}
 		
 		return throwable;
 	}
 
 	
 	private Throwable getExceptionThrownOnRemove(RepositoryManager rm, URI uri) {
 		Throwable throwable = null;
 
 		try {
 			rm.remove(uri);
 		} catch(Throwable t) {
 			throwable = t;
 		}
 		
 		return throwable;
 	}
 }
