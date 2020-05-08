 package test;
 
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import model.Aeroport;
 import model.Vol;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import dao.MysqlDao;
 
 @RunWith(JUnit4.class)
 public class TestMysqlDao {
 
 	// Décommenter le mot @Test pour lancer le(s) test(s) correspondant(s)
 
 	//@Test
 	public void getAllAeroports() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		List<Aeroport>aeroports = dao.getAllAeroports();
 		// on peut tester que le nombre d'aéroports retournés correspond bien
 		// au nombre d'aéroports en base (à réajuster au fur et à mesure) :
 		Assert.assertEquals(40, aeroports.size());
 		// on teste que le premier élément de la liste est bien une instance d'Aeroport
 		Assert.assertTrue(aeroports.get(0) instanceof Aeroport);
 	}
 
 	//@Test
 	public void addNewAeroport() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		// à remodifier à chaque test :
 		Aeroport aeroportTest = new Aeroport("SXB", "Strasbourg", "France");
 		boolean result = dao.addNewAeroport(aeroportTest);
 		Assert.assertTrue(result);
 		// à réajuster à chaque test :
 		Aeroport a = dao.getAeroportByCode("SXB");
 		Assert.assertEquals(aeroportTest.getCodeAeroport(), a.getCodeAeroport());
 	}
 
 	//@Test
 	public void getAllVolsProgrammes() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		List<Vol>vols = dao.getAllVolsProgrammes();
 		// on peut tester que le nombre de vols retournés correspond bien
 		// au nombre de vols en base (à réajuster au fur et à mesure) :
 		Assert.assertEquals(9, vols.size());
 		// on teste que le premier élément de la liste est bien une instance de Vol
 		Assert.assertTrue(vols.get(0) instanceof Vol);
 	}
 
 	//@Test
 	public void getAllVolsEnAttente() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		List<Vol>vols = dao.getAllVolsEnAttente();
 		// on peut tester que le nombre de vols retournés correspond bien
 		// au nombre de vols en base (à réajuster au fur et à mesure) :
 		Assert.assertEquals(4, vols.size());
 		// on teste que le premier élément de la liste est bien une instance de Vol
 		Assert.assertTrue(vols.get(0) instanceof Vol);
 	}
 
 	//@Test
 	public void addNewVol() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		
 		// On transforme les chaînes de caractères en dates selon le motif.
 		// On déclare les dates à l'extérieur du try / catch
 		Date dateDepart = null;
 		Date dateArrivee = null;
 		
 		try {
			dateDepart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-03-24 02:20:00");
			dateArrivee = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-03-24 18:34:00");
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		
 		Aeroport aeroportTest1 = new Aeroport("CMN", "Casablanca", "Maroc");
 		Aeroport aeroportTest2 = new Aeroport("HNL", "Honolulu", "États-Unis");
 		
 		// (On peut le modifier avant de relancer un test) :
 		Vol volTest = new Vol(aeroportTest1, aeroportTest2, dateDepart, dateArrivee, 1540, 974F);
 		
 		int result = dao.addNewVol(volTest); // doit ajouter le vol en base (table vol_tmp)
 		Assert.assertEquals(1, result); // une seule ligne doit être impactée
 		//Vol v = dao.getVolById(5);
 		//Assert.assertEquals(volTest.getId(), v.getId());
 	}
 
 
 	//@Test
 	public void getAeroportByVille() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		// doit renvoyer un objet Aeroport correspondant à la ville en paramètre
 		Aeroport aeroportRecupere = dao.getAeroportByVille("Berne");
 		Aeroport aeroportTest = new Aeroport("BRN", "Berne", "Suisse");
 		Assert.assertEquals(aeroportTest.getCodeAeroport(), aeroportRecupere.getCodeAeroport());
 	}
 
 	//@Test
 	public void getAeroportByCode() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		// doit renvoyer un objet Aeroport correspondant au code en paramètre
 		Aeroport aeroportRecupere = dao.getAeroportByCode("CMN");
 		Aeroport aeroportTest = new Aeroport("CMN", "Casablanca", "Maroc");
 		Assert.assertEquals(aeroportTest.getCodeAeroport(), aeroportRecupere.getCodeAeroport());
 		Assert.assertEquals(aeroportTest.getVille(), aeroportRecupere.getVille());
 	}
 
 	//@Test
 	public void getDureeVol(){ // doit renvoyer la durée du vol en min
 		Date dateDepart = null;
 		Date dateArrivee = null;
 		
 		try {
			dateDepart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-03-24 02:20:00");
			dateArrivee = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-03-24 04:30:00");
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		int duree = util.Donnees.getDureeVol(dateDepart, dateArrivee);
 		Assert.assertEquals(130, duree);		
 	}
 
 	@Test
 	public void doesAirportAlreadyExist() throws SQLException{ // doit renvoyer vrai si l'aéroport existe déjà en base
 		MysqlDao dao = new MysqlDao();
 		boolean test1 = dao.doesAirportAlreadyExist("BRN");
 		boolean test2 = dao.doesAirportAlreadyExist("LLL");
 		Assert.assertTrue(test1);
 		Assert.assertFalse(test2);
 	}
 
 	//@Test
 	public void getNextIdVolTmp() throws SQLException{ // doit renvoyer le prochain ID à insérer dans la table vol_tmp
 		MysqlDao dao = new MysqlDao();
 		String nextId = dao.getNextIdVolTmp();
 		// à adapter à chaque test
 		Assert.assertEquals("TMP7", nextId);
 	}
 	
 	//@Test
 	public void getNextIdVol() throws SQLException{ // doit renvoyer le prochain ID à insérer dans la table vol
 		MysqlDao dao = new MysqlDao();
 		String nextId = dao.getNextIdVol();
 		// à adapter à chaque test
 		Assert.assertEquals("DF11", nextId);
 	}
 
 	//@Test
 	public void updateAeroport() throws Exception{
 		MysqlDao dao = new MysqlDao();
 		// (On peut le modifier avant de relancer un test) :
 		Aeroport a = new Aeroport("ANI", "Ailleurs", "Tunisie");
 		Aeroport b = new Aeroport("BRN", "Berne", "Suisse"); // cet aéroport déjà utilisé n'est plus modifiable
 		Aeroport c = new Aeroport("LLL", "Truc", "Pays"); // Cet aéroport n'existe pas et n'est donc pas modifiable
 		boolean result1 = dao.updateAeroport(a);
 		boolean result2 = dao.updateAeroport(b);
 		boolean result3 = dao.updateAeroport(c);
 		Aeroport a1 = dao.getAeroportByCode("ANI"); // on récupère l'aéroport modifié
 		Assert.assertTrue(result1); // true
 		Assert.assertFalse(result2); // false
 		Assert.assertFalse(result3); // false
 		Assert.assertEquals(a.getVille(), a1.getVille()); // les villes coïncident
 	}
 
 	//@Test
 	public void deleteAeroport() throws Exception{ // doit supprimer l'aéroport dont le code est en paramètre
 		MysqlDao dao = new MysqlDao();
 		// À modifier avant de relancer un test :
 		boolean result1 = dao.deleteAeroport("ALG"); // cet aéroport est supprimable
 		boolean result2 = dao.deleteAeroport("BRN"); // cet aéroport est déjà utilisé
 		boolean result3 = dao.deleteAeroport("LLL"); // cet aéroport n'existe pas
 		Assert.assertTrue(result1); // renvoie vrai si l'aéroport a été supprimé
 		Assert.assertFalse(result2); // non supprimable
 		Assert.assertFalse(result3); // non supprimable
 	}
 
 	//@Test
 	public void isAirportUsed() throws SQLException{ // doit renvoyer vrai si l'aéroport est utilisé
 		MysqlDao dao = new MysqlDao();
 		// à réajuster à chaque test :
 		boolean test1 = dao.isAirportUsed("BRN");
 		boolean test2 = dao.isAirportUsed("ORD");
 		Assert.assertTrue(test1);
 		Assert.assertFalse(test2);
 	}
 	
 	//@Test
 	public void getVolEnAttenteById() throws Exception{ // doit renvoyer un objet Vol correspondant à l'id en paramètre
 		MysqlDao dao = new MysqlDao();
 		Vol volRecupere = dao.getVolEnAttenteById("TMP2");
 		
 		Date dateDepart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-06 02:00:00");
 		Date dateArrivee = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-06 15:15:00");
 		
 		Aeroport aeroportDepart = dao.getAeroportByVille("Sydney");
 		Aeroport aeroportArrivee = dao.getAeroportByVille("Berne");
 		// à réajuster à chaque test :
 		Vol volTest = new Vol("TMP2", aeroportDepart, aeroportArrivee, dateDepart, dateArrivee, 795, 1480, "", "", "", "", "");
 		Assert.assertEquals(volTest.getId(), volRecupere.getId());
 		Assert.assertEquals(volTest.getAeroportDepart().getCodeAeroport(), volRecupere.getAeroportDepart().getCodeAeroport());		
 		Assert.assertEquals(volTest.getDuree(), volRecupere.getDuree());
 	}
 	
 	//@Test
 	public void getVolProgrammeById() throws Exception{ // doit renvoyer un objet Vol correspondant à l'id en paramètre
 		MysqlDao dao = new MysqlDao();
 		Vol volRecupere = dao.getVolProgrammeById("DF3");
 		
 		Date dateDepart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-24 02:20:00");
 		Date dateArrivee = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-24 18:34:00");
 		
 		Aeroport aeroportDepart = dao.getAeroportByVille("Casablanca");
 		Aeroport aeroportArrivee = dao.getAeroportByVille("Honolulu");
 		// à réajuster à chaque test :
 		Vol volTest = new Vol("DF3", aeroportDepart, aeroportArrivee, dateDepart, dateArrivee, 974, 1524, "P0001", "C0006", "H0002", "H0007", "H0006");
 		Assert.assertEquals(volTest.getId(), volRecupere.getId());
 		Assert.assertEquals(volTest.getAeroportDepart().getCodeAeroport(), volRecupere.getAeroportDepart().getCodeAeroport());		
 		Assert.assertEquals(volTest.getCodeCopilote(), volRecupere.getCodeCopilote());
 	}
 	
 	//@Test
 	public void deleteVolEnAttente() throws Exception{ // doit supprimer le vol "en attente" dont le code est en paramètre
 		MysqlDao dao = new MysqlDao();
 		// À modifier avant de relancer un test :
 		boolean result1 = dao.deleteVolEnAttente("TMP4"); // ce vol existe et peut être supprimé
 		boolean result2 = dao.deleteVolEnAttente("TMP42"); // ce vol n'existe pas
 		Assert.assertTrue(result1); // renvoie vrai si le vol a été supprimé
 		Assert.assertFalse(result2); // non supprimable
 	}
 	
	@Test
 	public void updateVolEnAttente() throws Exception{
 		MysqlDao dao = new MysqlDao();
 		// (On peut le modifier avant de relancer un test) :
		// TODO : vérifier pour 1 ou 2h
		Date dateDepart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-03-30 02:00:00");
		Date dateArrivee = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-03-30 15:15:00");
 		
 		Aeroport aeroportDepart = dao.getAeroportByVille("Casablanca");
 		Aeroport aeroportArrivee = dao.getAeroportByVille("Honolulu");
 		// à réajuster à chaque test :
		Vol volTest1 = new Vol("TMP1", aeroportDepart, aeroportArrivee, dateDepart, dateArrivee, 720, 1000, "P0001", "C0006", "H0002", "", "");
		Vol volTest2 = new Vol("TMP42", aeroportDepart, aeroportArrivee, dateDepart, dateArrivee, 720, 1000, "P0001", "C0006", "H0002", "", "");
 		boolean result1 = dao.updateVolEnAttente(volTest1);
 		boolean result2 = dao.updateVolEnAttente(volTest2); // n'existe pas, ne peut pas être modifié
 		Vol v1 = dao.getVolEnAttenteById("TMP1"); // on récupère le vol modifié
 		Assert.assertTrue(result1); // true
 		Assert.assertFalse(result2); // false
 		Assert.assertEquals(volTest1.getCodePilote(), v1.getCodePilote()); // les codes pilotes coïcident
 	}
 
 	//@Test
 	public void getPilotes() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		List<String> pilotes = dao.getPilotes();
 		boolean test1 = pilotes.contains("P0001"); // la liste doit contenir P0001
 		boolean test2 = pilotes.contains("autreChaine"); // la liste ne contient pas cette chaine
 		Assert.assertTrue(test1); // true
 		Assert.assertFalse(test2); // false
 	}
 	
 	@Test
 	public void getCopilotes() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		List<String> copilotes = dao.getCopilotes();
 		boolean test1 = copilotes.contains("C0001"); // la liste doit contenir C0001
 		boolean test2 = copilotes.contains("autreChaine"); // la liste ne contient pas cette chaine
 		Assert.assertTrue(test1); // true
 		Assert.assertFalse(test2); // false
 	}
 	
 	//@Test
 	public void getHotessesSt() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		List<String> hotessesSt = dao.getHotessesSt();
 		boolean test1 = hotessesSt.contains("H0002"); // la liste doit contenir H0002 (steward)
 		boolean test2 = hotessesSt.contains("H0003"); // la liste doit contenir H0003 (hôtesse)
 		boolean test3 = hotessesSt.contains("autreChaine"); // la liste ne contient pas cette chaine
 		Assert.assertTrue(test1); // true
 		Assert.assertTrue(test2); // true
 		Assert.assertFalse(test3); // false
 	}
 	
 	//@Test
 	public void updateTarifVolProgramme() throws SQLException{
 		MysqlDao dao = new MysqlDao();
 		// à réajuster à chaque test :
 		boolean test1 = dao.updateTarifVolProgramme("DF1", 700.77F); // le vol existe, le tarif est modifiable
 		boolean test2 = dao.updateTarifVolProgramme("DF42", 700.77F); // le vol n'existe pas
 		Vol v = dao.getVolProgrammeById("DF1"); // on récupère le vol modifié
 		Assert.assertTrue(test1); // true
 		Assert.assertFalse(test2); // false
 		// on vérifie que le tarif a bien été changé (on le passe en String uniquement pour le test)
 		Assert.assertEquals("700.77", String.valueOf(v.getTarif()));
 	}
 	
 	//@Test
 	public void volReserve() throws SQLException{ // doit renvoyer "vrai" si au moins une place est réservée sur le vol
 		MysqlDao dao = new MysqlDao();
 		// à réajuster à chaque test :
 		boolean test1 = dao.volReserve("DF1"); // une réservation existe sur ce vol
 		boolean test2 = dao.volReserve("DF9"); // pas de réservation sur ce vol
 		boolean test3 = dao.volReserve("DFTest"); // ce vol n'existe pas
 		Assert.assertTrue(test1); // true
 		Assert.assertFalse(test2); // false
 		Assert.assertFalse(test3); // false
 	}
 	
 	//@Test
 	public void deleteVolProgramme() throws SQLException{ // supprime un vol s'il n'y a pas de réservation dessus
 		MysqlDao dao = new MysqlDao();
 		// à réajuster à chaque test :
 		boolean test1 = dao.deleteVolProgramme("DF9"); // ce vol n'a pas de réservation
 		boolean test2 = dao.deleteVolProgramme("DF1"); // ce vol a des réservations
 		boolean test3 = dao.deleteVolProgramme("DFTest"); // ce vol n'existe pas
 		Assert.assertTrue(test1); // true
 		Assert.assertFalse(test2); // false
 		Assert.assertFalse(test3); // false
 	}
 	
 	//@Test
 	public void confirmVol() throws Exception{ // 
 		MysqlDao dao = new MysqlDao();
		Date dateDepart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-03-30 03:15:00");
		Date dateArrivee = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-03-30 15:15:00");
 		
 		Aeroport aeroportDepart = dao.getAeroportByVille("Casablanca");
 		Aeroport aeroportArrivee = dao.getAeroportByVille("Honolulu");
 		Vol v = new Vol(aeroportDepart, aeroportArrivee, dateDepart, dateArrivee, 720, 1000, "P0001", "C0003", "H0003", "H0004", "H0005");
 		boolean test = dao.confirmVol(v);
 		// à réajuster à chaque test
 		Vol volRecupere = dao.getVolProgrammeById("DF11");
 		Assert.assertTrue(test);
 		Assert.assertEquals(v.getCodePilote(), volRecupere.getCodePilote());
 		Assert.assertEquals(v.getAeroportDepart().getCodeAeroport(), volRecupere.getAeroportDepart().getCodeAeroport());
 	}
 }
