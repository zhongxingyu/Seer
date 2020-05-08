 package controllers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.Transient;
 
 import models.Setting;
 import models.User;
 import play.Logger;
 import play.data.Form;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.data.validation.Constraints.Required;
 import play.data.validation.ValidationError;
 import play.mvc.*;
 
 public class Administrator extends Controller {
 	
 	public static class AdminForms {
 		public Form<CreateAdminForm> createAdminForm = Administrator.createAdminForm;
 		public Form<SetWSForm> setWSForm = Administrator.setWSForm;
 		public Form<SetUploadDirForm> setUploadDirForm = Administrator.setUploadDirForm;
 		public Form<ConfigureEmailForm> configureEmailForm = Administrator.configureEmailForm;
 		public Form<User> userForm = Administrator.userForm;
 		public Form<GuestUser> guestForm = Administrator.guestForm;
 		public Form<GlobalPermissions> globalForm = Administrator.globalForm;
 		public Form<SetJobCleanupForm> setJobCleanupForm = Administrator.setJobCleanupForm;
 		public Form<SetVisualThemeForm> setVisualThemeForm = Administrator.setVisualThemeForm;
 	}
 	
 	public final static Form<CreateAdminForm> createAdminForm = form(CreateAdminForm.class);
 	public static class CreateAdminForm {
 		@Constraints.Required
 		@Formats.NonEmpty
 		@Constraints.Email
 		public String email;
 
 		@Constraints.Required
 		@Constraints.MinLength(6)
 		public String password;
 		
 		@Constraints.MinLength(6)
 		public String repeatPassword;
 		
 		public static void validate(Form<CreateAdminForm> filledForm) {
 			if (User.findByEmail(filledForm.field("email").valueOr("")) != null)
 				filledForm.reject("email", "That e-mail address is already taken");
 			
 			if (!filledForm.field("password").valueOr("").equals("") && !filledForm.field("password").valueOr("").equals(filledForm.field("repeatPassword").value()))
 				filledForm.reject("repeatPassword", "Password doesn't match.");
 		}
 	}
 	
 	public final static Form<SetWSForm> setWSForm = form(SetWSForm.class);
 	public static class SetWSForm {
         @Required
         public String endpoint;
         
         public String authid;
         
         public String secret;
         
         public static void validate(Form<SetWSForm> filledForm) {
         	if (filledForm.field("endpoint").valueOr("").equals(""))
         		filledForm.reject("endpoint", "Invalid endpoint URL.");
         }
     }
 	
 	public final static Form<SetUploadDirForm> setUploadDirForm = form(SetUploadDirForm.class);
 	public static class SetUploadDirForm {
         @Required
         public String uploaddir;
         
         public static void validate(Form<SetUploadDirForm> filledForm) {
         	String uploadPath = filledForm.field("uploaddir").valueOr("");
         	if (!uploadPath.endsWith(System.getProperty("file.separator")))
         		uploadPath += System.getProperty("file.separator");
         	File dir = new File(uploadPath);
         	if (!dir.exists())
         		filledForm.reject("uploaddir", "The directory does not exist.");
         	if (!dir.isDirectory())
         		filledForm.reject("uploaddir", "The path does not point to a directory.");
         }
     }
 	
 	public final static Form<ConfigureEmailForm> configureEmailForm = form(ConfigureEmailForm.class);
 	public static class ConfigureEmailForm {
 		@Required
 		public String smtp;
 		
 		public String username;
 		
 		public String password;
 		
 		public static void validate(Form<ConfigureEmailForm> filledForm) {
 			if (filledForm.field("smtp").valueOr("").equals(""))
 	    		filledForm.reject("smtp", "Invalid SMTP IP / domain name.");
 		}
 	}
 	
 	public final static Form<SetJobCleanupForm> setJobCleanupForm = form(SetJobCleanupForm.class);
 	public static class SetJobCleanupForm {
         @Required
         public String jobcleanup;
         
         public static void validate(Form<SetJobCleanupForm> filledForm) {
         	String jobCleanupString = filledForm.field("jobcleanup").valueOr("0");
         	Long jobCleanup = null;
         	try {
         		jobCleanup = Long.parseLong(jobCleanupString);
         	} catch (NumberFormatException e) {
         		filledForm.reject("jobcleanup", "Please enter a number.");
         	}
         	if (jobCleanup != null && jobCleanup < 0)
         		filledForm.reject("jobcleanup", "Please enter a positive number.");
         }
     }
 	
 	public final static Form<SetVisualThemeForm> setVisualThemeForm = form(SetVisualThemeForm.class);
 	public static class SetVisualThemeForm {
        @Required
         public String theme;
         
         public static void validate(Form<SetVisualThemeForm> filledForm) {
         	String theme = filledForm.field("theme").valueOr("");
        	File themeDir = new File("public/stylesheets/themes/"+theme);
        	if (!themeDir.exists() || !themeDir.isDirectory())
        		filledForm.reject("theme", "The theme \""+theme+"\" does not exist.");
         }
         
         public static List<String> themes = new ArrayList<String>();
         public static void refreshList() {
         	synchronized(themes) {
 	        	File themeDir = new File("public/stylesheets/themes/");
 	        	if (!themeDir.isDirectory())
 	        		return;
 	        	
 	        	themes = new ArrayList<String>();
 	        	for (String theme : themeDir.list())
 	        		themes.add(theme);
         	}
         }
     }
 	
 	final static Form<User> userForm = form(User.class);
 	
 	final static Form<GuestUser> guestForm = form(GuestUser.class);
 	public static class GuestUser {
 		@Constraints.MinLength(1)
 		@Constraints.Pattern("[^{}\\[\\]();:'\"<>]+") // Avoid breaking JavaScript code in templates
 		public String name;
 	}
 	
 	final static Form<GlobalPermissions> globalForm = form(GlobalPermissions.class);
 	public static class GlobalPermissions {
 		public boolean allowGuests;
 		public boolean hideAdvancedOptions;
 	}
 	
 	public static Result getSettings() {
 		if (FirstUse.isFirstUse())
 			return redirect(routes.FirstUse.getFirstUse());
 
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null || !user.admin)
 			return redirect(routes.Login.login());
 
 		List<User> users = User.find.orderBy("admin, name, email").findList();
 		AdminForms forms = new AdminForms();
 		SetVisualThemeForm.refreshList();
 		
 		return ok(views.html.Administrator.settings.render(forms, users));
 	}
 	
 	public static Result postSettings() {
 		if (FirstUse.isFirstUse())
 			return redirect(routes.FirstUse.getFirstUse());
 
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null || !user.admin)
 			return redirect(routes.Login.login());
 		
 		AdminForms forms = new AdminForms();
 		
 		String formName = request().queryString().containsKey("formName") ? request().queryString().get("formName")[0] :
 							request().body().asFormUrlEncoded().containsKey("formName") ? request().body().asFormUrlEncoded().get("formName")[0] :
 							null;
 		if (formName == null) {
 			flash("error", "Form not found. Submitted information ignored :(");
 			redirect(routes.Administrator.getSettings());
 		}
 		
 		if ("updateGlobalPermissions".equals(formName)) {
 			Form<GlobalPermissions> filledForm = globalForm.bindFromRequest();
 //			GlobalPermissions.validate(filledForm);
 			
 			flash("settings.usertab", "global");
 			
 			if (filledForm.hasErrors()) {
 				forms.globalForm = filledForm;
 				List<User> users = User.find.orderBy("admin, name, email").findList();
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 				
 			} else {
 				Setting.set("guest.allowGuests", filledForm.field("allowGuests").valueOr("false"));
 				Setting.set("jobs.hideAdvancedOptions", filledForm.field("hideAdvancedOptions").valueOr("false"));
 				flash("success", "Global permissions was updated successfully!");
 				return redirect(routes.Administrator.getSettings());
 			}
 		}
 		
 		if ("updateGuest".equals(formName)) {
 			Form<GuestUser> filledForm = guestForm.bindFromRequest();
 //			GuestUser.validate(filledForm);
 			
 			flash("settings.usertab", "guest");
 			
 			if (filledForm.hasErrors()) {
 				forms.guestForm = filledForm;
 				List<User> users = User.find.orderBy("admin, name, email").findList();
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 				
 			} else {
 				Setting.set("guest.name", filledForm.field("name").valueOr("Guest"));
 				flash("success", "Guest was updated successfully!");
 				return redirect(routes.Administrator.getSettings());
 			}
 		}
 		
 		if ("updateUser".equals(formName)) {
 			Form<User> filledForm = userForm.bindFromRequest();
 			Long userId = request().queryString().containsKey("userid") ? Long.parseLong(request().queryString().get("userid")[0])
 					: request().body().asFormUrlEncoded().containsKey("userid") ? Long.parseLong(request().body().asFormUrlEncoded().get("userid")[0])
 					: 0L;
 			flash("settings.usertab", userId + "");
 			
 			User updateUser = User.findById(userId);
 			if (updateUser == null) {
 				flash("success", "Hmm, that's weird; the user was not found. Nothing was changed...");
 				return redirect(routes.Administrator.getSettings());
 			}
 			
 			updateUser.validateChange(filledForm);
 
 			filledForm.errors().remove("password"); // admin does not require the users password to edit the user
 			
 			if (!updateUser.hasChanges(filledForm)) {
 				flash("success", "You did not submit any changes. No changes were made.");
 				return redirect(routes.Administrator.getSettings());
 
 			} else if (filledForm.hasErrors()) {
 				List<User> users = User.find.orderBy("admin, name, email").findList();
 				forms.userForm = filledForm;
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 
 			} else {
 				updateUser.name = filledForm.field("name").valueOr("");
 				updateUser.admin = filledForm.field("admin").valueOr("").equals("true");
 				
 				if (!filledForm.field("email").valueOr("").equals(updateUser.email)) {
 					updateUser.email = filledForm.field("email").valueOr("");
 					updateUser.active = false;
 					updateUser.makeNewActivationUid();
 
 					String activateUrl = routes.Account.showActivateForm(updateUser.email, updateUser.getActivationUid()).absoluteURL(request());
 					String html = views.html.Account.emailActivate.render(activateUrl).body();
 					String text = "Go to this link to activate your account: " + activateUrl;
 					if (!Account.sendEmail("Activate your account", html, text, updateUser.name, updateUser.email))
 						flash("error", "Was unable to send the e-mail.");
 
 				}
 				
 				updateUser.save();
 				if (updateUser.id == user.id) {
 					session("name", user.name);
 					session("email", user.email);
 				}
 				
 				flash("success", "Your changes were saved successfully!");
 				return redirect(routes.Administrator.getSettings());
 			}
 		}
 		
 		if ("resetPassword".equals(formName)) {
 			Long userId = request().queryString().containsKey("userid") ? Long.parseLong(request().queryString().get("userid")[0])
 					: request().body().asFormUrlEncoded().containsKey("userid") ? Long.parseLong(request().body().asFormUrlEncoded().get("userid")[0])
 					: 0L;
 			flash("settings.usertab", userId + "");
 
 			User resetUser = User.findById(userId);
 
 			if (resetUser.active) {
 				resetUser.makeNewActivationUid();
 				resetUser.save();
 				String resetUrl = routes.Account.showResetPasswordForm(resetUser.email, resetUser.getActivationUid()).absoluteURL(request());
 				String html = views.html.Account.emailResetPassword.render(resetUrl).body();
 				String text = "Go to this link to change your password: " + resetUrl;
 				if (Account.sendEmail("Reset your password", html, text, resetUser.name, resetUser.email))
 					flash("success", "A password reset link was sent to " + resetUser.name);
 				else
 					flash("error", "Was unable to send the e-mail. Please notify the owners of this website so they can fix their e-mail settings.");
 				
 			} else {
 				resetUser.makeNewActivationUid();
 				resetUser.save();
 				String activateUrl = routes.Account.showActivateForm(resetUser.email, resetUser.getActivationUid()).absoluteURL(request());
 				String html = views.html.Account.emailActivate.render(activateUrl).body();
 				String text = "Go to this link to activate your account: " + activateUrl;
 
 				if (Account.sendEmail("Activate your account", html, text, resetUser.name, resetUser.email))
 					flash("success", "An account activation link was sent to " + resetUser.name);
 				else
 					flash("error", "Was unable to send the e-mail. Please notify the owners of this website so they can fix their e-mail settings.");
 			}
 
 			return redirect(routes.Administrator.getSettings());
 		}
 		
 		if ("deleteUser".equals(formName)) {
 			Long userId = request().queryString().containsKey("userid") ? Long.parseLong(request().queryString().get("userid")[0])
 					: request().body().asFormUrlEncoded().containsKey("userid") ? Long.parseLong(request().body().asFormUrlEncoded().get("userid")[0])
 					: 0L;
 			
 			User deleteUser = User.findById(userId);
 			flash("settings.usertab", deleteUser.id + "");
 			
 			if (deleteUser == null) {
 				flash("success", "Hmm, that's weird. The user was not found; nothing was changed...");
 				return redirect(routes.Administrator.getSettings());
 			}
 			
 			if (deleteUser.id == user.id) {
 				flash("error", "Only other admins can delete you, you cannot do it yourself");
 				return redirect(routes.Administrator.getSettings());
 			}
 			
 			deleteUser.delete();
 			flash("settings.usertab", "global");
 			flash("success", deleteUser.name + " was deleted");
 			return redirect(routes.Administrator.getSettings());
 		}
 		
 		if ("createUser".equals(formName)) {
 			Form<User> filledForm = userForm.bindFromRequest();
 			User.validateNew(filledForm);
 			
 			if (filledForm.hasErrors()) {
 				flash("settings.usertab", "adduser");
 				List<User> users = User.find.orderBy("admin, name, email").findList();
 				forms.userForm = filledForm;
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 				
 			} else {
 				User newUser = new User(filledForm.field("email").valueOr(""), filledForm.field("name").valueOr(""), "", filledForm.field("admin").valueOr("").equals("true"));
 				newUser.makeNewActivationUid();
 				newUser.save();
 				
 				String activateUrl = routes.Account.showActivateForm(newUser.email, newUser.getActivationUid()).absoluteURL(request());
 				String html = views.html.Account.emailActivate.render(activateUrl).body();
 				String text = "Go to this link to activate your account: " + activateUrl;
 	
 				if (!Account.sendEmail("Activate your account", html, text, newUser.name, newUser.email))
 					flash("error", "Was unable to send the e-mail. :(");
 	
 				flash("settings.usertab", newUser.id + "");
 				flash("success", "User " + newUser.name + " created successfully!");
 				
 				return redirect(routes.Administrator.getSettings());
 			}
 		}
 		
 		if ("setWS".equals(formName)) {
 			Form<Administrator.SetWSForm> filledForm = setWSForm.bindFromRequest();
 			Administrator.SetWSForm.validate(filledForm);
 			
 			if (filledForm.hasErrors()) {
 				List<User> users = User.find.orderBy("admin, name, email").findList();
 				forms.setWSForm = filledForm;
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 	        	
 	        } else {
 	        	Setting.set("dp2ws.endpoint", filledForm.field("endpoint").valueOr(""));
 	        	Setting.set("dp2ws.authid", filledForm.field("authid").valueOr(""));
 	        	if (Setting.get("dp2ws.secret") == null || !"".equals(filledForm.field("secret").value()))
 	        		Setting.set("dp2ws.secret", filledForm.field("secret").valueOr(""));
 	        	flash("success", "Pipeline 2 Web Service endpoint changed successfully!");
 	        	return redirect(routes.Administrator.getSettings());
 	        }
 		}
 		
 		if ("setUploadDir".equals(formName)) {
 			Form<Administrator.SetUploadDirForm> filledForm = setUploadDirForm.bindFromRequest();
 			Administrator.SetUploadDirForm.validate(filledForm);
 			
 			if(filledForm.hasErrors()) {
 	        	List<User> users = User.find.orderBy("admin, name, email").findList();
 				forms.setUploadDirForm = filledForm;
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 	        	
 	        } else {
 	        	String uploadPath = filledForm.field("uploaddir").valueOr("");
 	        	if (!uploadPath.endsWith(System.getProperty("file.separator")))
 	        		uploadPath += System.getProperty("file.separator");
 	        	Setting.set("uploads", uploadPath);
 	        	flash("success", "Uploads directory changed successfully!");
 	        	return redirect(routes.Administrator.getSettings());
 	        }
 		}
 		
 		if ("configureEmail".equals(formName)) {
 			Form<Administrator.ConfigureEmailForm> filledForm = configureEmailForm.bindFromRequest();
 			Administrator.ConfigureEmailForm.validate(filledForm);
 			
 			if(filledForm.hasErrors()) {
 				List<User> users = User.find.orderBy("admin, name, email").findList();
 				forms.configureEmailForm = filledForm;
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 
 			} else {
 				Setting.set("mail.username", filledForm.field("username").valueOr(""));
 				Setting.set("mail.password", filledForm.field("password").valueOr(""));
 				Setting.set("mail.smtp.host", filledForm.field("smtp").valueOr(""));
 				Setting.set("mail.smtp.port", "465"); // TODO: make configurable like the host
 				Setting.set("mail.smtp.ssl", "true"); // TODO: make configurable like the host
 				Setting.set("mail.from.name", "Pipeline 2");
 				Setting.set("mail.from.email", user.email);
 				flash("success", "Successfully changed e-mail settings!");
 	        	return redirect(routes.Administrator.getSettings());
 			}
 		}
 		
 		if ("setJobCleanup".equals(formName)) {
 			Form<Administrator.SetJobCleanupForm> filledForm = setJobCleanupForm.bindFromRequest();
 			Administrator.SetJobCleanupForm.validate(filledForm);
 			
 			if(filledForm.hasErrors()) {
 	        	List<User> users = User.find.orderBy("admin, name, email").findList();
 				forms.setJobCleanupForm  = filledForm;
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 	        	
 	        } else {
 	        	long jobCleanupTime = Long.parseLong(filledForm.field("jobcleanup").valueOr("0")) * 60000L;
 	        	Setting.set("jobs.deleteAfterDuration", jobCleanupTime+"");
 	        	flash("success", "Job cleanup time changed successfully!");
 	        	return redirect(routes.Administrator.getSettings());
 	        }
 		}
 		
 		if ("setVisualTheme".equals(formName)) {
 			Form<Administrator.SetVisualThemeForm> filledForm = setVisualThemeForm.bindFromRequest();
 			Administrator.SetVisualThemeForm.validate(filledForm);
 			
 			if(filledForm.hasErrors()) {
 				for (String key : filledForm.errors().keySet()) {
 					for (ValidationError error : filledForm.errors().get(key)) {
 						Logger.debug(key+": "+error.message());
 					}
 				}
 				
 	        	List<User> users = User.find.orderBy("admin, name, email").findList();
 				forms.setVisualThemeForm  = filledForm;
 				SetVisualThemeForm.refreshList();
 				return badRequest(views.html.Administrator.settings.render(forms, users));
 	        	
 	        } else {
 	        	String theme = filledForm.field("theme").valueOr("");
	        	flash("success", "Theme changed to \""+theme+"\" !");
 	        	if (theme.length() > 0)
 	        		theme += "/";
 	        	Setting.set("theme", theme);
 	        	return redirect(routes.Administrator.getSettings());
 	        }
 		}
 		
 		flash("error", "Form not found. Submitted information ignored :(");
 		return redirect(routes.Administrator.getSettings());
 	}
 	
 	/**
 	 * /admin/POST
 	 * When POST is not possible (as in javascript redirect), this route lets you treat a GET request as a POST.
 	 * @return
 	 */
 	public static Result getPostSettings() {
 		return postSettings();
 	}
 }
