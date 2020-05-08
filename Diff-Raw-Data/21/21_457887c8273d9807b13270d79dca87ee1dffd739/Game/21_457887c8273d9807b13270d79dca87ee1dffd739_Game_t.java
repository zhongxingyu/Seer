 package fr.uha.ensisa.brundubreil.servlet;
 
 import fr.uha.ensisa.brundubreil.model.Gameplay;
 import fr.uha.ensisa.brundubreil.model.Grid;
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 @WebServlet(name = "Game", urlPatterns = {"/Game"})
 public class Game extends HttpServlet {
     
     private GameBean gameBean;
     
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         HttpSession session =  request.getSession(true);
         gameBean = (GameBean) session.getAttribute("gameBean");
         
         if (request.getParameter("initGrid") != null && request.getParameter("initGrid").equals("true"))
         {
             gameBean.setGrid(new Grid());
             gameBean.setIsComputerWinner(false);
             gameBean.setIsDraw(false);
             gameBean.setIsHumanPlayerWinner(false);
//            session.setAttribute("gameBean", gameBean);
             
             if (!gameBean.isHumanPlayerBegin())
                 Gameplay.doComputerPlay(gameBean.getGrid());
             
             getServletContext().getRequestDispatcher("/jeu.jsp").forward(request, response);
         }
         
         Grid grid = gameBean.getGrid();
         
        boolean columnNotFull = false;
         // Make the human player play
        columnNotFull |= Gameplay.doHumanPlayerPlay(grid, Integer.parseInt(request.getParameter("column")));
         
        if (columnNotFull)
         { // Only if the user did not complete a column
             if (grid.isHumanPlayerWinner()) {
                 gameBean.setIsHumanPlayerWinner(true);
             } else {
                 // Make the computer play
                columnNotFull |= Gameplay.doComputerPlay(grid);
                 if (grid.isComputerWinner()) {
                     gameBean.setIsComputerWinner(true);
                 }
             }
             // Check if there is a draw
             if (grid.isGridFull()) {
                 gameBean.setIsDraw(true);
             }
         }
         getServletContext().getRequestDispatcher("/jeu.jsp").forward(request, response);
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
     }
     // </editor-fold>
 }
