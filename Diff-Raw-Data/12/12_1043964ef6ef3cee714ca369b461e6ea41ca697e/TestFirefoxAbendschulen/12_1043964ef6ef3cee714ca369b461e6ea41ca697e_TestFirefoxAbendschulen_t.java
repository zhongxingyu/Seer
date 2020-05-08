 package eu.testingmachine.eu;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.internal.FindsByClassName;
 import org.openqa.selenium.internal.FindsByLinkText;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 
 public class TestFireFoxAbendschulen {
  
     // create a WebDriver
     WebDriver driver;
  
 
     @BeforeClass
     public void setUp(){
 	
 	driver = new FirefoxDriver();
 	
     }
      
     // quit from WebDriver
     @AfterClass
     public void tearDown(){
 	driver.quit();
     }
  
     //Start testing AbendSchulen http://www.provinz.bz.it/de/dienste/dienste-az.asp?bnsvaz_svid=1003544
 
     
     @Test
     public void testFindElements()throws Exception{
 	 
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	//driver.findElement(By.linkText("Abendschulen")).click();
 	//String expectedTitle = "Abendschulen | Dienste A-Z | Südtiroler Bürgernetz";
 	//String actualTitle = driver.getTitle();
 	//Assert.assertEquals(driver.findElement(By.linkText("Abendschule für Erwachsene - Mittelschule (Italienisches Schulamt)")));
 	
     }
     
     @Test
     public void verifyTitleAbendSchulen() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	String expectedTitle = "Abendschulen | Dienste A-Z | Südtiroler Bürgernetz";
 	String actualTitle = driver.getTitle();
 	Assert.assertEquals(actualTitle, expectedTitle);
 	
     } 
     
     @Test
     public void verifyLinkAbendschulen() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	WebElement element = ((FindsByLinkText)driver).findElementByLinkText("Abendschule für Erwachsene - Mittelschule (Italienisches Schulamt)");
 	
     }
     
     @Test
     public void verifyTextAbendSchulen() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendschulenErwachsen();
 	Assert.assertEquals("Nella nostra provincia l'Istituto Comprensivo Bolzano III di via Napoli 1 - Bolzano è il punto di riferimento per questo tipo di servizio.", driver.findElement(By.xpath("//div[@id='main']/p[3]")).getText());
     }
 
     // Verify all links on http://www.provinz.bz.it/de/dienste/dienste-az.asp?bnsvaz_svid=1003544
     
     @Test
     public void verifyLinkScolastica() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendschulenErwachsen();
 	driver.findElement(By.linkText("sito dell'Intendenza scolastica italiana")).click();
 	String expectedTitle = "Corsi per adulti | Intendenza scolastica italiana | Provincia autonoma di Bolzano - Alto Adige";
 	String actualTitle = driver.getTitle();
 	Assert.assertEquals(actualTitle, expectedTitle);
 	
     } 
     
     @Test
     public void verifyLinkAbendschuleWebsite() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendschulenErwachsen();
 	driver.findElement(By.linkText("Webseite der für diesen Dienst zuständigen Institution")).click();
 	String expectedTitle = "Corsi per adulti | Intendenza scolastica italiana | Provincia autonoma di Bolzano - Alto Adige";
 	String actualTitle = driver.getTitle();
 	Assert.assertEquals(actualTitle, expectedTitle);
 }
 
     //Verify all contact links on http://www.provinz.bz.it/de/dienste/dienste-az.asp?bnsvaz_svid=1003544
 
     @Test
     public void checkAbdendSchulenContactEmail() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendschulenErwachsen();
 	Assert.assertEquals("is.organici@scuola.alto-adige.it", driver.findElement(By.linkText("is.organici@scuola.alto-adige.it")).getText());
     }
 
     @Test
     public void checkAbendSchulenContactPEC() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendschulenErwachsen();
 	Assert.assertEquals("is.organici@pec.prov.bz.it", driver.findElement(By.linkText("is.organici@pec.prov.bz.it")).getText());
 
     }
 
     @Test
     public void checkAbendSchulenContactWebsite() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendschulenErwachsen();
 	Assert.assertEquals("http://www.provinz.bz.it/italienisches-schulamt/verwaltung/83.asp", driver.findElement(By.linkText("http://www.provinz.bz.it/italienisches-schulamt/verwaltung/83.asp")).getText());


     }
     
     //Start testing Abendoberschule http://www.provinz.bz.it/de/dienste/dienste-az.asp?bnsvaz_svid=1005822
     
     @Test
     public void verifyLinkAbendoberschule() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendoberschule();
 	String expectedTitle = "Deutschsprachige Abendoberschule | Dienste A-Z | Südtiroler Bürgernetz";
 	String actualTitle = driver.getTitle();
 	Assert.assertEquals(actualTitle, expectedTitle);
 	
     }
     
     @Test
     public void verifyTextAbendoberschule() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendoberschule();
 	Assert.assertEquals("Die Abendkurse erstrecken sich über drei Jahre. Das erste Kursjahr bereitet auf die Eignungsprüfung der ersten und zweiten Klasse, das zweite Kursjahr auf die Eignungsprüfung der dritten und vierten Klasse und das dritte Kursjahr die Privatistinnen und Privatisten auf die Matura vor.", driver.findElement(By.xpath("//div[@id='main']/p[]")).getText());
     }
     
     // Verify all links on http://www.provinz.bz.it/de/dienste/dienste-az.asp?bnsvaz_svid=1005822
     
     @Test
     public void verifyLinkAbendoberschuleWebsite() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendoberschule();
 	driver.findElement(By.linkText("Webseite der für diesen Dienst zuständigen Institution")).click();
 	String expectedTitle = "Autonome Provinz Bozen - Südtirol | Deutsches Schulamt | Abendschule";
 	String actualTitle = driver.getTitle();
 	Assert.assertEquals(actualTitle, expectedTitle);
     }
     
     @Test
     public void verifyLinkAbendoberschuleVerwaltung() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendoberschule();
 	driver.findElement(By.linkText("http://www.provinz.bz.it/schulamt/verwaltung/111.asp?intOrga_id=8")).click();
 	String expectedTitle = "Autonome Provinz Bozen - Südtirol | Deutsches Schulamt | Mitarbeiter/innen";
 	String actualTitle = driver.getTitle();
 	Assert.assertEquals(actualTitle, expectedTitle);
     }
 
     //Verify all contact links on http://www.provinz.bz.it/de/dienste/dienste-az.asp?bnsvaz_svid=1005822
 
     @Test
     public void checkAbdendoberschuleContactEmail() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendoberschule();
	Assert.assertEquals("sa.schulverwaltung@schule.suedtirol.it", driver.findElement(By.linkText("sa.schulverwaltung@schule.suedtirol.it")).getText());
     }
 
     @Test
     public void checkAbendoberchuleContactPEC() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendoberschule();
	Assert.assertEquals("schulverwaltung.ammscolastica@pec.prov.bz.it", driver.findElement(By.linkText("schulverwaltung.ammscolastica@pec.prov.bz.it")).getText());
 
     }
 
     @Test
     public void checkAbendoberchuleContactWebsite() {
 	loadServicesPage();
 	clickAndLoadAbendschulen();
 	clickAndLoadAbendoberschule();
	Assert.assertEquals("http://www.provinz.bz.it/schulamt/verwaltung/111.asp?intOrga_id=8", driver.findElement(By.xpath("//a[contains(.,'http://www.provinz.bz.it/schulamt/verwaltung/111.asp?intOrga_id=8')]")).getText());

     }
     
     private void loadServicesPage() {
 	driver.get("http://www.provinz.bz.it/de/dienste/dienste-az.asp");
     }
     
     private void clickAndLoadAbendschulen() {
 	driver.findElement(By.linkText("Abendschulen")).click();
     }
     
     private void clickAndLoadAbendschulenErwachsen() {
 	driver.findElement(By.linkText("Abendschule für Erwachsene - Mittelschule (Italienisches Schulamt)")).click();
     }
     
     private void clickAndLoadAbendoberschule() {
 	driver.findElement(By.linkText("Deutschsprachige Abendoberschule")).click();
     }
     
 }
