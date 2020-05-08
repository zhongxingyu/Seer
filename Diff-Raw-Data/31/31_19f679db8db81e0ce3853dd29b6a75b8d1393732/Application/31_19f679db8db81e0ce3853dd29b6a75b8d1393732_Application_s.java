 package controllers;
 
 import models.Hotel;
 import play.data.Form;
 import play.mvc.*;
 
 public class Application extends Controller {
 
    static Form<Hotel> hotelForm = Form.form(Hotel.class);

     public static Result index() {
         return redirect(routes.Hotels.allHotel());
     }
   
 }
