 package controllers;
 
 import java.io.File;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import play.mvc.*;
 import play.mvc.Http.RequestBody;
 import play.data.Form;
 
 import what.AdminLogin;
 import what.AdminAuth;
 import what.Facade;
 import what.LogfileUpload;
 
 
 public class Website extends Controller {
 	
 	// needed for admin login
 	private static Form<AdminLogin> form = form(AdminLogin.class);
 	// needed for logfilePathUpload
 	private static Form<LogfileUpload> log = form(LogfileUpload.class);
 
 	/**
 	 * method to render the index site with all available chart types
 	 * @return returns the html index site
 	 */
     public static Result index() {
     	return ok(views.html.index.render(ChartIndex.getInstance().getCharts()));
     }
     /**
      * method to dynamically return a chart site depending on the chartName
      * @param chartName the name of the requested chart
      * @return returns a valid http response, a website
      */
     public static Result chartType(String chartName) {
     	return ok(views.html.abstractChart.render(chartName));
     }
     /**
      * method to dynamically return a chart javascript depending on the chartName
      * @param chartName the name of the requested chart
      * @return returns a valid http response, a javascript
      */
     public static Result chartJS(String chartName) {
     	return ok(views.html.chartJS.render(chartName)).as("application/javascript");
     }
     
     /**
      * requests static resources for a chart
      * @param chartName the chart name
      * @param file the path to the file requested
      * @return returns the file
      */
     public static Result chartStatics(String chartName, String file) {
     	return ok(new File("./charts/" + chartName + "/" + file));
     }
     
     /**
      * method to process chart requests
      * @return returns the needed chart data
      */
     //TolerantText because ContentType is json
     @BodyParser.Of(BodyParser.TolerantText.class)
     public static Result chartRequest() {
     	try {
 			JSONObject json = new JSONObject(request().body().asText());
 			
 			//String chart = json.getString("chart");
 		    return ok(Facade.getFacadeIstance().computeChart(json).toString());
     	} catch (JSONException e) {
     		return internalServerError("Something went wrong :(");
     	}
     }
     
     /**
      * change the language and redirect to current path with new language
      * @param path the current path
      * @return returns the same page in chosen language
      */
     public static Result changeLanguage() {
     	RequestBody body = request().body();
     	if(body.asFormUrlEncoded() != null && body.asFormUrlEncoded().containsKey("lang")) {
 	    	session("lang", request().body().asFormUrlEncoded().get("lang")[0]);
     	}
     	return redirect(body.asFormUrlEncoded().get("path")[0]);
     }
     
     /**
      * display login form
      * @return returns a http response with the form page
      */
     public static Result adminLogin (){
     	return ok(views.html.login.render(form));
     }
     
     /**
      * method to validate the login form
      * @return returns the admin page or a badrequest
      */
     public static Result login() {
 		Form<AdminLogin> filledForm = form.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			return badRequest(views.html.login.render(filledForm));
 		}
 		session().put("username", filledForm.get().username);
 		return redirect(routes.Website.admin());
 	}
     
     /**
      * method to display admin page if authorized
      * @return returns admin page or error
      */
     @Security.Authenticated(AdminAuth.class)
 	public static Result admin() {
 		return ok(views.html.adminPage.render(log));
 	}
     
     /**
      * method to pass new log file path to the parser
      * see what.LogfileUpload
      * @return returns to the admin page
      */
     @Security.Authenticated(AdminAuth.class)
     public static Result logfile() {
     	Form<LogfileUpload> filledForm = log.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			return badRequest(views.html.adminPage.render(filledForm));
		} 
     	return ok(views.html.adminPage.render(log));
     }
     
     
     /**
      * method to log out from admin
      * @return returns to index
      */
     public static Result logout() {
 		session().clear();
 		return redirect(routes.Website.index());
 	}
 }
