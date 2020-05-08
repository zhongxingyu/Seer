 /**
 	Copyright (c) 2011, Strata Health Solutions Inc.
  	All rights reserved.
 
 	Redistribution and use in source and binary forms, with or without modification, are permitted 
 	provided that the following conditions are met:
 
 	Redistributions of source code must retain the above copyright notice, this list of conditions 
 	and the following disclaimer.
 
 	Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 	and the following disclaimer in the documentation and/or other materials provided with the distribution.
 
 	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
 	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 	FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 	BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 	OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 	EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
 
 package unit.core.browsers;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriverService;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 import shelob.core.browsers.Chrome;
 
 
 public class ChromeTests {
 
 	private RemoteWebDriver chrome;
 	private RemoteWebDriver chromeGrid;
 	@Before
 	public void setup() {}
 	
 	@After
 	public void teardown() {}
 	
 	@Test
 	public void baseChromeTest(){
 		
		System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY , "/Users/mllaguno/Downloads/chromedriver");
 		chrome = Chrome.getDriver();
 		assertThat(chrome, is(notNullValue()));
 		chrome.quit();
 	}
 	
 	// Expect failure here since there is no running instance of Grid at the specified URL
 	@Test(expected=WebDriverException.class)
 	public void baseChromeGridTest(){
 		
 		try {
			System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY , "/Users/mllaguno/Downloads/chromedriver");
 			chromeGrid = Chrome.getDriver(new URL("http://localhost"));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		assertThat(chromeGrid, is(notNullValue()));
 		
 	}
 	
 	@Test
 	public void capabilitiesTest(){
 		assertThat(Chrome.getDefaultCapabilities().getPlatform(), is(Platform.ANY));
 	}
 }
