 package org.ow2.mindEd.ide.ui.test;
 
 import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
 import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
 import static org.hamcrest.Matchers.instanceOf;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
 import org.eclipse.swtbot.swt.finder.finders.MenuFinder;
 import org.eclipse.swtbot.swt.finder.results.WidgetResult;
 import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 import org.hamcrest.Matcher;
 import org.junit.Test;
 import org.ow2.mindEd.ide.core.MindIdeCore;
 import org.ow2.mindEd.ide.test.TestMindProject;
 import org.ow2.mindEd.ide.ui.wizards.Messages;
 
 import fr.imag.adele.graphictests.gef.gtgefeditor.GTGefEditor;
 import fr.imag.adele.graphictests.gtmenu.GTMenu;
 import fr.imag.adele.graphictests.gttree.GTTreePath;
 import fr.imag.adele.graphictests.gtworkbench_part.GTEditor;
 import fr.imag.adele.graphictests.gtworkbench_part.GTShell;
 import fr.imag.adele.graphictests.gtworkbench_part.GTTextEditor;
 import fr.imag.adele.graphictests.gtworkbench_part.GTView;
 import fr.imag.adele.graphictests.test.GTTestCase;
 
 /**
  * Test Fractal Mind Wizard
  */
 public class TestCreateMindproject extends GTTestCase {
 	
 	
 	private static final String MIND_ADL_COMPONENT_MENU = "Mind ADL Component";
 	public static final String MIND_PROJECT_NODE = "Mind Project ";
 	public static final String FRACTAL_MIND_PROJECT = "MindEd Project";
 	public static final String MIND_NAVIGATOR = "Mind Navigator";
 	public static final String MIND_REPO_WS = "Mind Repo ws";
 	static private String projectName;
 
 	
 	
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		SWTBotPreferences.TIMEOUT = 6000;
 	}
 	/**
 	 * Test Fractal Mind Wizard in the default location.
 	 * @throws Exception
 	 */
 	
 	@Test
 	public void testCreateMindProject() throws Exception {
 		String name = "Test" ; //call a generator which compute a new name
 		GTMenu.clickItem("File", "New", FRACTAL_MIND_PROJECT);
 		GTShell shell = new GTShell(Messages.MindProjectWizard_window_title);
 		//shell.findTree().selectNode("Mind",FRACTAL_MIND_PROJECT);
 		//shell.findButton("Next >").click();
 		shell.findTextWithLabel("Project name:").typeText(name);
 		shell.close();
 		
 		TestMindProject.assertMindProject(name);		
 	}
 
 	
 	
 	/**
 	 * Select mind navigator.
 	 * @throws Exception
 	 */
 	
 	@Test
 	public void testSelectMindNavigatorAndCreateProject() throws Exception {
 		GTView mindView = new GTView(MIND_NAVIGATOR);
 		mindView.show();
 		
 		projectName = "Test2_"+System.currentTimeMillis() ; //call a generator which compute a new name
 		mindView.contextMenu(new GTTreePath(MIND_REPO_WS), FRACTAL_MIND_PROJECT).click();
 		GTShell shell = new GTShell(Messages.MindProjectWizard_window_title);
 		shell.findTextWithLabel("Project name:").typeText(projectName);
 		shell.close();
 		
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName);
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName, "/"+projectName+"/src");
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName, "Mind path");
 		
 		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 		assertTrue(p.exists());
 		IFolder srcFolder = p.getFolder("src");
 		assertTrue(srcFolder.exists());
 		
 		// create a package
 		GTTreePath node = new GTTreePath(MIND_REPO_WS, MIND_PROJECT_NODE+projectName, "/"+projectName+"/src");
 		mindView.contextMenu(node, "Mind package").click();
 		shell = new GTShell(Messages.MindPackageWizard_window_title);
 		final String packageName = "toto";
 		shell.findTextWithLabel("Package:").typeText(packageName);
 		shell.close();
 		
 		IFolder totoPackageFolder = srcFolder.getFolder(packageName);
 		assertTrue(totoPackageFolder.exists());
 		
 		node = node.concat(packageName);
 		mindView.contextMenu(node, MIND_ADL_COMPONENT_MENU).click();
 		
 		
 		// create a component
 		shell = new GTShell(Messages.ComponentNewWizard_window_title);
 	//	shell.findComboBoxWithLabel("Component kind:").setText("COMPOSITE");
 		shell.bot().ccomboBoxWithLabel("Component kind:").setSelection("COMPOSITE");
 		shell.findTextWithLabel("Component name:").typeText("CompA");
 		shell.close();
 		
 		IFile compAFile = totoPackageFolder.getFile("CompA.adl");
 		assertTrue(compAFile.exists());
 		IFile compAFile_Diagram = totoPackageFolder.getFile("CompA.adl"+MindIdeCore.DIAGRAM_EXT);
 		assertTrue(compAFile_Diagram.exists());
 		
 		
 		
 	}
 	
 	/**
 	 * Select mind navigator.
 	 * @throws Exception
 	 */
 	
 	@Test
 	public void testSelectMindNavigatorAndCreateProject2() throws Exception {
 		GTView mindView = new GTView(MIND_NAVIGATOR);
 		mindView.show();
 		
 		GTTreePath rootNode = new GTTreePath(MIND_REPO_WS);
 		
 		projectName = "Test2_"+System.currentTimeMillis() ; //call a generator which compute a new name
 		mindView.contextMenu(rootNode, FRACTAL_MIND_PROJECT).click();
 		GTShell shell = new GTShell(Messages.MindProjectWizard_window_title);
 		shell.findTextWithLabel("Project name:").typeText(projectName);
 		shell.close();
 		
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName);
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName, "/"+projectName+"/src");
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName, "Mind path");
 		
 		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 		assertTrue(p.exists());
 		IFolder srcFolder = p.getFolder("src");
 		assertTrue(srcFolder.exists());
 		
 		// create a package 
 		GTTreePath rootSrcNode = rootNode.concat(MIND_PROJECT_NODE+projectName, "/"+projectName+"/src");
 		mindView.contextMenu(rootSrcNode, "Mind package").click();
 		shell = new GTShell(Messages.MindPackageWizard_window_title);
 		final String packageName = "toto";
 		shell.findTextWithLabel("Package:").typeText(packageName);
 		shell.close();
 		
 		IFolder totoPackageFolder = srcFolder.getFolder(packageName);
 		assertTrue(totoPackageFolder.exists());
 		
 		GTTreePath packageNode = rootSrcNode.concat(packageName);
 		mindView.contextMenu(packageNode, MIND_ADL_COMPONENT_MENU).click();
 		
 		
 		// create a component
 		shell = new GTShell(Messages.ComponentNewWizard_window_title);
 		shell.bot().ccomboBoxWithLabel("Component kind:").setSelection("PRIMITIVE");
 		shell.findTextWithLabel("Component name:").typeText("CompA");
 		shell.close();
 		
 		IFile compAFile = totoPackageFolder.getFile("CompA.adl");
 		assertTrue(compAFile.exists());
 		IFile compAFile_Diagram = totoPackageFolder.getFile("CompA.adl"+MindIdeCore.DIAGRAM_EXT);
 		assertTrue(compAFile_Diagram.exists());
 	}
 	
 	
 	/**
 	 * Select mind navigator.
 	 * @throws Exception
 	 */
 	
 	@Test
 	public void testOpenWith() throws Exception {
 		GTView mindView = new GTView(MIND_NAVIGATOR);
 		mindView.show();
 		
 		GTTreePath rootNode = new GTTreePath(MIND_REPO_WS);
 		
 		projectName = "Test2_"+System.currentTimeMillis() ; //call a generator which compute a new name
 		String compA = "compA_OpenW";
 		
 		mindView.contextMenu(rootNode, FRACTAL_MIND_PROJECT).click();
 		GTShell shell = new GTShell(Messages.MindProjectWizard_window_title);
 		shell.findTextWithLabel("Project name:").typeText(projectName);
 		shell.close();
 		
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName);
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName, "/"+projectName+"/src");
 		mindView.findTree().selectNode(MIND_REPO_WS, MIND_PROJECT_NODE+projectName, "Mind path");
 		
 		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 		assertTrue(p.exists());
 		IFolder srcFolder = p.getFolder("src");
 		assertTrue(srcFolder.exists());
 		
 		// create a package 
 		GTTreePath rootSrcNode = rootNode.concat(MIND_PROJECT_NODE+projectName, "/"+projectName+"/src");
 		mindView.contextMenu(rootSrcNode, "Mind package").click();
 		shell = new GTShell(Messages.MindPackageWizard_window_title);
 		final String packageName = "toto";
 		shell.findTextWithLabel("Package:").typeText(packageName);
 		shell.close();
 		
 		IFolder totoPackageFolder = srcFolder.getFolder(packageName);
 		assertTrue(totoPackageFolder.exists());
 		
 		GTTreePath packageNode = rootSrcNode.concat(packageName);
 		mindView.contextMenu(packageNode, MIND_ADL_COMPONENT_MENU).click();
 		
 		
 		// create a component
 		shell = new GTShell(Messages.ComponentNewWizard_window_title);
 		shell.bot().ccomboBoxWithLabel("Component kind:").setSelection("PRIMITIVE");
 		shell.findTextWithLabel("Component name:").typeText(compA);
 		shell.close();
 		
 		GTTextEditor text = new GTTextEditor(compA+".adl");
 		text.close();
 		
 		GTTreePath compANode = packageNode.concat("Mind Adl "+compA.substring(0,1).toLowerCase()+compA.substring(1));
 		mindView.selectNode(compANode);
 		
 //		final SWTBotMenu openWMenu = mindView.contextMenu(compANode, "Open With");
 //		
 //		fr.imag.adele.graphictests.test.GTMenu gtOpenWMenu = new fr.imag.adele.graphictests.test.GTMenu(openWMenu);
 //		
 //		mindView.bot().getDisplay().syncExec(new Runnable() {
 //			
 //			@Override
 //			public void run() {
 //				MenuItem[] items = openWMenu.widget.getMenu().getItems();
 //				for (MenuItem menuItem : items) {
 //					System.out.println(" - "+menuItem.getText());
 //				}
 //				
 //			}
 //		});
 		mindView.click(compANode, "Open With","MindEd ADL Editor");
 		text = new GTTextEditor(compA+".adl");
 		text.close();
 		
 		mindView.click(compANode, "Open With","Mind Graphical Editor");
 		GTGefEditor gef = new GTGefEditor(compA+".adl_diagram");
 		checkError(gef);
 		gef.close();
 		
 		IFile diagFile = totoPackageFolder.getFile(compA+".adl_diagram");
 		IFile adlFile = totoPackageFolder.getFile(compA+".adl");
 		assertTrue(diagFile.exists());
 		diagFile.delete(true, null);
 		assertTrue(!diagFile.exists());
 		
 		assertTrue(adlFile.exists());
 		mindView.click(compANode, "Open With","Mind Graphical Editor");
 		gef = new GTGefEditor(compA+".adl_diagram");
 		checkError(gef);
 		gef.close();
 		
 		assertTrue(diagFile.exists());
 		
 	}
 	
 	private void checkError(final GTGefEditor gef) {
 		Matcher matcher = instanceOf(FigureCanvas.class);
 		List widgets = gef.bot().widgets(matcher);
 		assertTrue(widgets.size() == 2);		
 	}
 
 
 
 	public MenuItem click(final GTView mindView, final GTTreePath node , final String... menuName) throws WidgetNotFoundException {
 		final MenuItem[] menuItem = new MenuItem[1]; 
 		final String[] error = new String[1];
 		
 		mindView.bot().getDisplay().syncExec(new Runnable() {
 			public void run() {
 				try {
 					SWTBotTreeItem node2 = mindView.bot().tree().expandNode(node.getPath());
 					node2.contextMenu(menuName[0]);
 					Menu bar = mindView.bot().tree().widget.getMenu();
 					String errorMsg = "Cannot found menu ";
 					ONE : for (int i = 0; i < menuName.length ; i++) {
 						String n = menuName[i];
 						if (i == 0)
 							errorMsg = errorMsg+n;
 						else
 							errorMsg = errorMsg+":"+n;
 						if (bar == null) {
 							error[0] = errorMsg;
 							menuItem[0] = null;
 							return;
 						}
 						bar.notifyListeners(SWT.Show, new Event());
 						Matcher<MenuItem> withMnemonic = withMnemonic(n);
 						MenuItem[] items = bar.getItems();
 						for (MenuItem m : items) {
 							if (withMnemonic.matches(m)) {
 								if (i+1 == menuName.length) {
 									bar.notifyListeners(SWT.Hide, new Event());
 									error[0] = null;
 									menuItem[0] = m;
 									new SWTBotMenu(m).click();
 									return;
 								}
 								bar.notifyListeners(SWT.Hide, new Event());
 								bar = m.getMenu();
 								continue ONE;
 							}
 						}
 						error[0] = errorMsg;
 						menuItem[0] = null;
 						return;
 					}
 					error[0] = errorMsg;
 					menuItem[0] = null;
 					return;
 				} catch (WidgetNotFoundException e) {
 					error[0] = e.getMessage();
 					menuItem[0] = null;
 					return;
 				}
 			}
 		});
 
 		if (error[0] != null)
 			throw new WidgetNotFoundException(error[0]);
 		return menuItem[0];
 	}
 }
