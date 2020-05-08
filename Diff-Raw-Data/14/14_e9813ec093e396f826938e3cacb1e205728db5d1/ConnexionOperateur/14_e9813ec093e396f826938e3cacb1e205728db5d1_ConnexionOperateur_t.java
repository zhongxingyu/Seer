 
 /*****************************************************
 * Module : Controlleur
 * Fichier : ConnexionOperateur
 * Description : 
 * Projet : SKF Traceability
 * Auteur : GC
 * Date : 12/12/12
 * Version : 0.5
 ******************************************************/
 
 package Controlleur;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import dao.OperateurDAO;
 import dao.PieceDAO;
 import form.ConnexionOperateurForm;
 
 import Modele.Operateur;
 import Modele.Piece;
 
 /**
  * Servlet implementation class ConnexionOperateur
  */
 @WebServlet(description = "Connexion d'un op�rateur", urlPatterns = { "/ConnexionOperateur" })
 public class ConnexionOperateur extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	 public static final String ATT_USER         = "utilisateur";
 	 public static final String ATT_FORM         = "form";
 	 public static final String ATT_SESSION_USER = "sessionUtilisateur";
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ConnexionOperateur() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		System.out.println("servlet : ConnexionOperateurds");
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//On instancie un operateur DAO
 		/*OperateurDAO operateurDAO  = new OperateurDAO();
 		// On r�cup�re les valeurs pour le login/mdp
 		String login = request.getParameter("login");
 		String mdp = request.getParameter("motDePasse");
 		if(login == "" || mdp == ""){
 			//Si le login OU mdp est vide, on stocke l'erreur dans une variable de session
 			HttpSession session = request.getSession(true);  
 			session.setAttribute("erreurConnexion", "login/mdp vide");
 			System.out.println("login/mdp vide");
 			//On affiche le formulaire de connexion
 			
 			dispatcher.forward( request, response );
 		}else{
 			//On r�cupere l'operateur suivant le login/mot de passe transmis
 			Operateur operateur = operateurDAO.getOperateur(login,mdp);
 			if(operateur != null){
 				//Si l'op�rateur existe
 				System.out.println("Utilisateur OK");
 				HttpSession session = request.getSession(true);  
 				session.setAttribute("utilisateur", operateur);
 				session.setAttribute("etatDouchette", false);
 				RequestDispatcher dispatcher = request.getRequestDispatcher("servlet/index.html");
 				
 			}else{		
 				//Si l'op�rateur n'existe pas
 				HttpSession session = request.getSession(true);  
 				session.setAttribute("erreurConnexion", "login/mdp incorrect");
 				System.out.println("login/mdp incorrect");
 				RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
 				dispatcher.forward( request, response );
 			}
 		}	*/
 		 /* Préparation de l'objet formulaire */
         ConnexionOperateurForm form = new ConnexionOperateurForm();
        System.out.println("Essai");
         /* Traitement de la requête et récupération du bean en résultant */
         Operateur utilisateur = form.connecterUtilisateur( request );
 
         /* Récupération de la session depuis la requête */
        HttpSession session = request.getSession(true);
        session.setAttribute("etatDouchette", false);
         RequestDispatcher dispatcher;
         /**
          * Si aucune erreur de validation n'a eu lieu, alors ajout du bean
          * Utilisateur à la session, sinon suppression du bean de la session.
          */
         if ( form.getErreurs().isEmpty() ) {
         	System.out.println("Pas d'erreur d'authentification");
             session.setAttribute( ATT_SESSION_USER, utilisateur );
             dispatcher = request.getRequestDispatcher("servlet/index.html");
             
         } else {
        	
             session.setAttribute( ATT_SESSION_USER, null );
             dispatcher = request.getRequestDispatcher("index.jsp");
         }
 
         /* Stockage du formulaire et du bean dans l'objet request */
         request.setAttribute( ATT_FORM, form );
         request.setAttribute( ATT_USER, utilisateur );
     	
         dispatcher.forward( request, response );
 		
 		
 	}
 
 }
