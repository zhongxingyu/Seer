 /*******************************************************************************
  * Copyright 2012 Thomas Letsch
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  *******************************************************************************/
 package org.vaadin.addons.javaee.ui;
 
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.vaadin.addons.javaee.domain.Customer;
import org.vaadin.addons.javaee.form.BasicEntityForm;
 import org.vaadin.addons.javaee.page.BasicEditPage;
 import org.vaadin.addons.javaee.page.ContentView;
 
 public class CustomerNewPage extends BasicEditPage<Customer> implements ContentView {
 
     private static final long serialVersionUID = 1L;
 
     public static final String PAGE = "CustomerNew";
 
     public static final String ENTITY = "Customer";
 
     @Inject
     CustomerForm form;
 
     @Inject
     javax.enterprise.event.Event<CustomerChangedEvent> customerChanged;
 
     public CustomerNewPage() {
         super(PAGE, ENTITY);
     }
 
     @Override
    protected BasicEntityForm<Customer> getForm() {
         return form;
     }
 
     @Override
     public void onShow(String comingFrom, Map<String, Object> parameters) {
         form.editNew();
     }
 
     @Override
     public void saveClicked() {
         super.saveClicked();
         CustomerChangedEvent event = new CustomerChangedEvent(form.getEntity());
         customerChanged.fire(event);
     }
 }
