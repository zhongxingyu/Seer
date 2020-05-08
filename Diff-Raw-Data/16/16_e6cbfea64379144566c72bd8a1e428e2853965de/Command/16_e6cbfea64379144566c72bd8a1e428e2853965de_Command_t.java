 package com.hapiware.util.cmdlineparser;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.hapiware.util.cmdlineparser.constraint.Constraint;
 import com.hapiware.util.cmdlineparser.constraint.ConstraintException;
 
 public class Command
 {
 	private ElementBase _command = new ElementBase();
 	private final String _shortDescription;
 	private final CommandExecutor _commandExecutor;
 
 	private Map<String, Option.Internal> _definedOptions = new LinkedHashMap<String, Option.Internal>();
 	private Map<String, String> _definedOptionAlternatives = new HashMap<String, String>();
 	private Map<String, Argument.Internal<?>> _definedArguments =
 		new LinkedHashMap<String, Argument.Internal<?>>();
 	private boolean _mandatoryArguments;
 	private int _numOfOptionalArguments;
 	private List<Option.Internal> _cmdLineOptions = new ArrayList<Option.Internal>(); 
 	private List<Argument.Internal<?>> _cmdLineArguments = new ArrayList<Argument.Internal<?>>();
 	
 	
 	private Command(Command command)
 	{
 		_command = new ElementBase(command._command);
 		_commandExecutor = command._commandExecutor;
 		_mandatoryArguments = command._mandatoryArguments;
 		_cmdLineOptions.addAll(command._cmdLineOptions);
 		_cmdLineArguments.addAll(command._cmdLineArguments);
 		_numOfOptionalArguments = command._numOfOptionalArguments;
 		_shortDescription = command._shortDescription;
 		
 		// References are ok here.
 		_definedOptions = command._definedOptions;
 		_definedOptionAlternatives = command._definedOptionAlternatives;
 		_definedArguments = command._definedArguments;
 	}
 	
 	public Command(String name, String shortDescription)
 	{
 		if(name == null || name.trim().length() == 0)
 			throw new ConfigurationException("'name' must have a value.");
 		
 		if(!Util.checkName(name))
 			throw
 				new ConfigurationException(
 					"'name' for the command is incorrect ('" + name + "')."
 				);
 		
 		if(shortDescription == null || shortDescription.trim().length() == 0)
 			throw
 				new ConfigurationException(
 					"'shortDescription' for command '" + name + "' must have a value."
 				);
 
 		_command.name(name);
 		_commandExecutor = null;
 		_shortDescription = shortDescription;
 	}
 	
 	public Command(String name, String shortDescription, CommandExecutor commandExecutor)
 	{
 		if(name == null || name.trim().length() == 0)
 			throw new ConfigurationException("'name' must have a value.");
 
 		if(!Util.checkName(name))
 			throw
 				new ConfigurationException(
 					"'name' for the command is incorrect ('" + name + "')."
 				);
 		
 		if(shortDescription == null || shortDescription.trim().length() == 0)
 			throw
 				new ConfigurationException(
 					"'shortDescription' for command '" + name + "' must have a value."
 				);
 
 		if(commandExecutor == null)
 			throw
 				new ConfigurationException(
 					"'commandExecutor' for command '" + name + "' must have a value."
 				);
 		
 		_command.name(name);
 		_commandExecutor = commandExecutor;
 		_shortDescription = shortDescription;
 	}
 	
 	public Command alternatives(String...alternatives)
 	{
 		if(alternatives == null || alternatives.length == 0)
 			throw
 				new ConfigurationException(
 					"'alternatives' for command '" + _command.name() + "' must have a value."
 				);
 		for(int i = 0; i < alternatives.length; i++)
 			if(!Util.checkName(alternatives[i]))
 				throw
 					new ConfigurationException(
 						"Alternative name for command '" + _command.name() 
 							+ "' is incorrect ('" + alternatives[i] + "')."
 					);
 		
 		_command.alternatives(alternatives);
 		return this;
 	}
 	
 	public Command id(String id)
 	{
 		if(id == null || id.trim().length() == 0)
 			throw
 				new ConfigurationException(
 					"'id' for command '" + _command.name() + "' must have a value."
 				);
 		if(!Util.checkName(id))
 			throw
 				new ConfigurationException(
 					"'id' for command '" + _command.name() + "' is incorrect ('" + id + "')."
 				);
 		
 		_command.id(id);
 		return this;
 	}
 	
 	/**
 	 * For further details see {@link Description#description(String)}
 	 */
 	public Command description(String description)
 	{
 		if(description == null || description.trim().length() == 0)
 			throw
 				new ConfigurationException(
 					"'description' for command '" + _command.name() + "'  must have a value."
 				);
 		
 		_command.description(description);
 		return this;
 	}
 	
 	/**
 	 * For further details see {@link Description#strong(String)}
 	 */
 	public Command strong(String text)
 	{
 		_command.strong(text);
 		return this;
 	}
 	
 	/**
 	 * For further details see {@link Description#paragraph()}
 	 */
 	public Command paragraph()
 	{
 		_command.paragraph();
 		return this;
 	}
 	
 	/**
 	 * For further details see {@link Description#d(String)}
 	 */
 	public Command d(String description)
 	{
 		description(description);
 		return this;
 	}
 	
 	/**
 	 * For further details see {@link Description#b(String)}
 	 */
 	public Command b(String text)
 	{
 		strong(text);
 		return this;
 	}
 	
 	/**
 	 * For further details see {@link Description#p()}
 	 */
 	public Command p()
 	{
 		paragraph();
 		return this;
 	}
 	
 	public <T> Command add(Class<T> argumentType, Argument<T> argument)
 	{
 		if(argument == null)
 			throw
 				new ConfigurationException(
 					"'argument' for command '" + _command.name() + "' must have a value."
 				);
 		
 		Argument.Internal<T> internal = new Argument.Internal<T>(argument, argumentType);
 		if(internal.name() == null || internal.name().trim().length() == 0)
 			throw
 				new ConfigurationException(
 					"'argument' for command '" + _command.name() + "' must have a name."
 				);
 		
 		if(internal.description().size() == 0)
 			throw
 				new ConfigurationException(
 					"Argument '" + internal.name() + "' for command '" 
 						+ _command.name() + "' must have a description."
 				);
 		if(_definedArguments.containsKey(internal.name()))
 			throw
 				new ConfigurationException(
 					"Argument name '" + internal.name() + "' for command '" 
 						+ _command.name() +"' must be unique."
 				);
 
 		if(internal.optional() && !internal.hasDefaultValueForOptional()) {
 			String msg =
 				"When annotations are used then optional arguments must have a default value "
 					+ "(command '" + _command.name() + "', argument '" + internal.name() + "').";
 			throw new ConfigurationException(msg);
 		}
 		
 		for(Constraint<?> constraint : internal.constraints())
 			if(!constraint.typeCheck(argumentType)) {
 				String msg =
 					"Using '" + constraint.getClass().getName() + "' with argument type '"
 						+ argumentType + "' creates a type conflict "
 						+ "(command '" + _command.name() + "', argument '" + internal.name() + "').";
 				throw new ConfigurationException(msg);
 			}
 		
 		_definedArguments.put(internal.name(), internal);
 		if(!internal.optional()) {
 			_mandatoryArguments = true;
 			if(_numOfOptionalArguments >= 2) {
 				String msg =
 					"If there are more than one optional argument they must be the last arguments "
 						+ "(command '" + _command.name() + "', argument '" + internal.name() + "'). "
 						+ "A single optional argument can have any position.";
 				throw new ConfigurationException(msg);
 			}
 		}
 		else
 			_numOfOptionalArguments++;
 		return this;
 	}
 	
 	public Command add(Option option)
 	{
 		if(option == null)
 			throw
 				new ConfigurationException(
 					"'option' for command '" + _command.name() + "' must have a value."
 				);
 		
 		Option.Internal internal = new Option.Internal(option);
 		if(internal.name() == null || internal.name().trim().length() == 0)
 			throw
 				new ConfigurationException(
 					"'option' for command '" + _command.name() + "' must have a name."
 				);
 		
 		if(internal.description().size() == 0)
 			throw
 				new ConfigurationException(
 					"Option '" + internal.name() + "' for command '" 
 						+ _command.name() + "' must have a description."
 				);
 		if(_definedOptionAlternatives.containsKey(internal.name()))
 			throw
 				new ConfigurationException(
 					"Option name '" + internal.name() + "' for command '" 
 						+ _command.name() + "'must be unique."
 				);
 		
 		_definedOptions.put(internal.name(), internal);
 		_definedOptionAlternatives.put(internal.name(), internal.name());
 		for(String alternative : internal.alternatives())
 			if(_definedOptionAlternatives.put(alternative, internal.name()) != null)
 				throw
 					new ConfigurationException(
 						"Option alternative name '" + alternative + "' for command '" 
 							+ _command.name() + "'must be unique."
 					);
 		
 		return this;
 	}
 
 	
 	/**
 	 * {@code Command.Data} is a container class for holding information about the given commands.
 	 * <p>
 	 * This class is immutable <b>only if {@link Argument.Data} is immutable</b>.
 	 * 
 	 * @author <a href="http://www.hapiware.com" target="_blank">hapi</a>
 	 *
 	 */
 	public static final class Data
 		extends
 			DataBase
 	{
 		private final List<Argument.Data<?>> _arguments;
 		private final List<Option.Data> _options;
 		
 		/**
 		 * "Copy" constructs a data object from the internal command object.
 		 * 
 		 * @param internal
 		 * 		The internal command object.
 		 */
		Data(Internal internal)
 		{
 			super(internal.name(), internal.id(), internal.alternatives());
 			
 			List<Argument.Data<?>> argumentData = new ArrayList<Argument.Data<?>>();
 			for(Argument.Internal<?> argument : internal._outer._cmdLineArguments)
 				argumentData.add(argument.createDataObject());
 			_arguments = Collections.unmodifiableList(argumentData);
 
 			List<Option.Data> optionData = new ArrayList<Option.Data>();
 			for(Option.Internal option : internal._outer._cmdLineOptions)
 				optionData.add(new Option.Data(option));
 			_options  = Collections.unmodifiableList(optionData);
 		}
 		
 		
 		public Set<String> getAlternatives()
 		{
 			return super.getAlternatives();
 		}
 
 		
 		/**
 		 * Returns an array of command options.
 		 * 
 		 * @return
 		 * 		An array of options.
 		 */
 		public Option.Data[] getAllOptions()
 		{
 			return _options.toArray(new Option.Data[0]);
 		}
 		
 		
 		public boolean optionExists(String name)
 		{
 			for(Option.Data option : _options)
 				if(option.getName().equals(name))
 					return true;
 			
 			return false;
 		}
 		
 		
 		@SuppressWarnings("unchecked")
 		public <T> T optionValue(String name)
 		{
 			try {
 				Option.Data option = getOptions(name)[0];
 				if(option.getArgument() != null)
 					return (T)option.getArgument().getValue();
 				else
 					return null;
 			}
 			catch(IndexOutOfBoundsException e) {
 				return null;
 			}
 		}
 		
 		public Option.Data[] getOptions(String name)
 		{
 			List<Option.Data> options = new ArrayList<Option.Data>();
 			for(Option.Data option : _options)
 				if(option.getName().equals(name))
 					options.add(option);
 			
 			return options.toArray(new Option.Data[0]);
 		}
 		
 		public Argument.Data<?> getArgument(String name)
 		{
 			for(Argument.Data<?> argument : _arguments)
 				if(argument.getName().equals(name))
 					return argument;
 
 			return null;
 		}
 
 		/**
 		 * Returns an array of command arguments.
 		 * 
 		 * @return
 		 * 		An array of arguments.
 		 */
 		public Argument.Data<?>[] getAllArguments()
 		{
 			return _arguments.toArray(new Argument.Data[0]);
 		}
 		
 		/**
 		 * Returns a {@code String} representation of {@code Option.Data} object. The form is:
 		 * <p>
 		 * <code>{NAME(ID) : OPTIONS : ARGUMENTS : ALTERNATIVES}</code>
 		 * <p>
 		 * where:
 		 * 	<ul>
 		 * 		<li>NAME is the argument name.</li>
 		 * 		<li>ID is the id for the argument.</li>
 		 * 		<li>MULTI indicates if the option can occur multiple times.</li>
 		 * 		<li>VALUE is the argument value.</li>
 		 * 		<li>OPTIONAL indicates if the value is optional or not.</li>
 		 * 		<li>ALTERNATIVES alternative names for the option</li>
 		 * 	</ul>
 		 */
 		@Override
 		public String toString()
 		{
 			return
 				"{" + getName() + "(" + getId()  + ") : "
 					+ getAllOptions() + ":"
 					+ getAllArguments() + ":"
 					+ getAlternatives()
 					+ "}";
 		}
 	}
 
 	
 	static final class Internal
 		implements
 			Parser
 	{
 		private Command _outer;
 		
 		public Internal(Internal internal)
 		{
 			_outer = new Command(internal._outer);
 		}
 		public Internal(Command outer)
 		{
 			_outer = outer;
 		}
 		public String name()
 		{
 			return _outer._command.name();
 		}
 		public Set<String> alternatives()
 		{
 			return Collections.unmodifiableSet(_outer._command.alternatives());
 		}
 		public String id()
 		{
 			return _outer._command.id();
 		}
 		public List<String> description()
 		{
 			return _outer._command.description();
 		}
 		public String shortDescription()
 		{
 			return _outer._shortDescription;
 		}
 		public List<Option.Internal> cmdLineOptions()
 		{
 			return Collections.unmodifiableList(_outer._cmdLineOptions);
 		}
 		public List<Argument.Internal<?>> cmdLineArguments()
 		{
 			return Collections.unmodifiableList(_outer._cmdLineArguments);
 		}
 		public Map<String, Option.Internal>definedOptions()
 		{
 			return Collections.unmodifiableMap(_outer._definedOptions);
 		}
 		public Map<String, Argument.Internal<?>> definedArguments()
 		{
 			return Collections.unmodifiableMap(_outer._definedArguments);
 		}
 		
 		public boolean parse(List<String> arguments)
 			throws
 				ConstraintException,
 				IllegalCommandLineArgumentException
 		{
 			if(arguments.size() == 0)
 				return false;
 			
 			String commandName = arguments.remove(0);
 			Set<Option.Internal> nonMultipleOptionCheckSet = new HashSet<Option.Internal>();
 			boolean commandArgumentsChecked = false;
 			while(arguments.size() > 0) {
 				String arg = arguments.get(0);
 				if(
 					Util.checkOption(
 						arg,
 						arguments,
 						_outer._definedOptions,
 						_outer._definedOptionAlternatives,
 						nonMultipleOptionCheckSet,
 						_outer._cmdLineOptions
 					)
 				)
 					continue;
 				else
 					if(commandArgumentsChecked)
 						break;
 				
 				if(
 					Util.checkArguments(
 						commandName,
 						arguments,
 						_outer._definedArguments,
 						_outer._cmdLineArguments
 					)
 				) {
 					commandArgumentsChecked = true;
 					continue;
 				}
 				
 				return false;
 			}
 			
 			return true;
 		}
 		public void execute(List<Option.Data> options)
 		{
 			if(_outer._commandExecutor != null)
 				_outer._commandExecutor.execute(new Data(this), options);
 		}
 		
 		@Override
 		public boolean equals(Object obj)
 		{
 			if(obj == this)
 				return true;
 
 			if(!(obj instanceof Command.Internal))
 				return false;
 			Command.Internal internal = (Command.Internal)obj;
 			return name().equals(internal.name());
 		}
 
 		@Override
 		public int hashCode()
 		{
 			int resultHash = 17;
 			resultHash = 31 * resultHash + (name() == null ? 0 : name().hashCode());
 			return resultHash;
 		}
 		
 		@Override
 		public String toString()
 		{
 			String str = "{" + name() + "(" + id() + ")";
 			if(alternatives().size() > 0)
 				str += " = " + alternatives();
 			str += "}";
 			return str;
 		}
 		
 	}
 }
