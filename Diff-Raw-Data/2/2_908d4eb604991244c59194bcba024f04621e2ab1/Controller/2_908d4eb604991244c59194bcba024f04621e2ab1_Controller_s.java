 package controllers;
 
 import controllers.modules.user.ArticlesController;
 import controllers.modules.*;
 import conf.Blog;
 import conf.JSP;
 import controllers.modules.admin.AdminController;
 import controllers.modules.user.ConnectionController;
 import db.Connexion;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import models.ArticlesModel;
 import models.SessionModel;
 
 
 public class Controller extends HttpServlet {
     private static final long serialVersionUID = 1L;
    
     private Map<String, ModuleController> modulesControllers = new HashMap<String, ModuleController>();
 
     
     @Override
     public void init() throws ServletException {
         Connexion.setCredentials("root", "");
         Connexion.setUrl("jdbc:mysql://localhost/jblog");
         
         initModules();
     }
     
     private void initModules() {
         addModule("articles", new ArticlesController(this));
         addModule("connection", new ConnectionController(this));
        addModule("admin", new AdminController(this));
     }
     
     protected final void addModule(String name, ModuleController ctrl) {
         if(modulesControllers.containsKey(name))
             throw new IllegalArgumentException("Ce module est déjà enregistré");
         
         modulesControllers.put(name, ctrl);
     }
     
     protected final ModuleController getModuleController(String name) {
         return modulesControllers.get(name);
     }
     
     
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
         request.setAttribute("HTTP_METHOD", "GET");
 
         dispatchToModulesControllers(request, response);
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
         // pour gérer l'utf-8 dans les formulaires
         request.setCharacterEncoding("UTF-8");
         
         request.setAttribute("HTTP_METHOD", "POST");
         
         dispatchToModulesControllers(request, response);
     }
 
     private void dispatchToModulesControllers(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         ModuleController controller = getModuleController(request.getParameter("module"));
         
         // pas de contrôleur trouvé = requête incorrect : pas la peine d'aller plus loin
         if(controller == null) {
             error("Erreur 404", request, response);
             return;
         }
         
         // vérification de l'état de la connexion à la DB
         try {
             Connexion.initConnexion();
         } catch (Exception ex) {
             error(ex.getMessage(), request, response);
             return;
         }
         
         // création du modèle de session
         SessionModel mdl = new SessionModel(request, response);
         request.setAttribute("session", mdl);
         
         // connexion auto
         try {
             mdl.tryConnect();
         } catch (SQLException e) {
             error(e.getMessage(), request, response);
             return;
         }
         
         // on passe le relai au sous-contrôleur
         controller.handle(request, response);
     }
     
     
     
     private void defineViewVariables(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // variables de configuration
         for(Field key : Blog.class.getFields()) {
             try {
                 String val = null;
                 val = key.get(val).toString();
                 
                 request.setAttribute(key.getName(), val);
             } catch (Exception e) {
 
             }
         }
         
         // variable indiquant si le membre actuel est connecté
         request.setAttribute("IS_LOGGED_IN", new Boolean(((SessionModel) request.getAttribute("session")).isLoggedIn()));
         
         // catégories
         ArticlesModel articlesMdl = new ArticlesModel();
         try {
             request.setAttribute("LIST_CATEGORIES", articlesMdl.getCategories());
         } catch (Exception ex) {
             error(ex.getMessage(), request, response);
             return;
         }
         try {
             // statistiques
             request.setAttribute("STATS_NB_ARTICLES", articlesMdl.getNBArticles(true));
             request.setAttribute("STATS_NB_COMS", articlesMdl.getNBComments(true));
         } catch (SQLException ex) {
             error(ex.getMessage(), request, response);
             return;
         }
     }
     
     
     public final void redirect(String to, String msg, HttpServletRequest request, HttpServletResponse response)
                          throws ServletException, IOException {
         request.setAttribute("redir_msg", msg);
         request.setAttribute("redir_url", to);
         request.setAttribute("redir_time", "3");
         
         forward(JSP.MESSAGE, request, response);
     }
     
     public final void forward(String to, HttpServletRequest request, HttpServletResponse response)
                          throws ServletException, IOException {
         defineViewVariables(request, response);
         getServletContext().getRequestDispatcher(to).forward(request, response);
     }
 
     public final void error(HttpServletRequest request, HttpServletResponse response)
                          throws ServletException, IOException {
         error(null, request, response);
     }
 
     public final void error(String msg, HttpServletRequest request, HttpServletResponse response)
                          throws ServletException, IOException {
         request.setAttribute("error_msg", msg);
         forward(JSP.ERROR, request, response);
     }
 }
