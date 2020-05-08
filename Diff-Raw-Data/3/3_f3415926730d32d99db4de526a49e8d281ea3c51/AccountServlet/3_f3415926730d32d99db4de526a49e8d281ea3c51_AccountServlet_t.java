 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlet;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import orderprocess.business.Customer;
 import model.Security;
 /**
  *
  * @author plecl022
  */
 public class AccountServlet extends HttpServlet {
     
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
         
         // match url "~/signup" and "~/login"
         String userPath = request.getRequestURI().substring(request.getContextPath().length());
         String url = "/WEB-INF/views" + userPath + ".jsp";
         request.getRequestDispatcher(url).forward(request, response);
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
         
         String userPath = request.getRequestURI().substring(request.getContextPath().length());
         System.out.println("user path = " + userPath);
         
         String url = null;
         // create session 
         HttpSession session = request.getSession();
         // obtain security methods
         Security secure = new Security();
 
         // match url "~/login"
         if (userPath.equals("/login")) {
             
             /* Get all the data from the form */
             String email = request.getParameter( "email" );
             String password = request.getParameter( "password" );
             
             // get account by calling web service operations
             orderprocess.business.Customer customer = null;
             // encrypt password before sending throught soap
             String pass = null;
             try {
                 customer = getAccount(email, secure.encrypt(password));
                 pass = secure.decrypt(customer.getPassword());
                 customer.setPassword(pass);
                 System.out.println(pass);
             } catch (Exception ex) {
                 System.out.println("Fail secure encrypte");
                 Logger.getLogger(AccountServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             if (customer != null) {
                 session.setAttribute("customer", customer);
                 session.setAttribute("fname", customer.getFname());
                 session.setAttribute("lname", customer.getLname());
                 session.setAttribute("email", customer.getEmail());
                 // fill out order with customer
                 // orderprocess.business.Order order = new orderprocess.business.Order(); 
                 // order.setCustomer(customer);
 
                 //Message sent to the view
                 String mess = "You are now connected to your account";
                 session.setAttribute( "notice", mess );
                 
                 if (session.getAttribute("authRequired") != null && (Boolean)session.getAttribute("authRequired")) {
                     session.setAttribute("authRequired", null);                        
                         
                     // should return last page if user is prompted to login 
                     url = request.getContextPath() + (String)session.getAttribute("lastUserPath");
                     response.sendRedirect(url);
                 } else {
                     response.sendRedirect(request.getContextPath() + "/home");
                 }
             } else {
                 
                 //Message sent to the view
                 String mess = "Fail to login";
                 session.setAttribute( "error", mess );                
                 url = "/WEB-INF/views/login.jsp";
                 request.getRequestDispatcher(url).forward(request, response);
             }
             
         // match url "~/signup"
         } else if (userPath.equals("/signup")) {
             
             /* Get all the data from the form */
             /* USER DATA */
             String email = request.getParameter( "email" );
             String phone = request.getParameter("phone");
             String password = request.getParameter( "password" );
             String confirmation = request.getParameter( "confirmation" );
             String lname = request.getParameter( "lname" );
             String fname = request.getParameter( "fname" );
             
             /* ADRESS DATA */
             String city = request.getParameter("city");
             String street = request.getParameter( "street" );
             String province = request.getParameter( "province" );
             String zip = request.getParameter( "ZIP" );
             String country = request.getParameter( "country" );
             
             /** Check data integrity **/
             try {
                 /* CHECK ALL THE DATA */
                 checkEmail( email );
                 checkPassword( password, confirmation );
                 checkName( lname );
                 // (...)
             } catch (Exception e) {
                 
                 //Message sent to the view
                 String mess = e.toString().substring("java.lang.Exception: ".length());
                 session.setAttribute( "error", mess );
                 url = "/WEB-INF/views/signup.jsp";
                 request.getRequestDispatcher(url).forward(request, response);
                 return;
             }
             
             // construct paramters
             orderprocess.business.Address address = new orderprocess.business.Address();
             address.setStreet(street);
             address.setProvince(province);
             address.setCountry(country);
             address.setZip(zip);
             address.setCity(city);
             orderprocess.business.Customer customer = new orderprocess.business.Customer();
             customer.setFname(fname);
             customer.setLname(lname);
             customer.setEmail(email);
             customer.setAdress(address);
             customer.setPhone(phone);
             // encrypt password before sending throught soap
             try {
                 customer.setPassword(secure.encrypt(password));
             } catch (Exception ex) {
                 Logger.getLogger(AccountServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
             // create account by calling web service operations
             int customerId = createAccount(customer);
             if (customerId >= 0) {
                 customer.setId(customerId);
                 session.setAttribute("customer", customer);
                session.setAttribute("fname", customer.getFname());
                session.setAttribute("lname", customer.getLname());
                session.setAttribute("email", customer.getEmail());
                 
                 // fill out order with customer
                 // orderprocess.business.Order order = new orderprocess.business.Order(); 
                 // order.setCustomer(customer);
 
                 //Message sent to the view
                 String mess = "You have just created an account.";
                 session.setAttribute( "notice", mess );
                 
                 if (session.getAttribute("authRequired") != null && (Boolean)session.getAttribute("authRequired")) {
                     session.setAttribute("authRequired", null);
                     
                     // should return last page if user is prompted to signup 
                     url = request.getContextPath() + (String)session.getAttribute("lastUserPath");
                     response.sendRedirect(url);
                 } else {
                     response.sendRedirect(request.getContextPath() + "/home");
                 }
             } else {
                 
                 //Message sent to the view
                 String mess = "Fail to create an account.";
                 session.setAttribute( "error", mess );
                 url = "/WEB-INF/views/signup.jsp";
                 request.getRequestDispatcher(url).forward(request, response);
             }
         }
         else if(userPath.equals("/logout")){
            session.invalidate();
            session = request.getSession();
            
            //Message sent to the view
            String mess = "You've just logged out.";
            session.setAttribute( "notice", mess );
                 
            response.sendRedirect(request.getContextPath() + "/home");
         }
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
     
     
     /**
      * checkEmail
      */
     private void checkEmail( String email ) throws Exception {
         if ( email != null && email.trim().length() != 0 ) {
             if ( !email.matches( "([^.@]+)(\\.[^.@]+)*@([^.@]+\\.)+([^.@]+)" ) ) {
                 throw new Exception( "Email not valid." );
             }
         } else {
             throw new Exception( "Email not valid." );
         }
     }
     
     /**
      * Valid password
      */
     private void checkPassword( String password, String confirmation ) throws Exception{
         if (password != null && password.trim().length() != 0 && confirmation != null && confirmation.trim().length() != 0) {
             if (!password.equals(confirmation)) {
                 throw new Exception("Different passwords");
             } else if (password.trim().length() < 3) {
                 throw new Exception("Too short min 3 char).");
             }
         } else {
             throw new Exception("Please, enter your login and password");
         }
     }
     
     /**
      * Check name
      */
     private void checkName( String name ) throws Exception {
         if ( name != null && name.trim().length() < 3 ) {
             throw new Exception( "Name too short." );
         }
     }
 
     private static int createAccount(orderprocess.business.Customer customer) {
         orderprocess.business.OrderProcessService_Service service = new orderprocess.business.OrderProcessService_Service();
         orderprocess.business.OrderProcessService port = service.getOrderProcessServicePort();
         return port.createAccount(customer);
     }
 
     private static Customer getAccount(java.lang.String name, java.lang.String pass) {
         orderprocess.business.OrderProcessService_Service service = new orderprocess.business.OrderProcessService_Service();
         orderprocess.business.OrderProcessService port = service.getOrderProcessServicePort();
         return port.getAccount(name, pass);
     }  
 }
