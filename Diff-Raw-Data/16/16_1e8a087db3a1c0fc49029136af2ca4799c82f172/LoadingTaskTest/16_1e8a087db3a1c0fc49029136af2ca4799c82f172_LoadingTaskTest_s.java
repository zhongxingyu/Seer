 package org.kalibro.core.processing;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileFilter;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.Repository;
 import org.kalibro.RepositoryType;
 import org.kalibro.core.loaders.*;
 import org.kalibro.tests.UnitTest;
 import org.mockito.ArgumentCaptor;
 import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.powermock.reflect.Whitebox;
 
 @RunWith(PowerMockRunner.class)
 @PrepareOnlyThisForTest({FileUtils.class, LoadingTask.class})
 public class LoadingTaskTest extends UnitTest {
 
 	private ProcessContext context;
 	private Repository repository;
 	private Loader loader;
 	private File loadDirectory, projectDirectory, repositoryDirectory;
 
 	private LoadingTask loadingTask;
 
 	@Before
 	public void setUp() throws Exception {
 		mockRepository();
 		mockDirectories();
 		loadingTask = spy(new LoadingTask(context));
 		mockLoader();
 	}
 
 	private void mockRepository() {
 		context = mock(ProcessContext.class);
 		repository = mock(Repository.class);
 		when(context.repository()).thenReturn(repository);
 		when(repository.getAddress()).thenReturn("LoadingTask repository address");
 	}
 
 	private void mockDirectories() {
 		loadDirectory = mock(File.class);
 		projectDirectory = mock(File.class);
 		repositoryDirectory = mock(File.class);
 		when(context.codeDirectory()).thenReturn(repositoryDirectory);
 		when(repositoryDirectory.getName()).thenReturn("RepositoryName-1");
 		when(repositoryDirectory.getParentFile()).thenReturn(projectDirectory);
		when(projectDirectory.mkdirs()).thenReturn(true);
 		when(projectDirectory.getName()).thenReturn("ProjectName-2");
 		when(projectDirectory.listFiles(any(FileFilter.class))).thenReturn(new File[0]);
 		when(projectDirectory.getParentFile()).thenReturn(loadDirectory);
		when(loadDirectory.mkdirs()).thenReturn(true);
 		when(loadDirectory.listFiles(any(FileFilter.class))).thenReturn(new File[0]);
 		mockStatic(FileUtils.class);
 	}
 
 	private void mockLoader() throws Exception {
 		loader = mock(Loader.class);
 		doReturn(loader).when(loadingTask, "createLoader");
 	}
 
 	@Test
 	public void shouldCreateParentDirectories() throws Exception {
 		loadingTask.perform();
 		verify(loadDirectory).mkdirs();
 		verify(projectDirectory).mkdirs();
 	}
 
 	@Test
 	public void shouldMakeSureParentDirectoriesAreCreated() {
 		shouldMakeSureIsCreated(projectDirectory);
 		shouldMakeSureIsCreated(loadDirectory);
 	}
 
 	private void shouldMakeSureIsCreated(File directory) {
		when(directory.mkdirs()).thenReturn(false);
 		assertThat(loadingTask).throwsException().withMessage("Could not create directory: " + directory);
 	}
 
 	@Test
 	public void shouldListFilesWithSameSuffix() throws Exception {
 		loadingTask.perform();
 		ArgumentCaptor<FileFilter> captor = ArgumentCaptor.forClass(FileFilter.class);
 
 		verify(loadDirectory).listFiles(captor.capture());
 		assertArrayEquals(new String[]{"-2"}, (String[]) Whitebox.getInternalState(captor.getValue(), "suffixes"));
 		verify(projectDirectory).listFiles(captor.capture());
 		assertArrayEquals(new String[]{"-1"}, (String[]) Whitebox.getInternalState(captor.getValue(), "suffixes"));
 	}
 
 	@Test
 	public void shouldDeleteAllInCaseOfAmbiguity() throws Exception {
 		File[] withSameSuffix = new File[]{projectDirectory, repositoryDirectory};
 		when(loadDirectory.listFiles(any(FileFilter.class))).thenReturn(withSameSuffix);
 		loadingTask.perform();
 
 		verifyStatic();
 		FileUtils.deleteQuietly(projectDirectory);
 		verifyStatic();
 		FileUtils.deleteQuietly(repositoryDirectory);
 	}
 
 	@Test
 	public void shouldUpdateNamesIfDirectoriesExist() throws Exception {
 		when(loadDirectory.listFiles(any(FileFilter.class))).thenReturn(new File[]{projectDirectory});
 		when(projectDirectory.listFiles(any(FileFilter.class))).thenReturn(new File[]{repositoryDirectory});
 		loadingTask.perform();
 
 		verify(projectDirectory).renameTo(projectDirectory);
 		verify(repositoryDirectory).renameTo(repositoryDirectory);
 	}
 
 	@Test
 	public void shouldTellLoaderToLoad() throws Exception {
 		loadingTask.perform();
 		verify(loader).load(repository.getAddress(), repositoryDirectory);
 	}
 
 	@Test
 	public void shouldDeleteIfExistsButIsNotDirectory() throws Exception {
 		shouldDeleteIfExistsButIsNotDirectory(projectDirectory);
 		shouldDeleteIfExistsButIsNotDirectory(repositoryDirectory);
 	}
 
 	private void shouldDeleteIfExistsButIsNotDirectory(File directory) throws Exception {
 		when(directory.exists()).thenReturn(true);
 		when(directory.isDirectory()).thenReturn(false);
 		loadingTask.perform();
 		verify(directory).delete();
 	}
 
 	@Test
 	public void shouldCreateCorrectLoader() throws Exception {
 		assertLoaderCreated(RepositoryType.BAZAAR, BazaarLoader.class);
 		assertLoaderCreated(RepositoryType.CVS, CvsLoader.class);
 		assertLoaderCreated(RepositoryType.GIT, GitLoader.class);
 		assertLoaderCreated(RepositoryType.LOCAL_DIRECTORY, LocalDirectoryLoader.class);
 		assertLoaderCreated(RepositoryType.LOCAL_TARBALL, LocalTarballLoader.class);
 		assertLoaderCreated(RepositoryType.LOCAL_ZIP, LocalZipLoader.class);
 		assertLoaderCreated(RepositoryType.MERCURIAL, MercurialLoader.class);
 		assertLoaderCreated(RepositoryType.REMOTE_TARBALL, RemoteTarballLoader.class);
 		assertLoaderCreated(RepositoryType.REMOTE_ZIP, RemoteZipLoader.class);
 		assertLoaderCreated(RepositoryType.SUBVERSION, SubversionLoader.class);
 	}
 
 	private void assertLoaderCreated(RepositoryType type, Class<? extends Loader> loaderClass) throws Exception {
 		when(repository.getType()).thenReturn(type);
 		doCallRealMethod().when(loadingTask, "createLoader");
 		assertClassEquals(loaderClass, Whitebox.invokeMethod(loadingTask, "createLoader"));
 	}
 }
