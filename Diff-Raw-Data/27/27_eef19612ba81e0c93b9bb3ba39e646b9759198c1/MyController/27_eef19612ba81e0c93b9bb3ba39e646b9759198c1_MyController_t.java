 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package app.controller;
 
import app.services.ServiceImpl;
 import cx.ath.mancel01.webframework.view.Render;
 import cx.ath.mancel01.webframework.view.RenderView;
 import cx.ath.mancel01.webframework.annotation.Controller;
 import cx.ath.mancel01.webframework.http.Request;
 import java.util.ArrayList;
 import java.util.List;
 import javax.inject.Inject;
 
 /**
  *
  * @author mathieuancelin
  */
 @Controller
 public class MyController {
 
     @Inject
    private ServiceImpl service;
 
     @Inject
     private Request request;
 
     public RenderView index() {
         List<String> numbers = new ArrayList<String>();
         numbers.add("one");
         numbers.add("two");
         numbers.add("three");
         return new RenderView()
                 .param("message", service.hello("One eyed Jack"))
                 .param("numbers", numbers);
     }
 
     public RenderView other() {
         List<String> numbers = new ArrayList<String>();
         numbers.add("four");
         numbers.add("five");
         numbers.add("six");
         return new RenderView("index.html")
                 .param("message", service.hello("Anonymous"))
                 .param("numbers", numbers);
     }
 
     public void foo() {
         List<String> numbers = new ArrayList<String>();
         numbers.add("seven");
         numbers.add("height");
         numbers.add("nine");
         Render.page("index.html")
             .with("message", service.hello("foo"))
             .with("numbers", numbers)
             .go();
     }
 }
