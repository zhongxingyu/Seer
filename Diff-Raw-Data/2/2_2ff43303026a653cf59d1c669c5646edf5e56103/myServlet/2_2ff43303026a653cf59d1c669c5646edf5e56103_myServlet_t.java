 package Servlets;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import Objects.DBManager;
 import Objects.Product;
 import Objects.ProductInCart;
 import captchas.CaptchasDotNet;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Jason
  */
 public class myServlet extends HttpServlet {
 
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
         try {
             if (request.getParameter("page").equals("login")) {
                 try {
                     String name = null;
                     String role = null;
                     // Load the driver
                     Class.forName("com.mysql.jdbc.Driver").newInstance();
 
                     // Connect to MySQL
                     Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
                     Statement statement = connection.createStatement();
 
                     // Search for the user
                     ResultSet resultSet = statement.executeQuery("SELECT * FROM users;");
                     while (resultSet.next()) {
                         if (resultSet.getString("username").equals(request.getParameter("username")) && resultSet.getString("password").equals(request.getParameter("password"))) {
                             name = resultSet.getString("name");
                             role = resultSet.getString("role");
                             break;
                         }
                     }
 
                     // Close connection to database
                     statement.close();
                     connection.close();
 
                     if (name == null) {
                         response.sendRedirect("loginFailed.jsp");
                     } else {
                         HttpSession session = request.getSession();
                         session.setAttribute("name", name);
 
                         if (role.equals("administrator")) {
                             response.sendRedirect("admin.jsp");
                         } else {
                             session.setAttribute("name", name);
                             session.setAttribute("cart", new ArrayList<ProductInCart>());
                            response.sendRedirect("mainmenu.jsp");
                         }
                     }
                 } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                     out.println(ex.toString());
                 }
 
             } else if (request.getParameter(
                     "page").equals("signup")) {
                 try {
                     // Construct the captchas object
                     // Use same settings as in query.jsp
                     CaptchasDotNet captchas = new captchas.CaptchasDotNet(request.getSession(true), "demo", "secret");
 
                     // Read the form values
                     String captcha = request.getParameter("captcha");
 
                     // Check captcha
                     switch (captchas.check(captcha)) {
                         case 's':
                             // Fail
                             response.sendRedirect("loginFailed.jsp");
                             break;
                         case 'm':
                             // Fail
                             response.sendRedirect("loginFailed.jsp");
                             break;
                         case 'w':
                             // Fail
                             response.sendRedirect("loginFailed.jsp");
                             break;
                         default:
                             // Success
 
                             // Load the driver
                             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
                             // Connect to MySQL
                             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
                             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(role, username, password, name, email) VALUES ( ?, ?, ?, ?, ? );");
 
                             // Add new user
                             preparedStatement.setString(1, "user");
                             preparedStatement.setString(2, request.getParameter("username"));
                             preparedStatement.setString(3, request.getParameter("password"));
                             preparedStatement.setString(4, request.getParameter("name"));
                             preparedStatement.setString(5, request.getParameter("email"));
                             preparedStatement.executeUpdate();
 
                             // Close connection to database
                             preparedStatement.close();
                             connection.close();
 
                             // Redirect to index.jsp
                             response.sendRedirect("index.jsp");
                     }
                 } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                     out.println(ex.toString());
                 }
             } else if (request.getParameter("page").equals("admin")) {
                 try {
                     if (request.getParameter("command").equals("delete")) {
                         // Load the driver
                         Class.forName("com.mysql.jdbc.Driver").newInstance();
 
                         // Connect to MySQL
                         Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
                         Statement statement = connection.createStatement();
 
                         // Delete the data
                         statement.execute("DELETE FROM users WHERE username = '" + request.getParameter("username") + "';");
 
                         // Redirect to admin.jsp
                         response.sendRedirect("admin.jsp");
                     } else {
                         request.getSession(false).setAttribute("username", request.getParameter("username"));
                         response.sendRedirect("editUser.jsp");
                     }
                 } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                     out.println(ex.toString());
                 }
             } else if (request.getParameter("page").equals("editUser")) {
                 try {
                     // Load the driver
                     Class.forName("com.mysql.jdbc.Driver").newInstance();
 
                     // Connect to MySQL
                     Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
                     PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET password=?, name=?, email=? WHERE username=?;");
 
                     // Update the data
                     preparedStatement.setString(1, request.getParameter("password"));
                     preparedStatement.setString(2, request.getParameter("name"));
                     preparedStatement.setString(3, request.getParameter("email"));
                     preparedStatement.setString(4, request.getSession(false).getAttribute("username").toString());
                     preparedStatement.executeUpdate();
 
                     // Redirect to admin.jsp
                     response.sendRedirect("admin.jsp");
                 } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                     out.println(ex.toString());
                 }
             } else if (request.getParameter(
                     "page").equals("buy")) {
                 ArrayList<ProductInCart> cart = (ArrayList<ProductInCart>) request.getSession(false).getAttribute("cart");
                 Product product = new DBManager().getProduct(request.getParameter("productID"));
 
                 Boolean exists = false;
                 for (ProductInCart p : cart) {
                     if (p.getProductID().equals(product.getProductID())) {
                         p.setAmount(p.getAmount() + Integer.parseInt(request.getParameter("amount")));
                         exists = true;
                     }
                 }
 
                 if (!exists) {
                     ProductInCart newProduct = new ProductInCart(product.getProductID(), product.getName(), product.getDescription(), product.getStock(), product.getPrice(), product.getPictureURL(), Integer.parseInt(request.getParameter("amount")));
                     cart.add(newProduct);
                 }
 
                 response.sendRedirect("shelf.jsp");
             } else if (request.getParameter(
                     "page").equals("delete")) {
                 ArrayList<ProductInCart> cart = (ArrayList<ProductInCart>) request.getSession(false).getAttribute("cart");
 
                 for (int i = 0; i < cart.size(); i++) {
                     if (cart.get(i).getProductID().equals(request.getParameter("productID"))) {
                         cart.remove(i);
                         break;
                     }
                 }
 
                 response.sendRedirect("checkout.jsp");
             } else if (request.getParameter(
                     "page").equals("editamount")) {
                 ArrayList<ProductInCart> cart = (ArrayList<ProductInCart>) request.getSession(false).getAttribute("cart");
 
                 for (int i = 0; i < cart.size(); i++) {
                     if (cart.get(i).getProductID().equals(request.getParameter("productID"))) {
                         Integer newamount = Integer.parseInt(request.getParameter("newamount"));
 
                         if (newamount == 0) {
                             cart.remove(i);
                         } else {
                             cart.get(i).setAmount(Integer.parseInt(request.getParameter("newamount")));
                         }
 
                         break;
                     }
                 }
 
                 response.sendRedirect("checkout.jsp");
             }
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
