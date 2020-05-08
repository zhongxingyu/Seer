 package com.euroit.militaryshop;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class ProductCardPage extends BasePage {
 
 	public ProductCardPage(WebDriver driver) {
 		super(driver);
 	}
 
 	public void selectFirstItemOptions() {
		driver.findElement(By.cssSelector("#colorContainer img")).click();
 		driver.findElement(By.cssSelector("input[name=material]")).click();
 		driver.findElement(By.id("addToTrolley")).click();
 	}
 }
