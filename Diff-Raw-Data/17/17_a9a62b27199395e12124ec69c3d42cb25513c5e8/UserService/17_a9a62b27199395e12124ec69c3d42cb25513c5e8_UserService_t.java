 package com.algo.webshop.server.services;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Service;
 
 import com.algo.webshop.common.domain.User;
 import com.algo.webshop.common.domainimpl.IUser;
 import com.algo.webshop.server.jdbc.UserRowMapper;
 
 @Service("userService")
 public class UserService implements IUser {
 	@Autowired
 	protected JdbcTemplate jdbcTemplate;
 
 	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
 		this.jdbcTemplate = jdbcTemplate;
 	}
 
 	@Override
 	public User getUserByLogin(String login) {
 		String SQL = "select * from users where login = ?";
		User user;
		try {
			user = jdbcTemplate.queryForObject(SQL, new Object[] { login },
					new UserRowMapper());
		} catch (EmptyResultDataAccessException ex) {
			user = null;
		}
 		return user;
 	}
 
 	@Override
 	public User getUserByLogPass(String login, String pass) {
 		User user;
 		String SQL = "select * from users where login=? AND pass=?";
 		try {
			user = jdbcTemplate.queryForObject(SQL,
					new Object[] { login, pass }, new UserRowMapper());
 		} catch (EmptyResultDataAccessException ex) {
			user = null;
 		}
 		return user;
 	}
 
 	@Override
 	public User getUserById(String id) {
 		String SQL = "select * from users where id = ?";
 		User user = jdbcTemplate.queryForObject(SQL, new Object[] { id },
 				new UserRowMapper());
 		return user;
 	}
 
 	@Override
 	public List<User> getAllUsers() {
 		String SQL = "select * from users";
 		List<User> userList = jdbcTemplate.query(SQL, new UserRowMapper());
 		return userList;
 	}
 
 	@Override
 	public void addUserInDataBase(User user) {
 		String SQL = "insert into users (name,email,phone,login,pass) values (?,?,?,?,?)";
 		jdbcTemplate.update(SQL, user.getName(), user.getEmail(),
 				user.getPhone(), user.getLogin(), user.getPass());
 	}
 
 	@Override
 	public void updateUser(User user) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void deleteUser(String id) {
 		String SQL = "delete from users where id = ?";
 		jdbcTemplate.update(SQL, id);
 	}
 
 	@Override
 	public String getUserName(int id) {
 		String SQL = "select name from users where id = ?";
 		String name = jdbcTemplate.queryForObject(SQL, String.class);
 
 		return name;
 	}
 
 }
