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
 
 package net.sf.okapi.filters.xmlstream;
 
import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.okapi.common.filters.InlineCodeFinder;
 import net.sf.okapi.filters.xmlstream.XmlStreamFilter;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class XmlStreamConfigurationTest {
 
 	@Before
 	public void setUp() throws Exception {
 
 	}
 
 	@Test
 	public void defaultConfiguration() {
 		URL url = XmlStreamFilter.class.getResource("/net/sf/okapi/filters/xmlstream/dita.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		assertTrue(rules.isRuleType("xml:id", TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTE_ID));				
 	}
 
 	@Test
 	public void preserveWhiteSpace() {
 		URL url = XmlStreamFilter.class.getResource("/net/sf/okapi/filters/xmlstream/default.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		assertTrue(rules.isRuleType("xml:space", TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE));
 	}
 
 	@Test
 	public void xmlLang() {
 		URL url = XmlStreamFilter.class.getResource("/net/sf/okapi/filters/xmlstream/default.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 
 		Map<String, String> attributes = new HashMap<String, String>();
 		attributes.clear();
 		attributes.put("lang", "en");
 		assertTrue(rules.isWritableLocalizableAttribute("x", "xml:lang", attributes));
 		assertFalse(rules.isReadOnlyLocalizableAttribute("x", "xml:lang", attributes));
 		assertFalse(rules.isTranslatableAttribute("x", "xml:lang", attributes));
 
 		assertTrue(rules.isWritableLocalizableAttribute("p", "xml:lang", attributes));
 		assertFalse(rules.isReadOnlyLocalizableAttribute("p", "xml:lang", attributes));
 		assertFalse(rules.isTranslatableAttribute("p", "xml:lang", attributes));
 
 		attributes.clear();
 		attributes.put("xml:lang", "en");
 		assertTrue(rules.isWritableLocalizableAttribute("x", "xml:lang", attributes));
 	}
 
 	@Test
 	public void genericCodeTypes() {
 		URL url = XmlStreamFilter.class.getResource("/net/sf/okapi/filters/xmlstream/dita.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		assertNotNull(rules);
 	}
 
 	@Test
 	public void textUnitCodeTypes() {
 		URL url = XmlStreamFilter.class.getResource("/net/sf/okapi/filters/xmlstream/dita.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);		
 		assertNotNull(rules);
 	}
 
 	@Test
 	public void collapseWhitespace() {
 		URL url = XmlStreamFilter.class.getResource("/net/sf/okapi/filters/xmlstream/default.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		assertFalse(rules.isGlobalPreserveWhitespace());
 
 		url = XmlStreamConfigurationTest.class.getResource("/collapseWhitespaceOff.yml");
 		rules = new TaggedFilterConfiguration(url);
 		assertTrue(rules.isGlobalPreserveWhitespace());
 	}
 	
 	@Test
 	public void excludeByDefault() {
 		URL url = XmlStreamFilter.class.getResource("/net/sf/okapi/filters/xmlstream/default.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		assertFalse(rules.isGlobalExcludeByDefault()); 
 
 		url = XmlStreamConfigurationTest.class.getResource("/excludeByDefault.yml");
 		rules = new TaggedFilterConfiguration(url);
 		assertTrue(rules.isGlobalExcludeByDefault());
 	}
 
 	@Test
 	public void testCodeFinderRules() {
 		URL url = XmlStreamConfigurationTest.class.getResource("/withCodeFinderRules.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		assertTrue(rules.isUseCodeFinder());
 		InlineCodeFinder cf = new InlineCodeFinder();
 		cf.fromString(rules.getCodeFinderRules());
 		cf.compile();
 		ArrayList<String> list = cf.getRules();
 		assertNotNull(list);
 		assertEquals(2, list.size());
 		assertEquals("[eE]", list.get(0));
 		assertEquals("\\bVAR\\d\\b", list.get(1));
 	}
 
 	@Test
 	public void attributeID() {
 		URL url = XmlStreamFilter.class.getResource("/net/sf/okapi/filters/xmlstream/default.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 
 		Map<String, String> attributes = new HashMap<String, String>();
 		attributes.clear();
 		attributes.put("xml:id", "value");
 		assertTrue(rules.isIdAttribute("p", "xml:id", attributes));
 		assertFalse(rules.isIdAttribute("p", "foo", attributes));
 	}
	
	@Test
	public void loadNonAsciiRuleFile() throws Exception {
		// nonAscii.yml contains some Japanese characters and it's
		// encoded in UTF-8. Loading the file shouldn't throw an
		// exception.
		URL url = XmlStreamConfigurationTest.class.getResource("/nonAscii.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertNotNull(rules);

		File file = new File(url.getFile());
		rules = new TaggedFilterConfiguration(file);
		assertNotNull(rules);
	}
 }
