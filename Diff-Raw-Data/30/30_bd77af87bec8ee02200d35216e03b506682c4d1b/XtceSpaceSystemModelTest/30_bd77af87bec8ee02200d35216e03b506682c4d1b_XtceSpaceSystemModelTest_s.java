 package org.hbird.transport.xtce;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.Map;
 
 import org.hbird.transport.generatedcode.xtce.SpaceSystem;
 import org.hbird.transport.spacesystemmodel.SpaceSystemModel;
 import org.hbird.transport.spacesystemmodel.encoding.Encoding;
 import org.hbird.transport.spacesystemmodel.exceptions.InvalidParameterTypeException;
 import org.hbird.transport.spacesystemmodel.exceptions.UnknownParameterGroupException;
 import org.hbird.transport.spacesystemmodel.parameters.Parameter;
 import org.hbird.transport.spacesystemmodel.tmtcgroups.ParameterGroup;
 import org.hbird.transport.xtce.exceptions.InvalidXtceFileException;
 import org.hbird.transport.xtce.exceptions.UnsupportedXtceConstructException;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class XtceSpaceSystemModelTest {
 
 	private SpaceSystem spaceSystem;
 
 	private static final int NUM_OF_PARAMETERS_IN_PAYLOADS = 3;
 	private static final int NUM_OF_PARAMETER_ENCODINGS = 4;
 	private static final int NUM_OF_PARAMETER_GROUPS = 4;
 
 	private static final String P_FLIGHT_HOURS_NAME = "FLIGHT_HOURS";
 	private static final int LIGHT_HOURS_LENGTH = 33;
 
 	private static final String P_LASER_TEMP_NAME = "LASER_TEMP";
 	private static final int LASER_TEMP_LENGTH = 11;
 
 	private static final String P_FUEL_NAME = "FUEL";
 	private static final int FUEL_LENGTH = 16;
 
	private static final int NUM_OF_INTEGER_TYPED_PARAMETERS = 3;
 	private static final int NUM_OF_LONG_TYPED_PARAMETERS = 1;
 
 	private static final String PG_FLIGHT_HOURS_NAME = "FlightHours";
 	private static final int PG_FLIGHT_HOURS_NUM_PARAMETERS = 1;
 	private static final int PG_FLIGHT_HOURS_NUM_LONG_PARAMETERS = 1;
 
 	private static final String PG_LASER_TEMP_NAME = "LaserTemp";
 	private static final int PG_LASER_TEMP_NUM_PARAMETERS = 1;
 	private static final int PG_LASER_TEMP_NUM_INT_PARAMETERS = 1;
 
 	private static final String PG_FUEL_LOAD_NAME = "FuelLoad";
 	private static final int PG_FUEL_LOAD_NUM_PARAMETERS = 1;
 	private static final int PG_FUEL_LOAD_NUM_INT_PARAMETERS = 1;
 
 	private static final String PG_ALL_SYSTEMS_NAME = "AllSystems";
 	private static final int PG_ALL_SYSTEMS_NUM_PARAMETERS = 3;
 	private static final int PG_ALL_SYSTEMS_NUM_INT_PARAMETERS = 2;
 	private static final int PG_ALL_SYSTEMS_NUM_LONG_PARAMETERS = 1;
 
 	private SpaceSystemModel xtceSsm;
 
 	private static XtceSpaceSystemModelFactory factory;
 
 	@BeforeClass
 	public static final void SetupOnce() {
 		factory = new XtceSpaceSystemModelFactory();
 	}
 
 	@Before
 	public final void perTestSetup() {
 		xtceSsm = null;
 	}
 
 
 	@Test
 	public void testSpaceSystemCreationAllUints() throws InvalidXtceFileException, InvalidParameterTypeException, NumberFormatException, UnsupportedXtceConstructException {
 		URL testFileUrl = XtceSpaceSystemModelTest.class.getResource("TestSat-all-uints.xml");
 		xtceSsm = factory.createSpaceSystemModel(testFileUrl.getPath());
 
 		// Assert it was created.
 		assertNotNull(xtceSsm);
 
 		testParameters(xtceSsm);
 
 		testParameterGroups(xtceSsm);
 	}
 
 
 	private static void testParameterGroups(final SpaceSystemModel xtceSsm) {
 		assertEquals("Should be 3 integer parameters", xtceSsm.getParameterGroups().size(), NUM_OF_PARAMETER_GROUPS);
 	}
 
 
 	private static void testParameters(final SpaceSystemModel xtceSsm) {
 		assertEquals(NUM_OF_PARAMETER_GROUPS, xtceSsm.getParameterGroups().size());
 
 		Map<String, Parameter<?>> allParameters = xtceSsm.getAllPayloadParameters();
 		assertEquals("There should be " + NUM_OF_PARAMETERS_IN_PAYLOADS + " parameters in payloads", NUM_OF_PARAMETERS_IN_PAYLOADS, allParameters.size());
 
 		String tmPrefix = xtceSsm.getName() + ".tm.";
 
 		assertTrue(allParameters.containsKey(tmPrefix + P_FLIGHT_HOURS_NAME));
 		assertTrue(allParameters.containsKey(tmPrefix + P_LASER_TEMP_NAME));
 		assertTrue(allParameters.containsKey(tmPrefix + P_FUEL_NAME));
 
 		Map<String, Encoding> encodings = xtceSsm.getEncodings();
 		assertEquals("There should be " + NUM_OF_PARAMETER_ENCODINGS + " encodings (one per parameter)", NUM_OF_PARAMETER_ENCODINGS, encodings.size());
 
 		Parameter<?> param = null;
 		param = allParameters.get(tmPrefix + P_FLIGHT_HOURS_NAME);
 		assertNotNull(param);
 		Encoding encoding = encodings.get(param.getQualifiedName());
 		assertEquals(LIGHT_HOURS_LENGTH, encoding.getSizeInBits());
 
 
 		param = allParameters.get(tmPrefix + P_LASER_TEMP_NAME);
 		assertNotNull(param);
 		encoding = encodings.get(param.getQualifiedName());
 		assertEquals(LASER_TEMP_LENGTH, encoding.getSizeInBits());
 
 		param = allParameters.get(tmPrefix + P_FUEL_NAME);
 		assertNotNull(param);
 		encoding = encodings.get(param.getQualifiedName());
 		assertEquals(FUEL_LENGTH, encoding.getSizeInBits());
 
 		assertEquals("Should be " + NUM_OF_INTEGER_TYPED_PARAMETERS + " integer type parameters", NUM_OF_INTEGER_TYPED_PARAMETERS, xtceSsm.getAllIntegerParameters().size());
 		assertEquals("Should be " + NUM_OF_LONG_TYPED_PARAMETERS + " long type parameters", NUM_OF_LONG_TYPED_PARAMETERS, xtceSsm.getAllLongParameters().size());
 	}
 
 
 	@Test
 	public void testParameterGroupsCreationAllUints() throws InvalidXtceFileException, InvalidParameterTypeException, UnknownParameterGroupException, UnsupportedXtceConstructException {
 		URL testFileUrl = XtceSpaceSystemModelTest.class.getResource("TestSat-all-uints.xml");
 		xtceSsm = factory.createSpaceSystemModel(testFileUrl.getPath());
 		String tmPrefix = xtceSsm.getName() + ".tm.";
 
 		Collection<ParameterGroup> parameterGroups = xtceSsm.getParameterGroups().values();
 		assertEquals("There should be " + NUM_OF_PARAMETER_GROUPS + " parameter groups", NUM_OF_PARAMETER_GROUPS, parameterGroups.size());
 
 		ParameterGroup allSystemsPg = xtceSsm.getParameterGroup(tmPrefix + PG_ALL_SYSTEMS_NAME);
 		assertEquals("There should be " + PG_ALL_SYSTEMS_NUM_PARAMETERS + " parameter groups", PG_ALL_SYSTEMS_NUM_PARAMETERS, allSystemsPg.getAllParameters().size());
 		assertEquals("There should be " + PG_ALL_SYSTEMS_NUM_INT_PARAMETERS + " parameter groups", PG_ALL_SYSTEMS_NUM_INT_PARAMETERS, allSystemsPg.getIntegerParameters().size());
 		assertEquals("There should be " + PG_ALL_SYSTEMS_NUM_LONG_PARAMETERS + " parameter groups", PG_ALL_SYSTEMS_NUM_LONG_PARAMETERS, allSystemsPg.getLongParameters().size());
 
 		ParameterGroup fuelPg = xtceSsm.getParameterGroup(tmPrefix + PG_FUEL_LOAD_NAME);
 		assertEquals("There should be " + PG_FUEL_LOAD_NUM_PARAMETERS + " parameter groups", PG_FUEL_LOAD_NUM_PARAMETERS, fuelPg.getAllParameters().size());
 		assertEquals("There should be " + PG_FUEL_LOAD_NUM_INT_PARAMETERS + " parameter groups", PG_FUEL_LOAD_NUM_INT_PARAMETERS, fuelPg.getIntegerParameters().size());
 		assertNull("There should be no long typed parameters on this group", fuelPg.getLongParameters());
 
 		ParameterGroup flightHoursPg = xtceSsm.getParameterGroup(tmPrefix + PG_FLIGHT_HOURS_NAME);
 		assertEquals("There should be " + PG_FLIGHT_HOURS_NUM_PARAMETERS + " parameter groups", PG_FLIGHT_HOURS_NUM_PARAMETERS, flightHoursPg.getAllParameters().size());
 		assertEquals("There should be " + PG_FLIGHT_HOURS_NUM_LONG_PARAMETERS + " parameter groups", PG_FLIGHT_HOURS_NUM_LONG_PARAMETERS, flightHoursPg.getLongParameters().size());
 		assertNull("There shouzld be no int typed parameters on this group", flightHoursPg.getIntegerParameters());
 
 		ParameterGroup laserTempPg = xtceSsm.getParameterGroup(tmPrefix + PG_LASER_TEMP_NAME);
 		assertEquals("There should be " + PG_LASER_TEMP_NUM_PARAMETERS + " parameter groups", PG_LASER_TEMP_NUM_PARAMETERS, laserTempPg.getAllParameters().size());
 		assertEquals("There should be " + PG_LASER_TEMP_NUM_INT_PARAMETERS + " parameter groups", PG_LASER_TEMP_NUM_INT_PARAMETERS, laserTempPg.getIntegerParameters().size());
 		assertNull("There should be no long typed parameters on this group", laserTempPg.getLongParameters());
 	}
 }
 
 
 
