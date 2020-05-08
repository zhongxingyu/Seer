 /**
  *
  * Copyright 2010 (C) The original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.toolazydogs.aunit;
 
 import junit.framework.AssertionFailedError;
 import org.antlr.runtime.BaseRecognizer;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.tree.Tree;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import com.toolazydogs.aunit.internal.PreorderException;
 import com.toolazydogs.aunit.internal.PreorderStream;
 
 
 /**
  * @author Jeremy D. Frens (jdfrens@users.sourceforge.net)
  * @author Alan D. Cabrera (adc@toolazydogs.com)
  * @version $Revision: $ $Date: $
  */
 public class Assert
 {
     /**
      * Asserts the token produced by an ANTLR tester.
      *
      * @param message      the message to display on failure.
      * @param expectedType the expected type of the token.
      * @param expectedText the expected text of the token.
      * @param lexerResults the result of {@link Work#scan(String)} which will
      *                     produce the token to assert.
      */
     public static void assertToken(String message, int expectedType, String expectedText, LexerResults lexerResults)
     {
         assertToken(message, expectedType, expectedText, lexerResults.getToken());
     }
 
     /**
      * Asserts the token produced by an ANTLR tester.
      *
      * @param message         the message to display on failure.
      * @param expectedChannel the channel the token should appear on.
      * @param expectedType    the expected type of the token.
      * @param expectedText    the expected text of the token.
      * @param lexerResults    the result of {@link Work#scan(String)} which will
      *                        produce the token to assert.
      */
     public static void assertToken(String message, int expectedChannel, int expectedType, String expectedText, LexerResults lexerResults)
     {
         assertToken(message, expectedChannel, expectedType, expectedText, lexerResults.getToken());
     }
 
     /**
      * Asserts the token produced by an ANTLR tester.
      *
      * @param expectedType the expected type of the token.
      * @param expectedText the expected text of the token.
      * @param lexerResults the result of {@link Work#scan(String)} which will
      *                     produce the token to assert.
      */
     public static void assertToken(int expectedType, String expectedText, LexerResults lexerResults)
     {
         assertToken(expectedType, expectedText, lexerResults.getToken());
     }
 
     /**
      * Asserts the token produced by an ANTLR tester.
      *
      * @param expectedChannel the channel the token should appear on.
      * @param expectedType    the expected type of the token.
      * @param expectedText    the expected text of the token.
      * @param lexerResults    the result of {@link Work#scan(String)} which will
      *                        produce the token to assert.
      */
     public static void assertToken(int expectedChannel, int expectedType, String expectedText, LexerResults lexerResults)
     {
         assertToken(expectedChannel, expectedType, expectedText, lexerResults.getToken());
     }
 
     /**
      * Asserts properties of a token.
      *
      * @param message      the message to display on failure.
      * @param expectedType the expected type of the token.
      * @param expectedText the expected text of the token.
      * @param token        the token to assert.
      */
     public static void assertToken(String message, int expectedType, String expectedText, Token token)
     {
         assertToken(message, BaseRecognizer.DEFAULT_TOKEN_CHANNEL, expectedType, expectedText, token);
     }
 
     /**
      * Asserts properties of a token.
      *
      * @param message         the message to display on failure.
      * @param expectedChannel the channel the token should appear on.
      * @param expectedType    the expected type of the token.
      * @param expectedText    the expected text of the token.
      * @param token           the token to assert.
      */
     public static void assertToken(String message, int expectedChannel, int expectedType, String expectedText, Token token)
     {
         assertEquals(message + " (channel check)", expectedChannel, token.getChannel());
         assertEquals(message + " (type check)", expectedType, token.getType());
         assertEquals(message + " (text check)", expectedText, token.getText());
     }
 
     /**
      * Asserts properties of a token.
      *
      * @param expectedType the expected type of the token.
      * @param expectedText the expected text of the token.
      * @param token        the token to assert.
      */
     public static void assertToken(int expectedType, String expectedText, Token token)
     {
         assertToken(BaseRecognizer.DEFAULT_TOKEN_CHANNEL, expectedType, expectedText, token);
     }
 
     /**
      * Asserts properties of a token.
      *
      * @param expectedChannel the channel the token should appear on.
      * @param expectedType    the expected type of the token.
      * @param expectedText    the expected text of the token.
      * @param token           the token to assert.
      */
     public static void assertToken(int expectedChannel, int expectedType, String expectedText, Token token)
     {
         assertEquals(expectedChannel, token.getChannel());
         assertEquals("failed to match token types,", expectedType, token.getType());
         assertEquals("failed to match token text,", expectedText, token.getText());
     }
 
     /**
      * To "refute" a token means to assert that it cannot be of the specified
      * type. This is useful, for example, when you want to assert that "x" will
      * not be recognized as an integer. Eventually, it may be recognized as an
      * identifier or some other token, but you just want to assert that it's
      * definitely not an integer.
      * <p/>
      * <pre>
      * refuteToken(MyLexer.INTEGER, myTester.scanInput(&quot;x&quot;));
      * </pre>
      *
      * @param refutedType  the type the token should <em>not</em> be.
      * @param lexerResults the result of scanning input with the tester.
      */
     public static void refuteToken(int refutedType, LexerResults lexerResults)
     {
         try
         {
             if (refutedType == lexerResults.getToken().getType())
             {
                 fail("scanned successfully as specified type");
             }
         }
         catch (AssertionError e)
         {
             if (checkMessage(e.getMessage()))
             {
                 // things are good
             }
             else
             {
                 throw e;
             }
         }
     }
 
     /**
      * To "refute" a parse means that the scan cannot be parsed with the
      * specified production.
      * <p/>
      * <pre>
      * refuteParse(&quot;program&quot;, myTester.scanInput(&quot;5 / * 8&quot;));
      * </pre>
      *
      * @param production   the production to apply from the parser.
      * @param lexerResults the result of scanning input with the tester.
      */
     public static void refuteParse(String production, LexerResults lexerResults)
     {
         try
         {
             lexerResults.parseAs(production);
             fail("parsed as " + production);
         }
         catch (AssertionError e)
         {
             if (checkMessage(e.getMessage()))
             {
                 // things are good
             }
             else
             {
                 throw e;
             }
         }
     }
 
     private static boolean checkMessage(String message)
     {
         return message.startsWith("failed to match EOF")
                || message.startsWith("unexpected error output")
                || message.startsWith("parsing does not consume all tokens");
     }
 
     /**
      * Asserts a parse tree.
      *
      * @param rootType     the type of the root of the tree.
      * @param preorder     the preorder traversal of the tree.
      * @param parseResults a helper class
      */
     public static void assertTree(int rootType, String preorder, ParseResults parseResults)
     {
         assertTree(rootType, preorder, parseResults.getTree());
     }
 
     /**
      * Asserts a parse tree.
      *
      * @param rootType the type of the root of the tree.
      * @param preorder the preorder traversal of the tree.
      * @param tree     an ANTLR tree to assert on.
      */
     public static void assertTree(int rootType, String preorder, Tree tree)
     {
         assertNotNull("tree should be non-null", tree);
         assertPreordered("asserting preorder", preorder, tree);
         assertEquals(rootType, tree.getType());
     }
 
     /**
      * Asserts a parse tree.
      *
      * @param message      the message to display on failure.
      * @param rootType     the type of the root of the tree.
      * @param preorder     the preorder traversal of the tree.
      * @param parseResults a helper class
      */
     public static void assertTree(String message, int rootType, String preorder, ParseResults parseResults)
     {
         assertTree(message, rootType, preorder, parseResults.getTree());
     }
 
     /**
      * Asserts a parse tree.
      *
      * @param message  the message to display on failure.
      * @param rootType the type of the root of the tree.
      * @param preorder the preorder traversal of the tree.
      * @param tree     an ANTLR tree to assert on.
      */
     public static void assertTree(String message, int rootType, String preorder, Tree tree)
     {
         assertNotNull("tree should be non-null", tree);
         assertEquals(message + " (asserting type of root)", rootType, tree.getType());
         assertPreordered(message + " (asserting preorder)", preorder, tree);
     }
 
     private static void assertPreordered(String message, String preorder, Tree tree)
     {
         try
         {
             PreorderStream stream = new PreorderStream(preorder);
             assertPreordered(message, stream, tree);
             stream.done();
         }
         catch (PreorderException e)
         {
             throw new AssertionFailedError(message + " " + e.getMessage());
         }
     }
 
     private static void assertPreordered(String message, PreorderStream preorder, Tree tree)
     {
         try
         {
             if (tree == null)
             {
                 assertEquals(message, preorder.token(), "<NULL!!!!>");
             }
             else if (tree.isNil())
             {
                 assertEquals(message, preorder.token(), "<nil>");
             }
             else
             {
                 preorder.leftparen();
 
                 assertEquals(message, preorder.token(), tree.getText());
 
                 for (int i = 0; i < tree.getChildCount(); i++)
                 {
                    Tree child = tree.getChild(i);

                    if (child.getChildCount() == 0) assertEquals(message, preorder.token(), child.getText());
                    else assertPreordered(message, preorder, tree.getChild(i));
                 }
 
                 preorder.rightparen();
             }
         }
         catch (PreorderException e)
         {
             throw new AssertionFailedError(message + " " + e.getMessage());
         }
     }
 
     private Assert() { }
 }
