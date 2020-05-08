 /**
  * Copyright (C) 2008 Ovea <dev@testatoo.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.testatoo.cartridge.multisession;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.testatoo.cartridge.html4.By;
 import org.testatoo.cartridge.html4.element.*;
 import org.testatoo.config.annotation.TestatooModules;
 import org.testatoo.config.junit.TestatooJunitRunner;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.testatoo.cartridge.html4.Language.*;
 import static org.testatoo.core.ComponentFactory.component;
 import static org.testatoo.core.ComponentFactory.page;
 import static org.testatoo.core.EvaluatorHolder.withEvaluator;
 
 @RunWith(TestatooJunitRunner.class)
 @TestatooModules(MultiSessionModule.class)
 public class MultiSessionTest {
 
     @Before
     public void setUp() {
         page().open("Form.html");
     }
 
     @Test
     public void can_reset_a_form() {
         type("Joe", on(component(InputText.class, "firstname")));
         type("Blow", into(component(InputText.class, "lastname")));
         enter("email@noname.com", into(into(component(InputText.class, "email"))));
 
         check(component(Radio.class, "male"));
         check(component(CheckBox.class, By.jQuery("$('[name=yes]')")));
 
         on(component(Select.class, "cities")).select("Casablanca");
 
         component(Form.class, "myForm").reset();
 
         assertThat(component(InputText.class, "firstname").value(), is(""));
         assertThat(component(InputText.class, "lastname").value(), is(""));
         assertThat(component(InputText.class, "email").value(), is(""));
 
         assertThat(component(Radio.class, "male").isChecked(), is(false));
         assertThat(component(CheckBox.class, By.jQuery("$('[name=yes]')")).isChecked(), is(false));
 
         assertThat(component(Select.class, "cities").selectedOptions().get(0).content(), is("New York"));
 
 
         // Execute some other stuff and wait until finished
         withEvaluator("MY-OTHER-SESSION-NAME").run(new Runnable() {
             @Override
             public void run() {
                 page().open("/");
                 type("testatoo", on(component(InputText.class, By.jQuery("$('[name=q]')"))));
                 clickOn(component(Button.class, By.jQuery("$('[name=btnG]')")));
             }
         });
 
         type("Joe", on(component(InputText.class, "firstname")));
         type("Blow", into(component(InputText.class, "lastname")));
         enter("email@noname.com", into(component(InputText.class, "email")));
 
         check(component(Radio.class, "male"));
         check(component(CheckBox.class, By.jQuery("$('[name=yes]')")));
 
         on(component(Select.class, "cities")).select("Casablanca");
 
         on(component(Form.class, "myForm")).reset();
 
         assertThat(component(InputText.class, "firstname").value(), is(""));
         assertThat(component(InputText.class, "lastname").value(), is(""));
         assertThat(component(InputText.class, "email").value(), is(""));
 
         assertThat(component(Radio.class, "male").isChecked(), is(false));
         assertThat(component(CheckBox.class, By.jQuery("$('[name=yes]')")).isChecked(), is(false));
 
         assertThat(component(Select.class, "cities").selectedOptions().get(0).content(), is("New York"));
     }
 
 }
