 package reevent.web.account;
 
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.spring.injection.annot.SpringBean;
 import reevent.service.UserService;
 import reevent.web.ReEventApplication;
 import reevent.web.ReEventSession;
 import reevent.web.Template;
 
 @AuthorizeInstantiation("USER")
 public class DeleteAccount extends Template {
 
    @SpringBean
 	UserService users;
 	
 	public DeleteAccount(){
 
 		Form deleteForm = new Form("deleteForm");
 
 		Button yesButton = new Button("yes") {
 			public void onSubmit() {
                users.disable(ReEventSession.get().getUserSignedIn());
 				ReEventSession.get().signOut();
 				setResponsePage(ReEventApplication.get().getHomePage());	
 			}
 		};
 		deleteForm.add(yesButton);
 
 		Button noButton = new Button("no") {
 			public void onSubmit() {
 				setResponsePage(myAccount.class);
 			}
 		};
 		noButton.setDefaultFormProcessing(false);
 		deleteForm.add(noButton);
 
 		add(deleteForm);
 	}
 }
