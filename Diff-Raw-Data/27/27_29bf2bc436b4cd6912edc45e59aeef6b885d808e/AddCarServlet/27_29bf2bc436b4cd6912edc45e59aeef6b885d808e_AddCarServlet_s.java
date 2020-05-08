 package main;
 
 import handler.AraclarHandler;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 @WebServlet("/caradder")
 public class AddCarServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddCarServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Araclar arac = new Araclar();
 		arac.setMarka(request.getParameter("marka"));
		arac.setKullanici(request.getParameter("model"));
		arac.setPlaka(request.getParameter("yakit"));
		arac.setDurum(request.getParameter("fiyat"));
 		AraclarHandler handler = new AraclarHandler();
 		handler.AracEkle(arac);
 		request.getRequestDispatcher("index.jsp").forward(request, response);
 	}
 
 }
