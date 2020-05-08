 package test;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 
 import modele.Billet;
 import modele.Billeterie;
 
 
 
 
 public class TestBillet extends TestCase {
 	Billeterie bill;
 	Billet billet, billet1, billet2;
 	protected void setUp() {
 		bill = new Billeterie("database.sqlite");
 
 		try {
 			billet = (Billet) bill.getListeBillets().getObjetById(100);
 			billet1 = (Billet) bill.getListeBillets().getObjetById(100);
 			billet2 = (Billet) bill.getListeBillets().getObjetById(101);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	public void testModifieQt() {
  		setUp();
  		int sub = billet.getNbPlaceSub();
  		int nonSub = billet.getNbPlace();
  		billet.modifieQt(4, true);
  		assertEquals(sub + 4, billet.getNbPlaceSub());
  		billet.modifieQt(6, false);
  		assertEquals(sub + 4, billet.getNbPlaceSub());
  		assertEquals(nonSub + 10, billet.getNbPlace());
  		
  		billet.modifieQt(-100000, false);
  		assertEquals(0, billet.getNbPlaceSub());
  		assertEquals(0, billet.getNbPlace());
  		
  		billet.modifieQt(500, false);
  		billet.modifieQt(200, true);
  		assertEquals(200, billet.getNbPlaceSub());
  		assertEquals(700, billet.getNbPlace());
 	}
 
 	public void testGetPrixRed() {
 		setUp();
 		assertEquals(4, billet.getPrixRed(), 0);
 	}
 
 	public void testGetPrixNor() {
 		setUp();
 		assertEquals(5, billet.getPrixNor(), 0);
 	}
 
 	public void testBillet() {
 		setUp();
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("categorie", "Musique");
 		map.put("prix", 5.0);
 		map.put("prix_sub", 4.0);
 		map.put("sous_categorie", "jeune");
 		map.put("nb_sub_par_personne", 5);
 		map.put("nb_total", 500);
 		map.put("nb_sub", 200);
		bill.getListeBillets().ajouter(map);
 		try {
 			Billet modif = (Billet) bill.getListeBillets().getObjetById(Billet.getProchainId()-1);
 			assertEquals(200, modif.getNbPlaceSub());
 	 		assertEquals(500, modif.getNbPlace());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
  		
 	}
 
 
 	public void testGetId() {
 		setUp();
 		assertEquals(100, billet.getId());
 	}
 
 	public void testEqual() {
 		setUp();
 		assertTrue(billet.equals(billet1));
 		assertFalse(billet.equals(billet2));
 	}
 	
 	public void testModifie() {
 		setUp();
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("categorie", "Musique");
 		map.put("prix", 5.0);
 		map.put("prix_sub", 4.0);
 		map.put("sous_categorie", "jeune");
 		map.put("nb_sub_par_personne", 5);
 		map.put("nb_total", 500);
 		map.put("nb_sub", 200);
 		
 		try {
 			Billet modif = (Billet) bill.getListeBillets().getObjetById(109);
 			modif.modifie(map);
 			assertEquals(200, billet.getNbPlaceSub());
 	 		assertEquals(700, billet.getNbPlace());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
