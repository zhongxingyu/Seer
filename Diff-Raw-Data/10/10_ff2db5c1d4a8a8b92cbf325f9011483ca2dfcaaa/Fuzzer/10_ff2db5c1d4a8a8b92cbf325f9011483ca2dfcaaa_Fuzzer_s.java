 package se331.projects.fuzzer;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import com.gargoylesoftware.htmlunit.CookieManager;
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.DomNodeList;
 import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.util.Cookie;
 
 public class Fuzzer {
 	
 	private static ArrayList<URL> urlsVisited;
 	private static String baseURL;
 	private static String loginURL;
 	private static HashMap<String, List<String>> urlParameterMap = new HashMap<String, List<String>>();
 	private static Set<Cookie> cookiesSet = new HashSet<Cookie>();
 	private static String username = "";
 	private static String password = "";
 	private static final String guessURLFileLocation = "conf/GuessURLs.txt";
 	private static final String commonPasswordsFileLocation = "conf/CommonPasswords.txt";
 	private static final String[] invalidLoginMessages = {
 		"You supplied an invalid name or password.",
 		"Login failed",
 		"Invalid username or password. Signon failed."
 	};
 	private static enum completenessOption {
 		RANDOM, FULL
 	}
	private static completenessOption completenessMode = completenessOption.RANDOM;
 	private static final String sensitiveDataFileLocation = "conf/sensitiveData.txt";
 	private static ArrayList<String> sensitiveDataList = null;
 	private static HashMap<String, ArrayList<String>> foundSesitiveData = new HashMap<String, ArrayList<String>>();
 	private static long requestInterval = 0;
 
 	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
 		if ( args.length == 1 ) {
 			int arg = Integer.parseInt(args[0]);
 			if ( arg >= 0 ) {
 				requestInterval = Integer.parseInt(args[0]) * 1000;
 				System.out.println(String.format("Request interval set to %s seconds", args[0]));
 			} else {
 				System.out.println("Request interval must be >= 0");
 				System.exit(1);
 			}
 				
 		}
 		fuzzDVWA();
 		fuzzJPetStore();
 		fuzzBodgeIt();
 	}
 	
 	private static void fuzzDVWA() throws MalformedURLException {
 		username = "admin";
 		password = "password";
 		baseURL = "http://127.0.0.1/dvwa/";
 		loginURL = "http://127.0.0.1/dvwa/login.php";
 		
 		runFuzzer();
 	}
 	
 	private static void fuzzJPetStore() throws MalformedURLException {
 		username = "j2ee";
 		password = "j2ee";
 		baseURL = "http://127.0.0.1:8080/jpetstore";
 		loginURL = "http://127.0.0.1:8080/jpetstore/actions/Account.action?signonForm=";
 
 		runFuzzer();
 	}
 	
 	private static void fuzzBodgeIt() throws MalformedURLException {
 		//TODO Set these to working credentials
		username = "";
		password = "";
 		baseURL = "http://127.0.0.1:8080/bodgeit";
 		loginURL = "http://127.0.0.1:8080/bodgeit/login.jsp";
 
 		runFuzzer();
 	}
 	
 	private static void runFuzzer() throws MalformedURLException {
 		WebClient webClient = new WebClient();
 		webClient.setPrintContentOnFailingStatusCode(false);
 		webClient.setJavaScriptEnabled(true);
 		
 		printLinkDiscovery(webClient);
 		
 		System.out.println("\n\n\nBeginning Page Guessing");
 		printPageGuessing(webClient, guessURLFileLocation);
 		System.out.println("\n\n");
 		printCookies(webClient.getCookieManager());
 		System.out.println("\n\nURL Map:");
 		printURLMap();
 		webClient.closeAllWindows();
 		System.out.println("\n\nGuessing Common Passwords");
 		guessCommonPasswords(webClient, "admin", new URL(loginURL));
 		printSensitiveData();
 	}
 	
 	/**
 	 * This prints out all the cookies contained in the CookieManager.
 	 * Call this after visiting a page to get the cookies that were set.
 	 * @author jmd2188
 	 * @param cookieManager	CookieManager instance to retrieve cookies from
 	 */
 	private static void printCookies( CookieManager cookieManager ) {
 		Set<Cookie> cookies = cookieManager.getCookies();
 		System.out.println(String.format("%30s", "").replace(' ', '*'));
 		System.out.println("Cookies:");
 		for ( Cookie cookie : cookies ) {
 			if ( !cookiesSet.contains(cookie) ) {
 				cookiesSet.add(cookie);
 				String key = cookie.getName();
 				String val = cookie.getValue();
 				String domain = cookie.getDomain();
 				String path = cookie.getPath();
 				Date expires = cookie.getExpires();
 				String expiresAt = expires == null ? "Never" : DateFormat.getDateInstance().format( expires );
 				System.out.println(String.format("%-20s = %-32s", key, val));
 				System.out.println(String.format("....for %s on %s, expires %s", path, domain, expiresAt));
 			}
 		}
 		System.out.println(String.format("%30s", "").replace(' ', '*'));
 	}
 	
 	/**
 	 * Prints out all form inputs on a page. Just finds all <input> elements and
 	 *  prints their name and value attributes.
 	 * @author jmd2188
 	 * @param page HtmlPage object to search for inputs
 	 */
 	private static void printFormInputs( HtmlPage page ) {
 		System.out.println(String.format("%30s", "").replace(' ', '*'));
 		DomNodeList<HtmlElement> inputs = page.getElementsByTagName("input");
 		if ( inputs.isEmpty() ) {
 			System.out.println(String.format("No form inputs on %s", page.getUrl().toString()));
 		} else {
 			System.out.println( String.format("Form inputs on %s:", page.getUrl().toString() ));
 			System.out.println(String.format("%-10s %-10s %-10s", "Name", "Type", "Default"));
 			for ( HtmlElement el : inputs ) {
 				HtmlInput input = (HtmlInput)el;
 				String output = String.format("%-10s %-10s %-10s", input.getNameAttribute(), input.getTypeAttribute(), input.getDefaultValue());
 				System.out.println(output);
 			}
 		}
 		System.out.println(String.format("%30s\n", "").replace(' ', '*'));
 	}
 	
 	/**
 	 * Prints all of the links that the crawl reveals.  Does not visit external sites
 	 * @author alh9634
 	 * @param webClient the WebClient instance to use
 	 */
 	private static void printLinkDiscovery( WebClient webClient){
 		System.out.println("\n\n\n\nBegin Link Discovery");
 		urlsVisited = new ArrayList<URL>();
 		URL myURL = null;
 		try {
 			myURL = new URL(baseURL);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		printLinkDiscovery_helper(webClient, myURL);
 	}
 
 	/**
 	 * Helper used to crawl through all of the web pages
 	 * 
 	 * @author alh9634
 	 * @param webClient the WebClient instance to use
 	 * @param myURL the URL visited
 	 */
 	private static void printLinkDiscovery_helper(WebClient webClient, URL myURL) {
 		if(myURL == null){
 			return;
 		}
 		System.out.println("Link discovered through crawl: " + myURL);
 		urlsVisited.add(myURL);
 		ParseURL(myURL);
 		HtmlPage page;
 		try {
 			page = webClient.getPage(myURL);
 			printFormInputs(page);
 			inputIntoFields(page);
 			checkForSensitiveData(page);
 		} catch (Exception e) {
 			System.out.println("External/Invalid URL: " + myURL);
 			return;
 		}
 		List<HtmlAnchor> links = page.getAnchors();
 		for (HtmlAnchor link : links) {
 			URL tempURL = null;
 			try {
 				tempURL = page.getFullyQualifiedUrl(link.getHrefAttribute());
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 				return;
 			}
 			if (!urlsVisited.contains(tempURL) && tempURL.getHost().equals(myURL.getHost())) {
 				try {
 					Thread.sleep(requestInterval);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				printLinkDiscovery_helper(webClient, tempURL);
 			}
 		}
 		
 		if ( isLoginPage(page) ) {
 			URL loggedInURL = doLogin( webClient, page, username, password );
 			if (loggedInURL != null && !urlsVisited.contains(loggedInURL) ) {
 				printLinkDiscovery_helper(webClient, loggedInURL);
 			}
 		}
 	}
 
 	/**
 	 * Goes through the guess URL file and determines which are actually valid
 	 * 
 	 * @author alh9634
 	 * @param webClient the WebClient instance to use
 	 * @param guessURLsLocation file location that contains all of the guess URLs to check
 	 */
 	private static void printPageGuessing(WebClient webClient, String guessURLsLocation) {
 		ArrayList<String> guessURLs = new ArrayList<String>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(guessURLsLocation));
 			String line = br.readLine();
 
 			while (line != null) {
 				guessURLs.add(line);
 				line = br.readLine();
 			}
 			br.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		for(String guess : guessURLs){
 			try {
 				webClient.getPage(baseURL + guess);
 				System.out.println("Successful guess: " + guess);
 			} catch (Exception e){
 				System.out.println("The following guess did not work: " + guess);
 			}
 		}
 	}
 	
 	/**
 	 * Adds the specific url into the URL Parameter Map
 	 * 
 	 * @author alh9634
 	 * @param myURL the URL to parse
 	 */
 	private static void ParseURL(URL myURL){
 		if(!urlParameterMap.keySet().contains(myURL.getPath())){
 			urlParameterMap.put(myURL.getPath(), new ArrayList<String>());
 		}
 		
 		if(myURL.getQuery()==null){
 			return;
 		}
 		String[] params = myURL.getQuery().split("&");
 		for(String param : params){
 			String stripValue = param.substring(0, param.indexOf('='));
 			if(!urlParameterMap.get(myURL.getPath()).contains(stripValue)){
 				urlParameterMap.get(myURL.getPath()).add(stripValue);
 			}
 		}
 	}
 	
 	/**
 	 * Prints the URL Parameter Map
 	 * 
 	 *@author alh9634
 	 */
 	private static void printURLMap(){
 		System.out.println("URL Parameter Map: ");
 		Set<String> keys = urlParameterMap.keySet();
 		for(String key : keys){
 			System.out.println("\tURL: " + key);
 			System.out.println("\tParameters: " + urlParameterMap.get(key));
 		}
 	}
 	
 	/**
 	 * Perform login with given credentials
 	 * @author jmd2188
 	 * 
 	 * @param client WebClient to perform requests with
 	 * @param loginPage page with login form
 	 * @param user username to use
 	 * @param pw password to use
 	 * @return post-login url
 	 */
 	private static URL doLogin(WebClient client, HtmlPage loginPage, String user, String pw) {
 		String userField = "";
 		String pwField = "";
 		String loginBtn = "";
 		HtmlForm loginForm = null;
 		URL nextUrl = null;
 		try {
 			List<HtmlElement> inputs = loginPage.getElementsByTagName("input");
 			for ( HtmlElement el : inputs ) {
 				HtmlInput i = (HtmlInput)el;
 				String type = i.getTypeAttribute();
 				
 				if ( type.equals("text") && i.getNameAttribute().matches(".*user.*") && userField.isEmpty() ) {
 					userField = i.getNameAttribute();
 					//System.out.println("User Field: " + userField);
 				} else if ( type.equals("password") && pwField.isEmpty() ) {
 					pwField = i.getNameAttribute();
 					//System.out.println("Password Field: " + pwField);
 					loginForm = i.getEnclosingFormOrDie();
 				} else if ( type.equals("submit") && i.getValueAttribute().matches("(?i:log.*in.*)") && loginBtn.isEmpty() ) {
 					loginBtn = i.getNameAttribute();
 					//System.out.println("Submit Button: " + loginBtn);
 				}
 			}
 			
 			if ( !pwField.isEmpty() && !userField.isEmpty() && !loginBtn.isEmpty() ) {
 				System.out.println(String.format("Attempting to login to '%s' with username '%s' and password '%s'", loginPage.getUrl(), user, pw));
 				
 				loginForm.getInputByName(userField).setValueAttribute(user);
 				loginForm.getInputByName(pwField).setValueAttribute(pw);
 				Thread.sleep(requestInterval);
 				HtmlPage loggedInPage = loginForm.getInputByName(loginBtn).click();
 				
 				nextUrl = loggedInPage.getUrl();
 				//System.out.println("Post-login URL: " + nextUrl.toString());
 				
 				boolean successfulGuess = true;
 				for(String invalidMsg : invalidLoginMessages){
 					if(loggedInPage.asText().contains(invalidMsg)){
 						successfulGuess = false;
 					}
 				}
 				if(successfulGuess){
 					System.out.println("Successfull password guess");
 				}else{
 					System.out.println("Common password did not work");
 				}
 			} else {
 				System.out.println("Could not find user field, password field, and/or login button");
 			}
 		} catch (FailingHttpStatusCodeException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		return nextUrl;
 	}
 	
 	/**
 	 * Does this page have a login form. Just look for an input of type 'password'
 	 * @author jmd2188
 	 * 
 	 * @param page HtmlPage to check for login form
 	 * @return true if login form exists; otherwise false
 	 */
 	private static boolean isLoginPage( HtmlPage page ) {
 		List<HtmlElement> inputs = page.getElementsByTagName("input");
 		for ( HtmlElement el : inputs ) {
 			if ( el.getAttribute("type").equalsIgnoreCase("password") ) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * @author alh9634
 	 * 
 	 * Using the common passwords file to get a list of common passwords and uses those passwords to guess the specified user's password
 	 * 
 	 * @param webClient The WebClient to perform the web requests with
 	 * @param user The username to test the common password list with
 	 * @param loginPageURL The URL of the log in page
 	 */
 	private static void guessCommonPasswords(WebClient webClient, String user, URL loginPageURL){
 		ArrayList<String> commonPasswords = new ArrayList<String>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(commonPasswordsFileLocation));
 			String line = br.readLine();
 
 			while (line != null) {
 				commonPasswords.add(line);
 				line = br.readLine();
 			}
 			br.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		for(String pw : commonPasswords){
 			try {
 				HtmlPage page = webClient.getPage(loginPageURL);
 				doLogin(webClient, page, user, pw);
 				Thread.sleep(requestInterval);
 			} catch (FailingHttpStatusCodeException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * @author alh9634
 	 * 
 	 * Enters inputs into the input fields and submits to see what the new URL is
 	 * 
 	 * @param page The HTML page for testing input fields
 	 */
 	private static void inputIntoFields(HtmlPage page) {
 		Random r = new Random();
 		int num = r.nextInt(100);
 		int percSkip = 50;
 		
 		DomNodeList<HtmlElement> inputs = page.getElementsByTagName("input");
 		if(completenessMode.equals(completenessOption.RANDOM) && num >= percSkip){
 			System.out.println("Random mode is on and skipping inputting");
 			return;
 		}
 		
 		List<HtmlForm> formElements = page.getForms();
 		if(formElements.size() == 0){
 			System.out.println("No submit element so input fields won't be testeds");
 			return;
 		}
 		System.out.println("Inputting input on page URL: " + page.getUrl());
 		for (int c=0; c<inputs.size(); c++) {
 			num = r.nextInt(100);
			if (completenessMode.equals(completenessOption.RANDOM) && num < percSkip ) {
 				HtmlElement el = inputs.get(c);
 				HtmlInput input = (HtmlInput) el;
 				input.setValueAttribute("test" + c);
 				System.out.println("Adding value for field ID: " + input.getNameAttribute() + " Value: test" + c);
 			}
 		}
 		HtmlPage nextPage;
 		try {
 			Thread.sleep(requestInterval);
 			nextPage = formElements.get(0).click();
 			System.out.println("Now at the URL: " + nextPage.getUrl());
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void loadSensitiveDataList( String sensitiveDataFile ) {
 		sensitiveDataList = new ArrayList<String>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(sensitiveDataFile));
 			String line = br.readLine();
 
 			while (line != null) {
 				line = line.trim();
 				if ( !line.isEmpty() ) {
 					sensitiveDataList.add(line.trim());
 				}
 				line = br.readLine();
 			}
 			br.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.println("Sensitive data list:");
 		for ( String s : sensitiveDataList ) {
 			System.out.println("\t" + s);
 		}
 	}
 	
 	private static void checkForSensitiveData(HtmlPage page) {
 		ArrayList<String> found = new ArrayList<String>();
 		if ( sensitiveDataList == null ) {
 			loadSensitiveDataList( sensitiveDataFileLocation );
 		}
 		
 		for ( String s : sensitiveDataList ) {
 			if ( page.asText().contains(s) ) {
 				found.add(s);
 			}
 		}
 		
 		if ( !foundSesitiveData.containsKey(page.getUrl().toString()) ) {
 			foundSesitiveData.put(page.getUrl().toString(), new ArrayList<String>());
 		}
 		
 		foundSesitiveData.get(page.getUrl().toString()).addAll(found);
 	}
 	
 	private static void printSensitiveData( ) {
 		for ( String url : foundSesitiveData.keySet() ) {
 			ArrayList<String> data = foundSesitiveData.get(url);
 			if ( !data.isEmpty() ) {
 				System.out.println(String.format("Found the following sensitive data in %s", url));
 				for ( String s : foundSesitiveData.get(url) ) {
 					System.out.println("\t" + s);
 				}
 			}
 		}
 	}
 }
