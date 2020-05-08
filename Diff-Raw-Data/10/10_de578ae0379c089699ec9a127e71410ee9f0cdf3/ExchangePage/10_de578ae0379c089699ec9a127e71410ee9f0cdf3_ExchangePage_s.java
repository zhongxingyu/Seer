 package com.telstra.webtest.acceptance.pages;
 
 import com.telstra.webtest.domain.Currency;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 public class ExchangePage extends BasePage {
 
     @Override
     protected String getPath() {
         return "/exchange/";
     }
 
     public void exchange(Currency fromCurrency, Currency toCurrency, double fromAmount) {
         WebElement fromCurrencyElement = webDriver.findElement(By.name("fromCurrency"));
         fromCurrencyElement.findElement(By.cssSelector("option[value=" + fromCurrency.name() + "]")).setSelected();
 
         WebElement fromAmountElement = webDriver.findElement(By.name("fromAmount"));
         fromAmountElement.clear();
         fromAmountElement.sendKeys(String.valueOf(fromAmount));
 
         WebElement toCurrencyElement = webDriver.findElement(By.name("toCurrency"));
         toCurrencyElement.findElement(By.cssSelector("option[value=" + toCurrency.name() + "]")).setSelected();
 
         webDriver.findElement(By.cssSelector("form.exchange")).submit();
     }
 
     public double getToAmount() {
         return Double.valueOf(webDriver.findElement(By.cssSelector("form.exchange input[name='toAmount']")).getValue());
     }
 
     public boolean hasErrorMessage(String message) {
        return false;
     }
 }
