 package com.mymed.controller.core.requesthandler;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.JsonSyntaxException;
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * Servlet implementation class UsersRequestHandler
  */
 public class ProfileRequestHandler extends AbstractRequestHandler {
 	/* --------------------------------------------------------- */
 	/* Attributes */
 	/* --------------------------------------------------------- */
 	private static final long serialVersionUID = 1L;
 
 	private ProfileManager profileManager;
 
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	/**
 	 * @throws ServletException 
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public ProfileRequestHandler() throws ServletException {
 		super();
 		try {
 			this.profileManager = new ProfileManager();
 		} catch (InternalBackEndException e) {
 			throw new ServletException("ProfileManager is not accessible because: " + e.getMessage());
 		}
 	}
 
 	/* --------------------------------------------------------- */
 	/* extends HttpServlet */
 	/* --------------------------------------------------------- */
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		try {
 			/** Get the parameters */
 			Map<String, String> parameters = getParameters(request);
 
 			/** Get the method code */
 			RequestCode code = requestCodeMap.get(parameters.get("code"));
 
 			/** handle the request */
 			String id;
 			if((id = parameters.get("id"))== null){
 				handleError(new InternalBackEndException("missing id argument!"), response);
 				return;
 			}
 			switch (code) {
 			case READ:
 				MUserBean userBean;
 				try {
 					userBean = profileManager.read(id);
 					setResponseText(getGson().toJson(userBean));
 				} catch (IOBackEndException e) {
 					handleError(e, response);
 					return;
 				}
 				break;
 			case DELETE:
 				profileManager.delete(id);
 				System.out.println("\nINFO: User " + id
 						+ " deleted!");
 				break;
 			default:
 				handleError(new InternalBackEndException("ProfileRequestHandler.doGet(" + code + ") not exist!"), response);
 				return;
 			}
 			super.doGet(request, response);
 		} catch (InternalBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 			return;
		} catch (IOBackEndException e) {
			e.printStackTrace();
			handleError(e, response);
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		try {
 			/** Get the parameters */
 			Map<String, String> parameters = getParameters(request);
 
 			/** Get the method code */
 			RequestCode code = requestCodeMap.get(parameters.get("code"));
 
 			/** handle the request */
 			String user;
 			if((user = parameters.get("user"))== null){
 				handleError(new InternalBackEndException("missing user argument!"), response);
 				return;
 			}
 			switch (code) {
 			case CREATE:
 				MUserBean userBean = null;
 				try {
 					System.out.println("USER:\n\n" + user);
 					userBean = getGson().fromJson(user,
 							MUserBean.class);
 				} catch (JsonSyntaxException e) {
 					handleError(new InternalBackEndException("user jSon format is not valid"), response);
 					return;
 				}
 				System.out.println("\nINFO: trying to create a new user:\n" + userBean);
 				userBean = profileManager.create(userBean);
 				System.out.println("\nINFO: User created!");
 				setResponseText(getGson().toJson(userBean));
 				break;
 			case UPDATE:
 				try {
 					userBean = getGson().fromJson(user,
 							MUserBean.class);
 				} catch (JsonSyntaxException e) {
 					handleError(new InternalBackEndException("user jSon format is not valid"), response);
 					return;
 				}
 				System.out.println("\nINFO: trying to update user:\n" + userBean);
 				userBean = profileManager.update(userBean);
 				setResponseText(getGson().toJson(userBean));
 				System.out.println("\nINFO: User updated!");
 				break;
 			default:
 				handleError(new InternalBackEndException("ProfileRequestHandler.doPost(" + code + ") not exist!"), response);
 				return;
 			}
 			super.doPost(request, response);
 		} catch (InternalBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 			return;
 		} catch (IOBackEndException e) {
 			e.printStackTrace();
 			handleError(e, response);
 		}
 	}
 }
