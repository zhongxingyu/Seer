 package ch.erebetez.activititestapp4.ui;
 
 import ch.erebetez.activititestapp4.bpmn.forms.*;
 import ch.erebetez.activititestapp4.ui.util.UserTaskFormContainer;
 import ch.erebetez.activititestapp4.ui.widgets.FormViewer;
 import ch.erebetez.activititestapp4.ui.widgets.MyTaskViewer;
 
 import com.vaadin.ui.*;
 
 public class ActivityWindow extends CustomComponent {
 	private static final long serialVersionUID = -7213851977742853381L;
 
 
 	FormViewer formViewer = null;
 	MyTaskViewer mytaskViewer = null;
 	UserTaskFormContainer userTaskFormContainer = null;
 
 	public ActivityWindow() {
 
 		setCaption("My Activiys");
 
 		VerticalLayout layout = new VerticalLayout();
 		setCompositionRoot(layout);
 
 		layout.addComponent(new Label("Select a task to activate the Form"));
 
 		layout.addComponent(getMytaskViewer());
 		layout.addComponent(getFormViewer());
 
 	}
 
 	public MyTaskViewer getMytaskViewer() {
 		if (mytaskViewer == null) {
 			mytaskViewer = new MyTaskViewer(getFormViewer());
 		}
 		return mytaskViewer;
 	}
 
 	public FormViewer getFormViewer() {
 		if (formViewer == null) {
 			formViewer = new FormViewer(getUserTaskFormContainer());
 		}
 		return formViewer;
 	}
 
 	// FIXME userTaskContainer does not belong here!
 	private UserTaskFormContainer getUserTaskFormContainer() {
 		if (userTaskFormContainer == null) {			
 			userTaskFormContainer = new UserTaskFormContainer();
 			
 			userTaskFormContainer.registerForm(DilutionReportForm.FORM_KEY,
 					DilutionReportForm.class);
 			userTaskFormContainer.registerForm(IsInventoryItemOkForm.FORM_KEY,
 					IsInventoryItemOkForm.class);			
 			userTaskFormContainer.registerForm(CheckInventoryReportForm.FORM_KEY,
 					CheckInventoryReportForm.class);					
			userTaskFormContainer.registerForm(CreateOosForm.FORM_KEY,
					CreateOosForm.class);
 		}
 		return userTaskFormContainer;
 	}
 
 }
