 /* Copyright (C) 2008 Jim Hargrave
 /*---------------------------------------------------------------------------*/
 /* This library is free software; you can redistribute it and/or modify it   */
 /* under the terms of the GNU Lesser General Public License as published by  */
 /* the Free Software Foundation; either version 2.1 of the License, or (at   */
 /* your option) any later version.                                           */
 /*                                                                           */
 /* This library is distributed in the hope that it will be useful, but       */
 /* WITHOUT ANY WARRANTY; without even the implied warranty of                */
 /* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   */
 /* General Public License for more details.                                  */
 /*                                                                           */
 /* You should have received a copy of the GNU Lesser General Public License  */
 /* along with this library; if not, write to the Free Software Foundation,   */
 /* Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA              */
 /*                                                                           */
 /* See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html */
 /*===========================================================================*/
 package net.sf.okapi.filters.openxml;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.filters.openxml.OpenXMLFilter;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 /**
  * This tests the Office 2007 configuration files that drive the OpenXMLFilter.
  */
 public class OpenXMLConfigurationTest {
 	
 	private OpenXMLFilter filter;
 
 	@Before
 	public void setUp() throws Exception {
 		filter = new OpenXMLFilter();
 	}
 	
 	@Test
 	public void defaultConfiguration() {		
 		URL url = OpenXMLConfigurationTest.class.getResource("/net/sf/okapi/filters/openxml/wordConfiguration.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);	
 		assertEquals(rules.getMainRuleType("w:p"), TaggedFilterConfiguration.RULE_TYPE.TEXT_UNIT_ELEMENT);
 		assertEquals(rules.getMainRuleType("wp:docpr"), TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTES_ONLY);
 		
 		Map<String, String> attributes = new HashMap<String, String>();
 		assertTrue(rules.isTranslatableAttribute("wp:docpr", "name", attributes));
 		assertTrue(rules.isTranslatableAttribute("pic:cnvpr", "name", attributes));
 		assertFalse(rules.isTranslatableAttribute("w:p", "name", attributes));
 		
 		attributes.clear();
 		attributes.put("w:val", "content-language");
 		assertTrue(rules.isWritableLocalizableAttribute("w:lang", "w:val", attributes));
 
 /*	
 		attributes.clear();
 		attributes.put("http-equiv", "content-type");
 		assertFalse(rules.isReadOnlyLocalizableAttribute("meta", "content", attributes));
 		
 		attributes.clear();
 		attributes.put("name", "generator");
 		assertTrue(rules.isReadOnlyLocalizableAttribute("meta", "content", attributes));
 */
 		url = OpenXMLConfigurationTest.class.getResource("/net/sf/okapi/filters/openxml/excelConfiguration.yml");
 		rules = new TaggedFilterConfiguration(url);	
 		attributes = new HashMap<String, String>();
 		assertTrue(rules.isTranslatableAttribute("table", "name", attributes));
 
 		url = OpenXMLConfigurationTest.class.getResource("/net/sf/okapi/filters/openxml/excelCommentConfiguration.yml");
 		rules = new TaggedFilterConfiguration(url);	
 		assertEquals(rules.getMainRuleType("t"), TaggedFilterConfiguration.RULE_TYPE.TEXT_MARKER_ELEMENT);
 
 		url = OpenXMLConfigurationTest.class.getResource("/net/sf/okapi/filters/openxml/powerpointConfiguration.yml");
 		rules = new TaggedFilterConfiguration(url);	
 		assertEquals(rules.getMainRuleType("a:p"), TaggedFilterConfiguration.RULE_TYPE.TEXT_UNIT_ELEMENT);
 		attributes = new HashMap<String, String>();
 		attributes.put("w:val", "content-language");
 		assertTrue(rules.isWritableLocalizableAttribute("a:rpr", "lang", attributes));
 	}
 
 	@Test
 	public void testStartDocument () {
		URL url = OpenXMLConfigurationTest.class.getResource("/BoldWorld.docx");
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(url.getPath(), null),
 			"UTF-8", "en", "en"));
 	}
 	
 }
