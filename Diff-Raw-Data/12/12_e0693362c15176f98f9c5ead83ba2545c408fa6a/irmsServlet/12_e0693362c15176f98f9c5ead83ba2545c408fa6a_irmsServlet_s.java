 package servlet;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 
 import FBMS.entity.RestaurantEntity;
 import FBMS.session.IndReservationSessionBeanRemote;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Set;
 import javax.ejb.EJB;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author lionetdd
  */
 
 
 @WebServlet(urlPatterns = {"/irmsServlet", "/irmsServlet/*"})
 public class irmsServlet extends HttpServlet {
    @EJB
     private IndReservationSessionBeanRemote indReservationSessionBean;
   
    
     private ArrayList data=null;
   
     
 
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
     
     
     @Override
     public void init(){
         System.out.println("irmsSERVLET: init()");
     }
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         System.out.println("irmsSERVLET: processRequest()");
         
        /* response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();*/
         try {
             RequestDispatcher dispatcher;
             ServletContext servletContext = getServletContext();
             
             String temp = request.getServletPath();
             
             String page = request.getPathInfo();
             page = page.substring(1);
             System.out.println(page);
             
 
             if("restaurantSearch".equals(page)){
                 
                 System.out.println("Current page is restaurant!");
                 //data = searchRestaurant(request);
                 System.out.println("data search has been performed and result has been returned by bean");
                 //request.setAttribute("data",data);
                 System.out.println("data has been returned");
                 request.getRequestDispatcher("/restaurantSearch.jsp").forward(request, response);
 
             } 
             else if ("MakeReservation".equals(page)){
                 data=makeReservation(request);
                 request.setAttribute("data", data);
             }
             else if ("restaurant".equals(page)){
                 System.out.println("***restaurant page***");
                 request.getRequestDispatcher("/restaurant.jsp").forward(request, response);
 
             }
             else if ("home".equals(page))
             {
                 System.out.println("***home page***");
                 request.getRequestDispatcher("/home.jsp").forward(request, response);
 
             }
             else{
                 System.out.println("other page");
             }
 //          
 //             
 //            dispatcher=servletContext.getNamedDispatcher(page);
 //            System.out.println("dispatcher set up");
 //            System.out.println(dispatcher);
 //            if(dispatcher==null){
 //                dispatcher=servletContext.getNamedDispatcher("Error");
 //            }
 //            System.out.println("Before push content");
 //            dispatcher.forward(request, response);
 //            System.out.println("After push content");
             
         }catch (Exception e){
             System.out.println(e);
             log("Exception in irmsServlet.processRequest()");
             //System.out.println(e);
         }
     }
 
     
     private ArrayList makeReservation(HttpServletRequest request) throws ParseException{
         DateFormat formatter =new SimpleDateFormat("dd/MM/yy");
         ArrayList al=new ArrayList();
         Date indReservationDateTime=formatter.parse(request.getParameter("indReservationDateTime"));
         
         return al;
         
     }
             
             /* TODO output your page here. You may use following sample code. */
 
               
          /* TODO output your page here. You may use following sample code. */
 
             /*
             out.println("<!DOCTYPE html>");
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Servlet irmsServlet</title>");            
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Servlet irmsServlet at " + request.getContextPath() + "</h1>");
             out.println("</body>");
             out.println("</html>");
             * */
         /*} finally {            
             out.close();
         }*/
     
     
 
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
    
     
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     
     
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
     private ArrayList searchRestaurant(HttpServletRequest request) {
         
         ArrayList al = new ArrayList();
         String restNeighbourhood = request.getParameter("restNeighbourhood");
         String restTypeOfPlace   = request.getParameter("restTypeOfPlace");
         String restCuisine       = request.getParameter("restCuisine");
         String keyword           = request.getParameter("keyword");
         
         RestaurantEntity re = indReservationSessionBean.createRestaurantEntity(restNeighbourhood, restTypeOfPlace, restCuisine, keyword);
 
         Set <RestaurantEntity> res =   indReservationSessionBean.searchRestaurant(re);  
 
 
         
         al.addAll(res);
         al.add("Restaurant Search has been performed!");
         
         System.out.println("irmsServlet: restaurant search has been completed!");
 
 
 
         return al;
         //To change body of generated methods, choose Tools | Templates.
     }
 
     
     
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
     
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
     
     @Override
     public void destroy(){
         System.out.println("irmsServlet: destroy()");
     }
     
     
 }
