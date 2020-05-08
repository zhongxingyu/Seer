 package org.kalibro.core.model.enums;
 
 import static org.junit.Assert.*;
 import static org.kalibro.core.model.enums.RepositoryType.*;
 import static org.mockito.Mockito.verify;
 import static org.powermock.api.mockito.PowerMockito.*;
 import static org.powermock.reflect.Whitebox.*;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.EnumerationTestCase;
 import org.kalibro.core.concurrent.Task;
 import org.kalibro.core.loaders.*;
 import org.kalibro.core.model.Repository;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(RepositoryType.class)
 public class RepositoryTypeTest extends EnumerationTestCase<RepositoryType> {
 
 	@Override
 	protected Class<RepositoryType> enumerationClass() {
 		return RepositoryType.class;
 	}
 
 	@Override
 	protected String expectedText(RepositoryType type) {
 		return (type == CVS) ? "CVS" : super.expectedText(type);
 	}
 
 	@After
 	public void setUp() {
 		setInternalState(GIT, ProjectLoader.class, new GitLoader());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldRetrieveSupportedTypes() {
 		RepositoryType supported = mockType(true);
 		RepositoryType unsupported = mockType(false);
 		spy(RepositoryType.class);
 		when(RepositoryType.values()).thenReturn(new RepositoryType[]{supported, unsupported});
 		assertDeepSet(RepositoryType.supportedTypes(), supported);
 	}
 
 	private RepositoryType mockType(boolean supported) {
 		RepositoryType type = mock(RepositoryType.class);
 		when(type.isSupported()).thenReturn(supported);
 		return type;
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldRetrieveIfIsLocal() {
 		assertTrue(LOCAL_DIRECTORY.isLocal());
 		assertTrue(LOCAL_TARBALL.isLocal());
 		assertTrue(LOCAL_ZIP.isLocal());
 
 		assertFalse(BAZAAR.isLocal());
 		assertFalse(CVS.isLocal());
 		assertFalse(GIT.isLocal());
 		assertFalse(MERCURIAL.isLocal());
 		assertFalse(REMOTE_TARBALL.isLocal());
 		assertFalse(REMOTE_ZIP.isLocal());
 		assertFalse(SUBVERSION.isLocal());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldProvideLoader() {
 		assertClassEquals(LocalDirectoryLoader.class, getLoader(LOCAL_DIRECTORY));
 		assertClassEquals(LocalTarballLoader.class, getLoader(LOCAL_TARBALL));
 		assertClassEquals(LocalZipLoader.class, getLoader(LOCAL_ZIP));
 
 		assertClassEquals(BazaarLoader.class, getLoader(BAZAAR));
 		assertClassEquals(CvsLoader.class, getLoader(CVS));
 		assertClassEquals(GitLoader.class, getLoader(GIT));
 		assertClassEquals(MercurialLoader.class, getLoader(MERCURIAL));
 		assertClassEquals(RemoteTarballLoader.class, getLoader(REMOTE_TARBALL));
 		assertClassEquals(RemoteZipLoader.class, getLoader(REMOTE_ZIP));
 		assertClassEquals(SubversionLoader.class, getLoader(SUBVERSION));
 	}
 
 	private ProjectLoader getLoader(RepositoryType type) {
 		return getInternalState(type, "loader");
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldRetrieveAuthenticationSupport() {
 		ProjectLoader loader = mockGitLoader();
 		when(loader.supportsAuthentication()).thenReturn(false);
 		assertFalse(GIT.supportsAuthentication());
 
 		when(loader.supportsAuthentication()).thenReturn(true);
 		assertTrue(GIT.supportsAuthentication());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldValidateProjectLoader() {
 		ProjectLoader loader = mockGitLoader();
 		when(loader.validate()).thenReturn(true);
 		assertTrue(GIT.isSupported());
 		when(loader.validate()).thenReturn(false);
 		assertFalse(GIT.isSupported());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldLoadRepository() {
 		File directory = mock(File.class);
 		Repository repository = mock(Repository.class);
 		ProjectLoader loader = mockGitLoader();
 
 		GIT.load(repository, directory);
 		verify(loader).load(repository, directory);
 	}
 
 	private ProjectLoader mockGitLoader() {
 		ProjectLoader loader = mock(ProjectLoader.class);
 		setInternalState(GIT, ProjectLoader.class, loader);
 		return loader;
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldThrowKalibroErrorIfLoaderCouldNotBeCreated() {
 		final RepositoryType type = spy(GIT);
 		when(type.name()).thenReturn("INEXISTENT");
 		checkKalibroError(new Task() {
 
 			@Override
 			protected void perform() throws Throwable {
 				invokeMethod(type, "initializeLoader");
 			}
 		}, "Error creating loader for Git", ClassNotFoundException.class);
 	}
 }
