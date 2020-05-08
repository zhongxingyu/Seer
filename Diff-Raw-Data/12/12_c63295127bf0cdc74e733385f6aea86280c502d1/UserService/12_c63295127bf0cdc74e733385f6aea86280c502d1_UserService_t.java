 package mta.devweb.bitcoinbuddy.controller.services;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import mta.devweb.bitcoinbuddy.controller.GenericResponse;
 import mta.devweb.bitcoinbuddy.model.beans.DataList;
 import mta.devweb.bitcoinbuddy.model.beans.HistoryTask;
 import mta.devweb.bitcoinbuddy.model.beans.User;
 import mta.devweb.bitcoinbuddy.model.beans.enums.Command;
 import mta.devweb.bitcoinbuddy.model.db.UserDao;
 
 public class UserService {
 	private UserDao userDao;
 
 	public UserService() {
 		this.userDao = new UserDao();
 		;
 	}
 
 	public GenericResponse login(String userName, String password) {
 		GenericResponse response = new GenericResponse();
 
 		User user = null;
 		try {
 			user = userDao.getUserByUserName(userName);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			response.setMessage(e.getMessage());
 			return response;
 		}
 		if (user != null) {
 			if (password.equals(user.getPassword())) {
 				response.setIsSuccess(true);
 				response.setData(user);
 			} else {
 				response.setMessage("user/password mismatch");
 			}
 		}
 		return response;
 	}
 
 	public GenericResponse getUsersByHistoryCount(int minimumCommands) {
 		GenericResponse response = new GenericResponse();
 
 		List<User> userList = null;
 		try {
 			userList = userDao.getUsersByHistoryCount(minimumCommands);
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setMessage(e.getMessage());
 			return response;
 		}
 		if (userList != null) {
 			response.setIsSuccess(true);
 			response.setData(new DataList(userList));
 		}
 		return response;
 	}
 
 	public GenericResponse getUser(int userId) {
 		GenericResponse response = new GenericResponse();
 
 		User user = null;
 		try {
 			user = (User) userDao.read(userId);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			response.setMessage(e.getMessage());
 			return response;
 		}
 		if (user != null) {
 			response.setIsSuccess(true);
 			response.setData(user);
 		}
 		return response;
 	}
 
 	public GenericResponse subscribe(String firstName, String lastName,
 			String userName, String password) {
 		GenericResponse response = new GenericResponse();
 
 		try {
 			if (userDao.getUserByUserName(userName) != null) {
 				response.setMessage("user name already exist");
 			} else {
 				User user = new User(firstName, lastName, userName, password);
 				if (userDao.create(user) == true) {
 					response.setIsSuccess(true);
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			response.setMessage(e.getMessage());
 			return response;
 		}
 		return response;
 	}
 
 	public GenericResponse updateSubscriber(int userId, String firstName,
 			String lastName, String password) {
 		GenericResponse response = new GenericResponse();
 
 		try {
 			User user = new User(userId, firstName, lastName, null, password);
 			if (userDao.update(user) == true) {
 				response.setIsSuccess(true);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			response.setMessage(e.getMessage());
 			return response;
 		}
 		return response;
 	}
 
 	public GenericResponse deleteUser(int userId) {
 		GenericResponse response = new GenericResponse();
 
 		try {
 			if (userDao.delete(userId) == true) {
 				response.setIsSuccess(true);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			response.setMessage(e.getMessage());
 			return response;
 		}
 		return response;
 	}
 }
