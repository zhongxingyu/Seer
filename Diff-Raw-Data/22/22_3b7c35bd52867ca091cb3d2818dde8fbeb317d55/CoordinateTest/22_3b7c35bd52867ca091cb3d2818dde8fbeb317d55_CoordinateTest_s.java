 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // CoordinateTest.java
 // Since: Feb 17, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.server.bio;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Properties;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.utgenome.gwt.utgb.client.bio.Coordinate;
 
 public class CoordinateTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testname() throws Exception {
 		Coordinate c = Coordinate.newUTGBCoordinate("human", "hg18", "chr1");
		assertEquals("http://somewhere.org/mytrack?group=utgb&species=human&revision=hg18&name=chr1", c.getTrackURL("http://somewhere.org/mytrack?%q"));
 
 		Properties p = new Properties();
 		p.put("width", 700);
		assertEquals("http://somewhere.org/mytrack?group=utgb&species=human&revision=hg18&name=chr1&width=700", c.getTrackURL(
				"http://somewhere.org/mytrack?%q", p));
 	}
 
 }
