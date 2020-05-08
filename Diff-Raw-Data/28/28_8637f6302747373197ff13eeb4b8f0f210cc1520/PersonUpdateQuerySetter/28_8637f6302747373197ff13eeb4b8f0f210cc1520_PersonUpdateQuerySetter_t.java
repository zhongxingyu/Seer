 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package poc.springbatch.beans;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
 import poc.springbatch.entities.Person;
 
 /**
  *
  * @author subhadip.chatterjee@tcs.com
  */
public class PersonUpdateQuerySetter implements ItemPreparedStatementSetter<Person> {
 
    private final static Logger logger = LoggerFactory.getLogger(PersonUpdateQuerySetter.class);
 
     @Override
    public void setValues(Person item, PreparedStatement ps) throws SQLException {
         if (logger.isInfoEnabled()) {
             logger.info("PreparedStatementSetter.setValues() begins...");
        }        
        ps.setString(1, item.getId());
     }
 }
