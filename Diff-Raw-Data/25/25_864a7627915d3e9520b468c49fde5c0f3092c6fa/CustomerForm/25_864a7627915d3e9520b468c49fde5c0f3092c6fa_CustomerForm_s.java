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
 
 import java.util.Arrays;
 
 import javax.inject.Inject;
 
 import org.vaadin.addons.javaee.container.EntityContainer;
 import org.vaadin.addons.javaee.domain.Customer;
import org.vaadin.addons.javaee.form.BasicEntityForm;
 
public class CustomerForm extends BasicEntityForm<Customer> {
 
     private static final long serialVersionUID = 1L;
 
     @Inject
     private CustomerContainer customerContainer;
 
     public CustomerForm() {
         super(Customer.class);
     }
 
     @Override
     protected void initFields() {
         initFields(Arrays.asList(CustomerFields.SALUTATION, CustomerFields.FIRST_NAME, CustomerFields.LAST_NAME, CustomerFields.ADDRESS
                 + "." + AddressFields.STREET, CustomerFields.ADDRESS + "." + AddressFields.ZIP, CustomerFields.ADDRESS + "."
                 + AddressFields.CITY, CustomerFields.MOBILE, CustomerFields.PHONE, CustomerFields.DATE_OF_BIRTH));
     }
 
     @Override
     protected EntityContainer<Customer> getContainer() {
         return customerContainer;
     }
 
 }
