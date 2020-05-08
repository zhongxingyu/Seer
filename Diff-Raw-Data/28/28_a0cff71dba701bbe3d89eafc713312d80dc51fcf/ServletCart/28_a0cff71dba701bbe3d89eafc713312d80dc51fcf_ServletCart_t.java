 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageController;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.Iterator;
 import java.util.Map;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import packageException.CommandeException;
 import packageModel.AlbumCart;
 import packageModel.Utilisateur;
 
 /**
  *
  * @author Emilien
  */
 public class ServletCart extends HttpServlet {
 
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
        
         try {
             double tot = 0;
             HttpSession sess = request.getSession();
             Utilisateur util = (Utilisateur)sess.getAttribute("user");
             
             for (Iterator iter = util.getHasmMapPanier().entrySet().iterator(); iter.hasNext();) //Vérification des quantités dans la hashmap
             {
                Map.Entry data = (Map.Entry)iter.next();
                AlbumCart album = (AlbumCart)data.getValue();
                if(album.getQte()<1)
                {
                    throw new CommandeException("qteInvalid");
                }
                else
                {
                    if(album.getPromo()) {
                        tot+= album.getQte() * album.getPrixPromo();
                     }
                    else {
                        tot+=album.getQte() * album.getPrix();
                     }
                }
             }
             DecimalFormat myFormatter = new DecimalFormat("#.##");
             String outputTot = myFormatter.format(tot);
             
             RequestDispatcher rd = request.getRequestDispatcher("cart.jsp");
             request.setAttribute("total",outputTot);
             rd.forward(request, response);
         }
         catch(CommandeException ex)
         {
             RequestDispatcher rd = request.getRequestDispatcher("erreur.jsp");
             request.setAttribute("reponse",ex);
             rd.forward(request, response);
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
