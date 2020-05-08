 package com.mymed.tests.unit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * Test class for the {@link AuthenticationManager}
  * 
  * @author Milo Casagrande
  * 
  */
 public class AuthenticationManagerTest extends GeneralTest {
 
 	/**
 	 * Create a simple Authentication entry in the database
 	 */
 	@Test
 	public void createAuthTest() {
 		try {
			profileManager.create(userBean);
 			authenticationManager.create(userBean, authenticationBean);
 		} catch (final Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 
 	/**
 	 * Read the just created authentication entry from the database, and compare
 	 * the {@link MUserBean} returned
 	 */
 	@Test
 	public void readAuthTest() {
 		try {
 			final MUserBean readValue = authenticationManager.read(authenticationBean.getLogin(),
 			        authenticationBean.getPassword());
 			assertEquals("The user beans are not the same\n", readValue, userBean);
 		} catch (final Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 }
