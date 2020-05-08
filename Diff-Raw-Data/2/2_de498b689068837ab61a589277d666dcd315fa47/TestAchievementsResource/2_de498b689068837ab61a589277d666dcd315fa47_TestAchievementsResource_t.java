 package de.kile.zapfmaster2000.rest.api.achievments;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.kile.zapfmaster2000.rest.AbstractMockingTest;
 import de.kile.zapfmaster2000.rest.api.achievements.AchievementResonse;
 import de.kile.zapfmaster2000.rest.api.achievements.AchievementResonse.UserThatGained;
 import de.kile.zapfmaster2000.rest.api.achievements.AchievementsResource;
 import de.kile.zapfmaster2000.rest.core.auth.AuthService;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Achievement;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Box;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Sex;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.UserType;
 
 @SuppressWarnings("unused")
 public class TestAchievementsResource extends AbstractMockingTest {
 
 	private Account account1;
 	private Box box1;
 	private User user1;
 	private User user2;
 	private User user3;
 	private Achievement achievement1;
 	private Achievement achievement2;
 	private Achievement achievement3;
 
 	@Before
 	public void setupData() {
 		// truncate at first! This way, we get rid of any achievements that are
 		// being created on start up
 		truncate();
 
 		account1 = createAccount("foo-account");
 		Account account2 = createAccount("bar-account");
 		box1 = createBox("box-pp", "somewhere", "1.0", account1);
 
 		user1 = createUser("Torsten", "img/user1", "user1-pw", 101, Sex.MALE,
 				85, UserType.USER, account1);
 		user2 = createUser("Bettina", "img/user2", "user2-pw", 202, Sex.FEMALE,
 				85, UserType.USER, account1);
 		user3 = createUser("Jutta", "img/user3", "user3-pw", 303, Sex.FEMALE,
 				85, UserType.USER, account1);
 		// user4 in account2
 		User user4 = createUser("Detlef", "img/user3", "user3-pw", 303,
 				Sex.MALE, 85, UserType.USER, account2);
 
 		// mixed up order to test ordering
 		achievement3 = createAchievement("achievement-3", "desc3", "img/ach3");
 		achievement1 = createAchievement("achievement-1", "desc1", "img/ach1");
 		achievement2 = createAchievement("achievement-2", "desc2", "img/ach2");
 
 		createGainedAchievement(createDate(2012, 1, 1), user1, achievement1);
 		createGainedAchievement(createDate(2012, 2, 1), user1, achievement2);
 		createGainedAchievement(createDate(2012, 3, 1), user2, achievement1);
 		createGainedAchievement(createDate(2012, 4, 1), user4, achievement1);
 
 		AuthService authService = mock(AuthService.class);
 		when(authService.retrieveAccount(anyString())).thenReturn(account1);
 		mockAuthService(authService);
 	}
 
 	@Test
 	public void testSimple() {
 		AchievementsResource resource = new AchievementsResource();
 		Response response = resource.retrieveAchievements(null);
 		assertEquals(response.getStatus(), Status.OK.getStatusCode());
 
 		@SuppressWarnings("unchecked")
 		List<AchievementResonse> achievements = (List<AchievementResonse>) response
 				.getEntity();
 		assertEquals(3, achievements.size());
 
 		AchievementResonse a1 = achievements.get(0);
 		assertConforms(achievement1, a1);
 		assertEquals(2, a1.getUsers().size()); // user 4 is in account 2 and
 												// thus ignored
 		assertConforms(user2, a1.getUsers().get(0)); // order by gain date, DESC
 		assertConforms(user1, a1.getUsers().get(1));
 
 		AchievementResonse a2 = achievements.get(1);
 		assertConforms(achievement2, a2);
 		assertEquals(1, a2.getUsers().size());
 		assertConforms(user1, a2.getUsers().get(0));
 
 		AchievementResonse a3 = achievements.get(2);
 		assertConforms(achievement3, a3);
 		assertEquals(0, a3.getUsers().size());
 	}
 
 	private void assertConforms(Achievement pAchievement,
 			AchievementResonse pResponse) {
		assertEquals(pAchievement.getId(), pResponse.getAchievementId());
 		assertEquals(pAchievement.getName(), pResponse.getAchievementName());
 		assertEquals(pAchievement.getDescription(),
 				pResponse.getAchievementDescription());
 		assertEquals(pAchievement.getImagePath(),
 				pResponse.getAchievementImage());
 	}
 
 	private void assertConforms(User pUser, UserThatGained pUserGained) {
 		assertEquals(pUser.getId(), pUserGained.getUserId());
 		assertEquals(pUser.getName(), pUserGained.getUserName());
 		assertEquals(pUser.getImagePath(), pUserGained.getUserImage());
 	}
 }
