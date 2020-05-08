 package org.vaadin.mideaas.app;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.LinkedList;
 
 import org.vaadin.mideaas.frontend.Icons;
 import org.vaadin.mideaas.model.SharedProject;
 
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Upload;
 import com.vaadin.ui.Upload.FailedEvent;
 import com.vaadin.ui.Upload.SucceededEvent;
 
 @SuppressWarnings("serial")
 public class UploadProjectPanel extends Panel implements Upload.Receiver,
 		Upload.SucceededListener, Upload.FailedListener {
 
 	public interface ProjectUploadListener {
 		public void projectUploaded(SharedProject p);
 	}
 	private LinkedList<ProjectUploadListener> listeners = new LinkedList<ProjectUploadListener>();
 	public void addListener(ProjectUploadListener li) {
 		listeners.add(li);
 	}
 	
 	private Upload upload = new Upload("Upload project zip file", this);
 	
 	private File file;
 
 	private final MideaasUI ui;
 
 	public UploadProjectPanel(MideaasUI ui) {
 		super("Upload Project");
 		
 		upload.addSucceededListener((Upload.SucceededListener) this);
 		upload.addFailedListener((Upload.FailedListener) this);
 
 		setIcon(Icons.BOX_LABEL);
 		
 		this.setContent(upload);
 		this.setWidth("100%");
 		this.ui =ui;
 	}
 
 	public OutputStream receiveUpload(String filename, String mimeType) {
 		FileOutputStream fos = null;
 		try {
 			file = File.createTempFile(filename, ".zip");
 			fos = new FileOutputStream(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return fos;
 	}
 
 	public void uploadFailed(FailedEvent event) {
 		Notification.show("Upload failed: " + event.getReason());
 	}
 
 	public void uploadSucceeded(SucceededEvent event) {
 		// Is this the right way to test if it's a zip file??
		if ("application/x-zip-compressed".equals(event.getMIMEType())) {
 			ui.uploadProject(file);
 		} else {
 			Notification.show("Not a zip file. The mimetype is " +event.getMIMEType());
 		}
 	}
 	
 //	private void createProject(Properties props, File dir) {
 //		createProject(props.getProperty("PROJECT_NAME"),
 //				ProjectType.valueOf(props.getProperty("PROJECT_TYPE")), dir);
 //	}
 
 /*	private void createProject(final String name, final ProjectType type, final File dir) {
 		Project p = Project.createProjectIfNotExist(name, type, false);
 		if (p==null) {
 			final Window main = getWindow();
 			final ConfirmResetDialog dia = new ConfirmResetDialog(name);
 			dia.overwrite.addListener(new ClickListener() {
 				public void buttonClick(ClickEvent event) {
 					main.removeWindow(dia);
 					Project p = Project.getProject(name);
 					if (p==null) {
 						unlucky();
 					}
 					else {
 						p.resetFromDisk(dir);
 						fireProjectUpload(p);
 					}
 				}
 			});
 			main.addWindow(dia);
 		}
 		else {
 			p.resetFromDisk(dir);
 			fireProjectUpload(p);
 		}
 	}
 	
 	private void unlucky() {
 		getWindow().showNotification("Something strange happened :(", Notification.TYPE_ERROR_MESSAGE);
 	}
 */
 	
 /*	
 	private Properties projectProps(File dir) {
 		File f = new File(dir, "project.properties");
 		try {
 			Properties props = PropertiesUtil.getProperties(f);
 			if (props.containsKey("PROJECT_TYPE")) {
 				return props;
 			}
 		} catch (IOException e) {
 			
 		}
 		return null;
 	}
 	*/
 //	private class ConfirmResetDialog extends Window {
 		
 //		Button cancel = new Button("Cancel");
 //		Button overwrite = new Button();
 		
 		// TODO: Allow renaming project if the name already exists.
 		// For that, the packages, folders etc. must be renamed too.
 		
 		//TextField newName = new TextField("Use Another Name:");
 		//Button rename = new Button("Create New");
 		
 /*		public ConfirmResetDialog(String name) {
 			super(name +" already exists");
 			setModal(true);
 			setResizable(false);
 			setWidth("300px");
 			addComponent(new Label("Project called '"+name+"' already exists."));
 			addComponent(new Label("What to do?"));
 			HorizontalLayout ho = new HorizontalLayout();
 			ho.addComponent(cancel);
 			ho.addComponent(overwrite);
 			overwrite.setCaption("Overwrite "+name);
 			VerticalLayout ho2 = new VerticalLayout();
 			addComponent(ho);
 			addComponent(ho2);
 			cancel.addListener(new ClickListener() {
 				public void buttonClick(ClickEvent event) {
 					ConfirmResetDialog.this.close();
 				}
 			});
 		}*/
 	//}
 }
