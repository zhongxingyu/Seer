 package net.usefulbits;
 
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
import net.usefulbits.dao.JpaPersonDao;
 import net.usefulbits.model.Person;
 
 import javax.annotation.Resource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 @ContextConfiguration
 public class PersonTest extends AbstractTransactionalJUnit4SpringContextTests {
 
    private JpaPersonDao fPersonDao;
     
     @Resource
    public void setPersonDao(JpaPersonDao personDao) {
         fPersonDao = personDao;
     }
 
     @Test
     public void testSave() {
         createAndSavePerson("David", 28);
         assertEquals(1, countRowsInTable("person"));
 
         Person david = getSinglePerson();
         assertEquals("Name not saved correctly", "David", david.getName());
         assertEquals("Age not saved correctly", 28, david.getAge());
     }
 
     @Test
     public void testGetById() {
         createAndSavePerson("David", 28);
         Person david = fPersonDao.getById(0);
         assertEquals(david.getName(), "David");
         assertEquals(david.getAge(), 28);
     }
 
     @Test
     public void testDelete() {
         createAndSavePerson("David", 28);
         Person david = fPersonDao.getById(0);
         fPersonDao.delete(david);
         fPersonDao.getEntityManager().flush();
         assertEquals("Deleting person failed.", 0, countRowsInTable("person"));
     }
 
     @Test
     public void testUpdate() {
         createAndSavePerson("David", 28);
         Person person = fPersonDao.getById(0);
         assertEquals("The Person didn't get saved.", 1, countRowsInTable("person"));
 
         person.setName("Jane");
         person.setAge(21);
         fPersonDao.update(person);
         fPersonDao.getEntityManager().flush();
 
         Person jane = getSinglePerson();
         assertEquals(1, countRowsInTable("person"));
         assertEquals("The name didn't get changed", "Jane", jane.getName());
         assertEquals("The Age didn't get changed", 21, jane.getAge());
     }
 
     private Person getSinglePerson() {
         return simpleJdbcTemplate.queryForObject(
                 "select * from person where id = ?", new PersonRowMapper(), 0);
     }
 
     private void createAndSavePerson(String name, int age) {
         Person person = new Person();
         person.setName(name);
         person.setAge(age);
 
         fPersonDao.save(person);
 
         // Must flush the person to the database before trying to find it
         fPersonDao.getEntityManager().flush();
     }
 
     private static class PersonRowMapper implements ParameterizedRowMapper<Person> {
         public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
             Person result = new Person();
             result.setName(rs.getString("name"));
             result.setAge(rs.getInt("age"));
             return result;
         }
     }
 }
