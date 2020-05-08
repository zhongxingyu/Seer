 package com.mymed.tests.stress;
 
 import java.util.LinkedList;
 
import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.model.data.AbstractMBean;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.user.MUserBean;
import com.mymed.utils.MyMedLogger;
 
 /**
  * This is the class that implements the thread that is executed for the
  * Authentication test. Here we have only one thread since there is no method to
  * remove an authentication entry form the database
  * 
  * @author Milo Casagrande
  * 
  */
 public class AuthenticationThread extends Thread implements NumberOfElements {
 
 	private final LinkedList<AbstractMBean[]> authList = new LinkedList<AbstractMBean[]>();
 	private final Thread createAuthentication;
 
 	public AuthenticationThread() {
 		this(NUMBER_OF_ELEMENTS);
 	}
 
 	public AuthenticationThread(final int maxElements) {
 		super();
 
 		final AuthenticationTest authTest = new AuthenticationTest(maxElements);
 
 		createAuthentication = new Thread("createAuthentication") {
 			@Override
 			public void run() {
				MyMedLogger.getLog().info("Starting thread '{}'", getName());
 
 				while (authList.isEmpty()) {
 					final AbstractMBean[] beanArray = authTest.createAuthenticationBean();
 
 					if (beanArray == null) {
 						interrupt();
 						break;
 					}
 
 					authList.add(beanArray);
 
 					final MAuthenticationBean authBean = (MAuthenticationBean) beanArray[0];
 					final MUserBean userBean = (MUserBean) beanArray[1];
 
 					try {
 						authTest.createAuthentication(userBean, authBean);
					} catch (final InternalBackEndException ex) {
 						interrupt();
						MyMedLogger.getLog().info("Thread '{}' interrupted", getName());
 						break;
 					}
 
 					authList.pop();
 				}

				MyMedLogger.getLog().info("Thread '{}' completed", getName());
 			}
 		};
 	}
 
 	@Override
 	public void run() {
 		createAuthentication.start();
 	}
 }
