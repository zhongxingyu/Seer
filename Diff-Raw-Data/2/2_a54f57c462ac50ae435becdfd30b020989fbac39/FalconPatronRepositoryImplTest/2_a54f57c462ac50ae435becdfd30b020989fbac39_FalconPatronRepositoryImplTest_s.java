 package com.twistlet.falcon.model.repository;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.apache.commons.lang3.time.DateUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.twistlet.falcon.model.entity.FalconAppointment;
 import com.twistlet.falcon.model.entity.FalconAppointmentPatron;
 import com.twistlet.falcon.model.entity.FalconLocation;
 import com.twistlet.falcon.model.entity.FalconPatron;
 import com.twistlet.falcon.model.entity.FalconRole;
 import com.twistlet.falcon.model.entity.FalconService;
 import com.twistlet.falcon.model.entity.FalconStaff;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.entity.FalconUserRole;
 
 public class FalconPatronRepositoryImplTest extends AbstractFalconRepositoryTest {
 
 	@Autowired
 	FalconPatronRepository falconPatronRepository;
 
 	@PersistenceContext
 	EntityManager entityManager;
 
 	@Before
 	public void init() {
 		FalconRole role1, role3;
 		FalconUser user1, user2, user3, admin1;
 		FalconPatron patron1, patron2, patron3;
 		FalconLocation location1;
 		FalconService service1;
 		FalconStaff staff1;
 		FalconAppointment appointment1, appointment2;
 		entityManager.persist(role1 = createNewRole("ROLE_LEVEL_1"));
 		entityManager.persist(createNewRole("ROLE_LEVEL_2"));
 		entityManager.persist(role3 = createNewRole("ROLE_LEVEL_3"));
 		entityManager.persist(user1 = createNewUser("USER_1", "email1@add.com", "1", "1"));
 		entityManager.persist(user2 = createNewUser("USER_2", "email2@add.com", "2", "2"));
 		entityManager.persist(user3 = createNewUser("USER_3", "email3@add.com", "3", "3"));
 		entityManager.persist(admin1 = createNewUser("ADMIN_1", "emailadmin1@add.com", "4", "4"));
 		entityManager.persist(new FalconUserRole(user1, role1));
 		entityManager.persist(new FalconUserRole(user2, role1));
 		entityManager.persist(new FalconUserRole(user3, role1));
 		entityManager.persist(new FalconUserRole(user2, role3));
 		entityManager.persist(patron1 = createNewPatron(user1, admin1));
 		entityManager.persist(patron2 = createNewPatron(user2, admin1));
 		entityManager.persist(patron3 = createNewPatron(user3, admin1));
 		entityManager.persist(location1 = createNewLocation("LOCATION_1", admin1));
 		entityManager.persist(service1 = createNewService("SERVICE_1", admin1));
 		entityManager.persist(staff1 = createNewStaff("STAFF_1", admin1));
 		final Date now = new Date();
 		entityManager.persist(appointment1 = createNewAppointment(now, DateUtils.addHours(now, 4), staff1, service1, location1));
 		entityManager.persist(appointment2 = createNewAppointment(DateUtils.addHours(now, 3), DateUtils.addHours(now, 5), staff1,
 				service1, location1));
 		entityManager.persist(createNewFalconAppointmentPatron(patron1, appointment1));
 		// entityManager.persist(createNewFalconAppointmentPatron(patron2,
 		// appointment1));
 		entityManager.persist(createNewFalconAppointmentPatron(patron3, appointment1));
 		// entityManager.persist(createNewFalconAppointmentPatron(patron1,
 		// appointment2));
 		entityManager.persist(createNewFalconAppointmentPatron(patron2, appointment2));
 		// entityManager.persist(createNewFalconAppointmentPatron(patron3,
 		// appointment2));
 		entityManager.flush();
 		entityManager.clear();
 	}
 
 	private FalconUser createNewUser(final String username, final String email, final String nric, final String phone) {
 		final FalconUser falconUser = new FalconUser();
 		falconUser.setUsername(username);
 		falconUser.setPassword("x");
 		falconUser.setName(username);
 		falconUser.setEmail(email);
 		falconUser.setNric(nric);
 		falconUser.setPhone(phone);
 		falconUser.setValid(true);
 		return falconUser;
 	}
 
 	private FalconRole createNewRole(final String rolename) {
 		final FalconRole falconRole = new FalconRole();
 		falconRole.setRoleName(rolename);
 		return falconRole;
 	}
 
 	private FalconPatron createNewPatron(final FalconUser patron, final FalconUser admin) {
 		final FalconPatron falconPatron = new FalconPatron();
 		falconPatron.setFalconUserByPatron(patron);
 		falconPatron.setFalconUserByAdmin(admin);
 		return falconPatron;
 	}
 
 	private FalconLocation createNewLocation(final String location, final FalconUser admin) {
 		final FalconLocation falconLocation = new FalconLocation();
 		falconLocation.setName(location);
 		falconLocation.setFalconUser(admin);
 		return falconLocation;
 	}
 
 	private FalconService createNewService(final String service, final FalconUser admin) {
 		final FalconService falconService = new FalconService();
 		falconService.setName(service);
 		falconService.setFalconUser(admin);
 		return falconService;
 	}
 
 	private FalconStaff createNewStaff(final String name, final FalconUser admin) {
 		final FalconStaff falconStaff = new FalconStaff();
 		falconStaff.setName(name);
 		falconStaff.setFalconUser(admin);
 		falconStaff.setNric("XXX");
 		return falconStaff;
 	}
 
 	private FalconAppointmentPatron createNewFalconAppointmentPatron(final FalconPatron patron, final FalconAppointment appointment) {
 		final FalconAppointmentPatron appointmentPatron = new FalconAppointmentPatron();
 		appointmentPatron.setFalconPatron(patron);
 		appointmentPatron.setFalconAppointment(appointment);
 		return appointmentPatron;
 	}
 
 	private FalconAppointment createNewAppointment(final Date start, final Date end, final FalconStaff staff,
 			final FalconService service, final FalconLocation location) {
 		final FalconAppointment appointment = new FalconAppointment();
 		appointment.setAppointmentDate(start);
 		appointment.setAppointmentDateEnd(end);
 		appointment.setFalconLocation(location);
 		appointment.setFalconService(service);
 		appointment.setFalconStaff(staff);
 		appointment.setNotified('N');
 		return appointment;
 
 	}
 
 	@Test
 	public void testPatronsOverlapStart() {
		final Date end = new Date();
 		final Date start = DateUtils.addHours(end, -1);
 		final FalconUser admin = createNewUser("ADMIN_1", "emailadmin1@add.com", "4", "4");
 		final Set<FalconPatron> patrons = falconPatronRepository.findPatronsDateRange(admin, start, end);
 		assertEquals(2, patrons.size());
 	}
 
 	@Test
 	public void testPatronsOverlapEnd() {
 		final Date start = DateUtils.addHours(new Date(), 3);
 		final Date end = DateUtils.addHours(new Date(), 6);
 		final FalconUser admin = createNewUser("ADMIN_1", "emailadmin1@add.com", "4", "4");
 		final Set<FalconPatron> patrons = falconPatronRepository.findPatronsDateRange(admin, start, end);
 		assertEquals(3, patrons.size());
 	}
 
 	@Test
 	public void testPatronsOverlapMiddle() {
 		final Date start = DateUtils.addHours(new Date(), 1);
 		final Date end = DateUtils.addHours(new Date(), 2);
 		final FalconUser admin = createNewUser("ADMIN_1", "emailadmin1@add.com", "4", "4");
 		final Set<FalconPatron> patrons = falconPatronRepository.findPatronsDateRange(admin, start, end);
 		assertEquals(2, patrons.size());
 	}
 
 	@Test
 	public void testPatronsNoOverlap() {
 		final Date start = DateUtils.addHours(new Date(), -4);
 		final Date end = DateUtils.addHours(new Date(), -2);
 		final FalconUser admin = createNewUser("ADMIN_1", "emailadmin1@add.com", "4", "4");
 		final Set<FalconPatron> patrons = falconPatronRepository.findPatronsDateRange(admin, start, end);
 		assertEquals(0, patrons.size());
 	}
 
 	@Test
 	public void testFindByFalconUserNameLike() {
 		final FalconUser admin = createNewUser("ADMIN_1", "emailadmin1@add.com", "4", "4");
 		final String name = "USER_1";
 		final List<FalconPatron> patrons = falconPatronRepository.findByFalconUserNameLike(admin, name);
 		assertEquals(1, patrons.size());
 	}
 
 	@Test
 	public void testBooleanNull() {
 		final FalconUser falconUser = new FalconUser();
 		if (falconUser.getValid() == null) {
 			System.out.print("ok");
 		}
 	}
 }
