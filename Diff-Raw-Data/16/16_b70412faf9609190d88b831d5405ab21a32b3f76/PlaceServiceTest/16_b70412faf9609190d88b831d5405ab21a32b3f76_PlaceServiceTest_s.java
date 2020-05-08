 package de.hswt.hrm.place.service;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.place.model.Place;
 import de.hswt.hrm.test.database.AbstractDatabaseTest;
 
 public class PlaceServiceTest extends AbstractDatabaseTest {
 	
 	private void ComparePlaceFields(final Place expected, final Place actual) {
 		assertEquals("Area not set correctly.", expected.getArea(), actual.getArea());
 		assertEquals("City not set correctly.", expected.getCity(), actual.getCity());
 		assertEquals("Location not set correctly.", expected.getLocation(), actual.getLocation());
 		assertEquals("PlaceName not set correctly.", expected.getPlaceName(), actual.getPlaceName());
 		assertEquals("PostCode not set correctly.", expected.getPostCode(), actual.getPostCode());
 		assertEquals("Street not set correctly.", expected.getStreet(), actual.getStreet());
 		assertEquals("StreetNo not set correctly.", expected.getStreetNo(), actual.getStreetNo());
 	}
 	
 	@Test
 	public void testFindAll() throws DatabaseException {
 		Place p1 = new Place("place Name", "55464", "SomeCity", "Street", "113a", "Location", "Some Area");
 		Place p2 = new Place("Another place", "65461", "Gotham City", "Bat-Avenue", "558", "Bat-Cave", "No idea");
 		PlaceService.insert(p1);
 		PlaceService.insert(p2);
 		
 		Collection<Place> places = PlaceService.findAll();
 		assertEquals("Count of retrieved places does not match.", 2, places.size());
 	}
 	
 	@Test
 	public void testFindById() throws ElementNotFoundException, DatabaseException {
 		Place expected = new Place("place Name", "55464", "SomeCity", "Street", "113a", "Location", "Some Area");
 		Place parsed = PlaceService.insert(expected);
 		
 		Place retrieved = PlaceService.findById(parsed.getId());
 		ComparePlaceFields(expected, retrieved);
 	}
 	
 	@Test
 	public void testInsert() throws ElementNotFoundException, DatabaseException {
 		Place expected = new Place("place Name", "55464", "SomeCity", "Street", "113a", "Location", "Some Area");
 		
 		Place parsed = PlaceService.insert(expected);
 		ComparePlaceFields(expected, parsed);
 		assertTrue("ID not set correctly.", parsed.getId() >= 0);
 		
 		Place retrieved = PlaceService.findById(parsed.getId());
 		ComparePlaceFields(expected, retrieved);
 		assertEquals("Requested object does not equal parsed one.", parsed, retrieved);
 	}
 	
 	public void testUpdate() throws ElementNotFoundException, DatabaseException {
 		Place expected = new Place("place Name", "55464", "SomeCity", "Street", "113a", "Location", "Some Area");
 		Place parsed = PlaceService.insert(expected);
 		
 		Place p2 = new Place("Another place", "65461", "Gotham City", "Bat-Avenue", "558", "Bat-Cave", "No idea");
 		PlaceService.insert(p2);
 		
 		parsed.setCity("A Changed City");
 		parsed.setStreet("Best Street Ever");
 		PlaceService.update(parsed);
 		
 		Place retrieved = PlaceService.findById(parsed.getId());
 		ComparePlaceFields(parsed, retrieved);
 		assertEquals("Requested object does not equal updated one.", parsed, retrieved);
 	}
 }
