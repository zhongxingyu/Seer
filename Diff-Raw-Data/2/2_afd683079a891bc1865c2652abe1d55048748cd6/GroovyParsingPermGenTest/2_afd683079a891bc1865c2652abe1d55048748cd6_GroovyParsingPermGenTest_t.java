 /*******************************************************************************
  * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  ******************************************************************************/
 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 
import org.cloudifysource.domain.Service;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.junit.Assert;
 import org.testng.annotations.Test;
 
 public class GroovyParsingPermGenTest extends AbstractTestSupport {
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testPermGenCleanup() throws Exception {
 		final int iterations = 2000;
 		LogUtils.log("Starting PermGen test for Groovy service parsing. Number of iterations: " + iterations);
 		for (int i = 0; i < iterations; ++i) {
 			if (i % 100 == 0) {
 				System.out.println("Starting iteration numbeer: " + i);
 			}
 			parseService();
 		}
 
 	}
 
 	private void parseService() throws Exception {
 		final File serviceDir = new File("src/main/resources/apps/USM/usm/empty");
 		final Service service = ServiceReader.getServiceFromDirectory(serviceDir).getService();
 
 		Assert.assertNotNull(service);
 
 	}
 
 }
