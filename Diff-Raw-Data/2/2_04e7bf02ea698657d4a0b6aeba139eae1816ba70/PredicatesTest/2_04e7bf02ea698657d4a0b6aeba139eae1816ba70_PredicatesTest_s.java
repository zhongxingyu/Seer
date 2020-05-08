 /*
  * Copyright (C) 2012 Yannick LOTH, LittleJ [www.littlej.biz]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package biz.littlej.jreqs.predicates;
 
 import org.junit.Test;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Unit tests for {@link Predicates}.
  *
  * @author Yannick LOTH
  * @since 0.1.0
  */
 public class PredicatesTest {
     @Test
     public void testAlwaysFalse() {
         {
             final Object o = new Object();
             assertFalse("AlwaysFalse predicate must always evaluate to false.", Predicates.alwaysFalse().apply(o));
         }
         assertFalse("AlwaysFalse predicate must always evaluate to false.", Predicates.alwaysFalse().apply(null));
     }
 
     @Test
     public void testAlwaysTrue() {
         {
             final Object o = new Object();
             assertTrue("AlwaysTrue predicate must always evaluate to true.", Predicates.alwaysTrue().apply(o));
         }
         assertTrue("AlwaysTrue predicate must always evaluate to true.", Predicates.alwaysTrue().apply(null));
     }
 
     @Test
     public void testZero() {
         {
             final int zero = 0;
             assertTrue("0 must evaluate to true.", Predicates.zero().apply(zero));
         }
         {
             final BigInteger zeroBigInteger = BigInteger.ZERO;
             assertTrue("zero BigInteger must evaluate to true.", Predicates.zero().apply(zeroBigInteger));
         }
         {
             final BigDecimal zeroBigDecimal = BigDecimal.ZERO;
             assertTrue("zero BigDecimal must evaluate to true.", Predicates.zero().apply(zeroBigDecimal));
         }
         {
             final int one = 1;
             assertFalse("1 must evaluate to false.", Predicates.zero().apply(one));
         }
         {
             final BigInteger oneBigInteger = BigInteger.ONE;
             assertFalse("1 BigInteger must evaluate to false.", Predicates.zero().apply(oneBigInteger));
         }
         {
             final BigDecimal oneBigDecimal = BigDecimal.ONE;
             assertFalse("1 BigDecimal must evaluate to false.", Predicates.zero().apply(oneBigDecimal));
         }
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testZeroWithNullInput() {
         {
             assertTrue("With null input an IllegalArgumentException must be thrown.", Predicates.zero().apply(null));
         }
     }
 
     @Test
     public void testIsNullWithNullInput() {
         {
             assertTrue("Null input should evaluate to true.", Predicates.isNull().apply(null));
         }
     }
 
     @Test
     public void testIsNullWithNotNullInput() {
         {
             assertFalse("Not null input should evaluate to false.", Predicates.isNull().apply(new Object()));
         }
     }
 
     @Test
     public void testNotNullWithNotNullInput() {
         {
             assertTrue("Not null input should evaluate to true.", Predicates.notNull().apply(new Object()));
         }
     }
 
     @Test
     public void testNotNullWithNullInput() {
         {
            assertFalse("Not null input should evaluate to false.", Predicates.notNull().apply(null));
         }
     }
 }
