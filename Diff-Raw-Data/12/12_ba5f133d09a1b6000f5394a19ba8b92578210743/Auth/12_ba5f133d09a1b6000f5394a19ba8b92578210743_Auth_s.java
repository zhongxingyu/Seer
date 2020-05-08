 package controllers;
 
 import java.util.Map;
 
 import org.expressme.openid.Association;
 import org.expressme.openid.Authentication;
 import org.expressme.openid.Base64;
 import org.expressme.openid.Endpoint;
 import org.expressme.openid.OpenIdManager;
 
 import play.api.templates.Html;
 import play.data.Form;
 import play.data.validation.Constraints.*;
 
 import play.libs.F.Function;
 import play.libs.F.Promise;
 import play.libs.OpenID;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import models.*;
 import views.html.auth.*;
 
 public class Auth extends Controller
 {
   public static class SignUp
   {
     @Required
     public String givenName;
     @Required
     public String familyName;
     @Required
     public String email;
     @Required
     public String password1;
     @Required
     public String password2;
 
     public String validate()
     {
       if (User.exists(email))
       {
         return "Email address is already in use.";
       }
       if (!password1.equals(password2))
       {
         return "Password mismatch.";
       }
       return null;
     }
   }
 
   public static Result signUp()
   {
     return ok(signup.render(form(SignUp.class)));
   }
 
   public static Result validateSignUp()
   {
     Form<SignUp> form = form(SignUp.class).bindFromRequest();
     if (form.hasErrors())
     {
       return badRequest(signup.render(form));
     }
 
     SignUp s = form.get();
     User user = new User(s.email, s.password1, s.givenName, s.familyName);
     User.create(user);
     session("email", s.email);
     flash("success", "Account created. Please check your email for an activation message.");
     return Application.index();
   }
 
   public static class Login
   {
     @Required
     public String userID;
     @Required
     public String password;
 
     public String validate()
     {
       if (User.connect(userID, password) == null)
       {
         return "Incorrect email or password.";
       }
       if (!User.get(userID).isActivated)
       {
         //TODO: return "Please check your email for an activation message.";
       }
       return null;
     }
   }
   
   private static Html renderLoginEmpty()
   {
     return login.render(form(Login.class), form(LoginOpenID.class));
   }
   
   private static Html renderLoginFromRequest()
   {
     return login.render(form(Login.class).bindFromRequest(),
                         form(LoginOpenID.class).bindFromRequest());
   }
 
   public static Result login()
   {
     return ok(renderLoginEmpty());
   }
 
   public static Result validateLogin()
   {
     Form<Login> form = form(Login.class).bindFromRequest();
     if (form.hasErrors())
     {
       return badRequest(renderLoginFromRequest());
     }
 
     Login l = form.get();
     session("email", l.userID);
     flash("success", "Welcome back.");
     return Application.index();
   }
 
   public static class LoginOpenID
   {
     @Required
     public String openID;
   }
 
   // JOpenID Vars
   private static final String ATTR_MAC = "openid_mac";
   private static final String ATTR_ALIAS = "openid_alias";
   private static OpenIdManager manager = new OpenIdManager();
   static
   {
     //manager.setRealm("http://localhost"); // change to your domain
     manager.setReturnTo(routes.Auth.openIdCallback().absoluteURL(request()));
   }
 
   public static Result openIdLogin(String provider, String error)
   {
     if (error.length() != 0)
     {
       flash("error", error);
     }
 
     if (provider.length() == 0)
     {
       return login();
     }
 
     //String openID = null;
     Endpoint endpoint = null;
     Association association = null;
 
     // Handle all known providers
     if (provider.equals("google"))
     {
       //openID = "https://www.google.com/accounts/o8/id";
       endpoint = manager.lookupEndpoint("Google");
       association = manager.lookupAssociation(endpoint);
     }
     else
     {
       String providerError = "Unhandled OpenID provider: " + provider + ".";
       if (error.length() != 0)
       {
         providerError = error + "\n" + providerError;
       }
       flash("error", providerError);
       return badRequest(renderLoginFromRequest());
     }
 
     session(ATTR_MAC, association.getMacKey());
     session(ATTR_ALIAS, endpoint.getAlias());
     String url = manager.getAuthenticationUrl(endpoint, association);
     return redirect(url);
   }
 
   public static Result openIdGeneric()
   {
     Form<LoginOpenID> form = form(LoginOpenID.class).bindFromRequest();
     if (form.hasErrors())
     {
       return badRequest(renderLoginFromRequest());
     }
 
     String openID = form.get().openID;
     String returnURL = routes.Auth.openIdCallback().absoluteURL(request());
     Promise<String> p = OpenID.redirectURL(openID, returnURL);
 
     return async(p.map(new Function<String, Result>()
     {
       public Result apply(String redirectURL)
       {
         return redirect(redirectURL);
       }
     }));
   }
   
   public static String getQueryParam(Map<String, String []> params, String key)
   {
     String [] values = params.get(key);
     if (values == null || values.length > 1)
     {
       return null;
     }
     return values[0];
   }
 
   public static Result openIdCallback()
   {
     //debugPrintQueryMap(request().queryString());
     try
     {
       Nonce.processNonce(getQueryParam(request().queryString(), "openid.response_nonce"));
 
       byte [] mac_key = Base64.decode(session(ATTR_MAC));
       String alias = session(ATTR_ALIAS);
       Authentication authentication = manager.getAuthentication(request().queryString(), mac_key, alias);
       String email = authentication.getEmail();
       //String identity = authentication.getIdentity();
       //System.out.println(identity + ", " + email + ", " + authentication);
      if (User.exists(email))
      {
        flash("success", "Welcome back.");
      }
      else
       {
         User u = new User(email, null, authentication.getFirstname(), authentication.getLastname());
         u.isOpenID = true;
         User.create(u);
         flash("success", "Welcome! Your OpenID has been registered.");
       }
      session("email", email);
       return Application.index();
     }
     catch (Exception e)
     {
       flash("error", "An error occured during OpenID authentication: " + e.getMessage());
       return badRequest(renderLoginFromRequest());
     }
   }
 
   private static void debugPrintQueryMap(Map<String, String []> params)
   {
     for (String key : params.keySet())
     {
       System.out.println("k: " + key);
       for (String v : params.get(key))
       {
         System.out.println("  v: " + v);
       }
     }
   }
 
   private static void debugPrintUserInfoAttributes(Map<String, String> attributes)
   {
     for (String key : attributes.keySet())
     {
       System.out.println("k: " + key + "; v: " + attributes.get(key));
     }
   }
 
   public static Result logout()
   {
     session().clear();
     flash("success", "You are now logged out.");
     return Application.index();
   }
 
   public static User getSessionUser()
   {
     if (session().containsKey("email") &&
         User.exists(session().get("email")))
     {
       return User.get(session().get("email"));
     }
     return null;
   }
 
   public static boolean isSessionLoggedIn()
   {
     return getSessionUser() != null;
   }
 
   public static boolean isSessionCrew()
   {
     User u = getSessionUser();
     return u != null && u.isCrew;
   }
 
   public static boolean isSessionCurator()
   {
     User u = getSessionUser();
     return u != null && u.isCurator;
   }
 
   public static boolean isSessionAdmin()
   {
     User u = getSessionUser();
     return u != null && u.isAdmin;
   }
 }
