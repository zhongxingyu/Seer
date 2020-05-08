 package controllers;
 
 import play.*;
 import play.cache.Cache;
 import play.data.validation.Email;
 import play.data.validation.Max;
 import play.data.validation.Min;
 import play.data.validation.Phone;
 import play.data.validation.Required;
 import play.data.validation.Valid;
 import play.i18n.Lang;
 import play.mvc.*;
 import play.mvc.results.*;
 
 import java.util.*;
 
 import notifiers.Mails;
 
 import models.*;
 
 public class Application extends Controller {
     
     public static void changeLanguage(String language) {
         if(language.matches("de"))
             Lang.change("de");
         else
             Lang.change("en");
         index();
     }
     
     public static void check(String email, String password) {
         validation.required(email);
         validation.required(password);
         
         if(validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             Application.login();
         }
         SoupUser user = SoupUser.connect(email, password);
         if(user != null) {
             session.put("user", user.FirstName);
             session.put("useremail", user.email);
             String fullname = user.toString();
             renderTemplate("@Application.index",fullname);
         }
         play.data.validation.Error error = validation.required(user).error;
         if(error != null) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             Application.login();
         }     
     }
     
     public static void shipping(@Email @Required String email, @Required String fname,@Required String lname, 
     		@Required @Min(5) @Max(5) String zipcode,@Required String street, @Required String city, @Required @Phone String telephone) {
 
         if(validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             //renderText("hello");
             Application.checkout();
             
         }
     }
     public static void saveUser(@Valid SoupUser users, String verifyPassword ,String email) {
         validation.required(verifyPassword);
         validation.equals(verifyPassword, users.Password).message("Your password doesn't match");
         SoupUser objUser = SoupUser.UserExist(users.email);
        
         if(objUser != null) {
             if(validation.equals(email, objUser.email) != null)
             {
                 validation.addError(email, "User already exists");
                 validation.keep();
                 render("@Application.register", users, verifyPassword);
             }
         }
         if(validation.hasErrors()) {
             //validation.keep();
             render("@Application.register", users, verifyPassword);
         }
         
 
         users.create();
         session.put("user", users.toString());
         session.put("useremail", users.email.toString());
         String fullname = users.toString();
         Mails.welcome(users);
         //flash.success("Welcome, " + user.fullname);
         renderTemplate("@Application.index",fullname);
     }
     
     public static void confirmemail(){
         render();
     }
     
     public static void done(){
     	
     	if(Cache.safeDelete("ShoppingCart")) {
     		session.remove("selectSlider");
           	String completesg = "Thank You we have received your Order. You receive your soup within half hour";
           	renderTemplate("@Application.index",completesg);
     	}else{
     		Cache.delete("ShoppingCart");
     		session.remove("selectSlider");
     		String completesg = "oops there was an error in finishing session";
           	renderTemplate("@Application.index",completesg);
     	}
     }
     
     public static void verifyemail(){
         render();
     }
     
     public static void createBaseSoup(String id) {
         
         BaseSoup bs = BaseSoup.find(Integer.parseInt(id));
         Soups baseSoup = new Soups(bs.SoupID,bs.Name,bs.value,bs.Image);
         Cache.set("soup_"+id, baseSoup);
         session.put("basesoup", id);
         session.put("chooseSlider", 2);
         soups(null);
     }
     
     public static void addBaseIngredient(String id) {
         //renderText("the id=" + id);
         SoupIngredients ing = SoupIngredients.find(Integer.parseInt(id));
         Ingredient baseIng = new Ingredient(ing.IngID,ing.Name,ing.value, ing.Image);
         if(session.get("basesoup") != null) {
             String strid = session.get("basesoup");
             Soups bs = Cache.get("soup_"+strid,Soups.class);
             bs.addIngredient(baseIng);
             Cache.set("soup_"+strid, bs);
             session.put("chooseSlider", 2);
             soups(null);
         }else {
             String errorMSG = "Please first select base soup";
             reload(errorMSG);
         }
         renderTemplate("@Application.soups");
     }
     
     public static void addPacketSize(String id) {
         
         if(session.get("basesoup") != null) {
             session.put("chooseSlider",0);
             String strid = session.get("basesoup");
             
             Soups bs = Cache.get("soup_"+strid,Soups.class);
            if(id.matches("1"))
                 bs.setSoupSize("Large");
            else if (id.matches("2"))
                 bs.setSoupSize("Medium");
            else if(id.matches("3"))
                 bs.setSoupSize("Small");
            
             session.put("chooseSlider", 4);
            
             Cache.set("soup_"+strid, bs);
             soups(null);
         }else {
             String errorMSG = "Please first select base soup";
             reload(errorMSG);
         }
     }
     
     public static void removePacketSize(String id) {
         
         if(session.get("basesoup") != null) {
             String strid = session.get("basesoup");
             Soups bs = Cache.get("soup_"+strid,Soups.class);
             bs.setSoupSize(null);
             Cache.set("soup_"+strid, bs);
             soups(null);
         }else {
             String errorMSG = "Please first select base soup";
             reload(errorMSG);
         }
     }
     
     
     public static void shopRemoveSoup(String id) {
         ShoppingCart objShop = Cache.get("ShoppingCart", ShoppingCart.class);
         
         if(objShop.removeProduct(id)) {
             if(objShop.getItemCount() == 0)
                 Cache.set("ShoppingCart", null);
             else
                 Cache.set("ShoppingCart", objShop);
             shoppingcart();
         }else {
             renderText("Error");
         }
     }
     
     public static void shopRemoveIng(String id, String ingid) {
         ShoppingCart objShop = Cache.get("ShoppingCart", ShoppingCart.class);
         
         if(objShop.RemoveIngredient(id, ingid)) {
             if(objShop.getItemCount() == 0)
                 Cache.set("ShoppingCart", null);
             else
                 Cache.set("ShoppingCart", objShop);
             shoppingcart();
         }else {
             renderText("Error");
         }   
     }
     public static void password(String email) {
         validation.email(email);
         if(validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             Application.lostPassword();
         }
 //        SimpleEmail objemail = new SimpleEmail();
 //        objemail.setFrom("dednasir@yahoo.com ");
 //        objemail.addTo("recipient@zenexity.fr");
 //        objemail.setSubject("subject");
 //        objemail.setMsg("Message");
 //        Mail.send(email); 
         render();
     }
     
     public static void logout() {
         session.clear();
         index();
     }
     
     public static void index() {
         render();
     }
 
     public static void privacy() {
         render();
     }
     
     public static void faq() {
         render();
     }
     
     public static void support() {
         render();
     }
 
     
     public static void checkout() {
     	if(session.get("user") != null) {
             String stremail = session.get("useremail");
             SoupUser user = SoupUser.UserExist(stremail);
             if(user != null){
             	renderArgs.put("userinfo", user );
             	render();
             }
         }
     	render();
         
     }
 
     public static void shoppingcart() {
     	session.remove("chooseSlider");
         ShoppingCart objShoppingCart;
         if(session.get("basesoup") != null) {
             String strid = session.get("basesoup");
             Soups bs = Cache.get("soup_"+strid,Soups.class);
             if(bs.getSoupSize()== null)
             {
                 String errorMSG = "Please Select A Soup Size.....";
                 reload(errorMSG);
             }
             
             
             if(Cache.get("ShoppingCart", ShoppingCart.class) == null ) {
                 objShoppingCart= new ShoppingCart(0);
                 objShoppingCart.addProduct(bs);
                 Cache.set("ShoppingCart", objShoppingCart, "30mn");
             }else {
                 objShoppingCart = Cache.get("ShoppingCart", ShoppingCart.class);
                 objShoppingCart.addProduct(bs);
                 Cache.set("ShoppingCart", objShoppingCart, "30mn");
             }
             session.remove("basesoup");
             
             if(Cache.safeDelete("soup_" + strid))
                 render(objShoppingCart);
             render(objShoppingCart);
         }else if(Cache.get("ShoppingCart", ShoppingCart.class) != null){ //in case users wants to check the shopping cart
             objShoppingCart = Cache.get("ShoppingCart", ShoppingCart.class);
             render(objShoppingCart);
             /*String errorMSG = "Please first Make A Soup.....";
             reload(errorMSG);*/
         }else {
             String errorMSG = "Shopping cart is empty. Please make a soup first ........";
             reload(errorMSG);
         }
     }
     
     public static void about() {
         render();
     }
     
     public static void soups(String errorMSG) {
         List<BaseSoup> bs = BaseSoup.findAll();
         List<SoupIngredients> si = SoupIngredients.findAll();
         String SoupID = session.get("basesoup");
         Soups cachedSoup = Cache.get("soup_"+SoupID,Soups.class);
         if(errorMSG != null)
         	render(bs,si,cachedSoup, errorMSG );
         else
         	render(bs,si,cachedSoup);
     }
     public static void reload(String errorMSG) {
         List<BaseSoup> bs = BaseSoup.findAll();
         List<SoupIngredients> si = SoupIngredients.findAll();
         renderTemplate("@Application.soups",bs,si,errorMSG);
     }
     
     public static void removeSoup(String id) {
         if(Cache.safeDelete("soup_" + id)) {
             session.remove("basesoup");
             soups(null);
         }
         else
             soups(null);
     }
     
     public static void removeIngredient(String id) {
         String SoupID = session.get("basesoup");
         Soups cachedSoup = Cache.get("soup_"+SoupID,Soups.class);
         if(cachedSoup.RemoveIngredient(id)) {
             String errorMSG = "Ingredient Removed Successfully";
             soups(errorMSG);
         }
     }
 
     public static void login() {
         render();
     }
     
     public static void blog() {
     	List<Blog> bl = Blog.findAll();                   
         render(bl);
     }
     
     public static void contact() {
         render();
     }
     
     public static void blogpost() {
         render();
     }
     
     public static void myaccount() {
         render();
     }
     
     public static void soupsgrid() {
         render();
     }
     
     public static void gallery() {
         render();
     }
     
     public static void orderhistory() {
         render();
     }
     
     public static void lostPassword() {
         render();
     }
     
     public static void sendEmail(String email) {
         validation.email(email);
         //renderText("what id this");
         if(validation.hasErrors()) {
             params.flash(); // add http parameters to the flash scope
             validation.keep(); // keep the errors for the next request
             Application.lostPassword();
         }
         SoupUser user = SoupUser.UserExist(email);
         if(user != null) {
             Mails.lostPassword(user);
         }
     }
     
     public static void register(){
         render();
     }
     
     public static void setQuantity(){
     	
         renderText("hello");
     }
 }
