 /* Copyright 2013 Jonatan Jönsson
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package se.softhouse.jargo;
 
 import java.util.Locale;
 
 import javax.annotation.CheckReturnValue;
 import javax.annotation.Nonnull;
 import javax.annotation.concurrent.Immutable;
 
 import se.softhouse.common.strings.Description;
 import se.softhouse.jargo.CommandLineParserInstance.ArgumentIterator;
 import se.softhouse.jargo.StringParsers.InternalStringParser;
 
 import com.google.common.base.Supplier;
 import com.google.common.base.Suppliers;
 import com.google.common.collect.ImmutableList;
 
 /**
  * <pre>
  * {@link Command}s automatically gets an invocation of execute when given on the command line.
  * This is particularly useful to avoid a never ending switch statement.
  * 
  * {@link Command}s have a {@link CommandLineParser} themselves (and thereby sub-commands are allowed as well), that is,
  * they execute a command and may support contextual arguments as specified by the constructor {@link Command#Command(Argument...)}.
  * 
  * Sub-commands are executed before their parent {@link Command}.
  * 
  * To integrate your {@link Command} into an {@link Argument} use {@link Arguments#command(Command)}
  * or {@link CommandLineParser#withCommands(Command...)} if you have several commands.
  * 
  * If you support several commands and a user enters several of them at the same
  * time they will be executed in the order given to {@link CommandLineParser#parse(String...)}.
  * If any {@link StringParser#parse(String, Locale) parse} errors occurs (for {@link Command#Command(Argument...) command arguments}) the {@link Command}
  * will not be executed. However, if given multiple commands and {@link StringParser#parse(String, Locale) parse errors} occurs,
  * all {@link Command}s given before the {@link Command} with {@link StringParser#parse(String, Locale) parse errors} will have been executed.
  * This is so because {@link Command#Command(Argument...) command arguments} are allowed to be dependent on earlier {@link Command}s being executed.
  * So it's recommended to let the user know when you've executed a {@link Command}.
  * 
  * <b>Mutability note:</b> although a {@link Command} should be {@link Immutable}
  * the objects it handles doesn't have to be. So repeated invocations of execute
  * is allowed to yield different results or to affect external state.
  * </pre>
  * 
  * Example subclass:
  * 
  * <pre class="prettyprint">
  * <code class="language-java">
  * public class BuildCommand extends Command
  * {
  *   private static final Argument&lt;File&gt; PATH = Arguments.fileArgument().description("the directory to build").build();
  * 
  *   public BuildCommand()
  *   {
  *     super(PATH);
  *   }
  * 
  *   public String commandName()
  *   {
  *     return "build";
  *   }
  * 
  *   public String description()
  *   {
  *     return "Builds a target";
  *   }
  * 
  *   protected void execute(ParsedArguments parsedArguments)
  *   {
  *     File pathToBuild = parsedArguments.get(PATH);
  *     //Build pathToBuild here
  *   }
  * }
  * </code>
  * </pre>
  * 
  * And the glue needed to integrate the BuildCommand with a {@link CommandLineParser}:
  * 
  * <pre class="prettyprint">
  * <code class="language-java">
  * CommandLineParser.withCommands(new BuildCommand()).parse("build", "some_directory_that_needs_building");
  * </code>
  * </pre>
  */
 @Immutable
 public abstract class Command extends InternalStringParser<ParsedArguments> implements Description
 {
 	@Nonnull private final ImmutableList<Argument<?>> commandArguments;
 
 	@Nonnull private final Supplier<CommandLineParserInstance> commandArgumentParser = Suppliers.memoize(new Supplier<CommandLineParserInstance>(){
 		@Override
 		public CommandLineParserInstance get()
 		{
 			return CommandLineParserInstance.createCommandParser(commandArguments);
 		}
 	});
 
 	/**
 	 * @param commandArguments the arguments that this command supports.
 	 */
 	protected Command(Argument<?> ... commandArguments)
 	{
 		this.commandArguments = ImmutableList.copyOf(commandArguments);
 	}
 
 	// TODO(jontejj): support commandArguments() by a method as well
	// TODO(jontejj): interface version of Command that's simpler and doesn't support arguments
	// Issues: the name isn't very clear and will people confuse it with Command?
 	/**
 	 * The name that triggers this command. For several names override this with
 	 * {@link ArgumentBuilder#names(String...)}
 	 */
 	@Nonnull
 	@CheckReturnValue
 	protected abstract String commandName();
 
 	/**
 	 * Called when this command is encountered on the command line
 	 * 
 	 * @param parsedArguments a container with parsed values for the command arguments,
 	 *            as specified by {@link Command#Command(Argument...)}
 	 */
 	protected abstract void execute(ParsedArguments parsedArguments);
 
 	/**
 	 * Override to provide a description to print in the usage text for this command.
 	 * This is essentially an alternative to {@link ArgumentBuilder#description(Description)}
 	 * 
 	 * @return the description to use in the usage text
 	 */
 	@Override
 	@Nonnull
 	public String description()
 	{
 		return "";
 	}
 
 	@Override
 	public String toString()
 	{
 		return commandName();
 	}
 
 	@Override
 	final ParsedArguments parse(final ArgumentIterator arguments, final ParsedArguments previousOccurance, final Argument<?> argumentSettings,
 			Locale locale) throws ArgumentException
 	{
 		arguments.rememberAsCommand();
 
 		ParsedArguments parsedArguments = parser().parse(arguments, locale);
 
 		arguments.rememberInvocationOfCommand(this, parsedArguments);
 
 		return parsedArguments;
 	}
 
 	/**
 	 * The parser for parsing the {@link Argument}s passed to {@link Command#Command(Argument...)}
 	 */
 	private CommandLineParserInstance parser()
 	{
 		return commandArgumentParser.get();
 	}
 
 	@Override
 	final ParsedArguments defaultValue()
 	{
 		return null;
 	}
 
 	@Override
 	final String descriptionOfValidValues(Argument<?> argumentSettings, Locale locale)
 	{
 		return parser().usage(locale).toString();
 	}
 
 	@Override
 	final String describeValue(ParsedArguments value)
 	{
 		return null;
 	}
 
 	@Override
 	final String metaDescription(Argument<?> argumentSettings)
 	{
 		return "";
 	}
 }
