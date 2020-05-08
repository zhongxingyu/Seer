 package com.artisztikum.ac.server;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.AbstractHttpConnection;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Response;
 import org.eclipse.jetty.util.component.AbstractLifeCycle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Catches and handles a request based on a regular expression.
  *
  * @author Adam DAJKA (dajka@artisztikum.hu)
  *
  */
 public abstract class AbstractUrlPatternHandler extends AbstractLifeCycle implements Handler
 {
 	/**
 	 * Logger.
 	 */
 	private static final Logger LOG = LoggerFactory.getLogger(AbstractUrlPatternHandler.class);
 
 	/**
 	 * The pattern which will trigger calling
 	 * {@link #doHandle(Matcher, String, Request, HttpServletRequest, HttpServletResponse)}.
 	 */
 	final Pattern urlPattern;
 
 	/**
 	 * @param urlPattern
 	 *            The pattern which will trigger calling
 	 *            {@link #doHandle(Matcher, String, Request, HttpServletRequest, HttpServletResponse)}.
 	 */
 	protected AbstractUrlPatternHandler(final Pattern urlPattern)
 	{
 		super();
 		this.urlPattern = urlPattern;
 	}
 
 	@Override
 	public final void handle(final String target, final Request baseRequest, final HttpServletRequest request,
 			final HttpServletResponse response) throws IOException, ServletException
 	{
		LOG.info("Processing URL '{}'", target);
 		final Matcher m = urlPattern.matcher(target);
 		if (!m.matches()) {
 			return;
 		}
 
 		doHandle(m, target, baseRequest, request, response);
 		LOG.info("Request {} done", target);
 	}
 
 	/**
 	 * @param m
 	 *            The {@link Matcher} used for matching. (The groups can be used).
 	 * @param target
 	 *            The target of the request - either a URI or a name.
 	 * @param baseRequest
 	 *            The original unwrapped request object.
 	 * @param request
 	 *            The request either as the {@link Request} object or a wrapper of that request. The
 	 *            {@link AbstractHttpConnection#getCurrentConnection()} method can be used access the Request object if
 	 *            required.
 	 * @param response
 	 *            The response as the {@link Response} object or a wrapper of that request. The
 	 *            {@link AbstractHttpConnection#getCurrentConnection()} method can be used access the Response object if
 	 *            required.
 	 * @throws IOException
 	 *             When IOExcepton happens. (Subclass decides)
 	 * @throws ServletException
 	 *             When Servlet exception happens. (Subclass decides)
 	 */
 	protected abstract void doHandle(final Matcher m, final String target, final Request baseRequest,
 			final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException;
 }
