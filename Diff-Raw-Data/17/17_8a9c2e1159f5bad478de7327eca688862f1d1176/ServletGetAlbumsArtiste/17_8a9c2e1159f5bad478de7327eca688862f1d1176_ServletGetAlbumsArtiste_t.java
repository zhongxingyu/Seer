 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageController;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import packageBusiness.Business;
 import packageException.AlbumException;
 import packageModel.Album;
 
 /**
  *
  * @author Emilien
  */
 public class ServletGetAlbumsArtiste extends HttpServlet {
 
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
         
         Business bu = new Business();
         
         try
         {
             Integer idAlbum = Integer.parseInt(request.getParameter("id"));
             ArrayList<Album> album = bu.getAlbumsArtiste(idAlbum);
             RequestDispatcher rd = request.getRequestDispatcher("artiste.jsp");          
            request.setAttribute("album", album);
            request.setAttribute("idArtiste", album.get(0).getIdArtiste());
             rd.forward(request, response);    
             
         }
         catch(AlbumException ex)
         {
             RequestDispatcher rd = request.getRequestDispatcher("erreur.jsp");
             request.setAttribute("reponse", ex);            
             rd.forward(request, response);                   
         }
         //getAlbumsArtiste
        
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
