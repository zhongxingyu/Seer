 package iaws.NBMR.services.impl;
 
 import static org.junit.Assert.*;
 import iaws.NBMR.domaines.Utilisateur;
 import iaws.NBMR.exception.CustomException;
 
 import org.junit.Assert;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 public class TestUtilisateurService {
 	
 	private UtilisateurServiceImpl utilisateurService;
 	
 	@Before
 	public void configureUtilisateurService(){
 		utilisateurService = new UtilisateurServiceImpl();
 	}
 
 	@Test
 	public void testInscrireUtilisateur() {
 		
		Utilisateur utilisateur = new Utilisateur("mynom", "myprenom", "myemail@univ-tlse3.fr", "Avenue des champs élysées");
 		
 		Assert.assertEquals("", "mynom", utilisateur.getNom());
 		Assert.assertEquals("", "myprenom", utilisateur.getPrenom());
		Assert.assertEquals("", "myemail@univ-tlse3.fr", utilisateur.getEmail());
 		Assert.assertEquals("", "Avenue des champs élysées", utilisateur.getAdresse());
 
 		try {
 			utilisateurService.inscrireUtilisateur(utilisateur);
 		} catch (CustomException e) {
 			fail("Exception levée: " + e.getCode() + ": " + e.getMessage());
 		}
 	}
	
	@Ignore
	@Test
	public void testValiditeAdresseMail(){
		
		
		
	}
 
 }
