 package fuzzer;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
 
 /**
  * Provides a consolidated interface for accessing and managing vulnerability
  * and attack surface information for a specific web site (i.e. all of the
  * URLs/web pages which have the same base URL).
  * 
  * @author Eric Newman (edn6266)
  * @author Ross Kahn (rtk1865) 
  * @author Timothy Heard (tjh2430)
  */
 public class SiteInformationManager
 {
 	private String baseUrl;
 	private Map<String, WebPage> webPages;
 	public FuzzerData configurations;
 	
 	private List<String> vectors, sensitiveData, passwordDictionary, 
 								sanitationInputs, pageGuesses;
 	
 	/*
 	 * Used for logging potential vulnerabilities found by the fuzzer.
 	 */
 	private StringBuilder vulnerabilityReport;
 	
 	/**
 	 * Private constructor for creating a SiteInformationManager for the site 
 	 * at the given URL.  
 	 */
 	private SiteInformationManager()
 	{
 		this.webPages = new HashMap<String, WebPage>();
 		this.vulnerabilityReport = new StringBuilder();
 	}
 	
 	/**
 	 * Initiate attack surface discovery using the currently loaded configurations.
 	 * Attempts to find all possible inputs for every page on the site by following
 	 * links to pages with the same base URL as well as attempting to access the 
 	 * pages in the current pageGuesses list (taken from the configuration data 
 	 * file). If any pages requiring authentication are encountered the set of 
 	 * default login credentials provided in the configuration files will be used,
 	 * unless password guessing is currently turned on, in which case the application
 	 * will attempt to guess the password for the username provided in the 
 	 * configuration file. If no username was provided in the configuration file or
 	 * if a username was provided but no password was provided and password guessing
 	 * is turned off then authentication will not be attempted and password fields
 	 * will be treated the same as any other input.
 	 */
 	public void performDiscovery() 
 		throws FailingHttpStatusCodeException, MalformedURLException, IOException
 	{
 		// Note: It is important that page guessing be attempted after conventional
 		// page discovery has been performed because otherwise the page guessing
 		// method may inaccurately report that there are no links to a page when
 		// in fact links do exist, they just haven't been explored yet.
 		performDiscoveryOnUrl(baseUrl);
 		performPageGuessing();
 	}
 	
 	private void doTimeGap(){
 		// Implement time gap in between each page to be discovered
 		long sleeptime = configurations.timeGap();
 		try {
 			Thread.sleep(sleeptime);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Attempts to enumerate the attack surface for the web page at the given URL. 
 	 */
 	private void performDiscoveryOnUrl(String pageUrl) 
 		throws FailingHttpStatusCodeException, MalformedURLException, IOException
 	{
 		WebPage webPage = WebPage.performDiscoveryOnPage(pageUrl);
 		if(webPage == null)
 		{
 			return;
 		}
 		
 		doTimeGap();
 		
 		webPages.put(pageUrl, webPage);
 		
 		String username = configurations.getUsername();
 		if(webPage.requiresAuthentication() && 
 		   username != null && username.trim().length() > 0 &&
 		   configurations.getAuthenticationSuccessString() != null)
 		{
 			HtmlPage authenticationPage = null;
 
 			List<WebForm> authenticationForms = webPage.getFormsWithAuthentication();
 			String password; 
 					
 			if(configurations.passwordGuessingIsOn())
 			{
 				password = null;
 				
 				for(WebForm form: authenticationForms)
 				{
 					for(String word: passwordDictionary)
 					{
 						authenticationPage = (HtmlPage) webPage.attemptAuthentication(form, username, word);
 						checkAuthenticationPage(authenticationPage, pageUrl, username, word);
 					}
 				}
 			}
 			else
 			{
 				password = configurations.getPassword();
 				
 				for(WebForm form: authenticationForms)
 				{
 					authenticationPage = (HtmlPage) webPage.attemptAuthentication(form, username, password);
 					checkAuthenticationPage(authenticationPage, pageUrl, username, password);
 				}
 			}
 		}
 		
 		performDiscoveryOnLinks(webPage, false);
 	}
 	
 	/**
 	 * Performs attack surface discovery on all the pages that are linked to from 
 	 * the given web page which are a part of the same web site (i.e. start with the
 	 * same base URL). If a value of true is passed into the logNewLinks parameter
 	 * then any links which are found on this page which have not already been 
 	 * encountered will be logged (this is used when following links from a unlinked
 	 * page discovered through page guessing).   
 	 */
 	private void performDiscoveryOnLinks(WebPage webPage, boolean logNewLinks)
 		throws FailingHttpStatusCodeException, IOException
 	{
 		List<HtmlAnchor> links = webPage.getPage().getAnchors(); 
 		for(HtmlAnchor link: links)
 		{
 			String linkUrl = webPage.getPage().getFullyQualifiedUrl(link.getHrefAttribute()).toString();
 
 			// If the page URL is not a part of the site being fuzzed of if this 
 			// page URL has already been discovered then nothing needs to be done
 			if(webPages.containsKey(linkUrl) || !linkUrl.startsWith(baseUrl))
 			{
 				continue;
 			}
 						
 			if(logNewLinks)
 			{
 				vulnerabilityReport.append("New link found: " + linkUrl + "\n\n");
 			}
 			
 			performDiscoveryOnUrl(linkUrl);			
 		}
 	}
 	
 	private void checkAuthenticationPage(HtmlPage authenticationPage, String pageUrl, String username, String password)
 		throws FailingHttpStatusCodeException, MalformedURLException, IOException
 	{
 		if(authenticationPage != null && authenticationPage.asText() != null &&
 		   authenticationPage.asText().contains(configurations.getAuthenticationSuccessString()))
 		{
 			// Records that this was a successful combination
 			vulnerabilityReport.append("Successful authentication with username \"" + 
 					username + "\" and password \"" + password + "\" on page at " + 
 					pageUrl + "\n\n");
 			
 			String authenticationPageUrl = authenticationPage.getUrl().toString();
 			if(!webPages.containsKey(authenticationPageUrl))
 			{
 				WebPage discoveredPage = new WebPage(authenticationPage);
 				webPages.put(authenticationPageUrl, discoveredPage);
 				
 				// Since the page reached after performing authentication has not
 				// already been encountered, perform discovery from the page 
 				List<HtmlAnchor> links = discoveredPage.getPage().getAnchors(); 
 				for(HtmlAnchor link: links)
 				{
 					String linkUrl = discoveredPage.getPage().getFullyQualifiedUrl(link.getHrefAttribute()).toString();
 
 					// If the page URL is not a part of the site being fuzzed of if this 
 					// page URL has already been discovered then nothing needs to be done
 					if(webPages.containsKey(linkUrl) || !linkUrl.startsWith(baseUrl))
 					{
 						continue;
 					}
 								
 					performDiscoveryOnUrl(linkUrl);			
 				}
 			}
 		}
 		else
 		{
 			// Records that this was not a successful combination
 			vulnerabilityReport.append("Unable to authenticate with username \"" + 
 				username + "\" and password \"" + password + "\" on page at " + 
 				pageUrl + "\n\n");
 		}
 	}
 	
 	private void performPageGuessing()
 		throws FailingHttpStatusCodeException, MalformedURLException, IOException
 	{
 		for(String pageGuess: pageGuesses)
 		{
 			String linkUrl;
 			
 			if(baseUrl.endsWith("/"))
 			{
 				linkUrl = baseUrl + pageGuess;
 			}
 			else
 			{
 				linkUrl = baseUrl + "/" + pageGuess;
 			}
 			
 			// If this page URL has already been discovered then nothing needs to
 			// be done
 			if(webPages.containsKey(linkUrl))
 			{
 				continue;
 			}
 			
 			// Checks to see if the current guess URL is actually a valid URL
 			// and if so, the fact that an unlinked page was discovered for the
 			// site is logged and then discovery is performed from this page 
 			// (i.e. any links on the page are followed), logging any pages which can 
 			// only be reached from this unlinked page (i.e. any previously 
 			// undiscovered pages which are found by following links on the newly 
 			// found page).
 			if(urlExists(linkUrl))
 			{
 				vulnerabilityReport.append("Page guessing found an unlinked page at " +	linkUrl);
 				
 				WebPage webPage = WebPage.performDiscoveryOnPage(linkUrl);
 				performDiscoveryOnLinks(webPage, true);
 			}
 			doTimeGap();
 		}
 	}
 	
 	public Set<String> getSiteUrls()
 	{
 		return webPages.keySet();
 	}
 	
 	public String getBaseUrl()
 	{
 		return baseUrl;
 	}
 	
 	public WebPage getPage(String url)
 	{
 		return webPages.get(url);
 	}
 
 	public boolean reconfigureAndFuzz(String configurationFileName)
 	{
 		try
 		{
 			if(loadConfigurations(configurationFileName))
 			{
 				performDiscovery();
 				performFuzzing();
 				return true;
 			}
 		} 
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Loads the configuration data contained in the configuration file with the given
 	 * file name.
 	 */
 	public boolean loadConfigurations(String configurationFileName)
 	{
 		configurations = new FuzzerData();
 		
 		try 
 		{
 			Scanner inputScanner = new Scanner(new File(configurationFileName));
 			StringTokenizer tokenizer;
 			String nextToken;
 			
 			while(inputScanner.hasNextLine())
 			{
 				tokenizer = new StringTokenizer(inputScanner.nextLine());
 				
 				nextToken = tokenizer.nextToken();
 				
 				if(nextToken.equals("app_data_file:"))
 				{
 					configurations.setDataFileName(tokenizer.nextToken());
 				}
 				else if(nextToken.equals("username:"))
 				{
 					configurations.setUsername(tokenizer.nextToken().replaceAll(" ", "\0"));
 				}
 				else if(nextToken.equals("password:"))
 				{
 					configurations.setPassword(tokenizer.nextToken().replaceAll(" ", "\0"));
 				}
 				else if(nextToken.equals("password_guessing:"))
 				{
 					String guessing = tokenizer.nextToken();
 					if((guessing.equalsIgnoreCase("on")))
 					{
 						configurations.setPasswordGuessing(true);
 					}
 					else if((guessing.equalsIgnoreCase("off")))
 					{
 						configurations.setPasswordGuessing(false);
 					}
 				}
 				else if(nextToken.equals("authentication_success_string:"))
 				{
 					String authenticationSuccessString = "";
 					while(tokenizer.hasMoreTokens())
 					{
 						authenticationSuccessString = authenticationSuccessString.concat(" " + tokenizer.nextToken());
 					}
 					
 					authenticationSuccessString = authenticationSuccessString.trim();
 					if(authenticationSuccessString.length() > 0)
 					{
 						configurations.setAuthenticationSuccessString(authenticationSuccessString);
 					}
 					else
 					{
 						// If no authentication success string is provided that
 						// configuration field is set to null, in which case 
 						// authenticaiton will not be attempted since there would
 						// be no way to determine if authentication was successful
 						configurations.setAuthenticationSuccessString(null);
 					}
 				}
 				else if(nextToken.equals("site_url:"))
 				{
 					baseUrl = tokenizer.nextToken();
 					
 					boolean urlExists = urlExists(baseUrl);
 					doTimeGap();
 					if(baseUrl == null || !urlExists)
 					{
 						return false;
 					}
 				}
 				else if(nextToken.equals("time_gap:"))
 				{
 					configurations.setTimeGap(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(nextToken.equals("completeness:"))
 				{
 					String complete = tokenizer.nextToken();
 					
 					if(complete == null)
 					{
 						//Defaults to full completeness
 						configurations.setCompleteness(100); 
 					}
 					else
 					{
 						int completeness = Integer.parseInt(complete);
 						
 						if(completeness <= 0 || completeness > 100)
 						{
 							System.out.println("Configuration error in " + configurationFileName +
 									": completeness must be an integer value between 1 and 100 inclusively");
 						}
 						
 						configurations.setCompleteness(completeness);
 					}
 				}
 				else
 				{
 					System.out.println("Unknown configuration option encountered: " + nextToken);
 				}
 			}
 			
 			inputScanner.close();
 		} 
 		catch (FileNotFoundException e) 
 		{
 			System.out.println("Configuration File Not Found");
 			e.printStackTrace();
 			return false;
 		}
 		catch (NumberFormatException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 
 		if(configurations.getDataFileName() != null && 
 		   !configurations.getDataFileName().isEmpty())
 		{
 			return loadData();
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Loads all the data from the data file into respective data structures.
 	 */
 	public boolean loadData()
 	{
 		vectors = new ArrayList<String>();
 		sensitiveData = new ArrayList<String>();
 		passwordDictionary = new ArrayList<String>();
 		sanitationInputs = new ArrayList<String>();
 		pageGuesses = new ArrayList<String>();
 		
 		FileInputStream fstream;
 		try 
 		{
 			fstream = new FileInputStream(configurations.getDataFileName());
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String line;
 			
 			while((line = br.readLine()) != null){
 				if(line.equals("external fuzz vectors:"))
 				{
 					vectors = readDataSectionIntoList(vectors, br);
 				}
 				else if(line.equals("sensitive data:"))
 				{
 					sensitiveData = readDataSectionIntoList(sensitiveData, br);
 				}
 				else if(line.equals("password dictionary:"))
 				{
 					passwordDictionary = readDataSectionIntoList(passwordDictionary, br);
 				}
 				else if(line.equals("sanitization checking inputs:"))
 				{
 					sanitationInputs = readDataSectionIntoList(sanitationInputs, br);
 				}
 				else if(line.equals("page guessing:"))
 				{
 					pageGuesses = readDataSectionIntoList(pageGuesses, br);
 				}
 			}
 			
 			br.close();
 			
 		} 
 		catch (FileNotFoundException e) 
 		{
 			System.out.println("Data File Not Found");
 			e.printStackTrace();
 			return false;
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private List<String> readDataSectionIntoList(List<String> dataList, BufferedReader br)
 		throws IOException
 	{
 		String line;
 		while((line = br.readLine()) != null)
 		{
 			if(line.trim().length() == 0)
 			{
 				// If an empty line is encountered then continue 
 				// processing the file (the next line read in will
 				// be the header for the next data section
 				break;
 			}
 			
 			dataList.add(line);
 		}
 		
 		return dataList;
 	}
 	
 	/**
 	 * Performs fuzz testing on the currently discovered attack surface using 
 	 * the currently loaded configurations and logs the results.
 	 */
 	public void performFuzzing()
 		throws IOException
 	{
 		for(String pageName: webPages.keySet())
 		{
 			WebPage page = webPages.get(pageName);
 			List<WebForm> forms = page.getForms();
 
 			Set<Integer> formIndices;
 			if(configurations.completeness() == 100)
 			{
 				// Adds all of the indices since every form is going to be tested
 				formIndices = new HashSet<Integer>();
 				for(int i = 0; i < forms.size(); i++)
 				{
 					formIndices.add(i);	
 				}
 			}
 			else
 			{
 				formIndices = generateTestIndices(forms.size(), configurations.completeness());
 			}
 			
 			for(Integer index: formIndices)
 			{
 				WebForm form = forms.get(index);
 				HtmlSubmitInput submitField = form.getSubmitField();
 				if(submitField == null)
 				{
 					// If the form cannot be submitted then nothing can be done
 					// with this form
 					break;
 				}
 				
 				Page resultingPage;
 				String pageAsString;
 				for(HtmlElement input: form.getInputs())
 				{
 					for(String vector: vectors)
 					{
 						String sensitiveDataFound = "";
 						input.type(vector);
 						
 						// Submits the form
 						resultingPage = submitField.click();
 						pageAsString = resultingPage.getWebResponse().getContentAsString();
 						
 						// Checks for sensitive data in response page
 						for(String s: sensitiveData)
 						{
 							if(pageAsString.contains(s))
 							{
 								sensitiveDataFound.concat(s + ", ");
 							}
 						}
 						
 						if(!sensitiveDataFound.equals(""))
 						{
 						
 							if(form.getForm().getId() == null && input.getId() == null)
 							{
 								vulnerabilityReport.append("Page: " + pageName + "| Form: ID-less" + " | Input: ID=less\n" +
 										"	Sensitive Data Found: " + sensitiveDataFound + "\n");
 							}
 							else if(form.getForm().getId() == null)
 							{
 								vulnerabilityReport.append("Page: " + pageName + "| Form: ID-less" + " | Input: " + input.getId() + "\n" +
 										"	Sensitive Data Found: " + sensitiveDataFound + "\n");
 							}
 							else if(input.getId() == null)
 							{
 								vulnerabilityReport.append("Page: " + pageName + "| Form: " + form.getForm().getId() + " | Input: ID=less\n" +
 										"	Sensitive Data Found: " + sensitiveDataFound + "\n");
 							}
 							else
 							{
 								vulnerabilityReport.append("Page: " + pageName + "| Form: " + form.getForm().getId() + " | Input: " + input.getId() + "\n" +
 										"	Sensitive Data Found: " + sensitiveDataFound + "\n");
 							}
 						}
 					}
 					
 					String unsanitizedInputs = "";
 					
 					for(String inputToSanitize: sanitationInputs)
 					{
 						// Submit input and then check the url params to ensure
 						// that the input has been changed
 						input.type(inputToSanitize);
 						
 						// Submits the form
 						resultingPage = submitField.click();
 						
 						// Checks if the input was sanitized (changed) at all
 						if(resultingPage.getUrl().getQuery().contains(inputToSanitize))
 						{
 							unsanitizedInputs.concat(inputToSanitize + ", ");
 						}
 						
 					}
 					
 					if(!unsanitizedInputs.equals(""))
 					{
 					
 						if(form.getForm().getId() == null && input.getId() == null)
 						{
 							vulnerabilityReport.append("Page: " + pageName + "| Form: ID-less" + " | Input: ID=less\n" +
 									"	Unsanitized Inputs Found: " + unsanitizedInputs + "\n");
 						}
 						else if(form.getForm().getId() == null)
 						{
 							vulnerabilityReport.append("Page: " + pageName + "| Form: ID-less" + " | Input: " + input.getId() + "\n" +
 									"	Unsanitized Inputs Found: " + unsanitizedInputs + "\n");
 						}
 						else if(input.getId() == null)
 						{
 							vulnerabilityReport.append("Page: " + pageName + "| Form: " + form.getForm().getId() + " | Input: ID=less\n" +
 									"	Unsanitized Inputs Found: " + unsanitizedInputs + "\n");
 						}
 						else
 						{
 							vulnerabilityReport.append("Page: " + pageName + "| Form: " + form.getForm().getId() + " | Input: " + input.getId() + "\n" +
 									"	Unsanitized Inputs Found: " + unsanitizedInputs + "\n");
 						}
 					}
 					
 				}
 			}
 		}
 	}
 	
 	private Set<Integer> generateTestIndices(int total, int completeness)
 	{
 		Set<Integer> testIndices = new HashSet<Integer>();
 		Random random = new Random();
 		
 		int index;
 		for(int i = 0; i < total; i++)
 		{
 			do
 			{
 				index = random.nextInt(total);
 			}
 			while(testIndices.contains(index));
 		}
 		
 		return testIndices;
 	}
 
 	/**
 	 * Writes a detailed report on the vulnerability and attack surface 
 	 * information which has been discovered for the site being examined.
 	 *
 	 * @param outputStream	The PrintStream to write the report to
 	 */
 	public void writeReport(PrintStream outputStream)
 	{
 		outputStream.println("********************************************************************************");
 		outputStream.println("Report for site based at " + baseUrl + "\n");
 		outputStream.println("Discovered Attack Surface:\n");
 		
 		for(String url: webPages.keySet())
 		{
 			outputStream.println("--------------------------------------------------------------------------------");
 			outputStream.println("Page: " + url + "\n");
 			webPages.get(url).writeReport(outputStream);
 		}
 		
 		outputStream.println("--------------------------------------------------------------------------------");
 		outputStream.println("--------------------------------------------------------------------------------");
 		outputStream.println("--------------------------------------------------------------------------------");
 		
 		outputStream.println("Fuzzing Results:\n");
 		outputStream.println("--------------------------------------------------------------------------------");
 		outputStream.println(vulnerabilityReport.toString());
 		outputStream.println("--------------------------------------------------------------------------------");
 		outputStream.println("********************************************************************************");
 	}
 
 	/**
 	 * Prints the current configurations to standard out (for debuggin purposes).
 	 */
 	public void printConfigurations()
 	{
 		System.out.println("\nLoaded Configurations:\n");
 		System.out.println("Seach Complete: " + configurations.completeness());
 		System.out.println("Time Gap: " + configurations.timeGap());
 		System.out.println("Username: " + configurations.getUsername());
 		System.out.println("Password: " + configurations.getPassword());
 		System.out.println("Password Guessing Is On: " + configurations.passwordGuessingIsOn());
 		System.out.println("Data File Name: " + configurations.getDataFileName());
 		System.out.println("Authentication Success String: " + configurations.getAuthenticationSuccessString());
 		System.out.println();
 	}
 	
 	/**
 	 * Initializes and returns a new SiteInformationManager using the configurations contained in 
 	 * the configuration file with the given file name after performing attack surface discovery. 
 	 */
 	public static SiteInformationManager initSiteInformationManager(String configurationFileName)
 		throws FailingHttpStatusCodeException, MalformedURLException, IOException
 	{
 		SiteInformationManager informationManager = new SiteInformationManager();
 		informationManager.loadConfigurations(configurationFileName);
 		informationManager.performDiscovery();
 		return informationManager;
 	}
 	
 	/**
 	 * Initializes and returns a new SiteInformationManager using the configurations contained in 
 	 * the configuration file with the given file name after performing attack surface discovery
 	 * and then fuzzing the discovered attack surface using the loaded configurations. 
 	 */
 	public static SiteInformationManager loadConfigurationAndFuzz(String configurationFileName)
 		throws FailingHttpStatusCodeException, MalformedURLException, IOException
 	{
 		SiteInformationManager informationManager = new SiteInformationManager();
 		
 		if(!informationManager.loadConfigurations(configurationFileName))
 		{
 			return null;
 		}
 		
 		informationManager.performDiscovery();
 		informationManager.performFuzzing();
 		return informationManager;
 	}
 	
 	public static String getBaseUrl(String url)
 	{
 		if(url == null || url.isEmpty())
 		{
 			
 			return null;
 		}
 		
 		int domainStart = url.indexOf("://");
 		
 		// If the pattern "://" cannot be found in the given URL string or if
 		// that pattern only occurs at the end of the string then no base URL
 		// can be extracted
 		if(domainStart == -1 || (domainStart + 1) >= url.length())
 		{
 			return null;
 		}		
 		
 		int baseUrlEnd = url.indexOf("/", (domainStart + 1));
 		
 		// If the given URL string does not contain any more forward slashes
 		// after the initial two for "http://" or "https://" then the URL is
 		// already in its base form
 		if(baseUrlEnd == -1)
 		{
 			return url;
 		}
 		
 		return url.substring(0, baseUrlEnd);
 	}
 	
 	/**
 	 * Method for checking whether or not a given URL exists on the web.
 	 * Taken from a response on stackoverflow 
 	 * (http://stackoverflow.com/questions/4177864/checking-a-url-exist-or-not) 
 	 */
 	public static boolean urlExists(String url)
 	{
 	    try {
 	      HttpURLConnection.setFollowRedirects(false);
 	      HttpURLConnection con =
 	         (HttpURLConnection) new URL(url).openConnection();
	      con.setInstanceFollowRedirects(false);
 	      con.setRequestMethod("HEAD");
 	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
 	    }
 	    catch (Exception e) {
 	       return false;
 	    }
 	}
 }
