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
 
 import static com.google.common.base.Predicates.not;
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.ImmutableList.copyOf;
 import static com.google.common.collect.Lists.newArrayList;
 import static java.lang.Math.max;
 import static se.softhouse.common.strings.StringsUtil.NEWLINE;
 import static se.softhouse.common.strings.StringsUtil.spaces;
 import static se.softhouse.jargo.Argument.IS_INDEXED;
 import static se.softhouse.jargo.Argument.IS_OF_VARIABLE_ARITY;
 import static se.softhouse.jargo.Argument.IS_VISIBLE;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import javax.annotation.concurrent.NotThreadSafe;
 
 import se.softhouse.common.strings.StringBuilders;
 import se.softhouse.jargo.internal.Texts.UsageTexts;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 
 /**
  * Responsible for formatting usage texts for {@link CommandLineParser#usage()} and
  * {@link Arguments#helpArgument(String, String...)}.
  * Using it is often as simple as:
  * 
  * <pre class="prettyprint">
  * <code class="language-java">
  * System.out.print(CommandLineParser.withArguments(someArguments).usage());
  * </code>
  * </pre>
  * 
  * <pre>
  * Sorts {@link Argument}s in the following order:
  * <ol>
  *   <li>{@link ArgumentBuilder#names(String...) indexed arguments} without a {@link ArgumentBuilder#variableArity() variable arity}</li>
 * 	 <li>By their {@link ArgumentBuilder#names(String...) first name}  in a <a href="http://weblog.masukomi.org/2007/12/10/alphabetical-asciibetical">alphabetical</a> order</li>
  * 	 <li>The remaining {@link ArgumentBuilder#names(String...) indexed arguments} that are of {@link ArgumentBuilder#variableArity() variable arity}</li>
  * </ol>
  * </pre>
  */
 @NotThreadSafe
 public final class Usage
 {
 	private static final int CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION = 40;
 	private static final int SPACES_BETWEEN_COLUMNS = 4;
 	private static final Joiner NAME_JOINER = Joiner.on(UsageTexts.NAME_SEPARATOR);
 
 	private final Collection<Argument<?>> unfilteredArguments;
 
 	private ImmutableList<Argument<?>> argumentsToPrint;
 	private final Locale locale;
 	private final ProgramInformation program;
 	private final boolean forCommand;
 	/**
 	 * <pre>
 	 * For:
 	 * -l, --enable-logging Output debug information to standard out
 	 * -p, --listen-port    The port clients should connect to.
 	 * 
 	 * This would be 20.
 	 * </pre>
 	 */
 	private int indexOfDescriptionColumn;
 	private boolean needsNewline = false;
 	private StringBuilder builder = null;
 
 	Usage(Collection<Argument<?>> arguments, Locale locale, ProgramInformation program, boolean forCommand)
 	{
 		this.unfilteredArguments = arguments;
 		this.locale = locale;
 		this.program = program;
 		this.forCommand = forCommand;
 	}
 
 	private void init()
 	{
 		if(argumentsToPrint == null)
 		{
 			Collection<Argument<?>> visibleArguments = filter(unfilteredArguments, IS_VISIBLE);
 			this.argumentsToPrint = copyOf(sortedArguments(visibleArguments));
 			this.indexOfDescriptionColumn = determineLongestNameColumn() + SPACES_BETWEEN_COLUMNS;
 		}
 	}
 
 	private Iterable<Argument<?>> sortedArguments(Collection<Argument<?>> arguments)
 	{
 		Collection<Argument<?>> indexedArguments = filter(arguments, IS_INDEXED);
 		Iterable<Argument<?>> indexedWithoutVariableArity = filter(indexedArguments, not(IS_OF_VARIABLE_ARITY));
 		Iterable<Argument<?>> indexedWithVariableArity = filter(indexedArguments, IS_OF_VARIABLE_ARITY);
 
 		List<Argument<?>> sortedArgumentsByName = newArrayList(filter(arguments, not(IS_INDEXED)));
 		Collections.sort(sortedArgumentsByName, Argument.NAME_COMPARATOR);
 
 		return Iterables.concat(indexedWithoutVariableArity, sortedArgumentsByName, indexedWithVariableArity);
 	}
 
 	private int determineLongestNameColumn()
 	{
 		int longestNameSoFar = 0;
 		for(Argument<?> arg : argumentsToPrint)
 		{
 			longestNameSoFar = max(longestNameSoFar, lengthOfNameColumn(arg));
 		}
 		return longestNameSoFar;
 	}
 
 	private static int lengthOfNameColumn(final Argument<?> argument)
 	{
 		int namesLength = 0;
 
 		for(String name : argument.names())
 		{
 			namesLength += name.length();
 		}
 		int separatorLength = max(0, UsageTexts.NAME_SEPARATOR.length() * (argument.names().size() - 1));
 
 		int metaLength = argument.metaDescriptionInLeftColumn().length();
 
 		return namesLength + separatorLength + metaLength;
 	}
 
 	private String buildArgumentDescriptions()
 	{
 		builder = newStringBuilder();
 		for(Argument<?> arg : argumentsToPrint)
 		{
 			usageForArgument(arg);
 		}
 		return builder.toString();
 	}
 
 	private StringBuilder newStringBuilder()
 	{
 		// Two lines for each argument
 		return StringBuilders.withExpectedSize(2 * argumentsToPrint.size() * (indexOfDescriptionColumn + CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION));
 	}
 
 	/**
 	 * Returns the usage text that's suitable to print on {@link System#out}.
 	 */
 	@Override
 	public String toString()
 	{
 		init();
 		String header = "";
 		if(forCommand)
 		{
 			header = commandUsage();
 		}
 		else
 		{
 			header = mainUsage();
 		}
 		return header + buildArgumentDescriptions();
 	}
 
 	private String mainUsage()
 	{
 		String mainUsage = UsageTexts.USAGE_HEADER + program.programName();
 
 		if(hasArguments())
 		{
 			mainUsage += UsageTexts.ARGUMENT_INDICATOR;
 		}
 
 		mainUsage += NEWLINE + program.programDescription();
 
 		if(hasArguments())
 		{
 			mainUsage += NEWLINE + UsageTexts.ARGUMENT_HEADER + NEWLINE;
 		}
 
 		return mainUsage;
 	}
 
 	private String commandUsage()
 	{
 		return hasArguments() ? UsageTexts.ARGUMENT_HEADER + NEWLINE : "";
 	}
 
 	private boolean hasArguments()
 	{
 		return !argumentsToPrint.isEmpty();
 	}
 
 	/**
 	 * <pre>
 	 * 	-foo   Foo something [Required]
 	 *         	Valid values: 1 to 5
 	 *        -bar   Bar something
 	 *         	Default: 0
 	 * </pre>
 	 */
 	private void usageForArgument(final Argument<?> arg)
 	{
 		int lengthBeforeCurrentArgument = builder.length();
 
 		NAME_JOINER.appendTo(builder, arg.names());
 
 		builder.append(arg.metaDescriptionInLeftColumn());
 
 		int lengthOfFirstColumn = builder.length() - lengthBeforeCurrentArgument;
 		builder.append(spaces(indexOfDescriptionColumn - lengthOfFirstColumn));
 
 		String description = arg.description();
 		if(!description.isEmpty())
 		{
 			builder.append(description);
 			addIndicators(arg);
 			needsNewline = true;
 			newlineWithIndentation();
 			valueExplanation(arg);
 		}
 		else
 		{
 			valueExplanation(arg);
 			addIndicators(arg);
 		}
 		builder.append(NEWLINE);
 		needsNewline = false;
 	}
 
 	private void newlineWithIndentation()
 	{
 		if(needsNewline)
 		{
 			builder.append(NEWLINE);
 			builder.append(spaces(indexOfDescriptionColumn));
 			needsNewline = false;
 		}
 	}
 
 	private <T> void addIndicators(final Argument<T> arg)
 	{
 		if(arg.isRequired())
 		{
 			builder.append(UsageTexts.REQUIRED);
 		}
 		if(arg.isAllowedToRepeat())
 		{
 			builder.append(UsageTexts.ALLOWS_REPETITIONS);
 		}
 	}
 
 	private <T> void valueExplanation(final Argument<T> arg)
 	{
 		String description = arg.descriptionOfValidValues(locale);
 		if(!description.isEmpty())
 		{
 			boolean isCommand = arg.parser() instanceof Command;
 			if(isCommand)
 			{
 				// For commands the validValues is a usage text itself for the command arguments
 				// +1 = indentation so that command options are tucked under the command
 				String spaces = spaces(indexOfDescriptionColumn + 1);
 				description = description.replace(NEWLINE, NEWLINE + spaces);
 			}
 			else
 			{
 				String meta = arg.metaDescriptionInRightColumn();
 				builder.append(meta + ": ");
 			}
 
 			builder.append(description);
 			needsNewline = true;
 		}
 		if(arg.isRequired())
 			return;
 
 		String descriptionOfDefaultValue = arg.defaultValueDescription(locale);
 		if(descriptionOfDefaultValue != null)
 		{
 			newlineWithIndentation();
 			String spaces = spaces(indexOfDescriptionColumn + UsageTexts.DEFAULT_VALUE_START.length());
 			descriptionOfDefaultValue = descriptionOfDefaultValue.replace(NEWLINE, NEWLINE + spaces);
 
 			builder.append(UsageTexts.DEFAULT_VALUE_START).append(descriptionOfDefaultValue);
 		}
 	}
 }
