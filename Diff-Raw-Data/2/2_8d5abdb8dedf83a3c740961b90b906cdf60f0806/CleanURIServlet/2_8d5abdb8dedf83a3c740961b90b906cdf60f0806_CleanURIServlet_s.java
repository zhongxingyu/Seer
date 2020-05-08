 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.penguineering.cleanuri.webapp;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.jcip.annotations.ThreadSafe;
 
 import com.penguineering.cleanuri.Site;
 import com.penguineering.cleanuri.api.Decorator;
 import com.penguineering.cleanuri.api.ExtractorException;
 import com.penguineering.cleanuri.api.Metakey;
 import com.penguineering.cleanuri.decorators.DokuwikiDecorator;
 import com.penguineering.cleanuri.sites.reichelt.ReicheltSite;
 
 @ThreadSafe
 public class CleanURIServlet extends HttpServlet {
 	private static final long serialVersionUID = 8983389610237056848L;
 
 	static final Map<String, Site> sites;
 
 	static {
 		sites = new HashMap<String, Site>();
 
 		final Site site = ReicheltSite.getInstance();
 		sites.put(site.getLabel(), site);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/plain; charset=UTF-8");
 
 		// get the request URI
 		final URI uri;
 		try {
 			uri = retrieveUriParameter(request);
 		} catch (IllegalArgumentException e) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"On reading uri parameter: " + e.getMessage());
 			return;
 		}
 
 		// retrieve the site
 		final Site site;
 		try {
 			final String site_label = retrieveSiteParameter(request);
 			site = getSite(uri, site_label);
 		} catch (NoSuchElementException e) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"Invalid site: " + e.getMessage());
 			return;
 		} catch (IllegalArgumentException e) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"Invalid site site: " + e.getMessage());
 			return;
 		}
 		if (site == null) {
 			response.sendError(HttpServletResponse.SC_NOT_FOUND,
 					"No site matching the URI could be found!");
 			return;
 		}
 
 		// retrieve the decorator
 		final Decorator decorator;
 		final String decorator_label = retrieveDecoratorParameter(request);
		if (decorator_label == null)
 			decorator = null;
 		else if (decorator_label.equals("dokuwiki"))
 			decorator = new DokuwikiDecorator();
 		else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"Invalid decorator label!");
 			return;
 		}
 
 		URI href = site.getCanonizer().canonize(uri);
 
 		if (decorator == null)
 			response.getWriter().print(href.toASCIIString());
 		else {
 			Map<Metakey, String> meta;
 			try {
 				meta = site.getExtractor().extractMetadata(href);
 			} catch (ExtractorException e) {
 				response.sendError(
 						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 						"Error during meta-data extraction: " + e.getMessage());
 				return;
 			}
 
 			response.getWriter().println(decorator.decorate(href, meta));
 		}
 	}
 
 	private Site getSite(URI uri, String label) {
 		if (uri == null)
 			throw new NullPointerException("URI argument must not be null!");
 
 		if (label != null) {
 			final Site site = sites.get(label);
 			if (site == null)
 				throw new NoSuchElementException("Unknown site label: " + label);
 			if (!site.getCanonizer().isSuitable(uri))
 				throw new IllegalArgumentException(
 						"Site is not suitable for the provided URI!");
 			return site;
 		}
 
 		for (Site site : sites.values())
 			if (site.getCanonizer().isSuitable(uri))
 				return site;
 
 		return null;
 	}
 
 	/**
 	 * Retrieve the URI from the HTTP request.
 	 * 
 	 * @param request
 	 *            the HTTP request.
 	 * @return The URI from the request.
 	 * @throws NullPointerException
 	 *             if the request argument is null.
 	 * @throws IllegalArgumentException
 	 *             if the URI in the request is invalid.
 	 */
 	private static URI retrieveUriParameter(HttpServletRequest request) {
 		if (request == null)
 			throw new NullPointerException("Request argument must not be null!");
 
 		final String p_uri = request.getParameter("uri");
 
 		if (p_uri == null || p_uri.isEmpty())
 			throw new IllegalArgumentException("Missing parameter: uri");
 
 		try {
 			return new URI(p_uri);
 		} catch (URISyntaxException e) {
 			throw new IllegalArgumentException(
 					"Illegal URI: " + e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Retrieve the site parameter from the request. This allows to specify a
 	 * site for handling the URI. If left out, the site is determined
 	 * automatically.
 	 * 
 	 * @return the site label.
 	 * @throws NullPointerException
 	 *             if the request argument is null.
 	 */
 	private static String retrieveSiteParameter(HttpServletRequest request) {
 		if (request == null)
 			throw new NullPointerException("Request argument must not be null!");
 
 		return (String) request.getParameter("site");
 	}
 
 	/**
 	 * Retrieve the decorator parameter from the request. This allows to specify
 	 * a decorator for transforming the URI to an output. If left out, the plain
 	 * URI is returned.
 	 * 
 	 * @return the decorator label.
 	 * @throws NullPointerException
 	 *             if the request argument is null.
 	 */
 	private static String retrieveDecoratorParameter(HttpServletRequest request) {
 		if (request == null)
 			throw new NullPointerException("Request argument must not be null!");
 
 		return (String) request.getParameter("decorator");
 	}
 
 }
