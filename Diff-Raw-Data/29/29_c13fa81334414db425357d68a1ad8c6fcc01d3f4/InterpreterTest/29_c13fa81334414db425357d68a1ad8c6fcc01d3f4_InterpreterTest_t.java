 /**
  * Copyright (C) 2012 Primitive Team <jpalka@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.primitive.javascript.tests.functional;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import junitparams.JUnitParamsRunner;
 import junitparams.Parameters;
 import net.primitive.javascript.core.Convertions;
 import net.primitive.javascript.core.Script;
 import net.primitive.javascript.core.Scriptable;
 import net.primitive.javascript.core.ScriptableObject;
 import net.primitive.javascript.core.jdk.Console;
 import net.primitive.javascript.core.natives.StandardObjects;
 import net.primitive.javascript.interpreter.Interpreter;
 import net.primitive.javascript.tests.utils.ResourceList;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(JUnitParamsRunner.class)
 public class InterpreterTest {
 
 	protected Object[] getParameters() {
 		Collection<String> scripts = ResourceList.getResources(Pattern
 				.compile(".*\\.js"));
 		List<Object[]> parameters = new ArrayList<Object[]>();
 
 		for (String script : scripts) {
 			parameters.add(new Object[] { script });
 		}
 
 		return parameters.toArray(new Object[] {});
 	}
 
 	@Test
 	@Parameters(method = "getParameters")
 	public void drive_javascript_test(String javaScriptFile) throws Exception {
 
 		Scriptable globalObject = new ScriptableObject();
 		StandardObjects stdObjs = StandardObjects.createStandardObjects(globalObject);
 
 
 		Console.init(globalObject);
 
 		Interpreter interpreter = new Interpreter();
 
 		Script script = interpreter.interpret(new File(javaScriptFile));
 		script.execute(globalObject);
 		Object object = globalObject.get("assertResult");
 		assertTrue("assert failed in " + javaScriptFile,
 				Convertions.toBoolean(object));
 
 	}
 	
 	public static void main(String[] args) throws Exception {
		new InterpreterTest().drive_javascript_test("/home/palkaj01/projects/newjs/functional-tests/src/main/resources/check.js");
 	}
 }
