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
 package it.com.pyxis.jira.selenium;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.PageFactory;
 
 public class DashboardPage {
 
 	private WebDriver driver;
 
 	public DashboardPage(WebDriver driver) {
 		this.driver = driver;
 		open();
 	}
 
 	public void open() {
 		driver.navigate().to("http://localhost:2990/jira");
 	}
 
 	public void login(String username, String password) {
 		PageFactory.initElements(driver, LoginGadget.class).doLogin(username, password);
		open();
 	}
 
 	public void loginAsAdmin() {
 		login("admin", "admin");
 	}
 }
