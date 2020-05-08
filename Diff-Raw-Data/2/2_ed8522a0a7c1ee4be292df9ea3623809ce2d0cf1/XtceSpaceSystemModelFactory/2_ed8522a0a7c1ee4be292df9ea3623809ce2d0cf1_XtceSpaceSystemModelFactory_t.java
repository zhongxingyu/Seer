 package org.hbird.core.xtce; // Hi Mark.
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.lang.reflect.Field;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.ValidationException;
 import org.exolab.castor.xml.XMLContext;
 import org.hbird.core.commons.tmtc.Parameter;
 import org.hbird.core.commons.tmtc.ParameterGroup;
 import org.hbird.core.generatedcode.xtce.Argument;
 import org.hbird.core.generatedcode.xtce.ArgumentListItem;
 import org.hbird.core.generatedcode.xtce.ArgumentTypeSetItem;
 import org.hbird.core.generatedcode.xtce.BaseContainer;
 import org.hbird.core.generatedcode.xtce.BaseDataTypeChoice;
 import org.hbird.core.generatedcode.xtce.BinaryParameterType;
 import org.hbird.core.generatedcode.xtce.CommandMetaData;
 import org.hbird.core.generatedcode.xtce.Comparison;
 import org.hbird.core.generatedcode.xtce.ComparisonList;
 import org.hbird.core.generatedcode.xtce.ContainerSet;
 import org.hbird.core.generatedcode.xtce.EntryList;
 import org.hbird.core.generatedcode.xtce.FloatParameterType;
 import org.hbird.core.generatedcode.xtce.IntegerArgumentType;
 import org.hbird.core.generatedcode.xtce.IntegerParameterType;
 import org.hbird.core.generatedcode.xtce.MetaCommand;
 import org.hbird.core.generatedcode.xtce.MetaCommandSet;
 import org.hbird.core.generatedcode.xtce.ParameterSetTypeItem;
 import org.hbird.core.generatedcode.xtce.ParameterTypeSetTypeItem;
 import org.hbird.core.generatedcode.xtce.SequenceContainer;
 import org.hbird.core.generatedcode.xtce.SpaceSystem;
 import org.hbird.core.generatedcode.xtce.StringParameterType;
 import org.hbird.core.generatedcode.xtce.TelemetryMetaData;
 import org.hbird.core.generatedcode.xtce.types.FloatDataEncodingTypeEncodingType;
 import org.hbird.core.generatedcode.xtce.types.IntegerDataEncodingTypeEncodingType;
 import org.hbird.core.spacesystemmodel.SpaceSystemModel;
 import org.hbird.core.spacesystemmodel.SpaceSystemModelFactory;
 import org.hbird.core.spacesystemmodel.encoding.Encoding;
 import org.hbird.core.spacesystemmodel.encoding.Encoding.BinaryRepresentation;
 import org.hbird.core.spacesystemmodel.exceptions.InvalidParameterTypeException;
 import org.hbird.core.spacesystemmodel.exceptions.InvalidSpaceSystemDefinitionException;
 import org.hbird.core.spacesystemmodel.parameters.HummingbirdParameter;
 import org.hbird.core.spacesystemmodel.tmtcgroups.HummingbirdParameterGroup;
 import org.hbird.core.xtce.exceptions.UnsupportedXtceConstructException;
 import org.hbird.core.xtce.utils.XtceToJavaMapping;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.primitives.Ints;
 
 public final class XtceSpaceSystemModelFactory implements SpaceSystemModelFactory {
 
 	public static final BinaryRepresentation DEFAULT_STRING_ENCODING = BinaryRepresentation.UTF8;
 
 	private static final Logger LOG = LoggerFactory.getLogger(XtceSpaceSystemModelFactory.class);
 
 	private final String spaceSystemModelFilename;
 
 	private SpaceSystem spaceSystem;
 
 	private SpaceSystemModel model;
 
 	private String modelName;
 
 	private final Map<String, ParameterTypeSetTypeItem> xtceTmParameterTypes = new LinkedHashMap<String, ParameterTypeSetTypeItem>();
 	private final Map<String, ParameterTypeSetTypeItem> xtceTcParameterTypes = new LinkedHashMap<String, ParameterTypeSetTypeItem>();
 
 	private final Map<String, Parameter<Integer>> integerParameters = new LinkedHashMap<String, Parameter<Integer>>();
 	private final Map<String, Parameter<Integer>> integerArguments = new LinkedHashMap<String, Parameter<Integer>>();
 
 	private final Map<String, Parameter<Long>> longParameters = new LinkedHashMap<String, Parameter<Long>>();
 	private final Map<String, Parameter<Long>> longArguments = new LinkedHashMap<String, Parameter<Long>>();
 
 	private final Map<String, Parameter<Float>> floatParameters = new LinkedHashMap<String, Parameter<Float>>();
 	private final Map<String, Parameter<Float>> floatArguments = new LinkedHashMap<String, Parameter<Float>>();
 
 	private final Map<String, Parameter<Double>> doubleParameters = new LinkedHashMap<String, Parameter<Double>>();
 	private final Map<String, Parameter<Double>> doubleArguments = new LinkedHashMap<String, Parameter<Double>>();
 
 	private final Map<String, Parameter<BigDecimal>> bigDecimalParameters = new LinkedHashMap<String, Parameter<BigDecimal>>();
 	private final Map<String, Parameter<BigDecimal>> bigDecimalArguments = new LinkedHashMap<String, Parameter<BigDecimal>>();
 
 	private final Map<String, Parameter<String>> stringParameters = new LinkedHashMap<String, Parameter<String>>();
 	private final Map<String, Parameter<String>> stringArguments = new LinkedHashMap<String, Parameter<String>>();
 
 	private final Map<String, Parameter<Byte[]>> rawParameters = new LinkedHashMap<String, Parameter<Byte[]>>();
 	private final Map<String, Parameter<Byte[]>> rawArguments = new LinkedHashMap<String, Parameter<Byte[]>>();
 
 	/**
 	 * This contains all the parameter groups (layouts if you like) defined in XTCE. It's contents are injected into the model
 	 * this factory creates.
 	 */
 	private final Map<String, ParameterGroup> tmParameterGroups = new HashMap<String, ParameterGroup>();
 	private final Map<String, ParameterGroup> tcParameterGroups = new HashMap<String, ParameterGroup>();
 
 	private final Map<String, List<Object>> restrictions = new HashMap<String, List<Object>>();
 
 	private final Map<String, Encoding> encodings = new HashMap<String, Encoding>();
 
 	public XtceSpaceSystemModelFactory(final String spaceSystemModelFilename) {
 		this.spaceSystemModelFilename = spaceSystemModelFilename;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.hbird.core.xtce.SpaceSystemModelFactory#createSpaceSystemModel(java.lang.String)
 	 */
 	@Override
 	public final SpaceSystemModel createSpaceSystemModel() throws InvalidSpaceSystemDefinitionException {
 
 		LOG.debug("File = " + spaceSystemModelFilename);
 
 		model = new XtceSpaceSystemModel();
 
 		spaceSystem = unmarshallXtceXmlSpaceSystem(spaceSystemModelFilename);
 
 		modelName = spaceSystem.getName();
 
 		try {
 			createTelemetryModel();
 			createCommandModel();
 		}
 		catch (final NumberFormatException e1) {
 			LOG.error(e1.toString());
 			// TODO - 27.03.2012 kimmell - replace with appropriate exception
 			//			System.exit(-1);
 		}
 		catch (final InvalidParameterTypeException e1) {
 			LOG.error(e1.toString());
 			// TODO - 27.03.2012 kimmell - replace with appropriate exception
 			//			System.exit(-1);
 		}
 
 
 		try {
 			injectConstructsIntoModel();
 		}
 		catch (final IllegalArgumentException e) {
 			LOG.error("Critical Error creating XTCE based Space System Model");
 			e.printStackTrace();
 			// TODO - 27.03.2012 kimmell - replace with appropriate exception
 			System.exit(-1);
 		}
 		catch (final IllegalAccessException e) {
 			LOG.error("Critical Error creating XTCE based Space System Model");
 			e.printStackTrace();
 			// TODO - 27.03.2012 kimmell - replace with appropriate exception
 			System.exit(-1);
 		}
 
 		return model;
 	}
 
 	/**
 	 * Creates the generated class model from the XML file.
 	 * @param spacesystemmodelFilename
 	 * @return
 	 */
 	private final static SpaceSystem unmarshallXtceXmlSpaceSystem(final String spacesystemmodelFilename) {
 		SpaceSystem spaceSystem = null;
 		try {
 			final XMLContext context = new XMLContext();
 
 			// Create a new Unmarshaller
 			final Unmarshaller unmarshaller = context.createUnmarshaller();
 			unmarshaller.setClass(SpaceSystem.class);
 
 			// Unmarshall the space system object
 			spaceSystem = (SpaceSystem) unmarshaller.unmarshal(new FileReader(spacesystemmodelFilename));
 		}
 		catch (final FileNotFoundException e) {
 			LOG.error(e.toString());
 		}
 		catch (final MarshalException e) {
 			LOG.error(e.toString());
 		}
 		catch (final ValidationException e) {
 			LOG.error(e.toString());
 		}
 
 		return spaceSystem;
 	}
 
 	private void createTelemetryModel() throws InvalidSpaceSystemDefinitionException, NumberFormatException, InvalidParameterTypeException {
 		createAllTmParameterTypes(spaceSystem.getTelemetryMetaData());
 		createAllTmParameters();
 		createAllParameterGroups();
 		populateParameterGroups();
 	}
 
 	private void createCommandModel() throws InvalidSpaceSystemDefinitionException {
 		final CommandMetaData commandMetaData = spaceSystem.getCommandMetaData();
 		if (commandMetaData == null) {
 			LOG.info("No command metadate defined");
 			return;
 		}
 		createAllTcArgumentTypes(commandMetaData);
 		createAllTcArguments();
 		createAllCommandGroups();
 		populateCommandGroups();
 	}
 
 
 	/**
 	 * Bit nasty we have another method but we are working with XML. The generated code creates two separate classes
 	 * TelemetryMetaData and CommandMetaData.
 	 * @param commandMetaData
 	 * @throws InvalidSpaceSystemDefinitionException
 	 */
 	private final void createAllTcArgumentTypes(final CommandMetaData commandMetaData) throws InvalidSpaceSystemDefinitionException {
 		final int numberOfParameterTypes = commandMetaData.getParameterTypeSet().getParameterTypeSetTypeItemCount();
 
 		for (int parameterTypeIndex = 0; parameterTypeIndex < numberOfParameterTypes; ++parameterTypeIndex) {
 			final ParameterTypeSetTypeItem item = commandMetaData.getParameterTypeSet().getParameterTypeSetTypeItem(parameterTypeIndex);
 			final String name = checkParameterType(item);
 			xtceTcParameterTypes.put(name, item);
 		}
 	}
 
 	/**
 	 * @param telemetryMetaData
 	 * @return
 	 * @throws InvalidParameterTypeException
 	 * @throws InvalidSpaceSystemDefinitionException
 	 */
 	private final void createAllTmParameterTypes(final TelemetryMetaData telemetryMetaData) throws InvalidSpaceSystemDefinitionException {
 		final int numberOfParameterTypes = telemetryMetaData.getParameterTypeSet().getParameterTypeSetTypeItemCount();
 
 		for (int parameterTypeIndex = 0; parameterTypeIndex < numberOfParameterTypes; ++parameterTypeIndex) {
 			final ParameterTypeSetTypeItem item = telemetryMetaData.getParameterTypeSet().getParameterTypeSetTypeItem(parameterTypeIndex);
 			final String name = checkParameterType(item);
 			xtceTmParameterTypes.put(name, item);
 		}
 	}
 
 	private final void createAllTmParameters() throws InvalidSpaceSystemDefinitionException {
 		final TelemetryMetaData categoryMetaData = spaceSystem.getTelemetryMetaData();
 		final int numberOfParameters = categoryMetaData.getParameterSet().getParameterSetTypeItemCount();
 
 		// @formatter:off
 		for (int i = 0; i < numberOfParameters; ++i) {
 			final ParameterSetTypeItem xtceParameter = categoryMetaData.getParameterSet().getParameterSetTypeItem(i);
 			final String parameterTypeRef = xtceParameter.getParameter().getParameterTypeRef();
 			final ParameterTypeSetTypeItem xtceType = xtceTmParameterTypes.get(parameterTypeRef);
 
 			if (xtceType == null) {
 				throw new InvalidSpaceSystemDefinitionException("Unknown TM parameter type: " + parameterTypeRef
 						+ ". A parameter references an undeclared TM parameter type in the XTCE space system definition file.");
 			}
 
 			final String qualifiedNamePrefix = spaceSystem.getName() + ".tm.";
 			final String name = xtceParameter.getParameter().getName();
 			final String qualifiedName = qualifiedNamePrefix + name;
 			final String shortDescription = xtceParameter.getParameter().getShortDescription();
 			final String longDescription = xtceParameter.getParameter().getLongDescription();
 
 			// If it's an integer type ...
 			if (xtceType.getIntegerParameterType() != null) {
 				final IntegerParameterType type = xtceType.getIntegerParameterType();
 				if (!XtceToJavaMapping.doesXtceIntRequireJavaLong(type)) {
 					final Parameter<Integer> intParameter = new HummingbirdParameter<Integer>(qualifiedName, name, shortDescription, longDescription);
 					LOG.debug("Adding Integer parameter {}", intParameter.getQualifiedName());
 					integerParameters.put(intParameter.getQualifiedName(), intParameter);
 					encodings.put(intParameter.getQualifiedName(), createXtceIntegerEncoding(type));
 				}
 				else {
 					final Parameter<Long> longParameter = new HummingbirdParameter<Long>(qualifiedName, name, shortDescription, longDescription);
 					LOG.debug("Adding Long parameter {}", longParameter.getQualifiedName());
 					longParameters.put(longParameter.getQualifiedName(), longParameter);
 					encodings.put(longParameter.getQualifiedName(), createXtceIntegerEncoding(type));
 				}
 			}
 
 			// If it's a float type ...
 			else if (xtceType.getFloatParameterType() != null) {
 				final FloatParameterType type = xtceType.getFloatParameterType();
 				switch (type.getSizeInBits()) {
 					case VALUE_32:
 						final Parameter<Float> floatParameter = new HummingbirdParameter<Float>(qualifiedName, name, shortDescription, longDescription);
 						LOG.debug("Adding Float parameter {}", floatParameter.getQualifiedName());
 						floatParameters.put(floatParameter.getQualifiedName(), floatParameter);
 						// TODO - 27.03.2012 kimmell - add encoding
 						break;
 					case VALUE_64:
 						final Parameter<Double> doubleParameter = new HummingbirdParameter<Double>(qualifiedName, name, shortDescription, longDescription);
 						LOG.debug("Adding Double parameter {}", doubleParameter.getQualifiedName());
 						doubleParameters.put(doubleParameter.getQualifiedName(), doubleParameter);
 						// TODO - 27.03.2012 kimmell - add encoding
 						break;
 					case VALUE_128:
 						final Parameter<BigDecimal> bigDecimalParameter = new HummingbirdParameter<BigDecimal>(qualifiedName, name, shortDescription, longDescription);
 						LOG.debug("Adding BigDecimal parameter {}", bigDecimalParameter.getQualifiedName());
 						bigDecimalParameters.put(bigDecimalParameter.getQualifiedName(), bigDecimalParameter);
 						// TODO - 27.03.2012 kimmell - add encoding
 						break;
 					default:
 						throw new InvalidSpaceSystemDefinitionException("Invalid bit size for float type " + type.getName());
 				}
 			}
 
 			// If it's a string type ...
 			else if (xtceType.getStringParameterType() != null) {
 				final StringParameterType type = xtceType.getStringParameterType();
 				final Parameter<String> stringParameter = new HummingbirdParameter<String>(qualifiedName, name, shortDescription, longDescription);
 				LOG.debug("Adding String parameter {}", stringParameter.getQualifiedName());
 				stringParameters.put(stringParameter.getQualifiedName(), stringParameter);
 				encodings.put(stringParameter.getQualifiedName(), createXtceStringEncoding(type));
 			}
 
 			// If it's binary type ...
 			else if (xtceType.getBinaryParameterType() != null) {
 				final BinaryParameterType type = xtceType.getBinaryParameterType();
 				final Parameter<Byte[]> rawParameter = new HummingbirdParameter<Byte[]>(qualifiedName, name, shortDescription, longDescription);
 				LOG.debug("Adding raw parameter {}", rawParameter.getQualifiedName());
 				rawParameters.put(rawParameter.getQualifiedName(), rawParameter);
 				encodings.put(rawParameter.getQualifiedName(), createXtceBinaryEncoding(type));
 			}
 
 			else {
 				throw new InvalidSpaceSystemDefinitionException("Unknown or unsupported parameter type: " + parameterTypeRef
 						+ ". A parameter references an undeclared TM parameter type in the XTCE space system definition file.");
 			}
 			// @formatter:on
 		}
 	}
 
 	/**
 	 * @throws InvalidSpaceSystemDefinitionException
 	 */
 	private final void createAllTcArguments() throws InvalidSpaceSystemDefinitionException {
 		final CommandMetaData commandMetaData = spaceSystem.getCommandMetaData();
		final int numberOfTcArguments = commandMetaData.getParameterSet().getParameterSetTypeItemCount();
 
 		// @formatter:off
 		for (int i = 0; i < numberOfTcArguments; ++i) {
 			final ParameterSetTypeItem xtceArgument = commandMetaData.getParameterSet().getParameterSetTypeItem(i);
 			final String parameterTypeRef = xtceArgument.getParameter().getParameterTypeRef();
 			final ParameterTypeSetTypeItem xtceType = xtceTcParameterTypes.get(parameterTypeRef);
 
 			// If it's an integer type...
 			if (xtceType == null) {
 				throw new InvalidSpaceSystemDefinitionException("Unknown TC argument (parameter) type: " + parameterTypeRef
 						+ ". A parameter references an undeclared TC argument (parameter) type in the XTCE space system definition file.");
 			}
 
 			final String qualifiedNamePrefix = spaceSystem.getName() + ".tc.";
 			final String name = xtceArgument.getParameter().getName();
 			final String qualifiedName = qualifiedNamePrefix + name;
 			final String shortDescription = xtceArgument.getParameter().getShortDescription();
 			final String longDescription = xtceArgument.getParameter().getLongDescription();
 
 			if (xtceType.getIntegerParameterType() != null) {
 				final IntegerParameterType type = xtceType.getIntegerParameterType();
 				if (!XtceToJavaMapping.doesXtceIntRequireJavaLong(type)) {
 					final Parameter<Integer> intParameter = new HummingbirdParameter<Integer>(qualifiedName, name, shortDescription, longDescription);
 					if (LOG.isDebugEnabled()) {
 						LOG.debug("Adding Integer argument " + intParameter.getName());
 					}
 					integerArguments.put(intParameter.getQualifiedName(), intParameter);
 					encodings.put(intParameter.getQualifiedName(), createXtceIntegerEncoding(type));
 				}
 				else {
 					final Parameter<Long> longParameter = new HummingbirdParameter<Long>(qualifiedName, name, shortDescription, longDescription);
 					if (LOG.isDebugEnabled()) {
 						LOG.debug("Adding Long argument " + longParameter.getName());
 					}
 					longArguments.put(longParameter.getQualifiedName(), longParameter);
 					encodings.put(longParameter.getQualifiedName(), createXtceIntegerEncoding(type));
 				}
 			}
 
 			// If it's an float type...
 			else if (xtceType.getFloatParameterType() != null) {
 				final FloatParameterType type = xtceType.getFloatParameterType();
 				switch (type.getSizeInBits()) {
 					case VALUE_32:
 						final Parameter<Float> floatParameter = new HummingbirdParameter<Float>(qualifiedName, name, shortDescription, longDescription);
 						floatArguments.put(floatParameter.getQualifiedName(), floatParameter);
 						if (LOG.isDebugEnabled()) {
 							LOG.debug("Adding Float argument " + floatParameter.getName());
 						}
 						break;
 					case VALUE_64:
 						final Parameter<Double> doubleParameter = new HummingbirdParameter<Double>(qualifiedName, name, shortDescription, longDescription);
 						doubleArguments.put(doubleParameter.getQualifiedName(), doubleParameter);
 						if (LOG.isDebugEnabled()) {
 							LOG.debug("Adding Double argument " + doubleParameter.getName());
 						}
 						break;
 					case VALUE_128:
 						final Parameter<BigDecimal> bigDecimalParameter = new HummingbirdParameter<BigDecimal>(qualifiedName, name, shortDescription, longDescription);
 						bigDecimalArguments.put(bigDecimalParameter.getQualifiedName(), bigDecimalParameter);
 						if (LOG.isDebugEnabled()) {
 							LOG.debug("Adding BigDecimal argument " + bigDecimalParameter.getName());
 						}
 						break;
 					default:
 						throw new InvalidSpaceSystemDefinitionException("Could not add command argument " + type.getName() + " because it has an invalid bit size for float type.");
 				}
 			}
 			else {
 				throw new InvalidSpaceSystemDefinitionException("Unknown or unsupported TC argument (parameter) type: " + parameterTypeRef
 						+ ". A parameter references an undeclared TC argument (parameter) type in the XTCE space system definition file.");
 			}
 			// @formatter:on
 		}
 	}
 
 	/**
 	 * Create all ParameterGroups. In this iteration we create the parameter groups, but do not create the references
 	 * between them as the referenced objects do not yet exit.
 	 *
 	 * @throws InvalidSpaceSystemDefinitionException
 	 *
 	 * @throws UnsupportedXtceConstructException
 	 */
 	private final void createAllParameterGroups() throws InvalidSpaceSystemDefinitionException {
 		final String qualifiedNamePrefix = spaceSystem.getName() + ".tm.";
 		final int numTmParameterGroups = spaceSystem.getTelemetryMetaData().getContainerSet().getContainerSetTypeItemCount();
 		for (int containerIndex = 0; containerIndex < numTmParameterGroups; ++containerIndex) {
 			final SequenceContainer xtceContainer = spaceSystem.getTelemetryMetaData().getContainerSet().getContainerSetTypeItem(containerIndex)
 					.getSequenceContainer();
 
 			// @formatter:off
 			final ParameterGroup parameterGroup =
 					new HummingbirdParameterGroup(qualifiedNamePrefix + xtceContainer.getName(), xtceContainer.getName(),
 							xtceContainer.getShortDescription(), xtceContainer.getLongDescription());
 			// @formatter:on
 
 			tmParameterGroups.put(parameterGroup.getQualifiedName(), parameterGroup);
 			populateParameterGroupRestrictions(parameterGroup.getQualifiedName(), xtceContainer);
 
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Created ParameterGroup " + xtceContainer.getName());
 			}
 		}
 	}
 
 	/**
 	 * Create all ParameterGroups. In this iteration we create the parameter groups, but do not create the references
 	 * between them as the referenced objects do not yet exit.
 	 *
 	 * @throws InvalidSpaceSystemDefinitionException
 	 *
 	 * @throws UnsupportedXtceConstructException
 	 */
 	private final void createAllCommandGroups() throws InvalidSpaceSystemDefinitionException {
 		final String qualifiedNamePrefix = spaceSystem.getName() + ".tc.";
 		final int numTcParameterGroups = spaceSystem.getCommandMetaData().getMetaCommandSet().getMetaCommandSetItemCount();
 		for (int containerIndex = 0; containerIndex < numTcParameterGroups; ++containerIndex) {
 			final MetaCommand xtceContainer = spaceSystem.getCommandMetaData().getMetaCommandSet().getMetaCommandSetItem(containerIndex)
 					.getMetaCommand();
 
 			// @formatter:off
 			final ParameterGroup parameterGroup =
 					new HummingbirdParameterGroup(qualifiedNamePrefix + xtceContainer.getName(), xtceContainer.getName(),
 							xtceContainer.getShortDescription(), xtceContainer.getLongDescription());
 			// @formatter:on
 
 			tcParameterGroups.put(parameterGroup.getQualifiedName(), parameterGroup);
 
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Created TC ParameterGroup " + xtceContainer.getName());
 			}
 		}
 	}
 
 
 	/**
 	 * @throws InvalidSpaceSystemDefinitionException
 	 * @throws UnsupportedXtceConstructException
 	 *
 	 */
 	private final void populateParameterGroupRestrictions(final String qualifiedName, final SequenceContainer parameterGroupContainer)
 			throws InvalidSpaceSystemDefinitionException {
 		// If the group extends another, e.g. a payload that is linked to a header via a restriction
 		// we need to create the restrictions.
 		final BaseContainer baseContainer = parameterGroupContainer.getBaseContainer();
 		if (baseContainer != null) {
 			final List<Object> comparisons = new ArrayList<Object>();
 			// In Hummingbird we do not model from the packet level, only the payload. In light of this we stipulate
 			// that base containers representing parameter groups that are linked to another base container via a
 			// restriction
 			// (e.g. header) extend a base container whose name is defined as the
 			// SpaceSystemModel.HUMMINGBIRD_PROCESSED_HEADER
 			// constant
 			if (StringUtils.equalsIgnoreCase(baseContainer.getContainerRef(), SpaceSystemModel.HUMMINGBIRD_PROCESSED_HEADER)) {
 				// Check for lists of comparisons
 				final ComparisonList comparisonList = baseContainer.getRestrictionCriteria().getComparisonList();
 				if (comparisonList != null) {
 					final Comparison[] restrictionCriteria = comparisonList.getComparison();
 					for (final Comparison comparison : restrictionCriteria) {
 						final String comparisonValue = comparison.getValue();
 						comparisons.add(comparisonValue);
 						if (LOG.isDebugEnabled()) {
 							LOG.debug("Added restriction " + comparisonValue + " to parameter group " + parameterGroupContainer.getName());
 						}
 					}
 					restrictions.put(qualifiedName, comparisons);
 				}
 				// Check for a single comparison
 				final Comparison singleComparison = baseContainer.getRestrictionCriteria().getComparison();
 				if (singleComparison != null) {
 					final String comparisonValue = singleComparison.getValue();
 					comparisons.add(comparisonValue);
 					restrictions.put(qualifiedName, comparisons);
 					if (LOG.isDebugEnabled()) {
 						LOG.debug("Added restriction " + comparisonValue + " to parameter group " + parameterGroupContainer.getName());
 					}
 				}
 
 				if (baseContainer.getRestrictionCriteria().getBooleanExpression() != null) {
 					throw new InvalidSpaceSystemDefinitionException(
 							"Hummingbird does not currently support Boolean Expression restrictions. Offending Container = "
 									+ parameterGroupContainer.getName());
 				}
 				else if (baseContainer.getRestrictionCriteria().getChoiceValue() != null) {
 					throw new InvalidSpaceSystemDefinitionException("Hummingbird does not currently support Choice Value restrictions. Offending Container = "
 							+ parameterGroupContainer.getName());
 				}
 				else if (baseContainer.getRestrictionCriteria().getCustomAlgorithm() != null) {
 					throw new InvalidSpaceSystemDefinitionException(
 							"Hummingbird does not currently support Custom Algorithm restrictions. Offending Container = " + parameterGroupContainer.getName());
 				}
 				else if (baseContainer.getRestrictionCriteria().getNextContainer() != null) {
 					throw new InvalidSpaceSystemDefinitionException(
 							"Hummingbird does not currently support Next Container restrictions. Offending Container = " + parameterGroupContainer.getName());
 				}
 			}
 			else {
 				LOG.error("Hummingbird does not process hierarchical container models due to their incompatiablity with multi-packet spanning payloads and/or multi-frame spanning packets.");
 				LOG.error("Specific error: ");
 				LOG.error("ParameterGroup: " + parameterGroupContainer.getName() + " extends base container " + baseContainer.getContainerRef());
 			}
 		}
 	}
 
 	private void populateParameterGroups() throws InvalidSpaceSystemDefinitionException {
 		final String qualifiedNamePrefix = spaceSystem.getName() + ".tm.";
 		final ContainerSet containers = spaceSystem.getTelemetryMetaData().getContainerSet();
 
 		// For every defined container
 		for (int i = 0; i < containers.getContainerSetTypeItemCount(); i++) {
 			final SequenceContainer sequenceContainer = containers.getContainerSetTypeItem(i).getSequenceContainer();
 
 			// Get the ParameterGroup we have created that corresponds to this SequenceContainer
 			final ParameterGroup group = tmParameterGroups.get(qualifiedNamePrefix + sequenceContainer.getName());
 
 			// grab it's entry list
 			final EntryList parameterEntrys = sequenceContainer.getEntryList();
 
 			for (int x = 0; x < parameterEntrys.getEntryListTypeItemCount(); x++) {
 				final String parameterRef = parameterEntrys.getEntryListTypeItem(x).getParameterRefEntry().getParameterRef();
 
 				addParameterToGroup(group, qualifiedNamePrefix + parameterRef);
 
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("Added parameter " + qualifiedNamePrefix + parameterRef + " to group " + group.getName());
 				}
 			}
 		}
 	}
 
 	private void populateCommandGroups() throws InvalidSpaceSystemDefinitionException {
 		final String qualifiedNamePrefix = spaceSystem.getName() + ".tc.";
 		final MetaCommandSet commands = spaceSystem.getCommandMetaData().getMetaCommandSet();
 
 		// For every defined command
 		for (int i = 0; i < commands.getMetaCommandSetItemCount(); i++) {
 			final MetaCommand command = commands.getMetaCommandSetItem(i).getMetaCommand();
 			final ParameterGroup commandGroup = tcParameterGroups.get(qualifiedNamePrefix + command.getName());
 
 			final ArgumentListItem[] parameterEntrys = command.getArgumentList().getArgumentListItem();
 			for (int x = 0; x < parameterEntrys.length; x++) {
 				final ArgumentListItem argumentListEntry = parameterEntrys[x];
 				final Argument[] arguments = argumentListEntry.getArgument();
 				for(int y = 0; y < arguments.length; y++) {
 					final Argument argument = arguments[y];
 					final String argumentTypeRef = argument.getArgumentTypeRef();
 
 					addArgumentParameterToGroup(commandGroup, qualifiedNamePrefix + argumentTypeRef);
 
 					if (LOG.isDebugEnabled()) {
 						LOG.debug("Added TC argument " + qualifiedNamePrefix + argumentTypeRef + " to command group " + commandGroup.getName());
 					}
 				}
 			}
 		}
 	}
 
 	private void addParameterToGroup(final ParameterGroup group, final String qualifiedName) throws InvalidSpaceSystemDefinitionException {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Adding " + qualifiedName + " to ParameterGroup " + group.getQualifiedName());
 		}
 		if (integerParameters.containsKey(qualifiedName)) {
 			group.addIntegerParameter(integerParameters.get(qualifiedName));
 		}
 		else if (longParameters.containsKey(qualifiedName)) {
 			group.addLongParameter(longParameters.get(qualifiedName));
 		}
 		else if (stringParameters.containsKey(qualifiedName)) {
 			group.addStringParameter(stringParameters.get(qualifiedName));
 		}
 		else if (rawParameters.containsKey(qualifiedName)) {
 			group.addRawParameter(rawParameters.get(qualifiedName));
 		}
 		else {
 			// TODO Finish unsupported parameter types
 			throw new InvalidSpaceSystemDefinitionException("Hummingbird currently only supports integer, long string & binary parameters");
 		}
 	}
 
 	private void addArgumentParameterToGroup(final ParameterGroup group, final String qualifiedName) throws InvalidSpaceSystemDefinitionException {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Adding " + qualifiedName + " to ParameterGroup " + group.getQualifiedName());
 		}
 		if (integerArguments.containsKey(qualifiedName)) {
 			group.addIntegerParameter(integerArguments.get(qualifiedName));
 		}
 		else if (longArguments.containsKey(qualifiedName)) {
 			group.addLongParameter(longArguments.get(qualifiedName));
 		}
 		else if (stringArguments.containsKey(qualifiedName)) {
 			group.addStringParameter(stringArguments.get(qualifiedName));
 		}
 		else if (rawArguments.containsKey(qualifiedName)) {
 			group.addRawParameter(rawArguments.get(qualifiedName));
 		}
 		else {
 			// TODO Finish unsupported parameter types
 			throw new InvalidSpaceSystemDefinitionException("Hummingbird currently only supports integer, long string & binary parameters");
 		}
 	}
 
 	/**
 	 * Injects the data into the model using reflection. This means we don't have to pollute the Space System Model interface
 	 * with lots of setters.
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	private void injectConstructsIntoModel() throws IllegalArgumentException, IllegalAccessException {
 		final Field[] fields = model.getClass().getDeclaredFields();
 		// TODO Switch on String when jdk 7 works with camel! Much nicer!
 		for (final Field field : fields) {
 			field.setAccessible(true);
 			final String name = field.getName();
 			if (StringUtils.equals(name, "parameterGroups")) {
 				field.set(model, tmParameterGroups);
 			}
 			else  if(StringUtils.equals(name, "commands")) {
 				field.set(model, tcParameterGroups);
 			}
 			else if (StringUtils.equals(name, "restrictions")) {
 				field.set(model, restrictions);
 			}
 			else if (StringUtils.equals(name, "encodings")) {
 				field.set(model, encodings);
 			}
 			else if (StringUtils.equals(name, "name")) {
 				field.set(model, modelName);
 			}
 			else {
 				LOG.debug("Not interested in field : " + name);
 			}
 		}
 	}
 	/**
 	 * Checks the parameter and returns the name if valid.
 	 *
 	 * @param item
 	 * @return
 	 * @throws InvalidSpaceSystemDefinitionException
 	 */
 	private static String checkArgumentType(final ArgumentTypeSetItem item) throws InvalidSpaceSystemDefinitionException {
 		String name = null;
 
 		// If it's an integer parameter..
 		final IntegerArgumentType integerArgumentType = item.getIntegerArgumentType();
 		if (integerArgumentType != null) {
 			name = integerArgumentType.getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("IntegerParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		// If it is a float parameter...
 		else if (item.getFloatArgumentType() != null) {
 			name = item.getFloatArgumentType().getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("FloatParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		// If it is a string parameter...
 		else if (item.getStringArgumentType() != null) {
 			name = item.getStringArgumentType().getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("StringParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		// If it is a boolean parameter...
 		else if (item.getBooleanArgumentType() != null) {
 			name = item.getBooleanArgumentType().getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("BooleanParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		// If it is a binary parameter...
 		else if (item.getBinaryArgumentType() != null) {
 			name = item.getBinaryArgumentType().getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("BinaryParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		else {
 			throw new InvalidSpaceSystemDefinitionException("Unknown/unsupported parameter type: " + item);
 		}
 
 		return name;
 	}
 
 
 	/**
 	 * Checks the parameter and returns the name if valid.
 	 *
 	 * @param item
 	 * @return
 	 * @throws InvalidSpaceSystemDefinitionException
 	 */
 	private static String checkParameterType(final ParameterTypeSetTypeItem item) throws InvalidSpaceSystemDefinitionException {
 		String name = null;
 
 		// If it's an integer parameter..
 		final IntegerParameterType integerParameterType = item.getIntegerParameterType();
 		if (integerParameterType != null) {
 			name = integerParameterType.getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("IntegerParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		// If it is a float parameter...
 		else if (item.getFloatParameterType() != null) {
 			name = item.getFloatParameterType().getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("FloatParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		// If it is a string parameter...
 		else if (item.getStringParameterType() != null) {
 			name = item.getStringParameterType().getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("StringParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		// If it is a boolean parameter...
 		else if (item.getBooleanParameterType() != null) {
 			name = item.getBooleanParameterType().getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("BooleanParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		// If it is a binary parameter...
 		else if (item.getBinaryParameterType() != null) {
 			name = item.getBinaryParameterType().getName();
 			if (name == null) {
 				throw new InvalidSpaceSystemDefinitionException("BinaryParameter has a null name; cannot add to parameterTypes");
 			}
 		}
 		else {
 			throw new InvalidSpaceSystemDefinitionException("Unknown/unsupported parameter type: " + item);
 		}
 
 		return name;
 	}
 
 	/**
 	 * Covers Java Integers and Longs
 	 *
 	 * @param intParamType
 	 * @return
 	 * @throws InvalidSpaceSystemDefinitionException
 	 */
 	private static final Encoding createXtceIntegerEncoding(final IntegerParameterType intParamType) throws InvalidSpaceSystemDefinitionException {
 		final Encoding encoding = new Encoding();
 
 		int sizeInBits = 0;
 		try {
 			sizeInBits = Ints.checkedCast(intParamType.getSizeInBits());
 		}
 		catch (final IllegalArgumentException e) {
 			throw new InvalidSpaceSystemDefinitionException("Illegal value (" + intParamType.getSizeInBits() + ") defined as size in bits for parameter type "
 					+ intParamType.getName() + ". Hummingbird suppports sizes up to " + Integer.MAX_VALUE + ".");
 		}
 
 		encoding.setSizeInBits(sizeInBits);
 
 		final BaseDataTypeChoice baseDataTypeChoice = intParamType.getBaseDataTypeChoice();
 		if (baseDataTypeChoice == null) {
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Base data type does not have a base data type choice, assuming default of unsigned integer encoding");
 			}
 			encoding.setBinaryRepresentation(BinaryRepresentation.unsigned);
 			return encoding;
 		}
 
 		final IntegerDataEncodingTypeEncodingType xtceEncoding = baseDataTypeChoice.getIntegerDataEncoding().getEncoding();
 		switch (xtceEncoding) {
 			case UNSIGNED:
 				encoding.setBinaryRepresentation(BinaryRepresentation.unsigned);
 				break;
 			case TWOSCOMPLIMENT:
 				encoding.setBinaryRepresentation(BinaryRepresentation.twosComplement);
 				break;
 			case BCD:
 				encoding.setBinaryRepresentation(BinaryRepresentation.binaryCodedDecimal);
 				break;
 			case ONESCOMPLIMENT:
 				encoding.setBinaryRepresentation(BinaryRepresentation.onesComplement);
 				break;
 			case SIGNMAGNITUDE:
 				encoding.setBinaryRepresentation(BinaryRepresentation.signMagnitude);
 				break;
 			case PACKEDBCD:
 				encoding.setBinaryRepresentation(BinaryRepresentation.packedBinaryCodedDecimal);
 				break;
 			default:
 				throw new InvalidSpaceSystemDefinitionException("Invalid integer encoding in type " + intParamType);
 		}
 
 		return encoding;
 	}
 
 	/**
 	 * Covers Java Floats and Doubles.
 	 *
 	 * @param type
 	 * @return
 	 * @throws InvalidSpaceSystemDefinitionException
 	 */
 	private final static Encoding getFloatEncoding(final FloatParameterType type) throws InvalidSpaceSystemDefinitionException {
 		final BaseDataTypeChoice baseDataTypeChoice = type.getBaseDataTypeChoice();
 
 		final Encoding encoding = new Encoding();
 		encoding.setSizeInBits(Integer.parseInt(type.getSizeInBits().value()));
 
 		if (baseDataTypeChoice == null) {
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Base data type does not have a base data type choice, assuming default of IEEE754_1985 float encoding");
 			}
 			encoding.setBinaryRepresentation(BinaryRepresentation.IEEE754_1985);
 		}
 		final FloatDataEncodingTypeEncodingType xtceEncoding = baseDataTypeChoice.getFloatDataEncoding().getEncoding();
 
 		switch (xtceEncoding) {
 			case IEEE754_1985:
 				encoding.setBinaryRepresentation(BinaryRepresentation.IEEE754_1985);
 				break;
 			case MILSTD_1750A:
 				encoding.setBinaryRepresentation(BinaryRepresentation.MILSTD_1750A);
 				break;
 			default:
 				throw new InvalidSpaceSystemDefinitionException("Invalid float encoding in type " + type);
 		}
 		return encoding;
 	}
 
 	final static Encoding createXtceStringEncoding(final StringParameterType type) throws InvalidSpaceSystemDefinitionException {
 		final Encoding encoding = new Encoding();
 		if (type.getCharacterWidth() == null) {
 			// fall back to default encoding
 			encoding.setBinaryRepresentation(DEFAULT_STRING_ENCODING);
 		}
 		else {
 			switch (type.getCharacterWidth()) {
 				case VALUE_8:
 					encoding.setBinaryRepresentation(BinaryRepresentation.UTF8);
 					// TODO - 27.03.2012 kimmell - encoding.setSizeInBits(); - one to four bytes - what's the correct
 					// value in here?
 					break;
 				case VALUE_16:
 					encoding.setBinaryRepresentation(BinaryRepresentation.UTF16);
 					// TODO - 27.03.2012 kimmell - encoding.setSizeInBits(); - what's the correct value in here?
 					break;
 				default:
 					throw new InvalidSpaceSystemDefinitionException("Invalid string encoding type " + type);
 			}
 		}
 
 		return encoding;
 	}
 
 	final static Encoding createXtceBinaryEncoding(final BinaryParameterType type) throws InvalidSpaceSystemDefinitionException {
 		final Encoding encoding = new Encoding();
 		// TODO - 29.03.2012 kimmell - implement
 		return encoding;
 	}
 }
