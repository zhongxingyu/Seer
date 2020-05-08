 /**
 * Copyright 2009 Robert Ramírez
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package nl.flotsam.xeger;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(value = Parameterized.class)
 public class RegexGenerationSupport {
 
 	private String regex;
 	private boolean working;
 	private static final Logger logger = Logger
 		.getLogger(RegexGenerationSupport.class.getSimpleName());
 
 	public RegexGenerationSupport(boolean working, String regexToTest) {
 		this.regex = regexToTest;
 		this.working = working;
 	}
 
 	@Parameters
 	public static Collection<Object[]> data() {
 		Object[][] data = new Object[][] {
 			// Predefined character classes does not work
 			{ false, "\\d\\d" },
 			{ false, "\\d{3}" },
 			{ false, "\b(\\w+)\\s+\\1\b " },
 			// Supported elements from java api
  			{ true, "[ab]{4,6}c" },
 			{ true, "a|b" },
 			{ true, "[abc]" },
 			{ true, "[^abc]" },
 			{ true, "a+" },
 			{ true, "a*" },
 			{ true, "ab" },
 			{ true, "[a-zA-Z]" },
 			// union and intersection does not works
 			{ false, "[a-d[m-p]]" },
 			{ false, "[a-z&&[def]]" },
 			{ false, "[a-z&&[^bc]]" },
 			{ false, "[a-z&&[^m-p]]" },
 			{ true, "" },
 			{ true, "" },
 			{ true, "" },
 			{ true, "" },
 			};
 		return Arrays.asList(data);
 	}
 
 	@Test
 	public void shouldNotGenerateTextCorrectly() {
 		Xeger generator = new Xeger(regex);
 		for (int i = 0; i < 100; i++) {
 			String text = generator.generate();
 
 			if (logger.isLoggable(Level.INFO)) {
 				logger.log(Level.INFO,
 					"For pattern \"{0}\" \t\t, generated: \"{1}\"",
 					new Object[] { regex, text });
 			}
 
 			if (working) {
 				assertTrue("text generated: " + text + " does match regexp: "
 					+ regex, text.matches(regex));
 			} else {
 				assertFalse("text generated: |" + text
 					+ "| does match regexp: |" + regex + "|",
 					text.matches(regex));
 			}
 		}
 	}
 
 }
