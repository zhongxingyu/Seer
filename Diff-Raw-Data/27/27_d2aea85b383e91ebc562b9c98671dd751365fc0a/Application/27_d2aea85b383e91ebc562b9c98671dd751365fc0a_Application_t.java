 package controllers;
 
import models.InventoryItem;
 import play.*;
import play.data.Form;
import play.db.ebean.Model;
 import play.mvc.*;
 import views.html.*;
import static play.libs.Json.toJson;

import java.util.List;
 
 public class Application extends Controller {
   
   public static Result index() {
    return ok(index.render());
  }

  public static Result addItem() {
      Form<InventoryItem> form = form(InventoryItem.class).bindFromRequest();
      InventoryItem item = form.get();
      item.save();
      return redirect(routes.Application.index());
  }

  public static Result getItems() {
      List<InventoryItem> items = new Model.Finder(String.class, InventoryItem.class).all();
      return ok(toJson(items));
   }
 }
