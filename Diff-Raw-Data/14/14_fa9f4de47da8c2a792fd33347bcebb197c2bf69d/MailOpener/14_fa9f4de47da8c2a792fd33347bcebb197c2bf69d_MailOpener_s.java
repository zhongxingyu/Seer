 package com.eclipsesource.makeithappen.ui;
 
 import org.eclipse.emf.ecp.core.ECPProject;
 import org.eclipse.emf.ecp.ui.util.ECPModelElementOpener;
 import org.eclipse.emf.edit.provider.AttributeValueWrapperItemProvider;
import org.eclipse.swt.program.Program;
 
 public class MailOpener implements ECPModelElementOpener {
 
 	public void openModelElement(Object object, ECPProject arg1) {
 		AttributeValueWrapperItemProvider wrapper=(AttributeValueWrapperItemProvider)object;
 		String value = (String) wrapper.getValue();
		Program.launch("mailto:"+value);
 	}
 
 }
