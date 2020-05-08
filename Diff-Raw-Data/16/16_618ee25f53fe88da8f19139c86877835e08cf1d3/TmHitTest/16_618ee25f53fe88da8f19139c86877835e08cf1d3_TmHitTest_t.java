 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.tm.pensieve.common;
 
 import static org.junit.Assert.*;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.query.MatchType;
 import net.sf.okapi.common.resource.TextFragment;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * 
  * @author HaslamJD
  * @author HARGRAVEJE
  */
 public class TmHitTest {
 	TmHit h1;
 	TmHit h2;
 
 	TranslationUnitVariant tuvSource1, tuvTarget1, tuvSource2, tuvTarget2;
 	TranslationUnit tu1, tu2;
 
 	@Before
 	public void setUp() {
 		tuvSource1 = new TranslationUnitVariant(new LocaleId("en"), new TextFragment("test1"));
 		tuvTarget1 = new TranslationUnitVariant(new LocaleId("es"), new TextFragment(
 				"prueba1"));
 		tu1 = new TranslationUnit(tuvSource1, tuvTarget1);
 
 		tuvSource2 = new TranslationUnitVariant(new LocaleId("en"), new TextFragment("test2"));
 		tuvTarget2 = new TranslationUnitVariant(new LocaleId("es"), new TextFragment(
 				"prueba2"));
 		tu2 = new TranslationUnit(tuvSource2, tuvTarget2);
 
 		h1 = new TmHit(tu1, MatchType.EXACT, 100.0f);
 		h2 = new TmHit(tu2, MatchType.EXACT, 100.0f);
 	}
 
 	@Test
 	public void noArgConstructor() {
 		TmHit tmh = new TmHit();
 		assertNull(tmh.getTu());
 		assertEquals(0.0f, tmh.getScore(), 0.001f);
 		assertTrue(MatchType.UKNOWN == tmh.getMatchType());
 	}
 
 	@Test
 	public void constructor() {
 		assertNotNull(h1.getTu());
 		assertNotNull(h1.getScore());
 		assertNotNull(h1.getMatchType());
 	}
 
 	@Test
 	public void instanceEquality() {
 		TmHit h1 = new TmHit();
 		TmHit h2 = h1;
 		assertTrue("instance equality", h1.equals(h2));
 	}
 
 	@Test
 	public void equals() {		
 		TmHit h = new TmHit();
 		h.setTu(tu1);
 		h.setScore(80.0f);
 		h.setMatchType(MatchType.EXACT);
 		assertTrue("equals", h1.equals(h));
 	}
 
 	@Test
 	public void notEquals() {
 		assertFalse("not equals", h1.equals(h2));
 	}
 	
 	@Test 
 	public void compareToEquals() {
 		TmHit h = new TmHit();
 		h.setTu(tu1);
 		h.setScore(100.0f);
 		h.setMatchType(MatchType.EXACT);
 		assertEquals(0, h1.compareTo(h1));
 	}
 	
 	@Test 
 	public void compareToGreaterThanScore() {
 		TmHit h = new TmHit();
 		h.setTu(tu1);
 		h.setScore(50.0f);
 		h.setMatchType(MatchType.EXACT);
 		assertTrue(h1.compareTo(h) < 0);
 	}
 	
 	@Test 
 	public void compareToLessThanScore() {
 		TmHit h = new TmHit();
 		h.setTu(tu1);
 		h.setScore(80.0f);
 		h.setMatchType(MatchType.EXACT);
 		assertTrue(h1.compareTo(h) < 0);
 	}
 	
 	@Test 
 	public void compareToLessThanMatchType() {
 		TmHit h = new TmHit();
 		h.setTu(tu1);
 		h.setScore(80.0f);
 		h.setMatchType(MatchType.FUZZY);
 		assertTrue(h1.compareTo(h) < 0);
 	}
 	
 	@Test 
 	public void compareToGreaterThanMatchType() {
 		TmHit h = new TmHit();
 		h.setTu(tu1);
 		h.setScore(80.0f);
		h.setMatchType(MatchType.EXACT_LOCAL_CONTEXT);
 		assertTrue(h1.compareTo(h) > 0);
 	}
 	
 	@Test 
 	public void compareToLessThanSource() {
 		TmHit h = new TmHit();
 		TranslationUnitVariant tuvSource = new TranslationUnitVariant(new LocaleId("en"), new TextFragment("aest1"));
 		TranslationUnitVariant tuvTarget = new TranslationUnitVariant(new LocaleId("es"), new TextFragment("prueba1"));
 		TranslationUnit tu = new TranslationUnit(tuvSource, tuvTarget);
 		
 		h.setTu(tu);
 		h.setScore(80.0f);
 		h.setMatchType(MatchType.FUZZY);
 		assertTrue(h1.compareTo(h) < 0);
 	}
 	
 	@Test 
 	public void compareToGreaterThanSource() {
 		TmHit h = new TmHit();
 		TranslationUnitVariant tuvSource = new TranslationUnitVariant(new LocaleId("en"), new TextFragment("zest1"));
 		TranslationUnitVariant tuvTarget = new TranslationUnitVariant(new LocaleId("es"), new TextFragment("prueba1"));
 		TranslationUnit tu = new TranslationUnit(tuvSource, tuvTarget);
 
 		
 		h.setTu(tu);
 		h.setScore(80.0f);
		h.setMatchType(MatchType.EXACT_LOCAL_CONTEXT);
 		assertTrue(h1.compareTo(h) > 0);
 	}
 }
