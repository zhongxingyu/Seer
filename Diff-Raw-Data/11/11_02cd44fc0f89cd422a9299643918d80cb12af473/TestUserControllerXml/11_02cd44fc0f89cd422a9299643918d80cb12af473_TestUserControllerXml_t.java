 package ch.hsr.waktu.controller.datacontroller;
 

import static org.junit.Assert.assertEquals;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.hsr.waktu.TestSuite;
 import ch.hsr.waktu.domain.SystemRole;
 import ch.hsr.waktu.domain.Usr;
 import ch.hsr.waktu.services.WaktuException;
 
 public class TestUserControllerXml extends TestSuite {
 
 	private LinkedList<Usr> allUsersReferenceList = new LinkedList<Usr>();
 	private LinkedList<Usr> inactiveUsersReferenceList = new LinkedList<Usr>();
 	private LinkedList<Usr> activeUsersReferenceList = new LinkedList<Usr>();
 	private LinkedList<Usr> projectManagersReferenceList = new LinkedList<Usr>();
 
 	
 	@Before
 	public void initializeReferenceUserLists() {
 		Usr u1 = new Usr("patriziaheer","Patrizia","Heer","81dc9bdb52d04dc20036dbd8313ed055", 100, SystemRole.ADMIN, 12);
 		allUsersReferenceList.add(u1);
 		Usr u2 = new Usr("simonstaeheli","Simon","Stäheli","81dc9bdb52d04dc20036dbd8313ed055", 100, SystemRole.ADMIN, 12);
 		allUsersReferenceList.add(u2);
 		Usr u3 = new Usr("mikefisler","Mike","Fisler","81dc9bdb52d04dc20036dbd8313ed055", 100, SystemRole.ADMIN, 12);
 		allUsersReferenceList.add(u3);
 		Usr u4 = new Usr("osamabinladen","Osama","Bin Laden","81dc9bdb52d04dc20036dbd8313ed055", 100, SystemRole.EMPLOYEE, 12);
 		u4.setActiveState(false);
 		allUsersReferenceList.add(u4);
 		Usr u5 = new Usr("mickeymouse","Mickey","Mouse","81dc9bdb52d04dc20036dbd8313ed055", 20, SystemRole.PROJECTMANAGER, 12);
 		u5.setActiveState(false);
 		allUsersReferenceList.add(u5);
 		Usr u6 = new Usr("chucknorris","Chuck","Norris", "e48e13207341b6bffb7fb1622282247b", 
 				1000, SystemRole.ADMIN, 0);
 		allUsersReferenceList.add(u6);
 		Usr u7 = new Usr("hansmuster","Hans","Muster","81dc9bdb52d04dc20036dbd8313ed055", 80, SystemRole.EMPLOYEE, 10);
 		allUsersReferenceList.add(u7);
 		Usr u8 = new Usr("hansmuster1","Hans","Muster","81dc9bdb52d04dc20036dbd8313ed055", 80, SystemRole.EMPLOYEE, 10);
 		allUsersReferenceList.add(u8);
 		Usr u9 = new Usr("hansmuster2","Hans","Muster","81dc9bdb52d04dc20036dbd8313ed055", 80, SystemRole.EMPLOYEE, 10);
 		allUsersReferenceList.add(u9);
 		Usr u10 = new Usr("obiwankenobi","Obi Wan","Kenobi","81dc9bdb52d04dc20036dbd8313ed055", 30, SystemRole.PROJECTMANAGER, 10);
 		allUsersReferenceList.add(u10);
 		
 		for(Usr u: allUsersReferenceList) {
 			if(u.isActive()) {
 				activeUsersReferenceList.add(u);
 			}
 		}
 		
 		for(Usr u: allUsersReferenceList) {
 			if(!u.isActive()) {
 				inactiveUsersReferenceList.add(u);
 			}
 		}
 		
 		for(Usr u: allUsersReferenceList) {
 			if(u.getSystemRole().equals(SystemRole.PROJECTMANAGER)) {
 				projectManagersReferenceList.add(u);
 			}
 		}
 	}
 	
 	@After
 	public void clearReferenceUserLists() {
 		for(@SuppressWarnings("unused") Usr u: allUsersReferenceList) { 
 			u = null;
 		}
 		allUsersReferenceList.clear();
 		activeUsersReferenceList.clear();
 		inactiveUsersReferenceList.clear();
 		projectManagersReferenceList.clear();
 	}
 
 	
 	@Test
 	public void getAllUsers() throws WaktuException {
 		List<Usr> userList = UserControllerXml.getInstance().getAllUsers();
 		
 		assertUserListEquality(allUsersReferenceList, userList);
 	}
 	
 	@Test
 	public void getActiveUsers() throws WaktuException {
 		List<Usr> userList = UserControllerXml.getInstance().getActiveUsers();
 		
 		assertUserListEquality(activeUsersReferenceList, userList);
 	}
 	
 	@Test
 	public void getInactiveUsers() throws WaktuException {
 		List<Usr> userList = UserControllerXml.getInstance().getInactiveUsers();
 		
 		assertUserListEquality(inactiveUsersReferenceList, userList);
 	}
 	
 	@Test
 	public void getProjectManagers() throws WaktuException {
 		List<Usr> userList = UserControllerXml.getInstance().getProjectManagers();
 		
 		assertUserListEquality(projectManagersReferenceList, userList);
 	}
 	
 	@Test
 	public void getUser_ValidUser() throws WaktuException {
 		Usr user = UserControllerXml.getInstance().getUser("chucknorris");
 		assertEquals(allUsersReferenceList.get(5), user);
 	}
 	
 	@Test
 	public void getUser_InvalidUser_throwsWaktuGeneralException() throws WaktuException {
 		Usr user = UserControllerXml.getInstance().getUser("raeuberhotzenplotz");
 		assertEquals(null, user);
 	}
 
 	private void assertUserListEquality(LinkedList<Usr> userReferenceList, List<Usr> userList) {
 		for(int i=0; i<userList.size(); i++) {
 			assertEquals(userReferenceList.get(i), userList.get(i));
 		}
 		assertEquals(userReferenceList.size(), userList.size());
 	}
 }
