 package controllers;
 
 import java.util.List;
 
 import models.Phrase;
 import models.User;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Admin extends Controller {
 
     
     @Before
     static void setConnectedUser() {
         if(Security.isConnected()) {
             User user = User.find("byLogin", Security.connected()).first();
             renderArgs.put("user", user.login);
         }
     }
  
     public static void index() {
         List<Phrase> phrases = Phrase.find("postBy.login", Security.connected()).fetch();
         render(phrases);
 
     }
     
     public static void form(Long id) {
         if(id != null) {
            Phrase phrase = Phrase.findById(id);
            render(phrase);
         }
         render();
     }
 
      
     public static void save(Long id, String phrase, String author) {
        Phrase newPhrase;
         if(id == null) {
             // Create post
         	User postBy = User.find("byLogin", Security.connected()).first();
        	newPhrase = new Phrase(author, postBy, phrase);
         } else {
             // Retrieve post
        	newPhrase = Phrase.findById(id);
             // Edit
        	newPhrase.phrase = phrase;
        	newPhrase.author = author;
         }
 
         // Validate
        validation.valid(newPhrase);
         if(validation.hasErrors()) {
            render("@form", newPhrase);
         }
         // Save
        newPhrase.save();
         index();

     }
 
     
 
 	
 }
