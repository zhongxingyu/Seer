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
 package se.softhouse.jargo.concurrency;
 
 import static java.util.Arrays.asList;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.Fail.fail;
 import static se.softhouse.jargo.Arguments.bigDecimalArgument;
 import static se.softhouse.jargo.Arguments.bigIntegerArgument;
 import static se.softhouse.jargo.Arguments.booleanArgument;
 import static se.softhouse.jargo.Arguments.byteArgument;
 import static se.softhouse.jargo.Arguments.charArgument;
 import static se.softhouse.jargo.Arguments.enumArgument;
 import static se.softhouse.jargo.Arguments.fileArgument;
 import static se.softhouse.jargo.Arguments.integerArgument;
 import static se.softhouse.jargo.Arguments.longArgument;
 import static se.softhouse.jargo.Arguments.optionArgument;
 import static se.softhouse.jargo.Arguments.shortArgument;
 import static se.softhouse.jargo.Arguments.stringArgument;
 import static se.softhouse.jargo.stringparsers.custom.DateTimeParser.dateArgument;
 import static se.softhouse.jargo.utils.Assertions2.assertThat;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.fest.assertions.Description;
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import se.softhouse.jargo.Argument;
 import se.softhouse.jargo.CommandLineParser;
 import se.softhouse.jargo.ParsedArguments;
 import se.softhouse.jargo.Usage;
 import se.softhouse.jargo.stringparsers.EnumArgumentTest.Action;
 import se.softhouse.jargo.utils.ExpectedTexts;
 
 import com.google.common.base.Strings;
 
 public class ConcurrencyTest
 {
 	final Argument<Boolean> enableLoggingArgument = optionArgument("-l", "--enable-logging").description("Output debug information to standard out")
 			.build();
 
 	final Argument<Integer> port = integerArgument("-p", "--listen-port").required().description("The port to start the server on.").build();
 
 	final Argument<String> greetingPhraseArgument = stringArgument().required().description("A greeting phrase to greet new connections with")
 			.build();
 
 	final Argument<Long> longArgument = longArgument("--long").build();
 
 	final Argument<DateTime> date = dateArgument("--date").build();
 
 	final Argument<Short> shortArgument = shortArgument("--short").build();
 
 	final Argument<Byte> byteArgument = byteArgument("--byte").build();
 
 	final Argument<File> fileArgument = fileArgument("--file").defaultValueDescription("The current directory").build();
 
 	final Argument<String> string = stringArgument("--string").build();
 
 	final Argument<Character> charArgument = charArgument("--char").build();
 
 	final Argument<Boolean> boolArgument = booleanArgument("--bool").build();
 
 	final Argument<Map<String, Boolean>> propertyArgument = booleanArgument("-B").asPropertyMap().build();
 
 	final Argument<List<Boolean>> arityArgument = booleanArgument("--arity").arity(6).build();
 
 	final Argument<List<Integer>> repeatedArgument = integerArgument("--repeated").repeated().build();
 
 	final Argument<List<Integer>> splittedArgument = integerArgument("--split").separator("=").splitWith(",").build();
 
 	final Argument<Action> enumArgument = enumArgument(Action.class, "--enum").build();
 
 	final Argument<List<Integer>> variableArityArgument = integerArgument("--variableArity").variableArity().build();
 
 	final Argument<BigInteger> bigIntegerArgument = bigIntegerArgument("--big-integer").build();
 
 	final Argument<BigDecimal> bigDecimalArgument = bigDecimalArgument("--big-decimal").build();
 
 	// The shared instance that the different threads will use
 	final CommandLineParser parser = CommandLineParser
 			.withArguments(greetingPhraseArgument, enableLoggingArgument, port, longArgument, date, shortArgument, byteArgument, fileArgument,
 							string, charArgument, boolArgument, propertyArgument, arityArgument, repeatedArgument, splittedArgument, enumArgument,
 							variableArityArgument, bigIntegerArgument, bigDecimalArgument)
 			.programDescription("Example of most argument types that jargo can handle by default").locale(Locale.US);
 
 	final String expectedUsageText = ExpectedTexts.expected("allFeaturesInUsage");
 
 	// Amount of test harness
 	private static final int ITERATION_COUNT = 300;
 
 	private static final int RUNNERS_PER_PROCESSOR = 3; // We want the threads
 														// to fight for CPU time
 
 	private static final int nrOfConcurrentRunners = Runtime.getRuntime().availableProcessors() * RUNNERS_PER_PROCESSOR;
 
 	/**
 	 * Used by other threads to report failure
 	 */
 	private final AtomicReference<Throwable> failure = new AtomicReference<Throwable>(null);
 
 	private final CountDownLatch activeWorkers = new CountDownLatch(nrOfConcurrentRunners);
 	private final CyclicBarrier startup = new CyclicBarrier(nrOfConcurrentRunners);
 	private final CyclicBarrier parseDone = new CyclicBarrier(nrOfConcurrentRunners);
 
 	private static final int timeoutInSeconds = 60;
 	private static final int cleanupTime = 5;
 
 	@Test(timeout = (timeoutInSeconds + cleanupTime) * 1000)
 	public void testThatDifferentArgumentsCanBeParsedConcurrently() throws Throwable
 	{
 		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nrOfConcurrentRunners);
 		for(int i = 0; i < nrOfConcurrentRunners; i++)
 		{
 			executor.execute(new ArgumentParseRunner(i));
 		}
 
 		try
 		{
 			if(!activeWorkers.await(timeoutInSeconds, TimeUnit.SECONDS))
 			{
 				executor.shutdownNow();
 				fail("Timeout waiting for concurrency test to finish");
 			}
 		}
 		catch(InterruptedException e)
 		{
 			Thread.interrupted();
 			if(failure.get() != null)
 			{
 				executor.shutdownNow();
 				throw failure.get();
 			}
 		}
 		assertThat(executor.shutdownNow()).isEmpty();
 	}
 
 	private final class ArgumentParseRunner implements Runnable
 	{
 		private final int offset;
 		private final Thread originThread;
 		private ParsedArguments arguments;
 
 		public ArgumentParseRunner(int offset)
 		{
 			this.offset = offset;
 			originThread = Thread.currentThread();
 		}
 
 		@Override
 		public void run()
 		{
 			int portNumber = 8090 + offset;
 
 			String greetingPhrase = "Hello" + offset;
 			DateTime time = DateTime.parse(new DateTime("2010-01-01").plusMillis(offset).toString());
 			char c = charForOffset();
 			boolean bool = offset % 2 == 0;
 			String enableLogging = bool ? "-l " : "";
 			short shortNumber = (short) (1232 + offset);
 			byte byteNumber = (byte) (123 + offset);
 			long longNumber = 1234567890L + offset;
 			BigInteger bigInteger = BigInteger.valueOf(12312313212323L + offset);
 			String str = "FooBar" + offset;
 			String action = Action.values()[offset % Action.values().length].toString();
 
 			Map<String, Boolean> propertyMap = new HashMap<String, Boolean>();
 			propertyMap.put("foo" + offset, true);
 			propertyMap.put("bar", false);
 
 			int amountOfVariableArity = offset % 10;
 			String variableArityIntegers = Strings.repeat(" " + portNumber, amountOfVariableArity);
 			List<Boolean> arityBooleans = asList(bool, bool, bool, bool, bool, bool);
 			String arityString = Strings.repeat(" " + bool, 6);
 
 			String filename = "user_" + offset;
 			File file = new File(filename);
 			BigDecimal bigDecimal = BigDecimal.valueOf(Long.MAX_VALUE);
 
 			String inputArguments = enableLogging + "-p " + portNumber + " " + greetingPhrase + " --long " + longNumber + " --big-integer "
 					+ bigInteger + " --date " + time + " --short " + shortNumber + " --byte " + byteNumber + " --file " + filename + " --string "
 					+ str + " --char " + c + " --bool " + bool + " -Bfoo" + offset + "=true -Bbar=false" + " --arity" + arityString
 					+ " --repeated 1 --repeated " + offset + " --split=1," + (2 + offset) + ",3" + " --enum " + action + " --big-decimal "
 					+ bigDecimal + " --variableArity" + variableArityIntegers;
 
 			try
 			{
 				String[] args = inputArguments.split(" ");
 				// Let all threads prepare the input and start processing at the
 				// same time
 				startup.await(10, TimeUnit.SECONDS);
 
 				for(int i = 0; i < ITERATION_COUNT; i++)
 				{
 					arguments = parser.parse(args);
 
 					// Let all threads assert at the same time
 					parseDone.await(10, TimeUnit.SECONDS);
 
 					checkThat(enableLoggingArgument).received(bool);
 					checkThat(port).received(portNumber);
 					checkThat(greetingPhraseArgument).received(greetingPhrase);
 					checkThat(longArgument).received(longNumber);
 					checkThat(bigIntegerArgument).received(bigInteger);
 					checkThat(date).received(time);
 					checkThat(shortArgument).received(shortNumber);
 					checkThat(byteArgument).received(byteNumber);
 					checkThat(fileArgument).received(file);
 					checkThat(string).received(str);
 					checkThat(charArgument).received(c);
 					checkThat(boolArgument).received(bool);
 					checkThat(arityArgument).received(arityBooleans);
 					checkThat(repeatedArgument).received(asList(1, offset));
 					checkThat(splittedArgument).received(asList(1, 2 + offset, 3));
 					checkThat(propertyArgument).received(propertyMap);
 					checkThat(enumArgument).received(Action.valueOf(action));
 					checkThat(bigDecimalArgument).received(bigDecimal);
 					assertThat(arguments.get(variableArityArgument)).hasSize(amountOfVariableArity);
 
 					if(i % 10 == 0) // As usage is expensive to create only test this sometimes
 					{
 						Usage usage = parser.usage();
 						assertThat(usage).isEqualTo(expectedUsageText);
 					}
 				}
 			}
 			catch(Throwable e)
 			{
 				// Don't report secondary failures
 				if(failure.compareAndSet(null, e))
 				{
 					originThread.interrupt();
 				}
 				return;
 			}
 			activeWorkers.countDown();
 		}
 
 		private char charForOffset()
 		{
 			char c = (char) (offset % Character.MAX_VALUE);
			// char charToReceive = c == ' ' ? '.' : c; // A space would be trimmed to nothing
			return c;
 		}
 
 		/**
 		 * Verifies that an argument received an expected value
 		 */
 		public <T> Checker<T> checkThat(Argument<T> argument)
 		{
 			return new Checker<T>(argument);
 		}
 
 		private class Checker<T>
 		{
 			Argument<T> arg;
 
 			public Checker(Argument<T> argument)
 			{
 				arg = argument;
 			}
 
 			public void received(final T expectation)
 			{
 				final T parsedValue = arguments.get(arg);
 				Description description = new Description(){
 					// In a concurrency test it makes a big performance difference
 					// with lazily created descriptions
 					@Override
 					public String value()
 					{
 						return "Failed to match: " + arg + ", actual: " + parsedValue + ", expected: " + expectation;
 					}
 				};
 				assertThat(parsedValue).as(description).isEqualTo(expectation);
 			}
 		}
 	}
 }
