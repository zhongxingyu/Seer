 package at.ac.tuwien.swag.webapp.in.form;
 
 import javax.persistence.EntityManager;
 
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.model.Model;
 
 import at.ac.tuwien.swag.model.dao.UserDAO;
 import at.ac.tuwien.swag.model.domain.User;
 import at.ac.tuwien.swag.webapp.SwagWebSession;
 
 import com.google.inject.Inject;
 
 public class SettingsForm extends Form<Void> {
     private static final long serialVersionUID = 6040480253914226510L;
 
     private RequiredTextField<String> fullname;
     private RequiredTextField<String> email;
     private TextArea<String> address;
 
     @Inject
     private EntityManager em;
 
     private UserDAO users;
     private User user;
 
     public SettingsForm(String id) {
         super(id);
         users = new UserDAO(em);
 
         String username = ((SwagWebSession) getSession()).getUsername();
 
        user = users.findByName(username).get(0);
 
         // TODO inject current values
 
         fullname = new RequiredTextField<String>("fullname", new Model<String>(user.getFullname()));
         email = new RequiredTextField<String>("email", new Model<String>(user.getEmail()));
         address = new TextArea<String>("address", new Model<String>(user.getAddress()));
         address.setRequired(true);
 
         add(fullname);
         add(email);
         add(address);
     }
 
     @Override
     protected void onSubmit() {
         String username = ((SwagWebSession) getSession()).getUsername();
 
        user = users.findByName(username).get(0);
         user.setFullname(fullname.getModel().getObject());
         user.setEmail(email.getModel().getObject());
         user.setAddress(address.getModel().getObject());
 
         users.update(user);
 
         info("The user settings were updated");
     }
 }
