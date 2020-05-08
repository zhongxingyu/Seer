 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.pages;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.FindBy;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 public class AbstractListPage extends AbstractPage {
 
   protected final RemoteWebDriver driver;
 
   @FindBy(css = "table.zebra-striped tbody")
   private WebElement table;
 
   public AbstractListPage(RemoteWebDriver driver) {
     this.driver = driver;
   }
 
   public String getTable() {
     return table.getText();
   }
 
   public void delete(String... fields) {
     WebElement row = findRow(fields);
 
    WebElement deleteButton = row.findElement(By.cssSelector("input[type=image]"));
     deleteButton.click();
     driver.switchTo().alert().accept();
   }
 
   protected void editRow(String... fields) {
     findRow(fields).findElement(By.cssSelector("a[alt~=Update]")).click();
   }
 
   public WebElement findRow(String... fields) {
     List<WebElement> rows = table.findElements(By.tagName("tr"));
 
     for (final WebElement row : rows) {
       if (containsAll(row, fields)) {
         return row;
       }
     }
 
     throw new NoSuchElementException(String.format("row with fields '%s' not found in rows: '%s'",
         Joiner.on(',').join(fields), Joiner.on(" | ").join(rows)));
   }
 
   private boolean containsAll(final WebElement row, String... fields) {
     return Iterables.all(Arrays.asList(fields), new Predicate<String>() {
       @Override
       public boolean apply(String field) {
         return row.getText().contains(field);
       }
     });
   }
 
   public boolean containsAnyItems() {
     try {
       table.getText();
     }
     catch (NoSuchElementException e) {
       return false;
     }
 
     return true;
   }
 }
