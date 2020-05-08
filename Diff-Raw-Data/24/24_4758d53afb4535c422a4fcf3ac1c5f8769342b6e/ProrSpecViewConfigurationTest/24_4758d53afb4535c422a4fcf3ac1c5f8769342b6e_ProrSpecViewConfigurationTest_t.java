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
 
 package org.eclipse.rmf.reqif10.configuration.tests;
 
import static org.junit.Assert.*;

import org.eclipse.rmf.pror.reqif10.configuration.Column;
 import org.eclipse.rmf.pror.reqif10.configuration.ConfigFactory;
import org.eclipse.rmf.pror.reqif10.configuration.ConfigPackage;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrSpecViewConfiguration;
 import org.eclipse.rmf.pror.reqif10.testframework.AbstractItemProviderTest;
 import org.junit.After;
 import org.junit.Before;
import org.junit.Test;
 
 /**
  * A test case for the model object '<em><b>Pror Spec View Configuration</b></em>'.
  */
 public class ProrSpecViewConfigurationTest extends AbstractItemProviderTest {
 
 	/**
 	 * The fixture for this Pror Spec View Configuration test case.
 	 */
 	protected ProrSpecViewConfiguration fixture = null;
 
 	/**
 	 * Sets the fixture for this Pror Spec View Configuration test case.
 	 */
 	protected void setFixture(ProrSpecViewConfiguration fixture) {
 		this.fixture = fixture;
 	}
 
 	/**
 	 * Returns the fixture for this Pror Spec View Configuration test case.
 	 */
 	protected ProrSpecViewConfiguration getFixture() {
 		return fixture;
 	}
 
 	/**
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Before
	public void setUp() throws Exception {
 		setFixture(ConfigFactory.eINSTANCE.createProrSpecViewConfiguration());
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@After
	public void tearDown() throws Exception {
 		setFixture(null);
 	}
	
	@Test
	public void testRenameColumn() {
		Column column = ConfigFactory.eINSTANCE.createColumn();
		column.setLabel("Foo");
		getFixture().getColumns().add(column);
		getItemProvider(getFixture()).addListener(listener);
		setViaCommand(column, ConfigPackage.Literals.COLUMN__LABEL, "Bar");
		assertEquals(1, notifications.size());
	}
 
 } //ProrSpecViewConfigurationTest
