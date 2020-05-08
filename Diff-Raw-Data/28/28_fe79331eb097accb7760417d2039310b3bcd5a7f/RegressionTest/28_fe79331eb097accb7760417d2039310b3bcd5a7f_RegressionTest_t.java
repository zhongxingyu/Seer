 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Scanner;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 import org.jboss.reddeer.swt.api.TreeItem;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.handler.ShellHandler;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.ctab.DefaultCTabItem;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
 import org.jboss.reddeer.swt.impl.menu.ShellMenu;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
 import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
import org.jboss.tools.fuse.reddeer.editor.CamelEditor;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
 import org.jboss.tools.fuse.reddeer.preference.ServerRuntimePreferencePage;
 import org.jboss.tools.fuse.reddeer.projectexplorer.CamelProject;
 import org.jboss.tools.fuse.reddeer.utils.ResourceHelper;
 import org.jboss.tools.fuse.reddeer.view.JMXNavigator;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
 import org.junit.After;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * Class contains test cases verifying resolved issues
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(FuseIntegrationPerspective.class)
 @RunWith(RedDeerSuite.class)
 @Server(type = ServerReqType.Fuse, state = ServerReqState.PRESENT)
 public class RegressionTest {
 
 	@InjectRequirement
 	private ServerRequirement serverRequirement;
 
 	@After
 	public void clean() {
 
 		new ProjectExplorer().deleteAllProjects();
 		ShellHandler.getInstance().closeAllNonWorbenchShells();
 	}
 
 	/**
 	 * GUI editor issue when using route scoped onException
 	 * https://issues.jboss.org/browse/FUSETOOLS-674
 	 * 
 	 * NOTE: not fixed yet - deferred to 8.0
 	 */
 	@Ignore
 	@Test
 	public void issue_674() throws ParserConfigurationException, SAXException, IOException {
 
 		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
 		new CamelProject("camel-spring").openCamelContext("camel-context.xml");
 		new DefaultCTabItem("Source").activate();
 
 		// copy sample of camel-context.xml
 		File testFile = new File(ResourceHelper.getResourceAbsolutePath(Activator.PLUGIN_ID,
 				"resources/camel-context.xml"));
 		DefaultStyledText editor = new DefaultStyledText();
 		Scanner scanner = new Scanner(testFile);
 		scanner.useDelimiter("\\Z");
 		editor.setText(scanner.next());
 		scanner.close();
 
 		new DefaultCTabItem("Design").activate();
 		new DefaultCTabItem("Source").activate();
 		new ShellMenu("File", "Save").select();
 
 		// check XML content
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		InputSource is = new InputSource(new StringReader(new DefaultStyledText().getText()));
 		Document doc = builder.parse(is);
 		int i = doc.getElementsByTagName("onException").item(0).getChildNodes().getLength();
 
 		assertEquals("'camel-context.xml' file was changed!", 11, i);
 	}
 
 	/**
 	 * New Server Runtime Wizard - Cancel/Finish button error
 	 * https://issues.jboss.org/browse/FUSETOOLS-1067
 	 * 
 	 * NOTE: this test is related to https://issues.jboss.org/browse/FUSETOOLS-1076 too
 	 */
 	@Test
 	public void issue_1067() {
 
 		new ServerRuntimePreferencePage().open();
 
 		new PushButton("Add...").click();
 		new DefaultShell("New Server Runtime Environment").setFocus();
 		new DefaultTreeItem("JBoss Fuse").expand();
 
 		// tests the _Finish_ button
 		for (TreeItem item : new DefaultTreeItem("JBoss Fuse").getItems()) {
 			if (!item.getText().startsWith("JBoss"))
 				continue;
 			AbstractWait.sleep(TimePeriod.SHORT);
 			item.select();
 			try {
 
 				assertFalse(new PushButton("Finish").isEnabled());
 			} catch (AssertionError ex) {
 
 				new DefaultTreeItem("JBoss Fuse").select();
 				AbstractWait.sleep(TimePeriod.SHORT);
 				new PushButton("Cancel").click();
 				AbstractWait.sleep(TimePeriod.NORMAL);
 				new DefaultShell().close();
 				throw ex;
 			}
 		}
 
 		// tests the _Cancel_ button
 		AbstractWait.sleep(TimePeriod.SHORT);
 		new DefaultTreeItem("JBoss Fuse", serverRequirement.getConfig().getServerBase().getName()).select();
 		new PushButton("Cancel").click();
 		try {
 
 			assertTrue(new DefaultShell().getText().equals("Preferences"));
 		} catch (AssertionError ex) {
 
 			new DefaultShell().close();
 			new DefaultTreeItem("JBoss Fuse").select();
 			AbstractWait.sleep(TimePeriod.SHORT);
 			new PushButton("Cancel").click();
 			AbstractWait.sleep(TimePeriod.NORMAL);
 			new DefaultShell().close();
 			throw ex;
 		}
 	}
 
 	/**
 	 * New Server Runtime Wizard - Finish button error
 	 * https://issues.jboss.org/browse/FUSETOOLS-1076
 	 */
 	@Test
 	public void issue_1076() {
 
 		ServerRuntimePreferencePage serverRuntime = new ServerRuntimePreferencePage();
 		serverRuntime.open();
 		new PushButton("Add...").click();
 		new DefaultShell("New Server Runtime Environment").setFocus();
 		new DefaultTreeItem("JBoss Fuse", "JBoss Fuse 6.1").select();
 		if (new PushButton("Finish").isEnabled()) {
 			new PushButton("Cancel").click();
 			new DefaultShell("Preferences").close();
 			fail("'Finish' button should not be enabled!");
 		}
 	}
 
 	/**
 	 * JMX Navigator - prevent from close Camel Context
 	 * https://issues.jboss.org/browse/FUSETOOLS-1115
 	 */
 	@Test
 	public void issue_1115() {
 
 		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
 		new CamelProject("camel-spring").runCamelContext("camel-context.xml");
 		new JMXNavigator().getNode("Local Camel Context", "Camel", "camel").select();
 
 		try {
 			new ContextMenu("Close Camel Context");
 		} catch (SWTLayerException ex) {
 			return;
 		} finally {
 			new ConsoleView().terminateConsole();
 		}
 
 		fail("Context menu item 'Close Camel Context' is available!");
 	}
 
 	/**
 	 * New Fuse Project - Finish button
 	 * https://issues.jboss.org/browse/FUSETOOLS-1149
 	 */
 	@Test
 	public void issue_1149() {
 
 		new ShellMenu("File", "New", "Fuse Project").select();
 		new DefaultShell("New Fuse Project");
 		if (new PushButton("Finish").isEnabled()) {
 			new DefaultShell().close();
 			fail("'Finish' button should not be enabled!");
 		}
 	}

	/**
	 * remove use of the customId attribute
	 * https://issues.jboss.org/browse/FUSETOOLS-1172
	 */
	@Test
	public void issue_1172() throws ParserConfigurationException, SAXException, IOException {

		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
		new CamelProject("camel-spring").openCamelContext("camel-context.xml");
		CamelEditor.switchTab("Design");
		CamelEditor editor = new CamelEditor("camel-context.xml");
		editor.click(5, 5);
		editor.setProperty("Id", "1");
		editor.save();
		CamelEditor.switchTab("Source");

		// check XML content
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(new DefaultStyledText().getText()));
		Document doc = builder.parse(is);
		assertNull(doc.getElementsByTagName("route").item(0).getAttributes().getNamedItem("customId"));
	}
 }
