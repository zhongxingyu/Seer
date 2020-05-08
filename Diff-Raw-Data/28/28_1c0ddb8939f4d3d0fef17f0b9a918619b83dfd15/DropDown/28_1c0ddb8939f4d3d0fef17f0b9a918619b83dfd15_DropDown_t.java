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
 
 package org.testatoo.cartridge.html4.element;
 
 import org.testatoo.cartridge.html4.HtmlEvaluator;
 import org.testatoo.core.ComponentException;
 import org.testatoo.core.Selection;
 
 /**
  * This class allows the testing of a DropDown for html4. Dropdown is equivalent to a "select" element, with simple selection.
  *
  * @author dev@testatoo.org
  * @see <a href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT"> "select" specification </a>
  */
 
 public final class DropDown extends org.testatoo.core.component.DropDown {
 
     private Select select;
 
     /**
      * Class constructor specifying the evaluator to use and the id of the "select" element we want to test.
      *
      * @param evaluator a UI Test engine specific html evaluator
      * @param id        the id (unique) of the "select" element
      * @throws ComponentException if the id is a multiple select
      */
     public DropDown(HtmlEvaluator evaluator, String id) {
         super(evaluator, id);
         this.evaluator = evaluator;
         select = new Select(evaluator, id);
     }
 
     /**
      * To get the values in the list.
      *
      * @return a list of String
      */
     public Selection<String> values() {
         return select.values();
     }
 
     /**
      * To select a given value in the list.
      *
      * @param value the value we want to select
      */
     public void select(String value) {
         select.select(value);
     }
 
     /**
      * To get the selected value in the list.
      *
      * @return the selected value or empty string if no value selected
      */
     public String selectedValue() {
         if (select.selectedValues().size() == 0)
             return "";
         return select.selectedValues().get(0);
     }

    /**
     * To get a Selection content the selected value in the list
     *
     * @return the
     */
    @Override
    protected Selection<String> listSelectedValues() {
        return select.selectedValues();
    }
 }
