 package de.zib.gndms.kit.monitor;
 
import com.oreilly.servlet.Base64Decoder;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.security.Principal;
 import java.util.Enumeration;

 
 /**
  * This servlet is run from GroovyMonitorServer to provide access to GroovyMonitors via
  * http.  The general idea is that users authenticate to the servlet container, initiate
  * a session and then explicitely create named monitors by initiating a get request to the
  * servlet.  This get request is used to read the monitor's (GroovyShell's) output stream.
  * Groovy script code can be streamed to a monitor for execution using HTTP multipart
  * post-requests.  All such requests are executed serially in the order of their arrival.
  * <br/>
  *
  * Valid servlet parameters are:
  * <br/>
  *
  * token: name of the named monitor:, m: mode which can be either a creation
  * run mode (cf. GroovyMonitor.RunMode), the string "close" for explicit monitor closedown,
  * the empty string "" for the initialization of a session for the user,
  * the string "destroy" for explicit destruction of the user's session,
  * "refresh" for a configuration refresh of the containing monitor server, or "restart" for
  * a forced restart of the containing monitor server, and b64: which if set to "1" denotes that
  * the contents of the multiparts of a post-request are encoded in base64. (Variable/File
  * names of http-multiparts are ignored; file-parts are preferred since they can be processed
  * in a streaming fashion). args: a base64 encoded argument string used during monitor
  * instantiation and potentially made accessible to the script code via its binding factory.
  * <br/>
  *
  * Implementation caveat: The implementation stores non-serializable objects in the HTTP session.
  * This is not what your servlet container might expect.  Since the number of monitoring users is
  * usually small, keeping the sessions in memory shouldnt be a big deal. If the serlvet container
  * passivates a session, all contained monitors are disconnected and removed from the session.
  *
  * @see GroovyMoniServer
  * @see de.zib.gndms.kit.monitor.GroovyMonitor
  * @see de.zib.gndms.kit.monitor.GroovyBindingFactory
  *
  * @author Stefan Plantikow <plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 18.07.2008 Time: 02:20:30
  */
 @SuppressWarnings(
         { "ThrowableInstanceNeverThrown", "SynchronizationOnLocalVariableOrMethodParameter" })
 public class GroovyMoniServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 4561717404398330474L;
 
 	/**
 	 * Containing monitor server
 	 */
 	private transient GroovyMoniServer moniServer;
 
 	/**
 	 * Required role of authenticated users that access this service
 	 */
 	private String roleName;
 
 	/**
 	 * Retrieves role name and monitor server reference from servlet context.
 	 *
 	 * @param servletConfig
 	 * @throws ServletException
 	 */
 	@Override
 	public synchronized void init(ServletConfig servletConfig) throws ServletException {
 		super.init(servletConfig);    // Overridden method
 		final ServletContext servletContext = servletConfig.getServletContext();
 		moniServer = (GroovyMoniServer)
 			  servletContext.getAttribute(GroovyMoniServer.ATTR_MONITOR_SERVER);
 		roleName = (String)
 			  servletContext.getAttribute(GroovyMoniServer.ATTR_ROLE_NAME);
 	}
 
 	/**
 	 * One of six things may happen here after succesful authorization:
 	 *
 	 * If m is empty, a new session is establised.
 	 * If m is "refresh", monitorServer.refresh() is a called
 	 * If m is "restart", monitorServer.restart() is a called
 	 * If m is "close", monitor with id of parameter "token" is closed,
 	 * If m is "shutdown", current user's session is shutdown and all associated monitors closed,
 	 * Otherwise, a new monitor with id of parameter "token" is created for the current user.
 	 *
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 */
 	@Override
 	public void doGet(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response)
 		  throws IOException
 	{
 		@NotNull
 		final HttpServletRequestWrapper reqWrapper = new HttpServletRequestWrapper(request);
 
 		try {
 			// authorization
 			verifyUserRole(request);
 
 			if (contAftOperatingSrvIfRequested(reqWrapper, response)) {
                 if (didDestroySessionOnRequest(reqWrapper))
                     response.setStatus(HttpServletResponse.SC_OK);
                 else {
                     final String token = parseToken(reqWrapper);
                     if (token.length() == 0)
                         // create new session of token is empty and none is existing
                         createNewSession(request, response);
                     else
                         establishNewMonitor(response, reqWrapper, token, getSessionOrFail(request));
                 }
             }
 		}
 		catch (ServletRuntimeException e) {
 			e.sendToClient(response);
 		}
 	}
 
 	/**
 	 * Stream incoming HTTP multiparts to a monitor previously opened by the current user.
 	 *
 	 * @param servletRequest
 	 * @param servletResponse
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	@Override
 	protected void doPost(
 		  @NotNull HttpServletRequest servletRequest, @NotNull HttpServletResponse servletResponse)
 		  throws ServletException, IOException
 	{
 		@NotNull
 		HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(servletRequest);
 
 		try {
 			verifyUserRole(servletRequest);
 
 			String token = parseToken(requestWrapper);
 			if (token.length() == 0)
 				throw notAcceptable("Zero-length token");
 
 			@NotNull HttpSession session = getSessionOrFail(servletRequest);
 			final @NotNull GroovyMonitor monitor =
 				  lookupMonitorOrFail(servletRequest.getUserPrincipal(), session, token);
 
 			monitor.evalParts(servletRequest, 
 				  parseArgs(requestWrapper), shouldDecodeBase64(requestWrapper));
 			servletResponse.setStatus(HttpServletResponse.SC_OK);
 		}
 		catch (ServletRuntimeException e) {
 			e.sendToClient(servletResponse);
 		}
 	}
 
 	private synchronized void verifyUserRole(@NotNull HttpServletRequest servletRequest) {
 		if (! servletRequest.isUserInRole(roleName))
 			throw unauthorized("User not in required role");
 	}
 
 	@SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
     private synchronized boolean contAftOperatingSrvIfRequested(
             @NotNull HttpServletRequestWrapper requestWrapper,
             final HttpServletResponse responseParam) throws IOException {
         final String mode = requestWrapper.getParameter("m");
 
 		// refresh == reload config and restart server if config has changed
 		if ("refresh".equalsIgnoreCase(mode)) {
 			try {
 				moniServer.refresh();
 				throw new ServletRuntimeException(HttpServletResponse.SC_RESET_CONTENT,
 					  "Refreshed", false);
 			}
 			catch (Exception e) {
 				intlError(e);
 			}
 		}
 		// restart server
 		if ("restart".equalsIgnoreCase(mode)) {
 			try {
 				moniServer.restart();
 				throw new ServletRuntimeException(HttpServletResponse.SC_RESET_CONTENT,
 					  "Restarted", false);
 			}
 			catch (Exception e) {
 				intlError(e);
 			}
 		}
         if ("call".equals(mode)) {
             doCallAction(requestWrapper, responseParam);
             return false;
         }
         return true;
 	}
 
 
     private void doCallAction(
             final HttpServletRequestWrapper requestWrapper, final HttpServletResponse responseParam)
             throws IOException {
         String args = parseArgs(requestWrapper);
         String className = parseAction(requestWrapper).trim();
         StringWriter swriter = new StringWriter();
         PrintWriter pwriter = new PrintWriter(swriter);
         try {
             moniServer.callAction(className, args, pwriter);
             pwriter.flush();
             PrintWriter rwriter = responseParam.getWriter();
             responseParam.setStatus(HttpServletResponse.SC_OK);
             rwriter.write(swriter.toString());
         }
         catch (Exception e) {
             throw new ServletRuntimeException(HttpServletResponse.SC_BAD_REQUEST, e, true);
         }
         finally { pwriter.close(); }
     }
 
 
     /**
 	 * Tries to destroy the current session and reclaim associated resources
 	 *
 	 * @param requestWrapper
 	 * @return true, if the session was destroyed. false, if there was none.
 	 */
 	@SuppressWarnings({ "unchecked" })
     private static boolean didDestroySessionOnRequest(@NotNull HttpServletRequest requestWrapper) {
 		if ("destroy".equalsIgnoreCase(requestWrapper.getParameter("m"))) {
 			final HttpSession session = getSessionOrFail(requestWrapper);
 			if (session != null) {
                 synchronized (session) {
                     final Enumeration<String> attrs =
                             (Enumeration<String>) session.getAttributeNames();
                     while (attrs.hasMoreElements())
                         session.removeAttribute(attrs.nextElement());
                     session.invalidate();
                 }
             }
 			return true;
 		}
 		else
 			return false;
 	}
 
 	private static void createNewSession(
 		  @NotNull HttpServletRequest request,
 		  @NotNull HttpServletResponse response) {
 		request.getSession(true);
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	private static HttpSession getSessionOrFail(@NotNull HttpServletRequest servletRequest) {
 		HttpSession session = servletRequest.getSession(false);
 		if (session == null)
 			throw preCondFailed("No session found");
 		return session;
 	}
 
 	/**
 	 * now that we have a session we try to create a monitor with id token
 	 * if it already exists, an exception will be thrown
 	 */
 	private void establishNewMonitor(
 		  @NotNull HttpServletResponse response,
 		  @NotNull HttpServletRequestWrapper requestWrapper,
 		  @NotNull String token,
 		  @NotNull HttpSession session) throws IOException {
 		PrintWriter outWriter = null;
 		GroovyMonitor monitor = null;
 		String monitorArgs = parseArgs(requestWrapper);
 		try {
 			monitor = createMonitor(requestWrapper, session, token, monitorArgs, response);
 			response.setStatus(HttpServletResponse.SC_OK);
 			if (monitor != null)
 				// keep output stream open
 				monitor.waitLoop();
 		}
 		finally {
 			if (monitor != null) {
 				synchronized (session) {
 					try {
 						session.removeAttribute(monitor.getToken());
 					}
 					catch (IllegalStateException e) {
 						// intentionally nothing; denotes that the session has been invalidated
 						// by another thread
 					}
 				}
 			}
 		}
 	}
 
     private static String parseAction(HttpServletRequestWrapper requestWrapper) {
         final String monitorArgs = requestWrapper.getParameter("action");
        return monitorArgs == null ? "" : Base64Decoder.decode(monitorArgs);
     }
 
 	private static String parseArgs(HttpServletRequestWrapper requestWrapper) {
 		final String monitorArgs = requestWrapper.getParameter("args");
		return monitorArgs == null ? "" : Base64Decoder.decode(monitorArgs);
 	}
 
 	// Im scared about this one: Didnt know thats not allowed
 	@SuppressWarnings({"NonSerializableObjectBoundToHttpSession"})
 	@Nullable
 	GroovyMonitor createMonitor(
 		  @NotNull HttpServletRequestWrapper request, @NotNull HttpSession session,
 		  @NotNull String token, @NotNull String args,
 		  @NotNull HttpServletResponse response) throws IOException
 	{
 		GroovyMonitor monitor;
 		final Principal principal = request.getUserPrincipal();
 
 		synchronized (session) {
 			monitor = (GroovyMonitor) session.getAttribute(token);
 			if (monitor == null) {
 				GroovyMonitor.RunMode mode = parseMode(request, moniServer.getDefaultMode());
 				if (mode == GroovyMonitor.RunMode.CLOSE)
 					throw badRequest("Cant close unavailable token");
 				final PrintWriter outWriter = response.getWriter();
 				monitor =
 					  new GroovyMonitor(moniServer, principal, token,  mode, args, outWriter);
 				session.setAttribute(token, monitor);
 			}
 			else {
 				GroovyMonitor.RunMode mode = parseMode(request, moniServer.getDefaultMode());
 				if (GroovyMonitor.RunMode.CLOSE.equals(mode)) {
 					// monitor.destroyMonitor(session) will be calle by doGet finalizer
 					monitor.setRunMode(mode);
 					return null;
 				}
 				else
 					throw badRequest("Token already open");
 			}
 		}
 
 		return monitor;
 	}
 
 	@NotNull
 	static GroovyMonitor lookupMonitorOrFail(
 		  @NotNull Principal principal, @NotNull HttpSession session, @NotNull String token)
 	{
 		synchronized (session) {
 			final GroovyMonitor monitor =
 				  (GroovyMonitor) session.getAttribute(token);
 			if (monitor == null)
 				throw preCondFailed("Unknown token");
 			monitor.verifyPrincipal(principal);
 			return monitor;
 		}
 	}
 
 	private static boolean shouldDecodeBase64(@NotNull HttpServletRequestWrapper requestWrapper) {
 		return "1".equals(requestWrapper.getParameter("b64"));
 	}
 
 	private static String parseToken(@NotNull HttpServletRequestWrapper requestWrapper) {
 		final String token = requestWrapper.getParameter("token");
 		return token == null ? "" : token.trim();
 	}
 
 	private static GroovyMonitor.RunMode parseMode(
 		  @NotNull HttpServletRequestWrapper requestWrapper,
 		  @NotNull GroovyMonitor.RunMode defaultMode)
 	{
 			final String param = requestWrapper.getParameter("m");
 			return param == null ?
 				  defaultMode : GroovyMonitor.RunMode.valueOf(param.trim().toUpperCase());
 	}
 
 	private static ServletRuntimeException unauthorized(String s) {
 		return new ServletRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, s, true);
 	}
 
 	private static ServletRuntimeException badRequest(String s) {
 		return new ServletRuntimeException(HttpServletResponse.SC_BAD_REQUEST, s, false);
 	}
 
 	private static ServletRuntimeException preCondFailed(String s) {
 		return new ServletRuntimeException(HttpServletResponse.SC_PRECONDITION_FAILED, s, false);
 	}
 
 	private static ServletRuntimeException notAcceptable(String s) {
 		return new ServletRuntimeException(HttpServletResponse.SC_NOT_ACCEPTABLE, s, false);
 	}
 
 	private static void intlError(Exception e) {
 		throw new ServletRuntimeException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e, true);
 	}
 }
