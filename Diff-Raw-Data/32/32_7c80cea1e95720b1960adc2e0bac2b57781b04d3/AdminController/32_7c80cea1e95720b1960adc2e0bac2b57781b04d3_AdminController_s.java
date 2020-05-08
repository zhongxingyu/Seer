 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import model.CRUDAction;
 import static model.CRUDAction.Create;
 import static model.CRUDAction.Delete;
 import static model.CRUDAction.NoAction;
 import static model.CRUDAction.Read;
 import static model.CRUDAction.Update;
 import model.DBMenuService;
 import model.IMenuService;
 import model.MenuItem;
 
 /**
  *
  * @author jrankin2
  */
 @WebServlet(name = "AdminController", urlPatterns = {"/AdminController"})
 public class AdminController extends HttpServlet {
 
     private static final String ADD_UPDATE_PAGE = "admin/modifyMenuItem.jsp";
     private static final String VIEW_PAGE = "admin/index.jsp";
     private IMenuService menuService;
 
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
         RequestDispatcher view = request.getRequestDispatcher(VIEW_PAGE);
         response.setContentType("text/html;charset=UTF-8");
 
         if(menuService == null){
             menuService = new DBMenuService();
         }
         
         try{
             
         }catch(Exception e){
             
         }
         
 
 
 
 
 
         String action = request.getParameter("a");
         MenuItem item = itemFromRequest(request);
         request.setAttribute("item", item);
         request.setAttribute("a", action);
         
         switch (CRUDAction.toCRUDAction(action)) {
             case Create:
                item = MenuItem.createFromRequest(request);
                menuService.addMenuItem(item);
                view = request.getRequestDispatcher(ADD_UPDATE_PAGE);
                 break;
             case Read:
                 view = request.getRequestDispatcher(VIEW_PAGE);
                 break;
             case Update:
                if (item != null) {
                    item.updateFromRequest(request);
                    menuService.updateMenuItem(item);
                    view = request.getRequestDispatcher(ADD_UPDATE_PAGE);
                } else {
                    //item not found - error?
                     view = request.getRequestDispatcher(ADD_UPDATE_PAGE);
                 }
                 break;
             case Delete:
                 if(item != null){
                     menuService.deleteMenuItem(item);
                 }
                 view = request.getRequestDispatcher(VIEW_PAGE);
                 break;
             case NoAction:
                 view = request.getRequestDispatcher(VIEW_PAGE);
                 break;
         }
         request.setAttribute("menuItems", menuService.getMenu());
 
         view.forward(request, response);
 
     }
 
     private MenuItem itemFromRequest(HttpServletRequest request) {
         String sId = request.getParameter("id");
         MenuItem item = null;
         if (sId != null && !sId.isEmpty()) {
             item = menuService.findMenuItemById(sId);
         }
         return item;
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
         protected void doGet
         (HttpServletRequest request, HttpServletResponse response
         )
             throws ServletException
         , IOException {
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
         protected void doPost
         (HttpServletRequest request, HttpServletResponse response
         )
             throws ServletException
         , IOException {
             processRequest(request, response);
         }
 
         /**
          * Returns a short description of the servlet.
          *
          * @return a String containing servlet description
          */
         @Override
         public String getServletInfo
         
             () {
         return "Short description";
         }// </editor-fold>
     }
