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
 
 package org.testatoo.cartridge.html4;
 
 import org.junit.Test;
 import org.testatoo.WebTest;
import org.testatoo.core.EvaluatorHolder;
 import org.testatoo.core.Selection;
 import org.testatoo.core.component.*;
 import org.testatoo.core.component.datagrid.DataGrid;
 
 import static org.hamcrest.Matchers.is;
 import static org.testatoo.core.ComponentFactory.*;
import static org.testatoo.core.Language.assertThat;
import static org.testatoo.core.matcher.Matchers.exist;
 
 public class FinderTest extends WebTest {
 
     @Test
    public void can_find_element_by_id_with_points() {
        page().open("ElementIdWithPoints.html");

        assertThat(component(Button.class, By.id("my.button.id")), exist());
        assertThat(new Panel(EvaluatorHolder.get(), "my.panel.id"), exist());
    }

    @Test
     public void can_find_all_buttons() {
         page().open("Button.html");
         Selection<Button> buttons = findAll(Button.class);
         assertThat(buttons.size(), is(7));
 
         assertThat(components(Button.class, By.jQuery("$(':submit')")).size(), is(2));
     }
 
     @Test
     public void can_find_all_Textfields() {
         page().open("InputText.html");
         Selection<TextField> textfields = findAll(TextField.class);
         assertThat(textfields.size(), is(10));
 
         assertThat(components(TextField.class, By.jQuery("$(':text')")).size(), is(10));
     }
 
     @Test
     public void can_find_all_Passwordfields() {
         page().open("InputPassword.html");
         Selection<PasswordField> passwordfields = findAll(PasswordField.class);
         assertThat(passwordfields.size(), is(4));
     }
 
     @Test
     public void can_find_all_Images() {
         page().open("Img.html");
         Selection<Image> images = findAll(Image.class);
         assertThat(images.size(), is(4));
     }
 
     @Test
     public void can_find_all_Links() {
         page().open("Link.html");
         Selection<Link> links = findAll(Link.class);
         assertThat(links.size(), is(4));
     }
 
     @Test
     public void can_find_all_Radios() {
         page().open("Radio.html");
         Selection<Radio> radios = findAll(Radio.class);
         assertThat(radios.size(), is(5));
     }
 
     @Test
     public void can_find_all_Checkboxes() {
         page().open("CheckBox.html");
         Selection<CheckBox> checkboxes = findAll(CheckBox.class);
         assertThat(checkboxes.size(), is(4));
     }
 
     @Test
     public void can_find_all_Listboxes() {
         page().open("Select.html");
         Selection<ListBox> listboxes = findAll(ListBox.class);
         assertThat(listboxes.size(), is(9));
     }
 
     @Test
     public void can_find_all_Datagrids() {
         page().open("DataGrid.html");
         Selection<DataGrid> datagrids = findAll(DataGrid.class);
         assertThat(datagrids.size(), is(2));
     }
 
 }
