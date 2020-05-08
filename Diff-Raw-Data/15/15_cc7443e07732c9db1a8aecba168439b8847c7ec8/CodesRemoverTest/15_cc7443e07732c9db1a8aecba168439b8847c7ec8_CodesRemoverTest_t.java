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
 
 package net.okapi.steps.codesremoval;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.steps.codesremoval.CodesRemover;
 import net.sf.okapi.steps.codesremoval.Parameters;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class CodesRemoverTest {
 
 	private CodesRemover remover;
 	private Parameters params;
 	private GenericContent fmt = new GenericContent();
 	
 	@Test
 	public void testSimple () {
 		params = new Parameters();
 		remover = new CodesRemover(params, LocaleId.SPANISH);
 		ITextUnit tu = new TextUnit("id");
 		tu.setSourceContent(createSimpleFragment());
 		tu.setTargetContent(LocaleId.SPANISH, createSimpleFragment());
 		
 		remover.processTextUnit(tu);
 		assertEquals("t1t2t3", tu.toString());
 		assertEquals("t1t2t3", tu.getTarget(LocaleId.SPANISH).toString());
 	}
 	
 	@Test
 	public void testSkipNonTranslatable () {
 		params = new Parameters();
 		remover = new CodesRemover(params, LocaleId.SPANISH);
 		ITextUnit tu = new TextUnit("id");
 		tu.setSourceContent(createSimpleFragment());
 		tu.setTargetContent(LocaleId.SPANISH, createSimpleFragment());
 		params.setIncludeNonTranslatable(false);
 		tu.setIsTranslatable(false);
 		
 		remover.processTextUnit(tu);
 		assertEquals("t1<br/>t2<b>t3</b>", tu.toString());
 		assertEquals(3, tu.getSource().getFirstContent().getCodes().size());
 		assertEquals("t1<br/>t2<b>t3</b>", tu.getTarget(LocaleId.SPANISH).toString());
 		assertEquals(3, tu.getTarget(LocaleId.SPANISH).getFirstContent().getCodes().size());
 	}
 	
 	@Test
 	public void testDontStripSource () {
 		params = new Parameters();
 		params.setStripSource(false);
 		remover = new CodesRemover(params, LocaleId.SPANISH);
 		ITextUnit tu = new TextUnit("id");
 		tu.setSourceContent(createSimpleFragment());
 		tu.setTargetContent(LocaleId.SPANISH, createSimpleFragment());
 		
 		remover.processTextUnit(tu);
 		assertEquals("t1<br/>t2<b>t3</b>", tu.toString());
 		assertEquals(3, tu.getSource().getFirstContent().getCodes().size());
 		assertEquals("t1t2t3", tu.getTarget(LocaleId.SPANISH).toString());
 	}
 	
 	@Test
 	public void testDontStripTarget () {
 		params = new Parameters();
 		params.setStripTarget(false);
 		remover = new CodesRemover(params, LocaleId.SPANISH);
 		ITextUnit tu = new TextUnit("id");
 		tu.setSourceContent(createSimpleFragment());
 		tu.setTargetContent(LocaleId.SPANISH, createSimpleFragment());
 		
 		remover.processTextUnit(tu);
 		assertEquals("t1t2t3", tu.toString());
 		assertEquals("t1<br/>t2<b>t3</b>", tu.getTarget(LocaleId.SPANISH).toString());
 		assertEquals(3, tu.getTarget(LocaleId.SPANISH).getFirstContent().getCodes().size());
 	}
 
 	@Test
 	public void testDontStripSegments () {
 		params = new Parameters();
 		remover = new CodesRemover(params, LocaleId.SPANISH);
 		TextContainer tc = new TextContainer();
 		ISegments segments = tc.getSegments();
 		tc.append("C1"); // Becomes segment
 		segments.append(createSimpleFragment());
 		tc.append("C2");
 		segments.append(createSimpleFragment());
 		
 		remover.processContainer(tc);
 		assertEquals("[C1][t1t2t3]C2[t1t2t3]", fmt.printSegmentedContent(tc, true));
 		assertEquals("t1t2t3", segments.get(1).text.toText());
 		assertEquals("t1t2t3", segments.get(2).text.toText());
 		segments.joinAll();
 		assertEquals("C1t1t2t3C2t1t2t3", tc.toString());
 	}
 
 	@Test
 	public void testKeepCodeRemoveContent () {
 		params = new Parameters();
 		params.setMode(Parameters.KEEPCODE_REMOVECONTENT);
 		remover = new CodesRemover(params, LocaleId.SPANISH);
 		TextFragment tf = createSimpleFragment();
 		
 		remover.processFragment(tf);
 		assertEquals("t1t2t3", tf.toText());
 		assertEquals(3, tf.getCodes().size());
 	}
 	
 	@Test
 	public void testRemoveCodeKeepContent () {
 		params = new Parameters();
 		params.setMode(Parameters.REMOVECODE_KEEPCONTENT);
 		remover = new CodesRemover(params, LocaleId.SPANISH);
 		TextFragment tf = createSimpleFragment();
 		
 		remover.processFragment(tf);
 		assertEquals("t1<br/>t2<b>t3</b>", tf.toText());
 		assertEquals(0, tf.getCodes().size());
 	}
 	
 	@Test
 	public void testRemoveCodeRemoveContent () {
 		params = new Parameters();
 		params.setMode(Parameters.REMOVECODE_REMOVECONTENT);
 		remover = new CodesRemover(params, LocaleId.SPANISH);
 		TextFragment tf = createSimpleFragment();
 		
 		remover.processFragment(tf);
 		assertEquals("t1t2t3", tf.toText());
 		assertEquals(0, tf.getCodes().size());
 	}
 	
 	@Test
 	public void testRemoveCodeRemoveContentWithSpace () {
 		params = new Parameters();
 		params.setMode(Parameters.REMOVECODE_REMOVECONTENT);
 		params.setReplaceWithSpace(true);
 		remover = new CodesRemover(params, LocaleId.SPANISH);
		TextFragment tf = createSimpleFragmentWithSpaces();
 		
 		remover.processFragment(tf);
 		assertEquals("t1 t2t3t4 t5 t6 t7 t8 t9 t10 t11 ", tf.toText());
 		assertEquals(0, tf.getCodes().size());
 	}
 	
 	TextFragment createSimpleFragment () {
 		TextFragment tf = new TextFragment("t1");
 		tf.append(TagType.PLACEHOLDER, "br", "<br/>");
 		tf.append("t2");
 		tf.append(TagType.OPENING, "b", "<b>");
 		tf.append("t3");
 		tf.append(TagType.CLOSING, "b", "</b>");
		return tf;
	}
	
	TextFragment createSimpleFragmentWithSpaces () {
		TextFragment tf = new TextFragment("t1");
		tf.append(TagType.PLACEHOLDER, "br", "<br/>");
		tf.append("t2");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("t3");
		tf.append(TagType.CLOSING, "b", "</b>");
 		tf.append("t4");
 		tf.append(TagType.PLACEHOLDER, "br", "<br>");
 		tf.append("t5");
 		tf.append(TagType.PLACEHOLDER, "br", "<br />");		
 		tf.append("t6");
 		tf.append(TagType.PLACEHOLDER, Code.TYPE_LB, "unspecified");
 		tf.append("t7");
 		tf.append(TagType.PLACEHOLDER, "unspecified", "\n");		
 		tf.append("t8");
 		tf.append(TagType.PLACEHOLDER, "unspecified", "\r");
 		tf.append("t9");
 		tf.append(TagType.PLACEHOLDER, "unspecified", "before\u0085after");
 		tf.append("t10");
 		tf.append(TagType.PLACEHOLDER, "unspecified", "before\u2028after");		
 		tf.append("t11");
 		tf.append(TagType.PLACEHOLDER, "unspecified", "before\u2029after");		
 		return tf;
 	}
 
 }
