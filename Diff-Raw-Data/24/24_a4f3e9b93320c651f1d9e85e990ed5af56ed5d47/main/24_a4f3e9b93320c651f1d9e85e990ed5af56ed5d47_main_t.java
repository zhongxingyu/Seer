 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 public class main {
 	
 	static List<WebElement> files;
 	
 	static Boolean hasFiles;
 	
 	static WebDriver driver;
 	
 	static WebElement login;
 	
 	static WebElement pass;
 	
 	static int filesNumber;
 	
 	static Element eElement;
 	
 	public static void main(String[] args) throws InterruptedException, SAXException, IOException, ParserConfigurationException{
 		NodeList nList = readXml("cfg.xml");
 				
 		for (int temp = 0; temp < nList.getLength(); temp++) {
 			 
 			Node nNode = nList.item(temp);
 			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 				eElement = (Element) nNode;
 				driver = new HtmlUnitDriver();
 				driver.get("http://files.mail.ru/");
 				
 				login = driver.findElement(By.name("Login"));
 				pass = driver.findElement(By.name("Password"));
 				
 				login.sendKeys(getTagValue("login", eElement));
 				pass.sendKeys(getTagValue("password", eElement));
				driver.findElement(By.xpath("//input[@value='Войти']")).click();				
 				driver.findElement(By.xpath("//a[@href='/ls/4']")).click();
				filesNumber = filesCount();
 				if (filesNumber > 0) {
 		    		hasFiles = true;		    		
 		    	} else {
 		    		hasFiles = false;
 		    	}		
 	    		
 				printLog();
 	    		
 				while (true == hasFiles) {
 					driver.findElement(By.xpath("//table[@class='fileList']/tbody/tr[3]/td[@class='do']/div/a/img[@alt='[Undelete]']/..")).click();	
 					Thread.sleep(2000);		
					filesNumber = filesCount();
 					if (filesNumber > 0) {
						printLog();
						hasFiles = true;						
 					} else {
 						printLog();
 						hasFiles = false;
 					}
 				}
 				driver.close();	
 		   }
 		}
 	}
 	
 	public static NodeList readXml(String file) throws SAXException, IOException, ParserConfigurationException {
 		File fXmlFile = new File(file);
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		Document doc = dBuilder.parse(fXmlFile);
 		doc.getDocumentElement().normalize();
 		NodeList nList = doc.getElementsByTagName("account");
 		
 		return nList;
 	}
 		
 	private static String getTagValue(String sTag, Element eElement) {
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 	    Node nValue = (Node) nlList.item(0);
 		return nValue.getNodeValue();
 	}
 	
 	private static void printLog() {
 		System.out.print("Account " + getTagValue("login", eElement) + " has " + filesNumber + " files\n");
 	}
	
	private static int filesCount() {
		return Integer.parseInt(driver.findElement(By.xpath("//span[@id='ffoldercnt4']")).getText());
	}
 }
