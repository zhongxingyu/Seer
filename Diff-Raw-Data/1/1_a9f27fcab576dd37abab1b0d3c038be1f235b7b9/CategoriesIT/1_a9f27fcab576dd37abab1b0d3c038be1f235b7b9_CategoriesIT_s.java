 package com.sk.admin.web.view;
 
 import static junit.framework.Assert.assertEquals;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class CategoriesIT {
 
 	private WebDriver webDriver;
 	
 	@Before
 	public void init(){
 		webDriver = new FirefoxDriver();
 	}
 	
 	@After
 	public void destroy(){
 		webDriver.close();
 	}
 	
 	@Test
 	public void shouldShowCategoryForm(){
 		webDriver.get("http://localhost:8080/Admin/categoryForm.xhtml");
 		WebElement saveSpan = webDriver.findElement(By.id("save")).findElement(By.tagName("span"));
 		assertEquals("Save", saveSpan.getText());
 	}
 }
