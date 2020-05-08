 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.gui.impl.servlets;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.servlet.Servlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.paxle.core.doc.ICommandProfile;
 import org.paxle.core.doc.ICommandProfileManager;
 import org.paxle.core.doc.IDocumentFactory;
 import org.paxle.core.doc.ICommandProfile.LinkFilterMode;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.IServiceManager;
 
 @Component(metatype=false, immediate=true)
 @Service(Servlet.class)
 @Properties({
 	@Property(name="org.paxle.servlet.path", value="/crawler"),
 	@Property(name="org.paxle.servlet.doUserAuth", boolValue=true),
 	@Property(name="org.paxle.servlet.menu", value="%menu.administration/%menu.bundles/%menu.bundles.crawler"), 
 	@Property(name="org.paxle.servlet.menu.icon", value="/resources/images/link_go.png")
 })
 public class CrawlerView extends ALayoutServlet {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 	
 	@Reference(target="(docType=org.paxle.core.doc.ICommandProfile)")
 	protected IDocumentFactory profileFactory;
 	
 	private class UrlTank {
 		/**
 		 * @see org.paxle.data.db.ICommandDB
 		 */
 		private final Object commandDB;
 		
 		/**
 		 * @see org.paxle.filter.robots.IRobotsTxtManager
 		 */
 		private final Object robotsManager;
 		
 		/**
 		 * @see org.paxle.core.norm.IReferenceNormalizer
 		 */
 		private final Object normalizer;
 		
 		/**
 		 * @see org.paxle.filter.robots.IRobotsTxtManager#isDisallowed(String)
 		 */
 		private Method isDisallowed = null;
 		
 		/**
 		 * @see org.paxle.data.db.ICommandDB#isKnown(URI)
 		 */
 		private Method doubleURL = null;
 		
 		/**
 		 * @see org.paxle.data.db.ICommandDB#enqueue(URI, int, int)
 		 */
 		private Method enqueueCommand = null;
 		
 		/**
 		 * @see org.paxle.core.norm.IReferenceNormalizer#normalizeReference(String)
 		 */
 		private Method normalizeReference = null;
 		
 		/**
 		 * The crawling profile to use
 		 */
 		private ICommandProfile profile = null;
 		
 		private HashMap<String,String> errorUrls = null;
 		
 		public UrlTank(final IServiceManager manager, final ICommandProfile profile) throws Exception {
 			if (profile == null) throw new NullPointerException("The command-profile is null");
 			this.profile = profile;
 			
 			this.commandDB = manager.getService("org.paxle.data.db.ICommandDB");
 			if (this.commandDB == null) throw new Exception("Command-DB not available");
 			this.enqueueCommand = this.commandDB.getClass().getMethod("enqueue", URI.class, int.class, int.class);
 			this.doubleURL = this.commandDB.getClass().getMethod("isKnown", URI.class);
 			
 			robotsManager = manager.getService("org.paxle.filter.robots.IRobotsTxtManager");
 			if (robotsManager != null) isDisallowed = robotsManager.getClass().getMethod("isDisallowed", URI.class);
 			
 			normalizer = manager.getService("org.paxle.core.norm.IReferenceNormalizer");
 			if (normalizer == null) throw new Exception("ReferenceNormalizer not available");
 			normalizeReference = normalizer.getClass().getMethod("normalizeReference", String.class);
 		}
 		
 		public void putUrl2Crawl(final String location) throws Exception {
 			final URI uri;
 			// uri = new URI(location);
 			uri = (URI)normalizeReference.invoke(normalizer, location);
 			
 			if (location.trim().length() == 0 || uri == null) {
 				putError(location, "URL '" + location + "' is not valid");
 				return;
 			}
 			
 			// check if URL is blocked by robots-txt
 			if (robotsManager != null && isDisallowed != null) {
 				final Object result = isDisallowed.invoke(robotsManager, uri);
 				if (result != null && ((Boolean)result).booleanValue()) {
 					final String msg = "Not allowed to begin crawling for the URL is blocked by robots.txt";
 					logger.info(msg);
 					putError(location, msg);
 					return;
 				}
 			}
 			
 			// check if URL is already known
 			final Object result = doubleURL.invoke(this.commandDB, uri);
 			if (result != null && ((Boolean)result).booleanValue()) {
 				final String msg = "URL rejected. URL was already crawled.";
 				logger.info(msg);
 				putError(location, msg);
 				return;
 			}
 			
 			// store command into DB
 			logger.info("Initiated crawl of URL '" + uri + "'");
 			this.enqueueCommand.invoke(this.commandDB, uri, Integer.valueOf(this.profile.getOID()), Integer.valueOf(0));
 		}
 		
 		private void putError(final String url, final String err) {
 			if (errorUrls == null)
 				errorUrls = new HashMap<String,String>();
 			errorUrls.put(url, err);
 		}
 		
 		public Map<String,String> getErrorUrls() {
 			return errorUrls;
 		}
 	}
 	
 	@Override
 	protected void fillContext(Context context, HttpServletRequest request) {
 		try {
 			String url = null;
 			if (request.getParameter("crawlSingle") != null) {
 				url = request.getParameter("startURL");
 				
 				// startURL denotes a single URL to crawl entered in an input-field
 				final UrlTank tank = new UrlTank(
 						(IServiceManager)context.get(IServiceManager.SERVICE_MANAGER),
 						this.createProfile(request, context)
 				);
 				tank.putUrl2Crawl(url);
 				context.put("errorUrls", tank.getErrorUrls());
 				
 			} else if (request.getParameter("crawlMass") != null) {
 				url = request.getParameter("startURL2");
 				
 				// startURL2 contains a whole bunch of URLs to crawl entered in a textarea
 				final UrlTank tank = new UrlTank(
 						(IServiceManager)context.get(IServiceManager.SERVICE_MANAGER),
 						this.createProfile(request, context)
 				);
 				final BufferedReader startURLs = new BufferedReader(new StringReader(url));
 				String line;
 				while ((line = startURLs.readLine()) != null) {
 					tank.putUrl2Crawl(line);
 				}
 				context.put("errorUrls", tank.getErrorUrls());
 			} else if (request.getParameter("crawlQuick") != null) {
 				url = request.getParameter("startURL");
 				
 				// startURL denotes a single URL to crawl entered in an input-field
 				final UrlTank tank = new UrlTank(
 						(IServiceManager)context.get(IServiceManager.SERVICE_MANAGER),
 						this.createProfile(request, context)
 				);
 				tank.putUrl2Crawl(url);
 				context.put("errorUrls", tank.getErrorUrls());
 			}
 
 			// default values for input fields
 			context.put("defaultDepth", "3");
 			context.put("defaultName", "Crawl " + this.formatter.format(new Date()));
 		} catch( Exception e ) {
 			logger.error("Error processing request: " + e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * Choosing the template to use 
 	 */
 	@Override
 	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
 		return this.getTemplate("/resources/templates/CrawlerView.vm");
 	}	
 	
 	private void setProfileDepth(HttpServletRequest request, final ICommandProfile profile) {
 		int depth = 0;
 		String depthStr = request.getParameter("crawlDepth");
 		
 		if (depthStr != null) {		
 			try {
 				depth =  Integer.parseInt(depthStr);
 				if (depth < 0) depth = 0;
 			} catch (NumberFormatException e){
 				depth = 0;
 			}
 		}
 		
 		profile.setMaxDepth(depth);
 	}
 	
 	private void setProfileName(HttpServletRequest request, final ICommandProfile profile) {
 		String name = request.getParameter("profileName");
 		if (name == null) name = "Crawl " + this.formatter.format(new Date());
 		profile.setName(name);
 	}
 	
 	private void setProfileLinkFilterMode(HttpServletRequest request, final ICommandProfile profile) {
 		LinkFilterMode filterMode = LinkFilterMode.none;
 		String filterExpression = null;
 		
 		String linkMode = request.getParameter("linkMode");
 		if (linkMode != null && linkMode.equals("specific")) {
 			String linkFilterMode = request.getParameter("linkFilterMode");
 			if (linkFilterMode != null && linkFilterMode.equals("regexp")) {
 				filterMode = LinkFilterMode.regexp;
 				filterExpression = request.getParameter("linkFilterExpressionRegexp");
 				
 				// check if the pattern is valid
 				Pattern.compile(filterExpression);
 			}
 		}
 		
 		profile.setLinkFilterMode(filterMode);
 		profile.setLinkFilterExpression(filterExpression);
 	}
 	
	private void setMiscProperties(HttpServletRequest request, final ICommandProfile profile) {
		final String miscProperties = request.getParameter("miscProperties");
		if (miscProperties != null) {
			String[] lines = miscProperties.split("(\r)\n");
			if (lines != null && lines.length > 0) {
				for (String line : lines) {
					String[] parts = line.split("=");
					if (parts != null && parts.length == 2) {
						final String propertyName = parts[0].trim();
						final String propertyValue = parts[1].trim();
						profile.setProperty(propertyName, propertyValue);
					}
				}
			}
		}
	}
	
 	private ICommandProfile createProfile(HttpServletRequest request, Context context) throws IOException {
 		// getting the service manager
 		final IServiceManager sm = (IServiceManager)context.get(IServiceManager.SERVICE_MANAGER);
 		if (sm == null) throw new NullPointerException("No service-manager found.");
 		
 		// getting the profile-manager
 		final ICommandProfileManager pm = (ICommandProfileManager) sm.getService(ICommandProfileManager.class.getName());
 		if (pm == null) throw new NullPointerException("No profile-manager found.");
 		
 		// create a new profile
 		final ICommandProfile profile = this.profileFactory.createDocument(ICommandProfile.class);
 		this.setProfileDepth(request, profile);
 		this.setProfileName(request, profile);
 		this.setProfileLinkFilterMode(request, profile);
		this.setMiscProperties(request, profile);
 		
 		// store it into the profile-db
 		pm.storeProfile(profile);
 		
 		return profile;
 	}
 }
