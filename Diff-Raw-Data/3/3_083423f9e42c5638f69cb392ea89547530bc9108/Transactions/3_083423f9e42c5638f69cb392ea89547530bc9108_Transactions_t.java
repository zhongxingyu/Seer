 package controllers;
 
import play.mvc.With;
 import models.Role;
 
 @Check(Role.ADMIN)
@With(Secure.class)
 public class Transactions extends CRUD {
 
 }
