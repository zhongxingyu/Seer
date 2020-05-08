 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.apache.commons.lang.StringUtils;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class NonCodedDiagnosesList extends AbstractPageObject {
 
    private static final By NON_CODED_DIAGNOSES_TABLE_DATA = By.cssSelector("#non-coded-diagnoses td");
 
     public NonCodedDiagnosesList(WebDriver driver) {
         super(driver);
     }
 
     public List<String> getNonCodedDiagnoses() {
         List<String> nonCodedDiagnoses = new ArrayList<String>();
         List<WebElement> tds = driver.findElements(NON_CODED_DIAGNOSES_TABLE_DATA);
        for (int i = 0; i < tds.size(); i +=5){
             nonCodedDiagnoses.add((String)tds.get(i).getText());
         }
         return nonCodedDiagnoses;
     }
 
     public void openCodeDiagnosisDialog(String nonCodedDiagnosis){
         List<WebElement> tds = driver.findElements(NON_CODED_DIAGNOSES_TABLE_DATA);
        for (int i = 0; i < tds.size(); i +=5){
            String item = (String)tds.get(i).getText();
             if(StringUtils.equals(item, nonCodedDiagnosis)){
                WebElement codeDiagnosisLink = tds.get(i+4);
                 if (codeDiagnosisLink != null){
                     codeDiagnosisLink.click();
                     return;
                 }
             }
         }
     }
 }
