 package cours.ulaval.glo4003.persistence;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import cours.ulaval.glo4003.controller.model.AvailabilityModel;
 import cours.ulaval.glo4003.domain.Availability;
 
 public class XMLAvailabilityRepositoryIT {
 
 	private static final String UN_IDUL = "un_idul";
 	private XMLAvailabilityRepository repository;
 
 	@Before
 	public void setUp()
 			throws Exception {
 		repository = new XMLAvailabilityRepository();
 	}
 
 	@Test
 	public void canSaveAvailabilities()
 			throws Exception {
 		Availability availability = createAvailabilities();
 
 		repository.store(availability);
 
 	}
 
 	@Test
 	public void canLoadAnAvailability() {
 		Availability availability = createAvailabilities();
 
		assertEquals(availability.getAvailabilities().get(0).getStartTime().get(Calendar.DAY_OF_WEEK),
				repository.findByIdul(UN_IDUL).getAvailabilities().get(0).getStartTime().get(Calendar.DAY_OF_WEEK));
		assertEquals(availability.getAvailabilities().get(0).getStartTime().get(Calendar.HOUR), repository.findByIdul(UN_IDUL)
				.getAvailabilities().get(0).getStartTime().get(Calendar.HOUR));
		assertEquals(availability.getAvailabilities().get(0).getDuration(), repository.findByIdul(UN_IDUL).getAvailabilities()
 				.get(0).getDuration());
 	}
 
 	private Availability createAvailabilities() {
 		List<Boolean> disponibility = new ArrayList<Boolean>();
 
 		disponibility.add(true);
 		disponibility.add(true);
 		disponibility.add(true);
 		disponibility.add(true);
 		disponibility.add(true);
 		disponibility.add(true);
 		disponibility.add(false);
 		disponibility.add(false);
 		disponibility.add(false);
 		disponibility.add(false);
 		disponibility.add(true);
 		disponibility.add(true);
 		disponibility.add(true);
 
 		AvailabilityModel model = new AvailabilityModel();
 		model.setMonday(disponibility);
 		model.setTuesday(disponibility);
 		model.setWednesday(disponibility);
 		model.setThursday(disponibility);
 		model.setFriday(disponibility);
 
 		Availability availability = new Availability(model, UN_IDUL);
 
 		return availability;
 	}
 }
