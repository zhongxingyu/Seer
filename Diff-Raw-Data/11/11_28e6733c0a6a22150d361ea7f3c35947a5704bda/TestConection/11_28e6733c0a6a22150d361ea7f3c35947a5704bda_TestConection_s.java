 package com.navigrad.m2m.server.gps;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.validation.Validation;
 import javax.validation.ValidatorFactory;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.navigrad.m2m.server.gps.dao.GPSDataDAO;
 import com.navigrad.m2m.server.gps.dao.IGPSDataDAO;
 import com.navigrad.m2m.server.gps.dao.ITransportDAO;
 import com.navigrad.m2m.server.gps.dao.IUserDAO;
 import com.navigrad.m2m.server.gps.dao.TransportDAO;
 import com.navigrad.m2m.server.gps.dao.UserDAO;
 import com.navigrad.m2m.server.gps.dao.home.UserDAOHome;
 import com.navigrad.m2m.server.gps.entity.GPSData;
 import com.navigrad.m2m.server.gps.entity.Transport;
 import com.navigrad.m2m.server.gps.entity.User;
 
 public class TestConection {
 
 	public static void main(String[] args) {
 		// TestConection testConection = new TestConection();
 		ApplicationContext ctx = new ClassPathXmlApplicationContext("gps.xml");
 		TestConection testConection = (TestConection) ctx
 				.getBean("TestConection");
 
 		// add
 //		 System.out.println(testConection.addUser("Николай", "Волынец",
 //		 "new", ""));
 		List<User> savedUsers = testConection.getUserDAO().findUsers();
 		System.out.println(savedUsers);
 
 		System.out.println("user: "
 				+ testConection.getUserDAO().findUserByLogin("new"));
 
 		// add
 //		 System.out.println(testConection.addTransport(savedUsers.get(1),
 //		 "ИМЕЙ123213523452345", "Трактор2123"));
 
 		List<Transport> savedTransports = testConection.getTransportDAO()
 				.findTransports();
 		System.out.println(savedTransports);
 
 		// add
		 System.out.println(testConection.addGPSData(savedTransports.get(0),
		 Calendar.getInstance().getTime(), 540.5640, 0564.0, 0564.46));
 
 		List<GPSData> savedGpsData = testConection.getGpsDAO().findGPSs();
 		System.out.println(savedGpsData);
 
 		// System.out.println(testConection.getGpsDao().getById(0L));
 
 	}
 
 	IGPSDataDAO gpsDAO;
 
 	IUserDAO userDAO;
 
 	ITransportDAO transportDAO;
 
 	private Long addGPSData(Transport transport, Date time, Double x, Double y,
 			Double z) {
 		GPSData gps = new GPSData();
 //		gps.setTransport(transport);
 		gps.setTime(time);
 		gps.setX(x);
 		gps.setY(y);
 		gps.setZ(z);
 		return new GPSDataDAO().add(gps);
 	}
 
 	private Long addTransport(User user, String imei, String name) {
 		Transport transport = new Transport();
 		transport.setImei(imei);
 		transport.setName(name);
 		transport.setUser(user);
 		return new TransportDAO().add(transport);
 
 	}
 
 	private Long addUser(String firstName, String secondName, String login, String password) {
 		UserDAO userDAO = new UserDAO();
 		User user = new User();
 		user.setFirstName(firstName);
 		user.setLastName(secondName);
 		user.setLogin(login);
 		user.setPassword(password);
 
 		return userDAO.add(user);
 
 	}
 
 	public IGPSDataDAO getGpsDAO() {
 		return gpsDAO;
 	}
 
 	public ITransportDAO getTransportDAO() {
 		return transportDAO;
 	}
 
 	public IUserDAO getUserDAO() {
 		return userDAO;
 	}
 
 	public void setGpsDAO(IGPSDataDAO gpsDAO) {
 		this.gpsDAO = gpsDAO;
 	}
 
 	public void setTransportDAO(ITransportDAO transportDAO) {
 		this.transportDAO = transportDAO;
 	}
 
 	public void setUserDAO(IUserDAO userDAO) {
 		this.userDAO = userDAO;
 	}
 }
