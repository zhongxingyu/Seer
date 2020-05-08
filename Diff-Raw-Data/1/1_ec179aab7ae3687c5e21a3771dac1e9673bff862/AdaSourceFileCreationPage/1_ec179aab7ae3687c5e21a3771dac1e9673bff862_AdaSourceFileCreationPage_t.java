 package org.padacore.wizards;
 
 import java.io.InputStream;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
 import org.padacore.IAdaSourceFile;
 
 public class AdaSourceFileCreationPage extends WizardNewFileCreationPage {
 	
 	private IAdaSourceFile sourceFileType;
 
 	public AdaSourceFileCreationPage(String pageName, IStructuredSelection selection, IAdaSourceFile sourceFileType) {
 		super(pageName, selection);
 		this.sourceFileType = sourceFileType;
 		super.setFileExtension(sourceFileType.getExtension());
 		super.setDescription(sourceFileType.getFileTypeDescription());
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		super.createControl(parent);
 	}
 	
 	@Override
 	protected InputStream getInitialContents() { 
 		return this.sourceFileType.getTemplate();
 				
 	}
 }
