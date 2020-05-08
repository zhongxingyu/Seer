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
            Phrase myPhrase = Phrase.findById(id);
            render(myPhrase);
         }
         render();
     }
 
      
     public static void save(Long id, String phrase, String author) {
        Phrase myPhrase;
         if(id == null) {
             // Create post
         	User postBy = User.find("byLogin", Security.connected()).first();
        	myPhrase = new Phrase(author, postBy, phrase);
         } else {
             // Retrieve post
        	myPhrase = Phrase.findById(id);
             // Edit
        	myPhrase.phrase = phrase;
        	myPhrase.author = author;
         }
 
         // Validate
        validation.valid(myPhrase);
         if(validation.hasErrors()) {
            render("@form", myPhrase);
         }
         // Save
        myPhrase.save();
         index();
     }
 
     
 
 	
 }
