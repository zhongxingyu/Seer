 package net.sf.testium.selenium;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.ElementNotVisibleException;
 import org.openqa.selenium.NotFoundException;
 import org.openqa.selenium.Point;
 import org.openqa.selenium.StaleElementReferenceException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.internal.Coordinates;
 import org.openqa.selenium.internal.FindsByClassName;
 import org.openqa.selenium.internal.FindsByCssSelector;
 import org.openqa.selenium.internal.FindsById;
 import org.openqa.selenium.internal.FindsByLinkText;
 import org.openqa.selenium.internal.FindsByName;
 import org.openqa.selenium.internal.FindsByTagName;
 import org.openqa.selenium.internal.FindsByXPath;
 import org.openqa.selenium.internal.Locatable;
 import org.openqa.selenium.internal.WrapsDriver;
 
 public class SimplePageElement implements SmartWebElement, //PageElement,
 		FindsByLinkText, FindsById, FindsByName, FindsByTagName, FindsByClassName, FindsByCssSelector, FindsByXPath,
 		WrapsDriver, Locatable {
 
 	private final By myBy;
 	private final WebDriverInterface myInterface;
 	private WebElement myElement;
 	private final WebElement myBaseElement;
 	
 	public SimplePageElement(By by, WebDriverInterface iface) {
 		this(by, iface, null, null);
 	}
 
 	/**
 	 * @param by		The by to find this element. If the element is a SmartWebElement, this by
 	 *                  overrides the by inside the SmartWebElement
 	 * @param iface		The interface on which this element will appear
 	 * @param element   If this is also a SmartWebElement, the element inside the SmartWebElement
 	 *                  is taken.
 	 */
 	public SimplePageElement(By by, WebDriverInterface iface, WebElement element) {
 		this(by, iface, element, null);
 	}
 
 	/**
 	 * @param by		The by to find this element. If the element is a SmartWebElement, this by
 	 *                  overrides the by inside the SmartWebElement
 	 * @param iface		The interface on which this element will appear
 	 * @param element   If this is also a SmartWebElement, the element inside the SmartWebElement
 	 *                  is taken.
 	 * @param baseElement   If set, this element is used as base element for commands like findElement.
 	 */
 	public SimplePageElement(By by, WebDriverInterface iface, WebElement element, WebElement baseElement) {
 		myBy = by;
 		myInterface = iface;
 		myElement = element;
 		myBaseElement = baseElement;
 		if ( element instanceof SmartWebElement ) {
 			// We won't nest the SmartElements, but we do use the specified by, allowing you to overwrite it.
 			SmartWebElement elm = (SmartWebElement) element;
 			myElement = elm.getElement();
 		}
 	}
 
 	public By getBy() {
 		return myBy;
 	}
 
 	public WebDriverInterface getInterface() {
 		return myInterface;
 	}
 
 	public void click() {
 		this.getElement().click();
 	}
 
 	public void submit() {
 		this.getElement().submit();
 	}
 
 	public void sendKeys(CharSequence... keysToSend) {
 //		try {
 			this.getElement().sendKeys(keysToSend);
 //		} catch ( ElementNotVisibleException ignored ) {
 //			this.refreshElement();
 //System.out.println("ElementNotVisibleException caught -> element refreshed");
 //			myElement.sendKeys(keysToSend);
 //		}
 	}
 
 	public void clear() {
 		this.getElement().clear();
 	}
 
 	public String getTagName() {
 		return this.getElement().getTagName();
 	}
 
 	public String getAttribute(String name) {
 		return this.getElement().getAttribute(name);
 	}
 
 	public boolean isSelected() {
 		return this.getElement().isSelected();
 	}
 
 	public boolean isEnabled() {
 		return this.getElement().isEnabled();
 	}
 
 	public String getText() {
 		return this.getElement().getText();
 	}
 
 	public List<WebElement> findElements(By by) {
 		List<WebElement> elements = this.getElement().findElements(by);
 		return new SimpleElementList( by, this.getInterface(), elements, this );
 	}
 
 	public SmartWebElement findElement(By by) {
 		WebElement element = this.getElement().findElement(by);
 		return new SimplePageElement( by, this.getInterface(), element, this );
 	}
 
 	public boolean isDisplayed() {
 		return this.getElement().isDisplayed();
 	}
 
 	public Point getLocation() {
 		return this.getElement().getLocation();
 	}
 
 	public Dimension getSize() {
 		return this.getElement().getSize();
 	}
 
 	public String getCssValue(String propertyName) {
 		return this.getElement().getCssValue(propertyName);
 	}
 
 //	public boolean isPresent() {
 //		// TODO test
 //		WebElement element = this.getElement();
 //		if( element == null ) { return false; }
 //		
 //		try {
 //			element.isEnabled();
 //			return true; //Even if not displayed, it is present
 //		} catch ( StaleElementReferenceException sere ) {
 //			return false;
 //		}
 //	}
 
 	public Coordinates getCoordinates() {
 		WebElement element = this.getElement();
 		if ( element instanceof Locatable ) {
 			return ((Locatable) element).getCoordinates();
 		}
 		return null;
 	}
 
 	public Point getLocationOnScreenOnceScrolledIntoView() {
 		WebElement element = this.getElement();
 		if ( element instanceof Locatable ) {
 			return ((Locatable) element).getLocationOnScreenOnceScrolledIntoView();
 		}
 		return null;
 	}
 
 	public WebDriver getWrappedDriver() {
 		return myInterface.getDriver();
 	}
 
 	public WebElement findElementByXPath(String paramString) {
 		WebElement element = this.getElement();
 		WebElement foundElement = null;
 		if ( element instanceof FindsByXPath ) {
 			foundElement = ((FindsByXPath) element).findElementByXPath(paramString);
 		}
 		return new SimplePageElement( By.xpath(paramString), this.getInterface(), foundElement, this );
 	}
 
 	public List<WebElement> findElementsByXPath(String paramString) {
 		WebElement element = this.getElement();
 		List<WebElement> foundElements = new ArrayList<WebElement>();
 		if ( element instanceof FindsByXPath ) {
 			foundElements = ((FindsByXPath) element).findElementsByXPath(paramString);
 		}
 		return new SimpleElementList( By.xpath(paramString), this.getInterface(), foundElements, this );
 	}
 
 	public WebElement findElementByCssSelector(String paramString) {
 		WebElement element = this.getElement();
 		WebElement foundElement = null;
 		if ( element instanceof FindsByCssSelector ) {
 			foundElement = ((FindsByCssSelector) element).findElementByCssSelector(paramString);
 		}
 		return new SimplePageElement( By.cssSelector(paramString), this.getInterface(), foundElement, this );
 	}
 
 	public List<WebElement> findElementsByCssSelector(String paramString) {
 		WebElement element = this.getElement();
 		List<WebElement> foundElements = new ArrayList<WebElement>();
 		if ( element instanceof FindsByCssSelector ) {
 			foundElements = ((FindsByCssSelector) element).findElementsByCssSelector(paramString);
 		}
 		return new SimpleElementList( By.cssSelector(paramString), this.getInterface(), foundElements, this );
 	}
 
 	public WebElement findElementByClassName(String paramString) {
 		WebElement element = this.getElement();
 		WebElement foundElement = null;
 		if ( element instanceof FindsByClassName ) {
 			foundElement = ((FindsByClassName) element).findElementByClassName(paramString);
 		}
 		return new SimplePageElement( By.className(paramString), this.getInterface(), foundElement, this );
 	}
 
 	public List<WebElement> findElementsByClassName(String paramString) {
 		WebElement element = this.getElement();
 		List<WebElement> foundElements = new ArrayList<WebElement>();
 		if ( element instanceof FindsByClassName ) {
 			foundElements = ((FindsByClassName) element).findElementsByClassName(paramString);
 		}
 		return new SimpleElementList( By.className(paramString), this.getInterface(), foundElements, this );
 	}
 
 	public WebElement findElementByTagName(String paramString) {
 		WebElement element = this.getElement();
 		WebElement foundElement = null;
 		if ( element instanceof FindsByTagName ) {
 			foundElement = ((FindsByTagName) element).findElementByTagName(paramString);
 		}
 		return new SimplePageElement( By.tagName(paramString), this.getInterface(), foundElement, this );
 	}
 
 	public List<WebElement> findElementsByTagName(String paramString) {
 		WebElement element = this.getElement();
 		List<WebElement> foundElements = new ArrayList<WebElement>();
 		if ( element instanceof FindsByTagName ) {
 			foundElements = ((FindsByTagName) element).findElementsByTagName(paramString);
 		}
 		return new SimpleElementList( By.tagName(paramString), this.getInterface(), foundElements, this );
 	}
 
 	public WebElement findElementByName(String paramString) {
 		WebElement element = this.getElement();
 		WebElement foundElement = null;
 		if ( element instanceof FindsByName ) {
 			foundElement = ((FindsByName) element).findElementByName(paramString);
 		}
 		return new SimplePageElement( By.name(paramString), this.getInterface(), foundElement, this );
 	}
 
 	public List<WebElement> findElementsByName(String paramString) {
 		WebElement element = this.getElement();
 		List<WebElement> foundElements = new ArrayList<WebElement>();
 		if ( element instanceof FindsByName ) {
 			foundElements = ((FindsByName) element).findElementsByName(paramString);
 		}
 		return new SimpleElementList( By.name(paramString), this.getInterface(), foundElements, this );
 	}
 
 	public WebElement findElementById(String paramString) {
 		WebElement element = this.getElement();
 		WebElement foundElement = null;
 		if ( element instanceof FindsById ) {
 			foundElement = ((FindsById) element).findElementById(paramString);
 		}
 		return new SimplePageElement( By.id(paramString), this.getInterface(), foundElement, this );
 	}
 
 	public List<WebElement> findElementsById(String paramString) {
 		WebElement element = this.getElement();
 		List<WebElement> foundElements = new ArrayList<WebElement>();
 		if ( element instanceof FindsById ) {
 			foundElements = ((FindsById) element).findElementsById(paramString);
 		}
 		return new SimpleElementList( By.id(paramString), this.getInterface(), foundElements, this );
 	}
 
 	public WebElement findElementByLinkText(String paramString) {
 		WebElement element = this.getElement();
 		WebElement foundElement = null;
 		if ( element instanceof FindsByLinkText ) {
 			foundElement = ((FindsByLinkText) element).findElementByLinkText(paramString);
 		}
 		return new SimplePageElement( By.linkText(paramString), this.getInterface(), foundElement, this );
 	}
 
 	public List<WebElement> findElementsByLinkText(String paramString) {
 		WebElement element = this.getElement();
 		List<WebElement> foundElements = new ArrayList<WebElement>();
 		if ( element instanceof FindsByLinkText ) {
 			foundElements = ((FindsByLinkText) element).findElementsByLinkText(paramString);
 		}
 		return new SimpleElementList( By.linkText(paramString), this.getInterface(), foundElements, this );
 	}
 
 	public WebElement findElementByPartialLinkText(String paramString) {
 		WebElement element = this.getElement();
 		WebElement foundElement = null;
 		if ( element instanceof FindsByLinkText ) {
 			foundElement = ((FindsByLinkText) element).findElementByPartialLinkText(paramString);
 		}
 		return new SimplePageElement( By.partialLinkText(paramString), this.getInterface(), foundElement, this );
 	}
 
 	public List<WebElement> findElementsByPartialLinkText(String paramString) {
 		WebElement element = this.getElement();
 		List<WebElement> foundElements = new ArrayList<WebElement>();
 		if ( element instanceof FindsByLinkText ) {
 			foundElements = ((FindsByLinkText) element).findElementsByPartialLinkText(paramString);
 		}
 		return new SimpleElementList( By.partialLinkText(paramString), this.getInterface(), foundElements, this );
 	}
 
 //	public int getTimeout() {
 //		return 10;
 //	}
 
 	public WebElement getElement() {
 		if (myElement == null) {
 			this.refreshElement();
 		} else {
 			try {
 				myElement.isDisplayed();
 			} catch (NotFoundException ignored ) {
 				// We could have used WebDriverException, but that's too much and would not always be solved by refresh.
 				// If needed we can add other specific Exceptions.
 				this.refreshElement();
 			} catch (ElementNotVisibleException ignored ) {
 				this.refreshElement();
 			} catch (StaleElementReferenceException ignored ) {
 				this.refreshElement();
 			}
 		}
 		return myElement;
 	}
 
 	/**
 	 * @throws Error
 	 */
 	private void refreshElement() throws Error {
 		if ( myBaseElement != null ) {
 			myElement = myBaseElement.findElement(myBy);
 		} else {
 			WebDriver driver = this.getInterface().getDriver();
 			if ( driver == null ) { // should not happen. The interface must make sure it's not null
 				throw new Error( "Element requested, but driver is not yet created: '" + myBy
 								 + "'. Make sure this interface (" + this.getInterface().toString() + ") opens a browser first.");
 			}
 			
 			myElement = driver.findElement(myBy);
 		}
 	}
 }
