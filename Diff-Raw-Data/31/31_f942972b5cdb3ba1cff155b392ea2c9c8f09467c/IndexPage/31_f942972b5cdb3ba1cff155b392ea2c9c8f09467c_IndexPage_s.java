 /**
  * Copyright 2012 Leonid Bogdanov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.lbogdanov.poker.web.page;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.subject.Subject;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.*;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.util.io.IClusterable;
 import org.lbogdanov.poker.core.Session;
 import org.odlabs.wiquery.core.javascript.JsQuery;
 
 
 /**
  * Represents an index page.
  * 
  * @author Leonid Bogdanov
  */
 public class IndexPage extends AbstractPage {
 
     /**
      * Simple POJO for internal login form.
      */
     private static final class Credentials implements IClusterable {
 
         private String username;
         private String password;
         private boolean rememberme;
 
     }
 
     /**
      * Simple POJO for session create and join forms.
      */
     private static final class Game implements IClusterable {
 
         private String code;
         private String name;
         private String description;
 
     }
 
     /**
      * Creates a new instance of <code>Index</code> page.
      */
     public IndexPage() {
         WebMarkupContainer session = new WebMarkupContainer("session");
         WebMarkupContainer login = new WebMarkupContainer("login") {
 
             @Override
            public boolean isVisible() {
                return SecurityUtils.getSubject().getPrincipal() == null;
             }
 
         };
 
         Form<?> internal = new StatelessForm<Credentials>("internal", new CompoundPropertyModel<Credentials>(new Credentials()));
         internal.add(new RequiredTextField<String>("username"), new PasswordTextField("password"),
                     new CheckBox("rememberme"), new AjaxButton("submit") {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 Credentials credentials = (Credentials) form.getModelObject();
                 Subject subject = SecurityUtils.getSubject();
                 // force a new session to prevent session fixation attack
                 subject.getSession().stop();
                 getSession().replaceSession();
                 AuthenticationToken token = new UsernamePasswordToken(credentials.username,
                                                                       credentials.password,
                                                                       credentials.rememberme);
                 try {
                     subject.login(token);
                     String js = new JsQuery().$("#crsl").chain("carousel", "{interval: false}")
                                                         .chain("carousel", "'next'").render().toString();
                    target.appendJavaScript(js);
                    target.add(IndexPage.this.get(NAVBAR_ID));
                 } catch (AuthenticationException ae) {
                     // TODO Handle errors
                 }
             }
 
         });
 
         IModel<Game> gameModel = new CompoundPropertyModel<Game>(new Game());
         Form<?> join = new Form<Game>("join", gameModel);
        join.add(new RequiredTextField<String>("code"), new AjaxButton("submit") {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 
             }
 
         });
         Form<?> create = new Form<Game>("create", gameModel) {
 
             @Override
             protected void onSubmit() {
                 setResponsePage(SessionPage.class);
             }
 
         };
         TextField<String> code = new TextField<String>("code", new AbstractReadOnlyModel<String>() {
 
             @Override
             public String getObject() {
                 return Session.newCode(7); // TODO Read from settings
             }
 
         });
         create.add(new RequiredTextField<String>("name"), new TextArea<String>("description"), code.setEnabled(false));
 
         login.add(internal);
         session.add(join, create);
         session.add(AttributeModifier.append("class", new AbstractReadOnlyModel<String>() {
 
             @Override
             public String getObject() {
                 return SecurityUtils.getSubject().getPrincipal() != null ? "active" : null;
             }
 
         }));
         add(login, session);
     }
 
 }
