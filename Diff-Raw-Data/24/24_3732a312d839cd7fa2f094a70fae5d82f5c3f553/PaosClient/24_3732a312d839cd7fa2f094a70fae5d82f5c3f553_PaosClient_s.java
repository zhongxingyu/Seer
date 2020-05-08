 /* ***************************************************************************
  * Copyright 2012 Carolina Lindqvist
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  * ***************************************************************************/
 
 package jettyClient.paosClient;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 
 import jettyClient.parser.MessageParser; // parse text to Envelope
 import jettyClient.parser.ParseHelper; // marshall response to envelope. 
 import jettyClient.parser.ValidateXML; // validates the SOAP envelope.
 import jettyClient.simpleClient.ClientExchange; // extends HttpContentExchange
 
 import net.shibboleth.utilities.java.support.xml.SerializeSupport;
 
 import org.eclipse.jetty.client.Address;
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.http.HttpHeaders;
 import org.eclipse.jetty.http.HttpMethods;
 import org.eclipse.jetty.http.HttpSchemes;
 import org.eclipse.jetty.http.MimeTypes;
 import org.eclipse.jetty.io.ByteArrayBuffer;
 import org.w3c.dom.Element;
 
 public class PaosClient {
 
 	HttpClient httpClient;
 
 	// ParserPool parserPool;
 
 	public PaosClient(HttpClient httpClient) {
 		// public PAOSHTTPClient() {
 		this.httpClient = httpClient;
 	}
 
 	/**
 	 * Send a GET request to the specified URL. Used when the client requests a
 	 * resource from a SP without being authenticated. The expected response is
 	 * a SOAP Envelope that contains an AuthnRequest.
 	 * 
 	 * @param endpoint
 	 * @return
 	 */
 	public ExchangeContent sendHttpGETRequest(URL endpoint,
 			ExchangeContent content) {
 
 		// Set up a connection to the SP.
 		ClientExchange serviceProviderExchange = getPAOSExchange(endpoint);
 
 		// Send GET request
 		serviceProviderExchange = exchangeContent(httpClient,
 				serviceProviderExchange);
 
 		// Store response (SOAP Envelope with an AuthnRequest)
 		content = storeResponse(serviceProviderExchange, content);
 
 		return content;
 	}
 
 	/**
 	 * Send a SOAP envelope to an endpoint. If a SOAP envelope is received as a
 	 * response, it will be stored in the content object. Any single cookie that
 	 * is sent along with a Response will also be stored in the content object.
 	 * The content object is returned, possibly unchanged.
 	 * 
 	 * @param endpoint
 	 * @param content
 	 * @return Returns an ExchangeContent object with a response.
 	 */
 	public ExchangeContent send(URL endpoint, ExchangeContent content) {
 
 		// Create a new POST exchange.
 		ClientExchange clientExchange = getPOSTExchange(endpoint);
 
 		// Fill POSTexchange with the envelope.
 		Element envelopeElement = ParseHelper.marshall(content
 				.getRequestEnvelope());
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		SerializeSupport.writeNode(envelopeElement, stream);
 
 		// Add content to the Exchange
 		clientExchange.setRequestContent(new ByteArrayBuffer(stream
 				.toByteArray()));
 
 		System.out.println("\nSent to " + clientExchange.getAddress().getHost()
 				+ clientExchange.getRequestURI() + "\n" + stream.toString());
 
 		// Add the cookie to the Exchange (if there is one)
 		if (!content.getCookieField().equals(""))
 			clientExchange.setRequestHeader(HttpHeaders.COOKIE,
 					content.getCookieField() + ";");
 
 		// Set realmResolver if there is one to set.
 		if (content.getRealmResolver() != null)
 			httpClient.setRealmResolver(content.getRealmResolver());
 
 		// Send exchange
 		clientExchange = exchangeContent(httpClient, clientExchange);
 
 		// exchangeContent() will return null when something fails.
 		if (clientExchange != null) {
 
 			// Inspect and store the response message.
 			content = storeResponse(clientExchange, content);
 
 			// Check if anything could be stored. React.
 
 		} else {
 			System.out.println("Could not send envelope.");
 		}
 		return content;
 	}
 
 	/**
 	 * Validate responseContent to see if it contained a SOAP Envelope. If it
 	 * did, store it in a ExchangeContent object.
 	 * 
 	 * @param clientExchange
 	 * @param content
 	 */
 	private ExchangeContent storeResponse(ClientExchange clientExchange,
 			ExchangeContent content) {
 
 		String response = "";
 		String cookieField = "";
 
 		try {
 			response = clientExchange.getResponseContent();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Check response status (200 = OK)
 		if (clientExchange.getResponseStatus() == 200) {
 			// If the response contains an envelope...
 			if (isEnvelope(response)) {
 
 				System.out.println("\nReceived from "
 						+ clientExchange.getAddress().getHost() + ":\n"
 						+ response);
 
 				// Extract + save an Envelope as EnvelopeParts from the
 				// response.
 				content.setResponseParts(MessageParser.parseMessage(response));
 
				// Check if the response contains a cookie.
				if (clientExchange.getResponseFields() != null)
					cookieField = clientExchange.getResponseFields()
							.getStringField("Set-Cookie");
 
				// Save the session cookie (from the SP).
				if (cookieField.isEmpty() == false)
					content.setCookieField(cookieField);
 
 			} else {
 				System.out.println("No SOAP Envelope received as response.");
 			}
 		} else {
 			System.out.println("HTTP Error \nStatus: "
 					+ clientExchange.getStatus() + " from "
 					+ clientExchange.getAddress().getHost());
 		}
 		return content;
 	}
 
 	/**
 	 * Generate a new POST exchange.
 	 * 
 	 * @param host
 	 * @param port
 	 * @param uri
 	 * @return
 	 */
 	private ClientExchange getPOSTExchange(URL url) {
 
 		ClientExchange exchange = null;
 
 		exchange = new ClientExchange();
 		exchange.setMethod(HttpMethods.POST);
 		exchange.setScheme(HttpSchemes.HTTPS_BUFFER); // Enable HTTPS
 
 		exchange.setAddress(new Address(url.getHost(), url.getPort()));
 		exchange.setRequestURI(url.getFile());
 
 		exchange.setRequestHeader(HttpHeaders.ACCEPT, MimeTypes.TEXT_HTML);
 
 		return exchange;
 	}
 
 	/**
 	 * Generate a GET request for a Service Provider. Ask for a resource and
 	 * receive an AuthnRequest in an Envelope.
 	 * 
 	 * @param host
 	 *            Host address
 	 * @param port
 	 *            Port number
 	 * @param uri
 	 *            Resource identifier
 	 * @return
 	 */
 	private ClientExchange getPAOSExchange(URL url) {
 
 		ClientExchange exchange = null;
 
 		String PAOS_header = "PAOS";
 		String acceptsPAOS = "application/vnd.paos+xml";
 
 		// Should use constant field values from opensaml library instead?
 		String supportsService = "\"urn:oasis:names:tc:SAML:2.0:profiles:SSO:ecp\"";
 		String supportsPaosV_1_1 = "ver=\"urn:liberty:paos:2003-08\"";
 
 		// Configure HttpExchange
 		Boolean cache_headers = true;
 		exchange = new ClientExchange(cache_headers);
 		exchange.setMethod(HttpMethods.GET);
 		exchange.setScheme(HttpSchemes.HTTPS_BUFFER);
 
 		// Set address & uri
 		exchange.setAddress(new Address(url.getHost(), url.getPort()));
 		exchange.setRequestURI(url.getFile());
 
 		// Set headers
 		exchange.setRequestHeader(HttpHeaders.ACCEPT, MimeTypes.TEXT_HTML
 				+ "; " + acceptsPAOS);
 		exchange.setRequestHeader(PAOS_header, supportsPaosV_1_1 + "; "
 				+ supportsService);
 
 		return exchange;
 	}
 
 	/**
 	 * Sends a contentExchange to a receiver, returns the response, whatever it
 	 * may be, as a text string (if STATUS_COMPLETED), or else null.
 	 * 
 	 * @param httpClient
 	 * @param clientExchange
 	 * @return
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws UnsupportedEncodingException
 	 */
 	private ClientExchange exchangeContent(HttpClient httpClient,
 			ClientExchange clientExchange) {
 
 		// Send clientExchange
 		try {
 			httpClient.send(clientExchange);
 		} catch (IOException e) {
 			System.out.println("Could not send message to "
 					+ clientExchange.getAddress().getHost());
 		}
 
 		// Wait until something is received.
 		int state = 0;
 
 		try {
 			state = clientExchange.waitForDone();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			System.out.println("ContentExchange was interrupted.");
 		}
 
 		if (state == ClientExchange.STATUS_COMPLETED) {
 			return clientExchange;
 		} else if (state == ClientExchange.STATUS_EXCEPTED)
 			System.out.println("Exception in ContextExchange");
 		else if (state == ClientExchange.STATUS_EXPIRED) // client.setTimeout(timeToExpire);
 			System.out.println("ContentExchange expired");
 		return null;
 	}
 
 	/**
 	 * Check if the message contains a SOAP envelope.
 	 * 
 	 * @param responseMessage
 	 * @return
 	 */
 	private boolean isEnvelope(String responseMessage) {
 
 		if (responseMessage != null)
 			if (ValidateXML.isValidEnvelope(new ByteArrayInputStream(
 					responseMessage.getBytes())))
 				return true;
 
 		System.out.println("Received instead of an envelope: "
 				+ responseMessage);
 		return false;
 	}
 
 }
