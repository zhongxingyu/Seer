 package org.hbird.transport.spacesystemmodel;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hbird.transport.spacesystemmodel.exceptions.ParameterNotInGroupException;
 import org.hbird.transport.spacesystemmodel.exceptions.UnknownParameterGroupException;
 import org.hbird.transport.spacesystemmodel.parameters.Parameter;
 
 /**
  * TELEMETRY
  *
  * @author Mark Doyle
  * @author Johannes Klug
  */
 public class HummingbirdParameterGroup implements ParameterGroup {
 	private static final long serialVersionUID = 7810839127277387757L;
 
 	private final String name;
 	private final String shortDescription;
 	private final String longDescription;
 
 	private Map<String, Parameter<Integer>> integerParameters;
 	private Map<String, Parameter<Long>> longParameters;
 	private Map<String, Parameter<Float>> floatParameters;
 	private Map<String, Parameter<Double>> doubleParameters;
 	private Map<String, Parameter<BigDecimal>> bigDecimalParameters;
 	private Map<String, Parameter<String>> stringParameters;
 	private Map<String, Parameter<Byte[]>> rawParameters;
 
 	private final ParameterGroupReport parameterReport = new ParameterGroupReport();
 
 	/**
 	 * The restrictions defining when this container should process. Each restriction is a parameter / string pair. The
 	 * parameter will convert the string based on its type and compare itself against the resulting value. If the string
 	 * is invalid then this will always count as a failed match.
 	 */
 	protected List<Object> restrictions;
 
 	/** List of Parameters belonging to this Group */
 	private final Map<String, Parameter<?>> parameters = new LinkedHashMap<String, Parameter<?>>();
 
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
 	public HummingbirdParameterGroup(final String name, final String shortDescription, final String longDescription) {
 		this.name = name;
 		this.shortDescription = shortDescription;
 		this.longDescription = longDescription;
 	}
 
 	@Override
 	public void addRestriction(final Object payloadLayoutId) {
 		if (this.restrictions == null) {
 			this.restrictions = new ArrayList<Object>();
 		}
 		this.restrictions.add(payloadLayoutId);
 	}
 
 	@Override
 	public Object getRestriction() {
 		// TODO Auto-generated method stub
 		// return null;
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public int getSizeInBits() {
 		// TODO Auto-generated method stub
 		// return 0;
 		throw new UnsupportedOperationException();
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
 	public Map<String, Parameter<?>> getAllParameters() {
 		return parameters;
 	}
 
 	@Override
 	public Collection<Parameter<Integer>> getIntegerParameters() {
		if (integerParameters == null) {
			return null;
		}
		else {
			return integerParameters.values();
		}
 	}
 
 	@Override
 	public Collection<Parameter<Long>> getLongParameters() {
		if (longParameters == null) {
			return null;
		}
		else {
			return longParameters.values();
		}
 	}
 
 	@Override
 	public Collection<Parameter<Float>> getFloatParameters() {
 		return floatParameters.values();
 	}
 
 	@Override
 	public Collection<Parameter<Double>> getDoubleParameters() {
 		return doubleParameters.values();
 	}
 
 	@Override
 	public Collection<Parameter<BigDecimal>> getBigDecimalParameters() {
 		return bigDecimalParameters.values();
 	}
 
 	@Override
 	public Collection<Parameter<String>> getStringParameters() {
 		return stringParameters.values();
 	}
 
 	@Override
 	public Collection<Parameter<Byte[]>> getRawParameters() {
 		return rawParameters.values();
 	}
 
 	@Override
 	public void addIntegerParameter(final Parameter<Integer> parameter) {
 		if (this.integerParameters == null) {
 			this.integerParameters = new LinkedHashMap<String, Parameter<Integer>>();
 		}
 		this.integerParameters.put(parameter.getName(), parameter);
 		this.parameters.put(parameter.getName(), parameter);
 		this.parameterReport.incrementIntCount();
 	}
 
 	@Override
 	public void addLongParameter(final Parameter<Long> parameter) {
 		if (this.longParameters == null) {
 			this.longParameters = new LinkedHashMap<String, Parameter<Long>>();
 		}
 		this.longParameters.put(parameter.getName(), parameter);
 		this.parameters.put(parameter.getName(), parameter);
 		this.parameterReport.incrementLongCount();
 	}
 
 	@Override
 	public void addBigDecimalParameter(final Parameter<BigDecimal> parameter) {
 		if (this.bigDecimalParameters == null) {
 			this.bigDecimalParameters = new LinkedHashMap<String, Parameter<BigDecimal>>();
 		}
 		this.bigDecimalParameters.put(parameter.getName(), parameter);
 		this.parameters.put(parameter.getName(), parameter);
 		this.parameterReport.incrementBigDecimalCount();
 	}
 
 	@Override
 	public void addFloatParameter(final Parameter<Float> parameter) {
 		if (this.floatParameters == null) {
 			this.floatParameters = new LinkedHashMap<String, Parameter<Float>>();
 		}
 		this.floatParameters.put(parameter.getName(), parameter);
 		this.parameters.put(parameter.getName(), parameter);
 		this.parameterReport.incrementFloatCount();
 	}
 
 	@Override
 	public void addDoubleParameter(final Parameter<Double> parameter) {
 		if (this.doubleParameters == null) {
 			this.doubleParameters = new LinkedHashMap<String, Parameter<Double>>();
 		}
 		this.doubleParameters.put(parameter.getName(), parameter);
 		this.parameters.put(parameter.getName(), parameter);
 		this.parameterReport.incrementDoubleCount();
 	}
 
 	@Override
 	public void addStringParameter(final Parameter<String> parameter) {
 		if (this.stringParameters == null) {
 			this.stringParameters = new LinkedHashMap<String, Parameter<String>>();
 		}
 		this.stringParameters.put(parameter.getName(), parameter);
 		this.parameters.put(parameter.getName(), parameter);
 		this.parameterReport.incrementStringCount();
 	}
 
 	@Override
 	public void addRawParameter(final Parameter<Byte[]> parameter) {
 		if (this.rawParameters == null) {
 			this.rawParameters = new LinkedHashMap<String, Parameter<Byte[]>>();
 		}
 		this.rawParameters.put(parameter.getName(), parameter);
 		this.parameters.put(parameter.getName(), parameter);
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
 	public void replaceParameterInGroup(final Parameter<?> parameter) throws ParameterNotInGroupException {
 		String pname = parameter.getName();
 		if (integerParameters.containsKey(pname)) {
 			integerParameters.put(parameter.getName(), (Parameter<Integer>) parameter);
 		}
 		else if (longParameters.containsKey(pname)) {
 			longParameters.put(parameter.getName(), (Parameter<Long>) parameter);
 		}
 		else if (bigDecimalParameters.containsKey(pname)) {
 			bigDecimalParameters.put(parameter.getName(), (Parameter<BigDecimal>) parameter);
 		}
 		else if (floatParameters.containsKey(pname)) {
 			floatParameters.put(parameter.getName(), (Parameter<Float>) parameter);
 		}
 		else if (doubleParameters.containsKey(pname)) {
 			doubleParameters.put(parameter.getName(), (Parameter<Double>) parameter);
 		}
 		else if (stringParameters.containsKey(pname)) {
 			stringParameters.put(parameter.getName(), (Parameter<String>) parameter);
 		}
 		else if (rawParameters.containsKey(pname)) {
 			rawParameters.put(parameter.getName(), (Parameter<Byte[]>) parameter);
 		}
 		else {
 			throw new ParameterNotInGroupException(parameter);
 		}
 		
 		if (parameters.containsKey(pname)) {
 			parameters.put(parameter.getName(), (Parameter<?>) parameter);
 		}
 	}
 
 	@Override
 	public Parameter<Integer> getIntegerParameter(String name) throws UnknownParameterGroupException {
 		Parameter<Integer> p = integerParameters.get(name);
 		if(p == null) {
 			throw new UnknownParameterGroupException(name);
 		}
 		return p;
 	}
 
 	@Override
 	public Parameter<Long> getLongParameter(String name) throws UnknownParameterGroupException {
 		Parameter<Long> p = longParameters.get(name);
 		if(p == null) {
 			throw new UnknownParameterGroupException(name);
 		}
 		return p;
 	}
 }
