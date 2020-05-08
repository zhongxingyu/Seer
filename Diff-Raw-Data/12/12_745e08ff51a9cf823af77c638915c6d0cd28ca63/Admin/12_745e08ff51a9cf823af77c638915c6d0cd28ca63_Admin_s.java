 package controllers;
 
 import com.avaje.ebean.Ebean;
 import exceptions.QueueException;
 import models.User;
 import models.norpneu.*;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.MissingNode;
 import org.codehaus.jackson.node.ObjectNode;
 import play.Logger;
 import play.i18n.Messages;
 import play.libs.Json;
 import play.mvc.BodyParser;
 import play.mvc.Result;
 import play.mvc.Security;
 import util.EnhancedController;
 import util.NotificationManager;
 import util.PlayUtils;
 import views.html.admin;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Map;
 
 @Security.Authenticated(AdminLock.class)
 public class Admin extends EnhancedController {
 
     public static Result index() {
         return ok(admin.render(getCachedUser()));
     }
 
     public static Result approveAccount(String email, String pricing) {
         User u = Ebean.find(User.class).where()
                 .eq("email", email)
                 .eq("active", false).findUnique();
         boolean ok = false;
         if (u != null) {
             u.active = true;
             u.activeDate = new Timestamp(System.currentTimeMillis());
             if (pricing != null) {
                 u.company.priceStrategy = PRICE_STRATEGY.valueOf(pricing);
                 u.company.update();
             }
             u.update();
             flash("success", "Account " + email + " has been approved");
             ok = true;
             try {
                 NotificationManager.queueNotification(email,
                         Messages.get("registration.approved.email.subject"),
                         Messages.get("registration.approved.email.body",
                                 PlayUtils.getApplicationConfig("host"), u.email, u.password, u.name));
             } catch (QueueException e) {
                 Logger.error("Mail notification failed", e);
             }
         } else {
             flash("error", "Account " + email + " not found or is already active");
         }
 
         ObjectNode result = Json.newObject();
         result.put("status", ok);
         if (!ok) {
             result.put("message", "Account " + email + " not found or is already active");
         }
         return ok(result);
     }
 
     public static Result declineAccount(String email) {
         User u = Ebean.find(User.class).where()
                 .eq("email", email)
                 .eq("active", false).findUnique();
         boolean ok = false;
         ObjectNode result = Json.newObject();
         try {
             u.delete();
             ok = true;
         } catch (Exception e) {
             ok = false;
             result.put("message", "Erro no sistema no momento de apagar o registo. P.f. tente novamente e caso se mantenha, procure o seu administrador do sistema");
             Logger.error(e.getMessage());
         }
         result.put("status", ok);
         return ok(result);
     }
 
     public static Result revokeAccount() {
         JsonNode json = request().body().asJson();
         if (json == null) {
             return badRequest("Not a json request");
         }
 
         // check arguments
         JsonNode listOfEmailsJsonNode = json.findPath("listOfEmails");
         String email = json.findPath("email").getTextValue();
         ArrayList<String> emails;
         if (listOfEmailsJsonNode instanceof MissingNode && email == null) {
             return badRequest("Missing email or listOfEmails");
         } else if (!(listOfEmailsJsonNode instanceof MissingNode)) {
             Iterator<JsonNode> listOfEmails = listOfEmailsJsonNode.getElements();
             emails = new ArrayList<String>(5);
             while (listOfEmails.hasNext()) {
                 emails.add(listOfEmails.next().getTextValue());
             }
         } else {
             emails = new ArrayList<String>(1);
             emails.add(email);
         }
 
         boolean ok;
         ObjectNode result = Json.newObject();
         int deleted = 0, failed = 0;
         String message = "";
         try {
             for (String contact : emails) {
                 try {
                     if (User.revokeAccount(contact)) {
                         deleted++;
                     } else {
                         failed++;
                     }
                 } catch (Exception e) {
                     Logger.error(e.getMessage(), e);
                     failed++;
                 }
             }
             if ( emails.size() == deleted) {
                 ok = true;
             } else {
                 ok = false;
                 if ( emails.size() == 1) {
                     // TODO: should not assume this was the case
                     message = "Erro a apagar o registo - já existem encomendas para este utilizador";
                 } else if ( emails.size() == failed ) {
                     message = "Não foi possível apagar nenhum dos registos - já existem encomendas para os utilizadores em causa";
                 } else {
                     message = "Parte dos registos não foram eliminados por já existirem encomendas registadas para esses utilizadores";
                 }
             }
         } catch (Exception e) {
             ok = false;
             result.put("message", "Erro no sistema no momento de apagar o registo. " +
                     "P.f. tente novamente e caso se mantenha, procure o seu administrador do sistema");
             Logger.error(e.getMessage());
         }
         result.put("status", ok);
         result.put("message", message);
         return ok(result);
     }
 
     public static Result users() {
         if (!isJsonRequest())
             return badRequest("Expecting Json request");
         ObjectNode result = Json.newObject();
        result.put("data", Json.toJson(User.find.where().eq("powerUser", false).eq("active", true).order("company").findList()));
         return ok(result);
     }
 
     public static Result userRequests() {
         if (!isJsonRequest())
             return badRequest("Expecting Json request");
         ObjectNode result = Json.newObject();
         result.put("data", Json.toJson(User.find.where().ne("active", true).order("company").findList()));
         return ok(result);
     }
 
     public static Result resetPassword(String email) {
         User u = Ebean.find(User.class).where()
                 .eq("email", email)
                 .eq("active", true).findUnique();
         boolean ok;
         ObjectNode result = Json.newObject();
         try {
             ok = User.tagPasswordForRecovery(email);
             u.refresh();
             // Add notification to queue
             NotificationManager.queueNotification(u.email,
                     Messages.get("admin.members.password_reset.email.subject"),
                     Messages.get("admin.members.password_reset.email.body",
                             PlayUtils.getApplicationConfig("host"), u.email, u.resetToken, u.name));
         } catch (QueueException e) {
             ok = true;
             Logger.error("Error sending email for:" + u.email, e);
         }
         result.put("status", ok);
         return ok(result);
     }
 
     public static Result updateUser() {
         JsonNode json = request().body().asJson();
         if (json == null) {
             return badRequest("Not a json request");
         }
 
         boolean ok = false;
         ObjectNode result = Json.newObject();
 
         ObjectMapper mapper = new ObjectMapper();
         try {
             User user = null;
             Map<?, ?> map = mapper.readValue(json, Map.class);
             String email;
             if (map.containsKey("email")) {
                 email = (String) map.get("email");
                 user = Ebean.find(User.class).where()
                         .eq("email", email).findUnique();
 
             }
 
             if (user == null) {
                 result.put("status", false);
                 result.put("message", "Não foi possível localizar o utilizador pretendido!");
                 return ok(result);
             }
             if (map.containsKey("name")) {
                 user.name = (String) map.get("name");
             }
 
 
             if (map.containsKey("company")) {
                 user.company.name = (String) map.get("company");
             }
 
             if (map.containsKey("phoneNumber")) {
                 user.phoneNumber = (String) map.get("phoneNumber");
             }
 
             if (map.containsKey("nif")) {
                 user.company.nif = (String) map.get("nif");
             }
 
             if (map.containsKey("street")) {
                 user.company.street = (String) map.get("street");
             }
 
             if (map.containsKey("postalCode")) {
                 user.company.postalCode = (String) map.get("postalCode");
             }
 
             if (map.containsKey("city")) {
                 user.company.city = (String) map.get("city");
             }
 
             if (map.containsKey("priceStrategy")) {
                 user.company.priceStrategy = PRICE_STRATEGY.valueOf((String) map.get("priceStrategy"));
             }
             user.company.update();
             user.update();
             ok = true;
 
         } catch (IOException e) {
             Logger.error(e.getMessage());
             ok = false;
             result.put("message", "Erro no sistema no momento de actualizar o registo. P.f. tente novamente.");
         } catch (Exception e) {
             Logger.error(e.getMessage());
             ok = false;
             result.put("message", "Erro no sistema no momento de actualizar o registo. P.f. tente novamente.");
         }
         result.put("status", ok);
         return ok(result);
     }
 
     @BodyParser.Of(BodyParser.Json.class)
     public static Result createTire() {
         JsonNode json = request().body().asJson();
 
         boolean ok = false;
         ObjectNode result = Json.newObject();
         ObjectMapper mapper = new ObjectMapper();
         try {
             Map<?, ?> map = mapper.readValue(json, Map.class);
             models.norpneu.Tire tire = new models.norpneu.Tire();
             tire.brand = Brand.getOrCreate((String) map.get("brand"));
             tire.measure = (String) map.get("measure");
             tire.radialType = (String) map.get("radialType");
             tire.costPrice = readLong(map.get("costPrice"));
             tire.sellingPrice1 = readLong(map.get("sellingPrice1"));
             tire.sellingPrice2 = readLong(map.get("sellingPrice2"));
             tire.sellingPrice3 = readLong(map.get("sellingPrice3"));
             tire.stockUnitsAvailable = readLong(map.get("stockUnitsAvailable"));
             tire.maximumVisibleStock = readLong(map.get("maximumVisibleStock"));
             tire.safetyStock = readLong(map.get("safetyStock"));
             tire.pavementIndex = (String) map.get("pavementIndex");
             tire.speedIndex = (String) map.get("speedIndex");
             tire.ecoValue = readLong(map.get("ecoValue"));
 
             tire.save();
             tire.refresh();
 
             StockLog log = new StockLog();
             log.tire = tire;
             log.timestamp = System.currentTimeMillis();
             log.quantityDiff = tire.stockUnitsAvailable;
             log.currentUnits =  tire.stockUnitsAvailable;
             log.user = getCachedUser();
             log.save();
 
             ok = true;
         } catch (IOException e) {
             Logger.error(e.getMessage());
             ok = false;
             result.put("message", "Erro no sistema no momento de actualizar o registo. P.f. tente novamente.");
         } catch (Exception e) {
             Logger.error(e.getMessage());
             ok = false;
             result.put("message", "Erro no sistema no momento de actualizar o registo. P.f. tente novamente.");
         }
         result.put("status", ok);
         return ok(result);
     }
 
     @BodyParser.Of(BodyParser.Json.class)
     public static Result updateTire() {
         JsonNode json = request().body().asJson();
         boolean ok = false;
         ObjectNode result = Json.newObject();
         ObjectMapper mapper = new ObjectMapper();
 
         try {
             models.norpneu.Tire tire = null;
             Map<?, ?> map = mapper.readValue(json, Map.class);
             Long id;
             if (map.containsKey("id")) {
                 id = readLong(map.get("id"));
                 tire = models.norpneu.Tire.finder.byId(id);
             }
 
             if (tire == null) {
                 result.put("status", false);
                 result.put("message", "Não foi possível localizar o pneu pretendido!");
                 return ok(result);
             }
             if (map.containsKey("brand")) {
                 tire.brand.name = (String) map.get("brand");
             }
             if (map.containsKey("measure")) {
                 tire.measure = (String) map.get("measure");
             }
             if (map.containsKey("pavementIndex")) {
                 tire.pavementIndex = (String) map.get("pavementIndex");
             }
             if (map.containsKey("speedIndex")) {
                 tire.speedIndex = (String) map.get("speedIndex");
             }
             if (map.containsKey("radialType")) {
                 tire.radialType = (String) map.get("radialType");
             }
             if (map.containsKey("costPrice")) {
                 tire.costPrice = readLong(map.get("costPrice"));
             }
             if (map.containsKey("sellingPrice1")) {
                 tire.sellingPrice1 = readLong(map.get("sellingPrice1"));
             }
             if (map.containsKey("sellingPrice2")) {
                 tire.sellingPrice2 = readLong(map.get("sellingPrice2"));
             }
             if (map.containsKey("sellingPrice3")) {
                 tire.sellingPrice3 = readLong(map.get("sellingPrice3"));
             }
             if (map.containsKey("ecoValue")) {
                 tire.ecoValue = readLong(map.get("ecoValue"));
             }
             Long difStockUnitsAvailable = tire.stockUnitsAvailable;
             if (map.containsKey("stockUnitsAvailable")) {
                 tire.stockUnitsAvailable = readLong(map.get("stockUnitsAvailable"));
                 difStockUnitsAvailable = tire.stockUnitsAvailable - difStockUnitsAvailable;
             }
             if (map.containsKey("maximumVisibleStock")) {
                 tire.maximumVisibleStock = readLong(map.get("maximumVisibleStock"));
             }
             if (map.containsKey("safetyStock")) {
                 tire.safetyStock = readLong(map.get("safetyStock"));
             }
 
             tire.brand.update();
             tire.update();
 
 
             StockLog log = new StockLog();
             log.tire = tire;
             log.timestamp = System.currentTimeMillis();
             log.quantityDiff = difStockUnitsAvailable;
             log.currentUnits =  tire.stockUnitsAvailable;
             log.user = getCachedUser();
             log.save();
 
             ok = true;
 
         } catch (IOException e) {
             Logger.error(e.getMessage());
             ok = false;
             result.put("message", "Erro no sistema no momento de actualizar o registo. P.f. tente novamente.");
         } catch (Exception e) {
             Logger.error(e.getMessage());
             ok = false;
             result.put("message", "Erro no sistema no momento de actualizar o registo. P.f. tente novamente.");
         }
 
         result.put("status", ok);
         return ok(result);
     }
 
     public static Result shipments() {
         ObjectNode result = Json.newObject();
         result.put("data", Json.toJson(Shipment.finder.all()));
         return ok(result);
     }
 
     public static Result orders() {
         ObjectNode result = Json.newObject();
         result.put("data", Json.toJson(OrderTireLine.finder.all()));
         return ok(result);
     }
 
     public static Result ordersActiveCount() {
         ObjectNode result = Json.newObject();
         result.put("data", Json.toJson(OrderTire.finder.where().in("status",new ArrayList<ORDERSTATUS>(Arrays.asList(new ORDERSTATUS[]{ORDERSTATUS.SUBMITTED}))).findList().size()));
         return ok(result);
     }
 
     public static Result membersActiveCount() {
         ObjectNode result = Json.newObject();
         result.put("data", Json.toJson(User.find.where().eq("active", Boolean.FALSE).findList().size()));
         return ok(result);
     }
 
     public static Result orderstatus(String orderid, String status) {
         ObjectNode result = Json.newObject();
         synchronized (getCachedUser()) {
             if (ORDERSTATUS.valueOf(status) != null) {
                 OrderTire order = OrderTire.finder.byId(Long.valueOf(orderid));
                 if(order!=null){
                     order.status =   ORDERSTATUS.valueOf(status);
                     order.timestamp = System.nanoTime();
                     order.update();
                     if(ORDERSTATUS.valueOf(status).equals(ORDERSTATUS.PROCESSED)){
                         try {
                             NotificationManager.queueNotification(order.user.email,
                                     "Norpneu - Ordem de encomenda " + order.getFriendlyId() + " processada",
                                     "A ordem de encomenda #" + order.getFriendlyId() + " foi processada. Irá receber uma notificação assim que esta estiver finalizada.Obrigado!");
                         } catch (QueueException e) {
                             Logger.error("Mail notification failed", e);
                         }
                     }  else if(ORDERSTATUS.valueOf(status).equals(ORDERSTATUS.FINISHED)){
                         try {
                             NotificationManager.queueNotification(order.user.email,
                                     "Norpneu - Ordem de encomenda " + order.getFriendlyId() + " finalizada",
                                     "A ordem de encomenda #" + order.getFriendlyId() + " está finalizada. Esperamos ter correspondido às suas expectativas. Obrigado pela preferência!");
                         } catch (QueueException e) {
                             Logger.error("Mail notification failed", e);
                         }
                     }
                     result.put("status", true);
                     result.put("order", Json.toJson(order));
                 }   else{
                     result.put("status", false);
                     result.put("message", "Ordem " + orderid + " inexistente");
                     return ok(result);
                 }
             } else {
                 result.put("status", false);
                 result.put("message", "Status " + status + " inválido");
                 return ok(result);
             }
         }
 
         return ok(result);
     }
 
     /*
        When user adds a Tire Order
        1 - If user already has one order in INITIATED,INPROGRESS WILL RETURN IT
        2 - If user doesnt have a order in INITIATED,INPROGRESS will create a new one
     */
     public static Result order() {
         JsonNode json = request().body().asJson();
         if (!isJsonRequest())
             return badRequest("Expecting Json request");
 
         User userObject = getCachedUser();
 
         synchronized (userObject) {
             if (json != null && json.findValue("orderid") != null) {
                 Long orderId = json.findValue("orderid").getLongValue();
                 if (orderId != null) {
                     OrderTire tireOrder = OrderTire.finderUser.where().eq("id", orderId)
                             .findUnique();
                     ObjectNode result = Json.newObject();
                     result.put("status", true);
                     result.put("order", Json.toJson(tireOrder));
                     return ok(result);
                 }
             }
         }
 
         ObjectNode result = Json.newObject();
         result.put("status", false);
         result.put("message", "Ordem de encomenda inexistente");
         return ok(result);
 
     }
 
 
     public static Result addTireToCategory(){
         JsonNode json = request().body().asJson();
         if (!isJsonRequest())
             return badRequest("Expecting Json request");
         Long longTire = json.findValue("tireid").getLongValue();
         Long longCAteg = json.findValue("categoryid").getLongValue();
         ObjectNode result = Json.newObject();
         TireCategory category = TireCategory.finder.byId(longCAteg);
         if(category==null){
             result.put("status", false);
             result.put("message", "Categoria de pneu inexistente");
             return ok(result);
         }
 
         models.norpneu.Tire t = models.norpneu.Tire.finder.byId(longTire);
         if(t==null){
             result.put("status", false);
             result.put("message", "Pneu inexistente");
             return ok(result);
         }
 
 
         if(!category.tyres.contains(t)){
             Ebean.createSqlUpdate("INSERT into categories_tires(ci_category_id,ci_tire_id) values (:cat_id,:tire_id)")
                     .setParameter("cat_id", category.id)
                     .setParameter("tire_id", t.id)
                     .execute();
 
             category.refresh();
             result.put("category",  Json.toJson(category));
         }

         return ok(result);
     }
 
     public static Result removeTireFromCategory(){
         JsonNode json = request().body().asJson();
         if (!isJsonRequest())
             return badRequest("Expecting Json request");
         Long longTire = json.findValue("tireid").getLongValue();
         Long longCAteg = json.findValue("categoryid").getLongValue();
 
         ObjectNode result = Json.newObject();
         TireCategory category = TireCategory.finder.byId(longCAteg);
         if(category==null){
             result.put("status", false);
             result.put("message", "Categoria de pneu inexistente");
         }
 
         models.norpneu.Tire t = models.norpneu.Tire.finder.byId(longTire);
         if(t==null){
             result.put("status", false);
             result.put("message", "Pneu inexistente");
         }
 
         if(category.tyres.contains(t)){
             Ebean.createSqlUpdate("DELETE FROM  categories_tires where ci_category_id = :cat_id AND ci_tire_id=:tire_id")
                     .setParameter("cat_id", category.id)
                     .setParameter("tire_id", t.id)
                     .execute();
             result.put("category",  Json.toJson(category));
         }

         return ok(result);
     }
 
     private static Long readLong(Object value) {
         if (value instanceof Integer) {
             return ((Integer) value).longValue();
         } else if (value instanceof Long) {
             return (Long) value;
         } else if (value instanceof String) {
             try {
                 return Long.parseLong((String) value);
             } catch (NumberFormatException e) {
                 return null;
             }
         } else {
             return null;
         }
     }
 
 
 }
