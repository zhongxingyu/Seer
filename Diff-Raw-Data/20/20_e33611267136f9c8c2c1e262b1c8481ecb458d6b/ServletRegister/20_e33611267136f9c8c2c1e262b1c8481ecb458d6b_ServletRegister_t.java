 package net.yace.web.servlets;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import net.yace.entity.Yrank;
 import net.yace.entity.Yuser;
 import net.yace.facade.YuserFacade;
 import net.yace.web.utils.ServicesLocator;
 import net.yace.web.utils.YaceUtils;
 
 /**
  *
  * @author Scohy Jerome
  * @author Boi Bruno
  */
 public class ServletRegister extends HttpServlet {
     
     private final static String VUE_PRESENTATION = "welcome.jsp";
     private final static String SVLT_LOGIN = "login";
     private final static String VUE_HOME = "WEB-INF/view/user/home.jsp";
 
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession(false);
         if(session==null) {
             request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
         } else {
             Yuser yuser = (Yuser)session.getAttribute("user");
             if(yuser==null) {
                 request.setAttribute("error", "Session invalide");
                 request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
             } else {
                 request.setAttribute("pageTitle", "Page d'accueil");
                 request.getRequestDispatcher(VUE_HOME).forward(request, response);
             }
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
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // Récupération du formulaire
         String pseudo = request.getParameter("pseudo");
         String email = request.getParameter("email");
         String pass = request.getParameter("pwd");
         String passVerif = request.getParameter("pwd-verif");
         
         // Test des champs
         if (pseudo == null || pseudo.isEmpty()) {
             request.setAttribute("error", "Vous devez choisir<br/>un pseudo !");
             request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
         } else if (email == null || email.isEmpty() || !YaceUtils.isValidEmail(email)) {
             request.setAttribute("error", "Vous devez indiquer<br/>un email valide !");
             request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
         } else if (pass == null || passVerif == null || pass.isEmpty() || passVerif.isEmpty() || !pass.equals(passVerif)) {
             request.setAttribute("error", "Vous devez indiquer deux fois<br/>le même mot de passe !");
             request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
         } else {
             // Chargement de la facade
             YuserFacade userFacade = ServicesLocator.getUserFacade();
             
             // Reste à tester que l'utilisateur n'existe pas déjà
             Yuser userTest = userFacade.findUser(email);
             if (userTest != null) { // Utilisateur existant !
                 request.setAttribute("error", "Email déjà utilisé.<br/>Veuillez en indiquer un autre.");
                 request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
             } else {
                 userTest = userFacade.findUser(pseudo);
                 if (userTest != null) { // Utilisateur existant !
                     request.setAttribute("error", "Pseudo déjà pris.<br/>Veuillez en choisir un autre.");
                     request.getRequestDispatcher(VUE_PRESENTATION).forward(request, response);
                 } else { // Tout est OK pour l'enregistrement
                     Yuser u = new Yuser();
                     u.setPseudo(pseudo);
                     u.setEmail(email);
                     u.setPasswordHash(YaceUtils.digestMD5(pass));
                     u.setRank(new Yrank(1)); //TODO : récupérer le rang par défaut
 
                     userFacade.create(u);
 
                     request.getRequestDispatcher(SVLT_LOGIN).forward(request, response);
                 }
             }
         }
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Enregistre un membre dans la BDD";
     }
 }
