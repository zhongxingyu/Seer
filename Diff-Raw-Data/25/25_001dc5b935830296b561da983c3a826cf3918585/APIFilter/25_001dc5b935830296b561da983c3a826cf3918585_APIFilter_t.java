 package uk.ac.cam.cl.dtg.teaching;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.jboss.resteasy.client.ClientRequestFactory;
 import org.jboss.resteasy.client.ClientResponseFailure;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import uk.ac.cam.cl.dtg.teaching.api.APIUtil;
 import uk.ac.cam.cl.dtg.teaching.api.ApiFailureMessage;
 import uk.ac.cam.cl.dtg.teaching.api.DashboardApi;
 import uk.ac.cam.cl.dtg.teaching.api.DashboardApi.ApiPermissions;
 import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
 
 public class APIFilter implements Filter {
 
 	private static final String INIT_PARAM_ALLOW_GLOBAL = "allowGlobal";
 
 	private static final String INIT_PARAM_EXCLUDE_FROM_LOGGER = "excludeFromLogger";
 
 	private static final String INIT_PARAM_EXCLUDE_PREFIXES = "excludePrefixes";
 
 	private static final String SERVLET_INIT_PARAM_API_KEY = "apiKey";
 
 	private static final String SERVLET_INIT_PARAM_DASHBOARD_URL = "dashboardUrl";
 
 	/**
 	 * Name of the request attribute populated with the current user.
 	 */
 	public static final String USER_ATTR = "userId";
 
 	/**
 	 * Base URL used to access dashboard API.
 	 */
 	private String dashboardUrl;
 
 	/**
 	 * A list of comma separated URLs to allow through
 	 */
 	private String[] excludePrefixes;
 
 	/**
 	 * A list of comma separated URLs to ignore in the logger
 	 */
 	private String[] excludeFromLogger;
 
 	/**
 	 * API key with global permissions for checking other API keys.
 	 */
 	private String apiKey;
 
 	/**
 	 * Whether the service supports global API keys (defaults to false).
 	 */
 	private boolean allowGlobal = true;
 
 	private static Logger log = LoggerFactory.getLogger(APIFilter.class);
 
 	private HibernateUtil hibernateUtil;
 
 	@Override
 	public void init(FilterConfig config) throws ServletException {
 
 		hibernateUtil = HibernateUtil.init("/hibernateRequestLog.cfg.xml");
 
 		dashboardUrl = config.getServletContext().getInitParameter(
 				SERVLET_INIT_PARAM_DASHBOARD_URL);
 		if (dashboardUrl == null) {
 			log.error("Missing dashboard URL from context parameters.");
 		}
 
 		this.excludePrefixes = getInitParamList(config,
 				INIT_PARAM_EXCLUDE_PREFIXES);
 		this.excludeFromLogger = getInitParamList(config,
 				INIT_PARAM_EXCLUDE_FROM_LOGGER);
 
 		apiKey = config.getServletContext().getInitParameter(
 				SERVLET_INIT_PARAM_API_KEY);
 		if (apiKey == null) {
 			log.error("Missing API key from context parameters.");
 		}
 
 		allowGlobal = Boolean.valueOf(config
 				.getInitParameter(INIT_PARAM_ALLOW_GLOBAL));
 
 		log.info("API filter initialised.");
 	}
 
 	@Override
 	public void destroy() {
 		hibernateUtil.close();
 	}
 
 	@Override
 	public void doFilter(ServletRequest servletReq,
 			ServletResponse servletResp, FilterChain chain) throws IOException,
 			ServletException {
 
 		// Convert arguments to HTTP ones to allow grabbing session etc.
 		HttpServletRequest request = (HttpServletRequest) servletReq;
 		HttpServletResponse response = (HttpServletResponse) servletResp;
 		HttpSession session = request.getSession();
 
 		try {
 			boolean authorized = false;
 			boolean credentialsFound = false;
 			if (matchesExcludedPrefix(request)) {
 				authorized = true;
 			} else if (request.getParameter("key") != null) {
 				credentialsFound = true;
 				if (checkKey(request)) {
 					authorized = true;
 				}
 			} else if (isSessionValid(session)
 					&& session.getAttribute("RavenRemoteUser") != null) {
 				credentialsFound = true;
 				// We have a Raven login active
 				String crsid = (String) session.getAttribute("RavenRemoteUser");
 				log.debug("API request permitted for user " + crsid);
 				logRequest(crsid, request.getRequestURI().toString(),
 						request.getMethod(), request.getQueryString());
 				request.setAttribute(USER_ATTR, crsid);
 				authorized = true;
 			}
 
 			if (authorized) {
 				chain.doFilter(request, response);
 			} else {
 				if (credentialsFound) {
 					throw new AuthFailException(new ApiFailureMessage(
 							"Failed to authorize request.  Key provided was: "
 									+ request.getParameter("key")));
 				} else {
 					throw new AuthFailException(
 							new ApiFailureMessage(
 									"No authentication information provided.  Login required"));
 				}
 			}
 		} catch (AuthFailException e) {
 
 			ObjectMapper mapper = new ObjectMapper();
 			String jsonString = mapper.writeValueAsString(e.message);
 			response.setContentType(MediaType.APPLICATION_JSON);
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			response.getWriter().write(jsonString);
 		}
 
 	}
 
 	private boolean matchesExcludedPrefix(HttpServletRequest request) {
 		if (excludePrefixes != null) {
 			// Check whether the URL should be excluded from the filter
 			// If so, chain through
 			for (String p : excludePrefixes) {
 				if (request.getRequestURI().startsWith(
 						request.getContextPath() + p)) {
 					log.debug(
 							"Chaining request through API filter with prefix: {}",
 							request.getRequestURI());
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private static class AuthFailException extends Exception {
 
 		private static final long serialVersionUID = -5530143141307492684L;
 		ApiFailureMessage message;
 
 		public AuthFailException(ApiFailureMessage message) {
 			super(message.getMessage());
 			this.message = message;
 		}
 	}
 
 	private boolean checkKey(HttpServletRequest request)
 			throws AuthFailException {
 		String key = (String) request.getParameter("key");
 
 		ApiPermissions permissions = lookupApiPermissions(key);
 
 		if (permissions.getError() != null) {
 			// Key was invalid
 			log.error("Request with invalid API key = " + key);
 			// TODO: return SC 401
 			throw new AuthFailException(new ApiFailureMessage(
 					"The API key provided was invalid", new ApiFailureMessage(
 							permissions.getError())));
 		}
 
 		if (permissions.getType().equals("global")) {
 			String fakeUser = request.getParameter("impostorUser");
 
 			if (allowGlobal) {
 				// Global supported, allow request with null user.
 				// Allow global api keys to fake users.
 
 				log.debug("API request permitted for global key.");
 				logRequest("GLOBAL", request.getRequestURI().toString(),
 						request.getMethod(), request.getQueryString());
 				request.setAttribute(USER_ATTR, fakeUser);
 
 				return true;
 			} else {
 				// Global unsupported,
 				// TODO: return 405 (unsupported method)
 				log.error("Request with global API key when allowGlobal=false.");
 				throw new AuthFailException(new ApiFailureMessage(
 						"Global API keys are not supported by this method"));
 			}
 		}
 
 		if (permissions.getType().equals("user")) {
 			// User-specific key.
 			String userId = permissions.getUserId();
 
 			log.debug("API request permitted with key for " + userId);
 			logRequest(userId, request.getRequestURI().toString(),
 					request.getMethod(), request.getQueryString());
 			request.setAttribute(USER_ATTR, userId);
 			return true;
 		}
 
 		return false;
 	}
 
 	private ApiPermissions lookupApiPermissions(String key)
 			throws AuthFailException {
 		ClientRequestFactory crf = new ClientRequestFactory(UriBuilder.fromUri(
 				dashboardUrl).build());
 		DashboardApi dApi = crf.createProxy(DashboardApi.class);
 		try {
 			ApiPermissions permissions = dApi.getApiPermissions(key);
 			if (permissions == null) {
 				// Empty response
 				log.error("Error checking key: empty response");
 				throw new AuthFailException(
 						new ApiFailureMessage(
 								"The returned API permissions information was null when checking key"));
 			}
 			return permissions;
 		} catch (ClientResponseFailure e) {
			ApiFailureMessage failMessage = APIUtil.getApiFailureMessage(e);
			throw new AuthFailException(new ApiFailureMessage("Failed make getApiPermissions call",failMessage));
 		}
 	}
 

 	private void logRequest(String crsid, String uri, String method,
 			String queryString) throws AuthFailException {
 
 		Session s = null;
 		try {
 			if (excludeFromLogger != null
 					&& !Arrays.asList(excludeFromLogger).contains(uri)
 					|| excludeFromLogger == null) {
 				s = hibernateUtil.getSession();
 				RequestLog rl = new RequestLog(crsid, uri, queryString, method);
 				s.save(rl);
 				s.getTransaction().commit();
 			}
 		} catch (HibernateException e) {
 			if (s != null)
 				s.getTransaction().rollback();
 			throw new AuthFailException(new ApiFailureMessage(
 					"A database problem occurred when logging request", e));
 		}
 	}
 
 	private static boolean isSessionValid(HttpSession session) {
 		try {
 			session.getCreationTime();
 			return true;
 		} catch (IllegalStateException e) {
 			return false;
 		}
 	}
 
 	private static String[] getInitParamList(FilterConfig config, String name) {
 		String s = config.getServletContext().getInitParameter(name);
 		if (s != null) {
 			String[] list = s.split(",");
 			for (int i = 0; i < list.length; i++) {
 				list[i] = list[i].trim();
 			}
 			return list;
 		}
 		return null;
 	}
 }
