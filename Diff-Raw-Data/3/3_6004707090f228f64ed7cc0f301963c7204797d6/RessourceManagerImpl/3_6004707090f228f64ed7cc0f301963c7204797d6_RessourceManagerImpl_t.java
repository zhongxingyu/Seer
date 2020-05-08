 package manager.impl;
 
 import java.util.Date;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 import manager.GestionnaireRessource;
 import manager.Ressources;
 import modele.Artiste;
 import modele.Evenement;
 import modele.Utilisateur;
 
 import org.apache.log4j.Logger;
 
 import clientrest.deezer.DeezerRestService;
 import clientrest.lastFM.LastFMRestService;
 import dao.DAOArtistService;
 import dao.DAOUserService;
 
 
 /**
  * Implementation du gestionnaire de ressource
  * Dispatch les ressources en fonction des besoins de l'application web
  * @author nicolas
  *
  */
 @Stateless
 public class RessourceManagerImpl implements GestionnaireRessource {
 
 	private final Logger log = Logger.getLogger(RessourceManagerImpl.class);
 	
 	@EJB
 	private DAOArtistService unDaoArtiste;
 	
 	@EJB
 	private DAOUserService daoUser;
 	
 	@EJB
 	private LastFMRestService api;
 	
 	@EJB
 	private DeezerRestService apiDeezer;
 	
 	public Object get(Ressources r,String... params) {
 		
 		String param = params[0];
 		
 		if(r.equals(Ressources.artiste)){
 			Artiste a = unDaoArtiste.getUnArtiste(param);
 			if(a !=null ){
				if(params.length==2 && params[1]=="view")
					a.incrementPopularity();
 				unDaoArtiste.update(a);
 				return a;
 			}
 			else{
 				a = api.getDetailArtistInfo(param);
 				a = apiDeezer.getTrackStream(a);
 				SortedSet<Evenement> events = api.getArtistEvent(param);
 				
 				for(Evenement event : events)
 					a.addEvenement(event);
 				
 				unDaoArtiste.ajouterArtiste(a);
 				return a;
 			
 			}
 		}
 		else if(r.equals(Ressources.topArtistes)){
 			return unDaoArtiste.topArtistes(Integer.parseInt(param));
 		}
 		else if(r.equals(Ressources.utilisateur)){
 			return daoUser.get(param);
 		}
 		else if(r.equals(Ressources.artistesToString)){
 			
 			List<String> artistesToString = unDaoArtiste.list();
 			return artistesToString;
 			
 		}
 		else if(r.equals(Ressources.evenement)){
 			SortedSet<Evenement> evenements = api.getArtistEvent(param);
 			if(evenements!=null){
 				return evenements;
 			}
 			else{
 				return new ConcurrentSkipListSet<Evenement>();
 			}
 			
 		}
 		else{
 			return "Erreur : je n'ai pas compris ce que vous recherchiez";
 		}
 
 	}
 
 	public Object update(Ressources r, Object param) {
 		if(r.equals(Ressources.utilisateur)){
 			daoUser.update((Utilisateur)param);
 		}
 		return null;
 	}
 
 	@Override
 	public void add(Ressources r, Object o) {
 		if(r.equals(Ressources.utilisateur)){
 			Utilisateur uti = (Utilisateur)o;
 			Utilisateur user = daoUser.get(uti.getIpAdresse());
 			
 			if(user==null){
 				user = new Utilisateur();
 				user.setDateDerniereConnection(new Date());
 				user.setIpAdresse(uti.getIpAdresse());
 				daoUser.add(user);
 			}
 			else{
 				user.setDateDerniereConnection(new Date());
 				daoUser.update(user);
 			}
 		}
 		
 	}
 }
