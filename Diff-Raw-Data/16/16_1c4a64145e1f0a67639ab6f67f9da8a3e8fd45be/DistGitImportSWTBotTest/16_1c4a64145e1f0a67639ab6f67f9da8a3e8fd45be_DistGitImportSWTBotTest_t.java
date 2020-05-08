 package org.fedoraproject.eclipse.packager.tests.git.swtbot;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
 import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
 import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
 import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
  
 @RunWith(SWTBotJunit4ClassRunner.class)
 public class DistGitImportSWTBotTest {
  
 	private static SWTWorkbenchBot	bot;
 	private IProject edProject;
 	
 	// the successful upload test requires ~/.fedora.cert
 	private static File fedoraCert = new File(System.getProperty("user.home") + 
 		IPath.SEPARATOR + ".fedora.cert");
 	private static File tmpExistingFedoraCert; // temporary reference to already existing ~/.fedora.cert
 	private static boolean fedoraCertExisted = false;
  
 	@BeforeClass
 	public static void beforeClass() throws Exception {
 		tmpExistingFedoraCert = moveAwayFedoraCert();
 		bot = new SWTWorkbenchBot();
 		try {
 			bot.viewByTitle("Welcome").close();
 		} catch (WidgetNotFoundException e) {
 			// ignore
 		}
 	}
  
 	/**
 	 * Basic functional test of import wizard. All cases should succeed.
 	 * 
 	 * @throws Exception
 	 */
 	@SuppressWarnings("static-access")
 	@Test
 	public void canImportNewFedoraDistGitProject() throws Exception {
 		bot.menu("File").menu("Import...").click();
  
 		SWTBotShell shell = bot.shell("Import");
 		shell.activate();
 		bot.tree().expandNode("Git").select("Projects from Fedora Git");
 		bot.button("Next >").click();
 		
 		// Import ed
 		SWTBotShell importDialog = bot.shell("Import from Fedora Git");
 		bot.textWithLabel("Package name:").setText("ed");
  
 		bot.button("Finish").click();
 		// Wait for import operation to finish
		SWTBotPreferences.TIMEOUT = 3 * 5000;
 		bot.waitUntil(Conditions.shellCloses(importDialog));
		SWTBotPreferences.TIMEOUT = 5000; // reset timeout
 		// Find newly created project
 		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 		edProject = (IProject)wsRoot.findMember(new Path("ed"));
 		assertNotNull(edProject);
 		IResource edSpecFile = edProject.findMember(new Path("ed.spec"));
 		assertNotNull(edSpecFile);
 	}
 	
 	/**
 	 * Try to import non-existent project. I don't know why the error
 	 * dialog does not show up during this test run... :-(
 	 * 
 	 * @throws Exception
 	 */
 	/*@SuppressWarnings("static-access")
 	@Test
 	public void testShowsProperErrorMessageForNotExistentDistGitProject() throws Exception {
 		bot = new SWTWorkbenchBot();
 		bot.menu("File").menu("Import...").click();
  
 		SWTBotShell shell = bot.shell("Import");
 		shell.activate();
 		bot.tree().expandNode("Git").select("Projects from Fedora Git");
 		bot.button("Next >").click();
 		
 		// Import something non-existent
 		bot.textWithLabel("Package name:").setText("not__there_repo");
 		bot.button("Finish").click();
 		// Get widget with expected error message
 		final Widget test = bot.widget(
 				WidgetMatcherFactory.allOf(
 				WidgetMatcherFactory.withRegex(".*" +
 						NLS.bind(org.fedoraproject.eclipse.packager.git.Messages.
 								fedoraCheckoutWizard_repositoryNotFound, "not__there_repo") +
 								".*" )));
 		// Wait for problem shell to appear
 		assertNotNull(test);
 		// FIXME: Assert that proper error message is shown
 	}*/
 	
 	@After
 	public void tearDown() {
		if (this.edProject == null) {
			// Test may have raised an exception before lookup would have
			// occured
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			edProject = (IProject) wsRoot.findMember(new Path("ed"));
		}
		// Delete potentially existing project
		if (edProject != null) {
 			try {
 				this.edProject.delete(true, new NullProgressMonitor());
 			} catch (CoreException e) {
 				// ignore
 			}
 		}
 	}
 	
 	@AfterClass
 	public static void cleanUp() {
 		if (fedoraCertExisted) {
 			copyBackFedoraCert();
 		}
 	}
 	
 	/**
 	 * We need to make sure that no ~/.fedora.cert is around. Otherwise
 	 * a non-anonymous clone is attempted and EGit, JSch rather, prompts
 	 * for credentials. We don't want this, so move ~/.fedora.cert out of
 	 * the way and put it back in place when the test ran.  
 	 * 
 	 * @return A File handle to a copy of an already existing
 	 * 			~/.fedora.cert or null if there wasn't any.
 	 */
 	private static File moveAwayFedoraCert() {
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
 				// delete ~/.fedora.cert we have a copy in tmp
 				fedoraCert.delete();
 			} catch (IOException e) {
 				fail("Unable to setup test: (~/.fedora.cert)!");
 			}
 		}		
 		// if there was a ~/.fedora.cert return a File handle to a copy of it,
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
 	private static void copyBackFedoraCert() {
 		// Do this only if old file is not null and exists
 		if (tmpExistingFedoraCert != null && tmpExistingFedoraCert.exists()) {
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
