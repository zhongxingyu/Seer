 package mta.devweb.bitcoinbuddy.controller;
 
 import javax.servlet.http.HttpSession;
 
 import mta.devweb.bitcoinbuddy.controller.services.UserService;
 import mta.devweb.bitcoinbuddy.model.beans.User;
 
 public class UserActionHandler {
 	private HttpSession session;
 	private UserService userService;
 
 	public UserActionHandler(HttpSession session) {
 		this.session = session;
 		this.userService = new UserService();
 	}
 
 	public GenericResponse login(String userName, String password) {
 		String userId = (String) session.getAttribute("userId");
 		GenericResponse response = new GenericResponse();
 
 		if (userId != null) {
 			response.setMessage("user already logged in");
 		} else if (nullOrEmpty(userName) || nullOrEmpty(password)) {
 			response.setMessage("missing user name or password");
 		} else {
 			response = userService.login(userName, password);
 			if (response.getIsSuccess()) {
 				session.setAttribute("userId",
 						String.valueOf(((User) response.getData()).getId()));
 			}
 		}
 		return response;
 	}
 
 	public GenericResponse getUser() {
 		String userId = (String) session.getAttribute("userId");
 		GenericResponse response = new GenericResponse();
 
 		if (userId == null) {
 			response.setMessage("user isn't logged in");
 		} else {
 			response = userService.getUser(Integer.valueOf(userId));
 		}
 		return response;
 	}
 
 	public GenericResponse getUsersByHistoryCount(Integer minimumCommands) {
 		String userId = (String) session.getAttribute("userId");
 		GenericResponse response = new GenericResponse();
 
 		if (userId == null) {
 			response.setMessage("user isn't logged in");
 		} else if (minimumCommands == null) {
 			response.setMessage("invalid parameters");
 		} else {
 			response = userService.getUsersByHistoryCount(minimumCommands);
 		}
 		return response;
 	}
 
 	public GenericResponse signOut() {
 		String userId = (String) session.getAttribute("userId");
 		GenericResponse response = new GenericResponse();
 
 		if (userId == null) {
 			response.setMessage("user isn't logged in");
 		} else {
 			session.removeAttribute("userId");
 			response.setIsSuccess(true);
 		}
 		return response;
 	}
 
 	public GenericResponse subscribe(String firstName, String lastName,
 			String userName, String password) {
 		String userId = (String) session.getAttribute("userId");
 		GenericResponse response = new GenericResponse();
 
 		if (userId != null) {
 			response.setMessage("user logged in");
 		} else if (nullOrEmpty(firstName) || nullOrEmpty(lastName)
 				|| nullOrEmpty(userName) || nullOrEmpty(password)) {
 			response.setMessage("invalid parameters");
 		} else {
 			response = userService.subscribe(firstName, lastName, userName,
 					password);
 		}
 		return response;
 	}
 
 	public GenericResponse updateSubscriber(String firstName, String lastName,
 			String password) {
 		String userId = (String) session.getAttribute("userId");
 		GenericResponse response = new GenericResponse();
 
 		firstName = handleEmptyParam(firstName);
		lastName = handleEmptyParam(firstName);
		password = handleEmptyParam(firstName);
 
 		if (userId == null) {
 			response.setMessage("user isn't logged in");
 		} else if (firstName == null && lastName == null && password == null) {
 			response.setMessage("invalid parameters");
 		} else {
 			response = userService.updateSubscriber(Integer.valueOf(userId),
 					firstName, lastName, password);
 		}
 		return response;
 	}
 
 	public GenericResponse unsubscribe() {
 		String userId = (String) session.getAttribute("userId");
 		GenericResponse response = new GenericResponse();
 
 		if (userId == null) {
 			response.setMessage("user isn't logged in");
 		} else {
 			response = userService.deleteUser(Integer.valueOf(userId));
 			if (response.getIsSuccess()) {
 				session.removeAttribute("userId");
 			}
 		}
 		return response;
 	}
 
 	public GenericResponse getUnsupported() {
 		return new GenericResponse(false, "method unsupported", null);
 	}
 
 	private String handleEmptyParam(String param) {
 		return param != null && param.isEmpty() ? null : param;
 	}
 
 	private Boolean nullOrEmpty(String param) {
 		return param == null || param.isEmpty();
 	}
 
 	public HttpSession getSession() {
 		return session;
 	}
 
 	public void setSession(HttpSession session) {
 		this.session = session;
 	}
 }
