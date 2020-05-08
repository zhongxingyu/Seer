 package reevent.web.signinout;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.model.StringResourceModel;
 import reevent.web.HomePage;
 import reevent.web.ReEventSession;
 import reevent.web.account.myAccount;
 import reevent.web.myEvents.myEvents;
 
 public class SignOutPanel extends Panel {
     Label userSignedInLabel;
     Form<?> signOutForm;
     Link myAccount;
     Link myEvents;
 
     public SignOutPanel(String id) {
         super(id);
 
         PropertyModel<String> firstNameModel = new PropertyModel<String>(ReEventSession.userSignedInModel, "firstName");
         StringResourceModel realNameMessage =
                 new StringResourceModel("you.are.logged.in.as.user", firstNameModel);
         add(userSignedInLabel = new Label("userSignedInLabel", realNameMessage));
         
         add(signOutForm = new Form("signOutForm"){
             @Override
             protected void onSubmit() {
                 ReEventSession.get().signOut();
                 setResponsePage(HomePage.class);
             }
         });
         
         add(myAccount = new BookmarkablePageLink("myAccount", myAccount.class));
         add(myEvents = new BookmarkablePageLink("myEvents", myEvents.class));
     }
 
 }
