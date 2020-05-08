 // 
 //
 //  @ Projet : Project Gammon
 //  @ Fichier : Master.java
 //  @ Date : 12/12/2012
 //  @ Auteurs : DONG Chuan, BONNETTO Benjamin, FRANCON Adrien, POTHELUNE Jean-Michel
 //
 //
 
 
 
 
 package fr.ujm.tse.info4.pgammon.models;
 
 import java.util.ArrayList;
import java.util.Calendar;
 import java.util.Date;
 
 import fr.ujm.tse.info4.pgammon.controleur.ControleurPrincipal;
 
//TODO: Interdire le redimensionnement
 
 public class Master
 {
 	private int idMax;
 	private int idDate;
 	private ArrayList<Session> listSession;
 	private ControleurPrincipal controleurPrincipal;
 	
 	public Master()
 	{
 		idMax= 1;
		Calendar date = Calendar.getInstance();
		idDate = 10000*date.get(Calendar.MONTH)+1000*date.get(Calendar.DATE)
				+100*date.get(Calendar.HOUR)+10*date.get(Calendar.MINUTE)+date.get(Calendar.SECOND);
 		controleurPrincipal = new ControleurPrincipal(this);
 		listSession = new ArrayList<Session>();
 	}
 	
 	public void lancerSession(ParametreJeu parametreJeu)
 	{
 		if (peutLancerSession())
 		{
 			listSession.add(new Session(idDate,parametreJeu));
 			//listSession.add(new Session(idMax,parametreJeu));
 			//idMax +=1;
 		}
 		try {
 			GestionDeSession gestion = GestionDeSession.getGestionDeSession();
 			gestion.setListSession(listSession);
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 	}
 	
 	public void chargerSession(Session session)
 	{
 		if (peutLancerSession())
 		{
 			listSession.add(session);
 			//listSession.add(new Session(idMax,parametreJeu));
 			//idMax +=1;
 		}	
 	}
 	
 	public void arreterSession(int id)
 	{
 		if(listSession.size()!=0)
 		{
 			for (Session session : listSession) {
 			
 				if(session.getIdSession() == id)
 				{
 					listSession.remove(session);
 					break;
 				}
 			}	
 		}	
 	}
 
 
 	public boolean peutLancerSession()
 	{
 		//TODO gere le multi THREAD
 		if (listSession.size() != 1 )
 			return true;
 		return false;
 	}
 	
 	public static void main(String[] args) 
 	{
 		Master master = new Master();
 
 	}
 	
 	public Session getSession() 
 	{
 		// a modifier pour le multi THREAD
 		return listSession.get(listSession.size()-1);
 	}
 	
 	
 }
