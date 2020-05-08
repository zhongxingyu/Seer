 package feri.rvir.elocator;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.*;
 
 import com.google.appengine.api.datastore.Key;
 
 import feri.rvir.elocator.dao.LocationDao;
 import feri.rvir.elocator.dao.TrackingDao;
 import feri.rvir.elocator.dao.UserDao;
 import feri.rvir.elocator.rest.resource.location.Location;
 import feri.rvir.elocator.rest.resource.tracking.Tracking;
 import feri.rvir.elocator.rest.resource.user.User;
 import feri.rvir.elocator.util.Crypto;
 
 @SuppressWarnings("serial")
 public class ElocatorWebServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		UserDao udao=new UserDao();
 		User u = new User("pwnt","pass");
 		//udao.addUser(u);
 		List<User> users = udao.getAll();
 		System.out.println("users " + users.size());
 		
 		Tracking t = new Tracking(users.get(3).getKey(),users.get(5).getKey());
 		TrackingDao tdao = new TrackingDao();
 		//tdao.addTracking(t);
 		
 		Calendar cal = Calendar.getInstance();
 		Date now = cal.getTime();
 		
 		Location l = new Location(users.get(5).getKey(),now, 46.027482,14.47998);
 		LocationDao ldao = new LocationDao();
 		//ldao.addLocation(l);
 		
		/*
		UserDao udao=new UserDao();
		LocationDao ldao=new LocationDao();
		TrackingDao tdao=new TrackingDao();
		System.out.println(udao.getAll().size());
		System.out.println(ldao.getAll().size());
		System.out.println(tdao.getAllTrackings().size());
		
		System.out.println(udao.getAll().get(0).getUsername());
		System.out.println(udao.getAll().get(0).getPassword());
		System.out.println(Crypto.hash("test", "SHA-1"));
 	}
 }
