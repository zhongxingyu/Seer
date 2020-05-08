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
 import play.db.ebean.Model;
 import static play.mvc.Controller.session;
 
 import java.util.List;
 
 /**
  *
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class SimpleReleaseContoller extends Controller {
     
     public static Result index() {
         if (session("connected") != null) {
             List<models.MessageProbe> probs = new Model.Finder(String.class, models.MessageProbe.class).where().eq("author", session("connected")).orderBy("id desc").findList();
             return ok(post_list.render(probs));
         } else {
             flash("err_login", "Ви повинні зареєструватись!");
             return redirect(routes.LoginController.index());
         }
     }
     
     public static Result postForm() {
         if (session("connected") != null) {
             return ok(simple_release.render(models.PseudoDirectorySet.get(session("connected")), null));
         } else {
             flash("err_login", "Ви повинні зареєструватись!");
             return redirect(routes.LoginController.index());
         }
     }
     
     public static Result post() {
         models.MessageProbe newPost = Form.form(models.MessageProbe.class).bindFromRequest().get();
         newPost.author = session("connected");
         newPost.save();
         if(MiniGate.sender != null) {
             MiniGate.sender.interrupt();
         }
         return redirect(routes.SimpleReleaseContoller.index());
     }
     
     public static Result viewPost(String id) {
         models.MessageProbe probe = (models.MessageProbe) new Model.Finder(String.class, models.MessageProbe.class).byId(id);
         return ok(post_view.render(probe));
     }
     
     public static Result editForm(String id) {
         if (session("connected") != null) {
             models.MessageProbe editProbe = (models.MessageProbe) new Model.Finder(String.class, models.MessageProbe.class).byId(id);
             if (editProbe.curr_status == models.MessageProbe.STATUS.ACCEPTED) {
                 return redirect(routes.SimpleReleaseContoller.index());
             } else {
                 return ok(simple_release.render(models.PseudoDirectorySet.get(session("connected")), editProbe));
             }
         } else {
             flash("err_login", "Ви повинні зареєструватись!");
             return redirect(routes.LoginController.index());
         }
     }
     
     public static Result edit() {
         models.MessageProbe editPost = Form.form(models.MessageProbe.class).bindFromRequest().get();
         models.MessageProbe oldPost = (models.MessageProbe) new Model.Finder(String.class, models.MessageProbe.class).byId(editPost.id);
         editPost.author = oldPost.author;
        if (editPost.curr_status == models.MessageProbe.STATUS.POSTED) {
             editPost.curr_status = models.MessageProbe.STATUS.EDITED;
         }
         editPost.ribbon_index = oldPost.ribbon_index;
         editPost.update();
         if(MiniGate.sender != null) {
             MiniGate.sender.interrupt();
         }
         return redirect(routes.SimpleReleaseContoller.index());
     }
     
     public static Result deletePost(String id) {
         models.MessageProbe probe = (models.MessageProbe) new Model.Finder(String.class, models.MessageProbe.class).byId(id);
         probe.curr_status = models.MessageProbe.STATUS.DELETED;
         probe.update();
         if(MiniGate.sender != null) {
             MiniGate.sender.interrupt();
         }
         return redirect(routes.SimpleReleaseContoller.index());
     }
     
 }
