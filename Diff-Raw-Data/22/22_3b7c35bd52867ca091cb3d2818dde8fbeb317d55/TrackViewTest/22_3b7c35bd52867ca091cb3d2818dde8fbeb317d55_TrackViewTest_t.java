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
 // TrackViewTest.java
 // Since: 2009/12/22
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.UnsupportedEncodingException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.utgenome.gwt.utgb.client.view.TrackView;
 import org.xerial.lens.Lens;
 import org.xerial.util.FileResource;
 import org.xerial.util.log.Logger;
 
 public class TrackViewTest {
 
 	private static Logger _logger = Logger.getLogger(TrackViewTest.class);
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void encode() throws UnsupportedEncodingException {
 
 		System.out.println("hello world".getBytes("UTF-16").length);
 		System.out.println(System.getProperty("file.encoding"));
 	}
 
 	@Test
 	public void loadSilk() throws Exception {
 		TrackView v = Lens.loadSilk(TrackView.class, FileResource.find(TrackViewTest.class, "default-view.silk"));
 		assertNotNull(v);
 
 		assertEquals("human:hg19", v.trackGroup.coordinate.ref);
 		assertEquals("chr1", v.trackGroup.coordinate.chr);
 		TrackView.Coordinate w = v.trackGroup.coordinate;
 
 		assertEquals(1, w.start);
 		assertEquals(100000, w.end);
 		assertEquals(800, w.pixelWidth);
 
 		assertEquals(7, v.track.size());
 
 		TrackView.Track t = v.track.get(0);
 		assertEquals("UTGB Navigator", t.name);
 		assertEquals(41, t.height);
 		assertEquals(true, t.pack);
 		assertEquals("org.utgenome.gwt.utgb.client.track.lib.NavigatorTrack", t.class_);
		assertEquals(true, t.property.containsKey("sequencelist"));
 
 		_logger.info("\n" + Lens.toSilk(v));
 
 	}
 
 }
