 package xdi2.connector.facebook.connect;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.HttpRequestHandler;
 
 import xdi2.client.XDIClient;
 import xdi2.client.exceptions.Xdi2ClientException;
 import xdi2.client.http.XDIHttpClient;
 import xdi2.connector.facebook.api.FacebookApi;
 import xdi2.connector.facebook.util.GraphUtil;
 import xdi2.core.Graph;
 import xdi2.core.impl.memory.MemoryGraphFactory;
 import xdi2.core.io.XDIReader;
 import xdi2.core.io.XDIReaderRegistry;
 import xdi2.core.io.XDIWriter;
 import xdi2.core.io.XDIWriterRegistry;
 import xdi2.core.io.writers.XDIDisplayWriter;
 import xdi2.core.xri3.XDI3Segment;
 import xdi2.messaging.MessageEnvelope;
 import xdi2.messaging.MessageResult;
 
 public class ConnectServlet extends HttpServlet implements HttpRequestHandler {
 
 	private static final long serialVersionUID = 3793048689633131588L;
 
 	private static final Logger log = LoggerFactory.getLogger(ConnectServlet.class);
 
 	private static MemoryGraphFactory graphFactory;
 	static String sampleInput;
 	static String sampleEndpoint;
 
 	private Graph graph;
 	private FacebookApi facebookApi;
 
 	static {
 
 		graphFactory = MemoryGraphFactory.getInstance();
 		graphFactory.setSortmode(MemoryGraphFactory.SORTMODE_ORDER);
 
 		InputStream inputStream = ConnectServlet.class.getResourceAsStream("message.xdi");
 		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		int i;
 
 		try {
 
 			while ((i = inputStream.read()) != -1) outputStream.write(i);
 			sampleInput = new String(outputStream.toByteArray());
 		} catch (Exception ex) {
 
 		} finally {
 
 			try {
 
 				inputStream.close();
 				outputStream.close();
 			} catch (Exception ex) {
 
 			}
 		}
 
 		sampleEndpoint = "/xdi/facebook"; 
 	}
 
 	public ConnectServlet() {
 
 		this.graph = null;
 		this.facebookApi = null;
 	}
 
 	@Override
 	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		this.service(request, response);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		log.debug("Incoming GET request to " + request.getRequestURL() + ". Content-Type: " + request.getContentType() + ", Content-Length: " + request.getContentLength());
 
 		// start OAuth?
 
 		if ("Request Access Token!".equals(request.getParameter("submit"))) {
 
 			XDI3Segment userXri = XDI3Segment.create(request.getParameter("userXri"));
 			request.getSession().setAttribute("userXri", userXri);
 
 			try {
 
 				response.sendRedirect(this.getFacebookApi().startOAuth(request, null, userXri));
 				return;
 			} catch (Exception ex) {
 
 				request.setAttribute("error", ex.getMessage());
 			}
 		}
 
 		// revoke OAuth?
 
 		if ("Revoke Access Token!".equals(request.getParameter("submit"))) {
 
 			XDI3Segment userXri = XDI3Segment.create(request.getParameter("userXri"));
 			request.getSession().setAttribute("userXri", userXri);
 
 			try {
 
 				String accessToken = GraphUtil.retrieveAccessToken(this.getGraph(), userXri);
 				if (accessToken == null) throw new Exception("No access token in graph.");
 
 				this.getFacebookApi().revokeAccessToken(accessToken);
 
 				GraphUtil.removeAccessToken(this.getGraph(), userXri);
 
 				request.setAttribute("feedback", "Access token successfully revoked and removed from graph.");
 			} catch (Exception ex) {
 
 				request.setAttribute("error", ex.getMessage());
 			}
 		}
 
 		// error from OAuth?
 
 		if (request.getParameter("error") != null) {
 
 			String errorDescription = request.getParameter("error_description");
 			if (errorDescription == null) errorDescription = request.getParameter("error_reason");
 			if (errorDescription == null) errorDescription = request.getParameter("error");
 
 			request.setAttribute("error", "OAuth error: " + errorDescription);
 		}
 
 		// callback from OAuth?
 
 		if (request.getParameter("code") != null) {
 
 			XDI3Segment userXri = (XDI3Segment) request.getSession().getAttribute("userXri");
 
 			try {
 
				this.getFacebookApi().checkState(request.getParameterMap(), userXri);

				String accessToken = this.getFacebookApi().exchangeCodeForAccessToken(request.getRequestURL().toString(), request.getParameterMap());
 				if (accessToken == null) throw new Exception("No access token received.");
 
 				GraphUtil.storeAccessToken(this.getGraph(), userXri, accessToken);
 
 				request.setAttribute("feedback", "Access token successfully received and stored in graph.");
 			} catch (Exception ex) {
 
 				request.setAttribute("error", ex.getMessage());
 			}
 		}
 
 		// display results
 
 		request.setAttribute("resultFormat", XDIDisplayWriter.FORMAT_NAME);
 		request.setAttribute("writeContexts", null);
 		request.setAttribute("writeOrdered", "on");
 		request.setAttribute("writePretty", null);
 		request.setAttribute("input", sampleInput);
 		request.setAttribute("endpoint", request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/")) + sampleEndpoint);
 
 		request.getRequestDispatcher("/Connect.jsp").forward(request, response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		String resultFormat = request.getParameter("resultFormat");
 		String writeContexts = request.getParameter("writeContexts");
 		String writeOrdered = request.getParameter("writeOrdered");
 		String writePretty = request.getParameter("writePretty");
 		String input = request.getParameter("input");
 		String endpoint = request.getParameter("endpoint");
 		String output = "";
 		String stats = "-1";
 		String error = null;
 
 		Properties xdiResultWriterParameters = new Properties();
 
 		if ("on".equals(writeContexts)) xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_IMPLIED, "1");
 		if ("on".equals(writeOrdered)) xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_ORDERED, "1");
 		if ("on".equals(writePretty)) xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_PRETTY, "1");
 
 		XDIReader xdiReader = XDIReaderRegistry.getAuto();
 		XDIWriter xdiResultWriter = XDIWriterRegistry.forFormat(resultFormat, xdiResultWriterParameters);
 
 		MessageEnvelope messageEnvelope = null;
 		MessageResult messageResult = null;
 
 		long start = System.currentTimeMillis();
 
 		try {
 
 			// parse the message envelope
 
 			messageEnvelope = new MessageEnvelope();
 
 			xdiReader.read(messageEnvelope.getGraph(), new StringReader(input));
 
 			// send the message envelope and read result
 
 			XDIClient client = new XDIHttpClient(endpoint);
 
 			messageResult = client.send(messageEnvelope, null);
 
 			// output the message result
 
 			StringWriter writer = new StringWriter();
 
 			xdiResultWriter.write(messageResult.getGraph(), writer);
 
 			output = StringEscapeUtils.escapeHtml(writer.getBuffer().toString());
 		} catch (Exception ex) {
 
 			if (ex instanceof Xdi2ClientException) {
 
 				messageResult = ((Xdi2ClientException) ex).getErrorMessageResult();
 
 				// output the message result
 
 				if (messageResult != null) {
 
 					StringWriter writer2 = new StringWriter();
 					xdiResultWriter.write(messageResult.getGraph(), writer2);
 					output = StringEscapeUtils.escapeHtml(writer2.getBuffer().toString());
 				}
 			}
 
 			log.error(ex.getMessage(), ex);
 			error = ex.getMessage();
 			if (error == null) error = ex.getClass().getName();
 		}
 
 		long stop = System.currentTimeMillis();
 
 		stats = "";
 		stats += Long.toString(stop - start) + " ms time. ";
 		if (messageEnvelope != null) stats += Integer.toString(messageEnvelope.getMessageCount()) + " message(s). ";
 		if (messageEnvelope != null) stats += Integer.toString(messageEnvelope.getOperationCount()) + " operation(s). ";
 		if (messageResult != null) stats += Integer.toString(messageResult.getGraph().getRootContextNode().getAllStatementCount()) + " result statement(s). ";
 
 		// display results
 
 		request.setAttribute("resultFormat", resultFormat);
 		request.setAttribute("writeContexts", writeContexts);
 		request.setAttribute("writeOrdered", writeOrdered);
 		request.setAttribute("writePretty", writePretty);
 		request.setAttribute("input", input);
 		request.setAttribute("endpoint", endpoint);
 		request.setAttribute("output", output);
 		request.setAttribute("stats", stats);
 		request.setAttribute("error", error);
 
 		request.getRequestDispatcher("/Connect.jsp").forward(request, response);
 	}
 
 	public Graph getGraph() {
 
 		return this.graph;
 	}
 
 	public void setGraph(Graph graph) {
 
 		this.graph = graph;
 	}
 
 	public FacebookApi getFacebookApi() {
 
 		return this.facebookApi;
 	}
 
 	public void setFacebookApi(FacebookApi facebookApi) {
 
 		this.facebookApi = facebookApi;
 	}
 }
