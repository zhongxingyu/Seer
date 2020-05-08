 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.ctab.DefaultCTabItem;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
 import org.jboss.reddeer.swt.impl.menu.ShellMenu;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
 import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
 import org.jboss.tools.fuse.reddeer.preference.ServerRuntimePreferencePage;
 import org.jboss.tools.fuse.reddeer.projectexplorer.CamelProject;
 import org.jboss.tools.fuse.reddeer.utils.ResourceHelper;
 import org.jboss.tools.fuse.reddeer.view.FuseJMXNavigator;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Scanner;
 
 /**
  * Class contains test cases verifying resolved issues
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(FuseIntegrationPerspective.class)
 @RunWith(RedDeerSuite.class)
 public class RegressionTest {
 
 	@After
 	public void clean() {
 
 		new ProjectExplorer().deleteAllProjects();
 	}
 
 	/**
 	 * GUI editor issue when using route scoped onException
 	 * https://issues.jboss.org/browse/FUSETOOLS-674
 	 */
 	@Test
 	public void issue_674() throws ParserConfigurationException, SAXException, IOException {
 
 		ProjectFactory.createProject("camel-archetype-spring");
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
 
 		ProjectFactory.createProject("camel-archetype-spring");
 		new CamelProject("camel-spring").runCamelContext("camel-context.xml");
 		new FuseJMXNavigator().getNode("Local Processes", "Local Camel Context", "Camel", "camel").select();
 
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
 }
