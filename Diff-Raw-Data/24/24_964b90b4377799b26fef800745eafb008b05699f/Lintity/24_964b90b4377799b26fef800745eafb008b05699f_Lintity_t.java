 package controllers;
 
 import helpers.subdomainchecker.SubdomainChecker;
 import lagoon.LintRobot;
 import models.Context;
 import models.Profile;
 import models.User;
 import models.UserLagoon;
 import play.data.validation.Required;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 
public class Lintity extends SecureLint.Lintity {
 
 	public static void registerUser(@Required String username, @Required String email, @Required String name) {
 
 		String context = SubdomainChecker.currentSubdomain(request);
 
 		JsonElement resp = null;
 
 		//Send user create request
 		try {
 			resp = LintRobot.createUser(username, email, name, context);
 		} catch(Exception ex) {
 
 			Application.doFlashError(ex.getMessage());
			renderTemplate("SecureLint/Lintity/register.html");
 		}
 
 		UserLagoon userLagoon = UserLagoon.findByExternalID(resp.getAsJsonObject().get("id").getAsLong());
 
 		//save user profiles
 		JsonElement profilesJson = resp.getAsJsonObject().get("profiles").getAsJsonArray();
 		Gson gson = new Gson();
 		Profile[] profiles  = gson.fromJson(profilesJson, Profile[].class);
 
 		User user = new User(userLagoon, currentContext());
 
 		for(Profile p : profiles) {
 
 			Profile userProfile = Profile.findByExternalId(p.id);
 			if(userProfile == null) {
 				userProfile = p.save();
 			}
 			user.profiles.add(userProfile);
 		}
 
 		user.save();
 		flash.success("User successfully created. View your email inbox.");
		renderTemplate("SecureLint/login.html");
 	}
 
 	public static void activateUser(@Required String password, @Required String passwordConf, String token) throws Exception {
 		if(!password.equals(passwordConf)) {
 			Application.doFlashError("Passwords don't match");
			renderTemplate("SecureLint/Lintity/activation.html", token);
 		}
 
 		String context = SubdomainChecker.currentSubdomain(request);
 		LintRobot.registerUser(password, token, context);
 		flash.success("User successfully registered.");
		renderTemplate("SecureLint/login.html");
 	}
 
 	public static Context currentContext() {
 		System.out.println("SUBDOMAIN: " + SubdomainChecker.currentSubdomain(request));
 		System.out.println(Context.findByName(SubdomainChecker.currentSubdomain(request)));
 		return Context.findByName(SubdomainChecker.currentSubdomain(request));
 	}
 
 }
