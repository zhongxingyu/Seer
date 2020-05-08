 package org.padacore.core.project.test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 import org.junit.After;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.padacore.core.project.ResourceLocator;
 import org.padacore.core.test.utils.CommonTestUtils;
 
 public class ResourceLocatorTest {
 
 	private Fixture fixture;
 
 	private class Fixture {
 		public IProject project;
 		public ResourceLocator sut;
 	}
 
 	@Rule
 	public TemporaryFolder testFolder = new TemporaryFolder();
 
 	public void createFixture(boolean projectIsImported) {
 		this.fixture = new Fixture();
 		this.fixture.project = CommonTestUtils.CreateAdaProjectAt(new Path(
 				this.testFolder.getRoot().getAbsolutePath()));
 		this.fixture.sut = new ResourceLocator(this.fixture.project);
 
 		CommonTestUtils.CreateGprFileIn(this.fixture.project.getRawLocation(),
 				this.fixture.project.getName());
 
 		String projectKindPpty = "";
 
 		try {
 			if (projectIsImported) {
 				projectKindPpty = "imported";
 				this.fixture.project.getFolder("toto").create(true, true, null);
 			} else {
 				projectKindPpty = "created";
 			}
 
 			this.fixture.project.setPersistentProperty(new QualifiedName(
 					CommonTestUtils.SESSION_PROPERTY_QUALIFIED_NAME_PREFIX,
 					"projectKind"), projectKindPpty);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@After
 	public void tearDown() {
 		try {
 			this.fixture.project.delete(true, null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testForImportedProject() {
 		this.createFixture(true);
 
 		this.runTestForSimpleFile("testFile.txt", true);
 		this.runTestForSimpleFolder("testFolder", true);
 		this.runTestForComplexFile(new Path("testFolder/testFile.exe"), true);
 	}
 
 	private void runTestForSimpleFile(String fileName, boolean projectIsImported) {
 		IFile fileInProject;
 
 		if (projectIsImported) {
 			fileInProject = this.fixture.project.getFolder("toto").getFile(
 					fileName);
 		} else {
 			fileInProject = this.fixture.project.getFile(fileName);
 		}
 
 		this.createFile(fileInProject);
 		this.checkResourceWasFound(new Path(fileName));
 	}
 
 	private void checkResourceWasFound(IPath relativeResourcePath) {
 		IPath absoluteResourcePath = new Path(this.testFolder.getRoot()
 				.getAbsolutePath()).append(relativeResourcePath);
 
 		IResource retrievedResource = this.fixture.sut
 				.findResourceFromPath(absoluteResourcePath);
 
 		assertTrue(retrievedResource.getLocation().equals(
 				retrievedResource.getLocation()));
 	}
 
 	private void runTestForSimpleFolder(String folderName,
 			boolean projectIsImported) {
 		IFolder folderInProject;
 		if (projectIsImported) {
 			folderInProject = this.fixture.project.getFolder("toto").getFolder(
 					folderName);
 		} else {
 			folderInProject = this.fixture.project.getFolder(folderName);
 		}
 		try {
 			folderInProject.create(true, true, null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 		this.checkResourceWasFound(new Path(folderName));
 
 	}
 
 	private void createFile(IFile file) {
 		try {
 			file.create(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4 }),
 					true, null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void runTestForComplexFile(IPath relativePath,
 			boolean projectisImported) {
 		IFile fileInProject;
 
 		if (projectisImported) {
 			fileInProject = this.fixture.project.getFolder("toto").getFile(relativePath);
 		} else {
 			fileInProject = this.fixture.project.getFile(relativePath);
 		}
 		this.createFile(fileInProject);
 		this.checkResourceWasFound(relativePath);
 	}
 
 	@Test
 	public void testForCreatedProject() {
 		this.createFixture(false);
 
 		this.runTestForSimpleFile("testFile.txt", false);
 		this.runTestForSimpleFolder("testFolder", false);
 		this.runTestForComplexFile(new Path("testFolder/testFile.exe"), false);
 	}
 }
