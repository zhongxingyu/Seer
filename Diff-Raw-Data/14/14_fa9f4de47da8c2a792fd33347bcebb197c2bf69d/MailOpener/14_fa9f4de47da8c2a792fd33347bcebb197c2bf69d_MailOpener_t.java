 package com.eclipsesource.makeithappen.ui;
 
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

 import org.eclipse.emf.ecp.core.ECPProject;
 import org.eclipse.emf.ecp.ui.util.ECPModelElementOpener;
 import org.eclipse.emf.edit.provider.AttributeValueWrapperItemProvider;
 
 public class MailOpener implements ECPModelElementOpener {
 
 	public void openModelElement(Object object, ECPProject arg1) {
 		AttributeValueWrapperItemProvider wrapper=(AttributeValueWrapperItemProvider)object;
 		String value = (String) wrapper.getValue();
		try {
			Desktop.getDesktop().mail(
					URI.create("mailto:" + value));
		} catch (IOException e) {
			//ignore failure
		}
 	}
 
 }
