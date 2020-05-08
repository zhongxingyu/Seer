package life;
 
 
 
 import org.junit.Test;
 
 public class TestCellule {
 
 	/**
 	 * Test de l'initialisation des Vivantes
 	 * @throws Exception
 	 */
 	@Test
 	public void testVivante() throws Exception {
 		Vivante a = new Vivante(1, 2);
 		if(!a.vivante()){
 			throw(new Exception());
 		}
 	}
 	
 
 	/**
 	 * Test de l'initialisation des Mortes
 	 * @throws Exception
 	 */
 	@Test
 	public void TestMorte() throws Exception{
 		Morte a = new Morte(3,1);
 		if(a.vivante()){
 			throw (new Exception());
 		}
 	}
 
 }
