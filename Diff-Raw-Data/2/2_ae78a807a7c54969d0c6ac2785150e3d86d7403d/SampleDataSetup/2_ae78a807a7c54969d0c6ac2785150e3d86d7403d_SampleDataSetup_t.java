 package dk.softwarehuset.projectmanagement.util;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.junit.Before;
 
 import dk.softwarehuset.projectmanagement.app.Application;
 import dk.softwarehuset.projectmanagement.app.Employee;
 import dk.softwarehuset.projectmanagement.app.NonUniqueIdentifierException;
 import dk.softwarehuset.projectmanagement.app.PermissionDeniedException;
 import dk.softwarehuset.projectmanagement.app.WrongCredentialsException;
 import dk.softwarehuset.projectmanagement.app.DateServer;
 
 public class SampleDataSetup {
 	protected Application app = new Application();
 	
 	@Before
 	public void setup() throws WrongCredentialsException, PermissionDeniedException, NonUniqueIdentifierException {
 		// Set date
 		DateServer dateServer = mock(DateServer.class);
 		app.setDateServer(dateServer);
 		Calendar date = new GregorianCalendar(2012, Calendar.MARCH, 26);
 		when(dateServer.getDate()).thenReturn(date);
 		
 		// Sign in as administrator
		app.SignIn("ZZZZ");
 		
 		// Add employees
 		Employee employee1 = new Employee("ABCD", "Alpha Bravo Charlie Delta");
 		Employee employee2 = new Employee("EFGH", "Echo Foxtrot Golf Hotel");
 		Employee employee3 = new Employee("IJKL", "India Juliet Kilo Lima");
 		app.addEmployee(employee1);
 		app.addEmployee(employee2);
 		app.addEmployee(employee3);
 		
 		// Sign out
 		app.SignOut();
 	}
 }
