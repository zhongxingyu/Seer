 /*******************************************************************************
  * Copyright (c) 2012 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.reqif10.pror.configuration.provider;
 
 import static org.junit.Assert.assertEquals;
 
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.common.util.ReqIFToolExtensionUtil;
 import org.eclipse.rmf.reqif10.pror.configuration.ConfigurationFactory;
 import org.eclipse.rmf.reqif10.pror.configuration.ConfigurationPackage;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfigurations;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrToolExtension;
 import org.eclipse.rmf.reqif10.pror.presentation.linewrap.LinewrapConfiguration;
 import org.eclipse.rmf.reqif10.pror.presentation.linewrap.LinewrapFactory;
import org.eclipse.rmf.reqif10.pror.presentation.linewrap.provider.LinewrapConfigurationItemProvider;
 import org.eclipse.rmf.reqif10.pror.presentation.linewrap.provider.LinewrapItemProviderAdapterFactory;
 import org.eclipse.rmf.reqif10.pror.testframework.AbstractItemProviderTest;
 import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * A test case for the model object '<em><b>Pror Presentation Configurations</b></em>'.
  */
 public class ProrPresentationConfigurationsTest extends AbstractItemProviderTest {
 
 	/**
 	 * The fixture for this Pror Presentation Configurations test case.
 	 */
 	protected ProrPresentationConfigurations fixture = null;
 
 	/**
 	 * Sets the fixture for this Pror Presentation Configurations test case.
 	 */
 	protected void setFixture(ProrPresentationConfigurations fixture) {
 		this.fixture = fixture;
 	}
 
 	/**
 	 * Returns the fixture for this Pror Presentation Configurations test case.
 	 */
 	protected ProrPresentationConfigurations getFixture() {
 		return fixture;
 	}
 
 	/**
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Before
 	public void setUp() throws Exception {
 		setFixture(ConfigurationFactory.eINSTANCE.createProrPresentationConfigurations());
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@After
 	public void tearDown() throws Exception {
 		setFixture(null);
 	}
 	
 	@Test
 	public void testPresentationRegistration() throws Exception {
 		// We use the LinewrapPresentation just for this test -> Register Factory.
 		adapterFactory
 				.addAdapterFactory(new LinewrapItemProviderAdapterFactory());
 		
 		// Needs an EditingDomain.
 		((ProrPresentationConfigurationsItemProvider) ProrUtil.getItemProvider(
 				adapterFactory, fixture)).setEditingDomain(editingDomain);
 		
 		ReqIF reqif = getTestReqif("simple.reqif");
 		ProrToolExtension toolExtension = ConfigurationFactory.eINSTANCE
 				.createProrToolExtension();
 		toolExtension.setPresentationConfigurations(fixture);
 		ReqIFToolExtensionUtil.addToolExtension(reqif, toolExtension);
 
 		LinewrapConfiguration config = LinewrapFactory.eINSTANCE.createLinewrapConfiguration();
		LinewrapConfigurationItemProvider ip = (LinewrapConfigurationItemProvider) ProrUtil
 				.getConfigItemProvider(config, adapterFactory);
 
 		// should trigger registration.
 		setViaCommand(fixture, ConfigurationPackage.Literals.PROR_PRESENTATION_CONFIGURATIONS__PRESENTATION_CONFIGURATIONS, config);
 		assertEquals(ip.getRegisteredConfigurations().size(), 1);
 
 		// should trigger un-registration
 		removeViaCommand(fixture, ConfigurationPackage.Literals.PROR_PRESENTATION_CONFIGURATIONS__PRESENTATION_CONFIGURATIONS, config);
 		assertEquals(ip.getRegisteredConfigurations().size(), 0);
 
 	}
 
 } //ProrPresentationConfigurationsTest
