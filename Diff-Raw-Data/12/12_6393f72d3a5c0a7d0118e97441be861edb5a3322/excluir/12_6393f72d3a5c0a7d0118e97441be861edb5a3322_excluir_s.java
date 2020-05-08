 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import manager.DiretorioManager;
 import manager.JWICEsession;
 
 /**
  *
  * @author Marcel
  */
 @WebServlet(name = "excluir", urlPatterns = {"/excluir"})
 public class excluir extends HttpServlet {
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         JWICEsession sessao = new JWICEsession(request, response);
         if (sessao.falhou()) {
<<<<<<< HEAD
             System.gc();
             return;
         }
         System.gc();
         DiretorioManager dir = new DiretorioManager(sessao.getUsuarioLogado().getLogin());
         File arquivo = dir.getFile((String) request.getParameter("arquivo"));
         if(arquivo!=null&&arquivo.exists()){
             if(arquivo.delete())
                 sessao.setAviso("Arquivo apagado");
             else{
                 Integer i = 100;
                 while(!arquivo.delete()){
                     i--;
                     if(i==0)
                         break;
                 }
                 if(arquivo.canWrite())
                     sessao.setAviso("Arquivo apagavel");
                 if(arquivo.exists())
                     sessao.setAviso(i.toString().concat("Arquivo não apagado".concat((String) request.getParameter("arquivo"))));
                 else
                     sessao.setAviso("Arquivo apagado");
             }
             response.sendRedirect("/logado");
         }else{
             sessao.setAviso("Arquivo invalido");
             response.sendRedirect("/logado");
         }
         System.gc();
=======
            return;
        }
        DiretorioManager dir = new DiretorioManager(sessao.getUsuarioLogado().getLogin());
        File arquivo = dir.getFile((String) request.getParameter("arquivo"));
        if(arquivo.exists()){
            arquivo.delete();
            sessao.aviso("Arquivo apagado");
        }
>>>>>>> f0bc1aced80f6d1ffb291569aa0233f0b1ffeab8
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
