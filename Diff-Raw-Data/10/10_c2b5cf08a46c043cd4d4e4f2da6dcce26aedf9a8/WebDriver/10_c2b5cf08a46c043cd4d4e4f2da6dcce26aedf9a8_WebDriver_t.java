 package br.materdei.bdd.model;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 
 import br.materdei.bdd.jbehave.config.BddConfigPropertiesEnum;
 
 public class WebDriver {
 
 	/**
 	 * Tempo em segundos.
 	 */
 	public static final Integer DEFAULT_DRIVER_TIMEOUT = 30;
 	public static final BrowserEnum DEFAULT_BROWSER = BrowserEnum.FIREFOX;
 	
 	private Integer driverTimeout;
 	private BrowserEnum browser;
 	private File chromeDriver;
 	private boolean windowMaximized;
 	
 	public WebDriver() {
 		this.useDefaultValues();
 	}
 	
 	public WebDriver(Properties p) {
 		BrowserEnum b = BrowserEnum.browserFromString(p.getProperty(BddConfigPropertiesEnum.WEB_DRIVER_BROWSER.getKey()));
 		this.useBrowser(b);
 		
		String value = p.getProperty(BddConfigPropertiesEnum.WEB_DRIVER_CHROME_DRIVER.getKey());
		if (!StringUtils.isEmpty(value)) {
			File f = new File(value);
			this.useChromeDriver(f);
		}
 		
		value = p.getProperty(BddConfigPropertiesEnum.WEB_DRIVER_TIMEOUT.getKey());
 		if (!StringUtils.isEmpty(value)) {
 			this.useDriverTimeout(Integer.parseInt(value));
 		}
 		
 		value = p.getProperty(BddConfigPropertiesEnum.WEB_DRIVER_WINDOW_MAXIMIZED.getKey());
 		if (!StringUtils.isEmpty(value)) {
 			this.useWindowMaximized(Boolean.parseBoolean(value));
 		}
 		
 		this.useDefaultValues();
 	}
 
 	public Integer getDriverTimeout() {
 		return driverTimeout;
 	}
 
 	/**
 	 * Tempo limite de espera do driver até encontrar um componente na tela.
 	 * Valor padrão: 30 segundos
 	 * 
 	 * @param driverTimeout
 	 * @return Este objeto
 	 */
 	public WebDriver useDriverTimeout(Integer driverTimeout) {
 		if (driverTimeout != null) {
 			if (driverTimeout <= 0) {
 				throw new IllegalArgumentException("Driver timeout deve ser maior que zero.");
 			}
 			this.driverTimeout = driverTimeout;
 		}
 		
 		return this;
 	}
 
 	public BrowserEnum getBrowser() {
 		return browser;
 	}
 
 	/**
 	 * Navegador que será utilizado para executar os testes funcionais.
 	 * Valor padrão: Firefox
 	 * 
 	 * @param browser
 	 * @return Este objeto
 	 */
 	public WebDriver useBrowser(BrowserEnum browser) {
 		if (browser != null) {
 			this.browser = browser;
 		}
 		
 		return this;
 	}
 
 	public File getChromeDriver() {
 		return chromeDriver;
 	}
 
 	/**
 	 * Para usar o Google-Chrome é necessário informar a localização do ChromeDriver.
 	 * 
 	 * @param chromeDriver
 	 * @return Este objeto
 	 */
 	public WebDriver useChromeDriver(File chromeDriver) {
 		this.chromeDriver = chromeDriver;
 		if (!chromeDriver.exists()) {
 			throw new IllegalArgumentException(chromeDriver.getAbsolutePath() + " não existe!");
 		} else if (chromeDriver.isDirectory()) {
 			throw new IllegalArgumentException(chromeDriver.getAbsolutePath() + " é um diretório! Deveria ser um arquivo binário");
 		}
 		
 		return this;
 	}
 	
 	public boolean isWindowMaximized() {
 		return windowMaximized;
 	}
 	
 	/**
 	 * Maximiza a janela do navegador que está executando os testes.
 	 * Valor padrão: false
 	 * 
 	 * @param windowMaximized
 	 * @return Este objeto
 	 */
 	public WebDriver useWindowMaximized(boolean windowMaximized) {
 		this.windowMaximized = windowMaximized;
 		return this;
 	}
 	
 	public void validation() {
 		if (this.driverTimeout == null) {
 			throw new RuntimeException("Um valor deve ser informado para " + BddConfigPropertiesEnum.WEB_DRIVER_TIMEOUT.getKey());
 		} else if (this.driverTimeout <= 0) {
 			throw new RuntimeException(BddConfigPropertiesEnum.WEB_DRIVER_TIMEOUT.getKey() + " deve ser maior que zero");
 		}
 		
 		if (this.browser == null) {
 			throw new RuntimeException("Um valor deve ser informado para " + BddConfigPropertiesEnum.WEB_DRIVER_BROWSER.getKey());
 		}
 	}
 	
 	private void useDefaultValues() {
 		if (this.driverTimeout == null) {
 			this.driverTimeout = DEFAULT_DRIVER_TIMEOUT;
 		}
 		
 		if (this.browser == null) {
 			this.browser = DEFAULT_BROWSER;
 		}
 	}
 }
