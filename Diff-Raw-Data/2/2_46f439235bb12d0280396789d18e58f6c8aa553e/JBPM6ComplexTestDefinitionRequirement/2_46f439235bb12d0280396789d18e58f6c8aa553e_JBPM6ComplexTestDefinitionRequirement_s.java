 package org.jboss.tools.bpmn2.ui.bot.complex.test;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.jboss.reddeer.eclipse.core.resources.Project;
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.jdt.ui.packageexplorer.PackageExplorer;
 import org.jboss.reddeer.junit.requirement.Requirement;
 import org.jboss.reddeer.swt.condition.WaitCondition;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.impl.button.CheckBox;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.menu.ShellMenu;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.text.LabeledText;
 import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.reddeer.swt.util.Display;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 import org.jboss.tools.bpmn2.reddeer.JBPM6ComplexEnvironment;
 import org.jboss.tools.bpmn2.reddeer.dialog.JavaProjectWizard;
 import org.jboss.tools.bpmn2.ui.bot.complex.test.JBPM6ComplexTestDefinitionRequirement.JBPM6ComplexTestDefinition;
 import org.jboss.tools.bpmn2.ui.bot.tmp.ImportFileWizard;
 
 public class JBPM6ComplexTestDefinitionRequirement implements Requirement<JBPM6ComplexTestDefinition> {
 
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.TYPE)
 	public @interface JBPM6ComplexTestDefinition {
 		public String projectName();
 		public String importFolder() default "";
 		public String saveAs();
 		public String openFile();
 		public String dependentOn() default "";
 		public boolean noErrorsInValidation() default true;
 		public boolean useGraphiti() default true;
 	}
 	
 	private JBPM6ComplexTestDefinition declaration;
 	private static boolean configureShellHandled;
 	private static List<String> foldersToImport = new ArrayList<String>(Arrays.asList(""));
 	
 	@Override
 	public boolean canFulfill() {
 		return true;
 	}
 
 	@Override
 	public void fulfill() {
 		JBPM6ComplexEnvironment.getInstance().setUseGraphiti(declaration.useGraphiti());
 		
 		Display.getDisplay().syncExec(new Runnable() {
 			public void run() {
 				new DefaultShell().getSWTWidget().setMaximized(true);
 			}
 		});
 		
 		PackageExplorer pe = new PackageExplorer();
 		pe.open();
 		if (!pe.containsProject(declaration.projectName())) {
 			new JavaProjectWizard().execute(declaration.projectName());
 			
 			try {
 				new DefaultShell("Open Associated Perspective?");
 				new PushButton("No").click();
 			} catch (SWTLayerException e) {
 				// ignore
 			}
 		}
 		
 		if(!foldersToImport.contains(declaration.importFolder())){
 			foldersToImport.add(declaration.importFolder());
 			new ImportFileWizard().importFile(declaration.importFolder());
 		}
 		
 		Project project = new ProjectExplorer().getProject(declaration.projectName());
 		project.getProjectItem(declaration.openFile()).open();
 		
 		saveAs(declaration.saveAs());
 		
 	}
 
 	@Override
 	public void setDeclaration(JBPM6ComplexTestDefinition declaration) {
 		this.declaration = declaration;
 		
 	}
 	
 	private void saveAs(String filename) {
 		new WaitWhile(new MenuItemIsDisabled());
 		new DefaultShell("Save As");
 		new DefaultTreeItem(declaration.projectName()).select();
 		new LabeledText("File name:").setText(filename);
 		new PushButton("OK").click();
 		
 		if(!configureShellHandled) {
 			try{
				new DefaultShell("Configure BPMN2 Project Nature");
 				new CheckBox().toggle(true);
 				new PushButton("Yes").click();
 			} catch (SWTLayerException e) {
 				// probably previously configured
 			}
 			configureShellHandled = true;
 		}
 	}
 
 	private class MenuItemIsDisabled implements WaitCondition {
 
 		@Override
 		public boolean test() {
 			try{
 				new ShellMenu(new String[]{"File", "Save As..."}).select();
 			} catch(SWTLayerException e) {
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public String description() {
 			return "Wait for enabled menu item";
 		}
 		
 	}
 }
