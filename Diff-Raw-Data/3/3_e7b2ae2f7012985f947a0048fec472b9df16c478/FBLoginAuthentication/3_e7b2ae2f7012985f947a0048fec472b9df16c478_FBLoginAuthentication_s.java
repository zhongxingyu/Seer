 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.uni.sushilkumar.geodine.auth;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 /**
  *
  * @author sushil
  */
 public class FBLoginAuthentication extends HttpServlet {
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException, ParseException {
         response.setContentType("text/html;charset=UTF-8");
         try {
            String code=request.getParameter("code"),auth_code="",responseStr,jsonResponse="",jsonResponse1="",id="";
         int index=0,index1=0,index2=0;
             response.setContentType("text/html");
             URL authCode=new URL("https://graph.facebook.com/oauth/access_token?client_id=326194097435770&redirect_uri=http://projects-sushilkumar.rhcloud.com/geodine/FBLoginAuthentication&client_secret=3756f2742028c8f792704d008adc2c0d&code="+code);
             BufferedReader in=new BufferedReader(new InputStreamReader(authCode.openStream()));
             while((responseStr=in.readLine())!=null)
                 auth_code+=responseStr;
             in.close();
             index=auth_code.indexOf("access_token=");
             auth_code=auth_code.substring(index+13);
             index=auth_code.indexOf("&expires");
             auth_code=auth_code.substring(0,index);
             HttpSession session=request.getSession(true);
             URL nameURL=new URL("https://graph.facebook.com/me?access_token="+auth_code);
             BufferedReader br=new BufferedReader(new InputStreamReader(nameURL.openStream()));
             JSONParser parser=new JSONParser();
             Object  json=parser.parse(br);
             JSONObject obj=(JSONObject)json;
             String email=(String) obj.get("email");
             String name=(String) obj.get("name");
             session.setAttribute("user-name", email);
             session.setAttribute("name", name);
             response.sendRedirect("http://projects-sushilkumar.rhcloud.com/geodine");
 
             
        } finally {            
             
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
         try {
             processRequest(request, response);
         } catch (ParseException ex) {
             Logger.getLogger(FBLoginAuthentication.class.getName()).log(Level.SEVERE, null, ex);
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
         try {
             processRequest(request, response);
         } catch (ParseException ex) {
             Logger.getLogger(FBLoginAuthentication.class.getName()).log(Level.SEVERE, null, ex);
         }
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
