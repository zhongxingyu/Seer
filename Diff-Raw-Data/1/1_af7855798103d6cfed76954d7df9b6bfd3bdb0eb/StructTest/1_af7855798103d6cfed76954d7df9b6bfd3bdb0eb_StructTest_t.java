 /*******************************************************************************
  * This file is protected by Copyright. 
  * Please refer to the COPYRIGHT file distributed with this source distribution.
  *
  * This file is part of REDHAWK IDE.
  *
  * All rights reserved.  This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 // BEGIN GENERATED CODE
 package mil.jpeojtrs.sca.prf.tests;
 
 import junit.framework.Assert;
 import junit.textui.TestRunner;
 import mil.jpeojtrs.sca.prf.AccessType;
 import mil.jpeojtrs.sca.prf.ConfigurationKind;
 import mil.jpeojtrs.sca.prf.PrfFactory;
 import mil.jpeojtrs.sca.prf.Properties;
 import mil.jpeojtrs.sca.prf.PropertyValueType;
 import mil.jpeojtrs.sca.prf.Struct;
 import mil.jpeojtrs.sca.prf.StructPropertyConfigurationType;
 
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 
 /**
  * <!-- begin-user-doc --> A test case for the model object '
  * <em><b>Struct</b></em>'. <!-- end-user-doc -->
  * <p>
  * The following operations are tested:
  * <ul>
  *   <li>{@link mil.jpeojtrs.sca.prf.PropertyContainer#getProperty(java.lang.String) <em>Get Property</em>}</li>
  * </ul>
  * </p>
  * @generated
  */
 public class StructTest extends AbstractPropertyTest {
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static void main(String[] args) {
 		TestRunner.run(StructTest.class);
 	}
 
 	/**
 	 * Constructs a new Struct test case with the given name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StructTest(String name) {
 		super(name);
 	}
 
 	/**
 	 * Returns the fixture for this Struct test case.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected Struct getFixture() {
 		return (Struct)fixture;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 * @generated NOT
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		final Properties props = Properties.Util.getProperties(resourceSet.getResource(PrfTests.getURI("testFiles/StructTest.prf.xml"), true));
 		final Struct struct = props.getStruct().get(0);
 		Assert.assertNotNull(struct);
 		setFixture(struct);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 * @generated NOT
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		setFixture(null);
 	}
 
 	/**
 	 * Tests the '{@link mil.jpeojtrs.sca.prf.PropertyContainer#getProperty(java.lang.String) <em>Get Property</em>}' operation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see mil.jpeojtrs.sca.prf.PropertyContainer#getProperty(java.lang.String)
 	 * @generated NOT
 	 */
 	public void testGetProperty__String() {
 		// END GENERATED CODE
 		Assert.assertNull(getFixture().getProperty(null));
 		// BEGIN GENERATED CODE
 	}
 
 	public void test_parse() throws Exception {
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		final Properties props = Properties.Util.getProperties(resourceSet.getResource(PrfTests.getURI("testFiles/StructTest.prf.xml"), true));
 		final Struct struct = props.getStruct().get(0);
 		Assert.assertNotNull(struct);
 		Assert.assertEquals("DCE:24acefac-62eb-4b7f-9177-cc7d78c76aca", struct.getId());
 		Assert.assertEquals(AccessType.READONLY, struct.getMode());
 		Assert.assertEquals("Name", struct.getName());
 		Assert.assertEquals("Sample Description", struct.getDescription());
 		
 		Assert.assertEquals(1, struct.getSimple().size());
 		Assert.assertEquals(PropertyValueType.BOOLEAN, struct.getSimple().get(0).getType());
 		Assert.assertEquals("DCE:24acefac-62eb-4b7f-9177-cc7d78c76acb", struct.getSimple().get(0).getId());
 		Assert.assertNotNull(struct.getConfigurationKind());
 		Assert.assertEquals(StructPropertyConfigurationType.FACTORYPARAM, struct.getConfigurationKind().get(0).getType());
 	}
 
 	public void testExtra() throws Exception {
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		final Properties props = Properties.Util.getProperties(resourceSet.getResource(PrfTests.getURI("testFiles/StructTest.prf.xml"), true));
 		Assert.assertNotNull(props);
 		final Struct struct = props.getStruct().get(0);
 		Assert.assertNotNull(struct);
 
 		// test unsetMode
 		struct.unsetMode();
 		Assert.assertFalse(struct.isSetMode());
 		Assert.assertEquals(AccessType.READWRITE, struct.getMode());
 		
 		struct.setMode(AccessType.WRITEONLY);
 		Assert.assertEquals(AccessType.WRITEONLY, struct.getMode());
 		
 		struct.setMode(null);
 		Assert.assertEquals(AccessType.READWRITE, struct.getMode());
 		
 		struct.setMode(AccessType.READWRITE);
 		Assert.assertEquals(AccessType.READWRITE, struct.getMode());
 		
 		
 		// test set null and non null type
 		final ConfigurationKind ck = PrfFactory.eINSTANCE.createConfigurationKind();
 		ck.setType(StructPropertyConfigurationType.ALLOCATION);
		struct.getConfigurationKind().clear();
 		struct.getConfigurationKind().add(ck);
 		Assert.assertEquals(StructPropertyConfigurationType.ALLOCATION, struct.getConfigurationKind().get(0).getType());
 		struct.getConfigurationKind().set(0, ck);
 		Assert.assertEquals(StructPropertyConfigurationType.ALLOCATION, struct.getConfigurationKind().get(0).getType());
 
 		struct.getConfigurationKind().clear();
 		Assert.assertTrue(struct.getConfigurationKind().isEmpty());
 	}
 
 } //StructTest
