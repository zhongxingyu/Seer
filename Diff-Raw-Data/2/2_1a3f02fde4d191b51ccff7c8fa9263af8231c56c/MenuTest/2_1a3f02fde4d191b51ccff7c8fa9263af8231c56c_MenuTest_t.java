 package dcll.jfri.projetConvertisseur;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * MenuTest est le Junit qui permet de test les méthodes
  * de la classe Menu
  * @author Charly Carrere
  * @version 1.0
  */
 public class MenuTest {
 	
 	/**
	 * Attribut d'un Menu qui permettre de tester les méthodes
 	 */
 	protected Menu menu;
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	/**
 	 * Méthode qui serra exécuté avant les Tests.
 	 */
 	@Before
 	public void setUp() {
 		menu = new Menu();
 	}
 
 	/**
 	 * Méthode qui serra exécté après les tests.
 	 * @throws Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test de la méthode getCheminSource
 	 * @throws Exception
 	 */
 	@Test
 	public void testCheminSource() throws Exception {
 		assertEquals(null, menu.getCheminSource());
 	}
 	
 	/**
 	 * Test de la méthode getAction
 	 * @throws Exception
 	 */
 	@Test
 	public void testAction() throws Exception {
 		assertEquals(-1, menu.getAction());
 	}
 	
 	/**
 	 * Test de la méthode getCheminResultat
 	 * @throws Exception
 	 */
 	@Test
 	public void testCheminResultat() throws Exception {
 		assertEquals(null, menu.getCheminResultat());
 	}
 
 }
