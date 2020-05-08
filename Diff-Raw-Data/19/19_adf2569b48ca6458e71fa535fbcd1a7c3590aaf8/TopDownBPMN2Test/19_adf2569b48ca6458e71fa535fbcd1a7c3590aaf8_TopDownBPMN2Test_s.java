 package org.jboss.tools.switchyard.ui.bot.test;
 
 import static org.junit.Assert.assertEquals;
 
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.jdt.ui.packageexplorer.ProjectItem;
 import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.menu.ShellMenu;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
import org.jboss.reddeer.swt.impl.text.LabeledText;
 import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.reddeer.workbench.impl.view.WorkbenchView;
import org.jboss.tools.bpmn2.reddeer.ProcessEditorView;
import org.jboss.tools.bpmn2.reddeer.ProcessPropertiesView;
 import org.jboss.tools.bpmn2.reddeer.editor.ElementType;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.FromDataOutput;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.FromVariable;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.ParameterMapping;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.ToDataInput;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.ToVariable;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.endevents.TerminateEndEvent;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.startevents.StartEvent;
 import org.jboss.tools.bpmn2.reddeer.editor.switchyard.activities.SwitchYardServiceTask;
 import org.jboss.tools.switchyard.reddeer.component.Service;
 import org.jboss.tools.switchyard.reddeer.component.SwitchYardComponent;
 import org.jboss.tools.switchyard.reddeer.condition.JUnitHasFinished;
 import org.jboss.tools.switchyard.reddeer.editor.SwitchYardEditor;
 import org.jboss.tools.switchyard.reddeer.editor.TextEditor;
 import org.jboss.tools.switchyard.reddeer.project.ProjectItemExt;
 import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement;
 import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement.SwitchYard;
 import org.jboss.tools.switchyard.reddeer.view.JUnitView;
 import org.jboss.tools.switchyard.reddeer.wizard.ReferenceWizard;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Create simple BPMN process with a switchyard service task, run JUnit test.
  * 
  * @author lfabriko
  * @author apodhrad
  * 
  */
 @SwitchYard
 @OpenPerspective(JavaEEPerspective.class)
 @RunWith(RedDeerSuite.class)
 public class TopDownBPMN2Test {
 
 	private static final String PROJECT = "switchyard-bpm-processgreet";
 	private static final String PACKAGE = "org.switchyard.quickstarts.bpm.service";
 	private static final String GROUP_ID = "org.switchyard.quickstarts.bpm.service";
 	private static final String PROCESS_GREET = "ProcessGreet";
 	private static final String BPMN_FILE_NAME = "ProcessGreet.bpmn";
 	private static final String PACKAGE_MAIN_JAVA = "src/main/java";
 	private static final String PACKAGE_MAIN_RESOURCES = "src/main/resources";
 	private static final Integer[] BPM_COORDS = { 50, 200 };
 	private static final Integer[] BEAN_COORDS = { 250, 200 };
 	private static final String EVAL_GREET = "EvalGreet";
 	private static final String PROCESS_GREET_DECL = "public boolean checkGreetIsPolite(String greet);";
 	private static final String EVAL_GREET_DECL = "public boolean checkGreet(String greet);";
 	private static final String EVAL_GREET_BEAN = EVAL_GREET + "Bean";
 
 	@InjectRequirement
 	private SwitchYardRequirement switchyardRequirement;
 	
 	@Before
 	@After
 	public void closeSwitchyardFile() {
 		try {
 			new SwitchYardEditor().saveAndClose();
 		} catch (Exception ex) {
 			// it is ok, we just try to close switchyard.xml if it is open
 		}
 	}
 
 	@Test
 	public void topDownBPMN2Test() {
 		new WorkbenchShell().maximize();
 
 		// Create new Switchyard project, Add support for Bean, BPM
 		switchyardRequirement.project(PROJECT).impl("Bean", "BPM (jBPM)").groupId(GROUP_ID).packageName(PACKAGE)
 				.create();
 		openFile(PROJECT, PACKAGE_MAIN_RESOURCES, "META-INF", "switchyard.xml");
 		
 		new SwitchYardEditor().addBPMNImplementation().setFileName(BPMN_FILE_NAME).createNewInterface(PROCESS_GREET).finish();
 		new SwitchYardEditor().autoLayout();
 		new SwitchYardEditor().save();
 
 		new SwitchYardEditor().addBeanImplementation().createNewInterface(EVAL_GREET).finish();
 		new SwitchYardEditor().save();
 
 		// reference to bean
 		new SwitchYardComponent(PROCESS_GREET).getContextButton("Reference").click();
 		new ReferenceWizard().selectJavaInterface(EVAL_GREET).finish();
 		new SwitchYardEditor().save();
 
 		// declare ProcessGreet interface
		new Service(PROCESS_GREET, 1).openTextEditor();
 		new TextEditor(PROCESS_GREET + ".java").typeAfter("interface", PROCESS_GREET_DECL);
 		// declare EvalGreet interface
 		openFile(PROJECT, PACKAGE_MAIN_JAVA, PACKAGE, EVAL_GREET + ".java");
 		new TextEditor(EVAL_GREET + ".java").typeAfter("interface", EVAL_GREET_DECL);
 		// implement EvalGreetBean
 		openFile(PROJECT, PACKAGE_MAIN_JAVA, PACKAGE, EVAL_GREET_BEAN + ".java");
 		new TextEditor(EVAL_GREET_BEAN + ".java").typeAfter("implements", "@Override").newLine()
 				.type("public boolean checkGreet(String greet){").newLine()
 				.type("return (greet.equals(\"Good evening\")) ? true : false;").newLine().type("}");
 
 		// BPM Process and its properties
 		openFile(PROJECT, PACKAGE_MAIN_RESOURCES, BPMN_FILE_NAME);
		ProcessEditorView editor = new ProcessEditorView();
		editor.click(1, 1);
		new WorkbenchShell();
		new ProcessPropertiesView().selectTab("Process");
		new LabeledText("Id").setText(PROCESS_GREET);
		editor.setFocus();

 		new TerminateEndEvent("EndProcess").delete();
 		new StartEvent("StartProcess").append("EvalGreet", ElementType.SWITCHYARD_SERVICE_TASK);
 		SwitchYardServiceTask task = new SwitchYardServiceTask("EvalGreet");
 		task.setTaskAttribute("Operation Name", "checkGreet");
 		task.setTaskAttribute("Service Name", EVAL_GREET);
 		task.addParameterMapping(new ParameterMapping(new FromVariable(PROCESS_GREET + "/Parameter"),
 				new ToDataInput("Parameter", "String"), ParameterMapping.Type.INPUT));
 		task.addParameterMapping(new ParameterMapping(new FromDataOutput("Result", "String"), new ToVariable(
 				PROCESS_GREET + "/Result"),  ParameterMapping.Type.OUTPUT));
 		task.append("EndProcess", ElementType.TERMINATE_END_EVENT);
 
 		openFile(PROJECT, PACKAGE_MAIN_RESOURCES, "META-INF", "switchyard.xml");
 
 		// Junit
 		new Service(PROCESS_GREET, 1).createNewServiceTestClass();
 		new TextEditor(PROCESS_GREET + "Test.java")
 				.deleteLineWith("null")
 				.deleteLineWith("assertTrue")
 				.typeBefore("boolean", "String message = \"Good evening\";")
 				.newLine()
 				.typeAfter("getContent", "Assert.assertTrue(result);")
 				.newLine()
 				.type("Assert.assertFalse(service.operation(\"checkGreetIsPolite\").sendInOut(\"hi\").getContent(Boolean.class));");
 		new ShellMenu("File", "Save All").select();
 
 		new DefaultShell("Configure BPMN2 Project Nature");
 		new PushButton("No").click();// BPMN nature
 
 		ProjectItem item = new ProjectExplorer().getProject(PROJECT).getProjectItem("src/test/java", PACKAGE,
 				PROCESS_GREET + "Test.java");
 		new ProjectItemExt(item).runAsJUnitTest();
 		new WaitUntil(new JUnitHasFinished(), TimePeriod.LONG);
 
 		assertEquals("1/1", new JUnitView().getRunStatus());
 		assertEquals(0, new JUnitView().getNumberOfErrors());
 		assertEquals(0, new JUnitView().getNumberOfFailures());
 	}
 
 	private void openFile(String... file) {
 		// focus on project explorer
 		new WorkbenchView("General", "Project Explorer").open();
 		new DefaultTreeItem(0, file).doubleClick();
 	}
 }
