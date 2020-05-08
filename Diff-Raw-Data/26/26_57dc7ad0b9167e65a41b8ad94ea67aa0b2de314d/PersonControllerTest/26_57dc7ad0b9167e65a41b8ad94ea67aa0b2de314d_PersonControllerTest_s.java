 package de.htwg.seapal.person.controllers.mock;
 
 import static org.junit.Assert.*;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import de.htwg.seapal.person.app.PersonDemoMockModule;
 import de.htwg.seapal.person.controllers.IPersonController;
 
 public class PersonControllerTest {
 
 	private IPersonController personController;
 	// Set up Google Guice Dependency Injector
 	Injector injector = Guice.createInjector(new PersonDemoMockModule());
 
 	
 	@Before
 	public void setup(){
 		// Build up the application, resolving dependencies automatically by Guice
 		personController = injector.getInstance(IPersonController.class);
 	}
 
 	@Test
 	public void testGetPersonFirstname() {
 		personController.setPersonFirstname("Max");
 		assertEquals("Max", personController.getPersonFirstname());
 	}
 	
 	@Test
 	public void testGetPersonLastname() {
 		personController.setPersonLastname("Mustermann");
 		assertEquals("Mustermann", personController.getPersonLastname());
 	}
 	
 	@Test
	public void testGetPersonBirth() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date date = null;
		try {
			date = dateFormat.parse("00.00.0000");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		personController.setPersonBirth(date);
		assertEquals(date, personController.getPersonBirth());
	}
	
	@Test
	public void testGetPersonRegistration() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date date = null;
		try {
			date = dateFormat.parse("00.00.0000");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		personController.setPersonRegistration(date);
		assertEquals(date, personController.getPersonRegistration());
	}
	
	@Test
 	public void testGetPersonAge() {
 		personController.setPersonAge(50);
 		assertEquals(50, personController.getPersonAge());
 	}
 	
 	@Test
 	public void testGetPersonNationality() {
 		personController.setPersonNationality("German");
 		assertEquals("German", personController.getPersonNationality());
 	}
 	
 	@Test
 	public void testGetPersonEmail() {
 		personController.setPersonEmail("max.musterman@domain.de");
 		assertEquals("max.musterman@domain.de", personController.getPersonEmail());
 	}
 	
 	@Test
 	public void testGetPersonTelephone() {
 		personController.setPersonTelephone("0123/4567890");
 		assertEquals("0123/4567890", personController.getPersonTelephone());
 	}
 	
 	@Test
 	public void testGetPersonMobile() {
 		personController.setPersonMobile("0987/6543210");
 		assertEquals("0987/6543210", personController.getPersonMobile());
 	}
 	
 	@Test
 	public void testGetPersonStreet() {
 		personController.setPersonStreet("Musterstraße 1");
 		assertEquals("Musterstraße 1", personController.getPersonStreet());
 	}
 	
 	@Test
 	public void testGetPersonPostcode() {
 		personController.setPersonPostcode(12345);
 		assertEquals(12345, personController.getPersonPostcode());
 	}
 	
 	@Test
 	public void testGetPersonCity() {
 		personController.setPersonCity("Musterstadt");
 		assertEquals("Musterstadt", personController.getPersonCity());
 	}
 	
 	@Test
 	public void testGetPersonCountry() {
 		personController.setPersonCountry("Germany");
 		assertEquals("Germany", personController.getPersonCountry());
 	}
 
 }
