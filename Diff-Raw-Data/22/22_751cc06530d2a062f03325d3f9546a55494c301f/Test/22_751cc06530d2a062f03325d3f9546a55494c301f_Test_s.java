 package is.idega.idegaweb.pheidippides.webservice.hlaupastyrkurISB.test;
 
 import is.idega.idegaweb.pheidippides.webservice.hlaupastyrkurISB.client.ISBService;
 import is.idega.idegaweb.pheidippides.webservice.hlaupastyrkurISB.client.ISBServiceServiceLocator;
 import is.idega.idegaweb.pheidippides.webservice.hlaupastyrkurISB.client.Login;
 import is.idega.idegaweb.pheidippides.webservice.hlaupastyrkurISB.client.RunnerInfo;
 import is.idega.idegaweb.pheidippides.webservice.hlaupastyrkurISB.client.Session;
 
 import java.net.URL;
 
 public class Test {
 	public static void main(String[] args) {
 		String loginName = args[0];
 		String password = args[1];
 		try {
 			System.out.println("loginName = " + loginName);
 			System.out.println("password = " + password);
 						
 			ISBServiceServiceLocator locator = new ISBServiceServiceLocator();
 			ISBService port = locator.getISBService(new URL("http://skraning.marathon.is/services/ISBService"));
 			
 			Login login = new Login(loginName, password);
 			
 			Session session = port.authenticateUser(login);
 			
 			if (session == null) {
 				System.out.println("session is null");
 			} else {
 				System.out.println("session = " + session.getSessionID());
 				
				RunnerInfo in1 = new RunnerInfo(null, "10km", "palli@idega.is", null, "8671374", null, "0610703899", "8671374", "palli", session, "M", "Male");
 				String reg = port.registerRunner(in1 );
 				
 				System.out.println("registered = "+ reg);
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
