 package org.codefaces.web.internal.urls;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.codefaces.ui.SCMURLConfiguration;
 import org.codefaces.web.internal.CodeFacesWebActivator;
 import org.codefaces.web.urls.URLParsingStrategy;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 
 public class URLDispatchingServlet extends HttpServlet {
 	private static final String CODE_FACES_ENTRY_POINT_ID = "org.codefaces.ui.entrypoint.codefaces";
 
 	private static final String CODE_FACES_URL = getCodeFacesEntryPointPath();
 
 	private static final String FORWARD_SLASH = "/";
 
 	private static final long serialVersionUID = 25248922633642477L;
 
 	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String uri = req.getRequestURI();
 		if (uri.indexOf(FORWARD_SLASH) == 0) {
 			uri = uri.substring(1).trim();
 		}
 
 		SCMURLConfiguration config = new SCMURLConfiguration();
 		URLParsingStrategy strategy = CodeFacesWebActivator.getDefault()
 				.getUrlParseStrategies(uri);
 		if (strategy != null) {
 			config = strategy.buildConfigurations(uri);
 		}
 
 		String redirectUrl = createRepoUrl(req, config);
 		resp.sendRedirect(redirectUrl);
 	}
 
 	private String createRepoUrl(HttpServletRequest req,
 			SCMURLConfiguration config) {
 		StringBuilder url = new StringBuilder();
 		url.append(req.getContextPath());
 		url.append(req.getServletPath());
 		url.append(CODE_FACES_URL);
 
 		String queryString = config.toHTTPQueryString();
		if (queryString != null) {
 			url.append(queryString);
 		}
 		return url.toString();
 	}
 
 	private static String getCodeFacesEntryPointPath() {
 		IConfigurationElement[] configElements = Platform
 				.getExtensionRegistry().getConfigurationElementsFor(
 						"org.eclipse.rap.ui", "branding");
 		for (IConfigurationElement element : configElements) {
 			String defaultEntryPointId = element
 					.getAttribute("defaultEntrypointId");
 			if (StringUtils.equals(defaultEntryPointId,
 					CODE_FACES_ENTRY_POINT_ID)) {
 				return "/" + element.getAttribute("servletName");
 			}
 		}
 
 		return "/explore";
 	}
 }
