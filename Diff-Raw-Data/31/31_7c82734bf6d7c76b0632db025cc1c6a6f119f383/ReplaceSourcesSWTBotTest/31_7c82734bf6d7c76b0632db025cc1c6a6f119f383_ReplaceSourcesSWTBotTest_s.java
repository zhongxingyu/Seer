 package org.fedoraproject.eclipse.packager.tests.git.swtbot;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
 import org.eclipse.swtbot.swt.finder.SWTBot;
 import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
 import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
 import org.eclipse.swtbot.swt.finder.waits.Conditions;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 import org.fedoraproject.eclipse.packager.tests.git.utils.GitTestProject;
 import org.fedoraproject.eclipse.packager.tests.utils.swtbot.ContextMenuHelper;
 import org.fedoraproject.eclipse.packager.tests.utils.swtbot.PackageExplorer;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
  
 @RunWith(SWTBotJunit4ClassRunner.class)
 public class ReplaceSourcesSWTBotTest {
  
 	private static SWTWorkbenchBot bot;
 	private GitTestProject efpProject;
 	
 	// the successful upload test requires ~/.fedora.cert
 	private File fedoraCert = new File(System.getProperty("user.home") + 
 		IPath.SEPARATOR + ".fedora.cert");
 	private File tmpExistingFedoraCert; // temporary reference to already existing ~/.fedora.cert
 	private boolean fedoraCertExisted = false;
 	
 	// Filenames used for this test
 	private final String EMPTY_FILE_NAME_VALID = "REMOVE_ME.tar";
 	private final String NON_EMPTY_FILE_NAME_INVALID = "REMOVE_ME.exe";
 	private final String VALID_SOURCE_FILENAME_NON_EMPTY = "REMOVE_ME.tar.bz2";
  
 	@BeforeClass
 	public static void beforeClass() throws Exception {
 		bot = new SWTWorkbenchBot();
 		try {
 			bot.viewByTitle("Welcome").close();
 			// hide Subclipse Usage stats popup if present/installed
 			bot.shell("Subclipse Usage").activate();
 			bot.button("Cancel").click();
 		} catch (WidgetNotFoundException e) {
 			// ignore
 		}
 		// Make sure we have the Package Explorer view open and shown
 		PackageExplorer.openView();
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		// Import eclipse-fedorapackager
 		efpProject = new GitTestProject("eclipse-fedorapackager");
 		IResource efpSpec = efpProject.getProject().findMember(new Path("eclipse-fedorapackager.spec"));
 		assertNotNull(efpSpec);
 	}
  
 	/**
 	 * Upload Sources test (Replace existing sources) with empty
 	 * source file.
 	 * 
 	 * @throws Exception
 	 */
 	@SuppressWarnings({ "static-access", "unchecked" })
 	@Test
 	public void cannotUploadEmptySourceFileReplaceSourcesHandler() throws Exception {
 		// Create empty source file and try to upload
 		IResource emptySourceFile;
 		emptySourceFile = createNewFile(EMPTY_FILE_NAME_VALID, null);
 		assertNotNull(emptySourceFile);
 		
 		SWTBotTree packagerTree = PackageExplorer.getTree();
 		
 		// Select source file
 		final SWTBotTreeItem efpItem = PackageExplorer.getProjectItem(packagerTree,
 				"eclipse-fedorapackager");
 		efpItem.expand();
     	efpItem.select(EMPTY_FILE_NAME_VALID);
 
 		// Click on file and try to upload
 		clickOnReplaceExistingSources(packagerTree);
 		// Wait for error to pop up
 		bot.waitUntil(Conditions.shellIsActive("Fedora Packager"));
 		SWTBotShell efpErrorWindow = bot.shell("Fedora Packager");
 		assertNotNull(efpErrorWindow);
 		SWTBot errorDialogBot = efpErrorWindow.bot();
 		// Get widget with expected error message
 		Widget errorMessageWidget = errorDialogBot.widget(
 				WidgetMatcherFactory.allOf(
 				WidgetMatcherFactory.withText(
						NLS.bind(org.fedoraproject.eclipse.packager.Messages.
 								uploadHandler_invalidFile, EMPTY_FILE_NAME_VALID))));
 		assertNotNull(errorMessageWidget);
 		efpErrorWindow.close();
 	}
 	
 	/**
 	 * Upload Sources test (Replace existing sources) with non-empty
 	 * source file which has an invalid extension.
 	 * 
 	 * @throws Exception
 	 */
 	@SuppressWarnings({ "static-access", "unchecked" })
 	@Test
 	public void cannotUploadNonEmptySourceFileWithInvalidExtensionReplaceSourcesHandler() throws Exception {
 		// Create non-empty, invalid source file in project
 		IResource invalidSourceFile;
 		invalidSourceFile = createNewFile(NON_EMPTY_FILE_NAME_INVALID, 0x900);
 		assertNotNull(invalidSourceFile);
 		
 		SWTBotTree packagerTree = PackageExplorer.getTree();
 		
 		// Select source file
 		final SWTBotTreeItem efpItem = PackageExplorer.getProjectItem(packagerTree,
 		"eclipse-fedorapackager");
 		efpItem.expand();
 		efpItem.select(NON_EMPTY_FILE_NAME_INVALID);
 		
 		// Click on file and try to upload
 		clickOnReplaceExistingSources(packagerTree);
 		// Wait for error to pop up
 		bot.waitUntil(Conditions.shellIsActive("Fedora Packager"));
 		SWTBotShell efpErrorWindow = bot.shell("Fedora Packager");
 		assertNotNull(efpErrorWindow);
 		SWTBot errorDialogBot = efpErrorWindow.bot();
 		// Get widget with expected error message
 		Widget errorMessageWidget = errorDialogBot.widget(
 				WidgetMatcherFactory.allOf(
 				WidgetMatcherFactory.withText(
						NLS.bind(org.fedoraproject.eclipse.packager.Messages.
 								uploadHandler_invalidFile, NON_EMPTY_FILE_NAME_INVALID))));
 		assertNotNull(errorMessageWidget);
 		efpErrorWindow.close();
 	}
 	
 	/**
 	 * Upload Sources test (Replace existing sources) with valid source
 	 * file. This assumes a valid ~/.fedora.cert_tests is present or
 	 * system property "eclipseFedoraPackagerTestsCertificate" is set to
 	 * the path to a valid .fedora.cert. The latter takes precedence.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void canUploadValidSourceFileReplaceSourcesHandler() throws Exception {
 		
 		// Set up .fedora.cert, return may be null
 		tmpExistingFedoraCert = setupFedoraCert();
 		
 		// Create valid source file in project
 		IResource validSourceFile;
 		validSourceFile = createNewFile(VALID_SOURCE_FILENAME_NON_EMPTY, 0x90);
 		assertNotNull(validSourceFile);
 		
 		// Set up expectations
 		final String newSourcesContent = "fcd3dfe8777d16d64235bc7ae6bdcb8a  " + VALID_SOURCE_FILENAME_NON_EMPTY + "\n";
 		final String newLineGitIgnore = VALID_SOURCE_FILENAME_NON_EMPTY + "\n";
 		final String gitIgnoreBefore = readGitIgnore();
 		
 		SWTBotTree packagerTree = PackageExplorer.getTree();
 		
 		// Select source file to be uploaded
 		final SWTBotTreeItem efpItem = PackageExplorer.getProjectItem(packagerTree,
 		"eclipse-fedorapackager");
 		efpItem.expand();
 		efpItem.select(VALID_SOURCE_FILENAME_NON_EMPTY);
 		
 		// Click on file and try to upload
 		clickOnReplaceExistingSources(packagerTree);
 		// Wait for upload process to start
 		bot.waitUntil(Conditions.shellIsActive(org.fedoraproject.
				eclipse.packager.Messages.newSourcesHandler_jobName));
 		SWTBotShell efpUploadWindow = bot.shell(org.fedoraproject.
				eclipse.packager.Messages.newSourcesHandler_jobName);
 		assertNotNull(efpUploadWindow);
 		// Wait for upload process to finish
 		bot.waitUntil(Conditions.shellCloses(efpUploadWindow));
 		
 		// Assert success
 		final String sourcesFileAfter = readSourcesFile();
 		final String gitIgnoreAfter = readGitIgnore();
 		assertEquals(
 				newSourcesContent,
 				sourcesFileAfter
 		);
 		assertEquals(
 				(gitIgnoreBefore + newLineGitIgnore),
 				gitIgnoreAfter
 		);
 		// Make sure sources file is still around
 		IResource newSource = efpProject.getProject().findMember(new Path(VALID_SOURCE_FILENAME_NON_EMPTY));
 		assertNotNull(newSource);
 		
 		// reestablish .fedora.cert handled by tearDown() to make sure
 		// cert files are cleaned up properly
 	}
  
 	@After
 	public void tearDown() throws Exception {
 		this.efpProject.dispose();
 		// clean up some temp .fedora.cert
 		if (tmpExistingFedoraCert != null) {
 			reestablishFedoraCert();
 		}
 		// remove ~/.fedora.cert if it didn't exist
 		if (!fedoraCertExisted && fedoraCert.exists()) {
 			fedoraCert.delete();
 		}
 	}
 	
 	/**
 	 * Context menu click helper. Click on "Replace existing sources".
 	 * 
 	 * @param Tree of Package Explorer view.
 	 * @throws Exception
 	 */
 	private void clickOnReplaceExistingSources(SWTBotTree packagerTree) throws Exception {
 		String subMenu = "Upload This File";
 		String menuItem = "Replace existing sources";
 		ContextMenuHelper.clickContextMenu(packagerTree, "Fedora Packager",
 				subMenu, menuItem);
 	}
 	
 	/**
 	 * Create a file with given name in project as created in setUp().
 	 * 
 	 * @param name Name of newly created file.
 	 * @param contents Null or integer contents of file.
 	 * @return A reference to the newly created file.
 	 * @throws IOException
 	 * @throws CoreException
 	 */
 	private IResource createNewFile(String name, Integer contents) throws IOException, CoreException {
 		IProject project = efpProject.getProject();
 		File newSource = new File(project.getLocation().toOSString() +
 				IPath.SEPARATOR + name);
 		newSource.createNewFile();
 		if (contents != null) {
 			FileWriter out = new FileWriter(newSource);
 			out.write(contents);
 			out.close();
 		}
 		project.refreshLocal(IResource.DEPTH_INFINITE, null);
 		IResource result = project.findMember(name);
 		if (result == null) {
 			throw new IOException("Could not create file: '" + name +
 					"' for test.");
 		}
 		return result;
 	}
 	
 	/**
 	 * Convenience for readFile("sources").
 	 * 
 	 * @return Contents of sources file.
 	 */
 	private String readSourcesFile() {
 		return readFile("sources");
 	}
 	
 	/**
 	 * Convenience method for readfile(".gitignore").
 	 * 
 	 * @return Contents of .gitignore file.
 	 */
 	private String readGitIgnore() {
 		return readFile(".gitignore");
 	}
 	
 	/**
 	 * Read contents of a text file and return its contents
 	 * as a String.
 	 * 
 	 * @param name The name of the file to read from.
 	 * @return The contents of that file.
 	 */
 	private String readFile(String name) {
 		StringBuffer result = new StringBuffer();
 		File file = efpProject.getProject().findMember(name).getLocation().toFile();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String line;
 			do {
 				line = br.readLine();
 				if (line != null) {
 					result.append(line + "\n");
 				}
 			} while (line != null);
 			br.close();
 		} catch (IOException e) {
 			fail("Could not read from file " + name);
 		}
 		return result.toString();
 	}
 	
 	/**
 	 * We need a valid .fedora.cert for doing a successful upload.
 	 * This will first look for a system property called
 	 * "eclipseFedoraPackagerTestsCertificate" and if set uses the
 	 * file pointed to by it as ~/.fedora.cert. If this property
 	 * is not set, ~/.fedora.cert_tests will be used instead. If
 	 * nothing succeeds, fail.
 	 * 
 	 * @return A File handle to a copy of an already existing
 	 * 			~/.fedora.cert or null if there wasn't any.
 	 */
 	private File setupFedoraCert() {
 		// move away potentially existing ~/.fedora.cert
 		File oldFedoraCertMovedAway = null;
 		if (fedoraCert.exists()) {
 			fedoraCertExisted = true;
 			try {
 				oldFedoraCertMovedAway = File.createTempFile(".fedora", ".cert");
 				FileInputStream fsIn = new FileInputStream(fedoraCert);
 				FileOutputStream fsOut = new FileOutputStream(oldFedoraCertMovedAway);
 				int buf;
 				// copy stuff
 				while ( (buf = fsIn.read()) != -1 ) {
 					fsOut.write(buf);
 				}
 				fsIn.close();
 				fsOut.close();
 			} catch (IOException e) {
 				fail("Unable to setup test: (~/.fedora.cert)!");
 			}
 		}
 		// Use template cert to copy to ~/.fedora.cert
 		String certTemplatePath = System.getProperty("eclipseFedoraPackagerTestsCertificate");
 		if (certTemplatePath == null) {
 			// try ~/.fedora.cert_tests
 			File fedoraCertTests = new File(System.getProperty("user.home") +
 					IPath.SEPARATOR + ".fedora.cert_tests");
 			if (fedoraCertTests.exists()) {
 				certTemplatePath = fedoraCertTests.getAbsolutePath();
 			} else {
 				// can't continue - fail
 				fail("System property \"eclipseFedoraPackagerTestsCertificate\" " +
 						"needs to be configured or ~/.fedora.cert_tests be present" +
 						" in order for this test to work.");
 			}
 		}
 		// certTemplatePath must not be null at this point
 		assertNotNull(certTemplatePath);
 		
 		// Copy things over
 		File fedoraCertTests = new File(certTemplatePath);
 		try {
 			FileInputStream fsIn = new FileInputStream(fedoraCertTests);
 			FileOutputStream fsOut = new FileOutputStream(fedoraCert);
 			int buf;
 			// copy stuff
 			while ( (buf = fsIn.read()) != -1 ) {
 				fsOut.write(buf);
 			}
 			fsIn.close();
 			fsOut.close();
 		} catch (IOException e) {
 			fail("Unable to setup test: (~/.fedora.cert)!");
 		}
 		
 		// if there was a ~/.fedora.cert return a File handle to it,
 		// null otherwise
 		if (fedoraCertExisted) {
 			return oldFedoraCertMovedAway;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Reestablish moved away ~/.fedora.cert
 	 * 
 	 * @param oldFedoraCertMovedAway The File handle to a copy of ~/.fedora.cert
 	 * 								 before any tests were run.
 	 */
 	private void reestablishFedoraCert() {
 		// Do this only if old file still exists
 		if (tmpExistingFedoraCert.exists()) {
 			try {
 				FileInputStream fsIn = new FileInputStream(tmpExistingFedoraCert);
 				FileOutputStream fsOut = new FileOutputStream(fedoraCert);
 				int buf;
 				// copy stuff
 				while ( (buf = fsIn.read()) != -1 ) {
 					fsOut.write(buf);
 				}
 				fsIn.close();
 				fsOut.close();
 				// remove temorary file
 				tmpExistingFedoraCert.delete();				
 			} catch (IOException e) {
 				fail("copying back ~/.fedora.cert");
 			}
 		}
 	}
  
 }
