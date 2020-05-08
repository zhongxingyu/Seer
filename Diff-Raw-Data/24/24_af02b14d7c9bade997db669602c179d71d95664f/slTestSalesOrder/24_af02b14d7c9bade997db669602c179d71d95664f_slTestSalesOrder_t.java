 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Servlets;
 
 import restaurante.Utilities.SingletonGson;
 import com.google.gson.Gson;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import restaurante.Beans.Authentication;
 import restaurante.Beans.SalesOrder;
 import restaurante.Beans.SalesOrderItems;
 import restaurante.Controller.AbstractController;
 import restaurante.Controller.BranchesController;
 import restaurante.Controller.CustomersController;
 import restaurante.Controller.EmployeesController;
 import restaurante.Controller.MenuController;
 import restaurante.Controller.ProductsController;
 import restaurante.Controller.TablesController;
 /**
  *
  * @author Alan
  */
 public class slTestSalesOrder extends HttpServlet {
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         
         String token = request.getParameter("tkn");
         HttpSession session = request.getSession(true);
         if(token == null) {
             token = (String) session.getAttribute("token");
         }   
         
         Gson gson = SingletonGson.getInstance();
         
         //Session example
         session.setAttribute("token", token);
         Authentication authentication = new Authentication();
         authentication.setDate(new Date());
         authentication.setUserToken(token);
         authentication.setStatus("Success");
         try {
             SalesOrder saleOrder = new SalesOrder();
             
             saleOrder.setBranch(new BranchesController().findById(1));
             
             saleOrder.setCustomer(new CustomersController().findById(1));
             saleOrder.setTable(new TablesController().findById(1));
             saleOrder.setEmployee(new EmployeesController().findById(1));
             
             saleOrder.setPaymentMethod(SalesOrder.PaymentMethod.MONEY);
             saleOrder.setTotalValue(2);
             saleOrder.setNumberOfPersons(4);
             saleOrder.setDate(new Date());
            saleOrder.setStatus(SalesOrder.Status.OPENED);
             SalesOrderItems item = new SalesOrderItems();
             
             item.setAmount(4);
             item.setPrice(6);
             item.setSaleable(new ProductsController().findById(1));
             
             SalesOrderItems item2 = new SalesOrderItems();
             
             item2.setAmount(5);
             item2.setPrice(2);
            item2.setSaleable(new MenuController().findById(1));
             
             SalesOrderItems[] items = new SalesOrderItems[2];
             items[0] = item;
             items[1] = item2;
             saleOrder.setItems(items);
             
             authentication.setData(saleOrder);
             
             AbstractController.validateAuthentication(authentication);
             
             out.print(gson.toJson(authentication));
            
             if(authentication.getStatus().equals("Success")) {
                 request.setAttribute("json", gson.toJson(authentication));
                RequestDispatcher rd = request.getRequestDispatcher("/salesorder");
                 rd.forward(request, response);
             }
         } catch (Exception ex) {
             authentication.setStatus("Fail");
             authentication.setData(ex.getMessage());
             out.print(gson.toJson(authentication));
         } finally {
             out.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
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
      * Handles the HTTP <code>POST</code> method.
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
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
