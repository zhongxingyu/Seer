 package org.xtext.example.mydsl;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
 import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
 import org.eclipse.swtbot.swt.finder.waits.Conditions;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.xtext.example.mydsl.testutils.AbstractUITest;
 
 @RunWith(SWTBotJunit4ClassRunner.class)
 public class XtextTodoTest extends AbstractUITest {
 	final String projectName = "Test";
 
 	@Test
 	public void testCreateSimpleDsl() {
 		String sourceFolder = "model";
 		String dslFile = "test.mydsl";
 		createNewProject();
 		selectFolderNode(getProjectName()).select();
 		createFolder(getProjectName(), sourceFolder);
 		createFirstXtextFile(getProjectName(), sourceFolder, dslFile);
 		SWTBotEclipseEditor dslEditor = getBot().activeEditor().toTextEditor();
 		dslEditor.setText("// TODO change name!\nHello Joerg!");
 		dslEditor.save();
 		openView("Tasks", "General");
 		SWTBotView view = getBot().viewById("org.eclipse.ui.views.TaskList");
 		view.show();
 		SWTBotTree tree = getBot().tree();
 		getBot().waitUntil(Conditions.treeHasRows(tree, 1));
 		// TODO assert TODO Highlighting
 		// TODO assert Marker Existence
 		// TODO assert Marker Hyper Linking
 	}
 
 	@Override
 	protected String getProjectName() {
 		return projectName;
 	}
 }
