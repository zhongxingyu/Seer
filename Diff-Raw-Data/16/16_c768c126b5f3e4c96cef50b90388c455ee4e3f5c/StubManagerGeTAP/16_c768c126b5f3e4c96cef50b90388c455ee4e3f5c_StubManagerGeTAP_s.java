 package org.ldv.sio.getap.app.service.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import org.ldv.sio.getap.app.AccPersonalise;
 import org.ldv.sio.getap.app.Classe;
 import org.ldv.sio.getap.app.DemandeConsoTempsAccPers;
 import org.ldv.sio.getap.app.User;
 import org.ldv.sio.getap.app.service.IFManagerGeTAP;
 import org.springframework.stereotype.Service;
 
 @Service("mockServiceManagerInMemory")
 public class StubManagerGeTAP implements IFManagerGeTAP {
 
 	static private List<DemandeConsoTempsAccPers> listeDCTAP;
 	static private List<User> listeProfs;
 	static private List<User> listeEleves;
 	static private List<Classe> listeClasses;
 	static private List<AccPersonalise> listeAP;
 
 	static {
 
 		AccPersonalise[] lesAPs = { new AccPersonalise(1, "SOS Matières"),
 		    new AccPersonalise(2, "Sortie Théatre"),
 		    new AccPersonalise(3, "Methodologie de travail") };
 
 		listeAP = new ArrayList<AccPersonalise>();
 		Collections.addAll(listeAP, lesAPs);
 
 		Classe noclasse = null;
 		listeProfs = new ArrayList<User>();
 		listeProfs.add(new User(2L, "Jean David", "Ichbiah", noclasse,
 		    "prof-intervenant"));
 
		// listeProfs.add(new User(1L, "Dennis", "Ritchie", noclasse, "admin"));

 		listeProfs.add(new User(3L, "Guido ", "van Rossum", noclasse,
 		    "prof-intervenant"));
 		listeProfs
 		    .add(new User(4L, "Gérard", "Berry", noclasse, "prof-intervenant"));
 		listeProfs.add(new User(5L, "Ada", "Lovelace", noclasse, "prof-principal"));
 		listeProfs.add(new User(6L, "Yukihiro", "Matsumoto", noclasse,
 		    "prof-intervenant"));
 
 		listeEleves = new ArrayList<User>();
 		Random alea = new Random();
 		listeClasses = new ArrayList<Classe>();
 		Classe[] desClasses = { new Classe(1, "SIO12"), new Classe(1, "SIO11"),
 		    new Classe(1, "ES"), new Classe(1, "STI") };
 		Collections.addAll(listeClasses, desClasses);
 
 		// 30 eleves
 		for (long i = 10; i < 40; i++)
 			listeEleves.add(new User(i, "prenom" + i, "nom" + i, desClasses[alea
 			    .nextInt(desClasses.length)], "eleve"));
 
 		listeEleves.add(new User(40L, "James", "Gosling", desClasses[0], "eleve"));
 		listeEleves.add(new User(41L, "Bertrand", "Meyer", desClasses[0], "eleve"));
 		listeEleves.add(new User(42L, "Niklaus", "Wirth", desClasses[1], "eleve"));
 		listeEleves.add(new User(43L, "Rasmus", "Lerdorf", desClasses[1], "eleve"));
 
 		String anneScolaire = "2011-2012";
 
 		// quelques dctap pours les eleves d'id 10 et 20
 		listeDCTAP = new ArrayList<DemandeConsoTempsAccPers>();
 
 		listeDCTAP.add(new DemandeConsoTempsAccPers(100L, "2011-2012",
 		    java.sql.Date.valueOf("2011-01-21"), 60, getProfById(2L), getAPbyId(1),
 		    40L, 0));
 		listeDCTAP.add(new DemandeConsoTempsAccPers(101L, anneScolaire,
 		    java.sql.Date.valueOf("2011-01-22"), 60, getProfById(2L), getAPbyId(2),
 		    40L, 0));
 		listeDCTAP.add(new DemandeConsoTempsAccPers(102L, anneScolaire,
 		    java.sql.Date.valueOf("2011-01-23"), 120, getProfById(5L),
 		    getAPbyId(2), 40L, 0));
 		listeDCTAP.add(new DemandeConsoTempsAccPers(103L, anneScolaire,
 		    java.sql.Date.valueOf("2011-01-24"), 120, getProfById(6L),
 		    getAPbyId(0), 40L, 0));
 
 		listeDCTAP.add(new DemandeConsoTempsAccPers(104L, "2011-2012",
 		    java.sql.Date.valueOf("2011-02-21"), 60, getProfById(2L), getAPbyId(1),
 		    41L, 0));
 		listeDCTAP.add(new DemandeConsoTempsAccPers(105L, anneScolaire,
 		    java.sql.Date.valueOf("2011-02-22"), 60, getProfById(2L), getAPbyId(2),
 		    41L, 0));
 		listeDCTAP.add(new DemandeConsoTempsAccPers(106L, anneScolaire,
 		    java.sql.Date.valueOf("2011-02-23"), 120, getProfById(5L),
 		    getAPbyId(2), 42L, 0));
 		listeDCTAP.add(new DemandeConsoTempsAccPers(107L, anneScolaire,
 		    java.sql.Date.valueOf("2011-02-24"), 120, getProfById(6L),
 		    getAPbyId(0), 42L, 0));
 
 	}
 
 	public List<DemandeConsoTempsAccPers> getListeDCTAP() {
 		return listeDCTAP;
 	}
 
 	public void setListeDCTAP(List<DemandeConsoTempsAccPers> listeDCTAP) {
 		StubManagerGeTAP.listeDCTAP = listeDCTAP;
 	}
 
 	public List<User> getListeProfs() {
 		return listeProfs;
 	}
 
 	public void setListeProfs(List<User> listeProfs) {
 		StubManagerGeTAP.listeProfs = listeProfs;
 	}
 
 	public List<User> getListeEleves() {
 		return listeEleves;
 	}
 
 	public void setListeEleves(List<User> listeEleves) {
 		StubManagerGeTAP.listeEleves = listeEleves;
 	}
 
 	static public AccPersonalise getAPbyId(int id) {
 		if (id >= 0 && id < listeAP.size())
 			return listeAP.get(id);
 		return null;
 	}
 
 	public AccPersonalise getAPById(int id) {
 		return StubManagerGeTAP.getAPbyId(id);
 	}
 
 	public List<DemandeConsoTempsAccPers> getAllDCTAPByEleve(User eleve) {
 		List<DemandeConsoTempsAccPers> result = new ArrayList<DemandeConsoTempsAccPers>();
 		// get anneeScolaire dans la session
 		// puis SELECT ... WHERE anneeScolaire=... AND id=user.getId()
 		// non traité ici
 		for (int i = 0; i < listeDCTAP.size(); i++) {
 			if (listeDCTAP.get(i).getIdEleve() == eleve.getId())
 				result.add(listeDCTAP.get(i));
 		}
 		return result;
 	}
 
 	public void addDCTAP(DemandeConsoTempsAccPers dctap) {
 		listeDCTAP.add(dctap);
 	}
 
 	public void updateDCTAP(DemandeConsoTempsAccPers dctap) {
 		// update en mémoire seulement, donc nothing todo
 	}
 
 	public void deleteDCTAP(DemandeConsoTempsAccPers dctap) {
 		listeDCTAP.remove(dctap);
 	}
 
 	static User getProfById(Long id) {
 		for (User prof : listeProfs) {
 			if (id.equals(prof.getId()))
 				return prof;
 		}
 		return null;
 	}
 
 	public User getProfesseurById(Long id) {
 		for (User prof : listeProfs) {
 			if (id.equals(prof.getId()))
 				return prof;
 		}
 		return null;
 	}
 
 	public boolean deleteDCTAPById(Long id) {
 		for (DemandeConsoTempsAccPers dctap : listeDCTAP) {
 			if (id.equals(dctap.getId())) {
 				listeDCTAP.remove(dctap);
 				return true;
 			}
 		}
 		return false;
 
 	}
 
 	public DemandeConsoTempsAccPers getDCTAPById(Long id) {
 		for (DemandeConsoTempsAccPers dctap : listeDCTAP) {
 			if (id.equals(dctap.getId()))
 				return dctap;
 		}
 		return null;
 	}
 
 	public List<DemandeConsoTempsAccPers> getAllDCTAPByProfInterv(User profi) {
 		List<DemandeConsoTempsAccPers> result = new ArrayList<DemandeConsoTempsAccPers>();
 		for (int i = 0; i < listeDCTAP.size(); i++) {
 			User prof = listeDCTAP.get(i).getProf();
 			if (prof.equals(profi))
 				result.add(listeDCTAP.get(i));
 		}
 		return result;
 	}
 
 	public List<DemandeConsoTempsAccPers> getAllDCTAPByProfPrinc(User profprinc) {
 		// TODO Auto-generated method stub
 
 		// prendre tous les dctap des eleves appartenant a la classe dont profprinc
 		// est pp.
 
 		return null;
 	}
 
 	public List<DemandeConsoTempsAccPers> getAllDCTAPByClasse(String classe) {
 		List<DemandeConsoTempsAccPers> result = new ArrayList<DemandeConsoTempsAccPers>();
 		for (int i = 0; i < listeDCTAP.size(); i++) {
 			String classeName = getUserById(listeDCTAP.get(i).getIdEleve())
 			    .getClasse().getNom();
 			if (classeName.equals(classe))
 				result.add(listeDCTAP.get(i));
 		}
 		return result;
 	}
 
 	public List<User> getAllProf() {
 		return listeProfs;
 	}
 
 	public List<User> getAllProfInter() {
 		List<User> profi = new ArrayList<User>();
 		for (User prof : listeProfs) {
 			if (prof.getRole().equals("prof-intervenat"))
 				profi.add(prof);
 		}
 		return profi;
 	}
 
 	public List<User> getAllProfPrinc() {
 		List<User> profi = new ArrayList<User>();
 		for (User prof : listeProfs) {
 			if (prof.getRole().equals("prof-principal"))
 				profi.add(prof);
 		}
 		return profi;
 	}
 
 	public List<User> getAllEleve() {
 		return listeEleves;
 	}
 
 	public User getUserById(Long id) {
 		User user = null;
 		for (User prof : listeProfs) {
			if (prof.getId() == id)
 				user = prof;
			break;
 		}
 		if (user == null)
 			for (User e : listeEleves) {
				if (e.getId() == id)
 					user = e;
				break;
 			}
 		return user;
 	}
 
 	public void addUser(User user) {
 		if (user.getRole().equals("eleve"))
 			listeEleves.add(user);
 
 		else if (user.getRole().startsWith("prof"))
 			listeProfs.add(user);
 
 	}
 
 	public void updateUser(User user) {
 		// nothing todo, because in memory
 	}
 
 	public void deleteUser(User user) {
 		if (user.getRole().equals("eleve"))
 			listeEleves.remove(user);
 
 		else if (user.getRole().startsWith("prof"))
 			listeProfs.remove(user);
 
 	}
 
 	public List<AccPersonalise> getAllAP() {
 		return listeAP;
 	}
 
 	public void addAP(AccPersonalise ap) {
 		listeAP.add(ap);
 	}
 
 	public void upDateAP(AccPersonalise ap) {
 		// nothing because in memory
 	}
 
 	public void deleteAP(AccPersonalise ap) {
 		listeAP.remove(ap);
 	}
 
 	public List<Classe> getAllClasse() {
 		return listeClasses;
 	}
 
 	public Classe getClasseById(int id) {
 		for (Classe c : listeClasses) {
 			if (c.getId() == id)
 				return c;
 		}
 		return null;
 	}
 
 	public void addClasse(Classe classe) {
 		listeClasses.add(classe);
 	}
 
 	public void upDateClasse(Classe classe) {
 		// nothing because in memory
 	}
 
 	public void deleteClasse(Classe classe) {
 		listeClasses.remove(classe);
 	}
 
 	public String getCurrentAnneeScolaire() {
 		return "2011-2012";
 	}
 
 	public List<String> getAllAnneeScolaire() {
 		List<String> as = new ArrayList<String>();
 		as.add("2011-2012");
 		return as;
 	}
 
 }
