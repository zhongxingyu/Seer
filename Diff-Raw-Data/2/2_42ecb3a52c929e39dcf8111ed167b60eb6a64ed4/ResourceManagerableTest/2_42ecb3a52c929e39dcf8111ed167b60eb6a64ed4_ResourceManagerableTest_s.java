 /* Chronos - Game Development Toolkit for Java game developers. The
  * original source remains:
  * 
  * Copyright (c) 2013 Miguel Gonzalez http://my-reality.de
  * 
  * This source is provided under the terms of the BSD License.
  * 
  * Copyright (c) 2013, Chronos
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the following 
  * conditions are met:
  * 
  *  * Redistributions of source code must retain the above 
  *    copyright notice, this list of conditions and the 
  *    following disclaimer.
  *  * Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution.
  *  * Neither the name of the Chronos/my Reality Development nor the names of 
  *    its contributors may be used to endorse or promote products 
  *    derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
  * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
  * OF SUCH DAMAGE.
  */
 package de.myreality.chronos.resources;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test case for a resource managerable
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 
  * @version 
  */
 public class ResourceManagerableTest {
 	
 	ResourceManagerable manager;
 	
 	ResourceDefinition definition1, definition2;
 
 	@Before
 	public void setUp() throws Exception {
 		manager = new ResourceManager();		
 		manager.load(new MockDataSource());
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	@Test
 	public void testHasResource() {
 		assertTrue("Definition1 should be in the manager.", manager.hasResource("definition1", String.class));
 		assertTrue("Definition2 should be in the manager.", manager.hasResource("definition2", String.class));
 	}
 
 	@Test
 	public void testGetResource() {
 		assertTrue("Definition1 should be in the manager.", manager.getResource("definition1", String.class) != null);
 		assertTrue("Definition2 should be in the manager.", manager.getResource("definition2", String.class) != null);
 	}
 
 	// ===========================================================
 	// Inner classes
 	// ===========================================================
 	
 	// Data source which provides dump data
 	class MockDataSource implements DataSource {
 
 		@Override
 		public Collection<ResourceDefinition> load() throws ResourceException {
 			
 			List<ResourceDefinition> definitions = new ArrayList<ResourceDefinition>();
 			definition1 = new BasicResourceDefinition("definition1", "string");
			definition2 = new BasicResourceDefinition("definition1", "string");
 			definitions.add(definition1);
 			definitions.add(definition2);
 			
 			return definitions;
 		}
 		
 	}
 }
