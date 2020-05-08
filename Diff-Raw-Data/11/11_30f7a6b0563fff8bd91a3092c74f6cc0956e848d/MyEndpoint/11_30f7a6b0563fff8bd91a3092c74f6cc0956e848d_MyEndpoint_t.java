 package com.thoughtress.jsp.gen;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Arrays;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class MyEndpoint{
 
 	public static void doGet(HttpServletRequest request, HttpServletResponse response){
 		PrintWriter pw = null;
 		try {
 			pw = response.getWriter();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	    pw.println("<h1>Served by doGet</h1>");
 	}
 	public static void doPost(HttpServletRequest request, HttpServletResponse response){
 		StringBuffer data = new StringBuffer();
 		  String line = null;
 		  try {
 		    BufferedReader reader = request.getReader();
 		    while ((line = reader.readLine()) != null)
 		      data.append(line);
 		  } catch (Exception e) { /*report an error*/ }
 
 	    PrintWriter pw = null;
 		try {
 			pw = response.getWriter();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		//parse request using the correct MessageFormatter
 		Request req = null;
 		if (Arrays.asList(MyFormatter.getTypes()).contains(request.getContentType())){
 			req = MyFormatter.parseToRequest(data.toString());
		} else {
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
								   "Unsupported Content Type");
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
 	    //System.out.println("Method: " + req.getMethod() + "\n");
 	    FunctionProvider func = null;
 	    if (MyFunctionProvider.match(req)){
 	    	func = new MyFunctionProvider();
 	    } else {
 			try {
 				response.sendError(HttpServletResponse.SC_NOT_FOUND,
 								   "Requested Method Not Found");
				return;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}    
 	    if (func != null){
 	    	Response resp = func.process(req);
 		    String ret = MyFormatter.parseToFormat(resp);
 		    pw.println(ret);
 	    }
 	}
 }
