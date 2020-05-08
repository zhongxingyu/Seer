 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.view.handlebars;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 
 import com.github.jknack.handlebars.Handlebars;
 import com.github.jknack.handlebars.HandlebarsException;
 import com.github.jknack.handlebars.Template;
 import com.github.jknack.handlebars.io.ServletContextTemplateLoader;
 import com.threewks.thundr.view.ViewResolutionException;
 
 public class HandlebarsRenderer {
 	private Handlebars handlebars;
 	private Map<URI, Long> lastModified = new HashMap<URI, Long>();
 	private ReloadingConcurrentMapCache cache;
 
 	private boolean debug;
 	private ServletContext servletContext;
 	private ServletContextTemplateLoader loader;
 
 	public HandlebarsRenderer(boolean debug) {
 		this.debug = debug;
 	}
 
 	public Template getOrCompileTemplate(String templatePath, ServletContext servletContext) {
 		init(servletContext);
 
 		URI templateUri = uri(templatePath);
 		if (debug && requiresReload(templatePath)) {
 			cache.evict(templateUri.toString());
 		}
 		try {
 			Template existing = cache.get(templateUri.toString());
 			return existing == null ? handlebars.compile(templateUri) : existing;
 		} catch (IOException e) {
 			throw new ViewResolutionException(e, "Failed to find Handlebars template '%s': %s", templatePath, e.getMessage());
 		} catch (HandlebarsException e) {
 			throw new ViewResolutionException(e, "Failed to compile Handlebars template '%s': %s", templatePath, e.getMessage());
 		}
 	}
 
 	// package protected for tests
 	synchronized void init(ServletContext servletContext) {
 		if (this.handlebars == null) {
 			this.servletContext = servletContext;
 			loader = new ServletContextTemplateLoader(servletContext);
 			loader.setPrefix("");
 			loader.setSuffix("");
 			this.cache = new ReloadingConcurrentMapCache();
 			this.handlebars = new Handlebars(loader, cache);
 		}
 	}
 
 	private boolean requiresReload(String templatePath) {
 		try {
 			String resource = loader.resolve(templatePath);
 			URI uri = this.servletContext.getResource(resource).toURI();
 			Long lastModified = this.lastModified.get(uri);
 			long modified = new File(uri).lastModified();
 			this.lastModified.put(uri, modified);
 			return lastModified == null || modified > lastModified;
 		} catch (URISyntaxException e) {
 			return false;
 		} catch (MalformedURLException e) {
 			return false;
 		}
 	}
 
 	private URI uri(String templatePath) {
 		try {
 			return new URI(templatePath);
 		} catch (URISyntaxException e) {
 			throw new ViewResolutionException(e, "Template '%s' cannot be converted to a URI: %s", templatePath, e.getMessage());
 		}
 	}
 
 	public boolean isDebug() {
 		return debug;
 	}
 }
