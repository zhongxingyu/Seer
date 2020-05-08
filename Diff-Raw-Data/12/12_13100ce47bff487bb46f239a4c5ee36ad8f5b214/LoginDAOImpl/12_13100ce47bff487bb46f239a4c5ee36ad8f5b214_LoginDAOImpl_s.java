 package com.vikas.dao;
 
 import java.util.Date;
 
 import javax.sql.DataSource;
 
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
 import org.springframework.jdbc.core.namedparam.SqlParameterSource;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 
 import com.vikas.domain.Person;
 import com.vikas.domain.PersonMapper;
 
 /**
  * 
  * @author Vikas Sharma
  */
 public class LoginDAOImpl implements LoginDAO {
 
 	private JdbcTemplate jdbcTemplate;
 	private SimpleJdbcInsert insertPerson;
 
 	public void setDataSource(DataSource dataSource) {
 
 		jdbcTemplate = new JdbcTemplate(dataSource);
 		insertPerson = new SimpleJdbcInsert(dataSource).withTableName("PERSON")
 				.usingGeneratedKeyColumns("PERSON_ID");
 	}
 
 	@Override
 	public void addPerson(Person person) {
 
 		SqlParameterSource parameters = new BeanPropertySqlParameterSource(
 				person);
 		Number personId = insertPerson.executeAndReturnKey(parameters);
 
 		String sql = "INSERT INTO PERSON_ROLE (PERSON_ID, ROLE) VALUES (?, ?)";
 
 		jdbcTemplate.update(sql, personId.longValue(), person.getPersonRole()
 				.getRole());
 
 		person.setPersonId(personId.longValue());
 	}
 
 	@Override
 	public boolean activatePerson(long pid, Integer authKey) {
 
 		Person person = findByPID(pid);
 
 		if (person == null || !authKey.equals(person.getAuthKey())) {
 			return false;
 		}
 
 		String sql = "UPDATE PERSON SET STATUS = ?, ACTIVATED = ? WHERE PERSON_ID = ?";
 
 		jdbcTemplate.update(sql, "Active", new Date(), pid);
 
 		return true;
 	}
 
 	@Override
 	public Person findByPID(long pid) {
 
 		String sql = "SELECT * FROM PERSON WHERE PERSON_ID = ?";
 
 		return jdbcTemplate.queryForObject(sql, new PersonMapper(), pid);
 	}
 
 	@Override
 	public Person findByUsername(String username) {
 
		String sql = "SELECT * FROM PERSON WHERE NAME = ?";
 
 		Person person = null;
 		try {
 			person = jdbcTemplate.queryForObject(sql, new PersonMapper(),
 					username);
 		} catch (EmptyResultDataAccessException e) {
 			return null;
 		}
 
 		return person;
 	}
 
 	@Override
 	public Person findByEmailAddress(String emailAddress) {
 
 		String sql = "SELECT * FROM PERSON WHERE EMAIL_ADDRESS = ? LIMIT 1";
 
 		Person person = null;
 		try {
 			person = jdbcTemplate.queryForObject(sql, new PersonMapper(),
 					emailAddress);
 		} catch (EmptyResultDataAccessException e) {
 			return null;
 		}
 
 		return person;
 	}
 
 	@Override
 	public void updatePassword(long pid, String encodedPassword) {
 
 		String sql = "UPDATE PERSON SET ENCODED_PASSWORD = ? WHERE PERSON_ID = ?";
 
 		jdbcTemplate.update(sql, encodedPassword, pid);
 	}
 
 }
