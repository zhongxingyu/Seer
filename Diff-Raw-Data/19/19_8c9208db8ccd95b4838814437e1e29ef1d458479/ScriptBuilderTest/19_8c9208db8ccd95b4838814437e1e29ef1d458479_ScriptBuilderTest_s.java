 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package de.unioninvestment.eai.portal.support.scripting;
 
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
 import groovy.lang.Closure;

import java.util.LinkedList;
import java.util.List;

 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.MockitoAnnotations;
 import org.springframework.test.context.ContextConfiguration;
 
import de.unioninvestment.eai.portal.portlet.crud.config.GroovyScript;
import de.unioninvestment.eai.portal.portlet.crud.config.ScriptConfig;
import de.unioninvestment.eai.portal.support.vaadin.junit.AbstractSpringPortletContextTest;
 
 @ContextConfiguration({ "/eai-portal-web-test-applicationcontext.xml" })
 public class ScriptBuilderTest extends AbstractSpringPortletContextTest {
 	private List<String> list;
 
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 		list = new LinkedList<String>();
 	}
 
 	@Test
 	public void shouldInstantiateAndRunMainScript() throws ScriptingException {
 		ScriptConfig config = prepareScriptConfig("list.add 'Bla'");
 
 		ScriptBuilder builder = new ScriptBuilder();
 		builder.registerMainScript(config);
 		builder.addBindingVariable("list", list);
 		builder.runMainScript();
 
 		assertThat(list, hasItem("Bla"));
 	}
 
 	private ScriptConfig prepareScriptConfig(String source)
 			throws ScriptingException {
 
 		ScriptConfig config = new ScriptConfig();
 		config.setValue(new GroovyScript(source));
 
 		ScriptCompiler compiler = new ScriptCompiler();
 		config.getValue().setClazz(
 				compiler.compileScript(config.getValue().getSource()));
 		return config;
 	}
 
 	@Test
 	public void shouldBuildClosure() {
 		ScriptConfig mainScript = prepareScriptConfig("");
 		ScriptConfig closureScript = prepareScriptConfig("{ it -> list.add 'Bla' }");
 
 		ScriptBuilder builder = new ScriptBuilder();
 		builder.registerMainScript(mainScript);
 
 		Closure<?> closure = builder.buildClosure(closureScript.getValue());
 
 		builder.addBindingVariable("list", list);
 		builder.runMainScript();
 
 		closure.call();
 
 		assertThat(list, hasItem("Bla"));
 	}
 }
