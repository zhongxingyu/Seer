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
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.Fail.fail;
 import static se.softhouse.common.strings.StringsUtil.NEWLINE;
 import static se.softhouse.jargo.Arguments.integerArgument;
 import static se.softhouse.jargo.Arguments.stringArgument;
 import static se.softhouse.jargo.utils.Assertions2.assertThat;
 import static se.softhouse.jargo.utils.ExpectedTexts.expected;
 
 import org.fest.assertions.Fail;
 import org.junit.Test;
 
 import se.softhouse.common.classes.Classes;
 import se.softhouse.common.strings.Description;
 import se.softhouse.jargo.ArgumentExceptions.UnexpectedArgumentException;
 import se.softhouse.jargo.internal.Texts.ProgrammaticErrors;
 import se.softhouse.jargo.internal.Texts.UsageTexts;
 import se.softhouse.jargo.internal.Texts.UserErrors;
 
 /**
  * Tests for {@link CommandLineParser#usage()}, {@link Argument#usage()} and
  * {@link ArgumentBuilder#usage()}
  * 
  * @formatter:off
  */
 public class UsageTest
 {
 	@Test
 	public void testThatProgramNameDefaultsToMainClassName()
 	{
 		Usage usage = integerArgument("-n").usage();
 		assertThat(usage).startsWith(UsageTexts.USAGE_HEADER + Classes.mainClassName());
 	}
 
 	@Test
 	public void testUsageWithRequiredArguments()
 	{
 		Usage usage = integerArgument("-n").required().usage();
 		assertThat(usage).contains(UsageTexts.REQUIRED);
 	}
 
 	@Test
 	public void testUsageWithRepeatedArguments()
 	{
 		Usage usage = stringArgument("-s").repeated().metaDescription("\"greeting phrase\"")
 				.description("A greeting phrase to greet new connections with").defaultValueDescription("Nothing").usage();
 
 		assertThat(usage).isEqualTo(expected("repeatedArguments"));
 	}
 
 	@Test
 	public void testUsageForNoArguments()
 	{
 		Usage usage = CommandLineParser.withArguments().programName("NoArguments").usage();
 		assertThat(usage).isEqualTo("Usage: NoArguments" + NEWLINE);
 	}
 
 	@Test
 	public void testUsageForNoVisibleArguments()
 	{
 		Usage usage = CommandLineParser.withArguments(integerArgument().hideFromUsage().build()).programName("NoVisibleArguments").usage();
 		assertThat(usage).isEqualTo("Usage: NoVisibleArguments" + NEWLINE);
 	}
 
 	@Test
 	public void testUsageWithArguments()
 	{
 		Usage usage = stringArgument().usage();
 		assertThat(usage).startsWith(UsageTexts.USAGE_HEADER).contains(UsageTexts.ARGUMENT_INDICATOR).contains(UsageTexts.ARGUMENT_HEADER);
 	}
 
 	@Test
 	public void testThatHiddenArgumentsAreHidden()
 	{
 		Argument<String> hiddenArgument = stringArgument("--hidden-argument").hideFromUsage().build();
 		Argument<String> visibleArgument = stringArgument("--visible-argument").build();
 		CommandLineParser parser = CommandLineParser.withArguments(hiddenArgument, visibleArgument);
 		Usage usage = parser.usage();
 
 		assertThat(usage).doesNotContain("--hidden-argument");
 		assertThat(usage).contains("--visible-argument");
 	}
 
 	@Test
 	public void testThatHiddenArgumentsAreParsable() throws ArgumentException
 	{
 		assertThat(stringArgument("--hidden").hideFromUsage().parse("--hidden", "hello")).isEqualTo("hello");
 	}
 
 	@Test
 	public void testUsageTextForRepeatedArgumentWithDefaultValueSet()
 	{
 		Usage usage = integerArgument().defaultValue(1).repeated().usage();
 		assertThat(usage).contains(UsageTexts.DEFAULT_VALUE_START + "1").contains(UsageTexts.ALLOWS_REPETITIONS);
 	}
 
 	@Test
 	public void testArgumentNameSuggestions()
 	{
 		try
 		{
 			CommandLineParser.withArguments(integerArgument("--name").build(), integerArgument("--number").build(),
 											integerArgument("--nothing").build()).parse("--namr");
 			fail("--namr should not be a valid argument");
 		}
 		catch(ArgumentException expected)
 		{
			assertThat(expected).hasMessage(String.format(UserErrors.SUGGESTION, "--namr", "--name\n\t--number"));
 		}
 	}
 
 	@Test
 	public void testThatUsageOnArgumentExceptionThrowsWhenNoUsageIsAvailable()
 	{
 		try
 		{
 			throw ArgumentExceptions.withMessage("");
 		}
 		catch(ArgumentException e)
 		{
 			try
 			{
 				e.getMessageAndUsage();
 				fail("getMessageAndUsage should throw when not enough information is available to produce a sane usage text");
 			}
 			catch(NullPointerException expected)
 			{
 				assertThat(expected).hasMessage(ProgrammaticErrors.NO_USAGE_AVAILABLE);
 			}
 		}
 	}
 
 	@Test
 	public void testSortingOrderForIndexedArguments()
 	{
 		Argument<String> indexOne = stringArgument().description("IndexOne").build();
 		Argument<String> indexTwo = stringArgument().description("IndexTwo").build();
 		Argument<String> indexThree = stringArgument().description("IndexThree").build();
 		Argument<String> namedOne = stringArgument("-S").build();
 		Argument<String> namedTwo = stringArgument("-T").build();
 		Usage usage = CommandLineParser.withArguments(indexOne, indexTwo, namedOne, indexThree, namedTwo).usage();
 
 		assertThat(usage).isEqualTo(expected("indexedArgumentsSortingOrder"));
 	}
 
 	@Test
 	public void testUnexpectedArgument() throws ArgumentException
 	{
 		try
 		{
 			integerArgument("--number").parse("--number", "1", "foo");
 			fail("foo should cause a throw as it's not handled");
 		}
 		catch(UnexpectedArgumentException e)
 		{
 
 			assertThat(e).hasMessage("Unexpected argument: foo, previous argument: 1");
 		}
 	}
 
 	@Test
 	public void testUnexpectedArgumentWithoutPreviousArgument() throws ArgumentException
 	{
 		try
 		{
 			integerArgument("--number").parse("foo");
 			fail("foo should cause a throw as it's not handled");
 		}
 		catch(UnexpectedArgumentException e)
 		{
 			assertThat(e).hasMessage("Unexpected argument: foo");
 		}
 	}
 
 	@Test
 	public void testProgramDescriptionInUsage()
 	{
 		Usage usage = CommandLineParser.withArguments().programName("ProgramName").programDescription("Program description of ProgramName").usage();
 
 		assertThat(usage).isEqualTo("Usage: ProgramName" + NEWLINE + NEWLINE + "Program description of ProgramName" + NEWLINE);
 	}
 
 	@Test
 	public void testThatDescriptionsAreLazilyInitialized()
 	{
 		Usage usage = integerArgument("-n").description(new Description(){
 			@Override
 			public String description()
 			{
 				return "foo";
 			}
 		}).usage();
 		assertThat(usage).contains("foo");
 	}
 
 	@Test
 	public void testThatDescriptionsAreNotLazilyInitializedWhenNotNeeded()
 	{
 		try
 		{
 			integerArgument("-n").description(new FailingDescription()).parse("-n", "foo");
 			fail("foo should cause a throw as it's an invalid integer");
 		}
 		catch(ArgumentException expected)
 		{
 		}
 	}
 
 	private static final class FailingDescription implements Description
 	{
 		@Override
 		public String description()
 		{
 			Fail.fail("Description should not be called as no usage was printed");
 			return "";
 		}
 
 	}
 }
