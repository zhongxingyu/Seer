 package com.roosterpark.rptime.selenium.control.complex.reports;
 
 import com.roosterpark.rptime.selenium.control.DropDownList;
 import com.roosterpark.rptime.selenium.control.Option;
 import com.roosterpark.rptime.selenium.control.complex.reports.generator.ClientSelectOptionGenerator;
 import org.openqa.selenium.WebDriver;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: John
  * Date: 1/16/14
  * Time: 1:42 PM
  */
 public class ClientSelect extends DropDownList {
 
    private static final String ID = "client";
 
     private List<Option> options;
     private Map<String, Option> optionsByText;
     private Map<String, Option> optionsByValue;
 
     public ClientSelect(WebDriver driver) {
         super(driver, ID);
         optionsByText = new HashMap<>();
         optionsByValue = new HashMap<>();
     }
 
     public void initialize() {
         ClientSelectOptionGenerator generator = new ClientSelectOptionGenerator(getElement());
         options = generator.generate();
         for (Option option: options) {
             optionsByText.put(option.getText(), option);
             optionsByValue.put(option.getValue(), option);
         }
     }
 
     public void selectOptionByText(String text) {
         getSelect().selectByVisibleText(text);
     }
 
     public void unselectOptionByText(String text) {
         getSelect().deselectByVisibleText(text);
     }
 
     public void selectOptionByValue(String value) {
         getSelect().selectByValue(value);
     }
 
     public void unselectOptionByValue(String value) {
         getSelect().deselectByValue(value);
     }
 
     public List<Option> getAllOptions() {
         return options;
     }
 
     public Option getOptionByText(String text) {
         return optionsByText.get(text);
     }
 
     public Option getOptionByValue(String value) {
         return optionsByValue.get(value);
     }
 
 }
