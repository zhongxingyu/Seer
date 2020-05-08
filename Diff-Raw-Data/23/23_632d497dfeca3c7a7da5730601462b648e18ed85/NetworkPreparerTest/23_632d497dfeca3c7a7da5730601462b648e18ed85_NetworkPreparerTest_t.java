 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * 
  * @author dmyersturnbull
  */
 package org.structnetalign.util;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 
 import psidev.psi.mi.xml.model.EntrySet;
 
 
 public class NetworkPreparerTest {
 
 	private static final String RESOURCE_DIR = "src/test/resources/util/";
 	
 	@Test
 	public void testSimplify() throws IOException {
 		final File expected = new File(RESOURCE_DIR + "simplified_network.xml");
 		final File input = new File(RESOURCE_DIR + "input_network.xml");
 		final File output = new File("simplified_network.xml");
		output.deleteOnExit();
 		NetworkPreparer prep = new NetworkPreparer();
 		EntrySet entrySet = NetworkUtils.readNetwork(input);
 		entrySet = prep.simplify(entrySet);
 		NetworkUtils.writeNetwork(entrySet, output);
 		assertTrue("Simplified file is wrong", FileUtils.contentEquals(expected, output));
 		output.delete();
 	}
 
 	@Test
	public void testInitConfidence() {
		final File input = new File(RESOURCE_DIR + "before_init_conf.psimi.xml");
		NetworkPreparer prep = new NetworkPreparer();
		EntrySet entrySet = NetworkUtils.readNetwork(input);
		entrySet = prep.initConfidences(entrySet, "IamaLABEL", "andImaNAME", 0.2);
		File actual = new File("withinitconf.xml.tmp");
		actual.deleteOnExit();
		NetworkUtils.writeNetwork(entrySet, actual);
		final File expected = new File(RESOURCE_DIR + "after_init_conf.psimi.xml");
		boolean similar = TestUtils.compareXml(actual, expected);
		assertTrue("Graph is wrong", similar);
		actual.delete();
	}
	
	@Test
 	public void testGetCcs() throws IOException {
 		final File input = new File(RESOURCE_DIR + "simplified_network.xml");
 		final File output = new File("cc.xml");
		output.deleteOnExit();
 		final String expectedCcs = RESOURCE_DIR + "expected_ccs/";
 		NetworkPreparer prep = new NetworkPreparer();
 		EntrySet entrySet = NetworkUtils.readNetwork(input);
 		List<EntrySet> ccs = prep.getConnnectedComponents(entrySet);
 		
 		assertEquals("Found the wrong number of connected components", 2, ccs.size());
 		for (int i = 0; i < ccs.size(); i++) {
 			NetworkUtils.writeNetwork(ccs.get(i), output);
 			final File expectedCc = new File(expectedCcs + i + ".xml");
			boolean similar = TestUtils.compareXml(expectedCc, output);
			assertTrue("Connected component file " + i + " is wrong", similar);
 			output.delete();
 		}
 	}
 	
 }
