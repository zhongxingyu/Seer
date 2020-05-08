 package com.vikas.domain;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.springframework.jdbc.core.RowMapper;
 
 /**
  * 
  * @author Vikas Sharma
  * 
  */
 public class PersonMapper implements RowMapper<Person> {
 
 	@Override
 	public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
 
 		Person person = new Person();
 
 		person.setPersonId(rs.getLong("PERSON_ID"));
 		person.setName(rs.getString("NAME"));
 		person.setEncodedPassword(rs.getString("ENCODED_PASSWORD"));
 		person.setEmailAddress(rs.getString("EMAIL_ADDRESS"));
 		person.setFirstName(rs.getString("FIRST_NAME"));
 		person.setLastName(rs.getString("LAST_NAME"));
 		person.setGender(rs.getString("GENDER"));
 		person.setCountry(rs.getString("COUNTRY"));
 		person.setIpAddress(rs.getString("IP_ADDRESS"));
 		person.setCreated(rs.getDate("CREATED"));
 		person.setActivated(rs.getDate("ACTIVATED"));
 		person.setAuthKey(rs.getInt("AUTH_KEY"));
 		person.setStatus(rs.getString("STATUS"));
 
		PersonRole personRole = new PersonRole();
		personRole.setPerson(person);
		personRole.setRole(rs.getInt("ROLE"));
		person.setPersonRole(personRole);

 		return person;
 	}
 }
