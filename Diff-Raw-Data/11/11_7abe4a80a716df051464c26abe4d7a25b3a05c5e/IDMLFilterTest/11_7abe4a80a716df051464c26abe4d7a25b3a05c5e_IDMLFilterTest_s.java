 /*===========================================================================
   Copyright (C) 2009-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.idml.tests;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.filters.idml.IDMLFilter;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 public class IDMLFilterTest {
 
 	private IDMLFilter filter;
 	private String root;
 	private LocaleId locEN = LocaleId.fromString("en");
 
 	@Before
 	public void setUp() {
 		filter = new IDMLFilter();
 		root = TestUtil.getParentDir(this.getClass(), "/Test01.idml");
 	}
 
 	@Test
 	public void testDefaultInfo () {
 		assertNotNull(filter.getParameters());
 		assertNotNull(filter.getName());
 		List<FilterConfiguration> list = filter.getConfigurations();
 		assertNotNull(list);
 		assertTrue(list.size()>0);
 	}
 	
 	@Test
 	public void testSimpleEntry () {
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(root+"helloworld-1.idml"), 1);
 		assertNotNull(tu);
 		String text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
 		assertEquals("Hello World!", text);
 	}
 
 	@Test
 	public void testSimpleEntry2 () {
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(root+"Test00.idml"), 1);
 		assertNotNull(tu);
 		String text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
 		assertEquals("Hello World!", text);
 	}
 
 	@Test
 	public void testStartDocument () {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(root+"Test01.idml", null),
 			"UTF-8", locEN, locEN));
 	}
 	
 	@Test
 	public void testDoubleExtraction () {
 		// Read all files in the data directory
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		list.add(new InputDocument(root+"Test00.idml", "okf_idml@ExtractAll.fprm"));
 		list.add(new InputDocument(root+"Test01.idml", "okf_idml@ExtractAll.fprm"));
 		list.add(new InputDocument(root+"Test02.idml", "okf_idml@ExtractAll.fprm"));
 		list.add(new InputDocument(root+"helloworld-1.idml", "okf_idml@ExtractAll.fprm"));
 		list.add(new InputDocument(root+"ConditionalText.idml", "okf_idml@ExtractAll.fprm"));
 		list.add(new InputDocument(root+"Test03.idml", "okf_idml@ExtractAll.fprm"));
 		list.add(new InputDocument(root+"testWithSpecialChars.idml", "okf_idml@ExtractAll.fprm"));
 
 // For local test only, comment out for SVN		
 //		list.add(new InputDocument(root+"private/enlaso_001.idml", "../okf_idml@ExtractAll.fprm"));
 //		list.add(new InputDocument(root+"private/enlaso_002.idml", "../okf_idml@ExtractAll.fprm"));
 //		list.add(new InputDocument(root+"private/WithMasterPage.idml", "../okf_idml@ExtractAll.fprm"));
 //		list.add(new InputDocument(root+"Bluto.idml", "okf_idml@ExtractAll.fprm"));
 		
 		RoundTripComparison rtc = new RoundTripComparison(false); // Do not compare skeleton
 		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locEN, "output"));
 	}
 
 	private ArrayList<Event> getEvents (String path) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		RawDocument rd = new RawDocument(new File(path).toURI(), "UTF-8", locEN);
 		filter.open(rd);
 		while (filter.hasNext()) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 
 }
