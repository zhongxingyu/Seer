 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.connectors.pensieve;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.lib.translation.ITMQuery;
 import net.sf.okapi.lib.translation.QueryResult;
 import net.sf.okapi.tm.pensieve.common.TranslationUnit;
 import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;
 import net.sf.okapi.tm.pensieve.writer.ITmWriter;
 import net.sf.okapi.tm.pensieve.writer.TmWriterFactory;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PensieveTMConnectorTest {
 	
 	private ITMQuery connector;
 	private LocaleId locENUS = LocaleId.fromString("EN-US");
 	private LocaleId locFRFR = LocaleId.fromString("FR-FR");
 	
 	@Before
 	public void setUp() {
 		URL url = PensieveTMConnectorTest.class.getResource("/testtm/segments.gen");
 		connector = null;
 		if ( url == null ) return; // Creation case
 		connector = new PensieveTMConnector();
 		Parameters params = new Parameters();
 		params.setDbDirectory(Util.getDirectoryName(url.getPath()));
 		connector.setParameters(params);
 		connector.open();
 		connector.setLanguages(locENUS, locFRFR);
 	}
 	
 	@After
 	public void tearDown () {
 		if ( connector != null ) {
 			connector.close();
 			connector = null;
 		}
 	}
 
 	@Test
 	public void testCreateTM () throws IOException {
 		if ( connector != null ) {
 			connector.close(); // Close the connector created by setUp()
 		}
 		URL url = PensieveTMConnectorTest.class.getResource("/mytm.tmx");
 		String dir =  Util.getDirectoryName(url.getPath())+File.separator+"testtm"+File.separator;
 		Util.createDirectories(dir);
 		ITmWriter writer = TmWriterFactory.createFileBasedTmWriter(dir, true);
 		
 		// TU 1
 		TranslationUnit tu = new TranslationUnit();
 		TranslationUnitVariant tuv = new TranslationUnitVariant(locENUS,
 			new TextFragment("Elephants cannot fly."));
 		tu.setSource(tuv);
 		tuv = new TranslationUnitVariant(locFRFR,
 			new TextFragment("Les \u00e9l\u00e9phants ne peuvent pas voler."));
 		tu.setTarget(tuv);
 		writer.indexTranslationUnit(tu);
 
 		// TU 2
 		tu = new TranslationUnit();
 		TextFragment tf = new TextFragment("Elephants ");
 		tf.append(TagType.OPENING, "b", "<b>");
 		tf.append("cannot");
 		tf.append(TagType.CLOSING, "b", "</b>");
 		tf.append(" fly.");
 		tuv = new TranslationUnitVariant(locENUS, tf);
 		tu.setSource(tuv);
 		tf = new TextFragment("Les \u00e9l\u00e9phants ");
 		tf.append(TagType.OPENING, "b", "<b>");
 		tf.append("ne peuvent pas");
 		tf.append(TagType.CLOSING, "b", "</b>");
 		tf.append(" voler.");
 		tuv = new TranslationUnitVariant(locFRFR, tf);
 		tu.setTarget(tuv);
 		writer.indexTranslationUnit(tu);
 
 		// TU 3
 		tu = new TranslationUnit();
 		tf = new TextFragment("Elephants ");
 		tf.append(TagType.OPENING, "g0", "<g0>");
 		tf.append("cannot");
 		tf.append(TagType.CLOSING, "g0", "</g0>");
 		tf.append(" fly.");
 		tuv = new TranslationUnitVariant(locENUS, tf);
 		tu.setSource(tuv);
 		tf = new TextFragment("Les \u00e9l\u00e9phants ");
 		tf.append(TagType.OPENING, "g0", "<g0>");
 		tf.append("ne peuvent pas");
 		tf.append(TagType.CLOSING, "g0", "</g0>");
 		tf.append(" voler.");
 		tuv = new TranslationUnitVariant(locFRFR, tf);
 		tu.setTarget(tuv);
 		writer.indexTranslationUnit(tu);
 
 		writer.endIndex();
 	}
 
 	@Test
 	public void testGetMatches () {
 		String input = "Elephants cannot fly.";
 		connector.setThreshold(75);
 		assertTrue(connector.query(input) > 0);
 		assertTrue(connector.hasNext());
 		QueryResult qr = connector.next();
 		assertNotNull(qr);
 		assertEquals(input, qr.source.toString());
 		assertEquals("Les \u00e9l\u00e9phants ne peuvent pas voler.", qr.target.toString());
 	}
 
 	@Test
 	public void testGetNoMatch () {
 		connector.setThreshold(1);
 		assertTrue(connector.query("Otters can swim.") == 0);
 		assertTrue(connector.query("") == 0);
 		String tmp = null;
 		assertTrue(connector.query(tmp) == 0);
 	}
 
 	@Test
 	public void testGetNoMatchWithCodes () {
 		connector.setThreshold(1);
 		assertTrue(connector.query(createOttersFragment()) == 0);
 		TextFragment tf = new TextFragment();
 		assertTrue(connector.query(tf) == 0);
 	}
 
 	@Test
 	public void testGetExactMatch () {
 		String input = "Elephants cannot fly.";
 		connector.setThreshold(100);
 		assertTrue(connector.query(input) > 0);
 		QueryResult qr = connector.next();
 		assertEquals(input, qr.source.toString());
 		assertEquals("Les \u00e9l\u00e9phants ne peuvent pas voler.", qr.target.toString());
 		assertEquals(100, qr.score);
 	}
 
 	@Test
 	public void testGetExactMatchWithCodes () {
 		TextFragment tf = createElephantsFragment();
 		connector.setThreshold(100);
 		assertTrue(connector.query(tf) > 0);
 		QueryResult qr = connector.next();
 		assertEquals(tf.toString(), qr.source.toString());
 		assertEquals("Les \u00e9l\u00e9phants <b>ne peuvent pas</b> voler.", qr.target.toString());
 		assertEquals(100, qr.score);
 	}
 
 	@Test
 	public void testGetAlmostExactMatchWithCodes () {
 		TextFragment tf = createElephantsFragment();
 		connector.setThreshold(99);
		assertTrue(connector.query(tf) == 3);

		// First exact
 		QueryResult qr = connector.next();
 		assertEquals(100, qr.score);
		
		// Second exact
		qr = connector.next();
		assertEquals(100, qr.score);
		
		// Fuzzy
 		qr = connector.next();
		assertTrue(qr.score!=100);
 	}
 
 	private TextFragment createElephantsFragment () {
 		TextFragment tf = new TextFragment("Elephants ");
 		tf.append(TagType.OPENING, "b", "<b>");
 		tf.append("cannot");
 		tf.append(TagType.CLOSING, "b", "</b>");
 		tf.append(" fly.");
 		return tf;
 	}
 
 	private TextFragment createOttersFragment () {
 		TextFragment tf = new TextFragment("Otters ");
 		tf.append(TagType.OPENING, "b", "<b>");
 		tf.append("can");
 		tf.append(TagType.CLOSING, "b", "</b>");
 		tf.append(" swim.");
 		return tf;
 	}
 
 }
