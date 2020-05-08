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
  * @author dmyersturnbull
  */
 package org.structnetalign.util;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class MartinIdentifierMappingTest {
 
 	private MartinIdentifierMapping mapping;
 	
 	@Before
 	public void setUp() {
 		mapping = new MartinIdentifierMapping();
 	}
 	
 	@Test
 	public void testPdbId() {
 		String pdb = mapping.uniProtToPdb("P00720");
 		assertEquals("102l_A", pdb);
 	}
 
 	@Test
 	public void testScopId() {
 		String scop = mapping.uniProtToScop("P00720");
 		assertEquals("d102la_", scop);
 	}
 }
