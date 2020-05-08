 package no.magott.jz.txgotchas;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.AfterTransaction;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations="classpath:txgotchas-transactional-context.xml")
 @TransactionConfiguration(transactionManager="transactionManager")
 public class TransactionalTest {
 
 	@Autowired
 	DataSource datasource;
 	
 	private Map<String, Object> params;	
 	{	params = new HashMap<String, Object>();
 		params.put("id", 1);
 		params.put("name", "morten");}
 	
 	
 	@Test
 	@Transactional
 	@Rollback(false)
 	public void personIsInserted(){
 		assertThat(datasource).isNotNull().as("DataSource not set");		
 		SimpleJdbcInsert insert = new SimpleJdbcInsert(datasource);
 		int updated = insert.withTableName("person").execute(params);	
 		assertThat(updated).isEqualTo(1);
 	}
 	
 	
 	@AfterTransaction
 	public void checkInsertIsCommitted(){
 		SimpleJdbcTemplate template = new SimpleJdbcTemplate(datasource);
 		int tableCount=template.queryForInt("SELECT COUNT(*) FROM person");
		assertThat(tableCount).as("Insert was not committed").isEqualTo(1);
 	}
 	
 	
 	@After
 	public void cleanUpDb(){
 		SimpleJdbcTemplate template = new SimpleJdbcTemplate(datasource);
 		template.update("delete from person");
 	}
 	
 }
