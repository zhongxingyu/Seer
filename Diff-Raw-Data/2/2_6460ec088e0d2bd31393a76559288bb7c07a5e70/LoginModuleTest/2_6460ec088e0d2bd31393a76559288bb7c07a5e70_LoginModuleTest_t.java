 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.security.authentication;
 
 import java.net.URL;
 
 import javax.security.auth.login.LoginException;
 
 import org.eclipse.equinox.security.auth.ILoginContext;
 import org.eclipse.equinox.security.auth.LoginContextFactory;
 
 import org.eclipse.riena.internal.tests.Activator;
 import org.eclipse.riena.security.authentication.callbackhandler.TestLocalCallbackHandler;
 import org.eclipse.riena.security.authentication.module.TestLocalLoginModule;
 import org.eclipse.riena.tests.RienaTestCase;
 import org.eclipse.riena.tests.collect.NonUITestCase;
 
 @NonUITestCase
 public class LoginModuleTest extends RienaTestCase {
 
 	private static final String JAAS_CONFIG_FILE = "config/sample_jaas.config"; //$NON-NLS-1$
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		startBundles("org\\.eclipse\\.riena.communication.core", null);
 		startBundles("org\\.eclipse\\.riena.communication.factory.hessian", null);
 		startBundles("org\\.eclipse\\.riena.communication.registry", null);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testSimpleServiceSampleLoginSuccessful() throws Exception {
 		// set the userid,password that the authenticating callback handler will set (as user input)
 		TestLocalCallbackHandler.setSuppliedCredentials("testuser", "testpass");
 		// set the userid,password that we try the login Module will check for
 		TestLocalLoginModule.setCredentials("testuser", "testpass");
 
 		URL configUrl = Activator.getDefault().getContext().getBundle().getEntry(JAAS_CONFIG_FILE);
		ILoginContext secureContext = LoginContextFactory.createContext("LocalTest", configUrl);
 
 		secureContext.login();
 
 		assertNotNull(secureContext.getSubject());
 		assertNotNull(secureContext.getSubject().getPrincipals());
 		assertTrue(secureContext.getSubject().getPrincipals().size() > 0);
 	}
 
 	public void testSimpleServiceSampleLoginInvalidPassword() throws Exception {
 		try {
 			// set the userid,password that the authenticating callback handler will check for
 			TestLocalCallbackHandler.setSuppliedCredentials("testuser", "testpass");
 			// set the userid,password that we try to verify
 			TestLocalLoginModule.setCredentials("testuser", "invalidpass");
 
 			URL configUrl = Activator.getDefault().getContext().getBundle().getEntry(JAAS_CONFIG_FILE);
 			ILoginContext secureContext = LoginContextFactory.createContext("Local", configUrl);
 
 			secureContext.login();
 
 			fail("login MUST fail since the password is wrong");
 		} catch (LoginException e) {
 			ok("expecting an exception");
 		}
 
 	}
 }
