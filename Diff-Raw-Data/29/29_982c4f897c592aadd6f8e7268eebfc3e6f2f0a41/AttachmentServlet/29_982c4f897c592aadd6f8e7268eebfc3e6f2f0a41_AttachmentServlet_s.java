 package com.silverpeas.mobile.server.servlets;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 import com.stratelia.webactiv.beans.admin.OrganizationController;
 import com.stratelia.webactiv.beans.admin.UserFull;
 
 @SuppressWarnings("serial")
 public class AttachmentServlet extends HttpServlet {
 
 	private OrganizationController organizationController = new OrganizationController();
 
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
 		String id = request.getParameter("id");
 		String instanceId = request.getParameter("instanceId");
 		String lang = request.getParameter("lang");
		String userId = request.getParameter("userId");
 		UserFull userF = organizationController.getUserFull(userId);

 		String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/silverpeas/services/documents/" + instanceId + "/document/" + id + "/content/" + lang;
 		getFile(url, response, userF.getToken());
 		response.getOutputStream().flush();
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	public static void getFile(String host, HttpServletResponse response, String token) {
 		InputStream input = null;
 
 		try {
 
 			HttpClient client = new HttpClient();
 			HttpMethod method = new GetMethod(host);
 			method.addRequestHeader("X-Silverpeas-Session", token);
 			// String t = "SilverAdmin:SilverAdmin";
 			// method.addRequestHeader("Authorization", "Basic " +
 			// Base64Utils.toBase64(t.getBytes()));
 
 			client.executeMethod(method);
 
 			input = method.getResponseBodyAsStream();
 
 			byte[] buffer = new byte[1024];
 			int read;
 			while ((read = input.read(buffer)) > 0) {
 				response.getOutputStream().write(buffer, 0, read);
 			}
 		} catch (IOException e) {
 			System.out.println("Error while trying to download the file.");
 			e.printStackTrace();
 		}
 	}
 }
