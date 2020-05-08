 package au.com.miskinhill.search.tokenizer;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.apache.lucene.analysis.Token;
 import org.junit.Test;
 
 import com.hp.hpl.jena.rdf.model.Literal;
 
 public class StringLiteralTokenizerUnitTest {
 
 	@Test
 	public void testEnglish() throws Exception {
 		Literal text = createMock(Literal.class);
 		expect(text.getString()).andReturn("Hello to the World!").anyTimes();
 		expect(text.getLanguage()).andReturn("en").anyTimes();
 		replay(text);
 		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
 		Token tok = new Token();
 		assertEquals(new Token("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
 		assertEquals(new Token("world", 13, 18, "<ALPHANUM>"), t.next(tok));
 		assertEquals(null, t.next(tok));
 	}
 
 	@Test
 	public void testEnglishWithLocaleThingy() throws Exception {
 		Literal text = createMock(Literal.class);
 		expect(text.getString()).andReturn("Hello to the World!").anyTimes();
 		expect(text.getLanguage()).andReturn("en-AU").anyTimes();
 		replay(text);
 		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
 		Token tok = new Token();
 		assertEquals(new Token("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
 		assertEquals(new Token("world", 13, 18, "<ALPHANUM>"), t.next(tok));
 		assertEquals(null, t.next(tok));
 	}
 
 	@Test
 	public void testNonexistentLanguage() throws Exception {
 		Literal text = createMock(Literal.class);
 		expect(text.getString()).andReturn("Hello to the World!").anyTimes();
 		expect(text.getLanguage()).andReturn("xx").anyTimes();
 		replay(text);
 		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
 		Token tok = new Token();
 		assertEquals(new Token("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
 		assertEquals(new Token("to", 6, 8, "<ALPHANUM>"), t.next(tok));
 		assertEquals(new Token("the", 9, 12, "<ALPHANUM>"), t.next(tok));
 		assertEquals(new Token("world", 13, 18, "<ALPHANUM>"), t.next(tok));
 		assertEquals(null, t.next(tok));
 	}
 	
 	@Test
 	public void testLiteralTypeLookup() throws Exception {
 		Literal text = createMock(Literal.class);
 		expect(text.getString()).andReturn("0").anyTimes();
 		expect(text.getLanguage()).andReturn("en").anyTimes();
 		expect(text.getDatatypeURI()).andReturn(null).anyTimes();
 		replay(text);
 		RDFLiteralTokenizer t = RDFLiteralTokenizer.fromLiteral(text);
 		assertTrue(t instanceof StringLiteralTokenizer);
 	}
 	
 }
