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
 package org.lbogdanov.poker.web.markup;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.util.string.Strings;
 
 import fiftyfive.wicket.shiro.markup.LogoutLink;
 
 
 /**
  * A Navigation bar implementation.
  * 
  * @author Leonid Bogdanov
  */
 public class NavBar extends Panel {
 
     /**
      * @see Panel#Panel(String)
      */
     public NavBar(String id) {
         super(id);
         WebMarkupContainer userMenu = new WebMarkupContainer("userMenu") {
 
             @Override
            public boolean isVisible() {
                return SecurityUtils.getSubject().getPrincipal() != null;
             }
 
         };
         Label username = new Label("username", new AbstractReadOnlyModel<String>() {
 
             @Override
             public String getObject() {
                 return Strings.toString(SecurityUtils.getSubject().getPrincipal());
             }
 
         });
         userMenu.add(username.setRenderBodyOnly(true), new LogoutLink("logout"));
         add(userMenu);
     }
 
 }
