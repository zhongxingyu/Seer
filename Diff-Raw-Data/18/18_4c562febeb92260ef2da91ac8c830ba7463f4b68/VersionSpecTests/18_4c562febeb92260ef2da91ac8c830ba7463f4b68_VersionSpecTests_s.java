 package org.eclipse.emf.emfstore.client.test.server.api;
 
 import static org.eclipse.emf.emfstore.client.test.server.api.HistoryAPITests.createHistory;
 import static org.eclipse.emf.emfstore.client.test.server.api.HistoryAPITests.versions;
 import static org.junit.Assert.assertEquals;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.InvalidVersionSpecException;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.Versions;
 import org.junit.Test;
 
 public class VersionSpecTests extends CoreServerTest {
 
 	@Test
 	public void resolvePrimary() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
 		assertEquals(versions[5], history.resolveVersionSpec(versions[5]));
 	}
 
 	@Test
 	public void resolveNearestPrimary() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
 		assertEquals(versions[5], history.resolveVersionSpec(Versions.createPRIMARY("b2", 6)));
 	}
 
 	@Test
 	public void resolvePrimaryGlobal() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
		assertEquals(versions[7], history.resolveVersionSpec(Versions.createPRIMARY(VersionSpec.GLOBAL, 5)));
 	}
 
 	@Test(expected = InvalidVersionSpecException.class)
 	public void resolvePrimaryInvalid() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
 		history.resolveVersionSpec(Versions.createPRIMARY("foo", 5));
 	}
 
 	@Test
 	public void resolveLocalHead() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
 		assertEquals(versions[5], history.resolveVersionSpec(Versions.createHEAD("b2")));
 	}
 
 	@Test(expected = InvalidVersionSpecException.class)
 	public void resolveIllegalHead() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
 		history.resolveVersionSpec(Versions.createHEAD("foobar"));
 	}
 
 	@Test
 	public void resolveGlobalHead() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
 		assertEquals(versions[7], history.resolveVersionSpec(Versions.createHEAD(VersionSpec.GLOBAL)));
 	}
 
 	@Test
 	public void resolveBranch() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
 		assertEquals(versions[5], history.resolveVersionSpec(Versions.createBRANCH("b2")));
 	}
 
 	@Test(expected = InvalidVersionSpecException.class)
 	public void resolveIllegalBranch() throws EmfStoreException {
 		ProjectSpace history = createHistory(this);
 
 		history.resolveVersionSpec(Versions.createBRANCH("foobar"));
 	}
 }
