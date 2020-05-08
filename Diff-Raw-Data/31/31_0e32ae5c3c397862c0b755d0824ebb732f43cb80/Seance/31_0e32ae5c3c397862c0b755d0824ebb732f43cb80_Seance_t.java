 package models;
 
 import javax.persistence.*;
 import play.db.ebean.*;
 
 @Entity
 public class Seance extends Model {
 	public static Finder<Integer, Seance> find = new Finder<Integer, Seance>(
 		Integer.class, Seance.class
 	);
 	
 	public enum Etat {
 		NOUVELLE,
 		EN_COURS,
 		TERMINEE,
 		VALIDEE
 	}
 
 	@Id
 	public Integer id;
 	public Integer distance;
 	public Integer duree;
 	public Etat etat;
 	@ManyToOne
 	public Joueur joueur;
 	@ManyToOne
 	public Quete quete;
 
 	public Seance(Integer distance, Integer duree, Etat etat,
 	              Joueur joueur, Quete quete) {
 		this.distance = distance;
 		this.duree = duree;
 		this.etat = etat;
 		this.joueur = joueur;
 		this.quete = quete;
 	}
 	
 	public static void create(Joueur j, Quete q) {
 		new Seance(0, 0, Etat.NOUVELLE, j, q).save();
 	}
 
 	public static void accepter(Integer idQuete, String pseudo) {
 		Seance s = find.where().eq("joueur.pseudo", pseudo)
 		                       .eq("quete.id", idQuete)
 		                       .findUnique();
 		s.etat = Etat.EN_COURS;
 		s.save();
 	}
 	
	public static void terminer(Integer idQuete, String pseudo, Integer distance, Integer duree) {
 		Seance s = find.where().eq("joueur.pseudo", pseudo)
 		                       .eq("quete.id", idQuete)
 		                       .findUnique();
		s.distance = distance;
		s.duree = duree;
 		s.etat = Etat.TERMINEE;
 		s.save();
 	}
 
 	public static void valider(Integer idQuete, String pseudo) {
 		Seance s = find.where().eq("joueur.pseudo", pseudo)
 		                       .eq("quete.id", idQuete)
 		                       .findUnique();
 		s.etat = Etat.VALIDEE;
 		
 		Quete suivante = Quete.find.byId(idQuete).suivante;
 		if(suivante != null) {
 			Seance.create(Joueur.find.byId(pseudo), suivante);
 		}
 		
 		s.save();
 	}
 }
