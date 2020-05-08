 package org.prot.controller.handler;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.continuation.Continuation;
 import org.eclipse.jetty.continuation.ContinuationSupport;
 import org.eclipse.jetty.http.HttpStatus;
 import org.eclipse.jetty.http.HttpURI;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.prot.controller.app.AppInfo;
 import org.prot.controller.app.AppLife;
 import org.prot.controller.app.AppManager;
 import org.prot.controller.stats.ControllerStatsCollector;
 import org.prot.util.AppIdExtractor;
 import org.prot.util.ReservedAppIds;
 
 public class RequestHandler extends AbstractHandler
 {
 	private static final Logger logger = Logger.getLogger(RequestHandler.class);
 
 	private static final String APP_SERVER_HOST = "localhost";
 
 	private AppManager appManager;
 
 	private RequestProcessor requestProcessor;
 
 	private ControllerStatsCollector stats;
 
 	private final HttpURI getUrl(final Request request, final AppInfo appInfo)
 	{
 		// Get the connection settings
 		String scheme = request.getScheme();
 		int port = appInfo.getPort();
 		String uri = request.getUri().toString();
 
 		// Build the complete URL (In this case string concation seems to be
 		// inefficient)
 		StringBuilder builder = new StringBuilder(scheme.length() + 4 + uri.length() + 10);
 		builder.append(scheme);
 		builder.append("://");
 		builder.append(APP_SERVER_HOST);
 		builder.append(":");
 		builder.append(port);
 		builder.append(uri.substring(1 + appInfo.getAppId().length()));
 
 		return new HttpURI(builder.toString());
 	}
 
 	@Override
 	public void handle(String target, Request baseRequest, HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException
 	{
 		AppInfo appInfo = null;
 
 		// Check if this a continuation
 		Continuation continuation = ContinuationSupport.getContinuation(request);
 		if (continuation.isResumed())
 		{
 			appInfo = (AppInfo) continuation.getAttribute(AppInfo.CONTINUATION_ATTRIBUTE_APPINFO);
 			if (appInfo.getStatus().getLife() == AppLife.SECOND)
 			{
 				logger.debug("Continuation resumed, AppInfo life is SECOND " + appInfo.getAppId());
 				response.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, "Could not start the AppServer");
 				baseRequest.setHandled(true);
 				return;
 			}
 		}
 
 		if (appInfo == null)
 		{
 			// Extract the AppId
 			String appId = AppIdExtractor.fromUri(baseRequest.getUri().toString());
 
 			// Check the AppId and send an error
 			if (appId == null)
 			{
 				logger.debug("Unknown AppId in: " + baseRequest.getUri().toString());
 
 				response.sendError(HttpStatus.BAD_REQUEST_400,
 						"Invalid or missing AppId (scheme://domain/AppId/...)");
				
 				baseRequest.setHandled(true);
 				return;
 			}
 
 			// Check if it is a ping request from an AppServer
 			if (appId.equals(ReservedAppIds.APP_PING))
 			{
				logger.debug("ping from AppServer"); 
				
 				// Simply response this request with ok
 				response.getWriter().print("ok");
 				baseRequest.setHandled(true);
 				return;
 			}
 
 			// Check if the application is blocked
 			if (appManager.isBlocked(appId))
 			{
 				// Currently this Controller blocks all requests for the
 				// application
 				logger.debug("Recived request for blocked: " + appId);
				
				response.sendError(HttpStatus.MOVED_TEMPORARILY_302,
						"Controller blocks requested application");
				
 				baseRequest.setHandled(true);
 				return;
 			}
 
 			// Inform the AppManager
 			appInfo = appManager.requireApp(appId);
 		}
 
 		// The AppServer is not avialable - a continuation is used to restart
 		// this request when the AppServer is online. If a continuation is used
 		// the appInfo is null - this method will return for now. If the
 		// continuation continues this handle method will be called again and
 		// the appInfo than is *not* null
 		if (appInfo == null)
 		{
 			logger.debug("Could not aquire an AppInfo - Waiting until continuation resumes");
 			return;
 		}
 
 		// Update stats
 		stats.handle(appInfo);
 
 		// Create a destination URL to forward the request
 		try
 		{
 			HttpURI destination = getUrl(baseRequest, appInfo);
 
 			// Register the request in the RequestManager.
 			requestProcessor.process(appInfo, baseRequest, request, response, destination);
 		} catch (Exception e)
 		{
 			logger.error("Could not process request", e);
 			response.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, "Could not start AppServer");
 			baseRequest.setHandled(true);
 			return;
 		}
 	}
 
 	public void setAppManager(AppManager appManager)
 	{
 		this.appManager = appManager;
 	}
 
 	public void setRequestProcessor(RequestProcessor requestProcessor)
 	{
 		this.requestProcessor = requestProcessor;
 	}
 
 	public void setStats(ControllerStatsCollector stats)
 	{
 		this.stats = stats;
 	}
 }
