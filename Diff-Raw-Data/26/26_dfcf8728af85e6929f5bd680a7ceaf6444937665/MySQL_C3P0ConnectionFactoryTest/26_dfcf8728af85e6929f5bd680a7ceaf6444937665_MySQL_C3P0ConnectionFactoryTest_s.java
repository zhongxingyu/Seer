 /**
  * 
  */
 package br.study.ebah.miguel.cdsCatalog.sql.access;
 
import java.sql.SQLException;

import junit.framework.Assert;

 import org.junit.Test;
 
 import br.study.ebah.miguel.cdsCatalog.sql.MySQL_C3P0ConnectionFactory;
 
 /**
  * @author miguel
  * 
  */
 public class MySQL_C3P0ConnectionFactoryTest {
 
 	@Test
	public void getConnectionTest() throws SQLException, ClassNotFoundException {
		Assert.assertNotNull(MySQL_C3P0ConnectionFactory.getInstance().getConnection());
 	}
 
 }
