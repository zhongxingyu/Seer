 package org.bandex;
 
import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Scanner;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class IndexServlet
  */
 @WebServlet("/index.html")
 public class IndexServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public IndexServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
     
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		OutputStream out = response.getOutputStream();
		FileInputStream fis;
 		if (Detector.isMobileBrowser(request))
			fis = new FileInputStream("/home/andre/workspace/BandexView/WebContent/main-mobile.html");
 		else
			fis = new FileInputStream("/home/andre/workspace/BandexView/WebContent/main.html");
		Scanner in = new Scanner(fis);
 		String line;
 		while(in.hasNextLine()){
 			line = in.nextLine();
 			out.write(line.getBytes());
 		}
 		in.close();
 		out.close();
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
