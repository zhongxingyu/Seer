 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package controller;
 
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Antón García Dosil
  */
 @WebServlet(name = "ControllerServlet",
             loadOnStartup = 1,
             urlPatterns = {"/Home",
                            "/Cars",
                            "/General",
                            "/Motorbikes",
                            "/UserProfile",
                            "/Register",
                            "/RegisterAction",
                            "/TermsAndConditions",
                            "/About",
                            "/AdminPage",
                            "/PostAd",
                            "/EditProfile",
                           "/UserProfile"
                            })
 public class ControllerServlet extends HttpServlet {
 
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
 
         String userPath = request.getServletPath();
 
         /**
          * SAMPLE CODE
          */
         // if category page is requested
         if (userPath.equals("/category")) {
             // TODO: Implement category request
 
         // if cart page is requested
         } else if (userPath.equals("/toMotorQuery")) {
             // TODO: Implement motorbike page request
             
 
         // if checkout page is requested
         } 
 
         // use RequestDispatcher to forward request internally
         String url = userPath + ".jsp";
 
         try {
             request.getRequestDispatcher(url).forward(request, response);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
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
 
         String userPath = request.getServletPath();
         if(userPath.equals("/Home")){
             userPath = "/index.jsp";
         }
         // if toVehicleQuery action is called
         else if(userPath.equals("/Cars")) {
             // TODO: Implement toVehicleQuery action
             userPath = "/QueryController?opt=Cars";
         }else if (userPath.equals("/Motorbikes")) {
             // TODO: Implement motorbike page request
             
              userPath = "/QueryController?opt=Motorbikes";
         // if checkout page is requested
         } else if (userPath.equals("/General")) {
 
                 // TODO: Implement general page request                
                 
                 userPath = "/QueryController?opt=General";
             // if checkout page is requested
 
         } else if (userPath.equals("/UserProfile")) {
             // TODO: Implement motorbike page request
             userPath = "/UserProfileController";
 
              //userPath = "/query";
         // if checkout page is requested
         } else if (userPath.equals("/Register")) {
             // TODO: Implement motorbike page request
             userPath = "/registration.jsp";
 
              //userPath = "/query";
         // if checkout page is requested
         } else if (userPath.equals("/About")) {
             // TODO: Implement motorbike page request
             userPath = "/aboutus.jsp";
 
              //userPath = "/query";
         // if checkout page is requested
         }else if (userPath.equals("/AdminPage")) {
             // TODO: Implement motorbike page request
             userPath = "/admin.jsp";
 
              //userPath = "/query";
         // if checkout page is requested
         }else if (userPath.equals("/TermsAndConditions")) {
             // TODO: Implement motorbike page request
             userPath = "/termsandconditions.jsp";
 
              //userPath = "/query";
         // if checkout page is requested
         }else if (userPath.equals("/PostAd")) {
             // TODO: Implement motorbike page request
             userPath = "/postAd.jsp";
 		
 	}else if (userPath.equals("/EditProfile")) {
             // TODO: Implement motorbike page request
             userPath = "/EditProfileController";
             
 	}else if (userPath.equals("/RegisterAction")) {
             // TODO: Implement motorbike page request
             userPath = "/RegistrationController";
 	}
 
         // use RequestDispatcher to forward request internally
         String url = userPath;
 
         try {
             request.getRequestDispatcher(url).forward(request, response);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
 }
