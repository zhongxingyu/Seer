 /*
  * Copyright 2013 Telegraph Hill (http://www.thpii.com/).
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.kulana.demo.gui;
 
 import java.util.List;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.FindBy;
 
 import com.kulana.core.foundation.webdriver.decorator.ExtendedWebElement;
 import com.kulana.core.gui.AbstractPage;
 
 public class GoogleMapsPage extends AbstractPage 
 {
 	@FindBy(name="q")
 	public ExtendedWebElement searchTextField;
 	
	//@FindBy(id="gbqfb")
	@FindBy(className="searchbutton")
 	public ExtendedWebElement searchButton;
 	
	//@FindBy(linkText="Get directions")
	@FindBy(className="cards-icon-link")
 	public ExtendedWebElement getDirectionsLink;
 	
	//@FindBy(id="d_sub")
	@FindBy(className="cards-icon-link")
 	public ExtendedWebElement getDirectionsButton;
 
	//@FindBy(id="d_d")
	//@FindBy(className = "tactile-searchbox-input")
	//@FindBy(id="gs_tti52")
	@FindBy(xpath = "//td[@id='gs_tti52']/input")
 	public ExtendedWebElement pointATextField;
 	
	//@FindBy(id="d_daddr")
	//@FindBy(id="gs_tti51")
	@FindBy(xpath = "//td[@id='gs_tti51']/input")
 	public ExtendedWebElement pointBTextField;
 	
	//@FindBy(xpath="//div[@class='dir-altroute-inner']")
	@FindBy(xpath="//div[@class='cards-card']")
 	public List<ExtendedWebElement> routes;
 	
 	public GoogleMapsPage(WebDriver driver) 
 	{
 		super(driver);
 		setPageAbsoluteURL("https://maps.google.com/");
 	}
 	
 	public void search(String q)
 	{
 		type(searchTextField, q);
 		click(searchButton);
 	}
 	
 	public List<ExtendedWebElement> getDirections(String from, String to)
 	{
 		type(pointATextField, from);
 		pressEnter(pointATextField);
 		type(pointBTextField, to);
 		pressEnter(pointBTextField);
		//click(getDirectionsButton);
 		pause(5);
 		return routes;
 	}
 }
