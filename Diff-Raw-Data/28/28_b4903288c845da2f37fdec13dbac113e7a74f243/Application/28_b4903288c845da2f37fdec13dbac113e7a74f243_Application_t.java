 package controllers;
 
 import play.*;
 import play.cache.Cache;
 import play.data.validation.Valid;
 import play.mvc.*;
 import play.mvc.results.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     @Before
     @Finally
     static void addUser() {
         String user = connected();
         if(user != null) {
             renderArgs.put("user", user);
         }
     }
     
     static String connected() {
 //        if(renderArgs.get("user") != null) {
 //            return renderArgs.get("user", SoupUser.class);
 //        }
 //        String username = session.get("user");
 //        if(username != null) {
 //            return SoupUser.find("byUsername", username).first();
 //        } 
 //        return null;
         String user = session.get("user");
 
 
         if(user != null) {
             return user;
         }
         else
             return null;
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
         String fullname = users.toString();
         //flash.success("Welcome, " + user.fullname);
         renderTemplate("@Application.index",fullname);
     }
     
     public static void CreateBaseSoup(String id) {
         
         BaseSoup bs = BaseSoup.find(Integer.parseInt(id));
         Soups baseSoup = new Soups(bs.SoupID,bs.Name,bs.value);
         Cache.set("soup_"+id, baseSoup);
         session.put("basesoup", id);
         soups();
     }
     
     public static void AddBaseIngredient(String id) {
         //renderText("the id=" + id);
         SoupIngredients ing = SoupIngredients.find(Integer.parseInt(id));
         Ingredient baseIng = new Ingredient(ing.IngID,ing.Name,ing.value);
         if(session.get("basesoup") != null) {
             String strid = session.get("basesoup");
             Soups bs = Cache.get("soup_"+strid,Soups.class);
             bs.addIngredient(baseIng);
             Cache.set("soup_"+strid, bs);
             soups();
         }else {
             String errorMSG = "Please first select base soup";
             reload(errorMSG);
         }
         renderTemplate("@Application.soups");
     }
     
     public static void AddPacketSize(String id) {
       
         if(session.get("basesoup") != null) {
             String strid = session.get("basesoup");
             Soups bs = Cache.get("soup_"+strid,Soups.class);
             if(id.matches("1"))
                 bs.setSoupSize("Large");
             else if (id.matches("2"))
                 bs.setSoupSize("Medium");
             else if(id.matches("3"))
                 bs.setSoupSize("small");
             
             Cache.set("soup_"+strid, bs);
             soups();
         }else {
             String errorMSG = "Please first select base soup";
             reload(errorMSG);
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
 
     public static void faq() {
         render();
     }
     

     public static void shoppingcart() {
        
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
     
     public static void soups() {
         List<BaseSoup> bs = BaseSoup.findAll();
         List<SoupIngredients> si = SoupIngredients.findAll();
         String SoupID = session.get("basesoup");
         Soups cachedSoup = Cache.get("soup_"+SoupID,Soups.class);
         render(bs,si,cachedSoup);
     }
     public static void reload(String errorMSG) {
         List<BaseSoup> bs = BaseSoup.findAll();
         List<SoupIngredients> si = SoupIngredients.findAll();
         renderTemplate("@Application.soups",bs,si,errorMSG);
     }
     
     public static void RemoveSoup(String id) {
         if(Cache.safeDelete("soup_" + id)) {
             session.remove("basesoup");
             soups();
         }
         else
             soups();
     }
     
     public static void RemoveIngredient(String id) {
         String SoupID = session.get("basesoup");
         Soups cachedSoup = Cache.get("soup_"+SoupID,Soups.class);
         if(cachedSoup.RemoveIngredient(id)) {
             String errorMSG = "Ingredient Removed Successfully";
             List<BaseSoup> bs = BaseSoup.findAll();
             List<SoupIngredients> si = SoupIngredients.findAll();
             renderTemplate("@Application.soups",bs,si,cachedSoup,errorMSG);
         }
     }
 
     public static void login() {
         render();
     }
     
     public static void blog() {
         render();
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
     
     public static void register(){
         render();
     }
 }
