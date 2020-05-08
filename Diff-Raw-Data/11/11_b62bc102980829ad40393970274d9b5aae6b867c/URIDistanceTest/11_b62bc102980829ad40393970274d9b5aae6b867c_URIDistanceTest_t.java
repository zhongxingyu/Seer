 /**
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 package org.eclipse.emf.compare.tests.diff;
 
 import static org.junit.Assert.assertEquals;
 
 import org.eclipse.emf.compare.match.eobject.URIDistance;
 import org.junit.Test;
 
 public class URIDistanceTest {
 
 	private URIDistance meter = new URIDistance();
 
 	@Test
 	public void sameNumberOfFragments() throws Exception {
 		assertEquals(3, meter.proximity("/root/a/b", "/root/a/b/c"));
 		assertEquals(3, meter.proximity("/root/a/b/", "/root/a/b/c/"));
 		assertEquals(0, meter.proximity("/root/a/b", "/root/a/b"));
 		assertEquals(8, meter.proximity("/root/a/a2/a3", "/root/b/b2/a3"));
 		assertEquals(8, meter.proximity("/root/a/a2/a3", "/root/b/b2/b3"));
 	}
 
 	@Test
 	public void identics() throws Exception {
 		assertEquals(0, meter.proximity("/root/a/b/", "/root/a/b/"));
 		assertEquals(0, meter.proximity("/root/", "/root/"));
 		assertEquals(0, meter.proximity("", ""));
 	}
 
 	@Test
 	public void limitCases() throws Exception {
 		assertEquals(0, meter.proximity("", ""));
 		assertEquals(10, meter.proximity("",
 				"/a/very/long/path/just/to/check/we/wont/ends/up/with/a/weird/thing"));
		assertEquals(10, meter.proximity(
				"/a/very/long/path/just/to/check/we/wont/ends/up/with/a/weird/thing", ""));
 	}
 
 	@Test
 	public void completelyDifferent() throws Exception {
 		assertEquals(10, meter.proximity("/c/d/e/", "/root/a/b/"));
 		assertEquals(10, meter.proximity("/c/", "/root/a/b/"));
 		assertEquals(10, meter.proximity("/c/d/e", "/root/"));
 		assertEquals(10, meter.proximity("/c/d/e/f", "/a/b/e/f"));
 		assertEquals(10, meter.proximity("/a", "/b"));
 	}
 
 	@Test
 	public void orderMatters() throws Exception {
 		assertEquals(10, meter.proximity("/c/d/e/f", "/f/d/c/e"));
 	}
 
 	@Test
 	public void idLikeURIs() throws Exception {
 		assertEquals(10, meter.proximity("#131233", "#azeazezae"));
 		assertEquals(3, meter.proximity("/c/d/e/f", "/c/d/e/f?#azeaze"));
 	}
 
 	@Test
 	public void traillingSlashes() throws Exception {
 		assertEquals(3, meter.proximity("/root/a/b/", "/root/a/b/c/"));
 		assertEquals(3, meter.proximity("root/a/b/", "/root/a/b/c/"));
 		assertEquals(3, meter.proximity("/root/a/b/", "/root/a/b/c"));
 		assertEquals(3, meter.proximity("///root/a/b/", "/root/a/b/c"));
 	}
 
 }
