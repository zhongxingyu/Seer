 package test.unit;
 
 import static org.junit.Assert.*;
 import org.junit.*;
 import java.util.Date;
 import java.util.Calendar;

 /**
  * Unit test for {@link app.Session}.
  */
 public class Session {
	app.Session s;
 	Date futureDate;

 	@Before
 	public void setup() {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(new Date());
 		cal.add(Calendar.HOUR_OF_DAY, 1);
 		futureDate = cal.getTime();
 	}
 
 	@After
 	public void teardown() {
		s = null;
 	}
 
 	@Test
	public void checkExpiration() {
		s = new app.Session(new app.User(new app.Email[] { new app.Email("fart@traf") }), futureDate);
 		assertTrue(s.isValid());
 	}
 
 }
