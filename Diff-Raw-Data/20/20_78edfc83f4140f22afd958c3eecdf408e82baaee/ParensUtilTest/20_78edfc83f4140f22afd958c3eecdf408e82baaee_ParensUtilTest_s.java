 package ch.sbs.utils.preptools.parens;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Test;
 
 import ch.sbs.utils.preptools.Match;
 import ch.sbs.utils.preptools.RegionSkipper;
 import ch.sbs.utils.preptools.parens.ParensUtil.QuoteOrphanMatcher;
 
 public class ParensUtilTest {
 
 	// null list != empty list
 	@SuppressWarnings("null")
 	@Test(expected = NullPointerException.class)
 	public void testNullList() {
 		List<String> list = null;
 		for (@SuppressWarnings("unused")
 		final String item : list) {
 
 		}
 	}
 
 	// null list != empty list
 	@Test
 	public void testEmtpyList() {
 		List<String> list = new ArrayList<String>();
 		for (@SuppressWarnings({ "unused" })
 		final String item : list) {
 
 		}
 	}
 
 	@Test
 	public void testParens() {
 		final String sample = "« » ‹ › 〈 〉 ( ) [ ] { } ";
 		assertEquals(
 				0,
 				ParensUtil.findOrphans(sample,
 						RegionSkipper.getCommentSkipper()).size());
 	}
 
 	@Test
 	public void testParens1() {
 		final String sample = "« » ‹ › 〈 〉 ( ) [ ] {  ";
 		assertEquals(
 				1,
 				ParensUtil.findOrphans(sample,
 						RegionSkipper.getCommentSkipper()).size());
 	}
 
 	@Test
 	public void testParens2() {
 		final String sample = "« » ‹ › 〈  ( ) [ ] {  ";
 		assertEquals(
 				2,
 				ParensUtil.findOrphans(sample,
 						RegionSkipper.getCommentSkipper()).size());
 	}
 
 	@Test
 	public void testParensLoc() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "« » ‹ › 〈    ) [ ] {  ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		assertEquals(3, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(8, match.startOffset);
 		assertEquals(9, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(13, match.startOffset);
 		assertEquals(14, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(19, match.startOffset);
 		assertEquals(20, match.endOffset);
 	}
 
 	@Test
 	public void testParensLoc2() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "« » ‹ › 〈 (    [ ] {  ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		assertEquals(3, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(8, match.startOffset);
 		assertEquals(9, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(10, match.startOffset);
 		assertEquals(11, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(19, match.startOffset);
 		assertEquals(20, match.endOffset);
 	}
 
 	@Test
 	public void testParensLoc3() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "« » ‹ › 〈 ( )) (( [ ] { } ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(4, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(8, match.startOffset);
 		assertEquals(9, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(13, match.startOffset);
 		assertEquals(14, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(15, match.startOffset);
 		assertEquals(16, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(16, match.startOffset);
 		assertEquals(17, match.endOffset);
 	}
 
 	@Test
 	public void testParensLocMix() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "(( )) (( [   } ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(6, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(0, match.startOffset);
 		assertEquals(1, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(4, match.startOffset);
 		assertEquals(5, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(6, match.startOffset);
 		assertEquals(7, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(7, match.startOffset);
 		assertEquals(8, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(9, match.startOffset);
 		assertEquals(10, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(13, match.startOffset);
 		assertEquals(14, match.endOffset);
 	}
 
 	@Test
 	public void testParensLoc4() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "(( ))";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(2, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(0, match.startOffset);
 		assertEquals(1, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(4, match.startOffset);
 		assertEquals(5, match.endOffset);
 	}
 
 	@Test
 	public void testParensLocQuot() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "«« »»";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(2, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(0, match.startOffset);
 		assertEquals(1, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(4, match.startOffset);
 		assertEquals(5, match.endOffset);
 	}
 
 	@Test
 	public void testParensLocQuot2() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "«« »» ‹ › 〈 〉 ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(2, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(0, match.startOffset);
 		assertEquals(1, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(4, match.startOffset);
 		assertEquals(5, match.endOffset);
 	}
 
 	@Test
 	public void testParensLocQuot3() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "« » ‹ › 〈 〉 ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuot4() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "〈 › « ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(3, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(0, match.startOffset);
 		assertEquals(1, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(2, match.startOffset);
 		assertEquals(3, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals(4, match.startOffset);
 		assertEquals(5, match.endOffset);
 	}
 
 	@Test
 	public void testParensLocQuot5() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "« › » ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(1, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals(2, match.startOffset);
 		assertEquals(3, match.endOffset);
 	}
 
 	@Test
 	public void testParensLocQuotBrlLiteral1() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "<brl:literal>›</brl:literal>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuotBrlLiteral2() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "<brl:literal>(</brl:literal>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuotBrlLiteral3() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "<brl:literal brl:grade=\"0\">(</brl:literal>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuotBrlLiteral4() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "<brl:literal brl:grade=\"1\">(</brl:literal>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuotBrlLiteral5() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "<brl:literal brl:grade=\"2\">(</brl:literal>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuotBrlLiteral() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "<brl:literal>« › »</brl:literal>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		Collections.sort(orphans);
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuot6() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "« ‹ 〈 〉 › » ";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuot7() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "Du, (<brl:literal>(!Sie </brl:literal> wert <brl:literal brl:grade=\"2\">und)]}</brl:literal>) [Eure, Ihre";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		assertEquals(1, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuot7a() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "(<brl:literal></brl:literal>)";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuot8() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 				+ "<dtbook>"
 				+ "<section>"
 				+ "<oh style=\"\"     bla=\"z\"> 2"
 				+ "Du, (<brl:literal>(!Sie </brl:literal> wert <brl:literal brl:grade=\"2\">und)]}</brl:literal>) [Eure, Ihre"
 				+ "</oh><ol>" + "<li>a) «bla»</li>" + "<li>b) «blu»</li></ol>"
 				+ "{ bla }" + "</section>" + "</dtbook>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		assertEquals(3, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuot8a() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "" + "( wert ) [Eure, Ihre" + "<li>a)</li>"
 				+ "<li>b)</li></ol>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		assertEquals(3, orphans.size());
 	}
 
 	@Test
 	public void testParensLocQuot9() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 				+ "<dtbook>" + " <brl:literal brl:grade=\"0\">)</brl:literal>"
 				+ "</dtbook>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		assertEquals(0, orphans.size());
 	}
 
 	@Test
 	public void testParensOffset() {
 		// ------------------------------1---------2
 		// --------012345678901234567890123456789
 		final String sample = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 				+ "<dtbook>" + ")" + "</dtbook>";
 		List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		assertEquals(1, orphans.size());
 		Match match = orphans.get(0);
 		final int indexOf = sample.indexOf(')');
 		assertEquals(indexOf, match.startOffset);
 		assertEquals(indexOf + 1, match.endOffset);
 		assertEquals(
 				1,
 				ParensUtil.findOrphans(sample, indexOf,
 						RegionSkipper.getCommentSkipper()).size());
 		assertEquals(
 				0,
 				ParensUtil.findOrphans(sample, indexOf + 1,
 						RegionSkipper.getCommentSkipper()).size());
 	}
 
 	@Test
 	public void testParensLocQuot10() {
 		final String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 				+ "<dtbook>";
 		final String middle = "(<brl:literal></brl:literal>)";
 		// ----------------------------------------------1---------2
 		// ------------------------------------012345678901234567890123456789
 		final String sample = head + middle + "<li>a)</li><li>b)</li>"
 				+ "</dtbook>";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getCommentSkipper());
 		assertEquals(2, orphans.size());
 		int i = 0;
 		Match match = orphans.get(i++);
 		assertEquals((head + middle).length() + 5, match.startOffset);
 		assertEquals((head + middle).length() + 6, match.endOffset);
 		match = orphans.get(i++);
 		assertEquals((head + middle).length() + 16, match.startOffset);
 		assertEquals((head + middle).length() + 17, match.endOffset);
 	}
 
 	@Test
 	public void testBug1246QuoteOrphan() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "›a‹ b‹ »c« ›d‹";
 		final QuoteOrphanMatcher quoteOrphanMatcher = new QuoteOrphanMatcher();
 		final List<Match> orphans = quoteOrphanMatcher.findOrphans(sample, 0,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		assertEquals(1, orphans.size());
 		int i = 0;
 		final Match match = orphans.get(i++);
 		assertEquals(5, match.startOffset);
 	}
 
 	@Test
 	public void testBug1246QuoteOrphan2() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "〉a〈 b〈 »c« 〉d〈";
 		final QuoteOrphanMatcher quoteOrphanMatcher = new QuoteOrphanMatcher();
 		final List<Match> orphans = quoteOrphanMatcher.findOrphans(sample, 0,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		assertEquals(1, orphans.size());
 		int i = 0;
 		final Match match = orphans.get(i++);
 		assertEquals(5, match.startOffset);
 	}
 
 	@Test
 	public void testBug1246QuoteOrphanSwappedQuotes() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "»a« b« ›c‹ »d«";
 		final List<Match> orphans = new QuoteOrphanMatcher().findOrphans(
 				sample, 0, RegionSkipper.getLiteralAndCommentSkipper());
 		assertEquals(1, orphans.size());
 		int i = 0;
 		final Match match = orphans.get(i++);
 		assertEquals(5, match.startOffset);
 	}
 
 	@Test
 	public void testBug1246() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "›a‹ b‹ »c« ›d‹";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		assertEquals(1, orphans.size());
 		int i = 0;
 		final Match match = orphans.get(i++);
 		assertEquals(5, match.startOffset);
 	}
 
 	@Test
 	public void testBug1246swappedQuotes() {
 		// ------------------------------1---------2
 		// --------------------012345678901234567890123456789
 		final String sample = "»a« b« ›c‹ »d«";
 		final List<Match> orphans = ParensUtil.findOrphans(sample,
 				RegionSkipper.getLiteralAndCommentSkipper());
 		assertEquals(1, orphans.size());
 		int i = 0;
 		final Match match = orphans.get(i++);
 		assertEquals(5, match.startOffset);
 	}
 }
