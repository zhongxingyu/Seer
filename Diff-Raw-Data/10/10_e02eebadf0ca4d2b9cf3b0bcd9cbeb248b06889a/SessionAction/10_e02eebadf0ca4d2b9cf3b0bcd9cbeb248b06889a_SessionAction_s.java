 /*
  * The MIT License
  *
  *   Copyright (c) 2013, benas (md.benhassine@gmail.com)
  *
  *   Permission is hereby granted, free of charge, to any person obtaining a copy
  *   of this software and associated documentation files (the "Software"), to deal
  *   in the Software without restriction, including without limitation the rights
  *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *   copies of the Software, and to permit persons to whom the Software is
  *   furnished to do so, subject to the following conditions:
  *
  *   The above copyright notice and this permission notice shall be included in
  *   all copies or substantial portions of the Software.
  *
  *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *   THE SOFTWARE.
  */
 
 package net.benas.todolist.web.struts.action.user;
 
 import com.opensymphony.xwork2.Action;
 import net.benas.todolist.core.domain.User;
 import net.benas.todolist.web.common.form.LoginForm;
 import net.benas.todolist.web.common.util.TodolistUtils;
 import net.benas.todolist.web.struts.action.BaseAction;
 
 /**
  * Action class that handles login/logout process.
  *
  * @author benas (md.benhassine@gmail.com)
  */
 public class SessionAction extends BaseAction {
 
     private LoginForm loginForm;
 
     public String login() {
         return Action.SUCCESS;
     }
 
     public String doLogin() {
         if (userService.login(loginForm.getEmail(), loginForm.getPassword())) {
             User user = userService.getUserByEmail(loginForm.getEmail());
             session.put(TodolistUtils.SESSION_USER, user);
             return Action.SUCCESS;
         } else {
            //Todo set errors
             return Action.INPUT;
         }
     }
 
     public String doLogout() {
         if (session instanceof org.apache.struts2.dispatcher.SessionMap) {
             try {
                 ((org.apache.struts2.dispatcher.SessionMap) session).invalidate();
             } catch (IllegalStateException e) {
                 logger.error("Unable to invalidate session.", e);
             }
         }
         return Action.SUCCESS;
     }
 
     /*
      * Getters and setters for model attributes
      */
 
     public LoginForm getLoginForm() {
         return loginForm;
     }
 
     public void setLoginForm(LoginForm loginForm) {
         this.loginForm = loginForm;
     }
 
     public String getLoginTabStyle() {
         return "active";
     }
 
 }
