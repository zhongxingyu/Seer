 /*
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.server.servlets;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.Writer;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.daxplore.presenter.server.ServerPrefixProperties;
 import org.daxplore.presenter.server.ServerTools;
 import org.daxplore.presenter.server.storage.PMF;
 import org.daxplore.presenter.server.storage.QuestionMetadataServerImpl;
 import org.daxplore.presenter.server.storage.SettingItemStore;
 import org.daxplore.presenter.server.storage.StatDataItemStore;
 import org.daxplore.presenter.server.storage.StorageTools;
 import org.daxplore.presenter.server.storage.TextFileStore;
 import org.daxplore.presenter.server.throwable.BadRequestException;
 import org.daxplore.presenter.server.throwable.InternalServerException;
 import org.daxplore.presenter.shared.EmbedDefinition;
 import org.daxplore.presenter.shared.EmbedDefinition.EmbedFlag;
 import org.daxplore.presenter.shared.QueryDefinition;
 import org.daxplore.presenter.shared.QuestionMetadata;
 import org.daxplore.shared.SharedResourceTools;
 
 @SuppressWarnings("serial")
 public class PresenterServlet extends HttpServlet {
 	private static Logger logger = Logger.getLogger(PresenterServlet.class.getName());
 	
 	private static String presenterHtmlTemplate = null;
 	private static String browserSuggestionTemplate = null;
 	private static String printHtmlTemplate = null;
 	private static String embedHtmlTemplate = null;
 	private static String googleAnalyticsTrackingTemplate = null;
 	
 	private HashMap<String, QuestionMetadata> metadataMap = new HashMap<>(); 
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
 		PersistenceManager pm = null;
 		try {
 			// Get input from URL
 			//TODO use better and more stable parsing
 			String prefix = request.getPathInfo();
 			if (prefix!=null && !prefix.isEmpty()) {
 				if(prefix.charAt(0)=='/') {
 					prefix = prefix.substring(1);
 				}
 				if(!prefix.isEmpty() && prefix.charAt(prefix.length()-1)=='/'){
 					prefix = prefix.substring(0, prefix.length()-2);
 				}
 			}
 			String useragent = request.getHeader("user-agent");
 			Cookie[] cookies = request.getCookies();
 			String feature = request.getParameter("f");
			String baseurl = request.getRequestURL().toString();
			baseurl = baseurl.substring(0, baseurl.lastIndexOf("/"));
			baseurl = baseurl.substring(0, baseurl.lastIndexOf("/")+1);
 			
 			// Clean user input
 			if(prefix==null || !SharedResourceTools.isSyntacticallyValidPrefix(prefix)) {
 				throw new BadRequestException("Someone tried to access a syntactically invalid prefix: '" + prefix + "'");
 			}
 			
 			boolean browserSupported = true;
 			double ieversion = ServerTools.getInternetExplorerVersion(useragent);
 			if(useragent == null | (ieversion > 0.0 & ieversion < 8.0)) {
 				browserSupported = false;
 			}
 			
 			boolean ignoreBadBrowser = false;
 			if(cookies != null) {
 				ignoreBadBrowser = ServerTools.ignoreBadBrowser(cookies);
 			}
 			browserSupported |= ignoreBadBrowser;
 			
 			pm = PMF.get().getPersistenceManager();
 			String googleAnalyticsID = SettingItemStore.getProperty(pm, prefix, "adminpanel", "gaID");
 			String gaTemplate = "";
 			if(googleAnalyticsID!=null && !googleAnalyticsID.equals("")) {
 				if (googleAnalyticsTrackingTemplate == null) {
 					try {
 						googleAnalyticsTrackingTemplate = IOUtils.toString(getServletContext().getResourceAsStream("/js/ga-tracking.js"));
 					} catch (IOException e) {
 						throw new InternalServerException("Failed to load the google analytics tracking template", e);
 					}
 				}
 				gaTemplate = MessageFormat.format(googleAnalyticsTrackingTemplate, googleAnalyticsID);
 			}
 			
 			String responseHTML = "";
 			if (!browserSupported) {
 				responseHTML = getUnsupportedBrowserHTML(baseurl, gaTemplate);
 			} else {
 				Locale locale = ServerTools.selectLocale(request, prefix);
 
 				String secondaryFlagText = SettingItemStore.getLocalizedProperty(pm, prefix, "properties/usertexts", locale, "secondary_flag");
 				//TODO handle timepoints properly
 				String timepoint0Text = SettingItemStore.getLocalizedProperty(pm, prefix, "properties/usertexts", locale, "timepoint_0");
 				String timepoint1Text = SettingItemStore.getLocalizedProperty(pm, prefix, "properties/usertexts", locale, "timepoint_1");
 				ServerPrefixProperties prefixProperties = new ServerPrefixProperties(prefix, secondaryFlagText, timepoint0Text, timepoint1Text);
 
 				if (feature!=null && feature.equalsIgnoreCase("embed")) { // embedded chart
 					
 					// TODO clean query string
 					String queryString = request.getParameter("q");
 					responseHTML = getEmbedHTML(pm, prefix, locale, queryString, baseurl, prefixProperties, gaTemplate);
 					
 				} else if (feature!=null && feature.equalsIgnoreCase("print")) { // printer-friendly chart
 					
 					String serverPath = request.getRequestURL().toString();
 					// remove last slash
 					if (serverPath.charAt(serverPath.length() - 1) == '/') {
 						serverPath = serverPath.substring(0, serverPath.length() - 1);
 					}
 					// remove module name
 					serverPath = serverPath.substring(0, serverPath.lastIndexOf("/"));
 					
 					// TODO clean query string
 					String queryString = request.getParameter("q");
 					responseHTML = getPrintHTML(pm, prefix, locale, serverPath, queryString, baseurl, gaTemplate);
 					
 				} else { // standard presenter
 					
 					responseHTML = getPresenterHTML(pm, prefix, locale, baseurl, prefixProperties, gaTemplate);
 					
 				}
 			}
 			
 			response.setContentType("text/html; charset=UTF-8");
 			try (Writer writer = response.getWriter()){
 				writer.write(responseHTML);
 			} catch (IOException e) {
 				throw new InternalServerException("Failed to display presenter servlet", e);
 			}
 		} catch (BadRequestException e) {
 			logger.log(Level.WARNING, e.getMessage(), e);
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 		} catch (InternalServerException e) {
 			logger.log(Level.SEVERE, e.getMessage(), e);
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		} catch (RuntimeException e) {
 			logger.log(Level.SEVERE, "Unexpected exception: " + e.getMessage(), e);
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		} finally {
 			if (pm != null) {
 				pm.close();
 			}
 		}
 	}
 	
 	private String getUnsupportedBrowserHTML(String baseurl, String gaTemplate) throws InternalServerException {
 		if (browserSuggestionTemplate == null) {
 			try {
 				browserSuggestionTemplate = IOUtils.toString(getServletContext().getResourceAsStream("/templates/browser-suggestion.html"));
 			} catch (IOException e) {
 				throw new InternalServerException("Failed to load the html browser suggestion template", e);
 			}
 		}
 		
 		String[] arguments = {
 			baseurl,				// {0}
 			gaTemplate				// {1}
 		};
 		
 		return MessageFormat.format(browserSuggestionTemplate, (Object[])arguments);
 	}
 	
 	private String getPresenterHTML(PersistenceManager pm, String prefix, Locale locale, String baseurl, ServerPrefixProperties properties, String gaTemplate)
 			throws InternalServerException, BadRequestException {
 		
 		String perspectives = "", groups = "", questions = "";
 		perspectives = TextFileStore.getFile(pm, prefix, "meta/perspectives", locale, ".json");
 		questions = TextFileStore.getFile(pm, prefix, "meta/questions", locale, ".json");
 		groups = TextFileStore.getFile(pm, prefix, "meta/groups", locale, ".json");
 		String prefixProperties = properties.toJson().toJSONString();
 		
 		String pageTitle = SettingItemStore.getLocalizedProperty(pm, prefix, "properties/usertexts", locale, "page_title");
 		
 		String[] arguments = {
 			locale.toLanguageTag(), // {0}
 			baseurl,				// {1}
 			pageTitle,				// {2}
 			perspectives,			// {3}
 			questions,				// {4}
 			groups,					// {5}
 			prefixProperties,		// {6}
 			gaTemplate				// {7}
 		};
 		
 		if (presenterHtmlTemplate == null) {
 			try {
 				presenterHtmlTemplate = IOUtils.toString(getServletContext().getResourceAsStream("/templates/presentation.html"));
 			} catch (IOException e) {
 				throw new InternalServerException("Failed to load the embed html template", e);
 			}
 		}
 		
 		return MessageFormat.format(presenterHtmlTemplate, (Object[])arguments);
 	}
 	
 	private String getEmbedHTML(PersistenceManager pm, String prefix, Locale locale,
 			String queryString, String baseurl, ServerPrefixProperties properties, String gaTemplate) throws BadRequestException, InternalServerException {
 		
 		QuestionMetadata questionMetadata;
 		String key = prefix + "_" + locale.toLanguageTag();
 		if(metadataMap.containsKey(key)) {
 			questionMetadata = metadataMap.get(key);
 		} else {
 			String questionText = TextFileStore.getFile(pm, prefix, "meta/questions", locale, ".json");
 			questionMetadata = new QuestionMetadataServerImpl(new StringReader(questionText));
 			metadataMap.put(key, questionMetadata);
 		}
 		
 		QueryDefinition queryDefinition = new QueryDefinition(questionMetadata, queryString);
 		String statItem = StatDataItemStore.getStats(pm, prefix, queryDefinition);
 
 		LinkedList<String> questions = new LinkedList<>();
 		questions.add(queryDefinition.getQuestionID());
 		questions.add(queryDefinition.getPerspectiveID());
 		
 		String pageTitle = SettingItemStore.getLocalizedProperty(pm, prefix, "properties/usertexts", locale, "page_title");
 		
 		String questionString = StorageTools.getQuestionDefinitions(pm, prefix, questions, locale);
 		
 		String prefixProperties = properties.toJson().toJSONString();
 		
 		String[] arguments = {
 			prefix, 				// {0}
 			locale.toLanguageTag(), // {1}
 			pageTitle,				// {2}
 			baseurl, 				// {3}
 			gaTemplate,				// {4}
 			statItem, 				// {5}
 			questionString,			// {6}
 			prefixProperties		// {7}
 		};
 		
 		if (embedHtmlTemplate == null) {
 			try {
 				embedHtmlTemplate = IOUtils.toString(getServletContext().getResourceAsStream("/templates/embed.html"));
 			} catch (IOException e) {
 				throw new InternalServerException("Failed to load the embed html template", e);
 			}
 		}
 		
 		return MessageFormat.format(embedHtmlTemplate, (Object[])arguments);
 	}
 	
 	private String getPrintHTML(PersistenceManager pm, String prefix, Locale locale,
 			String serverPath, String queryString, String baseurl, String gaTemplate) throws InternalServerException, BadRequestException {
 		
 		LinkedList<EmbedFlag> flags = new LinkedList<>();
 		flags.add(EmbedFlag.LEGEND);
 		flags.add(EmbedFlag.TRANSPARENT);
 		flags.add(EmbedFlag.PRINT);
 		String embedDefinition = new EmbedDefinition(flags).getAsString();
 
 		String pageTitle = SettingItemStore.getLocalizedProperty(pm, prefix, "properties/usertexts", locale, "page_title");
 		
 		String[] arguments = {
 			pageTitle,				// {0}
 			baseurl,				// {1}
 			gaTemplate,				// {2}
 			serverPath,				// {3}
 			queryString,			// {4}
 			locale.toLanguageTag(),	// {5}
 			prefix,					// {6}
 			embedDefinition,		// {7}
 			
 		};
 		
 		if (printHtmlTemplate == null) {
 			try {
 				printHtmlTemplate = IOUtils.toString(getServletContext().getResourceAsStream("/templates/print.html"));
 			} catch (IOException e) {
 				throw new InternalServerException("Failed to load print html template", e);
 			}
 		}
 		
 		return MessageFormat.format(printHtmlTemplate, (Object[])arguments);
 	}
 }
