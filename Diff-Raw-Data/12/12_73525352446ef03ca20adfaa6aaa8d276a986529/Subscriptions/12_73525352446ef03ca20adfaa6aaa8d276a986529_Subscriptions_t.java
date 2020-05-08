 package controllers;
 
 import models.Company;
 import models.Subscription;
 import models.SubscriptionItem;
 import models.User;
 import models.xml.*;
 import play.libs.F.Function;
 import play.libs.Json;
 import play.libs.WS;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 
 import java.io.StringReader;
 import java.net.URLDecoder;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  *
  * @author Saad Ahmed
  */
 public class Subscriptions extends Controller {
 
 
     @BodyParser.Of(BodyParser.Xml.class)
     public static Result create() {
         Map<String, String[]> queryParams = request().queryString();
 
         if (!queryParams.containsKey("url")) {
             return badRequest();
         }
 
         String encodedUrl = queryParams.get("url")[0];
         String url = "";
         String authorization = "";
 
         try {
             url = URLDecoder.decode(encodedUrl, "UTF-8");
             authorization = Security.signAuthorization(url);
         }
 
         catch (Exception e) {
             return unauthorized();
         }
 
         return async(
                 WS.url(url).setHeader("Authorization", authorization).get().map(
                         new Function<WS.Response, Result>() {
                             @Override
                             public Result apply(WS.Response response) throws Throwable {
 
                                 String s = response.getBody();
                                 StringReader reader = new StringReader(s);
 
                                 JAXBContext context = JAXBContext.newInstance(Event.class);
                                 Unmarshaller unmarshaller = context.createUnmarshaller();
 
                                 Event event = (Event) unmarshaller.unmarshal(reader);
                                 User creator = event.getCreator();
                                 Company company = event.getPayload().getCompany();
                                 Order order = event.getPayload().getOrder();
                                 String edition = order.getEditionCode();
 
                                 if (Company.find().byId(company.uuid) == null) {
 
                                     if (User.find().byId(creator.uuid) != null) {
                                         String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                 "<result>\n" +
                                                 "    <success>false</success>\n" +
                                                 "    <errorCode>USER_ALREADY_EXISTS</errorCode>\n" +
                                                 "    <message>The user is already registered</message>\n" +
                                                 "</result>";
 
                                         return ok(errorResponse).as("text/xml");
                                     }
                                 }
 
                                 else {
                                     Subscription subscription = Subscription.find().where().eq("company_uuid", company.uuid).findUnique();
 
                                     if (subscription.status.equals(Subscription.Status.ACTIVE.toString()) || subscription.status.equals(Subscription.Status.FREE_TRIAL.toString())) {
                                         String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                 "<result>\n" +
                                                 "    <success>false</success>\n" +
                                                 "    <errorCode>USER_ALREADY_EXISTS</errorCode>\n" +
                                                 "    <message>The company is already registered</message>\n" +
                                                 "</result>";
 
                                         return ok(errorResponse).as("text/xml");
                                     }
 
                                    subscription.delete();
                                 }
 
                                String accountId = Subscription.create(creator, company, edition);

                                 String responseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                         "<result>\n" +
                                         "    <success>true</success>\n" +
                                         "    <message>Account successfully created</message>\n" +
                                         "    <accountIdentifier>" + accountId + "</accountIdentifier>\n" +
                                         "</result>";
                                 return ok(responseStr).as("text/xml");
                             }
                         }
                 )
         );
 
     }
 
 
     @BodyParser.Of(BodyParser.Xml.class)
     public static Result change() {
         Map<String, String[]> queryParams = request().queryString();
 
         if (!queryParams.containsKey("url")) {
             return badRequest();
         }
 
         String encodedUrl = queryParams.get("url")[0];
         String url = "";
         String authorization = "";
 
         try {
             url = URLDecoder.decode(encodedUrl, "UTF-8");
             authorization = Security.signAuthorization(url);
         }
 
         catch (Exception e) {
             return unauthorized();
         }
 
         return async(
                 WS.url(url).setHeader("Authorization", authorization).get().map(
                         new Function<WS.Response, Result>() {
                             @Override
                             public Result apply(WS.Response response) throws Throwable {
 
                                 String s = response.getBody();
                                 StringReader reader = new StringReader(s);
 
                                 JAXBContext context = JAXBContext.newInstance(Event.class);
                                 Unmarshaller unmarshaller = context.createUnmarshaller();
 
                                 Event event = (Event) unmarshaller.unmarshal(reader);
                                 User creator = event.getCreator();
                                 Payload payload = event.getPayload();
                                 Account account = payload.getAccount();
 
                                 Subscription subscription = Subscription.find().byId(account.getAccountIdentifier());
 
                                 if (subscription == null) {
                                     String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                             "<result>\n" +
                                             "    <success>false</success>\n" +
                                             "    <errorCode>ACCOUNT_NOT_FOUND</errorCode>\n" +
                                             "    <message>The account " + account.getAccountIdentifier() + " could not be found.</message>\n" +
                                             "</result>";
 
                                     return ok(errorResponse).as("text/xml");
                                 }
 
 
                                 Order order = payload.getOrder();
                                 String edition = order.getEditionCode();
 
                                 if (!edition.equals(subscription.edition)) {
                                     Subscription.changeEdition(subscription.id, edition);
                                 }
 
                                 String successResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                         "<result>\n" +
                                         "    <success>true</success>\n" +
                                         "    <message>Account successfully changed</message>\n" +
                                         "    <accountIdentifier>" + subscription.id + "</accountIdentifier>\n" +
                                         "</result>";
                                 return ok(successResponse).as("text/xml");
                             }
                         }
                 )
         );
     }
 
 
     @BodyParser.Of(BodyParser.Xml.class)
     public static Result cancel() {
         Map<String, String[]> queryParams = request().queryString();
 
         if (!queryParams.containsKey("url")) {
             return badRequest();
         }
 
         String encodedUrl = queryParams.get("url")[0];
         String url = "";
         String authorization = "";
 
         try {
             url = URLDecoder.decode(encodedUrl, "UTF-8");
             authorization = Security.signAuthorization(url);
         }
 
         catch (Exception e) {
             return unauthorized();
         }
 
         return async(
                 WS.url(url).setHeader("Authorization", authorization).get().map(
                         new Function<WS.Response, Result>() {
                             @Override
                             public Result apply(WS.Response response) throws Throwable {
 
                                 String s = response.getBody();
                                 StringReader reader = new StringReader(s);
 
                                 JAXBContext context = JAXBContext.newInstance(Event.class);
                                 Unmarshaller unmarshaller = context.createUnmarshaller();
 
                                 Event event = (Event) unmarshaller.unmarshal(reader);
                                 Account account = event.getPayload().getAccount();
 
                                 Subscription subscription = Subscription.find().byId(account.getAccountIdentifier());
 
                                 if (subscription == null) {
                                     String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                             "<result>\n" +
                                             "    <success>false</success>\n" +
                                             "    <errorCode>ACCOUNT_NOT_FOUND</errorCode>\n" +
                                             "    <message>The account " + account.getAccountIdentifier() + " could not be found.</message>\n" +
                                             "</result>";
 
                                     return ok(errorResponse).as("text/xml");
                                 }
 
 
 
                                 Subscription.changeStatus(subscription.id, Subscription.Status.CANCELLED.toString());
 
                                 String successResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                         "<result>\n" +
                                         "    <success>true</success>\n" +
                                         "    <message>Account successfully cancelled</message>\n" +
                                         "    <accountIdentifier>" + subscription.id + "</accountIdentifier>\n" +
                                         "</result>";
                                 return ok(successResponse).as("text/xml");
                             }
                         }
                 )
         );
     }
 
 
     @BodyParser.Of(BodyParser.Xml.class)
     public static Result notice() {
         Map<String, String[]> queryParams = request().queryString();
 
         if (!queryParams.containsKey("url")) {
             return badRequest();
         }
 
         String encodedUrl = queryParams.get("url")[0];
         String url = "";
         String authorization = "";
 
         try {
             url = URLDecoder.decode(encodedUrl, "UTF-8");
             authorization = Security.signAuthorization(url);
         }
 
         catch (Exception e) {
             return unauthorized();
         }
 
         return async(
                 WS.url(url).setHeader("Authorization", authorization).get().map(
                         new Function<WS.Response, Result>() {
                             @Override
                             public Result apply(WS.Response response) throws Throwable {
 
                                 String s = response.getBody();
                                 StringReader reader = new StringReader(s);
 
                                 JAXBContext context = JAXBContext.newInstance(Event.class);
                                 Unmarshaller unmarshaller = context.createUnmarshaller();
 
                                 Event event = (Event) unmarshaller.unmarshal(reader);
                                 Account account = event.getPayload().getAccount();
                                 Notice notice = event.getPayload().getNotice();
 
                                 Subscription subscription = Subscription.find().byId(account.getAccountIdentifier());
 
                                 if (subscription == null) {
                                     String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                             "<result>\n" +
                                             "    <success>false</success>\n" +
                                             "    <errorCode>ACCOUNT_NOT_FOUND</errorCode>\n" +
                                             "    <message>The account " + account.getAccountIdentifier() + " could not be found.</message>\n" +
                                             "</result>";
 
                                     return ok(errorResponse).as("text/xml");
                                 }
 
 
 
                                 Subscription.changeStatus(subscription.id, notice.getType());
 
                                 String successResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                         "<result>\n" +
                                         "    <success>true</success>\n" +
                                         "    <message>Account successfully cancelled</message>\n" +
                                         "    <accountIdentifier>" + subscription.id + "</accountIdentifier>\n" +
                                         "</result>";
                                 return ok(successResponse).as("text/xml");
                             }
                         }
                 )
         );
     }
 
 
     @BodyParser.Of(BodyParser.Json.class)
     public static Result all() {
         List<Subscription> subscriptions = Subscription.find().all();
         return ok(Json.toJson(subscriptions));
     }
 
 
     public static Result deleteAll() {
         List<Subscription> subscriptions = Subscription.find().all();
         for (Subscription s: subscriptions) {
             s.delete();
         }
 
         return ok("All subscriptions deleted");
     }
 }
