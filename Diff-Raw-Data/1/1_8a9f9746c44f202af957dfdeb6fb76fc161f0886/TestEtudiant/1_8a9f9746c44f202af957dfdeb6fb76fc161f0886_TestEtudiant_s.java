 package jdbc;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import fr.eservice.common.Etudiant;
 import fr.eservice.common.EtudiantDao;
 import fr.eservice.jdbc.EtudiantJdbcDao;
 
 public class TestEtudiant {
 	
 	private static EtudiantDao dao;
 	
 	@BeforeClass
 	public static void initDB() {
 		dao = new EtudiantJdbcDao();
 	}
 	
 	@Before
 	public void clearEtudiant() throws SQLException {
 		dao.clear();
 	}
 	
 	private int saveEtudiant(String firstName, String lastName, int age) {
 		Etudiant etudiant = new Etudiant();
 		etudiant.setFirstname(firstName);
 		etudiant.setLastname(lastName);
 		etudiant.setAge(age);
 		dao.save(etudiant);
 		return etudiant.getId();
 	}
 	
 	@Test
 	public void canSave() {
 		Etudiant etudiant = new Etudiant();
 		etudiant.setFirstname("Guillaume");
 		etudiant.setLastname("Dufrêne");
 		etudiant.setAge(31);
 		
 		boolean saved = dao.save(etudiant);
 		
 		assertTrue( "Etudiant n'a pas été sauvegardé ?", saved );
 		assertTrue( "Un identifiant doit être placé", etudiant.getId() > 0 );
 	}
 	
 	@Test
 	public void canLoad() {
 		int id = saveEtudiant("Guillaume", "Dufrêne", 31);
 		Etudiant etudiant = dao.load(id);
 		
 		assertEquals("Guillaume", etudiant.getFirstname());
 		assertEquals("Dufrêne", etudiant.getLastname());
 		assertEquals(31, etudiant.getAge());
 		assertEquals(id, etudiant.getId());
 	}
 	
 	@Test
 	public void loadNotFound() {
 		Etudiant etudiant = dao.load( 0xCAFE);
 		assertNull("Doit retourner null lorsqu'il n'existe pas d'étudiant de cet ID", etudiant);
 	}
 	
 	private int[] init_BeforeAfter() {
 		int[] ids = new int[] {
 			saveEtudiant("Guillaume",  "Dufrêne", 31),
 			saveEtudiant("Jean",       "Dupond",  28),
 			saveEtudiant("Christophe", "Martin",  32),
 			saveEtudiant("Julien",     "Durand",  27)
 		};
 		return ids;
 	}
 	
 	@Test
 	public void testAfter() {
 		int[] ids = init_BeforeAfter();
 		Etudiant etudiant;
 		
 		etudiant = dao.after( ids[0] );
 		assertEquals( "Dupond", etudiant.getLastname());
 		
 		etudiant = dao.after( ids[1] );
 		assertEquals( "Martin", etudiant.getLastname());
 	}
 	
 	@Test
 	public void testAfterLast() {
 		int[] ids = init_BeforeAfter();
 		Etudiant etudiant;
 		etudiant = dao.after( ids[3] );
 		assertNull( etudiant );
 	}
 	
 	@Test
 	public void testBefore() {
 		int[] ids = init_BeforeAfter();
 		Etudiant etudiant;
 		
 		etudiant = dao.before( ids[3] );
 		assertEquals( "Christophe", etudiant.getFirstname());
 		
 		etudiant = dao.before( ids[1] );
 		assertEquals( "Guillaume", etudiant.getFirstname());
 	}
 
 	@Test
 	public void testBeforeFirst() {
 		int[] ids = init_BeforeAfter();
 		Etudiant etudiant;
 		etudiant = dao.before( ids[0] );
 		assertNull( etudiant );
 	}
 
 }
