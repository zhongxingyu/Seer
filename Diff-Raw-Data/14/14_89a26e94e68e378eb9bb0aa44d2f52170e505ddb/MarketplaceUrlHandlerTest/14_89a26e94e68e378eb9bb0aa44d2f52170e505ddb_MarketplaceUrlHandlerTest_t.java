 /*******************************************************************************
  * Copyright (c) 2011 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.mpc.tests.ui.wizard;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
import java.net.URL;

import org.eclipse.epp.internal.mpc.ui.CatalogRegistry;
 import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceUrlHandler;
 import org.eclipse.epp.internal.mpc.ui.wizards.MarketplaceUrlHandler.SolutionInstallationInfo;
import org.eclipse.epp.mpc.ui.CatalogDescriptor;
import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test {@link MarketplaceUrlHandler}
  * 
  * @author Benjamin Muskalla
  */
 public class MarketplaceUrlHandlerTest {
 
	@Before
	public void installMockMarketplace() throws Exception {
		URL url = new URL("http://marketplace.eclipse.org");
		CatalogDescriptor eclipseMarketplace = new CatalogDescriptor(url, "Eclipse Marketplace");
		CatalogRegistry.getInstance().register(eclipseMarketplace);
	}

 	@Test
 	public void testGetInstallationInfoNoValidUrl() throws Exception {
 		SolutionInstallationInfo info = MarketplaceUrlHandler.createSolutionInstallInfo(null);
 		assertNull(info);
 		info = MarketplaceUrlHandler.createSolutionInstallInfo("");
 		assertNull(info);
 		info = MarketplaceUrlHandler.createSolutionInstallInfo("http://www.eclipse.org");
 		assertNull(info);
 		String missingInstallId = "http://marketplace.eclipse.org/mpc/install?mpc_install=";
 		info = MarketplaceUrlHandler.createSolutionInstallInfo(missingInstallId);
 	}
 
 	@Test
 	public void testGetInstallationInfo() throws Exception {
 		String url = "http://marketplace.eclipse.org/mpc/install?x=19&y=17&mpc_state=&mpc_install=953";
 		SolutionInstallationInfo info = MarketplaceUrlHandler.createSolutionInstallInfo(url);
 		assertEquals("http://marketplace.eclipse.org", info.getCatalogDescriptor().getUrl().toExternalForm());
 		assertEquals("953", info.getInstallId());
 		assertNull(info.getState());
 	}
 
 	@Test
 	public void testGetInstallationInfoWithState() throws Exception {
 		String statefulUrl = "http://marketplace.eclipse.org/mpc/install?mpc_install=953";
 		SolutionInstallationInfo info = MarketplaceUrlHandler.createSolutionInstallInfo(statefulUrl);
 		assertEquals("http://marketplace.eclipse.org", info.getCatalogDescriptor().getUrl().toExternalForm());
 		assertEquals("953", info.getInstallId());
 		assertNull(info.getState());
 	}
 
 	@Test
 	public void testPotentialUrls() throws Exception {
 		assertFalse(MarketplaceUrlHandler.isPotentialSolution(null));
 		assertFalse(MarketplaceUrlHandler.isPotentialSolution(""));
 		String url = "http://marketplace.eclipse.org/mpc/install?x=19&y=17&mpc_state=&mpc_install=953";
 		assertTrue(MarketplaceUrlHandler.isPotentialSolution(url));
 
 		url = "http://marketplace.eclipse.org";
 		assertFalse(MarketplaceUrlHandler.isPotentialSolution(url));
 	}
 
 }
