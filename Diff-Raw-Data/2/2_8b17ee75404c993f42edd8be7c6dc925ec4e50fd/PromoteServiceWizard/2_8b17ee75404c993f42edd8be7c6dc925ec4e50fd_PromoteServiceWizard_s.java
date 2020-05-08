 package org.jboss.tools.switchyard.reddeer.wizard;
 
 import org.jboss.reddeer.swt.impl.combo.LabeledCombo;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.text.LabeledText;
 import org.jboss.tools.switchyard.reddeer.widget.Link;
 
 /**
  * Wizard for promoting a service.
  * 
  * @author apodhrad
  * 
  */
 public class PromoteServiceWizard extends ServiceWizard<PromoteServiceWizard> {
 
 	public static final String DEFAULT_INTERFACE_TYPE = "Java";
 	public static final String DEFAULT_TRANSFORMER_TYPE = "Java Transformer";
 
 	public PromoteServiceWizard(String dialogTitle) {
 		super(dialogTitle);
 	}
 
 	public PromoteServiceWizard setName(String name) {
		new LabeledText("Service Name:").setText(name);
 		return this;
 	}
 
 	public PromoteServiceWizard setTransformerType(String transformerType) {
 		new DefaultShell("New Transformers");
 		new LabeledCombo("Transformer Type:").setSelection(transformerType);
 		return this;
 	}
 
 	protected void createInterface() {
 		new Link("Interface:").click();
 		Java2WSDLWizard wizard = new Java2WSDLWizard();
 		wizard.next();
 		wizard.finish();
 	}
 }
