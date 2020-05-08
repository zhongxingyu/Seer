 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package abelymiguel.miralaprima;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.jdom.JDOMException;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  *
  * @author refusta
  */
 public class GetMoza extends HttpServlet {
 
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
     protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = null;
         try {
             out = response.getWriter();
         } catch (IOException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
         }
         response.setContentType("text/javascript;charset=UTF-8");
 
         String country_code;
         country_code = request.getParameter("country_code");
 
         JSONObject jsonObject;
         JSONArray jsonArray;
         String json_str = null;
 
         if (country_code != null) {
 //            jsonObject = new JSONObject(this.getCountry(country_code));
 //            json_str = jsonObject.toString();
         } else {
             jsonObject = new JSONObject(this.searchInDB());
             json_str = jsonObject.toString();
         }
 
 
 
         String jsonpCallback = request.getParameter("callback");
         if (jsonpCallback != null) {
             out.write(jsonpCallback + "(" + json_str + ")");
         } else {
             out.println(json_str);
         }
         out.close();
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
 
     private HashMap<String, String> searchInDB() {
 
         HashMap<String, String> respuestaJson = new HashMap<String, String>();
         try {
             String confFilePath = getServletContext().getRealPath("/")
                     + "WEB-INF" + File.separator + "dbconf.xml";
            ResultSet rs = DBConnect.getInstance(confFilePath).doQuery("SELECT url_prima FROM primas WHERE approved = 1");
 
             while (rs.next()) {
                 String url = rs.getString("url_prima");
                 String provider = rs.getString("provider");
                 respuestaJson.put(url, provider);
             }
             rs.close();
         } catch (JDOMException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
         }
         return respuestaJson;
     }
 }
