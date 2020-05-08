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
 // OldViewXMLTest.java
 // Since: 2010/04/22
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.config;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.StringReader;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.utgenome.gwt.utgb.client.view.TrackView;
 import org.xerial.lens.Lens;
 import org.xerial.util.FileResource;
 import org.xerial.util.log.Logger;
 
 public class OldViewXMLTest {
 
 	private static Logger _logger = Logger.getLogger(OldViewXMLTest.class);
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void load() throws Exception {
 		OldViewXML ov = Lens.loadXML(OldViewXML.class, FileResource.open(OldViewXMLTest.class, "bed.xml"));
 
 		TrackView tv = ov.toTrackView();
 		String silk = Lens.toSilk(tv);
 
		assertTrue(tv.trackGroup.property.containsKey("dbGroup"));
 
 		TrackView tv2 = Lens.loadSilk(TrackView.class, new StringReader(silk));
 		_logger.info(Lens.toSilk(tv2));
 	}
 
 }
