 package controllers;
 
import play.mvc.With;
 
 /**
  * Controller which takes care of users.
  * @author OpenARMS Service team
  */
 @With(Secure.class)
 public class Users extends CRUD {
 
 }
