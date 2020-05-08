 package org.wicketstuff.examples;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ErrorCollector;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /**
  * This test checks that all bookmarkable pages in Wicket Examples project can be successfully
  * loaded, i.e. the return status code is 2xx and the requested URL is the one that is loaded (no
  * redirects to error page)
  */
 @RunWith(JUnit4.class)
 public class LinkCheckerTest
 {
 	private static final String HOSTNAME = System.getProperty("checker.host", "localhost");
 	
 	private static final String PORT = System.getProperty("checker.port", "8080");
 
 	private static final String CONTEXT_PATH = System.getProperty("checker.context", "wicket-examples");
 	
 	@Rule
 	public ErrorCollector errorCollector = new ErrorCollector();
 	
 	private WebClient webClient;
 	
 	@Before
 	public void before() {
 		webClient = new WebClient(BrowserVersion.FIREFOX_3);
 		
 		// no caching
 		webClient.getCache().setMaxSize(0);
 		
 		webClient.setJavaScriptEnabled(false);
 		webClient.setCssEnabled(false);
 	}
 	
 	@Test
 	public void checkPages() {
 
 		for (final PageDescription pageDescription : getPageDescriptions()) {
 			
 			try {
 				
 				getExamplePage(pageDescription.pageUrl);
 				
 			} catch (final Exception x) {
 				final FailedTestException exception = new FailedTestException(String.format("Cannot not open [%s]: %s ", pageDescription.pageNavigationPath, x.getMessage()), x);
 				errorCollector.addError(exception);
 			}
 		}
 		
 	}
 	
 	private void getExamplePage(String relativePageUrl) 
 		throws FailingHttpStatusCodeException, MalformedURLException, IOException {
 		
 		final StringBuilder url = new StringBuilder();
 		
 		url.append("http://").append(HOSTNAME).append(':').append(PORT).append('/').append(CONTEXT_PATH).append('/').append(relativePageUrl);
	System.err.println("URL: " + url);
 		
 		final Page page = webClient.getPage(url.toString());
 		
 		if (page instanceof HtmlPage) {
 			final HtmlPage examplePage = (HtmlPage) page;
 			
 			final String fullyQualifiedUrl = examplePage.getFullyQualifiedUrl("").toString();
 			
 			assertTrue(String.format("The returned page [%s] is not the expected one [%s]", fullyQualifiedUrl, relativePageUrl), 
 					fullyQualifiedUrl.contains(relativePageUrl));
 			
 			final int httpResponseCode = examplePage.getWebResponse().getStatusCode();
 			assertTrue(String.format("The http response code is bigger than 2xx! Actual value: %d", httpResponseCode), httpResponseCode < 300);
 		} else {
 			throw new IllegalStateException("Unexpected page is returned: " + page.getClass());
 		}
 		
 	}
 	
 	private List<PageDescription> getPageDescriptions() {
 			
 			final List<PageDescription> pageDescriptions = new ArrayList<PageDescription>();
 			
 			pageDescriptions.add(new PageDescription("helloworld", "Hello World"));
 			pageDescriptions.add(new PageDescription("echo", "Echo"));
 			pageDescriptions.add(new PageDescription("forminput", "Form Input"));
 			pageDescriptions.add(new PageDescription("compref", "Component refernce"));
 			pageDescriptions.add(new PageDescription("images", "Images"));
 			
 			pageDescriptions.add(new PageDescription("linkomatic", "Linkomatic"));
 			pageDescriptions.add(new PageDescription("linkomatic/wicket/bookmarkable/org.apache.wicket.examples.linkomatic.Page1", 
 					"Linkomatic BookmarkablePageLink"));
 			pageDescriptions.add(new PageDescription("linkomatic/wicket/bookmarkable/org.apache.wicket.examples.linkomatic.Page2", 
 					"Linkomatic autolinking (<wicket:link>)"));
 			pageDescriptions.add(new PageDescription("linkomatic/wicket/bookmarkable/org.apache.wicket.examples.linkomatic.Page3?bookmarkparameter=aaaa", 
 					"Linkomatic BookmarkablePageLink with parameters"));
 			pageDescriptions.add(new PageDescription("linkomatic/wicket/bookmarkable/org.apache.wicket.examples.linkomatic.Popup", 
 					"Linkomatic open in popup"));
 			
 			pageDescriptions.add(new PageDescription("navomatic", ""));
 			pageDescriptions.add(new PageDescription("pub", "Pub"));
 			pageDescriptions.add(new PageDescription("pub2", "Pub 2"));
 			pageDescriptions.add(new PageDescription("unicodeconverter", "Unicode converter"));
 			
 			pageDescriptions.add(new PageDescription("niceurl", "Nice URLs"));
 			pageDescriptions.add(new PageDescription("niceurl/a/nice/path/to/the/first/page", "Bookmarkable Page 1"));
 			pageDescriptions.add(new PageDescription("niceurl/path/to/page2?param1=-1321348277+-1040203643&param2=-1040203643+-1321348277", 
 					"Bookmarkable Page 2 with parameters"));
			pageDescriptions.add(new PageDescription("niceurl/path/to/page2qpencoded?param1=2013913791+1967182738&param2=1967182738+2013913791", 
 					"Bookmarkable Page 2 with parameters in normal fashion"));
			pageDescriptions.add(new PageDescription("niceurl/my/mounted/package/Page3", 
 					"Page 3 mounted from package"));
			pageDescriptions.add(new PageDescription("niceurl/my/mounted/package/Page4", 
 					"Page 4 mounted from package"));
			pageDescriptions.add(new PageDescription("niceurl/my/mounted/package/Page5?param1=1077839790&param2=92307760", 
 					"Page 4 mounted from package with parameters"));
 			
 			
 			pageDescriptions.add(new PageDescription("ajax/autocomplete", "Ajax Autocomplete"));
 			pageDescriptions.add(new PageDescription("ajax/choice", "Ajax DropDownChoice"));
 			pageDescriptions.add(new PageDescription("ajax/clock", "Ajax Clock"));
 			pageDescriptions.add(new PageDescription("ajax/editable-label", "Ajax Editable Label"));
 			pageDescriptions.add(new PageDescription("ajax/effects", "Ajax effects with Script.aculo.us"));
 			pageDescriptions.add(new PageDescription("ajax/form", "Ajax form submit"));
 			pageDescriptions.add(new PageDescription("ajax/guest-book", "Ajax Guest book"));
 			pageDescriptions.add(new PageDescription("ajax/lazy-loading", "Ajax lazy loading"));
 			pageDescriptions.add(new PageDescription("ajax/links", "Ajax links"));
 			pageDescriptions.add(new PageDescription("ajax/upload", "Ajax upload"));
 			pageDescriptions.add(new PageDescription("ajax/modal-window", "Ajax Modal window"));
 			pageDescriptions.add(new PageDescription("ajax/on-change-ajax-behavior", "Ajax OnChangeBehavior"));
 			pageDescriptions.add(new PageDescription("ajax/pageables", "Ajax pageables"));
 			pageDescriptions.add(new PageDescription("ajax/ratings", "Ajax ratings"));
 			pageDescriptions.add(new PageDescription("ajax/tree/simple", "Ajax Simple tree"));
 			pageDescriptions.add(new PageDescription("ajax/tabbed-panel", "Ajax Tabbed panel"));
 			pageDescriptions.add(new PageDescription("ajax/todo-list", "Ajax Todo List"));
 			pageDescriptions.add(new PageDescription("ajax/world-clock", "Ajax World clock"));
 			
 			pageDescriptions.add(new PageDescription("nested", "Nested components"));
 			
 			pageDescriptions.add(new PageDescription("repeater", "Repeaters"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.RepeatingPage", 
 					"Basic RepeatingView"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.RefreshingPage", 
 					"Basic RefreshingView"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.FormPage", 
 					"Contacts editor"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.SimplePage", 
 					"Simple DataView"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.PagingPage", 
 					"Paging DataView"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.SortingPage", 
 					"Sorting DataView"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.OIRPage", 
 					"DataView with optimized item removal"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.DataGridPage", 
 					"DataGridView"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.DataTablePage", 
 					"DataTable example"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.GridViewPage", 
 					"GridView example"));
 			pageDescriptions.add(new PageDescription("repeater/wicket/bookmarkable/org.apache.wicket.examples.repeater.AjaxDataTablePage", 
 					"AjaxDataTable example"));
 			pageDescriptions.add(new PageDescription("", ""));
 			pageDescriptions.add(new PageDescription("", ""));
 			
 			
 			pageDescriptions.add(new PageDescription("authentication1", "Authentication 1"));
 			pageDescriptions.add(new PageDescription("authentication2", "Authentication 2"));
 			
 			pageDescriptions.add(new PageDescription("authentication3", "Authentication 3"));
 			pageDescriptions.add(new PageDescription("authentication3/wicket/bookmarkable/org.apache.wicket.examples.authentication3.MySignInPage", 
 					"Authentication 3 sign in page"));
 			pageDescriptions.add(new PageDescription("authentication3/wicket/bookmarkable/org.apache.wicket.examples.authentication3.SignOut", 
 					"Authentication 3 sign out page"));
 			
 			pageDescriptions.add(new PageDescription("authorization", "Authorization"));
 			pageDescriptions.add(new PageDescription("authorization/wicket/bookmarkable/org.apache.wicket.examples.authorization.pages.AdminBookmarkablePage", 
 					"Admin page protected with meta data"));
 			pageDescriptions.add(new PageDescription("authorization/wicket/bookmarkable/org.apache.wicket.examples.authorization.pages.PanelsPage", 
 					"Panels page protected with meta data"));
 			pageDescriptions.add(new PageDescription("authorization/wicket/bookmarkable/org.apache.wicket.examples.authorization.pages.AdminAnnotationsBookmarkablePage", 
 					"Admin page protected with annotations"));
 			pageDescriptions.add(new PageDescription("authorization/wicket/bookmarkable/org.apache.wicket.examples.authorization.pages.AnnotationsPanelsPage", 
 					"Panels page protected with annotations"));
 			pageDescriptions.add(new PageDescription("", ""));
 			
 			pageDescriptions.add(new PageDescription("upload/single", "Single upload"));
 			pageDescriptions.add(new PageDescription("upload/multi", "Multi upload"));
 			
 			pageDescriptions.add(new PageDescription("template", "Templates"));
 			pageDescriptions.add(new PageDescription("template/wicket/bookmarkable/org.apache.wicket.examples.template.pageinheritance.Page1", 
 					"Templating with markup inheritance"));
 			pageDescriptions.add(new PageDescription("template/wicket/bookmarkable/org.apache.wicket.examples.template.border.Page1", 
 					"Templating using Border"));
 			
 			pageDescriptions.add(new PageDescription("stateless", "Stateless"));
			pageDescriptions.add(new PageDescription("stateless/foo", "Stateless page"));
 			pageDescriptions.add(new PageDescription("stateless/query", "Stateless query"));
 			pageDescriptions.add(new PageDescription("stateless/mixed", "Stateless mixed"));
 			pageDescriptions.add(new PageDescription("stateless/state-in-url", "Stateless in URL"));
 			pageDescriptions.add(new PageDescription("stateless/statefull", "Stateless go to statefull"));
 			
//			pageDescriptions.add(new PageDescription("hellobrowser/howdy", "Hello browser"));
 			pageDescriptions.add(new PageDescription("frames", "Frames"));
 			pageDescriptions.add(new PageDescription("prototype", "Prototype.js integration"));
 			
 			pageDescriptions.add(new PageDescription("wizard", "Wizards"));
 			
 			pageDescriptions.add(new PageDescription("customresourceloading", "Custom resource loading"));
 			pageDescriptions.add(new PageDescription("customresourceloading/wicket/bookmarkable/org.apache.wicket.examples.customresourceloading.PageFromWebContext", 
 					"Custom resource loading from web context"));
 			pageDescriptions.add(new PageDescription("customresourceloading/wicket/bookmarkable/org.apache.wicket.examples.customresourceloading.AlternativePageFromWebContext", 
 					"Custom resource loading from web context (alternative)"));
 			pageDescriptions.add(new PageDescription("customresourceloading/wicket/bookmarkable/org.apache.wicket.examples.customresourceloading.PageWithCustomLoading", 
 					"Custom resource loading from page member"));
 			pageDescriptions.add(new PageDescription("breadcrumb", "Breadcrumbs"));
 			pageDescriptions.add(new PageDescription("captcha", "Captcha"));
 			pageDescriptions.add(new PageDescription("kitten-captcha", "Kitten captcha"));
 			pageDescriptions.add(new PageDescription("dates", "Wicket datetime"));
//			pageDescriptions.add(new PageDescription("stock", "Stock quotes"));
 			pageDescriptions.add(new PageDescription("guestbook", "Guest book"));
 			pageDescriptions.add(new PageDescription("hangman", "Hangman"));
 			pageDescriptions.add(new PageDescription("hangman/wicket/bookmarkable/org.apache.wicket.examples.hangman.Guess", "Hangman guess"));
 			pageDescriptions.add(new PageDescription("library", "Library app"));
 			pageDescriptions.add(new PageDescription("spring", "Spring integration"));
 			pageDescriptions.add(new PageDescription("guice", "Guice integration"));
 			pageDescriptions.add(new PageDescription("velocity", "Velocity integration"));
 			pageDescriptions.add(new PageDescription("velocity/wicket/bookmarkable/org.apache.wicket.examples.velocity.TemplatePage", "Velocity page"));
 			pageDescriptions.add(new PageDescription("velocity/wicket/bookmarkable/org.apache.wicket.examples.velocity.DynamicPage", "Velocity dynamic page"));
 			
 			return pageDescriptions;
 	}
 
 	/**
 	 * A POJO which brings the information about any Wicket Examples page
 	 */
 	private static final class PageDescription {
 		
 		/**
 		 * The relative path to the tested page.
 		 * Relative to "wicket-examples"
 		 */
 		final String pageUrl;
 		
 		/**
 		 * The user friendly title of the tested page.
 		 */
 		final String pageNavigationPath;
 		
 		/**
 		 * 
 		 * @param pageUrl
 		 * @param navigationPath
 		 */
 		PageDescription(final String pageUrl, final String navigationPath) {
 			this.pageUrl = pageUrl;
 			this.pageNavigationPath = navigationPath;
 		}
 	}
 	
 }
