 package fr.imie.servlet;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import fr.imie.formation.DAO.exceptions.DAOException;
 import fr.imie.formation.DTO.ProjetDTO;
 import fr.imie.formation.DTO.PromotionDTO;
 import fr.imie.formation.DTO.StatutProjetDTO;
 import fr.imie.formation.DTO.UtilisateurDTO;
 import fr.imie.formation.factory.DAOFactory1;
 import fr.imie.formation.services.exceptions.ServiceException;
 import fr.imie.formation.transactionalFramework.exception.TransactionalConnectionException;
 
 /**
  * Servlet implementation class Projet
  */
 @WebServlet("/Projet")
 public class Projet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Projet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 
 		// Afficher un projet marche bien
 		if (request.getParameter("numligne") != null
 				&& request.getParameter("update") == null
 				&& request.getParameter("delete") == null) {
 		
 					int ligne = Integer.valueOf(request.getParameter("numligne"));
 					Object listObj = session.getAttribute("listeProjet");
 					@SuppressWarnings("unchecked")
 					List<ProjetDTO> listeProjet = (List<ProjetDTO>) listObj;
 					ProjetDTO projet = listeProjet.get(ligne);
 	
 					//session.removeAttribute("listeProjet");
 					
 				try {
 					ProjetDTO projetDTO = DAOFactory1.getInstance().createProjetService(null).readProjet(projet);
 					request.setAttribute("projetDTO", projetDTO);
 					List<UtilisateurDTO> listeUtil = DAOFactory1.getInstance().createUtilisateurService(null).readUtilisateurProjet(projetDTO);
 					session.setAttribute("listeUtil", listeUtil);
 
 				} catch (TransactionalConnectionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 	
 				} catch (ServiceException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 				
 		
 		request.getRequestDispatcher("/ProjetConsult.jsp")
 		.forward(request, response);
 		}
 		
 		
 		//création projet
 		else if (request.getParameter("numligne") == null
 				&& request.getParameter("create") != null
 				&& request.getParameter("create").equals("creer")) {
 			
 			List<StatutProjetDTO>listeStatut = null;
 			List<UtilisateurDTO> listeForChef =null;
 			
 			try {
 				//listeStatut=DAOFactory1.getInstance().createProjetService(null).readAllStatutProjet();
 				listeForChef= DAOFactory1.getInstance().createUtilisateurService(null).readAllUtilisateur();
 				
 			} catch (TransactionalConnectionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			//request.setAttribute("listeStatut", listeStatut);
 			request.setAttribute("listeForChef", listeForChef);		
 			
 			request.getRequestDispatcher("./ProjetCreate.jsp").forward(request,response);
 		}
 		
 		
 		//modification projet
 		else if (request.getParameter("update") != null
 				&& request.getParameter("update").equals("modifier")) {
 			request.setAttribute("projetDTO",getProjet(request.getParameter("numProjet")));
 			
 			List<UtilisateurDTO> listeForChef =null;
 			List<StatutProjetDTO>listeStatut = null;
 			List<UtilisateurDTO> listeUtil = null;
 			try {
 				listeForChef= DAOFactory1.getInstance().createUtilisateurService(null).readAllUtilisateur();
 				listeStatut=DAOFactory1.getInstance().createProjetService(null).readAllStatutProjet();
 				listeUtil = DAOFactory1.getInstance().createUtilisateurService(null).readUtilisateurProjet(getProjet(request.getParameter("numProjet")));
 			} catch (TransactionalConnectionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			request.setAttribute("listeForChef", listeForChef);
 			request.setAttribute("listeStatut", listeStatut);
 			request.setAttribute("listeUtil", listeUtil);
 			
 			request.getRequestDispatcher("./ProjetUpdate.jsp").forward(request,response);
 		
 		} 
 		
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// Modifier un projet
 			if (request.getParameter("updateAction") != null
 						&& request.getParameter("updateAction").equals("Confirmer")) {
 					
 					ProjetDTO projetUpdate = getProjet(request.getParameter("numProjet"));
 					projetUpdate.setIntitule(request.getParameter("intituleProjet"));
 					projetUpdate.setDescription(request.getParameter("descriptionProjet"));
 					
 					//modif du statut
 					String statutParam = request.getParameter("statutProjet");
 			
 					StatutProjetDTO statut =new StatutProjetDTO();
 					
 					Integer statutNum = null;
 					if (statutParam != null){
 						statutNum = Integer.valueOf(statutParam);
 					}
 					if (statutNum!= null){
 						StatutProjetDTO statutToUpdate = new StatutProjetDTO();
 						statutToUpdate.setNum(statutNum);
 						try {
 							statut = DAOFactory1.getInstance().createProjetService(null).readStatutProjet(statutToUpdate);
 						} catch (TransactionalConnectionException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (ServiceException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					projetUpdate.setStatutProjet(statut);
 				
 					//modif chef de projet
 					String chefParam = request.getParameter("chefProjet");
 					UtilisateurDTO chefProjet = new UtilisateurDTO();
 					Integer chefNum = null;
 					if (chefParam != null) {
 						chefNum = Integer.valueOf(chefParam);
 					}
 						if(chefNum !=null){
 							UtilisateurDTO chefUpdate = new UtilisateurDTO();
 							chefUpdate.setNum(chefNum);						
 						
 							try {
 								chefProjet = DAOFactory1.getInstance().createUtilisateurService(null).readUtilisateur(chefUpdate);
 							
 							} catch (TransactionalConnectionException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							} catch (ServiceException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}					
 						}
 						
 						projetUpdate.setChefDeProjet(chefProjet);
 									
 					try {
 						DAOFactory1.getInstance().createProjetService(null).updateProjet(projetUpdate);
 						DAOFactory1.getInstance().createProjetService(null).ajoutChefDeProjet(projetUpdate);
 						request.setAttribute("action", "updateAction");
 					} catch (TransactionalConnectionException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (ServiceException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 					request.setAttribute("projetDTO",
 							getProjet(request.getParameter("numProjet")));
 					request.getRequestDispatcher("./ProjetConsult.jsp").forward(request,
 							response);
 				}
 		
 			
 			
 		//ajout d' l'utilisateur au projet
 		if (request.getParameter("envoyerInvite")!=null){
 				
 			ProjetDTO projetForUtil = getProjet(request.getParameter("projetForInvitation"));
 				//request.setAttribute("projetDTO", projetForUtil);// envoie sur la fiche Projet mais sans les participants
 			
 			
 			UtilisateurDTO utilForProjet = new UtilisateurDTO();
 			String numUtil = request.getParameter("numUtilisateur");
 			int numUtilInt = Integer.valueOf(numUtil);
 			utilForProjet.setNum(numUtilInt);
 			
 			try {
 				DAOFactory1.getInstance().createProjetService(null).addProjetUtil(utilForProjet, projetForUtil);
 			} catch (TransactionalConnectionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 					
 			request.getRequestDispatcher("./ProjetConsult.jsp").forward(request, response);
 		}
 		
 		
 		//creation d'un projet
 		else if (request.getParameter("createAction") != null
 				&& request.getParameter("createAction").equals("ajouter")) {
 		
 			
 			ProjetDTO projetCreate = new ProjetDTO();
 			projetCreate.setIntitule(request.getParameter("intituleProjet"));
 			projetCreate.setDescription(request.getParameter("descriptionProjet"));
 			//affectation du statut 1 =  ouvert
 			StatutProjetDTO statutProjet = new StatutProjetDTO();
 			statutProjet.setNum(1);
 			projetCreate.setStatutProjet(statutProjet);
 			
 			try {
 				DAOFactory1.getInstance().createProjetService(null).createProjet(projetCreate);
 			} catch (TransactionalConnectionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			request.setAttribute("createAction", "ajouter");
 			request.setAttribute("projet", projetCreate);
 			request.getRequestDispatcher("./ProjetConsult.jsp").forward(request,
 					response);
 		}
 		
 		//suppression de projet
 		else if (request.getParameter("deleteAction") != null
				&& request.getParameter("deleteAction").equals("supprimer")) {
 			ProjetDTO projetDelete = getProjet(request.getParameter("numProjet"));
 			try {
 				DAOFactory1.getInstance().createProjetService(null).deleteProjet(projetDelete);
 			} catch (TransactionalConnectionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 					
 			response.sendRedirect("./ListProjetView");
 		}
 			
 	}
 	
 	
 	
 	private ProjetDTO getProjet(String requestNumProjet) {
 
 		ProjetDTO projetDTO = new ProjetDTO();
 		int numProjet = Integer.valueOf(requestNumProjet);
 		
 		ProjetDTO projetTemp = new ProjetDTO();
 		projetTemp.setNum(numProjet);
 
 	
 		try {
 			projetDTO = DAOFactory1.getInstance().createProjetService(null).readProjet(projetTemp);
 					
 		} catch (TransactionalConnectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ServiceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return projetDTO;
 	}
 
 }
