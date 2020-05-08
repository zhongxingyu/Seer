 package org.jboss.tools.fuse.reddeer.wizard;
 
 import org.jboss.reddeer.eclipse.jface.wizard.NewWizardDialog;
 import org.jboss.reddeer.swt.condition.JobIsRunning;
 import org.jboss.reddeer.swt.condition.ShellWithTextIsActive;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.table.DefaultTable;
 import org.jboss.reddeer.swt.impl.text.LabeledText;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 
 /**
  * 
  * @author apodhrad
  * 
  */
 public class FuseProjectWizard extends NewWizardDialog {
 
 	public FuseProjectWizard() {
 		super("Fuse Tooling", "Fuse Project");
 	}
 
 	public void setFilter(String text) {
 		new LabeledText("Filter:").setText(text);
 	}
 	
 	public void selectFirstArchetype() {
 		new DefaultTable().select(0);
 	}
 	
 	@Override
 	public void finish() {
 		log.info("Finish wizard");

 		String shellText = new DefaultShell().getText();
		new PushButton("Finish").click();
 
 		TimePeriod timeout = TimePeriod.getCustom(20 * 60 * 1000);
 		new WaitWhile(new ShellWithTextIsActive(shellText), timeout);
 		new WaitWhile(new JobIsRunning(), timeout);
 	}
 
 }
