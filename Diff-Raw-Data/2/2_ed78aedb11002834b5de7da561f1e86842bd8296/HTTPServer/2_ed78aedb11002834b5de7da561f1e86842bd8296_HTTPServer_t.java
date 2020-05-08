 /**
  * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * 	  disclaimer.
  * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
  * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
  * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
  * 	  products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package p2p;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.URLDecoder;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.jboss.netty.handler.codec.http.HttpServerCodec;
 
 import lupos.datastructures.queryresult.QueryResult;
 import lupos.endpoint.server.format.CSVFormatter;
 import lupos.endpoint.server.format.Formatter;
 import lupos.endpoint.server.format.HTMLFormatter;
 import lupos.endpoint.server.format.JSONFormatter;
 import lupos.endpoint.server.format.PlainFormatter;
 import lupos.endpoint.server.format.TSVFormatter;
 import lupos.endpoint.server.format.XMLFormatter;
 import lupos.engine.evaluators.CommonCoreQueryEvaluator;
 import lupos.engine.operators.singleinput.federated.BitVectorFilterFunction;
 import luposdate.evaluators.P2PIndexQueryEvaluator;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 @SuppressWarnings("restriction")
 public class HTTPServer {
 
 	public static P2PIndexQueryEvaluator evaluator;
 	public final static int port = 9998;
 
 	// enable or disable logging into console
 	public static boolean log = false;
 
 	private final static Map<String, Formatter> registeredFormatter = Collections
 			.synchronizedMap(new HashMap<String, Formatter>());
 
 	private final static Map<String, HttpHandler> registeredhandler = Collections
 			.synchronizedMap(new HashMap<String, HttpHandler>());
 
 	private static HTMLForm htmlForm = new StandardHTMLForm();
 
 	public static void registerFormatter(final Formatter formatter) {
 		HTTPServer.registeredFormatter.put(formatter.getKey().toLowerCase(),
 				formatter);
 		HTTPServer.registeredFormatter.put(formatter.getName().toLowerCase(),
 				formatter);
 	}
 
 	public static Map<String, Formatter> getRegisteredFormatters() {
 		return HTTPServer.registeredFormatter;
 	}
 
 	public static void registerHandler(final String context,
 			final HttpHandler httpHandler) {
 		HTTPServer.registeredhandler.put(context, httpHandler);
 	}
 
 	public static HTMLForm getHTMLForm() {
 		return HTTPServer.htmlForm;
 	}
 
 	public static void setHTMLForm(final HTMLForm htmlForm) {
 		HTTPServer.htmlForm = htmlForm;
 	}
 
 	/**
 	 * HTTP Request Example
 	 * http://localhost:8080/sparql?query=PREFIX+rdf%3A%3Chttp
 	 * %3A%2F%2Fwww.w3.org
 	 * %2F1999%2F02%2F22-rdf-syntax-ns%23%3E+SELECT+*+WHERE%7B
 	 * +%3Fs+rdf%3Atype+%3Fo.+%7D&format=application%2Fsparql-results%2Bxml for
 	 * query PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT *
 	 * WHERE{?s rdf:type ?o. }
 	 */
 
 	public static void initAndStartServer(P2PIndexQueryEvaluator ev) {
 		evaluator = ev;
 		try {
 			final String localHost = InetAddress.getLocalHost().getHostName();
			System.out.println("http endpoint started: "
 					+ localHost + ":" + port);
 			HTTPServer.startServer();
 		} catch (Exception e) {
 			System.err.println(e);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * register the standard formatters and contexts of the server...
 	 */
 	static {
 		HTTPServer.registerStandardFormatter();
 		HTTPServer.registerStandardContexts();
 	}
 
 	public static void registerStandardFormatter() {
 		HTTPServer.registerFormatter(new XMLFormatter());
 		HTTPServer.registerFormatter(new PlainFormatter());
 		HTTPServer.registerFormatter(new JSONFormatter());
 		HTTPServer.registerFormatter(new CSVFormatter());
 		HTTPServer.registerFormatter(new TSVFormatter());
 		HTTPServer.registerFormatter(new HTMLFormatter(false));
 		HTTPServer.registerFormatter(new HTMLFormatter(true));
 	}
 
 	public static void registerStandardContexts() {
 		HTTPServer.registerHandler("/sparql", new SPARQLHandler(
 				new SPARQLExecutionImplementation()));
 		HTTPServer.registerHandler("/", new HTMLFormHandler());
 	}
 
 	public static void startServer() {
 		try {
 			HttpServer server = HttpServer.create(new InetSocketAddress(8080),
 					0);
 
 			for (Entry<String, HttpHandler> entry : HTTPServer.registeredhandler
 					.entrySet()) {
 				server.createContext(entry.getKey(), entry.getValue());
 			}
 
 			server.setExecutor(null);
 			server.start();
 		} catch (Exception e) {
 			System.err.println(e);
 			e.printStackTrace();
 		}
 	}
 
 	public static String getResponse(HttpExchange t) throws IOException {
 		final String requestMethod = t.getRequestMethod();
 		String response;
 		if (requestMethod.equalsIgnoreCase("POST")) {
 			final InputStream bodyStream = t.getRequestBody();
 			StringBuilder builder = new StringBuilder();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					bodyStream, "UTF-8"));
 			String readLine;
 			boolean firstTime = true;
 			while ((readLine = reader.readLine()) != null) {
 				if (firstTime) {
 					firstTime = false;
 				} else {
 					builder.append("\n");
 				}
 				builder.append(readLine);
 			}
 			response = builder.toString();
 		} else if (requestMethod.equalsIgnoreCase("GET")) {
 			response = t.getRequestURI().getRawQuery();
 		} else {
 			response = "";
 		}
 		return response;
 	}
 
 	public static interface SPARQLExecution {
 		public void execute(final String queryParameter,
 				final Formatter formatter, final HttpExchange t)
 				throws IOException;
 	}
 
 	public static class SPARQLExecutionImplementation implements
 			SPARQLExecution {
 		public void execute(final String queryParameter,
 				final Formatter formatter, final HttpExchange t)
 				throws IOException {
 			try {
 				synchronized (HTTPServer.evaluator) { // avoid any inference of
 														// several queries in
 														// parallel!
 					System.out.println("Evaluating query:\n" + queryParameter);
 					QueryResult queryResult = (HTTPServer.evaluator instanceof CommonCoreQueryEvaluator) ? ((CommonCoreQueryEvaluator) HTTPServer.evaluator)
 							.getResult(queryParameter, true)
 							: HTTPServer.evaluator.getResult(queryParameter);
 					final String mimeType = formatter.getMIMEType(queryResult);
 					System.out
 							.println("Done, sending response using MIME type "
 									+ mimeType);
 					t.getResponseHeaders().add("Content-type", mimeType);
 					t.getResponseHeaders().add("Transfer-encoding", "chunked");
 					t.sendResponseHeaders(200, 0);
 					OutputStream os = t.getResponseBody();
 					if (HTTPServer.log) {
 						os = new OutputStreamLogger(os);
 					}
 					formatter.writeResult(os,
 							HTTPServer.evaluator.getVariablesOfQuery(),
 							queryResult);
 					os.close();
 				}
 				return;
 			} catch (Error e) {
 				System.err.println(e);
 				e.printStackTrace();
 				t.getResponseHeaders().add("Content-type", "text/plain");
 				final String answer = "Error:\n" + e.getMessage();
 				System.out.println(answer);
 				HTTPServer.sendString(t, answer);
 				return;
 			} catch (Exception e) {
 				System.err.println(e);
 				e.printStackTrace();
 				t.getResponseHeaders().add("Content-type", "text/plain");
 				final String answer = "Error:\n" + e.getMessage();
 				System.out.println(answer);
 				HTTPServer.sendString(t, answer);
 				return;
 			}
 		}
 	}
 
 	public static class SPARQLHandler implements HttpHandler {
 
 		private final SPARQLExecution sparqlExecution;
 
 		public SPARQLHandler(final SPARQLExecution sparqlExecution) {
 			super();
 			this.sparqlExecution = sparqlExecution;
 			BitVectorFilterFunction.register();
 		}
 
 		private final static String format = "format=";
 		private final static String query = "query=";
 
 		public void handle(HttpExchange t) throws IOException {
 			System.out.println("\n-> Receiving request from: "
 					+ t.getRequestHeaders().get("Host"));
 			String response = HTTPServer.getResponse(t);
 			String[] responseParts = response.split("[&]");
 			if (responseParts.length > 0) {
 				// first check whether or not a format is given (default is XML
 				// as defined by W3C)
 				String formatParameter = HTTPServer.getParameter(responseParts,
 						format, "XML");
 				Formatter formatter = registeredFormatter.get(formatParameter
 						.toLowerCase());
 				if (formatter == null) {
 					t.getResponseHeaders().add("Content-type", "text/plain");
 					final String answer = "Bad Request: format "
 							+ formatParameter + " not supported";
 					System.out.println(answer);
 					HTTPServer.sendString(t, answer);
 					return;
 				}
 				// now look for a query parameter
 				String queryParameter = getParameter(responseParts, query);
 				if (queryParameter != null) {
 					this.sparqlExecution.execute(queryParameter, formatter, t);
 				} else {
 					t.getResponseHeaders().add("Content-type", "text/plain");
 					final String answer = "Bad Request: query parameter missing";
 					System.out.println(answer);
 					HTTPServer.sendString(t, answer);
 					return;
 				}
 			}
 			HTTPServer.htmlForm.sendHTMLForm(t);
 		}
 	}
 
 	public static class HTMLFormHandler implements HttpHandler {
 		public void handle(HttpExchange t) throws IOException {
 			HTTPServer.htmlForm.sendHTMLForm(t);
 		}
 	}
 
 	protected static String getParameter(String[] responseParts,
 			String parameter) throws UnsupportedEncodingException {
 		for (String item : responseParts) {
 			if (item.startsWith(parameter)) {
 				return URLDecoder.decode(item.substring(parameter.length()),
 						"UTF-8");
 			}
 		}
 		return null;
 	}
 
 	protected static String getParameter(String[] responseParts,
 			String parameter, String defaultValue)
 			throws UnsupportedEncodingException {
 		final String result = HTTPServer.getParameter(responseParts, parameter);
 		if (result != null) {
 			return result;
 		} else {
 			return defaultValue;
 		}
 	}
 
 	protected static void sendString(final HttpExchange t, final String toSend)
 			throws IOException {
 		t.sendResponseHeaders(200, toSend.length());
 		OutputStream os = t.getResponseBody();
 		os.write(toSend.getBytes());
 		os.close();
 	}
 
 	public static interface HTMLForm {
 		public void sendHTMLForm(final HttpExchange t) throws IOException;
 	}
 
 	public static class StandardHTMLForm implements HTMLForm {
 
 		private static String HTML_FORM_1 = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
 				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n"
 				+ "<head>\n <title>LUPOSDATE SPARQL Endpoint</title>\n</head>\n"
 				+ "<body>\n <h1>LUPOSDATE SPARQL Endpoint</h1>\n\n"
 				+ " <form method=\"get\" action=\"sparql\">\n  <p>Type in your SPARQL query:<br/>\n   "
 				+ "<textarea name=\"query\" cols=\"50\" rows=\"10\">SELECT * WHERE { ?s ?p ?o. } LIMIT 10</textarea>\n  </p>\n"
 				+ "  <p>\n   Result Format:<br/>\n   <select name=\"format\" size=\"1\">\n   ";
 		private static String HTML_FORM_2 = "</select>\n  </p>\n  <p>\n   <input type=\"submit\" value=\" Submit Query \"/>\n  </p>\n "
 				+ "</form>\n</body>";
 
 		private static String HTML_OPTION_1 = " <option value=\"";
 		private static String HTML_OPTION_2 = "\">";
 		private static String HTML_OPTION_3 = "</option>\n   ";
 
 		public void sendHTMLForm(final HttpExchange t) throws IOException {
 			final StringBuilder toSend = new StringBuilder(
 					StandardHTMLForm.HTML_FORM_1);
 			for (final Formatter formatter : HTTPServer
 					.getRegisteredFormatters().values()) {
 				toSend.append(StandardHTMLForm.HTML_OPTION_1);
 				toSend.append(formatter.getKey());
 				toSend.append(StandardHTMLForm.HTML_OPTION_2);
 				toSend.append(formatter.getName());
 				toSend.append(StandardHTMLForm.HTML_OPTION_3);
 			}
 			toSend.append(StandardHTMLForm.HTML_FORM_2);
 			String html = toSend.toString();
 			HTTPServer.sendString(t, html);
 		}
 	}
 
 	public static class OutputStreamLogger extends OutputStream {
 
 		private final OutputStream piped;
 
 		public OutputStreamLogger(final OutputStream piped) {
 			this.piped = piped;
 		}
 
 		@Override
 		public void write(int b) throws IOException {
 			if (b >= 0) {
 				for (char c : Character.toChars(b)) {
 					System.out.print(c);
 				}
 			}
 			this.piped.write(b);
 		}
 
 		@Override
 		public void close() throws IOException {
 			this.piped.close();
 		}
 	}
 }
