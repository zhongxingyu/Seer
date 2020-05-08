 /**
  * 
  */
 package br.study.ebah.miguel.cdsCatalog.sql.access;
 
import org.junit.Assert;
 import org.junit.Test;
 
 import br.study.ebah.miguel.cdsCatalog.sql.MySQL_C3P0ConnectionFactory;
 
 /**
  * @author miguel
  * 
  */
 public class MySQL_C3P0ConnectionFactoryTest {
 
 	@Test
	public void getConnectionTest() throws Exception {
		Assert.assertNotNull(MySQL_C3P0ConnectionFactory.getInstance()
				.getConnection());
 	}
 
 }
