 package edu.cibertec.buscomida.test.selenium;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import org.openqa.selenium.chrome.ChromeDriver;
 
 import com.thoughtworks.selenium.SeleneseTestBase;
 import com.thoughtworks.selenium.Selenium;
 
 
 public class TestRegistrarRestauranteOk extends SeleneseTestBase {
 	
 	private static final String SELENIUM_SERVER_PORT = "4444";
 	
 	@Before
 	public void setUp() throws Exception {
 		setUp("http://localhost:8080/", "*firefox", Integer.valueOf(SELENIUM_SERVER_PORT));
 		//WebDriver driver = new ChromeDriver();
 		//String baseUrl = "http://localhost:8080/";
 		//Selenium selenium = new WebDriverBackedSelenium(driver, baseUrl);
 		//selenium.start();
 
 //		WebDriver driver = new FirefoxDriver(); 
 //		String baseUrl = "http://localhost:8080/";
 //		Selenium selenium = new WebDriverBackedSelenium(driver, baseUrl);
 //		selenium.start();
 	}
 
 	@Test
 	public void testRegistrarRestauranteOK() throws Exception {	
		
 		selenium.open("buscomida/paginas/busqueda/buscarPlato.jsf");		
 		selenium.click("link=Registrar Restaurante");
 		selenium.waitForPageToLoad("60000");
 		selenium.type("id=resNombreMostrar", "Prueba0001");
 		selenium.type("id=resRazon", "Prueba001");
 		selenium.type("id=resRuc", "12345678911");
 		selenium.type("id=resEmail", "makoto@hotmail.com");
 		selenium.type("id=rsContrasena_input", "123456789");
 		selenium.type("id=resRContrasena", "123456789");
 		selenium.type("id=resDescripcion", "Prueba con Selenium");
 		selenium.click("id=j_idt51");
 		selenium.type("id=sucDireccion", "Prueba Direccion");
 		selenium.select("name=j_idt92", "label=ASIA");
 		selenium.click("//div[@id='gmap']/div/div/div/div[4]/div/div/div");
 		selenium.type("name=j_idt96", "4539977");
 		selenium.click("id=btnAceptarSuc");
 		selenium.click("id=j_idt65");
 		selenium.type("id=nombremostrar", "Prueba Plato0001");
 		selenium.select("name=j_idt110", "label=PESCADOS Y MARISCOS");
 		selenium.select("name=j_idt114", "label=MODERADO2");
 		selenium.type("name=j_idt118", "Prueba 001");
 		selenium.click("id=j_idt128");
 		selenium.click("id=j_idt85");
 		verifyTrue(selenium.isTextPresent("Bienvenidos Prueba0001"));
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		selenium.stop();
 	}
 }
