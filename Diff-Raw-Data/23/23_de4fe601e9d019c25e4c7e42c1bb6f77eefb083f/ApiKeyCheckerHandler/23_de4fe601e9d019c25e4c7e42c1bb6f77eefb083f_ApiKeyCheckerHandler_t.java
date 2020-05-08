 package com.artisztikum.ac.server;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.eclipse.jetty.client.ContentExchange;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.util.component.AbstractLifeCycle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.artisztikum.ac.Util;
 import com.artisztikum.ac.httpclient.ACHttpClient;
 
 /**
  * Checks if an api key is saved into a cookie. If not, allows the user to provide it.
  *
  * @author Adam DAJKA (dajka@artisztikum.hu)
  *
  */
 public final class ApiKeyCheckerHandler extends AbstractLifeCycle implements Handler
 {
 	/**
 	 * Logger.
 	 */
 	private static final Logger LOG = LoggerFactory.getLogger(ApiKeyCheckerHandler.class);
 
 	/**
 	 * Reference to the {@link Server} instance.
 	 */
 	private Server server;
 
 	/**
 	 * The http client.
 	 */
 	private final ACHttpClient client;
 
 	/**
 	 * @param client
 	 *            The HTTP client to set the cookie in.
 	 */
 	public ApiKeyCheckerHandler(final ACHttpClient client)
 	{
 		this.client = client;
 
 	}
 
 	@Override
 	public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
 			final HttpServletResponse response) throws IOException, ServletException
 	{
		LOG.info("Processing URL '{}'", target);

 		// to be sure, we start with empty api key
 		client.resetApiKey();
 
 		final Cookie c = Util.getCookie(request, "apikey");
 
 		final VelocityContext ctx = new VelocityContext();
 		ctx.put("APIHost", client.getApiHost());
 
 		// find api key
 		if (null != c) {
 
 			client.setApiKey(c.getValue());
 
 			// check the url
 			// TODO a separate handler is needed
 			if (target.isEmpty() || target.equals("/")) {
 				response.setStatus(301);
 				response.setHeader("location", "/projects/");
 				baseRequest.setHandled(true);
 			}
 
 			final ContentExchange infoCtx = client.sendGetWait("/info");
 			if (infoCtx.getResponseStatus() != 403) {
 				return;
 			}
 
 			ctx.put("invalidKey", true);
 			ctx.put("apiKey", client.getApiKey());
 		}
 
 
 		// otherwise show the login screen
 		final Template template = Velocity.getTemplate("/com/artisztikum/ac/Login.vm");
 
 		response.setStatus(200);
 		response.setContentType("text/html;charset=utf-8");
 
 		template.merge(ctx, response.getWriter());
 		baseRequest.setHandled(true);
 	}
 
 	@Override
 	public void setServer(final Server server)
 	{
 		this.server = server;
 	}
 
 	@Override
 	public Server getServer()
 	{
 		return this.server;
 	}
 
 	@Override
 	public void destroy()
 	{
 		client.resetApiKey();
 	}
 
 }
