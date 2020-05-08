 package br.materdei.bdd.web.driver;
 
 import org.apache.commons.lang.StringUtils;
 import org.jbehave.web.selenium.PropertyWebDriverProvider;
 import org.jbehave.web.selenium.WebDriverProvider;
 
 import br.materdei.bdd.jbehave.config.BddConfigPropertiesEnum;
 
 public final class WebDriverSingleton {
 	
 	private static volatile WebDriverSingleton serverController = null;
 		
 	/**
 	 * Obtém a instância do controlador do selenium server.
 	 * 
 	 * @return SeleniumServerControllerSingleton
 	 */
 	public static synchronized WebDriverSingleton get() {
 		if (serverController == null) {
 			serverController = new WebDriverSingleton();
 		}
 
 		return serverController;
 	}
 	
 	private WebDriverProvider webDriverProvider;
 
 	private WebDriverSingleton() {
 		this.configureWebDriverProvider();
 	}
 	
 	private void configureWebDriverProvider() {
 		String browser = BddConfigPropertiesEnum.WEB_DRIVER_BROWSER.getValue();
 				
 		if ((!"firefox".equalsIgnoreCase(browser)) && (!"chrome".equalsIgnoreCase(browser)) &&
 				(!"ie".equalsIgnoreCase(browser)) && (!"android".equalsIgnoreCase(browser)) &&
 				(!"htmlunit".equalsIgnoreCase(browser))) {
 			throw new RuntimeException("Navegador '" + browser + "' não suportado. Permitido [firefox, chrome, ie, android, htmlunit]");
 		}
 		
 		System.setProperty("browser", browser);
 		if ("chrome".equalsIgnoreCase(browser)) {
 			if (StringUtils.isEmpty(BddConfigPropertiesEnum.WEB_DRIVER_CHROME_LOCATION.getValue())) {
 				throw new RuntimeException("É necessário informar onde a localização do chromedriver através da propriedade web.driver.chrome.location");
 			} else {
				System.setProperty(BddConfigPropertiesEnum.WEB_DRIVER_CHROME_LOCATION.getKey(), BddConfigPropertiesEnum.WEB_DRIVER_CHROME_LOCATION.getValue());
 			}
 		}		
 		
 		this.webDriverProvider = new PropertyWebDriverProvider();
 	}
 	
 	public WebDriverProvider getWebDriverProvider() {
 		return this.webDriverProvider;
 	}
 }
