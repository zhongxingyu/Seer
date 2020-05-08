 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Challenge2;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author David
  */
 public class RectangleCalculatorController extends HttpServlet {
     private static final String destination = "/rectangleAreaJSP.jsp";
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
 //        PrintWriter out = response.getWriter();
         
         String strLength = request.getParameter("length");
         String strWidth = request.getParameter("width");
         double length = Double.valueOf(strLength);
         double width = Double.valueOf(strWidth);
         
         RectangleModel rect = new RectangleModel();
         
         double area = rect.getArea(length, width);
         
        request.setAttribute("area", area);
        
         RequestDispatcher dispatcher = 
                 getServletContext().getRequestDispatcher(destination);
               dispatcher.forward(request, response);  
         
                 
 //        try {
 //            /* TODO output your page here. You may use following sample code. */
 //            out.println("<html>");
 //            out.println("<head>");
 //            out.println("<title>Servlet RectangleCalculatorController</title>");            
 //            out.println("</head>");
 //            out.println("<body>");
 //            out.println("<h1>Servlet RectangleCalculatorController at " + request.getContextPath() + "</h1>");
 //            out.println("</body>");
 //            out.println("</html>");
 //        } finally {            
 //            out.close();
 //        }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
