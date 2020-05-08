 /*******************************************************************************
  * Copyright (c) 2011-2014 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.accesscontrol.test;
 
 import static org.junit.Assert.assertTrue;
 
 import org.eclipse.emf.emfstore.client.test.common.util.ProjectUtil;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESUsersessionImpl;
 import org.eclipse.emf.emfstore.internal.server.accesscontrol.PAPrivileges;
 import org.eclipse.emf.emfstore.internal.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Test the {@link PAPrivileges#ShareProject} privilege of a
  * {@link org.eclipse.emf.emfstore.internal.server.model.accesscontrol.roles.ProjectAdminRole ProjectAdminRole}.
  * 
  * @author emueller
  * 
  */
 public class CreateProjectTest extends ProjectAdminTest {
 
 	@BeforeClass
 	public static void beforeClass() {
		startEMFStoreWithPAProperties(PAPrivileges.ShareProject);
 	}
 
 	@AfterClass
 	public static void afterClass() {
 		stopEMFStore();
 	}
 
 	@Test(expected = AccessControlException.class)
 	public void shareProjectNotPA() throws ESException {
 		ProjectUtil.share(getUsersession(), getLocalProject());
 	}
 
 	@Test
 	public void shareProjectPA() throws ESException {
 		makeUserPA();
 		ProjectUtil.share(getUsersession(), getLocalProject());
 		final ACUser user = ESUsersessionImpl.class.cast(getUsersession()).toInternalAPI().getACUser();
 		hasProjectAdminRole(user, getProjectSpace().getProjectId());
 		assertTrue(getLocalProject().isShared());
 	}
 
 }
