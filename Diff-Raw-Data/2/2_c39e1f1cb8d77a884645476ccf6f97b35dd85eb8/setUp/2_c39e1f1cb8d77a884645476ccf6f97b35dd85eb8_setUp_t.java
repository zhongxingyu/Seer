 package com.partnerpedia.appzone.gui.spider;
 
 import org.openqa.selenium.By;
 //import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 
 public class setUp extends Base {
 	
 
 	public static int login(String baseURL, String browser, String store, String user, String password) {
 
 //		System.out.println("baseURL=" + baseURL);
 //		System.out.println("browser=" + browser);
 //		System.out.println("store=" + store);
 //		System.out.println("user=" + user);
 //		System.out.println("password=" + password);
 		
 		String url = baseURL + "/" + store + "/" + "account/new";
 		System.out.println("url=" + url);
 		
 		switch (browser) {
 		case "FireFox":
 			DRIVER = new FirefoxDriver();
 			break;
 		case "IE":
			System.setProperty("webdriver.ie.driver", "ExePath\\IEDriverServer.exe");
 			DRIVER = new InternetExplorerDriver();
 			break;
 		case "Chrome":
 			DRIVER = new ChromeDriver();
 			break;
 		case "HtmlDriver":
 			DRIVER = new HtmlUnitDriver();
 			break;
 		default:
 			System.out.println("The specified browser is not supported or incorrect");
 			System.exit(100);
 		}
 
 		DRIVER.get(url);
 
 		if (DRIVER != null)
 		{
 			System.out.println("return = " + url);
 			System.out.println("finding name = account[email]");
 			DRIVER.findElement(By.name("account[email]")).sendKeys(user);
 			DRIVER.findElement(By.name("account[password]")).sendKeys(password);
 			System.out.println("click the button");
 			DRIVER.findElement(By.name("commit")).click();
 			//driver.findElement(By.name("commit")).submit();
 			return 0;
 		}
 		
 		return 1;
 		
 	}	
 }
