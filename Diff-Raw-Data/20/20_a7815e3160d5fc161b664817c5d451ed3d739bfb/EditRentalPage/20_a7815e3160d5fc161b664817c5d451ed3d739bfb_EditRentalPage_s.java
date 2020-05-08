 /*
  * Copyright 2013 paynes.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package cz.muni.rentalservice.web.pages;
 
 import cz.muni.rentalservice.db.managers.CarManager;
 import cz.muni.rentalservice.db.managers.CustomerManager;
 import cz.muni.rentalservice.db.managers.RentalManager;
 import cz.muni.rentalservice.models.Rental;
 import cz.muni.rentalservice.web.components.DateDropDown;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.validation.validator.RangeValidator;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 
 /**
  *
  * @author paynes
  */
 public class EditRentalPage extends BasePage {
     
     @SpringBean
     private CarManager carMngr;
     
     @SpringBean
     private CustomerManager customerMngr;
     
     @SpringBean
     private RentalManager rentalMngr;
     
     Boolean payement;
     
     Integer expectedDays;
     
     LocalDate dateFrom;
     
     LocalDate dateTo;
     
     public EditRentalPage() {
         addForm();
         add(new Label("title","Edit rental"));
     }
 
     private void addForm() {
         Form<EditRentalPage> editForm = new Form<>("editRental",
                 new CompoundPropertyModel<>(this));
         
         add(editForm);
         
         Label dateFromLabel = new Label("dateFromLabel", "Date from");
         editForm.add(dateFromLabel);
         
         DateDropDown dateFromField = new DateDropDown("dateFrom");
         editForm.add(dateFromField);
         
         Label dateToLabel = new Label("dateToLabel", "Date to");
         editForm.add(dateToLabel);
         
         DateDropDown dateToField = new DateDropDown("dateTo");
         editForm.add(dateToField);
         
         Label expectedDaysLabel = new Label("expectedDaysLabel", "Expected days");
         editForm.add(expectedDaysLabel);
         
         TextField<Integer> expectedDaysField = new TextField<>("expectedDays");
         RangeValidator<Integer> validator = new RangeValidator<>(1,Integer.MAX_VALUE);
         expectedDaysField.add(validator);
         editForm.add(expectedDaysField);
         
         Label payementLabel = new Label("payementLabel", "Paid");
         editForm.add(payementLabel);
         
         CheckBox box = new CheckBox("payement");
         editForm.add(box);
         
         
         Button submitButton = new Button("submitButton") {
             
             @Override
             public void onSubmit() {
                 Rental r = new Rental();
                 r.setDateFrom(dateFrom);
                 r.setDateTo(dateTo);
                 r.setDays(expectedDays);
                 r.setPaid(payement);
                 r.setCar(carMngr.getCar(Long.valueOf(24)));
                 r.setCustomer(customerMngr.getCustomer(Long.valueOf(2)));
                 
                 rentalMngr.saveRental(r);
                 
                 getSession().info("Rental added successfully.");
                 setResponsePage(RentalsListPage.class);
             }
         };
         
         editForm.add(submitButton);
     }
     
}
