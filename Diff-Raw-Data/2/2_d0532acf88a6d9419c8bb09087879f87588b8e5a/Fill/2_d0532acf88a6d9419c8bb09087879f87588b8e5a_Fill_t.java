 package com.VaV.execute;
 
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import com.VaV.model.Airport;
 import com.VaV.model.Flight;
 import com.VaV.model.Plane;
 import com.VaV.model.Reservation;
 import com.VaV.model.User;
 import com.VaV.persistence.AirportDAO;
 import com.VaV.persistence.FlightDAO;
 import com.VaV.persistence.PlaneDAO;
 import com.VaV.persistence.ReservationDAO;
 import com.VaV.persistence.UserDAO;
 
 public class Fill {
 	public void fill_database() {
 		UserDAO uDAO = new UserDAO();
 		User user = new User("More", "John", "john", "more", User.USER);
 		User user2 = new User("Arc", "Éric", "ah", "bon", User.USER);
 		User user3 = new User("Black", "White", "Black", "White", User.USER);
		User user4 = new User("Plus", "Jean", "Plus", "jean", User.USER);
 		User user5 = new User("Le Rouge", "Al", "rouge", "la", User.USER);
 		User user6 = new User("Twig", "Henri", "twig", "faepfia", User.USER);
 		User user7 = new User("Pierre", "Paul", "PierrePaul", "faefpojeafpo", User.USER);
 		User user8 = new User("Alfred", "Jean", "jeanalfred", "aefpeaf", User.USER);
 		User user9 = new User("Jack", "Hi", "JackHi", "eafpoja", User.USER);
 		User user10 = new User("Mizuno", "Taichi", "Tai", "azerty", User.USER);
 		
 		uDAO.create(user);
 		uDAO.create(user2);
 		uDAO.create(user3);
 		uDAO.create(user4);
 		uDAO.create(user5);
 		uDAO.create(user6);
 		uDAO.create(user7);
 		uDAO.create(user8);
 		uDAO.create(user9);
 		uDAO.create(user10);
 		
 		AirportDAO airportDAO = new AirportDAO();
 		Airport airport1 = new Airport();
 		Airport airport2 = new Airport();
 		
 		airport1.setName("Paris Charles de Gaulle (CDG)");
 		airport1 = airportDAO.find(airport1);
 		airport2.setName("Newark Liberty International (EWR)");
 		airport2 = airportDAO.find(airport2);
 		
 		PlaneDAO planeDAO = new PlaneDAO();
 		Plane plane1 = new Plane();
 		Plane plane2 = new Plane();
 		Plane plane3 = new Plane();
 		Plane plane4 = new Plane();
 		Plane plane5 = new Plane();
 		Plane plane6 = new Plane();
 		Plane plane7 = new Plane();
 		Plane plane8 = new Plane();
 		Plane plane9 = new Plane();
 		Plane plane10 = new Plane();
 		
 		plane1.setName("A318");
 		plane1.setSeats(107);
 		planeDAO.create(plane1);
 		
 		plane2.setName("A319");
 		plane2.setSeats(124);
 		planeDAO.create(plane2);
 		
 		plane3.setName("A320");
 		plane3.setSeats(150);
 		planeDAO.create(plane3);
 		
 		plane4.setName("A321"); plane4.setSeats(185); planeDAO.create(plane4);
 		plane5.setName("A330-200"); plane5.setSeats(253); planeDAO.create(plane5);
 		plane6.setName("A330-300"); plane6.setSeats(295); planeDAO.create(plane6);
 		plane7.setName("A340-200"); plane7.setSeats(261); planeDAO.create(plane7);
 		plane8.setName("A340-300"); plane8.setSeats(295); planeDAO.create(plane8);
 		plane9.setName("A340-500"); plane9.setSeats(313); planeDAO.create(plane9);
 		plane10.setName("A340-600"); plane10.setSeats(380); planeDAO.create(plane10);
 		
 		Flight f1 = new Flight();
 		Flight f2 = new Flight();
 		Flight f3 = new Flight();
 		Flight f4 = new Flight();
 		Flight f5 = new Flight();
 		Flight f6 = new Flight();
 		Flight f7 = new Flight();
 		Flight f8 = new Flight();
 		Flight f9 = new Flight();
 		Flight f10 = new Flight();
 		Flight f11 = new Flight();
 		FlightDAO fDAO = new FlightDAO();
 	
 		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("CEST"), Locale.FRANCE);
 		
 		c.set(2005, Calendar.DECEMBER, 25, 10, 5);
 		f1.set(airport1, airport2, plane1, c.getTime());
 		fDAO.create(f1);
 		c.set(2005, Calendar.DECEMBER, 30, 20, 30);
 		f2.set(airport2, airport1, plane2, c.getTime());
 		fDAO.create(f2);
 		c.set(2006, Calendar.JANUARY, 5, 9, 30);
 		f3.set(airport1, airport2, plane3, c.getTime());
 		fDAO.create(f3);
 		c.set(2012, Calendar.MARCH, 5, 9, 30);
 		f4.set(airport2, airport1, plane10, c.getTime());
 		fDAO.create(f4);
 		c.set(2012, Calendar.JANUARY, 5, 9, 30);
 		f11.set(airport1, airport2, plane1, c.getTime()); fDAO.create(f11);
 		c.set(2012, Calendar.FEBRUARY, 5, 9, 30);
 		f5.set(airport1, airport2, plane4, c.getTime()); fDAO.create(f5);
 		c.set(2012, Calendar.MARCH, 5, 9, 30);
 		f6.set(airport2, airport1, plane5, c.getTime()); fDAO.create(f6);
 		c.set(2012, Calendar.APRIL, 5, 9, 30);
 		f7.set(airport1, airport2, plane6, c.getTime()); fDAO.create(f7);
 		c.set(2012, Calendar.MAY, 5, 9, 30);
 		f8.set(airport2, airport1, plane7, c.getTime()); fDAO.create(f8);
 		c.set(2012, Calendar.JUNE, 5, 9, 30);
 		f9.set(airport1, airport2, plane8, c.getTime()); fDAO.create(f9);
 		c.set(2012, Calendar.JULY, 5, 9, 30);
 		f10.set(airport2, airport1, plane9, c.getTime()); fDAO.create(f10);
 		
 		Reservation r = new Reservation();
 		ReservationDAO rDAO = new ReservationDAO();
 		
 		c.set(2005, Calendar.MARCH, 6, 10, 20); r.set(f1, f2, user, c.getTime()); rDAO.create(r);
 		c.set(2006, Calendar.MARCH, 6, 10, 20); r.set(f2, f1, user2, c.getTime()); rDAO.create(r);
 		c.set(2011, Calendar.MARCH, 6, 10, 20); r.set(f3, f4, user3, c.getTime()); rDAO.create(r);
 		c.set(2012, Calendar.MARCH, 6, 10, 20); r.set(f4, f3, user3, c.getTime()); rDAO.create(r);
 		c.set(2012, Calendar.MARCH, 6, 10, 20); r.set(f6, f5, user4, c.getTime()); rDAO.create(r);
 		c.set(2012, Calendar.MARCH, 6, 10, 20); r.set(f6, f5, user5, c.getTime()); rDAO.create(r);
 		c.set(2012, Calendar.MARCH, 6, 10, 20); r.set(f8, f7, user6, c.getTime()); rDAO.create(r);
 		c.set(2012, Calendar.MARCH, 6, 10, 20); r.set(f8, f7, user7, c.getTime()); rDAO.create(r);
 		c.set(2012, Calendar.MARCH, 6, 10, 20); r.set(f10, f9, user8, c.getTime()); rDAO.create(r);
 		c.set(2012, Calendar.MARCH, 6, 10, 20); r.set(f9, f10, user9, c.getTime()); rDAO.create(r);
 		c.set(2012, Calendar.MARCH, 6, 10, 20); r.set(f10, f9, user10, c.getTime()); rDAO.create(r);
 	}
 	
 	public void fill_basics() {
 		User user = new User();
 		UserDAO userDAO = new UserDAO();
 		
 		user.set("Nom d'user", "Prénom d'user", "user", "user", User.USER);
 		userDAO.create(user);
 		user.set("Nom d'admin", "Prénom d'admin", "admin", "admin", User.ADMIN);
 		userDAO.create(user);
 		
 		AirportDAO airportDAO = new AirportDAO();
 		Airport airport1 = new Airport();
 		Airport airport2 = new Airport();
 		
 		airport1.setName("Paris Charles de Gaulle (CDG)");
 		airportDAO.create(airport1);
 		airport2.setName("Newark Liberty International (EWR)");
 		airportDAO.create(airport2);
 	}
 }
