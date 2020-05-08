 package at.ac.tuwien.swag.webapp.in.form;
 
 import javax.persistence.EntityManager;
 
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.model.Model;
 
 import at.ac.tuwien.swag.model.dao.UserDAO;
 import at.ac.tuwien.swag.model.domain.User;
 import at.ac.tuwien.swag.webapp.SwagWebSession;
 
 import com.google.inject.Inject;
 
 public class PasswordForm extends Form<Void> {
     private static final long serialVersionUID = 6040480253914226510L;
 
     private final PasswordTextField password1;
     private final PasswordTextField password2;
 
     @Inject
     private EntityManager em;
 
     private UserDAO users;
     private User user;
 
     public PasswordForm(String id) {
         super(id);
 
         password1 = new PasswordTextField("password1", new Model<String>());
         password2 = new PasswordTextField("password2", new Model<String>());
 
         add(password1);
         add(password2);
     }
 
     @Override
     protected void onSubmit() {
 
         users = new UserDAO(em);
 
         String username = ((SwagWebSession) getSession()).getUsername();
 
         if (!password1.getModel().getObject().equals(password2.getModel().getObject())) {
             error("The two passwords are not equal");
             return;
         }
 
        user = users.findByUsername(username);
 
        user = users.findByUsername(username);
         user.setPassword(password1.getModel().getObject());
 
         users.update(user);
 
         error("The password was updated");
     }
 }
