 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.apache.commons.lang.StringUtils;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class NonCodedDiagnosesList extends AbstractPageObject {
 
    private static final By NON_CODED_DIAGNOSES_TABLE_DATA = By.cssSelector(".non-coded-diagnoses-td");
    private static final By CODE_DIAGNOSIS_LINK = By.cssSelector(".codeDiagnosis");
 
     public NonCodedDiagnosesList(WebDriver driver) {
         super(driver);
     }
 
     public List<String> getNonCodedDiagnoses() {
         List<String> nonCodedDiagnoses = new ArrayList<String>();
         List<WebElement> tds = driver.findElements(NON_CODED_DIAGNOSES_TABLE_DATA);
        for (int i = 0; i < tds.size(); i +=1){
             nonCodedDiagnoses.add((String)tds.get(i).getText());
         }
         return nonCodedDiagnoses;
     }
 
     public void openCodeDiagnosisDialog(String nonCodedDiagnosis){
         List<WebElement> tds = driver.findElements(NON_CODED_DIAGNOSES_TABLE_DATA);
        for (int i = 0; i < tds.size(); i +=1){
            WebElement webElement = tds.get(i);
            String item = (String)webElement.getText();
             if(StringUtils.equals(item, nonCodedDiagnosis)){
                WebElement codeDiagnosisLink = webElement.findElement(By.xpath("..")).findElement(CODE_DIAGNOSIS_LINK);
                 if (codeDiagnosisLink != null){
                     codeDiagnosisLink.click();
                     return;
                 }
             }
         }
     }
 }
