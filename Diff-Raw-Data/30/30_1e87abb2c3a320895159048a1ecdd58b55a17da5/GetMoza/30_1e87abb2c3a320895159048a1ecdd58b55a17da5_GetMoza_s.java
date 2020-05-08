 /*
  =============
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package abelymiguel.miralaprima;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URISyntaxException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
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
 
         String country_code = null;
         country_code = request.getParameter("country_code");
 
         String json_str;
         JSONArray jsonArray = new JSONArray(this.searchInDB(country_code));
         json_str = jsonArray.toString();
 
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
 
    private ArrayList<HashMap<String, Object>> searchInDB(String country_code) {
 
         ArrayList<HashMap<String, Object>> respuestaJson = new ArrayList<HashMap<String, Object>>();
         try {
            
             ResultSet rs;
 
            if (country_code == null) {
                rs = Utils.getConnection().createStatement().executeQuery("SELECT url_prima, provider FROM photos WHERE approved = 1");
 
                 while (rs.next()) {
                     HashMap<String, Object> objetoJson = new HashMap<String, Object>();
                     String url = rs.getString("url_prima");
                     String provider = rs.getString("provider");
                    objetoJson.put(url, provider);
                     respuestaJson.add(objetoJson);
                 }
             } else {
                rs = Utils.getConnection().createStatement().executeQuery("SELECT url_prima, provider FROM photos WHERE approved = 1 AND country_code='" + country_code + "'");
 
                 while (rs.next()) {
                     HashMap<String, Object> objetoJson = new HashMap<String, Object>();
                     String url = rs.getString("url_prima");
                     String provider = rs.getString("provider");
                    objetoJson.put(url, provider);
                     respuestaJson.add(objetoJson);
                 }
             }
 
            rs.close();
         } catch (SQLException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
         } catch (URISyntaxException ex) {
             Logger.getLogger(GetMoza.class.getName()).log(Level.SEVERE, null, ex);
         }
         return respuestaJson;
     }
 }
