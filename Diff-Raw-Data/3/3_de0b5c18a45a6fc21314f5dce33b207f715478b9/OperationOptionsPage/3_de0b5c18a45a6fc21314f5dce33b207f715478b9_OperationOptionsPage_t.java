 package org.jboss.tools.switchyard.reddeer.binding;
 
 import org.jboss.reddeer.eclipse.jface.wizard.WizardPage;
 import org.jboss.reddeer.swt.impl.combo.DefaultCombo;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.util.Display;
 import org.jboss.reddeer.swt.util.ResultRunnable;
 
 public abstract class OperationOptionsPage<T> extends WizardPage {
 
 	public static final String OPERATION_NAME = "Operation Name";
 	public static final String XPATH = "XPath";
 	public static final String REGEX = "Regex";
 	public static final String JAVA_CLASS = "Java Class";
 
 	@SuppressWarnings("unchecked")
 	public T setOperation(String operation) {
		new DefaultShell("");
 		new DefaultCombo(0).setSelection(OPERATION_NAME);
 		Display.syncExec(new ResultRunnable<Boolean>() {
 
 			@Override
 			public Boolean run() {
 				return new DefaultCombo(1).getSWTWidget().forceFocus();
 			}
 		});
 
 		new DefaultCombo(1).setSelection(operation);
 		return (T) this;
 	}
 
 	public String getOperation() {
 		return new DefaultCombo(1).getText();
 	}
 
 }
