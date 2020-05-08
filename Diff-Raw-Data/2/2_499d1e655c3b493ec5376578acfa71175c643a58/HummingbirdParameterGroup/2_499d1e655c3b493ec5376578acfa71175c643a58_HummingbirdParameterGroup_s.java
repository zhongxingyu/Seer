 package org.hbird.core.spacesystemmodel.tmtcgroups;
 
 import java.math.BigDecimal;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.hbird.core.commons.tmtc.Parameter;
 import org.hbird.core.commons.tmtc.ParameterGroup;
 import org.hbird.core.commons.tmtc.ParameterGroupReport;
 import org.hbird.core.commons.tmtc.exceptions.UnknownParameterException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * TELEMETRY
  *
  * @author Mark Doyle
  * @author Johannes Klug
  */
 public class HummingbirdParameterGroup implements ParameterGroup {
 	private static final long serialVersionUID = 7810839127277387757L;
 	private static final Logger LOG = LoggerFactory.getLogger(HummingbirdParameterGroup.class);
 
 	private final String qualifiedName;
 	private final String name;
 	private final String shortDescription;
 	private final String longDescription;
 	private long timeStamp = 0;
 
 	private final Map<String, Parameter<?>> parameters = new LinkedHashMap<String, Parameter<?>>();
 	private Map<String, Parameter<Integer>> integerParameters;
 	private Map<String, Parameter<Long>> longParameters;
 	private Map<String, Parameter<Float>> floatParameters;
 	private Map<String, Parameter<Double>> doubleParameters;
 	private Map<String, Parameter<BigDecimal>> bigDecimalParameters;
 	private Map<String, Parameter<String>> stringParameters;
 	private Map<String, Parameter<Byte[]>> rawParameters;
 
 	private final ParameterGroupReport parameterReport = new ParameterGroupReport();
 
 	/** List of Parameters belonging to this Group */
 
 	/**
 	 * Constructor of the ParameterGroup class.
 	 *
 	 * @param name
 	 *            The name of the container.
 	 * @param shortDescription
 	 *            A one line description of the container, used for tooltip type information.
 	 * @param longDescription
 	 *            A detailed description of the container.
 	 *
 	 */
 	public HummingbirdParameterGroup(final String qualifiedName, final String name, final String shortDescription, final String longDescription) {
 		this.name = name;
 		this.qualifiedName = qualifiedName;
 		this.shortDescription = shortDescription;
 		this.longDescription = longDescription;
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public String getShortDescription() {
 		return this.shortDescription;
 	}
 
 	@Override
 	public String getLongDescription() {
 		return this.longDescription;
 	}
 
 	@Override
 	public long getTimeStamp() {
 		return this.timeStamp;
 	}
 
 	@Override
 	public Map<String, Parameter<?>> getAllParameters() {
 		return parameters;
 	}
 
 	@Override
 	public Map<String, Parameter<Integer>> getIntegerParameters() {
 		return integerParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<Long>> getLongParameters() {
 		return longParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<Float>> getFloatParameters() {
 		return floatParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<Double>> getDoubleParameters() {
 		return doubleParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<BigDecimal>> getBigDecimalParameters() {
 		return bigDecimalParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<String>> getStringParameters() {
 		return stringParameters;
 	}
 
 	@Override
 	public Map<String, Parameter<Byte[]>> getRawParameters() {
 		return rawParameters;
 	}
 
 	@Override
 	public void addIntegerParameter(final String qualifiedName, final Parameter<Integer> parameter) {
 		if (this.integerParameters == null) {
 			this.integerParameters = new LinkedHashMap<String, Parameter<Integer>>();
 		}
 		this.integerParameters.put(qualifiedName, parameter);
 		this.parameters.put(qualifiedName, parameter);
 		this.parameterReport.incrementIntCount();
 	}
 
 	@Override
 	public void addLongParameter(final String qualifiedName, final Parameter<Long> parameter) {
 		if (this.longParameters == null) {
 			this.longParameters = new LinkedHashMap<String, Parameter<Long>>();
 		}
 		this.longParameters.put(qualifiedName, parameter);
 		this.parameters.put(qualifiedName, parameter);
 		this.parameterReport.incrementLongCount();
 	}
 
 	@Override
 	public void addBigDecimalParameter(final String qualifiedName, final Parameter<BigDecimal> parameter) {
 		if (this.bigDecimalParameters == null) {
 			this.bigDecimalParameters = new LinkedHashMap<String, Parameter<BigDecimal>>();
 		}
 		this.bigDecimalParameters.put(qualifiedName, parameter);
 		this.parameters.put(qualifiedName, parameter);
 		this.parameterReport.incrementBigDecimalCount();
 	}
 
 	@Override
 	public void addFloatParameter(final String qualifiedName, final Parameter<Float> parameter) {
 		if (this.floatParameters == null) {
 			this.floatParameters = new LinkedHashMap<String, Parameter<Float>>();
 		}
 		this.floatParameters.put(qualifiedName, parameter);
 		this.parameters.put(qualifiedName, parameter);
 		this.parameterReport.incrementFloatCount();
 	}
 
 	@Override
 	public void addDoubleParameter(final String qualifiedName, final Parameter<Double> parameter) {
 		if (this.doubleParameters == null) {
 			this.doubleParameters = new LinkedHashMap<String, Parameter<Double>>();
 		}
 		this.doubleParameters.put(qualifiedName, parameter);
 		this.parameters.put(qualifiedName, parameter);
 		this.parameterReport.incrementDoubleCount();
 	}
 
 	@Override
 	public void addStringParameter(final String qualifiedName, final Parameter<String> parameter) {
 		if (this.stringParameters == null) {
 			this.stringParameters = new LinkedHashMap<String, Parameter<String>>();
 		}
 		this.stringParameters.put(qualifiedName, parameter);
 		this.parameters.put(qualifiedName, parameter);
 		this.parameterReport.incrementStringCount();
 	}
 
 	@Override
 	public void addRawParameter(final String qualifiedName, final Parameter<Byte[]> parameter) {
 		if (this.rawParameters == null) {
 			this.rawParameters = new LinkedHashMap<String, Parameter<Byte[]>>();
 		}
 		this.rawParameters.put(qualifiedName, parameter);
 		this.parameters.put(qualifiedName, parameter);
 		this.parameterReport.incrementRawCount();
 	}
 
 	@Override
 	public ParameterGroupReport getParameterReport() {
 		return this.parameterReport;
 	}
 
 	// Cast suppress reasoning: Parameter names must be unique so if a Param is found in a specific type collection
 	// it is safe to cast.
 	@SuppressWarnings("unchecked")
 	@Override
 	public void replaceParameterInGroup(final String qualifiedName, final Parameter<?> parameter) {
 		String pname = parameter.getQualifiedName();
 
 		if (parameters.containsKey(pname)) {
 			parameters.put(qualifiedName, parameter);
 		}
 
 		if (integerParameters != null && integerParameters.containsKey(pname)) {
 			integerParameters.put(qualifiedName, (Parameter<Integer>) parameter);
 		}
 		else if (longParameters != null && longParameters.containsKey(pname)) {
 			longParameters.put(qualifiedName, (Parameter<Long>) parameter);
 		}
 		else if (bigDecimalParameters != null && bigDecimalParameters.containsKey(pname)) {
 			bigDecimalParameters.put(qualifiedName, (Parameter<BigDecimal>) parameter);
 		}
 		else if (floatParameters != null && floatParameters.containsKey(pname)) {
 			floatParameters.put(qualifiedName, (Parameter<Float>) parameter);
 		}
 		else if (doubleParameters != null && doubleParameters.containsKey(pname)) {
 			doubleParameters.put(qualifiedName, (Parameter<Double>) parameter);
 		}
 		else if (stringParameters != null && stringParameters.containsKey(pname)) {
 			stringParameters.put(qualifiedName, (Parameter<String>) parameter);
 		}
 		else if (rawParameters != null && rawParameters.containsKey(pname)) {
 			rawParameters.put(qualifiedName, (Parameter<Byte[]>) parameter);
 		}
 	}
 
 	@Override
 	public Parameter<Integer> getIntegerParameter(final String qualifiedName) throws UnknownParameterException {
 		Parameter<Integer> p = integerParameters.get(qualifiedName);
 		if (p == null) {
 			throw new UnknownParameterException(qualifiedName);
 		}
 		return p;
 	}
 
 	@Override
 	public Parameter<Long> getLongParameter(final String qualifiedName) throws UnknownParameterException {
 		Parameter<Long> p = longParameters.get(qualifiedName);
 		if (p == null) {
 			throw new UnknownParameterException(qualifiedName);
 		}
 		return p;
 	}
 
 	@Override
 	public ParameterGroup copyAllParameterValues(final ParameterGroup sourceGroup) {
 		try {
 			// Ints
 			if (integerParameters != null) {
 				for (String qualifiedName : integerParameters.keySet()) {
 					getIntegerParameter(qualifiedName).setValue(sourceGroup.getIntegerParameter(qualifiedName).getValue());
 					getIntegerParameter(qualifiedName).setReceivedTime(sourceGroup.getIntegerParameter(qualifiedName).getReceivedTime());
 				}
 			}
 			if (longParameters != null) {
 				for (String qualifiedName : longParameters.keySet()) {
 					getLongParameter(qualifiedName).setValue(sourceGroup.getLongParameter(qualifiedName).getValue());
 					getLongParameter(qualifiedName).setReceivedTime(sourceGroup.getLongParameter(qualifiedName).getReceivedTime());
 				}
 			}
 			if (floatParameters != null) {
 				for (String qualifiedName : floatParameters.keySet()) {
 					getFloatParameter(qualifiedName).setValue(sourceGroup.getFloatParameter(qualifiedName).getValue());
 					getFloatParameter(qualifiedName).setReceivedTime(sourceGroup.getFloatParameter(qualifiedName).getReceivedTime());
 				}
 			}
 			if (doubleParameters != null) {
 				for (String qualifiedName : doubleParameters.keySet()) {
 					getDoubleParameter(qualifiedName).setValue(sourceGroup.getDoubleParameter(qualifiedName).getValue());
 					getDoubleParameter(qualifiedName).setReceivedTime(sourceGroup.getDoubleParameter(qualifiedName).getReceivedTime());
 				}
 			}
 			if (bigDecimalParameters != null) {
 				for (String qualifiedName : bigDecimalParameters.keySet()) {
 					getBigDecimalParameter(qualifiedName).setValue(sourceGroup.getBigDecimalParameter(qualifiedName).getValue());
 					getBigDecimalParameter(qualifiedName).setReceivedTime(sourceGroup.getBigDecimalParameter(qualifiedName).getReceivedTime());
 				}
 			}
 			if (stringParameters != null) {
 				for (String qualifiedName : stringParameters.keySet()) {
 					getStringParameter(qualifiedName).setValue(sourceGroup.getStringParameter(qualifiedName).getValue());
 					getStringParameter(qualifiedName).setReceivedTime(sourceGroup.getStringParameter(qualifiedName).getReceivedTime());
 				}
 			}
 			if (rawParameters != null) {
 				for (String qualifiedName : rawParameters.keySet()) {
 					getRawParameter(qualifiedName).setValue(sourceGroup.getRawParameter(qualifiedName).getValue());
 					getRawParameter(qualifiedName).setReceivedTime(sourceGroup.getRawParameter(qualifiedName).getReceivedTime());
 				}
 			}
 		}
 		catch (UnknownParameterException e) {
 			LOG.error("Unknown parameter when copying parameter values. This is is a serious internal error and must indicate a corruption "
					+ "in memory, a system bug, or a seriosu misuse of the API (copying paraemters to a different space system"
 					+ "model which has a different structure.  The system must shut down as integrity cannot be guaranteed.");
 			System.exit(-1);
 		}
 
 		return this;
 	}
 
 	@Override
 	public Parameter<?> getParameter(final String qualifiedName) throws UnknownParameterException {
 		Parameter<?> p = parameters.get(qualifiedName);
 		if (p == null) {
 			throw new UnknownParameterException(qualifiedName);
 		}
 		return p;
 	}
 
 	@Override
 	public String getQualifiedName() {
 		return this.qualifiedName;
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
 	public Parameter<BigDecimal> getBigDecimalParameter(final String qualifiedName) throws UnknownParameterException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Parameter<String> getStringParameter(final String qualifiedName) throws UnknownParameterException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Parameter<Byte[]> getRawParameter(final String qualifiedName) throws UnknownParameterException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public String toString() {
 		return "HummingbirdParameterGroup [qualifiedName=" + qualifiedName + ", name=" + name + ", shortDescription=" + shortDescription + ", longDescription="
 				+ longDescription + ", timeStamp=" + timeStamp + ", parameters=" + parameters + "]";
 	}
 
 	@Override
 	public void setTimeStamp(final long timeStamp) {
 		this.timeStamp = timeStamp;
 	}
 
 }
