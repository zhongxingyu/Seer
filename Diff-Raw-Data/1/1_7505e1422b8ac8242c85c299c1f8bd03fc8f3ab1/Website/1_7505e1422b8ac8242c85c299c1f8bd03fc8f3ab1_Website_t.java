 package web.controllers;
 
 import java.awt.Color;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Map;
 
 import org.apache.batik.transcoder.TranscoderException;
 import org.apache.batik.transcoder.TranscoderInput;
 import org.apache.batik.transcoder.TranscoderOutput;
 import org.apache.batik.transcoder.image.PNGTranscoder;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Security;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Http.RequestBody;
 import play.mvc.Result;
 import play.data.Form;
 
 import web.AdminAuth;
 import web.AdminLogin;
 import web.ChartHelper;
 import web.ChartHistory;
 import web.ChartIndex;
 import web.LogfileUpload;
 import what.Facade;
 import what.FileHelper;
 import what.Printer;
 
 /**
  * Class that handles all HTTP requests. the routes redirect a request to one of the methods
  * of this class. This class may handle the request itself or call other classes and methods.
  * all methods return a HTTP response to the client
  * 
  * @author Lukas Ehnle, PSE Gruppe 14
  *
  */
 public class Website extends Controller {
 	
 	/** Message returned on internal server error. */
 	private static final String ISE = "Something went wrong :(";
 	/**
 	 * the form needed for admin login.
 	 */
 	private static Form<AdminLogin> form = form(AdminLogin.class);
 	/**
 	 * the form needed for logfilePathUpload.
 	 */
 	private static Form<LogfileUpload> log = form(LogfileUpload.class);
 
 	/**
 	 * render the index page with all available chart types.
 	 * @return returns the html index page
 	 */
     public static Result index() {
     	return ok(web.views.html.index.render());
     }
     
     /**
      * render the about page.
      * @return returns the about oage
      */
     public static Result about() {
     	return ok(web.views.html.about.render());
     }
    
     /**
      * method to dynamically return a chart page depending on the chartName.
      * @param chartName the name of the requested chart
      * @return returns a valid HTTP response, a web page
      */
     public static Result chartType(String chartName) {
     	return ok(web.views.html.abstractChart.render(chartName));
     }
   
     /**
      * method to dynamically return a chart JavaScript depending on the chartName.
      * @param chartName the name of the requested chart
      * @return returns a valid HTTP response, a JavaScript
      */
     public static Result chartJS(String chartName) {
     	return ok(web.views.html.chartJS.render(chartName)).as("application/javascript");
     }
     
     /**
      * requests static resources for a chart.
      * @param chartName the chart name
      * @param file the path to the file requested
      * @return returns the file
      */
     public static Result chartStatics(String chartName, String file) {
     	return ok(new File("./charts/" + chartName + "/" + file));
     }
     
     /**
      * method to process chart requests.
      * @return returns the needed chart data
      */
     //TolerantText because ContentType is JSON
     @BodyParser.Of(BodyParser.TolerantText.class)
     public static Result requestChart() {
     	try {
 			JSONObject json = new JSONObject(request().body().asText());
 			json = Facade.getFacadeInstance().computeChart(json);
 			if (json != null) {
 				return ok(json.toString());
 			}
     	} catch (JSONException e) {
     		Printer.pproblem("JSON request from web page");
     	}
     	return internalServerError(ISE);
     }
     
     /**
      * method to mirror SVG back to download or convert SVG to PNG.
      * @return returns the chart as SVG/PNG or an serverError
      */
     @BodyParser.Of(value = play.mvc.BodyParser.Raw.class, maxLength = 1024 * 1024)
     public static Result downloadChart() {
     	String[] file = FileHelper.getStringContent(request().body().asRaw().asFile()).split("&");
     	String name = file[0].substring(5);
     	if(name.equals("")) {
     		name = "chart";
     	}
     	String format = file[1].substring(7);
     	String svg="";
 			try {
 				svg += URLDecoder.decode(file[2].substring(4), "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				Printer.perror("Could not decode svg.");
 				return internalServerError(ISE);
 
 			}
     	String chart = file[3].substring(6);
 		if (format.equals("svg")) {
 			response().setHeader("Content-Disposition", "attachment; filename=\"" + name + ".svg\"");
 			return ok(svg).as("image/svg+xml");
 		} else if (format.equals("png")) {
 			PNGTranscoder t = new PNGTranscoder();
 			//if a CSS file for the chart exists
 			if (ChartIndex.getInstance().hasCss(chart)) {
 				try {
 					t.addTranscodingHint(PNGTranscoder.KEY_USER_STYLESHEET_URI,
 						new File("./charts/" + chart + "/" + chart + ".css").getCanonicalFile().toURI().toString());
 				} catch (IOException e) {
 					Printer.perror("Could not get css file when creating png from svg.");
 					return internalServerError(ISE);
 				}
 			}
 			//white background instead of transparent
 			t.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.white);
 			TranscoderInput in = new TranscoderInput(new StringReader(svg));
 			ByteArrayOutputStream png = new ByteArrayOutputStream();
 			TranscoderOutput out = new TranscoderOutput(png);
 			try {
 				t.transcode(in, out);
 			} catch (TranscoderException e) {
 				Printer.perror("Could not transcode from svg to png.");
 				return internalServerError(ISE);
 			}
 			try {
 				png.flush();
 				png.close();
 			} catch (IOException e) {
 				Printer.perror("Could not write to output stream.");
 				return internalServerError(ISE);
 			}
 			response().setHeader("Content-Disposition", "attachment; filename=\"" + name + ".png\"");
 			return ok(png.toByteArray()).as("image/png");
 		}
     	return internalServerError(ISE);
     }
    
     /**
      * Returns a chart history for a uuid provided in session information
      * and a history number.
      * 
      * @param num the number for the history
      * @return returns a JSON response or an internal server error
      */
     public static Result requestHistory(String num) {
     	String uuid = session("uuid");
     	if (uuid != null) {
     		JSONObject json = ChartHistory.requestHistory(uuid, Integer.parseInt(num));
     		if (json != null) {
     			return ok(json.toString());
     		}
     	}
 		return noContent();
     }
     
     /**
      * method to return a web page containing an overview of the last chart requests.
      * 
      * @return returns a HTML page with the overview
      */
     public static Result historyOfCharts() {
     	String uuid = session("uuid");
     	if (uuid == null) {
     	    uuid = java.util.UUID.randomUUID().toString();
     	    session("uuid", uuid);
     	}
     	return ok(web.views.html.chartHistory.render(uuid));
     }
     
     /**
      * Changes the language and redirect to current path with new language.
      * @return returns the same page in chosen language
      */
     public static Result changeLanguage() {
     	RequestBody body = request().body();
     	if (body.asFormUrlEncoded() != null && body.asFormUrlEncoded().containsKey("lang")) {
 	    	session("lang", request().body().asFormUrlEncoded().get("lang")[0]);
     	}
     	return redirect(body.asFormUrlEncoded().get("path")[0]);
     }
     
     /**
      * display login form.
      * @return returns a http response with the form page
      */
     public static Result adminLogin() {
     	return ok(web.views.html.login.render(form));
     }
     
     /**
      * display help page for charts.
      * 
      * @return returns a HTTP response with the help page
      */
     public static Result chartHelpPage() {
     	return ok(web.views.html.chartHelpPage.render());
     }
     
     /**
      * validates the login form.
      * @return returns the admin page or a badrequest
      */
     public static Result login() {
 		Form<AdminLogin> filledForm = form.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			return badRequest(web.views.html.login.render(filledForm));
 		}
 		session().put("username", filledForm.get().username);
 		return redirect(web.controllers.routes.Website.admin());
 	}
     
     /**
      * method to display admin page if authorized.
      * 
      * @return returns admin page or error
      */
     @Security.Authenticated(AdminAuth.class)
 	public static Result admin() {
 		return ok(web.views.html.adminPage.render(log));
 	}
     
     /**
      * method to pass new log file path to the parser.
      * see what.LogfileUpload
      * @return returns to the admin page
      */
     @Security.Authenticated(AdminAuth.class)
     public static Result logfile() {
     	Form<LogfileUpload> filledForm = log.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			return badRequest(web.views.html.adminPage.render(filledForm));
 		} 
     	return ok(web.views.html.adminPage.render(log));
     }
     
     
     /**
      * method to log out from admin.
      * @return returns to index
      */
     public static Result logout() {
 		session().clear();
 		return redirect(web.controllers.routes.Website.index());
 	}
     /**
      * redirects to the index page.
      * @param notNeeded required by routes, but not needed 
      * @return returns to index
      */
     public static Result standardRedirect(String notNeeded) {
     	return redirect(web.controllers.routes.Website.index());
     }
 }
