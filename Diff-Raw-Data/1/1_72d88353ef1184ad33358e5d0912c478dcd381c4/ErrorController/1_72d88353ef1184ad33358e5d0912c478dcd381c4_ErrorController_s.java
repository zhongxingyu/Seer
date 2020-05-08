 package com.kittens.controller;
 
 import com.kittens.Utils;
 import com.kittens.view.ViewRenderer;
 
 import java.io.IOException;
 import java.lang.String;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 
 public class ErrorController extends BaseController {
 
 	// the version of this object
 	public static final long serialVersionUID = 0L;
 
 	/**
 	 * Process GET requests.
 	 */
 	@Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// ask for this response to not be cached
 		Utils.pleaseDontCache(response);
 		// map all the values
 		final HashMap<String, String> values = new HashMap<String, String>();
 		values.put("title", "Error 404 (Not Found)!!1");
 		// render the view
 		response.setContentType("text/html");
 		ViewRenderer.render(response, "error", values);
 	}
 
 }
