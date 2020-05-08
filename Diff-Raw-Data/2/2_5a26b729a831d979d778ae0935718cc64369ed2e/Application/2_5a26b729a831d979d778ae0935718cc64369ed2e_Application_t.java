 package controllers;
 import models.*;
 
 import java.util.*;
 
 import models.Business;
 import models.Admin;
 import models.Login;
 import play.*;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.*;
 import play.db.ebean.Model;
 import views.html.*;
 import play.mvc.Http.*;

 
 
 
 public class Application extends Controller {
 
 	static Form<Business> businessForm = Form.form(Business.class);
 	static Form<Admin> formAdmin = Form.form(Admin.class);
 	static List <Business> myBusinesses = new Model.Finder(String.class, Business.class).all();
 	//static Form<Login>loginForm = Form.form(Login.class);
 	//static Form <User>userForm = Form.form(User.class);
 	static Form<Login> loginForm =Form.form(Login.class);
 
 
 
 	//@Security.Authenticated(Secured.class)
 	public static Result index()
 	{
 
 		return ok (index.render());
 	}
 
 	public static Result businesses()
 	{
 
 		return ok(registerBusiness.render(myBusinesses, businessForm));
 
 	}
 
 
 	public static Result registerBusiness()
 	{
 		return ok(registerBusiness.render(myBusinesses,businessForm));
 	}
 
 
 	public static Result getBusinesses()
 	{
 
 		List<Business> businesses = new Model.Finder(String.class, Business.class).all();
 		return ok (Json.toJson(businesses));
 	}
 
 	public static Result newBusiness() {
 		Form<Business> filledForm = businessForm.bindFromRequest();
 		if(filledForm.hasErrors())
 		{
 			System.out.println("i am error");
 			return badRequest(
 					views.html.registerBusiness.render(myBusinesses, filledForm));
 		}
 		else {
 			Business.create(filledForm.get());
 			return redirect(routes.Application.registerBusiness());
 		}
 	}
 
 	public static Result deleteBusiness(Long id)
 	{
 
 		Business.delete(id);
 		return redirect(routes.Application.goToBusinessListPage(""));
 		//return redirect("/");
 	}
 
 
 
 	public static Result saveBusiness(Long id)
 
 	{
 
 		Form<Business> filledForm = businessForm.bindFromRequest();
 		if(filledForm.hasErrors())
 		{
 
 			return badRequest(
 					views.html.registerBusiness.render(Business.all(),filledForm));
 		}
 		else {
 
 			Business b = filledForm.get();
 			b.id = id;
 			Business.create(b);
 			return redirect(routes.Application.businesses());
 		}
 
 	}
 
 	//	public static Result login()
 	//	
 	//	{
 	//		return ok (login.render(businessForm(Login.class)));
 	//		
 	//	}
 
 	public static Result addAdmin(){
 		Form<Admin> form = formAdmin.bindFromRequest();
 		Admin myadmin = form.get();
 		myadmin.save();
 		return redirect(routes.Application.registerBusiness());
 
 	}
 
 
 	public static Result getAllAdmins(){
 
 		List<Admin> admins = new Model.Finder(String.class, Admin.class).all();
 		return ok (Json.toJson(admins));
 
 
 	}
 
 	public static Result authenticate() {
 		Form<Login>loginForm = Form.form(Login.class).bindFromRequest();
 		if (loginForm.hasErrors()) {
 			return badRequest(login.render(loginForm));
 		} else {
 			session().clear();
 
 			session("email", loginForm.get().email);
 			return redirect(
 					routes.Application.index()
 					);
 		}
 
 	}
 
 	public static class Login {
 
 		public String email;
 		public String password;
 		public User user;
 		public String validate() 
 		{
 			user=User.authenticate(email, password);
 			if (user == null) {
 				return "Invalid user or password";
 			}
 
 			return null;
 		}
 
 	}
 
 	public static Result adminPage()
 	{
 
 		return ok(AdminRegistration.render(formAdmin));
 	}
 
 
 	public static Result login()
 	{
 		return ok(login.render(Form.form(Login.class)));
 	}
 
 	public static Result logout() {
 		session().clear();
 		flash("success", "You've been logged out");
 		return redirect(
 				routes.Application.login()
 				);
 	}
 	public static Result editBusiness(Long id)
 	{
 		Business b = Business.find.ref(id);
 		Form<Business> filledForm;
 		filledForm = businessForm.fill(b);
 		System.out.println("The id is " + b.getId());
 		System.out.println("business is: " + b.getBusinessName());
 		System.out.println("Filled form is: " + filledForm);
 		return ok(views.html.editBusiness.render(filledForm,  b.id));
 
 	}
 
 	public static Result updateBusiness(Long id)
 	{
 		Form<Business> businessForm= Form.form(Business.class).bindFromRequest();
 		businessForm.get().update(id);
 		System.out.println("i am here");
 		return redirect(routes.Application.goToBusinessListPage(""));
 
 	}
 	public static Result goToBusinessListPage(String businessType){
 		List<Business> businesses;
 		if(businessType=="all"){
 			businesses = new Model.Finder(String.class, Business.class).all();
 
 		} else {
 
 			businesses = Business.find.where().like("businessType",businessType).findList();
        }
 	     return ok(businesslist.render(businesses, businessForm));
 
 	}
 
 
 }
 
 
