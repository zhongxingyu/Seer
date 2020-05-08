 /**
  * This file is part of RibbonWeb application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 import views.html.defaultpages.error;
 
 import play.data.*;
 import static play.mvc.Controller.session;
 
 /**
  * Login controller class.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class LoginController extends Controller {
     
     /**
      * Remote login error display string.
      */
     private static String errorLogin = null;
 
     public static Result index() {
         if (!MiniGate.isGateReady) {
            return ok(ribbon_error.render(MiniGate.gateErrorStr));
         } else {
             if (session("connected") != null) {
                return redirect(routes.SimpleReleaseContoller.index());
             } else {
                 return ok(login.render("Вхід до системи..."));
             }
         }
     }
     
     public static Result login() {
     	models.Session newSession = Form.form(models.Session.class).bindFromRequest().get();
         String loginErr = MiniGate.gate.sendCommandWithCheck("RIBBON_NCTL_REM_LOGIN:{" + newSession.username + "}," + MiniGate.getHash(newSession.password));
         if (loginErr == null) {
             session("connected", newSession.username);
             return redirect(routes.SimpleReleaseContoller.index());
         } else {
             flash("err_login", loginErr);
             return redirect(routes.LoginController.index());
         }
     }
     
     public static Result logout() {
         session().remove("connected");
         return redirect(routes.LoginController.index());
     }
   
 }
