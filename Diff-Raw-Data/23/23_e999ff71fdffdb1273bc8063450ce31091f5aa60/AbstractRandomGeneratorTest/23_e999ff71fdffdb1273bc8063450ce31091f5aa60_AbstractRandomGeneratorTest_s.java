 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.commons.math.random;
 
 /**
  * Test cases for the AbstractRandomGenerator class.
  *
  * @version $Id$
  */
 
 public class AbstractRandomGeneratorTest extends RandomGeneratorAbstractTest {
    
    @Override
    public void testNextInt2() {
        // Currently broken.  Remove this stub when MATH-640 is resolved
    }
    
    @Override
    public void testNextLong2() {
        // Currently broken.  Remove this stub when MATH-640 is resolved
    }
 
     public AbstractRandomGeneratorTest() {
         super();
     }
     
     protected RandomGenerator makeGenerator() {
         RandomGenerator generator = new TestRandomGenerator();
        generator.setSeed(1000);
         return generator;
     }
     
 }
