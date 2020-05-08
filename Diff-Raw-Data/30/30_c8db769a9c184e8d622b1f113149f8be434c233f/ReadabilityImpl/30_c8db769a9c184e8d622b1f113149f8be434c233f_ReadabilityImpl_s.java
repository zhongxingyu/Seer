 package com.github.pageobject.impl.readability;
 
 import static com.google.common.base.Preconditions.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 import com.github.pageobject.impl.Readability;
 public class ReadabilityImpl implements Readability{
 
 	private Map<String,String> db;
 	private WebDriver driver;
 	private String result;
 	
 	public ReadabilityImpl(WebDriver webDriver) {
 		this.db = new HashMap<String, String>();
 		this.driver = webDriver;
 	}
 
	@Override
	public String read(String propertyName) {
 		checkNotNull(this.db.get(propertyName),"Property '"+propertyName+"' not found.");
 		try{
			return checkNotNull(
 					driver.findElement(By.xpath(db.get(propertyName))).getText()
 				);	
 		}catch(NullPointerException ex){
			return	checkNotNull(
 					driver.findElement(By.xpath(db.get(propertyName))).getAttribute("value")
				);
 		}
 	}
 
 	@Override
 	public void setProperty(String propertyName, String xpathValue) {
 		this.db.put(checkNotNull(propertyName),checkNotNull(xpathValue));
 	}
 	
 	@Override
 	public Map<String,String> getDb(){
 		return db;
 	}
 }
