 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */

 package controllers;
 
 import models.*;
 
public class Security extends Secure.Security {

static boolean authenticate(String username, String password) {
    return User.connect(username, password) != null;
}
 
 }
