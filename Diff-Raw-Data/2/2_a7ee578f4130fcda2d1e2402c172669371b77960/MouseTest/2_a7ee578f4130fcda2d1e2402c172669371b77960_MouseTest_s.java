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
 
 package org.testatoo.cartridge.input;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.testatoo.cartridge.WebTest;
import org.testatoo.cartridge.html4.element.A;
import org.testatoo.cartridge.html4.element.Link;
 import org.testatoo.core.component.Image;
 import org.testatoo.core.component.Panel;
 import org.testatoo.core.component.TextField;
 import org.testatoo.core.input.Mouse;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.testatoo.core.ComponentFactory.component;
 import static org.testatoo.core.ComponentFactory.page;
 
 public class MouseTest extends WebTest {
 
     @BeforeClass
     public static void setUp() {
         page().open("Mouse.html");
     }
 
     @Test
     public void can_test_focus_on_component() {
         TextField component = component(TextField.class, "textFocus");
         assertThat(component.hasFocus(), is(false));
         Mouse.clickOn(component);
         assertThat(component.hasFocus(), is(true));
     }
 
     @Test
     public void can_click_on_component() {
         TextField textField = component(TextField.class, "element_1");
         assertThat(textField.value(), is("Element 1"));
 
         Mouse.clickOn(textField);
 
         assertThat(textField.value(), is("Element 1 clicked"));
     }
 
     @Test
     public void can_doubleClick_on_component() {
         TextField textField = component(TextField.class, "element_2");
         assertThat(textField.value(), is("Element 2"));
 
         Mouse.doubleClickOn(textField);
 
         assertThat(textField.value(), is("Element 2 double clicked"));
     }
 
     @Test
     public void can_rightclick_on_component() {
         TextField textField = component(TextField.class, "element_5");
         assertThat(textField.value(), is("Element 5"));
 
         Mouse.rightClickOn(textField);
 
         assertThat(textField.value(), is("Element 5 right click"));
     }
 
     @Test
     public void can_mouseOver_component() {
         TextField textField = component(TextField.class, "element_3");
         assertThat(textField.value(), is("Element 3"));
 
         Mouse.mouseOverOn(textField);
 
         assertThat(textField.value(), is("Element 3 mouse over"));
     }
 
     @Test
     public void can_mouseOut_component() {
         TextField textField = component(TextField.class, "element_4");
         assertThat(textField.value(), is("Element 4"));
 
         Mouse.mouseOutOf(textField);
 
         assertThat(textField.value(), is("Element 4 mouse out"));
     }
 
     @Test
     public void test_component_drag_and_drop() throws Exception {
         assertThat(component(Image.class, "image").isVisible(), is(false));
         Panel draggablePanel = component(Panel.class, "draggable");
         Panel droppablePanel = component(Panel.class, "droppable");
 
         Mouse.drag(draggablePanel).on(droppablePanel);
 
         assertThat(component(Image.class, "image").isVisible(), is(true));
     }
 
 }
