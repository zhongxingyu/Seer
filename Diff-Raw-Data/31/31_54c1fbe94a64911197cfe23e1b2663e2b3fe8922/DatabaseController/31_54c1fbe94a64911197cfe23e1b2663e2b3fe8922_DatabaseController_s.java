 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import model.Item;
 import model.OrderService;
 
 /**
  *
  * @author ncummins
  */
 @WebServlet(name = "DatabaseController", urlPatterns = {"/DatabaseController"})
 public class DatabaseController extends HttpServlet {
 
    private static final String RESULT_PAGE = "/receipt.jsp";
     private static final String SUBMIT_BTN = "submit";
 
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
         PrintWriter out = response.getWriter();
         String orderEvent = request.getParameter(SUBMIT_BTN);
        try {
             HttpSession session = request.getSession();
             Object objService = session.getAttribute("orderService");
 
             OrderService orderService = null;
             if (objService == null) {
                 orderService = new OrderService();
                 session.setAttribute("orderService", orderService);
             } else {
                 orderService = (OrderService) objService;
             }
 
             List<Item> menuList = orderService.getMenuList();
             List<Item> orderList = orderService.getOrderList();
 
             String[] orderItems = null;
 
             if (orderEvent == null) {
                 // nothing to do, it's a new order
             } else if (orderEvent.startsWith("Place")) {
                 
                 orderItems = request.getParameterValues("menuItems");
                 orderList.clear();
                 for (String item : orderItems) {
                     for (Item menuItem : menuList) {
                         if (menuItem.getName().equals(item)) {
                             orderList.add(menuItem);
                             break;
                         }
                     }
                 }
 
                 orderService.setOrderList(orderList);
                 orderService.placeOrder();
                 
             }
 
 
             request.setAttribute("menuList", menuList);
             request.setAttribute("orderList", orderList);
 
 
             RequestDispatcher view =
                     request.getRequestDispatcher(RESULT_PAGE);
             view.forward(request, response);

        } finally {
            out.close();
        }
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
