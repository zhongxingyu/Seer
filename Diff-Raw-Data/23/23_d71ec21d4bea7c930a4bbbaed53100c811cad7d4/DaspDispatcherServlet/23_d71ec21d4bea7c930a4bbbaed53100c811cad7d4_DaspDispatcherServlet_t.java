 package ddth.dasp.servlet;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.catalina.comet.CometEvent;
 import org.apache.catalina.comet.CometProcessor;
 
 import ddth.dasp.common.DaspGlobal;
 import ddth.dasp.common.IRequestHandler;
import ddth.dasp.common.api.IApiHandler;
 import ddth.dasp.common.osgi.IOsgiBootstrap;
 import ddth.dasp.common.utils.ServletUtils;
 
 public class DaspDispatcherServlet extends HttpServlet implements
 		CometProcessor {
 	private static final long serialVersionUID = 1L;
 	private final static Class<IRequestHandler> HANDLER_CLASS = IRequestHandler.class;
 
 	// private Logger LOGGER =
 	// LoggerFactory.getLogger(DaspDispatcherServlet.class);
 
 	private String contextPath;
 	private Map<String, String> ERROR_PAGE_MAPPING = new HashMap<String, String>();
 	private final static String ERROR_PAGE_403 = "/403.jsp";
 	private final static String ERROR_PAGE_404 = "/404.jsp";
 	private final static String ERROR_PAGE_500 = "/500.jsp";
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		contextPath = getServletContext().getContextPath();
 		ERROR_PAGE_MAPPING.put(contextPath + ERROR_PAGE_403, ERROR_PAGE_403);
 		ERROR_PAGE_MAPPING.put(contextPath + ERROR_PAGE_404, ERROR_PAGE_404);
 		ERROR_PAGE_MAPPING.put(contextPath + ERROR_PAGE_500, ERROR_PAGE_500);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void event(CometEvent event) throws IOException, ServletException {
 		HttpServletRequest request = event.getHttpServletRequest();
 		HttpServletResponse response = event.getHttpServletResponse();
 		switch (event.getEventType()) {
 		case ERROR:
 		case END: {
 			event.close();
 		}
 		case BEGIN: {
 			event.setTimeout(5000);// 5 seconds
 			doHandleRequest(request, response);
 			event.close();
 		}
 		default: {
 			break;
 		}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException {
 		doHandleRequest(request, response);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException {
 		doHandleRequest(request, response);
 	}
 
 	protected void doHandleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException {
 		String uri = request.getRequestURI();
 		for (Entry<String, String> entry : ERROR_PAGE_MAPPING.entrySet()) {
 			if (uri.startsWith(entry.getKey())) {
 				RequestDispatcher rd = getServletContext()
 						.getRequestDispatcher(entry.getValue());
 				rd.forward(request, response);
 				return;
 			}
 		}
 		IOsgiBootstrap osgiBootstrap = DaspGlobal.getOsgiBootstrap();
 		IRequestHandler requestHandler = osgiBootstrap
 				.getService(HANDLER_CLASS);
 		if (requestHandler == null) {
			ServletUtils
					.responseHttpError(response,
							IApiHandler.RESULT_CODE_NOT_IMPLEMETED,
							"No handler found!");
 			return;
 		}
 		requestHandler.handleRequest(request, response);
 	}
 }
