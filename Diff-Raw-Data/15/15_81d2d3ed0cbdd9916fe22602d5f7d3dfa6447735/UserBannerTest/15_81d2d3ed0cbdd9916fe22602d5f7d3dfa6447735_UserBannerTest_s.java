 package org.jtalks.poulpe.model.logic;
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.dao.GroupDao;
 import org.jtalks.poulpe.model.dao.UserDao;
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertTrue;
 
 /**
  * @author stanislav bashkirtsev
  * @author alexandr afanasev
  * @author maxim reshetov
  */
 public class UserBannerTest {
 	GroupDao groupDao;
 	UserDao userDao;
 	UserBanner sut;
 
 	@BeforeMethod
 	public void setUp() throws Exception {
 		groupDao = mock(GroupDao.class);
 		userDao = mock(UserDao.class);
 		sut = new UserBanner(groupDao, userDao);
 	}
 
 	@Test
 	public void testGetBannedUsersGroup_withoutGroups() throws Exception {
 		doReturn(new ArrayList<Group>()).when(groupDao).getByNameContains("Banned Users");
 		List<Group> bannedUsersGroupsActual = sut.getBannedUsersGroups();
 		assertEquals(bannedUsersGroupsActual.get(0).getName(), "Banned Users");
 	}
 
 	@Test
 	public void testGetBannedUsersGroup_withoutExistGroups() throws Exception {
 		doReturn(Arrays.asList(new Group("Banned Users"))).when(groupDao).getByNameContains("Banned Users");
 		List<Group> bannedUsersGroupsActual = sut.getBannedUsersGroups();
 		assertEquals(bannedUsersGroupsActual.get(0).getName(), "Banned Users");
 	}
 
 	@Test(dataProvider = "provideGroupWithUsers")
 	public void testBanUsers(Group bannedUsersGroup) throws Exception {
		doReturn(Arrays.asList(bannedUsersGroup)).when(groupDao).getByNameContains("Banned Users");
 		PoulpeUser bannedUser = new PoulpeUser("c", "c", "c", "c");
 		sut.banUsers(new UserList(Arrays.asList(bannedUser)));
 		assertTrue(bannedUsersGroup.getUsers().contains(bannedUser));
 		verify(groupDao).saveOrUpdate(bannedUsersGroup);
 	}
 
 	@Test(dataProvider = "provideGroupWithUsers")
 	public void testRevokeBan(Group bannedUsersGroup) throws Exception {
		doReturn(Arrays.asList(bannedUsersGroup)).when(groupDao).getByNameContains("Banned Users");
 		PoulpeUser userToRevokeBan = new PoulpeUser("a", "b", "c", "d");
 		sut.revokeBan(new UserList(Arrays.asList(userToRevokeBan)));
 		bannedUsersGroup.getUsers().removeAll(Arrays.asList(userToRevokeBan));
 		assertTrue(!bannedUsersGroup.getUsers().contains(userToRevokeBan));
 		verify(groupDao).saveOrUpdate(bannedUsersGroup);
 	}
 
 	@Test(dataProvider = "provideGroupWithUsers")
 	public void getAllBannedUsers(Group bannedUsersGroup) throws Exception {
 		doReturn(Arrays.asList(bannedUsersGroup)).when(groupDao).getByNameContains("Banned Users");
 		doReturn(bannedUsersGroup.getUsers()).when(userDao).getUsersInGroups(Arrays.asList(bannedUsersGroup));
 		assertEquals(bannedUsersGroup.getUsers(), userDao.getUsersInGroups(Arrays.asList(bannedUsersGroup)));
 	}
 
 	@DataProvider
 	public Object[][] provideGroupWithUsers() {
 		Group bannedUsersGroup = new Group("Banned Users");
 		bannedUsersGroup.getUsers().addAll(Arrays.asList(
 				new PoulpeUser("a", "b", "c", "d"),
 				new PoulpeUser("e", "f", "g", "h"),
 				new PoulpeUser("i", "j", "k", "l")));
 		return new Object[][]{{bannedUsersGroup}};
 	}
 
 }
