 /**
  * 
  */
 package br.study.ebah.miguel.cdsCatalog.sql.access;
 
 import java.sql.SQLException;
 
import org.junit.Assert;
 import org.junit.Test;
 
 import br.study.ebah.miguel.cdsCatalog.sql.MySQLConnectionFactory;
 
 /**
  * @author miguel
  * 
  */
 public class MySQLConnectionFactoryTest {
 
 	@Test
 	public void getConnectionTest() throws SQLException, ClassNotFoundException {
 		Assert.assertNotNull(new MySQLConnectionFactory().getConnection());
 	}
 
 }
