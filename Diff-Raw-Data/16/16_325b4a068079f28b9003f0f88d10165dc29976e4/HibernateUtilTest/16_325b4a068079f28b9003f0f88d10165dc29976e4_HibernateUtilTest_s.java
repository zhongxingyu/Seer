 package org.softwaresynthesis.mytalk.server.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import org.junit.Test;
 
 /**
  * Test dei metodi della classe {@link HiberanteUtil}
  * 
  * @author 	Andrea Meneghinello
  * @version %I%, %G%
  */
 public class HibernateUtilTest 
{
	private static HibernateUtil tester;
	
 	/**
 	 * Testa l'effettiva presenza di un unica
 	 * istanza della classe
 	 * 
 	 * @author	Andrea Meneghinello
 	 * @version	%I%, %G%
 	 */
 	@Test
 	public void testInstance()
 	{
		HibernateUtil instance = null;
		tester = HibernateUtil.getInstance();
		instance = HibernateUtil.getInstance();
		assertNotNull(tester);
		assertNotNull(instance);
		assertEquals(tester, instance);
 	}
 }
