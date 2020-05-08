 package com.mymed.tests.stress;
 
 import java.io.File;
 
import com.mymed.controller.core.exception.IOBackEndException;
import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.core.configuration.WrapperConfiguration;
 import com.mymed.model.data.AbstractMBean;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.user.MUserBean;
 
 /**
  * Create all the necessary authentication beans in order to set up the
  * Cassandra stress test
  * 
  * @author Milo Casagrande
  * 
  */
 public class AuthenticationTest extends StressTestValues {
 
 	// The maximum number of elements to create
 	private final int maxElements;
 	private static int counter = 0;
 
 	public AuthenticationTest(final int maxElements) {
 		super();
 
 		this.maxElements = maxElements;
 	}
 
 	public AbstractMBean[] createAuthenticationBean() {
 		AbstractMBean[] beanArray = null;
 
 		MAuthenticationBean authBean = null;
 		MUserBean userBean = null;
 
 		if (counter < maxElements) {
 			beanArray = new AbstractMBean[2];
 			authBean = new MAuthenticationBean();
 
 			authBean.setLogin(String.format(AUTH_ID, counter));
 			authBean.setUser(String.format(USR_ID, counter));
 			authBean.setPassword(getRandomPwd());
 
 			userBean = new MUserBean();
 
 			userBean.setBirthday(getRandomDate());
 			userBean.setBuddyList(String.format(USR_LIST_ID, counter));
 			userBean.setEmail(String.format(EMAIL, counter));
 			userBean.setFirstName(String.format(FIRST_NAME, counter));
 			userBean.setLastName(String.format(LAST_NAME, counter));
 			userBean.setGender(getRandomGender());
 			userBean.setId(String.format(USR_ID, counter));
 			userBean.setLastConnection(System.currentTimeMillis());
 			userBean.setLogin(String.format(LOGIN, counter));
 			userBean.setName(String.format(NAME, counter));
 			userBean.setSession(String.format(SESSION, counter));
 			userBean.setSubscribtionList(String.format(SUB_LIST_ID, counter));
 
 			beanArray[0] = authBean;
 			beanArray[1] = userBean;
 		}
 
 		counter++;
 
 		return beanArray;
 	}
 
	public void createAuthentication(final MUserBean userBean, final MAuthenticationBean authBean)
	        throws InternalBackEndException {
 		try {
 			final AuthenticationManager authManager = new AuthenticationManager(new StorageManager(
 			        new WrapperConfiguration(new File(CONF_FILE))));
 			authManager.create(userBean, authBean);
		} catch (final IOBackEndException ex) {
			throw new InternalBackEndException(ex);
 		}
 	}
 }
