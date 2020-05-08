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
 
 import javax.xml.bind.JAXB;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 
 import java.net.URLDecoder;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 
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
 
                                 JAXBContext context = JAXBContext.newInstance(Event.class);
                                 Unmarshaller unmarshaller = context.createUnmarshaller();
 
                                 Event event = (Event) unmarshaller.unmarshal(response.getBodyAsStream());
                                 User creator = event.getCreator();
                                 Company company = event.getPayload().getCompany();
                                 Order order = event.getPayload().getOrder();
                                 String edition = order.getEditionCode();
 
 
                                 if (User.find().byId(creator.uuid) != null) {
                                     String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                             "<result>\n" +
                                             "    <success>false</success>\n" +
                                             "    <errorCode>USER_ALREADY_EXISTS</errorCode>\n" +
                                             "    <message>The user " + creator.firstName + " " + creator.lastName + " is already registered</message>\n" +
                                             "</result>";
 
                                    return badRequest(errorResponse).as("XML");
                                 }
 
                                 if (Company.find().byId(company.uuid) == null) {
                                     company.save();
                                 }
 
                                 String accountId = Subscription.create(creator, company, edition);
 
                                 String responseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                         "<result>\n" +
                                         "    <success>true</success>\n" +
                                         "    <message>Account successfully created</message>\n" +
                                         "    <accountIdentifier>" + accountId + "</accountIdentifier>\n" +
                                         "</result>";
                                return ok(responseStr).as("XML");
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
 
                                 JAXBContext context = JAXBContext.newInstance(Event.class);
                                 Unmarshaller unmarshaller = context.createUnmarshaller();
 
                                 Event event = (Event) unmarshaller.unmarshal(response.getBodyAsStream());
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
 
                                    return badRequest(errorResponse).as("XML");
                                 }
 
                                 if (User.find().byId(creator.uuid) == null) {
                                     String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                             "<result>\n" +
                                             "    <success>false</success>\n" +
                                             "    <errorCode>USER_NOT_FOUND</errorCode>\n" +
                                             "    <message>User " + creator.firstName + " " + creator.lastName + " could not be found.</message>\n" +
                                             "</result>";
 
                                    return badRequest(errorResponse).as("XML");
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
                                return ok(successResponse).as("XML");
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
 
                                 JAXBContext context = JAXBContext.newInstance(Event.class);
                                 Unmarshaller unmarshaller = context.createUnmarshaller();
 
                                 Event event = (Event) unmarshaller.unmarshal(response.getBodyAsStream());
                                 Account account = event.getPayload().getAccount();
 
                                 Subscription subscription = Subscription.find().byId(account.getAccountIdentifier());
 
                                 if (subscription == null) {
                                     String errorResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                             "<result>\n" +
                                             "    <success>false</success>\n" +
                                             "    <errorCode>ACCOUNT_NOT_FOUND</errorCode>\n" +
                                             "    <message>The account " + account.getAccountIdentifier() + " could not be found.</message>\n" +
                                             "</result>";
 
                                    return badRequest(errorResponse).as("XML");
                                 }
 
 
 
                                 Subscription.changeStatus(subscription.id, Subscription.Status.CANCELLED.toString());
 
                                 String successResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                         "<result>\n" +
                                         "    <success>true</success>\n" +
                                         "    <message>Account successfully cancelled</message>\n" +
                                         "    <accountIdentifier>" + subscription.id + "</accountIdentifier>\n" +
                                         "</result>";
                                return ok(successResponse).as("XML");
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
 
                                 JAXBContext context = JAXBContext.newInstance(Event.class);
                                 Unmarshaller unmarshaller = context.createUnmarshaller();
 
                                 Event event = (Event) unmarshaller.unmarshal(response.getBodyAsStream());
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
 
                                    return badRequest(errorResponse).as("XML");
                                 }
 
 
 
                                 Subscription.changeStatus(subscription.id, notice.getType());
 
                                 String successResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                         "<result>\n" +
                                         "    <success>true</success>\n" +
                                         "    <message>Account successfully cancelled</message>\n" +
                                         "    <accountIdentifier>" + subscription.id + "</accountIdentifier>\n" +
                                         "</result>";
                                return ok(successResponse).as("XML");
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
 }
