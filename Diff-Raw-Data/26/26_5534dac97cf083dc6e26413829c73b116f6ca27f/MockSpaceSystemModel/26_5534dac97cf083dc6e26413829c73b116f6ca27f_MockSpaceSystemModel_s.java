 package org.hbird.transport.payloadcodec;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hbird.transport.spacesystemmodel.SpaceSystemModel;
 import org.hbird.transport.spacesystemmodel.encoding.Encoding;
 import org.hbird.transport.spacesystemmodel.encoding.Encoding.BinaryRepresentation;
 import org.hbird.transport.spacesystemmodel.exceptions.UnknownParameterException;
 import org.hbird.transport.spacesystemmodel.exceptions.UnknownParameterGroupException;
 import org.hbird.transport.spacesystemmodel.parameters.HummingbirdParameter;
 import org.hbird.transport.spacesystemmodel.parameters.Parameter;
 import org.hbird.transport.spacesystemmodel.tmtcgroups.HummingbirdParameterGroup;
 import org.hbird.transport.spacesystemmodel.tmtcgroups.ParameterGroup;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MockSpaceSystemModel implements SpaceSystemModel {
 	private static final long serialVersionUID = -345641886444350845L;
 	private static final Logger LOG = LoggerFactory.getLogger(MockSpaceSystemModel.class);
 
 	private final String name = "MockSpaceModel";
 	private final Map<String, ParameterGroup> groups = new HashMap<>();
 	private final Map<String, Encoding> encodings = new HashMap<>();
 	private final Map<String, List<Object>> restrictions = new HashMap<>();
 
 	public static final String TEST_PREFIX = "Test";
 	public static final String TEST_GROUP_NAME = "Test group";
 	public static final String FUEL_PARAMETER_NAME = "Fuel";
 	public static final String SCID_PARAMETER_NAME = "SCID";
 	public static final String LASER_TEMP_PARAMETER_NAME = "Laser Temp";
 
 	public MockSpaceSystemModel() {
		LOG.debug("Building parameter " + SCID_PARAMETER_NAME);
 		Encoding uint31 = new Encoding(31, BinaryRepresentation.unsigned);
 		Parameter<Integer> spacecraftId = new HummingbirdParameter<Integer>(TEST_PREFIX + "." + SCID_PARAMETER_NAME, SCID_PARAMETER_NAME, "", "");
 		encodings.put(spacecraftId.getQualifiedName(), uint31);
 
		LOG.debug("Building parameter " + FUEL_PARAMETER_NAME);
 		Encoding uint12 = new Encoding(12, BinaryRepresentation.unsigned);
 		Parameter<Integer> fuelParam = new HummingbirdParameter<Integer>(TEST_PREFIX + "." + FUEL_PARAMETER_NAME, FUEL_PARAMETER_NAME, "", "");
 		encodings.put(fuelParam.getQualifiedName(), uint12);
 
		LOG.debug("Building parameter " + LASER_TEMP_PARAMETER_NAME);
 		Encoding twosInt31 = new Encoding(40, BinaryRepresentation.twosComplement);
 		Parameter<Long> laserTemp = new HummingbirdParameter<Long>(TEST_PREFIX + "." + LASER_TEMP_PARAMETER_NAME, LASER_TEMP_PARAMETER_NAME, "", "");
 		encodings.put(laserTemp.getQualifiedName(), twosInt31);
 
		LOG.debug("Building parameter group " + TEST_GROUP_NAME);
 		ParameterGroup testGroup = new HummingbirdParameterGroup(TEST_PREFIX + "." + TEST_GROUP_NAME, TEST_GROUP_NAME, "", "");
 		groups.put(testGroup.getQualifiedName(), testGroup);
 		testGroup.addIntegerParameter(spacecraftId.getQualifiedName(), spacecraftId);
 		testGroup.addIntegerParameter(fuelParam.getQualifiedName(), fuelParam);
 		testGroup.addLongParameter(laserTemp.getQualifiedName(), laserTemp);
 	}
 
 	@Override
 	public ParameterGroup getParameterGroup(final String name) throws UnknownParameterGroupException {
 		ParameterGroup group = groups.get(name);
 		if (group == null) {
 			throw new UnknownParameterGroupException(name);
 		}
 		return group;
 	}
 
 	@Override
 	public Collection<ParameterGroup> getParameterGroupsCollection() {
 		return groups.values();
 	}
 
 	@Override
 	public Parameter<?> getParameter(final String name) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void replaceParameterInModel(final String qualifiedName, final Parameter<?> newParameter) throws UnknownParameterException {
 		for (ParameterGroup pg : this.groups.values()) {
 			if (pg.getAllParameters().put(qualifiedName, newParameter) != null) {
 				// Parameter replaced, no need to iterate over the rest of the parameters.
 				break;
 			}
 		}
		throw new UnknownParameterException(newParameter.getName());
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public Map<String, ParameterGroup> getParameterGroups() {
 		return this.groups;
 	}
 
 	@Override
 	public Parameter<Integer> getIntParameter(final String qualifiedName) throws UnknownParameterException {
 		for (ParameterGroup pg : this.groups.values()) {
 			if (pg.getIntegerParameters().containsKey(qualifiedName)) {
 				return pg.getIntegerParameter(qualifiedName);
 			}
 		}
 		throw new UnknownParameterException(qualifiedName);
 	}
 
 	@Override
 	public Parameter<Long> getLongParameter(final String qualifiedName) throws UnknownParameterException {
 		for (ParameterGroup pg : this.groups.values()) {
 			if (pg.getLongParameters().containsKey(qualifiedName)) {
 				return pg.getLongParameter(qualifiedName);
 			}
 		}
 		throw new UnknownParameterException(qualifiedName);
 	}
 
 	@Override
 	public Parameter<BigDecimal> getBigDecimalParameter(final String qualifiedName) throws UnknownParameterException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Parameter<String> getStringParameter(final String qualifiedName) throws UnknownParameterException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Parameter<Float> getFloatParameter(final String qualifiedName) throws UnknownParameterException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Parameter<Double> getDoubleParameter(final String qualifiedName) throws UnknownParameterException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Parameter<Byte[]> getRawParameter(final String qualifiedName) throws UnknownParameterException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Map<String, Parameter<?>> getAllPayloadParameters() {
 		Map<String, Parameter<?>> allParameters = new HashMap<>();
 		for (ParameterGroup pg : this.groups.values()) {
 			for (String parameterId : pg.getAllParameters().keySet()) {
 				allParameters.put(parameterId, pg.getAllParameters().get(parameterId));
 			}
 		}
 		return allParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<Integer>> getAllIntegerParameters() {
 		Map<String, Parameter<Integer>> allParameters = new HashMap<>();
 		for (ParameterGroup pg : this.groups.values()) {
 			Map<String, Parameter<Integer>> integerParameters = pg.getIntegerParameters();
 			if (integerParameters != null) {
 				for (String qualifiedName : integerParameters.keySet()) {
 					allParameters.put(qualifiedName, integerParameters.get(qualifiedName));
 				}
 			}
 		}
 		return allParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<Long>> getAllLongParameters() {
 		Map<String, Parameter<Long>> allParameters = new HashMap<>();
 		for (ParameterGroup pg : this.groups.values()) {
 			Map<String, Parameter<Long>> longParameters = pg.getLongParameters();
 			if (longParameters != null) {
 				for (String qualifiedName : longParameters.keySet()) {
 					allParameters.put(qualifiedName, longParameters.get(qualifiedName));
 				}
 			}
 		}
 		return allParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<BigDecimal>> getAllBigDecimalParameters() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Map<String, Parameter<Float>> getAllFloatParameters() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Map<String, Parameter<Double>> getAllDoubleParameters() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Map<String, Parameter<String>> getAllStringParameters() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Map<String, Parameter<Byte[]>> getAllRawParameters() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Map<String, List<Object>> getAllPayloadRestrictions() {
 		return restrictions;
 	}
 
 	@Override
 	public Map<String, Encoding> getEncodings() {
 		return this.encodings;
 	}
 
 }
