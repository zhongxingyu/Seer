 /**
  * Copyright (c) 2009 Aurora Software Technology Studio. All rights reserved.
  */
 package com.corona.servlet.producing.freemaker;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Locale;
 
 import javax.servlet.ServletContext;
 
 import freemarker.cache.MruCacheStorage;
 import freemarker.cache.WebappTemplateLoader;
 import freemarker.core.Environment;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.TemplateException;
 import freemarker.template.TemplateExceptionHandler;
 
 /**
  * <p>The default configurator that is used to configure FreeMaker defined configuration </p>
  *
  * @author $Author$
  * @version $Id$
  */
 public class DefaultFreeMakerConfigurator implements FreeMakerConfigurator {
 
 	/**
 	 * {@inheritDoc}
 	 * @see com.corona.servlet.producing.freemaker.FreeMakerConfigurator#configure(
 	 * 	javax.servlet.ServletContext, freemarker.template.Configuration
 	 * )
 	 */
 	@Override
 	public void configure(final ServletContext servletContext, final Configuration configuration) {
 
 		// Configure FreeMaker template settings
 		configuration.setObjectWrapper(new DefaultObjectWrapper());
		configuration.setTemplateLoader(new WebappTemplateLoader(servletContext, ""));
 		configuration.setCacheStorage(new MruCacheStorage(8, 80));
 		
 		// set default encoding and locale to FreeMaker engine
 		configuration.setDefaultEncoding("UTF-8");
 		configuration.setLocale(new Locale("en", "US"));
 		configuration.setLocalizedLookup(true);
 
 		// set template new version checking and white space strip
 		configuration.setTemplateUpdateDelay(3);
 		configuration.setWhitespaceStripping(false);
 		
 		// add template exception handler to display error on page
 		configuration.setTemplateExceptionHandler(new TemplateExceptionHandler() {
 			public void handleTemplateException(
 					final TemplateException e, final Environment env, final Writer out
 			) throws TemplateException {
 				
 				try {
 					out.write(
 							"<div style=\"color: red; font-weight: bolder\">[ERROR: " + e.getMessage() + "]</div>"
 					);
 				} catch (IOException ioe) {
 					throw new TemplateException("Failed to print error message. Cause: " + ioe, env);
 				}
 			}
 		});
 	}
 }
