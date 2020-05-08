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
 
 public class TokenizerBlocksTest {
     private static final String MDC_TEST_NAME = "testCaseName";
     private TokenizerFactory tokenizerFactory;
 
     @Before
     public void init() {
         this.tokenizerFactory = new TokenizerFactory(HistoneTokensHolder.getTokens());
     }
 
 	@Test
 	public void blocksStartsWithExpr() {
 		String input = "fragmnet{{dsfdsf}}fragment";
 
 		Tokenizer tokenizer = tokenizerFactory.match(input);
 
 		MDC.put(MDC_TEST_NAME, "fragmnet");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 
         Token token = tokenizer.next(TokenType.T_FRAGMENT);
         assertNotNull(token);
         assertEquals(1, token.getPos());
         assertEquals("fragmnet", token.getContent());
 
         MDC.put(MDC_TEST_NAME, "{{");
         assertTrue(tokenizer.isNext(TokenType.T_BLOCK_START));
 
         token = tokenizer.next(TokenType.T_BLOCK_START);
 		assertNotNull(token);
 		assertEquals(9, token.getPos());
 		assertEquals("{{", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "ident");
 		assertTrue(tokenizer.isNext(TokenType.EXPR_IDENT));
 		token = tokenizer.next(TokenType.EXPR_IDENT);
 		assertNotNull(token);
 		assertEquals(11, token.getPos());
 		assertEquals("dsfdsf", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "}}");
 		assertTrue(tokenizer.isNext(TokenType.T_BLOCK_END));
 		token = tokenizer.next(TokenType.T_BLOCK_END);
 		assertNotNull(token);
 		assertEquals(17, token.getPos());
 		assertEquals("}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "fragment2");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(19, token.getPos());
 		assertEquals("fragment", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "EOF");
 		assertTrue(tokenizer.isNext(TokenType.T_EOF));
 	}
 
 	@Test
 	public void blocksEndsWithExpr() {
 
 		String input;
 		Token token;
 
 		input = "fragment{{ident}}";
 
 		Tokenizer tokenizer = tokenizerFactory.match(input);
 
 		MDC.put(MDC_TEST_NAME, "fragment2");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(1, token.getPos());
 		assertEquals("fragment", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "{{");
 		assertTrue(tokenizer.isNext(TokenType.T_BLOCK_START));
 		token = tokenizer.next(TokenType.T_BLOCK_START);
 		assertNotNull(token);
 		assertEquals(9, token.getPos());
 		assertEquals("{{", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "ident");
 		assertTrue(tokenizer.isNext(TokenType.EXPR_IDENT));
 		token = tokenizer.next(TokenType.EXPR_IDENT);
 		assertNotNull(token);
 		assertEquals(11, token.getPos());
 		assertEquals("ident", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "}}");
 		assertTrue(tokenizer.isNext(TokenType.T_BLOCK_END));
 		token = tokenizer.next(TokenType.T_BLOCK_END);
 		assertNotNull(token);
 		assertEquals(16, token.getPos());
 		assertEquals("}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "EOF");
 		assertTrue(tokenizer.isNext(TokenType.T_EOF));
 	}
 
 	@Test
 	public void blockWithoutOpeningToken() {
 
 		String input;
 		Token token;
 
 		input = "{{ident}} without opening token}}";
 
 		Tokenizer tokenizer = tokenizerFactory.match(input);
 
 		MDC.put(MDC_TEST_NAME, "{{");
 		assertTrue(tokenizer.isNext(TokenType.T_BLOCK_START));
 		token = tokenizer.next(TokenType.T_BLOCK_START);
 		assertNotNull(token);
 		assertEquals(1, token.getPos());
 		assertEquals("{{", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "ident");
 		assertTrue(tokenizer.isNext(TokenType.EXPR_IDENT));
 		token = tokenizer.next(TokenType.EXPR_IDENT);
 		assertNotNull(token);
 		assertEquals(3, token.getPos());
 		assertEquals("ident", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "}}");
 		assertTrue(tokenizer.isNext(TokenType.T_BLOCK_END));
 		token = tokenizer.next(TokenType.T_BLOCK_END);
 		assertNotNull(token);
 		assertEquals(8, token.getPos());
 		assertEquals("}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "comment");
 		assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
 		token = tokenizer.next(TokenType.T_FRAGMENT);
 		assertNotNull(token);
 		assertEquals(10, token.getPos());
 		assertEquals(" without opening token}}", token.getContent());
 
        MDC.put(MDC_TEST_NAME, "}}");
        assertTrue(tokenizer.isNext(TokenType.T_BLOCK_END));
        token = tokenizer.next(TokenType.T_BLOCK_END);
        assertNotNull(token);
        assertEquals(32, token.getPos());
        assertEquals("}}", token.getContent());
 
 		MDC.put(MDC_TEST_NAME, "EOF");
 		assertTrue(tokenizer.isNext(TokenType.T_EOF));
 	}
 
 }
