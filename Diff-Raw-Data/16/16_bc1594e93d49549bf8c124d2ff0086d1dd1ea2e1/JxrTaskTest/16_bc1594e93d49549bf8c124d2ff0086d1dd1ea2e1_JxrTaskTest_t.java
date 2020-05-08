/*
 * Copyright 2012 Matt Bertolini
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

 package com.mattbertolini.jxr.ant;
 
 import junit.framework.TestSuite;
 import org.apache.ant.antunit.junit3.AntUnitSuite;
 import org.apache.ant.antunit.junit4.AntUnitSuiteRunner;
 import org.junit.runner.RunWith;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 /**
  * @author Matt Bertolini
  */
 @RunWith(AntUnitSuiteRunner.class)
 public class JxrTaskTest {
     public static TestSuite suite() throws URISyntaxException {
         URL resource = JxrTaskTest.class.getResource("/com/mattbertolini/jxr/ant/JxrTaskTest.xml");
         File file = new File(resource.toURI());
         return new AntUnitSuite(file, JxrTaskTest.class);
     }
 }
