 /**
  * Copyright (c) 2010 Pyxis Technologies inc.
  *
  * This is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA,
  * or see the FSF site: http://www.fsf.org.
  */
 package it.com.pyxis.jira.monitoring.gadget.mapping;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import static it.com.pyxis.jira.selenium.WebDriverHelper.waitForElementToAppear;
 
 public class MonitoringGadget {
 
	private static final String GADGET_ID = "gadget-10011";

 	private WebElement content;
 
 	public MonitoringGadget(WebDriver driver) {
		waitForElementToAppear(driver, By.id(GADGET_ID));
		driver.switchTo().frame(GADGET_ID);
		
 		content = waitForElementToAppear(driver, By.id("gadget_monitoring_user"));
 	}
 
 	public String getText() {
 		return content.getText();
 	}
 }
