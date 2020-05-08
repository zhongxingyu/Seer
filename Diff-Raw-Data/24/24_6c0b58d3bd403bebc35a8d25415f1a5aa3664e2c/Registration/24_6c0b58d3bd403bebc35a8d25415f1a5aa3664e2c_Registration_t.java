 package controllers;
 
 import models.Author;
 import play.data.validation.Match;
 import play.data.validation.Required;
 import play.modules.gae.GAE;
 
 import com.google.appengine.repackaged.com.google.common.base.Strings;
 
 public class Registration extends Application {
 
     @Secure
     public static void index() {
         render();
     }
 
     @Secure
     public static void register(
                     @Required(message = "Name is required.")
                     @Match(value = "[a-zA-Z]{1}[a-zA-Z\\d]+",
                             message = "Name must be alphanumeric and beginning with letter.")
                     String name) {
         if (!Strings.isNullOrEmpty(name) && Author.findByName(name) != null) {
             validation.addError("name", "User with this name already exists.");
         }
 
         if (validation.hasErrors()) {
             params.flash();
             validation.keep();
             index();
         }
 
        Author author = Author.findByEmail(GAE.getUser().getEmail());
        boolean isNew = author == null;

        if (isNew) {
            author = new Author();
            author.email = GAE.getUser().getEmail();
        }
        author.name = name;
        author.storeOrUpdate();

        if (!isNew) {
            CheatSheet.list();
        }
 
         CheatSheet.create();
     }
 }
