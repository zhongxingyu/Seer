 package org.sgnn7.service;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 
 @WebServlet("/RedditService/*")
 public class RedditService extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static String jsonPage1;
 	private static String jsonPage2;
 
 	public RedditService() {
 		try {
 			jsonPage1 = IOUtils.toString(getClass().getResourceAsStream("/resources/json/json1.json"));
 			jsonPage2 = IOUtils.toString(getClass().getResourceAsStream("/resources/json/json2.json"));
 		} catch (IOException e) {
 			log("Could not read initial state", e);
 		}
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// log("Processing request...");
 		String parameters = "Params: ";
 		for (String key : request.getParameterMap().keySet()) {
 			parameters += key + "=" + request.getParameter(key) + ", ";
 		}
 
 		log(parameters);
 
 		response.setStatus(HttpServletResponse.SC_OK);
 
 		byte[] returnValue = null;
 		if (!isImageRequest(request.getParameterMap())) {
 			String returnJson = "";
 			if (!isSubredditRequest(request.getPathInfo())) {
 				returnJson = isFirstPage(request.getParameterMap()) ? jsonPage1 : jsonPage2;
 			} else {
 				returnJson = IOUtils.toString(getClass().getResourceAsStream("/resources/json/reddits.json"));
 			}
 			String address = request.getLocalAddr() + ":" + request.getLocalPort()
 					+ this.getServletContext().getContextPath() + this.getServletContext().getContextPath();
 			log("Address: " + address);
 
 			returnValue = returnJson.replaceAll("SERVER_ADDRESS", address).getBytes();
 		} else {
 			String imageName = request.getParameter("image");
 			response.setContentType("image/png");
 			returnValue = IOUtils.toByteArray(getClass().getResourceAsStream("/resources/thumbnails/" + imageName));
 		}
 
 		response.getOutputStream().write(returnValue);
 		response.flushBuffer();
 	}
 
 	private boolean isSubredditRequest(String path) {
 		boolean isSubredditRequest = false;
 		log("Path: " + path);
		if (path.equalsIgnoreCase("/reddits")) {
 			isSubredditRequest = true;
 		}
 		return isSubredditRequest;
 	}
 
 	private boolean isImageRequest(Map<String, String[]> parameterMap) {
 		boolean isImage = false;
 		boolean isImageParameterSpecified = parameterMap.keySet().contains("image")
 				&& parameterMap.get("image").length > 0;
 
 		if (isImageParameterSpecified) {
 			isImage = true;
 		}
 		return isImage;
 	}
 
 	private boolean isFirstPage(Map<String, String[]> parameterMap) {
 		boolean firstPage = true;
 		boolean isAfterParameterSpecified = parameterMap.keySet().contains("after")
 				&& parameterMap.get("after").length > 0;
 
 		if (isAfterParameterSpecified && parameterMap.get("after")[0].equals("t1_page1")) {
 			firstPage = false;
 		}
 		// log("first page = " + firstPage);
 		return firstPage;
 	}
 
 	@Override
 	public void log(String msg) {
 		System.err.println(msg);
 	}
 }
