 package it.polito.atlas.alea2.components;
 
 import static it.polito.atlas.alea2.components.DisplaySingleton.display;
 import it.polito.atlas.alea2.Project;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class NewProjectShell {
 	private Shell shell;
 	private Text textName;
 	private Text textTags;
 
 	public Shell shell() {
 		return shell;
 	}
 
 	public NewProjectShell() {
 		shell = new Shell(display());
 		shell.setText("New Project");
 		shell.setSize(320, 200);
 		shell.setLocation(SWT.DEFAULT, SWT.DEFAULT);
 		shell.addShellListener(new ShellListener() {
 			
 			@Override
 			public void shellIconified(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void shellDeiconified(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void shellDeactivated(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void shellClosed(ShellEvent arg0) {
 			}
 			
 			@Override
 			public void shellActivated(ShellEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		shell.setLayout(new FormLayout());
 		
 		Point size;
 		FormData formData;
 		
 		// Label name
 		Label labelName = new Label(shell(), SWT.BORDER);
 		labelName.setText("Insert the Project name:");
 		labelName.pack();
 		size = labelName.getSize();
 		formData = new FormData(size.x, size.y);
 		formData.left = new FormAttachment(0);
 		formData.right = new FormAttachment(100);
 		formData.top = new FormAttachment(0);
 		labelName.setLayoutData(formData);
 		
 		// Text name
 		textName = new Text(shell(), SWT.SINGLE | SWT.BORDER);
 		textName.pack();
 		size = textName.getSize();
 		formData = new FormData(size.x, size.y);
 		formData.left = new FormAttachment(0);
 		formData.right = new FormAttachment(100);
 		formData.top = new FormAttachment(labelName);
 		//formData.bottom = new FormAttachment(60);
 		textName.setLayoutData(formData);
 		
 		// Label Tags
 		Label labelTags = new Label(shell(), SWT.BORDER);
 		labelTags.setText("Insert the Project tags:");
 		labelTags.pack();
 		size = labelTags.getSize();
 		formData = new FormData(size.x, size.y);
 		formData.left = new FormAttachment(0);
 		formData.right = new FormAttachment(100);
 		formData.top = new FormAttachment(textName);
 		labelTags.setLayoutData(formData);
 
 		// Button OK
 		Button ok = new Button(shell(), SWT.PUSH);
         ok.setText("OK");
 		formData = new FormData(size.x, 40);
 		formData.left = new FormAttachment(50);
 		formData.right = new FormAttachment(100);
 		//formData.top = new FormAttachment(textTags);
 		formData.bottom = new FormAttachment(100);
 		ok.setLayoutData(formData);
 		ok.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				Project p=new Project(getProjectName());
 				p.setTags(getProjectTags());
 				if (MainWindowShell.openProject(p)){
 					shell().close();
 				} else {
 					MessageBox mb = new MessageBox(shell());
 					mb.setMessage("Project already exists");
 					mb.open();
 				}
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		// Button Cancel
 		Button cancel = new Button(shell(), SWT.PUSH);
 		cancel.setText("Cancel");
 		formData = new FormData(size.x, 40);
 		formData.left = new FormAttachment(0);
 		formData.right = new FormAttachment(50);
 		//formData.top = new FormAttachment(textTags);
 		formData.bottom = new FormAttachment(100);
 		cancel.setLayoutData(formData);
 		cancel.addSelectionListener(new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				shell().close();				
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});		
 		
 		// Text tags
		textTags = new Text(shell(), SWT.SINGLE | SWT.BORDER);
 		textTags.pack();
 		size = textTags.getSize();
 		formData = new FormData(size.x, size.y);
 		formData.left = new FormAttachment(0);
 		formData.right = new FormAttachment(100);
 		formData.top = new FormAttachment(labelTags);
 		formData.bottom = new FormAttachment(ok);
 		textTags.setLayoutData(formData);
 		
 		shell.open();
 	}
 	public String getProjectName() {
 		return textName.getText();
 	}
 	public String getProjectTags() {
 		return textTags.getText();
 	}
 }
