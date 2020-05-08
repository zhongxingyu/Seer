 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.tokenizer;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.MDC;
 import ru.histone.HistoneTokensHolder;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 public class TokenizerCommentsTest {
     private static final String MDC_TEST_NAME = "testCaseName";
     private TokenizerFactory tokenizerFactory;
 
     @Before
     public void init() {
         this.tokenizerFactory = new TokenizerFactory(HistoneTokensHolder.getTokens());
     }
 
 
 	@Test
 	public void fragmentCommentFragment() {
 
 		String input;
 		Token token;
 
 		input = "fragment1{{*comment*}}fragment2";
 
 		Tokenizer tokenizer = tokenizerFactory.match(input);
 
 		MDC.put(MDC_TEST_NAME, "fragment1");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(1, token.getPos());
 		assertEquals("fragment1", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "{{*");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_START));
 		token = tokenizer.next(TokenType.T_COMMENT_START);
 		assertNotNull(token);
 		assertEquals(10, token.getPos());
 		assertEquals("{{*", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "comment");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(13, token.getPos());
 		assertEquals("comment", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "*}}");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_END));
 		token = tokenizer.next(TokenType.T_COMMENT_END);
 		assertNotNull(token);
 		assertEquals(20, token.getPos());
 		assertEquals("*}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "comment");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(23, token.getPos());
 		assertEquals("fragment2", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "EOF");
 		assertTrue(tokenizer.isNext(TokenType.T_EOF));
 	}
 	
 	@Test
 	public void blocksStartsWithExpr() {
 
 		String input;
 		Token token;
 
 		input = "{{*comment*}}fragment";
 
 		Tokenizer tokenizer = tokenizerFactory.match(input);
 
 		MDC.put(MDC_TEST_NAME, "{{*");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_START));
 		token = tokenizer.next(TokenType.T_COMMENT_START);
 		assertNotNull(token);
 		assertEquals(1, token.getPos());
 		assertEquals("{{*", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "comment");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(4, token.getPos());
 		assertEquals("comment", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "*}}");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_END));
 		token = tokenizer.next(TokenType.T_COMMENT_END);
 		assertNotNull(token);
 		assertEquals(11, token.getPos());
 		assertEquals("*}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "comment");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(14, token.getPos());
 		assertEquals("fragment", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "EOF");
 		assertTrue(tokenizer.isNext(TokenType.T_EOF));
 	}
 
 	@Test
 	public void blocksEndsWithComment() {
 
 		String input;
 		Token token;
 
 		input = "fragment{{*comment*}}";
 
 		Tokenizer tokenizer = tokenizerFactory.match(input);
 
 		MDC.put(MDC_TEST_NAME, "fragment2");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(1, token.getPos());
 		assertEquals("fragment", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "{{*");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_START));
 		token = tokenizer.next(TokenType.T_COMMENT_START);
 		assertNotNull(token);
 		assertEquals(9, token.getPos());
 		assertEquals("{{*", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "comment");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(12, token.getPos());
 		assertEquals("comment", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "*}}");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_END));
 		token = tokenizer.next(TokenType.T_COMMENT_END);
 		assertNotNull(token);
 		assertEquals(19, token.getPos());
 		assertEquals("*}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "EOF");
 		assertTrue(tokenizer.isNext(TokenType.T_EOF));
 	}
 
 	@Test
 	public void commentWithoutClosingToken() {
 
 		String input;
 		Token token;
 
 		input = "{{*comment without closing token";
 
 		Tokenizer tokenizer = tokenizerFactory.match(input);
 
 		MDC.put(MDC_TEST_NAME, "{{*");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_START));
 		token = tokenizer.next(TokenType.T_COMMENT_START);
 		assertNotNull(token);
 		assertEquals(1, token.getPos());
 		assertEquals("{{*", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "text");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(4, token.getPos());
 		assertEquals("comment without closing token", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "EOF");
 		assertTrue(tokenizer.isNext(TokenType.T_EOF));
 	}
 
 	@Test
 	public void commentWithoutOpeningToken() {
 
 		String input;
 		Token token;
 
 		input = "{{*comment*}} without opening token*}}";
 
 		Tokenizer tokenizer = tokenizerFactory.match(input);
 
 		MDC.put(MDC_TEST_NAME, "{{*");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_START));
 		token = tokenizer.next(TokenType.T_COMMENT_START);
 		assertNotNull(token);
 		assertEquals(1, token.getPos());
 		assertEquals("{{*", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "comment");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(4, token.getPos());
 		assertEquals("comment", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "*}}");
 		assertTrue(tokenizer.isNext(TokenType.T_COMMENT_END));
 		token = tokenizer.next(TokenType.T_COMMENT_END);
 		assertNotNull(token);
 		assertEquals(11, token.getPos());
 		assertEquals("*}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "text");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(14, token.getPos());
		assertEquals(" without opening token*}}", token.getContent());

//		MDC.put(MDC_TEST_NAME, "*}}");
//		assertTrue(tokenizer.isNext(TokenType.TPL_COMMENT_WO_OPENNING));
//		token = tokenizer.next(TokenType.TPL_COMMENT_WO_OPENNING);
//		assertNotNull(token);
//		assertEquals(36, token.getPos());
//		assertEquals("*}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "EOF");
 		assertTrue(tokenizer.isNext(TokenType.T_EOF));
 	}
 
 }
