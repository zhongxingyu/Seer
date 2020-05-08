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
 import org.testatoo.config.annotation.ConcurrentEvaluation;
 import org.testatoo.config.annotation.TestatooModules;
 import org.testatoo.config.junit.TestatooJunitRunner;
 import org.testatoo.core.ComponentException;
 import org.testatoo.core.input.Mouse;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.fail;
 import static org.testatoo.cartridge.html4.Language.*;
 import static org.testatoo.core.ComponentFactory.component;
 import static org.testatoo.core.ComponentFactory.page;
 
 @RunWith(TestatooJunitRunner.class)
 @TestatooModules(ParallelMultiSessionModule.class)
 @ConcurrentEvaluation
 public class ParallelMultiSessionTest {
 
     @Before
     public void setUp() {
         page().open("Form.html");
     }
 
     @Test
     public void can_find_form_by_id() {
         component(Form.class, "myForm");
 
         try {
             component(Form.class, "otherForm");
             fail();
         } catch (ComponentException e) {
             assertThat(e.getMessage(), is("Cannot find component defined by id=otherForm"));
         }
     }
 
     @Test
     public void exception_thrown_if_component_not_a_html_form() {
         try {
             component(Form.class, "email");
             fail();
         } catch (ComponentException e) {
             assertThat(e.getMessage(), is("The component with id=email is not a Form but a InputText"));
         }
     }
 
     @Test
     public void test_i18nAttributes() {
         Form myForm = component(Form.class, "myForm");
 
         assertThat(myForm.direction(), is(Direction.lefttoright));
         assertThat(myForm.language(), is("fr"));
     }
 
     @Test
     public void test_coreAttributes() {
         Form myForm = component(Form.class, "myForm");
 
         assertThat(myForm.id(), is("myForm"));
         assertThat(myForm.classname(), is("myClass"));
         assertThat(myForm.style(), containsString("color:black"));
         assertThat(myForm.title(), is("formTitle"));
     }
 
     @Test
     public void test_specifics_attributes() {
         Form myForm = component(Form.class, "myForm");
 
         assertThat(myForm.method(), is(Method.post));
         assertThat(myForm.action(), is("Exit.html"));
         assertThat(myForm.enctype(), is("application/x-www-form-urlencoded"));
         assertThat(myForm.accept(), is("all"));
         assertThat(myForm.name(), is("formName"));
         // Not supported by IE
 //        assertThat(myForm.acceptCharset(), is("UTF-8"));
     }
 
     @Test
     public void if_attribute_are_not_defined_then_return_default_values() {
         Form myForm3 = component(Form.class, "myForm3");
 
         assertThat(myForm3.method(), is(Method.get));
         assertThat(myForm3.enctype(), is("application/x-www-form-urlencoded"));
         assertThat(myForm3.accept(), is(""));
         assertThat(myForm3.name(), is(""));
         assertThat(myForm3.acceptCharset(), is("UNKNOWN"));
     }
 
     @Test
     public void test_contains() {
         Form myForm = component(Form.class, "myForm");
 
         Select citiesList = component(Select.class, "cities");
         assertThat(myForm.contains(citiesList), is(true));
 
         InputText inputText = component(InputText.class, "lastname");
         assertThat(myForm.contains(inputText), is(true));
 
         Radio radio = component(Radio.class, "male");
         assertThat(myForm.contains(radio), is(true));
 
         CheckBox checkBox = component(CheckBox.class, By.jQuery("$('[name=yes]')"));
         assertThat(myForm.contains(checkBox), is(true));
 
         Button button = component(Button.class, "submitImage");
         assertThat(myForm.contains(button), is(true));
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
 
     @Test
     public void can_submit_a_form() {
         assertThat(page().title(), is("Form tests"));
 
         Button submitButton = component(Button.class, "submitImage");
         clickOn(submitButton);
 
         assertThat(page().title(), is("Exit page"));
 
         page().open("Form.html");
         assertThat(page().title(), is("Form tests"));
         Mouse.clickOn(submitButton);
 
         assertThat(page().title(), is("Exit page"));
 
         page().open("Form.html");
         assertThat(page().title(), is("Form tests"));
 
         component(Form.class, "myForm").submit();
 
         assertThat(page().title(), is("Exit page"));
     }
 
     @Test
     public void can_find_form_by_name() {
         component(Form.class, By.jQuery("$('[name=formName]')"));
 
         try {
             component(Form.class, By.jQuery("$('[name=otherFormName]')"));
             fail();
         } catch (ComponentException e) {
             assertThat(e.getMessage(), is("Cannot find component defined by jQueryExpression=$('[name=otherFormName]')"));
         }
     }
 
     @Test
     public void can_find_form_by_title() {
         component(Form.class, By.jQuery("$('[title=formTitle]')"));
 
         try {
             component(Form.class, By.jQuery("$('[title=otherFormTitle]')"));
             fail();
         } catch (ComponentException e) {
             assertThat(e.getMessage(), is("Cannot find component defined by jQueryExpression=$('[title=otherFormTitle]')"));
         }
     }
 
     @Test
     public void test_toString() {
         assertThat(component(Form.class, By.id("myForm")).toString(), is("class org.testatoo.cartridge.html4.element.Form with state : enabled:true, visible:true, action:Exit.html"));
     }
 }
