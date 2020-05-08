 /**
  * Copyright (c) 2011, Mikael Svahn, Softhouse Consulting AB
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so:
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package se.softhouse.garden.orchid.text;
 
 import static se.softhouse.garden.orchid.commons.text.OrchidMessage.arg;
 import static se.softhouse.garden.orchid.commons.text.OrchidMessage.args;
 import static se.softhouse.garden.orchid.commons.text.OrchidMessage.func;
 
import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Locale;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import se.softhouse.garden.orchid.commons.text.OrchidMessageArgumentCode;
 import se.softhouse.garden.orchid.commons.text.OrchidMessageArguments;
 import se.softhouse.garden.orchid.commons.text.OrchidMessageCode;
 import se.softhouse.garden.orchid.commons.text.OrchidMessageFormat;
 import se.softhouse.garden.orchid.commons.text.OrchidMessageFormatFunction;
 import se.softhouse.garden.orchid.commons.text.OrchidMessageFormatFunctionExecutor;
 
 public class TestOrchidMessageFormat {
 
 	@Test
 	public void testEnum() {
 		Assert.assertEquals("Test message 1", OrchidMessageFormat.format("Test message {id}", arg(TestArguments.ID, 1)));
 		Assert.assertEquals("Test message 002 with name Micke",
 		        OrchidMessageFormat.format("Test message {id,number,000} with name {name}", arg(TestArguments.NAME, "Micke").arg(TestArguments.ID, 2)));
 		Assert.assertEquals("Test message", MessageFormat.format("Test message", (Object[]) null));
 		Assert.assertEquals("Test message 1", OrchidMessageFormat.format(TestMessages.MSG1, arg(TestArguments.ID, 1)));
 	}
 
 	@Test
 	public void testString() {
 		Assert.assertEquals("Test message 1", OrchidMessageFormat.format("Test message {id}", arg("id", 1)));
 		Assert.assertEquals("Test message 002 with name Micke",
 		        OrchidMessageFormat.format("Test message {id,number,000} with name {name}", arg("name", "Micke").arg("id", 2)));
 		Assert.assertEquals("Test message", MessageFormat.format("Test message", (Object[]) null));
 		Assert.assertEquals("Test message 1", OrchidMessageFormat.format(TestMessages.MSG1, arg("id", 1)));
 		Assert.assertEquals("Test message 1", OrchidMessageFormat.format("Test message {0}", 1));
 		Assert.assertEquals("Test message 1", OrchidMessageFormat.format("Test message {0}", arg("0", 1)));
 		OrchidMessageFormat omf = new OrchidMessageFormat("Test message {0}");
 		Assert.assertEquals("Test message 1", omf.format(new Object[] { 1 }));
 		Assert.assertEquals("Test message 1", omf.format(arg("0", 1)));
 		Assert.assertEquals("Test message null", OrchidMessageFormat.format("Test message {id}", args()));
 	}
 
 	@Test
 	public void testFunction() {
 		Assert.assertEquals("Test message {id}", OrchidMessageFormat.format("Test message {m:id}", arg("id", 1)));
 		Assert.assertEquals("Test message {x:id}", OrchidMessageFormat.format("Test message {x:id}", arg("id", 1)));
 		Assert.assertEquals("Test message test-id", OrchidMessageFormat.format("Test message {test:id}", args().func("test", new TestFunction())));
 		Assert.assertEquals("Test message test-id", OrchidMessageFormat.format("Test message {test:id}", func("test", new TestFunction())));
 	}
 
	@Test
	public void testEscapedMessage() throws IOException {
		Assert.assertEquals("L'importo da {min} {currency} e {max} {currency}.",
		        OrchidMessageFormat.format("L'''importo da {min} {currency} e {max} {currency}.", arg("id", 1)));
	}

	@Test
	public void testEmptyMessage() throws IOException {
		Assert.assertEquals("", OrchidMessageFormat.format("", arg("id", 1)));
	}

 	public static enum TestMessages implements OrchidMessageCode {
 		MSG0("Test message"), //
 		MSG1("Test message {id}"), //
 		MSG2("Test message {id,number,000} with name {name}"), //
 		MSG_3("Test message {id,number,000} with name {name}"), //
 		MSG4("Missed code"), //
 		LOCAL_1("code1"), //
 		LOCAL_2("code2"), //
 		LOCAL_3("code3"), //
 		LOCAL_4("code3"), //
 		EMBEDDED_0("embedded message"), //
 		EMBEDDED_10("XXX"), //
 		EMBEDDED_20("embedded message"), //
 		;
 
 		private final String realName;
 		private final String pattern;
 
 		private TestMessages(String pattern) {
 			this.pattern = pattern;
 			this.realName = name().toLowerCase().replaceAll("_", ".");
 		}
 
 		@Override
 		public String getPattern() {
 			return this.pattern;
 		}
 
 		@Override
 		public String getRealName() {
 			return this.realName;
 		}
 	}
 
 	public enum TestArguments implements OrchidMessageArgumentCode {
 		ID, NAME;
 
 		@Override
 		public String getRealName() {
 			return name().toLowerCase();
 		}
 	}
 
 	public class TestFunction implements OrchidMessageFormatFunction {
 
 		@Override
 		public Object execute(OrchidMessageFormatFunctionExecutor executor, OrchidMessageArguments args, Locale locale) {
 			return executor.getFunction() + "-" + executor.getValue();
 		}
 
 	}
 }
