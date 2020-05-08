 package eu.margiel.pages.admin.user;
 
 import static eu.margiel.utils.Components.*;
 import static eu.margiel.utils.Models.*;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 import org.wicketstuff.annotation.mount.MountPath;
 import org.wicketstuff.annotation.strategy.MountMixedParam;
 
 import eu.margiel.components.StaticImage;
 import eu.margiel.domain.Admin;
 import eu.margiel.domain.User;
 import eu.margiel.pages.admin.AdminBasePage;
 import eu.margiel.pages.javarsovia.c4p.SpeakerPhotoProvider;
 import eu.margiel.repositories.AdminRepository;
 import eu.margiel.utils.Models;
 
 @MountPath(path = "admin/users/view")
 @MountMixedParam(parameterNames = "id")
 public class AddUserPage extends AdminBasePage {
 
 	@SpringBean
 	private AdminRepository repository;
 	private transient SpeakerPhotoProvider provider = new SpeakerPhotoProvider("kapitula");
 
 	public AddUserPage() {
 		initPage(new Admin());
 	}
 
 	public AddUserPage(PageParameters params) {
 		initPage(repository.readByPrimaryKey(params.getAsInteger("id")));
 	}
 
 	private void initPage(Admin user) {
 		add(new UserForm(user));
 	}
 
 	@SuppressWarnings("serial")
 	private final class UserForm extends Form<User> {
 		private final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
 		private FileUploadField fileUploadField = new FileUploadField("photo");;
 		private final PasswordTextField repassword;
 		private final PasswordTextField password;
 		private final Admin admin;
 
 		private UserForm(Admin admin) {
 			super("form");
 			this.admin = admin;
 			this.repassword = withLabel("hasło", passwordField("repassword", new Model<String>(), admin.isNew()));
 			this.password = withLabel("Powtórz hasło", passwordField("password", new Model<String>(), admin.isNew()));
 			add(new StaticImage("photoView", provider.getPathTo(admin)).setVisible(admin.isNotNew()));
 			add(withLabel("Imię", textField("firstName", propertyModel(admin, "firstName"), true)));
 			add(withLabel("Nazwisko", textField("lastName", propertyModel(admin, "lastName"), true)));
 			add(withLabel("nazwa użytkownika", textField("userName", propertyModel(admin, "userName"), true)));
 			add(withLabel("e-mail", textField("mail", Models.<String> propertyModel(admin, "mail"), true)
 					.add(EmailAddressValidator.getInstance())));
 			add(withLabel("O sobie", richEditor("bio", propertyModel(admin, "bio"), true)));
 			add(fileUploadField);
 			add(password);
 			add(repassword);
 			add(cancelLink(ListUserPage.class));
 			add(new EqualPasswordInputValidator(password, repassword));
 			add(feedbackPanel);
 		}
 
 		@Override
 		protected void onSubmit() {
			setPasswordIfAvailable();
 			save();
 			setResponsePage(ListUserPage.class);
 		}
 
 		private void save() {
 			repository.save(admin);
 			provider.savePhoto(fileUploadField.getFileUpload(), admin);
 		}
 
		private void setPasswordIfAvailable() {
			String passwordString = password.getValue();
			if (passwordString.isEmpty() == false)
				admin.passwordWithEncryption(passwordString);
 		}
 
 	}
 }
