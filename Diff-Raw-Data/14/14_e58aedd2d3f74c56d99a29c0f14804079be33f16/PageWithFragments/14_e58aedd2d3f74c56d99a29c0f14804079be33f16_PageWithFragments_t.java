 package net.thucidides.fragments;
 
 import com.google.common.base.Predicate;
 import com.google.inject.Injector;
 import net.thucidides.fragments.elements.Fragment;
 import net.thucidides.fragments.locators.DefaultElementLocator;
 import net.thucydides.core.pages.PageObject;
 import org.openqa.selenium.*;
 import org.openqa.selenium.internal.WrapsElement;
 
 import javax.annotation.Nullable;
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import static net.thucidides.fragments.utils.XPathFunctions.containstext;
 
 public class PageWithFragments extends PageObject implements IFragmentContext, SearchContext {
 	
 	/**
 	 * Injected with {@link FragmentFactory}
 	 */
	private FragmentFactory fragmentFactory;
 	
 	/**
 	 * Injected with {@link FragmentFactory}
 	 */
	private String contextName;
 	
 	public PageWithFragments(WebDriver webDriver, final Injector injector){
 		super(webDriver, new Predicate<PageObject>() {
             public boolean apply(@Nullable PageObject input) {
                 FragmentFactory.initPageFragments((PageWithFragments)input, injector);
                 return true;
             }
         });
 	}
 	
 	public String getName() { return contextName; }
 	
 	public WebElement findElement(By by) { return getDriver().findElement(by); }
 	
 	public List<WebElement> findElements(By by) { return getDriver().findElements(by); }
 		
 	public Fragment findFragment(By by) {
 		return findFragment(by, Fragment.class);
 	}
 	
 	public <E extends Fragment<?>> E findFragment(By by, Class<E> type) {
 		return findFragment(by, type, by.toString());
 	}
 	
 	public <E extends Fragment<?>> E findFragment(By by, Class<E> type, String name) {
 		return fragmentFactory.createFragment(type, new DefaultElementLocator(getDriver(), by), name);
 	}
 	
 	public List<Fragment> findFragments(By by) {
 		return findFragments(by, Fragment.class);
 	}
 	
 	public <E extends Fragment<?>> List<E> findFragments(By by, Class<E> type) {
 		return findFragments(by, type, by.toString());
 	}
 	
 	public <E extends Fragment<?>> List<E> findFragments(By by, Class<E> type, String name) {
 		return fragmentFactory.createList(type, new DefaultElementLocator(getDriver(), by), name);
 	}
 	
 	public String getCurrentUrl() { return getDriver().getCurrentUrl(); }
 
 	public String getTitle() { return getDriver().getTitle(); }
 
 	public String getPageSource() { return getDriver().getPageSource(); }
 
 	public void navigateBack() { getDriver().navigate().back(); }
 
 	public void navigateForward() {	getDriver().navigate().forward(); }
 	
 	public void navigateTo(String url) { getDriver().navigate().to(url); }
 
 	public void navigateTo(URL url) { getDriver().navigate().to(url); }
 	
 	public void refresh() { getDriver().navigate().refresh(); }
 	
 	public void setImplicitWait(long time, TimeUnit unit){
 		getDriver().manage().timeouts().implicitlyWait(time, unit);
 	}
 	
 	public void addCookie(Cookie cookie) { getDriver().manage().addCookie(cookie); }
 
 	public void deleteCookieNamed(String name) { getDriver().manage().deleteCookieNamed(name); }
 
 	public void deleteCookie(Cookie cookie) { getDriver().manage().deleteCookie(cookie); }
 
 	public void deleteAllCookies() { getDriver().manage().deleteAllCookies(); }
 
 	public Cookie getCookieNamed(String name) { return getDriver().manage().getCookieNamed(name); }
 	
 	public void switchToFrame(WebElement webElement){
 		if(webElement instanceof WrapsElement){
 			webElement = ((WrapsElement) webElement).getWrappedElement();
 		}
 		
 		getDriver().switchTo().frame(webElement);
 	}
 	
 	public void switchToPage(){ getDriver().switchTo().defaultContent(); }
 	
 	public boolean isTextPresent(String text) {
 		return getDriver().getPageSource().toLowerCase().contains(text.toLowerCase());
 	}
 	
 	public boolean isTextDisplayed(String text){
 		return isTextPresent(text) && isElementVisible(By
 				.xpath(String.format("//*[%s]", containstext("text()", text))));
 	}
 	
 	public boolean isValueDisplayed(String value){
 		return isTextPresent(value) && isElementVisible(By
 				.xpath(String.format("//*[%s]", containstext("@value", value))));
 	}
 }
