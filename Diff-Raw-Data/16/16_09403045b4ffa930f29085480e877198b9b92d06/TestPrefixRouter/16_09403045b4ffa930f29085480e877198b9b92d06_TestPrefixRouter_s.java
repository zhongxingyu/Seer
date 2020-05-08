 package net.cheney.calendar.router.prefix;
 
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 
 import java.util.Arrays;
 import java.util.List;
 
import junit.framework.Assert;

 import net.cheney.motown.uri.Path;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class TestPrefixRouter {
 	
	private static PrefixRoutable<Object> PRINCIPALS_USERS = new PrefixRoutable<Object>(Path.fromString("/principals/users"));
	private static PrefixRoutable<Object> PRINCIPALS_GROUPS = new PrefixRoutable<Object>(Path.fromString("/principals/groups"));
	private static PrefixRoutable<Object> PRINCIPALS = new PrefixRoutable<Object>(Path.fromString("/principals"));
	private static PrefixRoutable<Object> CALENDARS = new PrefixRoutable<Object>(Path.fromString("/calendars"));
	private static PrefixRoutable<Object> GROUPS = new PrefixRoutable<Object>(Path.fromString("/groups"));
  	
 	private static List<PrefixRoutable<Object>> routables;
 
 	@BeforeClass public static void init() {
 		routables = Arrays.<PrefixRoutable<Object>>asList(
 				PRINCIPALS_USERS,
 				PRINCIPALS_GROUPS,
 				CALENDARS,
 				GROUPS
 		);
 	}
 
 	private PrefixRouter<Object> router;
 	
 	@Before public void setup() {
 		this.router = new PrefixRouter<Object>(routables);
 	}
 	
 	@Test 
 	public void testPrincipals() {
		assertEquals(asList(PRINCIPALS_USERS), router.bestMatchForPrefix(Path.fromString("/principals/users/dave")));
 		assertFalse(asList(PRINCIPALS_GROUPS).equals(router.bestMatchForPrefix(Path.fromString("/principals/users/dave"))));
 		assertFalse(asList(PRINCIPALS).equals(router.bestMatchForPrefix(Path.fromString("/principals/users/dave"))));
 	}
 	
 }
