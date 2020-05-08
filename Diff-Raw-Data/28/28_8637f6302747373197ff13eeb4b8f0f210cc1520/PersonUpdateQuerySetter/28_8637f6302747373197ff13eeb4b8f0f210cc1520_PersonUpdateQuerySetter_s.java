 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package poc.springbatch.beans;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementSetter;
 import poc.springbatch.entities.Person;
 
 /**
  *
  * @author subhadip.chatterjee@tcs.com
  */
public class PersonUpdateQuerySetter implements PreparedStatementSetter {
    
    private final static Logger logger = LoggerFactory.getLogger(PersonUpdateQuerySetter.class);
    private Person person;

    public Person getPerson() {
        return person;
    }
 
    public void setPerson(Person person) {
        this.person = person;
    }
 
     @Override
    public void setValues(PreparedStatement ps) throws SQLException {
         if (logger.isInfoEnabled()) {
             logger.info("PreparedStatementSetter.setValues() begins...");
        }
        if (person == null) {
            throw new RuntimeException();
        }
        ps.setString(1, person.getId());        
     }
 }
