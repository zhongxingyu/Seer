 // vim:filetype=java:ts=4
 /*
 	Copyright (c) 2006, 2007
 	Conor McDermottroe.  All rights reserved.
 
 	Redistribution and use in source and binary forms, with or without
 	modification, are permitted provided that the following conditions
 	are met:
 	1. Redistributions of source code must retain the above copyright
 	   notice, this list of conditions and the following disclaimer.
 	2. Redistributions in binary form must reproduce the above copyright
 	   notice, this list of conditions and the following disclaimer in the
 	   documentation and/or other materials provided with the distribution.
 	3. Neither the name of the author nor the names of any contributors to
 	   the software may be used to endorse or promote products derived from
 	   this software without specific prior written permission.
 
 	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 	"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 	HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 	TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 	OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 	OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package junit.com.mcdermottroe.exemplar.ui.ant;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.tools.ant.BuildException;
 
 import com.mcdermottroe.exemplar.ui.Options;
 import com.mcdermottroe.exemplar.ui.ant.Task;
 import com.mcdermottroe.exemplar.utils.Strings;
 
 import static com.mcdermottroe.exemplar.Constants.Character.MINUS;
 import static com.mcdermottroe.exemplar.Constants.Character.UNDERSCORE;
 import static com.mcdermottroe.exemplar.Constants.UI.Ant.SETTER_PREFIX;
 
 import junit.com.mcdermottroe.exemplar.NormalClassTestCase;
 
 /** Test class for {@link Task}.
 
 	@author	Conor McDermottroe
 	@since	0.1
 */
 public class TaskTest
 extends NormalClassTestCase<Task>
 {
 	/** {@inheritDoc} */
 	@Override public void setUp()
 	throws Exception
 	{
 		super.setUp();
 
 		addSample(new Task());
 	}
 
 	/**	Ensure that all of the options have a method and all of the methods
 		here correspond to an option. This must be done to ensure that the
 		options definition file and this Task are in sync. Unfortuantely, Ant is
 		not smart enough to allow dynamically defined tasks.
 
 		<p>The algorithm is as follows:
 	 		<ol>
 				<li>Get the {@link Set} of options defined in the master options
 					definition.
 				</li>
 				<li>For each method declared in the task of the form "set*", if
 					the method name is in the set, remove it, if not fail on the
 					"extra method" failing case.
 				</li>
 	 			<li>After step 2 has completed, if the set is non-empty, then
 					all of the method names in the set correspond to "missing
 					methods".
 				</li>
 			 </ol>
 		</p>
 	*/
 	public void testAntInSyncWithOptions() {
 		Method[] methods = testedClass.getDeclaredMethods();
 		Set<String> defOptions = Options.getOptionNames();
 		for (Method method : methods) {
 			String methodName = method.getName();
 			if (methodName.startsWith(SETTER_PREFIX)) {
 				// Convert the method name to an option name
 				String optionName = methodName.substring(
 					SETTER_PREFIX.length()
 				);
 				optionName = optionName.toLowerCase();
 				optionName = optionName.replace(UNDERSCORE, MINUS);
 
 				// Now check it.
 				if (defOptions.contains(optionName)) {
 					defOptions.remove(optionName);
 				} else {
 					fail("Extra method found: " + methodName);
 				}
 			}
 		}
 		for (String defOption : defOptions) {
 			String methodName =	SETTER_PREFIX +
 								Strings.upperCaseFirst(defOption);
 			methodName = methodName.replace(MINUS, UNDERSCORE);
 			fail("Missing method: " + methodName);
 		}
 	}
 
 	/** Test {@link Task#execute()}. */
 	public void testExecute() {
 		boolean fellThrough = false;
 
 		Options.reset();
 		try {
 			new Task().execute();
 			fellThrough = true;
 		} catch (BuildException e) {
 			assertNotNull("BuildException was null", e);
 		} catch (AssertionError e) {
 			assertNotNull("AssertionError was null", e);
 			fail("execute() failed an assertion");
 		}
 		assertFalse("Fell through.", fellThrough);
 
 		Options.reset();
 		File outputDir = new File(TMP, getClass().getName());
 		outputDir.mkdirs();
 		assertTrue("Output directory does not exist", outputDir.exists());
 		try {
 			Task task = new Task();
 			task.setDebug("true");
 			task.setInput("/dev/null");
 			task.setOutput(outputDir.getAbsolutePath());
 			task.setOutput_language("dtd");
 			task.execute();
 		} catch (BuildException e) {
 			assertNotNull("BuildException was null", e);
 			fail("Task.execute() threw a BuildException");
 		} catch (AssertionError e) {
 			assertNotNull("AssertionError was null", e);
 			fail("Task.execute() failed an assertion");
 		}
 		for (String fileName : outputDir.list()) {
 			new File(outputDir, fileName).delete();
 		}
 		outputDir.delete();
 		assertFalse("Did not delete output directory", outputDir.exists());
 	}
 
 	/** Test {@link Task#setDebug(String)}. */
 	public void testSetDebugTrue() {
 		Task.setDebug("true");
 		assertTrue("Options.isDebugSet() returned false", Options.isDebugSet());
 		assertNotNull(
 			"Options.getBoolean(\"debug\") == null",
 			Options.getBoolean("debug")
 		);
 		assertTrue(
 			"Options.getBoolean(\"debug\") == false",
 			Options.getBoolean("debug")
 		);
 	}
 
 	/** Test {@link Task#setDebug(String)}. */
 	public void testSetDebugFalse() {
 		Task.setDebug("false");
 		assertFalse("Options.isDebugSet() returned true", Options.isDebugSet());
 		assertNotNull(
 			"Options.getBoolean(\"debug\") == null",
 			Options.getBoolean("debug")
 		);
 		assertFalse(
 			"Options.getBoolean(\"debug\") == true",
 			Options.getBoolean("debug")
 		);
 	}
 
 
 	/** Test {@link Task#setDebug_level(String)}. */
 	public void testSetDebug_level() {
 		Task.setDebug_level("2");
 		assertNotNull(
 			"Options.getString(\"debug-level\") == null",
 			Options.getString("debug-level")
 		);
 		assertEquals(
 			"Options.getString(\"debug-level\") != \"2\"",
 			2,
 			(int)Options.getInteger("debug-level")
 		);
 	}
 
 	/** Test {@link Task#setExclude(String)}. */
 	public void testSetExclude() {
 		Map<String, String> enumDescs = Options.getEnumDescriptions("exclude");
 		for (String enumValue : enumDescs.keySet()) {
 			Task.setExclude(enumValue);
 			assertTrue(
 				"Options.isSet(\"exclude\", \"" + enumValue + "\") == false",
 				Options.isSet("exclude", enumValue)
 			);
 		}
 	}
 
 	/** Test {@link Task#setHelp(String)}. */
 	public void testSetHelpTrue() {
 		Task.setHelp("true");
 		assertNotNull(
 			"Options.getBoolean(\"help\") == null",
 			Options.getBoolean("help")
 		);
 		assertTrue(
 			"Options.getBoolean(\"help\") == false",
 			Options.getBoolean("help")
 		);
 	}
 
 	/** Test {@link Task#setHelp(String)}. */
 	public void testSetHelpFalse() {
 		Task.setHelp("false");
 		assertNotNull(
 			"Options.getBoolean(\"help\") == null",
 			Options.getBoolean("help")
 		);
 		assertFalse(
 			"Options.getBoolean(\"help\") == true",
 			Options.getBoolean("help")
 		);
 	}
 
 	/** Test {@link Task#setExclude(String)}. */
 	public void testSetInclude() {
 		Map<String, String> enumDescs = Options.getEnumDescriptions("include");
 		for (String enumValue : enumDescs.keySet()) {
 			Task.setInclude(enumValue);
 			assertTrue(
 				"Options.isSet(\"include\", \"" + enumValue + "\") == false",
 				Options.isSet("include", enumValue)
 			);
 		}
 	}
 
 	/** Test {@link Task#setInput(String)}. */
 	public void testSetInput() {
 		Task.setInput("foo");
 		assertNotNull(
 			"Options.getString(\"input\") == null",
 			Options.getString("input")
 		);
 		assertEquals(
 			"Options.getString(\"input\") != \"foo\"",
 			"foo",
 			Options.getString("input")
 		);
 	}
 
 	/** Test {@link Task#setInput_type(String)}. */
 	public void testSetInput_type() {
 		Task.setInput_type("dtd");
 		assertNotNull(
 			"Options.getString(\"input-type\") == null",
 			Options.getString("input-type")
 		);
 		assertEquals(
 			"Options.getString(\"input-type\") != \"dtd\"",
 			"dtd",
 			Options.getString("input-type")
 		);
 	}
 
 	/** Test {@link Task#setOutput(String)}. */
 	public void testSetOutput() {
 		Task.setOutput("foo");
 		assertNotNull(
 			"Options.getString(\"output\") == null",
 			Options.getString("output")
 		);
 		assertEquals(
 			"Options.getString(\"output\") != \"foo\"",
 			"foo",
 			Options.getString("output")
 		);
 	}
 
 	/** Test {@link Task#setOutput_api(String)}. */
 	public void testSetOutput_api() {
 		Task.setOutput_api("sax1");
 		assertNotNull(
 			"Options.getString(\"output-api\") == null",
 			Options.getString("output-api")
 		);
 		assertEquals(
 			"Options.getString(\"output-api\") != \"sax1\"",
 			"sax1",
 			Options.getString("output-api")
 		);
 	}
 
 	/** Test {@link Task#setOutput_language(String)}. */
 	public void testSetOutput_language() {
 		Task.setOutput_language("java");
 		assertNotNull(
 			"Options.getString(\"output-language\") == null",
 			Options.getString("output-language")
 		);
 		assertEquals(
 			"Options.getString(\"output-language\") != \"java\"",
 			"java",
 			Options.getString("output-language")
 		);
 	}
 
 	/** Test {@link Task#setOutput_package(String)}. */
 	public void testSetOutput_package() {
 		Task.setOutput_package("foo");
 		assertNotNull(
 			"Options.getString(\"output-package\") == null",
 			Options.getString("output-package")
 		);
 		assertEquals(
 			"Options.getString(\"output-package\") != \"foo\"",
 			"foo",
 			Options.getString("output-package")
 		);
 	}
 
 	/** Test {@link Task#setVerbose(String)}. */
 	public void testSetVerboseTrue() {
 		Task.setVerbose("true");
 		assertNotNull(
 			"Options.getBoolean(\"verbose\") == null",
 			Options.getBoolean("verbose")
 		);
 		assertTrue(
 			"Options.getBoolean(\"verbose\") == false",
 			Options.getBoolean("verbose")
 		);
 	}
 
 	/** Test {@link Task#setVerbose(String)}. */
 	public void testSetVerboseFalse() {
 		Task.setVerbose("false");
 		assertNotNull(
 			"Options.getBoolean(\"verbose\") == null",
 			Options.getBoolean("verbose")
 		);
 		assertFalse(
 			"Options.getBoolean(\"verbose\") == true",
 			Options.getBoolean("verbose")
 		);
 	}
 
 	/** Test {@link Task#setVersion(String)}. */
 	public void testSetVersionTrue() {
 		Task.setVersion("true");
 		assertNotNull(
 			"Options.getBoolean(\"version\") == null",
 			Options.getBoolean("version")
 		);
 		assertTrue(
 			"Options.getBoolean(\"version\") == false",
 			Options.getBoolean("version")
 		);
 	}
 
 	/** Test {@link Task#setVersion(String)}. */
 	public void testSetVersionFalse() {
 		Task.setVersion("false");
 		assertNotNull(
 			"Options.getBoolean(\"version\") == null",
 			Options.getBoolean("version")
 		);
 		assertFalse(
 			"Options.getBoolean(\"version\") == true",
 			Options.getBoolean("version")
 		);
 	}
 
 	/** Test {@link Task#setVocabulary(String)}. */
 	public void testSetVocabulary() {
 		Task.setVocabulary("foo");
 		assertNotNull(
 			"Options.getString(\"vocabulary\") == null",
 			Options.getString("vocabulary")
 		);
 		assertEquals(
 			"Options.getString(\"vocabulary\") != \"foo\"",
 			"foo",
 			Options.getString("vocabulary")
 		);
 	}
 }
