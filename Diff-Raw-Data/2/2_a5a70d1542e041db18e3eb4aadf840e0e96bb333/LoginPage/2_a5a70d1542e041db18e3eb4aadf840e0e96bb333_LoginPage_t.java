 package eu.margiel.pages.admin.login;
 
 import static eu.margiel.utils.Components.*;
 
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import eu.margiel.JavarsoviaBasePage;
 import eu.margiel.domain.Admin;
 import eu.margiel.domain.User;
 import eu.margiel.pages.admin.AdminHomePage;
 import eu.margiel.repositories.AdminRepository;
 
 @SuppressWarnings("serial")
 public class LoginPage extends JavarsoviaBasePage {
 	@SpringBean
 	private AdminRepository repository;
 	private TextField<String> userName = textField("userName", new Model<String>());
 	private PasswordTextField password = new PasswordTextField("password", new Model<String>());
 	private FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
 
 	public LoginPage() {
 		Form<Void> form = new Form<Void>("form") {
 			@Override
 			protected void onSubmit() {
 				checkLogin();
 			}
 
 		};
 		form.add(userName.setRequired(true));
 		form.add(password);
 		form.add(feedbackPanel);
 		add(form);
 	}
 
 	private void checkLogin() {
		if (userName.getInput().equals("admin") && repository.count() == 0) {
 			login(new Admin("admin"));
 			return;
 		}
 		User user = repository.readByUserName(userName.getInput());
 		if (user != null && user.passwordIsCorrect(password.getInput())) {
 			login(user);
 		} else {
 			reportLoginError(feedbackPanel);
 			return;
 		}
 	}
 
 	private void login(User user) {
 		getSession().setUser(user);
 		setResponsePage(AdminHomePage.class);
 	}
 
 	private void reportLoginError(FeedbackPanel feedbackPanel) {
 		feedbackPanel.error("Niepoprawna nazwa użytkownika lub hasło");
 	}
 }
