 /**
  * Copyright (c) 2007 - 2013 www.Abiss.gr
  *
  * This file is part of Calipso, a software platform by www.Abiss.gr.
  *
  * Calipso is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Calipso is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with Calipso. If not, see http://www.gnu.org/licenses/agpl.html
  */
 package gr.abiss.calipso;
 
 import java.util.EnumSet;
 
 import javax.servlet.DispatcherType;
 import javax.servlet.FilterRegistration;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRegistration;
 
 import org.apache.commons.configuration.CompositeConfiguration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.h2.server.web.WebServlet;
 import org.springframework.web.WebApplicationInitializer;
 import org.springframework.web.context.ContextLoaderListener;
 import org.springframework.web.context.support.XmlWebApplicationContext;
 import org.springframework.web.filter.CharacterEncodingFilter;
 import org.springframework.web.filter.DelegatingFilterProxy;
 import org.springframework.web.servlet.DispatcherServlet;
 import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
 
 /**
  * This class replaces the "old" web.xml and is automatically scanned at the application startup
  */
 public class WebAppInitializer implements WebApplicationInitializer {
 
     @Override
     public void onStartup(ServletContext servletContext) throws ServletException {
 		// Load config properties
 		CompositeConfiguration config = new CompositeConfiguration();
 		try {
 			config.addConfiguration(new PropertiesConfiguration(
 					"calipso.properties"));
 			config.addConfiguration(new PropertiesConfiguration(
 					"calipso.defaults.properties"));
 		} catch (ConfigurationException e) {
 			throw new RuntimeException("Failed to load configuration", e);
 		}
 
 		// Configure cookie security
 		boolean secureCookies = config.getBoolean("calipso.cookies.secure",
 				false);
 		boolean httpOnly = config.getBoolean("calipso.cookies.httpOnly", false);
 		servletContext.getSessionCookieConfig().setHttpOnly(httpOnly);
 		servletContext.getSessionCookieConfig().setSecure(secureCookies);
 		// LOGGER.info("Using secure cookies: " + secureCookies +
 		// ", HTTP only: "
 		// + httpOnly);
 
 		// utf-8
 		FilterRegistration.Dynamic encodingFilter = servletContext.addFilter(
 				"CharacterEncodingFilter", CharacterEncodingFilter.class);
 		encodingFilter.setInitParameter("encoding", "UTF-8");
 		encodingFilter.setInitParameter("forceEncoding", "true");
 		encodingFilter.addMappingForUrlPatterns(
 				(EnumSet.of(DispatcherType.REQUEST)), true, "/*");
 
 		// URL rewrite filter
 		FilterRegistration.Dynamic urlrewriteFilter = servletContext.addFilter(
 				"UrlRewriteFilter", UrlRewriteFilter.class);
 		urlrewriteFilter.addMappingForUrlPatterns(
 				(EnumSet.of(DispatcherType.REQUEST)), true, "/*");
 		// urlrewriteFilter.setInitParameter("logLevel", "DEBUG");
 
 		// // Convert JSONP requests (i.e. HTTP GETs) to proper REST methods
 		// FilterRegistration.Dynamic jsonpToRestFilter = servletContext
 		// .addFilter("JSONP View Filter", RestToJsonpFilter.class);
 		// jsonpToRestFilter.addMappingForUrlPatterns(
 		// (EnumSet.of(DispatcherType.REQUEST)), true, "/api/*");
 		// jsonpToRestFilter.addMappingForUrlPatterns(
 		// (EnumSet.of(DispatcherType.REQUEST)), true, "/apiauth/*");
 
 		// security filter for normal session based users
 		FilterRegistration.Dynamic springSecurityFilterChain = servletContext
 				.addFilter("springSecurityFilterChain",
 						DelegatingFilterProxy.class);
 		springSecurityFilterChain.addMappingForUrlPatterns(
 				(EnumSet.of(DispatcherType.REQUEST)), true, "/api/*");
 
         XmlWebApplicationContext appContext = new XmlWebApplicationContext();
         appContext.getEnvironment().setActiveProfiles("resthub-jpa", "resthub-web-server");
         String[] locations = { "classpath*:resthubContext.xml", "classpath*:applicationContext.xml" };
         appContext.setConfigLocations(locations);
 
 		DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
 		dispatcherServlet.setDispatchOptionsRequest(true);
 		ServletRegistration.Dynamic dispatcher = servletContext.addServlet(
 				"dispatcher", dispatcherServlet);
         dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/*");
 
         servletContext.addListener(new ContextLoaderListener(appContext));
 
         //Database Console for managing the app's database (TODO : profile)
         ServletRegistration.Dynamic h2Servlet = servletContext.addServlet("h2console", WebServlet.class);
         h2Servlet.setLoadOnStartup(2);
         h2Servlet.addMapping("/console/database/*");
     }
 }
