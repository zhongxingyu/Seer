 package site;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import com.gargoylesoftware.htmlunit.CookieManager;
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.ScriptException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
 import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
 import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
 import com.gargoylesoftware.htmlunit.util.Cookie;
 
 import fuzzer.LoginFuzzer;
 
 public class Site {
 	private WebClient webClient;
 
 	private URL baseUrl;
 	private Page basePage;
 	private List<Page> pages;
 	private List<Cookie> cookies;
 	private List<Form> forms;
 	private String pageGuessFilePath;
 	private LoginFuzzer loginFuzzer = null;
 
 	public Site(HtmlPage baseHtmlPage, String pageGuessFilePath)
 			throws MalformedURLException {
 		this.pageGuessFilePath = pageGuessFilePath;
 		this.webClient = baseHtmlPage.getWebClient();
 		this.basePage = new Page(baseHtmlPage);
 		this.baseUrl = baseHtmlPage.getWebResponse().getWebRequest().getUrl();
 		this.pages = new ArrayList<Page>();
 		this.cookies = new ArrayList<Cookie>();
 		this.forms = new ArrayList<Form>();
 		pages.add(basePage);
 	}
 	
 	public void enablePasswordFuzzing(String userNamesFilePath, String passwordsFilePath) throws IOException{
 		this.loginFuzzer = new LoginFuzzer(userNamesFilePath, passwordsFilePath);
 	}
 
 	public void discoverSite() throws MalformedURLException, IOException {
 		discoverPage(basePage);
 
 		guessPages();
 
 		discoverCookies();
 	}
 
 	private void guessPages() throws FileNotFoundException {
 		File file = new File(pageGuessFilePath);
 		Scanner scanner = new Scanner(file);
 		try {
 			while (scanner.hasNextLine()) {
 				try {
 					HtmlPage guessPage = webClient.getPage(baseUrl.toString()
 							+ scanner.nextLine());
 					if (!pages.contains(guessPage)) {
 						Page page = new Page(guessPage);
 						pages.add(page);
 						discoverPage(page);
 					}
 				} catch (FailingHttpStatusCodeException e) {
 					continue; // Failed to find the page
 				} catch (MalformedURLException e) {
 					// TODO These should probably be thrown so that the user can
 					// fix their URL list
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Consider throwing this too
 					e.printStackTrace();
 				}
 			}
 		} finally {
 			scanner.close();
 		}
 	}
 
 	private void discoverCookies() {
 		CookieManager cookieManager = webClient.getCookieManager();
 		for (Cookie cookie : cookieManager.getCookies()) {
 			cookies.add(cookie);
 		}
 	}
 
 	/**
 	 * Authenticate JPetStore username: njf1116 password: genericpassword
 	 */
 	private void authenticateJPetStore(HtmlForm form) throws IOException {
 		final HtmlSubmitInput submit = form.getInputByValue("Login");
 		final HtmlTextInput username = form.getInputByName("username");
 		final HtmlPasswordInput password = form.getInputByName("password");
 
 		if (null == loginFuzzer){
 			username.setValueAttribute("njf1116");
 			password.setValueAttribute("genericpassword");
 	
 			final HtmlPage p = submit.click();
 		}
 		else
 			loginFuzzer.fuzzLogin(submit, username, password);
 	}
 
 	/**
 	 * Authenticate BodgeIt username: njfuschino@gmail.com password:
 	 * genericpassword
 	 */
 	private void authenticateBodgeIt(HtmlForm form) throws IOException {
 		final HtmlSubmitInput submit = form.getInputByValue("Login");
 		final HtmlTextInput username = form.getInputByName("username");
 		final HtmlPasswordInput password = form.getInputByName("password");
 		
 		if (null == loginFuzzer){
 			username.setValueAttribute("nick@nick.com");
 			password.setValueAttribute("asdfasdf");
 	
 			final HtmlPage p = submit.click();
 		}
 		else
 			loginFuzzer.fuzzLogin(submit, username, password);
 	}
 
 	/**
 	 * Authenticate DVWA username: admin password: password
 	 */
 	private void authenticateDVWA(HtmlForm form) throws IOException {
 		
 			final HtmlSubmitInput submit = form.getInputByValue("Login");
 			final HtmlTextInput username = form.getInputByName("username");
 			final HtmlPasswordInput password = form.getInputByName("password");
 
 		if (null == loginFuzzer){
 			username.setValueAttribute("admin");
 			password.setValueAttribute("password");
 	
 			final HtmlPage p = submit.click();
 			
 			discoverPage(new Page(p));
 		}
 		else
 			loginFuzzer.fuzzLogin(submit, username, password);
 	}
 
 	private void authenticateLogin(List<Form> forms) throws IOException {
 		if (this.baseUrl.toString().contains("dvwa")) {
 			for (Form form : forms) {
 				if (form.toString().contains("login")) {
 					authenticateDVWA(form.getForm());
 				}
 			}
 		} else if (this.baseUrl.toString().contains("bodgeit")) {
 			for (Form form : forms) {
 				if (form.toString().contains("login")) {
 					authenticateBodgeIt(form.getForm());
 				}
 			}
 		} else if (this.baseUrl.toString().contains("jpetstore")) {
 			for (Form form : forms) {
 				if (form.toString().contains("signon")
 						&& form.toString().contains("password")) {
 					authenticateJPetStore(form.getForm());
 				}
 			}
 		}
 	}
 
 	private void discoverPage(Page page) throws MalformedURLException,
 			IOException {
 		page.discoverInputs();
 		forms.addAll(page.getForms());
 
 		if (page.getURL().toString().contains("login")
 				|| page.getURL().toString().contains("Account")) {
 			authenticateLogin(page.getForms());
 		}
 
 		List<HtmlAnchor> links = page.getHtmlPage().getAnchors();
 		List<HtmlPage> linkedPages = new ArrayList<HtmlPage>();
 		for (HtmlAnchor link : links) {
 			try {
 				HtmlPage linkedHtmlPage = (HtmlPage) webClient.getPage(page
 						.getHtmlPage().getFullyQualifiedUrl(
 								link.getHrefAttribute()));
 
 				if (isInternalLink(linkedHtmlPage)) {
 					linkedPages.add(linkedHtmlPage);
 				}
 			} catch (FailingHttpStatusCodeException e) {
 				continue;
 			} catch (ScriptException e) {
				System.out.println("SCRIPT EXCEPTION: " + e.getFailingLine() + "\n\t"
 						+ e.getPage().getUrl());
			} catch (UnknownHostException e) {
				//whatever
 			}
 		}
 
 		List<Page> nonDuplicateLinkedPages = new ArrayList<Page>();
 		for (HtmlPage linkedPage : linkedPages) {
 			Page newPage = new Page(linkedPage);
 			if (!pages.contains(newPage)
 					&& !nonDuplicateLinkedPages.contains(newPage)) {
 				nonDuplicateLinkedPages.add(newPage);
 			} else {
 				for (Page existingPage : pages) {
 					if (existingPage.equals(newPage)) {
 						existingPage.addArguments(linkedPage);
 						break;
 					}
 				}
 			}
 		}
 
 		pages.addAll(nonDuplicateLinkedPages);
 
 		for (Page linkedPage : nonDuplicateLinkedPages) {
 			discoverPage(linkedPage);
 		}
 	}
 
 	private boolean isInternalLink(HtmlPage linkedHtmlPage) {
 		return baseUrl.getHost().equals(
 				linkedHtmlPage.getWebResponse().getWebRequest().getUrl()
 						.getHost());
 	}
 
 	public List<Page> getPages() {
 		return pages;
 	}
 
 	public List<Cookie> getCookies() {
 		return cookies;
 	}
 
 	public List<Form> getForms() {
 		return forms;
 	}
 }
