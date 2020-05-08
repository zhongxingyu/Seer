 package com.polytech.propps.servlets;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.polytech.propps.bdd.Base;
 import com.polytech.propps.models.Membre;
 import com.polytech.propps.models.Notification;
 import com.polytech.propps.models.Recruteur;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 @WebServlet("/CurrentUserProfileServlet")
 public class CurrentUserProfileServlet extends HttpServlet {
 
 	/**
 	 *  connexion classe 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public void doPost( HttpServletRequest request, HttpServletResponse	response ) throws ServletException, IOException{
 		int ID_Membre_Courant = Integer.parseInt(request.getParameter(ParametresServlet.ID_Membre_Courant));
 		Base base = new Base();
 		try {
 			base.connect();
 			Membre membre = new Membre(ID_Membre_Courant);
 			membre.fill();
 			request.setAttribute("nom", membre.getsNom());
 			request.setAttribute("prenom", membre.getsPrenom());
 			request.setAttribute(ParametresServlet.ID_Membre_Courant, Integer.toString(ID_Membre_Courant));
 			request.removeAttribute("password");
 			request.removeAttribute("email");
 			System.out.println(membre.getAdresse().getVille());
 			request.setAttribute("ville", membre.getAdresse().getVille());
 			List lstContacts = Arrays.asList(membre.getLstContacts().toArray());
 			HashMap<Integer,Notification> lstNotifRecept = membre.getLstNotifRecept();
 			System.out.println(lstNotifRecept.toString());
 			HashMap<String,Integer> mapNotifRecept = new HashMap<String,Integer>();
 			for(Notification n : lstNotifRecept.values()){
 				Membre tmp = n.getSource();
 				System.out.println(tmp.getsPrenom() + " "+ tmp.getsNom());
 				mapNotifRecept.put(tmp.getsPrenom() + " "+ tmp.getsNom(), n.getID());
 			}
 			request.setAttribute("mapNotifRecept", mapNotifRecept);
 			request.setAttribute("nbNotif", Integer.toString(lstNotifRecept.size()));
 			request.setAttribute("lstNotifRecept", lstNotifRecept);
 			request.setAttribute("LstContacts", lstContacts);
 			getServletContext().getRequestDispatcher("/jsp/compte.jsp").forward(request, response);
 			
 		}catch(Exception e) {
 			e.printStackTrace();
 		}finally {
 			base.close();
 		}
 	}
 
 }
 
