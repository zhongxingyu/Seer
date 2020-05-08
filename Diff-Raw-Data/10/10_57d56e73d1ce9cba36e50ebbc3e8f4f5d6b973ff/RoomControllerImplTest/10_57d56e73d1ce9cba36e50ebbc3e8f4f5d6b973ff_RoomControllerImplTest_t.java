 package no.niths.application.rest;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.AccessFieldController;
 import no.niths.application.rest.interfaces.RoomController;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.location.Room;
 import no.niths.domain.signaling.AccessField;
 
 import org.hibernate.NonUniqueObjectException;
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class RoomControllerImplTest {
 
 	@Autowired
 	private RoomController controller;
 
 	@Autowired
 	private AccessFieldController afController;
 
 	private AccessField testAF;
 
 	private Room testRoom01;
 	private Room testRoom02;
 	private Room testRoom03;
 	private Room testRoom04;
 
 	@Before
 	public void setUp() throws Exception {
 		testRoom01 = new Room("81");
 		testRoom02 = new Room("40");
 		testRoom03 = new Room("Kantina");
 		testRoom04 = new Room("41");
 		testAF = new AccessField(22, 50);
 
 		controller.create(testRoom01);
 		controller.create(testRoom02);
 		controller.create(testRoom03);
 		controller.create(testRoom04);
 		afController.create(testAF);
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		controller.hibernateDelete(testRoom01.getId());
 		controller.hibernateDelete(testRoom02.getId());
 		controller.hibernateDelete(testRoom03.getId());
 		controller.hibernateDelete(testRoom04.getId());
 
 		afController.hibernateDelete(testAF.getId());
 	}
 
 	@Test
 	public void testGetById() {
 		Room room = controller.getById(testRoom02.getId());
 		assertEquals(testRoom02, room);
 	}
 
 	@Test(expected = ObjectNotFoundException.class)
 	public void testGetByInvalidId_shouldThrowException() {
 		controller.getById(new Long(-1));
 	}
 
 	@Test
 	public void testGetAllT() {
 		ArrayList<Room> rooms = controller.getAll(new Room());
 		assertEquals(4, rooms.size());
 	}
 
	@Ignore
 	@Test
     public void testGetAllWithSearch() {
         ArrayList<Room> rooms = controller.getAll(new Room("Kantina"));
         assertEquals(1, rooms.size());
     }
 
 	@Test
 	public void testGetAllTIntInt() {
 		ArrayList<Room> rooms = controller.getAll(new Room(), 1, 3);
 		assertEquals(3, rooms.size());
 	}
 
 	@Test
 	public void testUpdate() {
 		Room room = new Room();
 		room.setId(testRoom03.getId());
 		room.setRoomName("new room name");
 
 		controller.update(room);
 		assertEquals(room.getRoomName(), controller.getById(testRoom03.getId())
 				.getRoomName());
 	}
 
 	@Test
 	public void testAddAndRemoveAccessField(){
 		Room room = controller.getById(testRoom04.getId());
 		assertEquals(0, room.getAccessFields().size());
 		
 		// add access point to access field
 		controller.addAccessField(testRoom04.getId(), testAF.getId());
 		room = controller.getById(testRoom04.getId());
 		assertEquals(testAF, room.getAccessFields().get(0));
 		
 		// remove access point from access field
 		controller.removeAccessField(testRoom04.getId(), testAF.getId());
 		
 		// access field should not have access point
 		room = controller.getById(testRoom04.getId());
 		assertEquals(0, room.getAccessFields().size());
 	}
 	
 	@Test(expected=NonUniqueObjectException.class)
 	public void testAddTheSameAccessFieldTwice(){
 		controller.addAccessField(testRoom04.getId(), testAF.getId());
 		controller.addAccessField(testRoom04.getId(), testAF.getId());
 	}
 	
 	@Test(expected=ObjectNotFoundException.class)
 	public void testRemoveANonExistingAccessFieldRelationship(){
 		controller.removeAccessField(testRoom04.getId(), testAF.getId());
 	}
 }
