 package jp.ikikko.bti.gdata;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.Collection;
 
 import jp.ikikko.bti.BaseBtiTest;
 import jp.ikikko.bti.backlogapi.BacklogApiClient;
 import jp.ikikko.bti.entity.User;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class GdataValidatorTest extends BaseBtiTest {
 
 	private static final int DUMMY_USER_ID = -1;
 
 	private GdataValidator validator;
 
 	private BacklogApiClient client;
 
 	@Before
 	public void setUp() {
 		validator = new GdataValidator();
 
 		client = new BacklogApiClient();
 		client.login(SPACE, USERNAME, PASSWORD);
 	}
 
 	@Test
 	public void setRegisteredData() throws Exception {
 		validator.setRegisteredData(client, PROJECT_KEY);
 		Collection<User> users = validator.getRegisteredUsers();
 
 		assertThat(users.size(), is(not(0)));
 		for (User user : users) {
 			assertThat(user, is(notNullValue()));
 		}
 	}
 
 	@Test
 	public void isRegisteredUserで成功() throws Exception {
 		validator.setRegisteredData(client, PROJECT_KEY);
 		boolean actual = validator.isRegisteredUser(USER_ID);
 
 		assertThat(actual, is(true));
 	}
 
 	@Test
 	public void isRegisteredUserで失敗() throws Exception {
 		validator.setRegisteredData(client, PROJECT_KEY);
 		boolean actual = validator.isRegisteredUser(DUMMY_USER_ID);
 
 		assertThat(actual, is(false));
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void setRegisteredDataしていない状態でチェックしようとすると例外発生() throws Exception {
 		validator.isRegisteredUser(USER_ID);
 	}
 
 }
