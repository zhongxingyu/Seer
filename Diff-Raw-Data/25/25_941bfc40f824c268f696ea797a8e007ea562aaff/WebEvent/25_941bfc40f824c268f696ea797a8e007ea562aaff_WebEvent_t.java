 package edu.umd.cs.guitar.event;
 
 import java.util.Hashtable;
 import java.util.List;
 
 import org.openqa.selenium.RenderedWebElement;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import edu.umd.cs.guitar.model.GComponent;
 import edu.umd.cs.guitar.model.WebComponent;
 import edu.umd.cs.guitar.model.WebWindowHandler;
 
 public class WebEvent implements GEvent, GEventConfigurable {
 	
 	private boolean noNavigateHrefs;
 
 	@Override
 	public boolean isSupportedBy(GComponent gComponent) {
 		if(gComponent instanceof WebComponent) {
 			WebComponent webComponent = (WebComponent) gComponent;
 			if(webComponent.getElement() instanceof RenderedWebElement) {
 				RenderedWebElement renderedWebElement = (RenderedWebElement) webComponent.getElement();
 				if(renderedWebElement.isDisplayed()) {
 					if("a".equals(webComponent.getElement().getTagName().toLowerCase())) {
 						String href = webComponent.getElement().getAttribute("href");
 						if(href == null) {
 							return false;
 						}
 						if(href.toLowerCase().startsWith("javascript:") || href.startsWith("#")) {
 							// Assume javascript URLs and intrapage anchors don't navigate away.
 							return true;
 						}
 						if(noNavigateHrefs) {
 							// We're not supposed to follow hrefs that navigate away.
 							return false;
 						}
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
	public void perform(GComponent gComponent, List<String> parameters,
 			Hashtable<String, List<String>> optionalData) {
 		perform(gComponent, optionalData);
 	}
 
 	@Override
 	public void perform(GComponent gComponent,
 			Hashtable<String, List<String>> optionalData) {
 		//Should do this by using WebWindowHandler, not use getElement().click(), as clicking the 
 		//link will lead the current window away from the current page, whereas we want to keep both in memory
 		// smcmaster says: This is wrong; for "javascript:" URLs, click() is exactly what we should to.
 		if(gComponent instanceof WebComponent) { 
 			WebElement el = ((WebComponent) gComponent).getElement();
 			
 			if(!(el instanceof RenderedWebElement)) {
 				return;
 			} else {
 				if(!((RenderedWebElement) el).isDisplayed())
 					return;
 			}
 			
 			//Check to see if this is a link of the form: <a href="URL"></a>
 			if(el.getAttribute("href") != null && el.getTagName().equals("a")) { 
 				//New Window is opening, presumably
 				String href = el.getAttribute("href");
 				String finalURL = "";
 				WebDriver driver = ((WebComponent) gComponent).getWindow().getDriver();
 	
 				//Not sure exactly how to do this. It would be nice if there was a robust way already written
 				//Needs to check domain, root links, etc.
 				if(href.startsWith("http://") || href.startsWith("https://"))
 					finalURL = href;
 				else {
 					//Must not be a full url
 					String current = driver.getCurrentUrl();
 					
 					if(href.startsWith("/")) {
 						//Link is a direct reference from root
 						//7 = "http://".length
 						int third = 7 + current.substring(7).indexOf('/');
 						finalURL = current.substring(0, third) + href;
 					} else {
 						//Not from root
 						String end = current.substring(0, current.lastIndexOf('/') + 1);
 						finalURL = end + href;
 					}
 				}
 			
 				//Load a new page with finalURL
 				WebWindowHandler wwh = WebWindowHandler.findByDriver(driver);
 				wwh.createNewWindow(finalURL);
 			}
 		}
 	}
 
 	@Override
 	public void configure(GEventConfiguration config) {
 		noNavigateHrefs = ((WebEventConfiguration) config).isNoNavigateHrefs();
 	}
 }
