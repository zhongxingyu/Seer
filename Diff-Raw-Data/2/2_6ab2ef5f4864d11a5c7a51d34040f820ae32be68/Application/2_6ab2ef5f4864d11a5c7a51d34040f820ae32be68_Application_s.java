 package controllers;
 
 import models.Client;
 import org.w3c.dom.Document;
 import play.Logger;
 import play.libs.XPath;
 import play.mvc.*;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static play.mvc.BodyParser.Xml;
 
 public class Application extends Controller {
 
     private static final String CREATE_AGT = "CREATE-AGT";
     private static final String GET_BALANCE = "GET-BALANCE";
 
     private static final int CLIENT_DOES_NOT_EXIST = 3;
     private static final int OK = 0;
     private static final int WRONG_PASSWORD = 4;
     private static final int CLIENT_ALREADY_EXISTS = 1;
    public static final int TECHNICAL_ERROR = 2;
 
     @BodyParser.Of(Xml.class)
     public static Result process() {
         Logger.debug("Incoming request: " + request().body().toString());
         Result response;
         Document dom = request().body().asXml();
         if (dom == null) {
             Logger.info("Bad request: expecting xml data");
             return badRequest("Expecting xml data");
         }
         String login = XPath.selectText("//request//extra[@name='login']", dom);
         if (isNullOrEmpty(login)) {
             Logger.info("Bad request: missing parameter [login]");
             return badRequest("Missing parameter [login]");
         }
         String password = XPath.selectText("//request//extra[@name='password']", dom);
         if (isNullOrEmpty(password)) {
             Logger.info("Bad request: missing parameter [password]");
             return badRequest("Missing parameter [password]");
         }
         String type = XPath.selectText("//request//request-type", dom);
         if (isNullOrEmpty(type)) {
             Logger.info("Bad request: missing parameter [request-type]");
             return badRequest("Missing parameter [request-type]");
         }
         if (type.equals(CREATE_AGT)) {
             response = createClient(login, password);
         } else if (type.equals(GET_BALANCE)) {
             response = getBalance(login, password);
         } else {
             response = processUnknown();
         }
         return response;
     }
 
 
     private static Result processUnknown() {
         return badRequest("Unknown request type");
     }
 
     private static Result getBalance(String login, String password) {
         Client client;
         try {
             client = Client.findByLogin(login);
         } catch (Exception e) {
             return ok(views.xml.getBalance.render(TECHNICAL_ERROR, null));
         }
         if (client == null) {
             return ok(views.xml.getBalance.render(CLIENT_DOES_NOT_EXIST, null));
         } else {
             String storedPassword = client.getPassword();
             if (password.equals(storedPassword)) {
                 return ok(views.xml.getBalance.render(OK, client.getBalance()));
             } else {
                 return ok(views.xml.getBalance.render(WRONG_PASSWORD, null));
             }
         }
     }
 
     private static Result createClient(String login, String password) {
         Client client;
         try {
             client = Client.findByLogin(login);
         } catch (Exception e) {
             return ok(views.xml.newClientResponse.render(TECHNICAL_ERROR));
         }
         if (client == null) {
             client = new Client(login, password);
             try {
                 client.save();
                 return ok(views.xml.newClientResponse.render(OK));
             } catch (Exception e) {
                 return ok(views.xml.newClientResponse.render(TECHNICAL_ERROR));
             }
         } else {
             return ok(views.xml.newClientResponse.render(CLIENT_ALREADY_EXISTS));
         }
     }
 }
