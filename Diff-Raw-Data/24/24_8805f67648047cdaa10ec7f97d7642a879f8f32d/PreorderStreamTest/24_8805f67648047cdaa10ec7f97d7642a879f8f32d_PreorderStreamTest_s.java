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
 package com.toolazydogs.aunit.internal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import org.junit.Test;
 
 
 /**
  * @version $Revision: $ $Date: $
  */
 public class PreorderStreamTest
 {
     @Test
     public void testABC() throws Exception
     {
         PreorderStream stream = new PreorderStream("(A B C)");
 
         stream.leftparen();
         assertEquals("A", stream.token());
         assertEquals("B", stream.token());
         assertEquals("C", stream.token());
         stream.rightparen();
         stream.done();
     }
 
     @Test
     public void testSpaces() throws Exception
     {
         PreorderStream stream = new PreorderStream("  ( A   B   C   )   ");
 
         stream.leftparen();
         assertEquals("A", stream.token());
         assertEquals("B", stream.token());
         assertEquals("C", stream.token());
         stream.rightparen();
         stream.done();
     }
 
     @Test
     public void testNonCharacters() throws Exception
     {
         PreorderStream stream = new PreorderStream("  ( +    1   2   )   ");
 
         stream.leftparen();
         assertEquals("+", stream.token());
         assertEquals("1", stream.token());
         assertEquals("2", stream.token());
         stream.rightparen();
         stream.done();
     }
 
     @Test
     public void testQuotes() throws Exception
     {
         PreorderStream stream = new PreorderStream("  ( +   ' 1   2  ' )   ");
 
         stream.leftparen();
         assertEquals("+", stream.token());
         assertEquals(" 1   2  ", stream.token());
         stream.rightparen();
         stream.done();
     }
 
     @Test
     public void testEscapedQuotes() throws Exception
     {
         PreorderStream stream = new PreorderStream("  ( +   ' 1 \\'\\\\  2  ' )   ");
 
         stream.leftparen();
         assertEquals("+", stream.token());
         assertEquals(" 1 '\\  2  ", stream.token());
         stream.rightparen();
         stream.done();
     }
 
     @Test
     public void testLeftoverParen() throws Exception
     {
         PreorderStream stream = new PreorderStream("  ( +    1   2  )  )  ");
 
         stream.leftparen();
         assertEquals("+", stream.token());
         assertEquals("1", stream.token());
         assertEquals("2", stream.token());
         stream.rightparen();
         try
         {
             stream.done();
             fail("Should have failed on extra characters");
         }
         catch (PreorderException pe)
         {
         }
     }
 
     @Test
     public void testLeftoverCharacters() throws Exception
     {
         PreorderStream stream = new PreorderStream("  ( +   ( 1) (  2  ) ) abc  ");
 
         stream.leftparen();
         assertEquals("+", stream.token());
         stream.leftparen();
         assertEquals("1", stream.token());
         stream.rightparen();
         stream.leftparen();
         assertEquals("2", stream.token());
         stream.rightparen();
         stream.rightparen();
         try
         {
             stream.done();
             fail("Should have failed on extra characters");
         }
         catch (PreorderException pe)
         {
         }
     }
 
     @Test
     public void testTooManyTokens() throws Exception
     {
         PreorderStream stream = new PreorderStream("  ( +   ( 1 asd) (  2  ) )  ");
 
         stream.leftparen();
         assertEquals("+", stream.token());
         stream.leftparen();
         assertEquals("1", stream.token());
         try
         {
             stream.rightparen();
             fail("Should have failed on extra token");
         }
         catch (PreorderException pe)
         {
         }
     }
 
     @Test
     public void testMissingParen() throws Exception
     {
         PreorderStream stream = new PreorderStream("   CAR ( +   ( 1 asd) (  2  ) )  ");
 
         try
         {
             stream.leftparen();
             fail("Should have failed on missing paren");
         }
         catch (PreorderException pe)
         {
         }
     }
 }
