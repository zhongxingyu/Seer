 package org.jboss.tools.bpmn2.reddeer.dialog;
 
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
 import org.jboss.reddeer.jface.wizard.NewWizardDialog;
 import org.jboss.reddeer.swt.impl.text.LabeledText;
 
 /**
  * 
  */
 public class BPMN2ProcessWizard extends NewWizardDialog {
 
 	/**
 	 * Creates a new instance of BPMN2ProcessWizard.
 	 */
 	public BPMN2ProcessWizard() {
 		super("BPMN2", "jBPM Process Diagram");
 	}
 	
 	/**
 	 * 
 	 * @param fileName
 	 */
 	public void execute(String fileName) {
 		execute(new String[0], fileName);	
 	}
 	
 	/**
 	 *
 	 * @param location
 	 * @param fileName
 	 */
 	public void execute(String[] location, String fileName) {
 		execute(location, fileName, null, null, null);
 	}
 
 	/**
 	 * 
 	 * @param location
 	 * @param fileName
 	 * @param processName
 	 * @param processID
 	 * @param pkg
 	 */
 	public void execute(String[] location, String fileName, String processName, String processId, String packageName) {
 		open();
 		// if these are null use predefined values by the editor wizard.
 		if (processName != null && !processName.isEmpty()) new LabeledText("Process name:").setText(processName);
 		if (packageName != null && !packageName.isEmpty()) new LabeledText("Package:").setText(packageName);
 		if (processId != null && !processId.isEmpty()) new LabeledText("Process ID:").setText(processId);
 		if (location != null && location.length > 0) new LabeledText("Container:").setText(ProjectPath.valueOf(location));
 		if (fileName != null && !fileName.isEmpty()) new LabeledText("File name:").setText(fileName);
 		finish();
 	}
 }
