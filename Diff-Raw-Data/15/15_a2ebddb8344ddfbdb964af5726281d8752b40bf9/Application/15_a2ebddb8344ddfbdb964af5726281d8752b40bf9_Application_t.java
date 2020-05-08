 package controllers;
 
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import play.*;
 import play.mvc.*;
 import play.data.Form;
 import play.db.ebean.Model;
 
 import views.html.*;
 import models.*;
 
 public class Application extends Controller {
   
     public static Result index() {
         List<Person> persons = Person.find.all();
         return ok(index.render(persons));
     }
 
     public static Result addPersonPost() {
        Form<Person> personForm = Form.form(Person.class);
        personForm = personForm.bindFromRequest();
        if (personForm.hasErrors()) {
            return badRequest(addPerson.render(personForm));
        }
        Person person = personForm.get();
         person.save();
         return redirect(routes.Application.index());
     }
 
     public static Result addPersonGet() {
         Form personForm = Form.form(Person.class);
         return ok(addPerson.render(personForm));
     }
 
     public static Result detailPerson(Long personId) {
         Person person = Person.find.byId(personId);
         List<Buy> buys = Buy.find.where("person=" + personId).findList();
         return ok(detailPerson.render(person, buys));
     }
 
     public static Result addBuyGet(Long personId) {
         Person person = Person.find.byId(personId);
         Form buyForm = Form.form(Buy.class);
         return ok(addBuy.render(buyForm, person));
     }
 
     public static Result addBuyPost(Long personId) {
         Person person = Person.find.byId(personId);
        Form<Buy> buyForm = Form.form(Buy.class);
        buyForm = buyForm.bindFromRequest();
        if (buyForm.hasErrors()) {
            return badRequest(addBuy.render(buyForm, person));
        }
        Buy buy = buyForm.get();
         buy.person = person;
         buy.save();
         return redirect(routes.Application.detailPerson(person.id));
     }
 
     public static Result detailBuy(Long personId, Long buyId) {
         Person person = Person.find.byId(personId);
         Buy buy = Buy.find.byId(buyId);
         return ok(detailBuy.render(person, buy));
     }
 }
