 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.security.authorizationservice;
 
 import java.io.InputStream;
 
 import javax.security.auth.Subject;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 
 import org.eclipse.riena.core.service.Service;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.internal.security.authorizationservice.AuthorizationService;
 import org.eclipse.riena.internal.tests.Activator;
 import org.eclipse.riena.security.common.ISubjectHolder;
 import org.eclipse.riena.security.common.authentication.SimplePrincipal;
 import org.eclipse.riena.security.common.authorization.IAuthorizationService;
 import org.eclipse.riena.security.common.authorization.Sentinel;
 import org.eclipse.riena.security.simpleservices.authorizationservice.store.FilePermissionStore;
 
 /**
  * Tests the Sentinel which means we are testing for permissions without
  * actually activating java security. Permissions are checked by the Sentinel
  * instead
  */
 @NonUITestCase
 public class SentinelTest extends RienaTestCase {
 	private ServiceRegistration authorizationServiceReg;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		// create FilePermissionStore which we inject into a local AuthorizationService
 		final InputStream inputStream = this.getClass().getResourceAsStream("policy-def-test.xml"); //$NON-NLS-1$
 		final FilePermissionStore store = new FilePermissionStore(inputStream);
 		final ServiceReference ref = getContext().getServiceReference(IAuthorizationService.class.getName());
 		if (ref != null && ref.getBundle().getState() == Bundle.ACTIVE
 				&& ref.getBundle() != Activator.getDefault().getBundle()) {
 			ref.getBundle().stop();
 		}
 		// create and register a local AuthorizationService with a dummy permission store
 		final AuthorizationService authorizationService = new AuthorizationService();
 		authorizationServiceReg = getContext().registerService(IAuthorizationService.class.getName(),
 				authorizationService, null);
 		// inject my test filestore
 		authorizationService.bind(store);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		authorizationServiceReg.unregister();
		super.tearDown();
 	}
 
 	public void testWithoutUser() {
 		final boolean result = Sentinel.checkAccess(new TestcasePermission("testPerm"));
 		assertFalse("no permission if there is no subject", result);
 	}
 
 	public void testValidUser() {
 		final Subject subject = new Subject();
 		subject.getPrincipals().add(new SimplePrincipal("testuser"));
 		Service.get(ISubjectHolder.class).setSubject(subject);
 
 		final boolean result = Sentinel.checkAccess(new TestcasePermission("testPerm"));
 		assertTrue("has permission since valid subject", result);
 	}
 
 	public void testValidUserMissingPermissions() {
 		final Subject subject = new Subject();
 		subject.getPrincipals().add(new SimplePrincipal("anotheruser"));
 		Service.get(ISubjectHolder.class).setSubject(subject);
 
 		final boolean result = Sentinel.checkAccess(new TestcasePermission("testPerm"));
 		assertFalse("has no permission since subject has no permission", result);
 
 	}
 }
