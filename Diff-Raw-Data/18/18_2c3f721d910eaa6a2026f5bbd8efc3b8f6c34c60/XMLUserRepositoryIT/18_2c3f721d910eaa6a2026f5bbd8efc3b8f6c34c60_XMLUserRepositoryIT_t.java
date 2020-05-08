 package cours.ulaval.glo4003.persistence;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import cours.ulaval.glo4003.domain.Role;
 import cours.ulaval.glo4003.domain.User;
 
 public class XMLUserRepositoryIT extends ITTestBase {
 
 	private XMLUserRepository users;
 
 	private User teacher;
 	private User director;
 
 	@Before
	public void setUp()
			throws Exception {
 		users = new XMLUserRepository();
 
 		teacher = new User("enseignant", "Enseignant", "pass", Role.ROLE_Enseignant);
 		director = new User("directeur", "Directeur", "pass", Role.ROLE_Directeur);
 		director.addRole(Role.ROLE_Enseignant);
 		director.addRole(Role.ROLE_Usager);
 
 		users.store(teacher);
 
 		users.store(director);
	}

	@Test
	public void canSaveUser()
			throws Exception {
 
 		User responsable = new User("responsable", "Responsable", "pass", Role.ROLE_Responsable);
 		users.store(responsable);
 
 		User administrator = new User("administrateur", "Administrateur", "pass", Role.ROLE_Administrateur);
 		users.store(administrator);
 
 		User utilisateur = new User("utilisateur", "Utilisateur", "pass", Role.ROLE_Usager);
 		users.store(utilisateur);
 
 		assertEquals(3, director.getRoles().size());
 		assertEquals(utilisateur, users.findByIdul(utilisateur.getIdul()));
 		assertTrue(users.findAll().contains(teacher));
 	}
 
 	@Test
	public void canFindUser()
			throws Exception {
 		assertEquals(teacher.getIdul(), users.findByIdul("enseignant").getIdul());
 		assertEquals(director.getIdul(), users.findByIdul("directeur").getIdul());
 	}
 }
