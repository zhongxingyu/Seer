 package com.admin.servlet;
 
 import java.io.IOException;
import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.admin.owlmanager.OntologyManager;
 /**
  * Servlet implementation class OntologyServlet
  */
 @WebServlet("/OntologyServlet")
 public class OntologyServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public OntologyServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	/**
 	 * This servlet does all the model generation and reasoning magic
 	 * TODO: this should call OntologyManager class
 	 * 
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		OntologyManager om = new OntologyManager();
 		boolean deleteResult = om.deleteOntology();
 		boolean loadResult = om.loadOntology();
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
 		if (deleteResult && loadResult) {
			writer.write("<h1 fontcolor=\"green\">OK</h1>");
 		} else {
			writer.write("<h1 fontcolot=\"red\">ERROR</h1>");
 		}
 	}
 }
