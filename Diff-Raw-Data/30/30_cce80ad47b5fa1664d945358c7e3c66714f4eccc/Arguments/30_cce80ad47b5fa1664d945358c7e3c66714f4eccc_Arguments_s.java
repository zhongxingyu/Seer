 package com.alexrnl.commons.arguments;
 
 import java.io.PrintStream;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 import com.alexrnl.commons.utils.StringUtils;
 import com.alexrnl.commons.utils.object.ReflectUtils;
 
 /**
  * Class which allow to parse string arguments and store them in a target object.
  * @author Alex
  */
 public class Arguments {
 	/** Logger */
 	private static Logger				lg							= Logger.getLogger(Arguments.class.getName());
 	
 	/** The tab character */
 	private static final Character		TAB							= '\t';
 	/** The number of tabs between the name of the argument and their description */
 	private static final int			NB_TABS_BEFORE_DESCRIPTION	= 4;
 	/** The short name for the help command */
 	public static final String			HELP_SHORT_NAME				= "-h";
 	/** The long name for the help command */
 	public static final String			HELP_LONG_NAME				= "--help";
 	
 	/** The name of the program. */
 	private final String				programName;
 	/** The object which holds the target */
 	private final Object				target;
 	/** The list of parameters in the target */
 	private final SortedSet<Parameter>	parameters;
 	/** The output to use to display the usage of the arguments */
 	private final PrintStream			out;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param programName
 	 *        the name of the program.
 	 * @param target
 	 *        the object which holds the target.
 	 */
 	public Arguments (final String programName, final Object target) {
 		this(programName, target, System.out);
 	}
 
 	/**
 	 * Constructor #1.<br />
 	 * @param programName
 	 *        the name of the program.
 	 * @param target
 	 *        the object which holds the target.
 	 * @param out
 	 *        the output stream to use for displaying argument's usage.
 	 */
 	public Arguments (final String programName, final Object target, final PrintStream out) {
 		super();
 		this.programName = programName;
 		this.target = target;
 		this.parameters = Collections.unmodifiableSortedSet(retrieveParameters(target));
 		this.out = out;
 	}
 	
 	/**
 	 * Parse and build a list with the parameters field marked with the {@link Param} annotation in
 	 * the object specified.
 	 * @param obj
 	 *        the object to parse for parameters.
 	 * @return the list of parameters associated to this object.
 	 */
 	private static SortedSet<Parameter> retrieveParameters (final Object obj) {
 		final SortedSet<Parameter> params = new TreeSet<>();
 		for (final Field field : ReflectUtils.retrieveFields(obj.getClass(), Param.class)) {
 			field.setAccessible(true);
 			final Parameter param = new Parameter(field, field.getAnnotation(Param.class));
 			if (params.contains(param)) {
 				lg.warning("Could not add the parameter " + param.getNames() + ". The probable " +
 						"cause is that the first name has already been used by another parameter");
 				continue;
 			}
 			params.add(param);
 		}
 		return params;
 	}
 	
 	/**
 	 * Parse the arguments and set the target in the target object.
 	 * @param arguments
 	 *        the arguments to parse.
 	 */
 	public void parse (final String... arguments) {
 		parse(Arrays.asList(arguments));
 	}
 	
 	/**
 	 * Parse the arguments and set the target in the target object.
 	 * @param arguments
 	 *        the arguments to parse.
 	 */
	public void parse (final Iterable<String> arguments) {
 		if (lg.isLoggable(Level.INFO)) {
 			lg.info("Parsing arguments " + arguments.toString());
 		}
 		
 		final Set<Parameter> requiredParameters = getRequiredParameters();
 		initializeBooleansParameters();
 		
 		// Parse arguments provided
 		final Iterator<String> iterator = arguments.iterator();
 		boolean helpRequested = false;
 		while (iterator.hasNext()) {
 			final String argument = iterator.next();
 			if (lg.isLoggable(Level.INFO)) {
 				lg.info("Processing argument " + argument);
 			}
 			
 			if (isHelp(argument)) {
 				helpRequested = true;
 				continue;
 			}
 			
 			final Parameter currentParameter = getParameterByName(argument);
 			if (currentParameter == null) {
				throw new IllegalArgumentException("No parameter with name " + argument + " found");
 			}
 			
 			// If the parameter is a boolean, then its presence is enough
 			final Class<?> parameterType = currentParameter.getField().getType();
 			if (parameterType.equals(Boolean.class) || parameterType.equals(boolean.class)) {
 				try {
 					if (parameterType.equals(boolean.class)) {
 						currentParameter.getField().setBoolean(target, true);
 					} else {
 						currentParameter.getField().set(target, Boolean.TRUE);
 					}
 					requiredParameters.remove(currentParameter);
 				} catch (IllegalArgumentException | IllegalAccessException e) {
 					lg.warning("Parameter " + argument + " boolean value could not be set: "
 							+ ExceptionUtils.display(e));
 				}
 				continue;
 			}
 			
 			// The parameter will take the next argument as value
 			if (!iterator.hasNext()) {
				throw new IllegalArgumentException("No value found for parameter " + argument);
 			}
 			final String value = iterator.next();
 			// Check type (TODO allow factories to set dynamically fields?)
 			if (parameterType.equals(String.class)) {
 				try {
 					currentParameter.getField().set(target, value);
 					requiredParameters.remove(currentParameter);
 				} catch (IllegalArgumentException | IllegalAccessException e) {
 					lg.warning("Parameter " + argument + " value could not be set: "
 							+ ExceptionUtils.display(e));
 				}
 			} else {
 				lg.warning("Could not set type " + parameterType.getName() + " for parameter "
 						+ argument);
 			}
 		}
 		
 		// Check that all required arguments were set
 		if (!requiredParameters.isEmpty()) {
 			final List<Set<String>> listParamNames = new ArrayList<>(requiredParameters.size());
 			for (final Parameter parameter : requiredParameters) {
 				listParamNames.add(parameter.getNames());
 			}
			throw new IllegalArgumentException("The following parameters were not set: "
					+ StringUtils.separateWith(", ", listParamNames));
 		}
 		
 		if (helpRequested) {
 			usage();
 		}
 	}
 	
 	/**
 	 * Check if the name of the argument is an alias of the help command.
 	 * @param name
 	 *        the name to check.
 	 * @return <code>true</code> if the argument is the help command.
 	 */
 	private static boolean isHelp (final String name) {
 		return HELP_SHORT_NAME.equals(name) || HELP_LONG_NAME.equals(name);
 	}
 	/**
 	 * Build a set with the reference of required parameters.
 	 * @return the required parameters of the target.
 	 */
 	private Set<Parameter> getRequiredParameters () {
 		final Set<Parameter> requiredParameters = new HashSet<>(parameters.size(), 1.0f);
 		for (final Parameter parameter : parameters) {
 			if (parameter.isRequired()) {
 				requiredParameters.add(parameter);
 			}
 		}
 		return requiredParameters;
 	}
 
 	/**
 	 * Initialize all Booleans parameters to false.
 	 */
 	private void initializeBooleansParameters () {
 		for (final Parameter parameter : parameters) {
 			if (parameter.getField().getType().equals(Boolean.class)) {
 				try {
 					parameter.getField().set(target, Boolean.FALSE);
 				} catch (IllegalArgumentException | IllegalAccessException e) {
 					lg.warning("Parameter " + parameter.getNames() + " value could not be " +
 							"initialize to false: " + ExceptionUtils.display(e));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Find a parameter according to its name.
 	 * @param name
 	 *        the name of the parameter to find.
 	 * @return the parameter which has the specified name.
 	 */
 	private Parameter getParameterByName (final String name) {
 		for (final Parameter parameter : parameters) {
 			if (parameter.getNames().contains(name)) {
 				return parameter;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Display the parameter's usage on the output specified in the constructor.
 	 */
 	public void usage () {
 		out.println(this);
 	}
 	
 	@Override
 	public String toString () {
 		final StringBuilder usage = new StringBuilder(programName).append(" usage as follow:");
 		for (final Parameter param : parameters) {
 			usage.append(StringUtils.NEW_LINE).append(TAB);
 			if (!param.isRequired()) {
 				usage.append('[');
 			} else {
 				usage.append(' ');
 			}
 			usage.append("  ").append(StringUtils.separateWith(", ", param.getNames()));
 			int nbTabs = NB_TABS_BEFORE_DESCRIPTION;
 			while (nbTabs-- > 0) {
 				usage.append(TAB);
 			}
 			usage.append(param.getDescription());
 			if (!param.isRequired()) {
 				usage.append("  ]");
 			}
 		}
 		return usage.toString();
 	}
 }
