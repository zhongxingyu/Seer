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
 
 package net.sf.okapi.common.filterwriter;
 
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class GenericContentTest {
 
 	private GenericContent fmt;
 	
 	@Before
 	public void setUp() throws Exception {
 		fmt = new GenericContent();
 	}
 	
 	@Test
 	public void testSimple_Default () {
 		TextFragment tf = createTextFragment();
 		assertEquals(tf.getCodes().size(), 5);
 		String gtext = fmt.setContent(tf).toString();
 		assertEquals("t1<1><2><3/>t2</2></1>t3", gtext);
 		// Reconstruct it
 		TextFragment tf2 = tf.clone();
 		fmt.updateFragment(gtext, tf2, false);
 		assertEquals("t1<1><2><3/>t2</2></1>t3", fmt.setContent(tf2).toString());
 	}
 	
 //	@Test
 //	public void testFromNumericCodedToFragment1 () {
 //		TextFragment tf = createTextFragment();
 //		String gtext = fmt.setContent(tf).toString();
 //		assertEquals("t1<1><2><3/>t2</2></1>t3", gtext);
 //		// Reconstruct it
 //		TextFragment tf2 = fmt.fromNumericCodedToFragment(gtext, tf.getCodes(), false);
 //		assertEquals("t1<1><2><3/>t2</2></1>t3", fmt.setContent(tf2).toString());
 //	}
 	
 //	@Test
 //	public void testFromNumericCodedToFragment2 () {
 //		TextFragment tf = createTextFragment();
 //		StringBuilder tmp = new StringBuilder(fmt.setContent(tf).toString());
 //		assertEquals("t1<1><2><3/>t2</2></1>t3", tmp.toString());
 //		// Reconstruct it (with lost of codes)
 //		tmp.delete(2, 5); // Removes <1>
 //		TextFragment tf2 = fmt.fromNumericCodedToFragment(tmp.toString(), tf.getCodes(), true);
 //		assertEquals("t1<2><3/>t2</2><e1>t3", fmt.setContent(tf2).toString());
 //	}
 	
 	@Test
 	public void testSimple_WithOption () {
 		TextFragment tf = createTextFragment();
 		assertEquals(tf.getCodes().size(), 5);
 		fmt.setContent(tf);
 		assertEquals("t1<b1><b2><x1/>t2</b2></b1>t3", fmt.toString(true));
 		assertEquals("t1<1><2><3/>t2</2></1>t3", fmt.toString(false));
 	}
 	
 	@Test
 	public void testMisOrderedCodes () {
 		TextFragment tf = new TextFragment();
 		tf.append("t1");
 		tf.append(TagType.OPENING, "b1", "<b1>");
 		tf.append("t2");
 		tf.append(TagType.OPENING, "b2", "<b2>");
 		tf.append("t3");
 		tf.append(TagType.CLOSING, "b1", "</b1>");
 		tf.append("t4");
 		tf.append(TagType.CLOSING, "b2", "</b2>");
 		tf.append("t5");
 		fmt.setContent(tf);
 		// Not real XML so mis-ordering is OK
 		assertEquals("t1<b1>t2<b2>t3</b1>t4</b2>t5", fmt.toString(true));
 		String gtext = fmt.toString(false);
 		assertEquals("t1<b1/>t2<b2/>t3<e1/>t4<e2/>t5", gtext);
 		// Reconstruct it
 		TextFragment tf2 = tf.clone();
 		fmt.updateFragment(gtext, tf2, false);
 		assertEquals("t1<b1>t2<b2>t3</b1>t4</b2>t5", fmt.setContent(tf2).toString(true));
 		assertEquals("t1<b1/>t2<b2/>t3<e1/>t4<e2/>t5", fmt.setContent(tf2).toString());
 	}
 	
 	@Test
 	public void testReOrderingCodes () {
 		TextFragment tf = new TextFragment();
 		tf.append("t1");
 		tf.append(TagType.OPENING, "b1", "<b1>");
 		tf.append("t2");
 		tf.append(TagType.OPENING, "b2", "<b2>");
 		tf.append("t3");
 		tf.append(TagType.CLOSING, "b1", "</b1>");
 		tf.append("t4");
 		tf.append(TagType.CLOSING, "b2", "</b2>");
 		tf.append("t5");
 		fmt.setContent(tf);
 		// Not real XML so mis-ordering is OK
 		assertEquals("t1<b1>t2<b2>t3</b1>t4</b2>t5", fmt.toString(true));
 		assertEquals("t1<b1/>t2<b2/>t3<e1/>t4<e2/>t5", fmt.toString(false));
 		// Reconstruct it in a different order
 //TODO
 //		TextFragment tf2 = tf.clone();
 //		fmt.updateFragment("t1<b1/>t2<b2/>t4<e2/>t5 t3<e1/>", tf2, false);
 //		assertEquals("t1<1>t2<2>t4</2>t5 t3</1>", fmt.setContent(tf2).toString());
 	}
 
 	@Test
 	public void testLetterCodedToFragment () {
 		String ori = "t1<g1>t2</g1><g2><x3/>t3<g4>t4</g4>t5</g2>t6<x5/>t<b6/>t<g7>t</g7>t<e6/><x8/><x9/><x10/>";
 		TextFragment tf1 = fmt.fromLetterCodedToFragment(ori, null, false);
 		assertNotNull(tf1);
 		assertEquals(15, tf1.getCodes().size());
 		assertEquals("t1<1>t2</1><2><3/>t3<4>t4</4>t5</2>t6<5/>t<6>t<7>t</7>t</6><8/><9/><10/>", fmt.setContent(tf1).toString());
 		
 		TextFragment tf = createTextFragment();
 		tf1 = fmt.fromLetterCodedToFragment(ori, tf, false);
 		assertEquals(tf, tf1);
 		assertEquals("t1<1>t2</1><2><3/>t3<4>t4</4>t5</2>t6<5/>t<6>t<7>t</7>t</6><8/><9/><10/>", fmt.setContent(tf1).toString());
 	}
 
 	@Test
 	public void testFragmentToLetterCoded () {
 		TextFragment tf1 = createTextFragment();
 		String res = fmt.fromFragmentToLetterCoded(tf1);
 		assertEquals("t1<g1><g2><x3/>t2</g2></g1>t3", res);
 		// Try round trip
 		TextFragment tf2 = fmt.fromLetterCodedToFragment(res, null, false);
 		assertEquals(fmt.setContent(tf1).toString(), fmt.setContent(tf2).toString());
 	}
 
 	@Test
 	public void testDataTransfer () {
 		TextFragment tf = new TextFragment();
 		tf.append("t1");
 		tf.append(TagType.OPENING, "b1", "[b1]");
 		tf.append(TagType.OPENING, "b2", "[b2]");
 		tf.append(TagType.PLACEHOLDER, "x1", "[x/]");
 		tf.append("t2");
 		tf.append(TagType.CLOSING, "b2", "[/b2]");
 		tf.append(TagType.CLOSING, "b1", "[/b1]");
 		tf.append("t3");
 		assertEquals("t1[b1][b2][x/]t2[/b2][/b1]t3", tf.toText());
 		
 		String res = fmt.fromFragmentToLetterCoded(tf);
 		TextFragment tf2 = fmt.fromLetterCodedToFragment(res, tf, true);
 		assertEquals("t1[b1][b2][x/]t2[/b2][/b1]t3", tf2.toText());
 
 		TextFragment tf3 = fmt.fromLetterCodedToFragment(res, tf, false);
 		assertEquals("t1<g1><g2><x3/>t2</g2></g1>t3", tf3.toText());
 	}
 	
 	@Test
 	public void testUpdate () {
 		TextFragment tf = new TextFragment();
 		tf.append(TagType.PLACEHOLDER, "x1", "[x/]");
 		tf.append("A");
 		tf.append(TagType.OPENING, "b1", "[b1]");
 		tf.append("B");
		tf.append(TagType.CLOSING, "b1", "[/b1]");
 		
 		TextFragment tf2 = new TextFragment("", tf.getClonedCodes());
 		fmt.updateFragment("<1/>ZZ<2>QQ</2>", tf2, false);
 		assertEquals("[x/]ZZ[b1]QQ[/b1]", tf2.toText());
 	}
 	
 	private TextFragment createTextFragment () {
 		TextFragment tf = new TextFragment();
 		tf.append("t1");
 		tf.append(TagType.OPENING, "b1", "<b1>");
 		tf.append(TagType.OPENING, "b2", "<b2>");
 		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
 		tf.append("t2");
 		tf.append(TagType.CLOSING, "b2", "</b2>");
 		tf.append(TagType.CLOSING, "b1", "</b1>");
 		tf.append("t3");
 		return tf;
 	}
 	
 }
