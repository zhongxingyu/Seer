 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.ui.tests.text;
 
import org.eclipse.dltk.core.tests.model.SuiteOfTestCases;
 import org.eclipse.dltk.ruby.internal.ui.text.hyperlink.RubyRequireHyperlinkDetector;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 
public class RubyRequireHyperlinkDetectorTest extends SuiteOfTestCases {
 
 	public RubyRequireHyperlinkDetectorTest(String name) {
 		super(name);
 	}
 
 	/**
 	 * @author Alexey
 	 * 
 	 */
 	private final class TestDetector extends RubyRequireHyperlinkDetector {
 
 		public IHyperlink checkLine(String line) {
 			return checkLine(0, line);
 		}
 
 		protected IHyperlink createLink(int offset, String line, int begin,
 				int end) {
 			final String requiredFile = line.substring(begin, end);
 			final Region region = new Region(offset + begin, end - begin);
 			return new TestHyperlink(requiredFile, region);
 		}
 	}
 
 	private static class TestHyperlink implements IHyperlink {
 
 		private final String path;
 		private final IRegion region;
 
 		public TestHyperlink(String path, IRegion region) {
 			this.path = path;
 			this.region = region;
 		}
 
 		public IRegion getHyperlinkRegion() {
 			return region;
 		}
 
 		public String getHyperlinkText() {
 			return path;
 		}
 
 		public String getTypeLabel() {
 			return null;
 		}
 
 		public void open() {
 			// empty
 		}
 
 	}
 
 	private final TestDetector detector = new TestDetector();
 
 	private void detect(String line, int offset, int length) {
 		final IHyperlink hyperlink = detector.checkLine(line);
 		assertNotNull(hyperlink);
 		assertEquals(offset, hyperlink.getHyperlinkRegion().getOffset());
 		assertEquals(length, hyperlink.getHyperlinkRegion().getLength());
 		assertEquals(line.substring(offset, offset + length), hyperlink
 				.getHyperlinkText());
 	}
 
 	public void testSingleQuotes() {
 		detect("require 'abc'", 9, 3);
 	}
 
 	public void testDoubleQuotes() {
 		detect("require \"abc\"", 9, 3);
 	}
 
 	public void testManySpaces() {
 		detect(" require  \"abc\"", 11, 3);
 	}
 
 	public void testBracketsSingleQuotes() {
 		detect("require('abc')", 9, 3);
 	}
 
 	public void testBracketsDoubleQuotes() {
 		detect("require(\"abc\")", 9, 3);
 	}
 
 	public void testBracketsAndSpaces() {
 		detect("require( 'abc' )", 10, 3);
 	}
 
 	public void testNoHyperlink() {
 		assertNull(detector.checkLine("require abc"));
 		assertNull(detector.checkLine("require 'abc\""));
 		assertNull(detector.checkLine("require \"abc'"));
 		assertNull(detector.checkLine("require(abc)"));
 		assertNull(detector.checkLine("require('abc\")"));
 	}
 
 }
