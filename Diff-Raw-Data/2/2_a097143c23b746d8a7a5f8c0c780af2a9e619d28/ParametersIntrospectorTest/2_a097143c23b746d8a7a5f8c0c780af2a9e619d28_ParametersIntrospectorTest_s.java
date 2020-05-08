 /*
  * Copyright (c) 2010-2010, Dmitry Sidorenko. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.googlecode.commandme.impl.introspector;
 /**
  *
  * @author Dmitry Sidorenko
  */
 
 import com.googlecode.commandme.ParameterDefinitionException;
 import com.googlecode.commandme.annotations.Action;
 import com.googlecode.commandme.annotations.Parameter;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 @SuppressWarnings({"UnusedParameters"})
 public class ParametersIntrospectorTest {
     private ParametersIntrospector<TestModule1> parameters;
 
     @Before
     public void setup() {
         parameters = new ParametersIntrospector<TestModule1>(TestModule1.class);
     }
 
     @Test
     public void testModuleParameters() throws Exception {
         assertThat(parameters.getParameterDefinitions(), notNullValue());
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void testGetParameterDefinitions() throws Exception {
         parameters.getParameterDefinitions().clear();
     }
 
     @Test
     public void testAddParameter() throws Exception {
         final ParameterDefinition definition = new ParameterDefinition();
         parameters.addParameter(definition);
         assertThat(parameters.getParameterDefinitions().size(), is(1));
         assertThat(parameters.getParameterDefinitions().get(0), is(definition));
     }
 
     @Test
     public void testGetByLongName() throws Exception {
         ParameterDefinition definition = new ParameterDefinition();
         definition.setLongName("foo");
         parameters.addParameter(definition);
 
         ParameterDefinition definitionBar = new ParameterDefinition();
         definitionBar.setLongName("bar");
         parameters.addParameter(definitionBar);
 
         assertThat(parameters.getByLongName("bar"), is(definitionBar));
         assertThat(parameters.getByLongName("foo"), is(definition));
     }
 
     @Test
     public void testGetByShortName() throws Exception {
         ParameterDefinition definition = new ParameterDefinition();
         definition.setShortName("f");
         parameters.addParameter(definition);
 
         ParameterDefinition definitionBar = new ParameterDefinition();
         definitionBar.setShortName("b");
         parameters.addParameter(definitionBar);
 
         assertThat(parameters.getByShortName("b"), is(definitionBar));
         assertThat(parameters.getByShortName("f"), is(definition));
     }
 
     @Test
     public void testInspectParameters() throws Exception {
         parameters.inspect();
         for (ParameterDefinition parameterDefinition : parameters.getParameterDefinitions()) {
             assertThat(parameterDefinition.getShortName(), notNullValue());
             assertThat(parameterDefinition.getShortName().length(), is(1));
 
             assertThat(parameterDefinition.getLongName(), notNullValue());
 
             assertThat(parameterDefinition.getDefaultValue(), notNullValue());
             assertThat(parameterDefinition.getDescription(), notNullValue());
             assertThat(parameterDefinition.getType(), notNullValue());
         }
 
     }
 
     @Test
     public void testNonBeanCompliantParams() throws Exception {
         parameters.inspect();
         final ParameterDefinition fooParam = parameters.getByLongName("label");
         assertThat(fooParam, notNullValue());
         assertThat(fooParam.getLongName(), is("label"));
         assertThat(fooParam.getShortName(), is("l"));
         assertEquals(String.class, fooParam.getType());
     }
 
     @Test
     public void testInspectParametersValuesAreCorrect() throws Exception {
         parameters.inspect();
         final ParameterDefinition fooParam = parameters.getByLongName("foo");
         assertThat(fooParam, notNullValue());
         assertThat(fooParam.getLongName(), is("foo"));
         assertThat(fooParam.getShortName(), is("f"));
         assertEquals(Integer.TYPE, fooParam.getType());
         assertThat(fooParam.getDefaultValue(), is("0"));
         assertThat(fooParam.getDescription(), is("none"));
 
         final ParameterDefinition nameParam = parameters.getByLongName("name");
         assertThat(nameParam, notNullValue());
         assertThat(nameParam.getLongName(), is("name"));
         assertThat(nameParam.getShortName(), is("n"));
         assertEquals(String.class, nameParam.getType());
         assertThat(nameParam.getDefaultValue(), is(""));
         assertThat(nameParam.getDescription(), is(""));
 
 
         for (ParameterDefinition parameterDefinition : parameters.getParameterDefinitions()) {
             assertThat(parameterDefinition.getShortName(), notNullValue());
             assertThat(parameterDefinition.getShortName().length(), is(1));
 
             assertThat(parameterDefinition.getLongName(), notNullValue());
 
             assertThat(parameterDefinition.getDefaultValue(), notNullValue());
             assertThat(parameterDefinition.getDescription(), notNullValue());
             assertThat(parameterDefinition.getType(), notNullValue());
         }
 
     }
 
     @Test(expected = ParameterDefinitionException.class)
     public void testBadSetter() throws Exception {
         ParametersIntrospector<BadModule1> parameters = new ParametersIntrospector<BadModule1>(BadModule1.class);
         parameters.inspect();
 
         final ParameterDefinition fooParam = parameters.getByLongName("name");
         assertThat(fooParam, nullValue());
     }
 
     static class BadModule1 {
         @Parameter(longName = "name")
         public void setName(String name, int age) {
         }
 
     }
 
     @Test
     public void testShortNames() throws Exception {
         ParametersIntrospector<ShortModule1> parameters = new ParametersIntrospector<ShortModule1>(ShortModule1.class);
         parameters.inspect();
 
         final ParameterDefinition fooParam = parameters.getByLongName("name");
         assertThat(fooParam, nullValue());
     }
 
     static class ShortModule1 {
         @Parameter(shortName = "n")
        public void setName(String name, int age) {
         }
 
     }
 
     @Test(expected = ParameterDefinitionException.class)
     public void testShortNamesBad() throws Exception {
         ParametersIntrospector<ShortModuleBad1> parameters = new ParametersIntrospector<ShortModuleBad1>(ShortModuleBad1.class);
         parameters.inspect();
     }
 
     static class ShortModuleBad1 {
         @Parameter(shortName = "loong")
         public void setName(String name) {
         }
     }
 
     @Test(expected = ParameterDefinitionException.class)
     public void testShortNamesBadSameShorts() throws Exception {
         ParametersIntrospector<ShortModuleBad2> parameters = new ParametersIntrospector<ShortModuleBad2>(ShortModuleBad2.class);
         parameters.inspect();
     }
 
     static class ShortModuleBad2 {
         @Parameter(shortName = "n")
         public void setFuss(String name) {
         }
 
         @Parameter
         public void setName(String name) {
         }
     }
 
     @Test
     public void testBadSetterAccess() throws Exception {
         ParametersIntrospector<BadModule2> parameters = new ParametersIntrospector<BadModule2>(BadModule2.class);
         parameters.inspect();
 
         final ParameterDefinition fooParam = parameters.getByLongName("name");
         assertThat(fooParam, nullValue());
     }
 
     @Test
     public void testIntParameter() throws Exception {
         ParametersIntrospector<IntModule> parameters = new ParametersIntrospector<IntModule>(IntModule.class);
         parameters.inspect();
         assertThat(parameters.getParameterDefinitions().size(), is(8));
 
         assertEquals(parameters.getByLongName("int").getType(), Integer.TYPE);
         assertEquals(parameters.getByLongName("intC").getType(), Integer.class);
     }
 
     static class IntModule {
 
         @Parameter
         public void setInt(int id) {
         }
 
         @Parameter
         public void setIntC(Integer id) {
         }
 
         @Parameter
         public void setLong(long id) {
         }
 
         @Parameter
         public void setLongC(Long id) {
         }
 
         @Parameter
         public void setByte(byte id) {
         }
 
         @Parameter
         public void setByteC(Byte id) {
         }
 
         @Parameter
         public void setShort(short id) {
         }
 
 
         @Parameter
         public void setShortC(Short id) {
         }
     }
 
     static class BadModule2 {
         @Parameter
         void setName(String name) {
         }
 
     }
 
     static class TestModule1 {
 
         @Parameter
         public void setName(String sd) {
         }
 
         @Parameter(longName = "label")
         public void labelIt(String label) {
         }
 
         @Parameter(longName = "foo", shortName = "f", defaultValue = "0", description = "none", helpRequest = true)
         public void setNoName(int i) {
         }
 
         @Action
         public void greet() {
 
         }
     }
 }
