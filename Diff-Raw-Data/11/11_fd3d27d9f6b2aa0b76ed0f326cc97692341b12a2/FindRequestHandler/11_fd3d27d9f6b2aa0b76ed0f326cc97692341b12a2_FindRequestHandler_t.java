 package com.mymed.controller.core.requesthandler;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.pubsub.PubSubManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.utils.MLogger;
 
 /**
  * Servlet implementation class PubSubRequestHandler
  */
 public class FindRequestHandler extends AbstractRequestHandler {
 	/* --------------------------------------------------------- */
 	/* Attributes */
 	/* --------------------------------------------------------- */
 	private static final long serialVersionUID = 1L;
 
 	private PubSubManager pubsubManager;
 
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	/**
 	 * @throws ServletException
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public FindRequestHandler() throws ServletException {
 		super();
 		try {
 			pubsubManager = new PubSubManager();
 		} catch (final InternalBackEndException e) {
 			throw new ServletException("PubSubManager is not accessible because: " + e.getMessage());
 		}
 	}
 
 	/* --------------------------------------------------------- */
 	/* extends HttpServlet */
 	/* --------------------------------------------------------- */
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
 	IOException {
 
 		JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
 		try {
 			final Map<String, String> parameters = getParameters(request);
 			final RequestCode code = requestCodeMap.get(parameters.get("code"));
 			String application, predicate, user;
 			
 			// accessToken
 			if (!parameters.containsKey("accessToken")) {
 				throw new InternalBackEndException("accessToken argument is missing!");
 			} else {
 				tokenValidation(parameters.get("accessToken")); // Security Validation
 			}
 
 			switch (code) {
 			case READ : // GET
 				message.setMethod("READ");
 				if ((application = parameters.get("application")) == null) {
 					throw new InternalBackEndException("missing application argument!");
 				} else if ((predicate = parameters.get("predicate")) == null) {
 					throw new InternalBackEndException("missing predicate argument!");
 				}
 				if ((user = parameters.get("user")) != null) {
					System.out.println("************PREDICATE = "  + predicate);
 					final List<Map<String, String>> details =
 							pubsubManager.read(application, predicate, user);
 					if (details.isEmpty()) {
 						throw new IOBackEndException("no reslult found!", 404);
 					}
 					message.setDescription("Details found for Application: " + application + " User: " + user + " Predicate: " + predicate);
 					MLogger.getLog().info("Details found for Application: " + application + " User: " + user + " Predicate: " + predicate);
 					message.addData("details", getGson().toJson(details));
 				} else { // GET RESULTS
 					final List<Map<String, String>> resList =
 							pubsubManager.read(application, predicate);
 					if (resList.isEmpty()) {
 						throw new IOBackEndException("No reslult found for Application: " + application + " Predicate: " + predicate, 404);
 					}
 					message.setDescription("Results found for Application: " + application + " Predicate: " + predicate);
 					MLogger.getLog().info("Results found for Application: " + application + " Predicate: " + predicate);
 					message.addData("results", getGson().toJson(resList));
 				}
 				break;
 			default :
 				throw new InternalBackEndException("FindRequestHandler(" + code + ") not exist!");
 			}
 		} catch (final AbstractMymedException e) {
 			MLogger.getLog().info("Error in doGet operation");
 			MLogger.getDebugLog().debug("Error in doGet operation", e.getCause());
 			message.setStatus(e.getStatus());
 			message.setDescription(e.getMessage());
 		} 
 
 		printJSonResponse(message, response);
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
 			throws ServletException, IOException {
 		
 		JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
 		try {
 			final Map<String, String> parameters = getParameters(request);
 			final RequestCode code = requestCodeMap.get(parameters.get("code"));
 
 			// accessToken
 			if (!parameters.containsKey("accessToken")) {
 				throw new InternalBackEndException("accessToken argument is missing!");
 			} else {
 				tokenValidation(parameters.get("accessToken")); // Security Validation
 			}
 			
 			switch (code) {
 			case CREATE :
 			default :
 				throw new InternalBackEndException("FindRequestHandler(" + code + ") not exist!");
 			}
 
 		} catch (final AbstractMymedException e) {
 			MLogger.getLog().info("Error in doPost operation");
 			MLogger.getDebugLog().debug("Error in doPost operation", e.getCause());
 			message.setStatus(e.getStatus());
 			message.setDescription(e.getMessage());
 		} 
 
 		printJSonResponse(message, response);
 	}
 
 }
