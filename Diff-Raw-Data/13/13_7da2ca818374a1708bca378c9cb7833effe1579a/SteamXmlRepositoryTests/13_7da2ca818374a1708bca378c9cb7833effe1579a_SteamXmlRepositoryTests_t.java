 package repository;
 
 import static org.junit.Assert.*;
 
 import model.*;
 
 import org.junit.Test;
 
 public class SteamXmlRepositoryTests {
 
 
 	
 	@Test
	public void testValidUri()
 	{
 		String uri = "http://www.steamcommunity.com/id/raveturned?xml=1";
 		SteamXmlRepository repo = new SteamXmlRepository();
 		SteamProfile profile = repo.resolveSteamProfile(uri);
 		
 		assertNotNull("Profile should not be null", profile);
 		assertEquals("Id should be value specified at creation", 76561197970485997l, profile.getId());
 		assertTrue("Name should be value specified at creation", "raveturned".equalsIgnoreCase(profile.getName()));
 
 	}
	
	@Test
	public void testInvalidUri()
	{
		String uri = "hrrp://www.steamcommunity.com/id/raveturned?xml=1";
		SteamXmlRepository repo = new SteamXmlRepository();
		SteamProfile profile = repo.resolveSteamProfile(uri);
		
		assertNull("Profile should be null", profile);
	}	
 }
