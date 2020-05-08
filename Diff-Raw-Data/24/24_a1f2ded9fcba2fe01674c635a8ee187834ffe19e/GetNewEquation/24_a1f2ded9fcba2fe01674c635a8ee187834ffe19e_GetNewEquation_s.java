 package org.benetech.mde.servlet;
 
 
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.benetech.mde.compute.GraphDescriber;
 
 //@WebServlet("/MdeDescribeEquation")
 public class GetNewEquation extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	public GetNewEquation() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 	    response.setHeader("Cache-Control", "no-cache");
 	    response.setHeader("Pragma", "no-cache");
 	    response.setContentType("text/html");
 	    String equation = request.getParameter("equation");
 	    String[] pnames = request.getParameterValues("pname");
 	    String[] pvalues = request.getParameterValues("pvalue");
 	    
 	    for (int i=0; i < pnames.length; i++) 
 	    	System.out.println("pname: "+pnames[i]+" pvalue: "+pvalues[i]);
 	    
 	    PrintWriter out = response.getWriter();
 	    
 	    GraphDescriber describer = new GraphDescriber(equation);
 	    
 	    String newEquation = describer.getNewEquation(pnames, pvalues);
		System.out.println("NEW EQUATION IS: "+newEquation);
 	    
 	  //The only output for production is respJson
//	    out.print(respJson);
 
 	    //HTML out for testing only.  Comment out for production.
	    out.println("<html>");
	    out.println("<head></head><body>");
	    out.println("<form method='get' action='/Mde-Web-Service/GetEquationDescription'>");
	    out.println("Enter an equation: ");
	    out.println("<input type='text' name='equation'>");
	    out.println("<input type='submit'>");
	    out.println("</form>");
	    out.println("</head></body>");
	    out.println("</html>");
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		doGet(request, response);
 	}
 	
 
 }
