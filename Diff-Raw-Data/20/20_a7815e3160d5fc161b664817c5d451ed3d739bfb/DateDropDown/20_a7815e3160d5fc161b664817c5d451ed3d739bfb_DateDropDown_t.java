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
 package cz.muni.rentalservice.web.components;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.FormComponentPanel;
 import org.apache.wicket.model.PropertyModel;
 import org.joda.time.LocalDate;
 
 /**
  *
  * @author paynes
  */
 public class DateDropDown extends FormComponentPanel<LocalDate> {
     
     //private static final long serialVersionUID = 1L;
 
     private Integer month;
     private Integer day;
     private Integer year;
     
     private DropDownChoice<Integer> monthField;
     private DropDownChoice<Integer> dayField;
     private DropDownChoice<Integer> yearField;
     
     private List<Integer> months;
     private List<Integer> days;
     private List<Integer> years;
     
     private void initLists() {
         months = new ArrayList<>();
         for (int i = 1; i<13;i++) {
             months.add(i);
         }
         
         years = new ArrayList<>();
         
         for (int i = 1930; i<2051;i++) {
             years.add(i);
         }
         
         days = new ArrayList<>();
         
         for (int i = 1;i<32;i++) {
             days.add(i);
         }
     }
     
     public DateDropDown(String id) {
         super(id);
         
         initLists();
                 
         monthField = new DropDownChoice<>("month", new PropertyModel<Integer>(this,"month"),months);
         monthField.setRequired(true);
         add(monthField);
         
         dayField = new DropDownChoice<>("day", new PropertyModel<Integer>(this,"month"),days);
         dayField.setRequired(true);
         add(dayField);
         
         yearField = new DropDownChoice<>("year", new PropertyModel<Integer>(this,"month"),years);
         yearField.setRequired(true);
         add(yearField);
     }
     
     public Integer getMonth() {
         return this.month;
     }
     
     public Integer getDay() {
         return this.day;
     }
     
     public Integer getYear() {
         return this.year;
     }
     
     public void setMonth(Integer month) {
         this.month = month;
     }
     
     public void setDay(Integer day) {
         this.day = day;
     }
     
     public void setYear(Integer year) {
         this.year = year;
     }
    
     
     @Override
     protected void convertInput() {
         setMonth(monthField.getConvertedInput());
         setDay(dayField.getConvertedInput());
         setYear(yearField.getConvertedInput());
         setConvertedInput(new LocalDate(getYear(),getMonth(),getDay()));
     }
 }
