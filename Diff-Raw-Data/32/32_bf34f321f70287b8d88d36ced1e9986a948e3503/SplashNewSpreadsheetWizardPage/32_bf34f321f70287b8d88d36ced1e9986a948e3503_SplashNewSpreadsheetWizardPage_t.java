 package org.amanzi.splash.ui.neo4j.wizards;
 
import java.io.File;
 
 import net.refractions.udig.project.internal.impl.RubyProjectImpl;
 
 import org.amanzi.splash.neo4j.utilities.Util;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 import org.rubypeople.rdt.internal.ui.wizards.OpenNewRubyProjectWizardAction;
 
 /**
  * The "New" wizard page allows setting the container for the new file as well
  * as the file name. The page will only accept file name without the extension
  * OR with the extension that matches the expected one (jrss).
  */
 
 public class SplashNewSpreadsheetWizardPage extends WizardPage {
 	private Text containerText;
 	private static int nameCounter = 1;
 	private Text fileText;
 
 	private ISelection selection;
 
 	/**
 	 * Constructor for SampleNewWizardPage.
 	 * 
 	 * @param pageName
 	 */
 	public SplashNewSpreadsheetWizardPage(ISelection selection) {
 		super("wizardPage");
 		setTitle("Amanzi Splash Spreadsheet Wizard");
 		setDescription("This wizard creates Amanzi Splash Spreadsheet file");
 		this.selection = selection;
 	}
 	private int rowCount = 30, columnCount = 30;
 	private Text rowText, columnText;
 
 	/**
 	 * @see IDialogPage#createControl(Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 3;
 		layout.verticalSpacing = 9;
 		Label label = new Label(container, SWT.NULL);
 		label.setText("&Container:");
 
 		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		containerText.setLayoutData(gd);
 		containerText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		
 		//containerText.setText(this.selection.)
 
 		Button button = new Button(container, SWT.PUSH);
 		button.setText("Browse...");
 		button.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleBrowse();
 			}
 		});
 		
 		
 		
 		label = new Label(container, SWT.NULL);
 		label.setText("&File name:");
 
 		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fileText.setLayoutData(gd);
 		fileText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				dialogChanged();
 			}
 		});
 		
 		Button button1 = new Button(container, SWT.PUSH);
 		button1.setText("New Ruby Project...");
 		button1.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				new OpenNewRubyProjectWizardAction().run();
 			}
 		});
 		
 
 		
 		initialize();
 		dialogChanged();
 		setControl(container);
 	}
 	
 	
 
 	public int getRowCount() {
 		return rowCount;
 	}
 
 
 
 	public void setRowCount(int rowCount) {
 		this.rowCount = rowCount;
 	}
 
 
 
 	public int getColumnCount() {
 		return columnCount;
 	}
 
 
 
 	public void setColumnCount(int columnCount) {
 		this.columnCount = columnCount;
 	}
 
 
 
 	/**
 	 * Tests if the current workbench selection is a suitable container to use.
 	 */
 
 	private void initialize() {
 		if (selection != null && selection.isEmpty() == false
 				&& selection instanceof IStructuredSelection) {
 			IStructuredSelection ssel = (IStructuredSelection) selection;
 			
 			Util.logn("ssel: "+ ssel.toString());
 			
 			if (ssel.size() > 1)
 				return;
 			Object obj = ssel.getFirstElement();
 			Util.logn("obj: "+ obj.toString());
 			
 			if (obj instanceof IResource) {
 				IContainer container;
 				if (obj instanceof IContainer)
 					container = (IContainer) obj;
 				else
 					container = ((IResource) obj).getParent();
 				
 				Util.logn("container: "+ container.getName());
 				
 				
 				containerText.setText(container.getFullPath().toString());
 			}else if (obj instanceof RubyProjectImpl){
 				RubyProjectImpl rpi = (RubyProjectImpl) obj;
 				if (!"".equals( rpi.getName()))
 					containerText.setText("/" + rpi.getName());
 			}
 		}
 		fileText.setText("sheet" + nameCounter + ".jrss");
 		nameCounter++;
 	}
 
 	/**
 	 * Uses the standard container selection dialog to choose the new value for
 	 * the container field.
 	 */
 
 	private void handleBrowse() {
 		Util.logn("ResourcesPlugin.getWorkspace().getRoot(): " + ResourcesPlugin.getWorkspace().getRoot().toString());
 		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
 				getShell(), ResourcesPlugin.getWorkspace().getRoot(), true,
 				"Select new file container");
 		
 		if (dialog.open() == ContainerSelectionDialog.OK) {
 			Object[] result = dialog.getResult();
 			if (result.length == 1) {
 				containerText.setText(((Path) result[0]).toString());
 			}
 		}
 	}
 
 	/**
 	 * Ensures that both text fields are set.
 	 */
 
 	private void dialogChanged() {
 		IResource container = ResourcesPlugin.getWorkspace().getRoot()
 				.findMember(new Path(getContainerName()));
		
 		String fileName = getFileName();
		
		Util.logn(Platform.getLocation() + "/" + getContainerName() + "/" + fileName);
		File f = new File(Platform.getLocation() + "/" + getContainerName() + "/" + fileName);
		
		if (f.exists()){
			updateStatus("File already exists...");
			return;
		}
 
 		if (getContainerName().length() == 0) {
 			updateStatus("File container must be specified");
 			return;
 		}
		
		
 		if (container == null
 				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
 			updateStatus("File container must exist");
 			return;
 		}
 		if (!container.isAccessible()) {
 			updateStatus("Project must be writable");
 			return;
 		}
 		if (fileName.length() == 0) {
 			updateStatus("File name must be specified");
 			return;
 		}
 		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
 			updateStatus("File name must be valid");
 			return;
 		}
 		int dotLoc = fileName.lastIndexOf('.');
 		if (dotLoc != -1) {
 			String ext = fileName.substring(dotLoc + 1);
 			if (ext.equalsIgnoreCase("jrss") == false) {
 				updateStatus("File extension must be \"jrss\"");
 				return;
 			}
 		}
 		updateStatus(null);
 	}
 
 	private void updateStatus(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 
 	public String getContainerName() {
 		return containerText.getText();
 	}
 
 	public String getFileName() {
 		return fileText.getText();
 	}
 }
